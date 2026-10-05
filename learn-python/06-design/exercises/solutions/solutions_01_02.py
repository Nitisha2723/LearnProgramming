"""
Exercise Solutions — Module 06: Design Patterns

Full implementations for all exercises.
"""

from typing import Protocol, Callable
from abc import ABC, abstractmethod
from decimal import Decimal
import json
import csv
from io import StringIO
from datetime import datetime


# ═══════════════════════════════════════════════════════════════════════════════
# EXERCISE 1A SOLUTION: Report Generator — SRP Refactoring
# ═══════════════════════════════════════════════════════════════════════════════

class SalesDataFetcher:
    """SRP: Only responsible for fetching data."""

    def __init__(self, db):
        self._db = db

    def fetch(self, start_date: str, end_date: str) -> list[dict]:
        rows = self._db.execute(
            "SELECT date, product, amount, quantity FROM sales WHERE date BETWEEN ? AND ?",
            (start_date, end_date),
        )
        return [
            {"date": r[0], "product": r[1], "amount": r[2], "quantity": r[3]}
            for r in rows
        ]


class SalesCalculator:
    """SRP: Only responsible for calculations."""

    def compute(self, records: list[dict], start_date: str, end_date: str) -> dict:
        if not records:
            return {
                "period": f"{start_date} to {end_date}",
                "total_sales": 0,
                "average_sale": 0,
                "sales_by_product": {},
                "record_count": 0,
                "generated_at": datetime.now().isoformat(),
            }

        total = sum(r["amount"] for r in records)
        avg = total / len(records)

        by_product: dict[str, float] = {}
        for record in records:
            by_product[record["product"]] = (
                by_product.get(record["product"], 0) + record["amount"]
            )

        return {
            "period": f"{start_date} to {end_date}",
            "total_sales": total,
            "average_sale": avg,
            "sales_by_product": by_product,
            "record_count": len(records),
            "generated_at": datetime.now().isoformat(),
        }


class ReportFormatter:
    """SRP: Only responsible for formatting."""

    def format_json(self, report: dict) -> str:
        return json.dumps(report, indent=2)

    def format_csv(self, report: dict) -> str:
        buffer = StringIO()
        writer = csv.writer(buffer)
        writer.writerow(["metric", "value"])
        for key, value in report.items():
            if key == "sales_by_product":
                for product, amount in value.items():
                    writer.writerow([f"product:{product}", amount])
            else:
                writer.writerow([key, value])
        return buffer.getvalue()

    def format_text(self, report: dict) -> str:
        lines = [f"=== Sales Report: {report['period']} ==="]
        lines.append(f"Total Sales: ${report['total_sales']:.2f}")
        lines.append(f"Average Sale: ${report['average_sale']:.2f}")
        lines.append(f"Records: {report['record_count']}")
        lines.append("By Product:")
        for product, amount in report.get("sales_by_product", {}).items():
            lines.append(f"  {product}: ${amount:.2f}")
        return "\n".join(lines)


class ReportWriter:
    """SRP: Only responsible for output."""

    def write_to_file(self, content: str, path: str) -> None:
        with open(path, "w") as f:
            f.write(content)

    def write_to_stdout(self, content: str) -> None:
        print(content)


# ═══════════════════════════════════════════════════════════════════════════════
# EXERCISE 1B SOLUTION: Shipping Calculator — OCP
# ═══════════════════════════════════════════════════════════════════════════════

class ShippingCalculator(Protocol):
    def calculate(self, weight_kg: float, distance_km: float) -> float: ...
    def name(self) -> str: ...


class StandardShipping:
    def calculate(self, weight_kg: float, distance_km: float) -> float:
        return weight_kg * 1.5 + distance_km * 0.01

    def name(self) -> str:
        return "Standard"


class ExpressShipping:
    def calculate(self, weight_kg: float, distance_km: float) -> float:
        return weight_kg * 3.0 + distance_km * 0.02 + 5.0

    def name(self) -> str:
        return "Express"


class OvernightShipping:
    def calculate(self, weight_kg: float, distance_km: float) -> float:
        return weight_kg * 5.0 + distance_km * 0.05 + 10.0

    def name(self) -> str:
        return "Overnight"


class DroneShipping:
    """Added LATER — no existing code was modified."""

    def __init__(self, max_weight_kg: float = 5.0):
        self._max_weight = max_weight_kg

    def calculate(self, weight_kg: float, distance_km: float) -> float:
        if weight_kg > self._max_weight:
            raise ValueError(f"Drone cannot carry {weight_kg}kg (max {self._max_weight}kg)")
        return distance_km * 0.05 + 2.0  # Flat + distance

    def name(self) -> str:
        return "Drone"


def get_shipping_quote(
    calculator: ShippingCalculator, weight_kg: float, distance_km: float
) -> str:
    cost = calculator.calculate(weight_kg, distance_km)
    return f"{calculator.name()}: ${cost:.2f}"


# ═══════════════════════════════════════════════════════════════════════════════
# EXERCISE 1C SOLUTION: Notification Service — DIP
# ═══════════════════════════════════════════════════════════════════════════════

class EmailClient(Protocol):
    def send(self, to: str, subject: str, body: str) -> None: ...


class SmsClient(Protocol):
    def send_sms(self, to: str, message: str) -> None: ...


class ConsoleEmailClient:
    def send(self, to: str, subject: str, body: str) -> None:
        print(f"[EMAIL] To: {to} | Subject: {subject}")


class ConsoleSmsClient:
    def send_sms(self, to: str, message: str) -> None:
        print(f"[SMS] To: {to} | Message: {message}")


class NotificationService:
    """DIP-compliant: depends on abstractions, not concrete implementations."""

    def __init__(self, email: EmailClient, sms: SmsClient):
        self._email = email
        self._sms = sms

    def notify_email(self, address: str, subject: str, message: str) -> None:
        self._email.send(address, subject, message)

    def notify_sms(self, phone: str, message: str) -> None:
        self._sms.send_sms(phone, message)


# ═══════════════════════════════════════════════════════════════════════════════
# EXERCISE 2A SOLUTION: EventBus
# ═══════════════════════════════════════════════════════════════════════════════

class EventBus:
    def __init__(self):
        self._handlers: dict[str, list[Callable[[dict], None]]] = {}

    def subscribe(self, event_name: str, handler: Callable[[dict], None]) -> None:
        if event_name not in self._handlers:
            self._handlers[event_name] = []
        if handler not in self._handlers[event_name]:
            self._handlers[event_name].append(handler)

    def unsubscribe(self, event_name: str, handler: Callable[[dict], None]) -> None:
        if event_name in self._handlers:
            try:
                self._handlers[event_name].remove(handler)
            except ValueError:
                pass

    def publish(self, event_name: str, data: dict) -> None:
        handlers = list(self._handlers.get(event_name, []))
        for handler in handlers:
            handler(data)


# ═══════════════════════════════════════════════════════════════════════════════
# EXERCISE 2B SOLUTION: QueryBuilder
# ═══════════════════════════════════════════════════════════════════════════════

class QueryBuilder:
    def __init__(self):
        self._table = ""
        self._columns: list[str] = []
        self._conditions: list[str] = []
        self._order_column: str | None = None
        self._order_desc = False
        self._limit_value: int | None = None

    def from_table(self, table: str) -> "QueryBuilder":
        self._table = table
        return self

    def select(self, *columns: str) -> "QueryBuilder":
        self._columns = list(columns)
        return self

    def where(self, condition: str) -> "QueryBuilder":
        self._conditions.append(condition)
        return self

    def order_by(self, column: str, descending: bool = False) -> "QueryBuilder":
        self._order_column = column
        self._order_desc = descending
        return self

    def limit(self, n: int) -> "QueryBuilder":
        self._limit_value = n
        return self

    def build(self) -> str:
        if not self._table:
            raise ValueError("Table name is required. Call from_table() first.")

        cols = ", ".join(self._columns) if self._columns else "*"
        sql = f"SELECT {cols} FROM {self._table}"

        if self._conditions:
            sql += " WHERE " + " AND ".join(self._conditions)

        if self._order_column:
            direction = "DESC" if self._order_desc else "ASC"
            sql += f" ORDER BY {self._order_column} {direction}"

        if self._limit_value is not None:
            sql += f" LIMIT {self._limit_value}"

        return sql


# ═══════════════════════════════════════════════════════════════════════════════
# EXERCISE 2C SOLUTION: TextProcessor
# ═══════════════════════════════════════════════════════════════════════════════

class TextProcessor:
    def __init__(self):
        self._transformers: list = []

    def add_transformer(self, transformer) -> "TextProcessor":
        self._transformers.append(transformer)
        return self

    def remove_transformer(self, transformer) -> None:
        try:
            self._transformers.remove(transformer)
        except ValueError:
            pass

    def process(self, text: str) -> str:
        for transformer in self._transformers:
            text = transformer.transform(text)
        return text


class UpperCaseTransformer:
    def transform(self, text: str) -> str:
        return text.upper()


class TrimTransformer:
    def transform(self, text: str) -> str:
        return text.strip()


class ReplaceTransformer:
    def __init__(self, old: str, new: str):
        self._old = old
        self._new = new

    def transform(self, text: str) -> str:
        return text.replace(self._old, self._new)


# ═══════════════════════════════════════════════════════════════════════════════
# EXERCISE 2D SOLUTION: Drink Decorators
# ═══════════════════════════════════════════════════════════════════════════════

class Drink(ABC):
    @abstractmethod
    def cost(self) -> float: ...

    @abstractmethod
    def description(self) -> str: ...


class SimpleCoffee(Drink):
    def cost(self) -> float:
        return 1.00

    def description(self) -> str:
        return "Simple Coffee"


class DrinkDecorator(Drink, ABC):
    def __init__(self, drink: Drink):
        self._drink = drink


class WithMilk(DrinkDecorator):
    def cost(self) -> float:
        return self._drink.cost() + 0.25

    def description(self) -> str:
        return f"{self._drink.description()}, Milk"


class WithSyrup(DrinkDecorator):
    def cost(self) -> float:
        return self._drink.cost() + 0.50

    def description(self) -> str:
        return f"{self._drink.description()}, Syrup"


class LargeSize(DrinkDecorator):
    def cost(self) -> float:
        return self._drink.cost() * 1.5

    def description(self) -> str:
        return f"Large {self._drink.description()}"
