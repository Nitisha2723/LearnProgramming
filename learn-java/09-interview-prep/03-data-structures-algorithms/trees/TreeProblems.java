import java.util.*;

/**
 * Solutions to 10 classic tree interview problems.
 * Uses a static inner class TreeNode to represent a binary tree node.
 *
 * Problems covered:
 *  1.  Maximum Depth of Binary Tree       — recursive DFS
 *  2.  Validate BST                       — range check
 *  3.  Inorder Traversal (iterative)      — explicit stack
 *  4.  Level Order Traversal (BFS)        — Queue
 *  5.  Binary Tree Paths                  — DFS backtracking
 *  6.  Lowest Common Ancestor of BST      — BST property
 *  7.  Symmetric Tree                     — recursive and iterative
 *  8.  Count Complete Tree Nodes          — binary search on levels
 *  9.  Serialize and Deserialize          — pre-order with null markers
 * 10.  Kth Smallest Element in BST        — iterative in-order
 */
public class TreeProblems {

    // =========================================================================
    // Inner Class: TreeNode
    // =========================================================================

    /**
     * A node in a binary tree.
     */
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

    // =========================================================================
    // Helper: Build tree from level-order array (null = missing node)
    // =========================================================================

    /**
     * Build a binary tree from level-order (BFS) array representation.
     * Integer.MIN_VALUE in the array represents a null node.
     */
    static TreeNode buildTree(Integer... values) {
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

    // =========================================================================
    // Problem 1: Maximum Depth of Binary Tree
    // =========================================================================

    /**
     * Return the maximum depth (height) of a binary tree.
     *
     * Recursive DFS:
     *   The depth of a tree rooted at node is 1 + max(depth(left), depth(right)).
     *   Base case: null node has depth 0.
     *
     * Time:  O(n) — every node visited once
     * Space: O(h) — call stack depth equals tree height
     *
     * @param root root of the binary tree
     * @return maximum depth
     */
    public int maxDepth(TreeNode root) {
        if (root == null) return 0;
        return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
    }

    // =========================================================================
    // Problem 2: Validate BST
    // =========================================================================

    /**
     * Determine if a binary tree is a valid BST.
     *
     * Range Check:
     *   Pass a valid (min, max) range down the recursion.
     *   Each node must satisfy min < node.val < max.
     *   Left subtree: upper bound becomes parent's value.
     *   Right subtree: lower bound becomes parent's value.
     *
     *   Uses Long to handle Integer.MIN_VALUE and Integer.MAX_VALUE edge cases.
     *
     * Time:  O(n)
     * Space: O(h)
     *
     * @param root root of the tree
     * @return true if the tree is a valid BST
     */
    public boolean isValidBST(TreeNode root) {
        return isValidBSTHelper(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    private boolean isValidBSTHelper(TreeNode node, long min, long max) {
        if (node == null) return true;

        // Node value must be strictly within (min, max)
        if (node.val <= min || node.val >= max) return false;

        // Left child must be < current node (new max = node.val)
        // Right child must be > current node (new min = node.val)
        return isValidBSTHelper(node.left, min, node.val)
            && isValidBSTHelper(node.right, node.val, max);
    }

    // =========================================================================
    // Problem 3: Binary Tree Inorder Traversal (Iterative)
    // =========================================================================

    /**
     * Perform in-order traversal (left, root, right) iteratively using an explicit stack.
     *
     * Algorithm:
     *   current = root
     *   while current != null OR stack not empty:
     *     push current and go left until null
     *     pop: this is the next in-order node — add to result
     *     move to right subtree
     *
     * Time:  O(n)
     * Space: O(h) for the stack
     *
     * @param root root of the tree
     * @return in-order traversal as a list
     */
    public List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode current = root;

        while (current != null || !stack.isEmpty()) {
            // Go as far left as possible, pushing nodes onto the stack
            while (current != null) {
                stack.push(current);
                current = current.left;
            }

            // Process the leftmost unvisited node
            current = stack.pop();
            result.add(current.val);

            // Now visit the right subtree
            current = current.right;
        }

        return result;
    }

    // =========================================================================
    // Problem 4: Level Order Traversal (BFS)
    // =========================================================================

    /**
     * Return level-order (BFS) traversal as a list of lists, one per level.
     *
     * Queue-based BFS:
     *   Before processing each level, record the queue size — this is how many
     *   nodes are on the current level. Process exactly that many nodes, adding
     *   their children for the next level.
     *
     * Time:  O(n)
     * Space: O(w) where w is the maximum width of the tree
     *
     * @param root root of the tree
     * @return list of levels, each level is a list of node values
     */
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size(); // number of nodes at this level
            List<Integer> level = new ArrayList<>();

            for (int i = 0; i < levelSize; i++) {
                TreeNode node = queue.poll();
                level.add(node.val);

                if (node.left != null)  queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
            }

            result.add(level);
        }

        return result;
    }

    // =========================================================================
    // Problem 5: Binary Tree Paths (Backtracking DFS)
    // =========================================================================

    /**
     * Return all root-to-leaf paths as strings in the form "1->2->5".
     *
     * DFS Backtracking:
     *   Maintain the current path as a list. At each leaf, convert to string and save.
     *   After recursing into a child, remove the child from the path (backtrack).
     *
     * Time:  O(n * h) — n nodes, each path can be up to h long
     * Space: O(h) recursion stack + O(n * h) for the output
     *
     * @param root root of the tree
     * @return all root-to-leaf path strings
     */
    public List<String> binaryTreePaths(TreeNode root) {
        List<String> paths = new ArrayList<>();
        if (root == null) return paths;
        dfsPathHelper(root, new ArrayList<>(), paths);
        return paths;
    }

    private void dfsPathHelper(TreeNode node, List<Integer> currentPath, List<String> paths) {
        currentPath.add(node.val);

        // Leaf node: record the completed path
        if (node.left == null && node.right == null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentPath.size(); i++) {
                sb.append(currentPath.get(i));
                if (i < currentPath.size() - 1) sb.append("->");
            }
            paths.add(sb.toString());
        } else {
            // Recurse into children
            if (node.left  != null) dfsPathHelper(node.left,  currentPath, paths);
            if (node.right != null) dfsPathHelper(node.right, currentPath, paths);
        }

        // Backtrack: remove this node from the path
        currentPath.remove(currentPath.size() - 1);
    }

    // =========================================================================
    // Problem 6: Lowest Common Ancestor of BST
    // =========================================================================

    /**
     * Find the lowest common ancestor (LCA) of two nodes in a BST.
     *
     * BST Property:
     *   If both p and q are smaller than root: LCA is in the left subtree.
     *   If both are larger: LCA is in the right subtree.
     *   Otherwise: root is the LCA (it's the split point, or equals one of them).
     *
     * Time:  O(h) — O(log n) for balanced BST
     * Space: O(1) iterative
     *
     * @param root root of the BST
     * @param p    first node
     * @param q    second node
     * @return lowest common ancestor
     */
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        TreeNode current = root;

        while (current != null) {
            if (p.val < current.val && q.val < current.val) {
                // Both nodes are in the left subtree
                current = current.left;
            } else if (p.val > current.val && q.val > current.val) {
                // Both nodes are in the right subtree
                current = current.right;
            } else {
                // Split point: current is the LCA
                // This also handles the case where current.val == p.val or q.val
                return current;
            }
        }

        return null; // unreachable if p and q exist in the BST
    }

    // =========================================================================
    // Problem 7: Symmetric Tree
    // =========================================================================

    /**
     * Determine if a binary tree is a mirror of itself (symmetric).
     *
     * Recursive approach:
     *   A tree is symmetric if its root's left and right subtrees are mirrors.
     *   Two trees are mirrors if:
     *   1. Both roots have the same value
     *   2. Left's left subtree mirrors Right's right subtree
     *   3. Left's right subtree mirrors Right's left subtree
     *
     * Time:  O(n)
     * Space: O(h) call stack
     *
     * @param root root of the tree
     * @return true if the tree is symmetric
     */
    public boolean isSymmetric(TreeNode root) {
        if (root == null) return true;
        return isMirror(root.left, root.right);
    }

    private boolean isMirror(TreeNode left, TreeNode right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.val == right.val
            && isMirror(left.left, right.right)
            && isMirror(left.right, right.left);
    }

    /**
     * Determine if a binary tree is symmetric — iterative BFS version.
     *
     * Enqueue node pairs that must be mirrors. For each pair, check equality
     * and enqueue the "outer" and "inner" pairs of children.
     *
     * Time:  O(n)
     * Space: O(w) queue width
     *
     * @param root root of the tree
     * @return true if the tree is symmetric
     */
    public boolean isSymmetricIterative(TreeNode root) {
        if (root == null) return true;

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root.left);
        queue.offer(root.right);

        while (!queue.isEmpty()) {
            TreeNode left  = queue.poll();
            TreeNode right = queue.poll();

            if (left == null && right == null) continue;
            if (left == null || right == null) return false;
            if (left.val != right.val) return false;

            // Outer pair: left.left must mirror right.right
            queue.offer(left.left);
            queue.offer(right.right);
            // Inner pair: left.right must mirror right.left
            queue.offer(left.right);
            queue.offer(right.left);
        }

        return true;
    }

    // =========================================================================
    // Problem 8: Count Complete Tree Nodes
    // =========================================================================

    /**
     * Count nodes in a complete binary tree more efficiently than O(n).
     *
     * Key insight: In a complete binary tree, either:
     * - The left and right subtrees have the same height → left subtree is a PERFECT
     *   binary tree with 2^leftHeight - 1 nodes. Count it with a formula and recurse right.
     * - Left subtree is taller → right subtree is a perfect binary tree with
     *   2^rightHeight - 1 nodes. Recurse left.
     *
     * This gives O(log²n) time since we recurse O(log n) levels,
     * and computing height is O(log n).
     *
     * Time:  O(log²n)
     * Space: O(log n)
     *
     * @param root root of the complete binary tree
     * @return total number of nodes
     */
    public int countNodes(TreeNode root) {
        if (root == null) return 0;

        int leftHeight  = getLeftHeight(root);
        int rightHeight = getRightHeight(root);

        if (leftHeight == rightHeight) {
            // Left subtree is a perfect binary tree: 2^leftHeight - 1 nodes
            // Add 1 for root, then recurse on right subtree
            return (1 << leftHeight) + countNodes(root.right);
        } else {
            // Right subtree is a perfect binary tree one level shorter
            return (1 << rightHeight) + countNodes(root.left);
        }
    }

    /** Height by going all the way LEFT (leftmost leaf depth). */
    private int getLeftHeight(TreeNode node) {
        int height = 0;
        while (node != null) {
            height++;
            node = node.left;
        }
        return height;
    }

    /** Height by going all the way RIGHT (rightmost leaf depth). */
    private int getRightHeight(TreeNode node) {
        int height = 0;
        while (node != null) {
            height++;
            node = node.right;
        }
        return height;
    }

    // =========================================================================
    // Problem 9: Serialize and Deserialize Binary Tree
    // =========================================================================

    /**
     * Serialize a binary tree to a string using pre-order DFS with null markers.
     * Format: "1,2,null,null,3,4,null,null,5,null,null"
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * @param root root of the tree
     * @return serialized string
     */
    public String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        serializeHelper(root, sb);
        return sb.toString();
    }

    private void serializeHelper(TreeNode node, StringBuilder sb) {
        if (node == null) {
            sb.append("null,");
            return;
        }
        sb.append(node.val).append(",");
        serializeHelper(node.left, sb);
        serializeHelper(node.right, sb);
    }

    /**
     * Deserialize a pre-order serialized string back to a binary tree.
     * Process tokens from left to right using a Queue.
     *
     * Time:  O(n)
     * Space: O(n)
     *
     * @param data serialized string from serialize()
     * @return root of the reconstructed tree
     */
    public TreeNode deserialize(String data) {
        Queue<String> tokens = new LinkedList<>(Arrays.asList(data.split(",")));
        return deserializeHelper(tokens);
    }

    private TreeNode deserializeHelper(Queue<String> tokens) {
        String token = tokens.poll();
        if ("null".equals(token)) return null;

        TreeNode node = new TreeNode(Integer.parseInt(token));
        node.left  = deserializeHelper(tokens);
        node.right = deserializeHelper(tokens);
        return node;
    }

    // =========================================================================
    // Problem 10: Kth Smallest Element in BST
    // =========================================================================

    /**
     * Return the kth smallest value in a BST (1-indexed).
     *
     * Iterative In-Order Traversal:
     *   In-order traversal of a BST visits nodes in ascending order.
     *   Count nodes as we visit them. Return value when count reaches k.
     *
     * Time:  O(H + k) where H is tree height
     * Space: O(H) for the stack
     *
     * @param root root of the BST
     * @param k    1-indexed position of desired smallest element
     * @return kth smallest value
     */
    public int kthSmallest(TreeNode root, int k) {
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode current = root;
        int count = 0;

        while (current != null || !stack.isEmpty()) {
            // Go as far left as possible
            while (current != null) {
                stack.push(current);
                current = current.left;
            }

            // Visit the next in-order node
            current = stack.pop();
            count++;

            if (count == k) return current.val;

            // Move to the right subtree
            current = current.right;
        }

        return -1; // k is larger than tree size (shouldn't happen per constraints)
    }

    // =========================================================================
    // Main — Test Cases
    // =========================================================================

    public static void main(String[] args) {
        TreeProblems sol = new TreeProblems();

        System.out.println("=== Problem 1: Maximum Depth ===");
        TreeNode t1 = buildTree(3, 9, 20, null, null, 15, 7);
        System.out.println(sol.maxDepth(t1));   // 3
        System.out.println(sol.maxDepth(null)); // 0
        System.out.println(sol.maxDepth(buildTree(1, null, 2))); // 2

        System.out.println("\n=== Problem 2: Validate BST ===");
        TreeNode validBST = buildTree(2, 1, 3);
        System.out.println(sol.isValidBST(validBST)); // true

        TreeNode invalidBST = buildTree(5, 1, 4, null, null, 3, 6);
        System.out.println(sol.isValidBST(invalidBST)); // false

        TreeNode trickBST = buildTree(5, 4, 6, null, null, 3, 7);
        System.out.println(sol.isValidBST(trickBST)); // false (3 < 5 but is in right subtree)

        System.out.println("\n=== Problem 3: Inorder Traversal (Iterative) ===");
        TreeNode t3 = buildTree(1, null, 2, null, null, 3);
        System.out.println(sol.inorderTraversal(t3)); // [1, 3, 2]
        System.out.println(sol.inorderTraversal(buildTree(1, 2, 3, 4, 5))); // [4, 2, 5, 1, 3]

        System.out.println("\n=== Problem 4: Level Order Traversal ===");
        TreeNode t4 = buildTree(3, 9, 20, null, null, 15, 7);
        System.out.println(sol.levelOrder(t4)); // [[3], [9, 20], [15, 7]]
        System.out.println(sol.levelOrder(null)); // []

        System.out.println("\n=== Problem 5: Binary Tree Paths ===");
        TreeNode t5 = buildTree(1, 2, 3, null, 5);
        System.out.println(sol.binaryTreePaths(t5)); // [1->2->5, 1->3]
        System.out.println(sol.binaryTreePaths(buildTree(1))); // [1]

        System.out.println("\n=== Problem 6: Lowest Common Ancestor (BST) ===");
        TreeNode bst = buildTree(6, 2, 8, 0, 4, 7, 9, null, null, 3, 5);
        // Find nodes p=2, q=8 (LCA should be 6)
        TreeNode p1 = new TreeNode(2), q1 = new TreeNode(8);
        System.out.println(sol.lowestCommonAncestor(bst, p1, q1).val); // 6

        TreeNode p2 = new TreeNode(2), q2 = new TreeNode(4);
        System.out.println(sol.lowestCommonAncestor(bst, p2, q2).val); // 2

        System.out.println("\n=== Problem 7: Symmetric Tree ===");
        TreeNode sym1 = buildTree(1, 2, 2, 3, 4, 4, 3);
        System.out.println(sol.isSymmetric(sym1));           // true
        System.out.println(sol.isSymmetricIterative(sym1));  // true

        TreeNode sym2 = buildTree(1, 2, 2, null, 3, null, 3);
        System.out.println(sol.isSymmetric(sym2));           // false
        System.out.println(sol.isSymmetricIterative(sym2));  // false

        System.out.println("\n=== Problem 8: Count Complete Tree Nodes ===");
        TreeNode complete1 = buildTree(1, 2, 3, 4, 5, 6);
        System.out.println(sol.countNodes(complete1)); // 6

        TreeNode complete2 = buildTree(1, 2, 3, 4, 5, 6, 7);
        System.out.println(sol.countNodes(complete2)); // 7

        System.out.println("\n=== Problem 9: Serialize and Deserialize ===");
        TreeNode original = buildTree(1, 2, 3, null, null, 4, 5);
        String serialized = sol.serialize(original);
        System.out.println("Serialized: " + serialized);
        TreeNode restored = sol.deserialize(serialized);
        System.out.println("Restored in-order: " + sol.inorderTraversal(restored));
        System.out.println("Match: " + sol.serialize(restored).equals(serialized)); // true

        System.out.println("\n=== Problem 10: Kth Smallest in BST ===");
        TreeNode kthBST = buildTree(3, 1, 4, null, 2);
        System.out.println(sol.kthSmallest(kthBST, 1)); // 1
        System.out.println(sol.kthSmallest(kthBST, 2)); // 2
        System.out.println(sol.kthSmallest(kthBST, 3)); // 3

        TreeNode kthBST2 = buildTree(5, 3, 6, 2, 4, null, null, 1);
        System.out.println(sol.kthSmallest(kthBST2, 3)); // 3
    }
}
