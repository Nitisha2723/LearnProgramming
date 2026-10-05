# Transactions

## What is a Transaction?

A transaction is a unit of work that must succeed or fail as a whole. Either all operations in the transaction succeed (commit), or none of them take effect (rollback).

**Classic example — bank transfer:**

```
Transfer $100 from Alice to Bob:
  1. Debit  Alice by $100
  2. Credit Bob   by $100
```

If step 1 succeeds but step 2 fails (network error, constraint violation, application crash), Alice has lost $100 and Bob received nothing. A transaction prevents this: both operations happen atomically, or neither does.

---

## ACID Properties

Databases guarantee ACID for transactions:

### Atomicity
All operations in a transaction succeed, or none do. There is no partial state.

### Consistency
A transaction moves the database from one valid state to another. All constraints (foreign keys, NOT NULL, CHECK) are satisfied after the commit.

### Isolation
Concurrent transactions behave as if they ran serially. One transaction cannot see the uncommitted changes of another (at most isolation levels).

### Durability
Once a transaction commits, the changes are permanent — even if the server crashes immediately afterward (they're in the write-ahead log).

---

## JDBC Transaction Control

By default, JDBC runs in **auto-commit mode**: every statement is immediately committed. You must turn this off to group operations into a transaction.

```java
try (Connection conn = dataSource.getConnection()) {

    conn.setAutoCommit(false);  // start manual transaction control

    try {
        // Step 1
        debitAccount(conn, fromId, amount);
        // Step 2
        creditAccount(conn, toId, amount);

        conn.commit();          // all succeeded — make permanent

    } catch (SQLException e) {
        conn.rollback();        // something failed — undo everything
        throw e;                // re-throw so the caller knows
    }
    // Note: setAutoCommit(true) is not strictly needed if you close the connection
}
```

**Correct pattern — always:**
1. `setAutoCommit(false)` at the start
2. Do all your work
3. `commit()` on success
4. `rollback()` in the catch block
5. Re-throw (or wrap) the exception

---

## Commit and Rollback

```java
conn.commit();    // writes all changes since the last commit/rollback
conn.rollback();  // undoes all changes since the last commit/rollback
```

After a `commit()` or `rollback()`, the connection stays open in manual-commit mode. You can start a new transaction immediately.

**Always rollback in finally or catch:**

```java
conn.setAutoCommit(false);
try {
    // ... operations ...
    conn.commit();
} catch (Exception e) {
    try {
        conn.rollback();
    } catch (SQLException rollbackEx) {
        // log this — can't do much else
        log.error("Rollback failed", rollbackEx);
    }
    throw e;
}
```

---

## Savepoints

Savepoints let you partially rollback within a transaction, rolling back to a known-good point without discarding all work.

```java
conn.setAutoCommit(false);

// Step 1 — do some work
insertOrderHeader(conn, order);

// Save progress so far
Savepoint afterHeader = conn.setSavepoint("after_header");

try {
    // Step 2 — risky operation
    insertOrderLines(conn, order.getLines());
    conn.commit();
} catch (SQLException e) {
    // Roll back only to after the header, not the whole transaction
    conn.rollback(afterHeader);
    // Could retry step 2 differently, or commit what we have
    conn.commit();
}
```

```java
conn.releaseSavepoint(afterHeader); // optional: release when no longer needed
```

Savepoints are useful in complex batch operations where you want to skip a failed record and continue rather than abort the entire batch.

---

## Transaction Isolation Levels

When multiple transactions run concurrently, isolation levels control what each transaction can see of other transactions' in-progress work.

### The Anomalies

**Dirty read** — reading uncommitted changes from another transaction that may later be rolled back.

**Non-repeatable read** — reading the same row twice in a transaction and getting different values because another transaction committed a change in between.

**Phantom read** — running the same query twice and getting a different number of rows because another transaction inserted or deleted rows in between.

### The Levels

| Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|---|---|---|---|
| READ_UNCOMMITTED | Possible | Possible | Possible |
| READ_COMMITTED | Prevented | Possible | Possible |
| REPEATABLE_READ | Prevented | Prevented | Possible |
| SERIALIZABLE | Prevented | Prevented | Prevented |

Higher isolation = fewer anomalies, but more locking = lower concurrency.

### Setting the isolation level

```java
conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
```

Constants:
```java
Connection.TRANSACTION_READ_UNCOMMITTED  // rarely used
Connection.TRANSACTION_READ_COMMITTED    // PostgreSQL, Oracle default
Connection.TRANSACTION_REPEATABLE_READ   // MySQL/InnoDB default
Connection.TRANSACTION_SERIALIZABLE      // safest, slowest
```

### Which level to choose?

| Use case | Recommended level |
|---|---|
| Reporting / analytics (read-only) | READ_COMMITTED |
| E-commerce orders, financial records | READ_COMMITTED or REPEATABLE_READ |
| Inventory check + update | REPEATABLE_READ |
| Critical financial transfers | SERIALIZABLE (or application-level locking) |

**READ_COMMITTED** is the most common choice: it prevents dirty reads (which are almost always bugs) while maintaining good concurrency. It is the default for PostgreSQL and Oracle.

---

## When to Use Transactions

Use a transaction whenever:

1. **Multiple related writes must succeed or fail together**
   - Creating an order with order lines
   - Transferring money between accounts

2. **A read-then-write sequence must be consistent**
   - Check inventory → reserve stock (race condition without a transaction)
   - Read account balance → check funds → debit

3. **Batch inserts where you want all-or-nothing semantics**
   - Importing a CSV file

Do **not** use long-running transactions:
- They hold locks and block other users
- Connection pool connections are tied up
- Keep transactions as short as possible

```java
// Anti-pattern: long transaction doing expensive non-DB work
conn.setAutoCommit(false);
List<Data> data = fetchFromExternalApi(); // may take 10 seconds!
insertData(conn, data);                   // connection held for 10+ seconds
conn.commit();

// Better: fetch first, then use a short transaction for the DB work
List<Data> data = fetchFromExternalApi(); // outside any transaction
try (Connection conn = ds.getConnection()) {
    conn.setAutoCommit(false);
    insertData(conn, data);               // fast DB-only work
    conn.commit();
}
```

---

## Practical Transaction Template

```java
public void transferFunds(int fromId, int toId, BigDecimal amount)
        throws SQLException {

    try (Connection conn = dataSource.getConnection()) {
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        try {
            // 1. Read current balances (to validate before writing)
            BigDecimal fromBalance = getBalance(conn, fromId);
            if (fromBalance.compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient funds");
            }

            // 2. Debit source
            debit(conn, fromId, amount);

            // 3. Credit destination
            credit(conn, toId, amount);

            // 4. Commit — both changes persist
            conn.commit();

        } catch (Exception e) {
            // Undo all changes
            conn.rollback();
            throw e;
        }
    }
}
```

---

## Summary

| Concept | Key point |
|---|---|
| ACID | Atomicity, Consistency, Isolation, Durability |
| `setAutoCommit(false)` | Starts manual transaction mode |
| `commit()` | Makes all changes permanent |
| `rollback()` | Undoes all changes back to last commit |
| Savepoints | Partial rollback within a transaction |
| READ_COMMITTED | Most common isolation level — prevents dirty reads |
| SERIALIZABLE | Strictest — prevents all anomalies but slowest |
| Short transactions | Hold connections briefly; do non-DB work outside the transaction |
