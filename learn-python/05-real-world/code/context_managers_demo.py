"""
context_managers_demo.py — Custom context managers

Topics covered:
  - The with statement and how it works
  - Writing a context manager as a class (__enter__/__exit__)
  - Writing a context manager with @contextmanager decorator
  - Built-in context managers: suppress, redirect_stdout, ExitStack
  - Practical examples: Timer, DatabaseTransaction, TempDirectory
"""

import time
import os
import json
import tempfile
import shutil
from contextlib import contextmanager, suppress, redirect_stdout, ExitStack
from pathlib import Path
import io

# =============================================================================
# SECTION 1: How the with Statement Works
# =============================================================================

print("=" * 60)
print("SECTION 1: How 'with' Works")
print("=" * 60)

print("\nwith open(...) deconstructed:")
print("  1. __enter__() is called — opens the file")
print("  2. Your block runs")
print("  3. __exit__() is called — closes the file")
print("  (happens even if your block raises an exception)")

# =============================================================================
# SECTION 2: Context Manager as a Class
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 2: Context Manager Class")
print("=" * 60)

class Timer:
    """Context manager that times how long a block takes."""

    def __init__(self, name: str = ""):
        self.name = name
        self.elapsed = 0.0

    def __enter__(self):
        self._start = time.perf_counter()
        return self    # Returned as the 'as' variable

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.elapsed = time.perf_counter() - self._start
        label = f"[{self.name}] " if self.name else ""
        print(f"  {label}Elapsed: {self.elapsed:.4f}s")
        return False   # Don't suppress exceptions

# Basic usage
with Timer("sorting"):
    data = list(range(100_000, 0, -1))
    data.sort()

# Access elapsed time after the block
with Timer("computation") as t:
    total = sum(x**2 for x in range(100_000))
print(f"  Total was {total:,}, took {t.elapsed:.4f}s")

# Exception handling
print("\nTimer with exception:")
try:
    with Timer("failing_block"):
        raise ValueError("Something went wrong")
except ValueError:
    print("  Exception was propagated (not suppressed)")


class IndentLogger:
    """Context manager that indents log output within a block."""

    _indent = 0

    def __init__(self, label: str):
        self.label = label

    def __enter__(self):
        print("  " * self.__class__._indent + f"[{self.label}] START")
        self.__class__._indent += 1
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.__class__._indent -= 1
        print("  " * self.__class__._indent + f"[{self.label}] END")
        return False

print("\nIndented logging:")
with IndentLogger("outer"):
    print("  " * 1 + "Outer block code")
    with IndentLogger("inner"):
        print("  " * 2 + "Inner block code")
    print("  " * 1 + "Back in outer")


class SuppressAndLog:
    """Context manager that suppresses an exception but logs it."""

    def __init__(self, *exception_types):
        self.exception_types = exception_types
        self.exception = None

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type and issubclass(exc_type, self.exception_types):
            self.exception = exc_val
            print(f"  Suppressed: {exc_type.__name__}: {exc_val}")
            return True    # Suppress!
        return False

print("\nSuppressAndLog:")
with SuppressAndLog(FileNotFoundError, PermissionError) as handler:
    raise FileNotFoundError("test_file.txt")
print(f"  Caught exception: {handler.exception}")

# =============================================================================
# SECTION 3: @contextmanager Decorator
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 3: @contextmanager Decorator")
print("=" * 60)

@contextmanager
def timer(name: str = ""):
    """Simple timer using the decorator approach."""
    start = time.perf_counter()
    try:
        yield   # The 'with' block runs here
    finally:
        elapsed = time.perf_counter() - start
        label = f"[{name}] " if name else ""
        print(f"  {label}Elapsed: {elapsed:.4f}s")


@contextmanager
def temp_directory():
    """Create a temporary directory, yield it, then delete it."""
    tmpdir = Path(tempfile.mkdtemp())
    try:
        print(f"  Created temp dir: {tmpdir.name}")
        yield tmpdir
    finally:
        shutil.rmtree(tmpdir)
        print(f"  Deleted temp dir: {tmpdir.name}")


@contextmanager
def database_transaction(db_name: str):
    """
    Simulate a database transaction.
    Commits on success, rolls back on exception.
    """
    print(f"  [{db_name}] BEGIN TRANSACTION")
    operations = []
    try:
        yield operations    # Caller appends operations to this list
        print(f"  [{db_name}] COMMIT ({len(operations)} operations)")
    except Exception as e:
        print(f"  [{db_name}] ROLLBACK due to: {e}")
        raise   # Re-raise after rollback


@contextmanager
def managed_open(filepath, mode="r", **kwargs):
    """
    Context manager for file opening that handles common errors gracefully.
    """
    try:
        f = open(filepath, mode, **kwargs)
        try:
            yield f
        finally:
            f.close()
    except FileNotFoundError:
        raise FileNotFoundError(f"File not found: {filepath}")
    except PermissionError:
        raise PermissionError(f"No permission to access: {filepath}")


# Demo: temp_directory
with temp_directory() as tmpdir:
    (tmpdir / "output.txt").write_text("Hello from temp dir!")
    files = list(tmpdir.iterdir())
    print(f"  Files in temp dir: {[f.name for f in files]}")

print()

# Demo: database_transaction — success
with database_transaction("users_db") as ops:
    ops.append("INSERT users VALUES (...)")
    ops.append("UPDATE stats SET count = count + 1")

print()

# Demo: database_transaction — failure (rollback)
try:
    with database_transaction("orders_db") as ops:
        ops.append("INSERT orders VALUES (...)")
        raise ValueError("Payment verification failed")
except ValueError:
    pass

print()

# Demo: timer
with timer("matrix_sum"):
    total = sum(i * j for i in range(1000) for j in range(1000))

# =============================================================================
# SECTION 4: Built-in contextlib Utilities
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 4: contextlib Utilities")
print("=" * 60)

# contextlib.suppress — ignore specific exceptions
print("suppress(FileNotFoundError):")
with suppress(FileNotFoundError):
    os.remove("/nonexistent/file.txt")
print("  No error raised — file.txt doesn't exist but we don't care")

# redirect_stdout — capture or redirect output
print("\nredirect_stdout:")
captured = io.StringIO()
with redirect_stdout(captured):
    print("This line goes to StringIO, not the terminal")
    print("So does this one")
output = captured.getvalue()
print(f"  Captured: {output.strip()!r}")

# ExitStack — variable number of context managers
print("\nExitStack:")
files_to_read = ["a.txt", "b.txt", "c.txt"]

with tempfile.TemporaryDirectory() as tmpdir:
    # Create test files
    for name in files_to_read:
        Path(tmpdir, name).write_text(f"Content of {name}")

    # Open all files with ExitStack
    with ExitStack() as stack:
        handles = [
            stack.enter_context(open(Path(tmpdir) / name))
            for name in files_to_read
        ]
        contents = [f.read() for f in handles]
        print(f"  Read {len(contents)} files: {contents}")
    # All files automatically closed here

# =============================================================================
# SECTION 5: Practical Patterns
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 5: Practical Patterns")
print("=" * 60)

# Pattern: Benchmark multiple approaches
def approach_1(n):
    return [x**2 for x in range(n)]

def approach_2(n):
    result = []
    for x in range(n):
        result.append(x**2)
    return result

print("Benchmarking two approaches:")
with timer("list comprehension"):
    approach_1(100_000)

with timer("manual append"):
    approach_2(100_000)

# Pattern: JSON processing with error handling
@contextmanager
def json_file(path, mode="r", **kwargs):
    """Context manager for JSON files with automatic parse/serialize."""
    if mode == "r":
        try:
            with open(path, "r", **kwargs) as f:
                data = json.load(f)
            yield data
        except FileNotFoundError:
            yield None
        except json.JSONDecodeError as e:
            raise ValueError(f"Invalid JSON in {path}: {e}") from e
    elif mode == "w":
        data = {}
        yield data
        with open(path, "w", **kwargs) as f:
            json.dump(data, f, indent=2)


with tempfile.TemporaryDirectory() as tmpdir:
    config_path = Path(tmpdir) / "config.json"

    # Write via context manager
    with json_file(config_path, "w") as cfg:
        cfg["host"] = "localhost"
        cfg["port"] = 8080
        cfg["debug"] = True

    # Read via context manager
    with json_file(config_path) as cfg:
        print(f"\nJSON config: {cfg}")

    # Non-existent file returns None
    with json_file(Path(tmpdir) / "missing.json") as cfg:
        print(f"Missing file: {cfg}")   # None

print("\nAll context manager demos complete!")
