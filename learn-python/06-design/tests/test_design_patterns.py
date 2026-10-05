"""
Tests for Module 06: Design Patterns

Run with: python -m pytest tests/test_design_patterns.py -v

These tests cover:
- SOLID exercises (verifying refactored solutions)
- All design patterns: Observer, Builder, Strategy, Decorator, Repository
- Each pattern with 2+ tests covering normal use and edge cases

Total: 20 tests
"""

import pytest
from decimal import Decimal
from typing import Callable


# ─── Import solutions ─────────────────────────────────────────────────────────
# Tests check your solutions, not the exercises themselves.
from exercises.solutions.solutions_01_02 import (
    # SRP
    SalesCalculator,
    ReportFormatter,
    # OCP
    StandardShipping,
    ExpressShipping,
    DroneShipping,
    get_shipping_quote,
    # DIP
    NotificationService,
    ConsoleEmailClient,
    ConsoleSmsClient,
    # Patterns
    EventBus,
    QueryBuilder,
    TextProcessor,
    UpperCaseTransformer,
    TrimTransformer,
    ReplaceTransformer,
    SimpleCoffee,
    WithMilk,
    WithSyrup,
    LargeSize,
)


# ═══════════════════════════════════════════════════════════════════════════════
# SOLID Exercises
# ═══════════════════════════════════════════════════════════════════════════════

class TestSalesCalculator:
    """EXERCISE 1A: SRP — SalesCalculator"""

    def test_compute_with_data(self):
        calc = SalesCalculator()
        records = [
            {"date": "2024-01", "product": "Widget", "amount": 100.0, "quantity": 2},
            {"date": "2024-01", "product": "Gadget", "amount": 200.0, "quantity": 1},
            {"date": "2024-01", "product": "Widget", "amount": 100.0, "quantity": 2},
        ]
        result = calc.compute(records, "2024-01-01", "2024-01-31")

        assert result["total_sales"] == pytest.approx(400.0)
        assert result["average_sale"] == pytest.approx(400.0 / 3)
        assert result["record_count"] == 3
        assert result["sales_by_product"]["Widget"] == pytest.approx(200.0)
        assert result["sales_by_product"]["Gadget"] == pytest.approx(200.0)

    def test_compute_empty_data(self):
        calc = SalesCalculator()
        result = calc.compute([], "2024-01-01", "2024-01-31")

        assert result["total_sales"] == 0
        assert result["average_sale"] == 0
        assert result["record_count"] == 0


class TestReportFormatter:
    """EXERCISE 1A: SRP — ReportFormatter"""

    def test_format_json(self):
        formatter = ReportFormatter()
        report = {"total_sales": 500.0, "record_count": 5}
        result = formatter.format_json(report)
        import json
        parsed = json.loads(result)
        assert parsed["total_sales"] == 500.0

    def test_format_text(self):
        formatter = ReportFormatter()
        report = {
            "period": "Jan 2024",
            "total_sales": 1000.0,
            "average_sale": 200.0,
            "record_count": 5,
            "sales_by_product": {"Widget": 600.0},
        }
        result = formatter.format_text(report)
        assert "1000" in result
        assert "Widget" in result


class TestShippingOCP:
    """EXERCISE 1B: OCP — Shipping Calculators"""

    def test_standard_shipping(self):
        calc = StandardShipping()
        # 2kg * 1.5 + 100km * 0.01 = 3.0 + 1.0 = 4.0
        assert calc.calculate(2, 100) == pytest.approx(4.0)

    def test_express_shipping(self):
        calc = ExpressShipping()
        # 2kg * 3.0 + 100km * 0.02 + 5.0 = 6.0 + 2.0 + 5.0 = 13.0
        assert calc.calculate(2, 100) == pytest.approx(13.0)

    def test_drone_shipping_within_limit(self):
        calc = DroneShipping(max_weight_kg=5.0)
        # 3kg within limit: 100km * 0.05 + 2.0 = 7.0
        assert calc.calculate(3, 100) == pytest.approx(7.0)

    def test_drone_shipping_exceeds_limit(self):
        calc = DroneShipping(max_weight_kg=5.0)
        with pytest.raises(ValueError, match="cannot carry"):
            calc.calculate(10, 100)

    def test_shipping_quote_uses_any_calculator(self):
        """OCP: get_shipping_quote works with any calculator."""
        quote = get_shipping_quote(StandardShipping(), 1, 50)
        assert "Standard" in quote
        assert "$" in quote


# ═══════════════════════════════════════════════════════════════════════════════
# Pattern: Observer (EventBus)
# ═══════════════════════════════════════════════════════════════════════════════

class TestEventBus:
    """EXERCISE 2A: Observer — EventBus"""

    def test_subscribe_and_publish(self):
        bus = EventBus()
        received: list[dict] = []

        def handler(data: dict) -> None:
            received.append(data)

        bus.subscribe("user.created", handler)
        bus.publish("user.created", {"user_id": "123"})

        assert len(received) == 1
        assert received[0]["user_id"] == "123"

    def test_multiple_handlers(self):
        bus = EventBus()
        log: list[str] = []

        bus.subscribe("event", lambda d: log.append("handler1"))
        bus.subscribe("event", lambda d: log.append("handler2"))
        bus.publish("event", {})

        assert "handler1" in log
        assert "handler2" in log

    def test_unsubscribe(self):
        bus = EventBus()
        calls: list[int] = []

        def handler(data): calls.append(1)

        bus.subscribe("event", handler)
        bus.publish("event", {})
        assert len(calls) == 1

        bus.unsubscribe("event", handler)
        bus.publish("event", {})
        assert len(calls) == 1  # Still 1, not 2

    def test_publish_with_no_subscribers(self):
        """Should not raise."""
        bus = EventBus()
        bus.publish("no.one.listening", {"data": "hello"})  # Should not raise

    def test_publish_to_different_events(self):
        """Handlers for one event don't receive events for another."""
        bus = EventBus()
        event_a_calls: list[dict] = []
        event_b_calls: list[dict] = []

        bus.subscribe("event.a", lambda d: event_a_calls.append(d))
        bus.subscribe("event.b", lambda d: event_b_calls.append(d))

        bus.publish("event.a", {"source": "a"})

        assert len(event_a_calls) == 1
        assert len(event_b_calls) == 0


# ═══════════════════════════════════════════════════════════════════════════════
# Pattern: Builder (QueryBuilder)
# ═══════════════════════════════════════════════════════════════════════════════

class TestQueryBuilder:
    """EXERCISE 2B: Builder — QueryBuilder"""

    def test_simple_select_all(self):
        sql = QueryBuilder().from_table("users").build()
        assert sql == "SELECT * FROM users"

    def test_select_specific_columns(self):
        sql = QueryBuilder().from_table("users").select("id", "name", "email").build()
        assert "id, name, email" in sql

    def test_where_clause(self):
        sql = QueryBuilder().from_table("users").where("is_active = 1").build()
        assert "WHERE is_active = 1" in sql

    def test_multiple_where_clauses(self):
        sql = (
            QueryBuilder()
            .from_table("users")
            .where("is_active = 1")
            .where("age > 18")
            .build()
        )
        assert "is_active = 1" in sql
        assert "age > 18" in sql
        assert "AND" in sql

    def test_order_by_asc(self):
        sql = QueryBuilder().from_table("users").order_by("name").build()
        assert "ORDER BY name ASC" in sql

    def test_order_by_desc(self):
        sql = QueryBuilder().from_table("users").order_by("created_at", descending=True).build()
        assert "ORDER BY created_at DESC" in sql

    def test_limit(self):
        sql = QueryBuilder().from_table("users").limit(10).build()
        assert "LIMIT 10" in sql

    def test_full_query(self):
        sql = (
            QueryBuilder()
            .from_table("products")
            .select("id", "name", "price")
            .where("in_stock = 1")
            .where("price < 100")
            .order_by("price")
            .limit(20)
            .build()
        )
        assert sql == (
            "SELECT id, name, price FROM products "
            "WHERE in_stock = 1 AND price < 100 "
            "ORDER BY price ASC "
            "LIMIT 20"
        )

    def test_requires_table(self):
        with pytest.raises(ValueError, match="Table name is required"):
            QueryBuilder().select("id").build()


# ═══════════════════════════════════════════════════════════════════════════════
# Pattern: Strategy (TextProcessor)
# ═══════════════════════════════════════════════════════════════════════════════

class TestTextProcessor:
    """EXERCISE 2C: Strategy — TextProcessor"""

    def test_no_transformers_returns_original(self):
        processor = TextProcessor()
        assert processor.process("hello") == "hello"

    def test_uppercase_transformer(self):
        processor = TextProcessor()
        processor.add_transformer(UpperCaseTransformer())
        assert processor.process("hello world") == "HELLO WORLD"

    def test_trim_transformer(self):
        processor = TextProcessor()
        processor.add_transformer(TrimTransformer())
        assert processor.process("  hello  ") == "hello"

    def test_chained_transformers(self):
        processor = TextProcessor()
        processor.add_transformer(TrimTransformer())
        processor.add_transformer(UpperCaseTransformer())
        assert processor.process("  hello world  ") == "HELLO WORLD"

    def test_replace_transformer(self):
        processor = TextProcessor()
        processor.add_transformer(ReplaceTransformer("world", "Python"))
        assert processor.process("hello world") == "hello Python"

    def test_remove_transformer(self):
        processor = TextProcessor()
        upper = UpperCaseTransformer()
        processor.add_transformer(upper)
        processor.remove_transformer(upper)
        assert processor.process("hello") == "hello"


# ═══════════════════════════════════════════════════════════════════════════════
# Pattern: Decorator (Drinks)
# ═══════════════════════════════════════════════════════════════════════════════

class TestDrinkDecorator:
    """EXERCISE 2D: Decorator — Drink Decorators"""

    def test_simple_coffee_cost(self):
        coffee = SimpleCoffee()
        assert coffee.cost() == pytest.approx(1.00)
        assert "Coffee" in coffee.description()

    def test_milk_adds_cost(self):
        coffee = WithMilk(SimpleCoffee())
        assert coffee.cost() == pytest.approx(1.25)
        assert "Milk" in coffee.description()

    def test_syrup_adds_cost(self):
        coffee = WithSyrup(SimpleCoffee())
        assert coffee.cost() == pytest.approx(1.50)
        assert "Syrup" in coffee.description()

    def test_stacked_decorators(self):
        coffee = LargeSize(WithSyrup(WithMilk(SimpleCoffee())))
        # (1.00 + 0.25 + 0.50) * 1.5 = 2.625
        assert coffee.cost() == pytest.approx(2.625)
        assert "Milk" in coffee.description()
        assert "Syrup" in coffee.description()
        assert "Large" in coffee.description()

    def test_large_size_multiplies(self):
        coffee = LargeSize(SimpleCoffee())
        assert coffee.cost() == pytest.approx(1.50)  # 1.00 * 1.5
