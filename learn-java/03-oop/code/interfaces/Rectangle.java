import java.util.Objects;

/**
 * Rectangle — also implements both Drawable and Resizable.
 *
 * This shows that two completely different shapes (Circle, Rectangle)
 * can share the SAME interface and be used interchangeably as Drawable
 * or Resizable objects — the essence of polymorphism with interfaces.
 */
public class Rectangle implements Drawable, Resizable {

    private double x;       // Top-left corner X
    private double y;       // Top-left corner Y
    private double width;
    private double height;
    private String color;
    private boolean visible;
    private final double originalWidth;
    private final double originalHeight;

    public Rectangle(double x, double y, double width, double height) {
        this(x, y, width, height, Drawable.DEFAULT_COLOR);
    }

    public Rectangle(double x, double y, double width, double height, String color) {
        if (!Drawable.isValidSize(width) || !Drawable.isValidSize(height)) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.visible = true;
        this.originalWidth = width;
        this.originalHeight = height;
    }

    // =========================================================================
    // Drawable interface implementation
    // =========================================================================

    @Override
    public void draw() {
        if (visible) {
            System.out.printf("Drawing %s Rectangle at (%.1f, %.1f) [%.1f x %.1f]%n",
                             color, x, y, width, height);
        } else {
            System.out.println("Rectangle is hidden — nothing to draw.");
        }
    }

    @Override
    public void hide() {
        visible = false;
        System.out.println("Rectangle hidden.");
    }

    @Override
    public void toggle() {
        visible = !visible;
        System.out.println("Rectangle " + (visible ? "shown" : "hidden") + ".");
    }

    // =========================================================================
    // Resizable interface implementation
    // =========================================================================

    @Override
    public void resize(double factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Resize factor must be positive");
        }
        width *= factor;
        height *= factor;
        System.out.printf("Rectangle resized to %.1f x %.1f%n", width, height);
    }

    @Override
    public void resetSize() {
        this.width = originalWidth;
        this.height = originalHeight;
        System.out.printf("Rectangle reset to original %.1f x %.1f%n", width, height);
    }

    // =========================================================================
    // Rectangle-specific methods
    // =========================================================================

    public double getArea() {
        return width * height;
    }

    public double getPerimeter() {
        return 2 * (width + height);
    }

    public boolean isSquare() {
        return Math.abs(width - height) < 0.0001;  // Floating-point safe comparison
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= x + width &&
               py >= y && py <= y + height;
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public String getColor() { return color; }
    public boolean isVisible() { return visible; }

    @Override
    public String toString() {
        return String.format("Rectangle{pos=(%.1f,%.1f), size=%.1fx%.1f, color=%s, area=%.2f}",
                            x, y, width, height, color, getArea());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Rectangle)) return false;
        Rectangle other = (Rectangle) obj;
        return Double.compare(x, other.x) == 0 &&
               Double.compare(y, other.y) == 0 &&
               Double.compare(width, other.width) == 0 &&
               Double.compare(height, other.height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }
}
