package pl.usos2.client.view.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserManagementView extends VBox {

    private final AuthService authService;
    private final ObservableList<UserRow> allUsers;
    private final FilteredList<UserRow> filteredUsers;
    private final TableView<UserRow> table;

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
        searchField.setPrefWidth(320);

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

        allUsers = FXCollections.observableArrayList();
        filteredUsers = new FilteredList<>(allUsers, p -> true);
        table.setItems(filteredUsers);
        refreshUsers();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isBlank()) {
                    return true;
                }
                String lower = newValue.toLowerCase(Locale.ROOT);
                return user.getName().toLowerCase(Locale.ROOT).contains(lower)
                        || user.getId().toLowerCase(Locale.ROOT).contains(lower)
                        || user.getRole().toLowerCase(Locale.ROOT).contains(lower);
            });
        });

        addUserBtn.setOnAction(e -> openAddUserDialog());

        HBox actionsBar = new HBox(10);
        Button changeStatusBtn = new Button(isEnglish() ? "Change account status" : "Zmień status konta");
        changeStatusBtn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        changeStatusBtn.setOnAction(e -> changeSelectedUserStatus());

        Button deleteUserBtn = new Button(isEnglish() ? "Delete account" : "Usuń konto");
        deleteUserBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        deleteUserBtn.setOnAction(e -> deleteSelectedUser());

        actionsBar.getChildren().addAll(changeStatusBtn, deleteUserBtn);

        getChildren().addAll(header, searchBar, table, actionsBar);
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

        TextField firstNameInput = new TextField();
        TextField lastNameInput = new TextField();
        TextField emailInput = new TextField();
        PasswordField passwordInput = new PasswordField();

        ComboBox<String> roleInput = new ComboBox<>(FXCollections.observableArrayList(
                isEnglish() ? "Student" : "Student",
                isEnglish() ? "Lecturer" : "Wykładowca",
                isEnglish() ? "Administrator" : "Administrator"
        ));
        roleInput.getSelectionModel().selectFirst();

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
            if (dialogButton != saveButtonType) {
                return null;
            }

            String firstName = firstNameInput.getText() == null ? "" : firstNameInput.getText().trim();
            String lastName = lastNameInput.getText() == null ? "" : lastNameInput.getText().trim();
            String email = emailInput.getText() == null ? "" : emailInput.getText().trim();
            String password = passwordInput.getText() == null ? "" : passwordInput.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                return null;
            }

            long id = nextIncrementalUserId();
            String codeNumber = String.format(Locale.ROOT, "%04d", id);

            String roleValue = roleInput.getValue();
            String normalizedRole = roleValue == null ? "" : roleValue.toLowerCase(Locale.ROOT);
            if (normalizedRole.contains("wyk") || normalizedRole.contains("lectur")) {
                return new UserRow(new Lecturer(id, firstName, lastName, email, password, "EMP" + codeNumber, "Dr."));
            }
            if (normalizedRole.contains("admin")) {
                return new UserRow(new Administrator(id, firstName, lastName, email, password, "ADM" + codeNumber));
            }
            return new UserRow(new Student(id, firstName, lastName, email, password, "ST" + codeNumber, "Informatyka", Semester.THIRD));
        });

        dialog.showAndWait().ifPresent(newUserRow -> {
            try {
                authService.register(newUserRow.originalUser);
                refreshUsers();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void changeSelectedUserStatus() {
        UserRow selectedRow = table.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            showWarning(isEnglish() ? "Select a user first." : "Najpierw wybierz użytkownika.");
            return;
        }

        User user;
        try {
            user = authService.findById(Long.parseLong(selectedRow.getId()));
        } catch (RuntimeException ex) {
            showWarning(ex.getMessage());
            return;
        }

        String activeLabel = isEnglish() ? "Active" : "Aktywne";
        String inactiveLabel = isEnglish() ? "Inactive" : "Nieaktywne";

        ChoiceDialog<String> dialog = new ChoiceDialog<>(user.isActive() ? activeLabel : inactiveLabel,
                FXCollections.observableArrayList(activeLabel, inactiveLabel));
        dialog.setTitle(isEnglish() ? "Account status" : "Status konta");
        dialog.setHeaderText(isEnglish() ? "Choose new status" : "Wybierz nowy status");
        dialog.setContentText(isEnglish() ? "Status:" : "Status:");

        dialog.showAndWait().ifPresent(choice -> {
            try {
                if (activeLabel.equals(choice)) {
                    authService.activateUser(user.getId());
                } else if (inactiveLabel.equals(choice)) {
                    authService.deactivateUser(user.getId());
                }
                refreshUsers();
                reselectById(user.getId());
                table.refresh();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showWarning(ex.getMessage());
            }
        });
    }

    private void deleteSelectedUser() {
        UserRow selectedRow = table.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            showWarning(isEnglish() ? "Select a user first." : "Najpierw wybierz użytkownika.");
            return;
        }

        long userId = Long.parseLong(selectedRow.getId());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(isEnglish() ? "Delete account" : "Usuń konto");
        confirm.setHeaderText(null);
        confirm.setContentText(isEnglish()
                ? "Are you sure you want to delete selected account?"
                : "Czy na pewno chcesz usunąć wybrane konto?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            authService.deleteUser(userId);
            refreshUsers();
            table.refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void refreshUsers() {
        allUsers.setAll(authService.getAllUsers().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(UserRow::new)
                .collect(Collectors.toList()));
    }

    private void reselectById(Long userId) {
        if (userId == null) {
            return;
        }
        for (UserRow row : table.getItems()) {
            if (String.valueOf(userId).equals(row.getId())) {
                table.getSelectionModel().select(row);
                break;
            }
        }
    }

    private long nextIncrementalUserId() {
        return authService.getAllUsers().stream()
                .map(User::getId)
                .filter(id -> id != null)
                .max(Long::compareTo)
                .orElse(0L) + 1L;
    }

    private boolean isEnglish() {
        return "en".equalsIgnoreCase(MockDataProvider.getCurrentLocale().getLanguage());
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(MockDataProvider.i18n("alert_warn_title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(MockDataProvider.i18n("alert_error_title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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

        public String getId() {
            return id.get();
        }

        public javafx.beans.property.StringProperty idProperty() {
            return id;
        }

        public String getName() {
            return name.get();
        }

        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        public String getRole() {
            return role.get();
        }

        public javafx.beans.property.StringProperty roleProperty() {
            return role;
        }

        public String getStatus() {
            return status.get();
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }
    }
}
