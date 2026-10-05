# Exercises — Module 01: Foundations

These exercises solidify the concepts from the theory and code examples. Attempt each one yourself before looking at the solutions.

---

## How to Approach These Exercises

1. **Read the exercise file completely** before writing any code.
2. **Try to solve it yourself** — even if you struggle, the struggle is where learning happens.
3. **Use the theory files and code examples** as reference — that is what they are there for.
4. **Only look at solutions** (`solutions/` directory) after a genuine attempt.
5. After looking at a solution, **understand why it works**, then try to write it again from memory.

---

## Exercise 1 — Hello World

**File:** `Exercise01_HelloWorld.java`

**Goal:** Print three greeting messages to the console.

**Concepts:** `System.out.println`, String literals, running a Java program

**Difficulty:** Beginner

---

## Exercise 2 — Variables

**File:** `Exercise02_Variables.java`

**Goal:** Declare variables to represent a person's information, then print them in a formatted sentence.

**Concepts:** primitive types, String, variable declaration, concatenation

**Difficulty:** Beginner

---

## Exercise 3 — Calculator

**File:** `Exercise03_Calculator.java`

**Goal:** Perform all arithmetic operations on two numbers and print the results clearly.

**Concepts:** arithmetic operators, integer division, modulo, type casting, formatted output

**Difficulty:** Beginner–Intermediate

---

## How to Compile and Run

From the `01-foundations/exercises/` directory:

```bash
# Compile a specific exercise
javac Exercise01_HelloWorld.java

# Run it
java Exercise01_HelloWorld

# Compile all exercises at once
javac *.java
```

---

## Tips

- Java is **case-sensitive**: `String` is different from `string`, `System` is different from `system`.
- Every statement ends with a **semicolon** `;`.
- String literals use **double quotes**: `"Hello"`.
- char literals use **single quotes**: `'A'`.
- The compiler will tell you exactly which line has an error — read error messages carefully.
- If you are stuck on syntax, look at `code/HelloWorld.java` or `code/DataTypesDemo.java`.
