"""
Linked List Problems — 10 classic interview questions.

Includes Node and LinkedList helper classes, then 10 solved problems.
"""

from typing import Optional


# ─────────────────────────────────────────────────────────────────────────────
# Data Structures
# ─────────────────────────────────────────────────────────────────────────────

class ListNode:
    """Singly-linked list node."""

    def __init__(self, val: int = 0, next: "Optional[ListNode]" = None) -> None:
        self.val = val
        self.next = next

    def __repr__(self) -> str:
        return f"ListNode({self.val})"


def from_list(values: list[int]) -> Optional[ListNode]:
    """Build a linked list from a Python list."""
    if not values:
        return None
    head = ListNode(values[0])
    curr = head
    for v in values[1:]:
        curr.next = ListNode(v)
        curr = curr.next
    return head


def to_list(head: Optional[ListNode]) -> list[int]:
    """Convert a linked list to a Python list."""
    result = []
    while head:
        result.append(head.val)
        head = head.next
    return result


# ─────────────────────────────────────────────────────────────────────────────
# Problem 1: Reverse a Linked List
# ─────────────────────────────────────────────────────────────────────────────

def reverse_list(head: Optional[ListNode]) -> Optional[ListNode]:
    """
    Reverse a singly-linked list in-place.

    Approach:
        O(n) iterative: maintain prev/curr pointers; re-link as we go.

    Example:
        >>> to_list(reverse_list(from_list([1,2,3,4,5])))
        [5, 4, 3, 2, 1]
    """
    prev = None
    curr = head
    while curr:
        nxt = curr.next
        curr.next = prev
        prev = curr
        curr = nxt
    return prev


# ─────────────────────────────────────────────────────────────────────────────
# Problem 2: Detect Cycle (Floyd's Algorithm)
# ─────────────────────────────────────────────────────────────────────────────

def has_cycle(head: Optional[ListNode]) -> bool:
    """
    Detect if a linked list has a cycle.

    Approach:
        Floyd's tortoise-and-hare O(n), O(1) space: slow moves 1 step,
        fast moves 2; they meet if and only if there's a cycle.

    Example:
        head -> 3 -> 2 -> 0 -> -4 --(back to 2)
        has_cycle returns True.
    """
    slow = fast = head
    while fast and fast.next:
        slow = slow.next
        fast = fast.next.next
        if slow is fast:
            return True
    return False


# ─────────────────────────────────────────────────────────────────────────────
# Problem 3: Merge Two Sorted Lists
# ─────────────────────────────────────────────────────────────────────────────

def merge_two_sorted(
    l1: Optional[ListNode], l2: Optional[ListNode]
) -> Optional[ListNode]:
    """
    Merge two sorted linked lists into one sorted list.

    Approach:
        O(n+m): use a dummy head; repeatedly pick the smaller node.

    Example:
        >>> to_list(merge_two_sorted(from_list([1,2,4]), from_list([1,3,4])))
        [1, 1, 2, 3, 4, 4]
    """
    dummy = curr = ListNode(0)
    while l1 and l2:
        if l1.val <= l2.val:
            curr.next = l1
            l1 = l1.next
        else:
            curr.next = l2
            l2 = l2.next
        curr = curr.next
    curr.next = l1 or l2
    return dummy.next


# ─────────────────────────────────────────────────────────────────────────────
# Problem 4: Merge K Sorted Lists
# ─────────────────────────────────────────────────────────────────────────────

def merge_k_sorted(lists: list[Optional[ListNode]]) -> Optional[ListNode]:
    """
    Merge k sorted linked lists.

    Approach:
        O(N log k) using a min-heap where N = total nodes, k = number of lists.

    Example:
        >>> lists = [from_list([1,4,5]), from_list([1,3,4]), from_list([2,6])]
        >>> to_list(merge_k_sorted(lists))
        [1, 1, 2, 3, 4, 4, 5, 6]
    """
    import heapq

    # (value, tiebreaker, node) — tiebreaker avoids comparing ListNode objects
    heap = []
    for i, node in enumerate(lists):
        if node:
            heapq.heappush(heap, (node.val, i, node))

    dummy = curr = ListNode(0)
    while heap:
        val, i, node = heapq.heappop(heap)
        curr.next = node
        curr = curr.next
        if node.next:
            heapq.heappush(heap, (node.next.val, i, node.next))

    return dummy.next


# ─────────────────────────────────────────────────────────────────────────────
# Problem 5: Remove Nth Node From End
# ─────────────────────────────────────────────────────────────────────────────

def remove_nth_from_end(head: Optional[ListNode], n: int) -> Optional[ListNode]:
    """
    Remove the nth node from the end of the list.

    Approach:
        Two-pointer O(n): advance fast pointer n steps ahead; move both
        until fast reaches end; remove slow.next.

    Example:
        >>> to_list(remove_nth_from_end(from_list([1,2,3,4,5]), 2))
        [1, 2, 3, 5]
    """
    dummy = ListNode(0, head)
    fast = slow = dummy

    for _ in range(n + 1):
        fast = fast.next

    while fast:
        fast = fast.next
        slow = slow.next

    slow.next = slow.next.next
    return dummy.next


# ─────────────────────────────────────────────────────────────────────────────
# Problem 6: Reorder List
# ─────────────────────────────────────────────────────────────────────────────

def reorder_list(head: Optional[ListNode]) -> None:
    """
    Reorder list: L0 -> L1 -> ... -> Ln becomes L0 -> Ln -> L1 -> Ln-1 -> ...
    Modify in-place.

    Approach:
        1. Find middle (slow/fast pointers)
        2. Reverse second half
        3. Merge two halves

    Example:
        >>> node = from_list([1,2,3,4])
        >>> reorder_list(node)
        >>> to_list(node)
        [1, 4, 2, 3]
    """
    if not head:
        return

    # Find middle
    slow = fast = head
    while fast.next and fast.next.next:
        slow = slow.next
        fast = fast.next.next

    # Reverse second half
    second = slow.next
    slow.next = None
    prev = None
    while second:
        nxt = second.next
        second.next = prev
        prev = second
        second = nxt

    # Merge
    first = head
    second = prev
    while second:
        tmp1, tmp2 = first.next, second.next
        first.next = second
        second.next = tmp1
        first = tmp1
        second = tmp2


# ─────────────────────────────────────────────────────────────────────────────
# Problem 7: Find Middle of Linked List
# ─────────────────────────────────────────────────────────────────────────────

def find_middle(head: Optional[ListNode]) -> Optional[ListNode]:
    """
    Find the middle node. For even length, return the second middle.

    Approach:
        Slow/fast pointers O(n).

    Example:
        >>> find_middle(from_list([1,2,3,4,5])).val
        3
        >>> find_middle(from_list([1,2,3,4,5,6])).val
        4
    """
    slow = fast = head
    while fast and fast.next:
        slow = slow.next
        fast = fast.next.next
    return slow


# ─────────────────────────────────────────────────────────────────────────────
# Problem 8: Palindrome Linked List
# ─────────────────────────────────────────────────────────────────────────────

def is_palindrome_list(head: Optional[ListNode]) -> bool:
    """
    Check if a linked list is a palindrome.

    Approach:
        O(n), O(1) space: find middle, reverse second half, compare.

    Example:
        >>> is_palindrome_list(from_list([1,2,2,1]))
        True
        >>> is_palindrome_list(from_list([1,2]))
        False
    """
    slow = fast = head
    while fast and fast.next:
        slow = slow.next
        fast = fast.next.next

    # Reverse second half
    prev = None
    while slow:
        nxt = slow.next
        slow.next = prev
        prev = slow
        slow = nxt

    # Compare
    left, right = head, prev
    while right:
        if left.val != right.val:
            return False
        left = left.next
        right = right.next
    return True


# ─────────────────────────────────────────────────────────────────────────────
# Problem 9: Intersection of Two Linked Lists
# ─────────────────────────────────────────────────────────────────────────────

def get_intersection(
    headA: Optional[ListNode], headB: Optional[ListNode]
) -> Optional[ListNode]:
    """
    Find the node where two linked lists intersect.

    Approach:
        O(n+m), O(1): two pointers; each switches to the other's head when
        exhausted. They meet at the intersection (or both reach None if no
        intersection).

    Example:
        Common node: 8
        A: 4 -> 1 -> 8 -> 4 -> 5
        B:      5 -> 6 -> 1 -> 8 -> 4 -> 5
    """
    a, b = headA, headB
    while a is not b:
        a = a.next if a else headB
        b = b.next if b else headA
    return a


# ─────────────────────────────────────────────────────────────────────────────
# Problem 10: LRU Cache (using OrderedDict)
# ─────────────────────────────────────────────────────────────────────────────

from collections import OrderedDict


class LRUCache:
    """
    Least-Recently-Used cache with O(1) get and put.

    Approach:
        OrderedDict preserves insertion order and supports move_to_end().
        Evict the oldest (leftmost) entry when capacity exceeded.

    Example:
        >>> cache = LRUCache(2)
        >>> cache.put(1, 1)
        >>> cache.put(2, 2)
        >>> cache.get(1)
        1
        >>> cache.put(3, 3)   # evicts key 2
        >>> cache.get(2)
        -1
    """

    def __init__(self, capacity: int) -> None:
        self.capacity = capacity
        self._cache: OrderedDict[int, int] = OrderedDict()

    def get(self, key: int) -> int:
        if key not in self._cache:
            return -1
        self._cache.move_to_end(key)   # mark as recently used
        return self._cache[key]

    def put(self, key: int, value: int) -> None:
        if key in self._cache:
            self._cache.move_to_end(key)
        self._cache[key] = value
        if len(self._cache) > self.capacity:
            self._cache.popitem(last=False)  # evict least recently used


# ─────────────────────────────────────────────────────────────────────────────
# Quick self-test
# ─────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    assert to_list(reverse_list(from_list([1,2,3,4,5]))) == [5,4,3,2,1]
    assert to_list(merge_two_sorted(from_list([1,2,4]), from_list([1,3,4]))) == [1,1,2,3,4,4]
    assert to_list(remove_nth_from_end(from_list([1,2,3,4,5]), 2)) == [1,2,3,5]
    assert find_middle(from_list([1,2,3,4,5])).val == 3
    assert is_palindrome_list(from_list([1,2,2,1])) is True

    cache = LRUCache(2)
    cache.put(1, 1)
    cache.put(2, 2)
    assert cache.get(1) == 1
    cache.put(3, 3)
    assert cache.get(2) == -1
    print("All linked list problems passed!")
