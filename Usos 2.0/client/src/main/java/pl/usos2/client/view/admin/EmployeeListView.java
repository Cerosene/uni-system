package pl.usos2.client.view.admin;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class EmployeeListView extends ScrollPane {

    public EmployeeListView() {
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));

        Label title = new Label("Employee Directory");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        FlowPane flowPane = new FlowPane(20, 20); // Siatka na kartki

        // Dodajemy karty pracowników
        flowPane.getChildren().addAll(
                createEmployeeCard("Dr. Janusz Nowak", "Department of AI", "j.nowak@uni.pl"),
                createEmployeeCard("Prof. Maria Kowalska", "Data Science", "m.kow@uni.pl"),
                createEmployeeCard("Dr. Adam Wiśniewski", "Cybersecurity", "a.wis@uni.pl"),
                createEmployeeCard("Mgr. Ewa Bąk", "Student Affairs", "e.bak@uni.pl")
        );

        container.getChildren().addAll(title, flowPane);
        setContent(container);
    }

    private VBox createEmployeeCard(String name, String dept, String email) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label nameLbl = new Label(name);
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label deptLbl = new Label(dept);
        deptLbl.setTextFill(Color.GRAY);

        Hyperlink emailLnk = new Hyperlink(email);
        emailLnk.setStyle("-fx-padding: 0;");

        Button editBtn = new Button("Edit Profile");
        editBtn.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(nameLbl, deptLbl, emailLnk, editBtn);
        return card;
    }
}