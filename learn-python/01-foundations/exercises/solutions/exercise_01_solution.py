# SOLUTION: Exercise 1 - Hello World
# ============================================================
# This is the solution. Review it AFTER you have completed the exercise yourself.
# Compare your approach to this solution — there may be more than one good way.
# ============================================================

# Solution to TODO 1: Print "Hello, Python!"
print("Hello, Python!")

# Solution to TODO 2: Variable and f-string
my_name = "Alex"
print(f"My name is {my_name}")

# Solution to TODO 3: Print statement
print("I am learning Python today!")

# Solution to TODO 4: Variables and f-string calculation
years_experience = 0
future_years = 5
print(f"In {future_years} years, I will have {years_experience + future_years} years of Python experience.")

# ============================================================
# NOTES:
# - The f-string in TODO 4 demonstrates that you can put expressions
#   (not just variables) inside the {} curly braces.
# - years_experience + future_years is evaluated at print time: 0 + 5 = 5
#
# COMMON MISTAKES:
# - Forgetting the f before the string: "Hello {my_name}" prints literally
#   "{my_name}" instead of the variable's value.
# - Using = instead of == in comparisons (this matters in later exercises).
# - Missing quotes around the string: print(Hello) causes a NameError.
