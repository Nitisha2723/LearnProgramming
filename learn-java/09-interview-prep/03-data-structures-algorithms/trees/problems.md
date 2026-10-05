# Tree Problems — Interview Prep

Ten classic binary tree / BST problems that appear frequently in technical interviews.
Key techniques: recursive DFS, iterative with explicit stack, BFS with queue.

---

## Problem 1: Maximum Depth of Binary Tree

**Difficulty:** Easy

### Problem Statement
Given the root of a binary tree, return its maximum depth.
The maximum depth is the number of nodes along the longest path from the root to a leaf.

### Examples
```
    3
   / \
  9  20
    /  \
   15   7

Input:  root = [3,9,20,null,null,15,7]
Output: 3
```

### Constraints
- 0 <= number of nodes <= 10^4
- -100 <= Node.val <= 100

### Approach — Recursive DFS
The maximum depth of a tree is 1 + max(depth(left subtree), depth(right subtree)).
Base case: null node has depth 0.

```java
maxDepth(null) = 0
maxDepth(node) = 1 + max(maxDepth(node.left), maxDepth(node.right))
```

- Time: O(n) — visits every node once
- Space: O(h) — call stack, where h is tree height. O(n) worst case (skewed tree)

### Edge Cases
- Empty tree (root = null): return 0
- Single node: return 1
- Completely skewed tree: depth = n

---

## Problem 2: Validate Binary Search Tree

**Difficulty:** Medium

### Problem Statement
Given the root of a binary tree, determine if it is a valid BST.
A BST requires: left subtree contains only nodes with values strictly less than the root,
right subtree contains only nodes with values strictly greater.

### Examples
```
    2          5
   / \        / \
  1   3      1   4
             / \
            3   6

Input: [2,1,3]  → true
Input: [5,1,4,null,null,3,6] → false (root=5, right child=4 which is < 5)
```

### Constraints
- 1 <= number of nodes <= 10^4
- -2^31 <= Node.val <= 2^31 - 1

### Approach — Range Check (Valid Min/Max Window)
Pass a valid range (min, max) down the recursion tree.
At each node, verify `min < node.val < max`.
- Left child: upper bound becomes the parent's value
- Right child: lower bound becomes the parent's value

**Why not just check left < root < right locally?**
A BST constraint is global: all nodes in the left subtree must be less than the root,
not just the immediate children. The range check propagates this global constraint.

```
isValid(node, min=-∞, max=+∞)
  if node == null: return true
  if node.val <= min or node.val >= max: return false
  return isValid(node.left, min, node.val)
      && isValid(node.right, node.val, max)
```

- Time: O(n), Space: O(h)

### Alternative — In-Order Traversal
In-order traversal of a valid BST yields a strictly increasing sequence.
Track the previous value and verify each node is larger.

### Edge Cases
- Integer boundary values: use Long.MIN_VALUE and Long.MAX_VALUE for bounds
- Duplicate values: not allowed in BST (must be strictly greater/less)

---

## Problem 3: Binary Tree Inorder Traversal (Iterative)

**Difficulty:** Easy

### Problem Statement
Given the root of a binary tree, return the in-order traversal (left, root, right)
as a list of values. Implement iteratively.

### Examples
```
    1
     \
      2
     /
    3
Input: [1,null,2,3]
Output: [1,3,2]
```

### Approach — Explicit Stack
```
stack = [], result = [], current = root

while current != null OR stack not empty:
    while current != null:
        push current to stack
        current = current.left   ← go as far left as possible

    current = stack.pop()        ← process this node
    result.add(current.val)
    current = current.right      ← move to right subtree
```

**Why this simulates recursion:** The stack replaces the call stack used in recursive
in-order traversal. We push nodes as we go left, then process them as we pop.

- Time: O(n), Space: O(h)

### Edge Cases
- Empty tree: return empty list
- Skewed left tree: stack will hold all nodes before popping
- Skewed right tree: stack depth is 1 at most

---

## Problem 4: Level Order Traversal (BFS)

**Difficulty:** Medium

### Problem Statement
Given the root of a binary tree, return the level order traversal of its nodes' values
as a list of lists (one list per level).

### Examples
```
    3
   / \
  9  20
    /  \
   15   7

Output: [[3],[9,20],[15,7]]
```

### Approach — Queue (BFS)
1. Add root to a Queue.
2. While the queue is not empty:
   - Record the queue's current size (this is the number of nodes at this level).
   - Process exactly that many nodes (poll and process), adding their children.
   - Add the level's values to the result.

The key insight: `levelSize = queue.size()` before the inner loop ensures we
process exactly one level per outer iteration.

- Time: O(n), Space: O(w) where w is maximum width of the tree

### Edge Cases
- Empty tree: return empty list
- Single node: return [[root.val]]

---

## Problem 5: Binary Tree Paths (Backtracking DFS)

**Difficulty:** Easy

### Problem Statement
Given the root of a binary tree, return all root-to-leaf paths as a list of strings.

### Examples
```
    1
   / \
  2   3
   \
    5
Output: ["1->2->5", "1->3"]
```

### Approach — DFS with Backtracking
Use recursive DFS. Maintain the current path as a list.
When reaching a leaf, convert the path to a string and add to results.
Backtrack by removing the last element after recursing.

- Time: O(n), Space: O(h) for recursion stack + O(n*h) for storing all paths

### Edge Cases
- Single node (root is leaf): one path containing just the root value
- Empty tree: return empty list

---

## Problem 6: Lowest Common Ancestor of a BST

**Difficulty:** Medium

### Problem Statement
Given a BST and two nodes p and q, return their lowest common ancestor (LCA).
The LCA is the deepest node that has both p and q as descendants.

### Examples
```
BST:   6
      / \
     2   8
    / \ / \
   0  4 7  9
     / \
    3   5

LCA(2, 8) = 6
LCA(2, 4) = 2
```

### Approach — Leverage BST Property
In a BST, if both p and q are less than root, LCA is in the left subtree.
If both are greater, LCA is in the right subtree.
Otherwise (root is between them, or equals one of them), root IS the LCA.

```
if p.val < root.val AND q.val < root.val:  go left
if p.val > root.val AND q.val > root.val:  go right
else: return root (root is the split point)
```

- Time: O(h) — traverse from root to LCA, O(log n) for balanced BST
- Space: O(h) recursive, O(1) iterative

### Edge Cases
- One of p, q IS the LCA (one is an ancestor of the other)
- p == q (not typical but handled: return that node)

---

## Problem 7: Symmetric Tree

**Difficulty:** Easy

### Problem Statement
Given the root of a binary tree, check whether it is a mirror of itself (symmetric).

### Examples
```
    1
   / \
  2   2
 / \ / \
3  4 4  3
→ true

    1
   / \
  2   2
   \   \
    3   3
→ false
```

### Approach 1 — Recursive
Define a helper `isMirror(left, right)`:
- Both null → true
- One null → false
- Values equal AND left.left mirrors right.right AND left.right mirrors right.left

### Approach 2 — Iterative (BFS with Queue)
Use a queue storing node pairs. For each pair (left, right):
- Both null: continue
- One null or values differ: return false
- Enqueue (left.left, right.right) and (left.right, right.left)

- Time: O(n), Space: O(n)

---

## Problem 8: Count Complete Tree Nodes

**Difficulty:** Medium

### Problem Statement
Given the root of a complete binary tree, return the number of nodes.
A complete binary tree has all levels completely filled except possibly the last,
which is filled from the left.

### Approach — Binary Search on Last Level
For a complete binary tree, compute height h by going all the way left.
Then binary search for the last node on the last level.

Naive approach (count every node): O(n)
Optimal: O(log²n) by leveraging the complete tree property.

For a complete tree of height h:
- If left subtree height == right subtree height: left subtree is perfect (2^h - 1 nodes),
  recurse on right subtree.
- Otherwise: right subtree is perfect (2^(h-1) - 1 nodes), recurse on left subtree.

- Time: O(log²n), Space: O(log n)

---

## Problem 9: Serialize and Deserialize Binary Tree

**Difficulty:** Hard

### Problem Statement
Design an algorithm to serialize a binary tree to a string and deserialize that string
back to the tree.

### Examples
```
Tree:    1
        / \
       2   3
          / \
         4   5

Serialized: "1,2,null,null,3,4,null,null,5,null,null"
```

### Approach — Pre-Order Traversal with Null Markers
**Serialize:** DFS pre-order. For each node, write its value. For null, write "null".
Separate tokens with commas.

**Deserialize:** Process tokens left to right using a Queue.
Take next token:
- If "null": return null
- Otherwise: create node, set left = recurse(), set right = recurse()

Pre-order is the natural choice because the root is first, allowing us to reconstruct
the tree in the same order we read the tokens.

- Time: O(n) serialize, O(n) deserialize
- Space: O(n)

---

## Problem 10: Kth Smallest Element in BST

**Difficulty:** Medium

### Problem Statement
Given the root of a BST and an integer k, return the kth smallest value (1-indexed).

### Examples
```
BST:   3
      / \
     1   4
      \
       2

k=1 → 1, k=3 → 3
```

### Approach — In-Order Traversal
In-order traversal of a BST visits nodes in ascending order.
Count nodes as we visit them. The kth node visited is the answer.

**Iterative in-order** avoids recursion overhead:
```
stack = [], current = root, count = 0
while current != null OR stack not empty:
    while current != null:
        push current; current = current.left
    current = stack.pop(); count++
    if count == k: return current.val
    current = current.right
```

- Time: O(H + k) where H is tree height
- Space: O(H) for stack

### Edge Cases
- k = 1: return leftmost node
- k = total_nodes: return rightmost node
