"""
Medium Coding Challenges
========================
1. LRU Cache
2. BFS Level Order Traversal
3. Group Anagrams
4. Longest Substring Without Repeating Characters
5. Merge Intervals

All solutions include:
  - Problem statement
  - Approach explanation
  - Time and space complexity
  - Self-test block
"""

from __future__ import annotations
from typing import List, Optional
from collections import deque, OrderedDict


# ---------------------------------------------------------------------------
# 1. LRU Cache
# ---------------------------------------------------------------------------
# Design a data structure that follows the Least Recently Used (LRU) cache
# eviction policy. Implement get(key) and put(key, value).
#
#   get(key)         — return value if key exists, else -1. Marks as recently used.
#   put(key, value)  — insert/update the key. Evict LRU item if over capacity.
#
# Approach: OrderedDict
#   OrderedDict maintains insertion order. move_to_end() marks as MRU.
#   popitem(last=False) removes LRU.
#
# Time:  O(1) per operation
# Space: O(capacity)
# ---------------------------------------------------------------------------

class LRUCache:
    def __init__(self, capacity: int) -> None:
        self.capacity = capacity
        self.cache: OrderedDict[int, int] = OrderedDict()

    def get(self, key: int) -> int:
        if key not in self.cache:
            return -1
        self.cache.move_to_end(key)      # mark as most recently used
        return self.cache[key]

    def put(self, key: int, value: int) -> None:
        if key in self.cache:
            self.cache.move_to_end(key)
        self.cache[key] = value
        if len(self.cache) > self.capacity:
            self.cache.popitem(last=False)   # remove least recently used


# ---------------------------------------------------------------------------
# 2. BFS Level Order Traversal
# ---------------------------------------------------------------------------
# Given the root of a binary tree, return the level-order traversal
# as a list of lists (values grouped by level).
#
# Approach: BFS with deque
#   Process level by level using queue size as level boundary.
#
# Time:  O(n)
# Space: O(n)  (queue holds at most one level ≈ n/2 nodes at the leaves)
# ---------------------------------------------------------------------------

class TreeNode:
    def __init__(self, val: int = 0,
                 left: Optional[TreeNode] = None,
                 right: Optional[TreeNode] = None):
        self.val = val
        self.left = left
        self.right = right


def level_order(root: Optional[TreeNode]) -> List[List[int]]:
    if root is None:
        return []
    result: List[List[int]] = []
    queue: deque[TreeNode] = deque([root])
    while queue:
        level_size = len(queue)
        level: List[int] = []
        for _ in range(level_size):
            node = queue.popleft()
            level.append(node.val)
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
        result.append(level)
    return result


def build_tree(values: list) -> Optional[TreeNode]:
    """Build tree from level-order list, None = missing node."""
    if not values or values[0] is None:
        return None
    root = TreeNode(values[0])
    queue: deque[TreeNode] = deque([root])
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


# ---------------------------------------------------------------------------
# 3. Group Anagrams
# ---------------------------------------------------------------------------
# Given a list of strings, group anagrams together.
# Return groups in any order.
#
# Approach: Sort each string as hash key
#   Strings that are anagrams have the same sorted form.
#
# Time:  O(n * k log k)  where n = number of strings, k = max string length
# Space: O(n * k)
# ---------------------------------------------------------------------------

def group_anagrams(strs: List[str]) -> List[List[str]]:
    groups: dict = {}
    for s in strs:
        key = tuple(sorted(s))
        groups.setdefault(key, []).append(s)
    return list(groups.values())


# ---------------------------------------------------------------------------
# 4. Longest Substring Without Repeating Characters
# ---------------------------------------------------------------------------
# Given a string, find the length of the longest substring without
# repeating characters.
#
# Approach: Sliding window with hash map
#   Map each character to its last seen index.
#   Expand right; when duplicate found, jump left past the duplicate.
#
# Time:  O(n)
# Space: O(k)  where k = character set size
# ---------------------------------------------------------------------------

def length_of_longest_substring(s: str) -> int:
    last_seen: dict[str, int] = {}
    left = 0
    max_len = 0
    for right, ch in enumerate(s):
        if ch in last_seen and last_seen[ch] >= left:
            left = last_seen[ch] + 1
        last_seen[ch] = right
        max_len = max(max_len, right - left + 1)
    return max_len


# ---------------------------------------------------------------------------
# 5. Merge Intervals
# ---------------------------------------------------------------------------
# Given a list of intervals [start, end], merge all overlapping intervals
# and return the result.
#
# Approach: Sort by start, then linear scan
#   Sort intervals. For each interval, either extend the last merged interval
#   or start a new one.
#
# Time:  O(n log n)  dominated by sort
# Space: O(n)
# ---------------------------------------------------------------------------

def merge_intervals(intervals: List[List[int]]) -> List[List[int]]:
    if not intervals:
        return []
    intervals.sort(key=lambda x: x[0])
    merged = [intervals[0]]
    for start, end in intervals[1:]:
        if start <= merged[-1][1]:           # overlaps with last merged
            merged[-1][1] = max(merged[-1][1], end)
        else:
            merged.append([start, end])
    return merged


# ---------------------------------------------------------------------------
# Self-tests
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    # 1. LRU Cache
    cache = LRUCache(2)
    cache.put(1, 1)
    cache.put(2, 2)
    assert cache.get(1) == 1       # returns 1, marks 1 as MRU
    cache.put(3, 3)                # evicts key 2 (LRU)
    assert cache.get(2) == -1      # 2 was evicted
    cache.put(4, 4)                # evicts key 1
    assert cache.get(1) == -1
    assert cache.get(3) == 3
    assert cache.get(4) == 4
    print("LRU Cache: OK")

    # 2. BFS Level Order
    #     3
    #    / \
    #   9  20
    #     /  \
    #    15   7
    root = build_tree([3, 9, 20, None, None, 15, 7])
    assert level_order(root) == [[3], [9, 20], [15, 7]]
    assert level_order(None) == []
    assert level_order(build_tree([1])) == [[1]]
    print("BFS Level Order: OK")

    # 3. Group Anagrams
    result = group_anagrams(["eat", "tea", "tan", "ate", "nat", "bat"])
    # Sort inner and outer for comparison
    result_sorted = sorted([sorted(g) for g in result])
    assert result_sorted == [["ate", "eat", "tea"], ["bat"], ["nat", "tan"]]
    assert group_anagrams([""]) == [[""]]
    assert group_anagrams(["a"]) == [["a"]]
    print("Group Anagrams: OK")

    # 4. Longest Substring
    assert length_of_longest_substring("abcabcbb") == 3  # "abc"
    assert length_of_longest_substring("bbbbb") == 1      # "b"
    assert length_of_longest_substring("pwwkew") == 3     # "wke"
    assert length_of_longest_substring("") == 0
    assert length_of_longest_substring("dvdf") == 3       # "vdf"
    print("Longest Substring: OK")

    # 5. Merge Intervals
    assert merge_intervals([[1, 3], [2, 6], [8, 10], [15, 18]]) == \
           [[1, 6], [8, 10], [15, 18]]
    assert merge_intervals([[1, 4], [4, 5]]) == [[1, 5]]
    assert merge_intervals([[1, 4], [0, 4]]) == [[0, 4]]
    assert merge_intervals([]) == []
    print("Merge Intervals: OK")

    print("\nAll medium challenges passed.")
