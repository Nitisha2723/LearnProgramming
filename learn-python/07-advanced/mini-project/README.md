# Mini-Project: Async Task Manager

A production-quality async task queue demonstrating the advanced Python concepts from Module 07.

## Concepts Demonstrated

| Concept | Where |
|---------|-------|
| Decorators (`@retry`, `@timer`) | `task_manager.py` — task execution with retry |
| Async/await | `task_manager.py` — all execution is async |
| asyncio.Queue | `task_manager.py` — task queue between producer and workers |
| asyncio.gather() | `task_manager.py` — concurrent worker startup |
| asyncio.create_task() | `task_manager.py` — background workers |
| Generators | `task.py` — progress reporting via generator |
| Descriptors | `task.py` — validated task priority attribute |
| `__slots__` | `task.py` — memory-efficient task objects |
| Context managers | `task_manager.py` — clean startup and shutdown |

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                  AsyncTaskManager                    │
│                                                      │
│  submit(task) → asyncio.Queue → Worker coroutines   │
│                                                      │
│  on_complete callback (Observer pattern)            │
│  on_error callback (Observer pattern)               │
│                                                      │
│  get_stats() → TaskStats dataclass                  │
└─────────────────────────────────────────────────────┘
```

## Running the Demo

```bash
cd 07-advanced/mini-project
python async_task_manager/demo.py
```

## File Structure

```
mini-project/
├── README.md
└── async_task_manager/
    ├── __init__.py
    ├── task.py           ← Task data model with descriptors and slots
    ├── task_manager.py   ← Core async task manager
    └── demo.py           ← Demonstration script
```
