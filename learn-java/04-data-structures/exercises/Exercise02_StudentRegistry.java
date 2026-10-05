import java.util.*;
import java.util.stream.Collectors;

/**
 * Exercise02_StudentRegistry.java
 *
 * GOAL: Build a HashMap-based student registry with full CRUD operations.
 *
 * TASKS:
 * 1. Implement add, get, update, and delete operations
 * 2. Find students by various criteria (GPA, major)
 * 3. Compute statistics (average GPA, top students)
 * 4. Handle edge cases (null, duplicate IDs)
 */
public class Exercise02_StudentRegistry {

    record Student(String id, String name, String major, double gpa, int year) {
        @Override public String toString() {
            return String.format("Student{id='%s', name='%s', major='%s', gpa=%.2f, year=%d}",
                id, name, major, gpa, year);
        }
    }

    static class StudentRegistry {
        // TODO: Choose the right Map type here
        // Hint: We need O(1) lookup by student ID
        // Hint: What should K and V be?
        private final Map<String, Student> registry = /* YOUR CODE HERE */ null;

        // =====================================================
        // TODO 1: Add a student
        // - If a student with this ID already exists, throw IllegalArgumentException
        // - Return the registry (for chaining)
        // =====================================================
        public StudentRegistry addStudent(Student student) {
            // YOUR CODE HERE
            return this;
        }

        // =====================================================
        // TODO 2: Get a student by ID
        // - Return Optional<Student> (empty if not found)
        // =====================================================
        public Optional<Student> getStudent(String id) {
            // YOUR CODE HERE
            return Optional.empty();
        }

        // =====================================================
        // TODO 3: Update a student's GPA
        // - If student doesn't exist, throw IllegalArgumentException
        // - Records are immutable — you'll need to create a new Student
        //   Hint: new Student(existing.id(), existing.name(), ..., newGpa, ...)
        // =====================================================
        public void updateGpa(String id, double newGpa) {
            // YOUR CODE HERE
        }

        // =====================================================
        // TODO 4: Remove a student
        // - Return true if removed, false if not found
        // =====================================================
        public boolean removeStudent(String id) {
            // YOUR CODE HERE
            return false;
        }

        // =====================================================
        // TODO 5: Find all students in a given major
        // - Return sorted by GPA (descending)
        // =====================================================
        public List<Student> getByMajor(String major) {
            // YOUR CODE HERE
            return new ArrayList<>();
        }

        // =====================================================
        // TODO 6: Find students with GPA above a threshold
        // =====================================================
        public List<Student> getAboveGpa(double minGpa) {
            // YOUR CODE HERE
            return new ArrayList<>();
        }

        // =====================================================
        // TODO 7: Compute average GPA for a major
        // - Return OptionalDouble.empty() if no students in that major
        // =====================================================
        public OptionalDouble averageGpa(String major) {
            // YOUR CODE HERE
            return OptionalDouble.empty();
        }

        // =====================================================
        // TODO 8: Get top N students overall (by GPA)
        // =====================================================
        public List<Student> getTopStudents(int n) {
            // YOUR CODE HERE
            return new ArrayList<>();
        }

        // =====================================================
        // TODO 9: Group students by major
        // - Return Map<String, List<Student>>
        // =====================================================
        public Map<String, List<Student>> groupByMajor() {
            // YOUR CODE HERE
            return new HashMap<>();
        }

        // =====================================================
        // TODO 10: Print all students sorted by name
        // =====================================================
        public void printAll() {
            // YOUR CODE HERE
        }

        public int size() {
            return registry == null ? 0 : registry.size();
        }

        public boolean exists(String id) {
            return registry != null && registry.containsKey(id);
        }
    }

    // =========================================================
    // Main — Test your implementation
    // =========================================================
    public static void main(String[] args) {
        StudentRegistry registry = new StudentRegistry();

        // Add students
        registry.addStudent(new Student("S001", "Alice Chen",     "Computer Science", 3.9, 3))
                .addStudent(new Student("S002", "Bob Martinez",   "Mathematics",      3.5, 2))
                .addStudent(new Student("S003", "Charlie Davis",  "Computer Science", 3.2, 1))
                .addStudent(new Student("S004", "Diana Patel",    "Physics",          3.8, 4))
                .addStudent(new Student("S005", "Eve Wilson",     "Computer Science", 3.7, 2))
                .addStudent(new Student("S006", "Frank Johnson",  "Mathematics",      2.9, 3))
                .addStudent(new Student("S007", "Grace Kim",      "Physics",          3.6, 1))
                .addStudent(new Student("S008", "Henry Brown",    "Computer Science", 3.1, 4));

        System.out.println("Registry size: " + registry.size());

        // Test getStudent
        registry.getStudent("S001").ifPresent(s -> System.out.println("Found: " + s));
        System.out.println("S999 exists: " + registry.exists("S999"));

        // Test updateGpa
        registry.updateGpa("S003", 3.4);
        registry.getStudent("S003").ifPresent(s ->
            System.out.println("Updated GPA: " + s.gpa()));

        // Test getByMajor
        System.out.println("\nComputer Science students (by GPA):");
        registry.getByMajor("Computer Science")
                .forEach(s -> System.out.printf("  %-15s %.2f%n", s.name(), s.gpa()));

        // Test averageGpa
        registry.averageGpa("Computer Science")
            .ifPresent(avg -> System.out.printf("%nCS average GPA: %.2f%n", avg));

        // Test getTopStudents
        System.out.println("\nTop 3 students overall:");
        registry.getTopStudents(3)
                .forEach(s -> System.out.printf("  %-15s %.2f (%s)%n", s.name(), s.gpa(), s.major()));

        // Test groupByMajor
        System.out.println("\nStudents by major:");
        registry.groupByMajor().forEach((major, students) -> {
            System.out.println("  " + major + ": " + students.size() + " students");
        });

        // Test removeStudent
        boolean removed = registry.removeStudent("S008");
        System.out.println("\nRemoved S008: " + removed);
        System.out.println("Registry size after removal: " + registry.size());

        // Test duplicate ID
        try {
            registry.addStudent(new Student("S001", "Duplicate", "CS", 4.0, 1));
            System.out.println("ERROR: Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly rejected duplicate ID: " + e.getMessage());
        }

        // Print all students
        System.out.println("\nAll students (sorted by name):");
        registry.printAll();
    }
}
