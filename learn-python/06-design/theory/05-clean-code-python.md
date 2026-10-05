# Clean Code in Python

Clean code is code that is easy to read, easy to understand, and easy to change. Python has strong opinions about style. This module teaches you what those opinions are and why they matter.

---

## The Zen of Python

Run `import this` in any Python interpreter. You get 19 aphorisms that guide Python's design. They also guide clean Python code.

```
The Zen of Python, by Tim Peters

Beautiful is better than ugly.
Explicit is better than implicit.
Simple is better than complex.
Complex is better than complicated.
Flat is better than nested.
Sparse is better than dense.
Readability counts.
Special cases aren't special enough to break the rules.
Although practicality beats purity.
Errors should never pass silently.
Unless explicitly silenced.
In the face of ambiguity, refuse the temptation to guess.
There should be one — and preferably only one — obvious way to do it.
Although that way may not be obvious at first unless you're Dutch.
Now is better than never.
Although never is often better than *right* now.
If the implementation is hard to explain, it's a bad idea.
If the implementation is easy to explain, it may be a good idea.
Namespaces are one honking great idea — let's do more of those!
```

### Mapping the Zen to Clean Code Practice

**"Beautiful is better than ugly."**
Code that looks clean, is clean. Consistent indentation, clear naming, and logical structure are not just aesthetic — they reduce cognitive load.

**"Explicit is better than implicit."**
```python
# Implicit — what does True mean here?
process_user(user, True)

# Explicit — self-documenting
process_user(user, notify_by_email=True)
```

**"Simple is better than complex."**
```python
# Complex
result = [x for x in data if x is not None and len(str(x)) > 0 and x != 0]

# Simple
result = [x for x in data if x]  # If "truthy" is the right test
```

**"Flat is better than nested."**
```python
# Deeply nested — hard to read
def process(data):
    if data:
        if data["type"] == "user":
            if data["active"]:
                if data["age"] >= 18:
                    return process_adult_user(data)

# Flat — use early returns (guard clauses)
def process(data):
    if not data:
        return None
    if data["type"] != "user":
        return None
    if not data["active"]:
        return None
    if data["age"] < 18:
        return None
    return process_adult_user(data)
```

**"Errors should never pass silently."**
```python
# Bad — swallows ALL exceptions
try:
    result = do_something()
except:
    pass  # What went wrong? We'll never know.

# Good — handle specific exceptions, log or re-raise
try:
    result = do_something()
except ValueError as e:
    logger.warning(f"Invalid value: {e}")
    raise
```

**"There should be one obvious way to do it."**
When you are choosing between two ways to solve a problem in Python, one is usually "the Pythonic way." Knowing that way separates intermediate Python developers from experts.

---

## PEP 8 — The Python Style Guide

PEP 8 is the official Python style guide. You do not need to memorize it — use a linter (`flake8`, `ruff`) and a formatter (`black`, `autopep8`) and they will enforce it automatically. But you need to understand the key rules.

### Indentation

```python
# Use 4 spaces per level. Never tabs.
def greet(name):
    if name:
        print(f"Hello, {name}")
    else:
        print("Hello, stranger")
```

### Line Length

```python
# Maximum 79 characters (or 88 with Black's configuration)
# Use backslash or parentheses to break long lines

# Long import
from my_module import (
    FirstClass,
    SecondClass,
    ThirdClass,
)

# Long function call
result = some_function_with_long_name(
    argument_one,
    argument_two,
    keyword_argument=some_value,
)
```

### Blank Lines

```python
# Two blank lines before and after top-level functions and classes
def function_one():
    pass


def function_two():
    pass


class MyClass:
    # One blank line between methods inside a class
    def method_one(self):
        pass

    def method_two(self):
        pass
```

### Imports

```python
# Imports at the top, in this order, separated by blank lines:
# 1. Standard library
# 2. Third-party libraries
# 3. Local application modules

# Standard library
import os
import sys
from pathlib import Path

# Third-party
import requests
from pydantic import BaseModel

# Local
from myapp.models import User
from myapp.utils import format_date
```

### Whitespace

```python
# Spaces around operators
x = 1 + 2      # not 1+2
y = x * 2      # not x*2

# No space before colon in slices
items[1:3]     # not items[1 : 3]
items[::-1]    # not items[: : -1]

# No space inside brackets
my_list[0]     # not my_list[ 0 ]
my_dict["key"] # not my_dict[ "key" ]

# Space after comma, not before
f(a, b, c)     # not f(a ,b ,c)
```

---

## Naming Conventions

Python has strict conventions that the entire community follows:

| Type | Convention | Example |
|------|-----------|---------|
| Variables | `snake_case` | `user_name`, `total_count` |
| Functions | `snake_case` | `get_user()`, `calculate_total()` |
| Methods | `snake_case` | `self.get_name()` |
| Classes | `PascalCase` | `UserAccount`, `HttpClient` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_SIZE`, `BASE_URL` |
| Modules | `snake_case` | `user_service.py`, `data_utils.py` |
| Packages | `lowercase` | `myapp`, `utils` |
| Private attributes | `_single_underscore` | `self._connection` |
| Name mangling | `__double_underscore` | `self.__password` |
| Dunder methods | `__double_both__` | `__init__`, `__str__` |

### Good Names vs Bad Names

```python
# BAD — cryptic abbreviations
def calc(d, r):
    return d * (1 - r)

# GOOD — self-documenting
def calculate_discounted_price(price: float, discount_rate: float) -> float:
    return price * (1 - discount_rate)

# BAD — single-letter variables (except loop counters)
for i in range(n):
    v = data[i]
    p = process(v)

# GOOD
for index in range(total_count):
    item = data[index]
    result = process_item(item)

# Exception: single letters are fine as loop counters and math notation
for i in range(10):
    for j in range(10):
        matrix[i][j] = i * j
```

---

## Pythonic Code vs Non-Pythonic

### enumerate — Don't Use range(len())

```python
items = ["apple", "banana", "cherry"]

# Non-Pythonic
for i in range(len(items)):
    print(f"{i}: {items[i]}")

# Pythonic
for i, item in enumerate(items):
    print(f"{i}: {item}")

# With custom start index
for i, item in enumerate(items, start=1):
    print(f"{i}: {item}")
```

### zip — Iterate Multiple Sequences Together

```python
names = ["Alice", "Bob", "Charlie"]
scores = [95, 87, 92]

# Non-Pythonic
for i in range(len(names)):
    print(f"{names[i]}: {scores[i]}")

# Pythonic
for name, score in zip(names, scores):
    print(f"{name}: {score}")

# zip_longest for sequences of different length (from itertools)
from itertools import zip_longest
for name, score in zip_longest(names, scores, fillvalue=0):
    print(f"{name}: {score}")
```

### any() and all() — Readable Checks

```python
ages = [25, 30, 17, 22, 18]

# Non-Pythonic
has_minor = False
for age in ages:
    if age < 18:
        has_minor = True
        break

# Pythonic
has_minor = any(age < 18 for age in ages)
all_adults = all(age >= 18 for age in ages)
```

### Unpacking

```python
# Multiple assignment
x, y, z = 1, 2, 3

# Swap without temp variable
x, y = y, x

# Ignore values with _
first, *middle, last = [1, 2, 3, 4, 5]
print(first)   # 1
print(middle)  # [2, 3, 4]
print(last)    # 5

# Unpack in loops
pairs = [(1, "a"), (2, "b"), (3, "c")]
for number, letter in pairs:
    print(f"{number}: {letter}")
```

### Comprehensions — Build Collections Declaratively

```python
numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

# List comprehension
squares = [x**2 for x in numbers]
evens = [x for x in numbers if x % 2 == 0]

# Dict comprehension
squares_dict = {x: x**2 for x in numbers}

# Set comprehension
unique_remainders = {x % 3 for x in numbers}

# Generator expression (lazy — doesn't build a list)
total = sum(x**2 for x in numbers)

# RULE: If the comprehension is getting complex, use a regular loop
# This is too complex for a comprehension:
result = [
    transform(item)
    for item in get_items()
    if item.is_valid()
    if not item.is_excluded()
]
# Better as a loop with a comment explaining the logic
```

### Context Managers — Always Use with

```python
# Non-Pythonic — resource leak if exception occurs
f = open("data.txt", "r")
data = f.read()
f.close()

# Pythonic — file is always closed, even on exception
with open("data.txt", "r") as f:
    data = f.read()

# Multiple context managers
with open("input.txt") as infile, open("output.txt", "w") as outfile:
    outfile.write(infile.read().upper())
```

---

## Type Hints — When and How

Type hints (PEP 484) make code self-documenting and enable static analysis tools like `mypy`.

### Basic Type Hints

```python
from typing import Optional


def greet(name: str) -> str:
    return f"Hello, {name}!"


def find_user(user_id: int) -> Optional[dict]:
    # Returns a dict or None
    ...


def process_items(items: list[str]) -> dict[str, int]:
    return {item: len(item) for item in items}
```

### Modern Union Syntax (Python 3.10+)

```python
# Old way (3.9 and earlier)
from typing import Union, Optional

def old_style(x: Union[int, str]) -> Optional[str]:
    ...

# New way (3.10+)
def new_style(x: int | str) -> str | None:
    ...
```

### Type Hints for Complex Types

```python
from typing import TypeVar, Generic, Callable, Sequence
from collections.abc import Iterator

T = TypeVar("T")

def first(items: list[T]) -> T | None:
    return items[0] if items else None

def apply_to_all(func: Callable[[int], int], items: list[int]) -> list[int]:
    return [func(item) for item in items]
```

### When to Use Type Hints

- **Always** in public APIs (functions called by other modules)
- **Always** in function signatures for complex data structures
- **Optional** in private implementation details
- **Not needed** in short, obvious scripts

```python
# Good use of type hints
def calculate_compound_interest(
    principal: float,
    annual_rate: float,
    years: int,
    compounds_per_year: int = 12,
) -> float:
    rate_per_period = annual_rate / compounds_per_year
    total_periods = compounds_per_year * years
    return principal * (1 + rate_per_period) ** total_periods

# Over-annotated (too obvious, clutters the code)
x: int = 5           # obvious
name: str = "Alice"  # obvious
```

---

## Docstrings — Three Styles

### Google Style (Recommended for most projects)

```python
def transfer_money(
    from_account: str,
    to_account: str,
    amount: float,
    currency: str = "USD",
) -> dict:
    """Transfer money between two accounts.

    Validates both accounts, checks the balance, and processes the transfer
    atomically. Raises exceptions for any failure condition.

    Args:
        from_account: The account ID to transfer money FROM.
        to_account: The account ID to transfer money TO.
        amount: The amount to transfer. Must be positive.
        currency: The currency code (default: "USD").

    Returns:
        A dictionary containing:
            - transaction_id (str): Unique ID for this transfer.
            - timestamp (str): ISO 8601 timestamp of the transfer.
            - status (str): "completed" or "pending".

    Raises:
        ValueError: If amount is not positive, or if accounts are the same.
        InsufficientFundsError: If from_account has insufficient balance.
        AccountNotFoundError: If either account does not exist.

    Example:
        >>> result = transfer_money("ACC-001", "ACC-002", 100.0)
        >>> result["status"]
        'completed'
    """
    ...
```

### NumPy Style (Common in scientific code)

```python
def calculate_statistics(data: list[float]) -> dict:
    """
    Calculate basic statistics for a dataset.

    Parameters
    ----------
    data : list of float
        The dataset to analyze. Must not be empty.

    Returns
    -------
    dict
        A dictionary with keys 'mean', 'median', 'std', 'min', 'max'.

    Raises
    ------
    ValueError
        If data is empty.

    Examples
    --------
    >>> stats = calculate_statistics([1.0, 2.0, 3.0, 4.0, 5.0])
    >>> stats['mean']
    3.0
    """
    ...
```

---

## Python-Specific Code Smells

### 1. Overusing Classes — Not Everything Needs a Class

```python
# Smell: A class with only one method (just use a function!)
class TemperatureConverter:
    def convert_celsius_to_fahrenheit(self, celsius: float) -> float:
        return celsius * 9/5 + 32

# Better: just a function
def celsius_to_fahrenheit(celsius: float) -> float:
    return celsius * 9/5 + 32
```

### 2. Mutable Default Arguments — A Classic Bug

```python
# DANGEROUS: The list is created once and shared across all calls!
def add_item(item, items=[]):  # items=[] evaluated ONCE at definition time
    items.append(item)
    return items

add_item("a")    # ["a"]
add_item("b")    # ["a", "b"]  — BUG! Not ["b"]
add_item("c")    # ["a", "b", "c"]  — BUG!

# Correct: use None as default, create inside the function
def add_item_correct(item, items=None):
    if items is None:
        items = []
    items.append(item)
    return items
```

### 3. Not Using Context Managers

```python
# Smell: manual resource management
conn = get_database_connection()
try:
    conn.execute("SELECT 1")
finally:
    conn.close()  # Easy to forget, easy to miss

# Pythonic: context manager
with get_database_connection() as conn:
    conn.execute("SELECT 1")
# Connection automatically closed
```

### 4. Catching Too-Broad Exceptions

```python
# DANGEROUS: catches ALL exceptions, including KeyboardInterrupt and SystemExit
try:
    process_data()
except:
    pass  # Silences EVERYTHING. Bugs disappear forever.

# BAD: catches Exception (slightly better but still too broad)
try:
    process_data()
except Exception:
    pass

# GOOD: catch specific exceptions you can handle
try:
    data = json.loads(user_input)
except json.JSONDecodeError as e:
    print(f"Invalid JSON: {e}")
    data = {}
```

### 5. Global State

```python
# Smell: global variable that multiple functions modify
total = 0

def add_to_total(amount):
    global total  # "global" keyword is almost always a red flag
    total += amount

# Better: pass state as parameters or encapsulate in a class
def calculate_total(amounts: list[float]) -> float:
    return sum(amounts)
```

### 6. Not Using Built-in Functions

```python
# Smell: reimplementing what Python already has
def my_max(items):
    result = items[0]
    for item in items[1:]:
        if item > result:
            result = item
    return result

# Just use max()
result = max(items)

# Other built-ins you should know:
min(), max(), sum(), sorted(), reversed()
any(), all(), len(), enumerate(), zip()
map(), filter()  # (though comprehensions are usually clearer)
abs(), round(), divmod(), pow()
```

---

## Clean Code Quick Reference

| Topic | Rule |
|-------|------|
| Names | `snake_case` for everything except classes (`PascalCase`) and constants (`UPPER_SNAKE_CASE`) |
| Functions | Do one thing. If it does many things, split it. |
| Length | Functions: ideally under 20 lines. Classes: ideally under 200 lines. |
| Type hints | Always on public API functions |
| Docstrings | Always on public classes and functions |
| Default args | Never use mutable defaults |
| Exceptions | Always catch specific exceptions |
| Comprehensions | For simple transformations; use loops for complex logic |
| Context managers | Always for files, locks, connections |
| Global state | Avoid |
