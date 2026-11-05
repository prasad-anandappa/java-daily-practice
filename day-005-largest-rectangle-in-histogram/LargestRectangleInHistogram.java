import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Day 005 — Largest Rectangle in Histogram (LeetCode 84, Hard).
 *
 * Given bar heights in a histogram (each bar has width 1), return the area
 * of the largest rectangle that fits entirely under the bars.
 *
 * Approach: monotonic increasing stack of indices. For each bar, the stack
 * holds indices whose heights are still candidates for extension to the
 * right. When a lower bar (or a sentinel height of 0 past the end) arrives,
 * every taller bar on the stack is popped: its height is now final, its
 * right boundary is the current index, and its left boundary is the new
 * stack top (the previous lower bar). That gives the widest rectangle at
 * exactly that height in O(1) per pop. Each index is pushed and popped at
 * most once, so the scan is O(n) time, O(n) space.
 */
public class LargestRectangleInHistogram {

    public static int largestRectangleArea(int[] heights) {
        if (heights == null || heights.length == 0) {
            return 0;
        }
        Deque<Integer> stack = new ArrayDeque<>();
        int best = 0;
        for (int i = 0; i <= heights.length; i++) {
            int cur = i == heights.length ? 0 : heights[i];
            while (!stack.isEmpty() && cur < heights[stack.peek()]) {
                int height = heights[stack.pop()];
                int left = stack.isEmpty() ? -1 : stack.peek();
                int width = i - left - 1;
                best = Math.max(best, height * width);
            }
            stack.push(i);
        }
        return best;
    }

    private static void check(int[] heights, int expected) {
        int actual = largestRectangleArea(heights);
        if (actual != expected) {
            throw new AssertionError("largestRectangleArea(" + Arrays.toString(heights) + ") = " + actual
                    + ", expected " + expected);
        }
        System.out.println("PASS largestRectangleArea(" + Arrays.toString(heights) + ") = " + actual);
    }

    public static void main(String[] args) {
        check(new int[] { 2, 1, 5, 6, 2, 3 }, 10);
        check(new int[] { 2, 4 }, 4);
        check(new int[] { 6, 2, 5, 4, 5, 1, 6 }, 12);
        check(new int[] { 4, 2, 0, 3, 2, 5 }, 6);
        check(new int[] { 5, 4, 3, 2, 1 }, 9);
        check(new int[] { 1, 2, 3, 4, 5 }, 9);
        check(new int[] { 1, 1, 1, 1 }, 4);
        check(new int[] { 2, 1, 2 }, 3);
        check(new int[] { 0, 9 }, 9);
        check(new int[] { 0, 0, 0 }, 0);
        check(new int[] { 5 }, 5);
        check(new int[] {}, 0);
        System.out.println("All checks passed.");
    }
}
