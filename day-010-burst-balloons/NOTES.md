# Day 010 — Burst Balloons

**Problem.** n balloons are painted with `nums[i]`. Bursting balloon i earns `nums[left] * nums[i] * nums[right]` coins, where left and right are its current neighbours; after a burst the neighbours become adjacent. Return the maximum coins from bursting all balloons in some order. (LeetCode 312, Hard.)

**Approach.** Interval DP with memoised recursion. Pad the array with a 1 at each end — imaginary balloons that are never burst and serve as fixed boundaries. `solve(l, r)` is the maximum from bursting every balloon strictly between l and r while l and r still stand. The decisive move is to pick which balloon k in (l, r) bursts **last**: everything else inside the interval is gone by then, so k's neighbours are exactly l and r and it earns `a[l] * a[k] * a[r]` regardless of the order inside either side. That makes the two sub-intervals independent: `solve(l, r) = max over k of solve(l, k) + solve(k, r) + a[l] * a[k] * a[r]`, with empty intervals worth 0. Picking the *first* burst instead decouples nothing, because its neighbours are still present and every later neighbourhood keeps changing. The `main` cross-checks the DP against an exhaustive permutation-free brute force on small inputs (duplicates, zeros, unsorted mixes) in addition to the known answers.

**Complexity.** O(n³) time — O(n²) intervals, each trying O(n) choices for the last burst. O(n²) space for the memo table (plus O(n) recursion depth). Brute force over burst orders is O(n!), which is why it only appears in the tests.

**Takeaways.**
- When an operation's reward depends on what is still present, try defining the DP around the **last** operation instead of the first — fixing the last step often freezes the context the reward is computed in, and the subproblems fall apart cleanly.
- Padding with sentinel boundaries (here, 1s) removes edge cases from the recurrence: interior and edge balloons obey exactly the same rule.
- An exponential brute force is not wasted code at daily-practice scale — kept in `main` as an oracle, it verifies the DP on inputs whose answers nobody has memorised.
