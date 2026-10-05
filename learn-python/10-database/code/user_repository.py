"""
user_repository.py — Repository pattern with SQLAlchemy ORM.

Requires: pip install sqlalchemy

The repository wraps all database access for User objects behind a clean
Python interface. The rest of the application never touches SQLAlchemy
directly — it calls the repository. This makes business logic easy to test
(mock the repository) and makes swapping databases transparent.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Optional

from sqlalchemy import String, Integer, ForeignKey, Text, create_engine, select
from sqlalchemy.orm import (
    DeclarativeBase,
    Mapped,
    Session,
    mapped_column,
    relationship,
    selectinload,
)


# --------------------------------------------------------------------------- #
#  Models (same as sqlalchemy_orm.py)                                          #
# --------------------------------------------------------------------------- #


class Base(DeclarativeBase):
    pass


class User(Base):
    __tablename__ = "users"

    id:    Mapped[int] = mapped_column(Integer, primary_key=True)
    name:  Mapped[str] = mapped_column(String(100), nullable=False)
    email: Mapped[str] = mapped_column(String(200), nullable=False, unique=True)

    posts: Mapped[list[Post]] = relationship(
        "Post",
        back_populates="author",
        cascade="all, delete-orphan",
    )

    def __repr__(self) -> str:
        return f"User(id={self.id!r}, name={self.name!r}, email={self.email!r})"


class Post(Base):
    __tablename__ = "posts"

    id:        Mapped[int] = mapped_column(Integer, primary_key=True)
    title:     Mapped[str] = mapped_column(String(200), nullable=False)
    content:   Mapped[str] = mapped_column(Text, nullable=False, default="")
    author_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id"), nullable=False)

    author: Mapped[User] = relationship("User", back_populates="posts")

    def __repr__(self) -> str:
        return f"Post(id={self.id!r}, title={self.title!r})"


# --------------------------------------------------------------------------- #
#  UserRepository                                                               #
# --------------------------------------------------------------------------- #


@dataclass
class UserRepository:
    """
    Repository for User persistence.

    The session is injected — the repository does NOT manage transactions.
    Call session.commit() / session.rollback() from the service layer or
    the code that creates the session.

    Example:
        with Session(engine) as session:
            repo = UserRepository(session)
            user = repo.find_by_email("alice@example.com")
            if user:
                user.name = "Alice Smith"
            session.commit()
    """

    session: Session

    # ── Reads ──────────────────────────────────────────────────────────── #

    def find_by_id(self, user_id: int) -> Optional[User]:
        """Return the user with the given primary key, or None."""
        return self.session.get(User, user_id)

    def find_by_email(self, email: str) -> Optional[User]:
        """Return the user with the given email, or None."""
        stmt = select(User).where(User.email == email)
        return self.session.execute(stmt).scalar_one_or_none()

    def find_all(self) -> list[User]:
        """Return all users ordered alphabetically by name."""
        stmt = select(User).order_by(User.name)
        return self.session.execute(stmt).scalars().all()

    def find_all_with_posts(self) -> list[User]:
        """
        Return all users with their posts eagerly loaded.

        Uses selectinload to avoid the N+1 problem: one query for users,
        one IN-query for all posts — regardless of how many users exist.
        """
        stmt = (
            select(User)
            .options(selectinload(User.posts))
            .order_by(User.name)
        )
        return self.session.execute(stmt).scalars().all()

    def search_by_name(self, fragment: str) -> list[User]:
        """Return users whose name contains 'fragment' (case-insensitive)."""
        stmt = select(User).where(User.name.ilike(f"%{fragment}%")).order_by(User.name)
        return self.session.execute(stmt).scalars().all()

    def count(self) -> int:
        """Return the total number of users."""
        from sqlalchemy import func
        result = self.session.execute(select(func.count()).select_from(User))
        return result.scalar_one()

    def exists(self, user_id: int) -> bool:
        """Return True if a user with the given id exists."""
        return self.find_by_id(user_id) is not None

    # ── Writes ─────────────────────────────────────────────────────────── #

    def save(self, user: User) -> User:
        """
        Add a new user or merge an existing one into the session.

        For new users, flushes to populate user.id before returning.
        The caller must commit() to persist the change.
        """
        self.session.add(user)
        self.session.flush()
        return user

    def delete(self, user_id: int) -> bool:
        """
        Delete a user by id (and their posts via cascade).

        Returns True if the user was found and marked for deletion,
        False if no user with that id exists.
        The caller must commit() to finalize the deletion.
        """
        user = self.find_by_id(user_id)
        if user is None:
            return False
        self.session.delete(user)
        return True


# --------------------------------------------------------------------------- #
#  PostRepository                                                               #
# --------------------------------------------------------------------------- #


@dataclass
class PostRepository:
    """Repository for Post persistence."""

    session: Session

    def find_by_id(self, post_id: int) -> Optional[Post]:
        return self.session.get(Post, post_id)

    def find_by_author(self, user_id: int) -> list[Post]:
        stmt = select(Post).where(Post.author_id == user_id).order_by(Post.id)
        return self.session.execute(stmt).scalars().all()

    def find_all(self) -> list[Post]:
        stmt = select(Post).order_by(Post.id)
        return self.session.execute(stmt).scalars().all()

    def save(self, post: Post) -> Post:
        self.session.add(post)
        self.session.flush()
        return post

    def delete(self, post_id: int) -> bool:
        post = self.find_by_id(post_id)
        if post is None:
            return False
        self.session.delete(post)
        return True


# --------------------------------------------------------------------------- #
#  Demo                                                                         #
# --------------------------------------------------------------------------- #


def _sep(title: str) -> None:
    print(f"\n{'─' * 55}")
    print(f"  {title}")
    print("─" * 55)


def main() -> None:
    print("Repository Pattern Demo — in-memory SQLite")
    print("=" * 55)

    engine = create_engine("sqlite:///:memory:", echo=False)
    Base.metadata.create_all(engine)
    print("Tables created.")

    with Session(engine) as session:
        user_repo = UserRepository(session)
        post_repo = PostRepository(session)

        # ── Create users ──────────────────────────────────────
        _sep("Create users via repository")
        alice = user_repo.save(User(name="Alice", email="alice@example.com"))
        bob   = user_repo.save(User(name="Bob",   email="bob@example.com"))
        carol = user_repo.save(User(name="Carol", email="carol@example.com"))
        session.commit()
        print(f"Saved {user_repo.count()} users: alice.id={alice.id}, bob.id={bob.id}")

        # ── Find by id ────────────────────────────────────────
        _sep("find_by_id")
        found = user_repo.find_by_id(alice.id)
        print(f"Found: {found}")

        # ── Find by email ─────────────────────────────────────
        _sep("find_by_email")
        found = user_repo.find_by_email("bob@example.com")
        print(f"Found: {found}")

        not_found = user_repo.find_by_email("ghost@example.com")
        print(f"Not found: {not_found}")

        # ── Search by name ────────────────────────────────────
        _sep("search_by_name (fragment)")
        results = user_repo.search_by_name("a")  # alice, carol
        print(f"Names containing 'a': {[u.name for u in results]}")

        # ── Find all ──────────────────────────────────────────
        _sep("find_all (alphabetical)")
        for u in user_repo.find_all():
            print(f"  {u}")

        # ── Add posts ─────────────────────────────────────────
        _sep("Add posts and find_all_with_posts")
        post_repo.save(Post(title="Hello", content="Alice's first post.", author=alice))
        post_repo.save(Post(title="Tips",  content="Use selectinload!",   author=alice))
        post_repo.save(Post(title="Hi",    content="Bob here.",           author=bob))
        session.commit()

        users_with_posts = user_repo.find_all_with_posts()
        for u in users_with_posts:
            print(f"  {u.name}: {len(u.posts)} post(s)")

        # ── Delete user ───────────────────────────────────────
        _sep("delete user (cascade)")
        all_posts_before = post_repo.find_all()
        print(f"Posts before: {len(all_posts_before)}")

        deleted = user_repo.delete(alice.id)
        session.commit()
        print(f"Alice deleted: {deleted}")

        all_posts_after = post_repo.find_all()
        print(f"Posts after: {len(all_posts_after)}")
        print(f"Remaining post titles: {[p.title for p in all_posts_after]}")

        # ── Delete non-existent ──────────────────────────────
        _sep("delete non-existent user")
        ok = user_repo.delete(9999)
        print(f"Delete id=9999: {ok}")  # False

    print("\nSession closed. All done.")


if __name__ == "__main__":
    main()
