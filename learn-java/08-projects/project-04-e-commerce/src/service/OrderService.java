package service;

import builder.OrderBuilder;
import model.Cart;
import model.CartItem;
import model.Order;
import model.Order.OrderStatus;
import model.OrderItem;
import observer.OrderEventListener;
import repository.CartRepository;
import repository.OrderRepository;
import strategy.DiscountStrategy;
import strategy.NoDiscountStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Business logic for order placement and lifecycle management.
 *
 * <p>Registered {@link OrderEventListener}s are notified on every significant
 * state change (placed, confirmed, shipped, cancelled).</p>
 */
public class OrderService {

    private final OrderRepository   orderRepository;
    private final CartRepository    cartRepository;
    private final List<OrderEventListener> listeners = new ArrayList<>();

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository  = cartRepository;
    }

    // -------------------------------------------------------------------------
    // Observer registration
    // -------------------------------------------------------------------------

    public void registerListener(OrderEventListener listener) {
        if (listener != null) listeners.add(listener);
    }

    // -------------------------------------------------------------------------
    // Order placement
    // -------------------------------------------------------------------------

    /**
     * Converts the customer's current cart into a confirmed order.
     *
     * <p>Steps:</p>
     * <ol>
     *   <li>Fetch the cart — throw if empty</li>
     *   <li>Convert CartItems → OrderItems (price snapshot)</li>
     *   <li>Build the Order using {@link OrderBuilder} + the supplied discount</li>
     *   <li>Persist the order</li>
     *   <li>Clear the cart</li>
     *   <li>Notify all listeners</li>
     * </ol>
     *
     * @param customerId       the customer placing the order
     * @param discountStrategy discount to apply (null → no discount)
     * @return the newly created order
     * @throws IllegalStateException if the cart is empty or does not exist
     */
    public Order placeOrder(String customerId, DiscountStrategy discountStrategy) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalStateException(
                        "No cart found for customer: " + customerId));

        if (cart.isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }

        // Snapshot cart items as immutable OrderItems
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(ci -> new OrderItem(ci.getProductId(), ci.getProductName(),
                        ci.getUnitPrice(), ci.getQuantity()))
                .collect(Collectors.toList());

        // Determine shipping address (fallback placeholder — real system would have customer address)
        String shippingAddress = "Customer shipping address for " + customerId;

        // Build the order
        DiscountStrategy strategy = (discountStrategy != null)
                ? discountStrategy : new NoDiscountStrategy();

        Order order = new OrderBuilder()
                .withCustomerId(customerId)
                .withItems(orderItems)
                .withShippingAddress(shippingAddress)
                .withDiscountStrategy(strategy)
                .build();

        orderRepository.save(order);

        // Clear the cart
        cart.clear();
        cartRepository.update(cart);

        // Notify observers
        notifyOrderPlaced(order);

        return order;
    }

    // -------------------------------------------------------------------------
    // Lifecycle transitions
    // -------------------------------------------------------------------------

    public Order confirmOrder(String orderId) {
        Order order = getOrder(orderId);
        order.updateStatus(OrderStatus.CONFIRMED);
        orderRepository.update(order);
        notifyOrderConfirmed(order);
        return order;
    }

    public Order shipOrder(String orderId) {
        Order order = getOrder(orderId);
        order.updateStatus(OrderStatus.SHIPPED);
        orderRepository.update(order);
        notifyOrderShipped(order);
        return order;
    }

    public Order deliverOrder(String orderId) {
        Order order = getOrder(orderId);
        order.updateStatus(OrderStatus.DELIVERED);
        return orderRepository.update(order);
    }

    public Order cancelOrder(String orderId) {
        Order order = getOrder(orderId);
        order.cancel(); // throws if already shipped/delivered
        orderRepository.update(order);
        notifyOrderCancelled(order);
        return order;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Order not found: " + orderId));
    }

    public List<Order> getCustomerOrders(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // -------------------------------------------------------------------------
    // Event notification helpers (package-visible for test access)
    // -------------------------------------------------------------------------

    void notifyOrderPlaced(Order order) {
        listeners.forEach(l -> l.onOrderPlaced(order));
    }

    void notifyOrderConfirmed(Order order) {
        listeners.forEach(l -> l.onOrderConfirmed(order));
    }

    void notifyOrderShipped(Order order) {
        listeners.forEach(l -> l.onOrderShipped(order));
    }

    void notifyOrderCancelled(Order order) {
        listeners.forEach(l -> l.onOrderCancelled(order));
    }
}
