package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a teacher who can be assigned to one or more courses.
 */
public class Teacher {

    private final String teacherId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private final List<String> courseIds;

    public Teacher(String firstName, String lastName, String email, String department) {
        this.teacherId  = UUID.randomUUID().toString();
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.email      = email;
        this.department = department;
        this.courseIds  = new ArrayList<>();
    }

    /** Reconstruction constructor (loading from persistence). */
    public Teacher(String teacherId, String firstName, String lastName, String email,
                   String department, List<String> courseIds) {
        this.teacherId  = teacherId;
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.email      = email;
        this.department = department;
        this.courseIds  = new ArrayList<>(courseIds);
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /** Returns "FirstName LastName". */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Assigns a course to this teacher.
     *
     * @param courseId the UUID of the course to assign
     */
    public void assignCourse(String courseId) {
        if (courseId != null && !courseIds.contains(courseId)) {
            courseIds.add(courseId);
        }
    }

    /**
     * Removes a course assignment from this teacher.
     *
     * @param courseId the UUID of the course to remove
     */
    public void removeCourse(String courseId) {
        courseIds.remove(courseId);
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getTeacherId()                    { return teacherId; }
    public String getFirstName()                    { return firstName; }
    public String getLastName()                     { return lastName; }
    public String getEmail()                        { return email; }
    public String getDepartment()                   { return department; }

    /** Returns an unmodifiable view of the course ID list. */
    public List<String> getCourseIds() {
        return Collections.unmodifiableList(courseIds);
    }

    public void setFirstName(String firstName)      { this.firstName  = firstName; }
    public void setLastName(String lastName)        { this.lastName   = lastName; }
    public void setEmail(String email)              { this.email      = email; }
    public void setDepartment(String department)    { this.department = department; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Teacher)) return false;
        Teacher t = (Teacher) o;
        return Objects.equals(teacherId, t.teacherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId);
    }

    @Override
    public String toString() {
        return "Teacher{id='" + teacherId + "', name='" + getFullName()
                + "', dept='" + department + "', courses=" + courseIds.size() + "}";
    }
}
