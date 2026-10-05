"""
Module 07 Advanced Python — Code Examples: Generators

Demonstrates:
- Infinite generators (fibonacci, naturals, primes)
- Data processing pipeline (streaming, constant memory)
- Generator send() for two-way communication
- yield from for delegation and flattening
- itertools showcase
"""

import itertools
import sys
from typing import Callable, Iterator


# ============================================================================
# 1. Infinite Generators
# ============================================================================

def fibonacci() -> Iterator[int]:
    """Yield Fibonacci numbers forever: 0, 1, 1, 2, 3, 5, 8, ..."""
    a, b = 0, 1
    while True:
        yield a
        a, b = b, a + b


def naturals(start: int = 0) -> Iterator[int]:
    """Yield natural numbers forever: start, start+1, start+2, ..."""
    n = start
    while True:
        yield n
        n += 1


def primes() -> Iterator[int]:
    """
    Yield prime numbers forever using the Sieve of Eratosthenes.
    Memory-efficient: only tracks composites seen so far.
    """
    # D[n] maps composite n to the prime that caused it
    D: dict[int, int] = {}
    n = 2
    while True:
        if n not in D:
            yield n          # n is prime
            D[n * n] = n     # Smallest composite: n^2
        else:
            p = D.pop(n)
            # Find next multiple of p not already mapped
            q = n + p
            while q in D:
                q += p
            D[q] = p
        n += 1


# ============================================================================
# 2. Data Pipeline — Streaming Processing with Constant Memory
# ============================================================================

def parse_records(raw_lines: Iterator[str]) -> Iterator[dict]:
    """Stage 1: Parse raw lines into record dicts."""
    for line in raw_lines:
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        parts = line.split(",")
        if len(parts) >= 3:
            yield {
                "id": parts[0].strip(),
                "name": parts[1].strip(),
                "amount": parts[2].strip(),
            }


def parse_amounts(records: Iterator[dict]) -> Iterator[dict]:
    """Stage 2: Convert amount strings to floats."""
    for record in records:
        try:
            yield {**record, "amount": float(record["amount"])}
        except ValueError:
            pass  # Skip invalid records


def filter_positive(records: Iterator[dict]) -> Iterator[dict]:
    """Stage 3: Keep only records with positive amounts."""
    for record in records:
        if record["amount"] > 0:
            yield record


def apply_tax(records: Iterator[dict], tax_rate: float = 0.1) -> Iterator[dict]:
    """Stage 4: Add tax field."""
    for record in records:
        yield {**record, "tax": round(record["amount"] * tax_rate, 2)}


def build_pipeline(
    source: Iterator[str],
    tax_rate: float = 0.1,
) -> Iterator[dict]:
    """
    Compose all stages. Each stage is lazy — no data is processed
    until you iterate over the result.

    Memory usage: O(1) regardless of input size.
    """
    records = parse_records(source)
    records = parse_amounts(records)
    records = filter_positive(records)
    records = apply_tax(records, tax_rate)
    return records


# ============================================================================
# 3. send() — Two-Way Communication
# ============================================================================

def running_average() -> Iterator[float]:
    """
    Coroutine: receives values via send(), yields the running average.

    Usage:
        gen = running_average()
        next(gen)             # Prime the generator
        avg = gen.send(10.0)  # Sends 10.0 in, returns 10.0
        avg = gen.send(20.0)  # Sends 20.0 in, returns 15.0
    """
    total = 0.0
    count = 0
    average = 0.0
    while True:
        value = yield average
        if value is None:
            break
        total += value
        count += 1
        average = total / count


def exponential_smoother(alpha: float = 0.3) -> Iterator[float]:
    """
    Coroutine: exponential moving average.

    alpha close to 1 → heavily weighted toward latest value.
    alpha close to 0 → heavily weighted toward history.
    """
    smoothed: float | None = None
    while True:
        value = yield smoothed
        if value is None:
            break
        if smoothed is None:
            smoothed = value
        else:
            smoothed = alpha * value + (1 - alpha) * smoothed


# ============================================================================
# 4. yield from — Delegation
# ============================================================================

def flatten(nested) -> Iterator:
    """Flatten arbitrarily nested iterables (except strings/bytes)."""
    for item in nested:
        if isinstance(item, (list, tuple, set)):
            yield from flatten(item)
        else:
            yield item


def chunked(iterable, n: int) -> Iterator[list]:
    """
    Yield successive n-sized chunks from an iterable.

    chunked(range(10), 3) → [0, 1, 2], [3, 4, 5], [6, 7, 8], [9]
    """
    iterator = iter(iterable)
    while True:
        chunk = list(itertools.islice(iterator, n))
        if not chunk:
            break
        yield chunk


def windowed(iterable, n: int) -> Iterator[tuple]:
    """
    Yield overlapping windows of size n.

    windowed([1,2,3,4,5], 3) → (1,2,3), (2,3,4), (3,4,5)
    """
    window = []
    for item in iterable:
        window.append(item)
        if len(window) == n:
            yield tuple(window)
            window.pop(0)


# ============================================================================
# 5. itertools Showcase
# ============================================================================

def demonstrate_itertools():
    """Demonstrates the most useful itertools functions."""

    # --- Infinite iterators ---
    print("=== Infinite iterators ===")

    # count: integer counter
    counter = itertools.count(10, 2)  # 10, 12, 14, ...
    print("count(10, 2):", list(itertools.islice(counter, 5)))  # [10, 12, 14, 16, 18]

    # cycle: repeat forever
    colors = itertools.cycle(["red", "green", "blue"])
    print("cycle:", list(itertools.islice(colors, 7)))  # ['red', 'green', 'blue', 'red', ...]

    # repeat: a single value N times
    print("repeat:", list(itertools.repeat("x", 4)))  # ['x', 'x', 'x', 'x']

    # --- Slicing and filtering ---
    print("\n=== Slicing and filtering ===")

    data = [1, 3, 7, 2, 5, 4, 9, 6, 8]

    # dropwhile: skip while condition true, then yield rest
    print("dropwhile (<5):", list(itertools.dropwhile(lambda x: x < 5, data)))
    # [7, 2, 5, 4, 9, 6, 8]  — stops dropping at 7 (first item >= 5)

    # takewhile: yield while condition true
    print("takewhile (<5):", list(itertools.takewhile(lambda x: x < 5, data)))
    # [1, 3]  — stops at 7

    # filterfalse: yield items where condition is False
    evens = list(itertools.filterfalse(lambda x: x % 2, range(10)))
    print("filterfalse (odd):", evens)  # [0, 2, 4, 6, 8]

    # --- Grouping ---
    print("\n=== Grouping ===")

    # groupby: group consecutive items (sort first for full grouping)
    words = sorted(["apple", "ant", "bear", "bee", "cat", "alligator"], key=lambda w: w[0])
    for letter, group in itertools.groupby(words, key=lambda w: w[0]):
        print(f"  '{letter}': {list(group)}")

    # --- Combinations ---
    print("\n=== Combinations ===")
    items = ["A", "B", "C"]
    print("combinations(2):", list(itertools.combinations(items, 2)))
    print("permutations(2):", list(itertools.permutations(items, 2)))
    print("product([0,1], repeat=3):", list(itertools.product([0, 1], repeat=3)))

    # --- Accumulation ---
    print("\n=== Accumulation ===")
    import operator
    values = [1, 2, 3, 4, 5]
    print("accumulate (sum):", list(itertools.accumulate(values)))
    print("accumulate (product):", list(itertools.accumulate(values, operator.mul)))
    print("accumulate (max):", list(itertools.accumulate([3,1,4,1,5,9,2,6], max)))

    # --- Merging ---
    print("\n=== Merging ===")
    a, b, c = [1, 2], [3, 4], [5, 6]
    print("chain:", list(itertools.chain(a, b, c)))

    # zip_longest: fills missing values with fillvalue
    short = [1, 2, 3]
    long_ = ["a", "b", "c", "d", "e"]
    print("zip_longest:", list(itertools.zip_longest(short, long_, fillvalue=0)))

    # starmap: map with unpacked arguments
    pairs = [(2, 5), (3, 3), (10, 2)]
    import math
    print("starmap (pow):", list(itertools.starmap(math.pow, pairs)))


# ============================================================================
# Demo
# ============================================================================

if __name__ == "__main__":
    print("=== Infinite generators ===")
    first_10_fib = list(itertools.islice(fibonacci(), 10))
    print(f"Fibonacci: {first_10_fib}")

    first_10_primes = list(itertools.islice(primes(), 10))
    print(f"Primes: {first_10_primes}")

    print("\n=== Memory comparison ===")
    n = 1_000_000
    list_data = [x**2 for x in range(n)]
    gen_data = (x**2 for x in range(n))
    print(f"List ({n} items): {sys.getsizeof(list_data):,} bytes")
    print(f"Generator ({n} items): {sys.getsizeof(gen_data):,} bytes")

    print("\n=== Data pipeline ===")
    raw_data = [
        "# CSV data",
        "1, Alice, 150.00",
        "2, Bob, -50.00",
        "3, Charlie, 200.50",
        "4, Diana, not-a-number",
        "5, Eve, 75.25",
    ]
    for record in build_pipeline(iter(raw_data), tax_rate=0.08):
        print(f"  {record['name']}: ${record['amount']:.2f} + ${record['tax']:.2f} tax")

    print("\n=== send() coroutine ===")
    gen = running_average()
    next(gen)  # Prime
    for val in [10, 20, 30, 40]:
        avg = gen.send(float(val))
        print(f"  After adding {val}: avg = {avg:.1f}")

    print("\n=== yield from: flatten ===")
    nested = [1, [2, [3, 4], 5], [6, 7], 8]
    print(f"  {list(flatten(nested))}")

    print("\n=== itertools ===")
    demonstrate_itertools()
