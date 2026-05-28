package pl.usos2.server.dao.serviceticket;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.enumtype.ServiceTicketStatus;
import pl.usos2.server.model.service.ServiceTicket;
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
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public class JdbcServiceTicketDao implements ServiceTicketDao {
    private static final Logger logger = Logger.getLogger(JdbcServiceTicketDao.class.getName());

    private static final String BASE_SELECT = """
            SELECT
                t.ticket_id,
                t.title,
                t.description,
                t.status,
                t.created_at,
                t.assigned_admin_id,
                ru.user_id AS reporter_id,
                ru.first_name AS reporter_first_name,
                ru.last_name AS reporter_last_name,
                ru.email AS reporter_email,
                ru.password_hash AS reporter_password,
                ru.active_flag AS reporter_active_flag,
                rr.role_code AS reporter_role_code,
                rs.student_number AS reporter_student_number,
                rs.field_of_study AS reporter_field_of_study,
                rs.semester AS reporter_semester,
                re.employee_number AS reporter_employee_number,
                rl.academic_title AS reporter_academic_title,
                au.user_id AS admin_user_id,
                au.first_name AS admin_first_name,
                au.last_name AS admin_last_name,
                au.email AS admin_email,
                au.password_hash AS admin_password,
                au.active_flag AS admin_active_flag,
                ae.employee_number AS admin_employee_number,
                ae.position_title AS admin_position_title,
                ae.salary AS admin_salary
            FROM service_tickets t
            JOIN users ru ON ru.user_id = t.reporter_user_id
            JOIN roles rr ON rr.role_id = ru.role_id
            LEFT JOIN students rs ON rs.student_id = ru.user_id
            LEFT JOIN employees re ON re.employee_id = ru.user_id
            LEFT JOIN lecturers rl ON rl.lecturer_id = ru.user_id
            LEFT JOIN admins a ON a.admin_id = t.assigned_admin_id
            LEFT JOIN users au ON au.user_id = a.admin_id
            LEFT JOIN employees ae ON ae.employee_id = a.admin_id
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE t.ticket_id = ?";
    private static final String FIND_BY_TITLE_SQL = BASE_SELECT + " WHERE UPPER(t.title) = UPPER(?)";
    private static final String FIND_BY_STATUS_SQL = BASE_SELECT + " WHERE t.status = ? ORDER BY t.created_at DESC";
    private static final String FIND_BY_REPORTER_SQL = BASE_SELECT + " WHERE t.reporter_user_id = ? ORDER BY t.created_at DESC";
    private static final String FIND_BY_ASSIGNED_ADMIN_SQL = BASE_SELECT + " WHERE t.assigned_admin_id = ? ORDER BY t.created_at DESC";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY t.created_at DESC";

    private static final String INSERT_SQL = """
            INSERT INTO service_tickets (ticket_id, reporter_user_id, title, description, status, created_at, assigned_admin_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_STATUS_SQL = """
            UPDATE service_tickets
            SET status = ?
            WHERE ticket_id = ?
            """;
    private static final String ASSIGN_SQL = """
            UPDATE service_tickets
            SET assigned_admin_id = ?, status = ?
            WHERE ticket_id = ?
            """;
    private static final String NEXT_ID_SQL = "SELECT NVL(MAX(ticket_id), 0) + 1 FROM service_tickets";

    @Override
    public ServiceTicket save(Long reporterUserId, String title, String description) {
        Long nextId = getNextTicketId();
        LocalDateTime createdAt = LocalDateTime.now();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, nextId);
            statement.setLong(2, reporterUserId);
            statement.setString(3, title);
            statement.setString(4, description);
            statement.setString(5, ServiceTicketStatus.OPEN.name());
            statement.setTimestamp(6, Timestamp.valueOf(createdAt));
            statement.setNull(7, Types.NUMERIC);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save service ticket to Oracle.", exception);
        }

        return findById(nextId)
                .orElseThrow(() -> new IllegalStateException("Saved ticket cannot be read from Oracle. id=" + nextId));
    }

    @Override
    public ServiceTicket updateStatus(Long ticketId, ServiceTicketStatus status) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS_SQL)) {
            statement.setString(1, status.name());
            statement.setLong(2, ticketId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Ticket not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update ticket status in Oracle.", exception);
        }

        return findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found."));
    }

    @Override
    public ServiceTicket assignToAdmin(Long ticketId, Long adminId, ServiceTicketStatus status) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(ASSIGN_SQL)) {
            statement.setLong(1, adminId);
            statement.setString(2, status.name());
            statement.setLong(3, ticketId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Ticket not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to assign ticket in Oracle.", exception);
        }

        return findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found."));
    }

    @Override
    public Optional<ServiceTicket> findById(Long ticketId) {
        List<ServiceTicket> tickets = executeListQuery(FIND_BY_ID_SQL, statement -> statement.setLong(1, ticketId));
        return tickets.stream().findFirst();
    }

    @Override
    public Optional<ServiceTicket> findByTitle(String title) {
        List<ServiceTicket> tickets = executeListQuery(FIND_BY_TITLE_SQL, statement -> statement.setString(1, title));
        return tickets.stream().findFirst();
    }

    @Override
    public List<ServiceTicket> findByStatus(ServiceTicketStatus status) {
        return executeListQuery(FIND_BY_STATUS_SQL, statement -> statement.setString(1, status.name()));
    }

    @Override
    public List<ServiceTicket> findByReporterId(Long reporterUserId) {
        return executeListQuery(FIND_BY_REPORTER_SQL, statement -> statement.setLong(1, reporterUserId));
    }

    @Override
    public List<ServiceTicket> findByAssignedAdminId(Long adminId) {
        return executeListQuery(FIND_BY_ASSIGNED_ADMIN_SQL, statement -> statement.setLong(1, adminId));
    }

    @Override
    public List<ServiceTicket> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    private List<ServiceTicket> executeListQuery(String sql, SqlParameterSetter setter) {
        List<ServiceTicket> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapTicket(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query service tickets from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle service ticket query returned rows: " + result.size());
        return result;
    }

    private Long getNextTicketId() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(NEXT_ID_SQL)) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Cannot generate next service ticket id.");
            }
            return resultSet.getLong(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to generate next service ticket id from Oracle.", exception);
        }
    }

    private ServiceTicket mapTicket(ResultSet resultSet) throws SQLException {
        return new ServiceTicket(
                resultSet.getLong("ticket_id"),
                mapReporter(resultSet),
                resultSet.getString("title"),
                resultSet.getString("description"),
                toStatus(resultSet.getString("status")),
                toLocalDateTime(resultSet.getTimestamp("created_at")),
                mapAssignedAdmin(resultSet)
        );
    }

    private User mapReporter(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("reporter_id");
        String firstName = resultSet.getString("reporter_first_name");
        String lastName = resultSet.getString("reporter_last_name");
        String email = resultSet.getString("reporter_email");
        String password = resultSet.getString("reporter_password");
        String roleCode = resultSet.getString("reporter_role_code");

        User user = switch (roleCode.toUpperCase(Locale.ROOT)) {
            case "STUDENT" -> new Student(
                    id,
                    firstName,
                    lastName,
                    email,
                    password,
                    resultSet.getString("reporter_student_number"),
                    resultSet.getString("reporter_field_of_study"),
                    toSemester(resultSet.getString("reporter_semester"))
            );
            case "LECTURER" -> new Lecturer(
                    id,
                    firstName,
                    lastName,
                    email,
                    password,
                    resultSet.getString("reporter_employee_number"),
                    resultSet.getString("reporter_academic_title")
            );
            case "ADMINISTRATOR" -> new Administrator(
                    id,
                    firstName,
                    lastName,
                    email,
                    password,
                    resultSet.getString("reporter_employee_number")
            );
            default -> throw new IllegalStateException("Unsupported reporter role_code: " + roleCode);
        };

        user.setActive("Y".equalsIgnoreCase(resultSet.getString("reporter_active_flag")));
        return user;
    }

    private Administrator mapAssignedAdmin(ResultSet resultSet) throws SQLException {
        Long adminId = resultSet.getLong("assigned_admin_id");
        if (resultSet.wasNull()) {
            return null;
        }

        Administrator administrator = new Administrator(
                adminId,
                resultSet.getString("admin_first_name"),
                resultSet.getString("admin_last_name"),
                resultSet.getString("admin_email"),
                resultSet.getString("admin_password"),
                resultSet.getString("admin_employee_number")
        );
        administrator.setActive("Y".equalsIgnoreCase(resultSet.getString("admin_active_flag")));
        if (resultSet.getString("admin_position_title") != null) {
            administrator.setPosition(resultSet.getString("admin_position_title"));
        }
        if (resultSet.getBigDecimal("admin_salary") != null) {
            administrator.setSalary(resultSet.getBigDecimal("admin_salary"));
        }
        return administrator;
    }

    private ServiceTicketStatus toStatus(String value) {
        return ServiceTicketStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
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

    @FunctionalInterface
    private interface SqlParameterSetter {
        void set(PreparedStatement statement) throws SQLException;
    }
}
