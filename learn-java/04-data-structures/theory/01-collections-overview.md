# Java Collections Framework Overview

## What Is the Collections Framework?

The Java Collections Framework (JCF) is a unified architecture for representing and manipulating groups of objects. It provides:

- **Interfaces** that define what data structures can do
- **Implementations** (classes) that provide the actual storage
- **Algorithms** (utility methods) via `Collections` and `Arrays`

Before JCF (pre-Java 2), Java had `Vector`, `Hashtable`, and arrays — inconsistent, poorly designed, and hard to use. JCF replaced all of that with a coherent design.

---

## The Collection Hierarchy

```
java.lang.Iterable
    └── java.util.Collection
            ├── List              (ordered, allows duplicates)
            │     ├── ArrayList
            │     ├── LinkedList
            │     └── Vector (legacy)
            │
            ├── Set               (no duplicates)
            │     ├── HashSet
            │     ├── LinkedHashSet
            │     └── TreeSet (implements SortedSet)
            │
            └── Queue             (FIFO ordering by default)
                  ├── LinkedList  (also implements List!)
                  ├── PriorityQueue
                  └── Deque (extends Queue)
                        ├── ArrayDeque
                        └── LinkedList

java.util.Map                     (key-value pairs — NOT in Collection hierarchy)
    ├── HashMap
    ├── LinkedHashMap
    ├── TreeMap (implements SortedMap)
    └── Hashtable (legacy)
```

**Key insight:** `Map` is NOT a `Collection`. It represents mappings (key → value), not just a group of elements. However, you can get a `Collection` view of a Map via `map.values()`, `map.keySet()`, and `map.entrySet()`.

---

## ASCII Art: The Full Picture

```
┌─────────────────────────────────────────────────────────────┐
│                      Iterable<E>                            │
│                          │                                  │
│                     Collection<E>                           │
│                    /     |     \                            │
│                  /       |       \                          │
│              List<E>   Set<E>   Queue<E>                    │
│             /    \    /  |  \   /    \                      │
│        ArrayList  \ HashSet  \ PQ  Deque                    │
│        LinkedList  LinkedHashSet    └── ArrayDeque           │
│        Vector(legacy) TreeSet                               │
│                                                             │
│   ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─      │
│                                                             │
│                        Map<K,V>  (separate hierarchy)       │
│                       /    |    \                           │
│                 HashMap  LinkedHashMap  TreeMap              │
└─────────────────────────────────────────────────────────────┘
```

---

## Quick Decision Table

| I need to...                              | Use                | Why                              |
|-------------------------------------------|--------------------|----------------------------------|
| Store items in order, allow duplicates    | `ArrayList`        | Fast random access, most common  |
| Store items, fast insert/delete at ends   | `ArrayDeque`       | O(1) add/remove at both ends     |
| Store unique items, fast lookup           | `HashSet`          | O(1) average operations          |
| Store unique items in sorted order        | `TreeSet`          | O(log n), sorted automatically   |
| Map keys to values, fast lookup           | `HashMap`          | O(1) average get/put             |
| Map keys to values, maintain order        | `LinkedHashMap`    | Insertion order preserved        |
| Map keys to values, sorted by key         | `TreeMap`          | O(log n), sorted keys            |
| LIFO stack (undo, backtracking)           | `ArrayDeque`       | More efficient than Stack class  |
| FIFO queue (task processing)              | `ArrayDeque`       | More efficient than LinkedList   |
| Priority-based processing                 | `PriorityQueue`    | Heap-backed, O(log n) insert     |

---

## The Iterable and Iterator Pattern

Every `Collection` implements `Iterable<E>`, which means you can iterate over it with a for-each loop.

### How Iteration Works Internally

```java
// The for-each loop:
for (String name : names) {
    System.out.println(name);
}

// Is compiled by Java to:
Iterator<String> it = names.iterator();
while (it.hasNext()) {
    String name = it.next();
    System.out.println(name);
}
```

### The Iterator Interface

```java
public interface Iterator<E> {
    boolean hasNext();  // Is there another element?
    E next();           // Get next element and advance
    void remove();      // Remove the last element returned (optional)
}
```

### Why Iterator Matters

1. **Safe removal during iteration** — you cannot use a for-each loop to remove elements from a collection while iterating; you must use `iterator.remove()`.

```java
List<String> names = new ArrayList<>(Arrays.asList("Alice", "Bob", "Charlie"));

// WRONG — throws ConcurrentModificationException:
for (String name : names) {
    if (name.startsWith("B")) {
        names.remove(name);  // ❌ Don't do this!
    }
}

// RIGHT — use iterator.remove():
Iterator<String> it = names.iterator();
while (it.hasNext()) {
    if (it.next().startsWith("B")) {
        it.remove();  // ✅ Safe removal
    }
}

// ALSO RIGHT — Java 8+ removeIf:
names.removeIf(name -> name.startsWith("B"));  // ✅ Clean and modern
```

2. **Fail-fast behavior** — most Java collections are "fail-fast": if the collection is modified during iteration (by anything other than `iterator.remove()`), a `ConcurrentModificationException` is thrown immediately.

### ListIterator

`List` also provides `ListIterator<E>`, which adds:
- `hasPrevious()` / `previous()` — iterate backward
- `add()` — insert elements during iteration
- `set()` — replace elements during iteration

```java
List<String> names = new ArrayList<>(Arrays.asList("Alice", "Bob", "Charlie"));
ListIterator<String> lit = names.listIterator();

while (lit.hasNext()) {
    String name = lit.next();
    lit.set(name.toUpperCase());  // Replace each element
}
// names is now ["ALICE", "BOB", "CHARLIE"]
```

---

## Collections Utility Class

`java.util.Collections` provides static helper methods for all collections:

```java
Collections.sort(list)              // Sort in natural order
Collections.sort(list, comparator)  // Sort with custom comparator
Collections.reverse(list)           // Reverse a list
Collections.shuffle(list)           // Random shuffle
Collections.min(collection)         // Find minimum element
Collections.max(collection)         // Find maximum element
Collections.frequency(collection, element)  // Count occurrences
Collections.unmodifiableList(list)  // Wrap in read-only view
Collections.synchronizedList(list)  // Wrap in thread-safe view
Collections.emptyList()             // Empty immutable list
Collections.singletonList(element)  // Immutable one-element list
```

---

## Key Interfaces Summary

| Interface      | Key Contract                                 | Key Methods                              |
|----------------|----------------------------------------------|------------------------------------------|
| `Collection`   | Group of elements                            | `add`, `remove`, `contains`, `size`      |
| `List`         | Ordered, indexed, duplicates allowed         | `get(int)`, `set(int)`, `indexOf`        |
| `Set`          | No duplicates                                | Same as Collection (no index access!)    |
| `Queue`        | FIFO, elements processed in order            | `offer`, `poll`, `peek`                  |
| `Deque`        | Double-ended queue                           | `offerFirst`, `offerLast`, `pollFirst`   |
| `Map`          | Key-value mappings, unique keys              | `put`, `get`, `containsKey`, `entrySet`  |
| `SortedSet`    | Set in natural/comparator order              | `first`, `last`, `headSet`, `tailSet`    |
| `SortedMap`    | Map with sorted keys                         | `firstKey`, `lastKey`, `headMap`         |

---

## Modern Alternatives (Java 9+)

Java 9 introduced factory methods for creating small, immutable collections:

```java
// Immutable list
List<String> languages = List.of("Java", "Python", "Go");

// Immutable set
Set<Integer> primes = Set.of(2, 3, 5, 7, 11);

// Immutable map
Map<String, Integer> scores = Map.of("Alice", 95, "Bob", 87, "Charlie", 92);
```

These are **unmodifiable** — calling `add()` or `remove()` throws `UnsupportedOperationException`. Use them when you want to guarantee a collection won't be changed.

---

## Coming Up

- **02-lists-arraylist-linkedlist.md**: Deep dive into List implementations
- **03-maps-and-sets.md**: HashMap internals, equals/hashCode, and patterns
- **04-stack-queue-deque.md**: Stack, Queue, and Deque with real-world use cases
- **05-big-o-complexity.md**: Understanding performance characteristics
- **06-choosing-the-right-structure.md**: Decision flowchart for picking the right collection
