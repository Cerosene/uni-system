package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Administrator;
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
    @DisplayName("Powinno zarejestrować użytkownika z poprawnymi danymi")
    void shouldRegisterUser() {
        System.out.println("Test: rejestracja użytkownika");

        Student registered = (Student) authService.register(student);

        assertNotNull(registered);
        assertEquals("mateusz@test.pl", registered.getEmail());
        assertEquals(1, authService.getAllUsers().size());
    }

    @Test
    @DisplayName("Powinno zablokować rejestrację użytkownika z duplikatem id")
    void shouldThrowExceptionWhenUserIdAlreadyExists() {
        System.out.println("Test: blokada duplikatu id użytkownika");

        authService.register(student);

        Student second = new Student(
                1L,
                "Jan",
                "Kowalski",
                "jan@test.pl",
                "secret999",
                "s99999",
                "Matematyka",
                Semester.FIRST
        );

        assertThrows(IllegalArgumentException.class, () -> authService.register(second));
    }

    @Test
    @DisplayName("Powinno zablokować rejestrację użytkownika z duplikatem emaila")
    void shouldThrowExceptionWhenUserEmailAlreadyExists() {
        System.out.println("Test: blokada duplikatu email użytkownika");

        authService.register(student);

        Student second = new Student(
                2L,
                "Jan",
                "Kowalski",
                "mateusz@test.pl",
                "secret999",
                "s99999",
                "Matematyka",
                Semester.FIRST
        );

        assertThrows(IllegalArgumentException.class, () -> authService.register(second));
    }

    @Test
    @DisplayName("Powinno zalogować aktywnego użytkownika z poprawnym hasłem")
    void shouldLoginCorrectly() {
        System.out.println("Test: poprawne logowanie użytkownika");

        authService.register(student);

        assertDoesNotThrow(() -> authService.login("mateusz@test.pl", "secret123"));
    }

    @Test
    @DisplayName("Powinno zablokować logowanie nieaktywnego użytkownika")
    void shouldBlockLoginForInactiveUser() {
        System.out.println("Test: blokada logowania nieaktywnego użytkownika");

        authService.register(student);
        authService.deactivateUser(1L);

        assertThrows(IllegalStateException.class,
                () -> authService.login("mateusz@test.pl", "secret123"));
    }

    @Test
    @DisplayName("Powinno zaktualizować podstawowe dane użytkownika")
    void shouldUpdateBasicData() {
        System.out.println("Test: aktualizacja podstawowych danych użytkownika");

        authService.register(student);
        authService.updateBasicData(1L, "Jan", "Nowak");

        assertEquals("Jan", authService.findById(1L).getFirstName());
        assertEquals("Nowak", authService.findById(1L).getLastName());
    }

    @Test
    @DisplayName("Powinno zmienić email użytkownika")
    void shouldChangeEmail() {
        System.out.println("Test: zmiana emaila użytkownika");

        authService.register(student);
        authService.changeEmail(1L, "nowy@test.pl");

        assertEquals("nowy@test.pl", authService.findById(1L).getEmail());
    }

    @Test
    @DisplayName("Powinno zablokować zmianę emaila na już zajęty")
    void shouldThrowExceptionWhenChangingEmailToExistingOne() {
        System.out.println("Test: blokada zmiany emaila na zajęty");

        Student second = new Student(
                2L,
                "Anna",
                "Nowak",
                "anna@test.pl",
                "anna123",
                "s22222",
                "Biologia",
                Semester.SECOND
        );

        authService.register(student);
        authService.register(second);

        assertThrows(IllegalArgumentException.class,
                () -> authService.changeEmail(1L, "anna@test.pl"));
    }

    @Test
    @DisplayName("Powinno zmienić hasło użytkownika")
    void shouldChangePassword() {
        System.out.println("Test: zmiana hasła użytkownika");

        authService.register(student);
        authService.changePassword(1L, "secret123", "newpass123");

        assertDoesNotThrow(() -> authService.login("mateusz@test.pl", "newpass123"));
    }

    @Test
    @DisplayName("Powinno zablokować zmianę hasła przy błędnym starym haśle")
    void shouldThrowExceptionWhenCurrentPasswordIsWrong() {
        System.out.println("Test: błędne stare hasło przy zmianie hasła");

        authService.register(student);

        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword(1L, "wrongpass", "newpass123"));
    }

    @Test
    @DisplayName("Powinno zablokować zmianę hasła na takie samo jak stare")
    void shouldThrowExceptionWhenNewPasswordMatchesOldOne() {
        System.out.println("Test: nowe hasło takie samo jak stare");

        authService.register(student);

        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword(1L, "secret123", "secret123"));
    }

    @Test
    @DisplayName("Powinno aktywować użytkownika po wcześniejszej dezaktywacji")
    void shouldActivateUserAfterDeactivation() {
        System.out.println("Test: aktywacja użytkownika po dezaktywacji");

        authService.register(student);
        authService.deactivateUser(1L);
        authService.activateUser(1L);

        assertTrue(authService.findById(1L).isActive());
        assertDoesNotThrow(() -> authService.login("mateusz@test.pl", "secret123"));
    }

    @Test
    @DisplayName("Powinno znaleźć użytkownika po id")
    void shouldFindUserById() {
        System.out.println("Test: wyszukiwanie użytkownika po id");

        authService.register(student);

        assertEquals(student.getId(), authService.findById(1L).getId());
    }

    @Test
    @DisplayName("Powinno znaleźć użytkownika po emailu")
    void shouldFindUserByEmail() {
        System.out.println("Test: wyszukiwanie użytkownika po emailu");

        authService.register(student);

        assertEquals(student.getId(), authService.findByEmail("mateusz@test.pl").getId());
    }

    @Test
    @DisplayName("Powinno wykonać logout bez wyjątku")
    void shouldLogoutWithoutException() {
        System.out.println("Test: wylogowanie użytkownika");

        authService.register(student);

        assertDoesNotThrow(() -> authService.logout(1L));
    }

    @Test
    @DisplayName("Powinno zwrócić użytkowników po roli")
    void shouldReturnUsersByRole() {
        System.out.println("Test: pobieranie użytkowników po roli");

        Administrator administrator = new Administrator(
                2L,
                "Anna",
                "Admin",
                "admin@test.pl",
                "admin123",
                "ADM001"
        );

        authService.register(student);
        authService.register(administrator);

        assertEquals(1, authService.getUsersByRole(student.getRole()).size());
        assertEquals(1, authService.getUsersByRole(administrator.getRole()).size());
    }
}