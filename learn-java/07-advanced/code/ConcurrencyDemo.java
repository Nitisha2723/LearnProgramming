/**
 * ConcurrencyDemo.java
 *
 * Demonstrates concurrency concepts in Java:
 *   1. Race condition — the classic bank account problem
 *   2. Fixing with synchronized
 *   3. Fixing with AtomicLong
 *   4. ExecutorService — thread pool management
 *   5. CompletableFuture — async pipelines
 *   6. Thread-safe collections
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class ConcurrencyDemo {

    // ==========================================================================
    // 1. Race Condition
    // ==========================================================================

    /** UNSAFE bank account — demonstrates the race condition */
    static class UnsafeBankAccount {
        private int balance;

        UnsafeBankAccount(int initialBalance) {
            this.balance = initialBalance;
        }

        // NOT thread-safe: read → add → write is three operations, not one
        public void deposit(int amount) {
            // Imagine two threads both read balance=1000 at the same time...
            // Thread A adds 100 → wants to write 1100
            // Thread B adds 100 → wants to write 1100
            // Thread A writes 1100
            // Thread B writes 1100  ← Thread A's deposit is LOST!
            balance = balance + amount;
        }

        public int getBalance() { return balance; }
    }

    /** SAFE bank account — synchronized fixes the race */
    static class SafeBankAccount {
        private int balance;

        SafeBankAccount(int initialBalance) {
            this.balance = initialBalance;
        }

        // synchronized ensures only one thread runs this at a time
        public synchronized void deposit(int amount) {
            balance = balance + amount;
        }

        public synchronized int getBalance() { return balance; }
    }

    /** SAFE bank account using AtomicInteger — lock-free but still safe */
    static class AtomicBankAccount {
        private final AtomicInteger balance;

        AtomicBankAccount(int initialBalance) {
            this.balance = new AtomicInteger(initialBalance);
        }

        // addAndGet is a single atomic operation — no locks needed
        public void deposit(int amount) {
            balance.addAndGet(amount);
        }

        public int getBalance() { return balance.get(); }
    }

    static void demonstrateRaceCondition() throws InterruptedException {
        System.out.println("--- Race Condition Demo ---");
        final int THREADS = 10;
        final int DEPOSITS_PER_THREAD = 1000;
        final int DEPOSIT_AMOUNT = 1;
        final int INITIAL_BALANCE = 0;
        final int EXPECTED = THREADS * DEPOSITS_PER_THREAD * DEPOSIT_AMOUNT;

        // --- UNSAFE ---
        UnsafeBankAccount unsafe = new UnsafeBankAccount(INITIAL_BALANCE);
        List<Thread> unsafeThreads = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < DEPOSITS_PER_THREAD; j++) {
                    unsafe.deposit(DEPOSIT_AMOUNT);
                }
            });
            unsafeThreads.add(t);
            t.start();
        }
        for (Thread t : unsafeThreads) t.join();

        System.out.println("UNSAFE result:  " + unsafe.getBalance() + " (expected " + EXPECTED + ")");
        System.out.println("  -> Lost " + (EXPECTED - unsafe.getBalance()) + " deposits due to race condition!");

        // --- SAFE (synchronized) ---
        SafeBankAccount safe = new SafeBankAccount(INITIAL_BALANCE);
        List<Thread> safeThreads = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < DEPOSITS_PER_THREAD; j++) {
                    safe.deposit(DEPOSIT_AMOUNT);
                }
            });
            safeThreads.add(t);
            t.start();
        }
        for (Thread t : safeThreads) t.join();

        System.out.println("SAFE result:    " + safe.getBalance() + " (expected " + EXPECTED + ")");

        // --- ATOMIC ---
        AtomicBankAccount atomic = new AtomicBankAccount(INITIAL_BALANCE);
        List<Thread> atomicThreads = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < DEPOSITS_PER_THREAD; j++) {
                    atomic.deposit(DEPOSIT_AMOUNT);
                }
            });
            atomicThreads.add(t);
            t.start();
        }
        for (Thread t : atomicThreads) t.join();

        System.out.println("ATOMIC result:  " + atomic.getBalance() + " (expected " + EXPECTED + ")");
        System.out.println();
    }

    // ==========================================================================
    // 2. ExecutorService — Thread Pool
    // ==========================================================================

    static void demonstrateExecutorService() throws InterruptedException, ExecutionException {
        System.out.println("--- ExecutorService Demo ---");

        // Fixed thread pool: 4 threads handle all submitted tasks
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Submit tasks that return results (Callable)
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            final int taskNum = i;
            Future<String> future = executor.submit(() -> {
                Thread.sleep(100 + (taskNum * 10)); // simulate work
                return "Task " + taskNum + " done by " + Thread.currentThread().getName();
            });
            futures.add(future);
        }

        // Collect results
        System.out.println("Results from 8 tasks running on 4 threads:");
        for (Future<String> future : futures) {
            System.out.println("  " + future.get()); // blocks until this task is done
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println();
    }

    // ==========================================================================
    // 3. CompletableFuture — Async Pipelines
    // ==========================================================================

    // Simulate async operations
    static CompletableFuture<String> fetchUserId(String username) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(100); // simulate network call
            return "USER-" + username.hashCode();
        });
    }

    static CompletableFuture<String> fetchUserEmail(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(150);
            return userId + "@example.com";
        });
    }

    static CompletableFuture<String> fetchUserProfile(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(200);
            return "Profile[id=" + userId + ", tier=premium]";
        });
    }

    static void demonstrateCompletableFuture() throws ExecutionException, InterruptedException {
        System.out.println("--- CompletableFuture Demo ---");

        long start = System.currentTimeMillis();

        // Sequential approach (slow):
        // 1. fetch userId (100ms)
        // 2. fetch email (150ms)
        // 3. fetch profile (200ms)
        // Total: 450ms

        // With CompletableFuture — we can parallelize independent operations!

        // Step 1: Get user ID (async)
        CompletableFuture<String> userIdFuture = fetchUserId("alice");

        // Step 2: From userId, fetch BOTH email and profile in parallel
        CompletableFuture<String> emailFuture = userIdFuture.thenCompose(ConcurrencyDemo::fetchUserEmail);
        CompletableFuture<String> profileFuture = userIdFuture.thenCompose(ConcurrencyDemo::fetchUserProfile);

        // Step 3: Combine email and profile once both are ready
        CompletableFuture<String> combinedFuture = emailFuture.thenCombine(profileFuture,
            (email, profile) -> "Email: " + email + ", " + profile);

        // Step 4: Transform the combined result
        CompletableFuture<String> reportFuture = combinedFuture
            .thenApply(info -> "=== USER REPORT ===\n  " + info)
            .exceptionally(ex -> "Failed to generate report: " + ex.getMessage());

        // Get the result (blocks until done)
        String report = reportFuture.get();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println(report);
        System.out.println("  Completed in " + elapsed + "ms (sequential would be ~450ms)");
        System.out.println();

        // allOf — wait for ALL futures to complete
        System.out.println("Processing multiple users in parallel:");
        List<String> usernames = Arrays.asList("alice", "bob", "charlie", "dave");
        long parallelStart = System.currentTimeMillis();

        List<CompletableFuture<String>> userFutures = usernames.stream()
            .map(name -> fetchUserId(name)
                .thenApply(id -> name + " → " + id))
            .collect(Collectors.toList());

        CompletableFuture<Void> allDone = CompletableFuture.allOf(
            userFutures.toArray(new CompletableFuture[0])
        );

        allDone.get(); // wait for all

        System.out.println("All users processed in " + (System.currentTimeMillis() - parallelStart) + "ms:");
        for (CompletableFuture<String> f : userFutures) {
            System.out.println("  " + f.get());
        }

        // anyOf — first to complete wins
        System.out.println("\nRacing 3 servers — first response wins:");
        long raceStart = System.currentTimeMillis();
        CompletableFuture<Object> fastest = CompletableFuture.anyOf(
            CompletableFuture.supplyAsync(() -> { sleep(300); return "Server A"; }),
            CompletableFuture.supplyAsync(() -> { sleep(100); return "Server B"; }),
            CompletableFuture.supplyAsync(() -> { sleep(200); return "Server C"; })
        );
        System.out.println("Winner: " + fastest.get() + " (in " + (System.currentTimeMillis() - raceStart) + "ms)");
        System.out.println();
    }

    // ==========================================================================
    // 4. Thread-safe Collections
    // ==========================================================================

    static void demonstrateThreadSafeCollections() throws InterruptedException {
        System.out.println("--- Thread-Safe Collections ---");
        final int THREADS = 10;
        final int ITEMS_PER_THREAD = 1000;

        // ConcurrentHashMap — thread-safe HashMap
        ConcurrentHashMap<String, Integer> wordCount = new ConcurrentHashMap<>();
        List<Thread> mapThreads = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < ITEMS_PER_THREAD; j++) {
                    // merge is an atomic operation on ConcurrentHashMap
                    wordCount.merge("word", 1, Integer::sum);
                }
            });
            mapThreads.add(t);
            t.start();
        }
        for (Thread t : mapThreads) t.join();
        System.out.println("ConcurrentHashMap word count: " + wordCount.get("word")
            + " (expected " + (THREADS * ITEMS_PER_THREAD) + ")");

        // AtomicLong — thread-safe counter
        AtomicLong counter = new AtomicLong(0);
        List<Thread> counterThreads = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < ITEMS_PER_THREAD; j++) {
                    counter.incrementAndGet();
                }
            });
            counterThreads.add(t);
            t.start();
        }
        for (Thread t : counterThreads) t.join();
        System.out.println("AtomicLong counter: " + counter.get()
            + " (expected " + (THREADS * ITEMS_PER_THREAD) + ")");

        // BlockingQueue — producer-consumer
        System.out.println("\nProducer-Consumer with BlockingQueue:");
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(10);
        List<String> consumed = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(5);

        // Producer thread
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    String item = "item-" + i;
                    queue.put(item); // blocks if queue full
                    System.out.println("  Produced: " + item);
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // Consumer thread
        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    String item = queue.take(); // blocks if queue empty
                    System.out.println("  Consumed: " + item);
                    consumed.add(item);
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        producer.start();
        consumer.start();
        latch.await(5, TimeUnit.SECONDS); // wait until 5 items consumed
        System.out.println("All items consumed: " + consumed);
        System.out.println();
    }

    // ==========================================================================
    // Helper
    // ==========================================================================

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==========================================================================
    // MAIN
    // ==========================================================================

    public static void main(String[] args) throws Exception {
        System.out.println("========================================");
        System.out.println("  CONCURRENCY DEMO");
        System.out.println("========================================\n");

        demonstrateRaceCondition();
        demonstrateExecutorService();
        demonstrateCompletableFuture();
        demonstrateThreadSafeCollections();
    }
}
