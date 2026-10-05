package repository;

import model.Order;
import model.Order.OrderStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link OrderRepository}.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new HashMap<>();

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        if (customerId == null) return new ArrayList<>();
        return store.values().stream()
                .filter(o -> customerId.equals(o.getCustomerId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        if (status == null) return new ArrayList<>();
        return store.values().stream()
                .filter(o -> status == o.getStatus())
                .collect(Collectors.toList());
    }

    @Override
    public Order save(Order order) {
        if (store.containsKey(order.getOrderId())) {
            throw new IllegalArgumentException(
                    "Order with ID " + order.getOrderId() + " already exists.");
        }
        store.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Order update(Order order) {
        if (!store.containsKey(order.getOrderId())) {
            throw new IllegalArgumentException(
                    "No order found with ID " + order.getOrderId());
        }
        store.put(order.getOrderId(), order);
        return order;
    }
}
