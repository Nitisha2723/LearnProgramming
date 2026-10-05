# Module 05: Real-World Python

## Overview

This module covers the Python features you need to write production-quality code.
Exceptions, file I/O, generators, context managers, and thorough testing.

If Module 04 was about data structures, this module is about code structure —
how to handle errors gracefully, process data efficiently, manage resources safely,
and ensure your code works correctly.

## Prerequisites

Before this module, you should be comfortable with:
- Functions, classes, and OOP (Module 03)
- Data structures (Module 04)
- Basic pytest (we'll go much deeper here)

## Learning Path

Work through the material in this order:

### Theory (read first)
1. `theory/01-exceptions.md` — Error handling, exception hierarchy, try/except/else/finally
2. `theory/02-file-io.md` — Files, pathlib, CSV, JSON
3. `theory/03-comprehensions-and-generators.md` — Generators, yield, itertools, memory efficiency
4. `theory/04-context-managers.md` — with statement, __enter__/__exit__, contextlib
5. `theory/05-testing-deep-dive.md` — Mocking, fixtures, pytest-cov, Hypothesis

### Code Examples (study alongside theory)
- `code/exceptions_demo.py` — Custom exceptions, else/finally, exception chaining
- `code/file_io_demo.py` — pathlib, CSV, JSON, context managers
- `code/generators_demo.py` — yield, lazy pipelines, itertools, memory demo
- `code/context_managers_demo.py` — Custom CM class and contextlib decorator

### Exercises (practice after reading)
- `exercises/exercise_01_file_processor.py` — Read CSV → compute stats → write report
- `exercises/exercise_02_generators.py` — Lazy pipeline processing large data

### Mini-Project
- `mini-project/csv_analyzer.py` — Full grade analysis with generators and pathlib

## How to Run

```bash
cd 05-real-world

# Code demos
python code/exceptions_demo.py
python code/file_io_demo.py
python code/generators_demo.py
python code/context_managers_demo.py

# Run tests
pytest tests/ -v

# Mini-project
python mini-project/csv_analyzer.py
```

## What You'll Build

The mini-project is a **CSV grade analyzer** that:
- Reads a student grades CSV using generators (memory-efficient for large files)
- Computes statistics per student and per subject
- Generates a formatted text report using pathlib
- Handles missing data and malformed rows gracefully with proper exceptions
