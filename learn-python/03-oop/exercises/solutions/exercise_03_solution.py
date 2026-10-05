"""
Solution to Exercise 03: Payment System with Protocols
"""

from typing import Protocol, runtime_checkable, List, Optional
from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum
import uuid


class OrderStatus(Enum):
    PENDING = "pending"
    PAID = "paid"
    FAILED = "failed"
    REFUNDED = "refunded"


@runtime_checkable
class PaymentMethod(Protocol):
    """Protocol defining the interface for all payment methods."""

    def charge(self, amount: float, description: str) -> bool:
        ...

    def refund(self, amount: float, transaction_id: str) -> bool:
        ...

    def get_name(self) -> str:
        ...

    def get_available_balance(self) -> float:
        ...


@dataclass
class CreditCard:
    """A credit card payment method."""

    card_number: str          # Last 4 digits
    card_holder: str
    credit_limit: float
    current_balance: float = 0.0
    _transactions: dict = field(default_factory=dict, init=False, repr=False)

    def __post_init__(self) -> None:
        if not self.card_number.isdigit() or len(self.card_number) != 4:
            raise ValueError(f"card_number must be exactly 4 digits: {self.card_number!r}")
        if self.credit_limit <= 0:
            raise ValueError(f"Credit limit must be positive: {self.credit_limit}")
        if self.current_balance < 0:
            raise ValueError(f"Balance cannot be negative: {self.current_balance}")

    def charge(self, amount: float, description: str = "") -> bool:
        if amount <= 0:
            return False
        if self.current_balance + amount > self.credit_limit:
            return False   # Over limit

        transaction_id = str(uuid.uuid4())[:8]
        self.current_balance += amount
        self._transactions[transaction_id] = amount
        return True

    def refund(self, amount: float, transaction_id: str) -> bool:
        if transaction_id not in self._transactions:
            return False
        self.current_balance = max(0.0, self.current_balance - amount)
        return True

    def get_name(self) -> str:
        return f"Credit Card (**** {self.card_number})"

    def get_available_balance(self) -> float:
        return self.credit_limit - self.current_balance

    def pay_off(self, amount: float) -> str:
        if amount <= 0:
            raise ValueError(f"Amount must be positive: {amount}")
        paid = min(amount, self.current_balance)
        self.current_balance -= paid
        return f"Paid off {paid:.2f}. Remaining balance: {self.current_balance:.2f}"


@dataclass
class BankTransfer:
    """Direct debit from bank account."""

    account_number: str     # Last 4 digits
    bank_name: str
    account_balance: float
    _transactions: dict = field(default_factory=dict, init=False, repr=False)

    def __post_init__(self) -> None:
        if self.account_balance < 0:
            raise ValueError(f"Account balance cannot be negative: {self.account_balance}")

    def charge(self, amount: float, description: str = "") -> bool:
        if amount <= 0:
            return False
        if amount > self.account_balance:
            return False   # Insufficient funds

        transaction_id = str(uuid.uuid4())[:8]
        self.account_balance -= amount
        self._transactions[transaction_id] = amount
        return True

    def refund(self, amount: float, transaction_id: str) -> bool:
        if transaction_id not in self._transactions:
            return False
        self.account_balance += amount
        return True

    def get_name(self) -> str:
        return f"Bank Transfer ({self.bank_name} ****{self.account_number})"

    def get_available_balance(self) -> float:
        return self.account_balance

    def deposit(self, amount: float) -> str:
        if amount <= 0:
            raise ValueError(f"Deposit amount must be positive: {amount}")
        self.account_balance += amount
        return f"Deposited {amount:.2f}. New balance: {self.account_balance:.2f}"


@dataclass
class DigitalWallet:
    """Digital wallet with transaction fees."""

    email: str
    wallet_balance: float
    provider: str = "PayPal"
    _transactions: dict = field(default_factory=dict, init=False, repr=False)

    TRANSACTION_FEE_RATE: float = 0.029

    def charge(self, amount: float, description: str = "") -> bool:
        if amount <= 0:
            return False

        total = amount * (1 + self.TRANSACTION_FEE_RATE)
        if total > self.wallet_balance:
            return False

        transaction_id = str(uuid.uuid4())[:8]
        self.wallet_balance -= total
        self._transactions[transaction_id] = amount
        return True

    def refund(self, amount: float, transaction_id: str) -> bool:
        if transaction_id not in self._transactions:
            return False
        # 1% processing fee on refund
        refund_amount = amount * 0.99
        self.wallet_balance += refund_amount
        return True

    def get_name(self) -> str:
        return f"{self.provider} ({self.email})"

    def get_available_balance(self) -> float:
        return self.wallet_balance

    def add_funds(self, amount: float) -> str:
        if amount <= 0:
            raise ValueError(f"Amount must be positive: {amount}")
        self.wallet_balance += amount
        return f"Added {amount:.2f} to {self.provider} wallet. Balance: {self.wallet_balance:.2f}"


@dataclass
class OrderItem:
    """A single item in an order."""
    name: str
    quantity: int
    unit_price: float

    @property
    def total_price(self) -> float:
        return self.quantity * self.unit_price

    def __str__(self) -> str:
        return f"{self.quantity}x {self.name} @ {self.unit_price:.2f} = {self.total_price:.2f}"


@dataclass
class Order:
    """A purchase order."""

    customer_name: str
    order_id: str = field(default_factory=lambda: str(uuid.uuid4())[:8].upper())
    items: List[OrderItem] = field(default_factory=list)
    status: OrderStatus = field(default=OrderStatus.PENDING)
    payment_method_name: Optional[str] = field(default=None, init=False)
    transaction_id: Optional[str] = field(default=None, init=False)
    created_at: datetime = field(default_factory=datetime.now)

    def add_item(self, item: OrderItem) -> None:
        if self.status != OrderStatus.PENDING:
            raise RuntimeError(f"Cannot add items to {self.status.value} order")
        self.items.append(item)

    @property
    def total(self) -> float:
        return sum(item.total_price for item in self.items)

    def pay(self, payment_method: PaymentMethod) -> bool:
        if self.status != OrderStatus.PENDING:
            raise RuntimeError(f"Cannot pay {self.status.value} order")
        if not self.items:
            raise RuntimeError("Cannot pay for empty order")

        success = payment_method.charge(self.total, f"Order {self.order_id}")

        if success:
            self.status = OrderStatus.PAID
            self.payment_method_name = payment_method.get_name()
        else:
            self.status = OrderStatus.FAILED

        return success

    def refund(self) -> bool:
        if self.status != OrderStatus.PAID:
            raise RuntimeError(f"Cannot refund {self.status.value} order")
        self.status = OrderStatus.REFUNDED
        return True

    def __str__(self) -> str:
        lines = [f"Order {self.order_id} for {self.customer_name} ({self.status.value})"]
        for item in self.items:
            lines.append(f"  {item}")
        lines.append(f"  Total: {self.total:.2f}")
        return "\n".join(lines)


class PaymentProcessor:
    """Processes payments for orders using any PaymentMethod."""

    def __init__(self, payment_method: PaymentMethod) -> None:
        if not isinstance(payment_method, PaymentMethod):
            raise TypeError(
                f"Expected PaymentMethod, got {type(payment_method).__name__}"
            )
        self._payment_method = payment_method
        self._processed_orders: List[Order] = []

    def process_order(self, order: Order) -> bool:
        result = order.pay(self._payment_method)
        self._processed_orders.append(order)
        return result

    def get_history(self) -> List[Order]:
        return list(self._processed_orders)

    def get_summary(self) -> str:
        paid = [o for o in self._processed_orders if o.status == OrderStatus.PAID]
        failed = [o for o in self._processed_orders if o.status == OrderStatus.FAILED]
        revenue = sum(o.total for o in paid)

        return (
            f"Payment Summary for {self._payment_method.get_name()}\n"
            f"  Processed: {len(self._processed_orders)} orders\n"
            f"  Successful: {len(paid)} | Failed: {len(failed)}\n"
            f"  Total revenue: {revenue:.2f}"
        )

    def __str__(self) -> str:
        return (
            f"PaymentProcessor("
            f"method={self._payment_method.get_name()}, "
            f"orders={len(self._processed_orders)})"
        )
