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
| 008 | Binary Tree Maximum Path Sum | Binary tree, post-order DFS, downward gain vs bend-through best, O(n) time |
| 009 | Swim in Rising Water | Graph traversal, minimax Dijkstra, bottleneck path, O(n² log n) time |
| 010 | Burst Balloons | Recursion & memoised DP, interval DP, last-burst decomposition, O(n^3) time |
| 011 | Count of Smaller Numbers After Self | Sorting & binary search, merge-sort counting + Fenwick oracle, O(n log n) time |
| 012 | Word Ladder II | Graphs & strings, BFS parent DAG + DFS enumeration, O(N · L · 26) time |
| 013 | Multithreaded Web Crawler | Java concurrency, ForkJoin work-stealing, atomic visited claim, exactly-once parallel fetch, O(V + E) time |
| 014 | LFU Cache | Collections internals, HashMap + per-frequency doubly linked lists, minFreq tracking, O(1) get/put |
| 015 | Shortest Palindrome | Arrays & strings, KMP prefix function, longest palindromic prefix, O(n) time |
| 016 | Max Points on a Line | HashMap patterns, reduced slope canonicalisation (gcd + sign), exact integer geometry, O(n^2) time |
| 017 | K-th Smallest Pair Distance | Two pointers, binary search on answer + monotone pair counting, O(n log n + n log W) time |
| 018 | Sliding Window Median | Sliding window, two ordered multisets (TreeMap split at median), O(n log k) time |
| 019 | Shortest Subarray with Sum at Least K | Stack/queue, monotonic deque over prefix sums, O(n) time |
| 020 | Merge k Sorted Lists | Linked list, divide-and-conquer pairwise merging, O(N log k) time |
| 021 | Binary Tree Cameras | Binary tree, greedy post-order DFS (needs-cover / has-camera / covered states), brute-force oracle cross-check, O(n) time |
| 022 | Critical Connections in a Network | Graph traversal, Tarjan bridges, DFS low-link (disc/low), brute-force oracle cross-check, O(V + E) time |
| 023 | Regular Expression Matching | Recursion & memoised DP, suffix-state (i, j) recurrence, star zero/one-consume branch, bottom-up oracle cross-check, O(n · m) time |
| 024 | Split Array Largest Sum | Sorting & binary search, binary search on answer + greedy feasibility, DP oracle cross-check, O(n log S) time |
| 025 | Building H2O | Java concurrency, semaphore admission caps (2H + 1O) + CyclicBarrier rendezvous, reusable molecule synchroniser, deadlock-detecting checks, O(1) sync state |
| 026 | All O(1) Data Structure | Collections internals, count-bucketed doubly linked list + HashMap key->bucket, sentinel head/tail, O(1) inc/dec/getMax/getMin |
| 027 | Text Justification | Arrays & strings, greedy line packing + exact space distribution (extras left), O(output) time |
