"""
Dependency injection for the Todo API.

FastAPI's Depends() system lets you declare shared objects (like a database
connection or repository) once and inject them into route handlers automatically.

Using @lru_cache means the repository is created only once (singleton pattern)
and the same instance is reused across all requests — this is what keeps our
in-memory data alive between requests.

In tests, we override get_repository() with a fresh instance per test
to ensure test isolation.
"""

from functools import lru_cache
from .repository import TodoRepository


@lru_cache(maxsize=1)
def get_repository() -> TodoRepository:
    """
    Return the shared TodoRepository instance.

    @lru_cache ensures this is a singleton — the same object is returned
    on every call. This is the in-memory "database" for the application.

    In tests, override this dependency:
        app.dependency_overrides[get_repository] = lambda: TodoRepository()
    """
    return TodoRepository()
