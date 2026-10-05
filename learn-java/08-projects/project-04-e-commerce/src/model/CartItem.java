package model;

/**
 * A mutable line item inside a shopping cart.
 */
public class CartItem {

    private final String productId;
    private final String productName;
    private double unitPrice;
    private int    quantity;

    public CartItem(String productId, String productName, double unitPrice, int quantity) {
        if (quantity <= 0)  throw new IllegalArgumentException("Quantity must be positive.");
        if (unitPrice < 0)  throw new IllegalArgumentException("Unit price cannot be negative.");
        this.productId   = productId;
        this.productName = productName;
        this.unitPrice   = unitPrice;
        this.quantity    = quantity;
    }

    /** Returns unitPrice * quantity. */
    public double getSubtotal() {
        return unitPrice * quantity;
    }

    /**
     * Replaces the quantity with {@code qty}.
     *
     * @throws IllegalArgumentException if qty ≤ 0
     */
    public void updateQuantity(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        this.quantity = qty;
    }

    // Getters
    public String getProductId()   { return productId; }
    public String getProductName() { return productName; }
    public double getUnitPrice()   { return unitPrice; }
    public int    getQuantity()    { return quantity; }

    @Override
    public String toString() {
        return "CartItem{product='" + productName + "', qty=" + quantity
                + ", unitPrice=" + unitPrice + ", subtotal=" + getSubtotal() + "}";
    }
}
