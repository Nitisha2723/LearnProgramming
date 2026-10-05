# Big O Complexity

## What Is Big O Notation?

Big O notation describes how an algorithm's performance **scales** with input size. It answers the question: "If I double the input size, how does the runtime change?"

It's a **worst-case upper bound** — we're being pessimistic to ensure our system can handle any input.

**The key insight:** We drop constants and lower-order terms because at large scale, they don't matter.

```
f(n) = 3n² + 5n + 100

At n = 10:    3(100) + 5(10) + 100 = 450
At n = 1000:  3(1000000) + 5(1000) + 100 = 3,005,100

The 3n² term dominates. We write: O(n²)
The 3 and the 5n + 100 are irrelevant at scale.
```

---

## Why It Matters: The Real Difference at Scale

Let's say you're building a social network:

```
Algorithm A: O(n)    — checks each user once
Algorithm B: O(n²)  — compares each user with every other user

                     1,000 users        1,000,000 users
───────────────────────────────────────────────────────
O(n)    Algorithm A: 1,000 ops         1,000,000 ops (1 second)
O(n²)   Algorithm B: 1,000,000 ops    1,000,000,000,000 ops (11.5 DAYS)
```

**This is why algorithm choice matters more than hardware.** No amount of CPU upgrade saves you from a fundamentally bad algorithm.

---

## O(1) — Constant Time

The operation takes the **same amount of time regardless of input size**.

```
Input size: 1     10    100    1000    1,000,000
Operations: 1      1      1       1            1
```

### Examples

```java
// Array/ArrayList index access
int first = array[0];            // O(1) — direct memory offset
int fifth = list.get(4);         // O(1) — same

// HashMap get/put
map.put("Alice", 30);            // O(1) average
map.get("Alice");                // O(1) average

// Stack push/pop with ArrayDeque
stack.push("item");              // O(1)
stack.pop();                     // O(1)

// Check if collection is empty
list.isEmpty();                  // O(1) — just checks size field
list.size();                     // O(1) — size is tracked as a field

// Math operations
int sum = a + b;                 // O(1)
```

**Motto: "I can go straight to what I need."**

---

## O(log n) — Logarithmic Time

The input is repeatedly **halved** with each step. Doubling the input size adds only **one more step**.

```
Input size: 1    2    4    8    16    32    64    128    1,000    1,000,000
Operations: 0    1    2    3     4     5     6      7      10           20
```

### Examples

```java
// Binary search — the classic O(log n) algorithm
int binarySearch(int[] arr, int target) {
    int left = 0, right = arr.length - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (arr[mid] == target) return mid;
        if (arr[mid] < target) left = mid + 1;  // Eliminate left half
        else right = mid - 1;                   // Eliminate right half
    }
    return -1;
}
// Each iteration eliminates HALF the remaining elements
// 1,000,000 elements → at most 20 iterations!

// TreeMap/TreeSet operations
treeMap.put("key", value);    // O(log n) — balanced BST insert
treeSet.contains("item");     // O(log n) — BST traversal

// PriorityQueue push/pop
heap.offer(5);                // O(log n) — sift up
heap.poll();                  // O(log n) — sift down
```

**Motto: "Each step eliminates half the remaining possibilities."**

---

## O(n) — Linear Time

You need to **look at each element once**. Performance scales directly with input size.

```
Input size: 1    10    100    1,000    1,000,000
Operations: 1    10    100    1,000    1,000,000
```

### Examples

```java
// Linear search — must check each element
boolean contains = false;
for (String name : list) {
    if (name.equals("Alice")) { contains = true; break; }
}
// Best case O(1), worst case O(n) — we say O(n)

// ArrayList.contains() — delegates to a loop
list.contains("Alice");  // O(n)

// Summing all elements
int sum = 0;
for (int x : numbers) sum += x;  // O(n)

// Single-pass array operations
int max = arr[0];
for (int x : arr) max = Math.max(max, x);  // O(n)

// Building a frequency map from a list
for (String word : words) {
    freq.merge(word, 1, Integer::sum);  // O(n) total (each iteration O(1))
}

// Copying a collection
new ArrayList<>(existingList);  // O(n) — must copy each element
```

**Motto: "I have to check everything exactly once."**

---

## O(n log n) — Linearithmic Time

This is the complexity of efficient sorting algorithms. You can't sort faster than O(n log n) with a comparison-based sort.

```
Input size: 100       1,000       10,000       1,000,000
Operations: 700       10,000      140,000      20,000,000
```

### Examples

```java
// Java's Arrays.sort() and Collections.sort() — TimSort
Collections.sort(list);     // O(n log n)
Arrays.sort(arr);           // O(n log n) for objects (TimSort)
                            // O(n log n) for primitives (dual-pivot quicksort)

// Merge sort — classic O(n log n)
// Quick sort — O(n log n) average, O(n²) worst

// TreeSet/TreeMap building from a collection
new TreeSet<>(existingCollection);  // O(n log n) — n inserts at O(log n) each

// Stream sorted()
list.stream().sorted().collect(toList());  // O(n log n)
```

**Motto: "Sort the input, then process it."**

---

## O(n²) — Quadratic Time

**Nested loops** where each loop iterates over the input. As the input doubles, the work **quadruples**.

```
Input size: 10        100         1,000        10,000
Operations: 100     10,000     1,000,000    100,000,000
```

### Examples

```java
// Nested loop — the most common O(n²) pattern
// Find all duplicate pairs
for (int i = 0; i < n; i++) {
    for (int j = i + 1; j < n; j++) {
        if (arr[i] == arr[j]) {
            duplicates.add(arr[i]);
        }
    }
}

// Bubble sort — classic O(n²)
for (int i = 0; i < n - 1; i++) {
    for (int j = 0; j < n - i - 1; j++) {
        if (arr[j] > arr[j + 1]) {
            // swap
        }
    }
}

// Insertion sort — O(n²) worst case, O(n) best case (nearly sorted)
// Selection sort — always O(n²)

// ANTI-PATTERN: calling an O(n) method inside a loop
for (String name : names) {
    if (otherList.contains(name)) {  // contains() is O(n) — total O(n²)!
        // Fix: convert otherList to HashSet first
    }
}
```

**Warning sign: a loop inside a loop over the same data.**

**Fix pattern:**
```java
// O(n²) — bad
for (String name : names) {
    if (otherList.contains(name)) { ... }  // O(n) inside O(n) loop
}

// O(n) — good
Set<String> otherSet = new HashSet<>(otherList);  // O(n) once
for (String name : names) {
    if (otherSet.contains(name)) { ... }  // O(1) now!
}
```

**Motto: "For each element, I check all other elements."**

---

## O(2^n) — Exponential Time

Each new element **doubles** the work. This becomes impossible at even small inputs.

```
Input size: 10         20          30          40
Operations: 1,024   1,048,576   1,073,741,824   1,099,511,627,776
```

### Examples

```java
// Naive Fibonacci — exponential because of recomputation
long fib(int n) {
    if (n <= 1) return n;
    return fib(n - 1) + fib(n - 2);  // Each call branches into 2 more
}
// fib(40) makes 2^40 ≈ 1 trillion calls!
// Fix: memoization (O(n)) or dynamic programming

// Generating all subsets of a set
// A set of n elements has 2^n subsets

// Many brute-force combinatorial problems
// Traveling Salesman Problem (brute force)
// Cryptographic key space exploration (intentional!)
```

**Rule: If you see O(2^n) in your code, you need a better algorithm (memoization, DP, greedy).**

---

## Space Complexity

Time complexity measures **how long** code runs. Space complexity measures **how much memory** it uses.

```java
// O(1) space — no extra memory proportional to input
int sum = 0;
for (int x : arr) sum += x;  // Just one variable

// O(n) space — extra memory grows with input
Map<String, Integer> freq = new HashMap<>();
for (String word : words) freq.merge(word, 1, Integer::sum);
// At most n entries in the map

// O(n) space — recursive call stack
int sum(int[] arr, int i) {
    if (i == arr.length) return 0;
    return arr[i] + sum(arr, i + 1);
}
// n frames on the call stack simultaneously

// O(n²) space — 2D grid/matrix
int[][] dp = new int[n][n];  // n*n cells
```

**Trade-off:** You often trade space for time. The frequency counter above uses O(n) space to achieve O(1) lookup instead of O(n) repeated scanning.

---

## Big O Cheat Sheet for Java Collections

```
┌────────────────────┬─────────┬──────────┬─────────┬───────────┐
│ Data Structure     │ Access  │ Search   │ Insert  │ Delete    │
├────────────────────┼─────────┼──────────┼─────────┼───────────┤
│ Array              │  O(1)   │   O(n)   │  O(n)   │   O(n)    │
│ ArrayList          │  O(1)   │   O(n)   │  O(n)*  │   O(n)*   │
│ LinkedList         │  O(n)   │   O(n)   │  O(1)†  │   O(1)†   │
│ HashMap            │   —     │  O(1)‡   │  O(1)‡  │   O(1)‡   │
│ TreeMap            │   —     │ O(log n) │ O(log n)│  O(log n) │
│ HashSet            │   —     │  O(1)‡   │  O(1)‡  │   O(1)‡   │
│ TreeSet            │   —     │ O(log n) │ O(log n)│  O(log n) │
│ ArrayDeque (Stack) │   —     │   O(n)   │  O(1)   │   O(1)    │
│ ArrayDeque (Queue) │   —     │   O(n)   │  O(1)   │   O(1)    │
│ PriorityQueue      │ O(1)§   │   O(n)   │ O(log n)│  O(log n) │
└────────────────────┴─────────┴──────────┴─────────┴───────────┘

* O(1) amortized at end; O(n) at arbitrary index (shifting)
† O(1) if you have an iterator at position; O(n) to find position
‡ O(1) average; O(log n) worst case (Java 8+ treeification)
§ O(1) peek at min/max; random access is O(n)
```

---

## Common Complexity Classes (Visual)

```
Operations
    |
10⁹ |                                              O(2^n)
    |                                         ●
10⁶ |                                    ●
    |                              ●
10³ |                      ●  ●●●● O(n²)
    |              ●●●●●● O(n log n)
10² |        ●●●●● O(n)
    |   ●●●● O(log n)
 10 | ●●●●●● O(1)
    └──────────────────────────────────── n
       1    10   100  1000  10,000  100,000
```

---

## Interview Strategy: Walking Through Big O Analysis

When asked about complexity in an interview, use this approach:

**1. Identify the "work unit"** — what is one basic operation? (comparison, hash lookup, etc.)

**2. Count the loops:**
- One loop over n elements → O(n)
- Nested loops → O(n²) unless the inner loop doesn't depend on n
- Halving per step → O(log n)

**3. Look for hidden complexity:**
```java
for (String name : names) {              // O(n) outer loop
    if (otherList.contains(name)) { }   // O(n) inner — total O(n²)!
    // contains() is a loop!
}
```

**4. State your assumptions:** "Assuming n is the number of elements and hash operations are O(1) average..."

**5. Discuss trade-offs:** "I can improve this from O(n²) to O(n) by converting the list to a HashSet."

### Example Analysis: Frequency Counter

```java
// Analyze: find all words that appear more than once
Map<String, Integer> freq = new HashMap<>();   // O(1) — creating empty map
for (String word : words) {                   // O(n) — iterates over all words
    freq.merge(word, 1, Integer::sum);        // O(1) — HashMap operation
}
// Outer loop: n iterations
// Inner operation: O(1)
// Total: O(n) ← correct!

// Space complexity: O(n) in worst case (all unique words)
```

---

## Key Takeaways

1. **O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(2^n)** — memorize this order
2. **HashMap gives O(1)** for get/put — use it to turn O(n) searches into O(1)
3. **Nested loops over the same data** → suspect O(n²) — is there a better way?
4. **Sorting** → O(n log n) is the best you can do with comparisons
5. **O(log n) usually means binary search or a tree** — input is halved each step
6. **Space-time tradeoff** — using more memory often buys you better time complexity
7. **Amortized analysis** — ArrayList's add is O(1) amortized, O(n) worst case
