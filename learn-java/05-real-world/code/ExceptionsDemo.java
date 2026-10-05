import java.io.*;
import java.nio.file.*;
import java.sql.*;

/**
 * ExceptionsDemo.java
 *
 * Demonstrates:
 * - Custom exceptions (checked and unchecked)
 * - try-with-resources (the RIGHT way to handle resources)
 * - Exception chaining
 * - Multi-catch
 * - Best practices and anti-patterns
 */
public class ExceptionsDemo {

    // =========================================================
    // 1. Custom Exception Classes
    // =========================================================

    // Checked exception — caller MUST handle or declare
    static class InsufficientFundsException extends Exception {
        private final double amount;
        private final double balance;

        InsufficientFundsException(double amount, double balance) {
            super(String.format(
                "Cannot withdraw $%.2f: account balance is only $%.2f",
                amount, balance));
            this.amount = amount;
            this.balance = balance;
        }

        double getAmount()  { return amount; }
        double getBalance() { return balance; }
        double getShortfall() { return amount - balance; }
    }

    // Unchecked exception — extends RuntimeException, no forced handling
    static class ProductNotFoundException extends RuntimeException {
        private final String productId;

        ProductNotFoundException(String productId) {
            super("Product not found: " + productId);
            this.productId = productId;
        }

        // Constructor that preserves original cause (for exception chaining)
        ProductNotFoundException(String productId, Throwable cause) {
            super("Product not found: " + productId, cause);
            this.productId = productId;
        }

        String getProductId() { return productId; }
    }

    // Domain validation exception
    static class ValidationException extends RuntimeException {
        private final String field;

        ValidationException(String field, String message) {
            super(String.format("Validation failed for field '%s': %s", field, message));
            this.field = field;
        }

        String getField() { return field; }
    }

    // =========================================================
    // 2. Using Custom Exceptions
    // =========================================================

    static class BankAccount {
        private final String owner;
        private double balance;

        BankAccount(String owner, double initialBalance) {
            if (owner == null || owner.isBlank()) {
                throw new ValidationException("owner", "cannot be null or blank");
            }
            if (initialBalance < 0) {
                throw new ValidationException("initialBalance", "cannot be negative");
            }
            this.owner = owner;
            this.balance = initialBalance;
        }

        void deposit(double amount) {
            if (amount <= 0) {
                throw new ValidationException("amount", "deposit amount must be positive");
            }
            balance += amount;
            System.out.printf("[%s] Deposited $%.2f → balance: $%.2f%n", owner, amount, balance);
        }

        void withdraw(double amount) throws InsufficientFundsException {
            if (amount <= 0) {
                throw new ValidationException("amount", "withdrawal amount must be positive");
            }
            if (amount > balance) {
                throw new InsufficientFundsException(amount, balance);
            }
            balance -= amount;
            System.out.printf("[%s] Withdrew $%.2f → balance: $%.2f%n", owner, amount, balance);
        }

        double getBalance() { return balance; }
        String getOwner()   { return owner; }
    }

    static void customExceptionsDemo() {
        System.out.println("=== Custom Exceptions Demo ===\n");

        // --- Checked exception handling ---
        BankAccount account = new BankAccount("Alice", 1000.00);
        account.deposit(500.00);

        try {
            account.withdraw(2000.00);  // More than balance!
        } catch (InsufficientFundsException e) {
            System.out.println("Cannot withdraw: " + e.getMessage());
            System.out.printf("You're $%.2f short. Consider depositing more.%n",
                e.getShortfall());
        }

        // --- Unchecked exception ---
        try {
            throw new ProductNotFoundException("SKU-999");
        } catch (ProductNotFoundException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // --- Validation exception ---
        try {
            new BankAccount("", 500.00);
        } catch (ValidationException e) {
            System.out.println("Validation error: " + e.getMessage());
            System.out.println("Failed field: " + e.getField());
        }

        // --- Multi-catch ---
        try {
            String s = null;
            s.length();         // NullPointerException
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println("Caught: " + e.getClass().getSimpleName());
        }
    }

    // =========================================================
    // 3. try-with-resources
    // =========================================================

    // A custom AutoCloseable resource
    static class DatabaseConnection implements AutoCloseable {
        private final String url;
        private boolean open = false;

        DatabaseConnection(String url) throws SQLException {
            this.url = url;
            // Simulate connection
            if (url.contains("bad")) {
                throw new SQLException("Could not connect to: " + url);
            }
            this.open = true;
            System.out.println("Connection opened: " + url);
        }

        String query(String sql) throws SQLException {
            if (!open) throw new SQLException("Connection is closed");
            System.out.println("Executing: " + sql);
            return "Result of: " + sql;
        }

        @Override
        public void close() throws SQLException {
            if (open) {
                open = false;
                System.out.println("Connection closed: " + url);
            }
        }
    }

    static void tryWithResourcesDemo() {
        System.out.println("\n=== try-with-resources Demo ===\n");

        // --- Single resource ---
        try (DatabaseConnection conn = new DatabaseConnection("jdbc:mysql://localhost/mydb")) {
            String result = conn.query("SELECT * FROM users");
            System.out.println("Query result: " + result);
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        // conn.close() is automatically called here

        // --- Multiple resources (closed in reverse order) ---
        System.out.println();
        try (
            DatabaseConnection conn = new DatabaseConnection("jdbc:mysql://localhost/mydb");
            // Simulate a second resource (e.g., a file writer for logging)
            StringWriter logger = new StringWriter()
        ) {
            String result = conn.query("SELECT * FROM products");
            logger.write("Query executed: SELECT * FROM products\n");
            System.out.println("Result: " + result);
            System.out.println("Log: " + logger.toString().trim());
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }

        // --- Connection failure ---
        System.out.println();
        try (DatabaseConnection conn = new DatabaseConnection("bad://connection")) {
            conn.query("SELECT 1");
        } catch (SQLException e) {
            System.out.println("Connection failed (as expected): " + e.getMessage());
        }

        // --- File reading with try-with-resources ---
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("demo", ".txt");
            Files.writeString(tempFile, "Hello from temp file!\nLine 2\nLine 3");

            // Read the file — reader is automatically closed
            try (BufferedReader reader = Files.newBufferedReader(tempFile)) {
                reader.lines().forEach(line -> System.out.println("  " + line));
            }
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        } finally {
            // Clean up temp file in finally to ensure deletion even if exception occurs
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); }
                catch (IOException ignored) { /* best effort */ }
            }
        }
    }

    // =========================================================
    // 4. Exception Chaining
    // =========================================================

    // Simulated service layer
    static class UserService {
        private static final String[] DATABASE = {"alice@example.com", "bob@example.com"};

        // Low-level exception
        static String queryDatabase(String userId) throws SQLException {
            if (userId.equals("999")) {
                throw new SQLException("Connection reset while querying user " + userId);
            }
            if (userId.equals("42")) {
                return "alice@example.com";
            }
            return null;
        }

        // Service wraps and chains the exception
        static String getEmailForUser(String userId) {
            try {
                String email = queryDatabase(userId);
                if (email == null) {
                    throw new IllegalArgumentException("No user found with ID: " + userId);
                }
                return email;
            } catch (SQLException e) {
                // Chain the original exception — don't lose it!
                throw new RuntimeException(
                    "Failed to retrieve email for user " + userId, e);
            }
        }
    }

    static void exceptionChainingDemo() {
        System.out.println("\n=== Exception Chaining Demo ===\n");

        // Successful case
        try {
            String email = UserService.getEmailForUser("42");
            System.out.println("Found email: " + email);
        } catch (Exception e) {
            System.err.println("Unexpected: " + e.getMessage());
        }

        // Chained exception case
        System.out.println();
        try {
            UserService.getEmailForUser("999");  // Triggers SQLException
        } catch (RuntimeException e) {
            System.out.println("Top-level error: " + e.getMessage());
            System.out.println("Root cause: " + e.getCause().getClass().getSimpleName()
                + ": " + e.getCause().getMessage());

            // In production you'd use a logger:
            // logger.error("Failed to get user email", e);
            // Which would print the full chain with stack traces
        }
    }

    // =========================================================
    // 5. Best Practices vs Anti-Patterns
    // =========================================================

    static void bestPracticesDemo() {
        System.out.println("\n=== Best Practices vs Anti-Patterns ===\n");

        // --- ANTI-PATTERN: empty catch block ---
        // DON'T DO THIS:
        try {
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            // Empty! The exception is swallowed — you'll never know this happened
            // At minimum: System.err.println("Caught: " + e);
        }
        System.out.println("Anti-pattern: exception silently swallowed (no output)");

        // --- GOOD PRACTICE: specific catch with meaningful handling ---
        try {
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            System.out.println("Good practice: Division error caught — " + e.getMessage());
        }

        // --- ANTI-PATTERN: using exceptions for flow control ---
        // Don't use exception to check if string is a number:
        String input = "abc";
        boolean isNumber;
        try {
            Integer.parseInt(input);
            isNumber = true;
        } catch (NumberFormatException e) {
            isNumber = false;  // Exceptions are expensive!
        }

        // GOOD PRACTICE: use a check method
        boolean isNumberBetter = input.matches("-?\\d+");
        System.out.println("Is number (regex check): " + isNumberBetter);

        // --- FINALLY vs try-with-resources ---
        // Old way (verbose and error-prone):
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new StringReader("test content"));
            System.out.println("Old way reading: " + reader.readLine());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); }
                catch (IOException e) { /* ignore close error */ }
            }
        }

        // New way (clean and correct):
        try (BufferedReader br = new BufferedReader(new StringReader("test content"))) {
            System.out.println("New way reading: " + br.readLine());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        customExceptionsDemo();
        tryWithResourcesDemo();
        exceptionChainingDemo();
        bestPracticesDemo();
    }
}
