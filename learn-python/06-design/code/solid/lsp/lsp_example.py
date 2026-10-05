"""
Liskov Substitution Principle — Python Examples
"""

from abc import ABC, abstractmethod
from decimal import Decimal


# ─── VIOLATION ────────────────────────────────────────────────────────────────

class BankAccountViolation:
    def __init__(self, owner: str, balance: Decimal):
        self.owner = owner
        self._balance = balance

    def deposit(self, amount: Decimal) -> None:
        self._balance += amount

    def withdraw(self, amount: Decimal) -> None:
        if amount > self._balance:
            raise ValueError("Insufficient funds")
        self._balance -= amount

    @property
    def balance(self) -> Decimal:
        return self._balance


class FixedDepositAccountViolation(BankAccountViolation):
    """VIOLATION: Withdrawals are not allowed, but the parent contract says they should work."""

    def withdraw(self, amount: Decimal) -> None:
        # LSP VIOLATION: caller expects withdraw to work (or raise InsufficientFunds)
        # Instead we raise a different exception type
        raise PermissionError("Fixed deposit accounts cannot be withdrawn from!")


def process_account(account: BankAccountViolation, amount: Decimal) -> None:
    """This function reasonably expects all BankAccounts to support withdraw."""
    account.withdraw(amount)  # Crashes for FixedDepositAccountViolation!


# ─── CORRECT ──────────────────────────────────────────────────────────────────

class Account(ABC):
    """Base account: all accounts support deposits."""

    def __init__(self, owner: str, balance: Decimal):
        self.owner = owner
        self._balance = balance

    def deposit(self, amount: Decimal) -> None:
        if amount <= 0:
            raise ValueError("Deposit amount must be positive")
        self._balance += amount

    @property
    def balance(self) -> Decimal:
        return self._balance

    @abstractmethod
    def account_type(self) -> str:
        ...


class WithdrawableAccount(Account, ABC):
    """Accounts that support withdrawals."""

    @abstractmethod
    def withdraw(self, amount: Decimal) -> None:
        ...


class CheckingAccount(WithdrawableAccount):
    def withdraw(self, amount: Decimal) -> None:
        if amount <= 0:
            raise ValueError("Withdrawal amount must be positive")
        if amount > self._balance:
            raise ValueError(f"Insufficient funds: balance is {self._balance}")
        self._balance -= amount

    def account_type(self) -> str:
        return "Checking"


class SavingsAccount(WithdrawableAccount):
    """Savings account with withdrawal limit."""

    def __init__(self, owner: str, balance: Decimal, monthly_limit: int = 6):
        super().__init__(owner, balance)
        self._monthly_withdrawals = 0
        self._monthly_limit = monthly_limit

    def withdraw(self, amount: Decimal) -> None:
        if self._monthly_withdrawals >= self._monthly_limit:
            raise ValueError(f"Monthly withdrawal limit ({self._monthly_limit}) reached")
        if amount > self._balance:
            raise ValueError(f"Insufficient funds: balance is {self._balance}")
        self._balance -= amount
        self._monthly_withdrawals += 1

    def account_type(self) -> str:
        return "Savings"


class FixedDepositAccount(Account):
    """Fixed deposit: NO withdrawal capability. Does NOT inherit from WithdrawableAccount."""

    def __init__(self, owner: str, balance: Decimal, maturity_months: int):
        super().__init__(owner, balance)
        self._maturity_months = maturity_months

    def account_type(self) -> str:
        return f"Fixed Deposit ({self._maturity_months} months)"

    def interest_rate(self) -> Decimal:
        return Decimal("0.05")  # 5% annual


def process_withdrawable_accounts(
    accounts: list[WithdrawableAccount], amount: Decimal
) -> None:
    """The type annotation is our contract: only withdrawable accounts here."""
    for account in accounts:
        try:
            account.withdraw(amount)
            print(f"{account.account_type()}: withdrew ${amount}, balance: ${account.balance}")
        except ValueError as e:
            print(f"{account.account_type()}: withdrawal failed: {e}")


if __name__ == "__main__":
    checking = CheckingAccount("Alice", Decimal("1000"))
    savings = SavingsAccount("Bob", Decimal("5000"), monthly_limit=3)

    # These are substitutable for each other
    process_withdrawable_accounts([checking, savings], Decimal("100"))

    # Fixed deposit is not in the withdrawable list — correct design
    fixed = FixedDepositAccount("Charlie", Decimal("10000"), 12)
    print(f"\n{fixed.account_type()}: balance ${fixed.balance}, rate {fixed.interest_rate()}")
