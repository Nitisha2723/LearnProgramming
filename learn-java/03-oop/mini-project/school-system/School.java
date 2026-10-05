import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * School — top-level class managing all people and courses.
 *
 * OOP CONCEPTS:
 * - Composition: School HAS students, teachers, courses
 * - Polymorphism: getPeople() returns a List<Person> containing both Students and Teachers
 * - Encapsulation: all lists are private with controlled access
 * - Rich domain model: analytics and reporting live here
 */
public class School {

    private final String name;
    private final String address;
    private final List<Student> students;
    private final List<Teacher> teachers;
    private final List<Course> courses;

    public School(String name, String address) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("School name cannot be empty");
        }
        this.name = name.trim();
        this.address = address;
        this.students = new ArrayList<>();
        this.teachers = new ArrayList<>();
        this.courses = new ArrayList<>();
    }

    // =========================================================================
    // Student Management
    // =========================================================================

    public void addStudent(Student student) {
        if (student == null) throw new IllegalArgumentException("Student cannot be null");
        if (students.contains(student)) {
            System.out.println(student.getName() + " is already enrolled in " + name);
            return;
        }
        students.add(student);
        System.out.println("Enrolled student: " + student.getName());
    }

    public boolean removeStudent(Student student) {
        if (students.remove(student)) {
            // Also remove from all courses
            for (Course course : courses) {
                if (course.isEnrolled(student)) {
                    course.withdraw(student);
                }
            }
            System.out.println("Removed student: " + student.getName());
            return true;
        }
        System.out.println("Student " + student.getName() + " not found in " + name);
        return false;
    }

    public Student findStudentById(String studentId) {
        for (Student student : students) {
            if (student.getStudentId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }

    // =========================================================================
    // Teacher Management
    // =========================================================================

    public void addTeacher(Teacher teacher) {
        if (teacher == null) throw new IllegalArgumentException("Teacher cannot be null");
        if (teachers.contains(teacher)) {
            System.out.println(teacher.getName() + " is already on staff at " + name);
            return;
        }
        teachers.add(teacher);
        System.out.println("Hired teacher: " + teacher.getName());
    }

    public boolean removeTeacher(Teacher teacher) {
        if (teachers.remove(teacher)) {
            System.out.println("Removed teacher: " + teacher.getName());
            return true;
        }
        System.out.println("Teacher " + teacher.getName() + " not found at " + name);
        return false;
    }

    public Teacher findTeacherById(String employeeId) {
        for (Teacher teacher : teachers) {
            if (teacher.getEmployeeId().equals(employeeId)) {
                return teacher;
            }
        }
        return null;
    }

    // =========================================================================
    // Course Management
    // =========================================================================

    public void addCourse(Course course) {
        if (course == null) throw new IllegalArgumentException("Course cannot be null");
        if (courses.contains(course)) {
            System.out.println("Course " + course.getName() + " already offered at " + name);
            return;
        }
        courses.add(course);
        System.out.println("Added course: " + course.getName());
    }

    public boolean removeCourse(Course course) {
        if (courses.remove(course)) {
            System.out.println("Removed course: " + course.getName());
            return true;
        }
        return false;
    }

    public Course findCourseById(String courseId) {
        for (Course course : courses) {
            if (course.getCourseId().equals(courseId)) {
                return course;
            }
        }
        return null;
    }

    // =========================================================================
    // Reports and Analytics
    // =========================================================================

    /**
     * Returns ALL people (students + teachers) as a List<Person>.
     *
     * This demonstrates polymorphism: students and teachers are both Persons,
     * so they can be stored and returned together in a List<Person>.
     */
    public List<Person> getAllPeople() {
        List<Person> everyone = new ArrayList<>();
        everyone.addAll(students);   // Each Student IS a Person
        everyone.addAll(teachers);   // Each Teacher IS a Person
        return Collections.unmodifiableList(everyone);
    }

    public void printDirectoryAll() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(name + " — DIRECTORY");
        System.out.println("=".repeat(60));

        // Polymorphism in action: getAllPeople() returns both Students and Teachers
        // displayInfo() is an abstract method in Person, implemented differently by each
        for (Person person : getAllPeople()) {
            person.displayInfo();   // Calls the right version based on actual type
            System.out.println();
        }
    }

    public void printAcademicReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(name + " — ACADEMIC REPORT");
        System.out.println("=".repeat(60));

        System.out.println("\nSTUDENTS (" + students.size() + " total):");
        System.out.println("-".repeat(50));
        if (students.isEmpty()) {
            System.out.println("  No students enrolled.");
        } else {
            List<Student> sorted = new ArrayList<>(students);
            sorted.sort((a, b) -> Double.compare(b.calculateGPA(), a.calculateGPA()));
            for (int i = 0; i < sorted.size(); i++) {
                Student s = sorted.get(i);
                System.out.printf("  %d. %-20s GPA: %.2f (%s)%s%n",
                                 i + 1,
                                 s.getName(),
                                 s.calculateGPA(),
                                 s.getLetterGrade(),
                                 s.isHonorRoll() ? " ★ Honor Roll" : "");
            }
        }

        System.out.println("\nCOURSES (" + courses.size() + " total):");
        System.out.println("-".repeat(50));
        if (courses.isEmpty()) {
            System.out.println("  No courses offered.");
        } else {
            for (Course course : courses) {
                System.out.printf("  %-10s %-25s Teacher: %-15s Enrolled: %d/%d  Avg: %.1f%n",
                                 course.getCourseId(),
                                 course.getName(),
                                 course.getTeacher() != null ? course.getTeacher().getName() : "TBD",
                                 course.getEnrollmentCount(),
                                 course.getMaxCapacity(),
                                 course.getClassAverage());
            }
        }
    }

    public void printHonorRoll() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(name + " — HONOR ROLL");
        System.out.println("=".repeat(60));

        boolean hasHonorStudents = false;
        for (Student student : students) {
            if (student.isHonorRoll()) {
                System.out.printf("  ★ %-20s GPA: %.2f | %s | Year %d%n",
                                 student.getName(),
                                 student.calculateGPA(),
                                 student.getMajor(),
                                 student.getYearOfStudy());
                hasHonorStudents = true;
            }
        }
        if (!hasHonorStudents) {
            System.out.println("  No students currently on the honor roll.");
        }
    }

    public Student getTopStudent() {
        if (students.isEmpty()) return null;
        Student top = students.get(0);
        for (Student student : students) {
            if (student.calculateGPA() > top.calculateGPA()) {
                top = student;
            }
        }
        return top;
    }

    public double getSchoolAverageGPA() {
        if (students.isEmpty()) return 0.0;
        double total = 0;
        int count = 0;
        for (Student student : students) {
            if (student.getGradeCount() > 0) {
                total += student.calculateGPA();
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    // Getters
    public String getName() { return name; }
    public String getAddress() { return address; }
    public int getStudentCount() { return students.size(); }
    public int getTeacherCount() { return teachers.size(); }
    public int getCourseCount() { return courses.size(); }

    public List<Student> getStudents() { return Collections.unmodifiableList(students); }
    public List<Teacher> getTeachers() { return Collections.unmodifiableList(teachers); }
    public List<Course> getCourses() { return Collections.unmodifiableList(courses); }

    @Override
    public String toString() {
        return String.format("School{name=%s, students=%d, teachers=%d, courses=%d}",
                            name, students.size(), teachers.size(), courses.size());
    }
}
