package pl.usos2.client.view.admin;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.user.Employee;
import pl.usos2.server.service.admin.EmployeeService;

import java.math.BigDecimal;

/**
 * Widok zarządzania pracownikami wykorzystujący bezpośrednio EmployeeService.
 */
public class EmployeeListView extends BorderPane {

    private final EmployeeService employeeService;
    private final TableView<Employee> table = new TableView<>();

    // Pola formularza
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField positionField = new TextField();
    private final TextField salaryField = new TextField();
    private final CheckBox activeCheckBox = new CheckBox(MockDataProvider.i18n("employee_active"));

    public EmployeeListView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        setPadding(new Insets(20));

        setupTable();
        refreshTable();
        setCenter(table);
        setRight(createForm());
    }

    private void setupTable() {
        // 1. Imię i Nazwisko
        TableColumn<Employee, String> nameCol = new TableColumn<>("Imię i Nazwisko");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));

        // 2. Email
        TableColumn<Employee, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        // 3. Stanowisko
        TableColumn<Employee, String> posCol = new TableColumn<>("Stanowisko");
        posCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPosition()));

        // 4. Pensja
        TableColumn<Employee, String> salaryCol = new TableColumn<>("Pensja");
        salaryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSalary().toString() + " PLN"));

        // 5. Aktywny (checkbox w tabeli)
        TableColumn<Employee, String> activeCol = new TableColumn<>("Status");
        activeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().isActive() ? "Aktywny" : "Dezaktywowany"));

        // Dodajemy wszystkie kolumny do tabeli
        table.getColumns().addAll(nameCol, emailCol, posCol, salaryCol, activeCol);

        // Ustawienie elastycznej szerokości kolumn
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Ten listener reaguje na kliknięcie w wiersz tabeli
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillForm(newSelection);
            }
        });
    }

    private VBox createForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(0, 0, 0, 20));
        form.setPrefWidth(300);

        Button saveBtn = new Button("Zapisz / Aktualizuj");
        saveBtn.setOnAction(e -> handleUpdate());

        Button deleteBtn = new Button("Usuń");
        deleteBtn.setOnAction(e -> handleRemove());

        form.getChildren().addAll(
                new Label("Imię:"), firstNameField,
                new Label("Nazwisko:"), lastNameField,
                new Label("Email:"), emailField,
                new Label("Stanowisko:"), positionField,
                new Label("Pensja:"), salaryField,
                activeCheckBox,
                saveBtn, deleteBtn
        );
        return form;
    }

    private void handleUpdate() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            employeeService.updateEmployee(selected.getId(), firstNameField.getText(), lastNameField.getText(), emailField.getText(), positionField.getText());
            employeeService.changeSalary(selected.getId(), new BigDecimal(salaryField.getText()));
            employeeService.setEmployeeActive(selected.getId(), activeCheckBox.isSelected());

            refreshTable();
            new Alert(Alert.AlertType.INFORMATION, "Zaktualizowano pracownika.").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Błąd: " + e.getMessage()).show();
        }
    }

    private void handleRemove() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                employeeService.removeEmployee(selected.getId());
                refreshTable();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Błąd: " + e.getMessage()).show();
            }
        }
    }

    private void fillForm(Employee e) {
        firstNameField.setText(e.getFirstName());
        lastNameField.setText(e.getLastName());
        emailField.setText(e.getEmail());
        positionField.setText(e.getPosition());
        salaryField.setText(e.getSalary().toString());
        activeCheckBox.setSelected(e.isActive());
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(employeeService.getAllEmployees()));
    }
}