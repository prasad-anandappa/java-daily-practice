import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Day 011 — Count of Smaller Numbers After Self (LeetCode 315, Hard).
 *
 * Given an integer array nums, return an array counts where counts[i] is
 * the number of indices j &gt; i with nums[j] &lt; nums[i].
 *
 * Approach: merge sort on an array of indices, counting during the merge.
 * The indices are sorted by their values, never the values in place, so
 * each position's original place is remembered. While merging a left half
 * [lo, mid) and a right half [mid, hi) — both already sorted by value —
 * every element taken from the right half before a given left element is
 * strictly smaller than it (the merge takes the right element first only
 * when it is smaller) and originally stood to its right. So when a left
 * element is emitted, the number of right elements already emitted is
 * exactly its contribution from this merge, and contributions accumulate
 * over the O(log n) merges it takes part in. Equal values are never
 * counted: on a tie the left element is emitted first.
 *
 * That gives O(n log n) time and O(n) space, versus O(n^2) for the naive
 * scan. A second implementation below — coordinate compression plus a
 * Fenwick (binary indexed) tree, scanning from the right — is kept as an
 * independent oracle and is cross-checked in main: compression sorts the
 * distinct values and binary-searches each value's rank, the Fenwick
 * tree then answers "how many seen values are strictly smaller" in
 * O(log n) per element.
 */
public class CountOfSmallerAfterSelf {

    public static List<Integer> countSmaller(int[] nums) {
        if (nums == null) {
            throw new IllegalArgumentException("Input must not be null");
        }
        int n = nums.length;
        int[] counts = new int[n];
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }
        int[] temp = new int[n];
        mergeSort(nums, indices, temp, 0, n, counts);
        List<Integer> result = new ArrayList<>(n);
        for (int c : counts) {
            result.add(c);
        }
        return result;
    }

    /** Sorts indices[lo, hi) by nums value, accumulating counts. */
    private static void mergeSort(int[] nums, int[] indices, int[] temp,
                                  int lo, int hi, int[] counts) {
        if (hi - lo < 2) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        mergeSort(nums, indices, temp, lo, mid, counts);
        mergeSort(nums, indices, temp, mid, hi, counts);

        int i = lo;
        int j = mid;
        int k = lo;
        int rightTaken = 0;
        while (i < mid && j < hi) {
            if (nums[indices[j]] < nums[indices[i]]) {
                temp[k++] = indices[j++];
                rightTaken++;
            } else {
                // Tie goes to the left: equal values are not "smaller".
                counts[indices[i]] += rightTaken;
                temp[k++] = indices[i++];
            }
        }
        while (i < mid) {
            counts[indices[i]] += rightTaken;
            temp[k++] = indices[i++];
        }
        while (j < hi) {
            temp[k++] = indices[j++];
        }
        System.arraycopy(temp, lo, indices, lo, hi - lo);
    }

    // ---- Fenwick-tree implementation (independent oracle) ----

    private static class Fenwick {
        private final int[] bit;

        Fenwick(int size) {
            this.bit = new int[size + 1];
        }

        void add(int rank, int delta) {
            for (int i = rank; i < bit.length; i += i & -i) {
                bit[i] += delta;
            }
        }

        /** Sum of ranks 1..rank inclusive. */
        int sum(int rank) {
            int s = 0;
            for (int i = rank; i > 0; i -= i & -i) {
                s += bit[i];
            }
            return s;
        }
    }

    public static List<Integer> countSmallerFenwick(int[] nums) {
        int n = nums.length;
        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        // Distinct values, in order; rank of a value is its position + 1.
        int[] distinct = Arrays.stream(sorted).distinct().toArray();
        Fenwick tree = new Fenwick(distinct.length);
        Integer[] counts = new Integer[n];
        for (int i = n - 1; i >= 0; i--) {
            int rank = Arrays.binarySearch(distinct, nums[i]) + 1;
            counts[i] = tree.sum(rank - 1); // strictly smaller only
            tree.add(rank, 1);
        }
        return Arrays.asList(counts);
    }

    // ---- Brute force (quadratic), used only to cross-check small cases ----

    private static List<Integer> bruteForce(int[] nums) {
        List<Integer> result = new ArrayList<>(nums.length);
        for (int i = 0; i < nums.length; i++) {
            int c = 0;
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[j] < nums[i]) {
                    c++;
                }
            }
            result.add(c);
        }
        return result;
    }

    // ---- Self-checks ----

    private static void check(int[] nums, List<Integer> expected) {
        List<Integer> actual = countSmaller(nums);
        if (!actual.equals(expected)) {
            throw new AssertionError("countSmaller(" + Arrays.toString(nums) + ") = " + actual
                    + ", expected " + expected);
        }
        System.out.println("PASS countSmaller(" + Arrays.toString(nums) + ") = " + actual);
    }

    private static void checkAgainstOracles(int[] nums) {
        List<Integer> actual = countSmaller(nums);
        List<Integer> byFenwick = countSmallerFenwick(nums);
        List<Integer> byBruteForce = bruteForce(nums);
        if (!actual.equals(byFenwick) || !actual.equals(byBruteForce)) {
            throw new AssertionError("countSmaller(" + Arrays.toString(nums) + ") = " + actual
                    + ", fenwick says " + byFenwick + ", brute force says " + byBruteForce);
        }
        System.out.println("PASS countSmaller(" + Arrays.toString(nums) + ") = " + actual
                + " (agrees with Fenwick + brute force)");
    }

    public static void main(String[] args) {
        check(new int[] { 5, 2, 6, 1 }, List.of(2, 1, 1, 0));
        check(new int[] {}, List.of());
        check(new int[] { 1 }, List.of(0));
        check(new int[] { 2, 0, 1 }, List.of(2, 0, 0));
        check(new int[] { -1 }, List.of(0));
        check(new int[] { -1, -1 }, List.of(0, 0));
        check(new int[] { 5, 4, 3, 2, 1 }, List.of(4, 3, 2, 1, 0));
        check(new int[] { 1, 2, 3, 4 }, List.of(0, 0, 0, 0));
        check(new int[] { 3, 3, 3 }, List.of(0, 0, 0));
        check(new int[] { 2, 2, 1, 2 }, List.of(1, 1, 0, 0));
        check(new int[] { -5, 2, -1, 0, -3 }, List.of(0, 3, 1, 1, 0));

        checkAgainstOracles(new int[] { 7, 3, 9, 3, 1, 8, 3 });
        checkAgainstOracles(new int[] { 0, -2, 5, -2, 5, 0, -7, 4 });
        checkAgainstOracles(new int[] { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 });
        checkAgainstOracles(new int[] { 4, 4, 4, 4, 4 });
        checkAgainstOracles(new int[] { 100, -100, 50, -50, 0, 25, -25, 100 });

        System.out.println("All checks passed.");
    }
}
