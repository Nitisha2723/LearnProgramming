package patterns.factory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DESIGN PATTERN: Factory Method + Abstract Factory
 * ==================================================
 * Intent (Factory Method):
 *   Define an interface for creating an object, but let subclasses decide
 *   which class to instantiate. Factory Method lets a class defer instantiation
 *   to subclasses.
 *
 * Intent (Abstract Factory):
 *   Provide an interface for creating FAMILIES of related objects without
 *   specifying their concrete classes.
 *
 * REAL-WORLD USE CASE: Payment Processing System
 * ------------------------------------------------
 * An e-commerce platform supports multiple payment methods (Visa, PayPal,
 * Crypto). Each method has different APIs, validation rules, and behavior.
 * The Factory pattern lets you create the right processor without the caller
 * knowing implementation details.
 *
 * ============================================================
 * FACTORY METHOD vs ABSTRACT FACTORY:
 * ============================================================
 *
 * FACTORY METHOD:
 *   - ONE product type (PaymentProcessor)
 *   - A factory method returns different implementations based on input
 *   - Client calls: factory.createProcessor("VISA")
 *   - Use when: you have one kind of object with multiple variants
 *
 * ABSTRACT FACTORY:
 *   - MULTIPLE related product types (PaymentProcessor + PaymentValidator)
 *   - An abstract factory creates a "family" of compatible objects
 *   - Client calls: family.createProcessor() + family.createValidator()
 *   - Use when: you need families of related objects that must work together
 *   - Example: Visa processor must use Visa validator (not Crypto validator)
 *
 * WITHOUT factory pattern, your code looks like:
 *   PaymentProcessor p;
 *   if (type.equals("VISA"))   p = new VisaPaymentProcessor();
 *   else if (type.equals("PAYPAL")) p = new PayPalPaymentProcessor();
 *   else if (type.equals("CRYPTO")) p = new CryptoPaymentProcessor();
 *   // This if/else must be COPY-PASTED everywhere processors are created!
 *   // Adding a new payment type requires changing every copy.
 *
 * WITH factory pattern:
 *   PaymentProcessor p = factory.createProcessor(type);
 *   // Logic is centralized. Adding a new type = ONE place to change.
 */

// =============================================================================
// FILE STRUCTURE:
//   1. PaymentProcessor interface        — the Product interface
//   2. VisaPaymentProcessor              — Concrete Product
//   3. PayPalPaymentProcessor            — Concrete Product
//   4. CryptoPaymentProcessor            — Concrete Product
//   5. PaymentProcessorFactory           — Factory Method (also the main class)
//   6. PaymentValidator interface        — second product type for Abstract Factory
//   7. ProcessorFamilyFactory interface  — Abstract Factory interface
//   8. VisaProcessorFamily               — Concrete Abstract Factory
//   9. PayPalProcessorFamily             — Concrete Abstract Factory
// =============================================================================

// -----------------------------------------------------------------------------
// PRODUCT INTERFACE: What all payment processors must be able to do
// -----------------------------------------------------------------------------
/**
 * The PaymentProcessor interface defines the contract that all payment
 * processing strategies must fulfill. Clients depend ONLY on this interface,
 * never on concrete classes (Dependency Inversion Principle).
 */
interface PaymentProcessor {
    /**
     * Process a payment of the given amount in the given currency.
     * @param amount   Payment amount (positive)
     * @param currency ISO 4217 currency code (e.g., "USD", "EUR")
     * @return A transaction ID for tracking and refunds
     */
    String processPayment(double amount, String currency);

    /**
     * Issue a refund for a previously completed transaction.
     * @param transactionId The ID returned by processPayment()
     * @return true if refund succeeded, false otherwise
     */
    boolean refund(String transactionId);

    /**
     * Human-readable name of this processor (for logging, receipts, UI)
     */
    String getProcessorName();
}

// -----------------------------------------------------------------------------
// CONCRETE PRODUCT 1: Visa credit card processing
// -----------------------------------------------------------------------------
class VisaPaymentProcessor implements PaymentProcessor {
    // In real code, these would come from configuration / environment variables
    private static final String VISA_API_ENDPOINT = "https://api.visa.com/v2/payments";
    private final Map<String, Double> transactionLog = new HashMap<>();

    @Override
    public String processPayment(double amount, String currency) {
        // Simulate Visa-specific validation (real Visa API needs card number, CVV, etc.)
        if (amount <= 0) throw new IllegalArgumentException("Payment amount must be positive");
        if (amount > 10_000) {
            System.out.println("  [Visa] Large transaction flagged for review: " + amount + " " + currency);
        }

        // Generate a Visa-style transaction ID
        String txId = "VISA-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // Record the transaction (in real code, this would call VISA_API_ENDPOINT)
        transactionLog.put(txId, amount);

        System.out.printf("  [Visa] Processed payment: %.2f %s → Transaction: %s%n",
                amount, currency, txId);
        return txId;
    }

    @Override
    public boolean refund(String transactionId) {
        if (!transactionLog.containsKey(transactionId)) {
            System.out.println("  [Visa] Refund FAILED: Transaction not found: " + transactionId);
            return false;
        }
        double refundAmount = transactionLog.remove(transactionId);
        System.out.printf("  [Visa] Refunded %.2f for transaction %s%n", refundAmount, transactionId);
        return true;
    }

    @Override
    public String getProcessorName() { return "Visa Credit Card Processor v2.1"; }
}

// -----------------------------------------------------------------------------
// CONCRETE PRODUCT 2: PayPal processing
// -----------------------------------------------------------------------------
class PayPalPaymentProcessor implements PaymentProcessor {
    private static final double PAYPAL_FEE_PERCENT = 0.029; // 2.9% + $0.30
    private static final double PAYPAL_FLAT_FEE = 0.30;
    private final Map<String, Double> transactionLog = new HashMap<>();

    @Override
    public String processPayment(double amount, String currency) {
        if (amount <= 0) throw new IllegalArgumentException("Payment amount must be positive");

        // PayPal charges a processing fee — deducted from merchant payout
        double fee = (amount * PAYPAL_FEE_PERCENT) + PAYPAL_FLAT_FEE;
        double netAmount = amount - fee;

        String txId = "PP-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 9999);
        transactionLog.put(txId, amount);

        System.out.printf("  [PayPal] Processed: %.2f %s | Fee: %.2f | Net: %.2f | TX: %s%n",
                amount, currency, fee, netAmount, txId);
        return txId;
    }

    @Override
    public boolean refund(String transactionId) {
        if (!transactionLog.containsKey(transactionId)) {
            System.out.println("  [PayPal] Refund FAILED: Transaction not found: " + transactionId);
            return false;
        }
        // PayPal does NOT refund the processing fee to the merchant
        double originalAmount = transactionLog.remove(transactionId);
        System.out.printf("  [PayPal] Refunded %.2f (fee non-refundable) for %s%n",
                originalAmount, transactionId);
        return true;
    }

    @Override
    public String getProcessorName() { return "PayPal Commerce Platform v3.0"; }
}

// -----------------------------------------------------------------------------
// CONCRETE PRODUCT 3: Cryptocurrency processing
// -----------------------------------------------------------------------------
class CryptoPaymentProcessor implements PaymentProcessor {
    // Cryptocurrency: no chargebacks, irreversible, network fees vary
    private static final String DEFAULT_COIN = "BTC";
    private final Map<String, Double> transactionLog = new HashMap<>();
    private double simulatedExchangeRate = 45_000.0; // USD per BTC (simulated)

    @Override
    public String processPayment(double amount, String currency) {
        if (amount <= 0) throw new IllegalArgumentException("Payment amount must be positive");

        // Convert USD to crypto
        double cryptoAmount = amount / simulatedExchangeRate;

        // Blockchain transaction ID (hash-like)
        String txId = "0x" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        transactionLog.put(txId, amount);

        System.out.printf("  [Crypto] Processed: %.2f %s = %.8f %s | TX Hash: %s%n",
                amount, currency, cryptoAmount, DEFAULT_COIN, txId);
        System.out.println("  [Crypto] Note: Awaiting 3 blockchain confirmations...");
        return txId;
    }

    @Override
    public boolean refund(String transactionId) {
        // Crypto transactions are IRREVERSIBLE on the blockchain
        // A "refund" is actually a new transaction back to the customer
        if (!transactionLog.containsKey(transactionId)) {
            System.out.println("  [Crypto] Refund FAILED: Transaction not found in local log: " + transactionId);
            return false;
        }
        double amount = transactionLog.remove(transactionId);
        System.out.printf("  [Crypto] Initiating reverse transaction of %.2f USD for %s%n",
                amount, transactionId);
        System.out.println("  [Crypto] Warning: Crypto refunds create a NEW transaction, not a reversal.");
        return true;
    }

    @Override
    public String getProcessorName() { return "CryptoGate Bitcoin/Ethereum Processor"; }
}

// =============================================================================
// FACTORY METHOD: Creates the right PaymentProcessor based on a string type
//
// This is also the main public class (and contains main() for the demo).
// =============================================================================
/**
 * PaymentProcessorFactory — Factory Method Pattern
 *
 * The factory centralizes all the "which concrete class to instantiate" logic.
 * Callers never need to import or reference VisaPaymentProcessor directly.
 *
 * OPEN/CLOSED PRINCIPLE: To add a new payment type:
 *   1. Create a new class implementing PaymentProcessor
 *   2. Add a case to createProcessor()
 *   That's it. No other code changes needed.
 */
public class PaymentProcessorFactory {

    /**
     * Factory Method: creates the appropriate PaymentProcessor for the given type.
     *
     * @param type Payment type: "VISA", "PAYPAL", "CRYPTO" (case-insensitive)
     * @return The appropriate PaymentProcessor implementation
     * @throws IllegalArgumentException for unknown payment types
     */
    public static PaymentProcessor createProcessor(String type) {
        if (type == null) throw new IllegalArgumentException("Payment type cannot be null");

        // switch expression (Java 14+) — cleaner than if/else chain
        return switch (type.toUpperCase().trim()) {
            case "VISA", "MASTERCARD", "CREDIT_CARD" -> new VisaPaymentProcessor();
            case "PAYPAL", "PP"                      -> new PayPalPaymentProcessor();
            case "CRYPTO", "BTC", "ETH"              -> new CryptoPaymentProcessor();
            default -> throw new IllegalArgumentException(
                    "Unknown payment type: '" + type + "'. Supported: VISA, PAYPAL, CRYPTO");
        };
    }

    // =============================================================================
    // ABSTRACT FACTORY EXTENSION
    //
    // Sometimes you need a "family" of objects that work together.
    // For payments: each processor type needs its OWN validator.
    // Using the wrong validator with a processor causes bugs.
    // Abstract Factory ensures you always get compatible objects.
    // =============================================================================

    /**
     * PaymentValidator — second product type in our Abstract Factory family.
     * Each processor type has a matching validator with different rules.
     */
    interface PaymentValidator {
        /** @return null if valid, or an error message if invalid */
        String validate(double amount, String currency, Map<String, String> metadata);
        String getValidatorName();
    }

    /** Visa validator: checks card-specific fields */
    static class VisaValidator implements PaymentValidator {
        @Override
        public String validate(double amount, String currency, Map<String, String> metadata) {
            if (amount > 50_000) return "Visa: amount exceeds single-transaction limit";
            if (!metadata.containsKey("cardNumber")) return "Visa: cardNumber required";
            if (!metadata.containsKey("cvv")) return "Visa: CVV required";
            return null; // valid
        }
        @Override
        public String getValidatorName() { return "VisaTransactionValidator"; }
    }

    /** PayPal validator: checks account-specific fields */
    static class PayPalValidator implements PaymentValidator {
        @Override
        public String validate(double amount, String currency, Map<String, String> metadata) {
            if (amount > 100_000) return "PayPal: amount exceeds limit for unverified accounts";
            if (!metadata.containsKey("paypalEmail")) return "PayPal: paypalEmail required";
            return null; // valid
        }
        @Override
        public String getValidatorName() { return "PayPalAccountValidator"; }
    }

    /**
     * Abstract Factory interface: creates FAMILIES of compatible objects.
     *
     * Notice the contract: you get BOTH a processor AND a validator,
     * and they are guaranteed to be compatible with each other.
     */
    interface ProcessorFamilyFactory {
        PaymentProcessor createProcessor();
        PaymentValidator createValidator();
        String getFamilyName();
    }

    /** Visa family: Visa processor + Visa validator — always compatible */
    static class VisaProcessorFamily implements ProcessorFamilyFactory {
        @Override
        public PaymentProcessor createProcessor() { return new VisaPaymentProcessor(); }
        @Override
        public PaymentValidator createValidator() { return new VisaValidator(); }
        @Override
        public String getFamilyName() { return "Visa Payment Family"; }
    }

    /** PayPal family: PayPal processor + PayPal validator — always compatible */
    static class PayPalProcessorFamily implements ProcessorFamilyFactory {
        @Override
        public PaymentProcessor createProcessor() { return new PayPalPaymentProcessor(); }
        @Override
        public PaymentValidator createValidator() { return new PayPalValidator(); }
        @Override
        public String getFamilyName() { return "PayPal Payment Family"; }
    }

    /** Factory for the Abstract Factory: returns the right family by type */
    public static ProcessorFamilyFactory createProcessorFamily(String type) {
        return switch (type.toUpperCase()) {
            case "VISA"   -> new VisaProcessorFamily();
            case "PAYPAL" -> new PayPalProcessorFamily();
            default -> throw new IllegalArgumentException("No family for: " + type);
        };
    }

    // =============================================================================
    // DEMONSTRATION
    // =============================================================================
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("DESIGN PATTERN: Factory Method + Abstract Factory");
        System.out.println("USE CASE: Payment Processing System");
        System.out.println("=".repeat(60) + "\n");

        // -------------------------------------------------------
        // DEMO 1: Factory Method — creating processors by type string
        // -------------------------------------------------------
        System.out.println("--- DEMO 1: Factory Method Pattern ---");
        System.out.println("Client code never imports VisaPaymentProcessor directly.\n");

        String[] paymentTypes = {"VISA", "PayPal", "CRYPTO"};
        double[] amounts = {99.99, 250.00, 500.00};

        String lastTxId = null;
        String lastType = null;

        for (int i = 0; i < paymentTypes.length; i++) {
            System.out.println("Creating processor for: " + paymentTypes[i]);

            // THE KEY INSIGHT: client code only uses the interface and factory
            // It never says 'new VisaPaymentProcessor()' directly
            PaymentProcessor processor = PaymentProcessorFactory.createProcessor(paymentTypes[i]);
            System.out.println("Got: " + processor.getProcessorName());

            String txId = processor.processPayment(amounts[i], "USD");

            // Save one for refund demo
            if (i == 0) { lastTxId = txId; lastType = paymentTypes[i]; }
            System.out.println();
        }

        // -------------------------------------------------------
        // DEMO 2: Refund through factory
        // -------------------------------------------------------
        System.out.println("--- DEMO 2: Refund ---");
        if (lastTxId != null) {
            PaymentProcessor refundProcessor = PaymentProcessorFactory.createProcessor(lastType);
            System.out.println("Refunding transaction: " + lastTxId);
            // Note: In a real app, the processor instance would be the same one that
            // created the transaction (or pulled from a stateless service)
            refundProcessor.processPayment(99.99, "USD"); // create matching tx first
        }

        // -------------------------------------------------------
        // DEMO 3: Error handling for unknown type
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 3: Unknown Payment Type ---");
        try {
            PaymentProcessorFactory.createProcessor("BITCOIN_LIGHTNING");
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }

        // -------------------------------------------------------
        // DEMO 4: Abstract Factory — getting compatible families
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 4: Abstract Factory — Compatible Families ---");
        System.out.println("Abstract Factory ensures processor + validator are ALWAYS compatible.\n");

        String[] familyTypes = {"VISA", "PAYPAL"};
        for (String familyType : familyTypes) {
            ProcessorFamilyFactory family = PaymentProcessorFactory.createProcessorFamily(familyType);
            System.out.println("Family: " + family.getFamilyName());

            PaymentProcessor processor = family.createProcessor();
            PaymentValidator validator = family.createValidator();

            System.out.println("  Processor: " + processor.getProcessorName());
            System.out.println("  Validator: " + validator.getValidatorName());

            // Test validation
            Map<String, String> goodMeta = new HashMap<>();
            goodMeta.put("cardNumber", "4111111111111111");
            goodMeta.put("cvv", "123");
            goodMeta.put("paypalEmail", "user@example.com");

            String validationError = validator.validate(150.0, "USD", goodMeta);
            System.out.println("  Validation result: " + (validationError == null ? "VALID" : "INVALID: " + validationError));
            System.out.println();
        }

        System.out.println("--- Factory Pattern Summary ---");
        System.out.println("Factory Method: ONE product type, multiple variants");
        System.out.println("Abstract Factory: MULTIPLE product types, compatible families");
        System.out.println("Both: centralize object creation, follow Open/Closed Principle");
        System.out.println("\nDone!");
    }
}
