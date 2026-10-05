# Performance and Profiling in Python

Python is sometimes called slow — but that's often because developers use the wrong tools. With profiling, caching, NumPy, and the right data structures, Python performance is rarely the bottleneck.

---

## Measure Before You Optimize

The first rule of performance: **measure first, optimize second**. Guessing leads to wasted effort on the wrong bottleneck.

```python
# timeit: for micro-benchmarks
import timeit

# Compare two implementations
result = timeit.timeit(
    stmt="[x**2 for x in range(1000)]",  # list comprehension
    number=10000,
)
print(f"List comprehension: {result:.3f}s")

result = timeit.timeit(
    stmt="list(map(lambda x: x**2, range(1000)))",  # map + lambda
    number=10000,
)
print(f"map + lambda: {result:.3f}s")
```

---

## cProfile: Finding the Real Bottleneck

`cProfile` measures where your program spends time, sorted by cost:

```python
import cProfile
import pstats
import io


def fibonacci_naive(n: int) -> int:
    """Naive recursive Fibonacci — extremely slow for large n."""
    if n <= 1:
        return n
    return fibonacci_naive(n - 1) + fibonacci_naive(n - 2)


def compute_fibonacci_sequence(limit: int) -> list[int]:
    """Compute Fibonacci sequence up to limit. This is slow."""
    return [fibonacci_naive(i) for i in range(limit)]


# Profile it
profiler = cProfile.Profile()
profiler.enable()

result = compute_fibonacci_sequence(30)

profiler.disable()

# Print sorted by cumulative time
stream = io.StringIO()
stats = pstats.Stats(profiler, stream=stream)
stats.sort_stats("cumulative")
stats.print_stats(10)  # Top 10 functions
print(stream.getvalue())
```

Output shows:
- `ncalls`: how many times each function was called
- `tottime`: time in function itself (excluding called functions)
- `cumtime`: total time including called functions

### Running from Command Line

```bash
# Profile a script
python -m cProfile -s cumulative my_script.py

# Profile and save to file for later analysis
python -m cProfile -o profile_output.prof my_script.py
python -c "import pstats; pstats.Stats('profile_output.prof').sort_stats('cumulative').print_stats(20)"
```

---

## functools.lru_cache and functools.cache

The most common Python speedup: cache the results of expensive function calls:

```python
import functools
import time


# Without caching — O(2^n) exponential
def fibonacci_naive(n: int) -> int:
    if n <= 1:
        return n
    return fibonacci_naive(n - 1) + fibonacci_naive(n - 2)


# With memoization — O(n) linear
@functools.cache  # Python 3.9+ (= lru_cache with no size limit)
def fibonacci_cached(n: int) -> int:
    if n <= 1:
        return n
    return fibonacci_cached(n - 1) + fibonacci_cached(n - 2)


start = time.perf_counter()
result = fibonacci_naive(35)  # ~3 seconds
print(f"Naive: {time.perf_counter() - start:.3f}s, result={result}")

start = time.perf_counter()
result = fibonacci_cached(35)  # <0.001 seconds
print(f"Cached: {time.perf_counter() - start:.6f}s, result={result}")

print(fibonacci_cached.cache_info())  # CacheInfo(hits=33, misses=36, ...)
fibonacci_cached.cache_clear()         # Clear the cache when needed
```

### lru_cache vs cache

```python
# lru_cache(maxsize=N): keeps the N most recently used results
# Good when you have many possible inputs but only a subset are common
@functools.lru_cache(maxsize=128)
def expensive_query(user_id: str, month: int) -> dict:
    ...

# cache: unlimited size — use when the input space is small
# Equivalent to lru_cache(maxsize=None) but faster
@functools.cache
def fibonacci(n: int) -> int:
    ...
```

---

## __slots__: Reducing Memory Per Instance

By default, Python stores instance attributes in a dictionary (`__dict__`). For classes with many instances, this wastes memory.

```python
import sys


class PointNormal:
    def __init__(self, x: float, y: float, z: float):
        self.x = x
        self.y = y
        self.z = z


class PointSlots:
    __slots__ = ("x", "y", "z")  # Pre-declare all instance attributes
    
    def __init__(self, x: float, y: float, z: float):
        self.x = x
        self.y = y
        self.z = z


normal = PointNormal(1.0, 2.0, 3.0)
slotted = PointSlots(1.0, 2.0, 3.0)

print(sys.getsizeof(normal))           # ~48 bytes
print(sys.getsizeof(normal.__dict__))  # ~200 bytes (the dict itself)
print(sys.getsizeof(slotted))          # ~56 bytes (no __dict__)

# For 1 million instances:
# Normal:  ~248 MB
# Slotted:  ~56 MB  (4.4x more memory efficient)
```

**When to use `__slots__`:**
- Classes with many instances (e.g., data model objects, particles, nodes)
- Memory-sensitive applications
- Slight attribute access speedup

**When NOT to use:**
- You need to add arbitrary attributes at runtime
- The class uses multiple inheritance with slots (complex)
- You only create a few instances

---

## NumPy: When You Need Real Speed

For numerical computation on arrays, NumPy is orders of magnitude faster than Python loops:

```python
import numpy as np
import timeit


# Python list: slow
def sum_of_squares_python(n: int) -> float:
    return sum(x * x for x in range(n))


# NumPy array: fast (vectorized C code under the hood)
def sum_of_squares_numpy(n: int) -> float:
    arr = np.arange(n, dtype=np.float64)
    return np.sum(arr * arr)


n = 1_000_000

python_time = timeit.timeit(lambda: sum_of_squares_python(n), number=10)
numpy_time = timeit.timeit(lambda: sum_of_squares_numpy(n), number=10)

print(f"Python: {python_time:.3f}s")
print(f"NumPy:  {numpy_time:.3f}s")
print(f"NumPy is {python_time / numpy_time:.0f}x faster")
# NumPy is typically 10-100x faster for numerical operations
```

### NumPy Operations vs Python Loops

```python
import numpy as np

prices = np.array([10.0, 25.0, 5.0, 50.0, 15.0])
quantities = np.array([3, 1, 10, 2, 4])

# NumPy: vectorized — no Python loop
total_revenue = np.sum(prices * quantities)      # Element-wise multiply, then sum
above_average = prices[prices > prices.mean()]   # Boolean indexing

# Equivalent Python (much slower):
total_revenue_py = sum(p * q for p, q in zip(prices, quantities))
avg = sum(prices) / len(prices)
above_average_py = [p for p in prices if p > avg]
```

---

## Algorithmic Complexity: The Biggest Win

No profiling trick beats choosing the right algorithm:

```python
import time


def contains_slow(items: list, target) -> bool:
    """O(n) — checks each item."""
    return target in items


def contains_fast(items: set, target) -> bool:
    """O(1) average — hash lookup."""
    return target in items


data_list = list(range(1_000_000))
data_set = set(data_list)
target = 999_999

# 1,000 lookups in a list
start = time.perf_counter()
for _ in range(1000):
    contains_slow(data_list, target)
print(f"List: {time.perf_counter() - start:.3f}s")

# 1,000 lookups in a set
start = time.perf_counter()
for _ in range(1000):
    contains_fast(data_set, target)
print(f"Set:  {time.perf_counter() - start:.6f}s")
# Set is thousands of times faster
```

---

## Async for I/O Bound Work

For programs that wait for I/O (network, disk), async can give huge speedups with no algorithm changes:

```python
import asyncio
import aiohttp
import time


# Sync: sequential requests — waits for each response
def fetch_sequential(urls: list[str]) -> list:
    import requests
    return [requests.get(url).status_code for url in urls]


# Async: concurrent requests — all at once
async def fetch_concurrent(urls: list[str]) -> list:
    async with aiohttp.ClientSession() as session:
        tasks = [session.get(url) for url in urls]
        responses = await asyncio.gather(*tasks)
        return [r.status for r in responses]


# For 10 URLs that each take 0.5s:
# Sequential: ~5.0 seconds
# Concurrent: ~0.5 seconds (all overlap)
```

---

## Multiprocessing for CPU Bound Work

For CPU-intensive work, use multiple processes to bypass the GIL:

```python
import multiprocessing
from concurrent.futures import ProcessPoolExecutor
import math


def is_prime(n: int) -> bool:
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


def find_primes_range(start: int, end: int) -> list[int]:
    return [n for n in range(start, end) if is_prime(n)]


# Sequential
import time
start = time.perf_counter()
primes = find_primes_range(2, 500_000)
print(f"Sequential: {time.perf_counter() - start:.2f}s, {len(primes)} primes")

# Parallel — split work across CPU cores
start = time.perf_counter()
n_workers = multiprocessing.cpu_count()
chunk_size = 500_000 // n_workers
chunks = [(i * chunk_size, (i + 1) * chunk_size) for i in range(n_workers)]

with ProcessPoolExecutor(max_workers=n_workers) as executor:
    results = executor.map(lambda r: find_primes_range(*r), chunks)
    primes_parallel = [p for chunk in results for p in chunk]

print(f"Parallel ({n_workers} cores): {time.perf_counter() - start:.2f}s")
```

---

## Quick Wins Summary

| Optimization | Impact | When to Apply |
|--------------|--------|---------------|
| `@functools.cache` / `@lru_cache` | 10-1000x | Pure functions with repeated inputs |
| `set` for membership testing | 100-1000x | `x in collection` called repeatedly |
| `__slots__` | 2-5x memory | Many instances of the same class |
| List/dict/set comprehensions | 1.5-2x | Replacing for-loops that build collections |
| `collections.defaultdict` | 1.2-1.5x | Dict with default values |
| NumPy arrays | 10-100x | Numerical operations on large arrays |
| `asyncio` | 10-100x throughput | I/O-bound concurrent work |
| `multiprocessing` | N x CPU cores | CPU-bound parallel work |

**Profiling workflow:**
1. Run `cProfile` to find the bottleneck (don't guess)
2. Check for algorithmic improvements first (O(n²) → O(n log n))
3. Apply caching if the function is pure and called with repeated inputs
4. Use NumPy if the bottleneck is numerical loops
5. Use async/multiprocessing if the bottleneck is I/O or CPU
