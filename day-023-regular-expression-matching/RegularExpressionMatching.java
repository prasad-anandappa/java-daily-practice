import java.util.Random;

/**
 * Day 023 — Regular Expression Matching (LeetCode 10, Hard).
 *
 * Given a string s and a pattern p, return true if p matches the ENTIRE
 * string s. The pattern supports two special elements:
 *   '.' — matches any single character.
 *   '*' — matches zero or more copies of the PRECEDING element.
 * The pattern is guaranteed valid: '*' never appears first and never
 * directly follows another '*'.
 *
 * Approach: recursion with memoisation over suffix positions (i, j),
 * meaning "does s[i..] match p[j..]?". Let firstMatch be whether s[i]
 * exists and agrees with p[j] (equal, or p[j] == '.'). The only branch
 * in the whole problem is a '*' at p[j + 1]:
 *
 *   - Skip the starred element entirely (zero copies):
 *       solve(i, j + 2)
 *   - Consume one copy of it (only if firstMatch), staying on the
 *     same pattern position so more copies can follow:
 *       solve(i + 1, j)
 *
 *   solve(i, j) = solve(i, j + 2) || (firstMatch && solve(i + 1, j))
 *
 * Without a star the match is forced: firstMatch && solve(i + 1, j + 1).
 * Base case: the pattern is exhausted exactly when the string is, too.
 *
 * The naive recursion revisits the same (i, j) state exponentially many
 * times — the star branch is a classic overlapping-subproblems trap.
 * There are only (n + 1) * (m + 1) distinct states, so memoising them
 * collapses the search to O(n * m) time and O(n * m) space. A bottom-up
 * DP over the same states is kept below purely as a test oracle, and
 * the self-checks cross-verify the two implementations exhaustively on
 * small inputs plus randomised cases.
 */
public class RegularExpressionMatching {

    public static boolean isMatch(String s, String p) {
        if (s == null || p == null) {
            throw new IllegalArgumentException("Inputs must not be null");
        }
        Boolean[][] memo = new Boolean[s.length() + 1][p.length() + 1];
        return solve(s, p, 0, 0, memo);
    }

    /** Does s[i..] match p[j..]? Memoised over (i, j). */
    private static boolean solve(String s, String p, int i, int j, Boolean[][] memo) {
        if (memo[i][j] != null) {
            return memo[i][j];
        }
        boolean result;
        if (j == p.length()) {
            result = (i == s.length());
        } else {
            boolean firstMatch = i < s.length()
                    && (p.charAt(j) == '.' || p.charAt(j) == s.charAt(i));
            if (j + 1 < p.length() && p.charAt(j + 1) == '*') {
                // Zero copies of p[j], or one copy now and stay for more.
                result = solve(s, p, i, j + 2, memo)
                        || (firstMatch && solve(s, p, i + 1, j, memo));
            } else {
                result = firstMatch && solve(s, p, i + 1, j + 1, memo);
            }
        }
        memo[i][j] = result;
        return result;
    }

    // ---- Bottom-up DP over the same states, used only as a test oracle ----

    private static boolean isMatchBottomUp(String s, String p) {
        int n = s.length();
        int m = p.length();
        // dp[i][j] == does s[i..] match p[j..]?
        boolean[][] dp = new boolean[n + 1][m + 1];
        dp[n][m] = true;
        for (int i = n; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                boolean firstMatch = i < n
                        && (p.charAt(j) == '.' || p.charAt(j) == s.charAt(i));
                if (j + 1 < m && p.charAt(j + 1) == '*') {
                    dp[i][j] = dp[i][j + 2] || (firstMatch && dp[i + 1][j]);
                } else {
                    dp[i][j] = firstMatch && dp[i + 1][j + 1];
                }
            }
        }
        return dp[0][0];
    }

    // ---- Self-checks ----

    private static void check(String s, String p, boolean expected) {
        boolean actual = isMatch(s, p);
        if (actual != expected) {
            throw new AssertionError("isMatch(\"" + s + "\", \"" + p + "\") = " + actual
                    + ", expected " + expected);
        }
        boolean oracle = isMatchBottomUp(s, p);
        if (oracle != actual) {
            throw new AssertionError("isMatch(\"" + s + "\", \"" + p + "\") = " + actual
                    + ", bottom-up oracle says " + oracle);
        }
        System.out.println("PASS isMatch(\"" + s + "\", \"" + p + "\") = " + actual);
    }

    private static void checkAgainstOracle(String s, String p) {
        boolean actual = isMatch(s, p);
        boolean expected = isMatchBottomUp(s, p);
        if (actual != expected) {
            throw new AssertionError("isMatch(\"" + s + "\", \"" + p + "\") = " + actual
                    + ", bottom-up oracle says " + expected);
        }
    }

    /** Exhaustively cross-check both implementations on all tiny inputs. */
    private static void exhaustiveCrossCheck() {
        char[] sAlphabet = { 'a', 'b' };
        char[] pAtoms = { 'a', 'b', '.' };
        int cases = 0;
        // All strings over {a,b} of length 0..4.
        for (int sLen = 0; sLen <= 4; sLen++) {
            int sCount = 1;
            for (int k = 0; k < sLen; k++) {
                sCount *= sAlphabet.length;
            }
            for (int sMask = 0; sMask < sCount; sMask++) {
                StringBuilder sb = new StringBuilder();
                int v = sMask;
                for (int k = 0; k < sLen; k++) {
                    sb.append(sAlphabet[v % sAlphabet.length]);
                    v /= sAlphabet.length;
                }
                String s = sb.toString();
                // All patterns of 1..3 atoms, each atom optionally starred.
                for (int pAtomsCount = 1; pAtomsCount <= 3; pAtomsCount++) {
                    cases += crossCheckPatterns(s, pAtoms, pAtomsCount, new StringBuilder());
                }
                // Empty pattern only matches the empty string.
                checkAgainstOracle(s, "");
                cases++;
            }
        }
        System.out.println("PASS exhaustive cross-check (" + cases + " cases)");
    }

    private static int crossCheckPatterns(String s, char[] atoms, int remaining,
            StringBuilder current) {
        if (remaining == 0) {
            checkAgainstOracle(s, current.toString());
            return 1;
        }
        int cases = 0;
        for (char atom : atoms) {
            current.append(atom);
            cases += crossCheckPatterns(s, atoms, remaining - 1, current);
            current.append('*');
            cases += crossCheckPatterns(s, atoms, remaining - 1, current);
            current.setLength(current.length() - 2);
        }
        return cases;
    }

    /** Randomised cross-check on longer inputs, fixed seed for reproducibility. */
    private static void randomisedCrossCheck() {
        Random rng = new Random(23L);
        char[] sAlphabet = { 'a', 'b', 'c' };
        char[] pAtoms = { 'a', 'b', 'c', '.' };
        for (int trial = 0; trial < 2000; trial++) {
            int sLen = rng.nextInt(9);
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < sLen; k++) {
                sb.append(sAlphabet[rng.nextInt(sAlphabet.length)]);
            }
            int atomCount = 1 + rng.nextInt(5);
            StringBuilder pb = new StringBuilder();
            for (int k = 0; k < atomCount; k++) {
                pb.append(pAtoms[rng.nextInt(pAtoms.length)]);
                if (rng.nextBoolean()) {
                    pb.append('*');
                }
            }
            checkAgainstOracle(sb.toString(), pb.toString());
        }
        System.out.println("PASS randomised cross-check (2000 cases)");
    }

    public static void main(String[] args) {
        // LeetCode examples and canonical edge cases.
        check("aa", "a", false);
        check("aa", "a*", true);
        check("ab", ".*", true);
        check("aab", "c*a*b", true);
        check("mississippi", "mis*is*p*.", false);
        check("ab", ".*c", false);
        check("aaa", "a*a", true);
        check("aaa", "ab*a*c*a", true);
        check("a", "ab*", true);
        check("bbbba", ".*a*a", true);
        check("a", ".*..a*", false);
        // Empty-string / empty-pattern boundaries.
        check("", "", true);
        check("", "a*", true);
        check("", ".*", true);
        check("", "a", false);
        check("a", "", false);
        // Star consuming zero vs many copies of '.' and literals.
        check("abcd", "d*", false);
        check("abcd", ".*d", true);
        check("aaaa", "a*aa", true);
        check("a", "a*aa", false);
        check("aaaa", "a*aaa", true);

        exhaustiveCrossCheck();
        randomisedCrossCheck();
        System.out.println("All checks passed.");
    }
}
