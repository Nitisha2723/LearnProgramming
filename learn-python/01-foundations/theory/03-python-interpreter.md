# The Python Interpreter

You have Python installed. But what exactly is "Python"? When you type `python3`, what are you actually running? This file answers that question — and introduces the most useful tool in a Python developer's toolkit: the REPL.

---

## Compiler vs. Interpreter: A Deeper Look

We covered the basics in `01-how-computers-work.md`. Let's go deeper now that you have context.

### How a Compiler Works

A compiler takes your entire source code file and transforms it into machine code all at once, before the program runs. The result is an executable (a `.exe` on Windows, or an ELF binary on Linux).

Process:
1. Write `hello.c`
2. Run the compiler: `gcc hello.c -o hello`
3. The compiler reads the whole file, checks for errors, and produces a binary
4. Run the binary: `./hello`
5. The CPU runs the binary directly — no compiler needed at runtime

The compiled binary is specific to an operating system and CPU architecture. A binary compiled for Windows will not run on macOS.

### How an Interpreter Works

An interpreter takes your source code and executes it directly, without producing a separate binary file. The interpreter is the thing running your program.

Process:
1. Write `hello.py`
2. Run the interpreter: `python3 hello.py`
3. Python reads your code, translates it, and executes it simultaneously
4. The Python interpreter is present the entire time the program runs

The same `hello.py` file runs on any operating system that has Python installed. The interpreter handles the platform differences.

### The Key Difference in Practice

With a compiler: you must compile before every run. Errors are caught before execution. The program runs at full hardware speed.

With an interpreter: you run immediately. Some errors only appear at runtime. The program runs somewhat slower (the interpreter adds overhead).

---

## CPython: The Official Python Interpreter

When you install Python from python.org, you get **CPython** — the reference implementation of Python, written in C.

"Reference implementation" means it is the official version that defines what Python is. When people say "Python," they usually mean CPython.

There are other Python implementations:
- **PyPy** — Python written in Python, with a JIT (Just-In-Time) compiler. Often 5–10x faster than CPython for certain workloads.
- **Jython** — Python running on the Java Virtual Machine
- **IronPython** — Python for the .NET runtime
- **MicroPython** — Python for microcontrollers

But for learning Python, use CPython. It is what 95%+ of the Python ecosystem targets.

### What CPython Actually Does

Here is what actually happens when you run `python3 hello.py`:

1. Python reads your `.py` file (source code)
2. Lexer: tokenizes the text (breaks it into meaningful pieces: keywords, names, operators)
3. Parser: builds an Abstract Syntax Tree (AST) — a tree representation of your program's structure
4. Compiler: converts the AST to **bytecode** (not machine code, but a lower-level intermediate form)
5. The Python Virtual Machine (PVM): executes the bytecode instruction by instruction

The bytecode is stored in `.pyc` files inside a `__pycache__` directory. If you run the same file again, Python can skip steps 1–4 and go straight to executing the cached bytecode (if the source file has not changed).

You do not need to manage any of this — it happens automatically. But understanding it helps you understand why Python is "slower" than C, and why `.pyc` files appear in your project.

---

## The REPL: Your Interactive Playground

The REPL stands for **Read-Eval-Print Loop**. It is an interactive environment where you type Python expressions and see the result immediately.

**Read** — Python reads your input  
**Eval** — Python evaluates (executes) it  
**Print** — Python prints the result  
**Loop** — And then waits for your next input  

The REPL is one of the most powerful tools you have. Any time you want to try something quickly, test an idea, or understand how something works — use the REPL.

### Starting the REPL

```bash
python3
```

You will see something like:
```
Python 3.12.0 (main, Oct  2 2023, 15:03:37) [Clang 15.0.0 (clang-1500.0.40.1)] on darwin
Type "help", "copyright", "credits" or "license" for more information.
>>>
```

The `>>>` is the REPL prompt. Python is waiting for you.

### Using the REPL

Type Python code and press Enter:

```python
>>> 2 + 2
4
>>> "hello"
'hello'
>>> 10 * 7
70
```

When you type an expression, Python evaluates it and prints the result automatically. You do not need `print()` in the REPL — it prints everything.

```python
>>> name = "Alice"
>>> name
'Alice'
>>> print(name)
Alice
```

Note the difference: `name` gives you the repr (representation with quotes), `print(name)` gives you the value without quotes.

### Multi-line code in the REPL

When Python is expecting more code (inside an if block, function, etc.), the prompt changes to `...`:

```python
>>> for i in range(3):
...     print(i)
...
0
1
2
>>>
```

After the loop body, press Enter on a blank line to execute.

### Getting Help in the REPL

```python
>>> help(print)     # Shows documentation for the print function
>>> help(str)       # Shows documentation for the str type
>>> type(42)        # Shows the type of a value
<class 'int'>
>>> dir(str)        # Shows all methods available on str
```

### Exiting the REPL

```python
>>> exit()
# or
>>> quit()
# or press Ctrl+D on macOS/Linux
# or press Ctrl+Z then Enter on Windows
```

### REPL Examples — Try These

Open the REPL and type these in order:

```python
# Arithmetic
>>> 100 / 7
14.285714285714286
>>> 100 // 7
14
>>> 100 % 7
2

# Strings
>>> "hello" + " " + "world"
'hello world'
>>> "ha" * 3
'hahaha'
>>> len("Python")
6

# Variables
>>> x = 10
>>> y = 3
>>> x + y
13
>>> x ** y
1000

# Type checking
>>> type(42)
<class 'int'>
>>> type(3.14)
<class 'float'>
>>> type("hello")
<class 'str'>
>>> type(True)
<class 'bool'>
```

---

## .py Files vs. Interactive Mode

### When to use the REPL

- Experimenting with a new concept
- Testing a small piece of code quickly
- Checking what a function does
- Exploring a library you have just installed

### When to use .py files

- Writing programs that need to run again
- Writing more than a few lines of code
- Code that needs to be saved, shared, or version-controlled
- All serious programming work

In practice, you will use both constantly. The REPL for quick experiments, `.py` files for actual programs.

---

## Virtual Environments: Why You Need Them

This is slightly advanced for a foundations module, but it is important to know about from the start.

### The Problem

When you install a Python package (like `requests` or `numpy`), it gets installed globally on your computer. Now imagine:

- Project A needs `requests` version 2.28
- Project B needs `requests` version 2.31
- You cannot have two versions of the same package installed globally at the same time

This is called dependency conflict, and it will cause bugs that are very hard to trace.

### The Solution: Virtual Environments

A virtual environment is an isolated Python installation for a specific project. Each project gets its own Python and its own packages, completely separate from every other project.

### Creating a Virtual Environment

```bash
# In your project directory:
python3 -m venv venv          # Creates a virtual environment in a folder called 'venv'
source venv/bin/activate      # Activate it (macOS/Linux)
# OR
venv\Scripts\activate         # Activate it (Windows)

# Now install packages — they go into this venv, not globally
pip install requests

# When done, deactivate
deactivate
```

Your prompt will show `(venv)` when a virtual environment is active.

### For This Module

You do not need a virtual environment for the foundations module — we only use Python's built-in tools. But get in the habit of creating one for every project going forward. It will save you hours of debugging later.

---

## Summary

| Concept | Key Point |
|---------|-----------|
| CPython | The official Python interpreter, written in C |
| Compilation | Python does compile — to bytecode, not machine code |
| Bytecode | Intermediate form stored in `.pyc` files |
| Python VM | The component that runs bytecode |
| REPL | Interactive Python session (`python3` in terminal) |
| `>>>` prompt | Python is waiting for input |
| Virtual environment | Isolated Python + packages per project |

---

## Next

Read `04-how-python-runs.md` to understand exactly what happens under the hood when Python executes your code — including bytecode, the GIL, and memory management.
