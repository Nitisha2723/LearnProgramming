# Creational Design Patterns

> "The creational patterns are concerned with the process of object creation. They help make a system independent of how its objects are created, composed, and represented."  
> — Gang of Four, *Design Patterns*

Creational patterns solve a fundamental problem: *how do you create objects in a way that is flexible, controlled, and decoupled from the details of the concrete types being created?*

The five classical creational patterns cover a spectrum of complexity: from ensuring a single instance (Singleton) to constructing complex objects step by step (Builder). Each pattern answers a specific question about object creation.

---

## Table of Contents

1. [Singleton — Only One Instance](#singleton--only-one-instance)
2. [Factory Method — Let Subclasses Decide](#factory-method--let-subclasses-decide)
3. [Abstract Factory — Families of Objects](#abstract-factory--families-of-objects)
4. [Builder — Step-by-Step Construction](#builder--step-by-step-construction)
5. [Prototype — Copy Instead of Create](#prototype--copy-instead-of-create)
6. [Pattern Comparison Summary](#pattern-comparison-summary)

---

## Singleton — Only One Instance

**Intent:** Ensure a class has only one instance, and provide a global point of access to it.

### When to Use

Singleton makes sense when:
- A single shared resource must be coordinated (e.g., a connection pool, a thread pool, a device driver).
- A shared configuration object must be consistent across the application.
- A logger must serialize writes from multiple threads to a single destination.

### Implementation: The Naive Approach (Broken in Multithreading)

```java
// BAD: Not thread-safe
public class Logger {
    private static Logger instance;

    private Logger() {}

    public static Logger getInstance() {
        if (instance == null) {                  // Thread A and Thread B both pass here
            instance = new Logger();             // Both create separate instances!
        }
        return instance;
    }

    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}
```

In a multi-threaded environment, two threads can both see `instance == null` simultaneously and each create a new instance. Now you have two loggers, and your thread-safety guarantee is broken.

### Implementation: Thread-Safe with Double-Checked Locking

```java
// BETTER: Thread-safe with double-checked locking
public class Logger {
    // volatile ensures the instance reference is visible across all threads immediately
    private static volatile Logger instance;

    private Logger() {}

    public static Logger getInstance() {
        if (instance == null) {                 // First check: no locking overhead once initialized
            synchronized (Logger.class) {
                if (instance == null) {         // Second check: prevents duplicate creation
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    public void log(String message) {
        // Synchronized to prevent interleaved log lines from multiple threads
        synchronized (this) {
            System.out.println("[" + Thread.currentThread().getName() + "] " + message);
        }
    }
}
```

The `volatile` keyword is crucial here. Without it, the JVM may reorder the operations inside `new Logger()` such that another thread sees a partially-constructed object. `volatile` disables this optimization for this particular variable.

### Implementation: The Enum Singleton (Best Approach)

Joshua Bloch, in *Effective Java*, recommends using an enum for Singletons. It is concise, thread-safe by the JVM spec, and handles serialization correctly:

```java
// BEST: Enum singleton — thread-safe, serialization-safe, concise
public enum ApplicationConfig {
    INSTANCE;

    private final Properties properties = new Properties();
    private final String environment;

    // Enum constructors run once when the enum is first loaded
    ApplicationConfig() {
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        this.environment = properties.getProperty("app.environment", "development");
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String getEnvironment() {
        return environment;
    }
}

// Usage
String dbUrl = ApplicationConfig.INSTANCE.get("database.url");
```

Why enum is superior:
- **Thread safety** is guaranteed by the JVM's class loading mechanism.
- **Serialization safety**: a normal Singleton class, when serialized and deserialized, creates a new instance. An enum prevents this automatically.
- **Reflection safety**: Java prevents reflection from creating additional enum instances.

### When NOT to Use Singleton

Singleton is frequently overused, and it has a dark side:

```java
// Problem: Code that uses a Singleton is hard to test
public class OrderProcessor {
    public void process(Order order) {
        // This class is coupled to a specific Logger implementation
        Logger.getInstance().log("Processing order: " + order.getId());
        // ...
    }
}

// You cannot inject a mock logger — the coupling is baked in
```

If a class uses a Singleton directly, you cannot substitute a mock for testing. The solution is to apply the Dependency Inversion Principle: depend on a `Logger` *interface* and inject it, even if the real implementation is a Singleton:

```java
// GOOD: Testable even with Singleton internals
public interface LogService {
    void log(String message);
}

public enum RealLogger implements LogService {
    INSTANCE;

    @Override
    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

public class OrderProcessor {
    private final LogService logger;

    // Inject the interface — can use the Singleton in prod, mock in tests
    public OrderProcessor(LogService logger) {
        this.logger = logger;
    }

    public void process(Order order) {
        logger.log("Processing order: " + order.getId());
    }
}

// In production code:
OrderProcessor processor = new OrderProcessor(RealLogger.INSTANCE);

// In tests:
LogService mockLogger = message -> capturedMessages.add(message);
OrderProcessor processor = new OrderProcessor(mockLogger);
```

---

## Factory Method — Let Subclasses Decide

**Intent:** Define an interface for creating an object, but let subclasses decide which class to instantiate. Factory Method lets a class defer instantiation to subclasses.

### The Problem It Solves

You have a framework that knows *when* and *why* to create objects, but not *which* concrete objects to create. The decision of what to create should belong to the user of the framework.

```
    Creator (abstract)
    ├── createDocument()  ← abstract factory method
    └── processDocument()  ← uses the created object

        ↓ subclasses implement

    PdfCreator          WordCreator         HtmlCreator
    ├── createDocument() ├── createDocument() ├── createDocument()
    │   return PdfDoc    │   return WordDoc   │   return HtmlDoc
```

### Implementation

```java
// The product interface
public interface Document {
    void open();
    void save(String path);
    void close();
    String getFormat();
}

// Concrete products
public class PdfDocument implements Document {
    private final String content;

    public PdfDocument(String content) {
        this.content = content;
    }

    @Override
    public void open() {
        System.out.println("Opening PDF viewer for: " + content.substring(0, 20));
    }

    @Override
    public void save(String path) {
        System.out.println("Saving PDF to: " + path);
        // PDF-specific serialization logic
    }

    @Override
    public void close() {
        System.out.println("Closing PDF document");
    }

    @Override
    public String getFormat() {
        return "application/pdf";
    }
}

public class WordDocument implements Document {
    private final String content;

    public WordDocument(String content) {
        this.content = content;
    }

    @Override
    public void open() {
        System.out.println("Opening Word processor for document");
    }

    @Override
    public void save(String path) {
        System.out.println("Saving DOCX to: " + path + ".docx");
    }

    @Override
    public void close() {
        System.out.println("Closing Word document");
    }

    @Override
    public String getFormat() {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }
}

// The creator — defines the factory method
public abstract class DocumentEditor {
    // The factory method — subclasses override this
    protected abstract Document createDocument(String content);

    // The template method — uses the factory method
    public void editAndSave(String content, String path) {
        Document doc = createDocument(content);  // calls the factory method
        doc.open();
        // ... editing logic
        doc.save(path);
        doc.close();
        System.out.println("Saved as: " + doc.getFormat());
    }
}

// Concrete creators
public class PdfEditor extends DocumentEditor {
    @Override
    protected Document createDocument(String content) {
        return new PdfDocument(content);
    }
}

public class WordEditor extends DocumentEditor {
    @Override
    protected Document createDocument(String content) {
        return new WordDocument(content);
    }
}

// Usage
DocumentEditor editor = new PdfEditor();
editor.editAndSave("Invoice #1234...", "/reports/invoice");

editor = new WordEditor();
editor.editAndSave("Meeting notes...", "/notes/meeting");
```

### Factory Method vs. Static Factory

Java also has a common pattern called *static factory methods* — these are simply static methods that return instances of a class:

```java
// Static factory methods — NOT the Factory Method pattern, but useful
public class Connection {
    private Connection() {}

    public static Connection forMySQL(String host, int port) {
        Connection c = new Connection();
        // configure for MySQL
        return c;
    }

    public static Connection forPostgres(String host, int port) {
        Connection c = new Connection();
        // configure for Postgres
        return c;
    }
}

// Benefits: descriptive names, can return subtypes, can cache instances
Connection mysql = Connection.forMySQL("localhost", 3306);
```

These are widely used in the Java standard library: `List.of()`, `Optional.of()`, `LocalDate.now()`, `Collections.unmodifiableList()`.

### When to Use Factory Method

- When a class can't anticipate which type of objects it must create.
- When you want subclasses to specify the objects they create.
- When you want to localize the knowledge of which class to instantiate.

---

## Abstract Factory — Families of Objects

**Intent:** Provide an interface for creating families of related or dependent objects without specifying their concrete classes.

### Factory Method vs. Abstract Factory

- **Factory Method**: one product, one factory method. Subclasses decide which product to create.
- **Abstract Factory**: *multiple related products*, one factory object. Swapping the factory switches the whole family.

```
    Factory Method:         Abstract Factory:
    DocumentEditor          UIFactory
      ├── createDocument()    ├── createButton()
      │                       ├── createTextField()
      (one product)           └── createCheckbox()
                              (a family of products)
```

### Implementation: UI Theme Factory

```java
// The product interfaces
public interface Button {
    void render();
    void onClick(Runnable handler);
}

public interface TextField {
    void render();
    String getValue();
    void setValue(String value);
}

public interface Checkbox {
    void render();
    boolean isChecked();
    void setChecked(boolean checked);
}

// Abstract Factory — the interface for creating UI families
public interface UIFactory {
    Button createButton(String label);
    TextField createTextField(String placeholder);
    Checkbox createCheckbox(String label);
}

// Concrete Factory 1: Light Theme
public class LightThemeFactory implements UIFactory {
    @Override
    public Button createButton(String label) {
        return new LightButton(label);
    }

    @Override
    public TextField createTextField(String placeholder) {
        return new LightTextField(placeholder);
    }

    @Override
    public Checkbox createCheckbox(String label) {
        return new LightCheckbox(label);
    }
}

// Concrete Factory 2: Dark Theme
public class DarkThemeFactory implements UIFactory {
    @Override
    public Button createButton(String label) {
        return new DarkButton(label);
    }

    @Override
    public TextField createTextField(String placeholder) {
        return new DarkTextField(placeholder);
    }

    @Override
    public Checkbox createCheckbox(String label) {
        return new DarkCheckbox(label);
    }
}

// Concrete products (abbreviated for clarity)
public class LightButton implements Button {
    private final String label;
    public LightButton(String label) { this.label = label; }
    @Override public void render() { System.out.println("[LIGHT BUTTON: " + label + "]"); }
    @Override public void onClick(Runnable handler) { handler.run(); }
}

public class DarkButton implements Button {
    private final String label;
    public DarkButton(String label) { this.label = label; }
    @Override public void render() { System.out.println("[dark button: " + label + "]"); }
    @Override public void onClick(Runnable handler) { handler.run(); }
}

// The application — depends only on the abstract factory
public class LoginForm {
    private final Button loginButton;
    private final TextField usernameField;
    private final TextField passwordField;
    private final Checkbox rememberMeCheckbox;

    public LoginForm(UIFactory factory) {
        this.loginButton = factory.createButton("Login");
        this.usernameField = factory.createTextField("Username");
        this.passwordField = factory.createTextField("Password");
        this.rememberMeCheckbox = factory.createCheckbox("Remember me");
    }

    public void render() {
        usernameField.render();
        passwordField.render();
        rememberMeCheckbox.render();
        loginButton.render();
    }
}

// Usage: swap the entire UI theme by swapping the factory
UIFactory theme = isDarkMode() ? new DarkThemeFactory() : new LightThemeFactory();
LoginForm loginForm = new LoginForm(theme);
loginForm.render();
```

The entire form renders consistently because all components come from the same factory — you can't accidentally mix a light button with dark text fields.

---

## Builder — Step-by-Step Construction

**Intent:** Separate the construction of a complex object from its representation so that the same construction process can create different representations.

### The Problem: Telescoping Constructors

When a class has many optional parameters, a common (bad) solution is to create many constructors:

```java
// TERRIBLE: Telescoping constructors
public class Pizza {
    public Pizza(String size) { ... }
    public Pizza(String size, String crust) { ... }
    public Pizza(String size, String crust, String sauce) { ... }
    public Pizza(String size, String crust, String sauce, boolean cheese) { ... }
    public Pizza(String size, String crust, String sauce, boolean cheese, List<String> toppings) { ... }
    // How many constructors until it becomes unreadable?
}

// Which parameters go where?
Pizza pizza = new Pizza("large", "thin", "tomato", true, List.of("pepperoni", "mushrooms"));
```

At the call site, you must count positional arguments and hope you have them in the right order. This is fragile and unreadable.

### The Fix: Builder Pattern

```java
public class Pizza {
    // All fields — some mandatory, some optional
    private final String size;        // mandatory
    private final String crustType;   // mandatory
    private final String sauce;       // optional, default "tomato"
    private final boolean cheese;     // optional, default true
    private final List<String> toppings;  // optional, default empty
    private final boolean glutenFree; // optional, default false
    private final String notes;       // optional, default null

    // Private constructor — only the Builder can call this
    private Pizza(Builder builder) {
        this.size = builder.size;
        this.crustType = builder.crustType;
        this.sauce = builder.sauce;
        this.cheese = builder.cheese;
        this.toppings = Collections.unmodifiableList(new ArrayList<>(builder.toppings));
        this.glutenFree = builder.glutenFree;
        this.notes = builder.notes;
    }

    // Getters
    public String getSize() { return size; }
    public String getCrustType() { return crustType; }
    public List<String> getToppings() { return toppings; }
    public boolean isGlutenFree() { return glutenFree; }

    @Override
    public String toString() {
        return String.format("Pizza{size='%s', crust='%s', sauce='%s', cheese=%b, toppings=%s, gf=%b}",
            size, crustType, sauce, cheese, toppings, glutenFree);
    }

    // The Builder — a static nested class
    public static class Builder {
        // Mandatory fields
        private final String size;
        private final String crustType;

        // Optional fields with defaults
        private String sauce = "tomato";
        private boolean cheese = true;
        private List<String> toppings = new ArrayList<>();
        private boolean glutenFree = false;
        private String notes = null;

        public Builder(String size, String crustType) {
            if (size == null || size.isBlank())
                throw new IllegalArgumentException("Size cannot be blank");
            if (crustType == null || crustType.isBlank())
                throw new IllegalArgumentException("Crust type cannot be blank");
            this.size = size;
            this.crustType = crustType;
        }

        // Fluent setters — each returns `this` to enable chaining
        public Builder sauce(String sauce) {
            this.sauce = sauce;
            return this;
        }

        public Builder withCheese() {
            this.cheese = true;
            return this;
        }

        public Builder withoutCheese() {
            this.cheese = false;
            return this;
        }

        public Builder addTopping(String topping) {
            this.toppings.add(topping);
            return this;
        }

        public Builder toppings(List<String> toppings) {
            this.toppings = new ArrayList<>(toppings);
            return this;
        }

        public Builder glutenFree() {
            this.glutenFree = true;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        // Validation and construction happen here
        public Pizza build() {
            if (glutenFree && crustType.equals("traditional")) {
                throw new IllegalStateException("Traditional crust cannot be gluten-free");
            }
            return new Pizza(this);
        }
    }
}

// Usage — beautifully readable, order doesn't matter for optional fields
Pizza simplePizza = new Pizza.Builder("medium", "thin")
    .sauce("pesto")
    .addTopping("mozzarella")
    .addTopping("basil")
    .build();

Pizza complexPizza = new Pizza.Builder("large", "stuffed")
    .withCheese()
    .addTopping("pepperoni")
    .addTopping("olives")
    .addTopping("jalapeños")
    .notes("Extra crispy please")
    .build();

Pizza veganPizza = new Pizza.Builder("small", "cauliflower")
    .withoutCheese()
    .sauce("olive oil")
    .glutenFree()
    .addTopping("roasted vegetables")
    .build();
```

### Builder for HTTP Requests

The Builder pattern appears throughout real APIs. `HttpRequest` in Java 11+ is a canonical example:

```java
// Java 11+ HttpRequest.Builder — same pattern
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/orders"))
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer " + token)
    .timeout(Duration.ofSeconds(30))
    .POST(HttpRequest.BodyPublishers.ofString(json))
    .build();

// Building a SQL query (fluent query builder style)
Query query = QueryBuilder.select("id", "name", "email")
    .from("users")
    .where("active = true")
    .and("created_at > ?", thirtyDaysAgo)
    .orderBy("name ASC")
    .limit(100)
    .offset(0)
    .build();
```

### Key Properties of a Well-Designed Builder

1. **Mandatory parameters in the constructor**: You cannot create a `Pizza` without a size and crust type. The builder's constructor enforces this.
2. **Optional parameters as fluent methods**: Each optional parameter has a dedicated method with a sensible default.
3. **Validation in `build()`**: Complex cross-parameter validation happens at construction time, not scattered across setters.
4. **Immutable result**: The built object should be immutable — all fields `final`, collections wrapped in `unmodifiableList`.

### Builder vs. Factory

| Concern | Factory | Builder |
|---------|---------|---------|
| **Focus** | Which type to create | How to configure a complex object |
| **Complexity** | Simple objects, one step | Complex objects, many optional parts |
| **Client knowledge** | Client doesn't know the concrete type | Client configures all the parts |
| **Use case** | `Shape.create("circle")` | `new Pizza.Builder("large", "thin").addTopping("cheese").build()` |

---

## Prototype — Copy Instead of Create

**Intent:** Specify the kinds of objects to create using a prototypical instance, and create new objects by copying this prototype.

### When to Use

- Creating a new object is expensive (database query, network call, complex initialization).
- You need many objects that are nearly identical — start from a "template" object and customize.
- The type of the object isn't known at compile time, but you have an instance you can copy.

### Java's `Cloneable` Interface

Java has built-in support for the Prototype pattern via `Cloneable` and `Object.clone()`:

```java
public class DatabaseQuery implements Cloneable {
    private String sql;
    private List<Object> parameters;
    private int timeout;
    private boolean cacheable;
    private Map<String, String> hints;

    public DatabaseQuery(String sql) {
        this.sql = sql;
        this.parameters = new ArrayList<>();
        this.hints = new HashMap<>();
        this.timeout = 30;
        this.cacheable = false;
    }

    // Setters return this for fluent style
    public DatabaseQuery withParameter(Object param) {
        this.parameters.add(param);
        return this;
    }

    public DatabaseQuery withTimeout(int seconds) {
        this.timeout = seconds;
        return this;
    }

    public DatabaseQuery cacheable() {
        this.cacheable = true;
        return this;
    }

    // clone() creates a new instance with the same state
    @Override
    public DatabaseQuery clone() {
        try {
            DatabaseQuery copy = (DatabaseQuery) super.clone();
            // Deep copy mutable fields!
            copy.parameters = new ArrayList<>(this.parameters);
            copy.hints = new HashMap<>(this.hints);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone failed — this should never happen", e);
        }
    }
}

// Usage: create a prototype and customize clones
DatabaseQuery baseQuery = new DatabaseQuery("SELECT * FROM users WHERE active = ?")
    .withParameter(true)
    .withTimeout(60)
    .cacheable();

// Clone and customize — cheap and fast
DatabaseQuery adminQuery = baseQuery.clone()
    .withParameter("admin");

DatabaseQuery recentQuery = baseQuery.clone()
    .withParameter(LocalDate.now().minusDays(30));
```

### Deep Copy vs. Shallow Copy

This is the most important concept in Prototype:

```
Shallow Copy:
┌─────────────────────┐       ┌─────────────────────┐
│ Original Object     │       │ Cloned Object        │
│ ┌─────────────────┐ │       │ ┌─────────────────┐  │
│ │ primitives: 42  │ │       │ │ primitives: 42  │  │
│ │ str: "hello"    │ │       │ │ str: "hello"    │  │
│ │ list ──────────────────────▶ list (same!)     │  │
│ └─────────────────┘ │       │ └─────────────────┘  │
└─────────────────────┘       └─────────────────────┘
                                 ↑ Both point to the same list!
                                   Mutating one affects the other!

Deep Copy:
┌─────────────────────┐       ┌─────────────────────┐
│ Original Object     │       │ Cloned Object        │
│ ┌─────────────────┐ │       │ ┌─────────────────┐  │
│ │ primitives: 42  │ │       │ │ primitives: 42  │  │
│ │ str: "hello"    │ │       │ │ str: "hello"    │  │
│ │ list ──────────────────────▶ list (new copy)  │  │
│ └─────────────────┘ │       │ └─────────────────┘  │
│   ↓                 │       │   ↓                  │
│ [1, 2, 3]           │       │ [1, 2, 3] (different) │
└─────────────────────┘       └─────────────────────┘
                                  Safe to mutate independently
```

```java
// Demonstration of the shallow copy trap
public class ShallowCopyTrap implements Cloneable {
    public List<String> items = new ArrayList<>(List.of("a", "b", "c"));

    @Override
    protected ShallowCopyTrap clone() throws CloneNotSupportedException {
        return (ShallowCopyTrap) super.clone(); // shallow clone!
    }
}

ShallowCopyTrap original = new ShallowCopyTrap();
ShallowCopyTrap copy = original.clone();

copy.items.add("d");

System.out.println(original.items); // [a, b, c, d] — original was modified!
System.out.println(copy.items);     // [a, b, c, d]
```

Always deep-copy mutable reference fields in `clone()`. Primitives and `String`s are inherently safe (Strings are immutable).

### Alternative: Copy Constructors

Many modern Java practitioners prefer *copy constructors* over `Cloneable`, which has a confusing contract:

```java
public class UserProfile {
    private final String username;
    private final String email;
    private final List<String> roles;
    private final Map<String, String> preferences;

    // Normal constructor
    public UserProfile(String username, String email) {
        this.username = username;
        this.email = email;
        this.roles = new ArrayList<>();
        this.preferences = new HashMap<>();
    }

    // Copy constructor — explicit and clear
    public UserProfile(UserProfile other) {
        this.username = other.username;
        this.email = other.email;
        this.roles = new ArrayList<>(other.roles);          // deep copy
        this.preferences = new HashMap<>(other.preferences); // deep copy
    }

    public UserProfile withEmail(String newEmail) {
        UserProfile copy = new UserProfile(this);
        // But how do we change the email on a copy? We need a mutable copy
        // This is where record pattern or builder clone becomes useful
        return copy;
    }
}

// Usage
UserProfile original = new UserProfile("alice", "alice@example.com");
UserProfile copy = new UserProfile(original); // clear, no casting, no exception
```

---

## Pattern Comparison Summary

```
Creational Patterns at a Glance:

┌────────────────────┬─────────────────────────────────────────────────────┐
│ Pattern            │ Use When                                            │
├────────────────────┼─────────────────────────────────────────────────────┤
│ Singleton          │ Exactly one shared instance needed globally         │
│ Factory Method     │ Subclasses should decide what type to create        │
│ Abstract Factory   │ Creating a family of related objects consistently   │
│ Builder            │ Object has many optional parameters or steps        │
│ Prototype          │ Creating new objects by copying existing ones       │
└────────────────────┴─────────────────────────────────────────────────────┘
```

### Which One Do I Need?

- "I need exactly one of this thing, shared everywhere" → **Singleton**
- "The logic of *when* to create is fixed, but *what* to create varies" → **Factory Method**
- "I need to switch between entire product families" → **Abstract Factory**
- "My constructor has too many parameters and defaults" → **Builder**
- "Creating from scratch is expensive; I have a template I can modify" → **Prototype**

### Real-World Usage in the Java Ecosystem

| Pattern | Example in Java/Ecosystem |
|---------|--------------------------|
| Singleton | `Runtime.getRuntime()`, `Desktop.getDesktop()` |
| Factory Method | `Calendar.getInstance()`, `NumberFormat.getInstance()` |
| Abstract Factory | `DocumentBuilderFactory`, `SAXParserFactory` |
| Builder | `StringBuilder`, `HttpRequest.newBuilder()`, `ProcessBuilder` |
| Prototype | `Object.clone()`, `Arrays.copyOf()` |

Recognizing these patterns in standard library APIs trains your eye to spot them — and apply them — in your own code.
