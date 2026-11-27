# Day 029 — Alien Dictionary

**Problem.** Given a list of words sorted lexicographically by an unknown alien alphabet, derive an ordering of all distinct letters consistent with that sort, or return `""` if none exists (LeetCode 269, Hard).

**Approach.** Only adjacent word pairs teach anything, and only their first differing position does: it yields one precedence edge `earlier -> later`. A pair with no differing position is valid only when the shorter word comes first — a longer word listed before its own prefix is an immediate contradiction. All edges form a directed graph over the letters, and any topological order of that graph is a valid alphabet; a cycle means the dictionary contradicts itself. Kahn's algorithm with a min-heap emits the lexicographically smallest topological order, making the output deterministic. Duplicate edges are de-duplicated through a `HashSet` adjacency, so indegrees are never double-counted.

**Complexity.** O(C + V + E) time, where C is the total number of characters across all words and V / E are the distinct letters and derived edges; O(V + E) space.

**Takeaways.**
- A sorted list encodes far less than it appears to: non-adjacent pairs and later differing positions carry no new information, because the sort was already decided at the first difference.
- Two different failure modes must both surface as `""`: the prefix contradiction is detected while building edges, while a precedence cycle is detected only when Kahn's algorithm stalls with letters unemitted.
- When many answers are valid, pinning one down deterministically (here, lexicographically smallest via a min-heap) makes the result testable — the randomised checks additionally cross-verify against a brute-force permutation oracle, requiring `""` exactly when no valid permutation exists.
