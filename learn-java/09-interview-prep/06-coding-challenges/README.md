# Coding Challenges

This section contains Java implementations of common interview coding problems, organized by difficulty. Each solution includes explanations, complexity analysis, and working test cases.

---

## How to Approach a Coding Problem

Use this 6-step framework. Don't skip steps, especially under pressure.

### Step 1: Understand the Problem (2–3 min)
- Read the problem twice
- Ask clarifying questions: "What if the input is empty?" "Can there be duplicates?" "Is the array sorted?"
- Restate the problem in your own words to confirm understanding

### Step 2: Work Through Examples (2–3 min)
- Run through the provided examples manually
- Create your own examples, especially edge cases
- Don't touch code yet

### Step 3: Identify Edge Cases (1–2 min)
Common edge cases to consider:
- Empty input (empty string, empty array, null)
- Single element
- All same elements
- Negative numbers
- Integer overflow
- Very large or very small inputs

### Step 4: Brute Force First (2–3 min)
- Verbalize a naive solution — even if it's O(n²) or O(n³)
- This shows you can solve the problem and gives you a baseline to optimize
- "The brute force would be to try every pair — O(n²). Let me think about how to do better."

### Step 5: Optimize (5–10 min)
- Can you trade space for time? (HashMap, auxiliary array, stack)
- Can you use sorting + binary search?
- Can you use two pointers or sliding window?
- Can you process in one pass instead of multiple passes?

### Step 6: Code and Test (15–20 min)
- Write clean code; name variables descriptively
- Walk through your code with an example as you write
- Test edge cases explicitly

---

## Time Complexity Cheat Sheet

### Common Complexities (best to worst)

| Complexity | Name | Example |
|------------|------|---------|
| O(1) | Constant | HashMap get/put, array access |
| O(log n) | Logarithmic | Binary search, balanced BST operations |
| O(n) | Linear | Single loop, linear scan |
| O(n log n) | Linearithmic | Merge sort, heap sort, sort + scan |
| O(n²) | Quadratic | Nested loops, bubble sort |
| O(2^n) | Exponential | Recursive subsets, naive recursive Fibonacci |
| O(n!) | Factorial | Generating all permutations |

### Key Data Structure Operations

| Data Structure | Access | Search | Insert | Delete |
|---------------|--------|--------|--------|--------|
| Array | O(1) | O(n) | O(n) | O(n) |
| Linked List | O(n) | O(n) | O(1) | O(1) |
| Hash Map | N/A | O(1)* | O(1)* | O(1)* |
| Binary Search Tree (balanced) | O(log n) | O(log n) | O(log n) | O(log n) |
| Heap | O(1) peek | O(n) | O(log n) | O(log n) |
| Stack | O(1) top | O(n) | O(1) | O(1) |
| Queue | O(1) front | O(n) | O(1) | O(1) |

*amortized

### Sorting Algorithms

| Algorithm | Best | Average | Worst | Space | Stable? |
|-----------|------|---------|-------|-------|---------|
| Merge Sort | O(n log n) | O(n log n) | O(n log n) | O(n) | Yes |
| Quick Sort | O(n log n) | O(n log n) | O(n²) | O(log n) | No |
| Heap Sort | O(n log n) | O(n log n) | O(n log n) | O(1) | No |
| Java Arrays.sort (primitives) | — | O(n log n) | O(n log n) | O(log n) | — |
| Java Arrays.sort (objects) | — | O(n log n) | O(n log n) | O(n) | Yes (Timsort) |

---

## Files in This Section

### Easy
| File | Problem | Key Technique |
|------|---------|--------------|
| `ValidParentheses.java` | Check balanced brackets | Stack |
| `ReverseString.java` | Reverse string in-place | Two pointers |
| `Fibonacci.java` | Fibonacci: recursive, memoized, iterative | DP / memoization |
| `PalindromeNumber.java` | Palindrome check (no string conversion) | Math digit extraction |
| `ValidAnagram.java` | Are two strings anagrams? | Character frequency |

### Medium
| File | Problem | Key Technique |
|------|---------|--------------|
| `LRUCache.java` | LRU Cache (O(1) get + put) | HashMap + Doubly Linked List |
| `BinaryTreeLevelOrder.java` | BFS level-order traversal | Queue / BFS |
| `GroupAnagrams.java` | Group anagram strings | HashMap with sorted key |
| `LongestSubstringNoRepeat.java` | Longest substring without repeating | Sliding window |
| `MergeIntervals.java` | Merge overlapping intervals | Sort + greedy |

### Hard
| File | Problem | Key Technique |
|------|---------|--------------|
| `MergeKSortedLists.java` | Merge k sorted linked lists | Min-Heap / PriorityQueue |
| `TrappingRainWater.java` | Trapping rain water | Two pointers (O(1) space) |
| `WordLadder.java` | Word ladder shortest path | BFS |

---

## Common Patterns

### Two Pointers
Used for: sorted arrays, palindrome checks, two-sum variants

```
left = 0, right = n-1
while left < right:
    if condition: left++
    else: right--
```

### Sliding Window
Used for: substrings, subarrays with constraints

```
left = 0
for right in range(n):
    // expand window by adding s[right]
    while window is invalid:
        // shrink window by removing s[left]
        left++
    // window is valid — check if it's the best
```

### HashMap for O(1) Lookup
Trade space for time. Store: previously seen values, character counts, index-to-value mapping

### Stack for Matching/Nesting
Use stack for: balanced brackets, next greater element, monotonic stack problems

### BFS for Shortest Path
Use BFS (queue) when you need shortest path in an unweighted graph. BFS explores level by level.

### Binary Search
Use when: array is sorted, or you can define a monotonic condition (answer is "true/true/true/false/false/false")

---

## Interview Tips

1. **Talk while you code**: Explain what you're doing and why. Silence is the enemy.
2. **Write clean code**: Use descriptive names. `left`, `right` not `i`, `j`.
3. **Don't optimize prematurely**: Code the clear solution first, then optimize.
4. **Test before declaring done**: Walk through with a small example. Check edge cases.
5. **If stuck**: Step back to examples, consider a simpler version of the problem, or think aloud about what data structure would help.
6. **Time management**: If you're 20 min in and have no solution, tell the interviewer you'd like to start with a brute force.
