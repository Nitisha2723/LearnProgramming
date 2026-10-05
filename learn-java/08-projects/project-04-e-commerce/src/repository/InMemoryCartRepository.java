package repository;

import model.Cart;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of {@link CartRepository}.
 */
public class InMemoryCartRepository implements CartRepository {

    /** Keyed by cartId. */
    private final Map<String, Cart> store = new HashMap<>();

    @Override
    public Optional<Cart> findByCustomerId(String customerId) {
        if (customerId == null) return Optional.empty();
        return store.values().stream()
                .filter(c -> customerId.equals(c.getCustomerId()))
                .findFirst();
    }

    @Override
    public Optional<Cart> findById(String cartId) {
        return Optional.ofNullable(store.get(cartId));
    }

    @Override
    public Cart save(Cart cart) {
        if (store.containsKey(cart.getCartId())) {
            throw new IllegalArgumentException(
                    "Cart with ID " + cart.getCartId() + " already exists.");
        }
        store.put(cart.getCartId(), cart);
        return cart;
    }

    @Override
    public Cart update(Cart cart) {
        if (!store.containsKey(cart.getCartId())) {
            throw new IllegalArgumentException(
                    "No cart found with ID " + cart.getCartId());
        }
        store.put(cart.getCartId(), cart);
        return cart;
    }

    @Override
    public boolean delete(String cartId) {
        return store.remove(cartId) != null;
    }
}
