# Day 003 — Trapping Rain Water

**Problem.** Given an array of non-negative integers representing an elevation map where each bar has width 1, compute how much water the map can trap after raining. (LeetCode 42, Hard.)

**Approach.** Two pointers converging from both ends, carrying `leftMax`, the tallest bar seen from the left, and `rightMax`, the tallest bar seen from the right. The water above any bar is `min(leftMax, rightMax) - height`, so a bar can be settled as soon as the smaller side's maximum is known. At each step, settle the side with the smaller current bar: the opposite side already guarantees a wall at least as tall, so that bar traps `leftMax - height` (or the mirror image on the right), and that pointer moves inward.

**Complexity.** O(n) time, O(1) space. Precomputing prefix and suffix maxima also runs in O(n) time but costs O(n) space; scanning both sides of every bar is O(n²) time. The two-pointer invariant — always advancing the currently limiting side — is what removes the extra storage.

**Takeaways.**
- "Settle the side that is already bounded" is the hard-variant two-pointer pattern: don't process in order, process in the order the invariant lets you prove.
- The water above a bar depends only on the *smaller* of the two surrounding maxima — the taller wall never adds more.
- Edge cases carry the proof: monotonic maps trap nothing, and a symmetric valley traps exactly up to the lower rim — both are asserted in `main`.
