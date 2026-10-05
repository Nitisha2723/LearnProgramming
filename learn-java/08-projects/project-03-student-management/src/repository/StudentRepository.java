package repository;

import model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Student} entities.
 * Implementations may use in-memory maps, a database, a file, etc.
 */
public interface StudentRepository {

    /** Returns all students in the repository. */
    List<Student> findAll();

    /** Looks up a student by their unique UUID. */
    Optional<Student> findById(String studentId);

    /** Looks up a student by email address (case-insensitive). */
    Optional<Student> findByEmail(String email);

    /** Returns all students whose major matches the given string (case-insensitive). */
    List<Student> findByMajor(String major);

    /**
     * Persists a new student.
     *
     * @param student the student to save
     * @return the saved student (same object)
     * @throws IllegalArgumentException if a student with the same ID already exists
     */
    Student save(Student student);

    /**
     * Updates an existing student record.
     *
     * @param student the student with updated fields (must already exist)
     * @return the updated student
     * @throws IllegalArgumentException if no student with that ID exists
     */
    Student update(Student student);

    /**
     * Removes a student by ID.
     *
     * @param studentId the UUID of the student to remove
     * @return {@code true} if a student was removed, {@code false} otherwise
     */
    boolean delete(String studentId);

    /** Returns the total number of students in the repository. */
    long count();
}
