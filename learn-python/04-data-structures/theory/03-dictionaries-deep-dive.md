# Dictionaries: Deep Dive

Dictionaries are Python's most powerful built-in data structure. They're used everywhere —
from counting occurrences to representing objects to building lookup tables.

---

## Dict Comprehensions

Just like list comprehensions, but with `{key: value for ...}` syntax.

```python
# Square each number
squares = {x: x**2 for x in range(1, 6)}
# {1: 1, 2: 4, 3: 9, 4: 16, 5: 25}

# Invert a dict (swap keys and values)
original = {"a": 1, "b": 2, "c": 3}
inverted = {v: k for k, v in original.items()}
# {1: "a", 2: "b", 3: "c"}

# Filter: only keep items where grade >= 70
grades = {"Alice": 85, "Bob": 62, "Carol": 91, "Dave": 55}
passing = {name: grade for name, grade in grades.items() if grade >= 70}
# {"Alice": 85, "Carol": 91}

# From two lists
keys = ["a", "b", "c"]
values = [1, 2, 3]
combined = {k: v for k, v in zip(keys, values)}
# Same as: dict(zip(keys, values))
```

---

## Key Methods

### `.get(key, default)` — Safe Access

```python
student = {"name": "Alice", "grade": 85}

# Without get — raises KeyError if key missing
# age = student["age"]  # KeyError!

# With get — returns None or your default
age = student.get("age")           # None
age = student.get("age", 0)        # 0
grade = student.get("grade", 0)    # 85  — key exists, returns value
```

### `.setdefault(key, default)` — Get-or-Initialize

`setdefault` returns the value if the key exists, or sets it to the default and returns that.

```python
# Classic use: building a dict of lists
word_positions = {}
text = "the cat sat on the mat"

for i, word in enumerate(text.split()):
    # If key doesn't exist, create it with empty list
    word_positions.setdefault(word, []).append(i)

# {"the": [0, 4], "cat": [1], "sat": [2], "on": [3], "mat": [5]}
```

### `.update()` — Merge Dicts

```python
defaults = {"color": "blue", "size": "medium", "weight": 1.0}
user_prefs = {"color": "red", "size": "large"}

defaults.update(user_prefs)
# {"color": "red", "size": "large", "weight": 1.0}

# Python 3.9+ — merge with | operator
merged = defaults | user_prefs    # New dict
defaults |= user_prefs            # Update in place
```

### `.items()`, `.keys()`, `.values()`

```python
scores = {"Alice": 85, "Bob": 92, "Carol": 78}

# Iterate over key-value pairs
for name, score in scores.items():
    print(f"{name}: {score}")

# Just keys
for name in scores.keys():   # or just: for name in scores:
    print(name)

# Just values
for score in scores.values():
    print(score)

# Convert to lists
names = list(scores.keys())
all_scores = list(scores.values())
pairs = list(scores.items())   # [("Alice", 85), ...]
```

---

## `defaultdict` — Dict That Creates Missing Keys Automatically

The most common pattern in Python data manipulation: grouping items, building frequency maps,
accumulating values. `defaultdict` eliminates the "key exists?" check.

```python
from collections import defaultdict

# Without defaultdict — the "check and create" pattern
groups = {}
for word in ["apple", "avocado", "banana", "blueberry", "cherry"]:
    first = word[0]
    if first not in groups:
        groups[first] = []
    groups[first].append(word)

# With defaultdict — no check needed
groups = defaultdict(list)  # Pass the type to use as default factory
for word in ["apple", "avocado", "banana", "blueberry", "cherry"]:
    groups[word[0]].append(word)

# {"a": ["apple", "avocado"], "b": ["banana", "blueberry"], "c": ["cherry"]}

# defaultdict(int) for counting
word_count = defaultdict(int)
for word in "the quick brown fox jumps over the lazy dog".split():
    word_count[word] += 1   # No KeyError — missing keys default to 0
```

---

## `Counter` — Frequency Counting Made Easy

`Counter` is the most useful tool in the `collections` module. It counts hashable objects.

```python
from collections import Counter

# Count characters in a string
char_counts = Counter("mississippi")
# Counter({'s': 4, 'i': 4, 'p': 2, 'm': 1})

# Count words
words = "the quick brown fox jumps over the lazy dog".split()
word_counts = Counter(words)
# Counter({'the': 2, 'quick': 1, 'brown': 1, ...})

# most_common() — returns list of (element, count) sorted by count
top_3 = word_counts.most_common(3)
# [('the', 2), ('quick', 1), ('brown', 1)]

# Arithmetic operations on Counters
a = Counter("abba")            # {'a': 2, 'b': 2}
b = Counter("bccb")            # {'b': 2, 'c': 2}
print(a + b)                   # {'b': 4, 'a': 2, 'c': 2}
print(a - b)                   # {'a': 2}  — subtract, drop negatives
print(a & b)                   # {'b': 2}  — min of each count
print(a | b)                   # {'a': 2, 'b': 2, 'c': 2}  — max

# Update with more data
votes = Counter()
votes.update(["alice", "bob", "alice", "carol", "alice", "bob"])
print(votes)   # Counter({'alice': 3, 'bob': 2, 'carol': 1})
```

---

## How Dicts Work: Hash Tables

Understanding the internals helps you use dicts correctly.

A `dict` is a **hash table**. When you store `d["key"] = value`:
1. Python calls `hash("key")` to get an integer
2. That integer maps to a "bucket" in memory
3. The value is stored there

When you retrieve `d["key"]`:
1. Python calls `hash("key")` again — same hash
2. Goes directly to that bucket
3. Returns the value — O(1)!

### Rules for Dict Keys

Keys must be **hashable** — their hash must never change.

```python
# Valid keys (all immutable)
d = {
    42: "integer key",
    "name": "string key",
    (1, 2): "tuple key",
    3.14: "float key",
}

# INVALID keys (mutable, unhashable)
d[[1, 2]] = "list"    # TypeError: unhashable type: 'list'
d[{"a": 1}] = "dict"  # TypeError: unhashable type: 'dict'

# frozenset is hashable
d[frozenset({1, 2})] = "frozenset key"  # This works
```

---

## Dict Ordering

Since Python 3.7, dicts maintain **insertion order**. This is guaranteed behavior.

```python
d = {}
d["first"] = 1
d["second"] = 2
d["third"] = 3

for key in d:
    print(key)  # first, second, third — in insertion order
```

---

## Nested Dicts

Dicts can contain other dicts, enabling complex data modeling.

```python
school = {
    "students": {
        "alice": {"grade": 10, "gpa": 3.8, "clubs": ["debate", "chess"]},
        "bob":   {"grade": 11, "gpa": 3.2, "clubs": ["soccer"]},
    },
    "teachers": {
        "smith": {"subject": "math", "years": 15},
    }
}

# Access nested values
alice_gpa = school["students"]["alice"]["gpa"]  # 3.8

# Safe access through nested structure
alice_clubs = school.get("students", {}).get("alice", {}).get("clubs", [])
```

---

## JSON and Dicts

JSON maps almost perfectly to Python dicts.

```python
import json

# Dict to JSON string
data = {"name": "Alice", "scores": [85, 92, 78], "active": True}
json_str = json.dumps(data, indent=2)
print(json_str)
# {
#   "name": "Alice",
#   "scores": [85, 92, 78],
#   "active": true
# }

# JSON string to dict
parsed = json.loads(json_str)
print(parsed["name"])  # "Alice"

# Read/write JSON files
with open("data.json", "w") as f:
    json.dump(data, f, indent=2)

with open("data.json", "r") as f:
    loaded = json.load(f)
```

**JSON ↔ Python type mapping:**

| JSON         | Python  |
|--------------|---------|
| `object {}`  | `dict`  |
| `array []`   | `list`  |
| `string`     | `str`   |
| `number`     | `int` / `float` |
| `true`/`false` | `True`/`False` |
| `null`       | `None`  |

---

## Performance Characteristics

| Operation        | Average Case | Worst Case | Notes                        |
|------------------|-------------|-----------|------------------------------|
| `d[key]`         | O(1)        | O(n)      | Worst case: many hash collisions |
| `d[key] = val`   | O(1)        | O(n)      | Same                         |
| `key in d`       | O(1)        | O(n)      | Same                         |
| `del d[key]`     | O(1)        | O(n)      | Same                         |
| `len(d)`         | O(1)        | —         | Stored, not computed         |
| Iteration        | O(n)        | —         | Visit all elements           |

Worst case hash collisions are extremely rare in practice with Python's hash functions.
