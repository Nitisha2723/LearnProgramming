# Behavioral Design Patterns in Python

Behavioral patterns are about how objects communicate and how responsibilities are distributed.

**Python-specific insight:** Python's first-class functions make many behavioral patterns far simpler than their Java equivalents. A Strategy pattern in Java requires an interface hierarchy. In Python, a strategy can be just a function you pass around.

---

## 1. Observer Pattern

**Intent:** Define a one-to-many dependency so that when one object changes state, all dependents are notified automatically.

Real uses: event systems, model-view synchronization, stock tickers, pub/sub.

```python
# observer.py

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from decimal import Decimal
from typing import Callable


# ─── Protocol-based approach ──────────────────────────────────────────────────

@dataclass
class StockEvent:
    symbol: str
    old_price: Decimal
    new_price: Decimal

    @property
    def change_percent(self) -> float:
        if self.old_price == 0:
            return 0.0
        change = (self.new_price - self.old_price) / self.old_price
        return float(change * 100)


# Observer type: any callable that accepts a StockEvent
StockObserver = Callable[[StockEvent], None]


class StockTicker:
    """Subject — maintains the list of observers and notifies them."""

    def __init__(self, symbol: str, initial_price: Decimal):
        self.symbol = symbol
        self._price = initial_price
        self._observers: list[StockObserver] = []

    def subscribe(self, observer: StockObserver) -> None:
        """Register an observer. Can be a function or any callable."""
        self._observers.append(observer)

    def unsubscribe(self, observer: StockObserver) -> None:
        """Deregister an observer."""
        self._observers.remove(observer)

    @property
    def price(self) -> Decimal:
        return self._price

    @price.setter
    def price(self, new_price: Decimal) -> None:
        if new_price != self._price:
            event = StockEvent(self.symbol, self._price, new_price)
            self._price = new_price
            self._notify_observers(event)

    def _notify_observers(self, event: StockEvent) -> None:
        for observer in self._observers:
            observer(event)


# ─── Observers — just functions! ──────────────────────────────────────────────

def log_observer(event: StockEvent) -> None:
    """Log all price changes."""
    direction = "↑" if event.new_price > event.old_price else "↓"
    print(f"[LOG] {event.symbol} {direction} ${event.old_price} → ${event.new_price} ({event.change_percent:+.2f}%)")


def alert_observer(event: StockEvent) -> None:
    """Alert on significant price changes."""
    if abs(event.change_percent) >= 5.0:
        print(f"[ALERT] {event.symbol} moved {event.change_percent:+.2f}%! Watch out!")


# ─── Class-based observer for stateful observers ──────────────────────────────

class PortfolioTracker:
    """Tracks profit/loss across multiple holdings."""

    def __init__(self, symbol: str, shares: int, purchase_price: Decimal):
        self.symbol = symbol
        self.shares = shares
        self.purchase_price = purchase_price

    def __call__(self, event: StockEvent) -> None:
        """Makes the class callable — it IS an observer."""
        if event.symbol != self.symbol:
            return
        current_value = event.new_price * self.shares
        cost_basis = self.purchase_price * self.shares
        pl = current_value - cost_basis
        print(f"[PORTFOLIO] {self.symbol}: P&L = ${pl:+.2f}")


# Usage:
apple = StockTicker("AAPL", Decimal("150.00"))
portfolio = PortfolioTracker("AAPL", shares=100, purchase_price=Decimal("150.00"))

apple.subscribe(log_observer)
apple.subscribe(alert_observer)
apple.subscribe(portfolio)

apple.price = Decimal("155.00")  # Small increase
apple.price = Decimal("140.00")  # Big drop — triggers alert
```

---

## 2. Strategy Pattern

**Intent:** Define a family of algorithms, encapsulate each one, and make them interchangeable.

**Python insight:** In Python, strategies are often just functions. You do not need a class hierarchy.

```python
# strategy.py

from typing import Protocol, Callable
import functools


# ─── Function-based Strategy (most Pythonic) ─────────────────────────────────

# A strategy is just a function type
SortStrategy = Callable[[list[int]], list[int]]


def bubble_sort(items: list[int]) -> list[int]:
    """O(n²) — educational only."""
    items = list(items)  # Don't mutate input
    n = len(items)
    for i in range(n):
        for j in range(n - i - 1):
            if items[j] > items[j + 1]:
                items[j], items[j + 1] = items[j + 1], items[j]
    return items


def merge_sort(items: list[int]) -> list[int]:
    """O(n log n) — good general-purpose sort."""
    if len(items) <= 1:
        return list(items)
    mid = len(items) // 2
    left = merge_sort(items[:mid])
    right = merge_sort(items[mid:])
    return _merge(left, right)


def _merge(left: list[int], right: list[int]) -> list[int]:
    result = []
    i = j = 0
    while i < len(left) and j < len(right):
        if left[i] <= right[j]:
            result.append(left[i])
            i += 1
        else:
            result.append(right[j])
            j += 1
    result.extend(left[i:])
    result.extend(right[j:])
    return result


def builtin_sort(items: list[int]) -> list[int]:
    """Python's built-in Timsort — fastest in practice."""
    return sorted(items)


class Sorter:
    """Context that uses a sort strategy."""

    def __init__(self, strategy: SortStrategy = builtin_sort):
        self._strategy = strategy

    def set_strategy(self, strategy: SortStrategy) -> None:
        self._strategy = strategy

    def sort(self, items: list[int]) -> list[int]:
        return self._strategy(items)


# Usage:
data = [5, 3, 8, 1, 9, 2, 7, 4, 6]

sorter = Sorter(bubble_sort)
print(sorter.sort(data))  # [1, 2, 3, 4, 5, 6, 7, 8, 9]

sorter.set_strategy(builtin_sort)
print(sorter.sort(data))  # [1, 2, 3, 4, 5, 6, 7, 8, 9]

# Even simpler — just pass the function directly:
result = builtin_sort(data)


# ─── Protocol-based Strategy (for type safety with complex strategies) ────────

class TextFormatter(Protocol):
    def format(self, text: str) -> str:
        ...


class HtmlFormatter:
    def format(self, text: str) -> str:
        return f"<p>{text}</p>"


class MarkdownFormatter:
    def format(self, text: str) -> str:
        return f"{text}\n{'=' * len(text)}"


class PlainFormatter:
    def format(self, text: str) -> str:
        return text


class DocumentWriter:
    def __init__(self, formatter: TextFormatter):
        self._formatter = formatter

    def write(self, text: str) -> str:
        return self._formatter.format(text)


doc = DocumentWriter(HtmlFormatter())
print(doc.write("Hello World"))  # <p>Hello World</p>
```

---

## 3. Command Pattern

**Intent:** Encapsulate a request as an object, allowing parameterization and undo/redo.

```python
# command.py

from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Callable


# ─── Function-based Command (simple cases) ───────────────────────────────────

class CommandHistory:
    """Maintains a history of executed commands for undo support."""

    def __init__(self):
        self._history: list[Callable] = []

    def execute(self, command: Callable, undo: Callable) -> None:
        """Execute a command and remember how to undo it."""
        command()
        self._history.append(undo)

    def undo(self) -> bool:
        """Undo the most recent command."""
        if self._history:
            undo_fn = self._history.pop()
            undo_fn()
            return True
        return False


# ─── Class-based Command (complex cases with serialization needs) ─────────────

class Command(ABC):
    @abstractmethod
    def execute(self) -> None:
        ...

    @abstractmethod
    def undo(self) -> None:
        ...


@dataclass
class TextDocument:
    content: str = ""


class InsertTextCommand(Command):
    def __init__(self, document: TextDocument, position: int, text: str):
        self._doc = document
        self._position = position
        self._text = text

    def execute(self) -> None:
        self._doc.content = (
            self._doc.content[:self._position]
            + self._text
            + self._doc.content[self._position:]
        )

    def undo(self) -> None:
        self._doc.content = (
            self._doc.content[:self._position]
            + self._doc.content[self._position + len(self._text):]
        )


class DeleteTextCommand(Command):
    def __init__(self, document: TextDocument, position: int, length: int):
        self._doc = document
        self._position = position
        self._length = length
        self._deleted_text = ""

    def execute(self) -> None:
        self._deleted_text = self._doc.content[self._position:self._position + self._length]
        self._doc.content = (
            self._doc.content[:self._position]
            + self._doc.content[self._position + self._length:]
        )

    def undo(self) -> None:
        self._doc.content = (
            self._doc.content[:self._position]
            + self._deleted_text
            + self._doc.content[self._position:]
        )


class TextEditor:
    """Invoker — uses commands and maintains history."""

    def __init__(self, document: TextDocument):
        self._doc = document
        self._history: list[Command] = []
        self._redo_stack: list[Command] = []

    def execute(self, command: Command) -> None:
        command.execute()
        self._history.append(command)
        self._redo_stack.clear()

    def undo(self) -> bool:
        if self._history:
            command = self._history.pop()
            command.undo()
            self._redo_stack.append(command)
            return True
        return False

    def redo(self) -> bool:
        if self._redo_stack:
            command = self._redo_stack.pop()
            command.execute()
            self._history.append(command)
            return True
        return False


# Usage:
doc = TextDocument()
editor = TextEditor(doc)

editor.execute(InsertTextCommand(doc, 0, "Hello"))
print(doc.content)  # "Hello"

editor.execute(InsertTextCommand(doc, 5, " World"))
print(doc.content)  # "Hello World"

editor.execute(DeleteTextCommand(doc, 5, 6))
print(doc.content)  # "Hello"

editor.undo()
print(doc.content)  # "Hello World"

editor.undo()
print(doc.content)  # "Hello"

editor.redo()
print(doc.content)  # "Hello World"
```

---

## 4. Template Method Pattern

**Intent:** Define the skeleton of an algorithm in a base class, deferring some steps to subclasses.

```python
# template_method.py
from abc import ABC, abstractmethod


class DataMiner(ABC):
    """Abstract class defining the data mining algorithm template."""

    def mine(self, path: str) -> dict:
        """Template method — defines the steps, some are abstract."""
        raw_data = self.extract(path)
        parsed_data = self.parse(raw_data)
        analysis = self.analyze(parsed_data)
        report = self.format_report(analysis)
        return report

    @abstractmethod
    def extract(self, path: str) -> str:
        """Subclasses implement this for their file format."""
        ...

    @abstractmethod
    def parse(self, raw_data: str) -> list[dict]:
        """Subclasses implement this for their data format."""
        ...

    def analyze(self, data: list[dict]) -> dict:
        """Default analysis — subclasses may override."""
        return {
            "count": len(data),
            "first": data[0] if data else None,
            "last": data[-1] if data else None,
        }

    def format_report(self, analysis: dict) -> dict:
        """Default report format — subclasses may override."""
        return {**analysis, "status": "complete"}


class CsvDataMiner(DataMiner):
    def extract(self, path: str) -> str:
        print(f"[CSV] Reading {path}")
        return "name,age\nAlice,30\nBob,25"

    def parse(self, raw_data: str) -> list[dict]:
        lines = raw_data.strip().split("\n")
        headers = lines[0].split(",")
        return [
            dict(zip(headers, line.split(",")))
            for line in lines[1:]
        ]


class JsonDataMiner(DataMiner):
    def extract(self, path: str) -> str:
        print(f"[JSON] Reading {path}")
        return '[{"name": "Charlie", "age": 35}, {"name": "Diana", "age": 28}]'

    def parse(self, raw_data: str) -> list[dict]:
        import json
        return json.loads(raw_data)

    def format_report(self, analysis: dict) -> dict:
        """JSON miner adds extra metadata."""
        return {**super().format_report(analysis), "format": "json"}


# Usage:
csv_miner = CsvDataMiner()
json_miner = JsonDataMiner()

print(csv_miner.mine("data.csv"))
print(json_miner.mine("data.json"))
```

---

## 5. Chain of Responsibility Pattern

**Intent:** Pass a request along a chain of handlers, each handling it or passing it on.

```python
# chain_of_responsibility.py
from abc import ABC, abstractmethod
from dataclasses import dataclass


@dataclass
class Request:
    content: str
    sender: str
    amount: float = 0.0


class Handler(ABC):
    """Base handler with a reference to the next handler."""

    def __init__(self):
        self._next: Handler | None = None

    def set_next(self, handler: "Handler") -> "Handler":
        """Fluent method to chain handlers."""
        self._next = handler
        return handler

    def handle(self, request: Request) -> str | None:
        """Handle the request or pass it to the next handler."""
        if self._next:
            return self._next.handle(request)
        return None


class SpamFilter(Handler):
    SPAM_WORDS = {"buy now", "free money", "click here", "limited offer"}

    def handle(self, request: Request) -> str | None:
        if any(word in request.content.lower() for word in self.SPAM_WORDS):
            return f"[SPAM] Blocked message from {request.sender}"
        return super().handle(request)


class AuthenticationHandler(Handler):
    VALID_SENDERS = {"alice@example.com", "bob@example.com", "admin@example.com"}

    def handle(self, request: Request) -> str | None:
        if request.sender not in self.VALID_SENDERS:
            return f"[AUTH] Rejected unknown sender: {request.sender}"
        return super().handle(request)


class RateLimitHandler(Handler):
    def __init__(self, max_per_minute: int = 10):
        super().__init__()
        self._counts: dict[str, int] = {}
        self._max = max_per_minute

    def handle(self, request: Request) -> str | None:
        count = self._counts.get(request.sender, 0)
        if count >= self._max:
            return f"[RATE LIMIT] Too many requests from {request.sender}"
        self._counts[request.sender] = count + 1
        return super().handle(request)


class MessageProcessor(Handler):
    def handle(self, request: Request) -> str | None:
        return f"[PROCESSED] Message from {request.sender}: '{request.content}'"


# Build the chain:
spam = SpamFilter()
auth = AuthenticationHandler()
rate = RateLimitHandler(max_per_minute=5)
processor = MessageProcessor()

spam.set_next(auth).set_next(rate).set_next(processor)

# Test the chain:
requests = [
    Request("Hello, how are you?", "alice@example.com"),
    Request("Buy now! Free money!", "alice@example.com"),  # Spam
    Request("Normal message", "hacker@evil.com"),           # Unknown sender
]

for req in requests:
    result = spam.handle(req)
    print(result)
```

---

## 6. State Pattern

**Intent:** Allow an object to alter its behavior when its internal state changes.

```python
# state.py
# Traffic light state machine

from abc import ABC, abstractmethod
import time


class TrafficLightState(ABC):
    """Abstract state."""

    @abstractmethod
    def next_state(self) -> "TrafficLightState":
        """Return the next state after this one."""
        ...

    @abstractmethod
    def display(self) -> str:
        """What the light looks like."""
        ...

    @abstractmethod
    def duration(self) -> int:
        """How long this state lasts in seconds."""
        ...


class RedState(TrafficLightState):
    def next_state(self) -> TrafficLightState:
        return GreenState()

    def display(self) -> str:
        return "🔴 RED — STOP"

    def duration(self) -> int:
        return 30


class GreenState(TrafficLightState):
    def next_state(self) -> TrafficLightState:
        return YellowState()

    def display(self) -> str:
        return "🟢 GREEN — GO"

    def duration(self) -> int:
        return 25


class YellowState(TrafficLightState):
    def next_state(self) -> TrafficLightState:
        return RedState()

    def display(self) -> str:
        return "🟡 YELLOW — CAUTION"

    def duration(self) -> int:
        return 5


class TrafficLight:
    """Context — delegates behavior to the current state."""

    def __init__(self):
        self._state: TrafficLightState = RedState()

    def advance(self) -> None:
        """Move to the next state."""
        self._state = self._state.next_state()

    def status(self) -> str:
        return f"{self._state.display()} (for {self._state.duration()}s)"


# Usage:
light = TrafficLight()
for _ in range(6):
    print(light.status())
    light.advance()
```

---

## Functions vs Classes for Behavioral Patterns

Python's first-class functions dramatically simplify many behavioral patterns:

| Pattern | Java needs | Python can use |
|---------|-----------|---------------|
| Strategy | Interface + classes | Simple function reference |
| Command | Command interface hierarchy | Callable or lambda |
| Template Method | Abstract class | Function with callback parameters |
| Observer | Listener interface | Any callable |

```python
# Pythonic simplification of Strategy pattern
# Instead of:
class BubbleSortStrategy:
    def sort(self, data): ...

class MergeSortStrategy:
    def sort(self, data): ...

# Just use functions:
def bubble_sort(data): ...
def merge_sort(data): ...

# And pass them directly:
process_data(data, strategy=merge_sort)

# Or even more Pythonically:
process_data(sorted(data))
```

The principle: **reach for a pattern when you need it, not by default.** Many patterns that are mandatory in Java are optional or simplified in Python.
