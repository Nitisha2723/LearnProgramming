"""
lists_advanced.py — Advanced list operations in Python

Topics covered:
  - Advanced slicing with start:stop:step
  - List comprehensions (basic, filtered, nested)
  - Sorting with key functions
  - List as stack
  - Why list.pop(0) is bad and how deque fixes it
"""

# =============================================================================
# SECTION 1: Advanced Slicing
# =============================================================================

print("=" * 60)
print("SECTION 1: Advanced Slicing")
print("=" * 60)

nums = list(range(10))  # [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
print(f"Original:    {nums}")

# Basic slices
print(f"[2:5]        {nums[2:5]}")     # [2, 3, 4]
print(f"[:4]         {nums[:4]}")      # [0, 1, 2, 3]
print(f"[6:]         {nums[6:]}")      # [6, 7, 8, 9]

# Step
print(f"[::2]        {nums[::2]}")     # [0, 2, 4, 6, 8]
print(f"[1::2]       {nums[1::2]}")    # [1, 3, 5, 7, 9]

# Negative indices
print(f"[-3:]        {nums[-3:]}")     # [7, 8, 9]
print(f"[:-2]        {nums[:-2]}")     # [0, 1, 2, 3, 4, 5, 6, 7]

# Reversal — the most common use of step=-1
print(f"[::-1]       {nums[::-1]}")    # [9, 8, 7, 6, 5, 4, 3, 2, 1, 0]

# Reverse a string
text = "Hello, World!"
print(f"\nReversed string: {text[::-1]}")

# Rotate a list using slices
def rotate_right(lst, k):
    """Rotate list right by k positions."""
    k = k % len(lst)
    return lst[-k:] + lst[:-k]

print(f"\nRotate right by 3: {rotate_right(nums, 3)}")

# =============================================================================
# SECTION 2: List Comprehensions
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 2: List Comprehensions")
print("=" * 60)

# Basic: [expression for item in iterable]
squares = [x**2 for x in range(10)]
print(f"Squares:             {squares}")

# With filter: [expression for item in iterable if condition]
even_squares = [x**2 for x in range(10) if x % 2 == 0]
print(f"Even squares:        {even_squares}")

# Transform strings
words = ["hello", "world", "python", "list", "comprehension"]
upper_long = [w.upper() for w in words if len(w) > 5]
print(f"Long words uppercased: {upper_long}")

# Remove None values
data = [1, None, 3, None, 5, None, 7]
clean = [x for x in data if x is not None]
print(f"Cleaned data:        {clean}")

# Flatten a matrix
matrix = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
flat = [num for row in matrix for num in row]
print(f"Flattened matrix:    {flat}")

# Create a multiplication table
times_table = [[i * j for j in range(1, 6)] for i in range(1, 6)]
print("\nMultiplication table (5x5):")
for row in times_table:
    print(f"  {row}")

# Comprehension with function call
def process(x):
    return x * x + 1

processed = [process(x) for x in range(5) if x % 2 == 0]
print(f"\nProcessed evens:     {processed}")

# Comprehension vs generator — memory difference
import sys
comp = [x**2 for x in range(10000)]     # list — stores all values
gen  = (x**2 for x in range(10000))     # generator — computes on demand
print(f"\nList comprehension size:      {sys.getsizeof(comp):,} bytes")
print(f"Generator expression size:    {sys.getsizeof(gen):,} bytes")

# =============================================================================
# SECTION 3: Sorting with key Functions
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 3: Sorting")
print("=" * 60)

# sort() vs sorted()
original = [3, 1, 4, 1, 5, 9, 2, 6, 5, 3]
sorted_copy = sorted(original)           # New list
print(f"Original:       {original}")
print(f"sorted():       {sorted_copy}")
print(f"Original after: {original}")     # Unchanged

original.sort()                          # In place
print(f"After .sort():  {original}")

# sort by length
words = ["banana", "apple", "kiwi", "cherry", "fig", "date"]
by_length = sorted(words, key=len)
print(f"\nBy length:        {by_length}")

# sort by last character
by_last = sorted(words, key=lambda w: w[-1])
print(f"By last char:     {by_last}")

# sort descending
by_length_desc = sorted(words, key=len, reverse=True)
print(f"By length (desc): {by_length_desc}")

# sort case-insensitively
mixed = ["Banana", "apple", "Cherry", "date"]
case_insensitive = sorted(mixed, key=str.lower)
print(f"Case-insensitive: {case_insensitive}")

# sort list of tuples
students = [
    ("Alice", 85, "Engineering"),
    ("Bob", 92, "Marketing"),
    ("Carol", 78, "Engineering"),
    ("Dave", 92, "HR"),
    ("Eve", 85, "Marketing"),
]

# By grade ascending
by_grade = sorted(students, key=lambda s: s[1])
print(f"\nBy grade (asc):   {[f'{s[0]}:{s[1]}' for s in by_grade]}")

# By grade descending, then name ascending (multi-key)
by_grade_name = sorted(students, key=lambda s: (-s[1], s[0]))
print(f"By grade desc, name asc: {[f'{s[0]}:{s[1]}' for s in by_grade_name]}")

# operator.itemgetter — faster than lambda for dict/tuple access
from operator import itemgetter, attrgetter

student_dicts = [
    {"name": "Alice", "grade": 85},
    {"name": "Bob", "grade": 92},
    {"name": "Carol", "grade": 78},
]
by_grade_dicts = sorted(student_dicts, key=itemgetter("grade"))
print(f"\nDicts by grade:   {[s['name'] for s in by_grade_dicts]}")

# Stable sort — Python's sort is stable (equal elements keep original order)
data = [("Alice", 2), ("Bob", 1), ("Carol", 2), ("Dave", 1)]
by_second = sorted(data, key=itemgetter(1))
print(f"Stable sort result: {by_second}")
# Alices and Carols maintain original relative order among ties

# =============================================================================
# SECTION 4: List as Stack
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 4: List as Stack (LIFO)")
print("=" * 60)

stack = []
print("Pushing: 'first', 'second', 'third'")
stack.append("first")
stack.append("second")
stack.append("third")
print(f"Stack state: {stack}")

print(f"Popped: {stack.pop()}")  # third
print(f"Popped: {stack.pop()}")  # second
print(f"Stack state: {stack}")

# Real example: undo history
class TextEditor:
    def __init__(self):
        self.text = ""
        self._history = []

    def type(self, chars):
        self._history.append(self.text)
        self.text += chars

    def undo(self):
        if self._history:
            self.text = self._history.pop()

editor = TextEditor()
editor.type("Hello")
editor.type(", World")
editor.type("!")
print(f"\nEditor text: '{editor.text}'")
editor.undo()
print(f"After undo:  '{editor.text}'")
editor.undo()
print(f"After undo:  '{editor.text}'")

# =============================================================================
# SECTION 5: deque for Queue (Why list.pop(0) is Bad)
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 5: deque vs list for Queue")
print("=" * 60)

import time
from collections import deque

# Demonstrate that list.pop(0) is slow
n = 100_000

# List queue — O(n) per dequeue
list_queue = list(range(n))
start = time.perf_counter()
while list_queue:
    list_queue.pop(0)
list_time = time.perf_counter() - start

# deque queue — O(1) per dequeue
deque_queue = deque(range(n))
start = time.perf_counter()
while deque_queue:
    deque_queue.popleft()
deque_time = time.perf_counter() - start

print(f"List pop(0) for {n:,} items:   {list_time:.4f}s")
print(f"Deque popleft for {n:,} items: {deque_time:.4f}s")
print(f"Deque is ~{list_time/deque_time:.0f}x faster")

# Practical queue example
task_queue = deque()
task_queue.append("send email")
task_queue.append("process payment")
task_queue.append("update database")

print(f"\nTask queue: {list(task_queue)}")
while task_queue:
    task = task_queue.popleft()
    print(f"Processing: {task}")

# =============================================================================
# SECTION 6: Useful list tricks
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 6: Useful List Tricks")
print("=" * 60)

# zip — iterate multiple lists in parallel
names = ["Alice", "Bob", "Carol"]
scores = [85, 92, 78]
for name, score in zip(names, scores):
    print(f"  {name}: {score}")

# enumerate — get index AND value
fruits = ["apple", "banana", "cherry"]
for i, fruit in enumerate(fruits):
    print(f"  {i}: {fruit}")

# enumerate with start
for i, fruit in enumerate(fruits, start=1):
    print(f"  {i}. {fruit}")

# zip_longest — fill missing values
from itertools import zip_longest
a = [1, 2, 3]
b = [10, 20]
for x, y in zip_longest(a, b, fillvalue=0):
    print(f"  {x} + {y} = {x + y}")

# Check if sorted
data = [1, 2, 3, 4, 5]
is_sorted = all(data[i] <= data[i+1] for i in range(len(data)-1))
print(f"\n[1,2,3,4,5] is sorted: {is_sorted}")

# Find index of max/min
data = [3, 1, 4, 1, 5, 9, 2, 6]
max_idx = data.index(max(data))
print(f"Max value at index {max_idx}: {data[max_idx]}")
