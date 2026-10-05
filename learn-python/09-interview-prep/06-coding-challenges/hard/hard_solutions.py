"""
Hard Coding Challenges
======================
1. Merge K Sorted Lists
2. Word Ladder
3. Trapping Rain Water

All solutions include:
  - Problem statement
  - Approach explanation
  - Time and space complexity
  - Self-test block
"""

from __future__ import annotations
from typing import List, Optional
import heapq
from collections import deque


# ---------------------------------------------------------------------------
# Helper: Linked List Node
# ---------------------------------------------------------------------------

class ListNode:
    def __init__(self, val: int = 0, next: Optional[ListNode] = None):
        self.val = val
        self.next = next

    def __lt__(self, other: ListNode) -> bool:
        """Required so heapq can compare ListNodes on val."""
        return self.val < other.val


def from_list(values: list) -> Optional[ListNode]:
    """Build linked list from Python list."""
    dummy = ListNode()
    cur = dummy
    for v in values:
        cur.next = ListNode(v)
        cur = cur.next
    return dummy.next


def to_list(node: Optional[ListNode]) -> list:
    """Convert linked list to Python list."""
    result = []
    while node:
        result.append(node.val)
        node = node.next
    return result


# ---------------------------------------------------------------------------
# 1. Merge K Sorted Lists
# ---------------------------------------------------------------------------
# Given k sorted linked lists, merge them into one sorted linked list.
#
# Approach: Min-heap
#   Push the head of each list onto the heap (val, list_index, node).
#   Pop min, append to result, push next node from the same list.
#
# Why not merge two at a time? O(kN) — each of k-1 merges touches all N nodes.
# Heap approach: O(N log k) — every node touches the heap once (log k ops).
#
# Time:  O(N log k)  where N = total nodes, k = number of lists
# Space: O(k)        heap holds at most one node per list
# ---------------------------------------------------------------------------

def merge_k_sorted_lists(lists: List[Optional[ListNode]]) -> Optional[ListNode]:
    heap: list = []
    # Push initial heads. Use list index as tiebreaker to avoid comparing nodes
    # when vals are equal (though __lt__ on ListNode handles this).
    for node in lists:
        if node is not None:
            heapq.heappush(heap, node)

    dummy = ListNode()
    cur = dummy
    while heap:
        node = heapq.heappop(heap)
        cur.next = node
        cur = cur.next
        if node.next is not None:
            heapq.heappush(heap, node.next)
    return dummy.next


# ---------------------------------------------------------------------------
# 2. Word Ladder
# ---------------------------------------------------------------------------
# Given beginWord, endWord, and a wordList, return the number of words in the
# shortest transformation sequence from beginWord to endWord, where each step
# changes exactly one letter and each intermediate word must be in wordList.
# Return 0 if no such sequence exists.
#
# Example: "hit" → "hot" → "dot" → "dog" → "cog"  returns 5
#
# Approach: BFS
#   Each word is a node. Two words are connected if they differ by one letter.
#   BFS guarantees the shortest path.
#
# Optimisation: pre-build a pattern→words map so we don't compare every
# word against every word. Pattern "h*t" maps to ["hit", "hot"]. For each
# word, generate all patterns (replacing each position with '*').
#
# Time:  O(M^2 * N)  M = word length, N = words in list
# Space: O(M^2 * N)
# ---------------------------------------------------------------------------

def word_ladder(beginWord: str, endWord: str, wordList: List[str]) -> int:
    word_set = set(wordList)
    if endWord not in word_set:
        return 0

    # Build pattern map: "h*t" → ["hit", "hot"]
    from collections import defaultdict
    pattern_map: dict = defaultdict(list)
    for word in wordList:
        for i in range(len(word)):
            pattern = word[:i] + '*' + word[i + 1:]
            pattern_map[pattern].append(word)

    queue: deque = deque([(beginWord, 1)])
    visited = {beginWord}

    while queue:
        word, length = queue.popleft()
        for i in range(len(word)):
            pattern = word[:i] + '*' + word[i + 1:]
            for neighbour in pattern_map[pattern]:
                if neighbour == endWord:
                    return length + 1
                if neighbour not in visited:
                    visited.add(neighbour)
                    queue.append((neighbour, length + 1))
    return 0


# Bidirectional BFS (faster in practice — prunes the search space)
def word_ladder_bidir(beginWord: str, endWord: str, wordList: List[str]) -> int:
    word_set = set(wordList)
    if endWord not in word_set:
        return 0

    from collections import defaultdict
    pattern_map: dict = defaultdict(list)
    for word in wordList:
        for i in range(len(word)):
            pattern = word[:i] + '*' + word[i + 1:]
            pattern_map[pattern].append(word)

    begin_visited = {beginWord: 1}
    end_visited = {endWord: 1}

    def expand(current_level: dict, other_level: dict) -> int:
        # Expand the smaller frontier for efficiency
        for word in list(current_level.keys()):
            for i in range(len(word)):
                pattern = word[:i] + '*' + word[i + 1:]
                for neighbour in pattern_map[pattern]:
                    if neighbour in other_level:
                        return current_level[word] + other_level[neighbour]
                    if neighbour not in current_level:
                        current_level[neighbour] = current_level[word] + 1
        return -1

    while begin_visited and end_visited:
        # Always expand the smaller frontier
        if len(begin_visited) <= len(end_visited):
            result = expand(begin_visited, end_visited)
        else:
            result = expand(end_visited, begin_visited)
        if result != -1:
            return result
    return 0


# ---------------------------------------------------------------------------
# 3. Trapping Rain Water
# ---------------------------------------------------------------------------
# Given n non-negative integers representing an elevation map where the width
# of each bar is 1, compute how much water it can trap after raining.
#
# Example: [0,1,0,2,1,0,1,3,2,1,2,1] → 6
#
# Approach A: Two-pointer O(n) time, O(1) space
#   left pointer starts at 0, right at n-1.
#   Track left_max and right_max as we move inward.
#   Water above position i = min(left_max, right_max) - height[i].
#   If left_max < right_max: the left side is the bottleneck; process left.
#   Else: process right.
#
# Approach B: Monotonic stack O(n) time, O(n) space
#   Useful for visualising the "container" formed between bars.
#
# Time:  O(n)
# Space: O(1)  (two-pointer approach)
# ---------------------------------------------------------------------------

def trap_rain_water(height: List[int]) -> int:
    if not height:
        return 0

    left, right = 0, len(height) - 1
    left_max = right_max = 0
    water = 0

    while left < right:
        if height[left] <= height[right]:
            if height[left] >= left_max:
                left_max = height[left]
            else:
                water += left_max - height[left]
            left += 1
        else:
            if height[right] >= right_max:
                right_max = height[right]
            else:
                water += right_max - height[right]
            right -= 1
    return water


# Monotonic stack approach (alternative, more intuitive for some)
def trap_rain_water_stack(height: List[int]) -> int:
    stack: List[int] = []   # indices
    water = 0
    for i, h in enumerate(height):
        while stack and height[stack[-1]] < h:
            bottom = stack.pop()
            if not stack:
                break
            left = stack[-1]
            width = i - left - 1
            bounded_height = min(height[left], h) - height[bottom]
            water += width * bounded_height
        stack.append(i)
    return water


# ---------------------------------------------------------------------------
# Self-tests
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    # 1. Merge K Sorted Lists
    lists = [
        from_list([1, 4, 5]),
        from_list([1, 3, 4]),
        from_list([2, 6]),
    ]
    merged = merge_k_sorted_lists(lists)
    assert to_list(merged) == [1, 1, 2, 3, 4, 4, 5, 6]

    lists2 = [from_list([]), from_list([]), from_list([])]
    assert to_list(merge_k_sorted_lists(lists2)) == []

    lists3 = [from_list([1])]
    assert to_list(merge_k_sorted_lists(lists3)) == [1]
    print("Merge K Sorted Lists: OK")

    # 2. Word Ladder
    assert word_ladder("hit", "cog", ["hot", "dot", "dog", "lot", "log", "cog"]) == 5
    assert word_ladder("hit", "cog", ["hot", "dot", "dog", "lot", "log"]) == 0
    assert word_ladder("a", "c", ["a", "b", "c"]) == 2

    assert word_ladder_bidir("hit", "cog", ["hot", "dot", "dog", "lot", "log", "cog"]) == 5
    assert word_ladder_bidir("hit", "cog", ["hot", "dot", "dog", "lot", "log"]) == 0
    print("Word Ladder: OK")

    # 3. Trapping Rain Water
    assert trap_rain_water([0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]) == 6
    assert trap_rain_water([4, 2, 0, 3, 2, 5]) == 9
    assert trap_rain_water([]) == 0
    assert trap_rain_water([3]) == 0
    assert trap_rain_water([3, 3]) == 0

    assert trap_rain_water_stack([0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]) == 6
    assert trap_rain_water_stack([4, 2, 0, 3, 2, 5]) == 9
    print("Trapping Rain Water: OK")

    print("\nAll hard challenges passed.")
