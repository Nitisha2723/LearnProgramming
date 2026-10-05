# How Computers Work

Before you write a single line of Java, it helps enormously to understand what a computer actually does. This is not deep theory — it is a plain-English mental model that will make every programming concept click faster.

---

## What Is a Computer?

A computer is a machine that follows instructions very, very fast.

That is it. The impressive things computers do — rendering video games, running websites, translating languages — all come down to billions of simple instructions executed in sequence, incredibly quickly.

The key components you need to understand:

### The CPU (Central Processing Unit)

Think of the CPU as the computer's worker. It reads an instruction, does what the instruction says, and moves on to the next one. A modern CPU can do this billions of times per second.

The CPU can only do a small set of things:
- Move numbers from one place to another
- Add two numbers together
- Compare two numbers
- Jump to a different instruction based on a comparison

That is essentially it. Everything your computer does — playing music, loading a web page, running a game — is built from these primitives.

**Analogy:** The CPU is like a chef in a kitchen. It follows a recipe (your program) one step at a time. It doesn't think creatively or skip steps. It just executes instructions as fast as possible.

### Memory (RAM — Random Access Memory)

Memory is where the computer stores things it is currently working with.

Think of memory as the chef's workbench. While cooking, the chef keeps the current ingredients and tools right in front of them. It is fast to access, but it only holds what is needed right now. When you turn off the computer, everything in RAM disappears.

Memory is measured in gigabytes (GB). Your laptop might have 8 GB, 16 GB, or 32 GB. More memory means the computer can work on more things simultaneously.

**Key property:** Memory is fast but temporary.

### Storage (SSD or Hard Drive)

Storage is where the computer keeps things permanently — files, programs, photos, documents.

Think of storage as a filing cabinet. The chef is not constantly rummaging through the filing cabinet while cooking; they get what they need at the start, then work from the workbench. When the meal is done, they put the recipe back in the filing cabinet.

Storage is much slower than RAM, but it keeps its contents when the power is off.

**Key property:** Storage is slow but permanent.

### How They Work Together

When you open a Java program:
1. The program's code is **read from storage** (slow — happens once)
2. The code is **loaded into memory** (fast — now the CPU can reach it)
3. The **CPU reads and executes** instructions from memory (very fast — repeated billions of times)

---

## What Is a Program?

A program is a list of instructions for the CPU to follow.

But here is the catch: the CPU only understands *machine code* — binary numbers representing specific operations on specific hardware. Writing programs directly as machine code is possible but enormously difficult and completely non-portable (code written for one type of CPU won't work on another).

---

## Source Code vs Machine Code

**Source code** is the human-readable form of a program. It is what you write. It looks like this:

```java
int age = 25;
System.out.println("I am " + age + " years old.");
```

Source code uses words and structures that make sense to humans. It is clear, editable, and portable.

**Machine code** is what the CPU actually executes. The same instructions above, in machine code, would look something like this:

```
10110000 01100101 11110100 10000011
00111000 01100010 11000000 10111000
...
```

(This is a simplified illustration — real machine code depends on the CPU architecture.)

No human writes or reads machine code directly. We write source code, and then use a tool to translate it.

---

## What Is a Compiler?

A **compiler** is a program that reads your source code and translates the whole thing into machine code (or an intermediate form) before you run it.

Think of it like a human translator working on a book. The translator reads the entire foreign-language book, produces a complete English translation, and gives you the finished translation. You then read the English version — no translator needed while you read.

A compiler:
- Reads your entire source file
- Checks for errors (syntax mistakes, type errors)
- Produces output code (machine code or bytecode)
- You then run the output (the compiler is not involved anymore)

**Examples of compiled languages:** C, C++, Rust, Go.

---

## What Is an Interpreter?

An **interpreter** is a program that reads your source code and executes it line by line at runtime — on the fly, while your program is running.

Think of a human simultaneous interpreter at a conference. They listen to the speaker and immediately translate each sentence as it is spoken. No pre-translated document exists; the interpretation happens in real time.

An interpreter:
- Reads one statement of your source code
- Executes that statement immediately
- Reads the next statement, executes it, and so on

**Examples of interpreted languages:** Python (roughly), Ruby, older JavaScript.

**Trade-off:** Interpreted programs are often slower than compiled ones, because the translation overhead happens every time the program runs. Compiled programs pay the translation cost once.

---

## The Fetch-Decode-Execute Cycle

Here is the simple mental model for how a CPU runs a program:

```
    +---------------+
    |   FETCH       |  ← CPU reads the next instruction from memory
    +-------+-------+
            |
            v
    +---------------+
    |   DECODE      |  ← CPU figures out what the instruction means
    +-------+-------+
            |
            v
    +---------------+
    |   EXECUTE     |  ← CPU carries out the instruction
    +-------+-------+
            |
            +-------→  back to FETCH (next instruction)
```

**Fetch:** The CPU reads the next instruction from memory. Instructions are stored sequentially, so it knows where to look.

**Decode:** The CPU decodes the instruction — figures out what operation it represents (add two numbers, move data, compare values, etc.).

**Execute:** The CPU carries out the operation.

Then the cycle repeats, billions of times per second, until the program finishes or is stopped.

**Why does this matter for you?**

When you write code like `int x = 5 + 3;`, you eventually need this to become fetch-decode-execute cycles the CPU can follow. Understanding this helps you appreciate why:
- Type systems exist (the CPU needs to know how many bytes to allocate)
- Some operations are slower than others (more fetch-decode-execute cycles)
- Memory matters (data the CPU can't reach quickly causes delays)

---

## Why This Matters for Java

Java sits in an interesting position between compiled and interpreted:

1. You write Java **source code** (`.java` files)
2. The Java compiler (`javac`) compiles it to **bytecode** (`.class` files) — not machine code, but a platform-neutral intermediate form
3. The **JVM** (Java Virtual Machine) runs the bytecode, acting as an interpreter AND also doing Just-In-Time compilation to actual machine code for performance

This design is how Java achieves the famous "Write Once, Run Anywhere" promise: the bytecode is the same regardless of whether you're on Windows, macOS, or Linux. The JVM handles the final translation to each platform's machine code.

You will explore this more in `03-jvm-jdk-jre-explained.md` and `04-how-java-runs.md`.

---

## Summary

| Concept | Plain English |
|---------|---------------|
| CPU | The worker that executes instructions, billions of times per second |
| Memory (RAM) | Fast, temporary workspace — lost when power off |
| Storage | Slow, permanent — survives power off |
| Source code | Human-readable program text (what you write) |
| Machine code | CPU-executable binary instructions |
| Compiler | Translates entire source to machine/bytecode before running |
| Interpreter | Translates and runs source line by line at runtime |
| Fetch-Decode-Execute | The CPU's core loop: get instruction, understand it, do it |

---

*Next: `02-what-is-java.md` — the history and design philosophy of Java.*
