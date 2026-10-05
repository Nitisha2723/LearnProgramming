# Chapter 2: Functional Programming in Java

## What Is Functional Programming?

Functional programming (FP) is a style of programming that treats computation as the evaluation of mathematical functions. The core ideas:

1. **Pure functions** — given the same inputs, always return the same output. No hidden state changes.
2. **Immutability** — data doesn't change; you create new data instead.
3. **Function composition** — build complex operations from simple ones.
4. **First-class functions** — functions are values that can be passed around.

Java isn't a pure functional language (it's object-oriented first), but Java 8 added powerful FP features that make your code significantly cleaner and more expressive.

---

## Why Bother?

Compare these two approaches to filtering and transforming a list:

```java
// Imperative (old way)
List<String> result = new ArrayList<>();
for (Employee emp : employees) {
    if (emp.getSalary() > 50000) {
        result.add(emp.getName().toUpperCase());
    }
}

// Functional (modern Java)
List<String> result = employees.stream()
    .filter(emp -> emp.getSalary() > 50000)
    .map(emp -> emp.getName().toUpperCase())
    .collect(Collectors.toList());
```

The functional version reads like English: "filter employees with salary > 50000, then map each to their uppercase name." The imperative version requires you to mentally simulate the loop.

Functional code tends to be:
- **Shorter** — less boilerplate
- **More readable** — expresses WHAT, not HOW
- **Safer** — immutability prevents subtle state bugs
- **More testable** — pure functions are trivially testable

---

## Lambda Expressions

A **lambda expression** is an anonymous function — a function without a name, written inline.

### Syntax

```java
// Full syntax
(int x, int y) -> { return x + y; }

// Type inference — Java infers parameter types from context
(x, y) -> { return x + y; }

// Single expression — no braces, no return
(x, y) -> x + y

// Single parameter — parentheses optional
x -> x * 2

// No parameters
() -> System.out.println("Hello!")
() -> 42
```

### The Evolution from Anonymous Classes

Before lambdas, you wrote anonymous inner classes for callbacks:

```java
// Java 7 — anonymous class (verbose)
button.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Button clicked!");
    }
});

// Java 8+ — lambda (concise)
button.addActionListener(e -> System.out.println("Button clicked!"));
```

A lambda is essentially a shorthand for an anonymous class that implements a **functional interface**.

### Capturing Variables

Lambdas can "capture" variables from their enclosing scope, but those variables must be **effectively final** (either declared `final` or never reassigned):

```java
String greeting = "Hello";  // effectively final

Runnable r = () -> System.out.println(greeting + " World!");
r.run();  // "Hello World!"

// This would NOT compile:
String message = "Hi";
message = "Hey";  // reassignment makes it NOT effectively final
Runnable r2 = () -> System.out.println(message);  // COMPILE ERROR
```

---

## Functional Interfaces

A **functional interface** is an interface with exactly one abstract method. It can be implemented by a lambda.

Java annotates them with `@FunctionalInterface` to enforce this constraint:

```java
@FunctionalInterface
public interface MyFunction {
    int apply(int x);
    // You can't add another abstract method — compiler error
}

MyFunction doubler = x -> x * 2;
System.out.println(doubler.apply(5));  // 10
```

### The Built-in Functional Interfaces

Java provides a rich set in `java.util.function`:

#### `Function<T, R>` — Transform a value

```java
Function<String, Integer> length = s -> s.length();
Function<Integer, String> intToString = n -> "Number: " + n;

System.out.println(length.apply("Hello"));        // 5
System.out.println(intToString.apply(42));         // "Number: 42"

// Composition: f.andThen(g) = g(f(x))
Function<String, String> getLengthString = length.andThen(intToString);
System.out.println(getLengthString.apply("Hello")); // "Number: 5"
```

#### `BiFunction<T, U, R>` — Transform two values

```java
BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
System.out.println(repeat.apply("Ha", 3));  // "HaHaHa"
```

#### `Predicate<T>` — Test a condition

```java
Predicate<String> isLong = s -> s.length() > 10;
Predicate<String> startsWithA = s -> s.startsWith("A");

System.out.println(isLong.test("Hello"));          // false
System.out.println(isLong.test("Hello, World!"));  // true

// Combining predicates
Predicate<String> longAndStartsWithA = isLong.and(startsWithA);
Predicate<String> longOrStartsWithA = isLong.or(startsWithA);
Predicate<String> notLong = isLong.negate();

System.out.println(longAndStartsWithA.test("Amazing discovery!"));  // true
```

#### `Consumer<T>` — Consume without returning

```java
Consumer<String> print = System.out::println;  // method reference!
Consumer<String> printUppercase = s -> System.out.println(s.toUpperCase());

print.accept("Hello");         // "Hello"
printUppercase.accept("Hello"); // "HELLO"

// Chain consumers: first printUppercase, then print
Consumer<String> both = printUppercase.andThen(print);
both.accept("Hello");  // "HELLO" then "Hello"
```

#### `Supplier<T>` — Produce without taking input

```java
Supplier<List<String>> listFactory = ArrayList::new;  // creates new ArrayList each time
Supplier<Double> random = Math::random;
Supplier<LocalDateTime> now = LocalDateTime::now;

List<String> newList = listFactory.get();
System.out.println(random.get());  // some random number
```

#### `UnaryOperator<T>` — Transform a value to the same type

```java
UnaryOperator<String> trim = String::trim;
UnaryOperator<String> uppercase = String::toUpperCase;
UnaryOperator<Integer> square = n -> n * n;

// compose is the reverse of andThen: g(f(x)) vs f(g(x))
UnaryOperator<String> trimAndUppercase = trim.andThen(uppercase)::apply;
System.out.println(trimAndUppercase.apply("  hello  ")); // "HELLO"
```

#### `BinaryOperator<T>` — Combine two values of the same type

```java
BinaryOperator<Integer> add = Integer::sum;
BinaryOperator<String> concat = String::concat;
BinaryOperator<Integer> max = Integer::max;

System.out.println(add.apply(3, 4));        // 7
System.out.println(max.apply(10, 20));      // 20
```

---

## Method References

Method references are a shorthand for lambdas that just call a single method. There are four types:

### 1. Static Method Reference: `ClassName::staticMethod`

```java
// Lambda
Function<String, Integer> parse = s -> Integer.parseInt(s);

// Method reference (cleaner)
Function<String, Integer> parse = Integer::parseInt;

System.out.println(parse.apply("42"));  // 42
```

### 2. Instance Method of a Specific Object: `instance::method`

```java
String greeting = "Hello!";
Supplier<String> getGreeting = greeting::toUpperCase;

System.out.println(getGreeting.get());  // "HELLO!"
```

### 3. Instance Method of an Arbitrary Object: `ClassName::instanceMethod`

This is the trickiest one. The first argument becomes the receiver:

```java
// Lambda: the first arg (s) becomes the object we call toLowerCase() on
Function<String, String> lower = s -> s.toLowerCase();

// Method reference: String::toLowerCase means (s) -> s.toLowerCase()
Function<String, String> lower = String::toLowerCase;

// In a stream:
List<String> names = List.of("ALICE", "BOB", "CHARLIE");
List<String> lowered = names.stream()
    .map(String::toLowerCase)  // same as .map(s -> s.toLowerCase())
    .collect(Collectors.toList());
```

### 4. Constructor Reference: `ClassName::new`

```java
Supplier<ArrayList<String>> listFactory = ArrayList::new;
List<String> list = listFactory.get();

// With a function:
Function<String, StringBuilder> sbFactory = StringBuilder::new;
StringBuilder sb = sbFactory.apply("initial content");
```

---

## The Stream API — In Depth

Streams are a pipeline for processing sequences of data. They're lazy — work is only done when a terminal operation is reached.

```
Source → [intermediate ops] → [intermediate ops] → terminal op
                         (lazy — nothing runs until terminal op)
```

### Creating Streams

```java
// From a collection
List<Integer> nums = List.of(1, 2, 3, 4, 5);
Stream<Integer> stream = nums.stream();

// Infinite stream
Stream<Integer> naturals = Stream.iterate(1, n -> n + 1);
Stream<Double> randoms = Stream.generate(Math::random);

// From values
Stream<String> stream = Stream.of("a", "b", "c");

// IntStream, LongStream, DoubleStream (avoid boxing overhead)
IntStream range = IntStream.range(1, 100);  // 1 to 99
IntStream rangeClosed = IntStream.rangeClosed(1, 100); // 1 to 100
```

### Intermediate Operations (lazy)

```java
List<Employee> employees = getEmployees();

employees.stream()
    .filter(e -> e.getSalary() > 50000)             // keep matching
    .map(Employee::getName)                          // transform
    .filter(name -> name.startsWith("A"))           // filter again
    .map(String::toUpperCase)                        // transform
    .sorted()                                        // sort alphabetically
    .distinct()                                      // remove duplicates
    .limit(10)                                       // take first 10
    .peek(name -> System.out.println("Processing: " + name)) // debug
    .collect(Collectors.toList());
```

#### `flatMap` — Flattening Nested Structures

```java
// map produces a Stream<Stream<T>>, flatMap produces a Stream<T>
List<List<Integer>> nested = List.of(
    List.of(1, 2, 3),
    List.of(4, 5),
    List.of(6, 7, 8, 9)
);

List<Integer> flat = nested.stream()
    .flatMap(Collection::stream)     // flatten each inner list
    .collect(Collectors.toList());
// [1, 2, 3, 4, 5, 6, 7, 8, 9]

// Real-world: get all skills from all employees
List<String> allSkills = employees.stream()
    .flatMap(e -> e.getSkills().stream())
    .distinct()
    .sorted()
    .collect(Collectors.toList());
```

### Terminal Operations

```java
// Collect to a list, set, or map
List<String> list = stream.collect(Collectors.toList());
Set<String> set = stream.collect(Collectors.toSet());
Map<String, Integer> map = stream.collect(
    Collectors.toMap(Employee::getName, Employee::getSalary)
);

// Reduce to a single value
Optional<Integer> sum = Stream.of(1, 2, 3, 4).reduce(Integer::sum);
int sumWithIdentity = Stream.of(1, 2, 3, 4).reduce(0, Integer::sum);

// Count
long count = employees.stream().filter(e -> e.isActive()).count();

// Min/Max
Optional<Employee> highestPaid = employees.stream()
    .max(Comparator.comparing(Employee::getSalary));

// Any/All/None match
boolean anyHighPaid = employees.stream().anyMatch(e -> e.getSalary() > 100000);
boolean allActive = employees.stream().allMatch(Employee::isActive);
boolean noneNegative = salaries.stream().noneMatch(s -> s < 0);

// ForEach
employees.stream().forEach(e -> System.out.println(e.getName()));

// toArray
Employee[] array = employees.stream().toArray(Employee[]::new);
```

### Advanced Collectors

```java
// Group by department
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// Count per department
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

// Average salary by department
Map<String, Double> avgSalaryByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.averagingDouble(Employee::getSalary)
    ));

// Partition into two groups
Map<Boolean, List<Employee>> partitioned = employees.stream()
    .collect(Collectors.partitioningBy(e -> e.getSalary() > 70000));
List<Employee> highPaid = partitioned.get(true);
List<Employee> regularPaid = partitioned.get(false);

// Join strings
String names = employees.stream()
    .map(Employee::getName)
    .collect(Collectors.joining(", ", "[", "]"));
// "[Alice, Bob, Charlie]"
```

### Parallel Streams

```java
// Sequential vs parallel — just add .parallel()
long count = employees.parallelStream()
    .filter(e -> e.getSalary() > 50000)
    .count();
```

**When to use parallel streams:**
- Large data sets (thousands+ elements)
- CPU-intensive operations (not I/O bound)
- The operations are truly stateless
- Order doesn't matter

**Don't use parallel streams for:**
- Small collections (overhead exceeds benefit)
- I/O operations
- Operations with side effects
- When order matters and you can't reorder

---

## Function Composition

Building complex functions from simple ones:

```java
// andThen: f.andThen(g) = g(f(x))
Function<Integer, Integer> times2 = x -> x * 2;
Function<Integer, Integer> plus3 = x -> x + 3;

Function<Integer, Integer> times2ThenPlus3 = times2.andThen(plus3);
System.out.println(times2ThenPlus3.apply(5));  // (5 * 2) + 3 = 13

// compose: f.compose(g) = f(g(x))
Function<Integer, Integer> plus3ThenTimes2 = times2.compose(plus3);
System.out.println(plus3ThenTimes2.apply(5));  // (5 + 3) * 2 = 16

// Real-world example: data validation and transformation pipeline
Function<String, String> trim = String::trim;
Function<String, String> lowercase = String::toLowerCase;
Function<String, Boolean> validate = s -> s.length() >= 3;

Function<String, Boolean> normalizeAndValidate = trim
    .andThen(lowercase)
    .andThen(validate);

System.out.println(normalizeAndValidate.apply("  Alice  "));  // true
System.out.println(normalizeAndValidate.apply("  AB  "));     // false (too short)
```

---

## Optional — Safe Null Handling

`Optional<T>` is a container that may or may not hold a value. It forces you to explicitly handle the "no value" case, eliminating NullPointerExceptions.

```java
// Creating Optional
Optional<String> present = Optional.of("Hello");       // definitely has a value
Optional<String> absent = Optional.empty();            // definitely has no value
Optional<String> maybe = Optional.ofNullable(getName()); // might be null

// Checking and getting
if (present.isPresent()) {
    System.out.println(present.get());
}

// Preferred: functional style
present.ifPresent(System.out::println);  // prints if present

// Default values
String value = absent.orElse("Default");           // "Default"
String computed = absent.orElseGet(() -> compute()); // computed lazily

// Throwing if absent
String result = absent.orElseThrow(() ->
    new IllegalStateException("Expected a value!"));

// Transforming (map)
Optional<Integer> length = present.map(String::length);
System.out.println(length);  // Optional[5]

// Flat-mapping nested optionals
Optional<User> user = findUser(id);
Optional<String> email = user.flatMap(User::getOptionalEmail);

// Filtering
Optional<String> longName = present.filter(s -> s.length() > 3);
```

### The Real Power of Optional

```java
// WITHOUT Optional — chains of null checks (classic Java pain)
User user = userRepository.findById(userId);
if (user != null) {
    Address address = user.getAddress();
    if (address != null) {
        City city = address.getCity();
        if (city != null) {
            return city.getName();
        }
    }
}
return "Unknown";

// WITH Optional — clean, readable
return userRepository.findById(userId)
    .map(User::getAddress)
    .map(Address::getCity)
    .map(City::getName)
    .orElse("Unknown");
```

---

## Custom Functional Interfaces

Sometimes the built-in functional interfaces aren't enough. Create your own:

```java
@FunctionalInterface
public interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;
    
    // Static utility to wrap a ThrowingFunction in a regular Function
    static <T, R> Function<T, R> wrap(ThrowingFunction<T, R> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}

// Usage: parse files without checked exception noise in stream
List<Path> paths = getFilePaths();
List<String> contents = paths.stream()
    .map(ThrowingFunction.wrap(Files::readString))
    .collect(Collectors.toList());
```

---

## Why FP Makes Code More Testable

Pure functions are the easiest things to test:

```java
// Pure function — same input always gives same output
public static int add(int a, int b) {
    return a + b;
}

// Testing is trivial
@Test
void testAdd() {
    assertEquals(5, add(2, 3));
    assertEquals(0, add(-1, 1));
}

// Impure function — depends on external state
public int generateId() {
    return database.getNextId();  // depends on database!
}

// Testing is hard — needs a real/mock database
```

When your logic is composed of pure functions and immutable data, testing becomes:
1. Set up input data
2. Call the function
3. Assert the output

No mocking, no setup, no teardown.

---

## Summary

| Feature | Syntax | Use |
|---------|--------|-----|
| Lambda | `x -> x * 2` | Inline anonymous function |
| Method ref (static) | `Integer::parseInt` | Call static method |
| Method ref (instance) | `String::toLowerCase` | Call instance method |
| Method ref (constructor) | `ArrayList::new` | Create objects |
| Function | `Function<T,R>` | Transform T → R |
| Predicate | `Predicate<T>` | Test condition |
| Consumer | `Consumer<T>` | Side effect on T |
| Supplier | `Supplier<T>` | Produce T |
| Stream | `.stream().filter().map().collect()` | Pipeline processing |
| Optional | `Optional.ofNullable(x).map(f).orElse(default)` | Null-safe chaining |

Functional programming in Java isn't about abandoning objects — it's about choosing the right tool. Use objects for modeling domain concepts, use functions for transformations and business logic.
