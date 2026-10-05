/**
 * Unchecked exception thrown when an account lookup returns no result.
 *
 * It is unchecked (extends RuntimeException) because a missing account number
 * generally indicates a programming error — for example, passing a stale or
 * fabricated ID — rather than an expected, recoverable business condition.
 */
public class AccountNotFoundException extends RuntimeException {

    private final String accountNumber;

    /**
     * @param accountNumber the account number that could not be found
     */
    public AccountNotFoundException(String accountNumber) {
        super("Account not found: [" + accountNumber + "].");
        this.accountNumber = accountNumber;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** The account number that triggered this exception. */
    public String getAccountNumber() {
        return accountNumber;
    }
}
