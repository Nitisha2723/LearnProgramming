# Mini-Project: Notification System

A fully working notification system that demonstrates all the design patterns from Module 06.

## What This Project Demonstrates

| Pattern | Where Used |
|---------|-----------|
| Observer | `NotificationBus` — events flow to multiple handlers |
| Strategy | `NotificationChannel` — email, SMS, push are interchangeable strategies |
| Factory | `NotificationChannelFactory` — creates the right channel from config |
| Builder | `NotificationBuilder` — constructs notifications step by step |
| Repository | `NotificationRepository` — stores and retrieves notification history |
| Template Method | `BaseNotificationChannel` — defines send flow with hooks |
| SOLID (all 5) | Throughout the entire design |

## System Overview

```
User or System Event
        │
        ▼
NotificationBus (Observer pattern)
        │
        │  publishes to
        ▼
NotificationService (orchestrator)
        │
        ├──► EmailChannel (Strategy)
        ├──► SmsChannel (Strategy)
        └──► PushChannel (Strategy)
                │
                ▼
        NotificationRepository (stores history)
```

## Files

```
notification_system/
├── models.py          — Notification, Recipient, NotificationStatus
├── channels.py        — Channel strategies: Email, SMS, Push, Console
├── builder.py         — NotificationBuilder (fluent)
├── repository.py      — NotificationRepository
├── bus.py             — NotificationBus (Observer)
├── service.py         — NotificationService (orchestrator)
├── factory.py         — NotificationChannelFactory
└── demo.py            — Complete working demonstration
```

## Running the Demo

```bash
cd 06-design/mini-project/notification_system/
python demo.py
```

## Key Learning Points

1. **Observer (NotificationBus):** Multiple components can react to the same event without knowing about each other.

2. **Strategy (channels):** Adding a new notification channel (say, Slack or Teams) means creating a new class, not modifying existing code.

3. **Builder (NotificationBuilder):** Complex notification objects are built incrementally with a clean, readable API.

4. **Repository (NotificationRepository):** The system does not know or care whether notifications are stored in memory, SQLite, or a real database.

5. **Factory:** Channel creation is centralized — callers do not need to know how channels are configured.
