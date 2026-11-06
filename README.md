# Java Daily Practice

One Java exercise every day — problem, clean solution, and a self-checking `main` (no dependencies, plain `javac`/`java`).

Part of a deliberate path to deep Java expertise: data structures, algorithms, concurrency, and JVM internals, practised in small daily reps.

## Structure

Each day gets its own folder:

```
day-001-two-sum/
  TwoSum.java   — the solution, with checks in main()
  NOTES.md      — problem, approach, complexity, takeaways
```

## Run any day's exercise

```bash
cd day-001-two-sum
javac TwoSum.java
java TwoSum
```

## Progress

| Day | Topic | Focus |
|-----|-------|-------|
| 001 | Two Sum | HashMap, single-pass lookup, O(n) |
| 002 | Maximum Subarray | Arrays, Kadane's algorithm, O(n) |
| 003 | Trapping Rain Water | Two pointers, left/right maxima, O(n) time O(1) space |
| 004 | Minimum Window Substring | Sliding window, frequency counts, O(n) time |
| 005 | Largest Rectangle in Histogram | Monotonic stack, previous/next smaller boundaries, O(n) time |
| 006 | Median of Two Sorted Arrays | Binary search on partition, O(log(min(m, n))) |
| 007 | Reverse Nodes in k-Group | Linked list, in-place group reversal, O(n) time O(1) space |
