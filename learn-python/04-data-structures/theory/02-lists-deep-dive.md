# Lists: Deep Dive

Lists are Python's most-used data structure. This file goes beyond the basics to cover
the powerful features that make Python list manipulation expressive and efficient.

---

## Advanced Slicing: `[start:stop:step]`

The full slice syntax is `list[start:stop:step]`. All three are optional.

```python
nums = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

# Basic slicing (you know this)
print(nums[2:5])     # [2, 3, 4]   — index 2 up to (not including) 5
print(nums[:4])      # [0, 1, 2, 3] — from start to index 4
print(nums[6:])      # [6, 7, 8, 9] — from index 6 to end

# Step parameter
print(nums[::2])     # [0, 2, 4, 6, 8]   — every other element
print(nums[1::2])    # [1, 3, 5, 7, 9]   — every other, starting at 1
print(nums[::3])     # [0, 3, 6, 9]      — every third

# Negative indices
print(nums[-3:])     # [7, 8, 9]  — last 3 elements
print(nums[:-2])     # [0, 1, 2, 3, 4, 5, 6, 7]  — all but last 2

# Reverse a list — THE most common use of step=-1
print(nums[::-1])    # [9, 8, 7, 6, 5, 4, 3, 2, 1, 0]
print(nums[7:2:-1])  # [7, 6, 5, 4, 3]  — backwards from index 7 to 3
```

### Slicing Strings (same rules!)

```python
s = "Hello, World!"
print(s[:5])        # "Hello"
print(s[-6:])       # "orld!"
print(s[::-1])      # "!dlroW ,olleH"
print(s[7:12])      # "World"
```

---

## List Comprehensions

List comprehensions are one of Python's most distinctive features — they replace
`for` loops that build lists with a single, readable expression.

### Basic Pattern: `[expression for item in iterable]`

```python
# Without comprehension
squares = []
for x in range(10):
    squares.append(x ** 2)

# With comprehension — one line, more readable
squares = [x ** 2 for x in range(10)]
# [0, 1, 4, 9, 16, 25, 36, 49, 64, 81]
```

### With Filtering: `[expression for item in iterable if condition]`

```python
# Even squares only
even_squares = [x ** 2 for x in range(10) if x % 2 == 0]
# [0, 4, 16, 36, 64]

# Only words longer than 4 chars, uppercased
words = ["apple", "kiwi", "banana", "fig", "cherry"]
long_words = [w.upper() for w in words if len(w) > 4]
# ["APPLE", "BANANA", "CHERRY"]

# Filter None values from a list
data = [1, None, 3, None, 5]
clean = [x for x in data if x is not None]
# [1, 3, 5]
```

### Nested Comprehensions

```python
# Flatten a matrix (list of lists)
matrix = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
flat = [num for row in matrix for num in row]
# [1, 2, 3, 4, 5, 6, 7, 8, 9]

# Create a matrix
grid = [[row * col for col in range(1, 4)] for row in range(1, 4)]
# [[1, 2, 3], [2, 4, 6], [3, 6, 9]]

# All pairs (x, y) where x != y
pairs = [(x, y) for x in range(3) for y in range(3) if x != y]
# [(0, 1), (0, 2), (1, 0), (1, 2), (2, 0), (2, 1)]
```

### When NOT to Use Comprehensions

Comprehensions become hard to read when nested more than 2 levels deep,
or when the expression is complex. Use a regular loop in those cases.

```python
# This is too complex — use a loop
result = [process(x) for x in data if predicate(x) and another_check(x)]

# Better
result = []
for x in data:
    if predicate(x) and another_check(x):
        result.append(process(x))
```

---

## List as Stack and Queue

### List as Stack (LIFO — Last In, First Out)

```python
stack = []
stack.append("first")   # push
stack.append("second")  # push
stack.append("third")   # push

top = stack.pop()        # pop → "third"
top = stack.pop()        # pop → "second"
```

`append()` and `pop()` on a list are O(1). Lists work fine as stacks.

### List as Queue (FIFO — First In, First Out) — DON'T DO THIS

```python
# This works but is SLOW
queue = []
queue.append("first")    # enqueue — O(1)
queue.append("second")
queue.append("third")

front = queue.pop(0)     # dequeue — O(n) ← PROBLEM
```

`pop(0)` is O(n) because all remaining elements must shift left.

### Use `collections.deque` for Queues

```python
from collections import deque

queue = deque()
queue.append("first")     # enqueue right side — O(1)
queue.append("second")
queue.append("third")

front = queue.popleft()   # dequeue left side — O(1)
```

`deque` is O(1) for both append and popleft. Always use it for queues.

---

## Sorting

### `list.sort()` vs `sorted()`

```python
numbers = [3, 1, 4, 1, 5, 9, 2, 6]

# sort() modifies in place, returns None
numbers.sort()
print(numbers)  # [1, 1, 2, 3, 4, 5, 6, 9]

# sorted() returns a new list, original unchanged
original = [3, 1, 4, 1, 5, 9, 2, 6]
sorted_copy = sorted(original)
print(original)      # [3, 1, 4, 1, 5, 9, 2, 6]  — unchanged
print(sorted_copy)   # [1, 1, 2, 3, 4, 5, 6, 9]
```

**Rule of thumb:** Use `sort()` when you want to modify the list. Use `sorted()` 
when you need a sorted copy or are sorting a non-list iterable.

### `reverse=True`

```python
numbers.sort(reverse=True)
print(numbers)  # [9, 6, 5, 4, 3, 2, 1, 1]
```

### The `key` Parameter — Sorting by a Custom Criterion

The `key` parameter accepts a function. Each element is passed through that function,
and the result is used for comparison. The original elements are returned, not the keys.

```python
words = ["banana", "apple", "cherry", "date", "kiwi"]

# Sort by length
words.sort(key=len)
print(words)  # ["date", "kiwi", "apple", "banana", "cherry"]

# Sort by last character
words.sort(key=lambda w: w[-1])

# Sort case-insensitively
words.sort(key=str.lower)

# Sort a list of tuples by second element
data = [("Alice", 85), ("Bob", 92), ("Carol", 78)]
data.sort(key=lambda t: t[1])
# [("Carol", 78), ("Alice", 85), ("Bob", 92)]
```

### Sorting with `operator.attrgetter` and `operator.itemgetter`

For performance-sensitive code, the `operator` module provides faster alternatives to lambdas:

```python
from operator import itemgetter, attrgetter

# Sort list of dicts by a key
students = [
    {"name": "Alice", "grade": 85},
    {"name": "Bob", "grade": 92},
    {"name": "Carol", "grade": 78},
]
students.sort(key=itemgetter("grade"))

# Sort objects by attribute
class Student:
    def __init__(self, name, grade):
        self.name = name
        self.grade = grade

student_objects = [Student("Alice", 85), Student("Bob", 92), Student("Carol", 78)]
student_objects.sort(key=attrgetter("grade"))
```

### Stable Sorting and Multi-Key Sorting

Python's sort is **stable** — equal elements maintain their original order.
This enables multi-key sorting via successive sorts:

```python
# Sort by grade descending, then by name ascending for ties
students = [
    {"name": "Bob", "grade": 85},
    {"name": "Alice", "grade": 85},
    {"name": "Carol", "grade": 92},
]

# First sort by name (less important key)
students.sort(key=itemgetter("name"))
# Then sort by grade (more important key)
students.sort(key=itemgetter("grade"), reverse=True)
# [Carol:92, Alice:85, Bob:85]  — Alice before Bob because of stable sort

# Alternatively, use a tuple key (sorts by first, then second)
students.sort(key=lambda s: (-s["grade"], s["name"]))
```

---

## List Performance Characteristics

| Operation          | Time Complexity | Notes                              |
|--------------------|----------------|------------------------------------|
| `list[i]`          | O(1)           | Index access is constant time      |
| `list.append(x)`   | O(1) amortized | Occasional resize, but rare        |
| `list.pop()`       | O(1)           | Remove from end                    |
| `list.pop(0)`      | O(n)           | All elements shift — use deque!    |
| `list.insert(i,x)` | O(n)           | Elements after i must shift        |
| `x in list`        | O(n)           | Must scan every element            |
| `list.sort()`      | O(n log n)     | Timsort algorithm                  |
| `len(list)`        | O(1)           | Length is stored, not counted      |
| `list[a:b]`        | O(k)           | k = size of slice                  |

---

## Useful List Methods

```python
nums = [3, 1, 4, 1, 5, 9, 2, 6, 5]

nums.count(1)          # 2  — how many times 1 appears
nums.index(4)          # 2  — index of first occurrence of 4
nums.index(1, 2)       # 3  — search starting from index 2

nums.extend([7, 8])    # Add multiple items (modifies in place)
nums += [7, 8]         # Same effect

# Copy
shallow_copy = nums.copy()    # or nums[:]
import copy
deep_copy = copy.deepcopy(nums)  # For nested structures

nums.reverse()         # Reverse in place
nums.clear()           # Remove all elements
```
