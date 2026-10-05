from __future__ import annotations

import uuid
from dataclasses import dataclass, field
from datetime import datetime


@dataclass
class Customer:
    """Represents a bank customer."""

    name: str
    email: str
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    created_at: datetime = field(default_factory=datetime.now)

    def __post_init__(self) -> None:
        if not self.name.strip():
            raise ValueError("Customer name cannot be empty.")
        if "@" not in self.email:
            raise ValueError(f"Invalid email address: {self.email}")
        self.name = self.name.strip()
        self.email = self.email.lower().strip()

    def __str__(self) -> str:
        return f"Customer({self.name}, {self.email})"
