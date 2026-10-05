"""
Solutions for Exercises 01 and 02 — Advanced Python

Exercise 01: Decorator Framework
Exercise 02: Async Data Processing Pipeline
"""

import asyncio
import functools
import random
import time
from dataclasses import dataclass
from typing import AsyncIterator, Callable


# ============================================================================
# SOLUTIONS: Exercise 01 — Decorator Framework
# ============================================================================

# --- 1A: @retry with Exponential Backoff ---

def retry(
    max_attempts: int = 3,
    base_delay: float = 1.0,
    max_delay: float = 60.0,
    exceptions: tuple[type[Exception], ...] = (Exception,),
):
    """Retry with exponential backoff. See exercise for full docstring."""
    def decorator(func: Callable) -> Callable:
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            last_exception = None
            for attempt in range(1, max_attempts + 1):
                try:
                    return func(*args, **kwargs)
                except exceptions as e:
                    last_exception = e
                    if attempt == max_attempts:
                        break
                    delay = min(base_delay * (2 ** (attempt - 1)), max_delay)
                    time.sleep(delay)
            raise last_exception  # type: ignore[misc]
        return wrapper
    return decorator


# --- 1B: @rate_limit — Sliding Window ---

def rate_limit(max_calls: int, period: float = 60.0):
    """Rate limit using sliding window."""
    call_times: list[float] = []

    def decorator(func: Callable) -> Callable:
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            now = time.time()
            call_times[:] = [t for t in call_times if now - t < period]
            if len(call_times) >= max_calls:
                oldest = call_times[0]
                wait = period - (now - oldest)
                raise RuntimeError(
                    f"Rate limit exceeded for {func.__name__}: "
                    f"{max_calls} calls per {period}s. "
                    f"Try again in {wait:.1f}s."
                )
            call_times.append(now)
            return func(*args, **kwargs)
        return wrapper
    return decorator


# --- 1C: @memoize with Statistics ---

def memoize(func: Callable) -> Callable:
    """Memoize with hit/miss tracking and cache_clear()."""
    cache: dict = {}

    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        key = (args, tuple(sorted(kwargs.items())))
        if key in cache:
            wrapper.cache_hits += 1
            return cache[key]
        result = func(*args, **kwargs)
        cache[key] = result
        wrapper.cache_misses += 1
        return result

    def cache_clear():
        cache.clear()
        wrapper.cache_hits = 0
        wrapper.cache_misses = 0

    wrapper.cache_hits = 0
    wrapper.cache_misses = 0
    wrapper.cache_clear = cache_clear
    return wrapper


# ============================================================================
# SOLUTIONS: Exercise 02 — Async Data Processing Pipeline
# ============================================================================

@dataclass
class RawRecord:
    id: int
    name: str
    amount: str


@dataclass
class ValidRecord:
    id: int
    name: str
    amount: float


@dataclass
class EnrichedRecord:
    id: int
    name: str
    amount: float
    category: str
    amount_with_tax: float


@dataclass
class PipelineStats:
    total_raw: int = 0
    total_valid: int = 0
    total_invalid: int = 0
    total_batches: int = 0

    @property
    def validity_rate(self) -> float:
        if self.total_raw == 0:
            return 0.0
        return self.total_valid / self.total_raw


# --- 2A: Async Record Source ---

INVALID_AMOUNTS = ["N/A", "error", "--", "null", "TBD"]
NAMES = ["Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Heidi"]


async def async_record_source(n: int) -> AsyncIterator[RawRecord]:
    """Async generator: yields n RawRecords, simulating API latency."""
    for batch_start in range(0, n, 5):
        batch_end = min(batch_start + 5, n)
        for i in range(batch_start, batch_end):
            # 20% of records have invalid amounts
            if random.random() < 0.2:
                amount = random.choice(INVALID_AMOUNTS)
            else:
                amount = f"{random.uniform(10.0, 200.0):.2f}"
            yield RawRecord(
                id=i,
                name=random.choice(NAMES),
                amount=amount,
            )
        # Simulate network latency between batches
        await asyncio.sleep(0.05)


# --- 2B: Async Validation Stage ---

async def async_validate(
    source: AsyncIterator[RawRecord],
    stats: PipelineStats,
) -> AsyncIterator[ValidRecord]:
    """Validation stage: discard records with unparseable amounts."""
    async for record in source:
        try:
            amount = float(record.amount)
            stats.total_valid += 1
            yield ValidRecord(id=record.id, name=record.name, amount=amount)
        except (ValueError, TypeError):
            stats.total_invalid += 1


# --- 2C: Async Enrichment Stage ---

async def async_enrich(
    source: AsyncIterator[ValidRecord],
) -> AsyncIterator[EnrichedRecord]:
    """Enrichment stage: add category and tax fields."""
    async for record in source:
        if record.amount > 100:
            category = "high"
        elif record.amount > 50:
            category = "medium"
        else:
            category = "low"

        yield EnrichedRecord(
            id=record.id,
            name=record.name,
            amount=record.amount,
            category=category,
            amount_with_tax=round(record.amount * 1.10, 2),
        )


# --- 2D: Async Batching Stage ---

async def async_batch(
    source: AsyncIterator[EnrichedRecord],
    batch_size: int = 5,
) -> AsyncIterator[list[EnrichedRecord]]:
    """Batching stage: group records into lists of batch_size."""
    batch: list[EnrichedRecord] = []
    async for record in source:
        batch.append(record)
        if len(batch) >= batch_size:
            yield batch
            batch = []
    if batch:
        yield batch  # Last partial batch


# --- 2E: Pipeline Orchestrator ---

async def run_pipeline(n_records: int = 20) -> PipelineStats:
    """Run the full async data processing pipeline."""
    stats = PipelineStats()
    stats.total_raw = n_records

    source = async_record_source(n_records)
    validated = async_validate(source, stats)
    enriched = async_enrich(validated)
    batched = async_batch(enriched, batch_size=5)

    async for batch in batched:
        stats.total_batches += 1
        total = sum(r.amount_with_tax for r in batch)
        categories = [r.category for r in batch]
        print(
            f"  Batch {stats.total_batches}: {len(batch)} records, "
            f"total=${total:.2f}, categories={categories}"
        )

    return stats


# ============================================================================
# Manual test
# ============================================================================

if __name__ == "__main__":
    print("=== Exercise 01 Solutions ===")

    # retry
    attempt_log = []

    @retry(max_attempts=4, base_delay=0.0, exceptions=(ValueError,))
    def flaky():
        attempt_log.append(1)
        if len(attempt_log) < 3:
            raise ValueError("Not ready")
        return "success"

    result = flaky()
    print(f"retry result: {result} (took {len(attempt_log)} attempts)")

    # rate_limit
    @rate_limit(max_calls=3, period=60.0)
    def limited():
        return "ok"

    for _ in range(3):
        limited()
    try:
        limited()
    except RuntimeError as e:
        print(f"rate_limit: {e}")

    # memoize
    @memoize
    def square(n: int) -> int:
        return n * n

    square(5)
    square(5)
    square(6)
    print(f"memoize: hits={square.cache_hits}, misses={square.cache_misses}")

    print("\n=== Exercise 02 Solutions ===")
    asyncio.run(run_pipeline(20))
