package solid.dip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 *  DIP CORRECT EXAMPLE — Dependency Inversion Principle
 * ============================================================
 *
 *  THE FIX:
 *    1. Introduce ABSTRACTIONS (interfaces) for each infrastructure concern:
 *         - NotificationService  (was: GmailEmailService)
 *         - OrderRepository      (was: MySQLOrderRepository)
 *         - PaymentProcessor     (was: StripePaymentProcessor)
 *
 *    2. OrderService depends on the ABSTRACTIONS, not the implementations.
 *       The field types are interfaces — OrderService doesn't know or care
 *       which concrete class is used at runtime.
 *
 *    3. Dependencies are INJECTED via the constructor (Constructor Injection).
 *       OrderService never calls "new ConcreteClass()". It receives its
 *       dependencies from the outside.
 *
 *  THE DEPENDENCY GRAPH (CORRECTED):
 *    BEFORE (violation):
 *      OrderService → GmailEmailService
 *      OrderService → MySQLOrderRepository
 *      OrderService → StripePaymentProcessor
 *      (High-level depends directly on low-level)
 *
 *    AFTER (correct):
 *      OrderService → NotificationService (interface)  ← EmailNotificationService
 *      OrderService → OrderRepository     (interface)  ← InMemoryOrderRepository
 *      OrderService → PaymentProcessor    (interface)  ← StripePaymentProcessor
 *                                                      ← PayPalPaymentProcessor
 *      (Both high-level and low-level depend on the abstraction in the middle)
 *
 *  ABOUT DEPENDENCY INJECTION FRAMEWORKS:
 *    The main() method below wires everything together manually — this is
 *    called "Poor Man's Dependency Injection" or "Manual DI".
 *
 *    In real applications, Spring Framework (or Guice, CDI, etc.) does this
 *    wiring AUTOMATICALLY based on annotations or configuration:
 *
 *    // Spring version (compare to manual wiring in main()):
 *    @Service
 *    class OrderService {
 *        @Autowired  // ← Spring injects the right NotificationService impl
 *        private NotificationService notificationService;
 *        @Autowired
 *        private OrderRepository repository;
 *        @Autowired
 *        private PaymentProcessor paymentProcessor;
 *    }
 *
 *    The DIP principle is WHY Spring's dependency injection works so cleanly.
 *    Spring can only swap implementations because the code depends on interfaces.
 */
public class CorrectExample {

    // =========================================================================
    //  ABSTRACTIONS — the stable interfaces that both sides depend on
    //
    //  These interfaces are the "inversion point". High-level (OrderService)
    //  and low-level (GmailEmailService, etc.) both point TO these interfaces.
    //  The dependency direction is INVERTED compared to ViolationExample.
    // =========================================================================

    /**
     * Abstraction for sending notifications to customers.
     *
     * OrderService depends on THIS — not on Gmail, SendGrid, or any specific
     * notification technology. The underlying technology is a detail.
     */
    interface NotificationService {
        /**
         * Sends a notification to a recipient.
         *
         * @param recipient  The destination (email, phone number, user ID, etc.)
         * @param subject    The subject/title of the notification.
         * @param message    The body/content of the notification.
         */
        void send(String recipient, String subject, String message);
    }

    /**
     * Abstraction for storing and retrieving orders.
     *
     * OrderService depends on THIS — not on MySQL, PostgreSQL, or any
     * specific storage technology.
     */
    interface OrderRepository {
        void save(Order order);
        Order findById(String orderId);
    }

    /**
     * Abstraction for processing payments.
     *
     * OrderService depends on THIS — not on Stripe, PayPal, or Braintree.
     */
    interface PaymentProcessor {
        /**
         * Attempts to charge the customer.
         *
         * @param customerId The customer's identifier in our system.
         * @param amount     The amount to charge (in USD for simplicity).
         * @return true if the payment succeeded, false if it failed/declined.
         */
        PaymentResult charge(String customerId, double amount);
    }

    // =========================================================================
    //  DATA OBJECT
    // =========================================================================

    /** Simple value object representing an order. */
    static class Order {
        final String orderId;
        final String customerId;
        final String product;
        final double price;
        String status; // "PENDING", "PAID", "FAILED"

        Order(String orderId, String customerId, String product, double price) {
            this.orderId    = orderId;
            this.customerId = customerId;
            this.product    = product;
            this.price      = price;
            this.status     = "PENDING";
        }

        @Override
        public String toString() {
            return "Order{id=" + orderId + ", customer=" + customerId
                    + ", product=" + product + ", price=" + price
                    + ", status=" + status + "}";
        }
    }

    /** Result of a payment attempt. */
    static class PaymentResult {
        final boolean success;
        final String transactionId;
        final String errorMessage;

        PaymentResult(boolean success, String transactionId, String errorMessage) {
            this.success       = success;
            this.transactionId = transactionId;
            this.errorMessage  = errorMessage;
        }

        static PaymentResult success(String txId) {
            return new PaymentResult(true, txId, null);
        }

        static PaymentResult failure(String reason) {
            return new PaymentResult(false, null, reason);
        }
    }

    // =========================================================================
    //  LOW-LEVEL IMPLEMENTATIONS — concrete classes implementing the interfaces
    //
    //  These depend on the abstractions (they implement the interfaces above).
    //  The dependency direction is: ConcreteClass → Interface ← OrderService
    //  This is the "inversion" — both sides point to the middle abstraction.
    // =========================================================================

    /** Email notification — one implementation of NotificationService. */
    static class EmailNotificationService implements NotificationService {

        private final String smtpHost;

        EmailNotificationService(String smtpHost) {
            this.smtpHost = smtpHost;
            System.out.println("[EmailNotificationService] Ready, SMTP host: " + smtpHost);
        }

        @Override
        public void send(String recipient, String subject, String message) {
            System.out.println("[EmailNotificationService] Email to: " + recipient);
            System.out.println("[EmailNotificationService] Subject: " + subject);
            System.out.println("[EmailNotificationService] Body: " + message);
        }
    }

    /** SMS notification — a DIFFERENT implementation of NotificationService. */
    static class SmsNotificationService implements NotificationService {

        private final String smsProvider;

        SmsNotificationService(String smsProvider) {
            this.smsProvider = smsProvider;
            System.out.println("[SmsNotificationService] Ready, provider: " + smsProvider);
        }

        @Override
        public void send(String recipient, String subject, String message) {
            // SMS doesn't have subjects, so we combine them
            System.out.println("[SmsNotificationService] SMS to: " + recipient
                    + " via " + smsProvider);
            System.out.println("[SmsNotificationService] Message: " + subject + " - " + message);
        }
    }

    /**
     * In-memory repository — perfect for unit tests and development.
     *
     * DIP BENEFIT: In tests, we inject InMemoryOrderRepository instead of
     * MySQLOrderRepository. No database setup needed. Tests run in milliseconds.
     */
    static class InMemoryOrderRepository implements OrderRepository {

        private final Map<String, Order> store = new HashMap<>();

        @Override
        public void save(Order order) {
            store.put(order.orderId, order);
            System.out.println("[InMemoryOrderRepository] Saved: " + order);
        }

        @Override
        public Order findById(String orderId) {
            Order order = store.get(orderId);
            System.out.println("[InMemoryOrderRepository] Found: " + order);
            return order;
        }
    }

    /**
     * MySQL repository — for production use.
     *
     * DIP BENEFIT: OrderService doesn't know this exists. To switch from
     * InMemoryOrderRepository to MySQLOrderRepository (or back), you change
     * ONE LINE in the wiring code — not in OrderService.
     */
    static class MySQLOrderRepository implements OrderRepository {

        MySQLOrderRepository() {
            System.out.println("[MySQLOrderRepository] Connected: jdbc:mysql://localhost:3306/orders");
        }

        @Override
        public void save(Order order) {
            System.out.println("[MySQLOrderRepository] INSERT INTO orders: " + order);
        }

        @Override
        public Order findById(String orderId) {
            System.out.println("[MySQLOrderRepository] SELECT * FROM orders WHERE id='" + orderId + "'");
            // Simulated result
            return new Order(orderId, "customer-42", "Widget", 49.99);
        }
    }

    /** Stripe payment processor — one implementation of PaymentProcessor. */
    static class StripePaymentProcessor implements PaymentProcessor {

        StripePaymentProcessor() {
            System.out.println("[StripePaymentProcessor] Stripe SDK initialized.");
        }

        @Override
        public PaymentResult charge(String customerId, double amount) {
            System.out.println("[StripePaymentProcessor] Charging $" + amount
                    + " for customer: " + customerId);
            System.out.println("[StripePaymentProcessor] POST /v1/charges — OK");
            return PaymentResult.success("stripe_txn_" + System.currentTimeMillis());
        }
    }

    /** PayPal payment processor — a DIFFERENT implementation of PaymentProcessor. */
    static class PayPalPaymentProcessor implements PaymentProcessor {

        PayPalPaymentProcessor() {
            System.out.println("[PayPalPaymentProcessor] PayPal SDK initialized.");
        }

        @Override
        public PaymentResult charge(String customerId, double amount) {
            System.out.println("[PayPalPaymentProcessor] Charging $" + amount
                    + " via PayPal for: " + customerId);
            System.out.println("[PayPalPaymentProcessor] POST /v2/checkout/orders — OK");
            return PaymentResult.success("paypal_txn_" + System.currentTimeMillis());
        }
    }

    /**
     * Test-mode payment processor — always succeeds (or always fails, configurable).
     *
     * DIP BENEFIT: In unit tests, inject this instead of Stripe/PayPal.
     * Tests run without internet access, without API keys, without billing.
     * This is only possible because OrderService depends on the interface.
     */
    static class TestPaymentProcessor implements PaymentProcessor {

        private final boolean alwaysSucceed;

        TestPaymentProcessor(boolean alwaysSucceed) {
            this.alwaysSucceed = alwaysSucceed;
        }

        @Override
        public PaymentResult charge(String customerId, double amount) {
            if (alwaysSucceed) {
                System.out.println("[TestPaymentProcessor] Simulated SUCCESSFUL charge of $" + amount);
                return PaymentResult.success("test_txn_success");
            } else {
                System.out.println("[TestPaymentProcessor] Simulated FAILED charge of $" + amount);
                return PaymentResult.failure("Card declined (test mode)");
            }
        }
    }

    // =========================================================================
    //  HIGH-LEVEL MODULE: OrderService — DIP COMPLIANT
    //
    //  Compare to ViolationExample.OrderService:
    //    BEFORE: Three "new ConcreteClass()" in the constructor.
    //    AFTER:  Three interface-typed fields, all injected from outside.
    //
    //  OrderService no longer knows:
    //    - WHICH email provider is used (Gmail? SendGrid? SMS?)
    //    - WHICH database is used (MySQL? PostgreSQL? In-memory?)
    //    - WHICH payment provider is used (Stripe? PayPal?)
    //
    //  This is the INVERSION: the details depend on the abstraction,
    //  not the business logic depending on the details.
    // =========================================================================

    static class OrderService {

        // -----------------------------------------------------------------------
        //  DIP COMPLIANT: Field types are INTERFACES, not concrete classes.
        //  OrderService has no idea what's on the other end of these interfaces.
        //  It just knows: "I have something that can notify, something that can
        //  store orders, and something that can process payments."
        // -----------------------------------------------------------------------
        private final NotificationService notificationService; // interface
        private final OrderRepository     orderRepository;     // interface
        private final PaymentProcessor    paymentProcessor;    // interface

        /**
         * Constructor Injection — dependencies are provided from OUTSIDE.
         *
         * DIP COMPLIANT:
         *   The caller (main method, Spring framework, test framework) decides
         *   WHICH implementation to inject. OrderService never calls "new".
         *
         *   This one change makes OrderService:
         *     1. Testable: inject TestPaymentProcessor, InMemoryOrderRepository.
         *     2. Flexible: swap Stripe for PayPal without touching this class.
         *     3. Single-responsibility: only contains business logic.
         *     4. Stable: rarely changes even when infrastructure evolves.
         */
        public OrderService(
                NotificationService notificationService,
                OrderRepository orderRepository,
                PaymentProcessor paymentProcessor) {

            // No "new" calls here — dependencies come in from the outside.
            this.notificationService = notificationService;
            this.orderRepository     = orderRepository;
            this.paymentProcessor    = paymentProcessor;

            System.out.println("[OrderService] Initialized (dependencies injected from outside).");
        }

        /**
         * Business logic: place an order.
         *
         * This method is now PURE BUSINESS LOGIC. It reads like a story:
         *   "Save the order. Try to charge the customer. If successful,
         *    notify them. Otherwise, mark the order as failed."
         *
         * There is ZERO infrastructure code here. No JDBC. No Gmail. No Stripe.
         * All those details are hidden behind the injected interfaces.
         */
        public void placeOrder(String orderId, String customerId, String product, double price) {
            System.out.println("\n[OrderService] Placing order: " + orderId);

            // Create and save the order
            Order order = new Order(orderId, customerId, product, price);
            orderRepository.save(order); // ← no idea if this is MySQL or memory

            // Process payment
            PaymentResult result = paymentProcessor.charge(customerId, price); // ← no idea if Stripe or PayPal

            if (result.success) {
                // Update order status
                order.status = "PAID";
                orderRepository.save(order);

                // Notify customer — no idea if this sends email or SMS
                notificationService.send(
                    customerId + "@example.com",
                    "Order Confirmed: " + orderId,
                    "Your order for " + product + " ($" + price + ") is confirmed."
                    + " Transaction: " + result.transactionId
                );

                System.out.println("[OrderService] SUCCESS: " + orderId);
            } else {
                order.status = "FAILED";
                orderRepository.save(order);

                notificationService.send(
                    customerId + "@example.com",
                    "Payment Failed: " + orderId,
                    "We couldn't process your payment. Reason: " + result.errorMessage
                );

                System.out.println("[OrderService] PAYMENT FAILED: " + orderId
                        + " — " + result.errorMessage);
            }
        }
    }

    // =========================================================================
    //  MAIN — Manual Dependency Injection ("Poor Man's DI")
    //
    //  This demonstrates how you wire everything together WITHOUT a framework.
    //  The only place "new ConcreteClass()" appears is HERE — in the
    //  composition root (the outermost layer of the application).
    //
    //  With Spring: this wiring is automatic. Spring reads the @Service,
    //  @Repository, @Component annotations and wires everything for you.
    //  You never write this wiring code manually in a Spring application.
    //
    //  Without Spring: you write it once in main() or a factory class.
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== DIP CORRECT EXAMPLE ===\n");

        // =====================================================================
        //  SCENARIO 1: Production setup
        //  Using real email, real DB (simulated), real Stripe payments.
        // =====================================================================
        System.out.println("=== Scenario 1: Production Configuration ===");
        System.out.println("(wiring: Email + MySQL + Stripe)");

        // ALL "new ConcreteClass()" calls happen HERE — the composition root.
        // OrderService never sees any of these class names.
        NotificationService emailNotifier    = new EmailNotificationService("smtp.gmail.com");
        OrderRepository     mysqlRepo        = new MySQLOrderRepository();
        PaymentProcessor    stripeProcessor  = new StripePaymentProcessor();

        // Inject the implementations into OrderService via constructor
        OrderService productionService = new OrderService(
                emailNotifier,     // ← could swap for SmsNotificationService
                mysqlRepo,         // ← could swap for InMemoryOrderRepository
                stripeProcessor    // ← could swap for PayPalPaymentProcessor
        );

        productionService.placeOrder("ORD-001", "customer-42", "Laptop", 999.99);

        // =====================================================================
        //  SCENARIO 2: Testing — swap ALL implementations with test doubles
        //  OrderService code is IDENTICAL. Only the wiring changes.
        // =====================================================================
        System.out.println("\n=== Scenario 2: Test Configuration ===");
        System.out.println("(wiring: InMemory + InMemory + TestProcessor-success)");

        // In a unit test, we inject:
        //   - InMemoryOrderRepository: no real DB needed
        //   - TestPaymentProcessor(true): simulates successful payments
        //   - A simple print-based notifier (could be a mock library like Mockito)
        NotificationService testNotifier    = new SmsNotificationService("TestSMS");
        OrderRepository     memoryRepo      = new InMemoryOrderRepository();
        PaymentProcessor    testSuccessProc = new TestPaymentProcessor(true);

        OrderService testService = new OrderService(
                testNotifier,
                memoryRepo,
                testSuccessProc
        );

        testService.placeOrder("ORD-002", "tester-99", "Widget", 49.99);

        // =====================================================================
        //  SCENARIO 3: Test for PAYMENT FAILURE path
        //  Simulate a declined card — impossible without DIP
        // =====================================================================
        System.out.println("\n=== Scenario 3: Test Payment Failure ===");
        System.out.println("(wiring: InMemory + InMemory + TestProcessor-failure)");

        // DIP BENEFIT: We can test the "payment failed" branch easily!
        // Without DIP, you'd need a real Stripe test card that declines.
        PaymentProcessor testFailProc = new TestPaymentProcessor(false);

        OrderService failTestService = new OrderService(
                new SmsNotificationService("TestSMS"), // fresh instance
                new InMemoryOrderRepository(),
                testFailProc
        );

        failTestService.placeOrder("ORD-003", "tester-99", "Expensive Item", 9999.99);

        // =====================================================================
        //  SCENARIO 4: Switch from Stripe to PayPal
        //  OrderService.java is NOT modified — only the wiring changes here.
        // =====================================================================
        System.out.println("\n=== Scenario 4: Swap Stripe for PayPal ===");
        System.out.println("(OrderService.java is UNCHANGED — only wiring differs)");

        OrderService paypalService = new OrderService(
                new EmailNotificationService("smtp.company.com"),
                new InMemoryOrderRepository(),
                new PayPalPaymentProcessor() // ← swapped Stripe for PayPal, one line!
        );

        paypalService.placeOrder("ORD-004", "customer-77", "Tablet", 499.99);

        // =====================================================================
        //  SUMMARY
        // =====================================================================
        System.out.println("\n=== DIP TAKEAWAY ===");
        System.out.println("+ OrderService never calls 'new ConcreteClass()' internally.");
        System.out.println("+ All 'new' calls are in the composition root (main/factory).");
        System.out.println("+ Swapping Stripe for PayPal: change ONE line in wiring code.");
        System.out.println("+ Swapping MySQL for InMemory (for tests): change ONE line.");
        System.out.println("+ OrderService is testable: inject test doubles, no real infra needed.");
        System.out.println("+ In Spring: @Autowired replaces all the manual wiring above.");
        System.out.println("+ DIP is WHY Spring's dependency injection is so powerful.");
    }
}
