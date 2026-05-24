package pl.usos2.client.view.admin;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.usos2.client.util.MockDataProvider;

public class SystemRequestsView extends VBox {
    public SystemRequestsView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        // Dynamiczne pobieranie nagłówka głównego za pomocą i18n
        Label title = new Label(MockDataProvider.i18n("sys_requests_title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        TableView<Request> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Mapowanie kolumn tabeli z obsługą lokalizacji językowej
        TableColumn<Request, String> typeCol = new TableColumn<>(MockDataProvider.i18n("col_request_type"));
        TableColumn<Request, String> userCol = new TableColumn<>(MockDataProvider.i18n("col_request_from"));
        TableColumn<Request, String> statusCol = new TableColumn<>(MockDataProvider.i18n("col_request_status"));

        typeCol.setCellValueFactory(d -> d.getValue().typeProperty());
        userCol.setCellValueFactory(d -> d.getValue().userProperty());
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());

        table.getColumns().addAll(typeCol, userCol, statusCol);

        // Ładowanie testowych danych demonstracyjnych dla panelu administratora
        table.getItems().addAll(
                new Request("Student Application", "M. Lewandowski", "Pending"),
                new Request("IT Support", "Prof. Nowak", "In Progress"),
                new Request("Scholarship Request", "I. Sikora", "New")
        );

        // Definicja i nowoczesna stylizacja przycisku przetwarzania zgłoszenia
        Button approveBtn = new Button(MockDataProvider.i18n("btn_process_request"));
        approveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6;");

        // Obsługa kliknięcia: akcja zatwierdzania wybranego elementu z tabeli
        approveBtn.setOnAction(e -> {
            Request selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Wyświetlenie systemowego okna dialogowego po pomyślnym przetworzeniu wniosku
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(MockDataProvider.i18n("alert_info_title"));
                alert.setHeaderText(null);
                alert.setContentText(MockDataProvider.i18n("request_process_success"));
                alert.showAndWait();
            }
        });

        getChildren().addAll(title, table, approveBtn);
    }

    // Wewnętrzny model danych (POJO) reprezentujący pojedyncze zgłoszenie systemowe
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