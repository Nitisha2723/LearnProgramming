# SOLID Principles

> "SOLID is the foundation of object-oriented design. Every other design principle, every pattern, every best practice stands on top of it."

The SOLID acronym was coined by Robert C. Martin (Uncle Bob) to capture five design principles that, when followed together, lead to software that is easy to maintain, extend, and understand. These principles are not rules — they are guidelines. Understanding *why* they exist is more important than memorizing their definitions.

---

## Table of Contents

1. [S — Single Responsibility Principle](#s--single-responsibility-principle)
2. [O — Open/Closed Principle](#o--openclosed-principle)
3. [L — Liskov Substitution Principle](#l--liskov-substitution-principle)
4. [I — Interface Segregation Principle](#i--interface-segregation-principle)
5. [D — Dependency Inversion Principle](#d--dependency-inversion-principle)
6. [SOLID in Practice: Putting It All Together](#solid-in-practice-putting-it-all-together)

---

## S — Single Responsibility Principle

**"A class should have only one reason to change."**

This is probably the most misunderstood principle. Beginners read "one responsibility" and think it means "one method" or "one concept." But Uncle Bob is specific: *one reason to change* means serving *one actor* — one person, team, or stakeholder who owns that behavior.

### The Real Meaning

Consider who cares about a piece of functionality:
- The **finance team** cares about how reports are calculated.
- The **operations team** cares about how emails are sent.
- The **security team** cares about how users are authenticated.

If a single class mixes these concerns, then any change requested by any of these teams requires you to touch the same class. That's three *reasons to change*, meaning three groups of people who might ask you to modify it — and their requests might conflict.

### The Classic Violation

```java
// BAD: This class has at least three reasons to change
public class UserService {

    private final DatabaseConnection db;
    private final EmailClient emailClient;

    public UserService(DatabaseConnection db, EmailClient emailClient) {
        this.db = db;
        this.emailClient = emailClient;
    }

    // Responsibility 1: Authentication (owned by the Security team)
    public boolean authenticate(String username, String password) {
        String storedHash = db.query(
            "SELECT password_hash FROM users WHERE username = ?", username
        );
        return BCrypt.checkpw(password, storedHash);
    }

    // Responsibility 2: Email sending (owned by the Marketing/Ops team)
    public void sendWelcomeEmail(String userEmail, String username) {
        String subject = "Welcome to our platform, " + username + "!";
        String body = loadEmailTemplate("welcome.html").replace("{{name}}", username);
        emailClient.send(userEmail, subject, body);
    }

    // Responsibility 3: Report generation (owned by the Finance/Analytics team)
    public UserActivityReport generateActivityReport(long userId) {
        List<Activity> activities = db.query(
            "SELECT * FROM activities WHERE user_id = ? ORDER BY timestamp DESC", userId
        );
        return new UserActivityReport(userId, activities, LocalDate.now());
    }
}
```

This class has three stakeholders. If the security team requires a change to the hashing algorithm, you edit this class. If marketing wants to add a new email template, you edit this class. If finance changes the report format, you edit this class. Every edit risks introducing bugs into the other concerns.

### The Fix: Separate Actors

```java
// GOOD: Each class has exactly one reason to change

// Security team owns this class
public class UserAuthService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserAuthService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public boolean authenticate(String username, String password) {
        return userRepository.findByUsername(username)
            .map(user -> passwordHasher.verify(password, user.getPasswordHash()))
            .orElse(false);
    }
}

// Operations/Marketing team owns this class
public class UserEmailService {
    private final EmailClient emailClient;
    private final TemplateEngine templateEngine;

    public UserEmailService(EmailClient emailClient, TemplateEngine templateEngine) {
        this.emailClient = emailClient;
        this.templateEngine = templateEngine;
    }

    public void sendWelcomeEmail(User user) {
        String content = templateEngine.render("welcome", Map.of("name", user.getUsername()));
        emailClient.send(user.getEmail(), "Welcome!", content);
    }
}

// Analytics/Finance team owns this class
public class UserReportService {
    private final ActivityRepository activityRepository;
    private final ReportFormatter formatter;

    public UserReportService(ActivityRepository activityRepository, ReportFormatter formatter) {
        this.activityRepository = activityRepository;
        this.formatter = formatter;
    }

    public UserActivityReport generateActivityReport(long userId) {
        List<Activity> activities = activityRepository.findByUserId(userId);
        return formatter.format(userId, activities);
    }
}
```

### How to Spot SRP Violations

The quickest diagnostic: describe what your class does. If you use the word **"and"**, it probably violates SRP.

- "This class authenticates users **and** sends emails." — violation.
- "This class persists orders **and** calculates tax." — violation.
- "This class validates user input." — probably fine.

### Real-World Impact

Smaller, focused classes are:

- **Easier to test** — you can test authentication without setting up email infrastructure.
- **Easier to change** — you modify one class without fearing ripple effects.
- **Easier to name** — if you can't give a class a clear single-noun name, it's doing too much.
- **Easier to reuse** — `UserEmailService` can be reused across registration, password reset, and notifications.

---

## O — Open/Closed Principle

**"Software entities should be open for extension, but closed for modification."**

This principle, introduced by Bertrand Meyer and later refined by Uncle Bob, is about protecting existing, tested code from change. Every time you modify a class, you risk breaking something. The ideal scenario is: to add new behavior, you write a *new* class or method rather than changing an existing one.

### The Classic Violation

Imagine a tax calculator that needs to handle different countries:

```java
// BAD: Every new country requires modifying this class
public class TaxCalculator {

    public double calculateTax(double amount, String country) {
        if (country.equals("US")) {
            return amount * 0.08;
        } else if (country.equals("UK")) {
            return amount * 0.20;
        } else if (country.equals("DE")) {
            return amount * 0.19;
        } else if (country.equals("FR")) {
            return amount * 0.20;
        }
        // What happens when we need to add Brazil? Japan? Australia?
        // We edit this class every single time.
        throw new IllegalArgumentException("Unknown country: " + country);
    }
}
```

Every time a new country is added, a developer must:
1. Find this class
2. Edit an existing method
3. Re-test everything
4. Risk breaking existing tax calculations

This doesn't scale. It also creates a hotspot file that every developer is touching simultaneously.

### The Fix: Strategy Pattern

```java
// GOOD: New country = new file, not modified existing file

// The abstraction that all strategies implement
public interface TaxStrategy {
    double calculateTax(double amount);
    String getCountryCode();
}

// Each country is its own implementation — all existing strategies are untouched
public class USTaxStrategy implements TaxStrategy {
    @Override
    public double calculateTax(double amount) {
        return amount * 0.08;
    }

    @Override
    public String getCountryCode() {
        return "US";
    }
}

public class UKTaxStrategy implements TaxStrategy {
    @Override
    public double calculateTax(double amount) {
        return amount * 0.20;
    }

    @Override
    public String getCountryCode() {
        return "UK";
    }
}

// Adding Brazil: just create a new file, nothing existing changes
public class BRTaxStrategy implements TaxStrategy {
    @Override
    public double calculateTax(double amount) {
        // Brazil has a complex tiered tax system
        if (amount <= 1000) return amount * 0.12;
        if (amount <= 5000) return amount * 0.17;
        return amount * 0.25;
    }

    @Override
    public String getCountryCode() {
        return "BR";
    }
}

// The calculator is now closed for modification — it never needs to change
public class TaxCalculator {

    private final Map<String, TaxStrategy> strategies;

    public TaxCalculator(List<TaxStrategy> strategies) {
        this.strategies = strategies.stream()
            .collect(Collectors.toMap(TaxStrategy::getCountryCode, Function.identity()));
    }

    public double calculateTax(double amount, String country) {
        TaxStrategy strategy = strategies.get(country);
        if (strategy == null) {
            throw new IllegalArgumentException("No tax strategy for country: " + country);
        }
        return strategy.calculateTax(amount);
    }
}
```

### How Interfaces Enable OCP

The key insight is that interfaces and abstract classes define *stable contracts*. Clients depend on the contract, not the implementation. When you add a new implementation:

```
Before adding Brazil:
  TaxCalculator  →  [US, UK, DE]

After adding Brazil:
  TaxCalculator  →  [US, UK, DE, BR]   ← TaxCalculator never changed!
```

The calculator is **open for extension** (new `TaxStrategy` implementations) and **closed for modification** (the calculator class itself never changes).

### The Practical Rule

OCP doesn't mean you *never* modify code. It means that for the *likely sources of change*, you should structure your design so that change arrives via extension. Ask yourself: "What will change here in 6 months?" Design the abstraction around that axis of change.

---

## L — Liskov Substitution Principle

**"Subtypes must be substitutable for their base types."**

Barbara Liskov stated this in 1987. It sounds simple, but it has profound implications. If `Square` extends `Rectangle`, can a `Square` be used *everywhere* a `Rectangle` is expected without breaking the program? If not, the inheritance relationship is wrong.

### The Famous Rectangle/Square Violation

This is the canonical LSP example because it seems counterintuitive at first. A square *is* a rectangle in mathematics, so inheritance seems natural.

```java
// Seems reasonable...
public class Rectangle {
    protected int width;
    protected int height;

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getArea() {
        return width * height;
    }
}

// A square IS-A rectangle, right?
public class Square extends Rectangle {

    @Override
    public void setWidth(int width) {
        // A square must have equal sides
        this.width = width;
        this.height = width;  // <-- side effect!
    }

    @Override
    public void setHeight(int height) {
        // A square must have equal sides
        this.width = height;  // <-- side effect!
        this.height = height;
    }
}
```

Now watch what happens when someone writes code that expects a `Rectangle`:

```java
// This works perfectly with a Rectangle
public void testArea(Rectangle rect) {
    rect.setWidth(4);
    rect.setHeight(5);
    // For a Rectangle: 4 * 5 = 20, correct
    // For a Square: setHeight(5) also sets width to 5, so 5 * 5 = 25!
    assert rect.getArea() == 20 : "Expected 20, got " + rect.getArea();
}

// This will fail when a Square is passed!
testArea(new Rectangle()); // passes
testArea(new Square());    // throws AssertionError!
```

The `Square` cannot be substituted for a `Rectangle` without breaking the program. The inheritance relationship violates LSP.

### Why This Happens: Behavioral Contracts

LSP is really about *behavioral contracts*. When a class defines a method, it implicitly establishes:

- **Pre-conditions**: what must be true before calling the method.
- **Post-conditions**: what is guaranteed to be true after the method returns.
- **Invariants**: what remains true throughout the object's lifetime.

The LSP rules are:
1. **Pre-conditions cannot be strengthened** in a subclass. If the base class accepts any positive integer, the subclass cannot restrict to even numbers only.
2. **Post-conditions cannot be weakened** in a subclass. If the base class guarantees a non-null return value, the subclass cannot return null.
3. **Invariants cannot be broken** by the subclass.

In the Rectangle/Square example: `Rectangle` has an invariant that width and height are *independent*. `Square` breaks this invariant by coupling them.

### Fixing the Rectangle/Square Problem

The fix is to recognize that `Square` and `Rectangle` are different abstractions — they don't share the same behavioral contract:

```java
// Solution 1: Don't use inheritance — they're different shapes
public interface Shape {
    int getArea();
}

public class Rectangle implements Shape {
    private final int width;
    private final int height;

    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getArea() {
        return width * height;
    }
}

public class Square implements Shape {
    private final int side;

    public Square(int side) {
        this.side = side;
    }

    @Override
    public int getArea() {
        return side * side;
    }
}

// Now both are substitutable for Shape, which has no width/height mutation:
public void printArea(Shape shape) {
    System.out.println("Area: " + shape.getArea());
}
// This works correctly with both Rectangle and Square!
```

### Identifying LSP Violations

Watch for these warning signs:

1. **Overriding methods that throw exceptions**: If a subclass overrides `save()` to throw `UnsupportedOperationException`, it cannot substitute for its parent.

```java
// VIOLATION: ReadOnlyList cannot substitute for List
public class ReadOnlyList<T> extends ArrayList<T> {
    @Override
    public boolean add(T element) {
        throw new UnsupportedOperationException("Read-only list!");
    }
}
```

2. **Overriding methods that do nothing**: A no-op override often signals a broken abstraction.

```java
// VIOLATION: NullLogger "is-a" Logger but does nothing
public class NullLogger extends FileLogger {
    @Override
    public void log(String message) {
        // intentionally empty
    }
}
// Better: extract a Logger interface that NullLogger properly implements
```

3. **Type checking in client code**: If you find `instanceof` checks in consuming code, LSP is likely violated.

```java
// SMELL: Client has to know about subtypes
public void processShape(Shape shape) {
    if (shape instanceof Square) {
        // special handling for squares
    } else if (shape instanceof Circle) {
        // special handling for circles
    }
}
```

### The Duck Test

> "If it looks like a duck and quacks like a duck but needs batteries, you probably have the wrong abstraction."

This captures the spirit of LSP. If your subtype needs special treatment, the abstraction is wrong.

---

## I — Interface Segregation Principle

**"Clients should not be forced to depend on interfaces they don't use."**

Fat interfaces are a smell. When one interface grows to cover many unrelated operations, implementing classes are forced to implement methods that don't apply to them. This creates unnecessary coupling and confusing `UnsupportedOperationException` implementations.

### The Classic Violation: Fat Interface

Imagine a workplace management system:

```java
// BAD: This interface forces robots to implement eat()
public interface Worker {
    void work();
    void eat();
    void attendMeeting();
    void signTimesheet();
    void requestVacation();
}

// Human worker — implements all methods naturally
public class HumanWorker implements Worker {
    @Override public void work() { /* ... */ }
    @Override public void eat() { /* ... */ }
    @Override public void attendMeeting() { /* ... */ }
    @Override public void signTimesheet() { /* ... */ }
    @Override public void requestVacation() { /* ... */ }
}

// Robot worker — what do we do here?
public class RobotWorker implements Worker {
    @Override public void work() { /* ... */ }

    @Override
    public void eat() {
        // Robots don't eat! But the interface forces us to implement this.
        throw new UnsupportedOperationException("Robots don't eat");
    }

    @Override
    public void attendMeeting() { /* ... */ }

    @Override
    public void signTimesheet() {
        // Robots aren't employees, they don't have timesheets
        throw new UnsupportedOperationException("Robots don't sign timesheets");
    }

    @Override
    public void requestVacation() {
        // Robots don't take vacations
        throw new UnsupportedOperationException("Robots don't take vacations");
    }
}
```

The `RobotWorker` is forced to throw exceptions for methods it cannot meaningfully implement. This is also an LSP violation — a `RobotWorker` cannot substitute for `Worker` in code that calls `eat()`.

### The Fix: Segregated Interfaces

```java
// GOOD: Each interface represents one coherent capability

public interface Workable {
    void work();
}

public interface Eatable {
    void eat();
}

public interface Attendable {
    void attendMeeting();
}

public interface Employable {
    void signTimesheet();
    void requestVacation();
}

// Human implements all relevant interfaces
public class HumanWorker implements Workable, Eatable, Attendable, Employable {
    @Override public void work() { /* ... */ }
    @Override public void eat() { /* ... */ }
    @Override public void attendMeeting() { /* ... */ }
    @Override public void signTimesheet() { /* ... */ }
    @Override public void requestVacation() { /* ... */ }
}

// Robot only implements what makes sense
public class RobotWorker implements Workable, Attendable {
    @Override public void work() { /* ... */ }
    @Override public void attendMeeting() { /* ... */ }
    // No eat(), no signTimesheet(), no requestVacation() — and that's perfectly fine
}

// Client code depends only on what it needs
public class Factory {
    private final List<Workable> workers;  // only cares about work()

    public Factory(List<Workable> workers) {
        this.workers = workers;
    }

    public void runProduction() {
        workers.forEach(Workable::work);  // works with humans and robots alike
    }
}

public class Cafeteria {
    private final List<Eatable> diners;  // only cares about eat()

    public Cafeteria(List<Eatable> diners) {
        this.diners = diners;
    }

    public void serveLunch() {
        diners.forEach(Eatable::eat);  // correctly excludes robots
    }
}
```

### ISP in the Java Standard Library

The Java standard library demonstrates ISP throughout its Collections framework:

```
Iterable
  └── Collection
        ├── List        (add, get, set, remove by index)
        ├── Set         (no duplicates)
        └── Queue       (offer, poll, peek)
```

`RandomAccess` is a *marker interface* — it's empty, but a `List` implements it to signal that index-based access is fast (e.g., `ArrayList`). Code that needs fast random access checks for `RandomAccess` rather than requiring a different type.

### The ISP Diagnostic

Ask: "If I implement this interface, will I need to throw `UnsupportedOperationException` or leave methods empty?" If yes, the interface is too fat.

---

## D — Dependency Inversion Principle

**"High-level modules should not depend on low-level modules. Both should depend on abstractions. Abstractions should not depend on details. Details should depend on abstractions."**

This is arguably the most powerful principle in the set. It is the foundation of testability, flexibility, and loose coupling. It is also frequently misunderstood — it's about *inverting the direction of dependency*, not about dependency injection frameworks.

### The Problem: Tight Coupling

```java
// BAD: High-level business logic directly depends on low-level infrastructure

public class OrderService {

    // Creating dependencies directly — tight coupling
    private final MySQLDatabase database = new MySQLDatabase("jdbc:mysql://localhost/orders");
    private final SendGridEmailService emailService = new SendGridEmailService("API_KEY_123");
    private final StripePaymentGateway paymentGateway = new StripePaymentGateway("sk_live_...");

    public void placeOrder(Order order) {
        // validate, process payment, save, send confirmation
        boolean paymentOk = paymentGateway.charge(order.getTotal(), order.getCard());
        if (paymentOk) {
            database.save(order);
            emailService.sendConfirmation(order.getCustomerEmail(), order);
        }
    }
}
```

Problems with this design:

1. **Impossible to test without real infrastructure.** Running a unit test now requires a MySQL database, a real SendGrid account, and a live Stripe connection.
2. **Impossible to swap implementations.** Want to switch from MySQL to PostgreSQL? You must change `OrderService`. Want to use Mailgun instead of SendGrid? You must change `OrderService`.
3. **The high-level business logic (`OrderService`) is enslaved to low-level details** (which database, which email provider, which payment gateway).

### The Fix: Depend on Abstractions

```java
// GOOD: Define abstractions (interfaces) that both high-level and low-level modules depend on

// Abstractions — these live in the domain layer, owned by the business
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(long orderId);
    List<Order> findByCustomerId(long customerId);
}

public interface NotificationService {
    void sendOrderConfirmation(String email, Order order);
    void sendOrderShippedNotification(String email, Order order);
}

public interface PaymentGateway {
    PaymentResult charge(Money amount, PaymentDetails details);
    boolean refund(String transactionId);
}

// High-level module — depends ONLY on abstractions
public class OrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final PaymentGateway paymentGateway;

    // Dependencies are INJECTED, not created
    public OrderService(
            OrderRepository orderRepository,
            NotificationService notificationService,
            PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
        this.paymentGateway = paymentGateway;
    }

    public OrderResult placeOrder(Order order) {
        PaymentResult payment = paymentGateway.charge(order.getTotal(), order.getPaymentDetails());
        if (!payment.isSuccessful()) {
            return OrderResult.paymentFailed(payment.getErrorMessage());
        }

        order.markAsPaid(payment.getTransactionId());
        orderRepository.save(order);
        notificationService.sendOrderConfirmation(order.getCustomerEmail(), order);

        return OrderResult.success(order.getId());
    }
}

// Low-level modules — depend on the same abstractions
public class MySQLOrderRepository implements OrderRepository {
    private final DataSource dataSource;

    public MySQLOrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Order order) {
        // MySQL-specific implementation
    }

    @Override
    public Optional<Order> findById(long orderId) {
        // MySQL-specific implementation
        return Optional.empty();
    }

    @Override
    public List<Order> findByCustomerId(long customerId) {
        return List.of();
    }
}

public class SendGridNotificationService implements NotificationService {
    private final String apiKey;

    public SendGridNotificationService(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void sendOrderConfirmation(String email, Order order) {
        // SendGrid-specific implementation
    }

    @Override
    public void sendOrderShippedNotification(String email, Order order) {
        // SendGrid-specific implementation
    }
}
```

### The Dependency Graph Inversion

```
Before (BAD — high-level depends on low-level):

  OrderService
      ├──► MySQLDatabase
      ├──► SendGridEmailService
      └──► StripePaymentGateway


After (GOOD — everyone depends on abstractions):

  OrderService ──► OrderRepository ◄── MySQLOrderRepository
               ──► NotificationService ◄── SendGridNotificationService
               ──► PaymentGateway ◄── StripePaymentGateway

  The arrows from implementations point toward the interface (upward toward abstraction).
  This is the "inversion" — the implementation depends on the abstraction, not the other way.
```

### Dependency Injection: How to Hand Dependencies In

DIP tells you *what* to depend on (abstractions). Dependency Injection (DI) is the technique for *providing* those dependencies. There are three styles:

**1. Constructor Injection (Recommended)**

```java
public class OrderService {
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    // Dependencies declared in constructor — mandatory and immutable
    public OrderService(OrderRepository orderRepository, NotificationService notificationService) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.notificationService = Objects.requireNonNull(notificationService);
    }
}
```

Why it's best:
- Dependencies are explicit — you can't create the object without them.
- Fields can be `final` — the object is immutable with respect to its dependencies.
- Easy to test — just pass mock objects in the constructor.

**2. Setter Injection (Occasionally useful)**

```java
public class OrderService {
    private OrderRepository orderRepository;  // not final

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

Use when: dependencies are optional, or when you need to change them at runtime (rare). Avoid as the primary injection mechanism — it allows objects to be used before all dependencies are set.

**3. Field Injection (Avoid)**

```java
public class OrderService {
    @Autowired  // Spring annotation — injects directly into the field
    private OrderRepository orderRepository;
}
```

Why to avoid it:
- Requires a DI framework to work — class cannot be instantiated without it.
- Cannot use `final` — dependency might be null if not injected.
- Makes testing harder — you can't inject mocks without reflection or framework support.
- Hides dependencies — the class's contract (its constructor) doesn't reveal what it needs.

### Writing Testable Code: The Payoff

With DIP applied, testing becomes straightforward:

```java
// Test double — a fake implementation for testing
class InMemoryOrderRepository implements OrderRepository {
    private final Map<Long, Order> store = new HashMap<>();

    @Override
    public void save(Order order) {
        store.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(long orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public List<Order> findByCustomerId(long customerId) {
        return store.values().stream()
            .filter(o -> o.getCustomerId() == customerId)
            .collect(Collectors.toList());
    }
}

class FakeNotificationService implements NotificationService {
    private final List<String> sentEmails = new ArrayList<>();

    @Override
    public void sendOrderConfirmation(String email, Order order) {
        sentEmails.add(email);
    }

    @Override
    public void sendOrderShippedNotification(String email, Order order) {
        sentEmails.add(email);
    }

    public List<String> getSentEmails() {
        return Collections.unmodifiableList(sentEmails);
    }
}

// The test — no database, no network, instant
class OrderServiceTest {

    @Test
    void placeOrder_sendsConfirmationEmail() {
        // Arrange
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        FakeNotificationService notifications = new FakeNotificationService();
        FakePaymentGateway payment = new FakePaymentGateway(true); // always succeeds

        OrderService service = new OrderService(repository, notifications, payment);
        Order order = Order.create(customer("alice@example.com"), items());

        // Act
        OrderResult result = service.placeOrder(order);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(notifications.getSentEmails().contains("alice@example.com"));
        assertTrue(repository.findById(order.getId()).isPresent());
    }
}
```

No external infrastructure required. The test is fast, deterministic, and focused on business logic.

### IoC Containers: The Next Step (Spring Preview)

In large applications, manually constructing and wiring all dependencies becomes tedious. An **Inversion of Control (IoC) container** automates this wiring. Spring is the most popular Java IoC container:

```java
// With Spring, you declare the dependency graph with annotations
@Service  // Spring manages this bean's lifecycle
public class OrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final PaymentGateway paymentGateway;

    // Spring sees this constructor and knows to inject the registered beans
    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            NotificationService notificationService,
            PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
        this.paymentGateway = paymentGateway;
    }
}

@Repository  // Spring knows this is a data-access bean
public class MySQLOrderRepository implements OrderRepository {
    // ...
}

@Component  // Spring registers this as a bean
public class SendGridNotificationService implements NotificationService {
    // ...
}
```

Spring scans your application, finds all annotated classes, figures out which implementations satisfy which interfaces, and wires them together for you. The DIP design that made your code testable is what makes Spring integration seamless.

---

## SOLID in Practice: Putting It All Together

The five principles reinforce each other:

- **SRP** gives you small, focused classes, each serving one actor.
- **OCP** protects those classes from being modified every time requirements change.
- **LSP** ensures your inheritance hierarchies are correct, making polymorphism reliable.
- **ISP** keeps your interfaces lean, so implementing classes are never forced to lie.
- **DIP** binds it all together by ensuring high-level policy doesn't depend on low-level detail.

A design that violates any one principle tends to pull other principles toward violation as well. A class with too many responsibilities (SRP violation) tends to have fat interfaces (ISP violation) and tight coupling (DIP violation).

Conversely, applying SOLID consistently leads to a codebase that is a joy to work in: small classes, clear names, easy tests, painless extensions.

```
    The SOLID dependency graph:
    ┌─────────────────────────────────────────┐
    │  SRP: each class serves one actor       │
    │    → makes classes small and focused    │
    │                    │                    │
    │                    ▼                    │
    │  ISP: interfaces match client needs     │
    │    → focused contracts, no bloat        │
    │                    │                    │
    │                    ▼                    │
    │  LSP: subtypes honor contracts          │
    │    → polymorphism you can trust         │
    │                    │                    │
    │                    ▼                    │
    │  OCP: extend without modifying          │
    │    → strategy, new file, not edit       │
    │                    │                    │
    │                    ▼                    │
    │  DIP: depend on abstractions            │
    │    → testable, swappable, flexible      │
    └─────────────────────────────────────────┘
```

> "The goal of software architecture is to minimize the human resources required to build and maintain the required system."  
> — Robert C. Martin, *Clean Architecture*

SOLID is how you achieve that goal at the class level.
