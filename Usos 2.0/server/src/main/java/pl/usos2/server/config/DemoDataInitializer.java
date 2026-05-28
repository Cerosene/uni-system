package pl.usos2.server.config;

import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Logger;

public final class DemoDataInitializer {
    private static final Logger logger = Logger.getLogger(DemoDataInitializer.class.getName());

    private DemoDataInitializer() {
    }

    public static void initialize(ApplicationContext context) {
        Student student = new Student(
                1003L,
                "Dmytro",
                "Lytvyn",
                "dmytro@uni.pl",
                "pass123",
                "320103",
                "Informatyka",
                Semester.THIRD
        );

        Student mateusz = new Student(
                1001L,
                "Mateusz",
                "Lewandowski",
                "mateusz@uni.pl",
                "password123",
                "320101",
                "Informatyka",
                Semester.THIRD
        );

        Lecturer lecturer = new Lecturer(
                2L,
                "Tomasz",
                "Nowak",
                "lecturer@uni.pl",
                "password123",
                "EMP201",
                "Dr."
        );

        Lecturer lecturer2 = new Lecturer(
                5L,
                "Maria",
                "Kowalska",
                "m.kow@uni.pl",
                "password123",
                "EMP202",
                "Prof."
        );

        Administrator admin = new Administrator(
                3L,
                "Anna",
                "Zielinska",
                "admin@uni.pl",
                "password123",
                "ADM001"
        );

        tryRegisterUser(context, student);
        tryRegisterUser(context, mateusz);
        tryRegisterUser(context, lecturer);
        tryRegisterUser(context, lecturer2);
        tryRegisterUser(context, admin);

        tryAddEmployee(context, lecturer);
        tryAddEmployee(context, lecturer2);
        tryAddEmployee(context, admin);

        Course algorithms = new Course(
                1L,
                "Zaawansowane Algorytmy",
                "CS301",
                6,
                lecturer
        );

        Course databases = new Course(
                2L,
                "Bazy Danych 2",
                "CS302",
                5,
                lecturer2
        );

        tryAddCourse(context, algorithms);
        tryAddCourse(context, databases);

        tryAddGrade(context, student, algorithms, lecturer, 4.5, "Kolokwium 1");
        tryAddGrade(context, student, databases, lecturer2, 5.0, "Projekt semestralny");
        tryAddGrade(context, mateusz, algorithms, lecturer, 4.0, "Kolokwium 1");

        tryCreatePayment(context, 1L, student, new BigDecimal("22.00"), "Oplata za legitymacje studencka",
                LocalDate.of(2025, 10, 15));
        tryMarkPaymentAsPaid(context, 1L);

        tryCreatePayment(context, 2L, student, new BigDecimal("2000.00"), "Czesne - Semestr 3",
                LocalDate.of(2025, 11, 1));
        tryMarkPaymentAsPaid(context, 2L);

        tryCreatePayment(context, 3L, student, new BigDecimal("250.00"),
                "Oplata za powtarzanie przedmiotu: Algorytmy", LocalDate.of(2026, 6, 15));

        tryCreatePayment(context, 4L, mateusz, new BigDecimal("22.00"), "Oplata za legitymacje studencka",
                LocalDate.of(2025, 10, 15));

        tryCreatePayment(context, 5L, mateusz, new BigDecimal("250.00"),
                "Oplata za powtarzanie przedmiotu: Algorytmy", LocalDate.of(2026, 6, 15));

        context.getRequestService().submitRequest(
                mateusz,
                RequestType.OTHER,
                "Prosba o wydanie zaswiadczenia o statusie studenta."
        );

        context.getMessageService().sendMessage(
                lecturer,
                mateusz,
                "Informacja o zajeciach",
                "Prosze pamietac o oddaniu projektu semestralnego."
        );

        context.getMessageService().sendMessage(
                student,
                lecturer,
                "Pytanie o projekt",
                "Dzien dobry, czy projekt musi byc w JavaFX?"
        );

        context.getMessageService().sendMessage(
                lecturer,
                student,
                "Odpowiedz: projekt",
                "Tak, projekt powinien miec interfejs JavaFX."
        );

        context.getServiceTicketService().createTicket(
                student,
                "Brak dostepu do WiFi",
                "W sali 312 nie dziala eduroam."
        );
    }

    private static void tryAddGrade(ApplicationContext context,
                                    Student student,
                                    Course course,
                                    Lecturer lecturer,
                                    double value,
                                    String description) {
        try {
            context.getGradeService().addGrade(student, course, lecturer, value, description);
        } catch (IllegalArgumentException exception) {
            logger.info("Skipping demo grade insert (likely already in Oracle): " + exception.getMessage());
        }
    }

    private static void tryAddCourse(ApplicationContext context, Course course) {
        try {
            context.getCourseService().addCourse(course);
        } catch (IllegalArgumentException exception) {
            logger.info("Skipping demo course insert (likely already in Oracle): " + exception.getMessage());
        }
    }

    private static void tryRegisterUser(ApplicationContext context, pl.usos2.server.model.user.User user) {
        try {
            context.getAuthService().register(user);
        } catch (IllegalArgumentException exception) {
            logger.info("Skipping demo user register (likely already in Oracle): " + exception.getMessage());
        }
    }

    private static void tryAddEmployee(ApplicationContext context, pl.usos2.server.model.user.Employee employee) {
        try {
            context.getEmployeeService().addEmployee(employee);
        } catch (IllegalArgumentException exception) {
            logger.info("Skipping demo employee insert (likely already in Oracle): " + exception.getMessage());
        }
    }

    private static void tryCreatePayment(ApplicationContext context,
                                         Long paymentId,
                                         Student student,
                                         BigDecimal amount,
                                         String title,
                                         LocalDate dueDate) {
        try {
            context.getPaymentService().createPayment(paymentId, student, amount, title, dueDate);
        } catch (IllegalArgumentException exception) {
            logger.info("Skipping demo payment insert (likely already in Oracle): " + exception.getMessage());
        }
    }

    private static void tryMarkPaymentAsPaid(ApplicationContext context, Long paymentId) {
        try {
            context.getPaymentService().markAsPaid(paymentId);
        } catch (IllegalStateException | IllegalArgumentException exception) {
            logger.info("Skipping demo payment status update (likely already paid or missing): " + exception.getMessage());
        }
    }
}
