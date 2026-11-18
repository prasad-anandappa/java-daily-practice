# Day 020 — Merge k Sorted Lists

**Problem.** Given an array of `k` linked lists, each sorted in non-decreasing order, merge all of them into one sorted linked list and return its head. Lists may be empty, values may repeat or be negative, and `k` may be zero. (LeetCode 23, Hard.)

**Approach.** Divide-and-conquer pairwise merging. Merging two sorted lists is the familiar linear dummy-head walk that rewires `next` pointers without copying values. The hard part is the schedule: folding lists one at a time into a growing accumulator rescans that accumulator for every new list, costing O(k · N) for `N` total nodes. Pairing lists up instead — merge 0 with 1, 2 with 3, then repeat on the halved set, implemented here as a recursive merge over the index range — means each node takes part in only O(log k) merges, so the total work is O(N log k). A min-heap variant (push every head, repeatedly poll the smallest and push its successor) reaches the same bound with O(k) extra space; it is kept in `main` as an independent oracle alongside a flatten-and-sort brute force, and all three are cross-checked over 1,000 randomised inputs with varied `k`, empty lists, duplicates, and negatives.

**Complexity.** O(N log k) time, where `N` is the total number of nodes across all lists and `k` is the number of lists. Extra space is O(log k) for the recursion stack in the divide-and-conquer version (the merge itself is O(1) besides the dummy node, reusing the input nodes); the heap oracle uses O(k) space.

**Takeaways.**
- For `k`-way merging, *how* merges are scheduled matters as much as the merge itself: sequential accumulation is O(k · N), balanced pairing is O(N log k) — the same lesson as merge sort's recursion tree.
- The two-list merge is stable here because ties take from the left list first (`<=`), a one-character choice that decides whether equal values keep their list order.
- Rewiring beats copying: the solution returns the input nodes spliced together, so there is no second list's worth of allocations — the heap oracle deliberately copies values to stay fully independent as a test reference.
- Empty inputs are three different cases, not one: a `null`/zero-length array, an array of empty lists, and empty lists mixed with non-empty ones all need explicit checks.
