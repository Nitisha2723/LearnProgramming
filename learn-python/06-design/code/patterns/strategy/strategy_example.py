"""
Strategy Pattern — Python Examples

Demonstrates both function-based (Pythonic) and Protocol-based strategies.
"""

from typing import Protocol, Callable
from dataclasses import dataclass
import time


# ─── Function-Based Strategy (Most Pythonic) ─────────────────────────────────

# A sort strategy is just a function
SortStrategy = Callable[[list[int]], list[int]]


def bubble_sort(items: list[int]) -> list[int]:
    """O(n²) insertion sort — educational, not for production."""
    items = list(items)
    n = len(items)
    for i in range(n):
        for j in range(n - i - 1):
            if items[j] > items[j + 1]:
                items[j], items[j + 1] = items[j + 1], items[j]
    return items


def merge_sort(items: list[int]) -> list[int]:
    """O(n log n) — reliable general-purpose sort."""
    if len(items) <= 1:
        return list(items)
    mid = len(items) // 2
    left = merge_sort(items[:mid])
    right = merge_sort(items[mid:])
    return _merge(left, right)


def _merge(left: list[int], right: list[int]) -> list[int]:
    result, i, j = [], 0, 0
    while i < len(left) and j < len(right):
        if left[i] <= right[j]:
            result.append(left[i]); i += 1
        else:
            result.append(right[j]); j += 1
    return result + left[i:] + right[j:]


def builtin_timsort(items: list[int]) -> list[int]:
    """Python's built-in Timsort — fastest in practice."""
    return sorted(items)


def reverse_sort(items: list[int]) -> list[int]:
    """Sort in descending order."""
    return sorted(items, reverse=True)


class DataProcessor:
    """Context class that uses a sort strategy."""

    def __init__(self, strategy: SortStrategy = builtin_timsort):
        self._strategy = strategy

    def set_strategy(self, strategy: SortStrategy) -> None:
        self._strategy = strategy

    def process(self, data: list[int]) -> list[int]:
        start = time.perf_counter()
        result = self._strategy(data)
        elapsed = (time.perf_counter() - start) * 1000
        print(f"[{self._strategy.__name__}] Sorted {len(data)} items in {elapsed:.2f}ms")
        return result


# ─── Protocol-Based Strategy (for complex strategies) ────────────────────────

@dataclass
class TextDocument:
    content: str


class TextFormatter(Protocol):
    """Protocol for text formatting strategies."""

    def format(self, doc: TextDocument) -> str:
        ...

    def file_extension(self) -> str:
        ...


class HtmlFormatter:
    def format(self, doc: TextDocument) -> str:
        lines = doc.content.split("\n")
        paragraphs = "\n".join(f"<p>{line}</p>" for line in lines if line)
        return f"<!DOCTYPE html>\n<html>\n<body>\n{paragraphs}\n</body>\n</html>"

    def file_extension(self) -> str:
        return ".html"


class MarkdownFormatter:
    def format(self, doc: TextDocument) -> str:
        return doc.content  # Content is already markdown

    def file_extension(self) -> str:
        return ".md"


class PlainTextFormatter:
    def format(self, doc: TextDocument) -> str:
        # Strip any HTML or markdown
        import re
        text = re.sub(r"<[^>]+>", "", doc.content)
        return text.strip()

    def file_extension(self) -> str:
        return ".txt"


class DocumentExporter:
    def __init__(self, formatter: TextFormatter):
        self._formatter = formatter

    def export(self, doc: TextDocument, filename: str) -> str:
        """Export document using the configured formatter."""
        full_name = filename + self._formatter.file_extension()
        formatted = self._formatter.format(doc)
        print(f"[Exporter] Writing {len(formatted)} chars to {full_name}")
        return formatted


# ─── Demo ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    import random

    print("=== Sort Strategies ===")
    data = random.sample(range(1000), 100)

    processor = DataProcessor(builtin_timsort)
    processor.process(data)

    processor.set_strategy(merge_sort)
    processor.process(data)

    processor.set_strategy(bubble_sort)
    processor.process(data[:20])  # bubble sort only for small input

    print("\n=== Text Formatter Strategies ===")
    doc = TextDocument("Hello, World!\n\nThis is a test document.\nWith multiple paragraphs.")

    for formatter in [HtmlFormatter(), MarkdownFormatter(), PlainTextFormatter()]:
        exporter = DocumentExporter(formatter)
        exporter.export(doc, "output")
