package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.Grade;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.service.grade.GradeService;

import static org.junit.jupiter.api.Assertions.*;

class GradeServiceTest {

    private GradeService gradeService;
    private Student student;
    private Lecturer lecturer;
    private Course course;

    @BeforeEach
    void setUp() {
        gradeService = new GradeService();

        student = new Student(1L, "Anna", "Nowak", "anna@test.pl", "haslo123", "s111", "Informatyka", Semester.SECOND);
        lecturer = new Lecturer(2L, "Adam", "Profesor", "adam@test.pl", "haslo123", "p001", "dr");
        course = new Course(1L, "Programowanie Obiektowe", "PO123", 5, lecturer);
    }

    @Test
    void shouldThrowExceptionWhenStudentIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> gradeService.addGrade(null, course, lecturer, 4.0, "Kolokwium"));
    }

    @Test
    void shouldAddGradeCorrectly() {
        Grade grade = gradeService.addGrade(student, course, lecturer, 4.5, "Kolokwium");

        assertNotNull(grade);
        assertEquals(4.5, grade.getValue());
    }

    @Test
    void shouldThrowExceptionForInvalidGradeValue() {
        assertThrows(IllegalArgumentException.class,
                () -> gradeService.addGrade(student, course, lecturer, 6.0, "Bledna"));
    }
}