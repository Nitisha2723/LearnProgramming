import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import strategy.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for all four {@link DiscountStrategy} implementations.
 * Uses {@code @ParameterizedTest} to cover multiple input combinations concisely.
 */
@DisplayName("DiscountStrategy Tests")
class DiscountStrategyTest {

    // =========================================================================
    // NoDiscountStrategy
    // =========================================================================

    @Nested
    @DisplayName("NoDiscountStrategy")
    class NoDiscountTests {

        private final DiscountStrategy strategy = new NoDiscountStrategy();

        @ParameterizedTest(name = "total={0} → discount=0")
        @CsvSource({"0.0", "50.0", "100.0", "500.0", "1000.0"})
        void alwaysReturnsZero(double total) {
            assertEquals(0.0, strategy.calculateDiscount(total), 0.001);
        }

        @Test
        void nameIsCorrect() {
            assertEquals("No Discount", strategy.getDiscountName());
        }
    }

    // =========================================================================
    // PercentageDiscountStrategy
    // =========================================================================

    @Nested
    @DisplayName("PercentageDiscountStrategy")
    class PercentageDiscountTests {

        @ParameterizedTest(name = "{0}% of ${1} → ${2}")
        @CsvSource({
            "10.0,  100.0,  10.0",
            "10.0,  200.0,  20.0",
            "20.0,  150.0,  30.0",
            " 0.0,  500.0,   0.0",
            "100.0, 200.0, 200.0"
        })
        void calculatesCorrectPercentage(double percentage, double total, double expected) {
            DiscountStrategy strategy = new PercentageDiscountStrategy(percentage);
            assertEquals(expected, strategy.calculateDiscount(total), 0.001);
        }

        @Test
        void nameIncludesPercentage() {
            String name = new PercentageDiscountStrategy(15).getDiscountName();
            assertTrue(name.contains("15"), "Name should contain the percentage value");
        }

        @Test
        void throwsForNegativePercentage() {
            assertThrows(IllegalArgumentException.class, () ->
                    new PercentageDiscountStrategy(-1));
        }

        @Test
        void throwsForPercentageOver100() {
            assertThrows(IllegalArgumentException.class, () ->
                    new PercentageDiscountStrategy(101));
        }
    }

    // =========================================================================
    // FixedAmountDiscountStrategy
    // =========================================================================

    @Nested
    @DisplayName("FixedAmountDiscountStrategy")
    class FixedAmountDiscountTests {

        @ParameterizedTest(name = "fixed=${0}, total=${1} → discount=${2}")
        @CsvSource({
            "20.0, 100.0, 20.0",   // normal deduction
            "20.0,  15.0, 15.0",   // capped to total
            " 0.0, 100.0,  0.0",   // zero amount
            "50.0, 200.0, 50.0"    // well below total
        })
        void calculatesCorrectFixedDiscount(double amount, double total, double expected) {
            DiscountStrategy strategy = new FixedAmountDiscountStrategy(amount);
            assertEquals(expected, strategy.calculateDiscount(total), 0.001);
        }

        @Test
        void discountNeverExceedsTotal() {
            DiscountStrategy strategy = new FixedAmountDiscountStrategy(999.0);
            double discount = strategy.calculateDiscount(10.0);
            assertEquals(10.0, discount, 0.001); // capped at 10
        }

        @Test
        void nameIncludesAmount() {
            String name = new FixedAmountDiscountStrategy(25.0).getDiscountName();
            assertTrue(name.contains("25"), "Name should reference the fixed amount");
        }

        @Test
        void throwsForNegativeAmount() {
            assertThrows(IllegalArgumentException.class, () ->
                    new FixedAmountDiscountStrategy(-10));
        }
    }

    // =========================================================================
    // TieredDiscountStrategy
    // =========================================================================

    @Nested
    @DisplayName("TieredDiscountStrategy")
    class TieredDiscountTests {

        private final DiscountStrategy strategy = new TieredDiscountStrategy();

        @ParameterizedTest(name = "total=${0} → ${1} discount (tier={2})")
        @CsvSource({
            "  50.0,   0.0, none",    // ≤ 100 → 0%
            " 100.0,   0.0, none",    // exactly 100 → 0% (must be > 100)
            " 101.0,  10.1, 10%",     // > 100 → 10%
            " 200.0,  20.0, 10%",     // exactly 200 → 10% (must be > 200)
            " 200.1,  30.015, 15%",   // > 200 → 15%
            " 500.0,  75.0, 15%",     // exactly 500 → 15% (must be > 500)
            " 500.1, 100.02, 20%",    // > 500 → 20%
            "1000.0, 200.0, 20%"      // well above 500 → 20%
        })
        void correctTierApplied(double total, double expectedDiscount, String tierLabel) {
            assertEquals(expectedDiscount, strategy.calculateDiscount(total), 0.01,
                    "Tier label: " + tierLabel);
        }

        @Test
        void nameDescribesTiers() {
            String name = strategy.getDiscountName();
            assertNotNull(name);
            assertFalse(name.isBlank());
        }
    }
}
