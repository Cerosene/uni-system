package pl.usos2.server.config;

import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.enumtype.RequestType;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class DemoDataInitializer {

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
                "Zielińska",
                "admin@uni.pl",
                "password123",
                "ADM001"
        );

        context.getAuthService().register(student);
        context.getAuthService().register(mateusz);
        context.getAuthService().register(lecturer);
        context.getAuthService().register(lecturer2);
        context.getAuthService().register(admin);

        context.getEmployeeService().addEmployee(lecturer);
        context.getEmployeeService().addEmployee(lecturer2);
        context.getEmployeeService().addEmployee(admin);

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

        context.getCourseService().addCourse(algorithms);
        context.getCourseService().addCourse(databases);

        context.getGradeService().addGrade(student, algorithms, lecturer, 4.5, "Kolokwium 1");
        context.getGradeService().addGrade(student, databases, lecturer2, 5.0, "Projekt semestralny");
        context.getGradeService().addGrade(mateusz, algorithms, lecturer, 4.0, "Kolokwium 1");

        context.getPaymentService().createPayment(
                1L,
                student,
                new BigDecimal("22.00"),
                "Opłata za legitymację studencką",
                LocalDate.of(2025, 10, 15)
        );
        context.getPaymentService().markAsPaid(1L);

        context.getPaymentService().createPayment(
                2L,
                student,
                new BigDecimal("2000.00"),
                "Czesne - Semestr 3",
                LocalDate.of(2025, 11, 1)
        );
        context.getPaymentService().markAsPaid(2L);

        context.getPaymentService().createPayment(
                3L,
                student,
                new BigDecimal("250.00"),
                "Opłata za powtarzanie przedmiotu: Algorytmy",
                LocalDate.of(2026, 6, 15)
        );

        context.getPaymentService().createPayment(
                4L,
                mateusz,
                new BigDecimal("22.00"),
                "Opłata za legitymację studencką",
                LocalDate.of(2025, 10, 15)
        );

        context.getPaymentService().createPayment(
                5L,
                mateusz,
                new BigDecimal("250.00"),
                "Opłata za powtarzanie przedmiotu: Algorytmy",
                LocalDate.of(2026, 6, 15)
        );

        context.getRequestService().submitRequest(
                mateusz,
                RequestType.OTHER,
                "Prośba o wydanie zaświadczenia o statusie studenta."
        );

        context.getMessageService().sendMessage(
                lecturer,
                mateusz,
                "Informacja o zajęciach",
                "Proszę pamiętać o oddaniu projektu semestralnego."
        );

        context.getMessageService().sendMessage(
                student,
                lecturer,
                "Pytanie o projekt",
                "Dzień dobry, czy projekt musi być w JavaFX?"
        );

        context.getMessageService().sendMessage(
                lecturer,
                student,
                "Odpowiedź: projekt",
                "Tak, projekt powinien mieć interfejs JavaFX."
        );

        context.getServiceTicketService().createTicket(
                student,
                "Brak dostępu do WiFi",
                "W sali 312 nie działa eduroam."
        );
    }
}