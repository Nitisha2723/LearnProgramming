"""
school_demo.py — Complete demonstration of the school management system.

Run this file:
    python mini-project/school_system/school_demo.py
"""

import sys
import os

# Add school_system directory to path
sys.path.insert(0, os.path.dirname(__file__))

from datetime import date
from student import Student
from teacher import Teacher
from course import Course, Assessment
from school import School


def separator(title: str = "") -> None:
    if title:
        print(f"\n{'─' * 20} {title} {'─' * 20}")
    else:
        print(f"\n{'─' * 60}")


def create_sample_school() -> School:
    """Create a school with sample data."""
    school = School("Python Academy", "123 Learning Lane, Berlin")

    # -----------------------------------------------------------------------
    # Create teachers
    # -----------------------------------------------------------------------
    dr_smith = Teacher(
        teacher_id="T001",
        first_name="Sarah",
        last_name="Smith",
        date_of_birth=date(1975, 3, 15),
        email="s.smith@academy.edu",
        department="Computer Science",
        annual_salary=75_000.0,
        title="Professor",
        qualifications=["PhD Computer Science", "MSc Software Engineering"],
    )

    prof_jones = Teacher(
        teacher_id="T002",
        first_name="Michael",
        last_name="Jones",
        date_of_birth=date(1980, 7, 22),
        email="m.jones@academy.edu",
        department="Mathematics",
        annual_salary=65_000.0,
        title="Associate Professor",
        qualifications=["PhD Mathematics", "BSc Physics"],
    )

    # Using the factory method for adjunct professor
    adj_teacher = Teacher.create_adjunct(
        teacher_id="T003",
        first_name="Emma",
        last_name="Brown",
        dob=date(1988, 11, 30),
        email="e.brown@academy.edu",
        department="Computer Science",
        per_course_pay=5000.0,
    )

    school.add_teacher(dr_smith)
    school.add_teacher(prof_jones)
    school.add_teacher(adj_teacher)

    # -----------------------------------------------------------------------
    # Create courses
    # -----------------------------------------------------------------------
    python_101 = Course(
        course_id="CS101",
        name="Introduction to Python",
        department="Computer Science",
        credits=3.0,
        capacity=25,
        description="Learn Python from scratch.",
    )

    oop_201 = Course(
        course_id="CS201",
        name="Object-Oriented Programming",
        department="Computer Science",
        credits=3.0,
        capacity=20,
    )

    math_101 = Course(
        course_id="MATH101",
        name="Calculus I",
        department="Mathematics",
        credits=4.0,
        capacity=30,
    )

    # Add assessments to courses
    python_101.add_assessment(Assessment("Midterm Exam", 100, 30))
    python_101.add_assessment(Assessment("Final Exam", 100, 40))
    python_101.add_assessment(Assessment("Project", 50, 20))
    python_101.add_assessment(Assessment("Homework", 100, 10))

    oop_201.add_assessment(Assessment("Midterm", 100, 30))
    oop_201.add_assessment(Assessment("Final", 100, 40))
    oop_201.add_assessment(Assessment("Project", 100, 30))

    math_101.add_assessment(Assessment("Midterm", 100, 35))
    math_101.add_assessment(Assessment("Final", 100, 50))
    math_101.add_assessment(Assessment("Quizzes", 100, 15))

    school.add_course(python_101)
    school.add_course(oop_201)
    school.add_course(math_101)

    # Assign teachers to courses
    school.assign_teacher_to_course("T001", "CS101")
    school.assign_teacher_to_course("T001", "CS201")
    school.assign_teacher_to_course("T002", "MATH101")

    # -----------------------------------------------------------------------
    # Create students
    # -----------------------------------------------------------------------
    students_data = [
        ("S001", "Alice", "Anderson", date(2000, 5, 10), "alice@student.edu"),
        ("S002", "Bob", "Baker", date(2001, 8, 22), "bob@student.edu"),
        ("S003", "Carol", "Chen", date(1999, 12, 1), "carol@student.edu"),
        ("S004", "David", "Davis", date(2001, 3, 15), "david@student.edu"),
        ("S005", "Eve", "Evans", date(2000, 7, 4), "eve@student.edu"),
    ]

    for sid, first, last, dob, email in students_data:
        student = Student(
            student_id=sid,
            first_name=first,
            last_name=last,
            date_of_birth=dob,
            email=email,
            major="Computer Science",
        )
        school.add_student(student)

    return school


def demo_overview(school: School) -> None:
    """Show school overview."""
    separator("School Overview")
    print(school)
    print()
    print(school.get_school_summary())


def demo_enrollment(school: School) -> None:
    """Demonstrate course enrollment."""
    separator("Course Enrollment")

    python_course = school.get_course("CS101")
    oop_course = school.get_course("CS201")

    print(f"Before enrollment: {python_course}")

    # Use context manager for enrollment period
    print("\nOpening enrollment period for CS101...")
    with python_course.enrollment_period():
        for student_id in ["S001", "S002", "S003", "S004", "S005"]:
            result = school.enroll_student_in_course(student_id, "CS101")
            student = school.get_student(student_id)
            print(f"  {student.full_name}: {result}")

    print(f"\nAfter enrollment: {python_course}")
    print(f"Is enrollment still open? {python_course.is_enrollment_open}")

    # Enroll some students in OOP as well
    with oop_course.enrollment_period():
        school.enroll_student_in_course("S001", "CS201")
        school.enroll_student_in_course("S003", "CS201")
        school.enroll_student_in_course("S005", "CS201")


def demo_grades(school: School) -> None:
    """Demonstrate grade recording."""
    separator("Grade Recording")

    course = school.get_course("CS101")

    # Record scores for students
    grades_data = {
        "S001": {"Midterm Exam": 88, "Final Exam": 92, "Project": 45, "Homework": 95},
        "S002": {"Midterm Exam": 72, "Final Exam": 78, "Project": 40, "Homework": 85},
        "S003": {"Midterm Exam": 95, "Final Exam": 98, "Project": 50, "Homework": 100},
        "S004": {"Midterm Exam": 65, "Final Exam": 70, "Project": 38, "Homework": 75},
        "S005": {"Midterm Exam": 80, "Final Exam": 85, "Project": 44, "Homework": 90},
    }

    for student_id, scores in grades_data.items():
        for assessment, score in scores.items():
            course.record_score(student_id, assessment, score)

    print(course.get_grade_summary())

    # Complete the course (move grades to student transcript)
    print("\nCompleting CS101 for all students...")
    for student_id in ["S001", "S002", "S003", "S004", "S005"]:
        final_grade = course.calculate_final_grade(student_id)
        school.complete_course(student_id, "CS101", "Winter 2024", final_grade)
        student = school.get_student(student_id)
        print(f"  {student.full_name}: {final_grade:.1f} → GPA: {student.gpa:.2f}")


def demo_student_transcript(school: School) -> None:
    """Show a student's transcript."""
    separator("Student Transcript")

    alice = school.get_student("S001")
    print(alice.get_transcript())
    print()
    print(alice.describe())


def demo_teacher_info(school: School) -> None:
    """Show teacher information."""
    separator("Teacher Information")

    for teacher in school.get_all_teachers():
        print(teacher.describe())
        print()


def demo_analytics(school: School) -> None:
    """Show school analytics."""
    separator("School Analytics")

    print("Top students by GPA:")
    for i, student in enumerate(school.get_top_students(3), 1):
        print(f"  {i}. {student.full_name} — GPA: {student.gpa:.2f} ({student.standing})")

    print("\nDepartment summary:")
    for dept, count in school.get_department_summary().items():
        courses = school.get_courses_by_department(dept)
        print(f"  {dept}: {count} teachers, {len(courses)} courses")

    print(f"\nTotal people in school: {len(school)}")

    alice = school.get_student("S001")
    dr_smith = school.get_teacher("T001")
    print(f"\nIs Alice in school? {alice in school}")
    print(f"Is Dr. Smith in school? {dr_smith in school}")


def demo_dunder_methods(school: School) -> None:
    """Show various dunder methods."""
    separator("Dunder Methods")

    # __len__ on Course
    course = school.get_course("CS101")
    print(f"Students enrolled in CS101: len(course) = {len(course)}")

    # Sorting students (uses Person.__lt__ — sort by last name)
    students = school.get_all_students()
    print("\nStudents sorted by name:")
    for s in students:
        print(f"  {s.full_name} — {s.standing}, GPA: {s.gpa:.2f}")

    # __eq__ on Course
    c1 = school.get_course("CS101")
    c2 = school.get_course("CS201")
    print(f"\nCS101 == CS101: {c1 == c1}")
    print(f"CS101 == CS201: {c1 == c2}")


if __name__ == "__main__":
    print("School Management System Demo")
    print("=" * 60)

    # Create the school with all data
    school = create_sample_school()

    demo_overview(school)
    demo_enrollment(school)
    demo_grades(school)
    demo_student_transcript(school)
    demo_teacher_info(school)
    demo_analytics(school)
    demo_dunder_methods(school)

    print("\n" + "=" * 60)
    print("Demo complete!")
