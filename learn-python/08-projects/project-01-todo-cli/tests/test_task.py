"""Unit tests for the Task model."""

import pytest
from datetime import datetime

from src.models.task import Task
from src.models.priority import Priority


class TestTaskCreation:
    def test_task_has_uuid_id(self):
        task = Task(title="Test")
        assert len(task.id) == 36  # UUID format

    def test_task_default_priority_is_medium(self):
        task = Task(title="Test")
        assert task.priority == Priority.MEDIUM

    def test_task_default_completed_is_false(self):
        task = Task(title="Test")
        assert task.completed is False

    def test_task_created_at_is_set(self):
        before = datetime.now()
        task = Task(title="Test")
        after = datetime.now()
        assert before <= task.created_at <= after

    def test_two_tasks_have_different_ids(self):
        t1 = Task(title="A")
        t2 = Task(title="B")
        assert t1.id != t2.id


class TestTaskMutations:
    def test_complete_sets_completed_true(self):
        task = Task(title="Test")
        task.complete()
        assert task.completed is True

    def test_complete_sets_updated_at(self):
        task = Task(title="Test")
        task.complete()
        assert task.updated_at is not None

    def test_uncomplete_sets_completed_false(self):
        task = Task(title="Test")
        task.complete()
        task.uncomplete()
        assert task.completed is False

    def test_update_title(self):
        task = Task(title="Old")
        task.update_title("New")
        assert task.title == "New"

    def test_update_title_empty_raises(self):
        task = Task(title="Old")
        with pytest.raises(ValueError):
            task.update_title("   ")

    def test_update_priority(self):
        task = Task(title="Test")
        task.update_priority(Priority.HIGH)
        assert task.priority == Priority.HIGH


class TestPriority:
    def test_priority_from_string_case_insensitive(self):
        assert Priority.from_string("high") == Priority.HIGH
        assert Priority.from_string("HIGH") == Priority.HIGH
        assert Priority.from_string("High") == Priority.HIGH

    def test_priority_from_string_invalid_raises(self):
        with pytest.raises(ValueError, match="Invalid priority"):
            Priority.from_string("ULTRA")

    @pytest.mark.parametrize("p,expected", [
        (Priority.LOW, 1),
        (Priority.MEDIUM, 2),
        (Priority.HIGH, 3),
        (Priority.CRITICAL, 4),
    ])
    def test_priority_values(self, p, expected):
        assert p.value == expected
