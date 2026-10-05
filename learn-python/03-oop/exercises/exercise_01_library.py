"""
Exercise 01: Library Management System

TOPIC: Classes, properties, __str__, __repr__, __eq__, class attributes

SCENARIO:
You are building a library management system. You need to model books,
library members, and the library itself.

WHAT TO IMPLEMENT:
1. Book class — represents a book with ISBN, title, author, year, and availability
2. Member class — represents a library member who can borrow books
3. Library class — manages the collection and loans

LEARNING GOALS:
- Practice defining classes with __init__
- Use @property for controlled access
- Implement __str__, __repr__, __eq__
- Use class attributes for shared data
- Practice list manipulation within a class

GETTING STARTED:
- Read through all the TODO comments
- Implement one class at a time
- Test each class before moving to the next

RUN TO TEST:
    python exercise_01_library.py
"""

from typing import List, Optional
from datetime import datetime


# ============================================================
# TODO 1: Implement the Book class
# ============================================================

class Book:
    """Represents a book in the library.

    Attributes:
        isbn: International Standard Book Number (unique identifier)
        title: Book title
        author: Author's full name
        year: Publication year
        is_available: Whether the book is currently available for borrowing
    """

    def __init__(self, isbn: str, title: str, author: str, year: int) -> None:
        """Initialize a Book.

        All books start as available (is_available = True).

        TODO: Store all parameters as attributes.
              Use _underscore prefix for isbn and year (they shouldn't change after creation).
              Use _is_available for the availability flag.
        """
        # TODO: Implement this
        pass

    @property
    def isbn(self) -> str:
        """ISBN is read-only — doesn't change after creation."""
        # TODO: Return self._isbn
        pass

    @property
    def year(self) -> int:
        """Publication year is read-only."""
        # TODO: Return self._year
        pass

    @property
    def is_available(self) -> bool:
        """Whether the book is available to borrow."""
        # TODO: Return self._is_available
        pass

    def checkout(self) -> None:
        """Mark this book as checked out (not available).

        Raises:
            RuntimeError: If the book is already checked out.
        """
        # TODO: Set _is_available to False
        #       Raise RuntimeError if already checked out
        pass

    def return_book(self) -> None:
        """Mark this book as returned (available).

        Raises:
            RuntimeError: If the book is not currently checked out.
        """
        # TODO: Set _is_available to True
        #       Raise RuntimeError if book is not checked out
        pass

    def __str__(self) -> str:
        """Human-readable: 'The Hobbit by J.R.R. Tolkien (1937) [Available]'"""
        # TODO: Implement this
        pass

    def __repr__(self) -> str:
        """Developer: 'Book(isbn='978...', title='The Hobbit', author='...', year=1937)'"""
        # TODO: Implement this
        pass

    def __eq__(self, other: object) -> bool:
        """Two books are equal if they have the same ISBN."""
        # TODO: Implement this
        #       If other is not a Book, return NotImplemented
        pass

    def __hash__(self) -> int:
        """Hash based on ISBN."""
        # TODO: return hash(self._isbn)
        pass


# ============================================================
# TODO 2: Implement the Member class
# ============================================================

class Member:
    """Represents a library member.

    Class attribute:
        MAX_BOOKS: Maximum books a member can borrow at once (set to 3)

    Attributes:
        member_id: Unique member ID
        name: Member's full name
        borrowed_books: List of currently borrowed Books
    """

    MAX_BOOKS: int = 3   # Class attribute — shared by all members

    def __init__(self, member_id: str, name: str) -> None:
        """Initialize a Member with empty borrowed books list.

        TODO: Store member_id and name.
              Initialize _borrowed_books as an empty list.
        """
        # TODO: Implement this
        pass

    @property
    def member_id(self) -> str:
        """Member ID is read-only."""
        # TODO: Implement
        pass

    @property
    def name(self) -> str:
        """Member's name."""
        # TODO: Implement
        pass

    @name.setter
    def name(self, new_name: str) -> None:
        """Update name with validation (cannot be empty)."""
        # TODO: Validate and set self._name
        pass

    @property
    def borrowed_books(self) -> List[Book]:
        """Return a copy of the borrowed books list."""
        # TODO: Return list(self._borrowed_books) — return a copy!
        pass

    @property
    def can_borrow(self) -> bool:
        """Whether this member can borrow more books."""
        # TODO: Return True if len(borrowed_books) < MAX_BOOKS
        pass

    def borrow(self, book: Book) -> None:
        """Borrow a book.

        Args:
            book: The book to borrow

        Raises:
            RuntimeError: If the member already has MAX_BOOKS books
            RuntimeError: If the book is not available
            RuntimeError: If the member already has this book
        """
        # TODO: Check all conditions, then:
        #       - Call book.checkout()
        #       - Add book to self._borrowed_books
        pass

    def return_book(self, book: Book) -> None:
        """Return a borrowed book.

        Args:
            book: The book to return

        Raises:
            ValueError: If the member doesn't have this book
        """
        # TODO: Check the member has this book, then:
        #       - Remove from self._borrowed_books
        #       - Call book.return_book()
        pass

    def __str__(self) -> str:
        """Human-readable: 'Member Alice (M001) — 2/3 books borrowed'"""
        # TODO: Implement
        pass

    def __repr__(self) -> str:
        """Developer: 'Member(id='M001', name='Alice', books=2)'"""
        # TODO: Implement
        pass

    def __eq__(self, other: object) -> bool:
        """Members are equal if they have the same member_id."""
        # TODO: Implement
        pass


# ============================================================
# TODO 3: Implement the Library class
# ============================================================

class Library:
    """Manages a collection of books and members.

    Attributes:
        name: Library name
        books: Collection of all books
        members: Registered members
    """

    def __init__(self, name: str) -> None:
        """Initialize a Library.

        TODO: Store name.
              Initialize _books as an empty list.
              Initialize _members as an empty list.
        """
        # TODO: Implement
        pass

    @property
    def name(self) -> str:
        # TODO: Implement
        pass

    @property
    def available_books(self) -> List[Book]:
        """Return list of books that are currently available."""
        # TODO: Filter self._books for books where is_available is True
        pass

    @property
    def total_books(self) -> int:
        # TODO: Return len(self._books)
        pass

    def add_book(self, book: Book) -> None:
        """Add a book to the library.

        Raises:
            ValueError: If a book with the same ISBN already exists
        """
        # TODO: Check for duplicate ISBN, then add to _books
        pass

    def add_member(self, member: Member) -> None:
        """Register a new member.

        Raises:
            ValueError: If a member with the same ID already exists
        """
        # TODO: Check for duplicate member_id, then add to _members
        pass

    def find_book(self, isbn: str) -> Optional[Book]:
        """Find a book by ISBN. Returns None if not found."""
        # TODO: Search _books for matching ISBN
        pass

    def find_member(self, member_id: str) -> Optional[Member]:
        """Find a member by ID. Returns None if not found."""
        # TODO: Search _members for matching member_id
        pass

    def search_by_author(self, author: str) -> List[Book]:
        """Find all books by an author (case-insensitive partial match)."""
        # TODO: Return books where author.lower() is in book.author.lower()
        pass

    def __str__(self) -> str:
        """'City Library — 42 books (35 available), 15 members'"""
        # TODO: Implement
        pass

    def __repr__(self) -> str:
        # TODO: Implement
        pass

    def __len__(self) -> int:
        """Number of books in the library."""
        # TODO: Return total_books
        pass


# ============================================================
# Tests — these will tell you if your implementation is correct
# ============================================================

def run_basic_tests() -> None:
    """Run basic tests. These should all pass when you're done."""

    print("Testing Book class...")

    # Test Book creation
    book = Book("978-0-06-112008-4", "To Kill a Mockingbird", "Harper Lee", 1960)
    assert book.isbn == "978-0-06-112008-4"
    assert book.title == "To Kill a Mockingbird"
    assert book.author == "Harper Lee"
    assert book.year == 1960
    assert book.is_available is True
    print("  ✓ Book creation")

    # Test checkout and return
    book.checkout()
    assert book.is_available is False

    # Test double checkout raises error
    try:
        book.checkout()
        print("  ✗ Should have raised RuntimeError on double checkout")
    except RuntimeError:
        print("  ✓ Double checkout raises RuntimeError")

    book.return_book()
    assert book.is_available is True
    print("  ✓ Book checkout and return")

    # Test __eq__
    book2 = Book("978-0-06-112008-4", "Different Title", "Different Author", 2000)
    book3 = Book("978-different", "To Kill a Mockingbird", "Harper Lee", 1960)
    assert book == book2       # Same ISBN → equal
    assert book != book3       # Different ISBN → not equal
    print("  ✓ Book equality (based on ISBN)")

    # Test __str__ contains title and author
    book_str = str(book)
    assert "To Kill a Mockingbird" in book_str
    assert "Harper Lee" in book_str
    print("  ✓ Book __str__")

    print("\nTesting Member class...")

    member = Member("M001", "Alice")
    assert member.member_id == "M001"
    assert member.name == "Alice"
    assert member.can_borrow is True
    assert len(member.borrowed_books) == 0
    print("  ✓ Member creation")

    b1 = Book("B001", "Book 1", "Author A", 2000)
    b2 = Book("B002", "Book 2", "Author B", 2001)
    b3 = Book("B003", "Book 3", "Author C", 2002)
    b4 = Book("B004", "Book 4", "Author D", 2003)

    member.borrow(b1)
    member.borrow(b2)
    member.borrow(b3)
    assert len(member.borrowed_books) == 3
    assert member.can_borrow is False
    assert b1.is_available is False
    print("  ✓ Member borrow")

    # Cannot borrow when at limit
    try:
        member.borrow(b4)
        print("  ✗ Should have raised RuntimeError at max books")
    except RuntimeError:
        print("  ✓ Max books limit enforced")

    member.return_book(b1)
    assert len(member.borrowed_books) == 2
    assert b1.is_available is True
    print("  ✓ Member return book")

    print("\nTesting Library class...")

    library = Library("City Library")
    assert library.name == "City Library"
    assert len(library) == 0

    book_a = Book("A001", "Python OOP", "Guide Author", 2023)
    book_b = Book("B001", "Java OOP", "Java Author", 2022)
    member_alice = Member("M001", "Alice")

    library.add_book(book_a)
    library.add_book(book_b)
    library.add_member(member_alice)

    assert len(library) == 2
    assert library.total_books == 2
    assert len(library.available_books) == 2
    print("  ✓ Library add books and members")

    found = library.find_book("A001")
    assert found is not None
    assert found.title == "Python OOP"
    not_found = library.find_book("XXXXX")
    assert not_found is None
    print("  ✓ Library find book")

    results = library.search_by_author("author")
    assert len(results) == 2   # Both have "Author" in author name
    print("  ✓ Library search by author")

    print("\n✓ All basic tests passed!")


if __name__ == "__main__":
    run_basic_tests()
