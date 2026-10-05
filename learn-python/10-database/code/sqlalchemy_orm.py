"""
sqlalchemy_orm.py — SQLAlchemy 2.x ORM demonstration.

Requires: pip install sqlalchemy

Uses an in-memory SQLite database — runs without any setup.

Topics covered:
- DeclarativeBase
- Mapped / mapped_column (SQLAlchemy 2.x style)
- One-to-many relationship with back_populates
- Session CRUD: add, get, execute(select(...)), delete
- Eager loading with selectinload (avoids N+1)
- Query with join and filter
"""

from __future__ import annotations

from sqlalchemy import (
    ForeignKey,
    Integer,
    String,
    Text,
    create_engine,
    select,
)
from sqlalchemy.orm import (
    DeclarativeBase,
    Mapped,
    Session,
    mapped_column,
    relationship,
    selectinload,
)

# --------------------------------------------------------------------------- #
#  Engine                                                                       #
# --------------------------------------------------------------------------- #

engine = create_engine(
    "sqlite:///:memory:",
    echo=False,  # set True to see generated SQL
)

# --------------------------------------------------------------------------- #
#  Models                                                                       #
# --------------------------------------------------------------------------- #


class Base(DeclarativeBase):
    """Shared declarative base — all models inherit from this."""
    pass


class User(Base):
    """A registered user in the system."""

    __tablename__ = "users"

    id:    Mapped[int] = mapped_column(Integer, primary_key=True)
    name:  Mapped[str] = mapped_column(String(100), nullable=False)
    email: Mapped[str] = mapped_column(String(200), nullable=False, unique=True)

    # One user can have many posts
    posts: Mapped[list[Post]] = relationship(
        "Post",
        back_populates="author",
        cascade="all, delete-orphan",  # deleting a user deletes their posts
    )

    def __repr__(self) -> str:
        return f"User(id={self.id!r}, name={self.name!r}, email={self.email!r})"


class Post(Base):
    """A post authored by a user."""

    __tablename__ = "posts"

    id:        Mapped[int] = mapped_column(Integer, primary_key=True)
    title:     Mapped[str] = mapped_column(String(200), nullable=False)
    content:   Mapped[str] = mapped_column(Text, nullable=False, default="")
    author_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id"), nullable=False)

    # Many posts belong to one user
    author: Mapped[User] = relationship("User", back_populates="posts")

    def __repr__(self) -> str:
        return f"Post(id={self.id!r}, title={self.title!r}, author_id={self.author_id!r})"


# --------------------------------------------------------------------------- #
#  CRUD helpers                                                                 #
# --------------------------------------------------------------------------- #


def create_user(session: Session, name: str, email: str) -> User:
    """Create, persist (flush), and return a new User."""
    user = User(name=name, email=email)
    session.add(user)
    session.flush()  # assigns user.id without committing
    return user


def get_user_by_id(session: Session, user_id: int) -> User | None:
    """Fetch a user by primary key. Returns None if not found."""
    return session.get(User, user_id)


def get_user_by_email(session: Session, email: str) -> User | None:
    """Fetch a user by email address."""
    stmt = select(User).where(User.email == email)
    return session.execute(stmt).scalar_one_or_none()


def get_all_users(session: Session) -> list[User]:
    """Return all users ordered by name."""
    stmt = select(User).order_by(User.name)
    return session.execute(stmt).scalars().all()


def update_user_email(session: Session, user_id: int, new_email: str) -> bool:
    """Update a user's email. Returns True if found."""
    user = session.get(User, user_id)
    if user is None:
        return False
    user.email = new_email
    return True


def delete_user(session: Session, user_id: int) -> bool:
    """Delete a user (and their posts via cascade). Returns True if found."""
    user = session.get(User, user_id)
    if user is None:
        return False
    session.delete(user)
    return True


def create_post(session: Session, author: User, title: str, content: str = "") -> Post:
    """Create a post for a user and return it."""
    post = Post(title=title, content=content, author=author)
    session.add(post)
    session.flush()
    return post


def get_users_with_posts(session: Session) -> list[User]:
    """
    Return all users with their posts eagerly loaded.

    Without selectinload, accessing user.posts would fire one SELECT
    per user (N+1 problem). With selectinload, SQLAlchemy runs exactly
    two queries: one for users, one for all their posts using IN (...).
    """
    stmt = select(User).options(selectinload(User.posts)).order_by(User.name)
    return session.execute(stmt).scalars().all()


def find_posts_by_author_name(session: Session, author_name: str) -> list[Post]:
    """Return posts whose author's name matches exactly."""
    stmt = (
        select(Post)
        .join(Post.author)
        .where(User.name == author_name)
        .order_by(Post.id)
    )
    return session.execute(stmt).scalars().all()


# --------------------------------------------------------------------------- #
#  Demo                                                                         #
# --------------------------------------------------------------------------- #


def _sep(title: str) -> None:
    print(f"\n{'─' * 55}")
    print(f"  {title}")
    print("─" * 55)


def main() -> None:
    print("SQLAlchemy ORM Demo — in-memory SQLite")
    print("=" * 55)

    # Create all tables
    Base.metadata.create_all(engine)
    print("Tables created: users, posts")

    with Session(engine) as session:

        # ── Create users ────────────────────────────────────
        _sep("CREATE users")
        alice = create_user(session, "Alice", "alice@example.com")
        bob   = create_user(session, "Bob",   "bob@example.com")
        carol = create_user(session, "Carol", "carol@example.com")
        session.commit()
        print(f"Saved: {alice}")
        print(f"Saved: {bob}")
        print(f"Saved: {carol}")

        # ── Read by id ──────────────────────────────────────
        _sep("READ by primary key")
        found = get_user_by_id(session, alice.id)
        print(f"get(User, {alice.id}) → {found}")

        # ── Read by email ───────────────────────────────────
        _sep("READ by email")
        found_by_email = get_user_by_email(session, "bob@example.com")
        print(f"find by email → {found_by_email}")

        # ── Read all ─────────────────────────────────────────
        _sep("READ all users")
        for u in get_all_users(session):
            print(f"  {u}")

        # ── Update ──────────────────────────────────────────
        _sep("UPDATE email")
        ok = update_user_email(session, alice.id, "alice.updated@example.com")
        session.commit()
        refreshed = get_user_by_id(session, alice.id)
        print(f"Updated: {ok} → new email: {refreshed.email}")  # type: ignore[union-attr]

        # ── Create posts ─────────────────────────────────────
        _sep("CREATE posts (one-to-many)")
        p1 = create_post(session, alice, "Hello World", "My first post.")
        p2 = create_post(session, alice, "SQLAlchemy Tips", "Always use selectinload.")
        p3 = create_post(session, bob,   "Bob's Thoughts", "Hello from Bob!")
        session.commit()
        print(f"Created: {p1}")
        print(f"Created: {p2}")
        print(f"Created: {p3}")

        # ── Relationship traversal ───────────────────────────
        _sep("RELATIONSHIP — navigate from user to posts")
        # Direct access triggers lazy loading (one query per user if in a loop)
        alice_fresh = get_user_by_id(session, alice.id)
        print(f"Alice's posts (lazy): {alice_fresh.posts}")  # type: ignore[union-attr]

        # ── Eager loading — avoids N+1 ───────────────────────
        _sep("EAGER LOADING with selectinload (no N+1)")
        users_with_posts = get_users_with_posts(session)
        for u in users_with_posts:
            print(f"  {u.name}: {len(u.posts)} post(s)")
            for p in u.posts:
                print(f"    - '{p.title}'")

        # ── Join query ───────────────────────────────────────
        _sep("JOIN query — posts by author name")
        alice_posts = find_posts_by_author_name(session, "Alice")
        print(f"Posts by Alice: {[p.title for p in alice_posts]}")

        # ── Delete with cascade ─────────────────────────────
        _sep("DELETE user (cascades to posts)")
        posts_before = session.execute(select(Post)).scalars().all()
        print(f"Posts before delete: {len(posts_before)}")

        ok = delete_user(session, alice.id)
        session.commit()
        print(f"Deleted Alice: {ok}")

        posts_after = session.execute(select(Post)).scalars().all()
        print(f"Posts after delete: {len(posts_after)}")
        print(f"Remaining post titles: {[p.title for p in posts_after]}")

        # ── Missing user ─────────────────────────────────────
        _sep("READ non-existent user")
        missing = get_user_by_id(session, 9999)
        print(f"User 9999: {missing}")  # None

    print("\nSession closed. All done.")


if __name__ == "__main__":
    main()
