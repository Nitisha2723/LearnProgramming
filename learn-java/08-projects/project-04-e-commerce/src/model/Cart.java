package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A shopping cart belonging to a customer.
 *
 * <p>Mutable by design — items are added, removed, and updated during a session.</p>
 */
public class Cart {

    private final String cartId;
    private final String customerId;
    private final List<CartItem> items;
    private final LocalDateTime createdAt;

    public Cart(String customerId) {
        this.cartId     = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.items      = new ArrayList<>();
        this.createdAt  = LocalDateTime.now();
    }

    /** Reconstruction constructor. */
    public Cart(String cartId, String customerId, List<CartItem> items,
                LocalDateTime createdAt) {
        this.cartId     = cartId;
        this.customerId = customerId;
        this.items      = new ArrayList<>(items);
        this.createdAt  = createdAt;
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Adds an item to the cart.
     * If the product already exists the quantities are summed.
     */
    public void addItem(CartItem item) {
        findItemByProductId(item.getProductId()).ifPresentOrElse(
                existing -> existing.updateQuantity(existing.getQuantity() + item.getQuantity()),
                () -> items.add(item));
    }

    /**
     * Removes the item with the given productId.
     *
     * @return {@code true} if an item was removed
     */
    public boolean removeItem(String productId) {
        return items.removeIf(i -> i.getProductId().equals(productId));
    }

    /**
     * Sets the quantity of the item with the given productId.
     *
     * @throws java.util.NoSuchElementException if no such item exists
     */
    public void updateItemQuantity(String productId, int qty) {
        findItemByProductId(productId)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Item not found in cart: " + productId))
                .updateQuantity(qty);
    }

    /** Returns the sum of all item subtotals. */
    public double getTotal() {
        return items.stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    /** Returns total number of units across all items. */
    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    /** Returns {@code true} if there are no items in the cart. */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Removes all items from the cart. */
    public void clear() {
        items.clear();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getCartId()           { return cartId; }
    public String getCustomerId()       { return customerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** Returns an unmodifiable view of the items list. */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Optional<CartItem> findItemByProductId(String productId) {
        return items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cart)) return false;
        return Objects.equals(cartId, ((Cart) o).cartId);
    }

    @Override
    public int hashCode() { return Objects.hash(cartId); }

    @Override
    public String toString() {
        return "Cart{id='" + cartId + "', customer='" + customerId
                + "', items=" + items.size() + ", total=" + getTotal() + "}";
    }
}
