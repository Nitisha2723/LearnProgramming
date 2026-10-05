/**
 * NotificationFactory.java — Factory Pattern
 *
 * Encapsulates the complex logic of creating pre-configured notifications
 * for common e-commerce events.
 *
 * FACTORY PATTERN BENEFITS:
 *   - Caller doesn't need to know the builder API in detail
 *   - Common notification formats are standardised
 *   - Changing order confirmation format requires one change here, not everywhere
 */

public class NotificationFactory {

    /**
     * Create an order confirmation notification (sent via email).
     */
    public static Notification createOrderConfirmation(String email, String orderId, double amount) {
        return new Notification.Builder()
            .recipient(email)
            .channel(Notification.Channel.EMAIL)
            .priority(Notification.Priority.NORMAL)
            .subject("Your order " + orderId + " is confirmed!")
            .body(String.format(
                "Thank you for your order!%n%n" +
                "Order ID:  %s%n" +
                "Amount:    $%.2f%n%n" +
                "We'll send you tracking information as soon as your order ships.%n" +
                "Thank you for shopping with us!",
                orderId, amount))
            .metadata("orderId", orderId)
            .metadata("type", "ORDER_CONFIRMATION")
            .build();
    }

    /**
     * Create a shipping alert notification (sent via SMS).
     */
    public static Notification createShippingAlert(String phone, String orderId, String trackingNumber) {
        return new Notification.Builder()
            .recipient(phone)
            .channel(Notification.Channel.SMS)
            .priority(Notification.Priority.HIGH)
            .subject("Your order has shipped!")
            .body(String.format("Order %s shipped! Track: %s. Estimated 3-5 days.",
                orderId, trackingNumber))
            .metadata("orderId", orderId)
            .metadata("trackingNumber", trackingNumber)
            .metadata("type", "SHIPPING_ALERT")
            .build();
    }

    /**
     * Create a password reset notification (sent via email, HIGH priority).
     */
    public static Notification createPasswordReset(String email, String resetToken) {
        return new Notification.Builder()
            .recipient(email)
            .channel(Notification.Channel.EMAIL)
            .priority(Notification.Priority.HIGH)
            .subject("Reset your password")
            .body(String.format(
                "We received a request to reset your password.%n%n" +
                "Use this link to reset it (valid for 1 hour):%n" +
                "https://shop.example.com/reset-password?token=%s%n%n" +
                "If you didn't request this, you can safely ignore this email.",
                resetToken))
            .metadata("type", "PASSWORD_RESET")
            .build();
    }

    /**
     * Create a promotional email with a discount code.
     */
    public static Notification createPromotion(String email, String promoCode, double discountPercent) {
        return new Notification.Builder()
            .recipient(email)
            .channel(Notification.Channel.EMAIL)
            .priority(Notification.Priority.LOW)
            .subject(String.format("%.0f%% off your next order — Limited time!", discountPercent))
            .body(String.format(
                "Hi there!%n%n" +
                "We're giving you %.0f%% off your next order.%n%n" +
                "Use code: %s%n%n" +
                "Offer expires in 48 hours. Shop now!",
                discountPercent, promoCode))
            .metadata("promoCode", promoCode)
            .metadata("type", "PROMOTION")
            .build();
    }

    /**
     * Create a critical security alert notification.
     */
    public static Notification createSecurityAlert(String email, String message) {
        return new Notification.Builder()
            .recipient(email)
            .channel(Notification.Channel.EMAIL)
            .priority(Notification.Priority.CRITICAL)
            .subject("SECURITY ALERT: Action required")
            .body("IMPORTANT: " + message + "\n\nIf this was not you, please contact support immediately.")
            .metadata("type", "SECURITY_ALERT")
            .build();
    }
}
