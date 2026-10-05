"""
Notification System — Models

Domain objects for the notification system.
"""

from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum
import uuid


class NotificationStatus(Enum):
    PENDING = "pending"
    SENT = "sent"
    FAILED = "failed"
    DELIVERED = "delivered"


class NotificationPriority(Enum):
    LOW = "low"
    NORMAL = "normal"
    HIGH = "high"
    URGENT = "urgent"


@dataclass
class Recipient:
    """Who receives notifications."""
    name: str
    email: str
    phone: str = ""
    push_token: str = ""
    id: str = field(default_factory=lambda: str(uuid.uuid4()))

    def __repr__(self) -> str:
        return f"Recipient({self.name}, {self.email})"


@dataclass
class Notification:
    """A notification to be sent through one or more channels."""
    subject: str
    body: str
    recipient: Recipient
    channels: list[str] = field(default_factory=list)
    priority: NotificationPriority = NotificationPriority.NORMAL
    template: str | None = None
    metadata: dict = field(default_factory=dict)
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    created_at: datetime = field(default_factory=datetime.now)

    def __repr__(self) -> str:
        return f"Notification('{self.subject}', to={self.recipient.name}, channels={self.channels})"


@dataclass
class NotificationRecord:
    """Audit record: what happened when a notification was sent."""
    notification_id: str
    channel: str
    status: NotificationStatus
    sent_at: datetime = field(default_factory=datetime.now)
    error_message: str = ""
    delivery_id: str = field(default_factory=lambda: str(uuid.uuid4()))

    def __repr__(self) -> str:
        return f"NotificationRecord({self.channel}: {self.status.value})"
