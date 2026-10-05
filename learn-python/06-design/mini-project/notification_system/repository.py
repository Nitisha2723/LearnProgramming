"""
Notification System — Repository Pattern

Stores and retrieves notification history.
"""

from datetime import datetime
from typing import Protocol

from .models import Notification, NotificationRecord, NotificationStatus


class NotificationRepository(Protocol):
    """Abstract repository for notification storage."""

    def save_notification(self, notification: Notification) -> None: ...
    def save_record(self, record: NotificationRecord) -> None: ...
    def find_records_for(self, notification_id: str) -> list[NotificationRecord]: ...
    def find_failed(self) -> list[NotificationRecord]: ...
    def count_sent_by_channel(self) -> dict[str, int]: ...


class InMemoryNotificationRepository:
    """In-memory implementation. Fast. No dependencies. Perfect for testing."""

    def __init__(self):
        self._notifications: dict[str, Notification] = {}
        self._records: list[NotificationRecord] = []

    def save_notification(self, notification: Notification) -> None:
        self._notifications[notification.id] = notification

    def save_record(self, record: NotificationRecord) -> None:
        self._records.append(record)

    def find_records_for(self, notification_id: str) -> list[NotificationRecord]:
        return [r for r in self._records if r.notification_id == notification_id]

    def find_failed(self) -> list[NotificationRecord]:
        return [r for r in self._records if r.status == NotificationStatus.FAILED]

    def find_by_channel(self, channel: str) -> list[NotificationRecord]:
        return [r for r in self._records if r.channel == channel]

    def count_sent_by_channel(self) -> dict[str, int]:
        counts: dict[str, int] = {}
        for record in self._records:
            counts[record.channel] = counts.get(record.channel, 0) + 1
        return counts

    def get_all_notifications(self) -> list[Notification]:
        return list(self._notifications.values())

    def get_stats(self) -> dict:
        total = len(self._records)
        sent = sum(1 for r in self._records if r.status == NotificationStatus.SENT)
        failed = sum(1 for r in self._records if r.status == NotificationStatus.FAILED)
        return {
            "total_attempts": total,
            "successful": sent,
            "failed": failed,
            "success_rate": f"{(sent / total * 100):.1f}%" if total > 0 else "N/A",
            "by_channel": self.count_sent_by_channel(),
        }
