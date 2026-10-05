# Mini-Project: Grade Calculator

## Overview

You will build a **Grade Calculator** that processes a class roster, computes letter grades, and generates a formatted class report. This project ties together everything you've learned in Module 02.

---

## What You Will Build

A program that:
1. Stores student names and their test scores
2. Calculates each student's average score
3. Assigns a letter grade (A-F) based on the average
4. Prints a formatted class report
5. Shows class statistics: highest average, lowest average, class mean, and grade distribution

---

## Requirements

### Grading Scale
| Score Range | Letter Grade |
|-------------|-------------|
| 90 – 100 | A |
| 80 – 89.9 | B |
| 70 – 79.9 | C |
| 60 – 69.9 | D |
| Below 60 | F |

### Inputs (hardcoded in the program)
Use this class data:

| Student | T1 | T2 | T3 | T4 | T5 |
|---------|----|----|----|----|-----|
| Alice   | 92 | 88 | 95 | 90 | 85 |
| Bob     | 75 | 82 | 68 | 79 | 71 |
| Charlie | 55 | 62 | 70 | 48 | 60 |
| Diana   | 98 | 95 | 100| 97 | 99 |
| Eve     | 83 | 87 | 81 | 90 | 85 |
| Frank   | 65 | 72 | 58 | 63 | 70 |

### Expected Output

```
============================================================
               CLASS GRADE REPORT
============================================================
Student          T1    T2    T3    T4    T5  Average  Grade
------------------------------------------------------------
Alice            92    88    95    90    85    90.00     A
Bob              75    82    68    79    71    75.00     C
Charlie          55    62    70    48    60    59.00     F
Diana            98    95   100    97    99    97.80     A
Eve              83    87    81    90    85    85.20     B
Frank            65    72    58    63    70    65.60     D
============================================================

--- Class Statistics ---
Highest Average: Diana         (97.80)
Lowest Average:  Charlie       (59.00)
Class Average:   78.77

--- Grade Distribution ---
A: 2  (Diana, Alice)
B: 1  (Eve)
C: 1  (Bob)
D: 1  (Frank)
F: 1  (Charlie)

--- Pass/Fail Summary ---
Passed (D or above): 5 (83.3%)
Failed: 1 (16.7%)
============================================================
```

---

## Learning Objectives

By completing this project, you will demonstrate:
- [ ] 2D arrays (students × scores)
- [ ] String formatting with `printf` / `String.format`
- [ ] Methods: extracted reusable logic
- [ ] Loops: nested for loops, for-each
- [ ] Conditionals: if/else chain for grade assignment
- [ ] Array operations: sum, average, min, max
- [ ] Organizing code into logical methods

---

## Getting Started

1. Open `GradeCalculator.java`
2. The student data is already defined
3. The `main()` method calls all the pieces — implement each method
4. Run and verify your output matches the expected output above

---

## Extension Challenges

Once the base project works, try these extensions:

### Challenge 1: Score Validation
Add validation so that scores outside 0–100 trigger an error message:
```
ERROR: Alice's score of 105 is invalid (must be 0-100). Using 0.
```

### Challenge 2: Interactive Input
Use `Scanner` to let the user enter student names and scores at runtime instead of hardcoded data.

### Challenge 3: Grade Weighting
Modify the program so different tests have different weights:
- T1, T2, T3 each count for 15% of the final grade
- T4 counts for 25%
- T5 (the final exam) counts for 30%

Update the grade calculation to apply these weights.
