package pl.usos2.client.view.layout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.MainApp;
import pl.usos2.client.view.lecturer.StudentListView;
import pl.usos2.client.view.rental.StudentRentalsView;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.client.view.student.*;
import pl.usos2.client.view.lecturer.LecturerCoursesView;
import pl.usos2.client.view.lecturer.LecturerGradesView;
import pl.usos2.client.view.lecturer.LecturerMessagesView;
import pl.usos2.client.view.admin.*;
import pl.usos2.server.config.ApplicationContext;
import pl.usos2.server.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Główny układ aplikacji (BorderPane) zawierający boczny pasek nawigacyjny (Sidebar)
 * oraz dynamiczny obszar treści (Content Area). Obsługuje zmianę języków.
 */
public class MainLayout extends BorderPane {
    private final StackPane contentArea;
    private final User currentUser;
    private final UserRole role;
    private final ApplicationContext context;
    private final MainApp mainApp;

    // Lista przycisków nawigacyjnych do dynamicznego tłumaczenia i odświeżania UI
    private final List<NavButtonConfig> navButtons = new ArrayList<>();

    public MainLayout(User currentUser, ApplicationContext context, MainApp mainApp) {
        this.currentUser = currentUser;
        this.role = currentUser.getRole();
        this.context = context;
        this.mainApp = mainApp;

        // --- SIDEBAR (PANEL BOCZNY) ---
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(20));
        sidebar.setSpacing(10);
        sidebar.setStyle("-fx-background-color: #1e293b;"); // Elegancki ciemny grafit (Tailwind slate-800)
        sidebar.setPrefWidth(260);

        // Logo / Nagłówek systemu
        Label logoLabel = new Label("USOS 2.0");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        logoLabel.setTextFill(Color.WHITE);
        logoLabel.setPadding(new Insets(10, 10, 25, 10));
        sidebar.getChildren().add(logoLabel);

        // Dynamiczne dodawanie menu w zależności od roli użytkownika
        if (role == UserRole.STUDENT) {
            addStudentMenu(sidebar);
        } else if (role == UserRole.LECTURER) {
            addLecturerMenu(sidebar);
        } else if (role == UserRole.ADMINISTRATOR) {
            addAdminMenu(sidebar);
        }

        // Elastyczny separator przesuwający dół menu na sam dół ekranu
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // --- PRZEŁĄCZNIK JĘZYKÓW (PL / EN) ---
        HBox languageBox = new HBox(10);
        languageBox.setAlignment(Pos.CENTER);
        languageBox.setPadding(new Insets(10, 0, 10, 0));

        Button btnPl = new Button("PL");
        btnPl.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnPl.setOnAction(e -> changeLanguage(new Locale("pl")));

        Button btnEn = new Button("EN");
        btnEn.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnEn.setOnAction(e -> changeLanguage(new Locale("en")));

        languageBox.getChildren().addAll(btnPl, btnEn);
        sidebar.getChildren().add(languageBox);

        // Przycisk wylogowania
        Button logoutBtn = createNavButton("logout", () -> mainApp.logout());
        logoutBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        sidebar.getChildren().add(logoutBtn);

        setLeft(sidebar);

        // --- OBSZAR CENTRALNY (DYNAMICZNA TREŚĆ) ---
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f8fafc;"); // Jasne tło aplikacji
        setCenter(contentArea);

        // Ładowanie domyślnego ekranu startowego (Dashboard)
        setContent(new DashboardView(this.role, this));

        // Nasłuchiwanie zmian globalnej lokalizacji w celu automatycznego tłumaczenia paska bocznego
        MockDataProvider.currentLocaleProperty().addListener((obs, oldLocale, newLocale) -> refreshLocalization());
    }

    private void addStudentMenu(VBox sidebar) {
        sidebar.getChildren().addAll(
                createNavButton("dashboard", () -> setContent(new DashboardView(this.role, this))),
                createNavButton("schedule", () -> setContent(new ScheduleView(this.currentUser, context.getCourseService(), context.getScheduleService()))),
                createNavButton("grades", () -> setContent(new GradesView(this.currentUser, context.getGradeService(), context.getCourseService()))),
                createNavButton("messages", () -> setContent(new MessagesView(this.currentUser, context.getMessageService(), context.getAuthService()))),
                createNavButton("requests", () -> setContent(new ApplicationsView(this.currentUser, context.getRequestService()))),
                createNavButton("payments", () -> setContent(new PaymentsView(this.currentUser, context.getPaymentService()))),
                createNavButton("tickets", () -> setContent(new TicketsView(this.currentUser, context.getServiceTicketService()))),
                createNavButton("rentals", () -> setContent(new StudentRentalsView(this.currentUser, context.getRentalService())))
        );
    }

    private void addLecturerMenu(VBox sidebar) {
        sidebar.getChildren().addAll(
                createNavButton("dashboard", () -> setContent(new DashboardView(this.role, this))),
                createNavButton("grades", () -> setContent(new LecturerGradesView(this.currentUser, context.getGradeService()))),
                createNavButton("messages", () -> setContent(new LecturerMessagesView(this.currentUser, context.getMessageService(), context.getAuthService()))),
                createNavButton("course", () -> setContent(new LecturerCoursesView(this.currentUser, context.getCourseService()))),
                createNavButton("schedule", () -> setContent(new ScheduleView(this.currentUser, context.getCourseService(), context.getScheduleService())))


        );
    }

    private void addAdminMenu(VBox sidebar) {
        sidebar.getChildren().addAll(
                createNavButton("dashboard", () -> setContent(new DashboardView(this.role, this))),
                createNavButton("users", () -> setContent(new UserManagementView(context.getAuthService(), context.getCourseService()))),
                createNavButton("employees", () -> setContent(new EmployeeListView(context.getEmployeeService()))),
                createNavButton("schedule", () -> setContent(new AdminScheduleView(context.getCourseService(), context.getScheduleService(), context.getAuthService()))),
                createNavButton("requests", () -> setContent(new SystemRequestsView(context.getRequestService()))),
                createNavButton("tickets", () -> setContent(new AdminTicketsView(context.getServiceTicketService()))),
                createNavButton("payments", () -> setContent(new AdminPaymentsView(context.getPaymentService()))),
                createNavButton("admin_rentals", () -> setContent(new AdminRentalsView(context.getRentalService())))
        );
    }

    public void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public ApplicationContext getContext() {
        return context;
    }

    /**
     * Tworzy przycisk nawigacyjny, rejestruje go w systemie i wiąże z kluczem lokalizacyjnym.
     */
    private Button createNavButton(String i18nKey, Runnable action) {
        Button btn = new Button(MockDataProvider.i18n(i18nKey));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 15, 12, 15));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-font-size: 14;");

        btn.setOnAction(e -> action.run());

        // Efekty najechania myszką (Hover effects)
        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#ef4444")) {
                btn.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 14;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#ef4444")) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-font-size: 14;");
            }
        });

        navButtons.add(new NavButtonConfig(btn, i18nKey));
        return btn;
    }

    /**
     * Zmienia globalną lokalizację w dostawcy danych.
     */
    private void changeLanguage(Locale locale) {
        MockDataProvider.setCurrentLocale(locale);
    }

    /**
     * Odświeża teksty wszystkich zarejestrowanych przycisków bocznych po zmianie języka
     * oraz wymusza aktualizację językową aktywnego panelu głównego (Dashboard).
     */
    private void refreshLocalization() {
        // Aktualizacja napisów na przyciskach paska bocznego (Sidebar)
        for (NavButtonConfig config : navButtons) {
            config.button.setText(MockDataProvider.i18n(config.key));
        }

        // Sprawdzenie, czy aktualnie wyświetlanym oknem w contentArea jest DashboardView
        if (!contentArea.getChildren().isEmpty() && contentArea.getChildren().get(0) instanceof DashboardView) {
            // Rzutowanie i wywołanie metody translate() bezpośrednio na obiekcie widoku panelu
            ((DashboardView) contentArea.getChildren().get(0)).translate();
        }
    }

    /**
     * Klasa pomocnicza przechowująca konfigurację przycisku i jego klucza i18n.
     */
    private static class NavButtonConfig {
        Button button;
        String key;

        NavButtonConfig(Button button, String key) {
            this.button = button;
            this.key = key;
        }
    }
}
