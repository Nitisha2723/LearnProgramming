from __future__ import annotations

import uuid
from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional


@dataclass
class Student:
    """Represents a student."""

    name: str
    email: str
    department: str
    student_number: str = field(default_factory=lambda: f"STU{uuid.uuid4().hex[:6].upper()}")
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    enrolled_at: datetime = field(default_factory=datetime.now)
    active: bool = True

    def __post_init__(self) -> None:
        if not self.name.strip():
            raise ValueError("Student name cannot be empty.")
        if "@" not in self.email:
            raise ValueError(f"Invalid email: {self.email}")
        self.name = self.name.strip()
        self.email = self.email.lower().strip()

    def deactivate(self) -> None:
        self.active = False

    def __str__(self) -> str:
        status = "active" if self.active else "inactive"
        return f"Student({self.student_number}, {self.name}, {self.department}, {status})"
