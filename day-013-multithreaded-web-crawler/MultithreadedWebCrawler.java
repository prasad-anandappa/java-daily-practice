import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Day 013 — Multithreaded Web Crawler (LeetCode 1242, Hard).
 *
 * Crawl every page reachable from a start URL, following only links whose
 * hostname equals the start URL's hostname, fetching each page exactly once,
 * and do the fetching in parallel.
 *
 * Approach: a ForkJoinPool of RecursiveActions. The visited set is a
 * ConcurrentHashMap key set, and a page is claimed with the atomic
 * visited.add(url) BEFORE its fetch task is forked — that single atomic
 * test-and-set is what guarantees exactly-once fetching when dozens of
 * pages link to the same target at the same moment. A task forks one child
 * per newly claimed link, then joins them all.
 *
 * Why ForkJoin and not a fixed ExecutorService with futures: a crawler task
 * blocks waiting for the children it just submitted. In a fixed pool, a
 * deep or wide crawl can fill every thread with parents waiting for
 * children that can never start — a thread-starvation deadlock. ForkJoin's
 * work stealing lets a thread waiting in join() execute other pending
 * tasks instead, so parent/child crawling cannot deadlock that way.
 */
public class MultithreadedWebCrawler {

    /** The crawler's only window on the "web": the links out of a page. */
    public interface HtmlParser {
        List<String> getUrls(String url);
    }

    private static String hostname(String url) {
        int start = url.indexOf("://");
        start = (start < 0) ? 0 : start + 3;
        int end = url.indexOf('/', start);
        return (end < 0) ? url.substring(start) : url.substring(start, end);
    }

    private static class CrawlTask extends RecursiveAction {
        private final String url;
        private final String host;
        private final HtmlParser parser;
        private final Set<String> visited;

        CrawlTask(String url, String host, HtmlParser parser, Set<String> visited) {
            this.url = url;
            this.host = host;
            this.parser = parser;
            this.visited = visited;
        }

        @Override
        protected void compute() {
            List<CrawlTask> children = new ArrayList<>();
            for (String next : parser.getUrls(url)) {
                // Claim before forking: add() is atomic, so exactly one
                // racing task ever wins a given URL; everyone else skips it.
                if (hostname(next).equals(host) && visited.add(next)) {
                    CrawlTask child = new CrawlTask(next, host, parser, visited);
                    child.fork();
                    children.add(child);
                }
            }
            for (CrawlTask child : children) {
                child.join();
            }
        }
    }

    public static List<String> crawl(String startUrl, HtmlParser parser, int parallelism) {
        Set<String> visited = ConcurrentHashMap.newKeySet();
        visited.add(startUrl);
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            pool.invoke(new CrawlTask(startUrl, hostname(startUrl), parser, visited));
        } finally {
            pool.shutdown();
        }
        return new ArrayList<>(visited);
    }

    // ---- Test double: an in-memory web that records how it was crawled ----

    private static class FakeParser implements HtmlParser {
        private final Map<String, List<String>> graph;
        private final long delayMillis;
        final Map<String, AtomicInteger> callCounts = new ConcurrentHashMap<>();
        final Set<String> threadNames = ConcurrentHashMap.newKeySet();

        FakeParser(Map<String, List<String>> graph, long delayMillis) {
            this.graph = graph;
            this.delayMillis = delayMillis;
        }

        @Override
        public List<String> getUrls(String url) {
            callCounts.computeIfAbsent(url, k -> new AtomicInteger()).incrementAndGet();
            threadNames.add(Thread.currentThread().getName());
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
            return graph.getOrDefault(url, List.of());
        }
    }

    // ---- Checks ----

    private static void check(String name, Map<String, List<String>> graph, String start,
                              Set<String> expected, long delayMillis) {
        FakeParser parser = new FakeParser(graph, delayMillis);
        Set<String> actual = new HashSet<>(crawl(start, parser, 8));
        if (!actual.equals(expected)) {
            throw new AssertionError(name + ": crawled " + actual + ", expected " + expected);
        }
        for (String url : expected) {
            int calls = parser.callCounts.getOrDefault(url, new AtomicInteger(0)).get();
            if (calls != 1) {
                throw new AssertionError(name + ": " + url + " fetched " + calls
                        + " times, expected exactly 1");
            }
        }
        if (parser.callCounts.size() != expected.size()) {
            throw new AssertionError(name + ": parser saw " + parser.callCounts.keySet()
                    + ", expected only " + expected);
        }
        System.out.println("PASS " + name + " -> " + expected.size() + " pages, each fetched once");
    }

    /** Independent oracle: plain single-threaded BFS over the same graph. */
    private static Set<String> sequentialReachable(Map<String, List<String>> graph, String start) {
        Set<String> seen = new HashSet<>();
        List<String> stack = new ArrayList<>();
        seen.add(start);
        stack.add(start);
        while (!stack.isEmpty()) {
            String cur = stack.remove(stack.size() - 1);
            for (String next : graph.getOrDefault(cur, List.of())) {
                if (hostname(next).equals(hostname(start)) && seen.add(next)) {
                    stack.add(next);
                }
            }
        }
        return seen;
    }

    public static void main(String[] args) {
        String a = "http://news.example.com/a";
        String b = "http://news.example.com/b";
        String c = "http://news.example.com/c";
        String d = "http://news.example.com/d";

        // Diamond + cycle + duplicate links + a cross-host link that must be ignored.
        check("diamond-cycle-cross-host",
                Map.of(a, List.of(b, c, b),
                       b, List.of(d, a),
                       c, List.of(d),
                       d, List.of(a, "http://other.example.com/x")),
                a, Set.of(a, b, c, d), 0);

        // Self-loop only.
        check("self-loop", Map.of(a, List.of(a, a)), a, Set.of(a), 0);

        // Isolated start page.
        check("isolated", Map.of(), a, Set.of(a), 0);

        // A subdomain is a different hostname and must not be followed.
        check("subdomain-excluded",
                Map.of(a, List.of("http://blog.example.com/b")),
                a, Set.of(a), 0);

        // Wide graph: root -> 30 leaves -> one shared sink. With a per-fetch
        // delay the work must spread over more than one pool thread, and the
        // sink — linked from all 30 leaves at once — is still fetched once.
        Map<String, List<String>> wide = new HashMap<>();
        List<String> leaves = new ArrayList<>();
        Set<String> wideExpected = new HashSet<>();
        wideExpected.add(a);
        wideExpected.add(d);
        for (int i = 0; i < 30; i++) {
            String leaf = "http://news.example.com/leaf" + i;
            leaves.add(leaf);
            wideExpected.add(leaf);
            wide.put(leaf, List.of(d));
        }
        wide.put(a, leaves);
        wide.put(d, List.of());
        FakeParser wideParser = new FakeParser(wide, 20);
        Set<String> wideActual = new HashSet<>(crawl(a, wideParser, 8));
        if (!wideActual.equals(wideExpected)) {
            throw new AssertionError("wide: crawled " + wideActual.size()
                    + " pages, expected " + wideExpected.size());
        }
        if (wideParser.callCounts.get(d).get() != 1) {
            throw new AssertionError("wide: shared sink fetched "
                    + wideParser.callCounts.get(d).get() + " times, expected 1");
        }
        if (wideParser.threadNames.size() < 2) {
            throw new AssertionError("wide: crawl used only threads " + wideParser.threadNames
                    + " — no parallelism happened");
        }
        System.out.println("PASS wide-parallel -> " + wideActual.size() + " pages over "
                + wideParser.threadNames.size() + " threads, sink fetched once");

        // Deep chain of 200 pages: parent tasks wait on children the whole
        // way down — the shape that starves a naive fixed-pool crawler.
        Map<String, List<String>> chain = new HashMap<>();
        Set<String> chainExpected = new HashSet<>();
        String prev = null;
        for (int i = 0; i < 200; i++) {
            String cur = "http://news.example.com/p" + i;
            chainExpected.add(cur);
            if (prev != null) {
                chain.put(prev, List.of(cur));
            }
            prev = cur;
        }
        chain.put(prev, List.of());
        check("deep-chain-200", chain, "http://news.example.com/p0", chainExpected, 0);

        // Fuzz: random graphs (cycles, duplicates, cross-host links) must
        // match the sequential BFS oracle exactly, with exactly-once fetches.
        Random rng = new Random(42);
        for (int trial = 0; trial < 100; trial++) {
            int n = 1 + rng.nextInt(40);
            List<String> nodes = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                nodes.add("http://news.example.com/n" + i);
            }
            Map<String, List<String>> g = new HashMap<>();
            for (String node : nodes) {
                List<String> out = new ArrayList<>();
                int edges = rng.nextInt(5);
                for (int e = 0; e < edges; e++) {
                    out.add(rng.nextInt(10) == 0
                            ? "http://elsewhere.example.com/z"
                            : nodes.get(rng.nextInt(n)));
                }
                g.put(node, out);
            }
            FakeParser p = new FakeParser(g, 0);
            Set<String> actual = new HashSet<>(crawl(nodes.get(0), p, 8));
            Set<String> expected = sequentialReachable(g, nodes.get(0));
            if (!actual.equals(expected)) {
                throw new AssertionError("fuzz trial " + trial + ": crawled " + actual
                        + ", oracle says " + expected);
            }
            for (Map.Entry<String, AtomicInteger> e : p.callCounts.entrySet()) {
                if (e.getValue().get() != 1) {
                    throw new AssertionError("fuzz trial " + trial + ": " + e.getKey()
                            + " fetched " + e.getValue().get() + " times");
                }
            }
        }
        System.out.println("PASS fuzz-100-random-graphs -> all match sequential BFS oracle, exactly-once holds");

        System.out.println("All checks passed.");
    }
}
