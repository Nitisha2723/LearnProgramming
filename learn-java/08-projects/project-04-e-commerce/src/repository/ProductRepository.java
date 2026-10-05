package repository;

import model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Product} entities.
 */
public interface ProductRepository {

    List<Product> findAll();

    Optional<Product> findById(String productId);

    /** Returns all products belonging to the given category. */
    List<Product> findByCategory(String categoryId);

    /**
     * Returns products whose price falls within [minPrice, maxPrice].
     *
     * @param minPrice inclusive lower bound
     * @param maxPrice inclusive upper bound
     */
    List<Product> findByPriceRange(double minPrice, double maxPrice);

    /**
     * Returns products whose name or description contains the keyword
     * (case-insensitive).
     */
    List<Product> search(String keyword);

    Product save(Product product);

    Product update(Product product);

    boolean delete(String productId);
}
