from __future__ import annotations

import csv
import io
from typing import Dict, List, Optional

try:
    from ..models.student import Student
    from ..models.course import Course
    from ..models.enrollment import Enrollment, Grade
except ImportError:
    from models.student import Student  # type: ignore
    from models.course import Course  # type: ignore
    from models.enrollment import Enrollment, Grade  # type: ignore


class StudentService:
    """
    In-memory student management service with CSV import/export.
    Combines repository + service for simplicity in this project.
    """

    def __init__(self) -> None:
        self._students: Dict[str, Student] = {}
        self._courses: Dict[str, Course] = {}
        self._enrollments: Dict[str, Enrollment] = {}

    # ── Students ────────────────────────────────────────────────────────────

    def add_student(self, name: str, email: str, department: str) -> Student:
        student = Student(name=name, email=email, department=department)
        self._students[student.id] = student
        return student

    def get_student(self, student_id: str) -> Optional[Student]:
        return self._students.get(student_id)

    def get_all_students(self) -> List[Student]:
        return sorted(self._students.values(), key=lambda s: s.name)

    def get_active_students(self) -> List[Student]:
        return [s for s in self._students.values() if s.active]

    # ── Courses ─────────────────────────────────────────────────────────────

    def add_course(self, name: str, code: str, credits: int = 3) -> Course:
        course = Course(name=name, code=code, credits=credits)
        self._courses[course.id] = course
        return course

    def get_all_courses(self) -> List[Course]:
        return list(self._courses.values())

    # ── Enrollments ──────────────────────────────────────────────────────────

    def enroll(self, student_id: str, course_id: str) -> Enrollment:
        if student_id not in self._students:
            raise ValueError(f"Student not found: {student_id}")
        if course_id not in self._courses:
            raise ValueError(f"Course not found: {course_id}")
        # Check for existing enrollment
        for e in self._enrollments.values():
            if e.student_id == student_id and e.course_id == course_id:
                raise ValueError("Student already enrolled in this course.")
        enrollment = Enrollment(student_id=student_id, course_id=course_id)
        self._enrollments[enrollment.id] = enrollment
        return enrollment

    def assign_grade(self, enrollment_id: str, grade: Grade) -> Enrollment:
        enrollment = self._enrollments.get(enrollment_id)
        if enrollment is None:
            raise ValueError(f"Enrollment not found: {enrollment_id}")
        enrollment.assign_grade(grade)
        return enrollment

    def get_student_enrollments(self, student_id: str) -> List[Enrollment]:
        return [e for e in self._enrollments.values() if e.student_id == student_id]

    # ── GPA Calculation ──────────────────────────────────────────────────────

    def calculate_gpa(self, student_id: str) -> Optional[float]:
        """
        Calculate weighted GPA using course credits.
        Returns None if no graded courses.
        """
        enrollments = [
            e for e in self.get_student_enrollments(student_id)
            if e.is_graded
        ]
        if not enrollments:
            return None

        total_points = 0.0
        total_credits = 0
        for e in enrollments:
            course = self._courses.get(e.course_id)
            if course:
                total_points += e.grade.points * course.credits
                total_credits += course.credits

        return total_points / total_credits if total_credits > 0 else None

    # ── CSV Import / Export ──────────────────────────────────────────────────

    def export_students_csv(self) -> str:
        """Export all students to CSV string."""
        output = io.StringIO()
        writer = csv.DictWriter(
            output,
            fieldnames=["student_number", "name", "email", "department", "active", "gpa"]
        )
        writer.writeheader()
        for student in self.get_all_students():
            gpa = self.calculate_gpa(student.id)
            writer.writerow({
                "student_number": student.student_number,
                "name": student.name,
                "email": student.email,
                "department": student.department,
                "active": student.active,
                "gpa": f"{gpa:.2f}" if gpa is not None else "N/A",
            })
        return output.getvalue()

    def import_students_csv(self, csv_content: str) -> List[Student]:
        """Import students from CSV string. Returns list of imported students."""
        imported = []
        reader = csv.DictReader(io.StringIO(csv_content))
        for row in reader:
            student = self.add_student(
                name=row["name"],
                email=row["email"],
                department=row["department"],
            )
            imported.append(student)
        return imported

    # ── Reports ──────────────────────────────────────────────────────────────

    def report_by_department(self) -> Dict[str, List[Student]]:
        """Group students by department."""
        result: Dict[str, List[Student]] = {}
        for s in self._students.values():
            result.setdefault(s.department, []).append(s)
        return result

    def report_top_students(self, n: int = 10) -> List[dict]:
        """Return top N students by GPA."""
        records = []
        for student in self._students.values():
            gpa = self.calculate_gpa(student.id)
            if gpa is not None:
                records.append({"student": student, "gpa": gpa})
        return sorted(records, key=lambda r: r["gpa"], reverse=True)[:n]
