package solid.dip;

/**
 * ============================================================
 *  SOLID PRINCIPLE #5 — Dependency Inversion Principle (DIP)
 * ============================================================
 *
 *  DEFINITION (two-part):
 *    A. "High-level modules should not depend on low-level modules.
 *        Both should depend on abstractions."
 *    B. "Abstractions should not depend on details.
 *        Details should depend on abstractions."
 *    — Robert C. Martin (Uncle Bob)
 *
 *  WHAT ARE HIGH-LEVEL AND LOW-LEVEL MODULES?
 *    - High-level module: Contains the important BUSINESS LOGIC.
 *      Example: OrderService — orchestrates placing an order.
 *    - Low-level module: Contains technical INFRASTRUCTURE details.
 *      Example: GmailEmailService, MySQLOrderRepository, StripePaymentProcessor.
 *
 *    The business logic (high-level) should not know or care HOW emails
 *    are sent, HOW data is stored, or HOW payments are processed. These
 *    are infrastructure concerns that can change independently.
 *
 *  THE VIOLATION BELOW:
 *    OrderService directly instantiates (using "new") the concrete infrastructure
 *    classes. This means:
 *      1. OrderService cannot be tested without a real Gmail account, MySQL
 *         database, and Stripe API key.
 *      2. Switching from MySQL to PostgreSQL requires modifying OrderService
 *         (even though OrderService is supposed to be "business logic only").
 *      3. Switching from Stripe to PayPal requires modifying OrderService.
 *      4. OrderService and its dependencies must always be deployed together.
 *      5. The dependency graph points the WRONG way:
 *         High-level → Low-level (direct, concrete dependency)
 *         This should be: High-level → Abstraction ← Low-level
 */
public class ViolationExample {

    // =========================================================================
    //  LOW-LEVEL CONCRETE CLASSES
    //
    //  These represent infrastructure/technical details.
    //  They should be "plug-in" implementations, not things the
    //  business logic directly depends on.
    // =========================================================================

    /**
     * Concrete email service using Gmail's SMTP server.
     *
     * DIP VIOLATION (this class):
     *   This is a DETAIL — it's one specific way to send email.
     *   OrderService should not know this class exists.
     *   Tomorrow we might switch to SendGrid, SES, or Postmark.
     */
    static class GmailEmailService {

        public GmailEmailService() {
            // In a real class, this constructor would read Gmail credentials
            // from config, set up the JavaMail Session, etc.
            System.out.println("[GmailEmailService] Initializing Gmail SMTP connection...");
            System.out.println("[GmailEmailService] Host: smtp.gmail.com, Port: 587");
        }

        public void sendEmail(String to, String subject, String body) {
            System.out.println("[GmailEmailService] Sending via Gmail to: " + to);
            System.out.println("[GmailEmailService] Subject: " + subject);
            System.out.println("[GmailEmailService] Body: " + body);
        }
    }

    /**
     * Concrete repository using MySQL.
     *
     * DIP VIOLATION (this class):
     *   This is a DETAIL — one specific way to persist orders.
     *   OrderService should not know this class exists.
     *   The DBA might want to migrate to PostgreSQL, or tests might use
     *   an in-memory store. With direct instantiation, that's impossible.
     */
    static class MySQLOrderRepository {

        public MySQLOrderRepository() {
            // Real constructor: load JDBC driver, open connection pool
            System.out.println("[MySQLOrderRepository] Opening connection: jdbc:mysql://localhost:3306/orders");
        }

        public void save(String orderId, String product, double price) {
            System.out.println("[MySQLOrderRepository] INSERT INTO orders VALUES ('"
                    + orderId + "', '" + product + "', " + price + ")");
        }

        public String findById(String orderId) {
            System.out.println("[MySQLOrderRepository] SELECT * FROM orders WHERE id='" + orderId + "'");
            return "Order{id=" + orderId + ", product='Widget', price=99.99}"; // fake result
        }
    }

    /**
     * Concrete payment processor using Stripe.
     *
     * DIP VIOLATION (this class):
     *   This is a DETAIL — one specific payment provider.
     *   OrderService directly creates this, making it impossible to:
     *     - Test without a real Stripe API key.
     *     - Switch to PayPal or Braintree without modifying OrderService.
     *     - Use a test-mode payment processor in development.
     */
    static class StripePaymentProcessor {

        public StripePaymentProcessor() {
            // Real constructor: read Stripe API key from environment,
            // initialize Stripe SDK, set up webhook endpoints
            System.out.println("[StripePaymentProcessor] Initializing Stripe SDK...");
            System.out.println("[StripePaymentProcessor] Loading API key from env: STRIPE_API_KEY");
        }

        public boolean processPayment(String customerId, double amount) {
            System.out.println("[StripePaymentProcessor] Charging customer " + customerId
                    + " via Stripe: $" + amount);
            System.out.println("[StripePaymentProcessor] Calling Stripe API: POST /v1/charges");
            return true; // simulated success
        }
    }

    // =========================================================================
    //  HIGH-LEVEL MODULE: OrderService — THE DIP VIOLATION
    //
    //  OrderService is supposed to contain BUSINESS LOGIC:
    //    "When an order is placed: save it, charge the customer, send confirmation."
    //
    //  But instead of depending on abstractions, it directly instantiates
    //  three concrete infrastructure classes in its constructor.
    //
    //  PROBLEMS this causes:
    //
    //  1. UNTESTABLE:
    //     To write a unit test for placeOrder(), you need:
    //       - A running MySQL database with the orders table.
    //       - A valid Stripe test API key.
    //       - A Gmail account with SMTP access.
    //     Unit tests should be fast and isolated. This is the opposite.
    //
    //  2. RIGID:
    //     Want to use PostgreSQL instead of MySQL? Edit OrderService.
    //     Want to use PayPal instead of Stripe? Edit OrderService.
    //     Want to use SendGrid instead of Gmail? Edit OrderService.
    //     Every infrastructure change forces a business-logic class to change.
    //     This violates both DIP AND SRP.
    //
    //  3. WRONG DEPENDENCY DIRECTION:
    //     The dependency graph is: OrderService → {Gmail, MySQL, Stripe}
    //     The correct direction is: OrderService → Abstractions ← {Gmail, MySQL, Stripe}
    //
    //  4. COUPLED DEPLOYMENT:
    //     OrderService and all its infrastructure must be deployed as one unit.
    //     You can never replace just the email service without touching the
    //     OrderService source code.
    // =========================================================================

    static class OrderService {

        // -----------------------------------------------------------------------
        //  DIP VIOLATION: High-level module directly depends on three
        //  concrete low-level classes by naming their types here.
        //
        //  The field types are concrete classes, not interfaces/abstractions.
        //  This locks OrderService to these exact implementations forever.
        // -----------------------------------------------------------------------
        private final GmailEmailService   emailService;      // ← concrete type (violation)
        private final MySQLOrderRepository repository;        // ← concrete type (violation)
        private final StripePaymentProcessor paymentProcessor; // ← concrete type (violation)

        /**
         * DIP VIOLATION: OrderService creates its own dependencies.
         *
         * The "new" keyword here is the root of the problem. OrderService
         * is responsible for CONSTRUCTING its dependencies as well as using them.
         * This means it's impossible to swap implementations from the outside.
         *
         * "new" is glue code — whenever you see "new ConcreteClass()" inside
         * a high-level business service, ask yourself: should I be depending on
         * an abstraction instead?
         */
        public OrderService() {
            // ← DIP VIOLATION: Creating concrete infrastructure classes directly.
            // This makes OrderService impossible to test in isolation.
            this.emailService      = new GmailEmailService();
            this.repository        = new MySQLOrderRepository();
            this.paymentProcessor  = new StripePaymentProcessor();
            System.out.println("[OrderService] Initialized with concrete dependencies.");
        }

        /**
         * Business logic: place an order.
         *
         * The logic itself is correct: save → charge → notify.
         * But it's trapped in a class that's tightly coupled to three
         * specific infrastructure implementations.
         *
         * If we want to unit-test this method with a mock payment processor
         * (to simulate "what happens when payment fails?"), we CAN'T —
         * because we cannot replace StripePaymentProcessor with a mock.
         * The "new StripePaymentProcessor()" in the constructor is hardwired.
         */
        public void placeOrder(String orderId, String customerId, String product, double price) {
            System.out.println("\n[OrderService] Placing order: " + orderId);

            // Step 1: Save to DB (coupled to MySQL)
            repository.save(orderId, product, price);

            // Step 2: Process payment (coupled to Stripe)
            boolean paid = paymentProcessor.processPayment(customerId, price);

            if (paid) {
                // Step 3: Send confirmation (coupled to Gmail)
                emailService.sendEmail(
                    customerId + "@example.com",
                    "Order Confirmation - " + orderId,
                    "Thank you for ordering " + product + " ($" + price + ")."
                );
                System.out.println("[OrderService] Order placed successfully: " + orderId);
            } else {
                System.out.println("[OrderService] Payment failed for order: " + orderId);
            }
        }
    }

    // =========================================================================
    //  MAIN — Demonstrating the DIP violation
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== DIP VIOLATION DEMO ===\n");
        System.out.println("NOTICE: Creating OrderService triggers Gmail, MySQL,");
        System.out.println("and Stripe initialization. You CANNOT create an OrderService");
        System.out.println("without ALL THREE infrastructure classes being present.\n");

        // This ONE line triggers initialisation of GMAIL + MYSQL + STRIPE.
        // In tests, this would fail immediately if those services aren't running.
        OrderService orderService = new OrderService();
        System.out.println();

        // Place an order — the business logic is buried under infrastructure coupling
        orderService.placeOrder("ORD-001", "customer-42", "Laptop", 999.99);

        System.out.println();
        System.out.println("PROBLEM SUMMARY:");
        System.out.println("  - Can't unit-test OrderService.placeOrder() without MySQL + Gmail + Stripe");
        System.out.println("  - Can't switch payment from Stripe to PayPal without editing OrderService");
        System.out.println("  - Can't switch DB from MySQL to PostgreSQL without editing OrderService");
        System.out.println("  - Can't switch email from Gmail to SendGrid without editing OrderService");
        System.out.println("  - OrderService has 4 reasons to change (its own logic + 3 infra changes)");
        System.out.println("\nSee CorrectExample.java for the DIP-compliant solution.");
    }
}
