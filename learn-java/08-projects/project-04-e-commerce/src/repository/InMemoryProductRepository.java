package repository;

import model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link ProductRepository}.
 */
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> store = new HashMap<>();

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(store.get(productId));
    }

    @Override
    public List<Product> findByCategory(String categoryId) {
        if (categoryId == null) return new ArrayList<>();
        return store.values().stream()
                .filter(p -> categoryId.equals(p.getCategoryId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByPriceRange(double minPrice, double maxPrice) {
        return store.values().stream()
                .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return new ArrayList<>();
        String lower = keyword.toLowerCase();
        return store.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(lower)
                          || (p.getDescription() != null
                              && p.getDescription().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }

    @Override
    public Product save(Product product) {
        if (store.containsKey(product.getProductId())) {
            throw new IllegalArgumentException(
                    "Product with ID " + product.getProductId() + " already exists.");
        }
        store.put(product.getProductId(), product);
        return product;
    }

    @Override
    public Product update(Product product) {
        if (!store.containsKey(product.getProductId())) {
            throw new IllegalArgumentException(
                    "No product found with ID " + product.getProductId());
        }
        store.put(product.getProductId(), product);
        return product;
    }

    @Override
    public boolean delete(String productId) {
        return store.remove(productId) != null;
    }
}
