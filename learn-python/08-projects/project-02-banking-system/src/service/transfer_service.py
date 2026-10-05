from decimal import Decimal
from typing import Tuple

from ..models.transaction import Transaction
from .account_service import AccountService


class TransferService:
    """Handles fund transfers between accounts."""

    def __init__(self, account_service: AccountService) -> None:
        self._account_service = account_service

    def transfer(
        self,
        from_account_id: str,
        to_account_id: str,
        amount: Decimal,
        description: str = "Transfer",
    ) -> Tuple[Transaction, Transaction]:
        """
        Transfer funds between two accounts.

        Returns:
            A tuple of (outgoing_transaction, incoming_transaction).

        Raises:
            AccountNotFoundError: If either account does not exist.
            InsufficientFundsError: If the source account lacks funds.
            ValueError: If amount is not positive.
        """
        if amount <= Decimal("0"):
            raise ValueError("Transfer amount must be positive.")

        from_account = self._account_service.get_account(from_account_id)
        to_account = self._account_service.get_account(to_account_id)

        # Withdraw from source (may raise InsufficientFundsError)
        out_tx = from_account.withdraw(
            amount, f"{description} to {to_account_id[:8]}"
        )
        # Deposit to destination
        in_tx = to_account.deposit(
            amount, f"{description} from {from_account_id[:8]}"
        )

        # Persist both accounts
        self._account_service._accounts.save(from_account)
        self._account_service._accounts.save(to_account)

        return out_tx, in_tx
