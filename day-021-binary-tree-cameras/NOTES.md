# Day 021 — Binary Tree Cameras

**Problem.** A camera placed on a node monitors that node, its parent, and its immediate children. Return the minimum number of cameras that monitors every node of a binary tree. (LeetCode 968, Hard.)

**Approach.** Greedy post-order DFS with three states reported from each node to its parent: `NEEDS_COVER` (nothing below covers this node), `HAS_CAMERA`, and `COVERED` (a child covers it). The key decision is to push cameras as deep as possible: a node installs a camera exactly when a child reports `NEEDS_COVER`, because a camera there covers the child, the node itself, and the node's parent — placing it at the parent instead could never cover more. A leaf reports `NEEDS_COVER` (deferring upward is never worse, since its parent is an equally good site that also reaches further), a node with a camera child is `COVERED`, and a node whose children are only `COVERED` still needs cover itself. If the root still needs cover after the traversal, it gets one final camera. `main` does not trust the greedy argument alone: an exhaustive oracle enumerates every camera subset on small trees and takes the true minimum covering set, and 2,000 random trees (1–10 nodes) must match it exactly.

**Complexity.** O(n) time — one post-order visit per node. O(h) extra space for the recursion stack, where `h` is the tree height (O(n) worst case on a skewed tree, O(log n) when balanced). The brute-force oracle is O(2^n · n) and exists only as a test reference for small `n`.

**Takeaways.**
- "Place the resource as deep as it can still work" is the greedy pattern here: deferring a decision to the parent is safe only because the parent's camera dominates the child's in coverage — that dominance argument is what makes the greedy optimal, and the brute-force cross-check confirms it empirically.
- Three states are the minimum vocabulary for this problem: collapsing `COVERED` and `NEEDS_COVER` into one "no camera" state loses exactly the information the parent needs to decide.
- Sentinel handling does the quiet work: a `null` child reports `COVERED` so it never forces a camera, while a real leaf reports `NEEDS_COVER` — the null/leaf distinction is the whole base case.
- A greedy solution earns trust differently from a DP: pair the exchange/dominance argument in the notes with an exhaustive oracle on small random inputs in `main`, so optimality is tested, not just asserted.
