package patterns.repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * DESIGN PATTERN: Repository
 * ==========================
 * Intent: Mediates between the domain and data mapping layers using a
 * collection-like interface for accessing domain objects. The Repository
 * abstracts the data storage mechanism from the business logic.
 *
 * REAL-WORLD USE CASE: User Account Management
 * ---------------------------------------------
 * An application manages user accounts. The business logic (UserService)
 * needs to find, create, update, and delete users. But HOW users are stored
 * — in memory, in a relational database, in MongoDB — should NOT be the
 * concern of the business logic.
 *
 * ============================================================
 * WHY REPOSITORY PATTERN?
 * ============================================================
 *
 * WITHOUT Repository:
 *   class UserService {
 *       private final DataSource ds; // tightly coupled to JDBC
 *
 *       public User findById(Long id) {
 *           try (Connection c = ds.getConnection()) {
 *               PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE id=?");
 *               ps.setLong(1, id);
 *               ResultSet rs = ps.executeQuery();
 *               // ... mapping code ...
 *           }
 *       }
 *   }
 *   Problems:
 *     - UserService cannot be tested without a real database
 *     - Switching databases (MySQL → PostgreSQL) requires changing UserService
 *     - SQL scattered throughout business logic
 *     - Performance (N+1 queries) harder to see when mixed with logic
 *
 * WITH Repository:
 *   class UserService {
 *       private final UserRepository repo; // depends on interface!
 *
 *       public User findById(Long id) {
 *           return repo.findById(id).orElseThrow();  // clean!
 *       }
 *   }
 *   Benefits:
 *     - Swap InMemoryUserRepository in tests → no database required
 *     - Swap JdbcUserRepository for production → UserService unchanged
 *     - Swap for MongoUserRepository → UserService unchanged
 *     - Data access logic centralized and testable in isolation
 *
 * ============================================================
 * REPOSITORY vs DAO (Data Access Object):
 * ============================================================
 *   DAO:        Closer to the database. Often has one method per query.
 *               findUsersByActiveFlagAndRole(...) — very specific.
 *   Repository: Closer to the domain model. Collection-like semantics.
 *               findAll(Specification<User>) — more flexible.
 *   In practice: These terms are often used interchangeably. Spring Data
 *   calls its interfaces "Repositories" but they're essentially DAOs.
 *
 * ============================================================
 * DEPENDENCY INVERSION PRINCIPLE (DIP):
 * ============================================================
 * UserService depends on UserRepository (abstract interface).
 * InMemoryUserRepository and JdbcUserRepository depend on UserRepository too.
 * Nothing depends on concrete implementation classes — only the interface.
 * This is the Dependency Inversion Principle in action.
 *
 *   High-level:  UserService
 *                    ↓ depends on
 *   Abstraction: UserRepository (interface)
 *                    ↑ implemented by
 *   Low-level:   InMemoryUserRepository / JdbcUserRepository
 */

// =============================================================================
// FILE STRUCTURE:
//   1. User class                         — Domain entity
//   2. UserRepository interface           — Repository contract
//   3. InMemoryUserRepository             — Implementation for testing/dev
//   4. JdbcUserRepository                 — Stub showing JDBC implementation
//   5. UserService                        — Business logic (depends on interface)
//   6. UserRepository (public class)      — Demo runner with main()
// =============================================================================

// -----------------------------------------------------------------------------
// DOMAIN ENTITY: User
// -----------------------------------------------------------------------------
/**
 * User — the domain entity managed by the repository.
 *
 * Domain entities should be focused on business concepts, not storage details.
 * Notice: no @Column, no @Table, no database annotations here.
 * The repository is responsible for the mapping, not the entity.
 */
class User {
    private final Long id;           // Assigned by repository (null for new users)
    private String username;
    private String email;
    private final LocalDateTime createdAt;
    private boolean active;

    /** Constructor for creating new users (id assigned by repository) */
    public User(String username, String email) {
        this.id = null; // No ID yet — repository assigns one on save()
        this.username = username;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    /** Constructor for reconstituting users from storage (with existing id) */
    public User(Long id, String username, String email, LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.active = active;
    }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }

    // Domain methods — behavior belongs on the entity
    public void deactivate() { this.active = false; }
    public void updateEmail(String newEmail) {
        if (newEmail == null || newEmail.isBlank()) throw new IllegalArgumentException("Email cannot be blank");
        this.email = newEmail;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', email='%s', active=%s, created=%s}",
                id, username, email, active, createdAt.toLocalDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}

// -----------------------------------------------------------------------------
// REPOSITORY INTERFACE: the collection-like contract for data access
// -----------------------------------------------------------------------------
/**
 * UserRepository — defines what data operations are possible on Users.
 *
 * DESIGN DECISIONS:
 *   - findById returns Optional<User>: forces callers to handle "not found"
 *     Instead of returning null (which causes NullPointerExceptions)
 *   - save() for both insert AND update (upsert semantics)
 *   - No persistence-specific details: no SQL, no MongoDB operators
 *   - Could extend to add pagination: findAll(int page, int size)
 *   - Could add Specification<User> for complex queries
 *
 * This interface is stable: changes to how data is stored don't change this.
 * Adding a new storage backend = implement this interface, nothing else.
 */
interface UserRepositoryPort {
    /**
     * Find a user by their unique ID.
     * @return Optional.empty() if no user with that ID exists
     */
    Optional<User> findById(Long id);

    /**
     * Find a user by email address. Email is unique per user.
     * @return Optional.empty() if no user with that email exists
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by username. Username is unique per user.
     * @return Optional.empty() if no user with that username exists
     */
    Optional<User> findByUsername(String username);

    /**
     * Return all users in the system.
     * For large datasets, this would be findAll(Pageable pageable).
     */
    List<User> findAll();

    /**
     * Return all active users.
     */
    List<User> findAllActive();

    /**
     * Save a new user or update an existing one.
     * If user.getId() is null → INSERT, assigns and returns a new ID.
     * If user.getId() is not null → UPDATE the existing record.
     *
     * @return The saved user (with ID assigned for new users)
     */
    User save(User user);

    /**
     * Delete a user by ID.
     * @return true if the user was found and deleted, false if not found
     */
    boolean delete(Long id);

    /**
     * Count total number of users.
     */
    long count();
}

// -----------------------------------------------------------------------------
// IMPLEMENTATION 1: In-Memory (for testing and development)
// -----------------------------------------------------------------------------
/**
 * InMemoryUserRepository — stores users in a HashMap.
 *
 * This implementation is ideal for:
 *   1. Unit testing UserService without needing a database
 *   2. Development before the database schema is ready
 *   3. Performance-sensitive scenarios where a DB is overkill
 *
 * THREAD SAFETY NOTE: The HashMap here is not thread-safe. A production
 * in-memory implementation would use ConcurrentHashMap or synchronization.
 * This is simplified for clarity.
 */
class InMemoryUserRepository implements UserRepositoryPort {
    // Primary store: id → User
    private final Map<Long, User> store = new HashMap<>();

    // Auto-incrementing ID generator (thread-safe)
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // Linear scan — in production, you'd maintain a secondary index: Map<String, Long>
        return store.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        // Return a copy to prevent external modification of our internal list
        return new ArrayList<>(store.values());
    }

    @Override
    public List<User> findAllActive() {
        return store.values().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            // INSERT: assign new ID and store
            long newId = idSequence.getAndIncrement();
            // Reconstruct with assigned ID (User is quasi-immutable — id is final)
            User savedUser = new User(newId, user.getUsername(), user.getEmail(),
                    user.getCreatedAt(), user.isActive());
            store.put(newId, savedUser);
            System.out.println("  [InMemoryRepo] Inserted: " + savedUser);
            return savedUser;
        } else {
            // UPDATE: overwrite existing record
            store.put(user.getId(), user);
            System.out.println("  [InMemoryRepo] Updated: " + user);
            return user;
        }
    }

    @Override
    public boolean delete(Long id) {
        User removed = store.remove(id);
        if (removed != null) {
            System.out.println("  [InMemoryRepo] Deleted user id=" + id);
            return true;
        }
        return false;
    }

    @Override
    public long count() {
        return store.size();
    }
}

// -----------------------------------------------------------------------------
// IMPLEMENTATION 2: JDBC stub (shows what a real DB implementation looks like)
// -----------------------------------------------------------------------------
/**
 * JdbcUserRepository — what a real database-backed implementation looks like.
 *
 * THIS IS A STUB. It shows the structure and SQL without requiring an actual
 * database connection. In a real application, this would use java.sql.DataSource
 * or Spring's JdbcTemplate.
 *
 * KEY INSIGHT: UserService doesn't know or care which implementation it uses.
 * Swapping InMemoryUserRepository for JdbcUserRepository requires changing
 * ONE LINE in main() (or a dependency injection configuration).
 */
class JdbcUserRepository implements UserRepositoryPort {

    /*
     * In a real implementation, this class would have:
     *
     *   private final DataSource dataSource;  // injected via constructor
     *
     *   public JdbcUserRepository(DataSource dataSource) {
     *       this.dataSource = dataSource;
     *   }
     *
     * And each method would open a connection, prepare a statement, execute it,
     * map the ResultSet to User objects, and close the connection.
     *
     * DATABASE SCHEMA (for reference):
     *
     *   CREATE TABLE users (
     *       id         BIGINT PRIMARY KEY AUTO_INCREMENT,
     *       username   VARCHAR(50) UNIQUE NOT NULL,
     *       email      VARCHAR(255) UNIQUE NOT NULL,
     *       active     BOOLEAN NOT NULL DEFAULT TRUE,
     *       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
     *   );
     *
     *   CREATE INDEX idx_users_email    ON users(email);
     *   CREATE INDEX idx_users_username ON users(username);
     */

    @Override
    public Optional<User> findById(Long id) {
        /*
         * String sql = "SELECT id, username, email, created_at, active FROM users WHERE id = ?";
         * try (Connection conn = dataSource.getConnection();
         *      PreparedStatement ps = conn.prepareStatement(sql)) {
         *     ps.setLong(1, id);
         *     try (ResultSet rs = ps.executeQuery()) {
         *         if (rs.next()) {
         *             return Optional.of(mapRowToUser(rs));
         *         }
         *         return Optional.empty();
         *     }
         * } catch (SQLException e) {
         *     throw new RuntimeException("Failed to find user by id: " + id, e);
         * }
         */
        System.out.println("  [JdbcRepo] Would execute: SELECT * FROM users WHERE id = " + id);
        return Optional.empty(); // stub — no real DB
    }

    @Override
    public Optional<User> findByEmail(String email) {
        /*
         * String sql = "SELECT id, username, email, created_at, active FROM users WHERE email = ?";
         * // ... same pattern as findById but with email ...
         */
        System.out.println("  [JdbcRepo] Would execute: SELECT * FROM users WHERE email = '" + email + "'");
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        System.out.println("  [JdbcRepo] Would execute: SELECT * FROM users WHERE username = '" + username + "'");
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        System.out.println("  [JdbcRepo] Would execute: SELECT * FROM users ORDER BY created_at");
        return new ArrayList<>();
    }

    @Override
    public List<User> findAllActive() {
        System.out.println("  [JdbcRepo] Would execute: SELECT * FROM users WHERE active = TRUE");
        return new ArrayList<>();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            /*
             * String sql = "INSERT INTO users (username, email, active, created_at) VALUES (?,?,?,?)";
             * try (Connection conn = dataSource.getConnection();
             *      PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
             *     ps.setString(1, user.getUsername());
             *     ps.setString(2, user.getEmail());
             *     ps.setBoolean(3, user.isActive());
             *     ps.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt()));
             *     ps.executeUpdate();
             *     try (ResultSet keys = ps.getGeneratedKeys()) {
             *         keys.next();
             *         long generatedId = keys.getLong(1);
             *         return new User(generatedId, user.getUsername(), user.getEmail(),
             *                         user.getCreatedAt(), user.isActive());
             *     }
             * } catch (SQLException e) {
             *     throw new RuntimeException("Failed to insert user", e);
             * }
             */
            System.out.println("  [JdbcRepo] Would execute: INSERT INTO users (username, email, ...) VALUES (...)");
        } else {
            System.out.println("  [JdbcRepo] Would execute: UPDATE users SET username=?, email=?, active=? WHERE id=" + user.getId());
        }
        return user; // stub
    }

    @Override
    public boolean delete(Long id) {
        System.out.println("  [JdbcRepo] Would execute: DELETE FROM users WHERE id = " + id);
        return true; // stub
    }

    @Override
    public long count() {
        System.out.println("  [JdbcRepo] Would execute: SELECT COUNT(*) FROM users");
        return 0; // stub
    }

    /*
     * HELPER: maps a ResultSet row to a User object
     * (the Object-Relational Mapping that lives in the Repository)
     *
     * private User mapRowToUser(ResultSet rs) throws SQLException {
     *     return new User(
     *         rs.getLong("id"),
     *         rs.getString("username"),
     *         rs.getString("email"),
     *         rs.getTimestamp("created_at").toLocalDateTime(),
     *         rs.getBoolean("active")
     *     );
     * }
     */
}

// -----------------------------------------------------------------------------
// SERVICE LAYER: business logic that depends ONLY on the interface
// -----------------------------------------------------------------------------
/**
 * UserService — contains business logic for user management.
 *
 * DEPENDENCY INJECTION: UserService accepts a UserRepository in its constructor.
 * It doesn't create the repository — it just uses whatever is injected.
 *
 * This means:
 *   - In tests: inject InMemoryUserRepository (fast, no DB needed)
 *   - In production: inject JdbcUserRepository (real database)
 *   - In the future: inject MongoUserRepository, RedisUserRepository, etc.
 *
 * UserService NEVER imports InMemoryUserRepository or JdbcUserRepository.
 * This is the Dependency Inversion Principle: depend on abstractions.
 */
class UserService {
    // Depends on the INTERFACE, not any concrete class
    private final UserRepositoryPort userRepository;

    /**
     * Constructor injection — the repository is provided by the caller.
     * This is the standard way to inject dependencies.
     *
     * @param userRepository The repository to use for data access
     */
    public UserService(UserRepositoryPort userRepository) {
        if (userRepository == null) throw new IllegalArgumentException("Repository cannot be null");
        this.userRepository = userRepository;
    }

    /**
     * Register a new user. Business rules:
     *   - Username and email must be unique
     *   - Email must contain '@'
     *   - Username must be at least 3 characters
     */
    public User registerUser(String username, String email) {
        // Business rule validation
        if (username == null || username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address: " + email);
        }

        // Uniqueness checks — uses repository to check, but doesn't care HOW it checks
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Username already taken: " + username);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already registered: " + email);
        }

        // Create and save
        User newUser = new User(username, email);
        return userRepository.save(newUser);
    }

    /**
     * Get a user by ID. Throws if not found (business rule: user must exist).
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No user found with id: " + id));
    }

    /**
     * Update a user's email address.
     */
    public User updateEmail(Long userId, String newEmail) {
        User user = getUserById(userId);

        // Check email uniqueness before changing
        Optional<User> existing = userRepository.findByEmail(newEmail);
        if (existing.isPresent() && !existing.get().getId().equals(userId)) {
            throw new IllegalStateException("Email already in use by another user: " + newEmail);
        }

        user.updateEmail(newEmail);
        return userRepository.save(user);
    }

    /**
     * Deactivate (soft-delete) a user account.
     */
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.deactivate();
        userRepository.save(user);
        System.out.println("  [UserService] Deactivated user: " + user.getUsername());
    }

    /** Get all active users */
    public List<User> getActiveUsers() {
        return userRepository.findAllActive();
    }

    /** Get total user count */
    public long getTotalUserCount() {
        return userRepository.count();
    }
}

// =============================================================================
// DEMO RUNNER
// =============================================================================
/**
 * Demonstrates the Repository pattern by showing how UserService works
 * identically with InMemoryUserRepository and JdbcUserRepository.
 */
public class UserRepository {

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("DESIGN PATTERN: Repository");
        System.out.println("USE CASE: User Account Management");
        System.out.println("=".repeat(65) + "\n");

        // -------------------------------------------------------
        // DEMO 1: UserService with InMemoryUserRepository
        // -------------------------------------------------------
        System.out.println("--- DEMO 1: UserService with InMemoryUserRepository ---");
        System.out.println("(This is how you'd use it in tests — no database needed)\n");

        // THE KEY LINE: inject InMemoryUserRepository
        UserService userService = new UserService(new InMemoryUserRepository());

        // Register some users
        System.out.println("Registering users:");
        User alice = userService.registerUser("alice", "alice@example.com");
        User bob = userService.registerUser("bob", "bob@example.com");
        User charlie = userService.registerUser("charlie", "charlie@example.com");

        System.out.println("\nRegistered: " + alice);
        System.out.println("Registered: " + bob);
        System.out.println("Registered: " + charlie);

        // -------------------------------------------------------
        // DEMO 2: Lookup operations
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 2: Lookup Operations ---");

        User found = userService.getUserById(alice.getId());
        System.out.println("Found by ID: " + found);

        // -------------------------------------------------------
        // DEMO 3: Update email
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 3: Update Email ---");
        User updated = userService.updateEmail(alice.getId(), "alice.new@example.com");
        System.out.println("Updated: " + updated);

        // -------------------------------------------------------
        // DEMO 4: Business rule enforcement
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 4: Business Rule Enforcement ---");

        // Duplicate username
        try {
            userService.registerUser("alice", "different@example.com");
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // Invalid email
        try {
            userService.registerUser("dave", "not-an-email");
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // Not found
        try {
            userService.getUserById(999L);
        } catch (NoSuchElementException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // -------------------------------------------------------
        // DEMO 5: Deactivation
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 5: Deactivation ---");
        userService.deactivateUser(bob.getId());
        System.out.println("Active users after deactivating Bob:");
        userService.getActiveUsers().forEach(u ->
                System.out.println("  " + u.getUsername() + " (active=" + u.isActive() + ")"));

        // -------------------------------------------------------
        // DEMO 6: THE KEY DEMO — swap to JdbcUserRepository with ZERO UserService changes
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 6: Swapping to JdbcUserRepository ---");
        System.out.println("UserService is IDENTICAL — only the constructor argument changes.\n");

        // Swap the implementation — ONE LINE CHANGE
        UserService productionService = new UserService(new JdbcUserRepository());

        // The SAME method calls work — UserService has no idea about the swap
        System.out.println("Calling getUserById on JDBC implementation:");
        productionService.getUserById(1L); // Will call JdbcUserRepository.findById()

        System.out.println("\nCalling getActiveUsers on JDBC implementation:");
        productionService.getActiveUsers(); // Will call JdbcUserRepository.findAllActive()

        System.out.println("\n--- Repository Pattern Summary ---");
        System.out.println("UserService never imports InMemoryUserRepository or JdbcUserRepository");
        System.out.println("Tests use InMemory → no database setup, tests run in milliseconds");
        System.out.println("Production uses JDBC → real SQL, same UserService code");
        System.out.println("Total users in demo: " + userService.getTotalUserCount());
        System.out.println("\nDone!");
    }
}
