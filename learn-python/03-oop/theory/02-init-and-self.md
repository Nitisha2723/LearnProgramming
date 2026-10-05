# `__init__` and `self` in Python

## The Object Lifecycle: `__new__` vs `__init__`

In Python, creating an object happens in two steps:
1. **`__new__`** — allocates memory and creates the object (the actual "constructor")
2. **`__init__`** — initializes the newly created object with data

Most Python developers only ever work with `__init__` and never touch `__new__`. But
understanding the distinction is important:

```python
class MyClass:
    def __new__(cls, *args, **kwargs):
        # cls is the class itself (not an instance yet)
        # This is where memory is allocated
        print(f"__new__ called — creating {cls.__name__} object")
        instance = super().__new__(cls)  # Create the actual object
        return instance  # Must return the new instance
    
    def __init__(self, value: int):
        # self is the already-created object (returned by __new__)
        # We're just setting attributes, not creating the object
        print(f"__init__ called — initializing object with value={value}")
        self.value = value

obj = MyClass(42)
# Output:
# __new__ called — creating MyClass object
# __init__ called — initializing object with value=42
```

**Key insight:** When you write `obj = MyClass(42)`, Python calls `MyClass.__new__(MyClass, 42)`
first, which returns a new object, THEN calls `obj.__init__(42)` on that object.

The object **already exists** when `__init__` runs. That's why it's called an "initializer",
not a constructor. The Java `constructor` is actually closer to Python's `__new__ + __init__` combined.

### When would you override `__new__`?

Rarely. Common cases:
- Creating immutable types (subclassing `int`, `str`, `tuple`)
- Implementing singletons
- Metaclass programming

For 99% of Python code, you only need `__init__`.

---

## `self` — Python's Explicit `this`

In Java, `this` is implicit and always available inside any instance method. In Python,
the first parameter of every instance method is the object itself, and you must name it
explicitly. By convention, it's always named `self`.

```python
class Counter:
    def __init__(self, start: int = 0):
        self.count = start      # self.count is an instance attribute
    
    def increment(self):
        self.count += 1         # self refers to THIS Counter object
    
    def reset(self):
        self.count = 0
    
    def get_count(self) -> int:
        return self.count       # Access this object's count attribute

c1 = Counter(10)
c2 = Counter(5)

c1.increment()
c1.increment()
c2.increment()

print(c1.count)  # 12
print(c2.count)  # 6
```

When you call `c1.increment()`, Python translates this to `Counter.increment(c1)`. The
object `c1` becomes the first argument — that's what `self` is.

### Why Python requires explicit `self`

This is a deliberate design choice. Python's philosophy includes:
> "Explicit is better than implicit" — The Zen of Python

Having `self` explicitly listed:
- Makes it clear that a method receives an object as its first argument
- Makes the distinction between instance methods, class methods, and static methods visible
- Allows you to see exactly what's happening: `c1.increment()` is literally `Counter.increment(c1)`

```python
# This is exactly what Python does internally:
c1 = Counter()
Counter.increment(c1)      # Same as c1.increment()
Counter.increment(c2)      # Same as c2.increment()
```

### `self` is just a convention

You CAN name it something else (Python won't stop you), but you SHOULD always use `self`:

```python
class WeirdClass:
    def __init__(this, value):   # 'this' works but don't do this!
        this.value = value
    
    def double(me):              # 'me' works but don't do this!
        return me.value * 2

# Works fine, but will confuse every Python developer who reads it
```

---

## Dunder (Magic) Methods

"Dunder" stands for "double underscore". These are special methods that Python calls
automatically in certain situations. They let your objects work with Python's built-in
operations naturally.

### `__init__` — Initialization
```python
class Point:
    def __init__(self, x: float, y: float):
        self.x = x
        self.y = y
```

### `__str__` — Human-readable string representation
Called by `print()`, `str()`, and f-strings.

```python
class Point:
    def __init__(self, x: float, y: float):
        self.x = x
        self.y = y
    
    def __str__(self) -> str:
        return f"Point({self.x}, {self.y})"

p = Point(3.0, 4.0)
print(p)           # Point(3.0, 4.0)  — __str__ is called
print(f"Point: {p}")  # Point: Point(3.0, 4.0)  — __str__ is called
```

### `__repr__` — Developer-focused representation
Called by `repr()` and in the Python REPL. Should ideally be a string you could
paste back into Python to recreate the object.

```python
class Point:
    def __init__(self, x: float, y: float):
        self.x = x
        self.y = y
    
    def __str__(self) -> str:
        return f"({self.x}, {self.y})"             # For users
    
    def __repr__(self) -> str:
        return f"Point(x={self.x}, y={self.y})"   # For developers

p = Point(3.0, 4.0)
print(p)      # (3.0, 4.0)         — __str__ called
repr(p)       # "Point(x=3.0, y=4.0)"  — __repr__ called

# In the REPL, just typing the variable calls __repr__:
# >>> p
# Point(x=3.0, y=4.0)

# In a list or dict, __repr__ is used:
points = [Point(1, 2), Point(3, 4)]
print(points)  # [Point(x=1, y=2), Point(x=3, y=4)]  — __repr__ for each item
```

**Rule of thumb:** Always define `__repr__`. Define `__str__` when you want a different
user-facing format. If only `__repr__` is defined, Python uses it for both.

### `__eq__` — Equality comparison
By default, Python compares objects by identity (like Java's `==`). Override `__eq__`
to compare by value.

```python
class Point:
    def __init__(self, x: float, y: float):
        self.x = x
        self.y = y
    
    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Point):
            return NotImplemented
        return self.x == other.x and self.y == other.y
    
    def __repr__(self) -> str:
        return f"Point({self.x}, {self.y})"

p1 = Point(3.0, 4.0)
p2 = Point(3.0, 4.0)
p3 = Point(1.0, 2.0)

print(p1 == p2)  # True  — same values
print(p1 == p3)  # False — different values
print(p1 is p2)  # False — different objects in memory

# Note: if you define __eq__, Python disables __hash__ by default
# (because if equal objects should be the same key in a dict,
#  they need the same hash)
```

### `__len__` — Length
Called by `len()`.

```python
class Playlist:
    def __init__(self):
        self._songs = []
    
    def add_song(self, song: str) -> None:
        self._songs.append(song)
    
    def __len__(self) -> int:
        return len(self._songs)

playlist = Playlist()
playlist.add_song("Song A")
playlist.add_song("Song B")
playlist.add_song("Song C")

print(len(playlist))  # 3
```

### `__add__` — Addition operator
Called when you use `+`.

```python
class Vector:
    def __init__(self, x: float, y: float):
        self.x = x
        self.y = y
    
    def __add__(self, other: "Vector") -> "Vector":
        return Vector(self.x + other.x, self.y + other.y)
    
    def __repr__(self) -> str:
        return f"Vector({self.x}, {self.y})"

v1 = Vector(1.0, 2.0)
v2 = Vector(3.0, 4.0)
v3 = v1 + v2              # Python calls v1.__add__(v2)
print(v3)                  # Vector(4.0, 6.0)
```

### `__lt__`, `__le__`, `__gt__`, `__ge__` — Comparison operators

```python
from functools import total_ordering

@total_ordering  # Generates __le__, __gt__, __ge__ from __eq__ and __lt__
class Temperature:
    def __init__(self, celsius: float):
        self.celsius = celsius
    
    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Temperature):
            return NotImplemented
        return self.celsius == other.celsius
    
    def __lt__(self, other: "Temperature") -> bool:
        if not isinstance(other, Temperature):
            return NotImplemented
        return self.celsius < other.celsius
    
    def __repr__(self) -> str:
        return f"{self.celsius}°C"

t1 = Temperature(20.0)
t2 = Temperature(30.0)

print(t1 < t2)   # True
print(t1 > t2)   # False  (generated by @total_ordering)
print(t1 <= t2)  # True   (generated by @total_ordering)

temps = [Temperature(30), Temperature(10), Temperature(20)]
print(sorted(temps))  # [10°C, 20°C, 30°C]  — Python can sort them!
```

### `__getitem__` and `__setitem__` — Indexing

```python
class Matrix:
    def __init__(self, rows: int, cols: int):
        self._data = [[0.0] * cols for _ in range(rows)]
    
    def __getitem__(self, key: tuple) -> float:
        row, col = key
        return self._data[row][col]
    
    def __setitem__(self, key: tuple, value: float) -> None:
        row, col = key
        self._data[row][col] = value

m = Matrix(3, 3)
m[0, 0] = 1.0    # Python calls m.__setitem__((0, 0), 1.0)
m[1, 1] = 5.0
print(m[0, 0])   # Python calls m.__getitem__((0, 0)) → 1.0
```

---

## Dataclasses — The Modern Way to Write Simple Classes

Python 3.7 introduced `@dataclass`, which automatically generates `__init__`, `__repr__`,
`__eq__`, and optionally other methods. For classes that are mainly data containers, this
dramatically reduces boilerplate.

```python
from dataclasses import dataclass, field
from typing import List

# Without dataclass — lots of boilerplate
class PersonManual:
    def __init__(self, name: str, age: int, email: str = ""):
        self.name = name
        self.age = age
        self.email = email
    
    def __repr__(self) -> str:
        return f"PersonManual(name={self.name!r}, age={self.age}, email={self.email!r})"
    
    def __eq__(self, other: object) -> bool:
        if not isinstance(other, PersonManual):
            return NotImplemented
        return (self.name, self.age, self.email) == (other.name, other.age, other.email)


# With dataclass — same functionality, much less code
@dataclass
class Person:
    name: str
    age: int
    email: str = ""    # Default value

# Python auto-generates __init__, __repr__, __eq__
p1 = Person("Alice", 30, "alice@example.com")
p2 = Person("Alice", 30, "alice@example.com")
p3 = Person("Bob", 25)

print(p1)          # Person(name='Alice', age=30, email='alice@example.com')
print(p1 == p2)    # True
print(p1 == p3)    # False
```

### Dataclass with mutable defaults

```python
from dataclasses import dataclass, field
from typing import List

@dataclass
class Team:
    name: str
    # WRONG: members: List[str] = []  — mutable default, will cause issues
    # CORRECT: use field(default_factory=...)
    members: List[str] = field(default_factory=list)
    
    def add_member(self, name: str) -> None:
        self.members.append(name)

team1 = Team("Pythons")
team2 = Team("Javas")

team1.add_member("Alice")
print(team1.members)  # ['Alice']
print(team2.members)  # []  — correctly separate!
```

### Frozen dataclasses (immutable)

```python
@dataclass(frozen=True)
class Coordinate:
    latitude: float
    longitude: float

coord = Coordinate(52.52, 13.40)
# coord.latitude = 0.0  # This would raise FrozenInstanceError!

# Frozen dataclasses are hashable — can be used as dict keys or in sets!
location_map = {coord: "Berlin"}
```

### Post-init validation

```python
@dataclass
class Student:
    name: str
    grade: float
    
    def __post_init__(self):
        """Called automatically after __init__."""
        if not 0.0 <= self.grade <= 100.0:
            raise ValueError(f"Grade must be 0-100, got {self.grade}")
        if not self.name.strip():
            raise ValueError("Name cannot be empty")

# This raises ValueError
# bad_student = Student("Alice", 150.0)
```

---

## Summary of Dunder Methods

| Method           | Called when              | Example                    |
|------------------|--------------------------|----------------------------|
| `__init__`       | `MyClass(args)`          | Object initialization      |
| `__str__`        | `str(obj)`, `print(obj)` | User-friendly display      |
| `__repr__`       | `repr(obj)`, REPL        | Developer display          |
| `__eq__`         | `obj1 == obj2`           | Value equality             |
| `__lt__`         | `obj1 < obj2`            | Less-than comparison       |
| `__len__`        | `len(obj)`               | Length/size                |
| `__add__`        | `obj1 + obj2`            | Addition                   |
| `__getitem__`    | `obj[key]`               | Index access               |
| `__setitem__`    | `obj[key] = val`         | Index assignment           |
| `__contains__`   | `item in obj`            | Membership test            |
| `__iter__`       | `for item in obj`        | Iteration                  |
| `__bool__`       | `bool(obj)`, `if obj:`   | Boolean truth value        |
| `__hash__`       | `hash(obj)`, dict keys   | Hashing for containers     |
| `__enter__`      | `with obj as x:`         | Context manager entry      |
| `__exit__`       | End of `with` block      | Context manager exit       |

## Next Steps

Continue to `03-encapsulation.md` to learn how Python handles data hiding and access control.
