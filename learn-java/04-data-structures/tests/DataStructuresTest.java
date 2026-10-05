import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataStructuresTest.java
 *
 * JUnit 5 tests for Module 04: Data Structures
 * Tests cover ArrayList, HashMap, HashSet, ArrayDeque, and PriorityQueue operations.
 *
 * Run with: javac -cp junit-platform-console-standalone.jar DataStructuresTest.java
 *           java  -cp .:junit-platform-console-standalone.jar org.junit.platform.console.ConsoleLauncher --scan-classpath
 */
@DisplayName("Module 04: Data Structures Tests")
class DataStructuresTest {

    // =========================================================
    // ArrayList Tests
    // =========================================================

    @Nested
    @DisplayName("ArrayList Operations")
    class ArrayListTests {

        @Test
        @DisplayName("add() appends and size() reflects correctly")
        void testAddAndSize() {
            List<String> list = new ArrayList<>();
            assertEquals(0, list.size());

            list.add("Alice");
            list.add("Bob");
            list.add("Charlie");

            assertEquals(3, list.size());
            assertEquals("Bob", list.get(1));
        }

        @Test
        @DisplayName("add(index, element) inserts and shifts correctly")
        void testAddAtIndex() {
            List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
            list.add(2, "X");

            assertEquals(List.of("A", "B", "X", "C", "D"), list);
            assertEquals(5, list.size());
        }

        @Test
        @DisplayName("remove(index) removes correct element and shifts")
        void testRemoveByIndex() {
            List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
            String removed = list.remove(1);

            assertEquals("B", removed);
            assertEquals(List.of("A", "C", "D"), list);
        }

        @Test
        @DisplayName("remove(Object) removes first occurrence")
        void testRemoveByValue() {
            List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "B"));
            boolean removed = list.remove("B");

            assertTrue(removed);
            assertEquals(List.of("A", "C", "B"), list);  // Only first "B" removed
        }

        @Test
        @DisplayName("sort() orders elements by Comparator")
        void testSort() {
            List<Integer> numbers = new ArrayList<>(Arrays.asList(5, 3, 1, 4, 2));
            numbers.sort(Comparator.naturalOrder());
            assertEquals(List.of(1, 2, 3, 4, 5), numbers);

            numbers.sort(Comparator.reverseOrder());
            assertEquals(List.of(5, 4, 3, 2, 1), numbers);
        }

        @Test
        @DisplayName("subList() returns correct view")
        void testSubList() {
            List<String> list = List.of("A", "B", "C", "D", "E");
            List<String> sub = list.subList(1, 4);
            assertEquals(List.of("B", "C", "D"), sub);
        }

        @Test
        @DisplayName("removeIf() removes matching elements")
        void testRemoveIf() {
            List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
            numbers.removeIf(n -> n % 2 == 0);
            assertEquals(List.of(1, 3, 5, 7), numbers);
        }

        @Test
        @DisplayName("contains() returns true for existing element")
        void testContains() {
            List<String> list = List.of("Java", "Python", "Go");
            assertTrue(list.contains("Java"));
            assertFalse(list.contains("Rust"));
        }
    }

    // =========================================================
    // HashMap Tests
    // =========================================================

    @Nested
    @DisplayName("HashMap Operations")
    class HashMapTests {

        @Test
        @DisplayName("put() and get() work correctly")
        void testPutAndGet() {
            Map<String, Integer> map = new HashMap<>();
            map.put("Alice", 30);
            map.put("Bob", 25);

            assertEquals(30, map.get("Alice"));
            assertEquals(25, map.get("Bob"));
            assertNull(map.get("Charlie"));
        }

        @Test
        @DisplayName("getOrDefault() returns default for missing key")
        void testGetOrDefault() {
            Map<String, Integer> map = Map.of("Alice", 30);
            assertEquals(30, map.getOrDefault("Alice", -1));
            assertEquals(-1, map.getOrDefault("Bob", -1));
        }

        @Test
        @DisplayName("merge() correctly aggregates values")
        void testMerge() {
            Map<String, Integer> freq = new HashMap<>();
            String[] words = {"apple", "banana", "apple", "cherry", "banana", "apple"};

            for (String word : words) {
                freq.merge(word, 1, Integer::sum);
            }

            assertEquals(3, freq.get("apple"));
            assertEquals(2, freq.get("banana"));
            assertEquals(1, freq.get("cherry"));
        }

        @Test
        @DisplayName("computeIfAbsent() only computes for missing keys")
        void testComputeIfAbsent() {
            Map<String, List<String>> groups = new HashMap<>();

            groups.computeIfAbsent("fruits", k -> new ArrayList<>()).add("apple");
            groups.computeIfAbsent("fruits", k -> new ArrayList<>()).add("banana");
            groups.computeIfAbsent("veggies", k -> new ArrayList<>()).add("carrot");

            assertEquals(List.of("apple", "banana"), groups.get("fruits"));
            assertEquals(List.of("carrot"), groups.get("veggies"));
        }

        @Test
        @DisplayName("put() on existing key updates value")
        void testPutUpdatesExisting() {
            Map<String, Integer> map = new HashMap<>();
            map.put("score", 100);
            map.put("score", 200);
            assertEquals(200, map.get("score"));
            assertEquals(1, map.size());  // Still only one entry
        }

        @Test
        @DisplayName("frequency counter finds most common element")
        void testFrequencyCounter() {
            int[] nums = {1, 3, 2, 1, 4, 1, 2, 3, 1};
            Map<Integer, Integer> freq = new HashMap<>();
            for (int n : nums) freq.merge(n, 1, Integer::sum);

            int mostFrequent = freq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();

            assertEquals(1, mostFrequent);  // 1 appears 4 times
            assertEquals(4, freq.get(1));
        }
    }

    // =========================================================
    // HashSet Tests
    // =========================================================

    @Nested
    @DisplayName("HashSet Operations")
    class HashSetTests {

        @Test
        @DisplayName("add() ignores duplicates")
        void testNoDuplicates() {
            Set<String> set = new HashSet<>();
            set.add("java");
            set.add("python");
            set.add("java");  // Duplicate
            set.add("go");

            assertEquals(3, set.size());
            assertTrue(set.contains("java"));
        }

        @Test
        @DisplayName("Set intersection via retainAll()")
        void testIntersection() {
            Set<Integer> a = new HashSet<>(Set.of(1, 2, 3, 4, 5));
            Set<Integer> b = new HashSet<>(Set.of(3, 4, 5, 6, 7));

            Set<Integer> intersection = new HashSet<>(a);
            intersection.retainAll(b);

            assertEquals(Set.of(3, 4, 5), intersection);
        }

        @Test
        @DisplayName("Set union via addAll()")
        void testUnion() {
            Set<Integer> a = new HashSet<>(Set.of(1, 2, 3));
            Set<Integer> b = new HashSet<>(Set.of(3, 4, 5));

            Set<Integer> union = new HashSet<>(a);
            union.addAll(b);

            assertEquals(Set.of(1, 2, 3, 4, 5), union);
        }

        @Test
        @DisplayName("Set difference via removeAll()")
        void testDifference() {
            Set<Integer> a = new HashSet<>(Set.of(1, 2, 3, 4, 5));
            Set<Integer> b = new HashSet<>(Set.of(3, 4, 5, 6, 7));

            Set<Integer> diff = new HashSet<>(a);
            diff.removeAll(b);

            assertEquals(Set.of(1, 2), diff);
        }

        @Test
        @DisplayName("add() returns false for duplicate element")
        void testAddReturnValue() {
            Set<String> visited = new HashSet<>();
            assertTrue(visited.add("https://example.com"));  // New
            assertFalse(visited.add("https://example.com")); // Duplicate
        }
    }

    // =========================================================
    // ArrayDeque Tests (Stack and Queue)
    // =========================================================

    @Nested
    @DisplayName("ArrayDeque as Stack and Queue")
    class ArrayDequeTests {

        @Test
        @DisplayName("Stack: push/pop is LIFO")
        void testStackLIFO() {
            Deque<String> stack = new ArrayDeque<>();
            stack.push("first");
            stack.push("second");
            stack.push("third");

            assertEquals("third",  stack.pop());   // Last in, first out
            assertEquals("second", stack.pop());
            assertEquals("first",  stack.pop());
            assertTrue(stack.isEmpty());
        }

        @Test
        @DisplayName("Stack: peek() doesn't remove element")
        void testStackPeek() {
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(42);
            assertEquals(42, stack.peek());
            assertEquals(42, stack.peek());  // Still there
            assertEquals(1, stack.size());
        }

        @Test
        @DisplayName("Queue: offer/poll is FIFO")
        void testQueueFIFO() {
            Queue<String> queue = new ArrayDeque<>();
            queue.offer("first");
            queue.offer("second");
            queue.offer("third");

            assertEquals("first",  queue.poll());  // First in, first out
            assertEquals("second", queue.poll());
            assertEquals("third",  queue.poll());
            assertTrue(queue.isEmpty());
        }

        @Test
        @DisplayName("Queue: poll() returns null on empty queue")
        void testQueuePollEmpty() {
            Queue<String> queue = new ArrayDeque<>();
            assertNull(queue.poll());  // Returns null, doesn't throw
        }

        @Test
        @DisplayName("Balanced parentheses validation")
        void testBalancedParentheses() {
            assertTrue(isBalanced("{[()]}"));
            assertTrue(isBalanced("((()))"));
            assertTrue(isBalanced(""));
            assertFalse(isBalanced("{[(])}"));
            assertFalse(isBalanced("((("));
            assertFalse(isBalanced(")"));
        }

        private boolean isBalanced(String s) {
            Deque<Character> stack = new ArrayDeque<>();
            for (char c : s.toCharArray()) {
                if (c == '(' || c == '[' || c == '{') {
                    stack.push(c);
                } else if (c == ')' || c == ']' || c == '}') {
                    if (stack.isEmpty()) return false;
                    char top = stack.pop();
                    if ((c == ')' && top != '(') ||
                        (c == ']' && top != '[') ||
                        (c == '}' && top != '{')) return false;
                }
            }
            return stack.isEmpty();
        }
    }

    // =========================================================
    // PriorityQueue Tests
    // =========================================================

    @Nested
    @DisplayName("PriorityQueue Operations")
    class PriorityQueueTests {

        @Test
        @DisplayName("Default PriorityQueue is a min-heap")
        void testMinHeap() {
            PriorityQueue<Integer> pq = new PriorityQueue<>();
            pq.offer(5);
            pq.offer(1);
            pq.offer(3);
            pq.offer(2);
            pq.offer(4);

            assertEquals(1, pq.poll());
            assertEquals(2, pq.poll());
            assertEquals(3, pq.poll());
        }

        @Test
        @DisplayName("PriorityQueue with reverseOrder is a max-heap")
        void testMaxHeap() {
            PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.reverseOrder());
            pq.offer(3);
            pq.offer(1);
            pq.offer(5);
            pq.offer(2);

            assertEquals(5, pq.poll());
            assertEquals(3, pq.poll());
        }

        @Test
        @DisplayName("Top-K elements via min-heap of size K")
        void testTopK() {
            int[] nums = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3};
            int k = 3;

            PriorityQueue<Integer> minHeap = new PriorityQueue<>();
            for (int n : nums) {
                minHeap.offer(n);
                if (minHeap.size() > k) minHeap.poll();
            }

            // The heap contains the top-k elements; drain them in sorted order
            List<Integer> topK = new ArrayList<>();
            while (!minHeap.isEmpty()) topK.add(minHeap.poll());
            Collections.sort(topK, Collections.reverseOrder());

            // Top 3 unique elements should be 9, 6, 5
            assertTrue(topK.contains(9));
            assertTrue(topK.contains(6));
        }
    }

    // =========================================================
    // TreeSet / TreeMap Tests
    // =========================================================

    @Nested
    @DisplayName("TreeSet and TreeMap Sorted Order")
    class TreeTests {

        @Test
        @DisplayName("TreeSet maintains natural sort order")
        void testTreeSetSorted() {
            TreeSet<Integer> set = new TreeSet<>(Arrays.asList(5, 3, 1, 4, 2));
            assertEquals(1, set.first());
            assertEquals(5, set.last());

            List<Integer> inOrder = new ArrayList<>(set);
            assertEquals(List.of(1, 2, 3, 4, 5), inOrder);
        }

        @Test
        @DisplayName("TreeMap headMap() returns entries with keys strictly less than")
        void testTreeMapHeadMap() {
            TreeMap<String, Integer> map = new TreeMap<>();
            map.put("apple", 1);
            map.put("banana", 2);
            map.put("cherry", 3);
            map.put("date", 4);

            Map<String, Integer> head = map.headMap("cherry");
            assertEquals(Set.of("apple", "banana"), head.keySet());
        }
    }

    // =========================================================
    // LinkedHashMap Insertion Order Test
    // =========================================================

    @Test
    @DisplayName("LinkedHashMap preserves insertion order")
    void testLinkedHashMapOrder() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Zebra", 1);
        map.put("Apple", 2);
        map.put("Mango", 3);

        List<String> keys = new ArrayList<>(map.keySet());
        assertEquals(List.of("Zebra", "Apple", "Mango"), keys);
        // Regular HashMap would not guarantee this order
    }
}
