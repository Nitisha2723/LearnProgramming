import java.time.LocalDateTime;

/**
 * A savings account that earns monthly interest and enforces a minimum balance.
 *
 * <p>Key rules:
 * <ul>
 *   <li>The balance may never fall below {@value #MIN_BALANCE} after a withdrawal.</li>
 *   <li>Monthly interest is calculated as {@code balance × interestRate / 12}.</li>
 * </ul>
 */
public class SavingsAccount extends Account {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** The lowest balance this account is permitted to hold after any withdrawal. */
    public static final double MIN_BALANCE = 100.0;

    /** Default annual interest rate: 3.5 %. */
    private static final double DEFAULT_INTEREST_RATE = 0.035;

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Annual interest rate stored per-account so it can be changed later. */
    private double interestRate;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Opens a savings account with the default 3.5 % annual interest rate.
     *
     * @param accountNumber  unique identifier
     * @param ownerId        customer that owns this account
     * @param initialBalance opening deposit (must be ≥ {@value #MIN_BALANCE})
     */
    public SavingsAccount(String accountNumber, String ownerId, double initialBalance) {
        this(accountNumber, ownerId, initialBalance, DEFAULT_INTEREST_RATE);
    }

    /**
     * Opens a savings account with a custom annual interest rate.
     */
    public SavingsAccount(String accountNumber, String ownerId,
                          double initialBalance, double interestRate) {
        super(accountNumber, ownerId, initialBalance);
        this.interestRate = interestRate;
    }

    // -------------------------------------------------------------------------
    // Abstract method implementations
    // -------------------------------------------------------------------------

    @Override
    public double getInterestRate() {
        return interestRate;
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    /**
     * Withdraws {@code amount} from the account.
     *
     * <p>The withdrawal is rejected if {@code balance - amount < MIN_BALANCE}.
     *
     * @param amount positive amount to withdraw
     * @throws InsufficientFundsException if the withdrawal would breach the
     *                                    minimum balance floor
     * @throws IllegalArgumentException   if amount ≤ 0
     */
    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount must be positive: " + amount);
        }

        double available = getBalance() - MIN_BALANCE;   // how much can actually leave
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
    // Savings-specific behaviour
    // -------------------------------------------------------------------------

    /**
     * Computes but does not apply the interest due for the current month.
     *
     * @return {@code balance × interestRate / 12}
     */
    public double calculateMonthlyInterest() {
        return getBalance() * interestRate / 12.0;
    }

    /**
     * Credits the monthly interest to the balance and records an INTEREST
     * transaction.
     */
    public void applyMonthlyInterest() {
        double interest = calculateMonthlyInterest();
        adjustBalance(interest);
        addTransaction(new Transaction(
                getAccountNumber(),
                Transaction.TransactionType.INTEREST,
                interest,
                getBalance(),
                LocalDateTime.now(),
                String.format("Monthly interest at %.2f%%", interestRate * 100)));
    }

    // -------------------------------------------------------------------------
    // Getter / setter
    // -------------------------------------------------------------------------

    public void setInterestRate(double interestRate) {
        if (interestRate < 0) {
            throw new IllegalArgumentException(
                    "Interest rate cannot be negative: " + interestRate);
        }
        this.interestRate = interestRate;
    }
}
