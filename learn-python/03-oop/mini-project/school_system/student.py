"""
student.py — Student class for the school system.

Demonstrates:
- Inheriting from Person (ABC)
- super().__init__() call
- Additional domain logic (grades, GPA)
- Properties combining multiple attributes
"""

from datetime import date
from typing import Dict, List, Optional, Tuple
from person import Person


class GradeRecord:
    """Immutable record of a grade for a course.

    Using __slots__ for memory efficiency since there could be many of these.
    """
    __slots__ = ('course_id', 'course_name', 'grade', 'semester', 'credits')

    def __init__(
        self,
        course_id: str,
        course_name: str,
        grade: float,
        semester: str,
        credits: float,
    ) -> None:
        if not 0.0 <= grade <= 100.0:
            raise ValueError(f"Grade must be 0-100, got {grade}")
        self.course_id = course_id
        self.course_name = course_name
        self.grade = grade
        self.semester = semester
        self.credits = credits

    @property
    def letter_grade(self) -> str:
        """Convert numeric grade to letter grade."""
        if self.grade >= 90:
            return "A"
        elif self.grade >= 80:
            return "B"
        elif self.grade >= 70:
            return "C"
        elif self.grade >= 60:
            return "D"
        else:
            return "F"

    @property
    def grade_points(self) -> float:
        """Convert to GPA points (4.0 scale)."""
        if self.grade >= 90:
            return 4.0
        elif self.grade >= 80:
            return 3.0
        elif self.grade >= 70:
            return 2.0
        elif self.grade >= 60:
            return 1.0
        else:
            return 0.0

    def __str__(self) -> str:
        return (
            f"{self.course_name}: {self.grade:.1f} "
            f"({self.letter_grade}) — {self.semester}"
        )

    def __repr__(self) -> str:
        return (
            f"GradeRecord(course={self.course_id!r}, "
            f"grade={self.grade}, semester={self.semester!r})"
        )


class Student(Person):
    """A student enrolled in the school.

    Inherits from Person. Adds:
    - enrollment date and status
    - course enrollment tracking
    - grade recording
    - GPA calculation
    """

    def __init__(
        self,
        student_id: str,
        first_name: str,
        last_name: str,
        date_of_birth: date,
        email: str,
        enrollment_date: Optional[date] = None,
        phone: Optional[str] = None,
        major: str = "Undeclared",
    ) -> None:
        """Initialize a Student.

        Calls super().__init__() to handle Person initialization.
        """
        super().__init__(
            person_id=student_id,
            first_name=first_name,
            last_name=last_name,
            date_of_birth=date_of_birth,
            email=email,
            phone=phone,
        )

        self._enrollment_date: date = enrollment_date or date.today()
        self._major: str = major
        self._is_active: bool = True
        self._enrolled_course_ids: List[str] = []   # IDs of currently enrolled courses
        self._grade_records: List[GradeRecord] = []

    # -----------------------------------------------------------------------
    # Properties
    # -----------------------------------------------------------------------

    @property
    def role(self) -> str:
        """Implements abstract property from Person."""
        return "Student"

    @property
    def student_id(self) -> str:
        """Alias for person_id — more descriptive name."""
        return self.person_id

    @property
    def enrollment_date(self) -> date:
        return self._enrollment_date

    @property
    def major(self) -> str:
        return self._major

    @major.setter
    def major(self, new_major: str) -> None:
        if not new_major.strip():
            raise ValueError("Major cannot be empty")
        self._major = new_major.strip()

    @property
    def is_active(self) -> bool:
        """Whether the student is currently enrolled."""
        return self._is_active

    @property
    def enrolled_course_ids(self) -> List[str]:
        """IDs of courses the student is currently enrolled in."""
        return list(self._enrolled_course_ids)

    @property
    def grade_records(self) -> List[GradeRecord]:
        """All grade records (past courses)."""
        return list(self._grade_records)

    @property
    def gpa(self) -> float:
        """Calculate GPA on 4.0 scale using weighted average.

        This is a computed property — calculated from grade records.
        """
        if not self._grade_records:
            return 0.0

        total_points = sum(r.grade_points * r.credits for r in self._grade_records)
        total_credits = sum(r.credits for r in self._grade_records)

        if total_credits == 0:
            return 0.0

        return round(total_points / total_credits, 2)

    @property
    def total_credits(self) -> float:
        """Total credits earned."""
        return sum(r.credits for r in self._grade_records)

    @property
    def standing(self) -> str:
        """Academic standing based on total credits.

        Freshman < 30 credits
        Sophomore 30-59
        Junior 60-89
        Senior 90+
        """
        credits = self.total_credits
        if credits < 30:
            return "Freshman"
        elif credits < 60:
            return "Sophomore"
        elif credits < 90:
            return "Junior"
        else:
            return "Senior"

    # -----------------------------------------------------------------------
    # Methods
    # -----------------------------------------------------------------------

    def enroll_in_course(self, course_id: str) -> None:
        """Enroll in a course.

        Args:
            course_id: ID of the course to enroll in

        Raises:
            RuntimeError: If student is not active or already enrolled
        """
        if not self._is_active:
            raise RuntimeError(f"{self.full_name} is not an active student")
        if course_id in self._enrolled_course_ids:
            raise RuntimeError(f"{self.full_name} is already enrolled in {course_id}")
        self._enrolled_course_ids.append(course_id)

    def drop_course(self, course_id: str) -> None:
        """Drop a course.

        Raises:
            ValueError: If student is not enrolled in this course
        """
        if course_id not in self._enrolled_course_ids:
            raise ValueError(f"{self.full_name} is not enrolled in {course_id}")
        self._enrolled_course_ids.remove(course_id)

    def record_grade(
        self,
        course_id: str,
        course_name: str,
        grade: float,
        semester: str,
        credits: float = 3.0,
    ) -> None:
        """Record a grade for a completed course.

        This moves the course from current enrollment to grade history.
        """
        record = GradeRecord(course_id, course_name, grade, semester, credits)
        self._grade_records.append(record)
        # Remove from currently enrolled if present
        if course_id in self._enrolled_course_ids:
            self._enrolled_course_ids.remove(course_id)

    def deactivate(self) -> None:
        """Mark the student as inactive (graduated, withdrawn, etc.)."""
        self._is_active = False

    def get_transcript(self) -> str:
        """Generate an academic transcript."""
        lines = [
            f"Academic Transcript",
            f"Student: {self.full_name} ({self.student_id})",
            f"Major: {self._major} | Standing: {self.standing}",
            f"GPA: {self.gpa:.2f} | Total Credits: {self.total_credits:.1f}",
            f"{'=' * 50}",
        ]

        if not self._grade_records:
            lines.append("No grades recorded yet")
        else:
            # Group by semester
            by_semester: Dict[str, List[GradeRecord]] = {}
            for record in self._grade_records:
                by_semester.setdefault(record.semester, []).append(record)

            for semester, records in sorted(by_semester.items()):
                lines.append(f"\n{semester}:")
                for record in records:
                    lines.append(f"  {record}")

        return "\n".join(lines)

    # -----------------------------------------------------------------------
    # Abstract method implementation
    # -----------------------------------------------------------------------

    def describe(self) -> str:
        """Implements abstract method from Person."""
        status = "Active" if self._is_active else "Inactive"
        return (
            f"Student: {self.full_name}\n"
            f"  ID: {self.student_id} | Major: {self._major}\n"
            f"  Status: {status} | Standing: {self.standing}\n"
            f"  GPA: {self.gpa:.2f} | Credits: {self.total_credits:.1f}\n"
            f"  {self.contact_info()}"
        )

    def __str__(self) -> str:
        return f"Student: {self.full_name} ({self.student_id}, {self.standing}, GPA: {self.gpa:.2f})"
