"""
teacher.py — Teacher class for the school system.

Demonstrates:
- Inheriting from Person
- Domain-specific logic (salary, departments, teaching load)
- @property with computed values
- Class method as factory
"""

from datetime import date
from typing import List, Optional
from person import Person


class Teacher(Person):
    """A teacher who teaches courses at the school.

    Inherits from Person. Adds:
    - department and title
    - salary with validation
    - teaching assignments (course IDs)
    - qualification tracking
    """

    # Class attribute — standard teaching load
    STANDARD_COURSES_PER_SEMESTER: int = 4

    def __init__(
        self,
        teacher_id: str,
        first_name: str,
        last_name: str,
        date_of_birth: date,
        email: str,
        department: str,
        annual_salary: float,
        title: str = "Professor",
        phone: Optional[str] = None,
        qualifications: Optional[List[str]] = None,
    ) -> None:
        """Initialize a Teacher.

        Args:
            teacher_id: Unique teacher ID (e.g., "T001")
            first_name: First name
            last_name: Last name
            date_of_birth: Date of birth
            email: Email address
            department: Academic department (e.g., "Computer Science")
            annual_salary: Annual salary in the default currency
            title: Academic title (default: "Professor")
            phone: Optional phone number
            qualifications: Optional list of degrees/qualifications
        """
        super().__init__(
            person_id=teacher_id,
            first_name=first_name,
            last_name=last_name,
            date_of_birth=date_of_birth,
            email=email,
            phone=phone,
        )

        if not department.strip():
            raise ValueError("Department cannot be empty")
        if annual_salary < 0:
            raise ValueError(f"Salary cannot be negative: {annual_salary}")

        self._department: str = department.strip()
        self._annual_salary: float = annual_salary
        self._title: str = title
        self._course_ids: List[str] = []   # Currently teaching courses
        self._qualifications: List[str] = list(qualifications or [])
        self._is_active: bool = True

    # -----------------------------------------------------------------------
    # Properties
    # -----------------------------------------------------------------------

    @property
    def role(self) -> str:
        """Implements abstract property from Person."""
        return "Teacher"

    @property
    def teacher_id(self) -> str:
        """Alias for person_id."""
        return self.person_id

    @property
    def department(self) -> str:
        return self._department

    @department.setter
    def department(self, new_dept: str) -> None:
        if not new_dept.strip():
            raise ValueError("Department cannot be empty")
        self._department = new_dept.strip()

    @property
    def title(self) -> str:
        return self._title

    @title.setter
    def title(self, new_title: str) -> None:
        if not new_title.strip():
            raise ValueError("Title cannot be empty")
        self._title = new_title.strip()

    @property
    def annual_salary(self) -> float:
        return self._annual_salary

    @annual_salary.setter
    def annual_salary(self, new_salary: float) -> None:
        if new_salary < 0:
            raise ValueError(f"Salary cannot be negative: {new_salary}")
        self._annual_salary = new_salary

    @property
    def monthly_salary(self) -> float:
        """Computed: annual salary / 12."""
        return self._annual_salary / 12

    @property
    def course_ids(self) -> List[str]:
        """IDs of courses this teacher currently teaches."""
        return list(self._course_ids)

    @property
    def teaching_load(self) -> int:
        """Number of courses currently teaching."""
        return len(self._course_ids)

    @property
    def is_overloaded(self) -> bool:
        """Whether this teacher is teaching more than the standard load."""
        return self.teaching_load > self.STANDARD_COURSES_PER_SEMESTER

    @property
    def qualifications(self) -> List[str]:
        return list(self._qualifications)

    @property
    def is_active(self) -> bool:
        return self._is_active

    # -----------------------------------------------------------------------
    # Methods
    # -----------------------------------------------------------------------

    def assign_course(self, course_id: str) -> None:
        """Assign a course to this teacher.

        Raises:
            RuntimeError: If teacher is already teaching this course
        """
        if course_id in self._course_ids:
            raise RuntimeError(
                f"{self.full_name} is already assigned to course {course_id}"
            )
        self._course_ids.append(course_id)

    def unassign_course(self, course_id: str) -> None:
        """Remove a course from this teacher's load."""
        if course_id not in self._course_ids:
            raise ValueError(
                f"{self.full_name} is not assigned to course {course_id}"
            )
        self._course_ids.remove(course_id)

    def add_qualification(self, qualification: str) -> None:
        """Add a qualification/degree."""
        if qualification not in self._qualifications:
            self._qualifications.append(qualification)

    def give_raise(self, percentage: float) -> float:
        """Give a percentage raise. Returns the new salary."""
        if percentage <= 0:
            raise ValueError(f"Raise percentage must be positive: {percentage}")
        increase = self._annual_salary * (percentage / 100)
        self._annual_salary += increase
        return self._annual_salary

    def deactivate(self) -> None:
        """Mark as inactive (resigned, retired, etc.)."""
        self._is_active = False

    # -----------------------------------------------------------------------
    # Class methods
    # -----------------------------------------------------------------------

    @classmethod
    def create_adjunct(
        cls,
        teacher_id: str,
        first_name: str,
        last_name: str,
        dob: date,
        email: str,
        department: str,
        per_course_pay: float,
    ) -> "Teacher":
        """Factory method to create an adjunct (part-time) teacher.

        Adjuncts are paid per course with a lower standard.
        """
        return cls(
            teacher_id=teacher_id,
            first_name=first_name,
            last_name=last_name,
            date_of_birth=dob,
            email=email,
            department=department,
            annual_salary=per_course_pay * 4,   # Assume 4 courses/year
            title="Adjunct Professor",
        )

    # -----------------------------------------------------------------------
    # Abstract method implementation
    # -----------------------------------------------------------------------

    def describe(self) -> str:
        """Implements abstract method from Person."""
        quals = ", ".join(self._qualifications) if self._qualifications else "Not specified"
        status = "Active" if self._is_active else "Inactive"
        return (
            f"{self._title}: {self.full_name}\n"
            f"  ID: {self.teacher_id} | Department: {self._department}\n"
            f"  Status: {status} | Teaching: {self.teaching_load} courses\n"
            f"  Salary: ${self._annual_salary:,.2f}/year\n"
            f"  Qualifications: {quals}\n"
            f"  {self.contact_info()}"
        )

    def __str__(self) -> str:
        return (
            f"{self._title} {self.full_name} "
            f"({self.teacher_id}, {self._department}, "
            f"{self.teaching_load} courses)"
        )
