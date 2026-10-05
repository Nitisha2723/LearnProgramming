package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Records the numeric score a student received for a specific course/enrollment.
 * Score must be in the range 0–100 (inclusive).
 */
public class Grade {

    private final String gradeId;
    private final String studentId;
    private final String courseId;
    private final String enrollmentId;
    private double score;               // 0 – 100
    private LocalDateTime gradedAt;

    public Grade(String studentId, String courseId, String enrollmentId, double score) {
        validateScore(score);
        this.gradeId      = UUID.randomUUID().toString();
        this.studentId    = studentId;
        this.courseId     = courseId;
        this.enrollmentId = enrollmentId;
        this.score        = score;
        this.gradedAt     = LocalDateTime.now();
    }

    /** Reconstruction constructor. */
    public Grade(String gradeId, String studentId, String courseId, String enrollmentId,
                 double score, LocalDateTime gradedAt) {
        validateScore(score);
        this.gradeId      = gradeId;
        this.studentId    = studentId;
        this.courseId     = courseId;
        this.enrollmentId = enrollmentId;
        this.score        = score;
        this.gradedAt     = gradedAt;
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Updates the score (re-grading scenario).
     *
     * @param newScore value in 0–100
     */
    public void updateScore(double newScore) {
        validateScore(newScore);
        this.score    = newScore;
        this.gradedAt = LocalDateTime.now();
    }

    /**
     * Converts the numeric score to a letter grade.
     *
     * <ul>
     *   <li>90–100 → A</li>
     *   <li>80–89  → B</li>
     *   <li>70–79  → C</li>
     *   <li>60–69  → D</li>
     *   <li>0–59   → F</li>
     * </ul>
     */
    public String getLetterGrade() {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    /**
     * Returns the GPA grade points for this score on a 4.0 scale:
     * A=4.0, B=3.0, C=2.0, D=1.0, F=0.0
     */
    public double getGradePoints() {
        switch (getLetterGrade()) {
            case "A": return 4.0;
            case "B": return 3.0;
            case "C": return 2.0;
            case "D": return 1.0;
            default:  return 0.0;
        }
    }

    // -------------------------------------------------------------------------
    // Validation helper
    // -------------------------------------------------------------------------

    private static void validateScore(double score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException(
                    "Score must be between 0 and 100, got: " + score);
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getGradeId()          { return gradeId; }
    public String getStudentId()        { return studentId; }
    public String getCourseId()         { return courseId; }
    public String getEnrollmentId()     { return enrollmentId; }
    public double getScore()            { return score; }
    public LocalDateTime getGradedAt()  { return gradedAt; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Grade)) return false;
        Grade g = (Grade) o;
        return Objects.equals(gradeId, g.gradeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gradeId);
    }

    @Override
    public String toString() {
        return "Grade{id='" + gradeId + "', student='" + studentId
                + "', course='" + courseId + "', score=" + score
                + ", letter=" + getLetterGrade() + "}";
    }
}
