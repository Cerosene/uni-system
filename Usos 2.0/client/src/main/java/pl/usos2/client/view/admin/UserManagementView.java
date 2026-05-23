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

public class UserManagementView extends VBox {

    // Główna lista przechowująca wszystkich użytkowników systemu
    private final ObservableList<User> allUsers;
    // Lista opakowana, umożliwiająca dynamiczne filtrowanie danych w tabeli
    private final FilteredList<User> filteredUsers;
    private final TableView<User> table;

    public UserManagementView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        // Nagłówek panelu zarządzania użytkownikami
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("User Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addUserBtn = new Button("+ Add New User");
        addUserBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");

        // Podpięcie akcji otwierania okna modalnego dla nowego użytkownika
        addUserBtn.setOnAction(e -> openAddUserDialog());

        header.getChildren().addAll(title, spacer, addUserBtn);

        // Sekcja wyszukiwarki (Pasek wyszukiwania)
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_LEFT);

        Label searchLabel = new Label("Szukaj użytkownika:");
        searchLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));

        TextField searchField = new TextField();
        searchField.setPromptText("Wpisz imię, nazwisko, ID lub rolę...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6;");

        searchContainer.getChildren().addAll(searchLabel, searchField);

        // Inicjalizacja tabeli oraz danych demonstracyjnych
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> d.getValue().idProperty());

        TableColumn<User, String> nameCol = new TableColumn<>("Imię i Nazwisko");
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());

        TableColumn<User, String> roleCol = new TableColumn<>("Rola");
        roleCol.setCellValueFactory(d -> d.getValue().roleProperty());

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());

        table.getColumns().addAll(idCol, nameCol, roleCol, statusCol);

        // Wypełnienie listy początkowymi danymi
        allUsers = FXCollections.observableArrayList(
                new User("1001", "Mateusz Lewandowski", "Student", "Active"),
                new User("1002", "Dr. Janusz Nowak", "Lecturer", "Active"),
                new User("1003", "Anna Kowalska", "Admin", "Active"),
                new User("1004", "Piotr Zieliński", "Student", "Suspended")
        );

        // Powiązanie listy filtrowanej z główną kolekcją danych
        filteredUsers = new FilteredList<>(allUsers, p -> true);

        // Logika paska wyszukiwania: reaguje na każdą zmianę wprowadzonego tekstu
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                // Jeśli pole wyszukiwania jest puste, wyświetlamy wszystkich
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase().trim();

                // Sprawdzanie dopasowania w polach ID, Nazwisko oraz Rola
                if (user.getId().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getRole().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Użytkownik nie spełnia kryteriów wyszukiwania
            });
        });

        // Przekazanie przefiltrowanych danych bezpośrednio do widoku tabeli
        table.setItems(filteredUsers);

        getChildren().addAll(header, searchContainer, table);
    }

    // Metoda odpowiedzialna za wygenerowanie i wyświetlenie okna modalnego formularza
    private void openAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Dodaj Nowego Użytkownika");
        dialog.setHeaderText("Wprowadź dane konfiguracyjne dla nowego konta w systemie USOS2");

        // Ustawienie standardowych przycisków zatwierdzenia i anulowania
        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Kontener układu pól formularza
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idInput = new TextField();
        idInput.setPromptText("np. 1005");

        TextField nameInput = new TextField();
        nameInput.setPromptText("Imię i Nazwisko");

        ComboBox<String> roleInput = new ComboBox<>();
        roleInput.getItems().addAll("Student", "Lecturer", "Admin");
        roleInput.setValue("Student"); // Domyślny wybór roli

        ComboBox<String> statusInput = new ComboBox<>();
        statusInput.getItems().addAll("Active", "Suspended");
        statusInput.setValue("Active"); // Domyślny wybór statusu konta

        // Rozmieszczenie komponentów w siatce formularza
        grid.add(new Label("ID Użytkownika:"), 0, 0);
        grid.add(idInput, 1, 0);
        grid.add(new Label("Imię i Nazwisko:"), 0, 1);
        grid.add(nameInput, 1, 1);
        grid.add(new Label("Rola w systemie:"), 0, 2);
        grid.add(roleInput, 1, 2);
        grid.add(new Label("Status konta:"), 0, 3);
        grid.add(statusInput, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Konwersja wyniku kliknięcia przycisku "Zapisz" na obiekt klasy User
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String id = idInput.getText().trim();
                String name = nameInput.getText().trim();
                String role = roleInput.getValue();
                String status = statusInput.getValue();

                // Walidacja podstawowych pól przed utworzeniem obiektu
                if (id.isEmpty() || name.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Błąd walidacji");
                    alert.setHeaderText(null);
                    alert.setContentText("Wszystkie pola tekstowe muszą być wypełnione!");
                    alert.showAndWait();
                    return null;
                }
                return new User(id, name, role, status);
            }
            return null;
        });

        // Otwarcie okna i oczekiwanie na decyzję administratora
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