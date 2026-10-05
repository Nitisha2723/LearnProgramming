"""Demo for Student Management System."""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))

from service.student_service import StudentService
from models.enrollment import Grade


def main() -> None:
    svc = StudentService()

    # Add courses
    math = svc.add_course("Calculus I", "MATH101", credits=4)
    cs   = svc.add_course("Data Structures", "CS201", credits=3)
    eng  = svc.add_course("Technical Writing", "ENG101", credits=2)

    # Add students
    alice = svc.add_student("Alice Smith", "alice@uni.edu", "Computer Science")
    bob   = svc.add_student("Bob Jones",  "bob@uni.edu",   "Mathematics")

    # Enroll
    e1 = svc.enroll(alice.id, cs.id)
    e2 = svc.enroll(alice.id, math.id)
    e3 = svc.enroll(bob.id,   math.id)
    e4 = svc.enroll(bob.id,   eng.id)

    # Assign grades
    svc.assign_grade(e1.id, Grade.A)
    svc.assign_grade(e2.id, Grade.B_PLUS)
    svc.assign_grade(e3.id, Grade.A_MINUS)
    svc.assign_grade(e4.id, Grade.B)

    # Print GPAs
    for student in svc.get_all_students():
        gpa = svc.calculate_gpa(student.id)
        print(f"{student.name}: GPA = {gpa:.2f}" if gpa else f"{student.name}: no grades")

    # Export CSV
    csv_data = svc.export_students_csv()
    print("\n--- CSV Export ---")
    print(csv_data)


if __name__ == "__main__":
    main()
