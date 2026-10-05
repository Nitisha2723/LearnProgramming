"""Entry point for the Todo CLI application."""

import sys
import os

# Add src directory to path when running directly
sys.path.insert(0, os.path.dirname(__file__))

from repository.in_memory_task_repository import InMemoryTaskRepository
from service.task_service import TaskService
from cli.task_cli import TaskCLI


def main() -> None:
    """Bootstrap and start the application."""
    repository = InMemoryTaskRepository()
    service = TaskService(repository)
    cli = TaskCLI(service)
    cli.run()


if __name__ == "__main__":
    main()
