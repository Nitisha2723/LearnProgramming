import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of {@link AccountRepository} backed by a
 * {@link HashMap}.
 *
 * <p>All data lives in the JVM heap — nothing is persisted between runs.
 * Suitable for unit tests and demos; replace with a database-backed
 * implementation when persistence is required.
 *
 * <p>This class is not thread-safe.  Wrap it in a synchronised decorator or
 * use a {@link java.util.concurrent.ConcurrentHashMap} if concurrent access
 * is needed.
 */
public class InMemoryAccountRepository implements AccountRepository {

    /** Primary store: accountNumber → Account object. */
    private final Map<String, Account> store = new HashMap<>();

    // -------------------------------------------------------------------------
    // AccountRepository implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Account> findById(String accountNumber) {
        if (accountNumber == null) return Optional.empty();
        return Optional.ofNullable(store.get(accountNumber));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Iterates all stored accounts and collects those whose
     * {@link Account#getOwnerId()} matches.
     */
    @Override
    public List<Account> findByOwnerId(String ownerId) {
        List<Account> result = new ArrayList<>();
        for (Account account : store.values()) {
            if (account.getOwnerId().equals(ownerId)) {
                result.add(account);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Account> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if an account with the same number is
     *                                  already stored
     */
    @Override
    public Account save(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Cannot save a null account.");
        }
        if (store.containsKey(account.getAccountNumber())) {
            throw new IllegalArgumentException(
                    "Account already exists: " + account.getAccountNumber());
        }
        store.put(account.getAccountNumber(), account);
        return account;
    }

    /**
     * {@inheritDoc}
     *
     * @throws AccountNotFoundException if the account does not exist yet
     */
    @Override
    public Account update(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Cannot update a null account.");
        }
        if (!store.containsKey(account.getAccountNumber())) {
            throw new AccountNotFoundException(account.getAccountNumber());
        }
        store.put(account.getAccountNumber(), account);
        return account;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(String accountNumber) {
        return store.remove(accountNumber) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsById(String accountNumber) {
        if (accountNumber == null) return false;
        return store.containsKey(accountNumber);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Returns the number of accounts currently stored — useful for assertions
     * in tests.
     */
    public int size() {
        return store.size();
    }
}
