from abc import ABC, abstractmethod
from typing import List, Optional

from ..models.task import Task
from ..models.priority import Priority


class TaskRepository(ABC):
    """Abstract base class defining the contract for task storage."""

    @abstractmethod
    def save(self, task: Task) -> Task:
        """Persist a task and return it."""
        ...

    @abstractmethod
    def find_by_id(self, task_id: str) -> Optional[Task]:
        """Find a task by its ID. Returns None if not found."""
        ...

    @abstractmethod
    def find_all(self) -> List[Task]:
        """Return all tasks."""
        ...

    @abstractmethod
    def find_by_completed(self, completed: bool) -> List[Task]:
        """Return tasks filtered by completion status."""
        ...

    @abstractmethod
    def find_by_priority(self, priority: Priority) -> List[Task]:
        """Return tasks filtered by priority."""
        ...

    @abstractmethod
    def delete(self, task_id: str) -> bool:
        """Delete a task by ID. Returns True if deleted, False if not found."""
        ...

    @abstractmethod
    def count(self) -> int:
        """Return the total number of tasks."""
        ...
