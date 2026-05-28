package pl.usos2.server.dao.course;

import pl.usos2.server.model.academic.Course;

import java.util.List;
import java.util.Optional;

public interface CourseDao {
    Course save(Course course);

    Optional<Course> findById(Long courseId);

    List<Course> findAll();

    List<Course> findByLecturerId(Long lecturerId);

    boolean existsById(Long courseId);

    boolean existsByCode(String courseCode);
}
