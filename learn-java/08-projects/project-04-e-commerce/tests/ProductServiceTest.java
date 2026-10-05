import model.Product;
import org.junit.jupiter.api.*;
import repository.InMemoryProductRepository;
import service.ProductService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ProductService}.
 *
 * Uses {@link InMemoryProductRepository} directly — no Mockito needed for these
 * state-based tests. Mockito usage is shown in the interaction-based tests below.
 */
@DisplayName("ProductService Tests")
class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(new InMemoryProductRepository());
    }

    // -------------------------------------------------------------------------
    // addProduct
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addProduct: creates product with correct fields")
    void addProduct_success() {
        Product p = productService.addProduct("Laptop", "A laptop", 999.99, 10, "electronics");

        assertNotNull(p.getProductId());
        assertEquals("Laptop",       p.getName());
        assertEquals(999.99,         p.getPrice());
        assertEquals(10,             p.getStockQuantity());
        assertEquals("electronics",  p.getCategoryId());
        assertTrue(p.isActive());
    }

    @Test
    @DisplayName("addProduct: blank name throws IllegalArgumentException")
    void addProduct_blankName_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                productService.addProduct("  ", "desc", 10.0, 5, "cat"));
    }

    @Test
    @DisplayName("addProduct: negative price throws IllegalArgumentException")
    void addProduct_negativePrice_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                productService.addProduct("Widget", "desc", -1.0, 5, "cat"));
    }

    // -------------------------------------------------------------------------
    // getProduct
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getProduct: finds existing product by ID")
    void getProduct_found() {
        Product created = productService.addProduct("Widget", "A widget", 5.0, 100, "misc");
        Product found   = productService.getProduct(created.getProductId());

        assertEquals(created.getProductId(), found.getProductId());
    }

    @Test
    @DisplayName("getProduct: throws NoSuchElementException for unknown ID")
    void getProduct_notFound_throws() {
        assertThrows(NoSuchElementException.class, () ->
                productService.getProduct("does-not-exist"));
    }

    // -------------------------------------------------------------------------
    // updateProduct
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateProduct: updates only specified fields")
    void updateProduct_partialUpdate() {
        Product p = productService.addProduct("Old Name", "desc", 10.0, 20, "cat");

        Product updated = productService.updateProduct(
                p.getProductId(), "New Name", null, 15.0, null, null);

        assertEquals("New Name", updated.getName());
        assertEquals(15.0,       updated.getPrice());
        assertEquals("desc",     updated.getDescription()); // unchanged
        assertEquals(20,         updated.getStockQuantity()); // unchanged
    }

    // -------------------------------------------------------------------------
    // removeProduct (soft delete)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeProduct: deactivates product without deleting it")
    void removeProduct_deactivates() {
        Product p = productService.addProduct("ToRemove", "desc", 1.0, 10, "cat");
        assertTrue(productService.removeProduct(p.getProductId()));

        Product retrieved = productService.getProduct(p.getProductId());
        assertFalse(retrieved.isActive());
        assertFalse(retrieved.isAvailable());
    }

    // -------------------------------------------------------------------------
    // searchProducts
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("searchProducts: returns matching products case-insensitively")
    void searchProducts_caseInsensitive() {
        productService.addProduct("Wireless Mouse", "A mouse", 50.0, 10, "electronics");
        productService.addProduct("USB Keyboard", "A keyboard", 80.0, 5, "electronics");
        productService.addProduct("Mouse Pad", "A mouse pad", 10.0, 20, "accessories");

        List<Product> results = productService.searchProducts("MOUSE");
        assertEquals(2, results.size());
        results.forEach(p -> assertTrue(
                p.getName().toLowerCase().contains("mouse")
                || (p.getDescription() != null && p.getDescription().toLowerCase().contains("mouse"))));
    }

    @Test
    @DisplayName("searchProducts: returns empty list when no match")
    void searchProducts_noMatch() {
        productService.addProduct("Laptop", "Fast laptop", 1000.0, 3, "electronics");
        List<Product> results = productService.searchProducts("tablet");
        assertTrue(results.isEmpty());
    }

    // -------------------------------------------------------------------------
    // getProductsByPriceRange
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getProductsByPriceRange: returns only products in range")
    void getProductsByPriceRange() {
        productService.addProduct("Cheap Item",    "cheap", 5.0,   10, "c");
        productService.addProduct("Mid Item",      "mid",   50.0,  10, "c");
        productService.addProduct("Expensive Item","exp",   500.0, 10, "c");

        List<Product> results = productService.getProductsByPriceRange(10.0, 100.0);
        assertEquals(1, results.size());
        assertEquals("Mid Item", results.get(0).getName());
    }

    // -------------------------------------------------------------------------
    // updateStock
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateStock: increases stock correctly")
    void updateStock_increase() {
        Product p = productService.addProduct("Item", "d", 10.0, 5, "c");
        Product updated = productService.updateStock(p.getProductId(), 10);
        assertEquals(15, updated.getStockQuantity());
    }

    @Test
    @DisplayName("updateStock: reduces stock correctly")
    void updateStock_reduce() {
        Product p = productService.addProduct("Item", "d", 10.0, 10, "c");
        Product updated = productService.updateStock(p.getProductId(), -3);
        assertEquals(7, updated.getStockQuantity());
    }

    @Test
    @DisplayName("updateStock: throws when reducing below zero")
    void updateStock_belowZero_throws() {
        Product p = productService.addProduct("Item", "d", 10.0, 2, "c");
        assertThrows(IllegalStateException.class, () ->
                productService.updateStock(p.getProductId(), -5));
    }

    // -------------------------------------------------------------------------
    // getProductsByCategory
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getProductsByCategory: returns products in matching category only")
    void getProductsByCategory() {
        productService.addProduct("Phone",   "d", 500.0, 5, "electronics");
        productService.addProduct("Tablet",  "d", 300.0, 5, "electronics");
        productService.addProduct("T-Shirt", "d",  20.0, 50, "apparel");

        List<Product> electronics = productService.getProductsByCategory("electronics");
        assertEquals(2, electronics.size());
        electronics.forEach(p -> assertEquals("electronics", p.getCategoryId()));
    }
}
