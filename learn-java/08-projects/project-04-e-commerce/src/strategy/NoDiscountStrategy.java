package strategy;

/**
 * Null-object discount strategy — applies no discount at all.
 * Use as a safe default when no promotion is active.
 */
public class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculateDiscount(double orderTotal) {
        return 0.0;
    }

    @Override
    public String getDiscountName() {
        return "No Discount";
    }
}
