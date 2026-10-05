import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for all bank account types.
 *
 * <p>Shared state (account number, owner, balance, history) and shared
 * behaviour (deposit, transaction recording) live here.  Product-specific
 * rules — withdrawal policies and interest rates — are declared abstract so
 * every concrete subclass must provide its own implementation.
 *
 * <p>Design note: an interface would not be enough because we need to carry
 * mutable shared state and a concrete {@link #deposit} implementation.
 */
public abstract class Account {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Permanent, immutable account identifier (e.g. "SAV-000001"). */
    private final String accountNumber;

    /** The customer ID that owns this account. */
    private final String ownerId;

    /** Current balance. Modified only by {@link #deposit} and subclass withdraw implementations. */
    private double balance;

    /** When the account was opened. */
    private final LocalDateTime openedAt;

    /** Ordered list of every event recorded on this account. */
    private final List<Transaction> transactionHistory;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param accountNumber unique identifier assigned by the service layer
     * @param ownerId       customer ID of the account holder
     * @param initialBalance opening balance (must be ≥ 0)
     * @throws IllegalArgumentException if initialBalance is negative
     */
    protected Account(String accountNumber, String ownerId, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException(
                    "Initial balance cannot be negative: " + initialBalance);
        }
        this.accountNumber     = accountNumber;
        this.ownerId           = ownerId;
        this.balance           = initialBalance;
        this.openedAt          = LocalDateTime.now();
        this.transactionHistory = new ArrayList<>();

        // Record the opening deposit only when there is one
        if (initialBalance > 0) {
            addTransaction(new Transaction(
                    accountNumber,
                    Transaction.TransactionType.DEPOSIT,
                    initialBalance,
                    initialBalance,
                    openedAt,
                    "Account opened"));
        }
    }

    // -------------------------------------------------------------------------
    // Abstract methods — subclasses define their own rules
    // -------------------------------------------------------------------------

    /**
     * @return the annual interest rate applicable to this account type
     *         (e.g. 0.035 = 3.5 %)
     */
    public abstract double getInterestRate();

    /**
     * @return a short product label (e.g. "SAVINGS", "CHECKING")
     */
    public abstract String getAccountType();

    /**
     * Removes {@code amount} from the balance, applying the rules of the
     * specific account type (minimum balance for savings, overdraft for checking).
     *
     * @param amount positive value to remove
     * @throws InsufficientFundsException if the subclass rule prevents the withdrawal
     * @throws IllegalArgumentException   if amount ≤ 0
     */
    public abstract void withdraw(double amount) throws InsufficientFundsException;

    // -------------------------------------------------------------------------
    // Concrete shared behaviour
    // -------------------------------------------------------------------------

    /**
     * Adds {@code amount} to the balance and records a DEPOSIT transaction.
     *
     * @param amount positive value to add
     * @throws IllegalArgumentException if amount ≤ 0
     */
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Deposit amount must be positive: " + amount);
        }
        balance += amount;
        addTransaction(new Transaction(
                accountNumber,
                Transaction.TransactionType.DEPOSIT,
                amount,
                balance,
                LocalDateTime.now(),
                "Deposit"));
    }

    /**
     * Appends a transaction to this account's history.
     *
     * <p>Called by {@link #deposit}, by subclass {@code withdraw} overrides,
     * and by {@link SavingsAccount#applyMonthlyInterest}.
     */
    public void addTransaction(Transaction t) {
        transactionHistory.add(t);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String          getAccountNumber()     { return accountNumber;     }
    public String          getOwnerId()           { return ownerId;           }
    public double          getBalance()           { return balance;           }
    public LocalDateTime   getOpenedAt()          { return openedAt;          }

    /**
     * @return an unmodifiable view of the transaction history
     */
    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    // -------------------------------------------------------------------------
    // Package-private balance mutator used by subclass withdraw + interest
    // -------------------------------------------------------------------------

    /**
     * Adjusts the balance by {@code delta} (positive = credit, negative = debit).
     * Visible to subclasses in the same default package.
     */
    void adjustBalance(double delta) {
        this.balance += delta;
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("[%s] %s (owner: %s) — Balance: %.2f",
                getAccountType(), accountNumber, ownerId, balance);
    }
}
