# Chapter 3: Concurrency Basics

## What Is a Thread?

Modern computers can do multiple things at once. Your phone plays music while checking email while syncing your photos. How?

### Processes vs Threads

A **process** is a running program with its own memory space. When you open Chrome and open Spotify, those are two separate processes. They can't directly access each other's memory.

A **thread** is a lightweight execution unit within a process. Multiple threads share the same memory space. A single Java program can run many threads simultaneously.

```
PROCESS: JVM (your Java program)
├── Thread 1: Main thread (running your main())
├── Thread 2: Background tasks
├── Thread 3: UI event handling
├── Thread 4: Garbage collector
└── Thread 5: Network I/O
```

### Why Use Threads?

1. **Performance** — Do multiple things at once (download files while rendering UI)
2. **Responsiveness** — Keep UI responsive while doing background work
3. **Server scalability** — Handle many clients simultaneously

---

## Creating Threads

### Method 1: Extending Thread

```java
public class MyThread extends Thread {
    private final String name;
    
    public MyThread(String name) {
        this.name = name;
    }
    
    @Override
    public void run() {
        // This code runs in a new thread
        for (int i = 0; i < 5; i++) {
            System.out.println(name + ": " + i);
        }
    }
}

MyThread t = new MyThread("Worker");
t.start();  // DON'T call run() directly! That would run in the current thread.
```

### Method 2: Implementing Runnable (preferred)

```java
public class MyTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Running in thread: " + Thread.currentThread().getName());
    }
}

Thread t = new Thread(new MyTask());
t.start();

// Or with lambda (most concise)
Thread t = new Thread(() -> System.out.println("Hello from thread!"));
t.start();
```

**Why Runnable over Thread?**
- Java only has single inheritance — extending Thread wastes your one inheritance slot
- Runnable is more flexible — it can be executed by Thread, ExecutorService, etc.
- Separates the task from the execution mechanism

### Method 3: Callable + Future

```java
// Callable is like Runnable but can return a value and throw checked exceptions
Callable<Integer> task = () -> {
    // simulate computation
    Thread.sleep(1000);
    return 42;
};

// Run with ExecutorService (covered below)
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<Integer> future = executor.submit(task);

// Get the result (blocks until done)
Integer result = future.get();  // 42
```

---

## Thread Lifecycle

```
                        start()
  ┌─────────┐          ┌──────────┐          ┌─────────┐
  │  NEW    │ ────────>│ RUNNABLE │ ────────>│  DEAD   │
  └─────────┘          └──────────┘          └─────────┘
                          │    ▲
                          │    │
                    block │    │ unblock
                          ▼    │
                       ┌──────────┐
                       │ BLOCKED/ │
                       │ WAITING  │
                       └──────────┘
```

- **NEW** — Thread created but `start()` not called
- **RUNNABLE** — Running or ready to run (JVM decides when it runs)
- **BLOCKED** — Waiting for a monitor lock (synchronized)
- **WAITING** — Waiting indefinitely (wait(), join())
- **TIMED_WAITING** — Waiting with a timeout (sleep(), wait(timeout))
- **TERMINATED** — Finished running

---

## The Race Condition: Java's Most Famous Bug

Here's the problem with threads sharing data:

```java
public class BankAccount {
    private int balance = 1000;
    
    public void deposit(int amount) {
        balance = balance + amount;  // This looks atomic, but IT'S NOT!
    }
    
    public int getBalance() { return balance; }
}
```

The line `balance = balance + amount` actually compiles to THREE operations:
1. Read `balance` from memory → register
2. Add `amount` to register
3. Write register back to `balance`

**What can go wrong with two threads:**

```
Thread A: reads balance = 1000
Thread B: reads balance = 1000    ← B reads BEFORE A writes back!
Thread A: adds 100, writes 1100
Thread B: adds 100, writes 1100   ← B overwrites A's update!

Final balance: 1100 (one deposit was LOST!)
Expected: 1200
```

This is a **race condition** — the result depends on which thread runs first. It's non-deterministic, which makes it extremely hard to reproduce and debug.

```java
// Demonstrating the bug
BankAccount account = new BankAccount();

Thread t1 = new Thread(() -> {
    for (int i = 0; i < 10000; i++) account.deposit(1);
});
Thread t2 = new Thread(() -> {
    for (int i = 0; i < 10000; i++) account.deposit(1);
});

t1.start();
t2.start();
t1.join();  // wait for t1 to finish
t2.join();  // wait for t2 to finish

// Expected: 21000 (1000 + 10000 + 10000)
// Actual: might be 20847, 20931, 21000 — varies every run!
System.out.println(account.getBalance());
```

---

## The `synchronized` Keyword

`synchronized` ensures only ONE thread executes a block of code at a time. It uses a **monitor lock** (every Java object has one).

### Synchronized Method

```java
public class BankAccount {
    private int balance = 1000;
    
    // synchronized — only one thread can execute this at a time
    public synchronized void deposit(int amount) {
        balance = balance + amount;  // now truly atomic with the lock
    }
    
    public synchronized int getBalance() {
        return balance;
    }
}
```

This acquires the lock on `this` (the BankAccount object). While Thread A holds the lock, Thread B waits. When A releases the lock (when the method returns), B can proceed.

### Synchronized Block (finer granularity)

```java
public class BankAccount {
    private int balance = 1000;
    private final Object lock = new Object();  // explicit lock object
    
    public void deposit(int amount) {
        // Only synchronize the critical section, not the whole method
        synchronized(lock) {
            balance = balance + amount;
        }
        // Other code here runs without the lock
        auditLog(amount);  // not synchronized — faster
    }
}
```

### Static Synchronized Methods

```java
public class Counter {
    private static int count = 0;
    
    // Locks on Counter.class (the Class object), not an instance
    public static synchronized void increment() {
        count++;
    }
}
```

### The Happens-Before Guarantee

`synchronized` gives you more than mutual exclusion — it also guarantees **memory visibility**. When Thread A releases a lock and Thread B acquires the same lock, everything Thread A wrote is visible to Thread B. This matters because threads have local CPU caches.

---

## The `volatile` Keyword

`volatile` is lighter than `synchronized`. It guarantees **visibility** (changes are immediately visible to all threads) but NOT atomicity.

```java
public class StatusChecker {
    private volatile boolean running = true;  // volatile!
    
    public void stop() {
        running = false;  // this write is immediately visible to all threads
    }
    
    public void work() {
        while (running) {  // this read always gets the latest value
            doSomeWork();
        }
    }
}
```

**When is volatile enough?**
- A single flag (boolean) that one thread writes and another reads
- Simple reads/writes that are intrinsically atomic (not compound like `x++`)

**When do you need synchronized?**
- Compound operations: `x++`, `balance += amount`
- Check-then-act: `if (x == null) { x = new X(); }`

---

## java.util.concurrent: The Modern Way

Raw threads are error-prone. The `java.util.concurrent` package provides high-level abstractions.

### ExecutorService — Thread Pool Management

Creating a new thread for every task is expensive. Thread pools reuse threads:

```java
// Fixed thread pool — 4 threads, queues extra work
ExecutorService executor = Executors.newFixedThreadPool(4);

// Submit tasks
executor.submit(() -> System.out.println("Task 1"));
executor.submit(() -> System.out.println("Task 2"));

// ALWAYS shut down when done!
executor.shutdown();        // waits for current tasks to finish
executor.awaitTermination(30, TimeUnit.SECONDS);  // wait up to 30s

// Types of executors:
Executors.newFixedThreadPool(n);        // n threads, queue overflow
Executors.newCachedThreadPool();        // grows as needed, shrinks when idle
Executors.newSingleThreadExecutor();    // single thread, tasks run sequentially
Executors.newScheduledThreadPool(n);    // run tasks at a delay or repeatedly
```

### Future<T> — Async Results

```java
ExecutorService executor = Executors.newFixedThreadPool(2);

Future<Integer> future = executor.submit(() -> {
    Thread.sleep(2000);  // simulate long computation
    return 42;
});

// Do other work while waiting...
System.out.println("Working on other things...");

// Block until result is ready
Integer result = future.get();  // waits up to... forever
Integer result2 = future.get(5, TimeUnit.SECONDS);  // waits max 5s

// Check without blocking
if (future.isDone()) {
    System.out.println(future.get());
}

// Cancel
future.cancel(true);  // attempt to cancel, true = interrupt if running
```

### CompletableFuture — The Power Tool

`CompletableFuture` is `Future` on steroids — it supports chaining, combining, and error handling:

```java
// Basic async computation
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    Thread.sleep(1000);
    return "Hello from async!";
});

// Chain with thenApply (transforms result, like map)
CompletableFuture<Integer> lengthFuture = future
    .thenApply(String::length);

// Chain with thenAccept (consume result, like forEach)
future.thenAccept(System.out::println);

// Chain with thenCompose (flatMap — chain async operations)
CompletableFuture<UserDetails> userDetails = 
    getUserId("alice")                      // async
    .thenCompose(id -> getUserDetails(id)); // async

// Combine two futures
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> "World");

CompletableFuture<String> combined = f1.thenCombine(f2, 
    (s1, s2) -> s1 + " " + s2);  // "Hello World"

// Wait for ALL to complete
CompletableFuture<Void> allDone = CompletableFuture.allOf(f1, f2);
allDone.join();  // blocks until all are done

// Wait for ANY to complete
CompletableFuture<Object> anyDone = CompletableFuture.anyOf(f1, f2);

// Error handling
CompletableFuture<Integer> result = CompletableFuture
    .supplyAsync(() -> riskyOperation())
    .exceptionally(ex -> {
        System.out.println("Error: " + ex.getMessage());
        return -1;  // default value on error
    });

// Full pipeline example
CompletableFuture<String> report = CompletableFuture
    .supplyAsync(() -> fetchUserFromDatabase(userId))       // async DB call
    .thenApply(user -> enrichWithPreferences(user))         // transform
    .thenCompose(user -> fetchOrderHistory(user.getId()))   // another async call
    .thenApply(orders -> formatReport(orders))              // transform
    .exceptionally(ex -> "Error generating report: " + ex.getMessage());
```

---

## Deadlock

A **deadlock** occurs when two or more threads wait for each other forever.

```java
Object lockA = new Object();
Object lockB = new Object();

Thread thread1 = new Thread(() -> {
    synchronized(lockA) {
        System.out.println("Thread 1: acquired lockA, waiting for lockB");
        Thread.sleep(100);  // give other thread time to get lockB
        synchronized(lockB) {
            System.out.println("Thread 1: acquired both locks!");
        }
    }
});

Thread thread2 = new Thread(() -> {
    synchronized(lockB) {           // Thread 2 acquires lockB first!
        System.out.println("Thread 2: acquired lockB, waiting for lockA");
        Thread.sleep(100);
        synchronized(lockA) {       // Waits for lockA — but Thread 1 has it!
            System.out.println("Thread 2: acquired both locks!");
        }
    }
});
```

Thread 1 holds lockA, wants lockB.
Thread 2 holds lockB, wants lockA.
Neither can proceed. **Deadlock.**

**How to prevent deadlocks:**
1. **Always acquire locks in the same order** — if everyone acquires A before B, no deadlock
2. **Use `tryLock()` with timeout** — if you can't get the lock, release what you have and retry
3. **Use higher-level concurrency utilities** — avoid raw locks when possible

---

## Thread-Safe Collections

Regular collections (`HashMap`, `ArrayList`) are NOT thread-safe.

```java
// ConcurrentHashMap — thread-safe HashMap (much better than synchronizedMap)
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("key", 1);
map.computeIfAbsent("other", k -> 42);

// CopyOnWriteArrayList — thread-safe list for frequent reads, rare writes
// Every write creates a new copy of the array
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
list.add("Hello");

// BlockingQueue — producer-consumer pattern
BlockingQueue<Task> queue = new LinkedBlockingQueue<>(100);
queue.put(task);    // blocks if full
Task t = queue.take();  // blocks if empty

// AtomicInteger, AtomicLong — thread-safe counters without synchronized
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();          // atomic increment
counter.compareAndSet(5, 10);       // atomic compare-and-swap

// AtomicReference — thread-safe object reference updates
AtomicReference<String> ref = new AtomicReference<>("initial");
ref.set("updated");
String old = ref.getAndSet("newer");  // returns old value, sets new
```

---

## Summary: Concurrency Rules of Thumb

1. **Prefer immutability** — immutable objects are inherently thread-safe
2. **Prefer higher-level utilities** — `ExecutorService`, `CompletableFuture` over raw `Thread`
3. **Minimize shared mutable state** — the less shared, the fewer races
4. **Synchronize consistently** — always use the same lock for the same data
5. **Don't lock too broadly** — synchronized on whole methods slows performance
6. **Don't lock too narrowly** — missing synchronization causes races
7. **Use `volatile` for flags, `synchronized`/`AtomicXxx` for compound operations**

Concurrency is hard. Even experienced developers introduce concurrency bugs. The best protection is writing code that minimizes shared state and uses proven patterns.

```
                    DANGER LEVEL
Raw Thread             Medium  ────────> Hard to get right
synchronized           High    ────────> Easy to deadlock
volatile               Medium  ────────> Only for simple cases
ExecutorService        Low     ────────> Good abstraction
CompletableFuture      Low     ────────> Clean async code
Immutable objects      Zero    ────────> No synchronization needed!
```
