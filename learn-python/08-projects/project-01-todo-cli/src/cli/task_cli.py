import sys
from typing import List, Optional

from ..models.priority import Priority
from ..service.task_service import TaskService, TaskNotFoundError


class TaskCLI:
    """Command-line interface for the todo application."""

    COMMANDS = {
        "add":      "Add a new task: add <title> [--priority LOW|MEDIUM|HIGH|CRITICAL]",
        "list":     "List all tasks",
        "pending":  "List pending tasks",
        "done":     "List completed tasks",
        "complete": "Mark a task as complete: complete <id-prefix>",
        "delete":   "Delete a task: delete <id-prefix>",
        "stats":    "Show statistics",
        "help":     "Show this help message",
        "quit":     "Exit the application",
    }

    def __init__(self, service: TaskService) -> None:
        self._service = service

    def run(self) -> None:
        """Start the interactive CLI loop."""
        print("=== Todo CLI ===")
        print("Type 'help' to see available commands.\n")
        while True:
            try:
                raw = input("todo> ").strip()
                if not raw:
                    continue
                parts = raw.split(None, 1)
                command = parts[0].lower()
                args = parts[1] if len(parts) > 1 else ""
                self._dispatch(command, args)
            except (EOFError, KeyboardInterrupt):
                print("\nGoodbye!")
                sys.exit(0)

    def _dispatch(self, command: str, args: str) -> None:
        dispatch_map = {
            "add":      self._cmd_add,
            "list":     self._cmd_list,
            "pending":  self._cmd_pending,
            "done":     self._cmd_done,
            "complete": self._cmd_complete,
            "delete":   self._cmd_delete,
            "stats":    self._cmd_stats,
            "help":     self._cmd_help,
            "quit":     self._cmd_quit,
            "exit":     self._cmd_quit,
        }
        handler = dispatch_map.get(command)
        if handler:
            handler(args)
        else:
            print(f"Unknown command: '{command}'. Type 'help' for usage.")

    def _cmd_add(self, args: str) -> None:
        """Usage: add <title> [--priority LOW|MEDIUM|HIGH|CRITICAL]"""
        if not args:
            print("Usage: add <title> [--priority LEVEL]")
            return
        priority = Priority.MEDIUM
        if "--priority" in args:
            parts = args.split("--priority")
            title = parts[0].strip()
            p_str = parts[1].strip().split()[0]
            try:
                priority = Priority.from_string(p_str)
            except ValueError as e:
                print(f"Error: {e}")
                return
        else:
            title = args.strip()
        try:
            task = self._service.create_task(title, priority=priority)
            print(f"Created: {task}")
        except ValueError as e:
            print(f"Error: {e}")

    def _cmd_list(self, _: str) -> None:
        tasks = self._service.get_all_tasks()
        self._print_tasks(tasks, "All Tasks")

    def _cmd_pending(self, _: str) -> None:
        tasks = self._service.get_pending_tasks()
        self._print_tasks(tasks, "Pending Tasks")

    def _cmd_done(self, _: str) -> None:
        tasks = self._service.get_completed_tasks()
        self._print_tasks(tasks, "Completed Tasks")

    def _cmd_complete(self, args: str) -> None:
        if not args:
            print("Usage: complete <task-id-prefix>")
            return
        task_id = self._resolve_id(args.strip())
        if task_id is None:
            return
        try:
            task = self._service.complete_task(task_id)
            print(f"Completed: {task}")
        except TaskNotFoundError as e:
            print(f"Error: {e}")

    def _cmd_delete(self, args: str) -> None:
        if not args:
            print("Usage: delete <task-id-prefix>")
            return
        task_id = self._resolve_id(args.strip())
        if task_id is None:
            return
        try:
            self._service.delete_task(task_id)
            print(f"Deleted task {args.strip()}")
        except TaskNotFoundError as e:
            print(f"Error: {e}")

    def _cmd_stats(self, _: str) -> None:
        stats = self._service.get_statistics()
        print(f"Total:      {stats['total']}")
        print(f"Completed:  {stats['completed']}")
        print(f"Pending:    {stats['pending']}")
        print(f"Done rate:  {stats['completion_rate']:.1f}%")

    def _cmd_help(self, _: str) -> None:
        print("\nAvailable commands:")
        for cmd, description in self.COMMANDS.items():
            print(f"  {cmd:<12} {description}")
        print()

    def _cmd_quit(self, _: str) -> None:
        print("Goodbye!")
        sys.exit(0)

    def _print_tasks(self, tasks: List, header: str) -> None:
        print(f"\n{header} ({len(tasks)} task(s)):")
        if not tasks:
            print("  (none)")
        else:
            for task in tasks:
                print(f"  {task}")
        print()

    def _resolve_id(self, prefix: str) -> Optional[str]:
        """Resolve a short ID prefix to a full task ID."""
        all_tasks = self._service.get_all_tasks()
        matches = [t for t in all_tasks if t.id.startswith(prefix)]
        if len(matches) == 1:
            return matches[0].id
        if len(matches) == 0:
            print(f"Error: No task found with ID starting with '{prefix}'.")
            return None
        print(f"Error: Ambiguous ID '{prefix}' matches {len(matches)} tasks. "
              "Use more characters.")
        return None
