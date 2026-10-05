# Java 8 Streams

## What Are Streams?

A Stream is a **pipeline for processing sequences of data** declaratively. Instead of writing loops that say *how* to process data step by step, streams let you describe *what* transformation you want.

```java
// BEFORE streams: imperative (tells the computer HOW)
List<String> result = new ArrayList<>();
for (Employee emp : employees) {
    if (emp.getDepartment().equals("Engineering")) {
        result.add(emp.getName().toUpperCase());
    }
}
Collections.sort(result);

// WITH streams: declarative (tells the computer WHAT)
List<String> result = employees.stream()
    .filter(emp -> emp.getDepartment().equals("Engineering"))
    .map(emp -> emp.getName().toUpperCase())
    .sorted()
    .collect(Collectors.toList());
```

Both do exactly the same thing. The stream version is shorter, reads like English, and is often easier to understand at a glance.

---

## Stream vs Collection

| Collection | Stream |
|------------|--------|
| Stores data | Processes data |
| Can be iterated multiple times | Can only be consumed ONCE |
| Can be modified (add/remove) | Immutable pipeline |
| Eager (data is in memory) | Lazy (processes on demand) |
| Has a size | May be infinite |

**Critical rule: A stream can only be consumed once.** After a terminal operation, the stream is "used up":

```java
Stream<String> stream = list.stream();
long count = stream.count();       // Terminal — stream is consumed
stream.forEach(System.out::println); // ❌ IllegalStateException: stream already operated upon!
```

---

## The Three Parts of a Stream Pipeline

```
Source → Intermediate Operations → Terminal Operation
  ↓              ↓                      ↓
Creates       Transform the           Produces a
the stream     stream lazily           result (one time)
```

```java
employees.stream()                        // SOURCE
    .filter(e -> e.salary() > 50000)      // INTERMEDIATE
    .map(Employee::name)                  // INTERMEDIATE
    .sorted()                             // INTERMEDIATE
    .collect(Collectors.toList());        // TERMINAL
```

**Intermediate operations are LAZY** — they don't do any work until a terminal operation is called. This is why streams can be efficient:

```java
// This doesn't process ANY employees until collect() is called:
Stream<String> pipeline = employees.stream()
    .filter(e -> e.salary() > 50000)  // Not executed yet
    .map(Employee::name);              // Not executed yet

// Now the pipeline executes — only ONE pass through the data!
List<String> names = pipeline.collect(Collectors.toList());
```

---

## Intermediate Operations

### filter(Predicate)

Keep only elements that match the condition:

```java
.filter(n -> n > 0)                          // Keep positive numbers
.filter(s -> s.startsWith("A"))              // Keep strings starting with A
.filter(Objects::nonNull)                    // Remove nulls
.filter(emp -> emp.department().equals("IT"))// Domain filter
```

### map(Function)

Transform each element to another type or value:

```java
.map(String::toUpperCase)                    // String → String (uppercase)
.map(String::length)                         // String → Integer
.map(Employee::name)                         // Employee → String (name)
.map(emp -> emp.salary() * 1.10)            // Employee → Double (salary+10%)
```

### flatMap(Function returning Stream)

Each element maps to a stream; the streams are then flattened into one:

```java
// Each order has multiple items — get all items across all orders
List<Item> allItems = orders.stream()
    .flatMap(order -> order.items().stream())  // Order → Stream<Item>
    .collect(Collectors.toList());

// Split each sentence into words
List<String> words = sentences.stream()
    .flatMap(sentence -> Arrays.stream(sentence.split(" ")))
    .collect(Collectors.toList());
```

### sorted()

```java
.sorted()                                    // Natural order
.sorted(Comparator.reverseOrder())           // Reverse natural order
.sorted(Comparator.comparing(Employee::name))// By a field
.sorted(Comparator.comparingDouble(Employee::salary).reversed()) // Desc by salary
```

### distinct()

Remove duplicates (uses `equals()`):

```java
List<Integer> unique = numbers.stream().distinct().collect(Collectors.toList());
```

### limit(long) and skip(long)

Pagination:

```java
.limit(10)          // First 10 elements
.skip(20)           // Skip first 20 elements
.skip(20).limit(10) // Elements 21–30 (page 3 of size 10)
```

### peek(Consumer)

For debugging — inspect elements without modifying them:

```java
.peek(e -> System.out.println("Before filter: " + e))
.filter(e -> e.salary() > 50000)
.peek(e -> System.out.println("After filter: " + e))
```

---

## Terminal Operations

### collect(Collector)

The most versatile terminal operation — gather elements into a collection or summary:

```java
// To collections
.collect(Collectors.toList())          // Mutable List
.collect(Collectors.toUnmodifiableList()) // Immutable List
.collect(Collectors.toSet())           // Set (no duplicates)
.collect(Collectors.toCollection(LinkedList::new)) // Specific collection type

// To Map
.collect(Collectors.toMap(
    Employee::id,        // Key function
    Employee::name       // Value function
))

// Handle duplicate keys
.collect(Collectors.toMap(
    Employee::department,
    Employee::name,
    (existing, incoming) -> existing + ", " + incoming  // Merge function for duplicates
))

// To String
.collect(Collectors.joining(", "))           // "Alice, Bob, Charlie"
.collect(Collectors.joining(", ", "[", "]")) // "[Alice, Bob, Charlie]"

// Grouping
.collect(Collectors.groupingBy(Employee::department))
// → Map<String, List<Employee>>

// Counting in groups
.collect(Collectors.groupingBy(Employee::department, Collectors.counting()))
// → Map<String, Long>

// Average in groups
.collect(Collectors.groupingBy(
    Employee::department,
    Collectors.averagingDouble(Employee::salary)
))

// Statistics
.collect(Collectors.summarizingDouble(Employee::salary))
// → DoubleSummaryStatistics{count=5, sum=350000, min=50000, average=70000, max=105000}
```

### forEach(Consumer)

```java
.forEach(System.out::println)
.forEach(emp -> logger.info("Processing: {}", emp.name()))
```

### count()

```java
long seniorDevs = employees.stream()
    .filter(e -> e.yearsExperience() > 5)
    .count();
```

### findFirst() and findAny()

```java
// Returns Optional<T> — may be empty if stream is empty or no match
Optional<Employee> found = employees.stream()
    .filter(e -> e.name().equals("Alice"))
    .findFirst();

found.ifPresent(e -> System.out.println("Found: " + e.name()));
String name = found.map(Employee::name).orElse("Not found");
```

### anyMatch, allMatch, noneMatch

Short-circuit: stop early once the answer is known:

```java
boolean hasHighEarner = employees.stream().anyMatch(e -> e.salary() > 100_000);
boolean allAdults = users.stream().allMatch(u -> u.age() >= 18);
boolean noneBlocked = users.stream().noneMatch(User::isBlocked);
```

### reduce(BinaryOperator)

Combine all elements into a single value:

```java
// Sum
int total = numbers.stream().reduce(0, Integer::sum);
int total = numbers.stream().reduce(0, (a, b) -> a + b);

// Max
Optional<Integer> max = numbers.stream().reduce(Integer::max);

// Product
int product = numbers.stream().reduce(1, (a, b) -> a * b);
```

For numeric types, prefer specialized streams:

```java
int sum = numbers.stream().mapToInt(Integer::intValue).sum();
double avg = numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0);
IntSummaryStatistics stats = numbers.stream().mapToInt(i -> i).summaryStatistics();
```

---

## Method References

Method references are shorthand for lambdas that just call a method:

```java
// Instance method reference on a specific object
System.out::println           // s -> System.out.println(s)

// Static method reference
Integer::parseInt             // s -> Integer.parseInt(s)
Math::abs                     // n -> Math.abs(n)

// Instance method reference on an arbitrary instance of a type
String::toUpperCase           // s -> s.toUpperCase()
String::length                // s -> s.length()
Employee::getName             // emp -> emp.getName()

// Constructor reference
ArrayList::new                // () -> new ArrayList<>()
Employee::new                 // args -> new Employee(args)
```

```java
// Examples in context
names.stream().map(String::toUpperCase).forEach(System.out::println);
employees.stream().map(Employee::name).collect(Collectors.toList());
strings.stream().filter(String::isBlank).count();
numbers.stream().map(Integer::parseInt).collect(Collectors.toList());
```

---

## Collectors.groupingBy Examples

One of the most powerful collectors:

```java
record Employee(String name, String dept, double salary) {}

List<Employee> employees = List.of(
    new Employee("Alice", "Engineering", 95000),
    new Employee("Bob",   "Marketing",   72000),
    new Employee("Eve",   "Engineering", 105000),
    new Employee("Frank", "HR",          65000)
);

// Basic grouping
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept));

// Count per group
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));

// Average salary per group
Map<String, Double> avgSalary = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::dept,
        Collectors.averagingDouble(Employee::salary)
    ));

// Names per department (downstream: map to name, collect to list)
Map<String, List<String>> namesByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::dept,
        Collectors.mapping(Employee::name, Collectors.toList())
    ));

// Multi-level grouping
Map<String, Map<Boolean, List<Employee>>> grouped = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::dept,
        Collectors.partitioningBy(e -> e.salary() > 80000)
    ));
```

---

## Parallel Streams

```java
// Sequential (default)
long count = employees.stream().filter(e -> e.salary() > 50000).count();

// Parallel — uses ForkJoinPool, processes chunks concurrently
long count = employees.parallelStream().filter(e -> e.salary() > 50000).count();
// OR
long count = employees.stream().parallel().filter(e -> e.salary() > 50000).count();
```

### When to Use Parallel Streams

**Use when:**
- Dataset is large (hundreds of thousands of elements)
- Operations are CPU-intensive and stateless
- No shared mutable state
- The operation is associative (order doesn't matter for correctness)

**Don't use when:**
- Dataset is small (overhead of parallelism exceeds benefit)
- Operations have I/O (parallel I/O is complex and usually wrong)
- Operations have side effects or shared mutable state
- You care about ordering
- You're already in a multi-threaded context (double parallelism can cause thread starvation)

```java
// WRONG: Side effects in parallel stream — race condition!
List<String> result = new ArrayList<>();
employees.parallelStream().forEach(e -> result.add(e.name())); // ❌ Not thread-safe!

// RIGHT: Use collect(), which is thread-safe
List<String> result = employees.parallelStream()
    .map(Employee::name)
    .collect(Collectors.toList()); // ✓ Thread-safe
```

---

## From 10 Lines to 3: Real Transformation

```java
// BEFORE: Find average salary of top 5 earners in Engineering
List<Employee> engineers = new ArrayList<>();
for (Employee e : employees) {
    if (e.department().equals("Engineering")) {
        engineers.add(e);
    }
}
engineers.sort(Comparator.comparingDouble(Employee::salary).reversed());
List<Employee> top5 = engineers.subList(0, Math.min(5, engineers.size()));
double sum = 0;
for (Employee e : top5) {
    sum += e.salary();
}
double avg = top5.isEmpty() ? 0 : sum / top5.size();

// AFTER: Same thing with streams
double avg = employees.stream()
    .filter(e -> e.department().equals("Engineering"))
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .limit(5)
    .mapToDouble(Employee::salary)
    .average()
    .orElse(0);
```
