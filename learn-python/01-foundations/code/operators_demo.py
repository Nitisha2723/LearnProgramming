# Operators in Python
# ============================================================
# Operators are symbols that perform operations on values.
# Python has arithmetic, comparison, logical, assignment, and more.

# ============================================================
# SECTION 1: Arithmetic Operators
# ============================================================

print("=== Arithmetic Operators ===")

a = 17
b = 5

# Addition
print(f"{a} + {b} = {a + b}")          # 17 + 5 = 22

# Subtraction
print(f"{a} - {b} = {a - b}")          # 17 - 5 = 12

# Multiplication
print(f"{a} * {b} = {a * b}")          # 17 * 5 = 85

# True Division — ALWAYS returns a float in Python 3
print(f"{a} / {b} = {a / b}")          # 17 / 5 = 3.4
print(f"10 / 2 = {10 / 2}")            # 10 / 2 = 5.0  (float, not 5!)

# Integer Division (Floor Division) — divides and rounds DOWN to the nearest int
print(f"{a} // {b} = {a // b}")        # 17 // 5 = 3  (3 * 5 = 15, remainder 2)
print(f"17 // -5 = {17 // -5}")        # -4 (floors toward negative infinity, not zero!)
print(f"-17 // 5 = {-17 // 5}")        # -4

# Modulo — the remainder after division
print(f"{a} % {b} = {a % b}")          # 17 % 5 = 2  (17 = 3*5 + 2)
print(f"10 % 3 = {10 % 3}")            # 1  (10 = 3*3 + 1)
print(f"15 % 5 = {15 % 5}")            # 0  (15 = 3*5 + 0, evenly divisible)

# Key use of modulo: check if a number is even or odd
number = 42
if number % 2 == 0:
    print(f"{number} is even")
else:
    print(f"{number} is odd")

# Exponentiation — raise to a power
print(f"2 ** 10 = {2 ** 10}")          # 2 ** 10 = 1024
print(f"3 ** 3 = {3 ** 3}")            # 3 ** 3 = 27
print(f"9 ** 0.5 = {9 ** 0.5}")        # 9 ** 0.5 = 3.0 (square root!)
print(f"2 ** -1 = {2 ** -1}")          # 2 ** -1 = 0.5

print()

# ============================================================
# SECTION 2: Python-Specific Arithmetic Notes
# ============================================================

print("=== Division Nuances ===")

# Python 3 vs Python 2 IMPORTANT DIFFERENCE:
# Python 2: 7 / 2 = 3  (integer division was the default!)
# Python 3: 7 / 2 = 3.5 (true division is always the default)
# This was a major reason Python 3 broke backward compatibility.

print(f"7 / 2 = {7 / 2}")              # 3.5   (true division, always float)
print(f"7 // 2 = {7 // 2}")            # 3     (integer division, always int)
print(f"type(7 / 2) = {type(7 / 2)}") # <class 'float'>
print(f"type(7 // 2) = {type(7 // 2)}") # <class 'int'>

# Practical example: converting minutes to hours and minutes
total_minutes = 137
hours = total_minutes // 60         # How many complete hours?
remaining_minutes = total_minutes % 60  # Leftover minutes?
print(f"{total_minutes} minutes = {hours} hours and {remaining_minutes} minutes")

print()

# ============================================================
# SECTION 3: Comparison Operators
# ============================================================
# Comparison operators always return a boolean: True or False

print("=== Comparison Operators ===")

x = 10
y = 20

# Equal to (==) — note: NOT the same as = (assignment)
print(f"{x} == {y} → {x == y}")        # False
print(f"{x} == 10 → {x == 10}")        # True

# Not equal to
print(f"{x} != {y} → {x != y}")        # True

# Greater than
print(f"{x} > {y} → {x > y}")          # False
print(f"{y} > {x} → {y > x}")          # True

# Less than
print(f"{x} < {y} → {x < y}")          # True

# Greater than or equal to
print(f"{x} >= 10 → {x >= 10}")        # True (equal counts)
print(f"{x} >= 11 → {x >= 11}")        # False

# Less than or equal to
print(f"{x} <= 10 → {x <= 10}")        # True

# Python allows chained comparisons — unique and very readable!
age = 25
print(f"18 < age < 65 → {18 < age < 65}")  # True — works like math notation!
print(f"1 < 2 < 3 < 4 → {1 < 2 < 3 < 4}") # True

# Comparing strings (alphabetical / lexicographic order)
print(f'"apple" < "banana" → {"apple" < "banana"}')   # True (a comes before b)
print(f'"zebra" > "ant" → {"zebra" > "ant"}')          # True (z comes after a)

# COMMON MISTAKE: == vs is
# == checks if values are equal
# is  checks if they are the SAME object in memory
a = [1, 2, 3]
b = [1, 2, 3]
print(f"a == b → {a == b}")     # True (same values)
print(f"a is b → {a is b}")     # False (different objects in memory)
# Use == for value comparison (almost always what you want)
# Use 'is' ONLY for None checks: x is None

print()

# ============================================================
# SECTION 4: Logical Operators
# ============================================================
# Combine multiple conditions

print("=== Logical Operators ===")

# AND — True only if BOTH sides are True
print(f"True and True → {True and True}")    # True
print(f"True and False → {True and False}")  # False
print(f"False and True → {False and True}")  # False
print(f"False and False → {False and False}") # False

# Practical example:
age = 25
has_id = True
can_enter = age >= 18 and has_id
print(f"Can enter (age >= 18 and has ID): {can_enter}")  # True

# OR — True if AT LEAST ONE side is True
print(f"True or False → {True or False}")     # True
print(f"False or False → {False or False}")   # False

is_weekend = False
is_holiday = True
can_sleep_in = is_weekend or is_holiday
print(f"Can sleep in (weekend or holiday): {can_sleep_in}")  # True

# NOT — flips True to False, False to True
print(f"not True → {not True}")              # False
print(f"not False → {not False}")            # True

is_raining = False
going_outside = not is_raining               # Go outside if it is NOT raining
print(f"Going outside: {going_outside}")     # True

# Combining all three:
user_age = 20
is_member = True
is_banned = False
access_granted = user_age >= 18 and is_member and not is_banned
print(f"Access granted: {access_granted}")   # True

print()

# ============================================================
# SECTION 5: Short-Circuit Evaluation
# ============================================================
# Python is "lazy" — it stops evaluating as soon as the result is determined.
# and: if the left side is False, the right side is NOT evaluated
# or:  if the left side is True,  the right side is NOT evaluated

print("=== Short-Circuit Evaluation ===")

def always_true():
    print("  → always_true() was called")
    return True

def always_false():
    print("  → always_false() was called")
    return False

# With 'and': if first is False, second is never called
print("False and always_true():")
result = False and always_true()       # always_true() is never called!
print(f"Result: {result}")

# With 'or': if first is True, second is never called
print("True or always_false():")
result = True or always_false()        # always_false() is never called!
print(f"Result: {result}")

# Practical use: safe attribute access
user = None
# If user is None, the second part is never evaluated (avoids AttributeError):
name = user and user.get("name")       # user is falsy → name = None (safe!)
print(f"Name: {name}")                 # None

# Setting defaults with or:
provided_name = ""                     # User provided empty string (falsy)
display_name = provided_name or "Anonymous"  # Empty string is falsy, so use default
print(f"Display name: {display_name}") # Anonymous

print()

# ============================================================
# SECTION 6: Assignment Operators
# ============================================================

print("=== Assignment Operators ===")

# Basic assignment
x = 10
print(f"x = {x}")

# Augmented assignment — shorthand for x = x + 5
x += 5                   # x = x + 5 = 15
print(f"After x += 5: x = {x}")

x -= 3                   # x = x - 3 = 12
print(f"After x -= 3: x = {x}")

x *= 2                   # x = x * 2 = 24
print(f"After x *= 2: x = {x}")

x //= 5                  # x = x // 5 = 4
print(f"After x //= 5: x = {x}")

x **= 3                  # x = x ** 3 = 64
print(f"After x **= 3: x = {x}")

x %= 10                  # x = x % 10 = 4
print(f"After x %= 10: x = {x}")

# All augmented assignment operators:
# +=   -=   *=   /=   //=   %=   **=

# String augmented assignment:
greeting = "Hello"
greeting += ", World!"           # greeting = greeting + ", World!"
print(greeting)                   # Hello, World!

print()

# ============================================================
# SECTION 7: The Walrus Operator (:=)
# ============================================================
# Introduced in Python 3.8. Assigns AND returns a value at the same time.
# Also called the "assignment expression" operator.

print("=== Walrus Operator (:=) ===")

# Traditional way — get input and check in separate steps:
# (We will simulate user input here since we cannot use input() in a demo)
data = "Python"
n = len(data)
if n > 3:
    print(f"Long string: {n} characters")

# With walrus operator — assign and check in one step:
data = "Python"
if (n := len(data)) > 3:           # := assigns len(data) to n AND uses it in the condition
    print(f"Long string: {n} characters")  # n is available here!

# Practical use: processing items until empty
import random
items = [5, 3, 8, 1, 9, 2, 7]     # simulating data
while (item := items.pop() if items else None) is not None:
    print(f"Processing: {item}")

print()

# ============================================================
# SECTION 8: Operator Precedence
# ============================================================
# When multiple operators appear in one expression, Python follows rules about
# which to evaluate first — just like math (PEMDAS/BODMAS).
# Order (highest to lowest):
#   ** (exponentiation)
#   +x, -x (unary positive/negative)
#   *, /, //, % (multiplication, division)
#   +, - (addition, subtraction)
#   ==, !=, <, >, <=, >= (comparison)
#   not (logical NOT)
#   and (logical AND)
#   or (logical OR)

print("=== Operator Precedence ===")

# Without parentheses — Python follows precedence rules:
result = 2 + 3 * 4       # Multiplication first: 2 + 12 = 14
print(f"2 + 3 * 4 = {result}")     # 14

result = (2 + 3) * 4     # Parentheses override: 5 * 4 = 20
print(f"(2 + 3) * 4 = {result}")   # 20

result = 2 ** 3 ** 2     # Right-associative: 2 ** (3 ** 2) = 2 ** 9 = 512
print(f"2 ** 3 ** 2 = {result}")   # 512 (NOT (2**3)**2 = 64!)

# When in doubt, use parentheses — they make intent explicit and clear.
# Even if the precedence rules would give you the right answer,
# parentheses help readers understand your intent immediately.
print(f"Clear version: 2 ** (3 ** 2) = {2 ** (3 ** 2)}")
