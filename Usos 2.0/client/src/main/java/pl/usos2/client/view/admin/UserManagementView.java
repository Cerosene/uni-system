package pl.usos2.client.view.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;

public class UserManagementView extends VBox {

    // Główna lista przechowująca wszystkich użytkowników systemu w pamięci aplikacji
    private final ObservableList<User> allUsers;
    // Lista opakowana, umożliwiająca dynamiczne filtrowanie danych w tabeli w czasie rzeczywistym
    private final FilteredList<User> filteredUsers;
    private final TableView<User> table;

    public UserManagementView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        // Nagłówek panelu zarządzania użytkownikami
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(MockDataProvider.i18n("user_management_title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addUserBtn = new Button(MockDataProvider.i18n("btn_add_new_user"));
        addUserBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

        header.getChildren().addAll(title, spacer, addUserBtn);

        // --- PANEL WYSZUKIWANIA I FILTROWANIA (Search Bar) ---
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText(MockDataProvider.i18n("search_users_holder"));
        searchField.setPrefWidth(300);

        searchBar.getChildren().addAll(new Label(MockDataProvider.i18n("label_search")), searchField);

        // --- TABELA UŻYTKOWNIKÓW (TableView) ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> idCol = new TableColumn<>("ID");
        TableColumn<User, String> nameCol = new TableColumn<>(MockDataProvider.i18n("col_user_name"));
        TableColumn<User, String> roleCol = new TableColumn<>(MockDataProvider.i18n("col_user_role"));
        TableColumn<User, String> statusCol = new TableColumn<>(MockDataProvider.i18n("col_user_status"));

        idCol.setCellValueFactory(d -> d.getValue().idProperty());
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        roleCol.setCellValueFactory(d -> d.getValue().roleProperty());
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());

        table.getColumns().addAll(idCol, nameCol, roleCol, statusCol);

        // Inicjalizacja bazy danych Mock i powiązanie jej z filtrowaną strukturą danych
        allUsers = FXCollections.observableArrayList(
                new User("1001", "Jan Kowalski", "Student", "Active"),
                new User("1002", "Anna Nowak", "Lecturer", "Active"),
                new User("1003", "Dmytro Lytvyn", "Student", "Active"),
                new User("2001", "Tomasz Wiśniewski", "Admin", "Active")
        );
        filteredUsers = new FilteredList<>(allUsers, p -> true);
        table.setItems(filteredUsers);

        // Logika automatycznego filtrowania wierszy tabeli przy wprowadzaniu tekstu w pole wyszukiwania
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getName().toLowerCase().contains(lowerCaseFilter) ||
                        user.getId().contains(lowerCaseFilter) ||
                        user.getRole().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Podłączenie akcji otwierania formularza modalnego dodawania użytkownika
        addUserBtn.setOnAction(e -> openAddUserDialog());

        getChildren().addAll(header, searchBar, table);
    }

    private void openAddUserDialog() {
        // Tworzenie dedykowanego okna modalnego (Dialog) do wprowadzania danych nowego konta
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(MockDataProvider.i18n("dialog_add_user_title"));
        dialog.setHeaderText(MockDataProvider.i18n("dialog_add_user_header"));

        ButtonType saveButtonType = new ButtonType(MockDataProvider.i18n("btn_save_label"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idInput = new TextField();
        TextField nameInput = new TextField();
        ComboBox<String> roleInput = new ComboBox<>(FXCollections.observableArrayList("Student", "Lecturer", "Admin"));
        roleInput.setValue("Student");

        grid.add(new Label("ID:"), 0, 0);
        grid.add(idInput, 1, 0);
        grid.add(new Label(MockDataProvider.i18n("col_user_name") + ":"), 0, 1);
        grid.add(nameInput, 1, 1);
        grid.add(new Label(MockDataProvider.i18n("col_user_role") + ":"), 0, 2);
        grid.add(roleInput, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Konwersja wyniku formularza na instancję klasy User po kliknięciu przycisku Zapisz
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new User(idInput.getText(), nameInput.getText(), roleInput.getValue(), "Active");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newUser -> {
            // Dodanie nowego użytkownika do głównej listy (tabela zaktualizuje się automatycznie)
            allUsers.add(newUser);
        });
    }

    // Wewnętrzny model danych reprezentujący strukturę użytkownika w tabeli administracyjnej
    public static class User {
        private final javafx.beans.property.StringProperty id;
        private final javafx.beans.property.StringProperty name;
        private final javafx.beans.property.StringProperty role;
        private final javafx.beans.property.StringProperty status;

        public User(String id, String name, String role, String status) {
            this.id = new javafx.beans.property.SimpleStringProperty(id);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.role = new javafx.beans.property.SimpleStringProperty(role);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
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