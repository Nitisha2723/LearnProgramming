"""
course.py — Course class for the school system.

Demonstrates:
- Composition (Course has-a Teacher, has many Students)
- @dataclass for the assessment type
- Context manager for enrollment period
- Complex business logic with validation
"""

from dataclasses import dataclass, field
from datetime import date
from typing import Dict, List, Optional, Set
from contextlib import contextmanager
from typing import Generator


@dataclass
class Assessment:
    """An assessment (exam, assignment, project) in a course.

    Using @dataclass: auto-generates __init__, __repr__, __eq__.
    """
    name: str
    max_score: float
    weight: float   # Weight as a percentage (e.g., 30 means 30%)

    def __post_init__(self) -> None:
        if self.max_score <= 0:
            raise ValueError(f"Max score must be positive: {self.max_score}")
        if not 0 < self.weight <= 100:
            raise ValueError(f"Weight must be between 0 and 100: {self.weight}")


@dataclass
class StudentGrade:
    """A student's scores for a course — one record per (student, course) pair."""
    student_id: str
    scores: Dict[str, float] = field(default_factory=dict)   # assessment_name → score

    def add_score(self, assessment_name: str, score: float, max_score: float) -> None:
        if score < 0 or score > max_score:
            raise ValueError(
                f"Score {score} is out of range [0, {max_score}] "
                f"for assessment {assessment_name!r}"
            )
        self.scores[assessment_name] = score


class Course:
    """A course offered by the school.

    A course:
    - Has a teacher assigned to it
    - Has students enrolled in it
    - Has assessments (exams, assignments) with weights
    - Tracks student grades
    - Has a capacity limit

    This demonstrates composition: Course HAS-A teacher reference,
    Course HAS-MANY student references.
    """

    def __init__(
        self,
        course_id: str,
        name: str,
        department: str,
        credits: float,
        capacity: int = 30,
        description: str = "",
    ) -> None:
        """Initialize a Course.

        Args:
            course_id: Unique course identifier (e.g., "CS101")
            name: Course name (e.g., "Introduction to Python")
            department: Department offering the course
            credits: Credit hours (e.g., 3.0)
            capacity: Maximum number of students
            description: Course description
        """
        if not course_id.strip():
            raise ValueError("Course ID cannot be empty")
        if not name.strip():
            raise ValueError("Course name cannot be empty")
        if credits <= 0:
            raise ValueError(f"Credits must be positive: {credits}")
        if capacity < 1:
            raise ValueError(f"Capacity must be at least 1: {capacity}")

        self._course_id: str = course_id.strip()
        self._name: str = name.strip()
        self._department: str = department.strip()
        self._credits: float = credits
        self._capacity: int = capacity
        self._description: str = description

        self._teacher_id: Optional[str] = None
        self._enrolled_student_ids: List[str] = []   # Active enrollment
        self._waitlist_ids: List[str] = []            # Waitlist
        self._assessments: List[Assessment] = []
        self._grades: Dict[str, StudentGrade] = {}   # student_id → StudentGrade
        self._is_enrollment_open: bool = False

    # -----------------------------------------------------------------------
    # Properties
    # -----------------------------------------------------------------------

    @property
    def course_id(self) -> str:
        return self._course_id

    @property
    def name(self) -> str:
        return self._name

    @property
    def department(self) -> str:
        return self._department

    @property
    def credits(self) -> float:
        return self._credits

    @property
    def capacity(self) -> int:
        return self._capacity

    @property
    def teacher_id(self) -> Optional[str]:
        return self._teacher_id

    @property
    def enrolled_student_ids(self) -> List[str]:
        return list(self._enrolled_student_ids)

    @property
    def enrollment_count(self) -> int:
        return len(self._enrolled_student_ids)

    @property
    def available_seats(self) -> int:
        return self._capacity - len(self._enrolled_student_ids)

    @property
    def is_full(self) -> bool:
        return len(self._enrolled_student_ids) >= self._capacity

    @property
    def waitlist_count(self) -> int:
        return len(self._waitlist_ids)

    @property
    def is_enrollment_open(self) -> bool:
        return self._is_enrollment_open

    @property
    def assessments(self) -> List[Assessment]:
        return list(self._assessments)

    @property
    def total_assessment_weight(self) -> float:
        """Sum of all assessment weights — should equal 100.0."""
        return sum(a.weight for a in self._assessments)

    # -----------------------------------------------------------------------
    # Teacher assignment
    # -----------------------------------------------------------------------

    def assign_teacher(self, teacher_id: str) -> None:
        """Assign a teacher to this course."""
        self._teacher_id = teacher_id

    def unassign_teacher(self) -> None:
        """Remove the teacher assignment."""
        self._teacher_id = None

    # -----------------------------------------------------------------------
    # Enrollment management
    # -----------------------------------------------------------------------

    def open_enrollment(self) -> None:
        """Open enrollment for this course."""
        self._is_enrollment_open = True

    def close_enrollment(self) -> None:
        """Close enrollment."""
        self._is_enrollment_open = False

    @contextmanager
    def enrollment_period(self) -> Generator:
        """Context manager for managing an enrollment period.

        Usage:
            with course.enrollment_period():
                course.enroll(student_id)
                course.enroll(another_id)
            # Enrollment automatically closes after the `with` block

        This demonstrates the context manager pattern (__enter__/__exit__).
        """
        self.open_enrollment()
        try:
            yield self
        finally:
            self.close_enrollment()

    def enroll(self, student_id: str) -> str:
        """Enroll a student in this course.

        If the course is full, adds to waitlist.

        Returns:
            "enrolled", "waitlisted", or "already_enrolled"
        """
        if not self._is_enrollment_open:
            raise RuntimeError(f"Enrollment for {self._course_id} is not open")

        if student_id in self._enrolled_student_ids:
            return "already_enrolled"

        if student_id in self._waitlist_ids:
            return "waitlisted"

        if self.is_full:
            self._waitlist_ids.append(student_id)
            return "waitlisted"

        self._enrolled_student_ids.append(student_id)
        return "enrolled"

    def unenroll(self, student_id: str) -> None:
        """Remove a student from the course.

        If there's a waitlisted student, moves them to enrolled.
        """
        if student_id not in self._enrolled_student_ids:
            raise ValueError(f"Student {student_id} is not enrolled in {self._course_id}")

        self._enrolled_student_ids.remove(student_id)

        # Promote from waitlist if available
        if self._waitlist_ids:
            next_student = self._waitlist_ids.pop(0)
            self._enrolled_student_ids.append(next_student)

    # -----------------------------------------------------------------------
    # Assessment management
    # -----------------------------------------------------------------------

    def add_assessment(self, assessment: Assessment) -> None:
        """Add an assessment to this course.

        Raises:
            ValueError: If total weights would exceed 100%
        """
        new_total = self.total_assessment_weight + assessment.weight
        if new_total > 100.0 + 1e-9:   # Allow small float error
            raise ValueError(
                f"Adding {assessment.name} ({assessment.weight}%) would exceed 100% "
                f"(current: {self.total_assessment_weight}%)"
            )
        self._assessments.append(assessment)

    def record_score(
        self, student_id: str, assessment_name: str, score: float
    ) -> None:
        """Record a score for a student on an assessment.

        Raises:
            ValueError: If student is not enrolled or assessment doesn't exist
        """
        if student_id not in self._enrolled_student_ids:
            raise ValueError(f"Student {student_id} is not enrolled in {self._course_id}")

        # Find the assessment
        assessment = next(
            (a for a in self._assessments if a.name == assessment_name),
            None
        )
        if assessment is None:
            raise ValueError(f"Assessment {assessment_name!r} not found in {self._course_id}")

        # Get or create grade record for this student
        if student_id not in self._grades:
            self._grades[student_id] = StudentGrade(student_id)

        self._grades[student_id].add_score(
            assessment_name, score, assessment.max_score
        )

    def calculate_final_grade(self, student_id: str) -> Optional[float]:
        """Calculate weighted final grade for a student.

        Returns:
            Final grade 0-100, or None if no grades recorded
        """
        if student_id not in self._grades:
            return None

        student_grade = self._grades[student_id]
        if not student_grade.scores:
            return None

        total_score = 0.0
        total_weight = 0.0

        for assessment in self._assessments:
            if assessment.name in student_grade.scores:
                score = student_grade.scores[assessment.name]
                percentage = (score / assessment.max_score) * 100
                total_score += percentage * assessment.weight
                total_weight += assessment.weight

        if total_weight == 0:
            return None

        return total_score / total_weight

    def get_grade_summary(self) -> str:
        """Get a summary of grades for all enrolled students."""
        if not self._enrolled_student_ids:
            return f"{self._course_id}: No students enrolled"

        lines = [f"Grade Summary for {self._name} ({self._course_id})"]
        lines.append(f"{'=' * 50}")

        grades = []
        for student_id in self._enrolled_student_ids:
            grade = self.calculate_final_grade(student_id)
            grades.append((student_id, grade))

        for student_id, grade in sorted(grades, key=lambda x: x[0]):
            if grade is not None:
                lines.append(f"  {student_id}: {grade:.1f}")
            else:
                lines.append(f"  {student_id}: No grades yet")

        return "\n".join(lines)

    # -----------------------------------------------------------------------
    # Dunder methods
    # -----------------------------------------------------------------------

    def __str__(self) -> str:
        teacher_info = f" (Teacher: {self._teacher_id})" if self._teacher_id else " (No teacher)"
        return (
            f"{self._course_id}: {self._name}"
            f"{teacher_info} "
            f"[{self.enrollment_count}/{self._capacity}]"
        )

    def __repr__(self) -> str:
        return (
            f"Course(id={self._course_id!r}, "
            f"name={self._name!r}, "
            f"credits={self._credits})"
        )

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Course):
            return NotImplemented
        return self._course_id == other._course_id

    def __hash__(self) -> int:
        return hash(self._course_id)

    def __len__(self) -> int:
        """Number of enrolled students."""
        return self.enrollment_count
