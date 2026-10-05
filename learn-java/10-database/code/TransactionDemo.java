import java.math.BigDecimal;
import java.sql.*;

/**
 * Demonstrates JDBC transactions using a simple bank account transfer.
 *
 * Key concepts covered:
 *  - setAutoCommit(false) to start a transaction
 *  - commit() on success
 *  - rollback() on failure
 *  - Transaction isolation levels
 *  - Verifying both operations inside one atomic unit
 */
public class TransactionDemo {

    private static final String URL  = "jdbc:h2:mem:bankdb;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void main(String[] args) throws SQLException {
        System.out.println("=== Transaction Demo: Bank Transfer ===\n");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // Setup
            createSchema(conn);
            int aliceId = createAccount(conn, "Alice", new BigDecimal("1000.00"));
            int bobId   = createAccount(conn, "Bob",   new BigDecimal("500.00"));

            printBalances(conn, "Initial balances:");

            // Successful transfer
            System.out.println("\n--- Transfer $200 from Alice to Bob (should succeed) ---");
            try {
                transferFunds(conn, aliceId, bobId, new BigDecimal("200.00"));
                System.out.println("Transfer succeeded.");
            } catch (Exception e) {
                System.out.println("Transfer failed: " + e.getMessage());
            }
            printBalances(conn, "After successful transfer:");

            // Transfer that exceeds balance — should rollback
            System.out.println("\n--- Transfer $9000 from Bob to Alice (should rollback) ---");
            try {
                transferFunds(conn, bobId, aliceId, new BigDecimal("9000.00"));
                System.out.println("Transfer succeeded.");
            } catch (Exception e) {
                System.out.println("Transfer failed (expected): " + e.getMessage());
            }
            printBalances(conn, "After failed transfer (balances unchanged):");

            // Demonstrate isolation level setting
            demonstrateIsolationLevel(conn);
        }
    }

    // -------------------------------------------------------------------------
    // Core transaction: transfer funds atomically
    // -------------------------------------------------------------------------

    /**
     * Transfers {@code amount} from account {@code fromId} to {@code toId}.
     *
     * Both the debit and the credit must succeed, or neither takes effect.
     *
     * @throws IllegalArgumentException if funds are insufficient
     * @throws SQLException             on any database error (triggers rollback)
     */
    static void transferFunds(Connection conn, int fromId, int toId, BigDecimal amount)
            throws SQLException {

        // Set isolation level before starting the transaction.
        // READ_COMMITTED: see only committed data; prevents dirty reads.
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

        // Disable auto-commit to begin a manual transaction.
        conn.setAutoCommit(false);

        try {
            // Step 1: Read the source balance (inside the transaction)
            BigDecimal fromBalance = getBalance(conn, fromId);
            System.out.println("  Current balance for id=" + fromId + ": " + fromBalance);

            // Step 2: Business rule — check sufficient funds
            if (fromBalance.compareTo(amount) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient funds: balance=" + fromBalance + ", requested=" + amount);
            }

            // Step 3: Debit source account
            debit(conn, fromId, amount);
            System.out.println("  Debited " + amount + " from id=" + fromId);

            // Uncomment to simulate a crash between debit and credit:
            // if (true) throw new RuntimeException("Simulated crash!");

            // Step 4: Credit destination account
            credit(conn, toId, amount);
            System.out.println("  Credited " + amount + " to id=" + toId);

            // Step 5: All operations succeeded — commit
            conn.commit();
            System.out.println("  Transaction committed.");

        } catch (Exception e) {
            // Something went wrong — undo all changes in this transaction
            System.out.println("  Rolling back transaction: " + e.getMessage());
            conn.rollback();
            throw e; // re-throw so the caller knows the transfer failed

        } finally {
            // Restore auto-commit mode for the connection (good practice when
            // reusing connections from a pool)
            conn.setAutoCommit(true);
        }
    }

    // -------------------------------------------------------------------------
    // Lower-level helpers — each does one SQL operation
    // -------------------------------------------------------------------------

    /**
     * Returns the current balance for the given account id.
     * This read happens inside the caller's transaction (shared connection).
     */
    static BigDecimal getBalance(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
                throw new SQLException("Account not found: id=" + accountId);
            }
        }
    }

    /**
     * Subtracts {@code amount} from the account balance.
     * Uses a WHERE clause that enforces the non-negative balance constraint at
     * the SQL level — an extra safety net.
     */
    static void debit(Connection conn, int accountId, BigDecimal amount)
            throws SQLException {
        String sql = """
                UPDATE accounts
                   SET balance = balance - ?
                 WHERE id = ?
                   AND balance >= ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, accountId);
            ps.setBigDecimal(3, amount);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException(
                        "Debit failed: account id=" + accountId
                        + " has insufficient balance for " + amount);
            }
        }
    }

    /**
     * Adds {@code amount} to the account balance.
     */
    static void credit(Connection conn, int accountId, BigDecimal amount)
            throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, accountId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Credit failed: account not found id=" + accountId);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Isolation level demonstration
    // -------------------------------------------------------------------------

    /**
     * Shows how to set different transaction isolation levels.
     * Includes a comment explaining what each level prevents.
     */
    static void demonstrateIsolationLevel(Connection conn) throws SQLException {
        System.out.println("\n--- Isolation Level Demo ---");

        // READ_COMMITTED (most common default — prevents dirty reads)
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        System.out.println("Set isolation: READ_COMMITTED");
        System.out.println("  Prevents: dirty reads");
        System.out.println("  Allows:   non-repeatable reads, phantom reads");

        // REPEATABLE_READ (MySQL InnoDB default — prevents non-repeatable reads)
        conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        System.out.println("Set isolation: REPEATABLE_READ");
        System.out.println("  Prevents: dirty reads, non-repeatable reads");
        System.out.println("  Allows:   phantom reads");

        // SERIALIZABLE (strictest — prevents all anomalies, but slowest)
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        System.out.println("Set isolation: SERIALIZABLE");
        System.out.println("  Prevents: dirty reads, non-repeatable reads, phantom reads");
        System.out.println("  Cost:     highest locking overhead");

        // Reset to the common default
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

    // -------------------------------------------------------------------------
    // Schema setup helpers
    // -------------------------------------------------------------------------

    static void createSchema(Connection conn) throws SQLException {
        String ddl = """
                CREATE TABLE IF NOT EXISTS accounts (
                    id      INTEGER PRIMARY KEY AUTO_INCREMENT,
                    name    VARCHAR(100)   NOT NULL,
                    balance DECIMAL(15, 2) NOT NULL CHECK (balance >= 0)
                )
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        }
    }

    static int createAccount(Connection conn, String name, BigDecimal initialBalance)
            throws SQLException {
        String sql = "INSERT INTO accounts (name, balance) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setBigDecimal(2, initialBalance);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    static void printBalances(Connection conn, String heading) throws SQLException {
        System.out.println("\n" + heading);
        String sql = "SELECT name, balance FROM accounts ORDER BY id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("  %-10s  $%s%n",
                        rs.getString("name"),
                        rs.getBigDecimal("balance").toPlainString());
            }
        }
    }
}
