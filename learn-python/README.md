# Learn Python — From Zero to Professional

A comprehensive, structured Python learning repository for everyone — from complete beginners writing their first line of code to experienced developers adding Python to their toolkit.

---

## Who Is This For?

- **Complete beginners** who have never programmed before
- **Developers from other languages** (Java, C++, JavaScript) who want to learn Python's idioms
- **Self-taught programmers** who want to fill gaps and build depth
- **Senior developers** who want to use Python for data science, automation, or AI/ML

No prior experience required. We start from zero and build to professional-level code.

---

## Learning Philosophy

Every module follows this five-step cycle:

```
CONCEPT → UNDERSTAND → APPLY → BUILD → REFLECT
```

| Step | What it means |
|------|---------------|
| **Concept** | Read the theory — understand the "why," not just the "how" |
| **Understand** | Study working code examples with detailed explanations |
| **Apply** | Do structured exercises with hints and scaffolding |
| **Build** | Complete a mini-project that combines everything you learned |
| **Reflect** | Review what you built, what worked, and what was hard |

The goal is not to memorize syntax — it is to think like a Python developer.

---

## Full Learning Path

| Module | Title | What You Learn | Estimated Time |
|--------|-------|----------------|----------------|
| 01 | **Foundations** | How computers work, what Python is, variables, data types, operators | 4–6 hours |
| 02 | **Core Language** | Control flow (if/else/for/while), functions, modules, error handling | 6–8 hours |
| 03 | **Object-Oriented Python** | Classes, objects, inheritance, encapsulation, polymorphism, magic methods | 8–10 hours |
| 04 | **Data Structures** | Lists, tuples, sets, dicts, list comprehensions, generators, iterators | 6–8 hours |
| 05 | **Real-World Python** | File I/O, JSON, CSV, HTTP requests, working with APIs, environment variables | 6–8 hours |
| 06 | **Design & Architecture** | SOLID principles, design patterns, clean code, type hints, documentation | 8–10 hours |
| 07 | **Advanced Python** | Decorators, context managers, metaclasses, concurrency, async/await | 10–12 hours |
| 08 | **Projects** | Four complete projects: CLI tool, web scraper, REST API, data dashboard | 12–16 hours |
| 09 | **Interview Prep** | Algorithms, data structures, LeetCode patterns, system design questions | 10–12 hours |

**Total estimated time:** 70–90 hours of focused learning

---

## How to Use This Repository

### If you are a complete beginner
Work through every module in order. Do not skip ahead. The modules build on each other deliberately — concepts introduced in module 01 are used and reinforced in every module that follows.

### If you know another programming language
Read the theory files quickly (you understand the concepts), then focus on the code examples and exercises. Python has specific idioms that experienced developers often miss — pay attention to the "Pythonic way" notes throughout.

### If you are an experienced Python developer
Jump directly to the modules relevant to your goals. Modules 06 (Design), 07 (Advanced), and 08 (Projects) are written for developers who already know the basics.

### For every learner
- Type the code yourself. Do not copy-paste. Your fingers need to learn the syntax.
- Do every exercise before looking at the solution.
- Build the mini-project at the end of each module on your own first.
- The exercises directory at the root contains additional cross-module challenges.

---

## Prerequisites

**None.** This repository starts from the very beginning.

You need:
- A computer (Windows, macOS, or Linux)
- An internet connection (to install Python)
- Curiosity and patience

You do not need:
- Prior programming experience
- A computer science degree
- Any paid tools or subscriptions

---

## How to Run Python

### Step 1: Install Python

Visit [python.org/downloads](https://python.org/downloads) and download Python 3 (the latest stable version — 3.12 or newer).

**On macOS:** You can also use Homebrew:
```bash
brew install python3
```

**On Windows:** Download the installer and make sure to check "Add Python to PATH" during installation.

**On Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install python3 python3-pip
```

Verify your installation:
```bash
python3 --version
# Should output: Python 3.12.x (or newer)
```

### Step 2: Set Up Your Editor

We recommend **Visual Studio Code** (VS Code) — it's free, fast, and has excellent Python support.

1. Download VS Code at [code.visualstudio.com](https://code.visualstudio.com)
2. Install the **Python extension** by Microsoft (search in the Extensions panel)
3. Open the `learn-python` folder in VS Code

Other good options: PyCharm (full IDE), Zed, Neovim with Python plugins.

### Step 3: Run Your First Python File

Open a terminal (Terminal on macOS/Linux, Command Prompt or PowerShell on Windows):

```bash
# Navigate to this repository
cd path/to/learn-python

# Run a Python file
python3 01-foundations/code/hello_world.py
```

### Step 4: Use the Python REPL (Interactive Mode)

The REPL (Read-Eval-Print Loop) lets you run Python one line at a time — great for experiments:

```bash
python3
>>> print("Hello!")
Hello!
>>> 2 + 2
4
>>> exit()
```

---

## What Can You Build with Python?

Python is one of the most versatile languages ever created. Here is what professionals build with it every day:

### Web Applications
**Django** and **Flask** are Python's most popular web frameworks. Instagram, Pinterest, and Disqus are built with Django. Python web developers are in high demand.

### Data Science and Analytics
**Pandas**, **NumPy**, and **Matplotlib** make Python the language of data. Data scientists use Python to clean, analyze, and visualize massive datasets. Companies like Google, Amazon, and every bank you have heard of hire Python data scientists.

### Artificial Intelligence and Machine Learning
**TensorFlow**, **PyTorch**, and **scikit-learn** are all Python-first. The entire AI/ML research community writes Python. If you want to work in AI, you need Python.

### Automation and Scripting
Python excels at automating repetitive tasks — renaming files, sending emails, filling out forms, scraping websites, scheduling jobs. You can automate weeks of manual work in a few hours with Python.

### APIs and Microservices
**FastAPI** is one of the fastest web frameworks in any language (benchmarked against Go and Node.js). Python is used to build production APIs at companies like Uber, Netflix, and Spotify.

### Games
**Pygame** lets you build 2D games in Python. Many game developers learn game programming with Python before moving to Unity or Unreal.

### Scientific Computing
NASA, CERN, and research labs worldwide use Python for simulations, data analysis, and scientific visualization with tools like SciPy and Jupyter notebooks.

---

## Quick-Start: Your First 30 Minutes

Follow these steps exactly — by the end you will have written and run real Python code.

**Minutes 1–5: Verify Python is installed**
```bash
python3 --version
```
If you see a version number, you are ready. If not, install Python (see above).

**Minutes 5–10: Open the Python REPL**
```bash
python3
```
Type these lines one by one and press Enter after each:
```python
>>> print("Hello, World!")
>>> name = "your name here"
>>> print(f"My name is {name}")
>>> 10 + 5
>>> 10 * 7
>>> exit()
```

**Minutes 10–20: Run your first file**
Navigate to this repository and run:
```bash
python3 01-foundations/code/hello_world.py
```
Open the file in your editor and read every comment. Do not just run it — understand it.

**Minutes 20–30: Do Exercise 1**
Open `01-foundations/exercises/exercise_01_hello_world.py`. Read the instructions in the file and write the code yourself. Run it. Fix any errors. Then compare with the solution in `exercises/solutions/`.

**After 30 minutes:** You have written Python. Continue with the 01-foundations theory files to understand what is actually happening under the hood.

---

## Repository Structure

```
learn-python/
├── README.md                    ← You are here
├── .gitignore                   ← Standard Python gitignore
├── 01-foundations/              ← Start here — complete module
│   ├── README.md
│   ├── theory/                  ← Read these first
│   ├── code/                    ← Study these examples
│   ├── exercises/               ← Then do these
│   └── mini-project/            ← Then build this
├── 02-core-language/            ← Coming next
├── 03-oop/
├── 04-data-structures/
├── 05-real-world/
├── 06-design/
├── 07-advanced/
├── 08-projects/
├── 09-interview-prep/
└── exercises/                   ← Cross-module challenges
```

---

## A Note on Learning

Programming is a skill, not knowledge. Reading about Python is not enough — you must write code, make mistakes, read error messages, fix bugs, and build things. Every professional programmer you admire got there by writing a lot of bad code first.

When something does not work, that is not failure — that is learning. Error messages are not insults; they are clues. The process of finding and fixing bugs is called debugging, and it is one of the most important skills a programmer has.

Be patient with yourself. Be consistent. Write code every day, even if only for 20 minutes. The developers who succeed are not the most talented — they are the ones who kept going.

Now open `01-foundations/README.md` and start learning.
