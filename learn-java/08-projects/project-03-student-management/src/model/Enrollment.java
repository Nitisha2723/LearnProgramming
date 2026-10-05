package model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Records that a specific student is enrolled in a specific course.
 */
public class Enrollment {

    // -------------------------------------------------------------------------
    // Inner enum
    // -------------------------------------------------------------------------

    public enum EnrollmentStatus {
        ACTIVE,
        DROPPED,
        COMPLETED
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final String enrollmentId;
    private final String studentId;
    private final String courseId;
    private final LocalDate enrolledDate;
    private EnrollmentStatus status;

    public Enrollment(String studentId, String courseId) {
        this.enrollmentId = UUID.randomUUID().toString();
        this.studentId    = studentId;
        this.courseId     = courseId;
        this.enrolledDate = LocalDate.now();
        this.status       = EnrollmentStatus.ACTIVE;
    }

    /** Reconstruction constructor. */
    public Enrollment(String enrollmentId, String studentId, String courseId,
                      LocalDate enrolledDate, EnrollmentStatus status) {
        this.enrollmentId = enrollmentId;
        this.studentId    = studentId;
        this.courseId     = courseId;
        this.enrolledDate = enrolledDate;
        this.status       = status;
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Marks this enrollment as DROPPED.
     *
     * @throws IllegalStateException if already dropped or completed
     */
    public void drop() {
        if (status != EnrollmentStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot drop enrollment with status: " + status);
        }
        this.status = EnrollmentStatus.DROPPED;
    }

    /**
     * Marks this enrollment as COMPLETED.
     *
     * @throws IllegalStateException if not currently ACTIVE
     */
    public void complete() {
        if (status != EnrollmentStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot complete enrollment with status: " + status);
        }
        this.status = EnrollmentStatus.COMPLETED;
    }

    // -------------------------------------------------------------------------
    // Getters (no setters — most fields are immutable after creation)
    // -------------------------------------------------------------------------

    public String getEnrollmentId()         { return enrollmentId; }
    public String getStudentId()            { return studentId; }
    public String getCourseId()             { return courseId; }
    public LocalDate getEnrolledDate()      { return enrolledDate; }
    public EnrollmentStatus getStatus()     { return status; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment)) return false;
        Enrollment e = (Enrollment) o;
        return Objects.equals(enrollmentId, e.enrollmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId);
    }

    @Override
    public String toString() {
        return "Enrollment{id='" + enrollmentId + "', student='" + studentId
                + "', course='" + courseId + "', status=" + status + "}";
    }
}
