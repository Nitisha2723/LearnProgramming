"""
library_db.py — Library management system using SQLAlchemy ORM.

Requires: pip install sqlalchemy

Models:
    Book   — title, author, isbn, available copies
    Member — name, email, membership number
    Loan   — which member borrowed which book, when, when returned

Operations:
    borrow_book(session, member_id, book_id) — create a Loan, decrement copies
    return_book(session, loan_id)             — set return date, increment copies
    find_overdue_loans(session)               — loans older than 14 days with no return

Demo: creates books and members, borrows, returns, checks overdue.
"""

from __future__ import annotations

import random
import string
from datetime import datetime, timedelta, timezone

# Convenience helper — timezone-aware UTC now
def _utcnow() -> datetime:
    return datetime.now(tz=timezone.utc)

from sqlalchemy import (
    DateTime,
    ForeignKey,
    Integer,
    String,
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

engine = create_engine("sqlite:///:memory:", echo=False)

# --------------------------------------------------------------------------- #
#  Models                                                                       #
# --------------------------------------------------------------------------- #


class Base(DeclarativeBase):
    pass


class Book(Base):
    """A book in the library catalogue."""

    __tablename__ = "books"

    id:              Mapped[int] = mapped_column(Integer, primary_key=True)
    title:           Mapped[str] = mapped_column(String(300), nullable=False)
    author:          Mapped[str] = mapped_column(String(200), nullable=False)
    isbn:            Mapped[str] = mapped_column(String(20),  nullable=False, unique=True)
    total_copies:    Mapped[int] = mapped_column(Integer, nullable=False, default=1)
    available_copies: Mapped[int] = mapped_column(Integer, nullable=False, default=1)

    loans: Mapped[list[Loan]] = relationship("Loan", back_populates="book")

    def __repr__(self) -> str:
        return (
            f"Book(id={self.id!r}, title={self.title!r}, "
            f"available={self.available_copies}/{self.total_copies})"
        )


class Member(Base):
    """A library member."""

    __tablename__ = "members"

    id:                Mapped[int] = mapped_column(Integer, primary_key=True)
    name:              Mapped[str] = mapped_column(String(100), nullable=False)
    email:             Mapped[str] = mapped_column(String(200), nullable=False, unique=True)
    membership_number: Mapped[str] = mapped_column(String(20),  nullable=False, unique=True)

    loans: Mapped[list[Loan]] = relationship("Loan", back_populates="member")

    def __repr__(self) -> str:
        return f"Member(id={self.id!r}, name={self.name!r}, number={self.membership_number!r})"


class Loan(Base):
    """A borrowing record linking a Member to a Book."""

    __tablename__ = "loans"

    id:          Mapped[int] = mapped_column(Integer, primary_key=True)
    member_id:   Mapped[int] = mapped_column(Integer, ForeignKey("members.id"), nullable=False)
    book_id:     Mapped[int] = mapped_column(Integer, ForeignKey("books.id"),   nullable=False)
    borrowed_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    returned_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True, default=None)

    member: Mapped[Member] = relationship("Member", back_populates="loans")
    book:   Mapped[Book]   = relationship("Book",   back_populates="loans")

    @property
    def is_returned(self) -> bool:
        return self.returned_at is not None

    @property
    def days_borrowed(self) -> int:
        # SQLite stores datetimes without timezone; normalise to naive UTC for arithmetic.
        end = self.returned_at or datetime.now(tz=timezone.utc)
        end_naive = end.replace(tzinfo=None) if end.tzinfo else end
        start_naive = self.borrowed_at.replace(tzinfo=None) if self.borrowed_at.tzinfo else self.borrowed_at
        return (end_naive - start_naive).days

    def __repr__(self) -> str:
        status = f"returned {self.returned_at.date()}" if self.returned_at else "active"
        return (
            f"Loan(id={self.id!r}, member_id={self.member_id!r}, "
            f"book_id={self.book_id!r}, borrowed={self.borrowed_at.date()}, {status})"
        )


# --------------------------------------------------------------------------- #
#  Business operations                                                          #
# --------------------------------------------------------------------------- #


class LibraryError(Exception):
    """Raised when a business rule is violated."""
    pass


def borrow_book(session: Session, member_id: int, book_id: int) -> Loan:
    """
    Create a Loan for the given member and book.

    Business rules enforced:
    - Member must exist.
    - Book must exist.
    - At least one copy must be available.
    - A member cannot borrow the same book twice without returning it first.

    This runs as a single transaction: both the Loan insert and the
    available_copies decrement happen together, or neither does.
    """
    member = session.get(Member, member_id)
    if member is None:
        raise LibraryError(f"Member {member_id} not found.")

    book = session.get(Book, book_id)
    if book is None:
        raise LibraryError(f"Book {book_id} not found.")

    if book.available_copies < 1:
        raise LibraryError(
            f"No copies of '{book.title}' are available. "
            f"Total: {book.total_copies}, available: {book.available_copies}."
        )

    # Check: member doesn't already have this book on loan
    existing_stmt = (
        select(Loan)
        .where(
            Loan.member_id == member_id,
            Loan.book_id   == book_id,
            Loan.returned_at.is_(None),
        )
    )
    existing = session.execute(existing_stmt).scalar_one_or_none()
    if existing is not None:
        raise LibraryError(
            f"{member.name} already has '{book.title}' on loan (loan id={existing.id})."
        )

    # Create loan and decrement available copies (atomic via session transaction)
    loan = Loan(
        member_id=member_id,
        book_id=book_id,
        borrowed_at=_utcnow(),
    )
    book.available_copies -= 1
    session.add(loan)
    session.flush()  # assigns loan.id

    return loan


def return_book(session: Session, loan_id: int) -> Loan:
    """
    Mark a loan as returned and increment available copies.

    Business rules:
    - Loan must exist.
    - Loan must not already be returned.
    """
    loan = session.get(Loan, loan_id)
    if loan is None:
        raise LibraryError(f"Loan {loan_id} not found.")

    if loan.is_returned:
        raise LibraryError(
            f"Loan {loan_id} was already returned on {loan.returned_at}."
        )

    loan.returned_at = _utcnow()
    loan.book.available_copies += 1

    return loan


def find_overdue_loans(session: Session, days: int = 14) -> list[Loan]:
    """
    Return all active loans borrowed more than `days` days ago.

    Uses selectinload to eagerly load member and book — avoids N+1
    when the caller iterates over loans to print details.
    """
    cutoff = _utcnow() - timedelta(days=days)
    stmt = (
        select(Loan)
        .options(selectinload(Loan.member), selectinload(Loan.book))
        .where(
            Loan.returned_at.is_(None),
            Loan.borrowed_at <= cutoff,
        )
        .order_by(Loan.borrowed_at)
    )
    return session.execute(stmt).scalars().all()


def find_active_loans_for_member(session: Session, member_id: int) -> list[Loan]:
    """Return all currently borrowed books for a member."""
    stmt = (
        select(Loan)
        .options(selectinload(Loan.book))
        .where(Loan.member_id == member_id, Loan.returned_at.is_(None))
    )
    return session.execute(stmt).scalars().all()


def find_loan_history_for_book(session: Session, book_id: int) -> list[Loan]:
    """Return all loans (past and present) for a book, newest first."""
    stmt = (
        select(Loan)
        .options(selectinload(Loan.member))
        .where(Loan.book_id == book_id)
        .order_by(Loan.borrowed_at.desc())
    )
    return session.execute(stmt).scalars().all()


# --------------------------------------------------------------------------- #
#  Helpers                                                                      #
# --------------------------------------------------------------------------- #


def _generate_membership_number() -> str:
    """Generate a random 8-character membership number."""
    return "M" + "".join(random.choices(string.digits, k=7))


def _sep(title: str) -> None:
    print(f"\n{'─' * 60}")
    print(f"  {title}")
    print("─" * 60)


# --------------------------------------------------------------------------- #
#  Demo                                                                         #
# --------------------------------------------------------------------------- #


def main() -> None:
    print("Library Database System — SQLAlchemy ORM Demo")
    print("=" * 60)

    Base.metadata.create_all(engine)
    print("Tables created: books, members, loans")

    with Session(engine) as session:

        # ── Seed books ────────────────────────────────────────
        _sep("Add books to catalogue")
        books_data = [
            ("The Pragmatic Programmer", "Hunt & Thomas",    "978-0135957059", 3),
            ("Clean Code",               "Robert C. Martin", "978-0132350884", 2),
            ("Python Cookbook",          "Beazley & Jones",  "978-1449340377", 1),
            ("Design Patterns",          "Gang of Four",     "978-0201633610", 2),
            ("The Mythical Man-Month",   "Fred Brooks",      "978-0201835953", 1),
        ]
        books = []
        for title, author, isbn, copies in books_data:
            book = Book(
                title=title,
                author=author,
                isbn=isbn,
                total_copies=copies,
                available_copies=copies,
            )
            session.add(book)
            books.append(book)
        session.flush()

        for b in books:
            print(f"  Added: {b}")

        # ── Seed members ──────────────────────────────────────
        _sep("Register members")
        members_data = [
            ("Alice Chen",    "alice@library.example"),
            ("Bob Martinez",  "bob@library.example"),
            ("Carol Johnson", "carol@library.example"),
        ]
        members = []
        for name, email in members_data:
            member = Member(name=name, email=email, membership_number=_generate_membership_number())
            session.add(member)
            members.append(member)
        session.flush()

        for m in members:
            print(f"  Registered: {m}")

        session.commit()

        # ── Borrow books ──────────────────────────────────────
        _sep("Borrow books")
        alice, bob, carol = members
        pragmatic, clean_code, cookbook, design_patterns, mythical = books

        loan1 = borrow_book(session, alice.id,  pragmatic.id)
        loan2 = borrow_book(session, bob.id,    clean_code.id)
        loan3 = borrow_book(session, alice.id,  cookbook.id)
        loan4 = borrow_book(session, carol.id,  design_patterns.id)
        session.commit()

        print(f"  {alice.name} borrowed '{pragmatic.title}' → loan id={loan1.id}")
        print(f"  {bob.name} borrowed '{clean_code.title}' → loan id={loan2.id}")
        print(f"  {alice.name} borrowed '{cookbook.title}' → loan id={loan3.id}")
        print(f"  {carol.name} borrowed '{design_patterns.title}' → loan id={loan4.id}")

        # Check availability updated
        session.refresh(pragmatic)
        session.refresh(cookbook)
        print(f"\n  Pragmatic Programmer: {pragmatic.available_copies}/{pragmatic.total_copies} available")
        print(f"  Python Cookbook:      {cookbook.available_copies}/{cookbook.total_copies} available")

        # ── Try to borrow unavailable book ────────────────────
        _sep("Try to borrow an unavailable book")
        try:
            borrow_book(session, bob.id, cookbook.id)  # only 1 copy, already borrowed
        except LibraryError as e:
            print(f"  LibraryError: {e}")

        # ── Try to borrow same book twice ─────────────────────
        _sep("Try to borrow the same book twice")
        try:
            borrow_book(session, alice.id, pragmatic.id)  # alice already has it
        except LibraryError as e:
            print(f"  LibraryError: {e}")

        # ── Alice's active loans ──────────────────────────────
        _sep("Alice's active loans")
        alice_loans = find_active_loans_for_member(session, alice.id)
        for loan in alice_loans:
            print(f"  '{loan.book.title}' — borrowed {loan.borrowed_at.date()}")

        # ── Return a book ─────────────────────────────────────
        _sep("Return a book")
        return_book(session, loan3.id)  # alice returns cookbook
        session.commit()
        session.refresh(cookbook)
        print(f"  Alice returned '{cookbook.title}'")
        print(f"  Python Cookbook: {cookbook.available_copies}/{cookbook.total_copies} available")

        # ── Try to return already-returned book ───────────────
        _sep("Try to return already-returned book")
        try:
            return_book(session, loan3.id)
        except LibraryError as e:
            print(f"  LibraryError: {e}")

        # ── Simulate overdue loans ────────────────────────────
        _sep("Simulate overdue loans (backdating borrowed_at)")
        # Backdate loan1 and loan2 to 20 days ago to simulate overdue
        twenty_days_ago = _utcnow() - timedelta(days=20)
        loan1_obj = session.get(Loan, loan1.id)
        loan2_obj = session.get(Loan, loan2.id)
        loan1_obj.borrowed_at = twenty_days_ago  # type: ignore[union-attr]
        loan2_obj.borrowed_at = twenty_days_ago  # type: ignore[union-attr]
        session.commit()

        overdue = find_overdue_loans(session, days=14)
        print(f"  Overdue loans (>{14} days): {len(overdue)}")
        for loan in overdue:
            print(
                f"  [{loan.id}] '{loan.book.title}' borrowed by {loan.member.name} "
                f"— {loan.days_borrowed} days ago"
            )

        # ── Loan history for a book ───────────────────────────
        _sep("Loan history for 'Python Cookbook'")
        history = find_loan_history_for_book(session, cookbook.id)
        for loan in history:
            status = "returned" if loan.is_returned else "active"
            print(
                f"  {loan.member.name}: borrowed {loan.borrowed_at.date()} "
                f"— {status}"
            )

        # ── Summary ───────────────────────────────────────────
        _sep("Library summary")
        all_books = session.execute(select(Book)).scalars().all()
        total_loans = session.execute(select(Loan)).scalars().all()
        active_loans = [ln for ln in total_loans if not ln.is_returned]
        print(f"  Books in catalogue: {len(all_books)}")
        print(f"  Total loans created: {len(total_loans)}")
        print(f"  Currently active: {len(active_loans)}")

    print("\nSession closed. Library demo complete.")


if __name__ == "__main__":
    main()
