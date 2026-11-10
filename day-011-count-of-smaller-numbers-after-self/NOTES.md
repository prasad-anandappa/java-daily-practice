# Day 011 — Count of Smaller Numbers After Self

**Problem.** Given an integer array `nums`, return `counts` where `counts[i]` is the number of indices `j > i` with `nums[j] < nums[i]`. (LeetCode 315, Hard.)

**Approach.** Merge sort over an array of *indices*, sorted by their values so original positions are never lost. Merging a left half and a right half (both sorted by value), every right element emitted before a given left element is strictly smaller than it — the merge only takes the right element first when it is smaller — and it originally stood to that element's right. So when a left element is emitted, adding the count of right elements already emitted accumulates exactly its answer across the O(log n) merges it participates in. Ties go to the left, so equal values are never counted as smaller. `main` cross-checks this against two independent oracles: a coordinate-compression + Fenwick-tree scan from the right (sort the distinct values, binary-search each rank, query the prefix sum below it) and an O(n²) brute force on small inputs with duplicates, negatives, and sorted/reverse-sorted extremes.

**Complexity.** O(n log n) time — merge sort, with O(1) extra counting work per element per level. O(n) space for the index and scratch arrays (plus O(log n) recursion). The Fenwick oracle is also O(n log n); brute force is O(n²) and only appears in the tests.

**Takeaways.**
- "Count smaller/larger elements on one side" is a divide-and-conquer signal: if a merge already compares elements across the two halves in sorted order, the count you need is usually sitting in the merge — record it there instead of scanning again.
- Sorting indices rather than values preserves identity: the array being ordered and the positions being answered about stay separate, a trick that recurs in inversion counting and offline queries.
- Strictness lives in the tie rule. Emitting the left element first on equality is the whole difference between `<` and `<=` here — off-by-tie bugs pass sorted and distinct tests and only fail on duplicates, which is why the checks lean on them.
