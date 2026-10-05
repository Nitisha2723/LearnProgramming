import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a bank customer.
 *
 * <p>A customer holds references (by account number) to all accounts they own.
 * The actual account objects are stored in the repository layer; the customer
 * entity is intentionally kept thin to avoid circular object graphs.
 */
public class Customer {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** System-generated UUID — stable across renames or contact-info changes. */
    private final String customerId;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    /** When this customer record was first created. */
    private final LocalDateTime registeredAt;

    /** Account numbers belonging to this customer. */
    private final List<String> accountNumbers;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new customer.  The {@code customerId} and {@code registeredAt}
     * are assigned automatically.
     *
     * @param firstName first (given) name — must not be blank
     * @param lastName  last (family) name — must not be blank
     * @param email     contact e-mail address
     * @param phone     contact phone number
     */
    public Customer(String firstName, String lastName, String email, String phone) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name must not be blank.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name must not be blank.");
        }
        this.customerId     = UUID.randomUUID().toString();
        this.firstName      = firstName;
        this.lastName       = lastName;
        this.email          = email;
        this.phone          = phone;
        this.registeredAt   = LocalDateTime.now();
        this.accountNumbers = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Derived / convenience methods
    // -------------------------------------------------------------------------

    /** @return firstName + " " + lastName */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Associates an account number with this customer.
     *
     * @param accountNumber the account to add (ignored if already present)
     */
    public void addAccount(String accountNumber) {
        if (accountNumber != null && !accountNumbers.contains(accountNumber)) {
            accountNumbers.add(accountNumber);
        }
    }

    /**
     * Removes an account number from this customer's list.
     *
     * @param accountNumber the account to disassociate
     * @return {@code true} if it was present and removed
     */
    public boolean removeAccount(String accountNumber) {
        return accountNumbers.remove(accountNumber);
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public String        getCustomerId()    { return customerId;    }
    public String        getFirstName()     { return firstName;     }
    public String        getLastName()      { return lastName;      }
    public String        getEmail()         { return email;         }
    public String        getPhone()         { return phone;         }
    public LocalDateTime getRegisteredAt()  { return registeredAt;  }

    /** @return an unmodifiable view of the account-number list */
    public List<String> getAccountNumbers() {
        return Collections.unmodifiableList(accountNumbers);
    }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName)   { this.lastName  = lastName;  }
    public void setEmail(String email)         { this.email     = email;     }
    public void setPhone(String phone)         { this.phone     = phone;     }

    // -------------------------------------------------------------------------
    // equals / hashCode — based on the stable customerId
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer other = (Customer) o;
        return Objects.equals(customerId, other.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("Customer{id='%s', name='%s', email='%s', accounts=%d}",
                customerId, getFullName(), email, accountNumbers.size());
    }
}
