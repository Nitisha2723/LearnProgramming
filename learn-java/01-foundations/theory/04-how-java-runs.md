# How Java Runs

You have learned what a computer is, what Java is, and what the JVM, JRE, and JDK are. Now let's trace the complete journey of a Java program — from the moment you write it to the moment it produces output.

---

## The Complete Lifecycle

```
 +------------------+
 |  You write       |
 |  HelloWorld.java |   ← Plain text, human-readable Java source code
 +--------+---------+
          |
          |  javac HelloWorld.java
          |  (Java compiler in the JDK)
          v
 +------------------+
 |  HelloWorld.class|   ← Binary bytecode (platform-neutral)
 +--------+---------+
          |
          |  java HelloWorld
          |  (JVM launcher in the JDK)
          v
 +------------------+
 |  JVM             |   ← Loads the .class file
 |   |              |
 |   v              |
 |  Class Loader    |   ← Finds and loads the bytecode
 |   |              |
 |   v              |
 |  Bytecode        |
 |  Verifier        |   ← Checks the bytecode is safe to run
 |   |              |
 |   v              |
 |  Execution       |
 |  Engine          |   ← Interprets bytecode AND runs JIT compilation
 |   |              |
 |   v              |
 |  Output          |   ← "Hello, World!" appears in your terminal
 +------------------+
```

Let's walk through each step.

---

## Step 1: You Write Source Code

You create a file called `HelloWorld.java`. The `.java` extension tells the compiler this is a Java source file. The filename (before the extension) must match the public class name inside the file.

The source file is plain text. Any text editor can open it. It contains Java code that is readable by humans.

---

## Step 2: javac Compiles to Bytecode

You run:
```bash
javac HelloWorld.java
```

The `javac` compiler:
1. Parses your `.java` file (reads its structure)
2. Checks for syntax errors (malformed statements, unclosed braces)
3. Checks for type errors (assigning a number to a String variable, etc.)
4. If no errors: produces `HelloWorld.class` containing bytecode

If there are errors, `javac` reports them with line numbers and you fix them before a `.class` file is produced.

**The compiler enforces rules you cannot break.** This is one of Java's great strengths — a large class of mistakes is caught before your program ever runs.

### What Is in a .class File?

A `.class` file contains:
- **Bytecode**: the instructions for the JVM
- **Constant pool**: literal values used in the program (strings, numbers)
- **Metadata**: the class name, its parent class, implemented interfaces
- **Debugging information** (optional): line numbers, variable names for stack traces

The `.class` file is binary (not human-readable), but it is compact and platform-neutral. The exact same `HelloWorld.class` file runs on Windows, macOS, Linux, and Android.

---

## Step 3: java Launches the JVM

You run:
```bash
java HelloWorld
```

(Note: no `.class` extension — just the class name.)

This launches the JVM (`java` is the JVM launcher) and tells it which class to start with. The JVM then begins the process of loading and running your program.

---

## Step 4: The Class Loader

The **Class Loader** is the first component of the JVM that acts on your program. Its job is to find `.class` files and load them into memory.

When you run `java HelloWorld`, the Class Loader:
1. Finds `HelloWorld.class` (looks in the current directory and any configured classpath)
2. Reads the bytecode into memory
3. Makes the class available for execution

The Class Loader also loads the standard library classes your program uses. When `HelloWorld` calls `System.out.println()`, the Class Loader finds and loads `java.lang.System` (which is part of the JRE).

Classes are loaded *lazily* — only when they are first needed. Your entire program is not loaded into memory upfront; classes are loaded on demand as the program executes.

---

## Step 5: Bytecode Verification

Before executing any bytecode, the JVM's **Bytecode Verifier** performs a security and consistency check.

The verifier checks that:
- The bytecode is structurally valid (not malformed)
- No illegal type conversions occur
- Array access is within bounds
- No undefined variables are accessed
- The code doesn't try to do things the security model forbids

This step exists partly for security: if you download a Java program from the internet, you want the JVM to verify it won't do dangerous things with memory. This is why Java has historically been more resistant to certain types of exploits than languages without this check.

---

## Step 6: The Execution Engine (Where Your Code Actually Runs)

The **Execution Engine** is where bytecode is actually executed. It has two modes that work together:

### The Interpreter

When the JVM first starts executing your code, it uses an **interpreter** — it reads bytecode instructions one at a time and executes them. This is straightforward and starts immediately.

The interpreter is not as fast as native machine code execution, but it starts quickly (no warm-up time) and is fine for code that runs only occasionally.

### The Just-In-Time (JIT) Compiler

The JVM monitors which parts of your code execute frequently — these are called **hot spots**. (HotSpot is actually the name of the most widely used JVM implementation, named after this feature.)

When the JVM identifies a hot spot, it uses the **JIT compiler** to compile that bytecode directly to native machine code for the current hardware. After JIT compilation:
- The native machine code is cached
- Future calls to that code run at native speed
- No further interpretation overhead

**This is why Java can be as fast as C in many workloads.** After the JVM has had time to JIT-compile the hot code paths (the "warm-up" period), Java code runs at native machine code speed.

```
Execution timeline:

Program starts
   |
   v
[Interpretation phase]         ← Starts immediately, somewhat slow
   |
   | JVM identifies hot spots
   v
[JIT compilation begins]       ← Compiles frequently-run code to native
   |
   | JIT compilation completes
   v
[Native execution of hot code] ← Runs at full native speed
```

For short-lived programs (a command-line script that runs for a second), the JIT warm-up may be longer than the actual runtime. For long-lived server applications (a web server running for days), the JIT compilation pays off enormously.

---

## Memory Model: Stack vs Heap

Understanding where Java stores data is fundamental to understanding how programs work.

### The Stack

The **stack** stores:
- Local variables (variables declared inside a method)
- Method call information (what method called what, and where to return)

The stack is a *last-in, first-out* structure. When you call a method, a new *stack frame* is pushed onto the stack. When the method returns, its frame is popped off. Variables in the popped frame are immediately gone.

```java
void method1() {
    int x = 10;           // x lives on the stack
    method2();            // a new frame is pushed for method2
    // method2 returns, its frame popped, its variables gone
    // x is still here
}

void method2() {
    int y = 20;           // y lives on the stack (in method2's frame)
    // when method2 returns, y is gone
}
```

**The stack is fast.** Allocating and deallocating stack memory is just incrementing or decrementing a pointer — essentially instantaneous.

**The stack is limited.** If you recurse too deeply (calling methods that call methods that call methods...), you can overflow the stack. Java throws a `StackOverflowError` if this happens.

### The Heap

The **heap** stores:
- Objects (instances of classes, including String and arrays)

When you create an object with `new`, memory is allocated on the heap:

```java
String name = new String("Alice");   // "Alice" object lives on the heap
                                     // 'name' variable lives on the stack
                                     // 'name' holds a reference (address) to the heap object
```

Objects on the heap persist as long as something references them. When nothing references an object anymore, the garbage collector can reclaim its memory.

**The heap is larger but slower to allocate than the stack.** The garbage collector periodically pauses to reclaim unreachable objects.

---

## Garbage Collection: Java Manages Memory For You

In C and C++, when you allocate memory (with `malloc` or `new`), you are responsible for freeing it when done. Forgetting to free memory causes **memory leaks** — the program gradually consumes more and more memory until it crashes or slows to a crawl. Freeing memory twice causes crashes. These bugs are notoriously hard to find.

Java eliminates this entire category of bugs with **automatic garbage collection**.

The **Garbage Collector (GC)** runs in the background, periodically scanning the heap to find objects that are no longer reachable from any live code. These unreachable objects are "garbage" — your program can never access them again. The GC reclaims their memory.

```java
public void doSomething() {
    String temp = "this is temporary";   // object allocated on heap
    // ... use temp ...
}   // method ends: 'temp' variable is gone from the stack
    // the String object has no more references
    // it is now eligible for garbage collection
    // the GC will reclaim this memory at some future point
```

**You don't need to free memory.** Java's GC handles it. This is an enormous productivity advantage.

**GC has a cost.** The GC periodically pauses your program briefly to do its work. For most applications, these pauses are imperceptible. For extreme latency-sensitive applications (e.g., high-frequency trading), GC tuning is important. Java 21 includes significantly improved GC algorithms (ZGC, G1GC) that minimize pause times.

---

## Putting It All Together: HelloWorld's Journey

Let's trace exactly what happens when you run `java HelloWorld`:

1. `java HelloWorld` invokes the JVM launcher
2. JVM starts, allocates memory for the heap and main thread's stack
3. The Bootstrap Class Loader loads `java.lang` and other core classes
4. The Application Class Loader finds and loads `HelloWorld.class`
5. The Bytecode Verifier validates `HelloWorld.class`
6. The JVM looks for `public static void main(String[] args)` in `HelloWorld`
7. A stack frame for `main` is pushed onto the main thread's stack
8. The Execution Engine begins interpreting the bytecode of `main`:
   a. Processes `System.out.println("Hello, World!")`:
      - Looks up `System` (already loaded in step 2)
      - Accesses its `out` field (a `PrintStream` object)
      - Calls `println` on it
      - This eventually calls native OS code to write to stdout
      - `"Hello, World!"` appears in your terminal
9. `main` returns; its stack frame is popped
10. The main thread exits; the JVM shuts down

---

## Summary

| Step | What Happens |
|------|-------------|
| Write `.java` | Human-readable source code |
| `javac` compiles | Produces platform-neutral bytecode (`.class` file) |
| `java` launches | JVM starts; Class Loader loads `.class` files |
| Verification | Bytecode Verifier checks safety and validity |
| Interpretation | Execution Engine interprets bytecode (starts immediately) |
| JIT compilation | Hot code compiled to native machine code (after warm-up) |
| Stack | Stores local variables and method call frames (fast, limited) |
| Heap | Stores objects (larger, GC-managed) |
| GC | Automatically reclaims heap memory from unreachable objects |

---

*You are now ready to write and run Java code. Open `code/HelloWorld.java` and follow the README in that directory.*
