"""
test_database.py — pytest test suite for the 10-database module.

Requires: pip install sqlalchemy pytest

Covers:
    - sqlite3_basics.py  — all CRUD functions, row_factory, edge cases
    - sqlalchemy_orm.py  — User/Post models, CRUD, relationships, eager loading
    - user_repository.py — UserRepository, PostRepository
    - library_db.py      — borrow_book, return_book, find_overdue_loans

Run with:
    pytest tests/test_database.py -v
    pytest tests/test_database.py -v --tb=short   (compact tracebacks)
"""

from __future__ import annotations

import sqlite3
import sys
from contextlib import contextmanager
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Generator

import pytest

# ── Path setup ──────────────────────────────────────────────────────────────
# Make the code/ directory importable when running from any location.
_CODE_DIR = Path(__file__).parent.parent / "code"
_MINI_DIR = Path(__file__).parent.parent / "mini-project"
sys.path.insert(0, str(_CODE_DIR))
sys.path.insert(0, str(_MINI_DIR))

# ── SQLAlchemy imports ───────────────────────────────────────────────────────
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session

# ── Module imports ───────────────────────────────────────────────────────────
import sqlite3_basics as sq3
import sqlalchemy_orm as orm_module
import user_repository as repo_module
import library_db as lib


# ============================================================================ #
#  Fixtures                                                                     #
# ============================================================================ #


@contextmanager
def _memory_conn() -> Generator[sqlite3.Connection, None, None]:
    """In-memory sqlite3 connection with row_factory, auto-commit/rollback."""
    conn = sqlite3.connect(":memory:")
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


@pytest.fixture()
def sq3_conn() -> Generator[sqlite3.Connection, None, None]:
    """Provide a fresh in-memory sqlite3 connection with the schema set up."""
    with _memory_conn() as conn:
        sq3.create_tables(conn)
        yield conn


# ── SQLAlchemy fixtures ──────────────────────────────────────────────────────

@pytest.fixture()
def orm_engine():
    """In-memory SQLite engine with ORM schema."""
    eng = create_engine("sqlite:///:memory:", echo=False)
    orm_module.Base.metadata.create_all(eng)
    yield eng
    orm_module.Base.metadata.drop_all(eng)


@pytest.fixture()
def orm_session(orm_engine):
    """SQLAlchemy Session bound to the ORM engine."""
    with Session(orm_engine) as s:
        yield s


# ── Repository fixtures ──────────────────────────────────────────────────────

@pytest.fixture()
def repo_engine():
    eng = create_engine("sqlite:///:memory:", echo=False)
    repo_module.Base.metadata.create_all(eng)
    yield eng
    repo_module.Base.metadata.drop_all(eng)


@pytest.fixture()
def repo_session(repo_engine):
    with Session(repo_engine) as s:
        yield s


# ── Library fixtures ─────────────────────────────────────────────────────────

@pytest.fixture()
def lib_engine():
    eng = create_engine("sqlite:///:memory:", echo=False)
    lib.Base.metadata.create_all(eng)
    yield eng
    lib.Base.metadata.drop_all(eng)


@pytest.fixture()
def lib_session(lib_engine):
    with Session(lib_engine) as s:
        yield s


# ============================================================================ #
#  sqlite3_basics tests                                                         #
# ============================================================================ #


class TestSqlite3Basics:

    def test_insert_user_returns_int(self, sq3_conn):
        uid = sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        assert isinstance(uid, int)
        assert uid >= 1

    def test_insert_user_unique_ids(self, sq3_conn):
        id1 = sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        id2 = sq3.insert_user(sq3_conn, "Bob",   "bob@example.com")
        assert id1 != id2

    def test_find_all_users_empty(self, sq3_conn):
        rows = sq3.find_all_users(sq3_conn)
        assert rows == []

    def test_find_all_users_ordered_by_name(self, sq3_conn):
        sq3.insert_user(sq3_conn, "Zara", "zara@example.com")
        sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        sq3.insert_user(sq3_conn, "Mark", "mark@example.com")
        rows = sq3.find_all_users(sq3_conn)
        names = [r["name"] for r in rows]
        assert names == sorted(names)

    def test_find_user_by_id(self, sq3_conn):
        uid = sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        row = sq3.find_user_by_id(sq3_conn, uid)
        assert row is not None
        assert row["name"] == "Alice"
        assert row["email"] == "alice@example.com"

    def test_find_user_by_id_not_found(self, sq3_conn):
        row = sq3.find_user_by_id(sq3_conn, 9999)
        assert row is None

    def test_find_users_by_name_fragment(self, sq3_conn):
        sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        sq3.insert_user(sq3_conn, "Alexander", "alex@example.com")
        sq3.insert_user(sq3_conn, "Bob", "bob@example.com")
        results = sq3.find_users_by_name(sq3_conn, "al")
        names = [r["name"] for r in results]
        assert "Alice" in names
        assert "Alexander" in names
        assert "Bob" not in names

    def test_update_email_success(self, sq3_conn):
        uid = sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        ok = sq3.update_email(sq3_conn, uid, "new@example.com")
        assert ok is True
        row = sq3.find_user_by_id(sq3_conn, uid)
        assert row["email"] == "new@example.com"

    def test_update_email_not_found(self, sq3_conn):
        ok = sq3.update_email(sq3_conn, 9999, "ghost@example.com")
        assert ok is False

    def test_delete_user_success(self, sq3_conn):
        uid = sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        ok = sq3.delete_user(sq3_conn, uid)
        assert ok is True
        assert sq3.find_user_by_id(sq3_conn, uid) is None

    def test_delete_user_not_found(self, sq3_conn):
        ok = sq3.delete_user(sq3_conn, 9999)
        assert ok is False

    def test_bulk_insert(self, sq3_conn):
        data = [
            ("Alice", "alice@example.com"),
            ("Bob",   "bob@example.com"),
            ("Carol", "carol@example.com"),
        ]
        sq3.insert_users_bulk(sq3_conn, data)
        rows = sq3.find_all_users(sq3_conn)
        assert len(rows) == 3

    def test_count_users(self, sq3_conn):
        assert sq3.count_users(sq3_conn) == 0
        sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        sq3.insert_user(sq3_conn, "Bob",   "bob@example.com")
        assert sq3.count_users(sq3_conn) == 2

    def test_row_factory_named_access(self, sq3_conn):
        uid = sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        row = sq3.find_user_by_id(sq3_conn, uid)
        # Row factory allows dict-like access by column name
        assert row["name"] == "Alice"
        assert row["email"] == "alice@example.com"
        assert row["id"] == uid

    def test_insert_post_and_find(self, sq3_conn):
        uid = sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        pid = sq3.insert_post(sq3_conn, uid, "Hello", "Body text")
        posts = sq3.find_posts_by_user(sq3_conn, uid)
        assert len(posts) == 1
        assert posts[0]["title"] == "Hello"
        assert posts[0]["id"] == pid

    def test_posts_with_authors_join(self, sq3_conn):
        uid = sq3.insert_user(sq3_conn, "Alice", "alice@example.com")
        sq3.insert_post(sq3_conn, uid, "Post 1", "")
        sq3.insert_post(sq3_conn, uid, "Post 2", "")
        rows = sq3.find_posts_with_authors(sq3_conn)
        assert len(rows) == 2
        assert all(r["author_name"] == "Alice" for r in rows)


# ============================================================================ #
#  SQLAlchemy ORM tests                                                         #
# ============================================================================ #


class TestSqlAlchemyORM:

    def test_create_user(self, orm_session):
        user = orm_module.create_user(orm_session, "Alice", "alice@example.com")
        orm_session.commit()
        assert user.id is not None
        assert user.name == "Alice"

    def test_get_user_by_id(self, orm_session):
        user = orm_module.create_user(orm_session, "Alice", "alice@example.com")
        orm_session.commit()
        found = orm_module.get_user_by_id(orm_session, user.id)
        assert found is not None
        assert found.name == "Alice"

    def test_get_user_by_id_not_found(self, orm_session):
        assert orm_module.get_user_by_id(orm_session, 9999) is None

    def test_get_user_by_email(self, orm_session):
        orm_module.create_user(orm_session, "Alice", "alice@example.com")
        orm_session.commit()
        found = orm_module.get_user_by_email(orm_session, "alice@example.com")
        assert found is not None
        assert found.name == "Alice"

    def test_get_user_by_email_not_found(self, orm_session):
        assert orm_module.get_user_by_email(orm_session, "ghost@example.com") is None

    def test_update_user_email(self, orm_session):
        user = orm_module.create_user(orm_session, "Alice", "alice@example.com")
        orm_session.commit()
        ok = orm_module.update_user_email(orm_session, user.id, "updated@example.com")
        orm_session.commit()
        assert ok is True
        refreshed = orm_module.get_user_by_id(orm_session, user.id)
        assert refreshed.email == "updated@example.com"

    def test_update_user_email_not_found(self, orm_session):
        ok = orm_module.update_user_email(orm_session, 9999, "x@example.com")
        assert ok is False

    def test_delete_user(self, orm_session):
        user = orm_module.create_user(orm_session, "Alice", "alice@example.com")
        orm_session.commit()
        ok = orm_module.delete_user(orm_session, user.id)
        orm_session.commit()
        assert ok is True
        assert orm_module.get_user_by_id(orm_session, user.id) is None

    def test_delete_user_not_found(self, orm_session):
        ok = orm_module.delete_user(orm_session, 9999)
        assert ok is False

    def test_get_all_users_ordered(self, orm_session):
        orm_module.create_user(orm_session, "Zara",  "zara@example.com")
        orm_module.create_user(orm_session, "Alice", "alice@example.com")
        orm_module.create_user(orm_session, "Bob",   "bob@example.com")
        orm_session.commit()
        users = orm_module.get_all_users(orm_session)
        names = [u.name for u in users]
        assert names == sorted(names)

    def test_user_post_relationship(self, orm_session):
        user = orm_module.create_user(orm_session, "Alice", "alice@example.com")
        p1 = orm_module.create_post(orm_session, user, "Post 1", "Content 1")
        p2 = orm_module.create_post(orm_session, user, "Post 2", "Content 2")
        orm_session.commit()

        fetched = orm_module.get_user_by_id(orm_session, user.id)
        assert len(fetched.posts) == 2
        post_titles = {p.title for p in fetched.posts}
        assert "Post 1" in post_titles
        assert "Post 2" in post_titles

    def test_cascade_delete_removes_posts(self, orm_session):
        user = orm_module.create_user(orm_session, "Alice", "alice@example.com")
        orm_module.create_post(orm_session, user, "Post 1", "")
        orm_module.create_post(orm_session, user, "Post 2", "")
        orm_session.commit()

        orm_module.delete_user(orm_session, user.id)
        orm_session.commit()

        posts = orm_session.execute(select(orm_module.Post)).scalars().all()
        assert len(posts) == 0

    def test_get_users_with_posts_no_n1(self, orm_session):
        alice = orm_module.create_user(orm_session, "Alice", "alice@example.com")
        bob   = orm_module.create_user(orm_session, "Bob",   "bob@example.com")
        orm_module.create_post(orm_session, alice, "A1", "")
        orm_module.create_post(orm_session, alice, "A2", "")
        orm_module.create_post(orm_session, bob,   "B1", "")
        orm_session.commit()

        users = orm_module.get_users_with_posts(orm_session)
        # Find Alice and Bob by name (order may vary based on sort)
        alice_u = next(u for u in users if u.name == "Alice")
        bob_u   = next(u for u in users if u.name == "Bob")
        assert len(alice_u.posts) == 2
        assert len(bob_u.posts) == 1

    def test_find_posts_by_author_name(self, orm_session):
        alice = orm_module.create_user(orm_session, "Alice", "alice@example.com")
        bob   = orm_module.create_user(orm_session, "Bob",   "bob@example.com")
        orm_module.create_post(orm_session, alice, "Alice Post", "")
        orm_module.create_post(orm_session, bob,   "Bob Post",   "")
        orm_session.commit()

        alice_posts = orm_module.find_posts_by_author_name(orm_session, "Alice")
        assert len(alice_posts) == 1
        assert alice_posts[0].title == "Alice Post"


# ============================================================================ #
#  UserRepository tests                                                         #
# ============================================================================ #


class TestUserRepository:

    def test_save_and_find_by_id(self, repo_session):
        repo = repo_module.UserRepository(repo_session)
        user = repo.save(repo_module.User(name="Alice", email="alice@example.com"))
        repo_session.commit()
        found = repo.find_by_id(user.id)
        assert found is not None
        assert found.name == "Alice"

    def test_find_by_id_not_found(self, repo_session):
        repo = repo_module.UserRepository(repo_session)
        assert repo.find_by_id(9999) is None

    def test_find_by_email(self, repo_session):
        repo = repo_module.UserRepository(repo_session)
        repo.save(repo_module.User(name="Alice", email="alice@example.com"))
        repo_session.commit()
        found = repo.find_by_email("alice@example.com")
        assert found is not None
        assert found.name == "Alice"

    def test_find_by_email_not_found(self, repo_session):
        repo = repo_module.UserRepository(repo_session)
        assert repo.find_by_email("ghost@example.com") is None

    def test_find_all(self, repo_session):
        repo = repo_module.UserRepository(repo_session)
        repo.save(repo_module.User(name="Zara",  email="zara@example.com"))
        repo.save(repo_module.User(name="Alice", email="alice@example.com"))
        repo_session.commit()
        users = repo.find_all()
        names = [u.name for u in users]
        assert names == sorted(names)

    def test_search_by_name(self, repo_session):
        repo = repo_module.UserRepository(repo_session)
        repo.save(repo_module.User(name="Alice",     email="alice@example.com"))
        repo.save(repo_module.User(name="Alexander", email="alex@example.com"))
        repo.save(repo_module.User(name="Bob",       email="bob@example.com"))
        repo_session.commit()
        results = repo.search_by_name("al")
        names = {u.name for u in results}
        assert "Alice" in names
        assert "Alexander" in names
        assert "Bob" not in names

    def test_count(self, repo_session):
        repo = repo_module.UserRepository(repo_session)
        assert repo.count() == 0
        repo.save(repo_module.User(name="Alice", email="alice@example.com"))
        repo.save(repo_module.User(name="Bob",   email="bob@example.com"))
        repo_session.commit()
        assert repo.count() == 2

    def test_exists(self, repo_session):
        repo = repo_module.UserRepository(repo_session)
        user = repo.save(repo_module.User(name="Alice", email="alice@example.com"))
        repo_session.commit()
        assert repo.exists(user.id) is True
        assert repo.exists(9999) is False

    def test_delete(self, repo_session):
        repo = repo_module.UserRepository(repo_session)
        user = repo.save(repo_module.User(name="Alice", email="alice@example.com"))
        repo_session.commit()
        ok = repo.delete(user.id)
        repo_session.commit()
        assert ok is True
        assert repo.find_by_id(user.id) is None

    def test_delete_not_found(self, repo_session):
        repo = repo_module.UserRepository(repo_session)
        assert repo.delete(9999) is False

    def test_find_all_with_posts(self, repo_session):
        user_repo = repo_module.UserRepository(repo_session)
        post_repo = repo_module.PostRepository(repo_session)

        alice = user_repo.save(repo_module.User(name="Alice", email="alice@example.com"))
        post_repo.save(repo_module.Post(title="Post 1", content="", author=alice))
        post_repo.save(repo_module.Post(title="Post 2", content="", author=alice))
        repo_session.commit()

        users = user_repo.find_all_with_posts()
        alice_u = next(u for u in users if u.name == "Alice")
        assert len(alice_u.posts) == 2


# ============================================================================ #
#  Library mini-project tests                                                   #
# ============================================================================ #


class TestLibraryDB:

    def _make_book(self, session, title="Test Book", copies=2):
        import random, string
        isbn = "".join(random.choices(string.digits, k=13))
        book = lib.Book(
            title=title, author="Author", isbn=isbn,
            total_copies=copies, available_copies=copies,
        )
        session.add(book)
        session.flush()
        return book

    def _make_member(self, session, name="Alice"):
        import random, string
        email = f"{name.lower()}_{random.randint(1000,9999)}@test.com"
        num   = "M" + "".join(random.choices(string.digits, k=7))
        member = lib.Member(name=name, email=email, membership_number=num)
        session.add(member)
        session.flush()
        return member

    def test_borrow_book_creates_loan(self, lib_session):
        book   = self._make_book(lib_session)
        member = self._make_member(lib_session)
        lib_session.commit()

        loan = lib.borrow_book(lib_session, member.id, book.id)
        lib_session.commit()

        assert loan.id is not None
        assert loan.member_id == member.id
        assert loan.book_id   == book.id
        assert loan.returned_at is None

    def test_borrow_book_decrements_available(self, lib_session):
        book   = self._make_book(lib_session, copies=3)
        member = self._make_member(lib_session)
        lib_session.commit()

        lib.borrow_book(lib_session, member.id, book.id)
        lib_session.commit()
        lib_session.refresh(book)

        assert book.available_copies == 2

    def test_borrow_book_unavailable_raises(self, lib_session):
        book   = self._make_book(lib_session, copies=1)
        alice  = self._make_member(lib_session, "Alice")
        bob    = self._make_member(lib_session, "Bob")
        lib_session.commit()

        lib.borrow_book(lib_session, alice.id, book.id)
        lib_session.commit()

        with pytest.raises(lib.LibraryError):
            lib.borrow_book(lib_session, bob.id, book.id)

    def test_borrow_same_book_twice_raises(self, lib_session):
        book   = self._make_book(lib_session, copies=3)
        member = self._make_member(lib_session)
        lib_session.commit()

        lib.borrow_book(lib_session, member.id, book.id)
        lib_session.commit()

        with pytest.raises(lib.LibraryError):
            lib.borrow_book(lib_session, member.id, book.id)

    def test_borrow_nonexistent_member_raises(self, lib_session):
        book = self._make_book(lib_session)
        lib_session.commit()
        with pytest.raises(lib.LibraryError):
            lib.borrow_book(lib_session, 9999, book.id)

    def test_borrow_nonexistent_book_raises(self, lib_session):
        member = self._make_member(lib_session)
        lib_session.commit()
        with pytest.raises(lib.LibraryError):
            lib.borrow_book(lib_session, member.id, 9999)

    def test_return_book(self, lib_session):
        book   = self._make_book(lib_session, copies=1)
        member = self._make_member(lib_session)
        lib_session.commit()

        loan = lib.borrow_book(lib_session, member.id, book.id)
        lib_session.commit()
        lib_session.refresh(book)
        assert book.available_copies == 0

        lib.return_book(lib_session, loan.id)
        lib_session.commit()
        lib_session.refresh(book)

        assert book.available_copies == 1
        assert loan.returned_at is not None
        assert loan.is_returned is True

    def test_return_already_returned_raises(self, lib_session):
        book   = self._make_book(lib_session)
        member = self._make_member(lib_session)
        lib_session.commit()

        loan = lib.borrow_book(lib_session, member.id, book.id)
        lib_session.commit()

        lib.return_book(lib_session, loan.id)
        lib_session.commit()

        with pytest.raises(lib.LibraryError):
            lib.return_book(lib_session, loan.id)

    def test_return_nonexistent_loan_raises(self, lib_session):
        with pytest.raises(lib.LibraryError):
            lib.return_book(lib_session, 9999)

    def test_find_overdue_loans(self, lib_session):
        book1  = self._make_book(lib_session, "Book A", copies=2)
        book2  = self._make_book(lib_session, "Book B", copies=2)
        alice  = self._make_member(lib_session, "Alice")
        bob    = self._make_member(lib_session, "Bob")
        lib_session.commit()

        # Loan borrowed 20 days ago — overdue
        overdue_loan = lib.borrow_book(lib_session, alice.id, book1.id)
        lib_session.commit()
        overdue_loan.borrowed_at = datetime.now(tz=timezone.utc) - timedelta(days=20)
        lib_session.commit()

        # Loan borrowed today — not overdue
        fresh_loan = lib.borrow_book(lib_session, bob.id, book2.id)
        lib_session.commit()

        overdue = lib.find_overdue_loans(lib_session, days=14)
        overdue_ids = [ln.id for ln in overdue]

        assert overdue_loan.id in overdue_ids
        assert fresh_loan.id not in overdue_ids

    def test_find_overdue_excludes_returned(self, lib_session):
        book   = self._make_book(lib_session, copies=2)
        member = self._make_member(lib_session)
        lib_session.commit()

        loan = lib.borrow_book(lib_session, member.id, book.id)
        lib_session.commit()
        loan.borrowed_at = datetime.now(tz=timezone.utc) - timedelta(days=20)
        lib_session.commit()

        lib.return_book(lib_session, loan.id)
        lib_session.commit()

        overdue = lib.find_overdue_loans(lib_session, days=14)
        assert loan.id not in [ln.id for ln in overdue]

    def test_loan_is_returned_property(self, lib_session):
        book   = self._make_book(lib_session)
        member = self._make_member(lib_session)
        lib_session.commit()

        loan = lib.borrow_book(lib_session, member.id, book.id)
        lib_session.commit()
        assert loan.is_returned is False

        lib.return_book(lib_session, loan.id)
        lib_session.commit()
        assert loan.is_returned is True
