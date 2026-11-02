import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Day 001 — Two Sum.
 *
 * Given an array of integers and a target, return the indices of the two
 * numbers that add up to the target. Exactly one solution is assumed;
 * the same element may not be used twice.
 *
 * Approach: single pass with a HashMap from value -> index. For each value,
 * the complement (target - value) is either already in the map (done) or the
 * value is stored for a later element to match. O(n) time, O(n) space —
 * versus O(n^2) for the brute-force double loop.
 */
public class TwoSum {

    public static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            Integer match = seen.get(complement);
            if (match != null) {
                return new int[] { match, i };
            }
            seen.put(nums[i], i);
        }
        throw new IllegalArgumentException("No two elements add up to " + target);
    }

    private static void check(int[] nums, int target, int[] expected) {
        int[] actual = twoSum(nums, target);
        if (!Arrays.equals(actual, expected)) {
            throw new AssertionError("twoSum(" + Arrays.toString(nums) + ", " + target
                    + ") = " + Arrays.toString(actual)
                    + ", expected " + Arrays.toString(expected));
        }
        System.out.println("PASS twoSum(" + Arrays.toString(nums) + ", " + target
                + ") = " + Arrays.toString(actual));
    }

    public static void main(String[] args) {
        check(new int[] { 2, 7, 11, 15 }, 9, new int[] { 0, 1 });
        check(new int[] { 3, 2, 4 }, 6, new int[] { 1, 2 });
        check(new int[] { 3, 3 }, 6, new int[] { 0, 1 });
        check(new int[] { -1, -2, -3, -4, -5 }, -8, new int[] { 2, 4 });
        System.out.println("All checks passed.");
    }
}
