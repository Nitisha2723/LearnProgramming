"""
Notification System — Channels (Strategy Pattern)

Each channel is a strategy: pluggable, independently testable, following the same interface.
Adding a new channel (Slack, Teams, WhatsApp) means creating a new class — no existing code changes.
"""

from abc import ABC, abstractmethod
from typing import Protocol
import logging

from .models import Notification, NotificationRecord, NotificationStatus

logger = logging.getLogger(__name__)


# ─── Channel Protocol (the Strategy interface) ────────────────────────────────

class NotificationChannel(Protocol):
    """Strategy interface: every channel must implement send()."""

    def send(self, notification: Notification) -> NotificationRecord:
        """Send a notification through this channel.

        Returns a NotificationRecord indicating success or failure.
        Should NOT raise exceptions — failures are captured in the record.
        """
        ...

    def channel_name(self) -> str:
        """Return the name of this channel."""
        ...

    def can_send_to(self, notification: Notification) -> bool:
        """Return True if this channel can send to the notification's recipient."""
        ...


# ─── Base implementation with Template Method ────────────────────────────────

class BaseChannel(ABC):
    """
    Abstract base channel using Template Method pattern.

    The send() method defines the algorithm:
    1. Validate the recipient
    2. Format the message
    3. Deliver it
    4. Record the result

    Subclasses override _format() and _deliver().
    """

    def send(self, notification: Notification) -> NotificationRecord:
        if not self.can_send_to(notification):
            return NotificationRecord(
                notification_id=notification.id,
                channel=self.channel_name(),
                status=NotificationStatus.FAILED,
                error_message=f"Recipient {notification.recipient.name} not reachable via {self.channel_name()}",
            )

        try:
            message = self._format(notification)
            delivery_id = self._deliver(notification.recipient, message, notification)

            record = NotificationRecord(
                notification_id=notification.id,
                channel=self.channel_name(),
                status=NotificationStatus.SENT,
                delivery_id=delivery_id,
            )
            logger.info(f"Sent {self.channel_name()} notification to {notification.recipient.name}")
            return record

        except Exception as e:
            logger.error(f"Failed to send {self.channel_name()} notification: {e}")
            return NotificationRecord(
                notification_id=notification.id,
                channel=self.channel_name(),
                status=NotificationStatus.FAILED,
                error_message=str(e),
            )

    @abstractmethod
    def channel_name(self) -> str:
        ...

    @abstractmethod
    def can_send_to(self, notification: Notification) -> bool:
        ...

    @abstractmethod
    def _format(self, notification: Notification) -> str:
        """Format the notification into a sendable message string."""
        ...

    @abstractmethod
    def _deliver(self, recipient, message: str, notification: Notification) -> str:
        """Deliver the formatted message. Return a delivery ID."""
        ...


# ─── Console Channel (for development / testing) ─────────────────────────────

class ConsoleChannel(BaseChannel):
    """Prints to console. Always succeeds. Perfect for development."""

    def channel_name(self) -> str:
        return "console"

    def can_send_to(self, notification: Notification) -> bool:
        return True  # Console can always "send"

    def _format(self, notification: Notification) -> str:
        priority_prefix = ""
        if notification.priority.value in ("high", "urgent"):
            priority_prefix = f"[{notification.priority.value.upper()}] "
        return f"{priority_prefix}{notification.subject}\n{notification.body}"

    def _deliver(self, recipient, message: str, notification: Notification) -> str:
        print(f"\n{'='*60}")
        print(f"[CONSOLE] To: {recipient.name} <{recipient.email}>")
        print(f"{message}")
        print(f"{'='*60}")
        return f"console-{notification.id[:8]}"


# ─── Email Channel ────────────────────────────────────────────────────────────

class EmailChannel(BaseChannel):
    """Sends email notifications.

    In production, this would use smtplib or an email API like SendGrid.
    Here it simulates sending.
    """

    def __init__(self, from_address: str = "noreply@example.com"):
        self._from = from_address

    def channel_name(self) -> str:
        return "email"

    def can_send_to(self, notification: Notification) -> bool:
        return bool(notification.recipient.email)

    def _format(self, notification: Notification) -> str:
        return (
            f"From: {self._from}\n"
            f"To: {notification.recipient.email}\n"
            f"Subject: {notification.subject}\n"
            f"\n{notification.body}"
        )

    def _deliver(self, recipient, message: str, notification: Notification) -> str:
        # In production: smtp.sendmail(...)
        print(f"[EMAIL] → {recipient.email}: {notification.subject}")
        return f"email-{notification.id[:8]}"


# ─── SMS Channel ──────────────────────────────────────────────────────────────

class SmsChannel(BaseChannel):
    """Sends SMS notifications.

    In production, would use Twilio, AWS SNS, etc.
    """

    MAX_LENGTH = 160

    def __init__(self, from_number: str = "+15550000000"):
        self._from = from_number

    def channel_name(self) -> str:
        return "sms"

    def can_send_to(self, notification: Notification) -> bool:
        return bool(notification.recipient.phone)

    def _format(self, notification: Notification) -> str:
        # SMS: subject + brief body, truncated to 160 chars
        text = f"{notification.subject}: {notification.body}"
        if len(text) > self.MAX_LENGTH:
            text = text[:self.MAX_LENGTH - 3] + "..."
        return text

    def _deliver(self, recipient, message: str, notification: Notification) -> str:
        # In production: twilio_client.messages.create(...)
        print(f"[SMS] → {recipient.phone}: {message[:50]}...")
        return f"sms-{notification.id[:8]}"


# ─── Push Notification Channel ────────────────────────────────────────────────

class PushChannel(BaseChannel):
    """Sends push notifications.

    In production, would use Firebase, APNs, etc.
    """

    def __init__(self, app_id: str = "myapp"):
        self._app_id = app_id

    def channel_name(self) -> str:
        return "push"

    def can_send_to(self, notification: Notification) -> bool:
        return bool(notification.recipient.push_token)

    def _format(self, notification: Notification) -> str:
        import json
        return json.dumps({
            "title": notification.subject,
            "body": notification.body[:200],
            "priority": notification.priority.value,
            "data": notification.metadata,
        })

    def _deliver(self, recipient, message: str, notification: Notification) -> str:
        # In production: firebase.send(token, payload)
        print(f"[PUSH] → {recipient.push_token[:12]}...: {notification.subject}")
        return f"push-{notification.id[:8]}"
