"""Unit tests for InMemoryTaskRepository."""

import pytest

from src.models.task import Task
from src.models.priority import Priority
from src.repository.in_memory_task_repository import InMemoryTaskRepository


@pytest.fixture
def repo():
    return InMemoryTaskRepository()


class TestSaveAndFind:
    def test_save_returns_task(self, repo):
        task = Task(title="Test")
        result = repo.save(task)
        assert result is task

    def test_find_by_id_returns_saved_task(self, repo):
        task = Task(title="Test")
        repo.save(task)
        found = repo.find_by_id(task.id)
        assert found == task

    def test_find_by_id_missing_returns_none(self, repo):
        assert repo.find_by_id("nonexistent") is None

    def test_find_all_returns_all_tasks(self, repo):
        t1 = Task(title="A")
        t2 = Task(title="B")
        repo.save(t1)
        repo.save(t2)
        result = repo.find_all()
        assert len(result) == 2

    def test_count_reflects_saves(self, repo):
        assert repo.count() == 0
        repo.save(Task(title="A"))
        assert repo.count() == 1
        repo.save(Task(title="B"))
        assert repo.count() == 2


class TestFilter:
    def test_find_by_completed_true(self, repo):
        t1 = Task(title="A")
        t1.complete()
        t2 = Task(title="B")
        repo.save(t1)
        repo.save(t2)
        result = repo.find_by_completed(True)
        assert len(result) == 1
        assert result[0] == t1

    def test_find_by_priority(self, repo):
        t1 = Task(title="A", priority=Priority.HIGH)
        t2 = Task(title="B", priority=Priority.LOW)
        repo.save(t1)
        repo.save(t2)
        result = repo.find_by_priority(Priority.HIGH)
        assert len(result) == 1
        assert result[0] == t1


class TestDelete:
    def test_delete_existing_returns_true(self, repo):
        task = Task(title="Test")
        repo.save(task)
        assert repo.delete(task.id) is True
        assert repo.find_by_id(task.id) is None

    def test_delete_nonexistent_returns_false(self, repo):
        assert repo.delete("missing-id") is False
