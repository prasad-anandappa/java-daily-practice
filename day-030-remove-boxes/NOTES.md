# Day 030 — Remove Boxes

**Problem.** Given boxes coloured by integers, repeatedly remove a group of `k` consecutive boxes of the same colour for `k * k` points; the remaining boxes close ranks and may merge into larger groups. Return the maximum total score (LeetCode 546, Hard).

**Approach.** Memoised interval DP over the state `(l, r, k)`: the best score for `boxes[l..r]` when `k` extra boxes of the same colour as `boxes[l]` are attached just to its left and will be removed together with it. Two moves suffice. Either the whole attached group goes now, scoring `(k + 1)^2` plus the optimum of the untouched remainder `(l + 1, r, 0)`; or some later box `m` with `boxes[m] == boxes[l]` is kept — the segment between them is cleared first with `(l + 1, m - 1, 0)`, then `m`'s group joins the attached group as `(m, r, k + 1)`. The `k` dimension is what makes the state expressive enough: without it, the DP cannot know that clearing the middle first pays off quadratically later.

**Complexity.** O(n^4) time — O(n^3) states, each scanning O(n) merge partners — and O(n^3) space for the memo table.

**Takeaways.**
- The hard part of this problem is the state, not the transitions: augmenting the interval with the count of attached same-colour boxes turns an apparently global merging process into a local recurrence.
- Quadratic scoring is exactly why greedy fails — removing a large group early can be worse than spending moves to grow it first, as `[1, 2, 1]` shows (remove the `2`, then score `2^2`, total 5, not 3).
- An exhaustive oracle over removal orders is only feasible for tiny inputs, but 300 randomised trials at `n <= 9` pin the recurrence down far better than a handful of hand-picked cases.
