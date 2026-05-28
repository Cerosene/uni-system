package pl.usos2.server.dao.enrollment;

public interface EnrollmentDao {
    void enroll(Long studentId, Long groupId);

    void remove(Long studentId, Long groupId);

    boolean existsActive(Long studentId, Long groupId);
}
