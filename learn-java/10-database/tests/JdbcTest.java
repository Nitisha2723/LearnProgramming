import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for core JDBC operations, using H2 in-memory database.
 *
 * Each test runs against a fresh, empty schema so tests are fully independent.
 * No external database setup required.
 *
 * What is tested:
 *  - INSERT and SELECT round-trip
 *  - UPDATE behaviour (row changed, row count, non-existent row)
 *  - DELETE behaviour
 *  - Transaction commit and rollback
 *  - SQL injection prevention via PreparedStatement
 *
 * Add to pom.xml to compile and run:
 *   <dependency>
 *     <groupId>com.h2database</groupId>
 *     <artifactId>h2</artifactId>
 *     <version>2.2.224</version>
 *     <scope>test</scope>
 *   </dependency>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcTest {

    // Each test method gets its own connection from this URL.
    // Using a unique DB name per test class avoids cross-test contamination.
    private static final String URL  = "jdbc:h2:mem:jdbctest;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    /** Called once before any test in this class runs. Creates the schema. */
    @BeforeAll
    void createSchema() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt  = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id    INTEGER PRIMARY KEY AUTO_INCREMENT,
                        name  VARCHAR(100) NOT NULL,
                        email VARCHAR(255) NOT NULL UNIQUE
                    )
                    """);
        }
    }

    /** Called before each test — wipes all rows so tests don't interfere. */
    @BeforeEach
    void setUp() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt  = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
            // Reset auto-increment counter so ids are predictable across tests
            stmt.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        }
    }

    // =========================================================================
    // Helper methods — shared across tests
    // =========================================================================

    /** Inserts a user and returns the generated id. */
    private int insertUser(Connection conn, String name, String email)
            throws SQLException {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** Returns the email for the given user id, or null if not found. */
    private String getEmail(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT email FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("email") : null;
            }
        }
    }

    /** Counts rows in the users table. */
    private int countUsers(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // =========================================================================
    // Tests
    // =========================================================================

    @Test
    @DisplayName("Insert a user and retrieve it by id")
    void testInsertAndFind() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            // Insert
            int id = insertUser(conn, "Alice", "alice@example.com");

            // Verify the generated id is positive
            assertTrue(id > 0, "Generated id should be positive");

            // Find and verify all fields
            String sql = "SELECT id, name, email FROM users WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Should find the inserted user");
                    assertEquals(id,                   rs.getInt("id"));
                    assertEquals("Alice",              rs.getString("name"));
                    assertEquals("alice@example.com",  rs.getString("email"));
                    assertFalse(rs.next(), "Should only be one row");
                }
            }
        }
    }

    @Test
    @DisplayName("Insert multiple users and retrieve all")
    void testInsertMultipleAndFindAll() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            insertUser(conn, "Alice", "alice@example.com");
            insertUser(conn, "Bob",   "bob@example.com");
            insertUser(conn, "Carol", "carol@example.com");

            assertEquals(3, countUsers(conn), "Should have 3 users");

            // Verify order by id
            List<String> names = new java.util.ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs   = stmt.executeQuery(
                         "SELECT name FROM users ORDER BY id")) {
                while (rs.next()) names.add(rs.getString("name"));
            }
            assertEquals(List.of("Alice", "Bob", "Carol"), names);
        }
    }

    @Test
    @DisplayName("Update a user's email")
    void testUpdateUser() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            int id = insertUser(conn, "Alice", "alice@example.com");

            // Update
            String updateSql = "UPDATE users SET email = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, "alice@newdomain.com");
                ps.setInt(2, id);
                int rowsUpdated = ps.executeUpdate();
                assertEquals(1, rowsUpdated, "Should update exactly one row");
            }

            // Verify
            assertEquals("alice@newdomain.com", getEmail(conn, id),
                    "Email should be updated");
        }
    }

    @Test
    @DisplayName("Update returns 0 when user does not exist")
    void testUpdateNonExistentUser() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            String updateSql = "UPDATE users SET email = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, "ghost@example.com");
                ps.setInt(2, 9999); // does not exist
                int rowsUpdated = ps.executeUpdate();
                assertEquals(0, rowsUpdated,
                        "Should update 0 rows for non-existent id");
            }
        }
    }

    @Test
    @DisplayName("Delete a user removes exactly one row")
    void testDeleteUser() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            int aliceId = insertUser(conn, "Alice", "alice@example.com");
            int bobId   = insertUser(conn, "Bob",   "bob@example.com");

            assertEquals(2, countUsers(conn));

            // Delete Alice
            String deleteSql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, aliceId);
                int rowsDeleted = ps.executeUpdate();
                assertEquals(1, rowsDeleted, "Should delete exactly one row");
            }

            // Alice gone, Bob still there
            assertEquals(1, countUsers(conn), "Should have 1 user after delete");
            assertNull(getEmail(conn, aliceId), "Alice should be gone");
            assertNotNull(getEmail(conn, bobId), "Bob should still exist");
        }
    }

    @Test
    @DisplayName("Delete returns 0 when user does not exist")
    void testDeleteNonExistentUser() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            String deleteSql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, 9999);
                int rowsDeleted = ps.executeUpdate();
                assertEquals(0, rowsDeleted,
                        "Should delete 0 rows for non-existent id");
            }
        }
    }

    @Test
    @DisplayName("Rollback on exception leaves database unchanged")
    void testTransactionRollback() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            conn.setAutoCommit(false);

            try {
                // First insert — valid
                insertUser(conn, "Alice", "alice@example.com");

                assertEquals(1, countUsers(conn),
                        "Alice should be visible within the transaction");

                // Second insert — duplicate email causes a constraint violation
                // This should throw SQLException
                insertUser(conn, "Alice2", "alice@example.com"); // UNIQUE violation!

                conn.commit(); // should not reach here
                fail("Should have thrown SQLException for duplicate email");

            } catch (SQLException e) {
                // Expected — roll back the entire transaction
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }

            // After rollback, the table should still be empty
            assertEquals(0, countUsers(conn),
                    "Rollback should have removed both inserts");
        }
    }

    @Test
    @DisplayName("Commit makes changes permanent across connections")
    void testTransactionCommit() throws SQLException {
        int insertedId;

        // Connection 1: insert and commit
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            conn.setAutoCommit(false);
            insertedId = insertUser(conn, "Alice", "alice@example.com");
            conn.commit();
        }

        // Connection 2: verify the committed data is visible
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String email = getEmail(conn, insertedId);
            assertEquals("alice@example.com", email,
                    "Committed data should be visible from a new connection");
        }
    }

    @Test
    @DisplayName("PreparedStatement prevents SQL injection")
    void testPreparedStatementPreventsInjection() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            // Seed one legitimate user
            insertUser(conn, "Alice", "alice@example.com");

            // Classic SQL injection attempt — should return ALL rows if vulnerable
            String injectionPayload = "' OR '1'='1";

            String sql = "SELECT id, name, email FROM users WHERE name = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, injectionPayload);
                try (ResultSet rs = ps.executeQuery()) {
                    // If SQL injection succeeded, rs.next() would return true
                    // (it would return Alice's row). With PreparedStatement it
                    // safely returns no rows.
                    assertFalse(rs.next(),
                            "Injection payload should match no rows — "
                            + "PreparedStatement treats it as literal data");
                }
            }

            // Verify the legitimate query still works
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "Alice");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Legitimate query should find Alice");
                    assertEquals("alice@example.com", rs.getString("email"));
                }
            }
        }
    }

    @Test
    @DisplayName("Generated key is correctly returned after insert")
    void testGeneratedKey() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            int id1 = insertUser(conn, "Alice", "alice@example.com");
            int id2 = insertUser(conn, "Bob",   "bob@example.com");

            assertTrue(id1 > 0, "First id should be positive");
            assertTrue(id2 > id1, "Second id should be greater than first");
        }
    }

    @Test
    @DisplayName("Try-with-resources closes connection automatically")
    void testTryWithResourcesClosesConnection() throws SQLException {
        Connection conn;

        try (Connection c = DriverManager.getConnection(URL, USER, PASS)) {
            conn = c;
            assertFalse(conn.isClosed(), "Connection should be open inside try block");
        }

        // After the try block, the connection must be closed
        assertTrue(conn.isClosed(),
                "Connection should be closed after try-with-resources exits");
    }
}
