import java.util.List;

/**
 * Entry point for the banking-system demo.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Opening savings and checking accounts</li>
 *   <li>Deposits and withdrawals</li>
 *   <li>Fund transfers</li>
 *   <li>Balance queries</li>
 *   <li>Transaction history / monthly statements</li>
 *   <li>Monthly interest application</li>
 *   <li>Catching {@link InsufficientFundsException}</li>
 * </ul>
 */
public class Main {

    public static void main(String[] args) {

        // ------------------------------------------------------------------ //
        // Bootstrap — wire up infrastructure
        // ------------------------------------------------------------------ //
        AccountRepository   repo               = new InMemoryAccountRepository();
        AccountService      accountService     = new AccountService(repo);
        TransactionService  transactionService = new TransactionService(repo);

        // ------------------------------------------------------------------ //
        // Create a customer (no CustomerRepository in this demo — inline)
        // ------------------------------------------------------------------ //
        Customer alice = new Customer("Alice", "Mueller", "alice@example.com", "+49-170-1234567");
        System.out.println("=== New Customer ===");
        System.out.println(alice);
        System.out.println();

        // ------------------------------------------------------------------ //
        // Open accounts
        // ------------------------------------------------------------------ //
        SavingsAccount  savings  = accountService.openSavingsAccount(alice.getCustomerId(), 1_000.00);
        CheckingAccount checking = accountService.openCheckingAccount(alice.getCustomerId(), 500.00);
        alice.addAccount(savings.getAccountNumber());
        alice.addAccount(checking.getAccountNumber());

        System.out.println("=== Accounts Opened ===");
        System.out.println(savings);
        System.out.println(checking);
        System.out.println();

        // ------------------------------------------------------------------ //
        // Deposits
        // ------------------------------------------------------------------ //
        accountService.deposit(savings.getAccountNumber(), 2_500.00);
        accountService.deposit(checking.getAccountNumber(), 750.00);

        System.out.println("=== After Deposits ===");
        System.out.printf("  Savings  balance: %.2f%n", accountService.getBalance(savings.getAccountNumber()));
        System.out.printf("  Checking balance: %.2f%n", accountService.getBalance(checking.getAccountNumber()));
        System.out.println();

        // ------------------------------------------------------------------ //
        // Withdrawals
        // ------------------------------------------------------------------ //
        try {
            accountService.withdraw(savings.getAccountNumber(), 500.00);
            System.out.println("=== After Savings Withdrawal of 500.00 ===");
            System.out.printf("  Savings balance: %.2f%n",
                    accountService.getBalance(savings.getAccountNumber()));
            System.out.println();
        } catch (InsufficientFundsException e) {
            System.err.println("Withdrawal failed: " + e.getMessage());
        }

        // ------------------------------------------------------------------ //
        // Fund transfer: savings → checking
        // ------------------------------------------------------------------ //
        try {
            accountService.transfer(savings.getAccountNumber(), checking.getAccountNumber(), 300.00);
            System.out.println("=== After Transfer of 300.00 (savings → checking) ===");
            System.out.printf("  Savings  balance: %.2f%n",
                    accountService.getBalance(savings.getAccountNumber()));
            System.out.printf("  Checking balance: %.2f%n",
                    accountService.getBalance(checking.getAccountNumber()));
            System.out.println();
        } catch (InsufficientFundsException e) {
            System.err.println("Transfer failed: " + e.getMessage());
        }

        // ------------------------------------------------------------------ //
        // Demonstrate InsufficientFundsException on savings (min balance)
        // ------------------------------------------------------------------ //
        System.out.println("=== Attempting to breach minimum balance on savings ===");
        try {
            // Savings balance is now 2700; MIN_BALANCE = 100 → max withdrawal = 2600
            accountService.withdraw(savings.getAccountNumber(), 10_000.00);
            System.out.println("  Withdrawal succeeded (unexpected).");
        } catch (InsufficientFundsException e) {
            System.out.println("  Caught InsufficientFundsException (expected):");
            System.out.println("  " + e.getMessage());
            System.out.printf("  Requested: %.2f | Available: %.2f%n",
                    e.getRequestedAmount(), e.getAvailableAmount());
        }
        System.out.println();

        // ------------------------------------------------------------------ //
        // Demonstrate overdraft on checking
        // ------------------------------------------------------------------ //
        System.out.println("=== Overdraft on checking (within limit) ===");
        try {
            // Checking balance ≈ 1550; overdraft limit = 500 → max = 2050
            accountService.withdraw(checking.getAccountNumber(), 1_800.00);
            System.out.printf("  Checking balance after overdraft: %.2f%n",
                    accountService.getBalance(checking.getAccountNumber()));
        } catch (InsufficientFundsException e) {
            System.err.println("  Overdraft failed: " + e.getMessage());
        }

        // Restore checking balance for next demo steps
        try {
            accountService.deposit(checking.getAccountNumber(), 1_800.00);
        } catch (Exception ignored) {}

        System.out.println();

        // ------------------------------------------------------------------ //
        // Apply monthly interest to all savings accounts
        // ------------------------------------------------------------------ //
        int interestApplied = transactionService.applyMonthlyInterestToAll();
        System.out.printf("=== Monthly interest applied to %d savings account(s) ===%n",
                interestApplied);
        System.out.printf("  Savings balance after interest: %.4f%n",
                accountService.getBalance(savings.getAccountNumber()));
        System.out.println();

        // ------------------------------------------------------------------ //
        // Print transaction history for savings account
        // ------------------------------------------------------------------ //
        System.out.println("=== Full Transaction History — Savings Account ===");
        System.out.printf("  Account: %s%n%n", savings.getAccountNumber());
        List<Transaction> history =
                transactionService.getTransactionHistory(savings.getAccountNumber());
        for (Transaction t : history) {
            System.out.println("  " + t);
        }
        System.out.println();

        // ------------------------------------------------------------------ //
        // Monthly statement totals for current month
        // ------------------------------------------------------------------ //
        int year  = savings.getOpenedAt().getYear();
        int month = savings.getOpenedAt().getMonthValue();
        double totalDeposits    = transactionService.getTotalDeposits(savings.getAccountNumber(), year, month);
        double totalWithdrawals = transactionService.getTotalWithdrawals(savings.getAccountNumber(), year, month);

        System.out.printf("=== Monthly Totals — Savings (%d/%02d) ===%n", year, month);
        System.out.printf("  Total deposits   : +%.2f%n", totalDeposits);
        System.out.printf("  Total withdrawals: -%.2f%n", totalWithdrawals);
        System.out.println();

        // ------------------------------------------------------------------ //
        // Account lookup for non-existent account → AccountNotFoundException
        // ------------------------------------------------------------------ //
        System.out.println("=== Attempting to fetch non-existent account ===");
        try {
            accountService.getAccount("FAKE-999999");
        } catch (AccountNotFoundException e) {
            System.out.println("  Caught AccountNotFoundException (expected):");
            System.out.println("  " + e.getMessage());
        }
        System.out.println();

        System.out.println("Demo complete.");
    }
}
