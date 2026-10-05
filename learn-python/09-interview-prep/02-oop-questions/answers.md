# Python OOP Interview Questions — 30 Q&A

---

## Q1. What are the four pillars of OOP and how does Python implement them?

**Answer:**

1. **Encapsulation** — bundling data and behaviour; Python uses naming conventions
   (`_protected`, `__private` name-mangling) rather than access modifiers.
2. **Abstraction** — hiding complexity; Python uses `ABC`, `Protocol`, and `@property`.
3. **Inheritance** — code reuse via base classes; Python supports single and multiple.
4. **Polymorphism** — same interface, different behaviour; achieved naturally via
   duck typing and method overriding.

```python
class Animal:
    def __init__(self, name: str):
        self._name = name      # protected convention

    def speak(self) -> str:    # polymorphic method
        raise NotImplementedError

class Dog(Animal):
    def speak(self) -> str:    # override
        return "Woof!"

class Cat(Animal):
    def speak(self) -> str:
        return "Meow!"

animals: list[Animal] = [Dog("Rex"), Cat("Whiskers")]
for a in animals:
    print(a.speak())   # polymorphism — same call, different output
```

---

## Q2. What is the difference between `_name`, `__name`, and `__name__`?

**Answer:**

| Convention | Meaning |
|-----------|---------|
| `name` | Public attribute |
| `_name` | Protected by convention — don't use externally, but not enforced |
| `__name` | Name-mangled to `_ClassName__name` — hard to access from outside |
| `__name__` | Dunder/magic — special Python meaning, don't create your own |

```python
class MyClass:
    def __init__(self):
        self.public = "anyone"
        self._protected = "by convention"
        self.__private = "name-mangled"     # stored as _MyClass__private

obj = MyClass()
obj.public          # OK
obj._protected      # works but frowned upon
obj.__private       # AttributeError
obj._MyClass__private  # works — name mangling, not true private
```

---

## Q3. How does Python achieve encapsulation without access modifiers?

**Answer:**

Python uses naming conventions and `@property`:

```python
class BankAccount:
    def __init__(self, balance: float) -> None:
        self.__balance = balance    # "private" via name mangling

    @property
    def balance(self) -> float:
        return self.__balance

    @balance.setter
    def balance(self, value: float) -> None:
        if value < 0:
            raise ValueError("Balance cannot be negative")
        self.__balance = value

    def deposit(self, amount: float) -> None:
        if amount <= 0:
            raise ValueError("Amount must be positive")
        self.__balance += amount
```

The `@property` provides a clean public interface while controlling internal state.

---

## Q4. What is the difference between class variables and instance variables?

**Answer:**

- **Class variable** — shared by ALL instances; defined at class level
- **Instance variable** — unique to each instance; defined in `__init__`

```python
class Counter:
    count = 0       # class variable, shared

    def __init__(self, name: str) -> None:
        Counter.count += 1
        self.name = name    # instance variable

a = Counter("a")
b = Counter("b")

Counter.count   # 2 — shared
a.name          # "a"
b.name          # "b"
```

**Danger:** assigning to a class variable via an instance shadows it:
```python
a.count = 99    # creates INSTANCE variable, doesn't change Counter.count
Counter.count   # still 2
```

---

## Q5. How does inheritance work in Python?

**Answer:**

```python
class Shape:
    def __init__(self, color: str = "red") -> None:
        self.color = color

    def area(self) -> float:
        raise NotImplementedError

    def describe(self) -> str:
        return f"{self.__class__.__name__}(color={self.color}, area={self.area():.2f})"

class Circle(Shape):
    def __init__(self, radius: float, **kwargs) -> None:
        super().__init__(**kwargs)    # pass remaining kwargs to parent
        self.radius = radius

    def area(self) -> float:
        import math
        return math.pi * self.radius ** 2

c = Circle(5, color="blue")
c.describe()   # "Circle(color=blue, area=78.54)"
```

`super()` calls the next class in the MRO — always use it instead of hard-coding
the parent class name.

---

## Q6. What are abstract classes (ABC) and when should you use them?

**Answer:**

An **abstract class** cannot be instantiated; it defines a contract that
subclasses must fulfil by implementing all abstract methods.

```python
from abc import ABC, abstractmethod

class Repository(ABC):
    @abstractmethod
    def find_by_id(self, id: str): ...

    @abstractmethod
    def save(self, entity): ...

    def find_or_raise(self, id: str):   # concrete method on ABC
        result = self.find_by_id(id)
        if result is None:
            raise KeyError(f"Not found: {id}")
        return result

class InMemoryRepository(Repository):
    def find_by_id(self, id: str): ...   # must implement
    def save(self, entity): ...           # must implement

Repository()          # TypeError — cannot instantiate abstract class
InMemoryRepository()  # OK
```

**Use ABCs when:**
- You want to enforce that subclasses implement specific methods
- You want shared concrete methods on the base class
- You need `isinstance` checks against the interface

---

## Q7. What is `typing.Protocol` and when should you prefer it over ABC?

**Answer:**

`Protocol` enables **structural subtyping** (duck typing + type hints).
A class satisfies a Protocol if it has the right methods — no explicit
inheritance required.

```python
from typing import Protocol, runtime_checkable

@runtime_checkable
class Drawable(Protocol):
    def draw(self, canvas: object) -> None: ...
    def resize(self, factor: float) -> None: ...

class Circle:   # does NOT inherit Drawable
    def draw(self, canvas): print("Drawing circle")
    def resize(self, factor): self.radius *= factor

def render(item: Drawable) -> None:   # type hint works
    item.draw(None)

render(Circle())   # OK — Circle is structurally compatible
isinstance(Circle(), Drawable)  # True — @runtime_checkable
```

**Prefer Protocol when:** the classes come from different hierarchies or
third-party code you cannot modify. Use ABC when you want a shared base
class with concrete methods.

---

## Q8. What is a mixin and how is it implemented in Python?

**Answer:**

A **mixin** is a class that provides reusable behaviour for other classes via
multiple inheritance, without being a standalone base class.

```python
class TimestampMixin:
    """Adds created_at and updated_at to any model."""
    from datetime import datetime

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.created_at = self.datetime.now()
        self.updated_at = None

    def touch(self):
        self.updated_at = self.datetime.now()

class SerializeMixin:
    """Adds to_dict() to any class."""
    def to_dict(self) -> dict:
        return {k: v for k, v in vars(self).items() if not k.startswith("_")}

class User(TimestampMixin, SerializeMixin):
    def __init__(self, name: str):
        super().__init__()
        self.name = name

u = User("Alice")
u.to_dict()   # {"name": "Alice", "created_at": ..., "updated_at": None}
u.touch()
```

**Mixin naming convention:** suffix with `Mixin` to signal intent.

---

## Q9. How does method resolution order (MRO) work with multiple inheritance?

**Answer:**

Python's **C3 linearisation** ensures a consistent, deterministic method lookup
order that respects inheritance hierarchy and avoids the "diamond problem."

```python
class A:
    def hello(self): print("A")

class B(A):
    def hello(self): print("B")

class C(A):
    def hello(self): print("C")

class D(B, C):
    pass

# MRO: D -> B -> C -> A -> object
D.__mro__
# (<class 'D'>, <class 'B'>, <class 'C'>, <class 'A'>, <class 'object'>)

D().hello()   # "B" — first in MRO
```

**`super()` follows MRO**, not just the direct parent:
```python
class B(A):
    def hello(self):
        super().hello()   # when called from D, calls C.hello, not A.hello
        print("B")
```

---

## Q10. What is the `__init_subclass__` hook?

**Answer:**

`__init_subclass__` is called whenever a class is subclassed. It's a lighter
alternative to metaclasses for class customisation.

```python
class Plugin:
    _registry: dict = {}

    def __init_subclass__(cls, plugin_name: str = None, **kwargs):
        super().__init_subclass__(**kwargs)
        if plugin_name:
            Plugin._registry[plugin_name] = cls

class EmailPlugin(Plugin, plugin_name="email"):
    def send(self): print("Sending email")

class SMSPlugin(Plugin, plugin_name="sms"):
    def send(self): print("Sending SMS")

Plugin._registry
# {"email": EmailPlugin, "sms": SMSPlugin}
Plugin._registry["email"]().send()  # Sending email
```

---

## Q11. How does `__slots__` interact with inheritance?

**Answer:**

When using `__slots__` with inheritance, each class in the hierarchy must
define its own `__slots__`. If a base class has `__dict__` (no `__slots__`),
the subclass also gets `__dict__`.

```python
class Base:
    __slots__ = ("x",)

class Child(Base):
    __slots__ = ("y",)   # adds slot y; inherits slot x

c = Child()
c.x = 1   # OK
c.y = 2   # OK
c.z = 3   # AttributeError — no __dict__
```

**Pitfall:** mixing slotted and non-slotted classes in a hierarchy re-introduces
`__dict__`:
```python
class WithDict:
    pass  # has __dict__

class Mixed(WithDict):
    __slots__ = ("x",)   # still has __dict__ from WithDict!
```

---

## Q12. What is operator overloading and how is it implemented?

**Answer:**

Python lets you define how operators work on custom objects via dunder methods:

```python
from dataclasses import dataclass

@dataclass
class Vector:
    x: float
    y: float

    def __add__(self, other: "Vector") -> "Vector":
        return Vector(self.x + other.x, self.y + other.y)

    def __mul__(self, scalar: float) -> "Vector":
        return Vector(self.x * scalar, self.y * scalar)

    def __rmul__(self, scalar: float) -> "Vector":  # 3 * v
        return self.__mul__(scalar)

    def __abs__(self) -> float:
        return (self.x**2 + self.y**2) ** 0.5

    def __eq__(self, other) -> bool:
        return isinstance(other, Vector) and self.x == other.x and self.y == other.y

v1 = Vector(1, 2)
v2 = Vector(3, 4)
v1 + v2       # Vector(4, 6)
v1 * 3        # Vector(3, 6)
abs(v1)       # 2.236...
```

---

## Q13. What is a class decorator vs an instance decorator?

**Answer:**

```python
# Decorator applied to a function/method (instance-level)
def log(func):
    def wrapper(*args, **kwargs):
        print(f"Calling {func.__name__}")
        return func(*args, **kwargs)
    return wrapper

class MyClass:
    @log
    def method(self):
        return 42

# Decorator applied to a class
def singleton(cls):
    instances = {}
    def get_instance(*args, **kwargs):
        if cls not in instances:
            instances[cls] = cls(*args, **kwargs)
        return instances[cls]
    return get_instance

@singleton
class Config:
    def __init__(self):
        self.debug = False

Config() is Config()   # True — same instance
```

---

## Q14. What is the difference between composition and inheritance?

**Answer:**

- **Inheritance** ("is-a"): `Dog IS-A Animal`; inherits all parent behaviour
- **Composition** ("has-a"): `Car HAS-A Engine`; delegates to contained objects

```python
# Inheritance
class Logger:
    def log(self, message): print(f"[LOG] {message}")

class Service(Logger):   # tight coupling; inherits everything
    def process(self): self.log("processing")

# Composition — preferred for flexibility
class Service:
    def __init__(self, logger: Logger) -> None:
        self._logger = logger   # injected dependency

    def process(self):
        self._logger.log("processing")
```

**Favour composition over inheritance** (from Gang of Four) because:
- Easier to change the composed object at runtime
- Avoids fragile base class problem
- Reduces coupling

---

## Q15. What is the `__call__` method?

**Answer:**

Defining `__call__` makes an object callable like a function.

```python
class Multiplier:
    def __init__(self, factor: int) -> None:
        self.factor = factor

    def __call__(self, x: int) -> int:
        return x * self.factor

triple = Multiplier(3)
triple(10)   # 30 — calls __call__
callable(triple)  # True
```

**Use cases:**
- Stateful callables (closures with richer state)
- Classes that implement the Strategy pattern
- Function-like objects that need `__init__` for configuration

---

## Q16. How do you implement the Singleton pattern in Python?

**Answer:**

```python
# Method 1: class variable
class Singleton:
    _instance = None

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self, value: int = 0):
        if not hasattr(self, "_initialized"):
            self.value = value
            self._initialized = True

s1 = Singleton(42)
s2 = Singleton(99)
s1 is s2       # True
s1.value       # 42 (init only runs once)

# Method 2: module-level (simplest in Python)
# config.py — import once, reuse everywhere
class _Config:
    debug = False

config = _Config()   # module-level singleton
```

---

## Q17. What is the Observer pattern and how is it implemented in Python?

**Answer:**

The Observer pattern allows objects (observers) to subscribe to events from a
subject.

```python
from typing import Callable, List


class EventEmitter:
    """Simple Observer/EventEmitter implementation."""

    def __init__(self) -> None:
        self._listeners: dict[str, List[Callable]] = {}

    def on(self, event: str, callback: Callable) -> None:
        self._listeners.setdefault(event, []).append(callback)

    def emit(self, event: str, *args, **kwargs) -> None:
        for callback in self._listeners.get(event, []):
            callback(*args, **kwargs)


class Button(EventEmitter):
    def click(self) -> None:
        self.emit("click", source=self)


btn = Button()
btn.on("click", lambda src: print("Button clicked!"))
btn.on("click", lambda src: print("Second handler"))
btn.click()
# Button clicked!
# Second handler
```

---

## Q18. What is the Strategy pattern in Python?

**Answer:**

The Strategy pattern defines a family of algorithms, encapsulates each one,
and makes them interchangeable. In Python this is often just a callable.

```python
from typing import Callable, List

# Strategy as a callable
SortStrategy = Callable[[List], List]

def bubble_sort(data: List) -> List:
    data = list(data)
    for i in range(len(data)):
        for j in range(len(data) - 1 - i):
            if data[j] > data[j+1]:
                data[j], data[j+1] = data[j+1], data[j]
    return data

def python_sort(data: List) -> List:
    return sorted(data)

class Sorter:
    def __init__(self, strategy: SortStrategy) -> None:
        self._strategy = strategy

    def sort(self, data: List) -> List:
        return self._strategy(data)

sorter = Sorter(python_sort)
sorter.sort([3, 1, 2])   # [1, 2, 3]
sorter._strategy = bubble_sort   # swap strategy at runtime
```

---

## Q19. What is `__repr__` and why should every class implement it?

**Answer:**

`__repr__` should return a string that, ideally, could be used to recreate
the object. It's the developer-facing representation shown in the REPL and
in `repr()`.

```python
class Point:
    def __init__(self, x: float, y: float) -> None:
        self.x, self.y = x, y

    def __repr__(self) -> str:
        return f"Point({self.x!r}, {self.y!r})"

p = Point(3.0, 4.0)
repr(p)   # "Point(3.0, 4.0)"
eval(repr(p)) == p   # True if __eq__ is defined
```

Without `__repr__`, you see `<__main__.Point object at 0x7f...>` — useless for
debugging.

**Rule:** always implement `__repr__`. The `@dataclass` decorator does this
automatically.

---

## Q20. What is method chaining and how do you implement it?

**Answer:**

Method chaining returns `self` from each method so calls can be chained:

```python
class QueryBuilder:
    def __init__(self, table: str) -> None:
        self._table = table
        self._conditions: list[str] = []
        self._limit_val: int = None

    def where(self, condition: str) -> "QueryBuilder":
        self._conditions.append(condition)
        return self   # enables chaining

    def limit(self, n: int) -> "QueryBuilder":
        self._limit_val = n
        return self

    def build(self) -> str:
        sql = f"SELECT * FROM {self._table}"
        if self._conditions:
            sql += " WHERE " + " AND ".join(self._conditions)
        if self._limit_val:
            sql += f" LIMIT {self._limit_val}"
        return sql

query = (
    QueryBuilder("users")
    .where("age > 18")
    .where("active = 1")
    .limit(10)
    .build()
)
# "SELECT * FROM users WHERE age > 18 AND active = 1 LIMIT 10"
```

---

## Q21. What is the Factory pattern in Python?

**Answer:**

```python
from abc import ABC, abstractmethod

class Animal(ABC):
    @abstractmethod
    def speak(self) -> str: ...

class Dog(Animal):
    def speak(self) -> str: return "Woof!"

class Cat(Animal):
    def speak(self) -> str: return "Meow!"

# Factory function (simple, Pythonic)
def animal_factory(kind: str) -> Animal:
    registry = {"dog": Dog, "cat": Cat}
    if kind not in registry:
        raise ValueError(f"Unknown animal: {kind}")
    return registry[kind]()

# Factory method on the class
class Shape:
    @classmethod
    def from_dict(cls, data: dict) -> "Shape":
        """Alternative constructor — common factory method pattern."""
        return cls(**data)
```

Python's duck typing makes the Factory pattern simpler than in Java — you don't
always need an abstract factory; a dict mapping names to classes suffices.

---

## Q22. What is a data class vs a regular class vs a named tuple?

**Answer:**

```python
# namedtuple — immutable, lightweight, tuple semantics
from collections import namedtuple
Point = namedtuple("Point", ["x", "y"])
p = Point(1, 2)
p.x = 3   # AttributeError — immutable

# typing.NamedTuple — same but with type hints and methods
from typing import NamedTuple
class Point(NamedTuple):
    x: float
    y: float
    def distance(self): return (self.x**2 + self.y**2) ** 0.5

# dataclass — mutable by default, full class features
from dataclasses import dataclass
@dataclass
class Point:
    x: float
    y: float
    def distance(self): return (self.x**2 + self.y**2) ** 0.5

# @dataclass(frozen=True) makes it immutable + hashable
```

| | namedtuple | dataclass | class |
|--|-----------|-----------|-------|
| Mutable | No | Yes | Yes |
| Tuple interface | Yes | No | No |
| Auto __repr__ | Yes | Yes | No |
| Auto __eq__ | Yes | Yes | No |
| Inheritance | Limited | Full | Full |

---

## Q23. How does `__hash__` relate to `__eq__`?

**Answer:**

Python enforces a contract: objects that compare equal must have the same hash.

- If you define `__eq__`, Python sets `__hash__` to `None` (unhashable) by default.
- If you want the object to be hashable, you must also define `__hash__`.
- Immutable objects should be hashable; mutable objects generally should not be.

```python
class Point:
    def __init__(self, x, y):
        self.x, self.y = x, y

    def __eq__(self, other):
        return isinstance(other, Point) and self.x == other.x and self.y == other.y

    def __hash__(self):
        return hash((self.x, self.y))   # hash of tuple of components

p1 = Point(1, 2)
p2 = Point(1, 2)
p1 == p2        # True
hash(p1) == hash(p2)   # True
{p1, p2}        # {Point(1, 2)} — deduplicated
```

`@dataclass(frozen=True)` automatically generates a consistent `__hash__`.

---

## Q24. What is `__getitem__` and how does it enable iteration?

**Answer:**

`__getitem__` allows subscript access (`obj[key]`). Python can also iterate
over objects that have `__getitem__` — it calls it with 0, 1, 2, ... until
`IndexError` is raised.

```python
class NumberRange:
    def __init__(self, start: int, stop: int):
        self.start = start
        self.stop = stop

    def __getitem__(self, index: int) -> int:
        if index >= (self.stop - self.start):
            raise IndexError(index)
        return self.start + index

    def __len__(self) -> int:
        return self.stop - self.start

r = NumberRange(5, 10)
r[0]          # 5
r[2]          # 7
list(r)       # [5, 6, 7, 8, 9]  — iterable via __getitem__
5 in r        # True  — membership test via iteration
```

For proper iterators, implement `__iter__` + `__next__` or make `__iter__`
return a generator.

---

## Q25. What is `__iter__` and `__next__`?

**Answer:**

An **iterable** implements `__iter__` (returns an iterator).
An **iterator** implements `__next__` (returns next value, raises `StopIteration`
when exhausted).

```python
class Countdown:
    """An iterator that counts down from n to 0."""

    def __init__(self, start: int) -> None:
        self.current = start

    def __iter__(self):
        return self   # the object is its own iterator

    def __next__(self) -> int:
        if self.current < 0:
            raise StopIteration
        value = self.current
        self.current -= 1
        return value

list(Countdown(3))   # [3, 2, 1, 0]

# Simpler with a generator:
def countdown(n):
    while n >= 0:
        yield n
        n -= 1
```

---

## Q26. What is the Decorator pattern (not decorator syntax)?

**Answer:**

The Decorator design pattern wraps an object to add behaviour without
modifying the original class — useful when inheritance is too rigid.

```python
class Coffee:
    def cost(self) -> float: return 1.0
    def description(self) -> str: return "Coffee"

class MilkDecorator:
    def __init__(self, coffee) -> None:
        self._coffee = coffee

    def cost(self) -> float:
        return self._coffee.cost() + 0.5

    def description(self) -> str:
        return self._coffee.description() + " + Milk"

class SugarDecorator:
    def __init__(self, coffee) -> None:
        self._coffee = coffee

    def cost(self) -> float:
        return self._coffee.cost() + 0.2

    def description(self) -> str:
        return self._coffee.description() + " + Sugar"

drink = SugarDecorator(MilkDecorator(Coffee()))
drink.cost()           # 1.7
drink.description()    # "Coffee + Milk + Sugar"
```

---

## Q27. How do you implement a custom iterator with `__iter__` and `__next__`?

**Answer:**

```python
class FibonacciIterator:
    """Generates Fibonacci numbers up to a max count."""

    def __init__(self, max_count: int) -> None:
        self.max_count = max_count
        self.count = 0
        self.a, self.b = 0, 1

    def __iter__(self):
        return self

    def __next__(self) -> int:
        if self.count >= self.max_count:
            raise StopIteration
        value = self.a
        self.a, self.b = self.b, self.a + self.b
        self.count += 1
        return value

list(FibonacciIterator(7))   # [0, 1, 1, 2, 3, 5, 8]
```

Note: iterators are **consumed** — once exhausted they cannot be restarted
without creating a new object.

---

## Q28. What is `@property` and when should you prefer it over direct attributes?

**Answer:**

Use `@property` when:
- You need to validate or compute a value on access
- You want to enforce constraints without changing the public API
- You need to deprecate or log access
- You want a read-only computed attribute

```python
class Temperature:
    def __init__(self, celsius: float) -> None:
        self.celsius = celsius   # uses setter

    @property
    def celsius(self) -> float:
        return self._celsius

    @celsius.setter
    def celsius(self, value: float) -> None:
        if value < -273.15:
            raise ValueError(f"Temperature below absolute zero: {value}")
        self._celsius = value

    @property
    def fahrenheit(self) -> float:
        """Computed property — no setter."""
        return self._celsius * 9/5 + 32

t = Temperature(100)
t.fahrenheit   # 212.0
t.celsius = -300   # ValueError
```

---

## Q29. What is a metaclass and when would you use one?

**Answer:**

A metaclass is the "class of a class." It intercepts class creation:

```python
class AutoPropertyMeta(type):
    """Makes all methods that return a value into read-only properties."""

    def __new__(mcs, name, bases, namespace):
        new_namespace = {}
        for key, value in namespace.items():
            if callable(value) and not key.startswith("_"):
                new_namespace[key] = property(value)
            else:
                new_namespace[key] = value
        return super().__new__(mcs, name, bases, new_namespace)

class Circle(metaclass=AutoPropertyMeta):
    def __init__(self, radius):
        self._radius = radius

    def area(self):          # becomes a property
        import math
        return math.pi * self._radius ** 2

c = Circle(5)
c.area   # 78.539... (no parentheses needed!)
```

**Real-world uses:** Django models, SQLAlchemy declarative base, enum classes,
ORMs, plugin registries.

---

## Q30. How does Python's data model enable "Pythonic" code?

**Answer:**

Python's **data model** (dunder methods) lets your objects integrate seamlessly
with Python's built-in functions and syntax:

```python
class Matrix:
    def __init__(self, rows: list[list[float]]) -> None:
        self._rows = rows

    def __len__(self) -> int:               # len(matrix)
        return len(self._rows)

    def __getitem__(self, idx):             # matrix[0]
        return self._rows[idx]

    def __contains__(self, value) -> bool:  # value in matrix
        return any(value in row for row in self._rows)

    def __repr__(self) -> str:              # repr(matrix)
        return f"Matrix({self._rows!r})"

    def __add__(self, other: "Matrix") -> "Matrix":  # m1 + m2
        return Matrix([
            [a + b for a, b in zip(r1, r2)]
            for r1, r2 in zip(self._rows, other._rows)
        ])

m = Matrix([[1, 2], [3, 4]])
len(m)        # 2
m[0]          # [1, 2]
3 in m        # True
m + m         # Matrix([[2, 4], [6, 8]])
```

This is the essence of Pythonic code — objects that "feel like" built-in types
because they honour the data model.
