"""
Notification System — Demo

Complete demonstration of the notification system.
Run this file directly: python demo.py

Shows:
- Creating recipients with different contact methods
- Building notifications with the fluent builder
- Sending through multiple channels
- Observer callbacks for success/failure
- Statistics from the repository
"""

import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from notification_system import (
    Recipient,
    NotificationBuilder,
    NotificationPriority,
    NotificationStatus,
    NotificationService,
    NotificationChannelFactory,
    InMemoryNotificationRepository,
)


def demo_basic_sending():
    """Demo 1: Basic notification sending."""
    print("\n" + "="*60)
    print("DEMO 1: Basic Notification Sending")
    print("="*60)

    # Create the service with all channels
    service = NotificationChannelFactory.create_development_service()

    # Track events (Observer pattern)
    sent_count = [0]
    failed_count = [0]

    service.on_success(lambda r: print(f"  ✓ Sent via {r.channel}"))
    service.on_failure(lambda r: print(f"  ✗ Failed via {r.channel}: {r.error_message}"))
    service.on_success(lambda r: sent_count.__setitem__(0, sent_count[0] + 1))
    service.on_failure(lambda r: failed_count.__setitem__(0, failed_count[0] + 1))

    # Recipient with all contact methods
    alice = Recipient(
        name="Alice",
        email="alice@example.com",
        phone="+15551234567",
        push_token="device-token-abc-123",
    )

    # Build a notification using the fluent builder
    notification = (
        NotificationBuilder()
        .to(alice)
        .subject("Welcome to our platform!")
        .body("Hi Alice! Your account has been created successfully.")
        .via("email", "sms", "push")
        .priority(NotificationPriority.NORMAL)
        .with_metadata({"event": "user_registered", "user_id": "alice-123"})
        .build()
    )

    print(f"\nSending notification: {notification.subject}")
    records = service.send(notification)

    print(f"\nResults: {sent_count[0]} sent, {failed_count[0]} failed")


def demo_urgent_notification():
    """Demo 2: High-priority notification."""
    print("\n" + "="*60)
    print("DEMO 2: Urgent Notification")
    print("="*60)

    service = NotificationChannelFactory.create_development_service()

    # Recipient with only email (no phone or push token)
    bob = Recipient(
        name="Bob",
        email="bob@example.com",
        # No phone or push_token — SMS and push will fail gracefully
    )

    urgent = (
        NotificationBuilder()
        .to(bob)
        .subject("Security Alert: New login detected")
        .body("A new login was detected from IP 192.168.1.1 at 14:32 UTC.")
        .via("email", "sms", "push")  # sms and push will fail (no contact info)
        .urgent()
        .with_metadata({"ip": "192.168.1.1", "event": "login"})
        .build()
    )

    records = service.send(urgent)

    print("\nChannel results:")
    for record in records:
        status_icon = "✓" if record.status == NotificationStatus.SENT else "✗"
        print(f"  {status_icon} {record.channel}: {record.status.value}")
        if record.error_message:
            print(f"    Reason: {record.error_message}")


def demo_multiple_recipients():
    """Demo 3: Sending to multiple users."""
    print("\n" + "="*60)
    print("DEMO 3: Multiple Recipients")
    print("="*60)

    service = NotificationChannelFactory.create_development_service()

    users = [
        Recipient("Alice", "alice@example.com", "+15551111111", "token-alice"),
        Recipient("Bob", "bob@example.com", "+15552222222"),
        Recipient("Charlie", "charlie@example.com"),
    ]

    builder = NotificationBuilder()

    for user in users:
        notification = (
            builder
            .to(user)
            .subject("System Maintenance Tonight")
            .body(f"Hi {user.name}, scheduled maintenance at 11 PM UTC.")
            .via("email", "console")
            .priority(NotificationPriority.HIGH)
            .build()
        )
        service.send(notification)
        builder.reset()

    print("\n📊 Statistics:")
    stats = service.get_stats()
    print(f"  Total attempts: {stats['total_attempts']}")
    print(f"  Successful: {stats['successful']}")
    print(f"  Failed: {stats['failed']}")
    print(f"  Success rate: {stats['success_rate']}")
    print(f"  By channel: {stats['by_channel']}")


def demo_builder_validation():
    """Demo 4: Builder validates required fields."""
    print("\n" + "="*60)
    print("DEMO 4: Builder Validation")
    print("="*60)

    builder = NotificationBuilder()

    try:
        notification = builder.build()  # Missing everything
    except ValueError as e:
        print(f"Caught expected error: {e}")

    alice = Recipient("Alice", "alice@example.com")
    try:
        notification = (
            builder
            .to(alice)
            .subject("Test")
            .body("Body text")
            # Missing .via(...)
            .build()
        )
    except ValueError as e:
        print(f"Caught expected error: {e}")

    # Correct — all required fields present
    notification = (
        builder
        .to(alice)
        .subject("Test")
        .body("Body text")
        .via("console")
        .build()
    )
    print(f"Valid notification created: {notification.id[:8]}...")


if __name__ == "__main__":
    print("NOTIFICATION SYSTEM DEMO")
    print("Demonstrates: Observer, Strategy, Builder, Factory, Repository, Template Method")

    demo_basic_sending()
    demo_urgent_notification()
    demo_multiple_recipients()
    demo_builder_validation()

    print("\n" + "="*60)
    print("Demo complete!")
