"""
Exercise 01: File Processor — SOLUTION
========================================
"""

import csv
from pathlib import Path
from typing import Optional
from collections import defaultdict


class StudentData:
    def __init__(self, name: str, scores: dict[str, Optional[float]]):
        self.name = name
        self.scores = scores

    @property
    def subjects(self) -> list[str]:
        return [s for s, v in self.scores.items() if v is not None]

    @property
    def average(self) -> Optional[float]:
        valid = [v for v in self.scores.values() if v is not None]
        return sum(valid) / len(valid) if valid else None

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


def read_grades(csv_path: Path) -> list[StudentData]:
    students = []
    subjects = ["math", "science", "english", "history"]

    with open(csv_path, newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row.get("name", "").strip()
            if not name:
                continue

            scores = {}
            for subject in subjects:
                raw = row.get(subject, "").strip()
                if not raw:
                    scores[subject] = None
                else:
                    try:
                        scores[subject] = float(raw)
                    except ValueError:
                        scores[subject] = None

            students.append(StudentData(name, scores))

    return students


def compute_subject_stats(students: list[StudentData]) -> dict[str, dict]:
    subject_scores = defaultdict(list)
    for student in students:
        for subject, score in student.scores.items():
            if score is not None:
                subject_scores[subject].append(score)

    stats = {}
    for subject, scores in subject_scores.items():
        if scores:
            stats[subject] = {
                "average": sum(scores) / len(scores),
                "highest": max(scores),
                "lowest":  min(scores),
                "count":   len(scores),
            }
    return stats


def write_report(
    students: list[StudentData],
    subject_stats: dict[str, dict],
    output_path: Path,
) -> None:
    subjects = ["math", "science", "english", "history"]
    lines = []

    # Header
    lines.append("=" * 55)
    lines.append("GRADE REPORT")
    lines.append("=" * 55)

    # Students section
    lines.append("\nSTUDENTS (sorted by average, descending)")
    lines.append("-" * 55)

    sorted_students = sorted(
        students,
        key=lambda s: (s.average is None, -(s.average or 0))
    )

    for s in sorted_students:
        if s.average is not None:
            scores_str = "  ".join(
                f"{sub}={s.scores[sub]:.0f}" if s.scores[sub] is not None else f"{sub}=N/A"
                for sub in subjects
            )
            lines.append(
                f"{s.name:<15} {s.letter_grade:<3}  avg={s.average:5.1f}  {scores_str}"
            )
        else:
            lines.append(f"{s.name:<15} N/A  (no valid scores)")

    # Subject stats
    lines.append("\nSUBJECT STATISTICS")
    lines.append("-" * 55)
    for subject in subjects:
        if subject in subject_stats:
            st = subject_stats[subject]
            lines.append(
                f"{subject:<12} avg={st['average']:5.1f}  "
                f"highest={st['highest']:5.1f}  "
                f"lowest={st['lowest']:5.1f}  "
                f"({st['count']} students)"
            )

    # Class summary
    lines.append("\nCLASS SUMMARY")
    lines.append("-" * 55)
    lines.append(f"Total students: {len(students)}")

    all_avgs = [s.average for s in students if s.average is not None]
    if all_avgs:
        class_avg = sum(all_avgs) / len(all_avgs)
        lines.append(f"Class average:  {class_avg:.1f}")

    grade_counts = defaultdict(int)
    for s in students:
        grade_counts[s.letter_grade] += 1

    for grade in ["A", "B", "C", "D", "F", "N/A"]:
        if grade_counts[grade]:
            lines.append(f"Grade {grade}: {grade_counts[grade]} student(s)")

    lines.append("=" * 55)
    output_path.write_text("\n".join(lines), encoding="utf-8")


def process_grades(csv_path: Path, output_path: Path) -> dict:
    students = read_grades(csv_path)
    stats = compute_subject_stats(students)
    write_report(students, stats, output_path)

    avgs = [s.average for s in students if s.average is not None]
    class_avg = sum(avgs) / len(avgs) if avgs else None

    top = max(
        (s for s in students if s.average is not None),
        key=lambda s: s.average,
        default=None,
    )

    return {
        "student_count": len(students),
        "class_average": class_avg,
        "top_student": top.name if top else None,
        "output_path": str(output_path),
    }


# Demonstration
if __name__ == "__main__":
    import tempfile, shutil

    SAMPLE_CSV = """\
name,math,science,english,history
Alice,90,95,88,93
Bob,85,80,78,86
Carol,70,72,68,75
Dave,95,98,92,97
Eve,60,65,58,62
Frank,82,79,85,81
Grace,,88,90,85
Henry,75,invalid,80,78
"""

    tmpdir = Path(tempfile.mkdtemp())
    try:
        csv_path = tmpdir / "grades.csv"
        csv_path.write_text(SAMPLE_CSV)

        output_path = tmpdir / "report.txt"
        result = process_grades(csv_path, output_path)

        print(output_path.read_text())
        print(f"\nSummary: {result}")
    finally:
        shutil.rmtree(tmpdir)
