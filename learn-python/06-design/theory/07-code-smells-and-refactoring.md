# Code Smells and Refactoring in Python

A code smell is a symptom in the code that indicates a deeper problem. Smells do not cause bugs by themselves, but they make code harder to understand, test, and change — which leads to bugs.

This module covers universal code smells plus Python-specific ones.

---

## Universal Code Smells in Python

### 1. Long Method

A function that does too much. Signs: it is hard to name with a single verb, it has many nested levels, it takes 5+ parameters.

```python
# SMELL: process_order does everything
def process_order(order_id, user_id, items, discount_code, shipping_address, payment_info):
    # Validate
    if not order_id or not user_id:
        raise ValueError("Missing IDs")
    if not items:
        raise ValueError("No items")
    
    # Apply discount
    total = sum(item['price'] * item['qty'] for item in items)
    if discount_code:
        codes = {'SAVE10': 0.10, 'SAVE20': 0.20, 'HALF': 0.50}
        if discount_code in codes:
            total *= (1 - codes[discount_code])
    
    # Validate address
    required_fields = ['street', 'city', 'country', 'postal_code']
    for field in required_fields:
        if field not in shipping_address:
            raise ValueError(f"Missing address field: {field}")
    
    # Process payment
    if payment_info['type'] == 'card':
        # ... 20 lines of card processing ...
        pass
    elif payment_info['type'] == 'paypal':
        # ... 15 lines of PayPal processing ...
        pass
    
    # Send email
    # ... 10 lines of email sending ...
    
    # Log to database
    # ... 15 lines of database operations ...
    
    return {"status": "success", "total": total}


# REFACTORED: Each step is its own function
def process_order(order_id: str, user_id: str, items: list, discount_code: str | None,
                  shipping_address: dict, payment_info: dict) -> dict:
    validate_order_input(order_id, user_id, items)
    validate_shipping_address(shipping_address)

    total = calculate_total(items)
    total = apply_discount(total, discount_code)

    transaction = process_payment(total, payment_info)
    save_order(order_id, user_id, items, total, transaction)
    send_confirmation_email(user_id, order_id, total)

    return {"status": "success", "total": total, "transaction_id": transaction.id}
```

### 2. God Class

A class that knows too much and does too much. Usually identifiable by its name: `Manager`, `Controller`, `Handler`, `Processor`.

```python
# SMELL: Everything in one class
class AppManager:
    def manage_users(self): ...
    def manage_orders(self): ...
    def send_emails(self): ...
    def generate_reports(self): ...
    def backup_database(self): ...
    def validate_input(self): ...
    # 500 more lines...

# REFACTORED: Split by responsibility
class UserService: ...
class OrderService: ...
class EmailService: ...
class ReportGenerator: ...
class DatabaseBackup: ...
```

### 3. Feature Envy

A method that is more interested in another class's data than its own.

```python
from dataclasses import dataclass
from decimal import Decimal


# SMELL: OrderProcessor reaches into Order's internals
@dataclass
class Order:
    items: list[dict]
    customer_id: str
    discount_rate: float = 0.0

class OrderProcessor:
    def calculate_final_price(self, order: Order) -> Decimal:
        # This method knows too much about Order's internals
        subtotal = sum(
            Decimal(str(item['price'])) * item['quantity']
            for item in order.items  # Reaching into Order
        )
        discount = subtotal * Decimal(str(order.discount_rate))  # Reaching into Order
        return subtotal - discount


# REFACTORED: The logic belongs in Order
@dataclass
class Order:
    items: list[dict]
    customer_id: str
    discount_rate: float = 0.0

    @property
    def subtotal(self) -> Decimal:
        return sum(
            Decimal(str(item['price'])) * item['quantity']
            for item in self.items
        )

    @property
    def discount_amount(self) -> Decimal:
        return self.subtotal * Decimal(str(self.discount_rate))

    @property
    def total(self) -> Decimal:
        return self.subtotal - self.discount_amount


class OrderProcessor:
    def calculate_final_price(self, order: Order) -> Decimal:
        return order.total  # Simple delegation
```

### 4. Data Clumps

Groups of data that always appear together should become a class.

```python
# SMELL: City, street, country, postal_code always travel together
def create_user(name: str, email: str, street: str, city: str,
                country: str, postal_code: str) -> dict:
    ...

def update_shipping(user_id: str, street: str, city: str,
                    country: str, postal_code: str) -> None:
    ...

# REFACTORED: Group them into an Address class
from dataclasses import dataclass

@dataclass
class Address:
    street: str
    city: str
    country: str
    postal_code: str

    def __str__(self) -> str:
        return f"{self.street}, {self.city}, {self.postal_code}, {self.country}"

def create_user(name: str, email: str, address: Address) -> dict:
    ...

def update_shipping(user_id: str, address: Address) -> None:
    ...
```

### 5. Primitive Obsession

Using primitives (str, int, float) when a domain concept deserves its own class.

```python
# SMELL: Email is "just a string" — no validation, no behavior
def send_email(email: str, subject: str, body: str) -> None:
    # What if email is "not-an-email"? We'll only find out at send time.
    ...

def create_user(username: str, email: str) -> dict:
    # Is this email validated? When? Where?
    ...


# REFACTORED: Email as a value object
from dataclasses import dataclass
import re


@dataclass(frozen=True)  # frozen=True makes it immutable (like a proper value object)
class Email:
    value: str

    def __post_init__(self):
        if not re.match(r"^[^@]+@[^@]+\.[^@]+$", self.value):
            raise ValueError(f"'{self.value}' is not a valid email address")

    def __str__(self) -> str:
        return self.value

    @property
    def domain(self) -> str:
        return self.value.split("@")[1]


# Now invalid emails fail at creation time, not at send time
alice_email = Email("alice@example.com")  # OK
bad_email = Email("not-an-email")         # ValueError immediately

def send_email(email: Email, subject: str, body: str) -> None:
    # email is guaranteed to be valid
    ...
```

### 6. Duplicate Code

The same logic appears in multiple places. Any change must be made in multiple places — and you will forget one.

```python
# SMELL: Same validation logic in three places
def create_user(username: str, password: str):
    if len(username) < 3:
        raise ValueError("Username too short")
    if len(password) < 8:
        raise ValueError("Password too short")
    # ...

def update_user(user_id: str, username: str, password: str):
    if len(username) < 3:
        raise ValueError("Username too short")  # Duplicate!
    if len(password) < 8:
        raise ValueError("Password too short")  # Duplicate!
    # ...

def reset_password(user_id: str, new_password: str):
    if len(new_password) < 8:
        raise ValueError("Password too short")  # Duplicate!
    # ...


# REFACTORED: Extract validation into one place
def validate_username(username: str) -> None:
    if len(username) < 3:
        raise ValueError(f"Username must be at least 3 characters, got {len(username)}")

def validate_password(password: str) -> None:
    if len(password) < 8:
        raise ValueError(f"Password must be at least 8 characters, got {len(password)}")

def create_user(username: str, password: str):
    validate_username(username)
    validate_password(password)
    # ...
```

---

## Python-Specific Code Smells

### 7. Mutable Default Arguments

One of Python's most common bugs. Default argument values are evaluated ONCE when the function is defined.

```python
# DANGEROUS BUG
def add_user_to_group(user: str, group_members: list = []) -> list:
    group_members.append(user)
    return group_members

result1 = add_user_to_group("Alice")   # ["Alice"]
result2 = add_user_to_group("Bob")     # ["Alice", "Bob"]  ← BUG! Shared state!
result3 = add_user_to_group("Charlie") # ["Alice", "Bob", "Charlie"]  ← BUG!

# CORRECT: use None as default
def add_user_to_group(user: str, group_members: list | None = None) -> list:
    if group_members is None:
        group_members = []
    group_members.append(user)
    return group_members

# Also applies to dicts:
def create_config(settings: dict | None = None) -> dict:
    if settings is None:
        settings = {}
    settings.setdefault("debug", False)
    return settings
```

### 8. Not Using Context Managers

Manually managing resources is error-prone. If an exception occurs, the resource may never be released.

```python
# SMELL: Manual resource management
f = open("data.json")
data = json.load(f)
f.close()  # Never reached if json.load() raises!

db = get_connection()
cursor = db.cursor()
cursor.execute("SELECT 1")
db.close()  # Never reached if execute() raises!

lock = threading.Lock()
lock.acquire()
process_data()
lock.release()  # Never reached if process_data() raises!


# CORRECT: Context managers guarantee cleanup
with open("data.json") as f:
    data = json.load(f)

with get_connection() as db:
    db.execute("SELECT 1")

with threading.Lock():
    process_data()


# Writing your own context manager:
from contextlib import contextmanager

@contextmanager
def database_transaction(conn):
    """Context manager that commits on success, rolls back on exception."""
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise


with database_transaction(conn) as db:
    db.execute("INSERT INTO orders VALUES (?)", (order_id,))
    db.execute("UPDATE inventory SET count = count - 1 WHERE id = ?", (item_id,))
```

### 9. Catching Too-Broad Exceptions

Broad exception handling hides bugs and makes debugging nearly impossible.

```python
# DANGEROUS: Swallows EVERYTHING including system signals
try:
    result = risky_operation()
except:
    pass  # What failed? We'll never know.

# BAD: Still too broad
try:
    user_data = json.loads(request_body)
except Exception as e:
    return {"error": "Something went wrong"}  # Which exception? Programmer can't debug.

# GOOD: Handle specific, expected exceptions
try:
    user_data = json.loads(request_body)
except json.JSONDecodeError as e:
    return {"error": f"Invalid JSON: {e}", "position": e.pos}
except UnicodeDecodeError as e:
    return {"error": "Request body is not valid UTF-8"}

# If you truly need to catch everything (rare), at least log it
import logging
logger = logging.getLogger(__name__)

try:
    risky_operation()
except Exception as e:
    logger.exception("Unexpected error in risky_operation")  # Logs full traceback
    raise  # Re-raise so it doesn't disappear silently
```

### 10. Using Global State

Global mutable state causes hidden coupling between functions and makes testing extremely difficult.

```python
# SMELL: Global state
_db_connection = None
_current_user = None
_request_count = 0

def get_db():
    global _db_connection  # "global" is almost always a code smell
    if _db_connection is None:
        _db_connection = create_connection()
    return _db_connection

def process_request():
    global _request_count  # Mutation of global state
    _request_count += 1
    # ...


# BETTER: Encapsulate state in a class
class AppContext:
    def __init__(self):
        self._db: DatabaseConnection | None = None
        self._request_count = 0

    def get_db(self) -> DatabaseConnection:
        if self._db is None:
            self._db = create_connection()
        return self._db

    def record_request(self) -> int:
        self._request_count += 1
        return self._request_count


# Or: pass dependencies explicitly (preferred for testability)
def process_request(db: DatabaseConnection, request_counter: Counter) -> None:
    request_counter.increment()
    # use db...
```

### 11. Not Using Built-in Functions

Reimplementing what Python already provides is a smell of not knowing the language.

```python
# SMELL: Reimplementing built-ins
def find_max(items):
    if not items:
        return None
    result = items[0]
    for item in items[1:]:
        if item > result:
            result = item
    return result

def flatten(nested_list):
    result = []
    for sublist in nested_list:
        for item in sublist:
            result.append(item)
    return result

def check_all_positive(numbers):
    for n in numbers:
        if n <= 0:
            return False
    return True


# CORRECT: Use what Python gives you
max_value = max(items, default=None)

import itertools
flat = list(itertools.chain.from_iterable(nested_list))

all_positive = all(n > 0 for n in numbers)

# Python built-ins you should be using:
# max(), min(), sum(), sorted(), reversed()
# any(), all(), filter(), map()
# enumerate(), zip(), zip_longest()
# list/dict/set comprehensions
# itertools.chain, groupby, product, combinations, permutations
```

### 12. Overusing Classes (Non-Pythonic)

Python developers from Java backgrounds often create classes where functions would be simpler.

```python
# SMELL: A class with only __init__ and one method (just use a function!)
class TemperatureConverter:
    def __init__(self):
        pass  # Does nothing

    def celsius_to_fahrenheit(self, celsius: float) -> float:
        return celsius * 9/5 + 32


converter = TemperatureConverter()
result = converter.celsius_to_fahrenheit(100)


# CORRECT: Just a function
def celsius_to_fahrenheit(celsius: float) -> float:
    return celsius * 9/5 + 32

result = celsius_to_fahrenheit(100)


# RULE OF THUMB: Use a class when:
# 1. You need to maintain state across multiple method calls
# 2. You're implementing a known interface (Protocol/ABC)
# 3. The class truly represents a "thing" with behavior and identity
#
# Use a function when:
# 1. You just need to transform input to output
# 2. There is no persistent state
# 3. There is only one "operation" to perform
```

---

## Refactoring Techniques

### Extract Function

Move code into its own well-named function.

```python
# Before
def generate_report(users: list, start_date, end_date):
    # Long block of filtering
    active_users = []
    for user in users:
        if user.is_active and user.joined_at >= start_date:
            active_users.append(user)
    
    # Long block of calculation
    total_revenue = 0
    for user in active_users:
        for order in user.orders:
            if start_date <= order.date <= end_date:
                total_revenue += order.total
    
    # ... more blocks ...

# After
def generate_report(users: list, start_date, end_date):
    active_users = filter_active_users(users, start_date)
    total_revenue = calculate_revenue(active_users, start_date, end_date)
    # ...

def filter_active_users(users: list, since_date) -> list:
    return [u for u in users if u.is_active and u.joined_at >= since_date]

def calculate_revenue(users: list, start_date, end_date) -> float:
    return sum(
        order.total
        for user in users
        for order in user.orders
        if start_date <= order.date <= end_date
    )
```

### Replace Conditional with Polymorphism

```python
# Before: type-checking if/elif chain
def calculate_area(shape):
    if shape["type"] == "circle":
        return 3.14159 * shape["radius"] ** 2
    elif shape["type"] == "rectangle":
        return shape["width"] * shape["height"]
    elif shape["type"] == "triangle":
        return 0.5 * shape["base"] * shape["height"]

# After: polymorphism
import math
from abc import ABC, abstractmethod

class Shape(ABC):
    @abstractmethod
    def area(self) -> float: ...

class Circle(Shape):
    def __init__(self, radius: float):
        self.radius = radius
    def area(self) -> float:
        return math.pi * self.radius ** 2

class Rectangle(Shape):
    def __init__(self, width: float, height: float):
        self.width = width
        self.height = height
    def area(self) -> float:
        return self.width * self.height

# Now: shape.area() — no conditionals needed
```

---

## Smell Checklist

When reviewing your own code, ask:

- Is any function longer than 30 lines? (Long Method)
- Is any class longer than 200 lines or does more than 3 things? (God Class)
- Are there any mutable default arguments? (`def f(items=[]): ...`)
- Are there any `except:` or `except Exception:` blocks without logging? (Silent failures)
- Are there any `global` statements? (Global State)
- Are there any hand-written loops that `max()`, `min()`, `any()`, `all()`, `sum()` could replace?
- Are there any open files, locks, or connections not managed with `with`?
- Are there any classes with only one method? (Should be a function)
- Are there any groups of parameters that always travel together? (Data Clump → Value Object)
- Is the same validation or logic appearing in two places? (Duplicate Code)
