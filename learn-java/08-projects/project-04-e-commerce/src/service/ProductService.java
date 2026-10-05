package service;

import model.Product;
import repository.ProductRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Business logic for product catalog management.
 */
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    /**
     * Adds a new product to the catalog.
     */
    public Product addProduct(String name, String description, double price,
                              int stockQty, String categoryId) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Product name is required.");
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative.");
        if (stockQty < 0)
            throw new IllegalArgumentException("Stock quantity cannot be negative.");

        Product product = new Product(name, description, price, stockQty, categoryId);
        return productRepository.save(product);
    }

    /**
     * Retrieves a product by ID.
     *
     * @throws NoSuchElementException if not found
     */
    public Product getProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Product not found: " + productId));
    }

    /**
     * Updates mutable fields. Pass {@code null} to leave a field unchanged.
     */
    public Product updateProduct(String productId, String name, String description,
                                  Double price, Integer stockQty, String categoryId) {
        Product product = getProduct(productId);

        if (name        != null && !name.isBlank())        product.setName(name);
        if (description != null)                           product.setDescription(description);
        if (price       != null)                           product.setPrice(price);
        if (stockQty    != null)                           product.setStockQuantity(stockQty);
        if (categoryId  != null && !categoryId.isBlank()) product.setCategoryId(categoryId);

        return productRepository.update(product);
    }

    /**
     * Deactivates a product (soft delete — the record is kept but marked inactive).
     *
     * @throws NoSuchElementException if not found
     */
    public boolean removeProduct(String productId) {
        Product product = getProduct(productId);
        product.setActive(false);
        productRepository.update(product);
        return true;
    }

    /**
     * Full-text search across name and description (case-insensitive).
     */
    public List<Product> searchProducts(String keyword) {
        return productRepository.search(keyword);
    }

    public List<Product> getProductsByCategory(String categoryId) {
        return productRepository.findByCategory(categoryId);
    }

    public List<Product> getProductsByPriceRange(double min, double max) {
        return productRepository.findByPriceRange(min, max);
    }

    /**
     * Adjusts the stock quantity by the given delta (positive = add, negative = reduce).
     *
     * @throws NoSuchElementException if the product is not found
     * @throws IllegalStateException  if the adjustment would make stock negative
     */
    public Product updateStock(String productId, int quantity) {
        Product product = getProduct(productId);
        if (quantity > 0) {
            product.increaseStock(quantity);
        } else if (quantity < 0) {
            product.reduceStock(-quantity);
        }
        return productRepository.update(product);
    }

    /**
     * Returns all active products in the catalog.
     */
    public List<Product> getAllActiveProducts() {
        return productRepository.findAll().stream()
                .filter(Product::isActive)
                .collect(Collectors.toList());
    }
}
