"""
Exercise 02: Student Registry — SOLUTION
==========================================
"""

from collections import defaultdict, Counter
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class Student:
    student_id: str
    name: str
    grade: int


@dataclass
class CourseRecord:
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
    def __init__(self):
        self._students: dict[str, Student] = {}
        # student_id -> course -> CourseRecord
        self._records: dict[str, dict[str, CourseRecord]] = defaultdict(dict)
        # grade -> list of student_ids
        self._by_grade: dict[int, list[str]] = defaultdict(list)

    def add_student(self, student: Student) -> None:
        if student.student_id in self._students:
            raise ValueError(f"Student {student.student_id} already exists")
        self._students[student.student_id] = student
        self._by_grade[student.grade].append(student.student_id)

    def enroll(self, student_id: str, course: str) -> None:
        if student_id not in self._students:
            raise ValueError(f"Student {student_id} not found")
        if course not in self._records[student_id]:
            self._records[student_id][course] = CourseRecord(student_id, course)

    def add_score(self, student_id: str, course: str, score: float) -> None:
        if student_id not in self._students:
            raise ValueError(f"Student {student_id} not found")
        if course not in self._records[student_id]:
            raise ValueError(f"Student {student_id} not enrolled in {course}")
        if not 0 <= score <= 100:
            raise ValueError(f"Score {score} must be between 0 and 100")
        self._records[student_id][course].scores.append(score)

    def get_student_gpa(self, student_id: str) -> Optional[float]:
        records = self._records[student_id]
        averages = [r.average for r in records.values() if r.average is not None]
        return sum(averages) / len(averages) if averages else None

    def get_course_average(self, course: str) -> Optional[float]:
        course_records = [
            self._records[sid][course]
            for sid in self._records
            if course in self._records[sid]
        ]
        all_scores = [
            score
            for record in course_records
            for score in record.scores
        ]
        return sum(all_scores) / len(all_scores) if all_scores else None

    def get_top_students(self, n: int) -> list[tuple[str, float]]:
        gpas = []
        for student_id, student in self._students.items():
            gpa = self.get_student_gpa(student_id)
            if gpa is not None:
                gpas.append((student.name, gpa))
        return sorted(gpas, key=lambda x: -x[1])[:n]

    def get_students_by_grade(self, school_grade: int) -> list[Student]:
        student_ids = self._by_grade.get(school_grade, [])
        students = [self._students[sid] for sid in student_ids]
        return sorted(students, key=lambda s: s.name)

    def get_grade_distribution(self, course: str) -> dict[str, int]:
        letter_grades = []
        for sid in self._records:
            if course in self._records[sid]:
                letter = self._records[sid][course].letter_grade
                if letter != "N/A":
                    letter_grades.append(letter)
        counter = Counter(letter_grades)
        return dict(counter)

    def class_report(self) -> str:
        lines = ["Student Registry Report", "=" * 23]
        # Sort by GPA descending
        student_gpas = []
        for student_id, student in self._students.items():
            gpa = self.get_student_gpa(student_id)
            student_gpas.append((student, gpa))
        student_gpas.sort(key=lambda x: (x[1] is None, -(x[1] or 0)))

        for student, gpa in student_gpas:
            if gpa is not None:
                lines.append(f"{student.name} (Grade {student.grade}) — GPA: {gpa:.1f}")
            else:
                lines.append(f"{student.name} (Grade {student.grade}) — No scores")
        return "\n".join(lines)


# =============================================================================
# Demonstration
# =============================================================================

def build_sample_registry() -> StudentRegistry:
    registry = StudentRegistry()
    students = [
        Student("s001", "Alice", 10),
        Student("s002", "Bob",   11),
        Student("s003", "Carol", 10),
        Student("s004", "Dave",  12),
        Student("s005", "Eve",   11),
    ]
    for s in students:
        registry.add_student(s)

    for sid in ["s001", "s002", "s003", "s004", "s005"]:
        registry.enroll(sid, "math")
    for sid in ["s001", "s003", "s005"]:
        registry.enroll(sid, "science")
    for sid in ["s002", "s004"]:
        registry.enroll(sid, "history")

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


if __name__ == "__main__":
    reg = build_sample_registry()
    print(reg.class_report())
    print(f"\nMath average: {reg.get_course_average('math'):.2f}")
    print(f"Math grade distribution: {reg.get_grade_distribution('math')}")
    print(f"\nTop 3 students: {reg.get_top_students(3)}")
    print(f"Grade 10 students: {[s.name for s in reg.get_students_by_grade(10)]}")
