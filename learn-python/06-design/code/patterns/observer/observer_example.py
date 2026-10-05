"""
Observer Pattern — Python Examples

Stock price ticker with multiple observer types.
"""

from dataclasses import dataclass, field
from decimal import Decimal
from typing import Callable
from abc import ABC, abstractmethod


# ─── Event ────────────────────────────────────────────────────────────────────

@dataclass(frozen=True)
class PriceChangeEvent:
    """Immutable event data — observers cannot mutate it."""
    symbol: str
    previous_price: Decimal
    current_price: Decimal

    @property
    def change(self) -> Decimal:
        return self.current_price - self.previous_price

    @property
    def change_percent(self) -> float:
        if self.previous_price == 0:
            return 0.0
        return float((self.change / self.previous_price) * 100)

    @property
    def is_increase(self) -> bool:
        return self.current_price > self.previous_price


# ─── Subject ──────────────────────────────────────────────────────────────────

# Observer can be any callable
PriceObserver = Callable[[PriceChangeEvent], None]


class StockTicker:
    """Subject: notifies observers of price changes."""

    def __init__(self, symbol: str, initial_price: Decimal):
        self.symbol = symbol
        self._price = initial_price
        self._observers: list[PriceObserver] = []

    def subscribe(self, observer: PriceObserver) -> None:
        if observer not in self._observers:
            self._observers.append(observer)

    def unsubscribe(self, observer: PriceObserver) -> None:
        try:
            self._observers.remove(observer)
        except ValueError:
            pass  # Observer was not subscribed

    @property
    def price(self) -> Decimal:
        return self._price

    @price.setter
    def price(self, new_price: Decimal) -> None:
        if new_price == self._price:
            return
        event = PriceChangeEvent(self.symbol, self._price, new_price)
        self._price = new_price
        self._notify(event)

    def _notify(self, event: PriceChangeEvent) -> None:
        for observer in list(self._observers):  # list() prevents mutation during iteration
            observer(event)


# ─── Concrete Observers ───────────────────────────────────────────────────────

def console_logger(event: PriceChangeEvent) -> None:
    """Simple function observer: logs all price changes."""
    arrow = "▲" if event.is_increase else "▼"
    print(
        f"[LOG] {event.symbol} {arrow} "
        f"${event.previous_price:.2f} → ${event.current_price:.2f} "
        f"({event.change_percent:+.2f}%)"
    )


class AlertSystem:
    """Class-based observer: triggers alerts for significant moves."""

    def __init__(self, threshold_percent: float = 5.0):
        self._threshold = threshold_percent
        self._alerts_sent: list[PriceChangeEvent] = []

    def __call__(self, event: PriceChangeEvent) -> None:
        """Makes this class callable — satisfies the PriceObserver type."""
        if abs(event.change_percent) >= self._threshold:
            self._send_alert(event)

    def _send_alert(self, event: PriceChangeEvent) -> None:
        direction = "surged" if event.is_increase else "dropped"
        print(
            f"[ALERT] {event.symbol} {direction} {abs(event.change_percent):.1f}%! "
            f"Current price: ${event.current_price:.2f}"
        )
        self._alerts_sent.append(event)

    @property
    def alert_count(self) -> int:
        return len(self._alerts_sent)


class PortfolioTracker:
    """Tracks profit/loss for a holding."""

    def __init__(self, symbol: str, shares: int, cost_per_share: Decimal):
        self.symbol = symbol
        self.shares = shares
        self.cost_basis = cost_per_share * shares

    def __call__(self, event: PriceChangeEvent) -> None:
        if event.symbol != self.symbol:
            return
        current_value = event.current_price * self.shares
        profit_loss = current_value - self.cost_basis
        pct = float((profit_loss / self.cost_basis) * 100)
        status = "PROFIT" if profit_loss >= 0 else "LOSS"
        print(
            f"[PORTFOLIO] {self.symbol}: {status} ${profit_loss:+.2f} "
            f"({pct:+.1f}%) on {self.shares} shares"
        )


# ─── Demo ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    # Create ticker
    apple = StockTicker("AAPL", Decimal("175.00"))

    # Create observers
    alert = AlertSystem(threshold_percent=3.0)
    portfolio = PortfolioTracker("AAPL", shares=100, cost_per_share=Decimal("170.00"))

    # Subscribe
    apple.subscribe(console_logger)
    apple.subscribe(alert)
    apple.subscribe(portfolio)

    print("=== Price Updates ===")
    apple.price = Decimal("178.50")   # Small increase
    apple.price = Decimal("165.00")   # Significant drop — triggers alert
    apple.price = Decimal("165.00")   # No change — no notification
    apple.price = Decimal("185.00")   # Big jump

    print(f"\nTotal alerts sent: {alert.alert_count}")

    # Unsubscribe the portfolio tracker
    print("\n=== After Unsubscribing Portfolio ===")
    apple.unsubscribe(portfolio)
    apple.price = Decimal("190.00")   # Only logger and alert will fire
