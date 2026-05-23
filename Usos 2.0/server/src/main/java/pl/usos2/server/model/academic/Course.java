package pl.usos2.server.model.academic;

import pl.usos2.server.model.base.BaseEntity;
import pl.usos2.server.model.user.Lecturer;

public class Course extends BaseEntity {

    private String name;
    private String code;
    private int ects;
    private Lecturer lecturer;

    public Course() {
    }

    public Course(Long id, String name, String code, int ects, Lecturer lecturer) {
        super(id);
        this.name = name;
        this.code = code;
        this.ects = ects;
        this.lecturer = lecturer;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getEcts() {
        return ects;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }
}