import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransactionService}.
 *
 * <p>Uses a real {@link InMemoryAccountRepository} so transactions recorded
 * on domain objects are immediately visible to service queries.
 */
@DisplayName("TransactionService tests")
class TransactionServiceTest {

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    private AccountRepository  repo;
    private TransactionService service;
    private SavingsAccount     savings;
    private CheckingAccount    checking;

    @BeforeEach
    void setUp() throws InsufficientFundsException {
        repo    = new InMemoryAccountRepository();
        service = new TransactionService(repo);

        // Savings account: 1000 balance, some activity
        savings = new SavingsAccount("SAV-TX-01", "customer-1", 1_000.00);
        savings.deposit(500.00);      // +500
        savings.withdraw(200.00);     // -200
        savings.deposit(100.00);      // +100
        repo.save(savings);

        // Checking account: used in interest-apply test
        checking = new CheckingAccount("CHK-TX-01", "customer-1", 500.00);
        repo.save(checking);
    }

    // -------------------------------------------------------------------------
    // getTransactionHistory — sorted
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getTransactionHistory returns all transactions sorted by timestamp ascending")
    void testGetTransactionHistory_sorted() {
        List<Transaction> history = service.getTransactionHistory("SAV-TX-01");

        // 4 transactions: open deposit, +500, -200, +100
        assertEquals(4, history.size(), "Should contain all recorded transactions.");

        // Verify ascending order
        for (int i = 1; i < history.size(); i++) {
            assertFalse(
                    history.get(i).getTimestamp().isBefore(history.get(i - 1).getTimestamp()),
                    "Transactions must be ordered oldest-first.");
        }
    }

    // -------------------------------------------------------------------------
    // getMonthlyStatement — filters correctly
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getMonthlyStatement returns only transactions in the requested month")
    void testGetMonthlyStatement_filtersCorrectly() {
        // All activity happened in the current month (setUp() just ran)
        LocalDateTime now = LocalDateTime.now();
        int year  = now.getYear();
        int month = now.getMonthValue();

        List<Transaction> monthly = service.getMonthlyStatement("SAV-TX-01", year, month);

        // Should contain exactly the 4 transactions we created in setUp
        assertFalse(monthly.isEmpty(), "Monthly statement must not be empty.");
        for (Transaction t : monthly) {
            assertEquals(year,  t.getTimestamp().getYear(),       "Year must match.");
            assertEquals(month, t.getTimestamp().getMonthValue(), "Month must match.");
        }
    }

    @Test
    @DisplayName("getMonthlyStatement returns empty list for a month with no activity")
    void testGetMonthlyStatement_noActivity_returnsEmpty() {
        // Use a year guaranteed to have no data
        List<Transaction> monthly = service.getMonthlyStatement("SAV-TX-01", 2000, 1);
        assertTrue(monthly.isEmpty(), "No transactions should exist for year 2000.");
    }

    // -------------------------------------------------------------------------
    // getTotalDeposits
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getTotalDeposits sums DEPOSIT and TRANSFER_IN amounts for the month")
    void testGetTotalDeposits() {
        LocalDateTime now = LocalDateTime.now();
        // Opening deposit (1000) + deposit (500) + deposit (100) = 1600
        double total = service.getTotalDeposits("SAV-TX-01", now.getYear(), now.getMonthValue());
        assertEquals(1_600.00, total, 0.001,
                "Total deposits should be 1000 + 500 + 100 = 1600.");
    }

    // -------------------------------------------------------------------------
    // getTotalWithdrawals
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getTotalWithdrawals sums WITHDRAWAL and TRANSFER_OUT amounts for the month")
    void testGetTotalWithdrawals() {
        LocalDateTime now = LocalDateTime.now();
        // One withdrawal of 200
        double total = service.getTotalWithdrawals("SAV-TX-01", now.getYear(), now.getMonthValue());
        assertEquals(200.00, total, 0.001,
                "Total withdrawals should be 200.");
    }

    // -------------------------------------------------------------------------
    // applyMonthlyInterestToAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("applyMonthlyInterestToAll credits interest to all SavingsAccounts only")
    void testApplyMonthlyInterestToAll_onlySavings() {
        double savingsBalanceBefore  = savings.getBalance();
        double checkingBalanceBefore = checking.getBalance();

        int count = service.applyMonthlyInterestToAll();

        assertEquals(1, count, "Exactly one SavingsAccount should receive interest.");

        double expectedSavings = savingsBalanceBefore + savingsBalanceBefore * savings.getInterestRate() / 12.0;
        assertEquals(expectedSavings, savings.getBalance(), 0.0001,
                "Savings balance should increase by one month's interest.");

        // Checking account must not be touched
        assertEquals(checkingBalanceBefore, checking.getBalance(), 0.001,
                "Checking account balance must not change.");
    }

    @Test
    @DisplayName("applyMonthlyInterestToAll adds an INTEREST transaction to savings accounts")
    void testApplyMonthlyInterestToAll_recordsInterestTransaction() {
        service.applyMonthlyInterestToAll();

        List<Transaction> history = service.getTransactionHistory("SAV-TX-01");
        Transaction last = history.get(history.size() - 1);

        assertEquals(Transaction.TransactionType.INTEREST, last.getType(),
                "Last transaction should be INTEREST after interest is applied.");
        assertTrue(last.getAmount() > 0, "Interest amount must be positive.");
    }
}
