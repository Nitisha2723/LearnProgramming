# File I/O in Modern Java

## The Modern Java I/O API

Java has had multiple I/O APIs across its history:
- **java.io** (original) — `FileInputStream`, `FileReader`, `BufferedReader`, etc.
- **java.nio** (Java 1.4) — non-blocking I/O channels
- **java.nio.file (NIO.2)** (Java 7) — `Path`, `Files`, `Paths` — **the recommended API**

For most use cases today, use `java.nio.file.Files` and `java.nio.file.Path`.

---

## Core Concepts: Path and Files

```java
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

// Creating Path objects
Path file = Path.of("data/students.csv");          // Java 11+
Path file = Paths.get("data/students.csv");         // Pre-Java 11 (still works)
Path absolute = Path.of("/home/user/data/file.txt");
Path home = Path.of(System.getProperty("user.home"), "documents", "data.txt");

// Path manipulation (no I/O happens — just string operations)
path.getFileName()   // → "students.csv"
path.getParent()     // → "data"
path.toAbsolutePath()// → "/current/working/dir/data/students.csv"
path.resolve("more") // → "data/students.csv/more"
path.resolveSibling("output.txt") // → "data/output.txt"
```

---

## Reading Files

### Read Entire File as String (small files)

```java
// Java 11+ — read whole file as a String
String content = Files.readString(Path.of("readme.txt"));

// Java 7+ — read as list of lines
List<String> lines = Files.readAllLines(Path.of("readme.txt"));
// Charset defaults to UTF-8; specify if needed:
List<String> lines = Files.readAllLines(Path.of("readme.txt"), StandardCharsets.ISO_8859_1);
```

### Read as Byte Array

```java
byte[] bytes = Files.readAllBytes(Path.of("image.png"));
```

### Streaming Lines (large files)

For large files, don't load everything into memory — stream the lines:

```java
// Files.lines() returns a Stream<String> — processes lazily
try (Stream<String> lines = Files.lines(Path.of("large-log.txt"))) {
    long errorCount = lines
        .filter(line -> line.contains("ERROR"))
        .count();
    System.out.println("Errors: " + errorCount);
}
// IMPORTANT: The stream (and underlying file handle) is closed by try-with-resources
```

### BufferedReader (when you need line-by-line control)

```java
try (BufferedReader reader = Files.newBufferedReader(Path.of("data.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}
```

---

## Writing Files

### Write String to File

```java
// Java 11+ — write a String
Files.writeString(Path.of("output.txt"), "Hello, World!\n");

// Append to existing file
Files.writeString(Path.of("log.txt"),
    "New entry\n",
    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
```

### Write Lines

```java
List<String> lines = List.of("line1", "line2", "line3");
Files.write(Path.of("output.txt"), lines);

// With charset and append option
Files.write(Path.of("output.txt"), lines,
    StandardCharsets.UTF_8,
    StandardOpenOption.APPEND);
```

### BufferedWriter (for many small writes)

```java
try (BufferedWriter writer = Files.newBufferedWriter(Path.of("output.txt"))) {
    writer.write("First line");
    writer.newLine();
    writer.write("Second line");
    writer.newLine();
}
```

### PrintWriter (formatted output)

```java
try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Path.of("report.txt")))) {
    pw.printf("%-20s %5s %6s%n", "Name", "Score", "Grade");
    pw.println("-".repeat(33));
    pw.printf("%-20s %5d %6s%n", "Alice", 95, "A");
    pw.printf("%-20s %5d %6s%n", "Bob", 82, "B");
}
```

---

## Working With Directories

```java
// Check if path exists
Files.exists(path)          // true/false
Files.notExists(path)       // true/false
Files.isDirectory(path)
Files.isRegularFile(path)
Files.isReadable(path)
Files.isWritable(path)

// Create directories
Files.createDirectory(Path.of("newdir"));           // One level only
Files.createDirectories(Path.of("a/b/c/d"));        // All levels

// List directory contents
List<Path> entries = Files.list(Path.of("srcdir"))
    .collect(Collectors.toList());

// List recursively
Files.walk(Path.of("project"))
    .filter(p -> p.toString().endsWith(".java"))
    .forEach(System.out::println);

// Find files matching a glob pattern
Files.find(Path.of("project"), Integer.MAX_VALUE,
    (path, attrs) -> path.toString().endsWith(".java") && attrs.isRegularFile())
    .forEach(System.out::println);

// Copy and move
Files.copy(source, destination);
Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);

// Delete
Files.delete(path);              // Throws if doesn't exist
Files.deleteIfExists(path);      // Safe version

// Get file metadata
BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
attrs.size()
attrs.creationTime()
attrs.lastModifiedTime()
```

---

## CSV Parsing Basics

Java doesn't have a built-in CSV parser. For production use, use a library like Apache Commons CSV or OpenCSV. For learning, here's a basic approach:

```java
public class CsvParser {

    // Simple CSV line parser (doesn't handle quoted fields with commas)
    public static String[] parseLine(String line) {
        return line.split(",", -1);  // -1 preserves trailing empty strings
    }

    // Parse a CSV file with a header row
    public static List<Map<String, String>> parseCsv(Path file) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String headerLine = reader.readLine();
            if (headerLine == null) return records;

            String[] headers = headerLine.split(",");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] values = line.split(",", -1);
                Map<String, String> record = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    record.put(headers[i].trim(),
                               i < values.length ? values[i].trim() : "");
                }
                records.add(record);
            }
        }

        return records;
    }

    // Write records back to CSV
    public static void writeCsv(Path file, List<String[]> rows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            for (String[] row : rows) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
    }
}
```

### Handling Quoted Fields

For production CSV parsing with quoted fields, use Apache Commons CSV:

```java
// Add to pom.xml:
// <dependency>
//   <groupId>org.apache.commons</groupId>
//   <artifactId>commons-csv</artifactId>
//   <version>1.10.0</version>
// </dependency>

try (
    Reader reader = Files.newBufferedReader(Path.of("data.csv"));
    CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
) {
    for (CSVRecord record : parser) {
        String name = record.get("name");
        String email = record.get("email");
        System.out.println(name + " <" + email + ">");
    }
}
```

---

## JSON Basics

Java doesn't have built-in JSON support. The two most popular libraries are:

### Jackson (most feature-rich)

```java
// pom.xml: <artifactId>jackson-databind</artifactId>
ObjectMapper mapper = new ObjectMapper();

// Serialize object to JSON
String json = mapper.writeValueAsString(myObject);
// {"name":"Alice","age":30}

// Write to file
mapper.writeValue(new File("output.json"), myObject);
mapper.writerWithDefaultPrettyPrinter().writeValue(new File("output.json"), myObject);

// Deserialize JSON to object
MyClass obj = mapper.readValue(jsonString, MyClass.class);
MyClass obj = mapper.readValue(new File("data.json"), MyClass.class);

// Parse JSON without a POJO
JsonNode root = mapper.readTree(jsonString);
String name = root.get("name").asText();
int age = root.get("age").asInt();
```

### Gson (simpler API)

```java
// pom.xml: <artifactId>gson</artifactId>
Gson gson = new Gson();
String json = gson.toJson(myObject);
MyClass obj = gson.fromJson(json, MyClass.class);

// Pretty print
Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
String pretty = prettyGson.toJson(myObject);
```

---

## Best Practices

1. **Always use try-with-resources** for file operations — never leave files open

```java
// ✓ CORRECT
try (BufferedReader r = Files.newBufferedReader(path)) { ... }

// ❌ WRONG — what if an exception occurs before close()?
BufferedReader r = Files.newBufferedReader(path);
// ...work...
r.close();
```

2. **Use `Files.lines()` with try-with-resources for large files** — don't load entire files into memory

3. **Use `Path.of()` over `new File()`** — modern API, better methods

4. **Specify charset explicitly** for non-ASCII content:

```java
Files.readString(path, StandardCharsets.UTF_8);
Files.writeString(path, content, StandardCharsets.UTF_8);
```

5. **Create parent directories if needed**:

```java
Path output = Path.of("reports/2024/january/summary.csv");
Files.createDirectories(output.getParent());  // Create reports/2024/january/
Files.writeString(output, content);
```

6. **Use `Files.writeString()` with `APPEND` option instead of manual FileWriter**:

```java
// ✓ Clean
Files.writeString(logFile, newEntry + "\n",
    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
```

7. **Check if file exists before reading** to give better error messages:

```java
if (!Files.exists(inputPath)) {
    throw new IllegalArgumentException("Input file not found: " + inputPath.toAbsolutePath());
}
```
