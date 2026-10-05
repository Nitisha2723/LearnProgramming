# Modern Java Features (Java 10–21)

Java has evolved significantly since Java 8. This chapter covers the most important features introduced from Java 10 onwards — features you will see in modern codebases and that make Java feel like a much more expressive language.

For each feature, a "Before" and "After" comparison shows why the new syntax is better.

---

## var — Local Type Inference (Java 10)

The `var` keyword lets the compiler infer the type of a local variable from the right-hand side. You no longer have to write the type twice when it is obvious.

### Before (Java 8)
```java
Map<String, List<Order>> ordersByCustomer = new HashMap<String, List<Order>>();
Iterator<Map.Entry<String, List<Order>>> iterator = ordersByCustomer.entrySet().iterator();
```

### After (Java 10+)
```java
var ordersByCustomer = new HashMap<String, List<Order>>();
var iterator = ordersByCustomer.entrySet().iterator();
```

### Rules and When to Use
- `var` only works for **local variables** (inside methods). Not for fields, method parameters, or return types.
- The type must be inferrable from the right-hand side at compile time. `var x = null;` does not compile.
- Use `var` when the type is **obvious from context** (e.g., right after `new` or a factory method).
- Do NOT use `var` when it hides important type information. `var result = process();` is unclear; `OrderResult result = process();` is better.

```java
// Good uses of var
var name = "Alice";                           // obviously String
var accounts = new ArrayList<BankAccount>();  // obviously ArrayList<BankAccount>
var count = 0;                                // obviously int

// Bad uses of var — type is not obvious
var x = getValue();      // What type does getValue() return?
var data = load(path);   // Is data a String? byte[]? InputStream?
```

---

## Switch Expressions (Java 14)

Classic `switch` statements are verbose and error-prone (forgetting `break` causes fall-through bugs). Switch expressions fix this with arrow syntax and a result value.

### Before (Java 8 — classic switch statement)
```java
String dayType;
switch (dayOfWeek) {
    case "MONDAY":
    case "TUESDAY":
    case "WEDNESDAY":
    case "THURSDAY":
    case "FRIDAY":
        dayType = "Weekday";
        break;
    case "SATURDAY":
    case "SUNDAY":
        dayType = "Weekend";
        break;
    default:
        throw new IllegalArgumentException("Unknown day: " + dayOfWeek);
}
```

### After (Java 14+ — switch expression with arrow syntax)
```java
String dayType = switch (dayOfWeek) {
    case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" -> "Weekday";
    case "SATURDAY", "SUNDAY" -> "Weekend";
    default -> throw new IllegalArgumentException("Unknown day: " + dayOfWeek);
};
```

### The yield Keyword
When you need multiple statements in a branch, use `yield` to return a value:

```java
int score = switch (grade) {
    case "A" -> 100;
    case "B" -> 85;
    case "C" -> {
        System.out.println("Average grade");
        yield 70;   // yield returns the value from a block branch
    }
    default -> 0;
};
```

### When to Use
- Use switch expressions (arrow syntax) for all new code — they are safer and cleaner.
- The compiler enforces exhaustiveness for enums, so you cannot forget a case.

---

## Text Blocks (Java 15)

Multi-line strings used to require awkward `\n` escapes and string concatenation. Text blocks let you write multi-line strings naturally.

### Before (Java 8)
```java
String json = "{\n" +
              "  \"name\": \"Alice\",\n" +
              "  \"age\": 30\n" +
              "}";

String html = "<html>\n" +
              "  <body>\n" +
              "    <p>Hello</p>\n" +
              "  </body>\n" +
              "</html>";
```

### After (Java 15+)
```java
String json = """
        {
          "name": "Alice",
          "age": 30
        }
        """;

String html = """
        <html>
          <body>
            <p>Hello</p>
          </body>
        </html>
        """;
```

### Rules
- A text block starts with `"""` followed by a newline.
- Leading whitespace is stripped relative to the closing `"""` position.
- The closing `"""` on its own line includes a trailing newline in the result.
- Escape sequences like `\n` still work inside text blocks.

### When to Use
- SQL queries, JSON, HTML, XML, or any multi-line string literal.
- Test fixtures that contain structured text.

---

## Pattern Matching for instanceof (Java 16)

Before Java 16, checking an object's type and casting it required two steps. Pattern matching combines them into one.

### Before (Java 8)
```java
Object obj = getObject();

if (obj instanceof String) {
    String s = (String) obj;   // redundant cast — we already know it is a String
    System.out.println(s.toUpperCase());
}
```

### After (Java 16+)
```java
Object obj = getObject();

if (obj instanceof String s) {   // pattern variable 's' is automatically cast
    System.out.println(s.toUpperCase());
}
```

### More Examples
```java
// Before
void describe(Object shape) {
    if (shape instanceof Circle) {
        Circle c = (Circle) shape;
        System.out.println("Circle with radius " + c.getRadius());
    } else if (shape instanceof Rectangle) {
        Rectangle r = (Rectangle) shape;
        System.out.println("Rectangle " + r.getWidth() + "x" + r.getHeight());
    }
}

// After
void describe(Object shape) {
    if (shape instanceof Circle c) {
        System.out.println("Circle with radius " + c.getRadius());
    } else if (shape instanceof Rectangle r) {
        System.out.println("Rectangle " + r.getWidth() + "x" + r.getHeight());
    }
}
```

The pattern variable (`c`, `r`) is only in scope within the `if` block where the match succeeded. This prevents accidental use outside the type-safe context.

---

## Records (Java 16)

A **record** is a compact, immutable data class. Before records, creating a simple data carrier (a class that just holds some fields) required writing a constructor, getters, `equals()`, `hashCode()`, and `toString()` — often 50+ lines of boilerplate.

### Before (Java 8 — a typical data class)
```java
public final class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() { return x; }
    public int y() { return y; }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point)) return false;
        Point other = (Point) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point[x=" + x + ", y=" + y + "]";
    }
}
```

### After (Java 16+)
```java
public record Point(int x, int y) { }
```

That single line generates: a constructor, accessors (`x()` and `y()`), `equals()`, `hashCode()`, and `toString()`. All fields are automatically `private final`.

### Records with Validation
```java
public record BankTransfer(String fromAccount, String toAccount, double amount) {
    // Compact constructor — runs before the generated constructor
    public BankTransfer {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (fromAccount.equals(toAccount)) throw new IllegalArgumentException("Cannot transfer to same account");
    }
}
```

### When to Use Records
- Data transfer objects (DTOs): `record UserDto(String name, String email) {}`
- Query results, API responses, event objects, value types.
- Any class whose primary purpose is holding data, not implementing behavior.
- Do NOT use records when you need inheritance or mutable state.

---

## Sealed Classes (Java 17)

**Sealed classes** let you restrict which classes can extend or implement a type. This is useful when you have a fixed set of variants — for example, a `Shape` that can only be a `Circle`, `Rectangle`, or `Triangle`.

### Before (Java 8 — open hierarchy, anyone can subclass)
```java
public abstract class Shape {
    public abstract double area();
}

// Anyone, anywhere can write:
public class Hexagon extends Shape { ... }   // compiler allows it
public class StarShape extends Shape { ... } // and this too
```

### After (Java 17+ — sealed, only permitted subtypes allowed)
```java
public sealed interface Shape permits Circle, Rectangle, Triangle {
    double area();
}

public record Circle(double radius) implements Shape {
    public double area() { return Math.PI * radius * radius; }
}

public record Rectangle(double width, double height) implements Shape {
    public double area() { return width * height; }
}

public record Triangle(double base, double height) implements Shape {
    public double area() { return 0.5 * base * height; }
}
```

### Exhaustive Pattern Matching with Sealed Classes
The real power of sealed classes shows up with switch expressions: the compiler knows all possible subtypes and warns you if you miss one.

```java
double describe(Shape shape) {
    return switch (shape) {
        case Circle c    -> c.area();
        case Rectangle r -> r.area();
        case Triangle t  -> t.area();
        // No default needed — the compiler knows these are all possible Shapes
    };
}
```

If you later add `Hexagon` to the `permits` list without updating this switch, the compiler gives you an error immediately — before your code runs.

### When to Use
- Domain models with a fixed set of variants: `Result<T>` (Success/Failure), `Command` (Create/Update/Delete), `Event` (PaymentReceived/OrderShipped).
- Anywhere you previously used an enum but needed the variants to carry different data.

---

## Summary: Which Feature Solves Which Problem?

| Problem | Feature | Since |
|---|---|---|
| Type names are too long and repetitive | `var` | Java 10 |
| Switch statements are verbose and fall-through-prone | Switch expressions | Java 14 |
| Multi-line strings need ugly `\n` escapes | Text blocks | Java 15 |
| `instanceof` requires a redundant cast | Pattern matching for instanceof | Java 16 |
| Data classes need 50 lines of boilerplate | Records | Java 16 |
| Subclass hierarchies should be closed/finite | Sealed classes | Java 17 |

---

## What's Next

These features combine powerfully. A common modern pattern is using sealed interfaces with records as variants, then switching over them exhaustively — this gives you type-safe, concise, and compiler-verified code that would have taken much more boilerplate in Java 8.

The next chapter covers the Java module system, which helps you structure large applications cleanly.
