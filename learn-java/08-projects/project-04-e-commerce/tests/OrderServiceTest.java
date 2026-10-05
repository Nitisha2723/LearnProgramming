import model.Cart;
import model.Order;
import model.Order.OrderStatus;
import model.Product;
import observer.OrderEventListener;
import org.junit.jupiter.api.*;
import repository.InMemoryCartRepository;
import repository.InMemoryOrderRepository;
import repository.InMemoryProductRepository;
import service.CartService;
import service.OrderService;
import strategy.NoDiscountStrategy;
import strategy.PercentageDiscountStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OrderService}.
 *
 * Uses an in-memory observer to verify listener invocations without Mockito.
 */
@DisplayName("OrderService Tests")
class OrderServiceTest {

    // ---- Infrastructure ----
    private InMemoryProductRepository productRepo;
    private InMemoryCartRepository    cartRepo;
    private InMemoryOrderRepository   orderRepo;

    // ---- Services under test ----
    private CartService  cartService;
    private OrderService orderService;

    // ---- Test observer (records calls) ----
    private TestListener testListener;

    private static final String CUSTOMER_ID = "customer-001";
    private Product laptop;

    @BeforeEach
    void setUp() {
        productRepo  = new InMemoryProductRepository();
        cartRepo     = new InMemoryCartRepository();
        orderRepo    = new InMemoryOrderRepository();
        cartService  = new CartService(cartRepo, productRepo);
        orderService = new OrderService(orderRepo, cartRepo);
        testListener = new TestListener();
        orderService.registerListener(testListener);

        laptop = new Product("Laptop", "A laptop", 500.00, 10, "electronics");
        productRepo.save(laptop);
    }

    // -------------------------------------------------------------------------
    // placeOrder
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("placeOrder: creates order in PENDING status with correct total")
    void placeOrder_success() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 2);
        Order order = orderService.placeOrder(CUSTOMER_ID, new NoDiscountStrategy());

        assertNotNull(order.getOrderId());
        assertEquals(CUSTOMER_ID,       order.getCustomerId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(1000.00,           order.getSubtotal(), 0.001);
        assertEquals(0.00,              order.getDiscountAmount(), 0.001);
        assertEquals(1000.00,           order.getTotal(), 0.001);
    }

    @Test
    @DisplayName("placeOrder: clears the cart after placing the order")
    void placeOrder_clearsCart() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        orderService.placeOrder(CUSTOMER_ID, new NoDiscountStrategy());

        // Cart should be empty but still exist
        Cart cart = cartService.getOrCreateCart(CUSTOMER_ID);
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("placeOrder_emptyCart: throws IllegalStateException")
    void placeOrder_emptyCart_throws() {
        // Ensure cart exists but is empty
        cartService.getOrCreateCart(CUSTOMER_ID);

        assertThrows(IllegalStateException.class, () ->
                orderService.placeOrder(CUSTOMER_ID, new NoDiscountStrategy()));
    }

    @Test
    @DisplayName("placeOrder_noCart: throws IllegalStateException")
    void placeOrder_noCart_throws() {
        assertThrows(IllegalStateException.class, () ->
                orderService.placeOrder("no-cart-customer", new NoDiscountStrategy()));
    }

    // -------------------------------------------------------------------------
    // Discount applied correctly
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("discountApplied: 10% discount reduces total correctly")
    void discountApplied() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1); // 500.00
        Order order = orderService.placeOrder(CUSTOMER_ID, new PercentageDiscountStrategy(10));

        assertEquals(500.00, order.getSubtotal(), 0.001);
        assertEquals(50.00,  order.getDiscountAmount(), 0.001);
        assertEquals(450.00, order.getTotal(), 0.001);
    }

    // -------------------------------------------------------------------------
    // cancelOrder
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("cancelOrder: sets status to CANCELLED")
    void cancelOrder_success() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        Order order = orderService.placeOrder(CUSTOMER_ID, new NoDiscountStrategy());

        Order cancelled = orderService.cancelOrder(order.getOrderId());
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("cancelOrder: throws when order is already SHIPPED")
    void cancelOrder_alreadyShipped_throws() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        Order order = orderService.placeOrder(CUSTOMER_ID, new NoDiscountStrategy());
        orderService.confirmOrder(order.getOrderId());
        orderService.shipOrder(order.getOrderId());

        assertThrows(IllegalStateException.class, () ->
                orderService.cancelOrder(order.getOrderId()));
    }

    // -------------------------------------------------------------------------
    // Listener notifications (Observer pattern)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listener_notifiedOnOrderPlaced: onOrderPlaced called exactly once")
    void listener_notifiedOnOrderPlaced() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        orderService.placeOrder(CUSTOMER_ID, new NoDiscountStrategy());

        assertEquals(1, testListener.placedEvents.size());
        assertTrue(testListener.confirmedEvents.isEmpty());
    }

    @Test
    @DisplayName("listener_notifiedOnOrderConfirmed: onOrderConfirmed called after confirm")
    void listener_notifiedOnOrderConfirmed() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        Order order = orderService.placeOrder(CUSTOMER_ID, new NoDiscountStrategy());
        orderService.confirmOrder(order.getOrderId());

        assertEquals(1, testListener.confirmedEvents.size());
    }

    @Test
    @DisplayName("listener_notifiedOnOrderCancelled: onOrderCancelled called after cancel")
    void listener_notifiedOnOrderCancelled() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        Order order = orderService.placeOrder(CUSTOMER_ID, new NoDiscountStrategy());
        orderService.cancelOrder(order.getOrderId());

        assertEquals(1, testListener.cancelledEvents.size());
    }

    // -------------------------------------------------------------------------
    // getCustomerOrders
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getCustomerOrders: returns all orders for the customer")
    void getCustomerOrders_returnsAll() {
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        orderService.placeOrder(CUSTOMER_ID, new NoDiscountStrategy());
        cartService.addToCart(CUSTOMER_ID, laptop.getProductId(), 1);
        orderService.placeOrder(CUSTOMER_ID, new NoDiscountStrategy());

        List<Order> orders = orderService.getCustomerOrders(CUSTOMER_ID);
        assertEquals(2, orders.size());
    }

    // -------------------------------------------------------------------------
    // getOrder — not found
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getOrder: throws NoSuchElementException for unknown order ID")
    void getOrder_notFound_throws() {
        assertThrows(NoSuchElementException.class, () ->
                orderService.getOrder("bad-order-id"));
    }

    // -------------------------------------------------------------------------
    // Inner test observer
    // -------------------------------------------------------------------------

    static class TestListener implements OrderEventListener {
        final List<Order> placedEvents    = new ArrayList<>();
        final List<Order> confirmedEvents = new ArrayList<>();
        final List<Order> shippedEvents   = new ArrayList<>();
        final List<Order> cancelledEvents = new ArrayList<>();

        @Override public void onOrderPlaced(Order o)    { placedEvents.add(o); }
        @Override public void onOrderConfirmed(Order o) { confirmedEvents.add(o); }
        @Override public void onOrderShipped(Order o)   { shippedEvents.add(o); }
        @Override public void onOrderCancelled(Order o) { cancelledEvents.add(o); }
    }
}
