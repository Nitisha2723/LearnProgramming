# CSV Analyzer — Module 05 Mini-Project

A command-line Java application that reads a CSV file of student grades, performs a
comprehensive statistical analysis, and writes a formatted report. Every line of code
demonstrates at least one Module 05 concept.

---

## Skills Demonstrated

| Concept | Where |
|---|---|
| File I/O (NIO.2 Path/Files) | `CsvAnalyzer.parseCsv()`, `StudentReport.writeTo()` |
| try-with-resources | `BufferedReader`, `PrintWriter` in both classes |
| Custom checked exception | `CsvReadException`, `InvalidCsvFormatException` |
| Custom unchecked exception | `MalformedRowException` |
| Exception chaining | `CsvReadException(msg, cause)` wraps `IOException` |
| Stream filter/map/sorted | `topStudents()`, `atRiskStudents()` |
| groupingBy + collectingAndThen | `statsByMajor()` |
| DoubleSummaryStatistics | `classStats()` via `summaryStatistics()` |
| Optional.orElse / average() | `Student.average()` via `OptionalDouble.orElse(0)` |
| Method references | `Comparator.comparing(Student::major)` etc. |
| Separation of concerns | `CsvAnalyzer` (data) vs `StudentReport` (formatting) |

---

## Directory Structure

```
mini-project/
└── csv-analyzer/
    ├── CsvAnalyzer.java    ← Main analysis engine + entry point
    └── StudentReport.java  ← Report formatter (no analysis logic)
```

---

## Input CSV Format

```
id,name,major,grade1,grade2,grade3,...,gradeN
S001,Alice Chen,Computer Science,92,88,95,90,94
S002,Bob Martinez,Mathematics,78,82,75,80,77
```

Rules:
- First row is the header (any column names work; only positions matter)
- Column 1 = student ID
- Column 2 = full name
- Column 3 = major
- Columns 4+ = individual exam scores (integers 0–100, variable count)
- Malformed rows are skipped with a warning (not a fatal error)

---

## Output Report Sections

1. **Title block** — timestamp, student count, parse warning count
2. **Class Statistics** — average, median, highest, lowest
3. **Grade Distribution** — A/B/C/D/F counts, percentages, ASCII bar chart
4. **Top 5 Students** — ranked by average, tie-broken by name
5. **At-Risk Students** — average below 60, sorted worst-first
6. **Performance by Major** — headcount + average per major, sorted best-first
7. **Full Roster** — all students, sorted by major then average
8. **Parse Warnings** *(only if any)* — list of skipped rows with reason

---

## Running the Demo

```bash
# Compile both files (they reference each other's inner types)
javac CsvAnalyzer.java StudentReport.java

# Run the built-in demo (creates and analyzes a temp CSV automatically)
java CsvAnalyzer
```

The demo creates a temporary CSV, runs the full analysis, prints a summary to the
console, writes the report to a temp file, then displays the full report text.

---

## Using with Your Own CSV

```java
import java.nio.file.Path;

Path myData   = Path.of("data/students.csv");
Path myReport = Path.of("output/report.txt");

CsvAnalyzer analyzer = new CsvAnalyzer(myData);
analyzer.analyze();          // parse + compute
analyzer.printSummary();     // console summary
analyzer.writeReport(myReport); // full file report
```

---

## Error Handling Design

```
CsvReadException (checked)        — file can't be opened; caller must decide: abort? retry? prompt for new path?
InvalidCsvFormatException (checked) — header missing or too short; file is structurally wrong
MalformedRowException (unchecked)  — single bad row; caught internally, row is skipped with a warning
```

The design follows the "checked for things the caller should handle, unchecked for bugs"
principle — except `MalformedRowException` is unchecked because it's handled internally
rather than propagated to calling code.

---

## Key Implementation Notes

### Median Calculation

```java
int n = averages.size();
double median = (n % 2 == 1)
    ? averages.get(n / 2)
    : (averages.get(n / 2 - 1) + averages.get(n / 2)) / 2.0;
```

Requires the list to be **sorted** first — `classStats()` uses a sorted collect.

### Grade Distribution with Missing Grades

```java
Map<String, Long> dist = students.stream()
    .collect(Collectors.groupingBy(Student::letterGrade, Collectors.counting()));
```

The report writes all 5 letter grades (A-F) even if some have 0 count by using
`dist.getOrDefault(grade, 0L)` rather than iterating the map directly.

### Sorted Major Breakdown

```java
statsByMajor.entrySet().stream()
    .sorted(Comparator.comparingDouble(
        (Map.Entry<String, MajorStats> e) -> e.getValue().averageGrade()
    ).reversed())
    .forEach(...)
```

The explicit cast `(Map.Entry<String, MajorStats> e)` is needed because Java's type
inference can't resolve the generic bound on `comparingDouble` when combined with
`.reversed()`.
