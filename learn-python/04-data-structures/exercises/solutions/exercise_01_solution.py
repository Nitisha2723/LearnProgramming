"""
Exercise 01: Frequency Counter — SOLUTION
==========================================
"""

from collections import Counter, defaultdict
from typing import Optional
import string


def count_characters(text: str) -> dict[str, int]:
    """Count frequency of each alphanumeric character (case-insensitive)."""
    return Counter(ch.lower() for ch in text if ch.isalnum())


def count_words(text: str) -> dict[str, int]:
    """Count frequency of each word (case-insensitive, punctuation stripped)."""
    return Counter(
        word.strip(string.punctuation).lower()
        for word in text.split()
        if word.strip(string.punctuation)
    )


def top_n_words(text: str, n: int) -> list[tuple[str, int]]:
    """Return top N words by frequency; ties broken alphabetically."""
    counts = count_words(text)
    # Sort by count descending, then word ascending
    sorted_items = sorted(counts.items(), key=lambda x: (-x[1], x[0]))
    return sorted_items[:n]


def character_histogram(text: str) -> str:
    """Return a text histogram of letter frequencies."""
    counts = Counter(ch.lower() for ch in text if ch.isalpha())
    lines = []
    for char in sorted(counts.keys()):
        bar = "█" * counts[char]
        lines.append(f"{char} {bar} ({counts[char]})")
    return "\n".join(lines)


def find_unique_words(*texts: str) -> dict[str, set[str]]:
    """Find words unique to each text (not appearing in others)."""
    word_sets = [
        {w.strip(string.punctuation).lower() for w in t.split() if w.strip(string.punctuation)}
        for t in texts
    ]
    all_words = set().union(*word_sets)

    result = {}
    for i, words in enumerate(word_sets):
        others = set().union(*(word_sets[j] for j in range(len(word_sets)) if j != i))
        result[f"text_{i}"] = words - others

    return result


def group_by_frequency(text: str) -> dict[int, list[str]]:
    """Group words by their frequency count."""
    counts = count_words(text)
    groups = defaultdict(list)
    for word, count in counts.items():
        groups[count].append(word)
    # Sort words within each group
    return {count: sorted(words) for count, words in groups.items()}


# =============================================================================
# Demonstration
# =============================================================================

if __name__ == "__main__":
    sample_text = """
    To be or not to be that is the question
    Whether tis nobler in the mind to suffer
    The slings and arrows of outrageous fortune
    """

    print("=== Frequency Counter Solutions Demo ===\n")

    print("Character counts:")
    chars = count_characters("Hello, World!")
    for ch, count in sorted(chars.items()):
        print(f"  '{ch}': {count}")

    print("\nTop 5 words:")
    for word, count in top_n_words(sample_text, 5):
        print(f"  {word}: {count}")

    print("\nCharacter histogram for 'Hello Python':")
    print(character_histogram("Hello Python"))

    print("\nGroup by frequency (short text):")
    groups = group_by_frequency("the cat sat on the mat")
    for freq in sorted(groups.keys()):
        print(f"  Appears {freq}x: {groups[freq]}")

    print("\nUnique words:")
    unique = find_unique_words("I love Python and Java", "I love Java and Go")
    for key, words in unique.items():
        print(f"  {key}: {sorted(words)}")
