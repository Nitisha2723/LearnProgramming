# Python Packaging and Tooling

A professional Python project needs more than just source code — it needs a reproducible
environment, consistent style, static analysis, and a clear structure that others can
understand and run. This guide walks through everything you need.

---

## Virtual Environments

### Why Virtual Environments Matter

Without a virtual environment, `pip install` installs packages globally, which causes:

- **Version conflicts** — project A needs `requests==2.28`, project B needs `requests==2.31`
- **Reproducibility problems** — "it works on my machine" because your global packages differ
- **Pollution** — unrelated packages slow down tooling and create surprising import behaviour

A virtual environment is an isolated directory containing its own Python interpreter and
package set.

### Creating and Activating a Virtual Environment

```bash
# Create a virtual environment called .venv in the current directory
python3 -m venv .venv

# Activate it (macOS / Linux)
source .venv/bin/activate

# Activate it (Windows PowerShell)
.venv\Scripts\Activate.ps1

# Activate it (Windows cmd.exe)
.venv\Scripts\activate.bat

# Confirm you are inside the venv — should show the venv's Python
which python        # macOS/Linux
where python        # Windows

# Deactivate when you are done
deactivate
```

### Managing Python Versions with pyenv

[pyenv](https://github.com/pyenv/pyenv) lets you install and switch between multiple Python
versions without touching the system Python:

```bash
# Install pyenv (macOS)
brew install pyenv

# Install a specific Python version
pyenv install 3.12.3

# Use it in the current directory (creates .python-version file)
pyenv local 3.12.3

# Create a venv using that version
python -m venv .venv
```

---

## pip — Package Installer for Python

### Basic Usage

```bash
# Install a package
pip install requests

# Install a specific version
pip install requests==2.31.0

# Install multiple packages at once
pip install requests pytest black

# Upgrade a package
pip install --upgrade requests

# Uninstall a package
pip uninstall requests

# List installed packages
pip list

# Show details about a package
pip show requests
```

### requirements.txt — Pinning Dependencies

`requirements.txt` records the exact versions installed so others can reproduce your
environment:

```bash
# Generate requirements.txt from current environment
pip freeze > requirements.txt

# Install from requirements.txt (e.g., after cloning the repo)
pip install -r requirements.txt
```

A typical `requirements.txt`:

```
black==24.4.2
mypy==1.10.0
pytest==8.2.2
pytest-cov==5.0.0
requests==2.32.3
ruff==0.4.9
```

**Tip:** Split runtime and development dependencies into separate files:

```
requirements.txt          # Runtime only (what the app needs to run)
requirements-dev.txt      # Dev tools (pytest, black, ruff, mypy)
```

`requirements-dev.txt` then starts with:

```
-r requirements.txt
pytest==8.2.2
black==24.4.2
ruff==0.4.9
mypy==1.10.0
```

---

## pyproject.toml — The Modern Standard

`pyproject.toml` is the single configuration file for modern Python projects, replacing
`setup.py`, `setup.cfg`, `tox.ini`, `.flake8`, `mypy.ini`, and `pytest.ini`.

### Minimal project layout

```toml
[build-system]
requires = ["setuptools>=68", "wheel"]
build-backend = "setuptools.backends.legacy:build"

[project]
name = "my-package"
version = "0.1.0"
description = "A short description of the project"
readme = "README.md"
requires-python = ">=3.11"
license = { text = "MIT" }
authors = [{ name = "Your Name", email = "you@example.com" }]

dependencies = [
    "requests>=2.31",
]

[project.optional-dependencies]
dev = [
    "pytest>=8",
    "pytest-cov>=5",
    "black>=24",
    "ruff>=0.4",
    "mypy>=1.10",
]
```

### Configuring tools in pyproject.toml

```toml
# pytest
[tool.pytest.ini_options]
testpaths = ["tests"]
addopts = "-v --tb=short --cov=src --cov-report=term-missing"

# black
[tool.black]
line-length = 88
target-version = ["py311", "py312"]

# ruff
[tool.ruff]
line-length = 88
target-version = "py311"

[tool.ruff.lint]
select = ["E", "F", "I", "N", "UP", "B"]   # pycodestyle, pyflakes, isort, pep8-naming, pyupgrade, bugbear
ignore = ["E501"]                            # Line too long — handled by black

# mypy
[tool.mypy]
python_version = "3.11"
strict = true
ignore_missing_imports = true
```

Install dev dependencies with:

```bash
pip install -e ".[dev]"     # -e = editable install (src changes reflect immediately)
```

---

## black — Uncompromising Code Formatter

[black](https://black.readthedocs.io/) formats Python code automatically. It is intentionally
opinionated: there is no configuration for style choices — you format and move on.

```bash
# Install
pip install black

# Check which files would be changed (does not modify files)
black . --check

# Format all Python files in the project
black .

# Format a specific file
black src/mymodule.py

# Show a diff without writing
black src/mymodule.py --diff
```

Before:

```python
def greet(name:str,greeting:str="Hello")->str:
    return greeting+" "+name
```

After:

```python
def greet(name: str, greeting: str = "Hello") -> str:
    return greeting + " " + name
```

**CI integration:** Run `black . --check` in CI to reject unformatted code.

---

## ruff — Fast Python Linter

[ruff](https://docs.astral.sh/ruff/) is a linter written in Rust that is 10–100x faster than
pylint or flake8. It replaces flake8, isort, pyupgrade, and dozens of plugins in a single tool.

```bash
# Install
pip install ruff

# Lint the entire project
ruff check .

# Auto-fix fixable issues
ruff check . --fix

# Format (ruff can also format, as an alternative to black)
ruff format .

# Check and fix in one command
ruff check . --fix && ruff format .
```

Common issues ruff catches:

```python
# E711 — comparison to None using ==
if x == None:        # Bad
if x is None:        # Good

# F401 — unused import
import os            # Flagged if os is never used

# I001 — unsorted imports (isort rules)
import sys
import os            # Should come before sys

# B006 — mutable default argument
def append(x, lst=[]):  # Bug-prone: lst is shared across all calls!
    lst.append(x)
    return lst
```

---

## mypy — Static Type Checking

[mypy](https://mypy.readthedocs.io/) checks Python type annotations without running the code.
It catches type errors at development time rather than at runtime.

```bash
# Install
pip install mypy

# Check a file
mypy src/mymodule.py

# Check the entire project
mypy .

# Check with strict mode (recommended for new projects)
mypy . --strict
```

Example: mypy catching a bug

```python
def double(n: int) -> int:
    return n * 2

result = double("hello")    # mypy error: Argument 1 to "double" has incompatible type "str"; expected "int"
```

Gradual adoption — you can add types to one module at a time:

```python
# Start unannotated
def greet(name):
    return f"Hello, {name}"

# Add types gradually
def greet(name: str) -> str:
    return f"Hello, {name}"
```

---

## Setting Up a Professional Python Project from Scratch

### Step 1 — Create the directory structure

```bash
mkdir my-project && cd my-project

# Standard layout
mkdir -p src/my_package tests docs

# Create placeholder files
touch src/my_package/__init__.py
touch tests/__init__.py
touch README.md
```

Recommended project layout:

```
my-project/
├── src/
│   └── my_package/
│       ├── __init__.py
│       ├── models.py
│       └── service.py
├── tests/
│   ├── conftest.py
│   ├── test_models.py
│   └── test_service.py
├── pyproject.toml
├── requirements.txt
├── requirements-dev.txt
└── README.md
```

### Step 2 — Create the virtual environment

```bash
python3 -m venv .venv
source .venv/bin/activate   # macOS/Linux
```

### Step 3 — Create pyproject.toml

```toml
[build-system]
requires = ["setuptools>=68", "wheel"]
build-backend = "setuptools.backends.legacy:build"

[project]
name = "my-package"
version = "0.1.0"
requires-python = ">=3.11"

[tool.pytest.ini_options]
testpaths = ["tests"]
addopts = "-v --tb=short"

[tool.black]
line-length = 88
target-version = ["py311"]

[tool.ruff]
line-length = 88

[tool.ruff.lint]
select = ["E", "F", "I", "B"]

[tool.mypy]
python_version = "3.11"
strict = true
```

### Step 4 — Install dev tools

```bash
pip install pytest pytest-cov black ruff mypy
# Or if you set up optional-dependencies in pyproject.toml:
pip install -e ".[dev]"
```

### Step 5 — Write code and tests

```python
# src/my_package/models.py
from dataclasses import dataclass, field
from uuid import UUID, uuid4


@dataclass
class Item:
    name: str
    quantity: int
    id: UUID = field(default_factory=uuid4)
```

```python
# tests/test_models.py
from my_package.models import Item


def test_item_creation() -> None:
    item = Item(name="apple", quantity=3)
    assert item.name == "apple"
    assert item.quantity == 3
    assert item.id is not None
```

### Step 6 — Run the toolchain

```bash
# Run tests with coverage
pytest --cov=src --cov-report=term-missing

# Format code
black .

# Lint and auto-fix
ruff check . --fix

# Type check
mypy .
```

### Step 7 — Add a Makefile for convenience (optional)

```makefile
.PHONY: test lint format typecheck all

test:
	pytest --cov=src --cov-report=term-missing

lint:
	ruff check . --fix

format:
	black .

typecheck:
	mypy .

all: format lint typecheck test
```

Now `make all` runs the full quality pipeline with one command.

---

## Summary

| Tool | Purpose | Command |
|---|---|---|
| `venv` | Isolated Python environment | `python -m venv .venv` |
| `pip` | Package installer | `pip install -r requirements.txt` |
| `pyproject.toml` | Central configuration file | (edit manually) |
| `black` | Code formatter | `black .` |
| `ruff` | Fast linter (replaces flake8) | `ruff check . --fix` |
| `mypy` | Static type checker | `mypy .` |
| `pytest` | Test runner | `pytest` |
| `pytest-cov` | Coverage reporting | `pytest --cov=src` |
