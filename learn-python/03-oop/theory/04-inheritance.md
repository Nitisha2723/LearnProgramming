# Inheritance in Python

## Basic Syntax

Python uses parentheses after the class name to specify the parent class:

```python
class Animal:
    """Base class for all animals."""
    
    def __init__(self, name: str, sound: str):
        self.name = name
        self.sound = sound
    
    def speak(self) -> str:
        return f"{self.name} says {self.sound}"
    
    def __str__(self) -> str:
        return f"{type(self).__name__}({self.name!r})"


class Dog(Animal):     # Dog inherits from Animal
    def fetch(self) -> str:
        return f"{self.name} fetches the ball!"


class Cat(Animal):     # Cat inherits from Animal
    def purr(self) -> str:
        return f"{self.name} purrs..."


rex = Dog("Rex", "Woof")
whiskers = Cat("Whiskers", "Meow")

print(rex.speak())     # "Rex says Woof" — inherited method
print(rex.fetch())     # "Rex fetches the ball!" — own method
print(whiskers.speak()) # "Whiskers says Meow" — inherited method
print(whiskers.purr()) # "Whiskers purrs..."

print(isinstance(rex, Dog))     # True
print(isinstance(rex, Animal))  # True — IS-A relationship
print(isinstance(rex, Cat))     # False
```

Every class in Python implicitly inherits from `object` if no parent is specified:
```python
class Standalone:
    pass

# Same as:
class Standalone(object):
    pass

print(Standalone.__bases__)  # (<class 'object'>,)
```

---

## `super()` — Calling Parent Methods

Use `super()` to call a method from the parent class. This is essential in `__init__`
to ensure the parent class is properly initialized.

```python
class Animal:
    def __init__(self, name: str, age: int):
        self.name = name
        self.age = age
        print(f"Animal.__init__ called for {name}")
    
    def describe(self) -> str:
        return f"{self.name}, age {self.age}"


class Dog(Animal):
    def __init__(self, name: str, age: int, breed: str):
        # MUST call super().__init__() to initialize Animal's attributes
        super().__init__(name, age)   # Calls Animal.__init__
        self.breed = breed            # Add Dog-specific attribute
        print(f"Dog.__init__ called, breed={breed}")
    
    def describe(self) -> str:
        # Call parent's describe() and extend it
        base_description = super().describe()
        return f"{base_description}, breed: {self.breed}"


rex = Dog("Rex", 3, "Labrador")
# Output:
# Animal.__init__ called for Rex
# Dog.__init__ called, breed=Labrador

print(rex.describe())
# "Rex, age 3, breed: Labrador"
```

### `super()` without arguments

In Python 3, `super()` with no arguments automatically knows which class and instance
to use. (In Python 2, you had to write `super(Dog, self).__init__(...)`)

```python
# Python 3 — clean
super().__init__(name, age)

# Python 2 style (avoid this)
super(Dog, self).__init__(name, age)
```

### Calling parent methods other than `__init__`

```python
class Vehicle:
    def start(self) -> str:
        return "Engine started"
    
    def stop(self) -> str:
        return "Engine stopped"


class ElectricVehicle(Vehicle):
    def start(self) -> str:
        # Extend parent behavior
        parent_msg = super().start()
        return f"{parent_msg} (silently, electric motor engaged)"
    
    def charge(self) -> str:
        return "Charging battery..."


ev = ElectricVehicle()
print(ev.start())   # "Engine started (silently, electric motor engaged)"
print(ev.stop())    # "Engine stopped" — inherited unchanged
```

---

## Method Resolution Order (MRO)

The MRO determines the order in which Python looks for methods when a class has multiple
levels of inheritance. Python uses the **C3 linearization algorithm**.

```python
class A:
    def hello(self):
        return "Hello from A"

class B(A):
    pass  # Doesn't override hello

class C(A):
    def hello(self):
        return "Hello from C"

class D(B, C):
    pass

d = D()
print(d.hello())        # "Hello from C" — why?

# Check the MRO
print(D.__mro__)
# (<class 'D'>, <class 'B'>, <class 'C'>, <class 'A'>, <class 'object'>)

# Python searches in this order: D → B → C → A → object
# D doesn't have hello() → try B → B doesn't have hello() → try C → C has hello()!
```

You can also see the MRO using `help()` or the `mro()` method:
```python
print([cls.__name__ for cls in D.__mro__])
# ['D', 'B', 'C', 'A', 'object']
```

---

## Multiple Inheritance

Python supports inheriting from multiple classes simultaneously. Java doesn't allow this
for classes (only interfaces).

```python
class Flyable:
    def fly(self) -> str:
        return "Flying!"
    
    def move(self) -> str:
        return "Moving through air"


class Swimmable:
    def swim(self) -> str:
        return "Swimming!"
    
    def move(self) -> str:
        return "Moving through water"


class Duck(Flyable, Swimmable):
    def quack(self) -> str:
        return "Quack!"
    
    def move(self) -> str:
        # Explicitly choose which parent's move() to use
        # Or combine them
        return "Moving on land (can also fly or swim)"

donald = Duck()
print(donald.fly())    # "Flying!" — from Flyable
print(donald.swim())   # "Swimming!" — from Swimmable
print(donald.quack())  # "Quack!" — own method
print(donald.move())   # "Moving on land..."  — own method overrides both parents

print([cls.__name__ for cls in Duck.__mro__])
# ['Duck', 'Flyable', 'Swimmable', 'object']
```

---

## The Diamond Problem and MRO

The diamond problem occurs when a class inherits from two classes that both inherit
from a common ancestor. Python's C3 MRO algorithm resolves this correctly.

```python
class Base:
    def greet(self) -> str:
        return "Hello from Base"


class Left(Base):
    def greet(self) -> str:
        return f"Hello from Left, then: {super().greet()}"


class Right(Base):
    def greet(self) -> str:
        return f"Hello from Right, then: {super().greet()}"


class Diamond(Left, Right):
    def greet(self) -> str:
        return f"Hello from Diamond, then: {super().greet()}"


d = Diamond()
print(d.greet())
# "Hello from Diamond, then: Hello from Left, then: Hello from Right, then: Hello from Base"

print([cls.__name__ for cls in Diamond.__mro__])
# ['Diamond', 'Left', 'Right', 'Base', 'object']
```

**How MRO works here:**
1. `Diamond.greet()` calls `super().greet()`
2. `super()` in Diamond context means "next class in MRO" = `Left`
3. `Left.greet()` calls `super().greet()`
4. `super()` in Left context (in Diamond's MRO) means `Right`
5. `Right.greet()` calls `super().greet()`
6. `super()` in Right context (in Diamond's MRO) means `Base`
7. `Base.greet()` returns the string

**The key insight:** `super()` doesn't mean "my parent". It means "the next class in
the MRO of the object being created." This ensures each class in the hierarchy is called
exactly once, even in complex diamond inheritance.

---

## Mixins — Practical Multiple Inheritance

A mixin is a class that provides specific functionality meant to be mixed into other classes.
Mixins don't represent a logical IS-A relationship — they add capabilities.

```python
class JSONMixin:
    """Adds JSON serialization to any class."""
    
    def to_json(self) -> str:
        import json
        return json.dumps(self.__dict__)
    
    @classmethod
    def from_json(cls, json_string: str) -> "JSONMixin":
        import json
        data = json.loads(json_string)
        obj = cls.__new__(cls)
        obj.__dict__.update(data)
        return obj


class LogMixin:
    """Adds logging to any class."""
    
    def log(self, message: str) -> None:
        print(f"[{type(self).__name__}] {message}")


class ValidateMixin:
    """Adds validation capability to any class."""
    
    def validate(self) -> bool:
        """Override in subclass to add validation logic."""
        return True
    
    def validate_or_raise(self) -> None:
        if not self.validate():
            raise ValueError(f"Validation failed for {self}")


# Use mixins to compose behavior:
class User(JSONMixin, LogMixin, ValidateMixin):
    def __init__(self, name: str, email: str):
        self.name = name
        self.email = email
    
    def validate(self) -> bool:
        return "@" in self.email and len(self.name) > 0


user = User("Alice", "alice@example.com")
user.log("User created")                    # [User] User created
print(user.to_json())                       # {"name": "Alice", "email": "alice@example.com"}
user.validate_or_raise()                    # No error — valid

invalid_user = User("", "not-an-email")
# invalid_user.validate_or_raise()          # Raises ValueError
```

**Mixin naming convention:** Conventionally, mixins are named with "Mixin" suffix to make
their purpose clear.

---

## `__init_subclass__` — Hook Called When Subclass is Created

`__init_subclass__` is called on a class when it is subclassed. It's a clean way to
register subclasses or validate the class hierarchy.

```python
class Plugin:
    """Base class for all plugins. Automatically tracks all subclasses."""
    
    _registry: dict = {}
    
    def __init_subclass__(cls, plugin_name: str = "", **kwargs):
        super().__init_subclass__(**kwargs)
        if plugin_name:
            Plugin._registry[plugin_name] = cls
            print(f"Registered plugin: {plugin_name} -> {cls.__name__}")


class DatabasePlugin(Plugin, plugin_name="database"):
    def connect(self) -> str:
        return "Connected to database"


class CachePlugin(Plugin, plugin_name="cache"):
    def get(self, key: str) -> str:
        return f"Cache hit for {key}"


print(Plugin._registry)
# {'database': <class 'DatabasePlugin'>, 'cache': <class 'CachePlugin'>}

# Use the registry to instantiate plugins by name:
plugin_class = Plugin._registry["database"]
plugin = plugin_class()
print(plugin.connect())   # "Connected to database"
```

---

## When to Inherit vs When to Compose

**Inheritance** is appropriate when:
- The subclass IS-A specialized version of the base class (Dog IS-A Animal)
- You want to extend or override existing behavior
- The hierarchy is shallow (1-2 levels deep is usually fine, 4+ gets complex)

**Composition** (has-a) is appropriate when:
- The class USES or CONTAINS another class
- You need flexibility to swap implementations
- The relationship is not truly IS-A

```python
# Composition example — Engine is NOT a type of Car, it's PART of a Car
class Engine:
    def __init__(self, horsepower: int):
        self.horsepower = horsepower
    
    def start(self) -> str:
        return f"Engine ({self.horsepower}hp) started"
    
    def stop(self) -> str:
        return "Engine stopped"


class GPS:
    def navigate(self, destination: str) -> str:
        return f"Navigating to {destination}"


class Car:
    def __init__(self, make: str, horsepower: int):
        self.make = make
        self._engine = Engine(horsepower)   # HAS-A Engine
        self._gps = GPS()                   # HAS-A GPS
    
    def start(self) -> str:
        return self._engine.start()
    
    def navigate(self, destination: str) -> str:
        return self._gps.navigate(destination)

car = Car("Toyota", 150)
print(car.start())              # "Engine (150hp) started"
print(car.navigate("Berlin"))   # "Navigating to Berlin"

# Easy to swap: replace GPS with a different implementation
class OfflineGPS:
    def navigate(self, destination: str) -> str:
        return f"Using offline maps to navigate to {destination}"
```

**Python tip:** "Favor composition over inheritance" is good advice in both Java and Python.
Use inheritance when the IS-A relationship is genuinely clear and stable.

---

## Summary

| Feature             | Python                               | Java                          |
|---------------------|--------------------------------------|-------------------------------|
| Single inheritance  | `class Child(Parent):`               | `class Child extends Parent`  |
| Multiple inheritance| `class C(A, B):`                     | Not allowed (classes)         |
| Call parent method  | `super().method()`                   | `super.method()`              |
| Constructor chain   | `super().__init__(...)`              | `super(...)`                  |
| Method lookup       | C3 MRO (left-to-right, depth-first)  | Linear (single chain)         |
| Mixins              | Multiple inheritance + convention    | Interfaces with default methods|

## Next Steps

Continue to `05-polymorphism.md` to learn about duck typing — Python's powerful
alternative to Java's interface-based polymorphism.
