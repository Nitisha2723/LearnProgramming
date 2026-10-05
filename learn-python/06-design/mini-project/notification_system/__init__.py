"""
Notification System — Package init
"""

from .models import Notification, Recipient, NotificationStatus, NotificationPriority
from .builder import NotificationBuilder
from .channels import EmailChannel, SmsChannel, PushChannel, ConsoleChannel
from .repository import InMemoryNotificationRepository
from .service import NotificationService, NotificationChannelFactory

__all__ = [
    "Notification",
    "Recipient",
    "NotificationStatus",
    "NotificationPriority",
    "NotificationBuilder",
    "EmailChannel",
    "SmsChannel",
    "PushChannel",
    "ConsoleChannel",
    "InMemoryNotificationRepository",
    "NotificationService",
    "NotificationChannelFactory",
]
