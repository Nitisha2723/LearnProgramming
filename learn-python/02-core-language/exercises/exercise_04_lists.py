"""
Exercise 04: List Algorithms
==============================

Three classic list manipulation problems.

─────────────────────────────────────────────────────────────
PROBLEM 1: Rotate List
─────────────────────────────────────────────────────────────
Write rotate_left(lst, k) that returns a new list with elements
rotated k positions to the left.

rotate_left([1, 2, 3, 4, 5], 2)  → [3, 4, 5, 1, 2]
rotate_left([1, 2, 3, 4, 5], 0)  → [1, 2, 3, 4, 5]
rotate_left([1, 2, 3, 4, 5], 5)  → [1, 2, 3, 4, 5]  (full rotation = no change)
rotate_left([1, 2, 3], 7)        → [2, 3, 1]  (7 % 3 = 1, so rotate by 1)
rotate_left([], 3)               → []

Hint: Slicing makes this elegant!

─────────────────────────────────────────────────────────────
PROBLEM 2: Remove Duplicates (Preserving Order)
─────────────────────────────────────────────────────────────
Write remove_duplicates(lst) that returns a new list with duplicates
removed, preserving the original order of first appearances.

remove_duplicates([1, 2, 3, 2, 4, 3, 5])  → [1, 2, 3, 4, 5]
remove_duplicates([3, 1, 4, 1, 5, 9, 2, 6, 5, 3])  → [3, 1, 4, 5, 9, 2, 6]
remove_duplicates([])                       → []
remove_duplicates([1, 1, 1, 1])            → [1]
remove_duplicates(["a", "b", "a", "c"])    → ["a", "b", "c"]

Note: sorted(set(lst)) loses order — this function must preserve order!

─────────────────────────────────────────────────────────────
PROBLEM 3: Two Sum
─────────────────────────────────────────────────────────────
Write two_sum(numbers, target) that returns a tuple of two indices
(i, j) where numbers[i] + numbers[j] == target, with i < j.
Return None if no such pair exists.

two_sum([2, 7, 11, 15], 9)    → (0, 1)   because 2 + 7 = 9
two_sum([3, 2, 4], 6)         → (1, 2)   because 2 + 4 = 6
two_sum([3, 3], 6)            → (0, 1)   because 3 + 3 = 6
two_sum([1, 2, 3], 10)        → None     (no pair sums to 10)

Start with the simple O(n²) approach (nested loops).
Bonus: Can you do it in O(n) using a dict?
"""

from typing import Optional


# ── YOUR CODE BELOW ──────────────────────────────────────────


def rotate_left(lst: list, k: int) -> list:
    """
    Return a new list with elements rotated k positions to the left.

    Args:
        lst: The original list (not modified).
        k: Number of positions to rotate left. Can be > len(lst).

    Returns:
        A new rotated list.

    Examples:
        rotate_left([1, 2, 3, 4, 5], 2) → [3, 4, 5, 1, 2]
    """
    # TODO: handle edge cases, then use slicing
    pass


def remove_duplicates(lst: list) -> list:
    """
    Return a new list with duplicates removed, preserving original order.

    Args:
        lst: The original list (not modified).

    Returns:
        A new list without duplicates.

    Examples:
        remove_duplicates([1, 2, 3, 2, 1]) → [1, 2, 3]
    """
    # TODO: use a set to track seen items, build result list
    pass


def two_sum(numbers: list[int], target: int) -> Optional[tuple[int, int]]:
    """
    Find two indices whose values sum to target.

    Args:
        numbers: List of integers.
        target: The target sum.

    Returns:
        A tuple (i, j) with i < j such that numbers[i] + numbers[j] == target,
        or None if no such pair exists.

    Examples:
        two_sum([2, 7, 11, 15], 9) → (0, 1)
    """
    # TODO: nested loop approach first, then bonus dict approach
    pass


# ── MAIN ──────────────────────────────────────────────────────

if __name__ == "__main__":
    print("Problem 1: Rotate List")
    print("-" * 30)
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
    print("-" * 30)
    dup_tests = [
        ([1, 2, 3, 2, 4, 3, 5], [1, 2, 3, 4, 5]),
        ([3, 1, 4, 1, 5, 9, 2, 6, 5, 3], [3, 1, 4, 5, 9, 2, 6]),
        ([], []),
        ([1, 1, 1, 1], [1]),
    ]
    for lst, expected in dup_tests:
        result = remove_duplicates(lst)
        status = "PASS" if result == expected else "FAIL"
        print(f"  [{status}] remove_duplicates({lst}) = {result}")

    print("\nProblem 3: Two Sum")
    print("-" * 30)
    two_sum_tests = [
        ([2, 7, 11, 15], 9, (0, 1)),
        ([3, 2, 4], 6, (1, 2)),
        ([3, 3], 6, (0, 1)),
        ([1, 2, 3], 10, None),
    ]
    for numbers, target, expected in two_sum_tests:
        result = two_sum(numbers, target)
        status = "PASS" if result == expected else "FAIL"
        print(f"  [{status}] two_sum({numbers}, {target}) = {result}")
