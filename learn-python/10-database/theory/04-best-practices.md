# Database Best Practices

Production-quality database code goes beyond "it works". This file covers patterns that make code maintainable, testable, and safe.

---

## The Repository Pattern

A **repository** wraps database access behind a clean Python interface. The rest of your application never calls SQLAlchemy directly — it calls the repository.

```
Application layer  →  UserRepository  →  SQLAlchemy Session  →  Database
```

Benefits:
- Swap databases without touching business logic
- Mock the repository in unit tests (no real database needed)
- All queries for a model live in one place
- Business logic is not polluted with ORM details

### Example

```python
from dataclasses import dataclass
from typing import Optional
from sqlalchemy.orm import Session
from sqlalchemy import select
from .models import User

@dataclass
class UserRepository:
    session: Session

    def find_by_id(self, user_id: int) -> Optional[User]:
        return self.session.get(User, user_id)

    def find_all(self) -> list[User]:
        return self.session.execute(select(User)).scalars().all()

    def find_by_email(self, email: str) -> Optional[User]:
        stmt = select(User).where(User.email == email)
        return self.session.execute(stmt).scalar_one_or_none()

    def save(self, user: User) -> User:
        self.session.add(user)
        self.session.flush()   # gets the id without committing
        return user

    def delete(self, user_id: int) -> bool:
        user = self.find_by_id(user_id)
        if user is None:
            return False
        self.session.delete(user)
        return True
```

The session is injected; the repository doesn't manage transactions itself. Transactions belong to the **service layer** (the caller).

---

## Connection String Security

Never hardcode credentials in source code:

```python
# BAD — credentials in code, visible in git history
engine = create_engine("postgresql://admin:secretpassword@prod-db:5432/myapp")
```

Use environment variables:

```python
import os
from sqlalchemy import create_engine

DATABASE_URL = os.environ["DATABASE_URL"]
engine = create_engine(DATABASE_URL)
```

For local development, use a `.env` file with `python-dotenv`:

```bash
# .env  (add to .gitignore!)
DATABASE_URL=postgresql://dev:dev@localhost:5432/myapp_dev
```

```python
from dotenv import load_dotenv
load_dotenv()

import os
DATABASE_URL = os.environ["DATABASE_URL"]
```

Always add `.env` to `.gitignore`. Use a `.env.example` file with dummy values as documentation.

---

## The N+1 Query Problem

This is the most common database performance mistake in ORM code.

### The problem

```python
# Fetch all users — 1 query
users = session.execute(select(User)).scalars().all()

# Access posts for each user — 1 query PER user
for user in users:
    print(f"{user.name} has {len(user.posts)} posts")  # SELECT for each user!
```

With 100 users, this is 101 queries. With 10,000 users, 10,001 queries. This kills performance.

### The fix: eager loading

```python
from sqlalchemy.orm import selectinload

# 2 queries total, regardless of user count:
# 1. SELECT all users
# 2. SELECT all posts WHERE user_id IN (1, 2, 3, ...)
stmt = select(User).options(selectinload(User.posts))
users = session.execute(stmt).scalars().all()

for user in users:
    print(f"{user.name} has {len(user.posts)} posts")  # no extra queries
```

### `selectinload` vs `joinedload`

| Strategy | SQL | Use for |
|---|---|---|
| `selectinload` | Two queries (IN clause) | Collections (one-to-many) |
| `joinedload` | One query with JOIN | Single objects (many-to-one) |
| `subqueryload` | Two queries (subquery) | Large collections (legacy) |

```python
# joinedload for many-to-one (post → author)
stmt = select(Post).options(joinedload(Post.author))

# selectinload for one-to-many (user → posts)
stmt = select(User).options(selectinload(User.posts))
```

---

## Migrations with Alembic

Your models will change. Alembic tracks schema changes as versioned migration scripts.

### Setup

```bash
pip install alembic
alembic init alembic
```

Edit `alembic/env.py` to point to your models' `Base.metadata`.

### Workflow

```bash
# Generate a migration from model changes
alembic revision --autogenerate -m "add_age_column_to_users"

# Apply migrations
alembic upgrade head

# Roll back one step
alembic downgrade -1

# See current version
alembic current
```

Migration files are Python scripts with `upgrade()` and `downgrade()` functions. Commit them to version control — they are part of your codebase.

### Never use `create_all` in production

`Base.metadata.create_all(engine)` only creates missing tables; it never modifies existing ones. Use it for development and tests only. In production, always use Alembic migrations.

---

## When to Use Raw SQL vs ORM

### Use the ORM when:
- Working with a complex domain model (users, orders, products with relationships)
- Doing standard CRUD operations
- Working in a team — models are self-documenting
- You need type safety (mypy understands `Mapped[str]`)

### Use raw SQL (or Core) when:
- Bulk operations: INSERT 100,000 rows, UPDATE millions of records
- Complex analytical queries: window functions, CTEs, aggregations
- Database-specific features the ORM doesn't expose well
- Performance-critical paths where you need exact SQL control

### You can mix both

```python
# ORM for most operations
user = session.get(User, user_id)
user.name = "New Name"
session.commit()

# Raw SQL for bulk update
session.execute(
    text("UPDATE users SET last_login = NOW() WHERE id = ANY(:ids)"),
    {"ids": [1, 2, 3, 4, 5]},
)
session.commit()
```

---

## Testing with In-Memory SQLite

Tests should use an **in-memory SQLite database**: fast, isolated, no file cleanup.

### pytest fixture pattern

```python
import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from myapp.models import Base

@pytest.fixture(scope="function")
def engine():
    eng = create_engine("sqlite:///:memory:")
    Base.metadata.create_all(eng)
    yield eng
    Base.metadata.drop_all(eng)

@pytest.fixture(scope="function")
def session(engine):
    with Session(engine) as s:
        yield s
```

Each test function gets a fresh engine and session — fully isolated.

### Testing repositories

```python
def test_find_by_email(session):
    repo = UserRepository(session)
    user = User(name="Alice", email="alice@test.com")
    session.add(user)
    session.flush()

    found = repo.find_by_email("alice@test.com")
    assert found is not None
    assert found.name == "Alice"
```

### What to test

- Create, read, update, delete operations
- Constraint violations (duplicate email, not-null, foreign key)
- Queries return correct filtered/ordered results
- Cascades behave as expected
- Transactions roll back on error

---

## Transaction Management

Keep transactions short. Long transactions lock rows and slow down concurrent access.

```python
# Service layer manages the transaction
def transfer_credits(session: Session, from_id: int, to_id: int, amount: int) -> None:
    sender = session.get(User, from_id)
    receiver = session.get(User, to_id)

    if sender is None or receiver is None:
        raise ValueError("User not found")
    if sender.credits < amount:
        raise ValueError("Insufficient credits")

    sender.credits -= amount
    receiver.credits += amount
    # Caller commits — or the context manager does
```

```python
with Session(engine) as session:
    try:
        transfer_credits(session, from_id=1, to_id=2, amount=100)
        session.commit()
    except Exception:
        session.rollback()
        raise
```

---

## Key Takeaways

- Use the repository pattern to separate database access from business logic
- Never hardcode credentials — use environment variables and `.env` files
- Watch for N+1 queries; fix with `selectinload` or `joinedload`
- Use Alembic for migrations in production; never use `create_all` in production
- Use raw SQL for bulk operations and complex analytics
- Test with in-memory SQLite fixtures — fast, isolated, no cleanup
- Keep transactions short and handle commit/rollback at the service layer
