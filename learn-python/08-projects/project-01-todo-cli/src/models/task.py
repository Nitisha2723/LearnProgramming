from __future__ import annotations

import uuid
from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional

from .priority import Priority


@dataclass
class Task:
    """Represents a single todo task."""

    title: str
    description: str = ""
    priority: Priority = Priority.MEDIUM
    completed: bool = False
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    created_at: datetime = field(default_factory=datetime.now)
    updated_at: Optional[datetime] = None

    def complete(self) -> None:
        """Mark the task as completed."""
        self.completed = True
        self.updated_at = datetime.now()

    def uncomplete(self) -> None:
        """Mark the task as not completed."""
        self.completed = False
        self.updated_at = datetime.now()

    def update_title(self, new_title: str) -> None:
        """Update the task title."""
        if not new_title.strip():
            raise ValueError("Title cannot be empty.")
        self.title = new_title.strip()
        self.updated_at = datetime.now()

    def update_description(self, new_description: str) -> None:
        """Update the task description."""
        self.description = new_description
        self.updated_at = datetime.now()

    def update_priority(self, new_priority: Priority) -> None:
        """Update the task priority."""
        self.priority = new_priority
        self.updated_at = datetime.now()

    def __str__(self) -> str:
        status = "done" if self.completed else "todo"
        return (f"[{status}] [{self.priority}] {self.title}"
                f" (id: {self.id[:8]}...)")

    def __repr__(self) -> str:
        return (f"Task(id={self.id!r}, title={self.title!r}, "
                f"priority={self.priority!r}, completed={self.completed!r})")
