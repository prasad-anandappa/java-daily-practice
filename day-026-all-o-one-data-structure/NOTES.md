# Day 026 — All O(1) Data Structure

**Problem.** Maintain string keys with occurrence counts under `inc(key)` / `dec(key)` (delete at zero, no-op if absent), and answer `getMaxKey()` / `getMinKey()` — every operation in O(1) (LeetCode 432, Hard).

**Approach.** Bucket keys by their current count. Each live count is a node in a doubly linked list sorted by count, holding a `HashSet` of the keys at that count; a `HashMap` from key to its bucket gives O(1) location. Counts only ever change by one, so `inc`/`dec` moves a key to the *neighbouring* bucket — reuse it if its count matches, otherwise splice a fresh bucket in beside the current one. Empty buckets are unlinked immediately. Sentinel head (count 0) and tail (count ∞) buckets hold no keys and erase the edge cases: max is any key in `tail.prev`, min is any key in `head.next`.

**Complexity.** O(1) time for all four operations — a constant number of hash-map, hash-set and pointer edits, no scans and no heaps (a heap would make updates O(log n) and force lazy deletion). O(n) space in distinct keys plus O(distinct counts) buckets.

**Takeaways.**
- When values change by ±1, "sorted structure" stops meaning search: the destination is always adjacent, which is what makes true O(1) possible here.
- Sentinels are a design tool, not a trick — head/tail dummies turn four boundary cases into zero.
- Invariants do the work: "every live bucket is non-empty and buckets are strictly increasing in count" is exactly what `getMaxKey`/`getMinKey` rely on; each mutation restores it locally before returning.
- Verify hard data structures against a dumb oracle: 20,000 seeded random operations cross-checked against a plain count map catch bucket-splice bugs that hand-picked cases miss.
