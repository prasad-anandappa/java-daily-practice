import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Day 017 — K-th Smallest Pair Distance (LeetCode 719, Hard).
 *
 * Given an integer array nums and an integer k, consider all pairs
 * (i, j) with 0 <= i < j < nums.length and define the distance of a
 * pair as |nums[i] - nums[j]|. Return the k-th smallest distance
 * among all pairs (k is 1-indexed).
 *
 * Approach: sort a copy of the array, then binary search the answer.
 * Distances depend only on values, not original positions, so sorting
 * loses nothing and turns "pairs with distance <= D" into a monotone
 * two-pointer count: for each right endpoint, advance the left pointer
 * while nums[right] - nums[left] > D; every index in [left, right)
 * then forms a valid pair with right, contributing right - left pairs.
 * That count is non-decreasing in D, so it can drive a binary search
 * for the smallest D whose count reaches k — that D is itself a real
 * pair distance and is exactly the k-th smallest. Enumerating all
 * pairs would be O(n^2) time and space; this is O(n log n + n log W)
 * time and O(n) space for the sorted copy, where W is the value range.
 */
public class KthSmallestPairDistance {

    public static int smallestDistancePair(int[] nums, int k) {
        if (nums == null) {
            throw new IllegalArgumentException("nums must not be null");
        }
        int n = nums.length;
        if (n < 2) {
            throw new IllegalArgumentException("need at least two elements, got " + n);
        }
        long totalPairs = (long) n * (n - 1) / 2;
        if (k < 1 || k > totalPairs) {
            throw new IllegalArgumentException("k=" + k + " out of range [1, " + totalPairs + "]");
        }
        int[] sorted = nums.clone();
        Arrays.sort(sorted);

        int lo = 0;
        int hi = sorted[n - 1] - sorted[0];
        // Invariant: countPairs(lo - 1) < k <= countPairs(hi).
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (countPairsAtMost(sorted, mid) >= k) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    /** Number of pairs (i < j) with sorted[j] - sorted[i] <= d, in O(n). */
    private static long countPairsAtMost(int[] sorted, int d) {
        long count = 0;
        int left = 0;
        for (int right = 0; right < sorted.length; right++) {
            while (sorted[right] - sorted[left] > d) {
                left++;
            }
            // left..right-1 all pair with right within distance d.
            count += right - left;
        }
        return count;
    }

    // ---- Brute-force oracle (test only): enumerate and sort all distances. ----

    private static int bruteForce(int[] nums, int k) {
        List<Integer> distances = new ArrayList<>();
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                distances.add(Math.abs(nums[i] - nums[j]));
            }
        }
        Collections.sort(distances);
        return distances.get(k - 1);
    }

    private static void check(int[] nums, int k, int expected) {
        int actual = smallestDistancePair(nums, k);
        if (actual != expected) {
            throw new AssertionError("smallestDistancePair(" + Arrays.toString(nums) + ", " + k
                    + ") = " + actual + ", expected " + expected);
        }
        int oracle = bruteForce(nums, k);
        if (oracle != expected) {
            throw new AssertionError("oracle disagrees on " + Arrays.toString(nums) + ", k=" + k
                    + ": oracle=" + oracle + ", expected " + expected);
        }
        System.out.println("PASS smallestDistancePair(" + Arrays.toString(nums) + ", " + k
                + ") = " + actual);
    }

    private static void checkThrows(int[] nums, int k) {
        try {
            smallestDistancePair(nums, k);
        } catch (IllegalArgumentException e) {
            System.out.println("PASS smallestDistancePair(" + Arrays.toString(nums) + ", " + k
                    + ") threw IllegalArgumentException");
            return;
        }
        throw new AssertionError("smallestDistancePair(" + Arrays.toString(nums) + ", " + k
                + ") should have thrown IllegalArgumentException");
    }

    public static void main(String[] args) {
        check(new int[] { 1, 3, 1 }, 1, 0);
        check(new int[] { 1, 3, 1 }, 2, 2);
        check(new int[] { 1, 3, 1 }, 3, 2);
        check(new int[] { 1, 1, 1 }, 2, 0);
        check(new int[] { 1, 6, 1 }, 3, 5);
        check(new int[] { 10, 20 }, 1, 10);
        check(new int[] { 1, 2, 3, 4, 5 }, 1, 1);
        check(new int[] { 1, 2, 3, 4, 5 }, 4, 1);
        check(new int[] { 1, 2, 3, 4, 5 }, 5, 2);
        check(new int[] { 1, 2, 3, 4, 5 }, 10, 4);
        check(new int[] { -5, -1, 3 }, 1, 4);
        check(new int[] { -5, -1, 3 }, 2, 4);
        check(new int[] { -5, -1, 3 }, 3, 8);
        check(new int[] { 0, 0, 0, 0 }, 6, 0);
        check(new int[] { 9, 2, 4, 4, 7 }, 1, 0);
        check(new int[] { 62, 100, 4 }, 1, 38);
        check(new int[] { 62, 100, 4 }, 2, 58);
        check(new int[] { 62, 100, 4 }, 3, 96);

        checkThrows(new int[] { 1, 2 }, 0);
        checkThrows(new int[] { 1, 2 }, 2);
        checkThrows(new int[] { 7 }, 1);

        // Randomised cross-check against the brute-force oracle: every k
        // for many small arrays, including duplicates and negatives.
        Random rng = new Random(719);
        for (int trial = 0; trial < 500; trial++) {
            int n = 2 + rng.nextInt(9);
            int[] nums = new int[n];
            for (int i = 0; i < n; i++) {
                nums[i] = rng.nextInt(21) - 10;
            }
            int total = n * (n - 1) / 2;
            for (int k = 1; k <= total; k++) {
                int fast = smallestDistancePair(nums, k);
                int slow = bruteForce(nums, k);
                if (fast != slow) {
                    throw new AssertionError("mismatch on " + Arrays.toString(nums) + ", k=" + k
                            + ": binarySearch=" + fast + ", oracle=" + slow);
                }
            }
        }
        System.out.println("PASS randomised cross-check vs brute-force oracle (500 trials, every k)");
        System.out.println("All checks passed.");
    }
}
