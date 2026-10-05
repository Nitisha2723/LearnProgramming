# SQLAlchemy ORM

The ORM maps Python classes to database tables. You work with Python objects; SQLAlchemy translates them to SQL.

---

## The Declarative Base

In SQLAlchemy 2.x, all ORM models inherit from `DeclarativeBase`:

```python
from sqlalchemy.orm import DeclarativeBase

class Base(DeclarativeBase):
    pass
```

`Base` is the registry — every model class you create as a subclass of `Base` is tracked. You call `Base.metadata.create_all(engine)` to create all tables at once.

---

## Defining Models

```python
from sqlalchemy import String, Integer, ForeignKey, Text
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship

class Base(DeclarativeBase):
    pass

class User(Base):
    __tablename__ = "users"

    id:    Mapped[int] = mapped_column(primary_key=True)
    name:  Mapped[str] = mapped_column(String(100))
    email: Mapped[str] = mapped_column(String(200), unique=True)

    # Relationship: one user → many posts
    posts: Mapped[list["Post"]] = relationship(back_populates="author")

    def __repr__(self) -> str:
        return f"User(id={self.id!r}, name={self.name!r})"


class Post(Base):
    __tablename__ = "posts"

    id:        Mapped[int] = mapped_column(primary_key=True)
    title:     Mapped[str] = mapped_column(String(200))
    content:   Mapped[str] = mapped_column(Text)
    author_id: Mapped[int] = mapped_column(ForeignKey("users.id"))

    # Relationship: many posts → one user
    author: Mapped["User"] = relationship(back_populates="posts")

    def __repr__(self) -> str:
        return f"Post(id={self.id!r}, title={self.title!r})"
```

### `Mapped[T]` and `mapped_column()`

- `Mapped[int]` declares the Python type. SQLAlchemy infers the SQL type (`INTEGER`).
- `Mapped[str]` → `VARCHAR`. Add `mapped_column(String(100))` to set max length.
- `Mapped[str | None]` → nullable column. `Mapped[str]` → NOT NULL.
- `mapped_column(primary_key=True)` sets the primary key.

---

## Creating the Engine and Tables

```python
from sqlalchemy import create_engine

engine = create_engine("sqlite:///myapp.db", echo=True)
Base.metadata.create_all(engine)    # runs CREATE TABLE IF NOT EXISTS for all models
```

---

## The Session

The `Session` is the unit of work. It tracks changes to objects and sends them to the database in a batch:

```python
from sqlalchemy.orm import Session

with Session(engine) as session:
    # work happens here
    pass   # session closes automatically; does NOT auto-commit
```

Use `session.commit()` to persist changes, `session.rollback()` to undo them.

---

## Adding Objects

```python
with Session(engine) as session:
    alice = User(name="Alice", email="alice@example.com")
    session.add(alice)        # track the object
    session.commit()          # INSERT INTO users ...
    session.refresh(alice)    # reload from DB to get auto-generated id

    print(alice.id)           # 1
```

`session.add_all([obj1, obj2, ...])` adds multiple objects at once.

---

## Querying

### `session.get()` — fetch by primary key

```python
user = session.get(User, 1)   # SELECT ... WHERE id = 1
if user is None:
    print("not found")
```

### `session.execute(select(...))` — flexible queries

```python
from sqlalchemy import select

# All users
stmt = select(User)
users = session.execute(stmt).scalars().all()

# Filter
stmt = select(User).where(User.email == "alice@example.com")
user = session.execute(stmt).scalar_one_or_none()

# Order and limit
stmt = select(User).order_by(User.name).limit(10)
users = session.execute(stmt).scalars().all()

# Multiple filters
stmt = select(User).where(User.name.like("A%"), User.id > 0)
```

### `.scalars()` vs `.all()`

`session.execute(stmt)` returns a `Result` of rows. For ORM queries:
- `.scalars()` → extract the first column (the model object)
- `.scalars().all()` → list of model objects
- `.scalar_one()` → exactly one, raises if zero or multiple
- `.scalar_one_or_none()` → one or None, raises if multiple

---

## Updating Objects

```python
with Session(engine) as session:
    user = session.get(User, 1)
    user.email = "newalice@example.com"   # just set the attribute
    session.commit()                       # UPDATE users SET email = ... WHERE id = 1
```

SQLAlchemy tracks attribute changes automatically. No explicit "update" call needed.

---

## Deleting Objects

```python
with Session(engine) as session:
    user = session.get(User, 1)
    if user:
        session.delete(user)
        session.commit()    # DELETE FROM users WHERE id = 1
```

---

## Relationships

Once you have `relationship()` defined, you can navigate between objects:

```python
with Session(engine) as session:
    alice = User(name="Alice", email="alice@example.com")
    post  = Post(title="Hello World", content="My first post.", author=alice)

    session.add(post)   # adding the post also adds alice (cascade)
    session.commit()

    # Navigate the relationship
    print(alice.posts)   # [Post(id=1, title='Hello World')]
    print(post.author)   # User(id=1, name='Alice')
```

---

## Lazy vs Eager Loading

By default, relationships are **lazy loaded**: accessing `alice.posts` triggers a second SELECT query.

```python
with Session(engine) as session:
    alice = session.get(User, 1)
    # No query for posts yet
    print(alice.posts)   # ← SELECT FROM posts WHERE user_id = 1 runs HERE
```

This is fine for simple cases, but causes the **N+1 problem** when loading many users (see `04-best-practices.md`).

**Eager loading** fetches related objects in the same query:

```python
from sqlalchemy.orm import selectinload

stmt = select(User).options(selectinload(User.posts))
users = session.execute(stmt).scalars().all()
# Two queries total: one for users, one for all their posts
# NOT one query per user
```

`selectinload` is the safest eager-load strategy for collections. Use `joinedload` for single-object relationships (many-to-one, one-to-one).

---

## Querying with Joins

```python
from sqlalchemy import select

# Explicit join
stmt = (
    select(User, Post)
    .join(Post, User.id == Post.author_id)
    .where(User.name == "Alice")
)
for user, post in session.execute(stmt):
    print(user.name, post.title)

# Filter users who have posts (semi-join)
stmt = select(User).where(User.posts.any())
```

---

## Cascade Operations

Control what happens to related objects when the parent is deleted:

```python
class User(Base):
    ...
    posts: Mapped[list["Post"]] = relationship(
        back_populates="author",
        cascade="all, delete-orphan",   # delete posts when user is deleted
    )
```

Without cascade, deleting a user with posts raises a foreign key constraint error.

---

## session.flush() vs session.commit()

- `flush()` — sends pending SQL to the database within the **current transaction** but doesn't commit. Useful when you need the auto-generated `id` before committing.
- `commit()` — flushes and commits. The transaction is finalized.

```python
with Session(engine) as session:
    user = User(name="Dave", email="dave@example.com")
    session.add(user)
    session.flush()        # INSERT runs; user.id is now populated
    print(user.id)         # 4 — available before commit
    session.commit()
```

---

## expire_on_commit

After `commit()`, all tracked objects are **expired** by default. The next attribute access triggers a new SELECT. This keeps data fresh but adds queries.

```python
with Session(engine) as session:
    user = session.get(User, 1)
    session.commit()
    # user is now expired
    print(user.name)   # SELECT runs here to refresh
```

If you return objects from a function after closing the session, access them before closing or use `session.expunge(user)` / `make_transient(user)`.

---

## Key Takeaways

- Inherit from `DeclarativeBase` for all models
- `Mapped[T]` + `mapped_column()` is the modern (2.x) way to declare columns
- `Mapped[str | None]` = nullable, `Mapped[str]` = NOT NULL
- `Session` is the unit of work — add objects, commit to persist
- `session.get(Model, pk)` is the fastest way to fetch by primary key
- Use `selectinload` to avoid N+1 queries on collections
- `session.commit()` triggers the actual INSERT/UPDATE/DELETE
- Objects are expired after commit — access attributes before closing the session
