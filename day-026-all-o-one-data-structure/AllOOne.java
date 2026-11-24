import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
 * Day 026 — All O(1) Data Structure (LeetCode 432, Hard).
 *
 * Maintain a multiset of string keys with counts. Support, all in O(1):
 *   inc(key)    — add one occurrence (insert with count 1 if absent)
 *   dec(key)    — remove one occurrence (delete the key at count 0; no-op if absent)
 *   getMaxKey() — any key with the maximum count ("" if empty)
 *   getMinKey() — any key with the minimum count ("" if empty)
 *
 * Approach: bucket the keys by count. Each distinct count that currently
 * occurs is a node ("bucket") in a doubly linked list kept sorted by count,
 * holding the set of keys at that count. A HashMap key -> bucket locates a
 * key's bucket in O(1). inc/dec moves a key to the neighbouring bucket
 * (count+1 / count-1): because counts change by exactly one, the target
 * bucket is either the immediate neighbour or a fresh bucket spliced in
 * next to the current one — never a search. Empty buckets are unlinked.
 * Sentinel head/tail buckets (counts 0 / +infinity, never holding keys)
 * remove all edge cases: the max key lives in tail.prev, the min key in
 * head.next. Every operation is a constant number of map/set/list edits.
 */
public class AllOOne {

    private static final class Bucket {
        final int count;
        final Set<String> keys = new HashSet<>();
        Bucket prev;
        Bucket next;

        Bucket(int count) {
            this.count = count;
        }
    }

    private final Bucket head = new Bucket(0);              // sentinel, count 0
    private final Bucket tail = new Bucket(Integer.MAX_VALUE); // sentinel
    private final Map<String, Bucket> where = new HashMap<>();

    public AllOOne() {
        head.next = tail;
        tail.prev = head;
    }

    /** Splice a fresh bucket for {@code count} in immediately after {@code at}. */
    private Bucket insertAfter(Bucket at, int count) {
        Bucket b = new Bucket(count);
        b.prev = at;
        b.next = at.next;
        at.next.prev = b;
        at.next = b;
        return b;
    }

    private void unlink(Bucket b) {
        b.prev.next = b.next;
        b.next.prev = b.prev;
    }

    /** Move {@code key} out of {@code from}; unlink the bucket if it empties. */
    private void removeFrom(String key, Bucket from) {
        from.keys.remove(key);
        if (from.keys.isEmpty()) {
            unlink(from);
        }
    }

    public void inc(String key) {
        Bucket cur = where.get(key);
        if (cur == null) {
            // New key, count 1: bucket 1 is head.next or a fresh bucket after head.
            Bucket one = (head.next.count == 1) ? head.next : insertAfter(head, 1);
            one.keys.add(key);
            where.put(key, one);
            return;
        }
        int target = cur.count + 1;
        Bucket dest = (cur.next.count == target) ? cur.next : insertAfter(cur, target);
        dest.keys.add(key);
        where.put(key, dest);
        removeFrom(key, cur);
    }

    public void dec(String key) {
        Bucket cur = where.get(key);
        if (cur == null) {
            return; // absent: no-op, per the problem contract
        }
        if (cur.count == 1) {
            where.remove(key);
            removeFrom(key, cur);
            return;
        }
        int target = cur.count - 1;
        Bucket dest = (cur.prev.count == target) ? cur.prev : insertAfter(cur.prev, target);
        dest.keys.add(key);
        where.put(key, dest);
        removeFrom(key, cur);
    }

    public String getMaxKey() {
        return tail.prev == head ? "" : tail.prev.keys.iterator().next();
    }

    public String getMinKey() {
        return head.next == tail ? "" : head.next.keys.iterator().next();
    }

    // ---------- checks ----------

    private static void checkMax(AllOOne s, String... allowed) {
        String actual = s.getMaxKey();
        for (String a : allowed) {
            if (a.equals(actual)) {
                System.out.println("PASS getMaxKey() = \"" + actual + "\"");
                return;
            }
        }
        throw new AssertionError("getMaxKey() = \"" + actual + "\", expected one of "
                + String.join("/", allowed));
    }

    private static void checkMin(AllOOne s, String... allowed) {
        String actual = s.getMinKey();
        for (String a : allowed) {
            if (a.equals(actual)) {
                System.out.println("PASS getMinKey() = \"" + actual + "\"");
                return;
            }
        }
        throw new AssertionError("getMinKey() = \"" + actual + "\", expected one of "
                + String.join("/", allowed));
    }

    /** Naive oracle: recompute max/min counts from a plain count map. */
    private static void oracleCheck(AllOOne s, Map<String, Integer> counts, int step) {
        if (counts.isEmpty()) {
            if (!s.getMaxKey().isEmpty() || !s.getMinKey().isEmpty()) {
                throw new AssertionError("step " + step + ": empty structure must return \"\"");
            }
            return;
        }
        int max = counts.values().stream().mapToInt(Integer::intValue).max().orElseThrow();
        int min = counts.values().stream().mapToInt(Integer::intValue).min().orElseThrow();
        String gotMax = s.getMaxKey();
        String gotMin = s.getMinKey();
        if (!counts.containsKey(gotMax) || counts.get(gotMax) != max) {
            throw new AssertionError("step " + step + ": getMaxKey()=\"" + gotMax
                    + "\" but true max count is " + max + " in " + new TreeMap<>(counts));
        }
        if (!counts.containsKey(gotMin) || counts.get(gotMin) != min) {
            throw new AssertionError("step " + step + ": getMinKey()=\"" + gotMin
                    + "\" but true min count is " + min + " in " + new TreeMap<>(counts));
        }
    }

    public static void main(String[] args) {
        // Empty structure.
        AllOOne empty = new AllOOne();
        checkMax(empty, "");
        checkMin(empty, "");

        // Canonical LeetCode sequence.
        AllOOne s = new AllOOne();
        s.inc("hello");
        s.inc("hello");
        checkMax(s, "hello");
        checkMin(s, "hello");
        s.inc("leet");
        checkMax(s, "hello");
        checkMin(s, "leet");
        s.dec("hello");
        s.dec("hello"); // hello leaves entirely; bucket 1 and 2 churn
        checkMax(s, "leet");
        checkMin(s, "leet");
        s.dec("leet");
        checkMax(s, "");
        checkMin(s, "");

        // dec of an absent key is a no-op; dec to zero then re-inc restarts at 1.
        AllOOne t = new AllOOne();
        t.dec("ghost");
        checkMax(t, "");
        t.inc("a");
        t.inc("a");
        t.inc("b");
        t.dec("a");
        t.dec("a");
        checkMax(t, "b");
        checkMin(t, "b");
        t.inc("a");
        checkMin(t, "a", "b"); // both at count 1
        checkMax(t, "a", "b");

        // Bucket splicing: counts that skip — raise x to 3 while y sits at 1,
        // forcing fresh buckets on both the way up and the way down.
        AllOOne u = new AllOOne();
        u.inc("x");
        u.inc("y");
        u.inc("x");
        u.inc("x");
        checkMax(u, "x");
        checkMin(u, "y");
        u.dec("x");
        u.dec("x");
        checkMax(u, "x", "y"); // tied at 1
        checkMin(u, "x", "y");

        // Randomised cross-check against the naive oracle (fixed seed).
        AllOOne r = new AllOOne();
        Map<String, Integer> counts = new HashMap<>();
        String[] pool = { "a", "b", "c", "d", "e", "f", "g", "h" };
        Random rnd = new Random(432L);
        for (int step = 0; step < 20_000; step++) {
            String key = pool[rnd.nextInt(pool.length)];
            if (rnd.nextInt(100) < 55) {
                r.inc(key);
                counts.merge(key, 1, Integer::sum);
            } else {
                r.dec(key);
                counts.computeIfPresent(key, (k, v) -> v == 1 ? null : v - 1);
            }
            oracleCheck(r, counts, step);
        }
        System.out.println("PASS randomised oracle cross-check (20,000 ops, seed 432)");

        System.out.println("All checks passed.");
    }
}
