import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mini-project: Library Management System
 *
 * A small but realistic library application demonstrating:
 *  - Multiple related tables (books, members, loans)
 *  - Transactions for multi-step operations
 *  - Business rule enforcement (availability check, overdue detection)
 *  - Repository-style data access
 *
 * Runs entirely with H2 in-memory — no external database needed.
 *
 * Tables:
 *   books   (id, title, author, isbn, available)
 *   members (id, name, email)
 *   loans   (id, book_id, member_id, loan_date, return_date)
 */
public class LibraryDatabase {

    // =========================================================================
    // Domain records
    // =========================================================================

    public record Book(int id, String title, String author, String isbn, boolean available) {}

    public record Member(int id, String name, String email) {}

    public record Loan(int id, int bookId, int memberId,
                       LocalDate loanDate, LocalDate returnDate) {
        public boolean isReturned() { return returnDate != null; }
        public boolean isOverdue()  {
            return !isReturned() &&
                   ChronoUnit.DAYS.between(loanDate, LocalDate.now()) > 14;
        }
    }

    public record OverdueLoan(
            int loanId,
            String bookTitle,
            String memberName,
            LocalDate loanDate,
            long daysOverdue) {}

    // =========================================================================
    // Database setup
    // =========================================================================

    private final Connection conn; // single shared connection for this demo

    public LibraryDatabase(Connection conn) throws SQLException {
        this.conn = conn;
        createSchema();
    }

    private void createSchema() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS books (
                        id        INTEGER      PRIMARY KEY AUTO_INCREMENT,
                        title     VARCHAR(300) NOT NULL,
                        author    VARCHAR(200) NOT NULL,
                        isbn      VARCHAR(20)  NOT NULL UNIQUE,
                        available BOOLEAN      NOT NULL DEFAULT TRUE
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS members (
                        id    INTEGER      PRIMARY KEY AUTO_INCREMENT,
                        name  VARCHAR(200) NOT NULL,
                        email VARCHAR(255) NOT NULL UNIQUE
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS loans (
                        id          INTEGER  PRIMARY KEY AUTO_INCREMENT,
                        book_id     INTEGER  NOT NULL REFERENCES books(id),
                        member_id   INTEGER  NOT NULL REFERENCES members(id),
                        loan_date   DATE     NOT NULL,
                        return_date DATE
                    )
                    """);
        }
    }

    // =========================================================================
    // Book operations
    // =========================================================================

    public Book addBook(String title, String author, String isbn) throws SQLException {
        String sql = "INSERT INTO books (title, author, isbn, available) VALUES (?, ?, ?, TRUE)";
        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, isbn);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Book(keys.getInt(1), title, author, isbn, true);
            }
        }
    }

    public Optional<Book> findBookById(int id) throws SQLException {
        String sql = "SELECT id, title, author, isbn, available FROM books WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapBook(rs));
                return Optional.empty();
            }
        }
    }

    public List<Book> findAvailableBooks() throws SQLException {
        String sql = """
                SELECT id, title, author, isbn, available
                  FROM books
                 WHERE available = TRUE
                 ORDER BY title
                """;
        List<Book> books = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) books.add(mapBook(rs));
        }
        return books;
    }

    // =========================================================================
    // Member operations
    // =========================================================================

    public Member addMember(String name, String email) throws SQLException {
        String sql = "INSERT INTO members (name, email) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Member(keys.getInt(1), name, email);
            }
        }
    }

    public List<Member> findAllMembers() throws SQLException {
        String sql = "SELECT id, name, email FROM members ORDER BY name";
        List<Member> members = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                members.add(new Member(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")));
            }
        }
        return members;
    }

    // =========================================================================
    // Loan operations — the interesting part with transactions
    // =========================================================================

    /**
     * Lends a book to a member.
     *
     * Business rules:
     *  1. The book must exist and be available.
     *  2. The member must exist.
     *  3. A loan record is created and the book is marked unavailable.
     *
     * Steps 3a and 3b must be atomic — use a transaction.
     *
     * @param memberId the borrowing member
     * @param bookId   the book to borrow
     * @return the created Loan
     * @throws IllegalStateException if the book is not available
     * @throws SQLException          on any database error
     */
    public Loan borrowBook(int memberId, int bookId) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // Step 1: Check the book exists and is available (read inside transaction)
            String checkSql = "SELECT available FROM books WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Book not found: id=" + bookId);
                    }
                    if (!rs.getBoolean("available")) {
                        throw new IllegalStateException(
                                "Book id=" + bookId + " is not currently available");
                    }
                }
            }

            // Step 2: Create loan record
            LocalDate today = LocalDate.now();
            int loanId;
            String insertLoan = """
                    INSERT INTO loans (book_id, member_id, loan_date, return_date)
                    VALUES (?, ?, ?, NULL)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(
                    insertLoan, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                ps.setDate(3, Date.valueOf(today));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    loanId = keys.getInt(1);
                }
            }

            // Step 3: Mark book as unavailable
            String markUnavailable = "UPDATE books SET available = FALSE WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(markUnavailable)) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            conn.commit();
            return new Loan(loanId, bookId, memberId, today, null);

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Returns a borrowed book.
     *
     * Sets the loan's return_date to today and marks the book available again.
     * Both updates are in one transaction.
     *
     * @param loanId the loan to close
     * @return the updated Loan with return_date set
     * @throws IllegalStateException if the loan is already returned
     */
    public Loan returnBook(int loanId) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // Step 1: Load the loan (must exist and not already be returned)
            String findLoan = """
                    SELECT book_id, member_id, loan_date, return_date
                      FROM loans
                     WHERE id = ?
                    """;
            int bookId;
            int memberId;
            LocalDate loanDate;
            try (PreparedStatement ps = conn.prepareStatement(findLoan)) {
                ps.setInt(1, loanId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Loan not found: id=" + loanId);
                    }
                    if (rs.getDate("return_date") != null) {
                        throw new IllegalStateException(
                                "Loan id=" + loanId + " is already returned");
                    }
                    bookId   = rs.getInt("book_id");
                    memberId = rs.getInt("member_id");
                    loanDate = rs.getDate("loan_date").toLocalDate();
                }
            }

            // Step 2: Set return date on the loan
            LocalDate today = LocalDate.now();
            String updateLoan = "UPDATE loans SET return_date = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateLoan)) {
                ps.setDate(1, Date.valueOf(today));
                ps.setInt(2, loanId);
                ps.executeUpdate();
            }

            // Step 3: Mark book as available again
            String markAvailable = "UPDATE books SET available = TRUE WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(markAvailable)) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            conn.commit();
            return new Loan(loanId, bookId, memberId, loanDate, today);

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Finds all loans where the book has not been returned after 14 days.
     *
     * Uses a JOIN across three tables and a date calculation in SQL.
     * Returns enriched OverdueLoan records with book title, member name,
     * and how many days overdue.
     */
    public List<OverdueLoan> findOverdueLoans() throws SQLException {
        String sql = """
                SELECT l.id         AS loan_id,
                       b.title      AS book_title,
                       m.name       AS member_name,
                       l.loan_date,
                       DATEDIFF('DAY', l.loan_date, CURRENT_DATE) - 14 AS days_overdue
                  FROM loans   l
                  JOIN books   b ON b.id = l.book_id
                  JOIN members m ON m.id = l.member_id
                 WHERE l.return_date IS NULL
                   AND DATEDIFF('DAY', l.loan_date, CURRENT_DATE) > 14
                 ORDER BY days_overdue DESC
                """;

        List<OverdueLoan> overdue = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                overdue.add(new OverdueLoan(
                        rs.getInt("loan_id"),
                        rs.getString("book_title"),
                        rs.getString("member_name"),
                        rs.getDate("loan_date").toLocalDate(),
                        rs.getLong("days_overdue")));
            }
        }
        return overdue;
    }

    /**
     * Returns all active (not yet returned) loans with book and member details.
     */
    public List<String> findActiveLoans() throws SQLException {
        String sql = """
                SELECT l.id, b.title, m.name, l.loan_date
                  FROM loans   l
                  JOIN books   b ON b.id = l.book_id
                  JOIN members m ON m.id = l.member_id
                 WHERE l.return_date IS NULL
                 ORDER BY l.loan_date
                """;

        List<String> lines = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lines.add(String.format("Loan #%d: '%s' borrowed by %s on %s",
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("name"),
                        rs.getDate("loan_date").toLocalDate()));
            }
        }
        return lines;
    }

    // =========================================================================
    // Row mappers
    // =========================================================================

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"),
                rs.getBoolean("available"));
    }

    // =========================================================================
    // Main — full demonstration scenario
    // =========================================================================

    public static void main(String[] args) throws SQLException {
        System.out.println("=== Library Management System Demo ===\n");

        String url = "jdbc:h2:mem:librarydb;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {

            LibraryDatabase library = new LibraryDatabase(conn);

            // --- Add books ---
            System.out.println("-- Adding books --");
            Book b1 = library.addBook("Clean Code",
                    "Robert C. Martin", "978-0132350884");
            Book b2 = library.addBook("Effective Java",
                    "Joshua Bloch", "978-0134685991");
            Book b3 = library.addBook("The Pragmatic Programmer",
                    "Andrew Hunt & David Thomas", "978-0135957059");
            Book b4 = library.addBook("Design Patterns",
                    "Gang of Four", "978-0201633610");
            System.out.println("Added: " + b1.title());
            System.out.println("Added: " + b2.title());
            System.out.println("Added: " + b3.title());
            System.out.println("Added: " + b4.title());

            // --- Add members ---
            System.out.println("\n-- Adding members --");
            Member alice = library.addMember("Alice Chen",   "alice@example.com");
            Member bob   = library.addMember("Bob Martinez", "bob@example.com");
            System.out.println("Added member: " + alice.name());
            System.out.println("Added member: " + bob.name());

            // --- Show available books ---
            System.out.println("\n-- Available books (should be 4) --");
            library.findAvailableBooks().forEach(b ->
                    System.out.println("  " + b.title() + " by " + b.author()));

            // --- Borrow books ---
            System.out.println("\n-- Alice borrows 'Clean Code' --");
            Loan loan1 = library.borrowBook(alice.id(), b1.id());
            System.out.println("Loan created: #" + loan1.id()
                    + " on " + loan1.loanDate());

            System.out.println("\n-- Bob borrows 'Effective Java' --");
            Loan loan2 = library.borrowBook(bob.id(), b2.id());
            System.out.println("Loan created: #" + loan2.id());

            // --- Show available books (should be 2 now) ---
            System.out.println("\n-- Available books (should be 2) --");
            library.findAvailableBooks().forEach(b ->
                    System.out.println("  " + b.title()));

            // --- Try to borrow an already-borrowed book ---
            System.out.println("\n-- Alice tries to borrow 'Clean Code' again (should fail) --");
            try {
                library.borrowBook(alice.id(), b1.id());
                System.out.println("ERROR: should have thrown an exception!");
            } catch (IllegalStateException e) {
                System.out.println("Correctly rejected: " + e.getMessage());
            }

            // --- Active loans ---
            System.out.println("\n-- Active loans --");
            library.findActiveLoans().forEach(l -> System.out.println("  " + l));

            // --- Return a book ---
            System.out.println("\n-- Alice returns 'Clean Code' --");
            Loan returned = library.returnBook(loan1.id());
            System.out.println("Returned on: " + returned.returnDate());

            // --- Show available books again ---
            System.out.println("\n-- Available books (should be 3) --");
            library.findAvailableBooks().forEach(b ->
                    System.out.println("  " + b.title()));

            // --- Overdue loans demo ---
            System.out.println("\n-- Overdue loans (none — all borrowed today) --");
            List<OverdueLoan> overdue = library.findOverdueLoans();
            if (overdue.isEmpty()) {
                System.out.println("  No overdue loans.");
            } else {
                overdue.forEach(o -> System.out.printf(
                        "  Loan #%d: '%s' by %s — %d days overdue%n",
                        o.loanId(), o.bookTitle(), o.memberName(), o.daysOverdue()));
            }

            System.out.println("\nLibrary demo complete!");
        }
    }
}
