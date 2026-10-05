"""
Tree Problems — 10 classic interview questions.

Includes TreeNode definition, then 10 solved problems.
"""

from collections import deque
from typing import Optional, List


# ─────────────────────────────────────────────────────────────────────────────
# Data Structure
# ─────────────────────────────────────────────────────────────────────────────

class TreeNode:
    """Binary tree node."""

    def __init__(
        self,
        val: int = 0,
        left: "Optional[TreeNode]" = None,
        right: "Optional[TreeNode]" = None,
    ) -> None:
        self.val = val
        self.left = left
        self.right = right

    def __repr__(self) -> str:
        return f"TreeNode({self.val})"


def build_tree(values: list) -> Optional[TreeNode]:
    """Build a binary tree from level-order list (None = missing node)."""
    if not values or values[0] is None:
        return None
    root = TreeNode(values[0])
    queue = deque([root])
    i = 1
    while queue and i < len(values):
        node = queue.popleft()
        if i < len(values) and values[i] is not None:
            node.left = TreeNode(values[i])
            queue.append(node.left)
        i += 1
        if i < len(values) and values[i] is not None:
            node.right = TreeNode(values[i])
            queue.append(node.right)
        i += 1
    return root


# ─────────────────────────────────────────────────────────────────────────────
# Problem 1: Maximum Depth of Binary Tree
# ─────────────────────────────────────────────────────────────────────────────

def max_depth(root: Optional[TreeNode]) -> int:
    """
    Find the maximum depth (height) of a binary tree.

    Approach:
        DFS recursion O(n): depth = 1 + max(left_depth, right_depth).

    Example:
        Tree: [3,9,20,None,None,15,7] -> depth 3
    """
    if not root:
        return 0
    return 1 + max(max_depth(root.left), max_depth(root.right))


# ─────────────────────────────────────────────────────────────────────────────
# Problem 2: Same Tree
# ─────────────────────────────────────────────────────────────────────────────

def is_same_tree(p: Optional[TreeNode], q: Optional[TreeNode]) -> bool:
    """
    Check if two binary trees are structurally identical with same values.

    Approach:
        DFS recursion O(n).

    Example:
        >>> is_same_tree(build_tree([1,2,3]), build_tree([1,2,3]))
        True
        >>> is_same_tree(build_tree([1,2]), build_tree([1,None,2]))
        False
    """
    if not p and not q:
        return True
    if not p or not q:
        return False
    return (p.val == q.val
            and is_same_tree(p.left, q.left)
            and is_same_tree(p.right, q.right))


# ─────────────────────────────────────────────────────────────────────────────
# Problem 3: Invert Binary Tree
# ─────────────────────────────────────────────────────────────────────────────

def invert_tree(root: Optional[TreeNode]) -> Optional[TreeNode]:
    """
    Mirror a binary tree (swap left and right at every node).

    Approach:
        DFS recursion O(n).

    Example:
        [4,2,7,1,3,6,9] -> [4,7,2,9,6,3,1]
    """
    if not root:
        return None
    root.left, root.right = invert_tree(root.right), invert_tree(root.left)
    return root


# ─────────────────────────────────────────────────────────────────────────────
# Problem 4: Binary Tree Level Order Traversal (BFS)
# ─────────────────────────────────────────────────────────────────────────────

def level_order(root: Optional[TreeNode]) -> list[list[int]]:
    """
    Return nodes level by level (BFS).

    Approach:
        Queue-based BFS O(n): process one level at a time.

    Example:
        >>> level_order(build_tree([3,9,20,None,None,15,7]))
        [[3], [9, 20], [15, 7]]
    """
    if not root:
        return []
    result = []
    queue = deque([root])
    while queue:
        level = []
        for _ in range(len(queue)):
            node = queue.popleft()
            level.append(node.val)
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
        result.append(level)
    return result


# ─────────────────────────────────────────────────────────────────────────────
# Problem 5: Validate Binary Search Tree
# ─────────────────────────────────────────────────────────────────────────────

def is_valid_bst(root: Optional[TreeNode]) -> bool:
    """
    Validate that a binary tree is a valid BST.

    Approach:
        DFS with bounds O(n): each node must satisfy lo < val < hi.

    Example:
        >>> is_valid_bst(build_tree([2,1,3]))
        True
        >>> is_valid_bst(build_tree([5,1,4,None,None,3,6]))
        False
    """
    def validate(node, lo, hi):
        if not node:
            return True
        if not (lo < node.val < hi):
            return False
        return (validate(node.left, lo, node.val)
                and validate(node.right, node.val, hi))

    return validate(root, float("-inf"), float("inf"))


# ─────────────────────────────────────────────────────────────────────────────
# Problem 6: Lowest Common Ancestor of BST
# ─────────────────────────────────────────────────────────────────────────────

def lca_bst(
    root: Optional[TreeNode], p: TreeNode, q: TreeNode
) -> Optional[TreeNode]:
    """
    Find the lowest common ancestor in a BST.

    Approach:
        O(h): use BST property — if both values < root, go left; if both >
        root, go right; else root is the LCA.

    Example:
        BST [6,2,8,0,4,7,9,None,None,3,5], p=2, q=8 -> LCA is 6
    """
    while root:
        if p.val < root.val and q.val < root.val:
            root = root.left
        elif p.val > root.val and q.val > root.val:
            root = root.right
        else:
            return root
    return None


# ─────────────────────────────────────────────────────────────────────────────
# Problem 7: Binary Tree Right Side View
# ─────────────────────────────────────────────────────────────────────────────

def right_side_view(root: Optional[TreeNode]) -> list[int]:
    """
    Return values of nodes visible from the right side (last node per level).

    Approach:
        BFS O(n): take last node of each level.

    Example:
        >>> right_side_view(build_tree([1,2,3,None,5,None,4]))
        [1, 3, 4]
    """
    if not root:
        return []
    result = []
    queue = deque([root])
    while queue:
        for i in range(len(queue)):
            node = queue.popleft()
            if i == 0:   # last popped from right-to-left queue is the rightmost
                result.append(node.val)
            if node.right:
                queue.append(node.right)
            if node.left:
                queue.append(node.left)
    return result


# ─────────────────────────────────────────────────────────────────────────────
# Problem 8: Count Good Nodes in Binary Tree
# ─────────────────────────────────────────────────────────────────────────────

def good_nodes(root: Optional[TreeNode]) -> int:
    """
    Count nodes where no node on the path from root is greater than node's value.

    Approach:
        DFS O(n): pass current max along the path.

    Example:
        >>> good_nodes(build_tree([3,1,4,3,None,1,5]))
        4
    """
    def dfs(node, max_so_far):
        if not node:
            return 0
        is_good = 1 if node.val >= max_so_far else 0
        new_max = max(max_so_far, node.val)
        return is_good + dfs(node.left, new_max) + dfs(node.right, new_max)

    return dfs(root, float("-inf"))


# ─────────────────────────────────────────────────────────────────────────────
# Problem 9: Diameter of Binary Tree
# ─────────────────────────────────────────────────────────────────────────────

def diameter_of_binary_tree(root: Optional[TreeNode]) -> int:
    """
    Find the length of the longest path between any two nodes.

    Approach:
        DFS O(n): at each node, diameter through it = left_height + right_height.
        Track global maximum.

    Example:
        >>> diameter_of_binary_tree(build_tree([1,2,3,4,5]))
        3  # path: 4 -> 2 -> 1 -> 3
    """
    best = [0]

    def height(node):
        if not node:
            return 0
        left = height(node.left)
        right = height(node.right)
        best[0] = max(best[0], left + right)
        return 1 + max(left, right)

    height(root)
    return best[0]


# ─────────────────────────────────────────────────────────────────────────────
# Problem 10: Serialize and Deserialize Binary Tree
# ─────────────────────────────────────────────────────────────────────────────

def serialize(root: Optional[TreeNode]) -> str:
    """
    Encode a binary tree to a string (pre-order DFS).

    Example:
        serialize(build_tree([1,2,3,None,None,4,5])) -> "1,2,N,N,3,4,N,N,5,N,N"
    """
    def dfs(node):
        if not node:
            return ["N"]
        return [str(node.val)] + dfs(node.left) + dfs(node.right)

    return ",".join(dfs(root))


def deserialize(data: str) -> Optional[TreeNode]:
    """
    Decode a string (from serialize) back to a binary tree.
    """
    vals = iter(data.split(","))

    def dfs():
        val = next(vals)
        if val == "N":
            return None
        node = TreeNode(int(val))
        node.left = dfs()
        node.right = dfs()
        return node

    return dfs()


# ─────────────────────────────────────────────────────────────────────────────
# Quick self-test
# ─────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    t = build_tree([3, 9, 20, None, None, 15, 7])
    assert max_depth(t) == 3
    assert is_same_tree(build_tree([1,2,3]), build_tree([1,2,3]))
    assert level_order(t) == [[3], [9, 20], [15, 7]]
    assert is_valid_bst(build_tree([2,1,3]))
    assert right_side_view(build_tree([1,2,3,None,5,None,4])) == [1,3,4]
    assert diameter_of_binary_tree(build_tree([1,2,3,4,5])) == 3

    tree = build_tree([1,2,3,None,None,4,5])
    s = serialize(tree)
    assert serialize(deserialize(s)) == s
    print("All tree problems passed!")
