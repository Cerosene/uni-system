package pl.usos2.server.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Employee;
import pl.usos2.server.service.admin.EmployeeService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest {

    private EmployeeService employeeService;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
        employee = new Employee(
                1L,
                "Adam",
                "Nowak",
                "adam.nowak@test.pl",
                "secret123",
                "EMP001",
                "Specjalista",
                new BigDecimal("5500.00"),
                UserRole.LECTURER
        );
    }

    @Test
    @DisplayName("Powinno dodać pracownika")
    void shouldAddEmployee() {
        System.out.println("Test: dodawanie nowego pracownika");

        Employee saved = employeeService.addEmployee(employee);

        assertNotNull(saved);
        assertEquals(1, employeeService.getAllEmployees().size());
    }

    @Test
    @DisplayName("Powinno zablokować dodanie pracownika z duplikatem id")
    void shouldThrowExceptionWhenEmployeeIdAlreadyExists() {
        System.out.println("Test: blokada duplikatu id pracownika");

        employeeService.addEmployee(employee);

        Employee second = new Employee(
                1L,
                "Jan",
                "Kowalski",
                "jan.kowalski@test.pl",
                "secret999",
                "EMP002",
                "Asystent",
                new BigDecimal("4500.00"),
                UserRole.LECTURER
        );

        assertThrows(IllegalArgumentException.class, () -> employeeService.addEmployee(second));
    }

    @Test
    @DisplayName("Powinno zablokować dodanie pracownika z duplikatem emaila")
    void shouldThrowExceptionWhenEmployeeEmailAlreadyExists() {
        System.out.println("Test: blokada duplikatu email pracownika");

        employeeService.addEmployee(employee);

        Employee second = new Employee(
                2L,
                "Jan",
                "Kowalski",
                "adam.nowak@test.pl",
                "secret999",
                "EMP002",
                "Asystent",
                new BigDecimal("4500.00"),
                UserRole.LECTURER
        );

        assertThrows(IllegalArgumentException.class, () -> employeeService.addEmployee(second));
    }

    @Test
    @DisplayName("Powinno zablokować dodanie pracownika z duplikatem numeru")
    void shouldThrowExceptionWhenEmployeeNumberAlreadyExists() {
        System.out.println("Test: blokada duplikatu numeru pracownika");

        employeeService.addEmployee(employee);

        Employee second = new Employee(
                2L,
                "Jan",
                "Kowalski",
                "jan.kowalski@test.pl",
                "secret999",
                "EMP001",
                "Asystent",
                new BigDecimal("4500.00"),
                UserRole.LECTURER
        );

        assertThrows(IllegalArgumentException.class, () -> employeeService.addEmployee(second));
    }

    @Test
    @DisplayName("Powinno zaktualizować dane pracownika")
    void shouldUpdateEmployee() {
        System.out.println("Test: aktualizacja danych pracownika");

        employeeService.addEmployee(employee);
        employeeService.updateEmployee(1L, "Jan", "Kowalski", "jan@test.pl", "Starszy specjalista");

        Employee updated = employeeService.findById(1L);

        assertEquals("Jan", updated.getFirstName());
        assertEquals("Kowalski", updated.getLastName());
        assertEquals("jan@test.pl", updated.getEmail());
        assertEquals("Starszy specjalista", updated.getPosition());
    }

    @Test
    @DisplayName("Powinno zmienić stanowisko pracownika")
    void shouldChangePosition() {
        System.out.println("Test: zmiana stanowiska pracownika");

        employeeService.addEmployee(employee);
        employeeService.changePosition(1L, "Kierownik");

        assertEquals("Kierownik", employeeService.findById(1L).getPosition());
    }

    @Test
    @DisplayName("Powinno zmienić pensję pracownika")
    void shouldChangeSalary() {
        System.out.println("Test: zmiana pensji pracownika");

        employeeService.addEmployee(employee);
        employeeService.changeSalary(1L, new BigDecimal("7000.00"));

        assertEquals(new BigDecimal("7000.00"), employeeService.findById(1L).getSalary());
    }

    @Test
    @DisplayName("Powinno zablokować ustawienie niepoprawnej pensji")
    void shouldThrowExceptionWhenSalaryIsNotPositive() {
        System.out.println("Test: blokada niepoprawnej pensji");

        employeeService.addEmployee(employee);

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.changeSalary(1L, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Powinno zmienić numer pracownika")
    void shouldChangeEmployeeNumber() {
        System.out.println("Test: zmiana numeru pracownika");

        employeeService.addEmployee(employee);
        employeeService.changeEmployeeNumber(1L, "EMP999");

        assertEquals("EMP999", employeeService.findById(1L).getEmployeeNumber());
    }

    @Test
    @DisplayName("Powinno zablokować zmianę numeru pracownika na zajęty")
    void shouldThrowExceptionWhenChangingEmployeeNumberToExistingOne() {
        System.out.println("Test: blokada zmiany numeru pracownika na zajęty");

        employeeService.addEmployee(employee);

        Employee second = new Employee(
                2L,
                "Jan",
                "Kowalski",
                "jan@test.pl",
                "secret999",
                "EMP002",
                "Asystent",
                new BigDecimal("4500.00"),
                UserRole.LECTURER
        );
        employeeService.addEmployee(second);

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.changeEmployeeNumber(2L, "EMP001"));
    }

    @Test
    @DisplayName("Powinno dezaktywować pracownika")
    void shouldDeactivateEmployee() {
        System.out.println("Test: dezaktywacja pracownika");

        employeeService.addEmployee(employee);
        employeeService.setEmployeeActive(1L, false);

        assertFalse(employeeService.findById(1L).isActive());
    }

    @Test
    @DisplayName("Powinno aktywować wcześniej dezaktywowanego pracownika")
    void shouldActivateEmployee() {
        System.out.println("Test: aktywacja pracownika");

        employeeService.addEmployee(employee);
        employeeService.setEmployeeActive(1L, false);
        employeeService.setEmployeeActive(1L, true);

        assertTrue(employeeService.findById(1L).isActive());
    }

    @Test
    @DisplayName("Powinno znaleźć pracownika po id")
    void shouldFindEmployeeById() {
        System.out.println("Test: wyszukiwanie pracownika po id");

        employeeService.addEmployee(employee);

        assertEquals(employee.getId(), employeeService.findById(1L).getId());
    }

    @Test
    @DisplayName("Powinno znaleźć pracownika po emailu")
    void shouldFindEmployeeByEmail() {
        System.out.println("Test: wyszukiwanie pracownika po emailu");

        employeeService.addEmployee(employee);

        Employee found = employeeService.findByEmail("adam.nowak@test.pl");

        assertEquals(employee.getId(), found.getId());
    }

    @Test
    @DisplayName("Powinno znaleźć pracownika po numerze pracownika")
    void shouldFindEmployeeByEmployeeNumber() {
        System.out.println("Test: wyszukiwanie pracownika po numerze");

        employeeService.addEmployee(employee);

        Employee found = employeeService.findByEmployeeNumber("EMP001");

        assertEquals(employee.getId(), found.getId());
    }

    @Test
    @DisplayName("Powinno zwrócić tylko aktywnych pracowników")
    void shouldReturnOnlyActiveEmployees() {
        System.out.println("Test: pobieranie aktywnych pracowników");

        Employee second = new Employee(
                2L,
                "Jan",
                "Kowalski",
                "jan@test.pl",
                "secret999",
                "EMP002",
                "Asystent",
                new BigDecimal("4500.00"),
                UserRole.LECTURER
        );

        employeeService.addEmployee(employee);
        employeeService.addEmployee(second);
        employeeService.setEmployeeActive(2L, false);

        assertEquals(1, employeeService.getActiveEmployees().size());
        assertEquals(1L, employeeService.getActiveEmployees().get(0).getId());
    }

    @Test
    @DisplayName("Powinno usunąć pracownika")
    void shouldRemoveEmployee() {
        System.out.println("Test: usuwanie pracownika");

        employeeService.addEmployee(employee);
        employeeService.removeEmployee(1L);

        assertEquals(0, employeeService.getAllEmployees().size());
    }

    @Test
    @DisplayName("Powinno rzucić wyjątek przy usuwaniu nieistniejącego pracownika")
    void shouldThrowExceptionWhenRemovingMissingEmployee() {
        System.out.println("Test: usuwanie nieistniejącego pracownika");

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.removeEmployee(99L));
    }
}