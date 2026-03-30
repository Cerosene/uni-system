package pl.usos2.server.unit.model;

import org.junit.jupiter.api.Test;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;

import static org.junit.jupiter.api.Assertions.*;

class GradeTest {

    @Test
    void shouldCreateGradeWithCorrectData() {
        Lecturer lecturer = new Lecturer(
                2L, "Adam", "Profesor", "adam@test.pl", "haslo123", "p001", "dr"
        );

        Student student = new Student(
                1L, "Anna", "Nowak", "anna@test.pl", "haslo123", "s123", "Informatyka", Semester.SECOND
        );

        Course course = new Course(
                10L, "Programowanie Obiektowe", "PO123", 5, lecturer
        );

        Grade grade = new Grade(
                100L, student, course, lecturer, 4.5, "Kolokwium"
        );

        assertEquals(student, grade.getStudent());
        assertEquals(course, grade.getCourse());
        assertEquals(lecturer, grade.getLecturer());
        assertEquals(4.5, grade.getValue());
        assertEquals("Kolokwium", grade.getDescription());
    }
}