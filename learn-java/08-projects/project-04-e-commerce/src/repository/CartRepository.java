package repository;

import model.Cart;

import java.util.Optional;

/**
 * Data-access contract for {@link Cart} entities.
 */
public interface CartRepository {

    Optional<Cart> findByCustomerId(String customerId);

    Optional<Cart> findById(String cartId);

    Cart save(Cart cart);

    Cart update(Cart cart);

    boolean delete(String cartId);
}
