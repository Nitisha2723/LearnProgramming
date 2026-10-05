# Code Examples for Module 04: Data Structures

Run these files to see all concepts from the theory files in action.

## Files

### `lists_advanced.py`
Demonstrates advanced list operations:
- Slicing with start:stop:step
- List comprehensions (basic, filtered, nested)
- Sorting with key functions and lambda
- List as stack (append/pop)
- deque vs list.pop(0) performance comparison

### `dicts_advanced.py`
Demonstrates dictionary patterns:
- Dict comprehensions
- Counter for frequency counting and arithmetic
- defaultdict for grouping and accumulation
- Nested dicts
- Dict merging with | operator

### `sets_demo.py`
Demonstrates set operations:
- Union, intersection, difference, symmetric difference
- Set comprehensions
- Deduplication (with and without order preservation)
- Membership testing performance: set O(1) vs list O(n)
- frozenset as dict keys and constants

### `collections_demo.py`
Demonstrates the collections module:
- deque: browser history simulation, sliding window log buffer
- Counter: text analysis, anagram detection, vote tallying
- namedtuple: geographic points, database records, multi-value function returns
- defaultdict: inverted index (mini search engine)
- ChainMap: layered configuration system

## Running

```bash
cd 04-data-structures/code

python lists_advanced.py
python dicts_advanced.py
python sets_demo.py
python collections_demo.py
```

No external dependencies required — standard library only.
