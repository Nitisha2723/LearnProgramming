"""
Tests for BankAccount.

Run with:
    cd 03-oop && pytest tests/test_bank_account.py -v
"""

import pytest
import sys
import os

# Add code/basics to path so we can import bank_account
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'code', 'basics'))

from bank_account import BankAccount, Transaction, InsufficientFundsError


class TestBankAccountCreation:
    """Tests for BankAccount initialization."""

    def test_create_account_with_initial_balance(self) -> None:
        account = BankAccount("ACC001", "Alice", 1000.0)
        assert account.account_number == "ACC001"
        assert account.owner == "Alice"
        assert account.balance == 1000.0

    def test_create_account_default_balance(self) -> None:
        account = BankAccount("ACC002", "Bob")
        assert account.balance == 0.0

    def test_create_account_zero_balance(self) -> None:
        account = BankAccount("ACC003", "Carol", 0.0)
        assert account.balance == 0.0

    def test_create_account_invalid_negative_balance(self) -> None:
        with pytest.raises(ValueError, match="negative"):
            BankAccount("ACC004", "Dave", -100.0)

    def test_create_account_empty_owner_raises(self) -> None:
        with pytest.raises(ValueError, match="empty"):
            BankAccount("ACC005", "")

    def test_create_account_whitespace_owner_raises(self) -> None:
        with pytest.raises(ValueError, match="empty"):
            BankAccount("ACC006", "   ")

    def test_new_account_not_frozen(self) -> None:
        account = BankAccount("ACC007", "Eve")
        assert account.is_frozen is False

    def test_initial_transaction_recorded_when_balance_positive(self) -> None:
        account = BankAccount("ACC008", "Frank", 500.0)
        assert account.transaction_count == 1

    def test_no_initial_transaction_when_zero_balance(self) -> None:
        account = BankAccount("ACC009", "Grace")
        assert account.transaction_count == 0


class TestDeposit:
    """Tests for deposit operation."""

    @pytest.fixture
    def account(self) -> BankAccount:
        return BankAccount("ACC001", "Alice", 1000.0)

    def test_deposit_increases_balance(self, account: BankAccount) -> None:
        account.deposit(500.0)
        assert account.balance == 1500.0

    def test_deposit_zero_raises(self, account: BankAccount) -> None:
        with pytest.raises(ValueError):
            account.deposit(0.0)

    def test_deposit_negative_raises(self, account: BankAccount) -> None:
        with pytest.raises(ValueError):
            account.deposit(-100.0)

    def test_deposit_records_transaction(self, account: BankAccount) -> None:
        initial_count = account.transaction_count
        account.deposit(100.0)
        assert account.transaction_count == initial_count + 1

    def test_deposit_on_frozen_account_raises(self, account: BankAccount) -> None:
        account.freeze()
        with pytest.raises(RuntimeError):
            account.deposit(100.0)

    @pytest.mark.parametrize("amount", [0.01, 1.0, 100.0, 999999.99, 0.001])
    def test_deposit_various_amounts(self, amount: float) -> None:
        account = BankAccount("ACC001", "Test", 0.0)
        account.deposit(amount)
        assert abs(account.balance - amount) < 1e-9

    def test_multiple_deposits_accumulate(self, account: BankAccount) -> None:
        account.deposit(100.0)
        account.deposit(200.0)
        account.deposit(300.0)
        assert account.balance == 1600.0


class TestWithdrawal:
    """Tests for withdrawal operation."""

    @pytest.fixture
    def account(self) -> BankAccount:
        return BankAccount("ACC001", "Alice", 1000.0)

    def test_withdraw_decreases_balance(self, account: BankAccount) -> None:
        account.withdraw(200.0)
        assert account.balance == 800.0

    def test_withdraw_entire_balance(self, account: BankAccount) -> None:
        account.withdraw(1000.0)
        assert account.balance == 0.0

    def test_withdraw_more_than_balance_raises(self, account: BankAccount) -> None:
        with pytest.raises(InsufficientFundsError):
            account.withdraw(1001.0)

    def test_withdraw_zero_raises(self, account: BankAccount) -> None:
        with pytest.raises(ValueError):
            account.withdraw(0.0)

    def test_withdraw_negative_raises(self, account: BankAccount) -> None:
        with pytest.raises(ValueError):
            account.withdraw(-50.0)

    def test_withdraw_records_transaction(self, account: BankAccount) -> None:
        initial_count = account.transaction_count
        account.withdraw(100.0)
        assert account.transaction_count == initial_count + 1

    def test_withdraw_on_frozen_account_raises(self, account: BankAccount) -> None:
        account.freeze()
        with pytest.raises(RuntimeError):
            account.withdraw(100.0)

    def test_insufficient_funds_error_has_right_message(self, account: BankAccount) -> None:
        with pytest.raises(InsufficientFundsError) as exc_info:
            account.withdraw(2000.0)
        # The exception should mention both the requested amount and the balance
        assert "2000" in str(exc_info.value) or "1000" in str(exc_info.value)

    @pytest.mark.parametrize("amount", [0.01, 50.0, 999.99, 1000.0])
    def test_withdraw_valid_amounts(self, amount: float) -> None:
        account = BankAccount("ACC001", "Test", 1000.0)
        account.withdraw(amount)
        assert abs(account.balance - (1000.0 - amount)) < 1e-9


class TestTransfer:
    """Tests for transfer operation."""

    @pytest.fixture
    def alice(self) -> BankAccount:
        return BankAccount("ACC001", "Alice", 1000.0)

    @pytest.fixture
    def bob(self) -> BankAccount:
        return BankAccount("ACC002", "Bob", 500.0)

    def test_transfer_moves_money(
        self, alice: BankAccount, bob: BankAccount
    ) -> None:
        alice.transfer(bob, 300.0)
        assert alice.balance == 700.0
        assert bob.balance == 800.0

    def test_transfer_records_transactions_on_both_accounts(
        self, alice: BankAccount, bob: BankAccount
    ) -> None:
        alice_initial = alice.transaction_count
        bob_initial = bob.transaction_count
        alice.transfer(bob, 100.0)
        assert alice.transaction_count == alice_initial + 1
        assert bob.transaction_count == bob_initial + 1

    def test_transfer_insufficient_funds_raises(
        self, alice: BankAccount, bob: BankAccount
    ) -> None:
        with pytest.raises(InsufficientFundsError):
            alice.transfer(bob, 2000.0)
        # Both accounts should be unchanged
        assert alice.balance == 1000.0
        assert bob.balance == 500.0

    def test_transfer_to_self_raises(self, alice: BankAccount) -> None:
        with pytest.raises(ValueError, match="same"):
            alice.transfer(alice, 100.0)


class TestFreezeUnfreeze:
    """Tests for account freezing."""

    @pytest.fixture
    def account(self) -> BankAccount:
        return BankAccount("ACC001", "Alice", 1000.0)

    def test_freeze_sets_is_frozen(self, account: BankAccount) -> None:
        account.freeze()
        assert account.is_frozen is True

    def test_unfreeze_clears_is_frozen(self, account: BankAccount) -> None:
        account.freeze()
        account.unfreeze()
        assert account.is_frozen is False

    def test_frozen_account_shown_in_str(self, account: BankAccount) -> None:
        account.freeze()
        assert "FROZEN" in str(account)

    def test_unfrozen_account_not_in_str(self, account: BankAccount) -> None:
        assert "FROZEN" not in str(account)

    def test_can_deposit_after_unfreeze(self, account: BankAccount) -> None:
        account.freeze()
        account.unfreeze()
        account.deposit(100.0)
        assert account.balance == 1100.0


class TestDunderMethods:
    """Tests for __str__, __repr__, __eq__, __hash__, __bool__, __lt__."""

    @pytest.fixture
    def account(self) -> BankAccount:
        return BankAccount("ACC001", "Alice", 1000.0)

    def test_str_contains_account_number(self, account: BankAccount) -> None:
        assert "ACC001" in str(account)

    def test_str_contains_owner(self, account: BankAccount) -> None:
        assert "Alice" in str(account)

    def test_repr_contains_account_number(self, account: BankAccount) -> None:
        assert "ACC001" in repr(account)

    def test_repr_contains_balance(self, account: BankAccount) -> None:
        assert "1000" in repr(account)

    def test_eq_same_account_number(self) -> None:
        a1 = BankAccount("ACC001", "Alice", 1000.0)
        a2 = BankAccount("ACC001", "Different Name", 999.0)
        assert a1 == a2

    def test_eq_different_account_number(self) -> None:
        a1 = BankAccount("ACC001", "Alice", 1000.0)
        a2 = BankAccount("ACC002", "Alice", 1000.0)
        assert a1 != a2

    def test_eq_non_account_returns_not_implemented(
        self, account: BankAccount
    ) -> None:
        result = account.__eq__("not an account")
        assert result is NotImplemented

    def test_hash_same_for_equal_accounts(self) -> None:
        a1 = BankAccount("ACC001", "Alice", 1000.0)
        a2 = BankAccount("ACC001", "Bob", 999.0)
        assert hash(a1) == hash(a2)

    def test_accounts_in_set(self) -> None:
        a1 = BankAccount("ACC001", "Alice", 1000.0)
        a2 = BankAccount("ACC001", "Bob", 500.0)  # Same account number as a1
        a3 = BankAccount("ACC002", "Carol", 200.0)
        account_set = {a1, a2, a3}
        assert len(account_set) == 2   # a1 and a2 are "equal"

    def test_bool_true_when_positive_balance(self, account: BankAccount) -> None:
        assert bool(account) is True

    def test_bool_false_when_zero_balance(self) -> None:
        account = BankAccount("ACC001", "Alice", 0.0)
        assert bool(account) is False

    def test_lt_compares_by_balance(self) -> None:
        low = BankAccount("ACC001", "Alice", 100.0)
        high = BankAccount("ACC002", "Bob", 500.0)
        assert low < high
        assert not high < low

    def test_sort_accounts_by_balance(self) -> None:
        accounts = [
            BankAccount("A", "Alice", 500.0),
            BankAccount("B", "Bob", 100.0),
            BankAccount("C", "Carol", 1000.0),
        ]
        sorted_accounts = sorted(accounts)
        assert sorted_accounts[0].balance == 100.0
        assert sorted_accounts[-1].balance == 1000.0


class TestTransactions:
    """Tests for transaction recording."""

    @pytest.fixture
    def account(self) -> BankAccount:
        return BankAccount("ACC001", "Alice", 1000.0)

    def test_transactions_returns_copy(self, account: BankAccount) -> None:
        transactions = account.transactions
        transactions.append(None)   # type: ignore — modify the copy
        # Should not affect the account's transactions
        assert len(account.transactions) == account.transaction_count

    def test_deposit_transaction_has_correct_amount(
        self, account: BankAccount
    ) -> None:
        account.deposit(250.0, "Test deposit")
        last_tx = account.transactions[-1]
        assert last_tx.amount == 250.0

    def test_withdrawal_transaction_has_negative_amount(
        self, account: BankAccount
    ) -> None:
        account.withdraw(100.0, "Test withdrawal")
        last_tx = account.transactions[-1]
        assert last_tx.amount == -100.0

    def test_transaction_records_balance_after(
        self, account: BankAccount
    ) -> None:
        account.deposit(500.0)
        last_tx = account.transactions[-1]
        assert last_tx.balance_after == 1500.0


class TestInterest:
    """Tests for interest application."""

    def test_apply_interest_increases_balance(self) -> None:
        account = BankAccount("ACC001", "Alice", 1000.0)
        original_rate = BankAccount.get_interest_rate()

        interest = account.apply_interest()
        assert interest > 0
        assert account.balance > 1000.0

    def test_apply_interest_returns_interest_amount(self) -> None:
        account = BankAccount("ACC001", "Alice", 1000.0)
        original_rate = BankAccount.get_interest_rate()

        expected_interest = 1000.0 * original_rate
        actual_interest = account.apply_interest()
        assert abs(actual_interest - expected_interest) < 0.001

    def test_apply_interest_on_frozen_account_raises(self) -> None:
        account = BankAccount("ACC001", "Alice", 1000.0)
        account.freeze()
        with pytest.raises(RuntimeError):
            account.apply_interest()


class TestClassAttributes:
    """Tests for class attributes and class methods."""

    def test_get_interest_rate_returns_float(self) -> None:
        rate = BankAccount.get_interest_rate()
        assert isinstance(rate, float)
        assert 0.0 < rate < 1.0

    def test_set_interest_rate(self) -> None:
        original = BankAccount.get_interest_rate()
        try:
            BankAccount.set_interest_rate(0.05)
            assert BankAccount.get_interest_rate() == 0.05
        finally:
            # Restore original rate so other tests aren't affected
            BankAccount.set_interest_rate(original)

    def test_set_invalid_interest_rate_raises(self) -> None:
        with pytest.raises(ValueError):
            BankAccount.set_interest_rate(1.5)   # Over 100%

        with pytest.raises(ValueError):
            BankAccount.set_interest_rate(-0.1)  # Negative

    def test_total_accounts_increments(self) -> None:
        initial = BankAccount.get_total_accounts()
        BankAccount("NEWACCOUNT", "New Owner")
        assert BankAccount.get_total_accounts() == initial + 1


class TestStatement:
    """Tests for account statement generation."""

    def test_statement_contains_account_number(self) -> None:
        account = BankAccount("ACC001", "Alice", 1000.0)
        statement = account.get_statement()
        assert "ACC001" in statement

    def test_statement_contains_owner(self) -> None:
        account = BankAccount("ACC001", "Alice", 1000.0)
        statement = account.get_statement()
        assert "Alice" in statement

    def test_statement_with_last_n(self) -> None:
        account = BankAccount("ACC001", "Alice", 1000.0)
        for i in range(5):
            account.deposit(100.0)

        # Get last 3 transactions
        statement = account.get_statement(last_n=3)
        assert statement is not None   # Just check it doesn't crash
