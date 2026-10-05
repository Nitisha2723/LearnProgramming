"""
Exercise 03: Payment System with Protocols

TOPIC: Protocol, ABC, multiple classes, complex logic, @dataclass

SCENARIO:
You're building a payment processing system that supports multiple payment methods.
The system should work with any payment method via a Protocol — new payment methods
can be added without changing the existing code.

WHAT TO IMPLEMENT:
1. PaymentMethod Protocol — defines the interface for all payment methods
2. CreditCard — implements PaymentMethod
3. BankTransfer — implements PaymentMethod
4. DigitalWallet — implements PaymentMethod
5. PaymentProcessor — processes payments using any PaymentMethod
6. Order — represents a purchase order

LEARNING GOALS:
- Writing a Protocol and ensuring classes satisfy it
- Using @dataclass for data classes
- Composition: PaymentProcessor composes with PaymentMethod
- isinstance() with Protocol (@runtime_checkable)
- Working with the typing module

RUN TO TEST:
    python exercise_03_payment_system.py
"""

from typing import Protocol, runtime_checkable, List, Optional
from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum
import uuid


class OrderStatus(Enum):
    """Status of an order."""
    PENDING = "pending"
    PAID = "paid"
    FAILED = "failed"
    REFUNDED = "refunded"


# ============================================================
# TODO 1: Define the PaymentMethod Protocol
# ============================================================

@runtime_checkable
class PaymentMethod(Protocol):
    """Protocol defining the interface for all payment methods.

    Any class with these methods automatically satisfies the Protocol.
    No inheritance needed.

    Methods that must be implemented:
    - charge(amount: float, description: str) -> bool
      Attempt to charge the given amount. Return True if successful, False otherwise.
    - refund(amount: float, transaction_id: str) -> bool
      Refund the given amount for a transaction. Return True if successful.
    - get_name() -> str
      Return the name/identifier of this payment method.
    - get_available_balance() -> float
      Return available balance (or credit limit for credit cards).
    """

    # TODO: Define the protocol methods with ... as body
    # Use proper type hints!
    pass


# ============================================================
# TODO 2: Implement CreditCard
# ============================================================

@dataclass
class CreditCard:
    """A credit card payment method.

    Attributes:
        card_number: Last 4 digits (for display, never store full number!)
        card_holder: Name on the card
        credit_limit: Maximum credit available
        current_balance: Current balance owed (starts at 0)
        _transactions: Internal list of transaction IDs and amounts

    Methods:
        charge(amount, description) -> bool:
            Charge if (current_balance + amount) <= credit_limit.
            Return False if over limit.

        refund(amount, transaction_id) -> bool:
            Reduce current_balance by amount.
            Return False if transaction_id not found.

        get_name() -> str:
            Return "Credit Card (**** 1234)"

        get_available_balance() -> float:
            Return credit_limit - current_balance

    HINT: Use @dataclass but you'll need to manage _transactions manually
          in __post_init__. Use field(init=False, repr=False, default_factory=list)
          for the _transactions field, or initialize it in __post_init__.
    """

    card_number: str          # Last 4 digits only
    card_holder: str
    credit_limit: float
    current_balance: float = 0.0

    # TODO: Add _transactions field — a dict mapping transaction_id to amount
    # It's init=False (not passed to __init__) and repr=False (hidden from repr)

    def __post_init__(self) -> None:
        """Validate after initialization."""
        # TODO: Validate card_number is 4 digits
        # TODO: Validate credit_limit > 0
        # TODO: Validate current_balance >= 0
        pass

    def charge(self, amount: float, description: str = "") -> bool:
        """Charge amount to the credit card. Returns True if successful."""
        # TODO: Check amount > 0
        # TODO: Check (current_balance + amount) <= credit_limit
        # TODO: Generate transaction_id = str(uuid.uuid4())[:8]
        # TODO: Update current_balance
        # TODO: Record in _transactions
        # TODO: Return True if successful, False otherwise
        pass

    def refund(self, amount: float, transaction_id: str) -> bool:
        """Refund a transaction. Returns True if successful."""
        # TODO: Check transaction_id exists in _transactions
        # TODO: Reduce current_balance
        # TODO: Return True if successful
        pass

    def get_name(self) -> str:
        """Return 'Credit Card (**** 1234)'"""
        # TODO
        pass

    def get_available_balance(self) -> float:
        """Return credit_limit - current_balance"""
        # TODO
        pass

    def pay_off(self, amount: float) -> str:
        """Pay off part of the balance."""
        # TODO: Reduce current_balance (not below 0)
        pass


# ============================================================
# TODO 3: Implement BankTransfer
# ============================================================

@dataclass
class BankTransfer:
    """A bank transfer payment method (direct debit from bank account).

    Attributes:
        account_number: Bank account number (last 4 digits for display)
        bank_name: Name of the bank
        account_balance: Current account balance (real money)

    Methods:
        charge(amount, description) -> bool:
            Deduct from account_balance if sufficient funds.

        refund(amount, transaction_id) -> bool:
            Add back to account_balance.

        get_name() -> str:
            "Bank Transfer (MyBank ****5678)"

        get_available_balance() -> float:
            Return account_balance
    """

    account_number: str    # Last 4 digits
    bank_name: str
    account_balance: float

    def __post_init__(self) -> None:
        # TODO: Validate account_balance >= 0
        pass

    def charge(self, amount: float, description: str = "") -> bool:
        # TODO: Implement
        pass

    def refund(self, amount: float, transaction_id: str) -> bool:
        # TODO: Implement
        pass

    def get_name(self) -> str:
        # TODO
        pass

    def get_available_balance(self) -> float:
        # TODO
        pass

    def deposit(self, amount: float) -> str:
        """Add money to the account."""
        # TODO
        pass


# ============================================================
# TODO 4: Implement DigitalWallet
# ============================================================

@dataclass
class DigitalWallet:
    """A digital wallet (e.g., PayPal, Apple Pay).

    Attributes:
        email: Associated email address
        wallet_balance: Current wallet balance
        provider: Wallet provider name (e.g., "PayPal", "Stripe")

    Note: Digital wallets charge a 2.9% transaction fee.
    The charge() method should deduct amount + fee from wallet_balance.
    """

    email: str
    wallet_balance: float
    provider: str = "PayPal"

    TRANSACTION_FEE_RATE: float = 0.029   # 2.9%

    def charge(self, amount: float, description: str = "") -> bool:
        """Charge including transaction fee."""
        # TODO: Calculate total = amount * (1 + TRANSACTION_FEE_RATE)
        # TODO: Check sufficient balance
        # TODO: Deduct total from wallet_balance
        pass

    def refund(self, amount: float, transaction_id: str) -> bool:
        """Refund (add back to wallet, minus 1% processing fee)."""
        # TODO: Add amount * 0.99 back to wallet_balance
        pass

    def get_name(self) -> str:
        """Return 'PayPal (user@email.com)'"""
        # TODO
        pass

    def get_available_balance(self) -> float:
        # TODO
        pass

    def add_funds(self, amount: float) -> str:
        """Add funds to the wallet."""
        # TODO
        pass


# ============================================================
# TODO 5: Implement Order using @dataclass
# ============================================================

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
    """A purchase order.

    Attributes:
        order_id: Auto-generated unique ID
        customer_name: Customer name
        items: List of OrderItem
        status: OrderStatus (starts as PENDING)
        payment_method_name: Name of payment method used (filled on payment)
        transaction_id: Payment transaction ID (filled on payment)

    Methods:
        add_item(item): Add an OrderItem
        total(): Return total price of all items
        pay(payment_method): Process payment using any PaymentMethod
        refund(): Process a refund
    """

    customer_name: str
    order_id: str = field(default_factory=lambda: str(uuid.uuid4())[:8].upper())
    items: List[OrderItem] = field(default_factory=list)
    status: OrderStatus = field(default=OrderStatus.PENDING)
    payment_method_name: Optional[str] = field(default=None, init=False)
    transaction_id: Optional[str] = field(default=None, init=False)
    created_at: datetime = field(default_factory=datetime.now)

    def add_item(self, item: OrderItem) -> None:
        """Add an item to the order.

        Raises RuntimeError if order is already paid.
        """
        # TODO: Check status is PENDING, then append item
        pass

    @property
    def total(self) -> float:
        """Total price of all items."""
        # TODO: Sum of item.total_price for all items
        pass

    def pay(self, payment_method: PaymentMethod) -> bool:
        """Process payment for this order.

        Args:
            payment_method: Any object satisfying the PaymentMethod Protocol

        Returns:
            True if payment successful, False otherwise

        Side effects:
            - Sets status to PAID or FAILED
            - Records payment_method_name
        """
        # TODO: Check status is PENDING
        # TODO: Call payment_method.charge(self.total, f"Order {self.order_id}")
        # TODO: Update status based on result
        # TODO: Record payment_method_name = payment_method.get_name()
        pass

    def refund(self) -> bool:
        """Refund this order.

        Can only refund PAID orders.
        Returns True if refund successful.
        """
        # TODO: Check status is PAID
        # TODO: Need to refund somehow — but we don't have a reference to the payment method!
        # HINT: This is a design limitation. For this exercise, just set status to REFUNDED
        #       and return True. In a real system, you'd store a reference to the payment method.
        pass

    def __str__(self) -> str:
        lines = [f"Order {self.order_id} for {self.customer_name} ({self.status.value})"]
        for item in self.items:
            lines.append(f"  {item}")
        lines.append(f"  Total: {self.total:.2f}")
        return "\n".join(lines)


# ============================================================
# TODO 6: Implement PaymentProcessor
# ============================================================

class PaymentProcessor:
    """Processes payments for orders.

    The processor accepts ANY PaymentMethod — this is where Protocol shines.
    You don't need to change this class when adding new payment methods.

    Attributes:
        _payment_method: The payment method to use
        _processed_orders: History of processed orders

    Methods:
        process_order(order) -> bool: Process an order
        get_history() -> List[Order]: Get all processed orders
    """

    def __init__(self, payment_method: PaymentMethod) -> None:
        """Initialize with any PaymentMethod.

        Raises TypeError if payment_method doesn't satisfy the PaymentMethod Protocol.
        """
        # TODO: Check isinstance(payment_method, PaymentMethod) → TypeError if not
        # TODO: Store payment_method and initialize _processed_orders = []
        pass

    def process_order(self, order: Order) -> bool:
        """Process payment for an order.

        Returns True if payment successful.
        """
        # TODO: Call order.pay(self._payment_method)
        # TODO: Add order to _processed_orders regardless of outcome
        # TODO: Return the result
        pass

    def get_history(self) -> List[Order]:
        """Return list of all processed orders."""
        # TODO
        pass

    def get_summary(self) -> str:
        """Return a summary of processing statistics."""
        # TODO: Count paid vs failed orders
        # TODO: Sum total revenue
        # TODO: Return formatted string
        pass

    def __str__(self) -> str:
        method = self._payment_method  # type: ignore
        return f"PaymentProcessor(method={method.get_name()}, orders={len(self._processed_orders)})"  # type: ignore


# ============================================================
# Tests
# ============================================================

def run_tests() -> None:
    print("Testing Payment System...")

    # Test Protocol
    card = CreditCard("1234", "Alice", 5000.0)
    bank = BankTransfer("5678", "Deutsche Bank", 10000.0)
    wallet = DigitalWallet("alice@example.com", 500.0, "PayPal")

    assert isinstance(card, PaymentMethod), "CreditCard should satisfy PaymentMethod"
    assert isinstance(bank, PaymentMethod), "BankTransfer should satisfy PaymentMethod"
    assert isinstance(wallet, PaymentMethod), "DigitalWallet should satisfy PaymentMethod"
    print("  ✓ All payment methods satisfy Protocol")

    # Test CreditCard
    initial_balance = card.get_available_balance()
    success = card.charge(100.0, "Test charge")
    assert success is True
    assert card.get_available_balance() == initial_balance - 100.0
    assert card.current_balance == 100.0
    print("  ✓ CreditCard charge")

    # Test charge over limit
    big_charge = card.charge(10000.0, "Too big")
    assert big_charge is False   # Over credit limit
    print("  ✓ CreditCard over-limit rejected")

    # Test BankTransfer
    initial = bank.get_available_balance()
    bank.charge(500.0, "Test")
    assert bank.get_available_balance() == initial - 500.0
    print("  ✓ BankTransfer charge")

    # Test DigitalWallet (includes fee)
    wallet_initial = wallet.get_available_balance()
    wallet.charge(100.0, "Test")
    # Should deduct 100 + 2.9% fee = 102.90
    expected = wallet_initial - 100.0 * (1 + DigitalWallet.TRANSACTION_FEE_RATE)
    assert abs(wallet.get_available_balance() - expected) < 0.01
    print("  ✓ DigitalWallet charge with fee")

    # Test Order and payment
    fresh_card = CreditCard("9999", "Bob", 1000.0)
    order = Order("Bob Smith")
    order.add_item(OrderItem("Python Book", 2, 39.99))
    order.add_item(OrderItem("Keyboard", 1, 89.99))
    expected_total = 2 * 39.99 + 89.99
    assert abs(order.total - expected_total) < 0.01
    print("  ✓ Order total calculation")

    assert order.status == OrderStatus.PENDING
    paid = order.pay(fresh_card)
    assert paid is True
    assert order.status == OrderStatus.PAID
    print("  ✓ Order payment")

    # Cannot pay twice
    try:
        order.pay(fresh_card)
        print("  ✗ Should raise error paying already-paid order")
    except RuntimeError:
        print("  ✓ Cannot pay already-paid order")

    # Test PaymentProcessor
    processor = PaymentProcessor(CreditCard("1111", "Carol", 2000.0))
    o1 = Order("Alice")
    o1.add_item(OrderItem("Widget", 3, 10.0))
    result = processor.process_order(o1)
    assert result is True
    assert len(processor.get_history()) == 1
    print("  ✓ PaymentProcessor processes order")

    # PaymentProcessor rejects non-PaymentMethod
    try:
        bad_processor = PaymentProcessor("not a payment method")   # type: ignore
        print("  ✗ Should raise TypeError")
    except TypeError:
        print("  ✓ PaymentProcessor rejects invalid payment method")

    print("\n✓ All tests passed!")


if __name__ == "__main__":
    run_tests()
