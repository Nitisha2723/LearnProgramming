"""
test_generators.py — pytest tests for generators and itertools

Topics tested:
  - Generator functions (yield)
  - Generator expressions
  - Lazy evaluation
  - Chaining generators
  - itertools functions
  - Memory efficiency
"""

import sys
import types
import itertools
import pytest
from typing import Iterator


# =============================================================================
# Generator functions under test
# =============================================================================

def count_up(start: int, stop: int, step: int = 1) -> Iterator[int]:
    current = start
    while current <= stop:
        yield current
        current += step


def fibonacci(limit: int) -> Iterator[int]:
    """Yield Fibonacci numbers up to limit."""
    a, b = 0, 1
    while a <= limit:
        yield a
        a, b = b, a + b


def flatten(nested) -> Iterator:
    """Flatten arbitrarily nested lists."""
    for item in nested:
        if isinstance(item, list):
            yield from flatten(item)
        else:
            yield item


def running_total(numbers: Iterator[float]) -> Iterator[float]:
    """Yield running sum."""
    total = 0.0
    for n in numbers:
        total += n
        yield total


def take_while_under(numbers: Iterator[int], threshold: int) -> Iterator[int]:
    """Yield numbers until one meets or exceeds threshold."""
    for n in numbers:
        if n >= threshold:
            break
        yield n


def batch_generator(items: Iterator, size: int) -> Iterator[list]:
    """Yield batches of 'size' items."""
    current = []
    for item in items:
        current.append(item)
        if len(current) == size:
            yield current
            current = []
    if current:
        yield current


def read_lines_lazy(lines: list[str]) -> Iterator[str]:
    """Yield lines one at a time, stripping whitespace."""
    for line in lines:
        yield line.strip()


def pipeline(source: Iterator[int]) -> Iterator[int]:
    """Apply: filter evens, square, add 1."""
    for n in source:
        if n % 2 == 0:
            yield n**2 + 1


# =============================================================================
# Tests: count_up
# =============================================================================

class TestCountUp:

    def test_basic_range(self):
        result = list(count_up(1, 5))
        assert result == [1, 2, 3, 4, 5]

    def test_with_step(self):
        result = list(count_up(0, 10, 2))
        assert result == [0, 2, 4, 6, 8, 10]

    def test_single_element(self):
        assert list(count_up(5, 5)) == [5]

    def test_empty_when_start_above_stop(self):
        assert list(count_up(10, 5)) == []

    def test_returns_generator(self):
        gen = count_up(1, 10)
        assert isinstance(gen, types.GeneratorType)

    def test_lazy_evaluation(self):
        """Generator doesn't compute all values upfront."""
        gen = count_up(1, 1_000_000)
        first = next(gen)
        assert first == 1   # Only computed one value

    @pytest.mark.parametrize("start, stop, step, expected", [
        (1, 5, 1,  [1, 2, 3, 4, 5]),
        (0, 8, 3,  [0, 3, 6]),
        (5, 5, 1,  [5]),
        (10, 1, 1, []),
    ])
    def test_parametrized(self, start, stop, step, expected):
        assert list(count_up(start, stop, step)) == expected


# =============================================================================
# Tests: fibonacci
# =============================================================================

class TestFibonacci:

    def test_first_few(self):
        result = list(fibonacci(20))
        assert result == [0, 1, 1, 2, 3, 5, 8, 13]

    def test_includes_limit(self):
        result = list(fibonacci(13))
        assert 13 in result

    def test_returns_generator(self):
        gen = fibonacci(100)
        assert isinstance(gen, types.GeneratorType)

    def test_zero(self):
        result = list(fibonacci(0))
        assert result == [0]

    def test_consecutive_differences_increase(self):
        fibs = list(fibonacci(1000))
        diffs = [fibs[i+1] - fibs[i] for i in range(len(fibs)-1)]
        assert all(d >= 0 for d in diffs)    # Non-decreasing


# =============================================================================
# Tests: flatten
# =============================================================================

class TestFlatten:

    def test_simple_nested(self):
        assert list(flatten([1, [2, 3], [4, 5]])) == [1, 2, 3, 4, 5]

    def test_deeply_nested(self):
        nested = [1, [2, [3, [4, [5]]]]]
        assert list(flatten(nested)) == [1, 2, 3, 4, 5]

    def test_already_flat(self):
        assert list(flatten([1, 2, 3])) == [1, 2, 3]

    def test_empty(self):
        assert list(flatten([])) == []

    def test_mixed_types(self):
        result = list(flatten([1, "hello", [2, [True, None]]]))
        assert result == [1, "hello", 2, True, None]


# =============================================================================
# Tests: running_total
# =============================================================================

class TestRunningTotal:

    def test_basic(self):
        result = list(running_total(iter([1, 2, 3, 4, 5])))
        assert result == [1.0, 3.0, 6.0, 10.0, 15.0]

    def test_empty(self):
        assert list(running_total(iter([]))) == []

    def test_single(self):
        assert list(running_total(iter([42]))) == [42.0]

    def test_returns_generator(self):
        gen = running_total(iter([1, 2, 3]))
        assert isinstance(gen, types.GeneratorType)


# =============================================================================
# Tests: batch_generator
# =============================================================================

class TestBatchGenerator:

    def test_even_batches(self):
        result = list(batch_generator(iter(range(6)), 2))
        assert result == [[0, 1], [2, 3], [4, 5]]

    def test_uneven_last_batch(self):
        result = list(batch_generator(iter(range(5)), 2))
        assert result == [[0, 1], [2, 3], [4]]

    def test_single_batch(self):
        result = list(batch_generator(iter(range(3)), 10))
        assert result == [[0, 1, 2]]

    def test_batch_size_one(self):
        result = list(batch_generator(iter(range(3)), 1))
        assert result == [[0], [1], [2]]

    def test_empty_source(self):
        result = list(batch_generator(iter([]), 5))
        assert result == []

    def test_returns_generator(self):
        gen = batch_generator(iter(range(10)), 3)
        assert isinstance(gen, types.GeneratorType)

    @pytest.mark.parametrize("n, size, expected_batch_count", [
        (10, 3, 4),   # 3+3+3+1
        (9,  3, 3),   # 3+3+3
        (0,  3, 0),   # empty
        (1,  5, 1),   # just one partial batch
    ])
    def test_batch_counts(self, n, size, expected_batch_count):
        result = list(batch_generator(iter(range(n)), size))
        assert len(result) == expected_batch_count


# =============================================================================
# Tests: pipeline (chained generators)
# =============================================================================

class TestPipeline:

    def test_pipeline(self):
        # Filter even, square, add 1
        # From 1..10: even numbers are 2,4,6,8,10
        # Squared: 4, 16, 36, 64, 100
        # Plus 1:  5, 17, 37, 65, 101
        result = list(pipeline(iter(range(1, 11))))
        assert result == [5, 17, 37, 65, 101]

    def test_pipeline_returns_generator(self):
        gen = pipeline(iter(range(10)))
        assert isinstance(gen, types.GeneratorType)

    def test_chained_pipelines(self):
        """Two stages of filtering."""
        numbers = iter(range(1, 20))
        stage1 = (x for x in numbers if x % 2 == 0)   # Even
        stage2 = (x for x in stage1 if x % 3 == 0)    # Divisible by 6
        result = list(stage2)
        assert result == [6, 12, 18]


# =============================================================================
# Tests: Memory efficiency
# =============================================================================

class TestMemoryEfficiency:

    def test_generator_smaller_than_list(self):
        N = 100_000
        list_comp = [x**2 for x in range(N)]
        gen_expr  = (x**2 for x in range(N))

        assert sys.getsizeof(gen_expr) < sys.getsizeof(list_comp)

    def test_generator_exhausted_after_iteration(self):
        gen = count_up(1, 5)
        list(gen)               # Exhaust the generator
        assert list(gen) == []  # Empty on second pass

    def test_islice_limits_computation(self):
        """itertools.islice should stop the generator early."""
        computed = []

        def track_computation():
            for i in range(100):
                computed.append(i)
                yield i

        result = list(itertools.islice(track_computation(), 5))
        assert result == [0, 1, 2, 3, 4]
        assert len(computed) == 5   # Only 5 values computed, not 100


# =============================================================================
# Tests: itertools
# =============================================================================

class TestItertools:

    def test_chain(self):
        result = list(itertools.chain([1, 2], [3, 4], [5]))
        assert result == [1, 2, 3, 4, 5]

    def test_islice(self):
        gen = (x for x in range(1000))
        result = list(itertools.islice(gen, 5))
        assert result == [0, 1, 2, 3, 4]

    def test_cycle_with_islice(self):
        result = list(itertools.islice(itertools.cycle([1, 2, 3]), 7))
        assert result == [1, 2, 3, 1, 2, 3, 1]

    def test_product(self):
        result = list(itertools.product([1, 2], ["a", "b"]))
        assert result == [(1, "a"), (1, "b"), (2, "a"), (2, "b")]

    def test_combinations(self):
        result = list(itertools.combinations("ABC", 2))
        assert result == [("A","B"), ("A","C"), ("B","C")]

    def test_permutations(self):
        result = sorted(itertools.permutations("AB", 2))
        assert result == [("A","B"), ("B","A")]

    def test_accumulate(self):
        result = list(itertools.accumulate([1, 2, 3, 4, 5]))
        assert result == [1, 3, 6, 10, 15]

    def test_takewhile(self):
        result = list(itertools.takewhile(lambda x: x < 5, [1, 2, 3, 7, 4]))
        assert result == [1, 2, 3]   # Stops at 7

    def test_dropwhile(self):
        result = list(itertools.dropwhile(lambda x: x < 5, [1, 2, 3, 7, 4]))
        assert result == [7, 4]      # Drops until 7, takes rest

    @pytest.mark.parametrize("r, expected_count", [
        (2, 6),   # C(4,2) = 6
        (3, 4),   # C(4,3) = 4
        (4, 1),   # C(4,4) = 1
    ])
    def test_combinations_count(self, r, expected_count):
        result = list(itertools.combinations("ABCD", r))
        assert len(result) == expected_count
