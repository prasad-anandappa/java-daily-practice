import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Day 016 — Max Points on a Line (LeetCode 149, Hard).
 *
 * Given an array of points where points[i] = [xi, yi] is a point on the
 * XY plane, return the maximum number of points that lie on the same
 * straight line.
 *
 * Approach: anchor each point i in turn and count, in a HashMap, how
 * many other points share each direction from i. Two points lie on the
 * same line through i exactly when their displacement from i has the
 * same reduced direction, so the direction is canonicalised: divide
 * (dx, dy) by gcd(|dx|, |dy|) and fix the sign (force dx > 0, or
 * dx == 0 and dy > 0, flipping both signs otherwise) so that opposite
 * rays on one line collapse to a single key. Points identical to i are
 * counted separately — they lie on every line through i, so they add
 * to whichever direction wins. The answer for anchor i is
 * duplicates + best direction count + 1 (i itself). Floating-point
 * slopes are never used: a double cannot distinguish very close slopes
 * and needs special cases for vertical lines, while the reduced
 * integer pair is exact. O(n^2) time, O(n) space — versus O(n^3) for
 * defining a line by every pair and recounting all points.
 */
public class MaxPointsOnALine {

    /** Reduced, sign-canonical direction used as a HashMap key. */
    private record Slope(int dx, int dy) {
    }

    public static int maxPoints(int[][] points) {
        if (points == null) {
            throw new IllegalArgumentException("points must not be null");
        }
        int n = points.length;
        if (n <= 2) {
            return n;
        }
        int best = 0;
        for (int i = 0; i < n; i++) {
            Map<Slope, Integer> bySlope = new HashMap<>();
            int duplicates = 0;
            int bestFromAnchor = 0;
            for (int j = 0; j < n; j++) {
                if (j == i) {
                    continue;
                }
                int dx = points[j][0] - points[i][0];
                int dy = points[j][1] - points[i][1];
                if (dx == 0 && dy == 0) {
                    duplicates++;
                    continue;
                }
                Slope slope = canonical(dx, dy);
                int count = bySlope.merge(slope, 1, Integer::sum);
                bestFromAnchor = Math.max(bestFromAnchor, count);
            }
            best = Math.max(best, bestFromAnchor + duplicates + 1);
        }
        return best;
    }

    private static Slope canonical(int dx, int dy) {
        int g = gcd(Math.abs(dx), Math.abs(dy));
        dx /= g;
        dy /= g;
        // One key per undirected line: (dx, dy) and (-dx, -dy) are the same.
        if (dx < 0 || (dx == 0 && dy < 0)) {
            dx = -dx;
            dy = -dy;
        }
        return new Slope(dx, dy);
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    // ---- Brute-force oracle (test only): every pair defines a line. ----

    private static int bruteForce(int[][] points) {
        int n = points.length;
        if (n <= 2) {
            return n;
        }
        int best = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                // Duplicate pair defines no direction; it is covered when a
                // distinct pair is tried, and by the fallback below.
                if (points[i][0] == points[j][0] && points[i][1] == points[j][1]) {
                    continue;
                }
                int count = 0;
                for (int k = 0; k < n; k++) {
                    long cross = (long) (points[j][0] - points[i][0]) * (points[k][1] - points[i][1])
                            - (long) (points[j][1] - points[i][1]) * (points[k][0] - points[i][0]);
                    if (cross == 0) {
                        count++;
                    }
                }
                best = Math.max(best, count);
            }
        }
        // All points identical: no distinct pair was ever tried.
        return best == 0 ? n : best;
    }

    private static void check(int[][] points, int expected) {
        int actual = maxPoints(points);
        if (actual != expected) {
            throw new AssertionError("maxPoints(" + java.util.Arrays.deepToString(points) + ") = "
                    + actual + ", expected " + expected);
        }
        int oracle = bruteForce(points);
        if (oracle != expected) {
            throw new AssertionError("oracle disagrees on " + java.util.Arrays.deepToString(points)
                    + ": oracle=" + oracle + ", expected " + expected);
        }
        System.out.println("PASS maxPoints(" + java.util.Arrays.deepToString(points) + ") = " + actual);
    }

    public static void main(String[] args) {
        check(new int[][] { { 1, 1 }, { 2, 2 }, { 3, 3 } }, 3);
        check(new int[][] { { 1, 1 }, { 3, 2 }, { 5, 3 }, { 4, 1 }, { 2, 3 }, { 1, 4 } }, 4);
        check(new int[][] { { 1, 1 } }, 1);
        check(new int[][] { { 1, 1 }, { 2, 2 } }, 2);
        check(new int[][] {}, 0);
        check(new int[][] { { 1, 1 }, { 1, 1 }, { 2, 2 }, { 2, 2 } }, 4);
        check(new int[][] { { 0, 0 }, { 0, 0 }, { 0, 0 } }, 3);
        check(new int[][] { { 0, 0 }, { 0, 5 }, { 0, -3 }, { 1, 0 } }, 3);
        check(new int[][] { { 0, 0 }, { 5, 0 }, { -3, 0 }, { 0, 1 } }, 3);
        check(new int[][] { { 0, 0 }, { 2, -2 }, { 3, -3 }, { -1, 1 }, { 1, 0 } }, 4);
        check(new int[][] { { 0, 0 }, { 2, 4 }, { 4, 8 }, { 1, 3 } }, 3);
        check(new int[][] { { 0, 0 }, { 94911151, 94911150 }, { 94911152, 94911151 } }, 2);

        // Randomised cross-check against the brute-force oracle.
        Random rng = new Random(149);
        for (int trial = 0; trial < 500; trial++) {
            int n = rng.nextInt(10);
            int[][] points = new int[n][2];
            for (int i = 0; i < n; i++) {
                points[i][0] = rng.nextInt(7) - 3;
                points[i][1] = rng.nextInt(7) - 3;
            }
            int fast = maxPoints(points);
            int slow = bruteForce(points);
            if (fast != slow) {
                throw new AssertionError("mismatch on " + java.util.Arrays.deepToString(points)
                        + ": hashMap=" + fast + ", oracle=" + slow);
            }
        }
        System.out.println("PASS randomised cross-check vs brute-force oracle (500 trials)");
        System.out.println("All checks passed.");
    }
}
