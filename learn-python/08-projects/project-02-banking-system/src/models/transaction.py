from __future__ import annotations

import uuid
from dataclasses import dataclass, field
from datetime import datetime
from decimal import Decimal
from enum import Enum


class TransactionType(Enum):
    DEPOSIT = "DEPOSIT"
    WITHDRAWAL = "WITHDRAWAL"
    TRANSFER_IN = "TRANSFER_IN"
    TRANSFER_OUT = "TRANSFER_OUT"

    def __str__(self) -> str:
        return self.value


@dataclass
class Transaction:
    """Represents a single financial transaction."""

    account_id: str
    transaction_type: TransactionType
    amount: Decimal
    balance_after: Decimal
    description: str = ""
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    timestamp: datetime = field(default_factory=datetime.now)

    def __str__(self) -> str:
        return (f"[{self.timestamp:%Y-%m-%d %H:%M}] {self.transaction_type} "
                f"{self.amount:.2f} -> balance: {self.balance_after:.2f}")
