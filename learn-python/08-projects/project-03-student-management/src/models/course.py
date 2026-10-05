from __future__ import annotations

import uuid
from dataclasses import dataclass, field


@dataclass
class Course:
    """Represents an academic course."""

    name: str
    code: str
    credits: int = 3
    id: str = field(default_factory=lambda: str(uuid.uuid4()))

    def __post_init__(self) -> None:
        if self.credits <= 0:
            raise ValueError("Credits must be positive.")

    def __str__(self) -> str:
        return f"Course({self.code}, {self.name}, {self.credits} cr)"
