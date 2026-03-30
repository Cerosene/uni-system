package pl.usos2.server.model.academic;

import pl.usos2.server.model.base.BaseEntity;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentGroup extends BaseEntity {
    private String name;
    private Course course;
    private Lecturer lecturer;
    private final List<Student> students = new ArrayList<>();

    public StudentGroup() {
    }

    public StudentGroup(Long id, String name, Course course, Lecturer lecturer) {
        super(id);
        this.name = name;
        this.course = course;
        this.lecturer = lecturer;
    }

    public String getName() {
        return name;
    }

    public Course getCourse() {
        return course;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    public void addStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        students.add(student);
    }

    public void removeStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        students.remove(student);
    }
}