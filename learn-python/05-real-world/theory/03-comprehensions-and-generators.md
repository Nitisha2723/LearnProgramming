# Comprehensions and Generators

Generators are Python's answer to processing large datasets efficiently. Instead of
building an entire list in memory, a generator computes values one at a time, on demand.

---

## Comprehensions Review

You already know list comprehensions. Python also has dict and set comprehensions.

```python
# List comprehension
squares = [x**2 for x in range(10)]

# Dict comprehension
square_map = {x: x**2 for x in range(10)}

# Set comprehension
unique_lengths = {len(word) for word in words}

# Generator expression (covered below)
lazy_squares = (x**2 for x in range(10))
```

---

## Generator Expressions

A generator expression looks like a list comprehension but uses `()` instead of `[]`.
It does NOT build a list — it creates a generator object that computes values lazily.

```python
# List comprehension — builds entire list in memory
squares_list = [x**2 for x in range(1_000_000)]    # ~8MB in memory

# Generator expression — computes each value on demand
squares_gen  = (x**2 for x in range(1_000_000))    # ~200 bytes

import sys
print(sys.getsizeof(squares_list))   # ~8,697,456 bytes
print(sys.getsizeof(squares_gen))    # ~208 bytes
```

You iterate over a generator the same way as a list, but **you can only do it once**:

```python
gen = (x**2 for x in range(5))
for val in gen:
    print(val)      # 0, 1, 4, 9, 16

# Try iterating again — nothing!
for val in gen:
    print(val)      # No output — generator is exhausted
```

---

## Generator Functions — the `yield` Keyword

A generator function uses `yield` instead of `return`. Calling it returns a generator
object without running any code yet.

```python
def count_up(start, end):
    """A generator that yields numbers from start to end."""
    current = start
    while current <= end:
        yield current      # Pause here, return value to caller
        current += 1       # Resume from here next time

# Using the generator
for num in count_up(1, 5):
    print(num)   # 1 2 3 4 5

# Only generates values when asked
gen = count_up(1, 1_000_000)
first = next(gen)    # 1     — computed one value
second = next(gen)   # 2     — computed one more
# 999,998 values not yet computed
```

### How `yield` Works

When a generator function runs:
1. It executes until it hits a `yield`
2. It "pauses" and returns the yielded value to the caller
3. The function's state (local variables, position) is saved
4. When `next()` is called again, execution resumes from right after the `yield`

```python
def demo():
    print("Start")
    yield 1
    print("After first yield")
    yield 2
    print("After second yield")
    yield 3
    print("Done")

gen = demo()
print(next(gen))    # "Start"  → 1
print(next(gen))    # "After first yield" → 2
print(next(gen))    # "After second yield" → 3
# next(gen)         # StopIteration — generator is done
```

---

## Why Generators? Memory Efficiency

### Example: Processing a 1GB log file

```python
# BAD: loads the entire 1GB file into memory
def get_errors_slow(filename):
    lines = open(filename).readlines()   # 1GB in RAM!
    return [line for line in lines if "ERROR" in line]

# GOOD: processes one line at a time, uses ~kilobytes of RAM
def get_errors_fast(filename):
    with open(filename) as f:
        for line in f:           # Reads one line at a time
            if "ERROR" in line:
                yield line.rstrip()
```

### Generator Pipeline

Generators chain naturally — each stage is lazy:

```python
def read_lines(filename):
    with open(filename) as f:
        yield from f    # yield each line lazily

def filter_errors(lines):
    for line in lines:
        if "ERROR" in line:
            yield line

def parse_timestamp(lines):
    for line in lines:
        timestamp, _, message = line.partition(" - ")
        yield timestamp.strip(), message.strip()

# Build the pipeline — NO DATA HAS BEEN READ YET
raw_lines = read_lines("server.log")
error_lines = filter_errors(raw_lines)
parsed = parse_timestamp(error_lines)

# Data flows through the pipeline one item at a time
for timestamp, message in parsed:
    print(f"[{timestamp}] {message}")
```

This entire pipeline uses O(1) memory regardless of file size.

---

## `yield from` — Delegating to Sub-Generators

`yield from` lets you delegate to another iterable or generator:

```python
def chain(*iterables):
    for it in iterables:
        yield from it    # Yields every item from each iterable in turn

list(chain([1, 2], [3, 4], [5, 6]))   # [1, 2, 3, 4, 5, 6]

# vs itertools.chain — same concept, but built-in and faster
from itertools import chain
list(chain([1, 2], [3, 4], [5, 6]))
```

```python
def flatten(nested):
    """Flatten arbitrarily nested lists."""
    for item in nested:
        if isinstance(item, list):
            yield from flatten(item)
        else:
            yield item

list(flatten([1, [2, [3, 4]], [5, 6]]))   # [1, 2, 3, 4, 5, 6]
```

---

## The `itertools` Module

`itertools` provides building blocks for working with iterators. These are all lazy.

```python
import itertools
```

### `chain` — Concatenate Iterables

```python
from itertools import chain

combined = list(chain([1, 2], [3, 4], "abc"))
# [1, 2, 3, 4, 'a', 'b', 'c']

# chain.from_iterable — for a list of iterables
lists = [[1, 2], [3, 4], [5, 6]]
flat = list(chain.from_iterable(lists))   # [1, 2, 3, 4, 5, 6]
```

### `islice` — Lazy Slicing

```python
from itertools import islice

gen = (x**2 for x in range(1_000_000))
first_5 = list(islice(gen, 5))     # [0, 1, 4, 9, 16] — only 5 values computed
```

### `cycle` — Repeat Infinitely

```python
from itertools import cycle, islice

colors = cycle(["red", "green", "blue"])
pattern = list(islice(colors, 7))
# ["red", "green", "blue", "red", "green", "blue", "red"]
```

### `product` — Cartesian Product

```python
from itertools import product

# Equivalent to nested for loops
for x, y in product([1, 2], [3, 4]):
    print(x, y)
# (1,3), (1,4), (2,3), (2,4)

# Generate all possible values
suits = ["♠", "♥", "♦", "♣"]
values = ["A"] + list(map(str, range(2, 11))) + ["J", "Q", "K"]
deck = list(product(values, suits))
print(len(deck))   # 52
```

### `combinations` and `permutations`

```python
from itertools import combinations, permutations

items = ["a", "b", "c", "d"]

# Combinations: choose r, order doesn't matter
list(combinations(items, 2))
# [("a","b"), ("a","c"), ("a","d"), ("b","c"), ("b","d"), ("c","d")]

# Permutations: choose r, order matters
list(permutations(items, 2))
# [("a","b"), ("a","c"), ("a","d"), ("b","a"), ...]
```

### `groupby` — Group Consecutive Items

```python
from itertools import groupby

# IMPORTANT: data must be sorted by the grouping key first!
data = sorted([
    ("math", 90), ("science", 85), ("math", 78),
    ("science", 92), ("english", 88),
], key=lambda x: x[0])

for subject, group in groupby(data, key=lambda x: x[0]):
    scores = [score for _, score in group]
    print(f"{subject}: {scores}, avg={sum(scores)/len(scores):.1f}")
```

### `takewhile` and `dropwhile`

```python
from itertools import takewhile, dropwhile

data = [2, 4, 6, 7, 8, 9, 10]

# Take while condition is True, stop at first False
evens = list(takewhile(lambda x: x % 2 == 0, data))
# [2, 4, 6]  — stops at 7

# Drop while condition is True, then take everything
after_odd = list(dropwhile(lambda x: x % 2 == 0, data))
# [7, 8, 9, 10]  — drops 2,4,6; starts at 7
```

---

## Real-World Example: Processing Large Datasets

Imagine you have a 500MB CSV file with 5 million rows of transaction data.

```python
import csv
from itertools import islice

def read_transactions(filepath):
    """Lazy generator — reads one row at a time."""
    with open(filepath, newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            yield {
                "id":     row["transaction_id"],
                "amount": float(row["amount"]),
                "user":   row["user_id"],
                "date":   row["date"],
            }

def filter_large(transactions, min_amount=1000):
    for tx in transactions:
        if tx["amount"] >= min_amount:
            yield tx

def add_tax(transactions, rate=0.08):
    for tx in transactions:
        yield {**tx, "tax": tx["amount"] * rate}

# Build a lazy pipeline — no data loaded yet
pipeline = read_transactions("transactions.csv")
pipeline = filter_large(pipeline, min_amount=1000)
pipeline = add_tax(pipeline)

# Process — one transaction at a time, regardless of file size
total_tax = sum(tx["tax"] for tx in pipeline)
print(f"Total tax on large transactions: ${total_tax:,.2f}")
```

---

## When to Use Generators vs Lists

| Use a **list** when                    | Use a **generator** when                    |
|----------------------------------------|---------------------------------------------|
| You need random access (`data[5]`)     | You only iterate once, in order             |
| You'll iterate multiple times          | The data is very large (file, DB, API)      |
| You need `len()`                       | You're building a pipeline                  |
| You need to sort the results           | You only need the first N results           |
| Results are small                      | Memory is a concern                         |

```python
# Need to iterate twice — use a list
data = list(generate_data())    # Convert once
print(len(data))                # len() works
for item in data:               # First pass
    analyze(item)
for item in data:               # Second pass
    report(item)

# Need only first N — generator is perfect
gen = generate_data()
first_10 = list(islice(gen, 10))
```
