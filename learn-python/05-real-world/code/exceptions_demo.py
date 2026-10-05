"""
exceptions_demo.py — Exception handling patterns in Python

Topics covered:
  - try/except/else/finally
  - Exception hierarchy
  - Custom exceptions
  - Exception chaining (raise from)
  - Best practices
"""

import json
from pathlib import Path

# =============================================================================
# SECTION 1: Basic Exception Handling
# =============================================================================

print("=" * 60)
print("SECTION 1: Basic Exception Handling")
print("=" * 60)

# The four clauses
def divide_safely(a, b):
    try:
        result = a / b
    except ZeroDivisionError:
        print("  except: caught ZeroDivisionError")
        return None
    except TypeError as e:
        print(f"  except: caught TypeError — {e}")
        return None
    else:
        print("  else: no exception occurred")
        return result
    finally:
        print("  finally: always runs")

print("\ndivide_safely(10, 2):")
print(f"  result = {divide_safely(10, 2)}")

print("\ndivide_safely(10, 0):")
print(f"  result = {divide_safely(10, 0)}")

print("\ndivide_safely(10, 'a'):")
print(f"  result = {divide_safely(10, 'a')}")

# =============================================================================
# SECTION 2: The Importance of the `else` Clause
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 2: The else Clause")
print("=" * 60)

def parse_number_wrong(text):
    """Bug: catches ValueError from process_number() too!"""
    try:
        num = int(text)
        # BUG: if this raises ValueError, we catch it accidentally
        # and misattribute it to the int() conversion
        result = num / 0  # Raises ZeroDivisionError but imagine a ValueError here
        return result
    except ValueError:
        return "Invalid number"

def parse_number_right(text):
    """Correct: only the int() conversion is in the try block."""
    try:
        num = int(text)
    except ValueError:
        return "Invalid number"
    else:
        # We know 'num' is valid here — safe to process
        return num * 2

print(f"parse_number_right('42'):   {parse_number_right('42')}")
print(f"parse_number_right('abc'):  {parse_number_right('abc')}")

# =============================================================================
# SECTION 3: Exception Chaining with `raise from`
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 3: Exception Chaining")
print("=" * 60)

class ConfigError(Exception):
    pass

def load_config(path: str) -> dict:
    """Demonstrates raise from to chain exceptions."""
    try:
        with open(path) as f:
            return json.load(f)
    except FileNotFoundError as e:
        raise ConfigError(f"Config not found: {path}") from e
    except json.JSONDecodeError as e:
        raise ConfigError(f"Config has invalid JSON: {e}") from e

# Try loading a nonexistent file
try:
    config = load_config("/nonexistent/path/config.json")
except ConfigError as e:
    print(f"ConfigError caught: {e}")
    print(f"Caused by: {e.__cause__}")

# =============================================================================
# SECTION 4: Custom Exception Hierarchy
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 4: Custom Exceptions")
print("=" * 60)

# Define a hierarchy
class AppError(Exception):
    """Base exception for all app-specific errors."""

class ValidationError(AppError):
    """Input validation failed."""
    def __init__(self, field: str, message: str):
        self.field = field
        self.message = message
        super().__init__(f"Validation error on '{field}': {message}")

class NotFoundError(AppError):
    """Requested resource not found."""
    def __init__(self, resource_type: str, identifier):
        self.resource_type = resource_type
        self.identifier = identifier
        super().__init__(f"{resource_type} not found: {identifier!r}")

class InsufficientFundsError(AppError):
    """Not enough balance for the requested operation."""
    def __init__(self, balance: float, amount: float):
        self.balance = balance
        self.amount = amount
        self.shortfall = amount - balance
        super().__init__(
            f"Cannot withdraw ${amount:.2f}: balance is ${balance:.2f} "
            f"(need ${self.shortfall:.2f} more)"
        )

# Usage
class BankAccount:
    def __init__(self, owner: str, balance: float = 0.0):
        if not owner.strip():
            raise ValidationError("owner", "Owner name cannot be empty")
        if balance < 0:
            raise ValidationError("balance", "Initial balance cannot be negative")
        self.owner = owner
        self.balance = balance

    def withdraw(self, amount: float) -> None:
        if amount <= 0:
            raise ValidationError("amount", "Amount must be positive")
        if amount > self.balance:
            raise InsufficientFundsError(self.balance, amount)
        self.balance -= amount


def test_custom_exceptions():
    # Successful operation
    account = BankAccount("Alice", 100.0)
    account.withdraw(30.0)
    print(f"After withdrawal: ${account.balance:.2f}")

    # Catch specific exception
    try:
        account.withdraw(200.0)
    except InsufficientFundsError as e:
        print(f"Can't withdraw: {e}")
        print(f"  Shortfall: ${e.shortfall:.2f}")

    # Catch base class
    try:
        BankAccount("", 50.0)
    except AppError as e:
        print(f"App error: {e}")

    # Validation error with field info
    try:
        account.withdraw(-5.0)
    except ValidationError as e:
        print(f"Validation failed on field '{e.field}': {e.message}")


test_custom_exceptions()

# =============================================================================
# SECTION 5: Anti-Patterns
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 5: Anti-Patterns to Avoid")
print("=" * 60)

# ANTI-PATTERN 1: Bare except
def bad_1():
    try:
        result = 1 / 0
    except:
        pass   # Swallows everything including KeyboardInterrupt!

# ANTI-PATTERN 2: Catching Exception too broadly
def bad_2(x):
    try:
        return int(x)   # But also: connect_to_db(), send_email(), etc.
    except Exception:
        return None      # Hides ALL errors, not just TypeError/ValueError

# ANTI-PATTERN 3: Using exceptions as flow control
def bad_3(d, key):
    try:
        return d[key]
    except KeyError:
        return None

# GOOD: use .get() instead
def good_3(d, key):
    return d.get(key)

# ANTI-PATTERN 4: Losing the original traceback
def bad_4():
    try:
        risky()
    except Exception as e:
        raise Exception("Something went wrong")  # Lost original traceback!

# GOOD: preserve the chain
def good_4():
    try:
        risky()
    except Exception as e:
        raise RuntimeError("Something went wrong") from e  # Preserves original

def risky():
    raise ValueError("Original error")

print("Anti-pattern 4 demonstration:")
try:
    good_4()
except RuntimeError as e:
    print(f"RuntimeError: {e}")
    print(f"Caused by: {e.__cause__}")

# =============================================================================
# SECTION 6: Context Manager for Cleanup
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 6: finally for Cleanup (and why 'with' is better)")
print("=" * 60)

def process_file_manual(path):
    """Manual cleanup with finally — works but verbose."""
    f = None
    try:
        f = open(path, "r")
        return f.read()
    except FileNotFoundError:
        return None
    finally:
        if f:
            f.close()
            print("  File closed (via finally)")

def process_file_with(path):
    """Modern approach — context manager handles cleanup."""
    try:
        with open(path, "r") as f:
            return f.read()
    except FileNotFoundError:
        return None
    # File is ALWAYS closed here — even if something goes wrong in the with block

# Both work — prefer process_file_with
import tempfile, os
with tempfile.NamedTemporaryFile(mode='w', suffix='.txt', delete=False) as f:
    f.write("hello")
    tmppath = f.name

print(f"Manual approach: '{process_file_manual(tmppath)}'")
print(f"With approach:   '{process_file_with(tmppath)}'")
os.unlink(tmppath)
