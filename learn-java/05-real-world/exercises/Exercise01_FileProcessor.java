import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;

/**
 * Exercise01_FileProcessor.java
 *
 * GOAL: Read a CSV file of students, parse it, calculate statistics, and write a report.
 *
 * INPUT CSV FORMAT:
 * id,name,major,grade1,grade2,grade3,grade4,grade5
 * (grades are 0–100)
 *
 * OUTPUT REPORT FORMAT:
 * - Summary statistics (average, highest, lowest, median)
 * - Top 5 students
 * - Students at risk (average < 60)
 * - Grade distribution per major
 *
 * TASKS:
 * 1. Parse the CSV file into Student objects
 * 2. Calculate statistics (average, min, max, median)
 * 3. Find top 5 students by average grade
 * 4. Find students at risk
 * 5. Write a formatted text report
 * 6. Use try-with-resources for ALL file operations
 * 7. Handle malformed lines gracefully
 */
public class Exercise01_FileProcessor {

    record Student(String id, String name, String major, List<Integer> grades) {
        double average() {
            return grades.stream().mapToInt(Integer::intValue).average().orElse(0);
        }

        int highest() {
            return grades.stream().mapToInt(Integer::intValue).max().orElse(0);
        }

        int lowest() {
            return grades.stream().mapToInt(Integer::intValue).min().orElse(0);
        }

        String letterGrade() {
            double avg = average();
            if (avg >= 90) return "A";
            if (avg >= 80) return "B";
            if (avg >= 70) return "C";
            if (avg >= 60) return "D";
            return "F";
        }
    }

    // =========================================================
    // TODO 1: Parse CSV file
    //
    // - Skip the header row
    // - For each data row: parse id, name, major, grades
    // - Grades are columns 3 through the end
    // - Handle malformed rows: print a warning and skip
    // - Use try-with-resources
    // - Return a List<Student>
    // =========================================================
    public static List<Student> parseCsv(Path file) throws IOException {
        // YOUR CODE HERE
        return new ArrayList<>();
    }

    // =========================================================
    // TODO 2: Calculate overall class statistics
    //
    // Return a map with keys: "average", "highest", "lowest", "median"
    // Tip: to find median, sort all averages, then:
    //   - If odd count: take the middle element
    //   - If even count: average of the two middle elements
    // =========================================================
    public static Map<String, Double> classStatistics(List<Student> students) {
        // YOUR CODE HERE
        return new HashMap<>();
    }

    // =========================================================
    // TODO 3: Find top N students by average grade
    // (sorted by average descending, then name ascending for ties)
    // =========================================================
    public static List<Student> topStudents(List<Student> students, int n) {
        // YOUR CODE HERE
        return new ArrayList<>();
    }

    // =========================================================
    // TODO 4: Find students at risk (average below threshold)
    // Sort by average ascending (most at-risk first)
    // =========================================================
    public static List<Student> atRiskStudents(List<Student> students, double threshold) {
        // YOUR CODE HERE
        return new ArrayList<>();
    }

    // =========================================================
    // TODO 5: Group students by major and compute major averages
    // Return Map<major, average GPA>
    // =========================================================
    public static Map<String, Double> averageByMajor(List<Student> students) {
        // YOUR CODE HERE
        return new HashMap<>();
    }

    // =========================================================
    // TODO 6: Write a formatted text report to outputFile
    //
    // Include:
    // - Title and date
    // - Class statistics (average, highest, lowest, median)
    // - Top 5 students with their averages and letter grades
    // - Students at risk (below 60%)
    // - Average by major
    // - Use try-with-resources for the file writer
    // =========================================================
    public static void writeReport(Path outputFile, List<Student> students) throws IOException {
        // YOUR CODE HERE
    }

    // =========================================================
    // Main
    // =========================================================
    public static void main(String[] args) throws IOException {
        // Create sample input CSV
        String csvContent = """
                id,name,major,grade1,grade2,grade3,grade4,grade5
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
                INVALID,row without enough columns
                S012,Last Student,Engineering,100,98,99,97,100
                """;

        Path inputFile = Files.createTempFile("students", ".csv");
        Path reportFile = Files.createTempFile("report", ".txt");

        try {
            Files.writeString(inputFile, csvContent, StandardCharsets.UTF_8);

            // Parse
            List<Student> students = parseCsv(inputFile);
            System.out.println("Parsed " + students.size() + " students");

            // Statistics
            Map<String, Double> stats = classStatistics(students);
            System.out.printf("Class average: %.2f%n", stats.getOrDefault("average", 0.0));

            // Top students
            System.out.println("\nTop 3 students:");
            topStudents(students, 3).forEach(s ->
                System.out.printf("  %-15s %.2f (%s)%n", s.name(), s.average(), s.letterGrade()));

            // At risk
            System.out.println("\nAt-risk students (below 60):");
            atRiskStudents(students, 60).forEach(s ->
                System.out.printf("  %-15s %.2f%n", s.name(), s.average()));

            // By major
            System.out.println("\nAverage by major:");
            averageByMajor(students).entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %-20s %.2f%n", e.getKey(), e.getValue()));

            // Write report
            writeReport(reportFile, students);
            System.out.println("\n--- Report file contents ---");
            Files.readAllLines(reportFile).forEach(System.out::println);

        } finally {
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(reportFile);
        }
    }
}
