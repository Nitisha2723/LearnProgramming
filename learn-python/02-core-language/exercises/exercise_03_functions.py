"""
Exercise 03: String Utility Functions
=======================================

Implement four string utility functions using what you know about
string methods, iteration, and pure function design.

─────────────────────────────────────────────────────────────
FUNCTION 1: is_palindrome(s)
─────────────────────────────────────────────────────────────
Return True if s is a palindrome (reads the same forwards and backwards),
ignoring case and non-alphabetic characters.

is_palindrome("racecar")         → True
is_palindrome("A man a plan a canal Panama")  → True
is_palindrome("hello")           → False
is_palindrome("Never odd or even") → True
is_palindrome("Was it a car or a cat I saw") → True

─────────────────────────────────────────────────────────────
FUNCTION 2: reverse_words(sentence)
─────────────────────────────────────────────────────────────
Return the sentence with the order of words reversed.
(Not the letters within each word — the word order.)

reverse_words("Hello World")              → "World Hello"
reverse_words("the quick brown fox")      → "fox brown quick the"
reverse_words("  extra   spaces  ")       → "spaces extra"  (normalize whitespace)
reverse_words("single")                   → "single"

─────────────────────────────────────────────────────────────
FUNCTION 3: count_vowels(s)
─────────────────────────────────────────────────────────────
Return the number of vowels (a, e, i, o, u) in s (case-insensitive).

count_vowels("Hello, World!")  → 3
count_vowels("Python")         → 1
count_vowels("aeiou")          → 5
count_vowels("rhythm")         → 0

─────────────────────────────────────────────────────────────
FUNCTION 4: title_case(s)
─────────────────────────────────────────────────────────────
Return s in title case, but keep certain "small words" lowercase
(unless they are the first word).

Small words: a, an, the, and, or, but, in, on, at, to, for, of

title_case("the lord of the rings")       → "The Lord of the Rings"
title_case("a tale of two cities")        → "A Tale of Two Cities"
title_case("to kill a mockingbird")       → "To Kill a Mockingbird"
title_case("the old man and the sea")     → "The Old Man and the Sea"
"""


# ── YOUR CODE BELOW ──────────────────────────────────────────


def is_palindrome(s: str) -> bool:
    """
    Return True if s is a palindrome (ignoring case and non-alpha chars).

    Args:
        s: Any string.

    Returns:
        True if s reads the same forwards and backwards.
    """
    # TODO: clean the string (lowercase, letters only), then compare to reversed
    pass


def reverse_words(sentence: str) -> str:
    """
    Return sentence with word order reversed and whitespace normalized.

    Args:
        sentence: A sentence of space-separated words.

    Returns:
        The words in reverse order, single-space separated.
    """
    # TODO: split, reverse, rejoin
    pass


def count_vowels(s: str) -> int:
    """
    Count the vowels (a, e, i, o, u) in s, case-insensitive.

    Args:
        s: Any string.

    Returns:
        Number of vowel characters.
    """
    # TODO: iterate over characters, check against vowels
    pass


SMALL_WORDS = {"a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "of"}


def title_case(s: str) -> str:
    """
    Convert s to title case, keeping "small words" lowercase except the first.

    Args:
        s: A title string in any case.

    Returns:
        Title-cased string with small words lowercased (except first word).
    """
    # TODO: split into words, capitalize or lowercase each based on rules
    pass


# ── MAIN ──────────────────────────────────────────────────────

if __name__ == "__main__":
    print("Function 1: is_palindrome")
    print("-" * 30)
    tests = [
        ("racecar", True),
        ("A man a plan a canal Panama", True),
        ("hello", False),
        ("Never odd or even", True),
    ]
    for s, expected in tests:
        result = is_palindrome(s)
        status = "PASS" if result == expected else "FAIL"
        print(f"  [{status}] is_palindrome({s!r}) = {result}")

    print("\nFunction 2: reverse_words")
    print("-" * 30)
    sentences = [
        "Hello World",
        "the quick brown fox",
        "  extra   spaces  ",
        "single",
    ]
    for sentence in sentences:
        result = reverse_words(sentence)
        print(f"  {sentence!r} → {result!r}")

    print("\nFunction 3: count_vowels")
    print("-" * 30)
    words = ["Hello, World!", "Python", "aeiou", "rhythm"]
    for word in words:
        result = count_vowels(word)
        print(f"  count_vowels({word!r}) = {result}")

    print("\nFunction 4: title_case")
    print("-" * 30)
    titles = [
        "the lord of the rings",
        "a tale of two cities",
        "to kill a mockingbird",
        "the old man and the sea",
    ]
    for title in titles:
        result = title_case(title)
        print(f"  {title!r}")
        print(f"    → {result!r}")
