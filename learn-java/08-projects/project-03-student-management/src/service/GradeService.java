package service;

import model.Course;
import model.Enrollment;
import model.Enrollment.EnrollmentStatus;
import model.Grade;
import model.Student;
import repository.CourseRepository;
import repository.EnrollmentRepository;
import repository.GradeRepository;
import repository.StudentRepository;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Business logic for grade assignment, GPA calculation, and reporting.
 */
public class GradeService {

    private final GradeRepository      gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository     courseRepository;
    private final StudentRepository    studentRepository;

    public GradeService(GradeRepository gradeRepository,
                        EnrollmentRepository enrollmentRepository,
                        CourseRepository courseRepository,
                        StudentRepository studentRepository) {
        this.gradeRepository      = gradeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository     = courseRepository;
        this.studentRepository    = studentRepository;
    }

    // -------------------------------------------------------------------------
    // Grade CRUD
    // -------------------------------------------------------------------------

    /**
     * Assigns a grade to a student for a course/enrollment.
     *
     * @param score 0–100 inclusive
     * @throws IllegalStateException if the enrollment is not ACTIVE
     * @throws IllegalArgumentException if a grade already exists for this enrollment
     */
    public Grade assignGrade(String studentId, String courseId,
                             String enrollmentId, double score) {
        // Validate enrollment exists and is active
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Enrollment not found: " + enrollmentId));
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot grade a non-active enrollment (status=" + enrollment.getStatus() + ")");
        }

        // Prevent duplicate grades for same enrollment
        if (gradeRepository.findByEnrollmentId(enrollmentId).isPresent()) {
            throw new IllegalArgumentException(
                    "A grade already exists for enrollment: " + enrollmentId);
        }

        Grade grade = new Grade(studentId, courseId, enrollmentId, score);
        return gradeRepository.save(grade);
    }

    /**
     * Updates the score on an existing grade (re-grading).
     *
     * @param gradeId the UUID of the grade to update
     * @param newScore new score in 0–100
     */
    public Grade updateGrade(String gradeId, double newScore) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Grade not found: " + gradeId));
        grade.updateScore(newScore);
        return gradeRepository.update(grade);
    }

    public List<Grade> getStudentGrades(String studentId) {
        return gradeRepository.findByStudentId(studentId);
    }

    // -------------------------------------------------------------------------
    // GPA Calculation (weighted by credit hours)
    // -------------------------------------------------------------------------

    /**
     * Calculates a student's GPA, weighted by credit hours.
     *
     * <p>Formula: GPA = Σ(gradePoints × credits) / Σcredits</p>
     *
     * @return GPA in [0.0, 4.0], or 0.0 if no graded courses
     */
    public double calculateGpa(String studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        if (grades.isEmpty()) return 0.0;

        double totalWeightedPoints = 0.0;
        int    totalCredits        = 0;

        for (Grade grade : grades) {
            courseRepository.findById(grade.getCourseId()).ifPresent(course -> {
                // nothing — we just need the course reference below
            });
            // Fetch credits for each course
            Course course = courseRepository.findById(grade.getCourseId()).orElse(null);
            if (course != null) {
                int credits = course.getCredits();
                totalWeightedPoints += grade.getGradePoints() * credits;
                totalCredits        += credits;
            }
        }

        return totalCredits == 0 ? 0.0 : totalWeightedPoints / totalCredits;
    }

    // -------------------------------------------------------------------------
    // Transcript
    // -------------------------------------------------------------------------

    /**
     * Generates a formatted transcript string for a student.
     *
     * @param studentId the student's UUID
     * @return multi-line transcript
     */
    public String generateTranscript(String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Student not found: " + studentId));

        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        double gpa = calculateGpa(studentId);

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("OFFICIAL TRANSCRIPT\n");
        sb.append("========================================\n");
        sb.append(String.format("Student: %-30s ID: %s%n",
                student.getFullName(), student.getStudentId()));
        sb.append(String.format("Major:   %-30s Age: %d%n",
                student.getMajor(), student.getAge()));
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-8s %-30s %3s  %6s  %s%n",
                "Code", "Course Name", "Cr", "Score", "Grade"));
        sb.append("----------------------------------------\n");

        for (Grade grade : grades) {
            Course course = courseRepository.findById(grade.getCourseId()).orElse(null);
            String code   = course != null ? course.getCourseCode() : "N/A";
            String name   = course != null ? course.getCourseName() : "Unknown";
            int    credits = course != null ? course.getCredits() : 0;
            sb.append(String.format("%-8s %-30s %3d  %6.1f  %s%n",
                    code, name, credits, grade.getScore(), grade.getLetterGrade()));
        }

        sb.append("========================================\n");
        sb.append(String.format("Cumulative GPA: %.2f%n", gpa));
        sb.append("========================================\n");

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Class Report
    // -------------------------------------------------------------------------

    /**
     * Generates summary statistics for all grades in a course.
     *
     * @param courseId the course UUID
     * @return a formatted multi-line report string
     */
    public String getClassReport(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Course not found: " + courseId));

        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        if (grades.isEmpty()) {
            return "No grades recorded for course: " + course.getCourseCode();
        }

        DoubleSummaryStatistics stats = grades.stream()
                .mapToDouble(Grade::getScore)
                .summaryStatistics();

        // Grade distribution
        Map<String, Long> distribution = grades.stream()
                .collect(Collectors.groupingBy(Grade::getLetterGrade, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("=== Class Report: ")
          .append(course.getCourseCode()).append(" - ").append(course.getCourseName())
          .append(" ===\n");
        sb.append(String.format("Students graded : %d%n", grades.size()));
        sb.append(String.format("Average score   : %.2f%n", stats.getAverage()));
        sb.append(String.format("Highest score   : %.2f%n", stats.getMax()));
        sb.append(String.format("Lowest score    : %.2f%n", stats.getMin()));
        sb.append("Grade distribution:\n");
        for (String letter : new String[]{"A", "B", "C", "D", "F"}) {
            long count = distribution.getOrDefault(letter, 0L);
            sb.append(String.format("  %s: %d%n", letter, count));
        }

        return sb.toString();
    }
}
