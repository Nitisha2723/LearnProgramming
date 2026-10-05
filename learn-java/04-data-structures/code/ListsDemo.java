import java.util.*;
import java.util.stream.Collectors;

/**
 * ListsDemo.java
 *
 * Comprehensive demonstration of Java List operations:
 * - ArrayList basics and internals
 * - Sorting with Comparator
 * - List transformations
 * - Real-world task management scenario
 */
public class ListsDemo {

    // =========================================================
    // Domain Model
    // =========================================================

    record Product(String name, String category, double price, int stock) {
        // Records auto-generate equals(), hashCode(), toString(), and getters
    }

    // =========================================================
    // 1. ArrayList Basics
    // =========================================================

    static void arrayListBasics() {
        System.out.println("=== ArrayList Basics ===\n");

        // Creating lists
        List<String> cities = new ArrayList<>();
        cities.add("Berlin");
        cities.add("Paris");
        cities.add("Tokyo");
        cities.add("New York");
        cities.add("Sydney");

        System.out.println("Cities: " + cities);
        System.out.println("Size: " + cities.size());
        System.out.println("First city: " + cities.get(0));
        System.out.println("Last city: " + cities.get(cities.size() - 1));
        System.out.println("Contains Tokyo: " + cities.contains("Tokyo"));
        System.out.println("Index of Paris: " + cities.indexOf("Paris"));

        // Adding at specific position (shifts everything right — O(n))
        cities.add(2, "London");    // Insert "London" at index 2
        System.out.println("\nAfter inserting London at index 2: " + cities);

        // Removing by index vs by value
        cities.remove(0);                  // Remove by index (Berlin)
        System.out.println("After removing index 0: " + cities);

        cities.remove("Tokyo");            // Remove by value
        System.out.println("After removing Tokyo: " + cities);

        // Replacing an element
        cities.set(0, "Amsterdam");        // Replace Paris with Amsterdam
        System.out.println("After replacing index 0: " + cities);

        // Sublists (this is a VIEW — changes affect original!)
        List<String> europeView = cities.subList(0, 2);
        System.out.println("\nSubList view (indices 0-1): " + europeView);

        // Safe copy from subList
        List<String> europeCopy = new ArrayList<>(cities.subList(0, 2));

        // Initializing with content
        List<String> languages = new ArrayList<>(Arrays.asList("Java", "Python", "Go", "Rust"));
        List<String> immutable = List.of("Java", "Python", "Go");  // Java 9+, cannot modify
    }

    // =========================================================
    // 2. Sorting with Comparator
    // =========================================================

    static void sortingDemo() {
        System.out.println("\n=== Sorting with Comparator ===\n");

        List<Product> products = new ArrayList<>(List.of(
            new Product("Laptop",    "Electronics",  999.99, 50),
            new Product("Headphones","Electronics",   79.99, 200),
            new Product("Desk",      "Furniture",    299.99, 30),
            new Product("Chair",     "Furniture",    199.99, 45),
            new Product("Monitor",   "Electronics",  449.99, 80),
            new Product("Keyboard",  "Electronics",   59.99, 150)
        ));

        // Sort by price ascending (natural order for double)
        products.sort(Comparator.comparingDouble(Product::price));
        System.out.println("By price (ascending):");
        products.forEach(p -> System.out.printf("  %-12s $%.2f%n", p.name(), p.price()));

        // Sort by price descending
        products.sort(Comparator.comparingDouble(Product::price).reversed());
        System.out.println("\nBy price (descending):");
        products.forEach(p -> System.out.printf("  %-12s $%.2f%n", p.name(), p.price()));

        // Sort by category, then by price within each category
        products.sort(
            Comparator.comparing(Product::category)
                      .thenComparingDouble(Product::price)
        );
        System.out.println("\nBy category, then price:");
        products.forEach(p ->
            System.out.printf("  %-12s %-12s $%.2f%n", p.category(), p.name(), p.price())
        );

        // Sort by name length, then alphabetically for ties
        products.sort(
            Comparator.comparingInt((Product p) -> p.name().length())
                      .thenComparing(Product::name)
        );
        System.out.println("\nBy name length, then alphabetically:");
        products.forEach(p ->
            System.out.printf("  %-12s (len=%d)%n", p.name(), p.name().length())
        );

        // Natural order sort (String implements Comparable)
        List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "Bob", "Dave"));
        Collections.sort(names);
        System.out.println("\nNaturally sorted names: " + names);

        Collections.sort(names, Comparator.reverseOrder());
        System.out.println("Reverse sorted names: " + names);
    }

    // =========================================================
    // 3. List Transformations
    // =========================================================

    static void listTransformations() {
        System.out.println("\n=== List Transformations ===\n");

        List<Product> products = new ArrayList<>(List.of(
            new Product("Laptop",    "Electronics",  999.99, 50),
            new Product("Headphones","Electronics",   79.99, 200),
            new Product("Desk",      "Furniture",    299.99, 30),
            new Product("Chair",     "Furniture",    199.99, 45),
            new Product("Monitor",   "Electronics",  449.99, 80),
            new Product("Keyboard",  "Electronics",   59.99, 150)
        ));

        // --- Filtering ---
        List<Product> electronics = products.stream()
            .filter(p -> p.category().equals("Electronics"))
            .collect(Collectors.toList());
        System.out.println("Electronics only: " +
            electronics.stream().map(Product::name).collect(Collectors.joining(", ")));

        List<Product> affordable = products.stream()
            .filter(p -> p.price() < 300)
            .collect(Collectors.toList());
        System.out.println("Under $300: " +
            affordable.stream().map(Product::name).collect(Collectors.joining(", ")));

        // --- Mapping/Transforming ---
        List<String> productNames = products.stream()
            .map(Product::name)
            .collect(Collectors.toList());
        System.out.println("\nAll product names: " + productNames);

        List<String> discountedPrices = products.stream()
            .map(p -> p.name() + " → $" + String.format("%.2f", p.price() * 0.9))
            .collect(Collectors.toList());
        System.out.println("10% discounted prices:");
        discountedPrices.forEach(s -> System.out.println("  " + s));

        // --- replaceAll: modify in place ---
        List<String> words = new ArrayList<>(List.of("hello", "world", "java"));
        words.replaceAll(String::toUpperCase);
        System.out.println("\nUppercased words: " + words);

        // --- removeIf: remove elements matching a condition ---
        List<Product> inventory = new ArrayList<>(products);
        inventory.removeIf(p -> p.stock() < 50);  // Remove low stock
        System.out.println("In-stock products (>= 50): " +
            inventory.stream().map(Product::name).collect(Collectors.joining(", ")));

        // --- Combining lists ---
        List<String> list1 = new ArrayList<>(List.of("A", "B", "C"));
        List<String> list2 = List.of("D", "E", "F");
        list1.addAll(list2);
        System.out.println("\nCombined list: " + list1);

        // --- De-duplicating while preserving order ---
        List<String> withDups = new ArrayList<>(List.of("Alice", "Bob", "Alice", "Charlie", "Bob"));
        List<String> deduped = withDups.stream()
            .distinct()
            .collect(Collectors.toList());
        System.out.println("Deduplicated: " + deduped);

        // --- Finding min/max ---
        Product cheapest = products.stream()
            .min(Comparator.comparingDouble(Product::price))
            .orElseThrow();
        Product priciest = products.stream()
            .max(Comparator.comparingDouble(Product::price))
            .orElseThrow();
        System.out.println("\nCheapest: " + cheapest.name() + " ($" + cheapest.price() + ")");
        System.out.println("Most expensive: " + priciest.name() + " ($" + priciest.price() + ")");

        // --- Partitioning ---
        double avgPrice = products.stream()
            .mapToDouble(Product::price)
            .average()
            .orElse(0);
        Map<Boolean, List<Product>> partition = products.stream()
            .collect(Collectors.partitioningBy(p -> p.price() > avgPrice));
        System.out.println("\nAbove avg ($" + String.format("%.0f", avgPrice) + "):");
        partition.get(true).forEach(p -> System.out.println("  " + p.name()));
        System.out.println("Below/at avg:");
        partition.get(false).forEach(p -> System.out.println("  " + p.name()));
    }

    // =========================================================
    // 4. Real-World: Shopping Cart
    // =========================================================

    static class ShoppingCart {
        private final List<Product> items = new ArrayList<>();

        void addProduct(Product p) {
            items.add(p);
            System.out.println("Added: " + p.name());
        }

        void removeProduct(String productName) {
            boolean removed = items.removeIf(p -> p.name().equalsIgnoreCase(productName));
            System.out.println(removed ? "Removed: " + productName : productName + " not found");
        }

        double getTotal() {
            return items.stream().mapToDouble(Product::price).sum();
        }

        List<Product> getItemsSortedByPrice() {
            return items.stream()
                .sorted(Comparator.comparingDouble(Product::price).reversed())
                .collect(Collectors.toList());
        }

        void printReceipt() {
            System.out.println("\n--- Receipt ---");
            getItemsSortedByPrice().forEach(p ->
                System.out.printf("  %-15s  $%7.2f%n", p.name(), p.price())
            );
            System.out.printf("  %-15s  $%7.2f%n", "TOTAL", getTotal());
        }
    }

    static void shoppingCartDemo() {
        System.out.println("\n=== Shopping Cart Demo ===\n");

        ShoppingCart cart = new ShoppingCart();
        cart.addProduct(new Product("Laptop",    "Electronics", 999.99, 50));
        cart.addProduct(new Product("Headphones","Electronics",  79.99, 200));
        cart.addProduct(new Product("Keyboard",  "Electronics",  59.99, 150));
        cart.addProduct(new Product("Desk",      "Furniture",   299.99, 30));

        cart.removeProduct("Headphones");
        cart.printReceipt();
    }

    // =========================================================
    // 5. Performance Comparison: ArrayList vs LinkedList
    // =========================================================

    static void performanceDemo() {
        System.out.println("\n=== Performance: ArrayList vs LinkedList ===\n");
        int N = 100_000;

        // Test 1: Add to end
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();

        long start = System.nanoTime();
        for (int i = 0; i < N; i++) arrayList.add(i);
        long arrayListTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < N; i++) linkedList.add(i);
        long linkedListTime = System.nanoTime() - start;

        System.out.printf("Add %d elements to end:%n", N);
        System.out.printf("  ArrayList:   %,d ns%n", arrayListTime);
        System.out.printf("  LinkedList:  %,d ns%n", linkedListTime);

        // Test 2: Random access (get by index)
        start = System.nanoTime();
        for (int i = 0; i < 10_000; i++) arrayList.get(i * 7 % N);
        arrayListTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < 10_000; i++) linkedList.get(i * 7 % N);
        linkedListTime = System.nanoTime() - start;

        System.out.printf("%nRandom access (10,000 gets):%n");
        System.out.printf("  ArrayList:   %,d ns  (O(1) per get)%n", arrayListTime);
        System.out.printf("  LinkedList:  %,d ns  (O(n) per get!)%n", linkedListTime);
        System.out.println("  ArrayList is dramatically faster for random access");
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        arrayListBasics();
        sortingDemo();
        listTransformations();
        shoppingCartDemo();
        performanceDemo();
    }
}
