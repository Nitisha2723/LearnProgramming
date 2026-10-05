# Module 07 — Advanced Python

This module covers the features that separate Python developers from Python experts.
These concepts are used in production code, frameworks, and libraries every day.

---

## What You Will Learn

| Topic | What it covers |
|-------|---------------|
| Decorators Deep Dive | The engine behind Python's most powerful feature |
| Generators and Iterators | Memory-efficient data processing |
| Async Programming | Concurrency without threads for I/O-bound work |
| Metaclasses and Descriptors | How Python classes and attributes really work |
| Performance and Profiling | Finding and eliminating bottlenecks |

**Estimated time:** 10–12 hours

---

## Prerequisites

This module assumes you are comfortable with:
- Closures and first-class functions (Module 02)
- Decorators at a basic level (Module 02)
- Object-oriented Python (Module 03)
- Type hints (Module 06)

---

## Why These Topics Matter

Most Python code you encounter in the real world uses these features:

- **Decorators:** `@app.route`, `@property`, `@classmethod`, `@functools.lru_cache`, `@pytest.mark.parametrize` — you use these constantly. Building your own makes you a better user of them.

- **Generators:** Processing a CSV file with 10 million rows. Reading logs in real time. Data pipelines. Without generators, you load everything into memory and your program crashes.

- **Async:** Making 100 API calls at once instead of one at a time. Any modern web application backend uses async Python.

- **Metaclasses:** Django's ORM, SQLAlchemy, Pydantic, pytest — all use metaclasses. You need to understand them to debug framework behavior.

- **Performance:** Python is "slow" — but slow Python code is usually just code that doesn't use the right tools. NumPy, caching, slots, and profiling can make Python fast enough for most tasks.

---

## Module Structure

```
07-advanced/
├── README.md                         ← You are here
├── theory/
│   ├── 01-decorators-deep-dive.md    ← How decorators REALLY work
│   ├── 02-generators-and-iterators.md
│   ├── 03-async-programming.md
│   ├── 04-metaclasses-and-descriptors.md
│   └── 05-performance-and-profiling.md
├── code/
│   ├── decorators_advanced.py        ← @retry, @timer, @cache_result, @validate_types
│   ├── generators_advanced.py        ← Pipelines, infinite sequences, itertools
│   ├── async_demo.py                 ← Concurrent HTTP, producer-consumer
│   └── README.md
├── exercises/
│   ├── exercise_01_decorator_framework.py
│   ├── exercise_02_async_pipeline.py
│   └── solutions/
├── tests/
│   └── test_advanced.py
└── mini-project/
    └── async_task_manager/           ← Asyncio task queue with priorities
```
