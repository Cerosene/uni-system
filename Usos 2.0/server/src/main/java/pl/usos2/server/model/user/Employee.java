package pl.usos2.server.model.user;

import pl.usos2.server.model.enumtype.UserRole;

import java.math.BigDecimal;

public class Employee extends User {
    private String employeeNumber;
    private String position;
    private BigDecimal salary;

    public Employee() {
    }

    public Employee(Long id,
                    String firstName,
                    String lastName,
                    String email,
                    String password,
                    String employeeNumber,
                    String position,
                    BigDecimal salary,
                    UserRole role) {
        super(id, firstName, lastName, email, password, role, true);
        this.employeeNumber = employeeNumber;
        this.position = position;
        this.salary = salary;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }
}