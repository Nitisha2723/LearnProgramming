package service;

import factory.PaymentFactory;
import model.Payment;
import model.Payment.PaymentMethod;
import model.Payment.PaymentStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Business logic for payment processing.
 *
 * <p>Delegates creation and processing to {@link PaymentFactory}.</p>
 */
public class PaymentService {

    private final PaymentFactory paymentFactory;
    /** In-memory payment store — replace with a repository in a real system. */
    private final Map<String, Payment> payments = new HashMap<>();

    public PaymentService(PaymentFactory paymentFactory) {
        this.paymentFactory = paymentFactory;
    }

    // -------------------------------------------------------------------------
    // Payment operations
    // -------------------------------------------------------------------------

    /**
     * Creates and immediately processes a payment for the given order.
     *
     * @param orderId the order to pay for
     * @param amount  the payment amount
     * @param method  the payment method to use
     * @return the processed (COMPLETED) payment
     */
    public Payment processPayment(String orderId, double amount, PaymentMethod method) {
        Payment payment = paymentFactory.createPayment(orderId, amount, method);
        paymentFactory.processPayment(payment);
        payments.put(payment.getPaymentId(), payment);
        return payment;
    }

    /**
     * Refunds a previously completed payment.
     *
     * @param paymentId the ID of the payment to refund
     * @return the refunded payment
     * @throws NoSuchElementException if the payment does not exist
     * @throws IllegalStateException  if the payment is not in COMPLETED status
     */
    public Payment refundPayment(String paymentId) {
        Payment payment = getPayment(paymentId);
        paymentFactory.refundPayment(payment);
        return payment;
    }

    /**
     * Retrieves a payment by ID.
     *
     * @throws NoSuchElementException if not found
     */
    public Payment getPayment(String paymentId) {
        Payment payment = payments.get(paymentId);
        if (payment == null) {
            throw new NoSuchElementException("Payment not found: " + paymentId);
        }
        return payment;
    }
}
