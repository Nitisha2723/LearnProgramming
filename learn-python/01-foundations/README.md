# Module 01: Foundations

**Status:** Complete  
**Estimated time:** 4–6 hours  
**Prerequisites:** None

---

## What You Will Learn

By the end of this module, you will understand:

- How computers work at a conceptual level (CPU, memory, programs)
- What Python is, where it came from, and why it matters
- How the Python interpreter works
- What happens when Python runs your code
- Python's core data types: integers, floats, strings, booleans, None
- Variables — how to store and use data
- Operators — arithmetic, comparison, logical, assignment
- How to read error messages and fix basic mistakes

---

## Module Structure

```
01-foundations/
├── README.md              ← This file
├── theory/                ← Read these in order (20–30 min each)
│   ├── 01-how-computers-work.md
│   ├── 02-what-is-python.md
│   ├── 03-python-interpreter.md
│   └── 04-how-python-runs.md
├── code/                  ← Study these examples carefully
│   ├── README.md
│   ├── hello_world.py
│   ├── data_types_demo.py
│   └── operators_demo.py
├── exercises/             ← Do these yourself before checking solutions
│   ├── README.md
│   ├── exercise_01_hello_world.py
│   ├── exercise_02_variables.py
│   ├── exercise_03_calculator.py
│   └── solutions/
│       ├── exercise_01_solution.py
│       ├── exercise_02_solution.py
│       └── exercise_03_solution.py
└── mini-project/          ← Build this to complete the module
    ├── README.md
    └── personal_info_card.py
```

---

## Recommended Sequence

1. Read `theory/01-how-computers-work.md`
2. Read `theory/02-what-is-python.md`
3. Read `theory/03-python-interpreter.md`
4. Read `theory/04-how-python-runs.md`
5. Open the Python REPL and experiment as you read
6. Study `code/hello_world.py` — type it yourself, run it
7. Study `code/data_types_demo.py` — type it yourself, run it
8. Study `code/operators_demo.py` — type it yourself, run it
9. Do `exercises/exercise_01_hello_world.py` — read the instructions, write the code
10. Do `exercises/exercise_02_variables.py`
11. Do `exercises/exercise_03_calculator.py`
12. Check your solutions against `exercises/solutions/`
13. Build `mini-project/personal_info_card.py` on your own
14. Review the mini-project solution and notes

---

## Key Concepts at a Glance

### Variables
```python
name = "Alice"        # String
age = 30              # Integer
height = 5.7          # Float
is_student = True     # Boolean
nothing = None        # None (absence of value)
```

### Data Types
| Type | Example | Description |
|------|---------|-------------|
| `int` | `42` | Whole numbers |
| `float` | `3.14` | Decimal numbers |
| `str` | `"hello"` | Text |
| `bool` | `True` / `False` | True or False |
| `None` | `None` | Absence of value |

### Basic Operators
```python
# Arithmetic
10 + 3    # 13
10 - 3    # 7
10 * 3    # 30
10 / 3    # 3.333...  (true division, always float)
10 // 3   # 3         (integer division, floor)
10 % 3    # 1         (remainder/modulo)
10 ** 3   # 1000      (exponentiation)

# Comparison (returns True or False)
10 == 10  # True
10 != 5   # True
10 > 5    # True
10 < 5    # False
```

### print() and f-strings
```python
name = "World"
print("Hello!")               # Hello!
print(f"Hello, {name}!")      # Hello, World!
print(f"2 + 2 = {2 + 2}")    # 2 + 2 = 4
```

---

## Common Mistakes in This Module

**IndentationError** — Python uses indentation (spaces) to define structure. Always use 4 spaces per indent level (not tabs).

**NameError** — You tried to use a variable that does not exist yet. Check your spelling — Python is case-sensitive (`Name` and `name` are different variables).

**TypeError** — You tried to do something with the wrong type. You cannot add a number and a string directly: `"hello" + 5` fails. Use `str(5)` first.

**SyntaxError** — Python cannot read your code. Usually a missing colon, quote, or parenthesis. Read the error message — it tells you the line number.

---

## What Comes Next

After completing this module, move to **Module 02: Core Language**, where you will learn:
- `if`, `elif`, `else` — making decisions
- `for` and `while` loops — repeating actions
- Functions — reusable blocks of code
- Modules — organizing and reusing code
- Error handling with `try` and `except`
