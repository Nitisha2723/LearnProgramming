"""
sqlite3_basics.py — Complete sqlite3 demonstration.

Uses an in-memory database — runs with zero installation.

Topics covered:
- sqlite3.connect() with ":memory:"
- row_factory for named column access
- Context manager for connection lifecycle
- CREATE TABLE, INSERT, executemany, SELECT, UPDATE, DELETE
- Parameterized queries with ? placeholders
- lastrowid and rowcount
"""

import sqlite3
from contextlib import contextmanager
from typing import Generator

# --------------------------------------------------------------------------- #
#  Connection management                                                        #
# --------------------------------------------------------------------------- #

DATABASE = ":memory:"  # change to "users.db" to persist to a file


@contextmanager
def get_connection(db: str = DATABASE) -> Generator[sqlite3.Connection, None, None]:
    """Yield a connection, commit on success, rollback on exception, always close."""
    conn = sqlite3.connect(db)
    conn.row_factory = sqlite3.Row  # access columns by name: row["name"]
    conn.execute("PRAGMA foreign_keys = ON")  # enforce FK constraints in SQLite
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


# --------------------------------------------------------------------------- #
#  Schema                                                                       #
# --------------------------------------------------------------------------- #


def create_tables(conn: sqlite3.Connection) -> None:
    """Create the schema. Safe to call multiple times (IF NOT EXISTS)."""
    conn.executescript(
        """
        CREATE TABLE IF NOT EXISTS users (
            id    INTEGER PRIMARY KEY AUTOINCREMENT,
            name  TEXT    NOT NULL,
            email TEXT    NOT NULL UNIQUE
        );

        CREATE TABLE IF NOT EXISTS posts (
            id      INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
            title   TEXT    NOT NULL,
            body    TEXT    NOT NULL DEFAULT ''
        );
        """
    )


# --------------------------------------------------------------------------- #
#  User CRUD                                                                    #
# --------------------------------------------------------------------------- #


def insert_user(conn: sqlite3.Connection, name: str, email: str) -> int:
    """Insert a new user and return the auto-generated id."""
    cursor = conn.execute(
        "INSERT INTO users (name, email) VALUES (?, ?)",
        (name, email),
    )
    return cursor.lastrowid  # type: ignore[return-value]


def insert_users_bulk(conn: sqlite3.Connection, users: list[tuple[str, str]]) -> None:
    """Insert many users at once using executemany."""
    conn.executemany(
        "INSERT INTO users (name, email) VALUES (?, ?)",
        users,
    )


def find_all_users(conn: sqlite3.Connection) -> list[sqlite3.Row]:
    """Return all users ordered by name."""
    return conn.execute("SELECT * FROM users ORDER BY name").fetchall()


def find_user_by_id(conn: sqlite3.Connection, user_id: int) -> sqlite3.Row | None:
    """Return a single user by primary key, or None if not found."""
    return conn.execute(
        "SELECT * FROM users WHERE id = ?", (user_id,)
    ).fetchone()


def find_users_by_name(conn: sqlite3.Connection, name_fragment: str) -> list[sqlite3.Row]:
    """Return users whose name contains name_fragment (case-insensitive)."""
    return conn.execute(
        "SELECT * FROM users WHERE name LIKE ?",
        (f"%{name_fragment}%",),
    ).fetchall()


def update_email(conn: sqlite3.Connection, user_id: int, new_email: str) -> bool:
    """Update the email of a user. Returns True if the user was found."""
    cursor = conn.execute(
        "UPDATE users SET email = ? WHERE id = ?",
        (new_email, user_id),
    )
    return cursor.rowcount > 0


def delete_user(conn: sqlite3.Connection, user_id: int) -> bool:
    """Delete a user by id. Returns True if the user was found and deleted."""
    cursor = conn.execute("DELETE FROM users WHERE id = ?", (user_id,))
    return cursor.rowcount > 0


# --------------------------------------------------------------------------- #
#  Post CRUD                                                                    #
# --------------------------------------------------------------------------- #


def insert_post(conn: sqlite3.Connection, user_id: int, title: str, body: str = "") -> int:
    """Insert a new post and return its id."""
    cursor = conn.execute(
        "INSERT INTO posts (user_id, title, body) VALUES (?, ?, ?)",
        (user_id, title, body),
    )
    return cursor.lastrowid  # type: ignore[return-value]


def find_posts_by_user(conn: sqlite3.Connection, user_id: int) -> list[sqlite3.Row]:
    """Return all posts for a given user."""
    return conn.execute(
        "SELECT * FROM posts WHERE user_id = ? ORDER BY id",
        (user_id,),
    ).fetchall()


def find_posts_with_authors(conn: sqlite3.Connection) -> list[sqlite3.Row]:
    """Return all posts joined with the author's name and email."""
    return conn.execute(
        """
        SELECT
            posts.id        AS post_id,
            posts.title,
            posts.body,
            users.id        AS author_id,
            users.name      AS author_name,
            users.email     AS author_email
        FROM posts
        JOIN users ON users.id = posts.user_id
        ORDER BY posts.id
        """
    ).fetchall()


# --------------------------------------------------------------------------- #
#  Aggregates                                                                   #
# --------------------------------------------------------------------------- #


def count_users(conn: sqlite3.Connection) -> int:
    """Return the total number of users."""
    row = conn.execute("SELECT COUNT(*) FROM users").fetchone()
    return row[0]


def count_posts_per_user(conn: sqlite3.Connection) -> list[sqlite3.Row]:
    """Return (user_id, name, post_count) for all users."""
    return conn.execute(
        """
        SELECT
            users.id,
            users.name,
            COUNT(posts.id) AS post_count
        FROM users
        LEFT JOIN posts ON posts.user_id = users.id
        GROUP BY users.id, users.name
        ORDER BY post_count DESC
        """
    ).fetchall()


# --------------------------------------------------------------------------- #
#  Demo                                                                         #
# --------------------------------------------------------------------------- #


def _print_separator(title: str) -> None:
    print(f"\n{'─' * 50}")
    print(f"  {title}")
    print("─" * 50)


def main() -> None:
    print("sqlite3 Basics Demo — in-memory database")
    print("=" * 50)

    with get_connection() as conn:
        # ── Schema ─────────────────────────────────────────
        create_tables(conn)
        print("Tables created.")

        # ── Insert single user ──────────────────────────────
        _print_separator("Single INSERT with lastrowid")
        alice_id = insert_user(conn, "Alice", "alice@example.com")
        print(f"Inserted Alice with id={alice_id}")

        # ── Bulk insert ─────────────────────────────────────
        _print_separator("Bulk INSERT with executemany")
        bulk_data = [
            ("Bob",   "bob@example.com"),
            ("Carol", "carol@example.com"),
            ("Dave",  "dave@example.com"),
        ]
        insert_users_bulk(conn, bulk_data)
        print(f"Bulk-inserted {len(bulk_data)} users. Total: {count_users(conn)}")

        # ── Select all ──────────────────────────────────────
        _print_separator("SELECT all users")
        for row in find_all_users(conn):
            print(f"  [{row['id']}] {row['name']} <{row['email']}>")

        # ── Select by id ────────────────────────────────────
        _print_separator("SELECT by id")
        user = find_user_by_id(conn, alice_id)
        if user:
            print(f"Found: {user['name']} — {user['email']}")

        # ── Find with LIKE ──────────────────────────────────
        _print_separator("SELECT with LIKE filter")
        matches = find_users_by_name(conn, "a")
        print(f"Users with 'a' in name: {[r['name'] for r in matches]}")

        # ── Update ──────────────────────────────────────────
        _print_separator("UPDATE email")
        ok = update_email(conn, alice_id, "alice.new@example.com")
        updated = find_user_by_id(conn, alice_id)
        print(f"Update succeeded: {ok}")
        print(f"New email: {updated['email']}")  # type: ignore[index]

        # ── Posts ───────────────────────────────────────────
        _print_separator("INSERT posts and JOIN query")
        bob_id = find_users_by_name(conn, "Bob")[0]["id"]
        insert_post(conn, alice_id, "Hello World", "This is Alice's first post.")
        insert_post(conn, alice_id, "SQLite Tips", "Always use parameterized queries.")
        insert_post(conn, bob_id,   "Bob's Post",  "Hi from Bob!")

        posts_with_authors = find_posts_with_authors(conn)
        for p in posts_with_authors:
            print(f"  [{p['post_id']}] '{p['title']}' by {p['author_name']}")

        # ── Counts ──────────────────────────────────────────
        _print_separator("Posts per user (LEFT JOIN + GROUP BY)")
        for row in count_posts_per_user(conn):
            print(f"  {row['name']}: {row['post_count']} post(s)")

        # ── Delete ──────────────────────────────────────────
        _print_separator("DELETE user")
        dave_id = find_users_by_name(conn, "Dave")[0]["id"]
        ok = delete_user(conn, dave_id)
        print(f"Deleted Dave (id={dave_id}): {ok}")
        print(f"Users remaining: {count_users(conn)}")

        # ── rowcount ────────────────────────────────────────
        _print_separator("rowcount on no-op DELETE")
        ok = delete_user(conn, 9999)  # non-existent id
        print(f"Delete of id=9999 succeeded: {ok}")  # False

    print("\nConnection closed. All done.")


if __name__ == "__main__":
    main()
