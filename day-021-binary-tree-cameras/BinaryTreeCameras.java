import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Day 021 — Binary Tree Cameras (LeetCode 968, Hard).
 *
 * A camera placed on a node monitors that node, its parent, and its
 * immediate children. Return the minimum number of cameras needed to
 * monitor every node of a binary tree.
 *
 * Approach: greedy post-order DFS with three states. Each node reports
 * to its parent one of: NEEDS_COVER (nothing below covers it, so its
 * parent must help), HAS_CAMERA (it carries a camera), or COVERED (a
 * child covers it, so it asks nothing of its parent). The greedy rule
 * pushes cameras as deep as possible: a node places a camera exactly
 * when one of its children NEEDS_COVER, because that camera then also
 * covers the node itself and its parent — strictly more reach than
 * placing the camera at the parent instead. A node with a camera
 * child is COVERED; a node whose children are all merely COVERED
 * still NEEDS_COVER itself. A leaf NEEDS_COVER (it defers to its
 * parent, which is always at least as good a camera site), and a root
 * that still NEEDS_COVER after the traversal gets one final camera.
 * Each node is visited once: O(n) time, O(h) stack space.
 *
 * main cross-checks the greedy against an exhaustive brute-force
 * oracle (every camera subset, minimum covering set) on thousands of
 * small random trees, so the greedy's optimality is verified, not
 * assumed.
 */
public class BinaryTreeCameras {

    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int val) {
            this.val = val;
        }
    }

    private static final int NEEDS_COVER = 0;
    private static final int HAS_CAMERA = 1;
    private static final int COVERED = 2;

    private int cameras;

    // ---- Primary: greedy post-order state propagation. ----

    public int minCameraCover(TreeNode root) {
        cameras = 0;
        if (dfs(root) == NEEDS_COVER) {
            cameras++;
        }
        return cameras;
    }

    private int dfs(TreeNode node) {
        if (node == null) {
            return COVERED;
        }
        if (node.left == null && node.right == null) {
            return NEEDS_COVER;
        }
        int left = dfs(node.left);
        int right = dfs(node.right);
        if (left == NEEDS_COVER || right == NEEDS_COVER) {
            cameras++;
            return HAS_CAMERA;
        }
        if (left == HAS_CAMERA || right == HAS_CAMERA) {
            return COVERED;
        }
        return NEEDS_COVER;
    }

    // ---- Oracle (test only): exhaustive minimum covering subset. ----

    private static int bruteForce(TreeNode root) {
        List<TreeNode> nodes = new ArrayList<>();
        collect(root, nodes);
        int n = nodes.size();
        // covers[i] = bitmask of nodes monitored by a camera on nodes[i]:
        // itself, its parent, and its children.
        int[] covers = new int[n];
        for (int i = 0; i < n; i++) {
            TreeNode node = nodes.get(i);
            int mask = 1 << i;
            for (int j = 0; j < n; j++) {
                TreeNode other = nodes.get(j);
                if (other.left == node || other.right == node
                        || node.left == other || node.right == other) {
                    mask |= 1 << j;
                }
            }
            covers[i] = mask;
        }
        int full = (1 << n) - 1;
        int best = Integer.MAX_VALUE;
        for (int subset = 0; subset < (1 << n); subset++) {
            int bits = Integer.bitCount(subset);
            if (bits >= best) {
                continue;
            }
            int covered = 0;
            for (int i = 0; i < n; i++) {
                if ((subset & (1 << i)) != 0) {
                    covered |= covers[i];
                }
            }
            if (covered == full) {
                best = bits;
            }
        }
        return best;
    }

    private static void collect(TreeNode node, List<TreeNode> out) {
        if (node == null) {
            return;
        }
        out.add(node);
        collect(node.left, out);
        collect(node.right, out);
    }

    // ---- Test helpers. ----

    /** Builds a tree from a level-order array; null marks a missing node. */
    private static TreeNode build(Integer[] levelOrder) {
        if (levelOrder == null || levelOrder.length == 0 || levelOrder[0] == null) {
            return null;
        }
        TreeNode root = new TreeNode(levelOrder[0]);
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);
        int i = 1;
        while (!queue.isEmpty() && i < levelOrder.length) {
            TreeNode node = queue.poll();
            if (i < levelOrder.length && levelOrder[i] != null) {
                node.left = new TreeNode(levelOrder[i]);
                queue.offer(node.left);
            }
            i++;
            if (i < levelOrder.length && levelOrder[i] != null) {
                node.right = new TreeNode(levelOrder[i]);
                queue.offer(node.right);
            }
            i++;
        }
        return root;
    }

    /** Builds a random tree with exactly n nodes by random attachment. */
    private static TreeNode randomTree(int n, Random rng) {
        TreeNode root = new TreeNode(0);
        List<TreeNode> nodes = new ArrayList<>();
        nodes.add(root);
        for (int v = 1; v < n; v++) {
            TreeNode child = new TreeNode(v);
            // Retry until a parent with a free child slot is found.
            while (true) {
                TreeNode parent = nodes.get(rng.nextInt(nodes.size()));
                if (rng.nextBoolean()) {
                    if (parent.left == null) {
                        parent.left = child;
                        break;
                    }
                } else if (parent.right == null) {
                    parent.right = child;
                    break;
                }
            }
            nodes.add(child);
        }
        return root;
    }

    private static void check(Integer[] levelOrder, int expected) {
        TreeNode root = build(levelOrder);
        int actual = new BinaryTreeCameras().minCameraCover(root);
        if (actual != expected) {
            throw new AssertionError("minCameraCover(" + java.util.Arrays.toString(levelOrder)
                    + ") = " + actual + ", expected " + expected);
        }
        System.out.println("PASS minCameraCover(" + java.util.Arrays.toString(levelOrder)
                + ") = " + actual);
    }

    public static void main(String[] args) {
        check(new Integer[] { 0 }, 1);
        check(new Integer[] { 0, 0 }, 1);
        check(new Integer[] { 0, 0, 0 }, 1);
        check(new Integer[] { 0, 0, null, 0 }, 1);
        check(new Integer[] { 0, 0, null, 0, 0 }, 1);
        check(new Integer[] { 0, 0, null, 0, null, 0, null, null, 0 }, 2);
        check(new Integer[] { 0, 0, 0, 0, 0, 0, 0 }, 2);
        check(new Integer[] { 0, null, 0, null, 0, null, 0 }, 2);
        check(new Integer[] { 0, 0, 0, null, null, 0, 0 }, 2);

        // Randomised cross-check: greedy must match the exhaustive
        // optimum on every small tree, not just the fixed cases.
        Random rng = new Random(968L);
        int trials = 2_000;
        for (int t = 0; t < trials; t++) {
            int n = 1 + rng.nextInt(10);
            TreeNode root = randomTree(n, rng);
            int greedy = new BinaryTreeCameras().minCameraCover(root);
            int optimal = bruteForce(root);
            if (greedy != optimal) {
                throw new AssertionError("Random trial " + t + " (n=" + n
                        + "): greedy = " + greedy + ", optimal = " + optimal);
            }
        }
        System.out.println("PASS randomised cross-check vs brute force (" + trials + " trees)");
        System.out.println("All checks passed.");
    }
}
