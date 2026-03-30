package pl.usos2.server.model.user;

import pl.usos2.server.model.enumtype.UserRole;

public class Administrator extends User {
    private String employeeNumber;

    public Administrator() {
    }

    public Administrator(Long id, String firstName, String lastName, String email, String password,
                         String employeeNumber) {
        super(id, firstName, lastName, email, password, UserRole.ADMINISTRATOR, true);
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
}