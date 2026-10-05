"""
school.py — The main School class that coordinates everything.

Demonstrates:
- Composition: School has Teachers, Students, Courses
- Complex coordination between objects
- Data aggregation methods
- Type hints with generic types
"""

from datetime import date
from typing import Dict, List, Optional, Set, Tuple
from student import Student
from teacher import Teacher
from course import Course, Assessment


class School:
    """A school that manages students, teachers, and courses.

    The School is a coordinator class — it holds references to all entities
    and ensures consistency between them. This is the Facade pattern.

    School HAS MANY Students (composition)
    School HAS MANY Teachers (composition)
    School HAS MANY Courses (composition)
    """

    def __init__(self, name: str, address: str = "") -> None:
        """Initialize the school."""
        if not name.strip():
            raise ValueError("School name cannot be empty")

        self._name: str = name.strip()
        self._address: str = address

        # The main registries — dict for O(1) lookup by ID
        self._students: Dict[str, Student] = {}    # student_id → Student
        self._teachers: Dict[str, Teacher] = {}    # teacher_id → Teacher
        self._courses: Dict[str, Course] = {}      # course_id → Course

    # -----------------------------------------------------------------------
    # Properties
    # -----------------------------------------------------------------------

    @property
    def name(self) -> str:
        return self._name

    @property
    def address(self) -> str:
        return self._address

    @property
    def student_count(self) -> int:
        return len(self._students)

    @property
    def teacher_count(self) -> int:
        return len(self._teachers)

    @property
    def course_count(self) -> int:
        return len(self._courses)

    # -----------------------------------------------------------------------
    # Student management
    # -----------------------------------------------------------------------

    def add_student(self, student: Student) -> None:
        """Register a new student.

        Raises:
            ValueError: If a student with this ID already exists
        """
        if student.student_id in self._students:
            raise ValueError(
                f"Student with ID {student.student_id} already registered"
            )
        self._students[student.student_id] = student

    def get_student(self, student_id: str) -> Optional[Student]:
        """Find a student by ID."""
        return self._students.get(student_id)

    def remove_student(self, student_id: str) -> Student:
        """Remove and return a student from the school.

        Also removes them from all courses.
        """
        if student_id not in self._students:
            raise ValueError(f"Student {student_id} not found")

        student = self._students.pop(student_id)
        student.deactivate()

        # Remove from all courses
        for course in self._courses.values():
            if student_id in course.enrolled_student_ids:
                course.unenroll(student_id)

        return student

    def get_all_students(self, active_only: bool = True) -> List[Student]:
        """Get all students, optionally filtering to active only."""
        students = list(self._students.values())
        if active_only:
            students = [s for s in students if s.is_active]
        return sorted(students)   # Uses Person.__lt__ (sort by name)

    # -----------------------------------------------------------------------
    # Teacher management
    # -----------------------------------------------------------------------

    def add_teacher(self, teacher: Teacher) -> None:
        """Register a new teacher."""
        if teacher.teacher_id in self._teachers:
            raise ValueError(
                f"Teacher with ID {teacher.teacher_id} already registered"
            )
        self._teachers[teacher.teacher_id] = teacher

    def get_teacher(self, teacher_id: str) -> Optional[Teacher]:
        """Find a teacher by ID."""
        return self._teachers.get(teacher_id)

    def get_all_teachers(self, active_only: bool = True) -> List[Teacher]:
        """Get all teachers."""
        teachers = list(self._teachers.values())
        if active_only:
            teachers = [t for t in teachers if t.is_active]
        return sorted(teachers)

    # -----------------------------------------------------------------------
    # Course management
    # -----------------------------------------------------------------------

    def add_course(self, course: Course) -> None:
        """Register a new course."""
        if course.course_id in self._courses:
            raise ValueError(f"Course {course.course_id} already registered")
        self._courses[course.course_id] = course

    def get_course(self, course_id: str) -> Optional[Course]:
        """Find a course by ID."""
        return self._courses.get(course_id)

    def get_all_courses(self) -> List[Course]:
        """Get all courses."""
        return sorted(self._courses.values(), key=lambda c: c.course_id)

    def assign_teacher_to_course(
        self, teacher_id: str, course_id: str
    ) -> None:
        """Assign a teacher to a course.

        Updates both the Course and the Teacher objects to stay in sync.
        """
        teacher = self._teachers.get(teacher_id)
        if teacher is None:
            raise ValueError(f"Teacher {teacher_id} not found")

        course = self._courses.get(course_id)
        if course is None:
            raise ValueError(f"Course {course_id} not found")

        if not teacher.is_active:
            raise RuntimeError(f"Teacher {teacher.full_name} is not active")

        # Update both objects — course knows its teacher, teacher knows their courses
        course.assign_teacher(teacher_id)
        teacher.assign_course(course_id)

    # -----------------------------------------------------------------------
    # Enrollment management
    # -----------------------------------------------------------------------

    def enroll_student_in_course(
        self, student_id: str, course_id: str
    ) -> str:
        """Enroll a student in a course.

        Updates both the Student and the Course objects.

        Returns:
            "enrolled", "waitlisted", or "already_enrolled"
        """
        student = self._students.get(student_id)
        if student is None:
            raise ValueError(f"Student {student_id} not found")

        course = self._courses.get(course_id)
        if course is None:
            raise ValueError(f"Course {course_id} not found")

        if not student.is_active:
            raise RuntimeError(f"Student {student.full_name} is not active")

        result = course.enroll(student_id)

        # Update the student's record too (if actually enrolled, not waitlisted)
        if result == "enrolled":
            student.enroll_in_course(course_id)

        return result

    def complete_course(
        self,
        student_id: str,
        course_id: str,
        semester: str,
        final_grade: Optional[float] = None,
    ) -> None:
        """Mark a course as completed for a student.

        Calculates or uses provided final grade and records it on the student.
        """
        student = self._students.get(student_id)
        if student is None:
            raise ValueError(f"Student {student_id} not found")

        course = self._courses.get(course_id)
        if course is None:
            raise ValueError(f"Course {course_id} not found")

        # Calculate grade from course if not provided
        if final_grade is None:
            final_grade = course.calculate_final_grade(student_id)

        if final_grade is None:
            raise RuntimeError(
                f"No grade data available for student {student_id} in {course_id}"
            )

        # Record the grade on the student
        student.record_grade(
            course_id=course_id,
            course_name=course.name,
            grade=final_grade,
            semester=semester,
            credits=course.credits,
        )

    # -----------------------------------------------------------------------
    # Reports and analytics
    # -----------------------------------------------------------------------

    def get_department_summary(self) -> Dict[str, int]:
        """Return count of teachers per department."""
        summary: Dict[str, int] = {}
        for teacher in self._teachers.values():
            dept = teacher.department
            summary[dept] = summary.get(dept, 0) + 1
        return dict(sorted(summary.items()))

    def get_top_students(self, n: int = 5) -> List[Student]:
        """Get the top N students by GPA."""
        students = [s for s in self._students.values() if s.is_active]
        return sorted(students, key=lambda s: s.gpa, reverse=True)[:n]

    def get_courses_by_department(self, department: str) -> List[Course]:
        """Get all courses in a department."""
        return [
            c for c in self._courses.values()
            if c.department.lower() == department.lower()
        ]

    def get_school_summary(self) -> str:
        """Generate a comprehensive school summary."""
        active_students = [s for s in self._students.values() if s.is_active]
        active_teachers = [t for t in self._teachers.values() if t.is_active]

        avg_gpa = (
            sum(s.gpa for s in active_students) / len(active_students)
            if active_students else 0.0
        )

        total_payroll = sum(t.annual_salary for t in active_teachers)

        lines = [
            f"School: {self._name}",
            f"{'=' * 50}",
            f"Students: {len(active_students)} active",
            f"Teachers: {len(active_teachers)} active",
            f"Courses:  {len(self._courses)} offered",
            f"",
            f"Average Student GPA: {avg_gpa:.2f}",
            f"Annual Teacher Payroll: ${total_payroll:,.2f}",
            f"",
            f"Departments: {', '.join(self.get_department_summary().keys())}",
        ]

        return "\n".join(lines)

    # -----------------------------------------------------------------------
    # Dunder methods
    # -----------------------------------------------------------------------

    def __str__(self) -> str:
        return (
            f"{self._name} — "
            f"{self.student_count} students, "
            f"{self.teacher_count} teachers, "
            f"{self.course_count} courses"
        )

    def __repr__(self) -> str:
        return f"School(name={self._name!r})"

    def __len__(self) -> int:
        """Total number of people (students + teachers)."""
        return self.student_count + self.teacher_count

    def __contains__(self, item: object) -> bool:
        """Check if a student or teacher is registered."""
        if isinstance(item, Student):
            return item.student_id in self._students
        elif isinstance(item, Teacher):
            return item.teacher_id in self._teachers
        return False
