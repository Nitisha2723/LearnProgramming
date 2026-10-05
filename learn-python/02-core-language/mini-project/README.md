# Mini-Project: Grade Calculator

Apply everything from Module 02 in one cohesive program.

## What You Are Building

A grade calculator that:
1. Accepts student names and their scores
2. Calculates final grades with configurable weights
3. Generates a formatted class report
4. Identifies top performers and students needing help

## Learning Goals

By completing this project you will practice:
- Functions with default parameters and type hints
- f-string formatting
- List comprehensions and sorting
- Tuples and named tuples (or dataclasses)
- Pure function design
- String formatting aligned output

## Specification

### Input Format

The program works with a list of students, where each student has:
- Name (string)
- Scores: a dict with keys `"homework"`, `"midterm"`, `"final"`

```python
students = [
    {"name": "Alice", "scores": {"homework": 88, "midterm": 92, "final": 90}},
    {"name": "Bob",   "scores": {"homework": 72, "midterm": 68, "final": 75}},
    ...
]
```

### Grading Weights (default)

| Category | Weight |
|----------|--------|
| Homework | 30% |
| Midterm  | 30% |
| Final    | 40% |

### Letter Grade Scale

| Weighted Average | Grade |
|-----------------|-------|
| 90–100 | A |
| 80–89  | B |
| 70–79  | C |
| 60–69  | D |
| below 60 | F |

### Required Functions

1. `calculate_weighted_average(scores, weights)` — pure function, returns float
2. `assign_letter_grade(average)` — pure function, returns str
3. `process_student(student, weights)` — returns enriched student dict
4. `generate_report(students, weights)` — prints formatted report

### Sample Output

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
          CLASS GRADE REPORT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 Name          HW    Mid   Final  Avg   Grade
─────────────────────────────────────────────────
 Alice         88    92    90     90.0    A
 Bob           72    68    75     72.0    C
 Carol         95    89    97     94.1    A
 Dave          55    60    58     58.1    F
 Emma          80    75    82     79.5    C
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 Class Average:  78.7   Grade: C
 Highest Score:  Carol (94.1)
 Needs Attention: Dave (58.1), Bob (72.0)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## Getting Started

Open `grade_calculator.py`. The function signatures and docstrings are there.
Implement each function, then run the file to see your report.

```bash
python mini-project/grade_calculator.py
```

## Extension Ideas (after the basics work)

1. Add a `grade_distribution()` function that shows how many students got each letter grade
2. Read student data from a CSV file
3. Let the user enter student data interactively
4. Add a GPA calculator (A=4.0, B=3.0, C=2.0, D=1.0, F=0.0)
