import java.util.Objects;

/**
 * Circle — implements BOTH Drawable and Resizable.
 *
 * This class demonstrates:
 * 1. Implementing multiple interfaces with 'implements A, B'
 * 2. Providing concrete implementations for all interface methods
 * 3. Overriding default methods when needed
 * 4. Having its own fields and methods beyond what the interfaces require
 */
public class Circle implements Drawable, Resizable {

    private double x;        // Center X coordinate
    private double y;        // Center Y coordinate
    private double radius;   // Radius
    private String color;    // Fill color
    private boolean visible; // Visibility state
    private final double originalRadius;  // Track original for resetSize()

    /**
     * Constructor.
     */
    public Circle(double x, double y, double radius) {
        this(x, y, radius, Drawable.DEFAULT_COLOR);
    }

    public Circle(double x, double y, double radius, String color) {
        if (!Drawable.isValidSize(radius)) {  // Using the interface's static method
            throw new IllegalArgumentException("Radius must be positive");
        }
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
        this.visible = true;
        this.originalRadius = radius;
    }

    // =========================================================================
    // Drawable interface implementation
    // =========================================================================

    @Override
    public void draw() {
        if (visible) {
            System.out.printf("Drawing %s Circle at (%.1f, %.1f) with radius %.2f%n",
                             color, x, y, radius);
        } else {
            System.out.println("Circle is hidden — nothing to draw.");
        }
    }

    @Override
    public void hide() {
        visible = false;
        System.out.println("Circle hidden.");
    }

    /**
     * Override the default toggle() to actually switch visibility state.
     * The default implementation is a no-op; we do better here.
     */
    @Override
    public void toggle() {
        if (visible) {
            hide();
        } else {
            visible = true;
            System.out.println("Circle shown.");
        }
    }

    // =========================================================================
    // Resizable interface implementation
    // =========================================================================

    @Override
    public void resize(double factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Resize factor must be positive");
        }
        radius *= factor;
        System.out.printf("Circle radius changed from %.2f to %.2f%n",
                         radius / factor, radius);
    }

    /**
     * Override resetSize to actually restore the original radius.
     * The default in Resizable is a no-op — we can do better here.
     */
    @Override
    public void resetSize() {
        double oldRadius = radius;
        radius = originalRadius;
        System.out.printf("Circle reset from radius %.2f to original %.2f%n",
                         oldRadius, radius);
    }

    // =========================================================================
    // Circle-specific methods
    // =========================================================================

    public double getArea() {
        return Math.PI * radius * radius;
    }

    public double getCircumference() {
        return 2 * Math.PI * radius;
    }

    public boolean contains(double px, double py) {
        double dx = px - x;
        double dy = py - y;
        return Math.sqrt(dx * dx + dy * dy) <= radius;
    }

    // Getters and setters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public String getColor() { return color; }
    public boolean isVisible() { return visible; }

    public void moveTo(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return String.format("Circle{center=(%.1f,%.1f), radius=%.2f, color=%s, area=%.2f}",
                            x, y, radius, color, getArea());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Circle)) return false;
        Circle other = (Circle) obj;
        return Double.compare(x, other.x) == 0 &&
               Double.compare(y, other.y) == 0 &&
               Double.compare(radius, other.radius) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, radius);
    }
}
