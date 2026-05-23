package pl.usos2.server.unit.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Employee;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeTest {

    @Test
    @DisplayName("Powinno utworzyć pracownika z poprawnymi danymi")
    void shouldCreateEmployeeWithCorrectData() {
        System.out.println("Test: tworzenie pracownika z poprawnymi danymi");

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

        assertEquals(1L, employee.getId());
        assertEquals("Jan", employee.getFirstName());
        assertEquals("Kowalski", employee.getLastName());
        assertEquals("jan@test.pl", employee.getEmail());
        assertEquals("haslo123", employee.getPassword());
        assertEquals("EMP001", employee.getEmployeeNumber());
        assertEquals("Dean Office Clerk", employee.getPosition());
        assertEquals(new BigDecimal("5500.00"), employee.getSalary());
        assertEquals(UserRole.ADMINISTRATOR, employee.getRole());
        assertTrue(employee.isActive());
    }

    @Test
    @DisplayName("Powinno zaktualizować dane pracownika przez settery")
    void shouldUpdateEmployeeUsingSetters() {
        System.out.println("Test: aktualizacja danych pracownika setterami");

        Employee employee = new Employee();
        employee.setId(2L);
        employee.setFirstName("Adam");
        employee.setLastName("Nowak");
        employee.setEmail("adam@test.pl");
        employee.setPassword("secret123");
        employee.setActive(true);
        employee.setEmployeeNumber("EMP999");
        employee.setPosition("Specjalista");
        employee.setSalary(new BigDecimal("7000.00"));
        employee.setRole(UserRole.LECTURER);

        assertEquals(2L, employee.getId());
        assertEquals("Adam", employee.getFirstName());
        assertEquals("Nowak", employee.getLastName());
        assertEquals("adam@test.pl", employee.getEmail());
        assertEquals("secret123", employee.getPassword());
        assertEquals("EMP999", employee.getEmployeeNumber());
        assertEquals("Specjalista", employee.getPosition());
        assertEquals(new BigDecimal("7000.00"), employee.getSalary());
        assertEquals(UserRole.LECTURER, employee.getRole());
    }

    @Test
    @DisplayName("Powinno zwrócić pełne imię i nazwisko pracownika")
    void shouldReturnEmployeeFullName() {
        System.out.println("Test: pobieranie pełnego imienia i nazwiska pracownika");

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

        assertEquals("Jan Kowalski", employee.getFullName());
    }
}