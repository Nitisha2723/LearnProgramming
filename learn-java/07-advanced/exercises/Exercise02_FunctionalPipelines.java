/**
 * Exercise02_FunctionalPipelines.java
 *
 * EXERCISE 02: Functional Pipelines
 *
 * Process a dataset using Stream operations.
 * All answers should be computed using Stream API — no explicit loops allowed.
 *
 * HINTS: See theory/02-functional-programming.md for Stream operations.
 * SOLUTION: See solutions/Exercise02_Solution.java
 */

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class Exercise02_FunctionalPipelines {

    // ==========================================================================
    // Data Model
    // ==========================================================================

    record Product(String name, String category, double price, int stock, boolean active) {}
    record Order(String orderId, String customerId, List<String> productNames, double totalAmount) {}

    // ==========================================================================
    // Dataset
    // ==========================================================================

    static List<Product> getProducts() {
        return Arrays.asList(
            new Product("Laptop Pro 15",     "Electronics",  1299.99, 25, true),
            new Product("Wireless Mouse",    "Electronics",    29.99, 150, true),
            new Product("USB-C Hub",         "Electronics",    49.99, 80, true),
            new Product("Java Programming",  "Books",          39.99, 200, true),
            new Product("Clean Code",        "Books",          34.99, 180, true),
            new Product("Design Patterns",   "Books",          44.99, 120, true),
            new Product("Standing Desk",     "Furniture",    499.99, 10, true),
            new Product("Ergonomic Chair",   "Furniture",    299.99, 15, true),
            new Product("Monitor Stand",     "Furniture",     79.99, 40, true),
            new Product("Vintage Keyboard",  "Electronics",  199.99, 0, false),  // discontinued
            new Product("Old Mouse",         "Electronics",   15.99, 0, false)   // discontinued
        );
    }

    static List<Order> getOrders() {
        return Arrays.asList(
            new Order("ORD-001", "CUST-A", Arrays.asList("Laptop Pro 15", "USB-C Hub"), 1349.98),
            new Order("ORD-002", "CUST-B", Arrays.asList("Clean Code", "Design Patterns"), 79.98),
            new Order("ORD-003", "CUST-A", Arrays.asList("Wireless Mouse", "USB-C Hub"), 79.98),
            new Order("ORD-004", "CUST-C", Arrays.asList("Standing Desk", "Ergonomic Chair"), 799.98),
            new Order("ORD-005", "CUST-B", Arrays.asList("Java Programming"), 39.99),
            new Order("ORD-006", "CUST-D", Arrays.asList("Laptop Pro 15", "Wireless Mouse", "USB-C Hub"), 1379.97),
            new Order("ORD-007", "CUST-C", Arrays.asList("Monitor Stand"), 79.99)
        );
    }

    // ==========================================================================
    // Tasks — implement each method using Stream API
    // ==========================================================================

    /**
     * TASK 1: Get all active products sorted by price (ascending).
     * Return a List<Product>.
     */
    static List<Product> getActiveProductsSortedByPrice(List<Product> products) {
        // TODO: Implement using stream().filter().sorted().collect()
        return Collections.emptyList();
    }

    /**
     * TASK 2: Get the names of all products in a given category, sorted alphabetically.
     * Return a List<String>.
     */
    static List<String> getProductNamesByCategory(List<Product> products, String category) {
        // TODO: Implement using filter(), map(), sorted(), collect()
        return Collections.emptyList();
    }

    /**
     * TASK 3: Calculate the total inventory value for each category.
     * Inventory value = price * stock for each product.
     * Return a Map<String, Double> where key = category, value = total inventory value.
     */
    static Map<String, Double> getInventoryValueByCategory(List<Product> products) {
        // TODO: Implement using Collectors.groupingBy() + Collectors.summingDouble()
        return Collections.emptyMap();
    }

    /**
     * TASK 4: Find the most expensive product in each category.
     * Return a Map<String, Optional<Product>>.
     */
    static Map<String, Optional<Product>> getMostExpensiveByCategory(List<Product> products) {
        // TODO: Implement using Collectors.groupingBy() + Collectors.maxBy()
        return Collections.emptyMap();
    }

    /**
     * TASK 5: Get all unique product names that appear in any order, sorted.
     * Return a List<String>.
     * Hint: Each order has a List<String> productNames — you need flatMap.
     */
    static List<String> getOrderedProductNames(List<Order> orders) {
        // TODO: Implement using flatMap(), distinct(), sorted(), collect()
        return Collections.emptyList();
    }

    /**
     * TASK 6: Calculate each customer's total spending across all orders.
     * Return a Map<String, Double> where key = customerId, value = total spent.
     */
    static Map<String, Double> getTotalSpendingByCustomer(List<Order> orders) {
        // TODO: Implement using Collectors.groupingBy() + Collectors.summingDouble()
        return Collections.emptyMap();
    }

    /**
     * TASK 7: Find the top N highest-spending customers.
     * Return a List<String> of customer IDs, ordered by total spending (highest first).
     *
     * Hint: Build on your getTotalSpendingByCustomer() result.
     *       Sort a map's entries by value descending, then take top N.
     */
    static List<String> getTopSpenders(List<Order> orders, int n) {
        // TODO: Implement using entrySet().stream().sorted().limit().map().collect()
        return Collections.emptyList();
    }

    /**
     * TASK 8: Generate a formatted price list string.
     * Format: "Name: $Price" for each active product, sorted by name, joined by newlines.
     * Example: "Clean Code: $34.99\nDesign Patterns: $44.99\n..."
     */
    static String generatePriceList(List<Product> products) {
        // TODO: Implement using filter(), sorted(), map(), Collectors.joining()
        return "";
    }

    /**
     * TASK 9: Partition products into two groups: in-stock and out-of-stock.
     * Return a Map<Boolean, List<Product>> where true = in stock (stock > 0).
     */
    static Map<Boolean, List<Product>> partitionByStock(List<Product> products) {
        // TODO: Implement using Collectors.partitioningBy()
        return Collections.emptyMap();
    }

    /**
     * TASK 10 (BONUS — harder): Build a Function pipeline.
     *
     * Create a Function<List<Product>, String> that:
     *   1. Filters to active products only
     *   2. Filters to products with price > 50
     *   3. Groups by category
     *   4. For each category, gets the product names
     *   5. Returns a formatted string like:
     *      "Books: []\nElectronics: [Laptop Pro 15, USB-C Hub]\nFurniture: [Standing Desk, Ergonomic Chair]"
     *
     * Compose it from smaller functions using andThen().
     */
    static Function<List<Product>, String> buildPremiumProductsPipeline() {
        // TODO: Build and return a composed Function
        return products -> "TODO";
    }

    // ==========================================================================
    // MAIN — runs all tasks and prints results
    // ==========================================================================

    public static void main(String[] args) {
        List<Product> products = getProducts();
        List<Order> orders = getOrders();

        System.out.println("=======================================");
        System.out.println("  Exercise 02: Functional Pipelines");
        System.out.println("=======================================\n");

        System.out.println("TASK 1 — Active products by price:");
        getActiveProductsSortedByPrice(products)
            .forEach(p -> System.out.printf("  %s: $%.2f%n", p.name(), p.price()));

        System.out.println("\nTASK 2 — Electronics products (alphabetical):");
        System.out.println(getProductNamesByCategory(products, "Electronics"));

        System.out.println("\nTASK 3 — Inventory value by category:");
        getInventoryValueByCategory(products)
            .forEach((cat, val) -> System.out.printf("  %s: $%.2f%n", cat, val));

        System.out.println("\nTASK 4 — Most expensive by category:");
        getMostExpensiveByCategory(products)
            .forEach((cat, p) -> System.out.println("  " + cat + ": " +
                p.map(Product::name).orElse("none")));

        System.out.println("\nTASK 5 — All ordered product names:");
        System.out.println(getOrderedProductNames(orders));

        System.out.println("\nTASK 6 — Total spending by customer:");
        getTotalSpendingByCustomer(orders)
            .forEach((cust, total) -> System.out.printf("  %s: $%.2f%n", cust, total));

        System.out.println("\nTASK 7 — Top 2 spenders:");
        System.out.println(getTopSpenders(orders, 2));

        System.out.println("\nTASK 8 — Price list:");
        System.out.println(generatePriceList(products));

        System.out.println("\nTASK 9 — Stock partition:");
        Map<Boolean, List<Product>> partitioned = partitionByStock(products);
        System.out.println("  In stock: " + partitioned.get(true).stream()
            .map(Product::name).collect(Collectors.toList()));
        System.out.println("  Out of stock: " + partitioned.get(false).stream()
            .map(Product::name).collect(Collectors.toList()));

        System.out.println("\nTASK 10 (BONUS) — Premium products pipeline:");
        System.out.println(buildPremiumProductsPipeline().apply(products));
    }
}
