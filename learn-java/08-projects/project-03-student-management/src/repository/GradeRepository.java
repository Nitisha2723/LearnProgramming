package repository;

import model.Grade;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Grade} entities.
 */
public interface GradeRepository {

    List<Grade> findAll();

    Optional<Grade> findById(String gradeId);

    /** Returns all grades for a given student across all courses. */
    List<Grade> findByStudentId(String studentId);

    /** Returns all grades for a given course across all students. */
    List<Grade> findByCourseId(String courseId);

    /** Returns the grade associated with a specific enrollment record. */
    Optional<Grade> findByEnrollmentId(String enrollmentId);

    Grade save(Grade grade);

    Grade update(Grade grade);

    boolean delete(String gradeId);
}
