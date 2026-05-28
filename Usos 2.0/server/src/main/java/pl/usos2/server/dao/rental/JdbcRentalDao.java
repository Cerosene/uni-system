package pl.usos2.server.dao.rental;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.rental.Rental;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public class JdbcRentalDao implements RentalDao {
    private static final Logger logger = Logger.getLogger(JdbcRentalDao.class.getName());

    private static final String BASE_SELECT = """
            SELECT
                l.loan_id,
                l.loan_date,
                l.due_date,
                l.returned_flag,
                r.resource_name,
                u.user_id AS borrower_id,
                u.first_name AS borrower_first_name,
                u.last_name AS borrower_last_name,
                u.email AS borrower_email,
                u.password_hash AS borrower_password,
                u.active_flag AS borrower_active_flag,
                ro.role_code AS borrower_role_code,
                s.student_number AS borrower_student_number,
                s.field_of_study AS borrower_field_of_study,
                s.semester AS borrower_semester,
                e.employee_number AS borrower_employee_number,
                lec.academic_title AS borrower_academic_title
            FROM loans l
            JOIN resources r ON r.resource_id = l.resource_id
            JOIN users u ON u.user_id = l.borrower_user_id
            JOIN roles ro ON ro.role_id = u.role_id
            LEFT JOIN students s ON s.student_id = u.user_id
            LEFT JOIN employees e ON e.employee_id = u.user_id
            LEFT JOIN lecturers lec ON lec.lecturer_id = u.user_id
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE l.loan_id = ?";
    private static final String FIND_BY_BORROWER_SQL = BASE_SELECT + " WHERE l.borrower_user_id = ? ORDER BY l.loan_date DESC";
    private static final String FIND_BY_RESOURCE_SQL = BASE_SELECT + " WHERE UPPER(r.resource_name) = UPPER(?) ORDER BY l.loan_date DESC";
    private static final String FIND_ACTIVE_SQL = BASE_SELECT + " WHERE l.returned_flag = 'N' ORDER BY l.loan_date DESC";
    private static final String FIND_RETURNED_SQL = BASE_SELECT + " WHERE l.returned_flag = 'Y' ORDER BY l.loan_date DESC";
    private static final String FIND_OVERDUE_SQL = BASE_SELECT
            + " WHERE l.returned_flag = 'N' AND l.due_date < ? ORDER BY l.due_date, l.loan_date";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY l.loan_date DESC";

    private static final String INSERT_SQL = """
            INSERT INTO loans (loan_id, resource_id, borrower_user_id, loan_date, due_date, return_date, returned_flag)
            VALUES (?, ?, ?, ?, ?, NULL, 'N')
            """;
    private static final String MARK_RETURNED_SQL = """
            UPDATE loans
            SET returned_flag = 'Y',
                return_date = ?
            WHERE loan_id = ?
            """;
    private static final String EXTEND_DUE_DATE_SQL = """
            UPDATE loans
            SET due_date = ?
            WHERE loan_id = ?
            """;

    private static final String EXISTS_ID_SQL = "SELECT COUNT(*) FROM loans WHERE loan_id = ?";
    private static final String RESOURCE_RENTED_SQL = """
            SELECT COUNT(*)
            FROM loans l
            JOIN resources r ON r.resource_id = l.resource_id
            WHERE UPPER(r.resource_name) = UPPER(?)
              AND l.returned_flag = 'N'
            """;
    private static final String FIND_RESOURCE_ID_SQL = """
            SELECT resource_id
            FROM resources
            WHERE UPPER(resource_name) = UPPER(?)
            """;
    private static final String AVAILABLE_RESOURCES_SQL = """
            SELECT r.resource_name
            FROM resources r
            WHERE r.available_flag = 'Y'
              AND NOT EXISTS (
                SELECT 1
                FROM loans l
                WHERE l.resource_id = r.resource_id
                  AND l.returned_flag = 'N'
              )
            ORDER BY r.resource_name
            """;

    @Override
    public Rental save(Long id, Long borrowerId, String resourceName, LocalDate rentalDate, LocalDate dueDate) {
        Long resourceId = findResourceIdByName(resourceName)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, id);
            statement.setLong(2, resourceId);
            statement.setLong(3, borrowerId);
            statement.setDate(4, Date.valueOf(rentalDate));
            statement.setDate(5, Date.valueOf(dueDate));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save rental to Oracle.", exception);
        }

        return findById(id)
                .orElseThrow(() -> new IllegalStateException("Saved rental cannot be read from Oracle. id=" + id));
    }

    @Override
    public Rental markAsReturned(Long rentalId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(MARK_RETURNED_SQL)) {
            statement.setDate(1, Date.valueOf(LocalDate.now()));
            statement.setLong(2, rentalId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Rental not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to mark rental as returned in Oracle.", exception);
        }

        return findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found."));
    }

    @Override
    public Rental extendDueDate(Long rentalId, LocalDate newDueDate) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXTEND_DUE_DATE_SQL)) {
            statement.setDate(1, Date.valueOf(newDueDate));
            statement.setLong(2, rentalId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Rental not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to extend rental due date in Oracle.", exception);
        }

        return findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found."));
    }

    @Override
    public Optional<Rental> findById(Long rentalId) {
        List<Rental> rentals = executeListQuery(FIND_BY_ID_SQL, statement -> statement.setLong(1, rentalId));
        return rentals.stream().findFirst();
    }

    @Override
    public List<Rental> findByBorrowerId(Long borrowerId) {
        return executeListQuery(FIND_BY_BORROWER_SQL, statement -> statement.setLong(1, borrowerId));
    }

    @Override
    public List<Rental> findByResourceName(String resourceName) {
        return executeListQuery(FIND_BY_RESOURCE_SQL, statement -> statement.setString(1, resourceName));
    }

    @Override
    public List<Rental> findActive() {
        return executeListQuery(FIND_ACTIVE_SQL, statement -> {
        });
    }

    @Override
    public List<Rental> findReturned() {
        return executeListQuery(FIND_RETURNED_SQL, statement -> {
        });
    }

    @Override
    public List<Rental> findOverdue(LocalDate currentDate) {
        return executeListQuery(FIND_OVERDUE_SQL, statement -> statement.setDate(1, Date.valueOf(currentDate)));
    }

    @Override
    public List<Rental> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    @Override
    public List<String> findAvailableResourceNames() {
        List<String> resources = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(AVAILABLE_RESOURCES_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                resources.add(resultSet.getString("resource_name"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query available resources from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle available resources query returned rows: " + resources.size());
        return resources;
    }

    @Override
    public boolean existsById(Long rentalId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_ID_SQL)) {
            statement.setLong(1, rentalId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to check rental id existence in Oracle.", exception);
        }
    }

    @Override
    public boolean isResourceCurrentlyRented(String resourceName) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(RESOURCE_RENTED_SQL)) {
            statement.setString(1, resourceName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to check resource rental state in Oracle.", exception);
        }
    }

    private Optional<Long> findResourceIdByName(String resourceName) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_RESOURCE_ID_SQL)) {
            statement.setString(1, resourceName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(resultSet.getLong("resource_id"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to resolve resource id in Oracle.", exception);
        }
    }

    private List<Rental> executeListQuery(String sql, SqlParameterSetter setter) {
        List<Rental> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapRental(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query rentals from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle rental query returned rows: " + result.size());
        return result;
    }

    private Rental mapRental(ResultSet resultSet) throws SQLException {
        return new Rental(
                resultSet.getLong("loan_id"),
                mapBorrower(resultSet),
                resultSet.getString("resource_name"),
                resultSet.getDate("loan_date").toLocalDate(),
                resultSet.getDate("due_date").toLocalDate(),
                "Y".equalsIgnoreCase(resultSet.getString("returned_flag"))
        );
    }

    private User mapBorrower(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("borrower_id");
        String firstName = resultSet.getString("borrower_first_name");
        String lastName = resultSet.getString("borrower_last_name");
        String email = resultSet.getString("borrower_email");
        String password = resultSet.getString("borrower_password");
        String roleCode = resultSet.getString("borrower_role_code");

        User user = switch (roleCode.toUpperCase(Locale.ROOT)) {
            case "STUDENT" -> new Student(
                    id,
                    firstName,
                    lastName,
                    email,
                    password,
                    resultSet.getString("borrower_student_number"),
                    resultSet.getString("borrower_field_of_study"),
                    toSemester(resultSet.getString("borrower_semester"))
            );
            case "LECTURER" -> new Lecturer(
                    id,
                    firstName,
                    lastName,
                    email,
                    password,
                    resultSet.getString("borrower_employee_number"),
                    resultSet.getString("borrower_academic_title")
            );
            case "ADMINISTRATOR" -> new Administrator(
                    id,
                    firstName,
                    lastName,
                    email,
                    password,
                    resultSet.getString("borrower_employee_number")
            );
            default -> throw new IllegalStateException("Unsupported borrower role_code: " + roleCode);
        };

        user.setActive("Y".equalsIgnoreCase(resultSet.getString("borrower_active_flag")));
        return user;
    }

    private Semester toSemester(String value) {
        if (value == null || value.isBlank()) {
            return Semester.FIRST;
        }
        return Semester.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @FunctionalInterface
    private interface SqlParameterSetter {
        void set(PreparedStatement statement) throws SQLException;
    }
}
