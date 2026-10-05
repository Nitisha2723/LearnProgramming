import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;

/**
 * FileIODemo.java
 *
 * Demonstrates modern Java file I/O:
 * - Reading files with Files.readString, Files.lines, BufferedReader
 * - Writing files with Files.writeString, BufferedWriter, PrintWriter
 * - Working with directories
 * - CSV processing
 * - Always using try-with-resources
 */
public class FileIODemo {

    // =========================================================
    // Helper: create temp file with content
    // =========================================================

    static Path createTempFile(String prefix, String content) throws IOException {
        Path temp = Files.createTempFile(prefix, ".txt");
        Files.writeString(temp, content, StandardCharsets.UTF_8);
        return temp;
    }

    // =========================================================
    // 1. Reading Files
    // =========================================================

    static void readingFilesDemo() throws IOException {
        System.out.println("=== Reading Files ===\n");

        String content = """
                Alice Chen, Computer Science, 3.9
                Bob Martinez, Mathematics, 3.5
                Charlie Davis, Computer Science, 3.2
                Diana Patel, Physics, 3.8
                Eve Wilson, Computer Science, 3.7
                """;

        Path file = createTempFile("students", content);

        try {
            // Method 1: Read entire file as String (small files)
            System.out.println("--- readString() ---");
            String text = Files.readString(file, StandardCharsets.UTF_8);
            System.out.println("File content:\n" + text.trim());

            // Method 2: Read as list of lines
            System.out.println("\n--- readAllLines() ---");
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            System.out.println("Number of lines: " + lines.size());
            System.out.println("First line: " + lines.get(0));

            // Method 3: Stream lines (lazy — ideal for large files)
            System.out.println("\n--- Files.lines() with Stream ---");
            try (Stream<String> stream = Files.lines(file, StandardCharsets.UTF_8)) {
                long csCount = stream
                    .filter(line -> line.contains("Computer Science"))
                    .count();
                System.out.println("Computer Science students: " + csCount);
            }
            // Stream (and file handle) is automatically closed by try-with-resources

            // Method 4: BufferedReader for line-by-line
            System.out.println("\n--- BufferedReader ---");
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                int lineNum = 1;
                while ((line = reader.readLine()) != null) {
                    System.out.printf("  Line %d: %s%n", lineNum++, line);
                }
            }

        } finally {
            Files.deleteIfExists(file);
        }
    }

    // =========================================================
    // 2. Writing Files
    // =========================================================

    static void writingFilesDemo() throws IOException {
        System.out.println("\n=== Writing Files ===\n");

        Path outputFile = Files.createTempFile("output", ".txt");

        try {
            // Method 1: Write a String
            Files.writeString(outputFile, "Line 1\nLine 2\nLine 3\n", StandardCharsets.UTF_8);
            System.out.println("Written with writeString()");

            // Method 2: Append to existing file
            Files.writeString(outputFile, "Line 4 (appended)\n",
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);
            System.out.println("Appended line 4");

            // Verify
            System.out.println("Contents:");
            Files.readAllLines(outputFile).forEach(l -> System.out.println("  " + l));

            // Method 3: Write list of lines
            Path linesFile = Files.createTempFile("lines", ".txt");
            try {
                List<String> lines = List.of("Alpha", "Beta", "Gamma", "Delta");
                Files.write(linesFile, lines, StandardCharsets.UTF_8);
                System.out.println("\nLines file: " + Files.readAllLines(linesFile));
            } finally {
                Files.deleteIfExists(linesFile);
            }

            // Method 4: BufferedWriter for many small writes
            Path bufFile = Files.createTempFile("buffered", ".txt");
            try {
                try (BufferedWriter writer = Files.newBufferedWriter(bufFile, StandardCharsets.UTF_8)) {
                    for (int i = 1; i <= 5; i++) {
                        writer.write("Record " + i);
                        writer.newLine();
                    }
                }
                System.out.println("\nBuffered write lines: " + Files.readAllLines(bufFile).size());
            } finally {
                Files.deleteIfExists(bufFile);
            }

            // Method 5: PrintWriter for formatted output
            Path reportFile = Files.createTempFile("report", ".txt");
            try {
                try (PrintWriter pw = new PrintWriter(
                        Files.newBufferedWriter(reportFile, StandardCharsets.UTF_8))) {
                    pw.printf("%-20s %6s %5s%n", "Name", "Score", "Grade");
                    pw.println("─".repeat(35));
                    pw.printf("%-20s %6d %5s%n", "Alice Chen",   95, "A");
                    pw.printf("%-20s %6d %5s%n", "Bob Martinez", 82, "B");
                    pw.printf("%-20s %6d %5s%n", "Charlie Davis",71, "C");
                }
                System.out.println("\nFormatted report:");
                Files.readAllLines(reportFile).forEach(l -> System.out.println("  " + l));
            } finally {
                Files.deleteIfExists(reportFile);
            }

        } finally {
            Files.deleteIfExists(outputFile);
        }
    }

    // =========================================================
    // 3. Working with Directories
    // =========================================================

    static void directoriesDemo() throws IOException {
        System.out.println("\n=== Directories ===\n");

        // Create a temp directory structure
        Path tempDir = Files.createTempDirectory("demo");
        Path subDir = tempDir.resolve("subdir");

        try {
            // Create directories
            Files.createDirectories(subDir);
            System.out.println("Created: " + tempDir);
            System.out.println("Created subdir: " + subDir);

            // Create some files
            Files.writeString(tempDir.resolve("file1.java"), "public class A {}");
            Files.writeString(tempDir.resolve("file2.java"), "public class B {}");
            Files.writeString(subDir.resolve("file3.java"),  "public class C {}");
            Files.writeString(tempDir.resolve("readme.txt"), "README content");

            // List direct children
            System.out.println("\nDirect children:");
            try (Stream<Path> children = Files.list(tempDir)) {
                children.forEach(p -> System.out.println("  " + p.getFileName()));
            }

            // Find all .java files recursively
            System.out.println("\nAll .java files:");
            try (Stream<Path> javaFiles = Files.walk(tempDir)) {
                javaFiles
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> System.out.println("  " + p.getFileName()));
            }

            // File metadata
            Path file1 = tempDir.resolve("file1.java");
            System.out.println("\nFile metadata for file1.java:");
            System.out.println("  Size: " + Files.size(file1) + " bytes");
            System.out.println("  Exists: " + Files.exists(file1));
            System.out.println("  Readable: " + Files.isReadable(file1));

        } finally {
            // Clean up entire temp directory tree
            try (Stream<Path> allFiles = Files.walk(tempDir)) {
                allFiles.sorted(Comparator.reverseOrder())  // Delete files before dirs
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); }
                            catch (IOException e) { /* best effort */ }
                        });
            }
            System.out.println("\nTemp directory cleaned up");
        }
    }

    // =========================================================
    // 4. CSV Processing
    // =========================================================

    record Student(String name, String major, double gpa) {}

    static List<Student> parseCsv(Path csvFile) throws IOException {
        List<Student> students = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();  // Skip header
            if (headerLine == null) return students;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 3) continue;

                try {
                    students.add(new Student(
                        parts[0].trim(),
                        parts[1].trim(),
                        Double.parseDouble(parts[2].trim())
                    ));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid line: " + line);
                }
            }
        }
        return students;
    }

    static void writeCsvReport(Path outputFile, List<Student> students) throws IOException {
        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8))) {

            pw.println("name,major,gpa,grade");

            for (Student s : students) {
                String grade;
                if      (s.gpa() >= 3.7) grade = "A";
                else if (s.gpa() >= 3.3) grade = "B";
                else if (s.gpa() >= 3.0) grade = "C";
                else                      grade = "D";

                pw.printf("%s,%s,%.2f,%s%n", s.name(), s.major(), s.gpa(), grade);
            }
        }
    }

    static void csvProcessingDemo() throws IOException {
        System.out.println("\n=== CSV Processing ===\n");

        // Create input CSV
        String csvContent = """
                name,major,gpa
                Alice Chen,Computer Science,3.9
                Bob Martinez,Mathematics,3.5
                Charlie Davis,Computer Science,3.2
                Diana Patel,Physics,3.8
                Eve Wilson,Computer Science,3.7
                invalid line
                Frank Johnson,Mathematics,2.9
                """;

        Path inputFile = Files.createTempFile("input", ".csv");
        Path outputFile = Files.createTempFile("output", ".csv");

        try {
            Files.writeString(inputFile, csvContent, StandardCharsets.UTF_8);

            // Parse CSV
            List<Student> students = parseCsv(inputFile);
            System.out.println("Parsed " + students.size() + " students:");
            students.forEach(s ->
                System.out.printf("  %-15s %-20s %.2f%n", s.name(), s.major(), s.gpa()));

            // Process: sort by GPA desc, filter CS students
            System.out.println("\nComputer Science students (sorted by GPA):");
            students.stream()
                .filter(s -> s.major().equals("Computer Science"))
                .sorted(Comparator.comparingDouble(Student::gpa).reversed())
                .forEach(s -> System.out.printf("  %-15s %.2f%n", s.name(), s.gpa()));

            // Write report CSV
            List<Student> sorted = students.stream()
                .sorted(Comparator.comparingDouble(Student::gpa).reversed())
                .collect(Collectors.toList());
            writeCsvReport(outputFile, sorted);

            System.out.println("\nReport CSV:");
            Files.readAllLines(outputFile).forEach(l -> System.out.println("  " + l));

        } finally {
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
        }
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) throws IOException {
        readingFilesDemo();
        writingFilesDemo();
        directoriesDemo();
        csvProcessingDemo();
    }
}
