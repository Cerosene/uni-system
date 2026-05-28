package pl.usos2.server.dao.user;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.Employee;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public class JdbcUserDao implements UserDao {
    private static final Logger logger = Logger.getLogger(JdbcUserDao.class.getName());

    private static final String BASE_SELECT = """
            SELECT
                u.user_id,
                u.first_name,
                u.last_name,
                u.email,
                u.password_hash,
                u.active_flag,
                r.role_code,
                s.student_number,
                s.field_of_study,
                s.semester,
                e.employee_number,
                e.position_title,
                e.salary,
                l.academic_title
            FROM users u
            JOIN roles r ON r.role_id = u.role_id
            LEFT JOIN students s ON s.student_id = u.user_id
            LEFT JOIN employees e ON e.employee_id = u.user_id
            LEFT JOIN lecturers l ON l.lecturer_id = e.employee_id
            LEFT JOIN admins a ON a.admin_id = e.employee_id
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE u.user_id = ?";
    private static final String FIND_BY_EMAIL_SQL = BASE_SELECT + " WHERE LOWER(u.email) = LOWER(?)";
    private static final String FIND_BY_ROLE_SQL = BASE_SELECT + " WHERE UPPER(r.role_code) = UPPER(?) ORDER BY u.last_name, u.first_name";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY u.last_name, u.first_name";

    private static final String EXISTS_ID_SQL = "SELECT COUNT(*) FROM users WHERE user_id = ?";
    private static final String EXISTS_EMAIL_SQL = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";
    private static final String EXISTS_EMAIL_EXCLUDING_SQL = """
            SELECT COUNT(*)
            FROM users u
            WHERE LOWER(u.email) = LOWER(?)
              AND u.user_id <> ?
            """;

    private static final String FIND_ROLE_ID_SQL = "SELECT role_id FROM roles WHERE UPPER(role_code) = UPPER(?)";
    private static final String INSERT_USER_SQL = """
            INSERT INTO users (user_id, first_name, last_name, email, password_hash, role_id, active_flag, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)
            """;
    private static final String UPDATE_BASIC_SQL = """
            UPDATE users
            SET first_name = ?, last_name = ?, email = ?
            WHERE user_id = ?
            """;
    private static final String UPDATE_PASSWORD_SQL = "UPDATE users SET password_hash = ? WHERE user_id = ?";
    private static final String UPDATE_ACTIVE_SQL = "UPDATE users SET active_flag = ? WHERE user_id = ?";
    private static final String UPDATE_ROLE_SQL = "UPDATE users SET role_id = ? WHERE user_id = ?";

    private static final String INSERT_STUDENT_SQL = """
            INSERT INTO students (student_id, student_number, field_of_study, semester)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE_STUDENT_SQL = """
            UPDATE students
            SET student_number = ?, field_of_study = ?, semester = ?
            WHERE student_id = ?
            """;
    private static final String DELETE_STUDENT_SQL = "DELETE FROM students WHERE student_id = ?";

    private static final String INSERT_EMPLOYEE_SQL = """
            INSERT INTO employees (employee_id, employee_number, position_title, salary)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE_EMPLOYEE_SQL = """
            UPDATE employees
            SET employee_number = ?, position_title = ?, salary = ?
            WHERE employee_id = ?
            """;
    private static final String DELETE_EMPLOYEE_SQL = "DELETE FROM employees WHERE employee_id = ?";

    private static final String INSERT_LECTURER_SQL = "INSERT INTO lecturers (lecturer_id, academic_title) VALUES (?, ?)";
    private static final String UPDATE_LECTURER_SQL = "UPDATE lecturers SET academic_title = ? WHERE lecturer_id = ?";
    private static final String DELETE_LECTURER_SQL = "DELETE FROM lecturers WHERE lecturer_id = ?";

    private static final String INSERT_ADMIN_SQL = "INSERT INTO admins (admin_id) VALUES (?)";
    private static final String DELETE_ADMIN_SQL = "DELETE FROM admins WHERE admin_id = ?";

    @Override
    public User save(User user) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Long roleId = findRoleId(connection, user.getRole());

                try (PreparedStatement statement = connection.prepareStatement(INSERT_USER_SQL)) {
                    statement.setLong(1, user.getId());
                    statement.setString(2, user.getFirstName());
                    statement.setString(3, user.getLastName());
                    statement.setString(4, user.getEmail());
                    statement.setString(5, user.getPassword());
                    statement.setLong(6, roleId);
                    statement.setString(7, toActiveFlag(user.isActive()));
                    statement.executeUpdate();
                }

                syncRoleSpecificTables(connection, user, user.getRole());
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save user to Oracle.", exception);
        }

        User saved = findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("Saved user cannot be read from Oracle. id=" + user.getId()));
        logger.info("[DIAGNOSTIC] User saved in Oracle. userId=" + saved.getId());
        return saved;
    }

    @Override
    public User updateBasicData(Long userId, String firstName, String lastName, String email) {
        executeSingleUpdate(UPDATE_BASIC_SQL, statement -> {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, email);
            statement.setLong(4, userId);
        }, "Failed to update user basic data in Oracle.");

        User updated = findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        logger.info("[DIAGNOSTIC] User basic data updated in Oracle. userId=" + userId);
        return updated;
    }

    @Override
    public User updatePassword(Long userId, String newPassword) {
        executeSingleUpdate(UPDATE_PASSWORD_SQL, statement -> {
            statement.setString(1, newPassword);
            statement.setLong(2, userId);
        }, "Failed to update user password in Oracle.");

        User updated = findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        logger.info("[DIAGNOSTIC] User password updated in Oracle. userId=" + userId);
        return updated;
    }

    @Override
    public User updateActive(Long userId, boolean active) {
        executeSingleUpdate(UPDATE_ACTIVE_SQL, statement -> {
            statement.setString(1, toActiveFlag(active));
            statement.setLong(2, userId);
        }, "Failed to update user active flag in Oracle.");

        User updated = findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        logger.info("[DIAGNOSTIC] User active flag updated in Oracle. userId=" + userId + ", active=" + active);
        return updated;
    }

    @Override
    public User updateRole(Long userId, UserRole role) {
        User current = findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Long roleId = findRoleId(connection, role);
                try (PreparedStatement statement = connection.prepareStatement(UPDATE_ROLE_SQL)) {
                    statement.setLong(1, roleId);
                    statement.setLong(2, userId);
                    int updated = statement.executeUpdate();
                    if (updated == 0) {
                        throw new IllegalArgumentException("User not found.");
                    }
                }

                syncRoleSpecificTables(connection, current, role);
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update user role in Oracle.", exception);
        }

        User updated = findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        logger.info("[DIAGNOSTIC] User role updated in Oracle. userId=" + userId + ", role=" + role);
        return updated;
    }

    @Override
    public void deleteById(Long userId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                deleteById(connection, DELETE_ADMIN_SQL, userId);
                deleteById(connection, DELETE_LECTURER_SQL, userId);
                deleteById(connection, DELETE_STUDENT_SQL, userId);
                deleteById(connection, DELETE_EMPLOYEE_SQL, userId);

                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                    statement.setLong(1, userId);
                    int deleted = statement.executeUpdate();
                    if (deleted == 0) {
                        throw new IllegalArgumentException("User not found.");
                    }
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("ORA-02292")) {
                throw new IllegalStateException("Cannot delete user with related records.");
            }
            throw new IllegalStateException("Failed to delete user from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] User deleted from Oracle. userId=" + userId);
    }

    @Override
    public Optional<User> findById(Long userId) {
        List<User> users = executeListQuery(FIND_BY_ID_SQL, statement -> statement.setLong(1, userId));
        return users.stream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        List<User> users = executeListQuery(FIND_BY_EMAIL_SQL, statement -> statement.setString(1, email));
        return users.stream().findFirst();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return executeListQuery(FIND_BY_ROLE_SQL, statement -> statement.setString(1, role.name()));
    }

    @Override
    public List<User> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    @Override
    public boolean existsById(Long userId) {
        return existsByQuery(EXISTS_ID_SQL, statement -> statement.setLong(1, userId));
    }

    @Override
    public boolean existsByEmail(String email) {
        return existsByQuery(EXISTS_EMAIL_SQL, statement -> statement.setString(1, email));
    }

    @Override
    public boolean existsByEmailExcludingId(String email, Long excludedUserId) {
        return existsByQuery(EXISTS_EMAIL_EXCLUDING_SQL, statement -> {
            statement.setString(1, email);
            statement.setLong(2, excludedUserId);
        });
    }

    private List<User> executeListQuery(String sql, SqlParameterSetter setter) {
        List<User> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapUser(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query users from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle user query returned rows: " + result.size());
        return result;
    }

    private boolean existsByQuery(String sql, SqlParameterSetter setter) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to execute user existence query in Oracle.", exception);
        }
    }

    private void executeSingleUpdate(String sql, SqlParameterSetter setter, String errorMessage) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("User not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
    }

    private void syncRoleSpecificTables(Connection connection, User sourceUser, UserRole targetRole) throws SQLException {
        if (targetRole == UserRole.STUDENT) {
            deleteById(connection, DELETE_EMPLOYEE_SQL, sourceUser.getId());
            ensureStudentRow(connection, toStudent(sourceUser));
            return;
        }

        deleteById(connection, DELETE_STUDENT_SQL, sourceUser.getId());
        Employee employee = toEmployeeForRole(sourceUser, targetRole);
        ensureEmployeeRow(connection, employee);

        if (targetRole == UserRole.LECTURER) {
            ensureLecturerRow(connection, sourceUser.getId(), sourceUser instanceof Lecturer lecturer
                    ? safeText(lecturer.getAcademicTitle(), "Dr.")
                    : "Dr.");
            deleteById(connection, DELETE_ADMIN_SQL, sourceUser.getId());
            return;
        }

        ensureAdminRow(connection, sourceUser.getId());
        deleteById(connection, DELETE_LECTURER_SQL, sourceUser.getId());
    }

    private void ensureStudentRow(Connection connection, Student student) throws SQLException {
        if (rowExists(connection, "students", "student_id", student.getId())) {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_STUDENT_SQL)) {
                statement.setString(1, student.getStudentNumber());
                statement.setString(2, student.getFieldOfStudy());
                statement.setString(3, student.getSemester().name());
                statement.setLong(4, student.getId());
                statement.executeUpdate();
            }
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(INSERT_STUDENT_SQL)) {
            statement.setLong(1, student.getId());
            statement.setString(2, student.getStudentNumber());
            statement.setString(3, student.getFieldOfStudy());
            statement.setString(4, student.getSemester().name());
            statement.executeUpdate();
        }
    }

    private void ensureEmployeeRow(Connection connection, Employee employee) throws SQLException {
        if (rowExists(connection, "employees", "employee_id", employee.getId())) {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_EMPLOYEE_SQL)) {
                statement.setString(1, employee.getEmployeeNumber());
                statement.setString(2, employee.getPosition());
                statement.setBigDecimal(3, employee.getSalary());
                statement.setLong(4, employee.getId());
                statement.executeUpdate();
            }
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(INSERT_EMPLOYEE_SQL)) {
            statement.setLong(1, employee.getId());
            statement.setString(2, employee.getEmployeeNumber());
            statement.setString(3, employee.getPosition());
            statement.setBigDecimal(4, employee.getSalary());
            statement.executeUpdate();
        }
    }

    private void ensureLecturerRow(Connection connection, Long userId, String academicTitle) throws SQLException {
        if (rowExists(connection, "lecturers", "lecturer_id", userId)) {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_LECTURER_SQL)) {
                statement.setString(1, academicTitle);
                statement.setLong(2, userId);
                statement.executeUpdate();
            }
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(INSERT_LECTURER_SQL)) {
            statement.setLong(1, userId);
            statement.setString(2, academicTitle);
            statement.executeUpdate();
        }
    }

    private void ensureAdminRow(Connection connection, Long userId) throws SQLException {
        if (rowExists(connection, "admins", "admin_id", userId)) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(INSERT_ADMIN_SQL)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    private void deleteById(Connection connection, String sql, Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private boolean rowExists(Connection connection, String tableName, String idColumn, Long id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return resultSet.getInt(1) > 0;
            }
        }
    }

    private Long findRoleId(Connection connection, UserRole role) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_ROLE_ID_SQL)) {
            statement.setString(1, role.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Role not found in Oracle: " + role.name());
                }
                return resultSet.getLong("role_id");
            }
        }
    }

    private Student toStudent(User user) {
        if (user instanceof Student student) {
            String studentNumber = safeText(student.getStudentNumber(), "STU" + user.getId());
            String field = safeText(student.getFieldOfStudy(), "Informatyka");
            Semester semester = student.getSemester() == null ? Semester.FIRST : student.getSemester();
            Student normalized = new Student(
                    student.getId(),
                    student.getFirstName(),
                    student.getLastName(),
                    student.getEmail(),
                    student.getPassword(),
                    studentNumber,
                    field,
                    semester
            );
            normalized.setActive(student.isActive());
            return normalized;
        }

        Student converted = new Student(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                "STU" + user.getId(),
                "Informatyka",
                Semester.FIRST
        );
        converted.setActive(user.isActive());
        return converted;
    }

    private Employee toEmployeeForRole(User user, UserRole role) {
        String employeeNumber = "EMP" + user.getId();
        String position = role == UserRole.ADMINISTRATOR ? "Administrator" : "Lecturer";
        BigDecimal salary = role == UserRole.ADMINISTRATOR ? new BigDecimal("6000.00") : new BigDecimal("7000.00");

        if (user instanceof Employee employee) {
            employeeNumber = safeText(employee.getEmployeeNumber(), employeeNumber);
            position = safeText(employee.getPosition(), position);
            if (employee.getSalary() != null && employee.getSalary().compareTo(BigDecimal.ZERO) > 0) {
                salary = employee.getSalary();
            }
        }

        Employee converted = new Employee(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                employeeNumber,
                position,
                salary,
                role
        );
        converted.setActive(user.isActive());
        return converted;
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        String roleCode = resultSet.getString("role_code");
        Long id = resultSet.getLong("user_id");

        User user = switch (roleCode.toUpperCase(Locale.ROOT)) {
            case "STUDENT" -> new Student(
                    id,
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("email"),
                    resultSet.getString("password_hash"),
                    safeText(resultSet.getString("student_number"), "STU" + id),
                    safeText(resultSet.getString("field_of_study"), "Informatyka"),
                    toSemester(resultSet.getString("semester"))
            );
            case "LECTURER" -> {
                Lecturer lecturer = new Lecturer(
                        id,
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        safeText(resultSet.getString("employee_number"), "EMP" + id),
                        safeText(resultSet.getString("academic_title"), "Dr.")
                );
                String positionTitle = resultSet.getString("position_title");
                BigDecimal salary = resultSet.getBigDecimal("salary");
                if (positionTitle != null && !positionTitle.isBlank()) {
                    lecturer.setPosition(positionTitle);
                }
                if (salary != null && salary.compareTo(BigDecimal.ZERO) > 0) {
                    lecturer.setSalary(salary);
                }
                yield lecturer;
            }
            case "ADMINISTRATOR" -> {
                Administrator administrator = new Administrator(
                        id,
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        safeText(resultSet.getString("employee_number"), "EMP" + id)
                );
                String positionTitle = resultSet.getString("position_title");
                BigDecimal salary = resultSet.getBigDecimal("salary");
                if (positionTitle != null && !positionTitle.isBlank()) {
                    administrator.setPosition(positionTitle);
                }
                if (salary != null && salary.compareTo(BigDecimal.ZERO) > 0) {
                    administrator.setSalary(salary);
                }
                yield administrator;
            }
            default -> throw new IllegalStateException("Unsupported role_code in database: " + roleCode);
        };

        user.setActive(toActive(resultSet.getString("active_flag")));
        return user;
    }

    private boolean toActive(String activeFlag) {
        return "Y".equalsIgnoreCase(activeFlag);
    }

    private Semester toSemester(String semesterValue) {
        if (semesterValue == null || semesterValue.isBlank()) {
            return Semester.FIRST;
        }
        return Semester.valueOf(semesterValue.trim().toUpperCase(Locale.ROOT));
    }

    private String toActiveFlag(boolean active) {
        return active ? "Y" : "N";
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    @FunctionalInterface
    private interface SqlParameterSetter {
        void set(PreparedStatement statement) throws SQLException;
    }
}
