# Day 004 — Minimum Window Substring

**Problem.** Given strings `s` and `t`, return the smallest substring of `s` containing every character of `t`, including duplicates. If none exists, return `""`. (LeetCode 76, Hard.)

**Approach.** Sliding window with two frequency maps. `need` counts each distinct character required by `t`; `window` counts those characters inside the current window, and `formed` tracks how many distinct characters have reached their required count. Expand the right edge until `formed` equals the number of distinct characters in `t` — the window is now valid — then shrink the left edge while it stays valid, recording the shortest window seen. A character only breaks coverage when its window count drops *below* its required count, which is what makes duplicates in `t` work correctly.

**Complexity.** O(|s| + |t|) time, O(|s| + |t|) space for the maps (bounded by the alphabet in practice). Each index enters and leaves the window at most once; checking every substring would be O(|s|² · |t|). The hard part is not the scan but the invariant: validity is a property of counts, not of positions, so the window can shrink aggressively without ever re-scanning.

**Takeaways.**
- "Expand until valid, shrink while valid, record the best" is the hard sliding-window pattern — the optimum is always seen at a moment when the left edge can move no further.
- Track *distinct characters satisfied* (`formed`/`required`) instead of rescanning counts: coverage becomes an O(1) check per move.
- Duplicates are the trap: equality with the required count forms a character, dropping below it un-forms it — both transitions must be handled exactly once.
