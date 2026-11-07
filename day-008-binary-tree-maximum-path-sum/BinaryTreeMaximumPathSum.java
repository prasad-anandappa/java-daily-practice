import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * Day 008 — Binary Tree Maximum Path Sum (LeetCode 124, Hard).
 *
 * A path in a binary tree is any sequence of nodes connected by edges,
 * where each node appears at most once; the path need not pass through
 * the root and need not include any leaf. Return the maximum sum of
 * node values over all possible paths. The tree has at least one node;
 * values may all be negative, in which case the answer is the single
 * least-negative node.
 *
 * Approach: one post-order DFS computes two different things at every
 * node, and keeping them separate is the whole problem. The value
 * RETURNED upward is the best downward gain: this node's value plus the
 * better of its two child gains, with a negative child gain clamped to
 * 0 (dropping that branch entirely is always at least as good). The
 * value RECORDED globally is the best path that bends through this
 * node: value + left gain + right gain, using both clamped gains.
 * A path can bend at most once, so the bend candidate is never returned
 * upward — only the straight downward gain is, which is exactly what a
 * parent needs to extend a path through this node. Each node is visited
 * once, so the pass is O(n) time and O(h) stack space for tree height h.
 */
public class BinaryTreeMaximumPathSum {

    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int val) {
            this.val = val;
        }
    }

    public static int maxPathSum(TreeNode root) {
        if (root == null) {
            throw new IllegalArgumentException("Tree must contain at least one node");
        }
        int[] best = { Integer.MIN_VALUE };
        downwardGain(root, best);
        return best[0];
    }

    // Returns the best sum of a path starting at node and going only
    // downward; updates best[0] with the best path bending through node.
    private static int downwardGain(TreeNode node, int[] best) {
        if (node == null) {
            return 0;
        }
        int leftGain = Math.max(downwardGain(node.left, best), 0);
        int rightGain = Math.max(downwardGain(node.right, best), 0);

        // Best path whose highest node is this one (may use both sides).
        best[0] = Math.max(best[0], node.val + leftGain + rightGain);

        // Only one side can be extended upward without bending twice.
        return node.val + Math.max(leftGain, rightGain);
    }

    // Builds a tree from a level-order array; null marks a missing node.
    private static TreeNode build(Integer[] levelOrder) {
        if (levelOrder.length == 0 || levelOrder[0] == null) {
            return null;
        }
        TreeNode root = new TreeNode(levelOrder[0]);
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        int i = 1;
        while (!queue.isEmpty() && i < levelOrder.length) {
            TreeNode node = queue.poll();
            if (i < levelOrder.length && levelOrder[i] != null) {
                node.left = new TreeNode(levelOrder[i]);
                queue.add(node.left);
            }
            i++;
            if (i < levelOrder.length && levelOrder[i] != null) {
                node.right = new TreeNode(levelOrder[i]);
                queue.add(node.right);
            }
            i++;
        }
        return root;
    }

    private static void check(Integer[] levelOrder, int expected) {
        int actual = maxPathSum(build(levelOrder));
        if (actual != expected) {
            throw new AssertionError("maxPathSum(" + Arrays.toString(levelOrder)
                    + ") = " + actual + ", expected " + expected);
        }
        System.out.println("PASS maxPathSum(" + Arrays.toString(levelOrder)
                + ") = " + actual);
    }

    public static void main(String[] args) {
        check(new Integer[] { 1, 2, 3 }, 6);
        check(new Integer[] { -10, 9, 20, null, null, 15, 7 }, 42);
        check(new Integer[] { -3 }, -3);
        check(new Integer[] { 2, -1 }, 2);
        check(new Integer[] { -2, -1, -3 }, -1);
        check(new Integer[] { 1, -2, 3 }, 4);
        check(new Integer[] { 5, 4, 8, 11, null, 13, 4, 7, 2, null, null, null, 1 }, 48);
        check(new Integer[] { 9, 6, -3, null, null, -6, 2, null, null, 2 }, 16);
        check(new Integer[] { -1, -2, 10, null, null, -5, -3, null, null, 10, 10 }, 17);
        // Right-skewed chain: the best path is the whole chain.
        check(new Integer[] { 1, null, 2, null, 3, null, 4 }, 10);
        // Repeated calls must not leak the global best between runs.
        check(new Integer[] { 1, 2, 3 }, 6);
        check(new Integer[] { -5 }, -5);

        boolean threw = false;
        try {
            maxPathSum(null);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        if (!threw) {
            throw new AssertionError("maxPathSum(null) should throw IllegalArgumentException");
        }
        System.out.println("PASS maxPathSum(null) throws IllegalArgumentException");
        System.out.println("All checks passed.");
    }
}
