# Day 006 — Median of Two Sorted Arrays

**Problem.** Given two sorted arrays, return the median of the combined sorted order, in O(log(min(m, n))) time — merging is O(m + n) and does not meet the bar. (LeetCode 4, Hard.)

**Approach.** Binary search the *partition* of the shorter array. Choose i elements from nums1 and half − i from nums2 for the left side, where half = (m + n + 1) / 2. The partition is correct exactly when everything on the left is ≤ everything on the right: maxLeft1 ≤ minRight2 and maxLeft2 ≤ minRight1. Empty sides are handled with −∞ / +∞ sentinels (Integer.MIN_VALUE / MAX_VALUE). If maxLeft1 > minRight2 the cut in nums1 is too far right; if maxLeft2 > minRight1 it is too far left. At the correct cut, the median is the left side's maximum (odd total), or the average of that and the right side's minimum (even total). Searching the shorter array guarantees the nums2 index stays in range and keeps the search logarithmic.

**Complexity.** O(log(min(m, n))) time, O(1) space. The trick is re-framing: the median is a *partition property*, not a merge result, so a binary search over cut positions replaces the linear merge.

**Takeaways.**
- "Sorted + logarithmic" almost always means binary search — but the search space here is a cut position, not an element.
- Sentinels at the edges turn four boundary cases into one uniform comparison.
- The (m + n + 1) / 2 rounding puts the extra element on the left for odd totals, so the odd case never needs the right side.
