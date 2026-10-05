"""Demo entry point for the Banking System."""

import sys
import os

sys.path.insert(0, os.path.dirname(__file__))

from decimal import Decimal

from repository.in_memory_account_repository import InMemoryAccountRepository
from repository.in_memory_customer_repository import InMemoryCustomerRepository
from service.account_service import AccountService
from service.transfer_service import TransferService
from models.account import AccountType


def main() -> None:
    # Bootstrap
    account_repo = InMemoryAccountRepository()
    customer_repo = InMemoryCustomerRepository()
    svc = AccountService(account_repo, customer_repo)
    transfer_svc = TransferService(svc)

    # Register customers
    alice = svc.register_customer("Alice Smith", "alice@example.com")
    bob = svc.register_customer("Bob Jones", "bob@example.com")
    print(f"Registered: {alice}")
    print(f"Registered: {bob}")

    # Open accounts with initial deposits
    alice_acc = svc.open_account(alice.id, AccountType.CHECKING, Decimal("1000"))
    bob_acc = svc.open_account(bob.id, AccountType.SAVINGS, Decimal("500"))
    print(f"\nOpened: {alice_acc}")
    print(f"Opened: {bob_acc}")

    # Deposit
    svc.deposit(alice_acc.id, Decimal("250"))
    print(f"\nAlice after deposit: {svc.get_balance(alice_acc.id):.2f}")

    # Transfer
    out, inc = transfer_svc.transfer(alice_acc.id, bob_acc.id, Decimal("200"), "Loan")
    print(f"\nTransfer out: {out}")
    print(f"Transfer in:  {inc}")

    # Final balances
    print(f"\nAlice final balance: {svc.get_balance(alice_acc.id):.2f}")
    print(f"Bob final balance:   {svc.get_balance(bob_acc.id):.2f}")

    # Transaction history
    print(f"\nAlice transaction history:")
    for tx in svc.get_transaction_history(alice_acc.id):
        print(f"  {tx}")


if __name__ == "__main__":
    main()
