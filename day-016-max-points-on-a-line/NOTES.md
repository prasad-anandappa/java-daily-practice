# Day 016 — Max Points on a Line

**Problem.** Given `points[i] = [xi, yi]` on the XY plane, return the maximum number of points that lie on the same straight line. (LeetCode 149, Hard.)

**Approach.** Anchor each point `i` and group every other point by its direction from `i` in a `HashMap`: points on one line through `i` are exactly those sharing a direction. The direction must be canonical or the grouping breaks — divide `(dx, dy)` by `gcd(|dx|, |dy|)` so `(2, 4)`, `(3, 6)` and `(1, 2)` collapse to one key, and fix the sign (`dx > 0`, or `dx == 0` and `dy > 0`) so the two opposite rays of the same line also collapse. Points identical to the anchor are counted apart: they lie on every line through `i`, so they are added to the winning direction's count plus the anchor itself. No floating point is used — a `double` slope merges distinct near-parallel lines and needs a special case for verticals, while the reduced integer pair is exact. `main` cross-checks against an independent O(n³) oracle — every distinct pair defines a line, count by zero cross product — over 500 randomised point sets including duplicates.

**Complexity.** O(n²) time, O(n) space: for each of the `n` anchors, one pass over the other points with O(1) hash operations. The pair-and-recount oracle is O(n³) time.

**Takeaways.**
- "Same direction from a common anchor" turns a geometry problem into a hashing problem — the HashMap pattern from Day 001, with the key design doing all the real work.
- A hash key for a mathematical object must be a canonical form: reduce by the gcd *and* normalise the sign, or equal objects silently land in different buckets.
- Exact integer arithmetic beats floating point whenever the input allows it: precision failures on near-parallel slopes are the classic wrong answer for this problem, not a corner case.
