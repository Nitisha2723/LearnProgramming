"""
Solution: Exercise 04 — List Algorithms

Three classic list problems with O(n) bonus solutions.
"""

from typing import Optional


# ─────────────────────────────────────────────────────────────
# PROBLEM 1: Rotate List
# ─────────────────────────────────────────────────────────────

def rotate_left(lst: list, k: int) -> list:
    """Return a new list with elements rotated k positions to the left."""
    if not lst:
        return []

    # Use modulo to handle k > len(lst)
    k = k % len(lst)

    if k == 0:
        return lst[:]          # return a copy (no rotation needed)

    # Slicing: take [k:] as the front, [0:k] as the tail
    return lst[k:] + lst[:k]


# ─────────────────────────────────────────────────────────────
# PROBLEM 2: Remove Duplicates
# ─────────────────────────────────────────────────────────────

def remove_duplicates(lst: list) -> list:
    """Return a new list with duplicates removed, preserving original order."""
    seen = set()      # O(1) average lookup
    result = []

    for item in lst:
        if item not in seen:
            seen.add(item)
            result.append(item)

    return result


# One-liner using dict.fromkeys() (preserves insertion order in Python 3.7+):
def remove_duplicates_v2(lst: list) -> list:
    """Version 2: dict.fromkeys preserves order and removes duplicates."""
    return list(dict.fromkeys(lst))


# ─────────────────────────────────────────────────────────────
# PROBLEM 3: Two Sum
# ─────────────────────────────────────────────────────────────

def two_sum(numbers: list[int], target: int) -> Optional[tuple[int, int]]:
    """
    Find two indices whose values sum to target.

    O(n²) approach: try every pair.
    """
    for i in range(len(numbers)):
        for j in range(i + 1, len(numbers)):
            if numbers[i] + numbers[j] == target:
                return (i, j)
    return None


def two_sum_fast(numbers: list[int], target: int) -> Optional[tuple[int, int]]:
    """
    Find two indices whose values sum to target.

    O(n) approach using a dict (hash map):
    - For each number, check if (target - number) was seen before.
    - Store each number's index in the dict as we scan.
    """
    seen = {}   # maps value → index

    for i, num in enumerate(numbers):
        complement = target - num
        if complement in seen:
            return (seen[complement], i)
        seen[num] = i

    return None


# ── MAIN ──────────────────────────────────────────────────────

if __name__ == "__main__":
    print("Problem 1: Rotate List")
    print("-" * 40)
    rotate_tests = [
        ([1, 2, 3, 4, 5], 2, [3, 4, 5, 1, 2]),
        ([1, 2, 3, 4, 5], 0, [1, 2, 3, 4, 5]),
        ([1, 2, 3, 4, 5], 5, [1, 2, 3, 4, 5]),
        ([1, 2, 3], 7, [2, 3, 1]),
        ([], 3, []),
    ]
    for lst, k, expected in rotate_tests:
        result = rotate_left(lst, k)
        status = "PASS" if result == expected else "FAIL"
        print(f"  [{status}] rotate_left({lst}, {k}) = {result}")

    print("\nProblem 2: Remove Duplicates")
    print("-" * 40)
    dup_tests = [
        ([1, 2, 3, 2, 4, 3, 5], [1, 2, 3, 4, 5]),
        ([3, 1, 4, 1, 5, 9, 2, 6, 5, 3], [3, 1, 4, 5, 9, 2, 6]),
        ([], []),
        ([1, 1, 1, 1], [1]),
    ]
    for lst, expected in dup_tests:
        result = remove_duplicates(lst)
        result2 = remove_duplicates_v2(lst)
        status = "PASS" if result == expected else "FAIL"
        print(f"  [{status}] remove_duplicates({lst}) = {result}  (v2: {result2})")

    print("\nProblem 3: Two Sum")
    print("-" * 40)
    two_sum_tests = [
        ([2, 7, 11, 15], 9, (0, 1)),
        ([3, 2, 4], 6, (1, 2)),
        ([3, 3], 6, (0, 1)),
        ([1, 2, 3], 10, None),
    ]
    for numbers, target, expected in two_sum_tests:
        r1 = two_sum(numbers, target)
        r2 = two_sum_fast(numbers, target)
        status = "PASS" if r1 == expected else "FAIL"
        fast_status = "PASS" if r2 == expected else "FAIL"
        print(f"  [{status}] two_sum({numbers}, {target}) = {r1}")
        print(f"  [{fast_status}] two_sum_fast({numbers}, {target}) = {r2}")
