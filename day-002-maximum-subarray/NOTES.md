# Day 002 — Maximum Subarray

**Problem.** Given an integer array, find the contiguous subarray with the largest sum and return that sum. The subarray must contain at least one element.

**Approach.** Kadane's algorithm in one pass. Keep `endingHere`, the largest sum of any subarray that ends at the current position: for each new value, either extend the previous run (`endingHere + value`) or start a fresh run at this value, whichever is larger. Keep `best`, the largest `endingHere` seen anywhere — that is the answer, because every subarray ends somewhere.

**Complexity.** O(n) time, O(1) space. Checking every start/end pair is O(n²) time; the insight that an optimal subarray ending at `i` either extends the optimal run ending at `i - 1` or starts at `i` removes the inner loop entirely.

**Takeaways.**
- "Best ending here" is the core dynamic-programming pattern: solve each position from the previous one, then take the overall best.
- Restarting the run when the previous sum hurts more than it helps is why Kadane's works — a negative prefix can never improve a future subarray.
- Seed both accumulators with the first element, not zero, so all-negative arrays correctly return the single largest (least negative) value.
