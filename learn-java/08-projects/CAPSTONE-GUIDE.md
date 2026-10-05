# Capstone Project Guide

This guide explains how to approach each project in module 08. Read it once before starting Project 1, and re-read it before each subsequent project.

---

## Step 1 — Read the README First, Completely

Every project folder has a README. Read the whole thing before writing a single line of code. Understand:

- What the application does (from a user's perspective)
- What features are required
- What layers the architecture has
- What specific Java concepts the project targets

The biggest time-waster in project work is building the wrong thing. Two minutes of careful reading saves two hours of rework.

---

## Step 2 — Design Before Coding

Before opening your editor, spend time with pen and paper (or a whiteboard).

**Draw a UML class diagram.** You do not need a tool — boxes and arrows on paper are fine. Include:

- Your model classes and their fields
- The interfaces (repository, service)
- The relationships between classes (uses, implements, extends)
- The layer boundaries (which classes belong in which package)

Ask yourself these questions:

- Which classes hold data? (models)
- Which classes store and retrieve data? (repositories)
- Which classes contain business logic? (services)
- Which classes interact with the user? (CLI, I/O layer)

If you cannot draw the diagram, you do not yet understand the requirements well enough to code. Go back to Step 1.

---

## Step 3 — Write Tests First (TDD Approach)

For each class you plan to write, create the test file first. Write the test method signatures — even if the test bodies are empty — before you implement anything.

This forces you to think about:

- What the public API of each class should look like
- What the expected behavior is for both happy paths and edge cases
- What inputs should cause exceptions

Example workflow for a single class:

1. Create `TaskServiceTest.java`
2. Write the test method names: `testCreateTask_success`, `testCreateTask_emptyTitle_throwsException`, etc.
3. Write the first test body
4. Run the test — it will not compile yet
5. Write just enough production code to make it compile and the test pass
6. Move to the next test

This is the Red → Green → Refactor cycle. You do not need to be strict about it, but the discipline of writing the test before the implementation produces better designs.

---

## Step 4 — Build Incrementally

Do not try to build everything at once. Use this order:

1. **Model classes first** — plain data objects with no dependencies
2. **Repository interface and in-memory implementation** — storage without business logic
3. **Service class** — business logic that depends on the repository via its interface
4. **CLI or presentation layer last** — the user-facing part depends on everything else

Commit (or at least save and compile) after each layer. If you get a compile error after adding 200 lines of code, it is hard to find. If you get one after adding 20 lines, it is easy.

---

## Step 5 — Refactor After Green Tests

Once all tests pass, look at your code with fresh eyes. Ask:

- Is there duplication I can extract into a helper method?
- Are any methods longer than 20 lines? Can they be split?
- Are my names clear? Would someone reading this code for the first time understand it without comments?
- Does each class have a single, clear responsibility?
- Am I violating any of the SOLID principles?

Refactoring is not optional polish — it is part of building software. The first working version is always rough. The refactored version is what you show people.

---

## What Interviewers Look For in Project Code

When you submit a project as a portfolio piece or discuss it in an interview, these are the things that distinguish senior-quality work from beginner work:

### Clean Code

- Methods do one thing
- Names are descriptive (`calculateCompletionRate()` not `calc()`)
- No magic numbers — use named constants or enums
- No commented-out code
- Consistent formatting

### Test Coverage

- Happy paths tested
- Edge cases tested (empty input, null, boundary values)
- Exception paths tested
- Test names describe what they test (`testCreateTask_emptyTitle_throwsException`)

### SOLID Principles

- **S** — Each class has one reason to change
- **O** — You can add new task priorities without modifying existing code
- **L** — Subclasses can replace their parents without breaking behavior
- **I** — Interfaces are small and focused
- **D** — High-level classes depend on interfaces, not concrete implementations

### Proper Naming

Naming is the single most important readability factor. Follow these conventions:

- Classes: `PascalCase`, noun or noun phrase (`TaskService`, `InMemoryTaskRepository`)
- Methods: `camelCase`, verb or verb phrase (`findById`, `calculateStats`)
- Constants: `UPPER_SNAKE_CASE` (`MAX_TITLE_LENGTH`)
- Packages: `lowercase`, singular (`model`, `service`, `repository`)

### Architecture

- Clear layer separation — no business logic in the CLI, no I/O in the service
- Program to interfaces — `TaskRepository repo` not `InMemoryTaskRepository repo`
- Dependencies flow inward — outer layers depend on inner layers, never the reverse

---

## Common Mistakes to Avoid

**Putting business logic in the model**
A `Task` class should be a data object. It can have simple derived properties (like `isOverdue()`), but it should not call a repository or service.

**Putting business logic in the CLI**
The CLI's job is input/output. If you are writing `if` statements about task priorities in `TaskCli.java`, move that logic to `TaskService.java`.

**Not validating input**
Always validate at the service layer. An empty title, a null priority, or a negative amount should throw a clear `IllegalArgumentException` with a helpful message — never silently proceed.

**Catching and swallowing exceptions**
```java
// BAD
try {
    taskService.completeTask(id);
} catch (Exception e) {
    // do nothing
}
```
Either handle the exception meaningfully, rethrow it, or log it. Never swallow it silently.

**Writing one giant class**
If your service class is 500 lines, it is doing too much. Split it.

**Ignoring the Optional contract**
If a method returns `Optional<Task>`, always handle the empty case. Never call `.get()` without checking `.isPresent()` or using `.orElseThrow()`.

**Skipping the repository interface**
It is tempting to use `InMemoryTaskRepository` directly everywhere. Resist. Always inject the interface `TaskRepository`. This is what makes your code testable with mocks and swappable to a database implementation later.

---

## A Note on Complexity

These projects deliberately avoid frameworks (Spring, Hibernate) and build everything from Java standard library. This is intentional: understanding the plumbing makes you a better engineer when you eventually use the frameworks. If you understand why a service class needs a repository interface, you will understand why Spring's dependency injection exists.

Good luck. Build something you are proud of.
