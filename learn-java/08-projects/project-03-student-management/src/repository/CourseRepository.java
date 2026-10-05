package repository;

import model.Course;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Course} entities.
 */
public interface CourseRepository {

    List<Course> findAll();

    Optional<Course> findById(String courseId);

    /** Looks up a course by its human-readable code (e.g. "CS101"). */
    Optional<Course> findByCourseCode(String courseCode);

    /** Returns all courses assigned to a specific teacher. */
    List<Course> findByTeacherId(String teacherId);

    Course save(Course course);

    Course update(Course course);

    boolean delete(String courseId);
}
