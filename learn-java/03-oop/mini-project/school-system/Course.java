import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Course — represents an academic course in the school.
 *
 * OOP CONCEPTS:
 * - Composition: Course HAS a Teacher (not IS a Teacher)
 * - Encapsulation: student list is private, operations controlled
 * - Methods enforce business rules (capacity limits, enrollment)
 */
public class Course {

    private final String courseId;
    private String name;
    private String description;
    private Teacher teacher;
    private final List<Student> enrolledStudents;
    private final int maxCapacity;
    private int creditHours;

    public Course(String courseId, String name, Teacher teacher, int maxCapacity, int creditHours) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty");
        }
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be positive");
        }

        this.courseId = courseId.trim();
        this.name = name.trim();
        this.teacher = teacher;
        this.maxCapacity = maxCapacity;
        this.creditHours = creditHours;
        this.enrolledStudents = new ArrayList<>();

        // Tell the teacher about this course (bidirectional relationship)
        if (teacher != null) {
            teacher.assignCourse(this);
        }
    }

    // Convenience constructor with default capacity
    public Course(String courseId, String name, Teacher teacher) {
        this(courseId, name, teacher, 30, 3);
    }

    // =========================================================================
    // Enrollment management
    // =========================================================================

    public boolean enroll(Student student) {
        if (student == null) throw new IllegalArgumentException("Student cannot be null");

        if (isFull()) {
            System.out.println("Cannot enroll " + student.getName() +
                             " in " + name + ": course is full (" + maxCapacity + "/" + maxCapacity + ")");
            return false;
        }

        if (enrolledStudents.contains(student)) {
            System.out.println(student.getName() + " is already enrolled in " + name);
            return false;
        }

        enrolledStudents.add(student);
        System.out.println(student.getName() + " enrolled in " + name);
        return true;
    }

    public boolean withdraw(Student student) {
        if (enrolledStudents.remove(student)) {
            System.out.println(student.getName() + " withdrew from " + name);
            return true;
        }
        System.out.println(student.getName() + " is not enrolled in " + name);
        return false;
    }

    // =========================================================================
    // Grade management
    // =========================================================================

    public void recordGrade(Student student, double grade) {
        if (!enrolledStudents.contains(student)) {
            throw new IllegalArgumentException(student.getName() + " is not enrolled in " + name);
        }
        student.addGrade(grade);
        System.out.printf("Grade %.1f recorded for %s in %s%n",
                         grade, student.getName(), name);
    }

    // =========================================================================
    // Analytics
    // =========================================================================

    public double getClassAverage() {
        if (enrolledStudents.isEmpty()) return 0.0;
        double total = 0;
        int count = 0;
        for (Student student : enrolledStudents) {
            if (student.getGradeCount() > 0) {
                total += student.getAverageGrade();
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    public Student getTopStudent() {
        if (enrolledStudents.isEmpty()) return null;
        Student top = enrolledStudents.get(0);
        for (Student student : enrolledStudents) {
            if (student.getAverageGrade() > top.getAverageGrade()) {
                top = student;
            }
        }
        return top;
    }

    // =========================================================================
    // State queries
    // =========================================================================

    public boolean isFull() {
        return enrolledStudents.size() >= maxCapacity;
    }

    public boolean isEnrolled(Student student) {
        return enrolledStudents.contains(student);
    }

    public int getEnrollmentCount() { return enrolledStudents.size(); }
    public int getAvailableSeats() { return maxCapacity - enrolledStudents.size(); }

    // =========================================================================
    // Display
    // =========================================================================

    public void printRoster() {
        System.out.println("\nCourse: " + name + " (" + courseId + ")");
        System.out.println("Teacher: " + (teacher != null ? teacher.getName() : "TBD"));
        System.out.println("Enrolled: " + enrolledStudents.size() + "/" + maxCapacity);
        System.out.println("Class Average: " + String.format("%.1f", getClassAverage()));
        System.out.println("Students:");
        if (enrolledStudents.isEmpty()) {
            System.out.println("  (no students enrolled)");
        } else {
            for (Student student : enrolledStudents) {
                System.out.printf("  %-20s | GPA: %.2f | Avg: %.1f%n",
                                 student.getName(),
                                 student.calculateGPA(),
                                 student.getAverageGrade());
            }
        }
    }

    // Getters
    public String getCourseId() { return courseId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Teacher getTeacher() { return teacher; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getCreditHours() { return creditHours; }

    public List<Student> getEnrolledStudents() {
        return Collections.unmodifiableList(enrolledStudents);
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
        if (teacher != null) teacher.assignCourse(this);
    }

    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Course)) return false;
        Course other = (Course) obj;
        return Objects.equals(courseId, other.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId);
    }

    @Override
    public String toString() {
        return String.format("Course{%s: %s, teacher=%s, enrolled=%d/%d}",
                            courseId, name,
                            teacher != null ? teacher.getName() : "TBD",
                            enrolledStudents.size(), maxCapacity);
    }
}
