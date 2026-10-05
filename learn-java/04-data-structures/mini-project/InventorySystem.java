import java.util.*;
import java.util.stream.Collectors;

/**
 * InventorySystem.java — Module 04 Mini-Project
 *
 * An inventory management system demonstrating real-world use of:
 * - HashMap<String, Product>         : product catalog (id → product)
 * - HashMap<String, List<Product>>   : products by category
 * - PriorityQueue<Product>           : low-stock alert queue
 * - LinkedList<Transaction>          : transaction history
 * - TreeMap<String, Product>         : products sorted by name
 */
public class InventorySystem {

    // =========================================================
    // Domain Model
    // =========================================================

    enum Category { ELECTRONICS, CLOTHING, FOOD, FURNITURE, SPORTS, OTHER }

    static class Product {
        private final String id;
        private String name;
        private Category category;
        private double price;
        private int stock;

        Product(String id, String name, Category category, double price, int stock) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
        }

        // Getters
        String getId()        { return id; }
        String getName()      { return name; }
        Category getCategory(){ return category; }
        double getPrice()     { return price; }
        int getStock()        { return stock; }

        // Setters for mutable fields
        void setPrice(double price) { this.price = price; }
        void setStock(int stock)    { this.stock = stock; }

        @Override public String toString() {
            return String.format("[%s] %-20s %-12s $%8.2f  stock:%-4d",
                id, name, category, price, stock);
        }
    }

    enum TransactionType { RESTOCK, SALE, ADJUSTMENT, RETURN }

    record Transaction(
        String transactionId,
        String productId,
        TransactionType type,
        int quantity,
        double unitPrice,
        long timestamp
    ) {
        double total() { return quantity * unitPrice; }

        @Override public String toString() {
            return String.format("[%s] %s %s qty=%d @$%.2f = $%.2f",
                transactionId, type, productId, quantity, unitPrice, total());
        }
    }

    // =========================================================
    // Inventory System Core
    // =========================================================

    private final Map<String, Product>             catalog         = new HashMap<>();
    private final Map<String, List<Product>>       byCategory      = new HashMap<>();
    private final PriorityQueue<Product>           lowStockQueue   = new PriorityQueue<>(
        Comparator.comparingInt(Product::getStock)   // Lowest stock = highest priority
    );
    private final LinkedList<Transaction>          history         = new LinkedList<>();
    private final TreeMap<String, Product>         byName          = new TreeMap<>();

    private static final int LOW_STOCK_THRESHOLD = 10;
    private int transactionCounter = 0;

    // =========================================================
    // Product Management
    // =========================================================

    public void addProduct(Product product) {
        catalog.put(product.getId(), product);
        byCategory.computeIfAbsent(product.getCategory().name(), k -> new ArrayList<>())
                  .add(product);
        byName.put(product.getName(), product);
        if (product.getStock() <= LOW_STOCK_THRESHOLD) {
            lowStockQueue.offer(product);
        }
        System.out.println("Added: " + product);
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(catalog.get(id));
    }

    public Optional<Product> findByName(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    public List<Product> getByCategory(Category category) {
        return byCategory.getOrDefault(category.name(), Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(Product::getName))
                .collect(Collectors.toList());
    }

    public void updatePrice(String productId, double newPrice) {
        Product p = getProductOrThrow(productId);
        double oldPrice = p.getPrice();
        p.setPrice(newPrice);
        recordTransaction(productId, TransactionType.ADJUSTMENT, 0, newPrice);
        System.out.printf("Price updated: %s: $%.2f → $%.2f%n", p.getName(), oldPrice, newPrice);
    }

    // =========================================================
    // Stock Management
    // =========================================================

    public void restock(String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        Product p = getProductOrThrow(productId);
        boolean wasLowStock = p.getStock() <= LOW_STOCK_THRESHOLD;
        p.setStock(p.getStock() + quantity);

        // Remove from low-stock queue if now above threshold
        if (wasLowStock && p.getStock() > LOW_STOCK_THRESHOLD) {
            lowStockQueue.remove(p);
        }

        recordTransaction(productId, TransactionType.RESTOCK, quantity, p.getPrice());
        System.out.printf("Restocked: %s +%d → stock now %d%n",
            p.getName(), quantity, p.getStock());
    }

    public boolean sell(String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        Product p = getProductOrThrow(productId);
        if (p.getStock() < quantity) {
            System.out.printf("INSUFFICIENT STOCK: %s has %d, requested %d%n",
                p.getName(), p.getStock(), quantity);
            return false;
        }

        boolean wasAboveThreshold = p.getStock() > LOW_STOCK_THRESHOLD;
        p.setStock(p.getStock() - quantity);

        // Add to low-stock queue if just dropped below threshold
        if (wasAboveThreshold && p.getStock() <= LOW_STOCK_THRESHOLD) {
            lowStockQueue.offer(p);
            System.out.printf("LOW STOCK ALERT: %s has only %d units!%n",
                p.getName(), p.getStock());
        }

        recordTransaction(productId, TransactionType.SALE, quantity, p.getPrice());
        System.out.printf("Sold: %s x%d → stock now %d%n",
            p.getName(), quantity, p.getStock());
        return true;
    }

    public void processReturn(String productId, int quantity) {
        Product p = getProductOrThrow(productId);
        boolean wasLowStock = p.getStock() <= LOW_STOCK_THRESHOLD;
        p.setStock(p.getStock() + quantity);

        if (wasLowStock && p.getStock() > LOW_STOCK_THRESHOLD) {
            lowStockQueue.remove(p);
        }

        recordTransaction(productId, TransactionType.RETURN, quantity, p.getPrice());
        System.out.printf("Return processed: %s +%d → stock now %d%n",
            p.getName(), quantity, p.getStock());
    }

    // =========================================================
    // Alerts and Reports
    // =========================================================

    public List<Product> getLowStockAlerts() {
        // Copy and drain would modify the queue — return sorted list instead
        return catalog.values().stream()
            .filter(p -> p.getStock() <= LOW_STOCK_THRESHOLD)
            .sorted(Comparator.comparingInt(Product::getStock))
            .collect(Collectors.toList());
    }

    public void printLowStockReport() {
        List<Product> alerts = getLowStockAlerts();
        System.out.println("\n=== LOW STOCK ALERT REPORT ===");
        if (alerts.isEmpty()) {
            System.out.println("All products are well-stocked.");
        } else {
            System.out.printf("%-6s %-20s %8s%n", "ID", "Product", "Stock");
            System.out.println("-".repeat(40));
            alerts.forEach(p ->
                System.out.printf("%-6s %-20s %8d  ← REORDER NEEDED%n",
                    p.getId(), p.getName(), p.getStock())
            );
        }
    }

    public void printCatalog() {
        System.out.println("\n=== PRODUCT CATALOG (sorted by name) ===");
        System.out.printf("%-6s %-20s %-12s %8s %8s%n", "ID", "Name", "Category", "Price", "Stock");
        System.out.println("-".repeat(60));
        byName.values().forEach(p ->
            System.out.printf("%-6s %-20s %-12s $%7.2f %8d%n",
                p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getStock())
        );
        System.out.printf("Total products: %d%n", catalog.size());
    }

    public void printCategoryReport() {
        System.out.println("\n=== INVENTORY BY CATEGORY ===");
        byCategory.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String cat = entry.getKey();
                List<Product> products = entry.getValue();
                double totalValue = products.stream()
                    .mapToDouble(p -> p.getPrice() * p.getStock()).sum();
                System.out.printf("%s (%d items, total value: $%.2f):%n",
                    cat, products.size(), totalValue);
                products.stream()
                    .sorted(Comparator.comparing(Product::getName))
                    .forEach(p -> System.out.printf("  %s%n", p));
            });
    }

    public void printTransactionHistory() {
        System.out.println("\n=== TRANSACTION HISTORY (most recent first) ===");
        // LinkedList iterator starts from head (most recent added last)
        // We want most recent first — use descending iterator
        Iterator<Transaction> it = history.descendingIterator();
        int shown = 0;
        while (it.hasNext() && shown < 20) {
            System.out.println("  " + it.next());
            shown++;
        }
        System.out.printf("Total transactions: %d%n", history.size());
    }

    public void printSalesSummary() {
        System.out.println("\n=== SALES SUMMARY ===");

        // Total revenue from sales
        double totalRevenue = history.stream()
            .filter(t -> t.type() == TransactionType.SALE)
            .mapToDouble(Transaction::total)
            .sum();

        // Revenue by product
        Map<String, Double> revenueByProduct = history.stream()
            .filter(t -> t.type() == TransactionType.SALE)
            .collect(Collectors.groupingBy(
                Transaction::productId,
                Collectors.summingDouble(Transaction::total)
            ));

        System.out.printf("Total Revenue: $%.2f%n", totalRevenue);
        System.out.println("\nRevenue by product:");
        revenueByProduct.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(e -> {
                String productName = catalog.containsKey(e.getKey())
                    ? catalog.get(e.getKey()).getName()
                    : e.getKey();
                System.out.printf("  %-20s $%.2f%n", productName, e.getValue());
            });
    }

    // =========================================================
    // Helper Methods
    // =========================================================

    private Product getProductOrThrow(String id) {
        Product p = catalog.get(id);
        if (p == null) throw new NoSuchElementException("Product not found: " + id);
        return p;
    }

    private void recordTransaction(String productId, TransactionType type,
                                    int quantity, double price) {
        String txId = String.format("TX%05d", ++transactionCounter);
        history.addLast(new Transaction(txId, productId, type, quantity, price,
            System.currentTimeMillis()));
    }

    // =========================================================
    // Main — Demo
    // =========================================================

    public static void main(String[] args) {
        InventorySystem inv = new InventorySystem();

        System.out.println("=== Inventory System Demo ===\n");

        // Populate catalog
        System.out.println("--- Adding Products ---");
        inv.addProduct(new Product("E001", "Laptop Pro 15\"",   Category.ELECTRONICS, 1299.99, 45));
        inv.addProduct(new Product("E002", "Wireless Mouse",    Category.ELECTRONICS,   29.99, 8));  // Low stock!
        inv.addProduct(new Product("E003", "USB-C Hub",         Category.ELECTRONICS,   49.99, 120));
        inv.addProduct(new Product("E004", "Monitor 4K 27\"",   Category.ELECTRONICS,  449.99, 30));
        inv.addProduct(new Product("E005", "Mechanical Keyboard",Category.ELECTRONICS,  89.99, 3));  // Low stock!
        inv.addProduct(new Product("C001", "Dev T-Shirt L",     Category.CLOTHING,      24.99, 75));
        inv.addProduct(new Product("C002", "Hoodie XL",         Category.CLOTHING,      59.99, 40));
        inv.addProduct(new Product("F001", "Standing Desk",     Category.FURNITURE,    599.99, 15));
        inv.addProduct(new Product("F002", "Ergonomic Chair",   Category.FURNITURE,    349.99, 20));
        inv.addProduct(new Product("S001", "Yoga Mat",          Category.SPORTS,        39.99, 50));

        // Show initial state
        inv.printLowStockReport();

        // Simulate sales
        System.out.println("\n--- Processing Sales ---");
        inv.sell("E001", 5);   // Laptop
        inv.sell("E002", 3);   // Mouse — drops to 5, already low
        inv.sell("E003", 80);  // USB hub
        inv.sell("E004", 25);  // Monitor — drops to 5, triggers alert
        inv.sell("E002", 10);  // Mouse — insufficient stock!

        // Restock
        System.out.println("\n--- Restocking ---");
        inv.restock("E002", 50);  // Mouse
        inv.restock("E005", 100); // Keyboard

        // Returns
        System.out.println("\n--- Processing Returns ---");
        inv.processReturn("E001", 1);

        // Price update
        System.out.println("\n--- Price Update ---");
        inv.updatePrice("E003", 44.99);  // 10% discount

        // Reports
        inv.printCatalog();
        inv.printCategoryReport();
        inv.printLowStockReport();
        inv.printSalesSummary();
        inv.printTransactionHistory();
    }
}
