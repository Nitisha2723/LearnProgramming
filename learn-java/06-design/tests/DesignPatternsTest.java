/**
 * DesignPatternsTest.java
 *
 * Unit tests for the Java Design Patterns module (06-design).
 *
 * Patterns tested:
 *   1.  Singleton          — same instance, thread-safety demonstration
 *   2.  Builder            — required-field validation, optional fields, immutability
 *   3.  Observer           — notification, unsubscribe, multiple observers
 *   4.  Strategy           — runtime swap, per-strategy results
 *   5.  Decorator          — stacking, accumulated cost, composed description
 *   6.  Repository (CRUD)  — InMemoryUserRepository
 *   7.  Factory            — correct type per input
 *   8.  SOLID / DIP        — OrderService works with mock repository
 *
 * Compile and run (JUnit 5 on the classpath):
 *   javac -cp junit-platform-console-standalone.jar DesignPatternsTest.java
 *   java  -cp .:junit-platform-console-standalone.jar \
 *         org.junit.platform.console.ConsoleLauncher --select-class DesignPatternsTest
 */

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

// ============================================================
//  SUPPORTING CLASSES (self-contained for one-file compilation)
//  Each section contains just enough code to drive its tests.
// ============================================================

// ---- 1. SINGLETON -----------------------------------------

/**
 * Thread-safe lazy singleton via double-checked locking.
 * Using an inner-class holder is also acceptable.
 */
class DatabaseConnectionPool {
    private static volatile DatabaseConnectionPool instance;
    private final int poolId = System.identityHashCode(this);

    private DatabaseConnectionPool() {}

    public static DatabaseConnectionPool getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionPool.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionPool();
                }
            }
        }
        return instance;
    }

    public int getPoolId() { return poolId; }

    // Test helper: reset singleton (not for production use!)
    static void resetInstance() { instance = null; }
}

// ---- 2. BUILDER -------------------------------------------

/** Simple e-mail notification message built by a Builder. */
class EmailMessage {
    private final String to;       // required
    private final String subject;  // required
    private final String body;     // required
    private final String replyTo;  // optional
    private final int    retries;  // optional, default 3

    private EmailMessage(Builder b) {
        this.to      = b.to;
        this.subject = b.subject;
        this.body    = b.body;
        this.replyTo = b.replyTo;
        this.retries = b.retries;
    }

    public String getTo()      { return to; }
    public String getSubject() { return subject; }
    public String getBody()    { return body; }
    public String getReplyTo() { return replyTo; }
    public int    getRetries() { return retries; }

    public static class Builder {
        private final String to;
        private final String subject;
        private final String body;
        private String replyTo = null;
        private int    retries = 3;

        public Builder(String to, String subject, String body) {
            if (to == null || to.isBlank())
                throw new IllegalArgumentException("'to' is required");
            if (subject == null || subject.isBlank())
                throw new IllegalArgumentException("'subject' is required");
            if (body == null || body.isBlank())
                throw new IllegalArgumentException("'body' is required");
            this.to = to; this.subject = subject; this.body = body;
        }
        public Builder replyTo(String r) { this.replyTo = r; return this; }
        public Builder retries(int n)    {
            if (n < 0) throw new IllegalArgumentException("retries must be >= 0");
            this.retries = n;
            return this;
        }
        public EmailMessage build() { return new EmailMessage(this); }
    }
}

// ---- 3 & 5. OBSERVER + DECORATOR  (shared Order model) ---

enum OrderEvt { PLACED, SHIPPED, DELIVERED, CANCELLED }

interface OrderObserver {
    void onEvent(String orderId, OrderEvt event);
}

class TestOrderSubject {
    private final List<OrderObserver> observers = new ArrayList<>();

    public void subscribe(OrderObserver o) {
        if (!observers.contains(o)) observers.add(o);
    }
    public void unsubscribe(OrderObserver o)  { observers.remove(o); }
    public int  observerCount()               { return observers.size(); }

    public void fireEvent(String orderId, OrderEvt event) {
        new ArrayList<>(observers).forEach(o -> o.onEvent(orderId, event));
    }
}

/** Records every event it receives — useful as a test spy. */
class RecordingObserver implements OrderObserver {
    private final List<String> events = new ArrayList<>();
    @Override public void onEvent(String id, OrderEvt ev) { events.add(id + ":" + ev); }
    public List<String> events() { return Collections.unmodifiableList(events); }
    public int eventCount() { return events.size(); }
}

// ---- 4. STRATEGY ------------------------------------------

interface DiscountStrategy {
    double apply(double price);
    String name();
}

class NoDiscount implements DiscountStrategy {
    @Override public double apply(double p) { return p; }
    @Override public String name()          { return "NONE"; }
}

class TenPercentDiscount implements DiscountStrategy {
    @Override public double apply(double p) { return p * 0.90; }
    @Override public String name()          { return "10%"; }
}

class HalfPriceDiscount implements DiscountStrategy {
    @Override public double apply(double p) { return p * 0.50; }
    @Override public String name()          { return "50%"; }
}

/** Context — holds a strategy that can be swapped at runtime. */
class PriceCalculator {
    private DiscountStrategy strategy;

    public PriceCalculator(DiscountStrategy strategy) { this.strategy = strategy; }
    public void   setStrategy(DiscountStrategy s)     { this.strategy = s; }
    public double calculate(double price)              { return strategy.apply(price); }
    public String strategyName()                       { return strategy.name(); }
}

// ---- 5. DECORATOR -----------------------------------------

/** Component interface for order processing services. */
interface OrderProcessingService {
    double getCost();
    String getDescription();
}

/** The base ("plain") order processing. */
class BasicOrderService implements OrderProcessingService {
    @Override public double getCost()        { return 1.00; }
    @Override public String getDescription() { return "BasicOrder"; }
}

/** Abstract decorator: wraps another service. */
abstract class OrderServiceDecorator implements OrderProcessingService {
    protected final OrderProcessingService wrapped;
    OrderServiceDecorator(OrderProcessingService s) { this.wrapped = s; }
}

class ExpressShippingDecorator extends OrderServiceDecorator {
    ExpressShippingDecorator(OrderProcessingService s) { super(s); }
    @Override public double getCost()        { return wrapped.getCost() + 5.00; }
    @Override public String getDescription() { return wrapped.getDescription() + "+Express"; }
}

class GiftWrappingDecorator extends OrderServiceDecorator {
    GiftWrappingDecorator(OrderProcessingService s) { super(s); }
    @Override public double getCost()        { return wrapped.getCost() + 2.50; }
    @Override public String getDescription() { return wrapped.getDescription() + "+GiftWrap"; }
}

class InsuranceDecorator extends OrderServiceDecorator {
    InsuranceDecorator(OrderProcessingService s) { super(s); }
    @Override public double getCost()        { return wrapped.getCost() + 3.00; }
    @Override public String getDescription() { return wrapped.getDescription() + "+Insurance"; }
}

// ---- 6. REPOSITORY ----------------------------------------

class User {
    private final String id;
    private String name;
    private String email;

    public User(String id, String name, String email) {
        this.id = id; this.name = name; this.email = email;
    }
    public String getId()    { return id; }
    public String getName()  { return name; }
    public String getEmail() { return email; }
    public void   setName(String n)  { this.name  = n; }
    public void   setEmail(String e) { this.email = e; }
}

interface UserRepository {
    void   save(User user);
    Optional<User> findById(String id);
    List<User> findAll();
    void   delete(String id);
    int    count();
}

class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> store = new LinkedHashMap<>();

    @Override public void save(User u)   { store.put(u.getId(), u); }
    @Override public Optional<User> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public List<User> findAll(){ return new ArrayList<>(store.values()); }
    @Override public void delete(String id) { store.remove(id); }
    @Override public int  count()        { return store.size(); }
}

// ---- 7. FACTORY -------------------------------------------

interface Notification { String type(); }

class PushNotification  implements Notification { @Override public String type() { return "PUSH";  } }
class SmsNotification   implements Notification { @Override public String type() { return "SMS";   } }
class EmailNotification implements Notification { @Override public String type() { return "EMAIL"; } }
class SlackNotification implements Notification { @Override public String type() { return "SLACK"; } }

enum NotifType { PUSH, SMS, EMAIL, SLACK }

class NotificationFactory {
    public static Notification create(NotifType type) {
        return switch (type) {
            case PUSH  -> new PushNotification();
            case SMS   -> new SmsNotification();
            case EMAIL -> new EmailNotification();
            case SLACK -> new SlackNotification();
        };
    }
}

// ---- 8. SOLID / DIP  (mirrors Exercise 01 Solution) ------

interface DP_OrderRepository {
    void save(DP_Order order);
    Optional<DP_Order> findById(String id);
}

class DP_Order {
    private final String id;
    private final double total;
    private String status = "PENDING";
    DP_Order(String id, double total) { this.id = id; this.total = total; }
    public String getId()    { return id; }
    public double getTotal() { return total; }
    public String getStatus(){ return status; }
    public void   setStatus(String s) { this.status = s; }
}

/** Captures what gets saved — a test double / recording mock. */
class RecordingOrderRepository implements DP_OrderRepository {
    private final Map<String, DP_Order> store = new LinkedHashMap<>();
    @Override public void save(DP_Order o) { store.put(o.getId(), o); }
    @Override public Optional<DP_Order> findById(String id) { return Optional.ofNullable(store.get(id)); }
    public int savedCount() { return store.size(); }
    public DP_Order lastSaved() {
        List<DP_Order> all = new ArrayList<>(store.values());
        return all.isEmpty() ? null : all.get(all.size() - 1);
    }
}

/** Thin service: depends only on the DP_OrderRepository interface. */
class DP_OrderService {
    private final DP_OrderRepository repo;
    DP_OrderService(DP_OrderRepository repo) { this.repo = repo; }

    public void placeOrder(DP_Order order) {
        order.setStatus("CONFIRMED");
        repo.save(order);
    }
    public Optional<DP_Order> getOrder(String id) { return repo.findById(id); }
}

// ============================================================
//  TEST CLASS
// ============================================================

@DisplayName("Design Patterns Test Suite")
public class DesignPatternsTest {

    // =========================================================
    //  1. SINGLETON
    // =========================================================

    @Nested
    @DisplayName("1. Singleton Pattern")
    class SingletonTests {

        @BeforeEach
        void resetSingleton() {
            // Reset before each test to keep tests independent
            DatabaseConnectionPool.resetInstance();
        }

        @Test
        @DisplayName("1a. getInstance() always returns the same instance")
        void sameInstanceReturnedOnSubsequentCalls() {
            DatabaseConnectionPool first  = DatabaseConnectionPool.getInstance();
            DatabaseConnectionPool second = DatabaseConnectionPool.getInstance();

            assertSame(first, second,
                    "Both calls should return the identical object");
            assertEquals(first.getPoolId(), second.getPoolId());
        }

        @Test
        @DisplayName("1b. getInstance() is not null")
        void instanceIsNotNull() {
            assertNotNull(DatabaseConnectionPool.getInstance());
        }

        @Test
        @DisplayName("1c. Thread-safety: concurrent calls return the same instance")
        void concurrentAccessReturnsSameInstance() throws InterruptedException {
            int threadCount = 50;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            Set<Integer> poolIds = Collections.synchronizedSet(new HashSet<>());
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    poolIds.add(DatabaseConnectionPool.getInstance().getPoolId());
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            assertEquals(1, poolIds.size(),
                    "All threads must see the same instance (only one unique poolId)");
        }
    }

    // =========================================================
    //  2. BUILDER
    // =========================================================

    @Nested
    @DisplayName("2. Builder Pattern")
    class BuilderTests {

        @Test
        @DisplayName("2a. Build succeeds with all three required fields")
        void buildSucceedsWithRequiredFields() {
            EmailMessage msg = new EmailMessage.Builder(
                    "bob@example.com", "Hello", "Body text").build();

            assertEquals("bob@example.com", msg.getTo());
            assertEquals("Hello",           msg.getSubject());
            assertEquals("Body text",       msg.getBody());
        }

        @Test
        @DisplayName("2b. Optional fields default correctly when not set")
        void optionalFieldsHaveDefaults() {
            EmailMessage msg = new EmailMessage.Builder(
                    "a@b.com", "Sub", "Body").build();

            assertNull(msg.getReplyTo(), "replyTo should default to null");
            assertEquals(3, msg.getRetries(),  "retries should default to 3");
        }

        @Test
        @DisplayName("2c. Optional fields are stored when explicitly set")
        void optionalFieldsStoredWhenSet() {
            EmailMessage msg = new EmailMessage.Builder(
                    "a@b.com", "Sub", "Body")
                    .replyTo("support@example.com")
                    .retries(5)
                    .build();

            assertEquals("support@example.com", msg.getReplyTo());
            assertEquals(5,                     msg.getRetries());
        }

        @Test
        @DisplayName("2d. Blank 'to' field throws IllegalArgumentException")
        void blankToFieldThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new EmailMessage.Builder("", "Sub", "Body"),
                    "'to' must not be blank");
        }

        @Test
        @DisplayName("2e. Null 'subject' field throws IllegalArgumentException")
        void nullSubjectThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new EmailMessage.Builder("a@b.com", null, "Body"),
                    "'subject' must not be null");
        }

        @Test
        @DisplayName("2f. Negative retries throws IllegalArgumentException")
        void negativeRetriesThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new EmailMessage.Builder("a@b.com", "Sub", "Body").retries(-1),
                    "retries must be >= 0");
        }

        @Test
        @DisplayName("2g. Built messages are independent (builder reuse does not mutate past builds)")
        void builderProducesIndependentInstances() {
            EmailMessage.Builder builder = new EmailMessage.Builder(
                    "shared@example.com", "Subject", "Body");

            EmailMessage m1 = builder.retries(1).build();
            EmailMessage m2 = builder.retries(9).build();

            assertEquals(1, m1.getRetries(), "m1 retries should still be 1");
            assertEquals(9, m2.getRetries(), "m2 retries should be 9");
        }
    }

    // =========================================================
    //  3. OBSERVER
    // =========================================================

    @Nested
    @DisplayName("3. Observer Pattern")
    class ObserverTests {

        private TestOrderSubject subject;
        private RecordingObserver obs1;
        private RecordingObserver obs2;

        @BeforeEach
        void setUp() {
            subject = new TestOrderSubject();
            obs1    = new RecordingObserver();
            obs2    = new RecordingObserver();
        }

        @Test
        @DisplayName("3a. Single registered observer receives the event")
        void singleObserverReceivesEvent() {
            subject.subscribe(obs1);
            subject.fireEvent("ORD-1", OrderEvt.PLACED);

            assertEquals(1, obs1.eventCount());
            assertEquals("ORD-1:PLACED", obs1.events().get(0));
        }

        @Test
        @DisplayName("3b. Multiple observers each receive every event")
        void multipleObserversEachReceiveEvent() {
            subject.subscribe(obs1);
            subject.subscribe(obs2);
            subject.fireEvent("ORD-2", OrderEvt.SHIPPED);

            assertEquals(1, obs1.eventCount(), "obs1 should have 1 event");
            assertEquals(1, obs2.eventCount(), "obs2 should have 1 event");
        }

        @Test
        @DisplayName("3c. Unsubscribed observer does NOT receive subsequent events")
        void unsubscribedObserverDoesNotReceiveEvents() {
            subject.subscribe(obs1);
            subject.subscribe(obs2);
            subject.fireEvent("ORD-3", OrderEvt.PLACED);

            subject.unsubscribe(obs2);
            subject.fireEvent("ORD-3", OrderEvt.DELIVERED);

            assertEquals(2, obs1.eventCount(),  "obs1 subscribed throughout — should get both");
            assertEquals(1, obs2.eventCount(),  "obs2 unsubscribed before 2nd event");
        }

        @Test
        @DisplayName("3d. Observer count reflects subscribe and unsubscribe operations")
        void observerCountIsAccurate() {
            assertEquals(0, subject.observerCount());
            subject.subscribe(obs1);
            assertEquals(1, subject.observerCount());
            subject.subscribe(obs2);
            assertEquals(2, subject.observerCount());
            subject.unsubscribe(obs1);
            assertEquals(1, subject.observerCount());
        }

        @Test
        @DisplayName("3e. Subscribing the same observer twice does not duplicate notifications")
        void duplicateSubscribeIsIdempotent() {
            subject.subscribe(obs1);
            subject.subscribe(obs1); // second subscribe same ref
            subject.fireEvent("ORD-4", OrderEvt.CANCELLED);

            assertEquals(1, obs1.eventCount(),
                    "Observer registered twice must still receive each event only once");
        }

        @Test
        @DisplayName("3f. Firing multiple events delivers them all in order")
        void multipleEventsDeliveredInOrder() {
            subject.subscribe(obs1);
            subject.fireEvent("ORD-5", OrderEvt.PLACED);
            subject.fireEvent("ORD-5", OrderEvt.SHIPPED);
            subject.fireEvent("ORD-5", OrderEvt.DELIVERED);

            assertEquals(List.of("ORD-5:PLACED", "ORD-5:SHIPPED", "ORD-5:DELIVERED"),
                    obs1.events());
        }
    }

    // =========================================================
    //  4. STRATEGY
    // =========================================================

    @Nested
    @DisplayName("4. Strategy Pattern")
    class StrategyTests {

        @Test
        @DisplayName("4a. NoDiscount strategy returns original price")
        void noDiscountReturnsOriginalPrice() {
            PriceCalculator calc = new PriceCalculator(new NoDiscount());
            assertEquals(100.0, calc.calculate(100.0), 0.001);
        }

        @Test
        @DisplayName("4b. TenPercentDiscount strategy applies 10% off")
        void tenPercentDiscountApplies() {
            PriceCalculator calc = new PriceCalculator(new TenPercentDiscount());
            assertEquals(90.0, calc.calculate(100.0), 0.001);
        }

        @Test
        @DisplayName("4c. HalfPriceDiscount strategy applies 50% off")
        void halfPriceDiscountApplies() {
            PriceCalculator calc = new PriceCalculator(new HalfPriceDiscount());
            assertEquals(50.0, calc.calculate(100.0), 0.001);
        }

        @Test
        @DisplayName("4d. Strategy can be swapped at runtime")
        void strategySwappedAtRuntime() {
            PriceCalculator calc = new PriceCalculator(new NoDiscount());
            assertEquals(100.0, calc.calculate(100.0), 0.001);
            assertEquals("NONE", calc.strategyName());

            // Swap to 10%
            calc.setStrategy(new TenPercentDiscount());
            assertEquals(90.0, calc.calculate(100.0), 0.001);
            assertEquals("10%", calc.strategyName());

            // Swap to 50%
            calc.setStrategy(new HalfPriceDiscount());
            assertEquals(50.0, calc.calculate(100.0), 0.001);
        }

        @Test
        @DisplayName("4e. Strategy works correctly across a range of prices")
        void strategyAppliedToVariousPrices() {
            PriceCalculator calc = new PriceCalculator(new TenPercentDiscount());

            assertEquals(9.0,  calc.calculate(10.0),  0.001);
            assertEquals(45.0, calc.calculate(50.0),  0.001);
            assertEquals(0.0,  calc.calculate(0.0),   0.001);
        }

        @ParameterizedTest
        @DisplayName("4f. HalfPrice discount always yields exactly half")
        @ValueSource(doubles = { 1.0, 10.0, 99.99, 200.0, 0.02 })
        void halfPriceIsExactlyHalf(double price) {
            PriceCalculator calc = new PriceCalculator(new HalfPriceDiscount());
            assertEquals(price / 2.0, calc.calculate(price), 0.0001);
        }
    }

    // =========================================================
    //  5. DECORATOR
    // =========================================================

    @Nested
    @DisplayName("5. Decorator Pattern")
    class DecoratorTests {

        @Test
        @DisplayName("5a. Base service has correct cost and description")
        void baseServiceHasBaseValues() {
            OrderProcessingService svc = new BasicOrderService();
            assertEquals(1.00, svc.getCost(), 0.001);
            assertEquals("BasicOrder", svc.getDescription());
        }

        @Test
        @DisplayName("5b. Single ExpressShipping decorator adds $5.00")
        void singleDecoratorAddsCost() {
            OrderProcessingService svc = new ExpressShippingDecorator(new BasicOrderService());
            assertEquals(6.00, svc.getCost(), 0.001);
            assertTrue(svc.getDescription().contains("Express"));
        }

        @Test
        @DisplayName("5c. Two decorators stack — costs accumulate")
        void twoDecoratorsStackCost() {
            // Base(1) + Express(5) + GiftWrap(2.50) = 8.50
            OrderProcessingService svc =
                    new GiftWrappingDecorator(
                    new ExpressShippingDecorator(
                    new BasicOrderService()));

            assertEquals(8.50, svc.getCost(), 0.001);
        }

        @Test
        @DisplayName("5d. Three decorators stack — description contains all names")
        void threeDecoratorsComposeDescription() {
            OrderProcessingService svc =
                    new InsuranceDecorator(
                    new GiftWrappingDecorator(
                    new ExpressShippingDecorator(
                    new BasicOrderService())));

            String desc = svc.getDescription();
            assertTrue(desc.contains("BasicOrder"), "Base must appear in description");
            assertTrue(desc.contains("Express"),    "Express must appear in description");
            assertTrue(desc.contains("GiftWrap"),   "GiftWrap must appear in description");
            assertTrue(desc.contains("Insurance"),  "Insurance must appear in description");
        }

        @Test
        @DisplayName("5e. Full three-decorator cost is sum of all components")
        void fullDecoratorCostIsCorrect() {
            // Base(1) + Express(5) + GiftWrap(2.50) + Insurance(3) = 11.50
            OrderProcessingService svc =
                    new InsuranceDecorator(
                    new GiftWrappingDecorator(
                    new ExpressShippingDecorator(
                    new BasicOrderService())));

            assertEquals(11.50, svc.getCost(), 0.001);
        }

        @Test
        @DisplayName("5f. Decorating in different order changes description but same total cost")
        void decoratorOrderAffectsDescriptionNotCost() {
            OrderProcessingService a =
                    new GiftWrappingDecorator(
                    new ExpressShippingDecorator(new BasicOrderService()));

            OrderProcessingService b =
                    new ExpressShippingDecorator(
                    new GiftWrappingDecorator(new BasicOrderService()));

            assertEquals(a.getCost(), b.getCost(), 0.001, "Cost is order-independent");
            assertNotEquals(a.getDescription(), b.getDescription(),
                    "Description ordering differs when decorators are applied in different order");
        }
    }

    // =========================================================
    //  6. REPOSITORY (InMemoryUserRepository CRUD)
    // =========================================================

    @Nested
    @DisplayName("6. Repository Pattern — InMemoryUserRepository CRUD")
    class RepositoryTests {

        private InMemoryUserRepository repo;

        @BeforeEach
        void setUp() {
            repo = new InMemoryUserRepository();
        }

        @Test
        @DisplayName("6a. New repository is empty")
        void newRepositoryIsEmpty() {
            assertEquals(0, repo.count());
            assertTrue(repo.findAll().isEmpty());
        }

        @Test
        @DisplayName("6b. Save persists a user and count increments")
        void saveIncreasesCount() {
            repo.save(new User("U1", "Alice", "alice@example.com"));
            assertEquals(1, repo.count());
        }

        @Test
        @DisplayName("6c. findById returns the saved user")
        void findByIdReturnsSavedUser() {
            repo.save(new User("U2", "Bob", "bob@example.com"));
            Optional<User> found = repo.findById("U2");

            assertTrue(found.isPresent());
            assertEquals("Bob",              found.get().getName());
            assertEquals("bob@example.com",  found.get().getEmail());
        }

        @Test
        @DisplayName("6d. findById returns empty Optional for unknown ID")
        void findByIdReturnsEmptyForUnknown() {
            assertTrue(repo.findById("NOBODY").isEmpty());
        }

        @Test
        @DisplayName("6e. Saving a user with an existing ID updates (upsert)")
        void saveWithExistingIdUpdatesUser() {
            repo.save(new User("U3", "Carol", "old@example.com"));
            repo.save(new User("U3", "Carol Updated", "new@example.com"));

            assertEquals(1, repo.count(), "Count must not grow on update");
            assertEquals("Carol Updated", repo.findById("U3").get().getName());
        }

        @Test
        @DisplayName("6f. Delete removes the user")
        void deleteRemovesUser() {
            repo.save(new User("U4", "Dave", "dave@example.com"));
            repo.delete("U4");

            assertEquals(0, repo.count());
            assertTrue(repo.findById("U4").isEmpty());
        }

        @Test
        @DisplayName("6g. findAll returns all saved users")
        void findAllReturnsAllUsers() {
            repo.save(new User("U5", "Eve",   "eve@example.com"));
            repo.save(new User("U6", "Frank", "frank@example.com"));

            List<User> all = repo.findAll();
            assertEquals(2, all.size());
            assertTrue(all.stream().anyMatch(u -> u.getId().equals("U5")));
            assertTrue(all.stream().anyMatch(u -> u.getId().equals("U6")));
        }

        @Test
        @DisplayName("6h. delete on unknown ID is a no-op")
        void deleteOnUnknownIdIsNoOp() {
            repo.save(new User("U7", "Grace", "grace@example.com"));
            assertDoesNotThrow(() -> repo.delete("DOES_NOT_EXIST"),
                    "Deleting a non-existent user should not throw");
            assertEquals(1, repo.count());
        }
    }

    // =========================================================
    //  7. FACTORY
    // =========================================================

    @Nested
    @DisplayName("7. Factory Pattern")
    class FactoryTests {

        @Test
        @DisplayName("7a. Factory creates a PushNotification for PUSH type")
        void factoryCreatesPushNotification() {
            Notification n = NotificationFactory.create(NotifType.PUSH);
            assertInstanceOf(PushNotification.class, n);
            assertEquals("PUSH", n.type());
        }

        @Test
        @DisplayName("7b. Factory creates an SmsNotification for SMS type")
        void factoryCreatesSmsNotification() {
            Notification n = NotificationFactory.create(NotifType.SMS);
            assertInstanceOf(SmsNotification.class, n);
            assertEquals("SMS", n.type());
        }

        @Test
        @DisplayName("7c. Factory creates an EmailNotification for EMAIL type")
        void factoryCreatesEmailNotification() {
            Notification n = NotificationFactory.create(NotifType.EMAIL);
            assertInstanceOf(EmailNotification.class, n);
            assertEquals("EMAIL", n.type());
        }

        @Test
        @DisplayName("7d. Factory creates a SlackNotification for SLACK type")
        void factoryCreatesSlackNotification() {
            Notification n = NotificationFactory.create(NotifType.SLACK);
            assertInstanceOf(SlackNotification.class, n);
            assertEquals("SLACK", n.type());
        }

        @Test
        @DisplayName("7e. Factory returns a new instance on each call")
        void factoryReturnsNewInstanceEachTime() {
            Notification a = NotificationFactory.create(NotifType.EMAIL);
            Notification b = NotificationFactory.create(NotifType.EMAIL);
            assertNotSame(a, b, "Factory should produce distinct instances");
        }
    }

    // =========================================================
    //  8. SOLID / DIP: OrderService with mock repository
    // =========================================================

    @Nested
    @DisplayName("8. SOLID — DIP enables testing with a mock repository")
    class SolidDipTests {

        private RecordingOrderRepository mockRepo;
        private DP_OrderService          service;

        @BeforeEach
        void setUp() {
            mockRepo = new RecordingOrderRepository();
            // DIP: inject the recording (mock) repository — no real DB needed
            service  = new DP_OrderService(mockRepo);
        }

        @Test
        @DisplayName("8a. placeOrder saves the order to the repository")
        void placeOrderSavesOrderToRepository() {
            DP_Order order = new DP_Order("TEST-001", 59.99);
            service.placeOrder(order);

            assertEquals(1, mockRepo.savedCount(),
                    "Repository should have received exactly one save call");
        }

        @Test
        @DisplayName("8b. placeOrder sets order status to CONFIRMED")
        void placeOrderSetsStatusToConfirmed() {
            DP_Order order = new DP_Order("TEST-002", 29.99);
            service.placeOrder(order);

            assertEquals("CONFIRMED", mockRepo.lastSaved().getStatus());
        }

        @Test
        @DisplayName("8c. getOrder retrieves an order that was placed")
        void getOrderRetrievesPlacedOrder() {
            DP_Order order = new DP_Order("TEST-003", 14.99);
            service.placeOrder(order);

            Optional<DP_Order> retrieved = service.getOrder("TEST-003");
            assertTrue(retrieved.isPresent());
            assertEquals(14.99, retrieved.get().getTotal(), 0.001);
        }

        @Test
        @DisplayName("8d. getOrder returns empty for an unknown order ID")
        void getOrderEmptyForUnknownId() {
            assertTrue(service.getOrder("GHOST-999").isEmpty());
        }

        @Test
        @DisplayName("8e. Multiple orders are all saved independently")
        void multipleOrdersSavedIndependently() {
            service.placeOrder(new DP_Order("A1", 10.0));
            service.placeOrder(new DP_Order("A2", 20.0));
            service.placeOrder(new DP_Order("A3", 30.0));

            assertEquals(3, mockRepo.savedCount());
            assertTrue(service.getOrder("A1").isPresent());
            assertTrue(service.getOrder("A2").isPresent());
            assertTrue(service.getOrder("A3").isPresent());
        }

        @Test
        @DisplayName("8f. DIP demo: different repository impl can be swapped without changing service")
        void differentRepositoryCanBeInjected() {
            // A 'failing' repository that always throws — verifies the service
            // delegates saving and doesn't do it itself
            DP_OrderRepository alwaysThrows = new DP_OrderRepository() {
                @Override public void save(DP_Order o) {
                    throw new RuntimeException("Simulated DB failure");
                }
                @Override public Optional<DP_Order> findById(String id) {
                    return Optional.empty();
                }
            };

            DP_OrderService fragileService = new DP_OrderService(alwaysThrows);
            assertThrows(RuntimeException.class,
                    () -> fragileService.placeOrder(new DP_Order("X1", 5.0)),
                    "Service must delegate to repository and propagate its exception");
        }
    }
}
