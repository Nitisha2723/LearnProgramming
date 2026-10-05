# Methods

A method is a **named, reusable block of code** that performs a specific task. Instead of writing the same logic ten times in different places, you write it once in a method and call it ten times.

Think of methods like the buttons on a microwave: "Start", "Defrost", "Add 30 seconds". Each button triggers a specific sequence of actions. You don't need to know *how* the microwave does it — you just press the button.

---

## Method Anatomy

Every method has the same structure:

```java
accessModifier returnType methodName(parameterType paramName, ...) {
    // method body
    return value; // only if return type is not void
}
```

Let's break down each part:

```java
public static int add(int a, int b) {
// ^^^^^^  ^^^^^^ ^^^  ^^^^^^^^^^
//   |       |    |        |
//   |       |    |   parameters (inputs)
//   |       |  method name
//   |    return type (what it gives back)
// access modifier (who can call it)
    return a + b;
}
```

### Access Modifiers

| Modifier | Meaning |
|----------|---------|
| `public` | Any code anywhere can call this method |
| `private` | Only code in the same class can call this method |
| `protected` | Same class and subclasses (covered in OOP module) |
| (none) | Package-private — same package only |

You will use `public` and `private` most often. In the OOP module, you'll learn when each is appropriate.

### Return Types

| Return type | Meaning |
|-------------|---------|
| `void` | The method does something but returns nothing |
| `int`, `double`, `boolean`, etc. | Returns that type |
| `String`, `int[]`, any object | Returns that object type |

---

## void Methods: Doing Without Returning

A `void` method performs an action but doesn't give back a result.

```java
public static void printGreeting(String name) {
    System.out.println("Hello, " + name + "!");
    System.out.println("Welcome to Java programming.");
}

// Calling it:
printGreeting("Alice");
// Output:
// Hello, Alice!
// Welcome to Java programming.
```

You can still use `return;` in a void method to exit early:

```java
public static void printIfPositive(int number) {
    if (number <= 0) {
        return; // exit early — nothing to print
    }
    System.out.println("Positive number: " + number);
}
```

---

## Methods That Return Values

A method with a non-void return type must use the `return` keyword to send a value back to the caller.

```java
public static double celsiusToFahrenheit(double celsius) {
    double fahrenheit = (celsius * 9.0 / 5.0) + 32.0;
    return fahrenheit;
}

// Calling it:
double boiling = celsiusToFahrenheit(100.0);
System.out.println(boiling); // 212.0
```

The returned value can be:
- Assigned to a variable: `double result = celsiusToFahrenheit(37.0);`
- Used directly in an expression: `System.out.println(celsiusToFahrenheit(0.0));`
- Passed to another method: `Math.round(celsiusToFahrenheit(98.6))`

---

## Parameters vs Arguments

This is a vocabulary distinction that trips up many beginners.

- **Parameters** are the variables in the method *definition*. They are placeholders.
- **Arguments** are the actual values you pass when you *call* the method.

```java
// "a" and "b" are PARAMETERS — placeholders in the definition
public static int multiply(int a, int b) {
    return a * b;
}

// 3 and 7 are ARGUMENTS — actual values passed in the call
int result = multiply(3, 7);
```

---

## Method Overloading

Java allows you to have **multiple methods with the same name** as long as their parameter lists are different. This is called *overloading*.

The compiler figures out which version to call based on the number and types of arguments you pass.

```java
// Three versions of calculateArea — same name, different signatures
public static double calculateArea(double radius) {
    // Circle: π × r²
    return Math.PI * radius * radius;
}

public static double calculateArea(double width, double height) {
    // Rectangle: w × h
    return width * height;
}

public static double calculateArea(double base, double height, boolean isTriangle) {
    // Triangle: ½ × b × h
    return 0.5 * base * height;
}

// The compiler picks the right one:
double circleArea    = calculateArea(5.0);           // calls version 1
double rectArea      = calculateArea(4.0, 6.0);      // calls version 2
double triangleArea  = calculateArea(3.0, 8.0, true); // calls version 3
```

**Key rule:** The return type alone is NOT enough to distinguish overloaded methods. The parameter types and/or count must differ.

---

## The Call Stack

Every time your program calls a method, Java creates a **stack frame** — a block of memory that holds:
- The method's local variables
- The method's parameters
- The return address (where to go back when the method finishes)

These frames are stacked on top of each other. When a method finishes, its frame is popped off and execution continues where it left off.

### Tracing an Example

```java
public static void main(String[] args) {
    int result = double(5);        // Step 1: call double()
    System.out.println(result);    // Step 4: print 10
}

public static int double(int x) {
    int tripled = triple(x);       // Step 2: call triple()
    return tripled - x;            // Step 3: return 10
}

public static int triple(int x) {
    return x * 3;                  // Returns 15
}
```

Call stack at the moment `triple()` is running:

```
TOP (most recent call)
┌───────────────────────────────┐
│ triple()  — x = 5             │
├───────────────────────────────┤
│ double()  — x = 5, tripled=? │
├───────────────────────────────┤
│ main()    — result = ?        │
└───────────────────────────────┘
BOTTOM
```

When `triple()` returns, its frame is removed and control returns to `double()`.

### StackOverflowError

If a method keeps calling itself without stopping, the stack grows until it runs out of memory:

```java
// NEVER DO THIS — infinite recursion
public static void forever() {
    forever(); // calls itself with no exit condition
}
// Throws: java.lang.StackOverflowError
```

This is the classic **StackOverflowError**. It means recursion went too deep.

---

## Pass By Value in Java

This is one of the **most important concepts in Java** and a common interview question.

**Java is always pass-by-value.** This means:
- When you pass a variable to a method, the method gets a **copy** of the value.
- Changes to the parameter inside the method do NOT affect the original variable.

### Demonstration with a primitive

```java
public static void tryToDouble(int x) {
    x = x * 2;              // modify the copy
    System.out.println("Inside method: " + x); // 10
}

int number = 5;
tryToDouble(number);
System.out.println("After method: " + number); // Still 5! The original is unchanged.
```

### What about objects?

When you pass an object to a method, you're passing a copy of the **reference** (memory address), not the object itself.

- The reference is copied — so you can't make the variable point to a different object.
- BUT you can still modify the *contents* of the object through that reference.

```java
public static void addElement(int[] array) {
    array[0] = 99;     // Modifies the CONTENTS — this works!
    // array = new int[]{1, 2, 3};  // This would NOT affect the caller's variable
}

int[] myArray = {1, 2, 3};
addElement(myArray);
System.out.println(myArray[0]); // 99 — the contents changed!
```

Think of it like this: if you email someone a copy of your house address, they can go to your house and rearrange the furniture. But they can't make you live at a different house.

---

## Recursive Methods

A method can call itself — this is called **recursion**. Every recursive method needs:
1. A **base case** — a condition where it stops calling itself
2. A **recursive case** — where it calls itself with a smaller/simpler input

### Example: Factorial

`5! = 5 × 4 × 3 × 2 × 1 = 120`

```java
public static int factorial(int n) {
    if (n <= 1) {
        return 1;        // BASE CASE: stop here
    }
    return n * factorial(n - 1);  // RECURSIVE CASE: smaller problem
}

System.out.println(factorial(5)); // 120
```

Tracing `factorial(4)`:
```
factorial(4) = 4 × factorial(3)
                   = 3 × factorial(2)
                        = 2 × factorial(1)
                                  = 1   ← base case
                        = 2 × 1 = 2
                   = 3 × 2 = 6
factorial(4) = 4 × 6 = 24
```

---

## Method Best Practices

### Single Responsibility

Each method should do **one thing** and do it well. If you can describe what a method does with "and", it probably should be two methods.

```java
// BAD: does too many things
public static void calculateAndPrintAndSaveScore(int raw, int max) { ... }

// GOOD: separate concerns
public static double calculateScore(int raw, int max) { ... }
public static void printScore(double score) { ... }
public static void saveScore(double score) { ... }
```

### Verb Names

Methods perform actions, so name them with verbs:

```java
// GOOD names
getUserById(), calculateTax(), isValid(), sendEmail(), parseDate()

// BAD names
user(), tax(), validation(), email()
```

### The 20-Line Guideline

If a method exceeds 20 lines, it's probably doing too much. Consider extracting helper methods.

### The "Explain" Test

If you can't describe what a method does in one sentence without using "and", extract part of it into a new method.

---

## What's Next

With methods under your belt, you can now write well-structured programs. Next, we look at **arrays** — the first data structure for storing multiple values.

→ Continue to `03-arrays.md`
