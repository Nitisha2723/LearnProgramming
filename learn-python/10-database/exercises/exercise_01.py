"""
exercise_01.py — Implement a ProductRepository using sqlite3.

Task:
    Build a complete ProductRepository class that manages products
    in a SQLite database using the built-in sqlite3 module.

A Product has:
    - id:       auto-generated integer primary key
    - name:     text, not null
    - price:    real (float), not null, must be > 0
    - category: text, not null
    - stock:    integer, default 0

Requirements:
    Implement every method in the ProductRepository class below.
    All methods receive a connection as their first argument — they do
    NOT open or close connections themselves.

    ✓ create_table  — create the products table (safe to call multiple times)
    ✓ add           — insert a new product; return its id
    ✓ find_by_id    — return a single Row or None
    ✓ find_all      — return all products ordered by name
    ✓ find_by_category — return products matching a category
    ✓ update_price  — update a product's price; return True if found
    ✓ update_stock  — set stock quantity; return True if found
    ✓ delete        — remove a product; return True if found
    ✓ total_value   — sum of (price * stock) for all products (float)
    ✓ search        — find products whose name contains a substring (LIKE)

Constraints:
    - Use ? placeholders — never string formatting in SQL
    - Use sqlite3.Row as the row_factory for named column access
    - Raise ValueError if price <= 0 in the add() method

Run this file to test your implementation:
    python exercise_01.py

All assertions must pass for the exercise to be complete.
"""

import sqlite3
from contextlib import contextmanager
from typing import Generator


# --------------------------------------------------------------------------- #
#  Helper — connection context manager (already done for you)                  #
# --------------------------------------------------------------------------- #


@contextmanager
def get_connection(db: str = ":memory:") -> Generator[sqlite3.Connection, None, None]:
    conn = sqlite3.connect(db)
    conn.row_factory = sqlite3.Row
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


# --------------------------------------------------------------------------- #
#  ProductRepository — implement the methods below                             #
# --------------------------------------------------------------------------- #


class ProductRepository:
    """
    All methods take a sqlite3.Connection as the first argument.
    Do not open or close connections inside these methods.
    """

    # ── Schema ─────────────────────────────────────────────────────────── #

    def create_table(self, conn: sqlite3.Connection) -> None:
        """Create the products table if it doesn't exist."""
        # TODO: implement
        raise NotImplementedError

    # ── Create ─────────────────────────────────────────────────────────── #

    def add(
        self,
        conn: sqlite3.Connection,
        name: str,
        price: float,
        category: str,
        stock: int = 0,
    ) -> int:
        """
        Insert a new product and return its auto-generated id.
        Raise ValueError if price <= 0.
        """
        # TODO: implement
        raise NotImplementedError

    # ── Read ───────────────────────────────────────────────────────────── #

    def find_by_id(self, conn: sqlite3.Connection, product_id: int) -> sqlite3.Row | None:
        """Return the product row with the given id, or None."""
        # TODO: implement
        raise NotImplementedError

    def find_all(self, conn: sqlite3.Connection) -> list[sqlite3.Row]:
        """Return all products ordered alphabetically by name."""
        # TODO: implement
        raise NotImplementedError

    def find_by_category(
        self, conn: sqlite3.Connection, category: str
    ) -> list[sqlite3.Row]:
        """Return all products in the given category, ordered by name."""
        # TODO: implement
        raise NotImplementedError

    def search(self, conn: sqlite3.Connection, query: str) -> list[sqlite3.Row]:
        """
        Return products whose name contains 'query' (case-insensitive).
        Use a LIKE query with % wildcards.
        """
        # TODO: implement
        raise NotImplementedError

    # ── Update ─────────────────────────────────────────────────────────── #

    def update_price(
        self, conn: sqlite3.Connection, product_id: int, new_price: float
    ) -> bool:
        """
        Update the price of a product.
        Return True if the product was found, False otherwise.
        Raise ValueError if new_price <= 0.
        """
        # TODO: implement
        raise NotImplementedError

    def update_stock(
        self, conn: sqlite3.Connection, product_id: int, new_stock: int
    ) -> bool:
        """
        Set the stock quantity of a product.
        Return True if the product was found, False otherwise.
        """
        # TODO: implement
        raise NotImplementedError

    # ── Delete ─────────────────────────────────────────────────────────── #

    def delete(self, conn: sqlite3.Connection, product_id: int) -> bool:
        """
        Delete a product by id.
        Return True if found and deleted, False if not found.
        """
        # TODO: implement
        raise NotImplementedError

    # ── Aggregate ──────────────────────────────────────────────────────── #

    def total_value(self, conn: sqlite3.Connection) -> float:
        """Return the sum of (price * stock) for all products."""
        # TODO: implement
        raise NotImplementedError


# --------------------------------------------------------------------------- #
#  Tests — these run when you execute the file                                 #
# --------------------------------------------------------------------------- #


def run_tests() -> None:
    repo = ProductRepository()

    with get_connection() as conn:
        # Setup
        repo.create_table(conn)

        # --- add and find_by_id ---
        laptop_id = repo.add(conn, "Laptop", 999.99, "Electronics", stock=10)
        mouse_id  = repo.add(conn, "Mouse",   25.00, "Electronics", stock=50)
        desk_id   = repo.add(conn, "Desk",   349.00, "Furniture",   stock=5)
        chair_id  = repo.add(conn, "Chair",  199.00, "Furniture",   stock=8)
        _         = repo.add(conn, "USB Hub", 15.00, "Electronics", stock=0)

        assert isinstance(laptop_id, int), "add() must return an int"
        assert laptop_id != mouse_id, "IDs must be unique"
        print("  ✓ add()")

        laptop = repo.find_by_id(conn, laptop_id)
        assert laptop is not None, "find_by_id must find existing product"
        assert laptop["name"] == "Laptop"
        assert laptop["price"] == 999.99
        assert laptop["category"] == "Electronics"
        assert laptop["stock"] == 10
        print("  ✓ find_by_id()")

        missing = repo.find_by_id(conn, 9999)
        assert missing is None, "find_by_id must return None for unknown id"
        print("  ✓ find_by_id() returns None for missing product")

        # --- ValueError for bad price ---
        raised = False
        try:
            repo.add(conn, "Broken", -5.0, "Test")
        except ValueError:
            raised = True
        assert raised, "add() must raise ValueError for price <= 0"
        print("  ✓ add() raises ValueError for negative price")

        # --- find_all ---
        all_products = repo.find_all(conn)
        assert len(all_products) == 5, f"Expected 5, got {len(all_products)}"
        names = [p["name"] for p in all_products]
        assert names == sorted(names), "find_all must be ordered by name"
        print("  ✓ find_all()")

        # --- find_by_category ---
        electronics = repo.find_by_category(conn, "Electronics")
        assert len(electronics) == 3, f"Expected 3 electronics, got {len(electronics)}"
        furniture = repo.find_by_category(conn, "Furniture")
        assert len(furniture) == 2
        empty = repo.find_by_category(conn, "Nonexistent")
        assert empty == [], f"Expected empty list, got {empty}"
        print("  ✓ find_by_category()")

        # --- search ---
        results = repo.search(conn, "a")  # Laptop, Mouse, Desk, Chair, USB Hub → varies
        assert len(results) > 0, "search('a') should find something"
        chair_results = repo.search(conn, "chair")  # case-insensitive
        assert len(chair_results) == 1
        assert chair_results[0]["name"] == "Chair"
        print("  ✓ search()")

        # --- update_price ---
        ok = repo.update_price(conn, laptop_id, 899.99)
        assert ok is True
        updated = repo.find_by_id(conn, laptop_id)
        assert updated["price"] == 899.99
        not_found = repo.update_price(conn, 9999, 100.0)
        assert not_found is False
        raised = False
        try:
            repo.update_price(conn, laptop_id, 0.0)
        except ValueError:
            raised = True
        assert raised, "update_price must raise ValueError for price <= 0"
        print("  ✓ update_price()")

        # --- update_stock ---
        ok = repo.update_stock(conn, mouse_id, 100)
        assert ok is True
        updated = repo.find_by_id(conn, mouse_id)
        assert updated["stock"] == 100
        not_found = repo.update_stock(conn, 9999, 5)
        assert not_found is False
        print("  ✓ update_stock()")

        # --- total_value ---
        # laptop: 899.99 * 10 = 8999.90
        # mouse:   25.00 * 100 = 2500.00
        # desk:   349.00 * 5  = 1745.00
        # chair:  199.00 * 8  = 1592.00
        # usb hub: 15.00 * 0  = 0.00
        expected = 8999.90 + 2500.00 + 1745.00 + 1592.00 + 0.00
        total = repo.total_value(conn)
        assert abs(total - expected) < 0.01, f"Expected {expected:.2f}, got {total:.2f}"
        print("  ✓ total_value()")

        # --- delete ---
        ok = repo.delete(conn, desk_id)
        assert ok is True
        assert repo.find_by_id(conn, desk_id) is None
        all_now = repo.find_all(conn)
        assert len(all_now) == 4
        not_found = repo.delete(conn, 9999)
        assert not_found is False
        print("  ✓ delete()")

    print("\nAll tests passed!")


if __name__ == "__main__":
    print("Running exercise_01 tests ...\n")
    run_tests()
