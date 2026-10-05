"""Unit tests for TaskService."""

import pytest

from src.models.priority import Priority
from src.repository.in_memory_task_repository import InMemoryTaskRepository
from src.service.task_service import TaskService, TaskNotFoundError


@pytest.fixture
def service():
    return TaskService(InMemoryTaskRepository())


class TestCreateTask:
    def test_create_returns_task(self, service):
        task = service.create_task("Buy milk")
        assert task.title == "Buy milk"

    def test_create_with_priority(self, service):
        task = service.create_task("Urgent", priority=Priority.CRITICAL)
        assert task.priority == Priority.CRITICAL

    def test_create_empty_title_raises(self, service):
        with pytest.raises(ValueError):
            service.create_task("   ")

    def test_create_strips_whitespace(self, service):
        task = service.create_task("  Hello  ")
        assert task.title == "Hello"


class TestGetTask:
    def test_get_existing_task(self, service):
        task = service.create_task("Test")
        found = service.get_task(task.id)
        assert found == task

    def test_get_missing_task_raises(self, service):
        with pytest.raises(TaskNotFoundError):
            service.get_task("nonexistent-id")


class TestCompleteTask:
    def test_complete_task(self, service):
        task = service.create_task("Test")
        completed = service.complete_task(task.id)
        assert completed.completed is True

    def test_uncomplete_task(self, service):
        task = service.create_task("Test")
        service.complete_task(task.id)
        uncompleted = service.uncomplete_task(task.id)
        assert uncompleted.completed is False

    def test_complete_missing_raises(self, service):
        with pytest.raises(TaskNotFoundError):
            service.complete_task("missing")


class TestDeleteTask:
    def test_delete_task(self, service):
        task = service.create_task("Test")
        service.delete_task(task.id)
        with pytest.raises(TaskNotFoundError):
            service.get_task(task.id)

    def test_delete_missing_raises(self, service):
        with pytest.raises(TaskNotFoundError):
            service.delete_task("missing")


class TestStatistics:
    def test_stats_empty(self, service):
        stats = service.get_statistics()
        assert stats["total"] == 0
        assert stats["completion_rate"] == 0.0

    def test_stats_with_tasks(self, service):
        t1 = service.create_task("A")
        service.create_task("B")
        service.complete_task(t1.id)
        stats = service.get_statistics()
        assert stats["total"] == 2
        assert stats["completed"] == 1
        assert stats["completion_rate"] == 50.0
