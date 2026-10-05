from typing import Dict, List, Optional

from ..models.task import Task
from ..models.priority import Priority
from .task_repository import TaskRepository


class InMemoryTaskRepository(TaskRepository):
    """In-memory implementation of TaskRepository using a dictionary."""

    def __init__(self) -> None:
        self._storage: Dict[str, Task] = {}

    def save(self, task: Task) -> Task:
        """Save or update a task."""
        self._storage[task.id] = task
        return task

    def find_by_id(self, task_id: str) -> Optional[Task]:
        """Find a task by ID."""
        return self._storage.get(task_id)

    def find_all(self) -> List[Task]:
        """Return all tasks sorted by creation time (newest first)."""
        return sorted(
            self._storage.values(),
            key=lambda t: t.created_at,
            reverse=True,
        )

    def find_by_completed(self, completed: bool) -> List[Task]:
        """Return tasks filtered by completion status."""
        return [t for t in self._storage.values() if t.completed == completed]

    def find_by_priority(self, priority: Priority) -> List[Task]:
        """Return tasks with the given priority."""
        return [t for t in self._storage.values() if t.priority == priority]

    def delete(self, task_id: str) -> bool:
        """Delete a task. Returns True if deleted."""
        if task_id in self._storage:
            del self._storage[task_id]
            return True
        return False

    def count(self) -> int:
        """Return total task count."""
        return len(self._storage)

    def clear(self) -> None:
        """Remove all tasks (useful for testing)."""
        self._storage.clear()
