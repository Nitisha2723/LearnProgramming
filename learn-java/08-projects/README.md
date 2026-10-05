# Module 08 — Capstone Projects

This module contains four progressively complex capstone projects. Each one is a complete, runnable application — not an exercise, not a toy. You build real software here.

---

## Overview

By this point you have covered the Java fundamentals, OOP, exceptions, collections, generics, functional style, I/O, and testing (modules 01–07). These projects are where you apply everything together. Each project layers on the previous one in both complexity and design quality.

---

## How the Projects Build on Each Other

### Project 1 — TODO CLI (Foundations + Core Language)

A command-line task manager. Simple, but complete: real model objects, a repository interface, a service layer, and a menu-driven CLI. No frameworks, no database — just clean Java.

**Focus:** enums, UUID, LocalDateTime, HashMap, Scanner, interfaces, basic unit tests.

This is the "hello world" of application architecture. If you can build it cleanly, you understand the layers that every bigger project needs.

---

### Project 2 — Banking System (OOP + Design Patterns)

A bank with accounts, transactions, and transfers. Multiple account types (Checking, Savings, CreditCard) share a common `Account` base. Business rules enforce things like overdraft limits and minimum balances.

**Focus:** inheritance, polymorphism, abstract classes, custom exceptions, the Strategy pattern (interest calculation), the Observer pattern (transaction notifications), defensive programming.

You will see why raw inheritance breaks down and when composition beats it.

---

### Project 3 — Student Management System (SOLID + Repository + Testing)

A multi-entity application: students, courses, enrollments, grades. Full CRUD on each entity. Persistence via a file-backed repository. Comprehensive test coverage including integration tests.

**Focus:** all five SOLID principles applied consciously, the Repository pattern with swappable implementations, dependency injection, parameterized tests, test coverage analysis.

This is the project that teaches you how professionals structure a backend system.

---

### Project 4 — E-Commerce Platform (Everything Combined — Portfolio Piece)

Products, customers, shopping carts, orders, payments. Multiple payment strategies. Order state machine. Basic inventory management. CSV import/export. Clean REST-style service layer ready to be wired to a web framework later.

**Focus:** design patterns (Strategy, State, Builder, Factory), stream-heavy data processing, file I/O, robust exception handling, full test suite with mocks.

This is the project you put on your CV. It demonstrates that you can build something non-trivial with professional-grade code quality.

---

## Prerequisites

Complete modules 01–07 before starting here:

| Module | Topic |
|--------|-------|
| 01 | Java Fundamentals |
| 02 | Object-Oriented Programming |
| 03 | Exceptions and Error Handling |
| 04 | Collections and Generics |
| 05 | Functional Programming (Streams, Lambdas) |
| 06 | File I/O and Serialization |
| 07 | Testing (JUnit 5, Mockito) |

---

## How to Use This Module

1. Read the project README inside each `project-0X-*/` folder.
2. Read `CAPSTONE-GUIDE.md` in this directory — it tells you how to approach each project like a professional.
3. Work through the project on your own before looking at solutions.
4. After finishing, review the reference solution if one is provided, and compare your design decisions.
5. Move to the next project only when the current one compiles, passes tests, and you can explain every design choice.

---

## Learning Objectives

After completing all four projects you will be able to:

- Design a multi-layer application (model / repository / service / presentation) from scratch
- Apply OOP principles — encapsulation, inheritance, polymorphism, abstraction — purposefully
- Write clean interfaces and program to abstractions rather than implementations
- Use common design patterns (Strategy, Observer, Repository, Builder, Factory, State) where they genuinely help
- Apply all five SOLID principles and articulate why each one matters
- Write unit tests with JUnit 5, use Mockito for mocks and stubs, and achieve meaningful test coverage
- Handle errors gracefully with well-designed custom exceptions
- Read and write files (text, CSV)
- Speak confidently about your architectural decisions in an interview

---

## Directory Layout

```
08-projects/
  README.md               <- This file
  CAPSTONE-GUIDE.md       <- How to approach projects
  project-01-todo-cli/    <- Project 1
  project-02-banking/     <- Project 2
  project-03-student-mgmt/  <- Project 3
  project-04-ecommerce/   <- Project 4
```
