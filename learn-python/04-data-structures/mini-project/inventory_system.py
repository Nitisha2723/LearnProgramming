"""
inventory_system.py — Inventory Management System

A complete inventory system demonstrating:
  - dict for product catalog (O(1) lookups by SKU)
  - defaultdict(set) for category management
  - Counter for sales analytics
  - namedtuple for product records
  - list for ordered transaction history
  - set for low-stock tracking

Run this file to see a full demonstration.
"""

from collections import Counter, defaultdict, namedtuple
from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional
import json


# =============================================================================
# Data Types
# =============================================================================

Product = namedtuple("Product", ["sku", "name", "price", "unit"])

@dataclass
class InventoryItem:
    """Tracks a product and its current stock level."""
    product: Product
    quantity: int
    reorder_level: int = 10

    @property
    def is_low_stock(self) -> bool:
        return self.quantity <= self.reorder_level

    @property
    def is_out_of_stock(self) -> bool:
        return self.quantity == 0


@dataclass
class SaleRecord:
    """Records a single sale transaction."""
    timestamp: str
    sku: str
    product_name: str
    quantity: int
    unit_price: float

    @property
    def total(self) -> float:
        return self.quantity * self.unit_price


# =============================================================================
# Inventory System
# =============================================================================

class InventorySystem:
    """
    A complete inventory management system.

    Internal data structures:
    - _catalog: dict[sku -> InventoryItem]  — O(1) lookup by SKU
    - _categories: defaultdict[category -> set of SKUs]
    - _sales_history: list[SaleRecord]  — ordered transaction log
    - _sales_counter: Counter[sku -> total units sold]
    - _low_stock_alerts: set[sku]  — SKUs currently at/below reorder level
    """

    def __init__(self):
        self._catalog: dict[str, InventoryItem] = {}
        self._categories: dict[str, set[str]] = defaultdict(set)
        self._sales_history: list[SaleRecord] = []
        self._sales_counter: Counter = Counter()
        self._low_stock_alerts: set[str] = set()

    # -------------------------------------------------------------------------
    # Product Management
    # -------------------------------------------------------------------------

    def add_product(
        self,
        sku: str,
        name: str,
        price: float,
        unit: str,
        initial_quantity: int,
        category: str,
        reorder_level: int = 10,
    ) -> None:
        """Add a new product to the catalog."""
        if sku in self._catalog:
            raise ValueError(f"SKU '{sku}' already exists")

        product = Product(sku=sku, name=name, price=price, unit=unit)
        item = InventoryItem(
            product=product,
            quantity=initial_quantity,
            reorder_level=reorder_level,
        )
        self._catalog[sku] = item
        self._categories[category].add(sku)

        if item.is_low_stock:
            self._low_stock_alerts.add(sku)

    def get_product(self, sku: str) -> Optional[InventoryItem]:
        """Look up a product by SKU. Returns None if not found."""
        return self._catalog.get(sku)

    def update_price(self, sku: str, new_price: float) -> None:
        """Update the price of a product."""
        if sku not in self._catalog:
            raise ValueError(f"SKU '{sku}' not found")
        item = self._catalog[sku]
        # namedtuple is immutable — create a new Product with updated price
        new_product = item.product._replace(price=new_price)
        item.product = new_product

    def add_category(self, sku: str, category: str) -> None:
        """Add a product to an additional category."""
        if sku not in self._catalog:
            raise ValueError(f"SKU '{sku}' not found")
        self._categories[category].add(sku)

    # -------------------------------------------------------------------------
    # Stock Management
    # -------------------------------------------------------------------------

    def restock(self, sku: str, quantity: int) -> None:
        """Add stock to an item. Clears low-stock alert if applicable."""
        if sku not in self._catalog:
            raise ValueError(f"SKU '{sku}' not found")
        if quantity <= 0:
            raise ValueError("Restock quantity must be positive")

        self._catalog[sku].quantity += quantity

        # Clear low-stock alert if we're above the level now
        if not self._catalog[sku].is_low_stock:
            self._low_stock_alerts.discard(sku)

    def record_sale(self, sku: str, quantity: int) -> SaleRecord:
        """
        Record a sale. Reduces stock, updates counters, checks reorder level.

        Returns the SaleRecord created.
        Raises ValueError if SKU not found or insufficient stock.
        """
        if sku not in self._catalog:
            raise ValueError(f"SKU '{sku}' not found")

        item = self._catalog[sku]
        if item.quantity < quantity:
            raise ValueError(
                f"Insufficient stock: have {item.quantity}, need {quantity}"
            )

        # Reduce stock
        item.quantity -= quantity

        # Record sale
        record = SaleRecord(
            timestamp=datetime.now().isoformat(timespec="seconds"),
            sku=sku,
            product_name=item.product.name,
            quantity=quantity,
            unit_price=item.product.price,
        )
        self._sales_history.append(record)
        self._sales_counter[sku] += quantity

        # Check for low stock
        if item.is_low_stock:
            self._low_stock_alerts.add(sku)

        return record

    # -------------------------------------------------------------------------
    # Queries
    # -------------------------------------------------------------------------

    def get_low_stock(self) -> list[InventoryItem]:
        """Return all items at or below their reorder level."""
        return [
            self._catalog[sku]
            for sku in sorted(self._low_stock_alerts)
            if sku in self._catalog
        ]

    def get_out_of_stock(self) -> list[InventoryItem]:
        """Return all items with zero quantity."""
        return [
            item for item in self._catalog.values()
            if item.is_out_of_stock
        ]

    def get_by_category(self, category: str) -> list[InventoryItem]:
        """Return all items in a category, sorted by name."""
        skus = self._categories.get(category, set())
        items = [self._catalog[sku] for sku in skus if sku in self._catalog]
        return sorted(items, key=lambda i: i.product.name)

    def get_all_categories(self) -> list[str]:
        """Return all category names, sorted."""
        return sorted(self._categories.keys())

    def get_categories_for_sku(self, sku: str) -> set[str]:
        """Return all categories a product belongs to."""
        return {
            category
            for category, skus in self._categories.items()
            if sku in skus
        }

    # -------------------------------------------------------------------------
    # Analytics
    # -------------------------------------------------------------------------

    def best_sellers(self, n: int = 5) -> list[tuple[str, int]]:
        """Return top N SKUs by units sold, as (sku, units) tuples."""
        return self._sales_counter.most_common(n)

    def best_sellers_by_revenue(self, n: int = 5) -> list[tuple[str, float]]:
        """Return top N products by total revenue."""
        revenue: Counter = Counter()
        for record in self._sales_history:
            revenue[record.sku] += record.total
        return revenue.most_common(n)

    def category_summary(self) -> dict[str, dict]:
        """
        Return a summary of each category:
        - product count
        - total inventory value
        - average price
        """
        summary = {}
        for category, skus in self._categories.items():
            items = [self._catalog[sku] for sku in skus if sku in self._catalog]
            if not items:
                continue
            total_value = sum(i.product.price * i.quantity for i in items)
            avg_price = sum(i.product.price for i in items) / len(items)
            summary[category] = {
                "product_count": len(items),
                "total_inventory_value": round(total_value, 2),
                "average_price": round(avg_price, 2),
            }
        return summary

    def sales_by_day(self) -> dict[str, int]:
        """Return total units sold per day (date string → total units)."""
        daily: Counter = Counter()
        for record in self._sales_history:
            date = record.timestamp[:10]
            daily[date] += record.quantity
        return dict(sorted(daily.items()))

    def full_report(self) -> str:
        """Generate a complete inventory report."""
        lines = []
        lines.append("=" * 60)
        lines.append("INVENTORY MANAGEMENT SYSTEM — FULL REPORT")
        lines.append("=" * 60)

        # Stock overview
        total_products = len(self._catalog)
        total_value = sum(
            i.product.price * i.quantity for i in self._catalog.values()
        )
        lines.append(f"\nTotal products:    {total_products}")
        lines.append(f"Total inv. value:  ${total_value:,.2f}")
        lines.append(f"Low stock alerts:  {len(self._low_stock_alerts)}")
        lines.append(f"Out of stock:      {len(self.get_out_of_stock())}")

        # Category breakdown
        lines.append(f"\n{'CATEGORIES':}")
        lines.append("-" * 40)
        for category, stats in sorted(self.category_summary().items()):
            lines.append(
                f"  {category:<20} {stats['product_count']:2d} products  "
                f"${stats['total_inventory_value']:>8,.2f} total"
            )

        # Best sellers
        lines.append(f"\nTOP SELLERS (by units)")
        lines.append("-" * 40)
        for sku, units in self.best_sellers(5):
            item = self._catalog.get(sku)
            if item:
                lines.append(f"  {item.product.name:<25} {units:4d} units sold")

        # Low stock alerts
        low_stock = self.get_low_stock()
        if low_stock:
            lines.append(f"\nLOW STOCK ALERTS")
            lines.append("-" * 40)
            for item in low_stock:
                lines.append(
                    f"  [{item.product.sku}] {item.product.name:<22} "
                    f"qty: {item.quantity:3d}  reorder: {item.reorder_level}"
                )

        lines.append("\n" + "=" * 60)
        return "\n".join(lines)

    def to_dict(self) -> dict:
        """Serialize inventory to a dict (for JSON export)."""
        catalog_data = {}
        for sku, item in self._catalog.items():
            catalog_data[sku] = {
                "name": item.product.name,
                "price": item.product.price,
                "unit": item.product.unit,
                "quantity": item.quantity,
                "reorder_level": item.reorder_level,
                "categories": list(self.get_categories_for_sku(sku)),
            }
        return catalog_data

    def export_json(self, filepath: str) -> None:
        """Export inventory to a JSON file."""
        with open(filepath, "w") as f:
            json.dump(self.to_dict(), f, indent=2)
        print(f"Inventory exported to {filepath}")


# =============================================================================
# Demo
# =============================================================================

def build_demo_inventory() -> InventorySystem:
    """Build a sample inventory for demonstration."""
    inv = InventorySystem()

    # Produce
    inv.add_product("APPL-001", "Gala Apples",     1.29, "per lb",  150, "produce", reorder_level=20)
    inv.add_product("BNAN-001", "Bananas",          0.59, "per lb",  200, "produce", reorder_level=30)
    inv.add_product("ORAN-001", "Navel Oranges",    0.99, "per lb",   80, "produce", reorder_level=20)
    inv.add_product("SPIN-001", "Baby Spinach",     3.49, "5oz bag",  45, "produce", reorder_level=15)
    inv.add_product("TOMA-001", "Roma Tomatoes",    1.99, "per lb",    8, "produce", reorder_level=10)  # low!

    # Dairy
    inv.add_product("MILK-001", "Whole Milk",       3.99, "gallon",   60, "dairy", reorder_level=20)
    inv.add_product("MILK-002", "2% Milk",          3.79, "gallon",   45, "dairy", reorder_level=20)
    inv.add_product("CHEE-001", "Sharp Cheddar",    5.99, "8oz",      30, "dairy", reorder_level=10)
    inv.add_product("YOGU-001", "Greek Yogurt",     1.49, "each",      7, "dairy", reorder_level=10)  # low!
    inv.add_product("BUTR-001", "Salted Butter",    4.29, "1lb",      25, "dairy", reorder_level=10)

    # Bakery
    inv.add_product("BRED-001", "Sourdough Bread",  4.99, "loaf",    20, "bakery", reorder_level=10)
    inv.add_product("BRED-002", "Whole Wheat Bread",3.99, "loaf",    18, "bakery", reorder_level=10)
    inv.add_product("MUFF-001", "Blueberry Muffins",5.49, "6-pack",  12, "bakery", reorder_level=8)
    inv.add_product("CROK-001", "Croissants",       3.29, "4-pack",   0, "bakery", reorder_level=5)  # out!

    # Beverages
    inv.add_product("COFF-001", "Dark Roast Coffee",9.99, "12oz bag", 40, "beverages", reorder_level=10)
    inv.add_product("COFF-002", "Medium Roast",    8.99, "12oz bag",  35, "beverages", reorder_level=10)
    inv.add_product("JUCE-001", "Orange Juice",    4.49, "64oz",      50, "beverages", reorder_level=15)
    inv.add_product("WATE-001", "Sparkling Water", 1.29, "1L bottle", 100,"beverages", reorder_level=20)

    # Cross-category tags
    inv.add_category("COFF-001", "organic")
    inv.add_category("SPIN-001", "organic")
    inv.add_category("APPL-001", "organic")

    return inv


def simulate_sales(inv: InventorySystem) -> None:
    """Simulate a day of sales."""
    sales = [
        ("MILK-001", 8),  ("MILK-002", 6),  ("BRED-001", 5),
        ("APPL-001", 25), ("BNAN-001", 18), ("COFF-001", 4),
        ("YOGU-001", 3),  ("JUCE-001", 12), ("CHEE-001", 7),
        ("BRED-002", 4),  ("COFF-002", 3),  ("WATE-001", 15),
        ("MILK-001", 5),  ("BRED-001", 3),  ("APPL-001", 10),
        ("MUFF-001", 6),  ("SPIN-001", 8),  ("TOMA-001", 4),
        ("BUTR-001", 4),  ("ORAN-001", 12),
    ]

    print("\nSimulating daily sales...")
    failed = []
    for sku, qty in sales:
        try:
            record = inv.record_sale(sku, qty)
        except ValueError as e:
            failed.append(f"  FAILED: {sku} x{qty} — {e}")

    if failed:
        print("Sales that could not be processed:")
        for msg in failed:
            print(msg)


def main():
    print("Building inventory...")
    inv = build_demo_inventory()

    simulate_sales(inv)

    print(inv.full_report())

    print("\nBest sellers by revenue:")
    for sku, revenue in inv.best_sellers_by_revenue(5):
        item = inv.get_product(sku)
        if item:
            print(f"  {item.product.name:<25} ${revenue:,.2f}")

    print("\nOrganic products:")
    for item in inv.get_by_category("organic"):
        print(f"  [{item.product.sku}] {item.product.name}  qty: {item.quantity}")

    print("\nCategory summary:")
    for category, stats in inv.category_summary().items():
        print(f"  {category:<15} {stats['product_count']} products, "
              f"avg price ${stats['average_price']:.2f}")

    # JSON export demo
    import tempfile, os
    with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False) as f:
        tmppath = f.name
    inv.export_json(tmppath)
    with open(tmppath) as f:
        data = json.load(f)
    print(f"\nExported {len(data)} products to JSON")
    print(f"Sample SKU data for MILK-001: {data.get('MILK-001', {})}")
    os.unlink(tmppath)


if __name__ == "__main__":
    main()
