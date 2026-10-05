# Maps and Sets

## The Map Interface

A `Map<K, V>` stores key-value pairs where:
- **Keys are unique** — no two entries can have the same key
- **Each key maps to exactly one value**
- Values can be duplicates

Think of it like a dictionary: you look up a word (key) to find its definition (value).

---

## HashMap

### How HashMap Works Internally

This is one of the most important data structures to understand deeply.

```
HashMap with 16 buckets (default initial capacity):

bucket[0]:  null
bucket[1]:  → ["Bob" → 25]
bucket[2]:  null
bucket[3]:  → ["Alice" → 30] → ["Charlie" → 28]  (collision — same hash % 16)
bucket[4]:  null
...
bucket[15]: → ["Dave" → 22]
```

**Step-by-step: what happens when you call `map.put("Alice", 30)`:**

1. `"Alice".hashCode()` is computed → some large int (e.g., 1952991)
2. `index = hashCode & (capacity - 1)` → maps to a bucket (0–15)
3. If the bucket is empty: create a new `Node("Alice", 30)` and store it there
4. If the bucket has entries: check if any existing entry has key `"Alice"` (using `equals()`)
   - If yes: **replace** the value
   - If no: **add** to the list of entries in that bucket (collision!)

**Step-by-step: what happens when you call `map.get("Alice")`:**

1. Compute `"Alice".hashCode()`
2. Compute bucket index
3. Traverse entries in that bucket, comparing keys with `equals()`
4. Return value when found, `null` if not found

### Load Factor and Resizing

```
load factor = number of entries / number of buckets
default load factor = 0.75

When load factor exceeds 0.75:
  - Create new array with 2x capacity
  - Rehash ALL existing entries to new positions
  - This is expensive (O(n)) but rare
```

This is why `HashMap` has **O(1) amortized** get/put — individual operations are O(1), but occasional resizing is O(n).

### Java 8+ Optimization: Treeification

When a bucket has more than 8 entries (worst case with bad hashing), Java converts that bucket from a linked list to a **balanced binary tree** — changing worst-case lookup from O(n) to O(log n).

### Performance Characteristics

| Operation              | Average Case | Worst Case | Why                           |
|------------------------|--------------|------------|-------------------------------|
| `put(key, value)`      | O(1)         | O(log n)   | Hash + tree bucket            |
| `get(key)`             | O(1)         | O(log n)   | Hash + tree bucket            |
| `containsKey(key)`     | O(1)         | O(log n)   | Same as get                   |
| `remove(key)`          | O(1)         | O(log n)   | Hash + tree bucket            |
| Iteration              | O(n)         | O(n)       | Visit all buckets             |

---

## LinkedHashMap

Same as HashMap but maintains a **doubly-linked list** through all entries in insertion order.

```
LinkedHashMap:

Buckets:          Insertion order list:
[bucket 1] →     "Alice" ↔ "Bob" ↔ "Charlie" ↔ "Dave"
[bucket 2] →                       ↑ head         ↑ tail
[bucket 3] →
```

- Slightly more memory than HashMap
- Slightly slower than HashMap (maintaining the linked list)
- Iteration always in insertion order

**Special feature:** Constructor `new LinkedHashMap<>(capacity, loadFactor, true)` creates an **access-order** map (most recently accessed element moves to end). This is the basis of an **LRU Cache**!

```java
// Simple LRU Cache using LinkedHashMap
Map<Integer, String> lruCache = new LinkedHashMap<>(16, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > 100;  // Keep at most 100 entries
    }
};
```

---

## TreeMap

Stores entries in a **Red-Black Tree** sorted by key natural order (or a `Comparator`).

```
TreeMap with String keys (alphabetical order):
         "Charlie"
        /         \
    "Alice"       "Dave"
       \
      "Bob"
```

- Keys must implement `Comparable` OR you must provide a `Comparator`
- O(log n) for all operations
- Iteration is always in **sorted key order**

```java
TreeMap<String, Integer> map = new TreeMap<>();
map.put("Charlie", 3);
map.put("Alice", 1);
map.put("Bob", 2);

System.out.println(map.firstKey());  // "Alice"
System.out.println(map.lastKey());   // "Charlie"
System.out.println(map.headMap("Bob"));   // {Alice=1}
System.out.println(map.tailMap("Bob"));   // {Bob=2, Charlie=3}
System.out.println(map.subMap("Alice", "Charlie")); // {Alice=1, Bob=2}
```

---

## HashSet, LinkedHashSet, TreeSet

Sets are backed by their Map counterparts:
- `HashSet` is backed by a `HashMap`
- `LinkedHashSet` is backed by a `LinkedHashMap`
- `TreeSet` is backed by a `TreeMap`

The "value" in the backing map is just a dummy object (`PRESENT`). This is why `HashSet` has the same performance characteristics as `HashMap`.

```java
HashSet<String> set = new HashSet<>();
// Internally: HashMap<String, Object> where every value is PRESENT placeholder

set.add("Alice");    // map.put("Alice", PRESENT)
set.contains("Bob"); // map.containsKey("Bob")
```

### Set Operations

```java
Set<Integer> a = new HashSet<>(Set.of(1, 2, 3, 4, 5));
Set<Integer> b = new HashSet<>(Set.of(3, 4, 5, 6, 7));

// Union: all elements from both sets
Set<Integer> union = new HashSet<>(a);
union.addAll(b);  // {1, 2, 3, 4, 5, 6, 7}

// Intersection: elements in both sets
Set<Integer> intersection = new HashSet<>(a);
intersection.retainAll(b);  // {3, 4, 5}

// Difference: elements in a but not b
Set<Integer> difference = new HashSet<>(a);
difference.removeAll(b);  // {1, 2}

// Symmetric difference (elements in either but not both)
Set<Integer> symDiff = new HashSet<>(union);
symDiff.removeAll(intersection);  // {1, 2, 6, 7}
```

---

## CRITICAL: The equals() and hashCode() Contract

This is one of the most important things to get right in Java. If you use a custom object as a Map key or Set element, you **must** override `equals()` and `hashCode()` correctly.

### The Contract

1. If `a.equals(b)` is `true`, then `a.hashCode()` **must equal** `b.hashCode()`
2. If `a.hashCode() != b.hashCode()`, then `a.equals(b)` **must be** `false`
3. `hashCode()` must be **consistent** — same object, same hash across multiple calls

**Violation = broken HashMap/HashSet behavior:**

```java
// BROKEN — equals() overridden but not hashCode()
class Point {
    int x, y;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }
    // NO hashCode() override — uses Object.hashCode() (memory address!)
}

Map<Point, String> map = new HashMap<>();
Point p1 = new Point(1, 2);
map.put(p1, "treasure");

Point p2 = new Point(1, 2);  // Same x,y as p1
map.get(p2);  // Returns null! ❌
// p1.equals(p2) is true, but p1.hashCode() != p2.hashCode()
// So they land in different buckets and p2 is "not found"
```

### Correct Implementation

```java
class Point {
    int x, y;
    
    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);  // Java's Objects.hash() is convenient and correct
    }
}

// Now it works:
Map<Point, String> map = new HashMap<>();
Point p1 = new Point(1, 2);
map.put(p1, "treasure");

Point p2 = new Point(1, 2);
map.get(p2);  // Returns "treasure" ✓
```

### Using Records (Java 16+)

Records automatically generate `equals()` and `hashCode()` based on all fields — the recommended approach for simple value objects:

```java
record Point(int x, int y) {}  // equals() and hashCode() auto-generated ✓

Map<Point, String> map = new HashMap<>();
map.put(new Point(1, 2), "treasure");
map.get(new Point(1, 2));  // "treasure" — works! ✓
```

---

## Common Map Patterns

### 1. Frequency Counter

Count occurrences of each element — one of the most common interview patterns:

```java
String[] words = {"apple", "banana", "apple", "cherry", "banana", "apple"};
Map<String, Integer> freq = new HashMap<>();

for (String word : words) {
    // Old way:
    freq.put(word, freq.getOrDefault(word, 0) + 1);
    
    // Modern way (Java 8+):
    freq.merge(word, 1, Integer::sum);
}
// {apple=3, banana=2, cherry=1}

// Find most frequent
String mostFrequent = Collections.max(freq.entrySet(),
    Map.Entry.comparingByValue()).getKey();
```

### 2. Grouping (One-to-Many)

```java
List<String> names = List.of("Alice", "Bob", "Anna", "Charlie", "Brian", "Catherine");

// Group by first letter
Map<Character, List<String>> groups = new HashMap<>();
for (String name : names) {
    char key = name.charAt(0);
    groups.computeIfAbsent(key, k -> new ArrayList<>()).add(name);
}
// {A=[Alice, Anna], B=[Bob, Brian], C=[Charlie, Catherine]}

// Java 8 streams way (cleaner):
Map<Character, List<String>> groups = names.stream()
    .collect(Collectors.groupingBy(name -> name.charAt(0)));
```

### 3. Caching / Memoization

```java
Map<Integer, Long> fibCache = new HashMap<>();

long fibonacci(int n) {
    if (n <= 1) return n;
    return fibCache.computeIfAbsent(n, k -> fibonacci(k - 1) + fibonacci(k - 2));
}
```

### 4. Counting with computeIfAbsent

```java
// Building an adjacency list for a graph
Map<String, List<String>> adjacency = new HashMap<>();

void addEdge(String from, String to) {
    adjacency.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
    adjacency.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
}
```

### 5. Important Map Methods

```java
Map<String, Integer> map = new HashMap<>();

// Basic operations
map.put("Alice", 30);
map.get("Alice");             // 30
map.getOrDefault("Bob", -1);  // -1 (Bob not in map)
map.containsKey("Alice");     // true
map.containsValue(30);        // true (O(n) scan!)
map.remove("Alice");          // Removes and returns value
map.remove("Alice", 30);      // Only removes if key maps to this value

// Conditional operations
map.putIfAbsent("Bob", 25);              // Only put if key not present
map.computeIfAbsent("Bob", k -> 25);    // Compute value if absent
map.computeIfPresent("Alice", (k, v) -> v + 1); // Update if present
map.compute("Alice", (k, v) -> v == null ? 1 : v + 1); // Always compute
map.merge("Alice", 1, Integer::sum);    // Merge with existing value

// Iteration
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}

// Java 8+
map.forEach((key, value) -> System.out.println(key + " = " + value));

map.replaceAll((key, value) -> value * 2);  // Double all values

// Views
Set<String> keys = map.keySet();         // Live view of keys
Collection<Integer> values = map.values(); // Live view of values
Set<Map.Entry<String,Integer>> entries = map.entrySet(); // Live view of entries
```

---

## Map and Set Comparison Summary

| Class           | Order               | Null keys | Null values | Thread-safe | Time complexity |
|-----------------|---------------------|-----------|-------------|-------------|-----------------|
| `HashMap`       | None                | 1 allowed | Allowed     | No          | O(1) avg        |
| `LinkedHashMap` | Insertion order     | 1 allowed | Allowed     | No          | O(1) avg        |
| `TreeMap`       | Sorted by key       | Not allowed| Allowed    | No          | O(log n)        |
| `HashSet`       | None                | 1 allowed | N/A         | No          | O(1) avg        |
| `LinkedHashSet` | Insertion order     | 1 allowed | N/A         | No          | O(1) avg        |
| `TreeSet`       | Sorted              | Not allowed| N/A        | No          | O(log n)        |
| `Hashtable`     | None (legacy)       | Not allowed| Not allowed| Yes         | O(1) avg        |

For thread-safe maps, use `ConcurrentHashMap` (not `Hashtable`).
