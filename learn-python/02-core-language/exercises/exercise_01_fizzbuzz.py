"""
Exercise 01: FizzBuzz with Statistics
======================================

Classic FizzBuzz, but extended to count how many of each category occur.

Rules:
  - For numbers 1 to n (inclusive):
    - If divisible by 3 AND 5: print "FizzBuzz"
    - If divisible by 3 only:  print "Fizz"
    - If divisible by 5 only:  print "Buzz"
    - Otherwise:               print the number

Part A: Implement fizzbuzz(n) that prints each result.
Part B: Implement fizzbuzz_stats(n) that returns a dict with counts:
        {"fizz": ..., "buzz": ..., "fizzbuzz": ..., "number": ...}
Part C: Print a formatted summary showing counts and percentages.

Expected output for fizzbuzz(20):
  1
  2
  Fizz
  4
  Buzz
  Fizz
  7
  8
  Fizz
  Buzz
  11
  Fizz
  13
  14
  FizzBuzz
  16
  17
  Fizz
  19
  Buzz

Expected stats for n=100:
  fizzbuzz: 6  (6.0%)
  fizz:     27 (27.0%)
  buzz:     14 (14.0%)
  number:   53 (53.0%)
  total:   100
"""


# ── YOUR CODE BELOW ──────────────────────────────────────────


def fizzbuzz(n: int) -> None:
    """
    Print FizzBuzz results for numbers 1 through n.

    Args:
        n: The upper bound (inclusive).
    """
    # TODO: implement this function
    pass


def fizzbuzz_stats(n: int) -> dict:
    """
    Run FizzBuzz and return category counts.

    Args:
        n: The upper bound (inclusive).

    Returns:
        A dict with keys "fizzbuzz", "fizz", "buzz", "number".
    """
    # TODO: implement this function
    pass


def print_summary(stats: dict, n: int) -> None:
    """
    Print a formatted summary of FizzBuzz statistics.

    Args:
        stats: Dict returned by fizzbuzz_stats().
        n: The value of n used (for percentage calculation).
    """
    # TODO: print something like:
    # FizzBuzz Stats (n=100):
    #   fizzbuzz:  6 ( 6.0%)
    #   fizz:     27 (27.0%)
    #   buzz:     14 (14.0%)
    #   number:   53 (53.0%)
    #   total:   100
    pass


# ── MAIN (run when this file is executed directly) ───────────

if __name__ == "__main__":
    print("FizzBuzz 1 to 20:")
    print("-" * 20)
    fizzbuzz(20)

    print("\nStats for n=100:")
    print("-" * 20)
    stats = fizzbuzz_stats(100)
    if stats:
        print_summary(stats, 100)
