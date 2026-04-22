package pl.usos2.server.service.admin;

import pl.usos2.server.model.user.Employee;
import pl.usos2.server.util.ValidationUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class EmployeeService {
    private static final Logger logger = Logger.getLogger(EmployeeService.class.getName());

    private final List<Employee> employees = new ArrayList<>();

    public Employee addEmployee(Employee employee) {
        validateEmployee(employee);

        String normalizedEmail = ValidationUtils.normalizeEmail(employee.getEmail());
        String normalizedEmployeeNumber = ValidationUtils.normalizeText(
                employee.getEmployeeNumber(),
                "Employee number cannot be empty."
        ).toUpperCase();

        boolean emailExists = employees.stream()
                .anyMatch(existing -> existing.getEmail().equalsIgnoreCase(normalizedEmail));
        if (emailExists) {
            logger.warning("Cannot add employee. Duplicate email: " + normalizedEmail);
            throw new IllegalArgumentException("Employee with this email already exists.");
        }

        boolean numberExists = employees.stream()
                .anyMatch(existing -> existing.getEmployeeNumber().equalsIgnoreCase(normalizedEmployeeNumber));
        if (numberExists) {
            logger.warning("Cannot add employee. Duplicate employee number: " + normalizedEmployeeNumber);
            throw new IllegalArgumentException("Employee with this employee number already exists.");
        }

        employee.setFirstName(employee.getFirstName().trim());
        employee.setLastName(employee.getLastName().trim());
        employee.setEmail(normalizedEmail);
        employee.setEmployeeNumber(normalizedEmployeeNumber);
        employee.setPosition(employee.getPosition().trim());

        employees.add(employee);
        logger.info("Employee added: " + employee.getFullName());
        return employee;
    }

    public Employee updateEmployee(Long employeeId, String firstName, String lastName, String email) {
        Employee employee = findById(employeeId);
        return updateEmployee(employeeId, firstName, lastName, email, employee.getPosition());
    }

    public Employee updateEmployee(Long employeeId, String firstName, String lastName, String email, String position) {
        Employee employee = findById(employeeId);

        String normalizedEmail = ValidationUtils.normalizeEmail(email);

        boolean emailTakenByAnother = employees.stream()
                .filter(existing -> !existing.getId().equals(employeeId))
                .anyMatch(existing -> existing.getEmail().equalsIgnoreCase(normalizedEmail));

        if (emailTakenByAnother) {
            logger.warning("Cannot update employee. Email already used: " + normalizedEmail);
            throw new IllegalArgumentException("Employee with this email already exists.");
        }

        employee.setFirstName(ValidationUtils.normalizeText(firstName, "First name cannot be empty."));
        employee.setLastName(ValidationUtils.normalizeText(lastName, "Last name cannot be empty."));
        employee.setEmail(normalizedEmail);
        employee.setPosition(ValidationUtils.normalizeText(position, "Position cannot be empty."));

        logger.info("Employee updated: " + employee.getFullName());
        return employee;
    }

    public Employee changePosition(Long employeeId, String newPosition) {
        Employee employee = findById(employeeId);
        employee.setPosition(ValidationUtils.normalizeText(newPosition, "Position cannot be empty."));

        logger.info("Changed employee position for id=" + employeeId + " to " + employee.getPosition());
        return employee;
    }

    public Employee changeSalary(Long employeeId, BigDecimal newSalary) {
        Employee employee = findById(employeeId);
        ValidationUtils.requirePositive(newSalary, "Salary must be greater than zero.");

        employee.setSalary(newSalary);
        logger.info("Changed employee salary for id=" + employeeId);
        return employee;
    }

    public Employee changeEmployeeNumber(Long employeeId, String newEmployeeNumber) {
        Employee employee = findById(employeeId);
        String normalizedEmployeeNumber = ValidationUtils.normalizeText(
                newEmployeeNumber,
                "Employee number cannot be empty."
        ).toUpperCase();

        boolean numberTaken = employees.stream()
                .filter(existing -> !existing.getId().equals(employeeId))
                .anyMatch(existing -> existing.getEmployeeNumber().equalsIgnoreCase(normalizedEmployeeNumber));

        if (numberTaken) {
            logger.warning("Cannot update employee number. Number already exists: " + normalizedEmployeeNumber);
            throw new IllegalArgumentException("Employee with this employee number already exists.");
        }

        employee.setEmployeeNumber(normalizedEmployeeNumber);
        logger.info("Changed employee number for id=" + employeeId);
        return employee;
    }

    public Employee setEmployeeActive(Long employeeId, boolean active) {
        Employee employee = findById(employeeId);
        employee.setActive(active);

        logger.info("Changed employee active state for id=" + employeeId + " to " + active);
        return employee;
    }

    public Employee findById(Long employeeId) {
        ValidationUtils.requireNotNull(employeeId, "Employee id cannot be null.");

        Optional<Employee> employeeOptional = employees.stream()
                .filter(employee -> employeeId.equals(employee.getId()))
                .findFirst();

        if (employeeOptional.isEmpty()) {
            throw new IllegalArgumentException("Employee not found.");
        }

        return employeeOptional.get();
    }

    public Employee findByEmail(String email) {
        String normalizedEmail = ValidationUtils.normalizeEmail(email);

        return employees.stream()
                .filter(employee -> employee.getEmail().equalsIgnoreCase(normalizedEmail))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
    }

    public Employee findByEmployeeNumber(String employeeNumber) {
        String normalizedEmployeeNumber = ValidationUtils.normalizeText(
                employeeNumber,
                "Employee number cannot be empty."
        ).toUpperCase();

        return employees.stream()
                .filter(employee -> employee.getEmployeeNumber().equalsIgnoreCase(normalizedEmployeeNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
    }

    public void removeEmployee(Long employeeId) {
        Employee employee = findById(employeeId);
        employees.remove(employee);
        logger.info("Employee removed: id=" + employeeId);
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    public List<Employee> getActiveEmployees() {
        return employees.stream()
                .filter(Employee::isActive)
                .toList();
    }

    private void validateEmployee(Employee employee) {
        ValidationUtils.requireNotNull(employee, "Employee cannot be null.");
        ValidationUtils.requireNotNull(employee.getId(), "Employee id cannot be null.");
        ValidationUtils.requireNotBlank(employee.getFirstName(), "First name cannot be empty.");
        ValidationUtils.requireNotBlank(employee.getLastName(), "Last name cannot be empty.");
        ValidationUtils.requireValidEmail(employee.getEmail(), "Email has invalid format.");
        ValidationUtils.requireMinLength(employee.getPassword(), 6, "Password must have at least 6 characters.");
        ValidationUtils.requireNotBlank(employee.getEmployeeNumber(), "Employee number cannot be empty.");
        ValidationUtils.requireNotBlank(employee.getPosition(), "Position cannot be empty.");
        ValidationUtils.requirePositive(employee.getSalary(), "Salary must be greater than zero.");
        ValidationUtils.requireNotNull(employee.getRole(), "Employee role cannot be null.");
        boolean idExists = employees.stream()
                .anyMatch(existing -> existing.getId().equals(employee.getId()));

        if (idExists) {
            logger.warning("Cannot add employee. Duplicate employee id: " + employee.getId());
            throw new IllegalArgumentException("Employee with this id already exists.");
        }
    }
}