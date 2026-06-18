package pl.usos2.client.view.admin;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Employee;
import pl.usos2.server.service.admin.EmployeeService;
import pl.usos2.client.util.ErrorDialogUtil;

import java.math.BigDecimal;

public class EmployeeListView extends BorderPane {

    private final EmployeeService employeeService;
    private final TableView<Employee> table = new TableView<>();

    // Pola formularza
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField salaryField = new TextField();
    private final CheckBox activeCheckBox = new CheckBox();
    private final ComboBox<UserRole> roleComboBox = new ComboBox<>();

    private final TableColumn<Employee, String> nameCol = new TableColumn<>();
    private final TableColumn<Employee, String> emailCol = new TableColumn<>();
    private final TableColumn<Employee, String> posCol = new TableColumn<>();
    private final TableColumn<Employee, String> salaryCol = new TableColumn<>();
    private final TableColumn<Employee, String> activeCol = new TableColumn<>();

    private final Label nameLabel = new Label();
    private final Label surnameLabel = new Label();
    private final Label emailLabel = new Label();
    private final Label salaryLabel = new Label();
    private final Label roleLabel = new Label();

    private final Button saveBtn = new Button();
    private final Button deleteBtn = new Button();
    private final Button clearBtn = new Button();

    public EmployeeListView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        setPadding(new Insets(20));

        setupTable();
        refreshTable();
        setCenter(table);
        setRight(createForm());

        refreshLocalization();
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    private void setupTable() {
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));

        emailCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        posCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPosition()));

        salaryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSalary().toString() + " PLN"));

        activeCol.setCellValueFactory(data -> {
            boolean active = data.getValue().isActive();
            String text = isEnglish()
                    ? (active ? "Active" : "Inactive")
                    : (active ? "Aktywny" : "Nieaktywny");
            return new javafx.beans.property.SimpleStringProperty(text);
        });

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
        roleComboBox.setMaxWidth(Double.MAX_VALUE);

        saveBtn.setOnAction(e -> handleUpdate());
        deleteBtn.setOnAction(e -> handleRemove());
        clearBtn.setOnAction(e -> clearForm());

        form.getChildren().addAll(
                nameLabel, firstNameField,
                surnameLabel, lastNameField,
                emailLabel, emailField,
                salaryLabel, salaryField,
                roleLabel, roleComboBox,
                activeCheckBox,
                saveBtn, clearBtn, deleteBtn
        );
        return form;
    }

    private void handleUpdate() {
      
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorDialogUtil.showWarning("WARNING", MockDataProvider.i18n("error_select_employee"));
            return;
        }

        try {
            BigDecimal salary = new BigDecimal(salaryField.getText());
            String roleName = roleComboBox.getValue().toString(); 

          
            employeeService.updateEmployee(selected.getId(), firstNameField.getText(), lastNameField.getText(), emailField.getText(), roleName);
            employeeService.changeSalary(selected.getId(), salary);
           
            employeeService.setEmployeeActive(selected.getId(), activeCheckBox.isSelected()); 

            refreshTable();
           
        } catch (Exception e) {
            ErrorDialogUtil.showError("ERROR", MockDataProvider.i18n("error_generic") + e.getMessage());
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

    private boolean isEnglish() {
        return "en".equalsIgnoreCase(MockDataProvider.getCurrentLocale().getLanguage());
    }

    private void refreshLocalization() {
        nameCol.setText(MockDataProvider.i18n("table_name"));
        emailCol.setText(MockDataProvider.i18n("table_email"));
        posCol.setText(MockDataProvider.i18n("table_position"));
        salaryCol.setText(MockDataProvider.i18n("table_salary"));
        activeCol.setText(MockDataProvider.i18n("table_status"));

        nameLabel.setText(MockDataProvider.i18n("field_name"));
        surnameLabel.setText(MockDataProvider.i18n("field_surname"));
        emailLabel.setText(MockDataProvider.i18n("field_email"));
        salaryLabel.setText(MockDataProvider.i18n("field_salary"));
        roleLabel.setText(MockDataProvider.i18n("field_role"));

        saveBtn.setText(MockDataProvider.i18n("btn_save"));
        deleteBtn.setText(MockDataProvider.i18n("btn_delete"));
        clearBtn.setText(MockDataProvider.i18n("btn_clear"));

        activeCheckBox.setText(MockDataProvider.i18n("employee_active"));
        roleComboBox.setPromptText(MockDataProvider.i18n("select_role"));

        table.refresh();
    }
}