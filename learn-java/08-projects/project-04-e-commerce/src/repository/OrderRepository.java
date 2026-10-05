package repository;

import model.Order;
import model.Order.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Order} entities.
 */
public interface OrderRepository {

    List<Order> findAll();

    Optional<Order> findById(String orderId);

    /** Returns all orders placed by the given customer. */
    List<Order> findByCustomerId(String customerId);

    /** Returns all orders in the given status. */
    List<Order> findByStatus(OrderStatus status);

    Order save(Order order);

    Order update(Order order);
}
