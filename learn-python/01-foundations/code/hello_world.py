# This is your very first Python program!
# Lines starting with # are comments — Python ignores them when running.
# Use comments to explain what your code does and why.
# Good programmers write comments for their future selves.

# ============================================================
# SECTION 1: Printing to the Screen
# ============================================================

# The print() function displays text on the screen.
# The text must be inside quotes — these are called "strings".
print("Hello, World!")

# You can use single quotes or double quotes — both work.
# Convention: be consistent. Most Python code uses double quotes.
print("Welcome to Python!")
print('Single quotes also work.')

# print() always moves to a new line after output.
# To print multiple things, separate them with commas.
print("First", "Second", "Third")    # Output: First Second Third

# print() with no arguments prints a blank line — useful for spacing.
print()

# ============================================================
# SECTION 2: Variables
# ============================================================

# Variables store information so you can use it later.
# In Python, you do NOT need to declare the type — just assign a value.
# Python figures out the type automatically (this is called "dynamic typing").

name = "Learner"         # This is a variable named 'name' storing a string
age = 25                 # This variable stores an integer (whole number)
height = 5.9             # This variable stores a float (decimal number)
is_learning = True       # This variable stores a boolean (True or False)

# Variable names should be descriptive and use_underscores (snake_case).
# Good: first_name, user_age, total_price
# Bad: x, fn, tp (too cryptic)
# Bad: firstName, userAge (that is Java style — Python uses underscores)

print(name)              # Prints: Learner
print(age)               # Prints: 25

# ============================================================
# SECTION 3: f-strings (Formatted Strings)
# ============================================================

# f-strings let you embed variables directly inside a string.
# Put f before the opening quote: f"..."
# Then use {variable_name} to insert a variable's value.

print(f"Hello, {name}!")                           # Hello, Learner!
print(f"You are {age} years old.")                 # You are 25 years old.
print(f"You are {name} and you are {age} years old and learning Python!")

# You can put expressions (calculations) inside the curly braces:
print(f"In 10 years, you will be {age + 10}.")    # In 10 years, you will be 35.
print(f"2 + 2 = {2 + 2}")                          # 2 + 2 = 4

# This is much cleaner than the old way (string concatenation):
# Old way (avoid this):
print("Hello, " + name + "! You are " + str(age) + " years old.")  # Ugly!
# New way (use f-strings):
print(f"Hello, {name}! You are {age} years old.")  # Clean!

# ============================================================
# SECTION 4: Reassigning Variables
# ============================================================

# Variables can be reassigned at any time — the old value is replaced.
score = 0
print(f"Starting score: {score}")

score = 100
print(f"Score after winning: {score}")

# You can even change the type — Python allows this (though it's usually bad practice).
x = 42              # x is an int
x = "now a string"  # x is now a string — Python is fine with this
print(x)

# ============================================================
# SECTION 5: A Complete Mini-Example
# ============================================================

print()  # blank line for spacing
print("=== Welcome to Python! ===")

user_name = "Alice"
birth_year = 1998
current_year = 2025
user_age = current_year - birth_year

print(f"Name: {user_name}")
print(f"Birth year: {birth_year}")
print(f"Age in {current_year}: {user_age}")
print(f"Hello, {user_name}! Welcome to the Python learning journey.")
