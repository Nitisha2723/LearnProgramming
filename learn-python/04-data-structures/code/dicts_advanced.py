"""
dicts_advanced.py — Advanced dictionary operations and patterns

Topics covered:
  - Dict comprehensions
  - Counter for frequency counting
  - defaultdict for grouping and accumulation
  - Nested dicts
  - Dict merging (Python 3.9+ | operator)
  - Inverting dicts
  - Pattern: word frequency, grouping, graph adjacency list
"""

from collections import Counter, defaultdict
from operator import itemgetter

# =============================================================================
# SECTION 1: Dict Comprehensions
# =============================================================================

print("=" * 60)
print("SECTION 1: Dict Comprehensions")
print("=" * 60)

# Basic: {key: value for item in iterable}
squares = {x: x**2 for x in range(1, 8)}
print(f"Squares dict: {squares}")

# Invert a dict (swap keys and values)
original = {"a": 1, "b": 2, "c": 3, "d": 4}
inverted = {v: k for k, v in original.items()}
print(f"Inverted:     {inverted}")

# Filter: only passing grades
grades = {"Alice": 85, "Bob": 62, "Carol": 91, "Dave": 55, "Eve": 74}
passing = {name: grade for name, grade in grades.items() if grade >= 70}
failing = {name: grade for name, grade in grades.items() if grade < 70}
print(f"\nPassing:      {passing}")
print(f"Failing:      {failing}")

# Transform values
normalized = {name: grade / 100 for name, grade in grades.items()}
print(f"Normalized:   {normalized}")

# From two lists using zip
keys   = ["name", "age", "city"]
values = ["Alice", 30, "NYC"]
record = {k: v for k, v in zip(keys, values)}
print(f"\nRecord from zip: {record}")
# Simpler: dict(zip(keys, values)) — both work

# Nested dict comprehension: multiplication table
times_table = {
    i: {j: i * j for j in range(1, 6)}
    for i in range(1, 6)
}
print(f"\nTimes table 3x4: {times_table[3][4]}")

# =============================================================================
# SECTION 2: Counter — Frequency Counting
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 2: Counter")
print("=" * 60)

# Count characters
char_counts = Counter("mississippi")
print(f"char counts:   {char_counts}")
print(f"count of 's':  {char_counts['s']}")
print(f"count of 'z':  {char_counts['z']}")  # 0, not KeyError

# Count words
text = "to be or not to be that is the question whether tis nobler"
word_counts = Counter(text.split())
print(f"\nWord counts:   {dict(word_counts)}")
print(f"Most common 3: {word_counts.most_common(3)}")

# Count items from a list
survey_responses = [
    "Python", "Java", "Python", "C++", "Python",
    "Java", "Go", "Python", "Rust", "Java"
]
lang_count = Counter(survey_responses)
print(f"\nLanguage survey:")
for lang, count in lang_count.most_common():
    bar = "█" * count
    print(f"  {lang:<10} {bar} ({count})")

# Counter arithmetic
votes_day1 = Counter({"Alice": 120, "Bob": 85, "Carol": 95})
votes_day2 = Counter({"Alice": 105, "Bob": 110, "Carol": 70})

total_votes = votes_day1 + votes_day2
print(f"\nTotal votes: {dict(total_votes)}")

# Find winner
winner = total_votes.most_common(1)[0]
print(f"Winner: {winner[0]} with {winner[1]} votes")

# Update incrementally
click_tracker = Counter()
events = ["btn_login", "btn_signup", "btn_login", "btn_help", "btn_login"]
click_tracker.update(events)
print(f"\nClick counts: {dict(click_tracker)}")

# elements() — iterate with repetition
sample = Counter({"a": 3, "b": 1, "c": 2})
print(f"Elements: {list(sample.elements())}")

# =============================================================================
# SECTION 3: defaultdict — Auto-Creating Missing Keys
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 3: defaultdict")
print("=" * 60)

# Pattern: grouping items
students = [
    ("Alice",  "Engineering"),
    ("Bob",    "Marketing"),
    ("Carol",  "Engineering"),
    ("Dave",   "HR"),
    ("Eve",    "Marketing"),
    ("Frank",  "Engineering"),
]

# Without defaultdict — the check pattern
by_dept_old = {}
for name, dept in students:
    if dept not in by_dept_old:
        by_dept_old[dept] = []
    by_dept_old[dept].append(name)

# With defaultdict — no check needed
by_dept = defaultdict(list)
for name, dept in students:
    by_dept[dept].append(name)

print("Students by department:")
for dept, names in sorted(by_dept.items()):
    print(f"  {dept}: {names}")

# defaultdict(int) for counting
text = "the quick brown fox jumps over the lazy dog"
word_freq = defaultdict(int)
for word in text.split():
    word_freq[word] += 1   # No KeyError — missing key defaults to 0

print(f"\nWord frequencies: {dict(word_freq)}")

# defaultdict(set) for collecting unique values
user_hobbies = [
    ("alice", "reading"),
    ("alice", "hiking"),
    ("bob",   "gaming"),
    ("alice", "gaming"),
    ("bob",   "reading"),
]

hobby_map = defaultdict(set)
for user, hobby in user_hobbies:
    hobby_map[user].add(hobby)

print(f"\nUser hobbies:")
for user, hobbies in sorted(hobby_map.items()):
    print(f"  {user}: {sorted(hobbies)}")

# Graph as adjacency list — a classic use of defaultdict
edges = [("A", "B"), ("A", "C"), ("B", "D"), ("C", "D"), ("D", "E")]
graph = defaultdict(list)
for src, dst in edges:
    graph[src].append(dst)
    graph[dst].append(src)  # undirected

print(f"\nGraph adjacency list:")
for node in sorted(graph.keys()):
    print(f"  {node} -> {sorted(graph[node])}")

# =============================================================================
# SECTION 4: Nested Dicts
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 4: Nested Dicts")
print("=" * 60)

# Build a nested structure
school = {
    "students": {
        "alice": {"grade": 10, "gpa": 3.8, "subjects": ["math", "science"]},
        "bob":   {"grade": 11, "gpa": 3.2, "subjects": ["english", "art"]},
        "carol": {"grade": 10, "gpa": 3.9, "subjects": ["math", "english"]},
    },
    "teachers": {
        "smith": {"subject": "math",    "years_exp": 15},
        "jones": {"subject": "science", "years_exp": 8},
    }
}

# Access
alice_gpa = school["students"]["alice"]["gpa"]
print(f"Alice's GPA: {alice_gpa}")

# Safe nested access with get()
dave_gpa = school["students"].get("dave", {}).get("gpa", "Not found")
print(f"Dave's GPA:  {dave_gpa}")

# Find all grade-10 students
grade_10 = [
    name for name, info in school["students"].items()
    if info["grade"] == 10
]
print(f"Grade 10:    {grade_10}")

# Sort students by GPA
sorted_students = sorted(
    school["students"].items(),
    key=lambda x: x[1]["gpa"],
    reverse=True
)
print("\nStudents by GPA (desc):")
for name, info in sorted_students:
    print(f"  {name}: {info['gpa']}")

# =============================================================================
# SECTION 5: Dict Merging and Updating
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 5: Merging Dicts")
print("=" * 60)

defaults = {"color": "blue", "size": "medium", "debug": False, "timeout": 30}
user_prefs = {"color": "red", "size": "large"}

# Python 3.9+ — | operator
merged = defaults | user_prefs    # New dict, user_prefs wins
print(f"Merged (|):     {merged}")

# update() — modifies in place
config = dict(defaults)
config.update(user_prefs)
print(f"update():       {config}")

# Python 3.9+ in-place
config2 = dict(defaults)
config2 |= user_prefs
print(f"In-place (|=):  {config2}")

# =============================================================================
# SECTION 6: Dict Patterns and Idioms
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 6: Dict Patterns and Idioms")
print("=" * 60)

# Pattern: count-then-rank
text2 = """python is great python makes programming fun
          python is readable python is powerful"""
words = text2.split()
ranked = Counter(words).most_common()
print("Word ranking:")
for rank, (word, count) in enumerate(ranked, start=1):
    print(f"  {rank}. {word}: {count}")

# Pattern: de-duplicate with dict.fromkeys (preserves order)
duplicates = ["alice", "bob", "alice", "carol", "bob", "dave"]
unique_ordered = list(dict.fromkeys(duplicates))
print(f"\nDeduped (order preserved): {unique_ordered}")

# Pattern: groupby with defaultdict, then compute stats
scores_data = [
    ("Alice", "math",    90),
    ("Bob",   "math",    75),
    ("Alice", "science", 88),
    ("Bob",   "science", 82),
    ("Carol", "math",    95),
    ("Carol", "science", 79),
]

# Group by subject, collect scores
subject_scores = defaultdict(list)
for _, subject, score in scores_data:
    subject_scores[subject].append(score)

print("\nSubject averages:")
for subject, scores in sorted(subject_scores.items()):
    avg = sum(scores) / len(scores)
    print(f"  {subject}: avg={avg:.1f}, min={min(scores)}, max={max(scores)}")

# Group by student, compute GPA
student_scores = defaultdict(list)
for student, _, score in scores_data:
    student_scores[student].append(score)

print("\nStudent averages:")
for student, scores in sorted(student_scores.items()):
    avg = sum(scores) / len(scores)
    print(f"  {student}: {avg:.1f}")
