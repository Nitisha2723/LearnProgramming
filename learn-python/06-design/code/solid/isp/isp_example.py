"""
Interface Segregation Principle — Python Examples
"""

from typing import Protocol


# ─── VIOLATION ────────────────────────────────────────────────────────────────

class AllInOneDeviceViolation(Protocol):
    """VIOLATION: One fat interface. Robots can't print or scan."""

    def print_document(self, content: str) -> None: ...
    def scan_document(self) -> str: ...
    def fax_document(self, number: str, content: str) -> None: ...
    def copy_document(self) -> str: ...


class SimplePrinterViolation:
    """Forced to implement fax and scan even though it has neither."""

    def print_document(self, content: str) -> None:
        print(f"Printing: {content}")

    def scan_document(self) -> str:
        raise NotImplementedError("This printer cannot scan")

    def fax_document(self, number: str, content: str) -> None:
        raise NotImplementedError("This printer cannot fax")

    def copy_document(self) -> str:
        raise NotImplementedError("This printer cannot copy")


# ─── CORRECT: Small, focused interfaces ───────────────────────────────────────

class Printable(Protocol):
    def print_document(self, content: str) -> None: ...


class Scannable(Protocol):
    def scan_document(self) -> str: ...


class Faxable(Protocol):
    def fax_document(self, number: str, content: str) -> None: ...


class SimplePrinter:
    """Implements only Printable — no forced stubs."""

    def print_document(self, content: str) -> None:
        print(f"[SimplePrinter] Printing: {content[:50]}...")


class MultifunctionDevice:
    """Implements all interfaces — it truly has all capabilities."""

    def print_document(self, content: str) -> None:
        print(f"[MFD] Printing: {content[:50]}...")

    def scan_document(self) -> str:
        print("[MFD] Scanning document...")
        return "Scanned content"

    def fax_document(self, number: str, content: str) -> None:
        print(f"[MFD] Faxing to {number}: {content[:30]}...")


class Scanner:
    """Implements only Scannable."""

    def scan_document(self) -> str:
        print("[Scanner] Scanning...")
        return "Scanned content"


def print_all(devices: list[Printable], content: str) -> None:
    """Only needs Printable — works with SimplePrinter and MultifunctionDevice."""
    for device in devices:
        device.print_document(content)


def scan_all(devices: list[Scannable]) -> list[str]:
    """Only needs Scannable."""
    return [device.scan_document() for device in devices]


if __name__ == "__main__":
    printer = SimplePrinter()
    mfd = MultifunctionDevice()
    scanner = Scanner()

    print("=== Printing ===")
    print_all([printer, mfd], "My important document")

    print("\n=== Scanning ===")
    results = scan_all([mfd, scanner])
    print(f"Got {len(results)} scans")
