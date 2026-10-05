# Module 08: Projects

Four complete Python projects demonstrating real-world application of all concepts
learned in the previous modules: OOP, data structures, design patterns, testing.

## Projects

| # | Project | Key Concepts |
|---|---------|-------------|
| 01 | [Todo CLI](./project-01-todo-cli/) | dataclasses, ABC, argparse, pytest |
| 02 | [Banking System](./project-02-banking-system/) | Decimal, custom exceptions, transfer logic |
| 03 | [Student Management](./project-03-student-management/) | CSV import/export, GPA calculation, reports |
| 04 | [Web Scraper Pipeline](./project-04-web-scraper-pipeline/) | generators, asyncio, HTML parsing |

## How to Use These Projects

Each project is self-contained. Navigate to a project directory and:

```bash
# Install no external dependencies required (stdlib only)
python src/main.py        # Run the demo
pytest tests/ -v          # Run the test suite
```

## Learning Path

Work through the projects in order — each builds on concepts from the previous
modules and introduces new patterns appropriate to Python.

## Python Patterns Demonstrated

- `@dataclass` decorator for clean, boilerplate-free models
- `ABC` and `Protocol` for interface contracts
- `Enum` for type-safe constants
- `uuid`, `datetime`, `decimal` from the stdlib
- `csv` and `json` for data exchange
- `html.parser` for HTML parsing (no external deps)
- Generator functions and the pipeline pattern
- `asyncio` for concurrent I/O
- `pytest` fixtures, parametrize, monkeypatch
