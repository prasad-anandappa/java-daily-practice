# Day 009 — Swim in Rising Water

**Problem.** An n x n grid gives each cell's elevation. At time t you may occupy and swim between 4-directionally adjacent cells whose elevation is at most t. Starting at (0, 0), return the minimum t at which the bottom-right cell becomes reachable. (LeetCode 778, Hard.)

**Approach.** This is a minimax (bottleneck) path problem, solved with a Dijkstra variant. The cost of a path is the maximum elevation anywhere on it, and the answer is the cheapest such cost over all paths. Dijkstra still applies because extending a path can never lower its cost — the extended cost is `max(cost so far, next elevation)` — so a priority queue ordered by bottleneck settles cells in non-decreasing cost order, and the first time the target is popped its cost is final. `best[r][c]` holds the cheapest bottleneck known for each cell; a neighbour is relaxed only when the path through the current cell gives it a strictly smaller bottleneck, and stale queue entries (superseded by a cheaper path) are skipped on pop. An equally valid alternative is binary search on t plus a BFS/DFS reachability check at each threshold; the Dijkstra pass finds the threshold directly in one sweep instead of O(log n²) reachability passes.

**Complexity.** O(n² log n²) = O(n² log n) time — each of the n² cells is settled once and each edge relaxation costs a log-factor queue operation. O(n²) space for the bottleneck table plus the queue.

**Takeaways.**
- Not every "shortest path" minimises a sum: when the cost of a path is its worst edge (or worst cell), the same Dijkstra machinery works unchanged, because the only property it needs is that extending a path never makes it cheaper.
- Reachability at a fixed threshold being monotone is the tell for this family — it licenses both binary-search-on-answer and the one-pass minimax Dijkstra; picking the one-pass version avoids re-traversing the grid for every guess.
- The stale-entry skip (`cost != best[r][c]`) is what keeps a lazy priority queue correct: never delete outdated entries, just ignore them when they surface.
