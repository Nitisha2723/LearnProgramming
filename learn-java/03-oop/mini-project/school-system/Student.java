import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Student — extends Person, adds academic tracking capabilities.
 *
 * OOP CONCEPTS:
 * - Inheritance: extends Person, inherits personId, name, age, email
 * - Encapsulation: grades list is private, returned as unmodifiable
 * - Polymorphism: can be used anywhere a Person is expected
 */
public class Student extends Person {

    private final String studentId;
    private String major;
    private final List<Double> grades;
    private int yearOfStudy;  // 1 = freshman, 2 = sophomore, etc.

    public Student(String personId, String name, int age, String email,
                   String studentId, String major, int yearOfStudy) {
        super(personId, name, age, email);  // Call Person constructor
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty");
        }
        if (yearOfStudy < 1 || yearOfStudy > 6) {
            throw new IllegalArgumentException("Year of study must be 1-6");
        }
        this.studentId = studentId.trim();
        this.major = major;
        this.yearOfStudy = yearOfStudy;
        this.grades = new ArrayList<>();
    }

    // Convenience constructor without yearOfStudy (defaults to 1)
    public Student(String personId, String name, int age, String email,
                   String studentId, String major) {
        this(personId, name, age, email, studentId, major, 1);
    }

    // =========================================================================
    // Student-specific methods
    // =========================================================================

    @Override
    public String getRole() {
        return "Student";
    }

    public void addGrade(double grade) {
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Grade must be 0-100, got: " + grade);
        }
        grades.add(grade);
    }

    public double calculateGPA() {
        if (grades.isEmpty()) return 0.0;
        double sum = 0;
        for (double grade : grades) {
            sum += grade;
        }
        double average = sum / grades.size();
        // Convert 0-100 scale to 4.0 GPA scale
        return (average / 100.0) * 4.0;
    }

    public double getAverageGrade() {
        if (grades.isEmpty()) return 0.0;
        double sum = 0;
        for (double grade : grades) {
            sum += grade;
        }
        return sum / grades.size();
    }

    public boolean isHonorRoll() {
        return calculateGPA() >= 3.5;
    }

    public String getLetterGrade() {
        double avg = getAverageGrade();
        if (avg >= 90) return "A";
        if (avg >= 80) return "B";
        if (avg >= 70) return "C";
        if (avg >= 60) return "D";
        return "F";
    }

    public void advanceYear() {
        if (yearOfStudy < 4) {
            yearOfStudy++;
            System.out.println(getName() + " advanced to year " + yearOfStudy);
        } else {
            System.out.println(getName() + " has completed their studies!");
        }
    }

    // Getters
    public String getStudentId() { return studentId; }
    public String getMajor() { return major; }
    public int getYearOfStudy() { return yearOfStudy; }
    public int getGradeCount() { return grades.size(); }

    public List<Double> getGrades() {
        return Collections.unmodifiableList(grades);
    }

    public void setMajor(String major) { this.major = major; }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.printf("  Student ID: %s | Major: %s | Year: %d%n",
                         studentId, major, yearOfStudy);
        if (!grades.isEmpty()) {
            System.out.printf("  GPA: %.2f (%s) | Grade: %s%s%n",
                             calculateGPA(), getLetterGrade(),
                             grades.size() + " courses",
                             isHonorRoll() ? " | HONOR ROLL" : "");
        }
    }

    @Override
    public String toString() {
        return String.format("Student{%s, id=%s, major=%s, gpa=%.2f}",
                            getName(), studentId, major, calculateGPA());
    }
}
