"""
Array Problems — 10 classic interview questions with Python solutions.

Each function includes:
- Problem statement in docstring
- Approach and complexity analysis
- Optimal implementation
"""

from typing import List, Optional
from collections import defaultdict


# ─────────────────────────────────────────────────────────────────────────────
# Problem 1: Two Sum
# ─────────────────────────────────────────────────────────────────────────────

def two_sum(nums: list[int], target: int) -> list[int]:
    """
    Find indices of two numbers that add up to target.

    Approach:
        Brute force O(n²): check every pair.
        Optimal O(n): hash map stores num→index; for each num check complement.

    Args:
        nums: List of integers.
        target: Target sum.

    Returns:
        Indices [i, j] such that nums[i] + nums[j] == target.

    Example:
        >>> two_sum([2, 7, 11, 15], 9)
        [0, 1]
        >>> two_sum([3, 2, 4], 6)
        [1, 2]
    """
    seen: dict[int, int] = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []


# ─────────────────────────────────────────────────────────────────────────────
# Problem 2: Best Time to Buy and Sell Stock
# ─────────────────────────────────────────────────────────────────────────────

def max_profit(prices: list[int]) -> int:
    """
    Find maximum profit from one buy-sell transaction.

    Approach:
        Single pass O(n): track minimum price seen; at each step compute
        profit vs current min; update global max.

    Args:
        prices: Daily stock prices.

    Returns:
        Maximum profit achievable (0 if no profit possible).

    Example:
        >>> max_profit([7, 1, 5, 3, 6, 4])
        5
        >>> max_profit([7, 6, 4, 3, 1])
        0
    """
    if not prices:
        return 0
    min_price = float("inf")
    max_profit_val = 0
    for price in prices:
        min_price = min(min_price, price)
        max_profit_val = max(max_profit_val, price - min_price)
    return max_profit_val


# ─────────────────────────────────────────────────────────────────────────────
# Problem 3: Contains Duplicate
# ─────────────────────────────────────────────────────────────────────────────

def contains_duplicate(nums: list[int]) -> bool:
    """
    Return True if any value appears at least twice.

    Approach:
        O(n) time, O(n) space: use a set; early exit on first duplicate.

    Example:
        >>> contains_duplicate([1, 2, 3, 1])
        True
        >>> contains_duplicate([1, 2, 3, 4])
        False
    """
    return len(nums) != len(set(nums))


# ─────────────────────────────────────────────────────────────────────────────
# Problem 4: Product of Array Except Self
# ─────────────────────────────────────────────────────────────────────────────

def product_except_self(nums: list[int]) -> list[int]:
    """
    Return array where output[i] = product of all elements except nums[i].
    No division allowed; O(n) time, O(1) extra space.

    Approach:
        Two passes: first build prefix products left-to-right,
        then multiply by suffix products right-to-left in-place.

    Example:
        >>> product_except_self([1, 2, 3, 4])
        [24, 12, 8, 6]
        >>> product_except_self([-1, 1, 0, -3, 3])
        [0, 0, 9, 0, 0]
    """
    n = len(nums)
    result = [1] * n

    # Left pass: result[i] = product of nums[0..i-1]
    prefix = 1
    for i in range(n):
        result[i] = prefix
        prefix *= nums[i]

    # Right pass: multiply by product of nums[i+1..n-1]
    suffix = 1
    for i in range(n - 1, -1, -1):
        result[i] *= suffix
        suffix *= nums[i]

    return result


# ─────────────────────────────────────────────────────────────────────────────
# Problem 5: Maximum Subarray (Kadane's Algorithm)
# ─────────────────────────────────────────────────────────────────────────────

def max_subarray(nums: list[int]) -> int:
    """
    Find the contiguous subarray with the largest sum.

    Approach:
        Kadane's Algorithm O(n): at each index, decide whether to extend
        the current subarray or start fresh.

    Example:
        >>> max_subarray([-2, 1, -3, 4, -1, 2, 1, -5, 4])
        6  # subarray [4, -1, 2, 1]
        >>> max_subarray([1])
        1
    """
    max_sum = current_sum = nums[0]
    for num in nums[1:]:
        current_sum = max(num, current_sum + num)
        max_sum = max(max_sum, current_sum)
    return max_sum


# ─────────────────────────────────────────────────────────────────────────────
# Problem 6: Maximum Product Subarray
# ─────────────────────────────────────────────────────────────────────────────

def max_product(nums: list[int]) -> int:
    """
    Find contiguous subarray with the largest product.

    Approach:
        O(n): track both max and min products (negatives can flip signs).

    Example:
        >>> max_product([2, 3, -2, 4])
        6  # [2, 3]
        >>> max_product([-2, 0, -1])
        0
    """
    result = max(nums)
    cur_min = cur_max = 1

    for num in nums:
        if num == 0:
            cur_min = cur_max = 1
            continue
        candidates = (num, cur_max * num, cur_min * num)
        cur_max = max(candidates)
        cur_min = min(candidates)
        result = max(result, cur_max)

    return result


# ─────────────────────────────────────────────────────────────────────────────
# Problem 7: Find Minimum in Rotated Sorted Array
# ─────────────────────────────────────────────────────────────────────────────

def find_min(nums: list[int]) -> int:
    """
    Find the minimum in a rotated sorted array (no duplicates).

    Approach:
        Binary search O(log n): if left half is sorted, minimum is nums[lo]
        or in the right half; otherwise minimum is in the left half.

    Example:
        >>> find_min([3, 4, 5, 1, 2])
        1
        >>> find_min([4, 5, 6, 7, 0, 1, 2])
        0
    """
    lo, hi = 0, len(nums) - 1
    while lo < hi:
        mid = (lo + hi) // 2
        if nums[mid] > nums[hi]:
            lo = mid + 1
        else:
            hi = mid
    return nums[lo]


# ─────────────────────────────────────────────────────────────────────────────
# Problem 8: Search in Rotated Sorted Array
# ─────────────────────────────────────────────────────────────────────────────

def search_rotated(nums: list[int], target: int) -> int:
    """
    Search for target in a rotated sorted array.

    Approach:
        Binary search O(log n): at each step, one half is always sorted;
        determine which half and narrow accordingly.

    Returns:
        Index of target, or -1 if not found.

    Example:
        >>> search_rotated([4, 5, 6, 7, 0, 1, 2], 0)
        4
        >>> search_rotated([4, 5, 6, 7, 0, 1, 2], 3)
        -1
    """
    lo, hi = 0, len(nums) - 1
    while lo <= hi:
        mid = (lo + hi) // 2
        if nums[mid] == target:
            return mid
        # Left half is sorted
        if nums[lo] <= nums[mid]:
            if nums[lo] <= target < nums[mid]:
                hi = mid - 1
            else:
                lo = mid + 1
        else:  # Right half is sorted
            if nums[mid] < target <= nums[hi]:
                lo = mid + 1
            else:
                hi = mid - 1
    return -1


# ─────────────────────────────────────────────────────────────────────────────
# Problem 9: 3Sum
# ─────────────────────────────────────────────────────────────────────────────

def three_sum(nums: list[int]) -> list[list[int]]:
    """
    Find all unique triplets that sum to zero.

    Approach:
        Sort + two-pointer O(n²): for each element, use two pointers on
        the remainder; skip duplicates to avoid repeat triplets.

    Example:
        >>> three_sum([-1, 0, 1, 2, -1, -4])
        [[-1, -1, 2], [-1, 0, 1]]
        >>> three_sum([0, 1, 1])
        []
    """
    nums.sort()
    result: list[list[int]] = []

    for i, num in enumerate(nums):
        if i > 0 and nums[i] == nums[i - 1]:
            continue  # skip duplicate i
        lo, hi = i + 1, len(nums) - 1
        while lo < hi:
            total = num + nums[lo] + nums[hi]
            if total == 0:
                result.append([num, nums[lo], nums[hi]])
                while lo < hi and nums[lo] == nums[lo + 1]:
                    lo += 1
                while lo < hi and nums[hi] == nums[hi - 1]:
                    hi -= 1
                lo += 1
                hi -= 1
            elif total < 0:
                lo += 1
            else:
                hi -= 1

    return result


# ─────────────────────────────────────────────────────────────────────────────
# Problem 10: Container With Most Water
# ─────────────────────────────────────────────────────────────────────────────

def max_water(height: list[int]) -> int:
    """
    Find the container formed by two lines that holds the most water.

    Approach:
        Two-pointer O(n): start at both ends; move the pointer with the
        shorter height inward (can only improve by getting a taller boundary).

    Example:
        >>> max_water([1, 8, 6, 2, 5, 4, 8, 3, 7])
        49
        >>> max_water([1, 1])
        1
    """
    lo, hi = 0, len(height) - 1
    best = 0
    while lo < hi:
        water = min(height[lo], height[hi]) * (hi - lo)
        best = max(best, water)
        if height[lo] < height[hi]:
            lo += 1
        else:
            hi -= 1
    return best


# ─────────────────────────────────────────────────────────────────────────────
# Quick self-test
# ─────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    assert two_sum([2, 7, 11, 15], 9) == [0, 1]
    assert max_profit([7, 1, 5, 3, 6, 4]) == 5
    assert contains_duplicate([1, 2, 3, 1]) is True
    assert product_except_self([1, 2, 3, 4]) == [24, 12, 8, 6]
    assert max_subarray([-2, 1, -3, 4, -1, 2, 1, -5, 4]) == 6
    assert max_product([2, 3, -2, 4]) == 6
    assert find_min([3, 4, 5, 1, 2]) == 1
    assert search_rotated([4, 5, 6, 7, 0, 1, 2], 0) == 4
    assert three_sum([-1, 0, 1, 2, -1, -4]) == [[-1, -1, 2], [-1, 0, 1]]
    assert max_water([1, 8, 6, 2, 5, 4, 8, 3, 7]) == 49
    print("All array problems passed!")
