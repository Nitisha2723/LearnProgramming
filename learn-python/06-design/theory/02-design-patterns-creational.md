# Creational Design Patterns in Python

Creational patterns deal with object creation. They answer the question: **how do we create objects in a way that is flexible, controlled, and decoupled from the using code?**

---

## Python-Specific Insight: Module System as Singleton

Before diving into patterns, the most important Python insight about creational patterns:

**Python's module import system is already a singleton system.**

When you write `import config` in ten different files, Python loads that module exactly once and caches it. Every `import config` after the first returns the same object. This means:

```python
# config.py — This IS a singleton. No pattern needed.
DATABASE_URL = "postgresql://localhost/mydb"
DEBUG = False
MAX_RETRIES = 3
```

```python
# file_a.py
import config
print(config.DATABASE_URL)  # "postgresql://localhost/mydb"
config.MAX_RETRIES = 5       # Changes the shared module state
```

```python
# file_b.py
import config
print(config.MAX_RETRIES)   # 5 — sees the change from file_a.py!
```

This is Python's most natural "singleton." Use it freely.

---

## 1. Singleton Pattern

**Intent:** Ensure a class has only one instance, and provide a global access point to it.

**When to use:** Database connection pools, configuration objects, loggers, caches.

### Module-Level Singleton (Most Pythonic)

```python
# logger_singleton.py
# The module IS the singleton. Import this module everywhere.

import logging
import sys
from datetime import datetime


# This code runs ONCE when the module is first imported.
# Every subsequent import returns this same logger object.

_logger = logging.getLogger("myapp")
_logger.setLevel(logging.DEBUG)

_handler = logging.StreamHandler(sys.stdout)
_handler.setFormatter(
    logging.Formatter("%(asctime)s [%(levelname)s] %(message)s")
)
_logger.addHandler(_handler)


def get_logger() -> logging.Logger:
    """Return the application logger singleton."""
    return _logger
```

```python
# Usage from anywhere in your app:
from logger_singleton import get_logger

log = get_logger()
log.info("Application started")
log.error("Something went wrong")
```

### Singleton Using a Metaclass

When you need a singleton that is a class (not a module):

```python
# singleton_metaclass.py

class SingletonMeta(type):
    """
    Metaclass that makes any class it creates into a singleton.
    
    How it works:
    - __call__ is invoked when you call a class: MyClass()
    - We intercept that call
    - If an instance already exists, return it
    - Otherwise, create one and store it
    """

    _instances: dict[type, object] = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            instance = super().__call__(*args, **kwargs)
            cls._instances[cls] = instance
        return cls._instances[cls]


class DatabaseConnection(metaclass=SingletonMeta):
    """Database connection pool — only one should ever exist."""

    def __init__(self, host: str, port: int, database: str):
        self.host = host
        self.port = port
        self.database = database
        self._connection_count = 0
        print(f"[DatabaseConnection] Connected to {host}:{port}/{database}")

    def execute(self, query: str) -> list:
        self._connection_count += 1
        print(f"[DB] Executing query #{self._connection_count}: {query}")
        return []  # Simulated result

    def __repr__(self) -> str:
        return f"DatabaseConnection({self.host}:{self.port}/{self.database})"


# Demonstration
db1 = DatabaseConnection("localhost", 5432, "myapp")  # Creates the instance
db2 = DatabaseConnection("other-host", 5432, "other")  # Returns the SAME instance

print(db1 is db2)    # True — same object
print(db1.host)      # "localhost" — second call ignored
print(db2.host)      # "localhost" — same object
```

### Thread-Safe Singleton

```python
# singleton_thread_safe.py
import threading


class ThreadSafeSingleton(metaclass=SingletonMeta):
    """Thread-safe version using a lock."""

    _lock: threading.Lock = threading.Lock()
    _instances: dict = {}

    def __call__(cls, *args, **kwargs):
        with cls._lock:
            if cls not in cls._instances:
                instance = super().__call__(*args, **kwargs)
                cls._instances[cls] = instance
        return cls._instances[cls]
```

---

## 2. Factory Pattern

**Intent:** Provide an interface for creating objects without specifying the exact class.

### Factory Functions vs Factory Classes

In Python, **factory functions are almost always more Pythonic than factory classes**.

A factory function is just a function that creates and returns an object:

```python
# factory_function.py
# Pythonic: a simple function

from decimal import Decimal
from typing import Protocol


class PaymentProcessor(Protocol):
    def process(self, amount: Decimal) -> dict:
        ...


class StripeProcessor:
    def __init__(self, api_key: str):
        self.api_key = api_key

    def process(self, amount: Decimal) -> dict:
        return {"processor": "stripe", "amount": str(amount), "status": "ok"}


class PayPalProcessor:
    def __init__(self, client_id: str, secret: str):
        self.client_id = client_id
        self.secret = secret

    def process(self, amount: Decimal) -> dict:
        return {"processor": "paypal", "amount": str(amount), "status": "ok"}


class MockProcessor:
    """For testing."""
    def process(self, amount: Decimal) -> dict:
        return {"processor": "mock", "amount": str(amount), "status": "ok"}


def create_payment_processor(processor_type: str, **config) -> PaymentProcessor:
    """
    Factory function — creates the right processor based on configuration.
    
    This is idiomatic Python. A factory function, not a factory class.
    """
    processors = {
        "stripe": lambda: StripeProcessor(config.get("api_key", "")),
        "paypal": lambda: PayPalProcessor(
            config.get("client_id", ""),
            config.get("secret", ""),
        ),
        "mock": lambda: MockProcessor(),
    }

    creator = processors.get(processor_type)
    if not creator:
        raise ValueError(
            f"Unknown processor '{processor_type}'. "
            f"Available: {list(processors.keys())}"
        )

    return creator()


# Usage:
stripe = create_payment_processor("stripe", api_key="sk_test_123")
paypal = create_payment_processor("paypal", client_id="CLIENT", secret="SECRET")
mock = create_payment_processor("mock")
```

### Factory Class Pattern

When factory logic is complex enough to warrant its own class:

```python
# factory_class.py

from dataclasses import dataclass
from typing import ClassVar


@dataclass
class DatabaseConfig:
    host: str
    port: int
    database: str
    user: str
    password: str


class DatabaseConnectionFactory:
    """Factory class for creating database connections.
    
    Use a factory class when:
    - The factory itself needs configuration or state
    - You want to support multiple creation methods (create_from_url, create_from_env, etc.)
    - You need the factory itself to be injected as a dependency
    """

    _default_ports: ClassVar[dict[str, int]] = {
        "postgresql": 5432,
        "mysql": 3306,
        "mongodb": 27017,
    }

    def __init__(self, environment: str = "production"):
        self._env = environment

    def create_from_config(self, config: DatabaseConfig):
        """Create connection from explicit config."""
        print(f"[{self._env}] Connecting to {config.host}:{config.port}/{config.database}")
        # Real code would return an actual connection
        return {"type": "db", "config": config, "env": self._env}

    def create_from_url(self, url: str):
        """Create connection by parsing a database URL.
        
        Supports: postgresql://user:pass@host:port/db
        """
        from urllib.parse import urlparse
        parsed = urlparse(url)
        config = DatabaseConfig(
            host=parsed.hostname or "localhost",
            port=parsed.port or self._default_ports.get(parsed.scheme, 5432),
            database=parsed.path.lstrip("/"),
            user=parsed.username or "",
            password=parsed.password or "",
        )
        return self.create_from_config(config)

    def create_from_env(self):
        """Create connection from environment variables."""
        import os
        config = DatabaseConfig(
            host=os.getenv("DB_HOST", "localhost"),
            port=int(os.getenv("DB_PORT", "5432")),
            database=os.getenv("DB_NAME", "myapp"),
            user=os.getenv("DB_USER", "postgres"),
            password=os.getenv("DB_PASSWORD", ""),
        )
        return self.create_from_config(config)
```

---

## 3. Abstract Factory Pattern

**Intent:** Create families of related objects without specifying their concrete classes.

Use when you need to create multiple related objects that must work together.

```python
# abstract_factory.py
# Creating UI components for different themes/platforms

from abc import ABC, abstractmethod
from typing import Protocol


# ─── Abstract Products ────────────────────────────────────────────────────────

class Button(Protocol):
    def render(self) -> str:
        ...
    def on_click(self) -> str:
        ...


class TextInput(Protocol):
    def render(self) -> str:
        ...
    def get_value(self) -> str:
        ...


# ─── Abstract Factory ─────────────────────────────────────────────────────────

class UIFactory(Protocol):
    """Abstract factory for creating UI components.
    
    Concrete factories implement this to create consistent sets of widgets.
    """

    def create_button(self, label: str) -> Button:
        ...

    def create_text_input(self, placeholder: str) -> TextInput:
        ...


# ─── Light Theme ──────────────────────────────────────────────────────────────

class LightButton:
    def __init__(self, label: str):
        self.label = label

    def render(self) -> str:
        return f"[LIGHT BUTTON: {self.label}]"

    def on_click(self) -> str:
        return f"Light button '{self.label}' clicked"


class LightTextInput:
    def __init__(self, placeholder: str):
        self.placeholder = placeholder
        self._value = ""

    def render(self) -> str:
        return f"[LIGHT INPUT: placeholder='{self.placeholder}']"

    def get_value(self) -> str:
        return self._value


class LightThemeFactory:
    """Creates light-themed UI components."""

    def create_button(self, label: str) -> LightButton:
        return LightButton(label)

    def create_text_input(self, placeholder: str) -> LightTextInput:
        return LightTextInput(placeholder)


# ─── Dark Theme ───────────────────────────────────────────────────────────────

class DarkButton:
    def __init__(self, label: str):
        self.label = label

    def render(self) -> str:
        return f"[DARK BUTTON: {self.label}]"

    def on_click(self) -> str:
        return f"Dark button '{self.label}' clicked"


class DarkTextInput:
    def __init__(self, placeholder: str):
        self.placeholder = placeholder
        self._value = ""

    def render(self) -> str:
        return f"[DARK INPUT: placeholder='{self.placeholder}']"

    def get_value(self) -> str:
        return self._value


class DarkThemeFactory:
    """Creates dark-themed UI components."""

    def create_button(self, label: str) -> DarkButton:
        return DarkButton(label)

    def create_text_input(self, placeholder: str) -> DarkTextInput:
        return DarkTextInput(placeholder)


# ─── Client code (uses the abstract factory) ──────────────────────────────────

class LoginForm:
    """Builds a login form using whatever factory is provided."""

    def __init__(self, factory: UIFactory):
        self.submit_btn = factory.create_button("Log In")
        self.username_input = factory.create_text_input("Enter username")
        self.password_input = factory.create_text_input("Enter password")

    def render(self) -> str:
        parts = [
            self.username_input.render(),
            self.password_input.render(),
            self.submit_btn.render(),
        ]
        return "\n".join(parts)


# Usage:
light_form = LoginForm(LightThemeFactory())
dark_form = LoginForm(DarkThemeFactory())

print("=== Light Theme ===")
print(light_form.render())

print("\n=== Dark Theme ===")
print(dark_form.render())
```

---

## 4. Builder Pattern

**Intent:** Construct complex objects step by step. Separate construction from representation.

**Python insight:** Python's `dataclass` with default values handles many simple builder cases.
Use the fluent builder pattern when construction has optional steps and order matters.

### Using Dataclass for Simple Building

```python
# builder_dataclass.py
from dataclasses import dataclass, field


@dataclass
class HttpRequest:
    """Dataclass with defaults handles simple object creation elegantly."""
    url: str
    method: str = "GET"
    headers: dict[str, str] = field(default_factory=dict)
    params: dict[str, str] = field(default_factory=dict)
    body: str | None = None
    timeout: int = 30
    auth_token: str | None = None

    def with_json_content(self) -> "HttpRequest":
        """Return a new request with JSON content-type header."""
        from dataclasses import replace
        new_headers = {**self.headers, "Content-Type": "application/json"}
        return replace(self, headers=new_headers)
```

### Fluent Builder Pattern

```python
# builder_fluent.py

from dataclasses import dataclass, field


@dataclass
class HttpRequest:
    url: str
    method: str
    headers: dict[str, str]
    params: dict[str, str]
    body: str | None
    timeout: int
    auth_token: str | None


class HttpRequestBuilder:
    """Fluent builder for HttpRequest objects.
    
    Fluent interface: each method returns self, enabling method chaining.
    The build() method creates the final immutable object.
    """

    def __init__(self, url: str):
        self._url = url
        self._method = "GET"
        self._headers: dict[str, str] = {}
        self._params: dict[str, str] = {}
        self._body: str | None = None
        self._timeout = 30
        self._auth_token: str | None = None

    def method(self, method: str) -> "HttpRequestBuilder":
        self._method = method.upper()
        return self

    def header(self, key: str, value: str) -> "HttpRequestBuilder":
        self._headers[key] = value
        return self

    def param(self, key: str, value: str) -> "HttpRequestBuilder":
        self._params[key] = value
        return self

    def body(self, body: str) -> "HttpRequestBuilder":
        self._body = body
        return self

    def json_body(self, data: dict) -> "HttpRequestBuilder":
        """Set body as JSON and add the Content-Type header."""
        import json
        self._body = json.dumps(data)
        self._headers["Content-Type"] = "application/json"
        return self

    def timeout(self, seconds: int) -> "HttpRequestBuilder":
        if seconds <= 0:
            raise ValueError("Timeout must be positive")
        self._timeout = seconds
        return self

    def bearer_auth(self, token: str) -> "HttpRequestBuilder":
        self._auth_token = token
        self._headers["Authorization"] = f"Bearer {token}"
        return self

    def build(self) -> HttpRequest:
        """Validate and construct the final HttpRequest object."""
        if not self._url.startswith(("http://", "https://")):
            raise ValueError(f"Invalid URL: {self._url}")

        return HttpRequest(
            url=self._url,
            method=self._method,
            headers=dict(self._headers),
            params=dict(self._params),
            body=self._body,
            timeout=self._timeout,
            auth_token=self._auth_token,
        )


# Usage — method chaining reads almost like English:
request = (
    HttpRequestBuilder("https://api.example.com/users")
    .method("POST")
    .bearer_auth("my-secret-token")
    .json_body({"name": "Alice", "email": "alice@example.com"})
    .timeout(10)
    .build()
)

print(request.method)           # POST
print(request.headers)          # {'Content-Type': 'application/json', 'Authorization': 'Bearer my-secret-token'}
print(request.timeout)          # 10
```

---

## 5. Prototype Pattern

**Intent:** Create new objects by copying existing ones.

**Python tool:** The `copy` module provides `copy()` (shallow) and `deepcopy()` (deep).

```python
# prototype.py
import copy
from dataclasses import dataclass, field


@dataclass
class Address:
    street: str
    city: str
    country: str


@dataclass
class UserProfile:
    """A complex object we want to clone."""
    name: str
    email: str
    address: Address
    tags: list[str] = field(default_factory=list)
    preferences: dict[str, str] = field(default_factory=dict)

    def clone(self) -> "UserProfile":
        """Create a deep copy of this profile."""
        return copy.deepcopy(self)

    def clone_with(self, **overrides) -> "UserProfile":
        """Clone and override specific fields.
        
        This is a very Pythonic pattern for creating variants of an object.
        """
        cloned = self.clone()
        for key, value in overrides.items():
            if not hasattr(cloned, key):
                raise AttributeError(f"UserProfile has no attribute '{key}'")
            setattr(cloned, key, value)
        return cloned


# Usage:
alice = UserProfile(
    name="Alice",
    email="alice@example.com",
    address=Address("123 Main St", "Boston", "USA"),
    tags=["admin", "verified"],
    preferences={"theme": "dark", "language": "en"},
)

# Clone Alice's profile for Bob — same address, different name and email
bob = alice.clone_with(
    name="Bob",
    email="bob@example.com",
    tags=["verified"],
)

# Verify independence — modifying bob doesn't affect alice
bob.tags.append("new-user")
bob.address.city = "New York"

print(alice.tags)         # ["admin", "verified"] — unchanged
print(alice.address.city) # "Boston" — unchanged

print(bob.tags)           # ["verified", "new-user"]
print(bob.address.city)   # "New York"
```

### Shallow vs Deep Copy

```python
# Understanding the difference

original = [[1, 2, 3], [4, 5, 6]]

shallow = copy.copy(original)     # Copies the outer list, but inner lists are shared
deep = copy.deepcopy(original)    # Copies everything recursively

# Mutate the inner list
original[0].append(99)

print(original)  # [[1, 2, 3, 99], [4, 5, 6]]
print(shallow)   # [[1, 2, 3, 99], [4, 5, 6]] — inner list is SHARED
print(deep)      # [[1, 2, 3], [4, 5, 6]]     — completely independent
```

---

## Pattern Comparison

| Pattern | Intent | Python Idiom |
|---------|--------|-------------|
| Singleton | One instance globally | Module-level object or `SingletonMeta` |
| Factory | Create objects by type | Factory function (preferred) or class |
| Abstract Factory | Create related object families | Factory function returning dataclass |
| Builder | Step-by-step construction | Fluent builder or `dataclass` with `replace()` |
| Prototype | Clone existing objects | `copy.deepcopy()` with `clone_with()` |

---

## When to Use Each

- **Singleton:** When exactly one instance must exist (config, logger, connection pool)
- **Factory:** When the caller should not know the exact class being created
- **Abstract Factory:** When you need to create families of related objects (themes, platforms)
- **Builder:** When creating complex objects with many optional configuration steps
- **Prototype:** When creating many similar objects based on a template

**The most important question before using any pattern:** Is the code simpler with the pattern than without it? If not, skip the pattern.
