# Testing REST APIs

## Why Test Your API?

Testing ensures:
- Endpoints return the correct status codes
- Response bodies have the right shape
- Validation rejects bad input
- Business logic works correctly
- You can refactor without breaking things

---

## Testing Options in FastAPI

FastAPI offers two main approaches:

| Approach             | When to Use                                           |
|----------------------|-------------------------------------------------------|
| `TestClient` (sync)  | Simple tests, no async needed, fast to write          |
| `AsyncClient` (async)| When testing async code directly, more realistic      |

---

## TestClient (Synchronous)

`TestClient` wraps your FastAPI app and lets you make HTTP requests in tests without running a real server. It uses `httpx` under the hood.

### Installation

```bash
pip install httpx pytest
```

### Basic Setup

```python
# tests/test_todos.py
from fastapi.testclient import TestClient
from ..main import app   # or: from code.main import app

client = TestClient(app)
```

### Writing Tests

```python
def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_create_todo_returns_201():
    response = client.post(
        "/api/v1/todos",
        json={"title": "Buy groceries", "description": "Milk and eggs"},
    )
    assert response.status_code == 201
    data = response.json()
    assert data["title"] == "Buy groceries"
    assert data["description"] == "Milk and eggs"
    assert data["completed"] is False
    assert "id" in data
    assert "created_at" in data


def test_create_todo_with_empty_title_returns_422():
    response = client.post(
        "/api/v1/todos",
        json={"title": ""},  # empty string — fails min_length=1
    )
    assert response.status_code == 422


def test_get_todo_not_found_returns_404():
    response = client.get("/api/v1/todos/99999")
    assert response.status_code == 404
    assert "not found" in response.json()["detail"].lower()
```

---

## Test Isolation with Fixtures

The problem: tests share state (in-memory data). One test's created todos affect another test.

**Solution**: Use pytest fixtures to create a fresh app instance for each test.

```python
# tests/conftest.py
import pytest
from fastapi.testclient import TestClient
from code.main import app
from code.dependencies import get_repository
from code.repository import TodoRepository


@pytest.fixture
def client():
    """Fresh TestClient with a clean repository for each test."""
    fresh_repo = TodoRepository()  # new empty repository

    # Override the dependency to use our fresh repo
    app.dependency_overrides[get_repository] = lambda: fresh_repo

    with TestClient(app) as test_client:
        yield test_client

    # Clean up override after test
    app.dependency_overrides.clear()
```

Now each test function gets a clean client:

```python
def test_list_todos_empty(client):
    response = client.get("/api/v1/todos")
    assert response.status_code == 200
    assert response.json() == []


def test_list_todos_after_create(client):
    # Create a todo first
    client.post("/api/v1/todos", json={"title": "Test"})

    response = client.get("/api/v1/todos")
    assert len(response.json()) == 1
```

---

## Dependency Injection for Mocking

FastAPI's dependency injection system (`Depends()`) makes testing clean. You can swap out any dependency in tests.

```python
# In your route
from fastapi import Depends
from .dependencies import get_repository
from .repository import TodoRepository

@router.get("/todos")
async def list_todos(repo: TodoRepository = Depends(get_repository)):
    return repo.find_all()
```

In tests, override `get_repository`:

```python
# Override with a mock or fresh instance
app.dependency_overrides[get_repository] = lambda: FakeRepository()
```

---

## Async Testing with httpx.AsyncClient

For async test cases (e.g., when your app uses real databases):

```bash
pip install pytest-asyncio httpx
```

```python
# tests/test_async.py
import pytest
import pytest_asyncio
from httpx import AsyncClient, ASGITransport
from code.main import app


@pytest_asyncio.fixture
async def async_client():
    async with AsyncClient(
        transport=ASGITransport(app=app),
        base_url="http://test"
    ) as client:
        yield client


@pytest.mark.asyncio
async def test_health_async(async_client):
    response = await async_client.get("/health")
    assert response.status_code == 200
```

Add to `pytest.ini` or `pyproject.toml`:

```ini
# pytest.ini
[pytest]
asyncio_mode = auto
```

---

## What to Test for Each Endpoint

Follow this pattern for every endpoint:

### GET (list)
- Returns 200
- Returns empty list when no items exist
- Returns items that were previously created
- Filtering works (e.g., `?completed=true`)

### GET (single)
- Returns 200 with correct data
- Returns 404 for non-existent ID
- Returns 422 for invalid ID type (e.g., `/todos/abc`)

### POST
- Returns 201 with created resource
- Created resource has all provided fields
- Returns 422 for missing required fields
- Returns 422 for invalid field values (e.g., empty title)

### PUT / PATCH
- Returns 200 with updated resource
- Only updates provided fields (PATCH)
- Returns 404 for non-existent ID
- Returns 422 for validation errors

### DELETE
- Returns 204 (no body)
- Returns 404 for non-existent ID
- Resource is actually removed (verify with GET)

---

## Complete Test File Example

```python
# tests/test_todos.py
import pytest
from fastapi.testclient import TestClient


@pytest.fixture(autouse=True)
def setup_client(client):
    """Makes 'client' available to all tests in this module."""
    pass


class TestHealthCheck:
    def test_returns_200(self, client):
        response = client.get("/health")
        assert response.status_code == 200

    def test_returns_ok_status(self, client):
        assert client.get("/health").json()["status"] == "ok"


class TestCreateTodo:
    def test_returns_201(self, client):
        response = client.post("/api/v1/todos", json={"title": "Test"})
        assert response.status_code == 201

    def test_response_has_required_fields(self, client):
        response = client.post("/api/v1/todos", json={"title": "Test"})
        data = response.json()
        assert "id" in data
        assert "title" in data
        assert "completed" in data
        assert "created_at" in data

    def test_new_todo_is_not_completed(self, client):
        response = client.post("/api/v1/todos", json={"title": "Test"})
        assert response.json()["completed"] is False

    def test_empty_title_returns_422(self, client):
        response = client.post("/api/v1/todos", json={"title": ""})
        assert response.status_code == 422

    def test_missing_title_returns_422(self, client):
        response = client.post("/api/v1/todos", json={})
        assert response.status_code == 422


class TestGetTodo:
    def test_returns_created_todo(self, client):
        create_resp = client.post("/api/v1/todos", json={"title": "Find me"})
        todo_id = create_resp.json()["id"]

        get_resp = client.get(f"/api/v1/todos/{todo_id}")
        assert get_resp.status_code == 200
        assert get_resp.json()["title"] == "Find me"

    def test_nonexistent_returns_404(self, client):
        response = client.get("/api/v1/todos/99999")
        assert response.status_code == 404

    def test_invalid_id_returns_422(self, client):
        response = client.get("/api/v1/todos/not-an-int")
        assert response.status_code == 422


class TestListTodos:
    def test_empty_list_initially(self, client):
        response = client.get("/api/v1/todos")
        assert response.status_code == 200
        assert response.json() == []

    def test_returns_created_todos(self, client):
        client.post("/api/v1/todos", json={"title": "First"})
        client.post("/api/v1/todos", json={"title": "Second"})
        response = client.get("/api/v1/todos")
        assert len(response.json()) == 2


class TestFilterTodos:
    def test_filter_completed(self, client):
        # Create two todos
        r1 = client.post("/api/v1/todos", json={"title": "Pending"})
        r2 = client.post("/api/v1/todos", json={"title": "Done"})
        todo_id = r2.json()["id"]

        # Complete one
        client.patch(f"/api/v1/todos/{todo_id}/complete")

        completed = client.get("/api/v1/todos?completed=true").json()
        pending = client.get("/api/v1/todos?completed=false").json()

        assert len(completed) == 1
        assert completed[0]["title"] == "Done"
        assert len(pending) == 1
        assert pending[0]["title"] == "Pending"


class TestUpdateTodo:
    def test_update_title(self, client):
        r = client.post("/api/v1/todos", json={"title": "Original"})
        todo_id = r.json()["id"]

        update_resp = client.put(
            f"/api/v1/todos/{todo_id}",
            json={"title": "Updated"},
        )
        assert update_resp.status_code == 200
        assert update_resp.json()["title"] == "Updated"

    def test_update_nonexistent_returns_404(self, client):
        response = client.put("/api/v1/todos/99999", json={"title": "X"})
        assert response.status_code == 404


class TestDeleteTodo:
    def test_returns_204(self, client):
        r = client.post("/api/v1/todos", json={"title": "Delete me"})
        todo_id = r.json()["id"]

        delete_resp = client.delete(f"/api/v1/todos/{todo_id}")
        assert delete_resp.status_code == 204

    def test_deleted_todo_is_gone(self, client):
        r = client.post("/api/v1/todos", json={"title": "Delete me"})
        todo_id = r.json()["id"]

        client.delete(f"/api/v1/todos/{todo_id}")

        get_resp = client.get(f"/api/v1/todos/{todo_id}")
        assert get_resp.status_code == 404

    def test_delete_nonexistent_returns_404(self, client):
        response = client.delete("/api/v1/todos/99999")
        assert response.status_code == 404
```

---

## Running Tests

```bash
# Run all tests
pytest

# Run with verbose output
pytest -v

# Run a specific file
pytest tests/test_todos.py

# Run a specific test
pytest tests/test_todos.py::TestCreateTodo::test_returns_201

# Run with coverage
pip install pytest-cov
pytest --cov=code --cov-report=term-missing
```

---

## Summary

| Tool                  | Use For                                          |
|-----------------------|--------------------------------------------------|
| `TestClient`          | Simple sync tests, no extra setup needed         |
| `AsyncClient`         | Async tests with real async behavior             |
| `pytest` fixtures     | Test isolation (fresh state per test)            |
| `dependency_overrides`| Replace real services with fakes/mocks           |
| `conftest.py`         | Shared fixtures across test files                |
| `pytest -v`           | Verbose test output                              |
| `pytest --cov`        | Coverage reports                                 |
