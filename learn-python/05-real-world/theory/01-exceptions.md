# Exceptions in Python

Exceptions are how Python signals that something went wrong. Understanding them well
is essential — both for writing code that handles errors gracefully and for building
APIs that give useful error messages to callers.

---

## The Exception Hierarchy

Python exceptions form a class hierarchy. All exceptions ultimately inherit from `BaseException`.

```
BaseException
├── SystemExit              ← sys.exit() raises this
├── KeyboardInterrupt       ← Ctrl+C
├── GeneratorExit           ← Generator closed
└── Exception               ← The base for all regular exceptions
    ├── ArithmeticError
    │   ├── ZeroDivisionError
    │   ├── OverflowError
    │   └── FloatingPointError
    ├── LookupError
    │   ├── IndexError       ← list[99] on a 5-element list
    │   └── KeyError         ← dict["missing_key"]
    ├── ValueError           ← Right type, wrong value: int("hello")
    ├── TypeError            ← Wrong type: "a" + 1
    ├── AttributeError       ← obj.no_such_attribute
    ├── NameError            ← Using undefined variable
    ├── IOError / OSError    ← File not found, permission denied
    │   └── FileNotFoundError
    │   └── PermissionError
    ├── RuntimeError
    │   └── RecursionError
    └── StopIteration        ← Iterator exhausted
```

**Key insight:** Catching `Exception` catches everything in the bottom half.
Catching `BaseException` also catches `KeyboardInterrupt` and `SystemExit` — rarely what you want.

---

## try / except / else / finally

Python's exception handling has four clauses. The `else` is unique to Python.

```python
try:
    # Code that might raise an exception
    result = risky_operation()
except SpecificException as e:
    # Handle the specific exception
    handle_error(e)
except (TypeError, ValueError) as e:
    # Handle multiple exception types
    handle_type_or_value_error(e)
except Exception as e:
    # Catch-all for anything else
    log_unexpected_error(e)
    raise    # Re-raise — don't swallow unknown errors silently
else:
    # Runs ONLY if no exception occurred
    # Use for code that should only run on success
    process_result(result)
finally:
    # ALWAYS runs — exception or not, return or not
    # Use for cleanup
    cleanup()
```

### The `else` Clause — Python's Unique Feature

The `else` clause runs only if the `try` block completed without raising an exception.
This is subtly different from putting the code after the try/except.

```python
# Without else — WRONG approach (hides bugs)
try:
    result = int(user_input)
    process(result)   # If THIS raises ValueError, we catch it accidentally
except ValueError:
    print("Invalid number")

# With else — CORRECT approach
try:
    result = int(user_input)    # Only this can raise the ValueError we care about
except ValueError:
    print("Invalid number")
else:
    process(result)             # We know result is valid here
```

### The `finally` Clause — Always Runs

```python
file = None
try:
    file = open("data.txt", "r")
    data = file.read()
    process(data)
except FileNotFoundError:
    print("File not found")
finally:
    if file:
        file.close()    # ALWAYS closes, even if process() raises an exception
```

**Note:** The `with` statement (context managers) is usually better than `finally` for
resource cleanup. We'll cover that in Module 04.

---

## Catching Specific vs Broad Exceptions

**Be as specific as possible.** Broad catches hide bugs.

```python
# BAD — catches too much, might hide real bugs
try:
    result = compute(data)
except Exception:
    return None    # What if compute() has a bug that raises NameError? Hidden!

# GOOD — catch exactly what you expect
try:
    result = compute(data)
except ValueError:
    return None    # Only catch the specific error we know how to handle
except ZeroDivisionError:
    return float("inf")
```

```python
# BAD — bare except catches EVERYTHING including KeyboardInterrupt
try:
    dangerous()
except:
    pass    # Never do this

# If you must catch broadly:
try:
    dangerous()
except Exception as e:
    logger.error(f"Unexpected error: {e}")
    raise    # Always re-raise if you can't handle it
```

---

## `raise` and `raise from` — Exception Chaining

### Re-raising the Current Exception

```python
try:
    risky()
except ValueError as e:
    log(e)
    raise    # Re-raise the same exception unchanged
```

### Raising a New Exception

```python
def parse_config(filename):
    try:
        with open(filename) as f:
            return json.load(f)
    except FileNotFoundError:
        raise FileNotFoundError(f"Config file not found: {filename}")
    except json.JSONDecodeError as e:
        raise ValueError(f"Invalid JSON in config file: {e}")
```

### `raise from` — Explicit Exception Chaining

When you're converting one exception type to another, use `raise X from Y`
to preserve the original exception context.

```python
class DatabaseError(Exception):
    pass

def get_user(user_id):
    try:
        return db.query(f"SELECT * FROM users WHERE id = {user_id}")
    except ConnectionError as e:
        raise DatabaseError(f"Failed to get user {user_id}") from e
        # The original ConnectionError is preserved as __cause__
```

The traceback will show:
```
ConnectionError: [Errno 111] Connection refused

The above exception was the direct cause of the following exception:

DatabaseError: Failed to get user 42
```

### Suppress Exception Context (`raise X from None`)

Sometimes you're converting an exception and don't want to show the original:

```python
try:
    value = int(text)
except ValueError:
    raise ValueError(f"Expected a number, got: {text!r}") from None
    # Hides the original "invalid literal for int()" message
```

---

## Custom Exceptions

Define custom exceptions as classes that inherit from `Exception`.

```python
# Minimal custom exception
class InsufficientFundsError(Exception):
    pass

# With context
class InsufficientFundsError(Exception):
    def __init__(self, balance: float, amount: float):
        self.balance = balance
        self.amount = amount
        super().__init__(
            f"Cannot withdraw ${amount:.2f}: balance is ${balance:.2f}"
        )

# Exception hierarchy for a domain
class AppError(Exception):
    """Base for all application-specific exceptions."""

class ValidationError(AppError):
    """Input validation failed."""

class NotFoundError(AppError):
    """Requested resource does not exist."""

class AuthError(AppError):
    """Authentication or authorization failed."""


# Usage
def transfer_funds(account, amount):
    if amount <= 0:
        raise ValidationError(f"Amount must be positive, got {amount}")
    if account.balance < amount:
        raise InsufficientFundsError(account.balance, amount)
    account.balance -= amount


# Callers can catch specific or broad
try:
    transfer_funds(account, 500)
except InsufficientFundsError as e:
    print(f"Can't transfer: {e}")
    print(f"You need ${e.amount - e.balance:.2f} more")
except AppError as e:
    # Catch any app error
    print(f"Application error: {e}")
```

---

## Best Practices

### Do: Handle exceptions at the right level

```python
# LOW-LEVEL: raise specific exceptions, don't catch broadly
def read_config(path: str) -> dict:
    try:
        with open(path) as f:
            return json.load(f)
    except FileNotFoundError:
        raise   # Let caller handle it
    except json.JSONDecodeError as e:
        raise ValueError(f"Invalid config: {e}") from e

# HIGH-LEVEL: handle what you can, let the rest propagate
def start_app():
    try:
        config = read_config("app.json")
    except FileNotFoundError:
        print("Config not found, using defaults")
        config = DEFAULT_CONFIG
    except ValueError as e:
        print(f"Config error: {e}")
        sys.exit(1)
```

### Do: Use `else` to avoid hiding bugs

```python
try:
    data = fetch_data()
except NetworkError:
    return cached_data()
else:
    return process(data)    # Only called if fetch succeeded
```

### Don't: Swallow exceptions silently

```python
# BAD
try:
    do_thing()
except:
    pass    # Silent failure is the worst kind

# GOOD
try:
    do_thing()
except SpecificError as e:
    logger.warning(f"do_thing failed: {e}")
    # Then decide: return default, re-raise, or handle
```

### Don't: Use exceptions for flow control

```python
# BAD — using exceptions as if/else
try:
    value = my_dict[key]
except KeyError:
    value = default

# GOOD — use .get()
value = my_dict.get(key, default)
```

---

## Common Python Exceptions Quick Reference

| Exception          | When raised                                   |
|--------------------|-----------------------------------------------|
| `ValueError`       | Right type, wrong value: `int("abc")`         |
| `TypeError`        | Wrong type: `"a" + 1`                         |
| `KeyError`         | Missing dict key: `d["missing"]`              |
| `IndexError`       | Out of range: `lst[100]` on 5-item list       |
| `AttributeError`   | No such attribute: `obj.xyz`                  |
| `FileNotFoundError`| File doesn't exist                            |
| `PermissionError`  | No permission to read/write file              |
| `ZeroDivisionError`| `x / 0`                                       |
| `StopIteration`    | Iterator is exhausted                         |
| `RuntimeError`     | Generic runtime error                         |
| `NotImplementedError`| Abstract method not implemented             |
