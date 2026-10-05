/**
 * AdvancedTest.java
 *
 * Unit Tests for Module 07 — Advanced Java
 *
 * Tests cover three areas:
 *   1. GenericsTests   — Generic data structures (BST, Pair, Result, Stack)
 *   2. FunctionalTests — Stream API pipelines and collectors
 *   3. ConcurrencyTests — AtomicInteger, ConcurrentHashMap, CompletableFuture
 *
 * This file is self-contained — it does NOT import the Exercise files.
 * All data classes and helpers are defined as inner classes or static helpers.
 *
 * Compile:  javac -cp junit-platform-console-standalone.jar AdvancedTest.java
 * Run:      java -jar junit-platform-console-standalone.jar --select-class AdvancedTest
 */

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;

public class AdvancedTest {

    // =========================================================================
    // ---- Inner data structures used by tests (self-contained) ----
    // =========================================================================

    // --- Generic Stack ---

    /**
     * A generic LIFO stack backed by an ArrayList.
     * Used to test generic types and push/pop ordering.
     */
    static class Stack<T> {
        private final List<T> elements = new ArrayList<>();

        public void push(T item) { elements.add(item); }

        public T pop() {
            if (isEmpty()) throw new EmptyStackException();
            return elements.remove(elements.size() - 1);
        }

        public T peek() {
            if (isEmpty()) throw new EmptyStackException();
            return elements.get(elements.size() - 1);
        }

        public int size()     { return elements.size(); }
        public boolean isEmpty() { return elements.isEmpty(); }
    }

    // --- Generic Pair ---

    /**
     * Immutable pair of two (possibly different typed) values.
     */
    static class Pair<A, B> {
        private final A first;
        private final B second;

        private Pair(A first, B second) {
            this.first  = first;
            this.second = second;
        }

        public static <A, B> Pair<A, B> of(A first, B second) {
            return new Pair<>(first, second);
        }

        public A getFirst()  { return first; }
        public B getSecond() { return second; }

        /** Returns a new Pair with the elements swapped. */
        public Pair<B, A> swap() {
            return new Pair<>(second, first);
        }
    }

    // --- Generic Result (functional error handling) ---

    /**
     * Represents either a successful value or a failure with an error message.
     * This is a simplified version of Either/Try monads from functional programming.
     */
    static class Result<T> {
        private final T value;
        private final String error;
        private final boolean success;

        private Result(T value, String error, boolean success) {
            this.value   = value;
            this.error   = error;
            this.success = success;
        }

        public static <T> Result<T> success(T value) {
            return new Result<>(value, null, true);
        }

        public static <T> Result<T> failure(String error) {
            return new Result<>(null, error, false);
        }

        public boolean isSuccess()  { return success; }
        public boolean isFailure()  { return !success; }
        public T getValue()         { return value; }
        public String getError()    { return error; }

        /** Transform the value if successful; leave failure unchanged. */
        public <U> Result<U> map(Function<T, U> mapper) {
            if (success) {
                return Result.success(mapper.apply(value));
            } else {
                return Result.failure(error);
            }
        }

        /** Return value if successful, otherwise return the default. */
        public T orElse(T defaultValue) {
            return success ? value : defaultValue;
        }
    }

    // --- Generic Binary Search Tree ---

    /**
     * Binary Search Tree used in GenericsTests.
     * (Same implementation as Exercise01_Solution, repeated here for self-containment.)
     */
    static class BST<T extends Comparable<T>> {
        private Node<T> root;
        private int size;

        private static class Node<T> {
            T value;
            Node<T> left, right;
            Node(T v) { this.value = v; }
        }

        public void insert(T value) { root = insert(root, value); }

        private Node<T> insert(Node<T> node, T value) {
            if (node == null) { size++; return new Node<>(value); }
            int c = value.compareTo(node.value);
            if      (c < 0) node.left  = insert(node.left,  value);
            else if (c > 0) node.right = insert(node.right, value);
            return node;
        }

        public boolean contains(T value) { return contains(root, value); }

        private boolean contains(Node<T> node, T value) {
            if (node == null) return false;
            int c = value.compareTo(node.value);
            if      (c < 0) return contains(node.left,  value);
            else if (c > 0) return contains(node.right, value);
            else            return true;
        }

        public void inOrder(Consumer<T> action) { inOrder(root, action); }

        private void inOrder(Node<T> node, Consumer<T> action) {
            if (node == null) return;
            inOrder(node.left, action);
            action.accept(node.value);
            inOrder(node.right, action);
        }

        public T min() {
            if (root == null) throw new NoSuchElementException();
            Node<T> n = root;
            while (n.left != null) n = n.left;
            return n.value;
        }

        public T max() {
            if (root == null) throw new NoSuchElementException();
            Node<T> n = root;
            while (n.right != null) n = n.right;
            return n.value;
        }

        public int size() { return size; }
    }

    // --- Simple Product record used by FunctionalTests ---

    record Product(String name, String category, double price, int stock) {}

    // --- Simple Employee record used by FunctionalTests ---

    record Employee(String name, String department, double salary) {}

    // =========================================================================
    // ---- TEST SUITES ----
    // =========================================================================

    // =========================================================================
    // 1. GENERICS TESTS
    // =========================================================================

    @Nested
    @DisplayName("Generics Tests")
    class GenericsTests {

        // --- Stack tests ---

        @Test
        @DisplayName("Stack: push and pop in LIFO order")
        void stackPushPopOrder() {
            Stack<Integer> stack = new Stack<>();
            stack.push(10);
            stack.push(20);
            stack.push(30);

            // Last in, first out
            assertEquals(30, stack.pop());
            assertEquals(20, stack.pop());
            assertEquals(10, stack.pop());
        }

        @Test
        @DisplayName("Stack: pop from empty stack throws EmptyStackException")
        void stackPopEmptyThrows() {
            Stack<String> stack = new Stack<>();
            assertThrows(EmptyStackException.class, stack::pop,
                "Popping an empty stack should throw EmptyStackException");
        }

        @Test
        @DisplayName("Stack: size is tracked correctly after push and pop")
        void stackSizeTracking() {
            Stack<String> stack = new Stack<>();
            assertEquals(0, stack.size());
            assertTrue(stack.isEmpty());

            stack.push("a");
            stack.push("b");
            assertEquals(2, stack.size());
            assertFalse(stack.isEmpty());

            stack.pop();
            assertEquals(1, stack.size());
        }

        // --- Pair tests ---

        @Test
        @DisplayName("Pair: getFirst and getSecond return correct elements")
        void pairGetFirstSecond() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            assertEquals("hello", pair.getFirst());
            assertEquals(42, pair.getSecond());
        }

        @Test
        @DisplayName("Pair: swap returns new Pair with elements reversed")
        void pairSwap() {
            Pair<String, Integer> original = Pair.of("hello", 42);
            Pair<Integer, String> swapped  = original.swap();

            assertEquals(42,      swapped.getFirst());
            assertEquals("hello", swapped.getSecond());
        }

        @Test
        @DisplayName("Pair: factory method of() creates pair correctly")
        void pairOfFactory() {
            Pair<Double, Boolean> pair = Pair.of(3.14, true);
            assertEquals(3.14, pair.getFirst(), 0.001);
            assertTrue(pair.getSecond());
        }

        // --- Result tests ---

        @Test
        @DisplayName("Result: success holds value and isSuccess returns true")
        void resultSuccess() {
            Result<Integer> result = Result.success(42);

            assertTrue(result.isSuccess());
            assertFalse(result.isFailure());
            assertEquals(42, result.getValue());
            assertNull(result.getError());
        }

        @Test
        @DisplayName("Result: failure holds error message and isFailure returns true")
        void resultFailure() {
            Result<Integer> result = Result.failure("Division by zero");

            assertFalse(result.isSuccess());
            assertTrue(result.isFailure());
            assertNull(result.getValue());
            assertEquals("Division by zero", result.getError());
        }

        @Test
        @DisplayName("Result: map transforms value on success")
        void resultMapOnSuccess() {
            Result<Integer> result = Result.success(10);
            Result<String>  mapped = result.map(n -> "Value is " + n);

            assertTrue(mapped.isSuccess());
            assertEquals("Value is 10", mapped.getValue());
        }

        @Test
        @DisplayName("Result: map on failure passes the error through unchanged")
        void resultMapOnFailure() {
            Result<Integer> failed = Result.failure("Something went wrong");
            Result<String>  mapped = failed.map(n -> "Value is " + n);

            // map on failure should NOT call the function — just propagate the error
            assertTrue(mapped.isFailure());
            assertEquals("Something went wrong", mapped.getError());
        }

        @Test
        @DisplayName("Result: orElse returns value on success and default on failure")
        void resultOrElse() {
            Result<String> success = Result.success("actual");
            Result<String> failure = Result.failure("oops");

            assertEquals("actual",  success.orElse("default"));
            assertEquals("default", failure.orElse("default"));
        }

        // --- BST tests ---

        @Test
        @DisplayName("BST: insert and inOrder produces sorted output")
        void bstInsertAndInOrder() {
            BST<Integer> tree = new BST<>();
            int[] values = {5, 3, 7, 1, 4, 6, 8};
            for (int v : values) tree.insert(v);

            List<Integer> result = new ArrayList<>();
            tree.inOrder(result::add);

            assertEquals(List.of(1, 3, 4, 5, 6, 7, 8), result,
                "In-order traversal must produce sorted ascending list");
        }

        @Test
        @DisplayName("BST: contains returns true for inserted values, false for others")
        void bstContains() {
            BST<String> tree = new BST<>();
            tree.insert("banana");
            tree.insert("apple");
            tree.insert("cherry");

            assertTrue(tree.contains("apple"));
            assertTrue(tree.contains("banana"));
            assertTrue(tree.contains("cherry"));
            assertFalse(tree.contains("date"));
            assertFalse(tree.contains(""));
        }

        @Test
        @DisplayName("BST: min returns the smallest value (leftmost node)")
        void bstMin() {
            BST<Integer> tree = new BST<>();
            for (int v : new int[]{5, 3, 7, 1, 9}) tree.insert(v);
            assertEquals(1, tree.min());
        }

        @Test
        @DisplayName("BST: max returns the largest value (rightmost node)")
        void bstMax() {
            BST<Integer> tree = new BST<>();
            for (int v : new int[]{5, 3, 7, 1, 9}) tree.insert(v);
            assertEquals(9, tree.max());
        }
    }

    // =========================================================================
    // 2. FUNCTIONAL PIPELINE TESTS
    // =========================================================================

    @Nested
    @DisplayName("Functional Tests")
    class FunctionalTests {

        // Sample data — defined here, not in the Exercise files
        private final List<Employee> employees = List.of(
            new Employee("Alice",   "Engineering", 95000),
            new Employee("Bob",     "Marketing",   48000),
            new Employee("Carol",   "Engineering", 85000),
            new Employee("Dave",    "HR",           42000),
            new Employee("Eve",     "Engineering", 120000),
            new Employee("Frank",   "Marketing",   52000)
        );

        private final List<Product> products = List.of(
            new Product("Laptop",    "Electronics", 999.0,  10),
            new Product("Mouse",     "Electronics",  29.0, 100),
            new Product("Desk",      "Furniture",   450.0,  20),
            new Product("Chair",     "Furniture",   250.0,  15),
            new Product("Java Book", "Books",        40.0, 200),
            new Product("Out Item",  "Books",        15.0,   0)
        );

        @Test
        @DisplayName("filter + map: employees with salary > 50000 mapped to names")
        void filterAndMapPipeline() {
            List<String> highEarners = employees.stream()
                .filter(e -> e.salary() > 50000)
                .map(Employee::name)
                .sorted()
                .collect(Collectors.toList());

            assertEquals(List.of("Alice", "Carol", "Eve", "Frank"), highEarners);
        }

        @Test
        @DisplayName("groupingBy: products correctly grouped by category")
        void groupingByCategory() {
            Map<String, List<Product>> byCategory = products.stream()
                .collect(Collectors.groupingBy(Product::category));

            assertEquals(2, byCategory.get("Electronics").size());
            assertEquals(2, byCategory.get("Furniture").size());
            assertEquals(2, byCategory.get("Books").size());

            // Verify a specific member
            List<String> electronicsNames = byCategory.get("Electronics").stream()
                .map(Product::name).sorted().collect(Collectors.toList());
            assertEquals(List.of("Laptop", "Mouse"), electronicsNames);
        }

        @Test
        @DisplayName("flatMap: nested list-of-lists flattened to single stream")
        void flatMapNestedLists() {
            List<List<Integer>> nested = List.of(
                List.of(1, 2, 3),
                List.of(4, 5),
                List.of(6, 7, 8, 9)
            );

            List<Integer> flat = nested.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

            assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), flat);
            assertEquals(9, flat.size());
        }

        @Test
        @DisplayName("partitioningBy: products partitioned into in-stock and out-of-stock")
        void partitioningByStock() {
            Map<Boolean, List<Product>> partitioned = products.stream()
                .collect(Collectors.partitioningBy(p -> p.stock() > 0));

            // Both keys must always be present
            assertTrue(partitioned.containsKey(true));
            assertTrue(partitioned.containsKey(false));

            // "Out Item" is the only one with stock=0
            assertEquals(1, partitioned.get(false).size());
            assertEquals("Out Item", partitioned.get(false).get(0).name());

            // The other 5 products are in stock
            assertEquals(5, partitioned.get(true).size());
        }

        @Test
        @DisplayName("Function.andThen: composed function applies both transformations")
        void functionAndThenComposition() {
            Function<Integer, Integer> doubler   = x -> x * 2;
            Function<Integer, String>  formatter = n -> "Result: " + n;

            // andThen composes: formatter(doubler(x))
            Function<Integer, String> composed = doubler.andThen(formatter);

            assertEquals("Result: 10", composed.apply(5));
            assertEquals("Result: 0",  composed.apply(0));
            assertEquals("Result: -6", composed.apply(-3));
        }

        @Test
        @DisplayName("Predicate.and: combined predicate requires both conditions true")
        void predicateAndCombination() {
            Predicate<Employee> highSalary   = e -> e.salary() > 50000;
            Predicate<Employee> engineering  = e -> e.department().equals("Engineering");

            // Both predicates must be true — AND logic
            List<String> result = employees.stream()
                .filter(highSalary.and(engineering))
                .map(Employee::name)
                .sorted()
                .collect(Collectors.toList());

            // Alice (95k, Eng), Carol (85k, Eng), Eve (120k, Eng) qualify
            // Frank (52k, Marketing) — wrong department
            // Bob/Dave — salary too low
            assertEquals(List.of("Alice", "Carol", "Eve"), result);
        }

        @Test
        @DisplayName("Collectors.joining: strings joined with correct delimiter, prefix, suffix")
        void collectorsJoiningWithPrefixSuffix() {
            List<String> names = List.of("Alice", "Bob", "Carol");

            String result = names.stream()
                .collect(Collectors.joining(", ", "[", "]"));

            assertEquals("[Alice, Bob, Carol]", result);
        }
    }

    // =========================================================================
    // 3. CONCURRENCY TESTS
    // =========================================================================

    @Nested
    @DisplayName("Concurrency Tests")
    class ConcurrencyTests {

        /**
         * Ten threads each increment an AtomicInteger 1000 times.
         * Without thread safety, some increments would be lost.
         * AtomicInteger.getAndIncrement() is atomic, so all 10,000 count.
         */
        @Test
        @DisplayName("AtomicInteger: 10 threads × 1000 increments = 10000")
        void atomicIntegerConcurrentIncrement() throws InterruptedException {
            final int THREADS    = 10;
            final int INCREMENTS = 1000;

            AtomicInteger counter = new AtomicInteger(0);
            ExecutorService executor = Executors.newFixedThreadPool(THREADS);

            // Submit THREADS tasks, each incrementing INCREMENTS times
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                futures.add(executor.submit(() -> {
                    for (int i = 0; i < INCREMENTS; i++) {
                        counter.incrementAndGet();
                    }
                }));
            }

            // Wait for all tasks to complete
            for (Future<?> f : futures) {
                try { f.get(); } catch (ExecutionException e) { fail(e); }
            }

            executor.shutdown();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

            // Every increment must be counted — no lost updates
            assertEquals(THREADS * INCREMENTS, counter.get(),
                "AtomicInteger must count all increments across all threads");
        }

        /**
         * Multiple threads insert entries into a ConcurrentHashMap simultaneously.
         * ConcurrentHashMap allows safe concurrent puts without external locking.
         */
        @Test
        @DisplayName("ConcurrentHashMap: concurrent puts from multiple threads — final count correct")
        void concurrentHashMapConcurrentPuts() throws InterruptedException {
            final int THREADS = 8;
            final int ENTRIES_PER_THREAD = 100;

            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
            ExecutorService executor = Executors.newFixedThreadPool(THREADS);
            CountDownLatch latch = new CountDownLatch(THREADS);

            for (int t = 0; t < THREADS; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    for (int i = 0; i < ENTRIES_PER_THREAD; i++) {
                        // Each thread inserts keys like "thread-0-item-0", "thread-0-item-1", ...
                        map.put("thread-" + threadId + "-item-" + i, i);
                    }
                    latch.countDown();
                });
            }

            // Wait for all threads
            assertTrue(latch.await(10, TimeUnit.SECONDS),
                "All threads should complete within 10 seconds");

            executor.shutdown();

            // All entries must be present
            assertEquals(THREADS * ENTRIES_PER_THREAD, map.size(),
                "All puts from all threads must be in the map");
        }

        /**
         * CompletableFuture.supplyAsync runs work on a thread pool
         * and returns a future that completes with the supplier's return value.
         */
        @Test
        @DisplayName("CompletableFuture.supplyAsync: completes and returns correct value")
        void completableFutureSupplyAsync() throws Exception {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                // Simulate some async work
                return "Hello from async";
            });

            // join() blocks until the future completes
            String result = future.get(5, TimeUnit.SECONDS);
            assertEquals("Hello from async", result);
            assertTrue(future.isDone());
            assertFalse(future.isCompletedExceptionally());
        }

        /**
         * thenApply transforms the result of a CompletableFuture,
         * producing a new future of the transformed type.
         */
        @Test
        @DisplayName("CompletableFuture.thenApply: transforms value correctly through chain")
        void completableFutureThenApply() throws Exception {
            CompletableFuture<Integer> future = CompletableFuture
                .supplyAsync(() -> 5)           // produces 5
                .thenApply(n -> n * n)          // squares it → 25
                .thenApply(n -> n + 1);         // adds 1 → 26

            assertEquals(26, future.get(5, TimeUnit.SECONDS));
        }

        /**
         * CompletableFuture.allOf waits for ALL given futures to complete.
         * The returned future completes when every input future has finished.
         */
        @Test
        @DisplayName("CompletableFuture.allOf: all futures complete before allOf completes")
        void completableFutureAllOf() throws Exception {
            List<CompletableFuture<Integer>> futures = new ArrayList<>();
            List<Integer> results = new CopyOnWriteArrayList<>();

            for (int i = 1; i <= 5; i++) {
                final int val = i;
                futures.add(CompletableFuture.supplyAsync(() -> {
                    results.add(val * 10); // 10, 20, 30, 40, 50 in some order
                    return val * 10;
                }));
            }

            // allOf returns Void — we must get individual results separately
            CompletableFuture<Void> allDone = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );

            allDone.get(5, TimeUnit.SECONDS); // waits for all

            // All 5 futures must have completed
            assertEquals(5, results.size());

            // Sum must be 10+20+30+40+50 = 150
            int sum = results.stream().mapToInt(Integer::intValue).sum();
            assertEquals(150, sum);
        }

        /**
         * CompletableFuture.exceptionally provides a fallback value when
         * the future completes exceptionally (with a thrown exception).
         */
        @Test
        @DisplayName("CompletableFuture.exceptionally: error caught and default value returned")
        void completableFutureExceptionally() throws Exception {
            CompletableFuture<String> future = CompletableFuture
                .<String>supplyAsync(() -> {
                    // This simulates a task that fails
                    throw new RuntimeException("Something went wrong");
                })
                .exceptionally(ex -> {
                    // Provide a fallback — the exception is available if we need it
                    return "default-on-error";
                });

            String result = future.get(5, TimeUnit.SECONDS);
            assertEquals("default-on-error", result,
                "exceptionally should return the fallback when the future fails");

            // The future itself is considered "done" (not exceptionally, because
            // exceptionally handled it and produced a normal result)
            assertTrue(future.isDone());
            assertFalse(future.isCompletedExceptionally());
        }
    }
}
