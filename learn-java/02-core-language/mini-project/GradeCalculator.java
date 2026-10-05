/**
 * GradeCalculator.java
 *
 * Mini-Project: Grade Calculator
 *
 * A complete working solution for the Grade Calculator mini-project.
 * Processes student test scores and generates a formatted class report.
 *
 * Concepts demonstrated:
 * - 2D arrays
 * - String formatting (printf)
 * - Methods with clear single responsibilities
 * - Loops: nested for, for-each
 * - if/else chain for grade classification
 * - Array operations: sum, average, min, max
 *
 * How to run:
 *   javac GradeCalculator.java
 *   java GradeCalculator
 */
public class GradeCalculator {

    // =========================================================================
    // DATA
    // In a real program you'd read this from a file or database.
    // =========================================================================

    static String[] studentNames = {
        "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank"
    };

    // Each row is one student; each column is one test score
    static int[][] scores = {
        {92, 88, 95, 90, 85},  // Alice
        {75, 82, 68, 79, 71},  // Bob
        {55, 62, 70, 48, 60},  // Charlie
        {98, 95, 100, 97, 99}, // Diana
        {83, 87, 81, 90, 85},  // Eve
        {65, 72, 58, 63, 70}   // Frank
    };

    // =========================================================================
    // MAIN
    // =========================================================================

    public static void main(String[] args) {
        // Compute averages for all students
        double[] averages = computeAllAverages(scores);

        // Compute letter grades for all students
        char[] grades = computeAllGrades(averages);

        // Print the full report
        printHeader();
        printStudentTable(averages, grades);
        printStatistics(averages, grades);

        System.out.println("=".repeat(60));
    }

    // =========================================================================
    // COMPUTE METHODS
    // =========================================================================

    /**
     * Computes the average score for a single student.
     *
     * @param studentScores array of test scores for one student
     * @return the average score as a double
     */
    static double computeAverage(int[] studentScores) {
        int sum = 0;
        for (int score : studentScores) {
            sum += score;
        }
        // Cast to double BEFORE dividing to avoid integer division
        return (double) sum / studentScores.length;
    }

    /**
     * Computes averages for all students.
     *
     * @param allScores 2D array: rows = students, columns = test scores
     * @return array of averages, one per student
     */
    static double[] computeAllAverages(int[][] allScores) {
        double[] averages = new double[allScores.length];
        for (int i = 0; i < allScores.length; i++) {
            averages[i] = computeAverage(allScores[i]);
        }
        return averages;
    }

    /**
     * Converts a numeric average to a letter grade.
     *
     * @param average the student's average score
     * @return letter grade character: 'A', 'B', 'C', 'D', or 'F'
     */
    static char getLetterGrade(double average) {
        if (average >= 90) return 'A';
        if (average >= 80) return 'B';
        if (average >= 70) return 'C';
        if (average >= 60) return 'D';
        return 'F';
    }

    /**
     * Converts all averages to letter grades.
     *
     * @param averages array of numeric averages
     * @return array of letter grade characters
     */
    static char[] computeAllGrades(double[] averages) {
        char[] grades = new char[averages.length];
        for (int i = 0; i < averages.length; i++) {
            grades[i] = getLetterGrade(averages[i]);
        }
        return grades;
    }

    // =========================================================================
    // STATISTICS METHODS
    // =========================================================================

    /**
     * Returns the index of the student with the highest average.
     */
    static int findTopStudentIndex(double[] averages) {
        int topIndex = 0;
        for (int i = 1; i < averages.length; i++) {
            if (averages[i] > averages[topIndex]) {
                topIndex = i;
            }
        }
        return topIndex;
    }

    /**
     * Returns the index of the student with the lowest average.
     */
    static int findBottomStudentIndex(double[] averages) {
        int bottomIndex = 0;
        for (int i = 1; i < averages.length; i++) {
            if (averages[i] < averages[bottomIndex]) {
                bottomIndex = i;
            }
        }
        return bottomIndex;
    }

    /**
     * Computes the class-wide average of all student averages.
     */
    static double computeClassAverage(double[] averages) {
        double sum = 0;
        for (double avg : averages) {
            sum += avg;
        }
        return sum / averages.length;
    }

    /**
     * Returns all names of students with a given grade, separated by ", ".
     */
    static String getNamesForGrade(char targetGrade, char[] grades) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < grades.length; i++) {
            if (grades[i] == targetGrade) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(studentNames[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Counts students with a given letter grade.
     */
    static int countGrade(char targetGrade, char[] grades) {
        int count = 0;
        for (char grade : grades) {
            if (grade == targetGrade) count++;
        }
        return count;
    }

    // =========================================================================
    // PRINT METHODS
    // =========================================================================

    /**
     * Prints the report header.
     */
    static void printHeader() {
        System.out.println("=".repeat(60));
        System.out.printf("%33s%n", "CLASS GRADE REPORT");
        System.out.println("=".repeat(60));
    }

    /**
     * Prints the table of students, scores, averages, and grades.
     */
    static void printStudentTable(double[] averages, char[] grades) {
        // Column headers
        System.out.printf("%-15s  %4s  %4s  %4s  %4s  %4s  %7s  %5s%n",
                "Student", "T1", "T2", "T3", "T4", "T5", "Average", "Grade");
        System.out.println("-".repeat(60));

        // One row per student
        for (int i = 0; i < studentNames.length; i++) {
            System.out.printf("%-15s", studentNames[i]);

            // Print each test score
            for (int j = 0; j < scores[i].length; j++) {
                System.out.printf("  %4d", scores[i][j]);
            }

            // Print average and letter grade
            System.out.printf("  %7.2f     %c%n", averages[i], grades[i]);
        }

        System.out.println("=".repeat(60));
    }

    /**
     * Prints class statistics and grade distribution.
     */
    static void printStatistics(double[] averages, char[] grades) {
        System.out.println();
        System.out.println("--- Class Statistics ---");

        int topIdx    = findTopStudentIndex(averages);
        int bottomIdx = findBottomStudentIndex(averages);
        double classAvg = computeClassAverage(averages);

        System.out.printf("Highest Average: %-14s (%.2f)%n",
                studentNames[topIdx], averages[topIdx]);
        System.out.printf("Lowest Average:  %-14s (%.2f)%n",
                studentNames[bottomIdx], averages[bottomIdx]);
        System.out.printf("Class Average:   %.2f%n%n", classAvg);

        // Grade distribution
        System.out.println("--- Grade Distribution ---");
        char[] gradeScale = {'A', 'B', 'C', 'D', 'F'};
        for (char g : gradeScale) {
            int count = countGrade(g, grades);
            String names = getNamesForGrade(g, grades);
            if (count > 0) {
                System.out.printf("%c: %d  (%s)%n", g, count, names);
            } else {
                System.out.printf("%c: 0%n", g);
            }
        }
        System.out.println();

        // Pass/fail summary (D or above = pass)
        int passed = 0;
        for (char grade : grades) {
            if (grade != 'F') passed++;
        }
        int failed = grades.length - passed;
        double passRate = (double) passed / grades.length * 100;
        double failRate = (double) failed / grades.length * 100;

        System.out.println("--- Pass/Fail Summary ---");
        System.out.printf("Passed (D or above): %d (%.1f%%)%n", passed, passRate);
        System.out.printf("Failed:              %d (%.1f%%)%n", failed, failRate);
        System.out.println();
    }
}
