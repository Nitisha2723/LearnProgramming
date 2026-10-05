"""
file_io_demo.py — File I/O with pathlib, CSV, and JSON

Topics covered:
  - open() with context managers
  - Reading strategies: read(), readline(), line-by-line iteration
  - pathlib.Path — the modern way
  - CSV reading and writing with DictReader/DictWriter
  - JSON serialization and deserialization
  - Working with directories
"""

import csv
import json
import os
import tempfile
from pathlib import Path

# We'll use a temp directory for all our examples
TMPDIR = Path(tempfile.mkdtemp())
print(f"Using temp directory: {TMPDIR}\n")

# =============================================================================
# SECTION 1: Basic File Reading and Writing
# =============================================================================

print("=" * 60)
print("SECTION 1: Basic File I/O")
print("=" * 60)

# Write a file
sample_file = TMPDIR / "sample.txt"
with open(sample_file, "w") as f:
    for i in range(1, 6):
        f.write(f"Line {i}: This is sample content\n")

# Read the whole file
with open(sample_file) as f:
    content = f.read()
print(f"read():\n{content}")

# Read line by line (memory efficient)
with open(sample_file) as f:
    print("Line-by-line:")
    for line in f:
        print(f"  '{line.rstrip()}'")

# readline() — one at a time
with open(sample_file) as f:
    print(f"\nFirst line:  '{f.readline().rstrip()}'")
    print(f"Second line: '{f.readline().rstrip()}'")

# readlines() — all lines as a list
with open(sample_file) as f:
    lines = f.readlines()
print(f"\nreadlines() returns {len(lines)} items")
print(f"First: {lines[0]!r}")

# =============================================================================
# SECTION 2: pathlib.Path — The Modern Way
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 2: pathlib.Path")
print("=" * 60)

p = Path("/home/alice/data/students.csv")

print(f"Path:    {p}")
print(f"name:    {p.name}")       # students.csv
print(f"stem:    {p.stem}")       # students
print(f"suffix:  {p.suffix}")     # .csv
print(f"parent:  {p.parent}")     # /home/alice/data
print(f"parts:   {p.parts}")

# Build paths with /
config_dir = TMPDIR / "config"
config_dir.mkdir(exist_ok=True)
config_file = config_dir / "app.json"

print(f"\nBuilt path: {config_file}")

# Check existence
print(f"exists():   {config_file.exists()}")
config_file.write_text('{"version": 1}')
print(f"exists():   {config_file.exists()}")
print(f"is_file():  {config_file.is_file()}")
print(f"is_dir():   {config_file.is_dir()}")

# Read with pathlib
content = config_file.read_text()
print(f"\nread_text(): {content!r}")

# Write with pathlib
log_file = TMPDIR / "app.log"
log_file.write_text("Session started\nUser logged in\nError occurred\n")

# Path properties
print(f"\nlog file size: {log_file.stat().st_size} bytes")

# Change suffix
backup_path = log_file.with_suffix(".log.bak")
print(f"with_suffix: {backup_path}")

# Iterate directory
print(f"\nContents of {TMPDIR}:")
for item in sorted(TMPDIR.iterdir()):
    kind = "DIR " if item.is_dir() else "FILE"
    print(f"  {kind} {item.name}")

# Glob pattern
(TMPDIR / "data").mkdir(exist_ok=True)
for i in range(3):
    (TMPDIR / "data" / f"file_{i}.txt").write_text(f"content {i}")
(TMPDIR / "data" / "other.csv").write_text("a,b,c")

print(f"\nAll .txt files in TMPDIR:")
for f in sorted(TMPDIR.rglob("*.txt")):
    print(f"  {f.relative_to(TMPDIR)}")

# =============================================================================
# SECTION 3: CSV
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 3: CSV")
print("=" * 60)

grades_csv = TMPDIR / "grades.csv"

# Write with DictWriter
students = [
    {"name": "Alice",  "math": 90, "science": 85, "english": 88},
    {"name": "Bob",    "math": 75, "science": 92, "english": 80},
    {"name": "Carol",  "math": 88, "science": 79, "english": 95},
    {"name": "Dave",   "math": 62, "science": 70, "english": 74},
    {"name": "Eve",    "math": 95, "science": 88, "english": 91},
]

with open(grades_csv, "w", newline="") as f:
    fieldnames = ["name", "math", "science", "english"]
    writer = csv.DictWriter(f, fieldnames=fieldnames)
    writer.writeheader()
    writer.writerows(students)

print(f"Written {len(students)} students to {grades_csv.name}")

# Read with DictReader
print("\nReading grades:")
with open(grades_csv, newline="") as f:
    reader = csv.DictReader(f)
    for row in reader:
        avg = (int(row["math"]) + int(row["science"]) + int(row["english"])) / 3
        print(f"  {row['name']:<8} avg={avg:.1f}")

# CSV with raw reader
plain_csv = TMPDIR / "plain.csv"
with open(plain_csv, "w", newline="") as f:
    writer = csv.writer(f)
    writer.writerow(["ID", "Name", "City"])
    writer.writerows([
        [1, "Alice", "New York"],
        [2, "Bob",   "Los Angeles"],
        [3, "Carol", "Chicago"],
    ])

with open(plain_csv) as f:
    reader = csv.reader(f)
    header = next(reader)
    print(f"\nPlain CSV columns: {header}")
    for row in reader:
        print(f"  {row}")

# =============================================================================
# SECTION 4: JSON
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 4: JSON")
print("=" * 60)

# Python → JSON
data = {
    "name": "Alice",
    "age": 30,
    "scores": [85, 92, 78],
    "address": {"city": "NYC", "zip": "10001"},
    "active": True,
    "notes": None,
}

# To string
json_str = json.dumps(data, indent=2)
print("JSON string:")
print(json_str)

# From string
parsed = json.loads(json_str)
print(f"\nParsed back: {parsed['name']}, scores={parsed['scores']}")

# To file
json_file = TMPDIR / "data.json"
with open(json_file, "w") as f:
    json.dump(data, f, indent=2)

# From file
with open(json_file) as f:
    loaded = json.load(f)
print(f"Loaded from file: {loaded['address']}")

# pathlib shortcut
json_file.write_text(json.dumps(data, indent=2))
loaded2 = json.loads(json_file.read_text())
print(f"Via pathlib: {loaded2['age']}")

# Handle non-serializable types
from datetime import datetime

class CustomEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, datetime):
            return obj.isoformat()
        return super().default(obj)

data_with_dt = {
    "event": "login",
    "timestamp": datetime(2024, 1, 15, 9, 30, 0),
    "user": "alice",
}

# Custom encoder handles datetime
json_str = json.dumps(data_with_dt, cls=CustomEncoder, indent=2)
print(f"\nJSON with datetime:\n{json_str}")

# =============================================================================
# SECTION 5: Working with Directories
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 5: Working with Directories")
print("=" * 60)

# Create nested directories
output_dir = TMPDIR / "output" / "2024" / "january"
output_dir.mkdir(parents=True, exist_ok=True)
print(f"Created: {output_dir.relative_to(TMPDIR)}")

# Write multiple files
for day in range(1, 4):
    report = output_dir / f"report_{day:02d}.txt"
    report.write_text(f"Report for day {day}\n")

# Find all reports
print("\nAll report files:")
for path in sorted((TMPDIR / "output").rglob("*.txt")):
    print(f"  {path.relative_to(TMPDIR)}")

# Copy (read and write)
src = TMPDIR / "sample.txt"
dst = TMPDIR / "sample_copy.txt"
dst.write_bytes(src.read_bytes())
print(f"\nCopied {src.stat().st_size} bytes")

# Delete a file
dst.unlink()
print(f"Deleted: {dst.name}")

# =============================================================================
# SECTION 6: Encoding Best Practices
# =============================================================================

print("\n" + "=" * 60)
print("SECTION 6: Encoding")
print("=" * 60)

# Always specify encoding for portability
unicode_file = TMPDIR / "unicode.txt"
unicode_file.write_text("Hello, 世界! Héllo Wörld 🌍", encoding="utf-8")
content = unicode_file.read_text(encoding="utf-8")
print(f"Unicode content: {content}")

# Different encodings
latin1_file = TMPDIR / "latin1.txt"
latin1_file.write_bytes("Café au lait".encode("latin-1"))
content = latin1_file.read_text(encoding="latin-1")
print(f"Latin-1 content: {content}")

# =============================================================================
# Cleanup
# =============================================================================

import shutil
shutil.rmtree(TMPDIR)
print(f"\nCleaned up {TMPDIR}")
