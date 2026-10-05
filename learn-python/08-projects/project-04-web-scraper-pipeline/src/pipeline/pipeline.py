"""
Generator-based data pipeline for memory-efficient processing.

Each step is a generator function that consumes an iterable and yields items.
Steps chain together so data flows through lazily — only as much as needed
is held in memory at any time.
"""

from typing import Callable, Generator, Iterable, Iterator, TypeVar

T = TypeVar("T")
U = TypeVar("U")

PipelineStep = Callable[[Iterable], Iterator]


class Pipeline:
    """
    Composable generator pipeline.

    Example:
        pipeline = (
            Pipeline(source_data)
            .filter(lambda x: x["word_count"] > 100)
            .map(lambda x: {**x, "category": "long"})
            .limit(20)
        )
        results = list(pipeline)
    """

    def __init__(self, source: Iterable) -> None:
        self._source = source

    def filter(self, predicate: Callable[[T], bool]) -> "Pipeline":
        """Keep only items where predicate returns True."""
        def _filter(src):
            for item in src:
                if predicate(item):
                    yield item
        return Pipeline(_filter(self._source))

    def map(self, transform: Callable[[T], U]) -> "Pipeline":
        """Apply transform to every item."""
        def _map(src):
            for item in src:
                yield transform(item)
        return Pipeline(_map(self._source))

    def limit(self, n: int) -> "Pipeline":
        """Yield at most n items."""
        def _limit(src):
            count = 0
            for item in src:
                if count >= n:
                    break
                yield item
                count += 1
        return Pipeline(_limit(self._source))

    def skip(self, n: int) -> "Pipeline":
        """Skip first n items."""
        def _skip(src):
            skipped = 0
            for item in src:
                if skipped < n:
                    skipped += 1
                    continue
                yield item
        return Pipeline(_skip(self._source))

    def batch(self, size: int) -> "Pipeline":
        """Group items into batches of given size."""
        def _batch(src):
            batch = []
            for item in src:
                batch.append(item)
                if len(batch) == size:
                    yield batch
                    batch = []
            if batch:
                yield batch
        return Pipeline(_batch(self._source))

    def flat_map(self, transform: Callable[[T], Iterable[U]]) -> "Pipeline":
        """Map each item to an iterable and flatten the result."""
        def _flat_map(src):
            for item in src:
                yield from transform(item)
        return Pipeline(_flat_map(self._source))

    def apply(self, step: PipelineStep) -> "Pipeline":
        """Apply a custom generator step."""
        return Pipeline(step(self._source))

    def __iter__(self) -> Iterator:
        return iter(self._source)

    def to_list(self) -> list:
        """Materialise the pipeline into a list."""
        return list(self)

    def count(self) -> int:
        """Consume the pipeline and count items."""
        return sum(1 for _ in self)
