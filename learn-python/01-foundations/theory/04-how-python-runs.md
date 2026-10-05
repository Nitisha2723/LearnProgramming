# How Python Runs Your Code

You write a `.py` file. You type `python3 myfile.py`. Your program runs. But what actually happens between those two moments? This file traces the entire journey from your source code to executed instructions.

---

## The Journey: From .py to Output

Let's trace exactly what happens when you run this program:

```python
name = "Alice"
greeting = "Hello, " + name
print(greeting)
```

### Step 1: You Run the File

```bash
python3 hello.py
```

This launches the CPython interpreter and tells it to process `hello.py`.

### Step 2: Lexical Analysis (Tokenization)

The interpreter reads your file as raw text and breaks it into **tokens** — the smallest meaningful units of Python code.

`name = "Alice"` becomes tokens:
- `name` (NAME token)
- `=` (EQUAL token)
- `"Alice"` (STRING token)

This is similar to how you read English: you recognize individual words before understanding sentences.

### Step 3: Parsing (Building the AST)

The tokens are assembled into an **Abstract Syntax Tree (AST)** — a tree-shaped data structure that represents the structure of your program.

The assignment `name = "Alice"` becomes a tree node of type "Assign" with:
- A target: the name `name`
- A value: the string literal `"Alice"`

The AST represents the logical structure of your code — what does what, in what order — independent of the original text.

### Step 4: Compilation to Bytecode

Python compiles the AST into **bytecode** — a series of simple instructions for the Python Virtual Machine.

Bytecode is not machine code. It is not specific to Windows, macOS, or Linux. It is a compact, portable set of instructions that the Python VM knows how to execute.

You can see bytecode with the `dis` module (try this in the REPL):

```python
import dis
dis.dis("""
name = "Alice"
greeting = "Hello, " + name
print(greeting)
""")
```

You will see output like:
```
  1           0 LOAD_CONST               0 ('Alice')
              2 STORE_NAME               0 (name)
  ...
```

Do not worry about reading bytecode — this is just to show you it exists.

### Step 5: Bytecode Caching (.pyc Files)

Python saves the bytecode to a `.pyc` file inside a `__pycache__` directory. Next time you run the same file (if it has not changed), Python skips steps 2–4 and loads the cached bytecode directly.

This is why you see `__pycache__/` directories in Python projects. They are safe to delete (Python will regenerate them) and should be in `.gitignore` (which ours is).

### Step 6: The Python Virtual Machine Executes Bytecode

The Python Virtual Machine (PVM) reads the bytecode instructions one at a time and executes them.

```
LOAD_CONST 'Alice'  →  push the string "Alice" onto the stack
STORE_NAME name     →  pop the top of the stack and assign it to "name"
```

The PVM uses a **stack-based architecture** — values are pushed onto and popped off a stack as instructions execute. You do not need to understand the details, but knowing this helps explain why Python's execution model is different from hardware CPUs (which use registers).

### The Output

After executing all the bytecode, `print(greeting)` sends "Hello, Alice" to standard output, and you see it in your terminal.

---

## The Full Picture

```
hello.py (source code)
    ↓
[Lexer] tokenizes into words and symbols
    ↓
[Parser] builds Abstract Syntax Tree
    ↓
[Compiler] generates bytecode
    ↓
__pycache__/hello.cpython-312.pyc (bytecode cache)
    ↓
[Python Virtual Machine] executes bytecode
    ↓
Output: "Hello, Alice"
```

---

## The GIL (Global Interpreter Lock)

The GIL is one of Python's most discussed features. You do not need to fully understand it as a beginner, but you should know it exists.

**What it is:** The GIL is a mutex (mutual exclusion lock) inside CPython that ensures only one thread executes Python bytecode at a time.

**What it means in plain terms:** Even if you write Python code that uses multiple threads, only one thread runs Python code at any given moment. CPython's threads take turns — they do not run truly simultaneously on multiple CPU cores.

**Why it exists:** Python's memory management (reference counting — described below) is not thread-safe without the GIL. Removing the GIL is a major ongoing engineering effort in the Python core team.

**Why it matters (and why it does not):**
- For CPU-bound tasks (calculations, data processing) — threading in Python does not give you true parallelism. Use `multiprocessing` instead.
- For I/O-bound tasks (network requests, file operations) — the GIL is released while waiting for I/O, so threads do help.
- For most beginner and intermediate code — you will not encounter this issue.

Python 3.12 introduced experimental support for running Python without the GIL (the "no-GIL" build). This is an active area of development and will eventually allow true multi-threaded Python programs.

---

## Memory Management

### Reference Counting

Python keeps track of how many variables point to each object in memory. This count is called the **reference count**.

```python
name = "Alice"     # "Alice" string object has reference count 1
alias = name       # reference count is now 2
name = None        # reference count drops back to 1
alias = None       # reference count drops to 0 — object is freed
```

When an object's reference count reaches zero, Python immediately frees the memory. This is efficient — memory is reclaimed as soon as it is no longer needed.

### Garbage Collection

Reference counting has a weakness: **circular references**.

```python
a = {}
b = {}
a['b'] = b    # a references b
b['a'] = a    # b references a
# Now we delete both variables
del a
del b
# The objects still reference each other! Reference count never reaches 0.
# They can never be freed by reference counting alone.
```

Python's **garbage collector** handles this case. It periodically scans for groups of objects that reference each other but are not reachable from any variable. It frees them.

As a Python programmer, you almost never think about memory management. Python handles it. But knowing it exists helps you understand:
- Why Python uses more memory than C (the reference counting overhead)
- Why there is occasional pause in long-running Python programs (garbage collection cycle)
- Why not creating unnecessary objects is a good habit

---

## Why Python Is "Slow" But Used Everywhere

Python is significantly slower than C, C++, or Rust for raw computation. A Python program doing mathematical calculations might run 50–100x slower than equivalent C code.

Yet Python is the most popular language in the world. Why?

### Developer Time > CPU Time

In most software projects, the bottleneck is the programmer's time, not the computer's speed. Python developers write correct, working code faster than developers in C++ or Java. A Python program that takes 2 minutes to run but took 2 days to write is often better than a C++ program that takes 2 seconds to run but took 2 weeks to write.

### The Slow Parts Can Be Replaced

The Python ecosystem heavily uses **C extensions** for performance-critical code. NumPy, pandas, and many other libraries do their heavy computation in optimized C code. You write Python to orchestrate the work; C does the heavy lifting. This is the best of both worlds.

```python
import numpy as np
# This runs at C speed, not Python speed
result = np.sum(np.arange(1_000_000))
```

### Python Is Fast Enough for Most Work

Web servers? Fast enough — the bottleneck is the database, not Python. Data pipelines? Use pandas, which is C underneath. APIs? FastAPI can handle tens of thousands of requests per second. Scripting and automation? Python is near-instant.

Only when you need raw numerical performance (game engines, real-time signal processing, embedded systems) does Python's speed become a real limitation. And even then, Python is often used as the glue language controlling performance-critical components written in faster languages.

### Modern Python Is Getting Faster

Python 3.11 was ~25% faster than Python 3.10. Python 3.12 added more improvements. The PyPy interpreter runs many programs 5–10x faster than CPython. Python is actively improving its performance, though it will never match compiled languages for raw computation.

---

## Putting It All Together

Now you understand the complete picture:

1. **You write** Python source code in a `.py` file
2. **Python compiles** it to bytecode (and caches in `__pycache__/`)
3. **The Python VM** executes the bytecode line by line
4. **Memory** is managed automatically via reference counting and garbage collection
5. **The GIL** ensures thread safety but limits parallelism
6. **Performance tradeoffs** are real but manageable — Python's productivity advantage usually wins

As you continue through this repository, you will understand more and more of what happens "under the hood." For now, focus on writing clear, readable Python code. The rest will come with experience.

---

## A Note on Line-by-Line Execution

Because Python executes code sequentially and errors are discovered at runtime, a Python program can run partway and then fail:

```python
print("Line 1 runs fine")        # This executes
print("Line 2 runs fine")        # This executes
x = 1 / 0                        # ZeroDivisionError! Execution stops here.
print("This never runs")          # Never reached
```

This is different from a compiled language where the compiler catches many errors before the program runs. In Python, you find runtime errors by... running the code.

This is why testing is important in Python — you want to exercise your code paths to find errors before your users do. We cover testing in Module 02 and beyond.

---

## Summary

| Step | What Happens |
|------|-------------|
| You write `.py` | Source code — human-readable Python text |
| Lexer | Breaks source into tokens |
| Parser | Builds Abstract Syntax Tree |
| Compiler | Generates bytecode |
| `__pycache__/` | Bytecode cached for next run |
| Python VM | Executes bytecode instructions |
| Reference counting | Frees memory when count reaches 0 |
| Garbage collector | Handles circular references |
| GIL | Ensures one thread runs at a time |

---

## Next Steps

Now that you understand the foundation, it is time to write code. Open `../code/README.md` and work through the code examples, starting with `hello_world.py`.
