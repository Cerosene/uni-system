package pl.usos2.server.dao.grade;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public class JdbcGradeDao implements GradeDao {
    private static final Logger logger = Logger.getLogger(JdbcGradeDao.class.getName());

    private static final String BASE_SELECT = """
            SELECT
                g.grade_id,
                g.grade_value,
                g.description,
                s.student_id,
                s.student_number,
                s.field_of_study,
                s.semester,
                su.first_name AS student_first_name,
                su.last_name AS student_last_name,
                su.email AS student_email,
                su.password_hash AS student_password,
                su.active_flag AS student_active_flag,
                subj.subject_id,
                subj.subject_name,
                subj.subject_code,
                subj.ects,
                l.lecturer_id,
                lu.first_name AS lecturer_first_name,
                lu.last_name AS lecturer_last_name,
                lu.email AS lecturer_email,
                lu.password_hash AS lecturer_password,
                lu.active_flag AS lecturer_active_flag,
                e.employee_number,
                e.position_title,
                e.salary,
                l.academic_title
            FROM grades g
            JOIN students s ON s.student_id = g.student_id
            JOIN users su ON su.user_id = s.student_id
            JOIN subjects subj ON subj.subject_id = g.subject_id
            JOIN lecturers l ON l.lecturer_id = g.lecturer_id
            JOIN employees e ON e.employee_id = l.lecturer_id
            JOIN users lu ON lu.user_id = l.lecturer_id
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE g.grade_id = ?";
    private static final String FIND_BY_STUDENT_SQL = BASE_SELECT + " WHERE g.student_id = ? ORDER BY g.grade_id";
    private static final String FIND_BY_SUBJECT_SQL = BASE_SELECT + " WHERE g.subject_id = ? ORDER BY g.grade_id";
    private static final String FIND_BY_LECTURER_SQL = BASE_SELECT + " WHERE g.lecturer_id = ? ORDER BY g.grade_id";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY g.grade_id";

    private static final String INSERT_SQL = """
            INSERT INTO grades (grade_id, student_id, subject_id, lecturer_id, grade_value, description)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE grades
            SET grade_value = ?, description = ?
            WHERE grade_id = ?
            """;
    private static final String NEXT_ID_SQL = "SELECT NVL(MAX(grade_id), 0) + 1 FROM grades";
    private static final String DUPLICATE_SQL = """
            SELECT COUNT(*)
            FROM grades
            WHERE student_id = ?
              AND subject_id = ?
              AND UPPER(description) = UPPER(?)
            """;

    @Override
    public Grade save(Long studentId, Long subjectId, Long lecturerId, double value, String description) {
        Long nextId = getNextGradeId();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, nextId);
            statement.setLong(2, studentId);
            statement.setLong(3, subjectId);
            statement.setLong(4, lecturerId);
            statement.setBigDecimal(5, BigDecimal.valueOf(value));
            statement.setString(6, description);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save grade to Oracle.", exception);
        }

        return findById(nextId)
                .orElseThrow(() -> new IllegalStateException("Saved grade cannot be read from Oracle. id=" + nextId));
    }

    @Override
    public Grade update(Long gradeId, double value, String description) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setBigDecimal(1, BigDecimal.valueOf(value));
            statement.setString(2, description);
            statement.setLong(3, gradeId);
            int affected = statement.executeUpdate();
            if (affected == 0) {
                throw new IllegalArgumentException("Grade not found.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to update grade in Oracle.", exception);
        }

        return findById(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found."));
    }

    @Override
    public Optional<Grade> findById(Long gradeId) {
        List<Grade> grades = executeListQuery(FIND_BY_ID_SQL, statement -> statement.setLong(1, gradeId));
        return grades.stream().findFirst();
    }

    @Override
    public List<Grade> findByStudentId(Long studentId) {
        return executeListQuery(FIND_BY_STUDENT_SQL, statement -> statement.setLong(1, studentId));
    }

    @Override
    public List<Grade> findBySubjectId(Long subjectId) {
        return executeListQuery(FIND_BY_SUBJECT_SQL, statement -> statement.setLong(1, subjectId));
    }

    @Override
    public List<Grade> findByLecturerId(Long lecturerId) {
        return executeListQuery(FIND_BY_LECTURER_SQL, statement -> statement.setLong(1, lecturerId));
    }

    @Override
    public List<Grade> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    @Override
    public boolean existsDuplicate(Long studentId, Long subjectId, String description) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DUPLICATE_SQL)) {
            statement.setLong(1, studentId);
            statement.setLong(2, subjectId);
            statement.setString(3, description);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to check duplicate grade in Oracle.", exception);
        }
    }

    private List<Grade> executeListQuery(String sql, SqlParameterSetter setter) {
        List<Grade> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setter.set(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapGrade(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query grades from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle grade query returned rows: " + result.size());
        return result;
    }

    private Long getNextGradeId() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(NEXT_ID_SQL)) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Cannot generate next grade id.");
            }
            return resultSet.getLong(1);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to generate next grade id from Oracle.", exception);
        }
    }

    private Grade mapGrade(ResultSet resultSet) throws SQLException {
        Lecturer lecturer = mapLecturer(resultSet);
        Course course = new Course(
                resultSet.getLong("subject_id"),
                resultSet.getString("subject_name"),
                resultSet.getString("subject_code"),
                resultSet.getInt("ects"),
                lecturer
        );
        Student student = mapStudent(resultSet);

        return new Grade(
                resultSet.getLong("grade_id"),
                student,
                course,
                lecturer,
                resultSet.getDouble("grade_value"),
                resultSet.getString("description")
        );
    }

    private Student mapStudent(ResultSet resultSet) throws SQLException {
        Student student = new Student(
                resultSet.getLong("student_id"),
                resultSet.getString("student_first_name"),
                resultSet.getString("student_last_name"),
                resultSet.getString("student_email"),
                resultSet.getString("student_password"),
                resultSet.getString("student_number"),
                resultSet.getString("field_of_study"),
                toSemester(resultSet.getString("semester"))
        );
        student.setActive(toActive(resultSet.getString("student_active_flag")));
        return student;
    }

    private Lecturer mapLecturer(ResultSet resultSet) throws SQLException {
        Lecturer lecturer = new Lecturer(
                resultSet.getLong("lecturer_id"),
                resultSet.getString("lecturer_first_name"),
                resultSet.getString("lecturer_last_name"),
                resultSet.getString("lecturer_email"),
                resultSet.getString("lecturer_password"),
                resultSet.getString("employee_number"),
                resultSet.getString("academic_title")
        );
        lecturer.setPosition(resultSet.getString("position_title"));
        lecturer.setSalary(resultSet.getBigDecimal("salary"));
        lecturer.setActive(toActive(resultSet.getString("lecturer_active_flag")));
        return lecturer;
    }

    private Semester toSemester(String value) {
        if (value == null || value.isBlank()) {
            return Semester.FIRST;
        }
        return Semester.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private boolean toActive(String flag) {
        return "Y".equalsIgnoreCase(flag);
    }

    @FunctionalInterface
    private interface SqlParameterSetter {
        void set(PreparedStatement statement) throws SQLException;
    }

public List<Grade> getStudentsWithGradesForLecturer(Long lecturerId) {
    String sql = """
        SELECT 
            g.grade_id, g.grade_value, g.description,
            s.student_id, su.first_name AS student_first_name, su.last_name AS student_last_name,
            su.email AS student_email, su.password_hash AS student_password,
            s.student_number, s.field_of_study, s.semester, su.active_flag AS student_active_flag,
            subj.subject_id, subj.subject_name, subj.subject_code, subj.ects,
            l.lecturer_id, lu.first_name AS lecturer_first_name, lu.last_name AS lecturer_last_name,
            lu.email AS lecturer_email, lu.password_hash AS lecturer_password,
            lu.active_flag AS lecturer_active_flag, e.employee_number, e.position_title, e.salary, l.academic_title
        FROM USOS.STUDENTS s
        JOIN USOS.USERS su ON su.user_id = s.student_id
        JOIN USOS.ENROLLMENTS enr ON s.student_id = enr.student_id
        JOIN USOS.COURSE_GROUPS cg ON enr.group_id = cg.group_id
        JOIN USOS.SUBJECTS subj ON cg.subject_id = subj.subject_id
        JOIN USOS.LECTURERS l ON subj.lecturer_id = l.lecturer_id
        JOIN USOS.EMPLOYEES e ON e.employee_id = l.lecturer_id
        JOIN USOS.USERS lu ON lu.user_id = l.lecturer_id
        LEFT JOIN USOS.GRADES g ON s.student_id = g.student_id AND subj.subject_id = g.subject_id
        WHERE subj.lecturer_id = ?
        """;
    
    return executeListQuery(sql, statement -> statement.setLong(1, lecturerId));
}
}

