package pl.usos2.server.dao.request;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.model.user.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public class JdbcRequestDao implements RequestDao {
    private static final Logger logger = Logger.getLogger(JdbcRequestDao.class.getName());

    private static final String BASE_SELECT = """
            SELECT
                a.application_id,
                a.application_type,
                a.content,
                a.status,
                a.created_at,
                s.student_id,
                s.student_number,
                s.field_of_study,
                s.semester,
                u.first_name,
                u.last_name,
                u.email,
                u.password_hash,
                u.active_flag
            FROM applications a
            JOIN students s ON s.student_id = a.student_id
            JOIN users u ON u.user_id = s.student_id
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE a.application_id = ?";
    private static final String FIND_BY_STUDENT_SQL = BASE_SELECT + " WHERE a.student_id = ? ORDER BY a.created_at DESC";
    private static final String FIND_BY_STATUS_SQL = BASE_SELECT + " WHERE a.status = ? ORDER BY a.created_at DESC";
    private static final String FIND_BY_TYPE_SQL = BASE_SELECT + " WHERE a.application_type = ? ORDER BY a.created_at DESC";
    private static final String FIND_PENDING_SQL = BASE_SELECT
            + " WHERE a.status IN ('SUBMITTED', 'IN_REVIEW') ORDER BY a.created_at DESC";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY a.created_at DESC";

    private static final String INSERT_SQL = """
            INSERT INTO applications (application_id, student_id, application_type, content, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_STATUS_SQL = """
            UPDATE applications
            SET status = ?
            WHERE application_id = ?
            """;
    private static final String NEXT_ID_SQL = "SELECT NVL(MAX(application_id), 0) + 1 FROM applications";

    @Override
    public Request save(Long studentId, RequestType type, String content) {
        Long nextId = getNextRequestId();
        LocalDateTime createdAt = LocalDateTime.now();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, nextId);
            statement.setLong(2, studentId);
            statement.setString(3, type.name());
            statement.setString(4, content);
            statement.setString(5, RequestStatus.SUBMITTED.name());
            statement.setTimestamp(6, Timestamp.valueOf(createdAt));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save request to Oracle.", exception);
        }

        return findById(nextId)
                .orElseThrow(() -> new IllegalStateException("Saved request cannot be read from Oracle. id=" + nextId));
    }

    @Override
    public Request updateStatus(Long requestId, RequestStatus newStatus) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS_SQL)) {
            statement.setString(1, newStatus.name());
            statement.setLong(2, requestId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Request not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update request status in Oracle.", exception);
        }

        return findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found."));
    }

    @Override
    public Optional<Request> findById(Long requestId) {
        List<Request> requests = executeListQuery(FIND_BY_ID_SQL, statement -> statement.setLong(1, requestId));
        return requests.stream().findFirst();
    }

    @Override
    public List<Request> findByStudentId(Long studentId) {
        return executeListQuery(FIND_BY_STUDENT_SQL, statement -> statement.setLong(1, studentId));
    }

    @Override
    public List<Request> findByStatus(RequestStatus status) {
        return executeListQuery(FIND_BY_STATUS_SQL, statement -> statement.setString(1, status.name()));
    }

    @Override
    public List<Request> findByType(RequestType type) {
        return executeListQuery(FIND_BY_TYPE_SQL, statement -> statement.setString(1, type.name()));
    }

    @Override
    public List<Request> findPending() {
        return executeListQuery(FIND_PENDING_SQL, statement -> {
        });
    }

    @Override
    public List<Request> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    private List<Request> executeListQuery(String sql, SqlParameterSetter setter) {
        List<Request> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setter.set(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapRequest(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query requests from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle request query returned rows: " + result.size());
        return result;
    }

    private Long getNextRequestId() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(NEXT_ID_SQL)) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Cannot generate next request id.");
            }
            return resultSet.getLong(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to generate next request id from Oracle.", exception);
        }
    }

    private Request mapRequest(ResultSet resultSet) throws SQLException {
        return new Request(
                resultSet.getLong("application_id"),
                mapStudent(resultSet),
                toRequestType(resultSet.getString("application_type")),
                resultSet.getString("content"),
                toRequestStatus(resultSet.getString("status")),
                toLocalDateTime(resultSet.getTimestamp("created_at"))
        );
    }

    private Student mapStudent(ResultSet resultSet) throws SQLException {
        Student student = new Student(
                resultSet.getLong("student_id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getString("student_number"),
                resultSet.getString("field_of_study"),
                toSemester(resultSet.getString("semester"))
        );
        student.setActive(toActive(resultSet.getString("active_flag")));
        return student;
    }

    private RequestType toRequestType(String value) {
        return RequestType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private RequestStatus toRequestStatus(String value) {
        return RequestStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private Semester toSemester(String value) {
        if (value == null || value.isBlank()) {
            return Semester.FIRST;
        }
        return Semester.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return LocalDateTime.now();
        }
        return timestamp.toLocalDateTime();
    }

    private boolean toActive(String flag) {
        return "Y".equalsIgnoreCase(flag);
    }

    @FunctionalInterface
    private interface SqlParameterSetter {
        void set(PreparedStatement statement) throws SQLException;
    }
}

