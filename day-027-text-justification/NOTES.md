# Day 027 — Text Justification

**Problem.** (LeetCode 68, Hard.) Given words in order and a width `maxWidth`, format the text so every line has exactly `maxWidth` characters and is fully justified: pack as many words per line as fit, spread the spaces between words as evenly as possible with any extra spaces assigned to the leftmost gaps, pad single-word lines on the right, and left-justify the last line (single spaces, trailing padding).

**Approach.** Two phases per line. (1) Greedy packing: extend the line while `letters + word + one space per gap so far` still fits — the longest feasible prefix is forced, since words cannot be reordered or split. (2) Space arithmetic: for a non-last line with `g` gaps and `S = maxWidth - letters` total spaces, each gap gets `S / g` spaces and the first `S % g` gaps get one extra, which is exactly "as even as possible, extras on the left". The last line and the single-word line (`g == 0`, where the division would blow up) share one degenerate path: single spaces between words, pad on the right.

**Complexity.** O(C) time where C is the total output size (lines × maxWidth) — every output character is written once. O(maxWidth) extra space beyond the returned lines.

**Takeaways.**
- The whole distribution rule collapses to one quotient and one remainder — resist the urge to simulate space-by-space round-robin placement.
- The `g == 0` case is the classic trap: a lone long word on a non-last line must be right-padded, not divided by zero.
- Greedy is provably right here because the constraints (fixed order, no hyphenation, maximise words per line) leave no choice to optimise over.
- Beyond the three canonical examples, the checks fuzz 3,000 random inputs for the invariants that define the problem: exact width on every line, words recovered in order, no trailing space on justified multi-word lines, gaps differing by at most one, and wider gaps never to the right of narrower ones.
