import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;

/**
 * Solution to Exercise01_FileProcessor
 */
public class Exercise01_FileProcessor {

    record Student(String id, String name, String major, List<Integer> grades) {
        double average() {
            return grades.stream().mapToInt(Integer::intValue).average().orElse(0);
        }
        int highest() { return grades.stream().mapToInt(Integer::intValue).max().orElse(0); }
        int lowest()  { return grades.stream().mapToInt(Integer::intValue).min().orElse(0); }
        String letterGrade() {
            double avg = average();
            if (avg >= 90) return "A";
            if (avg >= 80) return "B";
            if (avg >= 70) return "C";
            if (avg >= 60) return "D";
            return "F";
        }
    }

    public static List<Student> parseCsv(Path file) throws IOException {
        List<Student> students = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine(); // skip header
            if (header == null) return students;

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 4) {
                    System.err.println("Warning: skipping malformed line " + lineNum + ": " + line);
                    continue;
                }

                try {
                    List<Integer> grades = new ArrayList<>();
                    for (int i = 3; i < parts.length; i++) {
                        if (!parts[i].trim().isEmpty()) {
                            grades.add(Integer.parseInt(parts[i].trim()));
                        }
                    }
                    if (grades.isEmpty()) {
                        System.err.println("Warning: no grades on line " + lineNum);
                        continue;
                    }
                    students.add(new Student(parts[0].trim(), parts[1].trim(),
                        parts[2].trim(), grades));
                } catch (NumberFormatException e) {
                    System.err.println("Warning: invalid grade on line " + lineNum + ": " + line);
                }
            }
        }
        return students;
    }

    public static Map<String, Double> classStatistics(List<Student> students) {
        if (students.isEmpty()) return Map.of();

        List<Double> averages = students.stream()
            .map(Student::average)
            .sorted()
            .collect(Collectors.toList());

        double median;
        int n = averages.size();
        if (n % 2 == 1) {
            median = averages.get(n / 2);
        } else {
            median = (averages.get(n / 2 - 1) + averages.get(n / 2)) / 2.0;
        }

        DoubleSummaryStatistics stats = averages.stream()
            .mapToDouble(Double::doubleValue).summaryStatistics();

        return Map.of(
            "average", stats.getAverage(),
            "highest", stats.getMax(),
            "lowest",  stats.getMin(),
            "median",  median
        );
    }

    public static List<Student> topStudents(List<Student> students, int n) {
        return students.stream()
            .sorted(Comparator.comparingDouble(Student::average).reversed()
                .thenComparing(Student::name))
            .limit(n)
            .collect(Collectors.toList());
    }

    public static List<Student> atRiskStudents(List<Student> students, double threshold) {
        return students.stream()
            .filter(s -> s.average() < threshold)
            .sorted(Comparator.comparingDouble(Student::average))
            .collect(Collectors.toList());
    }

    public static Map<String, Double> averageByMajor(List<Student> students) {
        return students.stream()
            .collect(Collectors.groupingBy(
                Student::major,
                Collectors.averagingDouble(Student::average)
            ));
    }

    public static void writeReport(Path outputFile, List<Student> students) throws IOException {
        Map<String, Double> stats = classStatistics(students);

        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8))) {

            pw.println("STUDENT PERFORMANCE REPORT");
            pw.println("Generated: " + java.time.LocalDate.now());
            pw.println("=".repeat(60));

            pw.println("\nCLASS STATISTICS");
            pw.printf("  Average:  %.2f%n", stats.getOrDefault("average", 0.0));
            pw.printf("  Highest:  %.2f%n", stats.getOrDefault("highest", 0.0));
            pw.printf("  Lowest:   %.2f%n", stats.getOrDefault("lowest", 0.0));
            pw.printf("  Median:   %.2f%n", stats.getOrDefault("median", 0.0));
            pw.printf("  Students: %d%n", students.size());

            pw.println("\nTOP 5 STUDENTS");
            pw.printf("  %-20s %-20s %8s %6s%n", "Name", "Major", "Average", "Grade");
            pw.println("  " + "-".repeat(56));
            topStudents(students, 5).forEach(s ->
                pw.printf("  %-20s %-20s %8.2f %6s%n",
                    s.name(), s.major(), s.average(), s.letterGrade()));

            List<Student> atRisk = atRiskStudents(students, 60.0);
            pw.println("\nSTUDENTS AT RISK (below 60%)");
            if (atRisk.isEmpty()) {
                pw.println("  None — all students are passing!");
            } else {
                atRisk.forEach(s ->
                    pw.printf("  %-20s %.2f (%s)%n", s.name(), s.average(), s.major()));
            }

            pw.println("\nAVERAGE BY MAJOR");
            averageByMajor(students).entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> pw.printf("  %-20s %.2f%n", e.getKey(), e.getValue()));
        }
    }

    public static void main(String[] args) throws IOException {
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
                INVALID,row
                S012,Last Student,Engineering,100,98,99,97,100
                """;

        Path inputFile = Files.createTempFile("students", ".csv");
        Path reportFile = Files.createTempFile("report", ".txt");

        try {
            Files.writeString(inputFile, csvContent, StandardCharsets.UTF_8);

            List<Student> students = parseCsv(inputFile);
            System.out.println("Parsed: " + students.size() + " students");

            writeReport(reportFile, students);
            Files.readAllLines(reportFile).forEach(System.out::println);
        } finally {
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(reportFile);
        }
    }
}
