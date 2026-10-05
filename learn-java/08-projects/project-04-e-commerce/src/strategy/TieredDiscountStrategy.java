package strategy;

/**
 * Tiered discount strategy — percentage increases as the order total grows.
 *
 * <table border="1">
 *   <tr><th>Total</th><th>Discount</th></tr>
 *   <tr><td>&gt; $500</td><td>20%</td></tr>
 *   <tr><td>&gt; $200</td><td>15%</td></tr>
 *   <tr><td>&gt; $100</td><td>10%</td></tr>
 *   <tr><td>≤ $100</td><td>0%</td></tr>
 * </table>
 */
public class TieredDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculateDiscount(double orderTotal) {
        double rate;
        if      (orderTotal > 500) rate = 0.20;
        else if (orderTotal > 200) rate = 0.15;
        else if (orderTotal > 100) rate = 0.10;
        else                       rate = 0.0;

        return orderTotal * rate;
    }

    @Override
    public String getDiscountName() {
        return "Tiered Discount (10%/15%/20%)";
    }
}
