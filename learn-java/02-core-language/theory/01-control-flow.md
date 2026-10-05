# Control Flow

Control flow is how you tell a program to make decisions, repeat actions, and skip steps. Without control flow, every program would just run the same lines in the same order every time — which isn't very useful.

---

## if / else if / else

**Real-world analogy: a traffic light.**
When you approach an intersection, you look at the light and choose one of three actions: stop (red), slow down and prepare to stop (yellow), or go (green). The conditions are mutually exclusive — you pick exactly one.

### Syntax

```java
if (condition) {
    // runs when condition is true
} else if (anotherCondition) {
    // runs when the first condition is false AND this one is true
} else {
    // runs when ALL above conditions are false
}
```

### Example: classifying a test score

```java
int score = 82;

if (score >= 90) {
    System.out.println("A");
} else if (score >= 80) {
    System.out.println("B");
} else if (score >= 70) {
    System.out.println("C");
} else if (score >= 60) {
    System.out.println("D");
} else {
    System.out.println("F");
}
// Output: B
```

Notice the order matters. We test the *highest* grade first. Because `82 >= 80` is true and we already know `score < 90` (otherwise we'd have entered the first branch), this correctly identifies a B.

### Best Practices

**Avoid deep nesting.** This is hard to read:

```java
// BAD: pyramid of doom
if (user != null) {
    if (user.isActive()) {
        if (user.hasPermission("admin")) {
            doAdminThing();
        }
    }
}
```

Prefer **guard clauses** — return or throw early to eliminate nesting:

```java
// GOOD: guard clauses flatten the structure
if (user == null) return;
if (!user.isActive()) return;
if (!user.hasPermission("admin")) return;

doAdminThing();
```

**Rule of thumb:** if your `if/else` is more than 3 levels deep, refactor.

---

## Ternary Operator

The ternary operator (`? :`) is a shorthand for a simple `if/else` that produces a value.

### Syntax

```java
variable = condition ? valueIfTrue : valueIfFalse;
```

### Example

```java
int age = 20;
String status = age >= 18 ? "adult" : "minor";
System.out.println(status); // adult
```

### When to Use

Use the ternary when:
- The condition is simple and fits on one line
- You are assigning a value based on a condition

### When NOT to Use

Do NOT use the ternary when:
- The condition is complex (hard to read)
- Either branch requires multiple statements
- You're nesting ternaries inside other ternaries — this way lies madness

```java
// GOOD: simple, readable
String label = isVIP ? "VIP Customer" : "Standard Customer";

// BAD: nested ternaries — just use if/else
String grade = score >= 90 ? "A" : score >= 80 ? "B" : score >= 70 ? "C" : "F";
```

---

## switch Statement

Use `switch` when you have **one variable** and want to compare it against **multiple specific values**.

### Traditional switch (Java 1–13)

```java
int day = 3;
String dayName;

switch (day) {
    case 1:
        dayName = "Monday";
        break;      // IMPORTANT: without break, execution "falls through" to the next case
    case 2:
        dayName = "Tuesday";
        break;
    case 3:
        dayName = "Wednesday";
        break;
    case 4:
        dayName = "Thursday";
        break;
    case 5:
        dayName = "Friday";
        break;
    case 6:
        dayName = "Saturday";
        break;
    case 7:
        dayName = "Sunday";
        break;
    default:
        dayName = "Invalid day";
}

System.out.println(dayName); // Wednesday
```

**The `break` keyword is critical.** Without it, execution "falls through" into the next case. This is sometimes intentional (grouping cases) but is almost always a bug if forgotten.

### Intentional fall-through example

```java
// Multiple cases with the same behavior
switch (day) {
    case 6:
    case 7:
        System.out.println("Weekend!");
        break;
    default:
        System.out.println("Weekday");
}
```

### Modern switch expression (Java 14+)

Java 14 introduced a much cleaner syntax that eliminates `break` and fall-through bugs:

```java
int day = 3;

// Arrow syntax — no break needed, no fall-through possible
String dayName = switch (day) {
    case 1 -> "Monday";
    case 2 -> "Tuesday";
    case 3 -> "Wednesday";
    case 4 -> "Thursday";
    case 5 -> "Friday";
    case 6 -> "Saturday";
    case 7 -> "Sunday";
    default -> "Invalid day";
};

System.out.println(dayName); // Wednesday
```

Multiple labels in one case:

```java
String type = switch (day) {
    case 1, 2, 3, 4, 5 -> "Weekday";
    case 6, 7          -> "Weekend";
    default            -> "Invalid";
};
```

**When to use switch vs if/else:**
- Use `switch` when comparing one variable against a list of known exact values
- Use `if/else` when your conditions involve ranges, comparisons, or multiple variables

---

## while Loop

**Real-world analogy: a cashier at a checkout.**
The cashier keeps scanning items — one at a time — until there are no more items left. They don't know in advance how many items there will be.

Use `while` when you don't know ahead of time how many iterations you need.

### Syntax

```java
while (condition) {
    // body — runs as long as condition is true
    // MUST eventually make condition false, or you get an infinite loop
}
```

### Example: number guessing game

```java
int secret = 42;
int guess = 0;
int attempts = 0;

while (guess != secret) {
    // In a real program, you'd read from Scanner
    // For this example, simulate guesses
    guess++;
    attempts++;
}

System.out.println("Found it in " + attempts + " attempts!");
```

### Warning: infinite loops

If the condition never becomes false, your program hangs forever:

```java
// INFINITE LOOP — count never changes!
int count = 0;
while (count < 10) {
    System.out.println(count);
    // forgot count++
}
```

Always ensure the loop body makes progress toward making the condition false.

---

## do-while Loop

Similar to `while`, but the body **always executes at least once** before the condition is checked.

### Syntax

```java
do {
    // body — always runs at least once
} while (condition);
```

### When to use do-while

Use `do-while` when you need to execute the body first and then check if you should repeat. The classic use case is **menu-driven programs** and **input validation**:

```java
Scanner scanner = new Scanner(System.in);
int choice;

do {
    System.out.println("1. Option A");
    System.out.println("2. Option B");
    System.out.println("3. Quit");
    System.out.print("Enter choice: ");
    choice = scanner.nextInt();
} while (choice != 3);

System.out.println("Goodbye!");
```

You always want to show the menu at least once. With a regular `while`, you'd have to initialize `choice` to some arbitrary value before the loop.

---

## for Loop

**Real-world analogy: an assembly line.**
A factory assembles exactly 500 widgets per shift. The conveyor belt runs exactly 500 cycles — it doesn't check "are we done?" at each step with uncertainty, it counts down from a known number.

Use `for` when you know **exactly** how many iterations you need.

### Syntax

```java
for (initialization; condition; update) {
    // body
}
```

Anatomy of each part:

```java
for (int i = 0; i < 10; i++) {
//   ^^^^^^^^^   ^^^^^^  ^^^
//   runs once   tested  runs after
//   before      before  each iteration
//   loop        each
//               iteration
```

### Example: print a multiplication table

```java
for (int i = 1; i <= 10; i++) {
    System.out.println("5 x " + i + " = " + (5 * i));
}
```

### Common patterns

Counting down:
```java
for (int i = 10; i >= 1; i--) {
    System.out.println(i);
}
System.out.println("Blast off!");
```

Stepping by 2:
```java
for (int i = 0; i <= 100; i += 2) {
    System.out.println(i + " is even");
}
```

---

## for-each Loop (Enhanced for)

When iterating over an array or collection, the `for-each` loop is cleaner and less error-prone than a regular `for` loop.

### Syntax

```java
for (Type element : collection) {
    // use element
}
```

### Example

```java
int[] numbers = {10, 20, 30, 40, 50};

// Traditional for loop
for (int i = 0; i < numbers.length; i++) {
    System.out.println(numbers[i]);
}

// for-each — cleaner, no index needed
for (int num : numbers) {
    System.out.println(num);
}
```

### When to use for-each vs for

Use **for-each** when:
- You only need the values, not the indices
- You're not modifying the array elements

Use **regular for** when:
- You need the index
- You're modifying elements by index
- You need to iterate in reverse or with a custom step

---

## break and continue

### break

`break` immediately exits the **current loop** (or switch). Use it to stop a loop when a condition is met.

```java
// Find the first negative number
int[] numbers = {5, 3, 8, -2, 7, -4};
int firstNegative = Integer.MIN_VALUE;

for (int num : numbers) {
    if (num < 0) {
        firstNegative = num;
        break; // no need to keep looking
    }
}

System.out.println("First negative: " + firstNegative); // -2
```

### continue

`continue` skips the **rest of the current iteration** and moves to the next one.

```java
// Print only even numbers
for (int i = 1; i <= 10; i++) {
    if (i % 2 != 0) {
        continue; // skip odd numbers
    }
    System.out.println(i); // 2, 4, 6, 8, 10
}
```

### Use Sparingly

Both `break` and `continue` are legitimate, but overuse makes loops hard to reason about. If you find yourself using `break` or `continue` frequently, consider whether restructuring the condition or using a method would be cleaner.

---

## Summary: Choosing the Right Loop

| Loop | Use when |
|------|----------|
| `for` | You know the exact number of iterations |
| `for-each` | You're iterating over an array or collection |
| `while` | You don't know how many iterations; check first |
| `do-while` | You need to run the body at least once; check after |

---

## What's Next

Now that you can control program flow, the next step is organizing code into **methods** — reusable named blocks that you can call from anywhere in your program.

→ Continue to `02-methods.md`
