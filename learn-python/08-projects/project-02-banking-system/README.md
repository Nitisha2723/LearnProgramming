# Project 02: Banking System

A complete banking system demonstrating Python OOP patterns: ABC, dataclasses,
custom exceptions, Decimal arithmetic, and comprehensive pytest testing.

## Architecture

```
src/
├── models/          # Account, Customer, Transaction models
├── exceptions/      # Custom exception hierarchy
├── repository/      # Abstract + in-memory implementations
├── service/         # Business logic (AccountService, TransferService)
└── main.py          # Demo entry point
```

## Features

- Register and manage customers
- Open checking and savings accounts
- Deposit, withdraw, transfer funds
- Balance validation with precise Decimal arithmetic
- Full transaction history per account

## Running

```bash
python src/main.py
```

## Running Tests

```bash
pytest tests/ -v
```

## Concepts Demonstrated

- `ABC` for abstract repositories
- `@dataclass` with `__post_init__` validation
- Custom exception hierarchy
- `Decimal` for precise financial calculations
- Dependency injection via constructor
- `pytest` fixtures, parametrize
