"""
Solution: Exercise 02 — Loop Patterns

Three problems demonstrating different loop techniques.
"""

import math


# ─────────────────────────────────────────────────────────────
# PROBLEM 1: Star Triangle
# ─────────────────────────────────────────────────────────────

def print_triangle(n: int) -> None:
    """Print a right-aligned star triangle with n rows."""
    for row in range(1, n + 1):
        # Each row has 'row' stars, right-aligned in a field of width n
        stars = "*" * row
        print(stars.rjust(n))


# Alternative using string formatting directly:
def print_triangle_v2(n: int) -> None:
    """Version 2: use f-string alignment."""
    for row in range(1, n + 1):
        print(f"{'*' * row:>{n}}")


# ─────────────────────────────────────────────────────────────
# PROBLEM 2: Find Primes
# ─────────────────────────────────────────────────────────────

def find_primes(limit: int) -> list[int]:
    """Return a list of all prime numbers up to limit (inclusive)."""
    if limit < 2:
        return []

    primes = []
    for n in range(2, limit + 1):
        if _is_prime(n):
            primes.append(n)
    return primes


def _is_prime(n: int) -> bool:
    """Helper: return True if n is prime."""
    if n < 2:
        return False
    # Only need to check up to sqrt(n)
    # because if n = a * b, one of a or b must be ≤ sqrt(n)
    for i in range(2, int(math.sqrt(n)) + 1):
        if n % i == 0:
            return False
    return True


# Alternative: Sieve of Eratosthenes (more efficient for large limits)
def find_primes_sieve(limit: int) -> list[int]:
    """
    Sieve of Eratosthenes — O(n log log n).

    Creates a boolean array and marks composites.
    More efficient than trial division for finding all primes up to a limit.
    """
    if limit < 2:
        return []

    # Start with all True; index = number, value = is_prime
    is_prime = [True] * (limit + 1)
    is_prime[0] = is_prime[1] = False   # 0 and 1 are not prime

    for i in range(2, int(math.sqrt(limit)) + 1):
        if is_prime[i]:
            # Mark all multiples of i as composite
            for j in range(i * i, limit + 1, i):
                is_prime[j] = False

    return [n for n, prime in enumerate(is_prime) if prime]


# ─────────────────────────────────────────────────────────────
# PROBLEM 3: Digit Sum
# ─────────────────────────────────────────────────────────────

def digit_sum(n: int) -> int:
    """
    Return the sum of all digits of n.

    Approach 1 (string-based — simple):
    """
    return sum(int(d) for d in str(n))


def digit_sum_v2(n: int) -> int:
    """
    Return the sum of all digits of n.

    Approach 2 (arithmetic — no string conversion):
    - n % 10 gives the last digit
    - n // 10 removes the last digit
    """
    if n == 0:
        return 0

    total = 0
    while n > 0:
        total += n % 10     # add last digit
        n //= 10            # remove last digit
    return total


# ── MAIN ──────────────────────────────────────────────────────

if __name__ == "__main__":
    print("Problem 1: Star Triangle (n=5)")
    print("-" * 20)
    print_triangle(5)

    print("\nProblem 2: Primes to 50")
    print("-" * 20)
    primes = find_primes(50)
    sieve_primes = find_primes_sieve(50)
    print(f"Trial division: {primes}")
    print(f"Sieve:          {sieve_primes}")
    print(f"Both match:     {primes == sieve_primes}")

    print("\nProblem 3: Digit Sum")
    print("-" * 20)
    test_values = [12345, 999, 0, 7, 100]
    for val in test_values:
        r1 = digit_sum(val)
        r2 = digit_sum_v2(val)
        print(f"  digit_sum({val}) = {r1}  (v2: {r2}, match: {r1 == r2})")
