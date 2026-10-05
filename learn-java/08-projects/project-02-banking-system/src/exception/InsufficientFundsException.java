/**
 * Checked exception thrown when a withdrawal or transfer cannot be completed
 * because the account does not hold enough available funds.
 *
 * It is checked so that callers are forced to handle or declare the failure
 * explicitly — silently swallowing it would corrupt account state.
 */
public class InsufficientFundsException extends Exception {

    private final String accountNumber;
    private final double requestedAmount;
    private final double availableAmount;

    /**
     * @param accountNumber   the account on which the operation was attempted
     * @param requestedAmount the amount the caller tried to withdraw / transfer
     * @param availableAmount the maximum amount actually available (may differ from
     *                        the raw balance when a minimum-balance floor applies)
     */
    public InsufficientFundsException(String accountNumber,
                                      double requestedAmount,
                                      double availableAmount) {
        super(buildMessage(accountNumber, requestedAmount, availableAmount));
        this.accountNumber   = accountNumber;
        this.requestedAmount = requestedAmount;
        this.availableAmount = availableAmount;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static String buildMessage(String accountNumber,
                                       double requested,
                                       double available) {
        return String.format(
            "Insufficient funds on account [%s]: requested %.2f but only %.2f available.",
            accountNumber, requested, available);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** The account number on which the transaction was rejected. */
    public String getAccountNumber() {
        return accountNumber;
    }

    /** The amount that was requested. */
    public double getRequestedAmount() {
        return requestedAmount;
    }

    /** The amount that was actually available at the time of the attempt. */
    public double getAvailableAmount() {
        return availableAmount;
    }
}
