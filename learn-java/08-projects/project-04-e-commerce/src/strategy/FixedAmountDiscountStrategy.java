package strategy;

/**
 * Deducts a fixed currency amount from the order total.
 *
 * <p>The discount is capped to the order total so the result is never negative
 * (e.g., a $20 coupon on a $15 order yields a $15 discount, not −$5).</p>
 */
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    private final double amount;

    /**
     * @param amount fixed discount amount (must be ≥ 0)
     * @throws IllegalArgumentException if amount is negative
     */
    public FixedAmountDiscountStrategy(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Fixed discount amount cannot be negative, got: " + amount);
        }
        this.amount = amount;
    }

    @Override
    public double calculateDiscount(double orderTotal) {
        // Never discount more than the total
        return Math.min(amount, orderTotal);
    }

    @Override
    public String getDiscountName() {
        return String.format("$%.2f Off", amount);
    }
}
