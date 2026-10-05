# Module 10 — Database Programming with Python

Learn to persist data using Python's built-in `sqlite3` module and the SQLAlchemy ORM.

---

## Learning Objectives

By the end of this module you will be able to:

- Connect to a SQLite database, run parameterized queries, and manage transactions with `sqlite3`
- Define SQLAlchemy ORM models using the modern 2.x `Mapped` / `mapped_column` syntax
- Perform CRUD operations through a `Session`
- Define one-to-many relationships with `relationship()` and `back_populates`
- Avoid the N+1 query problem using `selectinload`
- Implement the Repository pattern to separate persistence from business logic
- Test database code with in-memory SQLite fixtures

---

## Installation

`sqlite3` is part of Python's standard library — no installation needed for the first section.

SQLAlchemy is required for sections 2–4:

```bash
pip install sqlalchemy
```

To run the tests:

```bash
pip install pytest
pytest tests/test_database.py -v
```

---

## Module Structure

```
10-database/
├── theory/
│   ├── 01-sqlite3-basics.md        # sqlite3 API: connect, cursor, execute, fetch, transactions
│   ├── 02-sqlalchemy-core.md       # Engine, text(), MetaData, Table, Core expressions
│   ├── 03-sqlalchemy-orm.md        # DeclarativeBase, Session, Mapped, relationships
│   └── 04-best-practices.md        # Repository pattern, N+1, migrations, security
│
├── code/
│   ├── sqlite3_basics.py           # Full sqlite3 CRUD example (zero dependencies)
│   ├── sqlalchemy_orm.py           # SQLAlchemy ORM example with relationships
│   └── user_repository.py          # Repository pattern demo
│
├── exercises/
│   ├── exercise_01.py              # Implement ProductRepository with sqlite3
│   └── solutions/
│       └── solution_01.py
│
├── mini-project/
│   └── library_db.py              # Library system: Book, Member, Loan + transactions
│
└── tests/
    └── test_database.py           # pytest suite covering all modules
```

---

## Suggested Learning Path

1. Read `theory/01-sqlite3-basics.md`
2. Run `python code/sqlite3_basics.py` — study each function
3. Do `exercises/exercise_01.py` — implement ProductRepository
4. Read `theory/02-sqlalchemy-core.md` and `theory/03-sqlalchemy-orm.md`
5. Run `python code/sqlalchemy_orm.py` — study models, session, relationships
6. Run `python code/user_repository.py` — study the repository pattern
7. Read `theory/04-best-practices.md`
8. Study and run `mini-project/library_db.py`
9. Run the tests: `pytest tests/test_database.py -v`

---

## sqlite3 vs SQLAlchemy — Quick Comparison

| Feature | `sqlite3` | SQLAlchemy ORM |
|---|---|---|
| Installation | Built-in | `pip install sqlalchemy` |
| Database support | SQLite only | SQLite, PostgreSQL, MySQL, MSSQL, Oracle |
| Query style | Raw SQL strings | Python objects and expressions |
| Schema definition | SQL strings | Python model classes |
| Migrations | Manual | Alembic integration |
| Relationships | Manual joins | `relationship()` with lazy/eager loading |
| Type safety | No | Yes (`Mapped[str]`, mypy-compatible) |
| Connection pooling | No | Built-in |
| Best for | Scripts, CLIs, learning SQL | Applications, teams, production |

---

## Key Concepts

### sqlite3

```python
import sqlite3
from contextlib import contextmanager

@contextmanager
def get_connection(db=":memory:"):
    conn = sqlite3.connect(db)
    conn.row_factory = sqlite3.Row   # named column access
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()

with get_connection() as conn:
    conn.execute("INSERT INTO users (name) VALUES (?)", ("Alice",))
```

### SQLAlchemy ORM

```python
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, Session

class Base(DeclarativeBase): pass

class User(Base):
    __tablename__ = "users"
    id:    Mapped[int] = mapped_column(primary_key=True)
    name:  Mapped[str] = mapped_column(String(100))
    email: Mapped[str] = mapped_column(String(200), unique=True)

with Session(engine) as session:
    user = User(name="Alice", email="alice@example.com")
    session.add(user)
    session.commit()
```

---

## Running the Examples

All code examples use in-memory databases — no files created, no cleanup needed.

```bash
# sqlite3 demo
python code/sqlite3_basics.py

# SQLAlchemy ORM demo
python code/sqlalchemy_orm.py

# Repository pattern demo
python code/user_repository.py

# Library mini-project
python mini-project/library_db.py

# Exercise (implement then test)
python exercises/exercise_01.py

# Full test suite
pytest tests/test_database.py -v
```
