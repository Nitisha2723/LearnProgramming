import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BankAccountTest — comprehensive JUnit 5 test suite for BankAccount.
 *
 * JUNIT 5 CONCEPTS DEMONSTRATED:
 * - @Test: marks a method as a test case
 * - @BeforeEach: runs before each test (setup)
 * - @DisplayName: human-readable test names in test reports
 * - @Nested: groups related tests logically
 * - assertThrows: verifies that exceptions are thrown
 * - assertEquals, assertTrue, assertFalse: verify values
 * - @ParameterizedTest: run same test with multiple inputs
 */
@DisplayName("BankAccount Tests")
class BankAccountTest {

    // The account under test, re-created fresh before each test
    private BankAccount account;

    /**
     * @BeforeEach runs before EVERY test method.
     *
     * This ensures each test starts with a clean, known state.
     * Without this, a test that modifies the account would affect other tests.
     * Test isolation is critical — tests must be independent and order-agnostic.
     */
    @BeforeEach
    void setUp() {
        account = new BankAccount("ACC-001", "Alice", 1000.00);
    }

    // =========================================================================
    // Constructor Tests
    // =========================================================================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create account with correct initial balance")
        void shouldCreateAccountWithCorrectBalance() {
            BankAccount newAccount = new BankAccount("ACC-X", "Bob", 500.0);
            assertEquals(500.0, newAccount.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Should create account with zero balance when using two-arg constructor")
        void shouldCreateAccountWithZeroBalance() {
            BankAccount emptyAccount = new BankAccount("ACC-Y", "Charlie");
            assertEquals(0.0, emptyAccount.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Should throw when account number is null")
        void shouldThrowWhenAccountNumberIsNull() {
            assertThrows(IllegalArgumentException.class,
                () -> new BankAccount(null, "Alice", 100.0));
        }

        @Test
        @DisplayName("Should throw when account number is empty")
        void shouldThrowWhenAccountNumberIsEmpty() {
            assertThrows(IllegalArgumentException.class,
                () -> new BankAccount("", "Alice", 100.0));
        }

        @Test
        @DisplayName("Should throw when owner is null")
        void shouldThrowWhenOwnerIsNull() {
            assertThrows(IllegalArgumentException.class,
                () -> new BankAccount("ACC-X", null, 100.0));
        }

        @Test
        @DisplayName("Should throw when initial balance is negative")
        void shouldThrowWhenInitialBalanceIsNegative() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BankAccount("ACC-X", "Alice", -100.0)
            );
            assertTrue(exception.getMessage().contains("negative"),
                      "Exception message should mention 'negative'");
        }
    }

    // =========================================================================
    // Deposit Tests
    // =========================================================================

    @Nested
    @DisplayName("Deposit Tests")
    class DepositTests {

        @Test
        @DisplayName("Deposit should increase balance")
        void depositShouldIncreaseBalance() {
            double initialBalance = account.getBalance();
            account.deposit(500.0);
            assertEquals(initialBalance + 500.0, account.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Multiple deposits should accumulate")
        void multipleDepositsShouldAccumulate() {
            account.deposit(100.0);
            account.deposit(200.0);
            account.deposit(300.0);
            assertEquals(1600.0, account.getBalance(), 0.001);  // 1000 + 100 + 200 + 300
        }

        @Test
        @DisplayName("Deposit should throw for zero amount")
        void depositShouldThrowForZeroAmount() {
            assertThrows(IllegalArgumentException.class,
                () -> account.deposit(0.0));
        }

        @Test
        @DisplayName("Deposit should throw for negative amount")
        void depositShouldThrowForNegativeAmount() {
            assertThrows(IllegalArgumentException.class,
                () -> account.deposit(-50.0));
        }

        @Test
        @DisplayName("Balance should remain unchanged after failed deposit")
        void balanceShouldRemainAfterFailedDeposit() {
            double balanceBefore = account.getBalance();
            try {
                account.deposit(-50.0);
            } catch (IllegalArgumentException ignored) { }
            assertEquals(balanceBefore, account.getBalance(), 0.001);
        }
    }

    // =========================================================================
    // Withdrawal Tests
    // =========================================================================

    @Nested
    @DisplayName("Withdrawal Tests")
    class WithdrawalTests {

        @Test
        @DisplayName("Withdraw should decrease balance")
        void withdrawShouldDecreaseBalance() {
            account.withdraw(300.0);
            assertEquals(700.0, account.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Can withdraw entire balance")
        void canWithdrawEntireBalance() {
            account.withdraw(1000.0);
            assertEquals(0.0, account.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Withdraw should throw when insufficient funds")
        void withdrawShouldThrowWhenInsufficientFunds() {
            // account starts at 1000.0
            assertThrows(IllegalStateException.class,
                () -> account.withdraw(1500.0));
        }

        @Test
        @DisplayName("Balance should not go negative after failed withdrawal")
        void balanceShouldNotGonegative() {
            double balanceBefore = account.getBalance();
            try {
                account.withdraw(balanceBefore + 1);  // Slightly over balance
            } catch (IllegalStateException ignored) { }
            // Balance must still be non-negative
            assertTrue(account.getBalance() >= 0,
                      "Balance must never go negative");
            assertEquals(balanceBefore, account.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Withdraw should throw for zero amount")
        void withdrawShouldThrowForZeroAmount() {
            assertThrows(IllegalArgumentException.class,
                () -> account.withdraw(0.0));
        }

        @Test
        @DisplayName("Withdraw should throw for negative amount")
        void withdrawShouldThrowForNegativeAmount() {
            assertThrows(IllegalArgumentException.class,
                () -> account.withdraw(-100.0));
        }
    }

    // =========================================================================
    // Transfer Tests
    // =========================================================================

    @Nested
    @DisplayName("Transfer Tests")
    class TransferTests {
        private BankAccount targetAccount;

        @BeforeEach
        void setUpTarget() {
            targetAccount = new BankAccount("ACC-002", "Bob", 500.0);
        }

        @Test
        @DisplayName("Transfer should decrease source and increase target")
        void transferShouldMoveMoneyBetweenAccounts() {
            account.transferTo(targetAccount, 300.0);
            assertEquals(700.0, account.getBalance(), 0.001);
            assertEquals(800.0, targetAccount.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Transfer should throw when insufficient funds")
        void transferShouldThrowWhenInsufficientFunds() {
            assertThrows(IllegalStateException.class,
                () -> account.transferTo(targetAccount, 9999.0));
        }

        @Test
        @DisplayName("Transfer should throw when target is null")
        void transferShouldThrowWhenTargetIsNull() {
            assertThrows(IllegalArgumentException.class,
                () -> account.transferTo(null, 100.0));
        }

        @Test
        @DisplayName("Transfer should throw when transferring to self")
        void transferShouldThrowWhenTransferringToSelf() {
            assertThrows(IllegalArgumentException.class,
                () -> account.transferTo(account, 100.0));
        }
    }

    // =========================================================================
    // Transaction History Tests
    // =========================================================================

    @Nested
    @DisplayName("Transaction History Tests")
    class TransactionHistoryTests {

        @Test
        @DisplayName("Transaction history should record operations")
        void transactionHistoryShouldRecordOperations() {
            int initialCount = account.getTransactionCount();  // Account opening = 1
            account.deposit(100.0);
            account.withdraw(50.0);
            assertEquals(initialCount + 2, account.getTransactionCount());
        }

        @Test
        @DisplayName("Transaction history should be unmodifiable")
        void transactionHistoryShouldBeUnmodifiable() {
            List<String> history = account.getTransactionHistory();
            assertThrows(UnsupportedOperationException.class,
                () -> history.clear());
        }

        @Test
        @DisplayName("Transaction history should not change after failed operation")
        void transactionHistoryShouldNotChangeAfterFailedOperation() {
            int countBefore = account.getTransactionCount();
            try {
                account.withdraw(99999.0);  // Will fail
            } catch (IllegalStateException ignored) { }
            assertEquals(countBefore, account.getTransactionCount(),
                        "Failed operations should not be recorded");
        }
    }

    // =========================================================================
    // Equality Tests
    // =========================================================================

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Same account number means equal accounts")
        void sameAccountNumberMeansEqual() {
            BankAccount account1 = new BankAccount("ACC-999", "Alice", 100.0);
            BankAccount account2 = new BankAccount("ACC-999", "Alice", 500.0);  // Same number, different balance
            assertEquals(account1, account2);
        }

        @Test
        @DisplayName("Different account numbers means unequal accounts")
        void differentAccountNumbersMeansUnequal() {
            BankAccount account1 = new BankAccount("ACC-111", "Alice", 100.0);
            BankAccount account2 = new BankAccount("ACC-222", "Alice", 100.0);
            assertNotEquals(account1, account2);
        }

        @Test
        @DisplayName("Equal accounts must have equal hash codes")
        void equalAccountsMustHaveEqualHashCodes() {
            BankAccount account1 = new BankAccount("ACC-999", "Alice", 100.0);
            BankAccount account2 = new BankAccount("ACC-999", "Bob", 999.0);
            assertEquals(account1.hashCode(), account2.hashCode(),
                        "If equals() returns true, hashCode() must return the same value");
        }

        @Test
        @DisplayName("Account is not equal to null")
        void accountIsNotEqualToNull() {
            assertNotEquals(null, account);
        }

        @Test
        @DisplayName("Account is not equal to a different type")
        void accountIsNotEqualToDifferentType() {
            assertNotEquals("ACC-001", account);
        }
    }

    // =========================================================================
    // ToString Test
    // =========================================================================

    @Test
    @DisplayName("toString should contain account number and owner")
    void toStringShouldContainKeyInfo() {
        String str = account.toString();
        assertTrue(str.contains("ACC-001"), "toString should contain account number");
        assertTrue(str.contains("Alice"), "toString should contain owner name");
    }

    // =========================================================================
    // Parameterized Test Example
    // =========================================================================

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -100.0, -0.01})
    @DisplayName("Deposit should reject non-positive amounts")
    void depositShouldRejectNonPositiveAmounts(double invalidAmount) {
        assertThrows(IllegalArgumentException.class,
            () -> account.deposit(invalidAmount),
            "Should reject amount: " + invalidAmount);
    }
}
