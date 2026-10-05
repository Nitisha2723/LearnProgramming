# Mini-Project: Personal Info Card
# ============================================================
# Build a program that prints a formatted personal info card.
# This project uses everything from the foundations module.
#
# How to run: python3 personal_info_card.py
# ============================================================


# ============================================================
# STARTER SECTION
# ============================================================
# Fill in YOUR information in these variables, then write the
# code to display them in a formatted card.
# Look at the expected output in README.md for guidance.
#
# Step 1: Fill in these variables with your information.
# Step 2: Calculate derived values (days alive, birth year, initials, height in cm).
# Step 3: Print the formatted card.
#
# Hints:
# - Days alive: age * 365 (approximate)
# - Birth year: current_year - age
# - Initials: use string indexing — name[0] gives first character
#   For "Alice Johnson", first_initial = name[0] = "A"
#   For last name initial, you need to find the space: name.split()
#   Actually, try: parts = full_name.split()  → ["Alice", "Johnson"]
#   Then: initials = parts[0][0] + parts[1][0]  → "AJ"
# - Height in cm: height_feet * 30.48

# ---- Fill these in ----
full_name = "Your Name Here"       # Replace with your name
age = 0                            # Replace with your age (integer)
height_feet = 0.0                  # Replace with your height in feet (float, e.g. 5.8)
city = "Your City"                 # Replace with your city
country = "Your Country"           # Replace with your country
occupation = "Your Occupation"     # Replace with your job/role
is_student = True                  # Replace with True or False

current_year = 2025                # Change this to the current year

# ---- Write your calculations here ----
# days_alive = ?
# birth_year = ?
# initials = ?
# height_cm = ?

# ---- Write your print statements here ----
# Print the formatted card


# ============================================================
# SOLUTION SECTION
# ============================================================
# Read the solution below ONLY after you have tried writing your own version.
# There are many ways to write this program — yours does not need to match exactly.
# The key is that it works and the output is readable and correct.
# ============================================================

def print_solution():
    """
    Solution implementation.
    This is wrapped in a function so it does not run automatically.
    To see the solution output, call print_solution() at the bottom of the file.
    """

    # ---- Personal information ----
    full_name = "Alice Johnson"
    age = 28
    height_feet = 5.7
    city = "Berlin"
    country = "Germany"
    occupation = "Software Engineer"
    is_student = False
    current_year = 2025

    # ---- Calculated values ----

    # Days alive: approximate (no leap year adjustment)
    days_alive = age * 365

    # Birth year: subtract age from current year
    birth_year = current_year - age

    # Initials: split name into parts, take first character of each
    name_parts = full_name.split()          # ["Alice", "Johnson"]
    first_initial = name_parts[0][0]        # "A" — first char of "Alice"
    last_initial = name_parts[1][0]         # "J" — first char of "Johnson"
    initials = first_initial + last_initial  # "AJ"

    # Height in centimeters: 1 foot = 30.48 cm
    height_cm = height_feet * 30.48

    # Student status as human-readable text
    student_text = "Yes" if is_student else "No"

    # ---- Print the formatted card ----

    # Box width: 40 characters inside
    print("╔══════════════════════════════════════╗")
    print("║         PERSONAL INFO CARD           ║")
    print("╠══════════════════════════════════════╣")

    # Each line: "║  Label:      Value               ║"
    # We use ljust() to left-justify text within a fixed width
    # ljust(n) pads the string with spaces on the right to reach length n

    line_width = 36  # characters inside the box (between ║  and  ║)

    def card_line(label, value):
        """Format a single line of the card."""
        content = f"{label}{value}"
        # Pad to line_width so the right border aligns
        return f"║  {content.ljust(line_width)}║"

    print(card_line("Name:       ", full_name))
    print(card_line("Age:        ", f"{age} years"))
    print(card_line("Height:     ", f"{height_feet} feet ({height_cm:.1f} cm)"))
    print(card_line("City:       ", f"{city}, {country}"))
    print(card_line("Occupation: ", occupation))
    print(card_line("Student:    ", student_text))

    print("╠══════════════════════════════════════╣")
    print("║  STATS                               ║")

    # Format days_alive with comma separator for readability
    print(card_line("Days alive: ", f"{days_alive:,}"))
    print(card_line("Birth year: ", str(birth_year)))
    print(card_line("Initials:   ", initials))

    print("╚══════════════════════════════════════╝")


# ============================================================
# HOW TO RUN
# ============================================================
# To run YOUR version (STARTER SECTION above):
#   Just run: python3 personal_info_card.py
#
# To run the SOLUTION:
#   Uncomment the line below and comment out your starter code,
#   OR just call print_solution() after your starter code to compare.
#
# print_solution()


# ============================================================
# SOLUTION NOTES
# ============================================================
# Things in the solution you might not have seen yet:
#
# str.split() — splits a string into a list at spaces.
#   "Alice Johnson".split() → ["Alice", "Johnson"]
#   Lists are covered in Module 04.
#
# str.ljust(n) — pads a string with spaces on the right to width n.
#   "hello".ljust(10) → "hello     " (5 spaces added)
#   This is used to align the right border of the card.
#
# f"{value:.1f}" — format a float to 1 decimal place.
#   f"{3.14159:.1f}" → "3.1"
#   Format specifiers are covered in Module 02.
#
# f"{value:,}" — format an integer with comma thousands separators.
#   f"{10220:,}" → "10,220"
#
# def function_name(): — defines a reusable function.
#   Functions are covered in Module 02.
#
# The ternary expression (value if condition else other_value) is
# a one-line if/else covered in Module 02.
#
# Do not worry if you did not use all of these — a simpler version
# using just print() and f-strings is perfectly fine for this module.
