package strategy;

/**
 * Strategy interface for discount algorithms.
 *
 * <p>Implementing classes encapsulate a single discount calculation rule.
 * Strategies are injected into {@link service.OrderService} at order-placement
 * time, allowing different discount policies without changing the service.</p>
 */
public interface DiscountStrategy {

    /**
     * Calculates the discount to apply to the given order total.
     *
     * @param orderTotal the pre-discount order total (must be ≥ 0)
     * @return the discount amount (≥ 0, never exceeds orderTotal)
     */
    double calculateDiscount(double orderTotal);

    /** Returns a human-readable name for this strategy (used in receipts/logs). */
    String getDiscountName();
}
