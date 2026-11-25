import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Day 027 — Text Justification (LeetCode 68, Hard).
 *
 * Given words in order and a line width maxWidth, format the text so that
 * every line has exactly maxWidth characters and is fully justified:
 * pack as many words per line as fit, distribute the spaces between words
 * as evenly as possible with the extra spaces going to the leftmost gaps,
 * pad a single-word line on the right, and left-justify the last line
 * (single spaces between words, trailing padding).
 *
 * Approach: greedy line packing, then exact space arithmetic per line.
 * A line holding words[i..j) has letters = sum of word lengths and needs at
 * least (j-i-1) single spaces to fit, so the greedy scan extends j while
 * letters + gaps <= maxWidth. For a non-last line with g = j-i-1 gaps and
 * S = maxWidth - letters total spaces, each gap gets S/g spaces and the
 * first S%g gaps get one extra — that is the whole "as evenly as possible,
 * extras on the left" rule in two divisions. The last line and the
 * single-word case (g == 0) degenerate to left-justification with right
 * padding, handled by the same builder. O(total characters output) time,
 * O(maxWidth) extra space beyond the output.
 */
public class TextJustification {

    public static List<String> fullJustify(String[] words, int maxWidth) {
        List<String> lines = new ArrayList<>();
        int n = words.length;
        int i = 0;
        while (i < n) {
            // Greedy pack: words[i..j) is the longest prefix fitting one line.
            int j = i;
            int letters = 0;
            while (j < n && letters + words[j].length() + (j - i) <= maxWidth) {
                letters += words[j].length();
                j++;
            }
            lines.add(buildLine(words, i, j, letters, maxWidth, j == n));
            i = j;
        }
        return lines;
    }

    private static String buildLine(String[] words, int i, int j, int letters,
                                    int maxWidth, boolean last) {
        StringBuilder sb = new StringBuilder(maxWidth);
        int gaps = j - i - 1;
        if (last || gaps == 0) {
            // Left-justify: single spaces between words, pad right.
            for (int k = i; k < j; k++) {
                if (k > i) {
                    sb.append(' ');
                }
                sb.append(words[k]);
            }
            while (sb.length() < maxWidth) {
                sb.append(' ');
            }
            return sb.toString();
        }
        int totalSpaces = maxWidth - letters;
        int base = totalSpaces / gaps;
        int extra = totalSpaces % gaps; // first `extra` gaps get one more
        for (int k = i; k < j; k++) {
            if (k > i) {
                int gapIndex = k - i - 1;
                int width = base + (gapIndex < extra ? 1 : 0);
                for (int s = 0; s < width; s++) {
                    sb.append(' ');
                }
            }
            sb.append(words[k]);
        }
        return sb.toString();
    }

    // ---------- checks ----------

    private static void check(String[] words, int maxWidth, String[] expected) {
        List<String> actual = fullJustify(words, maxWidth);
        if (!actual.equals(Arrays.asList(expected))) {
            throw new AssertionError("fullJustify(" + Arrays.toString(words) + ", " + maxWidth
                    + ") = " + actual + ", expected " + Arrays.asList(expected));
        }
        for (String line : actual) {
            if (line.length() != maxWidth) {
                throw new AssertionError("line length " + line.length() + " != " + maxWidth
                        + ": [" + line + "]");
            }
        }
        System.out.println("PASS fullJustify(" + Arrays.toString(words) + ", " + maxWidth + ")");
        for (String line : actual) {
            System.out.println("     [" + line + "]");
        }
    }

    /** Invariant fuzz: width exact, words preserved in order, non-last lines fully spread. */
    private static void fuzz() {
        Random rnd = new Random(27);
        String[] vocab = { "a", "be", "cat", "dogs", "eagle", "foobar", "g", "hello", "justified", "k" };
        for (int trial = 0; trial < 3000; trial++) {
            int maxWidth = 10 + rnd.nextInt(21);
            int count = 1 + rnd.nextInt(15);
            String[] words = new String[count];
            for (int k = 0; k < count; k++) {
                String w = vocab[rnd.nextInt(vocab.length)];
                if (w.length() > maxWidth) {
                    w = w.substring(0, maxWidth);
                }
                words[k] = w;
            }
            List<String> lines = fullJustify(words, maxWidth);
            List<String> recovered = new ArrayList<>();
            for (int li = 0; li < lines.size(); li++) {
                String line = lines.get(li);
                if (line.length() != maxWidth) {
                    throw new AssertionError("fuzz: bad width " + line.length() + " != " + maxWidth);
                }
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    recovered.addAll(Arrays.asList(trimmed.split(" +")));
                }
                boolean lastLine = li == lines.size() - 1;
                if (!lastLine && trimmed.contains(" ")) {
                    // Fully justified: no trailing space, gaps differ by at most 1,
                    // and wider gaps never appear to the right of narrower ones.
                    if (line.endsWith(" ")) {
                        throw new AssertionError("fuzz: justified line ends with space: [" + line + "]");
                    }
                    String[] parts = trimmed.split("(?<=\\S)(?= +\\S)|(?<= )(?=\\S)", -1);
                    int prevGap = Integer.MAX_VALUE;
                    int minGap = Integer.MAX_VALUE;
                    int maxGap = 0;
                    for (int p = 0; p + 1 < parts.length; p++) {
                        // parts alternate word / space-run when split keeps delimiters;
                        // simpler: measure gaps directly below.
                        break;
                    }
                    int[] gaps = gapsOf(line);
                    for (int g : gaps) {
                        minGap = Math.min(minGap, g);
                        maxGap = Math.max(maxGap, g);
                        if (g > prevGap) {
                            throw new AssertionError("fuzz: wider gap right of narrower: [" + line + "]");
                        }
                        prevGap = g;
                    }
                    if (gaps.length > 0 && maxGap - minGap > 1) {
                        throw new AssertionError("fuzz: uneven gaps in [" + line + "]");
                    }
                }
            }
            if (!recovered.equals(Arrays.asList(words))) {
                throw new AssertionError("fuzz: words not preserved: " + recovered
                        + " vs " + Arrays.asList(words));
            }
        }
        System.out.println("PASS fuzz (3000 trials: exact width, word order, even left-heavy gaps)");
    }

    private static int[] gapsOf(String line) {
        List<Integer> gaps = new ArrayList<>();
        int i = 0;
        int n = line.length();
        while (i < n) {
            while (i < n && line.charAt(i) != ' ') {
                i++;
            }
            int start = i;
            while (i < n && line.charAt(i) == ' ') {
                i++;
            }
            if (i < n && start > 0) {
                gaps.add(i - start);
            }
        }
        return gaps.stream().mapToInt(Integer::intValue).toArray();
    }

    public static void main(String[] args) {
        check(new String[] { "This", "is", "an", "example", "of", "text", "justification." }, 16,
                new String[] {
                    "This    is    an",
                    "example  of text",
                    "justification.  "
                });
        check(new String[] { "What", "must", "be", "acknowledgment", "shall", "be" }, 16,
                new String[] {
                    "What   must   be",
                    "acknowledgment  ",
                    "shall be        "
                });
        check(new String[] {
                    "Science", "is", "what", "we", "understand", "well", "enough", "to", "explain",
                    "to", "a", "computer.", "Art", "is", "everything", "else", "we", "do"
                }, 20,
                new String[] {
                    "Science  is  what we",
                    "understand      well",
                    "enough to explain to",
                    "a  computer.  Art is",
                    "everything  else  we",
                    "do                  "
                });
        // Single word exactly filling the width; single word needing padding.
        check(new String[] { "exactlyten" }, 10, new String[] { "exactlyten" });
        check(new String[] { "hi" }, 5, new String[] { "hi   " });
        // Every word alone on its line (none can share).
        check(new String[] { "aaaa", "bbbb", "cccc" }, 5,
                new String[] { "aaaa ", "bbbb ", "cccc " });
        // Even distribution with no remainder, and remainder spread left-first.
        check(new String[] { "a", "b", "c", "dddddddd" }, 8,
                new String[] { "a   b  c", "dddddddd" });
        check(new String[] { "aa", "bb", "cc", "zzzzzzzzzz" }, 10,
                new String[] { "aa  bb  cc", "zzzzzzzzzz" });
        fuzz();
        System.out.println("All checks passed.");
    }
}
