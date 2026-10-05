"""
test_core_language.py — Comprehensive pytest test suite for Module 02.

Demonstrates:
  - Plain assert statements with descriptive messages
  - @pytest.mark.parametrize for data-driven tests
  - @pytest.fixture for shared test data
  - pytest.raises for exception testing
  - pytest.approx for floating-point comparisons
  - The AAA (Arrange-Act-Assert) pattern (shown in comments)
  - Descriptive test names: test_function_scenario_expected

Run with:
  pytest tests/test_core_language.py -v
  pytest tests/ -k "fizzbuzz" -v
"""

import sys
import os
import pytest

# Add the solutions directory to the path so we can import from it
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "exercises", "solutions"))

from exercise_01_solution import fizzbuzz_stats, classify
from exercise_02_solution import find_primes, digit_sum, digit_sum_v2
from exercise_03_solution import is_palindrome, reverse_words, count_vowels, title_case
from exercise_04_solution import rotate_left, remove_duplicates, two_sum, two_sum_fast


# ─────────────────────────────────────────────────────────────
# FIXTURES — shared test data, available to all tests below
# ─────────────────────────────────────────────────────────────

@pytest.fixture
def sample_list():
    """A list with duplicates for testing remove_duplicates."""
    return [1, 2, 3, 2, 4, 3, 5]


@pytest.fixture
def number_sequence():
    """A sequence of numbers for various tests."""
    return [3, 1, 4, 1, 5, 9, 2, 6]


@pytest.fixture
def prime_numbers_to_30():
    """Known primes up to 30 for verification."""
    return [2, 3, 5, 7, 11, 13, 17, 19, 23, 29]


# ─────────────────────────────────────────────────────────────
# TESTS: Exercise 01 — FizzBuzz
# ─────────────────────────────────────────────────────────────

class TestFizzBuzz:
    """Tests for the FizzBuzz functions."""

    def test_fizzbuzz_stats_returns_dict(self):
        # ARRANGE
        n = 15
        # ACT
        result = fizzbuzz_stats(n)
        # ASSERT
        assert isinstance(result, dict)

    def test_fizzbuzz_stats_has_four_keys(self):
        # ARRANGE / ACT
        result = fizzbuzz_stats(15)
        # ASSERT
        assert set(result.keys()) == {"fizzbuzz", "fizz", "buzz", "number"}

    def test_fizzbuzz_stats_total_equals_n(self):
        # ARRANGE
        n = 100
        # ACT
        stats = fizzbuzz_stats(n)
        # ASSERT
        total = sum(stats.values())
        assert total == n, f"Expected total {n}, got {total}"

    def test_fizzbuzz_stats_fizzbuzz_count_for_15(self):
        # ARRANGE / ACT
        stats = fizzbuzz_stats(15)
        # ASSERT — only 15 is divisible by both 3 and 5 in range 1..15
        assert stats["fizzbuzz"] == 1

    def test_fizzbuzz_stats_fizz_count_for_15(self):
        # ARRANGE / ACT
        stats = fizzbuzz_stats(15)
        # ASSERT — 3, 6, 9, 12 are divisible by 3 only (15 is fizzbuzz)
        assert stats["fizz"] == 4

    def test_fizzbuzz_stats_buzz_count_for_15(self):
        # ARRANGE / ACT
        stats = fizzbuzz_stats(15)
        # ASSERT — 5, 10 are divisible by 5 only
        assert stats["buzz"] == 2

    def test_fizzbuzz_stats_counts_for_n100(self):
        # ARRANGE / ACT
        stats = fizzbuzz_stats(100)
        # ASSERT — known values for 1..100
        assert stats["fizzbuzz"] == 6
        assert stats["fizz"] == 27
        assert stats["buzz"] == 14
        assert stats["number"] == 53

    @pytest.mark.parametrize("number, expected_category", [
        (1, "number"),
        (2, "number"),
        (3, "fizz"),
        (5, "buzz"),
        (9, "fizz"),
        (10, "buzz"),
        (15, "fizzbuzz"),
        (30, "fizzbuzz"),
        (6, "fizz"),
        (25, "buzz"),
    ])
    def test_classify_individual_numbers(self, number, expected_category):
        # ARRANGE / ACT
        result = classify(number)
        # ASSERT
        assert result == expected_category, \
            f"classify({number}) should be '{expected_category}', got '{result}'"


# ─────────────────────────────────────────────────────────────
# TESTS: Exercise 02 — Loops
# ─────────────────────────────────────────────────────────────

class TestFindPrimes:
    """Tests for find_primes()."""

    def test_find_primes_returns_list(self):
        assert isinstance(find_primes(10), list)

    def test_find_primes_up_to_30(self, prime_numbers_to_30):
        # ARRANGE
        expected = prime_numbers_to_30
        # ACT
        result = find_primes(30)
        # ASSERT
        assert result == expected

    def test_find_primes_below_2_returns_empty(self):
        assert find_primes(1) == []
        assert find_primes(0) == []

    def test_find_primes_includes_upper_bound_if_prime(self):
        # 29 is prime — should be included in find_primes(29)
        result = find_primes(29)
        assert 29 in result

    def test_find_primes_excludes_composites(self):
        result = find_primes(20)
        composites = [4, 6, 8, 9, 10, 12, 14, 15, 16, 18, 20]
        for c in composites:
            assert c not in result, f"Composite {c} should not be in primes list"


class TestDigitSum:
    """Tests for digit_sum()."""

    @pytest.mark.parametrize("n, expected", [
        (12345, 15),
        (999, 27),
        (0, 0),
        (7, 7),
        (100, 1),
        (1234567890, 45),
    ])
    def test_digit_sum_parametrized(self, n, expected):
        # ARRANGE / ACT
        result = digit_sum(n)
        # ASSERT
        assert result == expected, f"digit_sum({n}) should be {expected}, got {result}"

    def test_digit_sum_v2_matches_v1(self):
        """Both implementations should produce the same results."""
        test_cases = [12345, 999, 0, 7, 100, 9999]
        for n in test_cases:
            assert digit_sum(n) == digit_sum_v2(n), \
                f"Mismatch for digit_sum({n})"


# ─────────────────────────────────────────────────────────────
# TESTS: Exercise 03 — String Functions
# ─────────────────────────────────────────────────────────────

class TestIsPalindrome:
    """Tests for is_palindrome()."""

    @pytest.mark.parametrize("text, expected", [
        ("racecar", True),
        ("hello", False),
        ("level", True),
        ("A man a plan a canal Panama", True),
        ("Never odd or even", True),
        ("Was it a car or a cat I saw", True),
        ("Python", False),
        ("", True),      # empty string is a palindrome
        ("a", True),     # single character
        ("aa", True),
        ("ab", False),
    ])
    def test_is_palindrome_various_inputs(self, text, expected):
        # ARRANGE / ACT
        result = is_palindrome(text)
        # ASSERT
        assert result == expected, \
            f"is_palindrome({text!r}) should be {expected}, got {result}"

    def test_is_palindrome_ignores_case(self):
        assert is_palindrome("Racecar") is True
        assert is_palindrome("RACECAR") is True

    def test_is_palindrome_ignores_spaces_and_punctuation(self):
        assert is_palindrome("A man, a plan, a canal: Panama") is True


class TestReverseWords:
    """Tests for reverse_words()."""

    def test_reverse_words_basic(self):
        assert reverse_words("Hello World") == "World Hello"

    def test_reverse_words_multiple_words(self):
        assert reverse_words("the quick brown fox") == "fox brown quick the"

    def test_reverse_words_normalizes_whitespace(self):
        # ARRANGE
        padded = "  extra   spaces  "
        # ACT
        result = reverse_words(padded)
        # ASSERT — multiple spaces should become single spaces, no leading/trailing
        assert result == "spaces extra"

    def test_reverse_words_single_word(self):
        assert reverse_words("single") == "single"

    def test_reverse_words_empty_string(self):
        assert reverse_words("") == ""


class TestCountVowels:
    """Tests for count_vowels()."""

    @pytest.mark.parametrize("text, expected_count", [
        ("Hello, World!", 3),
        ("Python", 1),
        ("aeiou", 5),
        ("AEIOU", 5),       # uppercase vowels
        ("rhythm", 0),
        ("", 0),
        ("bcdfg", 0),       # no vowels
    ])
    def test_count_vowels_parametrized(self, text, expected_count):
        result = count_vowels(text)
        assert result == expected_count, \
            f"count_vowels({text!r}) should be {expected_count}, got {result}"


# ─────────────────────────────────────────────────────────────
# TESTS: Exercise 04 — List Algorithms
# ─────────────────────────────────────────────────────────────

class TestRotateLeft:
    """Tests for rotate_left()."""

    def test_rotate_left_by_two(self):
        # ARRANGE
        lst = [1, 2, 3, 4, 5]
        # ACT
        result = rotate_left(lst, 2)
        # ASSERT
        assert result == [3, 4, 5, 1, 2]

    def test_rotate_left_by_zero_returns_same(self):
        lst = [1, 2, 3, 4, 5]
        result = rotate_left(lst, 0)
        assert result == [1, 2, 3, 4, 5]

    def test_rotate_left_full_rotation_returns_same(self):
        lst = [1, 2, 3, 4, 5]
        result = rotate_left(lst, 5)  # rotating by length = full circle
        assert result == [1, 2, 3, 4, 5]

    def test_rotate_left_more_than_length_wraps(self):
        lst = [1, 2, 3]
        result = rotate_left(lst, 7)  # 7 % 3 = 1
        assert result == [2, 3, 1]

    def test_rotate_left_empty_list_returns_empty(self):
        assert rotate_left([], 3) == []

    def test_rotate_left_does_not_modify_original(self):
        # ARRANGE
        original = [1, 2, 3, 4, 5]
        copy = original[:]
        # ACT
        rotate_left(original, 2)
        # ASSERT — original should be unchanged
        assert original == copy


class TestRemoveDuplicates:
    """Tests for remove_duplicates()."""

    def test_remove_duplicates_basic(self, sample_list):
        # ARRANGE — sample_list is [1, 2, 3, 2, 4, 3, 5]
        # ACT
        result = remove_duplicates(sample_list)
        # ASSERT
        assert result == [1, 2, 3, 4, 5]

    def test_remove_duplicates_preserves_order(self):
        # ARRANGE
        lst = [3, 1, 4, 1, 5, 9, 2, 6, 5, 3]
        # ACT
        result = remove_duplicates(lst)
        # ASSERT — first appearances: 3, 1, 4, 5, 9, 2, 6
        assert result == [3, 1, 4, 5, 9, 2, 6]

    def test_remove_duplicates_empty_list(self):
        assert remove_duplicates([]) == []

    def test_remove_duplicates_all_same(self):
        assert remove_duplicates([1, 1, 1, 1]) == [1]

    def test_remove_duplicates_no_duplicates(self):
        lst = [1, 2, 3, 4, 5]
        assert remove_duplicates(lst) == [1, 2, 3, 4, 5]

    def test_remove_duplicates_does_not_modify_original(self):
        # ARRANGE
        original = [1, 2, 3, 2, 1]
        copy = original[:]
        # ACT
        remove_duplicates(original)
        # ASSERT
        assert original == copy


class TestTwoSum:
    """Tests for two_sum() and two_sum_fast()."""

    @pytest.mark.parametrize("numbers, target, expected", [
        ([2, 7, 11, 15], 9, (0, 1)),
        ([3, 2, 4], 6, (1, 2)),
        ([3, 3], 6, (0, 1)),
        ([1, 2, 3], 10, None),
        ([0, 4, 3, 0], 0, (0, 3)),
    ])
    def test_two_sum_basic(self, numbers, target, expected):
        result = two_sum(numbers, target)
        assert result == expected

    @pytest.mark.parametrize("numbers, target, expected", [
        ([2, 7, 11, 15], 9, (0, 1)),
        ([3, 2, 4], 6, (1, 2)),
        ([3, 3], 6, (0, 1)),
        ([1, 2, 3], 10, None),
    ])
    def test_two_sum_fast_matches_slow(self, numbers, target, expected):
        """Fast and slow should give the same answer."""
        slow = two_sum(numbers, target)
        fast = two_sum_fast(numbers, target)
        assert slow == fast, \
            f"two_sum({numbers}, {target}): slow={slow}, fast={fast}"

    def test_two_sum_returns_tuple_or_none(self):
        result = two_sum([2, 7, 11], 9)
        assert isinstance(result, tuple)

        result_none = two_sum([1, 2], 100)
        assert result_none is None

    def test_two_sum_indices_are_valid(self):
        """The returned indices should actually work."""
        # ARRANGE
        numbers = [2, 7, 11, 15]
        target = 9
        # ACT
        i, j = two_sum(numbers, target)
        # ASSERT
        assert numbers[i] + numbers[j] == target
        assert i < j   # ensure i comes before j


# ─────────────────────────────────────────────────────────────
# ADDITIONAL EDGE CASE TESTS
# ─────────────────────────────────────────────────────────────

class TestEdgeCases:
    """Edge case tests that exercise boundary conditions."""

    def test_fizzbuzz_stats_n_equals_1(self):
        stats = fizzbuzz_stats(1)
        assert stats["number"] == 1
        assert stats["fizz"] == 0
        assert stats["buzz"] == 0
        assert stats["fizzbuzz"] == 0

    def test_find_primes_exactly_2(self):
        """2 is the smallest and only even prime."""
        result = find_primes(2)
        assert result == [2]
        assert 2 in find_primes(100)

    def test_rotate_left_single_element(self):
        assert rotate_left([42], 5) == [42]

    def test_remove_duplicates_works_with_strings(self):
        lst = ["a", "b", "a", "c", "b"]
        result = remove_duplicates(lst)
        assert result == ["a", "b", "c"]

    def test_is_palindrome_numbers_as_string(self):
        # The implementation strips non-alpha chars; digits are removed.
        # "12321" → "" (empty, which is a palindrome)
        assert is_palindrome("12321") is True
        # "12345" → "" (empty, also a palindrome after digit-stripping)
        assert is_palindrome("12345") is True
        # A string with letters is the meaningful test
        assert is_palindrome("abcba") is True
        assert is_palindrome("abcde") is False
