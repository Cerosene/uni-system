package pl.usos2.server.dao.employee;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Employee;

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

public class JdbcEmployeeDao implements EmployeeDao {
    private static final Logger logger = Logger.getLogger(JdbcEmployeeDao.class.getName());

    private static final String BASE_SELECT = """
            SELECT
                e.employee_id,
                e.employee_number,
                e.position_title,
                e.salary,
                u.first_name,
                u.last_name,
                u.email,
                u.password_hash,
                u.active_flag,
                r.role_code
            FROM employees e
            JOIN users u ON u.user_id = e.employee_id
            JOIN roles r ON r.role_id = u.role_id
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE e.employee_id = ?";
    private static final String FIND_BY_EMAIL_SQL = BASE_SELECT + " WHERE LOWER(u.email) = LOWER(?)";
    private static final String FIND_BY_NUMBER_SQL = BASE_SELECT + " WHERE UPPER(e.employee_number) = UPPER(?)";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY u.last_name, u.first_name";
    private static final String FIND_ACTIVE_SQL = BASE_SELECT + " WHERE u.active_flag = 'Y' ORDER BY u.last_name, u.first_name";

    private static final String EXISTS_ID_SQL = "SELECT COUNT(*) FROM employees WHERE employee_id = ?";
    private static final String EXISTS_EMAIL_SQL = """
            SELECT COUNT(*)
            FROM employees e
            JOIN users u ON u.user_id = e.employee_id
            WHERE LOWER(u.email) = LOWER(?)
            """;
    private static final String EXISTS_EMAIL_EXCLUDING_SQL = """
            SELECT COUNT(*)
            FROM employees e
            JOIN users u ON u.user_id = e.employee_id
            WHERE LOWER(u.email) = LOWER(?)
              AND e.employee_id <> ?
            """;
    private static final String EXISTS_NUMBER_SQL = "SELECT COUNT(*) FROM employees WHERE UPPER(employee_number) = UPPER(?)";
    private static final String EXISTS_NUMBER_EXCLUDING_SQL = """
            SELECT COUNT(*)
            FROM employees
            WHERE UPPER(employee_number) = UPPER(?)
              AND employee_id <> ?
            """;

    private static final String USER_EXISTS_SQL = "SELECT COUNT(*) FROM users WHERE user_id = ?";
    private static final String FIND_ROLE_ID_SQL = "SELECT role_id FROM roles WHERE UPPER(role_code) = UPPER(?)";

    private static final String INSERT_USER_SQL = """
            INSERT INTO users (user_id, first_name, last_name, email, password_hash, role_id, active_flag, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)
            """;
    private static final String UPDATE_USER_SQL = """
            UPDATE users
            SET first_name = ?, last_name = ?, email = ?, password_hash = ?, role_id = ?, active_flag = ?
            WHERE user_id = ?
            """;
    private static final String INSERT_EMPLOYEE_SQL = """
            INSERT INTO employees (employee_id, employee_number, position_title, salary)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_BASIC_SQL = """
            UPDATE users
            SET first_name = ?, last_name = ?, email = ?
            WHERE user_id = ?
            """;
    private static final String UPDATE_POSITION_SQL = "UPDATE employees SET position_title = ? WHERE employee_id = ?";
    private static final String UPDATE_SALARY_SQL = "UPDATE employees SET salary = ? WHERE employee_id = ?";
    private static final String UPDATE_NUMBER_SQL = "UPDATE employees SET employee_number = ? WHERE employee_id = ?";
    private static final String UPDATE_ACTIVE_SQL = "UPDATE users SET active_flag = ? WHERE user_id = ?";
    private static final String DELETE_EMPLOYEE_SQL = "DELETE FROM users WHERE user_id = ?";

    @Override
    public Employee save(Employee employee) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Long roleId = findRoleId(connection, employee.getRole());

                if (isUserPresent(connection, employee.getId())) {
                    try (PreparedStatement statement = connection.prepareStatement(UPDATE_USER_SQL)) {
                        statement.setString(1, employee.getFirstName());
                        statement.setString(2, employee.getLastName());
                        statement.setString(3, employee.getEmail());
                        statement.setString(4, employee.getPassword());
                        statement.setLong(5, roleId);
                        statement.setString(6, toActiveFlag(employee.isActive()));
                        statement.setLong(7, employee.getId());
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(INSERT_USER_SQL)) {
                        statement.setLong(1, employee.getId());
                        statement.setString(2, employee.getFirstName());
                        statement.setString(3, employee.getLastName());
                        statement.setString(4, employee.getEmail());
                        statement.setString(5, employee.getPassword());
                        statement.setLong(6, roleId);
                        statement.setString(7, toActiveFlag(employee.isActive()));
                        statement.executeUpdate();
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(INSERT_EMPLOYEE_SQL)) {
                    statement.setLong(1, employee.getId());
                    statement.setString(2, employee.getEmployeeNumber());
                    statement.setString(3, employee.getPosition());
                    statement.setBigDecimal(4, employee.getSalary());
                    statement.executeUpdate();
                }

                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save employee in Oracle.", exception);
        }

        return findById(employee.getId())
                .orElseThrow(() -> new IllegalStateException("Saved employee cannot be read from Oracle. id=" + employee.getId()));
    }

    @Override
    public Employee updateBasicData(Long employeeId, String firstName, String lastName, String email, String position) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(UPDATE_BASIC_SQL)) {
                    statement.setString(1, firstName);
                    statement.setString(2, lastName);
                    statement.setString(3, email);
                    statement.setLong(4, employeeId);
                    int updated = statement.executeUpdate();
                    if (updated == 0) {
                        throw new IllegalArgumentException("Employee not found.");
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(UPDATE_POSITION_SQL)) {
                    statement.setString(1, position);
                    statement.setLong(2, employeeId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update employee basic data in Oracle.", exception);
        }

        return findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
    }

    @Override
    public Employee updatePosition(Long employeeId, String position) {
        executeSingleUpdate(UPDATE_POSITION_SQL, statement -> {
            statement.setString(1, position);
            statement.setLong(2, employeeId);
        }, "Failed to update employee position in Oracle.");
        return findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found."));
    }

    @Override
    public Employee updateSalary(Long employeeId, BigDecimal salary) {
        executeSingleUpdate(UPDATE_SALARY_SQL, statement -> {
            statement.setBigDecimal(1, salary);
            statement.setLong(2, employeeId);
        }, "Failed to update employee salary in Oracle.");
        return findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found."));
    }

    @Override
    public Employee updateEmployeeNumber(Long employeeId, String employeeNumber) {
        executeSingleUpdate(UPDATE_NUMBER_SQL, statement -> {
            statement.setString(1, employeeNumber);
            statement.setLong(2, employeeId);
        }, "Failed to update employee number in Oracle.");
        return findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found."));
    }

    @Override
    public Employee updateActive(Long employeeId, boolean active) {
        executeSingleUpdate(UPDATE_ACTIVE_SQL, statement -> {
            statement.setString(1, toActiveFlag(active));
            statement.setLong(2, employeeId);
        }, "Failed to update employee active flag in Oracle.");
        return findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found."));
    }

    @Override
    public void deleteById(Long employeeId) {
        executeSingleUpdate(DELETE_EMPLOYEE_SQL, statement -> statement.setLong(1, employeeId),
                "Failed to delete employee in Oracle.");
    }

    @Override
    public Optional<Employee> findById(Long employeeId) {
        List<Employee> employees = executeListQuery(FIND_BY_ID_SQL, statement -> statement.setLong(1, employeeId));
        return employees.stream().findFirst();
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        List<Employee> employees = executeListQuery(FIND_BY_EMAIL_SQL, statement -> statement.setString(1, email));
        return employees.stream().findFirst();
    }

    @Override
    public Optional<Employee> findByEmployeeNumber(String employeeNumber) {
        List<Employee> employees = executeListQuery(FIND_BY_NUMBER_SQL, statement -> statement.setString(1, employeeNumber));
        return employees.stream().findFirst();
    }

    @Override
    public List<Employee> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    @Override
    public List<Employee> findActive() {
        return executeListQuery(FIND_ACTIVE_SQL, statement -> {
        });
    }

    @Override
    public boolean existsById(Long employeeId) {
        return existsByQuery(EXISTS_ID_SQL, statement -> statement.setLong(1, employeeId));
    }

    @Override
    public boolean existsByEmail(String email) {
        return existsByQuery(EXISTS_EMAIL_SQL, statement -> statement.setString(1, email));
    }

    @Override
    public boolean existsByEmailExcludingId(String email, Long excludedEmployeeId) {
        return existsByQuery(EXISTS_EMAIL_EXCLUDING_SQL, statement -> {
            statement.setString(1, email);
            statement.setLong(2, excludedEmployeeId);
        });
    }

    @Override
    public boolean existsByEmployeeNumber(String employeeNumber) {
        return existsByQuery(EXISTS_NUMBER_SQL, statement -> statement.setString(1, employeeNumber));
    }

    @Override
    public boolean existsByEmployeeNumberExcludingId(String employeeNumber, Long excludedEmployeeId) {
        return existsByQuery(EXISTS_NUMBER_EXCLUDING_SQL, statement -> {
            statement.setString(1, employeeNumber);
            statement.setLong(2, excludedEmployeeId);
        });
    }

    private List<Employee> executeListQuery(String sql, SqlParameterSetter setter) {
        List<Employee> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapEmployee(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query employees from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle employee query returned rows: " + result.size());
        return result;
    }

    private void executeSingleUpdate(String sql, SqlParameterSetter setter, String errorMessage) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Employee not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
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
            throw new IllegalStateException("Failed to execute employee existence query in Oracle.", exception);
        }
    }

    private boolean isUserPresent(Connection connection, Long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(USER_EXISTS_SQL)) {
            statement.setLong(1, userId);
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

    private Employee mapEmployee(ResultSet resultSet) throws SQLException {
        UserRole role = UserRole.valueOf(resultSet.getString("role_code").toUpperCase(Locale.ROOT));

        Employee employee = new Employee(
                resultSet.getLong("employee_id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getString("employee_number"),
                resultSet.getString("position_title"),
                resultSet.getBigDecimal("salary"),
                role
        );
        employee.setActive("Y".equalsIgnoreCase(resultSet.getString("active_flag")));
        return employee;
    }

    private String toActiveFlag(boolean active) {
        return active ? "Y" : "N";
    }

    @FunctionalInterface
    private interface SqlParameterSetter {
        void set(PreparedStatement statement) throws SQLException;
    }
}
