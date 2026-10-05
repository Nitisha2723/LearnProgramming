# Theory 03: Lists and Tuples

Lists are Python's most versatile and widely used data structure.
Tuples are their immutable cousins. Together they handle the vast majority of ordered-data needs.

---

## Table of Contents

1. [Lists — Python's Workhorse](#1-lists--pythons-workhorse)
2. [Indexing and Slicing](#2-indexing-and-slicing)
3. [List Methods](#3-list-methods)
4. [List Comprehensions](#4-list-comprehensions)
5. [Nested Lists](#5-nested-lists)
6. [Tuples — Immutable Sequences](#6-tuples--immutable-sequences)
7. [Tuple Packing and Unpacking](#7-tuple-packing-and-unpacking)
8. [Named Tuples](#8-named-tuples)
9. [List vs Tuple — Choosing the Right One](#9-list-vs-tuple--choosing-the-right-one)
10. [Common Patterns: filter, map, reduce](#10-common-patterns-filter-map-reduce)

---

## 1. Lists — Python's Workhorse

A list is an **ordered, mutable** collection that can hold any mix of types.

```python
# Creation
empty = []
numbers = [1, 2, 3, 4, 5]
mixed = [42, "hello", 3.14, True, None]
nested = [[1, 2], [3, 4], [5, 6]]

# From other iterables
from_range = list(range(1, 6))         # [1, 2, 3, 4, 5]
from_string = list("hello")            # ['h', 'e', 'l', 'l', 'o']
from_tuple = list((10, 20, 30))        # [10, 20, 30]

# Basic properties
print(len(numbers))      # 5
print(type(numbers))     # <class 'list'>
print(42 in mixed)       # True
print(99 in numbers)     # False
```

**Real-world analogy:** A shopping list — ordered, you can add/remove items, items can repeat,
and you can hold any kind of thing.

---

## 2. Indexing and Slicing

### Indexing

```python
fruits = ["apple", "banana", "cherry", "date", "elderberry"]
#          0         1          2         3         4
#         -5        -4         -3        -2        -1

print(fruits[0])     # apple   (first item)
print(fruits[2])     # cherry
print(fruits[-1])    # elderberry (last item)
print(fruits[-2])    # date
```

Python supports negative indices — `-1` is always the last element, `-2` the second-to-last, etc.
This is one of Python's most convenient features.

### Slicing

Slicing extracts a sub-list: `list[start:stop:step]`

```python
nums = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

nums[2:5]       # [2, 3, 4]       — stop is exclusive
nums[:4]        # [0, 1, 2, 3]    — omit start → from beginning
nums[6:]        # [6, 7, 8, 9]    — omit stop → to end
nums[:]         # full copy of the list
nums[::2]       # [0, 2, 4, 6, 8] — every 2nd element
nums[1::2]      # [1, 3, 5, 7, 9] — odd indices
nums[::-1]      # [9, 8, 7, 6, 5, 4, 3, 2, 1, 0] — reversed!
nums[7:2:-1]    # [7, 6, 5, 4, 3] — reverse slice
```

### Slicing Creates a New List

```python
original = [1, 2, 3, 4, 5]
copy = original[:]     # shallow copy

copy[0] = 99
print(original)    # [1, 2, 3, 4, 5]  — unchanged
print(copy)        # [99, 2, 3, 4, 5]
```

---

## 3. List Methods

### Adding Elements

```python
fruits = ["apple", "banana"]

fruits.append("cherry")        # add to end
print(fruits)   # ['apple', 'banana', 'cherry']

fruits.insert(1, "avocado")    # insert at index 1
print(fruits)   # ['apple', 'avocado', 'banana', 'cherry']

fruits.extend(["date", "elderberry"])  # add all from iterable
print(fruits)   # ['apple', 'avocado', 'banana', 'cherry', 'date', 'elderberry']

# + creates a new list; extend modifies in place
combined = ["a", "b"] + ["c", "d"]    # ['a', 'b', 'c', 'd']
```

### Removing Elements

```python
fruits = ["apple", "banana", "cherry", "banana", "date"]

fruits.remove("banana")    # removes FIRST occurrence
print(fruits)   # ['apple', 'cherry', 'banana', 'date']

popped = fruits.pop()      # removes and returns last item
print(popped)   # date
print(fruits)   # ['apple', 'cherry', 'banana']

popped = fruits.pop(0)     # removes and returns item at index 0
print(popped)   # apple

del fruits[0]              # delete by index (no return value)
fruits.clear()             # remove all items → []
```

### Searching

```python
fruits = ["apple", "banana", "cherry", "banana"]

print(fruits.index("banana"))   # 1  (first occurrence)
print(fruits.count("banana"))   # 2  (how many times)

# Safe search (won't raise ValueError)
if "mango" in fruits:
    idx = fruits.index("mango")
```

### Sorting

```python
numbers = [3, 1, 4, 1, 5, 9, 2, 6]

# sort() modifies in place, returns None
numbers.sort()
print(numbers)    # [1, 1, 2, 3, 4, 5, 6, 9]

numbers.sort(reverse=True)
print(numbers)    # [9, 6, 5, 4, 3, 2, 1, 1]

# sorted() returns a new list, original unchanged
original = [3, 1, 4, 1, 5]
sorted_copy = sorted(original)
print(original)      # [3, 1, 4, 1, 5]  unchanged
print(sorted_copy)   # [1, 1, 3, 4, 5]

# Sort with a key function
words = ["banana", "apple", "fig", "cherry"]
words.sort(key=len)           # sort by length
print(words)    # ['fig', 'apple', 'banana', 'cherry']

words.sort(key=str.lower)     # case-insensitive sort
```

### Other Useful Methods

```python
fruits = ["apple", "banana", "cherry"]

fruits.reverse()         # reverses in place
print(fruits)   # ['cherry', 'banana', 'apple']

print(list(reversed(fruits)))  # non-destructive reverse

fruits2 = fruits.copy()  # shallow copy
```

---

## 4. List Comprehensions

List comprehensions are the idiomatic Python way to create lists from other iterables.
They are more readable and often faster than equivalent `for` + `append` patterns.

### Basic Comprehension

```python
# [expression for item in iterable]

squares = [x ** 2 for x in range(1, 6)]
# [1, 4, 9, 16, 25]

words = ["hello", "world", "python"]
upper_words = [w.upper() for w in words]
# ['HELLO', 'WORLD', 'PYTHON']
```

### With Filter

```python
# [expression for item in iterable if condition]

evens = [x for x in range(1, 11) if x % 2 == 0]
# [2, 4, 6, 8, 10]

long_words = [w for w in words if len(w) > 4]
# ['hello', 'world', 'python']
```

### With Transformation

```python
# Transform and filter at once
positive_squares = [x ** 2 for x in range(-5, 6) if x > 0]
# [1, 4, 9, 16, 25]
```

### Nested Comprehensions

```python
# Flatten a 2D list
matrix = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
flat = [n for row in matrix for n in row]
# [1, 2, 3, 4, 5, 6, 7, 8, 9]

# Cartesian product
pairs = [(x, y) for x in range(1, 4) for y in range(1, 4) if x != y]
# [(1, 2), (1, 3), (2, 1), (2, 3), (3, 1), (3, 2)]
```

### Dictionary and Set Comprehensions

The same syntax works for other collections:

```python
# Dict comprehension
word_lengths = {word: len(word) for word in words}
# {'hello': 5, 'world': 5, 'python': 6}

# Set comprehension (unique values)
unique_lengths = {len(word) for word in words}
# {5, 6}
```

### Readability Warning

Comprehensions become hard to read when too complex. If you need more than two `for`/`if`
clauses, a regular `for` loop is usually clearer.

---

## 5. Nested Lists

```python
# A 3x3 matrix
matrix = [
    [1, 2, 3],
    [4, 5, 6],
    [7, 8, 9],
]

# Accessing elements: matrix[row][column]
print(matrix[0][0])    # 1 (top-left)
print(matrix[1][2])    # 6 (row 1, col 2)
print(matrix[2][-1])   # 9 (last row, last col)

# Iterating over a matrix
for row in matrix:
    for value in row:
        print(value, end=" ")
    print()    # newline after each row

# Transposing a matrix with comprehension
transposed = [[matrix[r][c] for r in range(3)] for c in range(3)]
```

---

## 6. Tuples — Immutable Sequences

A tuple is like a list, but **immutable** — once created, you cannot change it.

```python
# Creation
empty = ()
single = (42,)          # note the trailing comma — required for single-element tuple!
coordinates = (3, 4)
rgb = (255, 128, 0)
mixed = (1, "hello", 3.14)

# Without parentheses (tuple packing)
point = 3, 4            # same as (3, 4)

print(type(coordinates))   # <class 'tuple'>
print(len(rgb))            # 3
print(rgb[0])              # 255
print(rgb[-1])             # 0
```

### Immutability

```python
rgb = (255, 128, 0)
rgb[0] = 200    # TypeError: 'tuple' object does not support item assignment

# But if a tuple contains a mutable object, that object can change
data = ([1, 2, 3], "hello")
data[0].append(4)    # modifying the list inside the tuple is allowed
print(data)          # ([1, 2, 3, 4], 'hello')
```

### Tuple Methods

Tuples have only two methods (because they are immutable):

```python
t = (1, 2, 3, 2, 1)
print(t.count(2))    # 2
print(t.index(3))    # 2
```

---

## 7. Tuple Packing and Unpacking

### Packing

```python
# Implicit tuple creation
point = 10, 20         # (10, 20)
person = "Alice", 30   # ('Alice', 30)
```

### Unpacking

```python
coordinates = (3, 4)
x, y = coordinates
print(x)    # 3
print(y)    # 4

# Swap variables (classic Python idiom)
a, b = 1, 2
a, b = b, a    # swap in one line
print(a, b)    # 2 1

# Unpack with *rest
first, *rest = [1, 2, 3, 4, 5]
print(first)   # 1
print(rest)    # [2, 3, 4, 5]

*start, last = [1, 2, 3, 4, 5]
print(start)   # [1, 2, 3, 4]
print(last)    # 5

head, *middle, tail = [1, 2, 3, 4, 5]
print(head, middle, tail)   # 1 [2, 3, 4] 5
```

### Function Return Values

```python
def get_user():
    return "Alice", 30, "Berlin"    # returns a tuple

name, age, city = get_user()        # immediate unpacking
print(name, age, city)   # Alice 30 Berlin
```

---

## 8. Named Tuples

`collections.namedtuple` creates tuple subclasses with named fields.
Think of them as lightweight data classes.

```python
from collections import namedtuple

# Define the type
Point = namedtuple("Point", ["x", "y"])
Person = namedtuple("Person", ["name", "age", "city"])

# Create instances
p = Point(3, 4)
alice = Person("Alice", 30, "Berlin")

# Access by name or index
print(p.x, p.y)              # 3 4
print(p[0], p[1])            # 3 4  (still a tuple)

print(alice.name)            # Alice
print(alice.age)             # 30

# Unpack like a regular tuple
name, age, city = alice
print(f"{name} is {age} years old")

# Convert to dict
print(alice._asdict())
# {'name': 'Alice', 'age': 30, 'city': 'Berlin'}

# "Replace" (creates a new tuple — they're immutable)
older_alice = alice._replace(age=31)
print(older_alice)   # Person(name='Alice', age=31, city='Berlin')
```

### Python 3.6+ Typed Version

```python
from typing import NamedTuple

class Point(NamedTuple):
    x: float
    y: float
    z: float = 0.0    # default value

p = Point(1.0, 2.0)
print(p)    # Point(x=1.0, y=2.0, z=0.0)
```

---

## 9. List vs Tuple — Choosing the Right One

| Factor | List | Tuple |
|--------|------|-------|
| Mutability | Mutable | Immutable |
| Syntax | `[1, 2, 3]` | `(1, 2, 3)` |
| Use when | Collection may change | Data is fixed |
| Use when | Homogeneous items | Heterogeneous record |
| Performance | Slightly more memory | Slightly faster |
| Hashable | No (can't be dict key) | Yes (if contents are) |
| Example | List of tasks | (name, age) pair |

### Guidelines

Use a **list** when:
- The collection will grow or shrink
- Items are the same "kind" of thing (a list of names, a list of scores)
- You need list methods (append, sort, etc.)

Use a **tuple** when:
- The structure is fixed (coordinates, RGB, database row)
- You want to signal "this should not change"
- You need to use it as a dict key or set element
- Returning multiple values from a function

---

## 10. Common Patterns: filter, map, reduce

### Filter Pattern

Keep items matching a condition:

```python
numbers = [1, -2, 3, -4, 5, -6]

# List comprehension (preferred)
positives = [x for x in numbers if x > 0]

# filter() (functional style)
positives = list(filter(lambda x: x > 0, numbers))
```

### Map Pattern

Transform every item:

```python
words = ["hello", "world"]

# List comprehension (preferred)
upper = [w.upper() for w in words]

# map() (functional style)
upper = list(map(str.upper, words))
```

### Reduce Pattern

Aggregate to a single value:

```python
from functools import reduce

numbers = [1, 2, 3, 4, 5]

# Built-in sum (use this for simple sums)
total = sum(numbers)

# reduce() for custom aggregation
product = reduce(lambda x, y: x * y, numbers)   # 120

# max and min have their own builtins
print(max(numbers))    # 5
print(min(numbers))    # 1
```

### All and Any

```python
numbers = [2, 4, 6, 8, 10]

print(all(x % 2 == 0 for x in numbers))    # True — all are even
print(any(x > 9 for x in numbers))          # True — at least one is > 9
print(all(x > 5 for x in numbers))          # False — not all are > 5
```

Note: `all()` and `any()` work with **generator expressions** (similar to comprehensions
but without `[]`) — they are lazy and short-circuit, which makes them efficient.

---

## Quick Reference

| Operation | List | Tuple |
|-----------|------|-------|
| Create | `[1, 2, 3]` | `(1, 2, 3)` |
| Index | `lst[0]` | `t[0]` |
| Slice | `lst[1:3]` | `t[1:3]` |
| Length | `len(lst)` | `len(t)` |
| Append | `lst.append(x)` | — |
| Extend | `lst.extend(iter)` | — |
| Remove | `lst.remove(x)` | — |
| Pop | `lst.pop()` | — |
| Sort | `lst.sort()` | — |
| Reverse | `lst.reverse()` | — |
| Count | `lst.count(x)` | `t.count(x)` |
| Index of | `lst.index(x)` | `t.index(x)` |
| Unpack | `a, b = lst` | `a, b = t` |
| Comprehension | `[x*2 for x in lst]` | — |

---

## Key Takeaways

1. Lists are mutable; tuples are immutable — this is the core distinction.
2. Negative indexing (`list[-1]`) is a first-class Python feature, not a trick.
3. Slice syntax `[start:stop:step]` is incredibly flexible — `[::-1]` reverses anything.
4. List comprehensions are idiomatic Python — prefer them over `for` + `append`.
5. Tuples are for structured records; lists are for collections of like items.
6. `namedtuple` gives you readable, self-documenting data structures for free.
7. Starred unpacking (`a, *rest = lst`) makes many patterns elegant.

---

*Next: `theory/04-strings-in-depth.md` — Python's immutable sequence of characters.*
