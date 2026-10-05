# Optional: Taming the Null Problem

## The Billion-Dollar Mistake

In 1965, Tony Hoare invented null references. In a 2009 talk, he called it his "billion-dollar mistake":

> "I couldn't resist the temptation to put in a null reference, simply because it was so easy to implement. This has led to innumerable errors, vulnerabilities, and system crashes."

`NullPointerException` is still one of the most common Java runtime errors. Every time you write:

```java
User user = getUser(id);
System.out.println(user.getName());  // NPE if user is null!
```

You're trusting that `getUser()` never returns null. That trust is often violated.

---

## What Is Optional?

`Optional<T>` is a container that **may or may not hold a value**. It's a way to signal that a method might not return a result, forcing the caller to handle both cases.

```java
// Without Optional — easy to forget null check
User findUser(int id) { ... }  // Might return null!
User user = findUser(42);
user.getName();  // BOOM if user is null

// With Optional — impossible to "forget" the null case
Optional<User> findUser(int id) { ... }  // Explicitly says "might not exist"
Optional<User> user = findUser(42);
// You MUST call .get(), .orElse(), etc. — the API forces you to handle absence
user.get().getName();     // Works, but can still throw if not present
user.orElse(GUEST).getName(); // Safe!
```

---

## Creating Optionals

```java
// Has a value (throws NullPointerException if value is null!)
Optional<String> name = Optional.of("Alice");

// Has a value OR is empty (safe for potentially-null values)
Optional<String> name = Optional.ofNullable(possiblyNullString);

// Always empty
Optional<String> nothing = Optional.empty();
```

**When to use which:**
- `Optional.of()` when you KNOW the value is non-null (asserts non-null)
- `Optional.ofNullable()` when wrapping values that might be null
- `Optional.empty()` when you explicitly have no result

---

## Using Optionals

### Check and Get (verbose, not recommended)

```java
Optional<User> user = findUser(42);

if (user.isPresent()) {
    System.out.println(user.get().getName());  // .get() without isPresent() throws!
}

// isPresent() + get() is anti-pattern — defeats the purpose of Optional
// It's just a verbose null check
```

### orElse — Provide a Default

```java
// Return a default value if empty
User user = findUser(42).orElse(User.GUEST);

String name = findName(id).orElse("Anonymous");
int count = getCount().orElse(0);
```

### orElseGet — Lazy Default (computed only when needed)

```java
// orElse: ALWAYS evaluates the expression (even if Optional has a value)
User user = findUser(42).orElse(createDefaultUser());  // createDefaultUser() ALWAYS called!

// orElseGet: LAZY — only calls the supplier if Optional is empty
User user = findUser(42).orElseGet(() -> createDefaultUser());  // Only if empty ✓
```

### orElseThrow

```java
// Throw if empty (good for "this must exist" cases)
User user = findUser(42).orElseThrow(
    () -> new UserNotFoundException("User 42 not found")
);

// Java 10+ shorthand (throws NoSuchElementException)
User user = findUser(42).orElseThrow();
```

### map — Transform the Value

```java
// If present, apply function; if empty, stay empty
Optional<String> name = findUser(42).map(User::getName);

// Chain transformations
Optional<String> city = findUser(42)
    .map(User::getAddress)     // Optional<User> → Optional<Address>
    .map(Address::getCity);    // Optional<Address> → Optional<String>
```

### flatMap — Avoid Nested Optionals

```java
// If User.getAddress() returns Optional<Address>:
// map() would give Optional<Optional<Address>> — wrong!
Optional<Optional<Address>> bad = findUser(42).map(User::getOptionalAddress);

// flatMap() flattens the result
Optional<Address> good = findUser(42).flatMap(User::getOptionalAddress);

// Chain with flatMap
Optional<String> city = findUser(42)
    .flatMap(User::getOptionalAddress)
    .flatMap(Address::getOptionalCity);
```

### filter — Conditional Presence

```java
// If present AND condition is true: stay present
// If present AND condition is false: become empty
// If empty: stay empty
Optional<User> adultUser = findUser(42)
    .filter(u -> u.getAge() >= 18);
```

### ifPresent — Side Effect if Present

```java
findUser(42).ifPresent(user -> {
    logger.info("Found user: {}", user.getName());
    sendWelcomeEmail(user);
});
```

### ifPresentOrElse (Java 9+)

```java
findUser(42).ifPresentOrElse(
    user -> System.out.println("Found: " + user.getName()),
    ()   -> System.out.println("User not found")
);
```

### or (Java 9+) — Fallback Optional

```java
// If empty, try another Optional
Optional<User> user = findUserInCache(42)
    .or(() -> findUserInDatabase(42));
```

### stream (Java 9+) — Optional to Stream

```java
// Convert Optional to 0-or-1 element Stream
// Useful when combining with stream pipelines
List<User> users = userIds.stream()
    .map(this::findUser)          // Stream<Optional<User>>
    .flatMap(Optional::stream)    // Stream<User> (empties filtered out)
    .collect(Collectors.toList());
```

---

## Complete Chaining Example

```java
// Find the city of a user's shipping address
String city = userRepository.findById(userId)           // Optional<User>
    .filter(User::isActive)                              // Filter inactive users
    .map(User::getShippingAddress)                       // Optional<Address>
    .filter(addr -> addr.getCountry().equals("Germany")) // Filter non-German addresses
    .map(Address::getCity)                               // Optional<String>
    .map(String::toUpperCase)                            // Uppercase city name
    .orElse("UNKNOWN");                                  // Default if any step was empty
```

Without Optional, this would be:

```java
String city = "UNKNOWN";
User user = userRepository.findById(userId);
if (user != null && user.isActive()) {
    Address addr = user.getShippingAddress();
    if (addr != null && "Germany".equals(addr.getCountry())) {
        String cityStr = addr.getCity();
        if (cityStr != null) {
            city = cityStr.toUpperCase();
        }
    }
}
```

The Optional version is far more readable and less error-prone.

---

## When to Use Optional — and When NOT To

### USE Optional for:

**Return types of methods that might not find a value:**

```java
Optional<User> findById(int id) { ... }           // ✓
Optional<String> findConfig(String key) { ... }   // ✓
Optional<Double> computeAverage(List<Integer> list) { ... } // ✓ (empty list)
```

### DO NOT USE Optional for:

**Fields in a class (wastes memory, breaks serialization):**

```java
// ❌ Don't do this
class User {
    private Optional<String> nickname;  // Bad! Just use String nickname = null;
}

// ✓ Do this
class User {
    private String nickname;  // null means not set
}
```

**Method parameters (forces callers to create Optional wrappers):**

```java
// ❌ Don't do this
void process(Optional<String> name) { ... }
// Forces: process(Optional.of("Alice")) — ugly

// ✓ Do this
void process(String name) { ... }  // Or have two overloads
```

**Collections (use empty collection instead):**

```java
// ❌ Don't do this
Optional<List<User>> findUsers() { ... }

// ✓ Return empty list instead
List<User> findUsers() { return Collections.emptyList(); }
```

**Performance-critical code:**

Optional creates an object wrapper — in tight loops with millions of iterations, this allocation matters.

---

## Optional Signals Intent

The real value of Optional is **documentation through types**:

```java
// Without Optional: Is this nullable? Who knows!
User findUser(int id) { ... }

// With Optional: Explicitly says "user might not exist"
Optional<User> findUser(int id) { ... }

// Non-Optional return: Guarantees a value (or throws)
User getUserOrThrow(int id) { ... }
```

When you see `Optional<T>` as a return type, you immediately know: "I need to handle the case where this doesn't exist." That's valuable information.
