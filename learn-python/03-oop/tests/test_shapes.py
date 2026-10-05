"""
Tests for the Shapes module (Protocol-based polymorphism).

Run with:
    cd 03-oop && pytest tests/test_shapes.py -v
"""

import math
import pytest
import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'code', 'protocols'))

from shapes import (
    Shape, Drawable, Scalable,
    Circle, Rectangle, Triangle, RegularPolygon,
    total_area, find_largest, render_all, scale_all, print_summary,
)


class TestCircle:
    """Tests for Circle."""

    def test_circle_creation(self) -> None:
        c = Circle(5.0)
        assert c.radius == 5.0

    def test_circle_area(self) -> None:
        c = Circle(5.0)
        assert abs(c.area() - math.pi * 25) < 1e-9

    def test_circle_perimeter(self) -> None:
        c = Circle(5.0)
        assert abs(c.perimeter() - 2 * math.pi * 5) < 1e-9

    def test_circle_zero_radius_raises(self) -> None:
        with pytest.raises(ValueError):
            Circle(0.0)

    def test_circle_negative_radius_raises(self) -> None:
        with pytest.raises(ValueError):
            Circle(-1.0)

    def test_circle_satisfies_shape_protocol(self) -> None:
        c = Circle(3.0)
        assert isinstance(c, Shape)

    def test_circle_satisfies_drawable_protocol(self) -> None:
        c = Circle(3.0)
        assert isinstance(c, Drawable)

    def test_circle_satisfies_scalable_protocol(self) -> None:
        c = Circle(3.0)
        assert isinstance(c, Scalable)

    def test_circle_draw_returns_string(self) -> None:
        c = Circle(3.0)
        result = c.draw()
        assert isinstance(result, str)
        assert len(result) > 0

    def test_circle_scale(self) -> None:
        c = Circle(4.0)
        scaled = c.scale(2.0)
        assert scaled.radius == 8.0

    def test_circle_str(self) -> None:
        c = Circle(5.0)
        assert "5" in str(c) or "Circle" in str(c)

    @pytest.mark.parametrize("radius", [0.001, 0.1, 1.0, 10.0, 100.0])
    def test_circle_various_radii(self, radius: float) -> None:
        c = Circle(radius)
        assert c.area() > 0
        assert c.perimeter() > 0


class TestRectangle:
    """Tests for Rectangle."""

    def test_rectangle_creation(self) -> None:
        r = Rectangle(4.0, 6.0)
        assert r.width == 4.0
        assert r.height == 6.0

    def test_rectangle_area(self) -> None:
        r = Rectangle(4.0, 6.0)
        assert r.area() == 24.0

    def test_rectangle_perimeter(self) -> None:
        r = Rectangle(4.0, 6.0)
        assert r.perimeter() == 20.0

    def test_rectangle_zero_width_raises(self) -> None:
        with pytest.raises(ValueError):
            Rectangle(0.0, 5.0)

    def test_rectangle_negative_height_raises(self) -> None:
        with pytest.raises(ValueError):
            Rectangle(5.0, -1.0)

    def test_rectangle_is_square(self) -> None:
        square = Rectangle(5.0, 5.0)
        assert square.is_square is True

    def test_rectangle_is_not_square(self) -> None:
        rect = Rectangle(4.0, 6.0)
        assert rect.is_square is False

    def test_rectangle_satisfies_shape_protocol(self) -> None:
        r = Rectangle(3.0, 4.0)
        assert isinstance(r, Shape)

    def test_rectangle_scale(self) -> None:
        r = Rectangle(4.0, 6.0)
        scaled = r.scale(3.0)
        assert scaled.width == 12.0
        assert scaled.height == 18.0

    def test_rectangle_eq(self) -> None:
        r1 = Rectangle(4.0, 6.0)
        r2 = Rectangle(4.0, 6.0)
        assert r1 == r2


class TestTriangle:
    """Tests for Triangle."""

    def test_triangle_creation(self) -> None:
        t = Triangle(3.0, 4.0, 5.0)
        assert t.a == 3.0
        assert t.b == 4.0
        assert t.c == 5.0

    def test_triangle_perimeter(self) -> None:
        t = Triangle(3.0, 4.0, 5.0)
        assert t.perimeter() == 12.0

    def test_right_triangle_area(self) -> None:
        # 3-4-5 right triangle has area = 0.5 * 3 * 4 = 6.0
        t = Triangle(3.0, 4.0, 5.0)
        assert abs(t.area() - 6.0) < 1e-9

    def test_invalid_triangle_raises(self) -> None:
        with pytest.raises(ValueError):
            Triangle(1.0, 1.0, 10.0)   # Violates triangle inequality

    def test_zero_side_raises(self) -> None:
        with pytest.raises(ValueError):
            Triangle(0.0, 4.0, 5.0)

    def test_triangle_satisfies_shape_protocol(self) -> None:
        t = Triangle(3.0, 4.0, 5.0)
        assert isinstance(t, Shape)

    def test_equilateral_triangle_area(self) -> None:
        side = 4.0
        t = Triangle(side, side, side)
        expected_area = (math.sqrt(3) / 4) * side ** 2
        assert abs(t.area() - expected_area) < 1e-9


class TestRegularPolygon:
    """Tests for RegularPolygon."""

    def test_triangle(self) -> None:
        t = RegularPolygon(3, 5.0)
        assert t.sides == 3
        assert t.side_length == 5.0

    def test_square(self) -> None:
        s = RegularPolygon(4, 5.0)
        assert abs(s.area() - 25.0) < 1e-9   # 5x5 square
        assert abs(s.perimeter() - 20.0) < 1e-9

    def test_hexagon(self) -> None:
        h = RegularPolygon(6, 4.0)
        assert h.perimeter() == 24.0

    def test_less_than_3_sides_raises(self) -> None:
        with pytest.raises(ValueError):
            RegularPolygon(2, 5.0)

    def test_zero_side_length_raises(self) -> None:
        with pytest.raises(ValueError):
            RegularPolygon(4, 0.0)

    def test_satisfies_shape_protocol(self) -> None:
        p = RegularPolygon(5, 3.0)
        assert isinstance(p, Shape)


class TestProtocolCompliance:
    """Tests for Protocol-related behavior."""

    def test_shape_protocol_with_runtime_check(self) -> None:
        class NotAShape:
            pass

        assert not isinstance(NotAShape(), Shape)

    def test_custom_class_satisfying_protocol(self) -> None:
        class CustomShape:
            def area(self) -> float:
                return 42.0

            def perimeter(self) -> float:
                return 100.0

        custom = CustomShape()
        assert isinstance(custom, Shape)

    def test_custom_class_missing_method_not_protocol(self) -> None:
        class IncompleteShape:
            def area(self) -> float:   # Has area but no perimeter
                return 42.0

        incomplete = IncompleteShape()
        assert not isinstance(incomplete, Shape)

    def test_no_inheritance_needed(self) -> None:
        # None of our shapes inherit from Shape Protocol
        assert Shape not in Circle.__bases__
        assert Shape not in Rectangle.__bases__
        assert Shape not in Triangle.__bases__

    def test_all_shapes_satisfy_shape_protocol(self) -> None:
        shapes = [
            Circle(5.0),
            Rectangle(3.0, 4.0),
            Triangle(3.0, 4.0, 5.0),
            RegularPolygon(5, 3.0),
        ]
        for shape in shapes:
            assert isinstance(shape, Shape), f"{type(shape).__name__} should satisfy Shape"

    def test_all_shapes_satisfy_drawable_protocol(self) -> None:
        shapes = [Circle(5.0), Rectangle(3.0, 4.0)]
        for shape in shapes:
            assert isinstance(shape, Drawable)


class TestPolymorphicFunctions:
    """Tests for functions that work with Protocols."""

    @pytest.fixture
    def shapes(self) -> list:
        return [
            Circle(3.0),
            Rectangle(4.0, 5.0),
            Triangle(3.0, 4.0, 5.0),
        ]

    def test_total_area(self, shapes: list) -> None:
        result = total_area(shapes)
        expected = sum(s.area() for s in shapes)
        assert abs(result - expected) < 1e-9

    def test_total_area_empty_list(self) -> None:
        assert total_area([]) == 0.0

    def test_find_largest(self, shapes: list) -> None:
        largest = find_largest(shapes)
        # Find expected largest manually
        expected = max(shapes, key=lambda s: s.area())
        assert largest is expected

    def test_find_largest_empty_raises(self) -> None:
        with pytest.raises(ValueError):
            find_largest([])

    def test_render_all(self) -> None:
        shapes = [Circle(3.0), Rectangle(4.0, 5.0)]
        results = render_all(shapes)
        assert len(results) == 2
        for result in results:
            assert isinstance(result, str)

    def test_scale_all(self) -> None:
        shapes = [Circle(3.0), Rectangle(4.0, 5.0)]
        scaled = scale_all(shapes, 2.0)
        assert len(scaled) == 2
        # Circle should have doubled radius
        assert scaled[0].radius == 6.0   # type: ignore
        # Rectangle should have doubled dimensions
        assert scaled[1].width == 8.0    # type: ignore

    def test_print_summary_doesnt_crash(
        self, shapes: list, capsys: pytest.CaptureFixture
    ) -> None:
        print_summary(shapes)
        captured = capsys.readouterr()
        assert "Total" in captured.out

    @pytest.mark.parametrize("factor", [0.5, 1.0, 2.0, 10.0])
    def test_scale_changes_area(self, factor: float) -> None:
        circle = Circle(5.0)
        original_area = circle.area()
        scaled = circle.scale(factor)
        expected_area = original_area * (factor ** 2)
        assert abs(scaled.area() - expected_area) < 1e-9
