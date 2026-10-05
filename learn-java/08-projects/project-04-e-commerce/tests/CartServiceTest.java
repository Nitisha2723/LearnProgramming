import model.Cart;
import model.Product;
import org.junit.jupiter.api.*;
import repository.InMemoryCartRepository;
import repository.InMemoryProductRepository;
import service.CartService;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CartService}.
 */
@DisplayName("CartService Tests")
class CartServiceTest {

    private InMemoryProductRepository productRepo;
    private CartService               cartService;
    private Product                   laptop;
    private Product                   mouse;
    private final String              CUSTOMER_ID = "customer-001";

    @BeforeEach
    void setUp() {
        productRepo = new InMemoryProductRepository();
        cartService = new CartService(new InMemoryCartRepository(), productRepo);

        laptop = new Product("Laptop", "A laptop", 999.99, 5, "electronics");
        mouse  = new Product("Mouse",  "A mouse",   29.99, 0, "electronics"); // out of stock
        productRepo.save(laptop);
        productRepo.save(mouse);
    }

    // -------------------------------------------------------------------------
    // addToCart
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addToCart: creates cart and adds item correctly")
    void addToCart_success() {
        Cart cart = cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 2);

        assertFalse(cart.isEmpty());
        assertEquals(1,  cart.getItems().size());
        assertEquals(2,  cart.getItems().get(0).getQuantity());
        assertEquals(999.99 * 2, cart.getTotal(), 0.001);
    }

    @Test
    @DisplayName("addToCart: adding same product again accumulates quantity")
    void addToCart_sameProductTwice_accumulates() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        Cart cart = cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 2);

        assertEquals(1, cart.getItems().size());           // still one line
        assertEquals(3, cart.getItems().get(0).getQuantity()); // qty merged
    }

    @Test
    @DisplayName("addToCart: throws when product does not exist")
    void addToCart_unknownProduct_throws() {
        assertThrows(NoSuchElementException.class, () ->
                cartService.addToCart(CUSTOMER_ID, "bad-id", 1));
    }

    @Test
    @DisplayName("addToCart: throws when product is out of stock")
    void addToCart_outOfStock_throws() {
        // mouse has stockQuantity=0
        assertThrows(IllegalStateException.class, () ->
                cartService.addToCart(CUSTOMER_ID, mouse.getProductId(), 1));
    }

    @Test
    @DisplayName("addToCart: throws when requested quantity exceeds stock")
    void addToCart_exceedsStock_throws() {
        // laptop has stock=5
        assertThrows(IllegalStateException.class, () ->
                cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 10));
    }

    @Test
    @DisplayName("addToCart: throws for non-positive quantity")
    void addToCart_nonPositiveQty_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 0));
    }

    // -------------------------------------------------------------------------
    // removeFromCart
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeFromCart: removes the item successfully")
    void removeFromCart_success() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        Cart cart = cartService.removeFromCart(CUSTOMER_ID, laptop.getProductId());

        assertTrue(cart.isEmpty());
        assertEquals(0.0, cart.getTotal(), 0.001);
    }

    @Test
    @DisplayName("removeFromCart: no-op when item not in cart")
    void removeFromCart_itemNotPresent_noOp() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        Cart cart = cartService.removeFromCart(CUSTOMER_ID, "other-product-id");

        assertEquals(1, cart.getItems().size()); // laptop still there
    }

    // -------------------------------------------------------------------------
    // updateQuantity
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateQuantity: changes the quantity of an existing item")
    void updateQuantity_success() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        Cart cart = cartService.updateQuantity(CUSTOMER_ID, laptop.getProductId(), 3);

        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    // -------------------------------------------------------------------------
    // clearCart
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("clearCart: empties the cart")
    void clearCart_success() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 2);
        Cart cart = cartService.clearCart(CUSTOMER_ID);

        assertTrue(cart.isEmpty());
        assertEquals(0.0, cart.getTotal(), 0.001);
    }

    // -------------------------------------------------------------------------
    // getCartTotal
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getCartTotal: returns correct sum of all items")
    void getCartTotal_correctSum() {
        // Add a second in-stock product
        Product keyboard = new Product("Keyboard", "Keys", 79.99, 10, "electronics");
        productRepo.save(keyboard);

        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(),   1); // 999.99
        cartService.addToCart(CUSTOMER_ID, keyboard.getProductId(), 2); // 159.98

        double total = cartService.getCartTotal(CUSTOMER_ID);
        assertEquals(999.99 + 2 * 79.99, total, 0.001);
    }

    @Test
    @DisplayName("getCartTotal: returns 0.0 when no cart exists")
    void getCartTotal_noCart_returnsZero() {
        assertEquals(0.0, cartService.getCartTotal("unknown-customer"), 0.001);
    }

    // -------------------------------------------------------------------------
    // getOrCreateCart
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getOrCreateCart: creates a new cart on first call, reuses on second")
    void getOrCreateCart_idempotent() {
        Cart c1 = cartService.getOrCreateCart(CUSTOMER_ID);
        Cart c2 = cartService.getOrCreateCart(CUSTOMER_ID);

        assertEquals(c1.getCartId(), c2.getCartId());
    }
}
