# Module 04: Data Structures

Master the Java Collections Framework and understand when to use each data structure.

## Learning Objectives

By the end of this module, you will:
- Understand the Java Collections Framework hierarchy
- Know when to use ArrayList vs LinkedList, HashMap vs TreeMap, HashSet vs TreeSet
- Analyze algorithm complexity using Big O notation
- Implement common patterns: frequency counter, grouping, LRU cache, top-K
- Choose the right data structure for real problems

## Contents

### Theory
1. [Collections Overview](theory/01-collections-overview.md) — JCF hierarchy, Iterable, Iterator
2. [Lists](theory/02-lists-arraylist-linkedlist.md) — ArrayList vs LinkedList, all operations
3. [Maps and Sets](theory/03-maps-and-sets.md) — HashMap internals, equals/hashCode, patterns
4. [Stack Queue Deque](theory/04-stack-queue-deque.md) — LIFO, FIFO, PriorityQueue
5. [Big O Complexity](theory/05-big-o-complexity.md) — Time/space analysis, cheat sheet
6. [Choosing the Right Structure](theory/06-choosing-the-right-structure.md) — Decision flowchart

### Code Examples
- [ListsDemo.java](code/ListsDemo.java)
- [MapsDemo.java](code/MapsDemo.java)
- [SetsDemo.java](code/SetsDemo.java)
- [StackQueueDemo.java](code/StackQueueDemo.java)

### Exercises
- [Exercise 01](exercises/Exercise01_FrequencyCounter.java): Word frequency counter
- [Exercise 02](exercises/Exercise02_StudentRegistry.java): HashMap-based student CRUD
- [Exercise 03](exercises/Exercise03_BrowserHistory.java): Stack-based browser history

### Tests
- [DataStructuresTest.java](tests/DataStructuresTest.java): 15+ JUnit 5 tests

### Mini-Project
- [InventorySystem.java](mini-project/InventorySystem.java): Multi-structure inventory manager

## The Golden Rule

**Use the simplest structure that fits your requirements:**
- Default to `ArrayList` and `HashMap`
- Switch to `TreeMap`/`TreeSet` only when you need sorted keys
- Use `ArrayDeque` when you need a stack or queue
- Use `PriorityQueue` when you need priority-based processing
