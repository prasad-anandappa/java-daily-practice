# Day 001 — Two Sum

**Problem.** Given an integer array and a target, return the indices of the two numbers that add up to the target. One solution exists; an element cannot repeat.

**Approach.** One pass, remembering each value's index in a `HashMap`. For the current value, the only partner that can complete it is `target - value` — either that partner was already seen (return both indices) or it hasn't appeared yet (store the current value and move on).

**Complexity.** O(n) time, O(n) space. The brute-force double loop is O(n²) time; trading a little memory for a full order of growth is the recurring bargain of hash-based lookups.

**Takeaways.**
- "Have I seen the complement?" is the pattern behind a whole family of pair-finding problems.
- Check the map *before* inserting, so an element never pairs with itself.
- A `main` with hard assertions is a dependency-free test harness — enough to verify a daily exercise in seconds.
