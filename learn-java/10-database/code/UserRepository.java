import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Repository pattern implementation for User persistence.
 *
 * The repository pattern encapsulates all database access for a single
 * aggregate (User) behind a clean interface. Business logic never sees SQL.
 *
 * Design decisions:
 *  - Takes DataSource (not Connection) — each method gets its own connection
 *    from the pool and returns it when done.
 *  - SQLExceptions are wrapped in RuntimeException — callers do not need to
 *    handle checked SQL exceptions (a common convention).
 *  - Uses Optional<User> for queries that may return zero rows.
 *  - Retrieves generated keys after INSERT.
 */
public class UserRepository {

    /** Immutable User data record. */
    public record User(int id, String name, String email) {

        /** Convenience factory for creating a new (unsaved) user without an id. */
        public static User of(String name, String email) {
            return new User(0, name, email);
        }
    }

    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // -------------------------------------------------------------------------
    // Schema management (useful for tests and in-memory databases)
    // -------------------------------------------------------------------------

    /**
     * Creates the users table. Call once at startup for H2 in-memory databases.
     */
    public void createTable() {
        String ddl = """
                CREATE TABLE IF NOT EXISTS users (
                    id    INTEGER PRIMARY KEY AUTO_INCREMENT,
                    name  VARCHAR(100) NOT NULL,
                    email VARCHAR(255) NOT NULL UNIQUE
                )
                """;
        try (Connection conn = dataSource.getConnection();
             Statement stmt  = conn.createStatement()) {
            stmt.execute(ddl);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create users table", e);
        }
    }

    // -------------------------------------------------------------------------
    // CRUD operations
    // -------------------------------------------------------------------------

    /**
     * Finds a user by primary key.
     *
     * @param id the primary key
     * @return Optional containing the user, or Optional.empty() if not found
     */
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

    /**
     * Returns all users, ordered by id.
     *
     * @return list of all users (empty list if none)
     */
    public List<User> findAll() {
        String sql = "SELECT id, name, email FROM users ORDER BY id";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("findAll failed", e);
        }

        return users;
    }

    /**
     * Finds users whose name contains the given substring (case-insensitive).
     *
     * Demonstrates LIKE with a PreparedStatement — the % wildcards are
     * appended in Java, not embedded in the user input.
     *
     * @param namePart substring to search for
     * @return matching users
     */
    public List<User> findByNameContaining(String namePart) {
        String sql = "SELECT id, name, email FROM users WHERE LOWER(name) LIKE ?";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Append % wildcards here — user input goes through setString, not SQL
            ps.setString(1, "%" + namePart.toLowerCase() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("findByNameContaining failed for: " + namePart, e);
        }

        return users;
    }

    /**
     * Inserts a new user and returns the saved user with its generated id.
     *
     * @param user user to save (id field is ignored)
     * @return the saved user with its auto-generated id
     * @throws RuntimeException wrapping SQLException on failure
     */
    public User save(User user) {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.name());
            ps.setString(2, user.email());

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted != 1) {
                throw new RuntimeException("Expected 1 row inserted, got " + rowsInserted);
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    return new User(generatedId, user.name(), user.email());
                }
                throw new RuntimeException("No generated key returned after insert");
            }

        } catch (SQLException e) {
            throw new RuntimeException("save failed for user: " + user.name(), e);
        }
    }

    /**
     * Updates the name and email of an existing user.
     *
     * @param user user with updated fields (id must be set)
     * @return true if a row was updated, false if the user was not found
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.name());
            ps.setString(2, user.email());
            ps.setInt(3, user.id());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("update failed for id=" + user.id(), e);
        }
    }

    /**
     * Deletes the user with the given id.
     *
     * @param id primary key of the user to delete
     * @return true if a row was deleted, false if the user was not found
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("delete failed for id=" + id, e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Maps the current ResultSet row to a User record.
     * Centralising column names here avoids repeating them in each method.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email")
        );
    }

    // -------------------------------------------------------------------------
    // Quick demonstration main
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        // Use H2 in-memory — no external database needed
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:repotest;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");

        UserRepository repo = new UserRepository(ds);
        repo.createTable();

        System.out.println("=== UserRepository Demo ===\n");

        // Save
        User alice = repo.save(User.of("Alice", "alice@example.com"));
        User bob   = repo.save(User.of("Bob",   "bob@example.com"));
        User carol = repo.save(User.of("Carol", "carol@example.com"));
        System.out.println("Saved: " + alice);
        System.out.println("Saved: " + bob);
        System.out.println("Saved: " + carol);

        // FindAll
        System.out.println("\nAll users: " + repo.findAll());

        // FindById
        System.out.println("\nFind alice (id=" + alice.id() + "): "
                + repo.findById(alice.id()));
        System.out.println("Find missing (id=999): " + repo.findById(999));

        // FindByNameContaining
        System.out.println("\nSearch 'li': " + repo.findByNameContaining("li"));

        // Update
        User updatedAlice = new User(alice.id(), "Alice Smith", "alice@newdomain.com");
        System.out.println("\nUpdate alice: " + repo.update(updatedAlice));
        System.out.println("After update: " + repo.findById(alice.id()));

        // Delete
        System.out.println("\nDelete bob (id=" + bob.id() + "): " + repo.delete(bob.id()));
        System.out.println("Delete missing: " + repo.delete(999));

        System.out.println("\nFinal users: " + repo.findAll());
    }
}
