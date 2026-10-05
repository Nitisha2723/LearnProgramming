# Context Managers

Context managers ensure that resources are properly set up and cleaned up,
even when exceptions occur. The `with` statement is Python's syntax for using them.

---

## The `with` Statement

You've already used context managers for file handling:

```python
with open("data.txt") as f:
    content = f.read()
# File is automatically closed here, even if read() raised an exception
```

The `with` statement:
1. Calls `f.__enter__()` — sets up the resource
2. Runs the block of code
3. Calls `f.__exit__(...)` — cleans up, regardless of exceptions

Without `with`, you'd need `try/finally`:

```python
f = open("data.txt")
try:
    content = f.read()
finally:
    f.close()   # Must do this manually
```

The `with` version is cleaner and less error-prone.

---

## How Context Managers Work: `__enter__` and `__exit__`

A context manager is any object with `__enter__` and `__exit__` methods.

```python
import time  # <- import at module level, not inside the class

class Timer:
    """Context manager that measures elapsed time."""

    def __enter__(self):
        self.start = time.perf_counter()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.elapsed = time.perf_counter() - self.start
        print(f"Elapsed: {self.elapsed:.4f}s")
        return False  # Don't suppress exceptions


with Timer() as t:
    result = some_computation()

# Prints: Elapsed: 0.1234s
```

### The `__exit__` Method Parameters

```python
def __exit__(self, exc_type, exc_val, exc_tb):
    # exc_type: the exception class (or None if no exception)
    # exc_val:  the exception instance (or None)
    # exc_tb:   the traceback (or None)

    if exc_type is None:
        # No exception — normal exit
        pass

    # Return True to suppress the exception
    # Return False (or None) to let it propagate
    return False
```

---

## Built-in Context Managers

### File handling

```python
with open("data.txt") as f:
    content = f.read()
```

### Multiple context managers in one `with`

```python
with open("input.txt") as inp, open("output.txt", "w") as out:
    for line in inp:
        out.write(line.upper())
```

### `threading.Lock`

```python
import threading

lock = threading.Lock()

with lock:
    # Only one thread can be here at a time
    shared_resource.modify()
```

### `decimal.localcontext`

```python
from decimal import Decimal, localcontext

with localcontext() as ctx:
    ctx.prec = 50    # Set precision to 50 digits for this block
    result = Decimal("1") / Decimal("3")
# Precision is restored after the block
```

### `contextlib.suppress` — Ignore specific exceptions

```python
from contextlib import suppress

with suppress(FileNotFoundError):
    os.remove("temp_file.txt")    # No error if file doesn't exist
# Equivalent to: try/except FileNotFoundError: pass
```

### `contextlib.redirect_stdout`

```python
from contextlib import redirect_stdout
import io

# Capture stdout in tests or to redirect output to a file
f = io.StringIO()
with redirect_stdout(f):
    print("This goes to the StringIO, not the terminal")

output = f.getvalue()
```

---

## Writing Custom Context Managers with a Class

```python
import time

class DatabaseConnection:
    def __init__(self, host, port):
        self.host = host
        self.port = port
        self._connection = None

    def __enter__(self):
        print(f"Connecting to {self.host}:{self.port}")
        self._connection = self._create_connection()
        return self._connection    # Returned to 'as' variable

    def __exit__(self, exc_type, exc_val, exc_tb):
        if self._connection:
            if exc_type is not None:
                print(f"Rolling back due to: {exc_val}")
                self._connection.rollback()
            else:
                self._connection.commit()
            self._connection.close()
            print("Connection closed")
        return False   # Don't suppress exceptions

    def _create_connection(self):
        # Would create a real DB connection
        return MockConnection()


with DatabaseConnection("localhost", 5432) as conn:
    conn.execute("INSERT INTO users VALUES (?)", ("Alice",))
    conn.execute("UPDATE counters SET value = value + 1")
# Automatically committed and closed
```

---

## `contextlib.contextmanager` — The Decorator Approach

For simpler context managers, the `contextmanager` decorator lets you write
them as generator functions. The `yield` separates setup from teardown.

```python
from contextlib import contextmanager

@contextmanager
def timer(name=""):
    import time
    start = time.perf_counter()
    try:
        yield               # <- The 'with' block runs here
    finally:
        elapsed = time.perf_counter() - start
        print(f"{name}: {elapsed:.4f}s")

with timer("computation"):
    result = heavy_computation()
```

The pattern is:
1. Code before `yield` = `__enter__`
2. `yield` value = what gets assigned to `as` variable (optional)
3. Code after `yield` = `__exit__`
4. Use `try/finally` to ensure cleanup runs even on exception

### Example: Temporary Working Directory

```python
from contextlib import contextmanager
import os

@contextmanager
def working_directory(path):
    """Temporarily change the working directory."""
    original = os.getcwd()
    try:
        os.chdir(path)
        yield path
    finally:
        os.chdir(original)

with working_directory("/tmp"):
    # All relative paths here are under /tmp
    with open("temp_file.txt", "w") as f:
        f.write("temporary")
# Back to original directory
```

### Example: Managed Temp File

```python
from contextlib import contextmanager
from pathlib import Path
import tempfile

@contextmanager
def temp_file(suffix=".txt", **kwargs):
    """Create a temp file that's automatically deleted on exit."""
    with tempfile.NamedTemporaryFile(suffix=suffix, delete=False, **kwargs) as f:
        path = Path(f.name)
    try:
        yield path
    finally:
        if path.exists():
            path.unlink()

with temp_file(suffix=".json") as path:
    path.write_text('{"key": "value"}')
    data = json.loads(path.read_text())
# File is automatically deleted here
```

---

## Suppressing Exceptions in Context Managers

Usually you want exceptions to propagate. But sometimes it's appropriate to suppress them:

```python
class IgnorePermissionErrors:
    """Context manager that suppresses PermissionError."""

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type is PermissionError:
            print(f"Permission denied — skipping")
            return True    # Suppress the exception!
        return False       # Let everything else propagate


with IgnorePermissionErrors():
    sensitive_file.delete()   # Silently skips if no permission
```

---

## Why Context Managers Prevent Resource Leaks

Without context managers, resource leaks happen when:

```python
# LEAK: If process(data) raises an exception, connection never closes!
conn = get_connection()
data = conn.read()
process(data)       # Exception here!
conn.close()        # Never reached

# SAFE: with always closes the connection
with get_connection() as conn:
    data = conn.read()
    process(data)   # Exception here is fine — __exit__ still called
```

Common resources that benefit from context managers:
- File handles
- Database connections
- Network connections
- Locks (threading, multiprocessing)
- Temporary files and directories
- Decimal precision context
- Mock patches in tests

---

## Nesting and Stacking Context Managers

```python
# Nested with statements
with A() as a:
    with B() as b:
        ...

# Equivalent single with (Python 3.1+)
with A() as a, B() as b:
    ...

# ExitStack — variable number of context managers
from contextlib import ExitStack

files_to_process = [Path("a.txt"), Path("b.txt"), Path("c.txt")]

with ExitStack() as stack:
    file_handles = [
        stack.enter_context(open(f)) for f in files_to_process
    ]
    # All files open here; all automatically closed when with exits
    for f in file_handles:
        process(f.read())
```
