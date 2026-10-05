# The `collections` Module

The `collections` module provides specialized container types that go beyond the built-ins.
Each one solves a specific, common problem better than a plain dict or list would.

```python
from collections import defaultdict, Counter, OrderedDict, deque, namedtuple, ChainMap
```

---

## `defaultdict` — Dict That Auto-Creates Missing Keys

A `defaultdict` works exactly like a regular dict, except accessing a missing key
automatically creates it with a default value instead of raising `KeyError`.

```python
from collections import defaultdict

# Regular dict — KeyError on missing key
d = {}
d["count"] += 1    # KeyError: 'count'

# defaultdict(int) — missing keys default to int() = 0
word_count = defaultdict(int)
word_count["hello"] += 1    # No error — starts at 0, becomes 1

# defaultdict(list) — missing keys default to list() = []
groups = defaultdict(list)
groups["fruits"].append("apple")    # No error
groups["fruits"].append("banana")
groups["vegs"].append("carrot")
# defaultdict(list, {"fruits": ["apple", "banana"], "vegs": ["carrot"]})
```

### Common Patterns with defaultdict

```python
from collections import defaultdict

# Pattern 1: Grouping items by a property
students = [("Alice", "Math"), ("Bob", "Science"), ("Carol", "Math"), ("Dave", "Science")]
by_subject = defaultdict(list)
for name, subject in students:
    by_subject[subject].append(name)
# {"Math": ["Alice", "Carol"], "Science": ["Bob", "Dave"]}

# Pattern 2: Counting (though Counter is better for this)
char_count = defaultdict(int)
for char in "mississippi":
    char_count[char] += 1

# Pattern 3: Building a graph as adjacency list
graph = defaultdict(set)
edges = [("A", "B"), ("A", "C"), ("B", "C"), ("C", "D")]
for src, dst in edges:
    graph[src].add(dst)
    graph[dst].add(src)
# {"A": {"B", "C"}, "B": {"A", "C"}, "C": {"A", "B", "D"}, "D": {"C"}}

# Pattern 4: Nested defaultdict
nested = defaultdict(lambda: defaultdict(int))
nested["alice"]["math"] += 90
nested["alice"]["science"] += 85
```

---

## `Counter` — Count Hashable Objects

`Counter` is a specialized dict subclass for counting. It's the right tool for
any frequency-counting problem.

```python
from collections import Counter

# Count elements
chars = Counter("abracadabra")
# Counter({'a': 5, 'b': 2, 'r': 2, 'c': 1, 'd': 1})

words = Counter(["cat", "dog", "cat", "bird", "dog", "cat"])
# Counter({'cat': 3, 'dog': 2, 'bird': 1})

# Access like a dict — missing keys return 0 (not KeyError)
print(chars["a"])     # 5
print(chars["z"])     # 0  ← not KeyError!
```

### `most_common()` — Top N Elements

```python
text = "to be or not to be that is the question"
word_counts = Counter(text.split())

# All elements sorted by count
print(word_counts.most_common())
# [('to', 2), ('be', 2), ('or', 1), ('not', 1), ...]

# Top 3
print(word_counts.most_common(3))
# [('to', 2), ('be', 2), ('or', 1)]

# Least common — use negative index or reverse
print(word_counts.most_common()[:-4:-1])
# Last 3 elements
```

### Counter Arithmetic

```python
a = Counter({"cat": 3, "dog": 2, "bird": 1})
b = Counter({"cat": 1, "dog": 4, "fish": 2})

print(a + b)   # Counter({'dog': 6, 'cat': 4, 'fish': 2, 'bird': 1})
print(a - b)   # Counter({'cat': 2, 'bird': 1})  — drop negatives
print(a & b)   # Counter({'cat': 1, 'dog': 2})   — min counts
print(a | b)   # Counter({'dog': 4, 'cat': 3, 'fish': 2, 'bird': 1})  — max counts

# Elements: iterate with repetition
c = Counter({'cat': 2, 'dog': 1})
list(c.elements())   # ['cat', 'cat', 'dog']

# Update (add more counts)
c.update(["cat", "fish"])
# Counter({'cat': 3, 'dog': 1, 'fish': 1})

# Subtract
c.subtract(["cat", "cat"])
# Counter({'dog': 1, 'fish': 1, 'cat': 1})
```

---

## `OrderedDict` — Dict With Explicit Ordering Control

In Python 3.7+, regular dicts maintain insertion order. `OrderedDict` is mostly
legacy now, but it has one unique feature: `move_to_end()`.

```python
from collections import OrderedDict

od = OrderedDict([("first", 1), ("second", 2), ("third", 3)])

od.move_to_end("first")         # Move to end
od.move_to_end("third", last=False)   # Move to front

# The main remaining use case: LRU (Least Recently Used) cache
class LRUCache:
    def __init__(self, capacity: int):
        self.capacity = capacity
        self.cache = OrderedDict()

    def get(self, key: int) -> int:
        if key not in self.cache:
            return -1
        self.cache.move_to_end(key)    # Mark as recently used
        return self.cache[key]

    def put(self, key: int, value: int) -> None:
        if key in self.cache:
            self.cache.move_to_end(key)
        self.cache[key] = value
        if len(self.cache) > self.capacity:
            self.cache.popitem(last=False)    # Remove oldest
```

---

## `deque` — Double-Ended Queue

`deque` (pronounced "deck") supports O(1) append and pop from **both ends**.
Use it instead of a list when you're adding/removing from the front.

```python
from collections import deque

d = deque([1, 2, 3])

# Both ends are O(1)
d.append(4)        # Add to right: [1, 2, 3, 4]
d.appendleft(0)    # Add to left:  [0, 1, 2, 3, 4]
d.pop()            # Remove from right: returns 4
d.popleft()        # Remove from left:  returns 0

# Rotate: move elements from one end to the other
d = deque([1, 2, 3, 4, 5])
d.rotate(2)    # [4, 5, 1, 2, 3]  — rotate right by 2
d.rotate(-1)   # [5, 1, 2, 3, 4]  — rotate left by 1
```

### `maxlen` — Sliding Window / Recent-Items Buffer

```python
# Keep only the last N items — perfect for recent activity, log buffering
recent = deque(maxlen=5)
for i in range(10):
    recent.append(i)
    print(list(recent))

# After 10 iterations: deque([5, 6, 7, 8, 9], maxlen=5)
# Old items are automatically discarded

# Browser history implementation
history = deque(maxlen=10)
history.append("google.com")
history.append("github.com")
history.append("stackoverflow.com")
```

### deque as Queue (FIFO)

```python
queue = deque()
queue.append("task1")    # enqueue
queue.append("task2")
queue.append("task3")
item = queue.popleft()   # dequeue — O(1)
```

---

## `namedtuple` — Lightweight Record Type

`namedtuple` creates a tuple subclass with named fields. It's lightweight (no per-instance
`__dict__`), immutable, and unpacks like a tuple.

```python
from collections import namedtuple

# Define: namedtuple(typename, field_names)
Point = namedtuple("Point", ["x", "y"])
Person = namedtuple("Person", "name age city")   # space-separated also works

# Create instances
p = Point(3, 7)
alice = Person("Alice", 30, "NYC")

# Access by name OR by index
print(p.x, p.y)          # 3, 7
print(alice.name)         # Alice
print(alice[1])           # 30  — still works as a tuple

# Unpack like a tuple
x, y = p
name, age, city = alice

# Convert to dict
print(alice._asdict())    # {"name": "Alice", "age": 30, "city": "NYC"}

# Create new instance with one field changed
older_alice = alice._replace(age=31)
```

### namedtuple vs dataclass

Use `namedtuple` for:
- Simple, immutable records (coordinates, RGB colors, database rows)
- When you want tuple behavior (indexing, unpacking, works in set/dict keys)

Use `@dataclass` (Python 3.7+) for:
- Mutable records
- Default values
- Methods you want to add
- More complex behavior

---

## `ChainMap` — View Multiple Dicts as One

`ChainMap` groups multiple dicts into a single view. Lookups search all the dicts
in order, but writes go to the first dict only.

```python
from collections import ChainMap

# Layer configuration: user overrides > project > defaults
defaults = {"color": "blue", "size": "medium", "debug": False}
project  = {"color": "green", "timeout": 30}
user     = {"debug": True}

config = ChainMap(user, project, defaults)
print(config["color"])    # "green"  — found in project
print(config["debug"])    # True     — found in user
print(config["size"])     # "medium" — found in defaults

# Writes go to the first (highest priority) dict
config["new_key"] = "value"
print(user)   # {"debug": True, "new_key": "value"}

# Create a child scope (common in interpreter/scope implementations)
child = config.new_child({"size": "large"})
print(child["size"])    # "large"  — overrides parent
print(config["size"])   # "medium" — parent unchanged
```

---

## Quick Reference

| Type          | Use When                                              |
|---------------|-------------------------------------------------------|
| `defaultdict` | You're building dicts of lists, sets, or counts       |
| `Counter`     | You need to count occurrences of anything             |
| `OrderedDict` | You need `move_to_end()` (LRU cache, etc.)            |
| `deque`       | Queue, sliding window, or frequent front operations   |
| `namedtuple`  | Lightweight immutable records with named fields       |
| `ChainMap`    | Layered config, scoped namespaces                     |
