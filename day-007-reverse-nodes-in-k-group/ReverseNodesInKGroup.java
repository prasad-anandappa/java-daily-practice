import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Day 007 — Reverse Nodes in k-Group (LeetCode 25, Hard).
 *
 * Given the head of a singly linked list, reverse the nodes k at a time
 * and return the modified head. Nodes in a final group with fewer than k
 * nodes keep their original order. The nodes themselves must be reversed
 * by rewiring next pointers — values may not be swapped, and no array
 * copy of the list is allowed as the mechanism.
 *
 * Approach: a dummy node before the head makes the first group behave
 * like every other group. Keep groupPrev pointing at the node just
 * before the current group. First walk k steps from groupPrev to find
 * the group's kth node; if the walk falls off the end, the leftover
 * group is incomplete and is left untouched. Otherwise reverse the
 * k nodes in place with the classic three-pointer walk, bounded by the
 * node after the group so the tail splices back correctly. Then splice:
 * groupPrev.next becomes the old kth node (the group's new head), and
 * the old first node — now the group's tail — becomes groupPrev for
 * the next iteration. Each node is visited a constant number of times,
 * so the whole pass is O(n) time and O(1) extra space.
 */
public class ReverseNodesInKGroup {

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    public static ListNode reverseKGroup(ListNode head, int k) {
        if (head == null || k <= 1) {
            return head;
        }
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode groupPrev = dummy;

        while (true) {
            // Find the kth node of the current group.
            ListNode kth = groupPrev;
            for (int i = 0; i < k && kth != null; i++) {
                kth = kth.next;
            }
            if (kth == null) {
                break; // fewer than k nodes left — leave them as they are
            }
            ListNode groupNext = kth.next;

            // Reverse the group [groupPrev.next .. kth] in place.
            ListNode prev = groupNext;
            ListNode curr = groupPrev.next;
            while (curr != groupNext) {
                ListNode next = curr.next;
                curr.next = prev;
                prev = curr;
                curr = next;
            }

            // Splice the reversed group back in; its old head is now its tail.
            ListNode groupTail = groupPrev.next;
            groupPrev.next = kth;
            groupPrev = groupTail;
        }
        return dummy.next;
    }

    private static ListNode build(int[] values) {
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        for (int v : values) {
            tail.next = new ListNode(v);
            tail = tail.next;
        }
        return dummy.next;
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

    private static void check(int[] input, int k, int[] expected) {
        int[] actual = toArray(reverseKGroup(build(input), k));
        if (!Arrays.equals(actual, expected)) {
            throw new AssertionError("reverseKGroup(" + Arrays.toString(input) + ", k=" + k
                    + ") = " + Arrays.toString(actual)
                    + ", expected " + Arrays.toString(expected));
        }
        System.out.println("PASS reverseKGroup(" + Arrays.toString(input) + ", k=" + k
                + ") = " + Arrays.toString(actual));
    }

    public static void main(String[] args) {
        check(new int[] { 1, 2, 3, 4, 5 }, 2, new int[] { 2, 1, 4, 3, 5 });
        check(new int[] { 1, 2, 3, 4, 5 }, 3, new int[] { 3, 2, 1, 4, 5 });
        check(new int[] { 1, 2, 3, 4, 5 }, 1, new int[] { 1, 2, 3, 4, 5 });
        check(new int[] { 1, 2, 3, 4, 5 }, 5, new int[] { 5, 4, 3, 2, 1 });
        check(new int[] { 1, 2, 3, 4, 5, 6 }, 3, new int[] { 3, 2, 1, 6, 5, 4 });
        check(new int[] { 1, 2, 3, 4 }, 4, new int[] { 4, 3, 2, 1 });
        check(new int[] { 1, 2 }, 3, new int[] { 1, 2 });
        check(new int[] { 1 }, 2, new int[] { 1 });
        check(new int[] {}, 3, new int[] {});
        check(new int[] { 1, 2, 3, 4, 5, 6, 7, 8 }, 4, new int[] { 4, 3, 2, 1, 8, 7, 6, 5 });
        System.out.println("All checks passed.");
    }
}
