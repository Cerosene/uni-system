package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.course.CourseDao;
import pl.usos2.server.model.academic.Course;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeCourseDao implements CourseDao {
    private final Map<Long, Course> courses = new ConcurrentHashMap<>();

    @Override
    public Course save(Course course) {
        courses.put(course.getId(), course);
        return course;
    }

    @Override
    public Optional<Course> findById(Long courseId) {
        return Optional.ofNullable(courses.get(courseId));
    }

    @Override
    public List<Course> findAll() {
        return new ArrayList<>(courses.values());
    }

    @Override
    public List<Course> findByLecturerId(Long lecturerId) {
        return courses.values().stream()
                .filter(course -> course.getLecturer() != null && lecturerId.equals(course.getLecturer().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long courseId) {
        return courses.containsKey(courseId);
    }

    @Override
    public boolean existsByCode(String courseCode) {
        return courses.values().stream()
                .anyMatch(course -> course.getCode() != null && course.getCode().equalsIgnoreCase(courseCode));
    }
}
