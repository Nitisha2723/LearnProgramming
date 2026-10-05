"""
Repository Pattern — Python Examples

UserRepository with in-memory and SQLite implementations.
"""

from dataclasses import dataclass, field
from datetime import datetime
from typing import Protocol
import uuid


# ─── Domain Model ────────────────────────────────────────────────────────────

@dataclass
class User:
    username: str
    email: str
    role: str = "user"
    is_active: bool = True
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    created_at: datetime = field(default_factory=datetime.now)

    def deactivate(self) -> None:
        self.is_active = False

    def promote_to_admin(self) -> None:
        self.role = "admin"


# ─── Repository Interface (the "Port") ────────────────────────────────────────

class UserRepository(Protocol):
    def save(self, user: User) -> None: ...
    def find_by_id(self, user_id: str) -> User | None: ...
    def find_by_email(self, email: str) -> User | None: ...
    def find_all(self) -> list[User]: ...
    def find_by_role(self, role: str) -> list[User]: ...
    def delete(self, user_id: str) -> None: ...
    def count(self) -> int: ...


# ─── In-Memory Implementation ─────────────────────────────────────────────────

class InMemoryUserRepository:
    """Fast, dependency-free repository — perfect for unit tests."""

    def __init__(self):
        self._users: dict[str, User] = {}

    def save(self, user: User) -> None:
        self._users[user.id] = user

    def find_by_id(self, user_id: str) -> User | None:
        return self._users.get(user_id)

    def find_by_email(self, email: str) -> User | None:
        return next(
            (u for u in self._users.values() if u.email == email),
            None,
        )

    def find_all(self) -> list[User]:
        return list(self._users.values())

    def find_by_role(self, role: str) -> list[User]:
        return [u for u in self._users.values() if u.role == role]

    def delete(self, user_id: str) -> None:
        self._users.pop(user_id, None)

    def count(self) -> int:
        return len(self._users)


# ─── SQLite Implementation ────────────────────────────────────────────────────

class SqliteUserRepository:
    """SQLite-backed repository. Same interface as InMemoryUserRepository."""

    def __init__(self, db_path: str = ":memory:"):
        import sqlite3
        self._conn = sqlite3.connect(db_path)
        self._conn.row_factory = sqlite3.Row
        self._create_table()

    def _create_table(self) -> None:
        self._conn.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                username TEXT NOT NULL UNIQUE,
                email TEXT NOT NULL UNIQUE,
                role TEXT NOT NULL DEFAULT 'user',
                is_active INTEGER NOT NULL DEFAULT 1,
                created_at TEXT NOT NULL
            )
        """)
        self._conn.commit()

    def save(self, user: User) -> None:
        self._conn.execute("""
            INSERT OR REPLACE INTO users (id, username, email, role, is_active, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """, (
            user.id, user.username, user.email,
            user.role, int(user.is_active), user.created_at.isoformat(),
        ))
        self._conn.commit()

    def find_by_id(self, user_id: str) -> User | None:
        row = self._conn.execute(
            "SELECT * FROM users WHERE id = ?", (user_id,)
        ).fetchone()
        return self._row_to_user(row) if row else None

    def find_by_email(self, email: str) -> User | None:
        row = self._conn.execute(
            "SELECT * FROM users WHERE email = ?", (email,)
        ).fetchone()
        return self._row_to_user(row) if row else None

    def find_all(self) -> list[User]:
        rows = self._conn.execute("SELECT * FROM users ORDER BY created_at").fetchall()
        return [self._row_to_user(row) for row in rows]

    def find_by_role(self, role: str) -> list[User]:
        rows = self._conn.execute(
            "SELECT * FROM users WHERE role = ?", (role,)
        ).fetchall()
        return [self._row_to_user(row) for row in rows]

    def delete(self, user_id: str) -> None:
        self._conn.execute("DELETE FROM users WHERE id = ?", (user_id,))
        self._conn.commit()

    def count(self) -> int:
        return self._conn.execute("SELECT COUNT(*) FROM users").fetchone()[0]

    def _row_to_user(self, row) -> User:
        return User(
            id=row["id"],
            username=row["username"],
            email=row["email"],
            role=row["role"],
            is_active=bool(row["is_active"]),
            created_at=datetime.fromisoformat(row["created_at"]),
        )


# ─── Service (uses repository through the interface) ─────────────────────────

class UserService:
    def __init__(self, repo: UserRepository):
        self._repo = repo

    def register(self, username: str, email: str) -> User:
        if self._repo.find_by_email(email):
            raise ValueError(f"Email {email} is already registered")
        user = User(username=username, email=email)
        self._repo.save(user)
        return user

    def get_user(self, user_id: str) -> User:
        user = self._repo.find_by_id(user_id)
        if not user:
            raise ValueError(f"User {user_id} not found")
        return user

    def deactivate_user(self, user_id: str) -> None:
        user = self.get_user(user_id)
        user.deactivate()
        self._repo.save(user)

    def list_admins(self) -> list[User]:
        return self._repo.find_by_role("admin")

    def get_stats(self) -> dict:
        all_users = self._repo.find_all()
        return {
            "total": self._repo.count(),
            "active": sum(1 for u in all_users if u.is_active),
            "admins": len(self._repo.find_by_role("admin")),
        }


# ─── Demo ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    print("=== In-Memory Repository ===")
    service = UserService(InMemoryUserRepository())

    alice = service.register("alice", "alice@example.com")
    bob = service.register("bob", "bob@example.com")

    bob_user = service.get_user(bob.id)
    bob_user.promote_to_admin()
    service._repo.save(bob_user)

    print(f"Stats: {service.get_stats()}")
    print(f"Admins: {[u.username for u in service.list_admins()]}")

    service.deactivate_user(alice.id)
    print(f"Alice active: {service.get_user(alice.id).is_active}")

    print("\n=== SQLite Repository (same interface!) ===")
    service_sql = UserService(SqliteUserRepository(":memory:"))
    charlie = service_sql.register("charlie", "charlie@example.com")
    print(f"Registered: {charlie.username} (id: {charlie.id[:8]}...)")
    print(f"Stats: {service_sql.get_stats()}")
