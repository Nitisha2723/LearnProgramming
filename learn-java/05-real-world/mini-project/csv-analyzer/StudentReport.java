import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;

/**
 * StudentReport.java — Report generation for the CSV Analyzer mini-project.
 *
 * Responsibility: Take pre-computed analysis results and format them into
 * a readable plain-text report. No data processing happens here — only formatting.
 *
 * Demonstrates:
 *   - Separation of concerns (analysis vs. presentation)
 *   - try-with-resources for file writing
 *   - Printf-style table formatting
 *   - Stream pipelines for sorted output
 *   - Optional usage (Map.getOrDefault)
 */
public class StudentReport {

    // =========================================================
    // Dependencies (injected at construction)
    // =========================================================

    private final List<CsvAnalyzer.Student>                students;
    private final CsvAnalyzer.ClassStats                   classStats;
    private final List<CsvAnalyzer.Student>                topStudents;
    private final List<CsvAnalyzer.Student>                atRiskStudents;
    private final Map<String, CsvAnalyzer.MajorStats>      statsByMajor;
    private final List<String>                             parseWarnings;

    // =========================================================
    // Constants
    // =========================================================

    private static final int   LINE_WIDTH = 65;
    private static final char  SEPARATOR  = '=';
    private static final char  LIGHT_SEP  = '-';

    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm";

    // =========================================================
    // Constructor
    // =========================================================

    public StudentReport(
        List<CsvAnalyzer.Student>           students,
        CsvAnalyzer.ClassStats              classStats,
        List<CsvAnalyzer.Student>           topStudents,
        List<CsvAnalyzer.Student>           atRiskStudents,
        Map<String, CsvAnalyzer.MajorStats> statsByMajor,
        List<String>                        parseWarnings
    ) {
        this.students       = Objects.requireNonNull(students);
        this.classStats     = Objects.requireNonNull(classStats);
        this.topStudents    = Objects.requireNonNull(topStudents);
        this.atRiskStudents = Objects.requireNonNull(atRiskStudents);
        this.statsByMajor   = Objects.requireNonNull(statsByMajor);
        this.parseWarnings  = Objects.requireNonNull(parseWarnings);
    }

    // =========================================================
    // Public API
    // =========================================================

    /**
     * Writes the formatted report to the given file.
     * Creates parent directories if they don't exist.
     *
     * @param outputPath destination file path (will be created or overwritten)
     * @throws IOException on any I/O error
     */
    public void writeTo(Path outputPath) throws IOException {
        // Ensure parent directory exists
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8))) {

            writeTitle(pw);
            writeSectionBreak(pw, SEPARATOR);

            writeClassStats(pw);
            writeSectionBreak(pw, SEPARATOR);

            writeGradeDistribution(pw);
            writeSectionBreak(pw, SEPARATOR);

            writeTopStudents(pw);
            writeSectionBreak(pw, SEPARATOR);

            writeAtRiskStudents(pw);
            writeSectionBreak(pw, SEPARATOR);

            writeMajorBreakdown(pw);
            writeSectionBreak(pw, SEPARATOR);

            writeFullRoster(pw);
            writeSectionBreak(pw, SEPARATOR);

            if (!parseWarnings.isEmpty()) {
                writeParseWarnings(pw);
                writeSectionBreak(pw, SEPARATOR);
            }

            pw.println("END OF REPORT");
        }
    }

    // =========================================================
    // Section writers
    // =========================================================

    private void writeTitle(PrintWriter pw) {
        pw.println();
        pw.println(center("STUDENT PERFORMANCE ANALYSIS REPORT", LINE_WIDTH));
        pw.println(center("Module 05 Real-World Java — CSV Analyzer", LINE_WIDTH));
        pw.println();
        pw.printf("Generated : %s%n",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN)));
        pw.printf("Students  : %d%n", classStats.totalStudents());
        pw.printf("Warnings  : %d parse warnings%n", parseWarnings.size());
        pw.println();
    }

    private void writeClassStats(PrintWriter pw) {
        pw.println(sectionHeader("CLASS STATISTICS"));
        pw.println();
        pw.printf("  %-18s %7.2f%n", "Class Average:",  classStats.average());
        pw.printf("  %-18s %7.2f%n", "Median Average:", classStats.median());
        pw.printf("  %-18s %7.2f%n", "Highest Average:",classStats.highest());
        pw.printf("  %-18s %7.2f%n", "Lowest Average:", classStats.lowest());
        pw.println();
    }

    private void writeGradeDistribution(PrintWriter pw) {
        pw.println(sectionHeader("GRADE DISTRIBUTION"));
        pw.println();

        // Ensure all letter grades appear, even with 0 count
        List<String> gradeOrder = List.of("A", "B", "C", "D", "F");
        Map<String, Long> dist = classStats.gradeDistribution();
        int total = classStats.totalStudents();

        pw.printf("  %-6s %-8s %s%n", "Grade", "Count", "Percentage / Visual");
        pw.println("  " + repeat(LIGHT_SEP, 50));

        for (String grade : gradeOrder) {
            long count = dist.getOrDefault(grade, 0L);
            double pct = total > 0 ? (count * 100.0 / total) : 0;
            int bars = (int) (pct / 5);   // each bar = 5%
            String bar = repeat('|', bars);

            pw.printf("  %-6s %-8d %5.1f%%  %s%n", grade, count, pct, bar);
        }
        pw.println();
    }

    private void writeTopStudents(PrintWriter pw) {
        int n = topStudents.size();
        pw.println(sectionHeader("TOP " + n + " STUDENTS"));
        pw.println();

        if (topStudents.isEmpty()) {
            pw.println("  (no students)");
            pw.println();
            return;
        }

        pw.printf("  %-4s %-22s %-22s %7s %6s%n",
            "Rank", "Name", "Major", "Average", "Grade");
        pw.println("  " + repeat(LIGHT_SEP, 60));

        for (int i = 0; i < topStudents.size(); i++) {
            CsvAnalyzer.Student s = topStudents.get(i);
            pw.printf("  #%-3d %-22s %-22s %7.2f %6s%n",
                i + 1, s.name(), s.major(), s.average(), s.letterGrade());
        }
        pw.println();
    }

    private void writeAtRiskStudents(PrintWriter pw) {
        pw.println(sectionHeader("STUDENTS AT RISK  (average below 60)"));
        pw.println();

        if (atRiskStudents.isEmpty()) {
            pw.println("  Great news — all students are currently passing!");
            pw.println();
            return;
        }

        pw.printf("  %-22s %-22s %7s%n", "Name", "Major", "Average");
        pw.println("  " + repeat(LIGHT_SEP, 55));

        atRiskStudents.forEach(s ->
            pw.printf("  %-22s %-22s %7.2f  ** ALERT **%n",
                s.name(), s.major(), s.average()));
        pw.println();
        pw.printf("  Total at-risk students: %d / %d (%.1f%%)%n",
            atRiskStudents.size(),
            classStats.totalStudents(),
            atRiskStudents.size() * 100.0 / Math.max(classStats.totalStudents(), 1));
        pw.println();
    }

    private void writeMajorBreakdown(PrintWriter pw) {
        pw.println(sectionHeader("PERFORMANCE BY MAJOR"));
        pw.println();

        if (statsByMajor.isEmpty()) {
            pw.println("  (no data)");
            pw.println();
            return;
        }

        pw.printf("  %-24s %8s %10s%n", "Major", "Students", "Avg Grade");
        pw.println("  " + repeat(LIGHT_SEP, 45));

        // Sort by average descending
        statsByMajor.entrySet().stream()
            .sorted(Comparator.comparingDouble((Map.Entry<String, CsvAnalyzer.MajorStats> e)
                    -> e.getValue().averageGrade()).reversed())
            .forEach(e -> pw.printf("  %-24s %8d %10.2f%n",
                e.getKey(), e.getValue().count(), e.getValue().averageGrade()));
        pw.println();
    }

    private void writeFullRoster(PrintWriter pw) {
        pw.println(sectionHeader("FULL ROSTER"));
        pw.println();

        pw.printf("  %-8s %-22s %-22s %7s %5s%n",
            "ID", "Name", "Major", "Average", "Grade");
        pw.println("  " + repeat(LIGHT_SEP, 68));

        students.stream()
            .sorted(Comparator.comparing(CsvAnalyzer.Student::major)
                .thenComparingDouble(CsvAnalyzer.Student::average).reversed())
            .forEach(s -> pw.printf("  %-8s %-22s %-22s %7.2f %5s%n",
                s.id(), s.name(), s.major(), s.average(), s.letterGrade()));
        pw.println();
    }

    private void writeParseWarnings(PrintWriter pw) {
        pw.println(sectionHeader("PARSE WARNINGS (" + parseWarnings.size() + ")"));
        pw.println();
        parseWarnings.forEach(w -> pw.println("  " + w));
        pw.println();
    }

    // =========================================================
    // Formatting utilities
    // =========================================================

    private String sectionHeader(String title) {
        return " " + title;
    }

    private void writeSectionBreak(PrintWriter pw, char ch) {
        pw.println(repeat(ch, LINE_WIDTH));
    }

    private static String center(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }

    private static String repeat(char ch, int times) {
        char[] chars = new char[times];
        Arrays.fill(chars, ch);
        return new String(chars);
    }
}
