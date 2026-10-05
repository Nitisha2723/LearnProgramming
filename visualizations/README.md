# Algorithm & Concept Visualizations

Interactive browser-based visualizations for learning programming algorithms and concepts.

## How to Open

Just double-click any `.html` file — or open it in your browser directly. No server, no npm, no installation needed. Everything runs in vanilla HTML, CSS, and JavaScript.

---

## Algorithms

### Sorting

| File | What it teaches | Complexity |
|------|----------------|------------|
| [algorithms/sorting/bubble-sort.html](algorithms/sorting/bubble-sort.html) | Bubble sort step-by-step with comparison and swap animation | O(n²) |
| [algorithms/sorting/merge-sort.html](algorithms/sorting/merge-sort.html) | Divide-and-conquer — shows the split and merge phases | O(n log n) |
| [algorithms/sorting/quick-sort.html](algorithms/sorting/quick-sort.html) | Pivot selection, partitioning, recursive subarray sorting | O(n log n) avg |

### Searching

| File | What it teaches | Complexity |
|------|----------------|------------|
| [algorithms/searching/binary-search.html](algorithms/searching/binary-search.html) | Halving the search space, pointer movement, comparison to linear | O(log n) |
| [algorithms/searching/linear-search.html](algorithms/searching/linear-search.html) | Sequential scan, when it's necessary vs binary search | O(n) |

### Data Structures

| File | What it teaches |
|------|----------------|
| [algorithms/data-structures/stack-queue.html](algorithms/data-structures/stack-queue.html) | LIFO vs FIFO, push/pop vs enqueue/dequeue, real-world uses |
| [algorithms/data-structures/linked-list.html](algorithms/data-structures/linked-list.html) | Node/pointer model, insertion/deletion/traversal animations |
| [algorithms/data-structures/hash-table.html](algorithms/data-structures/hash-table.html) | Hash functions, bucket allocation, collision chaining, load factor |

---

## Concepts

### Object-Oriented Programming

| File | What it teaches |
|------|----------------|
| [concepts/oop/classes-objects.html](concepts/oop/classes-objects.html) | Class as blueprint, instantiation, independent object state |
| [concepts/oop/inheritance.html](concepts/oop/inheritance.html) | Inheritance hierarchy, method overriding, polymorphism |

### Memory Management

| File | What it teaches |
|------|----------------|
| [concepts/memory/stack-vs-heap.html](concepts/memory/stack-vs-heap.html) | Stack frames vs heap objects, references, scope and lifetime |
| [concepts/memory/garbage-collection.html](concepts/memory/garbage-collection.html) | Mark-and-sweep GC, reachability, object graphs |

### Recursion

| File | What it teaches |
|------|----------------|
| [concepts/recursion/recursion-visualizer.html](concepts/recursion/recursion-visualizer.html) | Fibonacci call tree building live, call stack frames, redundant subproblems |

---

## Java vs Python Comparisons

| File | What it teaches |
|------|----------------|
| [java-python/type-systems.html](java-python/type-systems.html) | Static vs dynamic typing, type checking, declarations, casting |
| [java-python/execution-model.html](java-python/execution-model.html) | Compilation pipeline — .java → javac → JVM vs .py → CPython → PVM |

---

## Design

All pages share a consistent dark theme:
- Background: `#1a1a2e`
- Panels: `#16213e`
- Accent: `#e94560`
- Font: system-ui / Segoe UI

---

## Related Repositories

These visualizations complement two programming learning repos in this workspace:
- Java learning module
- Python learning module

Each visualization notes which module and topic it relates to at the bottom of the page.
