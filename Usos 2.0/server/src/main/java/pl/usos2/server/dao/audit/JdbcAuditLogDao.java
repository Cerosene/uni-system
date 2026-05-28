package pl.usos2.server.dao.audit;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.audit.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JdbcAuditLogDao implements AuditLogDao {
    private static final Logger logger = Logger.getLogger(JdbcAuditLogDao.class.getName());

    private static final String INSERT_SQL = """
            INSERT INTO audit_logs (audit_log_id, user_id, action_name, entity_name, entity_id, action_details, created_at)
            VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP)
            """;
    private static final String NEXT_ID_SQL = "SELECT NVL(MAX(audit_log_id), 0) + 1 FROM audit_logs";

    private static final String BASE_SELECT = """
            SELECT
                audit_log_id,
                user_id,
                action_name,
                entity_name,
                entity_id,
                action_details,
                created_at
            FROM audit_logs
            """;

    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY created_at DESC, audit_log_id DESC";
    private static final String FIND_BY_USER_SQL = BASE_SELECT + " WHERE user_id = ? ORDER BY created_at DESC, audit_log_id DESC";
    private static final String FIND_BY_ACTION_SQL = BASE_SELECT + " WHERE UPPER(action_name) = UPPER(?) ORDER BY created_at DESC, audit_log_id DESC";
    private static final String FIND_BY_DATE_SQL = BASE_SELECT + " WHERE created_at >= ? AND created_at <= ? ORDER BY created_at DESC, audit_log_id DESC";

    @Override
    public AuditLog save(Long userId, String actionName, String entityName, Long entityId, String actionDetails) {
        Long auditId = getNextId();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, auditId);
            if (userId == null) {
                statement.setNull(2, java.sql.Types.NUMERIC);
            } else {
                statement.setLong(2, userId);
            }
            statement.setString(3, actionName);
            statement.setString(4, entityName);
            if (entityId == null) {
                statement.setNull(5, java.sql.Types.NUMERIC);
            } else {
                statement.setLong(5, entityId);
            }
            statement.setString(6, actionDetails);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save audit log in Oracle.", exception);
        }

        AuditLog log = new AuditLog(auditId, userId, actionName, entityName, entityId, actionDetails, LocalDateTime.now());
        logger.info("[DIAGNOSTIC] Audit log persisted in Oracle. auditLogId=" + auditId + ", action=" + actionName);
        return log;
    }

    @Override
    public List<AuditLog> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    @Override
    public List<AuditLog> findByUserId(Long userId) {
        return executeListQuery(FIND_BY_USER_SQL, statement -> statement.setLong(1, userId));
    }

    @Override
    public List<AuditLog> findByActionName(String actionName) {
        return executeListQuery(FIND_BY_ACTION_SQL, statement -> statement.setString(1, actionName));
    }

    @Override
    public List<AuditLog> findByDateRange(LocalDateTime fromInclusive, LocalDateTime toInclusive) {
        return executeListQuery(FIND_BY_DATE_SQL, statement -> {
            statement.setTimestamp(1, Timestamp.valueOf(fromInclusive));
            statement.setTimestamp(2, Timestamp.valueOf(toInclusive));
        });
    }

    private List<AuditLog> executeListQuery(String sql, SqlParameterSetter setter) {
        List<AuditLog> result = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapAuditLog(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query audit logs from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle audit log query returned rows: " + result.size());
        return result;
    }

    private Long getNextId() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(NEXT_ID_SQL)) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Cannot generate next audit log id.");
            }
            return resultSet.getLong(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to generate audit log id from Oracle.", exception);
        }
    }

    private AuditLog mapAuditLog(ResultSet resultSet) throws SQLException {
        Long userId = resultSet.getObject("user_id") == null ? null : resultSet.getLong("user_id");
        Long entityId = resultSet.getObject("entity_id") == null ? null : resultSet.getLong("entity_id");
        Timestamp createdAt = resultSet.getTimestamp("created_at");

        return new AuditLog(
                resultSet.getLong("audit_log_id"),
                userId,
                resultSet.getString("action_name"),
                resultSet.getString("entity_name"),
                entityId,
                resultSet.getString("action_details"),
                createdAt == null ? null : createdAt.toLocalDateTime()
        );
    }

    @FunctionalInterface
    private interface SqlParameterSetter {
        void set(PreparedStatement statement) throws SQLException;
    }
}
