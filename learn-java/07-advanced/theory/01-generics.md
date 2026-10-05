# Chapter 1: Generics

## The Problem Generics Solve

Cast your mind back to Java 1.4 (pre-generics). Collections existed, but they had no type information:

```java
// Java 1.4 — no generics
List names = new ArrayList();
names.add("Alice");
names.add("Bob");
names.add(42);              // No error! You could add ANYTHING.

// Retrieval required a cast
String name = (String) names.get(0);    // OK
String oops = (String) names.get(2);    // ClassCastException at RUNTIME!
```

The compiler couldn't help you. Bugs only appeared at runtime. Debugging a `ClassCastException` buried in a large application is painful.

**Generics** (introduced in Java 5) solve this by adding type parameters to classes, interfaces, and methods:

```java
// Java 5+ — with generics
List<String> names = new ArrayList<>();
names.add("Alice");
names.add("Bob");
names.add(42);              // COMPILE ERROR! Cannot add int to List<String>

String name = names.get(0); // No cast needed! Compiler KNOWS it's a String
```

The rule is simple: **Catch errors at compile time, not at runtime.**

---

## Generic Classes

A generic class is a class parameterized over one or more types.

### The Box Example

```java
// Without generics — requires casting, loses type info
public class Box {
    private Object value;
    
    public void set(Object value) { this.value = value; }
    public Object get() { return value; }
}

Box box = new Box();
box.set("Hello");
String s = (String) box.get();  // Works, but risky

// With generics — type-safe!
public class Box<T> {
    private T value;
    
    public void set(T value) { this.value = value; }
    public T get() { return value; }
}

Box<String> stringBox = new Box<>();
stringBox.set("Hello");
String s = stringBox.get();  // No cast! Compiler knows it's a String

Box<Integer> intBox = new Box<>();
intBox.set(42);
int n = intBox.get();        // Auto-unboxed, no cast needed
```

The `T` is a **type parameter** — a placeholder that the user fills in when they create an instance. By convention:
- `T` — Type (general purpose)
- `E` — Element (for collections)
- `K` — Key
- `V` — Value
- `N` — Number
- `R` — Return type

### Multiple Type Parameters

```java
// A Pair — holds two values of potentially different types
public class Pair<A, B> {
    private final A first;
    private final B second;
    
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }
    
    public A getFirst() { return first; }
    public B getSecond() { return second; }
    
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
    
    // Static factory method — convenient creation
    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }
}

// Usage
Pair<String, Integer> nameAge = Pair.of("Alice", 30);
String name = nameAge.getFirst();   // "Alice"
int age = nameAge.getSecond();      // 30

Pair<Double, Double> coordinates = Pair.of(51.5074, -0.1278); // London
```

---

## Generic Methods

A generic method has its own type parameters, independent of the class's type parameters.

```java
public class GenericUtils {
    
    // Generic method — T is defined on the method, not the class
    public static <T> T getFirst(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(0);
    }
    
    // Generic method with multiple type parameters
    public static <K, V> Map<V, K> invertMap(Map<K, V> map) {
        Map<V, K> inverted = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            inverted.put(entry.getValue(), entry.getKey());
        }
        return inverted;
    }
    
    // Generic swap — classic example
    public static <T> void swap(T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}

// Usage — Java infers the type parameters from context
List<String> names = List.of("Alice", "Bob", "Charlie");
String first = GenericUtils.getFirst(names);  // "Alice" — no cast!

// Or specify explicitly (rarely needed)
String firstExplicit = GenericUtils.<String>getFirst(names);
```

---

## Bounded Type Parameters

Sometimes you need to constrain what type T can be. You use **bounds** for this.

### Upper Bounded: `<T extends SomeType>`

"T must be SomeType or a subtype of SomeType"

```java
// Only makes sense for numbers — you need to call doubleValue()
public static <T extends Number> double sum(List<T> numbers) {
    double total = 0.0;
    for (T number : numbers) {
        total += number.doubleValue(); // Works! T is guaranteed to be a Number
    }
    return total;
}

sum(List.of(1, 2, 3));              // Integer is a Number — OK
sum(List.of(1.5, 2.5, 3.5));       // Double is a Number — OK
sum(List.of("a", "b", "c"));       // COMPILE ERROR! String is not a Number
```

```java
// Finds the maximum in a list — requires Comparable to use compareTo()
public static <T extends Comparable<T>> T findMax(List<T> items) {
    if (items.isEmpty()) throw new IllegalArgumentException("Empty list");
    
    T max = items.get(0);
    for (T item : items) {
        if (item.compareTo(max) > 0) {
            max = item;
        }
    }
    return max;
}

findMax(List.of(3, 1, 4, 1, 5, 9, 2, 6));  // Returns 9
findMax(List.of("banana", "apple", "cherry")); // Returns "cherry"
```

### Multiple Bounds

```java
// T must be both Serializable AND Comparable
public static <T extends Comparable<T> & Serializable> T findMin(List<T> items) {
    // ...
}
```

Note: A class bound must come first, interfaces after it.

---

## Wildcards: The Tricky Part

Wildcards are one of the most confusing features in Java generics. Let's demystify them completely.

### The Problem Wildcards Solve

Intuitively, if `Dog extends Animal`, shouldn't `List<Dog>` be a subtype of `List<Animal>`?

**No, and here's why:**

```java
List<Dog> dogs = new ArrayList<>();
List<Animal> animals = dogs;    // If this were allowed...
animals.add(new Cat());         // ...you could add a Cat to a List<Dog>!
Dog dog = dogs.get(0);          // ClassCastException!
```

This is called **invariance**. `List<Dog>` is NOT a subtype of `List<Animal>`.

But sometimes you genuinely want to work with a "list of some animals." That's where wildcards come in.

### The Unbounded Wildcard: `?`

`List<?>` means "a list of some unknown type."

```java
// Prints any list, regardless of type
public void printList(List<?> list) {
    for (Object item : list) {
        System.out.println(item);  // Safe — everything IS an Object
    }
}

printList(new ArrayList<String>());  // OK
printList(new ArrayList<Integer>()); // OK
printList(new ArrayList<Dog>());     // OK
```

But `List<?>` is essentially read-only — you can't add anything to it (except `null`) because you don't know what type it is.

### Upper Bounded Wildcard: `? extends T`

"A list of T or any subtype of T" — used when you want to READ from a collection.

```java
// Works with List<Number>, List<Integer>, List<Double>, etc.
public double sumNumbers(List<? extends Number> numbers) {
    double total = 0.0;
    for (Number n : numbers) {
        total += n.doubleValue();
    }
    return total;
}

List<Integer> ints = List.of(1, 2, 3);
List<Double> doubles = List.of(1.5, 2.5, 3.5);

sumNumbers(ints);     // Works!
sumNumbers(doubles);  // Works!
// sumNumbers(List.of("a")); // Compile error! String doesn't extend Number
```

**You can READ from `? extends T` but not WRITE to it.**

Why? Because you don't know the exact subtype. If it's `List<Integer>`, you can't add a `Double` to it, even though both extend `Number`.

### Lower Bounded Wildcard: `? super T`

"A list of T or any supertype of T" — used when you want to WRITE to a collection.

```java
// Add integers to a list of integers, numbers, or objects
public void addNumbers(List<? super Integer> list) {
    for (int i = 1; i <= 10; i++) {
        list.add(i);  // Safe! Integer can go into List<Integer>, List<Number>, or List<Object>
    }
}

List<Integer> ints = new ArrayList<>();
List<Number> numbers = new ArrayList<>();
List<Object> objects = new ArrayList<>();

addNumbers(ints);     // Works!
addNumbers(numbers);  // Works!
addNumbers(objects);  // Works!
```

**You can WRITE to `? super T` but reading gives you only `Object`.**

### PECS: The Memory Aid

**P**roducer **E**xtends, **C**onsumer **S**uper

- **Producer** = you read FROM it → use `? extends T`
- **Consumer** = you write TO it → use `? super T`

```java
// Classic example from Collections.copy():
// src is a PRODUCER (we read from it) → ? extends T
// dst is a CONSUMER (we write to it)  → ? super T
public static <T> void copy(List<? super T> dst, List<? extends T> src) {
    for (T element : src) {
        dst.add(element);
    }
}
```

This is exactly how `Collections.copy()` is implemented in the JDK!

---

## Type Erasure

Here's a dirty secret about Java generics: **at runtime, they don't exist.**

When the Java compiler compiles generic code, it **erases** the type parameters and replaces them with their bounds (or `Object` if unbounded). This process is called **type erasure**.

```java
// What you write:
public class Box<T> {
    private T value;
    public T get() { return value; }
}

// What the compiler generates (simplified):
public class Box {
    private Object value;  // T → Object (unbounded)
    public Object get() { return value; }
}
```

```java
// What you write:
public <T extends Number> double sum(List<T> numbers) { ... }

// What the compiler generates:
public double sum(List numbers) { ... }  // T → Number (bounded)
```

### Implications of Type Erasure

```java
// These two compile to the SAME method signature — won't compile!
public void process(List<String> list) { ... }
public void process(List<Integer> list) { ... }  // COMPILE ERROR: duplicate

// You can't do this:
if (list instanceof List<String>) { ... }   // COMPILE ERROR: generic type not available

// You can't do this:
T obj = new T();     // COMPILE ERROR: can't instantiate a type parameter

// You can't do this:
T[] array = new T[10];  // COMPILE ERROR: same reason

// But you CAN do this (with a cast warning):
@SuppressWarnings("unchecked")
T[] array = (T[]) new Object[10];
```

At runtime, `List<String>` and `List<Integer>` are the SAME class:

```java
List<String> strings = new ArrayList<>();
List<Integer> integers = new ArrayList<>();

System.out.println(strings.getClass() == integers.getClass()); // TRUE!
System.out.println(strings.getClass().getName()); // "java.util.ArrayList" (no generic info)
```

### Why Did Java Use Erasure?

Backwards compatibility. When generics were added in Java 5, the designers needed existing bytecode (Java 1-4) to interoperate with new generic code. Erasure made this possible — old code could pass `List` to methods expecting `List<String>` (with an "unchecked" warning).

This is a controversial decision. Many other languages (C#, Kotlin) have **reified** generics where type information is preserved at runtime.

---

## Common Generic Patterns

### Result<T> — Functional Error Handling

```java
/**
 * A Result type that either holds a success value or an error.
 * Avoids null returns and forces callers to handle errors.
 */
public class Result<T> {
    private final T value;
    private final String error;
    private final boolean success;
    
    private Result(T value, String error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }
    
    // Factory methods
    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, true);
    }
    
    public static <T> Result<T> failure(String error) {
        return new Result<>(null, error, false);
    }
    
    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }
    public T getValue() {
        if (!success) throw new IllegalStateException("Result is failure: " + error);
        return value;
    }
    public String getError() { return error; }
    
    // Functional operations
    public <R> Result<R> map(java.util.function.Function<T, R> mapper) {
        if (success) {
            return Result.success(mapper.apply(value));
        }
        return Result.failure(error);
    }
    
    public T orElse(T defaultValue) {
        return success ? value : defaultValue;
    }
    
    @Override
    public String toString() {
        return success ? "Success(" + value + ")" : "Failure(" + error + ")";
    }
}

// Usage:
Result<Integer> parseResult = parseInteger("42");
Result<Integer> errorResult = parseInteger("not a number");

System.out.println(parseResult);  // Success(42)
System.out.println(errorResult);  // Failure(Invalid integer: not a number)

// Chain operations safely
Result<String> doubledString = parseResult
    .map(n -> n * 2)
    .map(n -> "Value is: " + n);

System.out.println(doubledString); // Success(Value is: 84)
```

### Generic Stack

```java
public class Stack<T> {
    private final List<T> elements = new ArrayList<>();
    
    public void push(T element) {
        elements.add(element);
    }
    
    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        return elements.remove(elements.size() - 1);
    }
    
    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return elements.get(elements.size() - 1);
    }
    
    public boolean isEmpty() { return elements.isEmpty(); }
    public int size() { return elements.size(); }
    
    @Override
    public String toString() { return elements.toString(); }
}

Stack<String> words = new Stack<>();
words.push("Hello");
words.push("World");
System.out.println(words.pop()); // "World"
System.out.println(words.pop()); // "Hello"
```

---

## Summary

| Feature | Syntax | Use Case |
|---------|--------|----------|
| Generic class | `class Box<T>` | Type-safe container |
| Generic method | `<T> T method(T arg)` | Type-safe utility |
| Upper bound | `<T extends Number>` | Work with number types |
| Upper wildcard | `? extends Number` | Read from collection |
| Lower wildcard | `? super Integer>` | Write to collection |
| Unbounded wildcard | `?` | Works with any type |

**Remember PECS:** Producer Extends, Consumer Super.

Generics make your code both safer (compile-time errors instead of runtime crashes) and more expressive (no meaningless casts). Master them, and you'll write Java that reads like well-designed API code.
