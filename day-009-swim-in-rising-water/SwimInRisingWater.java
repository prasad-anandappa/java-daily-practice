import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Day 009 — Swim in Rising Water (LeetCode 778, Hard).
 *
 * An n x n grid gives the elevation of each cell. Rain raises the water
 * level uniformly: at time t you may stand on — and swim between
 * 4-directionally adjacent — cells whose elevation is at most t. Starting
 * at (0, 0), return the minimum t at which the bottom-right cell
 * (n - 1, n - 1) becomes reachable.
 *
 * Approach: this is a minimax path problem, solved with a Dijkstra
 * variant. Define the cost of a path as the maximum elevation anywhere
 * on it; the answer is the cheapest such cost over all paths. Dijkstra
 * still works because extending a path can never lower its cost — the
 * new cost is max(cost so far, next cell's elevation) — so costs leave
 * the priority queue in non-decreasing order and the first time the
 * target is popped its cost is final, exactly as with ordinary shortest
 * paths. best[r][c] stores the cheapest bottleneck known for that cell;
 * a neighbour is relaxed only when the path through the current cell
 * gives it a strictly smaller bottleneck. Plain BFS/DFS cannot work:
 * reachability at a fixed t is monotone, but finding the threshold by
 * search alone wastes the structure — the priority queue explores cells
 * in bottleneck order and settles the answer in one pass.
 */
public class SwimInRisingWater {

    public static int swimInWater(int[][] grid) {
        if (grid == null || grid.length == 0 || grid[0] == null || grid[0].length == 0) {
            throw new IllegalArgumentException("Grid must be non-empty");
        }
        int n = grid.length;
        for (int r = 0; r < n; r++) {
            if (grid[r] == null || grid[r].length != n) {
                throw new IllegalArgumentException("Grid must be square (n x n)");
            }
        }

        int[][] best = new int[n][n];
        for (int[] row : best) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
        best[0][0] = grid[0][0];

        // Entries are {bottleneck, row, col}, ordered by bottleneck.
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
        pq.offer(new int[] { best[0][0], 0, 0 });

        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int cost = cur[0];
            int r = cur[1];
            int c = cur[2];

            if (cost != best[r][c]) {
                continue; // stale entry superseded by a cheaper path
            }
            if (r == n - 1 && c == n - 1) {
                return cost;
            }
            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (nr < 0 || nr >= n || nc < 0 || nc >= n) {
                    continue;
                }
                int nextCost = Math.max(cost, grid[nr][nc]);
                if (nextCost < best[nr][nc]) {
                    best[nr][nc] = nextCost;
                    pq.offer(new int[] { nextCost, nr, nc });
                }
            }
        }
        // The grid is finite and fully connected, so this is unreachable.
        throw new IllegalStateException("Target was never settled");
    }

    private static String gridToString(int[][] grid) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < grid.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(grid[i]));
        }
        return sb.append("]").toString();
    }

    private static void check(int[][] grid, int expected) {
        int actual = swimInWater(grid);
        if (actual != expected) {
            throw new AssertionError("swimInWater(" + gridToString(grid)
                    + ") = " + actual + ", expected " + expected);
        }
        System.out.println("PASS swimInWater(" + gridToString(grid)
                + ") = " + actual);
    }

    private static void checkThrows(int[][] grid, String label) {
        boolean threw = false;
        try {
            swimInWater(grid);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        if (!threw) {
            throw new AssertionError("swimInWater(" + label
                    + ") should throw IllegalArgumentException");
        }
        System.out.println("PASS swimInWater(" + label
                + ") throws IllegalArgumentException");
    }

    public static void main(String[] args) {
        check(new int[][] { { 0, 2 }, { 1, 3 } }, 3);
        check(new int[][] {
                { 0, 1, 2, 3, 4 },
                { 24, 23, 22, 21, 5 },
                { 12, 13, 14, 15, 16 },
                { 11, 17, 18, 19, 20 },
                { 10, 9, 8, 7, 6 } }, 16);
        check(new int[][] { { 0 } }, 0);
        check(new int[][] { { 7 } }, 7);
        // Every path ends on the 8, so the bottleneck can never beat it.
        check(new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } }, 8);
        // The cheap corridor runs across the top and down the right edge;
        // the direct left column forces a bottleneck of 104.
        check(new int[][] { { 0, 100, 1 }, { 101, 102, 2 }, { 103, 104, 3 } }, 100);
        // A low snake winds through high ground: 0-1-2-3-... stays low.
        check(new int[][] {
                { 0, 1, 2, 3 },
                { 15, 14, 13, 4 },
                { 12, 11, 10, 5 },
                { 9, 8, 7, 6 } }, 6);
        // Start cell itself is the bottleneck.
        check(new int[][] { { 10, 0, 0 }, { 0, 0, 0 }, { 0, 0, 1 } }, 10);
        // Repeated calls must not leak state between runs.
        check(new int[][] { { 0, 2 }, { 1, 3 } }, 3);
        check(new int[][] { { 0 } }, 0);

        checkThrows(null, "null");
        checkThrows(new int[0][0], "empty");
        checkThrows(new int[][] { { 0, 1 }, { 2 } }, "ragged");

        System.out.println("All checks passed.");
    }
}
