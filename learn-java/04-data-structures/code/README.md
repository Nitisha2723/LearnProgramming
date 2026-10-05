# Module 04: Code Examples

## Files

| File | What It Demonstrates |
|------|----------------------|
| `ListsDemo.java` | ArrayList operations, sorting with Comparator, list transformations, shopping cart |
| `MapsDemo.java` | HashMap patterns, frequency counting, grouping, memoization, LRU cache, TreeMap |
| `SetsDemo.java` | Set operations (union/intersection/difference), deduplication, LinkedHashSet, TreeSet |
| `StackQueueDemo.java` | Browser history (stack), task queue (FIFO), priority queue, sliding window deque |

## How to Compile and Run

```bash
# From the 04-data-structures/code/ directory:
javac ListsDemo.java && java ListsDemo
javac MapsDemo.java && java MapsDemo
javac SetsDemo.java && java SetsDemo
javac StackQueueDemo.java && java StackQueueDemo
```

## Key Concepts Covered

- ArrayList O(1) random access vs O(n) middle insert
- Comparator chaining with `.thenComparing()`
- HashMap `merge()`, `computeIfAbsent()`, `getOrDefault()`
- Frequency counter pattern (most common interview pattern)
- Set operations as one-liners with `addAll`, `retainAll`, `removeAll`
- Stack pattern with `ArrayDeque.push()/pop()`
- Queue pattern with `ArrayDeque.offer()/poll()`
- PriorityQueue with custom `Comparable`
- Sliding window maximum with Deque
