"""
collections_demo.py — Practical use cases for the collections module

Topics covered:
  - deque: browser history simulation, sliding window log buffer
  - Counter: text analysis, vote tallying, histogram
  - namedtuple: representing GPS points, database records
  - defaultdict: building an inverted index, graph
  - ChainMap: layered configuration
"""

from collections import deque, Counter, namedtuple, defaultdict, ChainMap

# =============================================================================
# SECTION 1: deque — Double-Ended Queue
# =============================================================================

print("=" * 60)
print("SECTION 1: deque — Double-Ended Queue")
print("=" * 60)

# 1a: Browser History
print("\n--- Browser History ---")

class BrowserHistory:
    """Simulates browser forward/back navigation using two deques."""

    def __init__(self, max_history: int = 10):
        self._back_stack  = deque(maxlen=max_history)
        self._forward_stack = deque()
        self._current = None

    def visit(self, url: str):
        if self._current:
            self._back_stack.append(self._current)
        self._current = url
        self._forward_stack.clear()  # New navigation clears forward history
        print(f"  Visiting: {url}")

    def back(self) -> str | None:
        if not self._back_stack:
            print("  Already at beginning of history")
            return None
        if self._current:
            self._forward_stack.append(self._current)
        self._current = self._back_stack.pop()
        print(f"  Back to: {self._current}")
        return self._current

    def forward(self) -> str | None:
        if not self._forward_stack:
            print("  Already at most recent page")
            return None
        self._back_stack.append(self._current)
        self._current = self._forward_stack.pop()
        print(f"  Forward to: {self._current}")
        return self._current

    def current(self) -> str | None:
        return self._current

    def history_list(self) -> list[str]:
        return list(self._back_stack)

browser = BrowserHistory()
browser.visit("google.com")
browser.visit("github.com")
browser.visit("stackoverflow.com")
browser.visit("python.org")
print(f"  Current: {browser.current()}")
print(f"  Back history: {browser.history_list()}")
browser.back()
browser.back()
browser.forward()
browser.visit("docs.python.org")  # Clears forward history
browser.forward()  # Nothing to go forward to

# 1b: Sliding Window Log Buffer
print("\n--- Sliding Window Log Buffer ---")

class LogBuffer:
    """Keeps only the last N log entries."""

    def __init__(self, capacity: int = 5):
        self._buffer = deque(maxlen=capacity)

    def log(self, message: str):
        self._buffer.append(message)

    def get_recent(self) -> list[str]:
        return list(self._buffer)

log = LogBuffer(capacity=4)
messages = [
    "Server started",
    "Request received: GET /api/users",
    "DB query executed: 23ms",
    "Response sent: 200 OK",
    "Request received: POST /api/orders",
    "DB query executed: 45ms",
    "Response sent: 201 Created",
]

for msg in messages:
    log.log(msg)

print("Last 4 log entries:")
for entry in log.get_recent():
    print(f"  {entry}")

# 1c: Efficient deque vs list comparison
import time
print("\n--- deque vs list performance ---")
N = 50_000

list_queue = list(range(N))
start = time.perf_counter()
while list_queue:
    list_queue.pop(0)
list_ms = (time.perf_counter() - start) * 1000

deque_queue = deque(range(N))
start = time.perf_counter()
while deque_queue:
    deque_queue.popleft()
deque_ms = (time.perf_counter() - start) * 1000

print(f"list.pop(0) × {N:,}:   {list_ms:.1f}ms")
print(f"deque.popleft() × {N:,}: {deque_ms:.2f}ms")

# =============================================================================
# SECTION 2: Counter — Text Analysis
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 2: Counter — Text Analysis")
print("=" * 60)

# 2a: Word frequency analysis
hamlet_excerpt = """
To be or not to be that is the question
Whether tis nobler in the mind to suffer
The slings and arrows of outrageous fortune
Or to take arms against a sea of troubles
And by opposing end them to die to sleep
No more and by a sleep to say we end
The heartache and the thousand natural shocks
That flesh is heir to tis a consummation
"""

words = hamlet_excerpt.lower().split()
word_freq = Counter(words)

print("Top 10 words in Hamlet excerpt:")
for word, count in word_freq.most_common(10):
    bar = "█" * count
    print(f"  {word:<12} {bar} ({count})")

# 2b: Character frequency — useful for anagram detection
def is_anagram(word1: str, word2: str) -> bool:
    return Counter(word1.lower()) == Counter(word2.lower())

print(f"\n'listen' anagram of 'silent': {is_anagram('listen', 'silent')}")
print(f"'hello'  anagram of 'world':  {is_anagram('hello', 'world')}")

# 2c: Counter arithmetic for inventory
morning_stock = Counter({"apples": 100, "bananas": 80, "oranges": 60})
sales = Counter({"apples": 35, "bananas": 25, "oranges": 60, "grapes": 10})

remaining = morning_stock - sales
print(f"\nMorning stock: {dict(morning_stock)}")
print(f"Sales:         {dict(sales)}")
print(f"Remaining:     {dict(remaining)}")

# Note: missing items from result (negatives dropped)
sold_out = {item for item, qty in (morning_stock - sales).items() if qty == 0}
print(f"Sold out:      {sold_out}")

# 2d: Vote tallying
from random import choices, seed
seed(42)
candidates = ["Alice", "Bob", "Carol", "Dave"]
votes = choices(candidates, weights=[40, 30, 20, 10], k=1000)
tally = Counter(votes)

print(f"\nElection results (1000 votes):")
total = sum(tally.values())
for candidate, count in tally.most_common():
    pct = count / total * 100
    print(f"  {candidate:<10} {count:4d} votes ({pct:.1f}%)")

print(f"\nWinner: {tally.most_common(1)[0][0]}")

# =============================================================================
# SECTION 3: namedtuple — Lightweight Records
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 3: namedtuple — Lightweight Records")
print("=" * 60)

# 3a: Geographic points
Point = namedtuple("Point", ["x", "y"])
Location = namedtuple("Location", ["latitude", "longitude", "name"])

# Without namedtuple — easy to confuse x,y order
p1_tuple = (3, 7)   # Which is x? Which is y?

# With namedtuple — self-documenting
p1 = Point(x=3, y=7)
p2 = Point(x=10, y=4)

import math
def distance(a: Point, b: Point) -> float:
    return math.sqrt((a.x - b.x)**2 + (a.y - b.y)**2)

print(f"Distance from {p1} to {p2}: {distance(p1, p2):.2f}")

# Named locations
cities = [
    Location(51.5074,  -0.1278, "London"),
    Location(48.8566,   2.3522, "Paris"),
    Location(40.7128, -74.0060, "New York"),
    Location(35.6762, 139.6503, "Tokyo"),
]

# Sort by latitude
by_lat = sorted(cities, key=lambda loc: loc.latitude, reverse=True)
print("\nCities by latitude (N to S):")
for city in by_lat:
    print(f"  {city.name:<12} {city.latitude:+.4f}")

# namedtuple still works as a tuple (index access, unpacking)
lat, lon, name = cities[0]
print(f"\nUnpacking: {name} is at ({lat}, {lon})")
print(f"Index access: cities[0][2] = {cities[0][2]}")

# 3b: Database record
Student = namedtuple("Student", ["id", "name", "grade", "gpa"])

students = [
    Student(1, "Alice", 10, 3.8),
    Student(2, "Bob",   11, 3.2),
    Student(3, "Carol", 10, 3.9),
    Student(4, "Dave",  12, 2.9),
]

# Works with sorted, max, etc.
top_student = max(students, key=lambda s: s.gpa)
print(f"\nTop student: {top_student.name} with GPA {top_student.gpa}")

# Convert to dict
print(f"As dict: {top_student._asdict()}")

# Create modified copy with _replace
promoted = students[0]._replace(grade=11)
print(f"After promotion: {promoted}")

# 3c: Using namedtuple for function return values
def statistics(data: list[float]):
    """Return multiple stats as a single named result."""
    Stats = namedtuple("Stats", ["mean", "median", "min", "max", "count"])
    sorted_data = sorted(data)
    n = len(sorted_data)
    median = sorted_data[n // 2] if n % 2 else (sorted_data[n//2-1] + sorted_data[n//2]) / 2
    return Stats(
        mean=sum(data) / n,
        median=median,
        min=min(data),
        max=max(data),
        count=n,
    )

grades = [85, 92, 78, 95, 67, 88, 91]
stats = statistics(grades)
print(f"\nGrade stats: mean={stats.mean:.1f}, median={stats.median}, "
      f"min={stats.min}, max={stats.max}")

# =============================================================================
# SECTION 4: defaultdict — Inverted Index
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 4: defaultdict — Inverted Index (Mini Search Engine)")
print("=" * 60)

documents = {
    "doc1": "python is a great programming language",
    "doc2": "python is used in data science and machine learning",
    "doc3": "java is a popular programming language",
    "doc4": "data science uses python and statistics",
    "doc5": "machine learning requires data and programming skills",
}

# Build inverted index: word -> set of documents containing it
inverted_index = defaultdict(set)
for doc_id, text in documents.items():
    for word in text.split():
        inverted_index[word].add(doc_id)

def search(query: str) -> set[str]:
    """Return documents containing ALL words in query."""
    words = query.lower().split()
    if not words:
        return set()
    results = inverted_index[words[0]].copy()
    for word in words[1:]:
        results &= inverted_index[word]  # Intersection
    return results

print(f"Search 'python': {sorted(search('python'))}")
print(f"Search 'data':   {sorted(search('data'))}")
print(f"Search 'python data': {sorted(search('python data'))}")
print(f"Search 'machine learning': {sorted(search('machine learning'))}")

# =============================================================================
# SECTION 5: ChainMap — Layered Configuration
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 5: ChainMap — Layered Configuration")
print("=" * 60)

# Configuration layers: user > environment > project > defaults
defaults = {
    "debug":   False,
    "log_level": "WARNING",
    "timeout": 30,
    "color":   "blue",
    "max_connections": 10,
}
project_config = {
    "log_level": "INFO",
    "timeout": 60,
    "db_host": "localhost",
}
env_config = {
    "debug": True,
    "db_host": "prod-db.example.com",
}
user_config = {
    "color": "green",
}

# ChainMap: first dict wins for each key
config = ChainMap(user_config, env_config, project_config, defaults)

print("Resolved configuration:")
for key in sorted(set().union(*[d.keys() for d in [defaults, project_config, env_config, user_config]])):
    value = config[key]
    # Find which layer it came from
    for layer_name, layer in [("user", user_config), ("env", env_config),
                               ("project", project_config), ("defaults", defaults)]:
        if key in layer:
            source = layer_name
            break
    print(f"  {key:<20} = {str(value):<10}  (from: {source})")

# Write goes to first map only
config["new_setting"] = "hello"
print(f"\nAfter setting 'new_setting': user_config = {user_config}")

# Create a child scope
child = config.new_child({"timeout": 5, "child_only": True})
print(f"\nChild config 'timeout': {child['timeout']}")  # 5
print(f"Parent config 'timeout': {config['timeout']}")  # 60
