"""
person.py — Base class for all people in the school system.

Demonstrates:
- ABC with abstract methods
- @property for controlled attribute access
- @dataclass-like pattern with __init__ and type hints
- __str__, __repr__, __eq__
"""

from abc import ABC, abstractmethod
from datetime import date, datetime
from typing import Optional


class Person(ABC):
    """Abstract base class for everyone in the school system.

    All persons have: name, date_of_birth, email, phone (optional).
    Each subclass must implement role() and describe() methods.

    This is like a Java abstract class — cannot be instantiated directly.
    """

    def __init__(
        self,
        person_id: str,
        first_name: str,
        last_name: str,
        date_of_birth: date,
        email: str,
        phone: Optional[str] = None,
    ) -> None:
        """Initialize a Person.

        Args:
            person_id: Unique identifier (e.g., "S001" for students, "T001" for teachers)
            first_name: First name
            last_name: Last name
            date_of_birth: Date of birth
            email: Email address
            phone: Optional phone number
        """
        # Validation
        if not first_name.strip():
            raise ValueError("First name cannot be empty")
        if not last_name.strip():
            raise ValueError("Last name cannot be empty")
        if not email.strip() or "@" not in email:
            raise ValueError(f"Invalid email: {email!r}")
        if date_of_birth > date.today():
            raise ValueError(f"Date of birth cannot be in the future: {date_of_birth}")

        self._person_id: str = person_id
        self._first_name: str = first_name.strip()
        self._last_name: str = last_name.strip()
        self._date_of_birth: date = date_of_birth
        self._email: str = email.strip()
        self._phone: Optional[str] = phone

    # -----------------------------------------------------------------------
    # Read-only properties
    # -----------------------------------------------------------------------

    @property
    def person_id(self) -> str:
        """Unique ID — never changes."""
        return self._person_id

    @property
    def first_name(self) -> str:
        return self._first_name

    @property
    def last_name(self) -> str:
        return self._last_name

    @property
    def full_name(self) -> str:
        """Computed: first_name + last_name."""
        return f"{self._first_name} {self._last_name}"

    @property
    def date_of_birth(self) -> date:
        return self._date_of_birth

    @property
    def age(self) -> int:
        """Computed: current age in years."""
        today = date.today()
        birthday_this_year = self._date_of_birth.replace(year=today.year)
        years = today.year - self._date_of_birth.year
        if birthday_this_year > today:
            years -= 1
        return years

    # -----------------------------------------------------------------------
    # Mutable properties (with validation)
    # -----------------------------------------------------------------------

    @property
    def email(self) -> str:
        return self._email

    @email.setter
    def email(self, new_email: str) -> None:
        if not new_email.strip() or "@" not in new_email:
            raise ValueError(f"Invalid email: {new_email!r}")
        self._email = new_email.strip()

    @property
    def phone(self) -> Optional[str]:
        return self._phone

    @phone.setter
    def phone(self, new_phone: Optional[str]) -> None:
        self._phone = new_phone

    # -----------------------------------------------------------------------
    # Abstract methods
    # -----------------------------------------------------------------------

    @property
    @abstractmethod
    def role(self) -> str:
        """Return the role of this person (e.g., 'Student', 'Teacher')."""
        ...

    @abstractmethod
    def describe(self) -> str:
        """Return a full description of this person."""
        ...

    # -----------------------------------------------------------------------
    # Concrete methods
    # -----------------------------------------------------------------------

    def contact_info(self) -> str:
        """Return contact information."""
        lines = [f"Email: {self._email}"]
        if self._phone:
            lines.append(f"Phone: {self._phone}")
        return " | ".join(lines)

    # -----------------------------------------------------------------------
    # Dunder methods
    # -----------------------------------------------------------------------

    def __str__(self) -> str:
        return f"{self.role}: {self.full_name} ({self._person_id})"

    def __repr__(self) -> str:
        return (
            f"{type(self).__name__}("
            f"id={self._person_id!r}, "
            f"name={self.full_name!r}, "
            f"email={self._email!r})"
        )

    def __eq__(self, other: object) -> bool:
        """Persons are equal if they have the same person_id."""
        if not isinstance(other, Person):
            return NotImplemented
        return self._person_id == other._person_id

    def __hash__(self) -> int:
        return hash(self._person_id)

    def __lt__(self, other: "Person") -> bool:
        """Compare by last name, then first name (for sorting)."""
        if not isinstance(other, Person):
            return NotImplemented
        return (self._last_name, self._first_name) < (other._last_name, other._first_name)
