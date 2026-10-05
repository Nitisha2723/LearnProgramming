# Learn Java — From Zero to Production

A complete, structured Java learning repository for everyone from absolute beginners to developers levelling up for senior roles. Every concept is explained, every example is runnable, and every module builds on the last.

---

## Who This Is For

| You are... | Start here |
|---|---|
| Complete beginner (never programmed) | Module 01 — Foundations |
| Programmer new to Java | Module 02 — Core Language |
| Java beginner wanting OOP depth | Module 03 — OOP |
| Preparing for technical interviews | Module 09 — Interview Prep |
| Experienced, filling gaps | Jump to any module |

No prior programming experience required. We start from "what is a computer?" and build up from there.

---

## Learning Philosophy

Every module follows the same five-step cycle:

```
CONCEPT  →  UNDERSTAND  →  APPLY  →  BUILD  →  REFLECT
```

1. **Concept** — Read the theory files. Understand the *why* before the *how*.
2. **Understand** — Study the annotated code examples. Every line is explained.
3. **Apply** — Work through the exercises. Struggle first, then check solutions.
4. **Build** — Complete the mini-project at the end of each module.
5. **Reflect** — Ask: what did I learn? What am I still unsure about? What would I do differently?

This is not a tutorial to passively read. It is a curriculum to actively work through. The exercises are where learning actually happens.

---

## Full Learning Path

| # | Module | What You Learn | Est. Time |
|---|--------|---------------|-----------|
| 01 | **Foundations** | How computers work, what Java is, the JVM, your first program, data types, operators | 1–2 weeks |
| 02 | **Core Language** | Control flow, loops, methods, arrays, strings, input/output | 2–3 weeks |
| 03 | **Object-Oriented Programming** | Classes, objects, inheritance, polymorphism, interfaces, abstraction | 3–4 weeks |
| 04 | **Data Structures & Algorithms** | Lists, maps, sets, stacks, queues, sorting, searching, Big-O notation | 3–4 weeks |
| 05 | **Real-World Java** | File I/O, exceptions, generics, collections framework, date/time API | 2–3 weeks |
| 06 | **Design Patterns & Clean Code** | SOLID principles, Gang of Four patterns, refactoring, code smells | 2–3 weeks |
| 07 | **Advanced Java** | Concurrency, streams, lambdas, functional programming, optional, reflection | 3–4 weeks |
| 08 | **Projects** | Build a CLI task manager, REST API simulator, simple bank system | 4–6 weeks |
| 09 | **Interview Prep** | Top 50 Java interview questions, whiteboard coding, system design basics | 2–4 weeks |

**Total estimated time:** 22–37 weeks (studying 1–2 hours per day)

---

## How to Use This Repo

### For Beginners (recommended path)

Go in order. Do not skip modules. Each module assumes you have mastered the one before it.

1. Read the module `README.md` for objectives and overview.
2. Read all files in `theory/` — take notes.
3. Study each file in `code/` — run them, change them, break them on purpose.
4. Attempt every exercise in `exercises/` before looking at solutions.
5. Complete the `mini-project/`.
6. Only then move to the next module.

### For Experienced Developers

Jump directly to the module covering your gap. The theory files are concise — even experienced developers often benefit from reading them to see Java-specific nuances.

### For Interview Prep

Do modules 01–04 thoroughly, then jump to module 09. Come back and fill gaps as they surface.

---

## Prerequisites

**None.** If you can use a computer and are willing to think carefully, you can learn Java here.

You will need:
- A computer running Windows, macOS, or Linux
- An internet connection (for installing tools)
- Patience and persistence

---

## Setting Up Your Environment

### Step 1 — Install Java (JDK 21)

Java 21 is the current Long-Term Support (LTS) release. Install it first.

**Option A — From Adoptium (recommended for beginners)**
1. Go to https://adoptium.net
2. Download the installer for your OS (choose "JDK 21 LTS")
3. Run the installer, accept all defaults

**Option B — Using a package manager**
```bash
# macOS (using Homebrew)
brew install temurin@21

# Ubuntu/Debian
sudo apt install openjdk-21-jdk

# Windows — use the Adoptium installer above
```

**Verify the installation:**
```bash
java --version
javac --version
```

You should see output like `openjdk 21.0.x ...`. If you do, you're ready.

### Step 2 — Choose an Editor

**VS Code (recommended for beginners)**
1. Download from https://code.visualstudio.com
2. Install the "Extension Pack for Java" (by Microsoft) — search in the Extensions panel
3. Open the `learn-java` folder

**IntelliJ IDEA (recommended for serious Java work)**
1. Download Community Edition (free) from https://www.jetbrains.com/idea/
2. Open this project as a new project
3. Set the Project SDK to the JDK 21 you installed

**Just a terminal (totally fine)**
No IDE needed. Any text editor and a terminal works perfectly.

### Step 3 — Run Your First Program

From the terminal, navigate to the `01-foundations/code/` directory:

```bash
cd 01-foundations/code

# Compile
javac HelloWorld.java

# Run
java HelloWorld
```

You should see:
```
Hello, World!
Welcome to Java!
Hello, Learner! Let's learn Java.
```

If you see that output, your environment is working correctly.

---

## What Can You Build With Java?

Java is not a toy language or a learning-only language. It powers some of the most critical and high-scale systems in the world.

### Android Applications
Android's native development language is Java (and Kotlin, which runs on the same JVM). Every Android app you have ever used — WhatsApp, Spotify, Instagram's Android client — was built with Java or its close cousin. When you learn Java, you are one step away from building apps for over 3 billion Android devices.

### Backend APIs and Web Services
Spring Boot, the most popular Java web framework, is used by Netflix, Airbnb, Uber, LinkedIn, and thousands of other companies to build the backend servers that power their products. A Java backend developer is one of the most in-demand roles in software engineering.

### Enterprise Systems
Banking software. Insurance platforms. ERP systems. Government infrastructure. Healthcare records systems. These high-stakes, high-reliability systems disproportionately run on Java because of its stability, performance, and decades of production-tested libraries.

### Games
Minecraft — the best-selling video game of all time with over 238 million copies sold — was written in Java. The game's entire core engine and server infrastructure is Java.

### Data Pipelines and Big Data
Apache Kafka (the world's most-used data streaming platform), Apache Spark (big data processing), and Apache Hadoop are all written in Java or run on the JVM. Data engineers use Java constantly.

### NASA and Scientific Computing
NASA's mission control software, scientific simulation tools, and satellite control systems use Java. The Mars Science Laboratory's ground data system — the software that helps operate the Curiosity and Perseverance rovers — is written in Java.

---

## Your First 30 Minutes

If you want to jump straight in and feel the language before reading theory:

**Minutes 1–5: Setup check**
```bash
java --version    # Should show Java 21
javac --version   # Should match
```

**Minutes 5–15: Run your first program**
```bash
cd 01-foundations/code
javac HelloWorld.java
java HelloWorld
```

Now open `HelloWorld.java` in your editor. Change `"Learner"` to your actual name. Recompile and run it. You just modified a Java program.

**Minutes 15–25: Explore data types**
```bash
javac DataTypesDemo.java
java DataTypesDemo
```

Read through `DataTypesDemo.java` — every line is commented. Don't worry if you don't understand everything yet. Just get a feel for what Java code looks like.

**Minutes 25–30: Attempt Exercise 1**
Open `01-foundations/exercises/Exercise01_HelloWorld.java`. Read the TODO comments and try to complete it. If you get stuck, the solution is in `exercises/solutions/Exercise01_Solution.java`.

After 30 minutes, you will have compiled and run real Java programs and written your first lines of Java. Now start at Module 01 and go through it properly.

---

## Module Overview

| Module | Directory | Status |
|--------|-----------|--------|
| 01 — Foundations | `01-foundations/` | Complete |
| 02 — Core Language | `02-core-language/` | Coming soon |
| 03 — OOP | `03-oop/` | Coming soon |
| 04 — Data Structures | `04-data-structures/` | Coming soon |
| 05 — Real-World Java | `05-real-world/` | Coming soon |
| 06 — Design Patterns | `06-design/` | Coming soon |
| 07 — Advanced Java | `07-advanced/` | Coming soon |
| 08 — Projects | `08-projects/` | Coming soon |
| 09 — Interview Prep | `09-interview-prep/` | Coming soon |

---

## Repository Philosophy

This repository is opinionated:

- **Depth over breadth.** Better to truly understand ten concepts than to skim thirty.
- **Working code first.** Theory that is not anchored to runnable code is easily forgotten.
- **Struggle is learning.** Exercises do not hold your hand. Look at solutions only after genuinely trying.
- **Real-world context.** Every concept is connected to how it is used in production Java code.
- **Progressive complexity.** Each module assumes the previous. Do not skip.

---

*Start at `01-foundations/README.md` and begin the journey.*
