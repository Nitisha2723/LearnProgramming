/**
 * Exercise02_Solution.java
 *
 * SOLUTION: Functional Pipelines
 *
 * This file contains complete, correct implementations of all 10 Stream API tasks.
 *
 * KEY CONCEPTS DEMONSTRATED:
 *   - Stream filter, map, sorted, distinct, limit
 *   - Collectors: toList, groupingBy, summingDouble, maxBy, joining, partitioningBy
 *   - flatMap for flattening nested collections
 *   - Function composition with andThen()
 *   - Comparator.comparing() and reversed()
 *   - Map.Entry stream for processing map results
 *
 * PERFORMANCE NOTE:
 *   Streams are lazy — intermediate operations (filter, map, sorted) are only
 *   evaluated when a terminal operation (collect, forEach, count) is called.
 *   This means you can chain many operations without creating intermediate lists.
 */

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class Exercise02_Solution {

    // ==========================================================================
    // Data Model
    // ==========================================================================

    /**
     * Represents a product in our store catalog.
     * Java records automatically generate: constructor, getters, equals, hashCode, toString.
     */
    record Product(String name, String category, double price, int stock, boolean active) {}

    /**
     * Represents a customer order containing one or more product names.
     */
    record Order(String orderId, String customerId, List<String> productNames, double totalAmount) {}

    // ==========================================================================
    // Dataset (identical to exercise)
    // ==========================================================================

    static List<Product> getProducts() {
        return Arrays.asList(
            new Product("Laptop Pro 15",     "Electronics",  1299.99, 25,  true),
            new Product("Wireless Mouse",    "Electronics",    29.99, 150, true),
            new Product("USB-C Hub",         "Electronics",    49.99, 80,  true),
            new Product("Java Programming",  "Books",          39.99, 200, true),
            new Product("Clean Code",        "Books",          34.99, 180, true),
            new Product("Design Patterns",   "Books",          44.99, 120, true),
            new Product("Standing Desk",     "Furniture",    499.99, 10,  true),
            new Product("Ergonomic Chair",   "Furniture",    299.99, 15,  true),
            new Product("Monitor Stand",     "Furniture",     79.99, 40,  true),
            new Product("Vintage Keyboard",  "Electronics",  199.99, 0,   false), // discontinued
            new Product("Old Mouse",         "Electronics",   15.99, 0,   false)  // discontinued
        );
    }

    static List<Order> getOrders() {
        return Arrays.asList(
            new Order("ORD-001", "CUST-A", Arrays.asList("Laptop Pro 15", "USB-C Hub"),              1349.98),
            new Order("ORD-002", "CUST-B", Arrays.asList("Clean Code", "Design Patterns"),             79.98),
            new Order("ORD-003", "CUST-A", Arrays.asList("Wireless Mouse", "USB-C Hub"),               79.98),
            new Order("ORD-004", "CUST-C", Arrays.asList("Standing Desk", "Ergonomic Chair"),         799.98),
            new Order("ORD-005", "CUST-B", Arrays.asList("Java Programming"),                          39.99),
            new Order("ORD-006", "CUST-D", Arrays.asList("Laptop Pro 15", "Wireless Mouse", "USB-C Hub"), 1379.97),
            new Order("ORD-007", "CUST-C", Arrays.asList("Monitor Stand"),                             79.99)
        );
    }

    // ==========================================================================
    // TASK 1: Active products sorted by price (ascending)
    // ==========================================================================

    /**
     * Returns all active products in ascending price order.
     *
     * Pipeline:
     *   stream()                      → create stream from list
     *   .filter(Product::active)      → keep only active=true products
     *   .sorted(comparing price)      → sort cheapest to most expensive
     *   .collect(toList())            → materialize result into a List
     *
     * Comparator.comparing() creates a Comparator that extracts a sort key
     * from each element. Here Product::price extracts the double price field.
     */
    static List<Product> getActiveProductsSortedByPrice(List<Product> products) {
        return products.stream()
            .filter(Product::active)
            .sorted(Comparator.comparingDouble(Product::price))
            .collect(Collectors.toList());
    }

    // ==========================================================================
    // TASK 2: Product names in a category, sorted alphabetically
    // ==========================================================================

    /**
     * Returns names of all products in the given category, sorted A-Z.
     *
     * Pipeline:
     *   .filter(category equals)  → keep only matching category
     *   .map(Product::name)       → transform Product → String (name only)
     *   .sorted()                 → natural String ordering (alphabetical)
     *   .collect(toList())        → result list
     *
     * NOTE: map() transforms the stream's element type — we go from
     * Stream<Product> to Stream<String>, then collect to List<String>.
     */
    static List<String> getProductNamesByCategory(List<Product> products, String category) {
        return products.stream()
            .filter(p -> p.category().equals(category))
            .map(Product::name)
            .sorted()
            .collect(Collectors.toList());
    }

    // ==========================================================================
    // TASK 3: Total inventory value per category
    // ==========================================================================

    /**
     * Calculates total inventory value (price * stock) for each category.
     *
     * groupingBy(classifier, downstream):
     *   - classifier: determines the group key (here: product's category)
     *   - downstream: how to aggregate the group members
     *
     * summingDouble(mapper): sums the double values extracted by mapper.
     * Here the "value" of each product in inventory is price * stock.
     *
     * Result: Map<"Electronics" -> 5498.25, "Books" -> ...>
     */
    static Map<String, Double> getInventoryValueByCategory(List<Product> products) {
        return products.stream()
            .collect(Collectors.groupingBy(
                Product::category,
                Collectors.summingDouble(p -> p.price() * p.stock())
            ));
    }

    // ==========================================================================
    // TASK 4: Most expensive product per category
    // ==========================================================================

    /**
     * Finds the most expensive product in each category.
     *
     * maxBy(comparator): a downstream collector that finds the maximum element.
     * It returns Optional<Product> because a group could theoretically be empty
     * (though with groupingBy it never is — groups always have at least one element).
     *
     * The result type Map<String, Optional<Product>> reflects that Optional.
     * Callers must use .map(Product::name).orElse("none") to safely extract the name.
     */
    static Map<String, Optional<Product>> getMostExpensiveByCategory(List<Product> products) {
        return products.stream()
            .collect(Collectors.groupingBy(
                Product::category,
                Collectors.maxBy(Comparator.comparingDouble(Product::price))
            ));
    }

    // ==========================================================================
    // TASK 5: All unique product names that appear in any order
    // ==========================================================================

    /**
     * Gets all unique product names that appear in at least one order, sorted.
     *
     * CHALLENGE: Each order has a List<String> productNames — we have a
     * Stream<Order>, and we want a flat Stream<String> of all product names.
     *
     * flatMap(order -> order.productNames().stream()):
     *   - For each Order, create a Stream<String> of its product names
     *   - Flatten all those streams into one Stream<String>
     *   - This "unwraps" the nested structure: List<List<String>> → Stream<String>
     *
     * distinct(): removes duplicates (same product ordered multiple times)
     * sorted(): alphabetical order
     */
    static List<String> getOrderedProductNames(List<Order> orders) {
        return orders.stream()
            .flatMap(order -> order.productNames().stream())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    // ==========================================================================
    // TASK 6: Total spending per customer
    // ==========================================================================

    /**
     * Sums each customer's total spending across all their orders.
     *
     * groupingBy(Order::customerId): groups orders by customer ID
     * summingDouble(Order::totalAmount): sums the totalAmount within each group
     *
     * Result: {"CUST-A" -> 1429.96, "CUST-B" -> 119.97, ...}
     */
    static Map<String, Double> getTotalSpendingByCustomer(List<Order> orders) {
        return orders.stream()
            .collect(Collectors.groupingBy(
                Order::customerId,
                Collectors.summingDouble(Order::totalAmount)
            ));
    }

    // ==========================================================================
    // TASK 7: Top N highest-spending customers
    // ==========================================================================

    /**
     * Returns the top N customer IDs ranked by total spending (highest first).
     *
     * Build on getTotalSpendingByCustomer(), then:
     *   .entrySet().stream()           → stream of Map.Entry<String, Double>
     *   .sorted(comparingByValue desc) → sort entries by value, descending
     *   .limit(n)                      → take only the top N
     *   .map(Entry::getKey)            → extract just the customer ID string
     *   .collect(toList())             → result list
     *
     * Map.Entry.comparingByValue() returns a comparator on entries.
     * .reversed() flips it to descending (highest spending first).
     */
    static List<String> getTopSpenders(List<Order> orders, int n) {
        return getTotalSpendingByCustomer(orders).entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(n)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    // ==========================================================================
    // TASK 8: Formatted price list string
    // ==========================================================================

    /**
     * Generates a formatted price list for all active products, sorted by name.
     *
     * Collectors.joining(delimiter):
     *   Concatenates stream elements (Strings) using the given delimiter.
     *   With just "\n", it produces: "name1\nname2\n..." (no trailing newline).
     *
     * String.format("%-25s $%.2f", name, price):
     *   %-25s → left-align name in a 25-char field (nice columns)
     *   $%.2f → dollar sign + 2 decimal places for price
     */
    static String generatePriceList(List<Product> products) {
        return products.stream()
            .filter(Product::active)
            .sorted(Comparator.comparing(Product::name))
            .map(p -> String.format("%-25s $%.2f", p.name(), p.price()))
            .collect(Collectors.joining("\n"));
    }

    // ==========================================================================
    // TASK 9: Partition products into in-stock and out-of-stock
    // ==========================================================================

    /**
     * Partitions ALL products into two groups based on stock level.
     *
     * Collectors.partitioningBy(predicate):
     *   A special case of groupingBy that always produces exactly TWO groups:
     *   - true  key: products matching the predicate (stock > 0 → in stock)
     *   - false key: products NOT matching (stock == 0 → out of stock)
     *
     * Unlike groupingBy (which can have any number of groups), partitioningBy
     * guarantees both true and false keys are always present in the result.
     */
    static Map<Boolean, List<Product>> partitionByStock(List<Product> products) {
        return products.stream()
            .collect(Collectors.partitioningBy(p -> p.stock() > 0));
    }

    // ==========================================================================
    // TASK 10 (BONUS): Build a composed Function pipeline
    // ==========================================================================

    /**
     * Builds a Function<List<Product>, String> that applies a multi-step pipeline:
     *   1. Filter to active products
     *   2. Filter to price > 50
     *   3. Group by category
     *   4. Within each category, collect product names (sorted)
     *   5. Format as a string
     *
     * Function.andThen(after):
     *   Returns a composed function: first apply THIS function, then apply AFTER.
     *   It's like method chaining, but for functions: f.andThen(g) = g(f(x))
     *
     * WHY COMPOSE WITH andThen()?
     *   - Separates concerns: each step is independently readable and testable
     *   - The composed function can be stored, passed around, or reused
     *   - This is the foundation of pipeline/builder patterns in functional style
     *
     * NOTE: The steps below are written individually for clarity.
     * In production code, you might inline some of these, but named functions
     * make the intent clear and the pipeline easy to modify.
     */
    static Function<List<Product>, String> buildPremiumProductsPipeline() {

        // Step 1: Filter to active products
        Function<List<Product>, List<Product>> filterActive =
            prods -> prods.stream()
                .filter(Product::active)
                .collect(Collectors.toList());

        // Step 2: Filter to products priced above $50
        Function<List<Product>, List<Product>> filterPremiumPrice =
            prods -> prods.stream()
                .filter(p -> p.price() > 50)
                .collect(Collectors.toList());

        // Step 3: Group by category, with names sorted within each group
        Function<List<Product>, Map<String, List<String>>> groupByCategory =
            prods -> prods.stream()
                .collect(Collectors.groupingBy(
                    Product::category,
                    TreeMap::new,          // TreeMap so categories appear sorted
                    Collectors.mapping(
                        Product::name,
                        Collectors.toList()
                    )
                ));

        // Step 4: Format the map as a readable string
        Function<Map<String, List<String>>, String> formatOutput =
            map -> map.entrySet().stream()
                .map(e -> {
                    List<String> sortedNames = e.getValue().stream()
                        .sorted()
                        .collect(Collectors.toList());
                    return e.getKey() + ": " + sortedNames;
                })
                .collect(Collectors.joining("\n"));

        // Compose all four steps into a single Function using andThen()
        // The type flows: List<Product> → List<Product> → List<Product>
        //                              → Map<String, List<String>> → String
        return filterActive
            .andThen(filterPremiumPrice)
            .andThen(groupByCategory)
            .andThen(formatOutput);
    }

    // ==========================================================================
    // MAIN — runs all tasks and prints results
    // ==========================================================================

    public static void main(String[] args) {
        List<Product> products = getProducts();
        List<Order>   orders   = getOrders();

        System.out.println("=======================================");
        System.out.println("  Exercise 02 SOLUTION: Functional Pipelines");
        System.out.println("=======================================\n");

        // ---- Task 1 ----
        System.out.println("TASK 1 — Active products by price:");
        getActiveProductsSortedByPrice(products)
            .forEach(p -> System.out.printf("  %-25s $%.2f%n", p.name(), p.price()));

        // ---- Task 2 ----
        System.out.println("\nTASK 2 — Electronics products (alphabetical):");
        System.out.println("  " + getProductNamesByCategory(products, "Electronics"));

        // ---- Task 3 ----
        System.out.println("\nTASK 3 — Inventory value by category:");
        new TreeMap<>(getInventoryValueByCategory(products))
            .forEach((cat, val) -> System.out.printf("  %-15s $%,.2f%n", cat, val));

        // ---- Task 4 ----
        System.out.println("\nTASK 4 — Most expensive by category:");
        new TreeMap<>(getMostExpensiveByCategory(products))
            .forEach((cat, p) -> System.out.printf("  %-15s %s%n",
                cat, p.map(Product::name).orElse("none")));

        // ---- Task 5 ----
        System.out.println("\nTASK 5 — All ordered product names:");
        System.out.println("  " + getOrderedProductNames(orders));

        // ---- Task 6 ----
        System.out.println("\nTASK 6 — Total spending by customer:");
        new TreeMap<>(getTotalSpendingByCustomer(orders))
            .forEach((cust, total) -> System.out.printf("  %-10s $%,.2f%n", cust, total));

        // ---- Task 7 ----
        System.out.println("\nTASK 7 — Top 2 spenders:");
        System.out.println("  " + getTopSpenders(orders, 2));

        // ---- Task 8 ----
        System.out.println("\nTASK 8 — Price list:");
        System.out.println(generatePriceList(products));

        // ---- Task 9 ----
        System.out.println("\nTASK 9 — Stock partition:");
        Map<Boolean, List<Product>> partitioned = partitionByStock(products);
        System.out.println("  In stock:     " + partitioned.get(true).stream()
            .map(Product::name).collect(Collectors.toList()));
        System.out.println("  Out of stock: " + partitioned.get(false).stream()
            .map(Product::name).collect(Collectors.toList()));

        // ---- Task 10 ----
        System.out.println("\nTASK 10 (BONUS) — Premium products pipeline:");
        System.out.println(buildPremiumProductsPipeline().apply(products));
    }
}
