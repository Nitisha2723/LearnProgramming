/**
 * Task.java
 *
 * Infrastructure classes for the Concurrent Task Manager mini-project.
 *
 * CONTAINS:
 *   - TaskStatus  enum — lifecycle states a task can be in
 *   - TaskPriority enum — priority levels with numeric ordering
 *   - Task         class — represents a unit of work to be executed
 *
 * KEY DESIGN DECISIONS:
 *   - AtomicLong for ID generation avoids synchronized blocks on every new task
 *   - AtomicReference<TaskStatus> for thread-safe status transitions
 *   - Callable<String> (not Runnable) because tasks need to return a result
 *   - LocalDateTime fields for human-readable timing; Duration for elapsed ms
 */

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

// =============================================================================
// TaskStatus — lifecycle states
// =============================================================================

/**
 * The lifecycle of a task follows this state machine:
 *
 *   [created] → PENDING → RUNNING → COMPLETED
 *                    \            → FAILED
 *                     → CANCELLED  (if cancelled before it starts)
 *
 * PENDING:   Task has been submitted but not yet picked up by a thread
 * RUNNING:   A thread is currently executing the task's Callable
 * COMPLETED: The Callable returned normally
 * FAILED:    The Callable threw an exception
 * CANCELLED: The task was cancelled before it had a chance to run
 */
enum TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

// =============================================================================
// TaskPriority — ordered priority levels
// =============================================================================

/**
 * Priority levels for task ordering.
 *
 * Each level has a numeric value — higher number = higher priority.
 * The PriorityBlockingQueue in TaskManager uses the Comparable implementation
 * on Task to sort by priority descending (CRITICAL first, LOW last).
 *
 * Note: Using an int value field (rather than just ordinal()) makes the
 * ordering intention explicit and resistant to enum reordering.
 */
enum TaskPriority {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    CRITICAL(4);

    private final int value;

    TaskPriority(int value) {
        this.value = value;
    }

    /**
     * Numeric priority — higher is more important.
     * Used by Task.compareTo() for queue ordering.
     */
    public int getValue() {
        return value;
    }
}

// =============================================================================
// Task — the unit of work
// =============================================================================

/**
 * Represents a named, prioritized unit of work to be executed asynchronously.
 *
 * THREAD SAFETY:
 *   - id is final — safe to read from any thread
 *   - name, description, priority are final — immutable after construction
 *   - status is an AtomicReference — updateStatus() is always thread-safe
 *   - startedAt / completedAt are written by exactly one thread (the executor),
 *     so volatile would suffice, but we use plain fields for simplicity
 *     (they are only read after the task completes, which establishes
 *      a happens-before relationship via CompletableFuture.get())
 *
 * IMPLEMENTS Comparable<Task> so PriorityBlockingQueue can order tasks.
 * Higher priority value → smaller compareTo result → front of queue.
 */
class Task implements Comparable<Task> {

    // -------------------------------------------------------------------------
    // ID counter — shared across ALL Task instances
    // -------------------------------------------------------------------------

    /**
     * AtomicLong ensures every task gets a unique ID even when multiple threads
     * create tasks simultaneously. incrementAndGet() is a single atomic
     * read-modify-write operation — no synchronization needed.
     */
    private static final AtomicLong ID_COUNTER = new AtomicLong(0);

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final long   id;           // unique, auto-assigned at construction
    private final String name;         // human-readable task name
    private final String description;  // what this task does
    private final TaskPriority priority;

    /**
     * AtomicReference wraps the TaskStatus so status transitions are
     * atomic (compare-and-set) without requiring synchronized blocks.
     */
    private final AtomicReference<TaskStatus> status;

    /**
     * The actual work this task performs.
     * Callable<String> rather than Runnable because:
     *   - Callable can return a value (the task's output string)
     *   - Callable can throw checked exceptions (which Runnable cannot)
     */
    private final Callable<String> callable;

    // Timing fields — written by the executor thread, read after completion
    private final LocalDateTime submittedAt;
    private volatile LocalDateTime startedAt;
    private volatile LocalDateTime completedAt;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Create a new task.
     *
     * The task starts in PENDING status and receives a unique auto-incremented ID.
     * submittedAt is captured now (at creation time) for queue-wait tracking.
     *
     * @param name        short display name
     * @param description longer description of what the task does
     * @param priority    execution priority (affects queue ordering)
     * @param callable    the work to execute; must return a non-null String result
     */
    public Task(String name, String description, TaskPriority priority, Callable<String> callable) {
        this.id          = ID_COUNTER.incrementAndGet();
        this.name        = name;
        this.description = description;
        this.priority    = priority;
        this.callable    = callable;
        this.status      = new AtomicReference<>(TaskStatus.PENDING);
        this.submittedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Unique task ID, assigned at creation. */
    public long getId()              { return id; }

    /** Short display name. */
    public String getName()          { return name; }

    /** Longer description. */
    public String getDescription()   { return description; }

    /** Execution priority. */
    public TaskPriority getPriority(){ return priority; }

    /** Current lifecycle status. */
    public TaskStatus getStatus()    { return status.get(); }

    /** The callable to execute. */
    public Callable<String> getCallable() { return callable; }

    /** When this task was submitted. */
    public LocalDateTime getSubmittedAt()  { return submittedAt; }

    /** When a thread started executing this task (null if not yet started). */
    public LocalDateTime getStartedAt()    { return startedAt; }

    /** When the task finished (null if not yet done). */
    public LocalDateTime getCompletedAt()  { return completedAt; }

    // -------------------------------------------------------------------------
    // Status management
    // -------------------------------------------------------------------------

    /**
     * Atomically update the task's status.
     *
     * Uses AtomicReference.set() — any thread can call this safely.
     * The TaskManager calls this as the task moves through its lifecycle:
     *   - PENDING → RUNNING  (when a thread picks it up)
     *   - RUNNING → COMPLETED (on successful return)
     *   - RUNNING → FAILED    (on exception)
     *   - PENDING → CANCELLED (on explicit cancellation)
     *
     * @param newStatus the new status to set
     */
    public void updateStatus(TaskStatus newStatus) {
        status.set(newStatus);
    }

    // -------------------------------------------------------------------------
    // Timing helpers — called by TaskManager
    // -------------------------------------------------------------------------

    /** Record when execution started (called by executor just before callable.call()). */
    public void markStarted() {
        this.startedAt = LocalDateTime.now();
    }

    /** Record when execution finished (called by executor after callable.call() returns). */
    public void markCompleted() {
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Execution duration in milliseconds.
     *
     * @return ms from startedAt to completedAt, or -1 if the task hasn't finished
     */
    public long getDurationMs() {
        if (startedAt == null || completedAt == null) return -1;
        return Duration.between(startedAt, completedAt).toMillis();
    }

    // -------------------------------------------------------------------------
    // Comparable — used by PriorityBlockingQueue
    // -------------------------------------------------------------------------

    /**
     * Compare by priority in DESCENDING order so that CRITICAL tasks come first.
     *
     * PriorityBlockingQueue is a min-heap, so it takes the "smallest" element first.
     * By reversing the comparison (other - this instead of this - other), we make
     * higher-priority tasks appear "smaller" and therefore come first.
     */
    @Override
    public int compareTo(Task other) {
        // Descending priority: CRITICAL(4) → HIGH(3) → NORMAL(2) → LOW(1)
        return Integer.compare(other.priority.getValue(), this.priority.getValue());
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    /**
     * Compact representation for logging and debugging.
     *
     * Example: "Task[id=3, name=DataProcess, priority=HIGH, status=RUNNING]"
     */
    @Override
    public String toString() {
        return String.format("Task[id=%d, name=%s, priority=%s, status=%s]",
            id, name, priority, status.get());
    }
}
