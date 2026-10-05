"""
Exercise 02: Generators
========================

Build a lazy data processing pipeline using generators.

Learning goals:
  - Generator functions with yield
  - Chaining generators as a pipeline
  - yield from
  - Memory efficiency of generators vs lists

You are processing a large stream of "events" — think of these as log lines
or records from a large file. The pipeline must work lazily (one record at a time).

Complete the generator functions below.
"""

import sys
from typing import Iterator


# ---------------------------------------------------------------------------
# Event type
# ---------------------------------------------------------------------------

from dataclasses import dataclass
from typing import Optional


@dataclass
class Event:
    """Represents a single event/record."""
    event_id: int
    user_id: str
    action: str       # "click", "purchase", "view", "error"
    amount: Optional[float]  # Only for "purchase" events


# ---------------------------------------------------------------------------
# Generator functions to implement
# ---------------------------------------------------------------------------

def generate_events(count: int) -> Iterator[Event]:
    """
    Generate a sequence of simulated events.

    Generates 'count' events with:
    - event_id: 1, 2, 3, ...
    - user_id: "user_{i % 20}" (20 different users)
    - action: "click" for most, "purchase" every 7th, "view" every 5th, "error" every 50th
    - amount: a dollar amount for "purchase" events, None otherwise

    This is a generator — yield one event at a time.

    Args:
        count: Total number of events to generate

    Yields:
        Event objects
    """
    # YOUR CODE HERE
    pass


def filter_by_action(events: Iterator[Event], action: str) -> Iterator[Event]:
    """
    Filter events by action type.

    Yields only events where event.action == action.
    This is a generator — don't build a list.

    Args:
        events: Event iterator
        action: The action to filter for

    Yields:
        Matching Event objects
    """
    # YOUR CODE HERE
    pass


def filter_by_user(events: Iterator[Event], user_id: str) -> Iterator[Event]:
    """
    Filter events to only those from a specific user.

    Yields:
        Events from the specified user
    """
    # YOUR CODE HERE
    pass


def enrich_events(events: Iterator[Event]) -> Iterator[dict]:
    """
    Transform events into enriched dicts with additional computed fields.

    For each event, yield a dict with:
    - All original fields (event_id, user_id, action, amount)
    - "is_purchase": True if action == "purchase"
    - "revenue": amount if is_purchase else 0.0
    - "label": "PREMIUM" if user_id ends in even number, "STANDARD" otherwise

    This is a generator.
    """
    # YOUR CODE HERE
    pass


def batch(events: Iterator, size: int) -> Iterator[list]:
    """
    Group events into batches of 'size'.
    The last batch may be smaller than 'size'.

    Example:
        batch([1,2,3,4,5], 2) → [1,2], [3,4], [5]

    This is a generator — yield one batch at a time.
    """
    # YOUR CODE HERE
    pass


def compute_user_stats(events: Iterator[Event]) -> dict[str, dict]:
    """
    Consume a stream of events and compute per-user statistics.

    Returns a dict: user_id → {
        "event_count": int,
        "purchase_count": int,
        "total_revenue": float,
    }

    Note: This function CANNOT be a generator because it must consume
    the entire stream to compute final totals. It returns a dict.
    """
    # YOUR CODE HERE
    pass


def top_n_users_by_revenue(events: Iterator[Event], n: int) -> list[tuple[str, float]]:
    """
    Find the top N users by total revenue from purchases.

    Returns: list of (user_id, total_revenue) sorted by revenue descending.

    This is NOT a generator (must consume all events to rank them).
    """
    # YOUR CODE HERE
    pass


# ---------------------------------------------------------------------------
# TESTS
# ---------------------------------------------------------------------------

def test_generate_events():
    events = list(generate_events(10))
    assert len(events) == 10
    assert events[0].event_id == 1
    assert events[-1].event_id == 10
    assert all(isinstance(e, Event) for e in events)
    print("  generate_events: PASSED")


def test_filter_by_action():
    # Must work lazily (test that it's a generator)
    events = generate_events(100)
    purchases = filter_by_action(events, "purchase")

    # Should be a generator
    import types
    assert isinstance(purchases, types.GeneratorType), \
        "filter_by_action should return a generator"

    purchase_list = list(purchases)
    assert all(e.action == "purchase" for e in purchase_list)
    assert len(purchase_list) > 0
    print("  filter_by_action: PASSED")


def test_enrich_events():
    events = list(generate_events(100))
    purchases = filter_by_action(iter(events), "purchase")
    enriched = list(enrich_events(purchases))

    assert all("is_purchase" in e for e in enriched)
    assert all(e["is_purchase"] for e in enriched)
    assert all("revenue" in e for e in enriched)
    assert all(e["revenue"] > 0 for e in enriched)
    print("  enrich_events: PASSED")


def test_batch():
    items = list(range(10))
    batches = list(batch(iter(items), 3))
    assert batches == [[0,1,2], [3,4,5], [6,7,8], [9]]
    print("  batch: PASSED")


def test_user_stats():
    events = generate_events(1000)
    stats = compute_user_stats(events)
    assert len(stats) > 0
    assert all("event_count" in v for v in stats.values())
    assert all("total_revenue" in v for v in stats.values())

    # Revenue only comes from purchases
    events2 = generate_events(1000)
    non_purchase_events = filter_by_action(events2, "click")
    stats2 = compute_user_stats(non_purchase_events)
    assert all(v["total_revenue"] == 0 for v in stats2.values())
    print("  compute_user_stats: PASSED")


def test_memory_efficiency():
    """
    Demonstrate that the pipeline uses less memory than materializing everything.
    """
    import sys

    N = 100_000

    # Pipeline approach — generator, minimal memory
    pipeline = generate_events(N)
    pipeline = filter_by_action(pipeline, "purchase")
    pipeline = enrich_events(pipeline)

    gen_size = sys.getsizeof(pipeline)

    # List approach — stores all N events
    all_events = list(generate_events(N))
    list_size = sys.getsizeof(all_events)

    print(f"  Pipeline generator size: {gen_size} bytes")
    print(f"  Full list size:          {list_size:,} bytes")
    assert gen_size < list_size, "Generator should use less memory than full list"
    print("  memory_efficiency: PASSED")


if __name__ == "__main__":
    print("Running tests...\n")
    try:
        test_generate_events()
        test_filter_by_action()
        test_enrich_events()
        test_batch()
        test_user_stats()
        test_memory_efficiency()
        print("\nAll tests passed!")

        # Demo
        print("\n--- Top 5 users by revenue ---")
        events = generate_events(10_000)
        top = top_n_users_by_revenue(events, 5)
        if top:
            for user, rev in top:
                print(f"  {user}: ${rev:.2f}")
    except AssertionError as e:
        print(f"\nTest FAILED: {e}")
    except TypeError:
        print("\nTest FAILED: returned None — implement the generators!")
