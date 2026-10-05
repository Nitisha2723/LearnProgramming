from __future__ import annotations

import uuid
from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum
from typing import Optional


class Grade(Enum):
    """Letter grades with GPA points."""
    A_PLUS  = ("A+",  4.0)
    A       = ("A",   4.0)
    A_MINUS = ("A-",  3.7)
    B_PLUS  = ("B+",  3.3)
    B       = ("B",   3.0)
    B_MINUS = ("B-",  2.7)
    C_PLUS  = ("C+",  2.3)
    C       = ("C",   2.0)
    C_MINUS = ("C-",  1.7)
    D       = ("D",   1.0)
    F       = ("F",   0.0)

    def __init__(self, letter: str, points: float) -> None:
        self.letter = letter
        self.points = points

    def __str__(self) -> str:
        return self.letter

    @classmethod
    def from_letter(cls, letter: str) -> "Grade":
        for g in cls:
            if g.letter == letter.upper():
                return g
        raise ValueError(f"Unknown grade letter: {letter}")


@dataclass
class Enrollment:
    """Links a student to a course with an optional grade."""

    student_id: str
    course_id: str
    grade: Optional[Grade] = None
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    enrolled_at: datetime = field(default_factory=datetime.now)

    def assign_grade(self, grade: Grade) -> None:
        self.grade = grade

    @property
    def is_graded(self) -> bool:
        return self.grade is not None
