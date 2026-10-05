# Project 01: Todo CLI Application

A command-line todo application demonstrating Python best practices: dataclasses,
protocols/ABC, type hints, separation of concerns, and pytest testing.

## Architecture

```
src/
├── models/          # Data models (Task, Priority)
├── repository/      # Data access layer (ABC + implementation)
├── service/         # Business logic
├── cli/             # Command-line interface
└── main.py          # Entry point
```

## Features

- Create, read, update, delete tasks
- Priority levels (LOW, MEDIUM, HIGH, CRITICAL)
- Mark tasks complete/incomplete
- Filter by status and priority
- Short-ID prefix resolution

## Running the Application

```bash
python src/main.py
```

## Running Tests

```bash
pytest tests/ -v
```

## Concepts Demonstrated

- `@dataclass` decorator for clean model definitions
- `ABC` for interface contracts
- `UUID` for unique identifiers
- `datetime` for timestamps
- `enum.Enum` for priority levels
- Separation of concerns (models / repository / service / CLI)
- `pytest` fixtures and parametrize
