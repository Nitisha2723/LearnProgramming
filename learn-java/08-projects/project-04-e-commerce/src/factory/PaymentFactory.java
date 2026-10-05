package factory;

import model.Payment;
import model.Payment.PaymentMethod;
import model.Payment.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory for creating and processing {@link Payment} objects.
 *
 * <p>Centralises payment creation so callers never construct {@code Payment}
 * directly. In a real system each payment method would delegate to a separate
 * payment-gateway adapter; here we simulate with print statements.</p>
 */
public class PaymentFactory {

    /**
     * Creates a new {@link Payment} in PENDING status.
     *
     * @param orderId the order this payment is for
     * @param amount  the payment amount
     * @param method  the payment method
     * @return a new Payment object (not yet processed)
     */
    public Payment createPayment(String orderId, double amount, PaymentMethod method) {
        if (orderId == null || orderId.isBlank())
            throw new IllegalArgumentException("orderId is required.");
        if (amount <= 0)
            throw new IllegalArgumentException("Payment amount must be positive.");
        if (method == null)
            throw new IllegalArgumentException("PaymentMethod is required.");

        return new Payment(orderId, amount, method);
    }

    /**
     * Simulates processing a payment.
     *
     * <p>Sets status to COMPLETED and assigns a transaction reference.
     * In a real system this would call the appropriate payment gateway.</p>
     *
     * @param payment the payment to process (must be in PENDING status)
     * @return the updated payment (COMPLETED)
     * @throws IllegalStateException if the payment is not PENDING
     */
    public Payment processPayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot process payment with status: " + payment.getStatus());
        }

        String transactionRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        System.out.printf("[PAYMENT] Processing %s payment of $%.2f via %s...%n",
                payment.getPaymentId(), payment.getAmount(), payment.getMethod());
        System.out.printf("[PAYMENT] Transaction reference: %s — APPROVED%n", transactionRef);

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProcessedAt(LocalDateTime.now());
        payment.setTransactionReference(transactionRef);

        return payment;
    }

    /**
     * Simulates refunding a payment.
     *
     * <p>Sets status to REFUNDED.
     *
     * @param payment the payment to refund (must be COMPLETED)
     * @return the updated payment (REFUNDED)
     * @throws IllegalStateException if the payment is not COMPLETED
     */
    public Payment refundPayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot refund payment with status: " + payment.getStatus());
        }

        System.out.printf("[PAYMENT] Refunding $%.2f for payment %s...%n",
                payment.getAmount(), payment.getPaymentId());

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setProcessedAt(LocalDateTime.now());

        return payment;
    }
}
