# Architecture Patterns

> "Architecture is the decisions that are hard to change."
> — Martin Fowler

Writing a function well is craft. Organising a system well is architecture. The difference is scale: architecture decisions affect dozens of files and thousands of lines, and they are expensive to undo.

This guide introduces the patterns that structure real Java applications. Each pattern is a battle-tested answer to a recurring problem. Learning them means you can apply proven solutions rather than reinventing them — and, more importantly, you can *read* codebases built on them without getting lost.

---

## Layered Architecture

The most common architecture in enterprise Java is the layered architecture. It is the starting point for almost every web application.

### The Layers

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Presentation Layer                              │
│    HTTP Controllers, REST endpoints, View templates                 │
│    "Receive requests, send responses"                               │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ calls
┌───────────────────────────────▼─────────────────────────────────────┐
│                   Business Logic Layer (Service)                    │
│    Use cases, business rules, orchestration                         │
│    "Do the real work"                                               │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ calls
┌───────────────────────────────▼─────────────────────────────────────┐
│                   Data Access Layer (Repository)                    │
│    Database queries, ORM mapping, caching                           │
│    "Retrieve and store data"                                        │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ connects to
┌───────────────────────────────▼─────────────────────────────────────┐
│                        Database                                     │
│    PostgreSQL, MySQL, MongoDB, etc.                                 │
└─────────────────────────────────────────────────────────────────────┘
```

**The fundamental rule: dependencies point downward only.** The Presentation Layer can call the Service Layer. The Service Layer can call the Repository Layer. Nothing in a lower layer calls up to a higher layer. This rule is what makes the layers independent and testable.

### Why Layers?

**Separation of concerns.** Each layer has a distinct role. You can change your database technology without touching the business logic. You can change your REST API without touching your database queries.

**Testability.** Because the Service Layer does not depend on HTTP, you can test business logic with plain unit tests. The controller can be tested with a mock service. The repository can be tested against a real database in isolation.

**Replaceability.** Swap a Spring MVC controller for a gRPC endpoint without changing the service. Swap a JPA repository for a MongoDB repository without changing the service.

### Example: A User Registration Flow

```java
// Presentation Layer — handles HTTP
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UserResponse.from(user));
    }
}

// Business Logic Layer — enforces rules, orchestrates
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public User register(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException(request.email());
        }

        User user = User.create(
            request.name(),
            request.email(),
            passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmail(savedUser);
        return savedUser;
    }
}

// Data Access Layer — handles persistence
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
```

Notice what belongs where:
- The controller knows about HTTP status codes and request/response bodies. It knows nothing about passwords or databases.
- The service knows about business rules (no duplicate emails, welcome emails). It knows nothing about HTTP or SQL.
- The repository knows about database queries. It knows nothing about HTTP or business rules.

### Common Mistakes

**Putting business logic in the controller** — the controller grows large, becomes impossible to test without an HTTP context, and the logic can never be reused by a background job or command-line tool.

**Putting business logic in the entity** — the database model becomes bloated with rules that belong in a service. Database models should be simple data containers.

**Skipping layers** — the controller calling the repository directly creates a "smart UI" antipattern. The business logic ends up scattered across controllers with no coherent place to find it.

---

## The Repository Pattern

The Repository pattern sits between the business logic layer and the database. It provides a collection-like interface for accessing domain objects, hiding all the complexity of database interaction.

```
Business Logic Layer
        │
        │  findById(id), save(entity), findAll()
        ▼
  ┌─────────────────────┐
  │  UserRepository     │  ← The interface (pure Java, no SQL)
  │  (interface)        │
  └──────────┬──────────┘
             │  implements
  ┌──────────▼──────────┐
  │ JpaUserRepository   │  ← The implementation (Spring Data / JPA)
  │ (implementation)    │
  └──────────┬──────────┘
             │
       PostgreSQL / MySQL
```

### Why the Interface Matters

The interface is the contract. The business logic depends on the *interface*, not on the *implementation*. This means you can swap implementations:

```java
// The interface — pure domain language, no persistence technology
public interface UserRepository {
    User save(User user);
    Optional<User> findById(long id);
    Optional<User> findByEmail(String email);
    List<User> findAllActive();
    void delete(long id);
}

// Production implementation — JPA/Hibernate
@Repository
public class JpaUserRepository implements UserRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public User save(User user) {
        return em.merge(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return em.createQuery(
            "SELECT u FROM User u WHERE u.email = :email", User.class)
            .setParameter("email", email)
            .getResultStream()
            .findFirst();
    }
    // ... other methods
}

// Test implementation — in-memory, no database needed
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> store = new HashMap<>();
    private long nextId = 1;

    @Override
    public User save(User user) {
        if (user.getId() == 0) user.setId(nextId++);
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return store.values().stream()
            .filter(u -> u.getEmail().equals(email))
            .findFirst();
    }
    // ... other methods
}
```

In tests, inject `InMemoryUserRepository`. Tests run in milliseconds without a real database. In production, Spring injects `JpaUserRepository`. The business logic — `UserService` — never changes.

### Generic Repository vs Specific Repository

A generic repository provides a base of common operations:

```java
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}
```

Domain-specific repositories extend it with business-meaningful queries:

```java
public interface OrderRepository extends Repository<Order, Long> {
    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);
    List<Order> findOrdersPlacedBetween(LocalDate start, LocalDate end);
    Optional<Order> findMostRecentOrderForCustomer(Customer customer);
}
```

Never add generic query methods (`findBy(String fieldName, Object value)`) to your repository. Queries should have business-meaningful names that make the intent explicit.

---

## MVC — Model-View-Controller

MVC is the dominant architecture for web applications and is the pattern that Spring MVC is built on. It separates an application into three concerns.

```
                         ┌──────────────────────────────────┐
                         │           Controller              │
  HTTP Request ─────────►│  Handles input                   │
                         │  Validates request                │
                         │  Calls service/model             │
                         │  Selects view                    │
                         └───────────┬──────────────────────┘
                                     │
                         ┌───────────▼──────────────────────┐
                         │              Model                │
                         │  Business logic                  │
                         │  Domain objects                  │
                         │  State                           │
                         └───────────┬──────────────────────┘
                                     │
                         ┌───────────▼──────────────────────┐
                         │              View                 │
  HTTP Response ◄────────│  Renders HTML / JSON / XML       │
                         │  Formats data for the client     │
                         └──────────────────────────────────┘
```

### Responsibilities

**Model** — the domain. It contains the business data and rules. In Spring applications, the Model is typically your service and domain objects. The Model knows nothing about how it is displayed or how requests arrive.

**View** — the presentation. It renders data from the Model for the client. In a classic web app, this is Thymeleaf, JSP, or FreeMarker. In a REST API, the "view" is the JSON serialisation. The View knows nothing about business rules.

**Controller** — the coordinator. It receives the request, validates input, calls the Model (usually a service), and passes the result to the View. The Controller knows a little about both, but does very little processing itself.

### Spring MVC in Practice

```java
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAllActive());
        return "products/list";  // resolves to a Thymeleaf template
    }

    @GetMapping("/{id}")
    public String showProduct(@PathVariable long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "products/detail";
    }

    @PostMapping
    public String createProduct(@Valid @ModelAttribute ProductForm form,
                                BindingResult errors) {
        if (errors.hasErrors()) return "products/create";
        productService.create(form.toProduct());
        return "redirect:/products";
    }
}
```

For REST APIs, `@RestController` combines `@Controller` and `@ResponseBody`, returning data directly as JSON rather than selecting a template:

```java
@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    private final ProductService productService;

    @GetMapping
    public List<ProductDto> list() {
        return productService.findAllActive().stream()
            .map(ProductDto::from)
            .toList();
    }

    @GetMapping("/{id}")
    public ProductDto get(@PathVariable long id) {
        return ProductDto.from(productService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(@Valid @RequestBody CreateProductRequest request) {
        return ProductDto.from(productService.create(request));
    }
}
```

### The DTO Pattern

A Data Transfer Object (DTO) is a plain data class that carries data between layers. It is distinct from a domain entity. DTOs serve as the contract between your API and its callers.

```java
// Domain entity — the full object with all business methods
@Entity
public class Product {
    @Id
    private Long id;
    private String name;
    private Money price;
    private boolean active;
    private String internalSupplierCode;  // internal field, not for the API
    // ... business methods
}

// DTO — only what the API caller needs to see
public record ProductDto(Long id, String name, String formattedPrice) {
    public static ProductDto from(Product product) {
        return new ProductDto(
            product.getId(),
            product.getName(),
            product.getPrice().format()
        );
    }
}
```

Always convert between entities and DTOs in the service or a dedicated mapper class, never in the controller.

---

## Hexagonal Architecture (Ports and Adapters)

Layered architecture is a good starting point, but it has a limitation: the business logic is in the middle and can still end up depending on framework classes, JPA annotations, or HTTP concepts. Hexagonal architecture takes separation further.

The core insight: the domain should have *zero* dependencies on infrastructure. Not "few" — zero.

### The Mental Model

```
                         ┌─────────────────────────────────────────┐
                         │           External World                │
                         │  HTTP clients, databases, message       │
                         │  queues, email servers, file systems    │
                         └──────────┬───────────────────┬─────────┘
                                    │                   │
                         ┌──────────▼───────┐  ┌────────▼────────┐
                         │  Driving Adapter │  │ Driven Adapter  │
                         │ (REST Controller │  │ (JPA Repository │
                         │  CLI command,    │  │  Email client,  │
                         │  Message listener│  │  File writer)   │
                         └──────────┬───────┘  └────────┬────────┘
                                    │ (Primary Port)    │ (Secondary Port)
                         ┌──────────▼───────────────────▼────────┐
                         │                                        │
                         │            Domain Core                 │
                         │                                        │
                         │   Pure Java. No Spring. No JPA.        │
                         │   No HTTP. Business rules only.        │
                         │                                        │
                         └────────────────────────────────────────┘
```

**Primary ports (driving)** — interfaces the domain *exposes*. Callers use these to interact with the domain (e.g., `OrderService.placeOrder()`).

**Secondary ports (driven)** — interfaces the domain *defines and uses*, but does not implement. The domain needs a persistence mechanism; it defines `OrderRepository`. The adapter provides the implementation.

**Adapters** — the code that translates between the external world and the domain. A REST controller is an adapter. A JPA repository implementation is an adapter.

### A Complete Example

```java
// ─── Domain Core ─────────────────────────────────────────────────

// The domain entity — pure Java, no annotations
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> lines;
    private OrderStatus status;

    public Money calculateTotal() { ... }
    public void confirm() {
        if (this.status != OrderStatus.PENDING)
            throw new IllegalStateException("Cannot confirm a " + status + " order");
        this.status = OrderStatus.CONFIRMED;
    }
}

// Secondary port — defined by the domain, implemented by an adapter
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
}

// Primary port — the use case interface
public interface PlaceOrderUseCase {
    OrderId placeOrder(PlaceOrderCommand command);
}

// Domain service implementing the use case
@Service
public class OrderService implements PlaceOrderUseCase {

    // Depends only on the interface, not JPA
    private final OrderRepository orderRepository;
    private final ProductCatalog productCatalog;
    private final PaymentPort paymentPort;

    @Override
    public OrderId placeOrder(PlaceOrderCommand command) {
        List<OrderLine> lines = command.items().stream()
            .map(item -> buildOrderLine(item, productCatalog))
            .toList();

        Order order = Order.create(command.customerId(), lines);
        paymentPort.authorise(order.calculateTotal(), command.paymentDetails());
        return orderRepository.save(order).getId();
    }
}


// ─── Adapters ──────────────────────────────────────────────────────

// Driving adapter — REST controller
@RestController
@RequestMapping("/api/orders")
public class OrderRestAdapter {

    private final PlaceOrderUseCase placeOrderUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderIdResponse placeOrder(@RequestBody PlaceOrderRequest request) {
        PlaceOrderCommand command = request.toCommand();
        OrderId orderId = placeOrderUseCase.placeOrder(command);
        return new OrderIdResponse(orderId.value());
    }
}

// Driven adapter — JPA implementation
@Component
public class JpaOrderRepository implements OrderRepository {

    private final JpaOrderEntityRepository jpaRepo;
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity saved = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepo.findById(id.value()).map(mapper::toDomain);
    }
}
```

### Why Bother?

The payoff is testability. The entire `OrderService` — all of its business logic — can be tested with pure Java:

```java
class OrderServiceTest {
    private final OrderRepository orderRepo = new InMemoryOrderRepository();
    private final PaymentPort paymentPort = new FakePaymentPort();
    private final OrderService service = new OrderService(orderRepo, paymentPort, ...);

    @Test
    void placingAnOrderCreatesAConfirmedOrder() {
        OrderId id = service.placeOrder(aValidOrderCommand());
        Order order = orderRepo.findById(id).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }
}
```

No Spring context. No database. Tests run in milliseconds. The domain logic is tested in complete isolation from any infrastructure concern.

---

## Domain-Driven Design Concepts

Domain-Driven Design (DDD) is a collection of patterns and language for designing software around the business domain. A full DDD treatment is a book in itself (Eric Evans' *Domain-Driven Design* is the canonical reference). This section covers the five concepts most valuable for day-to-day Java development.

### Entities vs Value Objects

```
                ┌─────────────────────────────────────────────────────┐
                │ ENTITY                       VALUE OBJECT            │
                │ Has identity                 Described by its value  │
                │ Mutable                      Immutable               │
                │ Compared by ID               Compared by all fields  │
                │                                                      │
                │ Example: Customer #42        Example: Money($25.00)  │
                │ "Alice" at address X         $25.00 == $25.00        │
                │ moves to address Y           (any two $25 are equal) │
                │ but is still Customer #42                            │
                └─────────────────────────────────────────────────────┘
```

**Entity** — an object defined by a thread of continuity and identity. Two customers with the same name are not the same customer.

```java
@Entity
public class Customer {
    @Id
    @GeneratedValue
    private Long id;  // identity

    private String name;
    private Address address;  // can change

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer c)) return false;
        return Objects.equals(id, c.id);  // equality based on ID only
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

**Value Object** — an object defined entirely by its values. Two `Money` objects with the same amount and currency are the same thing. Value objects should be immutable.

```java
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        this.amount = Objects.requireNonNull(amount).setScale(2, RoundingMode.HALF_UP);
        this.currency = Objects.requireNonNull(currency);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new CurrencyMismatchException(this.currency, other.currency);
        return new Money(this.amount.add(other.amount), this.currency);  // new object
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money m)) return false;
        return amount.equals(m.amount) && currency.equals(m.currency);  // all fields
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
```

The discipline of distinguishing entities from value objects prevents a large class of bugs. Make value objects immutable. Enforce their invariants in the constructor.

### Aggregates

An aggregate is a cluster of domain objects treated as a unit. One object in the cluster is the *aggregate root* — the only object the outside world holds a reference to. All modifications to objects inside the aggregate go through the root.

```
                ┌─────────────────────────────────────────────┐
                │           Order Aggregate                   │
                │                                             │
                │   ┌────────────────┐                        │
                │   │  Order         │  ← Aggregate Root      │
                │   │  (root)        │                        │
                │   └───────┬────────┘                        │
                │           │                                 │
                │   ┌───────▼────────┐  ┌──────────────────┐  │
                │   │  OrderLine     │  │  ShippingAddress  │  │
                │   │  (child)       │  │  (value object)   │  │
                │   └───────┬────────┘  └──────────────────┘  │
                │           │                                 │
                │   ┌───────▼────────┐                        │
                │   │  Product       │  ← Reference (by ID)   │
                │   │  (other aggr.) │    not a child          │
                │   └────────────────┘                        │
                └─────────────────────────────────────────────┘
```

```java
public class Order {  // Aggregate root

    private final OrderId id;
    private final List<OrderLine> lines = new ArrayList<>();
    private OrderStatus status;

    // The only way to add a line — goes through the root
    public void addLine(ProductId productId, int quantity, Money unitPrice) {
        if (status != OrderStatus.DRAFT)
            throw new IllegalStateException("Cannot modify a " + status + " order");
        lines.add(new OrderLine(productId, quantity, unitPrice));
    }

    public Money total() {
        return lines.stream()
            .map(OrderLine::lineTotal)
            .reduce(Money.ZERO, Money::add);
    }
}

// WRONG — bypassing the aggregate root
order.getLines().add(new OrderLine(...));  // direct access breaks the aggregate boundary

// CORRECT — through the root
order.addLine(productId, 2, unitPrice);   // root enforces invariants
```

The rule: **only hold references to aggregate roots**. Other objects inside an aggregate should only be reachable by navigating from the root.

### Bounded Contexts

A bounded context is an explicit boundary within which a model applies. The same word can mean different things in different contexts, and that is fine — as long as each context is clear about what it means.

```
┌──────────────────────┐         ┌──────────────────────┐
│   Sales Context      │         │  Shipping Context    │
│                      │         │                      │
│  Customer:           │         │  Customer:           │
│  - Name              │         │  - Delivery name     │
│  - Email             │   ───►  │  - Delivery address  │
│  - Order history     │  event  │  - Special notes     │
│  - Payment details   │         │                      │
│  - Loyalty points    │         │  Order:              │
│                      │         │  - Reference number  │
│  Order:              │         │  - Items and weights │
│  - Basket, payment   │         │  - Tracking number   │
│  - Discounts applied │         │  - Delivery status   │
└──────────────────────┘         └──────────────────────┘
```

The `Customer` in the Sales context has payment details and order history. The `Customer` in the Shipping context has a delivery address and notes for the courier. They are not the same object, and forcing them to be the same object creates a bloated, incoherent model that serves neither context well.

The practical implication: do not try to build one unified domain model for your entire system. Identify the boundaries where terminology and meaning shift, and build separate models for each context.

### Ubiquitous Language

The Ubiquitous Language is the shared vocabulary between developers and domain experts. Every term in the domain should appear in the code, and every term in the code should come from the domain.

```java
// BAD — technical jargon that means nothing to a business analyst
class AbstractEntityStateTransitionProcessor {
    void processEntityStateChange(EntityRecord record, StateEnum newState) { }
}

// GOOD — uses the language of the business
class LoanApplicationWorkflow {
    void approveLoanApplication(LoanApplication application) { }
    void rejectLoanApplication(LoanApplication application, String reason) { }
    void requestAdditionalDocuments(LoanApplication application, DocumentType type) { }
}
```

The test: can a domain expert (a loan officer, in this example) read the method names and understand what the code does? If not, the language has drifted from the domain, and that gap will grow over time into misunderstandings and bugs.

---

## Choosing Between Architectures

These patterns are not mutually exclusive, and they are not of equal weight. Think of them as a progression:

```
┌──────────────────────────────────────────────────────────────┐
│  Start here for most applications                           │
│  Layered Architecture: Controller → Service → Repository   │
│  Fast to start, familiar to most Java developers           │
└──────────────────────────────────────────────────────────────┘
                           │
                 when domain logic is complex
                           │
┌──────────────────────────▼───────────────────────────────────┐
│  Add DDD patterns: Entities, Value Objects, Aggregates      │
│  Ubiquitous Language: speak the business domain             │
│  Bounded Contexts: identify where models diverge            │
└──────────────────────────────────────────────────────────────┘
                           │
              when the domain must be infrastructure-free
                           │
┌──────────────────────────▼───────────────────────────────────┐
│  Hexagonal Architecture: Ports and Adapters                 │
│  Domain has zero infrastructure dependencies                │
│  Maximum testability, maximum flexibility                   │
└──────────────────────────────────────────────────────────────┘
```

A simple CRUD application may never need to go beyond the layered architecture. A complex billing system with intricate rules may benefit enormously from hexagonal architecture and DDD.

Start simple. Introduce complexity only when the problem demands it.

---

## Summary

| Pattern | Problem It Solves | Key Rule |
|---|---|---|
| Layered Architecture | Organisation and separation of concerns | Dependencies flow downward only |
| Repository Pattern | Decoupling business logic from database | Depend on the interface, not the implementation |
| MVC | Web application structure | Each part has one role: Model (logic), View (rendering), Controller (routing) |
| Hexagonal Architecture | Infrastructure independence | Domain has zero dependencies; adapters translate |
| DDD — Entities/VOs | Modelling domain objects correctly | Entities: identity; Value Objects: values |
| DDD — Aggregates | Consistency boundary | Always go through the root |
| DDD — Bounded Contexts | Model coherence at scale | One model per context |
| DDD — Ubiquitous Language | Communication with domain experts | Code should speak the business language |
