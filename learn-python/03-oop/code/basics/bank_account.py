"""
BankAccount — demonstrating Python encapsulation patterns.

This module shows:
- Convention-based "private" attributes with _underscore prefix
- Properties with @property, @setter, @deleter
- Validation inside setters
- Dunder methods: __str__, __repr__, __eq__
- Type hints throughout

Compare with Java:
  - Python uses conventions (_balance) instead of enforcement (private)
  - @property replaces verbose getters/setters
  - The external API looks like plain attribute access
"""

from typing import List, Optional
from dataclasses import dataclass, field
from datetime import datetime


@dataclass
class Transaction:
    """Immutable record of a single bank transaction.

    Using @dataclass(frozen=True) gives us:
    - Auto-generated __init__, __repr__, __eq__
    - Immutability (can't be modified after creation)
    - Hashability (can be stored in sets, dict keys)
    """
    amount: float                          # Positive = deposit, negative = withdrawal
    description: str
    timestamp: datetime = field(default_factory=datetime.now)
    balance_after: float = 0.0

    def __str__(self) -> str:
        sign = "+" if self.amount >= 0 else ""
        return f"{self.timestamp.strftime('%Y-%m-%d %H:%M')} | {sign}{self.amount:.2f} | {self.description} | Balance: {self.balance_after:.2f}"


class InsufficientFundsError(Exception):
    """Raised when a withdrawal would exceed the account balance.

    Custom exceptions make error handling much clearer than generic ValueError.
    """

    def __init__(self, amount: float, balance: float):
        self.amount = amount
        self.balance = balance
        super().__init__(f"Cannot withdraw {amount:.2f}: insufficient funds (balance: {balance:.2f})")


class BankAccount:
    """
    A bank account demonstrating Python's approach to encapsulation.

    Python's encapsulation philosophy:
    - No true private attributes — Python trusts the developer
    - _single_underscore means "internal, please don't access directly"
    - @property provides controlled access identical to Java getters/setters
    - But the syntax is much cleaner: account.balance instead of account.getBalance()

    Key Python differences from Java:
    - No 'private' keyword — convention _name is used instead
    - @property replaces verbose getter methods
    - @property.setter replaces setter methods
    - Type hints document the expected types without enforcing them at runtime
    """

    # Class attribute — shared across ALL BankAccount instances
    # This is like a Java static field
    _total_accounts: int = 0
    _interest_rate: float = 0.02  # 2% annual interest rate

    def __init__(
        self,
        account_number: str,
        owner: str,
        initial_balance: float = 0.0
    ) -> None:
        """Initialize a new BankAccount.

        Note: __init__ does NOT create the object — it initializes it.
        The object already exists when __init__ runs (created by __new__).

        Args:
            account_number: Unique identifier for this account
            owner: Full name of the account owner
            initial_balance: Starting balance (default: 0.0)

        Raises:
            ValueError: If initial_balance is negative or owner is empty
        """
        # Validate inputs before setting attributes
        if not owner.strip():
            raise ValueError("Account owner name cannot be empty")
        if initial_balance < 0:
            raise ValueError(f"Initial balance cannot be negative: {initial_balance}")

        # _name convention: "protected" — internal to this class
        # Not enforced by Python, but signals "don't access directly"
        self._account_number: str = account_number
        self._owner: str = owner
        self._balance: float = initial_balance
        self._transactions: List[Transaction] = []
        self._is_frozen: bool = False

        # Record opening transaction if initial balance > 0
        if initial_balance > 0:
            self._transactions.append(Transaction(
                amount=initial_balance,
                description="Account opening deposit",
                balance_after=initial_balance
            ))

        # Update class attribute — affects ALL BankAccount instances
        BankAccount._total_accounts += 1

    # -----------------------------------------------------------------------
    # Properties — the Pythonic way to do getters (and setters)
    # -----------------------------------------------------------------------

    @property
    def balance(self) -> float:
        """Current account balance.

        This is a read-only property — no setter defined.
        Balance can only change through deposit(), withdraw(), transfer().

        Python: account.balance          (looks like attribute access)
        Java:   account.getBalance()     (explicit method call)
        """
        return self._balance

    @property
    def account_number(self) -> str:
        """Account number — read-only after creation."""
        return self._account_number

    @property
    def owner(self) -> str:
        """Account owner name."""
        return self._owner

    @owner.setter
    def owner(self, new_name: str) -> None:
        """Update the owner name with validation.

        Python: account.owner = "New Name"      (assignment syntax)
        Java:   account.setOwner("New Name")    (explicit setter call)
        """
        if not new_name.strip():
            raise ValueError("Owner name cannot be empty")
        self._owner = new_name.strip()

    @property
    def is_frozen(self) -> bool:
        """Whether this account is frozen (no transactions allowed)."""
        return self._is_frozen

    @property
    def transaction_count(self) -> int:
        """Number of transactions on this account."""
        return len(self._transactions)

    @property
    def transactions(self) -> List[Transaction]:
        """Copy of the transaction list — returns a copy to prevent external modification.

        Returning a copy is important: if we returned self._transactions directly,
        external code could modify it (e.g., list.append()) and bypass our logic.
        """
        return list(self._transactions)  # Return a copy!

    # -----------------------------------------------------------------------
    # Class-level properties (using @classmethod instead of @property)
    # Note: In Python 3.13+, chaining @classmethod with @property is deprecated
    # -----------------------------------------------------------------------

    @classmethod
    def get_total_accounts(cls) -> int:
        """Return the total number of BankAccount instances created."""
        return cls._total_accounts

    @classmethod
    def get_interest_rate(cls) -> float:
        """Return the current interest rate."""
        return cls._interest_rate

    @classmethod
    def set_interest_rate(cls, rate: float) -> None:
        """Set the interest rate for all accounts.

        This is a class method — affects ALL BankAccount instances.
        In Java, this would be a static method.
        """
        if not 0.0 <= rate <= 1.0:
            raise ValueError(f"Interest rate must be between 0.0 and 1.0, got {rate}")
        cls._interest_rate = rate

    # -----------------------------------------------------------------------
    # Instance methods — the main operations
    # -----------------------------------------------------------------------

    def deposit(self, amount: float, description: str = "Deposit") -> None:
        """Deposit money into the account.

        Args:
            amount: Amount to deposit (must be positive)
            description: Optional description for the transaction

        Raises:
            ValueError: If amount is not positive
            RuntimeError: If account is frozen
        """
        self._check_not_frozen()

        if amount <= 0:
            raise ValueError(f"Deposit amount must be positive, got {amount}")

        self._balance += amount
        self._record_transaction(amount, description)

    def withdraw(self, amount: float, description: str = "Withdrawal") -> None:
        """Withdraw money from the account.

        Args:
            amount: Amount to withdraw (must be positive)
            description: Optional description for the transaction

        Raises:
            ValueError: If amount is not positive
            InsufficientFundsError: If balance is insufficient
            RuntimeError: If account is frozen
        """
        self._check_not_frozen()

        if amount <= 0:
            raise ValueError(f"Withdrawal amount must be positive, got {amount}")

        # Custom exception is clearer than a generic ValueError
        if amount > self._balance:
            raise InsufficientFundsError(amount, self._balance)

        self._balance -= amount
        self._record_transaction(-amount, description)

    def transfer(self, target_account: "BankAccount", amount: float) -> None:
        """Transfer money from this account to another account.

        This operation is atomic in our simple model: both accounts
        are updated together. In a real system, this would use a database
        transaction to ensure atomicity.

        Args:
            target_account: The account to transfer money to
            amount: Amount to transfer

        Raises:
            ValueError: If amount is not positive or transferring to self
            InsufficientFundsError: If balance is insufficient
        """
        if target_account is self:
            raise ValueError("Cannot transfer to the same account")

        # withdraw() and deposit() each do their own validation
        self.withdraw(amount, f"Transfer to {target_account.account_number}")
        target_account.deposit(amount, f"Transfer from {self._account_number}")

    def apply_interest(self) -> float:
        """Apply annual interest to the account.

        Returns:
            The interest amount that was added
        """
        self._check_not_frozen()

        interest = self._balance * BankAccount._interest_rate
        if interest > 0:
            self._balance += interest
            self._record_transaction(interest, f"Interest ({BankAccount._interest_rate:.1%})")
        return interest

    def freeze(self) -> None:
        """Freeze the account to prevent transactions."""
        self._is_frozen = True

    def unfreeze(self) -> None:
        """Unfreeze the account to allow transactions again."""
        self._is_frozen = False

    def get_statement(self, last_n: Optional[int] = None) -> str:
        """Generate an account statement.

        Args:
            last_n: If provided, show only the last N transactions

        Returns:
            Formatted account statement as a string
        """
        lines = [
            f"Account Statement",
            f"Account: {self._account_number}",
            f"Owner: {self._owner}",
            f"Current Balance: {self._balance:.2f}",
            f"{'=' * 60}"
        ]

        transactions = self._transactions
        if last_n is not None:
            transactions = transactions[-last_n:]

        if not transactions:
            lines.append("No transactions")
        else:
            for t in transactions:
                lines.append(str(t))

        return "\n".join(lines)

    # -----------------------------------------------------------------------
    # "Private" helper methods — _underscore prefix signals internal use
    # -----------------------------------------------------------------------

    def _check_not_frozen(self) -> None:
        """Raise RuntimeError if the account is frozen.

        This is an internal helper method. The _underscore signals
        that external code should not call this directly.
        """
        if self._is_frozen:
            raise RuntimeError(f"Account {self._account_number} is frozen")

    def _record_transaction(self, amount: float, description: str) -> None:
        """Record a transaction in the transaction history.

        Internal method — should not be called directly from outside.
        Creates an immutable Transaction record and appends to the list.
        """
        self._transactions.append(Transaction(
            amount=amount,
            description=description,
            balance_after=self._balance
        ))

    # -----------------------------------------------------------------------
    # Dunder methods — make the object work with Python's built-in operations
    # -----------------------------------------------------------------------

    def __str__(self) -> str:
        """Human-readable representation.

        Called by: print(account), str(account), f"{account}"

        Python: print(account)           → "BankAccount[ACC001] Alice: 1000.00"
        Java:   System.out.println(acc)  → requires toString() override
        """
        status = " [FROZEN]" if self._is_frozen else ""
        return f"BankAccount[{self._account_number}] {self._owner}: {self._balance:.2f}{status}"

    def __repr__(self) -> str:
        """Developer-oriented representation.

        Called by: repr(account), in the REPL, in collections like lists.
        Should ideally be valid Python that recreates the object.

        Python: repr(account)     → "BankAccount(account_number='ACC001', owner='Alice', balance=1000.00)"
        Java:   No direct equivalent (toString() serves both purposes)
        """
        return (
            f"BankAccount("
            f"account_number={self._account_number!r}, "
            f"owner={self._owner!r}, "
            f"balance={self._balance:.2f})"
        )

    def __eq__(self, other: object) -> bool:
        """Check equality based on account number.

        Two BankAccount objects are equal if they have the same account number.

        Python: account1 == account2    → checks __eq__
        Java:   account1.equals(account2) → requires equals() override
        """
        if not isinstance(other, BankAccount):
            return NotImplemented   # Tell Python we can't compare with this type
        return self._account_number == other._account_number

    def __hash__(self) -> int:
        """Hash based on account number.

        If you define __eq__, you should also define __hash__ if the object
        should be usable in sets or as a dict key.

        Accounts with the same account_number are equal AND have the same hash.
        """
        return hash(self._account_number)

    def __bool__(self) -> bool:
        """Return True if the account has a positive balance.

        Called by: bool(account), if account:

        This is a design choice — you might want different semantics.
        """
        return self._balance > 0

    def __lt__(self, other: "BankAccount") -> bool:
        """Compare accounts by balance (for sorting).

        Allows: sorted([account1, account2, account3])
        """
        if not isinstance(other, BankAccount):
            return NotImplemented
        return self._balance < other._balance
