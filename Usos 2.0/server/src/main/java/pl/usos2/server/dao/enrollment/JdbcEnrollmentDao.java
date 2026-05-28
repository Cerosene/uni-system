package pl.usos2.server.dao.enrollment;

import pl.usos2.server.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
