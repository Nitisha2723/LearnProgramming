# Stack, Queue, and Deque

## The LIFO vs FIFO Mental Model

```
STACK (Last In, First Out — LIFO)         QUEUE (First In, First Out — FIFO)
Think: stack of plates                     Think: queue at a coffee shop

    TOP
  ┌─────┐     push("C") →  ┌─────┐       FRONT  BACK
  │  C  │ ← pop returns C  │     │       ←────  ────→
  │  B  │                  │  B  │
  │  A  │                  │  A  │       enqueue("A")  → [A]
  └─────┘                  └─────┘       enqueue("B")  → [A, B]
  BOTTOM                                 dequeue()     → A (returns A, leaves [B])
```

---

## Stack: LIFO (Last In, First Out)

### Real-World Uses

- **Undo/redo** — text editors, Photoshop, IDEs
- **Function call stack** — how the JVM handles method calls
- **Browser back button** — each visit is pushed, back pops
- **Expression evaluation** — `(2 + 3) * 4` parsed with a stack
- **Depth-First Search** (DFS) in graphs
- **Parenthesis matching** — is `{[()]}` balanced?
- **Backtracking algorithms** — maze solving, chess engines

### The Old Way: Stack Class (Avoid)

`java.util.Stack<E>` extends `Vector` (a legacy, synchronized class). **Don't use it.** It was a poor design choice — extending `Vector` adds methods like `elementAt(index)` which don't make sense for a stack.

### The Right Way: ArrayDeque as Stack

```java
Deque<String> stack = new ArrayDeque<>();

stack.push("first");   // Adds to front → ["first"]
stack.push("second");  // Adds to front → ["second", "first"]
stack.push("third");   // Adds to front → ["third", "second", "first"]

stack.peek();  // "third" — look at top without removing
stack.pop();   // "third" — remove and return top → ["second", "first"]
stack.pop();   // "second" → ["first"]
stack.isEmpty(); // false
stack.size();    // 1
```

### Practical Example: Balanced Parentheses

```java
public boolean isBalanced(String s) {
    Deque<Character> stack = new ArrayDeque<>();
    
    for (char c : s.toCharArray()) {
        if (c == '(' || c == '[' || c == '{') {
            stack.push(c);
        } else if (c == ')' || c == ']' || c == '}') {
            if (stack.isEmpty()) return false;
            char top = stack.pop();
            if ((c == ')' && top != '(') ||
                (c == ']' && top != '[') ||
                (c == '}' && top != '{')) {
                return false;
            }
        }
    }
    return stack.isEmpty();
}

// isBalanced("{[()]}") → true
// isBalanced("{[(])}") → false
```

---

## Queue: FIFO (First In, First Out)

### Real-World Uses

- **Task/job queues** — background job processors (emails, notifications)
- **Breadth-First Search** (BFS) in graphs
- **Printer queue** — jobs processed in submission order
- **Web server request handling** — requests queued when server is busy
- **Message queues** — Kafka, RabbitMQ model
- **Producer-consumer** patterns

### Queue Interface Methods

The Queue interface has two sets of methods — one that throws exceptions and one that returns special values:

```
Operation    Throws Exception    Returns null/false
──────────────────────────────────────────────────
Add to back  add(e)              offer(e)
Remove front remove()            poll()
Inspect front element()          peek()
```

**Prefer the null-returning versions** (`offer`, `poll`, `peek`) unless you specifically want an exception:

```java
Queue<String> queue = new ArrayDeque<>();

queue.offer("task1");  // Enqueue
queue.offer("task2");
queue.offer("task3");

queue.peek();   // "task1" — look at front without removing
queue.poll();   // "task1" — remove and return front
queue.poll();   // "task2"
queue.poll();   // "task3"
queue.poll();   // null — queue is empty (offer/poll/peek return null)
queue.remove(); // NoSuchElementException — queue is empty (add/remove/element throw)
```

### Practical Example: BFS (Level-Order Tree Traversal)

```java
void bfs(TreeNode root) {
    if (root == null) return;
    
    Queue<TreeNode> queue = new ArrayDeque<>();
    queue.offer(root);
    
    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        System.out.print(node.val + " ");
        
        if (node.left != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
}
```

---

## Deque: Double-Ended Queue

A Deque ("deck") is a `Queue` that supports adding and removing from **both ends**. It can act as both a stack and a queue.

```
DEQUE:
         ┌────────────────────────────┐
front →  │  "A"  |  "B"  |  "C"  |  │  ← back
         └────────────────────────────┘
↑ addFirst / pollFirst              addLast / pollLast ↑
```

### Deque Interface Methods

```java
Deque<String> deque = new ArrayDeque<>();

// Front operations
deque.addFirst("A");    // Add to front
deque.offerFirst("A");  // Add to front (returns false if full, unlike addFirst)
deque.removeFirst();    // Remove from front (throws if empty)
deque.pollFirst();      // Remove from front (returns null if empty)
deque.peekFirst();      // Inspect front (returns null if empty)
deque.getFirst();       // Inspect front (throws if empty)

// Back operations
deque.addLast("Z");
deque.offerLast("Z");
deque.removeLast();
deque.pollLast();
deque.peekLast();
deque.getLast();

// Stack methods (operate on front)
deque.push("X");   // Same as addFirst
deque.pop();       // Same as removeFirst

// Queue methods (add to back, remove from front)
deque.offer("X");  // Same as offerLast
deque.poll();      // Same as pollFirst
deque.peek();      // Same as peekFirst
```

### Real-World Use: Sliding Window Maximum

```java
// Find maximum in each window of size k
int[] maxInWindows(int[] nums, int k) {
    int[] result = new int[nums.length - k + 1];
    Deque<Integer> deque = new ArrayDeque<>();  // Stores indices
    
    for (int i = 0; i < nums.length; i++) {
        // Remove indices outside window
        while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
            deque.pollFirst();
        }
        // Remove indices whose values are smaller than current
        while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
            deque.pollLast();
        }
        deque.offerLast(i);
        if (i >= k - 1) {
            result[i - k + 1] = nums[deque.peekFirst()];
        }
    }
    return result;
}
```

---

## ArrayDeque vs LinkedList for Stack/Queue

Both `ArrayDeque` and `LinkedList` implement `Deque`. Why prefer `ArrayDeque`?

```
                 ArrayDeque              LinkedList
─────────────────────────────────────────────────────
Memory:          Array-backed            Node-per-element
Per-element:     ~4-8 bytes              ~32-48 bytes (node overhead)
Cache behavior:  Cache-friendly          Cache unfriendly
Random access:   No                      No (both O(n))
Null elements:   Not allowed             Allowed
Speed in practice: FASTER               Slower
When to use:     Almost always           When you need List + Deque
```

**Rule: Use `ArrayDeque` when you need a stack or queue. Use `LinkedList` only if you also need `List` operations.**

---

## PriorityQueue: Heap-Backed Ordering

`PriorityQueue<E>` processes elements by **priority** (lowest first by default with `Comparable`), not by insertion order.

Internally, it uses a **binary min-heap** — a complete binary tree where every node is smaller than or equal to its children.

```
PriorityQueue with integers [3, 1, 4, 1, 5, 9, 2, 6]:

Heap structure:
         1
       /   \
      1     2
     / \   / \
    3   4 5   9
   /
  6

Always poll() returns the minimum: 1, 1, 2, 3, 4, 5, 6, 9
```

### Performance

| Operation    | Time Complexity | Why                          |
|--------------|-----------------|------------------------------|
| `offer(e)`   | O(log n)        | Sift up in heap              |
| `poll()`     | O(log n)        | Remove root, sift down       |
| `peek()`     | O(1)            | Root element always at top   |
| `contains()` | O(n)            | No structure for this        |
| Build heap   | O(n)            | `new PriorityQueue<>(list)`  |

### Examples

```java
// Min-heap (default — smallest element processed first)
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
minHeap.offer(5);
minHeap.offer(1);
minHeap.offer(3);

minHeap.poll(); // 1 (smallest)
minHeap.poll(); // 3
minHeap.poll(); // 5

// Max-heap (largest element processed first)
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
maxHeap.offer(5);
maxHeap.offer(1);
maxHeap.offer(3);

maxHeap.poll(); // 5 (largest)

// Custom priority: process tasks by priority level
record Task(String name, int priority) {}
PriorityQueue<Task> taskQueue = new PriorityQueue<>(
    Comparator.comparingInt(Task::priority).reversed() // Higher number = higher priority
);

taskQueue.offer(new Task("Low priority report", 1));
taskQueue.offer(new Task("Critical bug fix", 10));
taskQueue.offer(new Task("Medium feature", 5));

taskQueue.poll(); // Task{name="Critical bug fix", priority=10}
taskQueue.poll(); // Task{name="Medium feature", priority=5}
taskQueue.poll(); // Task{name="Low priority report", priority=1}
```

### Real-World Use: Top-K Problem

Find the K largest elements from a large stream:

```java
// Find top 3 salaries from 1 million employees
// Use a MIN-heap of size k: if new element > heap min, swap it in

PriorityQueue<Integer> topK(int[] salaries, int k) {
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    
    for (int salary : salaries) {
        minHeap.offer(salary);
        if (minHeap.size() > k) {
            minHeap.poll();  // Remove smallest, keeping only top k
        }
    }
    return minHeap;  // Contains top k salaries
}
// Time: O(n log k) — much better than O(n log n) full sort!
```

---

## Real-World Uses Summary

```
┌─────────────────┬──────────────────────────────────────────────────┐
│ Data Structure  │ Real-World Applications                          │
├─────────────────┼──────────────────────────────────────────────────┤
│ Stack           │ Undo/redo, call stack, DFS, expression parser,   │
│ (ArrayDeque)    │ browser back, compiler parsing, backtracking     │
├─────────────────┼──────────────────────────────────────────────────┤
│ Queue           │ Task queues, BFS, printer queue, request buffer, │
│ (ArrayDeque)    │ event processing, producer-consumer              │
├─────────────────┼──────────────────────────────────────────────────┤
│ Deque           │ Sliding window algorithms, palindrome checking,  │
│ (ArrayDeque)    │ work stealing (ForkJoinPool uses deques)         │
├─────────────────┼──────────────────────────────────────────────────┤
│ PriorityQueue   │ Dijkstra's shortest path, A* search, task        │
│                 │ scheduling, top-K problems, hospital triage       │
└─────────────────┴──────────────────────────────────────────────────┘
```

---

## Key Takeaways

1. **Stack**: Use `ArrayDeque` with `push()`/`pop()`/`peek()` — NOT the `Stack` class
2. **Queue**: Use `ArrayDeque` with `offer()`/`poll()`/`peek()`
3. **Deque**: Use `ArrayDeque` for operations at both ends
4. **Priority**: Use `PriorityQueue` when order by value matters more than insertion order
5. **ArrayDeque vs LinkedList**: `ArrayDeque` is almost always faster in practice
6. **Null in ArrayDeque**: Not allowed — `offer(null)` throws `NullPointerException`
7. **PriorityQueue default**: Min-heap (smallest first); use `Comparator.reverseOrder()` for max-heap
