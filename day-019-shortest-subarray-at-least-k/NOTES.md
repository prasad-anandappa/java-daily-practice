# Day 019 — Shortest Subarray with Sum at Least K

**Problem.** Given an integer array `nums`, which may contain negative numbers, and an integer `k`, return the length of the shortest non-empty contiguous subarray whose sum is at least `k`, or `-1` if no such subarray exists. (LeetCode 862, Hard.)

**Approach.** Prefix sums plus a monotonic deque. With `P[0] = 0` and `P[i + 1] = nums[0] + ... + nums[i]` (formed in `long`), the sum of `nums[i..j - 1]` is `P[j] - P[i]`, so the task is the smallest `j - i` with `P[j] - P[i] >= k`. Negatives make `P` non-monotonic, which is exactly why the familiar shrinking two-pointer window fails here — a later dip in `P` can still pair with the current value. Scanning `j` left to right, the deque keeps candidate left indices with strictly increasing `P` values. Any front index already reaching `k` against the current `j` gives a feasible length, and polling it is safe because a later `j` could only make that subarray longer. Before `j` itself is queued, every back index with `P[back] >= P[j]` is discarded: `j` is later, so it yields shorter subarrays, and its prefix is no larger, so its sums are at least as big — the back index is dominated on both axes. `main` cross-checks against an independent O(n²) brute-force oracle over 1,000 randomised arrays with negatives throughout.

**Complexity.** O(n) time, O(n) space: each prefix index enters and leaves the deque at most once, and the prefix array is the only extra storage. The brute-force oracle is O(n²) time and appears only in the tests.

**Takeaways.**
- Negatives break the sliding-window reflex: once elements can shrink a window's sum, monotonicity is gone and the problem must be reframed — here as a closest-pair question over prefix sums.
- The deque maintains two ideas at once: increasing prefix values (so the front is the most promising left partner) and increasing indices (so the first feasible front is also the earliest). Dominated candidates — later *and* no smaller — are removed eagerly, which is what keeps the scan linear.
- A feasible front is consumed, not kept: pairing it with a later right end can only lengthen the subarray, so the shortest answer for that left index is found at its first feasible `j`.
- Prefix sums over `int` inputs belong in `long`: `n` large values can sum well past the `int` range, and the overflow checks in `main` pin that down.
