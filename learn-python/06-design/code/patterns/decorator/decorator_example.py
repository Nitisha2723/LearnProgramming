"""
Decorator Pattern — Python Examples

Two approaches:
1. Python @decorator syntax (functional)
2. GoF Decorator pattern (structural / class-based)
"""

import functools
from abc import ABC, abstractmethod
from decimal import Decimal


# ─── Part 1: Python @decorator syntax ────────────────────────────────────────

def log_calls(func):
    """Simple decorator that logs function calls."""
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        args_repr = [repr(a) for a in args]
        kwargs_repr = [f"{k}={v!r}" for k, v in kwargs.items()]
        signature = ", ".join(args_repr + kwargs_repr)
        print(f"[LOG] Calling {func.__name__}({signature})")
        result = func(*args, **kwargs)
        print(f"[LOG] {func.__name__} returned {result!r}")
        return result
    return wrapper


def validate_positive(func):
    """Decorator that ensures all numeric arguments are positive."""
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        for i, arg in enumerate(args):
            if isinstance(arg, (int, float, Decimal)) and arg <= 0:
                raise ValueError(
                    f"Argument {i} must be positive, got {arg} in {func.__name__}()"
                )
        return func(*args, **kwargs)
    return wrapper


def memoize(func):
    """Decorator that caches results — simplified version of functools.lru_cache."""
    cache: dict = {}

    @functools.wraps(func)
    def wrapper(*args):
        if args not in cache:
            cache[args] = func(*args)
        return cache[args]

    wrapper.cache = cache  # Expose cache for inspection
    wrapper.cache_info = lambda: f"{len(cache)} items cached"
    return wrapper


# Demonstrate stacking decorators
@log_calls
@validate_positive
@memoize
def calculate_compound_interest(principal: float, rate: float, years: int) -> float:
    """
    Calculate compound interest.

    Decorators applied bottom-up: memoize first, then validate_positive, then log_calls.
    """
    return principal * (1 + rate) ** years


# ─── Parametrized Decorator ───────────────────────────────────────────────────

def repeat(n: int):
    """
    Parametrized decorator: repeat a function n times.

    Usage: @repeat(3)
    """
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            result = None
            for _ in range(n):
                result = func(*args, **kwargs)
            return result
        return wrapper
    return decorator


@repeat(3)
def say_hello(name: str) -> str:
    print(f"Hello, {name}!")
    return f"Hello, {name}!"


# ─── Part 2: GoF Decorator Pattern (structural) ───────────────────────────────

class Beverage(ABC):
    """Component: the abstract base."""

    @abstractmethod
    def cost(self) -> Decimal:
        ...

    @abstractmethod
    def description(self) -> str:
        ...

    def __str__(self) -> str:
        return f"{self.description()} — ${self.cost():.2f}"


class Espresso(Beverage):
    def cost(self) -> Decimal:
        return Decimal("2.00")

    def description(self) -> str:
        return "Espresso"


class Latte(Beverage):
    def cost(self) -> Decimal:
        return Decimal("3.00")

    def description(self) -> str:
        return "Latte"


class BeverageDecorator(Beverage, ABC):
    """Abstract decorator."""

    def __init__(self, beverage: Beverage):
        self._beverage = beverage


class Milk(BeverageDecorator):
    def cost(self) -> Decimal:
        return self._beverage.cost() + Decimal("0.30")

    def description(self) -> str:
        return f"{self._beverage.description()}, Milk"


class Sugar(BeverageDecorator):
    def cost(self) -> Decimal:
        return self._beverage.cost() + Decimal("0.10")

    def description(self) -> str:
        return f"{self._beverage.description()}, Sugar"


class Vanilla(BeverageDecorator):
    def cost(self) -> Decimal:
        return self._beverage.cost() + Decimal("0.50")

    def description(self) -> str:
        return f"{self._beverage.description()}, Vanilla"


class WhippedCream(BeverageDecorator):
    def cost(self) -> Decimal:
        return self._beverage.cost() + Decimal("0.75")

    def description(self) -> str:
        return f"{self._beverage.description()}, Whipped Cream"


# ─── Demo ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    print("=== Python @decorator (functional) ===")
    result = calculate_compound_interest(1000.0, 0.05, 10)
    print(f"Result: ${result:.2f}")

    # Second call uses memoized result
    result = calculate_compound_interest(1000.0, 0.05, 10)
    print(f"Cache: {calculate_compound_interest.cache_info()}")

    print("\n=== Parametrized Decorator ===")
    say_hello("Alice")

    print("\n=== GoF Decorator Pattern ===")
    drink = Espresso()
    print(drink)

    drink = Milk(Sugar(Espresso()))
    print(drink)

    drink = WhippedCream(Vanilla(Milk(Latte())))
    print(drink)
