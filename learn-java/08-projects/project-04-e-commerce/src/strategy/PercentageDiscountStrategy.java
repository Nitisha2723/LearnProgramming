package strategy;

/**
 * Applies a fixed percentage off the order total.
 *
 * <p>Example: a 10% discount on a $200 order yields a $20 discount.</p>
 */
public class PercentageDiscountStrategy implements DiscountStrategy {

    private final double percentage; // e.g. 10.0 means 10%

    /**
     * @param percentage discount percentage (0 – 100 inclusive)
     * @throws IllegalArgumentException if out of range
     */
    public PercentageDiscountStrategy(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException(
                    "Percentage must be between 0 and 100, got: " + percentage);
        }
        this.percentage = percentage;
    }

    @Override
    public double calculateDiscount(double orderTotal) {
        return orderTotal * (percentage / 100.0);
    }

    @Override
    public String getDiscountName() {
        return String.format("%.0f%% Off", percentage);
    }
}
