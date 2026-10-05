# Theory 05: Scope and Namespaces

Understanding scope is the difference between code that accidentally works and code that
reliably works. Python's scoping rules are simple, consistent, and worth mastering.

---

## Table of Contents

1. [What is a Namespace?](#1-what-is-a-namespace)
2. [The LEGB Rule](#2-the-legb-rule)
3. [The `global` Keyword](#3-the-global-keyword)
4. [The `nonlocal` Keyword](#4-the-nonlocal-keyword)
5. [Closures](#5-closures)
6. [Everything is an Object](#6-everything-is-an-object)
7. [Python's Object Model](#7-pythons-object-model)

---

## 1. What is a Namespace?

A **namespace** is a mapping from names to objects — essentially a dictionary.

Python uses multiple namespaces simultaneously:
- The **global** namespace of the current module
- The **local** namespace of each function call
- The **built-in** namespace (always available)

```python
import sys

# Inspect the current namespaces
print(dir())          # names in current local/global scope
print(dir(__builtins__))  # built-in names: print, len, range, ...
print(sys.modules)    # all imported modules
```

When you write `x = 5`, Python adds the name `x` to the current namespace.
When you read `x`, Python searches namespaces in order.

### Name Lookup vs. Assignment

This distinction is critical:
- **Reading** a name → searches LEGB
- **Assigning** a name → always assigns in the **local** namespace (unless `global`/`nonlocal`)

```python
x = 10    # global namespace

def show():
    print(x)    # reading x → searches LEGB, finds global x

def modify():
    x = 20      # ASSIGNMENT creates a new LOCAL x — does not touch global x
    print(x)    # 20

show()      # 10
modify()    # 20
print(x)    # 10 (global is unchanged)
```

---

## 2. The LEGB Rule

Python resolves names by searching **four** scopes in order:

```
L — Local       Current function's scope
E — Enclosing   Outer function's scope(s) (for nested functions)
G — Global      Module-level scope
B — Built-in    Python's built-in names (print, len, range, ...)
```

Python searches L first, then E, then G, then B.
The first match wins. If no match is found: `NameError`.

### Full LEGB Example

```python
x = "global"        # G

def outer():
    x = "enclosing" # E (from inner's perspective)

    def inner():
        x = "local" # L
        print(x)    # L: "local"

    inner()
    print(x)        # E (in outer's scope, not inner's): "enclosing"

outer()
print(x)            # G: "global"
```

### Enclosing Scope Without Local Override

```python
message = "global message"

def outer():
    message = "outer message"   # E for inner

    def inner():
        print(message)          # no local 'message' → finds E: "outer message"

    inner()

outer()
```

### Built-in Scope

```python
def demo():
    # 'print' and 'len' come from built-in scope (B)
    items = [1, 2, 3]
    print(len(items))    # both print and len are found in B
```

Built-in names include: `print`, `len`, `range`, `type`, `int`, `str`, `list`, `dict`,
`set`, `tuple`, `bool`, `input`, `open`, `zip`, `map`, `filter`, `sorted`, `sum`,
`min`, `max`, `abs`, `round`, `enumerate`, `isinstance`, `issubclass`, `True`, `False`, `None`, ...

---

## 3. The `global` Keyword

`global` declares that a name refers to the global scope, not local.

```python
count = 0

def increment():
    global count    # "I want to use the global 'count', not create a local one"
    count += 1

increment()
increment()
print(count)    # 2
```

### When NOT to Use `global`

`global` is almost always the wrong tool. It makes code:
- Hard to test (function depends on external state)
- Hard to reason about (any function might change anything)
- Prone to bugs (order of calls matters)

```python
# BAD — global mutable state
total = 0

def add(n):
    global total
    total += n

# GOOD — pass values, return values
def add(total, n):
    return total + n

total = 0
total = add(total, 5)
total = add(total, 3)
```

**Legitimate uses of `global`:**
- Module-level constants (though usually just don't reassign them)
- Simple counters/flags in scripts (not in libraries)
- Caching/memoization (though `functools.lru_cache` is better)

---

## 4. The `nonlocal` Keyword

`nonlocal` is like `global` but for enclosing (not global) scopes.
It is specifically for **nested functions** that need to modify an outer variable.

```python
def make_accumulator():
    total = 0               # enclosing variable

    def add(n):
        nonlocal total      # refer to enclosing 'total', not a new local
        total += n
        return total

    return add

acc = make_accumulator()
print(acc(10))  # 10
print(acc(20))  # 30
print(acc(5))   # 35

# Each accumulator has its own 'total'
acc2 = make_accumulator()
print(acc2(100))  # 100 (independent from acc)
```

### `nonlocal` Without `global`

```python
x = 10

def outer():
    x = 20

    def inner():
        nonlocal x   # refers to outer's x (enclosing), NOT the global x
        x += 1
        print(x)

    inner()           # 21
    print(x)          # 21 (outer's x was modified)

outer()
print(x)              # 10 (global x is unchanged)
```

---

## 5. Closures

A **closure** is a function that remembers variables from its defining scope,
even after that scope has finished executing.

```python
def make_multiplier(factor):
    """Returns a function that multiplies by factor."""
    def multiply(x):
        return x * factor    # 'factor' is closed over — remembered
    return multiply

double = make_multiplier(2)
triple = make_multiplier(3)

print(double(5))    # 10
print(triple(5))    # 15

# The 'factor' variable still exists, captured in the closure
print(double.__closure__[0].cell_contents)    # 2
```

### The Classic Closure Trap

```python
# WRONG — all functions share the same 'i' from the loop
functions = []
for i in range(5):
    functions.append(lambda: i)   # all lambdas capture the same 'i'

print([f() for f in functions])    # [4, 4, 4, 4, 4]  (all see final i=4)

# CORRECT — capture the current value with a default argument
functions = []
for i in range(5):
    functions.append(lambda i=i: i)   # i=i creates a new local 'i' each iteration

print([f() for f in functions])    # [0, 1, 2, 3, 4]
```

### Practical Closure Example

```python
def make_validator(min_val, max_val):
    """Creates a range validator function."""
    def validate(value):
        if not (min_val <= value <= max_val):
            raise ValueError(f"{value} must be between {min_val} and {max_val}")
        return value
    return validate

validate_age = make_validator(0, 120)
validate_percentage = make_validator(0, 100)

print(validate_age(25))           # 25
print(validate_percentage(85))    # 85
validate_age(150)                 # raises ValueError
```

---

## 6. Everything is an Object

This is one of Python's most important design principles, and it has real practical consequences.

In Python, **every value is an object**:
- Numbers are objects: `42` is an instance of `int`
- Strings are objects: `"hello"` is an instance of `str`
- Functions are objects: `def foo(): ...` creates a `function` object
- Classes are objects: `class Foo: ...` creates a `type` object
- Even `None`, `True`, and `False` are objects

```python
# Everything has a type
print(type(42))           # <class 'int'>
print(type("hello"))      # <class 'str'>
print(type([1, 2, 3]))    # <class 'list'>
print(type(len))          # <class 'builtin_function_or_method'>

def greet():
    pass

print(type(greet))        # <class 'function'>

# Everything has an id (memory address)
x = 42
print(id(x))              # some memory address

# Everything has attributes
print((42).bit_length())  # 6  (number of bits needed to represent 42)
print("hello".upper())    # HELLO
```

### Consequences

1. **Functions can be passed as arguments:**

```python
def apply(func, value):
    return func(value)

print(apply(abs, -5))       # 5
print(apply(len, "hello"))  # 5
```

2. **Functions can be stored in collections:**

```python
operations = [str.upper, str.lower, str.strip]
text = "  Hello  "
for op in operations:
    print(op(text))
```

3. **Functions can be returned:**

```python
def get_formatter(style):
    if style == "upper":
        return str.upper
    elif style == "lower":
        return str.lower
    else:
        return str.strip
```

4. **Classes are objects too (metaclasses):**

```python
class Dog:
    pass

# Dog is an object of type 'type'
print(type(Dog))    # <class 'type'>
print(isinstance(Dog, type))  # True
```

---

## 7. Python's Object Model

### Variables are Name Tags

In Python, a variable is not a box that holds a value — it is a **name tag** pointing to an object.

```python
a = [1, 2, 3]
b = a             # b points to the SAME list object as a

b.append(4)
print(a)    # [1, 2, 3, 4]  ← a is affected because a and b point to the same object

# Check if they are the same object
print(a is b)     # True
print(id(a) == id(b))  # True
```

```python
# Numbers and strings are usually interned (small ones shared)
x = 5
y = 5
print(x is y)     # True  (same object, implementation detail)

x = [1, 2, 3]
y = [1, 2, 3]
print(x is y)     # False (different objects)
print(x == y)     # True  (same value)
```

### `is` vs `==`

- `==` tests **value equality**
- `is` tests **identity** (same object in memory)

```python
a = [1, 2, 3]
b = [1, 2, 3]
c = a

print(a == b)    # True   — same values
print(a is b)    # False  — different objects
print(a is c)    # True   — same object

# Use 'is' only for:
if result is None:    ...   # checking for None
if flag is True:      ...   # rarely needed (just: if flag:)
```

### Mutable vs Immutable Objects

| Mutable | Immutable |
|---------|-----------|
| `list` | `int`, `float`, `bool` |
| `dict` | `str` |
| `set` | `tuple` |
| `bytearray` | `bytes`, `frozenset` |
| Most custom objects | `None` |

Mutable objects can be modified in place. Immutable objects cannot.

```python
# Immutable — "modification" creates a new object
s = "hello"
s += " world"    # creates a NEW string, s points to it
                  # the original "hello" object still exists (until garbage collected)

# Mutable — modification in place
lst = [1, 2, 3]
lst.append(4)    # SAME list object, just modified
```

### The `id()` Gotcha

```python
a = 1000
b = 1000
print(a is b)    # might be False — depends on Python implementation
                  # small integers (-5 to 256) are cached, large ones aren't

# Never rely on 'is' for value equality with numbers and strings
# Always use '==' for value comparisons
```

---

## Quick Reference

| Concept | Key Point |
|---------|-----------|
| LEGB | L → E → G → B search order |
| `global` | Modify global from inside a function |
| `nonlocal` | Modify enclosing variable from nested function |
| Closure | Function + captured enclosing variables |
| Everything is an object | Functions, classes, modules — all have attributes and `id` |
| Variables | Name tags pointing to objects (not boxes) |
| `is` vs `==` | Identity vs value equality |

---

## Key Takeaways

1. Python searches names in **LEGB** order — Local first, Built-in last.
2. **Assignment** always creates a local name, unless `global`/`nonlocal` is declared.
3. Avoid `global` — pass values as arguments and return new values instead.
4. `nonlocal` is for nested functions modifying enclosing (not global) variables.
5. **Closures** remember their enclosing scope — powerful but can cause confusion in loops.
6. **Everything is an object** — functions are first-class, can be passed and returned.
7. Variables are **name tags**, not boxes — `a = b` makes two names point to the same object.
8. Use `==` for value comparison; use `is` only for identity checks (especially `is None`).

---

*You have completed all five theory files. Now head to `code/` to see everything in action.*
