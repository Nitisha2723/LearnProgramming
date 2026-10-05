package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Records payment details for an order.
 */
public class Payment {

    // -------------------------------------------------------------------------
    // Enums
    // -------------------------------------------------------------------------

    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final String paymentId;
    private final String orderId;
    private final double amount;
    private final PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime processedAt;
    private String transactionReference;

    public Payment(String orderId, double amount, PaymentMethod method) {
        this.paymentId  = UUID.randomUUID().toString();
        this.orderId    = orderId;
        this.amount     = amount;
        this.method     = method;
        this.status     = PaymentStatus.PENDING;
        this.processedAt = LocalDateTime.now();
    }

    /** Reconstruction constructor. */
    public Payment(String paymentId, String orderId, double amount, PaymentMethod method,
                   PaymentStatus status, LocalDateTime processedAt,
                   String transactionReference) {
        this.paymentId            = paymentId;
        this.orderId              = orderId;
        this.amount               = amount;
        this.method               = method;
        this.status               = status;
        this.processedAt          = processedAt;
        this.transactionReference = transactionReference;
    }

    // Getters & Setters
    public String getPaymentId()                                 { return paymentId; }
    public String getOrderId()                                   { return orderId; }
    public double getAmount()                                    { return amount; }
    public PaymentMethod getMethod()                             { return method; }
    public PaymentStatus getStatus()                             { return status; }
    public LocalDateTime getProcessedAt()                        { return processedAt; }
    public String getTransactionReference()                      { return transactionReference; }

    public void setStatus(PaymentStatus status)                  { this.status = status; }
    public void setProcessedAt(LocalDateTime processedAt)        { this.processedAt = processedAt; }
    public void setTransactionReference(String transactionRef)   { this.transactionReference = transactionRef; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        return Objects.equals(paymentId, ((Payment) o).paymentId);
    }

    @Override
    public int hashCode() { return Objects.hash(paymentId); }

    @Override
    public String toString() {
        return String.format("Payment{id='%s', order='%s', amount=%.2f, method=%s, status=%s}",
                paymentId, orderId, amount, method, status);
    }
}
