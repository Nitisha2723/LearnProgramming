# OOP Interview Questions — Complete Answers

## Q1: What are the four pillars of OOP? Explain each with a Java example.

**Answer:**
The four pillars of Object-Oriented Programming are Encapsulation, Inheritance, Polymorphism, and Abstraction. Together they form the conceptual foundation on which Java and most modern OOP languages are built.

**Encapsulation** means bundling data (fields) and behavior (methods) together inside a class, and restricting direct access to the internal state. You expose only what is necessary through a controlled public API. This protects the integrity of the object's state.

```java
public class BankAccount {
    private double balance; // hidden from outside

    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }

    public double getBalance() {
        return balance;
    }
}
```

**Inheritance** allows a class (subclass) to acquire the fields and methods of another class (superclass), enabling code reuse and the creation of hierarchies.

```java
public class Animal {
    public void breathe() { System.out.println("Breathing"); }
}

public class Dog extends Animal {
    public void bark() { System.out.println("Woof"); }
}
```

**Polymorphism** means "many forms." The same method call can produce different behavior depending on the actual runtime type of the object. It comes in two flavors: compile-time (method overloading) and runtime (method overriding via dynamic dispatch).

```java
Animal a = new Dog();
a.breathe(); // calls Dog's breathe() if overridden, or Animal's if not
```

**Abstraction** is the process of hiding complex implementation details and exposing only the essential interface to the user. Abstract classes and interfaces are the primary tools for abstraction in Java.

```java
public abstract class Shape {
    public abstract double area();
}

public class Circle extends Shape {
    private double radius;
    public Circle(double r) { this.radius = r; }
    public double area() { return Math.PI * radius * radius; }
}
```

**Key Points:**
- Encapsulation = data hiding + controlled access via getters/setters
- Inheritance = IS-A relationship, enabling code reuse through class hierarchies
- Polymorphism = one interface, many implementations; enabled by overriding and dynamic dispatch
- Abstraction = hiding "how" and exposing "what"; achieved via abstract classes and interfaces

---

## Q2: What is encapsulation and why does it matter?

**Answer:**
Encapsulation is one of the core OOP principles. It refers to the practice of bundling an object's state (fields) and the operations on that state (methods) into a single unit (a class), while restricting direct external access to the internal state. Instead of accessing fields directly, external code must interact through a well-defined public API.

In Java, encapsulation is implemented primarily through access modifiers: `private` fields combined with `public` getter and setter methods. The setter can enforce validation rules, ensuring the object can never be placed in an invalid state.

```java
public class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        setAge(age); // use setter for validation
    }

    public String getName() { return name; }

    public int getAge() { return age; }

    public void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Invalid age: " + age);
        }
        this.age = age;
    }
}
```

**Why it matters:**

1. **Invariant protection**: Because external code cannot directly set `age = -5`, you guarantee that all `Person` objects always have a valid age. You control every path into mutation.
2. **Flexibility to change internals**: If you later decide to store `age` as a `LocalDate birthDate` instead, you can do so without breaking any external code. The public API (`getAge()`) remains unchanged.
3. **Reduced coupling**: External code depends only on the class's public interface, not on implementation details. This makes the system easier to maintain and refactor.
4. **Improved testability**: A well-encapsulated class is a self-contained unit that is easy to test in isolation.

A class that exposes all its fields as `public` has zero encapsulation. Any part of the codebase can corrupt the state, making bugs extremely hard to trace. Encapsulation enforces a single point of control for every state transition.

**Key Points:**
- Private fields + public methods = classic encapsulation
- Setters can enforce validation, preventing invalid object states
- Allows changing internal representation without breaking external callers
- The more encapsulated a class, the easier it is to reason about, test, and refactor

---

## Q3: What is inheritance vs composition? When do you prefer composition?

**Answer:**
**Inheritance** models an IS-A relationship. When class `B extends A`, you are saying that every `B` IS-A `A`. The subclass inherits all non-private fields and methods of the superclass. It is a powerful mechanism for code reuse, but it comes with strong coupling — the subclass is intimately tied to the superclass's implementation.

```java
// Inheritance
public class Vehicle {
    public void startEngine() { System.out.println("Vroom"); }
}

public class Car extends Vehicle {
    // Car IS-A Vehicle
}
```

**Composition** models a HAS-A relationship. Instead of extending a class, you hold a reference to an instance of it and delegate behavior to it. This is a looser coupling because you depend on the object's interface rather than its internal implementation.

```java
// Composition
public class Engine {
    public void start() { System.out.println("Vroom"); }
}

public class Car {
    private final Engine engine; // Car HAS-A Engine

    public Car(Engine engine) { this.engine = engine; }

    public void startCar() { engine.start(); }
}
```

**When to prefer composition (almost always):**

1. **The IS-A relationship is not truly semantic**: `Stack extends Vector` in the Java standard library is a famous mistake. A `Stack` is not a `Vector` — it should not expose `add(index, element)`. Composition would have been correct.
2. **You want to swap implementations at runtime**: With composition, you can inject a different `Engine` type. With inheritance, the relationship is fixed at compile time.
3. **You want to avoid the fragile base class problem**: In inheritance, changes to the superclass can inadvertently break all subclasses. Composition is immune to this.
4. **Java does not support multiple inheritance**: If `Car` needs behavior from both `Vehicle` and `InsuredItem`, composition handles this naturally by holding references to both.

The GoF book's advice is "favor object composition over class inheritance." Use inheritance only when the IS-A relationship is natural, stable, and the subclass truly satisfies the Liskov Substitution Principle.

**Key Points:**
- Inheritance = IS-A; tight coupling; compile-time binding
- Composition = HAS-A; loose coupling; runtime flexibility
- Prefer composition to avoid the fragile base class problem and to keep designs open to change
- Inheritance is appropriate when the relationship is genuinely semantic and stable (e.g., `Dog extends Animal`)

---

## Q4: What is polymorphism? Give a real-world example in Java.

**Answer:**
Polymorphism (Greek for "many forms") is the ability of a single interface or method call to behave differently depending on the actual type of the object at runtime. In Java, polymorphism comes in two forms.

**Compile-time polymorphism (static binding / method overloading)**: The compiler decides which method to call based on the method signature (number and type of parameters). This is resolved at compile time.

```java
public class Calculator {
    public int add(int a, int b) { return a + b; }
    public double add(double a, double b) { return a + b; }
    public String add(String a, String b) { return a + b; }
}
```

**Runtime polymorphism (dynamic binding / method overriding)**: The JVM decides which method to call at runtime based on the actual object type, even when the reference type is a supertype. This is the more powerful and conceptually important form.

**Real-world example — a payment processing system:**

```java
public abstract class PaymentMethod {
    public abstract void processPayment(double amount);
}

public class CreditCard extends PaymentMethod {
    public void processPayment(double amount) {
        System.out.println("Charging $" + amount + " to credit card");
    }
}

public class PayPal extends PaymentMethod {
    public void processPayment(double amount) {
        System.out.println("Sending $" + amount + " via PayPal");
    }
}

public class Bitcoin extends PaymentMethod {
    public void processPayment(double amount) {
        System.out.println("Broadcasting $" + amount + " BTC transaction");
    }
}

// Client code
public class Checkout {
    public void complete(PaymentMethod method, double total) {
        method.processPayment(total); // polymorphic call
    }
}

// Usage
Checkout checkout = new Checkout();
checkout.complete(new CreditCard(), 49.99);
checkout.complete(new PayPal(), 49.99);
checkout.complete(new Bitcoin(), 49.99);
```

The `Checkout` class has no knowledge of which payment type is being used. When a new payment method is added, `Checkout` does not need to change. This is how polymorphism enables the Open/Closed Principle.

The key mechanism is Java's virtual method table (vtable). Every object carries a pointer to a dispatch table, and method calls on a reference go through this table to find the correct implementation.

**Key Points:**
- Runtime polymorphism is achieved via method overriding and dynamic dispatch (vtable lookup)
- Compile-time polymorphism is achieved via method overloading (resolved by the compiler)
- Polymorphism lets you write code against an interface and extend behavior without changing existing client code
- `instanceof` checks in client code are usually a sign that polymorphism should be used instead

---

## Q5: What is the Liskov Substitution Principle? Show a violation example.

**Answer:**
The Liskov Substitution Principle (LSP), introduced by Barbara Liskov in 1987, states: "If S is a subtype of T, then objects of type T may be replaced with objects of type S without altering any of the desirable properties of the program."

In practical terms: any code that works correctly with a base class instance must continue to work correctly when you substitute a subclass instance. The subclass must honor the contract established by the superclass — it cannot strengthen preconditions, weaken postconditions, or throw exceptions that the superclass would not throw.

**Classic LSP violation — the Rectangle/Square problem:**

```java
// Base class
public class Rectangle {
    protected int width;
    protected int height;

    public void setWidth(int w) { this.width = w; }
    public void setHeight(int h) { this.height = h; }
    public int area() { return width * height; }
}

// Subclass that violates LSP
public class Square extends Rectangle {
    @Override
    public void setWidth(int w) {
        this.width = w;
        this.height = w; // must keep sides equal
    }

    @Override
    public void setHeight(int h) {
        this.height = h;
        this.width = h; // must keep sides equal
    }
}

// This test passes for Rectangle but FAILS for Square
public void testArea(Rectangle r) {
    r.setWidth(5);
    r.setHeight(10);
    assert r.area() == 50; // fails for Square! area() returns 100
}
```

The `Square` class changes the behavior that callers expect from a `Rectangle`. Setting width to 5 and height to 10 gives area 50 for a `Rectangle`, but area 100 for a `Square` because `setHeight` also changes the width. This violates the contract.

**The fix:** Do not model this as inheritance. Either make both classes independent implementations of a `Shape` interface, or make `Rectangle` immutable so there is no setter behavior to violate.

```java
public interface Shape {
    int area();
}

public final class Rectangle implements Shape { ... }
public final class Square implements Shape { ... }
```

**Other common LSP violations:**
- A subclass that overrides a method and throws a new unchecked exception
- A subclass that ignores a method call instead of implementing it (e.g., `UnsupportedOperationException`)
- A subclass that strengthens a precondition (e.g., only accepts positive numbers where the base class accepts any number)

**Key Points:**
- LSP: subclasses must be substitutable for their base types without breaking correctness
- Violations are often a sign that the IS-A relationship is not semantically valid
- "Is-a" in natural language does not always mean "extends" in code (a square IS-A rectangle geometrically, but not in behavioral terms)
- Interfaces and composition are often the correct fix for LSP violations

---

## Q6: What is the Open/Closed Principle?

**Answer:**
The Open/Closed Principle (OCP), coined by Bertrand Meyer and popularized by Robert Martin, states: "Software entities (classes, modules, functions) should be open for extension but closed for modification."

This means that when new functionality is needed, you should be able to add it without changing existing, tested code. Instead of modifying a class, you extend it or provide a new implementation of an interface.

**Why it matters:** Every time you modify existing code, you risk introducing bugs in functionality that was already working. If you can add behavior by adding new code rather than changing old code, the system becomes more stable over time.

**Violation of OCP:**

```java
// Every time a new shape is added, this class must be modified
public class AreaCalculator {
    public double calculate(Object shape) {
        if (shape instanceof Circle) {
            Circle c = (Circle) shape;
            return Math.PI * c.getRadius() * c.getRadius();
        } else if (shape instanceof Rectangle) {
            Rectangle r = (Rectangle) shape;
            return r.getWidth() * r.getHeight();
        }
        // Adding Triangle requires modifying this method — OCP violation
        throw new IllegalArgumentException("Unknown shape");
    }
}
```

**OCP-compliant design:**

```java
public interface Shape {
    double area();
}

public class Circle implements Shape {
    private double radius;
    public Circle(double r) { this.radius = r; }
    public double area() { return Math.PI * radius * radius; }
}

public class Rectangle implements Shape {
    private double w, h;
    public Rectangle(double w, double h) { this.w = w; this.h = h; }
    public double area() { return w * h; }
}

// Adding Triangle requires NO modification to AreaCalculator
public class Triangle implements Shape {
    private double base, height;
    public Triangle(double b, double h) { this.base = b; this.height = h; }
    public double area() { return 0.5 * base * height; }
}

public class AreaCalculator {
    public double calculate(Shape shape) {
        return shape.area(); // closed for modification
    }
}
```

Now `AreaCalculator` never needs to change. Adding a new shape is purely additive — you write a new class. The "closed" part is enforced by the fact that nothing in the existing code needs to know about the new type.

In practice, OCP is achieved through polymorphism, the Strategy pattern, the Decorator pattern, and dependency injection. The key insight is to identify the axes of variation in your system and abstract those axes behind interfaces.

**Key Points:**
- Open for extension = new behavior can be added; closed for modification = existing code is not changed
- Achieved via abstraction, polymorphism, and design patterns (Strategy, Decorator, etc.)
- Prevents regression bugs caused by modifying working code
- `instanceof` chains in methods are a classic OCP violation signal

---

## Q7: What is the Single Responsibility Principle? Show a violation.

**Answer:**
The Single Responsibility Principle (SRP) states: "A class should have only one reason to change." This was articulated by Robert Martin. The "reason to change" is tied to a specific actor or stakeholder in the system — a class should serve only one such actor.

A class with multiple responsibilities is also called a "God class." It is hard to understand, hard to test, and very likely to break in unexpected ways when modified for one of its responsibilities.

**Classic SRP violation:**

```java
// This class does too many things — it has multiple reasons to change
public class Employee {
    private String name;
    private double salary;

    // Business logic (HR department cares about this)
    public double calculatePay() {
        // Complex pay calculation logic
        return salary * 1.2;
    }

    // Persistence (Database team cares about this)
    public void saveToDatabase() {
        // SQL INSERT or UPDATE logic
        System.out.println("INSERT INTO employees VALUES ...");
    }

    // Reporting (Finance department cares about this)
    public void generateReport() {
        // PDF/CSV generation logic
        System.out.println("Generating employee report...");
    }
}
```

This class has three reasons to change:
1. If the pay calculation rules change (HR)
2. If the database schema changes (DBA)
3. If the report format changes (Finance)

A change for any one of these stakeholders risks breaking the code used by the others.

**SRP-compliant refactoring:**

```java
// Each class has exactly one reason to change

public class Employee {
    private String name;
    private double salary;
    public String getName() { return name; }
    public double getSalary() { return salary; }
}

public class PayCalculator {
    public double calculatePay(Employee e) {
        return e.getSalary() * 1.2;
    }
}

public class EmployeeRepository {
    public void save(Employee e) {
        System.out.println("INSERT INTO employees ...");
    }
}

public class EmployeeReportGenerator {
    public void generateReport(Employee e) {
        System.out.println("Generating report for " + e.getName());
    }
}
```

Now each class has one reason to change and one stakeholder it serves. Testing is simpler because each class can be tested in isolation. The `Employee` class holds data; `PayCalculator` holds business rules; `EmployeeRepository` handles persistence; `EmployeeReportGenerator` handles presentation.

**Key Points:**
- SRP = one reason to change = one stakeholder or actor served
- God classes violate SRP and become hard to test, understand, and maintain
- SRP often leads to more, smaller classes — this is generally good
- SRP is not about "one method per class"; it is about cohesion of purpose

---

## Q8: What is the Interface Segregation Principle?

**Answer:**
The Interface Segregation Principle (ISP) states: "No client should be forced to depend on methods it does not use." This principle, part of Robert Martin's SOLID principles, advises splitting large interfaces into smaller, more focused ones so that classes only need to implement the methods relevant to their role.

When a class is forced to implement interface methods it does not need, it must either provide a meaningless or throwing implementation, which is a design smell and a maintenance burden.

**ISP violation:**

```java
// Fat interface — not every "worker" can do all of these
public interface Worker {
    void work();
    void eat();   // Robots don't eat
    void sleep(); // Robots don't sleep
}

public class HumanWorker implements Worker {
    public void work() { System.out.println("Working"); }
    public void eat() { System.out.println("Eating"); }
    public void sleep() { System.out.println("Sleeping"); }
}

// Robot is forced to implement methods it cannot use
public class RobotWorker implements Worker {
    public void work() { System.out.println("Working"); }
    public void eat() { throw new UnsupportedOperationException("Robots don't eat!"); }
    public void sleep() { throw new UnsupportedOperationException("Robots don't sleep!"); }
}
```

`RobotWorker` is forced to depend on (and implement) `eat()` and `sleep()`, which are meaningless for it.

**ISP-compliant refactoring:**

```java
public interface Workable {
    void work();
}

public interface Eatable {
    void eat();
}

public interface Sleepable {
    void sleep();
}

// Human implements all three
public class HumanWorker implements Workable, Eatable, Sleepable {
    public void work() { System.out.println("Working"); }
    public void eat() { System.out.println("Eating"); }
    public void sleep() { System.out.println("Sleeping"); }
}

// Robot only implements what it needs
public class RobotWorker implements Workable {
    public void work() { System.out.println("Working"); }
}
```

Now clients can depend on exactly the interface they need:

```java
// This method only requires a Workable — it can work with both humans and robots
public void assignWork(Workable worker) {
    worker.work();
}
```

ISP often goes hand in hand with the Dependency Inversion Principle. By depending on small, focused interfaces, clients are shielded from changes in unrelated parts of the system.

**Key Points:**
- Fat interfaces force implementors to provide dummy or throwing implementations
- Segregate interfaces by client role or capability
- Small, focused interfaces increase cohesion and reduce coupling
- Java's functional interfaces (one abstract method) are the ultimate expression of ISP

---

## Q9: What is the Dependency Inversion Principle?

**Answer:**
The Dependency Inversion Principle (DIP) states two things:
1. High-level modules should not depend on low-level modules. Both should depend on abstractions.
2. Abstractions should not depend on details. Details should depend on abstractions.

In simpler terms: your business logic should not directly instantiate or depend on concrete infrastructure classes (like `MySQLDatabase`, `FileLogger`, or `EmailSender`). Instead, it should depend on interfaces, and the concrete implementations are supplied from outside.

**DIP violation:**

```java
// High-level module directly depends on a low-level concrete class
public class OrderService {
    private MySQLDatabase database = new MySQLDatabase(); // concrete dependency

    public void placeOrder(Order order) {
        database.save(order); // tightly coupled to MySQL
    }
}
```

If you want to switch to PostgreSQL, or use an in-memory database for testing, you must modify `OrderService`. The high-level business logic is polluted by infrastructure concerns.

**DIP-compliant design:**

```java
// Abstraction — both high-level and low-level modules depend on this
public interface OrderRepository {
    void save(Order order);
}

// Low-level detail implements the abstraction
public class MySQLOrderRepository implements OrderRepository {
    public void save(Order order) {
        System.out.println("Saving order to MySQL...");
    }
}

public class InMemoryOrderRepository implements OrderRepository {
    private List<Order> orders = new ArrayList<>();
    public void save(Order order) { orders.add(order); }
}

// High-level module depends only on the abstraction
public class OrderService {
    private final OrderRepository repository;

    // Dependency is injected (from outside), not created inside
    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public void placeOrder(Order order) {
        repository.save(order); // talks to abstraction only
    }
}

// In production
OrderService service = new OrderService(new MySQLOrderRepository());

// In tests
OrderService testService = new OrderService(new InMemoryOrderRepository());
```

DIP is closely related to Dependency Injection (DI). DIP is the principle; DI is the technique that implements it. Together, they make code highly testable and flexible.

**Key Points:**
- DIP inverts the traditional "high-level calls low-level" direction of dependency
- Both layers depend on an interface defined by the high-level layer's needs
- Enables swapping implementations without changing business logic
- DI frameworks (Spring, Guice) automate the wiring of DIP-compliant code

---

## Q10: What is the difference between coupling and cohesion?

**Answer:**
Coupling and cohesion are two complementary metrics for evaluating the quality of a software design. The goal is **low coupling** and **high cohesion**.

**Coupling** measures the degree to which one module (class, package, service) depends on another. High coupling means a change in one module is likely to require changes in another, making the system fragile and hard to change.

Types of coupling from worst to best:
- **Content coupling**: Module A directly accesses or modifies internal data of Module B (e.g., accessing private fields via reflection)
- **Control coupling**: A passes a flag to B that controls B's internal behavior (e.g., `processPayment(amount, true)` where `true` means "use credit card")
- **Data coupling**: A and B share only the data they need, through clean method parameters — the most desirable form

```java
// High coupling — OrderService creates its own dependencies
public class OrderService {
    private MySQLDatabase db = new MySQLDatabase();
    private EmailService emailService = new EmailService();
    // ...
}

// Low coupling — depends on interfaces, receives them from outside
public class OrderService {
    public OrderService(OrderRepository repo, NotificationService notifications) { ... }
}
```

**Cohesion** measures how strongly the elements within a single module belong together — how well the methods and fields of a class serve a single, unified purpose.

Types of cohesion from best to worst:
- **Functional cohesion** (best): Every method contributes to a single well-defined task (e.g., a `PasswordHasher` class)
- **Sequential cohesion**: Methods form a pipeline where output of one feeds input of the next
- **Coincidental cohesion** (worst): Methods are grouped together for no logical reason — a "Utils" class with 50 unrelated static methods

```java
// Low cohesion — handles unrelated concerns
public class Utils {
    public String formatDate(Date d) { ... }
    public void sendEmail(String to, String body) { ... }
    public double calculateTax(double amount) { ... }
}

// High cohesion — all methods serve one focused purpose
public class TaxCalculator {
    public double calculateSalesTax(double amount) { ... }
    public double calculateIncomeTax(double income) { ... }
    public double applyDiscount(double amount, double rate) { ... }
}
```

**The relationship:** High cohesion tends to naturally produce low coupling because a focused class depends on fewer external things. A class that does everything needs to import everything. These two metrics together guide you toward the Single Responsibility Principle.

**Key Points:**
- Low coupling = minimal dependencies between modules; changes are localized
- High cohesion = elements within a module work toward one unified purpose
- These two goals are complementary and reinforce each other
- Coupling is between modules; cohesion is within a module

---

## Q11: What is a design pattern? Why use them?

**Answer:**
A design pattern is a reusable, named solution to a commonly recurring problem in software design. Design patterns are not finished code or libraries — they are templates or blueprints that describe how to structure classes and objects to solve a particular category of problem.

The concept was popularized by the "Gang of Four" (GoF) — Erich Gamma, Richard Helm, Ralph Johnson, and John Vlissides — in their seminal 1994 book "Design Patterns: Elements of Reusable Object-Oriented Software." They catalogued 23 fundamental patterns organized into three categories:

1. **Creational patterns**: Concerned with object creation mechanisms (Singleton, Factory Method, Abstract Factory, Builder, Prototype)
2. **Structural patterns**: Concerned with how classes and objects are composed to form larger structures (Adapter, Bridge, Composite, Decorator, Facade, Flyweight, Proxy)
3. **Behavioral patterns**: Concerned with communication and responsibility between objects (Chain of Responsibility, Command, Iterator, Mediator, Memento, Observer, State, Strategy, Template Method, Visitor)

**Why use design patterns?**

1. **Shared vocabulary**: When you say "use a Strategy here," every experienced developer immediately understands the intent, the structure, and the trade-offs. This dramatically speeds up design discussions.
2. **Proven solutions**: Patterns are distilled from decades of real-world experience. They have been tested across many codebases and contexts.
3. **Avoid reinventing the wheel**: Instead of devising a custom solution for a well-known problem, you apply the appropriate pattern.
4. **Documentation**: A codebase that uses recognized patterns is self-documenting to a degree. The pattern name communicates the intent better than any comment.
5. **Flexibility**: Most patterns introduce abstractions that make the system open to extension without modification.

**Caution:** Over-applying patterns leads to over-engineering. A pattern introduces abstraction and indirection, which has a complexity cost. Apply a pattern only when you have the problem it solves, not preemptively.

**Key Points:**
- Design patterns are named, reusable solutions to recurring OOP design problems
- Categorized as Creational, Structural, and Behavioral
- Provide a shared vocabulary among developers
- Should be applied to solve a real problem, not preemptively

---

## Q12: What is the Singleton pattern? What are its problems? How do you make it thread-safe?

**Answer:**
The Singleton pattern ensures that a class has only one instance throughout the application's lifetime, and provides a global point of access to that instance. It is a creational pattern.

**Basic (non-thread-safe) implementation:**

```java
public class Configuration {
    private static Configuration instance;
    private String dbUrl;

    private Configuration() {
        // private constructor prevents external instantiation
        this.dbUrl = "jdbc:mysql://localhost:3306/mydb";
    }

    public static Configuration getInstance() {
        if (instance == null) { // NOT thread-safe
            instance = new Configuration();
        }
        return instance;
    }

    public String getDbUrl() { return dbUrl; }
}
```

**Problems with Singleton:**
1. **Global state**: A singleton is essentially a global variable. Any part of the code can access and potentially mutate it, making behavior hard to trace.
2. **Hidden dependencies**: When a class calls `Configuration.getInstance()` internally, callers cannot see that it depends on `Configuration`. This violates DIP.
3. **Tight coupling**: Code that calls `getInstance()` is tightly coupled to the singleton class.
4. **Testing difficulties**: You cannot easily substitute a mock for a singleton, making unit testing painful.
5. **Thread safety**: The basic implementation has a race condition — two threads can both see `instance == null` and both create an instance.
6. **Classloader issues**: In complex applications with multiple classloaders, you can end up with multiple instances.

**Thread-safe implementations:**

**Option 1: Synchronized method (simple but slow):**
```java
public static synchronized Configuration getInstance() {
    if (instance == null) {
        instance = new Configuration();
    }
    return instance;
}
```
Every call acquires a lock, even after initialization. High contention in multi-threaded apps.

**Option 2: Double-checked locking (fast, correct with `volatile`):**
```java
public class Configuration {
    private static volatile Configuration instance; // volatile is critical!

    public static Configuration getInstance() {
        if (instance == null) {
            synchronized (Configuration.class) {
                if (instance == null) {
                    instance = new Configuration();
                }
            }
        }
        return instance;
    }
}
```
The `volatile` keyword prevents the JVM from reordering the object construction and the assignment of the reference, which could otherwise cause another thread to see a partially-constructed object.

**Option 3: Initialization-on-demand holder (best — lazy, thread-safe, no synchronization overhead):**
```java
public class Configuration {
    private Configuration() {}

    private static class Holder {
        // Initialized by the JVM class loader, which guarantees thread safety
        private static final Configuration INSTANCE = new Configuration();
    }

    public static Configuration getInstance() {
        return Holder.INSTANCE;
    }
}
```

**Option 4: Enum Singleton (Joshua Bloch's recommendation for true singletons):**
```java
public enum DatabaseConnection {
    INSTANCE;

    public void query(String sql) { System.out.println("Querying: " + sql); }
}
// Usage: DatabaseConnection.INSTANCE.query("SELECT ...");
```
Enum singletons are inherently thread-safe, serialization-safe, and reflection-safe. They are the strongest guarantee of a single instance.

**Key Points:**
- Singleton ensures one instance + global access point
- Main problems: global state, hidden dependencies, testing difficulty, thread safety
- Use the holder pattern or enum for the safest implementation
- Modern codebases often prefer DI frameworks (Spring `@Bean` with default singleton scope) over hand-rolled singletons

---

## Q13: What is the Factory pattern? Show a simple example.

**Answer:**
The Factory pattern is a creational design pattern that provides a way to create objects without exposing the instantiation logic to the client. The client requests an object of a given type, and the factory decides which concrete class to instantiate. This decouples the client from the concrete classes it uses.

There are two closely related patterns:

**Factory Method**: Defines an interface for creating an object, but lets subclasses decide which class to instantiate.

**Simple Factory** (not technically a GoF pattern but extremely common): A single class with a static method that creates objects based on a parameter.

**Simple Factory example — a shape creator:**

```java
public interface Shape {
    void draw();
}

public class Circle implements Shape {
    public void draw() { System.out.println("Drawing Circle"); }
}

public class Rectangle implements Shape {
    public void draw() { System.out.println("Drawing Rectangle"); }
}

public class Triangle implements Shape {
    public void draw() { System.out.println("Drawing Triangle"); }
}

// The Factory — centralizes object creation
public class ShapeFactory {
    public static Shape createShape(String type) {
        return switch (type.toLowerCase()) {
            case "circle" -> new Circle();
            case "rectangle" -> new Rectangle();
            case "triangle" -> new Triangle();
            default -> throw new IllegalArgumentException("Unknown shape: " + type);
        };
    }
}

// Client code
Shape s1 = ShapeFactory.createShape("circle");
Shape s2 = ShapeFactory.createShape("rectangle");
s1.draw(); // Drawing Circle
s2.draw(); // Drawing Rectangle
```

**Factory Method pattern:**

```java
// Creator — declares the factory method
public abstract class Dialog {
    public void render() {
        Button btn = createButton(); // factory method call
        btn.render();
    }

    protected abstract Button createButton(); // factory method
}

public class WindowsDialog extends Dialog {
    protected Button createButton() { return new WindowsButton(); }
}

public class MacDialog extends Dialog {
    protected Button createButton() { return new MacButton(); }
}
```

**Benefits of the Factory pattern:**
1. Client code is decoupled from concrete implementations
2. Adding a new type only requires a new class and a new `case` in the factory (or a new subclass for Factory Method)
3. Construction logic is centralized in one place — easier to change
4. Enables returning cached instances, subclasses, or mock objects in tests

**Key Points:**
- Factory centralizes object creation and hides concrete types from the client
- Simple Factory: static method with switch; Factory Method: subclasses override creation
- Supports OCP — adding new types requires adding new code, not changing existing code
- Essential for building flexible, testable systems

---

## Q14: What is the Strategy pattern? Give a real-world example.

**Answer:**
The Strategy pattern is a behavioral design pattern that defines a family of algorithms, encapsulates each one in a separate class, and makes them interchangeable. The pattern lets the algorithm vary independently from clients that use it. It is one of the most widely used patterns in practice.

The core structure involves:
- A **Strategy interface** declaring the algorithm
- **Concrete Strategies** implementing specific algorithms
- A **Context** class that holds a reference to a `Strategy` and delegates to it

**Real-world example — a sorting or pricing strategy in an e-commerce system:**

```java
// Strategy interface
public interface DiscountStrategy {
    double applyDiscount(double originalPrice);
}

// Concrete strategies
public class NoDiscount implements DiscountStrategy {
    public double applyDiscount(double price) { return price; }
}

public class PercentageDiscount implements DiscountStrategy {
    private final double percent;
    public PercentageDiscount(double percent) { this.percent = percent; }
    public double applyDiscount(double price) {
        return price * (1 - percent / 100);
    }
}

public class FlatDiscount implements DiscountStrategy {
    private final double amount;
    public FlatDiscount(double amount) { this.amount = amount; }
    public double applyDiscount(double price) {
        return Math.max(0, price - amount);
    }
}

public class VIPDiscount implements DiscountStrategy {
    public double applyDiscount(double price) {
        return price * 0.5; // 50% for VIP members
    }
}

// Context
public class PriceCalculator {
    private DiscountStrategy strategy;

    public PriceCalculator(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(DiscountStrategy strategy) {
        this.strategy = strategy; // strategy can be swapped at runtime
    }

    public double calculateFinalPrice(double basePrice) {
        return strategy.applyDiscount(basePrice);
    }
}

// Usage
PriceCalculator calc = new PriceCalculator(new NoDiscount());
System.out.println(calc.calculateFinalPrice(100)); // 100.0

calc.setStrategy(new PercentageDiscount(20));
System.out.println(calc.calculateFinalPrice(100)); // 80.0

calc.setStrategy(new VIPDiscount());
System.out.println(calc.calculateFinalPrice(100)); // 50.0
```

**Strategy vs. if-else:** Without Strategy, the `PriceCalculator` would contain a chain of `if-else` or `switch` statements. Adding a new discount type would require modifying `PriceCalculator`. With Strategy, adding a new type is purely additive — you just add a new class.

**Strategy in the JDK:** `Comparator` is the most prominent Strategy interface in Java. When you pass a `Comparator` to `Collections.sort()`, you are using the Strategy pattern. With lambdas, Strategies became even more lightweight:

```java
List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
names.sort((a, b) -> a.compareTo(b)); // lambda as a Strategy
```

**Key Points:**
- Strategy defines a family of algorithms and makes them interchangeable at runtime
- Eliminates if-else/switch chains based on "type of algorithm" decisions
- Context delegates to whichever strategy it currently holds
- Java's `Comparator`, `Runnable`, and all functional interfaces are Strategy instances

---

## Q15: What is the Observer pattern? Show a simple Java example.

**Answer:**
The Observer pattern is a behavioral design pattern that defines a one-to-many dependency between objects. When one object (the Subject or Observable) changes state, all its registered dependents (Observers) are automatically notified and updated. It is the foundation of event-driven programming.

The pattern decouples the subject from its observers — the subject does not need to know anything about the concrete observer types; it only knows that they implement the `Observer` interface.

**Simple Java example — a news agency and subscribers:**

```java
// Observer interface
public interface Observer {
    void update(String headline);
}

// Subject interface
public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}

// Concrete Subject
public class NewsAgency implements Subject {
    private final List<Observer> observers = new ArrayList<>();
    private String latestHeadline;

    public void registerObserver(Observer o) { observers.add(o); }
    public void removeObserver(Observer o) { observers.remove(o); }

    public void notifyObservers() {
        for (Observer o : observers) {
            o.update(latestHeadline);
        }
    }

    public void publishHeadline(String headline) {
        this.latestHeadline = headline;
        notifyObservers(); // automatically notify all registered observers
    }
}

// Concrete Observers
public class EmailSubscriber implements Observer {
    private final String email;
    public EmailSubscriber(String email) { this.email = email; }

    public void update(String headline) {
        System.out.println("Email to " + email + ": " + headline);
    }
}

public class MobileAppSubscriber implements Observer {
    private final String userId;
    public MobileAppSubscriber(String userId) { this.userId = userId; }

    public void update(String headline) {
        System.out.println("Push notification to " + userId + ": " + headline);
    }
}

// Usage
NewsAgency agency = new NewsAgency();
agency.registerObserver(new EmailSubscriber("alice@example.com"));
agency.registerObserver(new EmailSubscriber("bob@example.com"));
agency.registerObserver(new MobileAppSubscriber("user_42"));

agency.publishHeadline("Java 25 Released!");
// Output:
// Email to alice@example.com: Java 25 Released!
// Email to bob@example.com: Java 25 Released!
// Push notification to user_42: Java 25 Released!
```

**Observer in the Java standard library:**
- `java.util.Observer` / `java.util.Observable` (deprecated in Java 9 — the implementation had design issues)
- `PropertyChangeListener` in JavaBeans
- All AWT/Swing event listeners (`ActionListener`, `MouseListener`, etc.)
- `java.util.EventListener`
- Reactive streams (`Flow.Publisher` / `Flow.Subscriber` in Java 9+)

**Push vs. Pull model:**
- **Push**: Subject sends the changed data directly to observers (as shown above)
- **Pull**: Subject only notifies observers "something changed," and observers call back to query what they need

**Key Points:**
- Observer decouples subject from observers — subject only knows the Observer interface
- Core to event-driven and reactive programming
- Used in GUI frameworks, message buses, and reactive streams
- Consider thread safety if observers are notified from multiple threads

---

## Q16: What is the Builder pattern? When should you use it?

**Answer:**
The Builder pattern is a creational design pattern that separates the construction of a complex object from its representation. It allows you to construct complex objects step by step. The same construction process can create different representations.

It is most commonly used to address two problems:
1. **Telescoping constructor anti-pattern**: A class with many optional parameters leads to many constructor overloads or a single constructor with many parameters, most of which are often `null` or default values.
2. **Immutable objects with many fields**: You want an immutable object (no setters) but need a convenient way to set many fields during construction.

**Without Builder (telescoping constructor problem):**

```java
// Hard to read — what does 'null, true, false, 200' mean?
Pizza pizza = new Pizza("Large", "Thin", "Tomato", null, true, false, 200);
```

**With Builder:**

```java
public class Pizza {
    private final String size;
    private final String crustType;
    private final String sauce;
    private final String cheese;
    private final boolean extraCheese;
    private final boolean pepperoni;
    private final int calories;

    // Private constructor — only Builder can call it
    private Pizza(Builder builder) {
        this.size = builder.size;
        this.crustType = builder.crustType;
        this.sauce = builder.sauce;
        this.cheese = builder.cheese;
        this.extraCheese = builder.extraCheese;
        this.pepperoni = builder.pepperoni;
        this.calories = builder.calories;
    }

    public static class Builder {
        // Required fields
        private final String size;

        // Optional fields with defaults
        private String crustType = "Regular";
        private String sauce = "Tomato";
        private String cheese = "Mozzarella";
        private boolean extraCheese = false;
        private boolean pepperoni = false;
        private int calories = 0;

        public Builder(String size) { this.size = size; }

        public Builder crustType(String crustType) {
            this.crustType = crustType;
            return this; // returns 'this' for method chaining
        }

        public Builder sauce(String sauce) { this.sauce = sauce; return this; }
        public Builder cheese(String cheese) { this.cheese = cheese; return this; }
        public Builder extraCheese() { this.extraCheese = true; return this; }
        public Builder pepperoni() { this.pepperoni = true; return this; }
        public Builder calories(int cal) { this.calories = cal; return this; }

        public Pizza build() { return new Pizza(this); }
    }
}

// Usage — readable, self-documenting, fluent
Pizza pizza = new Pizza.Builder("Large")
    .crustType("Thin")
    .sauce("BBQ")
    .extraCheese()
    .pepperoni()
    .calories(850)
    .build();
```

**When to use Builder:**
1. When a class has more than 4-5 constructor parameters
2. When many parameters are optional
3. When you want to construct an immutable object
4. When you want a readable, fluent API for object construction
5. When the construction process must enforce a specific sequence of steps

**Builder in the JDK:** `StringBuilder`, `StringJoiner`, `HttpClient.Builder` (Java 11+), `Stream.Builder`, `ProcessBuilder`.

**Lombok alternative:** In modern Java projects, `@Builder` from Project Lombok generates the builder pattern automatically.

**Key Points:**
- Solves the telescoping constructor anti-pattern
- Produces readable, fluent construction syntax
- Works well with immutable objects (no setters needed)
- Inner `Builder` class holds mutable state during construction; the built object is immutable

---

## Q17: What is the Decorator pattern?

**Answer:**
The Decorator pattern is a structural design pattern that attaches additional responsibilities to an object dynamically. It provides a flexible alternative to subclassing for extending functionality. Decorators wrap an object in a transparent shell that adds behavior before or after delegating to the wrapped object.

The key insight is that all decorators implement the same interface as the object they wrap. Client code sees no difference between a decorated and an undecorated object.

**Classic example — a text file reader with optional compression and encryption:**

```java
// Component interface
public interface DataReader {
    String read();
}

// Concrete component
public class FileDataReader implements DataReader {
    private final String filename;
    public FileDataReader(String filename) { this.filename = filename; }
    public String read() { return "raw content from " + filename; }
}

// Base Decorator — implements the interface and holds a reference to a component
public abstract class DataReaderDecorator implements DataReader {
    protected final DataReader wrapped;
    public DataReaderDecorator(DataReader wrapped) { this.wrapped = wrapped; }
    public String read() { return wrapped.read(); } // delegates by default
}

// Concrete decorators
public class EncryptionDecorator extends DataReaderDecorator {
    public EncryptionDecorator(DataReader wrapped) { super(wrapped); }
    public String read() {
        String data = wrapped.read();
        return decrypt(data);
    }
    private String decrypt(String data) { return "[decrypted: " + data + "]"; }
}

public class CompressionDecorator extends DataReaderDecorator {
    public CompressionDecorator(DataReader wrapped) { super(wrapped); }
    public String read() {
        String data = wrapped.read();
        return decompress(data);
    }
    private String decompress(String data) { return "[decompressed: " + data + "]"; }
}

// Usage — decorators can be stacked in any order
DataReader reader = new FileDataReader("data.bin");
DataReader encrypted = new EncryptionDecorator(reader);
DataReader encryptedAndCompressed = new CompressionDecorator(encrypted);

System.out.println(encryptedAndCompressed.read());
// [decompressed: [decrypted: raw content from data.bin]]
```

**Decorator in the Java standard library — the most famous example is `java.io`:**

```java
// Stacking I/O decorators
InputStream in = new BufferedInputStream(
                     new GZIPInputStream(
                         new FileInputStream("data.gz")));
```

`FileInputStream` is the base component. `GZIPInputStream` adds decompression. `BufferedInputStream` adds buffering. All implement `InputStream`.

**Decorator vs. Subclassing:**
- With subclassing, you need a separate class for every combination: `EncryptedFileReader`, `CompressedFileReader`, `EncryptedCompressedFileReader` — combinatorial explosion.
- With Decorator, you compose behaviors at runtime with no class explosion.

**Key Points:**
- Decorator wraps an object and adds behavior while preserving the same interface
- Enables flexible, composable feature addition at runtime
- Avoids subclass explosion when many combinations of features are possible
- Java I/O streams are the canonical example of the Decorator pattern in practice

---

## Q18: What is the Repository pattern?

**Answer:**
The Repository pattern is a design pattern (not in the original GoF 23, but widely used in Domain-Driven Design) that mediates between the domain model and the data mapping layer. It provides a collection-like interface for accessing domain objects, abstracting the underlying persistence mechanism.

The repository acts like an in-memory collection of domain objects. The domain layer works with it as if it were a simple collection (`add`, `remove`, `findById`, `findAll`), completely unaware of how data is stored (SQL, NoSQL, in-memory, REST API, etc.).

**Without Repository (tight coupling to persistence):**

```java
public class OrderService {
    public Order findOrder(Long id) {
        // Business logic directly embedded with SQL
        String sql = "SELECT * FROM orders WHERE id = " + id;
        // JDBC/JPA code here
        // ...
    }
}
```

**With Repository:**

```java
// Repository interface — part of the domain layer
public interface OrderRepository {
    Optional<Order> findById(Long id);
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);
    void save(Order order);
    void delete(Long id);
}

// JPA implementation — part of the infrastructure layer
public class JpaOrderRepository implements OrderRepository {
    @PersistenceContext
    private EntityManager em;

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(em.find(Order.class, id));
    }

    public List<Order> findByCustomerId(Long customerId) {
        return em.createQuery(
            "SELECT o FROM Order o WHERE o.customerId = :cid", Order.class)
            .setParameter("cid", customerId)
            .getResultList();
    }

    public void save(Order order) { em.persist(order); }
    public void delete(Long id) { em.remove(em.find(Order.class, id)); }
    // ...
}

// In-memory implementation — used for unit testing
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<Long, Order> store = new HashMap<>();
    private long nextId = 1;

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public void save(Order order) { store.put(nextId++, order); }
    // ...
}

// Service — depends only on the interface
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository repository) {
        this.orderRepository = repository; // DI
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
```

**Benefits:**
1. Domain layer is completely decoupled from persistence technology
2. Swapping from SQL to MongoDB requires only a new implementation, not changes to business logic
3. Unit tests can use `InMemoryOrderRepository` instead of a real database — fast and reliable
4. Persistence logic is centralized in one place per entity

**Repository in Spring Data:** Spring Data's `JpaRepository<T, ID>` is an implementation of this pattern. You define an interface, and Spring generates the implementation automatically.

**Key Points:**
- Repository abstracts the data access layer behind a collection-like interface
- Domain logic depends only on the interface, not on JPA/JDBC/NoSQL specifics
- Enables testing with fast in-memory implementations
- Central to Domain-Driven Design (DDD); widely used in Spring, JPA, and microservices

---

## Q19: What is dependency injection? What are the three types?

**Answer:**
Dependency Injection (DI) is a technique where an object's dependencies (the objects it needs to work) are provided to it from the outside, rather than created internally. The object declares what it needs; some external entity (a DI framework, a factory, or calling code) provides those dependencies.

DI is the mechanism that implements the Dependency Inversion Principle. It makes code loosely coupled, easy to test, and easy to change.

**Without DI (tight coupling — hard to test):**
```java
public class OrderService {
    private final OrderRepository repository = new MySQLOrderRepository(); // creates its own dep
    private final EmailService emailService = new SmtpEmailService();

    public void placeOrder(Order order) {
        repository.save(order);
        emailService.sendConfirmation(order);
    }
}
```
You cannot test `OrderService` without a real database and SMTP server.

**The three types of Dependency Injection:**

**1. Constructor Injection (preferred):**
Dependencies are provided through the constructor. This makes dependencies explicit, mandatory, and visible. The object is always in a fully initialized state after construction.

```java
public class OrderService {
    private final OrderRepository repository;
    private final EmailService emailService;

    public OrderService(OrderRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }
}

// In tests:
OrderService service = new OrderService(
    new InMemoryOrderRepository(),
    new MockEmailService()
);
```

**2. Setter Injection (optional dependencies):**
Dependencies are provided through setter methods after construction. Useful for optional dependencies or when the dependency needs to change at runtime.

```java
public class OrderService {
    private EmailService emailService;

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
}
```
Drawback: The object can be in an incomplete, invalid state if setters are not called.

**3. Field Injection (discouraged in production code):**
Dependencies are injected directly into fields, typically by a DI framework using reflection. Common in Spring with `@Autowired`.

```java
@Service
public class OrderService {
    @Autowired
    private OrderRepository repository; // injected by Spring

    @Autowired
    private EmailService emailService;
}
```
Drawback: Dependencies are hidden; the class cannot be instantiated without a DI container; fields cannot be `final`.

**Constructor injection is best for:**
- Mandatory dependencies
- Immutable objects (`final` fields)
- Testability without a framework

**Spring example with constructor injection (recommended):**
```java
@Service
public class OrderService {
    private final OrderRepository repository;

    @Autowired // optional in modern Spring if only one constructor exists
    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

**Key Points:**
- DI = receive dependencies from outside rather than creating them internally
- Constructor injection: mandatory deps, immutable, most testable
- Setter injection: optional deps, mutable after construction
- Field injection: convenient but hides deps and makes testing harder
- DI frameworks (Spring, Guice) automate the wiring

---

## Q20: What is an Abstract Factory pattern?

**Answer:**
The Abstract Factory pattern is a creational design pattern that provides an interface for creating families of related or dependent objects without specifying their concrete classes. It is essentially a "factory of factories."

Whereas the Factory Method pattern produces one product, the Abstract Factory produces an entire suite of related products that are designed to work together.

**Motivating problem:** You are building a UI library that must work on both Windows and macOS. Each platform needs a consistent set of UI components: buttons, checkboxes, text fields. A Windows button looks different from a macOS button. You want to ensure that when you create a Windows button, you also get Windows-styled checkboxes — not a mix of styles.

**Abstract Factory implementation:**

```java
// Abstract Products
public interface Button {
    void render();
    void onClick();
}

public interface Checkbox {
    void render();
    void check();
}

// Concrete Products — Windows family
public class WindowsButton implements Button {
    public void render() { System.out.println("Rendering Windows button"); }
    public void onClick() { System.out.println("Windows button clicked"); }
}

public class WindowsCheckbox implements Checkbox {
    public void render() { System.out.println("Rendering Windows checkbox"); }
    public void check() { System.out.println("Windows checkbox checked"); }
}

// Concrete Products — macOS family
public class MacButton implements Button {
    public void render() { System.out.println("Rendering macOS button"); }
    public void onClick() { System.out.println("macOS button clicked"); }
}

public class MacCheckbox implements Checkbox {
    public void render() { System.out.println("Rendering macOS checkbox"); }
    public void check() { System.out.println("macOS checkbox checked"); }
}

// Abstract Factory
public interface GUIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

// Concrete Factories — each creates one consistent family
public class WindowsFactory implements GUIFactory {
    public Button createButton() { return new WindowsButton(); }
    public Checkbox createCheckbox() { return new WindowsCheckbox(); }
}

public class MacFactory implements GUIFactory {
    public Button createButton() { return new MacButton(); }
    public Checkbox createCheckbox() { return new MacCheckbox(); }
}

// Client — uses only the factory interface
public class Application {
    private final Button button;
    private final Checkbox checkbox;

    public Application(GUIFactory factory) {
        this.button = factory.createButton();
        this.checkbox = factory.createCheckbox();
    }

    public void render() {
        button.render();
        checkbox.render();
    }
}

// Factory selection at startup based on OS
GUIFactory factory;
if (System.getProperty("os.name").contains("Windows")) {
    factory = new WindowsFactory();
} else {
    factory = new MacFactory();
}

Application app = new Application(factory);
app.render();
```

**Abstract Factory vs. Factory Method:**
- Factory Method creates one product; Abstract Factory creates a family of related products
- Abstract Factory often uses Factory Methods internally
- Abstract Factory guarantees that the created objects are compatible with each other

**Key Points:**
- Abstract Factory produces families of related objects that are designed to work together
- Ensures consistency across a product family (no mixing Windows buttons with Mac checkboxes)
- The client depends only on abstract factory and product interfaces
- Adding a new product family (e.g., Linux) requires adding a new factory and product set without changing client code

---

## Q21: How would you design a parking lot OOP system? (answer with classes and relationships)

**Answer:**
This is a classic OOP system design question. The key is to identify the entities, their responsibilities, and the relationships between them.

**Requirements clarification (always ask these in an interview):**
- Multiple floors? Yes
- Different vehicle types (car, motorcycle, bus)? Yes
- Different spot types for different vehicles? Yes
- Ticketing and payment? Yes
- Track availability in real time? Yes

**Core classes and design:**

```java
// Enumerations
public enum VehicleType { MOTORCYCLE, CAR, BUS }
public enum SpotType { SMALL, MEDIUM, LARGE }
public enum TicketStatus { ACTIVE, PAID, CANCELLED }

// Vehicle hierarchy
public abstract class Vehicle {
    private final String licensePlate;
    private final VehicleType type;

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public abstract SpotType getRequiredSpotType();

    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }
}

public class Car extends Vehicle {
    public Car(String plate) { super(plate, VehicleType.CAR); }
    public SpotType getRequiredSpotType() { return SpotType.MEDIUM; }
}

public class Motorcycle extends Vehicle {
    public Motorcycle(String plate) { super(plate, VehicleType.MOTORCYCLE); }
    public SpotType getRequiredSpotType() { return SpotType.SMALL; }
}

public class Bus extends Vehicle {
    public Bus(String plate) { super(plate, VehicleType.BUS); }
    public SpotType getRequiredSpotType() { return SpotType.LARGE; }
}

// Parking Spot
public class ParkingSpot {
    private final String spotId;
    private final SpotType type;
    private final int floor;
    private Vehicle currentVehicle;

    public ParkingSpot(String spotId, SpotType type, int floor) {
        this.spotId = spotId;
        this.type = type;
        this.floor = floor;
    }

    public boolean isAvailable() { return currentVehicle == null; }

    public boolean canFit(Vehicle v) {
        return isAvailable() && type == v.getRequiredSpotType();
    }

    public void park(Vehicle v) {
        if (!canFit(v)) throw new IllegalStateException("Cannot park vehicle in this spot");
        this.currentVehicle = v;
    }

    public Vehicle leave() {
        Vehicle v = currentVehicle;
        currentVehicle = null;
        return v;
    }

    public String getSpotId() { return spotId; }
    public SpotType getType() { return type; }
    public int getFloor() { return floor; }
}

// Ticket
public class ParkingTicket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private TicketStatus status;

    public ParkingTicket(String ticketId, Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = LocalDateTime.now();
        this.status = TicketStatus.ACTIVE;
    }

    public void markPaid() {
        this.exitTime = LocalDateTime.now();
        this.status = TicketStatus.PAID;
    }

    public Duration getDuration() {
        LocalDateTime end = exitTime != null ? exitTime : LocalDateTime.now();
        return Duration.between(entryTime, end);
    }

    public String getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public TicketStatus getStatus() { return status; }
}

// Payment strategy
public interface PaymentStrategy {
    void pay(double amount);
}

// Pricing strategy
public interface PricingStrategy {
    double calculateFee(ParkingTicket ticket);
}

public class HourlyPricing implements PricingStrategy {
    private final double ratePerHour;
    public HourlyPricing(double rate) { this.ratePerHour = rate; }
    public double calculateFee(ParkingTicket ticket) {
        long hours = ticket.getDuration().toHours() + 1; // round up
        return hours * ratePerHour;
    }
}

// Floor
public class ParkingFloor {
    private final int floorNumber;
    private final List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber, List<ParkingSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>(spots);
    }

    public Optional<ParkingSpot> findAvailableSpot(Vehicle vehicle) {
        return spots.stream()
            .filter(s -> s.canFit(vehicle))
            .findFirst();
    }

    public long countAvailable(SpotType type) {
        return spots.stream()
            .filter(s -> s.getType() == type && s.isAvailable())
            .count();
    }
}

// Main Parking Lot — facade
public class ParkingLot {
    private final String name;
    private final List<ParkingFloor> floors;
    private final Map<String, ParkingTicket> activeTickets = new HashMap<>();
    private final PricingStrategy pricingStrategy;
    private int ticketCounter = 0;

    public ParkingLot(String name, List<ParkingFloor> floors, PricingStrategy pricing) {
        this.name = name;
        this.floors = floors;
        this.pricingStrategy = pricing;
    }

    public Optional<ParkingTicket> checkIn(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            Optional<ParkingSpot> spot = floor.findAvailableSpot(vehicle);
            if (spot.isPresent()) {
                spot.get().park(vehicle);
                String ticketId = "TKT-" + (++ticketCounter);
                ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, spot.get());
                activeTickets.put(ticketId, ticket);
                return Optional.of(ticket);
            }
        }
        return Optional.empty(); // full
    }

    public double checkOut(String ticketId, PaymentStrategy paymentStrategy) {
        ParkingTicket ticket = activeTickets.get(ticketId);
        if (ticket == null) throw new IllegalArgumentException("Invalid ticket");

        double fee = pricingStrategy.calculateFee(ticket);
        paymentStrategy.pay(fee);
        ticket.markPaid();
        ticket.getSpot().leave();
        activeTickets.remove(ticketId);
        return fee;
    }
}
```

**Class relationships:**
- `ParkingLot` HAS-A list of `ParkingFloor` objects (composition)
- `ParkingFloor` HAS-A list of `ParkingSpot` objects (composition)
- `ParkingSpot` HAS-A `Vehicle` when occupied (association)
- `ParkingTicket` HAS-A `Vehicle` and a `ParkingSpot` (association)
- `Car`, `Motorcycle`, `Bus` extend `Vehicle` (inheritance)
- `ParkingLot` uses `PricingStrategy` and `PaymentStrategy` (Strategy pattern)

**Key Points:**
- Model entities as classes; behavior as methods; enums for fixed categories
- Use Strategy for variable behavior (pricing, payment)
- Use Optional for "might not find" return values
- ParkingLot acts as a Facade hiding complexity from the client

---

## Q22: How would you design a library system? (classes and relationships)

**Answer:**
A library system manages books, members, borrowing, returns, reservations, and fines. Here is a thorough OOP design.

**Core entities and design:**

```java
// Book and catalog
public class Book {
    private final String isbn;
    private final String title;
    private final String author;
    private final String genre;
    private int totalCopies;
    private int availableCopies;

    public Book(String isbn, String title, String author, String genre, int copies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.totalCopies = copies;
        this.availableCopies = copies;
    }

    public boolean isAvailable() { return availableCopies > 0; }

    public void checkout() {
        if (!isAvailable()) throw new IllegalStateException("No copies available");
        availableCopies--;
    }

    public void returnCopy() {
        if (availableCopies >= totalCopies) throw new IllegalStateException("All copies already in");
        availableCopies++;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
}

// Member (patron)
public enum MembershipType { STUDENT, FACULTY, PUBLIC }

public class Member {
    private final String memberId;
    private final String name;
    private final String email;
    private final MembershipType membershipType;
    private final List<BorrowRecord> borrowHistory = new ArrayList<>();
    private double fineBalance = 0.0;

    public Member(String memberId, String name, String email, MembershipType type) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.membershipType = type;
    }

    public int getMaxBorrowLimit() {
        return switch (membershipType) {
            case STUDENT -> 3;
            case FACULTY -> 10;
            case PUBLIC -> 5;
        };
    }

    public boolean canBorrow() {
        long active = borrowHistory.stream()
            .filter(r -> r.getStatus() == BorrowStatus.ACTIVE)
            .count();
        return active < getMaxBorrowLimit() && fineBalance < 10.0;
    }

    public void addFine(double amount) { fineBalance += amount; }
    public void payFine(double amount) { fineBalance = Math.max(0, fineBalance - amount); }

    public String getMemberId() { return memberId; }
    public double getFineBalance() { return fineBalance; }
    public List<BorrowRecord> getBorrowHistory() { return Collections.unmodifiableList(borrowHistory); }
    void addBorrowRecord(BorrowRecord record) { borrowHistory.add(record); }
}

// Borrow record
public enum BorrowStatus { ACTIVE, RETURNED, OVERDUE }

public class BorrowRecord {
    private final String recordId;
    private final Member member;
    private final Book book;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;

    public BorrowRecord(String id, Member member, Book book, int loanDays) {
        this.recordId = id;
        this.member = member;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(loanDays);
        this.status = BorrowStatus.ACTIVE;
    }

    public void returnBook() {
        this.returnDate = LocalDate.now();
        this.status = BorrowStatus.RETURNED;
    }

    public boolean isOverdue() {
        return status == BorrowStatus.ACTIVE && LocalDate.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public BorrowStatus getStatus() { return status; }
    public Book getBook() { return book; }
    public Member getMember() { return member; }
}

// Reservation
public class Reservation {
    private final String reservationId;
    private final Member member;
    private final Book book;
    private final LocalDateTime reservedAt;
    private boolean fulfilled;

    public Reservation(String id, Member member, Book book) {
        this.reservationId = id;
        this.member = member;
        this.book = book;
        this.reservedAt = LocalDateTime.now();
        this.fulfilled = false;
    }

    public void fulfill() { this.fulfilled = true; }
    public boolean isFulfilled() { return fulfilled; }
}

// Fine calculation strategy
public interface FinePolicy {
    double calculateFine(BorrowRecord record);
}

public class StandardFinePolicy implements FinePolicy {
    private static final double DAILY_RATE = 0.50;
    public double calculateFine(BorrowRecord record) {
        return record.getDaysOverdue() * DAILY_RATE;
    }
}

// Catalog (search)
public class Catalog {
    private final Map<String, Book> booksByIsbn = new HashMap<>();

    public void addBook(Book book) { booksByIsbn.put(book.getIsbn(), book); }

    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(booksByIsbn.get(isbn));
    }

    public List<Book> searchByTitle(String keyword) {
        return booksByIsbn.values().stream()
            .filter(b -> b.getTitle().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }

    public List<Book> findAvailable() {
        return booksByIsbn.values().stream()
            .filter(Book::isAvailable)
            .collect(Collectors.toList());
    }
}

// Library — main facade / service
public class Library {
    private final Catalog catalog;
    private final Map<String, Member> members = new HashMap<>();
    private final List<BorrowRecord> allRecords = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final FinePolicy finePolicy;
    private int recordCounter = 0;

    public Library(Catalog catalog, FinePolicy finePolicy) {
        this.catalog = catalog;
        this.finePolicy = finePolicy;
    }

    public BorrowRecord borrowBook(String memberId, String isbn) {
        Member member = members.get(memberId);
        Book book = catalog.findByIsbn(isbn)
            .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        if (!member.canBorrow()) throw new IllegalStateException("Member cannot borrow");
        if (!book.isAvailable()) throw new IllegalStateException("Book not available");

        book.checkout();
        int loanDays = member.getMemberType() == MembershipType.FACULTY ? 30 : 14;
        BorrowRecord record = new BorrowRecord("REC-" + (++recordCounter), member, book, loanDays);
        member.addBorrowRecord(record);
        allRecords.add(record);
        return record;
    }

    public void returnBook(BorrowRecord record) {
        record.returnBook();
        record.getBook().returnCopy();

        if (record.isOverdue()) {
            double fine = finePolicy.calculateFine(record);
            record.getMember().addFine(fine);
        }

        // Check if there's a reservation for this book
        reservations.stream()
            .filter(r -> r.getBook().equals(record.getBook()) && !r.isFulfilled())
            .findFirst()
            .ifPresent(r -> notifyMember(r.getMember(), record.getBook()));
    }

    private void notifyMember(Member member, Book book) {
        System.out.println("Notifying " + member.getMemberId() +
            ": Book '" + book.getTitle() + "' is now available");
    }

    public void registerMember(Member member) {
        members.put(member.getMemberId(), member);
    }
}
```

**Class relationships:**
- `Library` HAS-A `Catalog`, list of `BorrowRecord`, list of `Reservation`, and `FinePolicy` (composition/aggregation)
- `Catalog` HAS-A collection of `Book` (aggregation)
- `BorrowRecord` references `Member` and `Book` (association)
- `Reservation` references `Member` and `Book` (association)
- `FinePolicy` is a Strategy interface

**Key Points:**
- Use Strategy pattern for fine calculation and borrow limits to allow configuration without code changes
- Catalog provides the search/query interface, separating it from transaction concerns
- Library acts as a Facade/Service coordinating multiple domain objects
- Immutable domain objects (Book, Member) with controlled mutation methods enforce invariants

---

## Q23: What is the difference between an interface and an abstract class for design?

**Answer:**
Both interfaces and abstract classes allow you to define a contract that subclasses/implementors must fulfill, but they serve different design purposes and have different technical capabilities.

**Abstract class:**
- Can have instance variables (state)
- Can have constructors
- Can have concrete methods (with implementation)
- Can have `protected` members
- A class can extend only ONE abstract class (single inheritance)
- Represents an "IS-A + partial implementation" relationship
- Use when you want to share code among closely related classes

```java
public abstract class Template {
    // Shared state
    private String name;

    // Constructor
    public Template(String name) { this.name = name; }

    // Concrete method — shared implementation
    public void start() {
        System.out.println("Starting " + name);
        doWork(); // abstract — subclasses provide specific behavior
        System.out.println("Finished " + name);
    }

    // Abstract method — subclasses must implement
    protected abstract void doWork();
}
```

**Interface:**
- Cannot have instance state (only `static final` constants)
- No constructors
- All methods are implicitly public (before Java 8, all abstract)
- Java 8+: can have `default` and `static` methods
- Java 9+: can have `private` methods (helper for default methods)
- A class can implement MULTIPLE interfaces
- Represents a "CAN-DO" capability contract
- Use when you want to define a capability that diverse, unrelated classes can have

```java
public interface Printable {
    void print(); // capability

    default void printTwice() { print(); print(); } // shared utility
}

public interface Serializable { ... }

// A class can implement multiple interfaces
public class Report implements Printable, Serializable {
    public void print() { System.out.println("Printing report"); }
}
```

**Design guidance: when to use which:**

| Situation | Use |
|-----------|-----|
| Closely related classes sharing state and behavior | Abstract class |
| Unrelated classes with a common capability | Interface |
| You want multiple "type" inheritance | Interface |
| Template Method pattern (skeleton algorithm) | Abstract class |
| DIP: depend on abstractions | Prefer interface |
| Evolving API where you can add `default` methods | Interface (Java 8+) |

**The "abstract class as type" vs "interface as role" distinction:** An `Animal` abstract class captures what something IS. A `Flyable` interface captures what something CAN DO. A `Duck` can extend `Animal` and implement `Flyable`.

**Key Points:**
- Abstract class: IS-A, shared state and behavior, single inheritance limitation
- Interface: CAN-DO capability, no state, multiple implementation allowed
- Prefer interfaces for polymorphism (DIP); use abstract classes for template/reuse patterns
- Java 8 `default` methods blur the line but interfaces still cannot hold per-instance state

---

## Q24: Give an example of a SOLID violation and how to fix it.

**Answer:**
Let us take a realistic, multi-violation example and fix it systematically.

**Violating class — an `InvoiceProcessor` that violates SRP, OCP, and DIP:**

```java
// Violations: SRP, OCP, DIP
public class InvoiceProcessor {
    public void processInvoice(Invoice invoice) {
        // SRP violation: calculating AND saving AND notifying in one class

        // Business logic
        double total = invoice.getSubtotal() * 1.1; // 10% tax hardcoded

        // OCP violation: adding a new payment type requires modifying this method
        if (invoice.getPaymentMethod().equals("CREDIT_CARD")) {
            System.out.println("Processing credit card payment of $" + total);
        } else if (invoice.getPaymentMethod().equals("PAYPAL")) {
            System.out.println("Processing PayPal payment of $" + total);
        }
        // Adding BITCOIN requires modifying this code — OCP violation

        // DIP violation: directly creating concrete infrastructure objects
        MySQLInvoiceRepository repo = new MySQLInvoiceRepository();
        repo.save(invoice);

        SmtpEmailService emailService = new SmtpEmailService();
        emailService.sendConfirmation(invoice.getCustomerEmail(), total);
    }
}
```

**What is wrong:**
1. **SRP**: `InvoiceProcessor` calculates tax, processes payment, persists data, and sends email — four reasons to change
2. **OCP**: Adding a new payment method requires modifying the `if-else` chain
3. **DIP**: Directly instantiates `MySQLInvoiceRepository` and `SmtpEmailService`

**SOLID-compliant refactoring:**

```java
// SRP: separate tax calculation
public class TaxCalculator {
    private static final double TAX_RATE = 0.10;
    public double calculate(double subtotal) { return subtotal * (1 + TAX_RATE); }
}

// OCP + ISP: payment processing via interface
public interface PaymentProcessor {
    void process(double amount);
}

public class CreditCardProcessor implements PaymentProcessor {
    public void process(double amount) {
        System.out.println("Processing credit card: $" + amount);
    }
}

public class PayPalProcessor implements PaymentProcessor {
    public void process(double amount) {
        System.out.println("Processing PayPal: $" + amount);
    }
}

// Adding Bitcoin requires only a new class — no existing code changes (OCP)
public class BitcoinProcessor implements PaymentProcessor {
    public void process(double amount) {
        System.out.println("Processing Bitcoin: $" + amount);
    }
}

// DIP: depend on abstractions
public interface InvoiceRepository {
    void save(Invoice invoice);
}

public interface NotificationService {
    void sendConfirmation(String email, double amount);
}

// SRP + DIP: InvoiceProcessor now only orchestrates
public class InvoiceProcessor {
    private final TaxCalculator taxCalculator;
    private final InvoiceRepository repository;
    private final NotificationService notificationService;

    // Constructor injection (DIP implemented)
    public InvoiceProcessor(TaxCalculator taxCalc,
                            InvoiceRepository repo,
                            NotificationService notif) {
        this.taxCalculator = taxCalc;
        this.repository = repo;
        this.notificationService = notif;
    }

    public void processInvoice(Invoice invoice, PaymentProcessor paymentProcessor) {
        double total = taxCalculator.calculate(invoice.getSubtotal());
        paymentProcessor.process(total);   // OCP: no if-else, works with any processor
        repository.save(invoice);          // DIP: talks to interface
        notificationService.sendConfirmation(invoice.getCustomerEmail(), total);
    }
}
```

**Key Points:**
- SOLID violations frequently cluster together — fixing one often fixes others
- OCP violations (if-else/switch on type) are often fixed by introducing a polymorphic Strategy
- DIP violations (direct instantiation) are fixed by injecting interfaces via constructor
- SRP violations are fixed by extracting responsibilities into separate, focused classes

---

## Q25: What is method hiding vs method overriding?

**Answer:**
Both method hiding and method overriding involve a subclass providing its own version of a method that also exists in the superclass, but they apply to fundamentally different types of methods and have very different runtime behavior.

**Method Overriding (instance methods):**
When a subclass provides its own implementation of an instance method declared in the superclass, the method is overridden. The runtime type of the object determines which implementation is called — this is dynamic dispatch (runtime polymorphism).

```java
public class Animal {
    public String speak() { return "..."; }
}

public class Dog extends Animal {
    @Override
    public String speak() { return "Woof"; }
}

Animal a = new Dog();
System.out.println(a.speak()); // "Woof" — runtime type is Dog
```

Even though the reference type is `Animal`, the JVM calls `Dog.speak()` because of dynamic dispatch. The `@Override` annotation verifies this is truly an override.

**Method Hiding (static methods):**
When a subclass defines a `static` method with the same signature as a `static` method in the superclass, the subclass method HIDES the superclass method. Static methods are NOT polymorphic — the method called is determined by the declared (reference) type at compile time, not the runtime type.

```java
public class Parent {
    public static String greet() { return "Hello from Parent"; }
    public String greetInstance() { return "Instance: Hello from Parent"; }
}

public class Child extends Parent {
    public static String greet() { return "Hello from Child"; } // hides Parent.greet()
    @Override
    public String greetInstance() { return "Instance: Hello from Child"; }
}

// Method hiding — resolved at compile time based on reference type
Parent p = new Child();
System.out.println(p.greet());         // "Hello from Parent" — reference type is Parent!
System.out.println(Child.greet());     // "Hello from Child"

// Method overriding — resolved at runtime based on actual object type
System.out.println(p.greetInstance()); // "Instance: Hello from Child" — runtime type is Child!
```

**Key differences:**

| Aspect | Overriding | Hiding |
|--------|-----------|--------|
| Applies to | Instance methods | Static methods |
| Dispatch | Runtime (dynamic) | Compile-time (static) |
| Reference determines | No — actual object does | Yes — reference type does |
| `@Override` applies | Yes | No |
| Polymorphism | Yes | No |

**Why does this matter?** Static method hiding can cause subtle bugs. If you call a "static" method through a polymorphic reference, you may not get the behavior you expect. This is why calling static methods through instances (rather than class names) is bad practice.

**Key Points:**
- Overriding = instance methods, dynamic dispatch, runtime polymorphism
- Hiding = static methods, compile-time dispatch, reference type determines which method
- `@Override` cannot be used on static methods
- Calling static methods via an instance reference is misleading and should be avoided

---

## Q26: What is a covariant return type?

**Answer:**
A covariant return type is a feature introduced in Java 5 (JDK 1.5) that allows an overriding method in a subclass to return a type that is a subtype of the return type declared in the superclass method. The return type "co-varies" (varies together) with the class type.

Before Java 5, an overriding method had to return exactly the same type as the superclass method. Since Java 5, it can return a narrower (more specific) type.

**Example:**

```java
public class Animal {
    public Animal create() {
        return new Animal();
    }

    public Animal clone() {
        return new Animal();
    }
}

public class Dog extends Animal {
    // Covariant return type: Dog is a subtype of Animal
    @Override
    public Dog create() {  // return type is Dog, not Animal
        return new Dog();
    }

    @Override
    public Dog clone() {   // covariant return type
        return new Dog();
    }
}
```

This is valid because `Dog` IS-A `Animal`. Any code that expects `Animal` from `create()` will still work, because a `Dog` is always an `Animal`. But callers who know they are working with a `Dog` can avoid a cast:

```java
// Without covariant return types
Animal a = new Dog();
Dog d = (Dog) a.create(); // requires explicit cast

// With covariant return types — no cast needed when using Dog reference
Dog dog = new Dog();
Dog newDog = dog.create(); // returns Dog directly — no cast!
```

**Why it's useful:**
1. **Avoids unnecessary casting** in client code
2. **Builder pattern**: Fluent builder subclasses return `this` typed as the concrete builder type
3. **clone() pattern**: `Object.clone()` returns `Object`, but overriding classes can return their own type

**Builder pattern use case:**

```java
public class AnimalBuilder {
    protected String name;
    public AnimalBuilder name(String name) { this.name = name; return this; }
    public Animal build() { return new Animal(name); }
}

public class DogBuilder extends AnimalBuilder {
    private String breed;
    
    // Covariant — returns DogBuilder, not AnimalBuilder
    @Override
    public DogBuilder name(String name) { this.name = name; return this; }
    
    public DogBuilder breed(String breed) { this.breed = breed; return this; }
    
    @Override
    public Dog build() { return new Dog(name, breed); } // covariant return
}

// No casts needed in the chain
Dog dog = new DogBuilder()
    .name("Rex")    // returns DogBuilder
    .breed("Lab")   // chain continues without cast
    .build();       // returns Dog
```

**Key Points:**
- Covariant return types allow overriding methods to return a subtype of the declared return type
- Introduced in Java 5; improves type safety and reduces casting
- The bridge method mechanism (compiler-generated) maintains binary compatibility
- Especially useful in clone(), factory methods, and fluent builder chains

---

## Q27: What is the Template Method pattern?

**Answer:**
The Template Method pattern is a behavioral design pattern that defines the skeleton of an algorithm in a base class, deferring some steps to subclasses. The base class defines the invariant parts of the algorithm (the "template") and declares abstract or hook methods for the variable parts that subclasses must or may override.

The pattern preserves the overall structure of the algorithm while allowing subclasses to customize specific steps. It is a classic use case for abstract classes.

**Structure:**
- An abstract base class defines a `templateMethod()` which calls other methods in sequence
- Some of those methods are abstract (subclasses MUST override them)
- Some are "hooks" — concrete methods with a default (often empty) implementation that subclasses MAY override

**Example — a data processing pipeline:**

```java
public abstract class DataProcessor {

    // THE TEMPLATE METHOD — defines the skeleton
    public final void process(String dataSource) {
        String data = readData(dataSource);      // step 1
        String processed = processData(data);    // step 2 — abstract
        if (shouldValidate()) {                  // hook
            validateData(processed);             // step 3 — hook
        }
        saveResult(processed);                   // step 4
        onComplete();                            // hook — optional notification
    }

    // Concrete step — shared implementation
    private String readData(String source) {
        System.out.println("Reading data from: " + source);
        return "raw data";
    }

    // Abstract step — subclasses MUST provide
    protected abstract String processData(String data);

    // Hook — subclasses MAY override; returns true by default
    protected boolean shouldValidate() { return true; }

    // Hook — subclasses MAY override; default does nothing
    protected void validateData(String data) {
        System.out.println("Validating: " + data);
    }

    // Concrete step — shared
    private void saveResult(String data) {
        System.out.println("Saving result: " + data);
    }

    // Hook — optional post-processing notification
    protected void onComplete() {}
}

// Concrete implementation 1
public class CSVDataProcessor extends DataProcessor {
    protected String processData(String data) {
        return "CSV_PROCESSED[" + data + "]";
    }

    @Override
    protected void onComplete() {
        System.out.println("CSV processing finished, sending alert");
    }
}

// Concrete implementation 2
public class JSONDataProcessor extends DataProcessor {
    protected String processData(String data) {
        return "JSON_PROCESSED{" + data + "}";
    }

    @Override
    protected boolean shouldValidate() {
        return false; // JSON processor skips validation
    }
}

// Usage
DataProcessor csvProcessor = new CSVDataProcessor();
csvProcessor.process("data.csv");

DataProcessor jsonProcessor = new JSONDataProcessor();
jsonProcessor.process("data.json");
```

**Template Method in the Java standard library:**
- `java.util.AbstractList` — defines the template for list behavior; subclasses implement `get(int)` and optionally `set(int, E)` and `add(int, E)`
- `java.io.InputStream` — defines `read(byte[], int, int)` in terms of `read()`
- `javax.servlet.HttpServlet` — `service()` calls `doGet()`, `doPost()`, etc. which subclasses override
- JUnit's test execution lifecycle follows this pattern

**Template Method vs. Strategy:**
Both allow varying parts of an algorithm, but:
- **Template Method** uses inheritance; the varying parts are in subclasses
- **Strategy** uses composition; the varying parts are in separate strategy objects
Strategy is more flexible (can change at runtime), while Template Method is simpler for fixed hierarchies.

**Key Points:**
- Template Method: skeleton in base class, specific steps in subclasses (via inheritance)
- `final` on the template method prevents subclasses from reordering the skeleton
- Hooks (non-abstract methods with defaults) give subclasses optional extension points
- The Hollywood Principle: "Don't call us, we'll call you" — the base class calls the subclass's methods

---

## Q28: What is the Chain of Responsibility pattern?

**Answer:**
The Chain of Responsibility (CoR) pattern is a behavioral design pattern that passes a request along a chain of handlers. Each handler decides either to process the request or to pass it to the next handler in the chain. This decouples the sender of a request from the receivers and allows multiple handlers to process a request without the sender knowing which handler will ultimately handle it.

**Structure:**
- A `Handler` interface with a method to handle requests and a reference to the next handler
- Concrete handlers implementing specific processing logic
- A chain is built by linking handlers; requests are passed down the chain

**Example — a support ticket escalation system:**

```java
public abstract class SupportHandler {
    private SupportHandler nextHandler;

    // Returns 'this' to allow method chaining when building the chain
    public SupportHandler setNext(SupportHandler handler) {
        this.nextHandler = handler;
        return handler;
    }

    public void handle(SupportTicket ticket) {
        if (canHandle(ticket)) {
            process(ticket);
        } else if (nextHandler != null) {
            nextHandler.handle(ticket);
        } else {
            System.out.println("No handler found for ticket: " + ticket.getId());
        }
    }

    protected abstract boolean canHandle(SupportTicket ticket);
    protected abstract void process(SupportTicket ticket);
}

public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

public class SupportTicket {
    private final String id;
    private final Priority priority;
    private final String description;
    public SupportTicket(String id, Priority priority, String description) {
        this.id = id; this.priority = priority; this.description = description;
    }
    public String getId() { return id; }
    public Priority getPriority() { return priority; }
}

// Level 1: handles LOW priority
public class JuniorSupportHandler extends SupportHandler {
    protected boolean canHandle(SupportTicket ticket) {
        return ticket.getPriority() == Priority.LOW;
    }
    protected void process(SupportTicket ticket) {
        System.out.println("Junior support handling ticket " + ticket.getId());
    }
}

// Level 2: handles MEDIUM priority
public class SeniorSupportHandler extends SupportHandler {
    protected boolean canHandle(SupportTicket ticket) {
        return ticket.getPriority() == Priority.MEDIUM;
    }
    protected void process(SupportTicket ticket) {
        System.out.println("Senior support handling ticket " + ticket.getId());
    }
}

// Level 3: handles HIGH priority
public class ManagerHandler extends SupportHandler {
    protected boolean canHandle(SupportTicket ticket) {
        return ticket.getPriority() == Priority.HIGH;
    }
    protected void process(SupportTicket ticket) {
        System.out.println("Manager handling ticket " + ticket.getId());
    }
}

// Level 4: handles CRITICAL
public class DirectorHandler extends SupportHandler {
    protected boolean canHandle(SupportTicket ticket) {
        return ticket.getPriority() == Priority.CRITICAL;
    }
    protected void process(SupportTicket ticket) {
        System.out.println("Director handling ticket " + ticket.getId());
    }
}

// Building and using the chain
SupportHandler junior = new JuniorSupportHandler();
SupportHandler senior = new SeniorSupportHandler();
SupportHandler manager = new ManagerHandler();
SupportHandler director = new DirectorHandler();

// Chain them: junior -> senior -> manager -> director
junior.setNext(senior).setNext(manager).setNext(director);

junior.handle(new SupportTicket("T001", Priority.LOW, "Password reset"));
// Junior support handling ticket T001

junior.handle(new SupportTicket("T002", Priority.CRITICAL, "System down"));
// Director handling ticket T002

junior.handle(new SupportTicket("T003", Priority.HIGH, "Data corruption"));
// Manager handling ticket T003
```

**CoR in the Java standard library:**
- Java's exception handling: a `try-catch` block is a linear chain; if a catch clause doesn't match the exception type, it passes to the next one
- Java Servlet `FilterChain`: each filter in the chain processes the request/response and calls `chain.doFilter()` to pass to the next filter
- Java's logging framework: `Logger` passes log records up through parent loggers
- Spring Security's `FilterSecurityInterceptor`

**Key Points:**
- CoR decouples request senders from receivers; neither knows about the other
- Each handler decides to handle or pass; the chain is built dynamically
- Can short-circuit (stop at the first handler) or pass to all (event notification)
- Used in middleware pipelines, event bubbling, and permission checking chains

---

## Q29: When would you break "prefer composition over inheritance"?

**Answer:**
"Prefer composition over inheritance" is excellent default advice, but like all design principles, it is a guideline, not an absolute law. There are legitimate scenarios where inheritance is the right choice.

**When to use inheritance (when the IS-A relationship is genuine and stable):**

**1. When Liskov Substitution holds perfectly and the hierarchy is semantically correct:**
```java
// This IS-A is real and behavioral — an ArrayList IS-A AbstractList
// No method violates the AbstractList contract
public class ArrayList<E> extends AbstractList<E> { ... }
```
Here inheritance is correct. An `ArrayList` is genuinely a list. Every operation inherited from `AbstractList` makes semantic sense for `ArrayList`.

**2. The Template Method pattern requires inheritance:**
When you need to define the skeleton of an algorithm in a base class and have subclasses fill in specific steps, inheritance is the natural and correct choice. Composition would require passing callbacks for every step, which is verbose for fixed hierarchies.
```java
public abstract class ReportGenerator {
    public final void generate() {
        fetchData();
        formatData();     // abstract — subclass-specific
        renderReport();
        cleanup();
    }
    protected abstract void formatData();
    // ...
}
```

**3. When you are working with a framework that requires it:**
JUnit test classes, Spring MVC controllers in certain configurations, JavaFX controls — many frameworks are built around inheritance. In these contexts, extending the framework class is idiomatic and correct.

**4. When you want to leverage a proven, stable base class:**
`AbstractList`, `AbstractMap`, `AbstractQueue` in the JDK provide a large number of useful concrete methods (iterator, subList, equals, hashCode) by implementing them in terms of a few abstract primitives. Inheriting these is much better than re-implementing them.

**5. Type hierarchy for polymorphism in a controlled context:**
When you have a tight, well-controlled hierarchy (`Shape` → `Circle`, `Rectangle`, `Triangle`) that will not change often, and you want polymorphism, inheritance is clean and direct.

**When composition is almost always better:**
- When you want to reuse code from a class but the IS-A relationship is not semantically true (`Stack extends Vector`)
- When you need behavior from multiple sources (Java's single inheritance would force composition anyway)
- When the base class is external and its implementation could change
- When you want to swap implementations at runtime
- When the relationship is more "uses" than "is"

**The pragmatic answer for interviews:** Prefer composition as your default. Use inheritance when: (a) the IS-A relationship is genuinely semantic and satisfies LSP, (b) you are implementing the Template Method pattern, (c) a framework requires it, or (d) you want the reuse benefits of a proven abstract base class in a controlled hierarchy.

**Key Points:**
- Composition is the default; inheritance is used when the IS-A relationship is semantically correct and stable
- Template Method pattern is a specific, legitimate use case for inheritance
- Framework extension (JUnit, Spring, JavaFX) often requires inheritance idiomatically
- Abstract base classes (AbstractList, AbstractMap) provide genuine reuse that justifies inheritance

---

## Q30: How does Java achieve multiple inheritance behavior through interfaces?

**Answer:**
Java deliberately does not support multiple class inheritance (a class cannot extend two classes). This was a design decision to avoid the "Diamond Problem," where a class inherits from two classes that both define the same method — it is ambiguous which version to use.

However, Java allows a class to implement multiple interfaces, and since Java 8, interfaces can have `default` methods (concrete implementations). This provides most of the benefits of multiple inheritance with clear, deterministic rules for resolving conflicts.

**Basic multiple interface implementation:**

```java
public interface Flyable {
    void fly();
    default String describe() { return "I can fly"; }
}

public interface Swimmable {
    void swim();
    default String describe() { return "I can swim"; }
}

// Duck implements two interfaces — multiple behavior inheritance
public class Duck implements Flyable, Swimmable {
    public void fly() { System.out.println("Duck flying"); }
    public void swim() { System.out.println("Duck swimming"); }

    // MUST override when two interfaces have the same default method
    @Override
    public String describe() {
        return Flyable.super.describe() + " and " + Swimmable.super.describe();
    }
}
```

**The Diamond Problem and Java's resolution rules:**

When two interfaces provide the same `default` method and a class implements both, Java forces the implementing class to override the method. This is compile-time checked and unambiguous.

```java
interface A {
    default void hello() { System.out.println("A"); }
}

interface B extends A {
    default void hello() { System.out.println("B"); }
}

// Rule 1: A class method always wins over interface defaults
// Rule 2: More specific interface wins (B is more specific than A)
// Rule 3: If conflict remains (A and C unrelated), you must override

class C implements A, B {
    // No conflict — B.hello() is more specific (B extends A)
    // Java automatically picks B.hello()
}

interface D {
    default void hello() { System.out.println("D"); }
}

class E implements A, D {
    // A and D are unrelated — MUST override explicitly
    @Override
    public void hello() {
        A.super.hello(); // can explicitly call a specific interface's default
        D.super.hello();
    }
}
```

**Java's three priority rules for default method resolution:**
1. **Class methods win**: If the class or a superclass has a concrete implementation, it wins over any interface default
2. **More specific interface wins**: If one interface extends the other, the more specific (child) interface's default wins
3. **Must override**: If two unrelated interfaces provide conflicting defaults, the class must override the method (compile error if it doesn't)

**Composing behavior via multiple interfaces:**

```java
public interface Loggable {
    default void log(String message) {
        System.out.println("[LOG] " + getClass().getSimpleName() + ": " + message);
    }
}

public interface Auditable {
    default void audit(String action) {
        System.out.println("[AUDIT] " + action + " by " + getClass().getSimpleName());
    }
}

public interface Cacheable {
    default void invalidateCache() {
        System.out.println("Cache invalidated for " + getClass().getSimpleName());
    }
}

// Service gains all three behaviors through interface composition
public class UserService implements Loggable, Auditable, Cacheable {
    public void createUser(String username) {
        log("Creating user: " + username);
        audit("CREATE_USER");
        invalidateCache();
        // actual creation logic
    }
}
```

**Key differences from true multiple inheritance:**
- Interfaces cannot hold instance state (no instance fields), only `static final` constants
- Constructors cannot be inherited from interfaces
- The composition of behavior without state avoids most multiple inheritance problems
- You can use composition with delegation to simulate full multiple inheritance of state if needed

**Key Points:**
- Java avoids the Diamond Problem by allowing only single class inheritance
- Multiple interface implementation provides multiple behavioral inheritance
- Java 8 `default` methods allow interfaces to carry concrete implementations
- Conflict resolution: class > specific interface > explicit override required
- Interfaces cannot hold instance state, which is why true state inheritance requires composition

---
