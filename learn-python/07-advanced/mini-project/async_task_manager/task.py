"""
Task Data Model

Demonstrates:
- Descriptors for validated attributes (priority, timeout)
- __slots__ for memory efficiency
- Dataclass-like interface without @dataclass
- Generator for progress reporting
"""

import uuid
from enum import Enum
from typing import Any, Callable, Coroutine


class TaskStatus(Enum):
    PENDING = "pending"
    RUNNING = "running"
    COMPLETED = "completed"
    FAILED = "failed"
    CANCELLED = "cancelled"


class TaskPriority(Enum):
    LOW = 1
    NORMAL = 2
    HIGH = 3
    CRITICAL = 4


# ============================================================================
# Descriptors for validated attributes
# ============================================================================

class BoundedInt:
    """
    Descriptor: validates that an integer stays within [min_val, max_val].

    Demonstrates the descriptor protocol: __set_name__, __get__, __set__.
    """

    def __init__(self, min_val: int, max_val: int):
        self._min = min_val
        self._max = max_val
        self._name: str = ""

    def __set_name__(self, owner, name: str):
        self._name = name
        self._private = f"_desc_{name}"

    def __get__(self, obj, objtype=None):
        if obj is None:
            return self
        return getattr(obj, self._private, self._min)

    def __set__(self, obj, value: int):
        if not isinstance(value, int):
            raise TypeError(
                f"{self._name} must be int, got {type(value).__name__}"
            )
        if not (self._min <= value <= self._max):
            raise ValueError(
                f"{self._name} must be between {self._min} and {self._max}, got {value}"
            )
        setattr(obj, self._private, value)


# ============================================================================
# Task
# ============================================================================

class Task:
    """
    Represents a unit of work to be processed by the task manager.

    Uses __slots__ to minimize memory per instance (the queue may hold
    thousands of tasks simultaneously).

    Attributes:
        id: Unique task identifier.
        name: Human-readable task name.
        func: Async callable to execute.
        args / kwargs: Arguments for func.
        priority: TaskPriority enum.
        max_retries: How many times to retry on failure (0 = no retry).
        timeout: Maximum execution time in seconds (None = no limit).
        status: Current TaskStatus.
        result: Execution result (set after completion).
        error: Exception message (set after failure).
        attempts: Number of execution attempts made.
    """

    # Bounded descriptor — must be >= 0 and <= 10
    max_retries = BoundedInt(0, 10)

    # Use __slots__ to avoid per-instance __dict__
    __slots__ = (
        "id",
        "name",
        "func",
        "args",
        "kwargs",
        "priority",
        "timeout",
        "status",
        "result",
        "error",
        "attempts",
        "_desc_max_retries",  # Storage slot for the descriptor
    )

    def __init__(
        self,
        name: str,
        func: Callable[..., Coroutine[Any, Any, Any]],
        *args,
        priority: TaskPriority = TaskPriority.NORMAL,
        max_retries: int = 0,
        timeout: float | None = None,
        **kwargs,
    ):
        self.id = str(uuid.uuid4())[:8]
        self.name = name
        self.func = func
        self.args = args
        self.kwargs = kwargs
        self.priority = priority
        self.max_retries = max_retries  # Uses BoundedInt descriptor
        self.timeout = timeout
        self.status = TaskStatus.PENDING
        self.result = None
        self.error: str | None = None
        self.attempts = 0

    def progress_events(self):
        """
        Generator that yields status change strings as the task progresses.
        Demonstrates generators for event streaming.
        """
        yield f"[{self.id}] {self.name} → {TaskStatus.PENDING.value}"
        yield f"[{self.id}] {self.name} → {TaskStatus.RUNNING.value}"
        if self.status == TaskStatus.COMPLETED:
            yield f"[{self.id}] {self.name} → {TaskStatus.COMPLETED.value} ✓"
        elif self.status == TaskStatus.FAILED:
            yield f"[{self.id}] {self.name} → {TaskStatus.FAILED.value} ✗ ({self.error})"
        else:
            yield f"[{self.id}] {self.name} → {self.status.value}"

    def __repr__(self) -> str:
        return (
            f"Task(id={self.id!r}, name={self.name!r}, "
            f"status={self.status.value!r}, attempts={self.attempts})"
        )

    def __lt__(self, other: "Task") -> bool:
        """Enable priority-based sorting (higher priority = lower value for min-heap)."""
        return self.priority.value > other.priority.value
