/**
 * Exercise01_GenericDataStructures.java
 *
 * EXERCISE 01: Generic Data Structures
 *
 * Build two generic data structures from scratch:
 *   Part A: Generic Binary Search Tree (BST)
 *   Part B: Generic Bounded Cache (LRU eviction)
 *
 * Both parts have skeleton code. Complete all methods marked with:
 *   // TODO: Implement this
 *
 * HINTS: See theory/01-generics.md for guidance on bounded types and wildcards.
 * SOLUTION: See solutions/Exercise01_Solution.java
 */

import java.util.*;
import java.util.function.Consumer;

public class Exercise01_GenericDataStructures {

    // ==========================================================================
    // PART A: Generic Binary Search Tree
    // ==========================================================================

    /**
     * A generic Binary Search Tree.
     *
     * <T extends Comparable<T>> means T must be comparable to itself.
     * This allows us to use compareTo() to decide left/right placement.
     *
     * Operations to implement:
     *   - insert(T value)    — add a value to the BST
     *   - contains(T value)  — check if a value exists
     *   - inOrder(Consumer)  — traverse in sorted order (left → root → right)
     *   - size()             — number of elements
     *   - min()              — smallest value
     *   - max()              — largest value
     */
    static class BinarySearchTree<T extends Comparable<T>> {

        private Node<T> root;
        private int size;

        private static class Node<T> {
            T value;
            Node<T> left;
            Node<T> right;

            Node(T value) {
                this.value = value;
            }
        }

        /**
         * Insert a value into the BST.
         * - If the value is less than current node → go left
         * - If the value is greater → go right
         * - If equal → don't insert (BST stores unique values)
         */
        public void insert(T value) {
            // TODO: Implement this
            // Hint: Create a private recursive helper method insert(Node<T> node, T value)
            // that returns the updated node. Root becomes insert(root, value).
        }

        /**
         * Check if a value exists in the BST.
         * @return true if the value is found
         */
        public boolean contains(T value) {
            // TODO: Implement this
            // Hint: Similar recursive approach to insert
            return false;
        }

        /**
         * Traverse the BST in-order (sorted ascending) and call action for each value.
         * In-order = left subtree, then root, then right subtree.
         */
        public void inOrder(Consumer<T> action) {
            // TODO: Implement this
        }

        /**
         * @return the minimum value in the BST
         * @throws NoSuchElementException if the tree is empty
         */
        public T min() {
            // TODO: Implement this
            // Hint: The minimum is the leftmost node
            throw new NoSuchElementException("Tree is empty");
        }

        /**
         * @return the maximum value in the BST
         * @throws NoSuchElementException if the tree is empty
         */
        public T max() {
            // TODO: Implement this
            throw new NoSuchElementException("Tree is empty");
        }

        public int size() { return size; }
        public boolean isEmpty() { return size == 0; }
    }

    // ==========================================================================
    // PART B: Generic Bounded Cache (LRU Eviction)
    // ==========================================================================

    /**
     * A generic LRU (Least Recently Used) cache with a maximum size.
     *
     * When the cache is full and a new entry is added, the LEAST RECENTLY USED
     * entry is evicted (removed).
     *
     * "Recently used" means: accessed (read) OR written.
     *
     * Operations to implement:
     *   - put(K key, V value) — add or update an entry
     *   - get(K key)          — retrieve an entry (returns Optional<V>)
     *   - evict(K key)        — remove an entry explicitly
     *   - size()              — current number of entries
     *   - containsKey(K key)  — check if key exists
     *
     * HINT: LinkedHashMap with accessOrder=true does most of the heavy lifting.
     *   new LinkedHashMap<K, V>(capacity, 0.75f, true) creates an access-ordered map
     *   where iteration order = least recently accessed to most recently accessed.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    static class LRUCache<K, V> {
        private final int capacity;
        private final Map<K, V> cache;

        public LRUCache(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");
            this.capacity = capacity;
            // TODO: Initialize cache as a LinkedHashMap with accessOrder=true
            // Hint: Override removeEldestEntry to evict when size > capacity
            this.cache = null; // Replace this
        }

        /**
         * Add or update a key-value pair.
         * If adding causes the cache to exceed capacity, the LRU entry is evicted.
         */
        public void put(K key, V value) {
            // TODO: Implement this
        }

        /**
         * Retrieve the value for the given key.
         * Accessing a key marks it as recently used.
         *
         * @return Optional.of(value) if found, Optional.empty() if not
         */
        public Optional<V> get(K key) {
            // TODO: Implement this
            return Optional.empty();
        }

        /**
         * Explicitly remove an entry from the cache.
         */
        public void evict(K key) {
            // TODO: Implement this
        }

        public int size() { return cache == null ? 0 : cache.size(); }
        public boolean containsKey(K key) { return cache != null && cache.containsKey(key); }

        @Override
        public String toString() {
            return "LRUCache" + (cache != null ? cache.toString() : "{}");
        }
    }

    // ==========================================================================
    // TESTS — run these to check your implementations
    // ==========================================================================

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println("  Exercise 01: Generic Data Structures");
        System.out.println("====================================\n");

        testBST();
        testLRUCache();
    }

    static void testBST() {
        System.out.println("--- Testing BinarySearchTree ---");
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();

        // Insert values
        int[] values = {5, 3, 7, 1, 4, 6, 8, 2};
        for (int v : values) tree.insert(v);

        System.out.println("Size: " + tree.size()); // Expected: 8

        // In-order traversal should print sorted: 1 2 3 4 5 6 7 8
        System.out.print("In-order: ");
        tree.inOrder(v -> System.out.print(v + " "));
        System.out.println();

        // Contains
        System.out.println("Contains 4: " + tree.contains(4));  // true
        System.out.println("Contains 9: " + tree.contains(9));  // false

        // Min and Max
        System.out.println("Min: " + tree.min());  // 1
        System.out.println("Max: " + tree.max());  // 8

        // Test with Strings
        BinarySearchTree<String> strTree = new BinarySearchTree<>();
        strTree.insert("banana");
        strTree.insert("apple");
        strTree.insert("cherry");
        strTree.insert("date");
        System.out.print("Strings in order: ");
        strTree.inOrder(s -> System.out.print(s + " "));
        System.out.println();

        System.out.println();
    }

    static void testLRUCache() {
        System.out.println("--- Testing LRUCache ---");

        LRUCache<String, Integer> cache = new LRUCache<>(3);

        // Fill the cache
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        System.out.println("After adding a,b,c: " + cache);  // {a=1, b=2, c=3}
        System.out.println("Size: " + cache.size());  // 3

        // Access "a" — now "a" is most recently used, "b" is LRU
        cache.get("a");

        // Add "d" — "b" should be evicted (it was LRU after "a" was accessed)
        cache.put("d", 4);
        System.out.println("After accessing a, adding d: " + cache);  // {c=3, a=1, d=4} (b evicted)
        System.out.println("Contains b: " + cache.containsKey("b"));  // false
        System.out.println("Contains a: " + cache.containsKey("a"));  // true

        // Update existing key
        cache.put("a", 100);
        System.out.println("After updating a: " + cache.get("a"));  // Optional[100]

        // Explicit eviction
        cache.evict("d");
        System.out.println("After evicting d: " + cache);
        System.out.println("Size: " + cache.size());  // 2

        System.out.println();
    }
}
