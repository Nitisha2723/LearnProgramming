# Introduction to Testing in Python

Welcome to one of the most important parts of this module.
Writing tests is not optional — it is a core professional skill, as important as writing the code itself.

---

## Table of Contents

1. [Why Test?](#1-why-test)
2. [Types of Tests](#2-types-of-tests)
3. [pytest — Python's Best Testing Framework](#3-pytest--pythons-best-testing-framework)
4. [pytest Features In Depth](#4-pytest-features-in-depth)
5. [TDD in Python — Red-Green-Refactor](#5-tdd-in-python--red-green-refactor)
6. [What Makes a Good Test?](#6-what-makes-a-good-test)

---

## 1. Why Test?

### The $327 Million Bug

On September 23, 1999, NASA's **Mars Climate Orbiter** disintegrated in the Martian atmosphere.
The cause: one subsystem calculated thrust in **pound-force seconds** (imperial units), and another
expected **newton-seconds** (metric). A unit conversion bug.

Cost: **$327.6 million** and a decade of work.

Was there a test for unit conversions? No.

This is the most famous software bug in history, but the principle generalizes everywhere.
Most of the bugs you will write in your career are similarly simple — wrong assumption,
wrong type, wrong boundary. Tests exist to catch them before they cost you or your users.

### "Untested Code is Broken Code"

You cannot reason your way to certainty that code is correct. You must verify it.
The universe of edge cases you have not thought about is always larger than the one you have.

### Tests as Living Documentation

A test file tells you:
- What the function is supposed to do
- What inputs are valid
- What outputs are expected
- What edge cases matter

And unlike comments, **tests cannot lie** — they either pass or fail.

### Confidence to Refactor

Without tests, changing code is scary. With tests:
- Refactor aggressively
- Change the implementation while keeping behavior
- Add a feature and immediately know if you broke anything
- Merge others' code confidently

---

## 2. Types of Tests

### The Test Pyramid

```
        ┌──────────────┐
        │  End-to-End  │  ← Few, slow, expensive, brittle
        │   (E2E)      │
       ┌┴──────────────┴┐
       │  Integration   │  ← Some: test how components work together
       │    Tests       │
      ┌┴────────────────┴┐
      │   Unit Tests     │  ← Many: fast, isolated, cheap to write and run
      └──────────────────┘
```

**Unit Tests** — Test a single function or class in isolation.
- Fast (milliseconds each)
- No database, no network, no file system
- Easy to pinpoint failures
- This module's `test_core_language.py` is entirely unit tests

**Integration Tests** — Test how multiple units work together.
- Test that your database layer actually writes and reads correctly
- Test that your API endpoint calls the right service
- Slower, more complex to set up

**End-to-End (E2E) Tests** — Simulate a real user workflow.
- Open a browser, click through the UI, assert the result
- Extremely slow, brittle (a UI change breaks the test)
- Run infrequently (nightly, pre-release)

**The Rule:** Write many unit tests, some integration tests, few E2E tests.
A pyramid, not an ice cream cone.

---

## 3. pytest — Python's Best Testing Framework

### What is pytest?

pytest is Python's dominant testing framework. It is:
- Simpler than `unittest` (Python's built-in framework)
- More powerful: better output, fixtures, parametrize, plugins
- The standard in the Python ecosystem

### Why pytest Over unittest?

| Feature | unittest | pytest |
|---------|----------|--------|
| Test file style | Class-based | Functions (can do both) |
| Assertions | `self.assertEqual(a, b)` | `assert a == b` |
| Error messages | Mediocre | **Excellent** (rewrites assertions) |
| Fixtures | Complex setup/teardown | Elegant `@pytest.fixture` |
| Parametrize | Verbose | `@pytest.mark.parametrize` |
| Discovery | Auto | Auto |
| Plugin ecosystem | Limited | Huge (`pytest-cov`, `pytest-mock`, ...) |

### Installation

```bash
pip install pytest

# With coverage reporting
pip install pytest pytest-cov
```

### Running Tests

```bash
# Run all tests in the current directory and subdirectories
pytest

# Verbose output — shows each test name and result
pytest -v

# Run a specific file
pytest tests/test_core_language.py

# Run a specific test function
pytest tests/test_core_language.py::test_fizzbuzz_basic

# Run tests matching a keyword
pytest -k "fizzbuzz"
pytest -k "not slow"

# Stop after first failure
pytest -x

# Show print() output (normally captured)
pytest -s

# Combine flags
pytest -v -x tests/

# With coverage
pytest --cov=exercises --cov-report=term-missing
```

### Test Discovery Rules

pytest automatically discovers tests by convention:
- File names matching `test_*.py` or `*_test.py`
- Function names starting with `test_`
- Class names starting with `Test` (with methods starting with `test_`)

You do not register tests — pytest finds them automatically.

---

## 4. pytest Features In Depth

### assert Statements — pytest's Magic

Python's `assert` statement raises `AssertionError` if the condition is False.
pytest **rewrites** assertions at collection time to produce rich error messages.

```python
# A failing assert in regular Python:
assert [1, 2, 3] == [1, 2, 4]
# AssertionError  ← not very helpful

# The same assertion in pytest:
# AssertionError: assert [1, 2, 3] == [1, 2, 4]
#   At index 2 diff: 3 != 4
#   Full diff:
#   - [1, 2, 3]
#   ?        ^
#   + [1, 2, 4]
#   ?        ^
```

Use plain `assert` — no `self.assertEqual`, no `self.assertTrue`. Just `assert`.

```python
def test_add():
    assert add(2, 3) == 5
    assert add(0, 0) == 0
    assert add(-1, 1) == 0

def test_empty_list():
    result = remove_duplicates([])
    assert result == []
    assert isinstance(result, list)
    assert len(result) == 0
```

### Fixtures: `@pytest.fixture`

A **fixture** is a function that provides a test with a prepared object or state.
Fixtures replace messy `setUp`/`tearDown` with clean, composable functions.

```python
import pytest

@pytest.fixture
def sample_list():
    """Provide a fresh list for each test."""
    return [3, 1, 4, 1, 5, 9, 2, 6]

@pytest.fixture
def student_names():
    return ["Alice", "Bob", "Carol", "Dave"]

def test_sort_does_not_modify_original(sample_list):
    # sample_list is injected by pytest — a fresh copy each time
    original = sample_list[:]
    sorted_list = sorted(sample_list)
    assert sample_list == original   # original unchanged

def test_list_length(sample_list):
    assert len(sample_list) == 8
```

Fixtures can be scoped (function, class, module, session) and can be shared across files via `conftest.py`.

### Parametrize: Test Multiple Inputs

`@pytest.mark.parametrize` runs the same test with different inputs.
Instead of writing 10 nearly-identical tests, write one parametrized test.

```python
import pytest

@pytest.mark.parametrize("input_str, expected", [
    ("racecar", True),
    ("hello", False),
    ("A man a plan a canal Panama", True),
    ("", True),        # edge case: empty string is a palindrome
    ("a", True),       # single character
])
def test_is_palindrome(input_str, expected):
    assert is_palindrome(input_str) == expected
```

pytest runs this test 5 times, once per row. Each failure is reported independently.

### Testing for Exceptions: `pytest.raises`

```python
def test_celsius_below_absolute_zero():
    with pytest.raises(ValueError):
        celsius_to_fahrenheit(-300)

# Check the error message
def test_celsius_error_message():
    with pytest.raises(ValueError, match="below absolute zero"):
        celsius_to_fahrenheit(-300)
```

### conftest.py — Shared Fixtures

Create a `conftest.py` file in your `tests/` directory. Fixtures defined there are
automatically available to all test files in that directory without importing them.

```python
# tests/conftest.py
import pytest

@pytest.fixture
def sample_numbers():
    return [3, 1, 4, 1, 5, 9, 2, 6]

@pytest.fixture
def empty_list():
    return []
```

---

## 5. TDD in Python — Red-Green-Refactor

**Test-Driven Development (TDD)** means writing the test *before* the code.

### The Red-Green-Refactor Cycle

```
1. RED    → Write a test that fails (it must fail — the code doesn't exist yet)
2. GREEN  → Write the minimum code to make the test pass
3. REFACTOR → Clean up the code, keeping tests green
```

Repeat for each small unit of behavior.

### Worked Example: Temperature Converter

Let us build a `celsius_to_fahrenheit()` function TDD-style.

#### Step 1: RED — Write a Failing Test

```python
# tests/test_temperature.py
from temperature import celsius_to_fahrenheit

def test_freezing_point():
    assert celsius_to_fahrenheit(0) == 32.0
```

Run: `pytest` → **FAILS** (ImportError — file does not exist)

#### Step 2: GREEN — Minimum Code to Pass

```python
# temperature.py
def celsius_to_fahrenheit(celsius):
    return 32.0
```

Run: `pytest` → **PASSES** ← but this is obviously wrong!

#### Step 3: Add Another Test (RED)

```python
def test_boiling_point():
    assert celsius_to_fahrenheit(100) == 212.0
```

Run: `pytest` → **FAILS** (returns 32.0 instead of 212.0)

#### Step 4: GREEN — Real Implementation

```python
def celsius_to_fahrenheit(celsius):
    return (celsius * 9 / 5) + 32
```

Run: `pytest` → **PASSES**

#### Step 5: Add Edge Case (RED)

```python
def test_body_temperature():
    assert celsius_to_fahrenheit(37) == pytest.approx(98.6)  # floating point!

def test_below_absolute_zero_raises():
    with pytest.raises(ValueError):
        celsius_to_fahrenheit(-300)
```

Run: `pytest` → **FAILS** (no validation)

#### Step 6: GREEN — Add Validation

```python
def celsius_to_fahrenheit(celsius: float) -> float:
    if celsius < -273.15:
        raise ValueError(f"Temperature {celsius} is below absolute zero")
    return (celsius * 9 / 5) + 32
```

Run: `pytest` → **PASSES**

#### Step 7: REFACTOR — Add type hints, docstring

(Code stays the same logically, but becomes cleaner.)

### Why TDD Produces Better Design

- You think about the **interface** before the **implementation**
- You discover unclear requirements through test writing
- Every function is testable by definition (you started with the test)
- You only write code that is necessary (no gold-plating)

### `pytest.approx` for Floating Point

Never use `==` to compare floats directly:

```python
0.1 + 0.2 == 0.3    # False! (floating point representation)

# Use pytest.approx
assert 0.1 + 0.2 == pytest.approx(0.3)     # True
assert celsius_to_fahrenheit(37) == pytest.approx(98.6)
```

---

## 6. What Makes a Good Test?

### The F.I.R.S.T Principles

| Letter | Principle | Meaning |
|--------|-----------|---------|
| **F** | Fast | Unit tests should run in milliseconds |
| **I** | Independent | Tests do not depend on each other; any order should work |
| **R** | Repeatable | Same result every time, on any machine |
| **S** | Self-validating | Pass or Fail — no human judgment needed |
| **T** | Timely | Written before or alongside the code, not months later |

### The Arrange-Act-Assert (AAA) Pattern

Structure every test in three phases:

```python
def test_remove_duplicates_preserves_order():
    # ARRANGE — set up the test data
    input_list = [3, 1, 4, 1, 5, 9, 2, 6, 5, 3]

    # ACT — call the function under test
    result = remove_duplicates(input_list)

    # ASSERT — verify the outcome
    assert result == [3, 1, 4, 5, 9, 2, 6]
```

Also called **Given-When-Then** (used in BDD style):

```python
def test_rotate_left_by_zero():
    # GIVEN a list and a rotation of 0
    lst = [1, 2, 3, 4, 5]

    # WHEN we rotate by 0
    result = rotate_left(lst, 0)

    # THEN the list is unchanged
    assert result == [1, 2, 3, 4, 5]
```

### One Concept Per Test

Each test should verify **one thing**. When a test fails, you should immediately know what broke.

```python
# BAD — tests multiple concepts
def test_everything():
    assert fizzbuzz_stats(15)["fizz"] == 4
    assert fizzbuzz_stats(15)["buzz"] == 2
    assert fizzbuzz_stats(15)["fizzbuzz"] == 1
    assert fizzbuzz_stats(0) == {}   # different concept!

# GOOD — separate concerns
def test_fizzbuzz_stats_fizz_count():
    stats = fizzbuzz_stats(15)
    assert stats["fizz"] == 4

def test_fizzbuzz_stats_buzz_count():
    stats = fizzbuzz_stats(15)
    assert stats["buzz"] == 2

def test_fizzbuzz_stats_fizzbuzz_count():
    stats = fizzbuzz_stats(15)
    assert stats["fizzbuzz"] == 1
```

### Descriptive Test Names

Test names are documentation. Make them describe the scenario:

```
test_<function>_<scenario>_<expected_result>
```

Examples:
- `test_is_palindrome_empty_string_returns_true`
- `test_rotate_left_by_more_than_length_wraps_around`
- `test_two_sum_no_solution_returns_none`
- `test_fizzbuzz_divisible_by_15_prints_fizzbuzz`

### Testing Edge Cases

Always consider:
- Empty input (`[]`, `""`, `0`, `None`)
- Single element
- All identical elements
- Maximum / minimum values
- Negative numbers (if applicable)
- Already-sorted input (for sort algorithms)
- Exact boundary values (`< 18` vs `<= 18`)

---

## Running the Test Suite

From the `02-core-language/` directory:

```bash
# Run all tests
pytest tests/ -v

# Run with a specific keyword
pytest tests/ -k "palindrome" -v

# Show what print() produces (useful for debugging)
pytest tests/ -v -s

# Stop at first failure
pytest tests/ -v -x

# Generate a coverage report
pytest tests/ --cov=exercises/solutions --cov-report=term-missing
```

Study `test_core_language.py` — it demonstrates all these pytest features in practice.
