import java.util.List;
import java.util.Optional;

/**
 * Contract for storing and retrieving {@link Account} objects.
 *
 * <p>Programming against this interface instead of a concrete class lets us
 * swap the underlying storage (in-memory map today, JDBC or JPA tomorrow)
 * without touching the service layer or the tests.
 */
public interface AccountRepository {

    /**
     * Returns the account with the given account number, or empty if not found.
     *
     * @param accountNumber the identifier to look up
     */
    Optional<Account> findById(String accountNumber);

    /**
     * Returns all accounts that belong to a specific customer.
     *
     * @param ownerId the customer ID to filter by
     */
    List<Account> findByOwnerId(String ownerId);

    /**
     * Returns every account currently stored in the repository.
     */
    List<Account> findAll();

    /**
     * Persists a new account and returns it.
     *
     * @param account the account to save
     * @throws IllegalArgumentException if an account with the same number already exists
     */
    Account save(Account account);

    /**
     * Replaces the stored state of an already-persisted account with the
     * current state of the given object.
     *
     * @param account the account whose stored version should be replaced
     * @throws AccountNotFoundException if no account with that number exists
     */
    Account update(Account account);

    /**
     * Removes the account with the given account number.
     *
     * @param accountNumber the identifier to remove
     * @return {@code true} if the account existed and was removed;
     *         {@code false} if it was not found
     */
    boolean delete(String accountNumber);

    /**
     * Checks whether an account with the given number is stored.
     *
     * @param accountNumber the identifier to check
     */
    boolean existsById(String accountNumber);
}
