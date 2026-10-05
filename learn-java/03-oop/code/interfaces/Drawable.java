/**
 * Drawable — interface defining drawing capability.
 *
 * An interface is a PURE CONTRACT: it says "anything that implements me
 * can be drawn and hidden." It doesn't say HOW — that's up to the implementer.
 *
 * Think of it as: Drawable = "I promise I can draw and hide myself"
 */
public interface Drawable {

    /**
     * Draws this object to the screen.
     * Abstract by default — every implementer must provide this.
     */
    void draw();

    /**
     * Hides this object.
     */
    void hide();

    /**
     * Default method (Java 8+) — has a body, inherited by all implementers.
     *
     * Why default methods exist: allows adding new methods to interfaces
     * without breaking all existing implementations.
     *
     * Implementers CAN override this, but don't have to.
     */
    default void toggle() {
        System.out.println("Toggling visibility.");
        // Note: we can't call draw/hide directly here in a meaningful way
        // without knowing the state. This is a simple demo.
    }

    /**
     * Another default method — show some info about this drawable.
     */
    default void info() {
        System.out.println("This is a Drawable object: " + getClass().getSimpleName());
    }

    /**
     * Static utility method — belongs to the interface, not instances.
     * Called as: Drawable.isValidSize(5.0)
     */
    static boolean isValidSize(double size) {
        return size > 0;
    }

    // Constants in interfaces are implicitly: public static final
    String DEFAULT_COLOR = "black";  // Every Drawable has a default color
}
