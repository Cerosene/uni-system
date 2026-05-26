package pl.usos2.server.service.course;

import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.util.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CourseService {
    private static final Logger logger = Logger.getLogger(CourseService.class.getName());

    private final List<Course> courses = new ArrayList<>();

    public Course addCourse(Course course) {
        ValidationUtils.requireNotNull(course, "Course cannot be null.");
        ValidationUtils.requireNotNull(course.getId(), "Course id cannot be null.");
        ValidationUtils.requireNotBlank(course.getName(), "Course name cannot be empty.");
        ValidationUtils.requireNotBlank(course.getCode(), "Course code cannot be empty.");
        if (course.getEcts() <= 0) {
            throw new IllegalArgumentException("ECTS must be greater than zero.");
        }
        ValidationUtils.requireNotNull(course.getLecturer(), "Course lecturer cannot be null.");
        ValidationUtils.requireNotNull(course.getLecturer().getId(), "Lecturer id cannot be null.");

        boolean duplicateCode = courses.stream()
                .anyMatch(existing -> existing.getCode().equalsIgnoreCase(course.getCode()));

        if (duplicateCode) {
            logger.warning("Cannot add course. Duplicate code: " + course.getCode());
            throw new IllegalArgumentException("Course code already exists.");
        }

        courses.add(course);
        logger.info("Added course: " + course.getCode() + " - " + course.getName());
        return course;
    }

    public List<Course> getAllCourses() {
        return new ArrayList<>(courses);
    }

    public List<Course> getCoursesForLecturer(Lecturer lecturer) {
        ValidationUtils.requireNotNull(lecturer, "Lecturer cannot be null.");
        ValidationUtils.requireNotNull(lecturer.getId(), "Lecturer id cannot be null.");

        return courses.stream()
                .filter(course -> course.getLecturer().getId().equals(lecturer.getId()))
                .toList();
    }
}
