package pl.usos2.client.view.admin;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import pl.usos2.client.util.MockDataProvider;
import pl.usos2.server.model.enumtype.RequestStatus;
import pl.usos2.server.model.request.Request;
import pl.usos2.server.service.request.RequestService;

public class SystemRequestsView extends VBox {

    public SystemRequestsView(RequestService requestService) {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label(MockDataProvider.i18n("sys_requests_title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        TableView<Request> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Request, String> typeCol = new TableColumn<>(MockDataProvider.i18n("col_request_type"));
        TableColumn<Request, String> userCol = new TableColumn<>(MockDataProvider.i18n("col_request_from"));
        TableColumn<Request, String> statusCol = new TableColumn<>(MockDataProvider.i18n("col_request_status"));
        TableColumn<Request, String> dateCol = new TableColumn<>(MockDataProvider.i18n("col_request_date"));
        TableColumn<Request, String> contentCol = new TableColumn<>(MockDataProvider.i18n("col_request_content"));

        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getType().toString()));
        userCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStudent().getFullName()));
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getCreatedAt() != null ? data.getValue().getCreatedAt().toString() : ""));
        contentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getContent()));

        table.getColumns().addAll(typeCol, userCol, statusCol, dateCol, contentCol);
        table.setItems(FXCollections.observableArrayList(requestService.getPendingRequests()));

        Button processBtn = new Button(MockDataProvider.i18n("btn_process_request"));
        processBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6;");
        processBtn.setOnAction(e -> {
            Request selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.getStatus() == RequestStatus.SUBMITTED) {
                    requestService.changeStatus(selected, RequestStatus.IN_REVIEW);
                } else if (selected.getStatus() == RequestStatus.IN_REVIEW) {
                    requestService.changeStatus(selected, RequestStatus.APPROVED);
                }
                table.setItems(FXCollections.observableArrayList(requestService.getPendingRequests()));
            }
        });

        getChildren().addAll(title, table, processBtn);
    }
}

