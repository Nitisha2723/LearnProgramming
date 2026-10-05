import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * An immutable record of a single monetary event on an account.
 *
 * <p>Instances are created once and never mutated.  They are stored inside
 * {@link Account#transactionHistory} and surfaced by the service layer for
 * statement generation.
 *
 * <p>The nested {@link TransactionType} enum describes the nature of the event.
 */
public class Transaction {

    // -------------------------------------------------------------------------
    // Nested enum
    // -------------------------------------------------------------------------

    /**
     * Classifies the nature of a transaction so statements and reports can
     * group / filter events without string comparisons.
     */
    public enum TransactionType {
        /** Money added directly to the account (cash or cheque in). */
        DEPOSIT,
        /** Money removed directly from the account. */
        WITHDRAWAL,
        /** Funds received from another account. */
        TRANSFER_IN,
        /** Funds sent to another account. */
        TRANSFER_OUT,
        /** Interest credited by the bank. */
        INTEREST
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String          transactionId;
    private final String          accountNumber;
    private final TransactionType type;
    private final double          amount;
    private final double          balanceAfter;
    private final LocalDateTime   timestamp;
    private final String          description;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a transaction with an auto-generated UUID.
     *
     * @param accountNumber the account this transaction belongs to
     * @param type          what kind of event this is
     * @param amount        the absolute (positive) value of the event
     * @param balanceAfter  the account balance immediately after the event
     * @param timestamp     when the event occurred
     * @param description   human-readable note (e.g. "Transfer to ACC-002")
     */
    public Transaction(String          accountNumber,
                       TransactionType type,
                       double          amount,
                       double          balanceAfter,
                       LocalDateTime   timestamp,
                       String          description) {
        this.transactionId = UUID.randomUUID().toString();
        this.accountNumber = accountNumber;
        this.type          = type;
        this.amount        = amount;
        this.balanceAfter  = balanceAfter;
        this.timestamp     = timestamp;
        this.description   = description;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String          getTransactionId() { return transactionId; }
    public String          getAccountNumber() { return accountNumber; }
    public TransactionType getType()          { return type;          }
    public double          getAmount()        { return amount;        }
    public double          getBalanceAfter()  { return balanceAfter;  }
    public LocalDateTime   getTimestamp()     { return timestamp;     }
    public String          getDescription()  { return description;   }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    /**
     * Returns a tab-separated statement line suitable for printing.
     *
     * <p>Example:
     * <pre>
     *   2024-03-15 09:30:00  DEPOSIT         +  500.00  Balance:  1500.00  Initial deposit
     * </pre>
     */
    @Override
    public String toString() {
        // Show credit (+) or debit (–) sign based on type
        boolean isCredit = (type == TransactionType.DEPOSIT
                         || type == TransactionType.TRANSFER_IN
                         || type == TransactionType.INTEREST);
        String sign = isCredit ? "+" : "-";

        return String.format("%-20s  %-14s  %s%9.2f  Balance: %10.2f  %s",
                timestamp.format(DISPLAY_FMT),
                type,
                sign,
                amount,
                balanceAfter,
                description);
    }
}
