import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

/**
 * Day 019 — Shortest Subarray with Sum at Least K (LeetCode 862, Hard).
 *
 * Given an integer array nums (which may contain negatives) and an
 * integer k, return the length of the shortest non-empty contiguous
 * subarray whose sum is at least k, or -1 if no such subarray exists.
 *
 * Approach: prefix sums plus a monotonic deque. Let P[0] = 0 and
 * P[i + 1] = nums[0] + ... + nums[i], formed in long so large inputs
 * cannot overflow. The sum of nums[i..j - 1] is P[j] - P[i], so the
 * problem becomes: find the smallest j - i with P[j] - P[i] >= k.
 * Negatives make P non-monotonic, so the usual shrinking two-pointer
 * window is invalid — a later, smaller P can still pair with the
 * current P[j] to reach k.
 *
 * Scan j from 0 to n while the deque holds candidate left indices
 * with strictly increasing P values. For the current j, every front
 * index i with P[j] - P[i] >= k yields a feasible subarray; the front
 * is the earliest such i still queued, and once it pairs with this j
 * it can never give a shorter answer with a later j, so it is polled
 * and the best length is updated. Then any back index with
 * P[back] >= P[j] is useless — j is later (shorter subarrays) and
 * has a prefix no larger (sums at least as big) — so it is discarded
 * before j is appended. Each index enters and leaves the deque once,
 * giving O(n) time and O(n) space, versus O(n^2) for brute force.
 */
public class ShortestSubarrayAtLeastK {

    public static int shortestSubarray(int[] nums, int k) {
        if (nums == null) {
            throw new IllegalArgumentException("nums must not be null");
        }
        if (nums.length == 0) {
            throw new IllegalArgumentException("nums must not be empty");
        }
        int n = nums.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }

        Deque<Integer> deque = new ArrayDeque<>();
        int best = n + 1;
        for (int j = 0; j <= n; j++) {
            while (!deque.isEmpty() && prefix[j] - prefix[deque.peekFirst()] >= k) {
                best = Math.min(best, j - deque.pollFirst());
            }
            while (!deque.isEmpty() && prefix[deque.peekLast()] >= prefix[j]) {
                deque.pollLast();
            }
            deque.offerLast(j);
        }
        return best <= n ? best : -1;
    }

    // ---- Brute-force oracle (test only): try every subarray. ----

    private static int bruteForce(int[] nums, int k) {
        int n = nums.length;
        int best = n + 1;
        for (int i = 0; i < n; i++) {
            long sum = 0;
            for (int j = i; j < n; j++) {
                sum += nums[j];
                if (sum >= k) {
                    best = Math.min(best, j - i + 1);
                    break; // extending this start only lengthens it
                }
            }
        }
        return best <= n ? best : -1;
    }

    private static void check(int[] nums, int k, int expected) {
        int actual = shortestSubarray(nums, k);
        if (actual != expected) {
            throw new AssertionError("shortestSubarray(" + Arrays.toString(nums) + ", " + k
                    + ") = " + actual + ", expected " + expected);
        }
        int oracle = bruteForce(nums, k);
        if (oracle != expected) {
            throw new AssertionError("oracle disagrees on " + Arrays.toString(nums) + ", k=" + k
                    + ": oracle=" + oracle + ", expected " + expected);
        }
        System.out.println("PASS shortestSubarray(" + Arrays.toString(nums) + ", " + k
                + ") = " + actual);
    }

    private static void checkThrows(int[] nums, int k) {
        try {
            shortestSubarray(nums, k);
        } catch (IllegalArgumentException e) {
            System.out.println("PASS shortestSubarray(" + Arrays.toString(nums) + ", " + k
                    + ") threw IllegalArgumentException");
            return;
        }
        throw new AssertionError("shortestSubarray(" + Arrays.toString(nums) + ", " + k
                + ") should have thrown IllegalArgumentException");
    }

    public static void main(String[] args) {
        check(new int[] { 1 }, 1, 1);
        check(new int[] { 1, 2 }, 4, -1);
        check(new int[] { 2, -1, 2 }, 3, 3);
        check(new int[] { 84, -37, 32, 40, 95 }, 167, 3);
        check(new int[] { 48, 99, 37, 4, -31 }, 140, 2);
        check(new int[] { 1, 2, 3, 4, 5 }, 11, 3);
        check(new int[] { 1, 2, 3, 4, 5 }, 15, 5);
        check(new int[] { 1, 2, 3, 4, 5 }, 16, -1);
        check(new int[] { -5, 10, -5, 10 }, 10, 1);
        check(new int[] { 10, -2, 5 }, 12, 3);
        check(new int[] { 3, -2, 5, -1 }, 6, 3);
        check(new int[] { 5, -10, 15 }, 10, 1);
        check(new int[] { -3, -2, -1 }, 1, -1);
        check(new int[] { -1, -1, -1 }, -5, 1);
        check(new int[] { 1, -1, 1, -1, 1 }, 2, -1);
        check(new int[] { 1, -1, 1, -1, 1 }, 1, 1);
        check(new int[] { 2, 3, -4, 5, 6 }, 10, 2);
        check(new int[] { 7, -3, 4, -1, 8 }, 12, 5);
        check(new int[] { 0, 0, 0 }, 1, -1);
        check(new int[] { 0, 5, 0 }, 5, 1);
        // Overflow guards: prefix sums must be formed in long.
        check(new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE }, Integer.MAX_VALUE, 1);
        check(new int[] { -100000, 100000, 100000 }, 150000, 2);

        checkThrows(null, 1);
        checkThrows(new int[] {}, 1);

        // Randomised cross-check against the brute-force oracle:
        // negatives throughout, so the deque logic — not a shrinking
        // window — is what is being exercised.
        Random rng = new Random(862);
        for (int trial = 0; trial < 1000; trial++) {
            int n = 1 + rng.nextInt(12);
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) {
                nums[i] = rng.nextInt(21) - 10;
            }
            int k = rng.nextInt(31) - 5;
            int fast = shortestSubarray(nums, k);
            int slow = bruteForce(nums, k);
            if (fast != slow) {
                throw new AssertionError("mismatch on " + Arrays.toString(nums) + ", k=" + k
                        + ": deque=" + fast + ", oracle=" + slow);
            }
        }
        System.out.println("PASS randomised cross-check vs brute-force oracle (1000 trials)");
        System.out.println("All checks passed.");
    }
}
