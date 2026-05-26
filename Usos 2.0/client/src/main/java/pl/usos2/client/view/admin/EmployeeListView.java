package pl.usos2.client.view.admin;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.user.Employee;
import pl.usos2.server.service.admin.EmployeeService;

public class EmployeeListView extends ScrollPane {

    private final EmployeeService employeeService;

    public EmployeeListView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));

        Label title = new Label(MockDataProvider.i18n("employee_directory_title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        FlowPane flowPane = new FlowPane(20, 20);

        for (Employee employee : employeeService.getAllEmployees()) {
            flowPane.getChildren().add(createEmployeeCard(employee));
        }

        container.getChildren().addAll(title, flowPane);
        setContent(container);
    }

    private VBox createEmployeeCard(Employee employee) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4); -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label nameLbl = new Label(employee.getFullName());
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLbl.setStyle("-fx-text-fill: #1e293b;");

        Label deptLbl = new Label(employee.getPosition());
        deptLbl.setTextFill(Color.GRAY);
        deptLbl.setFont(Font.font("System", 13));

        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 5 0;");

        Label emailLbl = new Label(employee.getEmail());
        emailLbl.setTextFill(Color.web("#2563eb"));
        emailLbl.setFont(Font.font("System", 12));

        card.getChildren().addAll(nameLbl, deptLbl, sep, emailLbl);
        return card;
    }
}