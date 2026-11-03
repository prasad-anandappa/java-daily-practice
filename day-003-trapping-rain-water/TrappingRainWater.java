import java.util.Arrays;

/**
 * Day 003 — Trapping Rain Water.
 *
 * Given an array of non-negative integers representing an elevation map
 * where the width of each bar is 1, compute how much water the map can
 * trap after raining. (LeetCode 42, Hard.)
 *
 * Approach: two pointers converging from both ends, carrying the tallest
 * bar seen so far from the left (leftMax) and from the right (rightMax).
 * At each step the side with the smaller current bar is settled: its
 * water level is bounded by the smaller of the two maxima, and since the
 * other side already guarantees a wall at least as tall as that side's
 * maximum, the amount above this bar is leftMax - height (or the mirror
 * image on the right). Move that pointer inward. O(n) time, O(1) space —
 * versus O(n) space for the precomputed prefix/suffix maxima version and
 * O(n^2) for scanning both sides of every bar.
 */
public class TrappingRainWater {

    public static int trap(int[] height) {
        if (height.length == 0) {
            return 0;
        }
        int left = 0;
        int right = height.length - 1;
        int leftMax = height[left];
        int rightMax = height[right];
        int water = 0;
        while (left < right) {
            if (height[left] < height[right]) {
                leftMax = Math.max(leftMax, height[left]);
                water += leftMax - height[left];
                left++;
            } else {
                rightMax = Math.max(rightMax, height[right]);
                water += rightMax - height[right];
                right--;
            }
        }
        return water;
    }

    private static void check(int[] height, int expected) {
        int actual = trap(height);
        if (actual != expected) {
            throw new AssertionError("trap(" + Arrays.toString(height) + ") = " + actual
                    + ", expected " + expected);
        }
        System.out.println("PASS trap(" + Arrays.toString(height) + ") = " + actual);
    }

    public static void main(String[] args) {
        check(new int[] { 0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1 }, 6);
        check(new int[] { 4, 2, 0, 3, 2, 5 }, 9);
        check(new int[] {}, 0);
        check(new int[] { 1 }, 0);
        check(new int[] { 1, 2, 3, 4, 5 }, 0);
        check(new int[] { 5, 4, 3, 2, 1 }, 0);
        check(new int[] { 3, 0, 3 }, 3);
        check(new int[] { 2, 0, 2 }, 2);
        check(new int[] { 5, 2, 1, 2, 1, 5 }, 14);
        check(new int[] { 0, 7, 1, 4, 6 }, 7);
        System.out.println("All checks passed.");
    }
}
