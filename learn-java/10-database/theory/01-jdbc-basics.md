# JDBC Basics

## What is JDBC?

JDBC (Java Database Connectivity) is the standard Java API for connecting to relational databases. It provides a uniform interface so your Java code works with MySQL, PostgreSQL, SQLite, H2, and others — you only change the driver and connection URL, not your logic.

JDBC lives in the `java.sql` package and has been part of the Java SE standard library since Java 1.1.

```
Your Java code
     |
  JDBC API (java.sql.*)
     |
  JDBC Driver (vendor-specific)
     |
  Database (MySQL, PostgreSQL, H2, ...)
```

---

## JDBC Drivers

A JDBC driver translates JDBC calls into the wire protocol the specific database understands. There are four driver types; Type 4 ("thin driver") is what you use today: a pure Java library that speaks directly to the database over a socket.

| Driver type | Example |
|---|---|
| Type 4 pure-Java | MySQL Connector/J, PostgreSQL JDBC, H2 |

You add the driver as a Maven/Gradle dependency. It registers itself automatically via `java.util.ServiceLoader` (since JDBC 4.0).

```xml
<!-- H2 in-memory database — great for learning and tests -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>

<!-- MySQL example -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

---

## Connection URLs

Every database has its own URL format. The driver is selected based on the URL prefix.

```
jdbc:<subprotocol>://<host>:<port>/<database>[?key=value&...]
```

| Database | URL example |
|---|---|
| H2 in-memory | `jdbc:h2:mem:mydb;DB_CLOSE_DELAY=-1` |
| H2 file-based | `jdbc:h2:~/data/mydb` |
| MySQL | `jdbc:mysql://localhost:3306/mydb` |
| PostgreSQL | `jdbc:postgresql://localhost:5432/mydb` |
| SQLite | `jdbc:sqlite:mydb.db` |

`DB_CLOSE_DELAY=-1` keeps the H2 in-memory database alive as long as the JVM runs (otherwise it drops when the last connection closes).

---

## Obtaining a Connection

### DriverManager (simple, not for production)

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

String url  = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
String user = "sa";
String pass = "";

try (Connection conn = DriverManager.getConnection(url, user, pass)) {
    // use conn
} // auto-closes
```

`DriverManager.getConnection` creates a brand new TCP connection every time — expensive. Fine for demos and one-off scripts; use a DataSource/connection pool for production.

### DataSource (production)

```java
// HikariCP example — see module 02
DataSource ds = buildDataSource(); // configured once at startup
try (Connection conn = ds.getConnection()) {
    // use conn — returned to pool on close
}
```

---

## Statement vs PreparedStatement

### Statement — DO NOT use for user input

```java
// BAD: string concatenation opens the door to SQL injection
String name = userInput; // could be "' OR '1'='1"
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(
    "SELECT * FROM users WHERE name = '" + name + "'"); // NEVER do this
```

### PreparedStatement — always prefer this

```java
String sql = "SELECT * FROM users WHERE name = ?";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, name);          // ? placeholder, 1-indexed
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) { ... }
    }
}
```

**Why PreparedStatement is better:**

1. **SQL injection prevention** — the driver sends the SQL and data separately; user input can never alter the query structure.
2. **Performance** — the database parses and plans the query once; subsequent executions with different parameters reuse the plan.
3. **Type safety** — `setInt`, `setString`, `setDate`, etc. handle quoting and encoding correctly.

---

## SQL Injection

SQL injection is one of the most common and damaging security vulnerabilities. It lets an attacker read, modify, or delete all your data by manipulating a query string.

```java
// Attacker sets: name = "Alice'; DROP TABLE users; --"
String sql = "SELECT * FROM users WHERE name = '" + name + "'";
// Becomes: SELECT * FROM users WHERE name = 'Alice'; DROP TABLE users; --'
// The entire users table is gone.
```

With `PreparedStatement`, the same input is just a literal string — it cannot be interpreted as SQL:

```java
PreparedStatement ps = conn.prepareStatement(
    "SELECT * FROM users WHERE name = ?");
ps.setString(1, "Alice'; DROP TABLE users; --");
// Safe: the database receives name as a parameter, not SQL
```

**Rule**: Never concatenate user-supplied values into SQL strings. Always use `?` placeholders.

---

## ResultSet

`ResultSet` represents a cursor over the rows returned by a query.

```java
String sql = "SELECT id, name, email FROM users WHERE id = ?";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, userId);
    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {                        // moves cursor to first row
            int    id    = rs.getInt("id");
            String name  = rs.getString("name");
            String email = rs.getString("email");
        }
        // rs.next() returns false when no more rows
    }
}
```

**Key methods:**

| Method | Returns |
|---|---|
| `rs.next()` | `true` if cursor moved to the next row |
| `rs.getInt(columnLabel)` | `int` value |
| `rs.getString(columnLabel)` | `String` value |
| `rs.getDouble(columnLabel)` | `double` value |
| `rs.getBigDecimal(columnLabel)` | `BigDecimal` value |
| `rs.getTimestamp(columnLabel)` | `Timestamp` (use `.toLocalDateTime()`) |
| `rs.wasNull()` | `true` if last get returned SQL NULL |

You can use column names (`"id"`) or 1-based column indexes (`1`). Names are more readable and resilient to column reordering.

---

## CRUD Operations

### Create (INSERT)

```java
String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
try (PreparedStatement ps = conn.prepareStatement(
        sql, Statement.RETURN_GENERATED_KEYS)) {
    ps.setString(1, "Alice");
    ps.setString(2, "alice@example.com");
    int rowsAffected = ps.executeUpdate();  // returns number of rows inserted

    try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
            long newId = keys.getLong(1);   // the auto-generated primary key
        }
    }
}
```

### Read (SELECT)

```java
String sql = "SELECT id, name, email FROM users";
try (Statement stmt = conn.createStatement();
     ResultSet rs   = stmt.executeQuery(sql)) {
    while (rs.next()) {
        System.out.printf("%d  %-20s  %s%n",
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"));
    }
}
```

### Update (UPDATE)

```java
String sql = "UPDATE users SET email = ? WHERE id = ?";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, "newemail@example.com");
    ps.setInt(2, userId);
    int rowsUpdated = ps.executeUpdate();
}
```

### Delete (DELETE)

```java
String sql = "DELETE FROM users WHERE id = ?";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, userId);
    int rowsDeleted = ps.executeUpdate();
}
```

---

## try-with-resources

`Connection`, `Statement`/`PreparedStatement`, and `ResultSet` all implement `AutoCloseable`. Always close them in reverse order of creation; try-with-resources handles this automatically.

```java
// Correct nesting — ResultSet closed before Statement, Statement before Connection
try (Connection conn = DriverManager.getConnection(url, user, pass);
     PreparedStatement ps = conn.prepareStatement("SELECT ...");
     ResultSet rs = ps.executeQuery()) {

    while (rs.next()) { ... }
} // rs closed, then ps, then conn — even if an exception occurs
```

Not closing resources causes:
- Connection leaks (eventually exhausts the pool)
- Cursor leaks in the database
- Memory pressure in the JVM

---

## DDL — Creating Tables

```java
String ddl = """
    CREATE TABLE IF NOT EXISTS users (
        id    INTEGER PRIMARY KEY AUTO_INCREMENT,
        name  VARCHAR(100) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE
    )
    """;
try (Statement stmt = conn.createStatement()) {
    stmt.execute(ddl);  // use execute() for DDL, executeUpdate() for DML
}
```

---

## Summary

| Concept | Key point |
|---|---|
| DriverManager | Simple connection creation — no pooling |
| PreparedStatement | Always use for user input — prevents SQL injection |
| ResultSet | Cursor over rows — call `next()` to advance |
| try-with-resources | Always close Connection, Statement, ResultSet |
| executeQuery() | Returns `ResultSet` (SELECT) |
| executeUpdate() | Returns row count (INSERT/UPDATE/DELETE/DDL) |
| getGeneratedKeys() | Retrieve auto-increment primary keys after INSERT |
