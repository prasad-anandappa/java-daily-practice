import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Day 020 — Merge k Sorted Lists (LeetCode 23, Hard).
 *
 * Given an array of k linked lists, each sorted in non-decreasing
 * order, merge all of them into one sorted linked list and return
 * its head.
 *
 * Approach: divide-and-conquer pairwise merging. Merging two sorted
 * lists is linear in their combined length, so the only question is
 * how the pairwise merges are scheduled. Merging the lists one at a
 * time into a growing accumulator costs O(k * N) — the accumulator
 * is rescanned for every new list. Pairing lists up instead (merge
 * lists 0 with 1, 2 with 3, and so on, then repeat on the halved
 * set) means each node moves through O(log k) merges, for
 * O(N log k) time overall, where N is the total number of nodes.
 * The merge itself reuses the existing nodes by rewiring next
 * pointers behind a dummy head; no values are copied into an array.
 *
 * A min-heap variant is kept as an independent oracle: push each
 * list's head, repeatedly poll the smallest head, append it, and
 * push its successor. It is also O(N log k) but with O(k) heap
 * space and a larger constant. main cross-checks both against a
 * brute-force flatten-and-sort oracle on randomised inputs.
 */
public class MergeKSortedLists {

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    // ---- Primary: divide-and-conquer pairwise merging. ----

    public static ListNode mergeKLists(ListNode[] lists) {
        if (lists == null || lists.length == 0) {
            return null;
        }
        return mergeRange(lists, 0, lists.length - 1);
    }

    private static ListNode mergeRange(ListNode[] lists, int lo, int hi) {
        if (lo == hi) {
            return lists[lo];
        }
        if (lo > hi) {
            return null;
        }
        int mid = lo + (hi - lo) / 2;
        ListNode left = mergeRange(lists, lo, mid);
        ListNode right = mergeRange(lists, mid + 1, hi);
        return mergeTwo(left, right);
    }

    private static ListNode mergeTwo(ListNode a, ListNode b) {
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) {
                tail.next = a;
                a = a.next;
            } else {
                tail.next = b;
                b = b.next;
            }
            tail = tail.next;
        }
        tail.next = (a != null) ? a : b;
        return dummy.next;
    }

    // ---- Oracle 1 (test only): min-heap over the current heads. ----

    private static ListNode mergeKListsHeap(ListNode[] lists) {
        if (lists == null || lists.length == 0) {
            return null;
        }
        PriorityQueue<ListNode> heap =
                new PriorityQueue<>(Comparator.comparingInt((ListNode n) -> n.val));
        for (ListNode head : lists) {
            if (head != null) {
                heap.offer(head);
            }
        }
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        while (!heap.isEmpty()) {
            ListNode smallest = heap.poll();
            tail.next = new ListNode(smallest.val);
            tail = tail.next;
            if (smallest.next != null) {
                heap.offer(smallest.next);
            }
        }
        return dummy.next;
    }

    // ---- Oracle 2 (test only): flatten, sort, rebuild. ----

    private static int[] bruteForce(int[][] lists) {
        List<Integer> all = new ArrayList<>();
        for (int[] list : lists) {
            for (int v : list) {
                all.add(v);
            }
        }
        int[] result = new int[all.size()];
        for (int i = 0; i < all.size(); i++) {
            result[i] = all.get(i);
        }
        Arrays.sort(result);
        return result;
    }

    // ---- Test helpers. ----

    private static ListNode build(int[] values) {
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        for (int v : values) {
            tail.next = new ListNode(v);
            tail = tail.next;
        }
        return dummy.next;
    }

    private static ListNode[] buildAll(int[][] lists) {
        ListNode[] heads = new ListNode[lists.length];
        for (int i = 0; i < lists.length; i++) {
            heads[i] = build(lists[i]);
        }
        return heads;
    }

    private static int[] toArray(ListNode head) {
        List<Integer> out = new ArrayList<>();
        for (ListNode n = head; n != null; n = n.next) {
            out.add(n.val);
        }
        int[] result = new int[out.size()];
        for (int i = 0; i < out.size(); i++) {
            result[i] = out.get(i);
        }
        return result;
    }

    private static String describe(int[][] lists) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lists.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(lists[i]));
        }
        return sb.append("]").toString();
    }

    private static void check(int[][] lists, int[] expected) {
        int[] actual = toArray(mergeKLists(buildAll(lists)));
        if (!Arrays.equals(actual, expected)) {
            throw new AssertionError("mergeKLists(" + describe(lists) + ") = "
                    + Arrays.toString(actual) + ", expected " + Arrays.toString(expected));
        }
        int[] heapActual = toArray(mergeKListsHeap(buildAll(lists)));
        if (!Arrays.equals(heapActual, expected)) {
            throw new AssertionError("heap oracle disagrees on " + describe(lists)
                    + ": heap=" + Arrays.toString(heapActual)
                    + ", expected " + Arrays.toString(expected));
        }
        int[] oracle = bruteForce(lists);
        if (!Arrays.equals(oracle, expected)) {
            throw new AssertionError("brute-force oracle disagrees on " + describe(lists)
                    + ": oracle=" + Arrays.toString(oracle)
                    + ", expected " + Arrays.toString(expected));
        }
        System.out.println("PASS mergeKLists(" + describe(lists) + ") = "
                + Arrays.toString(actual));
    }

    public static void main(String[] args) {
        check(new int[][] { { 1, 4, 5 }, { 1, 3, 4 }, { 2, 6 } },
                new int[] { 1, 1, 2, 3, 4, 4, 5, 6 });
        check(new int[][] {}, new int[] {});
        check(new int[][] { {} }, new int[] {});
        check(new int[][] { {}, {}, {} }, new int[] {});
        check(new int[][] { { 1, 2, 3 } }, new int[] { 1, 2, 3 });
        check(new int[][] { { 5 } }, new int[] { 5 });
        check(new int[][] { { 2 }, { 1 } }, new int[] { 1, 2 });
        check(new int[][] { { 1 }, { 1 }, { 1 }, { 1 } },
                new int[] { 1, 1, 1, 1 });
        check(new int[][] { { -5, -1, 0 }, { -3, 2 }, { -4 } },
                new int[] { -5, -4, -3, -1, 0, 2 });
        check(new int[][] { { 1, 3, 5, 7 }, { 2, 4, 6, 8 }, { 0, 9, 10, 11 } },
                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });
        check(new int[][] { {}, { 1 }, {}, { 2, 3 }, {} },
                new int[] { 1, 2, 3 });
        check(new int[][] { { 10, 20, 30 }, { 5, 15, 25 }, { 1, 2, 3 }, { 100 } },
                new int[] { 1, 2, 3, 5, 10, 15, 20, 25, 30, 100 });
        // Odd k exercises the unpaired branch of the divide-and-conquer.
        check(new int[][] { { 1, 6 }, { 2, 5 }, { 3, 4 }, { 0, 7 }, { 8 } },
                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 });
        // Duplicates across every list.
        check(new int[][] { { 1, 1, 2 }, { 1, 2, 2 }, { 2, 2, 2 } },
                new int[] { 1, 1, 1, 2, 2, 2, 2, 2, 2 });
        // Extreme values: comparisons only, no arithmetic on values.
        check(new int[][] { { Integer.MIN_VALUE, 0 }, { -1, Integer.MAX_VALUE } },
                new int[] { Integer.MIN_VALUE, -1, 0, Integer.MAX_VALUE });

        // Null array and all-null entries return null.
        if (mergeKLists(null) != null) {
            throw new AssertionError("mergeKLists(null) should be null");
        }
        System.out.println("PASS mergeKLists(null) = null");
        if (mergeKLists(new ListNode[] { null, null }) != null) {
            throw new AssertionError("mergeKLists([null, null]) should be null");
        }
        System.out.println("PASS mergeKLists([null, null]) = null");

        // Randomised cross-check against both oracles: varied k,
        // varied lengths (including empty lists), duplicates and
        // negatives throughout.
        Random rng = new Random(23);
        for (int trial = 0; trial < 1000; trial++) {
            int k = rng.nextInt(9);
            int[][] lists = new int[k][];
            for (int i = 0; i < k; i++) {
                int len = rng.nextInt(11);
                lists[i] = new int[len];
                for (int j = 0; j < len; j++) {
                    lists[i][j] = rng.nextInt(41) - 20;
                }
                Arrays.sort(lists[i]);
            }
            int[] fast = toArray(mergeKLists(buildAll(lists)));
            int[] slow = bruteForce(lists);
            if (!Arrays.equals(fast, slow)) {
                throw new AssertionError("mismatch on " + describe(lists)
                        + ": divide-and-conquer=" + Arrays.toString(fast)
                        + ", oracle=" + Arrays.toString(slow));
            }
            int[] heap = toArray(mergeKListsHeap(buildAll(lists)));
            if (!Arrays.equals(heap, slow)) {
                throw new AssertionError("heap mismatch on " + describe(lists)
                        + ": heap=" + Arrays.toString(heap)
                        + ", oracle=" + Arrays.toString(slow));
            }
        }
        System.out.println("PASS randomised cross-check vs heap + brute-force oracles (1000 trials)");
        System.out.println("All checks passed.");
    }
}
