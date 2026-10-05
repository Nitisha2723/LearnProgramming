# Sets and Frozensets

Sets are one of Python's most underused data structures. When you need to check membership,
remove duplicates, or find common or unique items between collections, sets are the right tool.

---

## Creating Sets

```python
# Literal syntax
colors = {"red", "green", "blue"}

# From any iterable
from_list = set([1, 2, 3, 2, 1])    # {1, 2, 3} — duplicates removed
from_string = set("hello")           # {'h', 'e', 'l', 'o'} — unique chars

# Empty set — IMPORTANT: {} creates an empty dict, not a set!
empty_set = set()    # Correct
empty_dict = {}      # This is a dict

print(type({}))      # <class 'dict'>
print(type(set()))   # <class 'set'>
```

---

## Basic Operations

```python
fruits = {"apple", "banana", "cherry"}

# Add and remove
fruits.add("date")
fruits.discard("banana")     # No error if not present
fruits.remove("cherry")      # Raises KeyError if not present

# Membership test — O(1)
print("apple" in fruits)     # True
print("mango" in fruits)     # False

# Size
print(len(fruits))           # 2

# Iteration (order is not guaranteed)
for fruit in fruits:
    print(fruit)
```

---

## Set Operations

Python uses operators for set math — much more readable than calling methods.

```python
a = {1, 2, 3, 4, 5}
b = {3, 4, 5, 6, 7}

# Union: all elements from both sets
print(a | b)         # {1, 2, 3, 4, 5, 6, 7}
print(a.union(b))    # same

# Intersection: elements in BOTH sets
print(a & b)              # {3, 4, 5}
print(a.intersection(b))  # same

# Difference: elements in a but NOT in b
print(a - b)              # {1, 2}
print(a.difference(b))    # same
print(b - a)              # {6, 7}  — note: not symmetric

# Symmetric difference: elements in one but NOT both
print(a ^ b)                         # {1, 2, 6, 7}
print(a.symmetric_difference(b))     # same
```

### Real-World Examples

```python
# Find common friends
alice_friends = {"bob", "carol", "dave", "eve"}
bob_friends   = {"alice", "carol", "frank", "dave"}

common = alice_friends & bob_friends        # {"carol", "dave"}
all_friends = alice_friends | bob_friends   # Everyone
only_alice = alice_friends - bob_friends    # {"bob", "eve"}

# Find new customers (in this month but not last month)
last_month = {"alice", "bob", "carol"}
this_month = {"bob", "carol", "dave", "eve"}

new_customers = this_month - last_month     # {"dave", "eve"}
lost_customers = last_month - this_month    # {"alice"}
retained = last_month & this_month         # {"bob", "carol"}
```

---

## Set Comparisons

```python
a = {1, 2, 3}
b = {1, 2, 3, 4, 5}
c = {4, 5, 6}

# Subset: is every element of a in b?
print(a <= b)           # True  — a is a subset of b
print(a.issubset(b))    # True

# Proper subset: subset AND not equal
print(a < b)            # True
print(b < b)            # False — not a proper subset of itself

# Superset: does a contain all elements of b?
print(b >= a)           # True  — b is a superset of a
print(b.issuperset(a))  # True

# Disjoint: no elements in common
print(a.isdisjoint(c))  # True  — {1,2,3} and {4,5,6} share nothing
print(a.isdisjoint(b))  # False
```

---

## Set Comprehensions

```python
# Squares of even numbers
even_squares = {x**2 for x in range(10) if x % 2 == 0}
# {0, 4, 16, 36, 64}

# Unique first letters of words
words = ["apple", "avocado", "banana", "cherry", "apricot"]
first_letters = {word[0] for word in words}
# {"a", "b", "c"}

# All unique lengths
words = ["cat", "dog", "bird", "fish", "elephant"]
lengths = {len(w) for w in words}
# {3, 4, 8}
```

---

## Deduplication

The fastest and most Pythonic way to remove duplicates from a list:

```python
names = ["alice", "bob", "alice", "carol", "bob", "dave"]

# Deduplicate (order NOT preserved)
unique = list(set(names))

# Deduplicate while preserving order (Python 3.7+)
# Using dict.fromkeys — dicts maintain insertion order
unique_ordered = list(dict.fromkeys(names))
# ["alice", "bob", "carol", "dave"]
```

---

## Membership Testing: Set vs List

For large collections, `in` on a set is dramatically faster than on a list:

```python
import time

big_list = list(range(10_000_000))
big_set = set(range(10_000_000))

# List: O(n) — must scan every element until found
start = time.time()
9_999_999 in big_list
print(f"List: {time.time() - start:.4f}s")   # ~0.1s

# Set: O(1) — hash lookup
start = time.time()
9_999_999 in big_set
print(f"Set: {time.time() - start:.4f}s")    # ~0.000001s
```

**Use a set when you'll be doing many membership tests on a large collection.**

---

## frozenset — Immutable Set

`frozenset` is to `set` as `tuple` is to `list`. It's immutable and hashable.

```python
# Create
vowels = frozenset("aeiou")
frozen = frozenset([1, 2, 3, 4])

# Supports all read operations
print(2 in frozen)           # True
print(frozen & {2, 3, 5})   # frozenset({2, 3})

# But NOT modification
# frozen.add(5)    # AttributeError: 'frozenset' object has no attribute 'add'
```

### Why Use frozenset?

**As a dict key** (regular sets are unhashable):

```python
# Graph edges: store unordered pairs as frozenset keys
edge_weights = {
    frozenset({"A", "B"}): 5,
    frozenset({"B", "C"}): 3,
    frozenset({"A", "C"}): 8,
}

# Order doesn't matter for lookup
print(edge_weights[frozenset({"B", "A"})])  # 5  ← same as {"A", "B"}
```

**In a set of sets:**

```python
# Without frozenset, you can't do this
teams = {frozenset({"alice", "bob"}), frozenset({"carol", "dave"})}
```

**As a constant** — signals that this collection should not be modified:

```python
VALID_STATUSES = frozenset({"pending", "active", "closed", "cancelled"})

def update_status(order, new_status):
    if new_status not in VALID_STATUSES:
        raise ValueError(f"Invalid status: {new_status}")
    order.status = new_status
```

---

## When Sets Are the Right Tool

Use a set when:
- You need fast membership testing (`x in collection`)
- You want to deduplicate a collection
- You're doing set math (find common/unique/all items across groups)
- You're building an algorithm that needs to track "visited" items
- You want to prevent duplicates in the first place

Don't use a set when:
- Order matters (use a list)
- You need to count occurrences (use `Counter`)
- You need key-value pairs (use a dict)
- You need items that aren't hashable (lists can't go in a set)
