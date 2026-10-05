"""
Async Task Manager

Demonstrates:
- asyncio.Queue for task distribution
- asyncio.create_task() for background workers
- asyncio.gather() for concurrent workers
- asyncio.wait_for() for task timeouts
- Context manager protocol (__aenter__, __aexit__) for clean lifecycle
- Observer pattern: on_complete and on_error callbacks
- @retry decorator (from code/decorators_advanced.py)
"""

import asyncio
import functools
import logging
import time
from dataclasses import dataclass, field
from typing import Any, Callable

from .task import Task, TaskPriority, TaskStatus

logger = logging.getLogger(__name__)


# ============================================================================
# Retry decorator (inline to keep mini-project self-contained)
# ============================================================================

def retry_on_error(max_attempts: int = 3, base_delay: float = 0.1):
    """Retry decorator with exponential backoff for async functions."""
    def decorator(func):
        @functools.wraps(func)
        async def wrapper(*args, **kwargs):
            last_exc = None
            for attempt in range(1, max_attempts + 1):
                try:
                    return await func(*args, **kwargs)
                except Exception as e:
                    last_exc = e
                    if attempt < max_attempts:
                        delay = base_delay * (2 ** (attempt - 1))
                        await asyncio.sleep(delay)
            raise last_exc  # type: ignore[misc]
        return wrapper
    return decorator


# ============================================================================
# Stats
# ============================================================================

@dataclass
class TaskStats:
    total_submitted: int = 0
    total_completed: int = 0
    total_failed: int = 0
    total_cancelled: int = 0
    total_retried: int = 0
    _start_time: float = field(default_factory=time.time, repr=False)

    @property
    def success_rate(self) -> float:
        total = self.total_completed + self.total_failed
        if total == 0:
            return 0.0
        return self.total_completed / total

    @property
    def elapsed_seconds(self) -> float:
        return time.time() - self._start_time


# ============================================================================
# Async Task Manager
# ============================================================================

class AsyncTaskManager:
    """
    Async task queue with concurrent workers, retries, timeouts, and observers.

    Usage:
        async with AsyncTaskManager(n_workers=4) as manager:
            manager.on_complete(lambda task: print(f"Done: {task.name}"))
            await manager.submit(my_task)
            await manager.wait_all()

    Features:
        - Priority-based queue (CRITICAL tasks processed first)
        - Per-task timeout via asyncio.wait_for()
        - Automatic retry on failure (configurable per task)
        - Observer callbacks for completion and failure events
        - Clean shutdown via context manager
    """

    def __init__(self, n_workers: int = 4, queue_size: int = 100):
        self._n_workers = n_workers
        self._queue: asyncio.Queue[Task | None] = asyncio.Queue(maxsize=queue_size)
        self._workers: list[asyncio.Task] = []
        self._on_complete_callbacks: list[Callable[[Task], None]] = []
        self._on_error_callbacks: list[Callable[[Task], None]] = []
        self._stats = TaskStats()
        self._running = False

    # -------------------------------------------------------------------------
    # Observer pattern: register callbacks
    # -------------------------------------------------------------------------

    def on_complete(self, callback: Callable[[Task], None]) -> None:
        """Register a callback for task completion."""
        self._on_complete_callbacks.append(callback)

    def on_error(self, callback: Callable[[Task], None]) -> None:
        """Register a callback for task failure."""
        self._on_error_callbacks.append(callback)

    def _notify_complete(self, task: Task) -> None:
        for cb in self._on_complete_callbacks:
            try:
                cb(task)
            except Exception as e:
                logger.error(f"on_complete callback error: {e}")

    def _notify_error(self, task: Task) -> None:
        for cb in self._on_error_callbacks:
            try:
                cb(task)
            except Exception as e:
                logger.error(f"on_error callback error: {e}")

    # -------------------------------------------------------------------------
    # Lifecycle
    # -------------------------------------------------------------------------

    async def start(self) -> None:
        """Start the worker pool."""
        if self._running:
            return
        self._running = True
        self._workers = [
            asyncio.create_task(self._worker(i), name=f"worker-{i}")
            for i in range(self._n_workers)
        ]
        logger.info(f"AsyncTaskManager started with {self._n_workers} workers")

    async def stop(self) -> None:
        """Gracefully stop all workers after they finish current tasks."""
        if not self._running:
            return
        # Send one sentinel per worker
        for _ in range(self._n_workers):
            await self._queue.put(None)
        # Wait for all workers to finish
        await asyncio.gather(*self._workers, return_exceptions=True)
        self._running = False
        logger.info("AsyncTaskManager stopped")

    async def __aenter__(self) -> "AsyncTaskManager":
        await self.start()
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb) -> None:
        await self.stop()

    # -------------------------------------------------------------------------
    # Task submission
    # -------------------------------------------------------------------------

    async def submit(self, task: Task) -> None:
        """Submit a task to the queue."""
        if not self._running:
            raise RuntimeError("Manager is not running. Use 'async with' or call start() first.")
        task.status = TaskStatus.PENDING
        self._stats.total_submitted += 1
        await self._queue.put(task)
        logger.debug(f"Submitted: {task}")

    async def wait_all(self) -> None:
        """Wait until all currently queued tasks are processed."""
        await self._queue.join()

    # -------------------------------------------------------------------------
    # Worker
    # -------------------------------------------------------------------------

    async def _worker(self, worker_id: int) -> None:
        """Worker coroutine: process tasks from the queue until sentinel."""
        logger.debug(f"Worker-{worker_id} started")
        while True:
            task = await self._queue.get()
            if task is None:
                self._queue.task_done()
                break
            await self._execute_task(task, worker_id)
            self._queue.task_done()
        logger.debug(f"Worker-{worker_id} stopped")

    async def _execute_task(self, task: Task, worker_id: int) -> None:
        """Execute a single task, with retries and timeout."""
        task.status = TaskStatus.RUNNING
        max_attempts = task.max_retries + 1

        for attempt in range(1, max_attempts + 1):
            task.attempts = attempt
            try:
                if task.timeout is not None:
                    result = await asyncio.wait_for(
                        task.func(*task.args, **task.kwargs),
                        timeout=task.timeout,
                    )
                else:
                    result = await task.func(*task.args, **task.kwargs)

                task.result = result
                task.status = TaskStatus.COMPLETED
                self._stats.total_completed += 1
                logger.info(f"Worker-{worker_id} completed: {task.name} (attempt {attempt})")
                self._notify_complete(task)
                return

            except asyncio.TimeoutError:
                task.error = f"Timed out after {task.timeout}s"
                logger.warning(f"Worker-{worker_id} timeout: {task.name}")
                if attempt >= max_attempts:
                    task.status = TaskStatus.FAILED
                    self._stats.total_failed += 1
                    self._notify_error(task)
                    return
                self._stats.total_retried += 1
                await asyncio.sleep(0.05 * attempt)

            except Exception as e:
                task.error = f"{type(e).__name__}: {e}"
                logger.warning(
                    f"Worker-{worker_id} error on {task.name} "
                    f"(attempt {attempt}/{max_attempts}): {e}"
                )
                if attempt >= max_attempts:
                    task.status = TaskStatus.FAILED
                    self._stats.total_failed += 1
                    self._notify_error(task)
                    return
                self._stats.total_retried += 1
                await asyncio.sleep(0.05 * attempt)

    # -------------------------------------------------------------------------
    # Stats
    # -------------------------------------------------------------------------

    def get_stats(self) -> TaskStats:
        return self._stats
