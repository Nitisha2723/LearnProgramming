# Project 03: Student Management System

A student management system demonstrating CSV import/export, GPA calculation,
report generation, and dataclass-based modeling.

## Features

- Add, update, delete students
- Enroll students in courses
- Record grades and calculate GPA
- Import/export data to CSV
- Generate reports by GPA, department, status

## Running

```bash
python src/main.py
```

## Running Tests

```bash
pytest tests/ -v
```

## Concepts Demonstrated

- `csv` module for data import/export
- `dataclasses` with computed properties
- Abstract repositories
- Report generation patterns
- `statistics` module for calculations
