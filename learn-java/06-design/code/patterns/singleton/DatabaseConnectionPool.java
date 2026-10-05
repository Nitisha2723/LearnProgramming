package patterns.singleton;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DESIGN PATTERN: Singleton
 * =========================
 * Intent: Ensure a class has only ONE instance and provide a global access point to it.
 *
 * REAL-WORLD USE CASE: Database Connection Pool
 * -----------------------------------------------
 * Creating database connections is expensive (network round-trip, authentication,
 * resource allocation). A connection pool maintains a fixed set of pre-created
 * connections and hands them out on demand, returning them to the pool after use.
 *
 * WHY SINGLETON? The entire application should share ONE pool. If every component
 * created its own pool, you'd have hundreds of idle connections and resource exhaustion.
 *
 * ============================================================
 * THE FOUR MAIN SINGLETON APPROACHES (ranked best to worst):
 * ============================================================
 *
 * 1. ENUM SINGLETON (BEST — used below)
 *    - Thread-safe by the JVM specification
 *    - Serialization-safe automatically (no readResolve() needed)
 *    - Reflection-safe (cannot be broken by newInstance())
 *    - Lazy-initialized by the class loader
 *    - Concise, readable
 *    DOWNSIDE: Cannot extend another class (enums can't extend classes).
 *
 * 2. STATIC HOLDER / INITIALIZATION-ON-DEMAND (very good)
 *    - Thread-safe via class loader guarantee
 *    - Lazy initialization (inner class not loaded until needed)
 *    - NOT serialization-safe (needs readResolve)
 *    - CAN be broken by reflection
 *
 * 3. DOUBLE-CHECKED LOCKING (acceptable, but fragile)
 *    - Requires 'volatile' keyword (often forgotten)
 *    - Thread-safe on Java 5+ (due to memory model fix)
 *    - Complex and error-prone
 *    - Shown below as a commented-out comparison
 *
 * 4. SYNCHRONIZED METHOD (worst for performance)
 *    - Thread-safe but every call acquires the lock
 *    - 25-100x slower than unsynchronized
 *    - Only acceptable for rarely-called initialization
 */

// =============================================================================
// FILE STRUCTURE:
//   1. DatabaseConnection      — simulates a real DB connection
//   2. ConnectionPool (enum)   — THE SINGLETON (enum approach)
//   3. ConnectionPoolDCL       — Double-Checked Locking comparison (inner class)
//   4. DatabaseConnectionPool  — Demo runner with main()
// =============================================================================

// -----------------------------------------------------------------------------
// Simulated Database Connection object
// In a real app, this would wrap java.sql.Connection
// -----------------------------------------------------------------------------
class DatabaseConnection {
    private final String id;
    private boolean inUse;
    private final LocalDateTime createdAt;

    public DatabaseConnection() {
        // Generate a unique ID to make each connection identifiable in demos
        this.id = "CONN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.inUse = false;
        this.createdAt = LocalDateTime.now();
        System.out.println("  [Pool] Created new connection: " + id);
    }

    public String getId() { return id; }
    public boolean isInUse() { return inUse; }
    public void setInUse(boolean inUse) { this.inUse = inUse; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Simulates executing a query — in real code this would call conn.prepareStatement(...)
    public String executeQuery(String sql) {
        if (!inUse) throw new IllegalStateException("Connection " + id + " is not checked out!");
        // Simulate some work
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return "[" + id + "] Result of: " + sql;
    }

    @Override
    public String toString() {
        return "DatabaseConnection{id='" + id + "', inUse=" + inUse + "}";
    }
}

// =============================================================================
// THE SINGLETON: Enum-based Connection Pool
// =============================================================================
/**
 * WHY ENUM IS THE BEST SINGLETON APPROACH:
 *
 * The Java Language Specification guarantees that enum constants are instantiated
 * exactly once per JVM, in a thread-safe manner, during class loading. This means:
 *
 *   - The JVM does the heavy lifting — no synchronized blocks, no volatile fields
 *   - It is SERIALIZATION-SAFE: Java automatically prevents deserialization from
 *     creating new enum instances. No need to override readResolve().
 *   - It is REFLECTION-SAFE: Constructor.newInstance() throws IllegalArgumentException
 *     for enum types. You cannot bypass the singleton with reflection.
 *   - It is CLONE-SAFE: Enum cloning is prohibited by the JVM.
 *
 * Joshua Bloch (Effective Java, Item 3) says:
 *   "A single-element enum type is often the best way to implement a singleton."
 *
 * LIMITATION: Enums cannot extend another class. If your singleton needs
 * to extend a base class, use the Static Holder pattern instead.
 */
enum ConnectionPool {
    // The single instance — this line runs once when the enum class is loaded
    INSTANCE;

    // Pool configuration
    private static final int POOL_SIZE = 5;

    // The actual pool of connections — initialized in the constructor
    // Deque gives us O(1) add/remove from both ends
    private final Deque<DatabaseConnection> availableConnections;
    private final AtomicInteger totalCheckouts;

    /**
     * Enum constructors run once, guaranteed by the JVM.
     * We initialize the pool here — this is our "eager initialization".
     *
     * NOTE: Enum constructors are always private (implicitly or explicitly).
     * You cannot call this constructor from outside — the JVM enforces it.
     */
    ConnectionPool() {
        System.out.println("[ConnectionPool] Initializing pool with " + POOL_SIZE + " connections...");
        availableConnections = new ArrayDeque<>();
        totalCheckouts = new AtomicInteger(0);

        // Pre-create all connections at startup
        // Trade-off: startup is slower, but first requests are fast
        for (int i = 0; i < POOL_SIZE; i++) {
            availableConnections.add(new DatabaseConnection());
        }
        System.out.println("[ConnectionPool] Pool ready.\n");
    }

    /**
     * Check out a connection from the pool.
     *
     * Synchronized on 'this' (the INSTANCE) to prevent two threads from
     * getting the same connection simultaneously.
     *
     * In production code, you'd use a java.util.concurrent.BlockingDeque
     * and call poll(timeout, TimeUnit) to wait for an available connection
     * instead of returning null.
     *
     * @return A DatabaseConnection, or null if pool is exhausted
     */
    public synchronized DatabaseConnection getConnection() {
        if (availableConnections.isEmpty()) {
            System.err.println("[ConnectionPool] WARNING: Pool exhausted! All " + POOL_SIZE + " connections in use.");
            return null; // Production code would throw or block here
        }

        DatabaseConnection conn = availableConnections.poll(); // removes from head
        conn.setInUse(true);
        totalCheckouts.incrementAndGet();
        System.out.println("[ConnectionPool] Checked out " + conn.getId()
                + " | Available: " + availableConnections.size() + "/" + POOL_SIZE);
        return conn;
    }

    /**
     * Return a connection to the pool.
     *
     * Critical: must be called in a finally block to prevent connection leaks.
     *
     * @param conn The connection to return
     */
    public synchronized void releaseConnection(DatabaseConnection conn) {
        if (conn == null) return;
        conn.setInUse(false);
        availableConnections.addLast(conn); // return to tail of queue
        System.out.println("[ConnectionPool] Released " + conn.getId()
                + " | Available: " + availableConnections.size() + "/" + POOL_SIZE);
    }

    /** Total connections in the pool (in use + available) */
    public int getPoolSize() {
        return POOL_SIZE;
    }

    /** Number of connections not currently in use */
    public synchronized int getAvailableConnections() {
        return availableConnections.size();
    }

    /** Number of connections currently checked out */
    public synchronized int getCheckedOutConnections() {
        return POOL_SIZE - availableConnections.size();
    }

    /** Total number of checkouts since pool creation */
    public int getTotalCheckouts() {
        return totalCheckouts.get();
    }
}

// =============================================================================
// COMPARISON: Double-Checked Locking Singleton
//
// This is shown for educational purposes only. The enum approach above is
// simpler, safer, and preferred. This is provided to explain WHY enum is better.
// =============================================================================
/**
 * DOUBLE-CHECKED LOCKING SINGLETON — comparison approach
 *
 * Common in legacy code. Works correctly on Java 5+ only if volatile is present.
 *
 * WHY volatile IS CRITICAL:
 * Without volatile, the JVM can reorder the steps of object creation:
 *   1. Allocate memory
 *   2. Assign reference to 'instance' field  ← if reordered, another thread sees
 *   3. Run constructor                           a non-null but uninitialized object!
 *
 * The 'volatile' keyword creates a memory barrier preventing this reorder.
 *
 * VULNERABILITIES vs Enum:
 *   - Reflection: Field.setAccessible(true) + Constructor.newInstance() can bypass
 *   - Serialization: Without readResolve(), deserialization creates a second instance
 *   - Complex: Easy to get wrong (forgotten volatile, removed by well-meaning refactor)
 */
class ConnectionPoolDCL {
    // volatile is REQUIRED for correct double-checked locking on Java 5+
    private static volatile ConnectionPoolDCL instance;

    private static final int POOL_SIZE = 5;
    private final Deque<DatabaseConnection> availableConnections;

    // Private constructor prevents external instantiation
    private ConnectionPoolDCL() {
        availableConnections = new ArrayDeque<>();
        for (int i = 0; i < POOL_SIZE; i++) {
            availableConnections.add(new DatabaseConnection());
        }
    }

    /**
     * Double-Checked Locking:
     * - First check (without lock): avoids lock overhead for the common case
     *   where instance already exists
     * - Second check (with lock): prevents race condition during first-time
     *   initialization where two threads both pass the first null check
     */
    public static ConnectionPoolDCL getInstance() {
        if (instance == null) {                    // Check 1: no lock (fast path)
            synchronized (ConnectionPoolDCL.class) {
                if (instance == null) {            // Check 2: under lock (slow path)
                    instance = new ConnectionPoolDCL();
                }
            }
        }
        return instance;
    }

    // Methods would be the same as above...
    // Not fully implemented — this is for structural comparison only
    public int getPoolSize() { return POOL_SIZE; }
}

// =============================================================================
// DEMO RUNNER
// =============================================================================
/**
 * Demonstrates the singleton pattern in a simulated multi-threaded environment.
 *
 * Access pattern: ConnectionPool.INSTANCE.getConnection()
 * Notice: No 'new', no factory call — just direct enum access.
 */
public class DatabaseConnectionPool {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(60));
        System.out.println("DESIGN PATTERN: Singleton (Enum-Based)");
        System.out.println("USE CASE: Database Connection Pool");
        System.out.println("=".repeat(60) + "\n");

        // -------------------------------------------------------
        // DEMO 1: Prove Singleton Identity
        // -------------------------------------------------------
        System.out.println("--- DEMO 1: Singleton Identity ---");
        ConnectionPool pool1 = ConnectionPool.INSTANCE;
        ConnectionPool pool2 = ConnectionPool.INSTANCE;

        // Both variables point to the exact same object
        System.out.println("pool1 == pool2: " + (pool1 == pool2));
        System.out.println("pool1.hashCode(): " + pool1.hashCode());
        System.out.println("pool2.hashCode(): " + pool2.hashCode());
        System.out.println("Same instance: " + System.identityHashCode(pool1)
                + " == " + System.identityHashCode(pool2) + "\n");

        // -------------------------------------------------------
        // DEMO 2: Basic Connection Checkout / Return
        // -------------------------------------------------------
        System.out.println("--- DEMO 2: Basic Connection Usage ---");
        System.out.println("Pool size: " + ConnectionPool.INSTANCE.getPoolSize());
        System.out.println("Available: " + ConnectionPool.INSTANCE.getAvailableConnections() + "\n");

        // Always use try-finally to guarantee connection is returned
        DatabaseConnection conn = ConnectionPool.INSTANCE.getConnection();
        try {
            if (conn != null) {
                String result = conn.executeQuery("SELECT * FROM users WHERE active = 1");
                System.out.println("Query result: " + result);
            }
        } finally {
            // This MUST be in finally — if an exception occurs, we still return the connection
            ConnectionPool.INSTANCE.releaseConnection(conn);
        }

        System.out.println("Available after release: "
                + ConnectionPool.INSTANCE.getAvailableConnections() + "\n");

        // -------------------------------------------------------
        // DEMO 3: Pool Exhaustion Scenario
        // -------------------------------------------------------
        System.out.println("--- DEMO 3: Pool Exhaustion ---");
        DatabaseConnection[] checkedOut = new DatabaseConnection[7]; // More than pool size

        // Check out all 5 connections
        for (int i = 0; i < 7; i++) {
            checkedOut[i] = ConnectionPool.INSTANCE.getConnection();
        }

        System.out.println("\nChecked out: " + ConnectionPool.INSTANCE.getCheckedOutConnections()
                + ", Available: " + ConnectionPool.INSTANCE.getAvailableConnections());

        // Return all non-null connections
        System.out.println("\nReleasing all connections...");
        for (DatabaseConnection c : checkedOut) {
            ConnectionPool.INSTANCE.releaseConnection(c);
        }

        System.out.println("\nAfter release — Available: "
                + ConnectionPool.INSTANCE.getAvailableConnections() + "\n");

        // -------------------------------------------------------
        // DEMO 4: Simulated Multi-Threaded Usage
        // -------------------------------------------------------
        System.out.println("--- DEMO 4: Simulated Multi-Threaded Access ---");
        System.out.println("Spawning 4 worker threads sharing ONE pool singleton...\n");

        Thread[] workers = new Thread[4];
        for (int i = 0; i < workers.length; i++) {
            final int workerId = i + 1;
            workers[i] = new Thread(() -> {
                // All threads use the SAME ConnectionPool.INSTANCE
                DatabaseConnection c = ConnectionPool.INSTANCE.getConnection();
                try {
                    if (c != null) {
                        System.out.println("  Worker " + workerId + " executing query on " + c.getId());
                        c.executeQuery("SELECT count(*) FROM orders WHERE worker_id = " + workerId);
                        System.out.println("  Worker " + workerId + " done.");
                    } else {
                        System.err.println("  Worker " + workerId + ": no connection available!");
                    }
                } finally {
                    ConnectionPool.INSTANCE.releaseConnection(c);
                }
            }, "Worker-" + i);
        }

        // Start all threads
        for (Thread w : workers) w.start();
        // Wait for all to complete
        for (Thread w : workers) w.join();

        System.out.println("\n--- Final Statistics ---");
        System.out.println("Pool size:         " + ConnectionPool.INSTANCE.getPoolSize());
        System.out.println("Available now:     " + ConnectionPool.INSTANCE.getAvailableConnections());
        System.out.println("Total checkouts:   " + ConnectionPool.INSTANCE.getTotalCheckouts());

        System.out.println("\n--- Enum Singleton vs. Other Approaches ---");
        System.out.println("Enum:  Thread-safe? YES | Serialization-safe? YES | Reflection-safe? YES");
        System.out.println("DCL:   Thread-safe? YES | Serialization-safe? NO  | Reflection-safe? NO");
        System.out.println("Lazy:  Thread-safe? NO  | Serialization-safe? NO  | Reflection-safe? NO");

        System.out.println("\nDone!");
    }
}
