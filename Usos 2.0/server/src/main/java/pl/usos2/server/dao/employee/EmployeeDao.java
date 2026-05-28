package pl.usos2.server.dao.employee;

import pl.usos2.server.model.user.Employee;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EmployeeDao {
    Employee save(Employee employee);

    Employee updateBasicData(Long employeeId, String firstName, String lastName, String email, String position);

    Employee updatePosition(Long employeeId, String position);

    Employee updateSalary(Long employeeId, BigDecimal salary);

    Employee updateEmployeeNumber(Long employeeId, String employeeNumber);

    Employee updateActive(Long employeeId, boolean active);

    void deleteById(Long employeeId);

    Optional<Employee> findById(Long employeeId);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    List<Employee> findAll();

    List<Employee> findActive();

    boolean existsById(Long employeeId);

    boolean existsByEmail(String email);

    boolean existsByEmailExcludingId(String email, Long excludedEmployeeId);

    boolean existsByEmployeeNumber(String employeeNumber);

    boolean existsByEmployeeNumberExcludingId(String employeeNumber, Long excludedEmployeeId);
}

