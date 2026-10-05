# JVM, JDK, JRE Explained

Three acronyms appear constantly in Java documentation, error messages, and job descriptions: JVM, JRE, and JDK. New Java developers often confuse them or use them interchangeably. They are related but distinct — understanding them precisely will save you hours of confusion.

---

## The Short Version

| Acronym | Full Name | What It Is | Who Needs It |
|---------|-----------|-----------|--------------|
| **JVM** | Java Virtual Machine | Runs Java bytecode | Everyone running Java programs |
| **JRE** | Java Runtime Environment | JVM + standard libraries | End users running Java apps |
| **JDK** | Java Development Kit | JRE + compiler + developer tools | Developers writing Java code |

**If you are a developer: install the JDK.** It includes everything.

---

## Understanding Each Layer

### JVM — The Engine

The **Java Virtual Machine** is the engine that executes Java programs. It is a software program that reads compiled Java bytecode and executes it on the underlying physical hardware.

**Analogy:** Think of the JVM as a translator between a universal language and each country's local language.

Imagine you write a speech in Esperanto (a constructed universal language). Anyone who speaks Esperanto can understand your speech — regardless of whether they're in Japan, Germany, or Brazil. When they speak your speech aloud, they translate it into their local language for their local audience.

The JVM is like an Esperanto speaker. Your bytecode is the Esperanto speech — universal, platform-neutral. Each JVM (one for Windows, one for macOS, one for Linux) translates it to machine code for the local hardware.

**What the JVM does:**
- Loads class files (bytecode)
- Verifies bytecode security before execution
- Executes bytecode instructions
- Manages memory (allocates memory for objects, runs the garbage collector)
- Provides the runtime environment (stack, heap, method area)

**Key point:** The JVM is what makes "Write Once, Run Anywhere" possible. The bytecode file is identical on all platforms; the JVM does the platform-specific translation.

**The JVM is NOT Java-specific.** Other languages also compile to JVM bytecode: Kotlin, Scala, Groovy, Clojure. They all run on the same JVM.

---

### JRE — What You Need to Run Java

The **Java Runtime Environment** is what an end user needs to run a Java application.

**JRE = JVM + Java Standard Library (class libraries)**

The **Java Standard Library** (also called the Java Class Library) is a massive collection of pre-written Java classes that come bundled with Java. When your program calls `System.out.println()`, that `System` class is part of the standard library. When you use `ArrayList` or `HashMap` or `Scanner`, those are all standard library classes.

Without the standard library, Java would be nearly useless — you'd have to write everything from scratch.

**Analogy:** If the JVM is a car's engine, the JRE is the whole car. The engine alone doesn't give you a working vehicle — you also need the steering wheel, fuel system, electrical system, and so on. Similarly, the JVM alone can't run most Java programs without the standard library classes they depend on.

As of Java 11, the standalone JRE is no longer distributed separately. Modern deployments include the full JDK or use modular runtimes built with the `jlink` tool. For learning, this doesn't matter — just install the JDK.

---

### JDK — What Developers Need

The **Java Development Kit** is everything a developer needs to write, compile, and run Java programs.

**JDK = JRE + javac (compiler) + developer tools**

The JDK includes:
- `javac` — the Java compiler (converts `.java` source files to `.class` bytecode)
- `java` — the JVM launcher (runs `.class` files)
- `jar` — the archiving tool (packages `.class` files into `.jar` files)
- `javadoc` — generates HTML documentation from source code comments
- `jdb` — the Java debugger
- `jconsole`, `jvisualvm` — monitoring and profiling tools
- `jshell` — an interactive REPL for experimenting with Java (added in Java 9)
- And many more tools

**You need the JDK.** When you installed Java following the setup instructions, you installed the JDK.

---

## The Relationship: Visual Diagram

```
+----------------------------------------------------------+
|                         J D K                            |
|                                                          |
|   javac  jar  javadoc  jdb  jshell  (developer tools)   |
|                                                          |
|   +--------------------------------------------------+   |
|   |                     J R E                       |   |
|   |                                                  |   |
|   |   Java Standard Library (java.lang, java.util,  |   |
|   |   java.io, java.net, java.util.concurrent ...)  |   |
|   |                                                  |   |
|   |   +------------------------------------------+  |   |
|   |   |              J V M                       |  |   |
|   |   |                                          |  |   |
|   |   |  Class Loader                            |  |   |
|   |   |  Bytecode Verifier                       |  |   |
|   |   |  Execution Engine (interpreter + JIT)    |  |   |
|   |   |  Garbage Collector                       |  |   |
|   |   |  Memory Manager (stack + heap)           |  |   |
|   |   +------------------------------------------+  |   |
|   +--------------------------------------------------+   |
+----------------------------------------------------------+
```

The JVM is inside the JRE, and the JRE is inside the JDK. Each layer adds functionality on top of the inner layer.

---

## What Is Bytecode?

When you run `javac HelloWorld.java`, the compiler produces `HelloWorld.class`. That `.class` file contains **bytecode**.

Bytecode is NOT machine code. It is an intermediate representation — a set of instructions designed for the JVM, not for any specific physical CPU.

**Why bytecode exists:**

If Java compiled directly to machine code (like C does), the compiled output would only work on one type of CPU/OS combination. Your compiled Windows x64 binary wouldn't run on a Mac or a Linux server.

Instead, Java compiles to bytecode — a compact, binary format that any JVM can understand, regardless of what CPU or OS it's running on.

```
You write:        HelloWorld.java   (plain text, human-readable)
javac compiles:   HelloWorld.class  (binary bytecode, JVM-readable)
JVM executes:     on Windows, macOS, Linux, Android... all the same .class file
```

**Bytecode is compact and fast to load.** It is smaller than source code and significantly more efficient to load and execute than re-parsing source code each time.

**Bytecode can be decompiled.** Because bytecode retains significant structural information about the original Java code, tools can reverse-engineer it back to something close to the original source. This is why production Java applications are often "obfuscated" — their bytecode is deliberately scrambled to make decompilation harder.

---

## The JIT Compiler: Why Java Is Fast

The JVM doesn't just interpret bytecode one instruction at a time forever. It includes a **Just-In-Time (JIT) compiler** that watches which parts of your bytecode are executed most frequently ("hot spots") and compiles them to native machine code.

After JIT compilation, that code runs at native speed — comparable to C or C++. The JVM continues interpreting the cold (rarely executed) parts.

This is why Java programs often get faster the longer they run: the JIT compiler gets more opportunities to identify and optimise hot code.

You'll explore JIT in more depth in `04-how-java-runs.md`.

---

## Practical Implications

### Why You Need the Right JDK Version

Different JDK versions support different Java language features. Code using Java 17+ features will not compile with JDK 11. Always check what version your project targets.

For this course: use JDK 21.

### "java.home" and Multiple JDK Versions

Experienced developers often have multiple JDK versions installed simultaneously. Tools like SDKMAN! or Homebrew on macOS make switching between versions easy. IDEs like IntelliJ allow you to configure a different JDK per project.

For now, you just need one: JDK 21.

### OpenJDK vs Other Distributions

Oracle distributes an official JDK, but there are many other distributions of OpenJDK (the open-source reference implementation):

- **Eclipse Temurin** (from Adoptium) — recommended for most users
- **Amazon Corretto** — used internally at Amazon, free for everyone
- **Microsoft Build of OpenJDK** — optimised for Azure
- **GraalVM** — alternative JVM with additional features including native compilation

For learning, any of these work identically. This course uses Eclipse Temurin 21.

---

## Summary

```
JVM  — executes bytecode (the engine)
JRE  — JVM + standard library (what's needed to run Java apps)
JDK  — JRE + compiler + tools (what developers install)

Bytecode — platform-neutral compiled output (.class files)
           enables "Write Once, Run Anywhere"
JIT     — compiles hot bytecode to native machine code at runtime
           makes Java fast despite being "interpreted"
```

---

*Next: `04-how-java-runs.md` — the complete lifecycle of a Java program from source file to execution.*
