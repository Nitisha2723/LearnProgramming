import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AccountService}.
 *
 * <p>Uses Mockito to mock {@link AccountRepository} so the service is tested
 * in complete isolation from storage concerns.
 */
@DisplayName("AccountService tests")
class AccountServiceTest {

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    private AccountRepository mockRepo;
    private AccountService    service;

    @BeforeEach
    void setUp() {
        mockRepo = Mockito.mock(AccountRepository.class);
        service  = new AccountService(mockRepo);

        // Default: save() returns the argument; update() returns the argument
        when(mockRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(mockRepo.update(any(Account.class))).thenAnswer(i -> i.getArgument(0));
    }

    // -------------------------------------------------------------------------
    // openSavingsAccount
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("openSavingsAccount creates a SavingsAccount with correct owner and saves it")
    void testOpenSavingsAccount() {
        SavingsAccount account = service.openSavingsAccount("customer-1", 500.00);

        assertNotNull(account);
        assertEquals("customer-1", account.getOwnerId());
        assertEquals("SAVINGS", account.getAccountType());
        assertEquals(500.00, account.getBalance(), 0.001);

        verify(mockRepo, times(1)).save(account);
    }

    // -------------------------------------------------------------------------
    // deposit — parameterized
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "deposit of {0} should update balance")
    @ValueSource(doubles = {100.0, 500.0, 1000.0, 5000.0})
    @DisplayName("deposit updates balance for various amounts")
    void testDeposit_updatesBalance(double depositAmount) {
        SavingsAccount account = new SavingsAccount("SAV-001", "customer-1", 200.00);
        when(mockRepo.findById("SAV-001")).thenReturn(Optional.of(account));

        service.deposit("SAV-001", depositAmount);

        assertEquals(200.00 + depositAmount, account.getBalance(), 0.001);
        verify(mockRepo, times(1)).update(account);
    }

    // -------------------------------------------------------------------------
    // withdraw — insufficient funds
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("withdraw on savings with too little balance throws InsufficientFundsException")
    void testWithdraw_insufficientFunds() {
        SavingsAccount account = new SavingsAccount("SAV-002", "customer-2", 150.00);
        // available = 150 - 100 (MIN_BALANCE) = 50
        when(mockRepo.findById("SAV-002")).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class,
                () -> service.withdraw("SAV-002", 100.00),
                "Withdrawing 100 when only 50 is available should throw.");

        // Repository must NOT be updated when withdrawal fails
        verify(mockRepo, never()).update(any());
    }

    // -------------------------------------------------------------------------
    // transfer — success
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transfer moves funds between two accounts and updates both")
    void testTransfer_success() throws InsufficientFundsException {
        SavingsAccount  from = new SavingsAccount("SAV-003",  "customer-1", 1_000.00);
        CheckingAccount to   = new CheckingAccount("CHK-003", "customer-2", 200.00);

        when(mockRepo.findById("SAV-003")).thenReturn(Optional.of(from));
        when(mockRepo.findById("CHK-003")).thenReturn(Optional.of(to));

        service.transfer("SAV-003", "CHK-003", 400.00);

        // Source should have 600 (1000 - 400)
        assertEquals(600.00, from.getBalance(), 0.001);
        // Destination should have 600 (200 + 400)
        assertEquals(600.00, to.getBalance(), 0.001);

        // Both accounts must be persisted
        verify(mockRepo, times(1)).update(from);
        verify(mockRepo, times(1)).update(to);
    }

    // -------------------------------------------------------------------------
    // transfer — source account not found
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transfer throws AccountNotFoundException when source account does not exist")
    void testTransfer_sourceAccountNotFound() {
        when(mockRepo.findById("INVALID")).thenReturn(Optional.empty());
        when(mockRepo.findById("CHK-004")).thenReturn(
                Optional.of(new CheckingAccount("CHK-004", "c2", 500.00)));

        assertThrows(AccountNotFoundException.class,
                () -> service.transfer("INVALID", "CHK-004", 100.00));

        verify(mockRepo, never()).update(any());
    }

    // -------------------------------------------------------------------------
    // getStatement
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getStatement returns the complete transaction history of the account")
    void testGetStatement_returnsTransactions() throws InsufficientFundsException {
        SavingsAccount account = new SavingsAccount("SAV-005", "customer-3", 500.00);
        account.deposit(100.00);
        account.withdraw(50.00);

        when(mockRepo.findById("SAV-005")).thenReturn(Optional.of(account));

        List<Transaction> statement = service.getStatement("SAV-005");

        // 3 transactions: opening deposit, explicit deposit, withdrawal
        assertEquals(3, statement.size());
    }
}
