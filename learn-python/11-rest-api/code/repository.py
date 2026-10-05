"""
In-memory repository for todos.

In a real application this would talk to a database (PostgreSQL, SQLite, etc.).
Using an in-memory dict keeps this example simple and dependency-free —
you don't need to set up a database to run or test the API.

Thread-safety: uses a threading.Lock so that concurrent requests
(which FastAPI can handle) don't corrupt the shared state.
"""

from threading import Lock
from datetime import datetime
from typing import Optional

from .models import TodoResponse


class TodoRepository:
    """
    In-memory store for todo items.

    All public methods accept and return plain dicts or TodoResponse objects.
    The router never touches the internal _todos dict directly.
    """

    def __init__(self) -> None:
        # Internal storage: {id: dict}
        self._todos: dict[int, dict] = {}
        self._next_id: int = 1
        self._lock = Lock()

    # ------------------------------------------------------------------
    # Read operations
    # ------------------------------------------------------------------

    def find_all(self, completed: Optional[bool] = None) -> list[TodoResponse]:
        """
        Return all todos, optionally filtered by completion status.

        Args:
            completed: If True, return only completed todos.
                       If False, return only pending todos.
                       If None, return all todos.
        """
        with self._lock:
            items = list(self._todos.values())

        if completed is not None:
            items = [item for item in items if item["completed"] is completed]

        return [TodoResponse(**item) for item in items]

    def find_by_id(self, todo_id: int) -> Optional[TodoResponse]:
        """
        Return a single todo by its ID, or None if not found.
        """
        with self._lock:
            item = self._todos.get(todo_id)

        if item is None:
            return None
        return TodoResponse(**item)

    # ------------------------------------------------------------------
    # Write operations
    # ------------------------------------------------------------------

    def create(self, title: str, description: Optional[str]) -> TodoResponse:
        """
        Create a new todo and return it.

        The repository assigns the ID and creation timestamp.
        """
        with self._lock:
            todo_id = self._next_id
            self._next_id += 1

            item = {
                "id": todo_id,
                "title": title,
                "description": description,
                "completed": False,
                "created_at": datetime.now(),
            }
            self._todos[todo_id] = item

        return TodoResponse(**item)

    def update(self, todo_id: int, **fields) -> Optional[TodoResponse]:
        """
        Update specific fields of a todo and return the updated version.

        Only fields explicitly passed as keyword arguments are changed.
        Returns None if the todo does not exist.

        Example:
            repo.update(1, title="New title", completed=True)
        """
        with self._lock:
            item = self._todos.get(todo_id)
            if item is None:
                return None

            for key, value in fields.items():
                item[key] = value

            # Return a copy so the caller doesn't hold a reference to internal state
            updated = dict(item)

        return TodoResponse(**updated)

    def delete(self, todo_id: int) -> bool:
        """
        Delete a todo by ID.

        Returns True if the todo existed and was deleted, False if not found.
        """
        with self._lock:
            if todo_id not in self._todos:
                return False
            del self._todos[todo_id]
            return True

    # ------------------------------------------------------------------
    # Utility
    # ------------------------------------------------------------------

    def count(self) -> int:
        """Return the total number of todos (useful for testing)."""
        with self._lock:
            return len(self._todos)
