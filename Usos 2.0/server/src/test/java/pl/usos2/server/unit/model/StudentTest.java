package pl.usos2.server.unit.model;

import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Student;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    @Test
    void shouldCreateStudentWithCorrectData() {
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

        assertEquals("Mateusz", student.getFirstName());
        assertEquals("Lewandowski", student.getLastName());
        assertEquals(UserRole.STUDENT, student.getRole());
        assertEquals("s12345", student.getStudentNumber());
        assertEquals(Semester.THIRD, student.getSemester());
    }

    @Test
    void shouldReturnFullName() {
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
}