# Java Core — 50 Interview Questions & Answers

---

## Section 1: Java Fundamentals (Q1–Q10)

---

### Q1. What is the difference between JDK, JRE, and JVM?

**JVM (Java Virtual Machine)** is the runtime engine that executes Java bytecode. It is platform-specific (Windows JVM, Linux JVM) but executes the same `.class` bytecode, enabling "write once, run anywhere."

**JRE (Java Runtime Environment)** = JVM + core class libraries (java.lang, java.util, etc.) + supporting files. Sufficient to *run* Java programs.

**JDK (Java Development Kit)** = JRE + compiler (`javac`) + debugger (`jdb`) + other development tools. Required to *develop* Java programs.

```
JDK
 └── JRE
      └── JVM
```

**Interview tip:** "I need JDK to compile, JRE to run, and JVM executes the bytecode. JVM is what makes Java platform-independent — the same `.class` file runs on any machine that has a compatible JVM."

---

### Q2. What is the difference between `==` and `.equals()`?

`==` compares **references** (memory addresses for objects, values for primitives).

`.equals()` compares **logical content** (defined by the class).

```java
String a = new String("hello");
String b = new String("hello");

System.out.println(a == b);       // false — different objects
System.out.println(a.equals(b));  // true  — same content

int x = 5, y = 5;
System.out.println(x == y);       // true  — primitives compared by value
```

**String pool:** String literals are interned, so `"hello" == "hello"` is `true`, but this is an implementation detail — always use `.equals()` for String comparison.

**Contract for `.equals()`:** reflexive, symmetric, transitive, consistent, and `x.equals(null)` must return false. Override `hashCode()` whenever you override `equals()`.

---

### Q3. Explain Java's memory model: Stack vs Heap.

**Stack:**
- Stores local variables, method call frames, and primitive values
- Each thread has its own stack
- Memory automatically freed when method returns (LIFO)
- StackOverflowError if depth exceeded (e.g., infinite recursion)
- Fast allocation/deallocation

**Heap:**
- Stores all objects and arrays
- Shared across threads (requires synchronization for thread safety)
- Memory managed by Garbage Collector
- OutOfMemoryError if heap exhausted
- Slower than stack due to GC overhead

```java
void method() {
    int x = 5;               // x lives on stack
    String s = new String(); // s reference on stack, String object on heap
}
// After method returns: x removed from stack, s removed from stack
// The String object on heap is eligible for GC (no more references)
```

**Permanent Generation / Metaspace (Java 8+):** Stores class metadata. Replaced PermGen with Metaspace which grows dynamically in native memory.

---

### Q4. What are the 8 primitive types in Java?

| Type      | Size    | Range                                  | Default |
|-----------|---------|----------------------------------------|---------|
| `byte`    | 8 bits  | -128 to 127                            | 0       |
| `short`   | 16 bits | -32,768 to 32,767                      | 0       |
| `int`     | 32 bits | -2^31 to 2^31-1 (~±2.1 billion)        | 0       |
| `long`    | 64 bits | -2^63 to 2^63-1                        | 0L      |
| `float`   | 32 bits | ±3.4e38 (~6-7 decimal digits)          | 0.0f    |
| `double`  | 64 bits | ±1.8e308 (~15-16 decimal digits)       | 0.0d    |
| `char`    | 16 bits | 0 to 65,535 (Unicode code point)       | '\u0000'|
| `boolean` | 1 bit   | true / false                           | false   |

**Wrapper classes:** `Integer`, `Long`, `Double`, etc. — used in generics (which require objects), Collections, and for null representation. **Autoboxing** converts automatically between primitive and wrapper.

```java
List<Integer> list = new ArrayList<>();
list.add(5);        // autoboxing: int 5 → Integer(5)
int x = list.get(0); // unboxing: Integer(5) → int 5
```

**Beware:** unboxing `null` causes `NullPointerException`.

---

### Q5. What is autoboxing and unboxing? What are the pitfalls?

**Autoboxing:** automatic conversion from primitive to wrapper (`int` → `Integer`).
**Unboxing:** automatic conversion from wrapper to primitive (`Integer` → `int`).

```java
Integer a = 5;   // autoboxing
int b = a;       // unboxing
```

**Pitfalls:**

1. **NullPointerException on unboxing:**
```java
Integer x = null;
int y = x; // NullPointerException
```

2. **Unexpected == comparison:** `Integer` caches values -128 to 127.
```java
Integer a = 127, b = 127;
System.out.println(a == b); // true (cached)
Integer c = 128, d = 128;
System.out.println(c == d); // false (not cached — different objects)
```

3. **Performance:** autoboxing in tight loops creates many objects.
```java
Long sum = 0L;
for (long i = 0; i < 1_000_000; i++) {
    sum += i; // unbox, add, rebox — 1 million Long objects created
}
// Fix: use long sum = 0L;
```

---

### Q6. What is the difference between `String`, `StringBuilder`, and `StringBuffer`?

| Feature         | String           | StringBuilder    | StringBuffer     |
|-----------------|------------------|------------------|------------------|
| Mutability      | Immutable        | Mutable          | Mutable          |
| Thread safety   | Thread-safe      | NOT thread-safe  | Thread-safe      |
| Performance     | Slow for concat  | Fast             | Slower (synchronized) |
| Use case        | General, maps    | Single-thread    | Multi-thread     |

**Why String is immutable:**
- Security (class names, network connections, passwords)
- String pool / interning
- Hash code can be cached (HashMap keys)

**String concatenation with `+` in loops:**
```java
// Bad — creates O(n^2) characters (each + creates new String)
String s = "";
for (int i = 0; i < 1000; i++) s += i;

// Good — O(n)
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) sb.append(i);
String s = sb.toString();
```

**Note:** Java compiler optimizes single-line concatenation to StringBuilder, but NOT loops.

---

### Q7. Explain `final`, `finally`, and `finalize`.

**`final`:**
- Variable: constant (must be initialized once)
- Method: cannot be overridden
- Class: cannot be subclassed (e.g., `String`, `Integer`)

```java
final int MAX = 100;           // constant
final class Immutable {}       // can't extend
final void method() {}         // can't override
```

**`finally`:**
- Block in try-catch that ALWAYS executes (with very few exceptions: `System.exit()`, JVM crash)
- Used for cleanup (close resources)
- Note: try-with-resources is preferred over `finally` for `AutoCloseable` objects

```java
try {
    // risky code
} catch (Exception e) {
    // handle
} finally {
    // ALWAYS runs — close connections, files, etc.
}
```

**`finalize()`:**
- Method called by GC before object is collected (deprecated in Java 9, removed in Java 18)
- Unreliable — no guarantee when or if called
- Do NOT use — use `Closeable`/`AutoCloseable` instead

---

### Q8. What is the difference between `throw` and `throws`?

**`throw`:** explicitly throws an exception instance.
```java
throw new IllegalArgumentException("Value must be positive");
```

**`throws`:** declares that a method may throw checked exceptions — callers must handle or declare them.
```java
public void readFile(String path) throws IOException {
    // ...
}
```

**Checked vs Unchecked exceptions:**
- **Checked** (must declare/handle): `IOException`, `SQLException`, `ClassNotFoundException`
- **Unchecked** (RuntimeException + Error): `NullPointerException`, `ArrayIndexOutOfBoundsException`, `IllegalArgumentException`
- **Error:** JVM-level, not meant to be caught: `OutOfMemoryError`, `StackOverflowError`

**Exception hierarchy:**
```
Throwable
├── Error         (don't catch — JVM problems)
└── Exception
    ├── RuntimeException   (unchecked)
    └── [Others]           (checked: IOException, etc.)
```

---

### Q9. What is the `static` keyword? When should you use it?

`static` members belong to the **class** rather than any instance.

**Static variable:** shared across all instances.
```java
class Counter {
    static int count = 0; // ONE copy shared by all Counter instances
    Counter() { count++; }
}
```

**Static method:** can be called without an instance. Cannot access instance variables or `this`.
```java
Math.sqrt(4.0);    // No Math instance needed
Integer.parseInt("42");
```

**Static block:** runs once when class is loaded.
```java
static {
    // Initialize static resources, load configs
}
```

**Static inner class:** does not hold a reference to the outer class instance.

**Use `static` when:**
- Utility methods (Math, Collections, Arrays)
- Factory methods
- Constants (`static final`)
- Shared state (counters, singletons — but be careful with thread safety)

**Avoid static when:** you need polymorphism, testability (static methods are hard to mock), or per-instance state.

---

### Q10. What is a varargs method? How does it work?

Varargs (`...`) allows a method to accept a variable number of arguments.

```java
public int sum(int... numbers) {
    int total = 0;
    for (int n : numbers) total += n;
    return total;
}

sum();          // 0
sum(1);         // 1
sum(1, 2, 3);   // 6
sum(new int[]{1, 2, 3}); // also valid
```

**Under the hood:** varargs is syntactic sugar for an array. `int... numbers` is `int[] numbers`.

**Rules:**
- Only one varargs parameter per method
- Must be the last parameter
- Avoid overloading varargs methods (ambiguity)

---

## Section 2: OOP in Java (Q11–Q20)

---

### Q11. What is the difference between abstract class and interface?

| Feature              | Abstract Class              | Interface                        |
|----------------------|-----------------------------|----------------------------------|
| Instantiation        | Cannot be instantiated      | Cannot be instantiated           |
| Multiple inheritance | No (single inheritance)     | Yes (implement multiple)         |
| Constructor          | Yes                         | No                               |
| Instance fields      | Yes                         | No (only `public static final`)  |
| Access modifiers     | Any                         | `public` by default              |
| Methods              | Abstract + concrete         | Abstract + default + static      |
| State                | Can have state              | Cannot have state (Java 8+: can have default methods but no fields) |

**When to use abstract class:** when classes share common state/implementation (Template Method pattern, partial implementation).

**When to use interface:** to define a contract; when multiple inheritance of type is needed (`Comparable`, `Runnable`, `Serializable`).

**Java 8+ interface features:**
- `default` methods: concrete method in interface (backward compatibility)
- `static` methods: utility methods on interface
- `private` methods (Java 9+): shared helper for default methods

---

### Q12. Explain method overloading vs overriding.

**Overloading (compile-time polymorphism):**
- Same class, same name, different parameter list
- Resolved at compile time based on parameter types

```java
void print(int x) {}
void print(String s) {}
void print(int x, int y) {} // all valid overloads
```

**Overriding (runtime polymorphism):**
- Subclass provides different implementation of inherited method
- Same name, same parameter list, same (or covariant) return type
- Resolved at runtime (dynamic dispatch)

```java
class Animal { String speak() { return "..."; } }
class Dog extends Animal {
    @Override
    String speak() { return "Woof"; } // overrides Animal.speak()
}
```

**Key differences:**

| Aspect        | Overloading         | Overriding              |
|---------------|---------------------|-------------------------|
| Class         | Same class          | Superclass + subclass   |
| Parameters    | Must differ         | Must match              |
| Return type   | Can differ          | Must match (or covariant) |
| Polymorphism  | Compile-time        | Runtime                 |
| `static`      | Can overload        | Cannot override static (only hide) |
| `private`     | Can overload        | Cannot override private |

---

### Q13. What is polymorphism? How does Java achieve it?

**Polymorphism** ("many forms") means the same operation can behave differently on different types.

**Runtime (dynamic) polymorphism:** via method overriding + inheritance. Java uses a **virtual method table (vtable)** — each object has a pointer to its class's vtable; method calls are dispatched dynamically.

```java
Animal[] animals = {new Dog(), new Cat(), new Bird()};
for (Animal a : animals) {
    System.out.println(a.speak()); // calls Dog.speak(), Cat.speak(), Bird.speak()
}
```

**Compile-time (static) polymorphism:** via method overloading. The compiler selects the correct method based on argument types at compile time.

**Parametric polymorphism:** via generics (`List<T>`) — same code works for different types.

**Why polymorphism matters:**
- Open/Closed Principle — add new types without changing existing code
- Dependency injection — depend on interfaces, swap implementations
- Collections — `List<Animal>` can hold any `Animal` subtype

---

### Q14. What is `super`? When do you use it?

`super` refers to the parent class.

**Call parent constructor:** must be first statement in child constructor.
```java
class Child extends Parent {
    Child(int x) {
        super(x);  // calls Parent(int x) constructor
        // ...
    }
}
```

**Call parent method:** when overriding, you can still invoke the parent version.
```java
@Override
void describe() {
    super.describe(); // parent's implementation
    System.out.println("Child additions");
}
```

**Access parent field:** when shadowed by child field (avoid shadowing — confusing).

**If you don't call `super()` explicitly:** Java automatically inserts `super()` (no-arg) as the first call. If the parent has no no-arg constructor, you must call `super(args)` explicitly.

---

### Q15. What is encapsulation? How do you implement it?

**Encapsulation** is bundling data (fields) and behavior (methods) together, while hiding internal state from external access.

**Implementation:**
1. Make fields `private`
2. Provide `public` getters/setters (only as needed)
3. Add validation in setters

```java
public class BankAccount {
    private double balance; // hidden

    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.balance += amount;
    }
    // No setter for balance — can only change through deposit/withdraw
}
```

**Benefits:**
- Control over state changes (validation, invariants)
- Implementation can change without affecting callers
- Easier to test and debug (limited entry points)
- Thread safety (synchronized setters)

---

### Q16. Explain inheritance vs composition. When to use each?

**Inheritance ("is-a"):** a subclass IS a more specific version of the parent.
```java
Dog extends Animal  // Dog IS-A Animal
```

**Composition ("has-a"):** a class CONTAINS instances of other classes.
```java
Car has-a Engine    // Car HAS-A Engine
```

**Prefer composition over inheritance** (Effective Java, Item 18):
- Inheritance tightly couples classes — changes in parent break child
- Composition is more flexible (swap implementations at runtime)
- Multiple inheritance of behavior not possible in Java

**When inheritance is appropriate:**
- Genuine "is-a" relationship
- Subclass truly IS a specialization
- Classes are designed for extension (abstract classes)

**Example:**
```java
// Bad: Stack extends Vector (Stack IS-NOT-A Vector)
// Good: Stack HAS-A internal storage

class Stack<T> {
    private Deque<T> storage = new ArrayDeque<>(); // composition
    public void push(T item) { storage.push(item); }
    public T pop() { return storage.pop(); }
}
```

---

### Q17. What is a functional interface? What are common ones in Java?

A **functional interface** has exactly one abstract method. They can be used with lambda expressions and method references.

Annotate with `@FunctionalInterface` (optional but recommended — compiler enforces single abstract method).

**Common functional interfaces:**

| Interface       | Method           | Description                                 |
|-----------------|------------------|---------------------------------------------|
| `Runnable`      | `void run()`     | No args, no return                          |
| `Callable<V>`   | `V call()`       | No args, returns V, throws checked exceptions |
| `Supplier<T>`   | `T get()`        | No args, returns T                          |
| `Consumer<T>`   | `void accept(T)` | Takes T, no return                          |
| `Function<T,R>` | `R apply(T)`     | Takes T, returns R                          |
| `Predicate<T>`  | `boolean test(T)`| Takes T, returns boolean                    |
| `BiFunction<T,U,R>` | `R apply(T,U)` | Two args, returns R                      |
| `Comparator<T>` | `int compare(T,T)` | Compare two values                       |

```java
// Lambda = implementation of functional interface
Predicate<String> isLong = s -> s.length() > 10;
Function<String, Integer> length = String::length; // method reference
Consumer<String> print = System.out::println;
Supplier<List<String>> factory = ArrayList::new;
```

---

### Q18. What are default methods in interfaces? Why were they added?

**Default methods** (Java 8+) provide a concrete implementation in an interface.

```java
interface Collection<E> {
    default void forEach(Consumer<E> action) {
        for (E e : this) action.accept(e);
    }
}
```

**Why added:** To evolve interfaces without breaking existing implementations. Before Java 8, adding a method to an interface broke all implementing classes. Default methods allow backwards-compatible interface evolution.

**Example (Java 8 `Iterable.forEach`):**
```java
list.forEach(System.out::println); // Works on all existing List implementations
```

**Diamond problem:** If a class implements two interfaces with the same default method, the class must override it.
```java
interface A { default void greet() { System.out.println("A"); } }
interface B { default void greet() { System.out.println("B"); } }
class C implements A, B {
    public void greet() { A.super.greet(); } // must override, specify which
}
```

---

### Q19. What is the difference between `instanceof` and casting?

**`instanceof`:** checks at runtime whether an object is an instance of a type. Returns boolean.

```java
Object obj = "hello";
boolean isString = obj instanceof String; // true
```

**Casting:** tells the compiler to treat an object as a specific type. Can throw `ClassCastException` at runtime.

```java
Object obj = "hello";
String s = (String) obj;         // OK — obj really is a String
Integer i = (Integer) obj;       // ClassCastException at runtime
```

**Pattern matching instanceof (Java 16+):**
```java
// Before Java 16
if (obj instanceof String) {
    String s = (String) obj; // manual cast
    System.out.println(s.length());
}

// Java 16+
if (obj instanceof String s) {
    System.out.println(s.length()); // s is automatically cast and bound
}
```

**Best practice:** always check with `instanceof` before casting (or use pattern matching).

---

### Q20. What is the Object class? List its key methods.

Every Java class implicitly extends `Object`. It defines the common contract for all Java objects.

**Key methods:**

| Method             | Purpose                                              |
|--------------------|------------------------------------------------------|
| `equals(Object o)` | Logical equality (default: == reference comparison) |
| `hashCode()`       | Hash code for use in HashMap/HashSet                |
| `toString()`       | String representation (default: ClassName@hexHash)  |
| `getClass()`       | Runtime class of the object                         |
| `clone()`          | Creates a copy (shallow by default, must implement Cloneable) |
| `finalize()`       | Called before GC (deprecated, don't use)            |
| `wait()`/`notify()`/`notifyAll()` | Thread synchronization (used with `synchronized`) |

**The `equals`/`hashCode` contract:**
1. If `a.equals(b)` then `a.hashCode() == b.hashCode()` (mandatory)
2. If `a.hashCode() == b.hashCode()`, `a.equals(b)` may be false (allowed — hash collision)
3. Violation breaks HashMap/HashSet behavior

---

## Section 3: Java Collections (Q21–Q30)

---

### Q21. Explain the Java Collections hierarchy.

```
Iterable
└── Collection
    ├── List (ordered, allows duplicates)
    │   ├── ArrayList   — dynamic array
    │   ├── LinkedList  — doubly linked list (also Queue/Deque)
    │   └── Vector      — legacy, synchronized
    ├── Set (no duplicates)
    │   ├── HashSet         — O(1) ops, no order
    │   ├── LinkedHashSet   — insertion order
    │   └── TreeSet         — sorted order, O(log n)
    └── Queue
        ├── PriorityQueue   — min-heap
        ├── ArrayDeque      — double-ended queue
        └── LinkedList      — also implements Queue

Map (key-value, separate hierarchy)
├── HashMap         — O(1) ops, no order
├── LinkedHashMap   — insertion/access order
├── TreeMap         — sorted keys, O(log n)
└── Hashtable       — legacy, synchronized
```

**Core interfaces to know:** `List`, `Set`, `Map`, `Queue`, `Deque`, `Iterator`.

---

### Q22. What is the difference between ArrayList and LinkedList?

| Feature          | ArrayList                     | LinkedList                      |
|------------------|-------------------------------|---------------------------------|
| Internal         | Dynamic array                 | Doubly linked list              |
| Get by index     | O(1)                          | O(n)                            |
| Insert at middle | O(n) (shift elements)         | O(1) if you have the node, O(n) to find it |
| Insert at end    | Amortized O(1)                | O(1)                            |
| Insert at front  | O(n)                          | O(1)                            |
| Memory           | Less (just array)             | More (node overhead: prev/next pointers) |
| Cache locality   | Better (contiguous)           | Poor (scattered nodes)          |

**When to use ArrayList:** default choice for random access, iteration, most use cases.

**When to use LinkedList:** frequent insertions/deletions at the front or middle when you already have the iterator position; also as a Queue/Deque.

**Real answer in interviews:** "ArrayList almost always. LinkedList has theoretical O(1) inserts but poor cache performance makes ArrayList faster in practice for most workloads."

---

### Q23. How does HashMap work internally?

**Data structure:** array of buckets (linked list or tree per bucket).

**`put(key, value)` process:**
1. Compute `key.hashCode()`
2. Apply supplemental hash: `hash = hash ^ (hash >>> 16)` (reduces collisions)
3. Bucket index = `hash & (capacity - 1)` (capacity is power of 2)
4. If bucket empty: insert directly
5. If collision: check if key exists (via `equals()`), update value; else append to chain

**Collision handling:** Java 8+ uses linked list up to 8 entries, then converts to a red-black tree (O(log n) worst case instead of O(n)).

**Resize (rehashing):** when load factor (default 0.75) exceeded, doubles capacity and rehashes all entries. O(n) operation.

**Initial capacity:** default 16. Use `new HashMap<>(expectedSize / 0.75 + 1)` to avoid rehashing.

**Key requirement:** `equals()` + `hashCode()` must be consistent.

**Thread safety:** HashMap is NOT thread-safe. Use `ConcurrentHashMap` for concurrent access.

---

### Q24. What is the difference between HashMap, LinkedHashMap, and TreeMap?

| Feature        | HashMap          | LinkedHashMap          | TreeMap                      |
|----------------|------------------|------------------------|------------------------------|
| Order          | None             | Insertion (or access)  | Sorted by key                |
| `get`/`put`    | O(1) average     | O(1) average           | O(log n)                     |
| Iteration      | Unpredictable    | Insertion order        | Ascending key order          |
| Null keys      | 1 null key       | 1 null key             | No (NullPointerException)    |
| Use case       | General purpose  | LRU cache, ordered output | Range queries, sorted iteration |

**LinkedHashMap for LRU cache:**
```java
new LinkedHashMap<>(16, 0.75f, true) { // accessOrder=true
    protected boolean removeEldestEntry(Map.Entry e) {
        return size() > MAX_SIZE;
    }
};
```

**TreeMap for range queries:**
```java
TreeMap<Integer, String> map = new TreeMap<>();
map.subMap(10, 20);    // keys in [10, 20)
map.headMap(15);        // keys < 15
map.tailMap(15);        // keys >= 15
```

---

### Q25. What is the difference between HashSet and TreeSet?

**HashSet:**
- Backed by HashMap (keys only, values are a dummy object)
- O(1) add/remove/contains
- No guaranteed order
- Allows one null element

**TreeSet:**
- Backed by TreeMap (NavigableMap)
- O(log n) add/remove/contains
- Maintains sorted order (natural order or custom Comparator)
- No null elements (comparison would fail)

**LinkedHashSet:**
- Maintains insertion order
- O(1) operations like HashSet

```java
TreeSet<Integer> ts = new TreeSet<>();
ts.addAll(Arrays.asList(5, 3, 1, 4, 2));
System.out.println(ts);         // [1, 2, 3, 4, 5] — sorted
System.out.println(ts.first()); // 1
System.out.println(ts.last());  // 5
System.out.println(ts.headSet(3)); // [1, 2]
```

---

### Q26. What is the Iterator pattern? What is fail-fast vs fail-safe?

**Iterator:** provides a standard way to traverse a collection without exposing its internal structure.

```java
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    if (s.isEmpty()) it.remove(); // safe removal during iteration
}
```

**Fail-fast iterators** (ArrayList, HashMap, HashSet):
- Track `modCount` — structural modification count
- If collection modified during iteration (not via iterator), throw `ConcurrentModificationException`
- Not guaranteed behavior — don't rely on it for correctness, use proper synchronization

```java
for (String s : list) {
    list.remove(s); // ConcurrentModificationException (fail-fast)
}
```

**Fail-safe iterators** (ConcurrentHashMap, CopyOnWriteArrayList):
- Operate on a snapshot or use lock-free techniques
- No `ConcurrentModificationException`
- May not reflect latest modifications

---

### Q27. Explain `Comparable` vs `Comparator`.

**`Comparable<T>`:** natural ordering — implemented by the class itself. Method: `compareTo(T o)`.

```java
class Student implements Comparable<Student> {
    String name;
    int grade;

    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.grade, other.grade); // sort by grade
    }
}
Collections.sort(students); // uses compareTo
```

**`Comparator<T>`:** external ordering — separate object, defines custom ordering. Method: `compare(T a, T b)`.

```java
Comparator<Student> byName = Comparator.comparing(s -> s.name);
Comparator<Student> byGradeDesc = Comparator.comparingInt(Student::getGrade).reversed();

students.sort(byName);
students.sort(byName.thenComparing(byGradeDesc)); // chaining
```

**When to use:**
- `Comparable`: class has a single "natural" ordering (numbers, dates, strings)
- `Comparator`: multiple orderings, or you don't own the class

**Return convention:** negative if this < other, 0 if equal, positive if this > other.

---

### Q28. How do you make a class safe to use as a HashMap key?

For a class to work correctly as a HashMap key:

1. **Override `hashCode()`:** equal objects must have the same hash code.
2. **Override `equals()`:** define logical equality.
3. **Be immutable:** if the key changes after insertion, the hash code changes, and the entry is lost. (Strings and Integer work as keys because they're immutable.)

```java
public class Point {
    private final int x, y; // final = immutable

    public Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y); // or 31 * x + y
    }
}
```

**Using `Objects.hash()`:** the easy way to combine multiple fields.

**Do not use mutable objects as keys** — if they change after insertion, the entry is unreachable.

---

### Q29. What is `ConcurrentHashMap`? How is it different from `Hashtable`?

**`Hashtable`:**
- Legacy (Java 1.0)
- Synchronizes every method (`synchronized` on entire table)
- Only one thread can access it at a time — very slow under contention
- Does not allow null keys or values

**`ConcurrentHashMap`:**
- Modern concurrent map (Java 5+)
- Uses **lock striping** (Java 7) / **CAS + synchronized on individual buckets** (Java 8+)
- Only locks the affected bucket during write — multiple threads can write to different buckets simultaneously
- Reads are lock-free (non-blocking)
- Does not allow null keys or values (null would be ambiguous: "not present" vs "mapped to null")

**Performance:** ConcurrentHashMap can have 16x (Java 7) or near-unlimited (Java 8) concurrent writes vs Hashtable.

**Use case:** shared map accessed by multiple threads. For single-threaded or externally synchronized code, use HashMap.

```java
ConcurrentHashMap<String, AtomicInteger> wordCount = new ConcurrentHashMap<>();
wordCount.computeIfAbsent("hello", k -> new AtomicInteger(0)).incrementAndGet();
```

---

### Q30. Explain `PriorityQueue` and when to use it.

**PriorityQueue:** implements a **min-heap** by default. `poll()` always returns the smallest element.

```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
minHeap.offer(5);
minHeap.offer(1);
minHeap.offer(3);
System.out.println(minHeap.poll()); // 1 (smallest)
System.out.println(minHeap.poll()); // 3
System.out.println(minHeap.poll()); // 5
```

**Max-heap:** use reverse comparator.
```java
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
// or: new PriorityQueue<>((a, b) -> b - a);
```

**Time complexity:** `offer()`/`poll()` O(log n), `peek()` O(1).

**Common use cases:**
- Dijkstra's shortest path
- Merge k sorted lists
- Top-K elements: maintain heap of size k
- Scheduling tasks by priority
- Huffman encoding

```java
// Top-K largest elements using min-heap of size K
PriorityQueue<Integer> topK = new PriorityQueue<>();
for (int num : nums) {
    topK.offer(num);
    if (topK.size() > k) topK.poll(); // remove smallest
}
// topK contains k largest elements
```

---

## Section 4: Java 8+ Features (Q31–Q40)

---

### Q31. What is a lambda expression? What problem does it solve?

A **lambda** is an anonymous function — a block of code that can be passed as a value.

**Before lambdas:** verbose anonymous inner classes.
```java
Collections.sort(list, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.compareTo(b);
    }
});
```

**With lambda:**
```java
Collections.sort(list, (a, b) -> a.compareTo(b));
// Or: list.sort(String::compareTo);
```

**Syntax:** `(parameters) -> expression` or `(parameters) -> { statements; }`

```java
Runnable r = () -> System.out.println("hello");
Comparator<String> c = (a, b) -> a.length() - b.length();
Function<Integer, Integer> square = x -> x * x;
```

**Lambdas solve:** boilerplate of single-method interface implementations; enable functional programming style; enable Streams API.

**Variable capture:** lambdas can capture `effectively final` local variables (not modified after initialization) and instance/class fields.

---

### Q32. Explain the Java Streams API. What are its benefits?

**Streams** provide a functional pipeline for processing sequences of data. They are **lazy** — operations are not executed until a terminal operation is called.

**Pipeline:** `source → zero or more intermediate operations → terminal operation`

```java
List<String> result = names.stream()         // source
    .filter(s -> s.startsWith("J"))           // intermediate (lazy)
    .map(String::toUpperCase)                 // intermediate (lazy)
    .sorted()                                 // intermediate (lazy)
    .collect(Collectors.toList());            // terminal (triggers execution)
```

**Intermediate operations (lazy):** `filter`, `map`, `flatMap`, `sorted`, `distinct`, `limit`, `skip`, `peek`

**Terminal operations (eager):** `collect`, `forEach`, `count`, `min`, `max`, `anyMatch`, `allMatch`, `noneMatch`, `findFirst`, `findAny`, `reduce`, `toArray`

**Benefits:**
1. Declarative — says WHAT, not HOW
2. Lazy evaluation — skip unnecessary computation
3. Parallel streams — easy parallelism: `.stream()` → `.parallelStream()`
4. Composable — chain operations cleanly

```java
// Find the 3 most expensive products over $50
products.stream()
    .filter(p -> p.getPrice() > 50)
    .sorted(Comparator.comparingDouble(Product::getPrice).reversed())
    .limit(3)
    .collect(Collectors.toList());
```

---

### Q33. What is `Optional`? Why is it better than returning null?

`Optional<T>` is a container that may or may not contain a non-null value.

**Problem with null:** `NullPointerException` — the "billion dollar mistake." Callers forget to null-check.

**With Optional:**
```java
// Method returns Optional instead of potentially null value
Optional<User> findUser(int id) {
    return Optional.ofNullable(userMap.get(id));
}

// Caller explicitly handles the absent case
findUser(42)
    .map(User::getName)
    .orElse("Anonymous");

// Safe access chain
Optional.of(order)
    .map(Order::getCustomer)
    .map(Customer::getAddress)
    .map(Address::getCity)
    .orElse("Unknown city");
```

**Key methods:**
- `Optional.of(value)` — non-null value (throws if null)
- `Optional.ofNullable(value)` — might be null
- `Optional.empty()` — explicitly empty
- `isPresent()` / `isEmpty()` — check presence
- `get()` — get value (throws if empty — avoid)
- `orElse(default)` — value or default
- `orElseGet(() -> ...)` — lazy default computation
- `orElseThrow(ExceptionSupplier)` — throw if empty
- `map(fn)` / `flatMap(fn)` / `filter(pred)` — transform

**When NOT to use Optional:** as a field type, in collections, as a method parameter. Use it as a return type only.

---

### Q34. What is `Stream.collect()` and what are common `Collectors`?

`collect()` is a terminal operation that transforms the stream into a different form.

```java
// To List
List<String> list = stream.collect(Collectors.toList());
// Java 16+: stream.toList() (unmodifiable)

// To Set
Set<String> set = stream.collect(Collectors.toSet());

// To Map
Map<String, Integer> map = stream.collect(
    Collectors.toMap(String::toLowerCase, String::length));

// Grouping
Map<Department, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// Counting by group
Map<Department, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

// Joining strings
String csv = stream.collect(Collectors.joining(", ", "[", "]"));

// Partitioning (split by predicate)
Map<Boolean, List<Integer>> evenOdd = numbers.stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));

// Statistics
IntSummaryStatistics stats = numbers.stream()
    .collect(Collectors.summarizingInt(Integer::intValue));
```

---

### Q35. What is a method reference? Give examples.

**Method reference** is a shorthand for a lambda that calls a specific method: `ClassName::methodName` or `object::methodName`.

| Type                | Syntax                    | Lambda equivalent              |
|---------------------|---------------------------|--------------------------------|
| Static method       | `Integer::parseInt`       | `s -> Integer.parseInt(s)`     |
| Instance (bound)    | `str::contains`           | `s -> str.contains(s)`         |
| Instance (unbound)  | `String::toLowerCase`     | `s -> s.toLowerCase()`         |
| Constructor         | `ArrayList::new`          | `() -> new ArrayList<>()`      |

```java
List<String> names = Arrays.asList("Bob", "Alice", "Charlie");

// Static
names.stream().map(Integer::valueOf); // s -> Integer.valueOf(s)

// Unbound instance
names.stream().map(String::toUpperCase); // s -> s.toUpperCase()

// Bound instance (on a specific object)
String prefix = "Hello";
names.stream().filter(prefix::startsWith); // s -> prefix.startsWith(s)

// Constructor
names.stream().map(StringBuilder::new); // s -> new StringBuilder(s)
```

---

### Q36. What is the difference between `map` and `flatMap` in Streams?

**`map(fn)`:** applies `fn` to each element and wraps results in stream. Returns `Stream<Stream<T>>` if `fn` returns a collection.

**`flatMap(fn)`:** like `map` but flattens nested streams into a single stream.

```java
// map: each word → Stream<String> of chars → results in Stream<Stream<String>>
List<String> words = List.of("Hello", "World");
Stream<Stream<String>> bad = words.stream()
    .map(w -> Arrays.stream(w.split("")));

// flatMap: flattens to Stream<String>
List<String> chars = words.stream()
    .flatMap(w -> Arrays.stream(w.split("")))
    .distinct()
    .collect(Collectors.toList());

// flatMap with Optional
Optional<String> name = Optional.of(user)
    .flatMap(u -> u.getAddress())  // returns Optional<Address>
    .flatMap(a -> a.getCity());    // returns Optional<String>
```

**Rule:** use `flatMap` when the mapping function returns a `Stream` (or `Optional`) and you want to unwrap the nesting.

---

### Q37. What is a `CompletableFuture`? How does it differ from `Future`?

**`Future<T>` (Java 5):** represents an async computation. Problems:
- `get()` blocks — no callback
- No way to chain: no `thenApply`, `thenCompose`
- No way to combine multiple futures
- No error handling pipeline

**`CompletableFuture<T>` (Java 8):** non-blocking, composable async computation.

```java
// Chain async operations
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> fetchUser(id))           // async
    .thenApply(user -> user.getName())           // transform result
    .thenApply(String::toUpperCase)              // transform again
    .exceptionally(ex -> "UNKNOWN");             // error handling

// Combine two futures
CompletableFuture<String> a = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> b = CompletableFuture.supplyAsync(() -> "World");
CompletableFuture<String> combined = a.thenCombine(b, (x, y) -> x + " " + y);

// Wait for all
CompletableFuture.allOf(f1, f2, f3).join();

// Wait for first
CompletableFuture.anyOf(f1, f2, f3).join();
```

**Key methods:** `supplyAsync`, `runAsync`, `thenApply`, `thenCompose`, `thenCombine`, `exceptionally`, `handle`, `allOf`, `anyOf`, `join`/`get`

---

### Q38. Explain `var` (local variable type inference, Java 10+).

`var` lets the compiler infer the type of local variables.

```java
var list = new ArrayList<String>();    // inferred as ArrayList<String>
var entry = map.entrySet().iterator(); // inferred as Iterator<Map.Entry<K,V>>
var i = 0;                             // inferred as int
```

**Rules:**
- Only for **local variables** (not fields, parameters, or return types)
- Must have an initializer (type must be determinable at compile time)
- Cannot be `null` initializer (type would be unknown)
- Not a keyword — `var` is a reserved type name (you can still have a variable named `var`)

**Benefits:** reduces verbosity, especially with generics and iterator types.

**When to avoid:**
```java
var x = process(); // Bad — what type is x? Use explicit type for clarity.
```

Use `var` when the type is obvious from the right-hand side.

---

### Q39. What are `record` classes in Java (Java 16+)?

**Records** are immutable data carriers with auto-generated boilerplate.

```java
// Old way
public final class Point {
    private final int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }
    public int x() { return x; }
    public int y() { return y; }
    @Override public boolean equals(Object o) { ... }
    @Override public int hashCode() { ... }
    @Override public String toString() { ... }
}

// With record (one line!)
public record Point(int x, int y) {}
```

**Auto-generated:** canonical constructor, accessors (`x()` and `y()`), `equals()`, `hashCode()`, `toString()`.

**Compact constructor:**
```java
record Range(int min, int max) {
    Range { // compact constructor — same signature as canonical
        if (min > max) throw new IllegalArgumentException();
    }
}
```

**Use cases:** DTOs, value objects, return multiple values from methods, pattern matching targets.

**Cannot:** extend other classes (implicitly extends Record), have mutable fields, declare instance fields outside the header.

---

### Q40. What are sealed classes in Java (Java 17+)?

**Sealed classes** restrict which classes can extend or implement them.

```java
public sealed interface Shape
    permits Circle, Rectangle, Triangle {}

public record Circle(double radius) implements Shape {}
public record Rectangle(double w, double h) implements Shape {}
public record Triangle(double a, double b, double c) implements Shape {}
```

**Permitted subclasses must** be: `final`, `sealed`, or `non-sealed`.

**Benefits:**
1. Exhaustive pattern matching — compiler knows all subtypes
2. Security — prevents unexpected extensions
3. Enables algebraic data types (sum types)

**With switch expressions (Java 21+):**
```java
double area = switch (shape) {
    case Circle c       -> Math.PI * c.radius() * c.radius();
    case Rectangle r    -> r.w() * r.h();
    case Triangle t     -> triangleArea(t);
    // No default needed — compiler knows all cases
};
```

---

## Section 5: Concurrency & Advanced (Q41–Q50)

---

### Q41. What is the difference between a process and a thread?

**Process:**
- Independent execution unit with its own memory space
- Inter-process communication (IPC) is expensive (pipes, sockets, shared memory)
- Process crash doesn't affect other processes
- Heavier to create/switch

**Thread:**
- Lightweight execution unit within a process
- Shares memory with other threads in the same process
- Faster context switching
- Risk: shared memory → race conditions, deadlocks

**Java threads:**
- Each Java application runs in a JVM process
- JVM manages threads mapped to OS threads (typically 1:1)
- `Thread.start()` creates a new OS thread

```java
// Create thread
Thread t = new Thread(() -> System.out.println("Hello from thread"));
t.start(); // Don't call run() directly — that runs on current thread

// Or with ExecutorService
ExecutorService pool = Executors.newFixedThreadPool(4);
pool.submit(() -> doWork());
```

---

### Q42. What is the `synchronized` keyword? What are its limitations?

`synchronized` ensures only one thread executes the synchronized block/method at a time.

```java
class Counter {
    private int count = 0;

    // Method-level lock: locks on 'this'
    public synchronized void increment() {
        count++;
    }

    // Block-level lock: more fine-grained
    public void incrementBlock() {
        synchronized (this) {
            count++;
        }
    }

    // Class-level lock (for static methods)
    public static synchronized void staticMethod() {}
}
```

**Limitations:**
1. **Coarse-grained:** method-level synchronization locks the whole method
2. **Not composable:** can't acquire two locks atomically without risk of deadlock
3. **No timeout:** cannot try to acquire a lock with a timeout (use `Lock.tryLock()`)
4. **No interrupt:** waiting for synchronized lock cannot be interrupted
5. **No fairness guarantee:** threads may starve
6. **Performance:** all reads and writes must acquire the same lock

**Better alternative for complex cases:** `java.util.concurrent.locks.ReentrantLock` — supports fairness, tryLock, lockInterruptibly.

---

### Q43. What is a deadlock? How do you prevent it?

**Deadlock:** Thread A holds lock 1, waits for lock 2. Thread B holds lock 2, waits for lock 1. Neither can proceed.

**Four conditions (Coffman conditions):**
1. Mutual exclusion
2. Hold and wait
3. No preemption
4. Circular wait

**Prevention strategies:**

**1. Lock ordering:** always acquire locks in the same order.
```java
// Both threads acquire lock1 before lock2 — no circular wait
synchronized(lock1) {
    synchronized(lock2) { /* ... */ }
}
```

**2. Lock timeout:** use `tryLock(timeout)` with `ReentrantLock`.
```java
if (lock1.tryLock(1, TimeUnit.SECONDS)) {
    try {
        if (lock2.tryLock(1, TimeUnit.SECONDS)) { /* ... */ }
    } finally { lock1.unlock(); }
}
```

**3. Avoid nested locks:** acquire only one lock at a time.

**4. Use higher-level concurrency primitives:** `ConcurrentHashMap`, `AtomicInteger`, `CopyOnWriteArrayList`, `BlockingQueue` — avoid manual locking.

---

### Q44. What is `volatile`? When should you use it?

`volatile` ensures visibility of changes across threads — reads/writes go directly to main memory, not CPU cache.

**Without `volatile`:** a thread may cache a field value. Another thread updating it may not be seen.

```java
// BUG: stopRequested may be cached — loop never terminates
private static boolean stopRequested = false;

// FIX:
private static volatile boolean stopRequested = false;
```

**`volatile` guarantees:**
1. **Visibility:** changes are immediately visible to all threads
2. **Ordering:** prevents instruction reordering across the volatile access (happens-before)

**`volatile` does NOT guarantee atomicity:**
```java
volatile int count = 0;
count++; // NOT atomic: read + increment + write — still a race condition
// Use AtomicInteger instead
```

**Use `volatile` when:**
- One thread writes, others only read (flag pattern above)
- Simple flags (`running`, `initialized`)
- Publishing an immutable object (safe publication)

**Use `AtomicInteger`/`AtomicReference` when you need atomic compound operations.**

---

### Q45. What is the Java Memory Model (JMM)?

The **JMM** defines how threads interact through memory — specifically the **visibility** and **ordering** guarantees.

**Key concepts:**

**Happens-before relationship:** if action A happens-before action B, the effects of A are visible to B.

Happens-before is established by:
- Program order (within a thread)
- `synchronized` (monitor release happens-before monitor acquire)
- `volatile` (volatile write happens-before volatile read of same field)
- Thread start (`t.start()` happens-before any action in thread t)
- Thread join (completion of thread t happens-before `t.join()` returns)

**Without happens-before:** compiler/CPU can reorder instructions and cache values.

**Example — double-checked locking (incorrect without volatile):**
```java
// Broken — object may be seen in partially initialized state
private static Singleton instance;

// Fixed with volatile
private static volatile Singleton instance;

static Singleton getInstance() {
    if (instance == null) {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton(); // safe with volatile
            }
        }
    }
    return instance;
}
```

---

### Q46. What is the `ExecutorService`? How is it better than creating threads directly?

**Problems with creating threads directly:**
- Thread creation is expensive (OS resources)
- Unbounded thread creation can exhaust memory
- No reuse of completed threads
- Hard to control concurrency level

**`ExecutorService`:** manages a pool of reusable threads.

```java
// Fixed thread pool — max N concurrent tasks
ExecutorService pool = Executors.newFixedThreadPool(4);

// Submit tasks
Future<Integer> future = pool.submit(() -> computeSomething());
pool.execute(() -> doFireAndForget());

// Shutdown gracefully
pool.shutdown(); // stop accepting new tasks
pool.awaitTermination(60, TimeUnit.SECONDS); // wait for running tasks
pool.shutdownNow(); // interrupt running tasks if needed
```

**Types:**
- `newFixedThreadPool(n)`: fixed number of threads
- `newCachedThreadPool()`: creates threads as needed, reuses idle ones
- `newSingleThreadExecutor()`: one thread, sequential execution
- `newScheduledThreadPool(n)`: scheduled/periodic tasks
- `newVirtualThreadPerTaskExecutor()` (Java 21): virtual thread per task

**Best practice:** use `ExecutorService` over raw threads. Use `CompletableFuture.supplyAsync()` for async pipelines.

---

### Q47. What is `AtomicInteger`? How does it work?

`AtomicInteger` provides thread-safe integer operations **without synchronization**.

```java
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();  // atomic ++
counter.getAndIncrement();  // atomic, returns old value
counter.addAndGet(5);       // atomic +=
counter.compareAndSet(expected, newValue); // CAS
```

**How it works:** uses **Compare-And-Swap (CAS)** — a CPU instruction that atomically:
1. Reads current value
2. Compares with expected value
3. Writes new value only if they match (otherwise retries)

**No locking needed** — CAS is implemented in hardware as a single atomic instruction.

**Under contention:** CAS can spin (retry loop), but for low-contention scenarios it's faster than synchronization.

**`LongAdder` (Java 8+):** better than `AtomicLong` for high-contention incrementing — uses multiple cells to distribute contention.

---

### Q48. What is the difference between `Callable` and `Runnable`?

| Feature       | `Runnable`               | `Callable<V>`              |
|---------------|--------------------------|----------------------------|
| Return value  | `void`                   | Returns `V`                |
| Exceptions    | Cannot throw checked     | Can throw checked exceptions |
| Method        | `void run()`             | `V call() throws Exception`|
| Use with      | `Thread`, `execute()`    | `submit()` → `Future<V>`   |

```java
// Runnable — no return
Runnable r = () -> System.out.println("working...");
new Thread(r).start();

// Callable — returns result
Callable<Integer> c = () -> {
    Thread.sleep(1000); // can throw checked exceptions
    return 42;
};

ExecutorService pool = Executors.newSingleThreadExecutor();
Future<Integer> future = pool.submit(c);
Integer result = future.get(); // blocks until done
```

---

### Q49. What is Garbage Collection? Explain generational GC.

**Garbage Collection:** automatic memory management — identifies and frees objects that are no longer reachable.

**Generational hypothesis:** most objects die young. GC exploits this by dividing heap into generations.

**Heap generations (HotSpot JVM):**

```
Heap
├── Young Generation (Eden + Survivor 0 + Survivor 1)
│   └── Minor GC — frequent, fast, collects short-lived objects
└── Old Generation (Tenured)
    └── Major GC — less frequent, slower
```

**Minor GC process:**
1. New objects allocated in Eden
2. When Eden full, live objects copied to Survivor space
3. Objects surviving multiple GC cycles promoted to Old Gen

**GC algorithms:**
- **Serial GC:** single thread, stop-the-world. Good for single-CPU, small heaps.
- **Parallel GC:** multiple GC threads. Maximizes throughput.
- **G1 GC (default Java 9+):** divides heap into equal-sized regions. Balances throughput and latency.
- **ZGC / Shenandoah:** low-latency, concurrent. Sub-millisecond pause times.

**Tuning:** `-Xms`, `-Xmx` (heap size), `-XX:+UseG1GC`, `-XX:MaxGCPauseMillis`

---

### Q50. What is reflection in Java? What are its uses and dangers?

**Reflection:** the ability to inspect and manipulate classes, methods, fields, and constructors at **runtime**, even private members.

```java
Class<?> clazz = Class.forName("com.example.MyClass");

// Inspect
Method[] methods = clazz.getDeclaredMethods();
Field field = clazz.getDeclaredField("secretField");
field.setAccessible(true); // bypass access control
field.set(instance, "new value");

// Invoke
Method m = clazz.getDeclaredMethod("privateMethod", String.class);
m.setAccessible(true);
m.invoke(instance, "argument");

// Create instance
Constructor<?> ctor = clazz.getDeclaredConstructor(int.class);
Object obj = ctor.newInstance(42);
```

**Common uses:**
- Frameworks: Spring (dependency injection), Hibernate (ORM), JUnit (test runner)
- Serialization/deserialization (Jackson, Gson)
- Plugin architectures
- IDE tools, debuggers

**Dangers:**
1. **Performance:** slower than direct calls (method lookup at runtime)
2. **Security:** bypasses access control (`private`, encapsulation)
3. **Brittleness:** no compile-time type checking; field/method name changes break code
4. **Refactoring difficulty:** IDEs may not detect reflection-based access

**Java 9+ module system** restricts some reflective access (open/exports required).

**Prefer alternatives:** dependency injection frameworks, interfaces, generics. Use reflection only when truly necessary.
