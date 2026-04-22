package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

        student = new Student(
                1L,
                "Anna",
                "Nowak",
                "anna@test.pl",
                "haslo123",
                "s111",
                "Informatyka",
                Semester.SECOND
        );

        lecturer = new Lecturer(
                2L,
                "Adam",
                "Profesor",
                "adam@test.pl",
                "haslo123",
                "P001",
                "dr"
        );

        course = new Course(
                1L,
                "Programowanie Obiektowe",
                "PO123",
                5,
                lecturer
        );
    }

    @Test
    @DisplayName("Powinno dodać ocenę z wygenerowanym id")
    void shouldAddGradeWithGeneratedId() {
        System.out.println("Test: dodawanie oceny z generowanym id");

        Grade grade = gradeService.addGrade(student, course, lecturer, 4.5, "Kolokwium");

        assertNotNull(grade.getId());
        assertEquals(4.5, grade.getValue());
    }

    @Test
    @DisplayName("Powinno zaktualizować ocenę")
    void shouldUpdateGrade() {
        System.out.println("Test: aktualizacja oceny");

        Grade grade = gradeService.addGrade(student, course, lecturer, 4.0, "Kolokwium");
        gradeService.updateGrade(grade, 5.0, "Poprawa");

        assertEquals(5.0, grade.getValue());
        assertEquals("Poprawa", grade.getDescription());
    }

    @Test
    @DisplayName("Powinno zablokować ocenę o niepoprawnej wartości")
    void shouldThrowExceptionForInvalidGradeValue() {
        System.out.println("Test: blokada niepoprawnej wartości oceny");

        assertThrows(IllegalArgumentException.class,
                () -> gradeService.addGrade(student, course, lecturer, 6.0, "Bledna"));
    }

    @Test
    @DisplayName("Powinno zablokować zduplikowaną ocenę")
    void shouldThrowExceptionForDuplicateGrade() {
        System.out.println("Test: blokada zduplikowanej oceny");

        gradeService.addGrade(student, course, lecturer, 4.5, "Kolokwium");

        assertThrows(IllegalArgumentException.class,
                () -> gradeService.addGrade(student, course, lecturer, 4.5, "Kolokwium"));
    }

    @Test
    @DisplayName("Powinno znaleźć ocenę po id")
    void shouldFindGradeById() {
        System.out.println("Test: wyszukiwanie oceny po id");

        Grade grade = gradeService.addGrade(student, course, lecturer, 4.5, "Kolokwium");

        assertEquals(grade.getId(), gradeService.findById(grade.getId()).getId());
    }

    @Test
    @DisplayName("Powinno zwrócić oceny studenta")
    void shouldReturnGradesForStudent() {
        System.out.println("Test: pobieranie ocen studenta");

        gradeService.addGrade(student, course, lecturer, 4.5, "Kolokwium");
        gradeService.addGrade(student, course, lecturer, 5.0, "Projekt");

        assertEquals(2, gradeService.getGradesForStudent(student).size());
    }

    @Test
    @DisplayName("Powinno zwrócić oceny kursu")
    void shouldReturnGradesForCourse() {
        System.out.println("Test: pobieranie ocen kursu");

        gradeService.addGrade(student, course, lecturer, 4.5, "Kolokwium");
        gradeService.addGrade(student, course, lecturer, 5.0, "Projekt");

        assertEquals(2, gradeService.getGradesForCourse(course).size());
    }

    @Test
    @DisplayName("Powinno zwrócić oceny prowadzącego")
    void shouldReturnGradesForLecturer() {
        System.out.println("Test: pobieranie ocen dla prowadzącego");

        gradeService.addGrade(student, course, lecturer, 4.5, "Kolokwium");
        gradeService.addGrade(student, course, lecturer, 5.0, "Projekt");

        assertEquals(2, gradeService.getGradesForLecturer(lecturer).size());
    }

    @Test
    @DisplayName("Powinno obliczyć średnią ocen studenta")
    void shouldCalculateAverageGradeForStudent() {
        System.out.println("Test: obliczanie średniej ocen studenta");

        gradeService.addGrade(student, course, lecturer, 4.0, "Kolokwium");
        gradeService.addGrade(student, course, lecturer, 5.0, "Projekt");

        assertEquals(4.5, gradeService.getAverageGradeForStudent(student));
    }

    @Test
    @DisplayName("Powinno zwrócić średnią 0.0 gdy student nie ma ocen")
    void shouldReturnZeroAverageWhenStudentHasNoGrades() {
        System.out.println("Test: średnia 0.0 dla studenta bez ocen");

        assertEquals(0.0, gradeService.getAverageGradeForStudent(student));
    }
}