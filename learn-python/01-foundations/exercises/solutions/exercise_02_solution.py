# SOLUTION: Exercise 2 - Variables and Data Types
# ============================================================
# This is the solution. Review it AFTER you have completed the exercise yourself.
# ============================================================

# Solution to TODO 1: Create the variables
name = "Alex Johnson"
age = 28
height = 5.8
is_student = True
grade = "A"

# Solution to TODO 2: Print the formatted profile
print("===== Personal Profile =====")
print(f"Name: {name}")
print(f"Age: {age}")
print(f"Height: {height} feet")
print(f"Student: {is_student}")
print(f"Grade: {grade}")
print("===========================")
print(f"{name} is {age} years old.")
print(f"{name} is {height} feet tall.")
print(f"{name}'s current grade is {grade}.")
print(f"Is {name} a student? {is_student}")

# Solution to TODO 3: Print variable types
print(f"Type of name: {type(name)}")
print(f"Type of age: {type(age)}")
print(f"Type of height: {type(height)}")
print(f"Type of is_student: {type(is_student)}")
print(f"Type of grade: {type(grade)}")

# Solution to TODO 4: Convert age to string
age_as_string = str(age)
print(f"Age as string: '{age_as_string}'")

# Solution to TODO 5: Grade point conversion
grade_points = 4.0 if grade == "A" else 0.0
print(f"In decimal form, {name}'s grade points are: {grade_points}")

# ============================================================
# NOTES:
# - Notice how we use an apostrophe in "Alex's" inside double-quoted f-strings.
#   This works because the outer string uses double quotes — the single quote
#   inside is just a character.
# - The ternary expression (4.0 if grade == "A" else 0.0) is covered fully in
#   Module 02. For now, know that it is a one-line if/else.
# - type() returns a class object. When printed in an f-string, it shows as
#   <class 'int'>, <class 'str'>, etc.
#
# COMMON MISTAKES:
# - Assigning age = "28" (string) instead of age = 28 (int). The type matters.
# - Writing print(f"Height: {height}" feet) — the "feet" must be inside the string.
# - Using is_student = true (lowercase) — Python booleans are True and False (capitalized).
