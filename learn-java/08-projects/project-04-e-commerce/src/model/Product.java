package model;

import java.util.Objects;
import java.util.UUID;

/**
 * A product available for purchase in the e-commerce store.
 */
public class Product {

    private final String productId;
    private String name;
    private String description;
    private double price;
    private int    stockQuantity;
    private String categoryId;
    private boolean active;

    public Product(String name, String description, double price,
                   int stockQuantity, String categoryId) {
        if (price < 0)         throw new IllegalArgumentException("Price cannot be negative.");
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock cannot be negative.");

        this.productId     = UUID.randomUUID().toString();
        this.name          = name;
        this.description   = description;
        this.price         = price;
        this.stockQuantity = stockQuantity;
        this.categoryId    = categoryId;
        this.active        = true;
    }

    /** Reconstruction constructor. */
    public Product(String productId, String name, String description, double price,
                   int stockQuantity, String categoryId, boolean active) {
        this.productId     = productId;
        this.name          = name;
        this.description   = description;
        this.price         = price;
        this.stockQuantity = stockQuantity;
        this.categoryId    = categoryId;
        this.active        = active;
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /** Returns {@code true} if active and at least 1 unit in stock. */
    public boolean isAvailable() {
        return active && stockQuantity > 0;
    }

    /**
     * Reduces stock by {@code qty}.
     *
     * @throws IllegalArgumentException if qty ≤ 0
     * @throws IllegalStateException    if insufficient stock
     */
    public void reduceStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be positive.");
        if (qty > stockQuantity)
            throw new IllegalStateException(
                    "Insufficient stock for product '" + name
                            + "': requested=" + qty + ", available=" + stockQuantity);
        stockQuantity -= qty;
    }

    /**
     * Restores stock by {@code qty} (e.g., after a cancelled order).
     *
     * @throws IllegalArgumentException if qty ≤ 0
     */
    public void increaseStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be positive.");
        stockQuantity += qty;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getProductId()                    { return productId; }
    public String getName()                         { return name; }
    public String getDescription()                  { return description; }
    public double getPrice()                        { return price; }
    public int    getStockQuantity()                { return stockQuantity; }
    public String getCategoryId()                   { return categoryId; }
    public boolean isActive()                       { return active; }

    public void setName(String name)                { this.name = name; }
    public void setDescription(String description)  { this.description = description; }
    public void setPrice(double price)              {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }
    public void setCategoryId(String categoryId)    { this.categoryId = categoryId; }
    public void setActive(boolean active)           { this.active = active; }
    public void setStockQuantity(int qty)           {
        if (qty < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.stockQuantity = qty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        return Objects.equals(productId, ((Product) o).productId);
    }

    @Override
    public int hashCode() { return Objects.hash(productId); }

    @Override
    public String toString() {
        return "Product{id='" + productId + "', name='" + name
                + "', price=" + price + ", stock=" + stockQuantity + "}";
    }
}
