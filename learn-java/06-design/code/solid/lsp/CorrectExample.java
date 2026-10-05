package solid.lsp;

/**
 * ============================================================
 *  LSP CORRECT EXAMPLE — Liskov Substitution Principle
 * ============================================================
 *
 *  THE PROBLEM RECAP:
 *    In ViolationExample.java, Square extends Rectangle but breaks its
 *    mutable setWidth/setHeight contract. The root cause is that we
 *    tried to model a mathematical relationship ("a square is a rectangle")
 *    in an OO system with MUTABLE setters. That combination doesn't work.
 *
 *  TWO CORRECT APPROACHES ARE SHOWN HERE:
 *
 *  APPROACH A — Immutable Shape Hierarchy (preferred)
 *    Make Rectangle and Square IMMUTABLE (no setters).
 *    Both implement a common Shape interface.
 *    An immutable Square has no reason to break Rectangle's contract
 *    because there IS no mutable contract to break.
 *    Each shape is created with fixed dimensions and never changes.
 *
 *  APPROACH B — Flat Hierarchy (no inheritance between Rectangle and Square)
 *    Rectangle and Square are sibling classes, both implementing Shape.
 *    Neither inherits from the other.
 *    This is the correct model: the mathematical "is-a" is acknowledged,
 *    but they are NOT forced into an inheritance relationship that would
 *    create mutable-setter conflicts.
 *
 *  KEY INSIGHT:
 *    "Favour composition and interface implementation over inheritance
 *     when the inheritance would break behavioral contracts."
 *
 *  WHEN IS INHERITANCE LSP-SAFE?
 *    When the subclass only ADDS new behavior and does NOT OVERRIDE
 *    methods in ways that weaken post-conditions or violate invariants.
 *    Example: ColoredRectangle extends Rectangle by adding a color field
 *    and getter. It doesn't override setWidth/setHeight, so LSP holds.
 */
public class CorrectExample {

    // =========================================================================
    //  APPROACH A: Immutable Shape Hierarchy
    //
    //  The trick: remove the mutable setters. Shapes are created once and
    //  never modified. There is nothing to "violate" because there are no
    //  setters whose post-conditions could be broken.
    //
    //  If you need a "resized" shape, you create a NEW shape object.
    //  (This is also the approach used by Java's BigDecimal, String, etc.)
    // =========================================================================

    /**
     * Common interface for all shapes.
     *
     * Notice: NO setters in this interface. Shapes are value objects.
     * The contract is simple: every shape can report its area and perimeter.
     */
    interface Shape {
        double getArea();
        double getPerimeter();
        String getDescription();
    }

    /**
     * Immutable Rectangle — LSP-safe because it has no mutable setters.
     *
     * LSP COMPLIANCE:
     *   Any class implementing Shape can be substituted for any other Shape.
     *   The contract is: getArea() returns a meaningful area, getPerimeter()
     *   returns a meaningful perimeter. Both Rectangle and Square honor this.
     */
    static final class ImmutableRectangle implements Shape {

        private final double width;
        private final double height;

        public ImmutableRectangle(double width, double height) {
            if (width  <= 0) throw new IllegalArgumentException("Width must be positive");
            if (height <= 0) throw new IllegalArgumentException("Height must be positive");
            this.width  = width;
            this.height = height;
        }

        public double getWidth()  { return width;  }
        public double getHeight() { return height; }

        @Override
        public double getArea() { return width * height; }

        @Override
        public double getPerimeter() { return 2 * (width + height); }

        /**
         * Returns a NEW rectangle with a different width.
         * Original rectangle is unchanged — no contract violation possible.
         *
         * LSP COMPLIANCE: Instead of setWidth (which caused the problem),
         * we return a new object. The caller always gets what they asked for
         * without any hidden side-effects.
         */
        public ImmutableRectangle withWidth(double newWidth) {
            return new ImmutableRectangle(newWidth, this.height);
        }

        /** Returns a NEW rectangle with a different height. */
        public ImmutableRectangle withHeight(double newHeight) {
            return new ImmutableRectangle(this.width, newHeight);
        }

        @Override
        public String getDescription() {
            return String.format("Rectangle[%.1f x %.1f, area=%.1f]", width, height, getArea());
        }
    }

    /**
     * Immutable Square — LSP-safe subtype of Shape.
     *
     * LSP COMPLIANCE:
     *   Square does NOT extend Rectangle (no inheritance relationship).
     *   Both implement Shape independently. There is no broken contract
     *   because Square never claims to be a mutable Rectangle.
     *
     *   A Square CAN safely be passed to any method that accepts a Shape,
     *   because the Shape contract (getArea, getPerimeter) is correctly
     *   and consistently implemented.
     */
    static final class ImmutableSquare implements Shape {

        private final double side;

        public ImmutableSquare(double side) {
            if (side <= 0) throw new IllegalArgumentException("Side must be positive");
            this.side = side;
        }

        public double getSide() { return side; }

        @Override
        public double getArea() { return side * side; }

        @Override
        public double getPerimeter() { return 4 * side; }

        /** Returns a NEW square with a different side length. */
        public ImmutableSquare withSide(double newSide) {
            return new ImmutableSquare(newSide);
        }

        @Override
        public String getDescription() {
            return String.format("Square[%.1f x %.1f, area=%.1f]", side, side, getArea());
        }
    }

    // =========================================================================
    //  APPROACH B: Safe Mutable Hierarchy (ColoredRectangle example)
    //
    //  If you DO need mutable shapes AND inheritance, the rule is:
    //    Only EXTEND the base class — do not OVERRIDE behavior in ways that
    //    weaken contracts.
    //
    //  ColoredRectangle adds a color field and getter but never overrides
    //  setWidth or setHeight. Therefore it is fully LSP-compliant.
    // =========================================================================

    /** A mutable Rectangle (with setters) as a controlled base class. */
    static class MutableRectangle {

        protected double width;
        protected double height;

        public MutableRectangle(double width, double height) {
            this.width  = width;
            this.height = height;
        }

        /**
         * Sets width. Contract: width = newWidth, height is UNCHANGED.
         * This post-condition is documented and must be honored by all subclasses.
         */
        public void setWidth(double newWidth) { this.width = newWidth; }

        /**
         * Sets height. Contract: height = newHeight, width is UNCHANGED.
         */
        public void setHeight(double newHeight) { this.height = newHeight; }

        public double getWidth()    { return width;  }
        public double getHeight()   { return height; }
        public double getArea()     { return width * height; }
        public double getPerimeter(){ return 2 * (width + height); }

        @Override
        public String toString() {
            return getClass().getSimpleName()
                    + "[width=" + width + ", height=" + height + "]";
        }
    }

    /**
     * LSP-SAFE subclass: ColoredRectangle extends MutableRectangle.
     *
     * LSP COMPLIANCE:
     *   ColoredRectangle only ADDS a color field. It does NOT override
     *   setWidth or setHeight. The parent's post-conditions are fully
     *   preserved. Anywhere a MutableRectangle is used, a ColoredRectangle
     *   can be substituted and everything works correctly.
     */
    static class ColoredRectangle extends MutableRectangle {

        private String color;

        public ColoredRectangle(double width, double height, String color) {
            super(width, height);
            this.color = color;
        }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        // setWidth and setHeight are NOT overridden — parent contract is preserved.
        // This is exactly what LSP requires.

        @Override
        public String toString() {
            return "ColoredRectangle[width=" + width
                    + ", height=" + height
                    + ", color=" + color + "]";
        }
    }

    // =========================================================================
    //  UTILITY METHOD — works correctly with ANY Shape implementation
    //
    //  This method uses only the Shape interface. Thanks to LSP, we can pass
    //  either ImmutableRectangle or ImmutableSquare and get correct results.
    // =========================================================================

    /**
     * Prints shape information.
     *
     * LSP IN ACTION:
     *   This method was written once against the Shape interface.
     *   It works correctly for EVERY Shape implementation.
     *   We never need to check instanceof or cast.
     *   Substituting any Shape for any other Shape produces correct results.
     */
    static void printShapeInfo(Shape shape) {
        System.out.println("  Shape       : " + shape.getDescription());
        System.out.printf ("  Area        : %.2f%n", shape.getArea());
        System.out.printf ("  Perimeter   : %.2f%n", shape.getPerimeter());
    }

    /**
     * Uses MutableRectangle's contract — works correctly for Rectangle AND
     * ColoredRectangle (LSP-safe subclass), but would FAIL for the Square
     * from ViolationExample.
     */
    static void resizeAndVerify(MutableRectangle r) {
        System.out.println("  Before: " + r);
        r.setWidth(5);
        r.setHeight(10);
        System.out.println("  After : " + r);
        double area = r.getArea();
        System.out.printf("  Area  : %.1f (expected 50.0) — %s%n",
                area, area == 50.0 ? "PASS" : "FAIL");
    }

    // =========================================================================
    //  MAIN — Demonstrating LSP-compliant substitution
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== LSP CORRECT EXAMPLE ===\n");

        // ----- APPROACH A: Immutable shapes via common interface -----
        System.out.println("--- Approach A: Immutable Shapes ---");

        Shape rect   = new ImmutableRectangle(4, 6);
        Shape square = new ImmutableSquare(5);

        // Both ImmutableRectangle and ImmutableSquare are substitutable for Shape.
        // printShapeInfo() works correctly for BOTH — no instanceof, no surprises.
        System.out.println("Rectangle:");
        printShapeInfo(rect);
        System.out.println("Square:");
        printShapeInfo(square);
        System.out.println();

        // Demonstrate immutable "resize" — returns new objects, never mutates
        System.out.println("--- Immutable resize (returns new objects) ---");
        ImmutableRectangle original = new ImmutableRectangle(3, 4);
        ImmutableRectangle resized  = original.withWidth(5).withHeight(10);
        System.out.println("Original : " + original.getDescription());
        System.out.println("Resized  : " + resized.getDescription());
        System.out.printf("Resized area: %.1f (expected 50.0) — %s%n",
                resized.getArea(), resized.getArea() == 50.0 ? "PASS" : "FAIL");
        System.out.println();

        // ----- Store a mix of shapes in a list — LSP enables polymorphism -----
        System.out.println("--- Polymorphic list of shapes ---");
        Shape[] shapes = {
            new ImmutableRectangle(3, 7),
            new ImmutableSquare(4),
            new ImmutableRectangle(5, 5),
            new ImmutableSquare(6),
        };

        double totalArea = 0;
        for (Shape s : shapes) {
            // Any Shape can be substituted here — no special cases needed.
            System.out.println("  " + s.getDescription());
            totalArea += s.getArea();
        }
        System.out.printf("Total area: %.1f%n%n", totalArea);

        // ----- APPROACH B: Mutable Rectangle with LSP-safe subclass -----
        System.out.println("--- Approach B: Mutable hierarchy (LSP-safe subclass) ---");

        MutableRectangle rect1 = new MutableRectangle(3, 4);
        // ColoredRectangle is substitutable for MutableRectangle — LSP holds!
        MutableRectangle rect2 = new ColoredRectangle(3, 4, "blue");

        System.out.println("Testing plain MutableRectangle:");
        resizeAndVerify(rect1);
        System.out.println();

        System.out.println("Testing ColoredRectangle (substituted for MutableRectangle):");
        resizeAndVerify(rect2); // Works correctly! LSP is respected.
        System.out.println();

        // ----- Summary -----
        System.out.println("=== LSP TAKEAWAY ===");
        System.out.println("Rule 1: If you must use inheritance, never override methods");
        System.out.println("        in ways that weaken post-conditions.");
        System.out.println("Rule 2: If the subclass needs to break the parent's contract,");
        System.out.println("        don't use inheritance — use a shared interface instead.");
        System.out.println("Rule 3: Immutable value objects sidestep the problem entirely");
        System.out.println("        because there are no mutable contracts to violate.");
        System.out.println("Rule 4: 'Is-a' in math != 'is-a' in OO when mutability is involved.");
    }
}
