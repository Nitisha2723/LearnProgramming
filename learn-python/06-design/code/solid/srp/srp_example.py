"""
Single Responsibility Principle — Python Examples

Demonstrates SRP violation and correct application.
"""


# ─── VIOLATION ────────────────────────────────────────────────────────────────

class OrderManagerViolation:
    """VIOLATION: This class has too many responsibilities.

    It validates, prices, persists, notifies, and logs — all in one class.
    Any change to email templates, database schema, or validation rules
    requires editing this class.
    """

    def __init__(self, db_connection, email_client):
        self.db = db_connection
        self.email = email_client

    def place_order(self, user_id: str, items: list[dict]) -> dict:
        # Validation
        if not user_id:
            raise ValueError("User ID required")
        if not items:
            raise ValueError("Order must have items")
        for item in items:
            if item.get("quantity", 0) <= 0:
                raise ValueError(f"Invalid quantity for item {item.get('id')}")

        # Pricing
        subtotal = sum(item["price"] * item["quantity"] for item in items)
        tax = subtotal * 0.1
        total = subtotal + tax

        # Persistence
        import uuid
        order_id = str(uuid.uuid4())
        self.db.execute(
            "INSERT INTO orders VALUES (?, ?, ?, ?)",
            (order_id, user_id, total, "pending"),
        )

        # Email notification
        self.email.send(
            to=f"user_{user_id}@example.com",
            subject="Order Confirmed",
            body=f"Your order {order_id} total: ${total:.2f}",
        )

        # Logging
        import logging
        logging.info(f"Order placed: {order_id} by user {user_id}")

        return {"order_id": order_id, "total": total}


# ─── CORRECT ──────────────────────────────────────────────────────────────────

from dataclasses import dataclass, field
from decimal import Decimal
import uuid


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
    user_id: str
    items: list[OrderItem]
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    status: str = "pending"

    @property
    def subtotal(self) -> Decimal:
        return sum(item.subtotal for item in self.items)

    @property
    def tax(self) -> Decimal:
        return self.subtotal * Decimal("0.10")

    @property
    def total(self) -> Decimal:
        return self.subtotal + self.tax


class OrderValidator:
    """SRP: validates order input only."""

    def validate(self, user_id: str, items: list[dict]) -> None:
        if not user_id:
            raise ValueError("User ID is required")
        if not items:
            raise ValueError("Order must contain at least one item")
        for item in items:
            if item.get("quantity", 0) <= 0:
                raise ValueError(f"Invalid quantity for item {item.get('product_id', '?')}")
            if Decimal(str(item.get("price", 0))) <= 0:
                raise ValueError(f"Invalid price for item {item.get('product_id', '?')}")


class OrderRepository:
    """SRP: persists and retrieves orders only."""

    def __init__(self, db):
        self._db = db

    def save(self, order: Order) -> None:
        self._db.execute(
            "INSERT INTO orders (id, user_id, total, status) VALUES (?, ?, ?, ?)",
            (order.id, order.user_id, str(order.total), order.status),
        )

    def find_by_id(self, order_id: str) -> Order | None:
        # Would query and reconstruct Order object
        pass


class OrderNotificationService:
    """SRP: sends order-related notifications only."""

    def __init__(self, email_client):
        self._email = email_client

    def send_confirmation(self, order: Order) -> None:
        self._email.send(
            to=f"user_{order.user_id}@example.com",
            subject=f"Order Confirmed — {order.id}",
            body=f"Your order total: ${order.total:.2f}",
        )


class OrderService:
    """SRP: orchestrates the order placement workflow only."""

    def __init__(
        self,
        validator: OrderValidator,
        repository: OrderRepository,
        notifications: OrderNotificationService,
    ):
        self._validator = validator
        self._repository = repository
        self._notifications = notifications

    def place_order(self, user_id: str, items: list[dict]) -> Order:
        self._validator.validate(user_id, items)

        order_items = [
            OrderItem(
                product_id=item["product_id"],
                quantity=item["quantity"],
                unit_price=Decimal(str(item["price"])),
            )
            for item in items
        ]
        order = Order(user_id=user_id, items=order_items)

        self._repository.save(order)
        self._notifications.send_confirmation(order)

        return order


if __name__ == "__main__":
    # Demo with mock objects
    class MockDB:
        def execute(self, sql, params): pass

    class MockEmail:
        def send(self, to, subject, body):
            print(f"[EMAIL] To: {to}\nSubject: {subject}\nBody: {body}")

    validator = OrderValidator()
    repo = OrderRepository(MockDB())
    notifier = OrderNotificationService(MockEmail())
    service = OrderService(validator, repo, notifier)

    order = service.place_order(
        user_id="user-123",
        items=[
            {"product_id": "prod-1", "quantity": 2, "price": "19.99"},
            {"product_id": "prod-2", "quantity": 1, "price": "49.99"},
        ],
    )
    print(f"Order placed: {order.id}, Total: ${order.total}")
