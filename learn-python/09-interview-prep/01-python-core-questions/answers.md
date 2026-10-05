# Python Core Interview Questions — 50 Q&A

---

## Q1. What is the difference between list, tuple, set, and dict?

**Answer:**

| Type | Ordered | Mutable | Duplicates | Key-Value |
|------|---------|---------|------------|-----------|
| `list` | Yes | Yes | Yes | No |
| `tuple` | Yes | No | Yes | No |
| `set` | No | Yes | No | No |
| `dict` | Yes (3.7+) | Yes | Keys: No | Yes |

```python
my_list  = [1, 2, 2, 3]          # ordered, mutable, allows dups
my_tuple = (1, 2, 2, 3)          # ordered, immutable, allows dups
my_set   = {1, 2, 3}             # unordered, mutable, no dups
my_dict  = {"a": 1, "b": 2}      # key-value, ordered (Py 3.7+)
```

Use **list** for sequences you'll modify. Use **tuple** for fixed data (coordinates,
named returns). Use **set** for membership tests and deduplication. Use **dict**
for key-value lookups.

---

## Q2. What are Python's mutable and immutable types?

**Answer:**

**Immutable** (cannot be changed after creation): `int`, `float`, `complex`,
`bool`, `str`, `bytes`, `tuple`, `frozenset`.

**Mutable** (can be changed in place): `list`, `dict`, `set`, `bytearray`,
most custom objects.

```python
# Immutable — rebinding creates a new object
x = "hello"
x += " world"   # x now points to a NEW string object

# Mutable — modified in place
lst = [1, 2, 3]
lst.append(4)   # same list object, new element
```

**Why it matters:** mutable objects should not be used as dict keys or set members.
Mutability also affects how function arguments behave (pass-by-object-reference).

---

## Q3. What is the difference between == and is?

**Answer:**

- `==` tests **value equality** (calls `__eq__`)
- `is` tests **identity** — whether two names point to the **same object** in memory

```python
a = [1, 2, 3]
b = [1, 2, 3]

a == b    # True  — same values
a is b    # False — different objects

c = a
a is c    # True  — same object
```

**CPython interning:** small integers (-5 to 256) and short strings are interned,
so `is` can return `True` unexpectedly. Never use `is` to compare values; only
use it for `None`, `True`, `False`.

```python
x is None   # correct way to check for None
x == None   # works but triggers __eq__; avoid
```

---

## Q4. What are Python decorators?

**Answer:**

A decorator is a callable that takes a function (or class) and returns a
modified version. They use the `@` syntax as syntactic sugar.

```python
def log_call(func):
    def wrapper(*args, **kwargs):
        print(f"Calling {func.__name__}")
        result = func(*args, **kwargs)
        print(f"Done {func.__name__}")
        return result
    return wrapper

@log_call                    # equivalent to: greet = log_call(greet)
def greet(name):
    print(f"Hello, {name}!")

greet("Alice")
# Calling greet
# Hello, Alice!
# Done greet
```

Use `functools.wraps` to preserve the original function's metadata:

```python
import functools

def log_call(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        print(f"Calling {func.__name__}")
        return func(*args, **kwargs)
    return wrapper
```

Common built-in decorators: `@property`, `@staticmethod`, `@classmethod`,
`@functools.lru_cache`, `@dataclasses.dataclass`.

---

## Q5. What is a generator and how does it differ from a regular function?

**Answer:**

A **generator** is a function that uses `yield` to produce values one at a time,
suspending and resuming its state between calls. It returns a **generator object**
(an iterator), not a list.

```python
def fibonacci():
    a, b = 0, 1
    while True:
        yield a
        a, b = b, a + b

gen = fibonacci()
print(next(gen))  # 0
print(next(gen))  # 1
print(next(gen))  # 1
```

**Key differences from a regular function:**

| Aspect | Function | Generator |
|--------|----------|-----------|
| Returns | Once, a value | Many values lazily |
| Memory | Builds full result | One item at a time |
| State | Restarted each call | Suspended between yields |
| Use case | Compute & return | Stream, pipeline, infinite sequences |

**Generator expression** (like list comprehension but lazy):
```python
squares = (x**2 for x in range(1_000_000))  # no memory allocation
```

---

## Q6. What is the GIL (Global Interpreter Lock)?

**Answer:**

The **GIL** is a mutex in CPython that allows only one thread to execute Python
bytecode at a time, even on multi-core hardware.

**Implications:**
- CPU-bound multi-threaded code does NOT run faster with more threads
- I/O-bound multi-threaded code IS fine — the GIL is released during I/O waits
- True parallelism for CPU work requires `multiprocessing` (separate processes)
  or external C extensions that release the GIL (e.g., NumPy)

```python
# I/O-bound: threading works well
import threading
threads = [threading.Thread(target=fetch_url, args=(u,)) for u in urls]

# CPU-bound: use multiprocessing instead
from multiprocessing import Pool
with Pool(4) as p:
    results = p.map(compute_heavy, data)
```

**Note:** PyPy and other implementations may not have a GIL. Python 3.13+
introduced a "no-GIL" build (PEP 703) as an experimental option.

---

## Q7. What is the difference between deepcopy and shallow copy?

**Answer:**

- **Shallow copy** — creates a new container but the elements inside still
  reference the **same objects**.
- **Deep copy** — recursively creates new copies of all nested objects.

```python
import copy

original = [[1, 2], [3, 4]]

shallow = copy.copy(original)     # or original[:]  or list(original)
deep    = copy.deepcopy(original)

original[0].append(99)

print(shallow[0])   # [1, 2, 99] — affected! inner list is shared
print(deep[0])      # [1, 2]     — not affected
```

**When to use each:**
- Shallow: when contained objects are immutable (e.g., list of strings/ints)
- Deep: when nested mutable objects exist and isolation is required

---

## Q8. What are *args and **kwargs?

**Answer:**

- `*args` collects extra **positional** arguments as a **tuple**
- `**kwargs` collects extra **keyword** arguments as a **dict**

```python
def demo(*args, **kwargs):
    print(args)    # tuple
    print(kwargs)  # dict

demo(1, 2, 3, x=10, y=20)
# (1, 2, 3)
# {'x': 10, 'y': 20}
```

They can be combined with regular parameters:
```python
def mixed(a, b, *args, key="default", **kwargs):
    ...
```

**Unpacking** (the inverse):
```python
nums = [1, 2, 3]
print(*nums)         # unpacks into positional args: print(1, 2, 3)

opts = {"sep": ", "}
print(*nums, **opts) # print(1, 2, 3, sep=", ")
```

---

## Q9. What is a lambda function?

**Answer:**

A `lambda` is an anonymous, single-expression function defined inline.

```python
square = lambda x: x ** 2
add    = lambda x, y: x + y

square(5)    # 25
add(3, 4)    # 7
```

Lambdas are most useful as short callback arguments:
```python
data = [{"name": "Bob", "age": 30}, {"name": "Alice", "age": 25}]
data.sort(key=lambda d: d["age"])
```

**Limitations:** only a single expression; no statements (no `if/else` blocks,
no `for`, no `try`). Use a named function when the logic is complex.

---

## Q10. What is list comprehension vs. generator expression?

**Answer:**

```python
# List comprehension — eager, builds entire list in memory
squares_list = [x**2 for x in range(10)]   # [0, 1, 4, 9, ...]

# Generator expression — lazy, produces one item at a time
squares_gen  = (x**2 for x in range(10))   # <generator object>
```

| | List comprehension | Generator expression |
|--|-------------------|--------------------|
| Syntax | `[expr for ...]` | `(expr for ...)` |
| Memory | Entire list | One element |
| Reusable | Yes | No (exhausted after one pass) |
| Speed | Faster if all items needed | Faster if only some items needed |

**Best practice:** use a generator when feeding a `for` loop or a function that
consumes lazily (`sum`, `any`, `all`, `max`). Use a list when you need random
access, `len()`, or multiple passes.

---

## Q11. What is __init__ vs __new__?

**Answer:**

- `__new__` — **allocates** the object; called first; returns the new instance
- `__init__` — **initialises** the already-created instance; called second; returns None

```python
class MyClass:
    def __new__(cls, *args, **kwargs):
        print("__new__ called")
        instance = super().__new__(cls)
        return instance

    def __init__(self, value):
        print("__init__ called")
        self.value = value

obj = MyClass(42)
# __new__ called
# __init__ called
```

**When to override `__new__`:**
- Immutable types (you cannot change a `str` in `__init__`)
- Singleton pattern
- Custom metaclass behaviour

For almost all normal classes, only `__init__` is overridden.

---

## Q12. What is __str__ vs __repr__?

**Answer:**

- `__repr__` — **unambiguous** representation; aimed at developers; used by the REPL
- `__str__` — **readable** representation; aimed at end users; used by `print()`

```python
class Point:
    def __init__(self, x, y):
        self.x, self.y = x, y

    def __repr__(self):
        return f"Point({self.x!r}, {self.y!r})"   # unambiguous, reconstructible

    def __str__(self):
        return f"({self.x}, {self.y})"             # user-friendly

p = Point(3, 4)
repr(p)    # "Point(3, 4)"
str(p)     # "(3, 4)"
print(p)   # (3, 4)   — uses __str__
```

**Rule:** always implement `__repr__`. Implement `__str__` only if you want a
different human-readable form.

---

## Q13. What are Python's scope rules (LEGB)?

**Answer:**

Python resolves names by searching four scopes in order:

1. **L**ocal — inside the current function
2. **E**nclosing — enclosing function scopes (closures)
3. **G**lobal — module-level scope
4. **B**uilt-in — Python's built-in names (`len`, `print`, etc.)

```python
x = "global"

def outer():
    x = "enclosing"

    def inner():
        x = "local"
        print(x)    # "local"  (L wins)
    inner()
    print(x)        # "enclosing"

outer()
print(x)            # "global"
```

**`global` and `nonlocal` keywords:**
```python
count = 0
def increment():
    global count    # modify global, not create a local
    count += 1

def make_counter():
    n = 0
    def inc():
        nonlocal n  # modify enclosing scope variable
        n += 1
        return n
    return inc
```

---

## Q14. What is a closure?

**Answer:**

A **closure** is a nested function that captures variables from its enclosing
scope, even after the enclosing function has returned.

```python
def make_multiplier(factor):
    def multiply(x):        # captures 'factor' from enclosing scope
        return x * factor
    return multiply

double = make_multiplier(2)
triple = make_multiplier(3)

double(5)   # 10
triple(5)   # 15
```

The captured variables live in the function's `__closure__`:
```python
double.__closure__[0].cell_contents   # 2
```

**Use cases:** factory functions, decorators, partial application without
`functools.partial`.

**Common pitfall — late binding:**
```python
fns = [lambda: i for i in range(3)]
[f() for f in fns]   # [2, 2, 2] — all capture the SAME i

# Fix: capture by default argument
fns = [lambda i=i: i for i in range(3)]
[f() for f in fns]   # [0, 1, 2]
```

---

## Q15. What is the difference between @staticmethod, @classmethod, and instance method?

**Answer:**

```python
class MyClass:
    class_var = "shared"

    def instance_method(self):
        """Gets the instance (self). Can access instance and class state."""
        return self.class_var

    @classmethod
    def class_method(cls):
        """Gets the class (cls). Can access class state but not instance state."""
        return cls.class_var

    @staticmethod
    def static_method():
        """Gets neither. Just a plain function namespaced inside the class."""
        return "no self or cls"
```

| | Receives | Access instance? | Access class? | Override in subclass? |
|--|---------|-----------------|---------------|-----------------------|
| Instance | `self` | Yes | Yes | Yes |
| Class | `cls` | No | Yes | Yes (cls changes) |
| Static | nothing | No | No | Technically yes |

**When to use:**
- Instance method: normal behaviour that depends on object state
- Class method: alternative constructors (`@classmethod def from_string(cls, s)`)
- Static method: utility function logically related to the class

---

## Q16. What is duck typing?

**Answer:**

Duck typing means Python checks whether an object **behaves** the right way
(has the required methods/attributes), not whether it **is** a specific type.
"If it walks like a duck and quacks like a duck, it is a duck."

```python
def make_sound(animal):
    animal.speak()    # works for anything with a .speak() method

class Dog:
    def speak(self): print("Woof!")

class Cat:
    def speak(self): print("Meow!")

class Robot:
    def speak(self): print("Beep boop")

make_sound(Dog())    # Woof!
make_sound(Cat())    # Meow!
make_sound(Robot())  # Beep boop — no inheritance required!
```

Duck typing is enforced at **runtime**. Use `hasattr()` or `try/except` for
defensive checks. For compile-time checks, use `typing.Protocol` (structural
subtyping) or `isinstance()`.

---

## Q17. What is multiple inheritance and MRO?

**Answer:**

Python supports multiple inheritance — a class can inherit from more than one
base class. The **Method Resolution Order (MRO)** determines which class's method
is called when names collide. Python uses the **C3 linearisation** algorithm.

```python
class A:
    def hello(self): print("A")

class B(A):
    def hello(self): print("B")

class C(A):
    def hello(self): print("C")

class D(B, C):    # inherits from both B and C
    pass

D.mro()    # [D, B, C, A, object]
D().hello()  # "B" — first match in MRO
```

**`super()`** follows the MRO, not just the direct parent:
```python
class B(A):
    def hello(self):
        super().hello()   # calls C.hello() if called from D, not A.hello()
        print("B")
```

**Mixins** are a common use of multiple inheritance: small classes that add
specific behaviour without being standalone base classes.

---

## Q18. What are Python's built-in data types?

**Answer:**

| Category | Types |
|----------|-------|
| Numeric | `int`, `float`, `complex`, `bool` |
| Sequence | `str`, `bytes`, `bytearray`, `list`, `tuple`, `range` |
| Mapping | `dict` |
| Set | `set`, `frozenset` |
| Binary | `bytes`, `bytearray`, `memoryview` |
| None | `NoneType` |
| Callable | `function`, `method`, `lambda`, `class` |

**Note:** `bool` is a subclass of `int` — `True == 1` and `False == 0`.

---

## Q19. How does Python memory management work?

**Answer:**

Python uses **reference counting** as the primary mechanism. Every object
has a reference count; when it reaches zero, the object is immediately
deallocated.

```python
import sys
x = [1, 2, 3]
sys.getrefcount(x)   # 2 (x + the argument to getrefcount)
y = x
sys.getrefcount(x)   # 3
del y
sys.getrefcount(x)   # 2
```

**Memory pools:** CPython uses a private heap and a system of memory pools
(`PyMalloc`) optimised for small objects (< 512 bytes). The pool avoids the
overhead of calling `malloc`/`free` for every small allocation.

**Object caching:** small integers (-5 to 256), `None`, `True`, `False`, and
short strings are interned/cached — Python reuses the same objects.

---

## Q20. What is garbage collection in Python?

**Answer:**

Reference counting alone cannot free **cyclic references** (objects that
reference each other). Python has a supplemental **cyclic garbage collector**
(`gc` module) that detects and clears such cycles.

```python
import gc

# Create a cycle
a = {}
b = {"ref": a}
a["ref"] = b
del a, b   # ref count never reaches 0 — cycle!

gc.collect()  # explicitly trigger GC
```

**Generational GC:** objects are divided into three generations.
New objects start in generation 0 (collected frequently).
Objects that survive are promoted to generation 1, then 2
(collected less often). Most short-lived objects never leave generation 0.

**Disable GC** for performance-sensitive code (if you manage cycles manually):
```python
gc.disable()
```

---

## Q21. What are metaclasses?

**Answer:**

A **metaclass** is the class of a class — it controls how classes are created.
The default metaclass is `type`. When Python processes a `class` statement,
it calls the metaclass to construct the class object.

```python
class Meta(type):
    def __new__(mcs, name, bases, namespace):
        print(f"Creating class: {name}")
        cls = super().__new__(mcs, name, bases, namespace)
        return cls

class MyClass(metaclass=Meta):   # triggers Meta.__new__
    pass
# Creating class: MyClass
```

**Common use cases:**
- Auto-register subclasses
- Enforce interface contracts (before ABCs)
- Add class-level validation
- ORM model definition (Django/SQLAlchemy)

**Prefer alternatives** when possible: decorators, `__init_subclass__`, or ABCs
are simpler for most needs.

---

## Q22. What are Python descriptors?

**Answer:**

A **descriptor** is any object that defines `__get__`, `__set__`, or `__delete__`.
Descriptors power `property`, `classmethod`, `staticmethod`, and more.

```python
class Validator:
    """Data descriptor: validates values on assignment."""

    def __set_name__(self, owner, name):
        self.name = name

    def __get__(self, obj, objtype=None):
        if obj is None:
            return self
        return obj.__dict__.get(self.name)

    def __set__(self, obj, value):
        if not isinstance(value, int) or value < 0:
            raise ValueError(f"{self.name} must be a non-negative int")
        obj.__dict__[self.name] = value


class Person:
    age = Validator()

p = Person()
p.age = 25    # OK
p.age = -1    # ValueError
```

**Non-data descriptor:** only `__get__` (e.g., functions — they become methods).
**Data descriptor:** `__get__` + `__set__`/`__delete__` (e.g., `property`).

---

## Q23. What is async/await in Python?

**Answer:**

`async`/`await` enables **cooperative concurrency** via `asyncio`. Coroutines
are functions declared with `async def`; they can pause execution at `await`
points without blocking the event loop.

```python
import asyncio

async def fetch(url: str) -> str:
    await asyncio.sleep(0.1)   # simulates I/O wait
    return f"data from {url}"

async def main():
    # Run concurrently — total ~0.1s, not 0.3s
    results = await asyncio.gather(
        fetch("https://a.com"),
        fetch("https://b.com"),
        fetch("https://c.com"),
    )
    print(results)

asyncio.run(main())
```

**Key points:**
- `await` can only be used inside `async def`
- Concurrency, not parallelism — one coroutine runs at a time
- Best for I/O-bound work (network, disk, DB)
- For CPU-bound parallel work, use `multiprocessing` or `asyncio.run_in_executor`

---

## Q24. What is the difference between threading and multiprocessing?

**Answer:**

| | `threading` | `multiprocessing` |
|--|------------|-------------------|
| Unit | Thread (shared memory) | Process (separate memory) |
| GIL | Still applies | Each process has its own GIL |
| CPU-bound | Limited by GIL | True parallelism |
| I/O-bound | Excellent | Works but heavyweight |
| Overhead | Low | High (process startup) |
| Communication | Shared objects | Queues, Pipes, shared memory |

```python
# Threading — good for I/O-bound
from threading import Thread

# Multiprocessing — good for CPU-bound
from multiprocessing import Pool

with Pool(4) as p:
    results = p.map(cpu_heavy_function, large_dataset)
```

**`concurrent.futures`** provides a unified high-level API:
```python
from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor
```

---

## Q25. What is the `with` statement and context managers?

**Answer:**

The `with` statement ensures resources are properly acquired and released,
even if exceptions occur. It calls `__enter__` on entry and `__exit__` on exit.

```python
with open("file.txt") as f:
    data = f.read()
# file is automatically closed here

# Equivalent:
f = open("file.txt")
try:
    data = f.read()
finally:
    f.close()
```

**Custom context manager:**
```python
class Timer:
    def __enter__(self):
        import time
        self.start = time.time()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.elapsed = time.time() - self.start
        return False   # False = don't suppress exceptions

with Timer() as t:
    do_work()
print(f"Elapsed: {t.elapsed:.2f}s")
```

Or use `contextlib.contextmanager`:
```python
from contextlib import contextmanager

@contextmanager
def timer():
    import time
    start = time.time()
    yield
    print(f"Elapsed: {time.time() - start:.2f}s")
```

---

## Q26. What is the difference between `__slots__` and `__dict__`?

**Answer:**

By default, Python stores instance attributes in a per-instance `__dict__`.
Defining `__slots__` replaces the dict with a fixed-size array of descriptors.

```python
class WithDict:
    def __init__(self, x, y):
        self.x, self.y = x, y
    # stores attrs in __dict__

class WithSlots:
    __slots__ = ("x", "y")
    def __init__(self, x, y):
        self.x, self.y = x, y
    # no __dict__; fixed slots only
```

**Benefits of `__slots__`:**
- ~50% less memory per instance (no per-instance dict overhead)
- Slightly faster attribute access
- Prevents accidental creation of new attributes

**Drawbacks:**
- Cannot add arbitrary attributes
- Multiple inheritance with slots is tricky
- Pickling requires special care

Best for: data-heavy classes where you create millions of instances (points,
records, etc.).

---

## Q27. What are Python's `collections` module highlights?

**Answer:**

```python
from collections import (
    defaultdict,   # dict with automatic default values
    Counter,       # multiset / frequency counter
    OrderedDict,   # dict that remembers insertion order (pre-3.7)
    deque,         # double-ended queue, O(1) appends/pops both ends
    namedtuple,    # tuple with named fields
    ChainMap,      # view over multiple dicts
)

# defaultdict
dd = defaultdict(list)
dd["key"].append(1)   # no KeyError — auto-creates []

# Counter
c = Counter("banana")   # Counter({'a': 3, 'n': 2, 'b': 1})
c.most_common(2)        # [('a', 3), ('n', 2)]

# deque
dq = deque([1, 2, 3], maxlen=3)
dq.appendleft(0)   # deque([0, 1, 2]) — 3 dropped off right

# namedtuple
Point = namedtuple("Point", ["x", "y"])
p = Point(3, 4)
p.x   # 3
```

---

## Q28. What is `itertools` and why is it useful?

**Answer:**

`itertools` provides fast, memory-efficient building blocks for working with
iterables — all implemented in C.

```python
import itertools

# Infinite iterators
itertools.count(10)          # 10, 11, 12, ...
itertools.cycle([1, 2, 3])   # 1, 2, 3, 1, 2, 3, ...
itertools.repeat("x", 3)     # "x", "x", "x"

# Combinatorics
list(itertools.permutations("AB", 2))   # [('A','B'), ('B','A')]
list(itertools.combinations("ABC", 2))  # [('A','B'), ('A','C'), ('B','C')]
list(itertools.product([0,1], repeat=2))# all 2-bit combos

# Chaining and grouping
itertools.chain([1,2], [3,4])           # 1, 2, 3, 4
itertools.groupby("AABBB", key=lambda x: x)  # groups consecutive equal elements

# Slicing
itertools.islice(range(100), 5, 15, 2)  # 5, 7, 9, 11, 13
```

Essential when building data pipelines and avoiding eager list construction.

---

## Q29. What is `functools` and what are its most important tools?

**Answer:**

```python
import functools

# lru_cache — memoisation
@functools.lru_cache(maxsize=None)
def fib(n):
    return n if n < 2 else fib(n-1) + fib(n-2)

# partial — fix some function arguments
from functools import partial
double = partial(pow, exp=2)   # partial(pow, exp=2)
double(base=5)   # 25

# reduce — fold a sequence
functools.reduce(lambda a, b: a + b, [1, 2, 3, 4])   # 10

# wraps — preserve decorated function metadata
@functools.wraps(original_func)
def wrapper(*args, **kwargs):
    return original_func(*args, **kwargs)

# total_ordering — define __eq__ + one comparison; get all others
@functools.total_ordering
class Card:
    def __eq__(self, other): ...
    def __lt__(self, other): ...
    # __le__, __gt__, __ge__ auto-generated
```

---

## Q30. What are Python type hints and the `typing` module?

**Answer:**

Type hints (PEP 484) add optional static type annotations. They are not
enforced at runtime by default but enable static analysis tools (mypy, pyright).

```python
from typing import Optional, Union, List, Dict, Tuple, Callable

def greet(name: str) -> str:
    return f"Hello, {name}"

def find_user(user_id: int) -> Optional[str]:  # may return None
    ...

# Python 3.10+ union syntax
def process(value: int | str) -> None:
    ...

# Generic types (3.9+ built-in)
def first(items: list[int]) -> int:
    return items[0]

# TypeVar — generic functions
from typing import TypeVar
T = TypeVar("T")

def identity(x: T) -> T:
    return x
```

**`Protocol`** — structural subtyping (duck typing with types):
```python
from typing import Protocol

class Drawable(Protocol):
    def draw(self) -> None: ...

def render(item: Drawable) -> None:
    item.draw()   # works for any class with draw(), no inheritance needed
```

---

## Q31. What is the difference between `is None` and `== None`?

**Answer:**

`is None` is the correct and idiomatic way to check for `None`. It tests
identity (there is only one `None` object in Python). `== None` triggers
`__eq__`, which can be overridden by custom classes to return unexpected results.

```python
# Correct
if x is None:
    ...

# Avoid — can be overridden
class Weird:
    def __eq__(self, other):
        return True   # equals everything, even None!

w = Weird()
w == None   # True — misleading
w is None   # False — correct
```

---

## Q32. How does Python handle exceptions?

**Answer:**

```python
try:
    result = 10 / 0
except ZeroDivisionError as e:
    print(f"Caught: {e}")
except (TypeError, ValueError) as e:
    print(f"Multiple types: {e}")
else:
    # Runs if NO exception was raised
    print(f"Result: {result}")
finally:
    # Always runs
    print("Cleanup")
```

**Exception hierarchy:** All exceptions inherit from `BaseException`.
`Exception` is the common base for normal exceptions. Never catch `BaseException`
unless you intend to catch `KeyboardInterrupt` and `SystemExit`.

**Chaining exceptions:**
```python
try:
    open("missing.txt")
except FileNotFoundError as e:
    raise RuntimeError("Config missing") from e   # sets __cause__
```

---

## Q33. What is the `__all__` variable?

**Answer:**

`__all__` is a list of names that should be exported when `from module import *`
is used. It also serves as documentation of the public API.

```python
# mymodule.py
__all__ = ["PublicClass", "public_function"]   # only these are exported

class PublicClass: ...
def public_function(): ...
def _private_helper(): ...   # not in __all__, not exported by *
```

Even without `__all__`, names starting with `_` are not exported by `*` imports.

---

## Q34. What are Python's dunder (magic) methods?

**Answer:**

Dunder methods let classes customise Python's built-in operations:

| Method | Triggered by |
|--------|-------------|
| `__init__` | `MyClass()` |
| `__str__` | `str(obj)`, `print(obj)` |
| `__repr__` | `repr(obj)`, REPL display |
| `__len__` | `len(obj)` |
| `__getitem__` | `obj[key]` |
| `__setitem__` | `obj[key] = val` |
| `__contains__` | `item in obj` |
| `__iter__` | `for x in obj` |
| `__next__` | `next(obj)` |
| `__add__` | `obj + other` |
| `__eq__` | `obj == other` |
| `__lt__` | `obj < other` |
| `__hash__` | `hash(obj)` |
| `__call__` | `obj()` |
| `__enter__` / `__exit__` | `with obj` |

---

## Q35. How do you handle circular imports in Python?

**Answer:**

Circular imports occur when module A imports B and B imports A. Solutions:

1. **Restructure** — extract the shared code into a third module C.
2. **Lazy import** — move the import inside the function/method that needs it.
3. **`TYPE_CHECKING` guard** — import only for type hints:

```python
from __future__ import annotations  # makes all annotations strings (lazy)
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from .other_module import OtherClass   # only imported during type checking

def process(item: "OtherClass") -> None:   # str annotation avoids runtime import
    ...
```

---

## Q36. What is the difference between `append`, `extend`, and `+` for lists?

**Answer:**

```python
a = [1, 2, 3]

a.append([4, 5])    # adds ONE item: [1, 2, 3, [4, 5]]
a.extend([4, 5])    # adds each item: [1, 2, 3, 4, 5]
b = a + [4, 5]      # returns a NEW list: [1, 2, 3, 4, 5]
a += [4, 5]         # in-place extend (calls __iadd__)
```

**Performance:** `append` and `extend` are O(1) amortised; `+` creates a new
list so it's O(n).

---

## Q37. What are Python's string formatting methods?

**Answer:**

```python
name, score = "Alice", 98.5

# % formatting (old-style, avoid)
"Hello %s, score: %.1f" % (name, score)

# str.format() (Python 2.6+)
"Hello {}, score: {:.1f}".format(name, score)
"Hello {name}, score: {score:.1f}".format(name=name, score=score)

# f-strings (Python 3.6+, preferred)
f"Hello {name}, score: {score:.1f}"
f"Expr: {2 + 2}"           # evaluates expressions
f"{name!r}"                 # applies repr()
f"{score:>10.2f}"           # right-align, width 10, 2 decimals

# Template strings (safe for user-supplied templates)
from string import Template
Template("Hello $name").substitute(name=name)
```

---

## Q38. What is `property` and how is it used?

**Answer:**

`@property` turns a method into a read-only attribute. Combined with
`@<name>.setter` and `@<name>.deleter`, it provides full get/set/delete control.

```python
class Circle:
    def __init__(self, radius: float) -> None:
        self._radius = radius

    @property
    def radius(self) -> float:
        return self._radius

    @radius.setter
    def radius(self, value: float) -> None:
        if value < 0:
            raise ValueError("Radius must be non-negative")
        self._radius = value

    @property
    def area(self) -> float:
        import math
        return math.pi * self._radius ** 2

c = Circle(5)
c.radius        # 5   — getter
c.radius = 10   # setter
c.area          # 314.159... — computed property
```

---

## Q39. What is `zip`, `map`, and `filter`?

**Answer:**

```python
# zip — pairs elements from multiple iterables
names = ["Alice", "Bob"]
scores = [90, 85]
list(zip(names, scores))   # [("Alice", 90), ("Bob", 85)]

# zip_longest (fills missing values)
from itertools import zip_longest

# map — apply function to each element (lazy)
list(map(str.upper, ["a", "b"]))   # ["A", "B"]
list(map(lambda x: x**2, [1,2,3])) # [1, 4, 9]

# filter — keep elements where predicate is True (lazy)
list(filter(lambda x: x > 0, [-1, 2, -3, 4]))  # [2, 4]
```

**Pythonic alternative:** prefer list/generator comprehensions over `map`/`filter`
for clarity:
```python
[x**2 for x in [1,2,3]]                  # map
[x for x in [-1,2,-3,4] if x > 0]        # filter
```

---

## Q40. What is the `__name__ == "__main__"` guard?

**Answer:**

When Python runs a file directly, `__name__` is set to `"__main__"`.
When the file is imported as a module, `__name__` is the module name.

```python
# my_module.py

def do_work():
    print("Working")

if __name__ == "__main__":
    # Only runs when executed directly, not when imported
    do_work()
```

This pattern lets a file serve as both a reusable module AND a runnable script.

---

## Q41. What are Python's `*` and `**` unpacking operators?

**Answer:**

```python
# * unpacks iterables
a, *rest = [1, 2, 3, 4]    # a=1, rest=[2, 3, 4]
first, *middle, last = range(5)  # 0, [1,2,3], 4

# ** unpacks dicts
d1 = {"a": 1}
d2 = {"b": 2}
merged = {**d1, **d2}  # {"a": 1, "b": 2}

# In function calls
def add(x, y, z): return x + y + z
args = [1, 2, 3]
add(*args)          # add(1, 2, 3)

kwargs = {"x": 1, "y": 2, "z": 3}
add(**kwargs)       # add(x=1, y=2, z=3)
```

---

## Q42. How does Python's `sorted` and `sort` work?

**Answer:**

Both use **Timsort** — a hybrid merge/insertion sort, O(n log n) worst case,
O(n) best case (already sorted), stable.

```python
# sort() — in-place, returns None
lst = [3, 1, 2]
lst.sort()                            # [1, 2, 3]
lst.sort(reverse=True)                # [3, 2, 1]
lst.sort(key=lambda x: -x)           # same effect

# sorted() — returns new list, works on any iterable
sorted([3, 1, 2])                     # [1, 2, 3]
sorted("banana")                      # ['a', 'a', 'a', 'b', 'n', 'n']

# Sort by key
people = [{"name": "Bob", "age": 30}, {"name": "Alice", "age": 25}]
sorted(people, key=lambda p: p["age"])

# attrgetter / itemgetter for speed
from operator import itemgetter
sorted(people, key=itemgetter("age"))
```

---

## Q43. What are `dataclasses` and when should you use them?

**Answer:**

`@dataclass` (Python 3.7+) auto-generates `__init__`, `__repr__`, `__eq__`,
and optionally `__hash__`, `__lt__`, etc.

```python
from dataclasses import dataclass, field
from typing import List

@dataclass(order=True)   # also generates __lt__, __le__, etc.
class Product:
    name: str
    price: float
    tags: List[str] = field(default_factory=list)
    _internal: str = field(default="", repr=False, compare=False)

    def discount(self, pct: float) -> float:
        return self.price * (1 - pct)

p = Product("Widget", 9.99)
repr(p)   # Product(name='Widget', price=9.99, tags=[])
```

**Use dataclasses when:** you need a class mainly to hold data.
**Use named tuples** when the data is immutable.
**Use regular classes** when you have significant behaviour/logic.

---

## Q44. What is walrus operator `:=` (assignment expression)?

**Answer:**

The walrus operator (Python 3.8+) assigns a value to a variable as part of an
expression. Useful for avoiding redundant computation.

```python
# Before
data = get_data()
if data:
    process(data)

# With walrus
if data := get_data():
    process(data)

# In while loops
while chunk := file.read(8192):
    process(chunk)

# In comprehensions
filtered = [y for x in data if (y := expensive(x)) > 0]
```

Use sparingly — it can reduce readability if overused.

---

## Q45. What is structural pattern matching (`match`/`case`)?

**Answer:**

Introduced in Python 3.10. More powerful than `if/elif` chains; matches on
structure, not just equality.

```python
def handle_command(command):
    match command.split():
        case ["quit"]:
            print("Quitting")
        case ["go", direction]:
            print(f"Going {direction}")
        case ["pick", item, "from", container]:
            print(f"Pick {item} from {container}")
        case _:
            print(f"Unknown: {command}")

handle_command("go north")       # Going north
handle_command("pick key from chest")  # Pick key from chest
```

**Matching objects:**
```python
match point:
    case Point(x=0, y=0):
        print("Origin")
    case Point(x=0, y=y):
        print(f"Y-axis at {y}")
    case Point(x=x, y=y):
        print(f"Point({x}, {y})")
```

---

## Q46. What is the difference between `__getattr__` and `__getattribute__`?

**Answer:**

- `__getattribute__` — called for **every** attribute access; override carefully
  (infinite recursion risk)
- `__getattr__` — called only when the attribute is **not found** through normal
  means; a safe fallback

```python
class LazyLoader:
    def __getattr__(self, name):
        # Called only when name is not in __dict__ or class
        print(f"Loading {name} on demand")
        value = load_from_db(name)
        setattr(self, name, value)   # cache it
        return value

class Proxy:
    def __getattribute__(self, name):
        print(f"Accessing {name}")
        return super().__getattribute__(name)   # MUST call super
```

---

## Q47. What is `abc.ABC` vs `typing.Protocol`?

**Answer:**

| | `abc.ABC` | `typing.Protocol` |
|--|-----------|-------------------|
| Enforcement | `isinstance`/`issubclass` | Structural (duck typing) |
| Inheritance | Required | Not required |
| Runtime check | `isinstance(obj, MyABC)` | `isinstance(obj, MyProtocol)` if `@runtime_checkable` |
| Use case | Nominal subtyping | Structural subtyping |

```python
# ABC — must inherit
from abc import ABC, abstractmethod

class Animal(ABC):
    @abstractmethod
    def speak(self) -> str: ...

class Dog(Animal):
    def speak(self) -> str: return "Woof"

# Protocol — no inheritance needed
from typing import Protocol, runtime_checkable

@runtime_checkable
class Speakable(Protocol):
    def speak(self) -> str: ...

class Cat:    # does NOT inherit Speakable
    def speak(self) -> str: return "Meow"

isinstance(Cat(), Speakable)   # True — structural check
```

---

## Q48. What are `__enter__` and `__exit__` used for?

**Answer:**

These methods implement the **context manager protocol**, used with `with`.

```python
class DatabaseConnection:
    def __enter__(self):
        self.conn = create_connection()
        return self.conn

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.conn.close()
        if exc_type is not None:
            # An exception occurred; return True to suppress it
            print(f"Exception suppressed: {exc_val}")
            return True   # suppress
        return False   # re-raise if exception

with DatabaseConnection() as conn:
    conn.execute("SELECT 1")
# conn.close() is guaranteed even if execute() raises
```

`__exit__` receives exception info (`None, None, None` if no exception).
Return `True` to suppress the exception; return `False` (or `None`) to propagate it.

---

## Q49. What are Python's comprehensions?

**Answer:**

Python supports four types of comprehensions:

```python
# List comprehension
squares = [x**2 for x in range(10) if x % 2 == 0]
# [0, 4, 16, 36, 64]

# Dict comprehension
square_map = {x: x**2 for x in range(5)}
# {0: 0, 1: 1, 2: 4, 3: 9, 4: 16}

# Set comprehension
unique_lengths = {len(word) for word in ["hi", "hello", "hey"]}
# {2, 5, 3}

# Generator expression (lazy)
total = sum(x**2 for x in range(10))

# Nested
matrix = [[i * j for j in range(3)] for i in range(3)]

# Equivalent to (but more Pythonic than):
squares = []
for x in range(10):
    if x % 2 == 0:
        squares.append(x**2)
```

---

## Q50. What are Python 3's most important improvements over Python 2?

**Answer:**

Python 2 reached end-of-life January 1, 2020. Key improvements in Python 3:

| Feature | Python 2 | Python 3 |
|---------|----------|----------|
| `print` | statement | function |
| Integer division | `5/2 == 2` | `5/2 == 2.5` |
| Unicode | bytes default | str is Unicode |
| `range` | returns list | returns iterator |
| `input` | evals expression | reads string |
| Metaclass syntax | `__metaclass__` | `class Foo(metaclass=Meta)` |
| Exception syntax | `except E, e:` | `except E as e:` |
| f-strings | No | Yes (3.6+) |
| `asyncio` | No | Yes (3.4+) |
| `dataclasses` | No | Yes (3.7+) |
| Walrus operator | No | Yes (3.8+) |
| Pattern matching | No | Yes (3.10+) |
| Type hints | Partial (comments) | Full (3.5+) |
| `pathlib` | No | Yes (3.4+) |
| `breakpoint()` | No | Yes (3.7+) |
