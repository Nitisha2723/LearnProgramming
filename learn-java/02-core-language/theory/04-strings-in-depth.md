# Strings In Depth

You've been using `String` since your first `"Hello, World!"`. Now it's time to understand what `String` really is, why it behaves the way it does, and how to use it effectively.

---

## Strings Are Objects, Not Primitives

In Java, `String` starts with a capital letter. That's your first clue: it's a **class**, not a primitive type.

```java
// Primitives (lowercase)
int age = 25;
double price = 9.99;
boolean active = true;

// String (capital S — it's a class)
String name = "Alice";
```

Because `String` is a class, `name` is actually a **reference** (a memory address pointing to an object in the heap). This distinction matters enormously — you'll see why in a moment.

---

## String Immutability

Once a `String` object is created, **its value can never change**. This is called *immutability*.

This might seem strange. After all, you can do:

```java
String greeting = "Hello";
greeting = "Hi there";
```

But you didn't change the `"Hello"` object. You created a *new* `String` object `"Hi there"` and made the variable `greeting` point to it. The original `"Hello"` object still exists in memory (until garbage collected).

### Why Immutability?

Immutability is a deliberate design decision with three major benefits:

**1. Security.** If someone passes a filename `"config.txt"` to a file-reading method, you don't want them to modify it mid-read and trick the method into reading `"/etc/passwd"` instead. Because `String` is immutable, the value you received is the value that was passed.

**2. Thread Safety.** Multiple threads can read the same `String` object without any synchronization, because no one can modify it. Immutable objects are inherently thread-safe.

**3. String Pool (Caching).** Because `String` values never change, Java can safely cache and reuse them — multiple variables can share the exact same `String` object. This saves memory.

---

## The String Pool

Java maintains a special region of memory called the **String pool** (also called the *interned string pool*) where it keeps String literals.

When you write:
```java
String a = "hello";
String b = "hello";
```

Java doesn't create two separate objects. It creates one `"hello"` object in the pool and makes both `a` and `b` point to it.

This is safe because Strings are immutable — no one can change the shared object.

When you use `new String()`, you bypass the pool:
```java
String c = new String("hello"); // creates a new object, NOT in the pool
```

This matters for the next topic.

---

## == vs .equals() — The Most Common Java Bug

This is a mistake every Java programmer makes at least once. Bookmark this section.

`==` compares **memory addresses** (references).
`.equals()` compares **content** (the actual characters).

```java
String a = "hello";
String b = "hello";
String c = new String("hello");

System.out.println(a == b);          // true  (same pool object)
System.out.println(a == c);          // false (c is a separate object)
System.out.println(a.equals(c));     // true  (same content)
System.out.println(a.equals(b));     // true  (same content)
```

### The rule: ALWAYS use .equals() to compare Strings

```java
// BAD
if (userInput == "quit") { ... }     // might not work!

// GOOD
if (userInput.equals("quit")) { ... }
```

Even better — put the known string on the left to avoid NullPointerException if `userInput` is null:

```java
if ("quit".equals(userInput)) { ... }  // safe even if userInput is null
```

---

## Essential String Methods

### Length and Character Access

```java
String s = "Hello, World!";

s.length();           // 13 — number of characters
s.charAt(0);          // 'H' — character at index 0
s.charAt(7);          // 'W' — character at index 7
s.isEmpty();          // false — is length == 0?
s.isBlank();          // false — is it empty or only whitespace? (Java 11+)
```

### Searching Within Strings

```java
String s = "Hello, World!";

s.indexOf('o');           // 4  — first occurrence of 'o'
s.lastIndexOf('o');       // 8  — last occurrence of 'o'
s.indexOf("World");       // 7  — first occurrence of substring
s.indexOf("xyz");         // -1 — not found

s.contains("World");      // true
s.startsWith("Hello");    // true
s.endsWith("!");          // true
```

### Extracting Parts of a String

```java
String s = "Hello, World!";

s.substring(7);       // "World!" — from index 7 to end
s.substring(7, 12);   // "World" — from index 7 up to (not including) 12
```

### Transforming Strings

```java
String s = "Hello, World!";

s.toLowerCase();                        // "hello, world!"
s.toUpperCase();                        // "HELLO, WORLD!"
s.trim();                               // removes leading/trailing whitespace
s.strip();                              // like trim(), but Unicode-aware (Java 11+)
s.replace('l', 'r');                    // "Herro, Worrd!"
s.replace("World", "Java");             // "Hello, Java!"
s.replaceAll("\\s+", "_");              // "Hello,_World!" (regex)
```

### Splitting and Joining

```java
String csv = "Alice,Bob,Charlie,Dave";
String[] names = csv.split(",");        // {"Alice", "Bob", "Charlie", "Dave"}

// Split with limit
String[] first2 = csv.split(",", 2);    // {"Alice", "Bob,Charlie,Dave"}

// Join (the reverse of split)
String joined = String.join(", ", "Alice", "Bob", "Charlie"); // "Alice, Bob, Charlie"

// Join an array
String rejoin = String.join(",", names); // "Alice,Bob,Charlie,Dave"
```

### Checking and Comparing

```java
String a = "hello";
String b = "HELLO";

a.equals(b);                   // false
a.equalsIgnoreCase(b);         // true
a.compareTo(b);                // positive (lowercase comes after uppercase in ASCII)
a.compareToIgnoreCase(b);      // 0 (equal ignoring case)
```

---

## String Concatenation vs StringBuilder

You know you can concatenate Strings with `+`:

```java
String result = "Hello" + ", " + "World" + "!"; // "Hello, World!"
```

### The Performance Problem

Because Strings are immutable, each `+` creates a **new String object**. In a loop, this can be very slow:

```java
// BAD: creates 1000 intermediate String objects
String result = "";
for (int i = 0; i < 1000; i++) {
    result = result + i + ", "; // new object each iteration!
}
```

With 1000 iterations, you create ~1000 temporary `String` objects. Most are immediately thrown away. This is wasteful.

### StringBuilder: The Solution

`StringBuilder` is a **mutable** character buffer. You build the string piece by piece, and only create the final `String` when you call `.toString()`.

```java
// GOOD: uses StringBuilder internally
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i);
    sb.append(", ");
}
String result = sb.toString();
```

### StringBuilder Methods

```java
StringBuilder sb = new StringBuilder("Hello");

sb.append(" World");          // "Hello World"
sb.insert(5, ",");            // "Hello, World"
sb.delete(5, 6);              // "Hello World"
sb.reverse();                 // "dlroW olleH"
sb.replace(0, 5, "Goodbye"); // "Goodbye olleH"
sb.length();                  // current length
sb.charAt(0);                 // 'G'
sb.toString();                // convert to String
```

### When to use each

| Situation | Use |
|-----------|-----|
| Simple one-liner concatenation | `+` is fine |
| Building a string in a loop | `StringBuilder` |
| Multiple appends in a method | `StringBuilder` |
| Concatenation in a single expression | `+` is fine (compiler optimizes) |

Note: The Java compiler automatically converts simple `+` chains into `StringBuilder` calls, but it can't always do this for loops.

---

## String.format() and printf

For formatted output, `String.format()` and `printf` are cleaner than manual concatenation.

### String.format()

```java
String name = "Alice";
int age = 30;
double gpa = 3.85;

String message = String.format("Student: %s, Age: %d, GPA: %.2f", name, age, gpa);
System.out.println(message);
// Output: Student: Alice, Age: 30, GPA: 3.85
```

### Common format specifiers

| Specifier | Type | Example |
|-----------|------|---------|
| `%s` | String | `"Alice"` |
| `%d` | integer | `42` |
| `%f` | floating-point | `3.140000` |
| `%.2f` | float, 2 decimal places | `3.14` |
| `%5d` | integer, min width 5 | `"   42"` |
| `%-5d` | left-aligned, width 5 | `"42   "` |
| `%n` | newline (platform-safe) | |
| `%10.2f` | float, width 10, 2 decimal | `"      3.14"` |

### printf

`System.out.printf()` is like `System.out.println(String.format(...))` — it formats and prints in one step, but does NOT add a newline automatically:

```java
System.out.printf("%-15s %5.2f%n", "Alice", 95.7);
System.out.printf("%-15s %5.2f%n", "Bob", 87.3);
// Output:
// Alice            95.70
// Bob              87.30
```

---

## Converting Between String and Other Types

### Other types → String

```java
int n = 42;
String s1 = String.valueOf(n);         // "42"
String s2 = Integer.toString(n);       // "42"
String s3 = "" + n;                    // "42" (works but less clear)
String s4 = String.valueOf(3.14);      // "3.14"
String s5 = String.valueOf(true);      // "true"
```

### String → other types

```java
String numStr = "42";
int n = Integer.parseInt(numStr);         // 42
double d = Double.parseDouble("3.14");    // 3.14
boolean b = Boolean.parseBoolean("true"); // true

// Watch out: throws NumberFormatException if the string isn't a valid number
int bad = Integer.parseInt("hello"); // throws NumberFormatException!
```

---

## Summary

| Concept | Key Point |
|---------|-----------|
| String is a class | Not a primitive; it's a reference type |
| Immutability | String values never change; operations return new Strings |
| String pool | Literals are cached; saves memory |
| == vs .equals() | Use `.equals()` to compare content |
| StringBuilder | Use when building Strings in a loop |
| String.format() | Clean, readable formatting |

→ Continue to `05-scope-and-memory.md`
