# Day 028 — Random Pick with Blacklist

**Problem.** Given `n` and a blacklist of distinct values in `[0, n)`, repeatedly return a uniformly random integer in `[0, n)` that is not blacklisted (LeetCode 710, Hard). The catch is space: `n` can be enormous while the blacklist is small, so materialising the list of allowed values is not acceptable.

**Approach.** Virtual remapping with a `HashMap`. Let `M = n - blacklist.size()` — the number of allowed values. Draw uniformly from the smaller range `[0, M)`. Most draws are already allowed; the only broken cases are draws that land on a blacklisted value below `M`. Each such value is pre-mapped to a distinct allowed value harvested from the tail `[M, n)`, skipping tail values that are themselves blacklisted. Tail values are never drawn directly, so they are reachable *only* through the map — which is exactly what keeps every allowed value at probability `1/M`.

**Complexity.** O(B) construction time and O(B) space, where B is the blacklist size; `pick()` is O(1). The naive allow-list costs O(n) space.

**Takeaways.**
- "Shrink the sample space, then patch the holes with a map" turns a rejection problem into an exact one-pass mapping — no resampling loop, no worst case.
- Uniformity is a counting argument: draws partition into identity slots plus remapped slots, one slot per allowed value. If two blacklisted values ever shared a replacement, or a tail value were also directly drawable, the distribution would skew.
- Seeding the `Random` in tests makes a stochastic contract checkable: with a fixed seed, "never blacklisted + every allowed value reached + counts within 50% of expected" is deterministic in practice and catches broken mappings outright.
