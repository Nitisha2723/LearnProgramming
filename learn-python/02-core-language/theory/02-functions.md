# Theory 02: Functions

Functions are the fundamental unit of code reuse in Python.
A function packages a named, reusable piece of logic — and in Python, functions are first-class
objects, which gives them superpowers Java functions don't have.

---

## Table of Contents

1. [Function Anatomy](#1-function-anatomy)
2. [Return Values](#2-return-values)
3. [Positional and Keyword Arguments](#3-positional-and-keyword-arguments)
4. [Default Parameter Values](#4-default-parameter-values)
5. [*args and **kwargs](#5-args-and-kwargs)
6. [Docstrings](#6-docstrings)
7. [First-Class Functions](#7-first-class-functions)
8. [Lambda Functions](#8-lambda-functions)
9. [Pure Functions](#9-pure-functions)
10. [The LEGB Scope Rule](#10-the-legb-scope-rule)
11. [Type Hints](#11-type-hints)

---

## 1. Function Anatomy

```python
def greet(name):
    """Say hello to someone."""       # docstring (optional but recommended)
    message = f"Hello, {name}!"       # local variable
    print(message)                     # side effect

greet("Alice")    # Hello, Alice!
greet("Bob")      # Hello, Bob!
```

Key parts:
- `def` — keyword that starts a function definition
- `greet` — the function name (use lowercase with underscores: snake_case)
- `(name)` — parameters (inputs), comma-separated, in parentheses
- `:` — the colon that ends the `def` line
- Indented block — the function body
- `"""..."""` — docstring (first string literal, if present)

**Real-world analogy:** A recipe. The ingredients are parameters. The steps are the body.
Every time you "call" the recipe (with specific ingredients), you get a result.

---

## 2. Return Values

```python
def add(a, b):
    return a + b

result = add(3, 4)
print(result)    # 7
```

### `None` by Default

If a function has no `return` statement (or just `return` with no value), it returns `None`.

```python
def greet(name):
    print(f"Hello, {name}!")    # side effect — no return

result = greet("Alice")
print(result)    # None
```

### Returning Multiple Values

Python functions can return multiple values — they come back as a tuple:

```python
def min_max(numbers):
    return min(numbers), max(numbers)    # returns a tuple

low, high = min_max([3, 1, 4, 1, 5, 9])
print(low, high)    # 1 9

# Equivalent:
result = min_max([3, 1, 4, 1, 5, 9])
print(result)       # (1, 9)
print(result[0])    # 1
```

### Early Return

```python
def is_even(n):
    if n % 2 == 0:
        return True
    return False

# Even more concise:
def is_even(n):
    return n % 2 == 0
```

---

## 3. Positional and Keyword Arguments

### Positional Arguments

Values matched to parameters by their position:

```python
def describe_pet(name, species, age):
    print(f"{name} is a {age}-year-old {species}")

describe_pet("Buddy", "dog", 3)       # positional — order matters
```

### Keyword Arguments

Named arguments — order doesn't matter when you name them:

```python
describe_pet(age=3, name="Buddy", species="dog")    # keyword — order irrelevant
describe_pet("Buddy", age=3, species="dog")         # mix is fine: positional first
```

### Positional-Only and Keyword-Only (Advanced)

```python
def strict(pos_only, /, normal, *, kw_only):
    """
    pos_only: must be passed positionally (before /)
    normal:   can be either
    kw_only:  must be passed as keyword (after *)
    """
    pass

strict(1, 2, kw_only=3)    # valid
strict(1, normal=2, kw_only=3)  # valid
```

---

## 4. Default Parameter Values

```python
def power(base, exponent=2):     # exponent has a default
    return base ** exponent

print(power(3))        # 9   (3^2, default used)
print(power(3, 3))     # 27  (3^3, default overridden)
print(power(2, 10))    # 1024
```

### The Mutable Default Trap

**Never** use a mutable object (list, dict) as a default value — it is created once and shared:

```python
# BAD — the list persists between calls!
def add_item(item, items=[]):
    items.append(item)
    return items

print(add_item("a"))    # ['a']
print(add_item("b"))    # ['a', 'b']  ← surprise!

# GOOD — use None as sentinel
def add_item(item, items=None):
    if items is None:
        items = []
    items.append(item)
    return items

print(add_item("a"))    # ['a']
print(add_item("b"))    # ['b']  ← correct
```

---

## 5. `*args` and `**kwargs`

### `*args` — Variable Positional Arguments

```python
def total(*args):
    """Accept any number of positional arguments."""
    print(f"args is a tuple: {args}")
    return sum(args)

print(total(1, 2, 3))           # 6
print(total(1, 2, 3, 4, 5))     # 15
print(total())                  # 0
```

Inside the function, `args` is a **tuple** of all extra positional arguments.

### `**kwargs` — Variable Keyword Arguments

```python
def show_info(**kwargs):
    """Accept any number of keyword arguments."""
    print(f"kwargs is a dict: {kwargs}")
    for key, value in kwargs.items():
        print(f"  {key}: {value}")

show_info(name="Alice", age=30, city="Berlin")
# kwargs is a dict: {'name': 'Alice', 'age': 30, 'city': 'Berlin'}
```

Inside the function, `kwargs` is a **dict** of all extra keyword arguments.

### Combining Everything

```python
def full_example(required, optional="default", *args, **kwargs):
    print(f"required:  {required}")
    print(f"optional:  {optional}")
    print(f"extra pos: {args}")
    print(f"extra kw:  {kwargs}")

full_example(1, 2, 3, 4, x=5, y=6)
# required:  1
# optional:  2
# extra pos: (3, 4)
# extra kw:  {'x': 5, 'y': 6}
```

### Unpacking into a Function Call

```python
def add(a, b, c):
    return a + b + c

numbers = [1, 2, 3]
print(add(*numbers))        # 6  — unpacks list as positional args

config = {"a": 1, "b": 2, "c": 3}
print(add(**config))        # 6  — unpacks dict as keyword args
```

---

## 6. Docstrings

Docstrings are the built-in documentation system for Python functions, classes, and modules.

```python
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
```

### Accessing Docstrings

```python
help(celsius_to_fahrenheit)       # formatted output in the REPL
print(celsius_to_fahrenheit.__doc__)  # raw string
```

### Docstring Formats

The example above uses **Google style**. Other common formats:
- **NumPy style** — popular in data science
- **reStructuredText (reST)** — used by Sphinx documentation generator

Pick one and stay consistent within a project.

---

## 7. First-Class Functions

In Python, functions are **objects**. You can:
- Assign them to variables
- Pass them as arguments
- Return them from other functions
- Store them in lists and dicts

```python
def square(x):
    return x ** 2

# Assign to variable
my_func = square
print(my_func(4))    # 16

# Pass as argument
def apply(func, value):
    return func(value)

print(apply(square, 5))    # 25
print(apply(abs, -7))      # 7  (built-in functions work too)

# Store in a data structure
operations = {
    "double": lambda x: x * 2,
    "square": square,
    "negate": lambda x: -x,
}
print(operations["square"](6))    # 36
```

### Higher-Order Functions

A **higher-order function** takes a function as an argument or returns one:

```python
numbers = [3, -1, 4, -1, 5, -9, 2, 6]

# sorted() accepts a key function
sorted_by_abs = sorted(numbers, key=abs)
print(sorted_by_abs)    # [-1, -1, 2, 3, 4, 5, 6, -9]

# filter() keeps items where func returns True
positives = list(filter(lambda x: x > 0, numbers))
print(positives)    # [3, 4, 5, 2, 6]

# map() applies a function to every item
squared = list(map(square, [1, 2, 3, 4, 5]))
print(squared)    # [1, 4, 9, 16, 25]
```

Note: In modern Python, list comprehensions are often preferred over `map` and `filter`,
but understanding them matters because you will see them in existing code.

---

## 8. Lambda Functions

A `lambda` is an **anonymous one-liner function**.

```python
# Syntax: lambda parameters: expression
double = lambda x: x * 2
add = lambda x, y: x + y

print(double(5))     # 10
print(add(3, 4))     # 7
```

### When to Use Lambdas

Good use cases:
```python
# As a sort key
students = [("Alice", 90), ("Bob", 87), ("Carol", 95)]
students.sort(key=lambda student: student[1], reverse=True)
# [('Carol', 95), ('Alice', 90), ('Bob', 87)]

# Short callbacks
numbers = [1, 2, 3, 4, 5]
doubled = list(map(lambda x: x * 2, numbers))
```

Bad use cases (use `def` instead):
```python
# Too complex for a lambda
f = lambda x: x**2 if x > 0 else -x  # hard to read

# Needs a docstring
# Needs to be called from multiple places
```

**Rule of thumb:** If you need to name a lambda (assign it to a variable that you reuse),
write a proper `def` instead.

---

## 9. Pure Functions

A **pure function**:
1. Given the same inputs, always returns the same output
2. Has no side effects (doesn't modify state outside itself)

```python
# Pure — no side effects, deterministic
def add(a, b):
    return a + b

# Impure — modifies external state
total = 0
def add_to_total(n):
    global total   # accessing external state — impure
    total += n

# Impure — result depends on external state
import random
def random_between(a, b):
    return random.randint(a, b)   # different result each call

# Impure — I/O is a side effect
def log_and_return(x):
    print(f"Got {x}")   # side effect
    return x
```

### Why Pure Functions Matter

- **Testable:** Input → Output, easy to assert
- **Predictable:** No hidden state changes
- **Composable:** Combine confidently
- **Thread-safe:** No shared mutable state

You cannot always write pure functions (I/O is inherently impure), but isolating pure logic
from impure plumbing is a hallmark of good design.

---

## 10. The LEGB Scope Rule

When Python encounters a name, it searches scopes in this order:

```
L → Local       (inside the current function)
E → Enclosing   (functions enclosing this one)
G → Global      (module-level)
B → Built-in    (Python's built-in names: len, print, range, ...)
```

```python
x = "global"

def outer():
    x = "enclosing"

    def inner():
        x = "local"
        print(x)    # local (L found first)

    inner()
    print(x)    # enclosing (L not in outer, E is outer's scope)

outer()
print(x)    # global (L, E not applicable at module level)
```

### The `global` Keyword

```python
count = 0

def increment():
    global count    # tells Python: use the global name, not a new local
    count += 1

increment()
increment()
print(count)    # 2
```

**Warning:** `global` makes code hard to reason about and test. Avoid it except in specific
cases (simple counters, module-level flags). Usually, better to pass values as arguments
and return new values.

### The `nonlocal` Keyword

Used in nested functions to modify an enclosing (but not global) variable:

```python
def make_counter():
    count = 0

    def increment():
        nonlocal count    # modify the enclosing 'count'
        count += 1
        return count

    return increment    # return the inner function (closure)

counter = make_counter()
print(counter())    # 1
print(counter())    # 2
print(counter())    # 3
```

This pattern — a function that returns a function that remembers state — is called a **closure**.
It is one of Python's most powerful idioms.

### Built-in Names

Python has many built-in names: `print`, `len`, `range`, `type`, `int`, `str`, `list`, `True`, `False`, `None`, ...
You can shadow them (define a local variable with the same name), but doing so is almost always a mistake:

```python
# DON'T DO THIS
list = [1, 2, 3]   # shadows the built-in list() constructor
list([4, 5, 6])    # TypeError: 'list' object is not callable
```

---

## 11. Type Hints

Python is dynamically typed — no type declarations required. But **type hints** (Python 3.5+)
are optional annotations that help tools and humans understand your code.

```python
def greet(name: str) -> str:
    return f"Hello, {name}!"

def add(a: int, b: int) -> int:
    return a + b

def process(items: list[str]) -> dict[str, int]:
    return {item: len(item) for item in items}
```

The annotations are just metadata — Python does not enforce them at runtime.
Use tools like **mypy** or IDE type checkers to catch type errors statically.

### Common Type Hint Patterns

```python
from typing import Optional, Union, Callable

def find(items: list, target: str) -> Optional[int]:
    """Returns the index or None if not found."""
    try:
        return items.index(target)
    except ValueError:
        return None

def double_or_int(x: Union[int, float]) -> Union[int, float]:
    return x * 2

def apply_twice(func: Callable[[int], int], value: int) -> int:
    return func(func(value))
```

Python 3.10+ allows the shorter `X | Y` syntax instead of `Union[X, Y]`:

```python
def greet(name: str | None = None) -> str:
    if name is None:
        return "Hello, stranger!"
    return f"Hello, {name}!"
```

---

## Quick Reference

| Feature | Syntax |
|---------|--------|
| Define a function | `def name(params):` |
| Return a value | `return expression` |
| Default parameter | `def f(x, y=10):` |
| Variable positional | `def f(*args):` |
| Variable keyword | `def f(**kwargs):` |
| Lambda | `lambda x: x * 2` |
| Docstring | `"""One-line or multi-line."""` |
| Type hint | `def f(x: int) -> str:` |
| Modify global | `global name` |
| Modify enclosing | `nonlocal name` |

---

## Key Takeaways

1. Python functions return `None` by default — always be explicit about what you return.
2. Never use a mutable default argument — use `None` as a sentinel instead.
3. `*args` → tuple; `**kwargs` → dict — inside the function.
4. Functions are objects — you can pass, store, and return them.
5. `lambda` is for short, throwaway functions — prefer `def` when the function has a name.
6. Pure functions are easier to test, debug, and compose.
7. Type hints are optional but professional — add them for anything non-trivial.

---

*Next: `theory/03-lists-and-tuples.md` — Python's most-used data structures.*
