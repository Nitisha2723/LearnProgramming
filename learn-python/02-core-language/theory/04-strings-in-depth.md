# Theory 04: Strings In Depth

Strings are everywhere in Python. Understanding them deeply — beyond just "a sequence of characters" —
makes the difference between clunky code and elegant code.

---

## Table of Contents

1. [Strings are Immutable Sequences](#1-strings-are-immutable-sequences)
2. [String Methods](#2-string-methods)
3. [f-Strings (Python 3.6+)](#3-f-strings-python-36)
4. [String Slicing](#4-string-slicing)
5. [Multi-line Strings](#5-multi-line-strings)
6. [Raw Strings](#6-raw-strings)
7. [String Encoding Basics](#7-string-encoding-basics)
8. [String Formatting Patterns](#8-string-formatting-patterns)

---

## 1. Strings are Immutable Sequences

A string is an **ordered, immutable sequence of Unicode characters**.
Every character is itself a string of length 1.

```python
s = "Hello, Python!"

# Strings are sequences — you can iterate, index, slice
print(s[0])          # H
print(s[-1])         # !
print(len(s))        # 14
print("Python" in s) # True

for char in "abc":
    print(char)      # a, b, c

# Strings are immutable — you cannot change a character
s[0] = "h"           # TypeError: 'str' object does not support item assignment

# To "modify", create a new string
s = s.replace("H", "h")    # new string object
```

### String Literals

```python
# Single quotes
name = 'Alice'

# Double quotes (equivalent)
name = "Alice"

# When the string contains one type, use the other
msg = "It's a wonderful day"     # contains apostrophe, use double quotes
code = 'print("hello")'          # contains double quotes, use single quotes

# Triple-quoted — spans multiple lines
bio = """Alice is a software engineer
who loves Python and clean code."""
```

### Strings are Unicode

Python 3 strings are Unicode by default. You can include any language:

```python
greeting = "こんにちは"    # Japanese
emoji_msg = "Hello 🐍"     # with emoji
print(len(emoji_msg))       # 8 (each emoji counts as one character in Python 3)
```

---

## 2. String Methods

Python strings have a rich set of methods. None of them modify the original (immutable!) —
they all return a new string.

### Case Methods

```python
text = "Hello, World!"

print(text.upper())          # HELLO, WORLD!
print(text.lower())          # hello, world!
print(text.title())          # Hello, World!  (first letter of each word)
print(text.capitalize())     # Hello, world!  (only first letter)
print(text.swapcase())       # hELLO, wORLD!
```

### Whitespace Methods

```python
padded = "   hello world   "

print(padded.strip())         # "hello world"
print(padded.lstrip())        # "hello world   "
print(padded.rstrip())        # "   hello world"

# Strip specific characters
"***hello***".strip("*")      # "hello"
```

### Search Methods

```python
s = "the quick brown fox jumps over the lazy dog"

print(s.find("fox"))          # 16  (index of first occurrence, or -1 if not found)
print(s.find("cat"))          # -1
print(s.rfind("the"))         # 31  (rightmost occurrence)

print(s.index("fox"))         # 16  (like find, but raises ValueError if not found)
print(s.count("the"))         # 2

print(s.startswith("the"))    # True
print(s.endswith("dog"))      # True
print(s.startswith(("the", "a")))  # True (accepts tuple of prefixes)
```

### Check Methods

```python
print("hello".isalpha())       # True — all alphabetic
print("hello123".isalpha())    # False
print("12345".isdigit())       # True — all digits
print("hello123".isalnum())    # True — alphanumeric
print("   ".isspace())         # True — all whitespace
print("Hello World".istitle()) # True — title case
print("HELLO".isupper())       # True
print("hello".islower())       # True
```

### Split and Join

```python
# split — string to list
sentence = "the quick brown fox"
words = sentence.split()           # split on whitespace
# ['the', 'quick', 'brown', 'fox']

csv_line = "Alice,30,Berlin"
fields = csv_line.split(",")       # split on specific separator
# ['Alice', '30', 'Berlin']

# Limit splits
"a:b:c:d".split(":", 2)    # ['a', 'b', 'c:d']

# join — list to string (the reverse of split)
words = ["the", "quick", "brown", "fox"]
sentence = " ".join(words)         # "the quick brown fox"

# Common join patterns
", ".join(["a", "b", "c"])         # "a, b, c"
"\n".join(["line1", "line2"])      # "line1\nline2"
"".join(["p", "y", "t", "h"])     # "pyth"
```

### Replace

```python
text = "I like cats and cats like me"

new_text = text.replace("cats", "dogs")
# "I like dogs and dogs like me"

# Replace only first N occurrences
text.replace("cats", "dogs", 1)
# "I like dogs and cats like me"
```

### Padding and Alignment

```python
name = "Alice"
print(name.ljust(10))         # "Alice     "
print(name.rjust(10))         # "     Alice"
print(name.center(11))        # "   Alice   "
print(name.center(11, "-"))   # "---Alice---"
print("42".zfill(5))          # "00042"
```

---

## 3. f-Strings (Python 3.6+)

f-strings (formatted string literals) are the modern, preferred way to build strings with
embedded expressions. They are faster and more readable than older alternatives.

### Basic Syntax

```python
name = "Alice"
age = 30
city = "Berlin"

# f-string — prefix with f or F
greeting = f"Hello, {name}! You are {age} years old and live in {city}."
```

### Expressions Inside `{}`

Any valid Python expression works:

```python
x = 10
print(f"x squared is {x ** 2}")       # x squared is 100
print(f"x is {'even' if x % 2 == 0 else 'odd'}")   # x is even

items = ["apple", "banana", "cherry"]
print(f"First item: {items[0]}")       # First item: apple
print(f"Count: {len(items)}")          # Count: 3
```

### Format Specifications

```python
pi = 3.141592653589793

print(f"{pi:.2f}")        # 3.14     — 2 decimal places
print(f"{pi:.5f}")        # 3.14159  — 5 decimal places
print(f"{pi:10.2f}")      # "      3.14" — width 10, 2 decimal places
print(f"{pi:>10.2f}")     # right-align
print(f"{pi:<10.2f}")     # left-align
print(f"{pi:^10.2f}")     # center-align

n = 1234567
print(f"{n:,}")           # 1,234,567  — thousands separator
print(f"{n:_}")           # 1_234_567  — underscore separator

pct = 0.8523
print(f"{pct:.1%}")       # 85.2%  — percentage format

binary = 255
print(f"{binary:b}")      # 11111111  — binary
print(f"{binary:x}")      # ff         — hex lowercase
print(f"{binary:X}")      # FF         — hex uppercase
print(f"{binary:08b}")    # 11111111   — zero-padded binary
```

### f-String Debugging (Python 3.8+)

```python
x = 42
print(f"{x=}")        # x=42   — variable name + value
print(f"{x * 2=}")    # x * 2=84
```

### Multi-line f-Strings

```python
name = "Alice"
score = 95
letter = "A"

report = (
    f"Student: {name}\n"
    f"Score: {score}\n"
    f"Grade: {letter}"
)
```

---

## 4. String Slicing

String slicing uses the same `[start:stop:step]` syntax as lists.

```python
s = "Hello, Python!"
#    0123456789...

s[0]        # 'H'
s[7]        # 'P'
s[-1]       # '!'
s[0:5]      # 'Hello'
s[7:]       # 'Python!'
s[:5]       # 'Hello'
s[::2]      # 'Hlo yhn'   (every other character)
s[::-1]     # '!nohtyP ,olleH'  (reversed)
```

### Common Slicing Patterns

```python
filename = "report_2024.pdf"

# Extract parts
extension = filename[-3:]          # "pdf"
base_name = filename[:-4]          # "report_2024"
year = filename[7:11]              # "2024"

# Check if palindrome
word = "racecar"
is_palindrome = word == word[::-1]  # True
```

---

## 5. Multi-line Strings

```python
# Triple double quotes or triple single quotes
haiku = """
An old silent pond
A frog jumps into the pond
Splash! Silence again
"""

# Useful for SQL, HTML, long text
sql = """
    SELECT name, age
    FROM users
    WHERE age > 18
    ORDER BY name
"""

# Note: the first newline is included — strip if unwanted
clean_haiku = """An old silent pond
A frog jumps into the pond
Splash! Silence again"""
```

### Multi-line with Parentheses (Often Cleaner)

```python
message = (
    "This is a long message that "
    "spans multiple lines in the source "
    "but becomes a single string."
)
# Adjacent string literals are automatically concatenated at compile time
```

---

## 6. Raw Strings

A raw string (prefixed with `r`) treats backslashes as literal characters, not escape sequences.

```python
# Normal string — backslash is an escape
path = "C:\\Users\\Alice\\Documents"    # need to escape each backslash
pattern = "\\d+\\.\\d+"                 # ugly regex

# Raw string — backslash is literal
path = r"C:\Users\Alice\Documents"      # clean and readable
pattern = r"\d+\.\d+"                   # clean regex
```

### Escape Sequences in Normal Strings

| Sequence | Meaning |
|----------|---------|
| `\n` | Newline |
| `\t` | Tab |
| `\\` | Literal backslash |
| `\"` | Literal double quote |
| `\'` | Literal single quote |
| `\r` | Carriage return |
| `\0` | Null character |
| `\uXXXX` | Unicode character |

```python
print("Hello\nWorld")     # Hello (newline) World
print("Tab\there")        # Tab    here
print("Quote: \"hi\"")    # Quote: "hi"
```

---

## 7. String Encoding Basics

### Why Encoding Matters

Computers store text as numbers. **Encoding** is the mapping between characters and numbers.

- **ASCII**: 128 characters (English letters, digits, basic symbols). 1 byte each.
- **Latin-1 (ISO-8859-1)**: 256 characters. Western European languages.
- **UTF-8**: Encodes all Unicode characters. 1–4 bytes per character. Backward compatible with ASCII. **The modern standard.**
- **UTF-16**, **UTF-32**: Fixed or variable width alternatives.

### Python 3 and Unicode

Python 3 strings are Unicode internally. Encoding matters when reading/writing files or network data:

```python
# Encoding: str → bytes
text = "Hello, 日本語"
encoded = text.encode("utf-8")
print(encoded)   # b'Hello, \xe6\x97\xa5\xe6\x9c\xac\xe8\xaa\x9e'

# Decoding: bytes → str
decoded = encoded.decode("utf-8")
print(decoded)   # Hello, 日本語

# Common file I/O pattern — always specify encoding
with open("file.txt", "r", encoding="utf-8") as f:
    content = f.read()

with open("output.txt", "w", encoding="utf-8") as f:
    f.write("Hello, world!")
```

### The BOM Problem

UTF-8 files sometimes start with a Byte Order Mark (BOM) — a hidden character.
Use `encoding="utf-8-sig"` when reading files that might have a BOM (common with Excel exports).

---

## 8. String Formatting Patterns

Python has three string formatting systems. Know all three — you will encounter all three
in existing code.

### f-Strings (Modern — Use This)

```python
name, score = "Alice", 95.7
f"Student: {name}, Score: {score:.1f}"
# 'Student: Alice, Score: 95.7'
```

### str.format() (Older — Still Common)

```python
"Hello, {}! You are {} years old.".format("Alice", 30)
"Hello, {name}! You are {age} years old.".format(name="Alice", age=30)
"{0} + {1} = {2}".format(3, 4, 7)
"{:>10}".format("right")    # "     right"
"{:.2f}".format(3.14159)    # "3.14"
```

### % Formatting (Legacy — Avoid in New Code)

```python
"Hello, %s! You are %d years old." % ("Alice", 30)
"Pi is approximately %.2f" % 3.14159
```

### Template Strings (Special Use Case)

```python
from string import Template
t = Template("Hello, $name!")
t.substitute(name="Alice")    # 'Hello, Alice!'
```

Use `Template` when working with user-supplied format strings — safer than the others
because it cannot execute arbitrary expressions.

---

## Quick Reference

| Operation | Method/Syntax |
|-----------|---------------|
| Uppercase | `s.upper()` |
| Lowercase | `s.lower()` |
| Strip whitespace | `s.strip()` |
| Split to list | `s.split(sep)` |
| Join from list | `sep.join(lst)` |
| Replace | `s.replace(old, new)` |
| Find index | `s.find(sub)` |
| Count occurrences | `s.count(sub)` |
| Starts with | `s.startswith(prefix)` |
| Ends with | `s.endswith(suffix)` |
| Format (modern) | `f"{var:.2f}"` |
| Reverse | `s[::-1]` |
| Slice | `s[start:stop:step]` |
| Raw string | `r"no\escape"` |
| Encode | `s.encode("utf-8")` |
| Decode | `b.decode("utf-8")` |

---

## Key Takeaways

1. Strings are **immutable** — all methods return new strings.
2. **f-strings** are the modern, preferred way to format strings — use them.
3. Slice syntax `s[::-1]` reverses a string — know this pattern.
4. Use **raw strings** (`r"..."`) for file paths and regular expressions.
5. Always specify `encoding="utf-8"` when opening files.
6. `split()` and `join()` are complementary — they convert between strings and lists.
7. Python 3 strings are Unicode — you can use any character by default.

---

*Next: `theory/05-scope-and-namespaces.md` — how Python finds names and what "everything is an object" really means.*
