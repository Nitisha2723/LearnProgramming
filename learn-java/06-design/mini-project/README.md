# Mini-Project: E-Commerce Notification System

This mini-project is the capstone for Module 06 — Design Patterns & Software Engineering. It brings together every major pattern you've learned into a cohesive, production-quality system.

---

## What You're Building

An **e-commerce notification system** that can send Email, SMS, Push, and Slack notifications for events like:

- Order confirmations
- Shipping updates
- Password resets
- Promotional campaigns
- Critical security alerts

This is the kind of system you'd find at any real e-commerce company (Amazon, Shopify, etc.). It handles multiple notification channels, respects user preferences, tracks delivery, logs everything, and collects metrics.

---

## Patterns in Action

| File | Pattern(s) | What It Demonstrates |
|------|-----------|----------------------|
| `Notification.java` | **Builder** | Immutable objects with optional parameters |
| `NotificationChannel.java` | **Strategy** | Swap delivery channels at runtime |
| `NotificationFactory.java` | **Factory Method** | Encapsulate creation logic |
| `NotificationRepository.java` | **Repository** | Abstract data access |
| `NotificationService.java` | **Observer + DIP** | Event-driven architecture |
| `NotificationListeners.java` | **Observer** | Audit logging, metrics, alerts |
| `UserPreferenceManager.java` | **OOP design** | Respecting user preferences |
| `NotificationSystemDemo.java` | **Everything** | Full integration |

---

## SOLID Throughout

- **S** — Each class has one job: Service sends, Repository stores, Factory creates, Channels deliver
- **O** — Add a new channel (WhatsApp) by adding a class, not modifying existing code
- **L** — Every NotificationChannel can replace any other without breaking the service
- **I** — NotificationListener only has notification-related methods
- **D** — NotificationService depends on interfaces, not concrete classes

---

## How to Compile and Run

```bash
# From the notification-system directory
cd mini-project/notification-system

# Compile all files
javac *.java

# Run the demo
java NotificationSystemDemo
```

Expected output: A series of notification scenarios with audit logs, delivery results, and a final metrics report.

---

## Learning Objectives

As you read through the code, look for:

1. **Builder** (`Notification.java`): How the private constructor forces use of the Builder, and how `build()` validates required fields.

2. **Strategy** (`NotificationChannel.java`): How `NotificationService` calls `channel.send(notification)` without knowing which channel it is. Adding WhatsApp would require zero changes to `NotificationService`.

3. **Observer** (`NotificationService.java` + `NotificationListeners.java`): How `NotificationService` calls `listener.onNotificationSent()` without knowing what the listeners do. The AuditLogger and MetricsCollector are completely decoupled.

4. **Factory** (`NotificationFactory.java`): How `createOrderConfirmation()` encapsulates all the details of building a proper order confirmation notification.

5. **Repository** (`NotificationRepository.java`): How `NotificationService` stores notifications without knowing if it's using a Map, a database, or a cloud service.

6. **DIP** (`NotificationService.java`): The service's constructor takes interfaces, not implementations. This means you can test it with mock objects, no real email or SMS needed.

---

## Extending the System

Once you understand the code, try these extensions:

1. Add a `WhatsAppNotificationChannel` — zero existing files need to change
2. Add a `DatabaseNotificationRepository` — swap it in the demo's setup
3. Add a `RateLimiterListener` that blocks more than 10 notifications per minute to the same user
4. Add retry logic to `NotificationService` for failed deliveries
5. Add a `TemplateEngine` that renders notification bodies from templates

These extensions are intentionally left as exercises.
