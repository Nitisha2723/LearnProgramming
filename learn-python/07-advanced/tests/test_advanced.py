"""
Module 07 Advanced Python — Test Suite

Tests for:
- @retry decorator behavior
- @rate_limit decorator behavior
- @memoize decorator with statistics
- Generator functions (fibonacci, primes, pipeline)
- Async pipeline stages

Run with: pytest tests/test_advanced.py -v
From: 07-advanced/ directory
"""

import asyncio
import importlib.util
import sys
import time
from pathlib import Path

import pytest

# Add parent directory so exercises/ is importable
_module_root = Path(__file__).parent.parent
sys.path.insert(0, str(_module_root))

from exercises.solutions.solutions_01_02 import (
    EnrichedRecord,
    PipelineStats,
    RawRecord,
    ValidRecord,
    async_batch,
    async_enrich,
    async_record_source,
    async_validate,
    memoize,
    rate_limit,
    retry,
    run_pipeline,
)

# Import generators_advanced directly by file path to avoid conflict with
# Python's built-in 'code' module.
_gen_spec = importlib.util.spec_from_file_location(
    "generators_advanced",
    _module_root / "code" / "generators_advanced.py",
)
_gen_module = importlib.util.module_from_spec(_gen_spec)  # type: ignore[arg-type]
_gen_spec.loader.exec_module(_gen_module)  # type: ignore[union-attr]

build_pipeline = _gen_module.build_pipeline
chunked = _gen_module.chunked
fibonacci = _gen_module.fibonacci
flatten = _gen_module.flatten
naturals = _gen_module.naturals
primes = _gen_module.primes
running_average = _gen_module.running_average
windowed = _gen_module.windowed


# ============================================================================
# Tests: @retry
# ============================================================================

class TestRetry:
    def test_succeeds_on_first_try(self):
        """Function that never fails should work normally."""
        calls = []

        @retry(max_attempts=3, base_delay=0.0)
        def always_works():
            calls.append(1)
            return 42

        result = always_works()
        assert result == 42
        assert len(calls) == 1  # Only called once

    def test_retries_and_eventually_succeeds(self):
        """Should retry until success."""
        attempts = []

        @retry(max_attempts=4, base_delay=0.0, exceptions=(ValueError,))
        def succeeds_on_third():
            attempts.append(1)
            if len(attempts) < 3:
                raise ValueError("Not yet")
            return "success"

        result = succeeds_on_third()
        assert result == "success"
        assert len(attempts) == 3

    def test_raises_after_max_attempts(self):
        """Should raise the last exception after all attempts fail."""
        attempts = []

        @retry(max_attempts=3, base_delay=0.0, exceptions=(ValueError,))
        def always_fails():
            attempts.append(1)
            raise ValueError("Always fails")

        with pytest.raises(ValueError, match="Always fails"):
            always_fails()

        assert len(attempts) == 3

    def test_only_retries_specified_exceptions(self):
        """Should NOT retry exceptions not in the exceptions tuple."""
        @retry(max_attempts=3, base_delay=0.0, exceptions=(ValueError,))
        def raises_type_error():
            raise TypeError("Wrong type")

        with pytest.raises(TypeError):
            raises_type_error()  # Should NOT retry

    def test_preserves_function_metadata(self):
        """Decorated function should retain __name__ and __doc__."""
        @retry(max_attempts=3, base_delay=0.0)
        def my_function():
            """My docstring."""
            pass

        assert my_function.__name__ == "my_function"
        assert my_function.__doc__ == "My docstring."


# ============================================================================
# Tests: @rate_limit
# ============================================================================

class TestRateLimit:
    def test_allows_calls_within_limit(self):
        """Should allow calls up to the limit."""
        @rate_limit(max_calls=5, period=60.0)
        def api_call():
            return "ok"

        results = [api_call() for _ in range(5)]
        assert results == ["ok"] * 5

    def test_blocks_calls_over_limit(self):
        """Should raise RuntimeError when limit is exceeded."""
        @rate_limit(max_calls=3, period=60.0)
        def limited():
            return "ok"

        for _ in range(3):
            limited()  # OK

        with pytest.raises(RuntimeError, match="Rate limit exceeded"):
            limited()  # Should raise

    def test_preserves_function_metadata(self):
        """Should preserve function name."""
        @rate_limit(max_calls=10, period=60.0)
        def my_api():
            """API endpoint."""
            pass

        assert my_api.__name__ == "my_api"


# ============================================================================
# Tests: @memoize
# ============================================================================

class TestMemoize:
    def test_returns_correct_results(self):
        """Memoized function should return correct results."""
        @memoize
        def add(a: int, b: int) -> int:
            return a + b

        assert add(2, 3) == 5
        assert add(10, 20) == 30

    def test_tracks_hits_and_misses(self):
        """Should track cache hits and misses."""
        @memoize
        def square(n: int) -> int:
            return n * n

        square(5)  # miss
        square(5)  # hit
        square(5)  # hit
        square(6)  # miss

        assert square.cache_hits == 2
        assert square.cache_misses == 2

    def test_cache_clear_resets(self):
        """cache_clear() should reset the cache."""
        @memoize
        def identity(x):
            return x

        identity(1)
        identity(1)
        assert identity.cache_hits == 1
        identity.cache_clear()
        assert identity.cache_hits == 0
        assert identity.cache_misses == 0

    def test_handles_keyword_arguments(self):
        """Should work correctly with keyword arguments."""
        @memoize
        def power(base: int, exp: int) -> int:
            return base ** exp

        result1 = power(base=2, exp=10)
        result2 = power(base=2, exp=10)  # hit
        assert result1 == 1024
        assert result2 == 1024
        assert power.cache_hits == 1


# ============================================================================
# Tests: Generators
# ============================================================================

class TestGenerators:
    def test_fibonacci_first_ten(self):
        """First 10 Fibonacci numbers should match the sequence."""
        import itertools
        result = list(itertools.islice(fibonacci(), 10))
        assert result == [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]

    def test_primes_first_ten(self):
        """First 10 primes should be known primes."""
        import itertools
        result = list(itertools.islice(primes(), 10))
        assert result == [2, 3, 5, 7, 11, 13, 17, 19, 23, 29]

    def test_naturals_from_zero(self):
        """naturals(0) should yield 0, 1, 2, 3, ..."""
        import itertools
        result = list(itertools.islice(naturals(0), 5))
        assert result == [0, 1, 2, 3, 4]

    def test_naturals_from_custom_start(self):
        """naturals(10) should start at 10."""
        import itertools
        result = list(itertools.islice(naturals(10), 5))
        assert result == [10, 11, 12, 13, 14]

    def test_flatten_nested_list(self):
        """flatten() should recursively flatten nested lists."""
        nested = [1, [2, [3, 4], 5], [6, 7], 8]
        assert list(flatten(nested)) == [1, 2, 3, 4, 5, 6, 7, 8]

    def test_flatten_empty(self):
        """flatten([]) should yield nothing."""
        assert list(flatten([])) == []

    def test_chunked_even_division(self):
        """chunked should produce chunks of exact size."""
        result = list(chunked(range(6), 2))
        assert result == [[0, 1], [2, 3], [4, 5]]

    def test_chunked_partial_last_chunk(self):
        """Last chunk should be smaller when not evenly divisible."""
        result = list(chunked(range(7), 3))
        assert result == [[0, 1, 2], [3, 4, 5], [6]]

    def test_windowed(self):
        """windowed should produce overlapping windows."""
        result = list(windowed([1, 2, 3, 4, 5], 3))
        assert result == [(1, 2, 3), (2, 3, 4), (3, 4, 5)]

    def test_running_average(self):
        """running_average should compute correct running averages."""
        gen = running_average()
        next(gen)  # Prime
        assert gen.send(10.0) == pytest.approx(10.0)
        assert gen.send(20.0) == pytest.approx(15.0)
        assert gen.send(30.0) == pytest.approx(20.0)

    def test_data_pipeline(self):
        """build_pipeline should filter and transform records correctly."""
        raw_lines = [
            "1, Alice, 100.00",
            "2, Bob, -50.00",    # Negative — filtered out
            "3, Charlie, invalid",  # Not a number — filtered out
            "# comment",          # Skipped
            "4, Diana, 200.00",
        ]
        results = list(build_pipeline(iter(raw_lines), tax_rate=0.10))

        # Should have Alice and Diana (positive amounts only)
        assert len(results) == 2
        assert results[0]["name"] == "Alice"
        assert results[0]["amount"] == pytest.approx(100.0)
        assert results[0]["tax"] == pytest.approx(10.0)
        assert results[1]["name"] == "Diana"


# ============================================================================
# Tests: Async Pipeline
# ============================================================================

class TestAsyncPipeline:
    def test_validate_filters_invalid(self):
        """async_validate should skip records with non-numeric amounts."""
        async def run():
            async def source():
                yield RawRecord(id=1, name="Alice", amount="100.0")
                yield RawRecord(id=2, name="Bob", amount="N/A")
                yield RawRecord(id=3, name="Charlie", amount="50.0")

            stats = PipelineStats()
            records = []
            async for r in async_validate(source(), stats):
                records.append(r)
            return records, stats

        records, stats = asyncio.run(run())
        assert len(records) == 2
        assert stats.total_invalid == 1

    def test_enrich_adds_category(self):
        """async_enrich should add correct category based on amount."""
        async def run():
            async def source():
                yield ValidRecord(id=1, name="A", amount=150.0)  # high
                yield ValidRecord(id=2, name="B", amount=75.0)   # medium
                yield ValidRecord(id=3, name="C", amount=25.0)   # low

            results = []
            async for r in async_enrich(source()):
                results.append(r)
            return results

        results = asyncio.run(run())
        assert results[0].category == "high"
        assert results[1].category == "medium"
        assert results[2].category == "low"

    def test_enrich_adds_tax(self):
        """async_enrich should compute amount_with_tax = amount * 1.10."""
        async def run():
            async def source():
                yield ValidRecord(id=1, name="A", amount=100.0)

            results = []
            async for r in async_enrich(source()):
                results.append(r)
            return results

        results = asyncio.run(run())
        assert results[0].amount_with_tax == pytest.approx(110.0)

    def test_batch_groups_correctly(self):
        """async_batch should group records into batches of batch_size."""
        async def run():
            async def source():
                for i in range(7):
                    yield EnrichedRecord(
                        id=i, name="X", amount=10.0,
                        category="low", amount_with_tax=11.0,
                    )

            batches = []
            async for batch in async_batch(source(), batch_size=3):
                batches.append(batch)
            return batches

        batches = asyncio.run(run())
        assert len(batches) == 3
        assert len(batches[0]) == 3
        assert len(batches[1]) == 3
        assert len(batches[2]) == 1  # Last partial batch

    def test_run_pipeline_returns_stats(self):
        """run_pipeline should return accurate stats."""
        stats = asyncio.run(run_pipeline(n_records=10))
        assert stats.total_raw == 10
        assert stats.total_valid + stats.total_invalid == 10
        assert stats.total_batches > 0
