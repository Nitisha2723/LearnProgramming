import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the domain model classes {@link SavingsAccount} and
 * {@link CheckingAccount}.
 *
 * <p>No mocking needed — model tests work directly with the objects.
 */
@DisplayName("Account model tests")
class AccountTest {

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    private SavingsAccount  savings;
    private CheckingAccount checking;

    @BeforeEach
    void setUp() {
        savings  = new SavingsAccount("SAV-TEST-01", "customer-1", 500.00);
        checking = new CheckingAccount("CHK-TEST-01", "customer-1", 1_000.00);
    }

    // -------------------------------------------------------------------------
    // SavingsAccount — deposit
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Depositing a positive amount increases balance and records a DEPOSIT transaction")
    void testSavingsAccountDeposit() {
        savings.deposit(200.00);

        assertEquals(700.00, savings.getBalance(), 0.001,
                "Balance should increase by the deposited amount.");

        // The opening transaction (index 0) + this deposit (index 1)
        assertEquals(2, savings.getTransactionHistory().size());

        Transaction last = lastTransaction(savings);
        assertEquals(Transaction.TransactionType.DEPOSIT, last.getType());
        assertEquals(200.00, last.getAmount(), 0.001);
        assertEquals(700.00, last.getBalanceAfter(), 0.001);
    }

    // -------------------------------------------------------------------------
    // SavingsAccount — withdraw success
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Withdrawing an amount that keeps balance above MIN_BALANCE succeeds")
    void testSavingsAccountWithdraw_success() throws InsufficientFundsException {
        // Balance 500, MIN_BALANCE 100 → can withdraw up to 400
        savings.withdraw(300.00);

        assertEquals(200.00, savings.getBalance(), 0.001);
        assertEquals(Transaction.TransactionType.WITHDRAWAL,
                lastTransaction(savings).getType());
    }

    // -------------------------------------------------------------------------
    // SavingsAccount — withdraw below min balance
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Withdrawing an amount that would breach MIN_BALANCE throws InsufficientFundsException")
    void testSavingsAccountWithdraw_belowMinBalance_throwsException() {
        // Balance 500, MIN_BALANCE 100 → available = 400; requesting 450 should fail
        InsufficientFundsException ex = assertThrows(
                InsufficientFundsException.class,
                () -> savings.withdraw(450.00));

        assertEquals("SAV-TEST-01", ex.getAccountNumber());
        assertEquals(450.00, ex.getRequestedAmount(), 0.001);
        assertEquals(400.00, ex.getAvailableAmount(), 0.001,
                "Available should be balance - MIN_BALANCE = 400.");

        // Balance must be unchanged
        assertEquals(500.00, savings.getBalance(), 0.001);
    }

    // -------------------------------------------------------------------------
    // CheckingAccount — overdraft within limit
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Withdrawing more than current balance but within overdraft limit succeeds")
    void testCheckingAccountOverdraft_withinLimit() throws InsufficientFundsException {
        // Balance 1000, overdraftLimit 500 → can withdraw up to 1500
        checking.withdraw(1_200.00);

        assertEquals(-200.00, checking.getBalance(), 0.001,
                "Balance should go negative when within overdraft limit.");
    }

    // -------------------------------------------------------------------------
    // CheckingAccount — overdraft exceeds limit
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Withdrawing beyond overdraft limit throws InsufficientFundsException")
    void testCheckingAccountOverdraft_exceedsLimit_throwsException() {
        // Balance 1000, overdraftLimit 500 → max = 1500; requesting 2000 should fail
        InsufficientFundsException ex = assertThrows(
                InsufficientFundsException.class,
                () -> checking.withdraw(2_000.00));

        assertEquals("CHK-TEST-01", ex.getAccountNumber());
        assertEquals(2_000.00, ex.getRequestedAmount(), 0.001);
        assertEquals(1_500.00, ex.getAvailableAmount(), 0.001,
                "Available = balance + overdraftLimit = 1000 + 500 = 1500.");

        // Balance unchanged
        assertEquals(1_000.00, checking.getBalance(), 0.001);
    }

    // -------------------------------------------------------------------------
    // SavingsAccount — interest calculation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("calculateMonthlyInterest returns balance * rate / 12")
    void testSavingsAccountInterestCalculation() {
        // balance = 500, rate = 0.035 → monthly = 500 * 0.035 / 12 ≈ 1.4583
        double expectedMonthly = 500.00 * 0.035 / 12.0;
        assertEquals(expectedMonthly, savings.calculateMonthlyInterest(), 0.0001);
    }

    // -------------------------------------------------------------------------
    // SavingsAccount — apply monthly interest
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("applyMonthlyInterest credits interest to balance and records INTEREST transaction")
    void testApplyMonthlyInterest_updatesBalance() {
        double before   = savings.getBalance();
        double expected = before + before * 0.035 / 12.0;

        savings.applyMonthlyInterest();

        assertEquals(expected, savings.getBalance(), 0.0001,
                "Balance should increase by one month's interest.");

        Transaction last = lastTransaction(savings);
        assertEquals(Transaction.TransactionType.INTEREST, last.getType(),
                "Last transaction should be of type INTEREST.");
        assertTrue(last.getAmount() > 0, "Interest amount must be positive.");
        assertEquals(savings.getBalance(), last.getBalanceAfter(), 0.0001);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Transaction lastTransaction(Account account) {
        var history = account.getTransactionHistory();
        return history.get(history.size() - 1);
    }
}
