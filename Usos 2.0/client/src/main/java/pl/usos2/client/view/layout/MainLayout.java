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
import pl.usos2.client.UserRole;
import pl.usos2.client.view.admin.EmployeeListView;
import pl.usos2.client.view.admin.SystemRequestsView;
import pl.usos2.client.view.admin.UserManagementView;
import pl.usos2.client.view.admin.AdminScheduleView;
import pl.usos2.client.view.lecturer.LecturerCoursesView;
import pl.usos2.client.view.lecturer.LecturerGradesView;
import pl.usos2.client.view.lecturer.LecturerMessagesView;
import pl.usos2.client.view.student.*;

public class MainLayout extends BorderPane {
    private StackPane contentArea;
    private final UserRole role;
    private final MainApp mainApp;

    public MainLayout(UserRole role, MainApp mainApp) {
        this.role = role;
        this.mainApp = mainApp;

        // --- SIDEBAR ---
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: #1e293b;");

        Label logo = new Label("USOS 2.0");
        logo.setTextFill(Color.WHITE);
        logo.setFont(Font.font("System", FontWeight.BOLD, 22));
        logo.setPadding(new Insets(0, 0, 30, 0));
        sidebar.getChildren().add(logo);

        // Menu dynamiczne w zależności od roli
        if (role == UserRole.STUDENT) {
            addStudentMenu(sidebar);
        } else if (role == UserRole.LECTURER) {
            addLecturerMenu(sidebar);
        } else if (role == UserRole.ADMIN) {
            addAdminMenu(sidebar);
        }

        // --- DODAJEMY PRZYCISK WYJŚCIA ---


        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button logoutBtn = createNavButton("Logout", () -> mainApp.showLogin());
        logoutBtn.setStyle(logoutBtn.getStyle() + "-fx-text-fill: #f87171;");

        // Efekt dla przycisku wyjścia (czerwone tło po najechaniu kursorem)
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: white; -fx-font-size: 14;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f87171; -fx-font-size: 14;"));

        sidebar.getChildren().add(logoutBtn);

        // --- CONTENT AREA ---
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f8fafc;");

        setLeft(sidebar);
        setCenter(contentArea);

        setContent(new DashboardView(this.role, this));
    }

    private void addStudentMenu(VBox sidebar) {
        sidebar.getChildren().addAll(
                createNavButton("Dashboard", () -> setContent(new DashboardView(role, this))),
                createNavButton("Schedule", () -> setContent(new ScheduleView())),
                createNavButton("Grades", () -> setContent(new GradesView())),
                createNavButton("Wiadomości", () -> setContent(new MessagesView())),
                createNavButton("Payments", () -> setContent(new PaymentsView()))
        );
    }

    private void addLecturerMenu(VBox sidebar) {
        sidebar.getChildren().addAll(
                createNavButton("Dashboard", () -> setContent(new DashboardView(this.role, this))),
                createNavButton("My Courses", () -> setContent(new LecturerCoursesView(this))),
                createNavButton("Grade Students", () -> setContent(new LecturerGradesView())),
                createNavButton("Schedule", () -> setContent(new ScheduleView())),
                createNavButton("Messages", () -> setContent(new LecturerMessagesView()))
        );
    }


    private void addAdminMenu(VBox sidebar) {
        sidebar.getChildren().addAll(
                createNavButton("Dashboard", () -> setContent(new DashboardView(this.role, this))),
                createNavButton("User Management", () -> setContent(new UserManagementView())),
                createNavButton("Employee List", () -> setContent(new EmployeeListView())),
                createNavButton("Global Schedule", () -> setContent(new AdminScheduleView())),
                createNavButton("System Requests", () -> setContent(new SystemRequestsView()))
        );
    }

    public void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 15, 12, 15));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-font-size: 14;");

        btn.setOnAction(e -> action.run());
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 14;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 14;"));

        return btn;
    }
}