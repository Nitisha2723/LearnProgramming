package repository;

import model.Enrollment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link EnrollmentRepository}.
 */
public class InMemoryEnrollmentRepository implements EnrollmentRepository {

    private final Map<String, Enrollment> store = new HashMap<>();

    @Override
    public List<Enrollment> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Enrollment> findById(String enrollmentId) {
        return Optional.ofNullable(store.get(enrollmentId));
    }

    @Override
    public List<Enrollment> findByStudentId(String studentId) {
        if (studentId == null) return new ArrayList<>();
        return store.values().stream()
                .filter(e -> studentId.equals(e.getStudentId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Enrollment> findByCourseId(String courseId) {
        if (courseId == null) return new ArrayList<>();
        return store.values().stream()
                .filter(e -> courseId.equals(e.getCourseId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Enrollment> findByStudentAndCourse(String studentId, String courseId) {
        return store.values().stream()
                .filter(e -> studentId.equals(e.getStudentId())
                          && courseId.equals(e.getCourseId()))
                .findFirst();
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        if (store.containsKey(enrollment.getEnrollmentId())) {
            throw new IllegalArgumentException(
                    "Enrollment with ID " + enrollment.getEnrollmentId() + " already exists.");
        }
        store.put(enrollment.getEnrollmentId(), enrollment);
        return enrollment;
    }

    @Override
    public Enrollment update(Enrollment enrollment) {
        if (!store.containsKey(enrollment.getEnrollmentId())) {
            throw new IllegalArgumentException(
                    "No enrollment found with ID " + enrollment.getEnrollmentId());
        }
        store.put(enrollment.getEnrollmentId(), enrollment);
        return enrollment;
    }

    @Override
    public boolean delete(String enrollmentId) {
        return store.remove(enrollmentId) != null;
    }
}
