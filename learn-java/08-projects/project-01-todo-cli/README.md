# Project 1 — TODO CLI

A complete command-line task manager built in plain Java. No frameworks. No database. Just well-structured code across four clean layers.

---

## What the App Does

The TODO CLI lets you manage a list of tasks from your terminal. You interact with a numbered menu:

```
===========================================
          TODO TASK MANAGER
===========================================
 1. Add Task
 2. List All Tasks
 3. Complete Task
 4. Delete Task
 5. Filter by Priority
 6. View Stats
 7. Quit
===========================================
```

Tasks have a title, an optional description, and a priority level (LOW / MEDIUM / HIGH / URGENT). The app tracks when each task was created and when it was completed.

---

## Features

| Feature | Description |
|---------|-------------|
| Add task | Enter title, description, and priority |
| List all | Shows all tasks sorted by priority (highest first) |
| Complete task | Mark a task done by entering its short ID |
| Delete task | Remove a task permanently |
| Filter by priority | Show only tasks at a selected priority level |
| View stats | Total, completed, pending, and completion rate |

---

## How to Compile and Run

All source files are under `src/`. Compile from the project root:

```bash
# Compile
javac -d out src/model/Priority.java src/model/Task.java \
             src/repository/TaskRepository.java \
             src/repository/InMemoryTaskRepository.java \
             src/service/TaskService.java \
             src/cli/TaskCli.java \
             src/Main.java

# Run
java -cp out Main
```

Or compile everything at once:

```bash
find src -name "*.java" | xargs javac -d out
java -cp out Main
```

---

## Architecture Overview

The application is divided into four layers. Each layer depends only on the layer below it, never the other way around.

```
src/
  Main.java                              <- Entry point, wires everything together
  model/
    Priority.java                        <- Enum: LOW, MEDIUM, HIGH, URGENT
    Task.java                            <- Immutable-ish domain object
  repository/
    TaskRepository.java                  <- Interface: CRUD operations
    InMemoryTaskRepository.java          <- HashMap-backed implementation
  service/
    TaskService.java                     <- Business logic + validation
  cli/
    TaskCli.java                         <- User interaction (Scanner + menus)
```

### Layer Responsibilities

**model/** — Plain data objects. A `Task` knows its own fields and can mark itself complete, but it does not know about storage or business rules.

**repository/** — Storage abstraction. `TaskRepository` is an interface; `InMemoryTaskRepository` implements it using a `ConcurrentHashMap`. Swap this implementation later for a file-backed or database-backed one without touching any other layer.

**service/** — Business logic. `TaskService` validates input, enforces rules (e.g., title cannot be blank), and delegates storage to the repository. It does not know or care whether the repository uses a HashMap or a database.

**cli/** — User interface. `TaskCli` reads user input via `Scanner`, calls the service, and formats the output. It contains no business logic.

**Main.java** — The composition root. Creates one instance of each class and starts the CLI.

---

## What You Will Learn Building This Project

| Concept | Where It Appears |
|---------|-----------------|
| `enum` with fields and methods | `Priority.java` |
| `UUID` for unique IDs | `Task.java` constructor |
| `LocalDateTime` for timestamps | `Task.createdAt`, `Task.completedAt` |
| `Optional<T>` for nullable returns | `TaskRepository.findById()` |
| `HashMap` / `ConcurrentHashMap` | `InMemoryTaskRepository` |
| Interface programming | `TaskRepository` interface |
| Input validation with exceptions | `TaskService.validateTitle()` |
| `Comparator` chaining | `TaskService.getAllTasks()` sort |
| `Scanner` for user input | `TaskCli.java` |
| `Map<String, Object>` for stats | `TaskService.getCompletionStats()` |
| JUnit 5 unit tests | `tests/` directory |
| Mockito mock objects | `TaskServiceTest.java` |
| Dependency injection (manual) | Constructor parameters throughout |

---

## Tests

Test files are in `tests/`. Run them with JUnit 5 on the classpath:

```
tests/
  TaskTest.java              <- Tests for Task model
  TaskServiceTest.java       <- Tests for TaskService (uses Mockito)
  TaskRepositoryTest.java    <- Tests for InMemoryTaskRepository
```

To compile and run tests you need `junit-5.jar` and `mockito-core.jar` on the classpath. The exact commands depend on how you download the JARs.
