# 07 — Advanced Java

This module takes your Java skills to the next level. You've mastered the fundamentals, learned OOP, and built real-world applications. Now we go deeper.

---

## What You'll Learn

- **Generics** — Type-safe code without casting. Master wildcards, bounded types, and PECS.
- **Functional Programming** — Lambdas, streams, and the functional interfaces that make modern Java so expressive.
- **Concurrency** — Write multi-threaded programs correctly. Understand race conditions, synchronized, and CompletableFuture.
- **Java Memory Model** — Heap vs stack, garbage collection, memory leaks, and JVM tuning.
- **Performance and Profiling** — Measure before optimizing. Know the common pitfalls and how to fix them.

---

## Theory

| File | Topic | Why It Matters |
|------|-------|----------------|
| `01-generics.md` | Type parameters, wildcards, PECS | Write reusable, type-safe code |
| `02-functional-programming.md` | Lambdas, streams, Optional | Write cleaner, more expressive code |
| `03-concurrency-basics.md` | Threads, locks, CompletableFuture | Handle parallel work correctly |
| `04-java-memory-model.md` | Heap, GC, memory leaks | Understand what the JVM is actually doing |
| `05-performance-and-profiling.md` | Profiling, caching, common pitfalls | Make code fast the right way |

---

## Code Examples

| File | Demonstrates |
|------|-------------|
| `GenericsDemo.java` | Generic stack, Pair, Result type, bounded types, wildcards |
| `FunctionalDemo.java` | Lambda pipelines, function composition, custom functional interfaces |
| `ConcurrencyDemo.java` | Race condition, synchronized fix, CompletableFuture pipeline |

---

## Exercises

| Exercise | Task |
|----------|------|
| `Exercise01_GenericDataStructures.java` | Build a generic binary search tree and generic cache |
| `Exercise02_FunctionalPipelines.java` | Process a dataset using only Stream operations |

---

## Mini-Project: Concurrent Task Manager

A production-quality task execution system using:
- `ExecutorService` for thread pool management
- `CompletableFuture` for async task chaining
- `ConcurrentHashMap` and `AtomicLong` for thread-safe state
- `BlockingQueue` for the producer-consumer pattern
- Proper synchronization throughout

This is the kind of infrastructure code you'd find in any serious Java application.

---

## Prerequisites

Complete Modules 01–05 before this one. In particular, you need:
- Comfortable with interfaces and polymorphism (Module 03)
- Familiar with the Collections Framework (Module 04)
- Have used exceptions and basic I/O (Module 05)

---

## How to Use This Module

1. Read the theory for a topic
2. Study the code example
3. Complete the exercise
4. Check your solution against the provided solution

For the mini-project: try to build it yourself before reading the provided implementation. The task manager uses patterns you'll see in frameworks like Spring and Akka.

---

## What Makes This Module Different

After Module 07, you can:

- Write APIs that work for any type (`Box<T>`, `Result<T>`, `Stack<T>`)
- Process data elegantly with streams instead of loops
- Write non-blocking async code with `CompletableFuture`
- Explain what the garbage collector actually does
- Know when your code is slow and how to fix it

These are the skills that separate junior developers from mid-level and senior developers. Most production Java code uses these features daily.
