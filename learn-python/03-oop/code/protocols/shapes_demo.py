"""
Shapes demo — showing Protocol-based polymorphism.

Run this file:
    python code/protocols/shapes_demo.py
"""

from shapes import (
    Shape, Drawable, Scalable,
    Circle, Rectangle, Triangle, RegularPolygon,
    print_shape_info, total_area, find_largest, render_all, scale_all, print_summary
)


def separator(title: str = "") -> None:
    if title:
        print(f"\n{'─' * 20} {title} {'─' * 20}")
    else:
        print(f"\n{'─' * 60}")


def demo_protocol_without_inheritance() -> None:
    """Show that no inheritance is needed to satisfy a Protocol."""
    separator("Protocol Without Inheritance")

    circle = Circle(5.0)
    rect = Rectangle(4.0, 6.0)
    triangle = Triangle(3.0, 4.0, 5.0)
    pentagon = RegularPolygon(5, 4.0)

    print("None of these inherit from Shape Protocol!")
    print(f"Circle's bases: {Circle.__bases__}")
    print(f"Rectangle's bases: {Rectangle.__bases__}")

    print()
    # But they all satisfy the Shape Protocol:
    for shape in [circle, rect, triangle, pentagon]:
        print(f"isinstance({shape}, Shape): {isinstance(shape, Shape)}")


def demo_polymorphic_functions() -> None:
    """Show functions that work with any Shape."""
    separator("Polymorphic Functions")

    shapes = [
        Circle(5.0),
        Rectangle(4.0, 6.0),
        Triangle(3.0, 4.0, 5.0),
        RegularPolygon(6, 3.0),
    ]

    # These functions accept any Shape — duck typing with type hints
    print("Shape info for each shape:")
    for shape in shapes:
        print_shape_info(shape)
        print()

    print(f"Total area: {total_area(shapes):.4f}")
    largest = find_largest(shapes)
    print(f"Largest shape: {largest} (area={largest.area():.4f})")


def demo_multiple_protocols() -> None:
    """Show a class satisfying multiple Protocols simultaneously."""
    separator("Multiple Protocols")

    shapes = [Circle(3.0), Rectangle(4.0, 5.0)]

    # These shapes satisfy BOTH Shape and Drawable Protocols
    for shape in shapes:
        print(f"Shape Protocol: {isinstance(shape, Shape)}")
        print(f"Drawable Protocol: {isinstance(shape, Drawable)}")
        print(f"Scalable Protocol: {isinstance(shape, Scalable)}")
        print()

    # Render using Drawable Protocol
    drawings = render_all(shapes)
    print("Rendered shapes:")
    for drawing in drawings:
        print(f"  {drawing}")

    # Scale using Scalable Protocol
    scaled = scale_all(shapes, 2.0)
    print("\nScaled by 2x:")
    for original, scaled_shape in zip(shapes, scaled):
        print(f"  {original} → {scaled_shape}")
        print(f"    Original area: {original.area():.4f}")  # type: ignore
        print(f"    Scaled area:   {scaled_shape.area():.4f}")   # type: ignore


def demo_protocol_with_external_class() -> None:
    """Show Protocol working with a class we don't control."""
    separator("Protocol with External/Custom Class")

    # Imagine this class comes from a third-party library
    # We can't change it to inherit from our Shape Protocol
    class EllipseFromLibrary:
        """A class from a hypothetical third-party library."""
        def __init__(self, semi_major: float, semi_minor: float):
            self.semi_major = semi_major
            self.semi_minor = semi_minor

        def area(self) -> float:
            import math
            return math.pi * self.semi_major * self.semi_minor

        def perimeter(self) -> float:
            # Approximation formula
            import math
            a, b = self.semi_major, self.semi_minor
            h = ((a - b) / (a + b)) ** 2
            return math.pi * (a + b) * (1 + 3*h / (10 + math.sqrt(4 - 3*h)))

        def __str__(self) -> str:
            return f"Ellipse({self.semi_major}×{self.semi_minor})"

    ellipse = EllipseFromLibrary(5.0, 3.0)

    # Does it satisfy our Shape Protocol?
    print(f"Ellipse isinstance(Shape): {isinstance(ellipse, Shape)}")
    print("(Works because it has area() and perimeter()!)")

    # Can use it with our functions:
    print_shape_info(ellipse)

    # Mix it with our own shapes:
    shapes = [Circle(4.0), Rectangle(3.0, 7.0), ellipse]
    print_summary(shapes)


def demo_protocol_fail() -> None:
    """Show what happens when an object doesn't satisfy the Protocol."""
    separator("Protocol Failure")

    class NotAShape:
        """Missing area() — doesn't satisfy Shape Protocol."""
        def perimeter(self) -> float:
            return 0.0

    not_shape = NotAShape()
    print(f"isinstance(NotAShape, Shape): {isinstance(not_shape, Shape)}")

    # Static type checkers (mypy/pyright) would catch this:
    # print_shape_info(not_shape)  # Type error — missing area()

    # At runtime, duck typing fails when the method is called:
    try:
        print_shape_info(not_shape)   # type: ignore
    except AttributeError as e:
        print(f"Caught AttributeError: {e}")


def demo_summary_table() -> None:
    """Show the summary table function."""
    separator("Summary Table")

    shapes = [
        Circle(1.0),
        Circle(5.0),
        Rectangle(3.0, 4.0),
        Rectangle(10.0, 10.0),
        Triangle(3.0, 4.0, 5.0),
        RegularPolygon(3, 5.0),   # Equilateral triangle
        RegularPolygon(4, 5.0),   # Square
        RegularPolygon(6, 5.0),   # Hexagon
    ]

    print_summary(shapes)


if __name__ == "__main__":
    print("Shapes Demo — Python Protocols and Structural Typing")
    print("=" * 60)

    demo_protocol_without_inheritance()
    demo_polymorphic_functions()
    demo_multiple_protocols()
    demo_protocol_with_external_class()
    demo_protocol_fail()
    demo_summary_table()

    print("\n" + "=" * 60)
    print("Demo complete!")
