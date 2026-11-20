# Day 022 — Critical Connections in a Network

**Problem.** There are `n` servers labelled `0..n-1` connected by undirected edges. A connection is critical (a bridge) if removing it increases the number of connected components. Return all critical connections. (LeetCode 1192, Hard.)

**Approach.** Tarjan's bridge-finding DFS. Each node receives a discovery time `disc[v]` and a low-link value `low[v]` — the earliest discovery time reachable from the subtree rooted at `v` using tree edges plus at most one back edge. For a tree edge `v -> to`, the edge is a bridge exactly when `low[to] > disc[v]`: nothing in `to`'s subtree can reach `v` or any ancestor of `v` without this edge, so removing it disconnects the subtree. A back edge pulls a low value up and rescues every edge on the cycle it closes — which is why edges on any cycle are never bridges. One DFS visits every node and edge once and reports all bridges. `main` does not trust the low-link argument alone: a brute-force oracle removes each edge in turn and tests connectivity, and 2,000 random connected graphs (2–9 nodes, spanning tree plus random cycle-closing edges) must match it exactly, alongside the fixed cases (LeetCode example, single edge, triangle, line graph, two triangles joined by one bridge).

**Complexity.** O(V + E) time — one DFS over the graph. O(V + E) space for the adjacency list plus the `disc`/`low` arrays and recursion stack. The brute-force oracle is O(E · (V + E)) and exists only as a test reference for small graphs.

**Takeaways.**
- "Is there another way back up?" is the whole bridge question, and `low[v]` is that question compressed into one integer per node.
- The strict inequality matters: `low[to] > disc[v]` is a bridge, `low[to] == disc[v]` means a back edge reaches `v` itself, so the edge survives.
- Skipping only the immediate parent edge is correct for simple graphs, but would break with parallel edges — with multi-edges, track edge ids instead of the parent vertex.
- Pairing a linear-time DFS invariant with a remove-and-test oracle on random small graphs is the reliable pattern for graph algorithms: the proof gives the idea, the oracle catches the off-by-one in `low` updates.
