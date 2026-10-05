package repository;

import model.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link StudentRepository} backed by a {@link HashMap}.
 * Suitable for unit tests and demos; data is lost when the JVM exits.
 */
public class InMemoryStudentRepository implements StudentRepository {

    private final Map<String, Student> store = new HashMap<>();

    @Override
    public List<Student> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Student> findById(String studentId) {
        return Optional.ofNullable(store.get(studentId));
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return store.values().stream()
                .filter(s -> email.equalsIgnoreCase(s.getEmail()))
                .findFirst();
    }

    @Override
    public List<Student> findByMajor(String major) {
        if (major == null) return new ArrayList<>();
        return store.values().stream()
                .filter(s -> major.equalsIgnoreCase(s.getMajor()))
                .collect(Collectors.toList());
    }

    @Override
    public Student save(Student student) {
        if (store.containsKey(student.getStudentId())) {
            throw new IllegalArgumentException(
                    "Student with ID " + student.getStudentId() + " already exists.");
        }
        store.put(student.getStudentId(), student);
        return student;
    }

    @Override
    public Student update(Student student) {
        if (!store.containsKey(student.getStudentId())) {
            throw new IllegalArgumentException(
                    "No student found with ID " + student.getStudentId());
        }
        store.put(student.getStudentId(), student);
        return student;
    }

    @Override
    public boolean delete(String studentId) {
        return store.remove(studentId) != null;
    }

    @Override
    public long count() {
        return store.size();
    }
}
