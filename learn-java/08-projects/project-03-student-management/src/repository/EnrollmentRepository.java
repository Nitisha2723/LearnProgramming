package repository;

import model.Enrollment;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Enrollment} entities.
 */
public interface EnrollmentRepository {

    List<Enrollment> findAll();

    Optional<Enrollment> findById(String enrollmentId);

    /** Returns all enrollments for a given student. */
    List<Enrollment> findByStudentId(String studentId);

    /** Returns all enrollments for a given course. */
    List<Enrollment> findByCourseId(String courseId);

    /**
     * Looks up the enrollment linking a specific student to a specific course.
     * Returns empty if no such record exists.
     */
    Optional<Enrollment> findByStudentAndCourse(String studentId, String courseId);

    Enrollment save(Enrollment enrollment);

    Enrollment update(Enrollment enrollment);

    boolean delete(String enrollmentId);
}
