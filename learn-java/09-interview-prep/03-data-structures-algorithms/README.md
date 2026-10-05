# Data Structures & Algorithms — Interview Prep

This module covers the most important data structures and algorithms for technical interviews.
Each subdirectory contains problem explanations and fully working Java solutions.

---

## Directory Structure

```
03-data-structures-algorithms/
├── arrays/
│   ├── problems.md          — 10 array problems with full explanations
│   └── ArrayProblems.java   — Complete working Java solutions
├── strings/
│   ├── problems.md          — 10 string problems with full explanations
│   └── StringProblems.java  — Complete working Java solutions
├── linked-list/
│   ├── problems.md          — 10 linked list problems with full explanations
│   └── LinkedListProblems.java
├── trees/
│   ├── problems.md          — 10 tree problems with full explanations
│   └── TreeProblems.java
└── dynamic-programming/
    ├── problems.md          — 8 DP problems with recurrence relations
    └── DPProblems.java
```

---

## Time Complexity Quick Reference

### Array Operations

| Operation               | Array (unsorted) | Array (sorted) | Notes                          |
|-------------------------|-----------------|----------------|--------------------------------|
| Access by index         | O(1)            | O(1)           | Direct memory offset           |
| Search (linear)         | O(n)            | O(n)           | Must scan entire array         |
| Search (binary)         | N/A             | O(log n)       | Requires sorted array          |
| Insert at end           | O(1) amortized  | O(1) amortized | ArrayList auto-resize          |
| Insert at beginning     | O(n)            | O(n)           | Must shift all elements        |
| Insert at arbitrary pos | O(n)            | O(n)           | Must shift elements right      |
| Delete at end           | O(1)            | O(1)           |                                |
| Delete at beginning     | O(n)            | O(n)           | Must shift elements left       |
| Delete arbitrary        | O(n)            | O(n)           |                                |

### Linked List Operations

| Operation               | Singly Linked   | Doubly Linked  | Notes                          |
|-------------------------|-----------------|----------------|--------------------------------|
| Access by index         | O(n)            | O(n)           | Must traverse from head        |
| Search                  | O(n)            | O(n)           |                                |
| Insert at head          | O(1)            | O(1)           | Update head pointer            |
| Insert at tail          | O(n) / O(1)*    | O(1)           | *O(1) if tail pointer stored   |
| Insert at arbitrary pos | O(n)            | O(n)           | O(n) to find position          |
| Delete at head          | O(1)            | O(1)           |                                |
| Delete at tail          | O(n)            | O(1)           | Singly needs to find prev node |
| Delete arbitrary        | O(n)            | O(n)           | O(n) to find node              |

### HashMap / HashSet Operations

| Operation               | Average Case    | Worst Case     | Notes                          |
|-------------------------|-----------------|----------------|--------------------------------|
| Put / Insert            | O(1)            | O(n)           | Worst case: all keys collide   |
| Get / Lookup            | O(1)            | O(n)           |                                |
| Delete                  | O(1)            | O(n)           |                                |
| Contains Key            | O(1)            | O(n)           |                                |
| Iteration               | O(n)            | O(n)           | Over all entries               |

### Binary Search Tree (BST) Operations

| Operation               | Average Case    | Worst Case     | Notes                          |
|-------------------------|-----------------|----------------|--------------------------------|
| Search                  | O(log n)        | O(n)           | Worst: degenerate tree (list)  |
| Insert                  | O(log n)        | O(n)           |                                |
| Delete                  | O(log n)        | O(n)           |                                |
| Min / Max               | O(log n)        | O(n)           | Leftmost / rightmost node      |
| In-order traversal      | O(n)            | O(n)           | Visits all nodes               |

### Balanced BST (AVL Tree, Red-Black Tree)

| Operation               | Time Complexity | Notes                          |
|-------------------------|-----------------|--------------------------------|
| Search                  | O(log n)        | Tree height always O(log n)    |
| Insert                  | O(log n)        | Rebalancing is O(log n)        |
| Delete                  | O(log n)        |                                |
| Java TreeMap operations | O(log n)        | Backed by Red-Black Tree       |

### Sorting Algorithms

| Algorithm      | Best      | Average   | Worst     | Space      | Stable | Notes                          |
|----------------|-----------|-----------|-----------|------------|--------|--------------------------------|
| Bubble Sort    | O(n)      | O(n²)     | O(n²)     | O(1)       | Yes    | Simple but slow                |
| Selection Sort | O(n²)     | O(n²)     | O(n²)     | O(1)       | No     | Always O(n²)                   |
| Insertion Sort | O(n)      | O(n²)     | O(n²)     | O(1)       | Yes    | Good for nearly sorted         |
| Merge Sort     | O(n log n)| O(n log n)| O(n log n)| O(n)       | Yes    | Consistent, divide & conquer   |
| Quick Sort     | O(n log n)| O(n log n)| O(n²)     | O(log n)   | No     | Worst case with bad pivot      |
| Heap Sort      | O(n log n)| O(n log n)| O(n log n)| O(1)       | No     | In-place, not cache friendly   |
| Counting Sort  | O(n+k)    | O(n+k)    | O(n+k)    | O(k)       | Yes    | k = range of values            |
| Radix Sort     | O(nk)     | O(nk)     | O(nk)     | O(n+k)     | Yes    | k = number of digits           |
| Tim Sort       | O(n)      | O(n log n)| O(n log n)| O(n)       | Yes    | Java Arrays.sort() for objects |

### Stack and Queue Operations

| Data Structure | Operation       | Time Complexity | Notes                          |
|----------------|-----------------|-----------------|--------------------------------|
| Stack          | Push / Pop      | O(1)            | Java: Deque/ArrayDeque         |
| Stack          | Peek            | O(1)            |                                |
| Queue          | Enqueue/Dequeue | O(1)            | Java: LinkedList/ArrayDeque    |
| Priority Queue | Add             | O(log n)        | Java: PriorityQueue (min heap) |
| Priority Queue | Poll (min/max)  | O(log n)        |                                |
| Priority Queue | Peek            | O(1)            |                                |

---

## Most Common Interview Patterns

### 1. Two Pointers

**When to use:** Array or string problems where you need to find pairs, remove elements in-place,
or process elements from both ends simultaneously.

**Classic problems:** Two Sum (sorted), Remove Duplicates, Container With Most Water,
Palindrome Check, Move Zeros.

**Template:**
```java
int left = 0, right = nums.length - 1;
while (left < right) {
    // check condition
    if (condition_met) {
        left++;
        right--;
    } else if (need_bigger) {
        left++;
    } else {
        right--;
    }
}
```

**Key insight:** One pointer moves from left, one from right. At each step, decide which one
to move based on the problem's constraint.

---

### 2. Sliding Window

**When to use:** Problems involving contiguous subarrays or substrings of fixed or variable size.
Look for keywords: "longest", "shortest", "minimum", "maximum" with a contiguous constraint.

**Classic problems:** Longest Substring Without Repeating Characters, Minimum Window Substring,
Maximum Sum Subarray of Size K.

**Template (variable window):**
```java
int left = 0, maxLen = 0;
Map<Character, Integer> window = new HashMap<>();

for (int right = 0; right < s.length(); right++) {
    char c = s.charAt(right);
    window.put(c, window.getOrDefault(c, 0) + 1);

    // shrink window when invariant is violated
    while (window.get(c) > 1) {
        char leftChar = s.charAt(left);
        window.put(leftChar, window.get(leftChar) - 1);
        left++;
    }
    maxLen = Math.max(maxLen, right - left + 1);
}
```

---

### 3. HashMap for Lookup

**When to use:** Reduce O(n²) brute force to O(n) by storing values you've already seen.
Any problem needing fast lookups of "have I seen X before?" or "where did X occur?"

**Classic problems:** Two Sum, Contains Duplicate, Group Anagrams, Subarray Sum Equals K.

**Key insight:** Trade O(n) space for O(n) time reduction.

```java
Map<Integer, Integer> seen = new HashMap<>();
for (int i = 0; i < nums.length; i++) {
    int complement = target - nums[i];
    if (seen.containsKey(complement)) {
        return new int[]{seen.get(complement), i};
    }
    seen.put(nums[i], i);
}
```

---

### 4. Binary Search

**When to use:** Sorted arrays. Also applies to monotonic functions where you binary search
on the answer space (e.g., "find minimum capacity such that X is possible").

**Classic problems:** Search in Rotated Sorted Array, Find First/Last Position,
Koko Eating Bananas, Capacity To Ship Packages.

**Template:**
```java
int left = 0, right = nums.length - 1;
while (left <= right) {
    int mid = left + (right - left) / 2;  // avoid overflow
    if (nums[mid] == target) return mid;
    else if (nums[mid] < target) left = mid + 1;
    else right = mid - 1;
}
return -1;
```

**Important:** Use `mid = left + (right - left) / 2` not `(left + right) / 2` to avoid integer overflow.

---

### 5. BFS (Breadth-First Search)

**When to use:** Graph or tree problems asking for shortest path, minimum steps, or
level-by-level traversal. BFS explores nodes layer by layer.

**Classic problems:** Level Order Traversal, Shortest Path in Unweighted Graph,
Word Ladder, Rotting Oranges.

**Template:**
```java
Queue<TreeNode> queue = new LinkedList<>();
queue.offer(root);

while (!queue.isEmpty()) {
    int levelSize = queue.size();  // process one level at a time
    for (int i = 0; i < levelSize; i++) {
        TreeNode node = queue.poll();
        // process node
        if (node.left != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
}
```

---

### 6. DFS (Depth-First Search)

**When to use:** Tree/graph traversal, path finding, detecting cycles, flood fill,
backtracking problems (generate all combinations/permutations).

**Classic problems:** Number of Islands, All Paths From Source to Target,
Generate Parentheses, Word Search.

**Template (recursive):**
```java
void dfs(int[][] grid, int r, int c) {
    if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length) return;
    if (grid[r][c] == 0) return;  // already visited or invalid

    grid[r][c] = 0;  // mark as visited
    dfs(grid, r + 1, c);
    dfs(grid, r - 1, c);
    dfs(grid, r, c + 1);
    dfs(grid, r, c - 1);
}
```

---

### 7. Dynamic Programming

**When to use:** Optimization problems with overlapping subproblems and optimal substructure.
Look for: "minimum/maximum number of ways", "can we achieve X", "count all ways to do X".

**Classic problems:** Climbing Stairs, House Robber, Coin Change, Longest Common Subsequence.

**Two approaches:**
1. **Top-down (memoization):** Recursive with a cache. Natural to write, starts from the answer.
2. **Bottom-up (tabulation):** Iterative, fill a table from base cases. Usually more space efficient.

**Key steps:**
1. Define the state: What does `dp[i]` represent?
2. Write the recurrence: How does `dp[i]` relate to smaller subproblems?
3. Identify base cases.
4. Determine the order of computation (for tabulation).

```java
// Bottom-up template
int[] dp = new int[n + 1];
dp[0] = baseCase0;
dp[1] = baseCase1;

for (int i = 2; i <= n; i++) {
    dp[i] = Math.max(dp[i-1], dp[i-2] + nums[i]);  // example recurrence
}
return dp[n];
```

---

## General Interview Tips

1. **Clarify before coding:** Ask about input size, whether it fits in memory, if the array is sorted,
   whether there are duplicates, what to return if no answer exists.

2. **Start with brute force:** Always mention the naive O(n²) approach first, then optimize.
   This shows you understand the problem before jumping to clever solutions.

3. **Think out loud:** Interviewers want to see your thought process. Explain what you're doing
   and why. Talk about trade-offs.

4. **Test your code:** Walk through at least two examples — one normal case and one edge case
   (empty input, single element, all same values, negative numbers).

5. **Know your complexity:** Always state the time and space complexity of your solution.
   Be ready to explain it.

6. **Common edge cases to always check:**
   - Empty array / null input
   - Single element
   - All elements the same
   - Already sorted (ascending / descending)
   - Integer overflow (use `long` when summing large numbers)
   - Negative numbers
