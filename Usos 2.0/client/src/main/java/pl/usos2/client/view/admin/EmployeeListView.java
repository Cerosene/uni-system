package pl.usos2.client.view.admin;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;

public class EmployeeListView extends ScrollPane {

    public EmployeeListView() {
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));

        // Tytuł sekcji pobierany za pomocą globalnego dostawcy i18n
        Label title = new Label(MockDataProvider.i18n("employee_directory_title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        FlowPane flowPane = new FlowPane(20, 20); // Elastyczna siatka kontenerów kart pracowniczych

        // Zasilenie widoku demonstracyjnymi kartami pracowników dydaktyczno-naukowych
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
        // Stylizacja komponentu w nowoczesnym formacie Material Design / Tailwind CSS
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4); -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label nameLbl = new Label(name);
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLbl.setStyle("-fx-text-fill: #1e293b;");

        Label deptLbl = new Label(dept);
        deptLbl.setTextFill(Color.GRAY);
        deptLbl.setFont(Font.font("System", 13));

        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 5 0;");

        Label emailLbl = new Label(email);
        emailLbl.setTextFill(Color.web("#2563eb")); // Niebieski akcent dla danych kontaktowych
        emailLbl.setFont(Font.font("System", 12));

        card.getChildren().addAll(nameLbl, deptLbl, sep, emailLbl);
        return card;
    }
}