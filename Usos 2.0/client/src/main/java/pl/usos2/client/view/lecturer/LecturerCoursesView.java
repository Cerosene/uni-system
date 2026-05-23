package pl.usos2.client.view.lecturer;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.view.layout.MainLayout;

public class LecturerCoursesView extends ScrollPane {
    MainLayout mainLayout;
    public LecturerCoursesView(MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        VBox container = new VBox(25);
        container.setPadding(new Insets(30));

        Label title = new Label("Moje Kursy");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));

        FlowPane coursesGrid = new FlowPane(20, 20);

        // Przykłady kursów prowadzonych przez tego wykładowcę
        coursesGrid.getChildren().addAll(
                createCourseCard("CS301", "Advanced Algorithms", "Lecture/Lab", "120 Students"),
                createCourseCard("CS302", "Database Systems", "Lab", "45 Students"),
                createCourseCard("CS405", "Machine Learning", "Seminar", "20 Students")
        );

        container.getChildren().addAll(title, coursesGrid);
        setContent(container);
    }

    private VBox createCourseCard(String code, String name, String type, String students) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label codeLbl = new Label(code);
        codeLbl.setTextFill(Color.web("#3b82f6"));
        codeLbl.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label nameLbl = new Label(name);
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLbl.setWrapText(true);

        HBox info = new HBox(10);
        Label typeLbl = new Label(type);
        typeLbl.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 3 8; -fx-background-radius: 5;");
        Label studentsLbl = new Label(students);
        studentsLbl.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 3 8; -fx-background-radius: 5;");
        info.getChildren().addAll(typeLbl, studentsLbl);

        Button manageBtn = new Button("Zarządzaj ocenami");

        manageBtn.setOnAction(e -> mainLayout.setContent(new LecturerGradesView()));
        manageBtn.setMaxWidth(Double.MAX_VALUE);
        manageBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-cursor: hand;");

        card.getChildren().addAll(codeLbl, nameLbl, info, manageBtn);
        return card;
    }
}