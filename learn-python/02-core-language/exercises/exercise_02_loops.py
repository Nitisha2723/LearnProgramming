"""
Exercise 02: Loop Patterns
===========================

Three classic loop problems that test different loop skills.

─────────────────────────────────────────────────────────────
PROBLEM 1: Star Triangle
─────────────────────────────────────────────────────────────
Write print_triangle(n) that prints a right-aligned triangle
of stars with n rows.

For n=5:
    *
   **
  ***
 ****
*****

─────────────────────────────────────────────────────────────
PROBLEM 2: Primes to 100
─────────────────────────────────────────────────────────────
Write find_primes(limit) that returns a list of all prime numbers
up to and including limit.

find_primes(30) should return:
[2, 3, 5, 7, 11, 13, 17, 19, 23, 29]

Hint: A number n is prime if it has no divisors between 2 and sqrt(n).
Hint: import math; math.sqrt(n) — or use n ** 0.5

─────────────────────────────────────────────────────────────
PROBLEM 3: Digit Sum
─────────────────────────────────────────────────────────────
Write digit_sum(n) that returns the sum of all digits of n.

digit_sum(12345) → 15
digit_sum(999)   → 27
digit_sum(0)     → 0
digit_sum(7)     → 7

Bonus: Write it without converting to a string.
Hint: n % 10 gives the last digit; n // 10 removes the last digit.
"""

import math


# ── YOUR CODE BELOW ──────────────────────────────────────────


def print_triangle(n: int) -> None:
    """
    Print a right-aligned star triangle with n rows.

    Args:
        n: Number of rows in the triangle.
    """
    # TODO: implement using nested loops and string padding
    pass


def find_primes(limit: int) -> list[int]:
    """
    Return a list of all prime numbers up to limit (inclusive).

    Args:
        limit: Upper bound (inclusive).

    Returns:
        List of prime integers.
    """
    # TODO: implement prime checking
    pass


def digit_sum(n: int) -> int:
    """
    Return the sum of all digits of n.

    Args:
        n: A non-negative integer.

    Returns:
        Sum of its digits.

    Examples:
        digit_sum(12345) → 15
        digit_sum(0) → 0
    """
    # TODO: implement using a loop (try without converting to string!)
    pass


# ── MAIN ──────────────────────────────────────────────────────

if __name__ == "__main__":
    print("Problem 1: Star Triangle (n=5)")
    print("-" * 20)
    print_triangle(5)

    print("\nProblem 2: Primes to 50")
    print("-" * 20)
    primes = find_primes(50)
    print(primes)

    print("\nProblem 3: Digit Sum")
    print("-" * 20)
    test_values = [12345, 999, 0, 7, 100]
    for val in test_values:
        result = digit_sum(val)
        print(f"  digit_sum({val}) = {result}")
