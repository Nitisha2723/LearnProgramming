# Abstract Classes and Protocols in Python

## The Problem: Enforcing a Contract

Sometimes duck typing is too loose. You want to guarantee that a class implements specific
methods, and you want to catch missing implementations early — ideally when the class is
defined, not when the method is called at runtime.

Python provides two solutions:
1. **Abstract Base Classes (ABC)** — enforced via inheritance
2. **Protocols** — enforced via structural typing (no inheritance required)

---

## Abstract Base Classes with the `abc` Module

The `abc` module provides `ABC` (Abstract Base Class) and `@abstractmethod` decorator.
A class that inherits from `ABC` and has abstract methods cannot be instantiated directly.

```python
from abc import ABC, abstractmethod

class Shape(ABC):
    """Abstract base class for all shapes."""
    
    @abstractmethod
    def area(self) -> float:
        """Calculate the area of the shape."""
        ...   # No implementation — subclasses MUST provide this
    
    @abstractmethod
    def perimeter(self) -> float:
        """Calculate the perimeter of the shape."""
        ...
    
    # Non-abstract method — shared by all subclasses
    def describe(self) -> str:
        return f"{type(self).__name__}: area={self.area():.2f}, perimeter={self.perimeter():.2f}"


class Circle(Shape):
    def __init__(self, radius: float):
        self.radius = radius
    
    def area(self) -> float:
        import math
        return math.pi * self.radius ** 2
    
    def perimeter(self) -> float:
        import math
        return 2 * math.pi * self.radius


class Rectangle(Shape):
    def __init__(self, width: float, height: float):
        self.width = width
        self.height = height
    
    def area(self) -> float:
        return self.width * self.height
    
    def perimeter(self) -> float:
        return 2 * (self.width + self.height)


# This works:
circle = Circle(5.0)
rect = Rectangle(3.0, 4.0)
print(circle.describe())   # Circle: area=78.54, perimeter=31.42
print(rect.describe())     # Rectangle: area=12.00, perimeter=14.00

# This raises TypeError IMMEDIATELY at instantiation:
# shape = Shape()   
# TypeError: Can't instantiate abstract class Shape with abstract methods area, perimeter
```

### Partial implementation — still abstract

If a subclass doesn't implement ALL abstract methods, it remains abstract:

```python
class SpecialShape(Shape):
    """Implements area but not perimeter — still abstract!"""
    
    def area(self) -> float:
        return 0.0
    
    # perimeter() is not implemented — SpecialShape is still abstract

# special = SpecialShape()  # TypeError!

class ConcreteSpecialShape(SpecialShape):
    def perimeter(self) -> float:
        return 0.0

concrete = ConcreteSpecialShape()   # Works! All abstract methods are now implemented
```

### Abstract class methods and abstract properties

```python
from abc import ABC, abstractmethod

class DataRepository(ABC):
    
    @classmethod
    @abstractmethod
    def get_name(cls) -> str:
        """Return the name of this repository type."""
        ...
    
    @property
    @abstractmethod
    def is_connected(self) -> bool:
        """Return whether this repository is connected."""
        ...
    
    @abstractmethod
    def fetch(self, id: int) -> dict:
        """Fetch a record by ID."""
        ...


class InMemoryRepository(DataRepository):
    _data: dict = {}
    
    @classmethod
    def get_name(cls) -> str:
        return "InMemoryRepository"
    
    @property
    def is_connected(self) -> bool:
        return True  # Always connected
    
    def fetch(self, id: int) -> dict:
        return self._data.get(id, {})
```

---

## `@abstractmethod` and Concrete Methods Together

Abstract classes can mix abstract and concrete (implemented) methods. This is a key
advantage over Java interfaces (before Java 8 default methods):

```python
from abc import ABC, abstractmethod

class Animal(ABC):
    def __init__(self, name: str):
        self.name = name
    
    @abstractmethod
    def speak(self) -> str:
        """Each animal speaks differently — must be implemented."""
        ...
    
    @abstractmethod
    def move(self) -> str:
        """Each animal moves differently — must be implemented."""
        ...
    
    # Concrete method — shared behavior for all animals
    def eat(self, food: str) -> str:
        return f"{self.name} eats {food}"
    
    # Concrete method that USES abstract methods
    def introduce(self) -> str:
        return f"I am {self.name}. {self.speak()} I move by {self.move()}."


class Dog(Animal):
    def speak(self) -> str:
        return "Woof!"
    
    def move(self) -> str:
        return "running on four legs"


class Bird(Animal):
    def speak(self) -> str:
        return "Tweet!"
    
    def move(self) -> str:
        return "flying with wings"


dog = Dog("Rex")
bird = Bird("Tweety")

print(dog.introduce())    # I am Rex. Woof! I move by running on four legs.
print(bird.introduce())   # I am Tweety. Tweet! I move by flying with wings.
print(dog.eat("bones"))   # Rex eats bones — concrete method inherited
```

---

## `Protocol` — Structural Typing Without Inheritance

`typing.Protocol` (Python 3.8+) lets you define a contract based on structure, not
inheritance. Any class with the required methods satisfies the Protocol — no explicit
inheritance needed.

```python
from typing import Protocol, runtime_checkable

@runtime_checkable  # Enables isinstance() checks
class Saveable(Protocol):
    def save(self, path: str) -> None:
        ...
    
    def load(self, path: str) -> None:
        ...


class Document:
    def __init__(self, content: str):
        self.content = content
    
    def save(self, path: str) -> None:
        print(f"Saving document to {path}")
    
    def load(self, path: str) -> None:
        print(f"Loading document from {path}")


class Image:
    def __init__(self, pixels: list):
        self.pixels = pixels
    
    def save(self, path: str) -> None:
        print(f"Saving image to {path}")
    
    def load(self, path: str) -> None:
        print(f"Loading image from {path}")


# Neither Document nor Image explicitly implements Saveable!
def backup_to_disk(item: Saveable, path: str) -> None:
    """Works with any Saveable — checked by type checker, not at runtime."""
    item.save(path)


doc = Document("Hello, World!")
img = Image([[255, 0, 0], [0, 255, 0]])

backup_to_disk(doc, "/tmp/doc.txt")    # Works — Document has save()
backup_to_disk(img, "/tmp/img.png")    # Works — Image has save()

# isinstance() works because of @runtime_checkable
print(isinstance(doc, Saveable))   # True — has save() and load()
print(isinstance(42, Saveable))    # False — int has no save()
```

---

## ABC vs Protocol: When to Use Which

| Feature                         | ABC                                     | Protocol                                    |
|---------------------------------|-----------------------------------------|---------------------------------------------|
| Enforcement mechanism           | Inheritance required                    | Structural (duck typing with types)         |
| Error caught when               | Class is instantiated                   | Type checking (mypy/pyright)                |
| Can mix concrete methods        | Yes                                     | Yes (but unusual)                           |
| Works with existing classes     | No — must change the class              | Yes — works with ANY class that has the methods |
| `isinstance()` check at runtime | Yes                                     | Yes (with `@runtime_checkable`)             |
| Best for                        | Base class for your own hierarchy       | Defining interfaces for unrelated classes  |

**Use ABC when:**
- You're designing a class hierarchy where subclasses share implementation
- You want to prevent instantiation of the abstract base
- You want to provide default implementations that subclasses can override

**Use Protocol when:**
- You need to work with classes you don't control
- You want to describe what "duck typing" expects, more formally
- You're writing a function that works with multiple unrelated types

---

## Comparison with Java Interfaces

```java
// Java interface
public interface Drawable {
    void draw();               // Must be implemented
    default String describe() {   // Optional default implementation (Java 8+)
        return "A drawable object";
    }
}

// Must explicitly declare implementation
public class Circle implements Drawable {
    public void draw() { System.out.println("Drawing circle"); }
}
```

```python
# Python Protocol — no explicit declaration needed
from typing import Protocol

class Drawable(Protocol):
    def draw(self) -> None: ...
    
    def describe(self) -> str:   # Default implementation
        return "A drawable object"

# No declaration needed — just have the method
class Circle:
    def draw(self) -> None:
        print("Drawing circle")

# Circle satisfies Drawable automatically!
```

```python
# Python ABC — more like Java's abstract class
from abc import ABC, abstractmethod

class Drawable(ABC):
    @abstractmethod
    def draw(self) -> None:
        ...
    
    def describe(self) -> str:   # Concrete default
        return "A drawable object"

class Circle(Drawable):          # Must explicitly inherit
    def draw(self) -> None:
        print("Drawing circle")
```

---

## Type Hints in OOP

Modern Python uses type hints to document what types are expected. This doesn't affect
runtime behavior but helps with documentation, IDEs, and static type checkers.

```python
from __future__ import annotations   # Allows forward references
from typing import Optional, Union, List
from abc import ABC, abstractmethod

class Employee(ABC):
    def __init__(self, name: str, employee_id: str, department: str):
        self.name: str = name
        self.employee_id: str = employee_id
        self.department: str = department
        self._manager: Optional[Employee] = None  # Can be None
    
    @property
    def manager(self) -> Optional[Employee]:
        return self._manager
    
    @manager.setter
    def manager(self, emp: Optional[Employee]) -> None:
        self._manager = emp
    
    @abstractmethod
    def calculate_pay(self) -> float:
        ...
    
    def get_payslip(self) -> str:
        return f"{self.name}: ${self.calculate_pay():.2f}"


class SalariedEmployee(Employee):
    def __init__(self, name: str, emp_id: str, department: str, annual_salary: float):
        super().__init__(name, emp_id, department)
        self.annual_salary: float = annual_salary
    
    def calculate_pay(self) -> float:
        return self.annual_salary / 12  # Monthly pay


class HourlyEmployee(Employee):
    def __init__(self, name: str, emp_id: str, department: str, hourly_rate: float):
        super().__init__(name, emp_id, department)
        self.hourly_rate: float = hourly_rate
        self._hours_worked: float = 0.0
    
    def log_hours(self, hours: float) -> None:
        if hours < 0:
            raise ValueError("Hours cannot be negative")
        self._hours_worked += hours
    
    def calculate_pay(self) -> float:
        return self.hourly_rate * self._hours_worked


def process_payroll(employees: List[Employee]) -> float:
    """Type hint ensures we get a list of Employee objects."""
    total = 0.0
    for emp in employees:
        payslip = emp.get_payslip()
        print(payslip)
        total += emp.calculate_pay()
    return total


# Usage with type safety
alice = SalariedEmployee("Alice", "E001", "Engineering", 84_000)
bob = HourlyEmployee("Bob", "E002", "Operations", 25.0)
bob.log_hours(80)  # 80 hours in a period

employees: List[Employee] = [alice, bob]
total = process_payroll(employees)
print(f"\nTotal payroll: ${total:.2f}")
```

---

## Summary

```
Need to enforce a contract?
  ↓
Are the classes in your own codebase?
  ├─ YES → Do they share implementation? → YES: Use ABC
  │                                      → NO:  Use Protocol
  └─ NO  → Use Protocol (can't change external classes)
```

## Next Steps

Continue to `07-pythonic-oop.md` for the final topic: modern Python patterns like
dataclasses, context managers, descriptors, and best practices.
