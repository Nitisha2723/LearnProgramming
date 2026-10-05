# Chapter 7: OOP in the Real World

## From Theory to Production Systems

You've learned all four OOP pillars. Now let's see how professional engineers use them to model real systems. The same patterns appear everywhere — at Netflix, at banks, at Amazon. Understanding the patterns helps you design your own systems with confidence.

---

## How Netflix Models Its Domain

Netflix is essentially a software system managing:
- Users who have accounts and preferences
- Content (movies, shows) with metadata
- Subscriptions that grant access and determine quality
- Streaming sessions that track viewing
- Payments that keep subscriptions active

### Domain Model

```
USER                        CONTENT
┌─────────────────┐         ┌─────────────────────┐
│ userId: String  │         │ contentId: String   │
│ email: String   │         │ title: String       │
│ displayName     │         │ description: String │
│ profileImage    │         │ genre: List<String> │
│ watchHistory    │         │ releaseYear: int     │
├─────────────────┤         │ rating: String      │
│ getProfiles()   │         ├─────────────────────┤
│ switchProfile() │         │ getRecommendations()│
│ search()        │         └─────────────────────┘
└─────────────────┘                  ▲
                                    / \
                          ┌────────┘   └────────┐
                        Movie              TVShow
                   ┌─────────────┐    ┌──────────────┐
                   │ duration    │    │ seasons      │
                   │ director    │    │ episodes     │
                   │ cast        │    │ currentEp    │
                   └─────────────┘    └──────────────┘

SUBSCRIPTION                  PAYMENT
┌──────────────────┐          ┌──────────────────┐
│ planType: Enum   │          │ amount: double   │
│   BASIC          │          │ date: LocalDate  │
│   STANDARD       │          │ status: Enum     │
│   PREMIUM        │          ├──────────────────┤
│ maxScreens: int  │          │ process()        │
│ resolution: Enum │          └──────────────────┘
├──────────────────┤
│ isActive()       │
│ canStream4K()    │
│ getMaxScreens()  │
└──────────────────┘
```

### Key OOP Patterns in Netflix

```java
// Abstraction: Content is an abstract type
abstract class Content {
    protected String contentId;
    protected String title;
    protected List<String> genres;
    protected String maturityRating;
    
    public abstract int getDurationMinutes();
    public abstract String getContentType();
    
    public boolean isAppropriateFor(int userAge) {
        // Logic based on maturityRating
    }
}

// Inheritance: Specific content types extend Content
class Movie extends Content {
    private int durationMinutes;
    private String director;
    
    @Override
    public int getDurationMinutes() { return durationMinutes; }
    
    @Override
    public String getContentType() { return "Movie"; }
}

class TVShow extends Content {
    private List<Season> seasons;
    
    @Override
    public int getDurationMinutes() {
        // Sum of all episode durations
        return seasons.stream()
            .mapToInt(Season::getTotalDuration)
            .sum();
    }
    
    @Override
    public String getContentType() { return "TV Show"; }
    
    public int getEpisodeCount() {
        return seasons.stream().mapToInt(Season::getEpisodeCount).sum();
    }
}

// Interface: Streaming capability
interface Streamable {
    StreamSession startStream(VideoQuality quality);
    void pauseStream();
    void resumeStream();
    void stopStream();
}

// Polymorphism: Recommendation engine works with any Content
class RecommendationEngine {
    public List<Content> getRecommendations(User user, int limit) {
        // Returns mix of Movies, TVShows, Documentaries
        // All treated as Content — polymorphism!
        return contentRepository.findSimilarTo(user.getWatchHistory(), limit);
    }
}

// Encapsulation: User's subscription details are private
class User {
    private Subscription subscription;  // Private!
    
    public boolean canWatch(Content content) {
        return subscription.isActive() && 
               content.isAppropriateFor(getAge());
    }
    
    public boolean canStreamIn4K() {
        return subscription.supportsResolution(Resolution.UHD_4K);
    }
    // Users can never directly access subscription details
}
```

---

## How a Bank Models Its Domain

A bank manages:
- Customers with personal information
- Accounts (savings, checking, credit)
- Transactions (deposits, withdrawals, transfers)
- Cards linked to accounts
- Loans with repayment schedules

```
CUSTOMER                    ACCOUNT (abstract)
┌──────────────────┐        ┌─────────────────────┐
│ customerId       │        │ accountNumber       │
│ firstName        │        │ balance             │
│ lastName         │        │ openedDate          │
│ taxId (SSN)      │        │ customer: Customer  │
│ dateOfBirth      │─1─────>├─────────────────────┤
│ address          │  *     │ deposit()           │
├──────────────────┤        │ withdraw()          │
│ openAccount()    │        │ getBalance()        │
│ getAccounts()    │        │ getStatement()      │
│ applyForLoan()   │        └─────────────────────┘
└──────────────────┘                ▲
                                   / \
                     ┌────────────┘   └──────────────┐
              SavingsAccount              CheckingAccount
          ┌──────────────────┐         ┌──────────────────┐
          │ interestRate     │         │ overdraftLimit   │
          │ minimumBalance   │         │ monthlyFee       │
          ├──────────────────┤         ├──────────────────┤
          │ applyInterest()  │         │ applyFee()       │
          │ getApy()         │         │ isOverdrawn()    │
          └──────────────────┘         └──────────────────┘
```

### Key Banking Patterns

```java
// Abstract account with invariant enforcement
abstract class Account {
    private final String accountNumber;
    private final Customer owner;
    private double balance;
    private final List<Transaction> transactions = new ArrayList<>();
    
    protected Account(String accountNumber, Customer owner, double initialBalance) {
        if (initialBalance < getMinimumOpeningBalance()) {
            throw new IllegalArgumentException(
                "Minimum opening balance is " + getMinimumOpeningBalance());
        }
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = initialBalance;
        recordTransaction(new Transaction(TransactionType.INITIAL_DEPOSIT, initialBalance));
    }
    
    // Template method pattern
    public final void withdraw(double amount) {
        validateAmount(amount);
        if (!canWithdraw(amount)) {
            throw new InsufficientFundsException(balance, amount);
        }
        balance -= amount;
        recordTransaction(new Transaction(TransactionType.WITHDRAWAL, amount));
        onWithdrawal(amount);  // Hook for subclasses
    }
    
    // Subclasses define their own rules
    protected abstract boolean canWithdraw(double amount);
    protected abstract double getMinimumOpeningBalance();
    protected void onWithdrawal(double amount) { }  // Optional hook
    
    private void validateAmount(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
    }
}

class SavingsAccount extends Account {
    private final double interestRate;
    private static final double MINIMUM_BALANCE = 100.0;
    
    @Override
    protected boolean canWithdraw(double amount) {
        return getBalance() - amount >= MINIMUM_BALANCE;  // Must maintain minimum
    }
    
    @Override
    protected double getMinimumOpeningBalance() { return MINIMUM_BALANCE; }
    
    public void applyMonthlyInterest() {
        double interest = getBalance() * (interestRate / 12);
        deposit(interest);
    }
}

class CheckingAccount extends Account {
    private final double overdraftLimit;
    
    @Override
    protected boolean canWithdraw(double amount) {
        return getBalance() - amount >= -overdraftLimit;  // Can go negative up to limit
    }
    
    @Override
    protected double getMinimumOpeningBalance() { return 0.0; }
    
    @Override
    protected void onWithdrawal(double amount) {
        if (getBalance() < 0) {
            System.out.println("Account overdrawn. Fee applied.");
            // Apply overdraft fee
        }
    }
}

// Polymorphism: Transaction processing works for any account
class TransferService {
    public void transfer(Account from, Account to, double amount) {
        from.withdraw(amount);  // Works for SavingsAccount or CheckingAccount!
        to.deposit(amount);
    }
}
```

---

## How E-Commerce Models Its Domain

An e-commerce site like Amazon manages:
- Products with categories, pricing, inventory
- Customers with addresses and payment methods
- Shopping carts (temporary state)
- Orders (permanent records)
- Shipments tracking delivery

```java
// Interface hierarchy for products
interface Purchasable {
    double getPrice();
    boolean isInStock();
    void reserve(int quantity);
}

interface Reviewable {
    double getAverageRating();
    List<Review> getReviews();
    void addReview(Review review);
}

interface Shippable {
    double getWeight();
    Dimensions getDimensions();
    boolean requiresSpecialHandling();
}

// Product implements all relevant interfaces
abstract class Product implements Purchasable, Reviewable {
    private final String productId;
    private final String name;
    private final String description;
    private double price;
    private int stockQuantity;
    private final List<Review> reviews = new ArrayList<>();
    
    // Core product behavior
    @Override
    public double getPrice() { return price; }
    
    @Override
    public boolean isInStock() { return stockQuantity > 0; }
    
    @Override
    public void reserve(int quantity) {
        if (quantity > stockQuantity) {
            throw new InsufficientStockException(name, stockQuantity, quantity);
        }
        stockQuantity -= quantity;
    }
}

// Physical products are also Shippable
class PhysicalProduct extends Product implements Shippable {
    private double weightKg;
    private Dimensions dimensions;
    
    @Override
    public double getWeight() { return weightKg; }
    
    @Override
    public Dimensions getDimensions() { return dimensions; }
    
    @Override
    public boolean requiresSpecialHandling() { return weightKg > 30; }
}

// Digital products are not Shippable
class DigitalProduct extends Product {
    private String downloadUrl;
    private long fileSizeBytes;
    
    public String getDownloadLink(Customer customer) {
        // Generate secure, time-limited download link
        return generateSecureUrl(downloadUrl, customer.getCustomerId());
    }
}

// Shopping cart — temporary state
class ShoppingCart {
    private final String sessionId;
    private final Map<Product, Integer> items = new LinkedHashMap<>();
    
    public void addItem(Product product, int quantity) {
        if (!product.isInStock()) {
            throw new OutOfStockException(product.getName());
        }
        items.merge(product, quantity, Integer::sum);
    }
    
    public Order checkout(Customer customer, Payable paymentMethod) {
        double total = calculateTotal();
        
        // Reserve inventory for all items
        items.forEach((product, qty) -> product.reserve(qty));
        
        // Process payment
        paymentMethod.process(total);
        
        // Create permanent order record
        return new Order(customer, new HashMap<>(items), total, paymentMethod);
    }
    
    private double calculateTotal() {
        return items.entrySet().stream()
            .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
            .sum();
    }
}
```

---

## Domain Modeling Process: Requirements to Classes

The process of designing a class model from requirements:

### Step 1: Extract Nouns (Candidates for Classes)

From: "A library manages books. Members can check out up to 3 books at a time for 2 weeks. The librarian tracks overdue books and applies fines."

Nouns: Library, Book, Member, Librarian, Fine, Checkout, Return

### Step 2: Extract Verbs (Candidates for Methods)

Verbs: manages, check out, return, tracks, applies

### Step 3: Identify Relationships

```
Library ──has many──> Books
Member ──borrows──> Book (through Checkout)
Checkout ──generates──> Fine (if overdue)
Librarian ──manages──> Library
```

### Step 4: Identify Inheritance Hierarchies

```
Person (abstract)
├── Member
└── Librarian
```

### Step 5: Identify Interfaces

```
Searchable → Library, Catalog
Printable → Receipt, FineNotice
```

### Step 6: Draft Class Skeletons

```java
abstract class Person {
    protected String name;
    protected String email;
    protected String phone;
}

class Member extends Person {
    private String memberId;
    private List<Checkout> activeCheckouts;
    
    public boolean canCheckOut() {
        return activeCheckouts.size() < 3;
    }
}

class Book {
    private String isbn;
    private String title;
    private String author;
    private boolean checkedOut;
}

class Checkout {
    private Member member;
    private Book book;
    private LocalDate checkoutDate;
    private LocalDate dueDate;
    
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate);
    }
    
    public Fine generateFine() {
        if (!isOverdue()) return null;
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return new Fine(member, book, daysOverdue * 0.25);
    }
}
```

---

## CRC Cards Technique

Class-Responsibility-Collaborator cards are an old but effective design technique. You write them on index cards:

```
┌────────────────────────────────────────────────────────┐
│ CLASS: BankAccount                                     │
├──────────────────────────────┬─────────────────────────┤
│ RESPONSIBILITIES             │ COLLABORATORS           │
│                              │                         │
│ - Maintain balance           │ Customer                │
│ - Record transactions        │ Transaction             │
│ - Enforce business rules     │ TransactionHistory      │
│ - Calculate interest         │ NotificationService     │
│ - Notify on low balance      │                         │
└──────────────────────────────┴─────────────────────────┘
```

The process:
1. Write each class on a card
2. List what the class is responsible for (max 3-5 responsibilities)
3. List which other classes it works with
4. If a card is crowded → the class has too many responsibilities (split it!)

---

## Common OOP Anti-Patterns

### Anti-Pattern 1: God Class

A class that knows too much and does too much. Symptoms:
- More than ~500 lines
- More than 10-15 methods
- Contains unrelated functionality
- Named vaguely: "Manager", "Controller", "Handler"

```java
// BAD: God Class
class ApplicationManager {
    // Knows about EVERYTHING
    void createUser() { }
    void deleteUser() { }
    void sendEmail() { }
    void generateReport() { }
    void processPayment() { }
    void updateInventory() { }
    void calculateTax() { }
    void archiveOldRecords() { }
    // ... 50 more methods
}

// GOOD: Focused classes
class UserService { ... }
class EmailService { ... }
class ReportGenerator { ... }
class PaymentProcessor { ... }
class InventoryManager { ... }
class TaxCalculator { ... }
```

### Anti-Pattern 2: Anemic Domain Model

Classes that are just data containers with no behavior. All logic lives in "service" classes.

```java
// BAD: Anemic model — Order is just a data container
class Order {
    private List<OrderItem> items;
    private double total;
    private String status;
    
    // Just getters and setters — no behavior
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    // ...
}

// All logic in a service:
class OrderService {
    public double calculateTotal(Order order) {  // Logic that belongs in Order!
        return order.getItems().stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }
    
    public boolean canShip(Order order) {  // Also belongs in Order!
        return order.getStatus().equals("PAID");
    }
}

// GOOD: Rich domain model — Order contains its own logic
class Order {
    private List<OrderItem> items;
    private OrderStatus status;
    
    public double calculateTotal() {
        return items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }
    
    public boolean canShip() {
        return status == OrderStatus.PAID;
    }
    
    public void markAsShipped(String trackingNumber) {
        if (!canShip()) throw new IllegalStateException("Order is not paid");
        this.status = OrderStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
    }
}
```

### Anti-Pattern 3: Feature Envy

A method that uses more data from another class than from its own class.

```java
// BAD: OrderPrinter is envious of Order's data
class OrderPrinter {
    public void printOrder(Order order) {
        // This method knows TOO MUCH about Order internals
        System.out.println("Order: " + order.getOrderId());
        System.out.println("Customer: " + order.getCustomer().getName());
        System.out.println("Email: " + order.getCustomer().getEmail());
        System.out.println("Items: " + order.getItems().size());
        double total = 0;
        for (OrderItem item : order.getItems()) {
            total += item.getQuantity() * item.getUnitPrice();
        }
        System.out.println("Total: " + total);
    }
}

// GOOD: Give Order the ability to describe itself
class Order {
    public String getFormattedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order: ").append(orderId).append("\n");
        sb.append("Customer: ").append(customer.getName()).append("\n");
        sb.append("Total: ").append(String.format("%.2f", calculateTotal())).append("\n");
        return sb.toString();
    }
}

class OrderPrinter {
    public void printOrder(Order order) {
        System.out.println(order.getFormattedSummary());  // Simple!
    }
}
```

---

## Putting It All Together: Design Checklist

When designing a new system:

**Start with a domain vocabulary:**
- What are the main "things" in this domain? (classes)
- What do they do? (methods)
- What do they know? (fields)
- How do they relate? (associations, inheritance)

**Apply OOP principles:**
- [ ] Encapsulation: are fields private? Do methods enforce rules?
- [ ] Inheritance: is there a true "is-a" relationship? Consider composition instead.
- [ ] Polymorphism: can a parent type replace specific types in most places?
- [ ] Abstraction: is the public interface minimal and clear?

**Watch for smells:**
- [ ] Is any class doing too many unrelated things? (God class)
- [ ] Are classes just data with no behavior? (Anemic model)
- [ ] Are methods reaching into other classes for data? (Feature envy)
- [ ] Is inheritance going more than 3 levels deep? (Fragile hierarchy)

---

## Key Takeaways

1. Real systems combine all four OOP pillars together
2. Domain modeling starts with nouns (classes) and verbs (methods) from requirements
3. CRC cards help you think through class responsibilities before coding
4. Avoid God classes — split large classes by responsibility
5. Rich domain models keep behavior with data — avoid anemic models
6. Feature envy usually means a method belongs in a different class
7. The patterns you've learned are used at Netflix, Amazon, banks — everywhere

---

## What's Next

You now understand OOP theory. Time to write code! Move to the `code/` directory and work through the examples, then test your understanding with the `exercises/`. The `mini-project/` ties everything together in a complete, realistic application.

```
code/
├── basics/         ← BankAccount with full encapsulation
├── inheritance/    ← Animal hierarchy
└── interfaces/     ← Shapes with multiple interfaces

exercises/          ← Practice problems
mini-project/       ← School management system (full application)
```
