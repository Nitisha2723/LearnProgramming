import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ShapeTest — tests for Circle and Rectangle implementing Drawable and Resizable.
 *
 * KEY TESTING CONCEPTS:
 * - Testing interface implementations
 * - Verifying default method behavior
 * - Testing polymorphism through interface references
 * - Floating-point comparison with delta tolerance
 */
@DisplayName("Shape Tests")
class ShapeTest {

    // =========================================================================
    // Circle Tests
    // =========================================================================

    @Nested
    @DisplayName("Circle Tests")
    class CircleTests {
        private Circle circle;

        @BeforeEach
        void setUp() {
            circle = new Circle(0, 0, 5.0, "red");
        }

        @Test
        @DisplayName("Circle should calculate correct area")
        void circleShouldCalculateCorrectArea() {
            double expectedArea = Math.PI * 5.0 * 5.0;
            assertEquals(expectedArea, circle.getArea(), 0.001);
        }

        @Test
        @DisplayName("Circle should calculate correct circumference")
        void circleShouldCalculateCorrectCircumference() {
            double expectedCircumference = 2 * Math.PI * 5.0;
            assertEquals(expectedCircumference, circle.getCircumference(), 0.001);
        }

        @Test
        @DisplayName("Circle should contain point at center")
        void circleShouldContainPointAtCenter() {
            assertTrue(circle.contains(0, 0));
        }

        @Test
        @DisplayName("Circle should contain point inside radius")
        void circleShouldContainPointInsideRadius() {
            assertTrue(circle.contains(3, 4));  // Distance from center = 5.0
        }

        @Test
        @DisplayName("Circle should not contain point outside radius")
        void circleShouldNotContainPointOutsideRadius() {
            assertFalse(circle.contains(6, 0));  // 6 > 5 (radius)
        }

        @Test
        @DisplayName("Circle should start visible")
        void circleShouldStartVisible() {
            assertTrue(circle.isVisible());
        }

        @Test
        @DisplayName("Hide should make circle invisible")
        void hideShouldMakeCircleInvisible() {
            circle.hide();
            assertFalse(circle.isVisible());
        }

        @Test
        @DisplayName("Toggle should flip visibility")
        void toggleShouldFlipVisibility() {
            assertTrue(circle.isVisible());
            circle.toggle();
            assertFalse(circle.isVisible());
            circle.toggle();
            assertTrue(circle.isVisible());
        }

        @Test
        @DisplayName("Constructor should reject non-positive radius")
        void constructorShouldRejectNonPositiveRadius() {
            assertThrows(IllegalArgumentException.class,
                () -> new Circle(0, 0, 0));
            assertThrows(IllegalArgumentException.class,
                () -> new Circle(0, 0, -5.0));
        }
    }

    // =========================================================================
    // Circle Resizable Tests
    // =========================================================================

    @Nested
    @DisplayName("Circle Resize Tests")
    class CircleResizeTests {
        private Circle circle;

        @BeforeEach
        void setUp() {
            circle = new Circle(0, 0, 4.0);
        }

        @Test
        @DisplayName("resize(2.0) should double the radius")
        void resizeShouldDoubleRadius() {
            circle.resize(2.0);
            assertEquals(8.0, circle.getRadius(), 0.001);
        }

        @Test
        @DisplayName("resize(0.5) should halve the radius")
        void resizeShouldHalveRadius() {
            circle.resize(0.5);
            assertEquals(2.0, circle.getRadius(), 0.001);
        }

        @Test
        @DisplayName("doubleSize() default method should double radius")
        void doubleSizeShouldDoubleRadius() {
            double radiusBefore = circle.getRadius();
            circle.doubleSize();  // Default method from Resizable
            assertEquals(radiusBefore * 2, circle.getRadius(), 0.001);
        }

        @Test
        @DisplayName("halfSize() default method should halve radius")
        void halfSizeShouldHalveRadius() {
            double radiusBefore = circle.getRadius();
            circle.halfSize();  // Default method from Resizable
            assertEquals(radiusBefore / 2, circle.getRadius(), 0.001);
        }

        @Test
        @DisplayName("resetSize() should restore original radius")
        void resetSizeShouldRestoreOriginalRadius() {
            double originalRadius = circle.getRadius();
            circle.resize(3.0);  // Change it
            circle.resetSize();  // Reset
            assertEquals(originalRadius, circle.getRadius(), 0.001);
        }

        @Test
        @DisplayName("resize should reject non-positive factor")
        void resizeShouldRejectNonPositiveFactor() {
            assertThrows(IllegalArgumentException.class,
                () -> circle.resize(0.0));
            assertThrows(IllegalArgumentException.class,
                () -> circle.resize(-1.0));
        }
    }

    // =========================================================================
    // Rectangle Tests
    // =========================================================================

    @Nested
    @DisplayName("Rectangle Tests")
    class RectangleTests {
        private Rectangle rectangle;

        @BeforeEach
        void setUp() {
            rectangle = new Rectangle(0, 0, 8.0, 4.0, "blue");
        }

        @Test
        @DisplayName("Rectangle should calculate correct area")
        void rectangleShouldCalculateCorrectArea() {
            assertEquals(32.0, rectangle.getArea(), 0.001);  // 8 * 4
        }

        @Test
        @DisplayName("Rectangle should calculate correct perimeter")
        void rectangleShouldCalculateCorrectPerimeter() {
            assertEquals(24.0, rectangle.getPerimeter(), 0.001);  // 2*(8+4)
        }

        @Test
        @DisplayName("Non-square rectangle should not be a square")
        void nonSquareRectangleShouldNotBeSquare() {
            assertFalse(rectangle.isSquare());
        }

        @Test
        @DisplayName("Square rectangle should be a square")
        void squareRectangleShouldBeSquare() {
            Rectangle square = new Rectangle(0, 0, 5.0, 5.0);
            assertTrue(square.isSquare());
        }

        @Test
        @DisplayName("Rectangle should contain point inside")
        void rectangleShouldContainPointInside() {
            assertTrue(rectangle.contains(4, 2));  // Inside 8x4 rect
        }

        @Test
        @DisplayName("Rectangle should not contain point outside")
        void rectangleShouldNotContainPointOutside() {
            assertFalse(rectangle.contains(9, 2));  // x=9 > width=8
        }
    }

    // =========================================================================
    // Interface Polymorphism Tests
    // =========================================================================

    @Nested
    @DisplayName("Interface Polymorphism Tests")
    class InterfacePolymorphismTests {

        @Test
        @DisplayName("Circle should implement Drawable")
        void circleShouldImplementDrawable() {
            Circle c = new Circle(0, 0, 5.0);
            assertTrue(c instanceof Drawable);
        }

        @Test
        @DisplayName("Circle should implement Resizable")
        void circleShouldImplementResizable() {
            Circle c = new Circle(0, 0, 5.0);
            assertTrue(c instanceof Resizable);
        }

        @Test
        @DisplayName("Rectangle should implement Drawable")
        void rectangleShouldImplementDrawable() {
            Rectangle r = new Rectangle(0, 0, 4.0, 3.0);
            assertTrue(r instanceof Drawable);
        }

        @Test
        @DisplayName("Rectangle should implement Resizable")
        void rectangleShouldImplementResizable() {
            Rectangle r = new Rectangle(0, 0, 4.0, 3.0);
            assertTrue(r instanceof Resizable);
        }

        @Test
        @DisplayName("Drawable reference should call correct draw() for Circle")
        void drawableReferenceCircleShouldWork() {
            // No exception = pass (draw writes to System.out, we just verify it doesn't throw)
            Drawable d = new Circle(0, 0, 5.0);
            assertDoesNotThrow(() -> d.draw());
        }

        @Test
        @DisplayName("Drawable reference should call correct draw() for Rectangle")
        void drawableReferenceRectangleShouldWork() {
            Drawable d = new Rectangle(0, 0, 4.0, 3.0);
            assertDoesNotThrow(() -> d.draw());
        }

        @Test
        @DisplayName("Resizable reference should resize Circle correctly")
        void resizableReferenceCircleShouldResize() {
            Circle c = new Circle(0, 0, 4.0);
            Resizable r = c;  // Upcast to interface
            r.resize(2.0);
            assertEquals(8.0, c.getRadius(), 0.001);  // Check via Circle reference
        }

        @Test
        @DisplayName("Drawable.isValidSize static method should work correctly")
        void drawableIsValidSizeShouldWork() {
            assertTrue(Drawable.isValidSize(1.0));
            assertTrue(Drawable.isValidSize(0.001));
            assertFalse(Drawable.isValidSize(0.0));
            assertFalse(Drawable.isValidSize(-5.0));
        }

        @Test
        @DisplayName("Drawable.DEFAULT_COLOR constant should be accessible")
        void drawableDefaultColorShouldBeAccessible() {
            assertNotNull(Drawable.DEFAULT_COLOR);
            assertFalse(Drawable.DEFAULT_COLOR.isEmpty());
        }
    }

    // =========================================================================
    // Equality Tests
    // =========================================================================

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Two identical circles should be equal")
        void twoIdenticalCirclesShouldBeEqual() {
            Circle c1 = new Circle(5, 5, 3.0);
            Circle c2 = new Circle(5, 5, 3.0);
            assertEquals(c1, c2);
        }

        @Test
        @DisplayName("Two circles with different radii should not be equal")
        void twoDifferentCirclesShouldNotBeEqual() {
            Circle c1 = new Circle(5, 5, 3.0);
            Circle c2 = new Circle(5, 5, 4.0);
            assertNotEquals(c1, c2);
        }

        @Test
        @DisplayName("Equal circles should have equal hash codes")
        void equalCirclesShouldHaveEqualHashCodes() {
            Circle c1 = new Circle(5, 5, 3.0);
            Circle c2 = new Circle(5, 5, 3.0);
            assertEquals(c1.hashCode(), c2.hashCode());
        }
    }
}
