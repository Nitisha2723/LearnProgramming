# What Is Python?

Python is a programming language — but "programming language" does not capture what makes Python special. Python is opinionated about how code should look. It is designed around the belief that code is read far more often than it is written, and that readable code is better code.

---

## A Brief History

### Guido van Rossum and the Christmas Project

In December 1989, a Dutch computer scientist named **Guido van Rossum** was looking for a programming project to work on during his Christmas holiday. He was frustrated with a language called ABC — it had good ideas but was too limited for real use.

So he started building a new language. He wanted it to:
- Be easy to read (unlike C or Perl)
- Have a small, clean set of rules
- Be extensible with modules
- Work as a scripting language that could also grow into full programs

He named it **Python** — not after the snake, but after the British comedy group **Monty Python's Flying Circus**. Guido wanted the language to be fun, and he was a fan of the show. (The snake logo came later and stuck — Python tutorials are full of snake puns as a result.)

Python 0.9.0 was released publicly in **February 1991**. Python 2.0 came in 2000. Python 3.0 — which broke backward compatibility to fix the language's design mistakes — came in 2008.

### The Benevolent Dictator for Life

For many years, Guido was Python's "Benevolent Dictator for Life" (BDFL) — the person with final say over the language's design. He stepped down from that role in 2018, and Python is now governed by the **Python Steering Council**. But his influence is stamped on every line of Python code ever written.

---

## Python's Philosophy: The Zen of Python

Python is one of the few programming languages with an explicit design philosophy. Type this in any Python interpreter:

```python
import this
```

You will see the **Zen of Python** — 19 guiding principles written by Tim Peters in 1999. Here are the most important ones, explained:

---

**"Beautiful is better than ugly."**

Python code should be pleasant to read. If your code looks ugly, that is a sign something is wrong. Python enforces a consistent visual style through syntax (required indentation, clean structure).

---

**"Explicit is better than implicit."**

Code should say what it does, not hide it. Magic behavior that happens invisibly is bad. If something happens, the reader should be able to see it happening.

---

**"Simple is better than complex."**

When there are two ways to do something, prefer the simpler one. Python actively discourages clever-but-unreadable code.

---

**"Complex is better than complicated."**

Sometimes a problem is genuinely complex. That is fine. But "complicated" (unnecessarily tangled) code is never acceptable. There is a difference between something that is hard because the problem is hard, and something that is hard because the programmer made it hard.

---

**"Readability counts."**

This is the central principle. Python is designed to be read. Code is a communication tool — you communicate with future humans (including yourself) as much as you communicate with the computer.

---

**"Special cases aren't special enough to break the rules."**

Python has consistent rules. There are very few exceptions. This makes the language predictable — once you learn the patterns, they apply everywhere.

---

**"Errors should never pass silently."**

If something goes wrong, Python tells you loudly. Silent failures (bugs that exist but you do not notice) are some of the hardest problems to debug. Python would rather crash with a clear error message than continue with wrong data.

---

**"There should be one — and preferably only one — obvious way to do it."**

This is very different from languages like Perl (which has a philosophy of "there's more than one way to do it"). Python's goal is that experienced Python developers, given the same problem, should write essentially the same code. This makes Python codebases consistent and readable.

---

**"If the implementation is hard to explain, it's a bad idea."**

If you cannot clearly explain what your code does, your code is too complex. Simplify it.

---

## Why Python Became the Most Popular Language

Python has been the most popular programming language in the world (by many metrics) since approximately 2020. It was not always this way — Python was a niche scripting language for years. What changed?

### 1. Readability lowers the learning curve

Python looks like structured English. Compare printing "Hello, World!" in different languages:

**Java:**
```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

**C++:**
```cpp
#include <iostream>
int main() {
    std::cout << "Hello, World!" << std::endl;
    return 0;
}
```

**Python:**
```python
print("Hello, World!")
```

Python's low barrier to entry made it the first choice for teaching programming. Universities worldwide adopted it. A generation of developers learned to code in Python.

### 2. The data science revolution

In the 2010s, data science exploded. Companies had more data than they could analyze, and they needed tools to work with it. **NumPy**, **Pandas**, and **Matplotlib** made Python the definitive language for data analysis. **Jupyter notebooks** made interactive data exploration mainstream.

### 3. The AI/ML explosion

**TensorFlow** (Google, 2015) and **PyTorch** (Facebook, 2016) were both built for Python. Every major AI research paper publishes Python code. The deep learning revolution happened in Python, which made Python the language of the most exciting field in technology.

### 4. "Batteries included"

Python's standard library is massive. You can do HTTP requests, parse JSON, read CSV files, work with dates, compress files, run a web server, send emails, and parse HTML — all without installing anything beyond Python itself. The phrase "batteries included" means Python ships with everything you need to get started.

### 5. The ecosystem

**pip** (Python's package manager) gives you access to 500,000+ third-party packages. Whatever you want to do, there is almost certainly a Python package for it.

---

## Where Python Is Used Today

Python is not just for beginners or scripts. It powers some of the most critical software in the world:

- **Instagram** — backend is almost entirely Python (Django). Processes over a billion users.
- **YouTube** — originally written substantially in Python. Google (which owns YouTube) is one of Python's biggest users and contributors.
- **Dropbox** — desktop client was written in Python by Drew Houston.
- **Netflix** — uses Python extensively for recommendation systems and data pipelines.
- **NASA** — uses Python for scientific computing, data analysis, and mission control scripts.
- **Spotify** — data pipeline and recommendation systems run on Python.
- **Reddit** — originally built in Lisp, then rewritten in Python in 2005.
- **The entire AI/ML research community** — every major AI lab (OpenAI, DeepMind, Anthropic, Google Brain) publishes Python code.

---

## Python's Design Characteristics

Understanding these will help you write better Python:

### Dynamically Typed

You do not declare variable types in Python. Python figures out the type at runtime:

```python
x = 42          # Python knows x is an int
x = "hello"     # Now x is a string — Python handles this automatically
x = 3.14        # Now x is a float
```

This is different from Java or C where you must declare `int x = 42` and cannot reassign a different type.

### Interpreted

Python code is executed line by line by the Python interpreter. You do not have a separate "compile" step before running. Type `python3 myfile.py` and it runs.

### Garbage Collected

You do not manually manage memory in Python. When you create an object, Python allocates memory. When you no longer need it, Python automatically frees the memory. This eliminates an entire class of bugs common in C and C++.

### "Batteries Included"

Python ships with a comprehensive standard library. Everything from file I/O to cryptography to networking is available without installing anything extra.

### Everything is an object

In Python, everything — numbers, strings, functions, classes — is an object. This gives the language consistency and power that beginners gradually discover.

---

## Python 2 vs Python 3

You may occasionally see Python 2 code in older tutorials, Stack Overflow answers, or legacy codebases. There are important differences:

| Python 2 | Python 3 |
|----------|----------|
| `print "hello"` | `print("hello")` |
| `3 / 2 = 1` (integer division) | `3 / 2 = 1.5` (true division) |
| `unicode` type separate | All strings are unicode |
| `range()` returns a list | `range()` returns an iterator |
| End of life: January 2020 | Current, actively developed |

**Always use Python 3.** Python 2 is no longer supported and should not be used for new code. This repository uses Python 3 throughout.

If you see `python` (without the `3`) in a terminal, it might be Python 2 on older systems. Use `python3` explicitly, or check with:
```bash
python3 --version
python --version
```

---

## Summary

| Fact | Detail |
|------|--------|
| Created by | Guido van Rossum |
| First released | 1991 |
| Named after | Monty Python's Flying Circus |
| Design philosophy | Readable, simple, explicit, consistent |
| Type system | Dynamically typed |
| Execution model | Interpreted |
| Memory management | Automatic (garbage collected) |
| Use Python version | 3 (always) |

---

## Next

Read `03-python-interpreter.md` to understand exactly what happens when you run a Python program and how to use the Python REPL — your interactive coding playground.
