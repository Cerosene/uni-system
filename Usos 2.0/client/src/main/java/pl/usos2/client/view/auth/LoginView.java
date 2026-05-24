package pl.usos2.client.view.auth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.MainApp;
import pl.usos2.server.model.enumtype.UserRole;

// Подключаем серверную логику авторизации и модель пользователя
import pl.usos2.server.service.auth.AuthService;
import pl.usos2.server.model.user.User;

public class LoginView extends StackPane {

    private final MainApp mainApp;
    // Tworzymy instancję serwisu autoryzacyjnego
    private final AuthService authService = new AuthService();

    public LoginView(MainApp app) {
        this.mainApp = app;

        // Inicjujemy użytkowników testowych (baza danych mock wewnątrz serwisu)
        initMockUsers();


        setStyle("-fx-background-color: linear-gradient(to bottom right, #eff6ff, #ffffff, #eff6ff);");

        // Karta formularza logowania
        VBox card = new VBox(20);
        card.setMaxSize(400, 520);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 20, 0, 0, 10);");

        // Ikona-logo
        Label logoPlaceholder = new Label("🎓");
        logoPlaceholder.setFont(Font.font("System", 40));

        Label title = new Label("USOS 2.0");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Zaloguj się do systemu akademickiego");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        // Etykieta do wyświetlania błędów walidacji
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#e11d48"));
        errorLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Pola wprowadzania danych
        VBox inputs = new VBox(10);
        inputs.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label("E-mail");
        userLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        TextField userField = new TextField();
        userField.setPromptText("student@uni.pl");
        setupFieldStyle(userField);

        Label passLabel = new Label("Hasło");
        passLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••");
        setupFieldStyle(passField);

        inputs.getChildren().addAll(userLabel, userField, passLabel, passField);

        // Przycisk logowania
        Button loginBtn = new Button("Zaloguj się");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPadding(new Insets(12));
        loginBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");

        // --- ZINTEGROWANA LOGIKA WEJŚCIOWA ---
        loginBtn.setOnAction(e -> {
            String email = userField.getText().trim();
            String password = passField.getText();

            // Podstawowa weryfikacja interfejsu użytkownika przed wysłaniem na serwer
            if (email.isEmpty() || password.isEmpty()) {
                showError(errorLabel, "Pola e-mail i hasło nie mogą być puste!");
                return;
            }

            try {
                // Wywołujemy metodę autoryzacji po stronie serwera
                User loggedInUser = authService.login(email, password);

                // Jeśli się uda - ukryjemy dawne błędy
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);

                // Pobieramy serwerową enumację ról i mapujemy ją na klientowską klasę UserRole w celu zmiany ekranu
                String serverRole = loggedInUser.getRole().toString().toUpperCase();

                if (serverRole.contains("ADMIN")) {
                    mainApp.showMainLayout(UserRole.ADMINISTRATOR);
                } else if (serverRole.contains("LECTURER")) {
                    mainApp.showMainLayout(UserRole.LECTURER);
                } else {
                    mainApp.showMainLayout(UserRole.STUDENT);
                }

            } catch (IllegalArgumentException | IllegalStateException ex) {
                // Tutaj pojawią się błędy z AuthService: "Invalid email or password" lub "User account is inactive"
                showError(errorLabel, ex.getMessage());
            }
        });

        card.getChildren().addAll(logoPlaceholder, title, subtitle, errorLabel, inputs, loginBtn);
        getChildren().add(card);
    }

    private void setupFieldStyle(Control field) {
        field.setStyle("-fx-padding: 10; -fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;");
        field.setOnMouseEntered(e -> {
            if (!field.isFocused()) field.setStyle("-fx-padding: 10; -fx-background-color: #f1f5f9; -fx-border-color: #94a3b8; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;");
        });
        field.setOnMouseExited(e -> {
            if (!field.isFocused()) field.setStyle("-fx-padding: 10; -fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;");
        });
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) field.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-color: #2563eb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14; -fx-effect: dropshadow(three-pass-box, rgba(37,99,235,0.1), 5, 0, 0, 0);");
            else field.setStyle("-fx-padding: 10; -fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14;");
        });
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    // Wprowadzenie tymczasowych danych do AuthService na potrzeby testów autoryzacji
    private void initMockUsers() {
        try {
            // Tworzymy anonimowe podklasy serwerowej klasy abstrakcyjnej User na potrzeby testów
            authService.register(new User(1L, "Jan", "Kowalski", "student@uni.pl", "password123", pl.usos2.server.model.enumtype.UserRole.STUDENT, true) {});
            authService.register(new User(2L, "Tomasz", "Nowak", "lecturer@uni.pl", "password123", pl.usos2.server.model.enumtype.UserRole.LECTURER, true) {});
            authService.register(new User(3L, "Anna", "Zielińska", "admin@uni.pl", "password123", pl.usos2.server.model.enumtype.UserRole.ADMINISTRATOR, true) {});
        } catch (Exception e) {
            // Ignorujemy duplikaty podczas ponownej inicjalizacji
        }
    }
}