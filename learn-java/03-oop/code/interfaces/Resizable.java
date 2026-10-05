/**
 * Resizable — interface defining resize capability.
 *
 * Notice: a class can implement both Drawable AND Resizable.
 * This is how Java achieves multiple inheritance of behavior.
 *
 * Analogy: A Circle is both Drawable (you can draw it) AND
 * Resizable (you can change its size). These are independent capabilities.
 */
public interface Resizable {

    /**
     * Resizes this object by the given factor.
     *
     * @param factor scale factor: 2.0 = double size, 0.5 = half size
     *               Must be > 0
     */
    void resize(double factor);

    /**
     * Default method: doubles the size.
     * Implemented in terms of resize() — available to all implementers.
     */
    default void doubleSize() {
        System.out.println("Doubling size...");
        resize(2.0);
    }

    /**
     * Default method: halves the size.
     */
    default void halfSize() {
        System.out.println("Halving size...");
        resize(0.5);
    }

    /**
     * Default method: reset to original size.
     * This is abstract-in-disguise — subclasses should override if they
     * track original size.
     */
    default void resetSize() {
        System.out.println("Reset to original size (no-op by default).");
    }
}
