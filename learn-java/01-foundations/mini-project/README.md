# Mini-Project: Personal Info Card

Your first complete Java program.

---

## Project Brief

Create a Java program that stores personal information about someone and displays it as a formatted info card in the terminal.

This project is deliberately simple — the goal is not to solve a hard problem, but to write clean, complete Java code that applies everything you have learned in this module.

---

## Requirements

Your program must:

1. Store the following information as variables:
   - Full name (String)
   - Age (int)
   - Height in metres (double)
   - Weight in kilograms (double)
   - Favourite programming language (String)
   - Is currently a student (boolean)
   - First initial of first name (char)

2. Calculate the following from the stored data:
   - Birth year (current year minus age)
   - A simple Body Mass Index (BMI): weight / (height * height) — display to 1 decimal place
   - Age category: "Young Adult" if age < 30, "Adult" if 30–60, "Senior" if over 60

3. Display a formatted info card that looks something like this:

```
╔══════════════════════════════════╗
║         PERSONAL INFO CARD       ║
╠══════════════════════════════════╣
║  Name:       Alice Johnson       ║
║  Initial:    A                   ║
║  Age:        25 (born ~1999)     ║
║  Height:     1.75m               ║
║  Weight:     70.0kg              ║
║  BMI:        22.9                ║
║  Category:   Young Adult         ║
║  Language:   Java                ║
║  Student:    Yes                 ║
╚══════════════════════════════════╝
```

(You don't need to use box-drawing characters — plain text formatting is fine.)

---

## Learning Objectives

By completing this mini-project, you demonstrate:

- [ ] Declaring and initialising variables of all main types
- [ ] Performing arithmetic including division with decimal results
- [ ] Using boolean values to control output ("Yes"/"No" instead of "true"/"false")
- [ ] Performing String concatenation to build formatted output
- [ ] Thinking in terms of: given data → compute results → present output

---

## How to Run

```bash
cd 01-foundations/mini-project
javac PersonalInfoCard.java
java PersonalInfoCard
```

---

## Extension Challenges

Work through these after completing the base requirements. They use concepts from upcoming modules (you may need to look ahead) — they are optional but valuable.

### Extension 1 — Input From User (Core Language)
Instead of hardcoding the values, ask the user to enter them using `Scanner`. You'll need `import java.util.Scanner;` and `new Scanner(System.in)`.

### Extension 2 — Multiple Cards (OOP)
Create a `Person` class with fields and a `printCard()` method. Instantiate three different Person objects and print all three cards.

### Extension 3 — Formatted Numbers (Core Language)
Use `String.format("%.1f", bmi)` to format the BMI to exactly one decimal place. Research `String.format` in Java documentation.
