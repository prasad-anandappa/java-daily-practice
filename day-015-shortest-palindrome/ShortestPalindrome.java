import java.util.Random;

/**
 * Day 015 — Shortest Palindrome (LeetCode 214, Hard).
 *
 * Given a string {@code s}, return the shortest palindrome that can be
 * formed by adding characters only in front of {@code s}. Equivalently:
 * find the longest palindromic prefix of {@code s}, keep it as the core,
 * and prepend the reverse of the remaining suffix.
 *
 * Approach: Knuth-Morris-Pratt prefix function on
 * {@code s + '#' + reverse(s)}. In that combined string, the prefix
 * function value at the last position is exactly the length of the
 * longest prefix of {@code s} that is also a suffix of {@code reverse(s)}
 * — i.e. a prefix of {@code s} that reads the same forwards and
 * backwards: the longest palindromic prefix. The '#' separator stops
 * matches from wrapping across the boundary. Prepending
 * {@code reverse(suffix)} then mirrors precisely the part that is not
 * already palindromic, and nothing shorter can work: any valid answer
 * must mirror that suffix, and mirroring it suffices.
 * O(n) time, O(n) space — versus O(n^2) for testing every prefix.
 */
public class ShortestPalindrome {

    public static String shortestPalindrome(String s) {
        if (s == null) {
            throw new IllegalArgumentException("s must not be null");
        }
        int n = s.length();
        if (n <= 1) {
            return s;
        }
        String rev = new StringBuilder(s).reverse().toString();
        String combined = s + '#' + rev;
        int longest = prefixFunction(combined)[combined.length() - 1];
        String suffix = s.substring(longest);
        return new StringBuilder(suffix).reverse().toString() + s;
    }

    /** KMP prefix function: pi[i] = length of longest proper prefix of t[0..i] that is also a suffix. */
    private static int[] prefixFunction(String t) {
        int[] pi = new int[t.length()];
        for (int i = 1; i < t.length(); i++) {
            int j = pi[i - 1];
            while (j > 0 && t.charAt(i) != t.charAt(j)) {
                j = pi[j - 1];
            }
            if (t.charAt(i) == t.charAt(j)) {
                j++;
            }
            pi[i] = j;
        }
        return pi;
    }

    // ---- Brute-force oracle (test only): try every prepend length. ----

    private static boolean isPalindrome(String t) {
        int lo = 0;
        int hi = t.length() - 1;
        while (lo < hi) {
            if (t.charAt(lo++) != t.charAt(hi--)) {
                return false;
            }
        }
        return true;
    }

    private static String bruteForce(String s) {
        for (int cut = s.length(); cut >= 0; cut--) {
            if (isPalindrome(s.substring(0, cut))) {
                return new StringBuilder(s.substring(cut)).reverse().toString() + s;
            }
        }
        throw new AssertionError("unreachable");
    }

    private static void check(String input, String expected) {
        String actual = shortestPalindrome(input);
        if (!actual.equals(expected)) {
            throw new AssertionError("shortestPalindrome(\"" + input + "\") = \"" + actual
                    + "\", expected \"" + expected + "\"");
        }
        if (!isPalindrome(actual) || !actual.endsWith(input)) {
            throw new AssertionError("result \"" + actual + "\" is not a palindromic superstring of \""
                    + input + "\"");
        }
        System.out.println("PASS shortestPalindrome(\"" + input + "\") = \"" + actual + "\"");
    }

    public static void main(String[] args) {
        check("aacecaaa", "aaacecaaa");
        check("abcd", "dcbabcd");
        check("aa", "aa");
        check("aba", "aba");
        check("a", "a");
        check("", "");
        check("ab", "bab");
        check("aabba", "abbaabba");
        check("race", "ecarace");
        check("aaaa", "aaaa");
        check("abbacd", "dcabbacd");

        // Randomised cross-check against the brute-force oracle.
        Random rng = new Random(214);
        String alphabet = "abc";
        for (int trial = 0; trial < 500; trial++) {
            int len = rng.nextInt(13);
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                sb.append(alphabet.charAt(rng.nextInt(alphabet.length())));
            }
            String s = sb.toString();
            String fast = shortestPalindrome(s);
            String slow = bruteForce(s);
            if (!fast.equals(slow)) {
                throw new AssertionError("mismatch on \"" + s + "\": KMP=" + fast + ", oracle=" + slow);
            }
        }
        System.out.println("PASS randomised cross-check vs brute-force oracle (500 trials)");
        System.out.println("All checks passed.");
    }
}
