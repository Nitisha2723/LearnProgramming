import java.util.HashMap;
import java.util.Map;

/**
 * PROBLEM: LRU Cache
 * Design a data structure that follows the Least Recently Used (LRU) cache eviction policy.
 *
 * Implement LRUCache class:
 *   - LRUCache(int capacity): Initialize cache with positive capacity.
 *   - int get(int key): Return the value of the key, or -1 if not exists.
 *   - void put(int key, int value): Update or insert key-value.
 *     If the number of keys exceeds capacity, evict the LRU key.
 *
 * CONSTRAINTS:
 *   - Both get and put must run in O(1) time.
 *
 * DATA STRUCTURE: HashMap + Doubly Linked List
 *
 * WHY THIS COMBINATION?
 *   - HashMap: O(1) lookup of any key → node
 *   - Doubly Linked List: O(1) insertion and removal anywhere in the list
 *     (given a pointer to the node — which HashMap provides)
 *
 * DESIGN:
 *   - The linked list is ordered by recency: head (most recent) ← → tail (least recent)
 *   - HashMap: key → Node (for O(1) access to any node)
 *   - On get(key): move the accessed node to the head (most recently used)
 *   - On put(key):
 *     - If key exists: update value, move to head
 *     - If key is new: create node, add to head
 *       - If over capacity: remove tail node AND remove from HashMap
 *   - Use dummy head and tail nodes to simplify boundary conditions
 *     (no null pointer checks needed for empty list operations)
 *
 * EXAMPLE (capacity = 2):
 *   put(1, 1) → [1:1]           head ↔ [1] ↔ tail
 *   put(2, 2) → [1:1, 2:2]     head ↔ [2] ↔ [1] ↔ tail  (2 is most recent)
 *   get(1)   → returns 1        head ↔ [1] ↔ [2] ↔ tail  (1 moved to front)
 *   put(3, 3) → evict LRU=2    head ↔ [3] ↔ [1] ↔ tail  (2 removed, 3 added)
 *   get(2)   → returns -1 (evicted)
 *   get(3)   → returns 3
 *   put(4, 4) → evict LRU=1    head ↔ [4] ↔ [3] ↔ tail
 *   get(1)   → returns -1 (evicted)
 *   get(3)   → returns 3
 *   get(4)   → returns 4
 *
 * TIME:  O(1) for both get and put
 * SPACE: O(capacity)
 */
public class LRUCache {

    // -----------------------------------------------------------------------
    // Inner class: Doubly Linked List Node
    // -----------------------------------------------------------------------
    private static class Node {
        int key;
        int value;
        Node prev;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }

        Node() {} // For dummy head/tail nodes
    }

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------
    private final int capacity;
    private final Map<Integer, Node> map; // key → Node for O(1) lookup

    // Dummy head and tail to avoid null checks at boundaries
    // Layout: head ↔ [most recent] ↔ ... ↔ [least recent] ↔ tail
    private final Node head; // Dummy head (MRU side)
    private final Node tail; // Dummy tail (LRU side)

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>(capacity);

        // Initialize dummy head and tail and link them
        head = new Node();
        tail = new Node();
        head.next = tail;
        tail.prev = head;
    }

    // -----------------------------------------------------------------------
    // get: O(1)
    // -----------------------------------------------------------------------
    /**
     * Returns the value of the key, or -1 if key is not in cache.
     * Accessing a key marks it as recently used (moves to head).
     */
    public int get(int key) {
        Node node = map.get(key);
        if (node == null) return -1;

        // This key was accessed — move it to the head (most recently used)
        moveToHead(node);
        return node.value;
    }

    // -----------------------------------------------------------------------
    // put: O(1)
    // -----------------------------------------------------------------------
    /**
     * Insert or update a key-value pair.
     * If over capacity after insertion, evict the LRU entry (the node at the tail).
     */
    public void put(int key, int value) {
        Node existing = map.get(key);

        if (existing != null) {
            // Key already exists: update value and move to head
            existing.value = value;
            moveToHead(existing);
        } else {
            // New key: create node and add to head
            Node newNode = new Node(key, value);
            map.put(key, newNode);
            addToHead(newNode);

            // Evict LRU if over capacity
            if (map.size() > capacity) {
                Node lruNode = removeTail();
                map.remove(lruNode.key); // Remove from HashMap too!
            }
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers: all O(1) linked list operations
    // -----------------------------------------------------------------------

    /**
     * Add node right after the dummy head (makes it the most recently used).
     *
     * Before: head ↔ A ↔ ...
     * After:  head ↔ node ↔ A ↔ ...
     */
    private void addToHead(Node node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * Remove a node from its current position in the list.
     * Does not remove from HashMap — caller must handle that.
     */
    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /**
     * Move an existing node to the head (mark as most recently used).
     */
    private void moveToHead(Node node) {
        removeNode(node);
        addToHead(node);
    }

    /**
     * Remove and return the node just before the dummy tail (the LRU node).
     *
     * Before: ... ↔ A ↔ tail
     * After:  ... ↔ tail   (A returned)
     */
    private Node removeTail() {
        Node lru = tail.prev; // The actual LRU node (just before dummy tail)
        removeNode(lru);
        return lru;
    }

    // -----------------------------------------------------------------------
    // Debug helper: print current cache state
    // -----------------------------------------------------------------------
    private void printState() {
        StringBuilder sb = new StringBuilder("Cache [head→");
        Node curr = head.next;
        while (curr != tail) {
            sb.append("(").append(curr.key).append(":").append(curr.value).append(")");
            if (curr.next != tail) sb.append("↔");
            curr = curr.next;
        }
        sb.append("←tail]  map keys: ").append(map.keySet());
        System.out.println("  " + sb);
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== LRU Cache ===");
        System.out.println();

        // Test 1: Classic LeetCode example
        System.out.println("--- Test 1: capacity=2 ---");
        LRUCache cache = new LRUCache(2);

        System.out.print("put(1, 1)  → ");
        cache.put(1, 1);
        cache.printState();

        System.out.print("put(2, 2)  → ");
        cache.put(2, 2);
        cache.printState();

        System.out.print("get(1)     → ");
        int r = cache.get(1);
        System.out.println(r + " (expected 1)");
        cache.printState(); // 1 should now be at head

        System.out.print("put(3, 3)  → ");
        cache.put(3, 3); // evicts key 2 (LRU)
        cache.printState();

        System.out.print("get(2)     → ");
        r = cache.get(2);
        System.out.println(r + " (expected -1, evicted)");

        System.out.print("put(4, 4)  → ");
        cache.put(4, 4); // evicts key 1 (LRU)
        cache.printState();

        System.out.print("get(1)     → ");
        r = cache.get(1);
        System.out.println(r + " (expected -1, evicted)");

        System.out.print("get(3)     → ");
        r = cache.get(3);
        System.out.println(r + " (expected 3)");

        System.out.print("get(4)     → ");
        r = cache.get(4);
        System.out.println(r + " (expected 4)");

        System.out.println();

        // Test 2: Update existing key
        System.out.println("--- Test 2: Update existing key ---");
        LRUCache cache2 = new LRUCache(2);
        cache2.put(1, 1);
        cache2.put(2, 2);
        System.out.print("put(1, 10) → update key 1's value: ");
        cache2.put(1, 10);
        cache2.printState();
        System.out.print("get(1)     → ");
        r = cache2.get(1);
        System.out.println(r + " (expected 10)");

        System.out.println();

        // Test 3: capacity=1
        System.out.println("--- Test 3: capacity=1 ---");
        LRUCache cache3 = new LRUCache(1);
        cache3.put(2, 1);
        System.out.print("get(2)     → ");
        r = cache3.get(2);
        System.out.println(r + " (expected 1)");
        cache3.put(3, 2); // evicts 2
        System.out.print("get(2)     → ");
        r = cache3.get(2);
        System.out.println(r + " (expected -1)");
        System.out.print("get(3)     → ");
        r = cache3.get(3);
        System.out.println(r + " (expected 2)");

        System.out.println();
        System.out.println("=== Complexity Summary ===");
        System.out.println("  get(key): O(1) — HashMap lookup + O(1) list move");
        System.out.println("  put(key): O(1) — HashMap insert + O(1) list insert + O(1) eviction");
        System.out.println("  Space:    O(capacity) — stores at most 'capacity' nodes");
        System.out.println();
        System.out.println("Key insight: HashMap gives O(1) access TO the node;");
        System.out.println("  doubly linked list allows O(1) removal FROM any position");
        System.out.println("  (because you have prev and next pointers).");
        System.out.println("  With a singly linked list, removal would require O(n) to find prev.");
    }
}
