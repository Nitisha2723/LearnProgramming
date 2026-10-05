"""
Module 07 Advanced Python — Code Examples: Decorators

Production-quality decorator implementations demonstrating:
- @retry with exponential backoff and jitter
- @timer that works with or without arguments
- @cache_result with TTL and max_size
- @validate_types using inspect and get_type_hints
- @rate_limit using sliding window
- @memoize (simple version for comparison with functools.cache)
"""

import functools
import inspect
import logging
import random
import time
from typing import Any, Callable, get_type_hints

logging.basicConfig(level=logging.DEBUG, format="%(levelname)s %(name)s: %(message)s")
logger = logging.getLogger(__name__)


# ============================================================================
# 1. @retry — Exponential Backoff with Jitter
# ============================================================================

def retry(
    max_attempts: int = 3,
    base_delay: float = 1.0,
    max_delay: float = 60.0,
    exponential: bool = True,
    jitter: bool = True,
    exceptions: tuple[type[Exception], ...] = (Exception,),
):
    """
    Retry a function on failure with exponential backoff.

    Args:
        max_attempts: Total number of tries (including first attempt).
        base_delay: Seconds to wait after first failure.
        max_delay: Maximum wait time cap.
        exponential: Double delay on each retry if True.
        jitter: Add randomness to avoid thundering herd if True.
        exceptions: Only retry on these exception types.

    Example:
        @retry(max_attempts=5, base_delay=0.5, exceptions=(ConnectionError,))
        def unstable_api_call(url: str) -> dict:
            ...
    """
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

                    if exponential:
                        delay = min(base_delay * (2 ** (attempt - 1)), max_delay)
                    else:
                        delay = base_delay

                    if jitter:
                        delay *= 0.5 + random.random() * 0.5

                    logger.warning(
                        f"[retry] {func.__name__} failed (attempt {attempt}/{max_attempts}): "
                        f"{type(e).__name__}: {e}. Retrying in {delay:.2f}s..."
                    )
                    time.sleep(delay)

            raise last_exception  # type: ignore[misc]

        return wrapper
    return decorator


# ============================================================================
# 2. @timer — Execution Time Measurement
# ============================================================================

def timer(func: Callable | None = None, *, log_level: str = "debug", threshold_ms: float = 0.0):
    """
    Time a function and log the result.

    Can be used with or without arguments:
        @timer                                          # No arguments
        @timer()                                        # Empty call
        @timer(log_level="warning", threshold_ms=100)  # With config

    Args:
        log_level: Logging level ("debug", "info", "warning").
        threshold_ms: Only log if execution time exceeds this value in ms.
    """
    # Support both @timer and @timer()
    if func is None:
        return lambda f: timer(f, log_level=log_level, threshold_ms=threshold_ms)

    log_fn = getattr(logger, log_level, logger.debug)

    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        start = time.perf_counter()
        result = func(*args, **kwargs)
        elapsed_ms = (time.perf_counter() - start) * 1000

        if elapsed_ms >= threshold_ms:
            log_fn(f"[timer] {func.__qualname__} took {elapsed_ms:.2f}ms")

        return result

    return wrapper


# ============================================================================
# 3. @cache_result — Function Cache with TTL and Max Size
# ============================================================================

def cache_result(ttl_seconds: float | None = None, max_size: int | None = None):
    """
    Cache function results with optional TTL and max size.

    This supplements functools.lru_cache by adding TTL support.
    For pure performance without TTL, prefer @functools.cache.

    Args:
        ttl_seconds: How long to keep cached values. None = forever.
        max_size: Maximum cache entries. Oldest evicted when full.

    Note:
        Arguments must be hashable (no lists or dicts as arguments).
    """
    def decorator(func: Callable) -> Callable:
        # key → (result, timestamp)
        cache: dict[tuple, tuple[Any, float]] = {}

        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            key = (args, tuple(sorted(kwargs.items())))
            now = time.time()

            if key in cache:
                result, cached_at = cache[key]
                if ttl_seconds is None or (now - cached_at) < ttl_seconds:
                    wrapper.cache_hits += 1
                    return result
                # TTL expired — remove
                del cache[key]

            # Evict oldest if at capacity
            if max_size is not None and len(cache) >= max_size:
                oldest_key = min(cache, key=lambda k: cache[k][1])
                del cache[oldest_key]

            result = func(*args, **kwargs)
            cache[key] = (result, now)
            wrapper.cache_misses += 1
            return result

        wrapper.cache = cache
        wrapper.cache_hits = 0
        wrapper.cache_misses = 0
        wrapper.cache_clear = cache.clear
        return wrapper

    return decorator


# ============================================================================
# 4. @validate_types — Runtime Type Checking
# ============================================================================

def validate_types(func: Callable) -> Callable:
    """
    Validate argument types against type hints at runtime.

    Raises TypeError if any argument's type doesn't match its hint.
    Only validates simple types (int, str, float, bool, etc.) — skips
    complex generics like list[str] or dict[str, int].

    Note: For production use, prefer Pydantic or the beartype library.
    """
    hints = get_type_hints(func)
    sig = inspect.signature(func)

    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        bound = sig.bind(*args, **kwargs)
        bound.apply_defaults()

        for param_name, value in bound.arguments.items():
            if param_name not in hints:
                continue
            expected_type = hints[param_name]
            # Skip complex generics (list[str], Optional[int], etc.)
            if not isinstance(expected_type, type):
                continue
            if not isinstance(value, expected_type):
                raise TypeError(
                    f"{func.__name__}(): argument '{param_name}' "
                    f"expected {expected_type.__name__}, "
                    f"got {type(value).__name__} ({value!r})"
                )

        return func(*args, **kwargs)

    return wrapper


# ============================================================================
# 5. @rate_limit — Sliding Window Rate Limiter
# ============================================================================

def rate_limit(max_calls: int, period: float = 60.0):
    """
    Limit how many times a function can be called in a sliding time window.

    Args:
        max_calls: Maximum number of calls allowed in the period.
        period: Time window in seconds.

    Raises:
        RuntimeError: When the rate limit is exceeded.
    """
    call_times: list[float] = []

    def decorator(func: Callable) -> Callable:
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            now = time.time()
            # Remove calls outside the window
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


# ============================================================================
# Demo: All decorators in action
# ============================================================================

@retry(max_attempts=4, base_delay=0.01, exceptions=(ValueError,))
@timer(log_level="info")
@validate_types
def divide(numerator: float, denominator: float) -> float:
    """Divide two numbers. Raises ValueError if denominator is zero."""
    if denominator == 0.0:
        raise ValueError("Cannot divide by zero")
    return numerator / denominator


@cache_result(ttl_seconds=60, max_size=10)
def slow_lookup(key: str) -> str:
    """Simulate an expensive lookup."""
    time.sleep(0.1)  # Simulate latency
    return f"result-for-{key}"


@rate_limit(max_calls=3, period=1.0)
def api_endpoint(path: str) -> dict:
    return {"path": path, "status": 200}


if __name__ == "__main__":
    print("=== @validate_types demo ===")
    try:
        result = divide(10.0, "oops")
    except TypeError as e:
        print(f"Type error caught: {e}")

    print("\n=== @cache_result demo ===")
    slow_lookup("user-42")            # Cache miss
    slow_lookup("user-42")            # Cache hit — instant
    slow_lookup("user-99")            # Cache miss
    print(f"Hits: {slow_lookup.cache_hits}, Misses: {slow_lookup.cache_misses}")

    print("\n=== @rate_limit demo ===")
    for i in range(3):
        api_endpoint(f"/path/{i}")  # OK
    try:
        api_endpoint("/path/extra")  # Rate limit exceeded
    except RuntimeError as e:
        print(f"Rate limit: {e}")
