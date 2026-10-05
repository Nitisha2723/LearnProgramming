package model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a placed customer order.
 */
public class Order {

    // -------------------------------------------------------------------------
    // Inner enum
    // -------------------------------------------------------------------------

    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final double subtotal;
    private final double discountAmount;
    private final double total;
    private String shippingAddress;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String paymentId;

    /**
     * Package-visible constructor; use {@link builder.OrderBuilder} to create orders.
     */
    public Order(String customerId, List<OrderItem> items, double subtotal,
                 double discountAmount, double total, String shippingAddress) {
        this.orderId        = UUID.randomUUID().toString();
        this.customerId     = customerId;
        this.items          = Collections.unmodifiableList(items);
        this.subtotal       = subtotal;
        this.discountAmount = discountAmount;
        this.total          = total;
        this.shippingAddress = shippingAddress;
        this.status         = OrderStatus.PENDING;
        this.createdAt      = LocalDateTime.now();
        this.updatedAt      = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /** Returns the total number of units across all order items. */
    public int getItemCount() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    /**
     * Cancels the order. Only orders in PENDING or CONFIRMED state may be cancelled.
     *
     * @throws IllegalStateException if the current status doesn't allow cancellation
     */
    public void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Cannot cancel order with status: " + status);
        }
        updateStatus(OrderStatus.CANCELLED);
    }

    /**
     * Advances the order to the given status, recording the update timestamp.
     *
     * @throws IllegalArgumentException if {@code newStatus} is {@code null}
     */
    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("Status cannot be null.");
        this.status    = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getOrderId()              { return orderId; }
    public String getCustomerId()           { return customerId; }
    public List<OrderItem> getItems()       { return items; }
    public double getSubtotal()             { return subtotal; }
    public double getDiscountAmount()       { return discountAmount; }
    public double getTotal()                { return total; }
    public String getShippingAddress()      { return shippingAddress; }
    public OrderStatus getStatus()          { return status; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }
    public String getPaymentId()            { return paymentId; }

    public void setShippingAddress(String address) { this.shippingAddress = address; }
    public void setPaymentId(String paymentId)     { this.paymentId = paymentId; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        return Objects.equals(orderId, ((Order) o).orderId);
    }

    @Override
    public int hashCode() { return Objects.hash(orderId); }

    @Override
    public String toString() {
        return String.format("Order{id='%s', customer='%s', total=%.2f, status=%s}",
                orderId, customerId, total, status);
    }
}
