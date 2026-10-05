package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a student in the management system.
 * Identified uniquely by a UUID-based studentId.
 */
public class Student {

    private final String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private String major;
    private final LocalDateTime enrolledAt;

    /**
     * Creates a new Student with an auto-generated UUID.
     */
    public Student(String firstName, String lastName, String email,
                   LocalDate dateOfBirth, String major) {
        this.studentId  = UUID.randomUUID().toString();
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.email      = email;
        this.dateOfBirth = dateOfBirth;
        this.major      = major;
        this.enrolledAt = LocalDateTime.now();
    }

    /**
     * Reconstruction constructor — used when loading from persistence.
     */
    public Student(String studentId, String firstName, String lastName, String email,
                   LocalDate dateOfBirth, String major, LocalDateTime enrolledAt) {
        this.studentId  = studentId;
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.email      = email;
        this.dateOfBirth = dateOfBirth;
        this.major      = major;
        this.enrolledAt = enrolledAt;
    }

    // -------------------------------------------------------------------------
    // Derived / business methods
    // -------------------------------------------------------------------------

    /** Returns "FirstName LastName". */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** Returns the student's age in full years based on today's date. */
    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getStudentId()            { return studentId; }
    public String getFirstName()            { return firstName; }
    public String getLastName()             { return lastName; }
    public String getEmail()                { return email; }
    public LocalDate getDateOfBirth()       { return dateOfBirth; }
    public String getMajor()                { return major; }
    public LocalDateTime getEnrolledAt()    { return enrolledAt; }

    public void setFirstName(String firstName)      { this.firstName = firstName; }
    public void setLastName(String lastName)        { this.lastName  = lastName; }
    public void setEmail(String email)              { this.email     = email; }
    public void setDateOfBirth(LocalDate dob)       { this.dateOfBirth = dob; }
    public void setMajor(String major)              { this.major     = major; }

    // -------------------------------------------------------------------------
    // equals / hashCode — identity is the studentId
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student s = (Student) o;
        return Objects.equals(studentId, s.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }

    @Override
    public String toString() {
        return "Student{id='" + studentId + "', name='" + getFullName()
                + "', major='" + major + "', email='" + email + "'}";
    }
}
