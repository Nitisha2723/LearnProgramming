# SOLID Principles in Python

SOLID is a set of five design principles that make software easier to understand, maintain, and extend.
The acronym stands for: **S**ingle Responsibility, **O**pen/Closed, **L**iskov Substitution, **I**nterface Segregation, **D**ependency Inversion.

These principles were formulated for object-oriented programming, but Python adds its own flavor to each one.

---

## Why SOLID Matters

Imagine you join a project and open a file called `user_manager.py`. Inside, you find a 600-line class that:
- Validates user input
- Hashes passwords
- Sends email confirmations
- Writes user data to the database
- Generates PDF receipts
- Logs every action

This is a real-world anti-pattern. Every time any requirement changes — a new email template, a different database, a new logging format — you are editing this one file. Every change risks breaking something else. Tests are nearly impossible because the class has ten external dependencies.

SOLID principles prevent this. They are not theoretical — they are practical tools for writing code that survives contact with changing requirements.

---

## S — Single Responsibility Principle

> A class should have only one reason to change.

More practically: **a module, class, or function should do one thing and do it well**.

### Python's Natural SRP Boundary: Modules

Python's module system encourages SRP naturally. A Python module is a `.py` file. When you write `from email_service import send_email`, you have already applied SRP — the email logic lives in one place.

The Python standard library demonstrates this beautifully:
- `json` — only for JSON serialization/deserialization
- `csv` — only for CSV reading/writing
- `pathlib` — only for filesystem path operations
- `hashlib` — only for cryptographic hashing

### Violation Example

```python
# srp_violation.py
# BAD: One class doing too many things

import json
import hashlib
import smtplib
from datetime import datetime


class UserManager:
    """Does WAY too much. This is called a 'God Class'."""

    def __init__(self, db_connection):
        self.db = db_connection

    def register_user(self, username: str, email: str, password: str) -> dict:
        # 1. Validate input
        if len(username) < 3:
            raise ValueError("Username too short")
        if "@" not in email:
            raise ValueError("Invalid email")
        if len(password) < 8:
            raise ValueError("Password too short")

        # 2. Hash the password
        hashed = hashlib.sha256(password.encode()).hexdigest()

        # 3. Save to database
        user = {
            "id": self._generate_id(),
            "username": username,
            "email": email,
            "password_hash": hashed,
            "created_at": datetime.now().isoformat(),
        }
        self.db.execute(
            "INSERT INTO users VALUES (?, ?, ?, ?, ?)",
            (user["id"], username, email, hashed, user["created_at"]),
        )

        # 4. Send welcome email
        with smtplib.SMTP("smtp.gmail.com", 587) as smtp:
            smtp.ehlo()
            smtp.starttls()
            smtp.login("app@example.com", "secret")
            smtp.sendmail(
                "app@example.com",
                email,
                f"Subject: Welcome!\n\nHello {username}, welcome!",
            )

        # 5. Log the action
        with open("app.log", "a") as f:
            f.write(f"{datetime.now()} - User registered: {username}\n")

        # 6. Generate audit report
        report = {"action": "user_registered", "user": username, "time": datetime.now().isoformat()}
        with open(f"audit_{username}.json", "w") as f:
            json.dump(report, f)

        return user

    def _generate_id(self) -> str:
        import uuid
        return str(uuid.uuid4())
```

This class has **six reasons to change**:
1. Validation rules change
2. Password hashing algorithm changes
3. Database schema changes
4. Email provider or template changes
5. Log format changes
6. Audit report format changes

### Correct Example

```python
# srp_correct.py
# GOOD: Each class has one responsibility

import hashlib
import logging
import uuid
from dataclasses import dataclass, field
from datetime import datetime
from typing import Protocol

logger = logging.getLogger(__name__)


# ─── Data ───────────────────────────────────────────────────────────────────

@dataclass
class User:
    """Just data. No behavior beyond being a user."""
    username: str
    email: str
    password_hash: str
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    created_at: str = field(default_factory=lambda: datetime.now().isoformat())


# ─── Validator ──────────────────────────────────────────────────────────────

class UserValidator:
    """One job: validate user input. That's it."""

    def validate(self, username: str, email: str, password: str) -> None:
        """Raise ValueError if any field is invalid."""
        if len(username) < 3:
            raise ValueError(f"Username '{username}' is too short (minimum 3 characters)")
        if "@" not in email or "." not in email.split("@")[-1]:
            raise ValueError(f"'{email}' is not a valid email address")
        if len(password) < 8:
            raise ValueError("Password must be at least 8 characters")


# ─── Password Service ────────────────────────────────────────────────────────

class PasswordService:
    """One job: securely handle passwords."""

    def hash(self, plain_password: str) -> str:
        """Hash a password using SHA-256 (use bcrypt in production!)."""
        return hashlib.sha256(plain_password.encode()).hexdigest()

    def verify(self, plain_password: str, hashed: str) -> bool:
        """Check if a plain password matches a hash."""
        return self.hash(plain_password) == hashed


# ─── Repository ──────────────────────────────────────────────────────────────

class UserRepository:
    """One job: persist and retrieve users."""

    def __init__(self, db_connection):
        self.db = db_connection

    def save(self, user: User) -> None:
        self.db.execute(
            "INSERT INTO users VALUES (?, ?, ?, ?, ?)",
            (user.id, user.username, user.email, user.password_hash, user.created_at),
        )

    def find_by_email(self, email: str) -> User | None:
        row = self.db.execute(
            "SELECT id, username, email, password_hash, created_at FROM users WHERE email = ?",
            (email,),
        ).fetchone()
        if row:
            return User(row[1], row[2], row[3], row[0], row[4])
        return None


# ─── Email Service ───────────────────────────────────────────────────────────

class EmailService:
    """One job: send emails."""

    def send_welcome(self, user: User) -> None:
        # In real code, use a proper email library like `emails` or call an API
        logger.info(f"Sending welcome email to {user.email}")
        # ... actual email sending code ...


# ─── Registration Service ────────────────────────────────────────────────────

class UserRegistrationService:
    """Orchestrates the registration flow using the single-responsibility classes above."""

    def __init__(
        self,
        validator: UserValidator,
        password_service: PasswordService,
        repository: UserRepository,
        email_service: EmailService,
    ):
        self._validator = validator
        self._password_service = password_service
        self._repository = repository
        self._email_service = email_service

    def register(self, username: str, email: str, password: str) -> User:
        # Validate
        self._validator.validate(username, email, password)

        # Create user
        hashed = self._password_service.hash(password)
        user = User(username=username, email=email, password_hash=hashed)

        # Persist
        self._repository.save(user)

        # Notify
        self._email_service.send_welcome(user)

        logger.info(f"User registered: {username}")
        return user
```

Now each class has **exactly one reason to change**:
- `UserValidator` changes only when validation rules change
- `PasswordService` changes only when the hashing algorithm changes
- `UserRepository` changes only when the persistence layer changes
- `EmailService` changes only when email logic changes
- `UserRegistrationService` changes only when the registration flow changes

### When You Will See SRP Violated

- Classes named `Manager`, `Handler`, `Processor` that do everything
- Functions longer than ~30 lines (usually doing multiple things)
- A single file with 1000+ lines covering multiple concepts

---

## O — Open/Closed Principle

> Software entities should be **open for extension** but **closed for modification**.

You should be able to add new behavior without changing existing, tested code.

### Using ABCs for Extension Points

Python's `abc` module lets you define abstract base classes. These are extension points — they define a contract that new implementations must follow.

### Violation Example

```python
# ocp_violation.py
# BAD: Every new payment type requires editing this function

def process_payment(amount: float, payment_type: str) -> str:
    if payment_type == "credit_card":
        # Process credit card
        return f"Charged {amount} to credit card"
    elif payment_type == "paypal":
        # Process PayPal
        return f"Sent {amount} via PayPal"
    elif payment_type == "bitcoin":
        # Process Bitcoin
        return f"Transferred {amount} BTC"
    # Adding a new payment method requires editing this function!
    # Every edit risks breaking existing cases.
    else:
        raise ValueError(f"Unknown payment type: {payment_type}")
```

### Correct Example Using ABC

```python
# ocp_correct_abc.py
# GOOD: New payment types extend the system without modifying existing code

from abc import ABC, abstractmethod
from dataclasses import dataclass
from decimal import Decimal


@dataclass
class PaymentResult:
    success: bool
    transaction_id: str
    message: str


class PaymentProcessor(ABC):
    """Abstract base class — the extension point.
    
    To add a new payment method, create a new class that inherits from this.
    No existing code changes.
    """

    @abstractmethod
    def process(self, amount: Decimal) -> PaymentResult:
        """Process a payment. Subclasses must implement this."""
        ...

    @abstractmethod
    def refund(self, transaction_id: str, amount: Decimal) -> PaymentResult:
        """Refund a payment. Subclasses must implement this."""
        ...


class CreditCardProcessor(PaymentProcessor):
    """Handles credit card payments."""

    def __init__(self, api_key: str):
        self._api_key = api_key

    def process(self, amount: Decimal) -> PaymentResult:
        # Real implementation would call the credit card API
        txn_id = f"CC-{hash(amount)}"
        return PaymentResult(True, txn_id, f"Charged {amount} to credit card")

    def refund(self, transaction_id: str, amount: Decimal) -> PaymentResult:
        return PaymentResult(True, f"REF-{transaction_id}", f"Refunded {amount}")


class PayPalProcessor(PaymentProcessor):
    """Handles PayPal payments."""

    def __init__(self, client_id: str, client_secret: str):
        self._client_id = client_id
        self._client_secret = client_secret

    def process(self, amount: Decimal) -> PaymentResult:
        txn_id = f"PP-{hash(amount)}"
        return PaymentResult(True, txn_id, f"Sent {amount} via PayPal")

    def refund(self, transaction_id: str, amount: Decimal) -> PaymentResult:
        return PaymentResult(True, f"REF-{transaction_id}", f"PayPal refund {amount}")


# --- Adding Bitcoin WITHOUT changing any existing code ---

class BitcoinProcessor(PaymentProcessor):
    """Handles Bitcoin payments — added LATER, no existing code modified."""

    def __init__(self, wallet_address: str):
        self._wallet = wallet_address

    def process(self, amount: Decimal) -> PaymentResult:
        txn_id = f"BTC-{hash(amount)}"
        return PaymentResult(True, txn_id, f"Transferred {amount} BTC")

    def refund(self, transaction_id: str, amount: Decimal) -> PaymentResult:
        return PaymentResult(False, "", "Bitcoin transactions are irreversible")


# This function never needs to change. It works with ANY PaymentProcessor.
def checkout(processor: PaymentProcessor, amount: Decimal) -> PaymentResult:
    """Process a checkout. Open for extension: any processor works here."""
    return processor.process(amount)
```

### Correct Example Using Protocol (More Pythonic)

```python
# ocp_correct_protocol.py
# Python's typing.Protocol enables structural subtyping (duck typing with type safety)

from decimal import Decimal
from typing import Protocol


class PaymentProcessor(Protocol):
    """Structural interface — no inheritance required.
    
    Any class with a process() method that matches this signature satisfies the protocol.
    """

    def process(self, amount: Decimal) -> dict:
        ...


class StripeProcessor:
    """Does NOT inherit from PaymentProcessor, but satisfies the Protocol."""

    def process(self, amount: Decimal) -> dict:
        return {"status": "ok", "processor": "stripe", "amount": str(amount)}


class SquareProcessor:
    """Also satisfies the Protocol without inheriting."""

    def process(self, amount: Decimal) -> dict:
        return {"status": "ok", "processor": "square", "amount": str(amount)}


def process_order(processor: PaymentProcessor, amount: Decimal) -> dict:
    """Works with any class that has a process() method — no inheritance needed."""
    return processor.process(amount)
```

### When You Will See OCP Violated

- Large `if/elif/else` chains on a type field
- `isinstance()` chains: `if isinstance(obj, CreditCard): ... elif isinstance(obj, PayPal): ...`
- A function that gets edited every time a new requirement is added

---

## L — Liskov Substitution Principle

> Objects of a subclass should be substitutable for objects of the superclass without breaking correctness.

This principle matters especially in Python because of duck typing. If a function accepts a `Bird` and you pass a `Penguin`, that Penguin had better not break when you call `.fly()`.

### Why LSP Matters More in Python

In Java, the compiler enforces method signatures. In Python, type hints are optional and duck typing is the norm. **This means LSP violations in Python fail at runtime, not compile time.** A duck-typed interface is an implicit contract — and LSP says you must honor it.

### Violation Example

```python
# lsp_violation.py
# BAD: Penguin violates the Bird contract

class Bird:
    def fly(self) -> str:
        return "I am flying!"

    def eat(self) -> str:
        return "I am eating!"


class Eagle(Bird):
    def fly(self) -> str:
        return "Soaring at 100 mph!"


class Penguin(Bird):
    def fly(self) -> str:
        # LSP VIOLATION: raises an exception instead of returning a string
        raise NotImplementedError("Penguins cannot fly!")

    def eat(self) -> str:
        return "Eating fish!"


def make_birds_fly(birds: list[Bird]) -> None:
    """This function assumes ALL Birds can fly — a reasonable assumption from the type."""
    for bird in birds:
        print(bird.fly())  # Crashes when it hits a Penguin!


birds = [Eagle(), Penguin()]
make_birds_fly(birds)  # ERROR: NotImplementedError
```

### Correct Example

```python
# lsp_correct.py
# GOOD: Separate the ability to fly into its own abstraction

from abc import ABC, abstractmethod


class Animal(ABC):
    """Base class for all animals. No assumption about flying."""

    @abstractmethod
    def eat(self) -> str:
        ...

    @abstractmethod
    def describe(self) -> str:
        ...


class FlyingAnimal(Animal, ABC):
    """Mixin for animals that CAN fly."""

    @abstractmethod
    def fly(self) -> str:
        ...


class Eagle(FlyingAnimal):
    def fly(self) -> str:
        return "Soaring at 100 mph!"

    def eat(self) -> str:
        return "Eating rabbits!"

    def describe(self) -> str:
        return "Eagle"


class Penguin(Animal):
    """Penguin is an Animal, but NOT a FlyingAnimal. It does not pretend to fly."""

    def swim(self) -> str:
        return "Swimming at 15 mph!"

    def eat(self) -> str:
        return "Eating fish!"

    def describe(self) -> str:
        return "Penguin"


def make_birds_fly(birds: list[FlyingAnimal]) -> None:
    """The type annotation now promises: only flying animals will be passed here."""
    for bird in birds:
        print(bird.fly())  # Safe — the type guarantees this works


# Eagles go in the flying list, penguins do not
flying_birds: list[FlyingAnimal] = [Eagle()]
all_animals: list[Animal] = [Eagle(), Penguin()]

make_birds_fly(flying_birds)  # Works perfectly
```

### The Rectangle/Square Problem

The classic LSP example: mathematically, a square IS a rectangle. But in OOP, `Square` cannot safely substitute for `Rectangle`:

```python
# lsp_rectangle_square.py
# Classic LSP violation

class Rectangle:
    def __init__(self, width: float, height: float):
        self.width = width
        self.height = height

    def area(self) -> float:
        return self.width * self.height


class Square(Rectangle):
    """LSP violation: changing width ALSO changes height"""

    def __init__(self, side: float):
        super().__init__(side, side)

    @property
    def width(self) -> float:
        return self._width

    @width.setter
    def width(self, value: float) -> None:
        self._width = value
        self._height = value  # Surprising! Changes height too.

    @property
    def height(self) -> float:
        return self._height

    @height.setter
    def height(self, value: float) -> None:
        self._height = value
        self._width = value  # Surprising! Changes width too.


def stretch_rectangle(rect: Rectangle) -> None:
    """Expected: doubles the width, keeps height."""
    rect.width = rect.width * 2
    # If rect is a Square, height also doubled — unexpected!
    print(f"Area: {rect.area()}")


r = Rectangle(4, 3)
stretch_rectangle(r)  # Area: 24.0 — as expected

s = Square(4)
stretch_rectangle(s)  # Area: 64.0 — NOT what the caller expected!
```

### When You Will See LSP Violated

- Overriding methods to raise `NotImplementedError`
- Subclass methods that ignore parameters the parent uses
- Subclass methods that return different types than the parent

---

## I — Interface Segregation Principle

> Clients should not be forced to depend on interfaces they do not use.

In Python, "interface" means Protocol or ABC. The principle: **make small, focused interfaces rather than one big one**.

### Python Protocols Are Naturally Segregated

Python's `Protocol` type from `typing` makes ISP natural. A `Protocol` with two methods is a perfectly valid interface — you do not need to inherit from a base class.

### Violation Example

```python
# isp_violation.py
# BAD: One fat interface forces all implementors to implement everything

from abc import ABC, abstractmethod


class WorkerInterface(ABC):
    """A huge interface that forces all workers to implement everything."""

    @abstractmethod
    def work(self) -> str:
        ...

    @abstractmethod
    def eat(self) -> str:
        ...

    @abstractmethod
    def sleep(self) -> str:
        ...

    @abstractmethod
    def write_report(self) -> str:  # Robots don't write reports!
        ...

    @abstractmethod
    def take_vacation(self) -> str:  # Robots don't take vacations!
        ...


class HumanWorker(WorkerInterface):
    def work(self) -> str:
        return "Human working"

    def eat(self) -> str:
        return "Human eating"

    def sleep(self) -> str:
        return "Human sleeping"

    def write_report(self) -> str:
        return "Human writing report"

    def take_vacation(self) -> str:
        return "Human on vacation"


class RobotWorker(WorkerInterface):
    def work(self) -> str:
        return "Robot working"

    def eat(self) -> str:
        raise NotImplementedError("Robots don't eat!")  # Forced to implement this!

    def sleep(self) -> str:
        raise NotImplementedError("Robots don't sleep!")

    def write_report(self) -> str:
        raise NotImplementedError("Robots don't write reports!")

    def take_vacation(self) -> str:
        raise NotImplementedError("Robots don't take vacations!")
```

### Correct Example Using Small Protocols

```python
# isp_correct.py
# GOOD: Small, focused protocols — each class implements only what it needs

from typing import Protocol


class Workable(Protocol):
    def work(self) -> str:
        ...


class Eatable(Protocol):
    def eat(self) -> str:
        ...


class Sleepable(Protocol):
    def sleep(self) -> str:
        ...


class Reportable(Protocol):
    def write_report(self) -> str:
        ...


class HumanWorker:
    """Implements all protocols relevant to humans."""

    def work(self) -> str:
        return "Human working productively"

    def eat(self) -> str:
        return "Human eating lunch"

    def sleep(self) -> str:
        return "Human sleeping 8 hours"

    def write_report(self) -> str:
        return "Human writing quarterly report"


class RobotWorker:
    """Only implements what robots actually do."""

    def work(self) -> str:
        return "Robot working at full capacity"

    # No eat(), sleep(), or write_report() — robots don't do these!
    # No LSP violations, no NotImplementedError.


def manage_work_shift(workers: list[Workable]) -> None:
    """Needs only Workable — accepts both humans and robots."""
    for worker in workers:
        print(worker.work())


def schedule_lunch(employees: list[Eatable]) -> None:
    """Needs only Eatable — only humans are passed here."""
    for employee in employees:
        print(employee.eat())


human = HumanWorker()
robot = RobotWorker()

manage_work_shift([human, robot])  # Both work
schedule_lunch([human])            # Only humans eat
```

---

## D — Dependency Inversion Principle

> High-level modules should not depend on low-level modules. Both should depend on abstractions.
> Abstractions should not depend on details. Details should depend on abstractions.

Simply: **code to interfaces, not to implementations**.

### Python Dependency Injection (Without a Framework)

Python does not have Spring. But DIP is easy to implement with constructor injection. Pass dependencies as constructor arguments, not creating them inside the class.

### Violation Example

```python
# dip_violation.py
# BAD: High-level service directly creates and depends on low-level components

import sqlite3
import smtplib


class UserRegistrationService:
    """Creates its own dependencies — tightly coupled to SQLite and SMTP."""

    def __init__(self):
        # VIOLATION: high-level module creates low-level modules
        self.db = sqlite3.connect("users.db")  # Hardcoded!
        self.email_server = smtplib.SMTP("smtp.gmail.com", 587)  # Hardcoded!

    def register(self, username: str, email: str) -> None:
        # Now this is impossible to test without SQLite and a real SMTP server
        self.db.execute("INSERT INTO users (username, email) VALUES (?, ?)", (username, email))
        self.email_server.sendmail("app@example.com", email, "Welcome!")
```

### Correct Example

```python
# dip_correct.py
# GOOD: Depend on abstractions, inject concrete implementations

from abc import ABC, abstractmethod
from typing import Protocol


# ─── Abstractions (the "interfaces") ─────────────────────────────────────────

class UserStorage(Protocol):
    """Abstract storage interface. Both high-level and low-level code depend on this."""

    def save_user(self, username: str, email: str) -> None:
        ...

    def find_user(self, email: str) -> dict | None:
        ...


class NotificationService(Protocol):
    """Abstract notification interface."""

    def send_welcome(self, email: str, username: str) -> None:
        ...


# ─── Concrete Implementations (the "details") ────────────────────────────────

class SqliteUserStorage:
    """Concrete SQLite implementation of UserStorage."""

    def __init__(self, db_path: str):
        import sqlite3
        self._conn = sqlite3.connect(db_path)
        self._conn.execute(
            "CREATE TABLE IF NOT EXISTS users (username TEXT, email TEXT)"
        )

    def save_user(self, username: str, email: str) -> None:
        self._conn.execute(
            "INSERT INTO users VALUES (?, ?)", (username, email)
        )
        self._conn.commit()

    def find_user(self, email: str) -> dict | None:
        row = self._conn.execute(
            "SELECT username, email FROM users WHERE email = ?", (email,)
        ).fetchone()
        return {"username": row[0], "email": row[1]} if row else None


class InMemoryUserStorage:
    """In-memory implementation — perfect for testing."""

    def __init__(self):
        self._users: dict[str, dict] = {}

    def save_user(self, username: str, email: str) -> None:
        self._users[email] = {"username": username, "email": email}

    def find_user(self, email: str) -> dict | None:
        return self._users.get(email)


class EmailNotificationService:
    """Real email implementation."""

    def send_welcome(self, email: str, username: str) -> None:
        print(f"[EMAIL] Sending welcome to {username} at {email}")
        # Real SMTP code would go here


class ConsoleNotificationService:
    """Console implementation — useful for development and testing."""

    def send_welcome(self, email: str, username: str) -> None:
        print(f"[CONSOLE] Welcome, {username}! (email: {email})")


# ─── High-Level Service (depends on abstractions, not concrete classes) ───────

class UserRegistrationService:
    """High-level service. Knows NOTHING about SQLite, SMTP, or any specific implementation."""

    def __init__(
        self,
        storage: UserStorage,          # Injected
        notifications: NotificationService,  # Injected
    ):
        self._storage = storage
        self._notifications = notifications

    def register(self, username: str, email: str) -> dict:
        if self._storage.find_user(email):
            raise ValueError(f"User with email {email} already exists")

        self._storage.save_user(username, email)
        self._notifications.send_welcome(email, username)

        return {"username": username, "email": email, "status": "registered"}


# ─── Wiring it together (in production code or a factory function) ────────────

def create_production_service() -> UserRegistrationService:
    """Create the service with real implementations."""
    return UserRegistrationService(
        storage=SqliteUserStorage("users.db"),
        notifications=EmailNotificationService(),
    )


def create_test_service() -> UserRegistrationService:
    """Create the service with in-memory implementations — no external dependencies!"""
    return UserRegistrationService(
        storage=InMemoryUserStorage(),
        notifications=ConsoleNotificationService(),
    )


# In tests:
# service = create_test_service()
# result = service.register("alice", "alice@example.com")
# assert result["status"] == "registered"
# — No database, no email server, fully testable
```

### When You Will See DIP Violated

- A class that calls `sqlite3.connect()` or `requests.get()` directly inside `__init__`
- A function that hardcodes URLs, file paths, or configuration values
- Tests that require a database or network to run

---

## SOLID Quick Reference

| Principle | One-Line Summary | Python Tool |
|-----------|-----------------|-------------|
| **S**RP | One class, one job | Python modules as natural boundaries |
| **O**CP | Extend without modifying | `ABC`, `Protocol`, `@abstractmethod` |
| **L**SP | Subclasses keep contracts | Careful inheritance design |
| **I**SP | Small interfaces | `typing.Protocol` (structural typing) |
| **D**IP | Depend on abstractions | Constructor injection, factory functions |

---

## The Most Important Insight

SOLID principles are not a checklist. They are lenses through which to view your code.

When you see a class that is hard to test, ask: "Does it have too many responsibilities?" (SRP)
When you need to add a feature and find yourself editing existing code, ask: "Is this truly closed to modification?" (OCP)
When a subclass raises `NotImplementedError`, ask: "Should this even be a subclass?" (LSP)
When a class imports something it never uses, ask: "Is the interface too fat?" (ISP)
When you cannot test a class without a database, ask: "Am I depending on abstractions?" (DIP)

These questions — not the rules themselves — are what SOLID gives you.
