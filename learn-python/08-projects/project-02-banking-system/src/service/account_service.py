from decimal import Decimal
from typing import List

from ..models.account import Account, AccountType
from ..models.customer import Customer
from ..models.transaction import Transaction
from ..repository.account_repository import AccountRepository
from ..repository.customer_repository import CustomerRepository
from ..exceptions.banking_exceptions import (
    AccountNotFoundError,
    CustomerNotFoundError,
    DuplicateEmailError,
)


class AccountService:
    """Core banking operations: customers, accounts, deposits, withdrawals."""

    def __init__(
        self,
        account_repo: AccountRepository,
        customer_repo: CustomerRepository,
    ) -> None:
        self._accounts = account_repo
        self._customers = customer_repo

    # ── Customer operations ─────────────────────────────────────────────────

    def register_customer(self, name: str, email: str) -> Customer:
        """Register a new customer. Raises DuplicateEmailError if email taken."""
        if self._customers.find_by_email(email) is not None:
            raise DuplicateEmailError(email)
        customer = Customer(name=name, email=email)
        return self._customers.save(customer)

    def get_customer(self, customer_id: str) -> Customer:
        customer = self._customers.find_by_id(customer_id)
        if customer is None:
            raise CustomerNotFoundError(customer_id)
        return customer

    def get_all_customers(self) -> List[Customer]:
        return self._customers.find_all()

    # ── Account operations ──────────────────────────────────────────────────

    def open_account(
        self,
        customer_id: str,
        account_type: AccountType = AccountType.CHECKING,
        initial_deposit: Decimal = Decimal("0.00"),
    ) -> Account:
        """Open a new account for an existing customer."""
        self.get_customer(customer_id)  # validates customer exists
        account = Account(owner_id=customer_id, account_type=account_type)
        if initial_deposit > Decimal("0"):
            account.deposit(initial_deposit, "Initial deposit")
        return self._accounts.save(account)

    def get_account(self, account_id: str) -> Account:
        account = self._accounts.find_by_id(account_id)
        if account is None:
            raise AccountNotFoundError(account_id)
        return account

    def get_customer_accounts(self, customer_id: str) -> List[Account]:
        self.get_customer(customer_id)
        return self._accounts.find_by_owner(customer_id)

    def deposit(self, account_id: str, amount: Decimal) -> Transaction:
        account = self.get_account(account_id)
        tx = account.deposit(amount)
        self._accounts.save(account)
        return tx

    def withdraw(self, account_id: str, amount: Decimal) -> Transaction:
        account = self.get_account(account_id)
        tx = account.withdraw(amount)
        self._accounts.save(account)
        return tx

    def get_balance(self, account_id: str) -> Decimal:
        return self.get_account(account_id).balance

    def get_transaction_history(self, account_id: str) -> List[Transaction]:
        return self.get_account(account_id).get_transactions()
