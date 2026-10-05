"""
Open/Closed Principle — Python Examples

Demonstrates OCP violation and correct application.
"""

from abc import ABC, abstractmethod
from decimal import Decimal
from dataclasses import dataclass
from typing import Protocol


# ─── VIOLATION ────────────────────────────────────────────────────────────────

def calculate_discount_violation(price: Decimal, customer_type: str) -> Decimal:
    """VIOLATION: Every new customer type requires editing this function."""
    if customer_type == "regular":
        return price
    elif customer_type == "vip":
        return price * Decimal("0.80")  # 20% off
    elif customer_type == "employee":
        return price * Decimal("0.50")  # 50% off
    # Adding "wholesale" customer requires MODIFYING this function
    else:
        raise ValueError(f"Unknown customer type: {customer_type}")


# ─── CORRECT: Using Protocol ──────────────────────────────────────────────────

@dataclass
class PriceResult:
    original_price: Decimal
    final_price: Decimal
    discount_applied: Decimal

    @property
    def discount_percentage(self) -> float:
        if self.original_price == 0:
            return 0.0
        return float((self.discount_applied / self.original_price) * 100)


class DiscountStrategy(Protocol):
    """Extension point: add new discount strategies without modifying existing code."""

    def apply(self, price: Decimal) -> Decimal:
        """Return the discounted price."""
        ...

    def description(self) -> str:
        """Human-readable description of this discount."""
        ...


class RegularCustomerDiscount:
    """No discount for regular customers."""

    def apply(self, price: Decimal) -> Decimal:
        return price

    def description(self) -> str:
        return "Regular price (no discount)"


class VipCustomerDiscount:
    """VIP customers get 20% off."""

    VIP_RATE = Decimal("0.20")

    def apply(self, price: Decimal) -> Decimal:
        return price * (1 - self.VIP_RATE)

    def description(self) -> str:
        return f"VIP discount ({int(self.VIP_RATE * 100)}% off)"


class EmployeeDiscount:
    """Employees get 50% off."""

    EMPLOYEE_RATE = Decimal("0.50")

    def apply(self, price: Decimal) -> Decimal:
        return price * (1 - self.EMPLOYEE_RATE)

    def description(self) -> str:
        return f"Employee discount ({int(self.EMPLOYEE_RATE * 100)}% off)"


# Adding a NEW strategy WITHOUT changing any existing code:

class WholesaleDiscount:
    """Wholesale: variable discount based on quantity."""

    def __init__(self, quantity: int):
        self._quantity = quantity

    def apply(self, price: Decimal) -> Decimal:
        if self._quantity >= 100:
            return price * Decimal("0.60")  # 40% off
        elif self._quantity >= 50:
            return price * Decimal("0.75")  # 25% off
        else:
            return price * Decimal("0.90")  # 10% off

    def description(self) -> str:
        return f"Wholesale discount (qty: {self._quantity})"


class PricingService:
    """This code never needs to change. New discount types are just new classes."""

    def calculate(self, price: Decimal, strategy: DiscountStrategy) -> PriceResult:
        final_price = strategy.apply(price)
        return PriceResult(
            original_price=price,
            final_price=final_price,
            discount_applied=price - final_price,
        )


if __name__ == "__main__":
    service = PricingService()
    base_price = Decimal("100.00")

    strategies: list[DiscountStrategy] = [
        RegularCustomerDiscount(),
        VipCustomerDiscount(),
        EmployeeDiscount(),
        WholesaleDiscount(quantity=75),
    ]

    for strategy in strategies:
        result = service.calculate(base_price, strategy)
        print(
            f"{strategy.description()}: "
            f"${base_price} → ${result.final_price} "
            f"(saved {result.discount_percentage:.0f}%)"
        )
