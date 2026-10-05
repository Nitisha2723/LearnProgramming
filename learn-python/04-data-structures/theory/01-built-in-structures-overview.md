# Python's Built-in Data Structures: Overview

Python has four core built-in data structures: `list`, `tuple`, `dict`, and `set`.
Understanding when to use each one is one of the most important skills in Python.

---

## The Four Built-in Structures at a Glance

| Structure | Ordered | Mutable | Duplicates | Syntax        | Primary Use                        |
|-----------|---------|---------|------------|---------------|------------------------------------|
| `list`    | Yes     | Yes     | Yes        | `[1, 2, 3]`  | Sequences, ordered collections     |
| `tuple`   | Yes     | No      | Yes        | `(1, 2, 3)`  | Fixed records, function returns    |
| `dict`    | Yes*    | Yes     | Keys: No   | `{"a": 1}`   | Key-value mapping, lookup tables   |
| `set`     | No      | Yes     | No         | `{1, 2, 3}`  | Membership testing, deduplication  |

*Dicts maintain insertion order in Python 3.7+

---

## list — Ordered, Mutable Sequence

A `list` is the go-to structure for any ordered collection of items.

```python
fruits = ["apple", "banana", "cherry"]
fruits.append("date")          # Add to end
fruits.insert(1, "avocado")    # Insert at index
fruits.remove("banana")        # Remove by value
popped = fruits.pop()          # Remove and return last item
popped_at = fruits.pop(0)      # Remove and return item at index 0

# Index access
first = fruits[0]
last = fruits[-1]
```

**When to use `list`:**
- You need an ordered collection
- Items will change (add, remove, update)
- You need to access items by position
- You may have duplicate values

---

## tuple — Ordered, Immutable Sequence

A `tuple` is like a `list` but cannot be changed after creation.

```python
point = (3, 7)               # 2D coordinate
rgb = (255, 128, 0)          # Color value
person = ("Alice", 30, "NYC") # Record

# Unpacking
x, y = point
name, age, city = person

# Single-element tuple (note the trailing comma)
single = (42,)    # NOT the same as (42)
```

**When to use `tuple`:**
- The data is fixed and shouldn't change (coordinates, RGB values)
- You want to signal "this is a record, not a list"
- You need a hashable sequence (to use as a dict key or in a set)
- Function returning multiple values (Python automatically packs them)

```python
def get_min_max(numbers):
    return min(numbers), max(numbers)   # Returns a tuple

lo, hi = get_min_max([3, 1, 4, 1, 5])  # Unpacks the tuple
```

---

## dict — Key-Value Mapping

A `dict` maps keys to values. Keys must be hashable (strings, numbers, tuples).

```python
student = {
    "name": "Alice",
    "age": 20,
    "grades": [85, 92, 78]
}

# Access
print(student["name"])              # "Alice"
print(student.get("gpa", 0.0))     # 0.0 (default if key missing)

# Modify
student["age"] = 21
student["major"] = "Computer Science"

# Iterate
for key, value in student.items():
    print(f"{key}: {value}")
```

**When to use `dict`:**
- You need to look something up by name or ID
- You're counting occurrences (frequency maps)
- You're grouping items by category
- You're modeling a real-world object with named properties

---

## set — Unordered Collection of Unique Items

A `set` stores unique items with no guaranteed order.

```python
unique_visitors = {"alice", "bob", "carol"}
unique_visitors.add("dave")
unique_visitors.discard("bob")    # Remove without error if missing

# Membership test — O(1), much faster than list
if "alice" in unique_visitors:
    print("Alice has visited")

# Set operations
a = {1, 2, 3, 4}
b = {3, 4, 5, 6}
print(a | b)    # Union: {1, 2, 3, 4, 5, 6}
print(a & b)    # Intersection: {3, 4}
print(a - b)    # Difference: {1, 2}
```

**When to use `set`:**
- You only care whether something is present or absent
- You want to deduplicate a list: `list(set(my_list))`
- You need to find common or unique elements between collections
- Membership testing speed matters (O(1) vs O(n) for list)

---

## frozenset — Immutable Set

`frozenset` is to `set` as `tuple` is to `list` — immutable and hashable.

```python
valid_colors = frozenset({"red", "green", "blue"})

# Can be used as a dict key or in a set
color_groups = {
    frozenset({"red", "green"}): "warm-ish",
    frozenset({"blue", "purple"}): "cool",
}
```

---

## Quick Decision Guide

```
Need key-value mapping?
  → dict

Need ordered sequence?
  → list (if it will change)
  → tuple (if it's fixed)

Need unique items or fast membership testing?
  → set

Need immutable set as a dict key?
  → frozenset
```

---

## Python vs Java Quick Comparison

| Python               | Java Equivalent              | Notes                              |
|----------------------|------------------------------|------------------------------------|
| `list`               | `ArrayList<T>`               | Python list is more flexible       |
| `tuple`              | No direct equivalent         | Closest: record class or array     |
| `dict`               | `HashMap<K, V>`              | Python syntax is much cleaner      |
| `set`                | `HashSet<T>`                 | Python has operator syntax         |
| `collections.deque`  | `ArrayDeque<T>`              | O(1) both ends                     |
| `collections.Counter`| No equivalent (write it)     | Built-in frequency counting        |
