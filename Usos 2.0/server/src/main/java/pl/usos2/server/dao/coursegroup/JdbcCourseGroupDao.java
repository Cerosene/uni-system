package pl.usos2.server.dao.coursegroup;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.user.Lecturer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcCourseGroupDao implements CourseGroupDao {
    private static final Logger logger = Logger.getLogger(JdbcCourseGroupDao.class.getName());

    private static final String BASE_SELECT = """
            SELECT
                cg.group_id,
                cg.group_name,
                s.subject_id,
                s.subject_name,
                s.subject_code,
                s.ects,
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
            FROM course_groups cg
            JOIN subjects s ON s.subject_id = cg.subject_id
            JOIN lecturers l ON l.lecturer_id = cg.lecturer_id
            JOIN employees e ON e.employee_id = l.lecturer_id
            JOIN users lu ON lu.user_id = l.lecturer_id
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE cg.group_id = ?";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY cg.group_name";
    private static final String FIND_BY_LECTURER_SQL = BASE_SELECT + " WHERE cg.lecturer_id = ? ORDER BY cg.group_name";
    private static final String FIND_BY_STUDENT_SQL = BASE_SELECT + " JOIN enrollments en ON en.group_id = cg.group_id WHERE en.student_id = ? AND en.active_flag = 'Y' ORDER BY cg.group_name";
    private static final String COUNT_ACTIVE_STUDENTS_BY_LECTURER_SQL = """
            SELECT COUNT(DISTINCT en.student_id)
            FROM enrollments en
            JOIN course_groups cg ON cg.group_id = en.group_id
            WHERE cg.lecturer_id = ?
              AND en.active_flag = 'Y'
            """;

    private static final String INSERT_SQL = """
            INSERT INTO course_groups (group_id, group_name, subject_id, lecturer_id)
            VALUES (?, ?, ?, ?)
            """;
    private static final String EXISTS_ID_SQL = "SELECT COUNT(*) FROM course_groups WHERE group_id = ?";

    @Override
    public StudentGroup save(StudentGroup group) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, group.getId());
            statement.setString(2, group.getName());
            statement.setLong(3, group.getCourse().getId());
            statement.setLong(4, group.getLecturer().getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save course group in Oracle.", exception);
        }

        StudentGroup saved = findById(group.getId())
                .orElseThrow(() -> new IllegalStateException("Saved course group cannot be read from Oracle. id=" + group.getId()));
        logger.info("[DIAGNOSTIC] Course group saved in Oracle. groupId=" + saved.getId());
        return saved;
    }

    @Override
    public Optional<StudentGroup> findById(Long groupId) {
        List<StudentGroup> groups = executeListQuery(FIND_BY_ID_SQL, statement -> statement.setLong(1, groupId));
        return groups.stream().findFirst();
    }

    @Override
    public List<StudentGroup> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    @Override
    public List<StudentGroup> findByLecturerId(Long lecturerId) {
        return executeListQuery(FIND_BY_LECTURER_SQL, statement -> statement.setLong(1, lecturerId));
    }

    @Override
    public List<StudentGroup> findByStudentId(Long studentId) {
        return executeListQuery(FIND_BY_STUDENT_SQL, statement -> statement.setLong(1, studentId));
    }

    @Override
    public int countActiveStudentsByLecturerId(Long lecturerId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_ACTIVE_STUDENTS_BY_LECTURER_SQL)) {
            statement.setLong(1, lecturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt(1);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to count active students by lecturer in Oracle.", exception);
        }
    }

    @Override
    public boolean existsById(Long groupId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_ID_SQL)) {
            statement.setLong(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to execute course group existence query in Oracle.", exception);
        }
    }

    private List<StudentGroup> executeListQuery(String sql, SqlParameterSetter setter) {
        List<StudentGroup> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapGroup(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query course groups from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle course group query returned rows: " + result.size());
        return result;
    }

    private StudentGroup mapGroup(ResultSet resultSet) throws SQLException {
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
        lecturer.setActive("Y".equalsIgnoreCase(resultSet.getString("lecturer_active_flag")));

        Course course = new Course(
                resultSet.getLong("subject_id"),
                resultSet.getString("subject_name"),
                resultSet.getString("subject_code"),
                resultSet.getInt("ects"),
                lecturer
        );

        return new StudentGroup(
                resultSet.getLong("group_id"),
                resultSet.getString("group_name"),
                course,
                lecturer
        );
    }

    @FunctionalInterface
    private interface SqlParameterSetter {
        void set(PreparedStatement statement) throws SQLException;
    }
}
