/**
 * BankAccountDemo — demonstrates the BankAccount class in action.
 *
 * Run this to see:
 * - Encapsulation protecting data
 * - Business rules enforced through methods
 * - Transaction history maintenance
 * - Proper error handling
 */
public class BankAccountDemo {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("BankAccount Demonstration");
        System.out.println("=".repeat(60));

        demo1_BasicOperations();
        demo2_Transfer();
        demo3_EncapsulationProtection();
        demo4_MultipleAccounts();
        demo5_TransactionHistory();
    }

    // =========================================================================
    // Demo 1: Basic Operations
    // =========================================================================
    static void demo1_BasicOperations() {
        System.out.println("\n--- Demo 1: Basic Operations ---");

        // Create an account using constructor
        BankAccount alice = new BankAccount("ACC-001", "Alice Johnson", 1000.0);
        System.out.println("Created: " + alice);
        System.out.println("Balance: $" + alice.getBalance());

        // Deposit
        alice.deposit(500.0);
        System.out.println("After depositing $500: $" + alice.getBalance());

        // Withdraw
        alice.withdraw(200.0);
        System.out.println("After withdrawing $200: $" + alice.getBalance());

        System.out.println("Final state: " + alice);
    }

    // =========================================================================
    // Demo 2: Transfer Between Accounts
    // =========================================================================
    static void demo2_Transfer() {
        System.out.println("\n--- Demo 2: Transfers ---");

        BankAccount alice = new BankAccount("ACC-001", "Alice", 2000.0);
        BankAccount bob = new BankAccount("ACC-002", "Bob", 500.0);

        System.out.println("Alice: " + alice);
        System.out.println("Bob:   " + bob);

        alice.transferTo(bob, 750.0);
        System.out.println("\nAfter Alice transfers $750 to Bob:");
        System.out.println("Alice: " + alice);
        System.out.println("Bob:   " + bob);
    }

    // =========================================================================
    // Demo 3: Encapsulation — Rules Cannot Be Bypassed
    // =========================================================================
    static void demo3_EncapsulationProtection() {
        System.out.println("\n--- Demo 3: Encapsulation in Action ---");

        BankAccount account = new BankAccount("ACC-003", "Charlie", 100.0);

        // Try to violate business rules — encapsulation prevents all of these

        System.out.println("\nAttempt 1: Deposit negative amount");
        try {
            account.deposit(-50.0);  // Should fail
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println("\nAttempt 2: Withdraw more than balance");
        try {
            account.withdraw(500.0);  // Balance is only 100.0
        } catch (IllegalStateException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println("\nAttempt 3: Withdraw zero");
        try {
            account.withdraw(0.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println("\nBalance unchanged at: $" + account.getBalance());

        // NOTE: This is impossible to compile — balance is private:
        // account.balance = 1000000.0;  // COMPILE ERROR
    }

    // =========================================================================
    // Demo 4: Multiple Independent Accounts
    // =========================================================================
    static void demo4_MultipleAccounts() {
        System.out.println("\n--- Demo 4: Multiple Independent Accounts ---");

        // Accounts are independent — changes to one don't affect others
        BankAccount[] accounts = {
            new BankAccount("SAV-001", "Alice", 5000.0),
            new BankAccount("CHK-001", "Alice", 1500.0),
            new BankAccount("SAV-002", "Bob", 10000.0),
        };

        System.out.println("All accounts:");
        for (BankAccount acc : accounts) {
            System.out.println("  " + acc);
        }

        // Deposit into first account only
        accounts[0].deposit(200.0);
        System.out.println("\nAfter depositing $200 into SAV-001:");
        for (BankAccount acc : accounts) {
            System.out.println("  " + acc);
        }

        // Equality check — same account number = same account
        BankAccount ref1 = accounts[0];
        BankAccount ref2 = new BankAccount("SAV-001", "Alice", 9999.0);  // Same account number
        System.out.println("\nEquality by account number:");
        System.out.println("ref1.equals(ref2): " + ref1.equals(ref2));  // true
        System.out.println("ref1 == ref2: " + (ref1 == ref2));           // false (different objects)

        // Use static factory method
        BankAccount newAcc = BankAccount.openNewAccount("ACC-999", "New Customer");
        System.out.println("\nAccount via factory: " + newAcc);
    }

    // =========================================================================
    // Demo 5: Transaction History
    // =========================================================================
    static void demo5_TransactionHistory() {
        System.out.println("\n--- Demo 5: Transaction History ---");

        BankAccount account = new BankAccount("ACC-100", "Diana", 500.0);
        account.deposit(300.0);
        account.withdraw(150.0);
        account.deposit(750.0);
        account.withdraw(200.0);

        System.out.println("Transaction history for " + account.getOwner() + ":");
        for (String entry : account.getTransactionHistory()) {
            System.out.println("  " + entry);
        }
        System.out.println("Total transactions: " + account.getTransactionCount());

        // Attempting to modify history should fail
        System.out.println("\nAttempting to tamper with history:");
        try {
            account.getTransactionHistory().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("History is protected — cannot be modified externally!");
        }
        System.out.println("History still intact: " + account.getTransactionCount() + " records");
    }
}
