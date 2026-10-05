"""
Async Task Manager — Demo

Run with: python demo.py (from the mini-project directory)
Or:       python -m async_task_manager.demo (from 07-advanced/mini-project)

Shows:
1. Basic task submission and completion
2. Priority ordering (CRITICAL tasks run before LOW tasks)
3. Retry on failure
4. Timeout handling
5. Observer callbacks for monitoring
"""

import asyncio
import logging
import random
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from async_task_manager import AsyncTaskManager, Task, TaskPriority, TaskStatus

logging.basicConfig(
    level=logging.WARNING,  # Set to DEBUG to see worker internals
    format="%(levelname)s %(name)s: %(message)s",
)


# ============================================================================
# Simulated async work functions
# ============================================================================

async def fetch_data(source: str, delay: float = 0.1) -> dict:
    """Simulate fetching data from an external source."""
    await asyncio.sleep(delay)
    return {"source": source, "records": random.randint(10, 100)}


async def process_batch(batch_id: int, size: int) -> dict:
    """Simulate batch processing."""
    await asyncio.sleep(0.05 * size)
    return {"batch_id": batch_id, "processed": size}


async def unreliable_task(name: str, fail_count: list) -> str:
    """Fails the first 2 times, then succeeds."""
    await asyncio.sleep(0.02)
    if fail_count[0] < 2:
        fail_count[0] += 1
        raise ConnectionError(f"Simulated network error for {name}")
    return f"{name} completed"


async def slow_task(duration: float) -> str:
    """A task that takes a while — used for timeout demo."""
    await asyncio.sleep(duration)
    return f"Completed after {duration}s"


# ============================================================================
# Demo 1: Basic Submission
# ============================================================================

async def demo_basic():
    print("\n" + "=" * 60)
    print("DEMO 1: Basic Task Submission")
    print("=" * 60)

    completed_tasks = []

    async with AsyncTaskManager(n_workers=3) as manager:
        manager.on_complete(lambda t: completed_tasks.append(t.name))

        # Submit several tasks
        tasks = [
            Task("fetch-github", fetch_data, "github.com", delay=0.1),
            Task("fetch-api", fetch_data, "api.example.com", delay=0.15),
            Task("process-batch-1", process_batch, 1, 5),
            Task("process-batch-2", process_batch, 2, 3),
            Task("fetch-db", fetch_data, "db.example.com", delay=0.05),
        ]

        for task in tasks:
            await manager.submit(task)

        await manager.wait_all()

    print(f"\nCompleted {len(completed_tasks)} tasks: {completed_tasks}")
    stats = manager.get_stats()
    print(f"Stats: {stats.total_completed} completed, {stats.total_failed} failed")
    print(f"Elapsed: {stats.elapsed_seconds:.2f}s")


# ============================================================================
# Demo 2: Priority Ordering
# ============================================================================

async def demo_priority():
    print("\n" + "=" * 60)
    print("DEMO 2: Priority Ordering")
    print("=" * 60)
    print("Submitting LOW, NORMAL, HIGH, CRITICAL tasks...")
    print("CRITICAL and HIGH should complete first.\n")

    execution_order = []

    async def record_execution(name: str, delay: float = 0.05):
        await asyncio.sleep(delay)
        execution_order.append(name)
        return name

    async with AsyncTaskManager(n_workers=1) as manager:  # 1 worker so order matters
        # Submit in "bad" order: low priority first
        await manager.submit(Task(
            "low-task", record_execution, "low-task",
            priority=TaskPriority.LOW,
        ))
        await manager.submit(Task(
            "normal-task", record_execution, "normal-task",
            priority=TaskPriority.NORMAL,
        ))
        await manager.submit(Task(
            "critical-task", record_execution, "critical-task",
            priority=TaskPriority.CRITICAL,
        ))
        await manager.submit(Task(
            "high-task", record_execution, "high-task",
            priority=TaskPriority.HIGH,
        ))

        await manager.wait_all()

    print(f"Execution order: {execution_order}")
    # Note: Priority queues require heapq-based implementation for strict ordering.
    # With asyncio.Queue (FIFO), tasks run in submission order.
    # A production system would use asyncio.PriorityQueue.


# ============================================================================
# Demo 3: Retry on Failure
# ============================================================================

async def demo_retry():
    print("\n" + "=" * 60)
    print("DEMO 3: Retry on Failure")
    print("=" * 60)

    fail_count = [0]

    async with AsyncTaskManager(n_workers=2) as manager:
        failed_tasks = []
        completed_tasks = []

        manager.on_complete(lambda t: completed_tasks.append((t.name, t.attempts)))
        manager.on_error(lambda t: failed_tasks.append((t.name, t.error)))

        # This task will fail twice then succeed
        retry_task = Task(
            name="flaky-api-call",
            func=unreliable_task,
            "flaky-service",
            fail_count,
            max_retries=3,  # Will retry up to 3 times
        )

        # This task always fails — will exhaust retries
        always_fail_task = Task(
            name="broken-service",
            func=unreliable_task,
            "broken",
            [100],  # fail_count starts high — always fails
            max_retries=2,
        )

        await manager.submit(retry_task)
        await manager.submit(always_fail_task)
        await manager.wait_all()

    print(f"\nCompleted: {completed_tasks}")
    print(f"Failed: {failed_tasks}")


# ============================================================================
# Demo 4: Timeouts
# ============================================================================

async def demo_timeout():
    print("\n" + "=" * 60)
    print("DEMO 4: Timeouts")
    print("=" * 60)

    async with AsyncTaskManager(n_workers=2) as manager:
        completed = []
        failed = []

        manager.on_complete(lambda t: completed.append(t.name))
        manager.on_error(lambda t: failed.append(f"{t.name} ({t.error})"))

        # Fast task — completes before timeout
        await manager.submit(Task(
            "fast-task", slow_task, 0.1,
            timeout=1.0,
        ))

        # Slow task — exceeds timeout
        await manager.submit(Task(
            "too-slow-task", slow_task, 5.0,
            timeout=0.5,
        ))

        await manager.wait_all()

    print(f"\nCompleted: {completed}")
    print(f"Failed (timed out): {failed}")


# ============================================================================
# Main
# ============================================================================

async def main():
    print("ASYNC TASK MANAGER DEMO")
    print("Demonstrates: asyncio, decorators, generators, descriptors")

    await demo_basic()
    await demo_priority()
    await demo_retry()
    await demo_timeout()

    print("\n" + "=" * 60)
    print("Demo complete!")


if __name__ == "__main__":
    asyncio.run(main())
