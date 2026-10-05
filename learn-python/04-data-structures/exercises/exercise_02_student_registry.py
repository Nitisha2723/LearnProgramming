"""
Exercise 02: Student Registry
==============================

Build a student grade registry system using dicts, defaultdict, and Counter.

Learning goals:
  - Nested dicts
  - defaultdict for grouping
  - Counter for statistics
  - Sorting complex data structures

Complete the StudentRegistry class below.
"""

from collections import defaultdict, Counter
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class Student:
    """Represents a single student."""
    student_id: str
    name: str
    grade: int        # School grade (e.g., 10, 11, 12)


@dataclass
class CourseRecord:
    """A student's record in a specific course."""
    student_id: str
    course: str
    scores: list[float] = field(default_factory=list)

    @property
    def average(self) -> Optional[float]:
        if not self.scores:
            return None
        return sum(self.scores) / len(self.scores)

    @property
    def letter_grade(self) -> str:
        avg = self.average
        if avg is None:
            return "N/A"
        if avg >= 90: return "A"
        if avg >= 80: return "B"
        if avg >= 70: return "C"
        if avg >= 60: return "D"
        return "F"


class StudentRegistry:
    """
    A registry that tracks students and their grades across courses.

    Internal structure (you decide how to implement):
    - Students are identified by student_id
    - Each student can be enrolled in multiple courses
    - Each course can have multiple scores per student
    """

    def __init__(self):
        # YOUR CODE HERE — decide on the data structures to use
        pass

    def add_student(self, student: Student) -> None:
        """
        Add a student to the registry.
        If student_id already exists, raise ValueError.
        """
        # YOUR CODE HERE
        pass

    def enroll(self, student_id: str, course: str) -> None:
        """
        Enroll a student in a course.
        If student doesn't exist, raise ValueError.
        If already enrolled, do nothing.
        """
        # YOUR CODE HERE
        pass

    def add_score(self, student_id: str, course: str, score: float) -> None:
        """
        Add a score for a student in a course.
        Raises ValueError if student or course enrollment doesn't exist.
        Score must be between 0 and 100.
        """
        # YOUR CODE HERE
        pass

    def get_student_gpa(self, student_id: str) -> Optional[float]:
        """
        Return the overall GPA of a student (average across all enrolled courses
        that have at least one score).
        Return None if no scores exist.
        """
        # YOUR CODE HERE
        pass

    def get_course_average(self, course: str) -> Optional[float]:
        """
        Return the average score across all students enrolled in a course.
        Return None if the course has no scores.
        """
        # YOUR CODE HERE
        pass

    def get_top_students(self, n: int) -> list[tuple[str, float]]:
        """
        Return the top N students by GPA.
        Returns a list of (student_name, gpa) tuples, sorted by GPA descending.
        Only include students with at least one score.
        """
        # YOUR CODE HERE
        pass

    def get_students_by_grade(self, school_grade: int) -> list[Student]:
        """
        Return all students in a given school grade, sorted by name.
        """
        # YOUR CODE HERE
        pass

    def get_grade_distribution(self, course: str) -> dict[str, int]:
        """
        Return the distribution of letter grades for a course.
        Returns a dict like {"A": 5, "B": 8, "C": 3, ...}.
        Only include grades that appear at least once.
        """
        # YOUR CODE HERE
        pass

    def class_report(self) -> str:
        """
        Return a formatted report of all students and their averages.
        Format:
            Student Registry Report
            =======================
            Alice (Grade 10) — GPA: 88.5
            Bob (Grade 11) — GPA: 74.2
            ...
        Sorted by GPA descending. Students with no scores shown as "No scores".
        """
        # YOUR CODE HERE
        pass


# =============================================================================
# TESTS
# =============================================================================

def build_sample_registry() -> StudentRegistry:
    registry = StudentRegistry()

    students = [
        Student("s001", "Alice",  10),
        Student("s002", "Bob",    11),
        Student("s003", "Carol",  10),
        Student("s004", "Dave",   12),
        Student("s005", "Eve",    11),
    ]
    for s in students:
        registry.add_student(s)

    # Enroll
    for sid in ["s001", "s002", "s003", "s004", "s005"]:
        registry.enroll(sid, "math")
    for sid in ["s001", "s003", "s005"]:
        registry.enroll(sid, "science")
    for sid in ["s002", "s004"]:
        registry.enroll(sid, "history")

    # Add scores
    scores = {
        ("s001", "math"):    [85, 90, 88],
        ("s002", "math"):    [70, 75, 72],
        ("s003", "math"):    [95, 92, 98],
        ("s004", "math"):    [60, 65, 55],
        ("s005", "math"):    [80, 82, 79],
        ("s001", "science"): [88, 91, 85],
        ("s003", "science"): [96, 94, 98],
        ("s005", "science"): [75, 78, 80],
        ("s002", "history"): [82, 85, 80],
        ("s004", "history"): [70, 68, 72],
    }
    for (sid, course), score_list in scores.items():
        for score in score_list:
            registry.add_score(sid, course, score)

    return registry


def test_student_registry():
    reg = build_sample_registry()

    # Test GPAs
    alice_gpa = reg.get_student_gpa("s001")
    assert alice_gpa is not None
    assert abs(alice_gpa - 87.83) < 0.1, f"Alice GPA expected ~87.83, got {alice_gpa:.2f}"

    # Test course average
    math_avg = reg.get_course_average("math")
    assert math_avg is not None
    assert abs(math_avg - 77.2) < 0.5, f"Math avg expected ~77.2, got {math_avg:.2f}"

    # Test top students
    top = reg.get_top_students(3)
    assert len(top) == 3
    assert top[0][0] == "Carol", f"Expected Carol at top, got {top[0][0]}"

    # Test grade filter
    grade_10 = reg.get_students_by_grade(10)
    assert len(grade_10) == 2
    assert {s.name for s in grade_10} == {"Alice", "Carol"}

    # Test grade distribution
    math_dist = reg.get_grade_distribution("math")
    assert isinstance(math_dist, dict)
    assert sum(math_dist.values()) == 5   # 5 students

    # Test duplicate student
    try:
        reg.add_student(Student("s001", "Duplicate", 10))
        assert False, "Should raise ValueError"
    except ValueError:
        pass

    print("  StudentRegistry: PASSED")


if __name__ == "__main__":
    print("Running tests...\n")
    try:
        test_student_registry()
        print("\nAll tests passed!")

        # Show demo
        reg = build_sample_registry()
        print("\n" + reg.class_report())
        print("\nMath grade distribution:", reg.get_grade_distribution("math"))
    except AssertionError as e:
        print(f"\nTest FAILED: {e}")
    except TypeError:
        print("\nTest FAILED: function returned None — did you implement all methods?")
