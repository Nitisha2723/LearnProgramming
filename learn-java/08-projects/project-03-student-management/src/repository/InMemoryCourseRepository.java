package repository;

import model.Course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link CourseRepository}.
 */
public class InMemoryCourseRepository implements CourseRepository {

    private final Map<String, Course> store = new HashMap<>();

    @Override
    public List<Course> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Course> findById(String courseId) {
        return Optional.ofNullable(store.get(courseId));
    }

    @Override
    public Optional<Course> findByCourseCode(String courseCode) {
        if (courseCode == null) return Optional.empty();
        return store.values().stream()
                .filter(c -> courseCode.equalsIgnoreCase(c.getCourseCode()))
                .findFirst();
    }

    @Override
    public List<Course> findByTeacherId(String teacherId) {
        if (teacherId == null) return new ArrayList<>();
        return store.values().stream()
                .filter(c -> teacherId.equals(c.getTeacherId()))
                .collect(Collectors.toList());
    }

    @Override
    public Course save(Course course) {
        if (store.containsKey(course.getCourseId())) {
            throw new IllegalArgumentException(
                    "Course with ID " + course.getCourseId() + " already exists.");
        }
        store.put(course.getCourseId(), course);
        return course;
    }

    @Override
    public Course update(Course course) {
        if (!store.containsKey(course.getCourseId())) {
            throw new IllegalArgumentException(
                    "No course found with ID " + course.getCourseId());
        }
        store.put(course.getCourseId(), course);
        return course;
    }

    @Override
    public boolean delete(String courseId) {
        return store.remove(courseId) != null;
    }
}
