package pl.usos2.server.dao.enrollment;

import pl.usos2.server.dao.enrollment.EnrollmentDao;
import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JdbcEnrollmentDao implements EnrollmentDao {
    private static final Logger logger = Logger.getLogger(JdbcEnrollmentDao.class.getName());

    private static final String EXISTS_ACTIVE_SQL = """
            SELECT COUNT(*)
            FROM enrollments
            WHERE student_id = ?
              AND group_id = ?
              AND active_flag = 'Y'
            """;
    private static final String EXISTS_ANY_SQL = """
            SELECT COUNT(*)
            FROM enrollments
            WHERE student_id = ?
              AND group_id = ?
            """;
    private static final String FIND_STUDENTS_BY_GROUP_SQL = """
            SELECT u.user_id,
                   u.first_name,
                   u.last_name,
                   u.email,
                   u.password_hash,
                   s.student_number,
                   s.field_of_study,
                   s.semester,
                   u.active_flag
            FROM enrollments e
                     JOIN students s ON e.student_id = s.student_id
                     JOIN users u ON s.student_id = u.user_id
            WHERE e.group_id = ?
              AND e.active_flag = 'Y'
            """;
    private static final String INSERT_SQL = """
            INSERT INTO enrollments (student_id, group_id, active_flag, enrolled_at)
            VALUES (?, ?, 'Y', SYSTIMESTAMP)
            """;
    private static final String REACTIVATE_SQL = """
            UPDATE enrollments
            SET active_flag = 'Y',
                enrolled_at = SYSTIMESTAMP
            WHERE student_id = ?
              AND group_id = ?
            """;
    private static final String DEACTIVATE_SQL = """
            UPDATE enrollments
            SET active_flag = 'N'
            WHERE student_id = ?
              AND group_id = ?
              AND active_flag = 'Y'
            """;

    @Override
    public void enroll(Long studentId, Long groupId) {
        if (existsActive(studentId, groupId)) {
            throw new IllegalArgumentException("Student is already enrolled in this group.");
        }

        if (existsAny(studentId, groupId)) {
            executeUpdate(REACTIVATE_SQL, studentId, groupId, "Failed to reactivate enrollment in Oracle.");
            logger.info("[DIAGNOSTIC] Enrollment reactivated in Oracle. studentId=" + studentId + ", groupId=" + groupId);
            return;
        }

        executeUpdate(INSERT_SQL, studentId, groupId, "Failed to create enrollment in Oracle.");
        logger.info("[DIAGNOSTIC] Enrollment created in Oracle. studentId=" + studentId + ", groupId=" + groupId);
    }

    @Override
    public void remove(Long studentId, Long groupId) {
        int updated = executeUpdate(DEACTIVATE_SQL, studentId, groupId, "Failed to remove enrollment in Oracle.");
        if (updated == 0) {
            throw new IllegalArgumentException("Enrollment not found.");
        }

        logger.info("[DIAGNOSTIC] Enrollment removed (deactivated) in Oracle. studentId=" + studentId + ", groupId=" + groupId);
    }

    @Override
    public boolean existsActive(Long studentId, Long groupId) {
        return existsByQuery(EXISTS_ACTIVE_SQL, studentId, groupId);
    }

    @Override
    public List<Student> findStudentsByGroupId(Long groupId) {
        List<Student> students = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_STUDENTS_BY_GROUP_SQL)) {
            statement.setLong(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    students.add(mapStudent(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query enrolled students from Oracle.", exception);
        }
        logger.info("[DIAGNOSTIC] Enrolled students loaded from Oracle. groupId=" + groupId + ", count=" + students.size());
        return students;
    }

    private boolean existsAny(Long studentId, Long groupId) {
        return existsByQuery(EXISTS_ANY_SQL, studentId, groupId);
    }

    private boolean existsByQuery(String sql, Long studentId, Long groupId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            statement.setLong(2, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to execute enrollment query in Oracle.", exception);
        }
    }

    private Student mapStudent(ResultSet resultSet) throws SQLException {
        Student student = new Student(
                resultSet.getLong("user_id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getString("student_number"),
                resultSet.getString("field_of_study"),
                toSemester(resultSet.getString("semester"))
        );
        student.setActive("Y".equalsIgnoreCase(resultSet.getString("active_flag")));
        return student;
    }

    private Semester toSemester(String value) {
        if (value == null || value.isBlank()) {
            return Semester.FIRST;
        }
        return Semester.valueOf(value.trim().toUpperCase());
    }

    private int executeUpdate(String sql, Long studentId, Long groupId, String errorMessage) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            statement.setLong(2, groupId);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
    }
}
