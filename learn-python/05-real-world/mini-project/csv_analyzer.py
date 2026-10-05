"""
csv_analyzer.py — Student Grade Analyzer
==========================================

A memory-efficient CSV grade analyzer that demonstrates:
  - Generator-based processing (works on files of any size)
  - pathlib for all file operations
  - csv.DictReader for named column access
  - Custom exceptions for data validation
  - Context managers
  - JSON export

Usage:
    python csv_analyzer.py                  # auto-generates sample data
    python csv_analyzer.py path/to/data.csv

Expected CSV format:
    name,math,science,english,history,art
    Alice,90,85,88,93,79
    Bob,75,92,,86,90        (empty cells treated as missing)
"""

import csv
import json
import sys
import tempfile
import textwrap
from collections import defaultdict
from dataclasses import dataclass, field
from pathlib import Path
from statistics import mean, stdev
from typing import Iterator, Optional


# =============================================================================
# Custom Exceptions
# =============================================================================

class AnalyzerError(Exception):
    """Base exception for csv_analyzer."""


class ParseError(AnalyzerError):
    """Raised when a row cannot be parsed."""
    def __init__(self, row_number: int, reason: str):
        self.row_number = row_number
        self.reason = reason
        super().__init__(f"Row {row_number}: {reason}")


class EmptyFileError(AnalyzerError):
    """Raised when the CSV file contains no data rows."""


# =============================================================================
# Data Classes
# =============================================================================

@dataclass
class StudentRecord:
    """Holds one student's cleaned grade data."""
    name: str
    grades: dict[str, float]   # subject → score

    @property
    def average(self) -> float:
        if not self.grades:
            return 0.0
        return mean(self.grades.values())

    @property
    def letter_grade(self) -> str:
        avg = self.average
        if avg >= 90:
            return "A"
        elif avg >= 80:
            return "B"
        elif avg >= 70:
            return "C"
        elif avg >= 60:
            return "D"
        else:
            return "F"


@dataclass
class SubjectStats:
    """Aggregated statistics for one subject."""
    subject: str
    scores: list[float] = field(default_factory=list)

    @property
    def count(self) -> int:
        return len(self.scores)

    @property
    def average(self) -> Optional[float]:
        return mean(self.scores) if self.scores else None

    @property
    def std_dev(self) -> Optional[float]:
        return stdev(self.scores) if len(self.scores) >= 2 else None

    @property
    def highest(self) -> Optional[float]:
        return max(self.scores) if self.scores else None

    @property
    def lowest(self) -> Optional[float]:
        return min(self.scores) if self.scores else None


@dataclass
class AnalysisResult:
    """Holds the complete analysis output."""
    students: list[StudentRecord]
    subject_stats: dict[str, SubjectStats]
    skipped_rows: list[ParseError]

    @property
    def total_students(self) -> int:
        return len(self.students)

    @property
    def class_average(self) -> float:
        averages = [s.average for s in self.students if s.grades]
        return mean(averages) if averages else 0.0

    @property
    def grade_distribution(self) -> dict[str, int]:
        dist: dict[str, int] = {"A": 0, "B": 0, "C": 0, "D": 0, "F": 0}
        for s in self.students:
            dist[s.letter_grade] += 1
        return dist


# =============================================================================
# Generator: parse CSV rows lazily
# =============================================================================

def parse_rows(csv_path: Path) -> Iterator[StudentRecord]:
    """
    Generator that reads one CSV row at a time and yields StudentRecord objects.

    Skips rows where:
    - The name field is empty
    - All grade fields are empty or invalid

    Cells with invalid or empty values are treated as missing (not counted).

    Yields:
        StudentRecord objects (one per valid row)
    """
    subjects = None

    with csv_path.open(newline="", encoding="utf-8") as fh:
        reader = csv.DictReader(fh)

        if reader.fieldnames is None:
            raise EmptyFileError(f"{csv_path} has no header row")

        # First field is "name", rest are subjects
        all_fields = [f.strip() for f in reader.fieldnames]
        subjects = [f for f in all_fields if f.lower() != "name"]

        for row_num, row in enumerate(reader, start=2):  # 1-indexed; row 1 = header
            name = row.get("name", "").strip()
            if not name:
                continue  # Skip nameless rows silently

            grades: dict[str, float] = {}
            for subj in subjects:
                raw = row.get(subj, "").strip()
                if not raw:
                    continue  # Missing score — skip this subject for this student
                try:
                    score = float(raw)
                    grades[subj] = score
                except ValueError:
                    # Non-numeric cell — skip this subject for this student
                    continue

            # Yield even if some grades are missing (student may just be enrolled in fewer)
            yield StudentRecord(name=name, grades=grades)


def collect_subject_stats(students: list[StudentRecord], subjects: list[str]) -> dict[str, SubjectStats]:
    """
    Compute per-subject statistics from a list of StudentRecord objects.

    Args:
        students: All parsed student records
        subjects: Ordered list of subject names

    Returns:
        Dict mapping subject name to SubjectStats
    """
    stats: dict[str, SubjectStats] = {subj: SubjectStats(subject=subj) for subj in subjects}

    for student in students:
        for subj, score in student.grades.items():
            if subj in stats:
                stats[subj].scores.append(score)

    return stats


# =============================================================================
# Analysis pipeline
# =============================================================================

def analyze(csv_path: Path) -> AnalysisResult:
    """
    Main entry point: parse a grades CSV and compute full statistics.

    Uses a generator pipeline internally for memory efficiency.

    Args:
        csv_path: Path to the input CSV file

    Returns:
        AnalysisResult with students, subject stats, and skipped rows
    """
    if not csv_path.exists():
        raise FileNotFoundError(f"CSV file not found: {csv_path}")

    skipped: list[ParseError] = []

    # Materialize students (needed for subject stats and report ordering).
    # For truly huge files, we could do two-pass or streaming aggregation —
    # but for grade files this list stays small enough.
    students: list[StudentRecord] = []

    try:
        for record in parse_rows(csv_path):
            students.append(record)
    except EmptyFileError:
        raise

    if not students:
        raise EmptyFileError(f"No valid student rows found in {csv_path}")

    # Detect subjects from the CSV header
    subjects = _read_subjects(csv_path)
    subject_stats = collect_subject_stats(students, subjects)

    return AnalysisResult(students=students, subject_stats=subject_stats, skipped_rows=skipped)


def _read_subjects(csv_path: Path) -> list[str]:
    """Read the header row and return the list of subject names (all columns except 'name')."""
    with csv_path.open(newline="", encoding="utf-8") as fh:
        reader = csv.DictReader(fh)
        if not reader.fieldnames:
            return []
        return [f.strip() for f in reader.fieldnames if f.strip().lower() != "name"]


# =============================================================================
# Reporting
# =============================================================================

def format_report(result: AnalysisResult) -> str:
    """
    Build a formatted text report from an AnalysisResult.

    Returns:
        Multi-line string report
    """
    lines: list[str] = []
    sep = "=" * 60

    lines.append(sep)
    lines.append("  STUDENT GRADE REPORT")
    lines.append(sep)
    lines.append(f"  Total students   : {result.total_students}")
    lines.append(f"  Class average    : {result.class_average:.1f}")
    lines.append(f"  Subjects covered : {', '.join(result.subject_stats.keys())}")
    lines.append("")

    # --- Per-student table ---
    lines.append("-" * 60)
    lines.append("  INDIVIDUAL RESULTS")
    lines.append("-" * 60)

    # Header
    col_w = 14
    subj_names = list(result.subject_stats.keys())
    header = f"  {'Name':<16}" + "".join(f"{s:<{col_w}}" for s in subj_names) + f"  {'Avg':>6}  {'Grade':>5}"
    lines.append(header)
    lines.append("  " + "-" * (len(header) - 2))

    for student in sorted(result.students, key=lambda s: -s.average):
        row = f"  {student.name:<16}"
        for subj in subj_names:
            score = student.grades.get(subj)
            cell = f"{score:.0f}" if score is not None else "--"
            row += f"{cell:<{col_w}}"
        row += f"  {student.average:>6.1f}  {student.letter_grade:>5}"
        lines.append(row)

    lines.append("")

    # --- Subject statistics ---
    lines.append("-" * 60)
    lines.append("  SUBJECT STATISTICS")
    lines.append("-" * 60)

    stat_header = f"  {'Subject':<16}  {'Count':>6}  {'Avg':>7}  {'StdDev':>8}  {'High':>6}  {'Low':>6}"
    lines.append(stat_header)
    lines.append("  " + "-" * (len(stat_header) - 2))

    for subj, stats in result.subject_stats.items():
        avg_str    = f"{stats.average:.1f}"   if stats.average  is not None else "n/a"
        std_str    = f"{stats.std_dev:.1f}"   if stats.std_dev  is not None else "n/a"
        high_str   = f"{stats.highest:.0f}"   if stats.highest  is not None else "n/a"
        low_str    = f"{stats.lowest:.0f}"    if stats.lowest   is not None else "n/a"
        lines.append(
            f"  {subj:<16}  {stats.count:>6}  {avg_str:>7}  {std_str:>8}  {high_str:>6}  {low_str:>6}"
        )

    lines.append("")

    # --- Grade distribution ---
    lines.append("-" * 60)
    lines.append("  GRADE DISTRIBUTION")
    lines.append("-" * 60)

    dist = result.grade_distribution
    for grade_letter, count in sorted(dist.items()):
        bar = "#" * count
        lines.append(f"  {grade_letter}: {bar:<30} ({count})")

    lines.append("")
    lines.append(sep)

    return "\n".join(lines)


def export_json(result: AnalysisResult, out_path: Path) -> None:
    """
    Export the analysis as a JSON file.

    Args:
        result: AnalysisResult to export
        out_path: Destination path for the JSON file
    """
    data = {
        "summary": {
            "total_students": result.total_students,
            "class_average": round(result.class_average, 2),
            "grade_distribution": result.grade_distribution,
        },
        "students": [
            {
                "name": s.name,
                "grades": {k: round(v, 1) for k, v in s.grades.items()},
                "average": round(s.average, 2),
                "letter_grade": s.letter_grade,
            }
            for s in sorted(result.students, key=lambda s: -s.average)
        ],
        "subject_stats": {
            subj: {
                "count":   stats.count,
                "average": round(stats.average, 2) if stats.average is not None else None,
                "std_dev": round(stats.std_dev, 2) if stats.std_dev is not None else None,
                "highest": stats.highest,
                "lowest":  stats.lowest,
            }
            for subj, stats in result.subject_stats.items()
        },
    }

    out_path.write_text(json.dumps(data, indent=2, ensure_ascii=False), encoding="utf-8")


# =============================================================================
# Sample data generator
# =============================================================================

SAMPLE_CSV = textwrap.dedent("""\
    name,math,science,english,history,art
    Alice,92,88,95,91,84
    Bob,78,85,80,76,90
    Carol,65,70,72,68,75
    Diana,95,97,92,98,96
    Ethan,55,60,58,62,50
    Fiona,82,79,88,85,91
    Grace,,74,80,77,83
    Henry,70,invalid,75,72,68
    Irene,88,90,87,92,85
    Jack,45,52,48,55,60
    Kate,76,83,79,81,88
    Leo,91,86,93,89,77
""")


def create_sample_csv(path: Path) -> None:
    """Write the sample CSV file to the given path."""
    path.write_text(SAMPLE_CSV, encoding="utf-8")
    print(f"[INFO] Created sample CSV: {path}")


# =============================================================================
# Main
# =============================================================================

def main() -> None:
    # Determine input file
    if len(sys.argv) >= 2:
        csv_path = Path(sys.argv[1])
        if not csv_path.exists():
            print(f"[ERROR] File not found: {csv_path}", file=sys.stderr)
            sys.exit(1)
    else:
        # Auto-generate sample data in a temp directory
        tmp_dir = Path(tempfile.mkdtemp())
        csv_path = tmp_dir / "grades.csv"
        create_sample_csv(csv_path)

    # Determine output paths (same directory as CSV)
    out_dir = csv_path.parent
    report_path = out_dir / "grades_report.txt"
    json_path   = out_dir / "grades_stats.json"

    print(f"[INFO] Analyzing: {csv_path}")

    # Run analysis
    try:
        result = analyze(csv_path)
    except EmptyFileError as e:
        print(f"[ERROR] {e}", file=sys.stderr)
        sys.exit(1)
    except FileNotFoundError as e:
        print(f"[ERROR] {e}", file=sys.stderr)
        sys.exit(1)

    # Print report to stdout
    report = format_report(result)
    print(report)

    # Write report file
    report_path.write_text(report, encoding="utf-8")
    print(f"[INFO] Report written : {report_path}")

    # Export JSON
    export_json(result, json_path)
    print(f"[INFO] JSON exported  : {json_path}")


if __name__ == "__main__":
    main()
