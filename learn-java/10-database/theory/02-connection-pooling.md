# Connection Pooling

## Why Connection Pooling?

Creating a database connection is expensive:

1. TCP/IP handshake with the database server
2. Authentication (username + password verification)
3. Session initialization (character sets, timezone, session variables)
4. Allocating server-side resources

On a local machine this takes **5–20 ms**. On a network it can be **50–200 ms+**. For a web application handling 100 requests per second, creating a connection per request burns most of the request budget before any SQL even runs.

**Connection pooling** solves this by maintaining a pool of pre-opened connections that are reused across requests:

```
Application thread 1 ──┐
Application thread 2 ──┤──→  [ Connection Pool ]  ──→  Database
Application thread 3 ──┘     [conn][conn][conn]
                              [conn][conn][conn]
```

When a thread calls `pool.getConnection()`, it gets an idle connection from the pool. When it closes the connection, the connection returns to the pool — it is not actually closed.

**Result:** connection acquisition drops from ~50 ms to ~0.1 ms.

---

## DriverManager vs DataSource

| | `DriverManager.getConnection()` | `DataSource.getConnection()` |
|---|---|---|
| Creates new connection | Every call | First call; reuses after |
| Connection lifecycle | Caller must close | Returned to pool on close |
| Configuration | Scattered throughout code | Centralized in DataSource |
| JEE/Jakarta EE aware | No | Yes |
| Suitable for production | No | Yes |

`DataSource` is the standard `javax.sql` interface. Connection pools implement `DataSource`. Your application code depends only on `DataSource`, making it easy to swap pool implementations.

---

## HikariCP

HikariCP is the fastest and most widely used JDBC connection pool in the Java ecosystem. It is the default pool in Spring Boot.

### Maven dependency

```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>

<!-- Also need the JDBC driver, e.g., H2 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
```

### Basic setup

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DataSourceFactory {

    public static DataSource create() {
        HikariConfig config = new HikariConfig();

        // Required: JDBC URL, credentials
        config.setJdbcUrl("jdbc:h2:mem:mydb;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");

        // Pool sizing
        config.setMaximumPoolSize(10);       // max connections in pool
        config.setMinimumIdle(2);            // keep at least 2 idle connections

        // Timeouts
        config.setConnectionTimeout(30_000);  // max wait for connection: 30s
        config.setIdleTimeout(600_000);       // close idle conn after 10 min
        config.setMaxLifetime(1_800_000);     // recycle conn after 30 min

        // Connection validation
        config.setConnectionTestQuery("SELECT 1"); // for drivers without isValid()

        // Leak detection — logs a warning if a connection is held > 2s
        config.setLeakDetectionThreshold(2_000);

        return new HikariDataSource(config);
    }
}
```

### Usage

```java
DataSource ds = DataSourceFactory.create(); // create once at application startup

// Later, in any method that needs the database:
try (Connection conn = ds.getConnection()) {
    // use conn exactly like DriverManager — same API
    try (PreparedStatement ps = conn.prepareStatement("SELECT ...")) {
        // ...
    }
} // conn returned to pool, NOT closed
```

The `DataSource` instance is typically stored as a singleton (Spring bean, static field, or DI container-managed).

---

## Properties file configuration

HikariCP can also load its configuration from a `.properties` file, which is useful for externalizing credentials:

```properties
# hikari.properties
dataSourceClassName=org.h2.jdbcx.JdbcDataSource
dataSource.url=jdbc:h2:mem:mydb;DB_CLOSE_DELAY=-1
dataSource.user=sa
dataSource.password=
maximumPoolSize=10
minimumIdle=2
connectionTimeout=30000
leakDetectionThreshold=2000
```

```java
HikariConfig config = new HikariConfig("/hikari.properties");
DataSource ds = new HikariDataSource(config);
```

---

## Pool Sizing

A common mistake is setting `maximumPoolSize` too high. More connections != better performance.

**Reasons not to use more connections than needed:**

- Each connection costs memory on the database server (~5–10 MB for PostgreSQL)
- The database has a connection limit (e.g., PostgreSQL defaults to 100)
- Context switching overhead at the database grows with more connections

**HikariCP's recommended formula** (from its documentation):

```
pool size = T_wait / T_query × parallel_threads
```

**Practical starting point:**

```
maximumPoolSize = (number_of_cpu_cores * 2) + number_of_spindle_disks
```

For a 4-core machine with SSD: `4 * 2 + 1 = 9`, so 10 is a good starting point.

For most applications: **5–20 connections** is sufficient. Benchmark before increasing.

---

## Connection Leak Detection

A connection leak occurs when code acquires a connection and never closes it (usually from a missing try-with-resources). The pool eventually exhausts and new requests block forever.

HikariCP's `leakDetectionThreshold` logs a stack trace if a connection is held longer than the threshold:

```java
config.setLeakDetectionThreshold(2_000); // 2000 ms = 2 seconds
```

Example log output:
```
WARN  HikariPool-1 - Connection leak detection triggered for conn0:
java.lang.Exception: Apparent connection leak detected
    at com.example.UserRepository.findById(UserRepository.java:34)
    at com.example.UserService.getUser(UserService.java:22)
```

This pinpoints exactly where the connection was acquired.

---

## HikariCP Pool Metrics

HikariCP exposes pool health through `HikariPoolMXBean`:

```java
HikariDataSource hikariDs = (HikariDataSource) ds;
HikariPoolMXBean pool = hikariDs.getHikariPoolMXBean();

System.out.println("Total connections : " + pool.getTotalConnections());
System.out.println("Active connections: " + pool.getActiveConnections());
System.out.println("Idle connections  : " + pool.getIdleConnections());
System.out.println("Threads waiting   : " + pool.getThreadsAwaitingConnection());
```

High `ThreadsAwaitingConnection` means your pool is too small or queries are too slow.

---

## DataSource vs DriverManager: Code Comparison

### DriverManager approach (don't use in production)

```java
// Scattered across every method — hard to change, no pooling
public List<User> findAll() throws SQLException {
    try (Connection conn = DriverManager.getConnection(
             "jdbc:mysql://localhost/db", "user", "pass");
         Statement stmt = conn.createStatement();
         ResultSet rs   = stmt.executeQuery("SELECT * FROM users")) {
        // ...
    }
}
```

### DataSource approach (correct)

```java
public class UserRepository {
    private final DataSource dataSource; // injected once

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<User> findAll() throws SQLException {
        try (Connection conn = dataSource.getConnection(); // from pool
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT * FROM users")) {
            // ...
        }
    }
}
```

---

## Shutting Down the Pool

Always close `HikariDataSource` when your application shuts down:

```java
HikariDataSource hikariDs = (HikariDataSource) ds;
hikariDs.close(); // closes all connections in the pool
```

In a Spring Boot application this happens automatically. In a standalone app, use a shutdown hook:

```java
Runtime.getRuntime().addShutdownHook(new Thread(hikariDs::close));
```

---

## Summary

| Concept | Key point |
|---|---|
| Connection creation cost | 5–200 ms — amortize with pooling |
| HikariCP | Fastest Java connection pool; default in Spring Boot |
| Pool size | Start with `cores * 2 + 1`; benchmark before increasing |
| Leak detection | Set `leakDetectionThreshold` to catch unclosed connections |
| DataSource | Standard interface — your code never depends on HikariCP directly |
| Shutdown | Call `HikariDataSource.close()` at application shutdown |
