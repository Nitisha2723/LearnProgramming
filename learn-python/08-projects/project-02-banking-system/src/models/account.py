from __future__ import annotations

import uuid
from dataclasses import dataclass, field
from datetime import datetime
from decimal import Decimal
from enum import Enum
from typing import List

from .transaction import Transaction, TransactionType


class AccountType(Enum):
    CHECKING = "CHECKING"
    SAVINGS = "SAVINGS"

    def __str__(self) -> str:
        return self.value


@dataclass
class Account:
    """Represents a bank account."""

    owner_id: str
    account_type: AccountType = AccountType.CHECKING
    balance: Decimal = field(default_factory=lambda: Decimal("0.00"))
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    account_number: str = field(
        default_factory=lambda: f"ACC{uuid.uuid4().hex[:8].upper()}"
    )
    created_at: datetime = field(default_factory=datetime.now)
    _transactions: List[Transaction] = field(default_factory=list, repr=False)

    def deposit(self, amount: Decimal, description: str = "Deposit") -> Transaction:
        """Deposit money into the account."""
        from ..exceptions.banking_exceptions import InsufficientFundsError
        if amount <= Decimal("0"):
            raise ValueError(f"Deposit amount must be positive, got {amount}")
        self.balance += amount
        tx = Transaction(
            account_id=self.id,
            transaction_type=TransactionType.DEPOSIT,
            amount=amount,
            balance_after=self.balance,
            description=description,
        )
        self._transactions.append(tx)
        return tx

    def withdraw(self, amount: Decimal, description: str = "Withdrawal") -> Transaction:
        """Withdraw money from the account."""
        from ..exceptions.banking_exceptions import InsufficientFundsError
        if amount <= Decimal("0"):
            raise ValueError(f"Withdrawal amount must be positive, got {amount}")
        if amount > self.balance:
            raise InsufficientFundsError(self.id, amount, self.balance)
        self.balance -= amount
        tx = Transaction(
            account_id=self.id,
            transaction_type=TransactionType.WITHDRAWAL,
            amount=amount,
            balance_after=self.balance,
            description=description,
        )
        self._transactions.append(tx)
        return tx

    def get_transactions(self) -> List[Transaction]:
        """Return a copy of the transaction history."""
        return list(self._transactions)

    def __str__(self) -> str:
        return (f"Account({self.account_number}, {self.account_type}, "
                f"balance={self.balance:.2f})")
