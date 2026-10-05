package solid.ocp;

/**
 * ============================================================
 *  SOLID PRINCIPLE #2 — Open/Closed Principle (OCP)
 * ============================================================
 *
 *  DEFINITION:
 *    "Software entities (classes, modules, functions) should be OPEN for
 *     extension, but CLOSED for modification."
 *    — Bertrand Meyer (1988), popularised by Robert C. Martin
 *
 *  What does "open for extension, closed for modification" mean?
 *    - OPEN for extension:   You can add new behavior.
 *    - CLOSED for modification: Adding new behavior does NOT require editing
 *      existing, tested, deployed code.
 *
 *  The goal is to allow growth WITHOUT changing what already works.
 *  If every new feature requires modifying existing classes, those classes
 *  can never be truly stable. Bug fixes in one branch of an if/else can
 *  silently break another branch.
 *
 *  THE VIOLATION BELOW:
 *    TaxCalculator uses a giant if/else-if chain that must be MODIFIED every
 *    time a new country's tax rules need to be supported.
 *
 *    Problems:
 *    1. To add "Japan", you must edit TaxCalculator — a class that is already
 *       tested and working. Every edit is a regression risk.
 *    2. The class grows without bound — eventually it handles 50+ countries,
 *       all in one enormous method.
 *    3. Multiple developers working on different countries will have MERGE
 *       CONFLICTS on this single file.
 *    4. Unit tests for "US tax" could break when someone edits "Germany tax"
 *       because they're in the same method.
 */
public class ViolationExample {

    // =========================================================================
    //  THE VIOLATING CLASS: TaxCalculator
    //
    //  Each country's tax rules are hard-coded here with if/else.
    //  Adding a new country = modifying this class = OCP violation.
    // =========================================================================

    static class TaxCalculator {

        /**
         * Calculates the tax amount for a given country.
         *
         * OCP VIOLATION:
         *   Every time a new country must be supported, a developer must:
         *     1. Find this method (already complex).
         *     2. Add a new "else if" branch.
         *     3. Re-test ALL countries (the change is inside an existing method).
         *     4. Risk accidentally modifying the wrong branch.
         *
         *   This method is NEVER CLOSED for modification. It changes every time
         *   the business decides to operate in a new country.
         *
         * @param country  The country code (e.g., "US", "UK")
         * @param amount   The pre-tax purchase amount
         * @return The tax amount (NOT the total — just the tax portion)
         */
        public double calculateTax(String country, double amount) {

            // ---------------------------------------------------------------
            //  Adding a new country requires editing this if/else chain.
            //  Each "else if" is a ticking time bomb — any edit here could
            //  accidentally change the logic for an already-working country.
            // ---------------------------------------------------------------

            if ("US".equals(country)) {
                // US: Simple federal rate of 10% (ignoring state taxes for brevity)
                // WHY THIS IS WRONG: US tax logic is buried in a shared method.
                // If US changes their rate, we edit this already-complex method.
                double taxRate = 0.10;
                double tax = amount * taxRate;
                System.out.println("[TaxCalculator] US tax (10%): " + tax);
                return tax;

            } else if ("UK".equals(country)) {
                // UK: Value Added Tax (VAT) at 20% standard rate
                // WHY THIS IS WRONG: UK logic is adjacent to US logic in the same
                // method. A bug fix for UK might accidentally touch US code.
                double vatRate = 0.20;
                double tax = amount * vatRate;
                System.out.println("[TaxCalculator] UK VAT (20%): " + tax);
                return tax;

            } else if ("DE".equals(country)) {
                // Germany: VAT at 19% standard rate
                // WHY THIS IS WRONG: Imagine Germany changes their rate due to
                // an EU regulation. We must open THIS class, find this branch,
                // change 0.19 to something else, and re-run all country tests.
                double vatRate = 0.19;
                double tax = amount * vatRate;
                System.out.println("[TaxCalculator] Germany VAT (19%): " + tax);
                return tax;

            } else if ("FR".equals(country)) {
                // France: VAT at 20% standard rate
                // WHY THIS IS WRONG: Same rate as UK but different rules for
                // certain goods. More complexity piling into one method.
                double vatRate = 0.20;
                double tax = amount * vatRate;
                System.out.println("[TaxCalculator] France VAT (20%): " + tax);
                return tax;

            } else if ("JP".equals(country)) {
                // Japan: Consumption tax at 10% (8% for food — simplified here)
                // WHY THIS IS WRONG: Japan has special rules (reduced rate for
                // food/beverages). This exception logic clutters the main method.
                double taxRate = 0.10;
                double tax = amount * taxRate;
                System.out.println("[TaxCalculator] Japan consumption tax (10%): " + tax);
                return tax;

            } else if ("AU".equals(country)) {
                // Australia: Goods and Services Tax (GST) at 10%
                // WHY THIS IS WRONG: AU, JP, and US all happen to be 10%, but
                // for completely different legal reasons. Mixing them in one
                // method obscures these distinctions.
                double gstRate = 0.10;
                double tax = amount * gstRate;
                System.out.println("[TaxCalculator] Australia GST (10%): " + tax);
                return tax;

            } else if ("CA".equals(country)) {
                // Canada: Federal GST 5% + provincial HST varies (simplified: 13%)
                // WHY THIS IS WRONG: Canada has a two-component tax. The formula
                // is different from all other countries. This inconsistency is
                // hidden inside a generic calculateTax() method.
                double federalGst = amount * 0.05;
                double provincialHst = amount * 0.08; // simplified Ontario HST
                double tax = federalGst + provincialHst;
                System.out.println("[TaxCalculator] Canada GST+HST (13%): " + tax);
                return tax;

            } else if ("BR".equals(country)) {
                // Brazil: Extremely complex tax system — simplified to ~30%
                // WHY THIS IS WRONG: Brazil's tax system involves ICMS, IPI, PIS,
                // COFINS and more. Putting even a simplified version here makes
                // this method enormously complex. Real Brazilian tax logic could
                // be hundreds of lines — all jammed into one class.
                double taxRate = 0.30;
                double tax = amount * taxRate;
                System.out.println("[TaxCalculator] Brazil combined taxes (~30%): " + tax);
                return tax;

            } else if ("IN".equals(country)) {
                // India: GST at 18% (standard rate; varies by category)
                // WHY THIS IS WRONG: India's GST rates vary from 0% to 28% by
                // product category. Handling this properly requires a lookup table
                // or strategy — not another if/else branch in a general method.
                double gstRate = 0.18;
                double tax = amount * gstRate;
                System.out.println("[TaxCalculator] India GST (18%): " + tax);
                return tax;

            } else {
                // ---------------------------------------------------------------
                //  IMAGINE THE NEXT DEVELOPER says:
                //    "We need to support Mexico (16% VAT), China (13% VAT),
                //     South Korea (10% VAT), and Singapore (9% GST)."
                //
                //  They must:
                //    1. Open this already-large method.
                //    2. Add four more "else if" branches.
                //    3. Worry about accidentally touching existing branches.
                //    4. Ask QA to regression-test ALL countries again.
                //
                //  This is the OCP violation: the class is NEVER CLOSED.
                //  Every new country reopens and risks breaking this method.
                // ---------------------------------------------------------------
                System.out.println("[TaxCalculator] Unknown country: " + country + " — no tax applied.");
                return 0.0;
            }
        }

        /**
         * Calculates the total price including tax.
         *
         * OCP VIOLATION (secondary):
         *   This method delegates to calculateTax(), so it also inherits the
         *   violation. But it also shows something subtle: if we need special
         *   total-calculation logic per country (e.g., tax-inclusive pricing
         *   in Australia), that would require even MORE if/else here.
         */
        public double calculateTotal(String country, double amount) {
            double tax = calculateTax(country, amount);
            return amount + tax;
        }
    }

    // =========================================================================
    //  MAIN — Demonstrating the OCP violation
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== OCP VIOLATION DEMO ===\n");

        TaxCalculator calc = new TaxCalculator();
        double amount = 100.0;

        System.out.println("Calculating taxes on $" + amount + ":\n");

        // These work fine — but adding any new country requires modifying the
        // calculateTax() method, which is the violation.
        String[] countries = {"US", "UK", "DE", "FR", "JP", "AU", "CA", "BR", "IN", "MX"};

        for (String country : countries) {
            double tax   = calc.calculateTax(country, amount);
            double total = calc.calculateTotal(country, amount);
            System.out.printf("  %-4s  tax=%.2f  total=%.2f%n", country, tax, total);
        }

        System.out.println();
        System.out.println("PROBLEM: To add 'MX' (Mexico), we had to modify TaxCalculator.");
        System.out.println("This means TaxCalculator is never stable — it changes with every");
        System.out.println("new country. Any change risks breaking existing country logic.");
        System.out.println("\nSee CorrectExample.java for the OCP-compliant solution.");
    }
}
