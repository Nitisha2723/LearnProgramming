"""
Easy Coding Challenges
======================
1. Valid Parentheses
2. Reverse String
3. Fibonacci Sequence
4. Is Palindrome
5. Valid Anagram

All solutions include:
  - Problem statement
  - Approach explanation
  - Time and space complexity
  - Self-test block
"""

from typing import List
from collections import Counter


# ---------------------------------------------------------------------------
# 1. Valid Parentheses
# ---------------------------------------------------------------------------
# Given a string containing '(', ')', '{', '}', '[', ']', determine if it is
# valid. A string is valid if:
#   - Every open bracket is closed by the same type
#   - Open brackets must be closed in the correct order
#
# Approach: Stack
#   Push open brackets. On close bracket, check top of stack matches.
#
# Time:  O(n)
# Space: O(n)
# ---------------------------------------------------------------------------

def is_valid_parentheses(s: str) -> bool:
    stack: List[str] = []
    matching = {')': '(', '}': '{', ']': '['}
    for ch in s:
        if ch in '({[':
            stack.append(ch)
        else:
            if not stack or stack[-1] != matching[ch]:
                return False
            stack.pop()
    return len(stack) == 0


# ---------------------------------------------------------------------------
# 2. Reverse String
# ---------------------------------------------------------------------------
# Reverse a list of characters in-place.
# Do not allocate extra space — modify the input list directly.
#
# Approach: Two-pointer
#   Swap characters at left and right pointers, move towards centre.
#
# Time:  O(n)
# Space: O(1)
# ---------------------------------------------------------------------------

def reverse_string(s: List[str]) -> None:
    left, right = 0, len(s) - 1
    while left < right:
        s[left], s[right] = s[right], s[left]
        left += 1
        right -= 1


# ---------------------------------------------------------------------------
# 3. Fibonacci Sequence
# ---------------------------------------------------------------------------
# Return the nth Fibonacci number (0-indexed: fib(0)=0, fib(1)=1, fib(2)=1).
#
# Three solutions shown: recursive (naive), memoised, bottom-up.
#
# fib_dp:
# Time:  O(n)
# Space: O(1)  (only last two values kept)
# ---------------------------------------------------------------------------

def fib_naive(n: int) -> int:
    """Recursive — O(2^n). Do not use for large n."""
    if n <= 1:
        return n
    return fib_naive(n - 1) + fib_naive(n - 2)


def fib_memo(n: int, memo: dict = {}) -> int:
    """Memoised — O(n) time, O(n) space."""
    if n <= 1:
        return n
    if n not in memo:
        memo[n] = fib_memo(n - 1, memo) + fib_memo(n - 2, memo)
    return memo[n]


def fib_dp(n: int) -> int:
    """Bottom-up — O(n) time, O(1) space. Preferred."""
    if n <= 1:
        return n
    a, b = 0, 1
    for _ in range(2, n + 1):
        a, b = b, a + b
    return b


# ---------------------------------------------------------------------------
# 4. Is Palindrome
# ---------------------------------------------------------------------------
# Given a string, return True if it reads the same forwards and backwards,
# ignoring case and non-alphanumeric characters.
#
# Approach: Two-pointer
#   Skip non-alphanumeric, compare chars case-insensitively.
#
# Time:  O(n)
# Space: O(1)
# ---------------------------------------------------------------------------

def is_palindrome(s: str) -> bool:
    left, right = 0, len(s) - 1
    while left < right:
        while left < right and not s[left].isalnum():
            left += 1
        while left < right and not s[right].isalnum():
            right -= 1
        if s[left].lower() != s[right].lower():
            return False
        left += 1
        right -= 1
    return True


# ---------------------------------------------------------------------------
# 5. Valid Anagram
# ---------------------------------------------------------------------------
# Return True if t is an anagram of s (same characters, same counts).
#
# Approach: Counter comparison — O(n)
# Alternative: sort both strings and compare — O(n log n)
#
# Time:  O(n)
# Space: O(k)  where k = number of distinct characters
# ---------------------------------------------------------------------------

def is_anagram(s: str, t: str) -> bool:
    return Counter(s) == Counter(t)


# Alternative without Counter:
def is_anagram_v2(s: str, t: str) -> bool:
    if len(s) != len(t):
        return False
    counts: dict = {}
    for ch in s:
        counts[ch] = counts.get(ch, 0) + 1
    for ch in t:
        if ch not in counts or counts[ch] == 0:
            return False
        counts[ch] -= 1
    return True


# ---------------------------------------------------------------------------
# Self-tests
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    # 1. Valid Parentheses
    assert is_valid_parentheses("()") is True
    assert is_valid_parentheses("()[]{}") is True
    assert is_valid_parentheses("(]") is False
    assert is_valid_parentheses("([)]") is False
    assert is_valid_parentheses("{[]}") is True
    assert is_valid_parentheses("") is True
    print("Valid Parentheses: OK")

    # 2. Reverse String
    s = list("hello")
    reverse_string(s)
    assert s == list("olleh")
    s = list("Hannah")
    reverse_string(s)
    assert s == list("hannaH")
    print("Reverse String: OK")

    # 3. Fibonacci
    expected = [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]
    for i, exp in enumerate(expected):
        assert fib_dp(i) == exp, f"fib_dp({i}) = {fib_dp(i)}, expected {exp}"
    print("Fibonacci: OK")

    # 4. Is Palindrome
    assert is_palindrome("A man, a plan, a canal: Panama") is True
    assert is_palindrome("race a car") is False
    assert is_palindrome(" ") is True
    assert is_palindrome("Was it a car or a cat I saw?") is True
    print("Is Palindrome: OK")

    # 5. Valid Anagram
    assert is_anagram("anagram", "nagaram") is True
    assert is_anagram("rat", "car") is False
    assert is_anagram("listen", "silent") is True
    assert is_anagram_v2("anagram", "nagaram") is True
    assert is_anagram_v2("rat", "car") is False
    print("Valid Anagram: OK")

    print("\nAll easy challenges passed.")
