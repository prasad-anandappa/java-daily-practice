# Day 015 — Shortest Palindrome

**Problem.** Given a string `s`, return the shortest palindrome obtainable by adding characters only in front of `s`. Characters may not be appended, removed, or reordered. (LeetCode 214, Hard.)

**Approach.** Any answer must keep a prefix of `s` as its palindromic core and mirror the rest: if the longest palindromic prefix has length `k`, the suffix `s[k:]` has to be mirrored in front, and mirroring exactly it suffices — so the whole problem reduces to finding that longest palindromic prefix. Testing every prefix naively is O(n²). Instead, run the Knuth–Morris–Pratt prefix function on `s + '#' + reverse(s)`: its last value is the longest string that is both a prefix of `s` and a suffix of `reverse(s)`, and a prefix of `s` equals a suffix of its reverse exactly when it is a palindrome. The `#` separator (a character that can never match) prevents matches from wrapping across the join. Prepend `reverse(s[k:])` and the result is provably the shortest valid palindrome. `main` cross-checks against an independent brute-force oracle — try every split point, test palindromicity directly — over 500 randomised strings.

**Complexity.** O(n) time and O(n) space: one reversal, one linear KMP pass, one prepend. The naive prefix-testing approach is O(n²) time; a rolling-hash variant also reaches O(n) but trades KMP's exactness for collision risk.

**Takeaways.**
- "Prefix of the string = suffix of its reverse" is the standard KMP reduction for palindromic-prefix questions — the same prefix function powers substring search, periodicity, and prefix-palindrome problems.
- Reducing a construction problem to one extremal quantity (here, the longest palindromic prefix) gives both the algorithm and the optimality proof: anything shorter would need a longer palindromic prefix, which by definition does not exist.
- A separator sentinel is load-bearing, not decorative: without `#`, a match could span the boundary between `s` and `reverse(s)` and report a "palindromic prefix" longer than any real one.
