"""
Exercise 01: Frequency Counter
==============================

Build functions to analyze text and count frequencies of characters and words.

Learning goals:
  - Counter from collections
  - defaultdict patterns
  - Dict comprehensions
  - Sorting by value

Complete the functions below. Each has a docstring describing what it should do.
Run the tests at the bottom to check your work.
"""

from collections import Counter, defaultdict
from typing import Optional


def count_characters(text: str) -> dict[str, int]:
    """
    Count the frequency of each character in text (case-insensitive).
    Ignore spaces and punctuation — count only letters and digits.

    Args:
        text: The input string

    Returns:
        A dict mapping each character (lowercase) to its count.

    Example:
        count_characters("Hello, World!") == {"h": 1, "e": 1, "l": 3,
                                               "o": 2, "w": 1, "r": 1, "d": 1}
    """
    # YOUR CODE HERE
    pass


def count_words(text: str) -> dict[str, int]:
    """
    Count the frequency of each word in text (case-insensitive).
    Words are split on whitespace. Strip leading/trailing punctuation from each word.

    Args:
        text: The input string

    Returns:
        A dict mapping each word (lowercase) to its count.

    Example:
        count_words("The cat sat on the mat.") ==
            {"the": 2, "cat": 1, "sat": 1, "on": 1, "mat": 1}
    """
    # YOUR CODE HERE
    pass


def top_n_words(text: str, n: int) -> list[tuple[str, int]]:
    """
    Return the top N most frequent words in text, sorted by frequency (descending).
    For ties, sort alphabetically.

    Args:
        text: The input string
        n: Number of top words to return

    Returns:
        List of (word, count) tuples, sorted by count desc then word asc.

    Example:
        top_n_words("to be or not to be", 2) == [("be", 2), ("to", 2)]
    """
    # YOUR CODE HERE
    pass


def character_histogram(text: str) -> str:
    """
    Return a visual histogram of character frequencies.
    Only include letters (a-z), sorted alphabetically.
    Use '█' to represent each occurrence.

    Args:
        text: The input string

    Returns:
        A multi-line string with one line per character.

    Example:
        character_histogram("hello") →
        "e █ (1)
         h █ (1)
         l ██ (2)
         o █ (1)"
    """
    # YOUR CODE HERE
    pass


def find_unique_words(*texts: str) -> dict[str, set[str]]:
    """
    Given multiple text strings, find which words appear in each text
    but NOT in any of the others.

    Args:
        *texts: Variable number of text strings

    Returns:
        A dict where keys are text indices ("text_0", "text_1", etc.)
        and values are sets of words unique to that text.

    Example:
        find_unique_words("I love python", "I love java") ==
            {"text_0": {"python"}, "text_1": {"java"}}
    """
    # YOUR CODE HERE
    pass


def group_by_frequency(text: str) -> dict[int, list[str]]:
    """
    Group words by their frequency count.

    Args:
        text: The input string

    Returns:
        A dict mapping frequency → list of words with that frequency (sorted).

    Example:
        group_by_frequency("the cat sat on the mat") ==
            {1: ["cat", "mat", "on", "sat"], 2: ["the"]}
    """
    # YOUR CODE HERE
    pass


# =============================================================================
# TESTS — run this file to check your solutions
# =============================================================================

def test_count_characters():
    result = count_characters("Hello, World!")
    assert result["l"] == 3, f"Expected 3 'l's, got {result.get('l')}"
    assert result["o"] == 2, f"Expected 2 'o's, got {result.get('o')}"
    assert " " not in result, "Spaces should not be counted"
    assert "," not in result, "Punctuation should not be counted"
    assert all(k == k.lower() for k in result), "All keys should be lowercase"
    print("  count_characters: PASSED")


def test_count_words():
    result = count_words("The cat sat on the mat.")
    assert result["the"] == 2
    assert result["cat"] == 1
    assert result["mat"] == 1
    assert "mat." not in result, "Punctuation should be stripped"
    print("  count_words: PASSED")


def test_top_n_words():
    result = top_n_words("to be or not to be", 2)
    assert len(result) == 2
    # Both "to" and "be" appear twice — sorted alphabetically for ties
    assert result == [("be", 2), ("to", 2)], f"Got {result}"
    print("  top_n_words: PASSED")


def test_group_by_frequency():
    result = group_by_frequency("the cat sat on the mat")
    assert result[2] == ["the"]
    assert sorted(result[1]) == ["cat", "mat", "on", "sat"]
    print("  group_by_frequency: PASSED")


def test_find_unique_words():
    result = find_unique_words("I love python", "I love java")
    assert result["text_0"] == {"python"}
    assert result["text_1"] == {"java"}
    print("  find_unique_words: PASSED")


if __name__ == "__main__":
    print("Running tests...\n")
    try:
        test_count_characters()
        test_count_words()
        test_top_n_words()
        test_group_by_frequency()
        test_find_unique_words()
        print("\nAll tests passed!")
    except AssertionError as e:
        print(f"\nTest FAILED: {e}")
    except TypeError:
        print("\nTest FAILED: function returned None — did you forget to implement it?")
