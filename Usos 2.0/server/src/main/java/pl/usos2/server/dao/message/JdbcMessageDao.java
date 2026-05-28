package pl.usos2.server.dao.message;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.enumtype.MessageStatus;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.request.Message;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;

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

public class JdbcMessageDao implements MessageDao {
    private static final Logger logger = Logger.getLogger(JdbcMessageDao.class.getName());

    private static final String BASE_SELECT = """
            SELECT
                m.message_id,
                m.subject,
                m.content,
                m.sent_at,
                m.status,
                su.user_id AS sender_id,
                su.first_name AS sender_first_name,
                su.last_name AS sender_last_name,
                su.email AS sender_email,
                su.password_hash AS sender_password,
                su.active_flag AS sender_active_flag,
                sr.role_code AS sender_role_code,
                ss.student_number AS sender_student_number,
                ss.field_of_study AS sender_field_of_study,
                ss.semester AS sender_semester,
                se.employee_number AS sender_employee_number,
                se.position_title AS sender_position_title,
                se.salary AS sender_salary,
                sl.academic_title AS sender_academic_title,
                ru.user_id AS recipient_id,
                ru.first_name AS recipient_first_name,
                ru.last_name AS recipient_last_name,
                ru.email AS recipient_email,
                ru.password_hash AS recipient_password,
                ru.active_flag AS recipient_active_flag,
                rr.role_code AS recipient_role_code,
                rs.student_number AS recipient_student_number,
                rs.field_of_study AS recipient_field_of_study,
                rs.semester AS recipient_semester,
                re.employee_number AS recipient_employee_number,
                re.position_title AS recipient_position_title,
                re.salary AS recipient_salary,
                rl.academic_title AS recipient_academic_title
            FROM messages m
            JOIN users su ON su.user_id = m.sender_user_id
            JOIN roles sr ON sr.role_id = su.role_id
            LEFT JOIN students ss ON ss.student_id = su.user_id
            LEFT JOIN employees se ON se.employee_id = su.user_id
            LEFT JOIN lecturers sl ON sl.lecturer_id = su.user_id
            JOIN users ru ON ru.user_id = m.recipient_user_id
            JOIN roles rr ON rr.role_id = ru.role_id
            LEFT JOIN students rs ON rs.student_id = ru.user_id
            LEFT JOIN employees re ON re.employee_id = ru.user_id
            LEFT JOIN lecturers rl ON rl.lecturer_id = ru.user_id
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE m.message_id = ?";
    private static final String FIND_INBOX_SQL = BASE_SELECT + " WHERE m.recipient_user_id = ? ORDER BY m.sent_at DESC";
    private static final String FIND_UNREAD_INBOX_SQL = BASE_SELECT
            + " WHERE m.recipient_user_id = ? AND m.status = 'SENT' ORDER BY m.sent_at DESC";
    private static final String FIND_SENT_SQL = BASE_SELECT + " WHERE m.sender_user_id = ? ORDER BY m.sent_at DESC";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY m.sent_at DESC";

    private static final String INSERT_SQL = """
            INSERT INTO messages (message_id, sender_user_id, recipient_user_id, subject, content, sent_at, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_READ_SQL = """
            UPDATE messages
            SET status = 'READ'
            WHERE message_id = ?
            """;
    private static final String NEXT_ID_SQL = "SELECT NVL(MAX(message_id), 0) + 1 FROM messages";

    @Override
    public Message save(Long senderId, Long recipientId, String subject, String content) {
        Long nextId = getNextMessageId();
        LocalDateTime sentAt = LocalDateTime.now();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, nextId);
            statement.setLong(2, senderId);
            statement.setLong(3, recipientId);
            statement.setString(4, subject);
            statement.setString(5, content);
            statement.setTimestamp(6, Timestamp.valueOf(sentAt));
            statement.setString(7, MessageStatus.SENT.name());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save message to Oracle.", exception);
        }

        return findById(nextId)
                .orElseThrow(() -> new IllegalStateException("Saved message cannot be read from Oracle. id=" + nextId));
    }

    @Override
    public Message markAsRead(Long messageId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_READ_SQL)) {
            statement.setLong(1, messageId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Message not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to mark message as READ in Oracle.", exception);
        }

        return findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found."));
    }

    @Override
    public Optional<Message> findById(Long messageId) {
        List<Message> messages = executeListQuery(FIND_BY_ID_SQL, statement -> statement.setLong(1, messageId));
        return messages.stream().findFirst();
    }

    @Override
    public List<Message> findInboxByRecipientId(Long recipientId) {
        return executeListQuery(FIND_INBOX_SQL, statement -> statement.setLong(1, recipientId));
    }

    @Override
    public List<Message> findUnreadInboxByRecipientId(Long recipientId) {
        return executeListQuery(FIND_UNREAD_INBOX_SQL, statement -> statement.setLong(1, recipientId));
    }

    @Override
    public List<Message> findSentBySenderId(Long senderId) {
        return executeListQuery(FIND_SENT_SQL, statement -> statement.setLong(1, senderId));
    }

    @Override
    public List<Message> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    private List<Message> executeListQuery(String sql, SqlParameterSetter parameterSetter) {
        List<Message> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            parameterSetter.set(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapMessage(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query messages from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle message query returned rows: " + result.size());
        return result;
    }

    private Long getNextMessageId() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(NEXT_ID_SQL)) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Cannot generate next message id.");
            }
            return resultSet.getLong(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to generate next message id from Oracle.", exception);
        }
    }

    private Message mapMessage(ResultSet resultSet) throws SQLException {
        return new Message(
                resultSet.getLong("message_id"),
                mapUser(resultSet, "sender"),
                mapUser(resultSet, "recipient"),
                resultSet.getString("subject"),
                resultSet.getString("content"),
                toLocalDateTime(resultSet.getTimestamp("sent_at")),
                toMessageStatus(resultSet.getString("status"))
        );
    }

    private User mapUser(ResultSet resultSet, String prefix) throws SQLException {
        Long id = resultSet.getLong(prefix + "_id");
        String firstName = resultSet.getString(prefix + "_first_name");
        String lastName = resultSet.getString(prefix + "_last_name");
        String email = resultSet.getString(prefix + "_email");
        String password = resultSet.getString(prefix + "_password");
        String roleCode = resultSet.getString(prefix + "_role_code");

        User user = switch (roleCode.toUpperCase(Locale.ROOT)) {
            case "STUDENT" -> new Student(
                    id,
                    firstName,
                    lastName,
                    email,
                    password,
                    resultSet.getString(prefix + "_student_number"),
                    resultSet.getString(prefix + "_field_of_study"),
                    toSemester(resultSet.getString(prefix + "_semester"))
            );
            case "LECTURER" -> {
                Lecturer lecturer = new Lecturer(
                        id,
                        firstName,
                        lastName,
                        email,
                        password,
                        resultSet.getString(prefix + "_employee_number"),
                        resultSet.getString(prefix + "_academic_title")
                );
                lecturer.setPosition(resultSet.getString(prefix + "_position_title"));
                lecturer.setSalary(resultSet.getBigDecimal(prefix + "_salary"));
                yield lecturer;
            }
            case "ADMINISTRATOR" -> {
                Administrator administrator = new Administrator(
                        id,
                        firstName,
                        lastName,
                        email,
                        password,
                        resultSet.getString(prefix + "_employee_number")
                );
                administrator.setPosition(resultSet.getString(prefix + "_position_title"));
                administrator.setSalary(resultSet.getBigDecimal(prefix + "_salary"));
                yield administrator;
            }
            default -> throw new IllegalStateException("Unsupported role_code in messages mapping: " + roleCode);
        };

        user.setActive(toActive(resultSet.getString(prefix + "_active_flag")));
        return user;
    }

    private MessageStatus toMessageStatus(String value) {
        if (value == null || value.isBlank()) {
            return MessageStatus.SENT;
        }
        return MessageStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
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

