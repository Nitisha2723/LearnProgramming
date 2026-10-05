import java.util.*;

/**
 * PROBLEM: Binary Tree Level Order Traversal
 * Given the root of a binary tree, return the level-order traversal
 * of its nodes' values (i.e., from left to right, level by level).
 *
 * Example:
 *       3
 *      / \
 *     9  20
 *       /  \
 *      15   7
 *
 * Output: [[3], [9, 20], [15, 7]]
 *
 * APPROACH: BFS with a Queue
 *   BFS naturally processes nodes level by level.
 *   The key technique: at the start of each level, the queue contains
 *   EXACTLY all nodes for that level. Process them all, adding their
 *   children for the next level.
 *
 * Algorithm:
 *   1. Initialize queue with root
 *   2. While queue is not empty:
 *      a. Get the current size of the queue (= number of nodes at this level)
 *      b. Process exactly `size` nodes from the queue → that's one level
 *      c. Add their children to the queue (for the next level)
 *      d. Add the collected level values to the result
 *
 * WHY BFS (not DFS)?
 *   DFS (depth-first) would go deep into one branch before visiting siblings.
 *   BFS explores all nodes at depth d before exploring depth d+1,
 *   which is exactly what level-order traversal requires.
 *
 * TIME:  O(n) — each node is enqueued and dequeued exactly once
 * SPACE: O(n) — the queue holds at most n/2 nodes (the widest level)
 *               in the worst case (a complete binary tree's last level)
 */
public class BinaryTreeLevelOrder {

    // -----------------------------------------------------------------------
    // Inner class: TreeNode
    // -----------------------------------------------------------------------
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    // -----------------------------------------------------------------------
    // Solution: BFS level order traversal
    // -----------------------------------------------------------------------
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();

        if (root == null) return result; // Empty tree

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root); // Start with root

        while (!queue.isEmpty()) {
            // Number of nodes at the current level
            int levelSize = queue.size();
            List<Integer> currentLevel = new ArrayList<>();

            // Process all nodes at this level
            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                currentLevel.add(node.val);

                // Add children for the next level
                if (node.left  != null) queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
            }

            result.add(currentLevel);
        }

        return result;
    }

    // -----------------------------------------------------------------------
    // Variant 1: Level Order from Bottom (LeetCode 107)
    // Return the bottom-up level order traversal.
    // -----------------------------------------------------------------------
    public List<List<Integer>> levelOrderBottom(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<Integer> currentLevel = new ArrayList<>();

            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                currentLevel.add(node.val);
                if (node.left  != null) queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
            }

            result.add(0, currentLevel); // Prepend instead of append → bottom-up
        }

        return result;
    }

    // -----------------------------------------------------------------------
    // Variant 2: Zigzag Level Order (LeetCode 103)
    // Alternate left-to-right and right-to-left by level.
    // -----------------------------------------------------------------------
    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        boolean leftToRight = true;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            LinkedList<Integer> currentLevel = new LinkedList<>();

            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();

                // Add to front or back depending on direction
                if (leftToRight) {
                    currentLevel.addLast(node.val);
                } else {
                    currentLevel.addFirst(node.val);
                }

                if (node.left  != null) queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
            }

            result.add(currentLevel);
            leftToRight = !leftToRight; // Toggle direction
        }

        return result;
    }

    // -----------------------------------------------------------------------
    // Variant 3: Recursive DFS level order (alternative to BFS)
    // Less intuitive for level order, but shows it's possible.
    // -----------------------------------------------------------------------
    public List<List<Integer>> levelOrderRecursive(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        dfs(root, 0, result);
        return result;
    }

    private void dfs(TreeNode node, int level, List<List<Integer>> result) {
        if (node == null) return;

        // If this is the first node we've seen at this level, create the list
        if (level == result.size()) {
            result.add(new ArrayList<>());
        }

        result.get(level).add(node.val);

        dfs(node.left,  level + 1, result);
        dfs(node.right, level + 1, result);
    }

    // -----------------------------------------------------------------------
    // Helper: Build tree from array (LeetCode format, null = no node)
    // -----------------------------------------------------------------------
    private static TreeNode buildTree(Integer[] values) {
        if (values == null || values.length == 0 || values[0] == null) return null;

        TreeNode root = new TreeNode(values[0]);
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        int i = 1;

        while (!queue.isEmpty() && i < values.length) {
            TreeNode node = queue.poll();

            if (i < values.length && values[i] != null) {
                node.left = new TreeNode(values[i]);
                queue.offer(node.left);
            }
            i++;

            if (i < values.length && values[i] != null) {
                node.right = new TreeNode(values[i]);
                queue.offer(node.right);
            }
            i++;
        }

        return root;
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        BinaryTreeLevelOrder solution = new BinaryTreeLevelOrder();

        System.out.println("=== Binary Tree Level Order Traversal ===");
        System.out.println();

        // Test 1: [3, 9, 20, null, null, 15, 7]
        //       3
        //      / \
        //     9  20
        //       /  \
        //      15   7
        TreeNode tree1 = buildTree(new Integer[]{3, 9, 20, null, null, 15, 7});
        System.out.println("Tree 1: [3, 9, 20, null, null, 15, 7]");
        System.out.println("  Level order:         " + solution.levelOrder(tree1));
        System.out.println("  Expected:            [[3], [9, 20], [15, 7]]");
        System.out.println("  Level order bottom:  " + solution.levelOrderBottom(tree1));
        System.out.println("  Expected:            [[15, 7], [9, 20], [3]]");
        System.out.println("  Zigzag level order:  " + solution.zigzagLevelOrder(tree1));
        System.out.println("  Expected:            [[3], [20, 9], [15, 7]]");
        System.out.println();

        // Test 2: Single node
        TreeNode tree2 = new TreeNode(1);
        System.out.println("Tree 2: [1] (single node)");
        System.out.println("  Level order: " + solution.levelOrder(tree2));
        System.out.println("  Expected:    [[1]]");
        System.out.println();

        // Test 3: Empty tree
        System.out.println("Tree 3: null (empty)");
        System.out.println("  Level order: " + solution.levelOrder(null));
        System.out.println("  Expected:    []");
        System.out.println();

        // Test 4: Perfect binary tree
        //         1
        //        / \
        //       2   3
        //      / \ / \
        //     4  5 6  7
        TreeNode tree4 = buildTree(new Integer[]{1, 2, 3, 4, 5, 6, 7});
        System.out.println("Tree 4: perfect binary tree [1,2,3,4,5,6,7]");
        System.out.println("  Level order: " + solution.levelOrder(tree4));
        System.out.println("  Expected:    [[1], [2, 3], [4, 5, 6, 7]]");
        System.out.println("  Recursive:   " + solution.levelOrderRecursive(tree4));
        System.out.println();

        // Test 5: Left-skewed tree
        TreeNode tree5 = new TreeNode(1, new TreeNode(2, new TreeNode(3), null), null);
        System.out.println("Tree 5: left-skewed [1,2,3]");
        System.out.println("  Level order: " + solution.levelOrder(tree5));
        System.out.println("  Expected:    [[1], [2], [3]]");

        System.out.println();
        System.out.println("=== Trace: levelOrder([3, 9, 20, null, null, 15, 7]) ===");
        System.out.println("Queue: [3]");
        System.out.println("Level 0: process size=1: poll 3, add children 9,20 → level=[3]");
        System.out.println("Queue: [9, 20]");
        System.out.println("Level 1: process size=2: poll 9 (no children), poll 20 (add 15,21) → level=[9,20]");
        System.out.println("Queue: [15, 7]");
        System.out.println("Level 2: process size=2: poll 15 (no children), poll 7 (no children) → level=[15,7]");
        System.out.println("Queue: [] — done");
        System.out.println("Result: [[3], [9, 20], [15, 7]]");
    }
}
