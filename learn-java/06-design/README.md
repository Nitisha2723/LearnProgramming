# Module 06 — Design Patterns and Software Engineering

There is a moment every programmer experiences. You have learned the syntax. You can write loops and classes and handle exceptions. You can build things that work. And then you look at a large, professional codebase and feel completely lost — not because the code is complex, but because it is *organised* in ways you don't yet recognise.

This module is about crossing that threshold. It is about the transition from programmer to software engineer.

A programmer makes things work. A software engineer makes things work *and* makes them maintainable, extensible, and comprehensible to other engineers — including themselves, six months later. This is not a small distinction. Most of the cost of software is not in writing it; it is in modifying it after it is written.

The patterns and principles in this module are not academic abstractions. They are the hard-won vocabulary of professional Java development. Learn them and you will be able to read — and contribute to — serious production codebases.

---

## What You Will Learn

### SOLID Principles
Five foundational principles of object-oriented design, each addressing a specific way that code becomes brittle and hard to change:
- **S**ingle Responsibility — one reason to change
- **O**pen/Closed — open for extension, closed for modification
- **L**iskov Substitution — subtypes must honour their supertype's contract
- **I**nterface Segregation — don't force clients to depend on what they don't use
- **D**ependency Inversion — depend on abstractions, not concretions

### Design Patterns
Twenty-three canonical patterns from the Gang of Four (Gamma, Helm, Johnson, Vlissides), selected and presented for modern Java:
- **Creational**: Singleton, Factory Method, Builder
- **Structural**: Adapter, Decorator, Facade
- **Behavioral**: Observer, Strategy, Command, Template Method

### Clean Code
The principles of Robert C. Martin's *Clean Code*, translated into concrete Java practices: naming that reveals intent, functions that do one thing, comments that explain why, error handling that doesn't hide failures, and classes that have a single responsibility.

### Architecture Patterns
How professional Java applications are structured at the system level:
- Layered Architecture (Controller → Service → Repository → Database)
- The Repository Pattern and why you always depend on an interface
- MVC in Spring
- Hexagonal Architecture (Ports and Adapters)
- Domain-Driven Design concepts: Entities, Value Objects, Aggregates, Bounded Contexts, Ubiquitous Language

### Code Smells and Refactoring
How to recognise problematic code before it becomes a crisis, and the systematic techniques for fixing it safely:
- Ten classic code smells (Long Method, God Class, Feature Envy, Primitive Obsession, and more)
- Six core refactoring techniques with before-and-after Java examples
- The discipline of refactoring with tests

---

## Prerequisites

Complete modules 01 through 05 before starting this module. Specifically, you need to be comfortable with:

- Java classes, interfaces, inheritance, and polymorphism (Module 02)
- Generics and collections (Module 03)
- Exceptions and I/O (Module 03)
- Functional programming and streams (Module 04)
- Working with unit tests — JUnit 5 basics (Module 05)

Design patterns only make sense when you understand what problems they solve. If you haven't encountered the problems yet, the patterns look like unnecessary complexity. After Module 05, you will have built enough real code to feel the pain that each pattern addresses.

---

## Module Contents

```
06-design/
├── README.md                           ← You are here
│
├── theory/
│   ├── 01-solid-principles.md          ← SOLID with Java examples
│   ├── 02-creational-patterns.md       ← Singleton, Factory, Builder
│   ├── 03-structural-patterns.md       ← Adapter, Decorator, Facade
│   ├── 04-behavioral-patterns.md       ← Observer, Strategy, Command
│   ├── 05-clean-code.md               ← Naming, functions, comments, errors
│   ├── 06-architecture-patterns.md    ← Layered, Repository, MVC, Hexagonal, DDD
│   └── 07-code-smells-and-refactoring.md ← Smells + refactoring techniques
│
├── code/
│   ├── README.md                       ← How to compile and run all examples
│   ├── solid/
│   │   ├── srp/                        ← Single Responsibility examples
│   │   ├── ocp/                        ← Open/Closed examples
│   │   ├── lsp/                        ← Liskov Substitution examples
│   │   ├── isp/                        ← Interface Segregation examples
│   │   └── dip/                        ← Dependency Inversion examples
│   └── patterns/
│       ├── singleton/
│       ├── factory/
│       ├── builder/
│       ├── observer/
│       ├── strategy/
│       ├── decorator/
│       └── repository/
│
├── exercises/
│   ├── 01-solid-violations.md          ← Identify and fix SOLID violations
│   ├── 02-pattern-recognition.md       ← Spot patterns in existing code
│   ├── 03-refactoring-kata.md          ← Refactor a provided messy class
│   ├── 04-design-from-scratch.md       ← Design a small system from requirements
│   └── solutions/                      ← Reference solutions (try first!)
│
└── mini-project/
    └── notification-system/            ← The module capstone project
```

---

## Recommended Learning Path

This module is dense. Do not rush it. Each topic builds on the previous.

**Week 1 — Foundations**
1. Read `theory/01-solid-principles.md` carefully. This is the most important document in the module.
2. Study the SOLID code examples in `code/solid/`. Run them, read them, understand both the violation and the correct version.
3. Complete Exercise 01 before moving on.

**Week 2 — Patterns**
4. Read `theory/02-creational-patterns.md` through `theory/04-behavioral-patterns.md`.
5. For each pattern: read the theory, then study the corresponding code example, then try to write it from memory.
6. Complete Exercise 02.

**Week 3 — Clean Code and Architecture**
7. Read `theory/05-clean-code.md`. Apply the naming and function rules to your code *immediately* — habits take time to build.
8. Read `theory/06-architecture-patterns.md`. The Layered Architecture section is essential for any Spring developer.
9. Complete Exercise 03 (the refactoring kata).

**Week 4 — Integration and Project**
10. Read `theory/07-code-smells-and-refactoring.md`.
11. Complete Exercise 04.
12. Build the mini-project: the Notification System.

---

## How to Use the Code Examples

Each code example in `code/solid/` and `code/patterns/` follows a consistent structure:

```
some-pattern/
├── ViolationExample.java   ← The BAD version (always labelled clearly)
├── CorrectExample.java     ← The GOOD version
└── Demo.java               ← A runnable main() to see both in action
```

To compile and run (from the module root):

```bash
# Compile a specific example
javac -d out code/solid/srp/ViolationExample.java
javac -d out code/solid/srp/CorrectExample.java
javac -d out code/solid/srp/Demo.java

# Run the demo
java -cp out Demo

# Or compile everything at once
find code -name "*.java" | xargs javac -d out
```

Read the `code/README.md` for a complete index of all examples and their learning objectives.

---

## What You Will Build: The Notification System

The module mini-project is a notification system that sends messages to users via multiple channels: email, SMS, and push notifications. It sounds simple, but it is an ideal vehicle for practising design principles.

The project is designed to be built incrementally. Each iteration introduces a new design requirement that, without the patterns from this module, would require a messy change:

**Iteration 1** — A single `NotificationService` that sends emails. (Deliberately simple.)

**Iteration 2** — Add SMS. Without OCP, you modify the existing class. With OCP, you extend it. Compare the approaches.

**Iteration 3** — Add user notification preferences. Some users want email only, some want SMS, some want both. This is the Strategy pattern.

**Iteration 4** — Add delivery confirmation and logging. This is the Observer pattern — the core system doesn't need to know about logging or confirmation; they listen to events.

**Iteration 5** — Add retry logic for failed deliveries. The Decorator pattern wraps the delivery mechanism without changing it.

**Iteration 6** — Refactor to hexagonal architecture. The business logic should have zero dependencies on email libraries or HTTP clients.

By the end, you will have a system that is fully tested, easily extensible, and demonstrably superior to a naive implementation — and you will understand exactly *why* it is better.

---

## Expectations

This module expects more from you than the previous ones. You are not just learning new syntax — you are learning to *think* differently about code.

**Be patient with the patterns.** Patterns only make sense in context. If a pattern seems like unnecessary complexity, ask yourself: "What problem is it solving? What would the code look like without it?" Return to the pattern after you've tried the naive approach.

**Refactor your old code.** After reading the clean code and refactoring chapters, go back to code you wrote in earlier modules. Rename variables. Extract methods. Apply what you've learned. The best practice is on real code.

**Read production code.** Find a well-regarded open-source Java project — Spring Framework itself, Apache Commons, Guava — and read it. Look for the patterns you've learned. You will find them everywhere.

**Engage with the exercises.** The exercises are not optional quizzes. They are the actual learning. Reading theory without practice is nearly worthless.

The engineers who master this material are the ones who can look at a large, unfamiliar system and understand it — not because they have superhuman intelligence, but because they recognise the patterns. That recognition is a skill, and like all skills, it is built through deliberate practice.

You are ready. Let's build something worth building.
