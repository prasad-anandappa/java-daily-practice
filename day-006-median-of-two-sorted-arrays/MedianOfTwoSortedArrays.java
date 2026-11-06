/**
 * Day 006 — Median of Two Sorted Arrays (LeetCode 4, Hard).
 *
 * Given two sorted arrays nums1 and nums2, return the median of the merged
 * sorted order in O(log(min(m, n))) time — without merging.
 *
 * Approach: binary search the partition of the SHORTER array. A partition
 * takes i elements from nums1 and (half - i) from nums2 into the left side,
 * where half = (m + n + 1) / 2. The partition is correct exactly when every
 * left element is <= every right element: maxLeft1 <= minRight2 and
 * maxLeft2 <= minRight1. Empty sides use -inf / +inf sentinels. If
 * maxLeft1 > minRight2, i is too large (move left); if maxLeft2 > minRight1,
 * i is too small (move right). At the correct partition, the median is the
 * max of the left side (odd total) or the average of that max and the min
 * of the right side (even total). Searching the shorter array keeps the
 * second index in range and the search logarithmic.
 */
public class MedianOfTwoSortedArrays {

    public static double findMedianSortedArrays(int[] nums1, int[] nums2) {
        if (nums1.length > nums2.length) {
            return findMedianSortedArrays(nums2, nums1);
        }
        int m = nums1.length;
        int n = nums2.length;
        int half = (m + n + 1) / 2;
        int lo = 0;
        int hi = m;
        while (lo <= hi) {
            int i = (lo + hi) / 2;      // elements taken from nums1
            int j = half - i;           // elements taken from nums2
            int maxLeft1 = i == 0 ? Integer.MIN_VALUE : nums1[i - 1];
            int minRight1 = i == m ? Integer.MAX_VALUE : nums1[i];
            int maxLeft2 = j == 0 ? Integer.MIN_VALUE : nums2[j - 1];
            int minRight2 = j == n ? Integer.MAX_VALUE : nums2[j];
            if (maxLeft1 <= minRight2 && maxLeft2 <= minRight1) {
                int leftMax = Math.max(maxLeft1, maxLeft2);
                if ((m + n) % 2 == 1) {
                    return leftMax;
                }
                int rightMin = Math.min(minRight1, minRight2);
                return (leftMax + rightMin) / 2.0;
            } else if (maxLeft1 > minRight2) {
                hi = i - 1;
            } else {
                lo = i + 1;
            }
        }
        throw new IllegalArgumentException("Input arrays must be sorted.");
    }

    private static void check(int[] nums1, int[] nums2, double expected) {
        double actual = findMedianSortedArrays(nums1, nums2);
        if (Math.abs(actual - expected) > 1e-9) {
            throw new AssertionError("findMedianSortedArrays(" + java.util.Arrays.toString(nums1) + ", "
                    + java.util.Arrays.toString(nums2) + ") = " + actual + ", expected " + expected);
        }
        System.out.println("PASS median(" + java.util.Arrays.toString(nums1) + ", "
                + java.util.Arrays.toString(nums2) + ") = " + actual);
    }

    public static void main(String[] args) {
        check(new int[] { 1, 3 }, new int[] { 2 }, 2.0);
        check(new int[] { 1, 2 }, new int[] { 3, 4 }, 2.5);
        check(new int[] { 1, 3, 8 }, new int[] { 7, 9, 10, 11 }, 8.0);
        check(new int[] { 1, 2, 3 }, new int[] { 1, 2, 3 }, 2.0);
        check(new int[] {}, new int[] { 1 }, 1.0);
        check(new int[] { 2 }, new int[] {}, 2.0);
        check(new int[] {}, new int[] { 2, 3 }, 2.5);
        check(new int[] { -5, -3, -1 }, new int[] { 1, 3, 5 }, 0.0);
        check(new int[] { 1 }, new int[] { 2, 3, 4, 5 }, 3.0);
        check(new int[] { 5, 6, 7 }, new int[] { 1, 2, 3, 4 }, 4.0);
        System.out.println("All checks passed.");
    }
}
