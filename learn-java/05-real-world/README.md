# Module 05 — Real-World Java

Practical Java skills used in every professional codebase: robust error handling,
file I/O, stream pipelines, null-safe code with Optional, and writing testable code
with mocks.

---

## Learning Objectives

By the end of this module you can:

- Design a custom exception hierarchy (checked vs unchecked) and explain when to use each
- Read and write files using Java NIO.2 (`Path`, `Files`, `BufferedReader`/`Writer`) with try-with-resources
- Write multi-step stream pipelines using `filter`, `map`, `flatMap`, `sorted`, `reduce`, and `Collectors`
- Replace null-returning methods with `Optional` and chain safely using `map`/`flatMap`/`orElseThrow`
- Write Mockito tests with `@Mock`, `@InjectMocks`, `when/thenReturn`, `verify`, and `assertThrows`

---

## Module Structure

```
05-real-world/
├── theory/
│   ├── 01-exceptions.md          ← Exception hierarchy, checked vs unchecked, try-with-resources
│   ├── 02-file-io.md             ← NIO.2 Path/Files, CSV parsing, directory operations
│   ├── 03-java-8-streams.md      ← Stream pipeline: source → intermediate → terminal
│   ├── 04-optionals.md           ← Optional creation, chaining, anti-patterns
│   └── 05-testing-deep-dive.md   ← Test doubles, Mockito, code coverage, test pyramid
│
├── code/
│   ├── ExceptionsDemo.java       ← Custom exceptions, try-with-resources, exception chaining
│   ├── FileIODemo.java           ← Reading/writing files, CSV processing, directory ops
│   ├── StreamsDemo.java          ← All intermediate & terminal ops, method references
│   └── OptionalsDemo.java        ← Optional patterns, anti-patterns, stream integration
│
├── exercises/
│   ├── Exercise01_FileProcessor.java  ← Parse CSV → statistics → write report
│   ├── Exercise02_StreamPipelines.java ← 5 stream queries on employee data
│   └── solutions/
│       ├── Exercise01_FileProcessor.java
│       └── Exercise02_StreamPipelines.java
│
├── tests/
│   ├── ExceptionsTest.java  ← Custom exceptions, try-with-resources, Mockito
│   └── StreamsTest.java     ← Full stream pipeline coverage, edge cases
│
└── mini-project/
    └── csv-analyzer/
        ├── CsvAnalyzer.java    ← Full analysis engine (all five concepts in one program)
        └── StudentReport.java  ← Formatted report writer
```

---

## Theory Summaries

### 01 — Exceptions

Java's exception hierarchy splits at `Throwable`:
- `Error` — JVM problems (OutOfMemoryError); never catch
- `Exception` → `RuntimeException` — **unchecked**; caller not forced to handle
- `Exception` (non-Runtime) — **checked**; must catch or declare `throws`

Rule of thumb: use checked for **recoverable** situations the caller should handle
(file not found, network timeout); use unchecked for **programming errors**
(null where not allowed, illegal argument).

### 02 — File I/O (NIO.2)

Always use `try-with-resources`. Prefer the high-level `Files` methods:

```java
// Reading
String content = Files.readString(path);          // whole file
List<String> lines = Files.readAllLines(path);    // all lines into List
try (Stream<String> s = Files.lines(path)) { ... } // lazy, must close

// Writing
Files.writeString(path, content);                 // overwrite
Files.writeString(path, content, StandardOpenOption.APPEND);
```

### 03 — Java 8 Streams

Three-stage pipeline: **source → intermediate (lazy) → terminal (eager)**.

```java
List<String> result = employees.stream()          // source
    .filter(e -> e.salary() > 80_000)             // intermediate
    .map(Employee::name)                           // intermediate
    .sorted()                                      // intermediate
    .collect(Collectors.toList());                 // terminal
```

Key collectors: `groupingBy`, `counting`, `averagingDouble`, `joining`,
`partitioningBy`, `toMap`, `collectingAndThen`, `flatMapping` (Java 9+).

### 04 — Optional

`Optional<T>` is a container that may or may not hold a value. Use it as a
**return type** to signal "this might not exist" without null:

```java
Optional<User> user = repository.findById(id);
String email = user
    .filter(u -> u.isActive())
    .map(User::getEmail)
    .orElse("no-email@example.com");
```

Never use `Optional.get()` without `isPresent()` first — that defeats the purpose.
Prefer `orElse`, `orElseGet`, `orElseThrow`, or `ifPresent`.

### 05 — Testing Deep Dive

Test doubles:
| Type | Description |
|------|-------------|
| **Mock** | Records calls; you verify interactions |
| **Stub** | Returns canned responses; no call verification |
| **Spy** | Real object + partial override |
| **Fake** | Lightweight in-memory implementation |

Mockito quick reference:
```java
@ExtendWith(MockitoExtension.class)
class MyTest {
    @Mock UserRepository repo;
    @InjectMocks UserService service;

    @Test void shouldReturnEmail() {
        when(repo.findById(1)).thenReturn(Optional.of(user));
        assertEquals("alice@example.com", service.getEmail(1));
        verify(repo).findById(1);
    }
}
```

---

## Exercises

### Exercise 01 — File Processor
Read a student-grades CSV, compute statistics (average, median, high/low),
find top students and at-risk students, write a formatted report.

Concepts: NIO.2, try-with-resources, DoubleSummaryStatistics, stream sorting, PrintWriter

### Exercise 02 — Stream Pipelines
5 queries on a 12-employee dataset — no for-loops allowed:
1. Salary summary by department (`groupingBy + collectingAndThen`)
2. Skills only managers have (flatMap + set difference)
3. Distinct engineering cities (filter + distinct + joining)
4. Promotion candidates (multiple filters + `toMap` with `LinkedHashMap::new`)
5. Department skill matrix (`groupingBy + flatMapping`)

---

## Mini-Project — CSV Analyzer

A self-contained program combining all five Module 05 skills.

**Run it:**
```bash
cd mini-project/csv-analyzer
javac CsvAnalyzer.java StudentReport.java
java CsvAnalyzer
```

See `mini-project/README.md` for full documentation.

---

## Dependencies (pom.xml excerpt)

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.5.0</version>
    <scope>test</scope>
</dependency>
```

---

## Prerequisites

- Module 01 — Java Basics (syntax, variables, control flow)
- Module 02 — OOP (classes, interfaces, generics)
- Module 03 — Functional Java (lambdas, method references) — *helpful but not required*
- Module 04 — Data Structures (collections, Map, List) — *helpful for exercises*
