# Architecture Patterns in Python

Architecture is about how the big pieces of your system fit together. The patterns in this module are not about individual classes — they are about how entire layers, components, and services interact.

---

## 1. Layered (N-Tier) Architecture

The most common architecture for web applications. Code is organized into horizontal layers, each with a specific responsibility.

```
┌─────────────────────────────────────────┐
│          Presentation Layer             │  ← HTTP handlers, templates, CLI
├─────────────────────────────────────────┤
│          Business Logic Layer           │  ← Domain logic, rules, workflows  
├─────────────────────────────────────────┤
│          Data Access Layer              │  ← Database queries, repositories
├─────────────────────────────────────────┤
│          Database / External Services   │  ← PostgreSQL, Redis, S3
└─────────────────────────────────────────┘
```

**Rule:** Each layer only talks to the layer directly below it. The presentation layer never queries the database directly.

### Flask Example

```python
# Layered architecture in Flask
# Project structure:
#
# myapp/
# ├── presentation/
# │   └── routes.py        ← Flask route handlers
# ├── service/
# │   └── user_service.py  ← Business logic
# ├── repository/
# │   └── user_repo.py     ← Database access
# └── models/
#     └── user.py          ← Data models

# ─── models/user.py ─────────────────────────────────────────────────────────
from dataclasses import dataclass
from datetime import datetime


@dataclass
class User:
    id: int
    username: str
    email: str
    created_at: datetime
    is_active: bool = True


# ─── repository/user_repo.py ─────────────────────────────────────────────────

class UserRepository:
    """Data Access Layer — knows SQL, knows nothing about business rules."""

    def __init__(self, db):
        self._db = db

    def find_by_id(self, user_id: int) -> User | None:
        row = self._db.execute(
            "SELECT id, username, email, created_at, is_active FROM users WHERE id = ?",
            (user_id,)
        ).fetchone()
        if row:
            return User(*row)
        return None

    def find_by_email(self, email: str) -> User | None:
        row = self._db.execute(
            "SELECT id, username, email, created_at, is_active FROM users WHERE email = ?",
            (email,)
        ).fetchone()
        if row:
            return User(*row)
        return None

    def save(self, user: User) -> None:
        self._db.execute(
            "INSERT OR REPLACE INTO users VALUES (?, ?, ?, ?, ?)",
            (user.id, user.username, user.email, user.created_at, user.is_active)
        )
        self._db.commit()


# ─── service/user_service.py ─────────────────────────────────────────────────

class UserService:
    """Business Logic Layer — knows domain rules, knows nothing about Flask or SQL."""

    def __init__(self, repo: UserRepository):
        self._repo = repo

    def get_user(self, user_id: int) -> User:
        user = self._repo.find_by_id(user_id)
        if not user:
            raise ValueError(f"User {user_id} not found")
        if not user.is_active:
            raise PermissionError(f"User {user_id} is deactivated")
        return user

    def deactivate_user(self, user_id: int, requesting_user_id: int) -> None:
        """Business rule: only admins can deactivate users."""
        requesting_user = self._repo.find_by_id(requesting_user_id)
        if not requesting_user:
            raise ValueError("Requesting user not found")
        # ... check admin role ...
        user = self._repo.find_by_id(user_id)
        if not user:
            raise ValueError(f"User {user_id} not found")
        user.is_active = False
        self._repo.save(user)


# ─── presentation/routes.py ──────────────────────────────────────────────────

# from flask import Flask, jsonify, request
# from myapp.service.user_service import UserService

# app = Flask(__name__)

# @app.route("/users/<int:user_id>", methods=["GET"])
# def get_user(user_id: int):
#     """Presentation Layer — knows HTTP, delegates to service layer."""
#     try:
#         user = user_service.get_user(user_id)
#         return jsonify({"id": user.id, "username": user.username})
#     except ValueError as e:
#         return jsonify({"error": str(e)}), 404
#     except PermissionError as e:
#         return jsonify({"error": str(e)}), 403
```

---

## 2. Repository Pattern

The Repository pattern provides a clean abstraction over data storage. Code that uses data should not know whether it comes from PostgreSQL, MongoDB, an API, or an in-memory dictionary.

```python
# repository_pattern.py

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from datetime import datetime
from typing import Protocol
import uuid


# ─── Domain Model ────────────────────────────────────────────────────────────

@dataclass
class Product:
    name: str
    price: float
    category: str
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    created_at: datetime = field(default_factory=datetime.now)
    in_stock: bool = True


# ─── Repository Interface ─────────────────────────────────────────────────────

class ProductRepository(Protocol):
    """Abstract repository — the interface all implementations must satisfy."""

    def find_by_id(self, product_id: str) -> Product | None:
        ...

    def find_all(self) -> list[Product]:
        ...

    def find_by_category(self, category: str) -> list[Product]:
        ...

    def save(self, product: Product) -> None:
        ...

    def delete(self, product_id: str) -> None:
        ...


# ─── In-Memory Implementation (for tests and development) ────────────────────

class InMemoryProductRepository:
    """Fast, no external dependencies. Perfect for unit tests."""

    def __init__(self):
        self._store: dict[str, Product] = {}

    def find_by_id(self, product_id: str) -> Product | None:
        return self._store.get(product_id)

    def find_all(self) -> list[Product]:
        return list(self._store.values())

    def find_by_category(self, category: str) -> list[Product]:
        return [p for p in self._store.values() if p.category == category]

    def save(self, product: Product) -> None:
        self._store[product.id] = product

    def delete(self, product_id: str) -> None:
        self._store.pop(product_id, None)


# ─── SQLite Implementation (for production) ──────────────────────────────────

class SqliteProductRepository:
    """Persists to SQLite. Satisfies the same interface as InMemoryProductRepository."""

    def __init__(self, db_path: str = ":memory:"):
        import sqlite3
        self._conn = sqlite3.connect(db_path)
        self._conn.execute("""
            CREATE TABLE IF NOT EXISTS products (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                price REAL NOT NULL,
                category TEXT NOT NULL,
                created_at TEXT NOT NULL,
                in_stock INTEGER NOT NULL
            )
        """)
        self._conn.commit()

    def find_by_id(self, product_id: str) -> Product | None:
        row = self._conn.execute(
            "SELECT id, name, price, category, created_at, in_stock FROM products WHERE id = ?",
            (product_id,)
        ).fetchone()
        if row:
            return self._row_to_product(row)
        return None

    def find_all(self) -> list[Product]:
        rows = self._conn.execute(
            "SELECT id, name, price, category, created_at, in_stock FROM products"
        ).fetchall()
        return [self._row_to_product(row) for row in rows]

    def find_by_category(self, category: str) -> list[Product]:
        rows = self._conn.execute(
            "SELECT id, name, price, category, created_at, in_stock FROM products WHERE category = ?",
            (category,)
        ).fetchall()
        return [self._row_to_product(row) for row in rows]

    def save(self, product: Product) -> None:
        self._conn.execute(
            "INSERT OR REPLACE INTO products VALUES (?, ?, ?, ?, ?, ?)",
            (product.id, product.name, product.price, product.category,
             product.created_at.isoformat(), int(product.in_stock))
        )
        self._conn.commit()

    def delete(self, product_id: str) -> None:
        self._conn.execute("DELETE FROM products WHERE id = ?", (product_id,))
        self._conn.commit()

    def _row_to_product(self, row) -> Product:
        return Product(
            id=row[0],
            name=row[1],
            price=row[2],
            category=row[3],
            created_at=datetime.fromisoformat(row[4]),
            in_stock=bool(row[5]),
        )


# ─── Service uses the repository through the interface ────────────────────────

class ProductService:
    """Business logic. Works with ANY repository implementation."""

    def __init__(self, repo: ProductRepository):
        self._repo = repo

    def get_affordable_products(self, max_price: float) -> list[Product]:
        return [p for p in self._repo.find_all() if p.price <= max_price]

    def add_product(self, name: str, price: float, category: str) -> Product:
        product = Product(name=name, price=price, category=category)
        self._repo.save(product)
        return product
```

---

## 3. MVC in Django

Django follows MVC — but calls it MVT (Model-View-Template).

| MVC | Django | Responsibility |
|-----|--------|---------------|
| Model | Model | Data structure and database access |
| View | Template | HTML rendering |
| Controller | View | Request handling and business logic |

```python
# Django MVT example

# ─── models.py (Model) ───────────────────────────────────────────────────────
# from django.db import models

# class Article(models.Model):
#     title = models.CharField(max_length=200)
#     content = models.TextField()
#     author = models.ForeignKey("auth.User", on_delete=models.CASCADE)
#     published_at = models.DateTimeField(auto_now_add=True)
#     is_published = models.BooleanField(default=False)
#
#     class Meta:
#         ordering = ["-published_at"]
#
#     def __str__(self) -> str:
#         return self.title


# ─── views.py (Controller logic) ─────────────────────────────────────────────
# from django.views import View
# from django.http import HttpRequest, HttpResponse
# from django.shortcuts import render, get_object_or_404
#
# class ArticleListView(View):
#     def get(self, request: HttpRequest) -> HttpResponse:
#         articles = Article.objects.filter(is_published=True)
#         return render(request, "articles/list.html", {"articles": articles})
#
# class ArticleDetailView(View):
#     def get(self, request: HttpRequest, article_id: int) -> HttpResponse:
#         article = get_object_or_404(Article, id=article_id, is_published=True)
#         return render(request, "articles/detail.html", {"article": article})


# ─── templates/articles/list.html (View/Template) ────────────────────────────
# {% for article in articles %}
#   <article>
#     <h2>{{ article.title }}</h2>
#     <p>By {{ article.author.username }}</p>
#     <a href="{% url 'article_detail' article.id %}">Read more</a>
#   </article>
# {% endfor %}
```

---

## 4. Hexagonal / Clean Architecture in Python

Also called "Ports and Adapters." The core of the application (domain logic) is completely isolated from external systems (databases, web frameworks, APIs).

```
        ┌─────────────────────────────────────────┐
        │          External World                 │
        │  (HTTP, CLI, Database, Email, Queue)    │
        └────────────┬───────────────┬────────────┘
                     │               │
              ┌──────┴──────┐  ┌─────┴──────┐
              │  Adapters   │  │  Adapters  │
              │  (Driving)  │  │  (Driven)  │
              └──────┬──────┘  └─────┬──────┘
                     │               │
              ┌──────┴─────────────┬─┘
              │      Ports         │
              │  (Interfaces)      │
              └──────┬─────────────┘
                     │
              ┌──────┴──────────────────────────┐
              │         Domain Core              │
              │    (Pure Business Logic)         │
              │  No dependencies on frameworks   │
              └─────────────────────────────────┘
```

```python
# hexagonal_architecture.py
# A simple order processing system

from abc import ABC, abstractmethod
from dataclasses import dataclass
from decimal import Decimal
from typing import Protocol
import uuid


# ─── DOMAIN CORE — No imports from frameworks, databases, or libraries ────────

@dataclass
class OrderItem:
    product_id: str
    quantity: int
    unit_price: Decimal

    @property
    def subtotal(self) -> Decimal:
        return self.unit_price * self.quantity


@dataclass
class Order:
    customer_id: str
    items: list[OrderItem]
    id: str = None

    def __post_init__(self):
        if self.id is None:
            self.id = str(uuid.uuid4())

    @property
    def total(self) -> Decimal:
        return sum(item.subtotal for item in self.items)

    def is_valid(self) -> bool:
        return len(self.items) > 0 and all(item.quantity > 0 for item in self.items)


# ─── PORTS (Interfaces that the domain core depends on) ──────────────────────

class OrderRepository(Protocol):
    """Port: how the domain core stores orders."""
    def save(self, order: Order) -> None: ...
    def find_by_id(self, order_id: str) -> Order | None: ...


class PaymentGateway(Protocol):
    """Port: how the domain core processes payments."""
    def charge(self, order_id: str, amount: Decimal) -> str: ...


class NotificationService(Protocol):
    """Port: how the domain core sends notifications."""
    def send_order_confirmation(self, order: Order) -> None: ...


# ─── DOMAIN SERVICE (uses only ports/interfaces) ─────────────────────────────

class OrderService:
    """Pure business logic. Depends on abstractions, not concrete implementations."""

    def __init__(
        self,
        repository: OrderRepository,
        payment: PaymentGateway,
        notifications: NotificationService,
    ):
        self._repo = repository
        self._payment = payment
        self._notifications = notifications

    def place_order(self, order: Order) -> str:
        """Place an order: validate, pay, save, notify."""
        if not order.is_valid():
            raise ValueError("Order has no valid items")

        transaction_id = self._payment.charge(order.id, order.total)
        self._repo.save(order)
        self._notifications.send_order_confirmation(order)

        return transaction_id


# ─── ADAPTERS (concrete implementations of the ports) ────────────────────────

class InMemoryOrderRepository:
    """Driven adapter: implements OrderRepository using in-memory storage."""

    def __init__(self):
        self._orders: dict[str, Order] = {}

    def save(self, order: Order) -> None:
        self._orders[order.id] = order

    def find_by_id(self, order_id: str) -> Order | None:
        return self._orders.get(order_id)


class StripePaymentGateway:
    """Driven adapter: implements PaymentGateway using Stripe."""

    def __init__(self, api_key: str):
        self._api_key = api_key

    def charge(self, order_id: str, amount: Decimal) -> str:
        print(f"[Stripe] Charging ${amount} for order {order_id}")
        return f"txn_{uuid.uuid4().hex[:8]}"


class EmailNotificationService:
    """Driven adapter: implements NotificationService using email."""

    def send_order_confirmation(self, order: Order) -> None:
        print(f"[Email] Sending order confirmation for order {order.id}")


# ─── Driving Adapter: HTTP API ────────────────────────────────────────────────
# In a real FastAPI app, this would be the route handler
# It receives HTTP requests and calls the OrderService

class OrderController:
    """Driving adapter: connects HTTP requests to the domain service."""

    def __init__(self, order_service: OrderService):
        self._service = order_service

    def handle_place_order(self, request_data: dict) -> dict:
        """Convert HTTP request → domain objects → call service → HTTP response."""
        items = [
            OrderItem(
                product_id=item["product_id"],
                quantity=item["quantity"],
                unit_price=Decimal(str(item["price"])),
            )
            for item in request_data["items"]
        ]
        order = Order(customer_id=request_data["customer_id"], items=items)

        try:
            transaction_id = self._service.place_order(order)
            return {"status": "ok", "order_id": order.id, "transaction_id": transaction_id}
        except ValueError as e:
            return {"status": "error", "message": str(e)}
```

---

## 5. FastAPI as Modern Python Architecture

FastAPI exemplifies modern Python architecture: async, type-safe, self-documenting.

```python
# fastapi_architecture.py
# Conceptual example (requires: pip install fastapi uvicorn)

from dataclasses import dataclass
from decimal import Decimal
from typing import Annotated

# from fastapi import FastAPI, Depends, HTTPException
# from pydantic import BaseModel

# ─── Request/Response Models (Pydantic) ───────────────────────────────────────

# class CreateUserRequest(BaseModel):
#     username: str
#     email: str
#     password: str
#
# class UserResponse(BaseModel):
#     id: str
#     username: str
#     email: str


# ─── Dependency Injection with FastAPI ────────────────────────────────────────
# FastAPI's Depends() implements DIP at the framework level

# def get_db():
#     """Dependency: provides a database session."""
#     db = SessionLocal()
#     try:
#         yield db
#     finally:
#         db.close()
#
# def get_user_service(db = Depends(get_db)) -> UserService:
#     """Dependency: provides a configured UserService."""
#     repo = SqlAlchemyUserRepository(db)
#     notifications = EmailNotificationService()
#     return UserService(repo, notifications)


# ─── Route Handlers ───────────────────────────────────────────────────────────

# app = FastAPI(title="My API", version="1.0.0")
#
# @app.post("/users", response_model=UserResponse, status_code=201)
# async def create_user(
#     request: CreateUserRequest,
#     service: UserService = Depends(get_user_service),
# ) -> UserResponse:
#     """
#     Create a new user.
#
#     FastAPI automatically:
#     - Validates the request body
#     - Serializes the response
#     - Generates OpenAPI docs at /docs
#     - Handles dependency injection
#     """
#     user = await service.register(request.username, request.email, request.password)
#     return UserResponse(id=user.id, username=user.username, email=user.email)
#
# @app.get("/users/{user_id}", response_model=UserResponse)
# async def get_user(
#     user_id: str,
#     service: UserService = Depends(get_user_service),
# ) -> UserResponse:
#     user = await service.get_user(user_id)
#     if not user:
#         raise HTTPException(status_code=404, detail=f"User {user_id} not found")
#     return UserResponse(id=user.id, username=user.username, email=user.email)
```

---

## Architecture Comparison

| Pattern | Best For | Python Tools |
|---------|---------|-------------|
| Layered | Traditional web apps | Flask, Django |
| Repository | Clean data access abstraction | Protocol, ABC |
| MVC/MVT | Full-stack web apps | Django |
| Hexagonal | Complex domain logic, high testability | Protocol, dataclass |
| FastAPI style | Modern async APIs | FastAPI, Pydantic |

**The universal principle:** The center of your application should be pure Python with no framework dependencies. Frameworks, databases, and external services are plugins — they should be easy to swap.
