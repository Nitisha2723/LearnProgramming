import java.util.*;
import java.util.stream.Collectors;

/**
 * Solution to Exercise02_StudentRegistry
 */
public class Exercise02_StudentRegistry {

    record Student(String id, String name, String major, double gpa, int year) {
        @Override public String toString() {
            return String.format("Student{id='%s', name='%s', major='%s', gpa=%.2f, year=%d}",
                id, name, major, gpa, year);
        }
    }

    static class StudentRegistry {
        private final Map<String, Student> registry = new HashMap<>();

        public StudentRegistry addStudent(Student student) {
            if (registry.containsKey(student.id())) {
                throw new IllegalArgumentException("Student ID already exists: " + student.id());
            }
            registry.put(student.id(), student);
            return this;
        }

        public Optional<Student> getStudent(String id) {
            return Optional.ofNullable(registry.get(id));
        }

        public void updateGpa(String id, double newGpa) {
            Student existing = registry.get(id);
            if (existing == null) throw new IllegalArgumentException("Student not found: " + id);
            registry.put(id, new Student(existing.id(), existing.name(),
                existing.major(), newGpa, existing.year()));
        }

        public boolean removeStudent(String id) {
            return registry.remove(id) != null;
        }

        public List<Student> getByMajor(String major) {
            return registry.values().stream()
                .filter(s -> s.major().equalsIgnoreCase(major))
                .sorted(Comparator.comparingDouble(Student::gpa).reversed())
                .collect(Collectors.toList());
        }

        public List<Student> getAboveGpa(double minGpa) {
            return registry.values().stream()
                .filter(s -> s.gpa() >= minGpa)
                .sorted(Comparator.comparingDouble(Student::gpa).reversed())
                .collect(Collectors.toList());
        }

        public OptionalDouble averageGpa(String major) {
            return registry.values().stream()
                .filter(s -> s.major().equalsIgnoreCase(major))
                .mapToDouble(Student::gpa)
                .average();
        }

        public List<Student> getTopStudents(int n) {
            return registry.values().stream()
                .sorted(Comparator.comparingDouble(Student::gpa).reversed())
                .limit(n)
                .collect(Collectors.toList());
        }

        public Map<String, List<Student>> groupByMajor() {
            return registry.values().stream()
                .collect(Collectors.groupingBy(Student::major));
        }

        public void printAll() {
            registry.values().stream()
                .sorted(Comparator.comparing(Student::name))
                .forEach(s -> System.out.printf("  %-20s %-20s %.2f%n",
                    s.name(), s.major(), s.gpa()));
        }

        public int size() { return registry.size(); }
        public boolean exists(String id) { return registry.containsKey(id); }
    }

    public static void main(String[] args) {
        StudentRegistry registry = new StudentRegistry();
        registry.addStudent(new Student("S001", "Alice Chen",    "Computer Science", 3.9, 3))
                .addStudent(new Student("S002", "Bob Martinez",  "Mathematics",      3.5, 2))
                .addStudent(new Student("S003", "Charlie Davis", "Computer Science", 3.2, 1))
                .addStudent(new Student("S004", "Diana Patel",   "Physics",          3.8, 4))
                .addStudent(new Student("S005", "Eve Wilson",    "Computer Science", 3.7, 2))
                .addStudent(new Student("S006", "Frank Johnson", "Mathematics",      2.9, 3))
                .addStudent(new Student("S007", "Grace Kim",     "Physics",          3.6, 1))
                .addStudent(new Student("S008", "Henry Brown",   "Computer Science", 3.1, 4));

        System.out.println("Size: " + registry.size());
        registry.getStudent("S001").ifPresent(s -> System.out.println("Found: " + s));

        registry.updateGpa("S003", 3.4);
        registry.getStudent("S003").ifPresent(s -> System.out.println("Updated: " + s.gpa()));

        System.out.println("\nCS students:");
        registry.getByMajor("Computer Science").forEach(s ->
            System.out.printf("  %-15s %.2f%n", s.name(), s.gpa()));

        registry.averageGpa("Computer Science").ifPresent(avg ->
            System.out.printf("%nCS avg GPA: %.2f%n", avg));

        System.out.println("\nTop 3:");
        registry.getTopStudents(3).forEach(s ->
            System.out.printf("  %-15s %.2f%n", s.name(), s.gpa()));

        try {
            registry.addStudent(new Student("S001", "Dup", "CS", 4.0, 1));
        } catch (IllegalArgumentException e) {
            System.out.println("\nCorrectly rejected: " + e.getMessage());
        }
    }
}
