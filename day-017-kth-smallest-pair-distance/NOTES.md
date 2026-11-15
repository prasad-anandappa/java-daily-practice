# Day 017 — K-th Smallest Pair Distance

**Problem.** Given an integer array `nums` and `k`, consider every pair `(i, j)` with `i < j` and its distance `|nums[i] - nums[j]|`. Return the k-th smallest distance among all pairs (1-indexed). (LeetCode 719, Hard.)

**Approach.** Sort a copy of the array — distances depend only on values, not on original positions. The key move is binary searching the *answer* instead of enumerating pairs: for a candidate distance `D`, count the pairs with distance at most `D` in one two-pointer pass. For each `right`, advance `left` while `sorted[right] - sorted[left] > D`; every index in `[left, right)` then pairs with `right` within `D`, contributing `right - left` pairs. That count is monotone non-decreasing in `D`, so binary search finds the smallest `D` whose count reaches `k`. That `D` is necessarily a real pair distance (the count only jumps at actual distances), so it is exactly the k-th smallest. `main` cross-checks against an independent oracle — enumerate all pair distances, sort them, index `k - 1` — over 500 randomised arrays, verifying *every* `k` for each array, including duplicates and negative values.

**Complexity.** O(n log n + n log W) time, O(n) space, where `W = max - min` is the value range: sorting dominates setup, and each of the ~log W counting passes is a linear two-pointer scan. The oracle is O(n² log n) time and O(n²) space — fine for tests, impossible as the solution for large `n`.

**Takeaways.**
- "K-th smallest X" where X is expensive to enumerate often yields to binary search on the answer plus a monotone counting predicate — the count function, not the list, is the real algorithm.
- Sorting converts an absolute-difference condition into a one-sided window condition, which is what makes the O(n) two-pointer count possible: both pointers only move forward because the array is ordered.
- The counting predicate must be monotone for the search to be valid, and the answer must be attained: here the count jumps only at genuine pair distances, which is why the smallest `D` with `count(D) >= k` is the answer rather than an approximation of it.
