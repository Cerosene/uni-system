package pl.usos2.server.model.user;

import pl.usos2.server.model.enumtype.UserRole;

public class Lecturer extends User {
    private String employeeNumber;
    private String academicTitle;

    public Lecturer() {
    }

    public Lecturer(Long id, String firstName, String lastName, String email, String password,
                    String employeeNumber, String academicTitle) {
        super(id, firstName, lastName, email, password, UserRole.LECTURER, true);
        this.employeeNumber = employeeNumber;
        this.academicTitle = academicTitle;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getAcademicTitle() {
        return academicTitle;
    }

    public void setAcademicTitle(String academicTitle) {
        this.academicTitle = academicTitle;
    }
}