# Module 10: Database Programming with Java

Learn to work with relational databases from Java using JDBC, connection pooling with HikariCP, and core SQL patterns. This module uses H2, an in-memory database that runs entirely in the JVM — no external database installation required.

---

## Learning Objectives

By the end of this module you will be able to:

- Connect to a database using JDBC `DriverManager` and a `DataSource`
- Execute CRUD operations safely using `PreparedStatement`
- Explain why SQL injection happens and prevent it with parameterized queries
- Iterate over query results with `ResultSet`
- Set up HikariCP connection pooling with appropriate pool sizing
- Wrap multiple SQL operations in a transaction with correct commit/rollback
- Set transaction isolation levels and explain the trade-offs
- Implement the Repository pattern to separate SQL from business logic
- Write JUnit 5 tests against an H2 in-memory database

---

## Setup

### Add H2 to your project

H2 is the only dependency needed to run every example in this module. Add it to the parent `pom.xml` in `learn-java/`:

```xml
<!-- In learn-java/pom.xml, inside <dependencies> -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
```

### Optional: HikariCP (for connection pooling examples)

```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
```

### No database server needed

All examples connect to H2 with an in-memory URL:

```
jdbc:h2:mem:mydb;DB_CLOSE_DELAY=-1
```

The database lives in JVM memory and is destroyed when the process exits.

---

## Module Structure

```
10-database/
├── theory/
│   ├── 01-jdbc-basics.md         ← JDBC API, PreparedStatement, CRUD
│   ├── 02-connection-pooling.md  ← HikariCP setup and pool sizing
│   ├── 03-transactions.md        ← ACID, commit/rollback, isolation levels
│   └── 04-best-practices.md      ← Repository pattern, security, ORM vs JDBC
├── code/
│   ├── JdbcBasicsDemo.java       ← Full CRUD demo with H2
│   ├── UserRepository.java       ← Repository pattern implementation
│   └── TransactionDemo.java      ← Bank transfer with transaction and rollback
├── exercises/
│   ├── Exercise01.java           ← Starter: ProductRepository (TODO stubs)
│   └── solutions/
│       └── Solution01.java       ← Complete solution
├── mini-project/
│   └── LibraryDatabase.java      ← Library system: books, members, loans
└── tests/
    └── JdbcTest.java             ← JUnit 5 tests using H2 in-memory
```

---

## What You'll Build

### Code Examples

**`JdbcBasicsDemo.java`** — A complete JDBC walkthrough:
- Connect to H2
- Create a `users` table
- INSERT with `PreparedStatement` and retrieve generated keys
- SELECT with `ResultSet` iteration
- UPDATE and DELETE with row-count verification
- SQL injection prevention demonstration

**`UserRepository.java`** — Repository pattern with:
- `findById(int id)` → `Optional<User>`
- `findAll()` → `List<User>`
- `findByNameContaining(String)` → `List<User>` (safe LIKE query)
- `save(User)` → `User` with generated id
- `update(User)` → `boolean`
- `delete(int id)` → `boolean`

**`TransactionDemo.java`** — Bank transfer with:
- `setAutoCommit(false)` / `commit()` / `rollback()`
- Balance check before debit
- Rollback demonstration when funds are insufficient
- Isolation level settings

### Exercise

`Exercise01.java` — Implement a `ProductRepository` from scratch with five CRUD methods. The record type and table DDL are provided; you write the SQL.

Check your work against `exercises/solutions/Solution01.java`.

### Mini-project

`LibraryDatabase.java` — A three-table library system:

| Method | What it does |
|---|---|
| `borrowBook(memberId, bookId)` | Checks availability, creates loan, marks book unavailable — all in one transaction |
| `returnBook(loanId)` | Sets return date, marks book available — atomic update |
| `findOverdueLoans()` | JOIN across three tables, date arithmetic in SQL |

### Tests

`JdbcTest.java` — Eight JUnit 5 tests:
- Insert and find
- Insert multiple and retrieve all
- Update (success and not-found)
- Delete (success and not-found)
- Transaction commit visible from new connection
- Transaction rollback on constraint violation
- SQL injection prevention
- Generated key retrieval

---

## Suggested Learning Path

1. Read `theory/01-jdbc-basics.md` — understand the core JDBC API
2. Run `code/JdbcBasicsDemo.java` — see it working
3. Read `theory/02-connection-pooling.md`
4. Read `code/UserRepository.java` — repository pattern in practice
5. Read `theory/03-transactions.md`
6. Run `code/TransactionDemo.java` — watch rollback happen
7. Read `theory/04-best-practices.md`
8. Work through `exercises/Exercise01.java` (attempt before looking at Solution01)
9. Explore `mini-project/LibraryDatabase.java`
10. Run `tests/JdbcTest.java` with Maven: `mvn test`

---

## Key Concepts Reference

| Concept | One-line summary |
|---|---|
| JDBC | Java's standard API for relational databases |
| `DriverManager` | Creates new connections — fine for demos, not production |
| `DataSource` | Standard interface for connection pools |
| `PreparedStatement` | Parameterized SQL — always use for user input |
| `ResultSet` | Cursor over query results; call `next()` to advance |
| `try-with-resources` | Auto-closes Connection/Statement/ResultSet |
| HikariCP | Fastest Java connection pool; default in Spring Boot |
| `setAutoCommit(false)` | Starts a manual transaction |
| `commit()` / `rollback()` | Makes changes permanent / undoes them |
| `READ_COMMITTED` | Most common isolation level — prevents dirty reads |
| Repository pattern | Keeps SQL out of business logic |

---

## Common Mistakes to Avoid

- **String concatenation into SQL** — use `?` placeholders, always
- **Forgetting try-with-resources** — connection leaks exhaust the pool
- **`SELECT *`** — name your columns explicitly
- **Hardcoding credentials** — use environment variables or config files
- **Ignoring `executeUpdate()` return value** — check that the expected row count changed
- **Long-running transactions** — keep DB transactions short; do non-DB work outside
