import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reporting and interest-application service.
 *
 * <p>Reads transaction data from account objects obtained via the repository
 * and applies monthly interest to savings accounts.
 */
public class TransactionService {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final AccountRepository repo;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param repo repository used to retrieve account data
     */
    public TransactionService(AccountRepository repo) {
        if (repo == null) throw new IllegalArgumentException("Repository must not be null.");
        this.repo = repo;
    }

    // -------------------------------------------------------------------------
    // History & statements
    // -------------------------------------------------------------------------

    /**
     * Returns all transactions for the account, sorted oldest-first.
     *
     * @param accountNumber target account
     * @throws AccountNotFoundException if the account does not exist
     */
    public List<Transaction> getTransactionHistory(String accountNumber) {
        Account account = findOrThrow(accountNumber);
        return account.getTransactionHistory()
                      .stream()
                      .sorted(Comparator.comparing(Transaction::getTimestamp))
                      .collect(Collectors.toList());
    }

    /**
     * Returns transactions for a specific calendar month, sorted oldest-first.
     *
     * @param accountNumber target account
     * @param year          four-digit calendar year (e.g. 2024)
     * @param month         1 = January … 12 = December
     * @throws AccountNotFoundException if the account does not exist
     */
    public List<Transaction> getMonthlyStatement(String accountNumber, int year, int month) {
        Account account = findOrThrow(accountNumber);
        return account.getTransactionHistory()
                      .stream()
                      .filter(t -> t.getTimestamp().getYear()        == year
                                && t.getTimestamp().getMonthValue()  == month)
                      .sorted(Comparator.comparing(Transaction::getTimestamp))
                      .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Monthly aggregates
    // -------------------------------------------------------------------------

    /**
     * Sums all DEPOSIT and TRANSFER_IN amounts for the given month.
     *
     * @param accountNumber target account
     * @param year          calendar year
     * @param month         1–12
     */
    public double getTotalDeposits(String accountNumber, int year, int month) {
        return getMonthlyStatement(accountNumber, year, month)
                .stream()
                .filter(t -> t.getType() == Transaction.TransactionType.DEPOSIT
                          || t.getType() == Transaction.TransactionType.TRANSFER_IN)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Sums all WITHDRAWAL and TRANSFER_OUT amounts for the given month.
     *
     * @param accountNumber target account
     * @param year          calendar year
     * @param month         1–12
     */
    public double getTotalWithdrawals(String accountNumber, int year, int month) {
        return getMonthlyStatement(accountNumber, year, month)
                .stream()
                .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL
                          || t.getType() == Transaction.TransactionType.TRANSFER_OUT)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // -------------------------------------------------------------------------
    // Interest
    // -------------------------------------------------------------------------

    /**
     * Applies monthly interest to every {@link SavingsAccount} in the repository.
     *
     * <p>Call this method once per month (e.g. via a scheduled job).  Each
     * account's balance is updated in-place and the repository is updated so
     * the change is persisted.
     *
     * @return the number of accounts that received an interest credit
     */
    public int applyMonthlyInterestToAll() {
        int count = 0;
        for (Account account : repo.findAll()) {
            if (account instanceof SavingsAccount) {
                SavingsAccount sa = (SavingsAccount) account;
                sa.applyMonthlyInterest();
                repo.update(sa);
                count++;
            }
        }
        return count;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Account findOrThrow(String accountNumber) {
        return repo.findById(accountNumber)
                   .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }
}
