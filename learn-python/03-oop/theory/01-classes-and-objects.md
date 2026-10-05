# Classes and Objects in Python

## What is Object-Oriented Programming?

Object-Oriented Programming is a way of organizing code around "objects" — things that have
both data (attributes) and behavior (methods). Instead of writing a list of instructions, you
model your program as interacting objects.

Python supports OOP fully, but its approach is more flexible and dynamic than Java's.

---

## The `class` Keyword

A class is a blueprint for creating objects. Here's the basic syntax:

```python
class Dog:
    # class body goes here
    pass
```

Creating an object (an instance of the class):

```python
my_dog = Dog()       # Creates a Dog object
your_dog = Dog()     # Creates a different Dog object
```

Both `my_dog` and `your_dog` are `Dog` objects, but they are completely independent.

---

## A Real Example

```python
class BankAccount:
    
    def __init__(self, owner: str, balance: float = 0.0):
        self.owner = owner
        self.balance = balance
    
    def deposit(self, amount: float) -> None:
        self.balance += amount
        print(f"Deposited {amount}. New balance: {self.balance}")
    
    def withdraw(self, amount: float) -> None:
        if amount > self.balance:
            raise ValueError("Insufficient funds")
        self.balance -= amount

# Create instances
alice_account = BankAccount("Alice", 1000.0)
bob_account = BankAccount("Bob", 500.0)

alice_account.deposit(200.0)   # Deposited 200.0. New balance: 1200.0
bob_account.withdraw(100.0)    # Bob's balance is now 400.0

# They are completely independent objects
print(alice_account.balance)   # 1200.0
print(bob_account.balance)     # 400.0
```

---

## Everything is an Object in Python

This is one of the most important things to understand about Python: **everything is an object**.
Integers, strings, lists, functions, and even classes themselves are objects.

```python
# int is a class
x = 42
print(type(x))            # <class 'int'>
print(isinstance(x, int)) # True

# str is a class
name = "Alice"
print(type(name))             # <class 'str'>
print(name.upper())           # "ALICE" — calling a method on the object

# list is a class
numbers = [1, 2, 3]
print(type(numbers))          # <class 'list'>
print(numbers.append)         # <built-in method append of list object>

# A function is an object too!
def greet(name):
    return f"Hello, {name}!"

print(type(greet))            # <class 'function'>
greet_copy = greet            # Functions can be assigned to variables
print(greet_copy("Bob"))      # Hello, Bob!

# A class itself is an object
print(type(BankAccount))      # <class 'type'>
print(type(int))              # <class 'type'>
```

This is fundamentally different from Java where primitives (`int`, `boolean`, `double`) are
NOT objects. Python doesn't have this distinction — everything is an object.

---

## `__dict__` — Every Object's Attribute Dictionary

Python stores an object's attributes in a dictionary called `__dict__`. This is how Python's
dynamic attribute system works.

```python
class Person:
    def __init__(self, name: str, age: int):
        self.name = name
        self.age = age

alice = Person("Alice", 30)

# Inspect the object's attributes
print(alice.__dict__)          # {'name': 'Alice', 'age': 30}

# You can even add attributes dynamically (not recommended, but possible)
alice.city = "Berlin"
print(alice.__dict__)          # {'name': 'Alice', 'age': 30, 'city': 'Berlin'}

# This would NOT compile in Java — Python is much more dynamic
```

Java stores attributes in class-defined fields and doesn't allow dynamic attribute addition.
Python's `__dict__` makes objects much more flexible, but also means you need to be careful.

---

## `type()` and `isinstance()`

Two essential built-in functions for working with types:

```python
class Animal:
    pass

class Dog(Animal):
    pass

class Cat(Animal):
    pass

rex = Dog()
whiskers = Cat()

# type() returns the exact type
print(type(rex))                  # <class '__main__.Dog'>
print(type(rex) == Dog)           # True
print(type(rex) == Animal)        # False — rex is a Dog, not an Animal

# isinstance() checks the type AND the class hierarchy
print(isinstance(rex, Dog))       # True
print(isinstance(rex, Animal))    # True  — Dog IS-A Animal
print(isinstance(rex, Cat))       # False

# isinstance() accepts a tuple of types
print(isinstance(rex, (Dog, Cat))) # True — rex is one of these
print(isinstance(whiskers, (Dog, Cat))) # True

# Checking built-in types
print(isinstance(42, int))        # True
print(isinstance(42, (int, float))) # True
print(isinstance("hello", str))   # True
```

**Best practice:** Prefer `isinstance()` over `type() ==` in most cases, because
`isinstance()` respects inheritance. Use `type() ==` only when you need the exact type.

---

## Class Attributes vs Instance Attributes

Instance attributes belong to individual objects. Class attributes are shared by all instances.

```python
class Counter:
    # Class attribute — shared by ALL Counter instances
    count = 0
    
    def __init__(self, name: str):
        # Instance attribute — unique to each Counter object
        self.name = name
        # Incrementing the class attribute
        Counter.count += 1
    
    def get_info(self) -> str:
        return f"Counter '{self.name}', total counters: {Counter.count}"

c1 = Counter("First")
c2 = Counter("Second")
c3 = Counter("Third")

print(Counter.count)    # 3 — class attribute
print(c1.name)          # "First" — instance attribute
print(c2.name)          # "Second" — instance attribute

print(c1.get_info())    # Counter 'First', total counters: 3
print(c2.get_info())    # Counter 'Second', total counters: 3
```

**Caution with mutable class attributes:**

```python
class Team:
    members = []  # DANGEROUS: shared list!
    
    def add_member(self, name: str):
        self.members.append(name)  # Modifies the CLASS attribute!

team1 = Team()
team2 = Team()

team1.add_member("Alice")
print(team2.members)  # ['Alice'] — oops! Both teams share the same list

# Correct approach:
class TeamFixed:
    def __init__(self):
        self.members = []  # Instance attribute — each team has its own list
    
    def add_member(self, name: str):
        self.members.append(name)
```

---

## Instance Methods, Class Methods, and Static Methods

Python has three types of methods:

```python
class MathHelper:
    PI = 3.14159  # Class attribute
    
    def __init__(self, value: float):
        self.value = value
    
    # INSTANCE METHOD — takes self, can access instance AND class attributes
    def double(self) -> float:
        """Returns double the instance value."""
        return self.value * 2
    
    # CLASS METHOD — takes cls, can access class attributes but NOT instance attributes
    @classmethod
    def circle_area(cls, radius: float) -> float:
        """Calculates circle area using the class's PI value."""
        return cls.PI * radius ** 2
    
    # STATIC METHOD — takes neither self nor cls, completely independent
    @staticmethod
    def add(a: float, b: float) -> float:
        """A utility function that happens to live in this class."""
        return a + b


helper = MathHelper(5.0)

# Calling instance method — requires an instance
print(helper.double())                   # 10.0

# Calling class method — can call on class or instance
print(MathHelper.circle_area(3.0))       # 28.27...
print(helper.circle_area(3.0))           # Same result

# Calling static method — can call on class or instance
print(MathHelper.add(2.0, 3.0))          # 5.0
print(helper.add(2.0, 3.0))             # 5.0
```

**When to use each:**
- **Instance method:** When the method needs to access or modify `self`
- **Class method:** When the method relates to the class as a whole (e.g., factory methods)
- **Static method:** When the method is logically part of the class but doesn't need `self` or `cls`

### Factory Methods with `@classmethod`

A common use of class methods is as alternative constructors (factory methods):

```python
class Date:
    def __init__(self, year: int, month: int, day: int):
        self.year = year
        self.month = month
        self.day = day
    
    @classmethod
    def from_string(cls, date_string: str) -> "Date":
        """Create a Date from a string like '2024-01-15'."""
        year, month, day = map(int, date_string.split("-"))
        return cls(year, month, day)
    
    @classmethod
    def today(cls) -> "Date":
        """Create a Date representing today."""
        from datetime import datetime
        now = datetime.now()
        return cls(now.year, now.month, now.day)
    
    def __str__(self) -> str:
        return f"{self.year}-{self.month:02d}-{self.day:02d}"


# Multiple ways to create a Date object
d1 = Date(2024, 1, 15)
d2 = Date.from_string("2024-06-20")
d3 = Date.today()

print(d1)  # 2024-01-15
print(d2)  # 2024-06-20
```

---

## Summary

| Concept              | Python                                  | Java                               |
|----------------------|-----------------------------------------|------------------------------------|
| Class definition     | `class MyClass:`                        | `public class MyClass {}`          |
| Creating objects     | `obj = MyClass()`                       | `MyClass obj = new MyClass();`     |
| Everything is object | Yes (int, str, function, class)         | No (int, boolean are primitives)   |
| Attribute inspection | `obj.__dict__`                          | Reflection API (complex)           |
| Type checking        | `isinstance(obj, MyClass)`              | `obj instanceof MyClass`           |
| Class attributes     | Defined in class body                   | `static` fields                    |
| Static methods       | `@staticmethod`                         | `static` methods                   |
| Class methods        | `@classmethod` (no Java equivalent)     | `static` (similar but different)   |

## Next Steps

Continue to `02-init-and-self.md` to learn about `__init__`, `self`, and Python's
special dunder methods that make classes truly Pythonic.
