/**
 * TaskManagerDemo.java
 *
 * Demonstration of the Concurrent Task Manager.
 *
 * SCENARIOS:
 *   1. Basic tasks     — 5 tasks of varying priorities; wait for all to complete
 *   2. Parallel speedup — 8 tasks × 200ms shows ~4× speedup with 4 threads
 *   3. Error handling  — some tasks intentionally fail; manager stays healthy
 *   4. Priority demo   — tasks submitted with different priorities; note ordering
 *
 * RUN:
 *   javac Task.java TaskManager.java TaskManagerDemo.java
 *   java TaskManagerDemo
 *
 * WHAT TO LOOK FOR:
 *   - Thread names in brackets: [task-worker-N] shows which thread runs each task
 *   - Scenario 2 elapsed time: should be ~400ms (8 × 200ms ÷ 4 threads)
 *   - Scenario 3 [ERR] lines: failures are captured gracefully, pool keeps running
 *   - Final stats summary printed by printStats()
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TaskManagerDemo {

    public static void main(String[] args) throws InterruptedException {
        printHeader();

        // Create a TaskManager with 4 worker threads.
        // With 4 threads: up to 4 tasks can run in parallel; the rest queue up.
        TaskManager manager = new TaskManager(4);

        // Run each demonstration scenario
        scenario1_basicTasks(manager);
        scenario2_parallelSpeedup(manager);
        scenario3_errorHandling(manager);
        scenario4_priorityDemo(manager);

        // Final statistics
        manager.printStats();

        // Graceful shutdown — waits for any remaining tasks (there should be none)
        manager.shutdown();
    }

    // =========================================================================
    // Scenario 1 — Basic tasks with varying priorities
    // =========================================================================

    /**
     * Demonstrates the basic submit → await → print-results workflow.
     *
     * Submits 5 tasks with different priorities and varying simulated durations.
     * Even though CRITICAL is submitted last, it should be picked up early.
     */
    static void scenario1_basicTasks(TaskManager manager) throws InterruptedException {
        printScenarioHeader(1, "Basic tasks — varying priorities and durations");

        List<Task> tasks = new ArrayList<>();

        // Mix of priorities and work durations
        tasks.add(new Task("LoadConfig",    "Load application configuration",   TaskPriority.HIGH,
            () -> {
                simulateWork(150);
                return "Config loaded: 42 settings";
            }));

        tasks.add(new Task("FetchData",     "Fetch records from database",       TaskPriority.NORMAL,
            () -> {
                simulateWork(300);
                return "Fetched 1,250 records";
            }));

        tasks.add(new Task("SendEmail",     "Send notification email",           TaskPriority.LOW,
            () -> {
                simulateWork(200);
                return "Email sent to user@example.com";
            }));

        tasks.add(new Task("ParseXML",      "Parse configuration XML file",      TaskPriority.NORMAL,
            () -> {
                simulateWork(100);
                return "Parsed 15 XML nodes";
            }));

        tasks.add(new Task("AuditLog",      "Write audit log entry",             TaskPriority.CRITICAL,
            () -> {
                simulateWork(50);
                return "Audit entry written: ID=TXN-9821";
            }));

        // Submit all tasks
        List<CompletableFuture<TaskResult>> futures = manager.submitAll(tasks);
        System.out.println("All 5 tasks submitted. Waiting for completion...\n");

        // Block until all tasks are done, then collect results
        List<TaskResult> results = manager.awaitAll(futures);

        System.out.println("\n--- Scenario 1 Results ---");
        results.forEach(System.out::println);
        System.out.println();
    }

    // =========================================================================
    // Scenario 2 — Parallel speedup measurement
    // =========================================================================

    /**
     * Demonstrates that parallelism reduces wall-clock time.
     *
     * 8 tasks × 200ms each:
     *   Sequential time (1 thread):  8 × 200ms = 1,600ms
     *   Parallel time (4 threads):   2 × 200ms =   400ms (2 batches of 4)
     *
     * The demo measures actual elapsed time and calculates the speedup factor.
     */
    static void scenario2_parallelSpeedup(TaskManager manager) throws InterruptedException {
        printScenarioHeader(2, "Parallel speedup — 8 tasks × 200ms with 4 threads");

        final int TASK_COUNT   = 8;
        final int TASK_SLEEP   = 200;

        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= TASK_COUNT; i++) {
            final int taskNum = i;
            tasks.add(new Task(
                "Worker-" + i,
                "Parallel work unit " + i,
                TaskPriority.NORMAL,
                () -> {
                    simulateWork(TASK_SLEEP);
                    return "Unit " + taskNum + " complete";
                }
            ));
        }

        System.out.printf("Submitting %d tasks, each taking %dms...%n", TASK_COUNT, TASK_SLEEP);
        System.out.printf("Expected sequential time: %dms%n", TASK_COUNT * TASK_SLEEP);
        System.out.printf("Expected parallel time (~4 threads): ~%dms%n", (TASK_COUNT / 4) * TASK_SLEEP);
        System.out.println();

        long startTime = System.currentTimeMillis();

        List<CompletableFuture<TaskResult>> futures = manager.submitAll(tasks);
        manager.awaitAll(futures); // wait for all 8 to finish

        long elapsed = System.currentTimeMillis() - startTime;
        double speedup = (double)(TASK_COUNT * TASK_SLEEP) / elapsed;

        System.out.println("\n--- Scenario 2 Results ---");
        System.out.printf("Total elapsed time:  %d ms%n", elapsed);
        System.out.printf("Sequential estimate: %d ms%n", TASK_COUNT * TASK_SLEEP);
        System.out.printf("Speedup factor:      %.1fx%n", speedup);

        if (elapsed < TASK_COUNT * TASK_SLEEP * 0.6) {
            System.out.println("Parallelism confirmed: significantly faster than sequential!");
        } else {
            System.out.println("(Parallelism may have been limited by available CPU cores)");
        }
        System.out.println();
    }

    // =========================================================================
    // Scenario 3 — Error handling
    // =========================================================================

    /**
     * Demonstrates that task failures are captured gracefully.
     *
     * Some tasks are designed to throw exceptions.
     * The TaskManager catches all exceptions and records them in TaskResult.success=false.
     * The thread pool continues running healthy tasks — one bad task doesn't kill the pool.
     */
    static void scenario3_errorHandling(TaskManager manager) {
        printScenarioHeader(3, "Error handling — some tasks intentionally fail");

        List<Task> tasks = new ArrayList<>();

        tasks.add(new Task("GoodTask-1", "This task succeeds", TaskPriority.NORMAL,
            () -> {
                simulateWork(100);
                return "Success: all good here";
            }));

        tasks.add(new Task("BadTask-1", "This task throws an exception", TaskPriority.NORMAL,
            () -> {
                simulateWork(50);
                throw new RuntimeException("Database connection refused");
            }));

        tasks.add(new Task("GoodTask-2", "Another good task", TaskPriority.HIGH,
            () -> {
                simulateWork(75);
                return "Success: computed result = 42";
            }));

        tasks.add(new Task("BadTask-2", "This task throws ArithmeticException", TaskPriority.LOW,
            () -> {
                simulateWork(30);
                // Simulate a divide-by-zero or similar logic error
                int x = 0;
                return "Result: " + (100 / x); // ArithmeticException: / by zero
            }));

        tasks.add(new Task("GoodTask-3", "Final healthy task", TaskPriority.NORMAL,
            () -> {
                simulateWork(120);
                return "Success: pipeline completed";
            }));

        List<CompletableFuture<TaskResult>> futures = manager.submitAll(tasks);
        List<TaskResult> results = manager.awaitAll(futures);

        System.out.println("\n--- Scenario 3 Results (all tasks, including failures) ---");
        results.forEach(System.out::println);

        long successCount = results.stream().filter(TaskResult::success).count();
        long failureCount = results.stream().filter(r -> !r.success()).count();
        System.out.printf("%nSucceeded: %d  |  Failed: %d%n", successCount, failureCount);
        System.out.println("Note: failures were caught gracefully — the thread pool kept running!\n");
    }

    // =========================================================================
    // Scenario 4 — Priority demonstration
    // =========================================================================

    /**
     * Submits tasks with different priorities to illustrate ordering.
     *
     * Because the thread pool has 4 threads and we submit tasks rapidly,
     * many will start immediately rather than queue up. To see priority ordering
     * more clearly, this scenario submits many tasks to fill the pool and queue.
     *
     * In a production scheduler backed by a PriorityBlockingQueue work queue,
     * CRITICAL tasks would always be picked before LOW tasks when threads free up.
     */
    static void scenario4_priorityDemo(TaskManager manager) {
        printScenarioHeader(4, "Priority ordering — CRITICAL tasks are scheduled first");

        List<Task> tasks = new ArrayList<>();

        // Submit tasks in "wrong" order (LOW first) — priority should reorder them
        tasks.add(new Task("LowPriority-1",      "Background cleanup",     TaskPriority.LOW,
            () -> { simulateWork(100); return "Cleanup done"; }));

        tasks.add(new Task("LowPriority-2",      "Archive old logs",       TaskPriority.LOW,
            () -> { simulateWork(100); return "Logs archived"; }));

        tasks.add(new Task("NormalPriority-1",   "Process user request",   TaskPriority.NORMAL,
            () -> { simulateWork(80);  return "Request processed"; }));

        tasks.add(new Task("NormalPriority-2",   "Update search index",    TaskPriority.NORMAL,
            () -> { simulateWork(80);  return "Index updated"; }));

        tasks.add(new Task("HighPriority-1",     "Generate report",        TaskPriority.HIGH,
            () -> { simulateWork(60);  return "Report generated"; }));

        tasks.add(new Task("HighPriority-2",     "Refresh dashboard",      TaskPriority.HIGH,
            () -> { simulateWork(60);  return "Dashboard refreshed"; }));

        tasks.add(new Task("CriticalPriority-1", "CRITICAL: Fraud alert",  TaskPriority.CRITICAL,
            () -> { simulateWork(40);  return "Fraud alert processed"; }));

        tasks.add(new Task("CriticalPriority-2", "CRITICAL: Health check", TaskPriority.CRITICAL,
            () -> { simulateWork(40);  return "Health check passed"; }));

        System.out.println("Submitting 8 tasks (LOW, LOW, NORMAL, NORMAL, HIGH, HIGH, CRITICAL, CRITICAL)");
        System.out.println("Watch the execution log — CRITICAL/HIGH tasks tend to start sooner.\n");

        List<CompletableFuture<TaskResult>> futures = manager.submitAll(tasks);
        List<TaskResult> results = manager.awaitAll(futures);

        System.out.println("\n--- Scenario 4 Results ---");
        results.forEach(System.out::println);
        System.out.println();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Simulate work by sleeping the calling thread.
     *
     * In a real task this would be file I/O, a network request, a DB query, etc.
     * Using Thread.sleep() keeps the demo simple and produces predictable timing.
     *
     * InterruptedException is wrapped as RuntimeException so Callable code
     * doesn't need a try-catch in every lambda.
     *
     * @param milliseconds how long to simulate work (wall-clock time)
     */
    private static void simulateWork(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Work interrupted", e);
        }
    }

    private static void printHeader() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║     Concurrent Task Manager Demo                 ║");
        System.out.println("║     Module 07 — Advanced Java                    ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printScenarioHeader(int num, String description) {
        System.out.println("══════════════════════════════════════════════════");
        System.out.printf( "  Scenario %d: %s%n", num, description);
        System.out.println("══════════════════════════════════════════════════");
    }
}
