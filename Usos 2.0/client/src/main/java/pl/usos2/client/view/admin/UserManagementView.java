package pl.usos2.client.view.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.enumtype.Semester;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.server.model.user.Administrator;
import pl.usos2.server.model.user.Lecturer;
import pl.usos2.server.model.user.Student;
import pl.usos2.server.model.user.User;
import pl.usos2.server.service.auth.AuthService;

import java.util.List;
import java.util.stream.Collectors;

public class UserManagementView extends VBox {

    private final AuthService authService;
    private final ObservableList<UserRow> allUsers;
    private final FilteredList<UserRow> filteredUsers;
    private final TableView<UserRow> table;

    private final TextField firstNameInput = new TextField();
    private final TextField lastNameInput = new TextField();
    private final TextField emailInput = new TextField();
    private final PasswordField passwordInput = new PasswordField();

    public UserManagementView(AuthService authService) {
        this.authService = authService;

        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(MockDataProvider.i18n("user_management_title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addUserBtn = new Button(MockDataProvider.i18n("btn_add_new_user"));
        addUserBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

        header.getChildren().addAll(title, spacer, addUserBtn);

        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText(MockDataProvider.i18n("search_users_holder"));
        searchField.setPrefWidth(300);

        searchBar.getChildren().addAll(new Label(MockDataProvider.i18n("label_search")), searchField);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<UserRow, String> idCol = new TableColumn<>("ID");
        TableColumn<UserRow, String> nameCol = new TableColumn<>(MockDataProvider.i18n("col_user_name"));
        TableColumn<UserRow, String> roleCol = new TableColumn<>(MockDataProvider.i18n("col_user_role"));
        TableColumn<UserRow, String> statusCol = new TableColumn<>(MockDataProvider.i18n("col_user_status"));

        idCol.setCellValueFactory(d -> d.getValue().idProperty());
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        roleCol.setCellValueFactory(d -> d.getValue().roleProperty());
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());

        table.getColumns().addAll(idCol, nameCol, roleCol, statusCol);

        allUsers = FXCollections.observableArrayList(
                authService.getAllUsers().stream().map(UserRow::new).collect(Collectors.toList())
        );
        filteredUsers = new FilteredList<>(allUsers, p -> true);
        table.setItems(filteredUsers);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getName().toLowerCase().contains(lowerCaseFilter) ||
                        user.getId().toLowerCase().contains(lowerCaseFilter) ||
                        user.getRole().toLowerCase().contains(lowerCaseFilter);
            });
        });

        addUserBtn.setOnAction(e -> openAddUserDialog());

        getChildren().addAll(header, searchBar, table);
    }

    private void openAddUserDialog() {
        Dialog<UserRow> dialog = new Dialog<>();
        dialog.setTitle(MockDataProvider.i18n("dialog_add_user_title"));
        dialog.setHeaderText(MockDataProvider.i18n("dialog_add_user_header"));

        ButtonType saveButtonType = new ButtonType(MockDataProvider.i18n("btn_save_label"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> roleInput = new ComboBox<>(FXCollections.observableArrayList("Student", "Lecturer", "Administrator"));
        roleInput.setValue("Student");

        grid.add(new Label(MockDataProvider.i18n("label_first_name") + ":"), 0, 0);
        grid.add(firstNameInput, 1, 0);
        grid.add(new Label(MockDataProvider.i18n("label_last_name") + ":"), 0, 1);
        grid.add(lastNameInput, 1, 1);
        grid.add(new Label(MockDataProvider.i18n("label_email") + ":"), 0, 2);
        grid.add(emailInput, 1, 2);
        grid.add(new Label(MockDataProvider.i18n("label_password") + ":"), 0, 3);
        grid.add(passwordInput, 1, 3);
        grid.add(new Label(MockDataProvider.i18n("col_user_role") + ":"), 0, 4);
        grid.add(roleInput, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                long id = System.currentTimeMillis();
                String firstName = firstNameInput.getText().trim();
                String lastName = lastNameInput.getText().trim();
                String email = emailInput.getText().trim();
                String password = passwordInput.getText().trim();
                String roleValue = roleInput.getValue();

                if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    return null;
                }

                if ("Lecturer".equals(roleValue)) {
                    return new UserRow(new Lecturer(id, firstName, lastName, email, password, "EMP" + id, "Dr."));
                }
                if ("Administrator".equals(roleValue)) {
                    return new UserRow(new Administrator(id, firstName, lastName, email, password, "EMP" + id));
                }
                return new UserRow(new Student(id, firstName, lastName, email, password, "ST" + id, "Informatyka", Semester.THIRD));
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newUserRow -> {
            try {
                authService.register(newUserRow.originalUser);
                allUsers.setAll(authService.getAllUsers().stream().map(UserRow::new).collect(Collectors.toList()));
            } catch (IllegalArgumentException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(MockDataProvider.i18n("alert_error_title"));
                alert.setHeaderText(null);
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });
    }

    // Wewnętrzny model danych reprezentujący strukturę użytkownika w tabeli administracyjnej
    public static class UserRow {
        private final User originalUser;
        private final javafx.beans.property.StringProperty id;
        private final javafx.beans.property.StringProperty name;
        private final javafx.beans.property.StringProperty role;
        private final javafx.beans.property.StringProperty status;

        public UserRow(User user) {
            this.originalUser = user;
            this.id = new javafx.beans.property.SimpleStringProperty(String.valueOf(user.getId()));
            this.name = new javafx.beans.property.SimpleStringProperty(user.getFullName());
            this.role = new javafx.beans.property.SimpleStringProperty(formatRole(user.getRole()));
            this.status = new javafx.beans.property.SimpleStringProperty(formatStatus(user.isActive()));
        }

        private static String formatRole(UserRole userRole) {
            return switch (userRole) {
                case STUDENT -> MockDataProvider.i18n("role_student");
                case LECTURER -> MockDataProvider.i18n("role_lecturer");
                case ADMINISTRATOR -> MockDataProvider.i18n("role_administrator");
                default -> userRole.name();
            };
        }

        private static String formatStatus(boolean isActive) {
            return isActive ? MockDataProvider.i18n("status_active") : MockDataProvider.i18n("status_inactive");
        }

        public String getId() { return id.get(); }
        public javafx.beans.property.StringProperty idProperty() { return id; }

        public String getName() { return name.get(); }
        public javafx.beans.property.StringProperty nameProperty() { return name; }

        public String getRole() { return role.get(); }
        public javafx.beans.property.StringProperty roleProperty() { return role; }

        public String getStatus() { return status.get(); }
        public javafx.beans.property.StringProperty statusProperty() { return status; }
    }
}