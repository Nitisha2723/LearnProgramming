# Polymorphism in Python

## What is Polymorphism?

Polymorphism means "many forms". In OOP, it means that the same operation can work on
different types of objects. A `draw()` method might behave differently for a `Circle` vs
a `Rectangle`, but you can call it the same way on both.

Java achieves polymorphism through interfaces and declared types.
Python achieves it primarily through **duck typing**.

---

## Duck Typing: "If it walks like a duck and quacks like a duck..."

The full saying is:
> "If it walks like a duck and quacks like a duck, then it's a duck."

In Python, if an object has the methods and attributes you need, you can use it — regardless
of its actual type or inheritance hierarchy. You don't need to declare that it implements
an interface. You just call the method and if the method exists, it works.

```python
# No shared parent class, no common interface declaration
class Dog:
    def speak(self) -> str:
        return "Woof!"
    
    def name(self) -> str:
        return "Dog"


class Cat:
    def speak(self) -> str:
        return "Meow!"
    
    def name(self) -> str:
        return "Cat"


class Robot:
    def speak(self) -> str:
        return "Beep boop!"
    
    def name(self) -> str:
        return "Robot"


def make_it_speak(anything) -> None:
    """Works with ANY object that has a speak() method."""
    print(f"{anything.name()} says: {anything.speak()}")


# Dog, Cat, and Robot share NO common parent (other than object)
# Python doesn't care — as long as they have speak() and name(), this works
make_it_speak(Dog())    # Dog says: Woof!
make_it_speak(Cat())    # Cat says: Meow!
make_it_speak(Robot())  # Robot says: Beep boop!

# Create a list of mixed types — all have speak()
animals = [Dog(), Cat(), Dog(), Robot(), Cat()]
for creature in animals:
    print(creature.speak())   # Works perfectly!
```

In Java, you'd need all three to implement a common `Speakable` interface.
Python just calls the method — if it exists, great. If not, you get an `AttributeError`.

---

## Why Duck Typing is Powerful

### No upfront design required

You can write a function that works with objects you haven't even created yet:

```python
def total_length(items) -> int:
    """Works with ANY iterable of objects that have __len__."""
    return sum(len(item) for item in items)

print(total_length(["hello", "world", "python"]))  # 5 + 5 + 6 = 16
print(total_length([[1, 2, 3], [4, 5]]))            # 3 + 2 = 5
print(total_length([{"a": 1}, {"b": 2, "c": 3}]))  # 1 + 2 = 3

# Your custom class works too:
class MyCollection:
    def __init__(self):
        self._data = [1, 2, 3, 4, 5]
    
    def __len__(self) -> int:
        return len(self._data)

print(total_length([MyCollection()]))               # 5
```

The function works without any knowledge of your `MyCollection` class.

### Built-in functions use duck typing

Python's built-ins work with any object that implements the right methods:

```python
class WordCollection:
    def __init__(self, words: list):
        self._words = words
    
    def __len__(self) -> int:
        return len(self._words)
    
    def __iter__(self):
        return iter(self._words)
    
    def __contains__(self, item: str) -> bool:
        return item in self._words
    
    def __getitem__(self, index: int) -> str:
        return self._words[index]


words = WordCollection(["python", "java", "kotlin", "go"])

print(len(words))           # 4 — uses __len__
print("python" in words)    # True — uses __contains__
print(words[0])             # "python" — uses __getitem__
print(list(words))          # ["python", ...] — uses __iter__

for word in words:           # Iteration — uses __iter__
    print(word.upper())
```

### Tradeoffs of duck typing

**Pros:**
- Very flexible — code works with types defined later
- Less boilerplate — no interface declarations needed
- Encourages writing code against behavior, not types

**Cons:**
- Errors are caught at runtime, not compile time
- Harder to know what a function expects without reading the code
- Can lead to confusing errors if the wrong type is passed

```python
# Duck typing error — happens at runtime, not when you read the code
def process(item):
    return item.process()   # What should item have? No way to tell from the signature!

# Compare with type hints (modern Python):
from typing import Protocol

class Processable(Protocol):
    def process(self) -> str:
        ...

def process(item: Processable) -> str:
    return item.process()   # Clear contract! Any object with process() works.
```

---

## `isinstance()` and Duck Typing

Sometimes you need to check types even in duck typing code. Use `isinstance()` for this,
not direct type comparison:

```python
def format_value(value) -> str:
    """Format a value for display."""
    if isinstance(value, bool):          # Check bool BEFORE int (bool is subclass of int!)
        return "yes" if value else "no"
    elif isinstance(value, int):
        return f"{value:,}"              # 1,234,567 format
    elif isinstance(value, float):
        return f"{value:.2f}"            # 3.14 format
    elif isinstance(value, str):
        return f'"{value}"'
    elif isinstance(value, (list, tuple)):
        items = ", ".join(format_value(v) for v in value)
        return f"[{items}]"
    else:
        return str(value)                # Fallback

print(format_value(True))         # yes
print(format_value(1234567))      # 1,234,567
print(format_value(3.14159))      # 3.14
print(format_value("hello"))      # "hello"
print(format_value([1, True, 3.14]))  # [1,234,567, yes, 3.14]
```

### EAFP vs LBYL

Python culture has two philosophies for handling potentially missing methods:

**LBYL (Look Before You Leap)** — Check before calling:
```python
def get_length(item) -> int:
    if hasattr(item, "__len__"):     # Check if __len__ exists
        return len(item)
    return 0
```

**EAFP (Easier to Ask Forgiveness than Permission)** — Try it and handle failure:
```python
def get_length(item) -> int:
    try:
        return len(item)            # Just try it
    except TypeError:
        return 0                    # Handle if it doesn't work
```

Python culture generally prefers EAFP — it's more Pythonic and often faster.

---

## Protocols (Python 3.8+): Structural Typing

Protocols let you express duck typing formally with type hints. A Protocol defines
what attributes/methods an object must have, without requiring inheritance.

```python
from typing import Protocol, runtime_checkable

@runtime_checkable
class Drawable(Protocol):
    def draw(self) -> None:
        """Draw the object."""
        ...   # Protocol method body is just ...
    
    def get_area(self) -> float:
        """Return the area."""
        ...


class Circle:
    def __init__(self, radius: float):
        self.radius = radius
    
    def draw(self) -> None:
        print(f"Drawing circle with radius {self.radius}")
    
    def get_area(self) -> float:
        import math
        return math.pi * self.radius ** 2


class Rectangle:
    def __init__(self, width: float, height: float):
        self.width = width
        self.height = height
    
    def draw(self) -> None:
        print(f"Drawing {self.width}x{self.height} rectangle")
    
    def get_area(self) -> float:
        return self.width * self.height


# Note: Circle and Rectangle DON'T inherit from Drawable!
def render_all(shapes: list[Drawable]) -> float:
    """Render all shapes and return total area."""
    total = 0.0
    for shape in shapes:
        shape.draw()
        total += shape.get_area()
    return total


shapes = [Circle(5.0), Rectangle(3.0, 4.0)]
total = render_all(shapes)
print(f"Total area: {total:.2f}")

# isinstance() works with @runtime_checkable:
print(isinstance(Circle(1.0), Drawable))     # True — has draw() and get_area()
print(isinstance("hello", Drawable))         # False — no draw() method
```

The `@runtime_checkable` decorator lets you use `isinstance()` with the Protocol at
runtime. Without it, Protocols work only for static type checking (mypy, pyright).

---

## Operator Overloading Through Dunder Methods

Python lets you define how your objects work with operators. This is a form of
polymorphism — the `+` operator means different things for different types.

```python
from __future__ import annotations  # Allows string-based type hints for forward references

class Money:
    """Represents an amount of money in a specific currency."""
    
    def __init__(self, amount: float, currency: str = "EUR"):
        self.amount = round(amount, 2)
        self.currency = currency
    
    def __add__(self, other: Money) -> Money:
        if self.currency != other.currency:
            raise ValueError(f"Cannot add {self.currency} and {other.currency}")
        return Money(self.amount + other.amount, self.currency)
    
    def __sub__(self, other: Money) -> Money:
        if self.currency != other.currency:
            raise ValueError(f"Cannot subtract currencies")
        return Money(self.amount - other.amount, self.currency)
    
    def __mul__(self, factor: float) -> Money:
        return Money(self.amount * factor, self.currency)
    
    def __rmul__(self, factor: float) -> Money:
        """Handles factor * money (right multiplication)."""
        return self.__mul__(factor)
    
    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Money):
            return NotImplemented
        return self.amount == other.amount and self.currency == other.currency
    
    def __lt__(self, other: Money) -> bool:
        if self.currency != other.currency:
            raise ValueError("Cannot compare different currencies")
        return self.amount < other.amount
    
    def __repr__(self) -> str:
        return f"Money({self.amount:.2f} {self.currency})"
    
    def __str__(self) -> str:
        return f"{self.amount:.2f} {self.currency}"


# These feel natural — just like built-in types
price = Money(10.00)
tax = Money(0.19)

total = price + tax              # Money.__add__
print(total)                     # 10.19 EUR

discount = Money(2.00)
final_price = total - discount   # Money.__sub__
print(final_price)               # 8.19 EUR

doubled = final_price * 2        # Money.__mul__
print(doubled)                   # 16.38 EUR

also_doubled = 2 * final_price   # Money.__rmul__ (reversed operands)
print(also_doubled)              # 16.38 EUR

prices = [Money(5.0), Money(15.0), Money(10.0)]
print(sorted(prices))            # [5.00 EUR, 10.00 EUR, 15.00 EUR]
```

---

## Real Example: `len()` and Duck Typing

Python's built-in `len()` works with any object that implements `__len__`. This is
duck typing in the standard library:

```python
# All of these work with len() — completely different types!
print(len("hello"))           # 5  — str.__len__
print(len([1, 2, 3]))         # 3  — list.__len__
print(len({"a": 1, "b": 2})) # 2  — dict.__len__
print(len((1, 2, 3, 4)))      # 4  — tuple.__len__
print(len({1, 2, 3}))         # 3  — set.__len__

import numpy as np
arr = np.array([1, 2, 3, 4, 5])
print(len(arr))               # 5  — numpy.ndarray.__len__

# Your custom class:
class Sentence:
    def __init__(self, text: str):
        self._words = text.split()
    
    def __len__(self) -> int:
        return len(self._words)

sentence = Sentence("Hello beautiful world")
print(len(sentence))          # 3  — Sentence.__len__
```

The `len()` function literally just calls `obj.__len__()`. Any object that provides
`__len__` automatically works with `len()`. That's duck typing built into Python's core.

---

## Summary: Duck Typing vs Java Interfaces

| Aspect                 | Python Duck Typing                 | Java Interfaces                    |
|------------------------|------------------------------------|------------------------------------|
| Type declaration       | Not required                       | Class must `implement` interface   |
| Error detection        | Runtime                            | Compile time                       |
| Flexibility            | Very high — works with future types| Lower — must declare all interfaces|
| Expressiveness         | Informal — relies on docs/hints    | Formal — contract in code          |
| Formal version         | `Protocol` (Python 3.8+)           | Interfaces                         |
| Operator overloading   | Via dunder methods (`__add__`, etc.)| Via method overloading             |

## Next Steps

Continue to `06-abstract-classes-and-protocols.md` to learn about ABCs and how to
create formal contracts in Python when duck typing alone isn't enough.
