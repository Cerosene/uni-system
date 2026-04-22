package pl.usos2.server.unit.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.academic.Course;
import pl.usos2.server.model.academic.StudentGroup;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentGroupTest {

    @Test
    @DisplayName("Powinno dodać studenta do grupy")
    void shouldAddStudentToGroup() {
        System.out.println("Test: dodawanie studenta do grupy");

        Lecturer lecturer = new Lecturer(
                1L, "Adam", "Nowak", "adam@test.pl", "haslo123", "EMP001", "dr"
        );

        Course course = new Course(
                2L, "Programowanie Obiektowe", "PO123", 5, lecturer
        );

        StudentGroup group = new StudentGroup(
                3L, "PO-1", course, lecturer
        );

        Student student = new Student(
                4L, "Anna", "Kowalska", "anna@test.pl", "haslo123", "S001", "Informatyka", Semester.SECOND
        );

        group.addStudent(student);

        assertEquals(1, group.getStudents().size());
        assertEquals(student, group.getStudents().get(0));
    }

    @Test
    @DisplayName("Powinno usunąć studenta z grupy")
    void shouldRemoveStudentFromGroup() {
        System.out.println("Test: usuwanie studenta z grupy");

        Lecturer lecturer = new Lecturer(
                1L, "Adam", "Nowak", "adam@test.pl", "haslo123", "EMP001", "dr"
        );

        Course course = new Course(
                2L, "Programowanie Obiektowe", "PO123", 5, lecturer
        );

        StudentGroup group = new StudentGroup(
                3L, "PO-1", course, lecturer
        );

        Student student = new Student(
                4L, "Anna", "Kowalska", "anna@test.pl", "haslo123", "S001", "Informatyka", Semester.SECOND
        );

        group.addStudent(student);
        group.removeStudent(student);

        assertEquals(0, group.getStudents().size());
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek przy dodawaniu null jako studenta")
    void shouldThrowExceptionWhenAddingNullStudent() {
        System.out.println("Test: dodawanie null jako studenta do grupy");

        Lecturer lecturer = new Lecturer(
                1L, "Adam", "Nowak", "adam@test.pl", "haslo123", "EMP001", "dr"
        );

        Course course = new Course(
                2L, "Programowanie Obiektowe", "PO123", 5, lecturer
        );

        StudentGroup group = new StudentGroup(
                3L, "PO-1", course, lecturer
        );

        assertThrows(IllegalArgumentException.class, () -> group.addStudent(null));
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek przy usuwaniu null jako studenta")
    void shouldThrowExceptionWhenRemovingNullStudent() {
        System.out.println("Test: usuwanie null jako studenta z grupy");

        Lecturer lecturer = new Lecturer(
                1L, "Adam", "Nowak", "adam@test.pl", "haslo123", "EMP001", "dr"
        );

        Course course = new Course(
                2L, "Programowanie Obiektowe", "PO123", 5, lecturer
        );

        StudentGroup group = new StudentGroup(
                3L, "PO-1", course, lecturer
        );

        assertThrows(IllegalArgumentException.class, () -> group.removeStudent(null));
    }
}