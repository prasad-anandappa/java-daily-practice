# Day 005 — Largest Rectangle in Histogram

**Problem.** Given an array of bar heights in a histogram, where each bar has width 1, return the area of the largest rectangle that fits entirely under the bars. (LeetCode 84, Hard.)

**Approach.** Monotonic increasing stack of indices. The stack keeps bars whose right boundary is not yet known, in non-decreasing height order. When a lower bar arrives — or a sentinel height of 0 just past the end forces a flush — every taller bar is popped: the current index is its right boundary (the first lower bar to its right), and the new stack top is its left boundary (the first lower bar to its left). The popped bar's widest rectangle is therefore `height * (right - left - 1)`, computed exactly once, at the moment both boundaries become known. Equal heights are not popped by a strictly-less comparison, so a plateau is measured at its full combined width when it finally flushes.

**Complexity.** O(n) time, O(n) space. Each index is pushed and popped at most once; the brute-force check of every interval is O(n²). The hard part is the invariant: a bar can only be measured when a lower bar closes it, so the algorithm must not measure early — the sentinel guarantees even the last, tallest run is closed.

**Takeaways.**
- "First lower element on each side" is the monotonic-stack signature — previous/next smaller (or greater) boundaries turn an O(n²) interval search into one pass.
- A sentinel value at the end removes special-case cleanup code: the same pop logic flushes the whole stack.
- Boundaries are indices of *lower* bars, not the popped bar's neighbours — width is `right - left - 1` using the stack top *after* the pop.
