# How Computers Work

Before writing a single line of code, it helps enormously to understand what a computer actually is and what it does. This is not a deep computer science lecture — it is the essential mental model you need to understand programming.

---

## What Is a Computer?

A computer is a machine that can follow instructions. That is the entire definition. Everything else — the browser you use, the games you play, the apps on your phone — is built on top of that simple idea.

Modern computers have three main components you need to understand:

### The CPU (Central Processing Unit)

The CPU is the brain of the computer. It executes instructions — arithmetic operations, comparisons, moving data around — billions of times per second.

**Analogy:** Think of the CPU as a chef. The chef can chop, stir, fry, and plate — but the chef can only work on one thing at a time, and only works with what is in arm's reach on the counter.

The CPU works with a very limited set of operations: add two numbers, compare two values, jump to a different instruction, read from memory, write to memory. That is basically it. Everything you have ever done on a computer — every video you have watched, every message you have sent — is built from billions of these tiny operations.

### Memory (RAM)

Memory (Random Access Memory) is where the computer keeps data it is currently working with. It is fast but temporary — when you turn off the computer, everything in RAM disappears.

**Analogy:** Memory is the chef's counter space — the workspace where ingredients are laid out right now. The chef can only work with what is on the counter. If you need something, you grab it from storage and put it on the counter. When you are done, you clear the counter.

When you run a program, the program is loaded from storage (your hard drive) into memory so the CPU can access it quickly.

### Storage (Hard Drive / SSD)

Storage is where data lives permanently — your files, your programs, your photos. It is much larger than memory but also much slower.

**Analogy:** Storage is the pantry. Everything is organized and stored there long-term, but you have to walk to the pantry, find what you need, and bring it to the counter before you can use it.

---

## What Is a Program?

A program is a sequence of instructions that tells the CPU what to do. That is all it is.

When you open a web browser, you are running a program — a very large sequence of instructions that tells the CPU: "listen for keyboard input, send network requests, display pixels on screen, handle clicks..."

When you write Python code, you are writing instructions for the computer to follow.

Here is a simple analogy: imagine you want to bake a cake. The recipe is a program. The steps in the recipe are instructions. You (the chef) are the CPU. You follow the instructions one at a time, in order, and the result is a cake.

A program is exactly like a recipe:
- It has steps that happen in order
- Some steps depend on the results of previous steps
- Some steps happen repeatedly (stir for 2 minutes)
- Some steps are conditional (if the oven is hot enough, then...)

---

## Source Code vs. Machine Code

The CPU does not understand Python, English, or any human-readable language. The CPU only understands **machine code** — binary instructions that look like this:

```
01001000 10000011 11101100 00101000
01001000 10001001 01001101 11111000
```

Nobody writes machine code directly (almost nobody). Instead, we write **source code** — human-readable instructions in a programming language like Python, Java, or C.

**Source code** is what you write. It looks like this:

```python
name = "Alice"
print(f"Hello, {name}!")
```

**Machine code** is what the CPU actually runs. It is the translated version of your source code.

Something has to translate between the two. That "something" is either a **compiler** or an **interpreter**.

---

## Compiler vs. Interpreter

### Compiler

A compiler reads all of your source code, translates the entire thing into machine code, and produces an executable file. The translation happens once, before the program runs. After compilation, you have a standalone program that runs directly on the CPU.

Examples of compiled languages: C, C++, Rust, Go.

**Analogy:** A compiler is like a professional translator who translates an entire book from French to English before the book is published. Once translated, anyone can read the English version — no translator needed.

Advantages: Very fast execution (no translation overhead at runtime). The final program runs directly on hardware.

Disadvantages: You have to compile before every run. A compiled program for Windows will not run on Mac (different machine code).

### Interpreter

An interpreter reads your source code one line (or one statement) at a time and executes it immediately. There is no separate compilation step — the interpreter translates and runs simultaneously.

Examples of interpreted languages: Python (mostly), Ruby, early JavaScript.

**Analogy:** An interpreter is like a live translator at a business meeting. As the speaker talks, the translator immediately converts each sentence into the other language. No pre-translation needed, but the translator must be present the whole time.

Advantages: Immediate execution. Cross-platform (run the same Python file on any operating system that has Python installed). Interactive mode (the REPL — you will use this constantly).

Disadvantages: Slower than compiled code (translation overhead at runtime). Requires the interpreter to be installed.

---

## The Reality: It Is More Complicated

In practice, the line between "compiled" and "interpreted" is blurry. Python actually does both:

1. Python reads your `.py` file and compiles it to an intermediate format called **bytecode** (stored in `.pyc` files)
2. The Python **virtual machine** (the interpreter) then reads and executes the bytecode

This is similar to how Java works (Java source → bytecode → JVM). We will cover this in detail in `04-how-python-runs.md`.

---

## Why This Matters for Python

Understanding this mental model helps you understand:

**Why Python programs start by saying `python3 filename.py`** — you are launching the Python interpreter and telling it which file to execute.

**Why Python is "cross-platform"** — your `.py` file contains source code, not machine code. The Python interpreter on any operating system can run the same file.

**Why Python is slower than C** — C code is compiled directly to machine code and runs natively. Python code goes through an interpreter layer. The interpreter adds overhead.

**Why Python errors happen at runtime** — unlike a compiler that checks for errors before running anything, the Python interpreter discovers errors as it executes, line by line. This is why you can have a bug in line 100 of your code but not discover it until you hit that line.

**Why the REPL works** — because Python is interpreted, you can type a single line of Python and see the result immediately. There is nothing to "compile first."

---

## Summary

| Concept | Simple Explanation |
|---------|-------------------|
| CPU | Executes instructions, billions per second |
| Memory (RAM) | Temporary workspace for the running program |
| Storage | Long-term home for files and programs |
| Program | A sequence of instructions for the CPU |
| Source code | Human-readable program text (what you write) |
| Machine code | CPU-readable instructions (what runs) |
| Compiler | Translates all source code at once, before running |
| Interpreter | Translates and runs source code line by line |
| Python | Interpreted language (with a bytecode compilation step) |

---

## Next

Read `02-what-is-python.md` to learn specifically about Python — its history, design, and why it became the world's most popular programming language.
