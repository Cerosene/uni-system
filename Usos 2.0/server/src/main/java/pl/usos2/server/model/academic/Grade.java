package pl.usos2.server.model.academic;

import pl.usos2.server.model.base.BaseEntity;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;

public class Grade extends BaseEntity {
    private Student student;
    private Course course;
    private Lecturer lecturer;
    private Double value;
    private String description;

    public Grade() {
    }

    public Grade(Long id, Student student, Course course, Lecturer lecturer, Double value, String description) {
        super(id);
        this.student = student;
        this.course = course;
        this.lecturer = lecturer;
        this.value = value;
        this.description = description;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public Double getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}