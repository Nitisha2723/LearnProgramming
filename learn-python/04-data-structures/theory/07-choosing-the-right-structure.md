# Choosing the Right Data Structure

The most important skill with data structures isn't knowing how they work —
it's knowing which one to reach for in each situation.

---

## Decision Flowchart

```
Do you need to store multiple items?
│
├─ Yes → Do you need to look things up by key?
│         │
│         ├─ Yes → Use DICT (or Counter/defaultdict from collections)
│         │         - "Find the student named Alice" → dict
│         │         - "How many times does each word appear?" → Counter
│         │         - "Group students by subject" → defaultdict(list)
│         │
│         └─ No → Do you care about order?
│                   │
│                   ├─ Yes → Can the data change?
│                   │         │
│                   │         ├─ Yes → Use LIST
│                   │         │         - "Add items, remove items, iterate"
│                   │         │         - "Access by index"
│                   │         │
│                   │         └─ No → Use TUPLE
│                   │                  - "Coordinates, RGB, fixed records"
│                   │                  - "Function returning multiple values"
│                   │
│                   └─ No → Do you need unique items only?
│                             │
│                             └─ Yes → Use SET
│                                       - "Is X in the valid values?"
│                                       - "Remove duplicates"
│                                       - "Find common/unique elements"
│
└─ No → None, True/False, 0, or a single value
```

---

## Concrete Scenarios

### Scenario 1: Counting Things

```python
# Count word frequencies in a document
from collections import Counter

text = open("document.txt").read().split()
word_freq = Counter(text)
print(word_freq.most_common(10))  # Top 10 words
```

Use `Counter` — it's literally built for this.

---

### Scenario 2: Grouping Items

```python
# Group employees by department
from collections import defaultdict

employees = [
    ("Alice", "Engineering"),
    ("Bob", "Marketing"),
    ("Carol", "Engineering"),
    ("Dave", "HR"),
]

by_dept = defaultdict(list)
for name, dept in employees:
    by_dept[dept].append(name)
```

Use `defaultdict(list)` — eliminates the check-then-append pattern.

---

### Scenario 3: Checking Membership

```python
# Check if a user is banned — checked millions of times
banned_users = {"spammer123", "troll456", "bot789"}  # SET

if username in banned_users:    # O(1)
    block_request()
```

Use a `set` — O(1) membership testing vs O(n) for a list.

---

### Scenario 4: Ordered Operations at Both Ends

```python
# Process print queue: new jobs added to back, oldest job processed first
from collections import deque

print_queue = deque()
print_queue.append("doc1.pdf")      # enqueue
print_queue.append("report.pdf")    # enqueue
job = print_queue.popleft()         # dequeue — process oldest first
```

Use `deque` — O(1) both ends. A list would be O(n) for `pop(0)`.

---

### Scenario 5: Recent/Sliding Window

```python
# Keep track of last 100 page views
from collections import deque

recent_pages = deque(maxlen=100)
recent_pages.append(current_page)   # Automatically discards oldest when full
```

Use `deque(maxlen=N)` — automatic oldest-item eviction.

---

### Scenario 6: Lightweight Records

```python
# GPS coordinates, database rows, function return values
from collections import namedtuple

# Without namedtuple — confusing tuples
def get_location():
    return (40.7128, -74.0060)   # What's which?

lat, lon = get_location()    # Easy to swap accidentally

# With namedtuple — self-documenting
Location = namedtuple("Location", ["latitude", "longitude"])

def get_location():
    return Location(40.7128, -74.0060)

loc = get_location()
print(loc.latitude)    # Clear!
```

---

### Scenario 7: Deduplication

```python
# Remove duplicate IDs from a list
ids = [1, 5, 3, 1, 2, 5, 4, 3]

# Order doesn't matter
unique = list(set(ids))

# Order matters (preserve first occurrence)
unique_ordered = list(dict.fromkeys(ids))
# [1, 5, 3, 2, 4]
```

---

### Scenario 8: Finding Common or Unique Elements

```python
# Who followed us this month but not last month?
last_month_followers = {"alice", "bob", "carol", "dave"}
this_month_followers = {"bob", "carol", "eve", "frank"}

new_followers  = this_month_followers - last_month_followers   # {"eve", "frank"}
lost_followers = last_month_followers - this_month_followers   # {"alice", "dave"}
retained       = last_month_followers & this_month_followers   # {"bob", "carol"}
```

Set operations express this clearly and efficiently.

---

## Anti-Patterns to Avoid

### ❌ List for membership testing in a hot loop

```python
valid_ids = [1001, 1002, 1003, ...]   # 100,000 IDs as a list
for event in stream:                   # 1M events
    if event.user_id in valid_ids:     # O(n) per check = O(100B) total!
        process(event)
```

Fix: convert to `set` before the loop.

### ❌ List.pop(0) instead of deque

```python
queue = []
while queue:
    item = queue.pop(0)   # O(n) each time
```

Fix: use `deque` with `popleft()`.

### ❌ Manual counting instead of Counter

```python
counts = {}
for item in data:
    counts[item] = counts.get(item, 0) + 1  # Works, but verbose
```

Fix: `counts = Counter(data)`.

### ❌ Nested list search instead of dict lookup

```python
# Searching for a student by ID in a list
students = [{"id": 1, "name": "Alice"}, {"id": 2, "name": "Bob"}, ...]
def find_by_id(target_id):
    for s in students:
        if s["id"] == target_id:   # O(n) each call
            return s
```

Fix: build a dict `students_by_id = {s["id"]: s for s in students}` once, then lookup is O(1).

---

## Python-Specific Tips

1. **When in doubt, start with `list`** — it's the most flexible
2. **Profile before optimizing** — often the "slow" code is fast enough
3. **`Counter` is almost always better than manual counting dicts**
4. **`defaultdict` eliminates the "if key not in d" pattern everywhere**
5. **Convert to `set` before membership-testing in a loop**
6. **`deque` for queues; `list` for stacks** (list.append/pop is already O(1))
7. **Use `namedtuple` or `@dataclass` instead of tiny dicts** for records
