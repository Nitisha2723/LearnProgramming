"""
solution_01.py — Complete solution for exercise_01.py

ProductRepository using sqlite3.
"""

import sqlite3
from contextlib import contextmanager
from typing import Generator


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


class ProductRepository:
    """Complete sqlite3 repository for products."""

    # ── Schema ─────────────────────────────────────────────────────────── #

    def create_table(self, conn: sqlite3.Connection) -> None:
        conn.executescript(
            """
            CREATE TABLE IF NOT EXISTS products (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                name     TEXT    NOT NULL,
                price    REAL    NOT NULL,
                category TEXT    NOT NULL,
                stock    INTEGER NOT NULL DEFAULT 0
            );
            """
        )

    # ── Create ─────────────────────────────────────────────────────────── #

    def add(
        self,
        conn: sqlite3.Connection,
        name: str,
        price: float,
        category: str,
        stock: int = 0,
    ) -> int:
        if price <= 0:
            raise ValueError(f"Price must be positive, got {price}")
        cursor = conn.execute(
            "INSERT INTO products (name, price, category, stock) VALUES (?, ?, ?, ?)",
            (name, price, category, stock),
        )
        return cursor.lastrowid  # type: ignore[return-value]

    # ── Read ───────────────────────────────────────────────────────────── #

    def find_by_id(self, conn: sqlite3.Connection, product_id: int) -> sqlite3.Row | None:
        return conn.execute(
            "SELECT * FROM products WHERE id = ?", (product_id,)
        ).fetchone()

    def find_all(self, conn: sqlite3.Connection) -> list[sqlite3.Row]:
        return conn.execute(
            "SELECT * FROM products ORDER BY name"
        ).fetchall()

    def find_by_category(
        self, conn: sqlite3.Connection, category: str
    ) -> list[sqlite3.Row]:
        return conn.execute(
            "SELECT * FROM products WHERE category = ? ORDER BY name",
            (category,),
        ).fetchall()

    def search(self, conn: sqlite3.Connection, query: str) -> list[sqlite3.Row]:
        return conn.execute(
            "SELECT * FROM products WHERE name LIKE ? ORDER BY name",
            (f"%{query}%",),
        ).fetchall()

    # ── Update ─────────────────────────────────────────────────────────── #

    def update_price(
        self, conn: sqlite3.Connection, product_id: int, new_price: float
    ) -> bool:
        if new_price <= 0:
            raise ValueError(f"Price must be positive, got {new_price}")
        cursor = conn.execute(
            "UPDATE products SET price = ? WHERE id = ?",
            (new_price, product_id),
        )
        return cursor.rowcount > 0

    def update_stock(
        self, conn: sqlite3.Connection, product_id: int, new_stock: int
    ) -> bool:
        cursor = conn.execute(
            "UPDATE products SET stock = ? WHERE id = ?",
            (new_stock, product_id),
        )
        return cursor.rowcount > 0

    # ── Delete ─────────────────────────────────────────────────────────── #

    def delete(self, conn: sqlite3.Connection, product_id: int) -> bool:
        cursor = conn.execute("DELETE FROM products WHERE id = ?", (product_id,))
        return cursor.rowcount > 0

    # ── Aggregate ──────────────────────────────────────────────────────── #

    def total_value(self, conn: sqlite3.Connection) -> float:
        row = conn.execute(
            "SELECT COALESCE(SUM(price * stock), 0.0) FROM products"
        ).fetchone()
        return float(row[0])


# --------------------------------------------------------------------------- #
#  Run the same tests as exercise_01.py to confirm the solution works          #
# --------------------------------------------------------------------------- #


def run_tests() -> None:
    repo = ProductRepository()

    with get_connection() as conn:
        repo.create_table(conn)

        laptop_id = repo.add(conn, "Laptop", 999.99, "Electronics", stock=10)
        mouse_id  = repo.add(conn, "Mouse",   25.00, "Electronics", stock=50)
        desk_id   = repo.add(conn, "Desk",   349.00, "Furniture",   stock=5)
        chair_id  = repo.add(conn, "Chair",  199.00, "Furniture",   stock=8)
        _         = repo.add(conn, "USB Hub", 15.00, "Electronics", stock=0)

        assert isinstance(laptop_id, int)
        assert laptop_id != mouse_id
        print("  ✓ add()")

        laptop = repo.find_by_id(conn, laptop_id)
        assert laptop is not None
        assert laptop["name"] == "Laptop"
        assert laptop["price"] == 999.99
        print("  ✓ find_by_id()")

        assert repo.find_by_id(conn, 9999) is None
        print("  ✓ find_by_id() returns None for missing product")

        raised = False
        try:
            repo.add(conn, "Bad", -5.0, "Test")
        except ValueError:
            raised = True
        assert raised
        print("  ✓ add() raises ValueError for negative price")

        all_products = repo.find_all(conn)
        assert len(all_products) == 5
        names = [p["name"] for p in all_products]
        assert names == sorted(names)
        print("  ✓ find_all()")

        electronics = repo.find_by_category(conn, "Electronics")
        assert len(electronics) == 3
        assert repo.find_by_category(conn, "None") == []
        print("  ✓ find_by_category()")

        chair_results = repo.search(conn, "chair")
        assert len(chair_results) == 1
        assert chair_results[0]["name"] == "Chair"
        print("  ✓ search()")

        ok = repo.update_price(conn, laptop_id, 899.99)
        assert ok is True
        assert repo.find_by_id(conn, laptop_id)["price"] == 899.99
        assert repo.update_price(conn, 9999, 100.0) is False
        print("  ✓ update_price()")

        ok = repo.update_stock(conn, mouse_id, 100)
        assert ok is True
        assert repo.find_by_id(conn, mouse_id)["stock"] == 100
        print("  ✓ update_stock()")

        expected = 899.99 * 10 + 25.00 * 100 + 349.00 * 5 + 199.00 * 8 + 15.00 * 0
        total = repo.total_value(conn)
        assert abs(total - expected) < 0.01, f"Expected {expected:.2f}, got {total:.2f}"
        print("  ✓ total_value()")

        ok = repo.delete(conn, desk_id)
        assert ok is True
        assert repo.find_by_id(conn, desk_id) is None
        assert len(repo.find_all(conn)) == 4
        assert repo.delete(conn, 9999) is False
        print("  ✓ delete()")

    print("\nAll tests passed!")


if __name__ == "__main__":
    print("Running solution_01 tests ...\n")
    run_tests()
