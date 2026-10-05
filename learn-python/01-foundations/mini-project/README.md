# Mini-Project: Personal Info Card

## Overview

Build a Python program that generates a formatted personal information card — like a digital business card printed to the terminal.

This project brings together everything you have learned in the foundations module: variables, data types, operators, and f-strings.

---

## What You Will Build

A program that displays a nicely formatted personal info card:

```
╔══════════════════════════════════════╗
║         PERSONAL INFO CARD           ║
╠══════════════════════════════════════╣
║  Name:       Alice Johnson           ║
║  Age:        28 years                ║
║  Height:     5.7 feet (170.7 cm)     ║
║  City:       Berlin, Germany         ║
║  Occupation: Software Engineer       ║
║  Student:    No                      ║
╠══════════════════════════════════════╣
║  STATS                               ║
║  Days alive: 10,220                  ║
║  Birth year: 1997                    ║
║  Initials:   AJ                      ║
╚══════════════════════════════════════╝
```

---

## Learning Objectives

By completing this project, you will practice:

- Declaring variables of different types (str, int, float, bool)
- Arithmetic calculations with variables
- String operations (indexing, upper/lower case)
- f-strings for formatted output
- Planning a small program from requirements

---

## Instructions

1. Open `personal_info_card.py`
2. Find the STARTER SECTION at the top
3. Fill in your own personal information as variables
4. Write the code to generate the formatted output
5. The SOLUTION SECTION below the starter shows one way to do it — look at it only after you have tried

---

## Expected Output

Your output does not need to match exactly — the values will be yours. The format (the box drawing characters) is given to you. Focus on:
- Getting the variables right
- Calculating days alive correctly
- Extracting initials from the name
- Converting height from feet to centimeters

---

## Extension Challenges

Once you have the basic version working, try these:

**Challenge 1: Add more information**
Add a `favorite_language` variable and a `years_programming` variable. Include them in the card.

**Challenge 2: Better height conversion**
The conversion from feet to cm: multiply feet by 30.48. Can you also show the height in feet and inches separately? (Hint: 5.7 feet = 5 feet and 0.7 * 12 = 8.4 inches)

**Challenge 3: Personalize the border**
Instead of the box-drawing characters shown above, create your own style. Maybe use `*` or `-` characters. Try different widths. Make it look professional.

---

## Notes

- Box-drawing characters (`╔`, `╗`, `╠`, `╣`, `╚`, `╝`, `║`, `═`) are standard Unicode characters. You can copy them from this README or type them using your operating system's character map.
- For days alive, use: `age * 365`. This is an approximation (does not account for leap years — that is fine for now).
- For initials, use string indexing: `name[0]` gives you the first character.
