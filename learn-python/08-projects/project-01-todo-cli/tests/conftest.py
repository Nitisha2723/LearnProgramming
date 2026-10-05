"""Pytest fixtures for todo-cli tests."""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

import pytest

from src.models.task import Task
from src.models.priority import Priority
from src.repository.in_memory_task_repository import InMemoryTaskRepository
from src.service.task_service import TaskService


@pytest.fixture
def repository() -> InMemoryTaskRepository:
    """Return a fresh in-memory repository."""
    return InMemoryTaskRepository()


@pytest.fixture
def service(repository: InMemoryTaskRepository) -> TaskService:
    """Return a TaskService backed by the in-memory repository."""
    return TaskService(repository)


@pytest.fixture
def sample_task() -> Task:
    """Return a single sample Task."""
    return Task(
        title="Buy groceries",
        description="Milk and eggs",
        priority=Priority.MEDIUM,
    )


@pytest.fixture
def populated_service(service: TaskService) -> TaskService:
    """Return a TaskService pre-loaded with several tasks."""
    service.create_task("Task A", priority=Priority.LOW)
    service.create_task("Task B", priority=Priority.HIGH)
    service.create_task("Task C", priority=Priority.CRITICAL)
    return service
