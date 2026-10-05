# Clean Code

> "Any fool can write code that a computer can understand. Good programmers write code that humans can understand."
> — Martin Fowler

Clean code is not about aesthetics. It is not about tabs versus spaces or where you put your braces. It is about *communication*. Code is read far more often than it is written. The next person who reads your code may be a colleague, a future hire, or — most likely — yourself six months from now when you have forgotten everything.

This guide is deliberately opinionated. There are trade-offs in software engineering where reasonable people disagree. Clean code is not one of them. The principles here have been validated across decades of professional software development and codified by practitioners like Robert C. Martin (*Clean Code*), Martin Fowler (*Refactoring*), and Kent Beck (*Implementation Patterns*). Learn them, internalise them, then write code you are proud to show.

---

## Naming — The Most Impactful Thing You Can Do

The single highest-return investment in code quality is naming. Good names turn unreadable code into self-documenting prose. Bad names force every reader to perform mental decoding before they can understand anything.

### Names Should Reveal Intent

A name should answer three questions: why it exists, what it does, and how it is used. If a name requires a comment to explain it, the name is wrong.

```java
// BAD — what is d? Days since what?
int d;
long ts;
List<int[]> list1;

// GOOD — reveals intent completely
int daysSinceLastModification;
long orderTimestampMillis;
List<int[]> flaggedCells;
```

The savings multiply with scope. A loop variable `i` is acceptable in a three-line loop where everything is visible. A field named `i` in a class with twenty methods is a maintenance catastrophe.

```java
// BAD — what does this do?
public List<int[]> getThem() {
    List<int[]> list1 = new ArrayList<>();
    for (int[] x : theList)
        if (x[0] == 4)
            list1.add(x);
    return list1;
}

// GOOD — same logic, reads like English
public List<Cell> getFlaggedCells() {
    List<Cell> flaggedCells = new ArrayList<>();
    for (Cell cell : gameBoard)
        if (cell.isFlagged())
            flaggedCells.add(cell);
    return flaggedCells;
}
```

The second version requires zero context to understand. The first requires knowing the structure of `theList`, knowing that index 0 is a status field, knowing that status 4 means "flagged". All of that tribal knowledge is hidden in the name `x[0] == 4`.

### Avoid Disinformation

Do not use names that lie, mislead, or confuse. This is more common than it sounds.

```java
// BAD — accountList is not a List, it's a Map
Map<String, Account> accountList = new HashMap<>();

// GOOD
Map<String, Account> accountsByCustomerId = new HashMap<>();

// BAD — what distinguishes these?
String getActiveAccount()
String getActiveAccounts()
String getActiveAccountInfo()

// GOOD — distinct meaning, distinct name
Account getActiveAccount()
List<Account> getAllActiveAccounts()
AccountSummary buildActiveAccountSummary()
```

Also avoid "noise words" — meaningless qualifiers that add length without adding information.

```java
// BAD — what is the difference between these three?
ProductInfo productInfo;
ProductData productData;
Product product;

// Usually there is none. Pick one. If there is a real distinction, name it explicitly.
Product product;
ProductSearchResult searchResult;
ProductCatalogView catalogView;
```

### Names Should Be Pronounceable

If you cannot say a name aloud, you cannot discuss it with colleagues.

```java
// BAD — try saying this in a code review
class DtaRcrd102 {
    private Date genymdhms;
    private Date modymdhms;
    private final String pszqint = "102";
}

// GOOD
class Customer {
    private Date generationTimestamp;
    private Date modificationTimestamp;
    private final String recordId = "102";
}
```

### Names Should Be Searchable

Single-letter names and numeric literals appear everywhere in a codebase. When you need to find where a concept is used, you cannot search for `e` or `7`.

```java
// BAD — how do you find all places that use a five-day work week?
for (int j = 0; j < 34; j++) {
    s += (t[j] * 4) / 5;
}

// GOOD — every concept has a name you can grep for
int realDaysPerIdealDay = 4;
final int WORK_DAYS_PER_WEEK = 5;
int sum = 0;
for (int j = 0; j < NUMBER_OF_TASKS; j++) {
    int realTaskDays = taskEstimate[j] * realDaysPerIdealDay;
    int realTaskWeeks = realTaskDays / WORK_DAYS_PER_WEEK;
    sum += realTaskWeeks;
}
```

### Classes Are Nouns, Methods Are Verbs

This rule is simple, universal, and enforced by convention across every major language ecosystem.

```java
// Classes: noun phrases
class Customer { }
class WikiPage { }
class HtmlParser { }
class AccountRepository { }

// Methods: verb phrases
account.getBalance();
order.save();
emailValidator.isValid(email);
fileParser.parse(filePath);
```

Constructor overloads should have factory methods with names that describe their argument:

```java
// BAD — what does this complex constructor do?
Complex c = new Complex(23.4);

// GOOD — the name explains the construction
Complex c = Complex.fromRealNumber(23.4);
```

### Abandon Hungarian Notation

Hungarian notation prefixes variable names with a type indicator: `strName`, `iCount`, `bIsActive`. It was invented in the 1970s for weakly-typed languages. Modern IDEs tell you the type on hover. Adding type prefixes creates noise without value and forces renaming when types change.

```java
// BAD — Hungarian notation
String strCustomerName;
int iOrderCount;
boolean bIsProcessed;
IUserRepository iUserRepository;

// GOOD
String customerName;
int orderCount;
boolean processed;
UserRepository userRepository;
```

Interface names especially should not begin with `I`. If you must distinguish the interface from its implementation, make the implementation's name concrete: `UserRepository` (interface) and `JpaUserRepository` or `InMemoryUserRepository` (implementations).

### Be Consistent: Pick a Word and Stick to It

Using multiple synonyms for the same concept in a codebase is maddening.

```java
// BAD — three words for the same operation
CustomerService.fetchCustomer(id);
OrderService.getOrder(id);
ProductService.retrieveProduct(id);

// GOOD — one word, used everywhere
CustomerService.findCustomer(id);
OrderService.findOrder(id);
ProductService.findProduct(id);
```

Likewise, do not use the same word for two different concepts:

```java
// BAD — "add" means two different things
void add(Element element);       // adds to a collection
void add(int a, int b);          // arithmetic addition

// GOOD — use different words for different concepts
void append(Element element);
int sum(int a, int b);
```

Document your vocabulary in a team glossary. When a new word is needed, discuss and agree before it proliferates across the codebase.

### The Newspaper Metaphor for Naming

A well-written newspaper article starts with the headline (highest-level summary), then the first paragraph (key facts), then supporting details. Readers can stop at any level and still understand the story.

Apply this to code organisation within a class. High-level methods appear first; the detail methods they call appear below. A reader who wants only the summary reads the top; a reader who wants to understand the implementation reads down.

```
ClassName
  publicHighLevelMethod()     ← reads like a headline
    privateStepOne()          ← first-level detail
      privateHelperForStep1() ← second-level detail
    privateStepTwo()
    privateStepThree()
```

This "Step-Down Rule" (discussed further under Functions) means you always put the most important things first.

---

## Functions

Functions are the primary unit of organisation in Java. A well-written function does exactly one thing, communicates clearly what that thing is, and contains no surprises.

### Small!

The first rule of functions is that they should be small. The second rule is that they should be *smaller than that*.

How small? A function should rarely exceed 20 lines. Functions of 5–10 lines are common in clean code. If your function is approaching 50 lines, it is almost certainly doing too many things.

```java
// BAD — 40+ lines doing everything
public void processPayment(Order order) {
    // validate
    if (order == null) throw new IllegalArgumentException("...");
    if (order.getItems().isEmpty()) throw new IllegalArgumentException("...");
    if (order.getCustomer() == null) throw new IllegalArgumentException("...");
    
    // calculate total
    double total = 0;
    for (OrderItem item : order.getItems()) {
        total += item.getPrice() * item.getQuantity();
        if (item.isDiscounted()) {
            total -= item.getDiscount();
        }
    }
    
    // charge card
    CreditCardGateway gateway = new CreditCardGateway();
    gateway.connect("https://api.payments.com");
    boolean charged = gateway.charge(order.getCustomer().getCreditCard(), total);
    
    // send confirmation
    if (charged) {
        EmailService email = new EmailService();
        email.sendConfirmation(order.getCustomer().getEmail(), order.getId(), total);
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }
}

// GOOD — each method is small and focused
public void processPayment(Order order) {
    validateOrder(order);
    double total = calculateOrderTotal(order);
    chargeCustomer(order.getCustomer(), total);
    confirmPayment(order, total);
}

private void validateOrder(Order order) {
    Objects.requireNonNull(order, "Order must not be null");
    if (order.getItems().isEmpty())
        throw new IllegalArgumentException("Order must have at least one item");
    Objects.requireNonNull(order.getCustomer(), "Order must have a customer");
}

private double calculateOrderTotal(Order order) {
    return order.getItems().stream()
        .mapToDouble(this::calculateItemPrice)
        .sum();
}

private double calculateItemPrice(OrderItem item) {
    double price = item.getPrice() * item.getQuantity();
    return item.isDiscounted() ? price - item.getDiscount() : price;
}

private void chargeCustomer(Customer customer, double amount) {
    paymentGateway.charge(customer.getCreditCard(), amount);
}

private void confirmPayment(Order order, double total) {
    emailService.sendConfirmation(order.getCustomer().getEmail(), order.getId(), total);
    order.setStatus(OrderStatus.PAID);
    orderRepository.save(order);
}
```

The second version has *more* lines total, but each function is immediately understandable. `processPayment` reads like a summary of four steps. If something goes wrong, you know exactly which step to investigate.

### Do One Thing

A function should do one thing. It should do it well. It should do it only.

The challenge is defining "one thing". A function that validates, then transforms, then persists is doing three things. The test: if you can meaningfully extract any part into a function with a different name, the original was doing more than one thing.

```java
// BAD — three things: checking the session, initialising the page, rendering the HTML
public String renderPage(PageData pageData) throws Exception {
    boolean isTestPage = pageData.hasAttribute("Test");
    if (isTestPage) {
        includeSetupAndTeardownPages(pageData, false);
    }
    return pageData.getHtml();
}

// GOOD — one thing: determine whether this is a test page and render it appropriately
public String renderPageWithSetupsAndTeardowns(PageData pageData) throws Exception {
    if (isTestPage(pageData))
        includeSetupAndTeardownPages(pageData);
    return pageData.getHtml();
}
```

### One Level of Abstraction Per Function

Each function should operate at a single level of abstraction. Mixing high-level policy (`parseHtml()`) with low-level detail (`buffer.append('\n')`) in the same function is disorienting.

```java
// BAD — mixes high-level parsing with low-level string operations
public void parseHtmlDocument(String html) {
    String title = extractTitle(html);
    // suddenly drops to character-level detail
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < html.length(); i++) {
        char c = html.charAt(i);
        if (c == '<') {
            // ... tag parsing ...
        }
    }
    document.setTitle(title);
    document.setBody(sb.toString());
}

// GOOD — stays at one level of abstraction
public void parseHtmlDocument(String html) {
    document.setTitle(extractTitle(html));
    document.setBody(extractBody(html));
}
```

### No Side Effects

A function's signature is a contract. If a function named `checkPassword` also initialises the user session, it is lying. Side effects create coupling and bugs that are extraordinarily difficult to track down.

Command-Query Separation (CQS) is a principle that helps here: a function should either *do something* (a command) or *answer something* (a query), but not both.

```java
// BAD — the name says "check" but the function also modifies state
public boolean checkPassword(String username, String password) {
    User user = userRepository.findByUsername(username);
    if (user == null) return false;
    if (!cryptographer.check(password, user.getPasswordHash())) return false;
    // Side effect hidden at the end!
    Session.initialize(user);
    return true;
}

// GOOD — separate concerns
public boolean isValidPassword(String username, String password) {
    User user = userRepository.findByUsername(username);
    if (user == null) return false;
    return cryptographer.check(password, user.getPasswordHash());
}

public void initializeSession(String username) {
    User user = userRepository.findByUsername(username);
    Session.initialize(user);
}

// Caller is explicit:
if (authService.isValidPassword(username, password)) {
    sessionManager.initializeSession(username);
}
```

### Parameters: Fewer Is Better

The ideal number of function arguments is zero. One is fine. Two is acceptable. Three requires justification. Four or more is almost always a design problem.

Each parameter is a concept the reader must hold in their head while reading the function. Parameters also multiply test cases: a function with two boolean parameters requires at least four tests to cover the combinations.

```java
// BAD — six parameters, impossible to remember the order
void createUser(String firstName, String lastName, String email,
                String phone, String address, boolean isAdmin) { ... }

// BAD — easy to swap arguments in the wrong order
Point p = new Point(3.4, 2.7);  // is it (x, y) or (lat, lng)?

// GOOD — wrap related parameters in an object
void createUser(UserRegistrationRequest request) { ... }

Point p = Point.fromCoordinates(x, y);
// or
GeoLocation loc = GeoLocation.fromLatLng(latitude, longitude);
```

### Don't Use Boolean Flags — They Signal Two Functions

A boolean parameter is almost always a sign that a function does two different things.

```java
// BAD — the boolean flag means this function does two things
render(pageData, true);   // what does true mean?
render(pageData, false);  // and false?

// Also bad — verbose but still confusing
renderForSuite(pageData, /* includeSetups= */ true);

// GOOD — two explicit functions
renderWithSetupsAndTeardowns(pageData);
renderWithoutSetupsAndTeardowns(pageData);
```

### The Step-Down Rule

Code should read like a top-down narrative. Each function is followed by the functions it calls, which are in turn followed by the functions *they* call. The reader can scan from top to bottom, reading the story at whatever level of detail they need.

```java
public class ReportGenerator {

    // Level 1 — the headline
    public Report generateMonthlySalesReport(Month month) {
        List<Sale> sales = fetchSalesForMonth(month);
        SaleSummary summary = summarizeSales(sales);
        return buildReport(month, summary);
    }

    // Level 2 — first-level detail
    private List<Sale> fetchSalesForMonth(Month month) {
        return saleRepository.findByMonthAndYear(month.getMonth(), month.getYear());
    }

    private SaleSummary summarizeSales(List<Sale> sales) {
        double totalRevenue = calculateTotalRevenue(sales);
        int totalUnits = countTotalUnits(sales);
        Sale bestSeller = findBestSellingProduct(sales);
        return new SaleSummary(totalRevenue, totalUnits, bestSeller);
    }

    private Report buildReport(Month month, SaleSummary summary) {
        return Report.builder()
            .title("Monthly Sales — " + month)
            .summary(summary)
            .generatedAt(Instant.now())
            .build();
    }

    // Level 3 — second-level detail
    private double calculateTotalRevenue(List<Sale> sales) {
        return sales.stream().mapToDouble(Sale::getAmount).sum();
    }

    private int countTotalUnits(List<Sale> sales) {
        return sales.stream().mapToInt(Sale::getQuantity).sum();
    }

    private Sale findBestSellingProduct(List<Sale> sales) {
        return sales.stream()
            .max(Comparator.comparingInt(Sale::getQuantity))
            .orElseThrow(() -> new IllegalStateException("No sales found"));
    }
}
```

The reader who only wants to know *what* `generateMonthlySalesReport` does reads six lines and is done. The reader who wants to know *how* it summarises sales reads five more lines. Each level of detail is available on demand, in the right order.

---

## Comments

Here is the uncomfortable truth about comments: **most comments are failures**. They are failures to write code that is clear enough to stand alone.

This does not mean all comments are bad. It means the first question when you reach for a comment should be: "Can I write the code so this comment is unnecessary?"

### The Best Comment Is No Comment

```java
// BAD — comment merely restates the code
// Check if employee is eligible for full benefits
if ((employee.flags & HOURLY_FLAG) && (employee.age > 65))

// GOOD — the code itself explains
if (employee.isEligibleForFullBenefits())
```

```java
// BAD — this comment will inevitably go out of date
// Returns the kth fibonacci number (uses memoization for performance)
public int fib(int k) {
    // ... implementation ...
}

// GOOD — the name says what, but a comment explains the non-obvious why
/**
 * Returns the kth Fibonacci number.
 *
 * Uses memoization to avoid exponential recomputation. Cache is intentionally
 * unbounded — callers are expected to use small k (< 50). For large k,
 * use {@link #fibIterative(int)} instead.
 */
public int fib(int k) { ... }
```

### Good Comments: Why, Not What

When a comment is warranted, it should explain *why* the code does something unusual, not *what* it does (the code already shows that).

**Legal comments** — required by licensing or policy.

```java
// Copyright (c) 2024 Acme Corp. All rights reserved.
// Licensed under the MIT License — see LICENSE file in project root.
```

**Warning of consequences** — when the cost of misuse is non-obvious.

```java
// WARNING: Do not call this from a test that runs in parallel.
// The legacy PaymentGateway uses a static lock that serialises
// all calls globally. Parallel tests will deadlock.
@SuppressWarnings("deprecation")
public Receipt chargeViaLegacyGateway(CreditCard card, Money amount) { ... }
```

**Explanation of intent** — why a particular decision was made.

```java
// We intentionally sort by insertion order, not alphabetically.
// The first address is the billing address and must appear first
// for the invoice PDF renderer (INVOICE-1042).
addresses.sort(Comparator.comparingInt(Address::getInsertionOrder));
```

**Amplification** — calling attention to something that looks minor but is not.

```java
String listItemContent = match.group(3).trim();
// The trim() here is critical. The leading whitespace in the group
// causes later parsing to fail for list items that begin with a tab.
```

**TODO comments** — temporary, with a ticket reference so they don't live forever.

```java
// TODO(PROJ-1234): Replace with the new AuthService once it ships in 2.4.
authToken = legacyAuthClient.authenticate(credentials);
```

### Bad Comments

**Redundant comments** — says what the code already says.

```java
// BAD
/** The name of the customer. */
private String customerName;

/** Returns the customer name. */
public String getCustomerName() {
    return customerName;
}
```

**Misleading comments** — a comment that is subtly wrong is worse than no comment.

```java
// BAD — the comment says "blocks until closed" but the code does not block
// This method closes the connection. Blocks until the connection is fully closed.
public void close() {
    this.closing = true;  // just sets a flag, nothing blocks
}
```

**Journal comments** — version control already tracks this. Do not use code files as a changelog.

```java
// BAD
// 2024-01-15 Added null check — jsmith
// 2024-03-02 Refactored to use streams — kdavis
// 2024-07-18 Fixed edge case when list is empty — jsmith
public List<String> getNames() { ... }
```

**Noise comments** — comments that add no information.

```java
// BAD
/** Default constructor. */
public Customer() { }

/** The day of the month. */
private int dayOfMonth;

// Catch the exception
} catch (Exception e) {
    // do nothing
}
```

**Commented-out code** — delete it. Version control will keep it forever.

```java
// BAD — why is this here? Is it coming back? Is it a reference?
// old implementation:
// doFoo();
// doBar();
// doBaz();
doNewThing();
```

---

## Error Handling

Error handling is a part of the code, not a special case bolted on afterwards. Done badly, it swamps the real logic. Done well, it is invisible.

### Use Exceptions, Not Return Codes

Return codes require callers to check the return value of every call. This is easy to forget and creates cluttered call sites.

```java
// BAD — caller must check the error code
DeviceResponse response = handle.sendShutdown();
if (response == DeviceResponse.OK) {
    DeviceController.pauseSending();
    // ...
} else {
    logger.error("Shutdown error: " + DeviceResponse.INVALID_RESPONSE);
}

// GOOD — exception path is separate from the happy path
try {
    handle.sendShutdown();
    DeviceController.pauseSending();
    // ...
} catch (DeviceShutdownException e) {
    logger.error("Shutdown failed", e);
}
```

### Create Informative Error Messages

An exception message is documentation for whoever is debugging at 2 AM.

```java
// BAD — tells you nothing useful
throw new RuntimeException("Error");
throw new IllegalArgumentException("Invalid input");

// GOOD — tells you what went wrong, where, and why
throw new OrderNotFoundException(
    "No order found with ID " + orderId + " for customer " + customerId);

throw new PaymentDeclinedException(
    "Payment of " + amount + " declined for card ending " + lastFourDigits +
    " — reason: " + declineReason);
```

Always include the erroneous value in the message. Always include context that identifies the operation. Always log the full stack trace, not just the message.

### Don't Return Null

Returning null forces every caller to add a null check. Inevitably someone forgets, and you get a `NullPointerException` at the worst possible time.

```java
// BAD — callers must null-check
public Employee getEmployee(String id) {
    // returns null if not found
}

// at the call site:
Employee employee = repo.getEmployee(id);
if (employee != null) {
    totalPay += employee.calculatePay();  // NPE waiting to happen if you forget
}

// GOOD — return Optional, throw an exception, or return a neutral value
public Optional<Employee> findEmployee(String id) { ... }

public Employee getEmployee(String id) {
    Employee e = repo.findById(id);
    if (e == null) throw new EmployeeNotFoundException(id);
    return e;
}

// For lists, return an empty list, never null
public List<Employee> getEmployeesInDepartment(String department) {
    List<Employee> employees = repo.findByDepartment(department);
    return employees != null ? employees : Collections.emptyList();
}
```

### Don't Pass Null

Similarly, passing null as a method argument is an invitation to a `NullPointerException`.

```java
// BAD — null is a valid argument here, according to the signature
calculator.xProjection(null, new Point(12, 13));

// GOOD — document the contract, validate, and fail fast
public double xProjection(Point p1, Point p2) {
    Objects.requireNonNull(p1, "p1 must not be null");
    Objects.requireNonNull(p2, "p2 must not be null");
    return (p2.x - p1.x) * 1.5;
}
```

Use `@NotNull`/`@NonNull` annotations from your IDE or static analysis tool to make the contract visible in signatures.

### Fail Fast

Detect problems as early as possible, at the point where the invalid data is first encountered, not five stack frames later when the symptoms appear.

```java
// BAD — the NPE manifests far from where null was set
public void processOrder(Order order) {
    // does not check order...
    for (OrderItem item : order.getItems()) {
        // NPE here, but the real bug is ten methods up the stack
    }
}

// GOOD — fail immediately with a useful message
public void processOrder(Order order) {
    Objects.requireNonNull(order, "Cannot process a null order");
    if (order.getItems().isEmpty())
        throw new InvalidOrderException("Order " + order.getId() + " has no items");
    // now proceed with confidence
}
```

---

## Classes

The same principles that apply to functions apply to classes, scaled up. A class is a collection of functions that share a common purpose.

### Small! Measure by Responsibilities

Classes should be small — but the measure is *responsibilities*, not lines of code. A class should have one reason to change. This is the Single Responsibility Principle (covered in the SOLID theory file), restated for classes.

Ask: "What is this class responsible for?" If your answer requires "and", the class probably does too much.

```java
// BAD — UserManager does too many things
public class UserManager {
    public void createUser(User user) { ... }
    public void deleteUser(long userId) { ... }
    public void sendWelcomeEmail(User user) { ... }
    public void generatePasswordResetToken(String email) { ... }
    public void validatePassword(String password) { ... }
    public void logUserAction(User user, String action) { ... }
    public void exportUsersToCSV() { ... }
}

// GOOD — each class has a single clear responsibility
public class UserRepository {
    public void save(User user) { ... }
    public void delete(long userId) { ... }
    public Optional<User> findById(long userId) { ... }
}

public class UserOnboardingService {
    public void onboardNewUser(User user) { ... }  // sends email, sets up profile
}

public class PasswordService {
    public boolean isValid(String password) { ... }
    public String generateResetToken(String email) { ... }
}

public class UserAuditLogger {
    public void log(User user, UserAction action) { ... }
}
```

### High Cohesion

A cohesive class is one where all of its instance variables are used by most of its methods. Low cohesion is a warning sign that a class should be split.

```java
// LOW COHESION — methods use disjoint subsets of the fields
class Employee {
    String name;
    String email;
    double hourlyRate;
    int hoursWorked;
    String reportFormat;
    String reportDestination;
    
    // Only uses name and email
    public void sendWelcomeEmail() { ... }
    
    // Only uses hourlyRate and hoursWorked
    public double calculatePay() { ... }
    
    // Only uses reportFormat and reportDestination
    public void generateReport() { ... }
}

// HIGH COHESION — each class uses all its fields
class Employee {
    String name;
    String email;
    double hourlyRate;
    int hoursWorked;
    
    public double calculatePay() { ... }
    public String getSummary() { ... }
}

class EmployeeReporter {
    String format;
    String destination;
    
    public void report(Employee employee) { ... }
}
```

### Minimal Coupling

Coupling is the degree to which one class depends on another. High coupling means a change in one class forces changes in others. Aim for loose coupling: classes should know as little as possible about each other.

The Law of Demeter is a useful heuristic: a method should only call methods on:
- itself
- its own fields
- objects passed as parameters
- objects it creates

```java
// BAD — reaches deep into the object graph (train wreck)
String street = order.getCustomer().getAddress().getStreet();
double discount = order.getCustomer().getLoyaltyAccount().getDiscount().getPercentage();

// GOOD — each class handles its own data
String street = order.getShippingStreet();  // Order delegates to Customer
double discountPct = order.getApplicableDiscountPercentage();  // Order delegates
```

### The Newspaper Metaphor for Class Organisation

Organise a class like a newspaper article: the most important things first, details below.

```
public class OrderProcessor {

    // 1. Public constants
    public static final int MAX_RETRIES = 3;

    // 2. Private fields
    private final OrderRepository repository;
    private final PaymentGateway paymentGateway;

    // 3. Constructors
    public OrderProcessor(...) { ... }

    // 4. Public methods (the interface — most important)
    public OrderConfirmation process(Order order) { ... }
    public void cancel(long orderId) { ... }

    // 5. Private helper methods (implementation detail)
    private void validate(Order order) { ... }
    private Receipt charge(Order order) { ... }
    private void notifyCustomer(Order order) { ... }
}
```

---

## YAGNI, DRY, and KISS

These three acronyms represent foundational principles of simple, maintainable design. They are often cited but rarely understood deeply.

### YAGNI — You Aren't Gonna Need It

Write code for the requirements you have now, not the requirements you imagine might appear in the future.

Speculative generality is one of the most common mistakes of intermediate developers. You add a plugin system because "we might want to support multiple implementations later". You add a configuration layer because "the requirements might change". You add an abstraction because "what if we need to swap out the database".

```java
// BAD — the "flexible" version nobody asked for
public interface DataExporter<T, F extends ExportFormat, S extends ExportStrategy> {
    ExportResult<T> export(Collection<T> items, F format, S strategy, ExportContext ctx);
}

// GOOD — the actual requirement: export orders to CSV
public class OrderCsvExporter {
    public void exportToCsv(List<Order> orders, Path outputPath) throws IOException { ... }
}
```

YAGNI does not mean "never abstract". It means wait until the second or third occurrence before abstracting. Premature abstraction is as harmful as premature optimisation.

### DRY — Don't Repeat Yourself

"Every piece of knowledge must have a single, unambiguous, authoritative representation within a system."

The key word is *knowledge*, not *syntax*. Two loops that happen to look similar are not necessarily a DRY violation. Two pieces of code that represent the same *business rule* most certainly are.

```java
// BAD — the discount calculation is duplicated in two places
public class OnlineOrderProcessor {
    public double calculateTotal(Order order) {
        double total = order.getSubtotal();
        if (order.getCustomer().isPremium()) {
            total *= 0.9;  // 10% discount
        }
        return total;
    }
}

public class InStoreOrderProcessor {
    public double calculateTotal(Order order) {
        double total = order.getSubtotal();
        if (order.getCustomer().isPremium()) {
            total *= 0.9;  // same business rule, duplicated
        }
        return total;
    }
}

// GOOD — the rule lives in one place
public class DiscountCalculator {
    public double applyPremiumDiscount(double subtotal) {
        return subtotal * 0.9;
    }
}
```

The DRY principle has a cost: abstraction. Two things merged into one become harder to change independently if they later diverge. This is why the rule of three is useful: if you see the same pattern three times, abstract it. Twice might be coincidence.

### KISS — Keep It Simple, Stupid

Simplicity is not the absence of intelligence. It is the elimination of everything that is not necessary.

```java
// BAD — clever but unreadable
public boolean isPalindrome(String s) {
    return IntStream.range(0, s.length() / 2)
        .noneMatch(i -> s.charAt(i) != s.charAt(s.length() - 1 - i));
}

// GOOD — obvious at a glance
public boolean isPalindrome(String s) {
    String reversed = new StringBuilder(s).reverse().toString();
    return s.equals(reversed);
}

// ALSO GOOD — if performance matters, the explicit loop is clearer than the stream
public boolean isPalindrome(String s) {
    int left = 0;
    int right = s.length() - 1;
    while (left < right) {
        if (s.charAt(left) != s.charAt(right)) return false;
        left++;
        right--;
    }
    return true;
}
```

Complexity compounds. Every clever trick you use requires the next programmer to be clever enough to understand it. Every unnecessary abstraction is another concept to learn. Every configuration option is another thing that can be wrong.

---

## Putting It All Together

Clean code is a discipline, not a destination. You will write messy code under time pressure, then clean it up when you return to it. The Boy Scout Rule: *leave the code cleaner than you found it*. Not a complete rewrite — just a variable renamed, a function extracted, a comment removed.

The practical workflow:
1. **Make it work** — get the tests passing.
2. **Make it right** — refactor without changing behaviour (the tests protect you).
3. **Make it fast** — optimise only if profiling reveals a bottleneck.

Steps 2 and 3 are skipped constantly in industry. The professional programmer does not skip them. Technical debt is real debt: it accrues interest in the form of bugs, slowness, and the inability to add features.

Write code that you would be proud to show to a senior engineer tomorrow. If you would be embarrassed by a name, change it now. If a function is confusing, extract it now. These investments pay off within days, not years.

---

## Summary

| Principle | The Rule |
|---|---|
| Naming | Reveal intent; be consistent; be searchable |
| Functions | Small; one level of abstraction; no side effects; few parameters |
| Comments | Say *why*, not *what*; prefer expressive code |
| Error Handling | Use exceptions; fail fast; never return or pass null |
| Classes | One responsibility; high cohesion; low coupling |
| YAGNI | Don't build for imagined future requirements |
| DRY | One authoritative representation of each piece of knowledge |
| KISS | Simple is almost always better |

> "Clean code always looks like it was written by someone who cares."
> — Robert C. Martin
