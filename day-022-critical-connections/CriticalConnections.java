import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Day 022 — Critical Connections in a Network (LeetCode 1192, Hard).
 *
 * There are n servers labelled 0..n-1 connected by undirected edges.
 * A connection is critical (a bridge) if removing it increases the
 * number of connected components. Return all critical connections.
 *
 * Approach: Tarjan's bridge-finding DFS. Each node gets a discovery
 * time disc[v] and a low-link value low[v] — the earliest discovery
 * time reachable from the subtree rooted at v using tree edges plus
 * at most one back edge. For a tree edge v -> to, the edge is a
 * bridge exactly when low[to] > disc[v]: nothing in to's subtree can
 * reach v or any ancestor of v without this edge, so removing it
 * disconnects the subtree. Back edges pull low values up and rescue
 * every edge on the cycle they close. One DFS computes all bridges:
 * O(V + E) time, O(V + E) space.
 *
 * main cross-checks Tarjan against a brute-force oracle (remove each
 * edge in turn and test connectivity by BFS/DFS) on thousands of
 * small random graphs, so correctness is verified, not assumed.
 */
public class CriticalConnections {

    private int timer;
    private int[] disc;
    private int[] low;
    private List<List<Integer>> graph;
    private List<List<Integer>> bridges;

    public List<List<Integer>> criticalConnections(int n, List<List<Integer>> connections) {
        graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        for (List<Integer> e : connections) {
            int u = e.get(0);
            int v = e.get(1);
            graph.get(u).add(v);
            graph.get(v).add(u);
        }
        disc = new int[n];
        low = new int[n];
        Arrays.fill(disc, -1);
        timer = 0;
        bridges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (disc[i] == -1) {
                dfs(i, -1);
            }
        }
        return bridges;
    }

    private void dfs(int v, int parent) {
        disc[v] = low[v] = timer++;
        for (int to : graph.get(v)) {
            if (to == parent) {
                continue;
            }
            if (disc[to] != -1) {
                // Back edge: v can reach an ancestor of itself.
                low[v] = Math.min(low[v], disc[to]);
            } else {
                dfs(to, v);
                low[v] = Math.min(low[v], low[to]);
                if (low[to] > disc[v]) {
                    bridges.add(List.of(v, to));
                }
            }
        }
    }

    // ---- Oracle (test only): remove each edge and test connectivity. ----

    private static List<List<Integer>> bruteForce(int n, List<List<Integer>> connections) {
        List<List<Integer>> result = new ArrayList<>();
        for (int skip = 0; skip < connections.size(); skip++) {
            List<List<Integer>> adj = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                adj.add(new ArrayList<>());
            }
            for (int i = 0; i < connections.size(); i++) {
                if (i == skip) {
                    continue;
                }
                int u = connections.get(i).get(0);
                int v = connections.get(i).get(1);
                adj.get(u).add(v);
                adj.get(v).add(u);
            }
            boolean[] seen = new boolean[n];
            // Graph in the tests is connected before removal; count reachable from 0.
            ArrayList<Integer> stack = new ArrayList<>();
            stack.add(0);
            seen[0] = true;
            while (!stack.isEmpty()) {
                int cur = stack.remove(stack.size() - 1);
                for (int nb : adj.get(cur)) {
                    if (!seen[nb]) {
                        seen[nb] = true;
                        stack.add(nb);
                    }
                }
            }
            int reached = 0;
            for (boolean b : seen) {
                if (b) {
                    reached++;
                }
            }
            if (reached != n) {
                int u = connections.get(skip).get(0);
                int v = connections.get(skip).get(1);
                result.add(List.of(Math.min(u, v), Math.max(u, v)));
            }
        }
        return result;
    }

    // ---- Checks ----

    private static Set<List<Integer>> normalize(List<List<Integer>> edges) {
        Set<List<Integer>> set = new HashSet<>();
        for (List<Integer> e : edges) {
            int u = Math.min(e.get(0), e.get(1));
            int v = Math.max(e.get(0), e.get(1));
            set.add(List.of(u, v));
        }
        return set;
    }

    private static List<List<Integer>> edges(int[][] raw) {
        List<List<Integer>> list = new ArrayList<>();
        for (int[] e : raw) {
            list.add(List.of(e[0], e[1]));
        }
        return list;
    }

    private static void check(String label, int n, int[][] raw, int[][] expectedRaw) {
        List<List<Integer>> input = edges(raw);
        List<List<Integer>> actual = new CriticalConnections().criticalConnections(n, input);
        Set<List<Integer>> actualSet = normalize(actual);
        Set<List<Integer>> expectedSet = normalize(edges(expectedRaw));
        if (!actualSet.equals(expectedSet)) {
            throw new AssertionError(label + ": criticalConnections = " + actualSet
                    + ", expected " + expectedSet);
        }
        // Every fixed case must also agree with the brute-force oracle.
        Set<List<Integer>> oracleSet = normalize(bruteForce(n, input));
        if (!actualSet.equals(oracleSet)) {
            throw new AssertionError(label + ": Tarjan " + actualSet
                    + " disagrees with oracle " + oracleSet);
        }
        System.out.println("PASS " + label + " -> " + actualSet);
    }

    public static void main(String[] args) {
        check("leetcode-example", 4,
                new int[][] { { 0, 1 }, { 1, 2 }, { 2, 0 }, { 1, 3 } },
                new int[][] { { 1, 3 } });
        check("single-edge", 2,
                new int[][] { { 0, 1 } },
                new int[][] { { 0, 1 } });
        check("triangle-no-bridges", 3,
                new int[][] { { 0, 1 }, { 1, 2 }, { 2, 0 } },
                new int[][] {});
        check("line-all-bridges", 5,
                new int[][] { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 4 } },
                new int[][] { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 4 } });
        check("two-triangles-one-bridge", 6,
                new int[][] { { 0, 1 }, { 1, 2 }, { 2, 0 }, { 2, 3 }, { 3, 4 }, { 4, 5 }, { 5, 3 } },
                new int[][] { { 2, 3 } });
        check("square-with-diagonal", 4,
                new int[][] { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 0 }, { 0, 2 } },
                new int[][] {});
        check("square-with-tail", 5,
                new int[][] { { 0, 1 }, { 1, 2 }, { 2, 3 }, { 3, 0 }, { 3, 4 } },
                new int[][] { { 3, 4 } });
        check("single-node", 1,
                new int[][] {},
                new int[][] {});

        // Randomised cross-check against the brute-force oracle.
        Random rng = new Random(1192L);
        for (int trial = 0; trial < 2000; trial++) {
            int n = 2 + rng.nextInt(8); // 2..9 nodes
            // Build a random spanning tree first so the graph is connected.
            List<List<Integer>> conns = new ArrayList<>();
            Set<List<Integer>> used = new HashSet<>();
            for (int v = 1; v < n; v++) {
                int u = rng.nextInt(v);
                List<Integer> e = List.of(Math.min(u, v), Math.max(u, v));
                conns.add(e);
                used.add(e);
            }
            // Add random extra edges (each closes a cycle, killing some bridges).
            for (int u = 0; u < n; u++) {
                for (int v = u + 1; v < n; v++) {
                    List<Integer> e = List.of(u, v);
                    if (!used.contains(e) && rng.nextDouble() < 0.3) {
                        conns.add(e);
                        used.add(e);
                    }
                }
            }
            Set<List<Integer>> actual = normalize(
                    new CriticalConnections().criticalConnections(n, conns));
            Set<List<Integer>> oracle = normalize(bruteForce(n, conns));
            if (!actual.equals(oracle)) {
                throw new AssertionError("random trial " + trial + ": n=" + n
                        + " conns=" + conns + " Tarjan=" + actual + " oracle=" + oracle);
            }
        }
        System.out.println("PASS randomised cross-check (2000 graphs vs brute-force oracle)");
        System.out.println("All checks passed.");
    }
}
