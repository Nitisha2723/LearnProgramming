"""
Grade Calculator — Mini-Project for Module 02: Core Language

This program demonstrates:
  - Functions with type hints and docstrings
  - Default parameters
  - Pure function design
  - f-string formatting with alignment
  - List comprehensions and sorting
  - Named tuples

Run with:  python mini-project/grade_calculator.py
"""

from typing import NamedTuple


# ─────────────────────────────────────────────────────────────
# DATA TYPES
# ─────────────────────────────────────────────────────────────

class StudentResult(NamedTuple):
    """Immutable record of a processed student's grades."""
    name: str
    homework: float
    midterm: float
    final: float
    average: float
    letter: str


# Default grading weights — sum must equal 1.0
DEFAULT_WEIGHTS = {
    "homework": 0.30,
    "midterm": 0.30,
    "final": 0.40,
}


# ─────────────────────────────────────────────────────────────
# CORE FUNCTIONS (all pure — no side effects)
# ─────────────────────────────────────────────────────────────

def calculate_weighted_average(scores: dict[str, float],
                               weights: dict[str, float]) -> float:
    """
    Calculate a weighted average from scores and weights.

    Args:
        scores: Dict mapping category name to numeric score (0-100).
        weights: Dict mapping category name to weight (must sum to 1.0).

    Returns:
        The weighted average as a float.

    Examples:
        >>> calculate_weighted_average(
        ...     {"homework": 80, "midterm": 90, "final": 70},
        ...     {"homework": 0.3, "midterm": 0.3, "final": 0.4}
        ... )
        79.0
    """
    total = 0.0
    for category, weight in weights.items():
        score = scores.get(category, 0.0)
        total += score * weight
    return round(total, 1)


def assign_letter_grade(average: float) -> str:
    """
    Convert a numeric average to a letter grade.

    Args:
        average: Numeric score between 0 and 100.

    Returns:
        Letter grade: "A", "B", "C", "D", or "F".
    """
    if average >= 90:
        return "A"
    elif average >= 80:
        return "B"
    elif average >= 70:
        return "C"
    elif average >= 60:
        return "D"
    else:
        return "F"


def process_student(student: dict,
                    weights: dict[str, float] = DEFAULT_WEIGHTS) -> StudentResult:
    """
    Compute the final grade for a single student.

    Args:
        student: Dict with 'name' (str) and 'scores' (dict).
        weights: Grading weights (defaults to DEFAULT_WEIGHTS).

    Returns:
        A StudentResult named tuple with all computed fields.
    """
    name = student["name"]
    scores = student["scores"]
    average = calculate_weighted_average(scores, weights)
    letter = assign_letter_grade(average)

    return StudentResult(
        name=name,
        homework=scores.get("homework", 0),
        midterm=scores.get("midterm", 0),
        final=scores.get("final", 0),
        average=average,
        letter=letter,
    )


def get_class_average(results: list[StudentResult]) -> float:
    """Return the class average from a list of StudentResult records."""
    if not results:
        return 0.0
    return round(sum(r.average for r in results) / len(results), 1)


def find_top_performers(results: list[StudentResult], n: int = 3) -> list[StudentResult]:
    """Return the top n students by average, highest first."""
    return sorted(results, key=lambda r: r.average, reverse=True)[:n]


def find_needs_attention(results: list[StudentResult],
                         threshold: float = 70.0) -> list[StudentResult]:
    """Return students with average below threshold, sorted by average ascending."""
    return sorted(
        [r for r in results if r.average < threshold],
        key=lambda r: r.average,
    )


def grade_distribution(results: list[StudentResult]) -> dict[str, int]:
    """Return a count of how many students received each letter grade."""
    distribution = {"A": 0, "B": 0, "C": 0, "D": 0, "F": 0}
    for result in results:
        distribution[result.letter] += 1
    return distribution


# ─────────────────────────────────────────────────────────────
# REPORT GENERATION (impure — has I/O side effects)
# ─────────────────────────────────────────────────────────────

def generate_report(students: list[dict],
                    weights: dict[str, float] = DEFAULT_WEIGHTS) -> None:
    """
    Process all students and print a formatted class grade report.

    Args:
        students: List of student dicts with 'name' and 'scores'.
        weights: Grading weights (defaults to DEFAULT_WEIGHTS).
    """
    # Process all students — pure computation
    results = [process_student(s, weights) for s in students]

    # Sort by name for consistent output
    results_by_name = sorted(results, key=lambda r: r.name)

    # Compute statistics
    class_avg = get_class_average(results)
    class_grade = assign_letter_grade(class_avg)
    top = find_top_performers(results, 1)[0] if results else None
    attention = find_needs_attention(results)
    distribution = grade_distribution(results)

    # Print the report
    WIDTH = 53
    RULE = "━" * WIDTH

    print(RULE)
    print(f"{'CLASS GRADE REPORT':^{WIDTH}}")
    print(RULE)
    print(f" {'Name':<14} {'HW':>4} {'Mid':>5} {'Final':>6} {'Avg':>6}  {'Grade'}")
    print("─" * WIDTH)

    for r in results_by_name:
        print(f" {r.name:<14} {r.homework:>4.0f} {r.midterm:>5.0f} "
              f"{r.final:>6.0f} {r.average:>6.1f}    {r.letter}")

    print(RULE)
    print(f" Class Average:  {class_avg:.1f}   Grade: {class_grade}")

    if top:
        print(f" Highest Score:  {top.name} ({top.average})")

    if attention:
        attn_str = ", ".join(f"{r.name} ({r.average})" for r in attention)
        print(f" Needs Attention: {attn_str}")

    print(RULE)

    # Grade distribution
    print(f"\n Grade Distribution:")
    for letter in ["A", "B", "C", "D", "F"]:
        count = distribution[letter]
        bar = "█" * count
        print(f"   {letter}: {bar} ({count})")


# ─────────────────────────────────────────────────────────────
# SAMPLE DATA AND MAIN
# ─────────────────────────────────────────────────────────────

SAMPLE_STUDENTS = [
    {"name": "Alice",   "scores": {"homework": 88, "midterm": 92, "final": 90}},
    {"name": "Bob",     "scores": {"homework": 72, "midterm": 68, "final": 75}},
    {"name": "Carol",   "scores": {"homework": 95, "midterm": 89, "final": 97}},
    {"name": "Dave",    "scores": {"homework": 55, "midterm": 60, "final": 58}},
    {"name": "Emma",    "scores": {"homework": 80, "midterm": 75, "final": 82}},
    {"name": "Frank",   "scores": {"homework": 65, "midterm": 70, "final": 68}},
    {"name": "Grace",   "scores": {"homework": 91, "midterm": 88, "final": 94}},
    {"name": "Henry",   "scores": {"homework": 78, "midterm": 82, "final": 79}},
]


def demo_individual() -> None:
    """Demonstrate individual function calls."""
    print("Individual function demos:")
    print("-" * 35)

    # calculate_weighted_average
    scores = {"homework": 80, "midterm": 90, "final": 70}
    avg = calculate_weighted_average(scores, DEFAULT_WEIGHTS)
    print(f"  Weighted average (80/90/70): {avg}")

    # assign_letter_grade
    for avg_val in [95, 85, 75, 65, 55]:
        grade = assign_letter_grade(avg_val)
        print(f"  assign_letter_grade({avg_val}) = {grade}")

    # process_student
    student = SAMPLE_STUDENTS[0]
    result = process_student(student)
    print(f"\n  process_student(Alice): {result}")
    print()


if __name__ == "__main__":
    demo_individual()
    generate_report(SAMPLE_STUDENTS)

    # Demonstrate custom weights
    print("\n\nWith exam-heavy weights (HW=10%, Mid=40%, Final=50%):")
    heavy_exam_weights = {"homework": 0.10, "midterm": 0.40, "final": 0.50}
    generate_report(SAMPLE_STUDENTS, weights=heavy_exam_weights)
