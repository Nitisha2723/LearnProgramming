# SQLite3 — Python's Built-In Database

Python ships with `sqlite3` in the standard library. Zero installation. Full SQL. Perfect for learning, prototyping, and applications that don't need a server.

---

## What is SQLite?

SQLite is a **serverless, file-based relational database**. The entire database lives in a single `.db` file (or in memory with `":memory:"`). It supports most of SQL: CREATE TABLE, INSERT, SELECT, UPDATE, DELETE, joins, transactions, indexes.

Use it when:
- You're learning SQL and Python together
- Your app is a desktop tool, CLI, or small web service
- You want zero-dependency persistence
- You're testing code that uses a real database

---

## Connecting

```python
import sqlite3

# File-based database (creates the file if it doesn't exist)
conn = sqlite3.connect("myapp.db")

# In-memory database (gone when conn closes — great for tests)
conn = sqlite3.connect(":memory:")
```

`sqlite3.connect()` returns a `Connection` object. You **must** close it when done.

---

## The Cursor

All SQL execution goes through a **cursor**:

```python
conn = sqlite3.connect(":memory:")
cursor = conn.cursor()

cursor.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, email TEXT UNIQUE)")
conn.commit()   # persist the change
conn.close()
```

The cursor is the "pen" that writes to and reads from the database.

---

## Parameterized Queries — Always Use `?` Placeholders

Never use f-strings or string concatenation to build SQL. That opens you to SQL injection.

```python
# WRONG — SQL injection risk
name = "Alice'; DROP TABLE users; --"
cursor.execute(f"INSERT INTO users (name) VALUES ('{name}')")  # DO NOT DO THIS

# CORRECT — use ? placeholders
cursor.execute("INSERT INTO users (name, email) VALUES (?, ?)", (name, email))
```

The `sqlite3` module escapes the values automatically. The second argument is always a **tuple** (or list).

---

## executemany — Bulk Inserts

```python
users = [
    ("Alice", "alice@example.com"),
    ("Bob",   "bob@example.com"),
    ("Carol", "carol@example.com"),
]
cursor.executemany("INSERT INTO users (name, email) VALUES (?, ?)", users)
conn.commit()
```

`executemany` iterates through the sequence and runs the query once per row. Much faster than calling `execute` in a loop.

---

## Fetching Results

After a SELECT, use one of three fetch methods:

```python
cursor.execute("SELECT id, name, email FROM users")

# Get one row (returns None if no rows left)
row = cursor.fetchone()       # (1, 'Alice', 'alice@example.com')

# Get all remaining rows
rows = cursor.fetchall()      # [(1, 'Alice', ...), (2, 'Bob', ...), ...]

# Get N rows at a time
batch = cursor.fetchmany(10)  # list of up to 10 rows

# Or iterate directly (memory-efficient for large results)
cursor.execute("SELECT * FROM users")
for row in cursor:
    print(row)
```

By default, rows are plain **tuples**. The column names are in `cursor.description`:

```python
print([col[0] for col in cursor.description])  # ['id', 'name', 'email']
```

---

## Row Factory — Named Columns

Access columns by name instead of index:

```python
conn.row_factory = sqlite3.Row

cursor.execute("SELECT * FROM users WHERE id = ?", (1,))
row = cursor.fetchone()

print(row["name"])   # 'Alice'    ← by name
print(row[1])        # 'Alice'    ← by index still works
print(dict(row))     # {'id': 1, 'name': 'Alice', 'email': '...'}
```

Set `row_factory` right after creating the connection, before any queries.

---

## Transactions

SQLite wraps changes in **transactions**. You must `commit()` to persist writes, or `rollback()` to undo them.

```python
conn = sqlite3.connect("myapp.db")
try:
    conn.execute("INSERT INTO accounts (owner, balance) VALUES (?, ?)", ("Alice", 1000))
    conn.execute("INSERT INTO accounts (owner, balance) VALUES (?, ?)", ("Bob", 500))
    conn.commit()       # both rows saved
except Exception:
    conn.rollback()     # neither row saved
    raise
finally:
    conn.close()
```

If you crash without committing, the transaction is automatically rolled back.

---

## Context Manager Pattern

The connection supports `with` as a **transaction** manager (not a connection closer):

```python
conn = sqlite3.connect("myapp.db")

with conn:
    # if no exception → auto-commit
    # if exception    → auto-rollback
    conn.execute("INSERT INTO users (name, email) VALUES (?, ?)", ("Dave", "dave@example.com"))

conn.close()  # you still close manually
```

For full resource management, use `contextlib.contextmanager`:

```python
from contextlib import contextmanager

@contextmanager
def get_connection(db_path: str):
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()

# Usage
with get_connection("myapp.db") as conn:
    conn.execute("INSERT INTO users (name, email) VALUES (?, ?)", ("Eve", "eve@example.com"))
# committed and closed automatically
```

---

## executescript — Multiple Statements

Run a block of SQL (schema setup, migrations):

```python
conn.executescript("""
    CREATE TABLE IF NOT EXISTS users (
        id    INTEGER PRIMARY KEY AUTOINCREMENT,
        name  TEXT NOT NULL,
        email TEXT UNIQUE NOT NULL
    );
    CREATE TABLE IF NOT EXISTS posts (
        id        INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id   INTEGER REFERENCES users(id),
        title     TEXT NOT NULL,
        body      TEXT
    );
""")
conn.commit()
```

`executescript` **commits any pending transaction** before running. Don't mix it with open transactions.

---

## lastrowid and rowcount

```python
cursor.execute("INSERT INTO users (name, email) VALUES (?, ?)", ("Frank", "frank@example.com"))
print(cursor.lastrowid)   # the auto-generated primary key of the new row

cursor.execute("DELETE FROM users WHERE name = ?", ("Frank",))
print(cursor.rowcount)    # number of rows affected: 1
```

---

## Common Patterns

### Schema setup function

```python
def create_tables(conn: sqlite3.Connection) -> None:
    conn.executescript("""
        CREATE TABLE IF NOT EXISTS users (
            id    INTEGER PRIMARY KEY AUTOINCREMENT,
            name  TEXT NOT NULL,
            email TEXT UNIQUE NOT NULL
        );
    """)
```

### SELECT with optional filter

```python
def find_users(conn, name_filter: str | None = None) -> list[sqlite3.Row]:
    if name_filter:
        return conn.execute(
            "SELECT * FROM users WHERE name LIKE ?", (f"%{name_filter}%",)
        ).fetchall()
    return conn.execute("SELECT * FROM users").fetchall()
```

---

## sqlite3 vs SQLAlchemy — When to Use Which

| Situation | Use |
|---|---|
| Scripts, CLIs, simple apps | `sqlite3` |
| Learning SQL directly | `sqlite3` |
| Zero dependencies | `sqlite3` |
| Complex domain models | SQLAlchemy ORM |
| Multiple database backends | SQLAlchemy |
| Production web application | SQLAlchemy |
| You want migrations (Alembic) | SQLAlchemy |
| Team project with schemas | SQLAlchemy |

`sqlite3` is transparent — you write SQL yourself. SQLAlchemy generates SQL from Python objects. Both are valid; the choice depends on your project's complexity.

---

## Key Takeaways

- `sqlite3` is built-in — no install needed
- Always use `?` placeholders, never string formatting
- Set `conn.row_factory = sqlite3.Row` to access columns by name
- Wrap writes in try/except with `commit()` / `rollback()`
- Use `":memory:"` for tests — fast, isolated, no cleanup
- `executemany` is faster than looping `execute`
