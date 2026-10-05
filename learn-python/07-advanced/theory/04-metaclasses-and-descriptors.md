# Metaclasses and Descriptors

Metaclasses and descriptors are the machinery behind Python's most powerful frameworks. Django's ORM, SQLAlchemy, Pydantic, dataclasses — all use these under the hood.

---

## type: Python's Built-in Metaclass

Everything in Python is an object — including classes themselves.

```python
class Dog:
    def bark(self):
        return "Woof!"

fido = Dog()

# Normal: fido is an instance of Dog
print(type(fido))   # <class '__main__.Dog'>
print(isinstance(fido, Dog))  # True

# Surprising: Dog is an instance of type!
print(type(Dog))    # <class 'type'>
print(isinstance(Dog, type))  # True

# type is its own metaclass:
print(type(type))   # <class 'type'>
```

`type` is the metaclass of all classes. When Python sees a class definition, it calls `type` to create the class object.

```python
# These are equivalent:
class Dog:
    species = "Canis lupus familiaris"
    
    def bark(self):
        return "Woof!"

# Same class, created manually with type(name, bases, namespace):
Dog = type("Dog", (), {
    "species": "Canis lupus familiaris",
    "bark": lambda self: "Woof!",
})
```

---

## Custom Metaclasses

A metaclass is a class whose instances are classes. To create one, subclass `type`:

```python
class Meta(type):
    """A custom metaclass."""
    
    def __new__(mcs, name, bases, namespace):
        """Called when a class using this metaclass is DEFINED."""
        print(f"Creating class: {name}")
        return super().__new__(mcs, name, bases, namespace)
    
    def __init__(cls, name, bases, namespace):
        """Called after the class object is created."""
        print(f"Initializing class: {name}")
        super().__init__(name, bases, namespace)


class Animal(metaclass=Meta):  # Animal is created by Meta, not type
    pass

# Output when this module is loaded:
# Creating class: Animal
# Initializing class: Animal
```

### Real Use Case: Automatic Method Registration

```python
class PluginMeta(type):
    """Metaclass that automatically registers all subclasses."""
    
    _registry: dict[str, type] = {}
    
    def __new__(mcs, name, bases, namespace):
        cls = super().__new__(mcs, name, bases, namespace)
        # Register all non-base classes
        if bases:  # Don't register the base class itself
            mcs._registry[name.lower()] = cls
        return cls
    
    @classmethod
    def get_plugin(mcs, name: str):
        return mcs._registry.get(name.lower())


class Plugin(metaclass=PluginMeta):
    """Base plugin class."""
    def process(self, data):
        raise NotImplementedError


class JsonPlugin(Plugin):
    def process(self, data):
        import json
        return json.dumps(data)


class CsvPlugin(Plugin):
    def process(self, data):
        return ",".join(str(x) for x in data)


# Lookup by name — no if/elif chains needed!
plugin_class = PluginMeta.get_plugin("json")
result = plugin_class().process({"key": "value"})  # '{"key": "value"}'
```

---

## __init_subclass__ — The Modern Alternative

Since Python 3.6, `__init_subclass__` handles most metaclass use cases more cleanly:

```python
class Plugin:
    """Base class that auto-registers subclasses. No metaclass needed."""
    
    _registry: dict[str, type] = {}
    
    def __init_subclass__(cls, plugin_name: str | None = None, **kwargs):
        """Called automatically when a class inherits from Plugin."""
        super().__init_subclass__(**kwargs)
        name = plugin_name or cls.__name__.lower()
        Plugin._registry[name] = cls
    
    @classmethod
    def get(cls, name: str):
        return cls._registry.get(name)


class JsonPlugin(Plugin, plugin_name="json"):  # Keyword arg passed to __init_subclass__
    def process(self, data):
        import json
        return json.dumps(data)


class CsvPlugin(Plugin, plugin_name="csv"):
    def process(self, data):
        return ",".join(str(x) for x in data)


print(Plugin._registry)  # {'json': <class 'JsonPlugin'>, 'csv': <class 'CsvPlugin'>}
plugin = Plugin.get("json")()
print(plugin.process([1, 2, 3]))  # '[1, 2, 3]'
```

**Prefer `__init_subclass__` over metaclasses** for subclass registration, validation, and hooks. Metaclasses are only needed when you want to intercept class creation itself (class body parsing, attribute access on the class, etc.).

---

## Descriptors

Descriptors are objects that control attribute access on the class that owns them. They implement `__get__`, `__set__`, and/or `__delete__`.

```python
class Descriptor:
    """Template for a descriptor."""
    
    def __get__(self, obj, objtype=None):
        """Called when attribute is READ: obj.attr"""
        if obj is None:
            return self  # Accessing on class, not instance: MyClass.attr
        # Return the value for this instance
    
    def __set__(self, obj, value):
        """Called when attribute is WRITTEN: obj.attr = value"""
        # Validate and store the value
    
    def __delete__(self, obj):
        """Called when attribute is DELETED: del obj.attr"""
```

### How @property Works

`property` is a built-in descriptor. Understanding descriptors means understanding `@property`:

```python
class Circle:
    def __init__(self, radius: float):
        self._radius = radius
    
    @property
    def radius(self) -> float:
        return self._radius
    
    @radius.setter
    def radius(self, value: float):
        if value < 0:
            raise ValueError(f"Radius cannot be negative, got {value}")
        self._radius = value
    
    @property
    def area(self) -> float:
        import math
        return math.pi * self._radius ** 2


# What @property actually does (equivalent implementation):
class Circle2:
    def __init__(self, radius: float):
        self._radius = radius
    
    def _get_radius(self):
        return self._radius
    
    def _set_radius(self, value):
        if value < 0:
            raise ValueError(f"Radius cannot be negative")
        self._radius = value
    
    # property() is the descriptor!
    radius = property(_get_radius, _set_radius)
```

### Writing a Custom Descriptor: ValidatedAttribute

```python
class PositiveNumber:
    """
    Descriptor that enforces a positive number constraint.
    
    This replaces dozens of @property definitions — one descriptor
    can be reused for many attributes.
    """
    
    def __set_name__(self, owner, name):
        """Called when the descriptor is assigned to a class attribute."""
        self._name = name
        self._private = f"_validated_{name}"
    
    def __get__(self, obj, objtype=None):
        if obj is None:
            return self
        return getattr(obj, self._private, 0)
    
    def __set__(self, obj, value):
        if not isinstance(value, (int, float)):
            raise TypeError(
                f"{self._name} must be a number, got {type(value).__name__}"
            )
        if value <= 0:
            raise ValueError(
                f"{self._name} must be positive, got {value}"
            )
        setattr(obj, self._private, value)


class Product:
    price = PositiveNumber()      # One descriptor, used for multiple attributes
    stock = PositiveNumber()
    weight = PositiveNumber()
    
    def __init__(self, name: str, price: float, stock: int, weight: float):
        self.name = name
        self.price = price        # Calls PositiveNumber.__set__
        self.stock = stock
        self.weight = weight


p = Product("Widget", price=9.99, stock=100, weight=0.5)
print(p.price)  # 9.99

try:
    p.price = -5.0   # ValueError: price must be positive
except ValueError as e:
    print(e)
```

### Lazy Property Descriptor

```python
class lazy_property:
    """
    Descriptor that computes a value once and caches it.
    Like @property but only calls the function once.
    """
    
    def __init__(self, func):
        self._func = func
        self.__doc__ = func.__doc__
        self.__name__ = func.__name__
    
    def __set_name__(self, owner, name):
        self._name = name
    
    def __get__(self, obj, objtype=None):
        if obj is None:
            return self
        # Compute and store on the instance — next access goes to instance dict directly
        value = self._func(obj)
        obj.__dict__[self._name] = value  # Replace descriptor with concrete value
        return value


class DataProcessor:
    def __init__(self, data: list[int]):
        self.data = data
    
    @lazy_property
    def statistics(self) -> dict:
        """Computed once, then cached. Expensive computation."""
        print("[computing statistics...]")
        return {
            "count": len(self.data),
            "mean": sum(self.data) / len(self.data),
            "min": min(self.data),
            "max": max(self.data),
        }


proc = DataProcessor([1, 2, 3, 4, 5])
print(proc.statistics)  # [computing statistics...] {'count': 5, ...}
print(proc.statistics)  # No recomputation! {'count': 5, ...}
```

---

## Class Decorators as Metaclass Alternatives

Class decorators run after the class is created and can modify it — often simpler than metaclasses:

```python
def singleton(cls):
    """Class decorator that enforces the singleton pattern."""
    instances = {}
    
    def get_instance(*args, **kwargs):
        if cls not in instances:
            instances[cls] = cls(*args, **kwargs)
        return instances[cls]
    
    return get_instance


@singleton
class DatabasePool:
    def __init__(self):
        self.connections = []
        print("DatabasePool created")


pool1 = DatabasePool()  # "DatabasePool created"
pool2 = DatabasePool()  # Nothing — returns existing
print(pool1 is pool2)   # True
```

---

## When to Use What

| Need | Tool |
|------|------|
| Validate/transform attribute values | Descriptor |
| Computed property (read-only) | `@property` |
| Auto-register subclasses | `__init_subclass__` |
| Modify all classes in a hierarchy | Metaclass |
| Post-process one class | Class decorator |
| Cache expensive computed values | `lazy_property` descriptor or `functools.cached_property` |

```python
# Python 3.8+ has functools.cached_property built-in
from functools import cached_property

class DataProcessor:
    def __init__(self, data: list[int]):
        self.data = data
    
    @cached_property  # Computed once, cached in instance __dict__
    def sorted_data(self) -> list[int]:
        return sorted(self.data)
```
