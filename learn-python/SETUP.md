# Setup Guide

Get the learn-python repository running on your machine in five minutes.

---

## Prerequisites

### Install Python 3.11+

**Check what you have:**

```bash
python3 --version   # Should print Python 3.11.x or higher
```

**Option A — System package manager (quick)**

```bash
# macOS with Homebrew
brew install python@3.12

# Ubuntu / Debian
sudo apt update && sudo apt install python3.12 python3.12-venv python3.12-pip
```

**Option B — pyenv (recommended for managing multiple versions)**

pyenv lets you install any Python version without touching your system Python:

```bash
# macOS
brew install pyenv

# Linux (automatic installer)
curl https://pyenv.run | bash

# Then add to your shell config (~/.zshrc or ~/.bashrc):
export PYENV_ROOT="$HOME/.pyenv"
export PATH="$PYENV_ROOT/bin:$PATH"
eval "$(pyenv init -)"

# Install and activate Python 3.12
pyenv install 3.12.3
pyenv local 3.12.3     # Creates .python-version in the current directory
python --version       # Should print Python 3.12.3
```

---

## Create and Activate a Virtual Environment

Always work inside a virtual environment to keep dependencies isolated from your system Python.

```bash
# From the repository root
cd learn-python

# Create the virtual environment
python3 -m venv .venv

# Activate it — macOS / Linux
source .venv/bin/activate

# Activate it — Windows PowerShell
.venv\Scripts\Activate.ps1

# Activate it — Windows cmd.exe
.venv\Scripts\activate.bat

# Verify you are inside the venv
which python    # macOS/Linux — should show .venv/bin/python
```

You should see `(.venv)` in your shell prompt.

---

## Install Development Dependencies

```bash
pip install pytest pytest-cov black ruff mypy
```

Verify the installs:

```bash
pytest --version    # e.g. pytest 8.2.2
black --version     # e.g. black, 24.4.2
ruff --version      # e.g. ruff 0.4.9
mypy --version      # e.g. mypy 1.10.0
```

---

## Running Tests

### Run all tests

```bash
pytest
```

This discovers and runs every `test_*.py` file across all modules, as configured in
`pyproject.toml`.

### Run tests for a specific module

```bash
pytest 01-foundations/
pytest 02-core-language/
pytest 03-oop/
pytest 04-data-structures/
pytest 05-real-world/
pytest 06-design/
pytest 07-advanced/
```

### Run tests with coverage

```bash
pytest --cov=. --cov-report=term-missing
```

### Run a single test file

```bash
pytest 02-core-language/tests/test_functions.py
```

### Run a single test by name

```bash
pytest -k "test_add"             # Run tests whose name contains "test_add"
pytest -k "test_add or test_sub" # Run tests matching either pattern
```

### Run tests verbosely (see each test name)

```bash
pytest -v
```

### Run tests and stop on first failure

```bash
pytest -x
```

---

## Code Style

### Check formatting without changing files

```bash
black . --check
```

### Format all files

```bash
black .
```

### Lint with ruff (and auto-fix fixable issues)

```bash
ruff check . --fix
```

---

## Type Checking

```bash
mypy .
```

For strict checking (recommended as you progress through the exercises):

```bash
mypy . --strict
```

---

## IDE Setup

### VS Code

1. Install [VS Code](https://code.visualstudio.com/)
2. Open the Extensions panel (`Cmd+Shift+X` / `Ctrl+Shift+X`)
3. Install the **Python** extension (Microsoft)
4. Open the Command Palette (`Cmd+Shift+P`) → "Python: Select Interpreter" → choose `.venv`
5. Recommended additional extensions:
   - **Pylance** — fast type checking in the editor
   - **Ruff** — inline linting
   - **Black Formatter** — format on save

Add to `.vscode/settings.json`:

```json
{
  "editor.formatOnSave": true,
  "[python]": {
    "editor.defaultFormatter": "ms-python.black-formatter"
  },
  "ruff.enable": true,
  "python.testing.pytestEnabled": true,
  "python.testing.pytestArgs": ["."]
}
```

### PyCharm

1. Install [PyCharm Community or Professional](https://www.jetbrains.com/pycharm/)
2. Open the project folder
3. Go to **Settings → Project → Python Interpreter → Add Interpreter → Existing**
4. Select `.venv/bin/python`
5. Enable pytest: **Settings → Tools → Python Integrated Tools → Default test runner → pytest**
6. Install the **Ruff** plugin from the marketplace for inline linting

---

## Troubleshooting

### `ModuleNotFoundError` when running a test

Make sure your virtual environment is activated (`source .venv/bin/activate`) and you are
running pytest from the repository root.

### `pytest: command not found`

pytest is not installed or the venv is not activated. Run:

```bash
source .venv/bin/activate
pip install pytest
```

### Tests pass locally but fail in a fresh clone

Regenerate `requirements.txt` and commit it:

```bash
pip freeze > requirements.txt
git add requirements.txt && git commit -m "pin dev dependencies"
```

### Python version mismatch

Use `pyenv local 3.12.3` (or whatever version you need) in the repository root to pin the
version. Create a fresh venv after changing the Python version.
