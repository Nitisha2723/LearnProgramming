# What Is Java?

Java is a general-purpose programming language and computing platform released in 1995 by Sun Microsystems. It is one of the most widely used programming languages in the world, running on billions of devices — from Android phones to NASA spacecraft to the servers powering your bank's website.

---

## The History of Java

### The Problem Java Solved

In the early 1990s, the software world had a serious problem: programs were not portable.

If you wrote a program in C for Windows, it would not run on a Mac or a Unix workstation. You had to rewrite (or at least recompile) it for each platform. This was expensive, slow, and error-prone. Companies spent enormous resources maintaining separate codebases for different operating systems.

Additionally, networked computing was exploding — the World Wide Web was emerging. People wanted software that could run securely inside web browsers, across all kinds of devices, without knowing in advance what hardware the user had.

### James Gosling and the Green Team

In 1991, Sun Microsystems assembled a small team of engineers called the **Green Team**, led by **James Gosling**. Their mission: create a language for consumer electronics — set-top boxes, handheld devices — where portability and reliability were critical.

Gosling started designing a new language called **Oak** (later renamed Java, supposedly after the coffee consumed in large quantities during development). The core design goal was revolutionary for its time:

> **"Write Once, Run Anywhere"** (WORA)

The idea: write your Java program once, and it runs identically on any device that has a Java Virtual Machine installed — Windows, macOS, Linux, embedded systems, phones.

### 1995: Java Goes Public

Sun released Java 1.0 to the public in **1995**. The timing was perfect — the Web was exploding, and Java's ability to run inside web browsers (via "applets") made it an instant sensation.

By the late 1990s, Java was one of the most popular languages in the world. Universities were teaching it as the first programming language. Enterprise companies were building critical systems in it.

### Oracle Acquires Java

Sun Microsystems struggled financially in the 2000s and was acquired by **Oracle Corporation in 2010**. Oracle now owns and develops Java, though the language is largely governed by open standards and the OpenJDK open-source implementation.

### Java Today

Java has released a new version every six months since 2018. The current **LTS (Long-Term Support) releases** are Java 21 (released September 2023) and Java 17 (released September 2021). For serious work, use an LTS release.

---

## Why Java Became So Popular

Java's success was not accidental. Several design decisions made it uniquely suited to the needs of software in the 1990s and 2000s.

### Platform Independence

The JVM (Java Virtual Machine) is the key. Your compiled Java code (bytecode) runs on any platform that has a JVM. Sun shipped JVMs for Windows, macOS, and Unix — so Java programs ran everywhere without modification.

This was genuinely revolutionary. Before Java, cross-platform software was extremely difficult.

### Safety and Reliability

Java was designed with safety as a first-class concern:

- **No manual memory management.** Java has a garbage collector that automatically reclaims memory you no longer need. In C and C++, forgetting to free memory caused catastrophic bugs. In Java, this entire class of bugs is eliminated.
- **Strong type system.** The compiler catches type errors before your program runs.
- **Array bounds checking.** Java checks that you're not reading memory you shouldn't. Buffer overflow bugs — the source of countless security vulnerabilities in C — don't exist in Java.
- **No pointers.** Java has references, which are safer and cannot be manipulated directly the way C pointers can.

### Rich Standard Library

Java ships with an enormous standard library — thousands of pre-built classes for networking, file I/O, data structures, cryptography, databases, user interfaces, and more. You do not have to build everything from scratch.

### Object-Oriented by Design

Java was built from the ground up as an object-oriented language. Every Java program is organized around classes and objects. This encourages modular, reusable, maintainable code at scale.

### Massive Ecosystem

Over thirty years, Java has accumulated one of the largest ecosystems in software:
- Thousands of open-source libraries
- Mature build tools (Maven, Gradle)
- Powerful frameworks (Spring, Hibernate, Jakarta EE)
- Enormous community knowledge base

---

## Where Java Is Used Today

### Android (Billions of Devices)

Google chose Java as the primary language for Android development when Android launched in 2008. There are currently **3+ billion active Android devices** worldwide, and nearly all the apps running on them are written in Java or Kotlin (a language that runs on the same JVM as Java and is fully interoperable with Java code).

Every Android app you have ever used — Gmail, WhatsApp, Snapchat, most banking apps — runs on the same JVM platform you are learning.

### Backend Web Services (Spring Boot)

**Spring Boot** is the most popular framework for building backend web services and APIs. Companies including Netflix, Airbnb, Uber, LinkedIn, Atlassian, and thousands of others use Spring Boot in production.

When you book an Uber or watch a Netflix show, your request likely passes through Java-powered backend services.

### Enterprise Systems

Banks, insurance companies, government agencies, and large enterprises disproportionately rely on Java for their core systems. The reliability, security, and long support cycles of Java LTS releases make it the default choice for mission-critical software.

The SWIFT network (which handles global financial transactions between banks), many stock exchange systems, and large-scale ERP platforms run on Java.

### Minecraft

Minecraft — the best-selling video game ever with over 238 million copies sold — was written in Java by Markus "Notch" Persson. While Microsoft (which acquired Minecraft) has created a separate C++ version for consoles and mobile, the original Java Edition is still actively developed and widely played.

### Big Data and Data Engineering

Apache Kafka (the world's most-used event streaming platform, used by 80% of Fortune 100 companies), Apache Spark (big data processing at scale), and Apache Hadoop are all JVM-based. Data engineers working with large-scale data pipelines use Java constantly.

### NASA

NASA has used Java in various mission systems. The ground data systems for the Mars Science Laboratory (which operates the Curiosity and Perseverance rovers) are written in Java. When engineers on Earth send commands to a rover on Mars, Java software helps process and route those commands.

---

## Java's Design Philosophy

Java was designed around a coherent set of principles:

### Strongly Typed

Every variable must have a declared type. The compiler enforces type rules. This catches mistakes early — at compile time rather than at runtime when a user might be affected.

### Object-Oriented

Java organises code into **classes** and **objects**. A class is a blueprint (e.g., "a Dog has a name and can bark"). An object is an instance of that blueprint (e.g., a specific dog named Rex). This paradigm maps naturally to real-world systems and promotes modular, reusable code.

You will learn OOP deeply in Module 03.

### Garbage Collected

Java's **garbage collector** automatically reclaims memory from objects that are no longer in use. You allocate objects freely; Java cleans up. This eliminates an enormous category of bugs (memory leaks, dangling pointers, double frees) that plague C and C++ code.

The downside: occasional GC pauses. For most applications, this is invisible. For extremely latency-sensitive systems (like some trading platforms), it requires careful tuning.

### Compiled to Bytecode, Interpreted by the JVM

Java source code compiles to platform-neutral **bytecode**, which runs on the JVM. The JVM interprets bytecode and also uses **Just-In-Time (JIT) compilation** to compile hot code paths to native machine code for performance. This is why Java, despite being "interpreted," can match or exceed C++ performance in many workloads.

---

## Java Versions: What Matters for a Learner

Java releases a new version every six months (March and September). Not all versions are created equal:

| Type | Description |
|------|-------------|
| **LTS (Long-Term Support)** | Supported with security updates for years. Use these. Current LTS versions: Java 21, Java 17, Java 11. |
| **Feature releases** | Released every six months, supported only until the next release. For learning and experimentation. |

**For this course, use Java 21 LTS.** It has the latest stable features and will be supported until at least 2028.

The major LTS milestones in Java's history:

| Version | Year | Key additions |
|---------|------|--------------|
| Java 8 | 2014 | Lambdas, streams, Optional — transformed modern Java |
| Java 11 | 2018 | New LTS schedule begins, HTTP client |
| Java 17 | 2021 | Sealed classes, pattern matching preview |
| Java 21 | 2023 | Virtual threads, pattern matching, record patterns |

Many enterprise companies still run Java 8 or Java 11. Understanding these versions is important for a working developer. This course teaches Java 21 features where relevant but flags when a feature is modern and may not be available in older environments.

---

## Summary

| Topic | Key Point |
|-------|-----------|
| Creator | James Gosling, Sun Microsystems, 1995 |
| Core promise | Write Once, Run Anywhere (via the JVM) |
| Why popular | Platform independence, safety, ecosystem, reliability |
| Where used | Android, Spring backends, enterprise, Minecraft, Big Data |
| Design principles | Strongly typed, OOP, garbage collected, compiled+interpreted |
| Use for learning | Java 21 LTS |

---

*Next: `03-jvm-jdk-jre-explained.md` — the three acronyms every Java developer must understand.*
