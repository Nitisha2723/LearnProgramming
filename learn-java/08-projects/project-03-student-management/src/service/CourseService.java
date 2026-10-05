package service;

import model.Course;
import repository.CourseRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Business logic for course lifecycle management.
 */
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    /**
     * Creates a new course.
     *
     * @throws IllegalArgumentException if the course code is already in use
     */
    public Course createCourse(String courseCode, String courseName,
                               String teacherId, int maxCapacity, int credits) {
        if (courseCode == null || courseCode.isBlank())
            throw new IllegalArgumentException("Course code is required.");
        if (courseName == null || courseName.isBlank())
            throw new IllegalArgumentException("Course name is required.");
        if (maxCapacity <= 0)
            throw new IllegalArgumentException("maxCapacity must be positive.");
        if (credits <= 0)
            throw new IllegalArgumentException("credits must be positive.");
        if (courseRepository.findByCourseCode(courseCode).isPresent())
            throw new IllegalArgumentException("Course code already exists: " + courseCode);

        Course course = new Course(courseCode, courseName, teacherId, maxCapacity, credits);
        return courseRepository.save(course);
    }

    /**
     * Retrieves a course by its UUID.
     *
     * @throws NoSuchElementException if not found
     */
    public Course getCourse(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Course not found: " + courseId));
    }

    /**
     * Updates mutable fields of an existing course.
     */
    public Course updateCourse(String courseId, String courseCode, String courseName,
                               String teacherId, Integer maxCapacity, Integer credits) {
        Course course = getCourse(courseId);

        if (courseCode != null && !courseCode.isBlank()) {
            // ensure new code not taken by a different course
            courseRepository.findByCourseCode(courseCode).ifPresent(existing -> {
                if (!existing.getCourseId().equals(courseId)) {
                    throw new IllegalArgumentException(
                            "Course code already in use: " + courseCode);
                }
            });
            course.setCourseCode(courseCode);
        }
        if (courseName  != null && !courseName.isBlank())  course.setCourseName(courseName);
        if (teacherId   != null && !teacherId.isBlank())   course.setTeacherId(teacherId);
        if (maxCapacity != null && maxCapacity > 0)        course.setMaxCapacity(maxCapacity);
        if (credits     != null && credits > 0)            course.setCredits(credits);

        return courseRepository.update(course);
    }

    /**
     * Deletes a course by ID.
     *
     * @throws NoSuchElementException if not found
     */
    public boolean deleteCourse(String courseId) {
        getCourse(courseId); // throws if absent
        return courseRepository.delete(courseId);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Returns all courses assigned to a specific teacher.
     */
    public List<Course> getCoursesByTeacher(String teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    /**
     * Returns courses that still have available seats.
     */
    public List<Course> getAvailableCourses() {
        return courseRepository.findAll().stream()
                .filter(c -> !c.isFull())
                .collect(Collectors.toList());
    }
}
