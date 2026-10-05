# Code Smells and Refactoring

> "Refactoring is a disciplined technique for restructuring an existing body of code, altering its internal structure without changing its external behavior."
> — Martin Fowler

Code smells are symptoms of poor design. Like a medical symptom, a smell is not a diagnosis — it is a signal that something might be wrong and warrants a closer look. Refactoring is the cure: a catalogue of small, safe transformations that improve the design without breaking the behaviour.

The two words that unlock this chapter: **without changing its external behavior**. Refactoring is only safe when backed by tests. A test suite is your safety net. You change the internal structure; the tests confirm the external contract is unchanged.

---

## Code Smells

### 1. Long Method

**Smell:** A method that has grown beyond its natural scope. The tell is when you need comments to separate sections inside a method, or when you scroll to see the whole thing.

Long methods hide complexity, accumulate unrelated responsibilities, and are nearly impossible to test at a fine-grained level.

```java
// BAD — one enormous method doing everything
public void generateMonthlyReport(int year, int month) throws Exception {
    // --- Fetch data ---
    List<Sale> sales = new ArrayList<>();
    Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
    PreparedStatement stmt = conn.prepareStatement(
        "SELECT * FROM sales WHERE YEAR(sale_date) = ? AND MONTH(sale_date) = ?");
    stmt.setInt(1, year);
    stmt.setInt(2, month);
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
        Sale sale = new Sale();
        sale.setId(rs.getLong("id"));
        sale.setAmount(rs.getDouble("amount"));
        sale.setProduct(rs.getString("product_name"));
        sales.add(sale);
    }

    // --- Calculate statistics ---
    double totalRevenue = 0;
    double highestSale = 0;
    String bestProduct = "";
    Map<String, Double> revenueByProduct = new HashMap<>();
    for (Sale sale : sales) {
        totalRevenue += sale.getAmount();
        if (sale.getAmount() > highestSale) {
            highestSale = sale.getAmount();
            bestProduct = sale.getProduct();
        }
        revenueByProduct.merge(sale.getProduct(), sale.getAmount(), Double::sum);
    }

    // --- Format and write report ---
    PrintWriter writer = new PrintWriter("report_" + year + "_" + month + ".txt");
    writer.printf("Monthly Report — %d/%02d%n", year, month);
    writer.printf("Total Revenue: $%.2f%n", totalRevenue);
    writer.printf("Best Product: %s ($%.2f)%n", bestProduct, highestSale);
    writer.println("\nRevenue by Product:");
    revenueByProduct.forEach((p, r) -> writer.printf("  %s: $%.2f%n", p, r));
    writer.flush();
    writer.close();
}
```

**Fix: Extract Method repeatedly until each section becomes its own focused function.**

```java
// GOOD — the top-level method reads like a summary
public void generateMonthlyReport(int year, int month) throws IOException {
    List<Sale> sales = fetchSalesForMonth(year, month);
    ReportStatistics stats = calculateStatistics(sales);
    writeReport(year, month, stats);
}

private List<Sale> fetchSalesForMonth(int year, int month) {
    return saleRepository.findByYearAndMonth(year, month);
}

private ReportStatistics calculateStatistics(List<Sale> sales) {
    double totalRevenue = sales.stream().mapToDouble(Sale::getAmount).sum();
    Map<String, Double> revenueByProduct = sales.stream()
        .collect(Collectors.groupingBy(Sale::getProduct, 
                 Collectors.summingDouble(Sale::getAmount)));
    String bestProduct = findBestProduct(revenueByProduct);
    return new ReportStatistics(totalRevenue, revenueByProduct, bestProduct);
}

private void writeReport(int year, int month, ReportStatistics stats) throws IOException {
    try (PrintWriter writer = new PrintWriter("report_" + year + "_" + month + ".txt")) {
        writer.printf("Monthly Report — %d/%02d%n", year, month);
        writer.printf("Total Revenue: $%.2f%n", stats.totalRevenue());
        writer.printf("Best Product: %s%n", stats.bestProduct());
        // ...
    }
}
```

---

### 2. Large Class (God Object)

**Smell:** A class that knows too much or does too much. It has absorbed responsibilities from across the system because it was convenient to add things there. The class becomes a gravitational centre — everything depends on it, and changing it is dangerous.

Warning signs: more than 500 lines, more than 20 methods, fields that are only used by a subset of methods, the class name contains "Manager", "Helper", "Util", or "Service" with a vague scope.

```java
// BAD — UserManager knows about authentication, persistence, email, 
// profile management, and reporting. It does everything.
public class UserManager {
    private Connection dbConnection;
    private SmtpClient emailClient;
    private FileStorage fileStorage;
    private AuditLogger auditLogger;
    
    public User createUser(String name, String email, String password) { ... }
    public void deleteUser(long id) { ... }
    public boolean authenticate(String email, String password) { ... }
    public String generateResetToken(String email) { ... }
    public void updateProfile(long userId, String name, String bio) { ... }
    public void uploadAvatar(long userId, byte[] imageData) { ... }
    public void sendWelcomeEmail(User user) { ... }
    public void sendPasswordResetEmail(String email, String token) { ... }
    public List<User> generateInactiveUsersReport() { ... }
    public void logUserActivity(long userId, String action) { ... }
    // ... 20 more methods
}
```

**Fix: Extract Class — split responsibilities into cohesive units.**

```java
// GOOD — each class has a single clear responsibility
public class UserRepository {
    public User save(User user) { ... }
    public Optional<User> findByEmail(String email) { ... }
    public void delete(long id) { ... }
}

public class AuthenticationService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    
    public boolean authenticate(String email, String password) { ... }
    public PasswordResetToken generateResetToken(String email) { ... }
}

public class UserProfileService {
    public void updateProfile(long userId, ProfileUpdate update) { ... }
    public void uploadAvatar(long userId, byte[] imageData) { ... }
}

public class UserNotificationService {
    public void sendWelcomeEmail(User user) { ... }
    public void sendPasswordResetEmail(String email, PasswordResetToken token) { ... }
}
```

---

### 3. Long Parameter List

**Smell:** A method that takes four or more parameters. Long parameter lists are hard to read, easy to call in the wrong order, and signal that parameters probably form a coherent concept that deserves its own class.

```java
// BAD — what is the order? What does each boolean mean?
public void createUserAccount(
    String firstName, String lastName, String email, 
    String phone, String country, String postalCode,
    boolean isAdmin, boolean isEmailVerified, boolean receivesNewsletter) { ... }

// Calling this is a nightmare:
createUserAccount("Alice", "Smith", "alice@example.com", 
                  "+1-555-0100", "US", "10001",
                  false, false, true);
```

**Fix: Introduce Parameter Object — group related parameters into a class.**

```java
// GOOD — the parameters have a name and structure
public record UserRegistrationRequest(
    String firstName,
    String lastName,
    String email,
    String phone,
    Address address,
    AccountSettings settings
) { }

public record AccountSettings(
    boolean isAdmin,
    boolean isEmailVerified,
    boolean receivesNewsletter
) { }

public void createUserAccount(UserRegistrationRequest request) { ... }

// Calling it is explicit and readable:
var request = new UserRegistrationRequest(
    "Alice", "Smith", "alice@example.com", "+1-555-0100",
    new Address("US", "10001"),
    new AccountSettings(false, false, true)
);
createUserAccount(request);
```

---

### 4. Duplicated Code

**Smell:** The same code structure appearing in multiple places. When you change it in one place, you must remember to change it everywhere. You will forget. This is where bugs live.

```java
// BAD — the same null check and string-building logic duplicated
public class CustomerReport {
    public String formatCustomerName(Customer customer) {
        if (customer == null) return "Unknown";
        String name = customer.getFirstName();
        if (customer.getLastName() != null) name += " " + customer.getLastName();
        return name.trim();
    }
}

public class InvoiceGenerator {
    public String getBillingName(Customer customer) {
        if (customer == null) return "Unknown";  // same
        String name = customer.getFirstName();   // same
        if (customer.getLastName() != null) name += " " + customer.getLastName();  // same
        return name.trim();  // same
    }
}
```

**Fix: Extract Method (shared), or Move Method to the class that owns the data.**

```java
// GOOD — the logic lives once, in the Customer class where it belongs
public class Customer {
    public String getFullName() {
        if (lastName == null || lastName.isBlank()) return firstName;
        return firstName + " " + lastName;
    }
}

// "Unknown" for null is a presentation concern — handle it in the caller
String name = customer != null ? customer.getFullName() : "Unknown";
```

---

### 5. Feature Envy

**Smell:** A method that is more interested in another class's data than its own. It calls many getters on another object to do its work. This is a strong hint that the method belongs in the other class.

```java
// BAD — TaxCalculator.calculateTax is deeply interested in Order's internals
public class TaxCalculator {
    public double calculateTax(Order order) {
        double subtotal = order.getSubtotal();
        String customerCountry = order.getCustomer().getAddress().getCountry();
        boolean isPremium = order.getCustomer().getLoyaltyTier() == LoyaltyTier.PREMIUM;
        double taxRate = lookupTaxRate(customerCountry);
        
        if (isPremium) taxRate *= 0.8;
        
        return subtotal * taxRate;
    }
}
```

The method `calculateTax` is doing almost nothing with `TaxCalculator`'s own data — it is reaching into `Order`, `Customer`, `Address`, and `LoyaltyTier`.

**Fix: Move Method to the class whose data it uses.**

```java
// GOOD — Order knows how to calculate its own tax
public class Order {
    public Money calculateTax() {
        TaxRate rate = taxService.lookupRate(customer.getCountry());
        TaxRate effectiveRate = customer.isPremiumMember() ? rate.withDiscount(0.2) : rate;
        return subtotal().multiply(effectiveRate.value());
    }
}
```

---

### 6. Primitive Obsession

**Smell:** Using primitive types (String, int, double) for concepts that deserve their own classes. Common examples: phone numbers as strings, money as doubles, email addresses as strings, status codes as integers.

```java
// BAD — these primitives carry hidden constraints
public class User {
    private String email;         // must match email format
    private String phoneNumber;   // must match phone format
    private double accountBalance; // must not be negative; has currency
    private String zipCode;        // country-specific format
    private int userStatus;        // only 0, 1, 2 are valid
}

// "any double" does not capture "a non-negative monetary value"
account.setBalance(-500.0);  // nothing prevents this
```

**Fix: Replace Primitive with Value Object.**

```java
// GOOD — each type enforces its own invariants
public final class Email {
    private final String value;
    
    public Email(String value) {
        if (!value.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[\\w]{2,}$"))
            throw new InvalidEmailException(value);
        this.value = value.toLowerCase();
    }
}

public final class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    public Money(BigDecimal amount, Currency currency) {
        if (amount.signum() < 0) throw new IllegalArgumentException("Amount cannot be negative");
        this.amount = amount;
        this.currency = Objects.requireNonNull(currency);
    }
}

public enum UserStatus { PENDING_VERIFICATION, ACTIVE, SUSPENDED, DEACTIVATED }

// Now the class is much cleaner
public class User {
    private Email email;
    private Money accountBalance;
    private UserStatus status;
}
```

---

### 7. Switch Statements

**Smell:** A switch statement (or a chain of if/else if) that dispatches on a type or status. These tend to be duplicated: the same switch appears in multiple methods to handle the same type discrimination.

```java
// BAD — the type-switching logic will be duplicated wherever behaviour varies
public double calculateShipping(Order order) {
    return switch (order.getCustomerType()) {
        case STANDARD   -> order.getWeight() * 2.5;
        case PREMIUM    -> order.getWeight() * 1.0;
        case ENTERPRISE -> 0.0;
        default         -> throw new IllegalStateException("Unknown type");
    };
}

// And again, elsewhere...
public String getServiceLevel(CustomerType type) {
    return switch (type) {
        case STANDARD   -> "5-7 business days";
        case PREMIUM    -> "2-3 business days";
        case ENTERPRISE -> "next day";
        default         -> throw new IllegalStateException("Unknown type");
    };
}
```

Every time a new `CustomerType` is added, every one of these switches must be found and updated.

**Fix: Replace Conditional with Polymorphism.**

```java
// GOOD — each customer type encapsulates its own shipping logic
public interface ShippingPolicy {
    double calculateCost(Order order);
    String getServiceLevel();
}

public class StandardShipping implements ShippingPolicy {
    @Override public double calculateCost(Order order) { return order.getWeight() * 2.5; }
    @Override public String getServiceLevel() { return "5-7 business days"; }
}

public class PremiumShipping implements ShippingPolicy {
    @Override public double calculateCost(Order order) { return order.getWeight() * 1.0; }
    @Override public String getServiceLevel() { return "2-3 business days"; }
}

public class EnterpriseShipping implements ShippingPolicy {
    @Override public double calculateCost(Order order) { return 0.0; }
    @Override public String getServiceLevel() { return "next day"; }
}

// The dispatch is now a simple delegation
public double calculateShipping(Order order) {
    return order.getShippingPolicy().calculateCost(order);
}
```

Adding a new customer type now means adding a new class, not finding and updating every switch.

---

### 8. Divergent Change and Shotgun Surgery

These are two opposite smells that both point to a cohesion problem.

**Divergent Change:** One class that needs to be changed for multiple different reasons. When a requirement changes, you have to make many unrelated modifications to the same class. "Every time we change the pricing rules, we also touch the reporting class, and also the discount engine."

**Fix: Split the class by the axis of change.** Create one class per reason-to-change.

**Shotgun Surgery:** A change that requires small modifications to many different classes. Every time you add a new payment method, you have to touch `PaymentController`, `PaymentProcessor`, `PaymentValidator`, `PaymentReporter`, `AuditLogger`, and `EmailService`.

**Fix: Move the scattered pieces back together.** The code that changes together belongs together.

```
Divergent Change:                    Shotgun Surgery:
One class ← many change reasons      One change reason → many classes

      ┌──────────┐                     ┌──────────┐
     ─►  Big     ◄─                    │ Change   │
      │  class   │                     └────┬─────┘
     ─►          ◄─                         │ forces changes in
      └──────────┘                    ┌─────┼─────┐
                                      ▼     ▼     ▼
                                   Class Class Class
                                   A     B     C
```

---

### 9. Data Clumps

**Smell:** Groups of data items that always appear together — in method parameters, in class fields, in local variables. If you find yourself deleting one item from the group and the group becomes meaningless, those items are a clump and deserve their own class.

```java
// BAD — street, city, state, zip always travel together
public void ship(String street, String city, String state, String zip) { ... }
public void validateAddress(String street, String city, String state, String zip) { ... }
public void formatAddress(String street, String city, String state, String zip) { ... }

class Order {
    String shippingStreet;
    String shippingCity;
    String shippingState;
    String shippingZip;
    
    String billingStreet;
    String billingCity;
    String billingState;
    String billingZip;
}
```

**Fix: Extract Class or Introduce Parameter Object.**

```java
// GOOD — Address is a first-class concept
public record Address(String street, String city, String state, String zip) {
    public String format() {
        return street + "\n" + city + ", " + state + " " + zip;
    }
}

class Order {
    Address shippingAddress;
    Address billingAddress;
}

public void ship(Address destination) { ... }
```

---

### 10. Dead Code

**Smell:** Code that is never executed. Unreachable code after a return statement. Methods that are never called. Branches of a switch that can never be reached. Parameters that are never used.

Dead code is a maintenance liability: readers try to understand it, wonder why it exists, and fear to delete it.

```java
// BAD — dead code in various forms
public class Calculator {
    
    // This method was replaced by newCalculate() but never deleted
    @Deprecated
    public double calculate(double a, double b, String op) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            default -> 0;
        };
    }
    
    public double divide(double a, double b) {
        if (b == 0) {
            return 0;  // unreachable — the check below throws
        }
        if (b == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return a / b;
    }
    
    // parameter 'logger' is never used
    public void process(double value, Logger logger) {
        System.out.println("Processing: " + value);
    }
}
```

**Fix: Delete it.** Version control keeps history. If dead code is "kept for reference", it belongs in a comment or a document, not in the live codebase.

---

## Refactoring Techniques

### 1. Extract Method

**When to apply:** The method is too long; a section of code has a comment above it explaining what it does; code is duplicated in several places.

**Mechanics:** Take the section of code, turn it into a method, and replace the original section with a call to the method. Name the method after what the code does, not how it does it.

```java
// BEFORE
public void printOwing(Order order) {
    Deque<Order> orders = new ArrayDeque<>();
    orders.push(order);
    
    // Print banner
    System.out.println("**************************");
    System.out.println("***** Customer Owes ******");
    System.out.println("**************************");
    
    // Calculate outstanding
    double outstanding = 0;
    for (Order o : orders) {
        outstanding += o.getAmount();
    }
    
    // Print details
    System.out.printf("Name: %s%n", order.getCustomer().getName());
    System.out.printf("Amount: $%.2f%n", outstanding);
}

// AFTER — each section is named, the top-level method reads as a summary
public void printOwing(Order order) {
    printBanner();
    double outstanding = calculateOutstanding(order);
    printDetails(order, outstanding);
}

private void printBanner() {
    System.out.println("**************************");
    System.out.println("***** Customer Owes ******");
    System.out.println("**************************");
}

private double calculateOutstanding(Order order) {
    return order.getAmount();  // simplified; real code might aggregate
}

private void printDetails(Order order, double outstanding) {
    System.out.printf("Name: %s%n", order.getCustomer().getName());
    System.out.printf("Amount: $%.2f%n", outstanding);
}
```

---

### 2. Extract Class

**When to apply:** A class is doing the work of two classes; there are fields and methods that only interact with each other (not with the rest of the class).

**Mechanics:** Create a new class. Move the relevant fields and methods. Replace the originals with delegation to the new class.

```java
// BEFORE — Person mixes personal info with telephony
public class Person {
    private String name;
    private String officeAreaCode;
    private String officeNumber;
    
    public String getTelephoneNumber() {
        return "(" + officeAreaCode + ") " + officeNumber;
    }
}

// AFTER — TelephoneNumber is its own first-class concept
public class TelephoneNumber {
    private final String areaCode;
    private final String number;
    
    public TelephoneNumber(String areaCode, String number) {
        this.areaCode = areaCode;
        this.number = number;
    }
    
    public String format() {
        return "(" + areaCode + ") " + number;
    }
}

public class Person {
    private String name;
    private TelephoneNumber officePhone;
    
    public String getTelephoneNumber() {
        return officePhone.format();
    }
}
```

---

### 3. Move Method

**When to apply:** A method uses more features of another class than its own (Feature Envy). Moving the method to the class it is most interested in reduces coupling and increases cohesion.

```java
// BEFORE — Account.overdraftCharge uses BankAccount's data exclusively
public class Account {
    private BankAccountType type;
    private int daysOverdrawn;
    
    public double overdraftCharge() {
        if (type.isPremium()) {
            double result = 10;
            if (daysOverdrawn > 7) result += (daysOverdrawn - 7) * 0.85;
            return result;
        } else {
            return daysOverdrawn * 1.75;
        }
    }
}

// AFTER — the charge calculation moves to BankAccountType where it belongs
public class BankAccountType {
    public double overdraftCharge(int daysOverdrawn) {
        if (isPremium()) {
            double result = 10;
            if (daysOverdrawn > 7) result += (daysOverdrawn - 7) * 0.85;
            return result;
        } else {
            return daysOverdrawn * 1.75;
        }
    }
}

public class Account {
    private BankAccountType type;
    private int daysOverdrawn;
    
    public double overdraftCharge() {
        return type.overdraftCharge(daysOverdrawn);
    }
}
```

---

### 4. Replace Conditional with Polymorphism

**When to apply:** You have a conditional that selects different behaviour based on the type of an object. This makes the code fragile: every new type requires a change to every conditional.

```java
// BEFORE — every new bird type requires updating this method
public class Bird {
    private BirdType type;
    private boolean isNailed;
    
    public double getSpeed() {
        return switch (type) {
            case EUROPEAN -> baseSpeed();
            case AFRICAN -> baseSpeed() - loadFactor() * numberOfCoconuts;
            case NORWEGIAN_BLUE -> isNailed ? 0 : baseSpeed() * voltage;
        };
    }
}

// AFTER — each bird type is its own class
public abstract class Bird {
    public abstract double getSpeed();
    protected double baseSpeed() { ... }
}

public class EuropeanBird extends Bird {
    @Override
    public double getSpeed() { return baseSpeed(); }
}

public class AfricanBird extends Bird {
    private int numberOfCoconuts;
    @Override
    public double getSpeed() { return baseSpeed() - loadFactor() * numberOfCoconuts; }
}

public class NorwegianBlueParrot extends Bird {
    private boolean isNailed;
    private double voltage;
    @Override
    public double getSpeed() { return isNailed ? 0 : baseSpeed() * voltage; }
}
```

Adding a new bird type is now entirely additive — no existing code needs to change.

---

### 5. Introduce Parameter Object

**When to apply:** Several parameters always travel together. They represent a concept that deserves a name.

This refactoring was covered under the "Long Parameter List" smell above. The mechanics:

1. Create a new class to represent the parameter group.
2. Update the method signature to take the new class.
3. Update all call sites.
4. Move any behaviour that belongs to the new class into it.

```java
// BEFORE — date range always travels as two parameters
public List<Invoice> findInvoicesInRange(Date startDate, Date endDate) { ... }
public List<Order> findOrdersInRange(Date startDate, Date endDate) { ... }
public double calculateRevenueInRange(Date startDate, Date endDate) { ... }

// AFTER — DateRange is a first-class concept
public record DateRange(LocalDate start, LocalDate end) {
    public DateRange {
        if (end.isBefore(start))
            throw new IllegalArgumentException("End date must not be before start date");
    }
    
    public boolean includes(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }
    
    public long numberOfDays() {
        return ChronoUnit.DAYS.between(start, end);
    }
}

public List<Invoice> findInvoicesInRange(DateRange period) { ... }
public List<Order> findOrdersInRange(DateRange period) { ... }
public double calculateRevenueInRange(DateRange period) { ... }
```

---

### 6. Replace Magic Numbers with Named Constants

**When to apply:** You see a literal number (or string) in the code whose meaning is not immediately obvious. Magic numbers make code hard to understand and change — if the value appears in five places, changing it requires finding all five.

```java
// BEFORE — what do these numbers mean?
if (password.length() < 8) {
    throw new ValidationException("Password too short");
}

double monthlyPayment = loanAmount * (0.045 / 12) / 
    (1 - Math.pow(1 + (0.045 / 12), -360));

if (user.getStatus() == 2) {
    sendPasswordResetEmail(user);
}
```

**After:**

```java
// AFTER — every magic number has a name
private static final int MINIMUM_PASSWORD_LENGTH = 8;
private static final double ANNUAL_INTEREST_RATE = 0.045;
private static final int LOAN_TERM_MONTHS = 360;
private static final int USER_STATUS_LOCKED = 2;

if (password.length() < MINIMUM_PASSWORD_LENGTH) {
    throw new ValidationException("Password must be at least " + 
        MINIMUM_PASSWORD_LENGTH + " characters");
}

double monthlyRate = ANNUAL_INTEREST_RATE / 12;
double monthlyPayment = loanAmount * monthlyRate /
    (1 - Math.pow(1 + monthlyRate, -LOAN_TERM_MONTHS));

if (user.getStatus() == USER_STATUS_LOCKED) {  // even better: use an enum
    sendPasswordResetEmail(user);
}
```

Better still, replace integer status codes with enums (see the Primitive Obsession section):

```java
public enum UserStatus { ACTIVE, LOCKED, DEACTIVATED }

if (user.getStatus() == UserStatus.LOCKED) {
    sendPasswordResetEmail(user);
}
```

---

## The Refactoring Process

Refactoring is only safe with tests. Here is the discipline:

```
┌─────────────────────────────────────────────────────────────┐
│  Before you refactor:                                       │
│  1. Ensure you have tests covering the code to be changed   │
│  2. Run the tests — they must all pass before you start     │
└─────────────────────────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│  Make ONE small change (rename, extract, move)              │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│  Run the tests                                              │
│  Green → commit and continue                                │
│  Red → undo the last change and diagnose                    │
└─────────────────────────────────────────────────────────────┘
```

Small, frequent commits are the key. A refactoring session with one hundred uncommitted changes and failing tests is dangerous. A refactoring session with ten commits of one change each, all green, is safe and reversible.

The most important tool is your IDE's built-in refactoring support. IntelliJ IDEA and Eclipse can rename, extract, and move with complete correctness guarantees — they update all references automatically and check for conflicts. Use these tools; don't do it manually.

---

## Summary

| Smell | Symptom | Refactoring |
|---|---|---|
| Long Method | Scrolling to read one method | Extract Method |
| Large Class | "Manager" class with 20+ methods | Extract Class |
| Long Parameter List | 4+ method parameters | Introduce Parameter Object |
| Duplicated Code | Same logic in multiple places | Extract Method, then share |
| Feature Envy | Method uses another class's data | Move Method |
| Primitive Obsession | Strings for email/phone/money | Replace with Value Object |
| Switch Statements | Same type-switching in many places | Replace with Polymorphism |
| Divergent Change | Class changes for many reasons | Extract Class per change axis |
| Shotgun Surgery | One change touches many classes | Move related code together |
| Data Clumps | Fields/params always appear together | Extract Class or Parameter Object |
| Dead Code | Methods/code paths never executed | Delete it |

Refactoring is not a one-time project. It is a continuous practice — the professional habit of leaving the code better than you found it, one small step at a time.
