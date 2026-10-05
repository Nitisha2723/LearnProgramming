package solid.lsp;

/**
 * ============================================================
 *  SOLID PRINCIPLE #3 — Liskov Substitution Principle (LSP)
 * ============================================================
 *
 *  DEFINITION:
 *    "If S is a subtype of T, then objects of type T may be replaced with
 *     objects of type S without altering any of the desirable properties
 *     of the program."
 *    — Barbara Liskov (1987 ACM Turing Award Lecture)
 *
 *  In plain English:
 *    Anywhere you use a BASE class, you should be able to swap in a SUBCLASS
 *    and the code should still work CORRECTLY — not just "not crash", but
 *    produce the SAME results and respect the SAME contracts.
 *
 *  PRE-CONDITIONS and POST-CONDITIONS (the Liskov contract rules):
 *    - A subclass may NOT STRENGTHEN pre-conditions
 *      (it cannot require MORE from callers than the base class did).
 *    - A subclass may NOT WEAKEN post-conditions
 *      (it cannot guarantee LESS than what the base class promised).
 *    - A subclass must preserve the INVARIANTS of the base class
 *      (stable truths about the object's state must remain true).
 *
 *  THE CLASSIC VIOLATION: Rectangle / Square
 *    Mathematically, a Square IS-A Rectangle (every square is a rectangle).
 *    Intuitively, inheritance feels correct: Square extends Rectangle.
 *
 *    BUT in object-oriented terms with mutable setters, the Square subclass
 *    BREAKS the Rectangle contract. The code below demonstrates exactly why.
 */
public class ViolationExample {

    // =========================================================================
    //  BASE CLASS: Rectangle
    //
    //  Contract (invariants and post-conditions):
    //    1. setWidth(w)  sets the width to exactly w; height is UNCHANGED.
    //    2. setHeight(h) sets the height to exactly h; width is UNCHANGED.
    //    3. getArea()    returns width * height.
    //
    //  These three guarantees form the "contract" of Rectangle. Any subclass
    //  that claims to be substitutable for Rectangle MUST honor this contract.
    // =========================================================================

    static class Rectangle {

        // width and height are protected so Square can technically override them,
        // but as we'll see, Square's override VIOLATES the parent's contract.
        protected double width;
        protected double height;

        public Rectangle(double width, double height) {
            this.width  = width;
            this.height = height;
        }

        /**
         * Sets the width.
         *
         * CONTRACT (post-condition):
         *   After this call, getWidth() == width AND getHeight() is UNCHANGED.
         *   Callers can rely on the height not being touched.
         */
        public void setWidth(double width) {
            this.width = width;
        }

        /**
         * Sets the height.
         *
         * CONTRACT (post-condition):
         *   After this call, getHeight() == height AND getWidth() is UNCHANGED.
         *   Callers can rely on the width not being touched.
         */
        public void setHeight(double height) {
            this.height = height;
        }

        public double getWidth()  { return width;  }
        public double getHeight() { return height; }

        /**
         * Calculates area.
         *
         * CONTRACT (post-condition):
         *   Returns width * height. Always.
         */
        public double getArea() {
            return width * height;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()
                    + "[width=" + width + ", height=" + height + ", area=" + getArea() + "]";
        }
    }

    // =========================================================================
    //  SUBCLASS: Square — THE LSP VIOLATION
    //
    //  A Square OVERRIDES setWidth and setHeight to keep both dimensions equal.
    //  This seems logical (a square must have equal sides), but it SILENTLY
    //  BREAKS the Rectangle contract:
    //
    //    Rectangle.setWidth(w) promises: width = w, height UNCHANGED.
    //    Square.setWidth(w)    does:     width = w, height = w (CHANGED!).
    //
    //  This is a WEAKENED POST-CONDITION:
    //    Rectangle's post-condition: "height is unchanged"
    //    Square's post-condition: "height may change" ← weaker guarantee
    //
    //  This means Square is NOT substitutable for Rectangle.
    //  Code written to work with Rectangle will BREAK silently with Square.
    //  The violation is subtle — it compiles and runs, but produces WRONG results.
    // =========================================================================

    static class Square extends Rectangle {

        public Square(double side) {
            super(side, side); // Both dimensions start equal — OK so far.
        }

        /**
         * VIOLATION: Overrides setWidth to also set height.
         *
         * WHY THIS VIOLATES LSP:
         *   The Rectangle contract says setWidth changes ONLY the width.
         *   But here, setting the width ALSO changes the height.
         *   A caller who holds a Rectangle reference (pointing to a Square at
         *   runtime) will see their height change when they only asked to change
         *   the width. This is unexpected and breaks the contract.
         *
         *   POST-CONDITION VIOLATION:
         *     Expected: setWidth(5) → width=5, height unchanged
         *     Actual:   setWidth(5) → width=5, height=5 (CHANGED!)
         */
        @Override
        public void setWidth(double width) {
            // Force both dimensions to stay equal — this BREAKS Rectangle's contract
            this.width  = width;
            this.height = width; // ← THIS is the violation
        }

        /**
         * VIOLATION: Overrides setHeight to also set width.
         *
         * WHY THIS VIOLATES LSP:
         *   Symmetric violation to setWidth. Setting height also sets width.
         *   A caller who sets height expecting width to be preserved will be
         *   surprised to find width has also changed.
         *
         *   POST-CONDITION VIOLATION:
         *     Expected: setHeight(8) → height=8, width unchanged
         *     Actual:   setHeight(8) → height=8, width=8 (CHANGED!)
         */
        @Override
        public void setHeight(double height) {
            // Force both dimensions to stay equal — this BREAKS Rectangle's contract
            this.width  = height; // ← THIS is the violation
            this.height = height;
        }
    }

    // =========================================================================
    //  CODE THAT RELIES ON THE RECTANGLE CONTRACT
    //
    //  This method was written with Rectangle's contract in mind:
    //    "I'll set the width to 5, then set the height to 10.
    //     The area should be 5 × 10 = 50."
    //
    //  This is a completely reasonable assumption for a Rectangle.
    //  But when a Square is passed in, it produces the WRONG answer silently.
    // =========================================================================

    /**
     * A method that uses Rectangle's contract.
     *
     * EXPECTED BEHAVIOR (for any legitimate Rectangle):
     *   setWidth(5) then setHeight(10) → area = 5 * 10 = 50
     *
     * WHY THIS BREAKS WITH SQUARE:
     *   When r is actually a Square:
     *     r.setWidth(5)   → width=5, height=5  (Square forces both equal)
     *     r.setHeight(10) → width=10, height=10 (Square forces both equal again!)
     *     r.getArea()     → 10 * 10 = 100       ← WRONG! Expected 50.
     *
     *   The method has no bug. The Square has no obvious bug either — it
     *   correctly maintains the "equal sides" invariant. But the SUBSTITUTION
     *   breaks the system. This is the essence of the LSP violation.
     */
    static void resizeAndPrint(Rectangle r) {
        System.out.println("Before resize: " + r);

        r.setWidth(5);    // Caller expects: width=5, height unchanged
        r.setHeight(10);  // Caller expects: height=10, width still 5

        double area = r.getArea();
        System.out.println("After resize : " + r);
        System.out.printf("Expected area: 50.0  |  Actual area: %.1f%n", area);

        // The assertion that should hold for ANY Rectangle:
        if (area == 50.0) {
            System.out.println("ASSERTION PASSED: area == 50");
        } else {
            System.out.println("ASSERTION FAILED: area should be 50 but was " + area);
            System.out.println("  -> Square is NOT substitutable for Rectangle!");
            System.out.println("  -> This is the LSP violation.");
        }
    }

    // =========================================================================
    //  MAIN — Demonstrating the violation
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== LSP VIOLATION DEMO ===\n");

        // ----- Test with a real Rectangle — works correctly -----
        System.out.println("--- Test 1: Rectangle (should work) ---");
        Rectangle rect = new Rectangle(3, 4);
        resizeAndPrint(rect);
        System.out.println();

        // ----- Test with a Square — LSP violation exposed -----
        // The method resizeAndPrint() expects a Rectangle.
        // We pass a Square (which extends Rectangle), so it compiles.
        // But the behavior is WRONG — the area is 100, not 50.
        System.out.println("--- Test 2: Square (LSP violation!) ---");
        Rectangle squareAsRectangle = new Square(3); // Stored as Rectangle ref (valid!)
        resizeAndPrint(squareAsRectangle);
        System.out.println();

        // ----- Direct demonstration of the broken setters -----
        System.out.println("--- Test 3: Direct setter demonstration ---");
        Square square = new Square(4);
        System.out.println("Initial: " + square);

        System.out.println("Calling setWidth(6) ...");
        square.setWidth(6);
        System.out.println("After setWidth(6): " + square);
        // Expected: width=6, height=4 (unchanged)
        // Actual:   width=6, height=6 (height was ALSO changed — surprise!)
        System.out.println("Surprise! Height also changed to 6 (was 4). Why?");
        System.out.println("Because Square.setWidth() silently modifies height too.");
        System.out.println();

        System.out.println("Calling setHeight(10) ...");
        square.setHeight(10);
        System.out.println("After setHeight(10): " + square);
        // Expected: height=10, width=6 (unchanged from previous step)
        // Actual:   height=10, width=10 (width was ALSO changed — another surprise!)
        System.out.println("Surprise! Width also changed to 10 (was 6).");
        System.out.println();

        System.out.println("CONCLUSION: Square cannot be substituted for Rectangle.");
        System.out.println("The mathematical 'is-a' relationship does NOT always");
        System.out.println("translate to a correct OO inheritance relationship.");
        System.out.println("\nSee CorrectExample.java for the LSP-compliant solution.");
    }
}
