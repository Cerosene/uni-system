package pl.usos2.server.model.user;

import pl.usos2.server.model.enumtype.UserRole;

import java.math.BigDecimal;

public class Lecturer extends Employee {
    private String academicTitle;

    public Lecturer() {
    }

    public Lecturer(Long id,
                    String firstName,
                    String lastName,
                    String email,
                    String password,
                    String employeeNumber,
                    String academicTitle) {
        super(
                id,
                firstName,
                lastName,
                email,
                password,
                employeeNumber,
                "Lecturer",
                new BigDecimal("7000.00"),
                UserRole.LECTURER
        );
        this.academicTitle = academicTitle;
    }

    public String getAcademicTitle() {
        return academicTitle;
    }

    public void setAcademicTitle(String academicTitle) {
        this.academicTitle = academicTitle;
    }
}