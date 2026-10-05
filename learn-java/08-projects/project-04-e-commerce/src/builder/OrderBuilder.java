package builder;

import model.Order;
import model.OrderItem;
import strategy.DiscountStrategy;
import strategy.NoDiscountStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link Order} objects.
 *
 * <p>Enforces that all required fields are set before construction, calculates
 * subtotals and totals, and applies the chosen {@link DiscountStrategy}.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * Order order = new OrderBuilder()
 *     .withCustomerId(customerId)
 *     .withItems(cartItems)
 *     .withShippingAddress("123 Main St")
 *     .withDiscountStrategy(new PercentageDiscountStrategy(10))
 *     .build();
 * }</pre>
 */
public class OrderBuilder {

    // -------------------------------------------------------------------------
    // Builder state
    // -------------------------------------------------------------------------

    private String customerId;
    private List<OrderItem> items = new ArrayList<>();
    private String shippingAddress;
    private DiscountStrategy discountStrategy = new NoDiscountStrategy();
    private String paymentId;

    // -------------------------------------------------------------------------
    // Fluent setters
    // -------------------------------------------------------------------------

    public OrderBuilder withCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public OrderBuilder withItems(List<OrderItem> items) {
        this.items = new ArrayList<>(items);
        return this;
    }

    public OrderBuilder withShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
        return this;
    }

    public OrderBuilder withDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy != null
                ? discountStrategy
                : new NoDiscountStrategy();
        return this;
    }

    public OrderBuilder withPaymentId(String paymentId) {
        this.paymentId = paymentId;
        return this;
    }

    // -------------------------------------------------------------------------
    // Build
    // -------------------------------------------------------------------------

    /**
     * Validates the builder state, calculates totals, and constructs an {@link Order}.
     *
     * @throws IllegalStateException if required fields are missing or items list is empty
     */
    public Order build() {
        // Validation
        if (customerId == null || customerId.isBlank())
            throw new IllegalStateException("Order requires a customerId.");
        if (items == null || items.isEmpty())
            throw new IllegalStateException("Order must contain at least one item.");
        if (shippingAddress == null || shippingAddress.isBlank())
            throw new IllegalStateException("Order requires a shippingAddress.");

        // Financial calculation
        double subtotal       = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        double discountAmount = discountStrategy.calculateDiscount(subtotal);
        double total          = Math.max(0, subtotal - discountAmount);

        Order order = new Order(customerId, new ArrayList<>(items),
                subtotal, discountAmount, total, shippingAddress);

        if (paymentId != null) {
            order.setPaymentId(paymentId);
        }

        return order;
    }

    /**
     * Resets all builder state so the instance can be reused to build another order.
     */
    public void reset() {
        customerId       = null;
        items            = new ArrayList<>();
        shippingAddress  = null;
        discountStrategy = new NoDiscountStrategy();
        paymentId        = null;
    }
}
