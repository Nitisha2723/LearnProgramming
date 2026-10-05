# Chapter 5: Performance and Profiling

## The Cardinal Rule

**"Premature optimization is the root of all evil."** — Donald Knuth (1974)

This quote is among the most repeated in software engineering, and among the most misunderstood. The full quote is:

> "We should forget about small efficiencies, say about 97% of the time: premature optimization is the root of all evil. Yet we should not pass up our opportunities in that critical 3%."

The lesson: **first make it correct, then make it fast — and only if speed actually matters.**

Here's what happens when developers optimize without measuring:

1. They optimize code that isn't the bottleneck
2. They make the code harder to read and maintain
3. The actual bottleneck (usually I/O or a database query) remains untouched
4. The program is still slow, but now it's also unmaintainable

---

## Measure Before Optimizing

### What to Measure

You need to identify the **hot path** — the code that actually consumes the most time or memory.

**Response time breakdown** for a typical web application:
```
Total request: 800ms
├── Network latency:     50ms  (6%)
├── Application code:    50ms  (6%)
├── Database queries:   680ms  (85%) ← THIS IS YOUR BOTTLENECK
└── Serialization:       20ms  (3%)
```

Optimizing the application code would save 50ms at most. Optimizing the database query could save 680ms. Where would you focus?

### Profiling with VisualVM

VisualVM is free and ships with the JDK. It shows:
- **CPU time per method** — where is time being spent?
- **Memory allocation** — what objects are being created?
- **Thread activity** — are threads waiting or doing work?
- **Heap usage over time** — is memory growing unboundedly?

```bash
# VisualVM is in your JDK's bin directory
jvisualvm

# Or use JConsole (simpler)
jconsole
```

**Workflow:**
1. Start your application
2. Connect VisualVM to the JVM process
3. Reproduce the slow operation
4. Look at the CPU sampler/profiler
5. Find the method(s) consuming the most time
6. Optimize THOSE methods

### Profiling with async-profiler

For low-overhead production profiling:
```bash
# Record CPU profile for 30 seconds
./profiler.sh -d 30 -f profile.html <pid>
```

---

## Common Java Performance Pitfalls

### 1. String Concatenation in Loops

`String` is immutable in Java. Every `+` creates a new String object.

```java
// BAD: O(n²) — creates thousands of String objects
String result = "";
for (String item : largeList) {
    result += item + ", ";  // New String created every iteration!
}

// GOOD: StringBuilder is mutable — no extra objects
StringBuilder sb = new StringBuilder();
for (String item : largeList) {
    sb.append(item).append(", ");
}
String result = sb.toString();  // One final String

// Or even better for joining:
String result = String.join(", ", largeList);

// Streams join:
String result = largeList.stream().collect(Collectors.joining(", "));
```

**Rule of thumb**: If you concatenate in a loop, use `StringBuilder`. If it's just a few literals or variables, `+` is fine — the compiler optimizes it.

### 2. Unnecessary Object Creation

```java
// BAD: creates a new Integer object each comparison
List<Integer> numbers = new ArrayList<>();
for (int i = 0; i < 1_000_000; i++) {
    numbers.add(i);  // autoboxing: int → Integer → heap allocation!
}

// BETTER for numeric data: use primitive arrays or IntStream
int[] numbers = IntStream.range(0, 1_000_000).toArray();

// For large numeric collections: consider Eclipse Collections or Trove
// which provide IntList, LongList, etc. with no boxing
```

```java
// BAD: new Date() in a tight loop
for (Record record : records) {
    record.setLastUpdated(new Date());  // new object every iteration
}

// BETTER: use a single timestamp
long now = System.currentTimeMillis();
for (Record record : records) {
    record.setLastUpdated(now);
}
```

### 3. Autoboxing Overhead

```java
// BAD: implicit boxing/unboxing
Long sum = 0L;  // Long, not long
for (long i = 0; i < 1_000_000; i++) {
    sum += i;  // unbox sum, add i, box result back to Long — EVERY ITERATION
}

// GOOD: use primitive
long sum = 0L;
for (long i = 0; i < 1_000_000; i++) {
    sum += i;  // pure primitive arithmetic — no boxing
}
```

### 4. Inefficient Collection Operations

```java
// BAD: O(n) contains() on a List
List<String> validCodes = getValidCodes();  // could have thousands
for (String code : inputCodes) {
    if (validCodes.contains(code)) {  // O(n) search each time!
        process(code);
    }
}

// GOOD: O(1) lookup with HashSet
Set<String> validCodeSet = new HashSet<>(getValidCodes());
for (String code : inputCodes) {
    if (validCodeSet.contains(code)) {  // O(1) hash lookup!
        process(code);
    }
}
```

### 5. Closing Resources

Not closing resources causes leaks AND performance degradation:

```java
// BAD: connection leaked if exception occurs
Connection conn = dataSource.getConnection();
PreparedStatement stmt = conn.prepareStatement(sql);
ResultSet rs = stmt.executeQuery();
// ... if an exception occurs above, conn is never closed!

// GOOD: try-with-resources
try (Connection conn = dataSource.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql);
     ResultSet rs = stmt.executeQuery()) {
    // process results
} // automatically closed, even on exception
```

---

## Caching Strategies

"The fastest operation is the one you don't have to do." Caching stores results of expensive operations for reuse.

### In-Memory Caching

```java
// Simple manual cache with LinkedHashMap (LRU eviction)
public class UserCache {
    private static final int MAX_SIZE = 1000;
    
    private final Map<Long, User> cache = new LinkedHashMap<>(MAX_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, User> eldest) {
            return size() > MAX_SIZE;  // remove oldest when over limit
        }
    };
    
    public User getUser(Long id) {
        return cache.computeIfAbsent(id, this::loadFromDatabase);
    }
    
    private User loadFromDatabase(Long id) {
        // expensive database call
        return userRepository.findById(id);
    }
}
```

### Memoization (Cache Function Results)

```java
// Cache expensive function results
public class FibonacciCalculator {
    private final Map<Integer, Long> memo = new HashMap<>();
    
    public long fib(int n) {
        if (n <= 1) return n;
        return memo.computeIfAbsent(n, k -> fib(k - 1) + fib(k - 2));
    }
}

// Without memoization: O(2^n) — incredibly slow
// With memoization: O(n) — fast
```

### Cache-Aside Pattern

```java
public User findUser(long id) {
    // 1. Check cache first
    User cached = cache.get(id);
    if (cached != null) return cached;
    
    // 2. Cache miss — load from database
    User user = database.findById(id);
    
    // 3. Store in cache for next time
    cache.put(id, user);
    
    return user;
}
```

### Cache Invalidation

> "There are only two hard things in Computer Science: cache invalidation and naming things." — Phil Karlton

When the underlying data changes, cached data becomes stale:

```java
public void updateUser(User user) {
    database.save(user);
    cache.remove(user.getId());  // Invalidate cache — next read will reload
    // Or: cache.put(user.getId(), user);  // Update cache immediately
}
```

---

## Connection Pooling

Creating a database connection is expensive (50-100ms typically). Connection pools maintain a set of reusable connections.

```java
// Without connection pool: new connection for every query
public User findUser(long id) {
    Connection conn = DriverManager.getConnection(url, user, password); // EXPENSIVE!
    // ... query ...
    conn.close();
}

// With connection pool (HikariCP is the most popular)
// Configuration (typically in application.properties):
// spring.datasource.hikari.maximum-pool-size=20
// spring.datasource.hikari.minimum-idle=5

public User findUser(long id) {
    // getConnection() returns a pooled connection instantly
    try (Connection conn = dataSource.getConnection()) {
        // ... query ...
    }  // "close" returns connection to pool, not actually closed
}
```

Pool sizing rule of thumb: `connections = (CPU_cores * 2) + disk_spindles`
For a typical server: 4 cores → ~10 connections

---

## Writing Performance Tests with JMH

JMH (Java Microbenchmark Harness) is the standard tool for measuring the performance of small Java code:

```java
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class StringConcatBenchmark {
    
    private List<String> items;
    
    @Setup
    public void setup() {
        items = IntStream.range(0, 1000)
            .mapToObj(i -> "item" + i)
            .collect(Collectors.toList());
    }
    
    @Benchmark
    public String concatenationBad() {
        String result = "";
        for (String item : items) {
            result += item;
        }
        return result;
    }
    
    @Benchmark
    public String stringBuilderGood() {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item);
        }
        return sb.toString();
    }
}
```

JMH handles JVM warmup, prevents dead code elimination, and produces statistically valid results. Without it, "microbenchmarks" are usually wrong.

**Never microbenchmark with `System.currentTimeMillis()` in a loop** — JVM warmup, JIT compilation, and GC pauses make those results meaningless.

---

## Performance Optimization Workflow

```
1. DEFINE success criteria
   "Page load < 200ms at 95th percentile"

2. BASELINE
   Measure current performance

3. PROFILE
   Find the actual bottleneck (it's almost always I/O)

4. OPTIMIZE
   Fix the bottleneck

5. VERIFY
   Measure again — did it improve?

6. REPEAT
   Until you meet the criteria
```

### Quick Wins (in order of impact)

1. **Add a missing database index** — can turn 10s query into 10ms
2. **Fix N+1 query problem** — loading 100 records with 101 queries → 1 query
3. **Add caching** — skip expensive computation for repeated inputs
4. **Use connection pooling** — avoid connection setup overhead
5. **Switch to async I/O** — don't block threads waiting for network
6. **String builder in loops** — minor but easy
7. **Use primitives** — minor, only matters in tight numeric loops

---

## Summary

| Pitfall | Fix | Impact |
|---------|-----|--------|
| N+1 queries | Batch queries, JOIN in SQL | Very high |
| No index | Add database index | Very high |
| No caching | Add cache layer | High |
| String concat in loop | StringBuilder | Medium |
| Autoboxing in loops | Use primitive types | Medium |
| List.contains() | Use HashSet | Medium |
| Connection creation | Connection pool | High |
| Unclosed resources | try-with-resources | Medium |

**The golden rule**: Measure → Profile → Fix the actual bottleneck → Measure again.

Code that you don't ship has zero performance. Ship it right first, then make it fast.
