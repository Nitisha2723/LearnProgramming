import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Teacher — extends Person, represents a faculty member.
 *
 * OOP CONCEPTS:
 * - Inheritance from Person
 * - Has its own additional state (employeeId, salary, subjects)
 * - Demonstrates "is-a" relationship: a Teacher IS A Person
 */
public class Teacher extends Person {

    private final String employeeId;
    private double salary;
    private String department;
    private final List<String> qualifications;
    private final List<Course> taughtCourses;  // courses this teacher teaches

    public Teacher(String personId, String name, int age, String email,
                   String employeeId, String department, double salary) {
        super(personId, name, age, email);
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be empty");
        }
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.employeeId = employeeId.trim();
        this.department = department;
        this.salary = salary;
        this.qualifications = new ArrayList<>();
        this.taughtCourses = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "Teacher";
    }

    // =========================================================================
    // Teacher-specific methods
    // =========================================================================

    public void addQualification(String qualification) {
        if (qualification != null && !qualification.trim().isEmpty()) {
            qualifications.add(qualification.trim());
        }
    }

    public void assignCourse(Course course) {
        if (!taughtCourses.contains(course)) {
            taughtCourses.add(course);
        }
    }

    public void removeCourse(Course course) {
        taughtCourses.remove(course);
    }

    public void giveRaise(double percentage) {
        if (percentage <= 0) {
            throw new IllegalArgumentException("Raise percentage must be positive");
        }
        double raise = salary * (percentage / 100.0);
        salary += raise;
        System.out.printf("%s received a %.1f%% raise. New salary: $%.2f%n",
                         getName(), percentage, salary);
    }

    // Getters
    public String getEmployeeId() { return employeeId; }
    public String getDepartment() { return department; }
    public double getSalary() { return salary; }
    public int getCourseCount() { return taughtCourses.size(); }

    public List<String> getQualifications() {
        return Collections.unmodifiableList(qualifications);
    }

    public List<Course> getTaughtCourses() {
        return Collections.unmodifiableList(taughtCourses);
    }

    public void setDepartment(String department) { this.department = department; }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.printf("  Employee ID: %s | Dept: %s | Salary: $%.2f%n",
                         employeeId, department, salary);
        System.out.printf("  Courses taught: %d | Qualifications: %d%n",
                         taughtCourses.size(), qualifications.size());
        if (!qualifications.isEmpty()) {
            System.out.println("  Qualifications: " + qualifications);
        }
    }

    @Override
    public String toString() {
        return String.format("Teacher{%s, id=%s, dept=%s}",
                            getName(), employeeId, department);
    }
}
