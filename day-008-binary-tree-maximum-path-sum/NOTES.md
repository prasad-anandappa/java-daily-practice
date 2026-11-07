# Day 008 — Binary Tree Maximum Path Sum

**Problem.** A path in a binary tree is any sequence of nodes connected by edges, each node used at most once; it need not pass through the root or end at a leaf. Return the maximum sum of node values over all paths. The tree has at least one node and values may all be negative. (LeetCode 124, Hard.)

**Approach.** One post-order DFS keeps two quantities separate at every node — confusing them is the classic way to get this problem wrong. The value *returned* upward is the best downward gain: the node's value plus the better child gain, with a negative child gain clamped to 0 because dropping that branch entirely is always at least as good. The value *recorded* in the global best is the path that bends through the node: value + clamped left gain + clamped right gain. A path bends at most once, so the bend candidate is never returned upward — the parent only ever receives the straight downward gain it can extend. The per-call best lives in a one-element array created inside `maxPathSum`, so repeated calls cannot leak state into each other.

**Complexity.** O(n) time, O(h) extra space for the recursion stack, where h is the tree height — O(n) worst case for a skewed tree, O(log n) for a balanced one. Each node does O(1) work on the way back up.

**Takeaways.**
- "Best path through here" and "best gain to offer my parent" are different answers to different questions; many hard tree problems split exactly this way.
- Clamping a negative gain to 0 encodes "take nothing from this side" — it is what makes the all-negative tree fall out naturally (the answer is the single least-negative node) with no special case.
- Keeping the global best in per-call state, not a static field, makes the function safe to call twice — the tests call it repeatedly on purpose to prove no stale maximum survives.
