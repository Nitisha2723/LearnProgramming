# File I/O in Python

Python makes file handling straightforward. The modern way uses `pathlib` for paths
and the `with` statement for safe resource management.

---

## `open()` — The Basics

```python
file = open("data.txt", "r")     # Open for reading
content = file.read()
file.close()   # Must close manually!
```

**Always use `with`** — it closes the file automatically, even if an exception occurs:

```python
with open("data.txt", "r") as f:
    content = f.read()
# File is automatically closed here
```

### File Modes

| Mode  | Meaning                          | Creates if not exists | Overwrites |
|-------|----------------------------------|-----------------------|------------|
| `"r"` | Read text                        | No (FileNotFoundError)| —          |
| `"w"` | Write text                       | Yes                   | Yes        |
| `"a"` | Append text                      | Yes                   | No         |
| `"x"` | Create (fail if exists)          | Yes (FileExistsError) | —          |
| `"rb"`| Read binary                      | No                    | —          |
| `"wb"`| Write binary                     | Yes                   | Yes        |
| `"r+"`| Read and write                   | No                    | No         |

---

## Reading Files

### Read the whole file at once

```python
with open("data.txt") as f:
    content = f.read()    # One big string
```

### Read line by line (memory efficient)

```python
# Method 1: Iterate directly (best for large files)
with open("data.txt") as f:
    for line in f:
        process(line.rstrip("\n"))  # Remove trailing newline

# Method 2: readline() — one line at a time
with open("data.txt") as f:
    first_line = f.readline()
    second_line = f.readline()

# Method 3: readlines() — list of all lines (loads entire file!)
with open("data.txt") as f:
    lines = f.readlines()
# lines = ["line1\n", "line2\n", ...]
```

For large files (hundreds of MB), always iterate directly — `readlines()` loads
everything into memory.

---

## Writing Files

```python
with open("output.txt", "w") as f:
    f.write("First line\n")
    f.write("Second line\n")

# writelines() — write a sequence of strings
lines = ["line1\n", "line2\n", "line3\n"]
with open("output.txt", "w") as f:
    f.writelines(lines)

# Append to existing file
with open("log.txt", "a") as f:
    f.write(f"[2024-01-15] Event occurred\n")
```

### Use `print()` for formatted output

```python
with open("report.txt", "w") as f:
    print("Report Title", file=f)
    print("=" * 40, file=f)
    for name, score in results:
        print(f"{name:<20} {score}", file=f)
```

---

## `pathlib.Path` — The Modern Way

`pathlib` provides object-oriented file path handling. Prefer it over the older `os.path`.

```python
from pathlib import Path

# Create path objects
p = Path("data/students.csv")
home = Path.home()          # /Users/alice
cwd = Path.cwd()            # Current working directory

# Path concatenation with /
config_file = Path("config") / "app.json"
data_dir = Path.home() / "Documents" / "data"
```

### Checking Existence and Type

```python
p = Path("myfile.txt")

p.exists()      # True/False
p.is_file()     # True if file
p.is_dir()      # True if directory
```

### Common Path Properties

```python
p = Path("/home/alice/data/students.csv")

p.name          # "students.csv"
p.stem          # "students"
p.suffix        # ".csv"
p.parent        # Path("/home/alice/data")
p.parts         # ("/", "home", "alice", "data", "students.csv")
str(p)          # "/home/alice/data/students.csv"
```

### Reading and Writing with pathlib

```python
from pathlib import Path

path = Path("data.txt")

# Read
content = path.read_text()                     # Whole file as string
content = path.read_text(encoding="utf-8")     # With explicit encoding
bytes_data = path.read_bytes()                 # Binary

# Write
path.write_text("Hello, World!\n")
path.write_bytes(b"\x00\x01\x02")
```

### Working with Directories

```python
from pathlib import Path

# Create directory
Path("output").mkdir(exist_ok=True)
Path("a/b/c").mkdir(parents=True, exist_ok=True)   # Create all intermediate dirs

# List contents
for item in Path(".").iterdir():
    print(item, "DIR" if item.is_dir() else "FILE")

# Find files recursively with glob
for csv_file in Path(".").rglob("*.csv"):
    print(csv_file)

# Find files matching a pattern
for py_file in Path("src").glob("**/*.py"):
    print(py_file)
```

### Path Arithmetic

```python
base = Path("/home/alice/project")

# Build new paths
report = base / "output" / "report.txt"

# Get relative path
relative = report.relative_to(base)    # output/report.txt

# Change suffix
new_name = Path("data.csv").with_suffix(".json")  # data.json
```

---

## CSV with the `csv` Module

```python
import csv
from pathlib import Path

# Write CSV
data = [
    ["Name", "Age", "City"],
    ["Alice", 30, "NYC"],
    ["Bob", 25, "LA"],
]

with open("people.csv", "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerows(data)

# Read CSV
with open("people.csv", "r") as f:
    reader = csv.reader(f)
    header = next(reader)
    for row in reader:
        name, age, city = row
        print(f"{name}, {age}, {city}")
```

### DictReader and DictWriter — Named Columns

```python
import csv

# Write with column names
students = [
    {"name": "Alice", "math": 90, "science": 85},
    {"name": "Bob",   "math": 75, "science": 88},
]

with open("grades.csv", "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=["name", "math", "science"])
    writer.writeheader()
    writer.writerows(students)

# Read with column names
with open("grades.csv") as f:
    reader = csv.DictReader(f)
    for row in reader:
        print(f"{row['name']}: math={row['math']}, science={row['science']}")
```

### Important: `newline=""` on Windows

Always open CSV files with `newline=""` to prevent double newlines on Windows.
The `csv` module handles its own newline logic.

---

## JSON with the `json` Module

```python
import json

# Python dict → JSON string
data = {
    "name": "Alice",
    "scores": [85, 92, 78],
    "active": True,
    "metadata": None,
}

# To string
json_str = json.dumps(data, indent=2)
print(json_str)

# From string
parsed = json.loads(json_str)

# To file
with open("data.json", "w") as f:
    json.dump(data, f, indent=2)

# From file
with open("data.json") as f:
    loaded = json.load(f)
```

### Handling Non-Serializable Types

```python
from datetime import datetime
import json

# Python datetime is not JSON serializable by default
data = {"timestamp": datetime.now()}
# json.dumps(data)  ← raises TypeError!

# Solution 1: Convert before serializing
data = {"timestamp": datetime.now().isoformat()}

# Solution 2: Custom encoder
class DatetimeEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, datetime):
            return obj.isoformat()
        return super().default(obj)

json.dumps(data_with_datetime, cls=DatetimeEncoder)
```

---

## `os.path` vs `pathlib` — What to Use When

| Task                        | `os.path`              | `pathlib` (preferred)          |
|-----------------------------|------------------------|--------------------------------|
| Join paths                  | `os.path.join(a, b)`   | `Path(a) / b`                  |
| Get filename                | `os.path.basename(p)`  | `Path(p).name`                 |
| Get directory               | `os.path.dirname(p)`   | `Path(p).parent`               |
| Check existence             | `os.path.exists(p)`    | `Path(p).exists()`             |
| Check is file               | `os.path.isfile(p)`    | `Path(p).is_file()`            |
| Get extension               | `os.path.splitext(p)`  | `Path(p).suffix`               |
| Create directory            | `os.makedirs(p)`       | `Path(p).mkdir(parents=True)`  |
| Read whole file             | `open(p).read()`       | `Path(p).read_text()`          |
| List directory              | `os.listdir(p)`        | `Path(p).iterdir()`            |

**Use `pathlib` for new code.** `os.path` is still fine if you're working
with older codebases that use it throughout.

---

## Encoding

Always specify encoding when working with text files to avoid platform-specific
behavior:

```python
with open("data.txt", "r", encoding="utf-8") as f:
    content = f.read()

path.read_text(encoding="utf-8")
```

`utf-8` is the right choice for almost everything. The default encoding varies
by OS (often UTF-8 on macOS/Linux, often cp1252 on Windows).
