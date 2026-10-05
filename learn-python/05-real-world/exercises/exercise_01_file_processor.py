"""
Exercise 01: File Processor
============================

Read a CSV file of students and their grades, compute statistics,
and write a formatted report.

Learning goals:
  - Reading CSV with csv.DictReader
  - pathlib for file operations
  - Exception handling for malformed data
  - Writing formatted output

The input CSV has these columns: name, math, science, english, history
Some rows may have missing or invalid values — handle them gracefully.

Complete the functions below.
"""

import csv
from pathlib import Path
from typing import Optional


# ---------------------------------------------------------------------------
# Data types
# ---------------------------------------------------------------------------

class StudentData:
    """Holds a student's name and their valid scores."""

    def __init__(self, name: str, scores: dict[str, Optional[float]]):
        self.name = name
        self.scores = scores  # {subject: score} — None if missing/invalid

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


# ---------------------------------------------------------------------------
# Functions to implement
# ---------------------------------------------------------------------------

def read_grades(csv_path: Path) -> list[StudentData]:
    """
    Read a CSV file and return a list of StudentData objects.

    The CSV has columns: name, math, science, english, history
    Handle these cases gracefully:
    - Missing file → raise FileNotFoundError
    - Missing score (empty cell) → store None for that subject
    - Non-numeric score → store None for that subject (don't crash)
    - Row with no name → skip the row

    Args:
        csv_path: Path to the CSV file

    Returns:
        List of StudentData objects, one per valid row
    """
    # YOUR CODE HERE
    pass


def compute_subject_stats(students: list[StudentData]) -> dict[str, dict]:
    """
    Compute statistics for each subject across all students.

    Returns a dict like:
    {
        "math": {
            "average": 82.5,
            "highest": 98.0,
            "lowest": 60.0,
            "count": 25,    # students with valid scores
        },
        ...
    }

    Only include students who have a valid (non-None) score for that subject.

    Args:
        students: List of StudentData objects

    Returns:
        Dict mapping subject name → stats dict
    """
    # YOUR CODE HERE
    pass


def write_report(
    students: list[StudentData],
    subject_stats: dict[str, dict],
    output_path: Path,
) -> None:
    """
    Write a formatted text report to output_path.

    Report format:
    =============================================
    GRADE REPORT
    =============================================

    STUDENTS (sorted by average, descending)
    -----------------------------------------
    Alice        A  avg=91.5  math=90 science=95 english=88 history=93
    Bob          B  avg=82.3  math=85 science=80 english=78 history=86
    ...
    Carol        N/A  (no valid scores)

    SUBJECT STATISTICS
    -----------------------------------------
    math:     avg=80.2  highest=98  lowest=55  (25 students)
    science:  avg=78.5  ...
    ...

    CLASS SUMMARY
    -----------------------------------------
    Total students: 30
    Class average:  81.2
    Grade A: 8 students
    Grade B: 12 students
    ...

    Args:
        students: List of StudentData
        subject_stats: Output of compute_subject_stats()
        output_path: Where to write the report
    """
    # YOUR CODE HERE
    pass


def process_grades(csv_path: Path, output_path: Path) -> dict:
    """
    Full pipeline: read CSV → compute stats → write report → return summary.

    Args:
        csv_path: Input CSV file path
        output_path: Output report file path

    Returns:
        Dict with: student_count, class_average, top_student, output_path
    """
    # YOUR CODE HERE
    pass


# ---------------------------------------------------------------------------
# Test data and tests
# ---------------------------------------------------------------------------

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


def create_test_csv(tmpdir: Path) -> Path:
    """Create a test CSV in tmpdir and return its path."""
    csv_path = tmpdir / "grades.csv"
    csv_path.write_text(SAMPLE_CSV)
    return csv_path


def test_read_grades(tmpdir: Path):
    csv_path = create_test_csv(tmpdir)
    students = read_grades(csv_path)

    assert len(students) == 8, f"Expected 8 students, got {len(students)}"

    # Alice should have all 4 scores
    alice = next(s for s in students if s.name == "Alice")
    assert alice.scores["math"] == 90.0
    assert alice.average is not None

    # Grace has missing math score
    grace = next(s for s in students if s.name == "Grace")
    assert grace.scores["math"] is None
    assert grace.scores["science"] == 88.0

    # Henry has invalid science score
    henry = next(s for s in students if s.name == "Henry")
    assert henry.scores["science"] is None

    print("  read_grades: PASSED")


def test_subject_stats(tmpdir: Path):
    csv_path = create_test_csv(tmpdir)
    students = read_grades(csv_path)
    stats = compute_subject_stats(students)

    assert "math" in stats
    assert "science" in stats

    # Math: 7 students have valid scores (Grace is missing)
    assert stats["math"]["count"] == 7, f"Expected 7 for math, got {stats['math']['count']}"

    # Dave has the highest math: 95
    assert stats["math"]["highest"] == 95.0

    print("  compute_subject_stats: PASSED")


def test_write_report(tmpdir: Path):
    csv_path = create_test_csv(tmpdir)
    output_path = tmpdir / "report.txt"

    students = read_grades(csv_path)
    stats = compute_subject_stats(students)
    write_report(students, stats, output_path)

    assert output_path.exists(), "Report file should be created"
    content = output_path.read_text()
    assert "Alice" in content
    assert "SUBJECT STATISTICS" in content

    print("  write_report: PASSED")


def test_full_pipeline(tmpdir: Path):
    csv_path = create_test_csv(tmpdir)
    output_path = tmpdir / "full_report.txt"

    result = process_grades(csv_path, output_path)

    assert result["student_count"] == 8
    assert result["top_student"] == "Dave"    # Dave has avg ~95.5
    assert output_path.exists()

    print("  process_grades: PASSED")


if __name__ == "__main__":
    import tempfile
    tmpdir = Path(tempfile.mkdtemp())

    print("Running tests...\n")
    try:
        test_read_grades(tmpdir)
        test_subject_stats(tmpdir)
        test_write_report(tmpdir)
        test_full_pipeline(tmpdir)
        print("\nAll tests passed!")
    except AssertionError as e:
        print(f"\nTest FAILED: {e}")
    except TypeError:
        print("\nTest FAILED: function returned None — implement the functions!")
    finally:
        import shutil
        shutil.rmtree(tmpdir)
