from decimal import Decimal


class BankingException(Exception):
    """Base exception for all banking errors."""


class AccountNotFoundError(BankingException):
    """Raised when an account cannot be found."""

    def __init__(self, account_id: str) -> None:
        super().__init__(f"Account not found: {account_id}")
        self.account_id = account_id


class CustomerNotFoundError(BankingException):
    """Raised when a customer cannot be found."""

    def __init__(self, customer_id: str) -> None:
        super().__init__(f"Customer not found: {customer_id}")
        self.customer_id = customer_id


class InsufficientFundsError(BankingException):
    """Raised when a withdrawal exceeds the available balance."""

    def __init__(
        self, account_id: str, requested: Decimal, available: Decimal
    ) -> None:
        super().__init__(
            f"Insufficient funds in account {account_id}: "
            f"requested {requested:.2f}, available {available:.2f}"
        )
        self.account_id = account_id
        self.requested = requested
        self.available = available


class DuplicateEmailError(BankingException):
    """Raised when registering a customer with an already-used email."""

    def __init__(self, email: str) -> None:
        super().__init__(f"Email already registered: {email}")
        self.email = email
