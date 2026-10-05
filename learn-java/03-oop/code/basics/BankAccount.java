import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * BankAccount — an exemplary class demonstrating core OOP principles.
 *
 * Design decisions explained throughout:
 *
 * 1. ENCAPSULATION: All fields are private. No direct access to balance from outside.
 *    External code can only change state through controlled business methods.
 *
 * 2. IMMUTABLE IDENTITY: accountNumber and owner are final — they never change
 *    after construction. A bank account doesn't change which account it is.
 *
 * 3. RICH DOMAIN MODEL: Business logic lives here, not in a separate service class.
 *    The account knows how to deposit, withdraw, and transfer.
 *
 * 4. DEFENSIVE VALIDATION: Every public method validates its inputs immediately.
 *    Invalid state is rejected at the boundary.
 *
 * 5. TRANSACTION HISTORY: Every operation is recorded, creating an audit trail.
 *    This is a real banking requirement — balance changes must be traceable.
 */
public class BankAccount {

    // =========================================================================
    // FIELDS — private to enforce encapsulation
    // =========================================================================

    /**
     * Unique identifier for this account.
     * final: account numbers never change (immutable identity)
     */
    private final String accountNumber;

    /**
     * The name of the account holder.
     * final: account ownership doesn't change (for simplicity; in reality,
     * joint accounts exist but we keep it simple here)
     */
    private final String owner;

    /**
     * The current balance. Private to prevent direct manipulation.
     *
     * Why private matters: if balance were public, anyone could write:
     *   account.balance = 1000000.0;
     * bypassing all business rules. Private + controlled methods = safety.
     */
    private double balance; // In real banking apps, use BigDecimal for exact decimal arithmetic

    /**
     * Complete history of all transactions.
     *
     * This is a mutable list, so we must be careful with our getter.
     * We'll return an unmodifiable view to prevent external modification.
     */
    private final List<String> transactionHistory;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /**
     * Full constructor — creates a BankAccount with a specified initial balance.
     *
     * @param accountNumber unique identifier (cannot be null or empty)
     * @param owner         name of the account holder (cannot be null or empty)
     * @param initialBalance starting balance (must be >= 0)
     * @throws IllegalArgumentException if any argument is invalid
     */
    public BankAccount(String accountNumber, String owner, double initialBalance) {
        // Validate all inputs before assigning — fail fast and loudly
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
        if (owner == null || owner.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be null or empty");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException(
                "Initial balance cannot be negative: " + initialBalance);
        }

        // Use this. to distinguish fields from parameters (same names)
        this.accountNumber = accountNumber.trim();
        this.owner = owner.trim();
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();

        // Record the account opening
        recordTransaction("Account opened with balance: " + formatAmount(initialBalance));
    }

    /**
     * Convenience constructor — creates an account with zero initial balance.
     * Delegates to the full constructor using this() (constructor chaining).
     *
     * Why constructor chaining? So the validation in the main constructor
     * is always executed — we don't duplicate validation logic.
     *
     * @param accountNumber unique identifier
     * @param owner         name of the account holder
     */
    public BankAccount(String accountNumber, String owner) {
        this(accountNumber, owner, 0.0);  // Delegate — calls the 3-arg constructor
    }

    // =========================================================================
    // BUSINESS METHODS — the "interface" of a BankAccount
    // =========================================================================

    /**
     * Deposits money into this account.
     *
     * Why not a setter? A setter like setBalance(balance + amount) allows
     * any value, bypassing business rules. This method enforces that only
     * positive amounts can be deposited and creates an audit record.
     *
     * @param amount the amount to deposit (must be > 0)
     * @throws IllegalArgumentException if amount is not positive
     */
    public void deposit(double amount) {
        // Validate: deposits must be positive
        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Deposit amount must be positive, got: " + amount);
        }

        balance += amount;
        recordTransaction(String.format("Deposit: +%s (New balance: %s)",
                                        formatAmount(amount), formatAmount(balance)));
    }

    /**
     * Withdraws money from this account.
     *
     * Two business rules enforced here:
     * 1. Amount must be positive
     * 2. Cannot withdraw more than current balance (no overdraft)
     *
     * These rules are IMPOSSIBLE to bypass because balance is private.
     *
     * @param amount the amount to withdraw (must be > 0 and <= balance)
     * @throws IllegalArgumentException if amount is not positive
     * @throws IllegalStateException    if insufficient funds
     */
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Withdrawal amount must be positive, got: " + amount);
        }
        if (amount > balance) {
            throw new IllegalStateException(
                String.format("Insufficient funds. Balance: %s, Requested: %s",
                              formatAmount(balance), formatAmount(amount)));
        }

        balance -= amount;
        recordTransaction(String.format("Withdrawal: -%s (New balance: %s)",
                                        formatAmount(amount), formatAmount(balance)));
    }

    /**
     * Transfers money from this account to another account.
     *
     * Notice: this method uses the existing withdraw() and deposit() methods.
     * This is a key OOP principle — reuse existing behavior rather than
     * duplicating logic. The validation in withdraw/deposit still runs.
     *
     * ATOMICITY: A transfer must be atomic — either BOTH the withdrawal and
     * the deposit succeed, or NEITHER happens. Without atomicity, if the
     * deposit throws an exception after the withdrawal, money vanishes:
     * it leaves the source account but never arrives in the destination.
     * The try/catch below restores the withdrawn amount if deposit fails,
     * ensuring the accounts remain consistent.
     *
     * NOTE FOR LEARNERS: In production banking systems, always use BigDecimal
     * instead of double for monetary values to avoid floating-point precision
     * errors. We use double here for simplicity.
     *
     * @param target      the account to transfer to (cannot be null or self)
     * @param amount      the amount to transfer (must be > 0 and <= balance)
     * @throws IllegalArgumentException if target is null or same as this account
     */
    public void transferTo(BankAccount target, double amount) {
        if (target == null) {
            throw new IllegalArgumentException("Target account cannot be null");
        }
        if (target == this) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // Step 1: Withdraw from this account (validates amount and checks balance)
        this.withdraw(amount);

        // Step 2: Deposit to target — if this fails for any reason, reverse the
        // withdrawal so that money is not lost. This makes the transfer atomic.
        try {
            target.deposit(amount);
        } catch (Exception e) {
            // Reverse the withdrawal to restore consistency.
            // Without this, the money would have left this account but never
            // arrived in the target — it would simply disappear.
            this.deposit(amount);
            throw new IllegalStateException(
                "Transfer failed: deposit to target account was rejected. " +
                "The withdrawal has been reversed. Cause: " + e.getMessage(), e);
        }

        // Update transaction record to show it was a transfer, not just a withdrawal
        // (This replaces the last "Withdrawal" record with a "Transfer" record)
        String lastEntry = transactionHistory.get(transactionHistory.size() - 1);
        transactionHistory.set(
            transactionHistory.size() - 1,
            lastEntry.replace("Withdrawal", "Transfer to " + target.accountNumber)
        );
    }

    // =========================================================================
    // QUERY METHODS — read-only access to state
    // =========================================================================

    /**
     * Returns the current balance.
     *
     * Why a getter instead of a public field?
     * We can add logging, permission checks, or computation in the future
     * without changing the interface. A public field gives no such flexibility.
     *
     * @return current balance (always >= 0 due to invariant enforcement)
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Returns the account number.
     * Account numbers are read-only — no setter provided.
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Returns the account owner's name.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Returns an unmodifiable view of the transaction history.
     *
     * Why unmodifiable? If we returned the actual list, callers could do:
     *   account.getTransactionHistory().clear();
     * which would delete our audit trail. Unmodifiable prevents this.
     *
     * Why a view instead of a copy? A view is more memory-efficient.
     * For a defensive copy, we'd use: new ArrayList<>(transactionHistory)
     *
     * @return immutable view of all transactions
     */
    public List<String> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    /**
     * Returns the number of transactions recorded.
     */
    public int getTransactionCount() {
        return transactionHistory.size();
    }

    // =========================================================================
    // PRIVATE HELPER METHODS — internal implementation details
    // =========================================================================

    /**
     * Records a transaction in the history with a timestamp.
     * Private: this is an implementation detail, not part of the public API.
     */
    private void recordTransaction(String description) {
        String entry = String.format("[%s] %s",
            java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            description);
        transactionHistory.add(entry);
    }

    /**
     * Formats a monetary amount consistently throughout this class.
     * Private: formatting details are implementation concerns.
     */
    private String formatAmount(double amount) {
        return String.format("$%.2f", amount);
    }

    // =========================================================================
    // Object CONTRACT METHODS — from java.lang.Object
    // =========================================================================

    /**
     * Returns a human-readable string representation of this account.
     *
     * @Override is used to indicate this overrides Object.toString()
     * Always override toString() for meaningful debugging output.
     * Never expose sensitive data (like balance) in production toString().
     */
    @Override
    public String toString() {
        return String.format("BankAccount{accountNumber='%s', owner='%s', balance=%s}",
                            accountNumber, owner, formatAmount(balance));
    }

    /**
     * Two BankAccounts are equal if and only if they have the same account number.
     *
     * Why account number only? Because account numbers are unique identifiers.
     * Even if two accounts have the same balance, they're different accounts.
     * This is the "business identity" of a bank account.
     *
     * IMPORTANT: equals() and hashCode() must always be overridden together.
     * If equals() says two objects are equal, hashCode() must return the same value.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;             // Same reference → same object
        if (obj == null) return false;            // null is never equal
        if (getClass() != obj.getClass()) return false;  // Different types
        BankAccount other = (BankAccount) obj;   // Safe cast
        return Objects.equals(accountNumber, other.accountNumber);
    }

    /**
     * Hash code based on account number, consistent with equals().
     *
     * The contract: if a.equals(b), then a.hashCode() == b.hashCode()
     * Violating this breaks HashMap, HashSet, and other hash-based collections.
     */
    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    // =========================================================================
    // STATIC FACTORY METHOD (alternative to constructor)
    // =========================================================================

    /**
     * Static factory method — alternative to calling 'new' directly.
     *
     * Benefits over constructors:
     * 1. Can have a descriptive name (openNewAccount vs. new BankAccount)
     * 2. Can return cached instances (not used here)
     * 3. Can return subtypes (e.g., return a SavingsAccount for some conditions)
     *
     * This is an optional pattern — both approaches are valid.
     */
    public static BankAccount openNewAccount(String accountNumber, String owner) {
        return new BankAccount(accountNumber, owner, 0.0);
    }

    public static BankAccount openNewAccountWithBalance(
            String accountNumber, String owner, double initialBalance) {
        return new BankAccount(accountNumber, owner, initialBalance);
    }
}
