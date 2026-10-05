# Project 02: Banking System

## Overview

A multi-layered Java application that simulates core banking operations. This project demonstrates
object-oriented design with inheritance hierarchies, custom exception handling, repository patterns,
and service-layer architecture — all without any external frameworks.

---

## Features

- **Account Management**: Open savings and checking accounts for customers
- **Deposits & Withdrawals**: Validated transactions with balance updates
- **Fund Transfers**: Move money between accounts with proper TRANSFER_IN / TRANSFER_OUT records
- **Account Statements**: Full transaction history per account, filterable by month/year
- **Interest Calculation**: Monthly interest computed and applied to savings accounts
- **Overdraft Support**: Checking accounts can go negative up to a configurable overdraft limit
- **Minimum Balance Enforcement**: Savings accounts maintain a minimum balance floor
- **Custom Exceptions**: Typed, descriptive exceptions for insufficient funds and missing accounts

---

## Architecture

The project follows a classic layered architecture:

```
src/
├── model/
│   ├── Account.java           ← abstract base class
│   ├── SavingsAccount.java    ← extends Account; enforces MIN_BALANCE
│   ├── CheckingAccount.java   ← extends Account; allows overdraft
│   ├── Customer.java          ← customer entity
│   └── Transaction.java       ← immutable transaction record with enum
│
├── repository/
│   ├── AccountRepository.java          ← interface (contract)
│   └── InMemoryAccountRepository.java  ← HashMap-based implementation
│
├── service/
│   ├── AccountService.java      ← business logic: open, deposit, withdraw, transfer
│   └── TransactionService.java  ← reporting: history, monthly statements, interest
│
├── exception/
│   ├── InsufficientFundsException.java  ← checked exception
│   └── AccountNotFoundException.java   ← runtime exception
│
└── Main.java   ← demo entry point

tests/
├── AccountTest.java
├── AccountServiceTest.java
└── TransactionServiceTest.java
```

**Layer responsibilities:**

| Layer       | Responsibility                                          |
|-------------|--------------------------------------------------------|
| model       | Domain objects; contain field-level business rules     |
| repository  | Data access abstraction (in-memory for now)            |
| service     | Orchestrates model + repository; enforces use-cases    |
| exception   | Typed error signalling across layer boundaries         |

---

## How to Compile and Run

### Prerequisites
- Java 17 or higher
- JUnit 5 + Mockito JARs on the classpath for tests (or use Maven/Gradle)

### Compile (from project root)
```bash
javac -d out \
  src/exception/*.java \
  src/model/*.java \
  src/repository/*.java \
  src/service/*.java \
  src/Main.java
```

### Run the demo
```bash
java -cp out Main
```

### Compile and run tests (with JUnit 5 on classpath)
```bash
javac -cp junit-5.jar:mockito.jar:out -d out tests/*.java
java  -cp junit-platform-console-standalone.jar:out \
      org.junit.platform.console.ConsoleLauncher --scan-classpath
```

---

## Design Decisions

### Why is `Account` an abstract class?

`Account` captures shared state (balance, transaction history, owner) and shared behaviour
(deposit, toString) but deliberately leaves `withdraw` and interest-rate policy as abstract.
This forces each subclass to implement the rules specific to that product:

- `SavingsAccount.withdraw` protects a minimum balance.
- `CheckingAccount.withdraw` permits overdraft up to a limit.

An interface alone could not hold the shared fields and the concrete `deposit` logic.

### Why custom checked / unchecked exceptions?

`InsufficientFundsException` is **checked** because callers must decide what to do when a
withdrawal is rejected — retry, notify the user, log the event.  Ignoring it silently would
corrupt account state.

`AccountNotFoundException` is **unchecked** (RuntimeException) because it signals a programming
error (wrong account number passed in) rather than a recoverable business condition.

### Why a repository interface?

`AccountRepository` is an interface so the service layer is not coupled to any storage technology.
Swapping the in-memory map for a JDBC or JPA implementation requires changing only the repository
class, not the service or the tests.

---

## What You Will Learn

| Concept                  | Where it appears                                              |
|--------------------------|---------------------------------------------------------------|
| Abstract classes          | `Account` — shared state + abstract `withdraw`               |
| Inheritance               | `SavingsAccount`, `CheckingAccount` extend `Account`         |
| Polymorphism              | Services work with `Account` references, dispatch at runtime |
| Interfaces                | `AccountRepository` — programming to an abstraction          |
| Custom exceptions         | `InsufficientFundsException`, `AccountNotFoundException`     |
| Checked vs unchecked      | Different exception types for different failure modes        |
| Enums                     | `TransactionType` inside `Transaction`                        |
| Generics & Collections    | `List<Transaction>`, `Optional<Account>`                     |
| LocalDateTime / UUID      | Modern Java API usage for timestamps and IDs                 |
| Unit testing              | JUnit 5 + Mockito; parameterized tests                       |
| Repository pattern        | Decoupled data access layer                                  |
| Service layer pattern     | Business logic separated from storage concerns               |
