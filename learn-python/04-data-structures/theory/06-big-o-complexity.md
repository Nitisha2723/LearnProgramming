# Big-O Complexity for Python Data Structures

Understanding performance characteristics helps you make the right choices.
This isn't about premature optimization — it's about avoiding accidentally O(n²) code.

---

## What Big-O Notation Means

Big-O describes how an algorithm's running time grows as input size `n` grows.

| Notation   | Name         | Example                        | n=1000 ops (approx) |
|------------|--------------|--------------------------------|---------------------|
| O(1)       | Constant     | Dict lookup, list index        | 1                   |
| O(log n)   | Logarithmic  | Binary search                  | 10                  |
| O(n)       | Linear       | Linear search, list iteration  | 1,000               |
| O(n log n) | Linearithmic | Sort (Timsort)                 | 10,000              |
| O(n²)      | Quadratic    | Nested loops over the same list| 1,000,000           |

The difference between O(1) and O(n) at 10 million elements:
- O(1): ~1 operation
- O(n): ~10,000,000 operations

---

## `list` Performance

| Operation            | Complexity     | Notes                                      |
|----------------------|---------------|--------------------------------------------|
| `list[i]`            | **O(1)**       | Random access — lists are arrays           |
| `list[-1]`           | **O(1)**       | Same — negative index, still direct access |
| `list.append(x)`     | **O(1)** amort | Occasional resize, but amortized constant  |
| `list.pop()`         | **O(1)**       | Remove last element                        |
| `list.pop(0)`        | **O(n)**       | All elements shift left — use deque!       |
| `list.insert(i, x)`  | **O(n)**       | Elements after i shift right               |
| `x in list`          | **O(n)**       | Must scan every element                    |
| `list.index(x)`      | **O(n)**       | Same                                       |
| `list.remove(x)`     | **O(n)**       | Find + shift                               |
| `list.sort()`        | **O(n log n)** | Timsort (hybrid merge/insertion sort)      |
| `sorted(list)`       | **O(n log n)** | Same algorithm                             |
| `len(list)`          | **O(1)**       | Length stored as attribute                 |
| Slicing `list[a:b]`  | **O(k)**       | k = size of slice                          |
| `list.extend(other)` | **O(k)**       | k = len(other)                             |
| `list.copy()`        | **O(n)**       | Must copy all elements                     |

### Practical Consequence

```python
# BAD: O(n²) — inserting at front of list in a loop
data = []
for item in source:
    data.insert(0, item)   # Each insert is O(n)!

# GOOD: O(n) — use deque or reverse at end
from collections import deque
data = deque()
for item in source:
    data.appendleft(item)   # O(1)

# Or: build forward, reverse at end
data = []
for item in source:
    data.append(item)       # O(1) each
data.reverse()              # O(n) once
```

---

## `dict` Performance

| Operation          | Average Case   | Worst Case | Notes                               |
|--------------------|---------------|-----------|-------------------------------------|
| `d[key]`           | **O(1)**       | O(n)      | Worst case: many hash collisions    |
| `d[key] = val`     | **O(1)**       | O(n)      | Same                                |
| `del d[key]`       | **O(1)**       | O(n)      | Same                                |
| `key in d`         | **O(1)**       | O(n)      | Same                                |
| `len(d)`           | **O(1)**       | —         | Stored as attribute                 |
| Iteration          | **O(n)**       | —         | Visit all elements                  |
| Copy               | **O(n)**       | —         | Must copy all key-value pairs       |

Worst case (hash collisions) is extremely rare with Python's built-in types.
In practice, treat all dict operations as O(1).

### Why dict is O(1)

When you do `d["name"]`:
1. Python computes `hash("name")` — a fixed-size integer: O(1)
2. Uses that integer to find the right "bucket" in memory: O(1)
3. Returns the value: O(1)

No searching required — Python goes directly to the location.

---

## `set` Performance

Sets use the same hash table implementation as dicts.

| Operation         | Average Case   | Notes                        |
|-------------------|---------------|------------------------------|
| `x in s`          | **O(1)**       | Hash-based lookup            |
| `s.add(x)`        | **O(1)**       | Hash-based insert            |
| `s.remove(x)`     | **O(1)**       | Hash-based delete            |
| `s.discard(x)`    | **O(1)**       | Same, no error if missing    |
| `len(s)`          | **O(1)**       | Stored as attribute          |
| `s | t` (union)   | **O(n + m)**   | n=len(s), m=len(t)           |
| `s & t` (intersect)| **O(min(n,m))**| Iterate smaller, check larger|
| `s - t` (diff)    | **O(n)**       | Iterate s, check t           |

---

## `collections.deque` Performance

| Operation            | Complexity | Notes                              |
|----------------------|------------|-------------------------------------|
| `deque.append(x)`    | **O(1)**   | Add to right end                    |
| `deque.appendleft(x)`| **O(1)**   | Add to left end                     |
| `deque.pop()`        | **O(1)**   | Remove from right                   |
| `deque.popleft()`    | **O(1)**   | Remove from left                    |
| `deque[i]`           | **O(n)**   | Index access is O(n) — use list if you need random access |
| `x in deque`         | **O(n)**   | Must scan                           |
| `len(deque)`         | **O(1)**   |                                     |

> Note: deque excels at both-end operations but has slow middle access.
> If you need both fast front operations AND fast index access, there's no perfect
> solution — choose based on which operation is more frequent.

---

## `tuple` vs `list`

Tuples are slightly faster than lists for iteration and creation because
they're immutable (Python can optimize memory layout).

```python
import timeit

# Tuple access is slightly faster than list access
t = tuple(range(1000))
l = list(range(1000))

# For most code, the difference is negligible
# Use tuple for semantic reasons (immutability), not performance
```

---

## Real-World Performance Anti-Patterns

### Anti-Pattern 1: Using `in` on a list when you need many membership tests

```python
# BAD: O(n) per lookup — if valid_users has 100,000 entries, each check is slow
valid_users = ["alice", "bob", "carol", ...]   # 100,000 users

for request in incoming_requests:
    if request.user in valid_users:    # O(n) each time!
        handle(request)

# GOOD: Convert to set first — O(1) per lookup
valid_users_set = set(valid_users)    # One-time O(n) cost

for request in incoming_requests:
    if request.user in valid_users_set:   # O(1)
        handle(request)
```

### Anti-Pattern 2: Using `list.pop(0)` in a loop

```python
# BAD: O(n²) total — each pop(0) is O(n)
queue = list(tasks)   # 10,000 tasks
while queue:
    task = queue.pop(0)    # O(n)!
    process(task)

# GOOD: O(n) total
from collections import deque
queue = deque(tasks)
while queue:
    task = queue.popleft()   # O(1)
    process(task)
```

### Anti-Pattern 3: Counting with a loop when Counter works

```python
# BAD: O(n) with more overhead
count = {}
for item in data:
    if item in count:
        count[item] += 1
    else:
        count[item] = 1

# GOOD: Same O(n) but simpler
from collections import Counter
count = Counter(data)
```

---

## Summary Table

| Need                   | Best Structure | Lookup | Insert | Delete |
|------------------------|---------------|--------|--------|--------|
| Ordered sequence       | `list`        | O(1)   | O(n)   | O(n)   |
| Fast front+back ops    | `deque`       | O(n)   | O(1)   | O(1)   |
| Key-value lookup       | `dict`        | O(1)   | O(1)   | O(1)   |
| Membership testing     | `set`         | O(1)   | O(1)   | O(1)   |
| Counting occurrences   | `Counter`     | O(1)   | O(1)   | O(1)   |
| Grouping items         | `defaultdict` | O(1)   | O(1)   | O(1)   |
| Sorted sequence        | `list` (sort) | O(1)   | O(n)   | O(n)   |
