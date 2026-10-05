# Introduction to Testing in Java

One of the most valuable skills a software developer can have is the ability to write automated tests. This guide introduces you to the *why*, the *what*, and the *how* of testing in Java.

---

## What Is Testing and Why Does It Matter?

### The $370 Million Lesson

On June 4, 1996, the Ariane 5 rocket — representing a decade of work and $370 million — exploded 37 seconds after launch. The cause? A piece of software reused from the Ariane 4 rocket that had never been tested in the Ariane 5 environment.

The bug: an integer overflow. A 64-bit floating-point number was converted to a 16-bit signed integer and the value was too large to fit. The exception was uncaught. The guidance system crashed. The rocket self-destructed.

This was a function that had been running reliably for years — just not in this specific context. **It was not tested for the new environment.**

The lesson: **if it's not tested, you don't know if it works.**

### Why Write Tests?

**1. Tests catch bugs before your users do.**
A bug caught by an automated test during development costs almost nothing to fix. The same bug caught in production after a customer reports it can cost hours of emergency debugging, lost revenue, and damaged trust.

**2. Tests are executable documentation.**
A test named `calculateDiscount_vipCustomer_returns20Percent()` tells future developers exactly what the system is supposed to do — and it's always up to date, because if it weren't, it would fail.

**3. Tests let you refactor with confidence.**
Want to improve that messy method without breaking anything? Write tests first. If they all pass after your refactoring, you know you didn't break anything. Without tests, refactoring is guesswork.

**4. Tests force better design.**
Code that is hard to test is usually poorly designed (too many dependencies, too many responsibilities). The act of writing tests pushes you toward cleaner, more modular code.

---

## Types of Tests

Software testing has many layers. Here's the landscape:

### Unit Tests
Test a **single method or class in isolation**. All dependencies are replaced with controlled test doubles (stubs/mocks).

- Very fast (milliseconds per test)
- Easy to pinpoint failures
- Form the foundation of your test suite
- Example: testing that `calculateTax(100, 0.2)` returns `20.0`

### Integration Tests
Test **how multiple components work together**. Might involve a database, file system, or external service.

- Slower than unit tests
- Test the wiring between components
- Example: testing that a service correctly saves a user to a database

### End-to-End (E2E) Tests
Test the **entire system** from the user's perspective.

- Slowest and most brittle
- Most realistic
- Example: open a browser, fill in a form, submit it, verify the result appears on screen

### The Test Pyramid

The widely accepted best practice is:

```
        /\
       /  \
      / E2E \       ← Few: slow, expensive, fragile
     /--------\
    /Integration\   ← Some: moderate speed and cost
   /------------\
  /  Unit Tests  \  ← Many: fast, cheap, stable
 /--------------\
```

Build a broad base of fast unit tests. Add a smaller layer of integration tests. Crown it with a few E2E tests for the most critical user flows.

---

## JUnit 5 — Java's Testing Framework

JUnit is the standard testing library for Java. Nearly every Java project uses it. JUnit 5 (also called JUnit Jupiter) is the current version.

### Setting Up JUnit 5

**With Maven** — add to `pom.xml`:
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>
```

**With Gradle** — add to `build.gradle`:
```groovy
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.11.0'
}

test {
    useJUnitPlatform()
}
```

**Without a build tool** — download the JUnit 5 JAR from https://junit.org and add it to your classpath.

### Test Class Structure

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Calculator Tests")
public class CalculatorTest {

    @Test
    @DisplayName("adding two positive numbers returns their sum")
    void add_twoPositiveNumbers_returnsSum() {
        // Arrange
        int a = 3, b = 5;

        // Act
        int result = Calculator.add(a, b);

        // Assert
        assertEquals(8, result);
    }
}
```

### The @Test Annotation

Every method you want JUnit to run as a test must be annotated with `@Test`. Methods without `@Test` are ignored by the test runner.

### Core Assertions

```java
assertEquals(expected, actual);             // equal values
assertEquals(3.14, result, 0.001);          // with tolerance (for doubles)
assertNotEquals(unexpected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(object);
assertNotNull(object);
assertArrayEquals(expectedArray, actualArray);

// assertThrows — verify an exception is thrown
assertThrows(IllegalArgumentException.class, () -> {
    methodThatShouldThrow(-1);
});
```

The most important rule: **put expected first, actual second** in assertions. This ensures error messages make sense: "expected 5 but was 3" — not the other way around.

### @BeforeEach and @AfterEach

Run setup/teardown code before or after each test:

```java
private Calculator calculator;

@BeforeEach
void setUp() {
    calculator = new Calculator(); // fresh instance before each test
}

@AfterEach
void tearDown() {
    // cleanup (e.g., close files, rollback database)
}
```

### @ParameterizedTest with @ValueSource

Run the same test with multiple inputs:

```java
@ParameterizedTest
@ValueSource(strings = {"racecar", "level", "madam"})
@DisplayName("isPalindrome returns true for known palindromes")
void isPalindrome_knownPalindromes_returnsTrue(String word) {
    assertTrue(StringUtils.isPalindrome(word));
}
```

### Test Naming Convention

Use the pattern: `methodName_scenario_expectedBehavior`

```java
// GOOD names
void add_positiveNumbers_returnsSum() { }
void divide_byZero_throwsArithmeticException() { }
void isPalindrome_emptyString_returnsTrue() { }
void findMax_emptyArray_throwsIllegalArgumentException() { }

// BAD names
void test1() { }
void testAdd() { }
void myTest() { }
```

---

## TDD — Test-Driven Development

TDD is a development practice where you write tests **before** writing the code that makes them pass.

### The Red-Green-Refactor Cycle

```
  RED           GREEN          REFACTOR
  ┌─────┐       ┌─────┐        ┌─────────┐
  │Write│──────▶│Write│───────▶│Clean up │
  │a    │       │just │        │without  │
  │failing│     │enough│       │breaking │
  │test │       │code │        │tests    │
  └─────┘       └─────┘        └─────────┘
     ↑                               │
     └───────────────────────────────┘
             repeat
```

1. **RED**: Write a test for the feature you're about to implement. Run it — it should fail (because the code doesn't exist yet). A failing test is confirmation that your test is actually testing something.

2. **GREEN**: Write the minimum code necessary to make the test pass. Don't over-engineer. Don't add features. Just make it green.

3. **REFACTOR**: Now clean up the code you just wrote — remove duplication, clarify variable names, simplify logic — while keeping the tests green.

### TDD Walkthrough: isPalindrome

**Step 1 (Red): Write a failing test**
```java
@Test
void isPalindrome_racecar_returnsTrue() {
    assertTrue(StringUtils.isPalindrome("racecar")); // FAILS: method doesn't exist
}
```

**Step 2 (Green): Write just enough code to pass**
```java
public static boolean isPalindrome(String s) {
    return true; // This passes! (Not complete, but green)
}
```

**Step 3 (Red again): Add a test that exposes the incomplete implementation**
```java
@Test
void isPalindrome_hello_returnsFalse() {
    assertFalse(StringUtils.isPalindrome("hello")); // FAILS: always returns true
}
```

**Step 4 (Green): Make both tests pass**
```java
public static boolean isPalindrome(String s) {
    String reversed = new StringBuilder(s).reverse().toString();
    return s.equals(reversed);
}
```

**Step 5 (Red): Add edge case test**
```java
@Test
void isPalindrome_upperCase_ignoresCase() {
    assertTrue(StringUtils.isPalindrome("Racecar")); // FAILS: uppercase mismatch
}
```

**Step 6 (Green): Handle case**
```java
public static boolean isPalindrome(String s) {
    String lower = s.toLowerCase();
    String reversed = new StringBuilder(lower).reverse().toString();
    return lower.equals(reversed);
}
```

By the end, you have working code AND a test suite that documents its behavior.

### Why TDD?

- **Forces you to think about design before implementation.** Writing the test first makes you think about the interface — what inputs and outputs the method should have — before you think about implementation.
- **Prevents over-engineering.** You only write what's needed to pass the tests.
- **Ensures every line of code is tested.** You can't write untestable code if you test first.
- **Creates confidence.** A growing test suite is a growing safety net.

---

## What Makes a Good Test?

### F.I.R.S.T. Principles

| Letter | Principle | Meaning |
|--------|-----------|---------|
| **F** | Fast | Tests should run in milliseconds. Slow tests don't get run. |
| **I** | Independent | Each test should set up and clean up its own state. Tests must not depend on execution order. |
| **R** | Repeatable | A test should produce the same result every time, on any machine. |
| **S** | Self-validating | The test passes or fails — no human interpretation required. |
| **T** | Timely | Write tests at the same time as the code (or before). Don't write them six months later. |

### One Concept Per Test

Each test should verify exactly one behavior. When a test fails, you know exactly what broke.

```java
// BAD: tests multiple things — if it fails, which part broke?
@Test
void testCalculator() {
    assertEquals(5, calc.add(2, 3));
    assertEquals(1, calc.subtract(3, 2));
    assertEquals(6, calc.multiply(2, 3));
}

// GOOD: one concept per test
@Test
void add_twoPositiveNumbers_returnsSum() {
    assertEquals(5, calc.add(2, 3));
}

@Test
void subtract_largerFromSmaller_returnsPositive() {
    assertEquals(1, calc.subtract(3, 2));
}
```

### Arrange-Act-Assert (AAA)

Structure every test in three clear phases:

```java
@Test
void findMax_unsortedArray_returnsLargestValue() {
    // Arrange — set up the inputs
    int[] numbers = {3, 1, 4, 1, 5, 9, 2, 6};

    // Act — call the method under test
    int result = ArrayUtils.findMax(numbers);

    // Assert — verify the outcome
    assertEquals(9, result);
}
```

### Test Behavior, Not Implementation

A test should verify *what* a method does, not *how* it does it. If you change the internal algorithm but the result is the same, the test should still pass.

```java
// BAD: tests that a specific variable name is used (tests implementation)
// (this would require reflection and is a sign of a bad test)

// GOOD: tests the observable behavior
@Test
void reverseString_hello_returnsOlleh() {
    assertEquals("olleh", StringUtils.reverseString("hello"));
}
```

---

## Running the Tests in This Module

The file `CoreLanguageTest.java` contains 15+ tests covering all exercise solutions. To run them:

**With Maven:**
```bash
mvn test
```

**With Gradle:**
```bash
./gradlew test
```

**With an IDE (IntelliJ, Eclipse, VS Code):**
Right-click the test class → "Run Tests"

**Expected output when all tests pass:**
```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

If a test fails, the output shows exactly which assertion failed and what values were involved — making debugging straightforward.

---

## Summary

| Concept | Takeaway |
|---------|---------|
| Why test? | Catch bugs early, document behavior, refactor safely |
| Unit tests | Test one method/class; fast and isolated |
| JUnit 5 | Java's standard test framework; `@Test` marks test methods |
| Assertions | `assertEquals`, `assertTrue`, `assertThrows`, etc. |
| TDD | Write failing test → make it pass → refactor |
| F.I.R.S.T. | Fast, Independent, Repeatable, Self-validating, Timely |
| AAA | Arrange, Act, Assert — the structure of a good test |
| Name tests well | `method_scenario_expectedBehavior` |

Testing is a skill. It feels slow at first. But the more you practice, the faster and more natural it becomes — and the more confident you'll feel in the code you ship.
