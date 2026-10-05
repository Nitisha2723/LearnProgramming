package observer;

import model.Order;
import model.OrderItem;
import repository.ProductRepository;

/**
 * Observer that keeps product stock in sync with order events.
 *
 * <ul>
 *   <li>{@link #onOrderPlaced} — reduces stock for each ordered item</li>
 *   <li>{@link #onOrderCancelled} — restores stock for each item</li>
 * </ul>
 *
 * <p>The other events (confirmed, shipped) do not affect inventory in this model.</p>
 */
public class InventoryUpdateListener implements OrderEventListener {

    private final ProductRepository productRepository;

    public InventoryUpdateListener(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void onOrderPlaced(Order order) {
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId()).ifPresentOrElse(
                    product -> {
                        product.reduceStock(item.getQuantity());
                        productRepository.update(product);
                        System.out.printf("[INVENTORY] Reduced stock of '%s' by %d (remaining: %d)%n",
                                product.getName(), item.getQuantity(), product.getStockQuantity());
                    },
                    () -> System.err.printf("[INVENTORY] Product not found: %s%n",
                            item.getProductId()));
        }
    }

    @Override
    public void onOrderConfirmed(Order order) {
        // Stock was already reserved when the order was placed — nothing to do here.
    }

    @Override
    public void onOrderShipped(Order order) {
        // Stock was already reduced at placement — nothing to do here.
    }

    @Override
    public void onOrderCancelled(Order order) {
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId()).ifPresentOrElse(
                    product -> {
                        product.increaseStock(item.getQuantity());
                        productRepository.update(product);
                        System.out.printf("[INVENTORY] Restored stock of '%s' by %d (now: %d)%n",
                                product.getName(), item.getQuantity(), product.getStockQuantity());
                    },
                    () -> System.err.printf("[INVENTORY] Product not found: %s%n",
                            item.getProductId()));
        }
    }
}
