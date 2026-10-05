package observer;

import model.Order;

/**
 * Observer interface for order lifecycle events.
 *
 * <p>Implement this interface to react to order state changes without
 * coupling notification logic to {@link service.OrderService}.</p>
 */
public interface OrderEventListener {

    /**
     * Called when a new order is placed (status: PENDING).
     *
     * @param order the newly placed order
     */
    void onOrderPlaced(Order order);

    /**
     * Called when an order is confirmed (status: CONFIRMED).
     *
     * @param order the confirmed order
     */
    void onOrderConfirmed(Order order);

    /**
     * Called when an order has shipped (status: SHIPPED).
     *
     * @param order the shipped order
     */
    void onOrderShipped(Order order);

    /**
     * Called when an order is cancelled (status: CANCELLED).
     *
     * @param order the cancelled order
     */
    void onOrderCancelled(Order order);
}
