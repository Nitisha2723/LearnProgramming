"""
Exercise 01: Decorator Framework

Build production-quality decorators from scratch.

EXERCISES:
1A: @retry with exponential backoff
1B: @rate_limit using token bucket algorithm
1C: @memoize with cache statistics
1D: Compose multiple decorators correctly

Run tests with: pytest tests/test_advanced.py -v
"""

import functools
import time
from typing import Callable


# ============================================================================
# EXERCISE 1A: @retry with exponential backoff
# ============================================================================
#
# Build a @retry decorator that:
# - Retries a function up to max_attempts times
# - Waits base_delay seconds after first failure, doubling each time
# - Caps wait time at max_delay seconds
# - Only retries on specified exception types
# - Raises the last exception if all attempts fail
#
# USAGE:
#   @retry(max_attempts=3, base_delay=1.0, exceptions=(ConnectionError,))
#   def unstable_function():
#       ...
#
# HINT: Three nested functions: retry(config) → decorator(func) → wrapper(*args)

def retry(
    max_attempts: int = 3,
    base_delay: float = 1.0,
    max_delay: float = 60.0,
    exceptions: tuple[type[Exception], ...] = (Exception,),
):
    """
    Retry decorator with exponential backoff.

    Args:
        max_attempts: Total attempts including the first one.
        base_delay: Seconds to wait after first failure.
        max_delay: Maximum wait time in seconds.
        exceptions: Tuple of exception types to catch and retry on.
    """
    def decorator(func: Callable) -> Callable:
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            # TODO: Implement retry logic
            # 1. Loop from attempt 1 to max_attempts
            # 2. Try calling func(*args, **kwargs)
            # 3. If it raises one of `exceptions`:
            #    a. If this was the last attempt, re-raise
            #    b. Otherwise, calculate delay = min(base_delay * 2^(attempt-1), max_delay)
            #    c. Sleep for delay seconds
            # 4. Return the result if successful
            pass

        return wrapper
    return decorator


# ============================================================================
# EXERCISE 1B: @rate_limit using token bucket
# ============================================================================
#
# Build a @rate_limit decorator that:
# - Allows at most max_calls calls within a rolling `period` seconds window
# - Raises RuntimeError when limit is exceeded
# - Tracks call timestamps and removes stale ones
#
# USAGE:
#   @rate_limit(max_calls=5, period=60.0)
#   def api_call(endpoint: str) -> dict:
#       ...
#
# HINT: Keep a list of recent call timestamps. Before each call, remove
#       timestamps older than `period`. If len(timestamps) >= max_calls, raise.

def rate_limit(max_calls: int, period: float = 60.0):
    """
    Rate limiting decorator using sliding window.

    Args:
        max_calls: Maximum number of calls allowed within period.
        period: Time window in seconds.

    Raises:
        RuntimeError: When the rate limit is exceeded.
    """
    call_times: list[float] = []

    def decorator(func: Callable) -> Callable:
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            # TODO: Implement rate limiting logic
            # 1. Get current time
            # 2. Remove timestamps older than `period` from call_times
            # 3. If len(call_times) >= max_calls, raise RuntimeError
            # 4. Otherwise, append current time to call_times and call func
            pass

        return wrapper
    return decorator


# ============================================================================
# EXERCISE 1C: @memoize with cache statistics
# ============================================================================
#
# Build a @memoize decorator that:
# - Caches results for repeated calls with the same arguments
# - Tracks hits and misses as attributes on the wrapper
# - Provides a cache_clear() method to reset the cache
# - Works correctly for both positional and keyword arguments
#
# USAGE:
#   @memoize
#   def expensive(n: int) -> int:
#       ...
#
#   result = expensive(10)
#   print(expensive.cache_hits)   # 0 (first call was a miss)
#   result = expensive(10)
#   print(expensive.cache_hits)   # 1 (second call was a hit)
#   expensive.cache_clear()       # Clear the cache

def memoize(func: Callable) -> Callable:
    """
    Memoize decorator that caches results and tracks statistics.

    Attributes added to the wrapper:
        cache_hits (int): Number of cache hits.
        cache_misses (int): Number of cache misses.
        cache_clear() -> None: Clears the cache and resets counts.
    """
    cache: dict = {}

    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        # TODO: Implement memoization
        # 1. Create a cache key from (args, sorted kwargs items)
        # 2. If key in cache, increment cache_hits and return cached value
        # 3. Otherwise, call func, store result, increment cache_misses
        pass

    wrapper.cache_hits = 0
    wrapper.cache_misses = 0
    wrapper.cache_clear = lambda: cache.clear()  # TODO: also reset hit/miss counts
    return wrapper


# ============================================================================
# EXERCISE 1D: Composing Decorators
# ============================================================================
#
# Apply @retry and @memoize to the same function correctly.
# Think about the order: should retry wrap memoize, or memoize wrap retry?
#
# Answer: @memoize should be the outer decorator (applied last, runs first).
# If memoize wraps retry, a failed call won't be cached — retry kicks in.
# If retry wraps memoize, a successful cached result would never be retried.
#
# YOUR TASK: Uncomment and implement the function below using your decorators.

call_count = 0

# @memoize
# @retry(max_attempts=3, base_delay=0.0, exceptions=(ValueError,))
def sometimes_fails(n: int) -> str:
    """
    Returns a string for n, but fails 40% of the time.
    Should be memoized so each n is only ever computed once.
    """
    global call_count
    call_count += 1
    if call_count % 3 == 0:  # Fail every 3rd call
        raise ValueError(f"Simulated failure for n={n}")
    return f"result-{n}"


# ============================================================================
# MANUAL TESTING
# ============================================================================

if __name__ == "__main__":
    # Test 1A: retry
    attempt_log = []

    @retry(max_attempts=3, base_delay=0.0, exceptions=(ValueError,))
    def flaky(n: int) -> int:
        attempt_log.append(n)
        if len(attempt_log) < 3:
            raise ValueError("Not ready yet")
        return n * 2

    try:
        result = flaky(5)
        print(f"flaky(5) = {result}")
    except Exception as e:
        print(f"flaky raised: {e}")

    # Test 1B: rate_limit
    @rate_limit(max_calls=3, period=60.0)
    def limited():
        return "ok"

    for i in range(3):
        limited()  # Should work
    try:
        limited()  # Should raise
    except RuntimeError as e:
        print(f"Rate limit: {e}")

    # Test 1C: memoize
    @memoize
    def square(n: int) -> int:
        return n * n

    square(5)
    square(5)  # Hit
    square(6)
    print(f"square hits={square.cache_hits}, misses={square.cache_misses}")
