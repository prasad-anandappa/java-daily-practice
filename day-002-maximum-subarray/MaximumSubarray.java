import java.util.Arrays;

/**
 * Day 002 — Maximum Subarray.
 *
 * Given an integer array, find the contiguous subarray with the largest
 * sum and return that sum. The array has at least one element, and the
 * subarray must contain at least one element.
 *
 * Approach: Kadane's algorithm — a single pass keeping the best sum of a
 * subarray ending at the current position. At each value, either extend
 * the previous run or start a new run at this value, whichever is larger;
 * the answer is the best ending-here value seen anywhere. O(n) time,
 * O(1) space — versus O(n^2) for checking every subarray.
 */
public class MaximumSubarray {

    public static int maxSubArray(int[] nums) {
        if (nums.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }
        int best = nums[0];
        int endingHere = nums[0];
        for (int i = 1; i < nums.length; i++) {
            endingHere = Math.max(nums[i], endingHere + nums[i]);
            best = Math.max(best, endingHere);
        }
        return best;
    }

    private static void check(int[] nums, int expected) {
        int actual = maxSubArray(nums);
        if (actual != expected) {
            throw new AssertionError("maxSubArray(" + Arrays.toString(nums) + ") = " + actual
                    + ", expected " + expected);
        }
        System.out.println("PASS maxSubArray(" + Arrays.toString(nums) + ") = " + actual);
    }

    public static void main(String[] args) {
        check(new int[] { -2, 1, -3, 4, -1, 2, 1, -5, 4 }, 6);
        check(new int[] { 1 }, 1);
        check(new int[] { 5, 4, -1, 7, 8 }, 23);
        check(new int[] { -1, -2, -3 }, -1);
        check(new int[] { -2, -1 }, -1);
        check(new int[] { 2, -1, 2 }, 3);
        System.out.println("All checks passed.");
    }
}
