# Choosing the Right Data Structure

## The Decision Flowchart

```
Start: What do I need to store?
            │
            ▼
    ┌───────────────────────────────────────────────┐
    │        Do I need key-value pairs?             │
    └───────────────────────────────────────────────┘
                 │                    │
               YES                   NO
                 │                    │
                 ▼                    ▼
    ┌────────────────────┐   ┌────────────────────────────────────┐
    │ Do I need sorted   │   │     What's the primary concern?    │
    │    keys?           │   └────────────────────────────────────┘
    └────────────────────┘           │           │          │
           │         │               │           │          │
          YES        NO      Uniqueness     Ordering    LIFO/FIFO
           │         │               │           │          │
           ▼         ▼               ▼           ▼          ▼
       TreeMap    HashMap         HashSet    ArrayList   ArrayDeque
                     │                         │
                     ▼                          ▼
             Need insertion          Need sorted elements?
              order kept?             │          │
               │        │           YES          NO
              YES        NO          │            │
               ▼         ▼        TreeSet      ArrayList
         LinkedHashMap  HashMap    (also: sort
                                   the list)
```

## Detailed Decision Guide

### 1. Do I Need Key-Value Pairs?

**YES → Use a Map**

| Condition                          | Use              | Example Use Case                     |
|------------------------------------|------------------|--------------------------------------|
| Fast lookup, no order needed       | `HashMap`        | User session store, cache            |
| Preserve insertion order           | `LinkedHashMap`  | LRU cache, ordered config            |
| Keys need to be in sorted order    | `TreeMap`        | Dictionary, leaderboard, phone book  |
| Sorted + range queries             | `TreeMap`        | `subMap("a", "f")` — all keys a–f   |

### 2. Do I Need Only Keys (No Values)?

**Use a Set**

| Condition                          | Use              | Example Use Case                     |
|------------------------------------|------------------|--------------------------------------|
| Just unique membership             | `HashSet`        | Visited URLs, unique tags            |
| Maintain insertion order           | `LinkedHashSet`  | Ordered de-duplication               |
| Sorted unique elements             | `TreeSet`        | Sorted unique words, range queries   |

### 3. Do I Need an Ordered Sequence?

**Use a List or Deque**

| Condition                          | Use              | Example Use Case                     |
|------------------------------------|------------------|--------------------------------------|
| General list, random access        | `ArrayList`      | Shopping cart, table rows            |
| Frequent add/remove at ends only   | `ArrayDeque`     | Sliding window, history buffer       |
| LIFO (undo, backtrack)             | `ArrayDeque`     | Undo stack, DFS, call stack          |
| FIFO (task queue, BFS)             | `ArrayDeque`     | Job queue, BFS graph traversal       |
| Priority-based processing          | `PriorityQueue`  | Task scheduler, Dijkstra, A*         |

---

## Real Examples from Well-Known Systems

### How Google Might Store Search Autocomplete Suggestions

```
Problem: Given a typed prefix, find all words that start with it
         and return them sorted by frequency.

Data structures used:
  - Trie (specialized prefix tree) for prefix matching
  - HashMap<String, Integer> for word → frequency count
  - TreeMap<Integer, List<String>> for frequency → words (sorted by freq)
  - PriorityQueue for top-K most frequent

Why NOT ArrayList for the lookup?
  - ArrayList.contains("google") → O(n), terrible at scale
  - HashMap lookup is O(1)
```

### How Twitter Might Store Your Timeline

```
Problem: Show the 100 most recent tweets from people you follow

Option A: Store all tweets in one list, filter on read
  - List<Tweet> allTweets (billions of tweets)
  - Filter by followedUsers.contains(tweet.authorId)
  - Problem: O(billions) on every read ❌

Option B: Pre-compute timelines
  - Map<UserId, Deque<Tweet>> userTimelines
  - Each Deque holds most recent tweets
  - When someone tweets: push to all followers' deques (write-heavy)
  - On read: just return your deque (O(1))
  - Used by Twitter for most users ✓
```

### How Your IDE Stores an Undo History

```
Problem: Support undo/redo for code edits

Undo stack: Deque<Edit> undoStack = new ArrayDeque<>();
  - Each edit is pushed onto undoStack
  - Undo pops from undoStack, pushes onto redoStack

Redo stack: Deque<Edit> redoStack = new ArrayDeque<>();
  - Each redo pops from redoStack, pushes back to undoStack
  - New edit clears redoStack

Why Deque (as stack) and not ArrayList?
  - Only need push/pop from one end: O(1) vs O(n) for ArrayList remove(0)
```

### How a Web Server Handles Requests

```
Problem: Handle incoming HTTP requests with a thread pool

Request queue: BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<>();
  - New request → offer() to queue
  - Worker thread → take() from queue (blocks if empty)

Why Queue and not List?
  - FIFO fairness: first request served first
  - BlockingQueue handles thread coordination automatically
  - ArrayBlockingQueue if you want bounded (prevent OOM from request flood)
```

### How Java's HashMap Is Used in Language Runtimes

```
Problem: Map class names to their bytecode at runtime

ClassLoader:
  Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();
  // ConcurrentHashMap for thread-safety (multiple threads loading classes)
  // O(1) lookup when calling new MyClass() after first load
```

---

## Anti-Patterns: Wrong Structure for the Job

### Anti-Pattern 1: Searching a List for Membership

```java
// WRONG: O(n) lookup every iteration → O(n²) total
List<String> blockedUsers = new ArrayList<>(loadBlockedUsers());
for (Message msg : messages) {
    if (blockedUsers.contains(msg.senderId())) {  // O(n) every time!
        continue;
    }
    display(msg);
}

// RIGHT: O(1) lookup → O(n) total
Set<String> blockedUsers = new HashSet<>(loadBlockedUsers());
for (Message msg : messages) {
    if (blockedUsers.contains(msg.senderId())) {  // O(1)!
        continue;
    }
    display(msg);
}
```

### Anti-Pattern 2: Sorting When You Need Sorted Insertion

```java
// WRONG: Re-sorting after each insert → O(n² log n)
List<Task> tasks = new ArrayList<>();
tasks.add(new Task("Urgent", 10));
Collections.sort(tasks, Comparator.comparingInt(Task::priority));
tasks.add(new Task("Low", 1));
Collections.sort(tasks, Comparator.comparingInt(Task::priority));
// ... sort after every add!

// RIGHT: PriorityQueue stays sorted automatically
PriorityQueue<Task> tasks = new PriorityQueue<>(
    Comparator.comparingInt(Task::priority).reversed()
);
tasks.offer(new Task("Urgent", 10));  // O(log n)
tasks.offer(new Task("Low", 1));      // O(log n)
Task next = tasks.poll();             // O(log n) — always highest priority
```

### Anti-Pattern 3: HashMap with Mutable Key

```java
// WRONG: Mutating a key after putting it in the map
List<String> key = new ArrayList<>(List.of("a", "b"));
Map<List<String>, String> map = new HashMap<>();
map.put(key, "value");

key.add("c");  // MUTATES the key! hashCode changes!
map.get(key);  // Returns null — can't find it anymore ❌

// RIGHT: Use immutable keys
List<String> key = List.of("a", "b");  // Immutable
map.put(key, "value");
// key cannot be changed, so hashCode is stable ✓
```

### Anti-Pattern 4: Stack Class Instead of Deque

```java
// WRONG: Stack<E> extends Vector — carries legacy baggage
Stack<String> stack = new Stack<>();
stack.push("item");
stack.elementAt(0);  // ??? This is meaningless for a stack concept
stack.insertElementAt("sneaky", 0);  // Breaks stack abstraction!

// RIGHT: Use Deque as stack
Deque<String> stack = new ArrayDeque<>();
stack.push("item");
stack.pop();
// Only stack operations available through Deque interface
```

---

## Quick Reference Card

```
┌──────────────────────────────────────────────────────────────────┐
│                  JAVA DATA STRUCTURE CHEAT SHEET                 │
├────────────────────┬────────────────────────────────────────────┤
│ Need               │ Use                                        │
├────────────────────┼────────────────────────────────────────────┤
│ Fast key lookup    │ HashMap          (O(1) avg)                │
│ Ordered keys       │ TreeMap          (O(log n), sorted)        │
│ Ordered insertion  │ LinkedHashMap    (O(1), insertion order)   │
│ Unique elements    │ HashSet          (O(1) avg)                │
│ Sorted unique      │ TreeSet          (O(log n))                │
│ Ordered unique     │ LinkedHashSet    (insertion order)         │
│ General list       │ ArrayList        (O(1) access)             │
│ Frequent end ops   │ ArrayDeque       (O(1) both ends)          │
│ Stack (LIFO)       │ ArrayDeque       (push/pop)                │
│ Queue (FIFO)       │ ArrayDeque       (offer/poll)              │
│ Priority order     │ PriorityQueue    (O(log n) push/pop)       │
│ Thread-safe map    │ ConcurrentHashMap                          │
│ Thread-safe queue  │ LinkedBlockingQueue                        │
│ Immutable list     │ List.of(...)     (Java 9+)                 │
│ Immutable map      │ Map.of(...)      (Java 9+)                 │
└────────────────────┴────────────────────────────────────────────┘
```

---

## The 80/20 Rule for Java Collections

In practice, 80% of your code uses only these:

1. **`ArrayList`** — your default list
2. **`HashMap`** — your default map
3. **`HashSet`** — when you need uniqueness
4. **`ArrayDeque`** — when you need a stack or queue

The other structures (TreeMap, PriorityQueue, etc.) are used in specific scenarios. When you reach for those, you'll know why you need them.
