/**
 * SOLUTION — Exercise 01: Refactor to SOLID Principles
 *
 * This file shows a clean refactoring of the spaghetti OrderManagementSystem.
 * Each SOLID fix is labelled so you can match it to the violations in the exercise.
 *
 * SOLID FIXES APPLIED
 * ===================
 * SRP  — Each class now has exactly ONE reason to change.
 *         OrderService is a thin orchestrator; all other concerns live in
 *         dedicated classes (OrderRepository, EmailNotifier, InvoiceService, etc.).
 *
 * OCP  — DiscountStrategy is an interface; adding a new customer type means
 *         writing a new class, not touching existing code.
 *         DiscountStrategyRegistry selects the right strategy without an if-chain.
 *
 * LSP  — PremiumOrder is gone. All Order objects honour the same getTotal()
 *         contract (raw sum only). Discounts are calculated separately via strategy.
 *
 * ISP  — The bloated OrderHandler interface is split into four small,
 *         cohesive interfaces that each describe one kind of capability.
 *
 * DIP  — OrderService depends on abstractions (interfaces), never on concrete
 *         classes. Dependencies are supplied via constructor injection, so tests
 *         can provide mocks without a framework.
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// ============================================================
//  DATA MODEL  (unchanged from exercise — just the pure data)
// ============================================================

class Sol1_OrderItem {
    private final String productId;
    private final String productName;
    private final double price;
    private final int    quantity;

    public Sol1_OrderItem(String productId, String productName, double price, int quantity) {
        this.productId   = productId;
        this.productName = productName;
        this.price       = price;
        this.quantity    = quantity;
    }
    public String getProductId()   { return productId; }
    public String getProductName() { return productName; }
    public double getPrice()       { return price; }
    public int    getQuantity()    { return quantity; }
}

/**
 * LSP FIX: Order is now a plain, final data object.
 * getTotal() always returns the raw item sum — no subclass can break that.
 * (The exercise's PremiumOrder subclass is deleted.)
 */
class Sol1_Order {
    private final String orderId;
    private final String customerId;
    private final String customerType;
    private final List<Sol1_OrderItem> items;
    private final String paymentMethod;
    private final LocalDateTime orderDate;
    private String status;

    public Sol1_Order(String orderId, String customerId, String customerType,
                      List<Sol1_OrderItem> items, String paymentMethod) {
        this.orderId      = orderId;
        this.customerId   = customerId;
        this.customerType = customerType;
        this.items        = Collections.unmodifiableList(new ArrayList<>(items));
        this.paymentMethod = paymentMethod;
        this.orderDate    = LocalDateTime.now();
        this.status       = "PENDING";
    }

    /** LSP: contract is stable — always returns the raw item sum. */
    public double getTotal() {
        return items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    }

    public String getOrderId()        { return orderId; }
    public String getCustomerId()     { return customerId; }
    public String getCustomerType()   { return customerType; }
    public List<Sol1_OrderItem> getItems() { return items; }
    public String getPaymentMethod()  { return paymentMethod; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public String getStatus()         { return status; }
    public void   setStatus(String s) { this.status = s; }
}

// ============================================================
//  ISP FIX: Four focused interfaces instead of one fat interface
// ============================================================

/**
 * ISP — only persistence operations.
 * Implementors don't need to know about emails, PDFs, or reports.
 */
interface Sol1_OrderRepository {
    void save(Sol1_Order order);
    Optional<Sol1_Order> findById(String orderId);
    List<Sol1_Order> findAll();
}

/**
 * ISP — only notification delivery.
 */
interface Sol1_NotificationService {
    void sendOrderConfirmation(Sol1_Order order);
}

/**
 * ISP — only document generation.
 */
interface Sol1_DocumentService {
    void generateInvoice(Sol1_Order order, double discountAmount);
}

/**
 * ISP — only payment processing.
 */
interface Sol1_PaymentService {
    boolean processPayment(Sol1_Order order, double amount);
    void processRefund(Sol1_Order order, double amount);
}

// ============================================================
//  OCP FIX: Strategy pattern for discounts
//  New customer type = new class; existing classes are untouched.
// ============================================================

/**
 * OCP — strategy for calculating a discount amount.
 * Each customer type gets its own class; no if-chain required.
 */
interface Sol1_DiscountStrategy {
    double calculateDiscount(double rawTotal);
    String getCustomerType();
}

/** No discount for regular customers. */
class RegularDiscount implements Sol1_DiscountStrategy {
    @Override public double calculateDiscount(double t) { return 0.0; }
    @Override public String getCustomerType()           { return "REGULAR"; }
}

/** 15 % for premium members. */
class PremiumDiscount implements Sol1_DiscountStrategy {
    @Override public double calculateDiscount(double t) { return t * 0.15; }
    @Override public String getCustomerType()           { return "PREMIUM"; }
}

/** 25 % for VIPs. */
class VipDiscount implements Sol1_DiscountStrategy {
    @Override public double calculateDiscount(double t) { return t * 0.25; }
    @Override public String getCustomerType()           { return "VIP"; }
}

/** 30 % for employees. */
class EmployeeDiscount implements Sol1_DiscountStrategy {
    @Override public double calculateDiscount(double t) { return t * 0.30; }
    @Override public String getCustomerType()           { return "EMPLOYEE"; }
}

/** 10 % for affiliate partners. */
class AffiliateDiscount implements Sol1_DiscountStrategy {
    @Override public double calculateDiscount(double t) { return t * 0.10; }
    @Override public String getCustomerType()           { return "AFFILIATE"; }
}

/**
 * OCP: Adding LOYALTY_GOLD next quarter is a new class (open for extension)
 * without touching the four existing classes above (closed for modification).
 * Example of the new class we WOULD add — no change to existing code needed:
 */
class LoyaltyGoldDiscount implements Sol1_DiscountStrategy {
    @Override public double calculateDiscount(double t) { return t * 0.18; }
    @Override public String getCustomerType()           { return "LOYALTY_GOLD"; }
}

/**
 * Registry that maps customer types to strategies.
 * OCP: register a new strategy once; lookup code never changes.
 */
class Sol1_DiscountStrategyRegistry {
    private final Map<String, Sol1_DiscountStrategy> strategies = new HashMap<>();

    public Sol1_DiscountStrategyRegistry() {
        // Register all known strategies
        register(new RegularDiscount());
        register(new PremiumDiscount());
        register(new VipDiscount());
        register(new EmployeeDiscount());
        register(new AffiliateDiscount());
        register(new LoyaltyGoldDiscount());
    }

    public void register(Sol1_DiscountStrategy strategy) {
        strategies.put(strategy.getCustomerType(), strategy);
    }

    public Sol1_DiscountStrategy forCustomerType(String customerType) {
        return strategies.getOrDefault(customerType, new RegularDiscount());
    }
}

// ============================================================
//  SRP FIX: Concrete implementations — each has ONE job
// ============================================================

/**
 * SRP — in-memory order storage (could be swapped for JPA without touching
 * anything else).
 */
class InMemoryOrderRepository implements Sol1_OrderRepository {
    private final Map<String, Sol1_Order> store = new LinkedHashMap<>();

    @Override
    public void save(Sol1_Order order) {
        store.put(order.getOrderId(), order);
        System.out.println("[DB] Saved order " + order.getOrderId());
    }

    @Override
    public Optional<Sol1_Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public List<Sol1_Order> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }
}

/**
 * SRP — only sends email confirmations.
 * Knows nothing about databases, PDFs, or payments.
 */
class EmailNotificationService implements Sol1_NotificationService {

    @Override
    public void sendOrderConfirmation(Sol1_Order order) {
        System.out.println("[EMAIL] Sending confirmation to customer "
                + order.getCustomerId() + " for order " + order.getOrderId());
        // Real implementation would call an SMTP client injected via constructor
    }
}

/**
 * SRP — only generates PDF invoices.
 */
class PdfDocumentService implements Sol1_DocumentService {

    @Override
    public void generateInvoice(Sol1_Order order, double discountAmount) {
        double subtotal = order.getTotal();
        double tax      = (subtotal - discountAmount) * 0.08;
        double total    = subtotal - discountAmount + tax;
        System.out.printf("[PDF] Invoice for %s: subtotal=%.2f  discount=%.2f  tax=%.2f  total=%.2f%n",
                order.getOrderId(), subtotal, discountAmount, tax, total);
    }
}

/**
 * SRP — only handles payment transactions.
 * No order saving, no email sending, just payment.
 */
class CreditCardPaymentService implements Sol1_PaymentService {

    @Override
    public boolean processPayment(Sol1_Order order, double amount) {
        System.out.printf("[PAY] Charging %.2f for order %s via %s%n",
                amount, order.getOrderId(), order.getPaymentMethod());
        return true; // stub — always succeeds
    }

    @Override
    public void processRefund(Sol1_Order order, double amount) {
        System.out.printf("[PAY] Refunding %.2f for order %s%n",
                amount, order.getOrderId());
    }
}

/**
 * SRP — only writes to the audit log.
 */
class AuditLogger {
    private final List<String> entries = new ArrayList<>();

    public void log(String event) {
        String entry = "[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                + "] " + event;
        entries.add(entry);
        System.out.println("[AUDIT] " + entry);
    }

    public List<String> getEntries() { return Collections.unmodifiableList(entries); }
}

// ============================================================
//  DIP FIX: OrderService depends on abstractions, not concretions
//  All dependencies injected via constructor — testable with mocks
// ============================================================

/**
 * DIP — OrderService works against interfaces, never against concrete classes.
 *
 * Constructor injection makes testing easy:
 *   - Pass in-memory / mock implementations in tests.
 *   - Pass real implementations in production.
 *   No reflection, no frameworks required.
 *
 * SRP — OrderService is an orchestrator. It coordinates the other services
 * but contains no business logic itself (no discount math, no SQL, no HTML).
 */
class Sol1_OrderService {

    // DIP: all fields are interface types
    private final Sol1_OrderRepository      repository;
    private final Sol1_NotificationService  notifier;
    private final Sol1_DocumentService      documentService;
    private final Sol1_PaymentService       paymentService;
    private final Sol1_DiscountStrategyRegistry discountRegistry;
    private final AuditLogger              auditLogger;

    /**
     * DIP: all dependencies injected — no "new ConcreteClass()" inside.
     */
    public Sol1_OrderService(Sol1_OrderRepository      repository,
                             Sol1_NotificationService  notifier,
                             Sol1_DocumentService      documentService,
                             Sol1_PaymentService       paymentService,
                             Sol1_DiscountStrategyRegistry discountRegistry,
                             AuditLogger              auditLogger) {
        this.repository       = repository;
        this.notifier         = notifier;
        this.documentService  = documentService;
        this.paymentService   = paymentService;
        this.discountRegistry = discountRegistry;
        this.auditLogger      = auditLogger;
    }

    /**
     * Processes a complete order:
     * 1. Calculate discount (OCP strategy)
     * 2. Process payment
     * 3. Persist order
     * 4. Generate invoice
     * 5. Send confirmation
     * 6. Audit log
     */
    public boolean processOrder(Sol1_Order order) {
        auditLogger.log("START processOrder " + order.getOrderId());

        // OCP: strategy selected by registry — no if-chain here
        Sol1_DiscountStrategy strategy = discountRegistry.forCustomerType(order.getCustomerType());
        double discount = strategy.calculateDiscount(order.getTotal());
        double amountDue = order.getTotal() - discount;

        boolean paid = paymentService.processPayment(order, amountDue);
        if (!paid) {
            order.setStatus("PAYMENT_FAILED");
            auditLogger.log("Payment failed for " + order.getOrderId());
            return false;
        }

        order.setStatus("CONFIRMED");
        repository.save(order);                            // SRP: repository's job
        documentService.generateInvoice(order, discount); // SRP: document service's job
        notifier.sendOrderConfirmation(order);             // SRP: notifier's job

        auditLogger.log("COMPLETE processOrder " + order.getOrderId()
                + " | discount=" + discount + " | paid=" + amountDue);
        return true;
    }

    /** Delegates to repository — OrderService doesn't own query logic. */
    public List<Sol1_Order> getAllOrders() {
        return repository.findAll();
    }
}

// ============================================================
//  DEMO MAIN — shows that the refactored code works
//  and that DIP lets us inject test doubles
// ============================================================
public class Exercise01_Solution {

    public static void main(String[] args) {
        System.out.println("========== SOLID Refactoring Demo ==========\n");

        // Wire up production dependencies via constructor injection (DIP)
        Sol1_OrderRepository       repo     = new InMemoryOrderRepository();
        Sol1_NotificationService   notifier = new EmailNotificationService();
        Sol1_DocumentService       docs     = new PdfDocumentService();
        Sol1_PaymentService        payments = new CreditCardPaymentService();
        Sol1_DiscountStrategyRegistry discounts = new Sol1_DiscountStrategyRegistry();
        AuditLogger                audit    = new AuditLogger();

        Sol1_OrderService service = new Sol1_OrderService(
                repo, notifier, docs, payments, discounts, audit);

        // Order 1: PREMIUM customer — 15 % discount
        List<Sol1_OrderItem> items1 = Arrays.asList(
                new Sol1_OrderItem("P001", "Wireless Headphones", 79.99, 2),
                new Sol1_OrderItem("P002", "USB-C Cable",          9.99, 5)
        );
        Sol1_Order order1 = new Sol1_Order("ORD-001", "CUST-42", "PREMIUM",
                items1, "CREDIT_CARD");

        System.out.println("-- Processing PREMIUM order --");
        boolean ok1 = service.processOrder(order1);
        System.out.println("Success: " + ok1);

        // Order 2: VIP customer — 25 % discount
        List<Sol1_OrderItem> items2 = Arrays.asList(
                new Sol1_OrderItem("P003", "Mechanical Keyboard", 149.99, 1)
        );
        Sol1_Order order2 = new Sol1_Order("ORD-002", "CUST-07", "VIP",
                items2, "PAYPAL");

        System.out.println("\n-- Processing VIP order --");
        boolean ok2 = service.processOrder(order2);
        System.out.println("Success: " + ok2);

        // Order 3: Future LOYALTY_GOLD type — OCP: no code change needed
        List<Sol1_OrderItem> items3 = Arrays.asList(
                new Sol1_OrderItem("P004", "Gaming Mouse", 59.99, 1)
        );
        Sol1_Order order3 = new Sol1_Order("ORD-003", "CUST-88", "LOYALTY_GOLD",
                items3, "CREDIT_CARD");

        System.out.println("\n-- Processing LOYALTY_GOLD order (new type, OCP) --");
        boolean ok3 = service.processOrder(order3);
        System.out.println("Success: " + ok3);

        System.out.println("\n-- All orders in repository --");
        service.getAllOrders().forEach(o ->
                System.out.println("  " + o.getOrderId() + " | " + o.getCustomerType()
                        + " | $" + String.format("%.2f", o.getTotal())
                        + " | " + o.getStatus()));

        // DIP in action: swap in a recording NotificationService for testing
        System.out.println("\n-- DIP: injecting a recording NotificationService --");
        List<String> sentNotifications = new ArrayList<>();
        Sol1_NotificationService recordingNotifier = order -> {
            String msg = "RECORDED confirmation for " + order.getOrderId();
            sentNotifications.add(msg);
            System.out.println("[TEST DOUBLE] " + msg);
        };

        Sol1_OrderService testableService = new Sol1_OrderService(
                repo, recordingNotifier, docs, payments, discounts, audit);

        List<Sol1_OrderItem> items4 = Arrays.asList(
                new Sol1_OrderItem("P005", "Desk Lamp", 34.99, 2)
        );
        Sol1_Order order4 = new Sol1_Order("ORD-004", "CUST-01", "REGULAR",
                items4, "CREDIT_CARD");

        testableService.processOrder(order4);
        System.out.println("Captured notifications: " + sentNotifications);
    }
}
