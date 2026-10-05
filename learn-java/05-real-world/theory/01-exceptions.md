# Exceptions in Java

## What Is an Exception?

An exception is an event that occurs during program execution that **disrupts the normal flow of instructions**. Think of it as an "unexpected situation" that the normal code path doesn't account for:

- File doesn't exist when you try to read it
- Division by zero
- Network connection drops mid-request
- User provides invalid input
- Out of memory

Java's exception system provides a structured way to **detect, report, and recover** from these situations.

---

## The Exception Hierarchy

```
java.lang.Throwable
├── Error                          (JVM-level problems — don't catch these)
│     ├── OutOfMemoryError
│     ├── StackOverflowError
│     └── AssertionError
│
└── Exception                      (Application-level problems)
      ├── RuntimeException         (UNCHECKED — compiler doesn't require handling)
      │     ├── NullPointerException
      │     ├── IllegalArgumentException
      │     ├── IllegalStateException
      │     ├── IndexOutOfBoundsException
      │     ├── ClassCastException
      │     ├── ArithmeticException (e.g., divide by zero)
      │     └── UnsupportedOperationException
      │
      ├── IOException               (CHECKED — must catch or declare)
      ├── SQLException              (CHECKED)
      ├── ParseException            (CHECKED)
      └── ... (all other non-Runtime exceptions are checked)
```

---

## Checked vs Unchecked Exceptions

This is **Java's controversial design choice** that other languages (Kotlin, C#, Python) mostly abandoned.

### Checked Exceptions

Must be either caught with `try-catch` OR declared with `throws`. The compiler enforces this.

```java
// This WON'T compile without handling the checked IOException:
public String readFile(String path) {
    return Files.readString(Path.of(path));  // ❌ Compile error!
}

// Fix 1: Catch it
public String readFile(String path) {
    try {
        return Files.readString(Path.of(path));
    } catch (IOException e) {
        return "default content";
    }
}

// Fix 2: Declare it (let caller handle it)
public String readFile(String path) throws IOException {
    return Files.readString(Path.of(path));
}
```

### Unchecked Exceptions (RuntimeException)

Don't require handling. The compiler trusts you to prevent these through good code.

```java
// This compiles fine — no forced handling
public int divide(int a, int b) {
    return a / b;  // Could throw ArithmeticException if b == 0
}

// Good practice: validate and throw meaningful exception
public int divide(int a, int b) {
    if (b == 0) throw new IllegalArgumentException("Divisor cannot be zero");
    return a / b;
}
```

### The Controversy

The Java designers thought checked exceptions would make programs more robust. In practice:
- They lead to "exception swallowing" (empty catch blocks)
- They make API design harder (every interface method that might throw must declare it)
- They don't actually guarantee better handling

Modern Java code often uses unchecked exceptions for cleaner APIs.

---

## try-catch-finally

```java
try {
    // Code that might throw an exception
    String content = Files.readString(Path.of("data.txt"));
    int value = Integer.parseInt(content.trim());
    System.out.println("Value: " + value);
} catch (IOException e) {
    // Handle file-related problem
    System.err.println("Could not read file: " + e.getMessage());
} catch (NumberFormatException e) {
    // Handle parsing problem
    System.err.println("File content is not a number: " + e.getMessage());
} catch (Exception e) {
    // Catch-all — usually a sign of poor error handling, but sometimes needed
    System.err.println("Unexpected error: " + e.getMessage());
} finally {
    // ALWAYS runs, regardless of exception or return
    // Used for cleanup that must happen
    System.out.println("Cleanup complete");
}
```

### Multi-catch (Java 7+)

```java
try {
    // ...
} catch (IOException | SQLException e) {
    // Handle both the same way
    logger.error("Data access error", e);
}
```

### When Finally Runs

```java
// finally runs even with a return statement!
int riskyMethod() {
    try {
        return 1;
    } finally {
        System.out.println("finally runs!");  // This prints before return
        // If you put a return here, it overrides the try's return — don't do this!
    }
}

// finally runs even if the exception isn't caught
void noHandler() throws IOException {
    try {
        throw new IOException("problem");
    } finally {
        System.out.println("still runs!");  // Runs, then exception propagates
    }
}
```

---

## try-with-resources: The RIGHT Way to Handle Resources

**The Problem:**
Resources (files, database connections, network sockets) implement `AutoCloseable` and must be closed when done. Pre-Java 7 code was error-prone:

```java
// Old (broken) way — resource leak if exception occurs:
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader("file.txt"));
    return reader.readLine();
} catch (IOException e) {
    e.printStackTrace();
} finally {
    if (reader != null) {
        try {
            reader.close();  // This can also throw IOException!
        } catch (IOException e) {
            e.printStackTrace();  // Which exception do we surface?
        }
    }
}
```

**The Solution: try-with-resources (Java 7+)**

```java
// Correct way — resource is ALWAYS closed, even if exception occurs:
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    return reader.readLine();
} catch (IOException e) {
    System.err.println("Read failed: " + e.getMessage());
    return null;
}
```

The resource declared in `try(...)` is automatically closed when the try block exits — whether normally, by exception, or by return.

### Multiple Resources

```java
// Both resources are closed in REVERSE order of declaration
try (
    Connection conn = DriverManager.getConnection(url, user, pass);
    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")
) {
    stmt.setInt(1, userId);
    ResultSet rs = stmt.executeQuery();
    // ... process results
} catch (SQLException e) {
    throw new RuntimeException("Database query failed", e);
}
// stmt.close() called first, then conn.close()
```

### Suppressed Exceptions

If both the resource's `close()` and the try body throw exceptions, the `close()` exception is **suppressed** (attached to the main exception) rather than hiding it:

```java
try (Resource r = new Resource()) {
    throw new RuntimeException("body exception");
    // Resource.close() also throws → becomes suppressed exception
} catch (RuntimeException e) {
    System.out.println("Main: " + e.getMessage());
    System.out.println("Suppressed: " + e.getSuppressed()[0].getMessage());
}
```

---

## Creating Custom Exceptions

Create custom exceptions when you want meaningful exception types for your domain:

```java
// Checked custom exception (extends Exception)
public class InsufficientFundsException extends Exception {
    private final double amount;
    private final double balance;

    public InsufficientFundsException(double amount, double balance) {
        super(String.format("Cannot withdraw $%.2f: balance is $%.2f", amount, balance));
        this.amount = amount;
        this.balance = balance;
    }

    public double getAmount()  { return amount; }
    public double getBalance() { return balance; }
}

// Unchecked custom exception (extends RuntimeException)
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Product not found: " + productId);
    }

    // Constructor for exception chaining
    public ProductNotFoundException(String productId, Throwable cause) {
        super("Product not found: " + productId, cause);
    }
}

// Usage
public void withdraw(double amount) throws InsufficientFundsException {
    if (amount > balance) {
        throw new InsufficientFundsException(amount, balance);
    }
    balance -= amount;
}
```

---

## Exception Chaining

When you catch one exception and throw a new one, preserve the original as the "cause":

```java
// Service layer catching a low-level exception
public User findUser(int id) {
    try {
        return userRepository.findById(id);  // might throw SQLException
    } catch (SQLException e) {
        // Chain the original exception so the stack trace is preserved
        throw new UserServiceException("Failed to find user with id " + id, e);
    }
}

// Later, someone catches UserServiceException:
catch (UserServiceException e) {
    System.out.println("Cause: " + e.getCause().getMessage());
    e.printStackTrace();  // Shows full chain of exceptions
}
```

**Never lose the original exception!** The cause is critical for debugging.

---

## Best Practices

### DO

```java
// 1. Catch the most specific exception possible
catch (FileNotFoundException e) { ... }    // ✓ Specific
catch (IOException e) { ... }             // ✓ Less specific but OK
catch (Exception e) { ... }               // ✗ Too broad (catch-all)

// 2. Include useful information in the message
throw new IllegalArgumentException(
    "Email must contain '@' but got: " + email);  // ✓

throw new IllegalArgumentException("Invalid email"); // ✗ Not helpful

// 3. Log the full exception with stack trace
logger.error("Failed to process order {}", orderId, e);  // ✓ (SLF4J)
logger.error("Failed: " + e.getMessage());               // ✗ Loses stack trace

// 4. Use try-with-resources for AutoCloseable resources
try (InputStream in = new FileInputStream(path)) { ... }  // ✓

// 5. Preserve exception cause when re-throwing
throw new ServiceException("Operation failed", originalException);  // ✓
```

### DON'T

```java
// 1. Never swallow exceptions silently
try {
    doSomething();
} catch (Exception e) {
    // ❌ Empty catch block — the exception just disappears!
    // At minimum: logger.warn("Swallowing exception", e);
}

// 2. Don't catch Exception or Throwable broadly (unless you have a good reason)
catch (Exception e) { ... }   // ❌ Catches NPE, OutOfMemory, everything!
catch (Throwable t) { ... }   // ❌ Even worse — catches Errors too!

// 3. Don't use exceptions for flow control
try {
    int result = Integer.parseInt(input);
} catch (NumberFormatException e) {
    // ❌ This is slow — exceptions are expensive to create
    // Use: if (input.matches("-?\\d+")) { ... }
}

// 4. Don't re-throw without the cause
catch (IOException e) {
    throw new RuntimeException("IO failed"); // ❌ Original stack trace lost!
    throw new RuntimeException("IO failed", e); // ✓ Cause preserved
}

// 5. Don't use printStackTrace() in production code
catch (Exception e) {
    e.printStackTrace();  // ❌ Goes to System.err, not your log system
    logger.error("Error", e);  // ✓ Goes through your logging framework
}
```

---

## Anti-Patterns in the Wild

```java
// ANTI-PATTERN 1: Pokemon exception handling ("gotta catch 'em all")
try {
    complexOperation();
} catch (Exception e) {
    // Catches NullPointerException, OutOfMemoryError subclasses, everything
    // You have NO IDEA what went wrong
}

// ANTI-PATTERN 2: Exception as return value
boolean processFile(String path) {
    try {
        Files.readString(Path.of(path));
        return true;
    } catch (IOException e) {
        return false;  // Caller can't tell why it failed
    }
}

// ANTI-PATTERN 3: Over-catching then re-throwing
try {
    doSomething();
} catch (IOException e) {
    throw e;  // Caught and immediately re-thrown — pointless!
}

// ANTI-PATTERN 4: Checked exception tunneling (breaking interfaces)
// Wrapping checked in Runtime just to avoid declaring throws
catch (IOException e) {
    throw new RuntimeException(e);  // OK if intentional, bad if done to hide poor design
}
```
