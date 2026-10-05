"""
Factory Pattern — Python Examples

Two approaches:
1. Factory function (more Pythonic)
2. Factory class (for stateful/complex factories)
"""

from abc import ABC, abstractmethod
from dataclasses import dataclass
from decimal import Decimal
from typing import Protocol
import os


# ─── Target interface ─────────────────────────────────────────────────────────

@dataclass
class PaymentResult:
    success: bool
    transaction_id: str
    amount: Decimal
    processor: str
    error_message: str = ""


class PaymentProcessor(Protocol):
    """Interface that all payment processors must satisfy."""

    def process(self, amount: Decimal, description: str) -> PaymentResult:
        ...

    def refund(self, transaction_id: str, amount: Decimal) -> PaymentResult:
        ...


# ─── Concrete Processors ──────────────────────────────────────────────────────

class StripeProcessor:
    """Handles Stripe payments."""

    def __init__(self, api_key: str, webhook_secret: str = ""):
        self._api_key = api_key
        self._webhook_secret = webhook_secret

    def process(self, amount: Decimal, description: str) -> PaymentResult:
        # Real implementation would call the Stripe API
        import hashlib
        txn_id = f"stripe_{hashlib.sha256(str(amount).encode()).hexdigest()[:12]}"
        print(f"[Stripe] Processing ${amount} — {description}")
        return PaymentResult(True, txn_id, amount, "stripe")

    def refund(self, transaction_id: str, amount: Decimal) -> PaymentResult:
        refund_id = f"refund_{transaction_id}"
        print(f"[Stripe] Refunding ${amount} for {transaction_id}")
        return PaymentResult(True, refund_id, amount, "stripe")


class PayPalProcessor:
    """Handles PayPal payments."""

    def __init__(self, client_id: str, client_secret: str, sandbox: bool = False):
        self._client_id = client_id
        self._client_secret = client_secret
        self._sandbox = sandbox

    def process(self, amount: Decimal, description: str) -> PaymentResult:
        env = "sandbox" if self._sandbox else "live"
        txn_id = f"paypal_{env}_{int(amount * 100)}"
        print(f"[PayPal/{env}] Processing ${amount} — {description}")
        return PaymentResult(True, txn_id, amount, "paypal")

    def refund(self, transaction_id: str, amount: Decimal) -> PaymentResult:
        refund_id = f"refund_{transaction_id}"
        return PaymentResult(True, refund_id, amount, "paypal")


class MockProcessor:
    """For testing — always succeeds."""

    def process(self, amount: Decimal, description: str) -> PaymentResult:
        return PaymentResult(True, "mock_txn_001", amount, "mock")

    def refund(self, transaction_id: str, amount: Decimal) -> PaymentResult:
        return PaymentResult(True, f"refund_{transaction_id}", amount, "mock")


# ─── Factory Function (preferred Pythonic approach) ───────────────────────────

def create_payment_processor(processor_type: str | None = None) -> PaymentProcessor:
    """
    Factory function for payment processors.

    If processor_type is None, reads from the PAYMENT_PROCESSOR environment variable.
    Falls back to 'mock' if not configured (safe for development).

    Args:
        processor_type: 'stripe', 'paypal', or 'mock'. Reads from env if None.

    Returns:
        A configured PaymentProcessor.

    Raises:
        ValueError: If processor_type is unrecognized.
        EnvironmentError: If required environment variables are missing.
    """
    ptype = processor_type or os.getenv("PAYMENT_PROCESSOR", "mock")

    if ptype == "stripe":
        api_key = os.getenv("STRIPE_API_KEY")
        if not api_key:
            raise EnvironmentError("STRIPE_API_KEY environment variable is required")
        return StripeProcessor(
            api_key=api_key,
            webhook_secret=os.getenv("STRIPE_WEBHOOK_SECRET", ""),
        )

    elif ptype == "paypal":
        client_id = os.getenv("PAYPAL_CLIENT_ID")
        client_secret = os.getenv("PAYPAL_CLIENT_SECRET")
        if not client_id or not client_secret:
            raise EnvironmentError(
                "PAYPAL_CLIENT_ID and PAYPAL_CLIENT_SECRET are required"
            )
        sandbox = os.getenv("PAYPAL_SANDBOX", "true").lower() == "true"
        return PayPalProcessor(client_id, client_secret, sandbox=sandbox)

    elif ptype == "mock":
        return MockProcessor()

    else:
        raise ValueError(
            f"Unknown payment processor: '{ptype}'. "
            f"Valid options: stripe, paypal, mock"
        )


# ─── Demo ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    # Using mock processor (no env vars needed)
    processor = create_payment_processor("mock")

    result = processor.process(Decimal("49.99"), "Premium subscription")
    print(f"Success: {result.success}, ID: {result.transaction_id}")

    refund = processor.refund(result.transaction_id, Decimal("49.99"))
    print(f"Refund: {refund.success}, ID: {refund.transaction_id}")

    # Using Stripe (would need env vars in real use)
    # os.environ["STRIPE_API_KEY"] = "sk_test_..."
    # stripe_processor = create_payment_processor("stripe")
    # result = stripe_processor.process(Decimal("99.00"), "Annual plan")
