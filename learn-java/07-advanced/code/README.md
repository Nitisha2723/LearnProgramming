# Code Examples — Module 07: Advanced Java

## Files

| File | What It Demonstrates |
|------|---------------------|
| `GenericsDemo.java` | Generic Stack, Pair, Result type, bounded types, wildcards, PECS |
| `FunctionalDemo.java` | Lambdas, method references, Stream API, function composition, Optional |
| `ConcurrencyDemo.java` | Race conditions, synchronized, AtomicInteger, ExecutorService, CompletableFuture |

## How to Compile and Run

```bash
# From the 07-advanced/code/ directory
cd code/

# Compile all files
javac GenericsDemo.java
javac FunctionalDemo.java
javac ConcurrencyDemo.java

# Run
java GenericsDemo
java FunctionalDemo
java ConcurrencyDemo
```

All files use only the Java standard library — no external dependencies needed.

## What to Look For

### GenericsDemo.java
- The `Stack<T>` class shows how type parameters work in practice
- `Pair<A,B>` demonstrates multiple type parameters
- `Result<T>` is a practical pattern you'll see in real codebases (like Rust's Result type)
- The wildcard section (`? extends`, `? super`) directly demonstrates PECS

### FunctionalDemo.java
- `demonstrateLambdas()` — notice how lambdas reduce boilerplate
- `demonstrateStreams()` — the employee analytics section shows real-world Stream use
- `demonstrateComposition()` — see how complex functions are built from simple ones
- `demonstrateOptional()` — compare the imperative null-check chain with the Optional chain

### ConcurrencyDemo.java
- `demonstrateRaceCondition()` — run it several times; the UNSAFE result changes!
- `demonstrateCompletableFuture()` — notice the timing difference between sequential and parallel
- `demonstrateThreadSafeCollections()` — shows thread-safe alternatives to HashMap/ArrayList
