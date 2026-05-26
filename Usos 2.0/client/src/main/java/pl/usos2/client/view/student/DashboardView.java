package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.server.model.enumtype.UserRole;
import pl.usos2.client.view.layout.MainLayout;
import pl.usos2.client.view.student.*;
import pl.usos2.client.view.lecturer.LecturerCoursesView;
import pl.usos2.client.view.lecturer.LecturerGradesView;
import pl.usos2.client.view.lecturer.LecturerMessagesView;
import pl.usos2.client.view.admin.*;
import pl.usos2.client.util.MockDataProvider;

/**
 * Widok panelu głównego (Dashboard) dostosowujący się dynamicznie do roli zalogowanego użytkownika.
 * Obsługuje dynamiczne przełączanie widoków w MainLayout poprzez interfejs funkcyjny Consumer.
 */
public class DashboardView extends ScrollPane {
    private final MainLayout mainLayout;
    private final UserRole role;

    // Kontener główny dla wszystkich elementów widoku panelu sterowania
    private final VBox mainContainer;

    // Węzły UI wymagające dynamicznej aktualizacji tekstów po zmianie języka (i18n)
    private Label welcomeLabel;
    private Label infoLabel;
    private Label statsTitleLabel;
    private Label actionsTitleLabel;

    public DashboardView(UserRole role, MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        this.role = role;

        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #f8fafc;");

        // 1. Sekcja powitalna (Header)
        createHeaderSection();

        // 2. Sekcja statystyk (Cards) - zależna od roli
        createStatsSection();

        // 3. Sekcja szybkich działań (Quick Actions) - zintegrowana z metodą setContent z MainLayout
        createQuickActionsSection();

        setContent(mainContainer);
    }

    private void createHeaderSection() {
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(20));
        headerBox.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 2);");

        welcomeLabel = new Label(MockDataProvider.i18n("dash_welcome") + ", " + mainLayout.getCurrentUser().getFullName() + "!");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 26));
        welcomeLabel.setTextFill(Color.web("#1e293b"));

        infoLabel = new Label(MockDataProvider.i18n("dash_info_sub"));
        infoLabel.setFont(Font.font("System", 14));
        infoLabel.setTextFill(Color.web("#64748b"));

        headerBox.getChildren().addAll(welcomeLabel, infoLabel);
        mainContainer.getChildren().add(headerBox);
    }

    private void createStatsSection() {
        VBox statsWrapper = new VBox(15);

        statsTitleLabel = new Label(MockDataProvider.i18n("dash_stats_title"));
        statsTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        statsTitleLabel.setTextFill(Color.web("#334155"));
        statsWrapper.getChildren().add(statsTitleLabel);

        FlowPane statsGrid = new FlowPane(20, 20);

        if (role == UserRole.STUDENT) {
            statsGrid.getChildren().addAll(
                    createStatCard("dash_stat_gpa", "4.52", "dash_stat_gpa_sub", "#3b82f6"),
                    createStatCard("dash_stat_ects", "30 / 30", "dash_stat_ects_sub", "#10b981"),
                    createStatCard("dash_stat_payments", "0.00 PLN", "dash_stat_payments_sub", "#ef4444")
            );
        } else if (role == UserRole.LECTURER) {
            statsGrid.getChildren().addAll(
                    createStatCard("dash_stat_courses", "3", "dash_stat_courses_sub", "#8b5cf6"),
                    createStatCard("dash_stat_students", "185", "dash_stat_students_sub", "#f59e0b"),
                    createStatCard("dash_stat_messages", "5", "dash_stat_messages_sub", "#06b6d4")
            );
        } else if (role == UserRole.ADMINISTRATOR) {
            statsGrid.getChildren().addAll(
                    createStatCard("dash_stat_total_users", "2,450", "dash_stat_total_users_sub", "#3b82f6"),
                    createStatCard("dash_stat_active_req", "12", "dash_stat_active_req_sub", "#ec4899"),
                    createStatCard("dash_stat_system_status", "ONLINE", "dash_stat_system_status_sub", "#10b981")
            );
        }

        statsWrapper.getChildren().add(statsGrid);
        mainContainer.getChildren().add(statsWrapper);
    }

    /**
     * Buduje sekcję szybkich przycisków akcji, wywołując metodę setContent z instancji MainLayout.
     */
    private void createQuickActionsSection() {
        VBox actionsWrapper = new VBox(15);

        actionsTitleLabel = new Label(MockDataProvider.i18n("dash_actions_title"));
        actionsTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        actionsTitleLabel.setTextFill(Color.web("#334155"));
        actionsWrapper.getChildren().add(actionsTitleLabel);

        FlowPane actionsGrid = new FlowPane(15, 15);

        // Mapowanie przycisków bezpośrednio na wywołania setContent() z przekazaniem instancji widoków
        if (role == UserRole.STUDENT) {
            actionsGrid.getChildren().addAll(
                    createActionButton("nav_grades", " Oceny", "#3b82f6", () -> mainLayout.setContent(new GradesView(mainLayout.getCurrentUser(), mainLayout.getContext().getGradeService()))),
                    createActionButton("nav_schedule", " Plan zajęć", "#10b981", () -> mainLayout.setContent(new ScheduleView())),
                    createActionButton("nav_applications", " Wnioski", "#8b5cf6", () -> mainLayout.setContent(new ApplicationsView(mainLayout.getCurrentUser(), mainLayout.getContext().getRequestService()))),
                    createActionButton("nav_payments", " Płatności", "#ef4444", () -> mainLayout.setContent(new PaymentsView(mainLayout.getCurrentUser(), mainLayout.getContext().getPaymentService()))),
                    createActionButton("nav_messages", " Wiadomości", "#06b6d4", () -> mainLayout.setContent(new MessagesView(mainLayout.getCurrentUser(), mainLayout.getContext().getMessageService(), mainLayout.getContext().getAuthService()))),
                    createActionButton("nav_thesis", " Praca dyplomowa", "#f59e0b", () -> mainLayout.setContent(new TicketsView(mainLayout.getCurrentUser(), mainLayout.getContext().getServiceTicketService())))
            );
        } else if (role == UserRole.LECTURER) {
            actionsGrid.getChildren().addAll(
                    createActionButton("nav_my_courses", " Moje kursy", "#8b5cf6", () -> mainLayout.setContent(new LecturerCoursesView(mainLayout.getCurrentUser(), mainLayout.getContext().getCourseService()))),
                    createActionButton("nav_add_grades", " Wystaw oceny", "#10b981", () -> mainLayout.setContent(new LecturerGradesView(mainLayout.getCurrentUser(), mainLayout.getContext().getGradeService()))),
                    createActionButton("nav_messages", " Wiadomości", "#06b6d4", () -> mainLayout.setContent(new LecturerMessagesView(mainLayout.getCurrentUser(), mainLayout.getContext().getMessageService(), mainLayout.getContext().getAuthService())))
            );
        } else if (role == UserRole.ADMINISTRATOR) {
            actionsGrid.getChildren().addAll(
                    createActionButton("nav_manage_users", " Użytkownicy", "#2563eb", () -> mainLayout.setContent(new UserManagementView(mainLayout.getContext().getAuthService()))),
                    createActionButton("nav_manage_schedule", " Edycja planu", "#f59e0b", () -> mainLayout.setContent(new AdminScheduleView())),
                    createActionButton("nav_system_requests", " Wnioski systemowe", "#ec4899", () -> mainLayout.setContent(new SystemRequestsView(mainLayout.getContext().getRequestService()))),
                    createActionButton("nav_employee_dir", " Pracownicy", "#64748b", () -> mainLayout.setContent(new EmployeeListView(mainLayout.getContext().getEmployeeService())))
            );
        }

        actionsWrapper.getChildren().add(actionsGrid);
        mainContainer.getChildren().add(actionsWrapper);
    }

    private VBox createStatCard(String titleKey, String value, String subKey, String accentColor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 4);");

        Pane accentBar = new Pane();
        accentBar.setPrefHeight(4);
        accentBar.setPrefWidth(40);
        accentBar.setMaxWidth(40);
        accentBar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 2;");

        Label titleLbl = new Label(MockDataProvider.i18n(titleKey));
        titleLbl.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        titleLbl.setTextFill(Color.web("#64748b"));

        Label valueLbl = new Label(value);
        valueLbl.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLbl.setTextFill(Color.web("#0f172a"));

        Label subLbl = new Label(MockDataProvider.i18n(subKey));
        subLbl.setFont(Font.font("System", 11));
        subLbl.setTextFill(Color.web("#94a3b8"));

        card.getChildren().addAll(accentBar, titleLbl, valueLbl, subLbl);
        card.setUserData(new String[]{titleKey, subKey});

        return card;
    }

    private Button createActionButton(String textKey, String fallbackText, String colorHex, Runnable action) {
        Button btn = new Button(MockDataProvider.i18n(textKey));
        if (btn.getText().equals(textKey) || btn.getText().isEmpty()) {
            btn.setText(fallbackText);
        }

        btn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        btn.setPrefSize(200, 50);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: " + colorHex + "; -fx-border-color: " + colorHex + "; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-border-color: " + colorHex + "; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: " + colorHex + "; -fx-border-color: " + colorHex + "; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));

        btn.setOnAction(e -> action.run());
        btn.setUserData(textKey);

        return btn;
    }

    public void translate() {
        // 1. Natychmiastowe tłumaczenie głównych etykiet tekstowych nagłówka
        welcomeLabel.setText(MockDataProvider.i18n("dash_welcome") + ", " + mainLayout.getCurrentUser().getFullName() + "!");
        infoLabel.setText(MockDataProvider.i18n("dash_info_sub"));
        statsTitleLabel.setText(MockDataProvider.i18n("dash_stats_title"));
        actionsTitleLabel.setText(MockDataProvider.i18n("dash_actions_title"));

        // 2. Bezpieczne przeszukiwanie kontenera głównego w poszukiwaniu siatek FlowPane
        for (javafx.scene.Node node : mainContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox wrapperBox = (VBox) node;

                // Przeszukujemy elementy wewnątrz wrappera (np. sekcji statystyk lub szybkich akcji)
                for (javafx.scene.Node subNode : wrapperBox.getChildren()) {
                    if (subNode instanceof FlowPane) {
                        // Wywołanie tłumaczenia dla grupy elementów wewnątrz siatki (FlowPane)
                        translateGroup((FlowPane) subNode);
                    }
                }
            }
        }
    }

    private void translateGroup(FlowPane pane) {
        for (javafx.scene.Node node : pane.getChildren()) {
            if (node instanceof VBox && node.getUserData() instanceof String[]) {
                String[] keys = (String[]) node.getUserData();
                VBox card = (VBox) node;
                int labelIndex = 0;
                for (javafx.scene.Node inner : card.getChildren()) {
                    if (inner instanceof Label) {
                        Label label = (Label) inner;
                        if (labelIndex == 0) {
                            label.setText(MockDataProvider.i18n(keys[0]));
                            labelIndex++;
                        } else if (labelIndex == 1 && card.getChildren().size() == 4) {
                            labelIndex++;
                        } else {
                            label.setText(MockDataProvider.i18n(keys[1]));
                        }
                    }
                }
            } else if (node instanceof Button && node.getUserData() instanceof String) {
                Button btn = (Button) node;
                btn.setText(MockDataProvider.i18n((String) node.getUserData()));
            }
        }
    }
}