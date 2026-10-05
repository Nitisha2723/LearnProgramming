"""
Exercise 02: Async Data Processing Pipeline

Build an async pipeline that:
1. Reads records from a simulated async source
2. Filters and transforms them concurrently
3. Batches results for efficient downstream processing
4. Tracks statistics

EXERCISES:
2A: Implement async_record_source() — yields records from a simulated API
2B: Implement async_validate() — filters out invalid records
2C: Implement async_enrich() — adds computed fields
2D: Implement async_batch() — groups records into batches
2E: Implement run_pipeline() — wires all stages together

Run tests with: pytest tests/test_advanced.py -v
"""

import asyncio
import random
from dataclasses import dataclass, field
from typing import AsyncIterator


# ============================================================================
# Data Models
# ============================================================================

@dataclass
class RawRecord:
    id: int
    name: str
    amount: str  # String — may be invalid


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
    category: str      # Derived from amount
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


# ============================================================================
# EXERCISE 2A: Async Record Source
# ============================================================================
#
# Implement an async generator that "fetches" records from a simulated API.
# Each call to the API returns one batch of records.
# Simulate network latency with asyncio.sleep().
#
# The function should:
# - Yield RawRecord objects one at a time
# - Simulate ~0.05s delay between batches (not between each record)
# - Include some records with invalid amounts ("N/A", "error", etc.)
#
# HINT: Use `async def` with `yield` to make an async generator.
#       The caller uses `async for record in async_record_source(n):`.

async def async_record_source(n: int) -> AsyncIterator[RawRecord]:
    """
    Async generator: yields n RawRecords, simulating API latency.

    Some records will have invalid amount values.

    Args:
        n: Number of records to generate.

    Yields:
        RawRecord objects.
    """
    # TODO: Implement this async generator
    # Suggestion: Use batches of 5. After each batch, await asyncio.sleep(0.05).
    # Mix valid amounts like "10.50" with invalid ones like "N/A".
    pass


# ============================================================================
# EXERCISE 2B: Async Validation Stage
# ============================================================================
#
# Implement an async generator that:
# - Receives RawRecords from the upstream generator
# - Tries to parse each record's amount as a float
# - Yields ValidRecord for parseable records
# - Skips invalid ones (and increments stats.total_invalid)
#
# HINT: Try/except float() conversion. Use `async for` to consume upstream.

async def async_validate(
    source: AsyncIterator[RawRecord],
    stats: PipelineStats,
) -> AsyncIterator[ValidRecord]:
    """
    Validation stage: parse amounts, discard invalid records.

    Args:
        source: Upstream async iterator of RawRecords.
        stats: Mutable stats object to track invalid record count.

    Yields:
        ValidRecord objects (only records with parseable amounts).
    """
    # TODO: Implement this async generator
    pass


# ============================================================================
# EXERCISE 2C: Async Enrichment Stage
# ============================================================================
#
# Implement an async generator that adds derived fields:
# - category: "high" if amount > 100, "medium" if > 50, else "low"
# - amount_with_tax: amount * 1.10 (10% tax)
#
# Simulate async enrichment (e.g., a database lookup) with a tiny sleep.

async def async_enrich(
    source: AsyncIterator[ValidRecord],
) -> AsyncIterator[EnrichedRecord]:
    """
    Enrichment stage: add category and tax fields.

    Args:
        source: Upstream async iterator of ValidRecords.

    Yields:
        EnrichedRecord objects with derived fields added.
    """
    # TODO: Implement this async generator
    # category: "high" if amount > 100, "medium" if > 50, else "low"
    # amount_with_tax: round(amount * 1.10, 2)
    pass


# ============================================================================
# EXERCISE 2D: Async Batching Stage
# ============================================================================
#
# Implement an async generator that collects records into batches of size N.
# The last batch may be smaller than N.
#
# HINT: Accumulate items into a list. When it reaches batch_size, yield it.
#       After the loop, yield any remaining items.

async def async_batch(
    source: AsyncIterator[EnrichedRecord],
    batch_size: int = 5,
) -> AsyncIterator[list[EnrichedRecord]]:
    """
    Batching stage: group records into lists of batch_size.

    Args:
        source: Upstream async iterator of EnrichedRecords.
        batch_size: Number of records per batch.

    Yields:
        Lists of EnrichedRecord (last list may be smaller).
    """
    # TODO: Implement this async generator
    pass


# ============================================================================
# EXERCISE 2E: Pipeline Orchestrator
# ============================================================================
#
# Wire all stages together and return a PipelineStats object.
# The pipeline flow: source → validate → enrich → batch → process
#
# Also: while the pipeline is running, start a background "heartbeat" task
# that prints a progress message every 0.5 seconds.

async def run_pipeline(n_records: int = 20) -> PipelineStats:
    """
    Run the full async data processing pipeline.

    Returns:
        PipelineStats with counts of processed and invalid records.
    """
    stats = PipelineStats()

    # TODO: Wire the pipeline stages together
    # 1. Create source = async_record_source(n_records)
    # 2. Create validated = async_validate(source, stats)
    # 3. Create enriched = async_enrich(validated)
    # 4. Create batched = async_batch(enriched, batch_size=5)
    # 5. Iterate over batched and process each batch:
    #    - Update stats.total_batches
    #    - Print batch summary
    # 6. Set stats.total_raw = n_records before returning

    return stats


# ============================================================================
# MANUAL TESTING
# ============================================================================

async def main():
    print("Running async pipeline...")
    stats = await run_pipeline(n_records=20)
    print(f"\nPipeline complete!")
    print(f"  Total raw records: {stats.total_raw}")
    print(f"  Valid: {stats.total_valid}")
    print(f"  Invalid: {stats.total_invalid}")
    print(f"  Batches: {stats.total_batches}")
    print(f"  Validity rate: {stats.validity_rate:.0%}")


if __name__ == "__main__":
    asyncio.run(main())
