"""
Dynamic Programming Problems — 8 classic problems with Python solutions.

Covers: memoisation (top-down), tabulation (bottom-up), and 1D/2D DP.
"""

from functools import lru_cache
from typing import List


# ─────────────────────────────────────────────────────────────────────────────
# Problem 1: Fibonacci Number
# ─────────────────────────────────────────────────────────────────────────────

def fib(n: int) -> int:
    """
    Return the nth Fibonacci number.

    Approach:
        Naive recursion O(2^n) -> memoised O(n) -> iterative DP O(n), O(1).

    Example:
        >>> fib(10)
        55
    """
    if n <= 1:
        return n
    a, b = 0, 1
    for _ in range(2, n + 1):
        a, b = b, a + b
    return b


# ─────────────────────────────────────────────────────────────────────────────
# Problem 2: Climbing Stairs
# ─────────────────────────────────────────────────────────────────────────────

def climb_stairs(n: int) -> int:
    """
    Count ways to climb n stairs taking 1 or 2 steps at a time.

    Approach:
        Same as Fibonacci: ways(n) = ways(n-1) + ways(n-2).
        O(n) time, O(1) space.

    Example:
        >>> climb_stairs(3)
        3  # (1+1+1), (1+2), (2+1)
        >>> climb_stairs(5)
        8
    """
    if n <= 2:
        return n
    a, b = 1, 2
    for _ in range(3, n + 1):
        a, b = b, a + b
    return b


# ─────────────────────────────────────────────────────────────────────────────
# Problem 3: Coin Change
# ─────────────────────────────────────────────────────────────────────────────

def coin_change(coins: list[int], amount: int) -> int:
    """
    Find the fewest coins needed to make up the amount.

    Approach:
        Bottom-up DP O(amount * coins): dp[i] = min coins for amount i.

    Returns:
        Minimum coins, or -1 if impossible.

    Example:
        >>> coin_change([1, 5, 6, 9], 11)
        2  # 5 + 6
        >>> coin_change([2], 3)
        -1
    """
    dp = [float("inf")] * (amount + 1)
    dp[0] = 0
    for i in range(1, amount + 1):
        for coin in coins:
            if coin <= i:
                dp[i] = min(dp[i], dp[i - coin] + 1)
    return dp[amount] if dp[amount] != float("inf") else -1


# ─────────────────────────────────────────────────────────────────────────────
# Problem 4: Longest Common Subsequence
# ─────────────────────────────────────────────────────────────────────────────

def lcs(text1: str, text2: str) -> int:
    """
    Find the length of the longest common subsequence.

    Approach:
        2D DP O(m*n): dp[i][j] = LCS of text1[:i] and text2[:j].

    Example:
        >>> lcs("abcde", "ace")
        3  # "ace"
        >>> lcs("abc", "abc")
        3
        >>> lcs("abc", "def")
        0
    """
    m, n = len(text1), len(text2)
    dp = [[0] * (n + 1) for _ in range(m + 1)]

    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if text1[i - 1] == text2[j - 1]:
                dp[i][j] = dp[i-1][j-1] + 1
            else:
                dp[i][j] = max(dp[i-1][j], dp[i][j-1])

    return dp[m][n]


# ─────────────────────────────────────────────────────────────────────────────
# Problem 5: 0/1 Knapsack
# ─────────────────────────────────────────────────────────────────────────────

def knapsack(weights: list[int], values: list[int], capacity: int) -> int:
    """
    Maximise total value of items in a knapsack (each item used at most once).

    Approach:
        2D DP O(n * capacity): dp[i][w] = max value using first i items with
        weight limit w.

    Example:
        >>> knapsack([1, 3, 4, 5], [1, 4, 5, 7], 7)
        9  # items with weight 3 (val 4) and weight 4 (val 5)
    """
    n = len(weights)
    dp = [[0] * (capacity + 1) for _ in range(n + 1)]

    for i in range(1, n + 1):
        for w in range(capacity + 1):
            dp[i][w] = dp[i-1][w]
            if weights[i-1] <= w:
                dp[i][w] = max(dp[i][w], dp[i-1][w - weights[i-1]] + values[i-1])

    return dp[n][capacity]


# ─────────────────────────────────────────────────────────────────────────────
# Problem 6: Longest Increasing Subsequence
# ─────────────────────────────────────────────────────────────────────────────

def lis(nums: list[int]) -> int:
    """
    Find the length of the longest strictly increasing subsequence.

    Approach:
        O(n log n) with patience sorting (binary search):
        maintain a list `tails` where tails[i] = smallest tail of all
        increasing subsequences of length i+1.

    Example:
        >>> lis([10, 9, 2, 5, 3, 7, 101, 18])
        4  # [2, 3, 7, 101]
        >>> lis([0, 1, 0, 3, 2, 3])
        4
    """
    import bisect
    tails: list[int] = []
    for num in nums:
        pos = bisect.bisect_left(tails, num)
        if pos == len(tails):
            tails.append(num)
        else:
            tails[pos] = num
    return len(tails)


# ─────────────────────────────────────────────────────────────────────────────
# Problem 7: Word Break
# ─────────────────────────────────────────────────────────────────────────────

def word_break(s: str, word_dict: list[str]) -> bool:
    """
    Determine if s can be segmented into words from word_dict.

    Approach:
        DP O(n^2 * m): dp[i] = True if s[:i] can be broken.

    Example:
        >>> word_break("leetcode", ["leet", "code"])
        True
        >>> word_break("applepenapple", ["apple", "pen"])
        True
        >>> word_break("catsandog", ["cats", "dog", "sand", "and", "cat"])
        False
    """
    word_set = set(word_dict)
    n = len(s)
    dp = [False] * (n + 1)
    dp[0] = True

    for i in range(1, n + 1):
        for j in range(i):
            if dp[j] and s[j:i] in word_set:
                dp[i] = True
                break

    return dp[n]


# ─────────────────────────────────────────────────────────────────────────────
# Problem 8: House Robber
# ─────────────────────────────────────────────────────────────────────────────

def rob(nums: list[int]) -> int:
    """
    Maximum money you can rob without robbing two adjacent houses.

    Approach:
        O(n), O(1): at each house either skip it or rob it (skip previous).
        dp[i] = max(dp[i-1], dp[i-2] + nums[i])

    Example:
        >>> rob([2, 7, 9, 3, 1])
        12  # rob houses 1, 3, 5 (0-indexed): 2 + 9 + 1 = 12
        >>> rob([1, 2, 3, 1])
        4
    """
    if not nums:
        return 0
    if len(nums) == 1:
        return nums[0]

    prev2, prev1 = nums[0], max(nums[0], nums[1])
    for i in range(2, len(nums)):
        curr = max(prev1, prev2 + nums[i])
        prev2, prev1 = prev1, curr

    return prev1


# ─────────────────────────────────────────────────────────────────────────────
# Quick self-test
# ─────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    assert fib(10) == 55
    assert climb_stairs(5) == 8
    assert coin_change([1, 5, 6, 9], 11) == 2
    assert lcs("abcde", "ace") == 3
    assert knapsack([1, 3, 4, 5], [1, 4, 5, 7], 7) == 9
    assert lis([10, 9, 2, 5, 3, 7, 101, 18]) == 4
    assert word_break("leetcode", ["leet", "code"]) is True
    assert rob([2, 7, 9, 3, 1]) == 12
    print("All DP problems passed!")
