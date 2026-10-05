import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Solution to Exercise 01: ProductRepository
 *
 * Complete implementation of all five CRUD methods.
 * Compare this to your own solution in Exercise01.java.
 */
public class Solution01 {

    public record Product(int id, String name, String category, double price) {

        public static Product of(String name, String category, double price) {
            return new Product(0, name, category, price);
        }
    }

    public static class ProductRepository {

        private final DataSource dataSource;

        public ProductRepository(DataSource dataSource) {
            this.dataSource = dataSource;
        }

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
         * Inserts a product and returns it with the generated id.
         *
         * Key points:
         *  - RETURN_GENERATED_KEYS requests the auto-increment value.
         *  - getGeneratedKeys() is a ResultSet — iterate with next(), read with getInt(1).
         *  - executeUpdate() returns affected row count — verify it is 1.
         */
        public Product save(Product product) {
            String sql = "INSERT INTO products (name, category, price) VALUES (?, ?, ?)";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, product.name());
                ps.setString(2, product.category());
                ps.setDouble(3, product.price());

                int rowsInserted = ps.executeUpdate();
                if (rowsInserted != 1) {
                    throw new RuntimeException("Insert affected " + rowsInserted + " rows");
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        return new Product(generatedId, product.name(),
                                product.category(), product.price());
                    }
                    throw new RuntimeException("No generated key returned");
                }

            } catch (SQLException e) {
                throw new RuntimeException("save failed for: " + product.name(), e);
            }
        }

        /**
         * Finds a product by primary key.
         *
         * Key points:
         *  - executeQuery() for SELECT.
         *  - rs.next() returns false when there are no rows.
         *  - Optional.empty() (not null) when not found.
         */
        public Optional<Product> findById(int id) {
            String sql = "SELECT id, name, category, price FROM products WHERE id = ?";

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
         * Returns products in the given category, cheapest first.
         *
         * Key points:
         *  - Iterate with while (rs.next()) to collect all rows.
         *  - ORDER BY in the SQL — never sort in Java when the DB can do it.
         */
        public List<Product> findByCategory(String category) {
            String sql = """
                    SELECT id, name, category, price
                      FROM products
                     WHERE category = ?
                     ORDER BY price ASC
                    """;
            List<Product> result = new ArrayList<>();

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, category);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(mapRow(rs));
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException("findByCategory failed for: " + category, e);
            }

            return result;
        }

        /**
         * Updates all fields of an existing product.
         *
         * Key points:
         *  - executeUpdate() returns number of rows changed.
         *  - 0 rows means the product was not found — return false.
         *  - id comes last in the WHERE clause — bind order matches SQL parameter order.
         */
        public boolean update(Product product) {
            String sql = """
                    UPDATE products
                       SET name = ?, category = ?, price = ?
                     WHERE id = ?
                    """;

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, product.name());
                ps.setString(2, product.category());
                ps.setDouble(3, product.price());
                ps.setInt(4, product.id());         // id goes LAST — matches WHERE id = ?

                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                throw new RuntimeException("update failed for id=" + product.id(), e);
            }
        }

        /**
         * Deletes a product by primary key.
         *
         * Key points:
         *  - DELETE uses executeUpdate(), not executeQuery().
         *  - Check the row count to distinguish "deleted" from "not found".
         */
        public boolean delete(int id) {
            String sql = "DELETE FROM products WHERE id = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                throw new RuntimeException("delete failed for id=" + id, e);
            }
        }

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
    // Main — runs the full scenario to verify the solution
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:solution01;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");

        ProductRepository repo = new ProductRepository(ds);
        repo.createTable();

        System.out.println("=== Solution 01: ProductRepository ===\n");

        Product laptop  = repo.save(Product.of("Laptop Pro",     "Electronics", 1299.99));
        Product mouse   = repo.save(Product.of("Wireless Mouse", "Electronics",   29.99));
        Product desk    = repo.save(Product.of("Standing Desk",  "Furniture",    449.00));
        Product monitor = repo.save(Product.of("4K Monitor",     "Electronics",  349.99));

        System.out.println("Saved: " + laptop);
        System.out.println("Saved: " + mouse);
        System.out.println("Saved: " + desk);
        System.out.println("Saved: " + monitor);

        System.out.println("\nFind id=" + laptop.id() + ": " + repo.findById(laptop.id()));
        System.out.println("Find id=999: " + repo.findById(999));

        System.out.println("\nElectronics (price asc): " + repo.findByCategory("Electronics"));
        System.out.println("Furniture: " + repo.findByCategory("Furniture"));

        Product updated = new Product(laptop.id(), "Laptop Pro 2025", "Electronics", 1399.99);
        System.out.println("\nUpdate: " + repo.update(updated));
        System.out.println("After update: " + repo.findById(laptop.id()));

        System.out.println("\nDelete mouse: " + repo.delete(mouse.id()));
        System.out.println("Delete 999: " + repo.delete(999));
        System.out.println("Electronics after delete: " + repo.findByCategory("Electronics"));

        System.out.println("\nSolution verification complete!");
    }
}
