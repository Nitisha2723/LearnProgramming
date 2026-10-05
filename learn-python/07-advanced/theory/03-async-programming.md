# Async Programming in Python

Python's `asyncio` library enables concurrent I/O without threads. This is essential for building fast web servers, making multiple API calls simultaneously, and handling many connections efficiently.

---

## Why Async? The Problem with Synchronous I/O

```python
# Synchronous — one at a time
import requests
import time

urls = ["https://httpbin.org/delay/1"] * 10  # Each takes ~1 second

start = time.time()
for url in urls:
    response = requests.get(url)  # Thread sleeps while waiting for server
print(f"Sync: {time.time() - start:.1f}s")  # ~10 seconds
```

Every time your code waits for a network response, it blocks. Nothing else can happen. For 10 URLs that each take 1 second, you wait 10 seconds total.

Async solves this: while waiting for one response, start another request.

```python
# Asynchronous — concurrent requests
import asyncio
import aiohttp
import time

urls = ["https://httpbin.org/delay/1"] * 10

async def fetch_all():
    async with aiohttp.ClientSession() as session:
        tasks = [session.get(url) for url in urls]
        responses = await asyncio.gather(*tasks)  # All 10 at once
    return responses

start = time.time()
asyncio.run(fetch_all())
print(f"Async: {time.time() - start:.1f}s")  # ~1 second!
```

Same 10 requests, but now they run concurrently: ~1 second instead of 10.

---

## Core Concepts

### async and await

```python
# async def: marks a function as a coroutine
async def greet(name: str) -> str:
    return f"Hello, {name}!"

# A coroutine object is created but NOT executed yet
coro = greet("Alice")  # This does NOT run greet!
print(type(coro))       # <class 'coroutine'>

# await: runs the coroutine and waits for its result
# Can only be used INSIDE an async function
async def main():
    result = await greet("Alice")  # NOW it runs
    print(result)  # "Hello, Alice!"

# asyncio.run: the entry point — runs the top-level coroutine
asyncio.run(main())
```

### The Event Loop

The event loop is the heart of asyncio. It:
1. Runs coroutines when they are ready
2. Suspends them when they are waiting for I/O
3. Resumes them when the I/O completes

```python
import asyncio

async def task(name: str, delay: float):
    print(f"{name}: starting")
    await asyncio.sleep(delay)  # Non-blocking sleep — hands control back to event loop
    print(f"{name}: done after {delay}s")

async def main():
    # These run concurrently:
    await asyncio.gather(
        task("Alpha", 2.0),
        task("Beta", 1.0),
        task("Gamma", 3.0),
    )

asyncio.run(main())
# Alpha: starting
# Beta: starting
# Gamma: starting
# Beta: done after 1.0s     ← Fastest finishes first
# Alpha: done after 2.0s
# Gamma: done after 3.0s
# Total time: ~3.0s, not 6.0s
```

---

## asyncio.gather() — Run Multiple Coroutines Concurrently

```python
import asyncio
import aiohttp
from dataclasses import dataclass


@dataclass
class GithubUser:
    login: str
    name: str | None
    public_repos: int


async def fetch_github_user(session: aiohttp.ClientSession, username: str) -> GithubUser:
    """Fetch a GitHub user's public info."""
    url = f"https://api.github.com/users/{username}"
    async with session.get(url) as response:
        data = await response.json()
        return GithubUser(
            login=data["login"],
            name=data.get("name"),
            public_repos=data["public_repos"],
        )


async def fetch_multiple_users(usernames: list[str]) -> list[GithubUser]:
    """Fetch multiple GitHub users concurrently."""
    async with aiohttp.ClientSession() as session:
        tasks = [fetch_github_user(session, name) for name in usernames]
        return await asyncio.gather(*tasks)


async def main():
    usernames = ["torvalds", "gvanrossum", "antirez"]
    users = await fetch_multiple_users(usernames)
    for user in users:
        print(f"{user.login}: {user.name} ({user.public_repos} repos)")


asyncio.run(main())
```

### gather() with Error Handling

```python
async def main():
    # By default, gather raises on first exception
    try:
        results = await asyncio.gather(
            coroutine_1(),
            coroutine_2(),
            coroutine_that_fails(),
        )
    except Exception as e:
        print(f"One failed: {e}")

    # return_exceptions=True: collect all results AND exceptions
    results = await asyncio.gather(
        coroutine_1(),
        coroutine_2(),
        coroutine_that_fails(),
        return_exceptions=True,
    )
    for result in results:
        if isinstance(result, Exception):
            print(f"Error: {result}")
        else:
            print(f"Success: {result}")
```

---

## Tasks

`asyncio.Task` wraps a coroutine and schedules it to run on the event loop. Use `asyncio.create_task()` to start a coroutine without waiting for it immediately:

```python
import asyncio


async def background_cleanup():
    """Runs in the background while other code executes."""
    await asyncio.sleep(5)
    print("Background cleanup complete")


async def main():
    # Start background_cleanup but don't wait for it yet
    task = asyncio.create_task(background_cleanup())

    # Do other work...
    print("Doing main work")
    await asyncio.sleep(1)
    print("Main work done")

    # Now wait for background task
    await task
    print("All done")


asyncio.run(main())
```

---

## asyncio Queues: Producer-Consumer Pattern

```python
import asyncio
import random
from dataclasses import dataclass


@dataclass
class WorkItem:
    id: int
    data: str


async def producer(queue: asyncio.Queue, n_items: int) -> None:
    """Produces work items and puts them in the queue."""
    for i in range(n_items):
        item = WorkItem(id=i, data=f"task-{i}")
        await queue.put(item)
        print(f"[Producer] Queued: {item}")
        await asyncio.sleep(random.uniform(0.1, 0.3))

    # Signal consumers that production is done
    # None is a sentinel value
    for _ in range(3):  # One sentinel per consumer
        await queue.put(None)

    print("[Producer] Done")


async def consumer(queue: asyncio.Queue, name: str) -> list[WorkItem]:
    """Processes work items from the queue."""
    processed = []
    while True:
        item = await queue.get()
        if item is None:
            print(f"[{name}] Stopping (received sentinel)")
            queue.task_done()
            break

        # Simulate processing time
        await asyncio.sleep(random.uniform(0.1, 0.5))
        processed.append(item)
        print(f"[{name}] Processed: {item}")
        queue.task_done()

    return processed


async def main():
    queue: asyncio.Queue = asyncio.Queue(maxsize=5)

    # Start producer and 3 concurrent consumers
    producer_task = asyncio.create_task(producer(queue, 10))
    consumer_tasks = [
        asyncio.create_task(consumer(queue, f"Worker-{i}"))
        for i in range(1, 4)
    ]

    # Wait for all tasks
    await asyncio.gather(producer_task, *consumer_tasks)
    print("All work complete!")


asyncio.run(main())
```

---

## When to Use Async vs Threads vs Multiprocessing

The choice depends on what your program is waiting for:

| Workload | Bottleneck | Best Tool |
|----------|-----------|-----------|
| HTTP requests, database queries, file I/O | Waiting for I/O (idle CPU) | `asyncio` |
| I/O but using legacy sync libraries | Waiting for I/O (idle CPU) | `threading` |
| CPU-heavy computation | CPU at 100% | `multiprocessing` |
| Mixed I/O + CPU | Both | `asyncio` + `ProcessPoolExecutor` |

### The GIL Revisited

Python's Global Interpreter Lock (GIL) prevents true multi-threaded CPU parallelism:

```python
import threading
import multiprocessing
import time


def cpu_work():
    """CPU-bound: counting to 50 million."""
    return sum(range(50_000_000))


# THREADS: GIL prevents true parallelism for CPU work
start = time.time()
threads = [threading.Thread(target=cpu_work) for _ in range(4)]
for t in threads: t.start()
for t in threads: t.join()
print(f"Threads (4x CPU): {time.time() - start:.2f}s")  # ~same as 1 thread!

# PROCESSES: Each process has its own GIL — true parallelism
start = time.time()
with multiprocessing.Pool(4) as pool:
    pool.map(cpu_work, range(4))
print(f"Processes (4x CPU): {time.time() - start:.2f}s")  # ~4x faster!
```

**Summary:**
- **asyncio:** I/O-bound work, single thread, event-driven — best for web servers, API clients
- **threading:** I/O-bound work using sync libraries — limited by GIL for CPU work
- **multiprocessing:** CPU-bound work — bypasses GIL, true parallelism

---

## Async Context Managers and Iterators

```python
import asyncio
import aiofiles  # pip install aiofiles


async def read_large_file_async(path: str):
    """Read a large file asynchronously without blocking the event loop."""
    async with aiofiles.open(path, "r") as f:
        async for line in f:
            # Process line — event loop can do other work between lines
            yield line.strip()


# Async context manager protocol: __aenter__ and __aexit__
class AsyncTimer:
    """Context manager that times async code blocks."""

    async def __aenter__(self):
        self._start = asyncio.get_event_loop().time()
        return self

    async def __aexit__(self, *args):
        elapsed = asyncio.get_event_loop().time() - self._start
        print(f"Elapsed: {elapsed:.3f}s")


async def main():
    async with AsyncTimer():
        await asyncio.sleep(0.5)
        await asyncio.sleep(0.3)
    # Elapsed: ~0.800s


asyncio.run(main())
```

---

## Common Async Patterns Cheat Sheet

```python
# Run one coroutine
asyncio.run(my_coroutine())

# Run multiple concurrently, wait for all
results = await asyncio.gather(coro1(), coro2(), coro3())

# Run with timeout
try:
    result = await asyncio.wait_for(slow_coro(), timeout=5.0)
except asyncio.TimeoutError:
    print("Timed out!")

# Create a background task
task = asyncio.create_task(background_work())
# ... do other work ...
await task

# Cancel a task
task.cancel()
try:
    await task
except asyncio.CancelledError:
    pass  # Task was cancelled

# Run CPU-bound code without blocking event loop
import concurrent.futures
loop = asyncio.get_event_loop()
with concurrent.futures.ProcessPoolExecutor() as pool:
    result = await loop.run_in_executor(pool, cpu_bound_function, arg1, arg2)
```
