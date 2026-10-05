# Module 08: Capstone Projects Guide

This guide helps you work through the four capstone projects effectively.

## Recommended Order

1. **Project 01 - Todo CLI** — Start here. Covers the foundation: dataclasses, ABC, service layer, pytest.
2. **Project 02 - Banking System** — Adds Decimal precision, custom exceptions, transfers.
3. **Project 03 - Student Management** — CSV I/O, GPA calculation, reports.
4. **Project 04 - Web Scraper Pipeline** — Generators, asyncio, HTML parsing.

## Project Comparison

| Feature | Todo CLI | Banking | Student Mgmt | Web Scraper |
|---------|----------|---------|--------------|-------------|
| Models | dataclass | dataclass | dataclass | dataclass |
| Repository | ABC | ABC | dict-based | N/A |
| Validation | ValueError | custom exc | __post_init__ | N/A |
| Data I/O | in-memory | in-memory | CSV | HTTP + JSON |
| Async | No | No | No | Yes (asyncio) |
| Test count | ~35 | ~20 | ~15 | ~13 |

## What to Build Next

After completing all four projects, consider these extensions:

### Todo CLI Extensions
- Add file persistence (JSON or SQLite)
- Add `--due-date` and date-based filtering
- Add sub-tasks support

### Banking System Extensions
- Interest calculation for savings accounts
- Account statement PDF export
- Fraud detection (unusual transaction patterns)

### Student Management Extensions
- Connect to a real SQLite database
- REST API with Flask or FastAPI
- Grade curve adjustments

### Web Scraper Extensions
- Add rate limiting and politeness delays
- Robots.txt compliance
- Resume interrupted scrapes with checkpointing
- Full asyncio with aiohttp for true async I/O

## Assessment Checklist

For each project, verify:

- [ ] All tests pass: `pytest tests/ -v`
- [ ] Code follows PEP 8 style
- [ ] Type hints are present on all functions
- [ ] Docstrings explain the purpose of each class/method
- [ ] Edge cases are handled (empty inputs, missing IDs, etc.)
- [ ] No circular imports
- [ ] README accurately describes how to run the project
