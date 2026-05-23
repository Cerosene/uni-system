package pl.usos2.client.view.admin;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SystemRequestsView extends VBox {
    public SystemRequestsView() {
        setPadding(new Insets(30));
        setSpacing(20);

        Label title = new Label("System Requests & Applications");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        TableView<Request> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Request, String> typeCol = new TableColumn<>("Type");
        TableColumn<Request, String> userCol = new TableColumn<>("From");
        TableColumn<Request, String> statusCol = new TableColumn<>("Status");

        typeCol.setCellValueFactory(d -> d.getValue().typeProperty());
        userCol.setCellValueFactory(d -> d.getValue().userProperty());
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());

        table.getColumns().addAll(typeCol, userCol, statusCol);

        // Dane demonstracyjne dla administratora
        table.getItems().addAll(
                new Request("Student Application", "M. Lewandowski", "Pending"),
                new Request("IT Support", "Prof. Nowak", "In Progress"),
                new Request("Scholarship Request", "I. Sikora", "New")
        );

        Button approveBtn = new Button("Process Selected");
        approveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");

        getChildren().addAll(title, table, approveBtn);
    }

    public static class Request {
        private final javafx.beans.property.StringProperty type;
        private final javafx.beans.property.StringProperty user;
        private final javafx.beans.property.StringProperty status;

        public Request(String t, String u, String s) {
            this.type = new javafx.beans.property.SimpleStringProperty(t);
            this.user = new javafx.beans.property.SimpleStringProperty(u);
            this.status = new javafx.beans.property.SimpleStringProperty(s);
        }
        public javafx.beans.property.StringProperty typeProperty() { return type; }
        public javafx.beans.property.StringProperty userProperty() { return user; }
        public javafx.beans.property.StringProperty statusProperty() { return status; }
    }
}