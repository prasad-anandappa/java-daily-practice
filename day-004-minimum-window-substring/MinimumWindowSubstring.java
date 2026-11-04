import java.util.HashMap;
import java.util.Map;

/**
 * Day 004 — Minimum Window Substring (LeetCode 76, Hard).
 *
 * Given strings s and t, return the smallest substring of s that contains
 * every character of t, including duplicates. If no such substring exists,
 * or t is empty, return "".
 *
 * Approach: sliding window with frequency counts. Expand the right edge
 * until the window covers t (tracked by how many distinct characters have
 * reached their required count), then shrink the left edge as far as the
 * coverage survives, recording the best window seen. Each index enters and
 * leaves the window once, so the scan is O(|s| + |t|) time.
 */
public class MinimumWindowSubstring {

    public static String minWindow(String s, String t) {
        if (s == null || t == null || t.isEmpty() || s.length() < t.length()) {
            return "";
        }

        Map<Character, Integer> need = new HashMap<>();
        for (int i = 0; i < t.length(); i++) {
            need.merge(t.charAt(i), 1, Integer::sum);
        }
        int required = need.size();

        Map<Character, Integer> window = new HashMap<>();
        int formed = 0;
        int left = 0;
        int bestLen = Integer.MAX_VALUE;
        int bestLeft = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            Integer needCount = need.get(c);
            if (needCount != null) {
                int count = window.getOrDefault(c, 0) + 1;
                window.put(c, count);
                if (count == needCount) {
                    formed++;
                }
            }

            while (formed == required) {
                int len = right - left + 1;
                if (len < bestLen) {
                    bestLen = len;
                    bestLeft = left;
                }
                char out = s.charAt(left);
                Integer outNeed = need.get(out);
                if (outNeed != null) {
                    int count = window.get(out) - 1;
                    window.put(out, count);
                    if (count < outNeed) {
                        formed--;
                    }
                }
                left++;
            }
        }

        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestLeft, bestLeft + bestLen);
    }

    private static void check(String s, String t, String expected) {
        String actual = minWindow(s, t);
        if (!actual.equals(expected)) {
            throw new AssertionError("minWindow(\"" + s + "\", \"" + t + "\") = \"" + actual
                    + "\", expected \"" + expected + "\"");
        }
        System.out.println("PASS minWindow(\"" + s + "\", \"" + t + "\") = \"" + actual + "\"");
    }

    public static void main(String[] args) {
        check("ADOBECODEBANC", "ABC", "BANC");
        check("a", "a", "a");
        check("a", "aa", "");
        check("ab", "b", "b");
        check("bbaa", "aba", "baa");
        check("aa", "aa", "aa");
        check("abacbc", "abc", "bac");
        check("cabwefgewcwaefgcf", "cae", "cwae");
        check("thisisateststring", "tist", "tstri");
        check("aaaaaaaaaaaabbbbbcdd", "abcdd", "abbbbbcdd");
        check("", "a", "");
        check("abc", "", "");
        System.out.println("All checks passed.");
    }
}
