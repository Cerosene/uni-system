package pl.usos2.server.unit.config;

import org.junit.jupiter.api.Test;
import pl.usos2.server.config.ApplicationContext;
import pl.usos2.server.config.DemoDataInitializer;
import pl.usos2.server.model.enumtype.UserRole;

import static org.junit.jupiter.api.Assertions.*;

class DemoDataInitializerTest {

    @Test
    void shouldInitializeDemoDataForGui() {
        ApplicationContext context = new ApplicationContext();

        DemoDataInitializer.initialize(context);

        assertFalse(context.getAuthService().getAllUsers().isEmpty(), "Users list should not be empty after initialization");
        assertFalse(context.getAuthService().getUsersByRole(UserRole.STUDENT).isEmpty(), "Students list should not be empty");
        assertFalse(context.getAuthService().getUsersByRole(UserRole.LECTURER).isEmpty(), "Lecturers list should not be empty");
        assertFalse(context.getAuthService().getUsersByRole(UserRole.ADMINISTRATOR).isEmpty(), "Administrators list should not be empty");

        assertFalse(context.getCourseService().getAllCourses().isEmpty(), "Courses list should not be empty");
        assertFalse(context.getGradeService().getAllGrades().isEmpty(), "Grades list should not be empty");
        assertFalse(context.getPaymentService().getAllPayments().isEmpty(), "Payments list should not be empty");
        assertFalse(context.getMessageService().getAllMessages().isEmpty(), "Messages list should not be empty");
        assertFalse(context.getRequestService().getAllRequests().isEmpty(), "Requests list should not be empty");
        assertFalse(context.getServiceTicketService().getAllTickets().isEmpty(), "Service tickets list should not be empty");
    }

    @Test
    void shouldCreateCorrectNumberOfUsers() {
        ApplicationContext context = new ApplicationContext();

        DemoDataInitializer.initialize(context);

        assertEquals(2, context.getAuthService().getUsersByRole(UserRole.STUDENT).size(), "Should have 2 students");
        assertEquals(2, context.getAuthService().getUsersByRole(UserRole.LECTURER).size(), "Should have 2 lecturers");
        assertEquals(1, context.getAuthService().getUsersByRole(UserRole.ADMINISTRATOR).size(), "Should have 1 administrator");
        assertEquals(5, context.getAuthService().getAllUsers().size(), "Total should be 5 users");
    }

    @Test
    void shouldCreateCoursesWithValidData() {
        ApplicationContext context = new ApplicationContext();

        DemoDataInitializer.initialize(context);

        var courses = context.getCourseService().getAllCourses();
        assertEquals(2, courses.size(), "Should have 2 courses");

        var algorithmsCourse = courses.stream()
                .filter(c -> "CS301".equals(c.getCode()))
                .findFirst();
        assertTrue(algorithmsCourse.isPresent(), "Should have course with code CS301");
        assertEquals("Zaawansowane Algorytmy", algorithmsCourse.get().getName());
        assertEquals(6, algorithmsCourse.get().getEcts());

        var databasesCourse = courses.stream()
                .filter(c -> "CS302".equals(c.getCode()))
                .findFirst();
        assertTrue(databasesCourse.isPresent(), "Should have course with code CS302");
        assertEquals("Bazy Danych 2", databasesCourse.get().getName());
        assertEquals(5, databasesCourse.get().getEcts());
    }

    @Test
    void shouldCreatePaymentsWithValidData() {
        ApplicationContext context = new ApplicationContext();

        DemoDataInitializer.initialize(context);

        var payments = context.getPaymentService().getAllPayments();
        assertTrue(payments.size() >= 5, "Should have at least 5 payments");

        var paidPayments = payments.stream().filter(p -> p.isPaid()).count();
        assertTrue(paidPayments >= 2, "Should have at least 2 paid payments");
    }

    @Test
    void shouldCreateMessagesWithValidData() {
        ApplicationContext context = new ApplicationContext();

        DemoDataInitializer.initialize(context);

        var messages = context.getMessageService().getAllMessages();
        assertEquals(3, messages.size(), "Should have 3 messages");

        var allHaveValidSubjects = messages.stream()
                .allMatch(m -> m.getSubject() != null && !m.getSubject().isBlank());
        assertTrue(allHaveValidSubjects, "All messages should have non-blank subjects");
    }

    @Test
    void shouldAllUsersBeActive() {
        ApplicationContext context = new ApplicationContext();

        DemoDataInitializer.initialize(context);

        var allActive = context.getAuthService().getAllUsers().stream()
                .allMatch(user -> user.isActive());
        assertTrue(allActive, "All initialized users should be active");
    }
}
