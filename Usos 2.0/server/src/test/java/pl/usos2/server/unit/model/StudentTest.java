package pl.usos2.server.unit.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Student;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentTest {

    @Test
    @DisplayName("Powinno utworzyć studenta z poprawnymi danymi")
    void shouldCreateStudentWithCorrectData() {
        System.out.println("Test: tworzenie studenta z poprawnymi danymi");

        Student student = new Student(
                1L,
                "Mateusz",
                "Lewandowski",
                "mateusz@test.pl",
                "secret123",
                "s12345",
                "Informatyka",
                Semester.THIRD
        );

        assertEquals(1L, student.getId());
        assertEquals("Mateusz", student.getFirstName());
        assertEquals("Lewandowski", student.getLastName());
        assertEquals("mateusz@test.pl", student.getEmail());
        assertEquals("secret123", student.getPassword());
        assertEquals(UserRole.STUDENT, student.getRole());
        assertEquals("s12345", student.getStudentNumber());
        assertEquals("Informatyka", student.getFieldOfStudy());
        assertEquals(Semester.THIRD, student.getSemester());
    }

    @Test
    @DisplayName("Powinno zwrócić pełne imię i nazwisko studenta")
    void shouldReturnFullName() {
        System.out.println("Test: pobieranie pełnego imienia i nazwiska studenta");

        Student student = new Student(
                1L,
                "Mateusz",
                "Lewandowski",
                "mateusz@test.pl",
                "secret123",
                "s12345",
                "Informatyka",
                Semester.THIRD
        );

        assertEquals("Mateusz Lewandowski", student.getFullName());
    }

    @Test
    @DisplayName("Powinno zaktualizować dane studenta przez settery")
    void shouldUpdateStudentDataUsingSetters() {
        System.out.println("Test: aktualizacja danych studenta setterami");

        Student student = new Student();
        student.setId(2L);
        student.setFirstName("Jan");
        student.setLastName("Nowak");
        student.setEmail("jan@test.pl");
        student.setPassword("haslo123");
        student.setActive(true);
        student.setStudentNumber("s99999");
        student.setFieldOfStudy("Matematyka");
        student.setSemester(Semester.FIRST);

        assertEquals(2L, student.getId());
        assertEquals("Jan", student.getFirstName());
        assertEquals("Nowak", student.getLastName());
        assertEquals("jan@test.pl", student.getEmail());
        assertEquals("haslo123", student.getPassword());
        assertEquals("s99999", student.getStudentNumber());
        assertEquals("Matematyka", student.getFieldOfStudy());
        assertEquals(Semester.FIRST, student.getSemester());
    }
}