# Day 007 — Reverse Nodes in k-Group

**Problem.** Given the head of a singly linked list, reverse the nodes k at a time and return the modified head. A final group with fewer than k nodes keeps its original order. Only pointer rewiring is allowed — no swapping values and no copying the list into an array. (LeetCode 25, Hard.)

**Approach.** A dummy node before the head makes the first group behave like every other group. `groupPrev` always points at the node just before the current group. Walk k steps from `groupPrev` to find the group's kth node; if the walk hits `null`, the leftover group is incomplete and stays untouched. Otherwise reverse exactly those k nodes with the standard three-pointer walk, bounded by `groupNext` (the node after the group) so the reversed tail splices straight onto the rest of the list. Then `groupPrev.next` becomes the old kth node — the group's new head — and the old first node, now the group's tail, becomes `groupPrev` for the next round.

**Complexity.** O(n) time, O(1) extra space. Each node is visited a constant number of times: once by the k-step probe and once by the reversal walk. The iterative form avoids the O(n/k) call-stack space of the recursive version.

**Takeaways.**
- A dummy head node removes the "first group is special" case — the most common source of linked-list bugs.
- Reverse with a sentinel bound (`groupNext`), not `null`, when reversing a sublist that must reconnect to what follows it.
- Probe before you mutate: confirming a full group of k exists before rewiring anything is what leaves a short final group correctly untouched.
