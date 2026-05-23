package pl.usos2.client.view.student;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.UserRole;
import pl.usos2.client.view.layout.MainLayout;

public class DashboardView extends ScrollPane {
    private MainLayout mainLayout;
    private final UserRole role;

    public DashboardView(UserRole role, MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        this.role = role;

        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        VBox mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // --- STATYSTYKA ---
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);

        if (role == UserRole.STUDENT) {
            statsRow.getChildren().addAll(
                    createStatCard("Current GPA", "4.2", "+0.3 from last sem", "#3b82f6"),
                    createStatCard("Credits", "120/180", "66% completed", "#10b981")
            );
        } else if (role == UserRole.LECTURER) {
            statsRow.getChildren().addAll(
                    createStatCard("Active Courses", "3", "Current semester", "#3b82f6"),
                    createStatCard("Total Students", "185", "Across all groups", "#10b981"),
                    createStatCard("Pending Grades", "12", "Need review", "#ea580c")
            );
        } else if (role == UserRole.ADMIN) {
            statsRow.getChildren().addAll(
                    createStatCard("Total Users", "1,240", "Students & Staff", "#3b82f6"),
                    createStatCard("System Load", "24%", "Stable", "#10b981")
            );
        }

        // -- Quick Actions ---
        VBox quickActionsSection = new VBox(15);
        Label sectionTitle = new Label("Quick Navigation");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Siatka na przyciski w układzie 4 w rzędzie
        TilePane actionGrid = new TilePane();
        actionGrid.setHgap(15);
        actionGrid.setVgap(15);
        actionGrid.setPrefColumns(4);
        actionGrid.setAlignment(Pos.CENTER);

        // Dodanie przecisków
        actionGrid.getChildren().addAll(
                createActionButton("Rejestracja", "#eff6ff", "#2563eb", () -> System.out.println("Reg")),
                createActionButton("Sprawdziany", "#fff7ed", "#ea580c", null),
                createActionButton("Oceny", "#f0fdf4", "#16a34a", () ->
                        mainLayout.setContent(new GradesView())),
                createActionButton("Podpięcia", "#faf5ff", "#9333ea", null),
                createActionButton("Decyzje", "#fff7ed", "#ea580c", null),
                createActionButton("Zaliczenia etapów", "#f0fdf4", "#16a34a", null),
                createActionButton("Wnioski", "#faf5ff", "#9333ea", () ->
                        mainLayout.setContent(new ApplicationsView())),
                createActionButton("Wybór promotora", "#fdf2f8", "#db2777", () ->
                        mainLayout.setContent(new ThesisView())),
                createActionButton("Stypendia", "#f0f9ff", "#0284c7", null),
                createActionButton("Ankiety", "#faf5ff", "#9333ea", null),
                createActionButton("Platności", "#fdf2f8", "#db2777", null),
                createActionButton("Dyplomy", "#f0f9ff", "#0284c7", null)
        );

        quickActionsSection.getChildren().addAll(sectionTitle, actionGrid);

        // --- OGŁOSZENIA I ROZKŁAD (dolny rząd) ---
        HBox bottomRow = new HBox(20);
        VBox annBox = createContentCard("Announcements");
        addAnnouncement(annBox, "System Maintenance", "2026-04-12", "Scheduled downtime at 10 PM.", "info");

        VBox schedBox = createContentCard("Next Class");
        addScheduleItem(schedBox, "14:00", "Computer Networks", "C-102", "Dr. Wiśniewski");

        HBox.setHgrow(annBox, Priority.ALWAYS);
        HBox.setHgrow(schedBox, Priority.ALWAYS);
        bottomRow.getChildren().addAll(annBox, schedBox);

        mainContainer.getChildren().addAll(statsRow, quickActionsSection, bottomRow);
        setContent(mainContainer);
    }

    // Metoda tworzenia stylowych płytek-przycisków
    private VBox createActionButton(String text, String bgColor, String textColor, Runnable action) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(180, 100);
        card.setPadding(new Insets(15));

        // Stylizacja imitująca „płytki”
        card.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: " + textColor + "; " +
                "-fx-border-width: 0.5; " +
                "-fx-border-radius: 12; " +
                "-fx-cursor: hand;");

        Label label = new Label(text);
        label.setTextFill(Color.web(textColor));
        label.setFont(Font.font("System", FontWeight.BOLD, 14));

        card.getChildren().add(label);

        // Efekt naprowadzania
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-effect: dropshadow(three-pass-box, " + textColor + "44, 10, 0, 0, 0);"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().split("-fx-effect")[0]));

        card.setOnMouseClicked(e -> {
            if (action != null) action.run();
        });

        return card;
    }


    private VBox createStatCard(String title, String value, String sub, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        Label t = new Label(title); t.setTextFill(Color.GRAY);
        Label v = new Label(value); v.setFont(Font.font("System", FontWeight.BOLD, 22));
        card.getChildren().addAll(t, v);
        return card;
    }

    private VBox createContentCard(String title) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");
        Label head = new Label(title);
        head.setFont(Font.font("System", FontWeight.BOLD, 16));
        box.getChildren().add(head);
        return box;
    }

    private void addAnnouncement(VBox container, String title, String date, String text, String type) {
        container.getChildren().add(new Label(title + " (" + date + ")"));
    }

    private void addScheduleItem(VBox container, String time, String subject, String room, String teacher) {
        container.getChildren().add(new Label(time + " - " + subject));
    }
}