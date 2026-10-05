# Decorators — Deep Dive

You have used decorators. Now you will understand exactly how they work and write powerful ones from scratch.

---

## How Decorators Work Under the Hood

A decorator is syntactic sugar for a function call. When you write:

```python
@my_decorator
def my_function():
    pass
```

Python executes exactly this:
```python
def my_function():
    pass
my_function = my_decorator(my_function)
```

That's it. A decorator is a callable that takes a callable and returns a callable.

### The Simplest Possible Decorator

```python
def do_nothing(func):
    """The most minimal decorator — does absolutely nothing."""
    return func  # Return the original function unchanged

@do_nothing
def greet(name):
    return f"Hello, {name}!"

# greet is still the original greet function
print(greet("Alice"))  # "Hello, Alice!"
```

### A Decorator That Adds Behavior

```python
def uppercase_result(func):
    """Modifies the return value of the function."""
    def wrapper(*args, **kwargs):
        result = func(*args, **kwargs)
        return result.upper()
    return wrapper

@uppercase_result
def greet(name: str) -> str:
    return f"Hello, {name}!"

print(greet("Alice"))  # "HELLO, ALICE!"
```

### The `*args, **kwargs` Pattern

Every decorator wrapper should use `*args, **kwargs` to pass through all arguments to the original function:

```python
def my_decorator(func):
    def wrapper(*args, **kwargs):
        # *args: catches all positional arguments as a tuple
        # **kwargs: catches all keyword arguments as a dict
        # Then passes them all through to the original function
        return func(*args, **kwargs)
    return wrapper
```

This works for functions with any signature.

---

## functools.wraps — Essential for Every Decorator

Without `functools.wraps`, your decorator replaces the function's metadata with the wrapper's metadata. This breaks introspection, help(), and debugging tools.

```python
import functools


def without_wraps(func):
    def wrapper(*args, **kwargs):
        return func(*args, **kwargs)
    return wrapper


def with_wraps(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        return func(*args, **kwargs)
    return wrapper


@without_wraps
def calculate_tax(amount: float, rate: float = 0.1) -> float:
    """Calculate tax on a given amount."""
    return amount * rate

@with_wraps
def calculate_discount(amount: float, rate: float = 0.2) -> float:
    """Calculate discount on a given amount."""
    return amount * rate


print(calculate_tax.__name__)      # "wrapper" — WRONG
print(calculate_tax.__doc__)       # None — WRONG

print(calculate_discount.__name__) # "calculate_discount" — correct
print(calculate_discount.__doc__)  # "Calculate discount on a given amount." — correct
```

`functools.wraps` copies these attributes from `func` to `wrapper`:
- `__name__` — function name
- `__qualname__` — qualified name
- `__doc__` — docstring
- `__module__` — module name
- `__annotations__` — type hints
- `__dict__` — function attributes
- `__wrapped__` — reference to the original function

**Rule:** Always use `@functools.wraps(func)` in every decorator wrapper.

---

## Parametrized Decorators (Decorator Factories)

When your decorator needs configuration parameters, you need a three-level structure:

```
decorator_factory(config) → decorator(func) → wrapper(*args, **kwargs)
```

```python
import functools
import time


def retry(max_attempts: int = 3, delay: float = 1.0, exceptions: tuple = (Exception,)):
    """
    Retry a function on failure with configurable parameters.
    
    @retry(max_attempts=5, delay=2.0, exceptions=(ConnectionError,))
    def unstable_api_call():
        ...
    """
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            last_exception = None
            for attempt in range(1, max_attempts + 1):
                try:
                    return func(*args, **kwargs)
                except exceptions as e:
                    last_exception = e
                    if attempt < max_attempts:
                        print(f"Attempt {attempt}/{max_attempts} failed: {e}. Retrying in {delay}s...")
                        time.sleep(delay)
                    else:
                        print(f"All {max_attempts} attempts failed.")
            raise last_exception
        return wrapper
    return decorator


@retry(max_attempts=3, delay=0.5, exceptions=(ConnectionError, TimeoutError))
def fetch_data(url: str) -> dict:
    """Fetches data from a URL. Retried on network errors."""
    import random
    if random.random() < 0.6:
        raise ConnectionError("Connection refused")
    return {"data": "success"}
```

### How to Think About Three Levels

1. `retry(max_attempts=3, delay=0.5)` — Called first. Returns `decorator`.
2. `decorator(fetch_data)` — Called by Python when it applies `@retry(...)`. Returns `wrapper`.
3. `wrapper(url)` — Called every time you call `fetch_data(url)`.

---

## Class-Based Decorators

When your decorator needs to maintain state across calls, a class works well:

```python
import functools
import time


class RateLimit:
    """
    Rate limit decorator: max N calls per minute.
    
    State (call history) is maintained in the class instance.
    The class is callable (__call__), so it works as a decorator.
    """
    
    def __init__(self, max_calls: int, period: float = 60.0):
        self.max_calls = max_calls
        self.period = period
        self._calls: list[float] = []
    
    def __call__(self, func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            now = time.time()
            # Remove old calls outside the window
            self._calls = [t for t in self._calls if now - t < self.period]
            
            if len(self._calls) >= self.max_calls:
                oldest = self._calls[0]
                wait_time = self.period - (now - oldest)
                raise RuntimeError(
                    f"Rate limit exceeded. {self.max_calls} calls per {self.period}s. "
                    f"Try again in {wait_time:.1f}s."
                )
            
            self._calls.append(now)
            return func(*args, **kwargs)
        return wrapper


# Usage — the class instance IS the decorator
rate_limited = RateLimit(max_calls=5, period=60.0)

@rate_limited
def api_call(endpoint: str) -> dict:
    return {"endpoint": endpoint, "data": "..."}
```

---

## Stacking Decorators

Decorators are applied bottom-to-top (the closest to the function is applied first):

```python
@decorator_a
@decorator_b
@decorator_c
def my_function():
    pass

# Equivalent to:
my_function = decorator_a(decorator_b(decorator_c(my_function)))
# Application order: c first, then b, then a
# Call order (when invoking): a's wrapper → b's wrapper → c's wrapper → original
```

```python
import functools


def tag(name: str):
    """Wraps return value in an HTML tag."""
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            return f"<{name}>{func(*args, **kwargs)}</{name}>"
        return wrapper
    return decorator


@tag("div")
@tag("p")
@tag("strong")
def get_greeting(name: str) -> str:
    return f"Hello, {name}!"

print(get_greeting("Alice"))
# <div><p><strong>Hello, Alice!</strong></p></div>
# Applied bottom-up: strong first, then p, then div
```

---

## Real-World Decorators: Five Complete Implementations

### 1. @retry — Exponential Backoff

```python
import functools
import time
import random
import logging

logger = logging.getLogger(__name__)


def retry(
    max_attempts: int = 3,
    base_delay: float = 1.0,
    max_delay: float = 60.0,
    exponential: bool = True,
    jitter: bool = True,
    exceptions: tuple[type[Exception], ...] = (Exception,),
):
    """
    Retry with exponential backoff and optional jitter.
    
    - max_attempts: total number of tries (including first)
    - base_delay: seconds to wait after first failure
    - max_delay: maximum wait time (cap for exponential growth)
    - exponential: if True, delay doubles each attempt
    - jitter: if True, add random variation to avoid thundering herd
    - exceptions: only retry on these exception types
    """
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            last_exception = None
            for attempt in range(1, max_attempts + 1):
                try:
                    return func(*args, **kwargs)
                except exceptions as e:
                    last_exception = e
                    if attempt == max_attempts:
                        break
                    
                    if exponential:
                        delay = min(base_delay * (2 ** (attempt - 1)), max_delay)
                    else:
                        delay = base_delay
                    
                    if jitter:
                        delay *= (0.5 + random.random() * 0.5)
                    
                    logger.warning(
                        f"[retry] {func.__name__} failed (attempt {attempt}/{max_attempts}): "
                        f"{type(e).__name__}: {e}. Retrying in {delay:.2f}s..."
                    )
                    time.sleep(delay)
            
            raise last_exception
        return wrapper
    return decorator


@retry(max_attempts=5, base_delay=0.5, exceptions=(ConnectionError,))
def connect_to_service(host: str, port: int) -> bool:
    # Simulated connection attempt
    raise ConnectionError(f"Cannot connect to {host}:{port}")
```

### 2. @timer — Measure Execution Time

```python
import functools
import time
import logging

logger = logging.getLogger(__name__)


def timer(func=None, *, log_level: str = "debug", threshold_ms: float = 0.0):
    """
    Time a function and log the result.
    
    Can be used with or without arguments:
        @timer                          # no arguments
        @timer()                        # empty call
        @timer(log_level="warning", threshold_ms=100)  # with config
    """
    # Support both @timer and @timer()
    if func is None:
        return lambda f: timer(f, log_level=log_level, threshold_ms=threshold_ms)
    
    log_fn = getattr(logger, log_level, logger.debug)
    
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        start = time.perf_counter()
        result = func(*args, **kwargs)
        elapsed_ms = (time.perf_counter() - start) * 1000
        
        if elapsed_ms >= threshold_ms:
            log_fn(f"[timer] {func.__qualname__} took {elapsed_ms:.2f}ms")
        
        return result
    
    return wrapper


@timer
def fast_function():
    return sum(range(1000))


@timer(log_level="warning", threshold_ms=50)
def potentially_slow_function(n: int) -> int:
    return sum(range(n))
```

### 3. @cache_result — Function-Level Caching

```python
import functools
import time
from typing import Any, Callable


def cache_result(ttl_seconds: float | None = None, max_size: int | None = None):
    """
    Cache function results with optional TTL and max size.
    
    This is a simplified version of functools.lru_cache with TTL support.
    For production use, functools.lru_cache or functools.cache are faster.
    """
    def decorator(func: Callable) -> Callable:
        cache: dict[tuple, tuple[Any, float]] = {}  # key → (result, timestamp)
        
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            # Create cache key from arguments
            # Note: kwargs must be sorted to be order-independent
            key = (args, tuple(sorted(kwargs.items())))
            
            now = time.time()
            
            # Check if cached and not expired
            if key in cache:
                result, cached_at = cache[key]
                if ttl_seconds is None or (now - cached_at) < ttl_seconds:
                    wrapper.cache_hits += 1
                    return result
            
            # Compute and cache
            result = func(*args, **kwargs)
            
            # Evict oldest if at max size
            if max_size is not None and len(cache) >= max_size:
                oldest_key = min(cache, key=lambda k: cache[k][1])
                del cache[oldest_key]
            
            cache[key] = (result, now)
            wrapper.cache_misses += 1
            return result
        
        wrapper.cache = cache
        wrapper.cache_hits = 0
        wrapper.cache_misses = 0
        wrapper.cache_clear = lambda: cache.clear()
        
        return wrapper
    return decorator


@cache_result(ttl_seconds=300, max_size=100)
def fetch_user_profile(user_id: str) -> dict:
    """Expensive database lookup — results cached for 5 minutes."""
    print(f"[cache_result] Fetching user {user_id} from database...")
    return {"id": user_id, "name": "Alice", "email": "alice@example.com"}


# First call: hits the database
profile = fetch_user_profile("user-123")
# Second call: returns from cache
profile = fetch_user_profile("user-123")

print(f"Cache hits: {fetch_user_profile.cache_hits}")    # 1
print(f"Cache misses: {fetch_user_profile.cache_misses}") # 1
```

### 4. @validate_types — Runtime Type Checking

```python
import functools
import inspect
from typing import get_type_hints


def validate_types(func: Callable) -> Callable:
    """
    Validate argument types against type hints at runtime.
    
    Raises TypeError if any argument doesn't match its hint.
    Only validates hints that are actual types (skips complex generics).
    
    Note: For production, use Pydantic or beartype instead.
    """
    hints = get_type_hints(func)
    sig = inspect.signature(func)
    
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        bound = sig.bind(*args, **kwargs)
        bound.apply_defaults()
        
        for param_name, value in bound.arguments.items():
            if param_name in hints:
                expected_type = hints[param_name]
                # Only validate simple types (not generics like list[str])
                if isinstance(expected_type, type) and not isinstance(value, expected_type):
                    raise TypeError(
                        f"{func.__name__}(): argument '{param_name}' "
                        f"expected {expected_type.__name__}, "
                        f"got {type(value).__name__} ({value!r})"
                    )
        
        return func(*args, **kwargs)
    
    return wrapper


@validate_types
def create_user(username: str, age: int, is_admin: bool = False) -> dict:
    return {"username": username, "age": age, "is_admin": is_admin}


create_user("alice", 30)       # OK
create_user("bob", 25, True)   # OK
# create_user("carol", "thirty")  # TypeError: age expected int, got str
```

### 5. @require_auth — Authentication Check

```python
import functools
from typing import Callable


def require_auth(roles: list[str] | None = None):
    """
    Decorator for protected functions that require authentication.
    
    In a real web app, this would check the session or JWT token.
    Here it checks a mock "current_user" context.
    
    Usage:
        @require_auth()                    # Any authenticated user
        @require_auth(roles=["admin"])     # Admin only
    """
    def decorator(func: Callable) -> Callable:
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            # In Flask: current_user = flask_login.current_user
            # In FastAPI: current_user = Depends(get_current_user)
            # Here: look for 'current_user' in kwargs
            current_user = kwargs.get("current_user")
            
            if current_user is None:
                raise PermissionError(
                    f"Authentication required to call {func.__name__}()"
                )
            
            if roles and current_user.get("role") not in roles:
                raise PermissionError(
                    f"Role '{current_user.get('role')}' cannot call {func.__name__}(). "
                    f"Required roles: {roles}"
                )
            
            return func(*args, **kwargs)
        return wrapper
    return decorator


@require_auth(roles=["admin"])
def delete_user(user_id: str, current_user: dict) -> bool:
    print(f"Admin {current_user['name']} deleted user {user_id}")
    return True


# Usage:
admin = {"name": "Alice", "role": "admin"}
regular = {"name": "Bob", "role": "user"}

delete_user("user-456", current_user=admin)   # Works

try:
    delete_user("user-456", current_user=regular)  # PermissionError
except PermissionError as e:
    print(f"Blocked: {e}")
```

---

## Summary: Decorator Patterns

| Pattern | Use Case | Key Technique |
|---------|----------|--------------|
| Simple | Add behavior to a function | `wrapper(*args, **kwargs)` |
| Parametrized | Configurable behavior | Three nested functions |
| Class-based | Stateful behavior | `__call__` method |
| Stacking | Multiple behaviors | Applied bottom-up |
| Preserving metadata | All production decorators | `@functools.wraps(func)` |

**The most important rule:** Always use `@functools.wraps`. Every time. No exceptions.
