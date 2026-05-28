package pl.usos2.client.view.admin;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Employee;
import pl.usos2.server.service.admin.EmployeeService;

import java.math.BigDecimal;

public class EmployeeListView extends BorderPane {

    private final EmployeeService employeeService;
    private final TableView<Employee> table = new TableView<>();

    // Pola formularza
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField salaryField = new TextField();
    private final CheckBox activeCheckBox = new CheckBox(MockDataProvider.i18n("employee_active"));
    private final ComboBox<UserRole> roleComboBox = new ComboBox<>();

    public EmployeeListView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        setPadding(new Insets(20));

        setupTable();
        refreshTable();
        setCenter(table);
        setRight(createForm());
    }

    private void setupTable() {
        TableColumn<Employee, String> nameCol = new TableColumn<>(MockDataProvider.i18n("table_name"));
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));

        TableColumn<Employee, String> emailCol = new TableColumn<>(MockDataProvider.i18n("table_email"));
        emailCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<Employee, String> posCol = new TableColumn<>(MockDataProvider.i18n("table_position"));
        posCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPosition()));

        TableColumn<Employee, String> salaryCol = new TableColumn<>(MockDataProvider.i18n("table_salary"));
        salaryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSalary().toString() + " PLN"));

        TableColumn<Employee, String> activeCol = new TableColumn<>(MockDataProvider.i18n("table_status"));
        activeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().isActive() ? "Aktywny" : "Nieaktywny"));

        table.getColumns().addAll(nameCol, emailCol, posCol, salaryCol, activeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) fillForm(newSelection);
        });
    }

    private VBox createForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(0, 0, 0, 20));
        form.setPrefWidth(300);

        roleComboBox.setItems(FXCollections.observableArrayList(UserRole.ADMINISTRATOR, UserRole.LECTURER));
        roleComboBox.setPromptText(MockDataProvider.i18n("select_role"));
        roleComboBox.setMaxWidth(Double.MAX_VALUE);

        Button saveBtn = new Button(MockDataProvider.i18n("btn_save"));
        saveBtn.setOnAction(e -> handleUpdate());
        Button deleteBtn = new Button(MockDataProvider.i18n("btn_delete"));
        deleteBtn.setOnAction(e -> handleRemove());
        Button clearBtn = new Button(MockDataProvider.i18n("btn_clear"));
        clearBtn.setOnAction(e -> clearForm());

        form.getChildren().addAll(
                new Label(MockDataProvider.i18n("field_name")), firstNameField,
                new Label(MockDataProvider.i18n("field_surname")), lastNameField,
                new Label(MockDataProvider.i18n("field_email")), emailField,
                new Label(MockDataProvider.i18n("field_salary")), salaryField,
                new Label(MockDataProvider.i18n("field_role")), roleComboBox,
                activeCheckBox,
                saveBtn, clearBtn, deleteBtn
        );
        return form;
    }

    private void handleUpdate() {
        if (firstNameField.getText().isEmpty() || emailField.getText().isEmpty() || roleComboBox.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, MockDataProvider.i18n("error_fields_required")).show();
            return;
        }

        Employee selected = table.getSelectionModel().getSelectedItem();

        try {
            BigDecimal salary = new BigDecimal(salaryField.getText());
            String roleName = roleComboBox.getValue().toString(); // Rola używana jako pozycja

            if (selected == null) {
                Employee newEmp = new Employee();
                newEmp.setId(System.currentTimeMillis() / 1000);
                newEmp.setFirstName(firstNameField.getText());
                newEmp.setLastName(lastNameField.getText());
                newEmp.setEmail(emailField.getText());
                newEmp.setPosition(roleName); // Позиция теперь равна выбранной роли
                newEmp.setSalary(salary);
                newEmp.setActive(activeCheckBox.isSelected());
                newEmp.setPassword("default123");
                newEmp.setRole(roleComboBox.getValue());
                newEmp.setEmployeeNumber("EMP-" + System.currentTimeMillis());

                employeeService.addEmployee(newEmp);
            } else {
                employeeService.updateEmployee(selected.getId(), firstNameField.getText(), lastNameField.getText(), emailField.getText(), roleName);
                employeeService.changeSalary(selected.getId(), salary);
                employeeService.setEmployeeActive(selected.getId(), activeCheckBox.isSelected());
            }

            refreshTable();
            clearForm();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, MockDataProvider.i18n("error_generic") + e.getMessage()).show();
        }
    }

    private void handleRemove() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            employeeService.removeEmployee(selected.getId());
            refreshTable();
            clearForm();
        }
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        salaryField.clear();
        roleComboBox.setValue(null);
        activeCheckBox.setSelected(true);
    }

    private void fillForm(Employee e) {
        firstNameField.setText(e.getFirstName());
        lastNameField.setText(e.getLastName());
        emailField.setText(e.getEmail());
        salaryField.setText(e.getSalary().toString());
        activeCheckBox.setSelected(e.isActive());
        roleComboBox.setValue(e.getRole());
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(employeeService.getAllEmployees()));
    }
}