# Mini-Project: CSV Grade Analyzer

## Overview

A memory-efficient grade analyzer that processes student CSV data using
generators, pathlib, and proper exception handling.

## What It Does

1. Reads a student grades CSV file **lazily** (generator-based, memory-efficient)
2. Computes per-student and per-subject statistics
3. Generates a formatted report file
4. Handles malformed data gracefully

## Features

- **Generator-based processing** — works on files of any size
- **pathlib** for all file operations
- **Custom exceptions** for data validation errors
- **CSV module** with DictReader for named column access
- **JSON export** of the computed statistics

## Running

```bash
# Run with auto-generated sample data
python mini-project/csv_analyzer.py

# Run with a specific CSV file
python mini-project/csv_analyzer.py path/to/grades.csv
```

## CSV Format

```
name,math,science,english,history,art
Alice,90,85,88,93,79
Bob,75,92,80,86,90
Carol,,,68,75,    (missing values OK)
```

## Output

The analyzer writes two files:
- `grades_report.txt` — formatted text report
- `grades_stats.json` — machine-readable statistics
