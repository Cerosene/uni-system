package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.service.audit.AuditLogService;
import pl.usos2.server.service.course.CourseService;
import pl.usos2.server.unit.fake.FakeAuditLogDao;
import pl.usos2.server.unit.fake.FakeCourseDao;
import pl.usos2.server.unit.fake.FakeCourseGroupDao;
import pl.usos2.server.unit.fake.FakeEnrollmentDao;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.user.Student;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CourseServiceTest {

    private CourseService courseService;
    private Lecturer lecturer;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(
                new FakeCourseDao(),
                new FakeCourseGroupDao(),
                new FakeEnrollmentDao(),
                new AuditLogService(new FakeAuditLogDao())
        );
        lecturer = new Lecturer(
                1L,
                "Tomasz",
                "Nowak",
                "tomasz@uni.pl",
                "password123",
                "EMP201",
                "Dr."
        );
    }

    @Test
    @DisplayName("addCourse should add valid course")
    void addCourse_shouldAddValidCourse() {
        Course course = new Course(1L, "Zaawansowane Algorytmy", "CS301", 6, lecturer);

        Course added = courseService.addCourse(course);

        assertNotNull(added);
        assertEquals(1, courseService.getAllCourses().size());
        assertEquals("CS301", added.getCode());
    }

    @Test
    @DisplayName("addCourse should reject null course")
    void addCourse_shouldRejectNullCourse() {
        assertThrows(IllegalArgumentException.class, () -> courseService.addCourse(null));
    }

    @Test
    @DisplayName("addCourse should reject empty name")
    void addCourse_shouldRejectEmptyName() {
        Course course = new Course(1L, "", "CS301", 6, lecturer);
        assertThrows(IllegalArgumentException.class, () -> courseService.addCourse(course));
    }

    @Test
    @DisplayName("addCourse should reject empty code")
    void addCourse_shouldRejectEmptyCode() {
        Course course = new Course(1L, "Zaawansowane Algorytmy", "", 6, lecturer);
        assertThrows(IllegalArgumentException.class, () -> courseService.addCourse(course));
    }

    @Test
    @DisplayName("addCourse should reject non positive ects")
    void addCourse_shouldRejectNonPositiveEcts() {
        Course course = new Course(1L, "Zaawansowane Algorytmy", "CS301", 0, lecturer);
        assertThrows(IllegalArgumentException.class, () -> courseService.addCourse(course));
    }

    @Test
    @DisplayName("addCourse should reject duplicate code")
    void addCourse_shouldRejectDuplicateCode() {
        courseService.addCourse(new Course(1L, "Zaawansowane Algorytmy", "CS301", 6, lecturer));
        Course duplicate = new Course(2L, "Inne Algorytmy", "CS301", 5, lecturer);

        assertThrows(IllegalArgumentException.class, () -> courseService.addCourse(duplicate));
    }

    @Test
    @DisplayName("getCoursesForLecturer should return only lecturer courses")
    void getCoursesForLecturer_shouldReturnOnlyLecturerCourses() {
        Lecturer otherLecturer = new Lecturer(2L, "Maria", "Kowalska", "m.kow@uni.pl", "password123", "EMP202", "Prof.");
        courseService.addCourse(new Course(1L, "Zaawansowane Algorytmy", "CS301", 6, lecturer));
        courseService.addCourse(new Course(2L, "Bazy Danych 2", "CS302", 5, otherLecturer));

        assertEquals(1, courseService.getCoursesForLecturer(lecturer).size());
        assertEquals("CS301", courseService.getCoursesForLecturer(lecturer).get(0).getCode());
    }

    @Test
    @DisplayName("getStudentsForGroup should return students enrolled in the group")
    void getStudentsForGroup_shouldReturnStudentsEnrolledInGroup() {
        Course course = new Course(1L, "Systemy Operacyjne", "CS303", 6, lecturer);
        courseService.addCourse(course);
        StudentGroup group = new StudentGroup(1L, "Grupa A", course, lecturer);
        courseService.addGroup(group);

        Student student = new Student(
                10L,
                "Anna",
                "Nowak",
                "anna@test.pl",
                "password123",
                "S10",
                "Informatyka",
                pl.usos2.server.model.enumtype.Semester.FIRST
        );
        courseService.enrollStudentToGroup(student, group);

        assertEquals(1, courseService.getStudentsForGroup(group.getId()).size());
        assertEquals(10L, courseService.getStudentsForGroup(group.getId()).get(0).getId());
    }
}
