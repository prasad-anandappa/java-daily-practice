import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Day 028 — Random Pick with Blacklist (LeetCode 710, Hard).
 *
 * Given n and a blacklist of distinct values in [0, n), pick() must return a
 * uniformly random integer in [0, n) that is NOT blacklisted, using O(B)
 * extra space (B = blacklist size) — building an array of all allowed values
 * is not allowed when n is huge and B is small.
 *
 * Approach: virtual remapping. Let M = n - B, the count of allowed values.
 * A uniform draw r in [0, M) is almost the answer — except when r itself is
 * blacklisted. Fix exactly those cases with a HashMap: each blacklisted
 * value b < M is remapped to a distinct allowed value taken from the tail
 * [M, n) that is not itself blacklisted. Draws >= M are never produced, so
 * the tail values are reachable only through the map, and every allowed
 * value ends up with probability exactly 1/M.
 *
 * Construction is O(B) time and space; pick() is O(1).
 */
public class RandomPickWithBlacklist {

    private final int bound; // M = n - blacklist size: draws come from [0, bound)
    private final Map<Integer, Integer> remap = new HashMap<>();
    private final Random random;

    public RandomPickWithBlacklist(int n, int[] blacklist) {
        this(n, blacklist, new Random());
    }

    /** Seedable constructor so the checks in main() are reproducible. */
    RandomPickWithBlacklist(int n, int[] blacklist, Random random) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive, got " + n);
        }
        if (blacklist.length >= n) {
            throw new IllegalArgumentException("blacklist must leave at least one allowed value");
        }
        this.random = random;
        this.bound = n - blacklist.length;

        Set<Integer> blocked = new HashSet<>();
        for (int b : blacklist) {
            if (b < 0 || b >= n) {
                throw new IllegalArgumentException("blacklist value out of range: " + b);
            }
            blocked.add(b);
        }

        // Hand each blacklisted value inside [0, bound) a distinct allowed
        // replacement scanned down from the tail [bound, n).
        int tail = n - 1;
        for (int b : blacklist) {
            if (b >= bound) {
                continue; // tail blacklist entries need no slot of their own
            }
            while (blocked.contains(tail)) {
                tail--;
            }
            remap.put(b, tail);
            tail--;
        }
    }

    /** Returns a uniformly random allowed value in [0, n). */
    public int pick() {
        int r = random.nextInt(bound);
        return remap.getOrDefault(r, r);
    }

    /** Number of remapped slots; exposed for the structural checks in main(). */
    int remapSize() {
        return remap.size();
    }

    // ------------------------------------------------------------------
    // Checks
    // ------------------------------------------------------------------

    private static void checkStructure(int n, int[] blacklist, int expectedRemaps) {
        RandomPickWithBlacklist picker =
                new RandomPickWithBlacklist(n, blacklist, new Random(42));
        if (picker.remapSize() != expectedRemaps) {
            throw new AssertionError("n=" + n + ": remapSize=" + picker.remapSize()
                    + ", expected " + expectedRemaps);
        }
        System.out.println("PASS structure n=" + n + " remaps=" + picker.remapSize());
    }

    /**
     * Draws many samples and verifies the hard contract: no pick is ever
     * blacklisted or out of range, and every allowed value is reached at
     * least once (with a fixed seed and this many draws, a correct uniform
     * picker misses an allowed value with vanishing probability, while a
     * broken mapping — e.g. one that can never reach a tail value — fails
     * deterministically).
     */
    private static void checkPicks(int n, int[] blacklist, int draws) {
        Set<Integer> blocked = new HashSet<>();
        for (int b : blacklist) {
            blocked.add(b);
        }
        RandomPickWithBlacklist picker =
                new RandomPickWithBlacklist(n, blacklist, new Random(123456789L));
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < draws; i++) {
            int v = picker.pick();
            if (v < 0 || v >= n) {
                throw new AssertionError("pick out of range: " + v + " for n=" + n);
            }
            if (blocked.contains(v)) {
                throw new AssertionError("pick returned blacklisted value " + v);
            }
            seen.add(v);
        }
        int allowed = n - blacklist.length;
        if (seen.size() != allowed) {
            throw new AssertionError("n=" + n + ": reached " + seen.size()
                    + " distinct allowed values, expected all " + allowed);
        }
        System.out.println("PASS picks n=" + n + " blacklist=" + blacklist.length
                + " draws=" + draws + " all " + allowed + " allowed values reached, none blacklisted");
    }

    private static void checkRoughlyUniform(int n, int[] blacklist, int draws) {
        int allowed = n - blacklist.length;
        RandomPickWithBlacklist picker =
                new RandomPickWithBlacklist(n, blacklist, new Random(987654321L));
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 0; i < draws; i++) {
            counts.merge(picker.pick(), 1, Integer::sum);
        }
        double expected = (double) draws / allowed;
        for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
            double ratio = e.getValue() / expected;
            if (ratio < 0.5 || ratio > 1.5) {
                throw new AssertionError("value " + e.getKey() + " picked " + e.getValue()
                        + " times vs expected ~" + (long) expected + " — not roughly uniform");
            }
        }
        System.out.println("PASS uniformity n=" + n + " draws=" + draws
                + " every allowed value within 50% of the expected count");
    }

    public static void main(String[] args) {
        // Structural: only blacklisted values below M need a remap slot.
        checkStructure(7, new int[] { 2, 3, 5 }, 2);   // M=4: 2,3 remapped; 5 is in the tail
        checkStructure(4, new int[] { 0, 1 }, 2);      // M=2: both remapped to 3,2
        checkStructure(10, new int[] {}, 0);           // empty blacklist: identity mapping
        checkStructure(10, new int[] { 9, 8, 7 }, 0);  // blacklist entirely in the tail
        checkStructure(5, new int[] { 0, 1, 2, 3 }, 1);// M=1: only the draw 0 exists, remapped to 4

        // Contract: never blacklisted, always in range, full coverage.
        checkPicks(7, new int[] { 2, 3, 5 }, 100_000);
        checkPicks(4, new int[] { 0, 1 }, 50_000);
        checkPicks(10, new int[] {}, 50_000);
        checkPicks(10, new int[] { 9, 8, 7 }, 100_000);
        checkPicks(5, new int[] { 0, 1, 2, 3 }, 20_000);
        checkPicks(1000, new int[] { 0, 500, 999 }, 200_000);

        // Uniformity: each allowed value should land near draws / allowed.
        checkRoughlyUniform(7, new int[] { 2, 3, 5 }, 400_000);
        checkRoughlyUniform(5, new int[] { 0, 1, 2, 3 }, 100_000);

        System.out.println("All checks passed.");
    }
}
