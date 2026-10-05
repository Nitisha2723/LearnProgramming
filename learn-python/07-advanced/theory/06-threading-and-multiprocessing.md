# Threading, Multiprocessing, and Concurrent Futures

## The GIL Revisited — Why It Matters for Concurrency

The Global Interpreter Lock (GIL) is CPython's mutex that allows only ONE thread to execute
Python bytecode at a time. This means:

- **Threads are great for I/O-bound tasks** (network requests, file reading) — while one thread
  waits for I/O, others run
- **Threads are NOT great for CPU-bound tasks** (calculations, image processing) — threads take
  turns, no real parallelism
- **Multiprocessing bypasses the GIL** — separate processes have separate GILs, true parallelism
  for CPU work

```
Task Type         | Best Tool
------------------+---------------------------------
I/O-bound         | threading / ThreadPoolExecutor
CPU-bound         | multiprocessing / ProcessPoolExecutor
Modern async I/O  | asyncio (see 03-async-programming.md)
```

---

## threading Module

### Creating Threads

```python
import threading
import time


def worker(name: str, delay: float) -> None:
    """Simulate work with a delay."""
    print(f"[{name}] starting")
    time.sleep(delay)
    print(f"[{name}] done after {delay}s")


# Create and start threads
t1 = threading.Thread(target=worker, args=("Alice", 1.0))
t2 = threading.Thread(target=worker, args=("Bob", 0.5))

t1.start()
t2.start()

# Wait for both to finish
t1.join()
t2.join()
print("All done")
# Bob finishes first because his delay is shorter
```

### Daemon Threads

A daemon thread runs in the background and dies automatically when the main program exits.
Use them for background housekeeping tasks that should not block program exit.

```python
import threading
import time


def heartbeat() -> None:
    """Continuously print heartbeats (background task)."""
    while True:
        print("♥ heartbeat")
        time.sleep(1)


# daemon=True means this thread dies when the main thread exits
monitor = threading.Thread(target=heartbeat, daemon=True)
monitor.start()

# Main thread does real work
time.sleep(3)
print("Main thread exiting — daemon will be killed automatically")
```

### threading.Lock — Preventing Race Conditions

Without a lock, two threads can corrupt shared state:

```python
import threading

counter = 0  # Shared state — UNSAFE without a lock

def increment_unsafe(n: int) -> None:
    global counter
    for _ in range(n):
        counter += 1  # read-modify-write: NOT atomic!


threads = [threading.Thread(target=increment_unsafe, args=(100_000,)) for _ in range(5)]
for t in threads:
    t.start()
for t in threads:
    t.join()

print(f"Expected 500000, got {counter}")  # Usually wrong!
```

Fix it with a `Lock`:

```python
import threading

counter = 0
lock = threading.Lock()


def increment_safe(n: int) -> None:
    global counter
    for _ in range(n):
        with lock:          # Only one thread in this block at a time
            counter += 1


threads = [threading.Thread(target=increment_safe, args=(100_000,)) for _ in range(5)]
for t in threads:
    t.start()
for t in threads:
    t.join()

print(f"Expected 500000, got {counter}")  # Always correct
```

### threading.Event — Signaling Between Threads

An `Event` is a simple flag that one thread sets and others wait on:

```python
import threading
import time

ready = threading.Event()


def producer() -> None:
    print("Producer: preparing data...")
    time.sleep(2)
    print("Producer: data ready!")
    ready.set()  # Signal consumers


def consumer(name: str) -> None:
    print(f"Consumer {name}: waiting for data...")
    ready.wait()  # Block until ready is set
    print(f"Consumer {name}: processing data!")


p = threading.Thread(target=producer)
c1 = threading.Thread(target=consumer, args=("Alpha",))
c2 = threading.Thread(target=consumer, args=("Beta",))

p.start(); c1.start(); c2.start()
p.join(); c1.join(); c2.join()
```

### threading.Queue — Thread-Safe Communication

`queue.Queue` is the recommended way for threads to safely exchange data:

```python
import threading
import queue
import time
import random


def producer(q: queue.Queue, n_items: int) -> None:
    for i in range(n_items):
        item = f"item-{i}"
        q.put(item)         # Thread-safe put
        print(f"Produced: {item}")
        time.sleep(random.uniform(0.1, 0.3))
    q.put(None)             # Sentinel: signal consumers to stop


def consumer(q: queue.Queue) -> None:
    while True:
        item = q.get()      # Thread-safe get; blocks until something is available
        if item is None:
            break
        print(f"  Consumed: {item}")
        q.task_done()       # Signal that the item was processed


work_queue: queue.Queue = queue.Queue(maxsize=5)
p = threading.Thread(target=producer, args=(work_queue, 10))
c = threading.Thread(target=consumer, args=(work_queue,))

p.start(); c.start()
p.join(); c.join()
```

---

## multiprocessing Module

### Creating Processes

```python
import multiprocessing
import os


def cpu_intensive(n: int) -> int:
    """Simulate CPU-bound work: sum of squares."""
    return sum(i * i for i in range(n))


if __name__ == "__main__":          # REQUIRED guard for multiprocessing on Windows/macOS
    p = multiprocessing.Process(target=cpu_intensive, args=(10_000_000,))
    p.start()
    p.join()
    print("Done")
```

### Pool — Parallel Map

`Pool` distributes work across multiple processes automatically:

```python
import multiprocessing
import time


def slow_square(n: int) -> int:
    time.sleep(0.1)      # Simulate expensive computation
    return n * n


if __name__ == "__main__":
    numbers = list(range(20))

    # Sequential
    start = time.perf_counter()
    results = [slow_square(n) for n in numbers]
    print(f"Sequential: {time.perf_counter() - start:.2f}s")

    # Parallel with 4 worker processes
    start = time.perf_counter()
    with multiprocessing.Pool(processes=4) as pool:
        results = pool.map(slow_square, numbers)
    print(f"Parallel (4 procs): {time.perf_counter() - start:.2f}s")
    # Roughly 4x faster!
```

### Shared Memory Pitfalls

Processes do NOT share memory by default — each has its own copy:

```python
import multiprocessing

shared_list = []         # This will NOT be modified by the subprocess!


def bad_worker() -> None:
    shared_list.append("hello")  # Modifies the subprocess's copy, not the parent's


if __name__ == "__main__":
    p = multiprocessing.Process(target=bad_worker)
    p.start()
    p.join()
    print(shared_list)   # [] — the subprocess's changes are invisible here
```

Use `multiprocessing.Manager` or `Value`/`Array` for true shared state:

```python
import multiprocessing


def worker(shared_list, lock) -> None:
    with lock:
        shared_list.append("hello from subprocess")


if __name__ == "__main__":
    manager = multiprocessing.Manager()
    shared = manager.list()
    lock = manager.Lock()

    procs = [multiprocessing.Process(target=worker, args=(shared, lock)) for _ in range(3)]
    for p in procs:
        p.start()
    for p in procs:
        p.join()

    print(list(shared))  # ['hello from subprocess', 'hello from subprocess', 'hello from subprocess']
```

### Queue and Pipe for IPC

```python
import multiprocessing


def sender(pipe_conn) -> None:
    for msg in ["hello", "world", None]:  # None = sentinel
        pipe_conn.send(msg)
    pipe_conn.close()


if __name__ == "__main__":
    parent_conn, child_conn = multiprocessing.Pipe()
    p = multiprocessing.Process(target=sender, args=(child_conn,))
    p.start()

    while True:
        msg = parent_conn.recv()
        if msg is None:
            break
        print(f"Received: {msg}")

    p.join()
```

---

## concurrent.futures — The Modern Way

`concurrent.futures` provides a high-level, uniform interface for both threading and
multiprocessing. It is the recommended approach for most real-world parallel work.

### ThreadPoolExecutor vs ProcessPoolExecutor

```python
from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor
import time
import requests  # pip install requests


# --- I/O-bound: use ThreadPoolExecutor ---
def fetch_url(url: str) -> int:
    """Return the HTTP status code for a URL."""
    response = requests.get(url, timeout=5)
    return response.status_code


urls = [
    "https://httpbin.org/delay/1",
    "https://httpbin.org/delay/1",
    "https://httpbin.org/delay/1",
]

start = time.perf_counter()
with ThreadPoolExecutor(max_workers=3) as executor:
    statuses = list(executor.map(fetch_url, urls))
print(f"3 requests in {time.perf_counter() - start:.2f}s: {statuses}")
# ~1s instead of ~3s — all requests happen concurrently


# --- CPU-bound: use ProcessPoolExecutor ---
def is_prime(n: int) -> bool:
    """Naive primality test."""
    if n < 2:
        return False
    for i in range(2, int(n**0.5) + 1):
        if n % i == 0:
            return False
    return True


numbers = [999_999_937, 999_999_893, 1_000_000_007, 999_999_877]

if __name__ == "__main__":
    start = time.perf_counter()
    with ProcessPoolExecutor() as executor:
        results = list(executor.map(is_prime, numbers))
    print(f"Primality checks in {time.perf_counter() - start:.2f}s: {results}")
```

### submit() vs map()

`map()` is convenient for homogeneous work; `submit()` gives you individual `Future` objects
for more control:

```python
from concurrent.futures import ThreadPoolExecutor, Future
import time


def task(n: int) -> str:
    time.sleep(n * 0.1)
    return f"task-{n} done"


with ThreadPoolExecutor(max_workers=3) as executor:
    # submit() returns a Future immediately
    futures: list[Future] = [executor.submit(task, i) for i in range(5)]

    # .result() blocks until the individual future completes
    for future in futures:
        print(future.result())
```

### Future Objects

A `Future` represents a computation that may not have finished yet:

```python
from concurrent.futures import ThreadPoolExecutor
import time


def slow_add(a: int, b: int) -> int:
    time.sleep(1)
    return a + b


with ThreadPoolExecutor() as executor:
    future = executor.submit(slow_add, 3, 4)

    print(f"Done yet? {future.done()}")   # False — still running
    result = future.result(timeout=5)     # Block up to 5s; raises TimeoutError otherwise
    print(f"Done yet? {future.done()}")   # True
    print(f"Result: {result}")            # 7
```

### as_completed() — Process Results as They Arrive

```python
from concurrent.futures import ThreadPoolExecutor, as_completed
import time
import random


def variable_task(n: int) -> str:
    delay = random.uniform(0.1, 1.0)
    time.sleep(delay)
    return f"task-{n} finished in {delay:.2f}s"


with ThreadPoolExecutor(max_workers=5) as executor:
    futures = {executor.submit(variable_task, i): i for i in range(10)}

    for future in as_completed(futures):   # Yields futures in completion order
        task_id = futures[future]
        try:
            result = future.result()
            print(f"[{task_id}] {result}")
        except Exception as exc:
            print(f"[{task_id}] raised {exc}")
```

### Timeout and Cancellation

```python
from concurrent.futures import ThreadPoolExecutor, TimeoutError
import time


def slow_task() -> str:
    time.sleep(10)
    return "finally done"


with ThreadPoolExecutor() as executor:
    future = executor.submit(slow_task)

    try:
        result = future.result(timeout=2)  # Only wait 2 seconds
    except TimeoutError:
        print("Timed out — cancelling")
        future.cancel()                     # May or may not succeed if already running
```

---

## Decision Guide

| Scenario | Recommended Tool |
|---|---|
| HTTP requests, DB queries, file I/O | `ThreadPoolExecutor` |
| Mathematical computation, data processing | `ProcessPoolExecutor` |
| Modern async I/O with `await` syntax | `asyncio` (see 03-async-programming.md) |
| Background housekeeping | `threading.Thread(daemon=True)` |
| Producer-consumer pipeline | `queue.Queue` + threads |
| Fine-grained shared state (rare) | `multiprocessing.Manager` |

**Rule of thumb:** Start with `concurrent.futures` — it covers 90 % of real-world needs with
minimal boilerplate. Drop to raw `threading` or `multiprocessing` only when you need daemon
threads, custom synchronisation primitives, or fine-grained process control.

---

## Common Pitfalls

1. **Forgetting `if __name__ == "__main__":` with multiprocessing** — on Windows and macOS,
   the spawn start method re-imports the module in each worker, causing infinite recursion
   without this guard.

2. **Sharing mutable state across threads without a lock** — leads to silent data corruption
   (race condition). Always protect shared state with `threading.Lock` or use `queue.Queue`.

3. **Using threads for CPU-bound work** — the GIL means threads will not speed up pure Python
   computation. Use `ProcessPoolExecutor` instead.

4. **Creating too many processes** — each process has significant overhead (~50 MB RAM, startup
   time). `ProcessPoolExecutor()` defaults to `os.cpu_count()` workers, which is usually right.

5. **Not calling `.join()` or using a context manager** — worker threads/processes may be killed
   before finishing if the main thread exits. Always use `with` blocks or explicit `.join()`.
