import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

/**
 * Day 029 — Alien Dictionary.
 *
 * Given words sorted lexicographically by an unknown alien alphabet,
 * derive an ordering of all distinct letters consistent with that sort,
 * or return "" if no such ordering exists (LeetCode 269, Hard).
 *
 * Approach: only adjacent word pairs teach anything, and only their
 * first differing position does: it yields one precedence edge
 * earlier -> later. A pair with no differing position is valid only
 * when the shorter word comes first; a longer word before its own
 * prefix is an immediate contradiction. All edges form a directed
 * graph over the letters, and any topological order of that graph is
 * a valid alphabet — a cycle means the dictionary is inconsistent.
 * Kahn's algorithm with a min-heap emits the lexicographically
 * smallest topological order, which makes the output deterministic.
 * O(C + V + E) time for C total characters, O(V + E) space.
 */
public class AlienDictionary {

    public static String alienOrder(String[] words) {
        Set<Character> letters = new HashSet<>();
        for (String w : words) {
            for (char c : w.toCharArray()) {
                letters.add(c);
            }
        }
        Map<Character, Set<Character>> adj = new HashMap<>();
        Map<Character, Integer> indegree = new HashMap<>();
        for (char c : letters) {
            adj.put(c, new HashSet<>());
            indegree.put(c, 0);
        }
        for (int i = 0; i + 1 < words.length; i++) {
            String a = words[i];
            String b = words[i + 1];
            int limit = Math.min(a.length(), b.length());
            int j = 0;
            while (j < limit && a.charAt(j) == b.charAt(j)) {
                j++;
            }
            if (j == limit) {
                if (a.length() > b.length()) {
                    return ""; // longer word before its own prefix
                }
                continue;
            }
            char from = a.charAt(j);
            char to = b.charAt(j);
            if (adj.get(from).add(to)) {
                indegree.put(to, indegree.get(to) + 1);
            }
        }
        PriorityQueue<Character> ready = new PriorityQueue<>();
        for (char c : letters) {
            if (indegree.get(c) == 0) {
                ready.add(c);
            }
        }
        StringBuilder out = new StringBuilder();
        while (!ready.isEmpty()) {
            char c = ready.poll();
            out.append(c);
            for (char next : adj.get(c)) {
                int d = indegree.get(next) - 1;
                indegree.put(next, d);
                if (d == 0) {
                    ready.add(next);
                }
            }
        }
        return out.length() == letters.size() ? out.toString() : "";
    }

    // ----- validation helpers (tests only) -----

    /** True iff order is a permutation of the distinct letters respecting every derivable constraint. */
    private static boolean isValid(String[] words, String order) {
        Set<Character> letters = new HashSet<>();
        for (String w : words) {
            for (char c : w.toCharArray()) {
                letters.add(c);
            }
        }
        if (order.length() != letters.size()) {
            return false;
        }
        Map<Character, Integer> pos = new HashMap<>();
        for (int i = 0; i < order.length(); i++) {
            if (!letters.contains(order.charAt(i)) || pos.put(order.charAt(i), i) != null) {
                return false;
            }
        }
        for (int i = 0; i + 1 < words.length; i++) {
            String a = words[i];
            String b = words[i + 1];
            int limit = Math.min(a.length(), b.length());
            int j = 0;
            while (j < limit && a.charAt(j) == b.charAt(j)) {
                j++;
            }
            if (j == limit) {
                if (a.length() > b.length()) {
                    return false;
                }
            } else if (pos.get(a.charAt(j)) > pos.get(b.charAt(j))) {
                return false;
            }
        }
        return true;
    }

    /** Brute-force oracle: some permutation of the distinct letters is valid. */
    private static boolean bruteHasOrder(String[] words) {
        Set<Character> set = new HashSet<>();
        for (String w : words) {
            for (char c : w.toCharArray()) {
                set.add(c);
            }
        }
        List<Character> chars = new ArrayList<>(set);
        return permute(words, chars, 0);
    }

    private static boolean permute(String[] words, List<Character> chars, int idx) {
        if (idx == chars.size()) {
            StringBuilder sb = new StringBuilder();
            for (char c : chars) {
                sb.append(c);
            }
            return isValid(words, sb.toString());
        }
        for (int i = idx; i < chars.size(); i++) {
            java.util.Collections.swap(chars, idx, i);
            if (permute(words, chars, idx + 1)) {
                return true;
            }
            java.util.Collections.swap(chars, idx, i);
        }
        return false;
    }

    private static void checkExact(String[] words, String expected) {
        String actual = alienOrder(words);
        if (!actual.equals(expected)) {
            throw new AssertionError("alienOrder(" + java.util.Arrays.toString(words)
                    + ") = \"" + actual + "\", expected \"" + expected + "\"");
        }
        System.out.println("PASS alienOrder(" + java.util.Arrays.toString(words)
                + ") = \"" + actual + "\"");
    }

    private static void checkValid(String[] words) {
        String actual = alienOrder(words);
        if (!isValid(words, actual)) {
            throw new AssertionError("alienOrder(" + java.util.Arrays.toString(words)
                    + ") = \"" + actual + "\" is not a valid order");
        }
        System.out.println("PASS alienOrder(" + java.util.Arrays.toString(words)
                + ") = \"" + actual + "\" (valid)");
    }

    public static void main(String[] args) {
        checkExact(new String[] { "wrt", "wrf", "er", "ett", "rftt" }, "wertf");
        checkExact(new String[] { "z", "x" }, "zx");
        checkExact(new String[] { "z", "x", "z" }, "");
        checkExact(new String[] { "abc", "ab" }, "");
        checkExact(new String[] { "a", "b", "a" }, "");
        checkValid(new String[] { "ab", "adc" });
        checkValid(new String[] { "ab", "abc" });
        checkValid(new String[] { "ac", "ab", "zc", "zb" });
        checkValid(new String[] { "abcd" });
        checkValid(new String[] { "baa", "abcd", "abca", "cab", "cad" });
        checkValid(new String[] {});

        // Randomised cross-check against the brute-force permutation oracle:
        // alienOrder must return "" exactly when no valid permutation exists,
        // and a genuinely valid order otherwise.
        Random rng = new Random(29);
        for (int trial = 0; trial < 3000; trial++) {
            int wordCount = 1 + rng.nextInt(5);
            String[] words = new String[wordCount];
            for (int i = 0; i < wordCount; i++) {
                int len = 1 + rng.nextInt(3);
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < len; k++) {
                    sb.append((char) ('a' + rng.nextInt(4)));
                }
                words[i] = sb.toString();
            }
            String actual = alienOrder(words);
            boolean exists = bruteHasOrder(words);
            if (!exists && !actual.isEmpty()) {
                throw new AssertionError("trial " + trial + ": expected \"\" for "
                        + java.util.Arrays.toString(words) + ", got \"" + actual + "\"");
            }
            if (exists && !isValid(words, actual)) {
                throw new AssertionError("trial " + trial + ": invalid order \"" + actual
                        + "\" for " + java.util.Arrays.toString(words));
            }
        }
        System.out.println("PASS randomised cross-check vs brute-force oracle (3000 trials)");
        System.out.println("All checks passed.");
    }
}
