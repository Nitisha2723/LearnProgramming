# Data Types in Python
# ============================================================
# Python has built-in types for different kinds of data.
# Understanding types is fundamental — every value in Python has a type,
# and the type determines what operations you can perform on a value.

# ============================================================
# SECTION 1: int (Integer) — Whole Numbers
# ============================================================

# Integers are whole numbers — positive, negative, or zero.
# No decimal point.

age = 25
temperature = -10
year = 2025
population = 8_000_000_000    # Underscores make large numbers readable
zero = 0

print("=== Integers ===")
print(age)                     # 25
print(temperature)             # -10
print(population)              # 8000000000

# Python integers can be arbitrarily large — unlike most languages.
# No overflow! Python handles numbers of any size.
huge_number = 10 ** 100       # 10 to the power of 100 (a googol!)
print(huge_number)             # Python prints all 101 digits!

# Check the type with type()
print(type(age))               # <class 'int'>
print(type(huge_number))       # <class 'int'>

# ============================================================
# SECTION 2: float (Floating Point) — Decimal Numbers
# ============================================================

# Floats are numbers with a decimal point.
# "Floating point" refers to the way they are stored in binary memory.

price = 9.99
pi = 3.14159
body_temp = 98.6
negative_float = -0.5

print("\n=== Floats ===")
print(price)           # 9.99
print(pi)              # 3.14159
print(type(pi))        # <class 'float'>

# IMPORTANT: Floating point has precision limits
# This is not a Python bug — it is how binary floating point works in all languages.
print(0.1 + 0.2)       # 0.30000000000000004 (not exactly 0.3!)

# For financial calculations, use the decimal module instead:
from decimal import Decimal
print(Decimal('0.1') + Decimal('0.2'))   # Exactly 0.3

# Scientific notation in floats:
speed_of_light = 3e8          # 3 × 10^8 = 300000000.0
electron_mass = 9.1e-31       # 9.1 × 10^-31
print(speed_of_light)         # 300000000.0
print(type(speed_of_light))   # <class 'float'>

# ============================================================
# SECTION 3: str (String) — Text
# ============================================================

# Strings are sequences of characters — text data.
# They must be enclosed in quotes (single or double).

greeting = "Hello, World!"
name = 'Alice'                     # Single quotes also fine
empty_string = ""                  # A string can be empty
multiline = """This is a
multi-line string.
It spans multiple lines."""

print("\n=== Strings ===")
print(greeting)
print(name)
print(empty_string)         # prints nothing (blank line)
print(multiline)

# Strings have many useful built-in methods (operations)
message = "  Hello, Python!  "
print(message.upper())              # "  HELLO, PYTHON!  "
print(message.lower())              # "  hello, python!  "
print(message.strip())              # "Hello, Python!"  (removes whitespace)
print(message.strip().replace("Python", "World"))  # "Hello, World!"

# String length
print(len("Python"))        # 6
print(len(""))              # 0

# String indexing (accessing individual characters)
word = "Python"
print(word[0])              # 'P'  (first character, index 0)
print(word[-1])             # 'n'  (last character, index -1)
print(word[1:4])            # 'yth' (slice: characters 1, 2, 3)

# Check if a string contains another string:
print("Py" in "Python")     # True
print("Java" in "Python")   # False

print(type(greeting))       # <class 'str'>

# ============================================================
# SECTION 4: bool (Boolean) — True or False
# ============================================================

# Booleans represent logical true/false values.
# They are the result of comparisons and conditions.
# Note: True and False must be capitalized in Python.

is_raining = True
is_sunny = False
has_account = True

print("\n=== Booleans ===")
print(is_raining)       # True
print(is_sunny)         # False
print(type(is_raining)) # <class 'bool'>

# Booleans come from comparisons:
print(10 > 5)           # True
print(10 < 5)           # False
print(10 == 10)         # True (== is "equal to", = is assignment)
print(10 != 5)          # True (!= is "not equal to")

# Every value in Python has a "truthiness" (bool() converts to bool):
print(bool(1))          # True
print(bool(0))          # False
print(bool("hello"))    # True  (non-empty string is truthy)
print(bool(""))         # False (empty string is falsy)
print(bool([1, 2, 3]))  # True  (non-empty list is truthy)
print(bool([]))         # False (empty list is falsy)
print(bool(None))       # False (None is always falsy)

# Falsy values in Python: 0, 0.0, "", [], {}, (), set(), None, False
# Everything else is truthy

# ============================================================
# SECTION 5: None — The Absence of a Value
# ============================================================

# None represents the absence of a value.
# It is Python's equivalent of "null" in Java or "undefined" in JavaScript.
# There is only ONE None value in all of Python — it is a singleton.

result = None
username = None      # User has not provided a username yet

print("\n=== None ===")
print(result)                   # None
print(type(None))               # <class 'NoneType'>
print(type(result))             # <class 'NoneType'>

# Check for None with 'is' (not ==)
print(result is None)           # True   (correct way)
print(result == None)           # True   (works but style guide prefers 'is')

# Functions that do not return a value actually return None:
def say_hi():
    print("Hi!")                # This function returns nothing

returned = say_hi()             # Calls the function — prints "Hi!"
print(returned)                 # None — that is what functions with no return give you
print(returned is None)         # True

# ============================================================
# SECTION 6: The type() Function
# ============================================================

# type() tells you the type of any value
print("\n=== type() function ===")
print(type(42))         # <class 'int'>
print(type(3.14))       # <class 'float'>
print(type("hello"))    # <class 'str'>
print(type(True))       # <class 'bool'>
print(type(None))       # <class 'NoneType'>

# Useful for debugging — when you are not sure what type a variable is:
mystery = 42 / 2         # What type is this? int or float?
print(type(mystery))     # <class 'float'>  — division always returns float!

# ============================================================
# SECTION 7: Dynamic Typing
# ============================================================

# In Python, variables do not have types — VALUES have types.
# A variable is just a name pointing to a value.
# You can reassign a variable to a value of a different type.

print("\n=== Dynamic Typing ===")
x = 42
print(f"x = {x}, type = {type(x)}")     # x = 42, type = <class 'int'>

x = "now a string"
print(f"x = {x}, type = {type(x)}")     # x = now a string, type = <class 'str'>

x = 3.14
print(f"x = {x}, type = {type(x)}")     # x = 3.14, type = <class 'float'>

# This is very different from Java:
# Java:   int x = 42;   x = "string";  // COMPILE ERROR
# Python: x = 42        x = "string"   # Fine!

# While Python allows this, reassigning different types to the same variable
# is usually a sign of confusing code. Avoid it in practice.

# ============================================================
# SECTION 8: Type Conversion (Casting)
# ============================================================

# You can convert between types using int(), float(), str(), bool()

print("\n=== Type Conversion ===")

# String to int:
age_string = "25"
age_int = int(age_string)
print(age_int + 5)              # 30 (works now — we converted to int)
print(type(age_int))            # <class 'int'>

# String to float:
price_string = "9.99"
price_float = float(price_string)
print(price_float * 2)          # 19.98

# Int to string:
number = 42
number_string = str(number)
print("The answer is " + number_string)   # "The answer is 42"

# Float to int (truncates — drops the decimal, does NOT round):
pi = 3.99
print(int(pi))                  # 3 (not 4! truncation, not rounding)
print(int(-3.7))                # -3 (toward zero, not -4)

# Be careful — not all conversions work:
# int("hello")    # ValueError: invalid literal for int() with base 10: 'hello'
# int("3.14")     # ValueError — int() cannot handle a string with a decimal!
# float("3.14")   # Works fine → 3.14

# Convert via float first:
decimal_string = "3.14"
result = int(float(decimal_string))  # "3.14" → 3.14 → 3
print(result)                         # 3

# ============================================================
# SECTION 9: Checking Types with isinstance()
# ============================================================

# isinstance() is a more Pythonic way to check types in real code.
# It is preferred over type() for type checking.

print("\n=== isinstance() ===")
x = 42
print(isinstance(x, int))          # True
print(isinstance(x, float))        # False
print(isinstance(x, (int, float))) # True — can check multiple types at once!
print(isinstance("hello", str))    # True
print(isinstance(True, bool))      # True
print(isinstance(True, int))       # Also True! bool is a subclass of int in Python

# Why isinstance() over type()?
# isinstance() handles inheritance correctly.
# True is a bool AND an int — isinstance catches this, type() == only sees bool.
