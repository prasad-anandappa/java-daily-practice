# Day 024 — Split Array Largest Sum

**Problem.** Given an array of non-negative integers and `k`, split it into exactly `k` non-empty contiguous subarrays so the largest subarray sum is minimised. Return that minimised largest sum. (LeetCode 410, Hard.)

**Approach.** Binary search on the answer over `[max(nums), sum(nums)]`. For a candidate cap `C`, a single greedy pass settles feasibility: extend the current subarray while it fits under `C`, start a new one only on overflow — for a fixed cap, longest-possible subarrays never increase the count needed. Feasibility is monotone in `C` (a working cap implies every larger cap works), so binary search converges on the smallest feasible cap. A partition DP over prefix sums is kept only as a test oracle and cross-checked exhaustively on small inputs plus 500 randomised cases.

**Complexity.** O(n log S) time, where S is the total sum, and O(1) extra space for the solution. The DP oracle is O(k · n²) time, O(n) space — correct but far too slow as the answer, which is exactly why the monotone-feasibility reformulation matters.

**Takeaways.**
- "Minimise the largest …" is a signature for binary search on the answer: turn the optimisation into a yes/no feasibility question, prove monotonicity, then search the boundary.
- The greedy feasibility check must itself be proven optimal — here, longest-first packing is safe because delaying a cut can only leave more room later.
- Bounds carry the argument: the lower bound is the largest single element (it must live somewhere), the upper bound is the total sum (one subarray always works).
- Keep sums in `long` during the search — an `int` total overflows silently on large inputs long before the algorithm itself breaks.
