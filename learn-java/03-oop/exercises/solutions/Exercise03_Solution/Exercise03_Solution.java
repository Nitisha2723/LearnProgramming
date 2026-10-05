import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Exercise 03 Solution: Payment System
 */
public class Exercise03_Solution {

    public static void main(String[] args) {
        System.out.println("Exercise 03 Solution: Payment System");
        System.out.println("=".repeat(50));

        ShoppingCart cart = new ShoppingCart("Alice");
        cart.addItem("Java Programming Book", 49.99, 1);
        cart.addItem("USB-C Cable", 12.99, 2);
        cart.addItem("Laptop Stand", 35.00, 1);
        cart.displayCart();

        System.out.println("\n--- Checkout with Credit Card ---");
        Payable creditCard = new CreditCardPayment(
            "1234567890123456", "Alice Smith", 12, 2026, "123");
        cart.checkout(creditCard);

        System.out.println("\n--- Checkout with PayPal ---");
        Payable paypal = new PayPalPayment("alice@example.com", 500.0);
        cart.checkout(paypal);

        System.out.println("\n--- Checkout with Crypto ---");
        Payable crypto = new CryptoPayment("0xABC123DEF456789", "ETH", 2000.0);
        cart.checkout(crypto);

        System.out.println("\n--- Testing isValid() ---");
        CreditCardPayment expiredCard = new CreditCardPayment(
            "9876543210987654", "Bob Jones", 1, 2020, "456");
        System.out.println("Expired card is valid: " + expiredCard.isValid());
        System.out.println("Active card is valid: " + creditCard.isValid());

        System.out.println("\n--- Testing Insufficient PayPal Balance ---");
        PayPalPayment lowBalance = new PayPalPayment("bob@example.com", 5.0);
        try {
            lowBalance.process(100.0);
        } catch (IllegalStateException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println("\n--- Default Method: displayPaymentInfo() ---");
        creditCard.displayPaymentInfo();
        paypal.displayPaymentInfo();
        crypto.displayPaymentInfo();

        System.out.println("\n--- Testing Empty Cart ---");
        ShoppingCart emptyCart = new ShoppingCart("Bob");
        try {
            emptyCart.checkout(creditCard);
        } catch (IllegalStateException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }
    }

    // ====================================================================
    // Payable interface
    // ====================================================================

    interface Payable {
        void process(double amount);
        String getPaymentMethod();
        boolean isValid();

        default void displayPaymentInfo() {
            System.out.println("Processing payment via: " + getPaymentMethod());
        }
    }

    // ====================================================================
    // CreditCardPayment
    // ====================================================================

    static class CreditCardPayment implements Payable {
        private final String cardNumber;      // Full number stored securely
        private final String cardholderName;
        private final int expiryMonth;
        private final int expiryYear;
        private final String cvv;             // Never printed

        public CreditCardPayment(String cardNumber, String cardholderName,
                                  int expiryMonth, int expiryYear, String cvv) {
            if (cardNumber == null || cardNumber.length() != 16) {
                throw new IllegalArgumentException("Card number must be 16 digits");
            }
            this.cardNumber = cardNumber;
            this.cardholderName = cardholderName;
            this.expiryMonth = expiryMonth;
            this.expiryYear = expiryYear;
            this.cvv = cvv;
        }

        @Override
        public void process(double amount) {
            if (!isValid()) {
                throw new IllegalStateException("Card is expired");
            }
            String lastFour = cardNumber.substring(cardNumber.length() - 4);
            System.out.printf("Charged $%.2f to card ending in %s (Cardholder: %s)%n",
                             amount, lastFour, cardholderName);
        }

        @Override
        public String getPaymentMethod() {
            String lastFour = cardNumber.substring(cardNumber.length() - 4);
            return "Credit Card (**** **** **** " + lastFour + ")";
        }

        @Override
        public boolean isValid() {
            int currentYear = LocalDate.now().getYear();
            int currentMonth = LocalDate.now().getMonthValue();
            if (expiryYear > currentYear) return true;
            if (expiryYear == currentYear && expiryMonth >= currentMonth) return true;
            return false;
        }
    }

    // ====================================================================
    // PayPalPayment
    // ====================================================================

    static class PayPalPayment implements Payable {
        private final String email;
        private double balance;

        public PayPalPayment(String email, double balance) {
            if (email == null || !email.contains("@")) {
                throw new IllegalArgumentException("Invalid email address");
            }
            this.email = email;
            this.balance = balance;
        }

        @Override
        public void process(double amount) {
            if (amount > balance) {
                throw new IllegalStateException(
                    String.format("Insufficient PayPal balance. Have: $%.2f, Need: $%.2f",
                                 balance, amount));
            }
            balance -= amount;
            System.out.printf("Paid $%.2f via PayPal (%s). Remaining balance: $%.2f%n",
                             amount, email, balance);
        }

        @Override
        public String getPaymentMethod() {
            return "PayPal (" + email + ")";
        }

        @Override
        public boolean isValid() {
            return email != null && email.contains("@");
        }

        public double getBalance() { return balance; }
    }

    // ====================================================================
    // CryptoPayment
    // ====================================================================

    static class CryptoPayment implements Payable {
        private final String walletAddress;
        private final String currency;
        private final double exchangeRate;  // USD per 1 unit of currency

        public CryptoPayment(String walletAddress, String currency, double exchangeRate) {
            if (walletAddress == null || walletAddress.trim().isEmpty()) {
                throw new IllegalArgumentException("Wallet address cannot be empty");
            }
            this.walletAddress = walletAddress;
            this.currency = currency;
            this.exchangeRate = exchangeRate;
        }

        @Override
        public void process(double amount) {
            double cryptoAmount = amount / exchangeRate;
            // Show abbreviated address for security
            String shortAddress = walletAddress.length() > 10
                ? walletAddress.substring(0, 6) + "..." + walletAddress.substring(walletAddress.length() - 4)
                : walletAddress;
            System.out.printf("Sent %.6f %s ($%.2f) to wallet %s%n",
                             cryptoAmount, currency, amount, shortAddress);
        }

        @Override
        public String getPaymentMethod() {
            return currency + " Wallet";
        }

        @Override
        public boolean isValid() {
            return walletAddress != null && !walletAddress.trim().isEmpty();
        }
    }

    // ====================================================================
    // ShoppingCart with CartItem
    // ====================================================================

    static class ShoppingCart {
        private final String customerName;
        private final List<CartItem> items;

        public ShoppingCart(String customerName) {
            this.customerName = customerName;
            this.items = new ArrayList<>();
        }

        public void addItem(String name, double price, int quantity) {
            if (price <= 0) throw new IllegalArgumentException("Price must be positive");
            if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
            items.add(new CartItem(name, price, quantity));
        }

        public boolean removeItem(String name) {
            return items.removeIf(item -> item.name.equalsIgnoreCase(name));
        }

        public double getTotal() {
            double total = 0;
            for (CartItem item : items) {
                total += item.price * item.quantity;
            }
            return total;
        }

        public void displayCart() {
            System.out.println("\nShopping Cart for: " + customerName);
            System.out.println("-".repeat(50));
            for (CartItem item : items) {
                System.out.printf("  %-30s %dx $%.2f = $%.2f%n",
                                 item.name, item.quantity, item.price,
                                 item.price * item.quantity);
            }
            System.out.println("-".repeat(50));
            System.out.printf("  TOTAL: $%.2f%n", getTotal());
        }

        // The KEY method: accepts any Payable — Credit Card, PayPal, Crypto, or future types
        public void checkout(Payable payment) {
            if (items.isEmpty()) {
                throw new IllegalStateException("Cannot checkout with an empty cart");
            }
            displayCart();
            System.out.println("\n" + customerName + " is checking out...");
            payment.displayPaymentInfo();
            payment.process(getTotal());
            System.out.println("Checkout complete!");
        }

        // Private inner class — CartItem is an implementation detail of ShoppingCart
        private static class CartItem {
            final String name;
            final double price;
            final int quantity;

            CartItem(String name, double price, int quantity) {
                this.name = name;
                this.price = price;
                this.quantity = quantity;
            }
        }
    }
}
