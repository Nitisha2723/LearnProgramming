"""
Solution: Exercise 01 — FizzBuzz with Statistics

Approach:
  - Use modulo (%) to check divisibility.
  - Check 15 (3×5) BEFORE checking 3 or 5 separately — order matters!
  - Use a dict to count categories.
  - Use f-string format specs for aligned percentage output.
"""


def fizzbuzz(n: int) -> None:
    """Print FizzBuzz results for numbers 1 through n."""
    for i in range(1, n + 1):
        if i % 15 == 0:      # check 15 FIRST (divisible by both 3 and 5)
            print("FizzBuzz")
        elif i % 3 == 0:
            print("Fizz")
        elif i % 5 == 0:
            print("Buzz")
        else:
            print(i)


def fizzbuzz_stats(n: int) -> dict:
    """Run FizzBuzz and return category counts."""
    stats = {"fizzbuzz": 0, "fizz": 0, "buzz": 0, "number": 0}

    for i in range(1, n + 1):
        if i % 15 == 0:
            stats["fizzbuzz"] += 1
        elif i % 3 == 0:
            stats["fizz"] += 1
        elif i % 5 == 0:
            stats["buzz"] += 1
        else:
            stats["number"] += 1

    return stats


def print_summary(stats: dict, n: int) -> None:
    """Print a formatted summary of FizzBuzz statistics."""
    print(f"FizzBuzz Stats (n={n}):")
    order = ["fizzbuzz", "fizz", "buzz", "number"]
    for key in order:
        count = stats[key]
        pct = count / n * 100
        print(f"  {key:>8}: {count:4d} ({pct:5.1f}%)")
    print(f"  {'total':>8}: {n:4d}")


# ── ALTERNATIVE: more Pythonic with a helper function ─────────

def classify(n: int) -> str:
    """Return the FizzBuzz category for a single number."""
    if n % 15 == 0:
        return "fizzbuzz"
    elif n % 3 == 0:
        return "fizz"
    elif n % 5 == 0:
        return "buzz"
    return "number"


def fizzbuzz_stats_v2(n: int) -> dict:
    """Version 2: use classify() and a list comprehension."""
    categories = [classify(i) for i in range(1, n + 1)]
    return {
        "fizzbuzz": categories.count("fizzbuzz"),
        "fizz": categories.count("fizz"),
        "buzz": categories.count("buzz"),
        "number": categories.count("number"),
    }


# ── MAIN ──────────────────────────────────────────────────────

if __name__ == "__main__":
    print("FizzBuzz 1 to 20:")
    print("-" * 20)
    fizzbuzz(20)

    print("\nStats for n=100:")
    print("-" * 20)
    stats = fizzbuzz_stats(100)
    print_summary(stats, 100)

    print("\nVerification (v2 matches v1):")
    stats2 = fizzbuzz_stats_v2(100)
    print(f"  Match: {stats == stats2}")
