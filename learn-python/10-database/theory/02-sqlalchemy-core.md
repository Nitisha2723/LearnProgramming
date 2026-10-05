# SQLAlchemy Core

SQLAlchemy is the most widely used Python database toolkit. It has two layers: **Core** (close to SQL) and **ORM** (Python objects ↔ tables). This file covers Core.

---

## What is SQLAlchemy?

SQLAlchemy provides:

1. **Engine** — manages database connections and connection pooling
2. **Core** — SQL expression language; you construct queries as Python objects, SQLAlchemy generates SQL
3. **ORM** — maps Python classes to database tables (covered in `03-sqlalchemy-orm.md`)

Install once:
```bash
pip install sqlalchemy
```

SQLAlchemy 2.x (released 2023) is the current API. This file uses 2.x throughout.

---

## Core vs ORM

| | Core | ORM |
|---|---|---|
| Abstraction level | SQL expressions | Python objects |
| Query style | `select(users_table)` | `session.query(User)` |
| When to use | Data-heavy scripts, ETL, raw control | Application domain models |
| Learning curve | Lower | Higher |
| SQL knowledge needed | Yes | Less (but still helpful) |

You can mix Core and ORM in the same application.

---

## The Engine

The engine is your connection to the database. Create one per application:

```python
from sqlalchemy import create_engine

# SQLite in-memory (tests, demos)
engine = create_engine("sqlite:///:memory:", echo=True)

# SQLite file
engine = create_engine("sqlite:///myapp.db")

# PostgreSQL (requires psycopg2)
engine = create_engine("postgresql+psycopg2://user:password@localhost:5432/mydb")

# MySQL (requires pymysql)
engine = create_engine("mysql+pymysql://user:password@localhost/mydb")
```

`echo=True` logs every SQL statement — useful while developing.

### Connection strings format

```
dialect+driver://username:password@host:port/database
```

Common dialects: `sqlite`, `postgresql`, `mysql`, `mssql`, `oracle`.

---

## Executing Raw SQL with `text()`

For quick SQL without the expression layer:

```python
from sqlalchemy import create_engine, text

engine = create_engine("sqlite:///:memory:")

with engine.connect() as conn:
    conn.execute(text("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, email TEXT)"))
    conn.execute(
        text("INSERT INTO users (name, email) VALUES (:name, :email)"),
        {"name": "Alice", "email": "alice@example.com"},
    )
    conn.commit()

    result = conn.execute(text("SELECT * FROM users"))
    for row in result:
        print(row.name, row.email)    # access by attribute name
```

Always use `:param_name` placeholders with `text()`, never string formatting.

---

## Connection Pooling

The engine maintains a **pool** of reusable connections. Instead of opening/closing a connection for every query, it keeps connections ready:

```python
engine = create_engine(
    "postgresql+psycopg2://user:pass@localhost/mydb",
    pool_size=10,        # number of persistent connections
    max_overflow=20,     # extra connections allowed when pool is full
    pool_timeout=30,     # seconds to wait for a connection
    pool_recycle=1800,   # recycle connections older than 30 minutes
)
```

SQLite doesn't benefit from pooling (it's file-based), but PostgreSQL and MySQL do. The defaults work well for most applications.

---

## Metadata and Table Definitions

`MetaData` holds all table definitions. `Table` describes a table's columns:

```python
from sqlalchemy import MetaData, Table, Column, Integer, String, ForeignKey, DateTime
from datetime import datetime

metadata = MetaData()

users = Table(
    "users",
    metadata,
    Column("id",    Integer, primary_key=True, autoincrement=True),
    Column("name",  String(100), nullable=False),
    Column("email", String(200), nullable=False, unique=True),
)

posts = Table(
    "posts",
    metadata,
    Column("id",        Integer, primary_key=True, autoincrement=True),
    Column("title",     String(200), nullable=False),
    Column("content",   String, nullable=False),
    Column("user_id",   Integer, ForeignKey("users.id"), nullable=False),
)

# Create all tables in the database
metadata.create_all(engine)
```

---

## Insert

```python
from sqlalchemy import insert

with engine.connect() as conn:
    # Single row
    stmt = insert(users).values(name="Alice", email="alice@example.com")
    result = conn.execute(stmt)
    print(result.inserted_primary_key)   # (1,)

    # Many rows
    conn.execute(
        insert(users),
        [
            {"name": "Bob",   "email": "bob@example.com"},
            {"name": "Carol", "email": "carol@example.com"},
        ],
    )
    conn.commit()
```

---

## Select

```python
from sqlalchemy import select

with engine.connect() as conn:
    # SELECT * FROM users
    stmt = select(users)
    for row in conn.execute(stmt):
        print(row.id, row.name, row.email)

    # SELECT with WHERE
    stmt = select(users).where(users.c.name == "Alice")
    row = conn.execute(stmt).fetchone()

    # SELECT specific columns
    stmt = select(users.c.id, users.c.name)

    # ORDER BY, LIMIT
    stmt = select(users).order_by(users.c.name).limit(10)

    # JOIN
    stmt = (
        select(users.c.name, posts.c.title)
        .join(posts, users.c.id == posts.c.user_id)
        .where(users.c.name == "Alice")
    )
```

`users.c` is the column namespace. `users.c.name` is the "name" column of the users table.

---

## Update

```python
from sqlalchemy import update

with engine.connect() as conn:
    stmt = (
        update(users)
        .where(users.c.id == 1)
        .values(email="newalice@example.com")
    )
    result = conn.execute(stmt)
    print(f"Updated {result.rowcount} rows")
    conn.commit()
```

---

## Delete

```python
from sqlalchemy import delete

with engine.connect() as conn:
    stmt = delete(users).where(users.c.id == 1)
    result = conn.execute(stmt)
    print(f"Deleted {result.rowcount} rows")
    conn.commit()
```

---

## Result Objects

`conn.execute()` returns a `CursorResult`:

```python
result = conn.execute(select(users))

# Fetch all rows as a list
rows = result.all()            # [Row(...), Row(...), ...]

# Fetch one
row = result.fetchone()        # Row(...) or None

# Column access
print(row.name)                # by attribute
print(row[1])                  # by index
print(row._mapping["email"])   # as dict-like

# Get column names
print(result.keys())           # RMKeyView(['id', 'name', 'email'])

# Scalar (single value)
result = conn.execute(select(func.count()).select_from(users))
count = result.scalar()        # 3
```

---

## SQLAlchemy Core vs Raw sqlite3

| | `sqlite3` | SQLAlchemy Core |
|---|---|---|
| SQL dialect | SQLite only | Any database |
| Query construction | Manual strings | Python expression objects |
| Parameter style | `?` | `:name` (with `text()`) |
| Connection pooling | No | Yes |
| Schema introspection | No | Yes |
| Result objects | tuple / Row | `Row` with attribute access |

---

## Key Takeaways

- `create_engine()` is a one-time setup — reuse it throughout the application
- Use `text()` for raw SQL; use `:name` placeholders, not string formatting
- `Table` + `MetaData` describe your schema; `metadata.create_all(engine)` creates the tables
- `select`, `insert`, `update`, `delete` return composable statement objects
- Always `conn.commit()` after writes, or use `engine.begin()` for auto-commit on exit
- Connection pooling is automatic — don't try to manage it manually
