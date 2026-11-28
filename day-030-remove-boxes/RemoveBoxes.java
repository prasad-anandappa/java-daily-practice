import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Day 030 — Remove Boxes.
 *
 * Given boxes coloured by integers, repeatedly remove a group of k
 * consecutive boxes of the same colour for k * k points; the remaining
 * boxes close ranks and may merge into larger groups. Return the maximum
 * total score obtainable (LeetCode 546, Hard).
 *
 * Approach: memoised interval DP over the state (l, r, k) — the best
 * score for boxes[l..r] when k extra boxes of the same colour as
 * boxes[l] are attached just to its left and will be removed together
 * with it. Either that whole group goes now, scoring (k + 1)^2 plus the
 * untouched remainder, or some later box m with boxes[m] == boxes[l] is
 * saved for last: the segment between them is cleared first, then m's
 * group joins the attached group, growing k by one. Trying only those
 * two shapes of move is sufficient because an optimal play never splits
 * a same-colour group it intends to merge. O(n^4) time, O(n^3) space.
 */
public class RemoveBoxes {

    public static int removeBoxes(int[] boxes) {
        int n = boxes.length;
        if (n == 0) {
            return 0;
        }
        int[][][] memo = new int[n][n][n + 1];
        for (int[][] plane : memo) {
            for (int[] row : plane) {
                Arrays.fill(row, -1);
            }
        }
        return solve(boxes, 0, n - 1, 0, memo);
    }

    private static int solve(int[] boxes, int l, int r, int k, int[][][] memo) {
        if (l > r) {
            return 0;
        }
        if (memo[l][r][k] != -1) {
            return memo[l][r][k];
        }
        // Option 1: remove boxes[l] together with its k attached partners now.
        int best = (k + 1) * (k + 1) + solve(boxes, l + 1, r, 0, memo);
        // Option 2: keep boxes[l] and merge it with a later equal-coloured box.
        for (int m = l + 1; m <= r; m++) {
            if (boxes[m] == boxes[l]) {
                int candidate = solve(boxes, l + 1, m - 1, 0, memo)
                        + solve(boxes, m, r, k + 1, memo);
                best = Math.max(best, candidate);
            }
        }
        memo[l][r][k] = best;
        return best;
    }

    // ----- brute-force oracle (tests only) -----

    /** Exhaustive search over removal orders, memoised on the box sequence. */
    private static int bruteForce(List<Integer> boxes, Map<String, Integer> memo) {
        if (boxes.isEmpty()) {
            return 0;
        }
        String key = boxes.toString();
        Integer cached = memo.get(key);
        if (cached != null) {
            return cached;
        }
        int best = 0;
        int i = 0;
        while (i < boxes.size()) {
            int j = i;
            while (j < boxes.size() && boxes.get(j).equals(boxes.get(i))) {
                j++;
            }
            // Remove any positive number of boxes from this same-colour run.
            for (int take = 1; take <= j - i; take++) {
                List<Integer> next = new ArrayList<>(boxes);
                next.subList(i, i + take).clear();
                best = Math.max(best, take * take + bruteForce(next, memo));
            }
            i = j;
        }
        memo.put(key, best);
        return best;
    }

    private static int bruteForce(int[] boxes) {
        List<Integer> list = new ArrayList<>();
        for (int b : boxes) {
            list.add(b);
        }
        return bruteForce(list, new HashMap<>());
    }

    private static void check(int[] boxes, int expected) {
        int actual = removeBoxes(boxes);
        if (actual != expected) {
            throw new AssertionError("removeBoxes(" + Arrays.toString(boxes) + ") = "
                    + actual + ", expected " + expected);
        }
        System.out.println("PASS removeBoxes(" + Arrays.toString(boxes) + ") = " + actual);
    }

    public static void main(String[] args) {
        check(new int[] {}, 0);
        check(new int[] { 1 }, 1);
        check(new int[] { 1, 1, 1 }, 9);
        check(new int[] { 1, 2, 3, 4 }, 4);
        check(new int[] { 1, 2, 1 }, 5);
        check(new int[] { 1, 3, 2, 2, 2, 3, 4, 3, 1 }, 23);
        check(new int[] { 1, 1, 2, 1, 1 }, 17);
        check(new int[] { 3, 3, 3, 3 }, 16);

        // Randomised cross-check against the exhaustive oracle on small inputs.
        Random rng = new Random(30);
        for (int trial = 0; trial < 300; trial++) {
            int n = rng.nextInt(10);
            int[] boxes = new int[n];
            for (int i = 0; i < n; i++) {
                boxes[i] = 1 + rng.nextInt(3);
            }
            int fast = removeBoxes(boxes);
            int slow = bruteForce(boxes);
            if (fast != slow) {
                throw new AssertionError("mismatch on " + Arrays.toString(boxes)
                        + ": dp=" + fast + ", brute=" + slow);
            }
        }
        System.out.println("PASS randomised cross-check vs brute-force oracle (300 trials, n <= 9)");
        System.out.println("All checks passed.");
    }
}
