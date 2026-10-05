"""
control_flow_demo.py — Runnable demonstrations of all Python control flow constructs.

Run with:  python code/control_flow_demo.py

Covers:
  - if/elif/else
  - Truthy and falsy values
  - Ternary expression
  - match statement (Python 3.10+)
  - while and while-else
  - for and for-else
  - range()
  - List comprehensions
  - break, continue, pass
"""

# ─────────────────────────────────────────────────────────────
# SECTION 1: if / elif / else
# ─────────────────────────────────────────────────────────────
print("=" * 50)
print("SECTION 1: if / elif / else")
print("=" * 50)


def classify_grade(score):
    """Classify a numeric score into a letter grade."""
    if score >= 90:
        return "A"
    elif score >= 80:
        return "B"
    elif score >= 70:
        return "C"
    elif score >= 60:
        return "D"
    else:
        return "F"


test_scores = [95, 83, 71, 62, 45]
for score in test_scores:
    grade = classify_grade(score)
    print(f"  Score {score} → Grade {grade}")


def check_login(username, password):
    """Demonstrate nested if with multiple conditions."""
    valid_users = {"admin": "secret123", "alice": "password"}

    if username in valid_users:
        if valid_users[username] == password:
            return f"Welcome, {username}!"
        else:
            return "Wrong password"
    else:
        return f"Unknown user: {username}"


print("\nLogin checks:")
print(" ", check_login("alice", "password"))
print(" ", check_login("alice", "wrong"))
print(" ", check_login("bob", "anything"))


# ─────────────────────────────────────────────────────────────
# SECTION 2: Truthy and Falsy
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 2: Truthy and Falsy Values")
print("=" * 50)

falsy_values = [False, 0, 0.0, "", [], (), {}, None]
truthy_values = [True, 1, -1, "a", [0], (None,), {"x": 1}]

print("Falsy values:")
for val in falsy_values:
    verdict = "falsy" if not val else "truthy"
    print(f"  {repr(val):15} → {verdict}")

print("\nTruthy values:")
for val in truthy_values:
    verdict = "falsy" if not val else "truthy"
    print(f"  {repr(val):15} → {verdict}")


def process_items(items):
    """Pythonic check: use the list directly as the condition."""
    if items:                          # True when items is non-empty
        print(f"\n  Processing {len(items)} items: {items}")
    else:
        print("\n  Nothing to process")


process_items([1, 2, 3])
process_items([])


# ─────────────────────────────────────────────────────────────
# SECTION 3: Ternary Expression
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 3: Ternary Expression")
print("=" * 50)

# Syntax: value_if_true if condition else value_if_false
for age in [16, 18, 21]:
    status = "adult" if age >= 18 else "minor"
    print(f"  Age {age} → {status}")

# Useful inside expressions
numbers = [-3, -1, 0, 2, 5]
absolutes = [n if n >= 0 else -n for n in numbers]
print(f"\n  Numbers:  {numbers}")
print(f"  Absolute: {absolutes}")


# ─────────────────────────────────────────────────────────────
# SECTION 4: match Statement (Python 3.10+)
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 4: match Statement")
print("=" * 50)


def describe_command(command):
    """Match on string values — like a switch statement but cleaner."""
    match command:
        case "quit" | "exit" | "q":
            return "Exiting..."
        case "help" | "h":
            return "Available commands: quit, help, status"
        case "status":
            return "System running normally"
        case _:
            return f"Unknown command: {command!r}"


for cmd in ["help", "status", "exit", "unknown"]:
    print(f"  '{cmd}' → {describe_command(cmd)}")


def describe_point(point):
    """Structural pattern matching on tuples."""
    match point:
        case (0, 0):
            return "Origin"
        case (x, 0):
            return f"On x-axis at {x}"
        case (0, y):
            return f"On y-axis at {y}"
        case (x, y):
            return f"Point at ({x}, {y})"


print("\nPoint matching:")
for p in [(0, 0), (3, 0), (0, -4), (3, 7)]:
    print(f"  {p} → {describe_point(p)}")


def process_event(event):
    """Match on dictionary structure."""
    match event:
        case {"type": "click", "x": x, "y": y}:
            return f"Click at ({x}, {y})"
        case {"type": "keypress", "key": key}:
            return f"Key pressed: {key}"
        case {"type": "scroll", "direction": dir}:
            return f"Scrolled {dir}"
        case _:
            return f"Unknown event: {event}"


print("\nEvent matching:")
events = [
    {"type": "click", "x": 100, "y": 200},
    {"type": "keypress", "key": "Enter"},
    {"type": "scroll", "direction": "down"},
]
for event in events:
    print(f"  {process_event(event)}")


# ─────────────────────────────────────────────────────────────
# SECTION 5: while Loop
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 5: while Loop")
print("=" * 50)

# Basic while
print("Counting down:")
count = 5
while count > 0:
    print(f"  {count}...")
    count -= 1
print("  Blast off!")


# while-else: the else runs only when condition becomes False (not on break)
def find_in_range(target, start, end):
    """Uses while-else to signal search outcome."""
    current = start
    while current <= end:
        if current == target:
            print(f"  Found {target}!")
            break
        current += 1
    else:
        # Only reaches here if the while condition became False (no break)
        print(f"  {target} not found between {start} and {end}")


print("\nwhile-else search:")
find_in_range(7, 1, 10)
find_in_range(15, 1, 10)


# ─────────────────────────────────────────────────────────────
# SECTION 6: for Loop
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 6: for Loop")
print("=" * 50)

# Iterating over different types
print("Iterating over a list:")
fruits = ["apple", "banana", "cherry"]
for fruit in fruits:
    print(f"  {fruit}")

print("\nIterating over a string:")
for char in "Python":
    print(f"  '{char}'", end="")
print()

# enumerate — get index and value together
print("\nenumerate — index and value:")
for i, fruit in enumerate(fruits, start=1):
    print(f"  {i}. {fruit}")

# zip — iterate over two sequences simultaneously
print("\nzip — two lists in parallel:")
names = ["Alice", "Bob", "Carol"]
scores = [92, 87, 95]
for name, score in zip(names, scores):
    print(f"  {name}: {score}")

# for-else: else runs only if the loop completes without break
print("\nfor-else (prime check):")


def is_prime(n):
    """Demonstrate for-else for prime detection."""
    if n < 2:
        return False
    for i in range(2, int(n**0.5) + 1):
        if n % i == 0:
            return False          # found a factor — not prime
    else:
        return True               # loop finished without finding a factor


for n in range(2, 20):
    if is_prime(n):
        print(f"  {n} is prime")


# ─────────────────────────────────────────────────────────────
# SECTION 7: range()
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 7: range()")
print("=" * 50)

print("range(5):", list(range(5)))
print("range(1, 6):", list(range(1, 6)))
print("range(0, 20, 4):", list(range(0, 20, 4)))
print("range(10, 0, -2):", list(range(10, 0, -2)))

# Multiplication table using nested range
print("\n3x3 multiplication table:")
for row in range(1, 4):
    for col in range(1, 4):
        print(f"  {row}×{col}={row*col}", end="  ")
    print()


# ─────────────────────────────────────────────────────────────
# SECTION 8: List Comprehensions
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 8: List Comprehensions")
print("=" * 50)

# Basic comprehension
squares = [x**2 for x in range(1, 8)]
print(f"Squares: {squares}")

# With filter
even_squares = [x**2 for x in range(1, 11) if x % 2 == 0]
print(f"Even squares: {even_squares}")

# Comprehension with string transformation
words = ["hello", "WORLD", "Python", "programming"]
normalized = [w.lower().strip() for w in words]
print(f"Normalized: {normalized}")

# Nested comprehension — flatten a matrix
matrix = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
flat = [n for row in matrix for n in row]
print(f"Flattened matrix: {flat}")


# ─────────────────────────────────────────────────────────────
# SECTION 9: break, continue, pass
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 9: break, continue, pass")
print("=" * 50)

# break — exit loop early
print("break: stop at 5")
for n in range(10):
    if n == 5:
        break
    print(f"  {n}", end=" ")
print()

# continue — skip rest of this iteration
print("\ncontinue: skip even numbers")
for n in range(10):
    if n % 2 == 0:
        continue
    print(f"  {n}", end=" ")
print()

# pass — placeholder, does nothing
print("\npass: skeleton loop (does nothing yet)")
for _ in range(3):
    pass    # TODO: implement logic
print("  (loop ran 3 times but did nothing)")

# Practical pattern: early break with a sentinel
print("\nFind first number divisible by 7:")
for n in range(100):
    if n % 7 == 0 and n > 0:
        print(f"  First positive multiple of 7: {n}")
        break


print("\n--- Demo Complete ---")
