/**
 * TaskManager.java
 *
 * The core concurrent task manager.
 *
 * CONTAINS:
 *   - TaskResult  record — immutable snapshot of a task's outcome
 *   - TaskManager class  — submits, tracks, and manages Task lifecycle
 *
 * CONCURRENCY DESIGN:
 *   All public methods in TaskManager are thread-safe:
 *   - submit()           — thread-safe via ExecutorService and ConcurrentHashMap
 *   - cancel()           — atomic status update + Future.cancel()
 *   - getTask()          — reads from ConcurrentHashMap (safe)
 *   - getTasksByStatus() — snapshot reads; safe but may be slightly stale
 *   - printStats()       — snapshot reads; consistent for display
 *   - shutdown()         — graceful drain then force-stop
 */

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

// =============================================================================
// TaskResult — immutable outcome record
// =============================================================================

/**
 * Captures the outcome of a completed (or failed) task.
 *
 * Using a Java record keeps this immutable and concise.
 * TaskManager creates one of these for every task that finishes (successfully or not).
 *
 * @param taskId       the task's unique ID
 * @param taskName     the task's display name
 * @param output       the String returned by the Callable (null if failed)
 * @param success      true if the Callable returned normally
 * @param errorMessage the exception message if success == false (null if success)
 * @param durationMs   wall-clock execution time in milliseconds
 */
record TaskResult(
    long    taskId,
    String  taskName,
    String  output,
    boolean success,
    String  errorMessage,
    long    durationMs
) {
    /**
     * Compact display for logging.
     * Example: "[OK]  DataProcess (42ms): Processed 1000 records"
     *          "[ERR] FaultyTask  (12ms): java.lang.RuntimeException: Boom"
     */
    @Override
    public String toString() {
        if (success) {
            return String.format("[OK]  %-20s (%4dms): %s",
                taskName, durationMs, output);
        } else {
            return String.format("[ERR] %-20s (%4dms): %s",
                taskName, durationMs, errorMessage);
        }
    }
}

// =============================================================================
// TaskManager — core orchestrator
// =============================================================================

/**
 * Thread-safe manager for submitting, tracking, and managing async tasks.
 *
 * USAGE:
 * <pre>
 *   TaskManager mgr = new TaskManager(4); // 4-thread pool
 *
 *   Task t = new Task("MyTask", "Does stuff", TaskPriority.HIGH,
 *                     () -> { doWork(); return "done"; });
 *
 *   CompletableFuture<TaskResult> future = mgr.submit(t);
 *
 *   // Do other work here...
 *
 *   TaskResult result = future.get(); // blocks until complete
 *   mgr.shutdown();
 * </pre>
 */
class TaskManager {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Thread pool that executes submitted tasks. */
    private final ExecutorService executor;

    /**
     * Central registry of all tasks, keyed by task ID.
     *
     * ConcurrentHashMap allows multiple threads to read and write simultaneously
     * without external synchronization (it uses fine-grained internal locking).
     */
    private final ConcurrentHashMap<Long, Task> taskRegistry = new ConcurrentHashMap<>();

    /**
     * Stores the CompletableFuture for each submitted task.
     *
     * This allows cancel() to look up the future and complete it exceptionally,
     * and allows callers to retrieve futures for tasks they submitted earlier.
     */
    private final ConcurrentHashMap<Long, CompletableFuture<TaskResult>> futures =
        new ConcurrentHashMap<>();

    /**
     * Tracks internal futures returned by ExecutorService.submit(),
     * used to cancel tasks that are still queued (PENDING) in the thread pool.
     */
    private final ConcurrentHashMap<Long, Future<?>> executorFutures =
        new ConcurrentHashMap<>();

    /**
     * Note: PriorityBlockingQueue is declared here for conceptual completeness.
     * In this implementation, priority ordering is handled by submitting tasks
     * to the pool — for a production scheduler you might use a custom
     * ThreadPoolExecutor with a PriorityBlockingQueue as its work queue.
     *
     * We keep this field to demonstrate the data structure:
     */
    private final PriorityBlockingQueue<Task> priorityQueue = new PriorityBlockingQueue<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Create a TaskManager with a fixed-size thread pool.
     *
     * The pool reuses threads — creating and destroying threads for every task
     * is expensive. With a fixed pool of N threads, at most N tasks run in
     * parallel; the rest wait in the executor's internal queue.
     *
     * @param threadPoolSize number of worker threads (e.g., number of CPU cores)
     * @throws IllegalArgumentException if threadPoolSize < 1
     */
    public TaskManager(int threadPoolSize) {
        if (threadPoolSize < 1) {
            throw new IllegalArgumentException("Thread pool size must be at least 1");
        }
        // Use an AtomicLong counter for thread naming to avoid deprecated Thread.getId()
        AtomicLong threadCounter = new AtomicLong(1);
        this.executor = Executors.newFixedThreadPool(threadPoolSize,
            // Custom thread factory to give threads descriptive names
            r -> {
                Thread t = new Thread(r);
                t.setName("task-worker-" + threadCounter.getAndIncrement());
                t.setDaemon(false); // non-daemon so JVM waits for them to finish
                return t;
            });

        System.out.printf("[TaskManager] Started with %d worker thread(s)%n", threadPoolSize);
    }

    // -------------------------------------------------------------------------
    // Task submission
    // -------------------------------------------------------------------------

    /**
     * Submit a single task for asynchronous execution.
     *
     * The method:
     *   1. Registers the task in the registry and priority queue
     *   2. Creates a CompletableFuture that will hold the result
     *   3. Submits work to the ExecutorService (work runs on a pool thread)
     *   4. Returns the CompletableFuture immediately (non-blocking)
     *
     * The caller can:
     *   - Call future.get() to block and wait for the result
     *   - Call future.thenAccept(result -> ...) to register a callback
     *   - Ignore the future and check status later via getTask()
     *
     * @param task the task to submit
     * @return a CompletableFuture that will complete with the TaskResult
     */
    public CompletableFuture<TaskResult> submit(Task task) {
        // Register task in the central registry and priority queue
        taskRegistry.put(task.getId(), task);
        priorityQueue.offer(task); // conceptual — pool manages actual ordering

        // Create the future that we will complete manually when the task finishes
        CompletableFuture<TaskResult> resultFuture = new CompletableFuture<>();
        futures.put(task.getId(), resultFuture);

        // Submit to the executor — this returns a Future<Void> we can use to cancel
        Future<?> executorFuture = executor.submit(() -> executeTask(task, resultFuture));
        executorFutures.put(task.getId(), executorFuture);

        return resultFuture;
    }

    /**
     * Submit multiple tasks at once and return all their futures.
     *
     * Tasks are submitted in the order provided; the thread pool will
     * pick them up based on availability (and priority, if the queue supports it).
     *
     * @param tasks list of tasks to submit
     * @return list of CompletableFutures, one per task, in the same order
     */
    public List<CompletableFuture<TaskResult>> submitAll(List<Task> tasks) {
        return tasks.stream()
            .map(this::submit)
            .collect(Collectors.toList());
    }

    /**
     * Wait for all given futures to complete and return their results as a list.
     *
     * This is a BLOCKING call — it returns only when ALL futures are done.
     *
     * CompletableFuture.allOf() returns a Void future that completes when
     * all input futures complete. We then collect individual results.
     *
     * @param futureList list of futures to wait for
     * @return list of TaskResults in the same order as the input futures
     */
    public List<TaskResult> awaitAll(List<CompletableFuture<TaskResult>> futureList) {
        // Build the allOf future — completes when every future in futureList completes
        CompletableFuture<Void> allDone = CompletableFuture.allOf(
            futureList.toArray(new CompletableFuture[0])
        );

        try {
            allDone.get(); // block until all futures complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[TaskManager] Interrupted while waiting for tasks");
        } catch (ExecutionException e) {
            // Individual task errors are captured in TaskResult — this should not happen
            System.err.println("[TaskManager] Unexpected execution exception: " + e.getMessage());
        }

        // Collect results — each future is already done at this point
        return futureList.stream()
            .map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    // Shouldn't happen since allOf finished, but handle defensively
                    return new TaskResult(-1, "unknown", null, false, e.getMessage(), -1);
                }
            })
            .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Task execution — called on the worker thread
    // -------------------------------------------------------------------------

    /**
     * Executes the task's Callable and completes the associated CompletableFuture.
     *
     * This runs on a worker thread from the pool, NOT on the calling thread.
     *
     * Steps:
     *   1. Check if task was cancelled — skip execution if so
     *   2. Update status to RUNNING, record start time
     *   3. Execute the Callable
     *   4. On success: update status to COMPLETED, complete the future
     *   5. On exception: update status to FAILED, complete the future with error info
     *
     * @param task         the task to run
     * @param resultFuture the future to complete when done
     */
    private void executeTask(Task task, CompletableFuture<TaskResult> resultFuture) {
        // If cancelled before we got here, skip execution
        if (task.getStatus() == TaskStatus.CANCELLED) {
            resultFuture.complete(new TaskResult(
                task.getId(), task.getName(), null, false, "Task was cancelled", 0));
            return;
        }

        // --- Mark as RUNNING ---
        task.updateStatus(TaskStatus.RUNNING);
        task.markStarted();

        String threadName = Thread.currentThread().getName();
        System.out.printf("[%s] Starting: %s (priority=%s)%n",
            threadName, task.getName(), task.getPriority());

        try {
            // --- Execute the actual work ---
            String output = task.getCallable().call();

            // --- Mark as COMPLETED ---
            task.markCompleted();
            task.updateStatus(TaskStatus.COMPLETED);

            System.out.printf("[%s] Completed: %s in %dms%n",
                threadName, task.getName(), task.getDurationMs());

            // Complete the future with a success result
            resultFuture.complete(new TaskResult(
                task.getId(),
                task.getName(),
                output,
                true,
                null,
                task.getDurationMs()
            ));

        } catch (Exception e) {
            // --- Mark as FAILED ---
            task.markCompleted(); // record when it "ended" even on failure
            task.updateStatus(TaskStatus.FAILED);

            System.out.printf("[%s] Failed:    %s — %s%n",
                threadName, task.getName(), e.getMessage());

            // Complete the future with a failure result — NOT exceptionally,
            // because we want callers to handle errors via TaskResult, not exceptions.
            resultFuture.complete(new TaskResult(
                task.getId(),
                task.getName(),
                null,
                false,
                e.getClass().getSimpleName() + ": " + e.getMessage(),
                task.getDurationMs()
            ));
        }
    }

    // -------------------------------------------------------------------------
    // Task management
    // -------------------------------------------------------------------------

    /**
     * Attempt to cancel a task.
     *
     * If the task is PENDING (waiting in the thread pool queue):
     *   - Marks it CANCELLED
     *   - Attempts to cancel the executor future (may interrupt if running)
     *   - Completes the result future with a cancellation result
     *
     * If the task is already RUNNING, COMPLETED, or FAILED — does nothing.
     *
     * @param taskId the ID of the task to cancel
     * @return true if the task was successfully marked CANCELLED, false otherwise
     */
    public boolean cancel(long taskId) {
        Task task = taskRegistry.get(taskId);
        if (task == null) return false;

        // Only cancel PENDING tasks — running tasks cannot be safely interrupted
        // in all cases (depends on whether the Callable checks Thread.interrupted())
        if (task.getStatus() == TaskStatus.PENDING) {
            task.updateStatus(TaskStatus.CANCELLED);

            // Try to pull the task out of the executor's work queue
            Future<?> executorFuture = executorFutures.get(taskId);
            if (executorFuture != null) {
                executorFuture.cancel(false); // false = don't interrupt if already running
            }

            // Complete the result future so any waiting callers unblock
            CompletableFuture<TaskResult> resultFuture = futures.get(taskId);
            if (resultFuture != null && !resultFuture.isDone()) {
                resultFuture.complete(new TaskResult(
                    taskId, task.getName(), null, false, "Task was cancelled", 0));
            }

            System.out.printf("[TaskManager] Cancelled: %s (id=%d)%n",
                task.getName(), taskId);
            return true;
        }

        return false; // task already running or done
    }

    // -------------------------------------------------------------------------
    // Query methods
    // -------------------------------------------------------------------------

    /**
     * Look up a task by its ID.
     *
     * @param taskId the task ID to look up
     * @return Optional containing the task, or empty if not found
     */
    public Optional<Task> getTask(long taskId) {
        return Optional.ofNullable(taskRegistry.get(taskId));
    }

    /**
     * Get all tasks currently in the given status.
     *
     * Note: This is a snapshot — the status of tasks may change
     * between when this method reads and when you inspect the result.
     *
     * @param status the status to filter by
     * @return list of tasks with that status at the time of the call
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRegistry.values().stream()
            .filter(t -> t.getStatus() == status)
            .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Reporting
    // -------------------------------------------------------------------------

    /**
     * Print a summary of task counts by status to stdout.
     * Useful for monitoring and debugging.
     */
    public void printStats() {
        long total     = taskRegistry.size();
        long completed = getTasksByStatus(TaskStatus.COMPLETED).size();
        long failed    = getTasksByStatus(TaskStatus.FAILED).size();
        long cancelled = getTasksByStatus(TaskStatus.CANCELLED).size();
        long pending   = getTasksByStatus(TaskStatus.PENDING).size();
        long running   = getTasksByStatus(TaskStatus.RUNNING).size();

        // Calculate average duration of completed tasks
        OptionalDouble avgDuration = taskRegistry.values().stream()
            .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
            .mapToLong(Task::getDurationMs)
            .average();

        System.out.println("\n============================");
        System.out.println("  Task Manager Statistics");
        System.out.println("============================");
        System.out.printf("  Total tasks:        %d%n",  total);
        System.out.printf("  Completed:          %d%n",  completed);
        System.out.printf("  Failed:             %d%n",  failed);
        System.out.printf("  Cancelled:          %d%n",  cancelled);
        System.out.printf("  Pending / Running:  %d / %d%n", pending, running);
        if (avgDuration.isPresent()) {
            System.out.printf("  Avg duration (OK):  %.1f ms%n", avgDuration.getAsDouble());
        }
        System.out.println("============================\n");
    }

    // -------------------------------------------------------------------------
    // Shutdown
    // -------------------------------------------------------------------------

    /**
     * Gracefully shut down the task manager.
     *
     * SHUTDOWN SEQUENCE:
     *   1. executor.shutdown() — stops accepting new tasks, lets running ones finish
     *   2. Wait up to 'timeoutSeconds' for all tasks to complete
     *   3. If still running after the timeout, force-kill with shutdownNow()
     *      (this interrupts running threads — they may not finish cleanly)
     *
     * @param timeoutSeconds how long to wait for tasks to finish before forcing stop
     */
    public void shutdown(int timeoutSeconds) {
        System.out.println("\n[TaskManager] Initiating shutdown...");
        executor.shutdown(); // no more submissions accepted

        try {
            if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                System.out.println("[TaskManager] Timeout reached — forcing shutdown");
                List<Runnable> unfinished = executor.shutdownNow();
                System.out.printf("[TaskManager] %d task(s) were still queued and dropped%n",
                    unfinished.size());
            } else {
                System.out.println("[TaskManager] All tasks finished — clean shutdown complete");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            System.out.println("[TaskManager] Interrupted during shutdown — forcing stop");
        }
    }

    /** Convenience overload with a default 30-second timeout. */
    public void shutdown() {
        shutdown(30);
    }
}
