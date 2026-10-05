/**
 * Exercise01_Solution.java
 *
 * SOLUTION: Generic Data Structures
 *
 * This file provides complete, correct implementations of:
 *   Part A: Generic Binary Search Tree (BST)
 *   Part B: Generic LRU Cache using LinkedHashMap
 *
 * KEY CONCEPTS DEMONSTRATED:
 *   - Bounded type parameters: <T extends Comparable<T>>
 *   - Recursive data structures and algorithms
 *   - Anonymous class override of removeEldestEntry
 *   - Optional<T> for safe value retrieval
 *   - Consumer<T> functional interface for traversal callbacks
 */

import java.util.*;
import java.util.function.Consumer;

public class Exercise01_Solution {

    // ==========================================================================
    // PART A: Generic Binary Search Tree
    // ==========================================================================

    /**
     * A generic Binary Search Tree that stores unique, comparable values.
     *
     * The bound <T extends Comparable<T>> means: T must implement Comparable<T>,
     * which gives us the compareTo() method needed for ordering decisions.
     *
     * BST PROPERTY: For every node N:
     *   - All values in the left subtree are less than N.value
     *   - All values in the right subtree are greater than N.value
     *
     * Time complexity (balanced tree): O(log n) for insert/contains/min/max
     * Time complexity (worst case - sorted input): O(n) - degrades to linked list
     */
    static class BinarySearchTree<T extends Comparable<T>> {

        private Node<T> root;
        private int size;

        // ---- Inner node class ----

        /**
         * Each node holds a value and references to left and right children.
         * This is a private implementation detail — callers never see Node<T>.
         */
        private static class Node<T> {
            T value;
            Node<T> left;
            Node<T> right;

            Node(T value) {
                this.value = value;
            }
        }

        // ---- Public API ----

        /**
         * Insert a value into the BST.
         *
         * Delegates to the private recursive helper that returns the updated
         * subtree root. This "return the node" pattern elegantly handles:
         *   - Inserting into an empty tree (returns a new node)
         *   - Attaching to left/right subtree (recursive case)
         *   - Duplicate detection (returns node unchanged)
         *
         * @param value the value to insert (ignored if already present)
         */
        public void insert(T value) {
            // The recursive helper returns the (possibly new) subtree root.
            // On first call, root is null → a new Node is created and returned.
            root = insert(root, value);
        }

        /**
         * Check if a value exists in the BST.
         *
         * @param value the value to search for
         * @return true if the value is present, false otherwise
         */
        public boolean contains(T value) {
            return contains(root, value);
        }

        /**
         * Traverse the BST in sorted (ascending) order and invoke action for each value.
         *
         * In-order traversal visits: left subtree → current node → right subtree.
         * Because of the BST property, this naturally produces sorted output.
         *
         * @param action called once per element, in ascending order
         */
        public void inOrder(Consumer<T> action) {
            inOrder(root, action);
        }

        /**
         * Return the minimum (leftmost) value in the tree.
         *
         * @return smallest value
         * @throws NoSuchElementException if the tree is empty
         */
        public T min() {
            if (isEmpty()) throw new NoSuchElementException("Tree is empty");
            // Walk left as far as possible — that's the minimum
            Node<T> current = root;
            while (current.left != null) {
                current = current.left;
            }
            return current.value;
        }

        /**
         * Return the maximum (rightmost) value in the tree.
         *
         * @return largest value
         * @throws NoSuchElementException if the tree is empty
         */
        public T max() {
            if (isEmpty()) throw new NoSuchElementException("Tree is empty");
            // Walk right as far as possible — that's the maximum
            Node<T> current = root;
            while (current.right != null) {
                current = current.right;
            }
            return current.value;
        }

        public int size()       { return size; }
        public boolean isEmpty(){ return size == 0; }

        // ---- Private recursive helpers ----

        /**
         * Recursive insert helper.
         *
         * Pattern: the method RETURNS the subtree rooted at 'node' after insertion.
         *   - Base case: node == null → create and return a new Node (size++)
         *   - Recurse left if value < node.value
         *   - Recurse right if value > node.value
         *   - Do nothing (return node unchanged) if value == node.value (duplicate)
         *
         * @param node  current subtree root (may be null)
         * @param value value to insert
         * @return      the updated subtree root
         */
        private Node<T> insert(Node<T> node, T value) {
            if (node == null) {
                // Found the insertion point — create a new leaf node
                size++;
                return new Node<>(value);
            }

            int cmp = value.compareTo(node.value);

            if (cmp < 0) {
                // value is smaller — insert into left subtree
                node.left = insert(node.left, value);
            } else if (cmp > 0) {
                // value is larger — insert into right subtree
                node.right = insert(node.right, value);
            }
            // cmp == 0 means duplicate — BST stores unique values, so do nothing

            return node; // Return the (unchanged) current node
        }

        /**
         * Recursive contains helper.
         *
         * @param node  current subtree root
         * @param value value to find
         * @return true if value is in this subtree
         */
        private boolean contains(Node<T> node, T value) {
            if (node == null) return false; // Fell off the tree — not found

            int cmp = value.compareTo(node.value);

            if (cmp < 0)      return contains(node.left, value);   // go left
            else if (cmp > 0) return contains(node.right, value);  // go right
            else              return true;                          // found it!
        }

        /**
         * Recursive in-order traversal helper.
         *
         * Visit order: left → current → right
         * This visits nodes in ascending value order due to the BST property.
         *
         * @param node   current subtree root (may be null — base case)
         * @param action callback to invoke for each value
         */
        private void inOrder(Node<T> node, Consumer<T> action) {
            if (node == null) return; // base case: empty subtree

            inOrder(node.left, action);   // 1. Visit all left (smaller) values
            action.accept(node.value);    // 2. Visit current node
            inOrder(node.right, action);  // 3. Visit all right (larger) values
        }
    }

    // ==========================================================================
    // PART B: Generic LRU Cache
    // ==========================================================================

    /**
     * A bounded LRU (Least Recently Used) cache.
     *
     * HOW IT WORKS:
     *   LinkedHashMap normally maintains insertion order. When constructed with
     *   accessOrder=true, it instead maintains ACCESS order — get() moves the
     *   accessed entry to the end of the internal linked list, so the front is
     *   always the Least Recently Used (LRU) entry.
     *
     *   By overriding removeEldestEntry() to return true when size > capacity,
     *   we automatically evict the LRU entry whenever a new entry pushes us over.
     *
     * TIME COMPLEXITY: O(1) for put, get, evict (LinkedHashMap uses a hash table)
     * SPACE COMPLEXITY: O(capacity)
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    static class LRUCache<K, V> {
        private final int capacity;
        private final Map<K, V> cache; // LinkedHashMap with access-order

        /**
         * Create a new LRU cache with the given maximum size.
         *
         * @param capacity maximum number of entries (must be > 0)
         */
        public LRUCache(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");
            this.capacity = capacity;

            // LinkedHashMap constructor: initialCapacity, loadFactor, accessOrder
            //   - accessOrder=true → iteration order = least-recently-accessed first
            // We use an anonymous subclass to override removeEldestEntry:
            //   That method is called after every put() — if it returns true,
            //   the eldest (LRU) entry is automatically removed.
            this.cache = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    // Remove the oldest entry when we're over capacity
                    return size() > LRUCache.this.capacity;
                }
            };
        }

        /**
         * Add or update a key-value pair.
         * If adding a new entry causes the cache to exceed capacity,
         * the least recently used entry is automatically evicted.
         *
         * @param key   cache key
         * @param value value to store
         */
        public void put(K key, V value) {
            cache.put(key, value);
        }

        /**
         * Retrieve the value for a key, marking it as recently used.
         *
         * Because accessOrder=true, calling get() moves the entry to the
         * end of the linked list (most recently used position).
         *
         * @param key the key to look up
         * @return Optional.of(value) if key exists, Optional.empty() otherwise
         */
        public Optional<V> get(K key) {
            // map.get() returns null if key is absent — Optional wraps that gracefully
            return Optional.ofNullable(cache.get(key));
        }

        /**
         * Explicitly remove an entry from the cache.
         *
         * @param key the key to remove
         */
        public void evict(K key) {
            cache.remove(key);
        }

        public int size()                    { return cache.size(); }
        public boolean containsKey(K key)   { return cache.containsKey(key); }

        @Override
        public String toString() {
            return "LRUCache" + cache.toString();
        }
    }

    // ==========================================================================
    // TESTS — same as exercise, but now they should produce correct output
    // ==========================================================================

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println("  Exercise 01 SOLUTION: Generic Data Structures");
        System.out.println("====================================\n");

        testBST();
        testLRUCache();
    }

    static void testBST() {
        System.out.println("--- Testing BinarySearchTree ---");
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();

        // Insert values in a non-sorted order to build a balanced-ish tree:
        //          5
        //        /   \
        //       3     7
        //      / \   / \
        //     1   4 6   8
        //      \
        //       2
        int[] values = {5, 3, 7, 1, 4, 6, 8, 2};
        for (int v : values) tree.insert(v);

        System.out.println("Size: " + tree.size());            // Expected: 8

        // In-order traversal visits left → root → right recursively,
        // which for a BST always produces sorted (ascending) output.
        System.out.print("In-order: ");
        tree.inOrder(v -> System.out.print(v + " "));          // Expected: 1 2 3 4 5 6 7 8
        System.out.println();

        System.out.println("Contains 4: " + tree.contains(4)); // true
        System.out.println("Contains 9: " + tree.contains(9)); // false

        System.out.println("Min: " + tree.min());              // 1
        System.out.println("Max: " + tree.max());              // 8

        // BST works for any Comparable — Strings compare lexicographically
        BinarySearchTree<String> strTree = new BinarySearchTree<>();
        strTree.insert("banana");
        strTree.insert("apple");
        strTree.insert("cherry");
        strTree.insert("date");
        System.out.print("Strings in order: ");
        strTree.inOrder(s -> System.out.print(s + " ")); // Expected: apple banana cherry date
        System.out.println();

        // Edge case: duplicate insertion should NOT increase size
        tree.insert(5); // already there
        System.out.println("Size after duplicate insert of 5: " + tree.size()); // Still 8

        System.out.println();
    }

    static void testLRUCache() {
        System.out.println("--- Testing LRUCache ---");

        LRUCache<String, Integer> cache = new LRUCache<>(3);

        // Fill the cache
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        System.out.println("After adding a,b,c: " + cache);   // {a=1, b=2, c=3}
        System.out.println("Size: " + cache.size());          // 3

        // Access "a" — now "a" is most recently used.
        // Access order from LRU to MRU: b, c, a
        cache.get("a");

        // Add "d" — cache is full, so LRU ("b") gets evicted automatically
        cache.put("d", 4);
        System.out.println("After accessing a, adding d: " + cache); // {c=3, a=1, d=4}
        System.out.println("Contains b: " + cache.containsKey("b")); // false — evicted
        System.out.println("Contains a: " + cache.containsKey("a")); // true

        // Update an existing key — just changes its value and marks it as recently used
        cache.put("a", 100);
        System.out.println("After updating a: " + cache.get("a")); // Optional[100]

        // Explicit eviction
        cache.evict("d");
        System.out.println("After evicting d: " + cache);
        System.out.println("Size: " + cache.size());          // 2

        // Getting a missing key returns Optional.empty(), not null — safe to use
        System.out.println("Get missing key: " + cache.get("z")); // Optional.empty

        System.out.println();
    }
}
