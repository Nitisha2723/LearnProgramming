"""
lists_demo.py — Demonstrations of all Python list and tuple operations.

Run with:  python code/lists_demo.py

Covers:
  - List creation, indexing, slicing
  - All important list methods
  - List comprehensions (basic, filtered, nested)
  - Nested lists / matrices
  - Tuples and immutability
  - Tuple packing and unpacking
  - Named tuples
  - Common patterns: filter, map, reduce, all, any
"""

from collections import namedtuple
from functools import reduce

# ─────────────────────────────────────────────────────────────
# SECTION 1: List Creation
# ─────────────────────────────────────────────────────────────
print("=" * 50)
print("SECTION 1: List Creation")
print("=" * 50)

# Different ways to create lists
empty = []
literal = [1, 2, 3, 4, 5]
mixed = [42, "hello", 3.14, True, None]
from_range = list(range(1, 6))
from_string = list("hello")
from_set = sorted(list({3, 1, 4, 1, 5, 9}))  # sorted because sets are unordered
repeated = [0] * 5                             # [0, 0, 0, 0, 0]

print(f"empty:       {empty}")
print(f"literal:     {literal}")
print(f"mixed:       {mixed}")
print(f"from_range:  {from_range}")
print(f"from_string: {from_string}")
print(f"from_set:    {from_set}")
print(f"repeated:    {repeated}")


# ─────────────────────────────────────────────────────────────
# SECTION 2: Indexing and Slicing
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 2: Indexing and Slicing")
print("=" * 50)

nums = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
print(f"List: {nums}")
print(f"  nums[0]     = {nums[0]}   ← first")
print(f"  nums[-1]    = {nums[-1]}   ← last")
print(f"  nums[3]     = {nums[3]}   ← index 3")
print(f"  nums[-3]    = {nums[-3]}   ← third from end")

print("\nSlices:")
print(f"  nums[2:5]   = {nums[2:5]}       ← indices 2,3,4")
print(f"  nums[:4]    = {nums[:4]}    ← first 4")
print(f"  nums[7:]    = {nums[7:]}       ← from index 7")
print(f"  nums[:]     = {nums[:]}  ← full copy")
print(f"  nums[::2]   = {nums[::2]}   ← every 2nd")
print(f"  nums[1::2]  = {nums[1::2]}   ← every 2nd, offset")
print(f"  nums[::-1]  = {nums[::-1]}  ← reversed!")

# Slice assignment — modify a section
mutable = [1, 2, 3, 4, 5]
mutable[1:3] = [20, 30, 40]  # replace indices 1 and 2 with three values
print(f"\nAfter mutable[1:3] = [20, 30, 40]: {mutable}")


# ─────────────────────────────────────────────────────────────
# SECTION 3: List Methods
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 3: List Methods")
print("=" * 50)

fruits = ["apple", "banana", "cherry"]
print(f"Starting: {fruits}")

# Adding
fruits.append("date")
print(f"After append('date'):       {fruits}")

fruits.insert(2, "avocado")
print(f"After insert(2, 'avocado'): {fruits}")

fruits.extend(["elderberry", "fig"])
print(f"After extend([...]):        {fruits}")

print()

# Removing
fruits.remove("avocado")       # removes first occurrence by value
print(f"After remove('avocado'):    {fruits}")

popped = fruits.pop()           # removes and returns last
print(f"After pop() (got '{popped}'): {fruits}")

popped2 = fruits.pop(1)        # removes and returns index 1
print(f"After pop(1) (got '{popped2}'): {fruits}")

print()

# Searching
fruits = ["apple", "banana", "cherry", "banana", "date"]
print(f"List: {fruits}")
print(f"  index('banana')  = {fruits.index('banana')}  ← first occurrence")
print(f"  count('banana')  = {fruits.count('banana')}  ← total count")
print(f"  'cherry' in list = {'cherry' in fruits}")
print(f"  'mango' in list  = {'mango' in fruits}")

print()

# Sorting
numbers = [3, 1, 4, 1, 5, 9, 2, 6, 5]
original = numbers.copy()     # make a copy before sorting
numbers.sort()
print(f"Original:      {original}")
print(f"After sort():  {numbers}")

numbers.sort(reverse=True)
print(f"Reversed sort: {numbers}")

words = ["banana", "Apple", "cherry", "date"]
words.sort(key=str.lower)      # case-insensitive sort
print(f"Case-insensitive sort: {words}")

# sorted() — non-destructive
nums_copy = [3, 1, 4, 1, 5]
sorted_copy = sorted(nums_copy)
print(f"\nOriginal unchanged: {nums_copy}")
print(f"sorted() result:    {sorted_copy}")

# reverse()
letters = ["c", "a", "b"]
letters.reverse()
print(f"\nAfter reverse(): {letters}")

print(f"reversed() non-destructive: {list(reversed(letters))}")


# ─────────────────────────────────────────────────────────────
# SECTION 4: List Comprehensions
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 4: List Comprehensions")
print("=" * 50)

# Basic transformation
squares = [x**2 for x in range(1, 8)]
print(f"Squares:         {squares}")

# With filter
even_squares = [x**2 for x in range(1, 11) if x % 2 == 0]
print(f"Even squares:    {even_squares}")

# String transformation
sentences = ["  hello world  ", "PYTHON is great", "learn every day"]
cleaned = [s.strip().lower() for s in sentences]
print(f"\nCleaned strings: {cleaned}")

# Conditional expression in comprehension
numbers = [-3, -1, 0, 2, 5, -4]
abs_values = [x if x >= 0 else -x for x in numbers]
print(f"\nAbsolute values (without abs()): {abs_values}")

# Nested comprehension — multiply every pair
pairs = [(x, y) for x in range(1, 4) for y in range(1, 4)]
print(f"\n3×3 coordinate pairs: {pairs}")

# Dict comprehension
word_lengths = {word: len(word) for word in ["apple", "banana", "fig"]}
print(f"\nWord lengths dict: {word_lengths}")

# Set comprehension — unique values
lengths = {len(w) for w in ["apple", "fig", "cherry", "kiwi", "pear"]}
print(f"Unique lengths set: {lengths}")


# ─────────────────────────────────────────────────────────────
# SECTION 5: Nested Lists
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 5: Nested Lists (Matrix)")
print("=" * 50)

matrix = [
    [1, 2, 3],
    [4, 5, 6],
    [7, 8, 9],
]

print("Matrix:")
for row in matrix:
    print(f"  {row}")

print(f"\n  matrix[0][0] = {matrix[0][0]}  ← top-left")
print(f"  matrix[1][2] = {matrix[1][2]}  ← row 1, col 2")
print(f"  matrix[2][-1] = {matrix[2][-1]}  ← last row, last col")

# Transposing with comprehension
transposed = [[matrix[r][c] for r in range(3)] for c in range(3)]
print("\nTransposed:")
for row in transposed:
    print(f"  {row}")

# Flatten
flat = [n for row in matrix for n in row]
print(f"\nFlattened: {flat}")


# ─────────────────────────────────────────────────────────────
# SECTION 6: Tuples
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 6: Tuples")
print("=" * 50)

# Creation
point = (3, 4)
rgb = (255, 128, 0)
single = (42,)          # trailing comma is required for single-element tuple!
empty_tuple = ()

print(f"point:  {point},  type: {type(point)}")
print(f"single: {single}, type: {type(single)}")
print(f"(42) without comma: {type((42))}  ← this is just an int!")

# Immutability
print("\nImmutability:")
try:
    point[0] = 99       # TypeError
except TypeError as e:
    print(f"  Cannot modify tuple: {e}")

# Tuples as dict keys (lists cannot be used as keys)
distances = {
    (0, 0): 0,
    (3, 4): 5.0,
    (5, 12): 13.0,
}
print(f"\nTuple as dict key: {distances[(3, 4)]}")

# Tuple methods
t = (1, 2, 3, 2, 1, 2)
print(f"\nTuple {t}:")
print(f"  count(2)  = {t.count(2)}")
print(f"  index(3)  = {t.index(3)}")


# ─────────────────────────────────────────────────────────────
# SECTION 7: Tuple Packing and Unpacking
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 7: Tuple Packing and Unpacking")
print("=" * 50)

# Packing (implicit tuple creation)
coordinates = 10, 20            # (10, 20)
print(f"Packed: {coordinates}")

# Unpacking
x, y = coordinates
print(f"Unpacked: x={x}, y={y}")

# Swap variables — the Pythonic way
a, b = 1, 2
print(f"\nBefore swap: a={a}, b={b}")
a, b = b, a
print(f"After swap:  a={a}, b={b}")

# Extended unpacking with *
numbers = [1, 2, 3, 4, 5]
first, *rest = numbers
print(f"\nfirst={first}, rest={rest}")

*start, last = numbers
print(f"start={start}, last={last}")

head, *middle, tail = numbers
print(f"head={head}, middle={middle}, tail={tail}")


def get_person():
    """Function returning multiple values (as a tuple)."""
    return "Alice", 30, "Berlin"


name, age, city = get_person()
print(f"\nUnpacked return: {name}, {age}, {city}")

# Ignore values with _
name, _, city = get_person()    # _ is a convention for "don't care"
print(f"Selective unpack: {name} from {city}")


# ─────────────────────────────────────────────────────────────
# SECTION 8: Named Tuples
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 8: Named Tuples")
print("=" * 50)

# Classic namedtuple
Point = namedtuple("Point", ["x", "y"])
p = Point(3, 4)
print(f"Point: {p}")
print(f"  Access by name:  p.x={p.x}, p.y={p.y}")
print(f"  Access by index: p[0]={p[0]}, p[1]={p[1]}")
print(f"  As dict: {p._asdict()}")

# Replace (creates new tuple — immutable)
p2 = p._replace(x=10)
print(f"  p._replace(x=10) = {p2}")

# Typed NamedTuple (Python 3.6+)
from typing import NamedTuple


class Student(NamedTuple):
    name: str
    grade: float
    major: str = "Undeclared"   # default value


alice = Student("Alice", 3.8, "Computer Science")
bob = Student("Bob", 3.5)      # uses default major

print(f"\nStudent: {alice}")
print(f"Student: {bob}")
print(f"  alice.grade = {alice.grade}")

students = [
    Student("Alice", 3.8, "CS"),
    Student("Bob", 3.2, "Math"),
    Student("Carol", 3.9, "Physics"),
]
top = max(students, key=lambda s: s.grade)
print(f"  Top student: {top.name} ({top.grade})")


# ─────────────────────────────────────────────────────────────
# SECTION 9: Common Patterns
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 9: Common Patterns")
print("=" * 50)

numbers = [3, -1, 4, -1, 5, -9, 2, 6, -5]

# Filter pattern
positives = [x for x in numbers if x > 0]
print(f"Positives: {positives}")

# Map pattern (transform)
doubled = [x * 2 for x in numbers]
print(f"Doubled:   {doubled}")

# Reduce pattern (aggregate)
total = sum(numbers)
product = reduce(lambda a, b: a * b, [1, 2, 3, 4, 5])
print(f"Sum:       {total}")
print(f"Product of 1..5: {product}")

# all() and any()
scores = [82, 91, 77, 88, 95]
print(f"\nScores: {scores}")
print(f"  All above 70?  {all(s > 70 for s in scores)}")
print(f"  Any above 90?  {any(s > 90 for s in scores)}")
print(f"  All above 90?  {all(s > 90 for s in scores)}")

# zip for parallel processing
names = ["Alice", "Bob", "Carol"]
print(f"\nPairs from zip:")
for name, score in zip(names, scores):
    print(f"  {name}: {score}")

# enumerate for indexed iteration
print(f"\nenumerate from 1:")
for i, name in enumerate(names, start=1):
    print(f"  {i}. {name}")


print("\n--- Demo Complete ---")
