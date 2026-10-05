"""
Solution: Exercise 03 — String Utility Functions

Four string functions demonstrating string methods and iteration.
"""


# ─────────────────────────────────────────────────────────────
# FUNCTION 1: is_palindrome
# ─────────────────────────────────────────────────────────────

def is_palindrome(s: str) -> bool:
    """Return True if s is a palindrome (ignoring case and non-alpha chars)."""
    # Step 1: Keep only alphabetic characters, convert to lowercase
    cleaned = "".join(c.lower() for c in s if c.isalpha())

    # Step 2: Compare with its reverse
    return cleaned == cleaned[::-1]


# ─────────────────────────────────────────────────────────────
# FUNCTION 2: reverse_words
# ─────────────────────────────────────────────────────────────

def reverse_words(sentence: str) -> str:
    """Return sentence with word order reversed and whitespace normalized."""
    # split() without arguments splits on any whitespace AND removes empty strings
    # This handles leading/trailing/multiple spaces automatically
    words = sentence.split()
    words.reverse()             # reverse the list in place
    return " ".join(words)


# One-liner version:
def reverse_words_oneliner(sentence: str) -> str:
    return " ".join(reversed(sentence.split()))


# ─────────────────────────────────────────────────────────────
# FUNCTION 3: count_vowels
# ─────────────────────────────────────────────────────────────

VOWELS = set("aeiouAEIOU")


def count_vowels(s: str) -> int:
    """Count the vowels (a, e, i, o, u) in s, case-insensitive."""
    return sum(1 for c in s if c in VOWELS)


# Alternative using str.lower():
def count_vowels_v2(s: str) -> int:
    return sum(1 for c in s.lower() if c in "aeiou")


# ─────────────────────────────────────────────────────────────
# FUNCTION 4: title_case
# ─────────────────────────────────────────────────────────────

SMALL_WORDS = {"a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "of"}


def title_case(s: str) -> str:
    """Convert s to title case, keeping small words lowercase except the first."""
    words = s.lower().split()

    result = []
    for i, word in enumerate(words):
        if i == 0 or word not in SMALL_WORDS:
            result.append(word.capitalize())
        else:
            result.append(word)          # keep small word lowercase

    return " ".join(result)


# ── MAIN ──────────────────────────────────────────────────────

if __name__ == "__main__":
    print("Function 1: is_palindrome")
    print("-" * 40)
    tests = [
        ("racecar", True),
        ("A man a plan a canal Panama", True),
        ("hello", False),
        ("Never odd or even", True),
        ("Was it a car or a cat I saw", True),
    ]
    for s, expected in tests:
        result = is_palindrome(s)
        status = "PASS" if result == expected else "FAIL"
        print(f"  [{status}] is_palindrome({s!r}) = {result}")

    print("\nFunction 2: reverse_words")
    print("-" * 40)
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
    print("-" * 40)
    word_tests = [
        ("Hello, World!", 3),
        ("Python", 1),
        ("aeiou", 5),
        ("rhythm", 0),
    ]
    for word, expected in word_tests:
        result = count_vowels(word)
        status = "PASS" if result == expected else "FAIL"
        print(f"  [{status}] count_vowels({word!r}) = {result}")

    print("\nFunction 4: title_case")
    print("-" * 40)
    title_tests = [
        "the lord of the rings",
        "a tale of two cities",
        "to kill a mockingbird",
        "the old man and the sea",
    ]
    for title in title_tests:
        result = title_case(title)
        print(f"  {title!r}")
        print(f"    → {result!r}")
