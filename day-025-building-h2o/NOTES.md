# Day 025 — Building H2O

**Problem.** Hydrogen and oxygen threads arrive in unpredictable order. Atoms may only be released in complete water molecules: every consecutive group of three released atoms must be exactly two H and one O, in any order (LeetCode 1117, run here as a hard reusable-synchroniser variant: unbounded molecules, adversarial arrival orders, deadlock detection).

**Approach.** Two admission semaphores cap who may enter the rendezvous — 2 hydrogen permits, 1 oxygen permit — so at most one molecule's worth of atoms is ever in flight. Admitted atoms run their release action, then wait on a `CyclicBarrier` of 3. The barrier action, executed once per trip before any waiter resumes, replenishes the permits (2H + 1O), admitting exactly the next molecule. Groups therefore cannot overlap or interleave, and the cyclic barrier makes the synchroniser reusable forever.

**Complexity.** O(1) synchronisation state (two semaphores + one barrier) regardless of molecule count; each atom does O(1) work beyond its own release action. Throughput is bounded by molecule assembly itself, not by the synchroniser.

**Takeaways.**
- Make the invariant structural: permit caps enforce "one group in flight" by construction, instead of relying on every woken thread to re-check a shared counter.
- A `CyclicBarrier` barrier-action is the clean release point for group protocols — it runs exactly once, by one thread, before any party proceeds, which is precisely where shared permits should be replenished.
- Concurrency tests must fail, not hang: every check joins with a deadline and reports a suspected deadlock with the partial output, and adversarial arrivals (all-O-first, all-H-first, shuffled with jitter) matter more than the friendly order.
