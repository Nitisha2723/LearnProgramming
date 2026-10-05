# Code Examples — Module 06

This directory contains runnable Java examples for every principle and pattern covered in the module. Examples are organised into two categories: `solid/` for SOLID principle demonstrations, and `patterns/` for design pattern implementations.

---

## How Examples Are Organised

### SOLID Examples: Violation vs Correct

Every SOLID principle has a pair of files that show the *same scenario* implemented badly and then well:

```
solid/
└── srp/
    ├── ViolationExample.java   ← Breaks the principle (labelled clearly)
    └── CorrectExample.java     ← Applies the principle correctly
```

Read the `ViolationExample` first. Understand *why* it is a problem — what maintenance burden does it create? Then read `CorrectExample` and notice how the design is more flexible, testable, and clear.

### Pattern Examples

Pattern examples show a real-world usage scenario:

```
patterns/
└── strategy/
    └── SortingStrategy.java   ← The pattern in context
```

Some patterns have multiple files representing the various participants (interface, concrete implementations, context class).

---

## Compiling and Running

All examples are standalone — no build tool is required. Use `javac` and `java` directly.

### Compile a single example

```bash
# From the module root (06-design/)
javac -d out code/solid/srp/ViolationExample.java
javac -d out code/solid/srp/CorrectExample.java

java -cp out ViolationExample
java -cp out CorrectExample
```

### Compile all examples at once

```bash
# From the module root
find code -name "*.java" -print | xargs javac -d out 2>&1
```

If compilation fails on some files due to missing dependencies between files in the same directory, compile the whole directory together:

```bash
javac -d out code/solid/srp/*.java
javac -d out code/patterns/observer/*.java
```

### Run a specific example

```bash
java -cp out CorrectExample
java -cp out SortingStrategy
```

---

## Quick Index of All Examples

### SOLID Principles

| Directory | Principle | What It Demonstrates |
|---|---|---|
| `solid/srp/` | Single Responsibility | `UserManager` god-class split into `UserRepository`, `EmailService`, `PasswordService` |
| `solid/ocp/` | Open/Closed | Area calculator extended for new shapes without modifying existing code |
| `solid/lsp/` | Liskov Substitution | `Square extends Rectangle` breaks LSP; fixed with separate hierarchy |
| `solid/isp/` | Interface Segregation | Fat `Machine` interface split into `Printer`, `Scanner`, `Fax` |
| `solid/dip/` | Dependency Inversion | `OrderService` depends on `PaymentGateway` interface, not `StripePaymentGateway` |

### Design Patterns

| Directory | Pattern | Scenario |
|---|---|---|
| `patterns/singleton/` | Singleton | `DatabaseConnectionPool` — one pool per JVM, thread-safe initialisation |
| `patterns/factory/` | Factory Method | `PaymentProcessorFactory` — creates `StripeProcessor`, `PayPalProcessor`, etc. based on type |
| `patterns/builder/` | Builder | `HttpRequestBuilder` — fluent API for constructing complex HTTP requests |
| `patterns/observer/` | Observer | `StockPriceObserver` — multiple dashboards react to price changes |
| `patterns/strategy/` | Strategy | `SortingStrategy` — swap bubble sort, quicksort, merge sort at runtime |
| `patterns/decorator/` | Decorator | (see `theory/03-structural-patterns.md` for the full example) |
| `patterns/repository/` | Repository | (see `theory/06-architecture-patterns.md` for the full example) |

---

## Learning Approach

Work through examples in this order:

1. **Read the violation first.** Try to articulate the problem before reading the solution. "If I needed to add feature X, what would I have to change?"

2. **Read the correct version.** Compare it to the violation. What changed? Why is it better?

3. **Write it from scratch.** Close both files. Write the correct version yourself without looking. This is where the learning happens.

4. **Modify the example.** Add a new concrete type to the Factory. Add a new Observer. Add a new Strategy. If the pattern is working, these additions should require adding a new class but *no changes to existing classes*.

5. **Find it in the wild.** Look for the same pattern in a library or framework you use. The Java standard library is full of patterns: `Comparator` is Strategy, `InputStream`/`FilterInputStream` is Decorator, `Iterator` is Iterator. Spring is built on patterns throughout.

---

## Troubleshooting

**`class not found` when running:** Make sure you compiled into the `out` directory and are running from that classpath. If your class is in a package, the directory structure must match.

**Compilation errors about missing classes:** Some examples reference classes defined in the same directory. Compile all files in the directory together:
```bash
javac -d out code/solid/srp/*.java
```

**`out` directory doesn't exist:** Create it first:
```bash
mkdir -p out
```
