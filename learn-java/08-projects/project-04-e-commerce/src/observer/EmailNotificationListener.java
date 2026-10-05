package observer;

import model.Order;

/**
 * Observer that simulates sending email notifications for order events.
 *
 * <p>In a real system this would use an SMTP client or a messaging queue.
 * Here it writes simulated emails to stdout so the behaviour is visible
 * during demo and testing.</p>
 */
public class EmailNotificationListener implements OrderEventListener {

    @Override
    public void onOrderPlaced(Order order) {
        System.out.printf("[EMAIL] Order confirmation sent for order #%s%n"
                + "        Customer: %s | Total: $%.2f%n",
                order.getOrderId(), order.getCustomerId(), order.getTotal());
    }

    @Override
    public void onOrderConfirmed(Order order) {
        System.out.printf("[EMAIL] Payment received — order #%s is being prepared.%n",
                order.getOrderId());
    }

    @Override
    public void onOrderShipped(Order order) {
        System.out.printf("[EMAIL] Your order #%s has shipped!%n"
                + "        Shipping to: %s%n",
                order.getOrderId(), order.getShippingAddress());
    }

    @Override
    public void onOrderCancelled(Order order) {
        System.out.printf("[EMAIL] We're sorry — order #%s has been cancelled.%n"
                + "        A refund of $%.2f will be processed shortly.%n",
                order.getOrderId(), order.getTotal());
    }
}
