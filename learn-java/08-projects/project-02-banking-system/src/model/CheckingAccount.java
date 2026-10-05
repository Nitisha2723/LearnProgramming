import java.time.LocalDateTime;

/**
 * A checking (current) account that supports a configurable overdraft limit.
 *
 * <p>Key rules:
 * <ul>
 *   <li>The balance may go negative, but not below {@code -overdraftLimit}.</li>
 *   <li>Interest rate is 1 % annually — primarily a transactional account.</li>
 * </ul>
 */
public class CheckingAccount extends Account {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Default overdraft limit: €500. */
    private static final double DEFAULT_OVERDRAFT_LIMIT = 500.0;

    /** Annual interest rate: 1 %. */
    private static final double INTEREST_RATE = 0.01;

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Maximum amount the balance is allowed to go below zero. */
    private double overdraftLimit;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Opens a checking account with the default €500 overdraft limit.
     *
     * @param accountNumber  unique identifier
     * @param ownerId        customer that owns this account
     * @param initialBalance opening balance (may be 0)
     */
    public CheckingAccount(String accountNumber, String ownerId, double initialBalance) {
        this(accountNumber, ownerId, initialBalance, DEFAULT_OVERDRAFT_LIMIT);
    }

    /**
     * Opens a checking account with a custom overdraft limit.
     */
    public CheckingAccount(String accountNumber, String ownerId,
                           double initialBalance, double overdraftLimit) {
        super(accountNumber, ownerId, initialBalance);
        if (overdraftLimit < 0) {
            throw new IllegalArgumentException(
                    "Overdraft limit cannot be negative: " + overdraftLimit);
        }
        this.overdraftLimit = overdraftLimit;
    }

    // -------------------------------------------------------------------------
    // Abstract method implementations
    // -------------------------------------------------------------------------

    @Override
    public double getInterestRate() {
        return INTEREST_RATE;
    }

    @Override
    public String getAccountType() {
        return "CHECKING";
    }

    /**
     * Withdraws {@code amount} from the account.
     *
     * <p>The withdrawal is allowed as long as
     * {@code balance - amount >= -overdraftLimit}.
     *
     * @param amount positive amount to withdraw
     * @throws InsufficientFundsException if the withdrawal would exceed the
     *                                    overdraft limit
     * @throws IllegalArgumentException   if amount ≤ 0
     */
    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount must be positive: " + amount);
        }

        // Maximum that can be withdrawn = current balance + overdraft headroom
        double available = getBalance() + overdraftLimit;
        if (amount > available) {
            throw new InsufficientFundsException(getAccountNumber(), amount, available);
        }

        adjustBalance(-amount);
        addTransaction(new Transaction(
                getAccountNumber(),
                Transaction.TransactionType.WITHDRAWAL,
                amount,
                getBalance(),
                LocalDateTime.now(),
                "Withdrawal"));
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    /**
     * Updates the overdraft limit.
     *
     * @param overdraftLimit new limit (must be ≥ 0)
     */
    public void setOverdraftLimit(double overdraftLimit) {
        if (overdraftLimit < 0) {
            throw new IllegalArgumentException(
                    "Overdraft limit cannot be negative: " + overdraftLimit);
        }
        this.overdraftLimit = overdraftLimit;
    }
}
