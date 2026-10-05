"""Tests for AccountService and TransferService."""

import pytest
from decimal import Decimal

from src.models.account import AccountType
from src.exceptions.banking_exceptions import (
    AccountNotFoundError,
    CustomerNotFoundError,
    DuplicateEmailError,
    InsufficientFundsError,
)


class TestCustomerRegistration:
    def test_register_returns_customer(self, service):
        c = service.register_customer("Alice", "alice@test.com")
        assert c.name == "Alice"
        assert c.email == "alice@test.com"

    def test_register_normalises_email(self, service):
        c = service.register_customer("Alice", "ALICE@TEST.COM")
        assert c.email == "alice@test.com"

    def test_register_duplicate_email_raises(self, service):
        service.register_customer("Alice", "alice@test.com")
        with pytest.raises(DuplicateEmailError):
            service.register_customer("Alice2", "alice@test.com")

    def test_get_missing_customer_raises(self, service):
        with pytest.raises(CustomerNotFoundError):
            service.get_customer("no-such-id")


class TestAccountOperations:
    def test_open_account_with_initial_deposit(self, service, alice):
        acc = service.open_account(alice.id, AccountType.CHECKING, Decimal("100"))
        assert acc.balance == Decimal("100")
        assert acc.account_type == AccountType.CHECKING

    def test_open_account_for_missing_customer_raises(self, service):
        with pytest.raises(CustomerNotFoundError):
            service.open_account("missing", AccountType.CHECKING)

    def test_deposit_increases_balance(self, service, alice_account):
        service.deposit(alice_account.id, Decimal("50"))
        assert service.get_balance(alice_account.id) == Decimal("1050")

    def test_withdraw_decreases_balance(self, service, alice_account):
        service.withdraw(alice_account.id, Decimal("100"))
        assert service.get_balance(alice_account.id) == Decimal("900")

    def test_withdraw_insufficient_funds_raises(self, service, alice_account):
        with pytest.raises(InsufficientFundsError):
            service.withdraw(alice_account.id, Decimal("9999"))

    def test_get_missing_account_raises(self, service):
        with pytest.raises(AccountNotFoundError):
            service.get_account("missing")

    def test_deposit_zero_raises(self, service, alice_account):
        with pytest.raises(ValueError):
            service.deposit(alice_account.id, Decimal("0"))

    def test_transaction_history(self, service, alice_account):
        service.deposit(alice_account.id, Decimal("50"))
        service.withdraw(alice_account.id, Decimal("25"))
        txs = service.get_transaction_history(alice_account.id)
        # initial deposit + our deposit + our withdrawal
        assert len(txs) == 3


class TestTransferService:
    def test_transfer_moves_funds(
        self, transfer_service, service, alice_account, bob_account
    ):
        transfer_service.transfer(
            alice_account.id, bob_account.id, Decimal("200")
        )
        assert service.get_balance(alice_account.id) == Decimal("800")
        assert service.get_balance(bob_account.id) == Decimal("700")

    def test_transfer_insufficient_funds_raises(
        self, transfer_service, alice_account, bob_account
    ):
        with pytest.raises(InsufficientFundsError):
            transfer_service.transfer(
                alice_account.id, bob_account.id, Decimal("9999")
            )

    def test_transfer_negative_amount_raises(
        self, transfer_service, alice_account, bob_account
    ):
        with pytest.raises(ValueError):
            transfer_service.transfer(
                alice_account.id, bob_account.id, Decimal("-1")
            )

    def test_transfer_returns_two_transactions(
        self, transfer_service, alice_account, bob_account
    ):
        out_tx, in_tx = transfer_service.transfer(
            alice_account.id, bob_account.id, Decimal("100")
        )
        assert out_tx is not None
        assert in_tx is not None
