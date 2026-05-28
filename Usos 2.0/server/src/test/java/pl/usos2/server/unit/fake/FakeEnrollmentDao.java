package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.enrollment.EnrollmentDao;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeEnrollmentDao implements EnrollmentDao {
    private final Map<String, Boolean> enrollmentStatus = new ConcurrentHashMap<>();

    @Override
    public void enroll(Long studentId, Long groupId) {
        String key = generateKey(studentId, groupId);
        if (Boolean.TRUE.equals(enrollmentStatus.get(key))) {
            throw new IllegalArgumentException("Student is already enrolled in this group.");
        }

        enrollmentStatus.put(key, true);
    }

    @Override
    public void remove(Long studentId, Long groupId) {
        String key = generateKey(studentId, groupId);
        if (!Boolean.TRUE.equals(enrollmentStatus.get(key))) {
            throw new IllegalArgumentException("Enrollment not found.");
        }
        enrollmentStatus.put(key, false);
    }

    @Override
    public boolean existsActive(Long studentId, Long groupId) {
        return Boolean.TRUE.equals(enrollmentStatus.get(generateKey(studentId, groupId)));
    }

    @Override
    public List<Student> findStudentsByGroupId(Long groupId) {
        return enrollmentStatus.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue()) && entry.getKey().endsWith("#" + groupId))
                .map(entry -> parseStudent(entry.getKey()))
                .collect(Collectors.toList());
    }

    private String generateKey(Long studentId, Long groupId) {
        return studentId + "#" + groupId;
    }

    private Student parseStudent(String key) {
        String[] parts = key.split("#");
        Long studentId = Long.parseLong(parts[0]);
        return new Student(studentId, "Student", "Student", "student" + studentId + "@example.com",
                "password", "S" + studentId, "Unknown", Semester.FIRST);
    }
}
