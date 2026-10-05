# Module 01 — Foundations

Welcome to the first module. This is where everything begins.

By the end of this module, you will understand what Java is, how it runs, and how to write and run basic programs. More importantly, you will understand *why* things work the way they do — not just how to type them.

---

## Learning Objectives

After completing this module, you will be able to:

- [ ] Explain what a computer program is in plain English
- [ ] Describe what Java is and where it is used
- [ ] Explain the difference between JVM, JDK, and JRE without hesitation
- [ ] Describe what happens between writing Java code and running it
- [ ] Write, compile, and run a Java program from the command line
- [ ] Declare and use variables of all primitive types
- [ ] Perform arithmetic, comparison, and logical operations
- [ ] Understand operator precedence and write clear expressions

---

## Module Structure

```
01-foundations/
├── README.md                     ← You are here
├── theory/
│   ├── 01-how-computers-work.md  ← Start here if you are new to programming
│   ├── 02-what-is-java.md        ← Java's history, philosophy, and use today
│   ├── 03-jvm-jdk-jre-explained.md ← The three acronyms demystified
│   └── 04-how-java-runs.md       ← The full lifecycle of a Java program
├── code/
│   ├── HelloWorld.java           ← Your first Java program
│   ├── DataTypesDemo.java        ← All primitive types with explanations
│   ├── OperatorsDemo.java        ← Arithmetic, comparison, logical operators
│   └── README.md                 ← How to compile and run the examples
├── exercises/
│   ├── README.md                 ← Exercise instructions and tips
│   ├── Exercise01_HelloWorld.java
│   ├── Exercise02_Variables.java
│   ├── Exercise03_Calculator.java
│   └── solutions/
│       ├── Exercise01_Solution.java
│       ├── Exercise02_Solution.java
│       └── Exercise03_Solution.java
└── mini-project/
    ├── README.md                 ← Project brief and requirements
    └── PersonalInfoCard.java     ← Your first complete program
```

---

## Recommended Order

1. Read `theory/01-how-computers-work.md`
2. Read `theory/02-what-is-java.md`
3. Read `theory/03-jvm-jdk-jre-explained.md`
4. Read `theory/04-how-java-runs.md`
5. Study and run `code/HelloWorld.java`
6. Study and run `code/DataTypesDemo.java`
7. Study and run `code/OperatorsDemo.java`
8. Attempt `exercises/Exercise01_HelloWorld.java`
9. Attempt `exercises/Exercise02_Variables.java`
10. Attempt `exercises/Exercise03_Calculator.java`
11. Complete the mini-project: `mini-project/PersonalInfoCard.java`

---

## Key Concepts in This Module

### Variables and Data Types
Java is *statically typed* — you must declare what type of data a variable holds before using it. This is different from languages like Python or JavaScript. It feels restrictive at first but prevents entire classes of bugs.

### Primitive Types
Java has 8 primitive types built into the language: `byte`, `short`, `int`, `long`, `float`, `double`, `boolean`, `char`. These are the atomic building blocks. Everything else is an object.

### Operators
Java operators follow mathematical precedence rules: multiplication before addition, parentheses override everything. Understanding this prevents subtle bugs.

---

## Before You Start

Make sure your environment is set up. From your terminal, run:

```bash
java --version
javac --version
```

Both should show version 21 or higher. If they do not, go back to the main `README.md` and follow the setup instructions.

---

*When you are ready, open `theory/01-how-computers-work.md` and begin.*
