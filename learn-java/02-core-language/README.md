# Module 02 — Core Language

## What You Will Learn

This module teaches you how to control the flow of your programs and organize code into reusable pieces. By the end, you will write programs that make decisions, repeat actions, manipulate text, and are structured into clean, reusable methods.

## Prerequisites

Before starting this module, you should be comfortable with:
- [ ] What Java is and how it runs on the JVM
- [ ] The difference between JDK, JRE, and JVM
- [ ] Writing and running a basic HelloWorld program
- [ ] Java's primitive data types: `int`, `double`, `boolean`, `char`
- [ ] Arithmetic, comparison, and logical operators

If any of these feel shaky, review **Module 01 — Foundations** first.

## Estimated Time

**8–10 hours** (theory + exercises + mini-project)

---

## Learning Objectives

By completing this module, you will be able to:

### Control Flow
- [ ] Write `if/else if/else` chains to make decisions
- [ ] Use the ternary operator for concise conditional expressions
- [ ] Write `switch` statements and modern switch expressions (Java 14+)
- [ ] Choose the right loop (`while`, `do-while`, `for`, `for-each`) for each situation
- [ ] Use `break` and `continue` appropriately
- [ ] Avoid deeply nested code by applying early-return and guard clauses

### Methods
- [ ] Define methods with correct access modifiers, return types, and parameters
- [ ] Distinguish between `void` methods and methods that return values
- [ ] Overload methods with different parameter lists
- [ ] Explain pass-by-value and why Java works this way
- [ ] Describe what the call stack is and trace method calls through it

### Arrays
- [ ] Declare and initialize arrays using all three syntactic forms
- [ ] Access elements by zero-based index
- [ ] Iterate arrays with both `for` and `for-each` loops
- [ ] Perform common operations: sum, average, minimum, maximum
- [ ] Create and navigate two-dimensional arrays
- [ ] Use `Arrays.sort()` and other `Arrays` utility methods

### Strings
- [ ] Explain why `String` is immutable and what that means in practice
- [ ] Use `==` vs `.equals()` correctly (and explain why they differ)
- [ ] Apply the most important `String` methods fluently
- [ ] Use `StringBuilder` when building strings in a loop
- [ ] Format output with `String.format()` and `printf`

### Scope and Memory
- [ ] Identify local, instance, and class (static) scope
- [ ] Explain stack vs heap and which variables live where
- [ ] Trace a program's stack frames through nested method calls

---

## Module Structure

| Folder | Contents |
|--------|----------|
| `theory/` | Five deep-dive markdown guides |
| `code/` | Four runnable demo programs |
| `exercises/` | Four coding exercises with solutions |
| `tests/` | Introduction to testing + JUnit 5 test suite |
| `mini-project/` | Grade Calculator — tie everything together |

## How to Work Through This Module

1. Read `theory/01-control-flow.md`, then run `code/ControlFlowDemo.java`
2. Read `theory/02-methods.md`, then run `code/MethodsDemo.java`
3. Read `theory/03-arrays.md`, then run `code/ArraysDemo.java`
4. Read `theory/04-strings-in-depth.md`, then run `code/StringsDemo.java`
5. Read `theory/05-scope-and-memory.md` (conceptual — pairs with all demos)
6. Attempt all four exercises in `exercises/` before looking at solutions
7. Read `tests/README.md` — your introduction to automated testing
8. Build the `mini-project/GradeCalculator.java`

---

## Next Module

**Module 03 — Object-Oriented Programming**: classes, objects, constructors, encapsulation, inheritance, and polymorphism.
