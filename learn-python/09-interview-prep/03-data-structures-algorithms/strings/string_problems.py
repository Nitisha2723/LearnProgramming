"""
String Problems — 10 classic interview questions with Python solutions.
"""

from collections import Counter, defaultdict
from typing import List


# ─────────────────────────────────────────────────────────────────────────────
# Problem 1: Valid Anagram
# ─────────────────────────────────────────────────────────────────────────────

def is_anagram(s: str, t: str) -> bool:
    """
    Determine if t is an anagram of s (same characters, same counts).

    Approach:
        O(n): compare Counter objects (character frequency maps).

    Example:
        >>> is_anagram("anagram", "nagaram")
        True
        >>> is_anagram("rat", "car")
        False
    """
    return Counter(s) == Counter(t)


# ─────────────────────────────────────────────────────────────────────────────
# Problem 2: Valid Palindrome
# ─────────────────────────────────────────────────────────────────────────────

def is_palindrome(s: str) -> bool:
    """
    Check if s is a palindrome considering only alphanumeric chars.

    Approach:
        O(n): two pointers, skip non-alphanumeric characters.

    Example:
        >>> is_palindrome("A man, a plan, a canal: Panama")
        True
        >>> is_palindrome("race a car")
        False
    """
    lo, hi = 0, len(s) - 1
    while lo < hi:
        while lo < hi and not s[lo].isalnum():
            lo += 1
        while lo < hi and not s[hi].isalnum():
            hi -= 1
        if s[lo].lower() != s[hi].lower():
            return False
        lo += 1
        hi -= 1
    return True


# ─────────────────────────────────────────────────────────────────────────────
# Problem 3: Longest Substring Without Repeating Characters
# ─────────────────────────────────────────────────────────────────────────────

def length_of_longest_substring(s: str) -> int:
    """
    Find the length of the longest substring without repeating characters.

    Approach:
        Sliding window O(n): maintain a window [lo, hi]; if s[hi] is seen,
        shrink from left until it's no longer in the window.

    Example:
        >>> length_of_longest_substring("abcabcbb")
        3
        >>> length_of_longest_substring("bbbbb")
        1
        >>> length_of_longest_substring("pwwkew")
        3
    """
    char_index: dict[str, int] = {}
    lo = max_len = 0
    for hi, char in enumerate(s):
        if char in char_index and char_index[char] >= lo:
            lo = char_index[char] + 1
        char_index[char] = hi
        max_len = max(max_len, hi - lo + 1)
    return max_len


# ─────────────────────────────────────────────────────────────────────────────
# Problem 4: Longest Repeating Character Replacement
# ─────────────────────────────────────────────────────────────────────────────

def character_replacement(s: str, k: int) -> int:
    """
    Find the longest substring you can get by replacing at most k characters
    so all characters in the window are the same.

    Approach:
        Sliding window O(n): window is valid if (window_size - max_count) <= k.

    Example:
        >>> character_replacement("ABAB", 2)
        4
        >>> character_replacement("AABABBA", 1)
        4
    """
    count: dict[str, int] = defaultdict(int)
    lo = max_count = result = 0

    for hi in range(len(s)):
        count[s[hi]] += 1
        max_count = max(max_count, count[s[hi]])

        if (hi - lo + 1) - max_count > k:
            count[s[lo]] -= 1
            lo += 1

        result = max(result, hi - lo + 1)

    return result


# ─────────────────────────────────────────────────────────────────────────────
# Problem 5: Minimum Window Substring
# ─────────────────────────────────────────────────────────────────────────────

def min_window(s: str, t: str) -> str:
    """
    Find the minimum window in s that contains all characters in t.

    Approach:
        Sliding window O(n+m): expand right until all t chars covered,
        then shrink left; track best window.

    Example:
        >>> min_window("ADOBECODEBANC", "ABC")
        "BANC"
        >>> min_window("a", "a")
        "a"
        >>> min_window("a", "aa")
        ""
    """
    if not t:
        return ""

    need = Counter(t)
    missing = len(t)
    lo = result_lo = result_hi = 0
    result = ""

    for hi, char in enumerate(s, 1):
        if need[char] > 0:
            missing -= 1
        need[char] -= 1

        if missing == 0:
            while need[s[lo]] < 0:
                need[s[lo]] += 1
                lo += 1
            if not result or hi - lo < len(result):
                result = s[lo:hi]
            need[s[lo]] += 1
            missing += 1
            lo += 1

    return result


# ─────────────────────────────────────────────────────────────────────────────
# Problem 6: Group Anagrams
# ─────────────────────────────────────────────────────────────────────────────

def group_anagrams(strs: list[str]) -> list[list[str]]:
    """
    Group strings that are anagrams of each other.

    Approach:
        O(n * k log k): sort each string as a key; group by key.

    Example:
        >>> group_anagrams(["eat","tea","tan","ate","nat","bat"])
        [["eat","tea","ate"], ["tan","nat"], ["bat"]]
    """
    groups: dict[str, list[str]] = defaultdict(list)
    for word in strs:
        key = "".join(sorted(word))
        groups[key].append(word)
    return list(groups.values())


# ─────────────────────────────────────────────────────────────────────────────
# Problem 7: Encode and Decode Strings
# ─────────────────────────────────────────────────────────────────────────────

def encode(strs: list[str]) -> str:
    """
    Encode a list of strings to a single string.

    Format: "length#string" for each string.

    Example:
        >>> encode(["neet", "code", "love", "you"])
        "4#neet4#code4#love3#you"
    """
    return "".join(f"{len(s)}#{s}" for s in strs)


def decode(encoded: str) -> list[str]:
    """
    Decode a string encoded with encode().

    Example:
        >>> decode("4#neet4#code4#love3#you")
        ["neet", "code", "love", "you"]
    """
    result = []
    i = 0
    while i < len(encoded):
        j = encoded.index("#", i)
        length = int(encoded[i:j])
        result.append(encoded[j + 1: j + 1 + length])
        i = j + 1 + length
    return result


# ─────────────────────────────────────────────────────────────────────────────
# Problem 8: Palindromic Substrings
# ─────────────────────────────────────────────────────────────────────────────

def count_palindromic_substrings(s: str) -> int:
    """
    Count the number of palindromic substrings.

    Approach:
        O(n²): expand around each center (n odd centers + n-1 even centers).

    Example:
        >>> count_palindromic_substrings("abc")
        3
        >>> count_palindromic_substrings("aaa")
        6
    """
    def expand(lo: int, hi: int) -> int:
        count = 0
        while lo >= 0 and hi < len(s) and s[lo] == s[hi]:
            count += 1
            lo -= 1
            hi += 1
        return count

    total = 0
    for i in range(len(s)):
        total += expand(i, i)      # odd length
        total += expand(i, i + 1)  # even length
    return total


# ─────────────────────────────────────────────────────────────────────────────
# Problem 9: Longest Palindromic Substring
# ─────────────────────────────────────────────────────────────────────────────

def longest_palindrome(s: str) -> str:
    """
    Find the longest palindromic substring.

    Approach:
        O(n²) expand-around-center: try each position as center.

    Example:
        >>> longest_palindrome("babad")
        "bab"
        >>> longest_palindrome("cbbd")
        "bb"
    """
    start = end = 0

    def expand(lo: int, hi: int) -> None:
        nonlocal start, end
        while lo >= 0 and hi < len(s) and s[lo] == s[hi]:
            if hi - lo > end - start:
                start, end = lo, hi
            lo -= 1
            hi += 1

    for i in range(len(s)):
        expand(i, i)      # odd
        expand(i, i + 1)  # even

    return s[start: end + 1]


# ─────────────────────────────────────────────────────────────────────────────
# Problem 10: Valid Parentheses
# ─────────────────────────────────────────────────────────────────────────────

def is_valid_parentheses(s: str) -> bool:
    """
    Check if parentheses string is valid (each open has matching close).

    Approach:
        O(n): stack-based matching.

    Example:
        >>> is_valid_parentheses("()")
        True
        >>> is_valid_parentheses("()[]{}")
        True
        >>> is_valid_parentheses("(]")
        False
        >>> is_valid_parentheses("([)]")
        False
    """
    stack: list[str] = []
    matching = {")": "(", "]": "[", "}": "{"}
    for char in s:
        if char in matching:
            if not stack or stack[-1] != matching[char]:
                return False
            stack.pop()
        else:
            stack.append(char)
    return len(stack) == 0


# ─────────────────────────────────────────────────────────────────────────────
# Quick self-test
# ─────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    assert is_anagram("anagram", "nagaram") is True
    assert is_palindrome("A man, a plan, a canal: Panama") is True
    assert length_of_longest_substring("abcabcbb") == 3
    assert character_replacement("ABAB", 2) == 4
    assert min_window("ADOBECODEBANC", "ABC") == "BANC"
    groups = group_anagrams(["eat","tea","tan","ate","nat","bat"])
    assert len(groups) == 3
    encoded = encode(["neet", "code"])
    assert decode(encoded) == ["neet", "code"]
    assert count_palindromic_substrings("aaa") == 6
    assert longest_palindrome("babad") in ("bab", "aba")
    assert is_valid_parentheses("()[]{}")
    print("All string problems passed!")
