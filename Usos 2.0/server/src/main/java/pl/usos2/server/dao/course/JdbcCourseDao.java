package pl.usos2.server.dao.course;

import pl.usos2.server.database.DatabaseConnection;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.user.Lecturer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcCourseDao implements CourseDao {
    private static final Logger logger = Logger.getLogger(JdbcCourseDao.class.getName());

    private static final String BASE_SELECT = """
            SELECT
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
            FROM subjects s
            JOIN lecturers l ON l.lecturer_id = s.lecturer_id
            JOIN employees e ON e.employee_id = l.lecturer_id
            JOIN users lu ON lu.user_id = l.lecturer_id
            """;

    private static final String FIND_BY_ID_SQL = BASE_SELECT + " WHERE s.subject_id = ?";
    private static final String FIND_ALL_SQL = BASE_SELECT + " ORDER BY s.subject_name";
    private static final String FIND_BY_LECTURER_SQL = BASE_SELECT + " WHERE s.lecturer_id = ? ORDER BY s.subject_name";

    private static final String INSERT_SQL = """
            INSERT INTO subjects (subject_id, subject_name, subject_code, ects, lecturer_id)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String EXISTS_ID_SQL = "SELECT COUNT(*) FROM subjects WHERE subject_id = ?";
    private static final String EXISTS_CODE_SQL = "SELECT COUNT(*) FROM subjects WHERE UPPER(subject_code) = UPPER(?)";

    @Override
    public Course save(Course course) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, course.getId());
            statement.setString(2, course.getName());
            statement.setString(3, course.getCode());
            statement.setInt(4, course.getEcts());
            statement.setLong(5, course.getLecturer().getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save course in Oracle.", exception);
        }

        Course saved = findById(course.getId())
                .orElseThrow(() -> new IllegalStateException("Saved course cannot be read from Oracle. id=" + course.getId()));
        logger.info("[DIAGNOSTIC] Course saved in Oracle. subjectId=" + saved.getId());
        return saved;
    }

    @Override
    public Optional<Course> findById(Long courseId) {
        List<Course> courses = executeListQuery(FIND_BY_ID_SQL, statement -> statement.setLong(1, courseId));
        return courses.stream().findFirst();
    }

    @Override
    public List<Course> findAll() {
        return executeListQuery(FIND_ALL_SQL, statement -> {
        });
    }

    @Override
    public List<Course> findByLecturerId(Long lecturerId) {
        return executeListQuery(FIND_BY_LECTURER_SQL, statement -> statement.setLong(1, lecturerId));
    }

    @Override
    public boolean existsById(Long courseId) {
        return existsByQuery(EXISTS_ID_SQL, statement -> statement.setLong(1, courseId));
    }

    @Override
    public boolean existsByCode(String courseCode) {
        return existsByQuery(EXISTS_CODE_SQL, statement -> statement.setString(1, courseCode));
    }

    private List<Course> executeListQuery(String sql, SqlParameterSetter setter) {
        List<Course> result = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setter.set(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapCourse(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query courses from Oracle.", exception);
        }

        logger.info("[DIAGNOSTIC] Oracle course query returned rows: " + result.size());
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
            throw new IllegalStateException("Failed to execute course existence query in Oracle.", exception);
        }
    }

    private Course mapCourse(ResultSet resultSet) throws SQLException {
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

        return new Course(
                resultSet.getLong("subject_id"),
                resultSet.getString("subject_name"),
                resultSet.getString("subject_code"),
                resultSet.getInt("ects"),
                lecturer
        );
    }

    @FunctionalInterface
    private interface SqlParameterSetter {
        void set(PreparedStatement statement) throws SQLException;
    }

    @Override
    public void updateLecturerForCourse(Long courseId, Long lecturerId) {
        String sql = "UPDATE USOS.SUBJECTS SET LECTURER_ID = ? WHERE SUBJECT_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, lecturerId);
            ps.setLong(2, courseId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка при обновлении преподавателя предмета", e);
        }
    }
}
