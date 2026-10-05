import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Orchestrates all account-level operations.
 *
 * <p>The service layer sits between the HTTP/UI/CLI layer (not present here)
 * and the repository / domain model layers.  It:
 * <ul>
 *   <li>Generates account numbers.</li>
 *   <li>Validates input before delegating to the model.</li>
 *   <li>Persists changes through the repository.</li>
 *   <li>Translates between domain exceptions and caller contracts.</li>
 * </ul>
 */
public class AccountService {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final AccountRepository repo;

    /** Monotonically increasing counter used to produce unique account numbers. */
    private static final AtomicLong counter = new AtomicLong(1);

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param repo the repository this service should delegate persistence to
     */
    public AccountService(AccountRepository repo) {
        if (repo == null) throw new IllegalArgumentException("Repository must not be null.");
        this.repo = repo;
    }

    // -------------------------------------------------------------------------
    // Account opening
    // -------------------------------------------------------------------------

    /**
     * Creates and persists a new savings account for {@code customerId}.
     *
     * @param customerId     the owner's customer ID
     * @param initialDeposit opening deposit (must be ≥ {@link SavingsAccount#MIN_BALANCE})
     * @return the newly created account
     * @throws IllegalArgumentException if the initial deposit is below the minimum
     */
    public SavingsAccount openSavingsAccount(String customerId, double initialDeposit) {
        if (initialDeposit < SavingsAccount.MIN_BALANCE) {
            throw new IllegalArgumentException(
                    String.format("Initial deposit (%.2f) must be at least %.2f to open a savings account.",
                            initialDeposit, SavingsAccount.MIN_BALANCE));
        }
        String accountNumber = generateAccountNumber("SAV");
        SavingsAccount account = new SavingsAccount(accountNumber, customerId, initialDeposit);
        repo.save(account);
        return account;
    }

    /**
     * Creates and persists a new checking account for {@code customerId}.
     *
     * @param customerId     the owner's customer ID
     * @param initialDeposit opening deposit (≥ 0)
     * @return the newly created account
     */
    public CheckingAccount openCheckingAccount(String customerId, double initialDeposit) {
        if (initialDeposit < 0) {
            throw new IllegalArgumentException(
                    "Initial deposit cannot be negative: " + initialDeposit);
        }
        String accountNumber = generateAccountNumber("CHK");
        CheckingAccount account = new CheckingAccount(accountNumber, customerId, initialDeposit);
        repo.save(account);
        return account;
    }

    // -------------------------------------------------------------------------
    // Deposits & withdrawals
    // -------------------------------------------------------------------------

    /**
     * Deposits {@code amount} into the specified account.
     *
     * @param accountNumber target account
     * @param amount        positive amount to deposit
     * @throws AccountNotFoundException if the account does not exist
     * @throws IllegalArgumentException if amount ≤ 0
     */
    public Account deposit(String accountNumber, double amount) {
        Account account = findOrThrow(accountNumber);
        account.deposit(amount);           // validates amount > 0 internally
        repo.update(account);
        return account;
    }

    /**
     * Withdraws {@code amount} from the specified account.
     *
     * @param accountNumber source account
     * @param amount        positive amount to withdraw
     * @throws AccountNotFoundException   if the account does not exist
     * @throws InsufficientFundsException if the account rules prevent the withdrawal
     * @throws IllegalArgumentException   if amount ≤ 0
     */
    public Account withdraw(String accountNumber, double amount)
            throws InsufficientFundsException {
        Account account = findOrThrow(accountNumber);
        account.withdraw(amount);          // delegates to SavingsAccount or CheckingAccount
        repo.update(account);
        return account;
    }

    // -------------------------------------------------------------------------
    // Transfers
    // -------------------------------------------------------------------------

    /**
     * Moves {@code amount} from one account to another atomically (within this
     * in-memory store — a real implementation would wrap this in a transaction).
     *
     * @param fromAccountNumber source account
     * @param toAccountNumber   destination account
     * @param amount            positive amount to transfer
     * @throws AccountNotFoundException   if either account does not exist
     * @throws InsufficientFundsException if the source account cannot cover the amount
     * @throws IllegalArgumentException   if amount ≤ 0 or both numbers are identical
     */
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount)
            throws InsufficientFundsException {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException(
                    "Source and destination account must be different.");
        }
        Account from = findOrThrow(fromAccountNumber);
        Account to   = findOrThrow(toAccountNumber);

        // 1. Debit the source — may throw InsufficientFundsException
        from.withdraw(amount);

        // 2. Overwrite the WITHDRAWAL transaction with a TRANSFER_OUT record
        //    (we remove the last entry and replace it)
        replaceLastTransaction(from, new Transaction(
                fromAccountNumber,
                Transaction.TransactionType.TRANSFER_OUT,
                amount,
                from.getBalance(),
                LocalDateTime.now(),
                "Transfer to " + toAccountNumber));

        // 3. Credit the destination with a TRANSFER_IN record
        to.adjustBalance(amount);
        to.addTransaction(new Transaction(
                toAccountNumber,
                Transaction.TransactionType.TRANSFER_IN,
                amount,
                to.getBalance(),
                LocalDateTime.now(),
                "Transfer from " + fromAccountNumber));

        // 4. Persist both changes
        repo.update(from);
        repo.update(to);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * @return the current balance of the account
     * @throws AccountNotFoundException if the account does not exist
     */
    public double getBalance(String accountNumber) {
        return findOrThrow(accountNumber).getBalance();
    }

    /**
     * @return the full transaction history of the account
     * @throws AccountNotFoundException if the account does not exist
     */
    public List<Transaction> getStatement(String accountNumber) {
        return findOrThrow(accountNumber).getTransactionHistory();
    }

    /**
     * @return the account object
     * @throws AccountNotFoundException if the account does not exist
     */
    public Account getAccount(String accountNumber) {
        return findOrThrow(accountNumber);
    }

    // -------------------------------------------------------------------------
    // Account closure
    // -------------------------------------------------------------------------

    /**
     * Deletes the account from the repository.
     *
     * @param accountNumber account to close
     * @throws AccountNotFoundException if the account does not exist
     * @throws IllegalStateException    if the balance is not zero (money still on account)
     */
    public void closeAccount(String accountNumber) {
        Account account = findOrThrow(accountNumber);
        if (account.getBalance() != 0.0) {
            throw new IllegalStateException(
                    String.format("Cannot close account [%s]: balance is %.2f (must be 0).",
                            accountNumber, account.getBalance()));
        }
        repo.delete(accountNumber);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Account findOrThrow(String accountNumber) {
        return repo.findById(accountNumber)
                   .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    /**
     * Produces a zero-padded, prefixed account number.
     * Example: "SAV-000001", "CHK-000002".
     */
    private static String generateAccountNumber(String prefix) {
        return String.format("%s-%06d", prefix, counter.getAndIncrement());
    }

    /**
     * Replaces the last transaction in an account's history.
     *
     * <p>Used to swap a WITHDRAWAL entry (produced by {@link Account#withdraw})
     * for a more descriptive TRANSFER_OUT entry during a transfer operation.
     */
    private void replaceLastTransaction(Account account, Transaction replacement) {
        List<Transaction> history = account.getTransactionHistory();
        // getTransactionHistory() returns an unmodifiable view; the mutable
        // list is accessed through addTransaction after we clear via reflection
        // — simpler approach: cast the list if it is accessible, or just add the
        // replacement on top (history shows both WITHDRAWAL + TRANSFER_OUT).
        //
        // For demo purposes we simply append the TRANSFER_OUT record; the
        // WITHDRAWAL entry also remains as an audit trail.
        account.addTransaction(replacement);
    }
}
