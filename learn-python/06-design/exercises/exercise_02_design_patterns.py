"""
Exercise 02 — Implement Design Patterns

Implement each design pattern from scratch. The interfaces are provided.
Tests are in tests/test_design_patterns.py.
"""

from typing import Protocol, Callable
from abc import ABC, abstractmethod


# ─────────────────────────────────────────────────────────────────────────────
# EXERCISE 2A: Implement Observer Pattern
#
# Build an EventBus that allows publishing and subscribing to named events.
# ─────────────────────────────────────────────────────────────────────────────

class EventBus:
    """
    A simple publish-subscribe event bus.

    Requirements:
    - subscribe(event_name, handler): Register a callable for an event name.
    - unsubscribe(event_name, handler): Remove a registered handler.
    - publish(event_name, data): Call all handlers registered for this event.
    - Handler signature: handler(data: dict) -> None
    - Publishing an event with no subscribers should not raise an error.
    - A handler unsubscribing during publish should not cause errors.

    Usage:
        bus = EventBus()
        bus.subscribe("user.created", send_welcome_email)
        bus.publish("user.created", {"user_id": "123", "email": "a@b.com"})
    """

    def __init__(self):
        # TODO: Initialize internal state
        pass

    def subscribe(self, event_name: str, handler: Callable[[dict], None]) -> None:
        # TODO: Register handler for event_name
        pass

    def unsubscribe(self, event_name: str, handler: Callable[[dict], None]) -> None:
        # TODO: Remove handler from event_name
        pass

    def publish(self, event_name: str, data: dict) -> None:
        # TODO: Call all handlers registered for event_name
        pass


# ─────────────────────────────────────────────────────────────────────────────
# EXERCISE 2B: Implement Builder Pattern
#
# Build a QueryBuilder that constructs SQL SELECT queries.
# ─────────────────────────────────────────────────────────────────────────────

class QueryBuilder:
    """
    Fluent builder for SQL SELECT queries.

    Requirements:
    - from_table(table): Set the table name (required).
    - select(*columns): Set columns to select (default: '*').
    - where(condition): Add a WHERE condition (AND logic for multiple calls).
    - order_by(column, descending=False): Add ORDER BY.
    - limit(n): Set LIMIT.
    - build(): Return the SQL string.

    Example:
        sql = (
            QueryBuilder()
            .from_table("users")
            .select("id", "name", "email")
            .where("is_active = 1")
            .where("age > 18")
            .order_by("name")
            .limit(10)
            .build()
        )
        # "SELECT id, name, email FROM users WHERE is_active = 1 AND age > 18 ORDER BY name ASC LIMIT 10"
    """

    def __init__(self):
        # TODO: Initialize builder state
        self._table: str = ""
        self._columns: list[str] = []
        self._conditions: list[str] = []
        self._order_column: str | None = None
        self._order_desc: bool = False
        self._limit: int | None = None

    def from_table(self, table: str) -> "QueryBuilder":
        # TODO
        return self

    def select(self, *columns: str) -> "QueryBuilder":
        # TODO
        return self

    def where(self, condition: str) -> "QueryBuilder":
        # TODO
        return self

    def order_by(self, column: str, descending: bool = False) -> "QueryBuilder":
        # TODO
        return self

    def limit(self, n: int) -> "QueryBuilder":
        # TODO
        return self

    def build(self) -> str:
        # TODO: Assemble and return the SQL string
        if not self._table:
            raise ValueError("Table name is required. Call from_table() first.")
        return ""


# ─────────────────────────────────────────────────────────────────────────────
# EXERCISE 2C: Implement Strategy Pattern
#
# Build a TextProcessor that applies pluggable text transformations.
# ─────────────────────────────────────────────────────────────────────────────

class TextTransformer(Protocol):
    """Strategy protocol for text transformations."""
    def transform(self, text: str) -> str: ...


class TextProcessor:
    """
    Processes text using a chain of transformers.

    Requirements:
    - __init__(): Start with no transformers.
    - add_transformer(transformer): Add a transformer to the chain.
    - remove_transformer(transformer): Remove a transformer.
    - process(text): Apply all transformers in order and return result.

    Usage:
        processor = TextProcessor()
        processor.add_transformer(UpperCaseTransformer())
        processor.add_transformer(TrimTransformer())
        result = processor.process("  hello world  ")
        # "HELLO WORLD"
    """

    def __init__(self):
        # TODO
        pass

    def add_transformer(self, transformer: TextTransformer) -> "TextProcessor":
        # TODO
        return self

    def remove_transformer(self, transformer: TextTransformer) -> None:
        # TODO
        pass

    def process(self, text: str) -> str:
        # TODO
        return text


# ─── Concrete Transformers ────────────────────────────────────────────────────

class UpperCaseTransformer:
    """Convert text to uppercase."""
    def transform(self, text: str) -> str:
        # TODO
        return text


class TrimTransformer:
    """Remove leading and trailing whitespace."""
    def transform(self, text: str) -> str:
        # TODO
        return text


class ReplaceTransformer:
    """Replace all occurrences of a substring."""
    def __init__(self, old: str, new: str):
        self._old = old
        self._new = new

    def transform(self, text: str) -> str:
        # TODO
        return text


# ─────────────────────────────────────────────────────────────────────────────
# EXERCISE 2D: Implement Decorator Pattern (GoF)
#
# Extend the beverage shop example to add SizeModifier decorators.
# ─────────────────────────────────────────────────────────────────────────────

class Drink(ABC):
    """Abstract base for drinks."""

    @abstractmethod
    def cost(self) -> float:
        ...

    @abstractmethod
    def description(self) -> str:
        ...


class SimpleCoffee(Drink):
    def cost(self) -> float:
        return 1.00

    def description(self) -> str:
        return "Simple Coffee"


class DrinkDecorator(Drink, ABC):
    def __init__(self, drink: Drink):
        self._drink = drink


# TODO: Implement these decorators:

class WithMilk(DrinkDecorator):
    """Adds milk — costs $0.25 more."""
    def cost(self) -> float:
        # TODO
        return self._drink.cost()

    def description(self) -> str:
        # TODO
        return self._drink.description()


class WithSyrup(DrinkDecorator):
    """Adds flavored syrup — costs $0.50 more."""
    def cost(self) -> float:
        # TODO
        return self._drink.cost()

    def description(self) -> str:
        # TODO
        return self._drink.description()


class LargeSize(DrinkDecorator):
    """Upsize to large — multiplies cost by 1.5."""
    def cost(self) -> float:
        # TODO
        return self._drink.cost()

    def description(self) -> str:
        # TODO
        return self._drink.description()


# ─────────────────────────────────────────────────────────────────────────────
# HOW TO CHECK YOUR WORK
# Run: python -m pytest tests/test_design_patterns.py -v
# ─────────────────────────────────────────────────────────────────────────────
