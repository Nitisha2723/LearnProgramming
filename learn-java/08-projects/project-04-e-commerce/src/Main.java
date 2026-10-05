import factory.PaymentFactory;
import model.*;
import model.Payment.PaymentMethod;
import observer.EmailNotificationListener;
import observer.InventoryUpdateListener;
import repository.*;
import service.*;
import strategy.*;

/**
 * End-to-end demo of the e-commerce system.
 *
 * <p>Run from the {@code src/} directory:</p>
 * <pre>
 *   javac model/*.java repository/*.java strategy/*.java observer/*.java \
 *         builder/*.java factory/*.java service/*.java Main.java
 *   java Main
 * </pre>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("     E-Commerce System Demo");
        System.out.println("========================================\n");

        // ---- Wire up infrastructure ----
        InMemoryProductRepository productRepo = new InMemoryProductRepository();
        InMemoryOrderRepository   orderRepo   = new InMemoryOrderRepository();
        InMemoryCartRepository    cartRepo    = new InMemoryCartRepository();

        // ---- Wire up services ----
        ProductService productService = new ProductService(productRepo);
        CartService    cartService    = new CartService(cartRepo, productRepo);
        OrderService   orderService   = new OrderService(orderRepo, cartRepo);
        PaymentService paymentService = new PaymentService(new PaymentFactory());

        // ---- Register observers ----
        orderService.registerListener(new EmailNotificationListener());
        orderService.registerListener(new InventoryUpdateListener(productRepo));

        // ---- Create products ----
        System.out.println("--- Products ---");
        Product laptop  = productService.addProduct("Laptop Pro 15",
                "High-performance laptop", 1299.99, 10, "electronics");
        Product mouse   = productService.addProduct("Wireless Mouse",
                "Ergonomic wireless mouse", 49.99, 50, "electronics");
        Product keyboard = productService.addProduct("Mechanical Keyboard",
                "Tactile mechanical keyboard", 129.99, 25, "electronics");

        System.out.println("Added: " + laptop.getName() + " ($" + laptop.getPrice() + ", stock=" + laptop.getStockQuantity() + ")");
        System.out.println("Added: " + mouse.getName()  + " ($" + mouse.getPrice()  + ", stock=" + mouse.getStockQuantity() + ")");
        System.out.println("Added: " + keyboard.getName() + " ($" + keyboard.getPrice() + ", stock=" + keyboard.getStockQuantity() + ")");

        // ---- Create customer ----
        Customer alice = new Customer("Alice", "Johnson",
                "alice@example.com", "123 Main St, Springfield");
        System.out.println("\n--- Customer ---");
        System.out.println("Customer: " + alice.getFullName() + " | " + alice.getShippingAddress());

        // ---- Build cart ----
        System.out.println("\n--- Shopping Cart ---");
        cartService.addToCart(alice.getCustomerId(), laptop.getProductId(), 1);
        cartService.addToCart(alice.getCustomerId(), mouse.getProductId(),  2);
        cartService.addToCart(alice.getCustomerId(), keyboard.getProductId(), 1);

        double cartTotal = cartService.getCartTotal(alice.getCustomerId());
        System.out.printf("Cart total (before discount): $%.2f%n", cartTotal);

        // ---- Place order with tiered discount ----
        System.out.println("\n--- Placing Order (Tiered Discount) ---");
        DiscountStrategy discount = new TieredDiscountStrategy();
        System.out.println("Discount strategy: " + discount.getDiscountName());

        Order order = orderService.placeOrder(alice.getCustomerId(), discount);

        System.out.printf("%nOrder placed!%n");
        System.out.printf("  Order ID  : %s%n", order.getOrderId());
        System.out.printf("  Subtotal  : $%.2f%n", order.getSubtotal());
        System.out.printf("  Discount  : $%.2f%n", order.getDiscountAmount());
        System.out.printf("  Total     : $%.2f%n", order.getTotal());
        System.out.printf("  Status    : %s%n", order.getStatus());

        // ---- Process payment ----
        System.out.println("\n--- Payment ---");
        Payment payment = paymentService.processPayment(
                order.getOrderId(), order.getTotal(), PaymentMethod.CREDIT_CARD);
        order.setPaymentId(payment.getPaymentId());
        System.out.printf("Payment ID : %s%n", payment.getPaymentId());
        System.out.printf("Status     : %s%n", payment.getStatus());
        System.out.printf("Reference  : %s%n", payment.getTransactionReference());

        // ---- Advance order through lifecycle ----
        System.out.println("\n--- Order Lifecycle ---");
        orderService.confirmOrder(order.getOrderId());
        System.out.println("Status after confirm : " + order.getStatus());

        orderService.shipOrder(order.getOrderId());
        System.out.println("Status after ship    : " + order.getStatus());

        orderService.deliverOrder(order.getOrderId());
        System.out.println("Status after deliver : " + order.getStatus());

        // ---- Demonstrate cancellation with a new order ----
        System.out.println("\n--- Cancellation Demo ---");
        cartService.addToCart(alice.getCustomerId(), mouse.getProductId(), 1);
        Order order2 = orderService.placeOrder(alice.getCustomerId(),
                new FixedAmountDiscountStrategy(10.00));
        System.out.println("New order placed: $" + order2.getTotal());
        orderService.cancelOrder(order2.getOrderId());
        System.out.println("Order2 status: " + order2.getStatus());

        // ---- Inventory check ----
        System.out.println("\n--- Inventory After Orders ---");
        System.out.printf("Laptop  stock: %d%n", productRepo.findById(laptop.getProductId()).get().getStockQuantity());
        System.out.printf("Mouse   stock: %d%n", productRepo.findById(mouse.getProductId()).get().getStockQuantity());
        System.out.printf("Keyboard stock: %d%n", productRepo.findById(keyboard.getProductId()).get().getStockQuantity());

        // ---- Search demo ----
        System.out.println("\n--- Product Search ---");
        productService.searchProducts("wireless").forEach(p ->
                System.out.println("Found: " + p.getName()));
        productService.getProductsByPriceRange(40, 150).forEach(p ->
                System.out.printf("In range $40-$150: %s ($%.2f)%n", p.getName(), p.getPrice()));

        System.out.println("\n=== Demo Complete ===");
    }
}
