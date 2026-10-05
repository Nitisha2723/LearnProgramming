"""
test_exceptions.py — pytest tests for exceptions and file I/O

Topics tested:
  - Custom exceptions
  - try/except/else/finally behavior
  - Exception chaining
  - File I/O with real temp files
  - Mocking file I/O with unittest.mock
  - pytest fixtures for temp directories
"""

import json
import pytest
from pathlib import Path
from unittest.mock import patch, mock_open, MagicMock


# =============================================================================
# Custom Exception Classes (defined here for testing)
# =============================================================================

class AppError(Exception):
    """Base application exception."""

class ValidationError(AppError):
    def __init__(self, field: str, message: str):
        self.field = field
        self.message = message
        super().__init__(f"Validation error on '{field}': {message}")

class NotFoundError(AppError):
    def __init__(self, resource: str, id_value):
        self.resource = resource
        self.id_value = id_value
        super().__init__(f"{resource} not found: {id_value!r}")

class InsufficientFundsError(AppError):
    def __init__(self, balance: float, amount: float):
        self.balance = balance
        self.amount = amount
        self.shortfall = amount - balance
        super().__init__(
            f"Cannot withdraw ${amount:.2f}: balance is ${balance:.2f}"
        )


# =============================================================================
# Functions under test
# =============================================================================

def parse_integer(text: str) -> int:
    """Parse a string to integer, raising ValueError on failure."""
    try:
        return int(text)
    except ValueError:
        raise ValueError(f"Cannot parse {text!r} as integer") from None


def read_json_file(path: Path) -> dict:
    """Read a JSON file. Raises FileNotFoundError or ValueError."""
    try:
        with open(path) as f:
            return json.load(f)
    except FileNotFoundError:
        raise
    except json.JSONDecodeError as e:
        raise ValueError(f"Invalid JSON in {path}: {e}") from e


def safe_divide(a: float, b: float) -> float:
    """Divide a by b. Raises ZeroDivisionError if b is zero."""
    return a / b


class BankAccount:
    def __init__(self, owner: str, balance: float = 0.0):
        if not owner.strip():
            raise ValidationError("owner", "Cannot be empty")
        if balance < 0:
            raise ValidationError("balance", "Must be non-negative")
        self.owner = owner
        self.balance = balance

    def deposit(self, amount: float) -> None:
        if amount <= 0:
            raise ValidationError("amount", "Must be positive")
        self.balance += amount

    def withdraw(self, amount: float) -> None:
        if amount <= 0:
            raise ValidationError("amount", "Must be positive")
        if amount > self.balance:
            raise InsufficientFundsError(self.balance, amount)
        self.balance -= amount


# =============================================================================
# Fixtures
# =============================================================================

@pytest.fixture
def tmp_dir(tmp_path):
    """Use pytest's built-in tmp_path fixture."""
    return tmp_path


@pytest.fixture
def json_file(tmp_path):
    """Create a valid JSON file and return its path."""
    data = {"name": "Alice", "scores": [85, 92, 78], "active": True}
    path = tmp_path / "test_data.json"
    path.write_text(json.dumps(data, indent=2))
    return path, data


@pytest.fixture
def bank_account():
    """Return a BankAccount with $100 balance."""
    return BankAccount("Alice", 100.0)


# =============================================================================
# Tests: parse_integer
# =============================================================================

class TestParseInteger:

    @pytest.mark.parametrize("text, expected", [
        ("42",   42),
        ("-5",   -5),
        ("0",    0),
        ("1000", 1000),
    ])
    def test_valid_integers(self, text, expected):
        assert parse_integer(text) == expected

    @pytest.mark.parametrize("text", [
        "hello",
        "3.14",
        "",
        "12abc",
    ])
    def test_invalid_raises_value_error(self, text):
        with pytest.raises(ValueError):
            parse_integer(text)

    def test_error_message_contains_input(self):
        with pytest.raises(ValueError, match="'not_a_number'"):
            parse_integer("not_a_number")


# =============================================================================
# Tests: BankAccount exceptions
# =============================================================================

class TestBankAccount:

    def test_valid_creation(self):
        account = BankAccount("Alice", 100.0)
        assert account.balance == 100.0
        assert account.owner == "Alice"

    def test_empty_owner_raises(self):
        with pytest.raises(ValidationError) as exc_info:
            BankAccount("", 100.0)
        assert exc_info.value.field == "owner"

    def test_negative_balance_raises(self):
        with pytest.raises(ValidationError) as exc_info:
            BankAccount("Alice", -50.0)
        assert exc_info.value.field == "balance"

    def test_successful_withdraw(self, bank_account):
        bank_account.withdraw(30.0)
        assert bank_account.balance == 70.0

    def test_insufficient_funds(self, bank_account):
        with pytest.raises(InsufficientFundsError) as exc_info:
            bank_account.withdraw(200.0)
        err = exc_info.value
        assert err.balance == 100.0
        assert err.amount == 200.0
        assert err.shortfall == 100.0

    def test_zero_withdrawal_raises(self, bank_account):
        with pytest.raises(ValidationError):
            bank_account.withdraw(0.0)

    def test_exception_hierarchy(self, bank_account):
        """InsufficientFundsError should be catchable as AppError."""
        with pytest.raises(AppError):
            bank_account.withdraw(999.0)

    @pytest.mark.parametrize("amount", [10.0, 50.0, 100.0])
    def test_valid_withdraw_amounts(self, bank_account, amount):
        initial = bank_account.balance
        bank_account.withdraw(amount)
        assert bank_account.balance == initial - amount


# =============================================================================
# Tests: file I/O with real temp files
# =============================================================================

class TestReadJsonFile:

    def test_reads_valid_json(self, json_file):
        path, expected = json_file
        result = read_json_file(path)
        assert result == expected

    def test_missing_file_raises_file_not_found(self, tmp_path):
        missing = tmp_path / "nonexistent.json"
        with pytest.raises(FileNotFoundError):
            read_json_file(missing)

    def test_invalid_json_raises_value_error(self, tmp_path):
        bad_json = tmp_path / "bad.json"
        bad_json.write_text("{ this is not valid json }")
        with pytest.raises(ValueError):
            read_json_file(bad_json)

    def test_writes_and_reads_json(self, tmp_path):
        """Integration: write a file, then read it back."""
        data = {"key": "value", "number": 42}
        path = tmp_path / "roundtrip.json"
        path.write_text(json.dumps(data))

        result = read_json_file(path)
        assert result == data


# =============================================================================
# Tests: Mocking file I/O
# =============================================================================

class TestMocking:

    def test_mock_open_for_json(self):
        """Test read_json_file with mocked file system."""
        fake_data = {"mocked": True, "value": 99}
        fake_json = json.dumps(fake_data)

        with patch("builtins.open", mock_open(read_data=fake_json)):
            result = read_json_file(Path("any_path.json"))

        assert result == fake_data

    def test_mock_open_is_called_with_correct_path(self):
        """Verify the correct path is passed to open()."""
        fake_data = {"key": "value"}
        target_path = Path("/specific/path/config.json")

        with patch("builtins.open", mock_open(read_data=json.dumps(fake_data))) as mock_f:
            read_json_file(target_path)

        mock_f.assert_called_once_with(target_path)

    def test_mock_file_not_found(self):
        """Simulate FileNotFoundError with mock."""
        with patch("builtins.open", side_effect=FileNotFoundError("mocked not found")):
            with pytest.raises(FileNotFoundError):
                read_json_file(Path("any.json"))

    def test_mock_json_decode_error(self):
        """Simulate invalid JSON file content."""
        with patch("builtins.open", mock_open(read_data="not valid json {")):
            with pytest.raises(ValueError):
                read_json_file(Path("bad.json"))


# =============================================================================
# Tests: finally and else behavior
# =============================================================================

class TestTryElseFinally:

    def test_else_runs_on_success(self):
        """else clause should run when no exception is raised."""
        log = []
        try:
            x = int("42")
        except ValueError:
            log.append("except")
        else:
            log.append("else")
        finally:
            log.append("finally")

        assert log == ["else", "finally"]

    def test_else_skipped_on_exception(self):
        """else clause should NOT run when exception is raised."""
        log = []
        try:
            x = int("not_a_number")
        except ValueError:
            log.append("except")
        else:
            log.append("else")   # Should NOT run
        finally:
            log.append("finally")

        assert log == ["except", "finally"]

    def test_finally_runs_even_on_return(self):
        """finally should run even when the function returns early."""
        log = []

        def func():
            try:
                return "early"
            finally:
                log.append("finally")

        result = func()
        assert result == "early"
        assert log == ["finally"]

    def test_exception_chaining(self):
        """raise X from Y should set __cause__."""
        original = ValueError("original")
        try:
            try:
                raise original
            except ValueError as e:
                raise RuntimeError("wrapped") from e
        except RuntimeError as e:
            assert e.__cause__ is original

    def test_exception_chaining_suppressed(self):
        """raise X from None should suppress __cause__."""
        try:
            try:
                raise ValueError("original")
            except ValueError:
                raise RuntimeError("new error") from None
        except RuntimeError as e:
            assert e.__cause__ is None
