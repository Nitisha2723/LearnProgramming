from typing import List, Optional

from ..models.task import Task
from ..models.priority import Priority
from ..repository.task_repository import TaskRepository


class TaskNotFoundError(Exception):
    """Raised when a task is not found by its ID."""

    def __init__(self, task_id: str) -> None:
        super().__init__(f"Task not found: {task_id}")
        self.task_id = task_id


class TaskService:
    """Business logic for task management."""

    def __init__(self, repository: TaskRepository) -> None:
        self._repository = repository

    def create_task(
        self,
        title: str,
        description: str = "",
        priority: Priority = Priority.MEDIUM,
    ) -> Task:
        """Create and persist a new task."""
        if not title.strip():
            raise ValueError("Task title cannot be empty.")
        task = Task(
            title=title.strip(),
            description=description,
            priority=priority,
        )
        return self._repository.save(task)

    def get_task(self, task_id: str) -> Task:
        """Retrieve a task by ID, raising TaskNotFoundError if missing."""
        task = self._repository.find_by_id(task_id)
        if task is None:
            raise TaskNotFoundError(task_id)
        return task

    def get_all_tasks(self) -> List[Task]:
        """Return all tasks."""
        return self._repository.find_all()

    def get_pending_tasks(self) -> List[Task]:
        """Return only incomplete tasks."""
        return self._repository.find_by_completed(False)

    def get_completed_tasks(self) -> List[Task]:
        """Return only completed tasks."""
        return self._repository.find_by_completed(True)

    def get_tasks_by_priority(self, priority: Priority) -> List[Task]:
        """Return tasks with a specific priority."""
        return self._repository.find_by_priority(priority)

    def complete_task(self, task_id: str) -> Task:
        """Mark a task as completed."""
        task = self.get_task(task_id)
        task.complete()
        return self._repository.save(task)

    def uncomplete_task(self, task_id: str) -> Task:
        """Mark a task as not completed."""
        task = self.get_task(task_id)
        task.uncomplete()
        return self._repository.save(task)

    def update_task(
        self,
        task_id: str,
        title: Optional[str] = None,
        description: Optional[str] = None,
        priority: Optional[Priority] = None,
    ) -> Task:
        """Update task fields. Only provided fields are changed."""
        task = self.get_task(task_id)
        if title is not None:
            task.update_title(title)
        if description is not None:
            task.update_description(description)
        if priority is not None:
            task.update_priority(priority)
        return self._repository.save(task)

    def delete_task(self, task_id: str) -> None:
        """Delete a task, raising TaskNotFoundError if not found."""
        if not self._repository.delete(task_id):
            raise TaskNotFoundError(task_id)

    def get_statistics(self) -> dict:
        """Return task statistics."""
        all_tasks = self.get_all_tasks()
        total = len(all_tasks)
        completed = sum(1 for t in all_tasks if t.completed)
        return {
            "total": total,
            "completed": completed,
            "pending": total - completed,
            "completion_rate": (completed / total * 100) if total > 0 else 0.0,
        }
