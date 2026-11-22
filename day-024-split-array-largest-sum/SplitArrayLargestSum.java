import java.util.Arrays;
import java.util.Random;

/**
 * Day 024 — Split Array Largest Sum (LeetCode 410, Hard).
 *
 * Given an array of non-negative integers nums and an integer k, split
 * nums into exactly k non-empty contiguous subarrays so that the largest
 * subarray sum is as small as possible. Return that minimised largest sum.
 *
 * Approach: binary search on the answer. For a candidate cap C, ask the
 * feasibility question "can nums be split into at most k subarrays, each
 * with sum <= C?" — answered greedily in one pass: keep extending the
 * current subarray while it fits under C, and start a new one only when
 * the next element would overflow. Greedy is optimal for feasibility
 * because, for a fixed cap, making each subarray as long as possible can
 * never increase the number of subarrays needed.
 *
 * Feasibility is monotone in C: if a cap works, every larger cap works
 * too (the same split still fits), and if a cap fails, every smaller cap
 * fails. That monotonicity is exactly what binary search needs. The
 * search interval is [max(nums), sum(nums)]: no cap below the largest
 * single element can work (that element must live somewhere), and the
 * total sum always works (one subarray, and k >= 1 refines it further
 * only when k allows — the greedy check settles exact feasibility).
 *
 * Sums can overflow int on large inputs, so the search runs in long and
 * only the final answer — bounded by the total sum of an int array that
 * fits the problem constraints — is cast back.
 *
 * A classic partition DP (O(k * n^2)) is kept below purely as a test
 * oracle, and the self-checks cross-verify the two implementations on
 * exhaustive small inputs plus randomised cases.
 */
public class SplitArrayLargestSum {

    public static int splitArray(int[] nums, int k) {
        if (nums == null || nums.length == 0) {
            throw new IllegalArgumentException("nums must be non-empty");
        }
        if (k < 1 || k > nums.length) {
            throw new IllegalArgumentException("k must be in [1, nums.length]: " + k);
        }
        long lo = 0; // inclusive lower bound: largest single element
        long hi = 0; // inclusive upper bound: total sum (always feasible)
        for (int x : nums) {
            if (x < 0) {
                throw new IllegalArgumentException("nums must be non-negative: " + x);
            }
            lo = Math.max(lo, x);
            hi += x;
        }
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            if (canSplit(nums, k, mid)) {
                hi = mid; // mid works; try to do better
            } else {
                lo = mid + 1; // mid fails; every smaller cap fails too
            }
        }
        return (int) lo;
    }

    /** Can nums be split into at most k subarrays each with sum <= cap? */
    private static boolean canSplit(int[] nums, int k, long cap) {
        int used = 1;
        long current = 0;
        for (int x : nums) {
            if (current + x <= cap) {
                current += x;
            } else {
                used++;
                if (used > k) {
                    return false;
                }
                current = x; // x <= cap is guaranteed: cap >= max(nums)
            }
        }
        return used <= k;
    }

    // ---- Partition DP over prefixes, used only as a test oracle ----

    private static int splitArrayDp(int[] nums, int k) {
        int n = nums.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }
        // dp[i] = best (minimised largest sum) splitting first i elements
        // into the current number of parts.
        final long INF = Long.MAX_VALUE / 4;
        long[] dp = new long[n + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0;
        for (int parts = 1; parts <= k; parts++) {
            long[] next = new long[n + 1];
            Arrays.fill(next, INF);
            for (int i = parts; i <= n; i++) { // need >= 1 element per part
                for (int j = parts - 1; j < i; j++) {
                    long lastPart = prefix[i] - prefix[j];
                    next[i] = Math.min(next[i], Math.max(dp[j], lastPart));
                }
            }
            dp = next;
        }
        return (int) dp[n];
    }

    // ---- Self-checks ----

    private static void check(int[] nums, int k, int expected) {
        int actual = splitArray(nums, k);
        if (actual != expected) {
            throw new AssertionError("splitArray(" + Arrays.toString(nums) + ", " + k
                    + ") = " + actual + ", expected " + expected);
        }
        int oracle = splitArrayDp(nums, k);
        if (oracle != actual) {
            throw new AssertionError("splitArray(" + Arrays.toString(nums) + ", " + k
                    + ") = " + actual + ", DP oracle says " + oracle);
        }
        System.out.println("PASS splitArray(" + Arrays.toString(nums) + ", " + k
                + ") = " + actual);
    }

    private static void checkAgainstOracle(int[] nums, int k) {
        int actual = splitArray(nums, k);
        int expected = splitArrayDp(nums, k);
        if (actual != expected) {
            throw new AssertionError("splitArray(" + Arrays.toString(nums) + ", " + k
                    + ") = " + actual + ", DP oracle says " + expected);
        }
    }

    /** Exhaustively cross-check on all small arrays over {0,1,2,3}. */
    private static void exhaustiveCrossCheck() {
        int[] values = { 0, 1, 2, 3 };
        for (int n = 1; n <= 5; n++) {
            int total = 1;
            for (int i = 0; i < n; i++) {
                total *= values.length;
            }
            for (int code = 0; code < total; code++) {
                int[] nums = new int[n];
                int c = code;
                for (int i = 0; i < n; i++) {
                    nums[i] = values[c % values.length];
                    c /= values.length;
                }
                for (int k = 1; k <= n; k++) {
                    checkAgainstOracle(nums, k);
                }
            }
        }
        System.out.println("PASS exhaustive small cross-check vs DP oracle");
    }

    private static void randomisedCrossCheck() {
        Random rng = new Random(410L);
        for (int trial = 0; trial < 500; trial++) {
            int n = 1 + rng.nextInt(12);
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) {
                nums[i] = rng.nextInt(21);
            }
            int k = 1 + rng.nextInt(n);
            checkAgainstOracle(nums, k);
        }
        System.out.println("PASS randomised cross-check vs DP oracle (500 trials)");
    }

    public static void main(String[] args) {
        check(new int[] { 7, 2, 5, 10, 8 }, 2, 18);
        check(new int[] { 1, 2, 3, 4, 5 }, 2, 9);
        check(new int[] { 1, 4, 4 }, 3, 4);
        check(new int[] { 1, 4, 4 }, 1, 9);
        check(new int[] { 1, 4, 4 }, 2, 5);
        check(new int[] { 10 }, 1, 10);
        check(new int[] { 0, 0, 0 }, 2, 0);
        check(new int[] { 5, 5, 5, 5 }, 4, 5);
        check(new int[] { 2, 3, 1, 2, 4, 3 }, 5, 4);
        check(new int[] { 1000000, 1000000 }, 1, 2000000);
        exhaustiveCrossCheck();
        randomisedCrossCheck();
        System.out.println("All checks passed.");
    }
}
