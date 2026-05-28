package pl.usos2.server.service.course;

import pl.usos2.server.dao.course.CourseDao;
import pl.usos2.server.dao.course.JdbcCourseDao;
import pl.usos2.server.dao.coursegroup.CourseGroupDao;
import pl.usos2.server.dao.coursegroup.JdbcCourseGroupDao;
import pl.usos2.server.dao.enrollment.EnrollmentDao;
import pl.usos2.server.dao.enrollment.JdbcEnrollmentDao;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.util.ValidationUtils;

import java.util.List;
import java.util.logging.Logger;

public class CourseService {
    private static final Logger logger = Logger.getLogger(CourseService.class.getName());

    private final CourseDao courseDao;
    private final CourseGroupDao courseGroupDao;
    private final EnrollmentDao enrollmentDao;
    private final AuditLogService auditLogService;

    public CourseService() {
        this(new JdbcCourseDao(), new JdbcCourseGroupDao(), new JdbcEnrollmentDao(), AuditLogService.getInstance());
    }

    public CourseService(CourseDao courseDao, CourseGroupDao courseGroupDao, EnrollmentDao enrollmentDao) {
        this(courseDao, courseGroupDao, enrollmentDao, AuditLogService.getInstance());
    }

    public CourseService(CourseDao courseDao,
                         CourseGroupDao courseGroupDao,
                         EnrollmentDao enrollmentDao,
                         AuditLogService auditLogService) {
        this.courseDao = courseDao;
        this.courseGroupDao = courseGroupDao;
        this.enrollmentDao = enrollmentDao;
        this.auditLogService = auditLogService;
    }

    public Course addCourse(Course course) {
        validateCourse(course);

        boolean duplicateId = courseDao.existsById(course.getId());
        if (duplicateId) {
            logger.warning("Cannot add course. Duplicate id: " + course.getId());
            throw new IllegalArgumentException("Course with this id already exists.");
        }

        boolean duplicateCode = courseDao.existsByCode(course.getCode());
        if (duplicateCode) {
            logger.warning("Cannot add course. Duplicate code: " + course.getCode());
            throw new IllegalArgumentException("Course code already exists.");
        }

        Course saved = courseDao.save(course);
        logger.info("Added course: " + saved.getCode() + " - " + saved.getName());
        logger.info("[DIAGNOSTIC] Course persisted in Oracle. subjectId=" + saved.getId());
        return saved;
    }

    public List<Course> getAllCourses() {
        List<Course> courses = courseDao.findAll();
        logger.info("[DIAGNOSTIC] All courses loaded from Oracle. count=" + courses.size());
        return courses;
    }

    public List<Course> getCoursesForLecturer(Lecturer lecturer) {
        ValidationUtils.requireNotNull(lecturer, "Lecturer cannot be null.");
        ValidationUtils.requireNotNull(lecturer.getId(), "Lecturer id cannot be null.");

        List<Course> courses = courseDao.findByLecturerId(lecturer.getId());
        logger.info("[DIAGNOSTIC] Lecturer courses loaded from Oracle. lecturerId=" + lecturer.getId()
                + ", count=" + courses.size());
        return courses;
    }

    public StudentGroup addGroup(StudentGroup group) {
        validateGroup(group);

        if (courseGroupDao.existsById(group.getId())) {
            logger.warning("Cannot add group. Duplicate id: " + group.getId());
            throw new IllegalArgumentException("Group with this id already exists.");
        }

        StudentGroup saved = courseGroupDao.save(group);
        logger.info("[DIAGNOSTIC] Course group persisted in Oracle. groupId=" + saved.getId());
        return saved;
    }

    public List<StudentGroup> getAllGroups() {
        List<StudentGroup> groups = courseGroupDao.findAll();
        logger.info("[DIAGNOSTIC] All groups loaded from Oracle. count=" + groups.size());
        return groups;
    }

    public List<StudentGroup> getGroupsForStudent(Student student) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");

        List<StudentGroup> groups = courseGroupDao.findByStudentId(student.getId());
        logger.info("[DIAGNOSTIC] Student groups loaded from Oracle. studentId=" + student.getId()
                + ", count=" + groups.size());
        return groups;
    }

    public List<StudentGroup> getGroupsForLecturer(Lecturer lecturer) {
        ValidationUtils.requireNotNull(lecturer, "Lecturer cannot be null.");
        ValidationUtils.requireNotNull(lecturer.getId(), "Lecturer id cannot be null.");

        List<StudentGroup> groups = courseGroupDao.findByLecturerId(lecturer.getId());
        logger.info("[DIAGNOSTIC] Lecturer groups loaded from Oracle. lecturerId=" + lecturer.getId()
                + ", count=" + groups.size());
        return groups;
    }

    public int countActiveStudentsForLecturer(Lecturer lecturer) {
        ValidationUtils.requireNotNull(lecturer, "Lecturer cannot be null.");
        ValidationUtils.requireNotNull(lecturer.getId(), "Lecturer id cannot be null.");

        int count = courseGroupDao.countActiveStudentsByLecturerId(lecturer.getId());
        logger.info("[DIAGNOSTIC] Active students count loaded from Oracle for lecturer. lecturerId="
                + lecturer.getId() + ", count=" + count);
        return count;
    }

    public void enrollStudentToGroup(Student student, StudentGroup group) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");
        ValidationUtils.requireNotNull(group, "Group cannot be null.");
        ValidationUtils.requireNotNull(group.getId(), "Group id cannot be null.");

        if (!courseGroupDao.existsById(group.getId())) {
            throw new IllegalArgumentException("Group not found.");
        }

        enrollmentDao.enroll(student.getId(), group.getId());
        logger.info("[DIAGNOSTIC] Student enrollment saved in Oracle. studentId=" + student.getId()
                + ", groupId=" + group.getId());
        auditSafely(student.getId(), "GROUP_ENROLLMENT_CREATED", "ENROLLMENTS", group.getId(),
                "Student enrolled to groupId=" + group.getId());
    }

    public void removeStudentFromGroup(Student student, StudentGroup group) {
        ValidationUtils.requireNotNull(student, "Student cannot be null.");
        ValidationUtils.requireNotNull(student.getId(), "Student id cannot be null.");
        ValidationUtils.requireNotNull(group, "Group cannot be null.");
        ValidationUtils.requireNotNull(group.getId(), "Group id cannot be null.");

        enrollmentDao.remove(student.getId(), group.getId());
        logger.info("[DIAGNOSTIC] Student enrollment removed in Oracle. studentId=" + student.getId()
                + ", groupId=" + group.getId());
        auditSafely(student.getId(), "GROUP_ENROLLMENT_REMOVED", "ENROLLMENTS", group.getId(),
                "Student removed from groupId=" + group.getId());
    }

    public List<StudentGroup> getScheduleGroups() {
        List<StudentGroup> groups = courseGroupDao.findAll();
        logger.info("[DIAGNOSTIC] Schedule groups loaded from Oracle course_groups. count=" + groups.size());
        return groups;
    }

    private void validateCourse(Course course) {
        ValidationUtils.requireNotNull(course, "Course cannot be null.");
        ValidationUtils.requireNotNull(course.getId(), "Course id cannot be null.");
        ValidationUtils.requireNotBlank(course.getName(), "Course name cannot be empty.");
        ValidationUtils.requireNotBlank(course.getCode(), "Course code cannot be empty.");
        if (course.getEcts() <= 0) {
            throw new IllegalArgumentException("ECTS must be greater than zero.");
        }
        ValidationUtils.requireNotNull(course.getLecturer(), "Course lecturer cannot be null.");
        ValidationUtils.requireNotNull(course.getLecturer().getId(), "Lecturer id cannot be null.");
    }

    private void validateGroup(StudentGroup group) {
        ValidationUtils.requireNotNull(group, "Group cannot be null.");
        ValidationUtils.requireNotNull(group.getId(), "Group id cannot be null.");
        ValidationUtils.requireNotBlank(group.getName(), "Group name cannot be empty.");
        ValidationUtils.requireNotNull(group.getCourse(), "Group course cannot be null.");
        ValidationUtils.requireNotNull(group.getCourse().getId(), "Group course id cannot be null.");
        ValidationUtils.requireNotNull(group.getLecturer(), "Group lecturer cannot be null.");
        ValidationUtils.requireNotNull(group.getLecturer().getId(), "Group lecturer id cannot be null.");
    }

    private void auditSafely(Long userId, String actionName, String entityName, Long entityId, String details) {
        try {
            auditLogService.logEvent(userId, actionName, entityName, entityId, details);
        } catch (RuntimeException exception) {
            logger.warning("Audit log write failed: " + exception.getMessage());
        }
    }
}
