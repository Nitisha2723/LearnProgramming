// EXERCISE 01: Refactor to SOLID Principles
//
// The OrderManagementSystem below violates multiple SOLID principles.
// Your task: Identify and fix each violation.
//
// VIOLATIONS TO FIND:
// [ ] SRP: This class has X responsibilities. List them and split into separate classes.
// [ ] OCP: The calculateDiscount() method violates OCP. How would you extend it without modifying?
// [ ] LSP: Look at PremiumOrder extending Order. What's wrong?
// [ ] ISP: The OrderHandler interface is too fat. Split it.
// [ ] DIP: OrderManagementSystem creates its own dependencies. Inject them instead.
//
// HINTS: See theory/01-solid-principles.md for guidance.
// SOLUTION: See solutions/Exercise01_Solution.java

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ============================================================
//  BLOATED INTERFACE — violates ISP
//  Every class that "handles orders" must implement ALL of this,
//  even if it only cares about one aspect.
// ============================================================
interface OrderHandler {
    void saveOrder(Order order);
    void sendConfirmationEmail(Order order);
    void sendSmsNotification(Order order);
    void generateInvoicePdf(Order order);
    double calculateDiscount(Order order);
    boolean validatePayment(Order order);
    void updateInventory(Order order);
    void generateDailyReport();
    void generateMonthlyReport();
    void logOrder(Order order);
    void archiveOldOrders(int daysOld);
    void refundOrder(Order order);
    void notifyWarehouse(Order order);
    void checkFraud(Order order);
}

// ============================================================
//  FRAGILE BASE CLASS — violates LSP
//  PremiumOrder overrides getTotal() and breaks the contract
//  that Order establishes. Code that works with Order objects
//  will produce wrong results when handed a PremiumOrder.
// ============================================================
class Order {
    private String orderId;
    private String customerId;
    private String customerType;   // "REGULAR", "PREMIUM", "VIP", "EMPLOYEE", "AFFILIATE"
    private List<OrderItem> items;
    private String paymentMethod;  // "CREDIT_CARD", "PAYPAL", "CRYPTO"
    private LocalDateTime orderDate;
    private String status;

    public Order(String orderId, String customerId, String customerType,
                 List<OrderItem> items, String paymentMethod) {
        this.orderId     = orderId;
        this.customerId  = customerId;
        this.customerType = customerType;
        this.items       = items;
        this.paymentMethod = paymentMethod;
        this.orderDate   = LocalDateTime.now();
        this.status      = "PENDING";
    }

    // Contract: returns the raw sum of all item prices
    public double getTotal() {
        return items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    }

    public String getOrderId()      { return orderId; }
    public String getCustomerId()   { return customerId; }
    public String getCustomerType() { return customerType; }
    public List<OrderItem> getItems() { return items; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public String getStatus()       { return status; }
    public void setStatus(String s) { this.status = s; }
}

class OrderItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private int stockLevel; // current stock

    public OrderItem(String productId, String productName, double price, int quantity, int stockLevel) {
        this.productId   = productId;
        this.productName = productName;
        this.price       = price;
        this.quantity    = quantity;
        this.stockLevel  = stockLevel;
    }

    public String getProductId()   { return productId; }
    public String getProductName() { return productName; }
    public double getPrice()       { return price; }
    public int getQuantity()       { return quantity; }
    public int getStockLevel()     { return stockLevel; }
}

// LSP VIOLATION: PremiumOrder overrides getTotal() to already
// include the discount, so any code that computes
//   discountedPrice = order.getTotal() - calculateDiscount(order)
// double-counts the discount when given a PremiumOrder.
class PremiumOrder extends Order {
    private static final double PREMIUM_DISCOUNT = 0.15;

    public PremiumOrder(String orderId, String customerId,
                        List<OrderItem> items, String paymentMethod) {
        super(orderId, customerId, "PREMIUM", items, paymentMethod);
    }

    // !! Breaks the parent's contract — getTotal() no longer means "raw sum"
    @Override
    public double getTotal() {
        double raw = super.getTotal();
        return raw - (raw * PREMIUM_DISCOUNT); // discount already baked in
    }
}

// ============================================================
//  THE GOD CLASS — violates SRP, OCP, DIP
//  Responsibilities:
//    1. Persistence (saveOrder)
//    2. Email notifications
//    3. SMS notifications
//    4. PDF generation
//    5. Discount calculation
//    6. Payment validation
//    7. Inventory management
//    8. Report generation
//    9. Audit logging
//   10. Order archiving
//   11. Refunds
//   12. Warehouse notifications
//   13. Fraud detection
// ============================================================
class OrderManagementSystem implements OrderHandler {

    // DIP VIOLATION: concrete dependencies hard-coded
    private final List<Order> database    = new ArrayList<>();  // pretend DB
    private final List<String> emailLog   = new ArrayList<>();  // pretend mail server
    private final List<String> smsLog     = new ArrayList<>();  // pretend SMS gateway
    private final Map<String, Integer> inventory = new HashMap<>(); // pretend inventory DB
    private final List<String> auditLog   = new ArrayList<>();

    // Pretend SMTP client — instantiated directly (DIP violation)
    private final SmtpClient smtpClient   = new SmtpClient("smtp.example.com", 587);

    // Pretend payment gateway — instantiated directly (DIP violation)
    private final PaymentGateway paymentGateway = new PaymentGateway("https://pay.example.com");

    // Pretend PDF renderer — instantiated directly (DIP violation)
    private final PdfRenderer pdfRenderer  = new PdfRenderer();

    // ---- 1. Persistence ----------------------------------------

    @Override
    public void saveOrder(Order order) {
        // In reality this would be SQL / JPA
        database.add(order);
        System.out.println("[DB] Saved order " + order.getOrderId());
        logOrder(order); // SRP: persistence method also calls logging
    }

    // ---- 2. Email notification ---------------------------------

    @Override
    public void sendConfirmationEmail(Order order) {
        // Builds the message inline — no separation of concerns
        String subject = "Order Confirmation #" + order.getOrderId();
        String body = "Dear Customer " + order.getCustomerId() + ",\n\n"
                + "Thank you for your order!\n"
                + "Order ID : " + order.getOrderId() + "\n"
                + "Date     : " + order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "\n"
                + "Total    : $" + String.format("%.2f", order.getTotal()) + "\n"
                + "Status   : " + order.getStatus() + "\n\n"
                + "Items:\n";
        for (OrderItem item : order.getItems()) {
            body += "  - " + item.getProductName()
                  + " x" + item.getQuantity()
                  + " @ $" + item.getPrice() + "\n";
        }
        body += "\nThank you for shopping with us!";

        // Using hard-coded SmtpClient (DIP violation)
        smtpClient.send("orders@shop.com",
                        order.getCustomerId() + "@customers.com",
                        subject, body);
        emailLog.add("SENT to " + order.getCustomerId() + " at " + LocalDateTime.now());
        System.out.println("[EMAIL] Confirmation sent for " + order.getOrderId());
    }

    // ---- 3. SMS notification -----------------------------------

    @Override
    public void sendSmsNotification(Order order) {
        String message = "Your order " + order.getOrderId()
                       + " has been received. Total: $"
                       + String.format("%.2f", order.getTotal());
        smsLog.add(message);
        System.out.println("[SMS] Notification sent for " + order.getOrderId());
    }

    // ---- 4. PDF Invoice ----------------------------------------

    @Override
    public void generateInvoicePdf(Order order) {
        // All PDF layout logic lives here — massive SRP violation
        StringBuilder pdf = new StringBuilder();
        pdf.append("=== INVOICE ===\n");
        pdf.append("Invoice #: INV-").append(order.getOrderId()).append("\n");
        pdf.append("Date     : ").append(
                order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n");
        pdf.append("Customer : ").append(order.getCustomerId()).append("\n");
        pdf.append("---\n");
        double subtotal = 0;
        for (OrderItem item : order.getItems()) {
            double lineTotal = item.getPrice() * item.getQuantity();
            subtotal += lineTotal;
            pdf.append(String.format("%-30s %3d  $%8.2f  $%8.2f\n",
                    item.getProductName(), item.getQuantity(),
                    item.getPrice(), lineTotal));
        }
        double discount = calculateDiscount(order);
        double tax      = (subtotal - discount) * 0.08;
        double total    = subtotal - discount + tax;
        pdf.append("---\n");
        pdf.append(String.format("Subtotal : $%.2f\n", subtotal));
        pdf.append(String.format("Discount : -$%.2f\n", discount));
        pdf.append(String.format("Tax (8%%) : $%.2f\n", tax));
        pdf.append(String.format("TOTAL    : $%.2f\n", total));
        pdfRenderer.render(pdf.toString(), "invoice_" + order.getOrderId() + ".pdf");
        System.out.println("[PDF] Invoice generated for " + order.getOrderId());
    }

    // ---- 5. Discount calculation — OCP VIOLATION ---------------
    // Every new customer type requires modifying this method.
    // Adding a new type means changing existing, tested code.
    @Override
    public double calculateDiscount(Order order) {
        double total = order.getTotal();
        String customerType = order.getCustomerType();

        if (customerType.equals("REGULAR")) {
            // No discount for regular customers
            return 0.0;
        } else if (customerType.equals("PREMIUM")) {
            // 15 % discount
            return total * 0.15;
        } else if (customerType.equals("VIP")) {
            // 25 % discount
            return total * 0.25;
        } else if (customerType.equals("EMPLOYEE")) {
            // 30 % discount
            return total * 0.30;
        } else if (customerType.equals("AFFILIATE")) {
            // 10 % discount
            return total * 0.10;
        } else if (customerType.equals("STUDENT")) {
            // 5 % discount — added last year, required modifying this method
            return total * 0.05;
        } else if (customerType.equals("SENIOR")) {
            // 12 % discount — added this year, required modifying this method
            return total * 0.12;
        } else if (customerType.equals("MILITARY")) {
            // 20 % discount — added last month, required modifying this method
            return total * 0.20;
        }
        // TODO: Next quarter we need LOYALTY_GOLD, LOYALTY_SILVER, PARTNER ...
        // Every addition means another else-if and another risk of regression
        return 0.0;
    }

    // ---- 6. Payment validation ---------------------------------

    @Override
    public boolean validatePayment(Order order) {
        String method = order.getPaymentMethod();
        double amount = order.getTotal() - calculateDiscount(order);

        if (method.equals("CREDIT_CARD")) {
            boolean ok = paymentGateway.chargeCard(order.getCustomerId(), amount);
            if (!ok) {
                System.out.println("[PAYMENT] Credit card declined for " + order.getOrderId());
                order.setStatus("PAYMENT_FAILED");
            }
            return ok;
        } else if (method.equals("PAYPAL")) {
            boolean ok = paymentGateway.chargePaypal(order.getCustomerId(), amount);
            if (!ok) {
                System.out.println("[PAYMENT] PayPal payment failed for " + order.getOrderId());
                order.setStatus("PAYMENT_FAILED");
            }
            return ok;
        } else if (method.equals("CRYPTO")) {
            // Direct wallet integration — another hard-coded dependency
            boolean ok = paymentGateway.chargeCrypto(order.getCustomerId(), amount);
            if (!ok) {
                System.out.println("[PAYMENT] Crypto payment failed for " + order.getOrderId());
                order.setStatus("PAYMENT_FAILED");
            }
            return ok;
        }
        return false;
    }

    // ---- 7. Inventory management --------------------------------

    @Override
    public void updateInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            int current = inventory.getOrDefault(item.getProductId(), item.getStockLevel());
            int updated = current - item.getQuantity();
            if (updated < 0) {
                // Should this throw? Return a flag? Send an alert? All mixed in here.
                System.out.println("[INVENTORY] WARNING: Negative stock for "
                        + item.getProductId() + "! Setting to 0.");
                updated = 0;
            }
            inventory.put(item.getProductId(), updated);
            if (updated < 10) {
                // Reorder logic lives inside inventory update — another SRP violation
                System.out.println("[INVENTORY] Low stock alert: " + item.getProductName()
                        + " has only " + updated + " units left. Sending reorder request...");
                notifyWarehouse(order); // nested responsibility
            }
        }
    }

    // ---- 8 & 9. Reports -----------------------------------------

    @Override
    public void generateDailyReport() {
        System.out.println("=== DAILY REPORT (" +
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ") ===");
        System.out.println("Total orders today: " + database.size()); // wrong — counts all time
        double revenue = database.stream().mapToDouble(Order::getTotal).sum();
        System.out.println("Total revenue: $" + String.format("%.2f", revenue));
        System.out.println("Emails sent : " + emailLog.size());
    }

    @Override
    public void generateMonthlyReport() {
        // Copy-paste of daily report with slight differences — no abstraction
        System.out.println("=== MONTHLY REPORT (" +
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ") ===");
        System.out.println("Total orders this month: " + database.size());
        double revenue = database.stream().mapToDouble(Order::getTotal).sum();
        System.out.println("Monthly revenue: $" + String.format("%.2f", revenue));
    }

    // ---- 10. Audit logging ------------------------------------

    @Override
    public void logOrder(Order order) {
        String entry = "[" + LocalDateTime.now() + "] ORDER " + order.getOrderId()
                + " | customer=" + order.getCustomerId()
                + " | total=$" + String.format("%.2f", order.getTotal())
                + " | status=" + order.getStatus();
        auditLog.add(entry);
        System.out.println("[AUDIT] " + entry);
    }

    // ---- 11. Archiving -----------------------------------------

    @Override
    public void archiveOldOrders(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        long count = database.stream()
                .filter(o -> o.getOrderDate().isBefore(cutoff))
                .count();
        System.out.println("[ARCHIVE] Would archive " + count + " orders older than "
                + daysOld + " days.");
        // Real code would move them to archive table — all in one class
    }

    // ---- 12. Refunds -------------------------------------------

    @Override
    public void refundOrder(Order order) {
        double amount = order.getTotal() - calculateDiscount(order);
        // DIP: directly calling concrete payment gateway for refund
        paymentGateway.refund(order.getCustomerId(), amount);
        order.setStatus("REFUNDED");
        // Sends email without going through a notification layer
        smtpClient.send("orders@shop.com",
                        order.getCustomerId() + "@customers.com",
                        "Refund Processed",
                        "Your refund of $" + String.format("%.2f", amount) + " has been processed.");
        logOrder(order);
        System.out.println("[REFUND] Processed refund of $" + amount
                + " for order " + order.getOrderId());
    }

    // ---- 13. Warehouse notification ----------------------------

    @Override
    public void notifyWarehouse(Order order) {
        System.out.println("[WAREHOUSE] Notifying warehouse about order " + order.getOrderId());
        // Hard-coded warehouse URL — should be configurable
        System.out.println("[WAREHOUSE] POST https://warehouse.internal/api/orders -> "
                + order.getOrderId());
    }

    // ---- 14. Fraud detection -----------------------------------

    @Override
    public void checkFraud(Order order) {
        // Naive fraud rules inlined — would grow indefinitely here
        double total = order.getTotal();
        if (total > 5000) {
            System.out.println("[FRAUD] High-value order flagged: " + order.getOrderId());
        }
        if (order.getCustomerType().equals("REGULAR") && total > 2000) {
            System.out.println("[FRAUD] Unusual spend for regular customer: " + order.getOrderId());
        }
        // TODO: add IP check, device fingerprint, velocity check... all inline here
    }

    // ---- Main "process order" orchestration -------------------

    /**
     * God method: does everything in sequence.
     * If any step fails, the others still run — no transactional safety.
     * Hard to test because you can't inject mocks.
     */
    public void processOrder(Order order) {
        System.out.println("\n========== Processing Order " + order.getOrderId() + " ==========");

        checkFraud(order);

        boolean paymentOk = validatePayment(order);
        if (!paymentOk) {
            System.out.println("Order " + order.getOrderId() + " failed payment validation.");
            return;
        }

        order.setStatus("CONFIRMED");
        saveOrder(order);
        updateInventory(order);
        sendConfirmationEmail(order);
        sendSmsNotification(order);
        generateInvoicePdf(order);
        notifyWarehouse(order);

        System.out.println("========== Order " + order.getOrderId() + " processed ==========\n");
    }
}

// ============================================================
//  STUB DEPENDENCIES (hard-coded; not injectable)
//  In real code these would be framework-managed beans
//  or externally configured services.
// ============================================================

class SmtpClient {
    private final String host;
    private final int port;
    SmtpClient(String host, int port) { this.host = host; this.port = port; }
    public void send(String from, String to, String subject, String body) {
        System.out.println("[SMTP] " + from + " -> " + to + " | " + subject);
    }
}

class PaymentGateway {
    private final String url;
    PaymentGateway(String url) { this.url = url; }
    public boolean chargeCard(String customerId, double amount) {
        System.out.println("[PAY] Charging card $" + amount + " for " + customerId);
        return true; // always succeeds in stub
    }
    public boolean chargePaypal(String customerId, double amount) {
        System.out.println("[PAY] PayPal $" + amount + " for " + customerId);
        return true;
    }
    public boolean chargeCrypto(String customerId, double amount) {
        System.out.println("[PAY] Crypto $" + amount + " for " + customerId);
        return true;
    }
    public void refund(String customerId, double amount) {
        System.out.println("[PAY] Refund $" + amount + " to " + customerId);
    }
}

class PdfRenderer {
    public void render(String content, String filename) {
        System.out.println("[PDF] Rendered " + filename + " (" + content.length() + " chars)");
    }
}

// ============================================================
//  DEMO MAIN — run this to see the god class in action
// ============================================================
public class Exercise01_RefactorToSOLID {

    public static void main(String[] args) {
        List<OrderItem> items = Arrays.asList(
                new OrderItem("P001", "Wireless Headphones", 79.99, 2, 50),
                new OrderItem("P002", "USB-C Cable",          9.99, 5, 200)
        );

        // Regular order — works as expected
        Order regularOrder = new Order("ORD-001", "CUST-42", "PREMIUM",
                items, "CREDIT_CARD");

        OrderManagementSystem oms = new OrderManagementSystem();
        oms.processOrder(regularOrder);

        // !! LSP bug: PremiumOrder.getTotal() already deducts discount,
        //    so processOrder() double-counts the discount in the invoice
        PremiumOrder premiumOrder = new PremiumOrder("ORD-002", "CUST-99",
                items, "PAYPAL");

        System.out.println("Regular order total  : $" + regularOrder.getTotal());
        System.out.println("Premium order total  : $" + premiumOrder.getTotal()
                + "  <-- already discounted in getTotal()");
        System.out.println("Discount calculated  : $" + oms.calculateDiscount(premiumOrder)
                + "  <-- discount applied AGAIN in calculateDiscount()");

        oms.generateDailyReport();
    }
}
