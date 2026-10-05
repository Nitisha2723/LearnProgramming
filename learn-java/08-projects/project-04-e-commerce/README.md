# Project 04: E-Commerce System (Portfolio Project)

A **production-quality** e-commerce backend demonstrating four classic Gang-of-Four design patterns in a cohesive, testable Java application.

---

## System Description

Full e-commerce platform supporting:
- Product catalog with category hierarchy
- Customer registration
- Shopping cart (add/remove/update items)
- Order placement with configurable discount strategies
- Payment processing through a factory abstraction
- Order lifecycle management (PENDING → CONFIRMED → SHIPPED → DELIVERED)
- Event-driven notifications via the Observer pattern

---

## Design Patterns

| Pattern | Where Used | Why |
|---------|-----------|-----|
| **Strategy** | `DiscountStrategy` hierarchy | Swap discount algorithms at runtime without changing `OrderService` |
| **Observer** | `OrderEventListener` + listeners | Decouple notifications and inventory updates from order business logic |
| **Builder** | `OrderBuilder` | Construct complex `Order` objects step-by-step with validation |
| **Factory** | `PaymentFactory` | Centralise payment creation and processing, hide implementation details |

---

## Architecture

```
src/
├── model/          # Domain objects
│   ├── Category, Product, Customer
│   ├── CartItem, Cart
│   ├── OrderItem, Order
│   └── Payment
├── repository/     # Interfaces + in-memory implementations
├── strategy/       # Discount algorithms (Strategy pattern)
├── observer/       # Order event listeners (Observer pattern)
├── builder/        # OrderBuilder (Builder pattern)
├── factory/        # PaymentFactory (Factory pattern)
├── service/        # Business logic
│   ├── ProductService, CartService, OrderService, PaymentService
└── Main.java       # Full demo

tests/
├── ProductServiceTest.java
├── CartServiceTest.java
├── OrderServiceTest.java
└── DiscountStrategyTest.java
```

---

## What Makes This Production-Quality

1. **Immutable order items** — `OrderItem` fields are final; the order cannot be mutated after creation
2. **Inventory validation** — `CartService` checks stock availability before adding items
3. **Atomic order placement** — cart is cleared only after order is successfully persisted
4. **Observer decoupling** — adding a new notification channel (SMS, Slack) requires zero changes to `OrderService`
5. **Validated Builder** — `OrderBuilder.build()` throws `IllegalStateException` for missing required fields
6. **Comprehensive tests** — JUnit 5 + Mockito, covering success and failure paths

---

## How to Demonstrate in Interviews

> "I built a full e-commerce backend to practice design patterns. The Strategy pattern lets me inject a `TieredDiscountStrategy` or a `PercentageDiscountStrategy` at runtime — the order service never changes. The Observer pattern means email and inventory updates are fully decoupled from order placement: I just call `notifyOrderPlaced()` and every registered listener reacts. The Builder ensures orders are always complete and valid before they're persisted."

Key talking points:
- Why you chose each pattern (not just that you used them)
- Trade-offs: in-memory repositories vs. a real database
- Extension points: adding a new payment method → one new `PaymentMethod` enum value + handling in `PaymentFactory`

---

## Running the Demo

```bash
cd src
javac model/*.java repository/*.java strategy/*.java observer/*.java builder/*.java factory/*.java service/*.java Main.java
java Main
```

## Running Tests

```bash
# Requires JUnit 5 + Mockito on classpath
javac -cp .:junit-5.jar:mockito.jar ../src/**/*.java tests/*.java
java -cp .:junit-5.jar org.junit.platform.console.ConsoleLauncher --scan-classpath
```
