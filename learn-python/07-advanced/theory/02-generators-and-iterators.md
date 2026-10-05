# Generators and Iterators

Generators are Python's answer to the question: "how do we process large data sequences without loading everything into memory?"

---

## The Iterator Protocol

Before generators, understand iterators. Python's `for` loop works with any object that implements the iterator protocol:

```python
# The iterator protocol:
# An object must implement __iter__() and __next__()

class CountUp:
    """Counts from start to end. Implements the iterator protocol manually."""

    def __init__(self, start: int, end: int):
        self._current = start
        self._end = end

    def __iter__(self):
        """Return the iterator object (self in this case)."""
        return self

    def __next__(self) -> int:
        """Return the next value. Raise StopIteration when done."""
        if self._current > self._end:
            raise StopIteration
        value = self._current
        self._current += 1
        return value


counter = CountUp(1, 5)
for n in counter:
    print(n)  # 1, 2, 3, 4, 5

# What the for loop actually does:
iterator = iter(counter)   # calls counter.__iter__()
try:
    while True:
        value = next(iterator)  # calls iterator.__next__()
        print(value)
except StopIteration:
    pass
```

Writing iterators with `__iter__` and `__next__` is tedious. Generators are the clean way.

---

## Generator Functions: yield

A generator function uses `yield` instead of `return`. Python turns it into a generator object automatically.

```python
def count_up(start: int, end: int):
    """Exactly equivalent to CountUp above, but 3 lines instead of 15."""
    current = start
    while current <= end:
        yield current
        current += 1


for n in count_up(1, 5):
    print(n)  # 1, 2, 3, 4, 5
```

### How yield Works

When Python hits `yield value`:
1. The value is returned to the caller
2. **The function's state is saved** (local variables, position in code)
3. Execution pauses

When `next()` is called on the generator:
1. Execution resumes from exactly where it paused
2. Until the next `yield` or end of function

```python
def demo_state():
    """Illustrates how state is preserved between yields."""
    print("Before first yield")
    yield 1
    print("Between yields")
    yield 2
    print("After last yield")


gen = demo_state()
print(next(gen))  # "Before first yield", then 1
print(next(gen))  # "Between yields", then 2
next(gen)         # "After last yield", then StopIteration
```

---

## Generator Expressions

Like list comprehensions, but lazy:

```python
numbers = range(10_000_000)

# List comprehension — builds all 10 million items in memory NOW
squares_list = [x**2 for x in numbers]
print(type(squares_list))   # <class 'list'>

# Generator expression — creates no items until you ask for them
squares_gen = (x**2 for x in numbers)
print(type(squares_gen))    # <class 'generator'>

# Memory difference:
import sys
print(sys.getsizeof(squares_list))  # ~80 million bytes
print(sys.getsizeof(squares_gen))   # ~104 bytes (!)

# Both work the same in a for loop:
for sq in squares_gen:
    if sq > 100:
        break
```

### When to Use Generators vs Lists

- Use a **list** when you need to: access by index, iterate multiple times, know the length, or sort
- Use a **generator** when you: process items once, the sequence is very long, or you're building a pipeline

---

## send() — Two-Way Communication

Generators can receive values through `send()`. This turns a generator into a coroutine (before async/await).

```python
def running_average():
    """
    Generator that maintains a running average.
    
    .send(value) sends a value IN and yields the updated average out.
    """
    total = 0.0
    count = 0
    average = 0.0
    
    while True:
        value = yield average  # yield current average, receive new value
        if value is None:
            break
        total += value
        count += 1
        average = total / count


gen = running_average()
next(gen)         # Prime the generator (advance to first yield)

print(gen.send(10.0))   # 10.0
print(gen.send(20.0))   # 15.0
print(gen.send(30.0))   # 20.0
```

---

## yield from — Delegating to Sub-Generators

`yield from` delegates to another iterable or generator:

```python
def flatten(nested):
    """Flatten arbitrarily nested lists using yield from."""
    for item in nested:
        if isinstance(item, (list, tuple)):
            yield from flatten(item)  # Recursively delegate
        else:
            yield item


nested_list = [1, [2, [3, 4], 5], [6, 7], 8]
print(list(flatten(nested_list)))  # [1, 2, 3, 4, 5, 6, 7, 8]


def chain_generators(*iterables):
    """Equivalent to itertools.chain."""
    for iterable in iterables:
        yield from iterable


combined = list(chain_generators([1, 2], [3, 4], [5, 6]))
print(combined)  # [1, 2, 3, 4, 5, 6]
```

---

## Infinite Generators

Generators can be infinite — you just never exhaust them:

```python
def fibonacci():
    """Infinite Fibonacci sequence."""
    a, b = 0, 1
    while True:
        yield a
        a, b = b, a + b


def naturals(start: int = 0):
    """Infinite sequence: 0, 1, 2, 3, ..."""
    n = start
    while True:
        yield n
        n += 1


# Use itertools.islice to take the first N items from an infinite generator
from itertools import islice

first_10_fibs = list(islice(fibonacci(), 10))
print(first_10_fibs)  # [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]

first_100_naturals = list(islice(naturals(), 100))
```

---

## Real Use Case: Streaming Data Processing Pipeline

Generators compose naturally into pipelines. Each stage processes one item at a time:

```python
from pathlib import Path
from typing import Iterator
import csv


def read_csv_rows(filepath: str) -> Iterator[dict]:
    """Stage 1: Read CSV file one row at a time."""
    with open(filepath, newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            yield row


def parse_numbers(rows: Iterator[dict]) -> Iterator[dict]:
    """Stage 2: Convert numeric string fields to numbers."""
    for row in rows:
        yield {**row, "amount": float(row.get("amount", 0))}


def filter_positive(rows: Iterator[dict]) -> Iterator[dict]:
    """Stage 3: Keep only positive amounts."""
    for row in rows:
        if row["amount"] > 0:
            yield row


def add_tax(rows: Iterator[dict], tax_rate: float = 0.1) -> Iterator[dict]:
    """Stage 4: Add tax field."""
    for row in rows:
        yield {**row, "tax": row["amount"] * tax_rate}


def sum_pipeline(rows: Iterator[dict]) -> float:
    """Stage 5: Aggregate."""
    return sum(row["amount"] for row in rows)


# Compose the pipeline — no intermediate lists!
def process_sales_file(filepath: str) -> float:
    """Process an entire CSV file with constant memory usage."""
    pipeline = read_csv_rows(filepath)
    pipeline = parse_numbers(pipeline)
    pipeline = filter_positive(pipeline)
    pipeline = add_tax(pipeline)
    return sum_pipeline(pipeline)

# This processes a 1GB file in constant memory.
```

---

## itertools Module Deep Dive

`itertools` is a module of efficient, composable iterator building blocks:

```python
from itertools import (
    islice,       # Take the first N items
    chain,        # Concatenate iterables
    cycle,        # Repeat an iterable indefinitely
    repeat,       # Repeat a value N times (or indefinitely)
    count,        # Infinite counter: count(10, 2) → 10, 12, 14, ...
    dropwhile,    # Drop items while condition is True
    takewhile,    # Take items while condition is True
    filterfalse,  # Filter items where condition is False
    groupby,      # Group consecutive items by a key function
    product,      # Cartesian product of iterables
    combinations, # All combinations of r items
    permutations, # All permutations of r items
    accumulate,   # Running totals
    zip_longest,  # zip, but fills missing values
    starmap,      # Map a function that takes multiple args
)


# islice — take first N items from any iterator
from itertools import islice, count
primes_first_10 = list(islice(filter(is_prime, count(2)), 10))


# chain — concatenate multiple iterables
from itertools import chain
combined = list(chain([1, 2], [3, 4], [5, 6]))
print(combined)  # [1, 2, 3, 4, 5, 6]


# cycle — repeat forever
from itertools import cycle, islice
colors = list(islice(cycle(["red", "green", "blue"]), 7))
print(colors)  # ['red', 'green', 'blue', 'red', 'green', 'blue', 'red']


# groupby — group consecutive equal items
from itertools import groupby
data = [1, 1, 2, 2, 2, 3, 1, 1]
for key, group in groupby(data):
    print(key, list(group))
# 1 [1, 1]
# 2 [2, 2, 2]
# 3 [3]
# 1 [1, 1]
# NOTE: groupby only groups CONSECUTIVE items — sort first if needed


# product — Cartesian product
from itertools import product
sizes = ["S", "M", "L"]
colors = ["red", "blue"]
variants = list(product(sizes, colors))
# [('S', 'red'), ('S', 'blue'), ('M', 'red'), ('M', 'blue'), ...]


# combinations and permutations
from itertools import combinations, permutations
items = ["a", "b", "c"]
pairs = list(combinations(items, 2))      # [('a','b'), ('a','c'), ('b','c')]
ordered = list(permutations(items, 2))    # [('a','b'), ('a','c'), ('b','a'), ...]


# accumulate — running totals
from itertools import accumulate
import operator
values = [1, 2, 3, 4, 5]
running_sum = list(accumulate(values))                    # [1, 3, 6, 10, 15]
running_product = list(accumulate(values, operator.mul)) # [1, 2, 6, 24, 120]
```

---

## Quick Reference

| Concept | Syntax | Use When |
|---------|--------|----------|
| Generator function | `yield value` | Produce a sequence lazily |
| Generator expression | `(expr for x in iterable)` | Simple lazy transformation |
| Infinite generator | `while True: yield` | Sequence with no end |
| Two-way comms | `value = yield result` | Coroutine-style processing |
| Delegation | `yield from iterable` | Flatten or chain generators |
| `islice` | `islice(gen, n)` | Take first N from any iterator |
| `chain` | `chain(a, b, c)` | Concatenate iterables |

**The core insight:** A generator produces values one at a time, on demand. This is what allows processing a 10GB file with 100MB of RAM.
