"""
strings_demo.py — Demonstrations of all Python string concepts.

Run with:  python code/strings_demo.py

Covers:
  - String properties (immutability, Unicode)
  - All important string methods
  - f-strings with format specifications
  - String slicing
  - Multi-line strings
  - Raw strings
  - Encoding basics
  - The three formatting systems: f-strings, .format(), %
"""

# ─────────────────────────────────────────────────────────────
# SECTION 1: String Basics
# ─────────────────────────────────────────────────────────────
print("=" * 50)
print("SECTION 1: String Basics")
print("=" * 50)

# Different quote styles
s1 = 'Single quotes'
s2 = "Double quotes"
s3 = "It's easier with double quotes here"
s4 = 'He said "hello" to her'
s5 = """Triple-quoted:
spans multiple lines"""

print(f"s1: {s1}")
print(f"s2: {s2}")
print(f"s3: {s3}")
print(f"s4: {s4}")
print(f"s5: {s5}")

# Strings are sequences
word = "Python"
print(f"\nString: '{word}'")
print(f"  Length:    {len(word)}")
print(f"  First:     {word[0]}")
print(f"  Last:      {word[-1]}")
print(f"  'th' in word: {'th' in word}")

# Strings are immutable
try:
    word[0] = "p"
except TypeError as e:
    print(f"\nImmutability: {e}")

# Unicode support
multilingual = ["Hello", "Héllo", "こんにちは", "مرحبا", "🐍"]
for s in multilingual:
    print(f"  '{s}'  (len={len(s)})")


# ─────────────────────────────────────────────────────────────
# SECTION 2: Case Methods
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 2: Case Methods")
print("=" * 50)

text = "hello, World! 123"
print(f"Original:    '{text}'")
print(f"upper():     '{text.upper()}'")
print(f"lower():     '{text.lower()}'")
print(f"title():     '{text.title()}'")
print(f"capitalize():'{text.capitalize()}'")
print(f"swapcase():  '{text.swapcase()}'")

# Case comparison
print(f"\n'hello'.isupper(): {'hello'.isupper()}")
print(f"'HELLO'.isupper(): {'HELLO'.isupper()}")
print(f"'Hello'.istitle(): {'Hello'.istitle()}")


# ─────────────────────────────────────────────────────────────
# SECTION 3: Whitespace and Strip
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 3: Whitespace and Strip")
print("=" * 50)

padded = "   hello world   "
print(f"Original: '{padded}'")
print(f"strip():  '{padded.strip()}'")
print(f"lstrip(): '{padded.lstrip()}'")
print(f"rstrip(): '{padded.rstrip()}'")

# Strip specific characters
special = "***:::hello:::***"
print(f"\n'{special}'.strip('*:'): '{special.strip('*:')}'")

# isspace
print(f"\n'   '.isspace(): {'   '.isspace()}")
print(f"'  a  '.isspace(): {'  a  '.isspace()}")


# ─────────────────────────────────────────────────────────────
# SECTION 4: Search Methods
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 4: Search Methods")
print("=" * 50)

sentence = "the quick brown fox jumps over the lazy dog"
print(f"Sentence: '{sentence}'")

print(f"\nfind('fox'):      {sentence.find('fox')}")
print(f"find('cat'):      {sentence.find('cat')}  ← -1 means not found")
print(f"rfind('the'):     {sentence.rfind('the')}  ← rightmost")
print(f"count('the'):     {sentence.count('the')}")
print(f"startswith('the'):{sentence.startswith('the')}")
print(f"endswith('dog'):  {sentence.endswith('dog')}")

# Checking multiple prefixes/suffixes
filename = "data_report.csv"
is_data_file = filename.endswith((".csv", ".xlsx", ".json"))
print(f"\n'{filename}'.endswith(('.csv', '.xlsx', '.json')): {is_data_file}")


# ─────────────────────────────────────────────────────────────
# SECTION 5: Split and Join
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 5: Split and Join")
print("=" * 50)

# split() — string to list
sentence = "the quick brown fox"
words = sentence.split()
print(f"split() on whitespace: {words}")

csv_line = "Alice,30,Berlin,engineer"
fields = csv_line.split(",")
print(f"split(','):            {fields}")

# Split with limit
"a:b:c:d".split(":", 2)
print(f"'a:b:c:d'.split(':', 2): {'a:b:c:d'.split(':', 2)}")

# splitlines() — split on newlines
multiline = "line 1\nline 2\nline 3"
lines = multiline.splitlines()
print(f"\nsplitlines(): {lines}")

# join() — list to string (the reverse of split)
words = ["the", "quick", "brown", "fox"]
print(f"\njoin examples:")
print(f"  ' '.join:  '{' '.join(words)}'")
print(f"  '-'.join:  '{'-'.join(words)}'")
print(f"  ''.join:   '{''.join(words)}'")
print(f"  ', '.join: '{', '.join(words)}'")

# Round-trip: split then join
csv = "Alice,Bob,Carol"
names = csv.split(",")
rejoined = " | ".join(names)
print(f"\n  CSV:        '{csv}'")
print(f"  Split:      {names}")
print(f"  Re-joined:  '{rejoined}'")


# ─────────────────────────────────────────────────────────────
# SECTION 6: Replace and Check Methods
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 6: Replace and Check Methods")
print("=" * 50)

text = "I like cats and cats like me"
print(f"Original:         '{text}'")
print(f"replace all:      '{text.replace('cats', 'dogs')}'")
print(f"replace 1 only:   '{text.replace('cats', 'dogs', 1)}'")

# Check methods
test_strings = ["hello", "Hello123", "12345", "  ", "", "Hello World"]
print(f"\nCheck method results:")
print(f"{'String':20} {'isalpha':8} {'isdigit':8} {'isalnum':8} {'isspace':8}")
print("-" * 55)
for s in test_strings:
    print(f"{repr(s):20} {str(s.isalpha()):8} {str(s.isdigit()):8} "
          f"{str(s.isalnum()):8} {str(s.isspace()):8}")


# ─────────────────────────────────────────────────────────────
# SECTION 7: f-Strings
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 7: f-Strings")
print("=" * 50)

name = "Alice"
age = 30
score = 0.8567
big_number = 1234567.89
pi = 3.141592653589793

# Basic usage
print(f"Hello, {name}! You are {age} years old.")

# Expressions
print(f"  5 squared = {5 ** 2}")
print(f"  Upper: {name.upper()}")
print(f"  Ternary: {'adult' if age >= 18 else 'minor'}")

# Format specifications
print(f"\nFloat formatting:")
print(f"  pi:          {pi}")
print(f"  pi (2dp):    {pi:.2f}")
print(f"  pi (5dp):    {pi:.5f}")
print(f"  pi (10.4f):  {pi:10.4f}")
print(f"  score (pct): {score:.1%}")

print(f"\nInteger formatting:")
n = 255
print(f"  decimal:     {n:d}")
print(f"  binary:      {n:b}")
print(f"  octal:       {n:o}")
print(f"  hex lower:   {n:x}")
print(f"  hex upper:   {n:X}")
print(f"  hex padded:  {n:08x}")

print(f"\nAlignment and padding:")
for item in ["left", "center", "right"]:
    print(f"  '<': '{item:<10}' | '^': '{item:^10}' | '>': '{item:>10}'")

print(f"\nNumber formatting:")
print(f"  comma:      {big_number:,.2f}")
print(f"  underscore: {big_number:_.2f}")
print(f"  scientific: {big_number:.3e}")

# Debug: = format (Python 3.8+)
x = 42
print(f"\nDebug format: {x=}")
print(f"Computed:     {x * 3 + 1=}")


# ─────────────────────────────────────────────────────────────
# SECTION 8: String Slicing
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 8: String Slicing")
print("=" * 50)

s = "Hello, Python!"
print(f"String: '{s}'  (length {len(s)})")
print(f"  s[0]     = '{s[0]}'")
print(f"  s[-1]    = '{s[-1]}'")
print(f"  s[0:5]   = '{s[0:5]}'")
print(f"  s[7:]    = '{s[7:]}'")
print(f"  s[:5]    = '{s[:5]}'")
print(f"  s[::2]   = '{s[::2]}'")
print(f"  s[::-1]  = '{s[::-1]}'  ← reversed")

# Practical slicing
filename = "report_2024_final.csv"
extension = filename[-3:]
base = filename[:-4]
year = filename[7:11]
print(f"\nFilename: '{filename}'")
print(f"  Extension: '{extension}'")
print(f"  Base name: '{base}'")
print(f"  Year:      '{year}'")

# Palindrome check
words_to_check = ["racecar", "level", "hello", "madam", "python"]
print(f"\nPalindrome check:")
for word in words_to_check:
    is_pal = word == word[::-1]
    print(f"  '{word}' → {'palindrome' if is_pal else 'not palindrome'}")


# ─────────────────────────────────────────────────────────────
# SECTION 9: Raw Strings
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 9: Raw Strings")
print("=" * 50)

# Normal strings interpret backslashes
normal = "C:\\Users\\Alice\\Documents"
print(f"Normal string (escaped):  '{normal}'")

# Raw strings treat backslash as literal
raw = r"C:\Users\Alice\Documents"
print(f"Raw string:               '{raw}'")

# Same result — raw is just easier to write
print(f"Are they equal: {normal == raw}")

# Essential for regex
import re

text = "My phone: 123-456-7890 and backup: 098-765-4321"
phone_pattern = r"\d{3}-\d{3}-\d{4}"   # raw string for regex
phones = re.findall(phone_pattern, text)
print(f"\nPhone numbers found: {phones}")


# ─────────────────────────────────────────────────────────────
# SECTION 10: Encoding Basics
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 10: Encoding Basics")
print("=" * 50)

text = "Hello, Python! 🐍"

# Encode to bytes
utf8_bytes = text.encode("utf-8")
print(f"Original string:   '{text}'")
print(f"Encoded (UTF-8):   {utf8_bytes}")
print(f"Byte length:       {len(utf8_bytes)}")
print(f"String length:     {len(text)}")  # fewer chars than bytes (emoji is multi-byte)

# Decode back
decoded = utf8_bytes.decode("utf-8")
print(f"Decoded back:      '{decoded}'")
print(f"Round-trip ok:     {text == decoded}")

# Different encodings
try:
    ascii_bytes = text.encode("ascii")
except UnicodeEncodeError as e:
    print(f"\nASCII cannot encode emoji: {e}")

# Handling encoding errors
print(f"\nWith error handling:")
safe = text.encode("ascii", errors="ignore").decode("ascii")
print(f"  ignore:  '{safe}'")
replaced = text.encode("ascii", errors="replace").decode("ascii")
print(f"  replace: '{replaced}'")


# ─────────────────────────────────────────────────────────────
# SECTION 11: Formatting Systems Compared
# ─────────────────────────────────────────────────────────────
print("\n" + "=" * 50)
print("SECTION 11: Three Formatting Systems")
print("=" * 50)

name, score, pi_val = "Alice", 95.678, 3.14159

# 1. f-strings (Python 3.6+) — PREFERRED
result1 = f"{name} scored {score:.1f} (pi ≈ {pi_val:.3f})"
print(f"f-string:    {result1}")

# 2. str.format() — older but still common
result2 = "{} scored {:.1f} (pi ≈ {:.3f})".format(name, score, pi_val)
print(f"str.format:  {result2}")

# Named placeholders with .format()
result3 = "{name} scored {score:.1f}".format(name=name, score=score)
print(f"Named .fmt:  {result3}")

# 3. % formatting — legacy, avoid in new code
result4 = "%s scored %.1f (pi ≈ %.3f)" % (name, score, pi_val)
print(f"% format:    {result4}")


print("\n--- Demo Complete ---")
