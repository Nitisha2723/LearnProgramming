# Chapter 4: Java Memory Model

## The JVM's Two Main Memory Areas

When your Java program runs, the JVM manages memory in two main areas: the **Heap** and the **Stack**.

```
JVM MEMORY LAYOUT
═══════════════════════════════════════════════════════
│                      HEAP                           │
│   ┌────────────────────────────────────────────┐   │
│   │         All Objects live here              │   │
│   │   new String("hello")  new ArrayList()     │   │
│   │   new BankAccount()    your objects         │   │
│   └────────────────────────────────────────────┘   │
│                                                     │
│         STACKS (one per thread)                     │
│   ┌──────────────┐   ┌──────────────┐              │
│   │   Thread 1   │   │   Thread 2   │   ...         │
│   │   main()     │   │   worker()   │              │
│   │   frame      │   │   frame      │              │
│   │   ──────────│   │   ──────────│              │
│   │   local vars │   │   local vars │              │
│   │   primitives │   │   primitives │              │
│   └──────────────┘   └──────────────┘              │
═══════════════════════════════════════════════════════
```

### The Heap

- **All objects** live on the heap
- **Shared** among all threads (this is what causes threading issues!)
- Managed by the **Garbage Collector**
- Size controlled with `-Xms` (initial) and `-Xmx` (maximum)

```java
String s = new String("hello");  // The String object lives on the heap
List<Integer> list = new ArrayList<>();  // ArrayList and its array live on heap
BankAccount account = new BankAccount();  // BankAccount lives on heap
```

### The Stack

- **One stack per thread** — completely private to that thread
- Holds **stack frames** — one per method call
- Each frame holds: local variables, method parameters, return address
- **Primitives** live on the stack (int, double, boolean, etc.)
- **Object references** live on the stack (the reference, not the object itself!)

```java
public void processAccount(BankAccount account, int amount) {
    // Stack frame for this method call contains:
    // account: reference (8 bytes on 64-bit) ──────────────> actual BankAccount on HEAP
    // amount: primitive int value (4 bytes) (the value 500 is right here on stack)
    // result: primitive int value (4 bytes)
    
    int result = account.getBalance() + amount;
    System.out.println(result);
}
// When method returns, this frame is popped off the stack — instantly freed
```

### Key Difference

```java
// Primitives: value IS on the stack
int x = 42;        // 42 is stored on the stack
int y = x;         // y gets its own copy: 42
y = 100;           // Changes y, x is still 42

// Objects: reference is on the stack, object is on heap
StringBuilder sb1 = new StringBuilder("Hello");
StringBuilder sb2 = sb1;    // sb2 points to the SAME object
sb2.append(" World");       // sb1.toString() is now "Hello World"!
```

---

## Garbage Collection

In C/C++, you manage memory manually (`malloc`/`free`). Forget to free? Memory leak. Free too early? Crash.

Java handles this automatically with **Garbage Collection (GC)**. The GC finds objects that are no longer reachable and frees their memory.

### Mark-and-Sweep (conceptual)

1. **Mark phase**: Starting from "GC roots" (stack variables, static fields), traverse the object graph and mark every reachable object.
2. **Sweep phase**: Free all unmarked objects.

```
GC ROOTS (always considered reachable):
- Local variables on thread stacks
- Static fields
- JNI references

BankAccount account = new BankAccount();  // account ────> [BankAccount on heap] REACHABLE
account = null;                            // reference dropped!
// Now: nothing points to [BankAccount on heap] — UNREACHABLE → will be collected
```

### Generational Garbage Collection

Most objects die young. A new `StringBuilder` for a temporary string is used once and discarded. The JVM exploits this with **generational GC**:

```
GENERATIONAL HEAP
═══════════════════════════════════════════════════════════════
│  YOUNG GENERATION                           │  OLD GEN     │
│  ┌──────────────┐  ┌──────────┐ ┌─────────┐│ ┌──────────┐ │
│  │   EDEN       │  │Survivor 0│ │Survivor1││ │ Tenured  │ │
│  │  (new objects│  │          │ │         ││ │  Space   │ │
│  │  born here)  │  │          │ │         ││ │          │ │
│  └──────────────┘  └──────────┘ └─────────┘│ └──────────┘ │
═══════════════════════════════════════════════════════════════
```

- **Eden**: All new objects start here
- **Minor GC**: Fast! Cleans Eden and Survivor spaces. Most objects die here.
- **Survivor spaces**: Objects that survive Minor GC move here. Checked again next Minor GC.
- **Old Generation**: Objects that survive many Minor GCs are "promoted" here.
- **Major/Full GC**: Cleans the old generation. Slower. Avoid frequent Full GC!

### GC Algorithms (Modern JVM)

Java has evolved through several GC implementations:

- **Serial GC** (`-XX:+UseSerialGC`): Single-threaded. Good for small heaps.
- **Parallel GC** (default pre-Java 9): Multiple threads, stop-the-world.
- **G1 GC** (`-XX:+UseG1GC`, default since Java 9): Concurrent, predictable pause times.
- **ZGC** (`-XX:+UseZGC`, Java 15+): Very low pause times (< 1ms), good for large heaps.
- **Shenandoah**: Similar to ZGC, open-source.

---

## Object Reference Types

Java has four types of object references, differing in how strongly they hold objects from GC.

### Strong Reference (default)

```java
String s = "hello";  // strong reference
// s will NOT be garbage collected as long as this reference exists
```

### Soft Reference

Collected only when JVM is running low on memory. Good for caches:

```java
import java.lang.ref.SoftReference;

SoftReference<byte[]> cache = new SoftReference<>(new byte[1024 * 1024]); // 1MB

byte[] data = cache.get();
if (data == null) {
    // Cache was cleared by GC — reload
    data = loadDataFromDisk();
    cache = new SoftReference<>(data);
}
```

### Weak Reference

Collected at the next GC, even if memory is plentiful. Good for canonical maps:

```java
import java.lang.ref.WeakReference;

WeakReference<ExpensiveObject> ref = new WeakReference<>(new ExpensiveObject());

// Retrieve (might be null if GC ran)
ExpensiveObject obj = ref.get();
if (obj == null) {
    // Object was collected
}

// Real use case: WeakHashMap — entries auto-removed when keys become unreachable
WeakHashMap<Session, UserData> sessions = new WeakHashMap<>();
```

### Phantom Reference

The weakest — always returns `null` from `get()`. Used for cleanup actions before memory is freed. Advanced use only.

---

## OutOfMemoryError — When GC Can't Keep Up

`OutOfMemoryError: Java heap space` means the heap is full and GC can't free enough memory.

### Common Causes

**1. Memory Leak — Accumulating Objects**
```java
// CLASSIC MEMORY LEAK: Adding to a static list that never gets cleared
public class EventListener {
    private static final List<Event> ALL_EVENTS = new ArrayList<>();  // Never cleared!
    
    public void handle(Event e) {
        ALL_EVENTS.add(e);  // Keeps growing forever
    }
}
```

**2. Caching Without Limits**
```java
// Dangerous: unbounded cache
private static final Map<String, byte[]> cache = new HashMap<>();

public byte[] getImage(String url) {
    return cache.computeIfAbsent(url, this::downloadImage);
    // This NEVER evicts entries — will OOM eventually!
}

// Fix: Use a size-limited cache (e.g., LinkedHashMap with removeEldestEntry)
private static final Map<String, byte[]> cache = new LinkedHashMap<>(1000, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
        return size() > 1000;  // keep max 1000 entries
    }
};
```

**3. Holding References Too Long**
```java
// Bad: holding reference to parent object prevents GC of the whole thing
public class Report {
    private final DataSource ENTIRE_DATABASE;  // holds huge object
    private final String reportText;
    
    // Even after report is done, ENTIRE_DATABASE is held in memory!
}
```

**4. String Interning Abuse**
```java
for (String line : readMillionsOfLines()) {
    line.intern();  // All strings go to the permanent PermGen/Metaspace — can overflow!
}
```

### How to Detect Memory Leaks

1. **VisualVM** — free JVM profiler, comes with JDK
2. **JProfiler** — commercial, very powerful
3. **Heap dump analysis** — `jmap -dump:format=b,file=heap.hprof <pid>`

Look for: objects that keep growing, unexpected retention, large objects not being released.

---

## JVM Tuning Basics

### Heap Size Flags

```bash
# Set initial heap size to 512MB, max to 2GB
java -Xms512m -Xmx2g MyApplication

# Rule of thumb: set -Xms = -Xmx to prevent heap resizing overhead
java -Xms2g -Xmx2g MyApplication

# See current GC behavior
java -verbose:gc MyApplication
java -Xlog:gc* MyApplication  # more detailed (Java 9+)
```

### Common GC Tuning Flags

```bash
# Choose GC
java -XX:+UseG1GC MyApp          # G1GC (default Java 9+)
java -XX:+UseZGC MyApp           # ZGC (low latency)

# G1GC tuning
java -XX:MaxGCPauseMillis=200    # target max GC pause
java -XX:G1HeapRegionSize=16m    # larger regions for large heaps

# Heap composition
java -XX:NewRatio=2              # Old:Young ratio = 2:1
java -XX:SurvivorRatio=8         # Eden:Survivor ratio = 8:1
```

### When to Tune

1. **Measure first** — never tune blindly. Profile!
2. **High CPU from GC?** — increase heap, tune GC algorithm
3. **Long GC pauses?** — switch to G1GC/ZGC
4. **Frequent Full GC?** — likely a memory leak, not a tuning problem
5. **OOM?** — increase heap OR fix memory leak

---

## Summary

| Concept | Location | Lifetime |
|---------|----------|----------|
| Primitive local variables | Stack | Until method returns |
| Object references | Stack | Until method returns |
| Objects | Heap | Until GC collects |
| Static fields | Metaspace | Until class unloaded |
| Strong reference | Heap | Never collected |
| Soft reference | Heap | Collected when OOM |
| Weak reference | Heap | Collected next GC |

**Memory leak checklist:**
- Unbounded caches (`HashMap` that never evicts)
- Static collections that accumulate
- Listeners not removed after use
- Inner class holding reference to outer class
- ThreadLocal not removed after thread reuse
