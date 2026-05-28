package pl.usos2.server.service.admin;

import pl.usos2.server.dao.employee.EmployeeDao;
import pl.usos2.server.dao.employee.JdbcEmployeeDao;
import pl.usos2.server.model.user.Employee;
import pl.usos2.server.util.ValidationUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

public class EmployeeService {
    private static final Logger logger = Logger.getLogger(EmployeeService.class.getName());
    private final EmployeeDao employeeDao;

    public EmployeeService() {
        this(new JdbcEmployeeDao());
    }

    public EmployeeService(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    public Employee addEmployee(Employee employee) {
        validateEmployee(employee);

        String normalizedEmail = ValidationUtils.normalizeEmail(employee.getEmail());
        String normalizedEmployeeNumber = ValidationUtils.normalizeText(
                employee.getEmployeeNumber(),
                "Employee number cannot be empty."
        ).toUpperCase();

        boolean emailExists = employeeDao.existsByEmail(normalizedEmail);
        if (emailExists) {
            logger.warning("Cannot add employee. Duplicate email: " + normalizedEmail);
            throw new IllegalArgumentException("Employee with this email already exists.");
        }

        boolean numberExists = employeeDao.existsByEmployeeNumber(normalizedEmployeeNumber);
        if (numberExists) {
            logger.warning("Cannot add employee. Duplicate employee number: " + normalizedEmployeeNumber);
            throw new IllegalArgumentException("Employee with this employee number already exists.");
        }

        employee.setFirstName(employee.getFirstName().trim());
        employee.setLastName(employee.getLastName().trim());
        employee.setEmail(normalizedEmail);
        employee.setEmployeeNumber(normalizedEmployeeNumber);
        employee.setPosition(employee.getPosition().trim());

        Employee saved = employeeDao.save(employee);
        logger.info("Employee added: " + employee.getFullName());
        logger.info("[DIAGNOSTIC] Employee persisted in Oracle. employeeId=" + saved.getId());
        return saved;
    }

    public Employee updateEmployee(Long employeeId, String firstName, String lastName, String email) {
        Employee employee = findById(employeeId);
        return updateEmployee(employeeId, firstName, lastName, email, employee.getPosition());
    }

    public Employee updateEmployee(Long employeeId, String firstName, String lastName, String email, String position) {
        Employee employee = findById(employeeId);

        String normalizedEmail = ValidationUtils.normalizeEmail(email);

        boolean emailTakenByAnother = employeeDao.existsByEmailExcludingId(normalizedEmail, employeeId);

        if (emailTakenByAnother) {
            logger.warning("Cannot update employee. Email already used: " + normalizedEmail);
            throw new IllegalArgumentException("Employee with this email already exists.");
        }

        Employee updated = employeeDao.updateBasicData(
                employeeId,
                ValidationUtils.normalizeText(firstName, "First name cannot be empty."),
                ValidationUtils.normalizeText(lastName, "Last name cannot be empty."),
                normalizedEmail,
                ValidationUtils.normalizeText(position, "Position cannot be empty.")
        );

        logger.info("Employee updated: " + updated.getFullName());
        logger.info("[DIAGNOSTIC] Employee basic data updated in Oracle. employeeId=" + updated.getId());
        return updated;
    }

    public Employee changePosition(Long employeeId, String newPosition) {
        Employee employee = employeeDao.updatePosition(
                employeeId,
                ValidationUtils.normalizeText(newPosition, "Position cannot be empty.")
        );

        logger.info("Changed employee position for id=" + employeeId + " to " + employee.getPosition());
        logger.info("[DIAGNOSTIC] Employee position updated in Oracle. employeeId=" + employeeId);
        return employee;
    }

    public Employee changeSalary(Long employeeId, BigDecimal newSalary) {
        ValidationUtils.requirePositive(newSalary, "Salary must be greater than zero.");

        Employee employee = employeeDao.updateSalary(employeeId, newSalary);
        logger.info("Changed employee salary for id=" + employeeId);
        logger.info("[DIAGNOSTIC] Employee salary updated in Oracle. employeeId=" + employeeId);
        return employee;
    }

    public Employee changeEmployeeNumber(Long employeeId, String newEmployeeNumber) {
        findById(employeeId);
        String normalizedEmployeeNumber = ValidationUtils.normalizeText(
                newEmployeeNumber,
                "Employee number cannot be empty."
        ).toUpperCase();

        boolean numberTaken = employeeDao.existsByEmployeeNumberExcludingId(normalizedEmployeeNumber, employeeId);

        if (numberTaken) {
            logger.warning("Cannot update employee number. Number already exists: " + normalizedEmployeeNumber);
            throw new IllegalArgumentException("Employee with this employee number already exists.");
        }

        Employee employee = employeeDao.updateEmployeeNumber(employeeId, normalizedEmployeeNumber);
        logger.info("Changed employee number for id=" + employeeId);
        logger.info("[DIAGNOSTIC] Employee number updated in Oracle. employeeId=" + employeeId);
        return employee;
    }

    public Employee setEmployeeActive(Long employeeId, boolean active) {
        Employee employee = employeeDao.updateActive(employeeId, active);

        logger.info("Changed employee active state for id=" + employeeId + " to " + active);
        logger.info("[DIAGNOSTIC] Employee active flag updated in Oracle. employeeId=" + employeeId);
        return employee;
    }

    public Employee findById(Long employeeId) {
        ValidationUtils.requireNotNull(employeeId, "Employee id cannot be null.");

        Employee employee = employeeDao.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        logger.info("[DIAGNOSTIC] Employee loaded from Oracle. employeeId=" + employeeId);
        return employee;
    }

    public Employee findByEmail(String email) {
        String normalizedEmail = ValidationUtils.normalizeEmail(email);

        Employee employee = employeeDao.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        logger.info("[DIAGNOSTIC] Employee loaded by email from Oracle. email=" + normalizedEmail);
        return employee;
    }

    public Employee findByEmployeeNumber(String employeeNumber) {
        String normalizedEmployeeNumber = ValidationUtils.normalizeText(
                employeeNumber,
                "Employee number cannot be empty."
        ).toUpperCase();

        Employee employee = employeeDao.findByEmployeeNumber(normalizedEmployeeNumber)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        logger.info("[DIAGNOSTIC] Employee loaded by number from Oracle. employeeNumber=" + normalizedEmployeeNumber);
        return employee;
    }

    public void removeEmployee(Long employeeId) {
        findById(employeeId);
        employeeDao.deleteById(employeeId);
        logger.info("Employee removed: id=" + employeeId);
        logger.info("[DIAGNOSTIC] Employee removed from Oracle. employeeId=" + employeeId);
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = employeeDao.findAll();
        logger.info("[DIAGNOSTIC] All employees loaded from Oracle. count=" + employees.size());
        return employees;
    }

    public List<Employee> getActiveEmployees() {
        List<Employee> employees = employeeDao.findActive();
        logger.info("[DIAGNOSTIC] Active employees loaded from Oracle. count=" + employees.size());
        return employees;
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
        boolean idExists = employeeDao.existsById(employee.getId());

        if (idExists) {
            logger.warning("Cannot add employee. Duplicate employee id: " + employee.getId());
            throw new IllegalArgumentException("Employee with this id already exists.");
        }
    }
}
