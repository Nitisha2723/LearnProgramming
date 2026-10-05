# Best Practices for JDBC

## The Repository Pattern

The repository pattern separates database access code from business logic. Each repository handles one aggregate (e.g., `UserRepository`, `OrderRepository`) and exposes domain-oriented methods rather than raw SQL.

**Benefits:**
- SQL is contained in one class — easy to change
- Business logic doesn't know about databases
- Easy to test: mock the repository interface in unit tests
- Easy to swap JDBC for JPA/Hibernate later by reimplementing the interface

### Interface definition

```java
public interface UserRepository {
    Optional<User> findById(int id);
    List<User>     findAll();
    User           save(User user);       // INSERT — returns user with generated id
    boolean        update(User user);     // UPDATE
    boolean        delete(int id);        // DELETE
}
```

### JDBC implementation

```java
public class JdbcUserRepository implements UserRepository {

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT id, name, email FROM users WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed for id=" + id, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email")
        );
    }
}
```

The `mapRow` helper converts a `ResultSet` row into a domain object. Extract it to avoid duplicating column-name strings.

---

## try-with-resources

JDBC resources (`Connection`, `Statement`, `PreparedStatement`, `ResultSet`) must always be closed. Not closing them causes:

- **Connection leaks** — pool exhaustion; application hangs
- **Cursor leaks** — database runs out of open cursors
- **Memory leaks** — JVM heap grows over time

`try-with-resources` closes resources automatically, even if an exception is thrown:

```java
// Correct: resources closed in reverse order (rs → ps → conn)
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement("SELECT ...");
     ResultSet rs = ps.executeQuery()) {

    while (rs.next()) { ... }
}
// All three closed here, even if rs.next() throws
```

**Never** do this — the connection leaks if `rs.next()` throws:

```java
// WRONG
Connection conn = dataSource.getConnection();
PreparedStatement ps = conn.prepareStatement("SELECT ...");
ResultSet rs = ps.executeQuery();
while (rs.next()) { ... }
rs.close();
ps.close();
conn.close(); // never reached if an exception occurs above
```

---

## SQL Anti-Patterns

### String concatenation (SQL injection)

```java
// WRONG
String query = "SELECT * FROM users WHERE name = '" + name + "'";
stmt.executeQuery(query);

// RIGHT
PreparedStatement ps = conn.prepareStatement(
    "SELECT * FROM users WHERE name = ?");
ps.setString(1, name);
```

### SELECT * in production code

```java
// WRONG — fetches all columns even if you need only 2
"SELECT * FROM orders"

// RIGHT — explicit columns, clear contract
"SELECT id, status, total FROM orders"
```

`SELECT *` breaks your code when the table schema changes (new columns appear, columns reorder). It also fetches data you don't need.

### Constructing IN clauses with loops

```java
// WRONG — one round trip per id
for (int id : ids) {
    findById(id); // N database calls
}

// RIGHT — one round trip for all ids
String placeholders = ids.stream()
    .map(i -> "?")
    .collect(Collectors.joining(", "));
PreparedStatement ps = conn.prepareStatement(
    "SELECT * FROM users WHERE id IN (" + placeholders + ")");
for (int i = 0; i < ids.size(); i++) {
    ps.setInt(i + 1, ids.get(i));
}
```

The `IN` clause size is dynamic, so the placeholder string is built from `?` characters — still parameterized, still safe.

### Ignoring row counts

```java
// WRONG — did the update succeed?
ps.executeUpdate();

// RIGHT — verify the expected number of rows changed
int rowsUpdated = ps.executeUpdate();
if (rowsUpdated == 0) {
    throw new EntityNotFoundException("User " + id + " not found");
}
```

---

## Parameterized Queries

Always use `?` for any value that comes from outside your code — user input, configuration files, API parameters, even values read from the database.

### Setting typed parameters

```java
ps.setInt(1, userId);                          // int
ps.setString(2, name);                         // String
ps.setDouble(3, price);                        // double
ps.setBigDecimal(4, amount);                   // BigDecimal (money)
ps.setBoolean(5, active);                      // boolean
ps.setDate(6, Date.valueOf(localDate));         // java.time.LocalDate
ps.setTimestamp(7, Timestamp.valueOf(localDT)); // java.time.LocalDateTime
ps.setNull(8, Types.VARCHAR);                  // SQL NULL
```

### Handling NULL

```java
// Reading nullable columns
String middleName = rs.getString("middle_name");
if (rs.wasNull()) {
    middleName = null; // rs.getString returns null for SQL NULL, wasNull() confirms
}

// Writing NULL
if (user.getMiddleName() != null) {
    ps.setString(3, user.getMiddleName());
} else {
    ps.setNull(3, Types.VARCHAR);
}
```

---

## Connection String Security

Connection strings often contain credentials. Never hardcode them or commit them to source control.

### Anti-patterns

```java
// WRONG — credentials in source code
String url  = "jdbc:mysql://prod-db:3306/myapp";
String user = "admin";
String pass = "hunter2"; // visible in git history forever
```

### Solutions

**Environment variables (preferred for containers/cloud):**

```java
String url  = System.getenv("DB_URL");
String user = System.getenv("DB_USER");
String pass = System.getenv("DB_PASS");
```

**External properties file (not committed to git):**

```java
Properties props = new Properties();
try (InputStream is = new FileInputStream("config/db.properties")) {
    props.load(is);
}
String url  = props.getProperty("db.url");
String user = props.getProperty("db.user");
String pass = props.getProperty("db.password");
```

Add `config/db.properties` to `.gitignore`. Provide a `config/db.properties.example` with placeholder values.

**Spring Boot** — use `application.properties` (excluded from git via `.gitignore`) or Spring Cloud Config / Vault for secrets in production.

---

## When to Use ORM vs Plain JDBC

Both have their place. Understanding when to use each prevents over-engineering.

### Use plain JDBC when:

- **Performance is critical** — you want exact control over queries and indexes
- **Complex custom queries** — reporting, analytics, batch jobs with intricate SQL
- **Microservices with simple schemas** — a service with 3–5 tables doesn't benefit from ORM overhead
- **Learning** — understanding what JDBC does is essential before abstracting it away
- **Legacy schema** — tables don't map well to objects (no primary keys, weird naming)

### Use an ORM (JPA/Hibernate) when:

- **Domain-rich application** — complex object graph with many relationships
- **Productivity matters more than raw performance** — CRUD for 20+ tables
- **You need database portability** — ORM dialects handle vendor differences
- **Your team knows JPA** — consistency with existing conventions

### Middle ground — Spring JDBC Template

Spring's `JdbcTemplate` removes JDBC boilerplate (try-with-resources, exception translation) while keeping you in control of SQL:

```java
List<User> users = jdbcTemplate.query(
    "SELECT id, name, email FROM users WHERE active = ?",
    (rs, rowNum) -> new User(rs.getInt("id"), rs.getString("name"), rs.getString("email")),
    true);
```

**Decision guide:**

```
Simple CRUD, team knows JPA, complex domain → JPA/Hibernate
Complex queries, performance sensitive, small team → plain JDBC
Spring app, want convenience without full ORM → Spring JdbcTemplate
```

---

## Batch Updates

For inserting or updating many rows, use batch operations instead of individual statements:

```java
String sql = "INSERT INTO events (type, payload, created_at) VALUES (?, ?, ?)";
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {

    conn.setAutoCommit(false);

    for (Event event : events) {
        ps.setString(1, event.type());
        ps.setString(2, event.payload());
        ps.setTimestamp(3, Timestamp.valueOf(event.createdAt()));
        ps.addBatch();                        // queue the statement

        if (++count % 500 == 0) {
            ps.executeBatch();                // send batch every 500 rows
        }
    }
    ps.executeBatch();                        // flush remainder
    conn.commit();
}
```

Batch inserts can be **10–100x faster** than individual inserts for large datasets.

---

## Summary

| Practice | Why |
|---|---|
| Repository pattern | Separates SQL from business logic; easier to test and maintain |
| try-with-resources | Guarantees Connection/Statement/ResultSet are always closed |
| PreparedStatement everywhere | Prevents SQL injection; better performance |
| Explicit column names | Resilient to schema changes; avoids fetching unneeded data |
| Externalize credentials | Never hardcode DB passwords; use env vars or config files |
| Verify row counts | Detect missing rows early instead of silently ignoring failures |
| Batch operations | Dramatically faster for bulk inserts/updates |
| JDBC vs ORM | Choose based on query complexity, team skills, and performance needs |
