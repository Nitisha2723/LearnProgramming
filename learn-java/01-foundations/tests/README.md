# Your Very First Test — Welcome to TDD

Congratulations on reaching the testing chapter! Writing tests might feel like extra work at first, but it is one of the most valuable habits you can build as a programmer. This guide walks you through your very first test with zero assumed knowledge.

---

## Why Write Tests Even for Simple Programs?

Imagine you write a method that adds two numbers. It seems obvious — what could go wrong? A lot, actually:

- What if you accidentally wrote `a - b` instead of `a + b`?
- What if you change the method later and introduce a subtle bug?
- What if a teammate "fixes" something and breaks your logic?

Tests are your safety net. They check your code **automatically**, every time, in seconds. Instead of running your program and squinting at the output to see if it looks right, a test either passes (green) or fails (red) with a clear message telling you exactly what went wrong.

**Test-Driven Development (TDD)** takes this further: write the test *first*, watch it fail, then write just enough code to make it pass. This might feel backwards, but it forces you to think clearly about what you want your code to do before you write it.

---

## What Is JUnit 5?

JUnit 5 is the standard testing framework for Java. Think of it as a toolkit that gives you the building blocks for writing tests: a way to mark methods as tests (`@Test`), a way to give tests human-readable names (`@DisplayName`), and a set of "assertion" methods that check whether your code produced the correct result (like `assertEquals(expected, actual)`). When you run your tests, JUnit collects all the `@Test` methods, runs each one, and reports which passed and which failed. JUnit 5 is the fifth major version of this framework and is the current standard — you will see it everywhere in professional Java projects.

---

## How to Run These Tests

If you are using Maven (recommended), run from the project root:
```
mvn test
```

If you are compiling manually, use these commands from the `01-foundations/tests/` directory:
```
javac -cp junit-platform-console-standalone-1.10.1.jar FoundationsTest.java
java -cp .:junit-platform-console-standalone-1.10.1.jar org.junit.platform.console.standalone.ConsoleLauncher --select-class=FoundationsTest
```

(On Windows, replace `:` with `;` in the classpath.)

You can download the standalone JUnit 5 JAR from:
https://mvnrepository.com/artifact/org.junit.platform/junit-platform-console-standalone

---

## What Each Annotation Means

| Annotation | What It Does |
|---|---|
| `@Test` | Marks a method as a test. JUnit will run it automatically. |
| `@DisplayName("...")` | Gives the test a human-friendly name in the test report. Use full sentences! |
| `@ParameterizedTest` | Runs the same test method multiple times with different input values. |
| `@ValueSource(...)` | Provides the list of values for a `@ParameterizedTest`. |

---

## The AAA Pattern

Every good test follows the **AAA pattern**: Arrange, Act, Assert.

```java
@Test
@DisplayName("Adding two positive numbers returns their sum")
void addingTwoPositiveNumbers() {
    // ARRANGE: set up the inputs and any objects you need
    int a = 3;
    int b = 4;

    // ACT: call the thing you are testing
    int result = a + b;

    // ASSERT: check that the result is what you expected
    assertEquals(7, result);
}
```

- **Arrange**: Get everything ready. Create objects, set up variables.
- **Act**: Call the method or perform the operation you are testing. Usually just one line.
- **Assert**: Check the outcome. If the assertion fails, the test fails with a helpful message.

Keeping these three phases separate makes your tests easy to read and easy to fix when they break.

---

## What to Do When a Test Fails

A failing test is not bad news — it is useful information. Read the failure message carefully:

```
expected: <7> but was: <-1>
```

This tells you exactly what went wrong. Find the code under test, fix the bug, run the tests again, and watch the test turn green. That satisfying green light is the goal.

---

## Your Next Step

Open `FoundationsTest.java` in this directory and read through the tests. Each one has a comment explaining *why* we test that particular thing. Once you understand the pattern, try writing a test of your own!
