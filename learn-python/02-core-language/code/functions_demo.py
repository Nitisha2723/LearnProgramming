"""
functions_demo.py — Demonstrations of all Python function concepts.

Run with:  python code/functions_demo.py

Covers:
  - Basic function definition and calling
  - Return values (including multiple returns)
  - Positional and keyword arguments
  - Default parameter values (and the mutable default trap)
  - *args and **kwargs
  - Docstrings
  - First-class functions (pass, store, return)
  - Lambda functions
  - Pure vs impure functions
  - Closures
  - Type hints
"""

from typing import Optional, Callable

# ─────────────────────────────────────────────────────────────
# SECTION 1: Basic Function Anatomy
# ─────────────────────────────────────────────────────────────
print("=" * 50)
print("SECTION 1: Basic Functions")
print("=" * 50)


def greet(name: str) -> str:
    """Return a greeting message for the given name."""
    return f"Hello, {name}!"


def add(a: int, b: int) -> int:
    """Add two integers and return the result."""
    return a + b


print(greet("Alice"))
print(greet("Bob"))
print(f"3 + 4 = {add(3, 4)}")


def count_and_describe(items: list) -> tuple:
    """
    Return both the count and a description of a list.

    Demonstrates returning multiple values as a tuple.
    """
    n = len(items)
    description = f"{n} item" + ("s" if n != 1 else "")
    return n, description                  # returns a tuple


count, desc = count_and_describe(["a", "b", "c"])
print(f"\ncount_and_describe: {count} → '{desc}'")

count, desc = count_and_describe(["solo"])
print(f"count_and_describe: {count} → '{desc}'")


# ─────────────────────────────────────────────────────────────
# SECTION 2: Positional and Keyword Arguments
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 2: Positional and Keyword Arguments")
print("=" * 50)


def describe_person(name: str, age: int, city: str) -> str:
    return f"{name}, {age} years old, lives in {city}"


# Positional — order matters
print(describe_person("Alice", 30, "Berlin"))

# Keyword — order doesn't matter
print(describe_person(city="Paris", name="Bob", age=25))

# Mixed — positional args must come first
print(describe_person("Carol", city="Tokyo", age=28))


# ─────────────────────────────────────────────────────────────
# SECTION 3: Default Parameter Values
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 3: Default Parameters")
print("=" * 50)


def power(base: float, exponent: float = 2) -> float:
    """Raise base to exponent. Default exponent is 2 (square)."""
    return base ** exponent


print(f"power(3):      {power(3)}")       # 9.0 (exponent defaults to 2)
print(f"power(3, 3):   {power(3, 3)}")    # 27.0
print(f"power(2, 0.5): {power(2, 0.5)}")  # sqrt(2) ≈ 1.414


def repeat(text: str, times: int = 3, separator: str = " ") -> str:
    """Repeat text a number of times, joined by separator."""
    return separator.join([text] * times)


print(f"\nrepeat('ha'):         '{repeat('ha')}'")
print(f"repeat('ha', 5):      '{repeat('ha', 5)}'")
print(f"repeat('ha', 4, '-'): '{repeat('ha', 4, '-')}'")


# The mutable default trap — NEVER do this
print("\n  MUTABLE DEFAULT TRAP:")


def bad_append(item, lst=[]):      # BAD: [] is created once and shared!
    lst.append(item)
    return lst


def good_append(item, lst=None):   # GOOD: None is immutable sentinel
    if lst is None:
        lst = []                   # fresh list each call
    lst.append(item)
    return lst


print(f"  bad_append('a'): {bad_append('a')}")
print(f"  bad_append('b'): {bad_append('b')}")  # ['a', 'b'] — oops!
print(f"  good_append('a'): {good_append('a')}")
print(f"  good_append('b'): {good_append('b')}")  # ['b'] — correct


# ─────────────────────────────────────────────────────────────
# SECTION 4: *args and **kwargs
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 4: *args and **kwargs")
print("=" * 50)


def total(*args: float) -> float:
    """Sum any number of arguments."""
    print(f"  args type: {type(args)}, value: {args}")
    return sum(args)


print(f"total(1, 2, 3):      {total(1, 2, 3)}")
print(f"total(1, 2, 3, 4, 5): {total(1, 2, 3, 4, 5)}")


def show_config(**kwargs) -> None:
    """Display any key-value configuration."""
    print(f"  kwargs type: {type(kwargs)}, value: {kwargs}")
    for key, value in kwargs.items():
        print(f"    {key} = {value}")


print("\nshow_config call:")
show_config(host="localhost", port=8080, debug=True)


def full_signature(required, optional="default", *extra_pos, **extra_kw):
    """Demonstrates the full argument signature."""
    print(f"  required:   {required}")
    print(f"  optional:   {optional}")
    print(f"  extra_pos:  {extra_pos}")
    print(f"  extra_kw:   {extra_kw}")


print("\nFull signature:")
full_signature(1, 2, 3, 4, x=5, y=6)


# Unpacking into function calls
print("\nUnpacking into function calls:")
args_list = [2, 10]
print(f"  power(*{args_list}) = {power(*args_list)}")  # 2^10 = 1024

kwargs_dict = {"base": 3, "exponent": 4}
print(f"  power(**{kwargs_dict}) = {power(**kwargs_dict)}")  # 3^4 = 81


# ─────────────────────────────────────────────────────────────
# SECTION 5: Docstrings
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 5: Docstrings")
print("=" * 50)


def celsius_to_fahrenheit(celsius: float) -> float:
    """
    Convert a temperature from Celsius to Fahrenheit.

    Args:
        celsius: Temperature in degrees Celsius.

    Returns:
        Temperature in degrees Fahrenheit.

    Raises:
        ValueError: If celsius is below absolute zero (-273.15).

    Examples:
        >>> celsius_to_fahrenheit(0)
        32.0
        >>> celsius_to_fahrenheit(100)
        212.0
    """
    if celsius < -273.15:
        raise ValueError(f"Temperature {celsius} is below absolute zero")
    return (celsius * 9 / 5) + 32


temps = [0, 20, 37, 100]
for c in temps:
    f = celsius_to_fahrenheit(c)
    print(f"  {c:6.1f}°C = {f:6.1f}°F")

# Access the docstring
print(f"\n  Function name:  {celsius_to_fahrenheit.__name__}")
print(f"  First doc line: {celsius_to_fahrenheit.__doc__.strip().splitlines()[0]}")


# ─────────────────────────────────────────────────────────────
# SECTION 6: First-Class Functions
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 6: First-Class Functions")
print("=" * 50)


def square(x: float) -> float:
    """Return x squared."""
    return x ** 2


def cube(x: float) -> float:
    """Return x cubed."""
    return x ** 3


def apply(func: Callable[[float], float], value: float) -> float:
    """Apply a function to a value — higher-order function."""
    return func(value)


# Functions as arguments
print("Functions as arguments:")
print(f"  apply(square, 4) = {apply(square, 4)}")
print(f"  apply(cube, 3)   = {apply(cube, 3)}")
print(f"  apply(abs, -7)   = {apply(abs, -7)}")  # built-in works too

# Functions stored in a dict (dispatch table pattern)
print("\nDispatch table:")
operations = {
    "square": square,
    "cube": cube,
    "abs": abs,
    "negate": lambda x: -x,
}

for name, func in operations.items():
    print(f"  {name}(5) = {func(5)}")

# sorted() with key function — very common pattern
print("\nSorted with key function:")
words = ["banana", "apple", "fig", "cherry", "kiwi"]
by_length = sorted(words, key=len)
by_last_char = sorted(words, key=lambda w: w[-1])
print(f"  Original:    {words}")
print(f"  By length:   {by_length}")
print(f"  By last char: {by_last_char}")


# ─────────────────────────────────────────────────────────────
# SECTION 7: Lambda Functions
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 7: Lambda Functions")
print("=" * 50)

# Basic lambda
double = lambda x: x * 2
add_tax = lambda price, rate=0.19: price * (1 + rate)

print(f"double(7)         = {double(7)}")
print(f"add_tax(100)      = {add_tax(100):.2f}")
print(f"add_tax(100, 0.07) = {add_tax(100, 0.07):.2f}")

# Lambda as sort key
print("\nLambda as sort key:")
students = [
    ("Alice", 85, "Computer Science"),
    ("Bob", 92, "Mathematics"),
    ("Carol", 78, "Physics"),
    ("Dave", 92, "Biology"),
]

by_score_desc = sorted(students, key=lambda s: s[1], reverse=True)
by_name = sorted(students, key=lambda s: s[0])

print("  By score (desc):")
for name, score, major in by_score_desc:
    print(f"    {name}: {score}")

print("  By name:")
for name, score, major in by_name:
    print(f"    {name}: {score}")


# ─────────────────────────────────────────────────────────────
# SECTION 8: Pure vs Impure Functions
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 8: Pure vs Impure Functions")
print("=" * 50)


# Pure: same input → same output, no side effects
def pure_add(a: int, b: int) -> int:
    """Pure: always returns a + b, no side effects."""
    return a + b


# Impure: depends on or modifies external state
log_messages = []


def impure_log_and_add(a: int, b: int) -> int:
    """Impure: modifies external log_messages list."""
    result = a + b
    log_messages.append(f"Added {a} + {b} = {result}")  # side effect
    return result


print("Pure function (same inputs → same outputs):")
print(f"  pure_add(3, 4) = {pure_add(3, 4)}")
print(f"  pure_add(3, 4) = {pure_add(3, 4)}")  # identical result

print("\nImpure function (logs each call):")
print(f"  impure_log_and_add(3, 4) = {impure_log_and_add(3, 4)}")
print(f"  impure_log_and_add(1, 2) = {impure_log_and_add(1, 2)}")
print(f"  Log contents: {log_messages}")


# ─────────────────────────────────────────────────────────────
# SECTION 9: Closures
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 9: Closures")
print("=" * 50)


def make_multiplier(factor: float) -> Callable[[float], float]:
    """Return a function that multiplies its input by factor."""
    def multiply(x: float) -> float:
        return x * factor          # 'factor' is captured from the enclosing scope
    return multiply


double = make_multiplier(2)
triple = make_multiplier(3)
halve = make_multiplier(0.5)

print("Closures from make_multiplier:")
print(f"  double(7) = {double(7)}")
print(f"  triple(7) = {triple(7)}")
print(f"  halve(7)  = {halve(7)}")

# Counter closure using nonlocal
def make_counter(start: int = 0) -> Callable[[], int]:
    """Return a counter function — each call increments by 1."""
    count = start

    def increment() -> int:
        nonlocal count
        count += 1
        return count

    return increment


counter_a = make_counter(0)
counter_b = make_counter(100)   # independent counter

print("\nIndependent counters:")
print(f"  counter_a: {counter_a()}, {counter_a()}, {counter_a()}")
print(f"  counter_b: {counter_b()}, {counter_b()}")
print(f"  counter_a: {counter_a()}")  # continues from where it left off


# ─────────────────────────────────────────────────────────────
# SECTION 10: Type Hints
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 10: Type Hints")
print("=" * 50)


def find_first(items: list[str], target: str) -> Optional[int]:
    """
    Find the first index of target in items.

    Returns:
        The index if found, None otherwise.
    """
    try:
        return items.index(target)
    except ValueError:
        return None


fruits = ["apple", "banana", "cherry"]
print(f"find_first(fruits, 'banana') = {find_first(fruits, 'banana')}")
print(f"find_first(fruits, 'mango')  = {find_first(fruits, 'mango')}")


def safe_divide(a: float, b: float) -> float | None:
    """Divide a by b; return None if b is zero (Python 3.10+ syntax)."""
    if b == 0:
        return None
    return a / b


print(f"\nsafe_divide(10, 3)  = {safe_divide(10, 3):.4f}")
print(f"safe_divide(10, 0)  = {safe_divide(10, 0)}")


print("\n--- Demo Complete ---")
