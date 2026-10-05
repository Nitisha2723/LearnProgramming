import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Exercise 01: ProductRepository
 *
 * Implement a JDBC repository for a Product entity.
 *
 * Requirements:
 *  - Use an H2 in-memory DataSource (see the main() method for setup).
 *  - All SQL must use PreparedStatement — no string concatenation of values.
 *  - Always use try-with-resources for Connection, Statement, and ResultSet.
 *  - Wrap SQLExceptions in RuntimeException (so callers don't need checked exceptions).
 *
 * The Product record is already defined below.
 * The table DDL is provided — call createTable() before running the other methods.
 *
 * TODO: Implement the five methods marked with TODO.
 *
 * Run main() to test your implementation interactively.
 * The tests in tests/JdbcTest.java also cover this class.
 */
public class Exercise01 {

    /** Represents a product in the catalog. */
    public record Product(int id, String name, String category, double price) {

        /** Factory for creating a new (unsaved) product without a known id. */
        public static Product of(String name, String category, double price) {
            return new Product(0, name, category, price);
        }
    }

    // -------------------------------------------------------------------------
    // ProductRepository — implement the TODO methods
    // -------------------------------------------------------------------------

    public static class ProductRepository {

        private final DataSource dataSource;

        public ProductRepository(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        /**
         * Creates the products table.
         * Already implemented — call this before inserting any data.
         */
        public void createTable() {
            String ddl = """
                    CREATE TABLE IF NOT EXISTS products (
                        id       INTEGER        PRIMARY KEY AUTO_INCREMENT,
                        name     VARCHAR(200)   NOT NULL,
                        category VARCHAR(100)   NOT NULL,
                        price    DECIMAL(10, 2) NOT NULL
                    )
                    """;
            try (Connection conn = dataSource.getConnection();
                 Statement stmt  = conn.createStatement()) {
                stmt.execute(ddl);
            } catch (SQLException e) {
                throw new RuntimeException("createTable failed", e);
            }
        }

        /**
         * TODO: Implement this method.
         *
         * Insert a new product and return the saved product with its generated id.
         * Use Statement.RETURN_GENERATED_KEYS to retrieve the auto-increment id.
         *
         * @param product product to save (id field is ignored)
         * @return the saved product with its auto-generated id set
         */
        public Product save(Product product) {
            // TODO: implement
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * TODO: Implement this method.
         *
         * Find a product by primary key.
         *
         * @param id the primary key
         * @return Optional.of(product) if found, Optional.empty() if not found
         */
        public Optional<Product> findById(int id) {
            // TODO: implement
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * TODO: Implement this method.
         *
         * Return all products in the given category, ordered by price ascending.
         *
         * @param category the category to filter by (exact match)
         * @return list of matching products (empty list if none)
         */
        public List<Product> findByCategory(String category) {
            // TODO: implement
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * TODO: Implement this method.
         *
         * Update the name, category, and price of an existing product.
         *
         * @param product product with updated fields (id must match an existing row)
         * @return true if the row was updated, false if no product with that id exists
         */
        public boolean update(Product product) {
            // TODO: implement
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * TODO: Implement this method.
         *
         * Delete the product with the given id.
         *
         * @param id primary key of the product to delete
         * @return true if deleted, false if no product with that id exists
         */
        public boolean delete(int id) {
            // TODO: implement
            throw new UnsupportedOperationException("Not yet implemented");
        }

        // Helper: already done for you
        private Product mapRow(ResultSet rs) throws SQLException {
            return new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price")
            );
        }
    }

    // -------------------------------------------------------------------------
    // Manual test runner — run main() to see output
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:exercise01;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");

        ProductRepository repo = new ProductRepository(ds);
        repo.createTable();

        System.out.println("=== Exercise 01: ProductRepository ===\n");

        // Save products
        Product laptop  = repo.save(Product.of("Laptop Pro",    "Electronics", 1299.99));
        Product mouse   = repo.save(Product.of("Wireless Mouse", "Electronics",   29.99));
        Product desk    = repo.save(Product.of("Standing Desk", "Furniture",     449.00));
        Product monitor = repo.save(Product.of("4K Monitor",    "Electronics",   349.99));
        System.out.println("Saved products: " + laptop + ", " + mouse + ", " + desk + ", " + monitor);

        // Find by id
        System.out.println("\nFind id=" + laptop.id() + ": " + repo.findById(laptop.id()));
        System.out.println("Find id=999 (missing): " + repo.findById(999));

        // Find by category
        System.out.println("\nElectronics (by price asc): " + repo.findByCategory("Electronics"));
        System.out.println("Furniture: " + repo.findByCategory("Furniture"));

        // Update
        Product updatedLaptop = new Product(laptop.id(), "Laptop Pro 2025", "Electronics", 1399.99);
        System.out.println("\nUpdate laptop: " + repo.update(updatedLaptop));
        System.out.println("After update: " + repo.findById(laptop.id()));

        // Delete
        System.out.println("\nDelete mouse: " + repo.delete(mouse.id()));
        System.out.println("Delete missing id=999: " + repo.delete(999));
        System.out.println("Electronics after delete: " + repo.findByCategory("Electronics"));

        System.out.println("\nAll exercises complete!");
    }
}
