"""
Singleton Pattern — Python Examples

Two approaches:
1. Module-level singleton (most Pythonic)
2. Metaclass-based singleton (for class-based needs)
"""

import threading
import logging


# ─── Approach 1: Module-Level Singleton (Most Pythonic) ───────────────────────
# When you import this module, the logger is created once and reused.
# Python's module system guarantees this.

_logger = logging.getLogger("myapp")
_logger.setLevel(logging.DEBUG)

_handler = logging.StreamHandler()
_handler.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(name)s: %(message)s"))

if not _logger.handlers:
    _logger.addHandler(_handler)


def get_app_logger() -> logging.Logger:
    """Return the application-wide logger singleton."""
    return _logger


# ─── Approach 2: Metaclass Singleton ──────────────────────────────────────────

class SingletonMeta(type):
    """
    Thread-safe metaclass for creating singletons.

    Usage:
        class MyClass(metaclass=SingletonMeta):
            ...
    """

    _instances: dict[type, object] = {}
    _lock: threading.Lock = threading.Lock()

    def __call__(cls, *args, **kwargs):
        # Double-checked locking pattern
        if cls not in cls._instances:
            with cls._lock:
                if cls not in cls._instances:
                    instance = super().__call__(*args, **kwargs)
                    cls._instances[cls] = instance
        return cls._instances[cls]


class DatabaseConnectionPool(metaclass=SingletonMeta):
    """
    Connection pool that should exist only once in the application.

    Only the first instantiation sets up the pool.
    Subsequent calls return the same pool.
    """

    def __init__(self, host: str = "localhost", port: int = 5432,
                 max_connections: int = 10):
        # This runs only ONCE due to SingletonMeta
        self.host = host
        self.port = port
        self.max_connections = max_connections
        self._active_connections: int = 0
        self._connection_count: int = 0
        get_app_logger().info(
            f"Connection pool created: {host}:{port} (max={max_connections})"
        )

    def get_connection(self):
        """Acquire a connection from the pool."""
        if self._active_connections >= self.max_connections:
            raise RuntimeError("Connection pool exhausted")
        self._active_connections += 1
        self._connection_count += 1
        conn_id = f"conn-{self._connection_count}"
        get_app_logger().debug(f"Connection acquired: {conn_id}")
        return conn_id

    def release_connection(self, conn_id: str) -> None:
        """Release a connection back to the pool."""
        if self._active_connections > 0:
            self._active_connections -= 1
        get_app_logger().debug(f"Connection released: {conn_id}")

    @property
    def stats(self) -> dict:
        return {
            "active": self._active_connections,
            "total_issued": self._connection_count,
            "max": self.max_connections,
        }


# ─── Demonstration ────────────────────────────────────────────────────────────

if __name__ == "__main__":
    print("=== Module-Level Logger Singleton ===")
    log1 = get_app_logger()
    log2 = get_app_logger()
    print(f"Same logger? {log1 is log2}")  # True
    log1.info("Application started")

    print("\n=== Metaclass Singleton ===")
    pool1 = DatabaseConnectionPool("db.example.com", 5432, max_connections=5)
    pool2 = DatabaseConnectionPool("different-host", 9999)  # Ignored — same object returned

    print(f"Same pool? {pool1 is pool2}")  # True
    print(f"Pool host: {pool2.host}")      # "db.example.com" — not "different-host"

    conn_a = pool1.get_connection()
    conn_b = pool2.get_connection()  # Same pool!
    print(f"Stats: {pool1.stats}")

    pool1.release_connection(conn_a)
    pool2.release_connection(conn_b)
    print(f"Stats after release: {pool1.stats}")
