package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.grade.GradeDao;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.enumtype.UserRole;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeGradeDao implements GradeDao {
    private final Map<Long, Grade> grades = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Grade save(Long studentId, Long subjectId, Long lecturerId, double value, String description) {
        Long id = nextId.getAndIncrement();
        Grade grade = new Grade(
                id,
                createStudent(studentId),
                createCourse(subjectId, lecturerId),
                createLecturer(lecturerId),
                value,
                description
        );
        grades.put(id, grade);
        return grade;
    }

    @Override
    public Grade update(Long gradeId, double value, String description) {
        Grade grade = findExisting(gradeId);
        grade.setValue(value);
        grade.setDescription(description);
        return grade;
    }

    @Override
    public Optional<Grade> findById(Long gradeId) {
        return Optional.ofNullable(grades.get(gradeId));
    }

    @Override
    public List<Grade> findByStudentId(Long studentId) {
        return grades.values().stream()
                .filter(grade -> grade.getStudent() != null && studentId.equals(grade.getStudent().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Grade> findBySubjectId(Long subjectId) {
        return grades.values().stream()
                .filter(grade -> grade.getCourse() != null && subjectId.equals(grade.getCourse().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Grade> findByLecturerId(Long lecturerId) {
        return grades.values().stream()
                .filter(grade -> grade.getLecturer() != null && lecturerId.equals(grade.getLecturer().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Grade> findAll() {
        return new ArrayList<>(grades.values());
    }

    @Override
    public boolean existsDuplicate(Long studentId, Long subjectId, String description) {
        return grades.values().stream()
                .anyMatch(grade -> grade.getStudent() != null && grade.getCourse() != null
                        && grade.getStudent().getId().equals(studentId)
                        && grade.getCourse().getId().equals(subjectId)
                        && grade.getDescription() != null
                        && grade.getDescription().equalsIgnoreCase(description));
    }

    private Grade findExisting(Long gradeId) {
        Grade grade = grades.get(gradeId);
        if (grade == null) {
            throw new IllegalArgumentException("Grade not found.");
        }
        return grade;
    }

    private Student createStudent(Long studentId) {
        return new Student(studentId, "Student", "Student", "student" + studentId + "@example.com",
                "password", "S" + studentId, "Unknown", null);
    }

    private Lecturer createLecturer(Long lecturerId) {
        return new Lecturer(lecturerId, "Lecturer", "Lecturer", "lecturer" + lecturerId + "@example.com",
                "password", "EMP" + lecturerId, "Dr.");
    }

    private Course createCourse(Long subjectId, Long lecturerId) {
        return new Course(subjectId, "Course" + subjectId, "C" + subjectId, 5, createLecturer(lecturerId));
    }
}
