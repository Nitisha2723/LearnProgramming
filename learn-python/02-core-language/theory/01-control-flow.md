# Theory 01: Control Flow

Control flow is how you tell a program *which* code to execute and *how many times*.
Without it, every program would just run every line from top to bottom, once — not very useful.

---

## Table of Contents

1. [if / elif / else](#1-if--elif--else)
2. [Truthy and Falsy Values](#2-truthy-and-falsy-values)
3. [Ternary Expression](#3-ternary-expression)
4. [match Statement (Python 3.10+)](#4-match-statement-python-310)
5. [while Loop](#5-while-loop)
6. [for Loop](#6-for-loop)
7. [range()](#7-range)
8. [List Comprehensions Preview](#8-list-comprehensions-preview)
9. [break, continue, pass](#9-break-continue-pass)

---

## 1. `if` / `elif` / `else`

### The Big Idea

Python uses **indentation** (4 spaces by convention) instead of curly braces `{}` to define blocks.
This is not optional — wrong indentation is a syntax error. Once you accept this, you will find
the code remarkably readable.

```python
temperature = 22

if temperature > 30:
    print("It's hot — drink water")
elif temperature > 20:
    print("Pleasant weather")
elif temperature > 10:
    print("A bit cool — bring a jacket")
else:
    print("Cold — bundle up")
```

**Real-world analogy:** A thermostat. It checks conditions in order and acts on the first one that is true.

### Rules

- Every `if` can have zero or more `elif` clauses and zero or one `else`.
- Conditions are checked **top to bottom**; Python stops at the first `True`.
- The `else` is the "none of the above" fallback — it never has a condition.
- Conditions can be any expression — Python evaluates it as `True` or `False`.

### Nested if

```python
age = 25
has_ticket = True

if age >= 18:
    if has_ticket:
        print("Welcome!")
    else:
        print("You need a ticket")
else:
    print("Must be 18 or older")
```

Nesting works, but more than two levels deep is a code-smell. Prefer flatter logic when possible.

### Comparison and Logical Operators

```python
x = 5

# Comparison operators
x == 5    # equal
x != 3    # not equal
x > 3     # greater
x < 10    # less
x >= 5    # greater or equal
x <= 5    # less or equal

# Logical operators
x > 3 and x < 10   # both must be true
x < 3 or x > 4     # at least one must be true
not (x == 5)        # inverts the boolean

# Python allows chained comparisons — very readable
3 < x < 10          # equivalent to x > 3 and x < 10
```

### `in` and `not in`

```python
fruits = ["apple", "banana", "cherry"]

if "banana" in fruits:
    print("Found it!")

if "mango" not in fruits:
    print("No mango")

# Works on strings too
if "py" in "python":
    print("python contains py")
```

---

## 2. Truthy and Falsy Values

### The Big Idea

Python's `if` does not require a strict `True` or `False`. Any value can be used as a condition.
Values that behave like `False` are called **falsy**; everything else is **truthy**.

**Falsy values** (the complete list):

| Value | Type |
|-------|------|
| `False` | bool |
| `0` | int |
| `0.0` | float |
| `""` | str (empty) |
| `[]` | list (empty) |
| `()` | tuple (empty) |
| `{}` | dict (empty) |
| `set()` | set (empty) |
| `None` | NoneType |

Everything else is truthy: `1`, `-1`, `"hello"`, `[0]`, `{"a": 1}`, any object, etc.

### Why This Matters

```python
name = input("Enter your name: ")

# Pythonic way — checks if name is non-empty
if name:
    print(f"Hello, {name}!")
else:
    print("You didn't enter a name")

# Less Pythonic (redundant)
if name != "":
    ...
```

```python
items = []

if items:
    print(f"Processing {len(items)} items")
else:
    print("Nothing to process")
```

**Real-world analogy:** A vending machine button. Pressing it works when there's something in stock (truthy). An empty slot does nothing (falsy).

### `None` is Special

`None` represents "no value" — like `null` in Java. It is falsy.

```python
result = None   # function hasn't produced a result yet

if result is None:      # use 'is' to check for None, not ==
    print("No result yet")
```

---

## 3. Ternary Expression

A one-line `if/else` for assigning values conditionally.

### Syntax

```python
value = value_if_true if condition else value_if_false
```

### Examples

```python
age = 20
status = "adult" if age >= 18 else "minor"
print(status)   # adult

# Equivalent long form:
if age >= 18:
    status = "adult"
else:
    status = "minor"
```

```python
# In function calls
temperature = 35
advice = "Stay inside" if temperature > 30 else "Go for a walk"

# In list comprehensions (preview — covered in section 8)
numbers = [1, -2, 3, -4, 5]
absolutes = [n if n >= 0 else -n for n in numbers]
```

**When to use:** Single-line value selection. Avoid nesting ternaries — it kills readability.

---

## 4. `match` Statement (Python 3.10+)

Python's `match` is more than a "switch statement." It performs **structural pattern matching** —
it can match shapes, types, and values simultaneously.

### Basic Value Matching

```python
day_number = 3

match day_number:
    case 1:
        print("Monday")
    case 2:
        print("Tuesday")
    case 3:
        print("Wednesday")
    case 4 | 5:          # OR pattern
        print("Thursday or Friday")
    case 6 | 7:
        print("Weekend")
    case _:              # default (like else)
        print("Invalid day")
```

### Matching with Guards (conditions)

```python
score = 85

match score:
    case s if s >= 90:
        grade = "A"
    case s if s >= 80:
        grade = "B"
    case s if s >= 70:
        grade = "C"
    case _:
        grade = "F"
```

### Structural Pattern Matching

This is where `match` shines — matching the *shape* of data:

```python
point = (3, 0)

match point:
    case (0, 0):
        print("Origin")
    case (x, 0):
        print(f"On x-axis at {x}")   # x is captured from the tuple
    case (0, y):
        print(f"On y-axis at {y}")
    case (x, y):
        print(f"Point at ({x}, {y})")
```

```python
command = {"action": "move", "direction": "north", "steps": 3}

match command:
    case {"action": "move", "direction": dir, "steps": n}:
        print(f"Moving {dir} by {n} steps")
    case {"action": "stop"}:
        print("Stopping")
    case _:
        print("Unknown command")
```

**Real-world analogy:** A customs officer at an airport. They check the shape and contents of your documents, not just a single value.

---

## 5. `while` Loop

Repeats a block **while** a condition is true.

### Basic while

```python
count = 0
while count < 5:
    print(f"Count: {count}")
    count += 1
# Prints 0, 1, 2, 3, 4
```

**Real-world analogy:** A car engine running *while* there's fuel. When fuel runs out, the loop ends.

### The `while ... else` Clause (Python-Unique!)

Python's `while` can have an `else` that runs when the condition becomes `False` — but NOT if the loop was broken with `break`.

```python
# Searching for a number
target = 7
current = 1

while current <= 10:
    if current == target:
        print(f"Found {target}!")
        break
    current += 1
else:
    # Only runs if we didn't break
    print(f"{target} not found in range 1-10")
```

This pattern is useful for search loops — the `else` confirms "not found."

### Infinite Loop with `break`

```python
while True:
    user_input = input("Enter 'quit' to exit: ")
    if user_input == "quit":
        break
    print(f"You entered: {user_input}")

print("Goodbye!")
```

### Common Pattern: Input Validation

```python
while True:
    age_str = input("Enter your age: ")
    if age_str.isdigit():
        age = int(age_str)
        if 0 <= age <= 120:
            break
        print("Age must be between 0 and 120")
    else:
        print("Please enter a number")

print(f"Your age is {age}")
```

---

## 6. `for` Loop

Iterates over **any iterable** — a sequence of values.
Unlike Java's for loop, Python's `for` is always a "for-each" style loop.

### Basic for

```python
fruits = ["apple", "banana", "cherry"]
for fruit in fruits:
    print(fruit)
```

### Iterating Over Strings

```python
word = "Python"
for char in word:
    print(char)     # P, y, t, h, o, n
```

### Iterating with Index: `enumerate()`

```python
fruits = ["apple", "banana", "cherry"]
for index, fruit in enumerate(fruits):
    print(f"{index}: {fruit}")
# 0: apple
# 1: banana
# 2: cherry

# Start index from 1
for index, fruit in enumerate(fruits, start=1):
    print(f"{index}. {fruit}")
```

### Iterating Over Two Lists: `zip()`

```python
names = ["Alice", "Bob", "Carol"]
scores = [92, 87, 95]

for name, score in zip(names, scores):
    print(f"{name}: {score}")
```

### `for ... else` Clause

Like `while ... else`, the `else` runs only if no `break` occurred:

```python
def find_prime_factor(n):
    for i in range(2, n):
        if n % i == 0:
            print(f"{n} is divisible by {i}")
            break
    else:
        print(f"{n} is prime")

find_prime_factor(7)    # 7 is prime
find_prime_factor(12)   # 12 is divisible by 2
```

**Real-world analogy:** Checking a list of keys to open a lock. If you try every key and none works (no `break`), the `else` tells you "no key fits."

---

## 7. `range()`

`range()` generates a sequence of integers without storing them all in memory.

### Forms

```python
range(stop)              # 0, 1, 2, ..., stop-1
range(start, stop)       # start, start+1, ..., stop-1
range(start, stop, step) # start, start+step, ..., < stop
```

### Examples

```python
# Count to 4
for i in range(5):
    print(i)    # 0 1 2 3 4

# Count from 1 to 10
for i in range(1, 11):
    print(i)    # 1 2 3 4 5 6 7 8 9 10

# Even numbers
for i in range(0, 20, 2):
    print(i)    # 0 2 4 6 8 10 12 14 16 18

# Countdown
for i in range(10, 0, -1):
    print(i)    # 10 9 8 7 6 5 4 3 2 1

# Convert to list when you need the values
squares = list(range(1, 6))    # [1, 2, 3, 4, 5]
```

### `range` is Lazy

`range(1_000_000)` does not store a million integers in memory.
It generates each value only when requested. This is called a **lazy sequence** or **iterator**.

---

## 8. List Comprehensions Preview

List comprehensions are the *Pythonic* way to create lists. They replace many `for` + `append` patterns.

```python
# Old way
squares = []
for x in range(1, 6):
    squares.append(x ** 2)

# Pythonic way — list comprehension
squares = [x ** 2 for x in range(1, 6)]
# [1, 4, 9, 16, 25]
```

### With a Condition (Filter)

```python
# Only even squares
even_squares = [x ** 2 for x in range(1, 11) if x % 2 == 0]
# [4, 16, 36, 64, 100]
```

### Comprehension Syntax

```python
[expression for item in iterable if condition]
#  ^what      ^variable  ^source    ^filter (optional)
```

Full coverage is in `theory/03-lists-and-tuples.md`.

---

## 9. `break`, `continue`, `pass`

### `break` — Exit the Loop

```python
for number in range(100):
    if number == 7:
        print(f"Found 7 at index {number}")
        break   # jump out of the loop entirely
```

### `continue` — Skip This Iteration

```python
for number in range(10):
    if number % 2 == 0:
        continue    # skip even numbers
    print(number)   # prints 1, 3, 5, 7, 9
```

### `pass` — Do Nothing (Placeholder)

```python
# When Python requires a statement but you have nothing to write yet
for item in range(5):
    pass    # TODO: implement later

if condition:
    pass    # handle this case later

def empty_function():
    pass    # function body required, but nothing to write yet
```

`pass` is useful when writing code skeletons — it lets you define the structure without
implementing every detail immediately.

---

## Quick Reference

| Construct | Unique Python Feature |
|-----------|----------------------|
| `if/elif/else` | Indentation defines blocks (no `{}`) |
| `while ... else` | `else` runs when loop finishes normally |
| `for ... else` | `else` runs when loop finishes without `break` |
| Truthy/Falsy | Any value can be used as a condition |
| `match` | Structural pattern matching (3.10+) |
| Ternary | `x if cond else y` (not `cond ? x : y`) |
| List comprehension | `[expr for x in iterable if cond]` |

---

## Key Takeaways

1. Python uses **indentation**, not braces. Four spaces is the standard.
2. Python's truthy/falsy system makes many checks natural: `if items:` instead of `if len(items) > 0:`.
3. Both `while` and `for` loops have an `else` clause — Python-unique and often overlooked.
4. `range()` is lazy — prefer it over `range(len(list))` when you don't need the index.
5. List comprehensions are idiomatic Python — learn to read and write them naturally.

---

*Next: `theory/02-functions.md` — how to package logic into reusable, testable units.*
