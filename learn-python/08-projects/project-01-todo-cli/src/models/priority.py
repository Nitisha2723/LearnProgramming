from enum import Enum


class Priority(Enum):
    """Task priority levels."""
    LOW = 1
    MEDIUM = 2
    HIGH = 3
    CRITICAL = 4

    def __str__(self) -> str:
        return self.name

    @classmethod
    def from_string(cls, value: str) -> "Priority":
        """Create Priority from string, case-insensitive."""
        try:
            return cls[value.upper()]
        except KeyError:
            raise ValueError(
                f"Invalid priority: '{value}'. "
                f"Valid values: {[p.name for p in cls]}"
            )
