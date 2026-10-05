"""
generators_demo.py — Generators, yield, itertools, and memory efficiency

Topics covered:
  - Generator expressions vs list comprehensions
  - Generator functions with yield
  - yield from
  - Lazy pipeline processing
  - itertools: chain, islice, cycle, product, combinations, groupby
  - Memory efficiency demonstration
"""

import sys
import time
import itertools

# =============================================================================
# SECTION 1: Generator Expressions vs List Comprehensions
# =============================================================================

print("=" * 60)
print("SECTION 1: Generator Expressions vs Lists")
print("=" * 60)

N = 1_000_000

# List comprehension — stores all N values
lst = [x**2 for x in range(N)]

# Generator expression — stores nothing, computes on demand
gen = (x**2 for x in range(N))

print(f"List ({N:,} items):      {sys.getsizeof(lst):>12,} bytes")
print(f"Generator ({N:,} items): {sys.getsizeof(gen):>12,} bytes")

# They produce the same results
lst_small = [x**2 for x in range(5)]
gen_small  = (x**2 for x in range(5))

print(f"\nList:      {lst_small}")
print(f"Generator: {list(gen_small)}")  # Must convert to list to print all

# Important: generator can only be iterated ONCE
gen2 = (x for x in range(3))
print(f"\nFirst pass:  {list(gen2)}")
print(f"Second pass: {list(gen2)}")   # Empty! Exhausted.

# =============================================================================
# SECTION 2: Generator Functions with yield
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 2: Generator Functions")
print("=" * 60)

def count_up(start, stop, step=1):
    """Yields integers from start to stop (inclusive)."""
    current = start
    while current <= stop:
        yield current
        current += step

# Usage
print("count_up(1, 10, 2):")
for n in count_up(1, 10, 2):
    print(f"  {n}", end=" ")
print()

# Only generates when asked
counter = count_up(1, 1_000_000)
print(f"\nFirst value: {next(counter)}")
print(f"Second:      {next(counter)}")
print(f"Third:       {next(counter)}")
print(f"(999,997 values not yet computed)")

# Fibonacci generator — infinite sequence
def fibonacci():
    """Yields Fibonacci numbers indefinitely."""
    a, b = 0, 1
    while True:
        yield a
        a, b = b, a + b

fib = fibonacci()
first_10 = [next(fib) for _ in range(10)]
print(f"\nFirst 10 Fibonacci: {first_10}")

# Demonstrate yield saves state
def debug_generator():
    print("  [start of generator]")
    yield 1
    print("  [after first yield]")
    yield 2
    print("  [after second yield]")
    yield 3
    print("  [generator done]")

print("\nStep-by-step generator execution:")
gen = debug_generator()
print(f"next() = {next(gen)}")
print(f"next() = {next(gen)}")
print(f"next() = {next(gen)}")

# =============================================================================
# SECTION 3: yield from — Delegation
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 3: yield from")
print("=" * 60)

def flatten(nested):
    """Flatten arbitrarily nested lists."""
    for item in nested:
        if isinstance(item, list):
            yield from flatten(item)  # Delegate to recursive call
        else:
            yield item

nested = [1, [2, [3, 4], 5], [6, 7], 8]
print(f"Nested:    {nested}")
print(f"Flattened: {list(flatten(nested))}")

# yield from with iterables
def chain_iterables(*iterables):
    for it in iterables:
        yield from it

combined = list(chain_iterables([1, 2], "abc", range(5, 8)))
print(f"\nChained: {combined}")

# =============================================================================
# SECTION 4: Lazy Pipeline Processing
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 4: Lazy Pipeline")
print("=" * 60)

def read_numbers(count):
    """Simulates reading from a large data source."""
    print(f"  [generator: starting to read {count} numbers]")
    for i in range(1, count + 1):
        yield i

def filter_even(numbers):
    """Filter: only yield even numbers."""
    for n in numbers:
        if n % 2 == 0:
            yield n

def square(numbers):
    """Transform: yield squares."""
    for n in numbers:
        yield n ** 2

def running_sum(numbers):
    """Accumulate: yield running sum."""
    total = 0
    for n in numbers:
        total += n
        yield total

# Build the pipeline — NO data has flowed yet
nums    = read_numbers(10)
evens   = filter_even(nums)
squared = square(evens)
sums    = running_sum(squared)

print("Pipeline built. No numbers read yet.")
print("Processing pipeline:")

for val in sums:
    print(f"  running sum: {val}")

# =============================================================================
# SECTION 5: Memory Efficiency — Large File Processing
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 5: Memory Efficiency")
print("=" * 60)

import tempfile, os

def create_large_file(path, num_lines=50_000):
    """Create a mock large text file."""
    with open(path, "w") as f:
        for i in range(num_lines):
            level = "ERROR" if i % 100 == 0 else ("WARN" if i % 20 == 0 else "INFO")
            f.write(f"2024-01-15 {9 + i//3600:02d}:00:00 [{level}] Event {i}: message text here\n")
    return num_lines

# Create test file
with tempfile.NamedTemporaryFile(mode="w", suffix=".log", delete=False) as f:
    tmplog = f.name
lines_written = create_large_file(tmplog)
file_size = os.path.getsize(tmplog)
print(f"Created log file: {file_size:,} bytes ({lines_written:,} lines)")

# APPROACH 1: Load all into memory (BAD for huge files)
start = time.perf_counter()
with open(tmplog) as f:
    all_lines = f.readlines()    # Loads everything!
error_lines = [l for l in all_lines if "[ERROR]" in l]
t1 = time.perf_counter() - start
mem1 = sys.getsizeof(all_lines)
print(f"\nreadlines() approach:")
print(f"  Memory used: {mem1:,} bytes")
print(f"  Time:        {t1*1000:.2f}ms")
print(f"  Errors found: {len(error_lines)}")

# APPROACH 2: Generator — O(1) memory
def read_errors(filepath):
    with open(filepath) as f:
        for line in f:
            if "[ERROR]" in line:
                yield line.rstrip()

start = time.perf_counter()
error_gen = list(read_errors(tmplog))
t2 = time.perf_counter() - start
mem2 = sys.getsizeof(error_gen)
print(f"\nGenerator approach:")
print(f"  Memory used: {mem2:,} bytes (only error lines)")
print(f"  Time:        {t2*1000:.2f}ms")
print(f"  Errors found: {len(error_gen)}")

print(f"\nMemory savings: {mem1/mem2:.1f}x less memory")

# Sample output
print(f"\nFirst 3 error lines:")
for line in error_gen[:3]:
    print(f"  {line}")

os.unlink(tmplog)

# =============================================================================
# SECTION 6: itertools
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 6: itertools")
print("=" * 60)

# chain — concatenate iterables
print("chain:")
combined = list(itertools.chain([1, 2], [3, 4], "abc"))
print(f"  {combined}")

print("\nchain.from_iterable:")
nested_lists = [[1, 2], [3, 4], [5, 6]]
flat = list(itertools.chain.from_iterable(nested_lists))
print(f"  {flat}")

# islice — lazy slicing
print("\nislice (first 5 of infinite generator):")
fibs = fibonacci()
first_5_fibs = list(itertools.islice(fibs, 5))
print(f"  {first_5_fibs}")

# cycle — repeat sequence infinitely
print("\ncycle (first 8 from cycling [R, G, B]):")
colors = list(itertools.islice(itertools.cycle(["R", "G", "B"]), 8))
print(f"  {colors}")

# product — cartesian product
print("\nproduct ([1,2] × ['a','b']):")
for combo in itertools.product([1, 2], ["a", "b"]):
    print(f"  {combo}")

# combinations
print("\ncombinations(['A','B','C','D'], 2):")
for combo in itertools.combinations("ABCD", 2):
    print(f"  {combo}")

print(f"\nNumber of 2-card hands from 52-card deck: "
      f"{sum(1 for _ in itertools.combinations(range(52), 2)):,}")

# permutations
print("\npermutations(['x','y','z'], 2):")
for perm in itertools.permutations("xyz", 2):
    print(f"  {perm}")

# groupby
print("\ngroupby (transactions by date):")
transactions = [
    {"date": "2024-01-01", "amount": 100},
    {"date": "2024-01-01", "amount": 250},
    {"date": "2024-01-02", "amount": 75},
    {"date": "2024-01-02", "amount": 400},
    {"date": "2024-01-02", "amount": 180},
    {"date": "2024-01-03", "amount": 300},
]

# Must be sorted by grouping key first!
sorted_txns = sorted(transactions, key=lambda x: x["date"])
for date, group in itertools.groupby(sorted_txns, key=lambda x: x["date"]):
    amounts = [t["amount"] for t in group]
    print(f"  {date}: {len(amounts)} transactions, total=${sum(amounts)}")

# takewhile and dropwhile
print("\ntakewhile (take while < 5):")
data = [1, 2, 3, 7, 4, 5, 6]    # Note: stops at 7, not 4
print(f"  {list(itertools.takewhile(lambda x: x < 5, data))}")

print("dropwhile (drop while < 5):")
print(f"  {list(itertools.dropwhile(lambda x: x < 5, data))}")

# accumulate
print("\naccumulate (running sum):")
import itertools as it
vals = [1, 2, 3, 4, 5]
print(f"  {list(itertools.accumulate(vals))}")
print(f"  running max: {list(itertools.accumulate(vals, max))}")
