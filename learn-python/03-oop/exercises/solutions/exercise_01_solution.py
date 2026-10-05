"""
Solution to Exercise 01: Library Management System

This is a complete, correct implementation.
Compare it to your solution after attempting the exercise yourself!
"""

from typing import List, Optional
from datetime import datetime


class Book:
    """Represents a book in the library."""

    def __init__(self, isbn: str, title: str, author: str, year: int) -> None:
        if not isbn.strip():
            raise ValueError("ISBN cannot be empty")
        if not title.strip():
            raise ValueError("Title cannot be empty")
        if not author.strip():
            raise ValueError("Author cannot be empty")
        if year < 0:
            raise ValueError(f"Invalid year: {year}")

        self._isbn = isbn
        self.title = title
        self.author = author
        self._year = year
        self._is_available = True

    @property
    def isbn(self) -> str:
        return self._isbn

    @property
    def year(self) -> int:
        return self._year

    @property
    def is_available(self) -> bool:
        return self._is_available

    def checkout(self) -> None:
        if not self._is_available:
            raise RuntimeError(f"Book '{self.title}' is already checked out")
        self._is_available = False

    def return_book(self) -> None:
        if self._is_available:
            raise RuntimeError(f"Book '{self.title}' is not checked out")
        self._is_available = True

    def __str__(self) -> str:
        status = "Available" if self._is_available else "Checked out"
        return f"{self.title} by {self.author} ({self._year}) [{status}]"

    def __repr__(self) -> str:
        return (
            f"Book(isbn={self._isbn!r}, title={self.title!r}, "
            f"author={self.author!r}, year={self._year})"
        )

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Book):
            return NotImplemented
        return self._isbn == other._isbn

    def __hash__(self) -> int:
        return hash(self._isbn)


class Member:
    """Represents a library member."""

    MAX_BOOKS: int = 3

    def __init__(self, member_id: str, name: str) -> None:
        if not member_id.strip():
            raise ValueError("Member ID cannot be empty")
        if not name.strip():
            raise ValueError("Member name cannot be empty")

        self._member_id = member_id
        self._name = name
        self._borrowed_books: List[Book] = []

    @property
    def member_id(self) -> str:
        return self._member_id

    @property
    def name(self) -> str:
        return self._name

    @name.setter
    def name(self, new_name: str) -> None:
        if not new_name.strip():
            raise ValueError("Name cannot be empty")
        self._name = new_name.strip()

    @property
    def borrowed_books(self) -> List[Book]:
        return list(self._borrowed_books)  # Return copy!

    @property
    def can_borrow(self) -> bool:
        return len(self._borrowed_books) < self.MAX_BOOKS

    def borrow(self, book: Book) -> None:
        if not self.can_borrow:
            raise RuntimeError(
                f"{self._name} has reached the borrowing limit ({self.MAX_BOOKS} books)"
            )
        if not book.is_available:
            raise RuntimeError(f"Book '{book.title}' is not available")
        if book in self._borrowed_books:
            raise RuntimeError(f"{self._name} already has '{book.title}'")

        book.checkout()
        self._borrowed_books.append(book)

    def return_book(self, book: Book) -> None:
        if book not in self._borrowed_books:
            raise ValueError(f"{self._name} doesn't have '{book.title}'")

        self._borrowed_books.remove(book)
        book.return_book()

    def __str__(self) -> str:
        return (
            f"Member {self._name} ({self._member_id}) — "
            f"{len(self._borrowed_books)}/{self.MAX_BOOKS} books borrowed"
        )

    def __repr__(self) -> str:
        return (
            f"Member(id={self._member_id!r}, name={self._name!r}, "
            f"books={len(self._borrowed_books)})"
        )

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Member):
            return NotImplemented
        return self._member_id == other._member_id

    def __hash__(self) -> int:
        return hash(self._member_id)


class Library:
    """Manages a collection of books and members."""

    def __init__(self, name: str) -> None:
        if not name.strip():
            raise ValueError("Library name cannot be empty")
        self._name = name
        self._books: List[Book] = []
        self._members: List[Member] = []

    @property
    def name(self) -> str:
        return self._name

    @property
    def available_books(self) -> List[Book]:
        return [b for b in self._books if b.is_available]

    @property
    def total_books(self) -> int:
        return len(self._books)

    def add_book(self, book: Book) -> None:
        existing = self.find_book(book.isbn)
        if existing is not None:
            raise ValueError(f"Book with ISBN {book.isbn} already exists")
        self._books.append(book)

    def add_member(self, member: Member) -> None:
        existing = self.find_member(member.member_id)
        if existing is not None:
            raise ValueError(f"Member with ID {member.member_id} already exists")
        self._members.append(member)

    def find_book(self, isbn: str) -> Optional[Book]:
        for book in self._books:
            if book.isbn == isbn:
                return book
        return None

    def find_member(self, member_id: str) -> Optional[Member]:
        for member in self._members:
            if member.member_id == member_id:
                return member
        return None

    def search_by_author(self, author: str) -> List[Book]:
        query = author.lower()
        return [b for b in self._books if query in b.author.lower()]

    def search_by_title(self, title: str) -> List[Book]:
        query = title.lower()
        return [b for b in self._books if query in b.title.lower()]

    def __str__(self) -> str:
        available = len(self.available_books)
        return (
            f"{self._name} — {self.total_books} books "
            f"({available} available), {len(self._members)} members"
        )

    def __repr__(self) -> str:
        return f"Library(name={self._name!r}, books={self.total_books})"

    def __len__(self) -> int:
        return self.total_books
