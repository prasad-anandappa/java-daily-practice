import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Day 010 — Burst Balloons (LeetCode 312, Hard).
 *
 * There are n balloons numbered 0..n-1, the i-th painted with nums[i].
 * Bursting balloon i earns nums[left] * nums[i] * nums[right] coins, where
 * left and right are its CURRENT neighbours; after the burst those neighbours
 * become adjacent to each other. Return the maximum coins obtainable by
 * bursting all balloons in some order.
 *
 * Approach: interval DP with memoised recursion. Pad the array with a 1 on
 * both ends — the imaginary end balloons can never be burst and act as fixed
 * boundaries. Define solve(l, r) as the maximum coins from bursting every
 * balloon strictly between l and r (open interval), with l and r still
 * standing. The key inversion is to choose which balloon k in (l, r) is
 * burst LAST: by the time k bursts, everything else inside (l, r) is already
 * gone, so k's neighbours are exactly l and r and it earns
 * a[l] * a[k] * a[r] — a term that no longer depends on the order inside the
 * two sub-intervals. That decouples the problem:
 *
 *   solve(l, r) = max over k in (l, r) of
 *                 solve(l, k) + solve(k, r) + a[l] * a[k] * a[r]
 *
 * Choosing the FIRST burst instead does not decouple anything, because the
 * reward for the first burst depends on neighbours that are still present,
 * while every later burst's neighbourhood keeps changing. "Last" is what
 * makes the subproblems independent. Memoisation over O(n^2) intervals,
 * each scanning O(n) choices for k, gives O(n^3) time and O(n^2) space —
 * versus O(n!) burst orders for brute force. Empty intervals (r - l < 2)
 * contribute 0.
 */
public class BurstBalloons {

    public static int maxCoins(int[] nums) {
        if (nums == null) {
            throw new IllegalArgumentException("Input must not be null");
        }
        int n = nums.length;
        int[] a = new int[n + 2];
        a[0] = 1;
        a[n + 1] = 1;
        System.arraycopy(nums, 0, a, 1, n);

        int[][] memo = new int[n + 2][n + 2];
        for (int[] row : memo) {
            Arrays.fill(row, -1);
        }
        return solve(a, 0, n + 1, memo);
    }

    /** Max coins from bursting balloons strictly between l and r. */
    private static int solve(int[] a, int l, int r, int[][] memo) {
        if (r - l < 2) {
            return 0;
        }
        if (memo[l][r] != -1) {
            return memo[l][r];
        }
        int best = 0;
        for (int k = l + 1; k < r; k++) {
            // k is burst last inside (l, r): its neighbours are l and r.
            int coins = solve(a, l, k, memo)
                    + solve(a, k, r, memo)
                    + a[l] * a[k] * a[r];
            if (coins > best) {
                best = coins;
            }
        }
        memo[l][r] = best;
        return best;
    }

    // ---- Brute force (exponential), used only to cross-check small cases ----

    private static int bruteForce(List<Integer> balloons) {
        if (balloons.isEmpty()) {
            return 0;
        }
        int best = 0;
        for (int i = 0; i < balloons.size(); i++) {
            int left = (i == 0) ? 1 : balloons.get(i - 1);
            int right = (i == balloons.size() - 1) ? 1 : balloons.get(i + 1);
            int gain = left * balloons.get(i) * right;
            List<Integer> rest = new ArrayList<>(balloons);
            rest.remove(i);
            best = Math.max(best, gain + bruteForce(rest));
        }
        return best;
    }

    private static int bruteForce(int[] nums) {
        List<Integer> list = new ArrayList<>();
        for (int v : nums) {
            list.add(v);
        }
        return bruteForce(list);
    }

    // ---- Self-checks ----

    private static void check(int[] nums, int expected) {
        int actual = maxCoins(nums);
        if (actual != expected) {
            throw new AssertionError("maxCoins(" + Arrays.toString(nums) + ") = " + actual
                    + ", expected " + expected);
        }
        System.out.println("PASS maxCoins(" + Arrays.toString(nums) + ") = " + actual);
    }

    private static void checkAgainstBruteForce(int[] nums) {
        int actual = maxCoins(nums);
        int expected = bruteForce(nums);
        if (actual != expected) {
            throw new AssertionError("maxCoins(" + Arrays.toString(nums) + ") = " + actual
                    + ", brute force says " + expected);
        }
        System.out.println("PASS maxCoins(" + Arrays.toString(nums) + ") = " + actual
                + " (matches brute force)");
    }

    public static void main(String[] args) {
        check(new int[] {}, 0);
        check(new int[] { 5 }, 5);
        check(new int[] { 1, 5 }, 10);
        check(new int[] { 3, 1, 5, 8 }, 167);
        check(new int[] { 1, 2, 3, 4, 5 }, 110);

        // Zero-valued balloons still change adjacency when burst.
        check(new int[] { 0, 5 }, 5);
        check(new int[] { 2, 0, 3 }, 9);

        // Cross-check the DP against exhaustive search on small inputs,
        // including duplicates and an unsorted mix where burst order matters.
        checkAgainstBruteForce(new int[] { 1, 2, 3 });
        checkAgainstBruteForce(new int[] { 4, 2, 5 });
        checkAgainstBruteForce(new int[] { 3, 3, 3 });
        checkAgainstBruteForce(new int[] { 7, 1, 4, 2, 6 });
        checkAgainstBruteForce(new int[] { 2, 4, 1, 3, 5, 2 });

        System.out.println("All checks passed.");
    }
}
