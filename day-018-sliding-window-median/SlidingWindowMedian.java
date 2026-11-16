import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;

/**
 * Day 018 — Sliding Window Median (LeetCode 480, Hard).
 *
 * Given an integer array nums and a window size k, slide a window of
 * k consecutive elements from left to right and return the median of
 * each window: the middle value for odd k, or the mean of the two
 * middle values for even k.
 *
 * Approach: maintain the window split into two ordered multisets,
 * each a TreeMap from value to count. lo holds the smaller half and
 * hi the larger half, with the invariants that every value in lo is
 * <= every value in hi and loSize is either hiSize or hiSize + 1.
 * Then the median is lo's largest key (odd k), or the mean of lo's
 * largest and hi's smallest keys (even k), computed in long arithmetic
 * so Integer.MAX_VALUE pairs cannot overflow. Inserting the incoming
 * element goes to lo when it is <= lo's current maximum and to hi
 * otherwise; removing the outgoing element deletes one occurrence
 * from whichever side holds it — if a value occurs on both sides the
 * copies are equal, so removing from lo is interchangeable. Either
 * operation can break the size invariant by one, and rebalancing
 * repairs it by moving lo's maximum into hi or hi's minimum into lo,
 * which also preserves the ordering invariant. Sorting each window
 * from scratch would be O(n * k log k); this is O(n log k) time and
 * O(k) space.
 */
public class SlidingWindowMedian {

    /** Two ordered multisets holding one window, split at the median. */
    private static class MedianBag {
        private final TreeMap<Integer, Integer> lo = new TreeMap<>();
        private final TreeMap<Integer, Integer> hi = new TreeMap<>();
        private int loSize;
        private int hiSize;

        void add(int num) {
            if (lo.isEmpty() || num <= lo.lastKey()) {
                addTo(lo, num);
                loSize++;
            } else {
                addTo(hi, num);
                hiSize++;
            }
            rebalance();
        }

        void remove(int num) {
            if (lo.containsKey(num)) {
                removeFrom(lo, num);
                loSize--;
            } else if (hi.containsKey(num)) {
                removeFrom(hi, num);
                hiSize--;
            } else {
                throw new IllegalStateException("removing absent value " + num);
            }
            rebalance();
        }

        double median() {
            if (loSize == 0) {
                throw new IllegalStateException("median of an empty window");
            }
            if (loSize > hiSize) {
                return lo.lastKey();
            }
            // long sum: two int extremes added as ints would overflow.
            return ((long) lo.lastKey() + (long) hi.firstKey()) / 2.0;
        }

        private void rebalance() {
            if (loSize > hiSize + 1) {
                int moved = lo.lastKey();
                removeFrom(lo, moved);
                loSize--;
                addTo(hi, moved);
                hiSize++;
            } else if (loSize < hiSize) {
                int moved = hi.firstKey();
                removeFrom(hi, moved);
                hiSize--;
                addTo(lo, moved);
                loSize++;
            }
        }

        private static void addTo(TreeMap<Integer, Integer> map, int key) {
            map.merge(key, 1, Integer::sum);
        }

        private static void removeFrom(TreeMap<Integer, Integer> map, int key) {
            int count = map.get(key);
            if (count == 1) {
                map.remove(key);
            } else {
                map.put(key, count - 1);
            }
        }
    }

    public static double[] medianSlidingWindow(int[] nums, int k) {
        if (nums == null) {
            throw new IllegalArgumentException("nums must not be null");
        }
        if (k < 1 || k > nums.length) {
            throw new IllegalArgumentException(
                    "k=" + k + " out of range [1, " + nums.length + "]");
        }
        double[] result = new double[nums.length - k + 1];
        MedianBag window = new MedianBag();
        for (int i = 0; i < nums.length; i++) {
            window.add(nums[i]);
            if (i >= k) {
                window.remove(nums[i - k]);
            }
            if (i >= k - 1) {
                result[i - k + 1] = window.median();
            }
        }
        return result;
    }

    // ---- Brute-force oracle (test only): sort a copy of each window. ----

    private static double[] bruteForce(int[] nums, int k) {
        double[] result = new double[nums.length - k + 1];
        int[] copy = new int[k];
        for (int i = 0; i + k <= nums.length; i++) {
            System.arraycopy(nums, i, copy, 0, k);
            Arrays.sort(copy);
            if (k % 2 == 1) {
                result[i] = copy[k / 2];
            } else {
                result[i] = ((long) copy[k / 2 - 1] + (long) copy[k / 2]) / 2.0;
            }
        }
        return result;
    }

    private static void check(int[] nums, int k, double[] expected) {
        double[] actual = medianSlidingWindow(nums, k);
        if (!Arrays.equals(actual, expected)) {
            throw new AssertionError("medianSlidingWindow(" + Arrays.toString(nums) + ", " + k
                    + ") = " + Arrays.toString(actual)
                    + ", expected " + Arrays.toString(expected));
        }
        double[] oracle = bruteForce(nums, k);
        if (!Arrays.equals(oracle, expected)) {
            throw new AssertionError("oracle disagrees on " + Arrays.toString(nums) + ", k=" + k
                    + ": oracle=" + Arrays.toString(oracle)
                    + ", expected " + Arrays.toString(expected));
        }
        System.out.println("PASS medianSlidingWindow(" + Arrays.toString(nums) + ", " + k
                + ") = " + Arrays.toString(actual));
    }

    private static void checkThrows(int[] nums, int k) {
        try {
            medianSlidingWindow(nums, k);
        } catch (IllegalArgumentException e) {
            System.out.println("PASS medianSlidingWindow(" + Arrays.toString(nums) + ", " + k
                    + ") threw IllegalArgumentException");
            return;
        }
        throw new AssertionError("medianSlidingWindow(" + Arrays.toString(nums) + ", " + k
                + ") should have thrown IllegalArgumentException");
    }

    public static void main(String[] args) {
        check(new int[] { 1, 3, -1, -3, 5, 3, 6, 7 }, 3,
                new double[] { 1.0, -1.0, -1.0, 3.0, 5.0, 6.0 });
        check(new int[] { 1, 2, 3, 4, 2, 3, 1, 4, 2 }, 3,
                new double[] { 2.0, 3.0, 3.0, 3.0, 2.0, 3.0, 2.0 });
        check(new int[] { 1, 4, 2, 3 }, 4, new double[] { 2.5 });
        check(new int[] { 1, 3 }, 2, new double[] { 2.0 });
        check(new int[] { 4, 1, 7 }, 1, new double[] { 4.0, 1.0, 7.0 });
        check(new int[] { 5, 5, 5, 5 }, 3, new double[] { 5.0, 5.0 });
        check(new int[] { 5, 5, 5, 5 }, 2, new double[] { 5.0, 5.0, 5.0 });
        check(new int[] { -1, -2, -3, -4 }, 2, new double[] { -1.5, -2.5, -3.5 });
        check(new int[] { -1, -2, -3, -4 }, 4, new double[] { -2.5 });
        check(new int[] { 9, 7, 5, 3, 1 }, 3, new double[] { 7.0, 5.0, 3.0 });
        check(new int[] { 1, 2, 3, 4, 5 }, 5, new double[] { 3.0 });
        check(new int[] { 2, 2, 2, 1, 1, 1 }, 4, new double[] { 2.0, 1.5, 1.0 });
        // Overflow guards: the even-k mean must be formed in long.
        check(new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE }, 2,
                new double[] { 2147483647.0 });
        check(new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE }, 2,
                new double[] { -2147483648.0 });
        check(new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE }, 2,
                new double[] { -0.5 });

        checkThrows(new int[] { 1, 2 }, 0);
        checkThrows(new int[] { 1, 2 }, 3);
        checkThrows(new int[] {}, 1);

        // Randomised cross-check against the brute-force oracle: every k
        // for many small arrays, including duplicates and negatives.
        Random rng = new Random(480);
        for (int trial = 0; trial < 500; trial++) {
            int n = 1 + rng.nextInt(10);
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) {
                nums[i] = rng.nextInt(21) - 10;
            }
            for (int k = 1; k <= n; k++) {
                double[] fast = medianSlidingWindow(nums, k);
                double[] slow = bruteForce(nums, k);
                if (!Arrays.equals(fast, slow)) {
                    throw new AssertionError("mismatch on " + Arrays.toString(nums) + ", k=" + k
                            + ": bag=" + Arrays.toString(fast)
                            + ", oracle=" + Arrays.toString(slow));
                }
            }
        }
        System.out.println("PASS randomised cross-check vs brute-force oracle (500 trials, every k)");
        System.out.println("All checks passed.");
    }
}
