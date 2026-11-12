import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Day 014 — LFU Cache (LeetCode 460, Hard).
 *
 * Design a cache with capacity {@code capacity} where {@code get} and
 * {@code put} both run in O(1) average time. When the cache is full and a
 * new key is inserted, evict the least frequently used key; if several
 * keys share the lowest frequency, evict the least recently used among
 * them.
 *
 * Approach: two levels of hashing plus a hand-rolled doubly linked list
 * per frequency — the same ingredients {@code LinkedHashMap} hides.
 *   keyMap : key  -> Node
 *   freqMap: freq -> FreqList (nodes at that frequency, LRU at the head,
 *                            most recently touched at the tail)
 * A running {@code minFreq} names the only bucket eviction ever looks at,
 * so eviction is: pop the head of {@code freqMap[minFreq]} — O(1), no scan.
 * Touching a key detaches its node from its old frequency list (pointer
 * surgery, O(1)) and appends it to the tail of the next frequency's list.
 * If the old list was the minimum list and empties, minFreq increments —
 * sound because the touched node itself just landed at minFreq + 1 and no
 * key can sit at a frequency between the old minimum and that.
 * New keys start at frequency 1, which resets minFreq to 1.
 */
public class LFUCache {

    private static final class Node {
        final int key;
        int value;
        int freq = 1;
        Node prev;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    /** Doubly linked list with sentinels: head.next is LRU, tail.prev is MRU. */
    private static final class FreqList {
        private final Node head = new Node(-1, -1);
        private final Node tail = new Node(-1, -1);
        private int size;

        FreqList() {
            head.next = tail;
            tail.prev = head;
        }

        void addLast(Node node) {
            node.prev = tail.prev;
            node.next = tail;
            tail.prev.next = node;
            tail.prev = node;
            size++;
        }

        void remove(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }

        Node removeFirst() {
            if (size == 0) {
                return null;
            }
            Node node = head.next;
            remove(node);
            return node;
        }

        boolean isEmpty() {
            return size == 0;
        }
    }

    private final int capacity;
    private final Map<Integer, Node> keyMap = new HashMap<>();
    private final Map<Integer, FreqList> freqMap = new HashMap<>();
    private int minFreq;

    public LFUCache(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be >= 0: " + capacity);
        }
        this.capacity = capacity;
    }

    public int get(int key) {
        Node node = keyMap.get(key);
        if (node == null) {
            return -1;
        }
        touch(node);
        return node.value;
    }

    public void put(int key, int value) {
        if (capacity == 0) {
            return;
        }
        Node node = keyMap.get(key);
        if (node != null) {
            node.value = value;
            touch(node);
            return;
        }
        if (keyMap.size() == capacity) {
            Node evicted = freqMap.get(minFreq).removeFirst();
            keyMap.remove(evicted.key);
        }
        Node fresh = new Node(key, value);
        keyMap.put(key, fresh);
        freqMap.computeIfAbsent(1, f -> new FreqList()).addLast(fresh);
        minFreq = 1;
    }

    /** Promote {@code node} one frequency bucket up, as most-recent there. */
    private void touch(Node node) {
        FreqList oldList = freqMap.get(node.freq);
        oldList.remove(node);
        if (node.freq == minFreq && oldList.isEmpty()) {
            minFreq++;
        }
        node.freq++;
        freqMap.computeIfAbsent(node.freq, f -> new FreqList()).addLast(node);
    }

    int size() {
        return keyMap.size();
    }

    // ----- Self-checking harness (no dependencies) -----

    /** Naive O(n)-per-op oracle: scans for min freq, ties broken by oldest touch. */
    private static final class Oracle {
        private static final class Entry {
            int value;
            int freq;
            long lastTouch;

            Entry(int value, long tick) {
                this.value = value;
                this.freq = 1;
                this.lastTouch = tick;
            }
        }

        private final int capacity;
        private final Map<Integer, Entry> map = new HashMap<>();
        private long tick;

        Oracle(int capacity) {
            this.capacity = capacity;
        }

        int get(int key) {
            Entry e = map.get(key);
            if (e == null) {
                return -1;
            }
            e.freq++;
            e.lastTouch = ++tick;
            return e.value;
        }

        void put(int key, int value) {
            if (capacity == 0) {
                return;
            }
            Entry e = map.get(key);
            if (e != null) {
                e.value = value;
                e.freq++;
                e.lastTouch = ++tick;
                return;
            }
            if (map.size() == capacity) {
                int victim = -1;
                int bestFreq = Integer.MAX_VALUE;
                long bestTouch = Long.MAX_VALUE;
                for (Map.Entry<Integer, Entry> en : map.entrySet()) {
                    Entry v = en.getValue();
                    if (v.freq < bestFreq || (v.freq == bestFreq && v.lastTouch < bestTouch)) {
                        bestFreq = v.freq;
                        bestTouch = v.lastTouch;
                        victim = en.getKey();
                    }
                }
                map.remove(victim);
            }
            map.put(key, new Entry(value, ++tick));
        }

        int size() {
            return map.size();
        }
    }

    private static void checkGet(LFUCache cache, int key, int expected) {
        int actual = cache.get(key);
        if (actual != expected) {
            throw new AssertionError("get(" + key + ") = " + actual + ", expected " + expected);
        }
        System.out.println("PASS get(" + key + ") = " + actual);
    }

    private static void checkSize(LFUCache cache, int expected) {
        if (cache.size() != expected) {
            throw new AssertionError("size() = " + cache.size() + ", expected " + expected);
        }
        System.out.println("PASS size() = " + expected);
    }

    public static void main(String[] args) {
        // Canonical LeetCode sequence.
        LFUCache c = new LFUCache(2);
        c.put(1, 1);
        c.put(2, 2);
        checkGet(c, 1, 1);   // freq(1) -> 2
        c.put(3, 3);         // evicts key 2 (only freq-1 key)
        checkGet(c, 2, -1);
        checkGet(c, 3, 3);   // freq(3) -> 2
        c.put(4, 4);         // keys 1 and 3 both freq 2; key 1 is LRU -> evicted
        checkGet(c, 1, -1);
        checkGet(c, 3, 3);
        checkGet(c, 4, 4);
        checkSize(c, 2);

        // Zero capacity: puts are no-ops, gets always miss.
        LFUCache zero = new LFUCache(0);
        zero.put(1, 10);
        checkGet(zero, 1, -1);
        checkSize(zero, 0);

        // Capacity one: every distinct put evicts the previous key.
        LFUCache one = new LFUCache(1);
        one.put(7, 70);
        checkGet(one, 7, 70);
        one.put(8, 80);
        checkGet(one, 7, -1);
        checkGet(one, 8, 80);

        // Updating an existing key refreshes value and frequency, no eviction.
        LFUCache upd = new LFUCache(2);
        upd.put(1, 1);
        upd.put(2, 2);
        upd.put(1, 100);     // freq(1) -> 2
        checkSize(upd, 2);
        upd.put(3, 3);       // evicts key 2, the cold key, not updated key 1
        checkGet(upd, 1, 100);
        checkGet(upd, 2, -1);

        // Frequency beats recency: a hot old key survives cold newer keys.
        LFUCache hot = new LFUCache(3);
        hot.put(1, 1);
        hot.put(2, 2);
        hot.put(3, 3);
        hot.get(1);
        hot.get(1);          // freq(1) = 3
        hot.get(2);          // freq(2) = 2
        hot.put(4, 4);       // evicts key 3, the only freq-1 key
        checkGet(hot, 3, -1);
        checkGet(hot, 1, 1);
        checkGet(hot, 2, 2);
        checkGet(hot, 4, 4);

        // Pure LRU tie-break inside one frequency: oldest touch evicted first.
        LFUCache tie = new LFUCache(3);
        tie.put(1, 1);
        tie.put(2, 2);
        tie.put(3, 3);       // all freq 1, recency order 1, 2, 3
        tie.put(4, 4);       // evicts 1
        tie.put(5, 5);       // evicts 2
        checkGet(tie, 1, -1);
        checkGet(tie, 2, -1);
        checkGet(tie, 3, 3);
        checkGet(tie, 4, 4);
        checkGet(tie, 5, 5);

        // Randomised cross-check against the scanning oracle.
        Random rng = new Random(460);
        for (int trial = 0; trial < 200; trial++) {
            int cap = rng.nextInt(6);
            LFUCache fast = new LFUCache(cap);
            Oracle slow = new Oracle(cap);
            for (int op = 0; op < 500; op++) {
                int key = rng.nextInt(10);
                if (rng.nextBoolean()) {
                    int expected = slow.get(key);
                    int actual = fast.get(key);
                    if (actual != expected) {
                        throw new AssertionError("trial " + trial + " op " + op + ": get(" + key
                                + ") = " + actual + ", oracle " + expected + " (capacity " + cap + ")");
                    }
                } else {
                    int value = rng.nextInt(1000);
                    fast.put(key, value);
                    slow.put(key, value);
                }
                if (fast.size() != slow.size()) {
                    throw new AssertionError("trial " + trial + " op " + op + ": size " + fast.size()
                            + ", oracle " + slow.size());
                }
            }
        }
        System.out.println("PASS randomised cross-check vs oracle (200 trials x 500 ops)");

        System.out.println("All checks passed.");
    }
}
