# Code Examples — How to Compile and Run

This directory contains annotated Java programs that demonstrate the concepts from the theory files. Run each one, study the output, then go back and read the code again.

---

## Setup Check

Before running anything, confirm Java is installed:

```bash
java --version
javac --version
```

Both should output version 21 (or higher).

---

## How to Compile and Run (Command Line)

All commands below assume your terminal is in the `01-foundations/code/` directory.

### HelloWorld.java

```bash
javac HelloWorld.java
java HelloWorld
```

Expected output:
```
Hello, World!
Welcome to Java!
Hello, Learner! Let's learn Java.
```

### DataTypesDemo.java

```bash
javac DataTypesDemo.java
java DataTypesDemo
```

This prints the values of various Java data types and demonstrates type casting.

### OperatorsDemo.java

```bash
javac OperatorsDemo.java
java OperatorsDemo
```

This demonstrates arithmetic, comparison, logical, and assignment operators.

---

## How to Compile Multiple Files at Once

```bash
javac *.java
```

This compiles every `.java` file in the current directory.

---

## Common Errors and Fixes

### "javac: command not found"
The JDK is not installed or not on your PATH. Go back to the main README.md and follow the installation steps.

### "error: class HelloWorld is public, should be declared in a file named HelloWorld.java"
The class name and filename don't match. Java requires them to be identical (including case).

### "error: ';' expected"
You forgot a semicolon at the end of a statement. Every Java statement ends with `;`.

### "cannot find symbol"
You used a variable or method that doesn't exist or isn't in scope. Check your spelling — Java is case-sensitive (`name` and `Name` are different variables).

### "HelloWorld.class" file missing after running javac
There was a compilation error. Read the error messages carefully — they include the line number and a description of the problem.

---

## Using an IDE

If you prefer an IDE (IntelliJ IDEA or VS Code with Java extension):

**IntelliJ IDEA:**
1. Right-click on any `.java` file in the Project panel
2. Select "Run 'ClassName.main()'"

**VS Code:**
1. Open the `.java` file
2. Click the green "Run" triangle that appears above the `main` method
   OR press F5

---

## Modifying the Examples

**Do this.** Do not just read the code and move on.

- Change string values and rerun
- Delete a semicolon and try to compile — read the error message
- Add more `System.out.println()` calls
- Change a variable's value and observe the output change

Breaking things and reading error messages is one of the most effective ways to learn.
