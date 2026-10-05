package service;

import model.Cart;
import model.CartItem;
import model.Product;
import repository.CartRepository;
import repository.ProductRepository;

import java.util.NoSuchElementException;

/**
 * Business logic for shopping cart operations.
 */
public class CartService {

    private final CartRepository    cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository    = cartRepository;
        this.productRepository = productRepository;
    }

    // -------------------------------------------------------------------------
    // Cart management
    // -------------------------------------------------------------------------

    /**
     * Returns the customer's cart, creating one if none exists.
     */
    public Cart getOrCreateCart(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(customerId);
                    return cartRepository.save(newCart);
                });
    }

    public Cart getCart(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No cart found for customer: " + customerId));
    }

    /**
     * Adds a product to the customer's cart.
     *
     * <p>Validates that the product exists, is active, and has sufficient stock.</p>
     *
     * @throws NoSuchElementException if the product does not exist
     * @throws IllegalStateException  if the product is unavailable or out of stock
     */
    public Cart addToCart(String customerId, String productId, int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive.");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Product not found: " + productId));

        if (!product.isAvailable()) {
            throw new IllegalStateException(
                    "Product '" + product.getName() + "' is not available.");
        }
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock for '" + product.getName()
                            + "': requested=" + quantity
                            + ", available=" + product.getStockQuantity());
        }

        Cart cart = getOrCreateCart(customerId);
        CartItem item = new CartItem(product.getProductId(), product.getName(),
                product.getPrice(), quantity);
        cart.addItem(item);
        return cartRepository.update(cart);
    }

    /**
     * Removes a product from the customer's cart.
     *
     * @return the updated cart
     */
    public Cart removeFromCart(String customerId, String productId) {
        Cart cart = getCart(customerId);
        cart.removeItem(productId);
        return cartRepository.update(cart);
    }

    /**
     * Changes the quantity of an item already in the cart.
     *
     * @throws NoSuchElementException if the item is not in the cart
     */
    public Cart updateQuantity(String customerId, String productId, int qty) {
        Cart cart = getCart(customerId);
        cart.updateItemQuantity(productId, qty);
        return cartRepository.update(cart);
    }

    /**
     * Empties the cart without deleting the cart record.
     */
    public Cart clearCart(String customerId) {
        Cart cart = getOrCreateCart(customerId);
        cart.clear();
        return cartRepository.update(cart);
    }

    /**
     * Returns the current cart total for the customer.
     *
     * @return total, or 0.0 if no cart exists
     */
    public double getCartTotal(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .map(Cart::getTotal)
                .orElse(0.0);
    }
}
