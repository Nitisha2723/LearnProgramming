"""
Notification System — Builder Pattern

Fluent builder for constructing Notification objects.
"""

from .models import Notification, Recipient, NotificationPriority


class NotificationBuilder:
    """
    Fluent builder for Notification objects.

    Enforces required fields (subject, body, recipient) at build time.
    Makes optional fields easy to set with method chaining.

    Example:
        notification = (
            NotificationBuilder()
            .to(recipient)
            .subject("Your order shipped!")
            .body("Order #12345 is on its way.")
            .via("email", "sms")
            .priority(NotificationPriority.HIGH)
            .with_metadata({"order_id": "12345"})
            .build()
        )
    """

    def __init__(self):
        self._recipient: Recipient | None = None
        self._subject: str = ""
        self._body: str = ""
        self._channels: list[str] = []
        self._priority = NotificationPriority.NORMAL
        self._template: str | None = None
        self._metadata: dict = {}

    def to(self, recipient: Recipient) -> "NotificationBuilder":
        """Set the notification recipient."""
        self._recipient = recipient
        return self

    def subject(self, subject: str) -> "NotificationBuilder":
        """Set the notification subject line."""
        if not subject:
            raise ValueError("Subject cannot be empty")
        self._subject = subject
        return self

    def body(self, body: str) -> "NotificationBuilder":
        """Set the notification body text."""
        if not body:
            raise ValueError("Body cannot be empty")
        self._body = body
        return self

    def via(self, *channels: str) -> "NotificationBuilder":
        """Set the channels to send through (e.g., 'email', 'sms', 'push')."""
        valid_channels = {"email", "sms", "push", "console"}
        for channel in channels:
            if channel not in valid_channels:
                raise ValueError(f"Unknown channel: '{channel}'. Valid: {valid_channels}")
        self._channels = list(channels)
        return self

    def priority(self, priority: NotificationPriority) -> "NotificationBuilder":
        """Set the notification priority."""
        self._priority = priority
        return self

    def urgent(self) -> "NotificationBuilder":
        """Shorthand for priority(NotificationPriority.URGENT)."""
        self._priority = NotificationPriority.URGENT
        return self

    def template(self, template_name: str) -> "NotificationBuilder":
        """Set a template name for rendering."""
        self._template = template_name
        return self

    def with_metadata(self, metadata: dict) -> "NotificationBuilder":
        """Attach arbitrary metadata to the notification."""
        self._metadata.update(metadata)
        return self

    def build(self) -> Notification:
        """Validate and construct the Notification object.

        Raises:
            ValueError: If required fields are missing.
        """
        errors = []
        if not self._recipient:
            errors.append("recipient (call .to(recipient))")
        if not self._subject:
            errors.append("subject (call .subject(...))")
        if not self._body:
            errors.append("body (call .body(...))")
        if not self._channels:
            errors.append("at least one channel (call .via(...))")

        if errors:
            raise ValueError(f"Missing required fields: {', '.join(errors)}")

        return Notification(
            subject=self._subject,
            body=self._body,
            recipient=self._recipient,
            channels=list(self._channels),
            priority=self._priority,
            template=self._template,
            metadata=dict(self._metadata),
        )

    def reset(self) -> "NotificationBuilder":
        """Reset the builder to create a new notification."""
        self.__init__()
        return self
