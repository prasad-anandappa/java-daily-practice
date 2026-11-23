import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

/**
 * Day 025 — Building H2O (LeetCode 1117, Hard concurrency variant).
 *
 * Threads arrive unpredictably as hydrogen or oxygen atoms. Atoms may only
 * be released in complete water molecules: every consecutive group of three
 * released atoms must contain exactly two H and one O, in any order. No atom
 * of an incomplete molecule may slip into the next group, no group may wait
 * forever once its atoms have arrived, and the synchroniser must be reusable
 * for an unbounded stream of molecules.
 *
 * Approach: admission semaphores plus a reusable rendezvous barrier.
 * A hydrogen thread must first acquire one of only 2 hydrogen permits and
 * an oxygen thread the single oxygen permit, so at most one molecule's
 * worth of atoms (2H + 1O) can be inside the rendezvous at any moment.
 * Admitted atoms run their release action, then wait on a CyclicBarrier
 * of 3 parties. The barrier's action — run once per trip, before any
 * waiting thread resumes — replenishes the permits (2H + 1O), admitting
 * exactly the next molecule. Because permits are only replenished by a
 * completed group, groups can never overlap or interleave, and because
 * the barrier is cyclic the same object serves every molecule.
 *
 * Why not a simple counter + wait/notify: counting arrivals under a lock
 * is easy to get subtly wrong — a notified thread can wake for the wrong
 * molecule (spurious / stolen wakeups) and a counter reset races with the
 * next group's arrivals. The semaphore caps make the "at most one group
 * in flight" invariant structural rather than something each wakeup must
 * re-check, and the barrier gives the group a single, well-defined
 * release point.
 */
public class BuildingH2O {

    private final Semaphore hydrogenPermits = new Semaphore(2);
    private final Semaphore oxygenPermits = new Semaphore(1);
    private final CyclicBarrier moleculeBarrier = new CyclicBarrier(3, () -> {
        hydrogenPermits.release(2);
        oxygenPermits.release(1);
    });

    public void hydrogen(Runnable releaseHydrogen) throws InterruptedException {
        hydrogenPermits.acquire();
        try {
            releaseHydrogen.run();
            moleculeBarrier.await();
        } catch (BrokenBarrierException e) {
            throw new IllegalStateException("Molecule barrier broken", e);
        }
    }

    public void oxygen(Runnable releaseOxygen) throws InterruptedException {
        oxygenPermits.acquire();
        try {
            releaseOxygen.run();
            moleculeBarrier.await();
        } catch (BrokenBarrierException e) {
            throw new IllegalStateException("Molecule barrier broken", e);
        }
    }

    // ---- Checks ----

    /**
     * Runs {@code molecules} worth of atoms (2 per molecule H, 1 O) on
     * separate threads in the given arrival order, with optional random
     * start jitter, and verifies the recorded release stream: correct
     * length and counts, and every consecutive triple a permutation of
     * HHO. Fails (rather than hanging the suite) if threads do not finish
     * within the timeout — a deadlock is a test failure, not a stall.
     */
    private static void check(String label, List<Character> arrival, long jitterMillis, long seed)
            throws InterruptedException {
        int molecules = (int) arrival.stream().filter(c -> c == 'O').count();
        if (arrival.size() != 3 * molecules) {
            throw new IllegalArgumentException(label + ": arrival is not whole molecules");
        }
        BuildingH2O h2o = new BuildingH2O();
        StringBuilder out = new StringBuilder();
        Object outLock = new Object();
        Random jitter = new Random(seed);
        List<Thread> threads = new ArrayList<>();
        for (char atom : arrival) {
            long delay = jitterMillis == 0 ? 0 : jitter.nextLong(jitterMillis + 1);
            Thread t = new Thread(() -> {
                try {
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                    if (atom == 'H') {
                        h2o.hydrogen(() -> {
                            synchronized (outLock) {
                                out.append('H');
                            }
                        });
                    } else {
                        h2o.oxygen(() -> {
                            synchronized (outLock) {
                                out.append('O');
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            });
            threads.add(t);
        }
        for (Thread t : threads) {
            t.start();
        }
        long deadlineMillis = 15_000;
        for (Thread t : threads) {
            t.join(deadlineMillis);
            if (t.isAlive()) {
                throw new AssertionError(label + ": thread did not finish within "
                        + deadlineMillis + " ms — deadlock suspected; partial output \""
                        + out + "\"");
            }
        }
        String result;
        synchronized (outLock) {
            result = out.toString();
        }
        if (result.length() != arrival.size()) {
            throw new AssertionError(label + ": released " + result.length()
                    + " atoms, expected " + arrival.size() + " (\"" + result + "\")");
        }
        for (int g = 0; g < molecules; g++) {
            String group = result.substring(3 * g, 3 * g + 3);
            int h = 0;
            for (char c : group.toCharArray()) {
                if (c == 'H') {
                    h++;
                }
            }
            if (h != 2 || group.indexOf('O') < 0) {
                throw new AssertionError(label + ": group " + g + " is \"" + group
                        + "\", not a permutation of HHO; full output \"" + result + "\"");
            }
        }
        System.out.println("PASS " + label + " (" + molecules + " molecules) = \"" + result + "\"");
    }

    private static List<Character> arrival(String s) {
        List<Character> list = new ArrayList<>();
        for (char c : s.toCharArray()) {
            list.add(c);
        }
        return list;
    }

    private static List<Character> shuffledArrival(int molecules, long seed) {
        List<Character> list = new ArrayList<>();
        for (int i = 0; i < molecules; i++) {
            list.add('H');
            list.add('H');
            list.add('O');
        }
        Collections.shuffle(list, new Random(seed));
        return list;
    }

    public static void main(String[] args) throws InterruptedException {
        check("single molecule, HHO order", arrival("HHO"), 0, 1);
        check("single molecule, oxygen first", arrival("OHH"), 0, 2);
        check("single molecule, interleaved", arrival("HOH"), 0, 3);
        check("oxygen flood arrives first", arrival("OOOHHHHHH"), 0, 4);
        check("hydrogen flood arrives first", arrival("HHHHHHOOO"), 0, 5);
        check("3 molecules shuffled", shuffledArrival(3, 42), 0, 6);
        check("20 molecules shuffled with jitter", shuffledArrival(20, 7), 5, 7);
        check("50 molecules shuffled with jitter", shuffledArrival(50, 99), 3, 8);
        System.out.println("All checks passed.");
    }
}
