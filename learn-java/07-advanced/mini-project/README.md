# Mini-Project: Concurrent Task Manager

A production-quality task execution engine demonstrating core Java concurrency concepts.

---

## What It Builds

A thread-safe `TaskManager` that:

- Accepts tasks with different priorities (LOW, NORMAL, HIGH, CRITICAL)
- Executes tasks concurrently on a configurable thread pool
- Returns `CompletableFuture<TaskResult>` for each task
- Tracks task status (PENDING → RUNNING → COMPLETED / FAILED)
- Reports completion metrics

---

## Files

| File | Purpose |
|------|---------|
| `Task.java` | Task data model with `AtomicReference<TaskStatus>` |
| `TaskManager.java` | Core executor using `ExecutorService` + `CompletableFuture` |
| `TaskManagerDemo.java` | 4-scenario demo showing all features |

---

## Concurrency Concepts Demonstrated

| Concept | Where |
|---------|-------|
| `ExecutorService` (thread pool) | `TaskManager` constructor, `executor.submit()` |
| `CompletableFuture.supplyAsync()` | `TaskManager.submit()` — async task execution |
| `CompletableFuture.allOf()` | `TaskManager.awaitAll()` — wait for all futures |
| `ConcurrentHashMap` | `TaskManager.tasks` and `futures` — thread-safe storage |
| `AtomicLong` | `completedCount`, `failedCount` — lock-free counters |
| `AtomicReference<Status>` | `Task.status` — thread-safe status updates |
| `volatile` | `Task.startedAt`, `completedAt` — visibility guarantee |
| Future chaining | `thenApply()`, `thenAccept()` in Scenario 4 |

---

## How to Compile and Run

```bash
cd mini-project/concurrent-task-manager/

# Compile
javac Task.java TaskManager.java TaskManagerDemo.java

# Run
java TaskManagerDemo
```

---

## What to Observe in the Output

**Thread names** — notice tasks run on `pool-1-thread-1` through `pool-1-thread-4`. The thread pool manages 4 workers for all tasks.

**Parallel speedup** — Scenario 2 shows 8 tasks of 200ms each completing in ~400ms (two batches of 4), not 1600ms. This is the core benefit of concurrent execution.

**Error isolation** — Scenario 3 shows that failed tasks don't crash the system. Each task's exception is caught and wrapped in a `TaskResult` with `success=false`.

**Future chaining** — Scenario 4 shows how `thenApply()` and `thenAccept()` chain operations after task completion without blocking.

---

## Extensions to Try

1. Add a `PriorityBlockingQueue` and implement priority-based scheduling
2. Add a task timeout — cancel tasks that run too long using `CompletableFuture.orTimeout()`
3. Add retry logic — automatically retry failed tasks up to N times
4. Add rate limiting — limit to N tasks per second
5. Add a `Callback` observer pattern — notify when specific tasks complete
