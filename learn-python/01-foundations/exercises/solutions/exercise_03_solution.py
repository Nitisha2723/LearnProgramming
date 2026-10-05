# SOLUTION: Exercise 3 - Calculator
# ============================================================
# This is the solution. Review it AFTER you have completed the exercise yourself.
# ============================================================

# Solution to TODO 1: Create the numbers
num1 = 17
num2 = 5

# Solution to TODO 2: Print the header
print("===== Python Calculator =====")
print(f"Number 1: {num1}")
print(f"Number 2: {num2}")
print("============================")

# Solution to TODO 3: All arithmetic operations
print(f"{num1} + {num2} = {num1 + num2}")        # Addition
print(f"{num1} - {num2} = {num1 - num2}")        # Subtraction
print(f"{num1} * {num2} = {num1 * num2}")        # Multiplication
print(f"{num1} / {num2} = {num1 / num2}")        # True division
print(f"{num1} // {num2} = {num1 // num2}     (integer division)")   # Integer division
print(f"{num1} % {num2} = {num1 % num2}      (remainder)")           # Modulo
print(f"{num1} ** {num2} = {num1 ** num2}   (exponentiation)")       # Exponentiation
print("============================")

# Solution to TODO 4: Odd or even check
if num1 % 2 == 0:
    print(f"{num1} is EVEN")
else:
    print(f"{num1} is ODD")

# Solution to TODO 5: Division result and further division
division_result = num1 / num2
print(f"{division_result} divided by 2 = {division_result / 2}")

# Solution to TODO 6: Type checking
print(f"True division result type: {type(num1 / num2)}")
print(f"Integer division result type: {type(num1 // num2)}")

# ============================================================
# NOTES:
# - True division (/) ALWAYS returns a float in Python 3, even when the result
#   is a whole number: 10 / 2 = 5.0, not 5.
# - Integer division (//) ALWAYS returns an int when both operands are ints.
# - Modulo (%) is extremely useful — checking even/odd is one of the most common
#   uses, but you will use it for many things: wrapping around lists, time calculations,
#   hashing, etc.
# - Exponentiation (**) is right-associative: 2 ** 3 ** 2 = 2 ** (3 ** 2) = 512
#
# EXTENSION CHALLENGES (optional):
# 1. What happens if num2 = 0? Try it and read the error message.
# 2. Add code that handles division by zero gracefully (hint: check if num2 == 0 first).
# 3. What if both numbers are floats? Change num1 = 17.5 and num2 = 2.5 — do all
#    operations still work as expected?
# 4. Add a calculation showing the absolute value of a negative result: abs(-5) = 5
