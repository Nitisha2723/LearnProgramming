"""
BankAccount demonstration — showing how the BankAccount class works.

Run this file:
    python code/basics/bank_account_demo.py
"""

from bank_account import BankAccount, Transaction, InsufficientFundsError


def separator(title: str = "") -> None:
    """Print a visual separator."""
    if title:
        print(f"\n{'─' * 20} {title} {'─' * 20}")
    else:
        print(f"\n{'─' * 60}")


def demo_basic_operations() -> None:
    """Demonstrate basic deposit and withdrawal."""
    separator("Basic Operations")

    # Create an account — __init__ is called
    alice = BankAccount("ACC001", "Alice Johnson", 1000.0)
    print(f"Created: {alice}")            # __str__ is called
    print(f"  repr: {repr(alice)}")       # __repr__ is called

    # Deposit — property reads and writes
    print(f"\nBalance before deposit: {alice.balance}")   # @property getter
    alice.deposit(500.0, "Paycheck")
    print(f"Balance after deposit: {alice.balance}")

    # Withdraw
    alice.withdraw(200.0, "Groceries")
    print(f"Balance after withdrawal: {alice.balance}")

    # Properties — read-only access
    print(f"\nAccount number: {alice.account_number}")
    print(f"Owner: {alice.owner}")


def demo_error_handling() -> None:
    """Demonstrate validation and error handling."""
    separator("Error Handling")

    account = BankAccount("ACC002", "Bob Smith", 500.0)

    # Try to withdraw more than balance
    print("Attempting to withdraw more than balance...")
    try:
        account.withdraw(1000.0)
    except InsufficientFundsError as e:
        print(f"  Caught: {e}")

    # Try negative deposit
    print("\nAttempting negative deposit...")
    try:
        account.deposit(-50.0)
    except ValueError as e:
        print(f"  Caught: {e}")

    # Try to create account with negative balance
    print("\nAttempting to create account with negative balance...")
    try:
        bad_account = BankAccount("ACC999", "Bad Account", -100.0)
    except ValueError as e:
        print(f"  Caught: {e}")

    print(f"\nBob's balance unchanged: {account.balance}")


def demo_property_setter() -> None:
    """Demonstrate @property setter."""
    separator("Property Setter")

    account = BankAccount("ACC003", "Carol Davis")
    print(f"Owner: {account.owner}")

    # Using the setter — looks like assignment but triggers validation
    account.owner = "Carol M. Davis"
    print(f"Updated owner: {account.owner}")

    # Setter validates input
    try:
        account.owner = ""   # Empty name — should fail
    except ValueError as e:
        print(f"Caught invalid owner: {e}")


def demo_transfer() -> None:
    """Demonstrate account transfers."""
    separator("Transfers")

    alice = BankAccount("ACC001", "Alice", 1000.0)
    bob = BankAccount("ACC002", "Bob", 500.0)

    print(f"Before: {alice}")
    print(f"Before: {bob}")

    alice.transfer(bob, 300.0)

    print(f"\nAfter transfer of 300.0:")
    print(f"  {alice}")
    print(f"  {bob}")

    # Try to transfer to the same account
    try:
        alice.transfer(alice, 100.0)
    except ValueError as e:
        print(f"\nCaught self-transfer: {e}")


def demo_account_statement() -> None:
    """Demonstrate the account statement."""
    separator("Account Statement")

    account = BankAccount("ACC004", "Diana Prince", 2000.0)
    account.deposit(500.0, "Freelance work")
    account.withdraw(150.0, "Rent")
    account.deposit(1000.0, "Bonus")
    account.withdraw(80.0, "Utilities")
    account.apply_interest()

    print(account.get_statement())
    print(f"\nLast 3 transactions:")
    print(account.get_statement(last_n=3))


def demo_frozen_account() -> None:
    """Demonstrate account freezing."""
    separator("Frozen Account")

    account = BankAccount("ACC005", "Eve Wilson", 1000.0)
    print(f"Account: {account}")
    print(f"Is frozen: {account.is_frozen}")

    account.freeze()
    print(f"\nAccount frozen.")
    print(f"Is frozen: {account.is_frozen}")
    print(f"  {account}")   # __str__ shows [FROZEN]

    try:
        account.deposit(100.0)
    except RuntimeError as e:
        print(f"Caught deposit attempt: {e}")

    account.unfreeze()
    print(f"\nAccount unfrozen. Deposit should work now.")
    account.deposit(100.0)
    print(f"  {account}")


def demo_dunder_methods() -> None:
    """Demonstrate dunder/magic methods."""
    separator("Dunder Methods")

    a1 = BankAccount("ACC001", "Alice", 1000.0)
    a2 = BankAccount("ACC001", "Alice Different Name", 500.0)  # Same account number
    a3 = BankAccount("ACC002", "Bob", 2000.0)

    # __eq__ — equality based on account number
    print(f"a1 == a2: {a1 == a2}")  # True — same account number
    print(f"a1 == a3: {a1 == a3}")  # False — different account number

    # __hash__ — can use in sets/dicts
    account_set = {a1, a2, a3}
    print(f"\nSet with a1, a2, a3: {len(account_set)} unique accounts")  # 2, because a1==a2

    # __bool__ — True if positive balance
    empty_account = BankAccount("ACC000", "Zero", 0.0)
    print(f"\nbool(alice, 1000): {bool(a1)}")       # True
    print(f"bool(empty, 0):  {bool(empty_account)}")  # False

    if a1:
        print("Alice's account has funds")

    # __lt__ — sort by balance
    accounts = [
        BankAccount("B001", "Bob", 500.0),
        BankAccount("A001", "Alice", 2000.0),
        BankAccount("C001", "Carol", 100.0),
    ]
    sorted_by_balance = sorted(accounts)
    print("\nSorted by balance:")
    for acc in sorted_by_balance:
        print(f"  {acc}")


def demo_class_attributes() -> None:
    """Demonstrate class attributes and class methods."""
    separator("Class Attributes & Methods")

    print(f"Total accounts created: {BankAccount.get_total_accounts()}")
    print(f"Current interest rate: {BankAccount.get_interest_rate():.1%}")

    # Create some accounts
    BankAccount("X001", "Xavier", 1000.0)
    BankAccount("Y001", "Yvonne", 2000.0)
    print(f"Total accounts after creating 2 more: {BankAccount.get_total_accounts()}")

    # Change the class-level interest rate — affects ALL accounts
    BankAccount.set_interest_rate(0.03)
    print(f"Updated interest rate: {BankAccount.get_interest_rate():.1%}")


if __name__ == "__main__":
    print("BankAccount Demo — Python OOP Encapsulation")
    print("=" * 60)

    demo_basic_operations()
    demo_error_handling()
    demo_property_setter()
    demo_transfer()
    demo_account_statement()
    demo_frozen_account()
    demo_dunder_methods()
    demo_class_attributes()

    print("\n" + "=" * 60)
    print("Demo complete!")
