package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an academic course with a fixed capacity.
 */
public class Course {

    private final String courseId;
    private String courseCode;      // e.g. "CS101"
    private String courseName;
    private String teacherId;
    private int maxCapacity;
    private int credits;
    private final List<String> enrolledStudentIds;

    public Course(String courseCode, String courseName, String teacherId,
                  int maxCapacity, int credits) {
        this.courseId           = UUID.randomUUID().toString();
        this.courseCode         = courseCode;
        this.courseName         = courseName;
        this.teacherId          = teacherId;
        this.maxCapacity        = maxCapacity;
        this.credits            = credits;
        this.enrolledStudentIds = new ArrayList<>();
    }

    /** Reconstruction constructor. */
    public Course(String courseId, String courseCode, String courseName, String teacherId,
                  int maxCapacity, int credits, List<String> enrolledStudentIds) {
        this.courseId           = courseId;
        this.courseCode         = courseCode;
        this.courseName         = courseName;
        this.teacherId          = teacherId;
        this.maxCapacity        = maxCapacity;
        this.credits            = credits;
        this.enrolledStudentIds = new ArrayList<>(enrolledStudentIds);
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Adds a student to this course.
     *
     * @param studentId the student's UUID
     * @throws IllegalStateException  if the course is already full
     * @throws IllegalArgumentException if the student is already enrolled
     */
    public void addStudent(String studentId) {
        if (isFull()) {
            throw new IllegalStateException(
                    "Course " + courseCode + " is full (capacity=" + maxCapacity + ")");
        }
        if (enrolledStudentIds.contains(studentId)) {
            throw new IllegalArgumentException(
                    "Student " + studentId + " is already enrolled in " + courseCode);
        }
        enrolledStudentIds.add(studentId);
    }

    /**
     * Removes a student from this course.
     *
     * @param studentId the student's UUID
     */
    public void removeStudent(String studentId) {
        enrolledStudentIds.remove(studentId);
    }

    /** Returns {@code true} if the course has reached maximum capacity. */
    public boolean isFull() {
        return enrolledStudentIds.size() >= maxCapacity;
    }

    /** Returns the number of currently enrolled students. */
    public int getEnrollmentCount() {
        return enrolledStudentIds.size();
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getCourseId()                     { return courseId; }
    public String getCourseCode()                   { return courseCode; }
    public String getCourseName()                   { return courseName; }
    public String getTeacherId()                    { return teacherId; }
    public int getMaxCapacity()                     { return maxCapacity; }
    public int getCredits()                         { return credits; }

    public List<String> getEnrolledStudentIds() {
        return Collections.unmodifiableList(enrolledStudentIds);
    }

    public void setCourseCode(String courseCode)    { this.courseCode  = courseCode; }
    public void setCourseName(String courseName)    { this.courseName  = courseName; }
    public void setTeacherId(String teacherId)      { this.teacherId   = teacherId; }
    public void setMaxCapacity(int maxCapacity)     { this.maxCapacity = maxCapacity; }
    public void setCredits(int credits)             { this.credits     = credits; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course c = (Course) o;
        return Objects.equals(courseId, c.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId);
    }

    @Override
    public String toString() {
        return "Course{id='" + courseId + "', code='" + courseCode
                + "', name='" + courseName + "', credits=" + credits
                + ", enrolled=" + getEnrollmentCount() + "/" + maxCapacity + "}";
    }
}
