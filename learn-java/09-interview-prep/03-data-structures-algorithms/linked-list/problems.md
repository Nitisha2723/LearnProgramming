# Linked List Problems — Interview Prep

Ten classic linked list problems that appear frequently in technical interviews.
Key techniques: fast/slow pointers, dummy head nodes, reversing in-place.

---

## Problem 1: Reverse a Linked List

**Difficulty:** Easy

### Problem Statement
Given the head of a singly linked list, reverse the list and return the new head.

### Examples
```
Input:  1 -> 2 -> 3 -> 4 -> 5 -> null
Output: 5 -> 4 -> 3 -> 2 -> 1 -> null

Input:  1 -> 2 -> null
Output: 2 -> 1 -> null

Input:  null
Output: null
```

### Constraints
- 0 <= number of nodes <= 5000
- -5000 <= Node.val <= 5000

### Iterative Approach
Maintain three pointers: prev (null), curr (head), next (temp storage).
Repeatedly redirect curr.next to prev, then advance all three pointers.

```
null <- 1 -> 2 -> 3
       ^prev ^curr ^next at start
       
Step 1: 1.next = null; prev=1, curr=2
Step 2: 2.next = 1;    prev=2, curr=3
Step 3: 3.next = 2;    prev=3, curr=null → done
```

- Time: O(n), Space: O(1)

### Recursive Approach
Recursively reverse from the end. `reverse(head.next)` returns the new head (last node).
Then hook head.next.next = head (make the next node point back to current).
Set head.next = null to terminate the reversed sublist.

- Time: O(n), Space: O(n) call stack

### Edge Cases
- Empty list: return null
- Single node: return it unchanged
- Two nodes: swap occurs correctly

---

## Problem 2: Detect Cycle in a Linked List

**Difficulty:** Easy

### Problem Statement
Given the head of a linked list, determine if the list has a cycle.
A cycle exists if some node can be reached again by following next pointers.

### Examples
```
1 -> 2 -> 3 -> 4
          ^         (4.next points back to 3)
          |_________|
→ true (cycle exists)

1 -> 2 -> 3 -> null
→ false
```

### Constraints
- 0 <= number of nodes <= 10^4
- -10^5 <= Node.val <= 10^5

### Approach — Floyd's Tortoise and Hare
Use two pointers: slow (moves 1 step) and fast (moves 2 steps).
- If there's no cycle, fast will reach null.
- If there's a cycle, fast will eventually "lap" slow inside the cycle, and they'll meet.

**Why they always meet:** Once both pointers are in the cycle, each iteration reduces
the distance between them by 1 (fast gains 1 step on slow per iteration since it moves 2
and slow moves 1). They must meet within at most cycle_length iterations.

- Time: O(n)
- Space: O(1) — only two pointers

### Edge Cases
- Empty list or single node without cycle: fast reaches null
- Single node pointing to itself: fast == slow after first iteration

---

## Problem 3: Find Middle of Linked List

**Difficulty:** Easy

### Problem Statement
Given the head of a linked list, return the middle node.
If there are two middle nodes (even length), return the second middle.

### Examples
```
Input:  1 -> 2 -> 3 -> 4 -> 5
Output: Node with value 3

Input:  1 -> 2 -> 3 -> 4 -> 5 -> 6
Output: Node with value 4 (second of two middles)
```

### Constraints
- 1 <= number of nodes <= 100

### Approach — Fast and Slow Pointers
- slow moves 1 step per iteration
- fast moves 2 steps per iteration
- When fast reaches the end (null or last node), slow is at the middle

**Why it works:** fast travels twice as fast, so when fast has covered n steps, slow has
covered n/2 steps — placing slow at the midpoint.

For even-length lists: fast reaches null when slow is at the second middle.
For odd-length lists: fast reaches the last node when slow is at the exact middle.

- Time: O(n), Space: O(1)

### Edge Cases
- Single node: return it (fast immediately can't move 2 steps)
- Two nodes: return the second node

---

## Problem 4: Merge Two Sorted Lists

**Difficulty:** Easy

### Problem Statement
Given the heads of two sorted linked lists, merge them into one sorted list.
Return the head of the merged list.

### Examples
```
Input:  l1 = 1 -> 2 -> 4, l2 = 1 -> 3 -> 4
Output: 1 -> 1 -> 2 -> 3 -> 4 -> 4

Input:  l1 = null, l2 = null
Output: null

Input:  l1 = null, l2 = 0
Output: 0
```

### Constraints
- 0 <= nodes in each list <= 50
- -100 <= Node.val <= 100
- Both lists are sorted

### Iterative Approach — Dummy Head Node
Use a dummy (sentinel) head node to simplify edge cases around the first node.
Maintain a `current` pointer to track where to append next.

1. Create dummy node, set current = dummy
2. While both l1 and l2 are non-null:
   - Append the smaller node to current.next
   - Advance the pointer of the list we took from
   - Advance current
3. Append any remaining nodes (one list may be longer)
4. Return dummy.next

- Time: O(m + n), Space: O(1)

### Edge Cases
- Both empty: return null
- One empty: return the other
- All elements of one list are smaller than all of the other

---

## Problem 5: Remove Nth Node From End of List

**Difficulty:** Medium

### Problem Statement
Given the head of a linked list, remove the nth node from the end of the list
and return the head.

### Examples
```
Input:  1 -> 2 -> 3 -> 4 -> 5, n = 2
Output: 1 -> 2 -> 3 -> 5

Input:  1, n = 1
Output: null (remove only node)

Input:  1 -> 2, n = 1
Output: 1
```

### Constraints
- 1 <= number of nodes <= 30
- 0 <= Node.val <= 100
- 1 <= n <= sz

### Two-Pass Approach
First pass: count total length L.
Second pass: the node to remove is at position (L - n) from the front.
Use a dummy head to handle removing the first node cleanly.

- Time: O(L), Space: O(1)

### One-Pass Approach (Two Pointers — Optimal)
Use two pointers with a gap of n+1 between them:
1. Advance `fast` pointer n+1 steps ahead of `slow`
2. Move both at the same pace until `fast` reaches null
3. Now `slow` is just before the node to remove
4. Skip the target: `slow.next = slow.next.next`

**Why n+1 gap?** We need slow to stop at the node BEFORE the one to delete,
not at the node itself. An n+1 gap ensures that.

Use a dummy head: when removing the first node (n == length), slow stays at dummy.

- Time: O(L), Space: O(1)

### Edge Cases
- Remove first node: use dummy head so slow.next = slow.next.next works
- Remove last node: n = 1
- Single node list: remove it, return null

---

## Problem 6: Intersection of Two Linked Lists

**Difficulty:** Easy

### Problem Statement
Given the heads of two singly linked-lists headA and headB, return the node at which
the two lists intersect. If the two linked lists have no intersection, return null.

### Examples
```
A:      4 -> 1 ─┐
                  8 -> 4 -> 5 -> null
B: 5 -> 6 -> 1 ─┘
Intersection at node with val 8.
```

### Constraints
- 0 <= nodes in listA, listB <= 3 * 10^4
- No cycles

### Approach — Length Difference
1. Compute lengths of both lists (lenA and lenB)
2. Advance the pointer in the longer list by |lenA - lenB| steps
3. Advance both pointers together until they point to the same node (or both null)

**Why this works:** After equalizing the starting positions, both pointers cover the
same number of steps to reach the intersection (if it exists).

- Time: O(m + n), Space: O(1)

### Alternative — Two-Pointer Swap
When pA reaches the end, redirect it to headB. When pB reaches the end, redirect to headA.
They will meet at the intersection after traversing a + b + c steps (where c is the common tail length).

- Time: O(m + n), Space: O(1) — slightly more elegant

### Edge Cases
- No intersection: both pointers reach null at the same time → return null
- Same head: immediately same pointer

---

## Problem 7: Palindrome Linked List

**Difficulty:** Easy

### Problem Statement
Given the head of a singly linked list, return true if it is a palindrome, false otherwise.
Must be O(n) time and O(1) extra space.

### Examples
```
Input:  1 -> 2 -> 2 -> 1
Output: true

Input:  1 -> 2
Output: false
```

### Constraints
- 1 <= number of nodes <= 10^5
- 0 <= Node.val <= 9

### Approach — Find Middle + Reverse Second Half
1. Find the middle of the list (fast/slow pointers)
2. Reverse the second half starting from middle.next
3. Compare the first half with the reversed second half
4. (Optionally restore the list by reversing again)

**Example with [1,2,2,1]:**
```
Find middle: slow=node(2 at idx 1), fast=node(1 at end)
Reverse second half: 2->1 becomes 1->2
Compare: 1==1 ✓, 2==2 ✓ → palindrome
```

- Time: O(n), Space: O(1)

### Edge Cases
- Single node: always palindrome
- Two nodes: palindrome iff both values equal
- Odd length: middle node doesn't need to be compared (it's the pivot)

---

## Problem 8: Remove Duplicates from Sorted List

**Difficulty:** Easy

### Problem Statement
Given the head of a sorted linked list, delete all duplicates such that each element
appears only once. Return the sorted list.

### Examples
```
Input:  1 -> 1 -> 2
Output: 1 -> 2

Input:  1 -> 1 -> 2 -> 3 -> 3
Output: 1 -> 2 -> 3
```

### Constraints
- 0 <= number of nodes <= 300
- -100 <= Node.val <= 100
- List is sorted in ascending order

### Approach
Traverse the list. For each node, skip all following nodes with the same value.

```
while (current != null && current.next != null):
    if current.val == current.next.val:
        current.next = current.next.next  // skip duplicate
    else:
        current = current.next
```

- Time: O(n), Space: O(1)

### Edge Cases
- Empty list or single node: return as-is
- All elements same: keep only one
- No duplicates: traverse without changing anything

---

## Problem 9: Add Two Numbers

**Difficulty:** Medium

### Problem Statement
Two non-negative integers are stored in linked lists in reverse order (each node contains
one digit). Add the two numbers and return the sum as a linked list in reverse order.

### Examples
```
Input:  l1 = 2->4->3 (represents 342), l2 = 5->6->4 (represents 465)
Output: 7->0->8 (represents 807)

Input:  l1 = 0, l2 = 0
Output: 0

Input:  l1 = 9->9->9->9->9->9->9, l2 = 9->9->9->9
Output: 8->9->9->9->0->0->0->1
```

### Constraints
- 1 <= nodes in each list <= 100
- 0 <= Node.val <= 9
- No leading zeros (except the number 0 itself)

### Approach — Simulate Addition with Carry
Use a dummy head. Traverse both lists simultaneously, adding corresponding digits plus carry.
When a list runs out, treat its value as 0.

```
carry = 0
while l1 != null OR l2 != null OR carry != 0:
    sum = (l1.val if l1 else 0) + (l2.val if l2 else 0) + carry
    carry = sum / 10
    new node with value sum % 10
    advance l1, l2
```

- Time: O(max(m, n)), Space: O(max(m, n)) for result

### Edge Cases
- Different lengths: shorter list treated as 0 when exhausted
- Carry at the end: e.g., 5 + 5 = 10 → need an extra node
- Both are [0]: result is [0]

---

## Problem 10: Reorder List

**Difficulty:** Medium

### Problem Statement
Given the head of a linked list L0 -> L1 -> ... -> Ln-1 -> Ln,
reorder it to: L0 -> Ln -> L1 -> Ln-1 -> L2 -> Ln-2 -> ...
Modify in-place, do not return anything.

### Examples
```
Input:  1 -> 2 -> 3 -> 4
Output: 1 -> 4 -> 2 -> 3

Input:  1 -> 2 -> 3 -> 4 -> 5
Output: 1 -> 5 -> 2 -> 4 -> 3
```

### Constraints
- 1 <= number of nodes <= 5 * 10^4
- 1 <= Node.val <= 1000

### Approach — Find Middle + Reverse Second Half + Merge
This breaks the problem into three subproblems we've already solved:

1. **Find the middle** using fast/slow pointers
2. **Reverse the second half** of the list
3. **Merge** the two halves by interleaving (first half and reversed second half)

```
Original: 1 -> 2 -> 3 -> 4 -> 5
Middle:   node 3 (slow pointer)
Second half after split: 4 -> 5
Reversed second half:    5 -> 4
Merge:
  1 -> 5 -> 2 -> 4 -> 3
```

**Merge step:**
```
while second != null:
    tmp1 = first.next
    tmp2 = second.next
    first.next = second
    second.next = tmp1
    first = tmp1
    second = tmp2
```

- Time: O(n), Space: O(1)

### Edge Cases
- 1 or 2 nodes: no reordering needed
- Odd length: middle node stays in place
