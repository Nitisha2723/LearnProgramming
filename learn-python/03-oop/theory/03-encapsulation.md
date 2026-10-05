# Encapsulation in Python

## Python's Philosophy: "We're All Consenting Adults"

In Java, encapsulation is enforced by the compiler:
- `private` fields literally cannot be accessed from outside the class
- You must provide `getters` and `setters` if you want controlled access
- The compiler prevents misuse at compile time

Python takes a completely different approach. Python has **no true private attributes**.
Instead, it uses naming conventions and trusts developers to follow them.

The philosophy comes from Python's culture: developers are treated as competent adults
who understand what they're doing. If something is labeled as "internal", you should
respect that — not because the language forces you to, but because it's the right thing to do.

> "We're all consenting adults here" — common Python community saying

This doesn't mean Python has no encapsulation. It means encapsulation is achieved through
**conventions** and **tools** (like properties) rather than strict enforcement.

---

## Naming Conventions

### Public attributes — No prefix

No prefix means the attribute is part of the public API. External code is welcome to use it.

```python
class Car:
    def __init__(self, make: str, model: str):
        self.make = make    # Public — anyone can read/write
        self.model = model  # Public — part of the public interface
```

### Protected by convention — Single underscore `_name`

A single underscore prefix is a signal: "This is internal. You CAN access it, but you
probably shouldn't, and I might change it without warning."

It's just a convention — Python doesn't enforce anything. Experienced Python developers
respect it.

```python
class BankAccount:
    def __init__(self, owner: str, balance: float):
        self.owner = owner          # Public — owner name is public info
        self._balance = balance     # Protected — don't access directly
        self._transactions = []     # Protected — internal implementation
    
    def deposit(self, amount: float) -> None:
        """Public method — this IS the API."""
        if amount <= 0:
            raise ValueError("Deposit amount must be positive")
        self._balance += amount
        self._transactions.append(f"Deposit: +{amount}")

account = BankAccount("Alice", 1000.0)
print(account.owner)       # Fine — it's public

# These WORK but violate the convention:
print(account._balance)    # Works, but you "shouldn't"
account._balance = 999999  # Works, but you "shouldn't"
```

### Name mangling — Double underscore `__name`

Double underscore triggers Python's name mangling: the attribute name is changed to
`_ClassName__name`. This prevents accidental overwrites in subclasses.

```python
class SecureAccount:
    def __init__(self, owner: str, pin: str):
        self.owner = owner
        self.__pin = pin         # Stored as _SecureAccount__pin
        self.__balance = 0.0     # Stored as _SecureAccount__balance
    
    def authenticate(self, pin: str) -> bool:
        return self.__pin == pin  # Access works inside the class
    
    def deposit(self, amount: float, pin: str) -> None:
        if not self.authenticate(pin):
            raise PermissionError("Invalid PIN")
        self.__balance += amount

account = SecureAccount("Alice", "1234")

# Direct access fails with a normal name:
# account.__pin  # AttributeError: 'SecureAccount' object has no attribute '__pin'

# But you CAN still access it if you know the mangled name:
print(account._SecureAccount__pin)     # "1234" — still accessible!
print(account._SecureAccount__balance) # 0.0

# This is why Python says "no true private" — determined developers can always get in
```

**Name mangling is about preventing accidental conflicts, not security:**

```python
class Base:
    def __init__(self):
        self.__data = "base data"
    
    def get_data(self):
        return self.__data  # Accesses _Base__data

class Child(Base):
    def __init__(self):
        super().__init__()
        self.__data = "child data"  # Stored as _Child__data, NOT overwriting base!
    
    def get_child_data(self):
        return self.__data  # Accesses _Child__data

obj = Child()
print(obj.get_data())        # "base data"  — Base's __data untouched
print(obj.get_child_data())  # "child data" — Child's own __data
```

Without name mangling, `self.__data = "child data"` in the child class would accidentally
overwrite the parent's `__data`. Name mangling prevents this.

---

## Properties: The Pythonic Getter/Setter

Java requires explicit getter and setter methods:
```java
// Java
private double balance;
public double getBalance() { return balance; }
public void setBalance(double amount) { 
    if (amount < 0) throw new IllegalArgumentException("...");
    this.balance = amount; 
}
```

Python's `@property` decorator provides the same functionality with much cleaner syntax.
External code accesses the attribute like a normal attribute, but your class controls what happens.

### Basic `@property`

```python
class Temperature:
    def __init__(self, celsius: float):
        self._celsius = celsius
    
    @property
    def celsius(self) -> float:
        """Getter — called when you read temperature.celsius"""
        return self._celsius
    
    @celsius.setter
    def celsius(self, value: float) -> None:
        """Setter — called when you assign to temperature.celsius"""
        if value < -273.15:
            raise ValueError(f"Temperature below absolute zero: {value}")
        self._celsius = value
    
    @property
    def fahrenheit(self) -> float:
        """Computed property — no setter, because it's derived from celsius"""
        return self._celsius * 9/5 + 32
    
    @celsius.deleter
    def celsius(self) -> None:
        """Deleter — called when you del temperature.celsius"""
        print("Deleting temperature")
        del self._celsius

temp = Temperature(100.0)

# Reading — looks like attribute access, calls the getter
print(temp.celsius)      # 100.0
print(temp.fahrenheit)   # 212.0

# Writing — looks like attribute assignment, calls the setter with validation!
temp.celsius = 0.0
print(temp.celsius)      # 0.0

# This triggers the validation:
# temp.celsius = -300    # ValueError: Temperature below absolute zero: -300

# Fahrenheit is read-only — no setter defined
# temp.fahrenheit = 100  # AttributeError: can't set attribute
```

### Properties with computed values

```python
class Rectangle:
    def __init__(self, width: float, height: float):
        self.width = width    # Public — no computation needed
        self.height = height  # Public — no computation needed
    
    @property
    def area(self) -> float:
        """Computed from width and height — no stored value."""
        return self.width * self.height
    
    @property
    def perimeter(self) -> float:
        return 2 * (self.width + self.height)
    
    @property
    def is_square(self) -> bool:
        return self.width == self.height

rect = Rectangle(4.0, 3.0)
print(rect.area)       # 12.0
print(rect.perimeter)  # 14.0
print(rect.is_square)  # False

rect.width = 3.0
print(rect.area)       # 9.0   — automatically updated
print(rect.is_square)  # True
```

### The property evolution pattern

A great Python pattern: start with a simple public attribute, then add a property
if you need validation later — without changing the public API!

```python
# Version 1: Simple class, no validation needed yet
class Product:
    def __init__(self, name: str, price: float):
        self.name = name
        self.price = price   # Simple attribute

# External code uses it like this:
p = Product("Widget", 9.99)
print(p.price)    # 9.99
p.price = 14.99   # Direct assignment

# Version 2: Later, you need validation. Refactor to property.
# External code doesn't need to change!
class Product:
    def __init__(self, name: str, price: float):
        self.name = name
        self.price = price   # This calls the setter now!
    
    @property
    def price(self) -> float:
        return self._price
    
    @price.setter
    def price(self, value: float) -> None:
        if value < 0:
            raise ValueError(f"Price cannot be negative: {value}")
        self._price = value

# External code is unchanged:
p = Product("Widget", 9.99)
print(p.price)    # 9.99  — still works exactly the same way
p.price = 14.99   # Still works!
# p.price = -1    # Now raises ValueError — validation added without API change
```

This is powerful: Java requires you to plan for getters/setters upfront (or break the API
later). Python lets you start simple and add control when you need it.

---

## `__slots__` — Memory Optimization

By default, Python stores every object's attributes in a `__dict__` dictionary. This is
flexible but uses extra memory. For classes with many instances, you can use `__slots__`
to tell Python exactly which attributes an instance will have.

```python
class PointNormal:
    def __init__(self, x: float, y: float):
        self.x = x
        self.y = y

class PointSlotted:
    __slots__ = ('x', 'y')  # Only these attributes are allowed
    
    def __init__(self, x: float, y: float):
        self.x = x
        self.y = y

# Memory comparison (approximate)
import sys
p_normal = PointNormal(1.0, 2.0)
p_slotted = PointSlotted(1.0, 2.0)

print(sys.getsizeof(p_normal.__dict__))   # ~200 bytes (the dict overhead)
# p_slotted has no __dict__ at all — uses less memory

# With __slots__, you CANNOT add dynamic attributes:
p_normal.z = 3.0     # Works fine — dict accepts new keys
# p_slotted.z = 3.0  # AttributeError: 'PointSlotted' object has no attribute 'z'
```

### When to use `__slots__`

Use `__slots__` when:
- You're creating millions of instances (e.g., in a simulation or data processing pipeline)
- Memory usage is a concern
- You want to prevent accidental attribute creation

Don't use `__slots__` when:
- Your class needs `__dict__` (e.g., for serialization)
- You're using multiple inheritance (can get complicated)
- You're prototyping and the class structure might change

---

## Comparison: Java vs Python Encapsulation

```java
// Java — enforced by compiler
public class BankAccount {
    private double balance;   // Cannot be accessed from outside!
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double amount) {
        if (amount < 0) throw new IllegalArgumentException("...");
        this.balance = amount;
    }
}

BankAccount account = new BankAccount();
account.balance = 100;  // COMPILE ERROR — Java enforces this
```

```python
# Python — enforced by convention and properties
class BankAccount:
    def __init__(self, balance: float):
        self._balance = balance   # Convention: "don't touch directly"
    
    @property
    def balance(self) -> float:
        return self._balance
    
    @balance.setter
    def balance(self, amount: float) -> None:
        if amount < 0:
            raise ValueError("Balance cannot be negative")
        self._balance = amount

account = BankAccount(100.0)
account._balance = -100  # Python allows this — you're "breaking the rules"
account.balance = -100   # This raises ValueError — the property enforces it
```

**The key difference:**
- Java prevents access at compile time — it's impossible to bypass without reflection
- Python relies on developers respecting conventions
- Python's `@property` provides the same controlled access pattern with cleaner syntax
- Python's `__name` mangling makes accidental conflicts harder (but not impossible)

---

## Practical Guidelines

Follow these rules in your Python code:

1. **Start with public attributes** for simple data — no need to protect everything upfront
2. **Use `_single_underscore`** for attributes that are internal implementation details
3. **Use `__double_underscore`** only when you need to prevent subclass attribute conflicts
4. **Use `@property`** when you need validation, computation, or want to control access
5. **Use `@property` to add validation later** without changing the public API
6. **Use `__slots__`** only when you need memory optimization and have profiled the difference

```python
# Good example following these guidelines
class Circle:
    def __init__(self, radius: float):
        self.radius = radius   # Public — validation via property below
    
    @property
    def radius(self) -> float:
        return self._radius
    
    @radius.setter
    def radius(self, value: float) -> None:
        if value <= 0:
            raise ValueError(f"Radius must be positive, got {value}")
        self._radius = value
    
    @property
    def area(self) -> float:
        """Computed property — no setter."""
        import math
        return math.pi * self._radius ** 2
    
    @property
    def diameter(self) -> float:
        return self._radius * 2
    
    @diameter.setter
    def diameter(self, value: float) -> None:
        self.radius = value / 2  # Delegates to the radius setter for validation

c = Circle(5.0)
print(c.radius)    # 5.0
print(c.area)      # 78.54...
print(c.diameter)  # 10.0

c.diameter = 20.0
print(c.radius)    # 10.0  — updated via diameter setter
```

## Next Steps

Continue to `04-inheritance.md` to learn how Python implements inheritance, including
multiple inheritance and the Method Resolution Order.
