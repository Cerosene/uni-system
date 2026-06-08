package pl.usos2.client.service.remote;

import pl.usos2.client.network.ApiClient;
import pl.usos2.client.session.ClientSession;
import pl.usos2.server.model.user.Employee;
import pl.usos2.server.network.protocol.ApiAction;
import pl.usos2.server.service.admin.EmployeeService;

import java.math.BigDecimal;
import java.util.List;

public class RemoteEmployeeService extends EmployeeService {
    private final ApiClient apiClient;
    private final ClientSession session;

    public RemoteEmployeeService(ApiClient apiClient, ClientSession session) {
        super();
        this.apiClient = apiClient;
        this.session = session;
    }

    @Override
    public Employee addEmployee(Employee employee) {
        return (Employee) apiClient.send(ApiAction.EMPLOYEE_ADD, session.getToken(), apiClient.payload("employee", employee));
    }

    @Override
    public Employee updateEmployee(Long employeeId, String firstName, String lastName, String email) {
        return updateEmployee(employeeId, firstName, lastName, email, "Pracownik");
    }

    @Override
    public Employee updateEmployee(Long employeeId, String firstName, String lastName, String email, String position) {
        return (Employee) apiClient.send(ApiAction.EMPLOYEE_UPDATE, session.getToken(), apiClient.payload(
                "employeeId", employeeId, "firstName", firstName, "lastName", lastName, "email", email, "position", position
        ));
    }

    @Override
    public Employee changePosition(Long employeeId, String newPosition) {
        return (Employee) apiClient.send(ApiAction.EMPLOYEE_CHANGE_POSITION, session.getToken(), apiClient.payload(
                "employeeId", employeeId, "position", newPosition
        ));
    }

    @Override
    public Employee changeSalary(Long employeeId, BigDecimal newSalary) {
        return (Employee) apiClient.send(ApiAction.EMPLOYEE_CHANGE_SALARY, session.getToken(), apiClient.payload(
                "employeeId", employeeId, "salary", newSalary
        ));
    }

    @Override
    public Employee changeEmployeeNumber(Long employeeId, String newEmployeeNumber) {
        return (Employee) apiClient.send(ApiAction.EMPLOYEE_CHANGE_NUMBER, session.getToken(), apiClient.payload(
                "employeeId", employeeId, "employeeNumber", newEmployeeNumber
        ));
    }

    @Override
    public Employee setEmployeeActive(Long employeeId, boolean active) {
        return (Employee) apiClient.send(ApiAction.EMPLOYEE_SET_ACTIVE, session.getToken(), apiClient.payload(
                "employeeId", employeeId, "active", active
        ));
    }

    @Override
    public Employee findById(Long employeeId) {
        return (Employee) apiClient.send(ApiAction.EMPLOYEE_FIND_BY_ID, session.getToken(), apiClient.payload("employeeId", employeeId));
    }

    @Override
    public Employee findByEmail(String email) {
        return (Employee) apiClient.send(ApiAction.EMPLOYEE_FIND_BY_EMAIL, session.getToken(), apiClient.payload("email", email));
    }

    @Override
    public Employee findByEmployeeNumber(String employeeNumber) {
        return (Employee) apiClient.send(ApiAction.EMPLOYEE_FIND_BY_NUMBER, session.getToken(), apiClient.payload("employeeNumber", employeeNumber));
    }

    @Override
    public void removeEmployee(Long employeeId) {
        apiClient.send(ApiAction.EMPLOYEE_REMOVE, session.getToken(), apiClient.payload("employeeId", employeeId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Employee> getAllEmployees() {
        return (List<Employee>) apiClient.send(ApiAction.EMPLOYEE_LIST_ALL, session.getToken());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Employee> getActiveEmployees() {
        return (List<Employee>) apiClient.send(ApiAction.EMPLOYEE_LIST_ACTIVE, session.getToken());
    }
}
