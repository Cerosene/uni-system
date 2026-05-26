package pl.usos2.server.model.user;

import pl.usos2.server.model.enumtype.UserRole;

import java.math.BigDecimal;

public class Administrator extends Employee {

    public Administrator() {
    }

    public Administrator(Long id,
                         String firstName,
                         String lastName,
                         String email,
                         String password,
                         String employeeNumber) {
        super(
                id,
                firstName,
                lastName,
                email,
                password,
                employeeNumber,
                "Administrator",
                new BigDecimal("6000.00"),
                UserRole.ADMINISTRATOR
        );
    }
}