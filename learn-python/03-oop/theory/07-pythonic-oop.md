# Pythonic OOP — Modern Python Patterns

## Dataclasses: Modern Class Creation

`@dataclass` (Python 3.7+) automatically generates `__init__`, `__repr__`, `__eq__`,
and optionally ordering and hashing. It's the modern way to write data-focused classes.

```python
from dataclasses import dataclass, field, KW_ONLY
from typing import List, ClassVar

@dataclass
class Point:
    x: float
    y: float

# Python auto-generates:
# __init__(self, x: float, y: float)
# __repr__(self) -> "Point(x=1.0, y=2.0)"
# __eq__(self, other) -> checks x and y

p1 = Point(1.0, 2.0)
p2 = Point(1.0, 2.0)
p3 = Point(3.0, 4.0)

print(p1)         # Point(x=1.0, y=2.0)
print(p1 == p2)   # True
print(p1 == p3)   # False
```

### Dataclass options

```python
from dataclasses import dataclass

# order=True: generates __lt__, __le__, __gt__, __ge__
# frozen=True: makes instances immutable (also makes them hashable)
# eq=True: generates __eq__ (default True)
@dataclass(order=True, frozen=True)
class Version:
    major: int
    minor: int
    patch: int = 0
    
    def __str__(self) -> str:
        return f"{self.major}.{self.minor}.{self.patch}"

v1 = Version(1, 0, 0)
v2 = Version(1, 2, 0)
v3 = Version(2, 0, 0)

versions = [v3, v1, v2]
print(sorted(versions))    # [Version(1,0,0), Version(1,2,0), Version(2,0,0)]
print(max(versions))       # Version(major=2, minor=0, patch=0)

# Frozen instances are hashable — can be dict keys
version_notes = {v1: "Initial release", v2: "Added features"}
```

### Dataclass `field()` for complex defaults

```python
from dataclasses import dataclass, field
from typing import List
from datetime import datetime

@dataclass
class Order:
    customer_name: str
    
    # Cannot use [] as default directly — use field(default_factory=list)
    items: List[str] = field(default_factory=list)
    
    # Created automatically when order is instantiated
    created_at: datetime = field(default_factory=datetime.now)
    
    # This field is not included in __repr__
    _internal_id: int = field(default=0, repr=False)
    
    # This field is excluded from __init__ (set manually or in __post_init__)
    total_items: int = field(init=False)
    
    def __post_init__(self):
        """Called after __init__ — perfect for computed fields and validation."""
        self.total_items = len(self.items)
        if not self.customer_name.strip():
            raise ValueError("Customer name cannot be empty")
    
    def add_item(self, item: str) -> None:
        self.items.append(item)
        self.total_items += 1


order = Order("Alice", ["laptop", "mouse"])
print(order)
# Order(customer_name='Alice', items=['laptop', 'mouse'], created_at=datetime(...), total_items=2)
```

### Inheritance with dataclasses

```python
from dataclasses import dataclass

@dataclass
class Animal:
    name: str
    sound: str
    
    def speak(self) -> str:
        return f"{self.name} says {self.sound}"

@dataclass
class Dog(Animal):
    breed: str
    
    def fetch(self) -> str:
        return f"{self.name} fetches!"

# Dog.__init__ takes: name, sound, breed
rex = Dog("Rex", "Woof", "Labrador")
print(rex)        # Dog(name='Rex', sound='Woof', breed='Labrador')
print(rex.speak())  # Rex says Woof
```

---

## NamedTuples — Immutable Record Types

`NamedTuple` creates immutable, lightweight objects that behave like tuples but with
named fields. Great for return values and data transfer objects.

```python
from typing import NamedTuple

class Point(NamedTuple):
    x: float
    y: float
    z: float = 0.0   # Default value

class Color(NamedTuple):
    red: int
    green: int
    blue: int
    alpha: int = 255

p = Point(1.0, 2.0)
c = Color(255, 0, 0)

print(p)         # Point(x=1.0, y=2.0, z=0.0)
print(p.x)       # 1.0 — named access
print(p[0])      # 1.0 — index access (tuple behavior)

# Immutable — like a frozen dataclass
# p.x = 5.0    # AttributeError: can't set attribute

# Works with tuple unpacking
x, y, z = p
print(x, y, z)  # 1.0 2.0 0.0

# Hashable — can be dict keys
locations = {Point(0, 0): "origin", Point(1, 1): "corner"}

# Use NamedTuple for functions that return multiple values:
def minmax(numbers: list) -> tuple:
    return min(numbers), max(numbers)

# vs. more readable with NamedTuple:
class MinMaxResult(NamedTuple):
    minimum: float
    maximum: float

def minmax_named(numbers: list) -> MinMaxResult:
    return MinMaxResult(minimum=min(numbers), maximum=max(numbers))

result = minmax_named([3, 1, 4, 1, 5, 9, 2, 6])
print(result.minimum)   # 1
print(result.maximum)   # 9
```

---

## `__slots__` vs Regular Attributes

As covered in the encapsulation section, `__slots__` eliminates `__dict__` for memory savings.

```python
import sys

class RegularPoint:
    def __init__(self, x: float, y: float, z: float):
        self.x = x
        self.y = y
        self.z = z

class SlottedPoint:
    __slots__ = ('x', 'y', 'z')
    
    def __init__(self, x: float, y: float, z: float):
        self.x = x
        self.y = y
        self.z = z

r = RegularPoint(1.0, 2.0, 3.0)
s = SlottedPoint(1.0, 2.0, 3.0)

# Check memory
print(f"Regular: {sys.getsizeof(r)} + {sys.getsizeof(r.__dict__)} bytes")
print(f"Slotted: {sys.getsizeof(s)} bytes (no __dict__)")

# With __slots__, no dynamic attribute creation:
r.w = 4.0   # Fine for regular class
# s.w = 4.0  # AttributeError for slotted class

# __slots__ with inheritance — parent must also use __slots__:
class Point3D(SlottedPoint):
    __slots__ = ('color',)  # Only add NEW slots
    
    def __init__(self, x, y, z, color):
        super().__init__(x, y, z)
        self.color = color
```

### Slots with dataclasses

```python
# Python 3.10+ supports __slots__ directly in @dataclass
from dataclasses import dataclass

@dataclass(slots=True)
class EfficientPoint:
    x: float
    y: float
    z: float = 0.0

# No need to manually define __slots__!
ep = EfficientPoint(1.0, 2.0)
print(ep)         # EfficientPoint(x=1.0, y=2.0, z=0.0)
# ep.w = 4.0    # AttributeError — slots enforced
```

---

## Context Managers: The `with` Statement

Context managers handle setup and teardown automatically. They're Python's answer to
try-finally blocks, file handles, database connections, locks, and more.

```python
# Without context manager — fragile:
f = open("file.txt", "w")
try:
    f.write("data")
finally:
    f.close()   # Must close manually!

# With context manager — clean and safe:
with open("file.txt", "w") as f:
    f.write("data")
# f is automatically closed here, even if an exception occurred
```

### Implementing `__enter__` and `__exit__`

```python
class DatabaseConnection:
    """A simple database connection that uses context manager protocol."""
    
    def __init__(self, host: str, port: int):
        self.host = host
        self.port = port
        self._connection = None
    
    def __enter__(self) -> "DatabaseConnection":
        """Called when entering the `with` block. Return value assigned to `as` variable."""
        print(f"Connecting to {self.host}:{self.port}")
        self._connection = f"Connection to {self.host}"  # Simulated connection
        return self   # Return self so `with conn as db:` gives you the connection object
    
    def __exit__(self, exc_type, exc_val, exc_tb) -> bool:
        """Called when leaving the `with` block (even if exception occurred).
        
        Args:
            exc_type: Exception type if exception occurred, else None
            exc_val: Exception value if exception occurred, else None
            exc_tb: Traceback if exception occurred, else None
        
        Returns:
            True to suppress the exception, False (or None) to re-raise it
        """
        print(f"Disconnecting from {self.host}:{self.port}")
        self._connection = None
        
        if exc_type is not None:
            print(f"Exception occurred: {exc_val}")
        
        return False   # Don't suppress exceptions
    
    def query(self, sql: str) -> list:
        if not self._connection:
            raise RuntimeError("Not connected!")
        print(f"Executing: {sql}")
        return []   # Simulated result


# Using the context manager:
with DatabaseConnection("localhost", 5432) as db:
    results = db.query("SELECT * FROM users")
    print(f"Got {len(results)} results")
# "Disconnecting from localhost:5432" — runs even if exception occurred!

# What happens if there's an exception:
try:
    with DatabaseConnection("localhost", 5432) as db:
        raise ValueError("Something went wrong!")
except ValueError:
    pass
# Output:
# Connecting to localhost:5432
# Exception occurred: Something went wrong!
# Disconnecting from localhost:5432
# (Exception is re-raised because __exit__ returns False)
```

### `contextlib.contextmanager` — The easier way

For simple cases, use `@contextmanager` from `contextlib`:

```python
from contextlib import contextmanager
from typing import Generator

@contextmanager
def timer(name: str) -> Generator:
    """Context manager that measures execution time."""
    import time
    start = time.perf_counter()
    try:
        yield   # Code inside the `with` block runs here
    finally:
        elapsed = time.perf_counter() - start
        print(f"{name}: {elapsed:.4f}s")


with timer("database query"):
    import time
    time.sleep(0.1)   # Simulated work
# "database query: 0.1002s"


@contextmanager
def managed_resource(name: str) -> Generator:
    """General resource management pattern."""
    print(f"Acquiring {name}")
    resource = f"Resource({name})"
    try:
        yield resource
    except Exception as e:
        print(f"Error with {name}: {e}")
        raise
    finally:
        print(f"Releasing {name}")


with managed_resource("database") as res:
    print(f"Using {res}")
# Output:
# Acquiring database
# Using Resource(database)
# Releasing database
```

---

## Descriptors — How `@property` Works Under the Hood

A descriptor is an object that defines `__get__`, `__set__`, and/or `__delete__`.
The `@property` decorator is built on descriptors. Understanding descriptors explains
how Python's attribute access works.

```python
class Validator:
    """A descriptor that validates values before storing."""
    
    def __init__(self, min_value: float, max_value: float):
        self.min_value = min_value
        self.max_value = max_value
        self.name = None  # Will be set by __set_name__
    
    def __set_name__(self, owner, name: str):
        """Called when the descriptor is assigned to a class attribute."""
        self.name = name
        self.storage_name = f"_{name}"
    
    def __get__(self, obj, objtype=None):
        """Called when the attribute is read."""
        if obj is None:
            return self   # Accessed on the class, not an instance
        return getattr(obj, self.storage_name, None)
    
    def __set__(self, obj, value: float):
        """Called when the attribute is assigned."""
        if not self.min_value <= value <= self.max_value:
            raise ValueError(
                f"{self.name} must be between {self.min_value} and {self.max_value}, got {value}"
            )
        setattr(obj, self.storage_name, value)


class Circle:
    # These are descriptors at the class level!
    radius = Validator(0.01, 1000.0)
    
    def __init__(self, radius: float):
        self.radius = radius   # This calls Validator.__set__!
    
    @property
    def area(self) -> float:
        import math
        return math.pi * self.radius ** 2


c = Circle(5.0)
print(c.radius)    # 5.0 — calls Validator.__get__
c.radius = 10.0    # calls Validator.__set__
# c.radius = -1.0  # ValueError: radius must be between 0.01 and 1000.0, got -1.0
```

Descriptors are used by: `@property`, `@classmethod`, `@staticmethod`, `__slots__`, and more.
For most code, you don't need to write descriptors directly — just use `@property`.

---

## Class Decorators

You can decorate classes just like functions. `@dataclass` is itself a class decorator:

```python
from functools import wraps

def singleton(cls):
    """Class decorator that makes a class a singleton (only one instance)."""
    instances = {}
    
    @wraps(cls)
    def get_instance(*args, **kwargs):
        if cls not in instances:
            instances[cls] = cls(*args, **kwargs)
        return instances[cls]
    
    return get_instance


@singleton
class Config:
    def __init__(self):
        self.debug = False
        self.log_level = "INFO"


config1 = Config()
config2 = Config()

print(config1 is config2)   # True — same object!
config1.debug = True
print(config2.debug)        # True — same object!
```

---

## Metaclasses (Brief Overview)

Every class in Python is an instance of `type`. `type` is the metaclass — the class
of classes. Metaclasses let you control class creation.

```python
# type() can create classes dynamically:
Dog = type("Dog", (object,), {
    "sound": "Woof",
    "speak": lambda self: f"I say {self.sound}"
})

rex = Dog()
print(rex.speak())   # "I say Woof"

# Custom metaclass — rare in practice
class AutoPropertyMeta(type):
    """Metaclass that auto-wraps _-prefixed attributes with properties."""
    
    def __new__(mcs, name, bases, namespace):
        # Called when the class is being defined
        # namespace is the class body as a dict
        return super().__new__(mcs, name, bases, namespace)

# Most Python code NEVER needs metaclasses.
# They're used by: ABCMeta, dataclasses, ORMs (SQLAlchemy), some frameworks
```

**When you might encounter metaclasses:**
- Django models
- SQLAlchemy ORM
- Custom frameworks
- The `abc.ABCMeta` metaclass (used internally by `ABC`)

**Rule:** You almost never need to write a metaclass. If you think you do, a class
decorator or `__init_subclass__` is usually the better solution.

---

## Python OOP Best Practices

### Prefer composition over inheritance
```python
# Avoid deep inheritance hierarchies
# Good: flat, composable
class Logger:
    def log(self, message: str) -> None:
        print(message)

class EmailSender:
    def send(self, to: str, message: str) -> None:
        print(f"Sending to {to}: {message}")

class UserService:
    def __init__(self):
        self._logger = Logger()       # Composed, not inherited
        self._email = EmailSender()   # Composed, not inherited
```

### Use `@dataclass` for simple data classes
```python
# Don't write __init__, __repr__, __eq__ manually for data classes
@dataclass
class ProductVariant:
    sku: str
    color: str
    size: str
    price: float
```

### Use ABC for hierarchies, Protocol for interfaces
```python
# ABC when you're building your own hierarchy with shared implementation
class Component(ABC):
    @abstractmethod
    def render(self) -> str: ...

# Protocol when you need a flexible interface that works with external types
class Renderable(Protocol):
    def render(self) -> str: ...
```

### Keep classes focused (Single Responsibility)
```python
# Bad: class does too many things
class UserManagerEmailSenderAuthenticator:
    ...

# Good: separate classes with clear responsibilities
class User:        # Data and identity
    ...
class UserService: # Business logic
    ...
class AuthService: # Authentication
    ...
```

### Use type hints throughout
```python
from typing import Optional, List
from abc import ABC, abstractmethod

class Repository(ABC):
    @abstractmethod
    def find_by_id(self, id: int) -> Optional[dict]:
        ...
    
    @abstractmethod
    def find_all(self) -> List[dict]:
        ...
    
    @abstractmethod
    def save(self, item: dict) -> dict:
        ...
```

---

## Python vs Java OOP Best Practices

| Practice                 | Java                          | Python                          |
|--------------------------|-------------------------------|---------------------------------|
| Simple data classes      | Lombock / record (Java 14+)   | `@dataclass`                    |
| Immutable types          | `final` fields                | `@dataclass(frozen=True)` or `NamedTuple` |
| Null safety              | Optional<T>                   | `Optional[T]` type hint         |
| Interfaces               | `interface`                   | `Protocol`                      |
| Abstract classes         | `abstract class`              | `ABC` + `@abstractmethod`       |
| Singletons               | `static` instance pattern     | Class decorator or module-level |
| Constants                | `static final`                | Module-level `UPPER_CASE`       |
| Factory methods          | Static methods / factory class| `@classmethod`                  |
| Resource management      | `try-with-resources`          | `with` statement / context manager |

## Congratulations!

You've completed the theory section of the Python OOP module. You now understand:
- Classes, objects, and Python's dynamic object model
- `__init__`, `self`, and dunder methods
- Python's convention-based encapsulation and `@property`
- Inheritance, `super()`, MRO, and multiple inheritance
- Duck typing and polymorphism
- ABCs, Protocols, and type hints
- Dataclasses, NamedTuples, context managers

**Next:** Work through the code examples and exercises to put these concepts into practice.
