"""
Shapes using Protocol — demonstrating structural typing in Python.

This module shows:
- typing.Protocol for structural typing (no inheritance required)
- @runtime_checkable for isinstance() at runtime
- How Protocol differs from ABC
- Duck typing with formal type hints

Key concept:
    In Java: a class must explicitly 'implement ShapeInterface'
    In Python with Protocol: any class that HAS the right methods automatically satisfies the Protocol

This is structural subtyping — the structure (what methods exist) matters, not the declaration.
"""

import math
from typing import Protocol, runtime_checkable, List
from dataclasses import dataclass


# -----------------------------------------------------------------------
# Define Protocols — the "interfaces" of Python
# -----------------------------------------------------------------------

@runtime_checkable  # Enables isinstance(obj, Shape) at runtime
class Shape(Protocol):
    """Protocol defining what any shape must be able to do.

    Classes that have area() and perimeter() methods automatically
    satisfy this Protocol — they don't need to declare it.

    Compare with Java:
        Java interface: public interface Shape { double area(); double perimeter(); }
        Java class: public class Circle implements Shape { ... }  // MUST declare

        Python Protocol: class Shape(Protocol): def area() -> float: ...
        Python class: class Circle: def area() -> float: ...  // No declaration needed!
    """

    def area(self) -> float:
        """Return the area of the shape."""
        ...

    def perimeter(self) -> float:
        """Return the perimeter of the shape."""
        ...


@runtime_checkable
class Drawable(Protocol):
    """Protocol for objects that can be drawn."""

    def draw(self) -> str:
        """Return a string representation of the drawing."""
        ...


@runtime_checkable
class Scalable(Protocol):
    """Protocol for objects that can be scaled."""

    def scale(self, factor: float) -> "Scalable":
        """Return a new shape scaled by the given factor."""
        ...


# -----------------------------------------------------------------------
# Shapes — these DO NOT inherit from Shape Protocol
# They just happen to have the right methods
# -----------------------------------------------------------------------

@dataclass
class Circle:
    """A circle — satisfies Shape Protocol without inheriting from it.

    Python sees that Circle has area() and perimeter(), so it
    automatically satisfies the Shape Protocol.
    """
    radius: float

    def __post_init__(self) -> None:
        if self.radius <= 0:
            raise ValueError(f"Radius must be positive, got {self.radius}")

    def area(self) -> float:
        return math.pi * self.radius ** 2

    def perimeter(self) -> float:
        return 2 * math.pi * self.radius

    def draw(self) -> str:
        return f"○ Circle (radius={self.radius})"

    def scale(self, factor: float) -> "Circle":
        return Circle(self.radius * factor)

    def __str__(self) -> str:
        return f"Circle(r={self.radius})"

    def __repr__(self) -> str:
        return f"Circle(radius={self.radius})"


@dataclass
class Rectangle:
    """A rectangle — also satisfies Shape Protocol implicitly."""
    width: float
    height: float

    def __post_init__(self) -> None:
        if self.width <= 0 or self.height <= 0:
            raise ValueError(f"Width and height must be positive")

    def area(self) -> float:
        return self.width * self.height

    def perimeter(self) -> float:
        return 2 * (self.width + self.height)

    def draw(self) -> str:
        return f"▭ Rectangle ({self.width}×{self.height})"

    def scale(self, factor: float) -> "Rectangle":
        return Rectangle(self.width * factor, self.height * factor)

    @property
    def is_square(self) -> bool:
        return self.width == self.height

    def __str__(self) -> str:
        return f"Rectangle({self.width}×{self.height})"


@dataclass
class Triangle:
    """A triangle with three sides."""
    a: float   # Side lengths
    b: float
    c: float

    def __post_init__(self) -> None:
        sides = [self.a, self.b, self.c]
        if any(s <= 0 for s in sides):
            raise ValueError("All sides must be positive")
        # Triangle inequality
        if self.a + self.b <= self.c or self.b + self.c <= self.a or self.a + self.c <= self.b:
            raise ValueError(f"Invalid triangle: sides {self.a}, {self.b}, {self.c}")

    def area(self) -> float:
        # Heron's formula
        s = self.perimeter() / 2
        return math.sqrt(s * (s - self.a) * (s - self.b) * (s - self.c))

    def perimeter(self) -> float:
        return self.a + self.b + self.c

    def draw(self) -> str:
        return f"△ Triangle ({self.a}, {self.b}, {self.c})"

    def __str__(self) -> str:
        return f"Triangle({self.a}, {self.b}, {self.c})"


class RegularPolygon:
    """A regular polygon with n sides.

    Uses __init__ instead of @dataclass to show the contrast.
    """

    def __init__(self, sides: int, side_length: float) -> None:
        if sides < 3:
            raise ValueError(f"A polygon needs at least 3 sides, got {sides}")
        if side_length <= 0:
            raise ValueError(f"Side length must be positive, got {side_length}")

        self.sides = sides
        self.side_length = side_length

    def area(self) -> float:
        # Area of regular polygon: (n * s^2) / (4 * tan(π/n))
        n = self.sides
        s = self.side_length
        return (n * s ** 2) / (4 * math.tan(math.pi / n))

    def perimeter(self) -> float:
        return self.sides * self.side_length

    def draw(self) -> str:
        return f"⬡ RegularPolygon({self.sides} sides, length={self.side_length})"

    def __str__(self) -> str:
        names = {3: "Triangle", 4: "Square", 5: "Pentagon", 6: "Hexagon",
                 7: "Heptagon", 8: "Octagon"}
        name = names.get(self.sides, f"{self.sides}-gon")
        return f"Regular{name}(side={self.side_length})"


# -----------------------------------------------------------------------
# Functions that work with ANY object satisfying the Protocol
# -----------------------------------------------------------------------

def print_shape_info(shape: Shape) -> None:
    """Print information about any Shape.

    The type hint 'shape: Shape' is just a hint — Python doesn't enforce it.
    But static type checkers (mypy, pyright) will verify that whatever you
    pass has area() and perimeter() methods.
    """
    print(f"  Shape: {shape}")
    print(f"  Area: {shape.area():.4f}")
    print(f"  Perimeter: {shape.perimeter():.4f}")


def total_area(shapes: List[Shape]) -> float:
    """Calculate total area of all shapes.

    Works with ANY list of objects that have area().
    """
    return sum(shape.area() for shape in shapes)


def find_largest(shapes: List[Shape]) -> Shape:
    """Find the shape with the largest area."""
    if not shapes:
        raise ValueError("Cannot find largest shape in empty list")
    return max(shapes, key=lambda s: s.area())


def render_all(shapes: List[Drawable]) -> List[str]:
    """Render all shapes to strings.

    Uses the Drawable Protocol — separate from Shape.
    A class can satisfy both Protocols!
    """
    return [shape.draw() for shape in shapes]


def scale_all(shapes: List[Scalable], factor: float) -> List[Scalable]:
    """Return scaled versions of all shapes."""
    return [shape.scale(factor) for shape in shapes]


def print_summary(shapes: List[Shape]) -> None:
    """Print a summary table of all shapes."""
    print(f"\n{'Shape':<30} {'Area':>12} {'Perimeter':>12}")
    print("─" * 56)
    total = 0.0
    for shape in shapes:
        area = shape.area()
        total += area
        print(f"  {str(shape):<28} {area:>12.4f} {shape.perimeter():>12.4f}")
    print("─" * 56)
    print(f"  {'Total area':<28} {total:>12.4f}")
