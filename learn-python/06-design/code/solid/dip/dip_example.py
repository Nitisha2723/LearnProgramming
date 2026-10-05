"""
Dependency Inversion Principle — Python Examples
"""

from typing import Protocol
from dataclasses import dataclass


# ─── VIOLATION ────────────────────────────────────────────────────────────────

class SqliteDatabase:
    """Concrete low-level module."""

    def query(self, sql: str) -> list[dict]:
        print(f"[SQLite] Executing: {sql}")
        return [{"id": 1, "name": "Alice"}]


class SmtpEmailSender:
    """Another concrete low-level module."""

    def send(self, to: str, subject: str, body: str) -> None:
        print(f"[SMTP] Sending to {to}: {subject}")


class UserServiceViolation:
    """VIOLATION: High-level module depends on concrete low-level modules."""

    def __init__(self):
        # Creates concrete dependencies internally — can't test without SQLite and SMTP!
        self._db = SqliteDatabase()
        self._email = SmtpEmailSender()

    def get_users(self) -> list[dict]:
        return self._db.query("SELECT * FROM users")

    def notify_user(self, user_id: str) -> None:
        users = self._db.query(f"SELECT * FROM users WHERE id = {user_id}")
        if users:
            self._email.send(users[0]["email"], "Hello", "Welcome!")


# ─── CORRECT: Depend on abstractions ──────────────────────────────────────────

@dataclass
class UserRecord:
    id: str
    name: str
    email: str


class UserStorage(Protocol):
    """Abstraction for user storage."""

    def get_all(self) -> list[UserRecord]: ...
    def get_by_id(self, user_id: str) -> UserRecord | None: ...
    def save(self, user: UserRecord) -> None: ...


class MessageSender(Protocol):
    """Abstraction for sending messages."""

    def send(self, recipient: str, subject: str, body: str) -> None: ...


# ─── Concrete Implementations ─────────────────────────────────────────────────

class InMemoryUserStorage:
    """Lightweight storage — perfect for testing."""

    def __init__(self, initial_data: list[UserRecord] | None = None):
        self._users: dict[str, UserRecord] = {}
        for user in (initial_data or []):
            self._users[user.id] = user

    def get_all(self) -> list[UserRecord]:
        return list(self._users.values())

    def get_by_id(self, user_id: str) -> UserRecord | None:
        return self._users.get(user_id)

    def save(self, user: UserRecord) -> None:
        self._users[user.id] = user


class ConsoleMessageSender:
    """Prints messages to console — good for development."""

    def send(self, recipient: str, subject: str, body: str) -> None:
        print(f"[Console] To: {recipient} | Subject: {subject}")
        print(f"[Console] Body: {body[:100]}")


# ─── High-Level Module ────────────────────────────────────────────────────────

class UserService:
    """High-level module: depends ONLY on abstractions."""

    def __init__(self, storage: UserStorage, messenger: MessageSender):
        self._storage = storage
        self._messenger = messenger

    def get_all_users(self) -> list[UserRecord]:
        return self._storage.get_all()

    def notify_user(self, user_id: str, message: str) -> bool:
        user = self._storage.get_by_id(user_id)
        if not user:
            return False
        self._messenger.send(user.email, "Notification", message)
        return True

    def create_user(self, name: str, email: str) -> UserRecord:
        import uuid
        user = UserRecord(id=str(uuid.uuid4()), name=name, email=email)
        self._storage.save(user)
        return user


# ─── Wiring ───────────────────────────────────────────────────────────────────

def create_development_service() -> UserService:
    return UserService(
        storage=InMemoryUserStorage(),
        messenger=ConsoleMessageSender(),
    )


if __name__ == "__main__":
    service = create_development_service()

    alice = service.create_user("Alice", "alice@example.com")
    bob = service.create_user("Bob", "bob@example.com")

    users = service.get_all_users()
    print(f"Users: {[u.name for u in users]}")

    service.notify_user(alice.id, "Welcome to our platform!")
    result = service.notify_user("nonexistent", "Hello?")
    print(f"Notify nonexistent user: {result}")
