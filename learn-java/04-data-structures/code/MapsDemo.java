import java.util.*;
import java.util.stream.Collectors;

/**
 * MapsDemo.java
 *
 * Comprehensive demonstration of Java Map operations:
 * - HashMap patterns: frequency counting, grouping, caching
 * - LinkedHashMap: insertion-order maps, LRU cache
 * - TreeMap: sorted keys, range queries
 * - Real-world word frequency analysis
 */
public class MapsDemo {

    // =========================================================
    // 1. HashMap Basics
    // =========================================================

    static void hashMapBasics() {
        System.out.println("=== HashMap Basics ===\n");

        Map<String, Integer> population = new HashMap<>();

        // Putting entries
        population.put("Germany", 83_000_000);
        population.put("France",  67_000_000);
        population.put("UK",      67_000_000);
        population.put("Japan",  125_000_000);
        population.put("Brazil", 215_000_000);

        // Accessing entries
        System.out.println("Germany population: " + population.get("Germany"));
        System.out.println("USA population: " + population.get("USA"));  // null (not in map)
        System.out.println("USA (with default): " + population.getOrDefault("USA", 0));

        // Checking membership
        System.out.println("\nContains Germany: " + population.containsKey("Germany"));
        System.out.println("Contains 67000000: " + population.containsValue(67_000_000));

        // Removing
        population.remove("UK");
        System.out.println("After removing UK: " + population.keySet());

        // Conditional remove (only if value matches)
        population.remove("France", 67_000_001);  // Won't remove — value doesn't match
        System.out.println("France still in map: " + population.containsKey("France"));
        population.remove("France", 67_000_000);  // Will remove — value matches
        System.out.println("After conditional remove of France: " + population.containsKey("France"));

        // Size
        System.out.println("Number of entries: " + population.size());

        // Iterating
        System.out.println("\nAll entries:");
        for (Map.Entry<String, Integer> entry : population.entrySet()) {
            System.out.printf("  %-10s → %,d%n", entry.getKey(), entry.getValue());
        }

        // Java 8 forEach
        System.out.println("\nJapan and Brazil:");
        population.forEach((country, pop) -> {
            if (pop > 100_000_000) {
                System.out.printf("  %s: %,d%n", country, pop);
            }
        });
    }

    // =========================================================
    // 2. Frequency Counter — Classic Pattern
    // =========================================================

    static void frequencyCounter() {
        System.out.println("\n=== Frequency Counter ===\n");

        String text = "to be or not to be that is the question whether tis nobler " +
                      "to suffer the slings and arrows or to take arms";

        String[] words = text.split("\\s+");

        // --- Method 1: getOrDefault ---
        Map<String, Integer> freq1 = new HashMap<>();
        for (String word : words) {
            freq1.put(word, freq1.getOrDefault(word, 0) + 1);
        }

        // --- Method 2: merge (most idiomatic) ---
        Map<String, Integer> freq2 = new HashMap<>();
        for (String word : words) {
            freq2.merge(word, 1, Integer::sum);
        }

        // --- Method 3: compute ---
        Map<String, Integer> freq3 = new HashMap<>();
        for (String word : words) {
            freq3.compute(word, (k, v) -> v == null ? 1 : v + 1);
        }

        // All three produce the same result — use merge() for clarity

        System.out.println("Word frequencies (top 5):");
        freq2.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> System.out.printf("  %-12s: %d%n", e.getKey(), e.getValue()));

        // Find most frequent word
        String mostFrequent = Collections.max(freq2.entrySet(),
            Map.Entry.comparingByValue()).getKey();
        System.out.println("\nMost frequent word: \"" + mostFrequent + "\" (" + freq2.get(mostFrequent) + " times)");

        // Find words appearing more than once
        List<String> repeatedWords = freq2.entrySet().stream()
            .filter(e -> e.getValue() > 1)
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.toList());
        System.out.println("Words appearing more than once: " + repeatedWords);
    }

    // =========================================================
    // 3. Grouping Data
    // =========================================================

    record Employee(String name, String department, double salary) {}

    static void groupingData() {
        System.out.println("\n=== Grouping Data ===\n");

        List<Employee> employees = List.of(
            new Employee("Alice",   "Engineering", 95000),
            new Employee("Bob",     "Marketing",   72000),
            new Employee("Charlie", "Engineering", 88000),
            new Employee("Dave",    "Marketing",   68000),
            new Employee("Eve",     "Engineering", 105000),
            new Employee("Frank",   "HR",          65000),
            new Employee("Grace",   "HR",          70000),
            new Employee("Heidi",   "Marketing",   75000)
        );

        // --- Manual grouping with computeIfAbsent ---
        Map<String, List<Employee>> byDept = new HashMap<>();
        for (Employee emp : employees) {
            byDept.computeIfAbsent(emp.department(), k -> new ArrayList<>()).add(emp);
        }

        System.out.println("Employees by department:");
        byDept.forEach((dept, emps) -> {
            System.out.println("  " + dept + ":");
            emps.forEach(e -> System.out.printf("    %-8s $%.0f%n", e.name(), e.salary()));
        });

        // --- Using Streams (cleaner) ---
        Map<String, List<Employee>> byDeptStream = employees.stream()
            .collect(Collectors.groupingBy(Employee::department));

        // Average salary per department
        Map<String, Double> avgSalaryByDept = employees.stream()
            .collect(Collectors.groupingBy(
                Employee::department,
                Collectors.averagingDouble(Employee::salary)
            ));

        System.out.println("\nAverage salary by department:");
        avgSalaryByDept.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(e -> System.out.printf("  %-12s $%.0f%n", e.getKey(), e.getValue()));

        // Count per department
        Map<String, Long> countByDept = employees.stream()
            .collect(Collectors.groupingBy(Employee::department, Collectors.counting()));

        System.out.println("\nHeadcount by department: " + countByDept);

        // Top earner per department
        Map<String, Optional<Employee>> topEarnerByDept = employees.stream()
            .collect(Collectors.groupingBy(
                Employee::department,
                Collectors.maxBy(Comparator.comparingDouble(Employee::salary))
            ));

        System.out.println("\nTop earner per department:");
        topEarnerByDept.forEach((dept, emp) ->
            emp.ifPresent(e ->
                System.out.printf("  %-12s %s ($%.0f)%n", dept, e.name(), e.salary())
            )
        );
    }

    // =========================================================
    // 4. Memoization / Caching
    // =========================================================

    static Map<Integer, Long> fibCache = new HashMap<>();

    static long fibonacci(int n) {
        if (n <= 1) return n;
        // computeIfAbsent: only compute if not already cached
        return fibCache.computeIfAbsent(n, k -> fibonacci(k - 1) + fibonacci(k - 2));
    }

    static void memoizationDemo() {
        System.out.println("\n=== Memoization with HashMap ===\n");

        // Without memoization, fib(50) would take years
        // With memoization, it's O(n)
        for (int i = 0; i <= 15; i++) {
            System.out.printf("  fib(%2d) = %,d%n", i, fibonacci(i));
        }
        System.out.println("Cache size: " + fibCache.size() + " entries");
    }

    // =========================================================
    // 5. LinkedHashMap: Insertion Order + LRU Cache
    // =========================================================

    static void linkedHashMapDemo() {
        System.out.println("\n=== LinkedHashMap ===\n");

        // Insertion order preserved
        Map<String, Integer> insertionOrder = new LinkedHashMap<>();
        insertionOrder.put("Zebra", 1);
        insertionOrder.put("Apple", 2);
        insertionOrder.put("Mango", 3);

        System.out.println("LinkedHashMap preserves insertion order:");
        insertionOrder.forEach((k, v) -> System.out.println("  " + k));

        System.out.println("\nHashMap does NOT preserve order:");
        Map<String, Integer> noOrder = new HashMap<>(insertionOrder);
        noOrder.forEach((k, v) -> System.out.println("  " + k));

        // LRU Cache using LinkedHashMap (access-order mode)
        int cacheCapacity = 3;
        Map<Integer, String> lruCache = new LinkedHashMap<>(cacheCapacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                return size() > cacheCapacity;  // Evict when over capacity
            }
        };

        System.out.println("\nLRU Cache (capacity=3):");
        lruCache.put(1, "Page-1"); System.out.println("Put 1: " + lruCache.keySet());
        lruCache.put(2, "Page-2"); System.out.println("Put 2: " + lruCache.keySet());
        lruCache.put(3, "Page-3"); System.out.println("Put 3: " + lruCache.keySet());
        lruCache.get(1);           System.out.println("Get 1: " + lruCache.keySet() + " (1 moved to most-recent)");
        lruCache.put(4, "Page-4"); System.out.println("Put 4: " + lruCache.keySet() + " (2 evicted — LRU)");
    }

    // =========================================================
    // 6. TreeMap: Sorted Keys + Range Queries
    // =========================================================

    static void treeMapDemo() {
        System.out.println("\n=== TreeMap: Sorted Keys ===\n");

        // Score leaderboard sorted by score (descending = largest first)
        TreeMap<Integer, String> leaderboard = new TreeMap<>(Comparator.reverseOrder());
        leaderboard.put(95, "Alice");
        leaderboard.put(87, "Bob");
        leaderboard.put(72, "Charlie");
        leaderboard.put(99, "Dave");
        leaderboard.put(80, "Eve");

        System.out.println("Leaderboard:");
        int rank = 1;
        for (Map.Entry<Integer, String> entry : leaderboard.entrySet()) {
            System.out.printf("  #%d %s (%d)%n", rank++, entry.getValue(), entry.getKey());
        }

        System.out.println("\nTop scorer: " + leaderboard.firstEntry().getValue());
        System.out.println("Lowest scorer: " + leaderboard.lastEntry().getValue());

        // Event log: TreeMap by timestamp enables time-range queries
        TreeMap<Long, String> eventLog = new TreeMap<>();
        long now = System.currentTimeMillis();
        eventLog.put(now - 10000, "User logged in");
        eventLog.put(now - 8000,  "Searched for 'Java'");
        eventLog.put(now - 5000,  "Clicked on result");
        eventLog.put(now - 2000,  "Viewed page");
        eventLog.put(now,         "User logged out");

        // Get events in the last 6 seconds
        System.out.println("\nEvents in last 6 seconds:");
        eventLog.tailMap(now - 6000).forEach((ts, event) ->
            System.out.println("  " + event)
        );

        // Get a range of events (3s to 8s ago)
        System.out.println("\nEvents between 3s and 9s ago:");
        eventLog.subMap(now - 9000, now - 3000).forEach((ts, event) ->
            System.out.println("  " + event)
        );
    }

    // =========================================================
    // 7. Advanced Map Operations (Java 8+)
    // =========================================================

    static void advancedOperations() {
        System.out.println("\n=== Advanced Map Operations (Java 8+) ===\n");

        Map<String, Integer> inventory = new HashMap<>();
        inventory.put("apples", 50);
        inventory.put("bananas", 30);
        inventory.put("cherries", 100);

        // putIfAbsent: only add if key doesn't exist
        inventory.putIfAbsent("apples", 999);   // Ignored — apples already there
        inventory.putIfAbsent("dates", 25);     // Added — dates is new
        System.out.println("After putIfAbsent: " + inventory);

        // merge: great for aggregation
        // New shipment arrives — add to existing stock
        Map<String, Integer> shipment = Map.of("apples", 20, "elderberries", 40, "bananas", 10);
        shipment.forEach((fruit, qty) ->
            inventory.merge(fruit, qty, Integer::sum)
        );
        System.out.println("After shipment (merged): " + inventory);

        // computeIfPresent: update only if key exists
        inventory.computeIfPresent("apples", (k, v) -> v - 5);  // Sold 5 apples
        System.out.println("After selling 5 apples: " + inventory.get("apples"));

        // replaceAll: apply a function to all values
        Map<String, Integer> prices = new HashMap<>();
        prices.put("apple", 100);
        prices.put("banana", 50);
        prices.put("cherry", 200);

        prices.replaceAll((product, price) -> (int)(price * 1.10));  // 10% price increase
        System.out.println("After 10% increase: " + prices);
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        hashMapBasics();
        frequencyCounter();
        groupingData();
        memoizationDemo();
        linkedHashMapDemo();
        treeMapDemo();
        advancedOperations();
    }
}
