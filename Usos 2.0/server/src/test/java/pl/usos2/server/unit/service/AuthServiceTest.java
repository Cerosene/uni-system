package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.auth.AuthService;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;
    private Student student;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        student = new Student(
                1L,
                "Mateusz",
                "Lewandowski",
                "mateusz@test.pl",
                "secret123",
                "s12345",
                "Informatyka",
                Semester.THIRD
        );
    }

    @Test
    void shouldRegisterUser() {
        Student registered = (Student) authService.register(student);

        assertNotNull(registered);
        assertEquals("mateusz@test.pl", registered.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        authService.register(student);

        Student second = new Student(
                2L,
                "Jan",
                "Kowalski",
                "mateusz@test.pl",
                "secret999",
                "s99999",
                "Informatyka",
                Semester.FIRST
        );

        assertThrows(IllegalArgumentException.class, () -> authService.register(second));
    }

    @Test
    void shouldLoginCorrectly() {
        authService.register(student);

        assertDoesNotThrow(() -> authService.login("mateusz@test.pl", "secret123"));
    }

    @Test
    void shouldThrowExceptionForWrongPassword() {
        authService.register(student);

        assertThrows(IllegalArgumentException.class, () -> authService.login("mateusz@test.pl", "wrongpass"));
    }
}