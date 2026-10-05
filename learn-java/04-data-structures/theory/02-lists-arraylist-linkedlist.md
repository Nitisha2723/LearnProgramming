# Lists: ArrayList and LinkedList

## The List Interface

`List<E>` is an ordered collection (also called a "sequence"). Key properties:
- Elements are **indexed** from 0
- **Duplicates are allowed**
- **Insertion order is preserved**

Two main implementations: `ArrayList` and `LinkedList`.

---

## ArrayList

### How It Works Internally

```
ArrayList backing array (capacity = 10 initially):

Index:  [0]    [1]    [2]    [3]    [4]    [5]  ...  [9]
Value: "Alice" "Bob" "Char"  null   null   null       null

size = 3, capacity = 10
```

`ArrayList` wraps a regular Java array. When you add elements:
1. If `size < capacity`: just assign `array[size] = element; size++`
2. If `size == capacity`: **resize** — create a new array ~1.5x larger, copy all elements, then add

### Performance Characteristics

| Operation                        | Time Complexity | Why                                          |
|----------------------------------|-----------------|----------------------------------------------|
| `get(int index)`                 | O(1)            | Direct array access by index                 |
| `set(int index, E element)`      | O(1)            | Direct array assignment                      |
| `add(E element)` (to end)        | O(1) amortized  | Append to end; resize rarely                 |
| `add(int index, E element)`      | O(n)            | Must shift all elements right of index       |
| `remove(int index)`              | O(n)            | Must shift all elements left of index        |
| `remove(Object o)`               | O(n)            | Linear search + shift                        |
| `contains(Object o)`             | O(n)            | Linear search                                |
| `size()`                         | O(1)            | Stored as a field                            |
| `sort()`                         | O(n log n)      | TimSort algorithm                            |

### When to Use ArrayList

- You need fast **random access** by index (e.g., `list.get(42)`)
- You add elements mostly at the **end** of the list
- You need to **iterate** through all elements
- You don't need frequent insertions/deletions in the **middle**

**Rule of thumb: 95% of the time, use ArrayList.**

---

## LinkedList

### How It Works Internally

```
LinkedList (doubly-linked):

head ──► [prev=null | "Alice" | next] ──► [prev | "Bob" | next] ──► [prev | "Charlie" | next=null] ◄── tail

Each node: prev pointer + data + next pointer
```

Each element is a `Node` object containing the element and references to the previous and next nodes.

### Performance Characteristics

| Operation                        | Time Complexity | Why                                              |
|----------------------------------|-----------------|--------------------------------------------------|
| `get(int index)`                 | O(n)            | Must traverse from head (or tail)                |
| `set(int index, E element)`      | O(n)            | Must traverse to find index                      |
| `add(E element)` (to end)        | O(1)            | Direct access to tail node                       |
| `add(int index, E element)`      | O(n)            | Must traverse to index, then insert              |
| `addFirst(E element)`            | O(1)            | Direct head manipulation                         |
| `addLast(E element)`             | O(1)            | Direct tail manipulation                         |
| `remove(int index)`              | O(n)            | Must traverse to find node                       |
| `removeFirst()`                  | O(1)            | Direct head manipulation                         |
| `removeLast()`                   | O(1)            | Direct tail manipulation                         |
| `contains(Object o)`             | O(n)            | Linear search                                    |
| Iterating all elements           | O(n)            | Follow next pointers                             |

### When to Use LinkedList

LinkedList is useful when:
- You need **O(1) insert/remove at both ends** (use as Deque)
- You hold a `ListIterator` and need to insert/remove at the **current position**
- You're implementing a queue, deque, or history structure

**Important:** `LinkedList` implements both `List` and `Deque`. When you want it as a deque (double-ended queue), use `ArrayDeque` instead — it's faster in practice.

---

## ArrayList vs LinkedList: Side-by-Side Comparison

```
                    ArrayList          LinkedList
                    ─────────          ──────────
Random access:      O(1)  ✓            O(n)  ✗
Add to end:         O(1)* ✓            O(1)  ✓
Add to start:       O(n)  ✗            O(1)  ✓
Add to middle:      O(n)              O(n) (but faster if you have iterator)
Remove from end:    O(1)  ✓            O(1)  ✓
Remove from start:  O(n)  ✗            O(1)  ✓
Remove from middle: O(n)              O(n)
Memory per element: ~4 bytes           ~24 bytes (node overhead!)
Cache friendly:     YES ✓              No — pointers scattered in memory
* amortized
```

**The memory and cache point is significant.** Modern CPUs are fast at traversing contiguous memory (ArrayList's backing array). LinkedList's pointers scattered across the heap cause cache misses. In practice, `ArrayList` often **outperforms** `LinkedList` even for operations where LinkedList has better theoretical complexity.

---

## Common List Operations

### Creating Lists

```java
// Mutable ArrayList
List<String> names = new ArrayList<>();
List<String> names = new ArrayList<>(Arrays.asList("Alice", "Bob", "Charlie"));
List<String> names = new ArrayList<>(50);  // Initial capacity hint (avoids resizing)

// Immutable (Java 9+)
List<String> names = List.of("Alice", "Bob", "Charlie");

// From array
String[] arr = {"Alice", "Bob"};
List<String> list = new ArrayList<>(Arrays.asList(arr));
```

### Adding Elements

```java
list.add("Dave");           // Add to end
list.add(1, "Eve");         // Add at index 1, shifts others right
list.addAll(otherList);     // Add all elements of another collection
list.addAll(1, otherList);  // Add all at index
```

### Accessing Elements

```java
String first = list.get(0);          // Get by index
int idx = list.indexOf("Alice");     // First occurrence (-1 if not found)
int last = list.lastIndexOf("Alice");// Last occurrence
boolean has = list.contains("Bob");  // Does it contain this element?
int sz = list.size();                // Number of elements
boolean empty = list.isEmpty();      // Size == 0?
```

### Removing Elements

```java
list.remove(0);             // Remove by index — returns removed element
list.remove("Alice");       // Remove by value — returns true/false
list.removeAll(otherList);  // Remove all elements that are in otherList
list.retainAll(otherList);  // Keep only elements that are in otherList
list.clear();               // Remove everything
```

### Searching and Sorting

```java
// Natural order sort (must implement Comparable)
Collections.sort(list);
list.sort(null);  // Same effect

// Custom comparator sort
list.sort(Comparator.naturalOrder());
list.sort(Comparator.reverseOrder());
list.sort(Comparator.comparing(Person::getName));
list.sort(Comparator.comparing(Person::getName).thenComparing(Person::getAge));

// Binary search (list must be sorted first!)
int idx = Collections.binarySearch(list, "Charlie");  // O(log n)
```

### Sublists and Views

```java
// subList returns a VIEW — changes affect the original list!
List<String> sub = list.subList(1, 3);  // Elements at index 1 and 2 (not 3)

// To get an independent copy:
List<String> copy = new ArrayList<>(list.subList(1, 3));

// Copy a list
List<String> copy = new ArrayList<>(original);
List<String> copy = List.copyOf(original);  // Immutable copy (Java 10+)
```

### Conversion

```java
// List to array
String[] arr = list.toArray(new String[0]);
Object[] arr = list.toArray();

// Array to List (fixed-size, backed by array)
List<String> fromArr = Arrays.asList("a", "b", "c");
// NOTE: asList returns a FIXED-SIZE list — can't add/remove, only set()

// To get a properly mutable list from array:
List<String> mutable = new ArrayList<>(Arrays.asList("a", "b", "c"));
```

---

## The Collections Utility Class (List-Specific)

```java
Collections.sort(list);                 // Sort
Collections.reverse(list);              // Reverse
Collections.shuffle(list);              // Random order
Collections.shuffle(list, new Random(42)); // Deterministic shuffle with seed
Collections.swap(list, i, j);           // Swap elements at two indices
Collections.fill(list, "default");      // Replace all with a value
Collections.copy(dest, src);            // Copy src into dest (dest must be large enough)
Collections.nCopies(5, "hello");        // Returns immutable list of ["hello","hello",...] x5
Collections.frequency(list, "Alice");   // Count occurrences of "Alice"
Collections.disjoint(list1, list2);     // True if no elements in common
Collections.min(list);                  // Natural minimum
Collections.max(list);                  // Natural maximum
Collections.min(list, comparator);      // Custom minimum
Collections.unmodifiableList(list);     // Read-only view
```

---

## Practical Example: Working With a List of Students

```java
import java.util.*;
import java.util.stream.Collectors;

public class StudentListExample {

    record Student(String name, int grade) {}

    public static void main(String[] args) {
        List<Student> students = new ArrayList<>(List.of(
            new Student("Alice", 92),
            new Student("Bob", 85),
            new Student("Charlie", 78),
            new Student("Dave", 95),
            new Student("Eve", 88)
        ));

        // Sort by grade descending
        students.sort(Comparator.comparingInt(Student::grade).reversed());

        // Print top 3
        System.out.println("Top 3 students:");
        students.subList(0, Math.min(3, students.size()))
                .forEach(s -> System.out.println(s.name() + ": " + s.grade()));

        // Filter passing students (grade >= 80)
        List<Student> passing = students.stream()
            .filter(s -> s.grade() >= 80)
            .collect(Collectors.toList());

        System.out.println("\nPassing: " + passing.size() + " students");

        // Find a student
        int idx = students.indexOf(new Student("Alice", 92));
        // Note: indexOf uses equals() — works with records (auto-generated equals)
    }
}
```

---

## Key Takeaways

1. **Use `ArrayList` by default** — it's correct 95% of the time
2. **ArrayList has O(1) random access** — ideal for get(index)
3. **LinkedList has O(1) at-end operations** — but ArrayDeque is better for queue/stack use
4. **Both have O(n) middle insertions** — if you need lots of these, reconsider your design
5. **Memory matters** — LinkedList uses ~3x more memory per element
6. **Pre-size ArrayList** if you know approximate size: `new ArrayList<>(1000)`
7. **`Arrays.asList()` returns a fixed-size list** — wrap with `new ArrayList<>()` to make it truly mutable
