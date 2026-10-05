# Behavioral Design Patterns

> "Behavioral patterns are concerned with algorithms and the assignment of responsibilities between objects. Behavioral patterns describe not just patterns of objects or classes but also the patterns of communication between them."  
> — Gang of Four, *Design Patterns*

Behavioral patterns focus on *how objects communicate and distribute work*. Where structural patterns define object compositions, behavioral patterns define the flow of control and responsibility. They are the patterns that make objects collaborate effectively.

---

## Table of Contents

1. [Observer — Notify the Interested](#observer--notify-the-interested)
2. [Strategy — Swap Algorithms at Runtime](#strategy--swap-algorithms-at-runtime)
3. [Command — Requests as Objects](#command--requests-as-objects)
4. [Template Method — Define the Skeleton, Fill in the Steps](#template-method--define-the-skeleton-fill-in-the-steps)
5. [Chain of Responsibility — Pass Until Handled](#chain-of-responsibility--pass-until-handled)
6. [State — Objects That Change Their Own Behavior](#state--objects-that-change-their-own-behavior)
7. [Pattern Comparison Summary](#pattern-comparison-summary)

---

## Observer — Notify the Interested

**Intent:** Define a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically.

### The Problem It Solves

You have a `StockPrice` object. Multiple objects need to react when the price changes: a price-alert display, a trading algorithm, a logging system, and a portfolio calculator. You could make `StockPrice` directly call each of these — but then `StockPrice` must know about all of them, and adding a new subscriber requires modifying `StockPrice`.

The Observer pattern decouples the *subject* (the object that changes) from its *observers* (the objects that react), letting you add and remove observers freely.

### Classic Implementation

```java
// The Observer interface — anyone who wants to be notified implements this
public interface StockObserver {
    void onPriceChanged(String symbol, double newPrice, double previousPrice);
}

// The Subject (Observable) — manages its list of observers
public class StockMarket {
    private final Map<String, Double> prices = new HashMap<>();
    private final List<StockObserver> observers = new CopyOnWriteArrayList<>();
    // CopyOnWriteArrayList is thread-safe for our observer list

    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(StockObserver observer) {
        observers.remove(observer);
    }

    public void setPrice(String symbol, double price) {
        double previousPrice = prices.getOrDefault(symbol, 0.0);
        prices.put(symbol, price);

        if (price != previousPrice) {
            notifyObservers(symbol, price, previousPrice);
        }
    }

    private void notifyObservers(String symbol, double newPrice, double previousPrice) {
        // Each observer is notified — they don't know about each other
        for (StockObserver observer : observers) {
            observer.onPriceChanged(symbol, newPrice, previousPrice);
        }
    }

    public double getPrice(String symbol) {
        return prices.getOrDefault(symbol, 0.0);
    }
}

// Concrete observers — each reacts differently
public class PriceAlertDisplay implements StockObserver {
    private final double alertThreshold;

    public PriceAlertDisplay(double threshold) {
        this.alertThreshold = threshold;
    }

    @Override
    public void onPriceChanged(String symbol, double newPrice, double previousPrice) {
        double changePercent = Math.abs((newPrice - previousPrice) / previousPrice * 100);
        if (changePercent > alertThreshold) {
            System.out.printf("ALERT: %s moved %.2f%% (%.2f → %.2f)%n",
                symbol, changePercent, previousPrice, newPrice);
        }
    }
}

public class TradingAlgorithm implements StockObserver {
    @Override
    public void onPriceChanged(String symbol, double newPrice, double previousPrice) {
        if (newPrice < previousPrice * 0.95) {  // 5% drop = buy signal
            System.out.printf("TRADING: Buy signal for %s at %.2f%n", symbol, newPrice);
        } else if (newPrice > previousPrice * 1.10) {  // 10% rise = sell signal
            System.out.printf("TRADING: Sell signal for %s at %.2f%n", symbol, newPrice);
        }
    }
}

public class PriceLogger implements StockObserver {
    @Override
    public void onPriceChanged(String symbol, double newPrice, double previousPrice) {
        System.out.printf("[LOG] %s: %.2f (prev: %.2f) at %s%n",
            symbol, newPrice, previousPrice, LocalDateTime.now());
    }
}

// Usage: subjects and observers are decoupled
StockMarket market = new StockMarket();
market.setPrice("AAPL", 175.00);  // Initial price — no observers yet, no notifications

PriceAlertDisplay alert = new PriceAlertDisplay(3.0);
TradingAlgorithm algo = new TradingAlgorithm();
PriceLogger logger = new PriceLogger();

market.addObserver(alert);
market.addObserver(algo);
market.addObserver(logger);

market.setPrice("AAPL", 171.00);  // Drop of ~2.3% — triggers logger, not alert
market.setPrice("AAPL", 162.00);  // Drop to ~7.4% — triggers alert AND buy signal
market.setPrice("GOOG", 140.00);  // All observers notified for GOOG too

market.removeObserver(algo);  // Remove one observer
market.setPrice("AAPL", 195.00);  // Only alert and logger notified now
```

### Java's Built-In Event Support

Java has several built-in mechanisms for the Observer pattern:

```java
// java.util.EventListener marker interface
// java.beans.PropertyChangeListener — the classic Java bean event system

public class TemperatureSensor {
    private double temperature;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }

    public void setTemperature(double newTemp) {
        double oldTemp = this.temperature;
        this.temperature = newTemp;
        // Fires the "temperature" property change event
        support.firePropertyChange("temperature", oldTemp, newTemp);
    }
}

// Observer using lambda
sensor.addPropertyChangeListener("temperature", event -> {
    double newTemp = (Double) event.getNewValue();
    if (newTemp > 100.0) {
        System.out.println("WARNING: Overheating! " + newTemp + "°C");
    }
});
```

### Observer vs. Reactive Programming

Traditional Observer is push-based and synchronous — the subject calls observers directly, one by one, in the calling thread. If an observer is slow, it blocks the subject.

Reactive programming (RxJava, Project Reactor) extends this model with:
- **Backpressure**: observers can signal they're overwhelmed
- **Asynchronous delivery**: notifications happen on separate threads
- **Composition**: filter, map, merge streams of events
- **Error handling**: propagation of errors through the observer chain

```java
// Reactive approach with Java 9+ Flow API
import java.util.concurrent.Flow.*;

public class ReactiveStockPrice implements Publisher<PriceUpdate> {
    // Java 9 Flow API — standardized reactive streams
    // Publishers emit items; Subscribers receive them
    // ...
}
```

Reactive programming is Observer at industrial scale — but for most use cases, the classic pattern is simpler and sufficient.

---

## Strategy — Swap Algorithms at Runtime

**Intent:** Define a family of algorithms, encapsulate each one, and make them interchangeable. Strategy lets the algorithm vary independently from clients that use it.

### The Problem: if/else Chains

```java
// BAD: Adding a new sort algorithm requires modifying this class
public class Sorter {
    public void sort(int[] array, String algorithm) {
        if (algorithm.equals("bubble")) {
            // bubble sort implementation
        } else if (algorithm.equals("quick")) {
            // quicksort implementation
        } else if (algorithm.equals("merge")) {
            // merge sort implementation
        } else if (algorithm.equals("heap")) {
            // heap sort implementation
        }
        // Every new algorithm = modify this class (OCP violation)
    }
}
```

This pattern appears everywhere: payment methods, compression algorithms, routing strategies, discount calculations. The Strategy pattern replaces these if/else chains with polymorphism.

### Clean Implementation

```java
// The Strategy interface
@FunctionalInterface
public interface SortStrategy {
    void sort(int[] array);
}

// Concrete strategies — each in its own class (OCP: new algorithm = new file)
public class BubbleSortStrategy implements SortStrategy {
    @Override
    public void sort(int[] array) {
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
        System.out.println("Sorted using BubbleSort");
    }
}

public class QuickSortStrategy implements SortStrategy {
    @Override
    public void sort(int[] array) {
        quickSort(array, 0, array.length - 1);
        System.out.println("Sorted using QuickSort");
    }

    private void quickSort(int[] array, int low, int high) {
        if (low < high) {
            int pi = partition(array, low, high);
            quickSort(array, low, pi - 1);
            quickSort(array, pi + 1, high);
        }
    }

    private int partition(int[] array, int low, int high) {
        int pivot = array[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (array[j] <= pivot) {
                i++;
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;
        return i + 1;
    }
}

// The Context — holds the strategy and delegates to it
public class DataProcessor {
    private SortStrategy sortStrategy;

    public DataProcessor(SortStrategy sortStrategy) {
        this.sortStrategy = sortStrategy;
    }

    // Strategy can be changed at runtime
    public void setSortStrategy(SortStrategy strategy) {
        this.sortStrategy = strategy;
    }

    public void process(int[] data) {
        System.out.println("Before: " + Arrays.toString(data));
        sortStrategy.sort(data);
        System.out.println("After: " + Arrays.toString(data));
    }
}

// Usage
DataProcessor processor = new DataProcessor(new QuickSortStrategy());
processor.process(new int[]{64, 25, 12, 22, 11});

// Swap strategy at runtime — for small arrays, bubble sort might be fine
processor.setSortStrategy(new BubbleSortStrategy());
processor.process(new int[]{5, 3, 1});

// Strategy as lambda — when the interface is @FunctionalInterface
processor.setSortStrategy(arr -> Arrays.sort(arr));  // Java's built-in sort
```

### A More Practical Example: Payment Methods

```java
public interface PaymentStrategy {
    PaymentResult pay(BigDecimal amount, String currency);
    String getMethodName();
}

public class CreditCardPayment implements PaymentStrategy {
    private final String cardNumber;
    private final String cvv;
    private final YearMonth expiry;

    public CreditCardPayment(String cardNumber, String cvv, YearMonth expiry) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiry = expiry;
    }

    @Override
    public PaymentResult pay(BigDecimal amount, String currency) {
        // Credit card processing logic
        System.out.printf("Charging credit card ending in %s: %s %s%n",
            cardNumber.substring(cardNumber.length() - 4), amount, currency);
        return PaymentResult.success("CC-" + UUID.randomUUID());
    }

    @Override
    public String getMethodName() { return "Credit Card"; }
}

public class PayPalPayment implements PaymentStrategy {
    private final String email;

    public PayPalPayment(String email) {
        this.email = email;
    }

    @Override
    public PaymentResult pay(BigDecimal amount, String currency) {
        System.out.printf("Initiating PayPal payment for %s: %s %s%n", email, amount, currency);
        return PaymentResult.success("PP-" + UUID.randomUUID());
    }

    @Override
    public String getMethodName() { return "PayPal"; }
}

public class CryptoPayment implements PaymentStrategy {
    private final String walletAddress;
    private final String coinType;

    public CryptoPayment(String walletAddress, String coinType) {
        this.walletAddress = walletAddress;
        this.coinType = coinType;
    }

    @Override
    public PaymentResult pay(BigDecimal amount, String currency) {
        System.out.printf("Sending %s to wallet %s...%n", coinType, walletAddress);
        return PaymentResult.success("CRYPTO-" + UUID.randomUUID());
    }

    @Override
    public String getMethodName() { return coinType; }
}

public class ShoppingCart {
    private final List<CartItem> items = new ArrayList<>();
    private PaymentStrategy paymentStrategy;

    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }

    public OrderConfirmation checkout() {
        if (paymentStrategy == null) {
            throw new IllegalStateException("Payment method not selected");
        }
        BigDecimal total = calculateTotal();
        PaymentResult result = paymentStrategy.pay(total, "USD");
        return new OrderConfirmation(result.getTransactionId(), total, paymentStrategy.getMethodName());
    }

    private BigDecimal calculateTotal() {
        return items.stream()
            .map(CartItem::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

---

## Command — Requests as Objects

**Intent:** Encapsulate a request as an object, thereby letting you parameterize clients with different requests, queue or log requests, and support undoable operations.

### Why Encapsulate a Request?

When a request is an object:
- It can be stored, passed around, serialized, logged, queued
- It can be undone (if you implement `undo()`)
- Multiple requests can be batched into a macro
- Requests can be delayed or scheduled

### Undo/Redo with Command

```java
// The Command interface
public interface Command {
    void execute();
    void undo();
    String getDescription();
}

// The receiver — the object that actually does the work
public class TextDocument {
    private final StringBuilder content = new StringBuilder();

    public void insertText(int position, String text) {
        content.insert(position, text);
    }

    public void deleteText(int position, int length) {
        content.delete(position, position + length);
    }

    public String getContent() {
        return content.toString();
    }
}

// Concrete Commands
public class InsertTextCommand implements Command {
    private final TextDocument document;
    private final int position;
    private final String text;

    public InsertTextCommand(TextDocument document, int position, String text) {
        this.document = document;
        this.position = position;
        this.text = text;
    }

    @Override
    public void execute() {
        document.insertText(position, text);
    }

    @Override
    public void undo() {
        document.deleteText(position, text.length());
    }

    @Override
    public String getDescription() {
        return "Insert '" + text + "' at position " + position;
    }
}

public class DeleteTextCommand implements Command {
    private final TextDocument document;
    private final int position;
    private final int length;
    private String deletedText;  // saved for undo

    public DeleteTextCommand(TextDocument document, int position, int length) {
        this.document = document;
        this.position = position;
        this.length = length;
    }

    @Override
    public void execute() {
        // Save the deleted text before deleting (needed for undo)
        deletedText = document.getContent().substring(position, position + length);
        document.deleteText(position, length);
    }

    @Override
    public void undo() {
        document.insertText(position, deletedText);
    }

    @Override
    public String getDescription() {
        return "Delete " + length + " characters at position " + position;
    }
}

// The Invoker — manages command history
public class CommandHistory {
    private final Deque<Command> history = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        command.execute();
        history.push(command);
        redoStack.clear();  // New action clears redo history
        System.out.println("Executed: " + command.getDescription());
    }

    public void undo() {
        if (history.isEmpty()) {
            System.out.println("Nothing to undo");
            return;
        }
        Command command = history.pop();
        command.undo();
        redoStack.push(command);
        System.out.println("Undone: " + command.getDescription());
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            System.out.println("Nothing to redo");
            return;
        }
        Command command = redoStack.pop();
        command.execute();
        history.push(command);
        System.out.println("Redone: " + command.getDescription());
    }
}

// Usage
TextDocument doc = new TextDocument();
CommandHistory history = new CommandHistory();

history.execute(new InsertTextCommand(doc, 0, "Hello"));
history.execute(new InsertTextCommand(doc, 5, ", World"));
System.out.println(doc.getContent());  // "Hello, World"

history.execute(new DeleteTextCommand(doc, 5, 7));
System.out.println(doc.getContent());  // "Hello"

history.undo();
System.out.println(doc.getContent());  // "Hello, World" — restored

history.undo();
System.out.println(doc.getContent());  // "Hello" — undone again

history.redo();
System.out.println(doc.getContent());  // "Hello, World" — redone
```

### Macro Commands (Composite Commands)

```java
// A command that executes multiple commands as one atomic operation
public class MacroCommand implements Command {
    private final List<Command> commands;
    private final String description;

    public MacroCommand(String description, Command... commands) {
        this.description = description;
        this.commands = List.of(commands);
    }

    @Override
    public void execute() {
        commands.forEach(Command::execute);
    }

    @Override
    public void undo() {
        // Undo in reverse order
        List<Command> reversed = new ArrayList<>(commands);
        Collections.reverse(reversed);
        reversed.forEach(Command::undo);
    }

    @Override
    public String getDescription() {
        return description;
    }
}

// "Format document" macro = save + run formatter + insert footer
Command formatMacro = new MacroCommand("Format Document",
    new InsertTextCommand(doc, 0, "# Title\n\n"),
    new InsertTextCommand(doc, doc.getContent().length(), "\n\n---\n*Auto-generated*")
);
history.execute(formatMacro);
history.undo();  // Undoes all three insertions atomically
```

---

## Template Method — Define the Skeleton, Fill in the Steps

**Intent:** Define the skeleton of an algorithm in an operation, deferring some steps to subclasses. Template Method lets subclasses redefine certain steps of an algorithm without changing the algorithm's structure.

### The Classic Structure

```
Abstract Class (the template):
  + finalOperation()          ← public, calls the steps in order (the skeleton)
  # step1()                   ← abstract — must override
  # step2()                   ← concrete — default behavior, may override (hook)
  # step3()                   ← abstract — must override
```

Subclasses override only the steps they need to customize. The algorithm's sequence is fixed in the parent.

### Data Export Example

All data export formats follow the same steps: connect to data source, query data, format it, write to file. Only the formatting step differs.

```java
// The abstract class with the template method
public abstract class DataExporter {

    // THE TEMPLATE METHOD — public, final, defines the algorithm skeleton
    public final void export(String query, String outputPath) {
        System.out.println("Starting export to: " + outputPath);

        Object connection = openDataSource();                    // Step 1
        List<Map<String, Object>> data = fetchData(connection, query);  // Step 2
        String formatted = formatData(data);                    // Step 3 — abstract
        writeToFile(formatted, outputPath);                     // Step 4
        closeDataSource(connection);                            // Step 5

        System.out.println("Export complete: " + data.size() + " records written");
    }

    // Steps 1, 2, 4, 5 have default implementations (hooks or concrete steps)
    protected Object openDataSource() {
        System.out.println("Opening default database connection");
        return new Object(); // placeholder for actual connection
    }

    protected List<Map<String, Object>> fetchData(Object connection, String query) {
        System.out.println("Executing query: " + query);
        // Generic fetch logic
        return List.of(
            Map.of("id", 1, "name", "Alice", "salary", 75000),
            Map.of("id", 2, "name", "Bob", "salary", 82000)
        );
    }

    protected void writeToFile(String content, String path) {
        System.out.println("Writing " + content.length() + " chars to " + path);
        // Generic file write
    }

    protected void closeDataSource(Object connection) {
        System.out.println("Closing connection");
    }

    // ABSTRACT STEP — each subclass must implement this
    protected abstract String formatData(List<Map<String, Object>> data);

    // Hook — optional override (default: no-op)
    protected void onExportComplete(String path) {
        // Subclasses can override to e.g. send a notification
    }
}

// Concrete implementation: CSV
public class CsvDataExporter extends DataExporter {

    @Override
    protected String formatData(List<Map<String, Object>> data) {
        if (data.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        // Header row
        String header = String.join(",", data.get(0).keySet());
        sb.append(header).append("\n");

        // Data rows
        for (Map<String, Object> row : data) {
            String line = row.values().stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}

// Concrete implementation: JSON
public class JsonDataExporter extends DataExporter {

    @Override
    protected String formatData(List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < data.size(); i++) {
            sb.append("  {\n");
            Map<String, Object> row = data.get(i);
            List<String> entries = new ArrayList<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String value = entry.getValue() instanceof String
                    ? "\"" + entry.getValue() + "\""
                    : entry.getValue().toString();
                entries.add("    \"" + entry.getKey() + "\": " + value);
            }
            sb.append(String.join(",\n", entries)).append("\n");
            sb.append("  }");
            if (i < data.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    protected void onExportComplete(String path) {
        // JSON exports get validated after writing
        System.out.println("Validating JSON at: " + path);
    }
}

// Concrete implementation: HTML
public class HtmlDataExporter extends DataExporter {

    @Override
    protected String formatData(List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder("<table>\n<tr>");

        // Headers
        if (!data.isEmpty()) {
            data.get(0).keySet().forEach(key -> sb.append("<th>").append(key).append("</th>"));
        }
        sb.append("</tr>\n");

        // Rows
        for (Map<String, Object> row : data) {
            sb.append("<tr>");
            row.values().forEach(val -> sb.append("<td>").append(val).append("</td>"));
            sb.append("</tr>\n");
        }

        sb.append("</table>");
        return sb.toString();
    }
}

// Usage: the same algorithm structure, different format output
String query = "SELECT id, name, salary FROM employees WHERE active = true";

DataExporter csvExporter = new CsvDataExporter();
csvExporter.export(query, "/reports/employees.csv");

DataExporter jsonExporter = new JsonDataExporter();
jsonExporter.export(query, "/reports/employees.json");

DataExporter htmlExporter = new HtmlDataExporter();
htmlExporter.export(query, "/reports/employees.html");
```

### Template Method vs. Strategy

Both patterns allow varying the behavior of an algorithm. The difference is the mechanism:

| Concern | Template Method | Strategy |
|---------|----------------|----------|
| Mechanism | Inheritance — subclass overrides steps | Composition — inject algorithm as object |
| Coupling | Tighter — parent controls structure | Looser — strategy is independent |
| Runtime swap | No — behavior fixed at class creation | Yes — can change strategy at runtime |
| Granularity | Override specific steps | Replace entire algorithm |
| Principle | "Hollywood Principle" — don't call us, we'll call you | Dependency Inversion |

---

## Chain of Responsibility — Pass Until Handled

**Intent:** Avoid coupling the sender of a request to its receiver by giving more than one object a chance to handle the request. Chain the receiving objects and pass the request along the chain until an object handles it.

### The Middleware Metaphor

Every modern web framework processes HTTP requests through a chain of middleware handlers. Each handler decides: "I'll handle this, or pass it to the next handler."

```
HTTP Request
    │
    ▼
Authentication Handler ──► if fails: return 401; if passes: next()
    │
    ▼
Authorization Handler ──► if no permission: return 403; if ok: next()
    │
    ▼
Rate Limiter ──► if limit exceeded: return 429; if ok: next()
    │
    ▼
Request Logger ──► logs request; always calls next()
    │
    ▼
Your Handler (Controller) ──► processes request, returns response
```

```java
// The Handler interface
public abstract class RequestHandler {
    private RequestHandler next;

    // Fluent chaining: handler1.setNext(handler2).setNext(handler3)
    public RequestHandler setNext(RequestHandler next) {
        this.next = next;
        return next;
    }

    // Template method: handle or pass to next
    public HttpResponse handle(HttpRequest request) {
        if (next != null) {
            return next.handle(request);
        }
        return HttpResponse.notFound("No handler found for: " + request.getPath());
    }
}

// Concrete handlers
public class AuthenticationHandler extends RequestHandler {
    private final TokenService tokenService;

    public AuthenticationHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !tokenService.isValid(token)) {
            return HttpResponse.unauthorized("Invalid or missing token");
        }
        // Token is valid — enrich request and pass to next handler
        request.setAttribute("userId", tokenService.getUserId(token));
        return super.handle(request);  // pass to next
    }
}

public class RateLimitHandler extends RequestHandler {
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final int maxRequestsPerMinute;

    public RateLimitHandler(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String ip = request.getClientIp();
        int count = requestCounts.merge(ip, 1, Integer::sum);
        if (count > maxRequestsPerMinute) {
            return HttpResponse.tooManyRequests("Rate limit exceeded");
        }
        return super.handle(request);  // pass to next
    }
}

public class LoggingHandler extends RequestHandler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        long start = System.currentTimeMillis();
        HttpResponse response = super.handle(request);  // always passes to next
        long duration = System.currentTimeMillis() - start;
        System.out.printf("[LOG] %s %s → %d (%dms)%n",
            request.getMethod(), request.getPath(), response.getStatus(), duration);
        return response;
    }
}

public class ApiController extends RequestHandler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        // Actual business logic — terminal handler
        if (request.getPath().startsWith("/api/users")) {
            return handleUsersEndpoint(request);
        }
        return super.handle(request);  // 404 from base class
    }

    private HttpResponse handleUsersEndpoint(HttpRequest request) {
        return HttpResponse.ok("{\"users\": []}");
    }
}

// Wiring the chain
RequestHandler chain = new LoggingHandler();
chain
    .setNext(new AuthenticationHandler(tokenService))
    .setNext(new RateLimitHandler(100))
    .setNext(new ApiController());

// All requests enter at the top of the chain
HttpResponse response = chain.handle(incomingRequest);
```

### Approval Workflows

```java
public abstract class ApprovalHandler {
    private ApprovalHandler next;

    public void setNext(ApprovalHandler next) {
        this.next = next;
    }

    public abstract void approveExpense(ExpenseRequest expense);

    protected void passToNext(ExpenseRequest expense) {
        if (next != null) {
            next.approveExpense(expense);
        } else {
            System.out.println("Expense request denied — amount exceeds all approval limits");
        }
    }
}

public class TeamLeadApprover extends ApprovalHandler {
    private static final BigDecimal LIMIT = new BigDecimal("500");

    @Override
    public void approveExpense(ExpenseRequest expense) {
        if (expense.getAmount().compareTo(LIMIT) <= 0) {
            System.out.println("Team Lead approved: $" + expense.getAmount());
        } else {
            passToNext(expense);
        }
    }
}

public class ManagerApprover extends ApprovalHandler {
    private static final BigDecimal LIMIT = new BigDecimal("5000");

    @Override
    public void approveExpense(ExpenseRequest expense) {
        if (expense.getAmount().compareTo(LIMIT) <= 0) {
            System.out.println("Manager approved: $" + expense.getAmount());
        } else {
            passToNext(expense);
        }
    }
}

public class DirectorApprover extends ApprovalHandler {
    private static final BigDecimal LIMIT = new BigDecimal("50000");

    @Override
    public void approveExpense(ExpenseRequest expense) {
        if (expense.getAmount().compareTo(LIMIT) <= 0) {
            System.out.println("Director approved: $" + expense.getAmount());
        } else {
            passToNext(expense);
        }
    }
}

// Build and use the chain
TeamLeadApprover teamLead = new TeamLeadApprover();
ManagerApprover manager = new ManagerApprover();
DirectorApprover director = new DirectorApprover();

teamLead.setNext(manager);
manager.setNext(director);

teamLead.approveExpense(new ExpenseRequest(new BigDecimal("300")));   // Team Lead approves
teamLead.approveExpense(new ExpenseRequest(new BigDecimal("2000")));  // Manager approves
teamLead.approveExpense(new ExpenseRequest(new BigDecimal("20000"))); // Director approves
teamLead.approveExpense(new ExpenseRequest(new BigDecimal("100000"))); // Denied
```

---

## State — Objects That Change Their Own Behavior

**Intent:** Allow an object to alter its behavior when its internal state changes. The object will appear to change its class.

### The Problem: State Machines as if/else

```java
// BAD: An order's behavior depends on its state, leading to complex if/else
public class Order {
    private String status;  // "NEW", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"

    public void pay() {
        if (status.equals("NEW")) {
            status = "PAID";
        } else if (status.equals("PAID")) {
            throw new IllegalStateException("Already paid");
        } else if (status.equals("SHIPPED")) {
            throw new IllegalStateException("Cannot pay after shipping");
        } else if (status.equals("CANCELLED")) {
            throw new IllegalStateException("Cannot pay cancelled order");
        }
        // Grows with every new state and every new operation
    }

    public void ship() {
        if (status.equals("PAID")) {
            status = "SHIPPED";
        } else {
            throw new IllegalStateException("Can only ship paid orders, status was: " + status);
        }
    }
    // ...
}
```

This becomes unmaintainable. Every new state multiplies the if/else chains in every method.

### The State Pattern: Behavior Moves Into State Objects

```java
// Each state is an object that knows how to handle operations
public interface OrderState {
    void pay(OrderContext order);
    void ship(OrderContext order);
    void deliver(OrderContext order);
    void cancel(OrderContext order);
    String getName();
}

// The Context — delegates to current state
public class OrderContext {
    private OrderState currentState;
    private final String orderId;
    private String shippingTrackingId;

    public OrderContext(String orderId) {
        this.orderId = orderId;
        this.currentState = new NewOrderState();  // Initial state
    }

    // State management
    void setState(OrderState state) {
        System.out.println("Order " + orderId + ": " + currentState.getName()
            + " → " + state.getName());
        this.currentState = state;
    }

    OrderState getState() { return currentState; }

    // Delegating operations to the current state
    public void pay()     { currentState.pay(this); }
    public void ship()    { currentState.ship(this); }
    public void deliver() { currentState.deliver(this); }
    public void cancel()  { currentState.cancel(this); }

    // Accessors for state transitions
    public void setShippingTrackingId(String id) { this.shippingTrackingId = id; }
    public String getShippingTrackingId() { return shippingTrackingId; }
    public String getOrderId() { return orderId; }
}

// Concrete states
public class NewOrderState implements OrderState {
    @Override
    public void pay(OrderContext order) {
        System.out.println("Processing payment for order " + order.getOrderId());
        order.setState(new PaidOrderState());
    }

    @Override
    public void ship(OrderContext order) {
        throw new IllegalStateException("Cannot ship unpaid order");
    }

    @Override
    public void deliver(OrderContext order) {
        throw new IllegalStateException("Cannot deliver unpaid order");
    }

    @Override
    public void cancel(OrderContext order) {
        System.out.println("Order " + order.getOrderId() + " cancelled before payment");
        order.setState(new CancelledOrderState());
    }

    @Override
    public String getName() { return "NEW"; }
}

public class PaidOrderState implements OrderState {
    @Override
    public void pay(OrderContext order) {
        throw new IllegalStateException("Order already paid");
    }

    @Override
    public void ship(OrderContext order) {
        String trackingId = "TRACK-" + System.currentTimeMillis();
        order.setShippingTrackingId(trackingId);
        System.out.println("Order " + order.getOrderId() + " shipped, tracking: " + trackingId);
        order.setState(new ShippedOrderState());
    }

    @Override
    public void deliver(OrderContext order) {
        throw new IllegalStateException("Order has not been shipped yet");
    }

    @Override
    public void cancel(OrderContext order) {
        System.out.println("Order " + order.getOrderId() + " cancelled, issuing refund");
        order.setState(new CancelledOrderState());
    }

    @Override
    public String getName() { return "PAID"; }
}

public class ShippedOrderState implements OrderState {
    @Override
    public void pay(OrderContext order) {
        throw new IllegalStateException("Order already paid");
    }

    @Override
    public void ship(OrderContext order) {
        throw new IllegalStateException("Order already shipped");
    }

    @Override
    public void deliver(OrderContext order) {
        System.out.println("Order " + order.getOrderId() + " delivered successfully");
        order.setState(new DeliveredOrderState());
    }

    @Override
    public void cancel(OrderContext order) {
        throw new IllegalStateException("Cannot cancel shipped order — contact support");
    }

    @Override
    public String getName() { return "SHIPPED"; }
}

public class DeliveredOrderState implements OrderState {
    @Override
    public void pay(OrderContext order)    { throw new IllegalStateException("Order complete"); }
    @Override
    public void ship(OrderContext order)   { throw new IllegalStateException("Order complete"); }
    @Override
    public void deliver(OrderContext order){ throw new IllegalStateException("Already delivered"); }
    @Override
    public void cancel(OrderContext order) { throw new IllegalStateException("Cannot cancel delivered order"); }
    @Override
    public String getName() { return "DELIVERED"; }
}

public class CancelledOrderState implements OrderState {
    @Override
    public void pay(OrderContext order)    { throw new IllegalStateException("Order is cancelled"); }
    @Override
    public void ship(OrderContext order)   { throw new IllegalStateException("Order is cancelled"); }
    @Override
    public void deliver(OrderContext order){ throw new IllegalStateException("Order is cancelled"); }
    @Override
    public void cancel(OrderContext order) { System.out.println("Order already cancelled"); }
    @Override
    public String getName() { return "CANCELLED"; }
}

// Usage: clean, no if/else
OrderContext order = new OrderContext("ORD-1001");
order.pay();      // NEW → PAID
order.ship();     // PAID → SHIPPED
order.deliver();  // SHIPPED → DELIVERED
// order.cancel(); // throws IllegalStateException — cannot cancel delivered order
```

### The State Machine Visualized

```
┌─────────┐   pay()   ┌─────────┐  ship()  ┌──────────┐  deliver()  ┌───────────┐
│   NEW   │──────────►│  PAID   │─────────►│ SHIPPED  │────────────►│ DELIVERED │
└─────────┘           └─────────┘          └──────────┘             └───────────┘
    │                     │
    │ cancel()            │ cancel()
    ▼                     ▼
┌───────────┐      ┌───────────┐
│ CANCELLED │◄─────│ CANCELLED │
└───────────┘      └───────────┘
```

---

## Pattern Comparison Summary

```
Behavioral Patterns — Intent Summary:

┌──────────────────────────┬──────────────────────────────────────────────────────┐
│ Pattern                  │ Core Intent                                          │
├──────────────────────────┼──────────────────────────────────────────────────────┤
│ Observer                 │ Notify dependents automatically when state changes   │
│ Strategy                 │ Swap algorithms at runtime using composition         │
│ Command                  │ Encapsulate requests as objects; enable undo/redo    │
│ Template Method          │ Fix the algorithm structure; vary the steps          │
│ Chain of Responsibility  │ Pass requests along a handler chain                 │
│ State                    │ Object changes behavior based on internal state      │
└──────────────────────────┴──────────────────────────────────────────────────────┘
```

### Pattern Relationships

**Strategy vs. Template Method**: Both vary algorithm behavior. Strategy does it via *composition* (inject a different object); Template Method via *inheritance* (override a method in a subclass). Prefer Strategy for its looser coupling; use Template Method when subclasses need to call back into the parent's algorithm.

**Observer vs. Command**: Both handle events. Observer is about broadcasting state changes to unknown subscribers. Command is about representing specific user actions as objects that can be executed, queued, and undone.

**State vs. Strategy**: They have the same structure — a context delegates to a state/strategy object. The difference is *intent and lifecycle*: in State, the context transitions between states automatically, and states may know about each other; in Strategy, the client chooses the strategy explicitly, and strategies are independent.

**Chain of Responsibility vs. Decorator**: Both process requests sequentially through a chain of objects. Decorator adds behavior and always passes to the next; Chain of Responsibility may stop the chain by handling the request and not passing it on.

### The Whole Picture

```
All 23 Gang of Four Patterns organized by what they address:

OBJECT CREATION:
  Singleton  Factory Method  Abstract Factory  Builder  Prototype

OBJECT COMPOSITION (STRUCTURE):
  Decorator  Adapter  Facade  Proxy  Composite  Bridge  Flyweight

OBJECT COLLABORATION (BEHAVIOR):
  Observer  Strategy  Command  Template Method
  Chain of Responsibility  State  Iterator
  Mediator  Visitor  Memento  Interpreter
```

The six patterns in this chapter cover the most commonly used behavioral patterns. Master these, and you'll recognize them — and know when to apply them — throughout your career.
