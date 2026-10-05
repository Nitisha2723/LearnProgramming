import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

/**
 * CsvAnalyzer.java — Mini-Project for Module 05: Real-World Java
 *
 * Demonstrates the full Module 05 skill set in one cohesive program:
 *   - File I/O with NIO.2 (Path, Files, try-with-resources)
 *   - Stream pipelines (filter, map, flatMap, sorted, groupingBy, ...)
 *   - Optional usage (ofNullable, map, orElse, orElseThrow)
 *   - Custom exceptions (checked + unchecked)
 *   - Meaningful error reporting
 *
 * INPUT: A CSV file with this header:
 *   id,name,major,grade1,grade2,...,gradeN
 * (variable number of grade columns, all 0–100)
 *
 * OUTPUT: A plain-text analysis report containing:
 *   1. Class-level statistics (average, median, highest, lowest)
 *   2. Grade distribution (A/B/C/D/F counts and percentages)
 *   3. Top 5 students
 *   4. Students at risk (average below 60)
 *   5. Per-major breakdown (headcount + average)
 *
 * USAGE:
 *   CsvAnalyzer analyzer = new CsvAnalyzer(inputPath);
 *   analyzer.analyze();
 *   analyzer.writeReport(outputPath);
 *   analyzer.printSummary();
 */
public class CsvAnalyzer {

    // =========================================================
    // Custom exceptions
    // =========================================================

    /** Thrown when the CSV file cannot be read or is missing. */
    static class CsvReadException extends Exception {
        CsvReadException(String msg, Throwable cause) { super(msg, cause); }
    }

    /** Thrown when a specific row cannot be parsed. Unchecked because callers usually skip & continue. */
    static class MalformedRowException extends RuntimeException {
        private final int lineNumber;
        MalformedRowException(int line, String msg) {
            super("Line " + line + ": " + msg);
            this.lineNumber = line;
        }
        int getLineNumber() { return lineNumber; }
    }

    /** Thrown when the CSV has no header or fewer than 2 columns. */
    static class InvalidCsvFormatException extends Exception {
        InvalidCsvFormatException(String msg) { super(msg); }
    }

    // =========================================================
    // Domain model
    // =========================================================

    /**
     * Represents one student parsed from the CSV.
     * Immutable record — all logic via methods.
     */
    record Student(String id, String name, String major, List<Integer> grades) {

        /** Average grade across all exams. */
        double average() {
            return grades.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        }

        int highest() {
            return grades.stream().mapToInt(Integer::intValue).max().orElse(0);
        }

        int lowest() {
            return grades.stream().mapToInt(Integer::intValue).min().orElse(0);
        }

        /** Letter grade based on average. */
        String letterGrade() {
            double avg = average();
            if (avg >= 90) return "A";
            if (avg >= 80) return "B";
            if (avg >= 70) return "C";
            if (avg >= 60) return "D";
            return "F";
        }

        boolean isAtRisk() { return average() < 60.0; }

        @Override
        public String toString() {
            return String.format("Student{id=%s, name=%s, major=%s, avg=%.1f}",
                id, name, major, average());
        }
    }

    /** Summary statistics for the entire class. */
    record ClassStats(
        double average,
        double median,
        double highest,
        double lowest,
        int totalStudents,
        Map<String, Long> gradeDistribution
    ) {}

    // =========================================================
    // State
    // =========================================================

    private final Path inputPath;
    private List<Student> students = Collections.emptyList();
    private List<String> parseWarnings = new ArrayList<>();
    private boolean analyzed = false;

    public CsvAnalyzer(Path inputPath) {
        this.inputPath = Objects.requireNonNull(inputPath, "inputPath cannot be null");
    }

    // =========================================================
    // Parse
    // =========================================================

    /**
     * Reads and parses the CSV file into a list of Student objects.
     * Skips malformed rows (with a warning) rather than aborting.
     *
     * @throws CsvReadException          if the file cannot be opened
     * @throws InvalidCsvFormatException if the header is missing or too short
     */
    public List<Student> parseCsv() throws CsvReadException, InvalidCsvFormatException {
        List<Student> parsed = new ArrayList<>();
        parseWarnings.clear();

        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new InvalidCsvFormatException("File is empty or has no header: " + inputPath);
            }

            String[] headers = headerLine.split(",", -1);
            if (headers.length < 4) {
                throw new InvalidCsvFormatException(
                    "Header must have at least 4 columns (id, name, major, grade1...). Found: "
                    + headers.length);
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;

                try {
                    Student s = parseRow(line, lineNum);
                    parsed.add(s);
                } catch (MalformedRowException e) {
                    parseWarnings.add("WARNING - " + e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new CsvReadException("Failed to read CSV file: " + inputPath, e);
        }

        return parsed;
    }

    private Student parseRow(String line, int lineNum) {
        String[] parts = line.split(",", -1);

        if (parts.length < 4) {
            throw new MalformedRowException(lineNum,
                "Row has too few columns (need id,name,major,grade1+). Got: " + parts.length);
        }

        String id    = parts[0].trim();
        String name  = parts[1].trim();
        String major = parts[2].trim();

        if (id.isEmpty())    throw new MalformedRowException(lineNum, "id is empty");
        if (name.isEmpty())  throw new MalformedRowException(lineNum, "name is empty");
        if (major.isEmpty()) throw new MalformedRowException(lineNum, "major is empty");

        List<Integer> grades = new ArrayList<>();
        for (int i = 3; i < parts.length; i++) {
            String cell = parts[i].trim();
            if (cell.isEmpty()) continue;
            try {
                int grade = Integer.parseInt(cell);
                if (grade < 0 || grade > 100) {
                    throw new MalformedRowException(lineNum,
                        "Grade out of range [0,100]: " + grade);
                }
                grades.add(grade);
            } catch (NumberFormatException e) {
                throw new MalformedRowException(lineNum, "Non-numeric grade: '" + cell + "'");
            }
        }

        if (grades.isEmpty()) {
            throw new MalformedRowException(lineNum, "No valid grades found on row");
        }

        return new Student(id, name, major, Collections.unmodifiableList(grades));
    }

    // =========================================================
    // Analysis
    // =========================================================

    /**
     * Runs the full analysis pipeline.
     * Must be called before writeReport() or printSummary().
     */
    public void analyze() throws CsvReadException, InvalidCsvFormatException {
        students = parseCsv();
        analyzed = true;
    }

    /** Class-level statistics. */
    public ClassStats classStats() {
        requireAnalyzed();
        if (students.isEmpty()) {
            return new ClassStats(0, 0, 0, 0, 0, Map.of());
        }

        List<Double> averages = students.stream()
            .map(Student::average)
            .sorted()
            .collect(Collectors.toList());

        int n = averages.size();
        double median = (n % 2 == 1)
            ? averages.get(n / 2)
            : (averages.get(n / 2 - 1) + averages.get(n / 2)) / 2.0;

        DoubleSummaryStatistics dss = averages.stream()
            .mapToDouble(Double::doubleValue)
            .summaryStatistics();

        // Grade distribution: A/B/C/D/F counts
        Map<String, Long> dist = students.stream()
            .collect(Collectors.groupingBy(Student::letterGrade, Collectors.counting()));

        return new ClassStats(
            dss.getAverage(),
            median,
            dss.getMax(),
            dss.getMin(),
            students.size(),
            dist
        );
    }

    /** Top N students by average (descending), then name (ascending) for ties. */
    public List<Student> topStudents(int n) {
        requireAnalyzed();
        return students.stream()
            .sorted(Comparator.comparingDouble(Student::average).reversed()
                .thenComparing(Student::name))
            .limit(n)
            .collect(Collectors.toList());
    }

    /** Students with average below the at-risk threshold (default 60.0). */
    public List<Student> atRiskStudents(double threshold) {
        requireAnalyzed();
        return students.stream()
            .filter(s -> s.average() < threshold)
            .sorted(Comparator.comparingDouble(Student::average))
            .collect(Collectors.toList());
    }

    /**
     * Per-major breakdown: headcount and average grade.
     * Returns sorted by average descending.
     */
    public Map<String, MajorStats> statsByMajor() {
        requireAnalyzed();
        return students.stream()
            .collect(Collectors.groupingBy(
                Student::major,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new MajorStats(
                        list.size(),
                        list.stream().mapToDouble(Student::average).average().orElse(0)
                    )
                )
            ));
    }

    /** Statistics for a single major. */
    record MajorStats(int count, double averageGrade) {}

    // =========================================================
    // Report generation (StudentReport delegate)
    // =========================================================

    /**
     * Writes the full analysis report to outputPath.
     * Uses StudentReport to format each section.
     */
    public void writeReport(Path outputPath) throws IOException {
        requireAnalyzed();
        StudentReport report = new StudentReport(
            students,
            classStats(),
            topStudents(5),
            atRiskStudents(60.0),
            statsByMajor(),
            parseWarnings
        );
        report.writeTo(outputPath);
    }

    /** Prints a concise summary to System.out. */
    public void printSummary() {
        requireAnalyzed();
        ClassStats cs = classStats();
        System.out.printf("--- CSV Analyzer Summary (%s) ---%n", inputPath.getFileName());
        System.out.printf("Students parsed : %d%n", cs.totalStudents());
        System.out.printf("Parse warnings  : %d%n", parseWarnings.size());
        System.out.printf("Class average   : %.2f%n", cs.average());
        System.out.printf("Median          : %.2f%n", cs.median());
        System.out.printf("Highest avg     : %.2f%n", cs.highest());
        System.out.printf("Lowest avg      : %.2f%n", cs.lowest());
        System.out.println("Grade dist.     : " + cs.gradeDistribution());

        List<Student> atRisk = atRiskStudents(60.0);
        if (!atRisk.isEmpty()) {
            System.out.println("AT RISK (" + atRisk.size() + "):");
            atRisk.forEach(s -> System.out.printf("  - %-20s %.1f%n", s.name(), s.average()));
        }
    }

    /** Returns any warnings collected during parsing. */
    public List<String> getParseWarnings() {
        return Collections.unmodifiableList(parseWarnings);
    }

    private void requireAnalyzed() {
        if (!analyzed) {
            throw new IllegalStateException(
                "Call analyze() before accessing results.");
        }
    }

    // =========================================================
    // Entry point — sample demonstration
    // =========================================================

    public static void main(String[] args) throws Exception {
        // Write a sample CSV to a temp file and analyze it
        String sampleCsv = """
                id,name,major,exam1,exam2,exam3,exam4,exam5
                S001,Alice Chen,Computer Science,92,88,95,90,94
                S002,Bob Martinez,Mathematics,78,82,75,80,77
                S003,Charlie Davis,Computer Science,65,70,62,68,71
                S004,Diana Patel,Physics,95,97,93,96,98
                S005,Eve Wilson,Computer Science,85,88,82,90,87
                S006,Frank Johnson,Mathematics,55,50,58,52,60
                S007,Grace Kim,Physics,88,91,85,89,92
                S008,Henry Brown,Computer Science,45,52,48,55,50
                S009,Ivan Lee,Engineering,72,68,75,70,73
                S010,Julia White,Engineering,91,88,94,90,92
                BAD_ROW_NO_GRADES
                S012,Lena Schmidt,Engineering,100,98,99,97,100
                S013,Max Weber,Mathematics,72,78,68,74,76
                S014,Nora Fischer,Physics,60,58,62,55,65
                """;

        Path inputFile  = Files.createTempFile("students", ".csv");
        Path outputFile = Files.createTempFile("report",   ".txt");

        try {
            Files.writeString(inputFile, sampleCsv, StandardCharsets.UTF_8);

            CsvAnalyzer analyzer = new CsvAnalyzer(inputFile);
            analyzer.analyze();

            // Print summary to console
            System.out.println();
            analyzer.printSummary();

            if (!analyzer.getParseWarnings().isEmpty()) {
                System.out.println("\nParse warnings:");
                analyzer.getParseWarnings().forEach(System.out::println);
            }

            // Write report to file, then display it
            analyzer.writeReport(outputFile);
            System.out.println("\n=== FULL REPORT ===");
            Files.readAllLines(outputFile).forEach(System.out::println);

        } finally {
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
        }
    }
}
