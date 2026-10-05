"""
Notification System — Service

The orchestrator. Uses all other components to send notifications.
"""

import logging
from typing import Callable

from .models import Notification, NotificationRecord, NotificationStatus
from .channels import NotificationChannel
from .repository import InMemoryNotificationRepository

logger = logging.getLogger(__name__)


class NotificationService:
    """
    Orchestrates notification delivery.

    Responsibilities:
    - Route notifications to the correct channels
    - Record all attempts and outcomes
    - Emit events for observers (success, failure)

    Uses:
    - Strategy pattern (channels are interchangeable)
    - Observer pattern (callbacks for events)
    - Repository pattern (result storage)
    """

    def __init__(
        self,
        repository: InMemoryNotificationRepository | None = None,
    ):
        self._channels: dict[str, NotificationChannel] = {}
        self._repository = repository or InMemoryNotificationRepository()
        self._success_handlers: list[Callable[[NotificationRecord], None]] = []
        self._failure_handlers: list[Callable[[NotificationRecord], None]] = []

    def register_channel(self, channel: NotificationChannel) -> None:
        """Register a channel strategy."""
        self._channels[channel.channel_name()] = channel
        logger.info(f"Registered channel: {channel.channel_name()}")

    def on_success(self, handler: Callable[[NotificationRecord], None]) -> None:
        """Subscribe to successful send events."""
        self._success_handlers.append(handler)

    def on_failure(self, handler: Callable[[NotificationRecord], None]) -> None:
        """Subscribe to failed send events."""
        self._failure_handlers.append(handler)

    def send(self, notification: Notification) -> list[NotificationRecord]:
        """Send a notification through all its specified channels.

        Returns a list of NotificationRecord, one per channel attempted.
        """
        self._repository.save_notification(notification)
        records: list[NotificationRecord] = []

        for channel_name in notification.channels:
            channel = self._channels.get(channel_name)

            if not channel:
                logger.warning(f"Unknown channel: '{channel_name}' — skipping")
                failed_record = NotificationRecord(
                    notification_id=notification.id,
                    channel=channel_name,
                    status=NotificationStatus.FAILED,
                    error_message=f"Channel '{channel_name}' is not registered",
                )
                self._repository.save_record(failed_record)
                records.append(failed_record)
                continue

            record = channel.send(notification)
            self._repository.save_record(record)
            records.append(record)

            if record.status == NotificationStatus.SENT:
                for handler in self._success_handlers:
                    handler(record)
            else:
                for handler in self._failure_handlers:
                    handler(record)

        return records

    def get_stats(self) -> dict:
        """Return delivery statistics."""
        return self._repository.get_stats()

    def get_failed_notifications(self) -> list[NotificationRecord]:
        """Return all failed notification records."""
        return self._repository.find_failed()


class NotificationChannelFactory:
    """
    Factory for creating notification channel instances.

    Centralizes channel creation and configuration.
    Uses environment variables or config for production settings.
    """

    @staticmethod
    def create_console_channel():
        from .channels import ConsoleChannel
        return ConsoleChannel()

    @staticmethod
    def create_email_channel(from_address: str | None = None):
        import os
        from .channels import EmailChannel
        address = from_address or os.getenv("EMAIL_FROM", "noreply@example.com")
        return EmailChannel(from_address=address)

    @staticmethod
    def create_sms_channel(from_number: str | None = None):
        import os
        from .channels import SmsChannel
        number = from_number or os.getenv("SMS_FROM_NUMBER", "+15550000000")
        return SmsChannel(from_number=number)

    @staticmethod
    def create_push_channel(app_id: str | None = None):
        import os
        from .channels import PushChannel
        app = app_id or os.getenv("PUSH_APP_ID", "myapp")
        return PushChannel(app_id=app)

    @classmethod
    def create_all_channels(cls) -> list[NotificationChannel]:
        """Create all available channels."""
        return [
            cls.create_console_channel(),
            cls.create_email_channel(),
            cls.create_sms_channel(),
            cls.create_push_channel(),
        ]

    @classmethod
    def create_development_service(cls) -> NotificationService:
        """Create a fully configured service for development."""
        service = NotificationService()
        for channel in cls.create_all_channels():
            service.register_channel(channel)
        return service
