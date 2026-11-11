import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * Day 012 — Word Ladder II (LeetCode 126, Hard).
 *
 * Given beginWord, endWord and a word list, return ALL shortest
 * transformation sequences from beginWord to endWord, where consecutive
 * words differ by exactly one letter and every word after the first is
 * in the list.
 *
 * Approach: BFS level by level from beginWord, but instead of recording
 * a single parent per word (enough for Word Ladder I's length), record
 * EVERY parent that discovers a word on the earliest possible level.
 * Those parent edges form a DAG layered by distance from beginWord;
 * a DFS from endWord back to beginWord along them enumerates exactly
 * the shortest ladders — no path the DFS can follow is longer than the
 * BFS distance, and every shortest ladder is present, because a word
 * only ever points to words one level closer to the start.
 *
 * The load-bearing detail is WHEN a word is marked visited. A word
 * discovered on level k may be reachable from several level-k words,
 * and all of them are legitimate parents, so words are removed from
 * the unvisited set only after the WHOLE level is processed. Removing
 * a word the moment it is first seen (the usual BFS reflex) silently
 * drops ladders; removing it a level later lets longer, cyclic paths
 * in. Level-synchronous removal is the whole algorithm.
 *
 * Enumerating neighbours by mutating each position through 'a'..'z'
 * costs O(L * 26) candidate words per word — cheaper and simpler than
 * pre-grouping the dictionary by wildcard patterns at this scale.
 *
 * main cross-checks against two independent oracles: a plain BFS that
 * computes only the shortest length, and a bounded brute-force DFS
 * that enumerates every simple path of exactly that length. Every
 * returned ladder is also validated directly (endpoints, one-letter
 * steps, dictionary membership).
 */
public class WordLadderII {

    public List<List<String>> findLadders(String beginWord, String endWord, List<String> wordList) {
        Set<String> dict = new HashSet<>(wordList);
        List<List<String>> result = new ArrayList<>();
        if (!dict.contains(endWord)) {
            return result;
        }

        // child -> every parent that reaches it on a shortest path
        Map<String, List<String>> parents = new HashMap<>();
        Set<String> unvisited = new HashSet<>(dict);
        unvisited.remove(beginWord);
        Set<String> level = new HashSet<>();
        level.add(beginWord);
        boolean found = false;

        while (!level.isEmpty() && !found) {
            Set<String> next = new HashSet<>();
            for (String word : level) {
                for (String nb : neighbours(word)) {
                    if (unvisited.contains(nb)) {
                        parents.computeIfAbsent(nb, k -> new ArrayList<>()).add(word);
                        next.add(nb);
                        if (nb.equals(endWord)) {
                            found = true;
                        }
                    }
                }
            }
            // Only now — the full level is done — do these words become
            // visited, so every same-level parent got recorded.
            unvisited.removeAll(next);
            level = next;
        }
        if (!found) {
            return result;
        }

        LinkedList<String> path = new LinkedList<>();
        path.add(endWord);
        backtrack(endWord, beginWord, parents, path, result);
        return result;
    }

    private void backtrack(String word, String beginWord, Map<String, List<String>> parents,
                           LinkedList<String> path, List<List<String>> result) {
        if (word.equals(beginWord)) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (String parent : parents.getOrDefault(word, List.of())) {
            path.addFirst(parent);
            backtrack(parent, beginWord, parents, path, result);
            path.removeFirst();
        }
    }

    /** All words one letter away (dictionary membership not checked). */
    private static List<String> neighbours(String word) {
        List<String> out = new ArrayList<>();
        char[] chars = word.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char original = chars[i];
            for (char c = 'a'; c <= 'z'; c++) {
                if (c == original) {
                    continue;
                }
                chars[i] = c;
                out.add(new String(chars));
            }
            chars[i] = original;
        }
        return out;
    }

    // ---------- independent oracles (tests only) ----------

    /** Plain BFS for the shortest ladder LENGTH (in words), -1 if none. */
    private static int shortestLength(String beginWord, String endWord, Set<String> dict) {
        if (!dict.contains(endWord)) {
            return -1;
        }
        Queue<String> queue = new ArrayDeque<>();
        Map<String, Integer> dist = new HashMap<>();
        queue.add(beginWord);
        dist.put(beginWord, 1);
        while (!queue.isEmpty()) {
            String word = queue.poll();
            if (word.equals(endWord)) {
                return dist.get(word);
            }
            for (String nb : neighbours(word)) {
                if (dict.contains(nb) && !dist.containsKey(nb)) {
                    dist.put(nb, dist.get(word) + 1);
                    queue.add(nb);
                }
            }
        }
        return -1;
    }

    /** Every simple path of exactly the shortest length, by brute force. */
    private static Set<List<String>> bruteForce(String beginWord, String endWord, Set<String> dict) {
        Set<List<String>> out = new HashSet<>();
        int shortest = shortestLength(beginWord, endWord, dict);
        if (shortest < 0) {
            return out;
        }
        LinkedList<String> path = new LinkedList<>();
        Set<String> used = new HashSet<>();
        path.add(beginWord);
        used.add(beginWord);
        dfs(beginWord, endWord, dict, shortest, path, used, out);
        return out;
    }

    private static void dfs(String word, String endWord, Set<String> dict, int targetLen,
                            LinkedList<String> path, Set<String> used, Set<List<String>> out) {
        if (path.size() == targetLen) {
            if (word.equals(endWord)) {
                out.add(new ArrayList<>(path));
            }
            return;
        }
        if (word.equals(endWord)) {
            return; // reached early; extending cannot stay a shortest simple path
        }
        for (String nb : neighbours(word)) {
            if (dict.contains(nb) && !used.contains(nb)) {
                path.addLast(nb);
                used.add(nb);
                dfs(nb, endWord, dict, targetLen, path, used, out);
                path.removeLast();
                used.remove(nb);
            }
        }
    }

    // ---------- checks ----------

    private static void validate(List<String> ladder, String beginWord, String endWord, Set<String> dict) {
        if (!ladder.get(0).equals(beginWord) || !ladder.get(ladder.size() - 1).equals(endWord)) {
            throw new AssertionError("bad endpoints: " + ladder);
        }
        for (int i = 1; i < ladder.size(); i++) {
            if (!dict.contains(ladder.get(i))) {
                throw new AssertionError("word not in dictionary: " + ladder);
            }
            String a = ladder.get(i - 1);
            String b = ladder.get(i);
            int diff = 0;
            for (int j = 0; j < a.length(); j++) {
                if (a.charAt(j) != b.charAt(j)) {
                    diff++;
                }
            }
            if (diff != 1) {
                throw new AssertionError("step differs by " + diff + " letters: " + ladder);
            }
        }
    }

    private static void check(String beginWord, String endWord, List<String> wordList) {
        check(beginWord, endWord, wordList, true);
    }

    private static void checkQuiet(String beginWord, String endWord, List<String> wordList) {
        check(beginWord, endWord, wordList, false);
    }

    private static void check(String beginWord, String endWord, List<String> wordList, boolean verbose) {
        Set<String> dict = new HashSet<>(wordList);
        List<List<String>> ladders = new WordLadderII().findLadders(beginWord, endWord, wordList);
        Set<List<String>> actual = new HashSet<>(ladders);
        if (actual.size() != ladders.size()) {
            throw new AssertionError("duplicate ladders returned for " + beginWord + "->" + endWord);
        }
        Set<List<String>> expected = bruteForce(beginWord, endWord, dict);
        if (!actual.equals(expected)) {
            throw new AssertionError(beginWord + "->" + endWord + ": expected " + expected
                    + " but got " + actual);
        }
        int shortest = shortestLength(beginWord, endWord, dict);
        for (List<String> ladder : ladders) {
            validate(ladder, beginWord, endWord, dict);
            if (ladder.size() != shortest) {
                throw new AssertionError("ladder not shortest: " + ladder);
            }
        }
        if (verbose) {
            System.out.println("OK " + beginWord + "->" + endWord + ": " + ladders.size()
                    + " ladder(s)" + (shortest < 0 ? " (no path)" : " of length " + shortest)
                    + " (agrees with brute force)");
        }
    }

    public static void main(String[] args) {
        check("hit", "cog", List.of("hot", "dot", "dog", "lot", "log", "cog"));
        check("red", "tax", List.of("ted", "tex", "red", "tax", "tad", "den", "rex", "pee"));
        check("a", "c", List.of("a", "b", "c"));
        check("hot", "dog", List.of("hot", "dog"));
        check("hit", "cog", List.of("hot", "dot", "dog", "lot", "log"));
        check("lost", "cost", List.of("most", "fist", "lost", "cost", "fish"));

        // Fuzz: random small dictionaries over a 3-letter alphabet,
        // every case cross-checked against the brute-force enumeration.
        List<String> pool = new ArrayList<>();
        for (char a = 'a'; a <= 'c'; a++) {
            for (char b = 'a'; b <= 'c'; b++) {
                for (char c = 'a'; c <= 'c'; c++) {
                    pool.add("" + a + b + c);
                }
            }
        }
        Random rng = new Random(123);
        int fuzzed = 0;
        for (int trial = 0; trial < 300; trial++) {
            List<String> shuffled = new ArrayList<>(pool);
            Collections.shuffle(shuffled, rng);
            List<String> dict = shuffled.subList(0, 10 + rng.nextInt(5));
            String begin = pool.get(rng.nextInt(pool.size()));
            String end = pool.get(rng.nextInt(pool.size()));
            if (!begin.equals(end)) {
                checkQuiet(begin, end, dict);
                fuzzed++;
            }
        }
        System.out.println("OK fuzz: " + fuzzed + " random dictionaries agree with brute force");

        System.out.println("All checks passed.");
    }
}
