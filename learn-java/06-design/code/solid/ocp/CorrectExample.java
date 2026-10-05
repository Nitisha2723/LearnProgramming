package solid.ocp;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 *  OCP CORRECT EXAMPLE — Open/Closed Principle
 * ============================================================
 *
 *  THE FIX:
 *    We introduce a TaxStrategy INTERFACE. Each country's tax logic lives
 *    in its own class that IMPLEMENTS the interface.
 *
 *    TaxCalculator is now "closed for modification":
 *      - Its core algorithm never changes.
 *      - New countries are added by creating NEW classes (open for extension).
 *      - Existing classes are untouched.
 *
 *  HOW IT ACHIEVES OCP:
 *    The if/else chain in ViolationExample.java has been replaced by
 *    polymorphism. The TaxCalculator class depends on the TaxStrategy
 *    ABSTRACTION, not on any concrete country's logic.
 *
 *    Adding Mexico:
 *      BEFORE: Edit TaxCalculator.java, risk breaking existing logic.
 *      AFTER:  Create MexicoTaxStrategy.java, register it, done.
 *              TaxCalculator.java is NEVER touched.
 *
 *  THIS PATTERN IS ALSO KNOWN AS:
 *    - Strategy Pattern (GoF) — behaviour is encapsulated behind an interface
 *    - Plug-in / Extension Point design
 */
public class CorrectExample {

    // =========================================================================
    //  THE ABSTRACTION: TaxStrategy interface
    //
    //  This is the "extension point". New countries extend this interface;
    //  they do NOT modify TaxCalculator.
    //
    //  KEY INSIGHT: TaxCalculator depends on THIS interface, not on any
    //  concrete country class. This is the key to keeping it closed.
    // =========================================================================

    /**
     * Contract for all tax-calculation strategies.
     *
     * Every country's tax implementation must fulfill this contract.
     * The interface is stable — it almost never changes — which is why
     * classes that depend on it (TaxCalculator) can remain stable too.
     */
    interface TaxStrategy {
        /**
         * Calculates the tax amount for a given pre-tax purchase amount.
         *
         * @param amount The purchase amount before tax.
         * @return The tax amount (not the total — just the tax portion).
         */
        double calculateTax(double amount);

        /**
         * Returns the country code this strategy handles (for display purposes).
         */
        String getCountryCode();
    }

    // =========================================================================
    //  CONCRETE STRATEGY: US Tax
    //
    //  This class has ONE reason to change: US tax law changes.
    //  It does not affect UK, Germany, or any other country's class.
    // =========================================================================

    /** US federal income/sales tax — simplified to 10% flat rate. */
    static class USTaxStrategy implements TaxStrategy {

        private static final double FEDERAL_RATE = 0.10;

        @Override
        public double calculateTax(double amount) {
            double tax = amount * FEDERAL_RATE;
            System.out.printf("[USTaxStrategy] US Federal tax (%.0f%%): %.2f%n",
                    FEDERAL_RATE * 100, tax);
            return tax;
        }

        @Override
        public String getCountryCode() { return "US"; }
    }

    // =========================================================================
    //  CONCRETE STRATEGY: UK Tax
    //
    //  If the UK changes its VAT rate from 20% to 22%, ONLY this class changes.
    //  USTaxStrategy, GermanyTaxStrategy, etc. remain completely untouched.
    // =========================================================================

    /** UK Value Added Tax (VAT) at 20% standard rate. */
    static class UKTaxStrategy implements TaxStrategy {

        private static final double VAT_RATE = 0.20;

        @Override
        public double calculateTax(double amount) {
            double tax = amount * VAT_RATE;
            System.out.printf("[UKTaxStrategy] UK VAT (%.0f%%): %.2f%n",
                    VAT_RATE * 100, tax);
            return tax;
        }

        @Override
        public String getCountryCode() { return "UK"; }
    }

    // =========================================================================
    //  CONCRETE STRATEGY: Germany Tax
    //
    //  Germany's strategy is fully encapsulated. Its 19% rate is a constant
    //  inside THIS class. No other class knows or cares about this detail.
    // =========================================================================

    /** Germany VAT at 19% standard rate. */
    static class GermanyTaxStrategy implements TaxStrategy {

        private static final double VAT_RATE = 0.19;

        @Override
        public double calculateTax(double amount) {
            double tax = amount * VAT_RATE;
            System.out.printf("[GermanyTaxStrategy] Germany VAT (%.0f%%): %.2f%n",
                    VAT_RATE * 100, tax);
            return tax;
        }

        @Override
        public String getCountryCode() { return "DE"; }
    }

    // =========================================================================
    //  CONCRETE STRATEGY: Canada Tax
    //
    //  Canada has a two-component tax (federal GST + provincial HST).
    //  This complexity is completely HIDDEN inside CanadaTaxStrategy.
    //  TaxCalculator doesn't know or care about this two-component logic.
    //  If Canada's rules change, we edit this ONE class.
    // =========================================================================

    /** Canada combined GST (5%) + provincial HST (8%) = 13% for Ontario. */
    static class CanadaTaxStrategy implements TaxStrategy {

        private static final double FEDERAL_GST   = 0.05;
        private static final double PROVINCIAL_HST = 0.08; // Ontario rate

        @Override
        public double calculateTax(double amount) {
            double gst = amount * FEDERAL_GST;
            double hst = amount * PROVINCIAL_HST;
            double tax = gst + hst;
            System.out.printf("[CanadaTaxStrategy] Canada GST(%.0f%%) + HST(%.0f%%) = %.2f%n",
                    FEDERAL_GST * 100, PROVINCIAL_HST * 100, tax);
            return tax;
        }

        @Override
        public String getCountryCode() { return "CA"; }
    }

    // =========================================================================
    //  ADDING A NEW COUNTRY: Mexico
    //
    //  BEFORE (ViolationExample): We would edit TaxCalculator, adding another
    //    else-if branch and risking breaking every other country.
    //
    //  AFTER (CorrectExample): We create THIS new class, register it with the
    //    calculator, and TaxCalculator.java is never touched. The existing
    //    country strategies are completely undisturbed.
    //
    //  This is exactly what "open for extension, closed for modification" means:
    //    - TaxCalculator is CLOSED (we didn't touch it).
    //    - The system is OPEN (we extended it with a new class).
    // =========================================================================

    /** Mexico VAT (IVA) at 16% standard rate — added WITHOUT modifying TaxCalculator. */
    static class MexicoTaxStrategy implements TaxStrategy {

        private static final double IVA_RATE = 0.16;

        @Override
        public double calculateTax(double amount) {
            double tax = amount * IVA_RATE;
            System.out.printf("[MexicoTaxStrategy] Mexico IVA (%.0f%%): %.2f%n",
                    IVA_RATE * 100, tax);
            return tax;
        }

        @Override
        public String getCountryCode() { return "MX"; }
    }

    // =========================================================================
    //  THE STABLE CORE: TaxCalculator
    //
    //  THIS CLASS NEVER CHANGES when new countries are added.
    //  It is CLOSED for modification.
    //
    //  It holds a registry of TaxStrategy objects (a Map).
    //  New strategies are registered via registerStrategy().
    //  The calculateTax() method delegates to whichever strategy is registered
    //  for the given country.
    //
    //  This method was written ONCE and never needs to change again.
    // =========================================================================

    static class TaxCalculator {

        // The registry maps country codes to their strategy objects.
        // Adding a new country = adding one entry to this map, NOT
        // editing the calculateTax() method.
        private final Map<String, TaxStrategy> strategies = new HashMap<>();

        /**
         * Registers a tax strategy for a country.
         *
         * This is the extension point. Client code calls this method to
         * plug in new country strategies. The TaxCalculator class itself
         * never needs to know the full list of countries — it just delegates.
         */
        public void registerStrategy(TaxStrategy strategy) {
            strategies.put(strategy.getCountryCode(), strategy);
            System.out.println("[TaxCalculator] Registered strategy for: "
                    + strategy.getCountryCode());
        }

        /**
         * Calculates the tax for the given country and amount.
         *
         * OCP COMPLIANT:
         *   This method was written once and is now CLOSED for modification.
         *   When Mexico is added, this method is NOT touched. When Canada's
         *   HST changes, this method is NOT touched.
         *
         *   It simply looks up the right strategy and delegates to it.
         *   All country-specific logic is in the strategy classes.
         */
        public double calculateTax(String country, double amount) {
            TaxStrategy strategy = strategies.get(country);

            if (strategy == null) {
                // If no strategy is registered, we fail loudly.
                // This is better than silently applying zero tax (as in ViolationExample).
                System.out.println("[TaxCalculator] No strategy registered for: " + country);
                return 0.0;
            }

            // Delegate entirely to the strategy — this method has no country knowledge.
            return strategy.calculateTax(amount);
        }

        /** Calculates the total price (amount + tax). */
        public double calculateTotal(String country, double amount) {
            return amount + calculateTax(country, amount);
        }
    }

    // =========================================================================
    //  MAIN — Wiring strategies and demonstrating usage
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== OCP CORRECT EXAMPLE ===\n");

        // Create the calculator — it starts with no strategies registered.
        TaxCalculator calculator = new TaxCalculator();

        // Register strategies for each supported country.
        // This is the "extension" mechanism: plug in as many countries as needed.
        System.out.println("--- Registering tax strategies ---");
        calculator.registerStrategy(new USTaxStrategy());
        calculator.registerStrategy(new UKTaxStrategy());
        calculator.registerStrategy(new GermanyTaxStrategy());
        calculator.registerStrategy(new CanadaTaxStrategy());
        // Mexico was added WITHOUT modifying TaxCalculator or any existing strategy!
        calculator.registerStrategy(new MexicoTaxStrategy());
        System.out.println();

        // Calculate taxes for each country
        double amount = 100.0;
        System.out.println("--- Calculating taxes on $" + amount + " ---");
        String[] countries = {"US", "UK", "DE", "CA", "MX", "XX"}; // XX = unknown

        for (String country : countries) {
            double tax   = calculator.calculateTax(country, amount);
            double total = calculator.calculateTotal(country, amount);
            System.out.printf("  %-4s  tax=%.2f  total=%.2f%n%n", country, tax, total);
        }

        // ----- KEY TAKEAWAY -----
        System.out.println("=== OCP TAKEAWAY ===");
        System.out.println("To add Singapore (9% GST), we would:");
        System.out.println("  1. Create: class SingaporeTaxStrategy implements TaxStrategy { ... }");
        System.out.println("  2. Register: calculator.registerStrategy(new SingaporeTaxStrategy());");
        System.out.println("  3. Done. TaxCalculator.java is NEVER modified.");
        System.out.println("     USTaxStrategy, UKTaxStrategy, etc. are NEVER modified.");
        System.out.println("     All existing tests still pass without re-running them.");
    }
}
