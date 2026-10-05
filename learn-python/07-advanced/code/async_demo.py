"""
Module 07 Advanced Python — Code Examples: Async Programming

Demonstrates:
- asyncio.gather() for concurrent HTTP requests
- asyncio.Queue producer-consumer pattern
- asyncio.create_task() for background tasks
- asyncio.wait_for() for timeouts
- Running CPU-bound work in a ProcessPoolExecutor

Note: The HTTP examples use httpbin.org for testing. You need
`pip install aiohttp` to run the HTTP demos.
"""

import asyncio
import math
import random
import time
from concurrent.futures import ProcessPoolExecutor
from dataclasses import dataclass, field
from typing import Any


# ============================================================================
# 1. Concurrent HTTP Requests
# ============================================================================

@dataclass
class HttpResult:
    url: str
    status: int
    elapsed_ms: float
    error: str | None = None


async def fetch_url(session, url: str) -> HttpResult:
    """Fetch a single URL and return the result."""
    try:
        import aiohttp
    except ImportError:
        # Simulate if aiohttp not installed
        await asyncio.sleep(random.uniform(0.1, 0.5))
        return HttpResult(url=url, status=200, elapsed_ms=random.uniform(100, 500))

    start = time.perf_counter()
    try:
        async with session.get(url, timeout=aiohttp.ClientTimeout(total=10)) as response:
            elapsed_ms = (time.perf_counter() - start) * 1000
            return HttpResult(url=url, status=response.status, elapsed_ms=elapsed_ms)
    except Exception as e:
        elapsed_ms = (time.perf_counter() - start) * 1000
        return HttpResult(url=url, status=0, elapsed_ms=elapsed_ms, error=str(e))


async def fetch_all_concurrent(urls: list[str]) -> list[HttpResult]:
    """Fetch all URLs concurrently. All requests run at the same time."""
    try:
        import aiohttp
        async with aiohttp.ClientSession() as session:
            tasks = [fetch_url(session, url) for url in urls]
            return await asyncio.gather(*tasks, return_exceptions=False)
    except ImportError:
        # Fallback simulation without aiohttp
        tasks = [simulate_fetch(url) for url in urls]
        return await asyncio.gather(*tasks)


async def simulate_fetch(url: str) -> HttpResult:
    """Simulate an HTTP request without aiohttp."""
    await asyncio.sleep(random.uniform(0.05, 0.3))
    return HttpResult(url=url, status=200, elapsed_ms=random.uniform(50, 300))


async def demo_concurrent_requests():
    """Show that 10 URLs complete in ~1x time, not 10x."""
    print("=== Concurrent HTTP Requests ===")
    urls = [f"https://httpbin.org/delay/{random.uniform(0.1, 0.5):.1f}" for _ in range(6)]
    # Use simulated URLs for demo purposes
    sim_urls = [f"https://api.example.com/users/{i}" for i in range(6)]

    start = time.perf_counter()
    results = await fetch_all_concurrent(sim_urls)
    total = time.perf_counter() - start

    for r in results:
        print(f"  {r.url.split('/')[-1]}: {r.elapsed_ms:.0f}ms")

    max_individual = max(r.elapsed_ms for r in results)
    print(f"\nTotal wall time: {total*1000:.0f}ms")
    print(f"Slowest individual: {max_individual:.0f}ms")
    print(f"Overlap achieved: {(sum(r.elapsed_ms for r in results) / (total*1000)):.1f}x")


# ============================================================================
# 2. Producer-Consumer Queue Pattern
# ============================================================================

@dataclass
class Job:
    id: int
    payload: str
    priority: int = 0


async def producer(queue: asyncio.Queue, n_jobs: int, name: str = "Producer") -> None:
    """Generate jobs and put them in the queue."""
    for i in range(n_jobs):
        job = Job(
            id=i,
            payload=f"work-item-{i}",
            priority=random.randint(1, 5),
        )
        await queue.put(job)
        print(f"[{name}] Queued job {i}")
        await asyncio.sleep(random.uniform(0.01, 0.05))

    print(f"[{name}] Done producing {n_jobs} jobs")


async def worker(queue: asyncio.Queue, worker_id: int) -> list[int]:
    """Process jobs from the queue until a sentinel None is received."""
    processed_ids = []
    while True:
        job = await queue.get()
        if job is None:
            queue.task_done()
            break

        # Simulate variable processing time
        await asyncio.sleep(random.uniform(0.02, 0.1))
        processed_ids.append(job.id)
        print(f"  [Worker-{worker_id}] Processed job {job.id}")
        queue.task_done()

    return processed_ids


async def demo_producer_consumer():
    """Producer-consumer with multiple concurrent workers."""
    print("\n=== Producer-Consumer Queue ===")
    N_JOBS = 8
    N_WORKERS = 3
    queue: asyncio.Queue[Job | None] = asyncio.Queue(maxsize=5)

    # Start producer
    producer_task = asyncio.create_task(producer(queue, N_JOBS))

    # Start workers
    worker_tasks = [
        asyncio.create_task(worker(queue, i))
        for i in range(1, N_WORKERS + 1)
    ]

    # Wait for producer to finish, then send stop signals
    await producer_task
    for _ in range(N_WORKERS):
        await queue.put(None)  # Sentinel per worker

    results = await asyncio.gather(*worker_tasks)
    all_processed = sorted(j for worker_results in results for j in worker_results)
    print(f"\nAll jobs processed: {all_processed}")
    print(f"Total: {len(all_processed)}/{N_JOBS}")


# ============================================================================
# 3. Background Tasks and Cancellation
# ============================================================================

async def heartbeat(interval: float = 1.0) -> None:
    """Background task that sends periodic heartbeats."""
    count = 0
    while True:
        await asyncio.sleep(interval)
        count += 1
        print(f"[heartbeat] tick #{count}")


async def demo_background_tasks():
    """Start a background task, do work, then cancel it."""
    print("\n=== Background Tasks ===")

    # Start heartbeat in background
    hb_task = asyncio.create_task(heartbeat(0.3))

    # Do main work
    print("Main work starting...")
    await asyncio.sleep(1.0)
    print("Main work complete.")

    # Cancel the background task
    hb_task.cancel()
    try:
        await hb_task
    except asyncio.CancelledError:
        print("[heartbeat] cancelled cleanly")


# ============================================================================
# 4. Timeouts
# ============================================================================

async def slow_operation(duration: float) -> str:
    """Simulates a slow operation."""
    await asyncio.sleep(duration)
    return f"completed after {duration}s"


async def demo_timeouts():
    """Show asyncio.wait_for() for timeouts."""
    print("\n=== Timeouts ===")

    # Operation completes within timeout
    try:
        result = await asyncio.wait_for(slow_operation(0.3), timeout=1.0)
        print(f"Success: {result}")
    except asyncio.TimeoutError:
        print("Timed out!")

    # Operation exceeds timeout
    try:
        result = await asyncio.wait_for(slow_operation(2.0), timeout=0.5)
        print(f"Success: {result}")
    except asyncio.TimeoutError:
        print("Timed out! (expected)")


# ============================================================================
# 5. CPU-Bound Work in ProcessPoolExecutor
# ============================================================================

def is_prime(n: int) -> bool:
    """CPU-bound: check if n is prime."""
    if n < 2:
        return False
    if n == 2:
        return True
    if n % 2 == 0:
        return False
    for i in range(3, int(math.sqrt(n)) + 1, 2):
        if n % i == 0:
            return False
    return True


def count_primes_in_range(start: int, end: int) -> int:
    """Count primes in [start, end). CPU-bound work."""
    return sum(1 for n in range(start, end) if is_prime(n))


async def demo_cpu_in_async():
    """
    Run CPU-bound work without blocking the event loop.
    Use loop.run_in_executor() to delegate to a ProcessPoolExecutor.
    """
    print("\n=== CPU-bound Work in Async ===")

    loop = asyncio.get_event_loop()

    # Without executor: blocks the event loop (don't do this!)
    # result = count_primes_in_range(2, 100_000)

    # With executor: runs in a separate process, event loop stays free
    with ProcessPoolExecutor(max_workers=2) as executor:
        # Two CPU-bound tasks run in parallel
        task1 = loop.run_in_executor(executor, count_primes_in_range, 2, 50_000)
        task2 = loop.run_in_executor(executor, count_primes_in_range, 50_000, 100_000)

        count1, count2 = await asyncio.gather(task1, task2)
        print(f"Primes in [2, 50000): {count1}")
        print(f"Primes in [50000, 100000): {count2}")
        print(f"Total: {count1 + count2}")


# ============================================================================
# Main
# ============================================================================

async def main():
    await demo_concurrent_requests()
    await demo_producer_consumer()
    await demo_background_tasks()
    await demo_timeouts()
    await demo_cpu_in_async()


if __name__ == "__main__":
    asyncio.run(main())
