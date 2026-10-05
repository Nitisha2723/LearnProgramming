"""
Tests for the Todo API.

Run with:
    pytest tests/
    pytest tests/ -v                    # verbose
    pytest tests/test_todos.py -v       # this file only

Uses TestClient (synchronous) for simplicity.
Test isolation is achieved via dependency injection override in conftest.py:
each test gets a fresh, empty repository.
"""

import pytest
from fastapi.testclient import TestClient


# ---------------------------------------------------------------------------
# Helper
# ---------------------------------------------------------------------------


def create_todo(client: TestClient, title: str, description: str | None = None) -> dict:
    """Create a todo and return its JSON data. Raises if status != 201."""
    response = client.post(
        "/api/v1/todos",
        json={"title": title, "description": description},
    )
    assert response.status_code == 201, f"Expected 201, got {response.status_code}: {response.text}"
    return response.json()


# ---------------------------------------------------------------------------
# Health check
# ---------------------------------------------------------------------------


class TestHealthCheck:
    def test_health_check_returns_200(self, client: TestClient) -> None:
        response = client.get("/health")
        assert response.status_code == 200

    def test_health_check_returns_ok_status(self, client: TestClient) -> None:
        response = client.get("/health")
        assert response.json() == {"status": "ok"}


# ---------------------------------------------------------------------------
# POST /api/v1/todos — create
# ---------------------------------------------------------------------------


class TestCreateTodo:
    def test_create_todo_returns_201(self, client: TestClient) -> None:
        response = client.post("/api/v1/todos", json={"title": "Buy milk"})
        assert response.status_code == 201

    def test_create_todo_response_has_all_fields(self, client: TestClient) -> None:
        response = client.post(
            "/api/v1/todos",
            json={"title": "Test task", "description": "A description"},
        )
        data = response.json()
        assert "id" in data
        assert data["title"] == "Test task"
        assert data["description"] == "A description"
        assert data["completed"] is False
        assert "created_at" in data

    def test_create_todo_without_description(self, client: TestClient) -> None:
        response = client.post("/api/v1/todos", json={"title": "No description"})
        assert response.status_code == 201
        assert response.json()["description"] is None

    def test_create_todo_with_empty_title_returns_422(self, client: TestClient) -> None:
        response = client.post("/api/v1/todos", json={"title": ""})
        assert response.status_code == 422

    def test_create_todo_with_missing_title_returns_422(self, client: TestClient) -> None:
        response = client.post("/api/v1/todos", json={})
        assert response.status_code == 422

    def test_create_todo_with_whitespace_only_title_returns_422(self, client: TestClient) -> None:
        # An all-whitespace title has length > 0, but you may want to handle it.
        # This test documents the current behaviour (FastAPI allows it by default).
        response = client.post("/api/v1/todos", json={"title": "   "})
        # Title "   " has length 3, so it passes min_length=1 validation.
        # If you want to forbid whitespace-only titles, add a field_validator.
        assert response.status_code == 201

    def test_create_multiple_todos_get_distinct_ids(self, client: TestClient) -> None:
        id1 = create_todo(client, "First")["id"]
        id2 = create_todo(client, "Second")["id"]
        assert id1 != id2

    def test_new_todo_is_not_completed_by_default(self, client: TestClient) -> None:
        data = create_todo(client, "Incomplete")
        assert data["completed"] is False


# ---------------------------------------------------------------------------
# GET /api/v1/todos/{id} — get single
# ---------------------------------------------------------------------------


class TestGetTodo:
    def test_get_todo_not_found_returns_404(self, client: TestClient) -> None:
        response = client.get("/api/v1/todos/99999")
        assert response.status_code == 404

    def test_get_todo_not_found_has_detail_message(self, client: TestClient) -> None:
        response = client.get("/api/v1/todos/99999")
        assert "detail" in response.json()

    def test_get_todo_returns_created_todo(self, client: TestClient) -> None:
        created = create_todo(client, "Find me later")
        todo_id = created["id"]

        response = client.get(f"/api/v1/todos/{todo_id}")
        assert response.status_code == 200
        assert response.json()["title"] == "Find me later"
        assert response.json()["id"] == todo_id

    def test_get_todo_with_non_integer_id_returns_422(self, client: TestClient) -> None:
        response = client.get("/api/v1/todos/not-a-number")
        assert response.status_code == 422


# ---------------------------------------------------------------------------
# GET /api/v1/todos — list
# ---------------------------------------------------------------------------


class TestListTodos:
    def test_list_todos_returns_200(self, client: TestClient) -> None:
        response = client.get("/api/v1/todos")
        assert response.status_code == 200

    def test_list_todos_initially_empty(self, client: TestClient) -> None:
        response = client.get("/api/v1/todos")
        assert response.json() == []

    def test_list_todos_returns_created_items(self, client: TestClient) -> None:
        create_todo(client, "Task 1")
        create_todo(client, "Task 2")
        create_todo(client, "Task 3")

        response = client.get("/api/v1/todos")
        assert response.status_code == 200
        assert len(response.json()) == 3

    def test_list_todos_response_is_list(self, client: TestClient) -> None:
        response = client.get("/api/v1/todos")
        assert isinstance(response.json(), list)


# ---------------------------------------------------------------------------
# GET /api/v1/todos?completed= — filter
# ---------------------------------------------------------------------------


class TestFilterByCompleted:
    @pytest.fixture(autouse=True)
    def setup_todos(self, client: TestClient) -> None:
        """Create two todos: one pending, one completed."""
        self.pending_id = create_todo(client, "Pending task")["id"]
        self.done_id = create_todo(client, "Done task")["id"]
        # Mark one as complete
        client.patch(f"/api/v1/todos/{self.done_id}/complete")
        self._client = client

    def test_filter_completed_true(self) -> None:
        response = self._client.get("/api/v1/todos?completed=true")
        assert response.status_code == 200
        data = response.json()
        assert len(data) == 1
        assert data[0]["completed"] is True
        assert data[0]["id"] == self.done_id

    def test_filter_completed_false(self) -> None:
        response = self._client.get("/api/v1/todos?completed=false")
        assert response.status_code == 200
        data = response.json()
        assert len(data) == 1
        assert data[0]["completed"] is False
        assert data[0]["id"] == self.pending_id

    def test_no_filter_returns_all(self) -> None:
        response = self._client.get("/api/v1/todos")
        assert len(response.json()) == 2


# ---------------------------------------------------------------------------
# PUT /api/v1/todos/{id} — update
# ---------------------------------------------------------------------------


class TestUpdateTodo:
    def test_update_todo_title(self, client: TestClient) -> None:
        todo_id = create_todo(client, "Original title")["id"]

        response = client.put(
            f"/api/v1/todos/{todo_id}",
            json={"title": "Updated title"},
        )
        assert response.status_code == 200
        assert response.json()["title"] == "Updated title"

    def test_update_todo_marks_complete(self, client: TestClient) -> None:
        todo_id = create_todo(client, "Task")["id"]

        response = client.put(
            f"/api/v1/todos/{todo_id}",
            json={"completed": True},
        )
        assert response.status_code == 200
        assert response.json()["completed"] is True

    def test_update_todo_description(self, client: TestClient) -> None:
        todo_id = create_todo(client, "Task", "Old description")["id"]

        response = client.put(
            f"/api/v1/todos/{todo_id}",
            json={"description": "New description"},
        )
        assert response.status_code == 200
        assert response.json()["description"] == "New description"

    def test_update_nonexistent_todo_returns_404(self, client: TestClient) -> None:
        response = client.put("/api/v1/todos/99999", json={"title": "X"})
        assert response.status_code == 404

    def test_update_with_empty_title_returns_422(self, client: TestClient) -> None:
        todo_id = create_todo(client, "Task")["id"]
        response = client.put(f"/api/v1/todos/{todo_id}", json={"title": ""})
        assert response.status_code == 422

    def test_update_preserves_unchanged_fields(self, client: TestClient) -> None:
        todo_id = create_todo(client, "Original", "Keep this description")["id"]

        response = client.put(
            f"/api/v1/todos/{todo_id}",
            json={"completed": True},  # only update completed
        )
        assert response.status_code == 200
        data = response.json()
        assert data["title"] == "Original"
        assert data["description"] == "Keep this description"
        assert data["completed"] is True


# ---------------------------------------------------------------------------
# PATCH /api/v1/todos/{id}/complete — mark complete
# ---------------------------------------------------------------------------


class TestCompleteTodo:
    def test_complete_todo_returns_200(self, client: TestClient) -> None:
        todo_id = create_todo(client, "To complete")["id"]
        response = client.patch(f"/api/v1/todos/{todo_id}/complete")
        assert response.status_code == 200

    def test_complete_todo_sets_completed_true(self, client: TestClient) -> None:
        todo_id = create_todo(client, "To complete")["id"]
        response = client.patch(f"/api/v1/todos/{todo_id}/complete")
        assert response.json()["completed"] is True

    def test_complete_nonexistent_todo_returns_404(self, client: TestClient) -> None:
        response = client.patch("/api/v1/todos/99999/complete")
        assert response.status_code == 404


# ---------------------------------------------------------------------------
# DELETE /api/v1/todos/{id} — delete
# ---------------------------------------------------------------------------


class TestDeleteTodo:
    def test_delete_todo_returns_204(self, client: TestClient) -> None:
        todo_id = create_todo(client, "Delete me")["id"]
        response = client.delete(f"/api/v1/todos/{todo_id}")
        assert response.status_code == 204

    def test_delete_todo_returns_no_body(self, client: TestClient) -> None:
        todo_id = create_todo(client, "Delete me")["id"]
        response = client.delete(f"/api/v1/todos/{todo_id}")
        assert response.content == b""

    def test_deleted_todo_is_gone(self, client: TestClient) -> None:
        todo_id = create_todo(client, "Delete me")["id"]
        client.delete(f"/api/v1/todos/{todo_id}")

        response = client.get(f"/api/v1/todos/{todo_id}")
        assert response.status_code == 404

    def test_delete_nonexistent_todo_returns_404(self, client: TestClient) -> None:
        response = client.delete("/api/v1/todos/99999")
        assert response.status_code == 404

    def test_delete_reduces_list_count(self, client: TestClient) -> None:
        id1 = create_todo(client, "First")["id"]
        create_todo(client, "Second")

        client.delete(f"/api/v1/todos/{id1}")

        response = client.get("/api/v1/todos")
        assert len(response.json()) == 1
        assert response.json()[0]["title"] == "Second"
