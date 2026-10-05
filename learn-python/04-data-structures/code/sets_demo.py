"""
sets_demo.py — Set operations and practical use cases

Topics covered:
  - Creating sets
  - Set operations: union, intersection, difference, symmetric difference
  - Set comprehensions
  - frozenset as dict key
  - Deduplication
  - Membership testing performance comparison
  - Real-world examples: visitor tracking, tag systems, graph algorithms
"""

import time

# =============================================================================
# SECTION 1: Creating Sets
# =============================================================================

print("=" * 60)
print("SECTION 1: Creating Sets")
print("=" * 60)

# Literal syntax
colors = {"red", "green", "blue"}
print(f"Colors:        {colors}")
print(f"Type:          {type(colors)}")

# From any iterable
from_list = set([1, 2, 3, 2, 1])  # Duplicates removed automatically
from_string = set("hello")         # Unique characters
from_range = set(range(5))

print(f"From list:     {from_list}")
print(f"From string:   {from_string}")
print(f"From range:    {from_range}")

# IMPORTANT: Empty set — {} creates a dict, not a set!
empty_dict = {}
empty_set = set()
print(f"\nType of {{}}:   {type(empty_dict)}")  # dict
print(f"Type of set(): {type(empty_set)}")      # set

# =============================================================================
# SECTION 2: Basic Operations
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 2: Basic Operations")
print("=" * 60)

fruits = {"apple", "banana", "cherry", "date"}

# Add and remove
fruits.add("elderberry")
fruits.discard("banana")     # No error if not present
# fruits.remove("mango")     # Would raise KeyError

print(f"After add/discard: {fruits}")

# Membership
print(f"\n'apple' in fruits: {'apple' in fruits}")
print(f"'mango' in fruits: {'mango' in fruits}")

# Size
print(f"len(fruits):       {len(fruits)}")

# =============================================================================
# SECTION 3: Set Operations
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 3: Set Operations")
print("=" * 60)

a = {1, 2, 3, 4, 5}
b = {3, 4, 5, 6, 7}
print(f"A = {a}")
print(f"B = {b}")

# Union — all elements from both
print(f"\nA | B (union):               {a | b}")
print(f"A.union(B):                  {a.union(b)}")

# Intersection — elements in BOTH
print(f"\nA & B (intersection):        {a & b}")
print(f"A.intersection(B):           {a.intersection(b)}")

# Difference — elements in A but NOT B
print(f"\nA - B (difference):          {a - b}")
print(f"B - A (difference, reversed): {b - a}")

# Symmetric difference — elements in ONE but not BOTH
print(f"\nA ^ B (symmetric diff):      {a ^ b}")

# Subset and superset
small = {3, 4, 5}
print(f"\n{small} <= {a}: {small <= a}")   # is subset of
print(f"{small} < {a}:  {small < a}")    # is proper subset
print(f"{a} >= {small}: {a >= small}")   # is superset of
print(f"{small}.isdisjoint({b - a}): {small.isdisjoint({1, 2})}")  # no overlap

# =============================================================================
# SECTION 4: Real-World Examples
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 4: Real-World Examples")
print("=" * 60)

# Example 1: Social network analysis
alice_friends = {"bob", "carol", "dave", "eve", "frank"}
bob_friends   = {"alice", "carol", "grace", "dave", "henry"}

mutual_friends  = alice_friends & bob_friends
only_alice      = alice_friends - bob_friends
only_bob        = bob_friends - alice_friends
all_friends     = alice_friends | bob_friends

print("Social network analysis:")
print(f"  Mutual friends:    {mutual_friends}")
print(f"  Only Alice knows:  {only_alice}")
print(f"  Only Bob knows:    {only_bob}")
print(f"  All known people:  {all_friends}")

# Example 2: Cohort analysis
jan_customers = {"alice", "bob", "carol", "dave", "eve"}
feb_customers = {"bob", "carol", "frank", "grace"}
mar_customers = {"carol", "henry", "alice", "frank"}

new_in_feb   = feb_customers - jan_customers            # New customers
lost_in_feb  = jan_customers - feb_customers            # Churned
retained_feb = jan_customers & feb_customers            # Retained

all_3_months = jan_customers & feb_customers & mar_customers   # Very loyal

print(f"\nCohort analysis:")
print(f"  New in Feb:         {new_in_feb}")
print(f"  Lost after Jan:     {lost_in_feb}")
print(f"  Retained Jan->Feb:  {retained_feb}")
print(f"  All 3 months:       {all_3_months}")

# Example 3: Tag system
article_tags = {
    "article1": {"python", "tutorial", "beginner"},
    "article2": {"python", "advanced", "generators"},
    "article3": {"javascript", "tutorial", "beginner"},
    "article4": {"python", "data-science", "pandas"},
}

# Find articles with "python" and "tutorial"
def find_articles(tags, required_tags):
    required = set(required_tags)
    return [title for title, t in tags.items() if required <= t]

results = find_articles(article_tags, ["python"])
print(f"\nArticles tagged 'python': {results}")

results = find_articles(article_tags, ["tutorial", "beginner"])
print(f"Articles tagged 'tutorial' AND 'beginner': {results}")

# Example 4: Set as "visited" tracker in graph traversal
def find_connected(graph, start):
    """Find all nodes reachable from start using BFS."""
    visited = set()
    queue = [start]

    while queue:
        node = queue.pop(0)
        if node in visited:
            continue
        visited.add(node)
        queue.extend(graph.get(node, []))

    return visited

graph = {
    "A": ["B", "C"],
    "B": ["D"],
    "C": ["D", "E"],
    "D": ["F"],
    "E": ["F"],
    "F": [],
    "G": ["H"],   # disconnected component
}

reachable = find_connected(graph, "A")
print(f"\nReachable from A: {reachable}")
reachable_g = find_connected(graph, "G")
print(f"Reachable from G: {reachable_g}")

# =============================================================================
# SECTION 5: Set Comprehensions
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 5: Set Comprehensions")
print("=" * 60)

# Squares of even numbers up to 20
even_squares = {x**2 for x in range(1, 11) if x % 2 == 0}
print(f"Even squares: {even_squares}")

# Unique first letters of words
words = ["apple", "avocado", "banana", "cherry", "apricot", "blueberry"]
first_letters = {word[0] for word in words}
print(f"First letters: {first_letters}")

# Unique word lengths
sentence = "Python makes programming fun and readable and powerful"
word_lengths = {len(w) for w in sentence.split()}
print(f"Word lengths: {word_lengths}")

# =============================================================================
# SECTION 6: Deduplication
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 6: Deduplication")
print("=" * 60)

names = ["alice", "bob", "alice", "carol", "bob", "dave", "alice"]
print(f"Original:             {names}")

# Order NOT preserved
unique_unordered = list(set(names))
print(f"set (no order):       {unique_unordered}")

# Order preserved (Python 3.7+ — dict maintains insertion order)
unique_ordered = list(dict.fromkeys(names))
print(f"fromkeys (ordered):   {unique_ordered}")

# Deduplicate list of tuples/points
points = [(1, 2), (3, 4), (1, 2), (5, 6), (3, 4)]
unique_points = list(set(points))  # tuples are hashable
print(f"\nUnique points: {unique_points}")

# =============================================================================
# SECTION 7: Membership Testing Performance
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 7: Membership Testing: List vs Set")
print("=" * 60)

N = 500_000

# Create test data
test_list = list(range(N))
test_set = set(range(N))
# Looking for an element near the end (worst case for list)
target = N - 1

iterations = 200

# List membership test
start = time.perf_counter()
for _ in range(iterations):
    _ = target in test_list
list_time = (time.perf_counter() - start) / iterations

# Set membership test
start = time.perf_counter()
for _ in range(iterations):
    _ = target in test_set
set_time = (time.perf_counter() - start) / iterations

print(f"List 'in' (N={N:,}):  {list_time*1000:.4f} ms per check")
print(f"Set  'in' (N={N:,}):  {set_time*1000:.6f} ms per check")
print(f"Set is {list_time/set_time:.0f}x faster for membership testing")

print("\nConclusion: When you need many membership tests, convert to a set FIRST.")

# =============================================================================
# SECTION 8: frozenset
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 8: frozenset — Immutable Set")
print("=" * 60)

# frozenset as a dict key
edge_weights = {
    frozenset({"A", "B"}): 5,
    frozenset({"B", "C"}): 3,
    frozenset({"A", "C"}): 8,
}

# Order doesn't matter for lookup
print(f"Weight A-B: {edge_weights[frozenset({'A', 'B'})]}")
print(f"Weight B-A: {edge_weights[frozenset({'B', 'A'})]}")  # Same edge!

# frozenset in a set
teams = {
    frozenset({"alice", "bob"}),
    frozenset({"carol", "dave"}),
    frozenset({"alice", "eve"}),
}
print(f"\nNumber of teams: {len(teams)}")

# frozenset as a constant — signals immutability
VALID_STATUSES = frozenset({"pending", "processing", "completed", "failed"})

def validate_status(status: str) -> bool:
    return status in VALID_STATUSES

print(f"\n'pending' valid:  {validate_status('pending')}")
print(f"'deleted' valid:  {validate_status('deleted')}")
