package pl.usos2.server.model.user;

import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.enumtype.UserRole;

public class Student extends User {
    private String studentNumber;
    private String fieldOfStudy;
    private Semester semester;

    public Student() {
    }

    public Student(Long id, String firstName, String lastName, String email, String password,
                   String studentNumber, String fieldOfStudy, Semester semester) {
        super(id, firstName, lastName, email, password, UserRole.STUDENT, true);
        this.studentNumber = studentNumber;
        this.fieldOfStudy = fieldOfStudy;
        this.semester = semester;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }
}