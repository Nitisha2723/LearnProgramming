import java.util.ArrayList;
import java.util.List;

/**
 * Exercise 03: Payment System
 * ============================================================
 *
 * BUILD A PAYMENT PROCESSING SYSTEM using interfaces.
 *
 * LEARNING GOALS:
 * - Define and implement interfaces
 * - Understand why interfaces enable flexible, extensible design
 * - Write code that works with any implementation of an interface
 * - Apply the Open/Closed Principle (open for extension, closed for modification)
 *
 * ============================================================
 * PART A: Define the Payable interface
 * ============================================================
 *
 * A Payable can:
 *   - process(double amount): processes a payment
 *   - getPaymentMethod(): returns a String describing the payment method
 *   - boolean isValid(): returns whether this payment method is valid/configured
 *
 * Add a default method:
 *   - displayPaymentInfo(): prints "Processing payment via [getPaymentMethod()]"
 *
 * ============================================================
 * PART B: Implement CreditCardPayment implements Payable
 * ============================================================
 *
 * Fields:
 *   - cardNumber (String): 16-digit card number
 *   - cardholderName (String): name on the card
 *   - expiryMonth (int): 1-12
 *   - expiryYear (int): 4-digit year
 *   - cvv (String): 3-digit security code (NEVER print this)
 *
 * process(amount): prints the last 4 digits only (security)
 *   e.g., "Charged $25.00 to card ending in 3456"
 *
 * isValid(): card is valid if not expired (check against current year/month)
 *
 * getPaymentMethod(): "Credit Card (**** **** **** 3456)"
 *
 * ============================================================
 * PART C: Implement PayPalPayment implements Payable
 * ============================================================
 *
 * Fields:
 *   - email (String): PayPal account email
 *   - balance (double): current PayPal balance
 *
 * process(amount):
 *   - If balance >= amount: deduct from balance and confirm
 *   - If insufficient: throw IllegalStateException("Insufficient PayPal balance")
 *
 * isValid(): returns true if email contains "@"
 *
 * getPaymentMethod(): "PayPal (" + email + ")"
 *
 * ============================================================
 * PART D: Implement CryptoPayment implements Payable
 * ============================================================
 *
 * Fields:
 *   - walletAddress (String): crypto wallet address
 *   - currency (String): e.g., "BTC", "ETH"
 *   - exchangeRate (double): USD per unit of currency
 *
 * process(amount):
 *   - Convert USD amount to crypto: cryptoAmount = amount / exchangeRate
 *   - Print: "Sent 0.000123 BTC to wallet 0xABC..."
 *
 * isValid(): returns true if walletAddress is not empty
 *
 * getPaymentMethod(): currency + " Wallet"
 *
 * ============================================================
 * PART E: Implement ShoppingCart
 * ============================================================
 *
 * Fields:
 *   - items (List<CartItem>): list of items
 *   - customerName (String)
 *
 * Inner class CartItem:
 *   - name (String): product name
 *   - price (double): unit price
 *   - quantity (int)
 *
 * Methods:
 *   - addItem(String name, double price, int quantity)
 *   - removeItem(String name)
 *   - getTotal(): sum of all item prices * quantities
 *   - displayCart(): prints all items with totals
 *   - checkout(Payable payment): displays cart, then processes payment
 *                                Throws if cart is empty
 *
 * KEY INSIGHT: checkout(Payable payment) works with ANY payment type!
 * You never need to modify ShoppingCart to support a new payment method.
 *
 * ============================================================
 * STRETCH GOALS:
 * ============================================================
 *
 * 1. Add a Refundable interface with refund(double amount)
 *    - CreditCardPayment implements Refundable (credit card refunds work)
 *    - CryptoPayment does NOT implement Refundable (no chargebacks on crypto)
 *
 * 2. Add a Loggable interface that all payments implement
 *    with getTransactionLog() returning the payment history
 *
 * 3. Create an ApplePayPayment class — the ShoppingCart.checkout() needs
 *    ZERO changes to work with it. This is the Open/Closed Principle!
 */
public class Exercise03_PaymentSystem {

    public static void main(String[] args) {
        System.out.println("Exercise 03: Payment System");
        System.out.println("=".repeat(50));

        // ====================================================================
        // TEST YOUR IMPLEMENTATION BY UNCOMMENTING THESE TESTS
        // ====================================================================

        // --- Build a shopping cart ---
        // ShoppingCart cart = new ShoppingCart("Alice");
        // cart.addItem("Java Programming Book", 49.99, 1);
        // cart.addItem("USB-C Cable", 12.99, 2);
        // cart.addItem("Laptop Stand", 35.00, 1);
        // cart.displayCart();

        // --- Test with Credit Card ---
        // Payable creditCard = new CreditCardPayment(
        //     "1234567890123456", "Alice Smith", 12, 2026, "123");
        // System.out.println("\nChecking out with credit card:");
        // cart.checkout(creditCard);

        // --- Test with PayPal ---
        // Payable paypal = new PayPalPayment("alice@example.com", 200.0);
        // System.out.println("\nChecking out with PayPal:");
        // cart.checkout(paypal);

        // --- Test with Crypto ---
        // Payable crypto = new CryptoPayment("0xABC123DEF456", "ETH", 2000.0);
        // System.out.println("\nChecking out with Crypto:");
        // cart.checkout(crypto);

        // --- Test isValid() ---
        // CreditCardPayment expiredCard = new CreditCardPayment(
        //     "9876543210987654", "Bob Jones", 1, 2020, "456");  // Expired!
        // System.out.println("\nExpired card is valid: " + expiredCard.isValid());

        // --- Test PayPal insufficient balance ---
        // PayPalPayment lowBalance = new PayPalPayment("bob@example.com", 5.0);
        // try {
        //     lowBalance.process(100.0);  // Should throw
        // } catch (IllegalStateException e) {
        //     System.out.println("Correctly rejected: " + e.getMessage());
        // }

        // --- Test displayPaymentInfo() default method ---
        // creditCard.displayPaymentInfo();
        // paypal.displayPaymentInfo();
        // crypto.displayPaymentInfo();

        System.out.println("\nImplement Payable, CreditCardPayment, PayPalPayment, CryptoPayment,");
        System.out.println("and ShoppingCart above main(), then uncomment the tests.");
    }

    // ====================================================================
    // IMPLEMENT YOUR INTERFACE AND CLASSES BELOW THIS LINE
    // ====================================================================

    // TODO: Define Payable interface
    // Methods: process(double amount), getPaymentMethod(), isValid()
    // Default method: displayPaymentInfo()

    // TODO: Implement CreditCardPayment implements Payable
    // IMPORTANT: Never print the full card number or CVV!

    // TODO: Implement PayPalPayment implements Payable

    // TODO: Implement CryptoPayment implements Payable

    // TODO: Implement ShoppingCart with inner class CartItem
    // KEY: checkout(Payable payment) — notice it takes a Payable, not a specific type!
}
