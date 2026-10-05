"""
Exercise 02: Generators — SOLUTION
=====================================
"""

import sys
from typing import Iterator
from dataclasses import dataclass
from typing import Optional
from collections import defaultdict


@dataclass
class Event:
    event_id: int
    user_id: str
    action: str
    amount: Optional[float]


def generate_events(count: int) -> Iterator[Event]:
    for i in range(1, count + 1):
        user_id = f"user_{i % 20}"
        if i % 50 == 0:
            action = "error"
            amount = None
        elif i % 7 == 0:
            action = "purchase"
            amount = round(10.0 + (i % 200) * 0.5, 2)
        elif i % 5 == 0:
            action = "view"
            amount = None
        else:
            action = "click"
            amount = None
        yield Event(event_id=i, user_id=user_id, action=action, amount=amount)


def filter_by_action(events: Iterator[Event], action: str) -> Iterator[Event]:
    for event in events:
        if event.action == action:
            yield event


def filter_by_user(events: Iterator[Event], user_id: str) -> Iterator[Event]:
    for event in events:
        if event.user_id == user_id:
            yield event


def enrich_events(events: Iterator[Event]) -> Iterator[dict]:
    for event in events:
        is_purchase = event.action == "purchase"
        # "PREMIUM" if user_id ends in even digit
        user_num = int(event.user_id.split("_")[1])
        label = "PREMIUM" if user_num % 2 == 0 else "STANDARD"
        yield {
            "event_id":   event.event_id,
            "user_id":    event.user_id,
            "action":     event.action,
            "amount":     event.amount,
            "is_purchase": is_purchase,
            "revenue":    event.amount if is_purchase else 0.0,
            "label":      label,
        }


def batch(events: Iterator, size: int) -> Iterator[list]:
    current_batch = []
    for item in events:
        current_batch.append(item)
        if len(current_batch) == size:
            yield current_batch
            current_batch = []
    if current_batch:
        yield current_batch


def compute_user_stats(events: Iterator[Event]) -> dict[str, dict]:
    stats = defaultdict(lambda: {"event_count": 0, "purchase_count": 0, "total_revenue": 0.0})
    for event in events:
        s = stats[event.user_id]
        s["event_count"] += 1
        if event.action == "purchase" and event.amount is not None:
            s["purchase_count"] += 1
            s["total_revenue"] += event.amount
    return dict(stats)


def top_n_users_by_revenue(events: Iterator[Event], n: int) -> list[tuple[str, float]]:
    stats = compute_user_stats(events)
    by_revenue = sorted(stats.items(), key=lambda x: -x[1]["total_revenue"])
    return [(user_id, s["total_revenue"]) for user_id, s in by_revenue[:n]]


# Demonstration
if __name__ == "__main__":
    print("=== Generator Pipeline Demo ===\n")

    N = 50_000

    # Show memory efficiency
    pipeline = generate_events(N)
    pipeline = filter_by_action(pipeline, "purchase")
    pipeline = enrich_events(pipeline)

    print(f"Pipeline generator size: {sys.getsizeof(pipeline)} bytes")

    all_events = list(generate_events(N))
    print(f"Full list ({N:,} events): {sys.getsizeof(all_events):,} bytes")

    # Batch processing
    print(f"\nProcessing {N:,} events in batches of 500:")
    events = generate_events(N)
    purchases = filter_by_action(events, "purchase")
    batches = batch(purchases, 500)

    batch_count = 0
    total_revenue = 0.0
    for b in batches:
        batch_count += 1
        total_revenue += sum(e.amount for e in b if e.amount)
    print(f"  Processed {batch_count} batches")
    print(f"  Total purchase revenue: ${total_revenue:,.2f}")

    # Top users
    print(f"\nTop 5 users by revenue:")
    events = generate_events(N)
    top = top_n_users_by_revenue(events, 5)
    for user, rev in top:
        print(f"  {user}: ${rev:,.2f}")

    # User stats for user_7
    print(f"\nuser_7 events:")
    events = generate_events(1000)
    user7_events = filter_by_user(events, "user_7")
    stats = compute_user_stats(user7_events)
    if "user_7" in stats:
        s = stats["user_7"]
        print(f"  Events: {s['event_count']}, Purchases: {s['purchase_count']}, "
              f"Revenue: ${s['total_revenue']:.2f}")
