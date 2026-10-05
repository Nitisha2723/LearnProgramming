package model;

import java.util.Objects;

/**
 * An immutable snapshot of a product at the time an order was placed.
 *
 * <p>Because orders must preserve historical prices, {@code OrderItem} is immutable
 * — all fields are {@code final}.</p>
 */
public final class OrderItem {

    private final String productId;
    private final String productName;
    private final double unitPrice;
    private final int    quantity;
    private final double subtotal;

    public OrderItem(String productId, String productName, double unitPrice, int quantity) {
        if (unitPrice < 0)  throw new IllegalArgumentException("Unit price cannot be negative.");
        if (quantity  <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        this.productId   = productId;
        this.productName = productName;
        this.unitPrice   = unitPrice;
        this.quantity    = quantity;
        this.subtotal    = unitPrice * quantity;
    }

    public String getProductId()   { return productId; }
    public String getProductName() { return productName; }
    public double getUnitPrice()   { return unitPrice; }
    public int    getQuantity()    { return quantity; }
    public double getSubtotal()    { return subtotal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem item = (OrderItem) o;
        return Objects.equals(productId, item.productId)
                && quantity == item.quantity
                && Double.compare(unitPrice, item.unitPrice) == 0;
    }

    @Override
    public int hashCode() { return Objects.hash(productId, unitPrice, quantity); }

    @Override
    public String toString() {
        return "OrderItem{product='" + productName + "', qty=" + quantity
                + ", unitPrice=" + unitPrice + ", subtotal=" + subtotal + "}";
    }
}
