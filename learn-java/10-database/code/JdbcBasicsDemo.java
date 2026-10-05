import java.sql.*;

/**
 * Demonstrates core JDBC operations using H2 in-memory database.
 *
 * No external database setup needed — H2 runs entirely in the JVM.
 * Add to pom.xml: com.h2database:h2:2.2.224
 *
 * Covers:
 *  - Connecting via DriverManager
 *  - DDL (CREATE TABLE)
 *  - INSERT with PreparedStatement and generated keys
 *  - SELECT with ResultSet iteration
 *  - UPDATE
 *  - DELETE
 *  - Proper try-with-resources throughout
 */
public class JdbcBasicsDemo {

    // DB_CLOSE_DELAY=-1 keeps the in-memory DB alive for the JVM lifetime.
    // Without it the DB drops as soon as the first connection closes.
    private static final String URL  = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void main(String[] args) throws SQLException {
        System.out.println("=== JDBC Basics Demo ===\n");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            // 1. Create the table
            createTable(conn);

            // 2. Insert some users
            System.out.println("-- Inserting users --");
            int aliceId = insertUser(conn, "Alice",   "alice@example.com");
            int bobId   = insertUser(conn, "Bob",     "bob@example.com");
            int carolId = insertUser(conn, "Carol",   "carol@example.com");
            System.out.println("Inserted users with ids: " + aliceId + ", " + bobId + ", " + carolId);

            // 3. List all users
            System.out.println("\n-- All users after insert --");
            listUsers(conn);

            // 4. Update Alice's email
            System.out.println("\n-- Updating Alice's email --");
            updateEmail(conn, aliceId, "alice@newdomain.com");
            System.out.println("Updated email for user id=" + aliceId);

            // 5. Delete Bob
            System.out.println("\n-- Deleting Bob --");
            deleteUser(conn, bobId);
            System.out.println("Deleted user id=" + bobId);

            // 6. Find a specific user by id
            System.out.println("\n-- Find user by id=" + aliceId + " --");
            findUserById(conn, aliceId);

            // 7. Final list
            System.out.println("\n-- All users after update and delete --");
            listUsers(conn);

            // 8. Demonstrate SQL injection prevention
            System.out.println("\n-- SQL injection prevention demo --");
            demonstrateSqlInjectionPrevention(conn);
        }
    }

    /**
     * Creates the users table if it does not already exist.
     * Uses execute() (not executeUpdate()) for DDL statements.
     */
    static void createTable(Connection conn) throws SQLException {
        String ddl = """
                CREATE TABLE IF NOT EXISTS users (
                    id    INTEGER PRIMARY KEY AUTO_INCREMENT,
                    name  VARCHAR(100) NOT NULL,
                    email VARCHAR(255) NOT NULL UNIQUE
                )
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
            System.out.println("Table 'users' created (or already exists).");
        }
    }

    /**
     * Inserts a new user and returns the auto-generated primary key.
     *
     * Key points:
     *  - Always use PreparedStatement for INSERT with user-supplied values.
     *  - RETURN_GENERATED_KEYS tells JDBC to capture the auto-increment id.
     *  - getGeneratedKeys() returns a ResultSet; read it with next() + getLong(1).
     */
    static int insertUser(Connection conn, String name, String email)
            throws SQLException {

        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";

        // Statement.RETURN_GENERATED_KEYS — captures the auto-increment id
        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted != 1) {
                throw new SQLException("Insert failed — " + rowsInserted + " rows affected");
            }

            // Retrieve the generated primary key
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Insert succeeded but no generated key returned");
            }
        }
    }

    /**
     * Retrieves all users and prints them to stdout.
     *
     * Uses a plain Statement here because there are no parameters.
     * ResultSet columns accessed by name (more readable than index).
     */
    static void listUsers(Connection conn) throws SQLException {
        String sql = "SELECT id, name, email FROM users ORDER BY id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                System.out.printf("  id=%-3d  name=%-15s  email=%s%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"));
                count++;
            }
            if (count == 0) {
                System.out.println("  (no users)");
            }
        }
    }

    /**
     * Finds a single user by primary key and prints the result.
     * Demonstrates the typical "find one or not found" pattern.
     */
    static void findUserById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, name, email FROM users WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("  Found: id=%d, name=%s, email=%s%n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"));
                } else {
                    System.out.println("  Not found: id=" + id);
                }
            }
        }
    }

    /**
     * Updates the email address for the user with the given id.
     * executeUpdate() returns the number of affected rows — verify it!
     */
    static void updateEmail(Connection conn, int id, String newEmail)
            throws SQLException {

        String sql = "UPDATE users SET email = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newEmail);
            ps.setInt(2, id);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                System.out.println("  Warning: no user found with id=" + id);
            }
        }
    }

    /**
     * Deletes the user with the given id.
     * Returns true if a row was deleted, false if the user was not found.
     */
    static boolean deleteUser(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Shows that PreparedStatement safely handles input that would be a
     * SQL injection attack if concatenated into a query string.
     *
     * The malicious input is treated as literal data — it cannot alter the
     * query structure.
     */
    static void demonstrateSqlInjectionPrevention(Connection conn) throws SQLException {
        // Classic SQL injection payload — attempts to return all users
        String maliciousInput = "' OR '1'='1";

        System.out.println("  Searching for user with name: " + maliciousInput);

        // SAFE: PreparedStatement sends the SQL structure and data separately
        String sql = "SELECT id, name, email FROM users WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maliciousInput);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("  Found (should not happen): " + rs.getString("name"));
                } else {
                    System.out.println("  No results — injection attempt safely neutralized!");
                }
            }
        }

        // For contrast, show what the UNSAFE concatenated query would look like:
        String unsafe = "SELECT * FROM users WHERE name = '" + maliciousInput + "'";
        System.out.println("  Unsafe SQL would be: " + unsafe);
        System.out.println("  (That would return ALL rows — never do this!)");
    }
}
