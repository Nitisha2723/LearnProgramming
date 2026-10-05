import java.util.ArrayList;
import java.util.List;

/**
 * ShapesDemo — demonstrates multiple interface implementation and polymorphism.
 *
 * KEY CONCEPTS SHOWN:
 * 1. Objects implementing multiple interfaces (Circle and Rectangle both implement
 *    Drawable AND Resizable)
 * 2. Polymorphic collections of Drawable objects
 * 3. Polymorphic collections of Resizable objects
 * 4. Interface references (a variable of type Drawable can hold Circle or Rectangle)
 * 5. Default methods from interfaces
 */
public class ShapesDemo {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Shapes with Multiple Interfaces Demo");
        System.out.println("=".repeat(60));

        demo1_InterfaceReferences();
        demo2_DrawableCollection();
        demo3_ResizableCollection();
        demo4_DefaultMethods();
        demo5_StaticInterfaceMethod();
    }

    // =========================================================================
    // Demo 1: Interface References
    // =========================================================================
    static void demo1_InterfaceReferences() {
        System.out.println("\n--- Demo 1: Interface References ---");

        Circle circle = new Circle(0, 0, 5.0, "red");
        Rectangle rect = new Rectangle(10, 10, 8.0, 4.0, "blue");

        // A Circle IS a Drawable
        Drawable d1 = circle;    // Upcast to interface type
        d1.draw();               // Calls Circle's draw()

        // A Rectangle IS ALSO a Drawable
        Drawable d2 = rect;      // Same interface type, different implementation
        d2.draw();               // Calls Rectangle's draw()

        System.out.println();

        // A Circle IS ALSO a Resizable
        Resizable r1 = circle;   // Same circle object, referenced as Resizable
        r1.resize(2.0);          // Calls Circle's resize()
        circle.draw();           // See the updated size

        // A Rectangle IS ALSO a Resizable
        Resizable r2 = rect;
        r2.halfSize();           // Uses Resizable's default method → calls resize(0.5)
        rect.draw();
    }

    // =========================================================================
    // Demo 2: Polymorphic Collection of Drawables
    // =========================================================================
    static void demo2_DrawableCollection() {
        System.out.println("\n--- Demo 2: Drawing All Shapes (Polymorphic) ---");

        // A list that can hold ANY Drawable — Circle, Rectangle, or future shapes
        List<Drawable> shapes = new ArrayList<>();
        shapes.add(new Circle(0, 0, 3.0, "green"));
        shapes.add(new Rectangle(5, 5, 10.0, 6.0, "purple"));
        shapes.add(new Circle(15, 0, 2.5, "orange"));
        shapes.add(new Rectangle(0, 20, 4.0, 4.0, "red"));  // This is a square!

        // Draw them all — single loop, all types handled
        System.out.println("Drawing all shapes:");
        drawAll(shapes);

        // Hide all, then draw again
        System.out.println("\nHiding all shapes:");
        for (Drawable shape : shapes) {
            shape.hide();
        }
        System.out.println("\nAttempting to draw hidden shapes:");
        drawAll(shapes);
    }

    // =========================================================================
    // Demo 3: Polymorphic Collection of Resizables
    // =========================================================================
    static void demo3_ResizableCollection() {
        System.out.println("\n--- Demo 3: Resizing All Shapes ---");

        List<Resizable> resizables = new ArrayList<>();
        resizables.add(new Circle(0, 0, 5.0));
        resizables.add(new Rectangle(0, 0, 10.0, 5.0));
        resizables.add(new Circle(20, 20, 3.0));

        System.out.println("Doubling all shapes:");
        for (Resizable r : resizables) {
            r.doubleSize();   // Default method from Resizable interface
        }

        System.out.println("\nResetting all shapes to original:");
        for (Resizable r : resizables) {
            r.resetSize();    // Each shape has its own implementation
        }
    }

    // =========================================================================
    // Demo 4: Default Methods
    // =========================================================================
    static void demo4_DefaultMethods() {
        System.out.println("\n--- Demo 4: Default Methods ---");

        Circle c = new Circle(5, 5, 3.0, "blue");

        // Default method from Drawable
        c.info();  // "This is a Drawable object: Circle"

        // Custom toggle override
        System.out.println("Toggling visibility:");
        c.toggle();  // Hides
        c.draw();    // Won't draw
        c.toggle();  // Shows again
        c.draw();    // Draws again
    }

    // =========================================================================
    // Demo 5: Static Interface Method
    // =========================================================================
    static void demo5_StaticInterfaceMethod() {
        System.out.println("\n--- Demo 5: Static Interface Method ---");

        // Static methods are called on the interface, not on instances
        System.out.println("Drawable.isValidSize(5.0): " + Drawable.isValidSize(5.0));
        System.out.println("Drawable.isValidSize(-1.0): " + Drawable.isValidSize(-1.0));
        System.out.println("Drawable.isValidSize(0.0): " + Drawable.isValidSize(0.0));

        // Interface constant
        System.out.println("Default color: " + Drawable.DEFAULT_COLOR);

        // Using static method for validation before creation
        double userRadius = -5.0;
        if (Drawable.isValidSize(userRadius)) {
            new Circle(0, 0, userRadius);
        } else {
            System.out.println("Invalid radius " + userRadius + " — not creating circle");
        }
    }

    // =========================================================================
    // HELPER METHOD — shows the power of polymorphism
    // =========================================================================

    /**
     * Draws ALL drawables — works for any mix of Circles, Rectangles,
     * or any future Drawable we add. No changes needed to this method.
     */
    static void drawAll(List<Drawable> shapes) {
        for (Drawable shape : shapes) {
            shape.draw();  // Dynamic dispatch — calls the right draw()
        }
    }

    /**
     * Resizes ALL resizables by the same factor.
     */
    static void resizeAll(List<Resizable> shapes, double factor) {
        for (Resizable shape : shapes) {
            shape.resize(factor);  // Each shape resizes in its own way
        }
    }
}
