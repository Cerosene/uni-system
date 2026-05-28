package pl.usos2.server.dao.enrollment;

import pl.usos2.server.model.user.Student;

import java.util.List;

public interface EnrollmentDao {
    void enroll(Long studentId, Long groupId);

    void remove(Long studentId, Long groupId);

    boolean existsActive(Long studentId, Long groupId);

    List<Student> findStudentsByGroupId(Long groupId);
}
