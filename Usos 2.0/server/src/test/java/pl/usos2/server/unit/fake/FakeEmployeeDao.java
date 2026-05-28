package pl.usos2.server.unit.fake;

import pl.usos2.server.dao.employee.EmployeeDao;
import pl.usos2.server.model.user.Employee;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeEmployeeDao implements EmployeeDao {
    private final Map<Long, Employee> employees = new ConcurrentHashMap<>();

    @Override
    public Employee save(Employee employee) {
        employees.put(employee.getId(), employee);
        return employee;
    }

    @Override
    public Employee updateBasicData(Long employeeId, String firstName, String lastName, String email, String position) {
        Employee employee = findExisting(employeeId);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPosition(position);
        return employee;
    }

    @Override
    public Employee updatePosition(Long employeeId, String position) {
        Employee employee = findExisting(employeeId);
        employee.setPosition(position);
        return employee;
    }

    @Override
    public Employee updateSalary(Long employeeId, BigDecimal salary) {
        Employee employee = findExisting(employeeId);
        employee.setSalary(salary);
        return employee;
    }

    @Override
    public Employee updateEmployeeNumber(Long employeeId, String employeeNumber) {
        Employee employee = findExisting(employeeId);
        employee.setEmployeeNumber(employeeNumber);
        return employee;
    }

    @Override
    public Employee updateActive(Long employeeId, boolean active) {
        Employee employee = findExisting(employeeId);
        employee.setActive(active);
        return employee;
    }

    @Override
    public void deleteById(Long employeeId) {
        employees.remove(employeeId);
    }

    @Override
    public Optional<Employee> findById(Long employeeId) {
        return Optional.ofNullable(employees.get(employeeId));
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        return employees.values().stream()
                .filter(employee -> employee.getEmail() != null && employee.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public Optional<Employee> findByEmployeeNumber(String employeeNumber) {
        return employees.values().stream()
                .filter(employee -> employee.getEmployeeNumber() != null && employee.getEmployeeNumber().equalsIgnoreCase(employeeNumber))
                .findFirst();
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(employees.values());
    }

    @Override
    public List<Employee> findActive() {
        return employees.values().stream()
                .filter(Employee::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long employeeId) {
        return employees.containsKey(employeeId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByEmailExcludingId(String email, Long excludedEmployeeId) {
        return employees.values().stream()
                .anyMatch(employee -> !employee.getId().equals(excludedEmployeeId)
                        && employee.getEmail() != null
                        && employee.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public boolean existsByEmployeeNumber(String employeeNumber) {
        return findByEmployeeNumber(employeeNumber).isPresent();
    }

    @Override
    public boolean existsByEmployeeNumberExcludingId(String employeeNumber, Long excludedEmployeeId) {
        return employees.values().stream()
                .anyMatch(employee -> !employee.getId().equals(excludedEmployeeId)
                        && employee.getEmployeeNumber() != null
                        && employee.getEmployeeNumber().equalsIgnoreCase(employeeNumber));
    }

    private Employee findExisting(Long employeeId) {
        Employee employee = employees.get(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found.");
        }
        return employee;
    }
}
