package pl.usos2.server.unit.model;

import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Employee;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeTest {

    @Test
    void shouldCreateEmployeeWithCorrectData() {
        Employee employee = new Employee(
                1L,
                "Jan",
                "Kowalski",
                "jan@test.pl",
                "haslo123",
                "EMP001",
                "Dean Office Clerk",
                new BigDecimal("5500.00"),
                UserRole.ADMINISTRATOR
        );

        assertEquals("Jan", employee.getFirstName());
        assertEquals("Kowalski", employee.getLastName());
        assertEquals("EMP001", employee.getEmployeeNumber());
        assertEquals("Dean Office Clerk", employee.getPosition());
        assertEquals(new BigDecimal("5500.00"), employee.getSalary());
        assertEquals(UserRole.ADMINISTRATOR, employee.getRole());
    }
}