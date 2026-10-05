"""
Exercise 01 — Refactor to SOLID

This file contains badly designed code that violates multiple SOLID principles.
Your task: identify the violations and refactor to clean, SOLID-compliant code.

Run the tests in tests/test_design_patterns.py to verify your solution.
"""

# ─────────────────────────────────────────────────────────────────────────────
# EXERCISE 1A: Refactor to Single Responsibility Principle
#
# Problem: ReportGenerator does too many things.
# Task: Split it into focused classes.
# ─────────────────────────────────────────────────────────────────────────────

import json
import csv
from io import StringIO
from datetime import datetime


class ReportGenerator:
    """VIOLATION: This class violates SRP — it does data fetching, processing,
    formatting, and output all in one place.

    Refactor this into separate classes:
    - DataFetcher: get sales data
    - ReportCalculator: compute totals, averages, etc.
    - ReportFormatter: format the data (JSON, CSV, plain text)
    - ReportWriter: write to file or stdout
    """

    def __init__(self, db_connection):
        self.db = db_connection
        self.data = []

    def generate_report(self, start_date: str, end_date: str, output_format: str = "json",
                        output_file: str | None = None) -> str:
        # Fetch data
        rows = self.db.execute(
            "SELECT * FROM sales WHERE date BETWEEN ? AND ?",
            (start_date, end_date)
        )
        self.data = [{"date": r[0], "product": r[1], "amount": r[2], "quantity": r[3]}
                     for r in rows]

        # Calculate
        total = sum(row["amount"] for row in self.data)
        avg = total / len(self.data) if self.data else 0
        by_product = {}
        for row in self.data:
            if row["product"] not in by_product:
                by_product[row["product"]] = 0
            by_product[row["product"]] += row["amount"]

        report = {
            "period": f"{start_date} to {end_date}",
            "total_sales": total,
            "average_sale": avg,
            "sales_by_product": by_product,
            "record_count": len(self.data),
            "generated_at": datetime.now().isoformat(),
        }

        # Format
        if output_format == "json":
            output = json.dumps(report, indent=2)
        elif output_format == "csv":
            buffer = StringIO()
            writer = csv.writer(buffer)
            writer.writerow(["metric", "value"])
            for key, value in report.items():
                writer.writerow([key, value])
            output = buffer.getvalue()
        else:
            output = "\n".join(f"{k}: {v}" for k, v in report.items())

        # Write
        if output_file:
            with open(output_file, "w") as f:
                f.write(output)
        else:
            print(output)

        return output


# ─────────────────────────────────────────────────────────────────────────────
# YOUR REFACTORED CODE HERE
# ─────────────────────────────────────────────────────────────────────────────

# TODO: Create these classes:
# class SalesDataFetcher:
#     """Fetches raw sales data from the database."""
#     def __init__(self, db): ...
#     def fetch(self, start_date: str, end_date: str) -> list[dict]: ...
#
# class SalesCalculator:
#     """Computes statistics from sales data."""
#     def compute(self, records: list[dict]) -> dict: ...
#
# class ReportFormatter:
#     """Formats a report dict into a string."""
#     def format_json(self, report: dict) -> str: ...
#     def format_csv(self, report: dict) -> str: ...
#     def format_text(self, report: dict) -> str: ...
#
# class ReportWriter:
#     """Outputs a formatted report string."""
#     def write_to_file(self, content: str, path: str) -> None: ...
#     def write_to_stdout(self, content: str) -> None: ...


# ─────────────────────────────────────────────────────────────────────────────
# EXERCISE 1B: Fix Open/Closed Violation
#
# Problem: calculate_shipping adds a new shipping method by modifying the function.
# Task: Refactor using Protocol or ABC so new methods can be added without
#       changing existing code.
# ─────────────────────────────────────────────────────────────────────────────

def calculate_shipping(weight_kg: float, shipping_type: str, distance_km: float) -> float:
    """VIOLATION: Adding new shipping methods requires editing this function."""
    if shipping_type == "standard":
        return weight_kg * 1.5 + distance_km * 0.01
    elif shipping_type == "express":
        return weight_kg * 3.0 + distance_km * 0.02 + 5.0  # $5 handling
    elif shipping_type == "overnight":
        return weight_kg * 5.0 + distance_km * 0.05 + 10.0  # $10 handling
    # What if we add "drone" delivery? We'd have to edit this function.
    else:
        raise ValueError(f"Unknown shipping type: {shipping_type}")


# TODO: Refactor to use Protocol + concrete classes
# class ShippingCalculator(Protocol):
#     def calculate(self, weight_kg: float, distance_km: float) -> float: ...
#     def name(self) -> str: ...
#
# class StandardShipping:
#     ...
#
# class ExpressShipping:
#     ...
#
# class OvernightShipping:
#     ...
#
# # Adding this later without changing existing code:
# class DroneShipping:
#     ...


# ─────────────────────────────────────────────────────────────────────────────
# EXERCISE 1C: Fix DIP Violation
#
# Problem: NotificationService creates its own dependencies.
# Task: Inject them through the constructor.
# ─────────────────────────────────────────────────────────────────────────────

class NotificationServiceViolation:
    """VIOLATION: Creates its own email and SMS clients — cannot be tested
    without real external services."""

    def __init__(self):
        # These would normally make real network connections
        # self._email = SmtpEmailClient("smtp.gmail.com", 587)
        # self._sms = TwilioSmsClient("account_sid", "auth_token")
        pass

    def notify(self, user_id: str, message: str, channel: str) -> None:
        if channel == "email":
            # self._email.send(get_user_email(user_id), "Notification", message)
            print(f"[Email] {user_id}: {message}")
        elif channel == "sms":
            # self._sms.send(get_user_phone(user_id), message)
            print(f"[SMS] {user_id}: {message}")


# TODO: Refactor NotificationService to inject its dependencies
# from typing import Protocol
#
# class EmailClient(Protocol):
#     def send(self, to: str, subject: str, body: str) -> None: ...
#
# class SmsClient(Protocol):
#     def send(self, to: str, message: str) -> None: ...
#
# class NotificationService:
#     def __init__(self, email: EmailClient, sms: SmsClient):
#         ...


# ─────────────────────────────────────────────────────────────────────────────
# HOW TO CHECK YOUR WORK
# Run: python -m pytest tests/test_design_patterns.py::TestSolidExercises -v
# ─────────────────────────────────────────────────────────────────────────────
