# Structural Design Patterns in Python

Structural patterns deal with how objects are composed to form larger structures.
Python's most famous structural feature is its `@decorator` syntax — and it goes much deeper than you might think.

---

## Python's Most Important Structural Pattern: Decorators

Before discussing any GoF structural patterns, we must talk about Python decorators. They are the most common structural pattern in all of Python.

Every Python developer writes decorators. You use them when you see `@property`, `@staticmethod`, `@classmethod`, `@functools.lru_cache`, `@dataclass`, `@app.route`. Understanding how they work — not just how to use them — is essential.

### How Python Decorators Work

A decorator is a function that takes a function and returns a new function:

```python
# decorator_fundamentals.py

# The simplest possible decorator:
def my_decorator(func):
    def wrapper(*args, **kwargs):
        print("Before the function")
        result = func(*args, **kwargs)
        print("After the function")
        return result
    return wrapper

@my_decorator
def greet(name: str) -> str:
    return f"Hello, {name}!"

# @my_decorator is exactly equivalent to:
# greet = my_decorator(greet)

print(greet("Alice"))
# Before the function
# After the function
# (returns) Hello, Alice!
```

### functools.wraps — Essential for Every Decorator

```python
# functools_wraps.py
import functools


def without_wraps(func):
    """BAD: decorator without functools.wraps."""
    def wrapper(*args, **kwargs):
        return func(*args, **kwargs)
    return wrapper


def with_wraps(func):
    """GOOD: decorator with functools.wraps."""
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        return func(*args, **kwargs)
    return wrapper


@without_wraps
def my_function():
    """This docstring should be preserved."""
    pass

@with_wraps
def my_other_function():
    """This docstring IS preserved."""
    pass


print(my_function.__name__)        # "wrapper"      — WRONG
print(my_function.__doc__)         # None           — WRONG

print(my_other_function.__name__)  # "my_other_function"  — correct
print(my_other_function.__doc__)   # "This docstring IS preserved."  — correct
```

`functools.wraps` copies `__name__`, `__doc__`, `__qualname__`, `__annotations__`, and `__module__` from the original function to the wrapper. Always use it.

### Stacking Decorators

Decorators are applied bottom-up (closest to the function first):

```python
# stacking_decorators.py
import functools


def bold(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        return f"<b>{func(*args, **kwargs)}</b>"
    return wrapper


def italic(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        return f"<i>{func(*args, **kwargs)}</i>"
    return wrapper


def underline(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        return f"<u>{func(*args, **kwargs)}</u>"
    return wrapper


@bold
@italic
@underline
def format_text(text: str) -> str:
    return text

# Equivalent to: bold(italic(underline(format_text)))
# Applied: underline first, then italic, then bold

print(format_text("Hello"))
# <b><i><u>Hello</u></i></b>
```

### Parametrized Decorators (Decorator Factories)

When your decorator needs arguments:

```python
# parametrized_decorators.py
import functools
import time
from typing import Callable


def retry(max_attempts: int = 3, delay: float = 1.0, exceptions: tuple = (Exception,)):
    """
    Parametrized decorator: a function that RETURNS a decorator.
    
    Usage: @retry(max_attempts=5, delay=0.5)
    
    The three-layer structure:
    - retry()        — decorator factory (accepts config parameters)
    - decorator()    — the actual decorator (accepts a function)
    - wrapper()      — the wrapped function (accepts the original arguments)
    """
    def decorator(func: Callable) -> Callable:
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            last_exception = None
            for attempt in range(1, max_attempts + 1):
                try:
                    return func(*args, **kwargs)
                except exceptions as e:
                    last_exception = e
                    if attempt < max_attempts:
                        print(f"Attempt {attempt} failed: {e}. Retrying in {delay}s...")
                        time.sleep(delay)
            raise last_exception
        return wrapper
    return decorator


@retry(max_attempts=3, delay=0.5, exceptions=(ConnectionError, TimeoutError))
def fetch_user_data(user_id: int) -> dict:
    """Fetch user data from an API (simulated)."""
    import random
    if random.random() < 0.7:  # 70% chance of failure in this demo
        raise ConnectionError("Network unavailable")
    return {"id": user_id, "name": "Alice"}
```

### Class-Based Decorators

When a decorator needs to maintain state:

```python
# class_based_decorator.py
import functools
import time
from typing import Callable


class Timer:
    """Class-based decorator that times function calls and keeps statistics."""

    def __init__(self, func: Callable):
        functools.update_wrapper(self, func)
        self.func = func
        self.call_count = 0
        self.total_time = 0.0

    def __call__(self, *args, **kwargs):
        start = time.perf_counter()
        result = self.func(*args, **kwargs)
        elapsed = time.perf_counter() - start

        self.call_count += 1
        self.total_time += elapsed
        print(f"[{self.func.__name__}] Call #{self.call_count}: {elapsed:.4f}s")

        return result

    @property
    def average_time(self) -> float:
        if self.call_count == 0:
            return 0.0
        return self.total_time / self.call_count


@Timer
def slow_computation(n: int) -> int:
    import time
    time.sleep(0.01)
    return sum(range(n))


slow_computation(1000)
slow_computation(1000)
slow_computation(1000)

print(f"Average time: {slow_computation.average_time:.4f}s")
print(f"Total calls: {slow_computation.call_count}")
```

---

## 1. Decorator Pattern (GoF)

The Gang of Four Decorator pattern (structural, adds behavior to objects dynamically) is different from Python's `@decorator` syntax, though both share the same concept.

```python
# gof_decorator.py
# Coffee shop example — add features to a coffee object at runtime

from abc import ABC, abstractmethod
from decimal import Decimal


class Coffee(ABC):
    """Abstract component — the thing we're decorating."""

    @abstractmethod
    def cost(self) -> Decimal:
        """Return the cost of this coffee."""
        ...

    @abstractmethod
    def description(self) -> str:
        """Describe this coffee."""
        ...


class SimpleCoffee(Coffee):
    """The base concrete component."""

    def cost(self) -> Decimal:
        return Decimal("1.00")

    def description(self) -> str:
        return "Simple Coffee"


class CoffeeDecorator(Coffee, ABC):
    """Abstract decorator — wraps a Coffee object."""

    def __init__(self, coffee: Coffee):
        self._coffee = coffee

    def cost(self) -> Decimal:
        return self._coffee.cost()

    def description(self) -> str:
        return self._coffee.description()


class Milk(CoffeeDecorator):
    def cost(self) -> Decimal:
        return self._coffee.cost() + Decimal("0.25")

    def description(self) -> str:
        return f"{self._coffee.description()}, Milk"


class Sugar(CoffeeDecorator):
    def cost(self) -> Decimal:
        return self._coffee.cost() + Decimal("0.10")

    def description(self) -> str:
        return f"{self._coffee.description()}, Sugar"


class Vanilla(CoffeeDecorator):
    def cost(self) -> Decimal:
        return self._coffee.cost() + Decimal("0.50")

    def description(self) -> str:
        return f"{self._coffee.description()}, Vanilla"


# Build complex coffees at runtime by composing decorators
coffee = SimpleCoffee()
print(f"{coffee.description()}: ${coffee.cost()}")
# Simple Coffee: $1.00

coffee = Milk(coffee)
print(f"{coffee.description()}: ${coffee.cost()}")
# Simple Coffee, Milk: $1.25

coffee = Vanilla(Sugar(coffee))
print(f"{coffee.description()}: ${coffee.cost()}")
# Simple Coffee, Milk, Sugar, Vanilla: $1.85
```

---

## 2. Adapter Pattern

**Intent:** Convert the interface of a class into another interface that clients expect.

```python
# adapter.py
# Adapt old/external APIs to a new interface

from dataclasses import dataclass
from typing import Protocol


# ─── Target Interface (what our code expects) ──────────────────────────────────

class Logger(Protocol):
    """The interface our application expects for logging."""

    def info(self, message: str) -> None:
        ...

    def error(self, message: str) -> None:
        ...

    def debug(self, message: str) -> None:
        ...


# ─── Incompatible Third-Party Logger ──────────────────────────────────────────

class LegacyLogger:
    """An old logger with a completely different interface."""

    def log_info(self, msg: str, source: str = "app") -> None:
        print(f"[INFO] [{source}] {msg}")

    def log_error(self, msg: str, source: str = "app", code: int = 0) -> None:
        print(f"[ERROR {code}] [{source}] {msg}")

    def log_debug(self, msg: str, source: str = "app") -> None:
        print(f"[DEBUG] [{source}] {msg}")


# ─── Adapter ──────────────────────────────────────────────────────────────────

class LegacyLoggerAdapter:
    """Adapts LegacyLogger to the Logger interface.
    
    Our application code calls info(), error(), debug().
    The adapter translates these calls to log_info(), log_error(), log_debug().
    """

    def __init__(self, legacy_logger: LegacyLogger, source: str = "adapter"):
        self._logger = legacy_logger
        self._source = source

    def info(self, message: str) -> None:
        self._logger.log_info(message, self._source)

    def error(self, message: str) -> None:
        self._logger.log_error(message, self._source, code=1)

    def debug(self, message: str) -> None:
        self._logger.log_debug(message, self._source)


# ─── Client code uses only the Logger protocol ────────────────────────────────

def process_order(order_id: str, logger: Logger) -> bool:
    """Client code — only knows about Logger, not LegacyLogger."""
    logger.info(f"Processing order {order_id}")
    try:
        # Process the order...
        logger.debug(f"Order {order_id} processed successfully")
        return True
    except Exception as e:
        logger.error(f"Failed to process order {order_id}: {e}")
        return False


# Usage:
legacy = LegacyLogger()
adapter = LegacyLoggerAdapter(legacy, source="order-service")

process_order("ORD-12345", adapter)
```

---

## 3. Facade Pattern

**Intent:** Provide a simple interface to a complex subsystem.

```python
# facade.py
# Video conversion subsystem behind a simple interface

from dataclasses import dataclass
from pathlib import Path


# ─── Complex Subsystem ────────────────────────────────────────────────────────

class VideoDecoder:
    """Complex class for decoding video files."""

    def decode(self, file_path: str, codec: str) -> bytes:
        print(f"[VideoDecoder] Decoding {file_path} with codec {codec}")
        return b"decoded_video_data"


class AudioExtractor:
    def extract(self, video_data: bytes, sample_rate: int = 44100) -> bytes:
        print(f"[AudioExtractor] Extracting audio at {sample_rate}Hz")
        return b"audio_data"


class AudioEncoder:
    def encode(self, audio_data: bytes, format: str = "aac") -> bytes:
        print(f"[AudioEncoder] Encoding audio to {format}")
        return b"encoded_audio"


class VideoEncoder:
    def encode(
        self,
        video_data: bytes,
        audio_data: bytes,
        resolution: tuple[int, int],
        codec: str,
    ) -> bytes:
        print(f"[VideoEncoder] Encoding {resolution[0]}x{resolution[1]} {codec} video")
        return b"encoded_video"


class FileSaver:
    def save(self, data: bytes, output_path: str) -> None:
        print(f"[FileSaver] Saving {len(data)} bytes to {output_path}")
        Path(output_path).write_bytes(data)


# ─── Facade ───────────────────────────────────────────────────────────────────

@dataclass
class VideoConversionResult:
    success: bool
    output_path: str
    message: str


class VideoConverterFacade:
    """Simple interface hiding the complex subsystem.
    
    Client code calls convert() — one method.
    The facade handles all the complexity internally.
    """

    def __init__(self):
        self._decoder = VideoDecoder()
        self._audio_extractor = AudioExtractor()
        self._audio_encoder = AudioEncoder()
        self._video_encoder = VideoEncoder()
        self._saver = FileSaver()

    def convert(
        self,
        input_path: str,
        output_path: str,
        format: str = "mp4",
    ) -> VideoConversionResult:
        """Convert a video file to the specified format.
        
        This is the only method clients need to call.
        """
        try:
            print(f"[Facade] Converting {input_path} to {format}...")

            # Coordinate the complex subsystem
            video_data = self._decoder.decode(input_path, "h264")
            audio_data = self._audio_extractor.extract(video_data)
            encoded_audio = self._audio_encoder.encode(audio_data, "aac")
            encoded_video = self._video_encoder.encode(
                video_data, encoded_audio, (1920, 1080), "h264"
            )
            self._saver.save(encoded_video, output_path)

            return VideoConversionResult(True, output_path, "Conversion complete")
        except Exception as e:
            return VideoConversionResult(False, "", str(e))


# Client code — simple!
converter = VideoConverterFacade()
result = converter.convert("input.avi", "output.mp4", "mp4")
print(result.message)
```

---

## 4. Proxy Pattern

**Intent:** Provide a surrogate or placeholder for another object to control access.

```python
# proxy.py
# Lazy-loading proxy and access control proxy

from typing import Protocol


class DataService(Protocol):
    def get_data(self, key: str) -> dict:
        ...


class RealDataService:
    """Expensive service — takes time to initialize."""

    def __init__(self):
        print("[RealDataService] Connecting to database... (slow)")
        import time
        time.sleep(0.1)  # Simulate slow initialization
        self._data = {
            "user:1": {"name": "Alice", "role": "admin"},
            "user:2": {"name": "Bob", "role": "user"},
        }

    def get_data(self, key: str) -> dict:
        return self._data.get(key, {})


class LazyProxy:
    """Lazy-loading proxy: defers expensive initialization until first use."""

    def __init__(self):
        self._service: RealDataService | None = None

    def _ensure_initialized(self) -> None:
        if self._service is None:
            print("[LazyProxy] First access — initializing real service")
            self._service = RealDataService()

    def get_data(self, key: str) -> dict:
        self._ensure_initialized()
        return self._service.get_data(key)


class AccessControlProxy:
    """Security proxy: controls access based on user role."""

    def __init__(self, real_service: RealDataService, current_user_role: str):
        self._service = real_service
        self._role = current_user_role

    def get_data(self, key: str) -> dict:
        # Non-admins can only access their own data
        if self._role != "admin" and not key.startswith(f"user:"):
            raise PermissionError(f"Access denied: role '{self._role}' cannot access '{key}'")

        return self._service.get_data(key)


# Usage:
print("=== Lazy Proxy ===")
proxy = LazyProxy()
print("Proxy created — no initialization yet")
data = proxy.get_data("user:1")  # Initializes now
print(f"Got: {data}")
data = proxy.get_data("user:2")  # Already initialized
print(f"Got: {data}")

print("\n=== Access Control Proxy ===")
real_service = RealDataService()
admin_proxy = AccessControlProxy(real_service, "admin")
user_proxy = AccessControlProxy(real_service, "user")

print(admin_proxy.get_data("user:1"))  # Works for admin
print(user_proxy.get_data("user:2"))   # Works for own data

try:
    user_proxy.get_data("system:config")
except PermissionError as e:
    print(f"Blocked: {e}")
```

---

## 5. Composite Pattern

**Intent:** Compose objects into tree structures to represent part-whole hierarchies. Let clients treat individual objects and compositions uniformly.

```python
# composite.py
# File system representation

from abc import ABC, abstractmethod


class FileSystemItem(ABC):
    """Component — both files and directories implement this."""

    def __init__(self, name: str):
        self.name = name

    @abstractmethod
    def size(self) -> int:
        """Return size in bytes."""
        ...

    @abstractmethod
    def display(self, indent: int = 0) -> None:
        """Display this item with indentation."""
        ...


class File(FileSystemItem):
    """Leaf — has no children."""

    def __init__(self, name: str, size_bytes: int):
        super().__init__(name)
        self._size = size_bytes

    def size(self) -> int:
        return self._size

    def display(self, indent: int = 0) -> None:
        print(f"{'  ' * indent}📄 {self.name} ({self._size} bytes)")


class Directory(FileSystemItem):
    """Composite — can contain files and other directories."""

    def __init__(self, name: str):
        super().__init__(name)
        self._children: list[FileSystemItem] = []

    def add(self, item: FileSystemItem) -> "Directory":
        self._children.append(item)
        return self  # Fluent interface

    def remove(self, item: FileSystemItem) -> None:
        self._children.remove(item)

    def size(self) -> int:
        """Total size includes all children recursively."""
        return sum(child.size() for child in self._children)

    def display(self, indent: int = 0) -> None:
        print(f"{'  ' * indent}📁 {self.name}/ ({self.size()} bytes)")
        for child in self._children:
            child.display(indent + 1)


# Build a file system tree:
root = Directory("project")
src = Directory("src")
tests = Directory("tests")

src.add(File("main.py", 1024))
src.add(File("utils.py", 512))
src.add(File("models.py", 2048))

tests.add(File("test_main.py", 800))
tests.add(File("test_utils.py", 400))

root.add(src).add(tests).add(File("README.md", 256))

root.display()
# 📁 project/ (5044 bytes)
#   📁 src/ (3584 bytes)
#     📄 main.py (1024 bytes)
#     📄 utils.py (512 bytes)
#     📄 models.py (2048 bytes)
#   📁 tests/ (1200 bytes)
#     📄 test_main.py (800 bytes)
#     📄 test_utils.py (400 bytes)
#   📄 README.md (256 bytes)

print(f"\nTotal size: {root.size()} bytes")
```

---

## Structural Patterns Summary

| Pattern | Intent | Python Example |
|---------|--------|---------------|
| Decorator (Python) | Add behavior to functions | `@functools.lru_cache`, `@property` |
| Decorator (GoF) | Add behavior to objects | Coffee add-ons wrapping base coffee |
| Adapter | Make incompatible interfaces compatible | Wrap legacy code in a new interface |
| Facade | Simplify complex subsystems | One `convert()` method over 5 complex classes |
| Proxy | Control access to an object | Lazy loading, access control, caching |
| Composite | Tree structure with uniform interface | File system, GUI components, org charts |

The most Python-specific insight of this module: **Python's `@decorator` syntax is the most-used structural pattern in the entire language.** Learn it deeply.
