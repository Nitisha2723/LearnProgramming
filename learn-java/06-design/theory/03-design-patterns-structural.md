# Structural Design Patterns

> "Structural patterns are concerned with how classes and objects are composed to form larger structures. Structural class patterns use inheritance to compose interfaces or implementations."  
> — Gang of Four, *Design Patterns*

Where creational patterns answer "how do I create objects?", structural patterns answer "how do I compose objects into larger structures while keeping the structure flexible and efficient?" They describe how to assemble objects and classes into larger patterns while keeping these structures flexible.

---

## Table of Contents

1. [Decorator — Adding Behavior Without Subclassing](#decorator--adding-behavior-without-subclassing)
2. [Adapter — Making Incompatible Interfaces Cooperate](#adapter--making-incompatible-interfaces-cooperate)
3. [Facade — Simplifying Complex Subsystems](#facade--simplifying-complex-subsystems)
4. [Proxy — Controlling Access to Objects](#proxy--controlling-access-to-objects)
5. [Composite — Trees of Objects](#composite--trees-of-objects)
6. [Pattern Comparison and Relationships](#pattern-comparison-and-relationships)

---

## Decorator — Adding Behavior Without Subclassing

**Intent:** Attach additional responsibilities to an object dynamically. Decorators provide a flexible alternative to subclassing for extending functionality.

### The Problem with Inheritance for Extension

Imagine you're building a coffee ordering system. You start with `Espresso`. Then you need `EspressoWithMilk`, `EspressoWithCaramel`, `EspressoWithMilkAndCaramel`, `EspressoWithSteamedMilkAndVanilla`...

```
Inheritance explosion:

Beverage
├── Espresso
├── EspressoWithMilk
├── EspressoWithCaramel
├── EspressoWithMilkAndCaramel
├── EspressoWithVanilla
├── EspressoWithMilkAndVanillaAndCaramel
├── DarkRoast
├── DarkRoastWithMilk
└── ... (exponential growth)
```

With 5 base coffees and 4 add-ins, you'd need dozens of subclasses. This is unmaintainable. The Decorator pattern solves this by wrapping objects dynamically.

### The Classic Example: Coffee Shop

```java
// The component interface — everything that a beverage must be
public interface Beverage {
    String getDescription();
    double getCost();
}

// Concrete components — the base objects
public class Espresso implements Beverage {
    @Override
    public String getDescription() {
        return "Espresso";
    }

    @Override
    public double getCost() {
        return 1.99;
    }
}

public class DarkRoast implements Beverage {
    @Override
    public String getDescription() {
        return "Dark Roast Coffee";
    }

    @Override
    public double getCost() {
        return 0.99;
    }
}

// The decorator base — it IS-A Beverage AND HAS-A Beverage
// This is the critical structure of the Decorator pattern
public abstract class CondimentDecorator implements Beverage {
    protected final Beverage beverage;  // The wrapped component

    protected CondimentDecorator(Beverage beverage) {
        this.beverage = beverage;
    }

    @Override
    public abstract String getDescription();

    @Override
    public abstract double getCost();
}

// Concrete decorators — each wraps a beverage and adds to it
public class Milk extends CondimentDecorator {
    public Milk(Beverage beverage) {
        super(beverage);
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Milk";
    }

    @Override
    public double getCost() {
        return beverage.getCost() + 0.10;
    }
}

public class Caramel extends CondimentDecorator {
    public Caramel(Beverage beverage) {
        super(beverage);
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Caramel";
    }

    @Override
    public double getCost() {
        return beverage.getCost() + 0.20;
    }
}

public class Vanilla extends CondimentDecorator {
    public Vanilla(Beverage beverage) {
        super(beverage);
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Vanilla";
    }

    @Override
    public double getCost() {
        return beverage.getCost() + 0.15;
    }
}

public class SteamedMilk extends CondimentDecorator {
    public SteamedMilk(Beverage beverage) {
        super(beverage);
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + ", Steamed Milk";
    }

    @Override
    public double getCost() {
        return beverage.getCost() + 0.15;
    }
}

// Usage: compose at runtime, not at compile time
Beverage simple = new Espresso();
System.out.println(simple.getDescription() + " $" + simple.getCost());
// "Espresso $1.99"

// Wrap with decorators — left to right: each wraps the previous
Beverage caramelLatte = new Caramel(new SteamedMilk(new Milk(new Espresso())));
System.out.println(caramelLatte.getDescription() + " $" + caramelLatte.getCost());
// "Espresso, Milk, Steamed Milk, Caramel $2.44"

// You can add the same decorator multiple times
Beverage doubleMilk = new Milk(new Milk(new DarkRoast()));
System.out.println(doubleMilk.getDescription() + " $" + doubleMilk.getCost());
// "Dark Roast Coffee, Milk, Milk $1.19"
```

### The Wrapping Chain Visualized

```
getCost() called on the outermost decorator:

  Caramel.getCost()
    │  returns: beverage.getCost() + 0.20
    │
    └─► SteamedMilk.getCost()
          │  returns: beverage.getCost() + 0.15
          │
          └─► Milk.getCost()
                │  returns: beverage.getCost() + 0.10
                │
                └─► Espresso.getCost()
                      returns: 1.99

Result: 1.99 + 0.10 + 0.15 + 0.20 = 2.44
```

Each decorator delegates to the one it wraps, adding its own cost before returning. The call chain unwinds and the costs accumulate.

### Java I/O Streams: The Masterclass in Decorator

The Java standard library's `java.io` package is the most famous real-world implementation of the Decorator pattern:

```java
// The component interface: InputStream
// Concrete components: FileInputStream, ByteArrayInputStream, SocketInputStream
// The decorator base: FilterInputStream
// Concrete decorators: BufferedInputStream, DataInputStream, GZIPInputStream

// Reading a plain file
InputStream raw = new FileInputStream("/data/records.dat");

// Add buffering (reads in chunks, dramatically faster for disk I/O)
BufferedInputStream buffered = new BufferedInputStream(raw);

// Add typed reading (read ints, longs, UTF strings directly)
DataInputStream typed = new DataInputStream(buffered);

// Add decompression (transparently handles gzip)
GZIPInputStream decompressed = new GZIPInputStream(typed);

// Now read as if it were a simple stream
int recordCount = decompressed.readInt();
String header = decompressed.readUTF();
```

Each layer adds exactly one capability. You compose exactly the capabilities you need for the task at hand. If you need buffering but not typing, stop at `BufferedInputStream`. The components never need to know what's wrapping them.

### Decorator vs. Inheritance

| Concern | Inheritance | Decorator |
|---------|-------------|-----------|
| When is behavior added? | Compile time | Runtime |
| Flexibility | Fixed | Composable, stackable |
| Number of classes | Exponential | Linear |
| Principle | IS-A | IS-A + HAS-A |
| Transparency | Yes | Yes (same interface) |

---

## Adapter — Making Incompatible Interfaces Cooperate

**Intent:** Convert the interface of a class into another interface that clients expect. Adapter lets classes work together that couldn't otherwise because of incompatible interfaces.

### The Everyday Metaphor

A travel adapter for electrical outlets. Your laptop's charger has one plug shape, the wall socket has a different shape. The adapter sits between them and makes them compatible — neither the charger nor the wall outlet changes.

```
Your Code (client) ──► [Target Interface] ──► Adapter ──► Adaptee (existing, incompatible)
```

### Object Adapter Example: Integrating a Third-Party Payment Library

Your codebase defines a `PaymentProcessor` interface. You've purchased a third-party library with `BraintreeGateway`. The third-party library has a completely different method signature. You need to make it work without changing either.

```java
// YOUR existing interface — the target
public interface PaymentProcessor {
    PaymentResult processPayment(String customerId, BigDecimal amount, String currency);
    boolean refundPayment(String transactionId, BigDecimal amount);
}

// THE THIRD-PARTY library — the adaptee (cannot change this)
// This lives in an external JAR; modifying it isn't an option
public class BraintreeGateway {
    public TransactionResult submitForSettlement(
            String customerId, 
            double amountInDollars,
            String currencyCode,
            Map<String, String> metadata) {
        // Braintree-specific API call
        return new TransactionResult();
    }

    public RefundResponse issuePartialRefund(String txnId, double refundAmountInDollars) {
        // Braintree-specific refund call
        return new RefundResponse();
    }
}

// THE ADAPTER — translates your interface to the third-party interface
public class BraintreePaymentAdapter implements PaymentProcessor {

    private final BraintreeGateway gateway;  // the adaptee

    public BraintreePaymentAdapter(BraintreeGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public PaymentResult processPayment(String customerId, BigDecimal amount, String currency) {
        // Translate: BigDecimal → double (your type → their type)
        double amountInDollars = amount.doubleValue();

        // Translate: your method signature → their method signature
        TransactionResult result = gateway.submitForSettlement(
            customerId,
            amountInDollars,
            currency,
            Map.of("source", "web-checkout")  // they require extra metadata
        );

        // Translate: their result type → your result type
        if (result.isSuccessful()) {
            return PaymentResult.success(result.getTransactionId());
        } else {
            return PaymentResult.failure(result.getErrorMessage());
        }
    }

    @Override
    public boolean refundPayment(String transactionId, BigDecimal amount) {
        RefundResponse response = gateway.issuePartialRefund(
            transactionId,
            amount.doubleValue()
        );
        return response.getStatusCode() == 200;
    }
}

// Your code uses the target interface — it never knows about Braintree
public class CheckoutService {
    private final PaymentProcessor paymentProcessor;

    public CheckoutService(PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    public OrderResult checkout(Cart cart, String customerId) {
        // Uses your interface — compatible with any PaymentProcessor implementation
        PaymentResult result = paymentProcessor.processPayment(
            customerId, cart.getTotal(), "USD"
        );
        // ...
    }
}

// Wiring it together
BraintreeGateway braintree = new BraintreeGateway("merchant_id", "public_key", "private_key");
PaymentProcessor processor = new BraintreePaymentAdapter(braintree);
CheckoutService checkout = new CheckoutService(processor);
```

### Object Adapter vs. Class Adapter

**Object Adapter** (shown above): Uses composition — the adapter *has* the adaptee. More flexible; works with any subclass of the adaptee.

**Class Adapter**: Uses multiple inheritance — the adapter *extends* both the target and the adaptee. Java doesn't support multiple class inheritance, so this is typically done through extending the adaptee and implementing the target interface:

```java
// Class adapter via extending + implementing
public class ClassAdapter extends BraintreeGateway implements PaymentProcessor {
    // Can call inherited BraintreeGateway methods directly (no wrapper field needed)
    // But cannot adapt arbitrary BraintreeGateway subclasses — less flexible
}
```

Object Adapter is generally preferred in Java for its flexibility and alignment with composition-over-inheritance.

---

## Facade — Simplifying Complex Subsystems

**Intent:** Provide a unified, simplified interface to a set of interfaces in a subsystem. Facade defines a higher-level interface that makes the subsystem easier to use.

### The Problem: Subsystem Complexity

Home theater systems are notoriously complex to operate. To watch a movie, you might need to:
1. Turn on the projector, switch it to HDMI input
2. Turn on the amplifier, set volume, select surround sound mode
3. Turn on the streaming device, navigate to the movie
4. Dim the lights (via smart home API)
5. Close the blinds (via smart home API)
6. Start the movie

Each of these involves calling methods on different objects with specific ordering requirements. The `Facade` pattern provides a single simple method: `watchMovie("The Dark Knight")`.

```java
// Complex subsystem components
public class Projector {
    public void on() { System.out.println("Projector: Powering on"); }
    public void off() { System.out.println("Projector: Powering off"); }
    public void setInput(String input) { System.out.println("Projector: Input set to " + input); }
    public void wideScreenMode() { System.out.println("Projector: Switching to wide screen 16:9"); }
}

public class Amplifier {
    public void on() { System.out.println("Amplifier: Powering on"); }
    public void off() { System.out.println("Amplifier: Powering off"); }
    public void setVolume(int volume) { System.out.println("Amplifier: Volume set to " + volume); }
    public void setSurroundSound() { System.out.println("Amplifier: Surround sound enabled (Dolby Atmos)"); }
    public void setInput(String input) { System.out.println("Amplifier: Input set to " + input); }
}

public class StreamingDevice {
    public void on() { System.out.println("Streaming device: Powering on"); }
    public void off() { System.out.println("Streaming device: Powering off"); }
    public void play(String movie) { System.out.println("Streaming device: Now playing '" + movie + "'"); }
    public void stop() { System.out.println("Streaming device: Stopped"); }
}

public class SmartLights {
    public void dim(int level) { System.out.println("Lights: Dimming to " + level + "%"); }
    public void brighten() { System.out.println("Lights: Full brightness"); }
}

public class SmartBlinds {
    public void close() { System.out.println("Blinds: Closing"); }
    public void open() { System.out.println("Blinds: Opening"); }
}

// Without a Facade, the client code is exhausting:
// projector.on();
// projector.setInput("HDMI-1");
// projector.wideScreenMode();
// amplifier.on();
// amplifier.setInput("Streaming");
// amplifier.setSurroundSound();
// amplifier.setVolume(50);
// streamingDevice.on();
// lights.dim(10);
// blinds.close();
// streamingDevice.play(movie);
// ... and reverse for turning off

// THE FACADE — provides a simple, high-level interface
public class HomeTheaterFacade {
    private final Projector projector;
    private final Amplifier amplifier;
    private final StreamingDevice streamingDevice;
    private final SmartLights lights;
    private final SmartBlinds blinds;

    public HomeTheaterFacade(
            Projector projector,
            Amplifier amplifier,
            StreamingDevice streamingDevice,
            SmartLights lights,
            SmartBlinds blinds) {
        this.projector = projector;
        this.amplifier = amplifier;
        this.streamingDevice = streamingDevice;
        this.lights = lights;
        this.blinds = blinds;
    }

    public void watchMovie(String movie) {
        System.out.println("--- Getting ready to watch '" + movie + "' ---");
        projector.on();
        projector.setInput("HDMI-1");
        projector.wideScreenMode();
        amplifier.on();
        amplifier.setInput("Streaming");
        amplifier.setSurroundSound();
        amplifier.setVolume(50);
        streamingDevice.on();
        lights.dim(10);
        blinds.close();
        streamingDevice.play(movie);
    }

    public void endMovie() {
        System.out.println("--- Shutting down the home theater ---");
        streamingDevice.stop();
        streamingDevice.off();
        amplifier.off();
        projector.off();
        lights.brighten();
        blinds.open();
    }

    public void listenToMusic(int volume) {
        System.out.println("--- Starting music mode ---");
        amplifier.on();
        amplifier.setInput("Bluetooth");
        amplifier.setVolume(volume);
        lights.dim(50);
    }
}

// Client code: clean and expressive
HomeTheaterFacade theater = new HomeTheaterFacade(
    new Projector(), new Amplifier(), new StreamingDevice(),
    new SmartLights(), new SmartBlinds()
);

theater.watchMovie("The Dark Knight");
// ... 2 hours later
theater.endMovie();
```

### Facade Doesn't Lock You Out

An important property: the Facade doesn't prevent clients from accessing the subsystem directly. It provides a *simplified* interface for *common* use cases, while advanced users can still reach through to the subsystem components directly. This is different from Adapter, which is about compatibility.

### Facade in Real Frameworks

- **Spring's `JdbcTemplate`**: Facades over JDBC — you don't manually open connections, create statements, handle ResultSets, or close connections.
- **SLF4J**: A facade over different logging frameworks (Logback, Log4j, java.util.logging).
- **Apache HttpClient's `HttpClients.createDefault()`**: Facades the complex configuration of an HTTP client.

---

## Proxy — Controlling Access to Objects

**Intent:** Provide a surrogate or placeholder for another object to control access to it.

### The Pattern Structure

```
Client ──► [Subject Interface] ──► Proxy ──► RealSubject
                                    │
                                    (may control access, add logging,
                                     delay creation, add caching, etc.)
```

The Proxy and the RealSubject both implement the same interface. The client doesn't know whether it's talking to the real thing or a proxy.

### Three Main Types of Proxy

#### 1. Virtual Proxy — Lazy Loading

Creates expensive objects only when they're actually needed:

```java
public interface Image {
    void display();
    int getWidth();
    int getHeight();
}

// Expensive to create — loads from disk or network
public class HighResolutionImage implements Image {
    private final String filename;
    private byte[] imageData;
    private int width;
    private int height;

    public HighResolutionImage(String filename) {
        this.filename = filename;
        System.out.println("Loading image: " + filename + " (expensive operation)");
        // Simulate loading a large file from disk
        this.imageData = loadFromDisk(filename);
        this.width = 4000;
        this.height = 3000;
    }

    @Override
    public void display() {
        System.out.println("Displaying " + filename + " (" + width + "x" + height + ")");
    }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }

    private byte[] loadFromDisk(String filename) {
        // Simulate slow disk read
        return new byte[0]; // placeholder
    }
}

// The Virtual Proxy — defers the expensive creation
public class ImageProxy implements Image {
    private final String filename;
    private HighResolutionImage realImage;  // null until first use

    public ImageProxy(String filename) {
        this.filename = filename;
        // Lightweight constructor — NO loading happens here
    }

    @Override
    public void display() {
        if (realImage == null) {
            realImage = new HighResolutionImage(filename);  // created on first use
        }
        realImage.display();
    }

    @Override
    public int getWidth() {
        // Can return metadata without loading the full image
        return 4000; // stored in metadata file — cheap
    }

    @Override
    public int getHeight() {
        return 3000;
    }
}

// Usage in a gallery application
public class ImageGallery {
    private final List<Image> images = new ArrayList<>();

    public ImageGallery(List<String> filenames) {
        // Creating 1000 ImageProxy objects is instant
        // Creating 1000 HighResolutionImage objects would load 1000 images
        for (String filename : filenames) {
            images.add(new ImageProxy(filename));  // cheap
        }
    }

    public void displayImage(int index) {
        images.get(index).display();  // only THIS image loads when displayed
    }

    public void layoutThumbnails() {
        // Use dimensions without loading full images
        for (Image image : images) {
            System.out.printf("Thumbnail: %dx%d%n", image.getWidth(), image.getHeight());
        }
    }
}
```

#### 2. Protection Proxy — Access Control

Controls access based on permissions:

```java
public interface BankAccount {
    void deposit(BigDecimal amount);
    void withdraw(BigDecimal amount);
    BigDecimal getBalance();
    List<Transaction> getTransactionHistory();
}

public class BankAccountProxy implements BankAccount {
    private final BankAccount realAccount;
    private final User currentUser;
    private final AccessControlService accessControl;

    public BankAccountProxy(BankAccount realAccount, User currentUser, AccessControlService acl) {
        this.realAccount = realAccount;
        this.currentUser = currentUser;
        this.accessControl = acl;
    }

    @Override
    public void deposit(BigDecimal amount) {
        accessControl.checkPermission(currentUser, "DEPOSIT");
        // Audit log before delegating
        auditLog("DEPOSIT", amount);
        realAccount.deposit(amount);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        accessControl.checkPermission(currentUser, "WITHDRAW");
        if (amount.compareTo(currentUser.getDailyWithdrawalLimit()) > 0) {
            throw new SecurityException("Withdrawal exceeds daily limit");
        }
        auditLog("WITHDRAW", amount);
        realAccount.withdraw(amount);
    }

    @Override
    public BigDecimal getBalance() {
        accessControl.checkPermission(currentUser, "VIEW_BALANCE");
        return realAccount.getBalance();
    }

    @Override
    public List<Transaction> getTransactionHistory() {
        accessControl.checkPermission(currentUser, "VIEW_HISTORY");
        return realAccount.getTransactionHistory();
    }

    private void auditLog(String operation, BigDecimal amount) {
        System.out.printf("[AUDIT] User %s performed %s of %s at %s%n",
            currentUser.getId(), operation, amount, LocalDateTime.now());
    }
}
```

#### 3. Caching Proxy — Performance

```java
public interface WeatherService {
    WeatherData getWeather(String city);
}

public class CachingWeatherProxy implements WeatherService {
    private final WeatherService realService;
    private final Map<String, CachedEntry> cache = new ConcurrentHashMap<>();
    private final Duration cacheTtl;

    public CachingWeatherProxy(WeatherService realService, Duration cacheTtl) {
        this.realService = realService;
        this.cacheTtl = cacheTtl;
    }

    @Override
    public WeatherData getWeather(String city) {
        CachedEntry entry = cache.get(city);

        if (entry != null && !entry.isExpired()) {
            System.out.println("Cache HIT for: " + city);
            return entry.getData();
        }

        System.out.println("Cache MISS for: " + city + " — fetching from API");
        WeatherData data = realService.getWeather(city);
        cache.put(city, new CachedEntry(data, Instant.now().plus(cacheTtl)));
        return data;
    }

    private static class CachedEntry {
        private final WeatherData data;
        private final Instant expiresAt;

        CachedEntry(WeatherData data, Instant expiresAt) {
            this.data = data;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        WeatherData getData() {
            return data;
        }
    }
}
```

---

## Composite — Trees of Objects

**Intent:** Compose objects into tree structures to represent part-whole hierarchies. Composite lets clients treat individual objects and compositions of objects uniformly.

### The Problem: Files and Directories

A file system has files (leaf nodes) and directories (composite nodes). Directories can contain files *and* other directories. You want to perform operations (like `getSize()` or `print()`) on both, without checking "is this a file or a directory?"

```
    /home/user/
    ├── documents/          ← Directory (composite)
    │   ├── resume.pdf      ← File (leaf)
    │   └── projects/       ← Directory (composite)
    │       ├── design.drawio ← File (leaf)
    │       └── notes.txt   ← File (leaf)
    ├── pictures/           ← Directory (composite)
    │   ├── photo1.jpg      ← File (leaf)
    │   └── photo2.jpg      ← File (leaf)
    └── config.yaml         ← File (leaf)
```

The key insight: *every* node supports `getSize()` and `print()`. For a file, `getSize()` returns the file size. For a directory, `getSize()` returns the sum of all children's sizes — recursively.

```java
// The Component — the uniform interface
public interface FileSystemItem {
    String getName();
    long getSize();             // bytes
    void print(String indent);  // display tree
    boolean isDirectory();
}

// Leaf — no children
public class File implements FileSystemItem {
    private final String name;
    private final long size;

    public File(String name, long size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public String getName() { return name; }

    @Override
    public long getSize() { return size; }  // Just the file's own size

    @Override
    public void print(String indent) {
        System.out.printf("%s📄 %s (%s)%n", indent, name, formatSize(size));
    }

    @Override
    public boolean isDirectory() { return false; }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }
}

// Composite — can hold children (both Files and other Directories)
public class Directory implements FileSystemItem {
    private final String name;
    private final List<FileSystemItem> children = new ArrayList<>();

    public Directory(String name) {
        this.name = name;
    }

    public void add(FileSystemItem item) {
        children.add(item);
    }

    public void remove(FileSystemItem item) {
        children.remove(item);
    }

    @Override
    public String getName() { return name; }

    @Override
    public long getSize() {
        // Recursive! Delegates to each child, which may also be directories
        return children.stream()
            .mapToLong(FileSystemItem::getSize)
            .sum();
    }

    @Override
    public void print(String indent) {
        System.out.printf("%s📁 %s/ (%s)%n", indent, name, formatSize(getSize()));
        children.forEach(child -> child.print(indent + "  "));
    }

    @Override
    public boolean isDirectory() { return true; }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }
}

// Building and using the tree
Directory home = new Directory("home");

Directory documents = new Directory("documents");
documents.add(new File("resume.pdf", 245_000));

Directory projects = new Directory("projects");
projects.add(new File("design.drawio", 85_000));
projects.add(new File("notes.txt", 3_200));
documents.add(projects);

Directory pictures = new Directory("pictures");
pictures.add(new File("photo1.jpg", 4_800_000));
pictures.add(new File("photo2.jpg", 3_200_000));

home.add(documents);
home.add(pictures);
home.add(new File("config.yaml", 1_400));

// Print the tree — works uniformly on directories and files
home.print("");

// Get total size — works uniformly, recursion is handled inside the composites
System.out.println("\nTotal home directory size: " + home.getSize() + " bytes");
```

Output:
```
📁 home/ (8 MB)
  📁 documents/ (333 KB)
    📄 resume.pdf (239 KB)
    📁 projects/ (86 KB)
      📄 design.drawio (83 KB)
      📄 notes.txt (3 KB)
  📁 pictures/ (7 MB)
    📄 photo1.jpg (4 MB)
    📄 photo2.jpg (3 MB)
  📄 config.yaml (1 KB)
```

### Composite in Practice

The Composite pattern appears everywhere:

- **GUI toolkits**: `Container` holds `Component`s (which may themselves be `Container`s). A `JPanel` holds buttons, labels, and other `JPanel`s.
- **HTML DOM**: An element node can contain text nodes and other element nodes.
- **Expression trees**: `(3 + 4) * (5 - 2)` — the `*` node has two children, each being a sub-expression.
- **Organization charts**: An `Employee` can be an individual contributor (leaf) or a manager (composite with direct reports).

---

## Pattern Comparison and Relationships

```
Structural Patterns — Intent Summary:

┌──────────────┬────────────────────────────────────────────────────────┐
│ Pattern      │ Core Intent                                            │
├──────────────┼────────────────────────────────────────────────────────┤
│ Decorator    │ Add behavior by wrapping — same interface, more power  │
│ Adapter      │ Bridge incompatible interfaces — translation layer     │
│ Facade       │ Simplify complex subsystem — one entry point           │
│ Proxy        │ Control access to an object — same interface, policies │
│ Composite    │ Treat parts and wholes uniformly — tree structures     │
└──────────────┴────────────────────────────────────────────────────────┘
```

### Common Confusions

**Decorator vs. Proxy**

Both wrap an object and implement the same interface, so they look identical in structure. The difference is *intent*:
- **Decorator** adds behavior: "I want to add logging/buffering/pricing to this."
- **Proxy** controls access: "I want to guard/cache/lazily load this."

A decorator chain is typically built by the client code for configuration purposes. A proxy is typically transparent — the client doesn't know (or care) it's there.

**Adapter vs. Facade**

- **Adapter** bridges one interface to another. There's an existing interface the client expects, and a different one the adaptee provides.
- **Facade** creates a new, simpler interface over a complex subsystem. The client's interface is defined *by* the facade.

**Facade vs. Proxy**

Both provide a simplified interface. The difference is structural:
- **Facade** works with an entire *subsystem* — multiple objects.
- **Proxy** provides a surrogate for a *single* object with the *same* interface.
