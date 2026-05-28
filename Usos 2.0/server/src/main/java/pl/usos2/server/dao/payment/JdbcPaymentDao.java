package pl.usos2.server.dao.payment;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.finance.Payment;
import pl.usos2.server.model.user.Student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public class JdbcPaymentDao implements PaymentDao {
    private static final Logger logger = Logger.getLogger(JdbcPaymentDao.class.getName());

    private static final String BASE_SELECT = """
            SELECT
                p.payment_id,
                p.amount,
                p.title,
                p.paid_flag,
                p.due_date,
                s.student_id,
                s.student_number,
                s.field_of_study,
                s.semester,
                u.first_name,
                u.last_name,
                u.email,
                u.password_hash,
                u.active_flag
            FROM payments p
            JOIN students s ON s.student_id = p.student_id
            JOIN users u ON u.user_id = s.student_id
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE p.payment_id = ?";
    private static final String FIND_BY_STUDENT_SQL = BASE_SELECT + " WHERE p.student_id = ? ORDER BY p.due_date, p.payment_id";
    private static final String FIND_UNPAID_BY_STUDENT_SQL = BASE_SELECT
            + " WHERE p.student_id = ? AND p.paid_flag = 'N' ORDER BY p.due_date, p.payment_id";
    private static final String FIND_PAID_BY_STUDENT_SQL = BASE_SELECT
            + " WHERE p.student_id = ? AND p.paid_flag = 'Y' ORDER BY p.due_date, p.payment_id";
    private static final String FIND_OVERDUE_SQL = BASE_SELECT
            + " WHERE p.paid_flag = 'N' AND p.due_date < ? ORDER BY p.due_date, p.payment_id";
    private static final String FIND_OVERDUE_BY_STUDENT_SQL = BASE_SELECT
            + " WHERE p.student_id = ? AND p.paid_flag = 'N' AND p.due_date < ? ORDER BY p.due_date, p.payment_id";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY p.due_date, p.payment_id";

    private static final String INSERT_SQL = """
            INSERT INTO payments (payment_id, student_id, amount, title, paid_flag, due_date, created_at)
            VALUES (?, ?, ?, ?, 'N', ?, ?)
            """;
    private static final String MARK_PAID_SQL = """
            UPDATE payments
            SET paid_flag = 'Y'
            WHERE payment_id = ?
            """;
    private static final String MARK_UNPAID_SQL = """
            UPDATE payments
            SET paid_flag = 'N'
            WHERE payment_id = ?
            """;
    private static final String DELETE_SQL = "DELETE FROM payments WHERE payment_id = ?";
    private static final String EXISTS_BY_ID_SQL = "SELECT COUNT(*) FROM payments WHERE payment_id = ?";
    private static final String EXISTS_DUPLICATE_UNPAID_SQL = """
            SELECT COUNT(*)
            FROM payments
            WHERE student_id = ?
              AND paid_flag = 'N'
              AND amount = ?
              AND UPPER(title) = UPPER(?)
              AND due_date = ?
            """;

    @Override
    public Payment save(Long id, Long studentId, BigDecimal amount, String title, LocalDate dueDate) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, id);
            statement.setLong(2, studentId);
            statement.setBigDecimal(3, amount);
            statement.setString(4, title);
            statement.setDate(5, Date.valueOf(dueDate));
            statement.setTimestamp(6, Timestamp.valueOf(java.time.LocalDateTime.now()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save payment to Oracle.", exception);
        }

        return findById(id)
                .orElseThrow(() -> new IllegalStateException("Saved payment cannot be read from Oracle. id=" + id));
    }

    @Override
    public Payment markAsPaid(Long paymentId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(MARK_PAID_SQL)) {
            statement.setLong(1, paymentId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Payment not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to mark payment as paid in Oracle.", exception);
        }

        return findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found."));
    }

    @Override
    public Payment markAsUnpaid(Long paymentId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(MARK_UNPAID_SQL)) {
            statement.setLong(1, paymentId);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Payment not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to mark payment as unpaid in Oracle.", exception);
        }

        return findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found."));
    }

    @Override
    public void deleteById(Long paymentId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, paymentId);
            int deleted = statement.executeUpdate();
            if (deleted == 0) {
                throw new IllegalArgumentException("Payment not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete payment in Oracle.", exception);
        }
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        List<Payment> payments = executeListQuery(FIND_BY_ID_SQL, statement -> statement.setLong(1, paymentId));
        return payments.stream().findFirst();
    }

    @Override
    public List<Payment> findByStudentId(Long studentId) {
        return executeListQuery(FIND_BY_STUDENT_SQL, statement -> statement.setLong(1, studentId));
    }

    @Override
    public List<Payment> findUnpaidByStudentId(Long studentId) {
        return executeListQuery(FIND_UNPAID_BY_STUDENT_SQL, statement -> statement.setLong(1, studentId));
    }

    @Override
    public List<Payment> findPaidByStudentId(Long studentId) {
        return executeListQuery(FIND_PAID_BY_STUDENT_SQL, statement -> statement.setLong(1, studentId));
    }

    @Override
    public List<Payment> findOverdue(LocalDate currentDate) {
        return executeListQuery(FIND_OVERDUE_SQL, statement -> statement.setDate(1, Date.valueOf(currentDate)));
    }

    @Override
    public List<Payment> findOverdueByStudentId(Long studentId, LocalDate currentDate) {
        return executeListQuery(FIND_OVERDUE_BY_STUDENT_SQL, statement -> {
            statement.setLong(1, studentId);
            statement.setDate(2, Date.valueOf(currentDate));
        });
    }

    @Override
    public List<Payment> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    @Override
    public boolean existsById(Long paymentId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_BY_ID_SQL)) {
            statement.setLong(1, paymentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to check payment id existence in Oracle.", exception);
        }
    }

    @Override
    public boolean existsDuplicateUnpaid(Long studentId, BigDecimal amount, String title, LocalDate dueDate) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_DUPLICATE_UNPAID_SQL)) {
            statement.setLong(1, studentId);
            statement.setBigDecimal(2, amount);
            statement.setString(3, title);
            statement.setDate(4, Date.valueOf(dueDate));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to check duplicate unpaid payment in Oracle.", exception);
        }
    }

    private List<Payment> executeListQuery(String sql, SqlParameterSetter setter) {
        List<Payment> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapPayment(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query payments from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle payment query returned rows: " + result.size());
        return result;
    }

    private Payment mapPayment(ResultSet resultSet) throws SQLException {
        return new Payment(
                resultSet.getLong("payment_id"),
                mapStudent(resultSet),
                resultSet.getBigDecimal("amount"),
                resultSet.getString("title"),
                toPaid(resultSet.getString("paid_flag")),
                resultSet.getDate("due_date").toLocalDate()
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
        student.setActive("Y".equalsIgnoreCase(resultSet.getString("active_flag")));
        return student;
    }

    private boolean toPaid(String paidFlag) {
        return "Y".equalsIgnoreCase(paidFlag);
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
