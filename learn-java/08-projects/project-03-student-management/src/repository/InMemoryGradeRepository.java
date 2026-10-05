package repository;

import model.Grade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link GradeRepository}.
 */
public class InMemoryGradeRepository implements GradeRepository {

    private final Map<String, Grade> store = new HashMap<>();

    @Override
    public List<Grade> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Grade> findById(String gradeId) {
        return Optional.ofNullable(store.get(gradeId));
    }

    @Override
    public List<Grade> findByStudentId(String studentId) {
        if (studentId == null) return new ArrayList<>();
        return store.values().stream()
                .filter(g -> studentId.equals(g.getStudentId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Grade> findByCourseId(String courseId) {
        if (courseId == null) return new ArrayList<>();
        return store.values().stream()
                .filter(g -> courseId.equals(g.getCourseId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Grade> findByEnrollmentId(String enrollmentId) {
        if (enrollmentId == null) return Optional.empty();
        return store.values().stream()
                .filter(g -> enrollmentId.equals(g.getEnrollmentId()))
                .findFirst();
    }

    @Override
    public Grade save(Grade grade) {
        if (store.containsKey(grade.getGradeId())) {
            throw new IllegalArgumentException(
                    "Grade with ID " + grade.getGradeId() + " already exists.");
        }
        store.put(grade.getGradeId(), grade);
        return grade;
    }

    @Override
    public Grade update(Grade grade) {
        if (!store.containsKey(grade.getGradeId())) {
            throw new IllegalArgumentException(
                    "No grade found with ID " + grade.getGradeId());
        }
        store.put(grade.getGradeId(), grade);
        return grade;
    }

    @Override
    public boolean delete(String gradeId) {
        return store.remove(gradeId) != null;
    }
}
